package com.zynergylabs.forager.brouter

import com.zynergylabs.forager.brouter.codec.DataBuffers
import com.zynergylabs.forager.brouter.mapaccess.PhysicalFile
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
    fun terminate(): Unit = TODO("ported with RoutingEngine, item I step 8")

    val isTerminated: Boolean
        get() = TODO("ported with RoutingEngine, item I step 8")
}

/** What a route call returns: the found track's nodes and distance, or the engine's own message unchanged. */
sealed interface RouteOutcome {
    class Found(val iLat: IntArray, val iLon: IntArray, val distance: Int) : RouteOutcome

    class Failed(val message: String) : RouteOutcome
}

/** Runs one route, blocking; the caller picks the thread. */
fun route(request: RouteRequest, cancel: RouteCancel): RouteOutcome =
    TODO("ported with RoutingEngine, item I step 8")

/** The header check Forager's `RoutingDataFolder.headerProblem` makes: null when the file reads, else the reason. */
fun checkDataFile(file: Path): String? = try {
    // Forager's RoutingDataFolder.headerProblem (RoutingDataFolder.kt:48-53 at Forager e2969abe): the header and footer
    // read as PhysicalFile's constructor reads them, without the lookup-version check.
    PhysicalFile(FileSystem.SYSTEM, file, DataBuffers(), -1, -1).close()
    null
} catch (e: IOException) {
    e.message ?: (e::class.simpleName ?: "IOException")
}
