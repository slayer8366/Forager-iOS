package com.zynergylabs.forager.brouter.oracle.e3

import btools.router.RoutingContext
import btools.router.RoutingEngine
import btools.router.RoutingParamCollector
import java.io.File
import java.io.RandomAccessFile

/**
 * Runs one [E3Case] on the oracle through Forager's calls (Phase 1 report, E1; `BRouterTrailRouter.kt:146-174` at
 * Forager e2969abe4): `getWayPointList("lon,lat|lon,lat")`, `setParams` with the profile, `alternativeidx=0` and
 * `profile:use_dynamic_range=0`, `quite = true`, `doRun(maxRunningTime)`. Forager calls `terminate()` only on a cancel,
 * and no case here has one. `memoryclass` is written after `setParams`; at 64 that is RoutingContext's own default.
 *
 * [base] holds `profiles2/` (a copy of the oracle's) and, for a logged regime, `debug.txt`, which switches the engine's
 * log on (`RoutingEngine.java:112-121`). The engine appends to it, so the lines this run added are read from the
 * file's length before the run.
 */
object E3CaseRun {
    /**
     * [extent] is a route's node extent, `minIlon, maxIlon, minIlat, maxIlat`, or null when no route was found: DL's reach
     * evidence ("the found route has nodes on both sides", E4) reads it against the line. [heightsByTile] is, for a found
     * route, each tile's (5° square's) count of the route's nodes with a height (not `Short.MIN_VALUE`) and of all its
     * nodes, the data RECORD -58 owes on R's routes; null when no route was found.
     */
    class Result(
        val outcome: E3Outcome,
        val collections: List<String>,
        val extent: IntArray?,
        val heightsByTile: Map<String, IntArray>? = null,
    )

    fun run(case: E3Case, base: File): Result {
        val debug = File(base, "debug.txt")
        val logStart = if (debug.exists()) debug.length() else -1L
        var extent: IntArray? = null
        var heightsByTile: Map<String, IntArray>? = null
        val outcome = try {
            val collector = RoutingParamCollector()
            val waypoints = collector.getWayPointList(
                "${case.startLon},${case.startLat}|${case.endLon},${case.endLat}",
            )
            val context = RoutingContext()
            val params = hashMapOf(
                "profile" to File(base, "profiles2/${case.profile}.brf").absolutePath,
                "alternativeidx" to "0",
                "profile:use_dynamic_range" to "0",
            )
            collector.setParams(context, waypoints, params)
            context.memoryclass = case.memoryclass
            val engine = RoutingEngine(null, null, case.segments, waypoints, context, RoutingEngine.BROUTER_ENGINEMODE_ROUTING)
            engine.quite = true
            engine.doRun(case.maxRunningTimeMillis)
            val message = engine.getErrorMessage()
            val track = engine.getFoundTrack()
            val pairs = track?.nodes?.map { it.getILon() to it.getILat() } ?: emptyList()
            val heights = track?.nodes?.map { it.getSElev() } ?: emptyList()
            if (message == null && pairs.isNotEmpty()) {
                extent = intArrayOf(pairs.minOf { it.first }, pairs.maxOf { it.first }, pairs.minOf { it.second }, pairs.maxOf { it.second })
                heightsByTile = pairs.indices.groupBy { tileOf(pairs[it].first, pairs[it].second) }
                    .mapValues { (_, i) -> intArrayOf(i.count { heights[it] != Short.MIN_VALUE }, i.size) }
                    .toSortedMap()
            }
            E3Outcome(
                E3Outcome.kindOf(message), pairs.size, track?.cost ?: -1, track?.distance ?: -1, E3Outcome.fnv1a(pairs),
                E3Outcome.fnv1aShorts(heights),
            )
        } catch (e: Exception) {
            // The constructor parses the profile; doRun catches what routing throws. Recorded as its own kind.
            E3Outcome(
                "threw:" + (e.message ?: e.javaClass.simpleName), 0, -1, -1, E3Outcome.fnv1a(emptyList()),
                E3Outcome.fnv1aShorts(emptyList()),
            )
        }
        return Result(outcome, if (logStart < 0) emptyList() else collectionLines(debug, logStart), extent, heightsByTile)
    }

    /**
     * The 5° tile holding (ilon, ilat), named as the segment files are (`W75_N40` for lon -75 to -70, lat 40 to 45):
     * ilon is (lon + 180) × 10^6 and ilat (lat + 90) × 10^6, both non-negative, so integer division floors.
     */
    fun tileOf(ilon: Int, ilat: Int): String {
        val lon = ilon / 5_000_000 * 5 - 180
        val lat = ilat / 5_000_000 * 5 - 90
        return (if (lon < 0) "W${-lon}" else "E$lon") + "_" + (if (lat < 0) "S${-lat}" else "N$lat")
    }

    /** The log's collection lines (`RoutingEngine.java:2025`, `:2031`) that this run appended after [from]. */
    private fun collectionLines(debug: File, from: Long): List<String> {
        val added = RandomAccessFile(debug, "r").use { f ->
            val bytes = ByteArray((f.length() - from).toInt())
            f.seek(from)
            f.readFully(bytes)
            String(bytes, Charsets.UTF_8)
        }
        return added.lines().filter { it.startsWith("collected, nodes/paths") || it.contains("memory limit reached") }
    }
}
