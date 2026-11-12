package com.zynergylabs.forager.brouter.tiles

import com.zynergylabs.forager.brouter.oracle.e3.E3Expected
import com.zynergylabs.forager.brouter.oracle.e3.E3Outcome
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Step 10, session 46 (RECORD -138, decision 1): the common code's tuple digest ([EngineRun.fnv1aText] over
 * [EngineRun.Outcome.toField]) against the stored `tupleDigest` of expected-1b.tsv, on the JVM, before the simulator relies
 * on it: F0's two rows by name, and every row of the file. Digests and ids only (E1).
 */
class Step10CommonDigestTest {
    private val rows = E3Expected.read(javaClass.getResource(E3Expected.RESOURCE)!!.readText()).rows

    private fun common(o: E3Outcome) = EngineRun.Outcome(o.kind, o.nodes, o.cost, o.distance, o.nodeDigest, o.heightDigest)

    @Test
    fun f0RowsTupleDigestInCommonCodeEqualsStored() {
        val f0 = rows.filter { it.stratum == "F0" }
        assertEquals(listOf("F0-inside-across-the-degree-line", "F0-inside-long"), f0.map { it.id })
        for (r in f0) assertEquals(r.tupleDigest, common(r.outcome).tupleDigest, "${r.id}: common tuple digest against the stored")
        println("step10 common tuple digest: F0 ${f0.size} of ${f0.size} equal to the stored")
    }

    @Test
    fun everyRowTupleDigestInCommonCodeEqualsStored() {
        val wrong = rows.filter { common(it.outcome).tupleDigest != it.tupleDigest }.map { it.id }
        println("step10 common tuple digest: ${rows.size - wrong.size} of ${rows.size} rows equal to the stored")
        assertEquals(emptyList(), wrong, "rows whose common tuple digest differs from the stored")
    }

    @Test
    fun ntConstantEqualsTheFilesNtRows() {
        val file = rows.filter { it.stratum == "NT" }
        assertEquals(59, file.size, "NT's rows in expected-1b")
        assertEquals(setOf("exact"), file.map { it.compare }.toSet(), "NT's rows are all held exactly")
        val want = file.map { listOf(it.id, common(it.outcome).toField(), it.tupleDigest) }
        val got = NtExpected.ROWS.map { listOf(it.id, it.outcome.toField(), it.tupleDigest) }
        assertEquals(want, got, "NtExpected.ROWS against expected-1b.tsv's NT rows, in file order")
        assertEquals(NtExpected.ROWS.map { it.tupleDigest }, NtExpected.ROWS.map { it.outcome.tupleDigest }, "NtExpected's tuple digests")
        println("step10 NtExpected: ${got.size} of ${want.size} rows equal to expected-1b.tsv's NT rows")
    }
}
