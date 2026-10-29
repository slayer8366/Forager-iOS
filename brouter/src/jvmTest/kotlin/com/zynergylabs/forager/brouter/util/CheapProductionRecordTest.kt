package com.zynergylabs.forager.brouter.util

import com.zynergylabs.forager.brouter.msun.msunCos
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Step 2b's production run against the oracle (Amendment 3 Part B; E6, "recorded"): the port as it ships (MsunLibm)
 * differs from the oracle's table by design, at the 13 rows where msun's cos is not fdlibm's (RECORD -35). This test
 * records each of them, cos, kx and ky on both sides, and fails if the set of rows is not exactly those 13. Route-level
 * differences, by stratum, are E's and arrive with the steps that reach routes.
 */
class CheapProductionRecordTest {
    private val known = listOf(474, 488, 502, 663, 675, 1046, 1053, 1079, 1295, 1297, 1310, 1311, 1318)

    @Test
    fun productionTableDiffersFromTheOraclesOnlyAtTheThirteenRows() {
        val cosRows = (0 until 1800).filter { msunCos(oracleLat(it)).toRawBits() != Math.cos(oracleLat(it)).toRawBits() }
        val tableRows = (0 until 1800).filter { row ->
            val p = MsunLibm.scales(row)
            val o = OracleLibm.scales(row)
            p[0].toRawBits() != o[0].toRawBits() || p[1].toRawBits() != o[1].toRawBits()
        }
        println("# production vs oracle: row, cos (msun, oracle, ulps), kx (msun, oracle, ulps), ky (msun, oracle, ulps)")
        for (row in (cosRows + tableRows).distinct().sorted()) {
            val lat = oracleLat(row)
            val p = MsunLibm.scales(row)
            val o = OracleLibm.scales(row)
            println("record $row ${triple(msunCos(lat), Math.cos(lat))} ${triple(p[0], o[0])} ${triple(p[1], o[1])}")
        }
        println("record: cos differs at ${cosRows.size} rows, kx or ky at ${tableRows.size}: $tableRows")
        assertEquals(known, cosRows, "rows where msun's cos differs from the oracle's Math.cos")
        assertTrue(known.containsAll(tableRows), "table rows differing outside the 13: ${tableRows - known.toSet()}")
    }

    @Test
    fun productionAnglesEqualTheOraclesAtEveryPair() {
        var n = 0
        val bad = ArrayList<String>()
        com.zynergylabs.forager.brouter.msun.forEachAtan2Pair { dy, dx ->
            for (k in 0 until 4) {
                val lon2 = PAIR_SIGNS[2 * k] * dy
                val lat2 = PAIR_SIGNS[2 * k + 1] * dx
                val got = CheapAngleMeter.getDirection(MsunLibm, 0, 0, lon2, lat2)
                val want = btools.util.CheapAngleMeter.getDirection(0, 0, lon2, lat2)
                if (got.toRawBits() != want.toRawBits()) bad.add("$lon2 $lat2: ${bits(got)} vs ${bits(want)}")
                n++
            }
        }
        println("record: production getDirection differs from the oracle's at ${bad.size} of $n")
        assertEquals(4 * 299974, n)
        assertEquals(emptyList(), bad.take(20), "${bad.size} of $n differ")
    }

    private fun triple(port: Double, oracle: Double) = "${bits(port)} ${bits(oracle)} ${port.toRawBits() - oracle.toRawBits()}"
}
