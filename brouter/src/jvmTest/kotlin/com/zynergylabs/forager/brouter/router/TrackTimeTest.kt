package com.zynergylabs.forager.brouter.router

import java.security.MessageDigest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Step 7: `OsmTrack.getTotalSeconds` and `Formatter`'s three statics against the oracle's. Time and energy values are
 * asserted (RECORD -110, the planner's call): on tracks built from step 6's paths and joined as `appendTrack` joins
 * them, the message `doRouting:238` writes (`getFormattedTime2(getTotalSeconds())`, `getFormattedEnergy(energy)`); on
 * synthetic tracks of zero-position elements (zero, one node, exact halves, negative spans, seeded floats); and on
 * seeded ints and doubles. `format1` is private in Java and is reached by name.
 */
class TrackTimeTest {
    private val oMsg = Class.forName("btools.router.MessageData")
    private val oFormat1 = btools.router.Formatter::class.java.getDeclaredMethod("format1", java.lang.Double.TYPE)
        .also { it.isAccessible = true }

    private fun oElement(time: Float): btools.router.OsmPathElement {
        val e = btools.router.OsmPathElement.create(0, 0, 0, null)
        btools.router.OsmPathElement::class.java.getField("message")
            .set(e, oMsg.getDeclaredConstructor().also { it.isAccessible = true }.newInstance())
        e.setTime(time)
        return e
    }

    private fun pElement(time: Float): OsmPathElement =
        OsmPathElement.create(0, 0, 0, null).also { it.message = MessageData(); it.setTime(time) }

    @Test
    fun totalSecondsOnBuiltTracksMatchTheOracle() {
        val cx = PathEngines.contexts(TrackEngines.foragerProfile())!!
        val md = MessageDigest.getInstance("SHA-256")
        var tracks = 0
        try {
            for ((ci, c) in TrackEngines.cases().withIndex()) {
                for ((wi, w) in TrackEngines.walks(cx, c, 7301L + ci, 4, 60).withIndex()) {
                    val (ot, pt) = TrackEngines.tracks(w)
                    val or = btools.router.OsmTrack()
                    val pr = OsmTrack()
                    or.appendTrack(ot)
                    pr.appendTrack(pt)
                    val o = listOf(or.totalSeconds.toString(), btools.router.Formatter.getFormattedTime2(or.totalSeconds),
                        or.energy.toString(), btools.router.Formatter.getFormattedEnergy(or.energy))
                    val p = listOf(pr.getTotalSeconds().toString(), Formatter.getFormattedTime2(pr.getTotalSeconds()),
                        pr.energy.toString(), Formatter.getFormattedEnergy(pr.energy))
                    assertEquals(o, p, "${c.id} walk $wi: seconds, time, energy, formatted energy")
                    md.update(o.joinToString(";").toByteArray())
                    tracks++
                }
            }
        } finally {
            cx.release()
        }
        println("7 track time, built: $tracks tracks, digest ${md.digest().joinToString("") { "%02x".format(it) }}")
        assertTrue(tracks > 0, "no track was built")
    }

    @Test
    fun totalSecondsOnSyntheticTracksMatchTheOracle() {
        val rnd = Random(20261111L)
        val spans = ArrayList<FloatArray>()
        spans += FloatArray(0)
        spans += floatArrayOf(5f)
        spans += floatArrayOf(3f, 3f)
        for (k in -4..4) spans += floatArrayOf(10f, 10f + k + 0.5f)
        spans += floatArrayOf(100f, 50f, 20f)
        repeat(500) {
            val n = rnd.nextInt(2, 6)
            spans += FloatArray(n) { if (rnd.nextInt(5) == 0) Float.fromBits(rnd.nextInt()) else rnd.nextFloat() * 20000f - 5000f }
        }
        for ((i, s) in spans.withIndex()) {
            val ot = btools.router.OsmTrack()
            val pt = OsmTrack()
            for (t in s) {
                ot.nodes.add(oElement(t))
                pt.nodes.add(pElement(t))
            }
            assertEquals(ot.totalSeconds, pt.getTotalSeconds(), "span $i: getTotalSeconds")
        }
        println("7 track time, synthetic: ${spans.size} spans")
    }

    @Test
    fun formatterMatchesTheOracle() {
        val rnd = Random(20261112L)
        val ints = listOf(0, 1, -1, 59, 60, 61, 3599, 3600, 3601, 86399, Int.MAX_VALUE, Int.MIN_VALUE, 3600000, 1799999) +
            List(3000) { if (it % 3 == 0) rnd.nextInt() else rnd.nextInt(-10_000, 400_000_000) }
        for ((i, v) in ints.withIndex()) {
            assertEquals(btools.router.Formatter.getFormattedTime2(v), Formatter.getFormattedTime2(v), "int $i: getFormattedTime2")
            assertEquals(btools.router.Formatter.getFormattedEnergy(v), Formatter.getFormattedEnergy(v), "int $i: getFormattedEnergy")
        }
        val doubles = listOf(0.0, 0.05, -0.05, 0.0499, 1.25, -1.25, 9.95, 1e18, -1e18, Double.NaN) +
            List(3000) { if (it % 4 == 0) Double.fromBits(rnd.nextLong()) else rnd.nextDouble(-1000.0, 1000.0) }
        for ((i, v) in doubles.withIndex()) {
            assertEquals(oFormat1.invoke(null, v), Formatter.format1(v), "double $i: format1")
        }
        println("7 formatter: ${ints.size} ints, ${doubles.size} doubles")
    }
}
