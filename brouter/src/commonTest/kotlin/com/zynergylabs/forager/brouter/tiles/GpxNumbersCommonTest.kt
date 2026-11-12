package com.zynergylabs.forager.brouter.tiles

import okio.FileSystem
import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Step 10, session 59 (RECORD -156, decision 1): a diagnosis, no check. For the 5 NT routes whose GPX differs on the
 * simulator (all in mode 7), each floating number `FormatGpx`'s mode-7 branch appends with the platform's own text is
 * printed with its raw bits: `gpxnum <id> <hint index> <d|t|s> <bits, 16 hex digits> <"" + x>` (d the hint's
 * `distanceToNext`, t its `ti`, s its `speed`, computed as that branch computes them). Run as [FormatDigestsCommonTest]
 * runs a case (after the five texts, so the track is in the same state), gated the same way: on the JVM only with
 * [Step10Cases.VARIABLE] set. E1: distances, times and speeds only; no position and no other text is printed.
 * It asserts only that each route printed at least one line.
 */
class GpxNumbersCommonTest {
    @Test
    fun printTheFloatingNumbersOfGpxModeSeven() {
        val nt = Step10Cases.ntFile("gpx-numbers") ?: return
        val version = com.zynergylabs.forager.brouter.router.OsmTrack.version
        com.zynergylabs.forager.brouter.router.OsmTrack.version = FormatDigests.ORACLE_VERSION
        try {
            val oracle = testSetting("brouter.oracle.dir", "BROUTER_ORACLE_DIR")!!.toPath() / "profiles2"
            val segments = testSetting("brouter.tiles.dir", "BROUTER_TILES_DIR")!!.toPath() / "segments"
            val stored = FormatDigests.NT.associateBy { it.id }
            val lines = FileSystem.SYSTEM.read(nt) { readUtf8() }.lines().filter { it.isNotEmpty() }
            for (id in IDS) {
                val f = lines.map { it.split('\t') }.singleOrNull { it[0] == id } ?: fail("$id is not in NT's file once")
                val mode = stored[id]?.mode ?: fail("$id has no stored row")
                if (mode != 7) fail("$id's stored mode is $mode, not 7")
                val tiles = if (f.size == 8) f[7].toPath() else segments
                val r = EngineRun.runTrack("${f[3]},${f[4]}|${f[5]},${f[6]}", oracle / "${f[1]}.brf", tiles, f[2].toInt(), 0L, mode)
                val t = r.track ?: fail("$id: no track")
                FormatOutputs.texts(t, r.rc!!)
                var printed = 0
                val vh = t.voiceHints!!
                var lastRteTime = t.getVoiceHintTime(0)
                for (i in 0 until vh.list.size) {
                    val hint = vh.list[i]
                    println(line(id, i, "d", hint.distanceToNext)); printed++
                    val rteTime = t.getVoiceHintTime(i + 1)
                    if (rteTime != lastRteTime) {
                        val ti: Double = (rteTime - lastRteTime).toDouble()
                        val speed = hint.distanceToNext / ti
                        println(line(id, i, "t", ti)); printed++
                        println(line(id, i, "s", speed)); printed++
                        lastRteTime = rteTime
                    }
                }
                println("gpxcount $id hints ${vh.list.size} lines $printed")
                assertTrue(printed > 0, "$id printed no line")
            }
        } finally {
            com.zynergylabs.forager.brouter.router.OsmTrack.version = version
        }
    }

    private fun line(id: String, i: Int, kind: String, x: Double): String =
        "gpxnum $id $i $kind ${x.toRawBits().toULong().toString(16).padStart(16, '0')} " + ("" + x)

    private companion object {
        val IDS = listOf("NT-0009", "NT-0031", "NT-0045", "NT-0053", "NT-0061")
    }
}
