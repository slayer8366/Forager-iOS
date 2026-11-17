package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.oracle.OracleCalls
import com.zynergylabs.forager.brouter.oracle.e3.CosRowsGenerator
import com.zynergylabs.forager.brouter.oracle.e3.E3CaseRun
import com.zynergylabs.forager.brouter.oracle.e3.E3Expected
import com.zynergylabs.forager.brouter.util.CountingLibm
import com.zynergylabs.forager.brouter.util.MsunLibm
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The cosine rows' production run (session 77; s72 §4.5 "Jobs", dispatch 7): the 472 cases of `expected-cosrows.tsv`
 * ([CosRowsRun.cases]) through the port **as it ships** ([MsunLibm]) against the oracle run in this JVM, recorded per row as
 * [RoutingEngineProductionRecordTest] records its groups. A class of its own beside that one: its `portMsun` takes no seam
 * and its `record` prints no ids, so adding this group there would thread a change through working code.
 *
 * **Gate:** [RoutingEngineProductionRecordTest.GATE], as there: unset, it prints one line and does nothing (the `jvm` job).
 *
 * **Recorded, not asserted**, as there: it asserts that every case produced an outcome on both sides, that each row's cases
 * are its 59 rows, that equal and differing add up, and (session 78, review 5 S1) that no oracle run in this JVM is off its
 * stored row's tuple digest, which judges the case list, not the port. A difference from the oracle is printed, never failed. Per row, in
 * the existing record's columns: cases, equal on all six fields, differing; per field how many differ (kind, nodes, cost,
 * distance, node digest, heights digest); the split (cost only, same nodes with another distance, a different route,
 * other); how many oracle runs in this JVM are off their stored row. Then each differing case by id with its fields.
 *
 * **Rows read on msun's table:** each port run is counted ([CountingLibm] over [MsunLibm]) and [CosRowsRun.rowAssertion]
 * is asserted: each case read its own row; the group's (c) rows read are exactly the 8. Ids, fields and counts only (E1).
 */
class RoutingEngineCosRowsProductionRecordTest {
    @Test
    fun cosRowsProductionRunIsRecordedAgainstTheOracle() {
        val gate = System.getenv(RoutingEngineProductionRecordTest.GATE)
        if (gate.isNullOrEmpty()) {
            println("cosrows production record: ${RoutingEngineProductionRecordTest.GATE} is unset; not run (RECORD -135)")
            return
        }
        val t0 = System.nanoTime()
        val rows = CosRowsRun.rows.associateBy { it.id }
        val read = LinkedHashMap<String, Set<Int>>()
        val lines = ArrayList<String>()
        val differing = ArrayList<String>()
        for ((row, cases) in CosRowsRun.cases()) {
            val stratum = CosRowsGenerator.stratum(row)
            var equal = 0
            var offRow = 0
            var outcomes = 0
            val field = IntArray(6)
            val split = IntArray(4)
            for (c in cases) {
                val r = rows.getValue(c.id)
                val o = E3CaseRun.run(c, OracleCalls.oracleDir).outcome
                val counter = CountingLibm(MsunLibm)
                val p = CosRowsRun.port(c, counter)
                read[c.id] = counter.rows
                outcomes++
                if (r.tupleDigest != E3Expected.fnv1aText(o.toField())) offRow++
                val d = booleanArrayOf(
                    o.kind != p.kind, o.nodes != p.nodes, o.cost != p.cost, o.distance != p.distance,
                    o.nodeDigest != p.nodeDigest, o.heightDigest != p.heightDigest,
                )
                if (d.none { it }) {
                    equal++
                    continue
                }
                d.forEachIndexed { i, b -> if (b) field[i]++ }
                split[ // RoutingEngineProductionRecordTest.record's split, unchanged
                    when {
                        d[0] -> 3
                        d[4] -> 2
                        d[3] -> 1
                        d[2] && !d[1] && !d[5] -> 0
                        else -> 3
                    },
                ]++
                differing += "${c.id}: ${CosRowsRun.differing(o, p)}"
            }
            assertEquals(cases.size, outcomes, "$stratum: every case produced an outcome")
            assertEquals(59, cases.size, "$stratum: cases against the row's 59")
            assertEquals(cases.size, equal + split.sum(), "$stratum: equal and differing add up to the cases")
            val line = "cosrows production record: $stratum | ${cases.size} | $equal | ${cases.size - equal} | ${field.joinToString(" ")} | " +
                "${split.joinToString(" ")} | $offRow"
            println(line)
            lines += line
            // Session 78 (review 5, S1): this judges the case list, not the port; a production difference is still only printed.
            assertEquals(0, offRow, "$stratum: oracle runs in this JVM off the stored row's tuple digest; the rebuilt cases are not the selected ones")
        }
        val failures = CosRowsRun.rowAssertion("msun's table", read)
        println("cosrows production record: row | cases | equal | differing | kind nodes cost distance nodeDigest heightsDigest | " +
            "costOnly sameNodesDistance differentRoute other | oracleOffRow")
        lines.forEach { println(it) }
        differing.forEach { println("cosrows production record: differs $it") }
        println("cosrows production record: ${read.size} cases, ${differing.size} differing, in %.1f s".format((System.nanoTime() - t0) / 1e9))
        failures.forEach { println("cosrows production record ROW ASSERTION FAILED: $it") }
        assertEquals(rows.keys, read.keys, "every row of expected-cosrows ran")
        assertTrue(failures.isEmpty(), "the cosine rows' row assertion on msun's table: $failures")
    }
}
