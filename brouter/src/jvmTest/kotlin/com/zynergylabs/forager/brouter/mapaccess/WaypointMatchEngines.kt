package com.zynergylabs.forager.brouter.mapaccess

import com.zynergylabs.forager.brouter.expressions.ExpressionsEngines
import com.zynergylabs.forager.brouter.oracle.e3.E3Case
import com.zynergylabs.forager.brouter.util.OracleLibm
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import java.io.File

/**
 * Step 5c (item I): both engines' `NodesCache.matchWaypointsToNodes` on one case's two ends, as `RoutingEngine`'s own
 * `matchWaypointsToNodes` (`RoutingEngine.java:1609-1614`) calls it on Forager's path: range `waypointCatchingRange`
 * (250, `RoutingContext.java:143`; no profile sets it), one call only, because Forager passes `use_dynamic_range=0`
 * (`BRouterTrailRouter.kt:154` at Forager a0535ece), and an empty island set of `MAXNODES_ISLAND_CHECK` (500,
 * `RoutingEngine.java:629`). The ends come from the oracle's own `RoutingParamCollector.getWayPointList`, in
 * `OracleCalls.route`'s form. The port runs on [OracleLibm], step 2b's logic-run seam.
 */
internal class WaypointMatchEngines(private val profile: File, private val segments: File) {
    private val oracleWay = ExpressionsEngines.oracle(profile).way
    private val portWay = ExpressionsEngines.port(profile).way

    /** One case's outcome on each engine, as one comparable line per end, plus the counts the revert is read by. */
    class Outcome(val oracle: List<String>, val port: List<String>, val oracleChoseReverse: Int, val oracleTies: Int)

    fun run(c: E3Case): Outcome {
        val ends = btools.router.RoutingParamCollector().getWayPointList("${c.startLon},${c.startLat}|${c.endLon},${c.endLat}")

        val om = ends.map { n ->
            btools.mapaccess.MatchedWaypoint().also { it.waypoint = n; it.name = n.name; it.wpttype = n.wpttype }
        }
        val oc = btools.mapaccess.NodesCache(segments, oracleWay, false, MAXMEM, null, false)
        val ook = try {
            oc.matchWaypointsToNodes(om, RANGE, btools.mapaccess.OsmNodePairSet(ISLAND))
        } finally {
            oc.close()
        }

        val pm = ends.map { n ->
            MatchedWaypoint().also {
                it.waypoint = OsmNode(n.ilon, n.ilat); it.name = n.name; it.wpttype = n.wpttype
            }
        }.toMutableList()
        val pc = NodesCache(FileSystem.SYSTEM, segments.toOkioPath(), portWay, false, MAXMEM, null, false)
        val pok = try {
            pc.matchWaypointsToNodes(OracleLibm, pm, RANGE, OsmNodePairSet(ISLAND))
        } finally {
            pc.close()
        }

        val o = om.map { "ok=$ook " + line(it.crosspoint?.xy(), it.node1?.xy(), it.node2?.xy(), it.radius, it.directionDiff, it.directionToNext, it.name, it.wpttype) + " | " +
            it.wayNearest.joinToString(" ; ") { w -> line(w.crosspoint?.xy(), w.node1?.xy(), w.node2?.xy(), w.radius, w.directionDiff, w.directionToNext, w.name, w.wpttype) } }
        val p = pm.map { "ok=$pok " + line(it.crosspoint?.xy(), it.node1?.xy(), it.node2?.xy(), it.radius, it.directionDiff, it.directionToNext, it.name, it.wpttype) + " | " +
            it.wayNearest.joinToString(" ; ") { w -> line(w.crosspoint?.xy(), w.node1?.xy(), w.node2?.xy(), w.radius, w.directionDiff, w.directionToNext, w.name, w.wpttype) } }
        val reverse = om.count { it.wayNearest.isNotEmpty() && it.wayNearest[0].name.contains("_w2_") }
        // A tie on radius at the head of the list: only the comparator's second line (WaypointMatcherImpl.java:71) orders it.
        val ties = om.count { it.wayNearest.size >= 2 && it.wayNearest[0].radius == it.wayNearest[1].radius }
        return Outcome(o, p, reverse, ties)
    }

    private fun btools.mapaccess.OsmNode.xy() = ilon to ilat
    private fun OsmNode.xy() = ilon to ilat

    private fun line(cross: Pair<Int, Int>?, n1: Pair<Int, Int>?, n2: Pair<Int, Int>?, radius: Double, diff: Double, toNext: Double, name: String?, type: Byte) =
        "cross=$cross n1=$n1 n2=$n2 r=${radius.toRawBits()} diff=${diff.toRawBits()} next=${toNext.toRawBits()} name=$name type=$type"

    companion object {
        const val RANGE = 250.0
        const val ISLAND = 500
        private const val MAXMEM = 64L * 1024 * 1024

        /** The fields of one end's line that differ, by name only, so that a failure message carries no coordinate. */
        fun differingFields(a: String, b: String): String {
            val fa = a.split(' ')
            val fb = b.split(' ')
            if (fa.size != fb.size) return "field count ${fa.size} vs ${fb.size}"
            return fa.indices.filter { fa[it] != fb[it] }.map { fa[it].substringBefore('=').ifEmpty { "#$it" } }.distinct().joinToString(",")
        }
    }
}
