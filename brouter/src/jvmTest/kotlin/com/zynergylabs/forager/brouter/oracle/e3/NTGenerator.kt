package com.zynergylabs.forager.brouter.oracle.e3

import btools.codec.WaypointMatcher
import btools.mapaccess.MatchedWaypoint
import btools.mapaccess.OsmNode
import btools.router.RoutingContext
import btools.router.RoutingEngine
import btools.router.RoutingParamCollector
import com.zynergylabs.forager.brouter.oracle.OracleCalls
import java.io.Closeable
import java.io.File
import java.lang.reflect.InvocationTargetException
import java.util.Random
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow

/**
 * NT's generator (Phase 1 report, E4: "a start nearest a junction node, and an end placed so the two ways' `directionDiff`s
 * differ by at most 4 ulps on the oracle's JVM"; `WaypointMatcherImpl.java:66-73`, `:194-235`).
 *
 * **Why the construction is a straight line through a junction.** A link's direction in the matcher is `atan2` of its two
 * end nodes' integer deltas (`CheapAngleMeter.java:50-56`), and the start's direction is `atan2` of the two waypoints'.
 * With the start exactly on the junction node J, every link at J has radius 0 (`WaypointMatcherImpl.java:110-128`), so the
 * comparator orders them by `directionDiff` alone (`:69-71`). Two links' diffs within 4 ulps (about 10^-14 degrees) need
 * either an end on their exact bisector, which integer ends can approach only to about 10^-11 degrees inside a tile, or two
 * links with one direction: N1 - J = -k (N2 - J), a zero integer cross product. Then N1→J and J→N2 are one direction up to
 * `atan2`'s and `normalize`'s rounding, whatever the end. So a draw is:
 * 1. a node J0 drawn as R draws a start ([RGenerator.drawNode], `W75_N40` only);
 * 2. in J0's cell, the junction nearest J0 with two links (of those `hiking-mountain` may use, decoded as the matcher
 *    decodes them, [TileNodes.decodeLinks]) whose far nodes lie exactly opposite through it; none: rejected;
 * 3. the start on J exactly, and the end the node nearest a point at R's distance (100 m × 1000^u) along that line, turned
 *    by up to 30° either way, so the pair are the two smallest diffs;
 * 4. the oracle's own matching, run in this JVM on the case ([probe]): the start's crosspoint is J and its two nearest
 *    entries of different links have equal radius and diffs at most 4 ulps apart. Otherwise rejected, with the gap.
 * Steps 1 to 4 are geometry, before routing; each draw counts toward the cap.
 */
class NTGenerator(private val segments: File, private val seed: Long) : Closeable {
    private val gen = RGenerator(segments, seed, only = TILE)
    private val tile = TileNodes(File(segments, TILE))
    private val validator = context(41.0, -74.0, 41.01, -74.01).second.expctxWay

    val nodeCounts get() = gen.nodeCounts

    /** One matched entry of the start's `wayNearest`, as the oracle left it. */
    class Entry(val radius: Double, val directionDiff: Double, val n1: Long, val n2: Long) {
        val link: Set<Long> get() = setOf(n1, n2)
    }

    /** The oracle's matching of the start: its crosspoint and `wayNearest`, in the comparator's order. */
    class Probe(val crosspoint: Long?, val entries: List<Entry>)

    fun draws(prefix: String, profile: String): Iterator<E3Select.Draw> = iterator {
        val rnd = Random(seed)
        var k = 0
        while (true) { // ends: the selector stops drawing at its cap
            k++
            val id = "%s-%04d".format(prefix, k)
            when (val c = candidate(rnd)) {
                is String -> yield(E3Select.Draw(k, null, c))
                else -> {
                    val e = c as DoubleArray
                    yield(E3Select.Draw(k, E3Case(id, segments, e[0], e[1], e[2], e[3], profile), null))
                }
            }
        }
    }

    private fun candidate(rnd: Random): Any {
        val j0 = gen.drawNode(rnd)
        val u = rnd.nextDouble()
        val side = rnd.nextBoolean()
        val turn = (rnd.nextDouble() * 2 - 1) * PI / 6
        val (j, far) = junction(j0) ?: return "no junction with two exactly opposite links in the drawn node's cell"
        val lat = gen.lat(j)
        val lon = gen.lon(j)
        val toward = if (side) far.first else far.second
        val dLat = TileNodes.ilat(toward) - TileNodes.ilat(j).toDouble()
        val dLon = (TileNodes.ilon(toward) - TileNodes.ilon(j)) * cos(Math.toRadians(lat))
        val bearing = atan2(dLon, dLat) + turn
        val (tLat, tLon) = gen.offset(lat, lon, 100.0 * 1000.0.pow(u), bearing)
        val end = gen.nearest(tLat, tLon) ?: return "no node in the target's 3 x 3 cells, or the target is outside the tile"
        if (end == j) return "the end's node is the junction"
        val out = doubleArrayOf(E3Case.six(lat), E3Case.six(lon), E3Case.six(gen.lat(end)), E3Case.six(gen.lon(end)))
        val p = try {
            probe(out)
        } catch (e: IllegalArgumentException) {
            return "the oracle's matching refused it: ${e.message}"
        }
        if (p.crosspoint != j) return "the start's crosspoint is not the junction"
        val first = p.entries.firstOrNull() ?: return "no matched entry"
        val second = p.entries.firstOrNull { it.link != first.link } ?: return "one link only among the start's entries"
        if (second.radius != first.radius) return "the two links' radii differ"
        val gap = ulps(first.directionDiff, second.directionDiff)
        if (gap > MAX_ULPS) return "gap over $MAX_ULPS ulps"
        return out
    }

