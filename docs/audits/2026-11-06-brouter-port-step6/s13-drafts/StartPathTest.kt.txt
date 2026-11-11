package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.mapaccess.GraphTranscript
import com.zynergylabs.forager.brouter.mapaccess.MatchedWaypoint
import com.zynergylabs.forager.brouter.mapaccess.OsmLink
import com.zynergylabs.forager.brouter.mapaccess.OsmNode
import com.zynergylabs.forager.brouter.oracle.e3.E3Case
import com.zynergylabs.forager.brouter.oracle.e3.RGenerator
import com.zynergylabs.forager.brouter.oracle.e3.strata.E3F0hTest
import com.zynergylabs.forager.brouter.oracle.e3.strata.E3RProfilesTest
import com.zynergylabs.forager.brouter.oracle.e3.strata.E3RTest
import com.zynergylabs.forager.brouter.tiles.TestTiles
import com.zynergylabs.forager.brouter.util.OracleLibm
import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Step 6, RECORD -99: the path-cost comparison from Forager's real ends, over the ported graph half. Test code copies
 * `RoutingEngine.getStartPath`'s steps (`RoutingEngine.java:1814-1870`, both overloads) after `_findTrack`'s graph
 * steps (`:1911-1928`, [PathEngines.OracleGraph]), and extends each start path once over its target's links as the
 * search does (`:2146-2227`), with the end waypoint set on the final link (`:2214-2225`). So the cut ends are reached:
 * the start cut (`OsmPath.java:236-255`, `interpolateEle` at `1 - wayfraction`), the pending end on the same segment,
 * and the end cut (`:233-235`).
 *
 * Ends: F0's two and F0h's eight (`E3F0hTest.ENDS`), on each of the 20 `StdModel` profiles; and the first [R_SAMPLE]
 * of R's ends on each of those profiles, from its own fixed seed (`E3RProfilesTest.SEEDS`; `hiking-mountain`'s is
 * `E3RTest.SEED`). Plain mode (the search, cleanup mode 1 or 2) and detail mode (the final pass, cleanup mode 0).
 */
class StartPathTest {
    @Test
    fun startPathsMatchTheOracleFromForagersEnds() {
        val ends = E3F0hTest.ENDS
        val fixed = listOf("inside-across-the-degree-line", "inside-long").map { n ->
            val e = ends.getValue(n)
            E3Case("F0-$n", TestTiles.f0, e[0], e[1], e[2], e[3], E3F0hTest.PROFILE)
        } + ends.map { (n, e) -> E3Case("F0h-$n", TestTiles.f0h, e[0], e[1], e[2], e[3], E3F0hTest.PROFILE) }
        val profiles = PathEngines.stdProfiles()
        assertEquals(20, profiles.size, "StdModel profiles: ${profiles.map { it.name }}")
        val md = MessageDigest.getInstance("SHA-256")
        var runs = 0
        var starts = 0
        var finals = 0
        var sameSegment = 0
        var cutStarts = 0
        for (profile in profiles) {
            val name = profile.name.removeSuffix(".brf")
            val seed = if (name == E3RTest.PROFILE) E3RTest.SEED else E3RProfilesTest.SEEDS.getValue(name)
            val gen = RGenerator(TestTiles.segments, seed)
            val r = try {
                gen.generate("R", name, R_SAMPLE, 20 * R_SAMPLE, TestTiles.segments).cases
            } finally {
                gen.close()
            }
            assertEquals(R_SAMPLE, r.size, "R's sample on $name is short")
            val cx = PathEngines.contexts(profile)!!
            try {
                for (c in fixed + r) {
                    val wps = PathEngines.waypoints(c)
                    for (detail in listOf(false, true)) {
                        val o = oracleRun(cx, c, wps, detail)
                        val p = portRun(cx, c, wps, detail)
                        runs++
                        val d = GraphTranscript.firstDifference(o, p)
                        if (d != null) fail("$name ${c.id} detail $detail: $d")
                        md.update(o.toByteArray())
                        starts += o.lines().count { it.startsWith("start") && !it.endsWith("null") }
                        finals += o.lines().count { it.startsWith("final ") }
                        if (o.contains("same true")) sameSegment++
                        if (detail) cutStarts += o.lines().count { it.startsWith("start") && it.contains("originElement=[") }
                    }
                }
            } finally {
                cx.release()
            }
        }
        println(
            "6 start paths: ${profiles.size} profiles, $runs runs, $starts start paths, $finals final links, " +
                "$sameSegment same-segment runs, $cutStarts cut starts in detail mode, " +
                "digest ${md.digest().joinToString("") { "%02x".format(it) }}",
        )
        assertTrue(finals > 0, "no run reached a final link with the end waypoint set")
        assertTrue(cutStarts > 0, "no detail run cut a start")
    }

