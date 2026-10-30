package com.zynergylabs.forager.brouter.oracle.e3

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * E3's positive control (Phase 1 report, E3): R1 and R4, each in a fresh JVM, must give the twelve-node map different
 * orders. If they do not, the regimes do not move identity hashes, and E3 has not been run. Each regime runs twice, so
 * the log also says whether a regime repeats itself across fresh JVMs (recorded, not asserted).
 */
class E3ControlTest {
    @Test
    fun r1AndR4GiveTheControlDifferentOrders() {
        val runs = listOf("R1", "R1", "R4", "R4").map { E3Launcher.run(it) }
        for (r in runs) {
            println("${r.regime}: control ${r.control}")
            val idx = r.control.split(',').filter { it.isNotEmpty() }.map { it.toInt() }
            assertEquals((0 until 12).toList(), idx.sorted(), "${r.regime}: the order is not a permutation of the twelve")
        }
        println("R1 repeats across fresh JVMs: ${runs[0].control == runs[1].control}; R4: ${runs[2].control == runs[3].control}")
        assertNotEquals(runs[0].control, runs[2].control, "R1 and R4 gave the control one order: E3 has not been run")
    }
}
