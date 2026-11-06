package com.zynergylabs.forager.brouter.oracle.e3.strata

import com.zynergylabs.forager.brouter.oracle.e3.E3Coverage
import com.zynergylabs.forager.brouter.oracle.e3.E3Select
import com.zynergylabs.forager.brouter.oracle.e3.NTGenerator
import com.zynergylabs.forager.brouter.tiles.TestTiles
import kotlin.test.Test

/**
 * Stratum NT, near-ties (Phase 1 report, E4; RECORD -77 item 2), on `hiking-mountain`, drawn by [NTGenerator].
 *
 * **Reach, as RECORD -78 rules it:** the oracle's own check of the gap between the start's two nearest links'
 * `directionDiff`s, at most [NTGenerator.MAX_ULPS] ulps, made in this JVM by the oracle's matching ([NTGenerator.probe]).
 * The generator makes it before routing; the mark makes it again on the routed case, so each taken case's gap is in
 * `select.tsv`. Coverage of `WaypointMatcherImpl.java:71` ([E3Coverage.NT]) is read from R6 and recorded, never required:
 * `:71` runs on almost every case (-78), so it is no evidence of a near-tie. Any outcome kind counts: the tie is broken in
 * the matching, before routing.
 *
 * A coverage reading refused because R6's tuple differs from R1's (RECORD -50) is a stop for the planner (-77's abort
 * conditions): it is printed as `E3 NT STOP` and the case is not taken. The seed is fixed here and pushed before any run.
 */
class E3NTTest {
    @Test
    fun ntIsSelectedByTheOraclesGapCheckAndCountedAgainstE3sStop() {
        val stratum = "NT"
        E3Strata.jdk(stratum)
        println("E3 $stratum seed: $SEED; profile ${E3Targeted.PROFILE}")
        val gen = NTGenerator(TestTiles.segments, SEED)
        var stops = 0
        val selection = try {
            println("E3 $stratum nodes: ${gen.nodeCounts}")
            E3Targeted.select(stratum, gen.draws(stratum, E3Targeted.PROFILE), listOf(stratum), coverage = true) { r ->
                val c = r.case
                val cov = E3Coverage.reach(r.r1, r.r6!!, r.exec!!, E3Coverage.NT)
                if (cov.refused != null) {
                    stops++
                    println("E3 $stratum STOP ${c.id}: ${cov.describe()}")
                    return@select E3Select.Mark(null, "coverage refused (a stop): ${cov.describe()}")
                }
                val p = gen.probe(doubleArrayOf(c.startLat, c.startLon, c.endLat, c.endLon))
                val first = p.entries.firstOrNull()
                val second = first?.let { f -> p.entries.firstOrNull { it.link != f.link } }
                val gap = if (first != null && second != null && first.radius == second.radius) {
                    NTGenerator.ulps(first.directionDiff, second.directionDiff)
                } else {
                    null
                }
                val read = "gap ${gap ?: "-"} ulps; :71 ${cov.describe()}"
                if (gap != null && gap <= NTGenerator.MAX_ULPS) E3Select.Mark(stratum, read) else E3Select.Mark(null, "no near-tie: $read")
            }
        } finally {
            gen.close()
        }
        println("E3 $stratum coverage stops (R6 differs from R1): $stops")
        E3Targeted.report(selection, mapOf(stratum to E3Targeted.stability(stratum, selection)))
    }

    private companion object {
        const val SEED = 20261205L
    }
}
