package com.zynergylabs.forager.brouter.router

import java.security.MessageDigest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Step 7: `OsmTrack.appendTrack` against the oracle's, on tracks built from step 6's paths ([TrackEngines]). Three
 * modes: one section into an empty track (Forager's case, `ourSize == 0`), two sections, and two sections with the
 * first's last height `Short.MIN_VALUE`. Compared per node by time and energy bits, cost, height, whether the message
 * still matches the node's position, and the index its `origin` has in the joined track; then the track's sums and
 * whether `voiceHints` and `detourMap` are null. A failure names the mode, case, walk and index, never a position (E1).
 */
class AppendTrackTest {
    private fun state(nodes: List<Any>, i: Int, origin: Any?, time: Float, energy: Float, cost: Int, selev: Short,
                      msg: Pair<Int, Int>?, ilon: Int, ilat: Int): String {
        val m = if (msg == null) "null" else (msg.first == ilon && msg.second == ilat).toString()
        return "i=$i time=${time.toRawBits()} energy=${energy.toRawBits()} cost=$cost selev=$selev msgAtNode=$m " +
            "origin=${TrackEngines.indexOf(nodes, origin)}"
    }

    private fun oState(t: btools.router.OsmTrack): List<String> = t.nodes.mapIndexed { i, e ->
        val m = TrackEngines.field(e, "message")
        state(t.nodes, i, e.origin, e.time, e.energy, e.cost, e.sElev,
            m?.let { TrackEngines.field(it, "lon") as Int to TrackEngines.field(it, "lat") as Int }, e.iLon, e.iLat)
    } + sums(t.distance, t.ascend, t.plainAscend, t.cost, t.energy, t.showspeed, t.showSpeedProfile, t.voiceHints,
        TrackEngines.field(t, "detourMap"))

    private fun pState(t: OsmTrack): List<String> = t.nodes.mapIndexed { i, e ->
        val m = e.message
        state(t.nodes, i, e.origin, e.getTime(), e.getEnergy(), e.cost, e.getSElev(), m?.let { it.lon to it.lat },
            e.getILon(), e.getILat())
    } + sums(t.distance, t.ascend, t.plainAscend, t.cost, t.energy, t.showspeed, t.showSpeedProfile, t.voiceHints,
        TrackEngines.field(t, "detourMap"))

    private fun sums(distance: Int, ascend: Int, plainAscend: Int, cost: Int, energy: Int, showspeed: Boolean,
                     showSpeedProfile: Boolean, voiceHints: Any?, detourMap: Any?): String =
        "distance=$distance ascend=$ascend plainAscend=$plainAscend cost=$cost energy=$energy showspeed=$showspeed " +
            "showSpeedProfile=$showSpeedProfile voiceHints=${voiceHints != null} detourMap=${detourMap != null}"

    /** The same seeded sums and flags on both engines' tracks. */
    private fun seed(rnd: Random, o: btools.router.OsmTrack, p: OsmTrack) {
        val v = IntArray(4) { rnd.nextInt(-100_000, 100_000) }
        val f = BooleanArray(2) { rnd.nextBoolean() }
        o.distance = v[0]; o.ascend = v[1]; o.plainAscend = v[2]; o.cost = v[3]
        p.distance = v[0]; p.ascend = v[1]; p.plainAscend = v[2]; p.cost = v[3]
        o.showspeed = f[0]; o.showSpeedProfile = f[1]
        p.showspeed = f[0]; p.showSpeedProfile = f[1]
    }

    @Test
    fun appendTrackMatchesTheOracle() {
        val cx = PathEngines.contexts(TrackEngines.foragerProfile())!!
        val md = MessageDigest.getInstance("SHA-256")
        val counts = IntArray(3)
        var nodes = 0L
        try {
            for ((ci, c) in TrackEngines.cases().withIndex()) {
                for (mode in 0 until 3) {
                    // Fresh walks for each mode: appendTrack changes the elements it takes.
                    val walks = TrackEngines.walks(cx, c, 7101L + ci, 4, 40)
                    val rnd = Random(9101L + ci * 7 + mode)
                    var wi = 0
                    while (wi + (if (mode == 0) 0 else 1) < walks.size) {
                        val id = "mode $mode ${c.id} walk $wi"
                        val (ot, pt) = TrackEngines.tracks(walks[wi])
                        seed(rnd, ot, pt)
                        val (or, pr) = if (mode == 0) {
                            btools.router.OsmTrack() to OsmTrack()
                        } else {
                            val (oa, pa) = TrackEngines.tracks(walks[wi + 1])
                            seed(rnd, oa, pa)
                            if (mode == 2) {
                                oa.nodes.last().setSElev(Short.MIN_VALUE)
                                pa.nodes.last().setSElev(Short.MIN_VALUE)
                            }
                            oa to pa
                        }
                        or.appendTrack(ot)
                        pr.appendTrack(pt)
                        val os = oState(or)
                        val ps = pState(pr)
                        assertEquals(os.size, ps.size, "$id: node count")
                        for (k in os.indices) assertEquals(os[k], ps[k], "$id: line $k")
                        os.forEach { md.update(it.toByteArray()) }
                        counts[mode]++
                        nodes += or.nodes.size
                        wi += if (mode == 0) 1 else 2
                    }
                }
            }
        } finally {
            cx.release()
        }
        println(
            "7 append track: ${counts[0]} one-section, ${counts[1]} two-section, ${counts[2]} with a missing height, " +
                "$nodes nodes, digest ${md.digest().joinToString("") { "%02x".format(it) }}",
        )
        assertTrue(counts.all { it > 0 }, "a mode was never reached: ${counts.toList()}")
    }
}
