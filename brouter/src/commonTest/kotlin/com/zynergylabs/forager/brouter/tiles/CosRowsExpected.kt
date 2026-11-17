package com.zynergylabs.forager.brouter.tiles

/**
 * Session 77: the cosine rows' group for the simulator (s72 §4.5), as [LbExpected] holds LB's.
 * - [ROWS]: the 472 rows of brouter/src/jvmTest/resources/e3/expected-cosrows.tsv in file order ([CosRowsExpectedA] then
 *   [CosRowsExpectedB]), ids `COS<row>-nnnn`, every row `exact`. No row holds a position (E1).
 * - [TABLE_ROWS]: the 8 table rows the group is for, ascending; a case's row is its id's prefix after `COS`.
 * - [C_ROWS]: the 13 rows where msun's scale table differs from the oracle's (`CheapProductionRecordTest.known`), so the
 *   simulator can check that the group's rows read among them are exactly [TABLE_ROWS] without the oracle's table.
 * `CosRowsCommonDigestTest` (jvmTest) holds [ROWS] to the file and [C_ROWS] to the table as computed on the JVM.
 */
internal object CosRowsExpected {
    val ROWS: List<NtExpected.Row> = CosRowsExpectedA.ROWS + CosRowsExpectedB.ROWS

    val TABLE_ROWS: List<Int> = listOf(474, 488, 502, 663, 675, 1046, 1053, 1079)

    val C_ROWS: List<Int> = listOf(474, 488, 502, 663, 675, 1046, 1053, 1079, 1295, 1297, 1310, 1311, 1318)

    /** The table row of a case id `COS<row>-nnnn`. */
    fun rowOf(id: String): Int = id.substringBefore('-').removePrefix("COS").toInt()
}
