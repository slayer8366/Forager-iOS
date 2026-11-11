package com.zynergylabs.forager.brouter.router

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Step 7: the track's head on Forager's path against the oracle's: `processVoiceHints` up to its early return
 * (`OsmTrack.java:452-460`, no detours and no direct routing), with `VoiceHintList`'s constructor and
 * `setTransportMode`; `prepareSpeedProfile`; `readBinary` with no file; `copyDetours` from a track with no detours.
 * Run under each of step 6's `StdPath` profiles with Forager's parameters, and under each of the four car and bike
 * flag pairs, on a track built from step 6's paths. No hint is made or asserted (step 7's report, §3).
 */
class TrackHeadTest {
    private fun hints(v: Any?): String = if (v == null) "null" else
        "size=${(TrackEngines.field(v, "list") as List<*>).size} transportMode=${TrackEngines.field(v, "transportMode")} " +
            "turnInstructionMode=${TrackEngines.field(v, "turnInstructionMode")}"

    @Test
    fun trackHeadMatchesTheOracle() {
        val fcx = PathEngines.contexts(TrackEngines.foragerProfile())!!
        val walk = try {
            TrackEngines.walks(fcx, TrackEngines.cases()[2], 7201L, 1, 30).single()
        } finally {
            fcx.release()
        }
        val profiles = PathEngines.stdProfiles()
        val modes = HashSet<String>()
        var runs = 0
        for (profile in profiles) {
            val cx = PathEngines.contexts(profile)!!
            try {
                assertEquals(cx.o.hasDirectRouting, cx.p.hasDirectRouting, "${profile.name}: hasDirectRouting")
                assertTrue(!cx.p.hasDirectRouting, "${profile.name}: direct routing would go past the head")
                val flags = listOf(null) + listOf(false to false, false to true, true to false, true to true)
                for (f in flags) {
                    val id = "${profile.name} flags $f"
                    if (f != null) {
                        cx.o.carMode = f.first; cx.o.bikeMode = f.second
                        cx.p.carMode = f.first; cx.p.bikeMode = f.second
                    }
                    val (ot, pt) = TrackEngines.tracks(walk)
                    ot.processVoiceHints(cx.o)
                    pt.processVoiceHints(cx.p)
                    val oh = hints(ot.voiceHints)
                    assertEquals(oh, hints(pt.voiceHints), "$id: voiceHints")
                    assertTrue(oh.startsWith("size=0 "), "$id: the head made a hint")
                    modes += oh
                    ot.prepareSpeedProfile(cx.o)
                    pt.prepareSpeedProfile(cx.p)
                    assertEquals(ot.nodes.size, pt.nodes.size, "$id: node count after prepareSpeedProfile")
                    val os = btools.router.OsmTrack()
                    val ps = OsmTrack()
                    ot.copyDetours(os)
                    pt.copyDetours(ps)
                    assertEquals(TrackEngines.field(ot, "detourMap") == null, TrackEngines.field(pt, "detourMap") == null, "$id: detourMap")
                    assertNull(TrackEngines.field(pt, "detourMap"), "$id: copyDetours made a detour map from none")
                    runs++
                }
            } finally {
                cx.release()
            }
        }
        assertNull(btools.router.OsmTrack.readBinary(null, null, null, 0L, null), "the oracle's readBinary(null)")
        assertNull(OsmTrack.readBinary(null, null, null, 0L, null), "readBinary(null)")
        println("7 track head: ${profiles.size} profiles, $runs runs, ${modes.size} head states: ${modes.sorted()}")
        assertTrue(modes.size >= 3, "the sample did not reach the three transport modes: $modes")
    }
}
