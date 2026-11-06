package com.zynergylabs.forager.brouter.oracle.e3.strata

import com.zynergylabs.forager.brouter.oracle.e3.E3Coverage
import com.zynergylabs.forager.brouter.oracle.e3.E3Profiles
import com.zynergylabs.forager.brouter.oracle.e3.E3Select
import com.zynergylabs.forager.brouter.oracle.e3.E3Stability
import com.zynergylabs.forager.brouter.oracle.e3.RGenerator
import com.zynergylabs.forager.brouter.tiles.TestTiles
import kotlin.test.Test

/**
 * Stratum CR, crossing (Phase 1 report, E4; RECORD -77 item 3): R's generator ([RGenerator], all four tiles, 100 m to
 * 100 km, `memoryclass` 64) with E's crossing profile ([E3Profiles.crossing]), written into every JVM's `profiles2/`.
 * Reach: R6's coverage of `StdPath.java:101-103` (the node's description is printed) and `:105-113` (a crossing class is
 * read), every line of both covered ([E3Coverage.CR]), counted only where R6's tuple equals R1's (RECORD -50). A refused
 * reading is a stop for the planner (-77): printed as `E3 CR STOP`, the case not taken. The seed is fixed here and
 * pushed before any run.
 */
class E3CRTest {
    @Test
    fun crIsSelectedByCoverageAndCountedAgainstE3sStop() {
        val stratum = "CR"
        E3Strata.jdk(stratum)
        val profile = E3Profiles.crossing()
        println("E3 $stratum seed: $SEED; profile ${E3Profiles.CROSSING}, SHA-256 ${E3Profiles.sha256(profile)}")
        E3Stability.ownProfiles = mapOf(E3Profiles.CROSSING to profile)
        var stops = 0
        try {
            val gen = RGenerator(TestTiles.segments, SEED)
            val selection = try {
                println("E3 $stratum nodes: ${gen.nodeCounts}")
                E3Targeted.select(stratum, gen.draws(stratum, E3Profiles.CROSSING, 64, TestTiles.segments), listOf(stratum), coverage = true) { r ->
                    val cov = E3Coverage.reach(r.r1, r.r6!!, r.exec!!, E3Coverage.CR)
                    when (cov.reached) {
                        null -> {
                            stops++
                            println("E3 $stratum STOP ${r.case.id}: ${cov.describe()}")
                            E3Select.Mark(null, "coverage refused (a stop): ${cov.describe()}")
                        }
                        true -> E3Select.Mark(stratum, cov.describe())
                        false -> E3Select.Mark(null, cov.describe())
                    }
                }
            } finally {
                gen.close()
            }
            println("E3 $stratum coverage stops (R6 differs from R1): $stops")
            E3Targeted.report(selection, mapOf(stratum to E3Targeted.stability(stratum, selection)))
        } finally {
            E3Stability.ownProfiles = emptyMap()
        }
    }

    private companion object {
        const val SEED = 20261206L
    }
}
