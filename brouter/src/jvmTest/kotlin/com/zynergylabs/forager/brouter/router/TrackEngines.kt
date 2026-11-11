package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.mapaccess.NodesCache
import com.zynergylabs.forager.brouter.mapaccess.OsmLink
import com.zynergylabs.forager.brouter.mapaccess.OsmNode
import com.zynergylabs.forager.brouter.oracle.e3.E3Case
import com.zynergylabs.forager.brouter.oracle.e3.strata.E3F0hTest
import com.zynergylabs.forager.brouter.tiles.TestTiles
import kotlin.random.Random

/**
 * Step 7: tracks built from step 6's paths in both engines, side by side. From Forager's own ends (F0's two, F0h's
 * eight; E1's exception, RECORD -93), a seeded walk takes the same link index in both engines at every step, back
 * links included (so nodes repeat and `nextHolder` chains form), and makes each step's path with `createPath` in
 * detail mode, so each element carries its path's cost and message. The walk's last element is made with
 * `OsmPathElement.create(path)` and its `origin` chain is added to a track with `addNode`, as `compileTrack` does.
 * Nothing positional leaves this object except as element objects; the tests compare by index, bits and digest.
 */
internal object TrackEngines {
    class Walk(val o: btools.router.OsmPathElement, val p: OsmPathElement, val steps: Int)

    private val oCreateElement = btools.router.OsmPathElement::class.java
        .getMethod("create", Class.forName("btools.router.OsmPath"))

    fun cases(): List<E3Case> {
        val ends = E3F0hTest.ENDS
        return listOf("inside-across-the-degree-line", "inside-long").map { n ->
            val e = ends.getValue(n)
            E3Case("F0-$n", TestTiles.f0, e[0], e[1], e[2], e[3], E3F0hTest.PROFILE)
        } + ends.map { (n, e) -> E3Case("F0h-$n", TestTiles.f0h, e[0], e[1], e[2], e[3], E3F0hTest.PROFILE) }
    }

    /** Forager's own profile, as step 6's tests take it. */
    fun foragerProfile() = PathEngines.stdProfiles().first { it.name == "${E3F0hTest.PROFILE}.brf" }

    private fun oLinks(n: btools.mapaccess.OsmNode, nc: btools.mapaccess.NodesCache): List<btools.mapaccess.OsmLink> {
        val out = ArrayList<btools.mapaccess.OsmLink>()
        var l = n.firstlink
        while (l != null) {
            val x = l.getTarget(n)
            if (x.isHollow) nc.obtainNonHollowNode(x)
            if (!x.isHollow && x.firstlink != null && x !== n) out += l
            l = l.getNext(n)
        }
        return out
    }

    private fun pLinks(n: OsmNode, nc: NodesCache): List<OsmLink> {
        val out = ArrayList<OsmLink>()
        var l = n.firstlink
        while (l != null) {
            val x = l.getTarget(n)!!
            if (x.isHollow()) nc.obtainNonHollowNode(x)
            if (!x.isHollow() && x.firstlink != null && x !== n) out += l
            l = l.getNext(n)
        }
        return out
    }

    /** [count] walks of up to [maxSteps] steps on case [c]; the same [seed] gives the same walks. */
    fun walks(cx: PathEngines.Contexts, c: E3Case, seed: Long, count: Int, maxSteps: Int): List<Walk> {
        val wps = PathEngines.waypoints(c)
        val og = PathEngines.OracleGraph(c.segments, cx.o, wps, true)
        try {
            val pg = PathEngines.PortGraph(c.segments, cx.p, wps, true)
            try {
                check((og.refusal == null) == (pg.refusal == null)) { "${c.id}: one engine refused the ends" }
                if (og.refusal != null) return emptyList()
                val out = ArrayList<Walk>()
                for (w in 0 until count) {
                    val rnd = Random(seed * 1009 + w)
                    var on = og.nodes[w % 2]
                    var pn = pg.nodes[w % 2]
                    var op: Any = PathEngines.oCreate(cx.o, PathEngines.startLink(on))
                    var pp: OsmPath = cx.p.createPath(PathEngines.startLink(pn))
                    var steps = 0
                    while (steps < maxSteps) {
                        val ol = oLinks(on, og.nc)
                        val pl = pLinks(pn, pg.nc)
                        check(ol.size == pl.size) { "${c.id} walk $w step $steps: the engines' link counts differ" }
                        if (ol.isEmpty()) break
                        val k = rnd.nextInt(ol.size)
                        val onext = runCatching { PathEngines.oCreate(cx.o, op, ol[k], true) }.getOrNull()
                        val pnext = runCatching { cx.p.createPath(pp, pl[k], null, true) }.getOrNull()
                        check((onext == null) == (pnext == null)) { "${c.id} walk $w step $steps: one engine threw" }
                        if (onext == null || pnext == null) break
                        val oc = PathEngines.cost(onext)
                        check(oc == PathEngines.cost(pnext)) { "${c.id} walk $w step $steps: path costs differ" }
                        if (oc < 0) break
                        on = ol[k].getTarget(on)
                        pn = pl[k].getTarget(pn)!!
                        op = onext
                        pp = pnext
                        steps++
                    }
                    out += Walk(oCreateElement.invoke(null, op) as btools.router.OsmPathElement, OsmPathElement.create(pp), steps)
                }
                return out
            } finally {
                pg.close()
            }
        } finally {
            og.close()
        }
    }

    /** The two engines' tracks from a walk, as `compileTrack` fills them (`addNode` along the `origin` chain). */
    fun tracks(w: Walk): Pair<btools.router.OsmTrack, OsmTrack> {
        val ot = btools.router.OsmTrack()
        var oe: btools.router.OsmPathElement? = w.o
        while (oe != null) {
            ot.addNode(oe)
            oe = oe.origin
        }
        val pt = OsmTrack()
        var pe: OsmPathElement? = w.p
        while (pe != null) {
            pt.addNode(pe)
            pe = pe.origin
        }
        return ot to pt
    }

    /** A field read by Java's own name, on either engine's object (private fields included). */
    fun field(o: Any, name: String): Any? = PathEngines.field(o, name)

    fun <T> indexOf(list: List<T>, x: Any?): Int = if (x == null) -1 else list.indexOfFirst { it === x }
}
