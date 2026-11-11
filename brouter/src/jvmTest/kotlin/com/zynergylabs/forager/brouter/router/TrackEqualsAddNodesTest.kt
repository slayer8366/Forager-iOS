package com.zynergylabs.forager.brouter.router

import java.security.MessageDigest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Step 7 (RECORD -120): `OsmTrack.equalsTrack` and `OsmTrack.addNodes` against the oracle's, on tracks built from step
 * 6's paths ([TrackEngines]). `equalsTrack` is called on the same pairs on both engines: a track against a second track
 * from the same walk, a copy of it, copies with one node's longitude or latitude moved by one unit, copies one node
 * shorter and one node longer, the next walk's track and an empty track, each in both directions. `addNodes` is run
 * onto an empty track (`RoutingEngine.java:991`'s first call), onto another walk's track, and twice in a row; the
 * result is compared per node by which input element it is (by identity) and by `getLink`/`containsNode` over the map
 * it builds, and the two engines' digests must agree. A failure names the case, walk, variant or mode and index, never
 * a position (E1).
 */
class TrackEqualsAddNodesTest {
    /** A track of fresh elements at the same positions, in the same order, with node [k] moved by ([dLon], [dLat]). */
    private fun oCopy(t: btools.router.OsmTrack, drop: Int = 0, extra: Boolean = false, k: Int = -1, dLon: Int = 0,
                      dLat: Int = 0): btools.router.OsmTrack {
        val c = btools.router.OsmTrack()
        val n = t.nodes.size - drop
        if (extra) c.addNode(btools.router.OsmPathElement.create(t.nodes[n - 1].iLon, t.nodes[n - 1].iLat, 0, null))
        for (i in n - 1 downTo 0) {
            val e = t.nodes[i]
            val d = if (i == k) 1 else 0
            c.addNode(btools.router.OsmPathElement.create(e.iLon + d * dLon, e.iLat + d * dLat, 0, null))
        }
        return c
    }

    private fun pCopy(t: OsmTrack, drop: Int = 0, extra: Boolean = false, k: Int = -1, dLon: Int = 0,
                      dLat: Int = 0): OsmTrack {
        val c = OsmTrack()
        val n = t.nodes.size - drop
        if (extra) c.addNode(OsmPathElement.create(t.nodes[n - 1].getILon(), t.nodes[n - 1].getILat(), 0, null))
        for (i in n - 1 downTo 0) {
            val e = t.nodes[i]
            val d = if (i == k) 1 else 0
            c.addNode(OsmPathElement.create(e.getILon() + d * dLon, e.getILat() + d * dLat, 0, null))
        }
        return c
    }

    @Test
    fun equalsTrackMatchesTheOracle() {
        val cx = PathEngines.contexts(TrackEngines.foragerProfile())!!
        val md = MessageDigest.getInstance("SHA-256")
        val seen = mutableMapOf<String, Int>()
        var trues = 0
        var falses = 0
        try {
            for ((ci, c) in TrackEngines.cases().withIndex()) {
                val walks = TrackEngines.walks(cx, c, 7201L + ci, 6, 40)
                for ((wi, w) in walks.withIndex()) {
                    val (ot, pt) = TrackEngines.tracks(w)
                    assertEquals(ot.nodes.size, pt.nodes.size, "${c.id} walk $wi: node count")
                    val n = ot.nodes.size
                    val rnd = Random(9201L + ci * 31 + wi)
                    val pairs = ArrayList<Triple<String, btools.router.OsmTrack, OsmTrack>>()
                    val (os, ps) = TrackEngines.tracks(w)
                    pairs += Triple("same walk", os, ps)
                    pairs += Triple("copy", oCopy(ot), pCopy(pt))
                    for (k in listOf(0, n - 1, rnd.nextInt(n))) {
                        pairs += Triple("lon at $k", oCopy(ot, k = k, dLon = 1), pCopy(pt, k = k, dLon = 1))
                        pairs += Triple("lat at $k", oCopy(ot, k = k, dLat = 1), pCopy(pt, k = k, dLat = 1))
                    }
                    if (n > 1) pairs += Triple("shorter", oCopy(ot, drop = 1), pCopy(pt, drop = 1))
                    pairs += Triple("longer", oCopy(ot, extra = true), pCopy(pt, extra = true))
                    if (wi + 1 < walks.size) {
                        val (on, pn) = TrackEngines.tracks(walks[wi + 1])
                        pairs += Triple("next walk", on, pn)
                    }
                    pairs += Triple("empty", btools.router.OsmTrack(), OsmTrack())
                    for ((v, o2, p2) in pairs) {
                        for (dir in 0 until 2) {
                            val id = "${c.id} walk $wi: $v, direction $dir"
                            val oe = if (dir == 0) ot.equalsTrack(o2) else o2.equalsTrack(ot)
                            val pe = if (dir == 0) pt.equalsTrack(p2) else p2.equalsTrack(pt)
                            assertEquals(oe, pe, "$id: equalsTrack")
                            if (oe) trues++ else falses++
                            seen.merge(v.substringBefore(" at"), 1, Int::plus)
                            md.update((if (oe) "1" else "0").toByteArray())
                        }
                    }
                }
            }
        } finally {
            cx.release()
        }
        println(
            "7 equals track: $trues true, $falses false, by variant ${seen.toSortedMap()}, " +
                "digest ${md.digest().joinToString("") { "%02x".format(it) }}",
        )
        assertTrue(trues > 0 && falses > 0, "equalsTrack was not seen both true and false: $trues true, $falses false")
        for (v in listOf("same walk", "copy", "lon", "lat", "shorter", "longer", "next walk", "empty")) {
            assertTrue((seen[v] ?: 0) > 0, "variant never reached: $v")
        }
    }

    /** Per node: which input element it is ("a:i", "b:i", "c:i" by identity); then the map's links and membership. */
    private fun <T : Any> state(nodes: List<T>, inputs: List<Pair<String, List<T>>>, ids: List<Long>,
                                link: (Long, Long) -> T?, contains: (T) -> Boolean): List<String> {
        val out = ArrayList<String>()
        for ((i, e) in nodes.withIndex()) {
            val src = inputs.firstNotNullOfOrNull { (name, l) ->
                l.indexOfFirst { it === e }.takeIf { it >= 0 }?.let { "$name:$it" }
            } ?: "none"
            val back = if (i > 0) TrackEngines.indexOf(nodes, link(ids[i - 1], ids[i])) else -2
            val fwd = if (i > 0) TrackEngines.indexOf(nodes, link(ids[i], ids[i - 1])) else -2
            out += "i=$i src=$src link=$back rlink=$fwd contains=${contains(e)}"
        }
        return out
    }

    @Test
    fun addNodesMatchesTheOracle() {
        val cx = PathEngines.contexts(TrackEngines.foragerProfile())!!
        val od = MessageDigest.getInstance("SHA-256")
        val pd = MessageDigest.getInstance("SHA-256")
        val counts = IntArray(3)
        var nodes = 0L
        var linked = 0L
        try {
            for ((ci, c) in TrackEngines.cases().withIndex()) {
                for (mode in 0 until 3) {
                    // Fresh walks for each mode, as in AppendTrackTest.
                    val walks = TrackEngines.walks(cx, c, 7301L + ci, 6, 40)
                    var wi = 0
                    val need = if (mode == 0) 1 else 2
                    while (wi + need <= walks.size) {
                        val id = "mode $mode ${c.id} walk $wi"
                        val (ob, pb) = TrackEngines.tracks(walks[wi])
                        val (or, pr) = if (mode == 1) TrackEngines.tracks(walks[wi + 1])
                        else btools.router.OsmTrack() to OsmTrack()
                        val oIn = listOf("a" to or.nodes.toList(), "b" to ob.nodes.toList())
                        val pIn = listOf("a" to pr.nodes.toList(), "b" to pb.nodes.toList())
                        or.addNodes(ob)
                        pr.addNodes(pb)
                        val (oInAll, pInAll) = if (mode == 2) {
                            val (oc, pc) = TrackEngines.tracks(walks[wi + 1])
                            val oi = oIn + ("c" to oc.nodes.toList())
                            val pi = pIn + ("c" to pc.nodes.toList())
                            or.addNodes(oc)
                            pr.addNodes(pc)
                            oi to pi
                        } else oIn to pIn
                        assertEquals(or.nodes.size, pr.nodes.size, "$id: node count")
                        val os = state(or.nodes, oInAll, or.nodes.map { it.idFromPos }, { a, b -> or.getLink(a, b) },
                            { or.containsNode(it) })
                        val ps = state(pr.nodes, pInAll, pr.nodes.map { it.getIdFromPos() },
                            { a, b -> pr.getLink(a, b) }, { pr.containsNode(it) })
                        for (k in os.indices) assertEquals(os[k], ps[k], "$id: line $k")
                        os.forEach { od.update(it.toByteArray()) }
                        ps.forEach { pd.update(it.toByteArray()) }
                        linked += os.count { !it.contains(" link=-1") && !it.contains(" link=-2") }
                        counts[mode]++
                        nodes += or.nodes.size
                        wi += need
                    }
                }
            }
        } finally {
            cx.release()
        }
        val o = od.digest().joinToString("") { "%02x".format(it) }
        val p = pd.digest().joinToString("") { "%02x".format(it) }
        println(
            "7 add nodes: ${counts[0]} onto empty, ${counts[1]} onto a track, ${counts[2]} twice, $nodes nodes, " +
                "$linked links found, digest $o",
        )
        assertEquals(o, p, "addNodes: digest")
        assertTrue(counts.all { it > 0 }, "a mode was never reached: ${counts.toList()}")
        assertTrue(linked > 0, "no link was found in any addNodes result")
    }
}
