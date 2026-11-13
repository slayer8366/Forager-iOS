package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.oracle.OracleCalls
import com.zynergylabs.forager.brouter.oracle.e3.E3CaseRun
import com.zynergylabs.forager.brouter.oracle.e3.E3Expected
import com.zynergylabs.forager.brouter.oracle.e3.E3Outcome
import com.zynergylabs.forager.brouter.oracle.e3.RGenerator
import com.zynergylabs.forager.brouter.oracle.e3.strata.E3R
import com.zynergylabs.forager.brouter.oracle.e3.strata.E3RProfilesTest
import com.zynergylabs.forager.brouter.oracle.e3.strata.E3RTest
import com.zynergylabs.forager.brouter.tiles.TestTiles
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Step 8a (RECORD -90, -111; the step 8 report's §2): stratum R, 59 cases on each of E4's 26 profiles, each case taken
 * from [RGenerator] with the profile's own seed as 1b's [E3R] builds it, through the port's [RoutingEngine] and the
 * oracle's, both in this JVM with Forager's calls ([RoutingEngineF0Test.port]; E3's unlogged regime). The oracle's run is
 * first held to `expected-1b.tsv`'s row for the case. On the 20 StdModel profiles the port is compared with the oracle
 * as [E3Outcome] (kind, nodes, cost, distance, the position digest and the heights digest); on the six whose profile
 * names a `---model:` (the car profiles' Kinematic models, -89) the port's outcome is the named refusal of that model.
 * Mismatches are counted and named by case id and field; only digests and counts are printed (E1).
 */
class RoutingEngineRTest {
    @Test
    fun rRoutesThroughThePortAsThroughTheOracle() {
        val rows = E3Expected.read(javaClass.getResource(E3Expected.RESOURCE)!!.readText()).rows
            .filter { it.stratum == "R" }.associateBy { it.id }
        val seeds = linkedMapOf(E3RTest.PROFILE to E3RTest.SEED) + E3RProfilesTest.SEEDS
        assertEquals(26, seeds.size, "R's profiles (E4)")
        val mismatches = ArrayList<String>()
        val counted = LinkedHashMap<String, Int>()
        val ids = HashSet<String>()
        for ((profile, seed) in seeds) {
            val model = File(OracleCalls.oracleDir, "profiles2/$profile.brf").readLines()
                .firstOrNull { it.startsWith("---model:") }?.removePrefix("---model:")?.trim()
            val gen = RGenerator(TestTiles.segments, seed)
            val g = try {
                gen.generate("R", profile, E3R.N, 20 * E3R.N, TestTiles.segments)
            } finally {
                gen.close()
            }
            assertEquals(E3R.N, g.cases.size, "R-$profile is short")
            for (c in g.cases) {
                ids += c.id
                val row = rows[c.id] ?: error("${c.id}: no row in expected-1b")
                assertEquals("exact", row.compare, "${c.id}: R's row is held exactly")
                val o = E3CaseRun.run(c, OracleCalls.oracleDir).outcome
                assertEquals(row.tupleDigest, E3Expected.fnv1aText(o.toField()), "${c.id}: the oracle in this JVM against expected-1b")
                val p = RoutingEngineF0Test.port(c)
                if (model != null) {
                    counted.merge("refused", 1, Int::plus)
                    if (p.kind != "threw:unsupported: path model $model") mismatches += "${c.id}: refusal (port ${p.kind})"
                    continue
                }
                counted.merge(if (o.kind == "route") "route" else "error", 1, Int::plus)
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
        }
        assertEquals(rows.keys, ids, "R's cases are expected-1b's R rows")
        println("8a R: ${ids.size} cases; $counted; ${mismatches.size} mismatches")
        mismatches.take(20).forEach { println("8a R mismatch $it") }
        assertTrue(mismatches.isEmpty(), "8a R: ${mismatches.size} mismatches; first ${mismatches.take(5)}")
    }
}
