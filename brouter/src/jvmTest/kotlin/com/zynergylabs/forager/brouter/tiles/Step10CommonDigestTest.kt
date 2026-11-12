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
    fun ntConstantEqualsTheFilesNtRows() = constantEqualsTheFilesRows("NT", NtExpected.ROWS)

    /** Session 47: LB's and CR's constants, held to the file as NT's is. */
    @Test
    fun lbConstantEqualsTheFilesLbRows() = constantEqualsTheFilesRows("LB", LbExpected.ROWS)

    @Test
    fun crConstantEqualsTheFilesCrRows() = constantEqualsTheFilesRows("CR", CrExpected.ROWS)

    /** Session 48: DL-lat's and DL-lon's constants, held to the file as NT's is. */
    @Test
    fun dlLatConstantEqualsTheFilesDlLatRows() = constantEqualsTheFilesRows("DL-lat", DlLatExpected.ROWS)

    @Test
    fun dlLonConstantEqualsTheFilesDlLonRows() = constantEqualsTheFilesRows("DL-lon", DlLonExpected.ROWS)

    /** Session 48: F0h's eight rows, all `exact`. */
    @Test
    fun f0hConstantEqualsTheFilesF0hRows() = constantEqualsTheFilesRows("F0h", F0hExpected.ROWS, 8)

    /** Session 48: FK's 84 rows of kinds 1 to 8, 81 `exact` and the three `outcome` rows. */
    @Test
    fun fkConstantEqualsTheFilesFkRowsOfKinds1To8() = constantEqualsTheFilesRows(
        "FK", FkExpected.ROWS, 84, setOf("exact", "outcome"), Regex("FK[1-8]-.*"),
    )

    private fun constantEqualsTheFilesRows(
        stratum: String,
        constant: List<NtExpected.Row>,
        count: Int = 59,
        compares: Set<String> = setOf("exact"),
        ids: Regex = Regex(".*"),
    ) {
        val file = rows.filter { it.stratum == stratum && ids.matches(it.id) }
        assertEquals(count, file.size, "$stratum's rows in expected-1b")
        assertEquals(compares, file.map { it.compare }.toSet(), "$stratum's rows' compare modes")
        val want = file.map { listOf(it.id, common(it.outcome).toField(), it.tupleDigest) }
        val got = constant.map { listOf(it.id, it.outcome.toField(), it.tupleDigest) }
        assertEquals(want, got, "$stratum's constant against expected-1b.tsv's $stratum rows, in file order")
        assertEquals(constant.map { it.tupleDigest }, constant.map { it.outcome.tupleDigest }, "$stratum's constant's tuple digests")
        println("step10 ${stratum}'s constant: ${got.size} of ${want.size} rows equal to expected-1b.tsv's $stratum rows")
    }
}
