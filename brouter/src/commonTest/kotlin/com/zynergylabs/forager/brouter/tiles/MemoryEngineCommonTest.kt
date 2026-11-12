package com.zynergylabs.forager.brouter.tiles

import com.zynergylabs.forager.brouter.mapaccess.PeninsulaDepth
import kotlin.test.Test

/**
 * Step 10, session 49 (RECORD -140, decisions 1 and 2): the memory groups as [DlEngineCommonTest] holds DL, through
 * [Step10Cases]: MP64 (memory class 64), ML5 and PM5 (memory class 5, one draw sequence), their cases from
 * `mp64-cases.tsv`, `ml5-cases.tsv` and `pm5-cases.tsv` beside the NT file (written at run time by `NtCasesWriteTest` from
 * `RoutingEngineMemoryTest`'s draws, never committed), held to [Mp64Expected], [Ml5Expected] and [Pm5Expected] on the six
 * fields and the stored tuple digest; tiles `<tiles dir>/segments`, profiles `<oracle dir>/profiles2`.
 *
 * These are the cases that reach `OsmNodesMap.cleanupPeninsulas` through routing (RECORD -61), whose recursion has no
 * catch on iOS: an overflow ends the test process. So each group is a class of its own, gated on [MEMORY_GATE] as the
 * production run is gated (session 50, RECORD -141, decisions 1 and 2): unset (`jvm`, `ios-smoke`), each prints one line
 * and does nothing; the `ios-memory` job sets it (as `SIMCTL_CHILD_`[MEMORY_GATE]) and runs only these three classes, so a
 * process ended by the recursion ends that job alone. Per group it prints the deepest `minVisitIdInSubtree`
 * recursion of any case ([PeninsulaDepth], reset before each case), how many cases entered the walk
 * and the test thread's stack ([testThreadStack]). Stack bytes in use at the deepest point are not read: no
 * stack address can be taken in common Kotlin, and reading one in the port would take more than the counter (session 49).
 * E1 as in [Step10Cases]: ids, counts, depths, byte counts and digests only.
 */
internal const val MEMORY_GATE = "BROUTER_STEP10_MEMORY"

private fun memoryGroup(stratum: String, file: String, rows: List<NtExpected.Row>) {
    if (testEnvironment(MEMORY_GATE).isNullOrEmpty()) {
        println("step10 $stratum memory: $MEMORY_GATE is unset; not run (RECORD -141)")
        return
    }
    val nt = Step10Cases.ntFile(stratum) ?: return
    var deepest = 0
    var walked = 0
    var deepestId = ""
    PeninsulaDepth.reset()
    try {
        Step10Cases.check(stratum, nt.parent!! / file, rows) { id, _ ->
            if (PeninsulaDepth.deepest > 0) walked++
            if (PeninsulaDepth.deepest > deepest) {
                deepest = PeninsulaDepth.deepest
                deepestId = id
            }
            PeninsulaDepth.reset()
        }
    } finally {
        println(
            "step10 $stratum depth: deepest recursion $deepest (case $deepestId); $walked cases entered the walk; " +
                "${testThreadStack()}",
        )
    }
}

class Mp64EngineCommonTest {
    @Test
    fun mp64CasesFromTheRunTimeFileThroughTheEngineMatchExpected1b() = memoryGroup("MP64", "mp64-cases.tsv", Mp64Expected.ROWS)
}

class Ml5EngineCommonTest {
    @Test
    fun ml5CasesFromTheRunTimeFileThroughTheEngineMatchExpected1b() = memoryGroup("ML5", "ml5-cases.tsv", Ml5Expected.ROWS)
}

class Pm5EngineCommonTest {
    @Test
    fun pm5CasesFromTheRunTimeFileThroughTheEngineMatchExpected1b() = memoryGroup("PM5", "pm5-cases.tsv", Pm5Expected.ROWS)
}
