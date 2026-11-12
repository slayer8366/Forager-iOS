package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.oracle.OracleCalls
import com.zynergylabs.forager.brouter.oracle.e3.E3Case
import com.zynergylabs.forager.brouter.oracle.e3.E3CaseRun
import com.zynergylabs.forager.brouter.oracle.e3.E3Expected
import com.zynergylabs.forager.brouter.oracle.e3.strata.E3F0hTest
import com.zynergylabs.forager.brouter.tiles.TestTiles
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Step 9 (session 41): stratum F0h, Forager's eight routes on its heighted square ([TestTiles.f0h]), through the port, as
 * [RoutingEngineF0Test] runs F0. The cases are [E3F0hTest]'s ([E3F0hTest.ENDS], its profile), in its order. Per case the
 * oracle in this JVM ([E3CaseRun]) is held to its row's whole-tuple digest in `expected-1b.tsv`, then port and oracle are
 * compared on the six fields of `E3Outcome`: kind, nodes, cost, distance, the position digest, the heights digest.
 * Only counts and field names are printed (E1).
 */
class RoutingEngineF0hTest {
    @Test
    fun f0hRoutesThroughThePortAsThroughTheOracle() {
        val rows = E3Expected.read(javaClass.getResource(E3Expected.RESOURCE)!!.readText()).rows
            .filter { it.stratum == "F0h" }.associateBy { it.id }
        val cases = E3F0hTest.ENDS.map { (name, e) -> E3Case("F0h-$name", TestTiles.f0h, e[0], e[1], e[2], e[3], E3F0hTest.PROFILE) }
        assertEquals(8, cases.size, "F0h's cases")
        assertEquals(cases.map { it.id }.toSet(), rows.keys, "F0h's rows in expected-1b")
        val mismatches = ArrayList<String>()
        for (c in cases) {
            val row = rows.getValue(c.id)
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
            if (differ.isNotEmpty()) mismatches += "${c.id}: $differ"
        }
        val kinds = rows.values.groupingBy { it.outcome.kind }.eachCount()
        println("9 F0h: ${cases.size} cases; kinds $kinds; ${mismatches.size} mismatches")
        mismatches.forEach { println("9 F0h mismatch $it") }
        assertTrue(mismatches.isEmpty(), "9 F0h: ${mismatches.size} mismatches; first ${mismatches.take(5)}")
    }
}
