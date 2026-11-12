package com.zynergylabs.forager.brouter.tiles

import com.zynergylabs.forager.brouter.util.OracleLibm
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Step 10, session 57 (RECORD -150, decision 1): a LOGIC run of the turn hints and the four formats. Every route group
 * [HintsFormatsRun] builds, the port on `OracleLibm` against the oracle, hints on (`turnInstructionMode` 2 to 9 in turn
 * by the case's place in its group): the six route fields, the hints text and each format's text are ASSERTED equal by
 * length and FNV-1a digest. Per group it prints cases, routes, and how many are equal per output; a difference prints
 * the group, the id, the output, both lengths and digests, and where the texts first differ (a hint's index and field,
 * or a character index and two character classes), never text (E1).
 */
class HintsFormatsLogicTest {
    @Test
    fun hintsAndFormatsOnOracleLibmAreTheOraclesOnEveryRouteGroup() {
        val problems = ArrayList<String>()
        val t0 = System.nanoTime()
        HintsFormatsRun.forEachGroup { g ->
            val t = HintsFormatsRun.compare(g, OracleLibm)
            println(t.line("step10 hints-formats logic"))
            problems += t.problems
        }
        println("step10 hints-formats logic: R ${HintsFormatsRun.R_FIRST?.let { "first $it routed per profile" } ?: "all routed"}; " +
            "%.1f s".format((System.nanoTime() - t0) / 1e9))
        problems.take(40).forEach { println("step10 hints-formats logic problem $it") }
        assertEquals(0, problems.size, "logic run: hints and formats against the oracle (${problems.size} differences; first: ${problems.firstOrNull()})")
    }
}
