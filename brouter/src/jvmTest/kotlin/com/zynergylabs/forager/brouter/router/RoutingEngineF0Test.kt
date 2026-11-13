package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.oracle.OracleCalls
import com.zynergylabs.forager.brouter.oracle.e3.E3Case
import com.zynergylabs.forager.brouter.oracle.e3.E3CaseRun
import com.zynergylabs.forager.brouter.oracle.e3.E3Expected
import com.zynergylabs.forager.brouter.oracle.e3.E3Outcome
import com.zynergylabs.forager.brouter.tiles.TestTiles
import com.zynergylabs.forager.brouter.util.OracleLibm
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Step 8a (RECORD -90, -111; the step 8 report's §2): stratum F0, Forager's own two routes, through the port's
 * [RoutingEngine] and the oracle's, both in this JVM with Forager's calls (as [E3CaseRun]'s unlogged regime). Compared
 * per case as [E3Outcome]: the kind, node count, cost, distance, and the digests over every route point's position and
 * height. The oracle's run is first held to `expected-1b.tsv`'s row for the case. Only digests and counts are printed (E1).
 */
class RoutingEngineF0Test {
    @Test
    fun f0RoutesThroughThePortAsThroughTheOracle() {
        val rows = E3Expected.read(javaClass.getResource(E3Expected.RESOURCE)!!.readText()).rows
            .filter { it.stratum == "F0" }.associateBy { it.id }
        assertEquals(CASES.map { it.id }.toSet(), rows.keys, "F0's rows in expected-1b")
        for (c in CASES) {
            val o = E3CaseRun.run(c, OracleCalls.oracleDir).outcome
            val row = rows.getValue(c.id)
            assertEquals("exact", row.compare, "${c.id}: F0's row is held exactly")
            assertEquals(row.tupleDigest, E3Expected.fnv1aText(o.toField()), "${c.id}: the oracle in this JVM against expected-1b")
            val p = port(c)
            println("8a F0 ${c.id}: oracle ${o.toField()}; port ${p.toField()}")
            assertEquals(o.kind, p.kind, "${c.id}: kind")
            assertEquals(o.nodes, p.nodes, "${c.id}: nodes")
            assertEquals(o.cost, p.cost, "${c.id}: cost")
            assertEquals(o.distance, p.distance, "${c.id}: distance")
            assertEquals(o.nodeDigest, p.nodeDigest, "${c.id}: node digest")
            assertEquals(o.heightDigest, p.heightDigest, "${c.id}: heights digest")
        }
    }

    companion object {
        /** E3F0Test's two cases: Forager's own ends (E1 allows them), `hiking-mountain`, memory class 64, no time limit. */
        val CASES = listOf(
            E3Case("F0-inside-across-the-degree-line", TestTiles.f0, 41.2742564, -73.9978932, 41.3036912, -74.0159457, "hiking-mountain"),
            E3Case("F0-inside-long", TestTiles.f0, 41.2772811, -74.0422743, 41.292043, -73.996678, "hiking-mountain"),
        )

        /** [E3CaseRun]'s unlogged attempt, with the port's classes in place of the oracle's; [base] holds `profiles2/`. */
        fun port(c: E3Case, base: File = OracleCalls.oracleDir): E3Outcome = try {
            val collector = RoutingParamCollector()
            val waypoints = collector.getWayPointList("${c.startLon},${c.startLat}|${c.endLon},${c.endLat}")
            val rc = RoutingContext(OracleLibm)
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
            val message = engine.getErrorMessage()
            val track = engine.getFoundTrack()
            val pairs = track.nodes.map { it.getILon() to it.getILat() }
            E3Outcome(
                E3Outcome.kindOf(message), pairs.size, track.cost, track.distance, E3Outcome.fnv1a(pairs),
                E3Outcome.fnv1aShorts(track.nodes.map { it.getSElev() }),
            )
        } catch (e: Exception) {
            // As E3CaseRun: the constructor parses the profile; what routing throws is caught by doRun. An Error (a stub's
            // NotImplementedError) is not caught.
            E3Outcome("threw:" + (e.message ?: e::class.simpleName), 0, -1, -1, E3Outcome.fnv1a(emptyList()), E3Outcome.fnv1aShorts(emptyList()))
        }
    }
}
