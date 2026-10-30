package com.zynergylabs.forager.brouter.expressions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Item I, step 4, RECORD -40's ruling: `addLookupValue(name, value, int[])`'s asterisk block
 * (`BExpressionContext.java:576-693`) runs only with a non-null array, and every caller in BRouter passes null
 * (`:299`, `:300`, `:304`), so the port names it "unsupported". The test calls the method directly, with a non-null
 * array, on both engines built as `ProfileCache` builds them. The pinned `lookups.dat` has no `*` value (RECORD -39),
 * so one is added first through the same method with a null array, the path BRouter does reach.
 */
class AsteriskUnsupportedTest {
    private val profile = ExpressionsEngines.profiles.first { it.name == "hiking-mountain.brf" }

    @Test
    fun theOracleRecordsTheValueAndThePortThrowsTheNamedUnsupported() {
        val rc = ExpressionsEngines.foragerRoutingContext(profile)
        val oracle = ExpressionsEngines.oracle(profile, rc)
        val port = ExpressionsEngines.port(profile, rc)
        val name = "maxspeed"
        val num = oracle.way.getLookupNameIdx(name)
        assertTrue(num > 0, "the pinned lookups.dat has $name")
        oracle.way.addLookupValue(name, "*", null)
        port.way.addLookupValue(name, "*", null)

        val oracleData = oracle.way.createNewLookupData()
        oracle.way.addLookupValue(name, "3.5", oracleData)
        // :687, 1000 + (int) (Math.abs(Float.parseFloat("3.5")) * 100f): the block ran and recorded the value.
        assertEquals(1350, oracleData[num], "the oracle's record for $name=3.5")

        val e = assertFailsWith<UnsupportedOperationException> { port.way.addLookupValue(name, "3.5", IntArray(oracleData.size)) }
        assertTrue(e.message!!.startsWith("unsupported: addLookupValue's asterisk block"), "the port's message: ${e.message}")
    }

    @Test
    fun withoutAnAsteriskAnUnknownValueIsRecordedAsUnknownOnBoth() {
        // The same call with no `*` value never enters the block: both engines record 1 (unknown) and return null.
        val rc = ExpressionsEngines.foragerRoutingContext(profile)
        val oracle = ExpressionsEngines.oracle(profile, rc)
        val port = ExpressionsEngines.port(profile, rc)
        val num = oracle.way.getLookupNameIdx("highway")
        val oracleData = oracle.way.createNewLookupData()
        val portData = IntArray(oracleData.size)
        assertNull(oracle.way.addLookupValue("highway", "nosuchvalue", oracleData))
        assertNull(port.way.addLookupValue("highway", "nosuchvalue", portData))
        assertEquals(1, oracleData[num], "the oracle's record")
        assertEquals(oracleData.toList(), portData.toList(), "the port's record")
    }
}
