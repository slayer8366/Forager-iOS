package com.zynergylabs.forager.brouter.oracle.e3

import com.zynergylabs.forager.brouter.oracle.OracleCalls
import java.io.File
import java.util.concurrent.Callable
import java.util.concurrent.Executors

/**
 * E3's six runs over one stratum's cases (Phase 1 report, E3), and the stability verdict per case.
 *
 * A case is stable when R1 to R6 give one tuple and the logged runs (R2, R3, R4, R5) give one list of collection lines.
 * R1+R2, R4, R5 and R6 are a fresh JVM per case; R3 is one JVM over the whole list. Every JVM gets its own copy of the
 * oracle's `profiles2/`, so a `debug.txt` beside it reaches no other run. R6 writes `<work>/jacoco/<id>.exec`.
 */
object E3Stability {
    val REGIMES = listOf("R1", "R2", "R3", "R4", "R5", "R6")
    val LOGGED = listOf("R2", "R3", "R4", "R5")

    class Verdict(
        val case: E3Case,
        val outcomes: Map<String, E3Outcome>,
        val logs: Map<String, List<String>>,
        val r1Millis: Long,
        val controls: Map<String, String>,
    ) {
        val tupleStable get() = REGIMES.map { outcomes[it] }.distinct().size == 1
        val logStable get() = LOGGED.map { logs[it] ?: emptyList() }.distinct().size == 1
        val stable get() = tupleStable && logStable
        val kindAgrees get() = REGIMES.map { outcomes[it]?.kind }.distinct().size == 1
        val controlMoves get() = controls["R1"] != controls["R4"]
    }

    class Result(val stratum: String, val verdicts: List<Verdict>) {
        val unstable get() = verdicts.filter { !it.stable }

        /** E3's stop: more than 5% of the stratum unstable. Integer form of unstable/n > 0.05. */
        val passesFivePercent get() = unstable.size * 100 > verdicts.size * 5
    }

    fun run(stratum: String, cases: List<E3Case>, work: File, agentJar: File, workers: Int, perCaseSeconds: Long): Result {
        require(cases.map { it.id }.distinct().size == cases.size) { "$stratum: case ids repeat" }
        work.deleteRecursively()
        work.mkdirs()
        val caseFile = File(work, "cases.tsv").also { E3Case.writeList(cases, it) }
        val jacoco = File(work, "jacoco").also { it.mkdirs() }
        val pool = Executors.newFixedThreadPool(workers)
        try {
            val r3 = pool.submit(Callable { launch("R3", "all", caseFile, work, emptyList(), perCaseSeconds * cases.size) })
            val perCase = cases.map { c ->
                listOf("R1R2", "R4", "R5", "R6").map { regime ->
                    val agent = if (regime == "R6") {
                        listOf("-javaagent:${agentJar.absolutePath}=destfile=${File(jacoco, "${c.id}.exec").absolutePath},includes=btools.*")
                    } else {
                        emptyList()
                    }
                    pool.submit(Callable { launch(regime, c.id, caseFile, work, agent, perCaseSeconds) })
                }
            }
            val outputs = perCase.flatten().flatMap { it.get() } + r3.get()
            return Result(stratum, cases.map { verdict(it, outputs) }).also { writeReport(it, File(work, "stability.tsv")) }
        } finally {
            pool.shutdownNow()
        }
    }

    private fun launch(regime: String, id: String, caseFile: File, work: File, agent: List<String>, seconds: Long): List<String> {
        val base = File(work, "base-$regime-$id")
        File(OracleCalls.oracleDir, "profiles2").copyRecursively(File(base, "profiles2"), overwrite = true)
        try {
            return E3Launcher.run(regime, listOf(base.absolutePath, caseFile.absolutePath, id), agent, seconds).output
        } finally {
            base.deleteRecursively()
        }
    }

    private fun verdict(case: E3Case, outputs: List<String>): Verdict {
        fun mine(kind: String) = outputs.filter { it.startsWith("$kind ") }.map { it.split(' ', limit = 4) }
            .filter { it[2] == case.id }
        val outcomes = mine("outcome").associate { it[1] to E3Outcome.fromField(it[3]) }
        check(outcomes.keys == REGIMES.toSet()) { "${case.id}: regimes ran ${outcomes.keys}, not $REGIMES" }
        val logs = mine("log").groupBy({ it[1] }, { it[3] })
        val r1Millis = mine("millis").single { it[1] == "R1" }[3].toLong()
        // Control lines carry no case id: each single-case JVM's are found through the outcome line beside them.
        val controls = HashMap<String, String>()
        for (o in outputs.indices) {
            val f = outputs[o].split(' ')
            if (f[0] == "control" && f[1] != "R3") {
                val next = outputs.drop(o + 1).firstOrNull { it.startsWith("outcome ") } ?: continue
                if (next.split(' ')[2] == case.id) controls[f[1]] = f[2]
            }
        }
        return Verdict(case, outcomes, logs, r1Millis, controls)
    }

    private fun writeReport(r: Result, file: File) {
        val rows = r.verdicts.map { v ->
            listOf(
                v.case.id, if (v.stable) "stable" else "UNSTABLE", v.tupleStable, v.logStable, v.controlMoves,
                v.outcomes["R1"]!!.toField(), v.logs["R2"]?.size ?: 0, v.r1Millis,
            ).joinToString("\t")
        }
        file.writeText(
            "id\tverdict\ttuple-stable\tlog-stable\tcontrol-R1-ne-R4\tR1-tuple\tR2-collection-lines\tR1-ms\n" +
                rows.joinToString("\n") + "\n",
        )
    }
}
