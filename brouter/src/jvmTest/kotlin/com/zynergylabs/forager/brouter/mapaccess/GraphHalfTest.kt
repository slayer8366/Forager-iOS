package com.zynergylabs.forager.brouter.mapaccess

import com.zynergylabs.forager.brouter.expressions.ExpressionsEngines
import com.zynergylabs.forager.brouter.oracle.OracleCalls
import com.zynergylabs.forager.brouter.oracle.e3.E3Case
import com.zynergylabs.forager.brouter.oracle.e3.RGenerator
import com.zynergylabs.forager.brouter.oracle.e3.strata.E3F0hTest
import com.zynergylabs.forager.brouter.oracle.e3.strata.E3RTest
import com.zynergylabs.forager.brouter.tiles.TestTiles
import com.zynergylabs.forager.brouter.util.OracleLibm
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import java.io.File
import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Step 6, 5b's debt (RECORD -99): `NodesCache`'s graph half (`getGraphNode`, `obtainNonHollowNode`,
 * `expandHollowLinkTargets`, `hasHollowLinkTargets`, `getStartNode`, `NodesCache.java:213-300`) on both engines, from
 * real ends on real tiles: F0's two, F0h's eight, and the first [R_SAMPLE] of R's on Forager's profile (its own seed).
 *
 * Each end pair is matched first, as `RoutingEngine.matchWaypointsToNodes` does; then a second cache is made over the
 * first, as `resetCache` does, and `_findTrack`'s graph steps run on it (`RoutingEngine.java:1911-1928`): the four
 * graph nodes, the two obtains and expands, then a search-like walk from both starts that obtains each hollow target
 * (`:2152`) and asks `hasHollowLinkTargets` (`:2006`) of each node reached, up to [WALK] nodes. Every call's result is
 * written, then every node reachable from the four (fields, links, geometry, as [GraphTranscript] writes them), so the
 * two texts are compared field by field. This runs in cleanup modes 1 and 2 (`:1880`), and in mode 1 again with a
 * small `maxmem`, which turns on the garbage collection branch (`NodesCache.java:229-231`). `getStartNode` (`:605`)
 * runs on a fresh cache, on the start's id and on an id beside it that is no node (its null return).
 */
class GraphHalfTest {
    @Test
    fun graphHalfMatchesTheOracleOnForagersEnds() {
        val ends = E3F0hTest.ENDS
        val f0 = listOf("inside-across-the-degree-line", "inside-long").map { n ->
            val e = ends.getValue(n)
            E3Case("F0-$n", TestTiles.f0, e[0], e[1], e[2], e[3], E3F0hTest.PROFILE)
        }
        val f0h = ends.map { (n, e) -> E3Case("F0h-$n", TestTiles.f0h, e[0], e[1], e[2], e[3], E3F0hTest.PROFILE) }
        val gen = RGenerator(TestTiles.segments, E3RTest.SEED)
        val r = try {
            gen.generate("R", E3RTest.PROFILE, R_SAMPLE, 20 * R_SAMPLE, TestTiles.segments).cases
        } finally {
            gen.close()
        }
        assertEquals(R_SAMPLE, r.size, "R's sample is short")

        val md = MessageDigest.getInstance("SHA-256")
        var runs = 0
        var nullStarts = 0
        var obtainedFalse = 0
        var lines = 0L
        val profile = File(OracleCalls.oracleDir, "profiles2/${E3F0hTest.PROFILE}.brf")
        val oracleWay = ExpressionsEngines.oracle(profile).way
        val portWay = ExpressionsEngines.port(profile).way
        for (c in f0 + f0h + r) {
            val wps = btools.router.RoutingParamCollector().getWayPointList("${c.startLon},${c.startLat}|${c.endLon},${c.endLat}")
            for ((mode, maxmem) in listOf(1 to MAXMEM, 2 to MAXMEM, 1 to SMALL_MAXMEM)) {
                val o = OracleSide(c.segments, oracleWay).run(wps, mode, maxmem)
                val p = PortSide(c.segments, portWay).run(wps, mode, maxmem)
                runs++
                val d = GraphTranscript.firstDifference(o, p)
                if (d != null) fail("${c.id} mode $mode maxmem $maxmem: $d")
                md.update(o.toByteArray())
                lines += o.count { it == '\n' }
                nullStarts += Regex("getStartNode [^\n]* null").findAll(o).count()
                obtainedFalse += Regex("obtain [^\n]* false").findAll(o).count()
            }
        }
        println(
            "6 graph half: ${f0.size + f0h.size + r.size} cases, $runs runs, $lines lines, getStartNode null $nullStarts, " +
                "obtain false $obtainedFalse, digest ${md.digest().joinToString("") { "%02x".format(it) }}",
        )
        assertTrue(nullStarts > 0, "the sample never reached getStartNode's null return")
    }

