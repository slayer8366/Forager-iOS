package com.zynergylabs.forager.brouter.tiles

import com.zynergylabs.forager.brouter.oracle.e3.CosRowsGenerator
import com.zynergylabs.forager.brouter.oracle.e3.E3CosRowsExpected
import com.zynergylabs.forager.brouter.oracle.e3.E3Outcome
import com.zynergylabs.forager.brouter.router.CosRowsRun
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Session 77: the simulator's constant for the cosine rows ([CosRowsExpected]) held to `expected-cosrows.tsv` on the JVM, as
 * `Step10CommonDigestTest.constantEqualsTheFilesRows` holds [LbExpected] to `expected-1b.tsv` (that one reads only 1b's
 * file, so this is a class of its own). Ids, digests and row numbers only (E1).
 */
class CosRowsCommonDigestTest {
    private fun common(o: E3Outcome) = EngineRun.Outcome(o.kind, o.nodes, o.cost, o.distance, o.nodeDigest, o.heightDigest)

    @Test
    fun cosRowsConstantEqualsTheFilesRows() {
        val file = CosRowsRun.rows
        assertEquals(E3CosRowsExpected.ROW_COUNT, file.size, "expected-cosrows' rows")
        assertEquals(setOf("exact"), file.map { it.compare }.toSet(), "expected-cosrows' compare modes")
        val want = file.map { listOf(it.id, common(it.outcome).toField(), it.tupleDigest) }
        val got = CosRowsExpected.ROWS.map { listOf(it.id, it.outcome.toField(), it.tupleDigest) }
        val wrong = want.indices.filter { it >= got.size || want[it] != got[it] }.map { want[it][0] }
        println("step10 COS's constant: ${got.size} rows; ${want.size - wrong.size} of ${want.size} equal to expected-cosrows.tsv in file order; differing ${wrong.take(20)}")
        assertEquals(want, got, "the cosine rows' constant against expected-cosrows.tsv, in file order")
        assertEquals(CosRowsExpected.ROWS.map { it.tupleDigest }, CosRowsExpected.ROWS.map { it.outcome.tupleDigest }, "the constant's tuple digests")
    }

    @Test
    fun cosRowsConstantsRowListsAreTheTables() {
        assertEquals(CosRowsGenerator.ROWS, CosRowsExpected.TABLE_ROWS, "the group's 8 rows")
        assertEquals(CosRowsRun.cRows, CosRowsExpected.C_ROWS, "the 13 (c) rows, msun's table against the oracle's")
        assertEquals(CosRowsExpected.ROWS.map { it.id.substringBefore('-') }.distinct(), CosRowsExpected.TABLE_ROWS.map { "COS$it" }, "the constant's id prefixes")
    }
}
