package com.zynergylabs.forager.brouter.oracle.e3.strata

import com.zynergylabs.forager.brouter.oracle.e3.E3Select
import com.zynergylabs.forager.brouter.oracle.e3.RGenerator
import com.zynergylabs.forager.brouter.tiles.TestTiles
import kotlin.test.Test

/**
 * Strata MP64, ML5 and PM5 (Phase 1 report, E4; RECORD -51, -52), on `hiking-mountain`. The generator is R's, in
 * `W75_N40` only, at 20 to 100 km (`20 km × 5^u`, log-uniform as R's law is).
 * - **MP64**, `memoryclass` 64: reach is at least one "collected" line in R2's log (`RoutingEngine.java:2025`).
 * - **ML5 and PM5** are one sequence at `memoryclass` 5. ML5: R1's kind is "memory limit reached" (`:2028`), whatever came
 *   before it. PM5: R2's log shows panic mode enabled (`:2031`), and the kind is not the limit. ML5 cases that also showed
 *   panic are counted (RECORD -52).
 * The seeds are fixed here and pushed before any run of them (RECORD -47).
 */
class E3MemoryTest {
    @Test
    fun mp64IsSelectedByItsCollectionsAndCountedAgainstE3sStop() {
        val stratum = "MP64"
        E3Strata.jdk(stratum)
        println("E3 $stratum seed: $SEED_MP64; profile ${E3Targeted.PROFILE}; memoryclass 64")
        val selection = withGenerator(SEED_MP64) { gen ->
            E3Targeted.select(stratum, gen.draws(stratum, E3Targeted.PROFILE, 64, TestTiles.segments), listOf(stratum)) { r ->
                if (r.collections > 0) E3Select.Mark(stratum, "${r.collections} collected lines") else E3Select.Mark(null, "no collected line")
            }
        }
        val taken = selection.taken.getValue(stratum)
        // E4: memoryclass 64 cases that reach the limit or panic mode are counted and reported, never padded.
        println("E3 $stratum taken with the limit: ${taken.count { it.r1.kind == LIMIT }}; with panic mode: ${taken.count { it.panic }}")
        E3Targeted.report(selection, mapOf(stratum to E3Targeted.stability(stratum, selection)))
    }

    @Test
    fun ml5AndPm5AreSelectedByTheirOutcomeAndCountedAgainstE3sStop() {
        val label = "M5"
        E3Strata.jdk(label)
        println("E3 ML5/PM5 seed: $SEED_M5; profile ${E3Targeted.PROFILE}; memoryclass 5")
        val selection = withGenerator(SEED_M5) { gen ->
            E3Targeted.select(label, gen.draws(label, E3Targeted.PROFILE, 5, TestTiles.segments), listOf("ML5", "PM5")) { r ->
                when {
                    r.r1.kind == LIMIT -> E3Select.Mark("ML5", if (r.panic) "the limit, after panic mode" else "the limit")
                    r.panic -> E3Select.Mark("PM5", "panic mode; kind ${r.r1.kind}")
                    else -> E3Select.Mark(null, "neither; kind ${r.r1.kind}; ${r.collections} collected lines")
                }
            }
        }
        println("E3 ML5 taken that also showed panic mode: ${selection.taken.getValue("ML5").count { it.panic }}")
        E3Targeted.report(selection, listOf("ML5", "PM5").associateWith { E3Targeted.stability(it, selection) })
    }

    private fun <T> withGenerator(seed: Long, body: (RGenerator) -> T): T {
        val gen = RGenerator(TestTiles.segments, seed, only = TILE, minMetres = 20_000.0, ratio = 5.0)
        try {
            println("E3 the tile's node count: ${gen.nodeCounts}")
            return body(gen)
        } finally {
            gen.close()
        }
    }

    private companion object {
        const val TILE = "W75_N40.rd5"
        const val LIMIT = "memory limit reached"
        const val SEED_MP64 = 20261203L
        const val SEED_M5 = 20261204L
    }
}
