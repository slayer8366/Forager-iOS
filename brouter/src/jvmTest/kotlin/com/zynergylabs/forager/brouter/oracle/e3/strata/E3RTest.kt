package com.zynergylabs.forager.brouter.oracle.e3.strata

import com.zynergylabs.forager.brouter.oracle.e3.E3Stability
import com.zynergylabs.forager.brouter.oracle.e3.RGenerator
import com.zynergylabs.forager.brouter.tiles.TestTiles
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Stratum R on one profile (Phase 1 report, E4 and E5): 59 cases from [RGenerator] with [SEED], on the four captured
 * tiles, through E3's six regimes. E5's figure is 59 per profile; a stratum not filled from 20 × 59 candidates is
 * reported short (E4) and fails here rather than being filled from elsewhere. Reach evidence: none needed; kinds counted.
 */
class E3RTest {
    @Test
    fun rOnHikingMountainIsCountedAgainstE3sStop() {
        val gen = RGenerator(TestTiles.segments, SEED)
        val g = try {
            println("E3 R tiles' node counts: ${gen.nodeCounts}")
            gen.generate("R", PROFILE, N, 20 * N, TestTiles.segments)
        } finally {
            gen.close()
        }
        println("E3 R-$PROFILE: ${g.cases.size} cases from ${g.candidates} candidates; rejected ${g.rejected}")
        assertEquals(N, g.cases.size, "R-$PROFILE is short: ${g.cases.size} of $N from ${g.candidates} candidates")
        val r = E3Stability.run("R-$PROFILE", g.cases, File(E3Strata.work, "R-$PROFILE"), E3Strata.agent, E3Strata.workers, 600)
        println("E3 R-$PROFILE kinds (R1): ${r.verdicts.groupingBy { it.outcomes.getValue("R1").kind }.eachCount()}")
        E3Strata.report(r)
    }

    private companion object {
        const val PROFILE = "hiking-mountain" // Forager's own profile: the first of E4's 26
        const val N = 59 // E5: 59 per profile
        const val SEED = 20261030L
    }
}
