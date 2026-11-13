package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.oracle.OracleCalls
import com.zynergylabs.forager.brouter.oracle.e3.DLGenerator
import com.zynergylabs.forager.brouter.oracle.e3.E3CaseRun
import com.zynergylabs.forager.brouter.oracle.e3.E3Expected
import com.zynergylabs.forager.brouter.oracle.e3.strata.E3DLTest
import com.zynergylabs.forager.brouter.oracle.e3.strata.E3Targeted
import com.zynergylabs.forager.brouter.tiles.TestTiles
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Step 8a (RECORD -90, -111; the step 8 report's §2): strata DL-lat and DL-lon, 59 cases each. The cases are the draws of
 * 1b's [DLGenerator] with [E3DLTest]'s seeds, in draw order, that 1b's selection took: the draws whose ids are the
 * stratum's rows in `expected-1b.tsv`, read within 1b's cap of [E3Targeted.CAP] draws. Each runs through the port's
 * [RoutingEngine] and the oracle's in this JVM with Forager's calls ([RoutingEngineF0Test.port]); the oracle's run is
 * first held to its row, then the port is compared with it as `E3Outcome` (kind, nodes, cost, distance, the position
 * digest and the heights digest). Mismatches are counted and named by case id and field; only digests and counts are
 * printed (E1).
 */
class RoutingEngineDLTest {
    @Test
    fun dlLatRoutesThroughThePortAsThroughTheOracle() = run(DLGenerator.Line.LAT40, E3DLTest.SEED_LAT)

    @Test
    fun dlLonRoutesThroughThePortAsThroughTheOracle() = run(DLGenerator.Line.LON70, E3DLTest.SEED_LON)

    private fun run(line: DLGenerator.Line, seed: Long) {
        val stratum = line.stratum
        val rows = E3Expected.read(javaClass.getResource(E3Expected.RESOURCE)!!.readText()).rows
            .filter { it.stratum == stratum }.associateBy { it.id }
        assertEquals(59, rows.size, "$stratum's rows in expected-1b")
        val mismatches = ArrayList<String>()
        val seen = HashSet<String>()
        val gen = DLGenerator(TestTiles.segments, line, seed)
        try {
            val draws = gen.draws(E3Targeted.PROFILE, TestTiles.segments)
            var n = 0
            while (seen.size < rows.size && n < E3Targeted.CAP) {
                n++
                val c = draws.next().case ?: continue
                val row = rows[c.id] ?: continue
                seen += c.id
                assertEquals("exact", row.compare, "${c.id}: the row is held exactly")
                val o = E3CaseRun.run(c, OracleCalls.oracleDir).outcome
                assertEquals(row.tupleDigest, E3Expected.fnv1aText(o.toField()), "${c.id}: the oracle in this JVM against expected-1b")
                val p = RoutingEngineF0Test.port(c)
                val differ = listOfNotNull(
                    "kind".takeIf { o.kind != p.kind },
                    "nodes".takeIf { o.nodes != p.nodes },
                    "cost".takeIf { o.cost != p.cost },
                    "distance".takeIf { o.distance != p.distance },
                    "node digest".takeIf { o.nodeDigest != p.nodeDigest },
                    "heights digest".takeIf { o.heightDigest != p.heightDigest },
                )
                if (differ.isNotEmpty()) mismatches += "${c.id}: $differ (oracle ${o.toField()}; port ${p.toField()})"
            }
            println("8a $stratum: ${seen.size} cases within $n draws; ${mismatches.size} mismatches")
        } finally {
            gen.close()
        }
        assertEquals(rows.keys, seen, "$stratum's cases are expected-1b's rows, within 1b's cap")
        mismatches.take(20).forEach { println("8a $stratum mismatch $it") }
        assertTrue(mismatches.isEmpty(), "8a $stratum: ${mismatches.size} mismatches; first ${mismatches.take(5)}")
    }
}
