package com.zynergylabs.forager.brouter.oracle.e3.strata

import com.zynergylabs.forager.brouter.oracle.e3.E3Stability
import com.zynergylabs.forager.brouter.oracle.e3.RGenerator
import com.zynergylabs.forager.brouter.tiles.TestTiles
import java.io.File
import kotlin.test.assertEquals

/**
 * Stratum R on one profile (Phase 1 report, E4 and E5): 59 cases from [RGenerator] with the profile's seed, on the four
 * captured tiles, through E3's six regimes. E5's figure is 59 per profile; a stratum not filled from 20 × 59 candidates
 * is reported short (E4) and fails here rather than being filled from elsewhere. Reach evidence: none needed; kinds
 * counted. The seed is the caller's, fixed in its source and pushed before any run of it (RECORD -47).
 */
object E3R {
    const val N = 59 // E5: 59 per profile

    fun run(profile: String, seed: Long) {
        val stratum = "R-$profile"
        E3Strata.jdk(stratum)
        println("E3 $stratum seed: $seed")
        val gen = RGenerator(TestTiles.segments, seed)
        val g = try {
            println("E3 R tiles' node counts: ${gen.nodeCounts}")
            gen.generate("R", profile, N, 20 * N, TestTiles.segments)
        } finally {
            gen.close()
        }
        println("E3 $stratum: ${g.cases.size} cases from ${g.candidates} candidates; rejected ${g.rejected}")
        assertEquals(N, g.cases.size, "$stratum is short: ${g.cases.size} of $N from ${g.candidates} candidates")
        val r = E3Stability.run(stratum, g.cases, File(E3Strata.work, stratum), E3Strata.agent, E3Strata.workers, 600)
        println("E3 $stratum kinds (R1): ${r.verdicts.groupingBy { it.outcomes.getValue("R1").kind }.eachCount()}")
        E3Strata.report(r)
    }
}
