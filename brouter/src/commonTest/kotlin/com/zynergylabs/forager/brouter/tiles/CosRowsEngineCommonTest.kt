package com.zynergylabs.forager.brouter.tiles

import com.zynergylabs.forager.brouter.util.MsunLibm
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Session 77 (s72 §4.5, dispatch 7): the cosine rows' 472 cases on the simulator, as [LbEngineCommonTest] holds LB, through
 * [Step10Cases.check]: `cos-cases.tsv` beside the NT file (written at run time by `NtCasesWriteTest`, never committed; each
 * line's eighth field is the cosine rows' tiles folder), held to [CosRowsExpected.ROWS] on the six fields and the stored
 * tuple digest, on `MsunLibm` (a production run, as every simulator group).
 *
 * **Rows read:** the engine's seam is a [CommonCountingLibm] over `MsunLibm`, read per case in `onCase`. After the check:
 * each case read its own row, and the rows read by all cases, intersected with [CosRowsExpected.C_ROWS], are exactly
 * [CosRowsExpected.TABLE_ROWS]. Ids, row numbers and counts only (E1).
 */
class CosRowsEngineCommonTest {
    @Test
    fun cosRowsCasesFromTheRunTimeFileThroughTheEngineMatchExpectedCosrows() {
        val nt = Step10Cases.ntFile("COS") ?: return
        val counter = CommonCountingLibm(MsunLibm)
        val read = LinkedHashMap<String, Set<Int>>()
        Step10Cases.check("COS", nt.parent!! / "cos-cases.tsv", CosRowsExpected.ROWS, libm = counter, onCase = { id, _ -> read[id] = counter.take() })
        val notOwn = read.filter { (id, rows) -> CosRowsExpected.rowOf(id) !in rows }.keys.toList()
        val group = read.values.flatten().filter { it in CosRowsExpected.C_ROWS }.distinct().sorted()
        for (row in CosRowsExpected.TABLE_ROWS) {
            val ids = read.keys.filter { CosRowsExpected.rowOf(it) == row }
            val seen = ids.flatMap { read.getValue(it) }.filter { it in CosRowsExpected.C_ROWS }.distinct().sorted()
            println("step10 COS$row rows: ${ids.size} cases; own row read by ${ids.count { row in read.getValue(it) }}; (c) rows read $seen")
        }
        println("step10 COS group: (c) rows read by all cases $group")
        assertEquals(emptyList(), notOwn, "cases that did not read their own row on msun's table")
        assertEquals(CosRowsExpected.TABLE_ROWS, group, "the group's (c) rows read on msun's table")
    }
}
