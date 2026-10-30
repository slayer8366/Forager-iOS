package com.zynergylabs.forager.brouter.oracle.e3.strata

import com.zynergylabs.forager.brouter.oracle.e3.E3Select
import com.zynergylabs.forager.brouter.oracle.e3.E3Stability
import java.io.File
import kotlin.test.assertTrue

/**
 * What E4's targeted strata share (RECORD -51, -52): `hiking-mountain`, 59 cases, a cap of 20 × 59 draws, selection by
 * the oracle's R2 ([E3Select]), then E3's six runs over the cases taken. `hiking-mountain` switches turn restrictions off,
 * so these strata do not exercise them; R does (RECORD -51).
 */
object E3Targeted {
    const val PROFILE = "hiking-mountain"
    const val N = 59
    const val CAP = 20 * N
    const val PER_CASE_SECONDS = 600L

    fun select(label: String, draws: Iterator<E3Select.Draw>, strata: List<String>, mark: (E3Select.Run) -> E3Select.Mark) =
        E3Select.select(label, draws, strata, N, CAP, File(E3Strata.work, "$label-select"), E3Strata.workers, PER_CASE_SECONDS, mark)
            .also { s ->
                println("E3 $label selection: ${s.draws} draws counted (cap $CAP), ${s.routed} routed; table ${s.table}")
                for ((why, k) in s.rejected) println("E3 $label rejected: $k $why")
                for ((stratum, runs) in s.taken) {
                    val last = runs.lastOrNull()?.draw
                    println("E3 $stratum taken: ${runs.size} of $N${if (runs.size < N) " (SHORT)" else ""}; last taken at draw $last")
                }
            }

    /**
     * E3's six runs over [stratum]'s cases. R1 and R2 run again there (the handoff's option: re-run, and record that the
     * selection's R1 equals E3's): each case's selection R1 tuple and R2 lines are compared with E3's and every difference
     * is printed and counted. Null when nothing was taken.
     */
    fun stability(stratum: String, s: E3Select.Selection): E3Stability.Result? {
        val runs = s.taken.getValue(stratum)
        if (runs.isEmpty()) return null
        val r = E3Stability.run(stratum, runs.map { it.case }, File(E3Strata.work, stratum), E3Strata.agent, E3Strata.workers, PER_CASE_SECONDS)
        var differ = 0
        for ((v, sel) in r.verdicts.zip(runs)) {
            check(v.case.id == sel.case.id)
            val same = v.outcomes["R1"] == sel.r1 && (v.logs["R2"] ?: emptyList()) == sel.r2Log
            if (!same) {
                differ++
                println("E3 $stratum ${sel.case.id}: selection's R1/R2 differ from E3's: R1 ${sel.r1.toField()} vs ${v.outcomes["R1"]!!.toField()}")
            }
        }
        println("E3 $stratum: selection's R1 tuple and R2 lines equal E3's in ${runs.size - differ} of ${runs.size}")
        println("E3 $stratum kinds (R1): ${r.verdicts.groupingBy { it.outcomes.getValue("R1").kind }.eachCount()}")
        return r
    }

    /** E3's report and stop for each stratum, all reported before the first failure is thrown; then each must be full. */
    fun report(s: E3Select.Selection, results: Map<String, E3Stability.Result?>) {
        val failures = ArrayList<Throwable>()
        for ((stratum, r) in results) {
            if (r == null) {
                println("E3 $stratum: nothing taken, E3 not run")
                continue
            }
            try {
                E3Strata.report(r)
            } catch (e: AssertionError) {
                failures += e
            }
        }
        failures.firstOrNull()?.let { throw it }
        val short = s.taken.filterValues { it.size < N }.mapValues { it.value.size }
        assertTrue(short.isEmpty(), "${s.label}: short after ${s.draws} draws: $short of $N")
    }
}
