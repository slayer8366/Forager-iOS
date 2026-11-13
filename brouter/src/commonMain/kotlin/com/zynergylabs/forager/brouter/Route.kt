package com.zynergylabs.forager.brouter

import com.zynergylabs.forager.brouter.codec.DataBuffers
import com.zynergylabs.forager.brouter.mapaccess.PhysicalFile
import com.zynergylabs.forager.brouter.router.RoutingContext
import com.zynergylabs.forager.brouter.router.RoutingEngine
import com.zynergylabs.forager.brouter.router.RoutingParamCollector
import com.zynergylabs.forager.brouter.util.LibmSeam
import com.zynergylabs.forager.brouter.util.MsunLibm
import kotlin.concurrent.Volatile
import okio.FileSystem
import okio.IOException
import okio.Path

// The port's public surface, all of it (Phase 1 report, item C, "what the port exposes"): exactly what Forager's adapter
// calls today, and nothing more. The engine's classes will be internal. Step 0 of item I declares it as stubs; the
// bodies come with the steps that port them (5a for the data-file check, 8 for the route and the cancel flag), each
// with its tests (the planner's ruling, 2026-10-29: port-side tests arrive with the step that ports them).

/** One route between two points, as Forager asks for it. */
class RouteRequest(
    val startLon: Double,
    val startLat: Double,
    val endLon: Double,
    val endLat: Double,
    /** The folder holding the `.rd5` tiles. */
    val routeData: Path,
    /** The `.brf` profile file; `lookups.dat` sits beside it. */
    val profile: Path,
    /** Forager's: `alternativeidx=0`, `profile:use_dynamic_range=0`. */
    val params: Map<String, String>,
    /** 0 turns the engine's time limit off. */
    val maxRunningTimeMillis: Long,
)

/** The engine's terminate flag, which a second thread sets (item B, row 7). */
class RouteCancel {
    // Forager calls terminate() from another thread than the run's (BRouterTrailRouter.kt:166-174 at Forager 947f1be2).
    // Both fields are volatile: terminate() writes the flag then reads the engine, route() writes the engine then reads
    // the flag, so at least one of the two forwards Java's terminate() to the engine (step 8 report §9.3).
    @Volatile
    private var requested = false

    @Volatile
    private var engine: RoutingEngine? = null

    /** Java's `RoutingEngine.terminate()` (`RoutingEngine.java:2432-2434`) on the engine of the [route] given this cancel. */
    fun terminate() {
        requested = true
        engine?.terminate()
    }

    /** Java's `isTerminated()` (`RoutingEngine.java:2436-2438`); before [route] builds the engine, the flag set here. */
    val isTerminated: Boolean
        get() = engine?.isTerminated() ?: requested

    internal fun attach(e: RoutingEngine) {
        engine = e
        if (requested) e.terminate()
    }
}

/**
 * What a route call returns: the found track's nodes, their heights and its distance, or the engine's own message unchanged.
 * [Found.sElev] is each node's `OsmPathElement.getSElev()`, in quarter metres, `Short.MIN_VALUE` where the data has no
 * height (RECORD -111, -122; Forager's `BRouterTrailRouter.heightMetresOf` converts it, as it converts the positions).
 */
sealed interface RouteOutcome {
    class Found(val iLat: IntArray, val iLon: IntArray, val distance: Int, val sElev: ShortArray) : RouteOutcome

    class Failed(val message: String) : RouteOutcome
}

/** Runs one route, blocking; the caller picks the thread. */
fun route(request: RouteRequest, cancel: RouteCancel): RouteOutcome = route(request, cancel, MsunLibm)

/** [route] with the libm seam given: tests' logic runs pass the oracle's (-24); the public call passes `MsunLibm`. */
internal fun route(request: RouteRequest, cancel: RouteCancel, libm: LibmSeam): RouteOutcome {
    // BRouterTrailRouter.buildEngine (:142-160 at Forager 947f1be2): the waypoints as "lon,lat|lon,lat", a context, the
    // profile and Forager's params, the engine on the tiles, quite. The constructor's exceptions pass through (:126-131).
    val collector = RoutingParamCollector()
    val waypoints = collector.getWayPointList("${request.startLon},${request.startLat}|${request.endLon},${request.endLat}")
    val context = RoutingContext(libm)
    collector.setParams(context, waypoints, request.params + ("profile" to request.profile.toString()))
    val engine = RoutingEngine(FileSystem.SYSTEM, request.routeData, waypoints, context, RoutingEngine.BROUTER_ENGINEMODE_ROUTING)
    engine.quite = true
    cancel.attach(engine)
    // runToEnd (:166-174): doRun with the limit. Then (:133-139) the message if set, else the found track.
    engine.doRun(request.maxRunningTimeMillis)
    val message = engine.getErrorMessage()
    if (message != null) return RouteOutcome.Failed(message)
    val nodes = engine.getFoundTrack().nodes
    return RouteOutcome.Found(
        IntArray(nodes.size) { nodes[it].getILat() },
        IntArray(nodes.size) { nodes[it].getILon() },
        engine.getFoundTrack().distance,
        ShortArray(nodes.size) { nodes[it].getSElev() },
    )
}

/** The header check Forager's `RoutingDataFolder.headerProblem` makes: null when the file reads, else the reason. */
fun checkDataFile(file: Path): String? = try {
    // Forager's RoutingDataFolder.headerProblem (RoutingDataFolder.kt:48-53 at Forager e2969abe): the header and footer
    // read as PhysicalFile's constructor reads them, without the lookup-version check.
    PhysicalFile(FileSystem.SYSTEM, file, DataBuffers(), -1, -1).close()
    null
} catch (e: IOException) {
    e.message ?: (e::class.simpleName ?: "IOException")
}
