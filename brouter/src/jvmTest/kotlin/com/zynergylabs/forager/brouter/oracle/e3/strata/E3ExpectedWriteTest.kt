package com.zynergylabs.forager.brouter.oracle.e3.strata

import com.zynergylabs.forager.brouter.oracle.e3.E3Case
import com.zynergylabs.forager.brouter.oracle.e3.E3Expected
import com.zynergylabs.forager.brouter.oracle.e3.E3Outcome
import com.zynergylabs.forager.brouter.oracle.e3.E3Profiles
import com.zynergylabs.forager.brouter.oracle.e3.E3Stability
import java.io.File
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Writes 1b's expected values ([E3Expected]; RECORD -88): every case of every stratum through R1 again (a fresh JVM, the
 * case alone, on a new thread, no `debug.txt`: [E3Stability.launch]'s `R1R2`, whose R1 is the strata's own), on the oracle's
 * JDK. The cases are the ones each stratum ran, read from its work folder's `cases.tsv` (never committed: they hold the
 * ends). Each R1 is compared with the R1 its stratum logged in the committed stability table; any difference is a stop
 * (RECORD -88) and no file is written. `R-shortest-025` (-50) is also run through E3's six regimes once, and kept out of
 * exact values if it is unstable now. A case its stratum logged unstable (FK9's timing voids, -85) is kept out too.
 *
 * Writes `<work>/expected/expected-1b.tsv` (copied by hand to `src/jvmTest/resources/e3/`) and
 * `<work>/expected/r1-vs-log.tsv`. Run alone: `e3Strata --tests '*E3ExpectedWriteTest*'`.
 */
class E3ExpectedWriteTest {
    @Test
    fun writeExpectedValuesFromR1() {
        E3Strata.jdk("expected")
        val out = File(E3Strata.work, "expected").also { it.deleteRecursively(); it.mkdirs() }
        val rDirs = (E3RProfilesTest.SEEDS.keys + "hiking-mountain").sorted().map { "R-$it" }
        val dirs = E3Expected.STRATA.keys.associateWith { if (it == "R") rDirs else listOf(it) }
        val stops = ArrayList<String>()
        val rows = ArrayList<E3Expected.Row>()
        val head = arrayListOf(
            "# 1b's expected values (RECORD -88): each case's R1 outcome (Phase 1 report, E3), written by E3ExpectedWriteTest.",
            "# jdk ${jdkLine()}",
            "# tuple kind|nodes|cost|distance|nodeDigest|heightsDigest; tupleDigest: 64-bit FNV-1a over its UTF-8 bytes",
            "# compare exact: whole tuple; kind, family: kind only (excluded below); outcome: FK's extra cases (RECORD -80, -85)",
        )
        val excluded = ArrayList<String>()
        val pool = Executors.newFixedThreadPool(E3Strata.workers)
        try {
            for ((stratum, folders) in dirs) {
                val log = E3Expected.loggedFor(stratum)
                if (stratum == "CR") E3Stability.ownProfiles = mapOf(E3Profiles.CROSSING to E3Profiles.crossing())
                try {
                    val jobs = folders.flatMap { folder ->
                        val caseFile = caseList(folder, out)
                        E3Case.readList(caseFile).map { c ->
                            c to pool.submit(Callable { E3Stability.launch("R1R2", c.id, caseFile, out, emptyList(), 600) })
                        }
                    }
                    for ((c, job) in jobs) {
                        val r1 = job.get().filter { it.startsWith("outcome R1 ${c.id} ") }.single().split(' ', limit = 4)[3]
                        val outcome = E3Outcome.fromField(r1)
                        val logged = log[c.id]
                        val same = logged != null && logged.second == outcome
                        File(out, "r1-vs-log.tsv").appendText("$stratum\t${c.id}\t${outcome.toField()}\t${logged?.second?.toField()}\t$same\n")
                        if (!same) stops += "$stratum ${c.id}: R1 now ${outcome.toField()}, logged ${logged?.second?.toField()}"
                        val compare = when {
                            logged != null && logged.first != "stable" -> {
                                excluded += "# excluded $stratum ${c.id} logged ${logged.first} in its stratum's run" +
                                    (if (c.id.startsWith("FK9-")) ", kind 9's timing void (RECORD -85, -86)" else "") + "; compared by family only"
                                "family"
                            }
                            c.id == "R-shortest-025" && !stableNow(c) -> {
                                excluded += "# excluded $stratum ${c.id} unstable when the values were written (RECORD -50, -88), compared on kind only"
                                "kind"
                            }
                            c.id in E3Expected.OUTCOME_ONLY -> "outcome"
                            else -> "exact"
                        }
                        rows += E3Expected.row(stratum, c.id, compare, outcome)
                    }
                    println("E3 expected $stratum: ${jobs.size} cases; R1 unlike its log so far: ${stops.size}")
                } finally {
                    E3Stability.ownProfiles = emptyMap()
                }
            }
        } finally {
            pool.shutdownNow()
        }
        for (s in stops) println("E3 expected STOP $s")
        assertTrue(stops.isEmpty(), "R1 now differs from its stratum's log (RECORD -88, a stop): $stops")
        E3Expected.write(head + excluded, rows, File(out, "expected-1b.tsv"))
        println("E3 expected: wrote ${rows.size} rows; excluded ${excluded.size}: ${excluded.joinToString(" | ")}")
    }

    /**
     * [folder]'s case list as the child JVM reads it. Lists written before [E3Case.Plan] was added (every stratum but FK's)
     * have 9 fields; their plan is [E3Case.Plan.NONE], which is appended. The copy is in [out]; the stratum's own is not edited.
     */
    private fun caseList(folder: String, out: File): File {
        val from = File(E3Strata.work, "$folder/cases.tsv")
        check(from.isFile) { "no case list at $from" }
        val lines = from.readLines().filter { it.isNotBlank() }.map { l ->
            when (l.split('\t').size) {
                10 -> l
                9 -> l + "\t" + E3Case.Plan.NONE
                else -> error("$from: a case line of ${l.split('\t').size} fields")
            }
        }
        return File(out, "cases-$folder.tsv").also { it.writeText(lines.joinToString("\n") + "\n") }
    }

    /** [c] through E3's six regimes once, alone; prints the verdict. */
    private fun stableNow(c: E3Case): Boolean {
        val r = E3Stability.run("expected-${c.id}", listOf(c), File(E3Strata.work, "expected-${c.id}"), E3Strata.agent, 1, 600)
        val v = r.verdicts.single()
        println("E3 expected ${c.id} six regimes: ${if (v.stable) "stable" else "UNSTABLE"}; " +
            E3Stability.REGIMES.joinToString("; ") { "$it ${v.outcomes.getValue(it).toField()}" })
        return v.stable
    }

    private fun jdkLine(): String {
        val p = { k: String -> System.getProperty(k) }
        return "java.vendor=${p("java.vendor")}; java.runtime.version=${p("java.runtime.version")}; os.arch=${p("os.arch")}"
    }
}