    /** The oracle's side: Java's own `NodesCache`. */
    private class OracleSide(val segments: File, val way: btools.expressions.BExpressionContextWay) {
        fun run(wps: List<btools.router.OsmNodeNamed>, mode: Int, maxmem: Long): String {
            val t = GraphTranscript(OracleGraph)
            val mw = wps.map { n -> btools.mapaccess.MatchedWaypoint().also { it.waypoint = n; it.name = n.name } }
            val first = btools.mapaccess.NodesCache(segments, way, false, maxmem, null, false)
            val matched = outcome { first.matchWaypointsToNodes(mw, WaypointMatchEngines.RANGE, btools.mapaccess.OsmNodePairSet(WaypointMatchEngines.ISLAND)) }
            t.line("match $matched")
            if (mw.any { it.node1 == null || it.node2 == null }) return t.text()
            val nc = btools.mapaccess.NodesCache(segments, way, false, maxmem, first, false)
            nc.nodesMap.cleanupMode = mode
            try {
                val s1 = nc.getGraphNode(mw[0].node1)
                val s2 = nc.getGraphNode(mw[0].node2)
                val e1 = nc.getGraphNode(mw[1].node1)
                val e2 = nc.getGraphNode(mw[1].node2)
                nc.nodesMap.endNode1 = e1
                nc.nodesMap.endNode2 = e2
                t.line("same ${s1 === e1} ${s2 === e2} ${s1 === e2} ${s2 === e1} hollow ${s1.isHollow} ${s2.isHollow} ${e1.isHollow} ${e2.isHollow}")
                val walk = ArrayList<btools.mapaccess.OsmNode>()
                for (s in listOf(s1, s2)) {
                    t.line("obtain ${s.ilon} ${s.ilat} ${outcome { nc.obtainNonHollowNode(s) }}")
                    t.line("expand ${outcome { nc.expandHollowLinkTargets(s); true }} hasHollow ${outcome { nc.hasHollowLinkTargets(s) }}")
                    walk += s
                }
                val seen = java.util.IdentityHashMap<btools.mapaccess.OsmNode, Boolean>()
                var i = 0
                while (i < walk.size && i < WALK) {
                    val n = walk[i++]
                    t.line("walk ${n.ilon} ${n.ilat} hasHollow ${outcome { nc.hasHollowLinkTargets(n) }}")
                    var l = n.firstlink
                    while (l != null) {
                        val x = l.getTarget(n)
                        if (x.isHollow) t.line("obtain ${x.ilon} ${x.ilat} ${outcome { nc.obtainNonHollowNode(x) }}")
                        if (!x.isHollow && seen.put(x, true) == null) walk += x
                        l = l.getNext(n)
                    }
                }
                t.mapState(nc.nodesMap)
                t.reachable(listOf(s1, s2, e1, e2), holders = false)
            } finally {
                nc.close()
            }
            val fresh = btools.mapaccess.NodesCache(segments, way, false, maxmem, null, false)
            try {
                for (id in listOf(mw[0].node1.idFromPos, mw[0].node1.idFromPos + 1)) {
                    val n = try { fresh.getStartNode(id) } catch (e: RuntimeException) { t.line("getStartNode $id ${e.javaClass.name}: ${e.message}"); null }
                    t.line("getStartNode $id ${if (n == null) "null" else "${n.ilon} ${n.ilat} hasHollow ${fresh.hasHollowLinkTargets(n)}"}")
                    if (n != null) t.reachable(listOf(n), holders = false)
                }
            } finally {
                fresh.close()
            }
            return t.text()
        }
    }

