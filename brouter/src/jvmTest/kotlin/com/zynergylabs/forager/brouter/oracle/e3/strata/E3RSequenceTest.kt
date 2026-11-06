package com.zynergylabs.forager.brouter.oracle.e3.strata

import com.zynergylabs.forager.brouter.oracle.e3.RGenerator
import com.zynergylabs.forager.brouter.tiles.TestTiles
import java.io.File
import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * R's case sequence is pinned (step 1b handoff, eighteenth session, item 4: "`RGenerator` must not change R's sequence;
 * check that it does not, against a list saved before any edit"). The 26 profiles' lists, as [E3R] generates them, are
 * written one case per line (id, the four ends, profile, `memoryclass`, time limit; not the tile folder, which is the
 * machine's) and their SHA-256 is held to the value read from `RGenerator` before MP64's options were added to it.
 */
class E3RSequenceTest {
    @Test
    fun rsSequenceOnAll26ProfilesIsThePinnedOne() {
        E3Strata.jdk("R-sequence")
        val seeds = linkedMapOf(E3RTest.PROFILE to E3RTest.SEED) + E3RProfilesTest.SEEDS
        assertEquals(26, seeds.size)
        val lines = ArrayList<String>()
        for ((profile, seed) in seeds) {
            val gen = RGenerator(TestTiles.segments, seed)
            val g = try { gen.generate("R", profile, E3R.N, 20 * E3R.N, TestTiles.segments) } finally { gen.close() }
            for (c in g.cases) {
                lines += listOf(c.id, c.startLat, c.startLon, c.endLat, c.endLon, c.profile, c.memoryclass, c.maxRunningTimeMillis)
                    .joinToString("\t")
            }
        }
        val text = lines.joinToString("\n") + "\n"
        val out = File(E3Strata.work, "r-sequence.tsv").also { it.parentFile.mkdirs(); it.writeText(text) }
        val sha = MessageDigest.getInstance("SHA-256").digest(text.toByteArray()).joinToString("") { "%02x".format(it) }
        println("E3 R-sequence: ${lines.size} cases; SHA-256 $sha; written to $out")
        assertEquals(26 * E3R.N, lines.size, "R's 26 lists are not 1,534 cases")
        assertEquals(PINNED, sha, "R's sequence changed")
    }

    private companion object {
        /** Read from `RGenerator` at `fa54097`, before any edit to it (this session's first run of this test). */
        const val PINNED = "ed378c856bce3758917d8f1c67a1917e18b8251c7f2bd08e208599db35e43d6c"
    }
}
