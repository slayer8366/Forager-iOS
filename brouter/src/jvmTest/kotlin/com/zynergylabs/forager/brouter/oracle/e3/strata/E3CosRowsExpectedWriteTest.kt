package com.zynergylabs.forager.brouter.oracle.e3.strata

import com.zynergylabs.forager.brouter.oracle.e3.E3Case
import com.zynergylabs.forager.brouter.oracle.e3.E3CosRowsExpected
import com.zynergylabs.forager.brouter.oracle.e3.E3Expected
import com.zynergylabs.forager.brouter.oracle.e3.E3Outcome
import com.zynergylabs.forager.brouter.oracle.e3.E3Stability
import java.io.File
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Writes the cosine rows' expected values ([E3CosRowsExpected]), as [E3ExpectedWriteTest] writes 1b's, for the strata of
 * [E3CosRowsExpected.STRATA] only: every case through R1 again (a fresh JVM, the case alone, [E3Stability.launch]'s
 * `R1R2`), on the oracle's JDK, from the case list [E3CosRowsTest]'s stability run left in `<work>/<stratum>/cases.tsv`
 * (never committed: it holds the ends). Each R1 is compared with the R1 logged in the stratum's committed stability table;
 * any difference, and any case logged other than stable, is a stop and no file is written. A class of its own, so
 * [E3ExpectedWriteTest] and 1b's file are untouched.
 *
 * Writes `<work>/expected-cos/expected-cosrows.tsv` (copied by hand to `src/jvmTest/resources/e3/`) and
 * `<work>/expected-cos/r1-vs-log.tsv`. Run alone: `e3Strata --tests '*E3CosRowsExpectedWriteTest*'`.
 */
class E3CosRowsExpectedWriteTest {
    @Test
    fun writeCosRowsExpectedValuesFromR1() {
        E3Strata.jdk("expected-cos")
        val out = File(E3Strata.work, "expected-cos").also { it.deleteRecursively(); it.mkdirs() }
        val stops = ArrayList<String>()
        val rows = ArrayList<E3Expected.Row>()
        val p = { k: String -> System.getProperty(k) }
        val head = listOf(
            "# The cosine rows' expected values (session 76): each case's R1 outcome (Phase 1 report, E3), written by E3CosRowsExpectedWriteTest.",
            "# jdk java.vendor=${p("java.vendor")}; java.runtime.version=${p("java.runtime.version")}; os.arch=${p("os.arch")}",
            "# tuple kind|nodes|cost|distance|nodeDigest|heightsDigest; tupleDigest: 64-bit FNV-1a over its UTF-8 bytes",
            "# compare exact: whole tuple (every row; a case logged unstable is a stop)",
        )
        val pool = Executors.newFixedThreadPool(E3Strata.workers)
        try {
            for ((stratum, entry) in E3CosRowsExpected.STRATA) {
                val (n, table) = entry
                val log = E3CosRowsExpected.logged(table)
                val caseFile = File(E3Strata.work, "$stratum/cases.tsv")
                check(caseFile.isFile) { "no case list at $caseFile: run E3CosRowsTest first" }
                val copy = File(out, "cases-$stratum.tsv").also { caseFile.copyTo(it) }
                val cases = E3Case.readList(copy)
                if (cases.size != n) stops += "$stratum: ${cases.size} cases in its list, not $n"
                val jobs = cases.map { c -> c to pool.submit(Callable { E3Stability.launch("R1R2", c.id, copy, out, emptyList(), 600) }) }
                for ((c, job) in jobs) {
                    val r1 = job.get().filter { it.startsWith("outcome R1 ${c.id} ") }.single().split(' ', limit = 4)[3]
                    val outcome = E3Outcome.fromField(r1)
                    val logged = log[c.id]
                    val same = logged != null && logged.second == outcome
                    File(out, "r1-vs-log.tsv").appendText("$stratum\t${c.id}\t${outcome.toField()}\t${logged?.second?.toField()}\t$same\n")
                    if (!same) stops += "$stratum ${c.id}: R1 now ${outcome.toField()}, logged ${logged?.second?.toField()}"
                    if (logged != null && logged.first != "stable") stops += "$stratum ${c.id}: logged ${logged.first}"
                    rows += E3Expected.row(stratum, c.id, "exact", outcome)
                }
                println("E3 expected-cos $stratum: ${jobs.size} cases; stops so far: ${stops.size}")
            }
        } finally {
            pool.shutdownNow()
        }
        for (s in stops) println("E3 expected-cos STOP $s")
        assertTrue(stops.isEmpty(), "the cosine rows' R1 against their logs (a stop): $stops")
        val file = File(out, "expected-cosrows.tsv")
        E3Expected.write(head, rows, file)
        val sha = java.security.MessageDigest.getInstance("SHA-256").digest(file.readBytes()).joinToString("") { "%02x".format(it) }
        println("E3 expected-cos: wrote ${rows.size} rows; SHA-256 $sha")
    }
}