    /** The port's side, line for line as [OracleSide]. */
    private class PortSide(val segments: File, val way: com.zynergylabs.forager.brouter.expressions.BExpressionContextWay) {
        fun run(wps: List<btools.router.OsmNodeNamed>, mode: Int, maxmem: Long): String {
            val t = GraphTranscript(PortGraph)
            val dir = segments.toOkioPath()
            val mw = wps.map { n -> MatchedWaypoint().also { it.waypoint = OsmNode(n.ilon, n.ilat); it.name = n.name } }.toMutableList()
            val first = NodesCache(FileSystem.SYSTEM, dir, way, false, maxmem, null, false)
            val matched = outcome { first.matchWaypointsToNodes(OracleLibm, mw, WaypointMatchEngines.RANGE, OsmNodePairSet(WaypointMatchEngines.ISLAND)) }
            t.line("match $matched")
            if (mw.any { it.node1 == null || it.node2 == null }) return t.text()
            val nc = NodesCache(FileSystem.SYSTEM, dir, way, false, maxmem, first, false)
            nc.nodesMap.cleanupMode = mode
            try {
                val s1 = nc.getGraphNode(mw[0].node1!!)
                val s2 = nc.getGraphNode(mw[0].node2!!)
                val e1 = nc.getGraphNode(mw[1].node1!!)
                val e2 = nc.getGraphNode(mw[1].node2!!)
                nc.nodesMap.endNode1 = e1
                nc.nodesMap.endNode2 = e2
                t.line("same ${s1 === e1} ${s2 === e2} ${s1 === e2} ${s2 === e1} hollow ${s1.isHollow()} ${s2.isHollow()} ${e1.isHollow()} ${e2.isHollow()}")
                val walk = ArrayList<OsmNode>()
                for (s in listOf(s1, s2)) {
                    t.line("obtain ${s.ilon} ${s.ilat} ${outcome { nc.obtainNonHollowNode(s) }}")
                    t.line("expand ${outcome { nc.expandHollowLinkTargets(s); true }} hasHollow ${outcome { nc.hasHollowLinkTargets(s) }}")
                    walk += s
                }
                val seen = java.util.IdentityHashMap<OsmNode, Boolean>()
                var i = 0
                while (i < walk.size && i < WALK) {
                    val n = walk[i++]
                    t.line("walk ${n.ilon} ${n.ilat} hasHollow ${outcome { nc.hasHollowLinkTargets(n) }}")
                    var l = n.firstlink
                    while (l != null) {
                        val x = l.getTarget(n)!!
                        if (x.isHollow()) t.line("obtain ${x.ilon} ${x.ilat} ${outcome { nc.obtainNonHollowNode(x) }}")
                        if (!x.isHollow() && seen.put(x, true) == null) walk += x
                        l = l.getNext(n)
                    }
                }
                t.mapState(nc.nodesMap)
                t.reachable(listOf(s1, s2, e1, e2), holders = false)
            } finally {
                nc.close()
            }
            val fresh = NodesCache(FileSystem.SYSTEM, dir, way, false, maxmem, null, false)
            try {
                val id0 = mw[0].node1!!.getIdFromPos()
                for (id in listOf(id0, id0 + 1)) {
                    val n = try { fresh.getStartNode(id) } catch (e: RuntimeException) { t.line("getStartNode $id ${e.javaClass.name}: ${e.message}"); null }
                    t.line("getStartNode $id ${if (n == null) "null" else "${n.ilon} ${n.ilat} hasHollow ${fresh.hasHollowLinkTargets(n)}"}")
                    if (n != null) t.reachable(listOf(n), holders = false)
                }
            } finally {
                fresh.close()
            }
            return t.text()
        }
    }

    companion object {
        const val R_SAMPLE = 20
        const val WALK = 300
        private const val MAXMEM = 64L * 1024 * 1024
        private const val SMALL_MAXMEM = 64L * 1024

        /** The call's result, or the exception's class and message, so a throw on one engine is compared as a value. */
        private inline fun outcome(block: () -> Any?): String = try {
            block().toString()
        } catch (e: RuntimeException) {
            "${e.javaClass.name.substringAfterLast('.')}: ${e.message}"
        } catch (e: NotImplementedError) {
            "NotImplementedError: ${e.message}"
        }
    }
}
