package com.zynergylabs.forager.brouter.router

import java.security.MessageDigest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Step 7: `OsmTrack.addNode`, `buildMap`, `getLink` and `containsNode` against the oracle's, on tracks built from step
 * 6's paths ([TrackEngines]). Compared by node index (the element `getLink` returns, found by identity), count and
 * boolean; a failure names the case, walk and index, never a position (E1).
 */
class TrackMapTest {
    @Test
    fun trackMapMatchesTheOracle() {
        val cx = PathEngines.contexts(TrackEngines.foragerProfile())!!
        val md = MessageDigest.getInstance("SHA-256")
        var tracks = 0
        var nodes = 0L
        var found = 0L
        var chained = 0L
        try {
            for ((ci, c) in TrackEngines.cases().withIndex()) {
                for ((wi, w) in TrackEngines.walks(cx, c, 7001L + ci, 6, 60).withIndex()) {
                    val (ot, pt) = TrackEngines.tracks(w)
                    val id = "${c.id} walk $wi"
                    assertEquals(ot.nodes.size, pt.nodes.size, "$id: node count")
                    ot.buildMap()
                    pt.buildMap()
                    val n = ot.nodes.size
                    val oIds = ot.nodes.map { it.idFromPos }
                    val pIds = pt.nodes.map { it.getIdFromPos() }
                    chained += oIds.size - oIds.toSet().size
                    val rnd = Random(9001L + ci * 31 + wi)
                    val pairs = ArrayList<Pair<Int, Int>>()
                    for (i in 1 until n) {
                        pairs += (i - 1) to i
                        pairs += i to (i - 1)
                    }
                    repeat(2 * n) { pairs += rnd.nextInt(n) to rnd.nextInt(n) }
                    for ((k, ab) in pairs.withIndex()) {
                        val (a, b) = ab
                        val oi = TrackEngines.indexOf(ot.nodes, ot.getLink(oIds[a], oIds[b]))
                        val pi = TrackEngines.indexOf(pt.nodes, pt.getLink(pIds[a], pIds[b]))
                        assertEquals(oi, pi, "$id: getLink, pair $k (indices $a, $b)")
                        if (oi >= 0) found++
                        md.update("$oi;".toByteArray())
                    }
                    for (i in 0 until n) {
                        assertEquals(ot.containsNode(ot.nodes[i]), pt.containsNode(pt.nodes[i]), "$id: containsNode, index $i")
                        // A point one unit east of each node, mostly off the track.
                        val oo = btools.router.OsmPathElement.create(ot.nodes[i].iLon + 1, ot.nodes[i].iLat, 0, null)
                        val po = OsmPathElement.create(pt.nodes[i].getILon() + 1, pt.nodes[i].getILat(), 0, null)
                        val oc = ot.containsNode(oo)
                        assertEquals(oc, pt.containsNode(po), "$id: containsNode off the track, index $i")
                        md.update((if (oc) "1" else "0").toByteArray())
                    }
                    tracks++
                    nodes += n
                }
            }
        } finally {
            cx.release()
        }
        println(
            "7 track map: $tracks tracks, $nodes nodes, $chained repeated nodes, $found links found, " +
                "digest ${md.digest().joinToString("") { "%02x".format(it) }}",
        )
        assertTrue(tracks > 0 && found > 0, "the sample built no track or found no link")
        assertTrue(chained > 0, "no node repeated, so no nextHolder chain was built")
    }
}
