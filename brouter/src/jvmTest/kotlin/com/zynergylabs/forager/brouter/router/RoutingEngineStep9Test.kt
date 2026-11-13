package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.oracle.OracleCalls
import com.zynergylabs.forager.brouter.oracle.e3.E3CaseRun
import com.zynergylabs.forager.brouter.oracle.e3.E3Expected
import com.zynergylabs.forager.brouter.oracle.e3.E3Profiles
import com.zynergylabs.forager.brouter.oracle.e3.E3Select
import com.zynergylabs.forager.brouter.oracle.e3.LBGenerator
import com.zynergylabs.forager.brouter.oracle.e3.NTGenerator
import com.zynergylabs.forager.brouter.oracle.e3.RGenerator
import com.zynergylabs.forager.brouter.oracle.e3.strata.E3Targeted
import com.zynergylabs.forager.brouter.tiles.TestTiles
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Step 9 (session 39): the strata that did not yet route through the port, each as [RoutingEngineMemoryTest] runs MP64.
 * The cases are the draws of 1b's own generator with 1b's seed and arguments, as the stratum's E3 test builds them, in
 * draw order, taken where a draw's id is one of the stratum's rows in `expected-1b.tsv`, within 1b's cap of
 * [E3Targeted.CAP] draws. The arguments are repeated here; the oracle's run in this JVM held to each row's digest is what
 * shows they are 1b's. Each case then runs through the port ([RoutingEngineF0Test.port], Forager's calls) and is compared
 * with the oracle's on the six fields of `E3Outcome` (kind, nodes, cost, distance, the position digest, the heights
 * digest). Only digests and counts are printed (E1).
 * - **NT** ([NTGenerator], seed `E3NTTest`'s 20261205, `hiking-mountain`).
 * - **LB** ([LBGenerator], seed `E3LBTest`'s 20261207, `hiking-mountain`).
 * - **CR** ([RGenerator] on all tiles with its defaults, seed `E3CRTest`'s 20261206, E's own `trekking-crossing` profile
 *   ([E3Profiles.crossing]) at memory class 64): both engines read the profile from one scratch copy of the oracle's
 *   `profiles2/` with it added, as [com.zynergylabs.forager.brouter.oracle.e3.E3Stability] writes it for each launch.
 */
class RoutingEngineStep9Test {
    @Test
    fun ntRoutesThroughThePortAsThroughTheOracle() {
        val gen = NTGenerator(TestTiles.segments, 20261205L)
        try {
            run("NT", gen.draws("NT", E3Targeted.PROFILE), OracleCalls.oracleDir)
        } finally {
            gen.close()
        }
    }

    @Test
    fun lbRoutesThroughThePortAsThroughTheOracle() {
        val gen = LBGenerator(TestTiles.segments, 20261207L)
        try {
            run("LB", gen.draws("LB", E3Targeted.PROFILE), OracleCalls.oracleDir)
        } finally {
            gen.close()
        }
    }

    @Test
    fun crRoutesThroughThePortAsThroughTheOracle() {
        val base = Files.createTempDirectory("s9-cr").toFile()
        try {
            File(OracleCalls.oracleDir, "profiles2").copyRecursively(File(base, "profiles2"))
            File(base, "profiles2/${E3Profiles.CROSSING}.brf").writeText(E3Profiles.crossing())
            val gen = RGenerator(TestTiles.segments, 20261206L)
            try {
                run("CR", gen.draws("CR", E3Profiles.CROSSING, 64, TestTiles.segments), base)
            } finally {
                gen.close()
            }
        } finally {
            base.deleteRecursively()
        }
    }

    private fun run(stratum: String, draws: Iterator<E3Select.Draw>, base: File) {
        val rows = E3Expected.read(javaClass.getResource(E3Expected.RESOURCE)!!.readText()).rows
            .filter { it.stratum == stratum }.associateBy { it.id }
        assertEquals(59, rows.size, "$stratum's rows in expected-1b")
        val mismatches = ArrayList<String>()
        val seen = HashSet<String>()
        var n = 0
        while (seen.size < rows.size && n < E3Targeted.CAP) {
            n++
            val c = draws.next().case ?: continue
            val row = rows[c.id] ?: continue
            seen += c.id
            assertEquals("exact", row.compare, "${c.id}: the row is held exactly")
            val o = E3CaseRun.run(c, base).outcome
            assertEquals(row.tupleDigest, E3Expected.fnv1aText(o.toField()), "${c.id}: the oracle in this JVM against expected-1b")
            val p = RoutingEngineF0Test.port(c, base)
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
        val kinds = rows.values.filter { it.id in seen }.groupingBy { it.outcome.kind }.eachCount()
        println("9 $stratum: ${seen.size} cases within $n draws; kinds $kinds; ${mismatches.size} mismatches")
        assertEquals(rows.keys, seen, "$stratum's cases are expected-1b's rows, within 1b's cap")
        mismatches.take(20).forEach { println("9 $stratum mismatch $it") }
        assertTrue(mismatches.isEmpty(), "9 $stratum: ${mismatches.size} mismatches; first ${mismatches.take(5)}")
    }
}
