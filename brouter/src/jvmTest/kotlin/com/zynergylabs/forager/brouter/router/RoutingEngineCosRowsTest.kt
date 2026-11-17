package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.oracle.OracleCalls
import com.zynergylabs.forager.brouter.oracle.e3.CosRowsGenerator
import com.zynergylabs.forager.brouter.oracle.e3.E3CaseRun
import com.zynergylabs.forager.brouter.oracle.e3.E3Expected
import com.zynergylabs.forager.brouter.util.CountingLibm
import com.zynergylabs.forager.brouter.util.OracleLibm
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The cosine rows' logic run (session 77; s72 §4.5 "Jobs", dispatch 7), as [RoutingEngineStep9Test] runs LB: the 472 cases
 * of `expected-cosrows.tsv` ([CosRowsRun.cases]), each through the port on the oracle's own table ([OracleLibm]) and held
 * to its row on the whole tuple (the six fields and the tuple digest; every row is `exact`). The oracle in this JVM is held
 * to the row's tuple digest first, which shows the rebuilt case is the selected one.
 *
 * **Rows read.** Each case's port run is counted ([CountingLibm] over [OracleLibm], one per case) and
 * [CosRowsRun.rowAssertion] asserts that each case read its own row and that the group's (c) rows read are exactly the 8.
 *
 * Every difference is collected before the assertions, so a failure lists all of them by id and field. Ids, fields,
 * digests and counts only (E1).
 */
class RoutingEngineCosRowsTest {
    @Test
    fun cosRowsRouteThroughThePortOnTheOraclesTableAsExpected() {
        val t0 = System.nanoTime()
        val rows = CosRowsRun.rows.associateBy { it.id }
        assertEquals(472, rows.size, "expected-cosrows' rows")
        val oracleOff = ArrayList<String>()
        val mismatches = ArrayList<String>()
        val read = LinkedHashMap<String, Set<Int>>()
        for ((row, cases) in CosRowsRun.cases()) {
            var equal = 0
            for (c in cases) {
                val r = rows.getValue(c.id)
                assertEquals("exact", r.compare, "${c.id}: the row is held exactly")
                val o = E3CaseRun.run(c, OracleCalls.oracleDir).outcome
                if (E3Expected.fnv1aText(o.toField()) != r.tupleDigest) oracleOff += c.id
                val counter = CountingLibm(OracleLibm)
                val p = CosRowsRun.port(c, counter)
                read[c.id] = counter.rows
                val differ = CosRowsRun.differing(r.outcome, p) +
                    listOfNotNull("tupleDigest".takeIf { E3Expected.fnv1aText(p.toField()) != r.tupleDigest })
                if (differ.isEmpty()) equal++ else mismatches += "${c.id}: $differ (stored ${r.tupleDigest}; port ${E3Expected.fnv1aText(p.toField())})"
            }
            println("cosrows logic ${CosRowsGenerator.stratum(row)}: ${cases.size} cases; equal to expected-cosrows on the whole tuple $equal; differing ${cases.size - equal}")
        }
        val failures = CosRowsRun.rowAssertion("the oracle's table", read)
        mismatches.forEach { println("cosrows logic mismatch $it") }
        failures.forEach { println("cosrows logic ROW ASSERTION FAILED: $it") }
        println("cosrows logic: ${read.size} cases; oracle in this JVM off its row ${oracleOff.size} $oracleOff; " +
            "port mismatches ${mismatches.size}; %.1f s".format((System.nanoTime() - t0) / 1e9))
        assertEquals(emptyList(), oracleOff, "cases whose oracle run in this JVM differs from expected-cosrows (the case is not the selected one)")
        assertTrue(mismatches.isEmpty(), "the port on the oracle's table against expected-cosrows: ${mismatches.size} mismatches: $mismatches")
        assertTrue(failures.isEmpty(), "the cosine rows' row assertion on the oracle's table: $failures")
    }
}