    /** The junction in [j0]'s cell nearest [j0] with two exactly opposite links, and those links' far nodes; or null. */
    private fun junction(j0: Long): Pair<Long, Pair<Long, Long>>? {
        val divisor = tile.divisor
        val lonIdx = TileNodes.ilon(j0) / (1_000_000 / divisor)
        val latIdx = TileNodes.ilat(j0) / (1_000_000 / divisor)
        val links = ArrayList<LongArray>() // source, target
        val collector = object : WaypointMatcher {
            override fun start(ilonStart: Int, ilatStart: Int, ilonTarget: Int, ilatTarget: Int, useAsStartWay: Boolean): Boolean {
                links += longArrayOf(id(ilonStart, ilatStart), id(ilonTarget, ilatTarget))
                return false // no geometry wanted: the decoder then skips transferNode and end
            }

            override fun transferNode(ilon: Int, ilat: Int) = Unit

            override fun end() = Unit

            override fun hasMatch(lon: Int, lat: Int) = false
        }
        for (di in -1..1) for (dj in -1..1) {
            if (tile.holds(lonIdx + di, latIdx + dj)) tile.decodeLinks(lonIdx + di, latIdx + dj, validator, collector)
        }
        val inCell = { n: Long -> TileNodes.ilon(n) / (1_000_000 / divisor) == lonIdx && TileNodes.ilat(n) / (1_000_000 / divisor) == latIdx }
        val around = HashMap<Long, MutableSet<Long>>()
        for ((s, t) in links.map { it[0] to it[1] }) {
            if (s == t) continue
            if (inCell(s)) around.getOrPut(s) { LinkedHashSet() } += t
            if (inCell(t)) around.getOrPut(t) { LinkedHashSet() } += s
        }
        val cosLat = cos(Math.toRadians(gen.lat(j0)))
        fun dist2(a: Long): Double {
            val dx = (TileNodes.ilon(a) - TileNodes.ilon(j0)) * cosLat
            val dy = (TileNodes.ilat(a) - TileNodes.ilat(j0)).toDouble()
            return dx * dx + dy * dy
        }
        for (j in around.keys.sortedWith(compareBy<Long>({ dist2(it) }, { it }))) {
            val ns = around.getValue(j).sorted()
            for (a in ns.indices) for (b in a + 1 until ns.size) {
                val ux = (TileNodes.ilon(ns[a]) - TileNodes.ilon(j)).toLong()
                val uy = (TileNodes.ilat(ns[a]) - TileNodes.ilat(j)).toLong()
                val vx = (TileNodes.ilon(ns[b]) - TileNodes.ilon(j)).toLong()
                val vy = (TileNodes.ilat(ns[b]) - TileNodes.ilat(j)).toLong()
                if (ux * vy - uy * vx == 0L && ux * vx + uy * vy < 0L) return j to (ns[a] to ns[b])
            }
        }
        return null
    }

    /** The oracle's matching of [ends] (lat, lon, lat, lon), through Forager's calls and the engine's own private step. */
    fun probe(ends: DoubleArray): Probe {
        val (collector, rc) = context(ends[0], ends[1], ends[2], ends[3])
        val waypoints = collector.first
        val engine = RoutingEngine(null, null, segments, waypoints, rc, RoutingEngine.BROUTER_ENGINEMODE_ROUTING)
        val matched = waypoints.map { w ->
            MatchedWaypoint().also { it.waypoint = w; it.name = w.name; it.wpttype = w.wpttype }
        }
        val m = RoutingEngine::class.java.getDeclaredMethod("matchWaypointsToNodes", List::class.java)
        m.isAccessible = true
        try {
            m.invoke(engine, matched)
        } catch (e: InvocationTargetException) {
            throw e.cause ?: e
        }
        val start = matched[0]
        return Probe(
            start.crosspoint?.let { id(it.ilon, it.ilat) },
            start.wayNearest.map { Entry(it.radius, it.directionDiff, id(it.node1.ilon, it.node1.ilat), id(it.node2.ilon, it.node2.ilat)) },
        )
    }

    /** Forager's calls up to the engine ([E3CaseRun]): the waypoints, and the context with `hiking-mountain` set. */
    private fun context(sLat: Double, sLon: Double, eLat: Double, eLon: Double): Pair<Pair<List<btools.router.OsmNodeNamed>, Unit>, RoutingContext> {
        val collector = RoutingParamCollector()
        val waypoints = collector.getWayPointList("$sLon,$sLat|$eLon,$eLat")
        val rc = RoutingContext()
        val params = hashMapOf(
            "profile" to File(OracleCalls.oracleDir, "profiles2/$PROFILE.brf").absolutePath,
            "alternativeidx" to "0",
            "profile:use_dynamic_range" to "0",
        )
        collector.setParams(rc, waypoints, params)
        // The constructor parses the profile (RoutingEngine.java:133), which sets the way context.
        RoutingEngine(null, null, segments, waypoints, rc, RoutingEngine.BROUTER_ENGINEMODE_ROUTING)
        return (waypoints to Unit) to rc
    }

    override fun close() {
        gen.close()
        tile.close()
    }

    companion object {
        const val TILE = "W75_N40.rd5"
        const val PROFILE = "hiking-mountain"
        const val MAX_ULPS = 4L

        fun id(ilon: Int, ilat: Int): Long = ilon.toLong() shl 32 or (ilat.toLong() and 0xffffffffL)

        /** The distance in ulps between two non-negative doubles: the difference of their bit patterns. */
        fun ulps(a: Double, b: Double): Long {
            require(a >= 0.0 && b >= 0.0) { "directionDiff is an abs (CheapAngleMeter.java:72): $a, $b" }
            return abs(java.lang.Double.doubleToLongBits(a) - java.lang.Double.doubleToLongBits(b))
        }
    }
}
