package com.zynergylabs.forager.brouter.tiles

import kotlin.test.Test

/**
 * Step 10, session 47 (RECORD -138, decision 2): stratum LB as [NtEngineCommonTest] holds NT, through [Step10Cases]: its
 * cases from `lb-cases.tsv` beside the NT file (written at run time by `NtCasesWriteTest`, never committed), held to
 * [LbExpected] on the six fields and the stored tuple digest; profiles `<oracle dir>/profiles2`. E1 as in [Step10Cases].
 */
class LbEngineCommonTest {
    @Test
    fun lbCasesFromTheRunTimeFileThroughTheEngineMatchExpected1b() {
        val nt = Step10Cases.ntFile("LB") ?: return
        Step10Cases.check("LB", nt.parent!! / "lb-cases.tsv", LbExpected.ROWS)
    }
}
