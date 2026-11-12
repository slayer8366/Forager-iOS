package com.zynergylabs.forager.brouter.tiles

import com.zynergylabs.forager.brouter.mapaccess.PeninsulaDepth
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Step 10, session 51 (RECORD -142, decision 3; E5's fallback): stratum R's first 12 cases of each of its 20 StdModel
 * profiles, as [LbEngineCommonTest] holds LB, through [Step10Cases]: the cases from `r-cases.tsv` beside the NT file
 * (written at run time by `NtCasesWriteTest`, never committed), held to [RExpected] on the six fields and the stored tuple
 * digest; profiles `<oracle dir>/profiles2`. The six car profiles are left out ([RExpected]).
 *
 * Gated on [R_GATE] (`ios-memory` sets it as `SIMCTL_CHILD_`[R_GATE]); unset (`jvm`, `ios-smoke`), it prints one line and
 * does nothing. Some of R's cases reach `cleanupPeninsulas`' recursion, which has no catch on iOS (RECORD -61), so R runs
 * in the job where a process ended by it ends that job alone. It prints the profiles its cases cover and, as the memory
 * groups do, the deepest recursion and how many cases entered the walk. E1 as in [Step10Cases].
 */
internal const val R_GATE = "BROUTER_STEP10_R"

class REngineCommonTest {
    @Test
    fun rFirst12CasesOfEachStdModelProfileThroughTheEngineMatchExpected1b() {
        if (testEnvironment(R_GATE).isNullOrEmpty()) {
            println("step10 R engine: $R_GATE is unset; not run (RECORD -142)")
            return
        }
        val nt = Step10Cases.ntFile("R") ?: return
        val profiles = LinkedHashSet<String>()
        var deepest = 0
        var deepestId = ""
        var walked = 0
        var overflows = 0
        PeninsulaDepth.reset()
        try {
            Step10Cases.check("R", nt.parent!! / "r-cases.tsv", RExpected.ROWS) { id, _ ->
                profiles += id.removePrefix("R-").substringBeforeLast('-')
                if (PeninsulaDepth.deepest > 0) walked++
                if (PeninsulaDepth.deepest > deepest) {
                    deepest = PeninsulaDepth.deepest
                    deepestId = id
                }
                overflows += PeninsulaDepth.overflowsCaught
                PeninsulaDepth.reset()
            }
        } finally {
            println(
                "step10 R profiles: ${profiles.size} of ${RExpected.PROFILES.size} covered; deepest recursion $deepest " +
                    "(case $deepestId); $walked cases entered the walk; $overflows overflows caught; ${testThreadStack()}",
            )
        }
        assertEquals(RExpected.PROFILES, profiles.toList(), "R's profiles covered, in the file's order")
    }
}
