package com.zynergylabs.forager.brouter.tiles

import kotlin.test.Test

/**
 * Step 10, session 48 (RECORD -139, decision 2): stratum F0h, Forager's eight routes on its heighted square, as
 * [NtEngineCommonTest] holds NT, through [Step10Cases]: its cases from `f0h-cases.tsv` beside the NT file, written at run
 * time by `NtCasesWriteTest` from `RoutingEngineF0hTest`'s cases (each line names the heighted square's folder, the build's
 * fetchF0h output), held to [F0hExpected] on the six fields and the stored tuple digest; profiles `<oracle dir>/profiles2`.
 * E1 as in [Step10Cases].
 */
class F0hEngineCommonTest {
    @Test
    fun f0hCasesFromTheRunTimeFileThroughTheEngineMatchExpected1b() {
        val nt = Step10Cases.ntFile("F0h") ?: return
        Step10Cases.check("F0h", nt.parent!! / "f0h-cases.tsv", F0hExpected.ROWS)
    }
}
