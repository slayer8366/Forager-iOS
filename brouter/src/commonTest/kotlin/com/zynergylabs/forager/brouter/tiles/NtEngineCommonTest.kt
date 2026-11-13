package com.zynergylabs.forager.brouter.tiles

import kotlin.test.Test

/**
 * Step 10, session 46 (RECORD -138, decision 2): stratum NT through the engine ([EngineRun], `MsunLibm`), its cases read
 * from the file `NtCasesWriteTest` (jvmTest) wrote at run time on a JVM, named by [Step10Cases.VARIABLE]; never
 * committed. Each case whose id is one of NT's 59 stored rows ([NtExpected]) is held to its row on the six fields and the
 * stored tuple digest. A case whose id is not among them is counted, not failed. The file must name all 59 stored ids.
 * Session 47 moved the check into [Step10Cases], unchanged, so LB and CR share it; E1 is kept there as stated.
 *
 * The tiles are `<tiles dir>/segments` (the JVM's `TestTiles.segments`), the profiles `<oracle dir>/profiles2`, as
 * [F0EngineCommonTest]'s.
 */
class NtEngineCommonTest {
    @Test
    fun ntCasesFromTheRunTimeFileThroughTheEngineMatchExpected1b() {
        val file = Step10Cases.ntFile("NT") ?: return
        Step10Cases.check("NT", file, NtExpected.ROWS)
    }
}
