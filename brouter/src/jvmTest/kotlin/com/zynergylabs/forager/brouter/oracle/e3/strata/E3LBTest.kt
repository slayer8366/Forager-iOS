package com.zynergylabs.forager.brouter.oracle.e3.strata

import com.zynergylabs.forager.brouter.oracle.e3.E3Select
import com.zynergylabs.forager.brouter.oracle.e3.LBGenerator
import com.zynergylabs.forager.brouter.tiles.TestTiles
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Stratum LB, latitude bands (Phase 1 report, E4; RECORD -77 item 4), on `hiking-mountain`, drawn by [LBGenerator]: start
 * and end in one of the (c) rows inside the tiles. Reach, read from R1's route (R2 equal to it, RECORD -52): "the route
 * lies in the band", every node's `ilat` in the row's band. The seed is fixed here and pushed before any run.
 */
class E3LBTest {
    @Test
    fun lbIsSelectedByReachAndCountedAgainstE3sStop() {
        val stratum = "LB"
        E3Strata.jdk(stratum)
        println("E3 $stratum seed: $SEED; profile ${E3Targeted.PROFILE}")
        val gen = LBGenerator(TestTiles.segments, SEED)
        val selection = try {
            println("E3 $stratum (c) rows: ${gen.cRows}; inside the tiles, with their nodes: ${gen.bandCounts}")
            assertEquals(13, gen.cRows.size, "step 2b's (c) rows are 13 (CheapProductionRecordTest)")
            val raw = gen.draws(stratum, E3Targeted.PROFILE)
            // Before routing: both ends, as the engine will read them, lie in one in-tile (c) row's band.
            val checked = object : Iterator<E3Select.Draw> {
                override fun hasNext() = raw.hasNext()
                override fun next(): E3Select.Draw = raw.next().also { d ->
                    val c = d.case ?: return@also
                    val rows = listOf(row(c.startLat), row(c.endLat))
                    assertTrue(rows[0] == rows[1] && rows[0] in gen.bandCounts, "${c.id}: ends in rows $rows")
                }
            }
            E3Targeted.select(stratum, checked, listOf(stratum)) { r ->
                val e = r.r1Extent ?: return@select E3Select.Mark(null, "no route: ${r.r1.kind}")
                val band = row(r.case.startLat)
                val lo = band * 100_000
                if (e[2] >= lo && e[3] < lo + 100_000) E3Select.Mark(stratum, "route in row $band's band")
                else E3Select.Mark(null, "route leaves the band")
            }
        } finally {
            gen.close()
        }
        E3Targeted.report(selection, mapOf(stratum to E3Targeted.stability(stratum, selection)))
    }

    /** The table row of a latitude as the engine reads it: `ilat` as `RoutingParamCollector.java:314` parses it. */
    private fun row(lat: Double) = ((lat + 90.0) * 1_000_000.0 + 0.5).toInt() / 100_000

    private companion object {
        const val SEED = 20261207L
    }
}
