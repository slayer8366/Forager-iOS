package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.mapaccess.GraphTranscript
import com.zynergylabs.forager.brouter.mapaccess.OsmNode
import com.zynergylabs.forager.brouter.oracle.e3.E3Case
import com.zynergylabs.forager.brouter.oracle.e3.strata.E3F0hTest
import com.zynergylabs.forager.brouter.tiles.TestTiles
import java.security.MessageDigest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Step 6: `OsmPath` and `StdPath` against the oracle's on decoded links, on each of the 20 `StdModel` profiles.
 *
 * From each of F0's two and F0h's eight ends (Forager's own, `E3F0hTest.ENDS`), the graph is prepared as `_findTrack`
 * prepares it ([PathEngines.OracleGraph]), then walked breadth first from both starts, obtaining each hollow target as
 * the search does (`RoutingEngine.java:2148`). From each of the first [EXTEND] nodes walked, every link to a usable
 * target (non-hollow, not a dead end, not itself) gets a start path (`createPath(new OsmLink(null, n))`, as
 * `getStartPath` makes one), that path extended over the link, and the result extended once more over each of its
 * target's links: in plain and in detail mode. Each path is written whole ([PathEngines.state]), so `cost`, the
 * hysteresis buffers, `bitfield`, the message and the `originElement` chain (with `OsmPathElement.create(path)`'s
 * element, first reached here) are compared by Java's field names. No waypoint is set, so the cut-end branches are
 * not reached here; `StartPathTest` reaches them from Forager's real ends.
 */
class StdPathTest {
    @Test
    fun pathsMatchTheOracleOnEveryStdProfile() {
        val ends = E3F0hTest.ENDS
        val cases = listOf("inside-across-the-degree-line", "inside-long").map { n ->
            val e = ends.getValue(n)
            E3Case("F0-$n", TestTiles.f0, e[0], e[1], e[2], e[3], E3F0hTest.PROFILE)
        } + ends.map { (n, e) -> E3Case("F0h-$n", TestTiles.f0h, e[0], e[1], e[2], e[3], E3F0hTest.PROFILE) }
        val profiles = PathEngines.stdProfiles()
        assertEquals(20, profiles.size, "StdModel profiles: ${profiles.map { it.name }}")
        val md = MessageDigest.getInstance("SHA-256")
        var paths = 0L
        var refused = 0L
        var elements = 0L
        val turnCosts = HashSet<String>()
        for (profile in profiles) {
            val cx = PathEngines.contexts(profile)!!
            try {
                for (c in cases) {
                    val wps = PathEngines.waypoints(c)
                    for (detail in listOf(false, true)) {
                        val o = oracleRun(cx, c, wps, detail)
                        val p = portRun(cx, c, wps, detail)
                        val d = GraphTranscript.firstDifference(o, p)
                        if (d != null) fail("${profile.name} ${c.id} detail $detail: $d")
                        md.update(o.toByteArray())
                        paths += o.lines().count { it.startsWith("path ") }
                        refused += o.lines().count { it.startsWith("path ") && it.contains(" cost=-1 ") }
                        elements += Regex(" <- ").findAll(o).count()
                        if (Regex("linkturncost=[1-9]").containsMatchIn(o)) turnCosts += profile.name
                    }
                }
            } finally {
                cx.release()
            }
        }
        println(
            "6 std path: ${profiles.size} profiles, ${cases.size} cases, $paths paths, $refused refused (cost -1), " +
                "$elements origin links, turn costs seen on ${turnCosts.sorted()}, " +
                "digest ${md.digest().joinToString("") { "%02x".format(it) }}",
        )
        assertTrue(refused in 1 until paths, "the sample never reached a refused path, or refused them all")
        assertTrue(elements > 0, "detail mode never built an origin chain")
    }

    private fun oracleRun(cx: PathEngines.Contexts, c: E3Case, wps: List<btools.router.OsmNodeNamed>, detail: Boolean): String {
        val sb = StringBuilder()
        val g = PathEngines.OracleGraph(c.segments, cx.o, wps, detail)
        try {
            if (g.refusal != null) return "refusal ${g.refusal}\n"
            val walk = ArrayList<btools.mapaccess.OsmNode>(g.nodes.subList(0, 2))
            val seen = java.util.IdentityHashMap<btools.mapaccess.OsmNode, Boolean>()
            var i = 0
            while (i < walk.size && i < EXTEND) {
                val n = walk[i++]
                sb.append("node ").append(i).append('\n')
                var l = n.firstlink
                while (l != null) {
                    val x = l.getTarget(n)
                    if (x.isHollow) g.nc.obtainNonHollowNode(x)
                    if (!x.isHollow && x.firstlink != null && x !== n) {
                        if (seen.put(x, true) == null) walk += x
                        val start = PathEngines.oCreate(cx.o, PathEngines.startLink(n))
                        val p1 = PathEngines.outcomeOr(sb) { PathEngines.oCreate(cx.o, start, l!!, detail) }
                        if (p1 != null && PathEngines.cost(p1) >= 0) {
                            var l2 = x.firstlink
                            while (l2 != null) {
                                val y = l2.getTarget(x)
                                if (y.isHollow) g.nc.obtainNonHollowNode(y)
                                if (!y.isHollow && y.firstlink != null && y !== n) PathEngines.outcomeOr(sb) { PathEngines.oCreate(cx.o, p1, l2!!, detail) }
                                l2 = l2.getNext(x)
                            }
                        }
                    }
                    l = l.getNext(n)
                }
            }
        } finally {
            g.close()
        }
        return sb.toString()
    }

    private fun portRun(cx: PathEngines.Contexts, c: E3Case, wps: List<btools.router.OsmNodeNamed>, detail: Boolean): String {
        val sb = StringBuilder()
        val g = PathEngines.PortGraph(c.segments, cx.p, wps, detail)
        try {
            if (g.refusal != null) return "refusal ${g.refusal}\n"
            val walk = ArrayList<OsmNode>(g.nodes.subList(0, 2))
            val seen = java.util.IdentityHashMap<OsmNode, Boolean>()
            var i = 0
            while (i < walk.size && i < EXTEND) {
                val n = walk[i++]
                sb.append("node ").append(i).append('\n')
                var l = n.firstlink
                while (l != null) {
                    val x = l.getTarget(n)!!
                    if (x.isHollow()) g.nc.obtainNonHollowNode(x)
                    if (!x.isHollow() && x.firstlink != null && x !== n) {
                        if (seen.put(x, true) == null) walk += x
                        val start = cx.p.createPath(PathEngines.startLink(n))
                        val p1 = PathEngines.outcomeOr(sb) { cx.p.createPath(start, l!!, null, detail) }
                        if (p1 != null && PathEngines.cost(p1) >= 0) {
                            var l2 = x.firstlink
                            while (l2 != null) {
                                val y = l2.getTarget(x)!!
                                if (y.isHollow()) g.nc.obtainNonHollowNode(y)
                                if (!y.isHollow() && y.firstlink != null && y !== n) PathEngines.outcomeOr(sb) { cx.p.createPath(p1 as OsmPath, l2!!, null, detail) }
                                l2 = l2.getNext(x)
                            }
                        }
                    }
                    l = l.getNext(n)
                }
            }
        } finally {
            g.close()
        }
        return sb.toString()
    }

    /** -97: the start-direction branch (`OsmPath.java:282-283`) refuses by name; Forager passes no heading. */
    @Test
    fun startDirectionIsUnsupported() {
        val profile = PathEngines.stdProfiles().first { it.name == "${E3F0hTest.PROFILE}.brf" }
        val cx = PathEngines.contexts(profile)!!
        try {
            val e = E3F0hTest.ENDS.entries.first()
            val c = E3Case("F0h-${e.key}", TestTiles.f0h, e.value[0], e.value[1], e.value[2], e.value[3], E3F0hTest.PROFILE)
            val g = PathEngines.PortGraph(c.segments, cx.p, PathEngines.waypoints(c), false)
            try {
                assertEquals(null, g.refusal)
                val n = g.nodes[0]
                val l = generateSequence(n.firstlink) { it.getNext(n) }.first { !it.getTarget(n)!!.isHollow() }
                cx.p.startDirection = 90
                cx.p.startDirectionValid = true
                val start = cx.p.createPath(PathEngines.startLink(n))
                val ex = assertFailsWith<UnsupportedOperationException> { cx.p.createPath(start, l, null, false) }
                assertEquals("unsupported: OsmPath's start direction (OsmPath.java:282-283)", ex.message)
                cx.p.startDirectionValid = false
                assertTrue(cx.p.createPath(start, l, null, false).cost >= -1, "the same path without a start direction")
            } finally {
                cx.p.startDirection = null
                g.close()
            }
        } finally {
            cx.release()
        }
    }

    /** `interpolateEle` (`OsmPath.java:388-393`) on seeded heights and fractions, its `(short)` cast included. */
    @Test
    fun interpolateEleMatchesTheOracle() {
        val oc = Class.forName("btools.router.StdPath").getDeclaredConstructor().also { it.isAccessible = true }
        val o = oc.newInstance()
        val m = Class.forName("btools.router.OsmPath").getDeclaredMethod(
            "interpolateEle", java.lang.Short.TYPE, java.lang.Short.TYPE, java.lang.Double.TYPE,
        ).also { it.isAccessible = true }
        val p = StdPath()
        val rnd = Random(20261111L)
        var cut = 0
        repeat(100_000) { i ->
            val e1 = if (i % 50 == 0) Short.MIN_VALUE else rnd.nextInt(-2000, 40000).toShort()
            val e2 = if (i % 70 == 0) Short.MIN_VALUE else rnd.nextInt(-2000, 40000).toShort()
            val f = if (i % 3 == 0) rnd.nextDouble() else rnd.nextInt(0, 1001) / 1000.0
            val want = m.invoke(o, e1, e2, f) as Short
            val got = p.interpolateEle(e1, e2, f)
            assertEquals(want, got, "case $i: interpolateEle($e1, $e2, $f)")
            val raw = e1 * (1.0 - f) + e2 * f
            if (want != Short.MIN_VALUE && raw.toInt().toDouble() != raw) cut++
        }
        assertTrue(cut > 0, "no case had a fraction for the cast to cut")
    }

    companion object {
        const val EXTEND = 40
    }
}