    private fun oracleRun(cx: PathEngines.Contexts, c: E3Case, wps: List<btools.router.OsmNodeNamed>, detail: Boolean): String {
        val sb = StringBuilder()
        val rc = cx.o
        val g = PathEngines.OracleGraph(c.segments, rc, wps, detail)
        try {
            if (g.refusal != null) return "refusal ${g.refusal}\n"
            val (s1, s2, e1, e2) = g.nodes
            val endPos = btools.router.OsmNodeNamed(g.mw[1].crosspoint)
            val same = (s1 === e1 && s2 === e2) || (s1 === e2 && s2 === e1)
            sb.append("same $same\n")
            rc.startDirectionValid = false
            val sp = listOf(
                getStartPath(rc, s1, s2, g.mw[0], endPos, same, detail),
                getStartPath(rc, s2, s1, g.mw[0], endPos, same, detail),
            )
            for ((i, p) in sp.withIndex()) sb.append("start${i + 1} ").append(PathEngines.state(p)).append('\n')
            val endIds = setOf(g.mw[1].node1.idFromPos, g.mw[1].node2.idFromPos)
            for (p in sp) {
                if (p == null || PathEngines.cost(p) < 0) continue
                val current = PathEngines.field(p, "targetNode") as btools.mapaccess.OsmNode
                val source = PathEngines.field(p, "sourceNode")
                var l = current.firstlink
                while (l != null) {
                    val next = l.getTarget(current)
                    if (g.nc.obtainNonHollowNode(next) && next.firstlink != null && next !== source) {
                        val isFinal = current.idFromPos in endIds && next.idFromPos in endIds
                        try {
                            if (isFinal) {
                                endPos.radius = 1.5
                                rc.setWaypoint(endPos, true)
                                sb.append("final ")
                            }
                            PathEngines.outcomeOr(sb) { PathEngines.oCreate(rc, p, l!!, detail) }
                        } finally {
                            if (isFinal) rc.unsetWaypoint()
                        }
                    }
                    l = l.getNext(current)
                }
            }
        } finally {
            g.close()
        }
        return sb.toString()
    }

    /** `RoutingEngine.java:1814-1824` and `:1826-1870`, on the oracle's types. */
    private fun getStartPath(
        rc: btools.router.RoutingContext, n1: btools.mapaccess.OsmNode, n2: btools.mapaccess.OsmNode,
        mwp: btools.mapaccess.MatchedWaypoint, endPos: btools.router.OsmNodeNamed?, same: Boolean, detail: Boolean,
    ): Any? {
        if (endPos != null) endPos.radius = 1.5
        val wp = btools.router.OsmNodeNamed(mwp.crosspoint)
        val p = try {
            rc.setWaypoint(wp, if (same) endPos else null, false)
            var bestPath: Any? = null
            var bestLink: btools.mapaccess.OsmLink? = null
            val startLink = PathEngines.startLink(n1)
            val startPath = PathEngines.oCreate(rc, startLink)
            startLink.addLinkHolder(startPath as btools.mapaccess.OsmLinkHolder, null)
            var minradius = 1e10
            var link = n1.firstlink
            while (link != null) {
                val nextNode = link.getTarget(n1)
                if (!nextNode.isHollow && nextNode.firstlink != null && nextNode !== n1 && nextNode === n2) {
                    wp.radius = 1.5
                    val testPath = PathEngines.oCreate(rc, startPath, link, detail)
                    PathEngines.setField(testPath, "airdistance", if (endPos == null) 0 else nextNode.calcDistance(endPos))
                    if (wp.radius < minradius) {
                        bestPath = testPath
                        minradius = wp.radius
                        bestLink = link
                    }
                }
                link = link.getNext(n1)
            }
            bestLink?.addLinkHolder(bestPath as btools.mapaccess.OsmLinkHolder, n1)
            if (bestPath != null) PathEngines.setField(bestPath, "treedepth", 1)
            bestPath
        } finally {
            rc.unsetWaypoint()
        }
        if (p != null && PathEngines.cost(p) >= 0 && same && endPos != null && endPos.radius < 1.5) PathEngines.setField(p, "treedepth", 0)
        return p
    }

