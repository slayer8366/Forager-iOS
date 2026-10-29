package com.zynergylabs.forager.brouter.oracle

import btools.router.OsmNodeNamed
import btools.router.RoutingContext
import btools.router.RoutingEngine
import btools.router.RoutingParamCollector
import java.io.File

/**
 * Drives the oracle (BRouter 1.7.10's -ro jar, Phase 1 report item D) through the calls Forager makes
 * (`BRouterTrailRouter.kt:146-178` at Forager e2969abe4), as part 1's `ForagerCalls.java` does.
 */
object OracleCalls {
    /** Where the build put the fetched jar's profiles (brouter/build.gradle.kts, `brouter.oracle.dir`). */
    val oracleDir: File = File(
        System.getProperty("brouter.oracle.dir") ?: error("brouter.oracle.dir is not set; run through Gradle's jvmTest"),
    )

    class Result(val errorMessage: String?, val nodeCount: Int?, val distance: Int?)

    /** One route, [start] and [end] as (lat, lon), with Forager's profile and parameters. */
    fun route(start: DoubleArray, end: DoubleArray, segments: File, maxRunningTimeMillis: Long): Result {
        val collector = RoutingParamCollector()
        // Forager builds "lon,lat|lon,lat" with Kotlin's string templates, which use Double.toString on the JVM.
        val waypoints: List<OsmNodeNamed> = collector.getWayPointList("${start[1]},${start[0]}|${end[1]},${end[0]}")
        val context = RoutingContext()
        val params = hashMapOf(
            "profile" to File(oracleDir, "profiles2/hiking-mountain.brf").absolutePath,
            "alternativeidx" to "0",
            "profile:use_dynamic_range" to "0",
        )
        collector.setParams(context, waypoints, params)
        val engine = RoutingEngine(null, null, segments, waypoints, context, RoutingEngine.BROUTER_ENGINEMODE_ROUTING)
        engine.quite = true
        engine.doRun(maxRunningTimeMillis)
        // The getters, not properties: RoutingEngine has protected fields of these names (as Forager's
        // BRouterTrailRouter.kt:135 notes).
        val track = engine.getFoundTrack()
        return Result(engine.getErrorMessage(), track?.nodes?.size, track?.distance)
    }
}
