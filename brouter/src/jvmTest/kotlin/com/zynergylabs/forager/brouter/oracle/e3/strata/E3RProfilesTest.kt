package com.zynergylabs.forager.brouter.oracle.e3.strata

import com.zynergylabs.forager.brouter.oracle.OracleCalls
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Stratum R on E4's other 25 profiles ([E3R]), one after another, each with its own seed. The first profile over E3's 5%
 * ends the run there: that is a stop for the owner (RECORD -47), so no later profile runs after it.
 */
class E3RProfilesTest {
    @Test
    fun rOnTheOther25ProfilesIsCountedAgainstE3sStop() {
        val brf = File(OracleCalls.oracleDir, "profiles2").list()!!.filter { it.endsWith(".brf") }.map { it.removeSuffix(".brf") }
        assertEquals(brf.sorted(), (SEEDS.keys + "hiking-mountain").sorted(), "R's profiles are not the oracle's 26 (E4)")
        for ((i, e) in SEEDS.entries.withIndex()) {
            println("E3 R profile ${i + 1} of ${SEEDS.size}: ${e.key}")
            E3R.run(e.key, e.value)
        }
    }

    private companion object {
        /** Fixed before any run (RECORD -47): 20261101 upward, in the profiles' alphabetical order. */
        val SEEDS = linkedMapOf(
            "car-eco" to 20261101L,
            "car-eco-nocost" to 20261102L,
            "car-fast" to 20261103L,
            "car-fast-nocost" to 20261104L,
            "car-vario" to 20261105L,
            "car-vario-nocost" to 20261106L,
            "fastbike" to 20261107L,
            "fastbike-asia-pacific" to 20261108L,
            "fastbike-lowtraffic" to 20261109L,
            "fastbike-verylowtraffic" to 20261110L,
            "gravel" to 20261111L,
            "moped" to 20261112L,
            "mtb" to 20261113L,
            "rail" to 20261114L,
            "river" to 20261115L,
            "safety" to 20261116L,
            "shortest" to 20261117L,
            "skating" to 20261118L,
            "trekking" to 20261119L,
            "trekking-ignore-cr" to 20261120L,
            "trekking-noferries" to 20261121L,
            "trekking-nosteps" to 20261122L,
            "trekking-steep" to 20261123L,
            "vm-forum-liegerad-schnell" to 20261124L,
            "vm-forum-velomobil-schnell" to 20261125L,
        )
    }
}
