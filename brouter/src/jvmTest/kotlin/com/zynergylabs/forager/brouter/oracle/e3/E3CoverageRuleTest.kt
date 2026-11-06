package com.zynergylabs.forager.brouter.oracle.e3

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * RECORD -50's rule in the coverage reader, without an R6 run: where R6's tuple differs from R1's, the reading is refused
 * and the file is never opened (here it does not exist); where they are equal, a missing file is an error, never "not
 * covered". The reader's reading of real execution data is [com.zynergylabs.forager.brouter.oracle.e3.strata.E3CoverageTest]'s.
 */
class E3CoverageRuleTest {
    private val r1 = E3Outcome("route", 10, 100, 1000, "a", "b")
    private val missing = File("/nonexistent/e3-coverage-rule.exec")

    @Test
    fun aReadingWhoseR6DiffersFromR1IsRefused() {
        val r6 = E3Outcome("route", 10, 100, 1001, "a", "b") // R-shortest-025's shape (RECORD -50): one metre longer
        val reading = E3Coverage.reach(r1, r6, missing, E3Coverage.NT)
        assertNull(reading.reached)
        assertTrue(reading.refused!!.contains("RECORD -50"), reading.describe())
        assertTrue(reading.describe().startsWith("coverage refused: "), reading.describe())
    }

    @Test
    fun aMissingFileIsAnErrorNotNotCovered() {
        val e = assertFailsWith<IllegalArgumentException> { E3Coverage.reach(r1, r1, missing, E3Coverage.NT) }
        assertEquals("no execution data at $missing", e.message)
    }
}
