package com.zynergylabs.forager.brouter.msun

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Step 2b: commonTest's committed reference (MsunReference.kt) equals this run's fresh build of bionic's C, line for
 * line. This is how the C reaches the iOS simulator's tests: built here, from the pinned sources, on every jvmTest run.
 */
class MsunReferenceCommittedTest {
    @Test
    fun committedCosRowsAreTheCBuilds() {
        val committed = MSUN_COS_ROWS_A.lines() + MSUN_COS_ROWS_B.lines()
        assertEquals(1800, MsunCBuild.cosLines.size, "C rows")
        assertEquals(MsunCBuild.cosLines, committed)
    }

    @Test
    fun committedEdgesAreTheCBuilds() {
        // gen-kotlin.py writes "u" for a cos line in k_rem_pio2.c's range; the C's own line reads "c".
        val committed = MSUN_EDGES.lines().map { if (it.startsWith("u ")) "c" + it.substring(1) else it }
        assertEquals(104, MsunCBuild.edgeLines.size, "C edge lines")
        assertEquals(MsunCBuild.edgeLines, committed)
    }
}
