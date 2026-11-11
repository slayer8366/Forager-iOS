package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.oracle.OracleCalls
import com.zynergylabs.forager.brouter.oracle.e3.E3CaseRun
import com.zynergylabs.forager.brouter.oracle.e3.E3Expected
import com.zynergylabs.forager.brouter.oracle.e3.RGenerator
import com.zynergylabs.forager.brouter.oracle.e3.strata.E3MemoryTest
import com.zynergylabs.forager.brouter.oracle.e3.strata.E3Targeted
import com.zynergylabs.forager.brouter.tiles.TestTiles
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Step 8b (RECORD -90, -123; the step 8 report's §1): strata MP64 (59 cases, memory class 64), ML5 (59) and PM5 (55), the
 * last two one sequence at memory class 5. The cases are the draws of 1b's [RGenerator] as [E3MemoryTest] builds it (its
 * seeds, `W75_N40` only, 20 km times 5^u), in draw order, that 1b's selection took: the draws whose ids are the
 * stratum's rows in `expected-1b.tsv`, read within 1b's cap of [E3Targeted.CAP] draws. The generator's arguments are
 * repeated here; the oracle's run held to each row's digest is what shows they are 1b's. Each case runs through the
 * port's [RoutingEngine] and the oracle's in this JVM with Forager's calls ([RoutingEngineF0Test.port]), and is compared
 * as `E3Outcome` (kind, nodes, cost, distance, the position digest and the heights digest), as [RoutingEngineDLTest].
 * Only digests and counts are printed (E1).
 */
class RoutingEngineMemoryTest {
    @Test
    fun mp64RoutesThroughThePortAsThroughTheOracle() = run("MP64", "MP64", 64, E3MemoryTest.SEED_MP64, 59)

    @Test
    fun ml5RoutesThroughThePortAsThroughTheOracle() = run("ML5", "M5", 5, E3MemoryTest.SEED_M5, 59)

    @Test
    fun pm5RoutesThroughThePortAsThroughTheOracle() = run("PM5", "M5", 5, E3MemoryTest.SEED_M5, 55)

    private fun run(stratum: String, label: String, memoryclass: Int, seed: Long, count: Int) {
        val rows = E3Expected.read(javaClass.getResource(E3Expected.RESOURCE)!!.readText()).rows
            .filter { it.stratum == stratum }.associateBy { it.id }
        assertEquals(count, rows.size, "$stratum's rows in expected-1b")
        val mismatches = ArrayList<String>()
        val seen = HashSet<String>()
        val gen = RGenerator(TestTiles.segments, seed, only = E3MemoryTest.TILE, minMetres = 20_000.0, ratio = 5.0)
        try {
            val draws = gen.draws(label, E3Targeted.PROFILE, memoryclass, TestTiles.segments)
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
            println("8b $stratum: ${seen.size} cases within $n draws; ${mismatches.size} mismatches")
        } finally {
            gen.close()
        }
        assertEquals(rows.keys, seen, "$stratum's cases are expected-1b's rows, within 1b's cap")
        mismatches.take(20).forEach { println("8b $stratum mismatch $it") }
        assertTrue(mismatches.isEmpty(), "8b $stratum: ${mismatches.size} mismatches; first ${mismatches.take(5)}")
    }
}
