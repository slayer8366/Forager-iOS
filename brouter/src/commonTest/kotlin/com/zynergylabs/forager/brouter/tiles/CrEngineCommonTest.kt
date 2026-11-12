package com.zynergylabs.forager.brouter.tiles

import kotlin.test.Test

/**
 * Step 10, session 47 (RECORD -138, decision 2): stratum CR as [NtEngineCommonTest] holds NT, through [Step10Cases]: its
 * cases from `cr-cases.tsv` beside the NT file, held to [CrExpected]. CR's crossing profile is not among the oracle's, so
 * `NtCasesWriteTest` writes it with `lookups.dat` into `profiles2/` beside the NT file, and that folder is read here.
 * Each line carries its memory class (64 for CR). E1 as in [Step10Cases].
 */
class CrEngineCommonTest {
    @Test
    fun crCasesFromTheRunTimeFileThroughTheEngineMatchExpected1b() {
        val nt = Step10Cases.ntFile("CR") ?: return
        val dir = nt.parent!!
        Step10Cases.check("CR", dir / "cr-cases.tsv", CrExpected.ROWS, ownProfiles = dir / "profiles2")
    }
}
