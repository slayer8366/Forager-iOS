package com.zynergylabs.forager.brouter.oracle.e3

import btools.router.RoutingContext
import btools.router.RoutingEngine
import btools.router.RoutingParamCollector
import java.io.File
import java.io.RandomAccessFile

/**
 * Runs one [E3Case] on the oracle through Forager's calls (Phase 1 report, E1; `BRouterTrailRouter.kt:146-174` at
 * Forager e2969abe4): `getWayPointList("lon,lat|lon,lat")`, `setParams` with the profile, `alternativeidx=0` and
 * `profile:use_dynamic_range=0`, `quite = true`, `doRun(maxRunningTime)`. Forager calls `terminate()` only on a cancel;
 * FK's kinds 9 to 11 ([E3Case.Plan]) are the only cases with a time limit or a cancel. `memoryclass` is written after `setParams`; at 64 that is RoutingContext's own default.
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
        /** The class of the exception the engine logged a stack trace for, in a logged regime; else null. */
        val thrown: String? = null,
        /** A plan's measured run: its milliseconds, links processed and kind; null for [E3Case.Plan.NONE]. */
        val measured: String? = null,
    )

    fun run(case: E3Case, base: File): Result {
        val debug = File(base, "debug.txt")
        val logStart = if (debug.exists()) debug.length() else -1L
        val first = attempt(case, base, if (case.plan == E3Case.Plan.NONE) case.maxRunningTimeMillis else 0L, null)
        // A plan's lines are the measured run's only: the variant's depend on when the limit or the cancel struck.
        val logEnd = if (logStart < 0) -1L else debug.length()
        val lines = if (logStart < 0) emptyList() else addedLines(debug, logStart, logEnd)
        val collections = lines.filter { it.startsWith("collected, nodes/paths") || it.contains("memory limit reached") }
        val thrown = lines.firstNotNullOfOrNull { THROWN.matchEntire(it)?.groupValues?.get(1) }
        if (case.plan == E3Case.Plan.NONE) return Result(first.outcome, collections, first.extent, first.heightsByTile, thrown)
        val measured = "${first.millis} ms; ${first.links} links; ${first.outcome.kind}"
        val void = when (case.plan) {
            E3Case.Plan.LIMIT50 -> if (first.millis < 1_000) "void: the measured run took under 1 s" else null
            E3Case.Plan.CANCEL_BEFORE -> if (first.outcome.kind != "route") "void: the measured run did not route" else null
            E3Case.Plan.CANCEL_MID -> if (first.links < 100_000) "void: the measured run processed under 100,000 links" else null
            E3Case.Plan.NONE -> error("unreachable")
        }
        val outcome = if (void != null) {
            E3Outcome(void, 0, -1, -1, E3Outcome.fnv1a(emptyList()), E3Outcome.fnv1aShorts(emptyList()))
        } else {
            attempt(case, base, if (case.plan == E3Case.Plan.LIMIT50) 50L else 0L, case.plan).outcome
        }
        return Result(outcome, collections, null, null, thrown, measured)
    }

    /** One engine over [case]: a measured run (`cancel` null) or a plan's variant. */
    private class Attempt(
        val outcome: E3Outcome,
        val extent: IntArray?,
        val heightsByTile: Map<String, IntArray>?,
        val millis: Long,
        val links: Int,
    )

    private fun attempt(case: E3Case, base: File, maxRunningTime: Long, cancel: E3Case.Plan?): Attempt {
        var extent: IntArray? = null
        var heightsByTile: Map<String, IntArray>? = null
        var links = -1
        val t0 = System.nanoTime()
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
            when (cancel) {
                E3Case.Plan.CANCEL_BEFORE -> {
                    engine.terminate()
                    engine.doRun(maxRunningTime)
                }
                E3Case.Plan.CANCEL_MID -> {
                    var failure: Throwable? = null
                    val t = Thread { try { engine.doRun(maxRunningTime) } catch (e: Throwable) { failure = e } }
                    t.start()
                    // linksProcessed is a plain int written by the search thread; sleep between reads, as Forager's
                    // cancel comes from another thread too. Ends: the search thread finishes or the count passes 10,000.
                    while (t.isAlive && engine.getLinksProcessed() <= CANCEL_AFTER_LINKS) Thread.sleep(1)
                    engine.terminate()
                    t.join()
                    failure?.let { throw it }
                }
                else -> engine.doRun(maxRunningTime)
            }
            links = engine.getLinksProcessed()
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
        return Attempt(outcome, extent, heightsByTile, (System.nanoTime() - t0) / 1_000_000, links)
    }

    /** Kind 11's cancel point (E4): `terminate()` once `getLinksProcessed()` passes this. */
    const val CANCEL_AFTER_LINKS = 10_000

    /**
     * The first line of a stack trace the engine logs (`RoutingEngine.java:159-164`, after a non-IllegalArgument exception,
     * `:320-322`): the exception's class. FK7-x1 is compared by it (RECORD -80); it is read in the logged regimes only.
     */
    private val THROWN = Regex("""((?:[a-z][a-z0-9_]*\.)+[A-Z][A-Za-z0-9_$]*(?:Exception|Error))(?::.*)?""")

    /**
     * The 5° tile holding (ilon, ilat), named as the segment files are (`W75_N40` for lon -75 to -70, lat 40 to 45):
     * ilon is (lon + 180) × 10^6 and ilat (lat + 90) × 10^6, both non-negative, so integer division floors.
     */
    fun tileOf(ilon: Int, ilat: Int): String {
        val lon = ilon / 5_000_000 * 5 - 180
        val lat = ilat / 5_000_000 * 5 - 90
        return (if (lon < 0) "W${-lon}" else "E$lon") + "_" + (if (lat < 0) "S${-lat}" else "N$lat")
    }

    /** The lines this run appended to the log, from byte [from] to byte [to]. */
    private fun addedLines(debug: File, from: Long, to: Long): List<String> {
        val added = RandomAccessFile(debug, "r").use { f ->
            val bytes = ByteArray((to - from).toInt())
            f.seek(from)
            f.readFully(bytes)
            String(bytes, Charsets.UTF_8)
        }
        return added.lines()
    }
}
