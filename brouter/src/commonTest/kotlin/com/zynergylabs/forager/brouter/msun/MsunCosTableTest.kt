package com.zynergylabs.forager.brouter.msun

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Step 2b (dispatch 2026-10-28-01, Amendment 3, Part B): the port's `cos` against bionic's C, bit for bit, at the
 * table's 1,800 latitudes. Runs on the JVM and on the iOS simulator (commonTest). The expected bits are tab.c's output
 * for msun built with Android's flags, committed as MsunReference.kt and checked against a fresh C build by jvmTest.
 */
class MsunCosTableTest {
    @Test
    fun latitudesAreTheTables() {
        assertEquals(1800, msunCosRows.size, "committed rows")
        val wrong = msunCosRows.filter { cheapRulerLat(it.row).toRawBits() != it.lat }.map { it.row }
        assertEquals(emptyList(), wrong, "rows whose CheapRuler latitude differs from tab.c's input")
    }

    @Test
    fun cosEqualsBionicAtEveryTableRow() {
        val wrong = msunCosRows.mapNotNull { r ->
            val got = msunCos(Double.fromBits(r.lat))
            if (got.toRawBits() == r.cos) null else "row ${r.row}: lat ${hex(Double.fromBits(r.lat))} " +
                "port ${hex(got)} C ${hex(Double.fromBits(r.cos))}"
        }
        assertEquals(emptyList(), wrong, "${wrong.size} of ${msunCosRows.size} rows differ from bionic's C")
    }
}