    private fun portRun(cx: PathEngines.Contexts, c: E3Case, wps: List<btools.router.OsmNodeNamed>, detail: Boolean): String {
        val sb = StringBuilder()
        val rc = cx.p
        val g = PathEngines.PortGraph(c.segments, rc, wps, detail)
        try {
            if (g.refusal != null) return "refusal ${g.refusal}\n"
            val (s1, s2, e1, e2) = g.nodes
            val endPos = OsmNodeNamed(g.mw[1].crosspoint!!)
            val same = (s1 === e1 && s2 === e2) || (s1 === e2 && s2 === e1)
            sb.append("same $same\n")
            rc.startDirectionValid = false
            val sp = listOf(
                getStartPath(rc, s1, s2, g.mw[0], endPos, same, detail),
                getStartPath(rc, s2, s1, g.mw[0], endPos, same, detail),
            )
            for ((i, p) in sp.withIndex()) sb.append("start${i + 1} ").append(PathEngines.state(p)).append('\n')
            val endIds = setOf(g.mw[1].node1!!.getIdFromPos(), g.mw[1].node2!!.getIdFromPos())
            for (p in sp) {
                if (p == null || PathEngines.cost(p) < 0) continue
                val current = PathEngines.field(p, "targetNode") as OsmNode
                val source = PathEngines.field(p, "sourceNode")
                var l = current.firstlink
                while (l != null) {
                    val next = l.getTarget(current)!!
                    if (g.nc.obtainNonHollowNode(next) && next.firstlink != null && next !== source) {
                        val isFinal = current.getIdFromPos() in endIds && next.getIdFromPos() in endIds
                        try {
                            if (isFinal) {
                                endPos.radius = 1.5
                                rc.setWaypoint(endPos, true)
                                sb.append("final ")
                            }
                            PathEngines.outcomeOr(sb) { rc.createPath(p, l!!, null, detail) }
                        } finally {
                            if (isFinal) rc.unsetWaypoint()
                        }
                    }
                    l = l.getNext(current)
                }
            }
        } finally {
            g.close()
        }
        return sb.toString()
    }

    /** The port's side of [getStartPath], line for line. */
    private fun getStartPath(
        rc: RoutingContext, n1: OsmNode, n2: OsmNode, mwp: MatchedWaypoint, endPos: OsmNodeNamed?, same: Boolean,
        detail: Boolean,
    ): OsmPath? {
        if (endPos != null) endPos.radius = 1.5
        val wp = OsmNodeNamed(mwp.crosspoint!!)
        val p = try {
            rc.setWaypoint(wp, if (same) endPos else null, false)
            var bestPath: OsmPath? = null
            var bestLink: OsmLink? = null
            val startLink = PathEngines.startLink(n1)
            val startPath = rc.createPath(startLink)
            startLink.addLinkHolder(startPath, null)
            var minradius = 1e10
            var link = n1.firstlink
            while (link != null) {
                val nextNode = link.getTarget(n1)!!
                if (!nextNode.isHollow() && nextNode.firstlink != null && nextNode !== n1 && nextNode === n2) {
                    wp.radius = 1.5
                    val testPath = rc.createPath(startPath, link, null, detail)
                    PathEngines.setField(testPath, "airdistance", if (endPos == null) 0 else nextNode.calcDistance(OracleLibm, endPos))
                    if (wp.radius < minradius) {
                        bestPath = testPath
                        minradius = wp.radius
                        bestLink = link
                    }
                }
                link = link.getNext(n1)
            }
            bestLink?.addLinkHolder(bestPath!!, n1)
            if (bestPath != null) PathEngines.setField(bestPath, "treedepth", 1)
            bestPath
        } finally {
            rc.unsetWaypoint()
        }
        if (p != null && p.cost >= 0 && same && endPos != null && endPos.radius < 1.5) PathEngines.setField(p, "treedepth", 0)
        return p
    }

    companion object {
        const val R_SAMPLE = 5
    }
}
