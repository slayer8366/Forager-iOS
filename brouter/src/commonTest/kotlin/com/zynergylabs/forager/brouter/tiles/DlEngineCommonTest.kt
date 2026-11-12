package com.zynergylabs.forager.brouter.tiles

import kotlin.test.Test

/**
 * Step 10, session 48 (RECORD -139, decision 2): strata DL-lat and DL-lon as [NtEngineCommonTest] holds NT, through
 * [Step10Cases]: their cases from `dl-lat-cases.tsv` and `dl-lon-cases.tsv` beside the NT file (written at run time by
 * `NtCasesWriteTest` from `RoutingEngineDLTest`'s draws, never committed), held to [DlLatExpected] and [DlLonExpected] on
 * the six fields and the stored tuple digest; tiles `<tiles dir>/segments`, profiles `<oracle dir>/profiles2`. E1 as in
 * [Step10Cases].
 */
class DlEngineCommonTest {
    @Test
    fun dlLatCasesFromTheRunTimeFileThroughTheEngineMatchExpected1b() {
        val nt = Step10Cases.ntFile("DL-lat") ?: return
        Step10Cases.check("DL-lat", nt.parent!! / "dl-lat-cases.tsv", DlLatExpected.ROWS)
    }

    @Test
    fun dlLonCasesFromTheRunTimeFileThroughTheEngineMatchExpected1b() {
        val nt = Step10Cases.ntFile("DL-lon") ?: return
        Step10Cases.check("DL-lon", nt.parent!! / "dl-lon-cases.tsv", DlLonExpected.ROWS)
    }
}
