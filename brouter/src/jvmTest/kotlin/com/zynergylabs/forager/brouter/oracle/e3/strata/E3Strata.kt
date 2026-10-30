package com.zynergylabs.forager.brouter.oracle.e3.strata

import com.zynergylabs.forager.brouter.oracle.e3.E3Stability
import java.io.File
import kotlin.test.assertTrue

/** What the stratum tests share: where the e3Strata task puts the work, the agent, and the report against E3's stop. */
object E3Strata {
    val work: File = File(System.getProperty("brouter.e3.work") ?: error("brouter.e3.work is not set; run e3Strata"))
    val agent: File = File(System.getProperty("brouter.jacoco.agent") ?: error("brouter.jacoco.agent is not set; run e3Strata"))

    /** JVMs at once. Each case's runs are independent JVMs, and no case has a time limit, so load moves no outcome. */
    val workers: Int = maxOf(1, Runtime.getRuntime().availableProcessors() - 1)

    /** Prints the stratum's count and every unstable case, then holds it to E3's controls and its 5% stop. */
    fun report(r: E3Stability.Result) {
        val n = r.verdicts.size
        println("E3 ${r.stratum}: $n cases; unstable ${r.unstable.size} of $n; over 5%: ${r.passesFivePercent}")
        for (v in r.verdicts) {
            println(
                "E3 ${r.stratum} ${v.case.id}: ${if (v.stable) "stable" else "UNSTABLE"}; R1 ${v.outcomes["R1"]!!.toField()}; " +
                    "collection lines ${v.logs["R2"]?.size ?: 0}; R1 ${v.r1Millis} ms; control R1 ${v.controls["R1"]} R4 ${v.controls["R4"]}",
            )
            if (!v.stable) {
                for (regime in E3Stability.REGIMES) println("  $regime ${v.outcomes[regime]!!.toField()} log ${v.logs[regime]}")
                println("  kinds agree in all six: ${v.kindAgrees}")
            }
        }
        val still = r.verdicts.filter { !it.controlMoves }.map { it.case.id }
        assertTrue(still.isEmpty(), "${r.stratum}: R1 and R4 gave the control one order for $still: E3 has not been run")
        assertTrue(!r.passesFivePercent, "${r.stratum}: ${r.unstable.size} of $n unstable, over E3's 5%: a stop for Phase 2")
    }
}
