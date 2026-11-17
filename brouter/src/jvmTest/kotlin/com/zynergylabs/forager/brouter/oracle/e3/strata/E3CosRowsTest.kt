package com.zynergylabs.forager.brouter.oracle.e3.strata

import com.zynergylabs.forager.brouter.oracle.OracleCalls
import com.zynergylabs.forager.brouter.oracle.e3.CosRowsGenerator
import com.zynergylabs.forager.brouter.oracle.e3.E3Case
import com.zynergylabs.forager.brouter.oracle.e3.E3Select
import com.zynergylabs.forager.brouter.router.RoutingContext
import com.zynergylabs.forager.brouter.router.RoutingEngine
import com.zynergylabs.forager.brouter.router.RoutingParamCollector
import com.zynergylabs.forager.brouter.tiles.TestTiles
import com.zynergylabs.forager.brouter.util.CountingLibm
import com.zynergylabs.forager.brouter.util.LibmSeam
import com.zynergylabs.forager.brouter.util.OracleLibm
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The cosine rows' group (session 76; s72 §4.5): 59 cases in each of [CosRowsGenerator.ROWS], one stratum per row
 * (`COS474` ... `COS1079`), on `hiking-mountain` and `TestTiles.cosRows`, drawn by [CosRowsGenerator] with its fixed
 * [CosRowsGenerator.SEEDS]. Per row, as [E3LBTest]: [E3Targeted.select] (59 cases, a cap of [E3Targeted.CAP] draws,
 * reach read from R1's route with R2 equal to it), the reach "the route lies in the row's band"; then E3's six runs.
 *
 * **The row assertion.** A candidate whose route lies in the band is also routed once through the port in this JVM on the
 * oracle's own table, counted ([CountingLibm] over [OracleLibm]); it is kept only if its own row is among the rows read.
 * After all 8 selections, and before any stability run:
 * - every row has 59 kept cases within the cap, or the test fails naming the short rows;
 * - every kept case's own row is among the rows its run read (the keep rule, checked again on what was recorded);
 * - the rows read by all kept cases, intersected with the 13 (c) rows, are exactly the 8.
 * The port's outcome is not compared here: that is the next dispatch's logic run. Only counts, row numbers and ids are
 * printed (E1).
 */
class E3CosRowsTest {
    @Test
    fun cosRowsAreSelectedByReachAndRowReadAndCountedAgainstE3sStop() {
        val label = "COS"
        E3Strata.jdk(label)
        println("E3 $label seeds: ${CosRowsGenerator.SEEDS}; profile ${E3Targeted.PROFILE}; cap ${E3Targeted.CAP} draws per row")
        val started = System.nanoTime()
        val gen = CosRowsGenerator(TestTiles.cosRows)
        val selections = LinkedHashMap<Int, E3Select.Selection>()
        val read = HashMap<String, Set<Int>>() // case id to the rows its counted port run read
        try {
            println("E3 $label (c) rows: ${gen.cRows}; the 8 rows with their nodes: ${gen.bandCounts}")
            assertEquals(13, gen.cRows.size, "step 2b's (c) rows are 13 (CheapProductionRecordTest)")
            assertTrue(gen.cRows.containsAll(CosRowsGenerator.ROWS), "the 8 rows are (c) rows")
            for (row in CosRowsGenerator.ROWS) {
                val stratum = CosRowsGenerator.stratum(row)
                val raw = gen.draws(row, stratum, E3Targeted.PROFILE)
                // Before routing: both ends, as the engine will read them, lie in this row's band.
                val checked = object : Iterator<E3Select.Draw> {
                    override fun hasNext() = raw.hasNext()
                    override fun next(): E3Select.Draw = raw.next().also { d ->
                        val c = d.case ?: return@also
                        assertTrue(row(c.startLat) == row && row(c.endLat) == row, "${c.id}: ends in rows ${row(c.startLat)}, ${row(c.endLat)}")
                    }
                }
                selections[row] = E3Targeted.select(stratum, checked, listOf(stratum)) { r ->
                    val e = r.r1Extent ?: return@select E3Select.Mark(null, "no route: ${r.r1.kind}")
                    val lo = row * 100_000
                    if (e[2] < lo || e[3] >= lo + 100_000) return@select E3Select.Mark(null, "route leaves the band")
                    val counter = CountingLibm(OracleLibm)
                    portRun(r.case, counter)
                    if (row !in counter.rows) return@select E3Select.Mark(null, "the port on the oracle's table did not read row $row")
                    read[r.case.id] = counter.rows
                    E3Select.Mark(stratum, "route in row $row's band; row $row read")
                }
            }
        } finally {
            gen.close()
        }
        val failures = rowAssertion(selections, read, gen.cRows)
        println("E3 $label selection, all 8 rows: ${(System.nanoTime() - started) / 1_000_000_000} s")
        for (f in failures) println("E3 $label ROW ASSERTION FAILED: $f")
        assertTrue(failures.isEmpty(), "the cosine rows' row assertion: $failures")

        val results = selections.mapValues { (row, s) -> E3Targeted.stability(CosRowsGenerator.stratum(row), s) }
        val stops = ArrayList<Throwable>()
        for ((row, r) in results) {
            try {
                E3Strata.report(r ?: error("${CosRowsGenerator.stratum(row)}: nothing taken"))
            } catch (e: Throwable) {
                stops += e
            }
            println("E3 ${CosRowsGenerator.stratum(row)} summary: draws ${selections.getValue(row).draws}; kept ${selections.getValue(row).taken.values.single().size}; stable ${r?.verdicts?.count { it.stable }} of ${r?.verdicts?.size}")
        }
        println("E3 $label total: ${(System.nanoTime() - started) / 1_000_000_000} s")
        stops.firstOrNull()?.let { throw it }
    }

    /** The three checks of the class comment, each failure naming its row; empty when all hold. */
    private fun rowAssertion(selections: Map<Int, E3Select.Selection>, read: Map<String, Set<Int>>, cRows: List<Int>): List<String> {
        val failures = ArrayList<String>()
        val union = HashSet<Int>()
        for ((row, s) in selections) {
            val kept = s.taken.values.single()
            if (kept.size < E3Targeted.N) failures += "row $row: ${kept.size} of ${E3Targeted.N} kept after ${s.draws} draws (cap ${E3Targeted.CAP})"
            val notOwn = kept.filter { row !in read[it.case.id].orEmpty() }.map { it.case.id }
            if (notOwn.isNotEmpty()) failures += "row $row: kept cases that did not read row $row: $notOwn"
            val rowsRead = kept.flatMap { read[it.case.id].orEmpty() }.toSet()
            union += rowsRead
            println("E3 ${CosRowsGenerator.stratum(row)} rows: kept ${kept.size}; own row read by ${kept.size - notOwn.size}; (c) rows read ${rowsRead.filter { it in cRows }.sorted()}")
        }
        val seen = union.filter { it in cRows }.toSortedSet()
        println("E3 COS group: (c) rows read by all kept cases $seen")
        if (seen != CosRowsGenerator.ROWS.toSortedSet()) {
            failures += "the group's (c) rows read are $seen, not ${CosRowsGenerator.ROWS}: missing ${CosRowsGenerator.ROWS - seen}, extra ${seen - CosRowsGenerator.ROWS.toSet()}"
        }
        return failures
    }

    /** The table row of a latitude as the engine reads it: `ilat` as `RoutingParamCollector.java:314` parses it. */
    private fun row(lat: Double) = ((lat + 90.0) * 1_000_000.0 + 0.5).toInt() / 100_000

    /**
     * `RoutingEngineF0Test.port`'s calls (Forager's, as [com.zynergylabs.forager.brouter.oracle.e3.E3CaseRun]'s unlogged
     * regime) with [libm] in the [RoutingContext]; only the rows [libm] records are read from it. What routing throws is
     * caught by `doRun`, as there.
     */
    private fun portRun(c: E3Case, libm: LibmSeam, base: File = OracleCalls.oracleDir) {
        val collector = RoutingParamCollector()
        val waypoints = collector.getWayPointList("${c.startLon},${c.startLat}|${c.endLon},${c.endLat}")
        val rc = RoutingContext(libm)
        val params = mapOf(
            "profile" to File(base, "profiles2/${c.profile}.brf").absolutePath,
            "alternativeidx" to "0",
            "profile:use_dynamic_range" to "0",
        )
        collector.setParams(rc, waypoints, params)
        rc.memoryclass = c.memoryclass
        val engine = RoutingEngine(FileSystem.SYSTEM, c.segments.toOkioPath(), waypoints, rc, RoutingEngine.BROUTER_ENGINEMODE_ROUTING)
        engine.quite = true
        engine.doRun(c.maxRunningTimeMillis)
    }
}
