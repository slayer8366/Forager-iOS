package com.zynergylabs.forager.brouter.oracle.e3.strata

import com.zynergylabs.forager.brouter.oracle.e3.E3Case
import com.zynergylabs.forager.brouter.oracle.e3.E3Coverage
import com.zynergylabs.forager.brouter.oracle.e3.E3Outcome
import com.zynergylabs.forager.brouter.oracle.e3.E3Stability
import com.zynergylabs.forager.brouter.tiles.TestTiles
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The coverage reader on real execution data (RECORD -77 item 1), seen failing first: F0's `inside-long` case, run as R1
 * (with F0's ends, [E3F0Test]) and as R6 under JaCoCo's agent, as E3 runs them.
 * - **Known not to reach:** CR's lines, `StdPath.java:101-103` and `:105-113`. They sit inside `if (rc.bikeMode)`
 *   (`StdPath.java:94`), and `hiking-mountain` sets `validForFoot` only (`hiking-mountain.brf:84`), so `bikeMode` is
 *   false (`RoutingContext.java:119`). They must read "not covered".
 * - **Known to reach:** `RoutingEngine.java:177`, `doRouting(maxRunningTime)` in `doRun`'s routing branch, which every
 *   routed case passes. It must read "covered", so a reader that read nothing as covered fails here.
 * - NT's line is read and printed, with no assertion: whether F0 reaches it is not known beforehand.
 */
class E3CoverageTest {
    @Test
    fun crsLinesReadNotCoveredOnAFootCaseAndDoRunReadsCovered() {
        val label = "COV"
        E3Strata.jdk(label)
        val work = File(E3Strata.work, label).also { it.deleteRecursively(); it.mkdirs() }
        val case = E3Case("COV-F0-inside-long", TestTiles.f0, 41.2772811, -74.0422743, 41.292043, -73.996678, "hiking-mountain")
        val caseFile = File(work, "cases.tsv").also { E3Case.writeList(listOf(case), it) }
        val exec = File(work, "${case.id}.exec")
        val agent = listOf("-javaagent:${E3Strata.agent.absolutePath}=destfile=${exec.absolutePath},includes=btools.*")
        fun outcome(out: List<String>, regime: String) =
            E3Outcome.fromField(out.single { it.startsWith("outcome $regime ${case.id} ") }.split(' ', limit = 4)[3])
        val r1 = outcome(E3Stability.launch("R1R2", case.id, caseFile, work, emptyList(), 600), "R1")
        val r6 = outcome(E3Stability.launch("R6", case.id, caseFile, work, agent, 600), "R6")
        println("E3 $label R1 ${r1.toField()}")
        println("E3 $label R6 ${r6.toField()}")
        assertEquals("route", r1.kind, "F0's inside-long routes")

        val cr = E3Coverage.reach(r1, r6, exec, E3Coverage.CR)
        println("E3 $label CR on a foot case: ${cr.describe()}")
        for ((s, ls) in cr.lines) for (l in ls) println("E3 $label   $s line ${l.number}: ${l.covered}/${l.total} instructions")
        assertEquals(false, cr.reached, "CR's lines on a foot profile: ${cr.describe()}")

        val run = E3Coverage.reach(r1, r6, exec, listOf(E3Coverage.Span("btools/router/RoutingEngine.java", 177)))
        println("E3 $label doRun's routing call: ${run.describe()}")
        assertEquals(true, run.reached, run.describe())

        val nt = E3Coverage.reach(r1, r6, exec, E3Coverage.NT)
        println("E3 $label NT's line on F0: ${nt.describe()}")
        assertFalse(nt.refused != null, nt.describe())
        assertTrue(exec.length() > 0)
    }
}
