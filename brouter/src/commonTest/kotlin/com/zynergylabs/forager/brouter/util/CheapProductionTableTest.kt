package com.zynergylabs.forager.brouter.util

import com.zynergylabs.forager.brouter.msun.hex
import com.zynergylabs.forager.brouter.msun.msunCosRows
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Step 2b's production run, on the JVM and on the simulator: the production table (MsunLibm) is msun's at all 1,800 rows.
 * Each row is the table built over bionic's C results (MsunReference.kt, which jvmTest checks against a fresh C build),
 * and the latitude the builder asks for is tab.c's latitude, bit for bit. The arithmetic between cos and kx/ky is proven
 * against the oracle by jvmTest's logic run, and across platforms by PlatformInvariantsTest's digests.
 */
class CheapProductionTableTest {
    @Test
    fun productionTableIsMsunsAtEveryRow() {
        val bad = ArrayList<String>()
        var rows = 0
        for (ref in msunCosRows) { // 1,800 committed rows: a fixed list, so it ends
            val ilat = ref.row * CheapRuler.SCALE_CACHE_INCREMENT + CheapRuler.SCALE_CACHE_INCREMENT / 2
            val expected = CheapRuler.calcKxKyFromILat(ilat) { x ->
                if (x.toRawBits() != ref.lat) bad.add("row ${ref.row}: the builder's lat ${hex(x)} is not tab.c's")
                Double.fromBits(ref.cos)
            }
            val got = MsunLibm.scales(ref.row)
            if (got[0].toRawBits() != expected[0].toRawBits() || got[1].toRawBits() != expected[1].toRawBits()) {
                bad.add("row ${ref.row}: kx ${hex(got[0])} ky ${hex(got[1])}, msun's ${hex(expected[0])} ${hex(expected[1])}")
            }
            rows++
        }
        assertEquals(1800, rows, "rows compared")
        assertEquals(emptyList(), bad)
    }

    @Test
    fun scalesAreLookedUpByIlatOver100000AsJavaDoes() {
        // CheapRuler.java:56-58, SCALE_CACHE[ilat / 100000]; Java's int division truncates toward zero.
        val cases = listOf(0 to 0, 99999 to 0, 100000 to 1, 50050000 to 500, 179999999 to 1799, -99999 to 0)
        for ((ilat, row) in cases) {
            assertEquals(MsunLibm.scales(row).toList(), CheapRuler.getLonLatToMeterScales(MsunLibm, ilat).toList(), "ilat $ilat")
        }
    }
}
