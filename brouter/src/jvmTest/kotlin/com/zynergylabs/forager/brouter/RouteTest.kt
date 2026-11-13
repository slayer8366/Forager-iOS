package com.zynergylabs.forager.brouter

import com.zynergylabs.forager.brouter.oracle.OracleCalls
import com.zynergylabs.forager.brouter.oracle.e3.E3Case
import com.zynergylabs.forager.brouter.oracle.e3.E3CaseRun
import com.zynergylabs.forager.brouter.oracle.e3.E3Expected
import com.zynergylabs.forager.brouter.oracle.e3.E3Outcome
import com.zynergylabs.forager.brouter.oracle.e3.RGenerator
import com.zynergylabs.forager.brouter.oracle.e3.strata.E3R
import com.zynergylabs.forager.brouter.oracle.e3.strata.E3RTest
import com.zynergylabs.forager.brouter.router.RoutingEngineF0Test
import com.zynergylabs.forager.brouter.tiles.TestTiles
import com.zynergylabs.forager.brouter.util.OracleLibm
import okio.Path.Companion.toOkioPath
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Step 8d (RECORD -90, -111, -122, -127; the step 8 report's §9.6): the public entry, [route] and [RouteCancel], with
 * Forager's calls (`BRouterTrailRouter.kt:142-174` at Forager `947f1be2`): `hiking-mountain`, `alternativeidx=0`,
 * `profile:use_dynamic_range=0`, the engine's message as the failure and the found track's nodes, heights and distance as
 * the route. Compared with the oracle (`E3CaseRun`, as [RoutingEngineF0Test]) on F0's two cases:
 * - **logic run** (the internal overload with [OracleLibm], -24): `Found`, node count (all three arrays), distance, the
 *   node digest over `(iLon, iLat)` and the height digest over `sElev`, each equal to the oracle's. Cost is not compared:
 *   `Found` does not carry it and Forager does not read it.
 * - **production run** (the public [route], `MsunLibm`): a `Found` of 2 or more nodes is asserted; equality with the oracle
 *   is recorded, not asserted (-24: production runs record any difference).
 * - **a terminate before the run** (FK10's plan): `Failed` on the port, a message on the oracle, the same kind, both
 *   matching Java's watchdog text (`RoutingEngine.java:1976`; the seconds are wall time), terminated on both.
 * - **R's first 10 routed cases** (RECORD -129, decision 1), in R's own order (`RoutingEngineRTest`'s:
 *   its first profile, `hiking-mountain`, StdModel, from its own seed): the logic run compared as F0's, with the oracle's
 *   heights asserted not all "no height" (F0's routes have none, -62), so the copy of the heights into `Found` is compared.
 * - **a terminate before the run through the public [route]** (-129, decision 2), compared as the internal one: the call
 *   Forager's cancel makes.
 * - **the flag** with no route: Java's `isTerminated()` (`:2432-2438`), not the oracle.
 * Only digests and counts are printed (E1).
 */
class RouteTest {
    @Test
    fun f0RoutesThroughRouteAsThroughTheOracle() {
        val rows = E3Expected.read(javaClass.getResource(E3Expected.RESOURCE)!!.readText()).rows
            .filter { it.stratum == "F0" }.associateBy { it.id }
        for (c in RoutingEngineF0Test.CASES) {
            val o = E3CaseRun.run(c, OracleCalls.oracleDir).outcome
            assertEquals(rows.getValue(c.id).tupleDigest, E3Expected.fnv1aText(o.toField()), "${c.id}: the oracle against expected-1b")
            assertEquals("route", o.kind, "${c.id}: the oracle routes")
            val p = assertIs<RouteOutcome.Found>(route(request(c), RouteCancel(), OracleLibm), "${c.id}: Found")
            val digests = Digests.of(p)
            println("8d route ${c.id}: oracle nodes ${o.nodes} distance ${o.distance}; port nodes ${p.iLat.size} distance ${p.distance} " +
                "node digest equal ${o.nodeDigest == digests.nodes}, heights digest equal ${o.heightDigest == digests.heights}")
            assertEquals(o.nodes, p.iLat.size, "${c.id}: nodes (iLat)")
            assertEquals(o.nodes, p.iLon.size, "${c.id}: nodes (iLon)")
            assertEquals(o.nodes, p.sElev.size, "${c.id}: a height per node")
            assertEquals(o.distance, p.distance, "${c.id}: distance")
            assertEquals(o.nodeDigest, digests.nodes, "${c.id}: node digest")
            assertEquals(o.heightDigest, digests.heights, "${c.id}: heights digest")
        }
    }

    @Test
    fun rFirstTenRoutedCasesRouteThroughRouteAsThroughTheOracle() {
        val rows = E3Expected.read(javaClass.getResource(E3Expected.RESOURCE)!!.readText()).rows
            .filter { it.stratum == "R" }.associateBy { it.id }
        val gen = RGenerator(TestTiles.segments, E3RTest.SEED)
        val cases = try {
            gen.generate("R", E3RTest.PROFILE, E3R.N, 20 * E3R.N, TestTiles.segments).cases
        } finally {
            gen.close()
        }
        var compared = 0
        for (c in cases) {
            if (compared == 10) break
            val run = E3CaseRun.run(c, OracleCalls.oracleDir)
            val o = run.outcome
            assertEquals(rows.getValue(c.id).tupleDigest, E3Expected.fnv1aText(o.toField()), "${c.id}: the oracle against expected-1b")
            if (o.kind != "route") continue
            compared++
            val byTile = assertNotNull(run.heightsByTile, "${c.id}: the oracle's heights")
            val withHeight = byTile.values.sumOf { it[0] }
            assertTrue(withHeight > 0, "${c.id}: the oracle's heights are not all \"no height\"")
            val p = assertIs<RouteOutcome.Found>(route(request(c), RouteCancel(), OracleLibm), "${c.id}: Found")
            val digests = Digests.of(p)
            println("8d route ${c.id}: oracle nodes ${o.nodes}, with a height $withHeight; port nodes ${p.iLat.size}; " +
                "distance equal ${o.distance == p.distance}, node digest equal ${o.nodeDigest == digests.nodes}, " +
                "heights digest equal ${o.heightDigest == digests.heights}")
            assertEquals(o.nodes, p.iLat.size, "${c.id}: nodes (iLat)")
            assertEquals(o.nodes, p.iLon.size, "${c.id}: nodes (iLon)")
            assertEquals(o.nodes, p.sElev.size, "${c.id}: a height per node")
            assertEquals(o.distance, p.distance, "${c.id}: distance")
            assertEquals(o.nodeDigest, digests.nodes, "${c.id}: node digest")
            assertEquals(o.heightDigest, digests.heights, "${c.id}: heights digest")
        }
        assertEquals(10, compared, "R's first 10 routed cases")
    }

    @Test
    fun f0ThroughThePublicRouteIsRecorded() {
        for (c in RoutingEngineF0Test.CASES) {
            val o = E3CaseRun.run(c, OracleCalls.oracleDir).outcome
            val p = assertIs<RouteOutcome.Found>(route(request(c), RouteCancel()), "${c.id}: Found")
            val digests = Digests.of(p)
            // Recorded, not asserted (-24): the production libm may move a route.
            println("8d production ${c.id}: nodes equal ${o.nodes == p.iLat.size}, distance equal ${o.distance == p.distance}, " +
                "node digest equal ${o.nodeDigest == digests.nodes}, heights digest equal ${o.heightDigest == digests.heights}")
            assertTrue(p.iLat.size >= 2, "${c.id}: a route of 2 or more nodes")
        }
    }

    @Test
    fun aTerminateBeforeTheRunFailsAsTheOracle() {
        for (c in RoutingEngineF0Test.CASES) {
            val oracle = oracleTerminatedFirst(c)
            val cancel = RouteCancel()
            cancel.terminate()
            val p = assertIs<RouteOutcome.Failed>(route(request(c), cancel, OracleLibm), "${c.id}: Failed")
            val o = assertNotNull(oracle.first, "${c.id}: the oracle's message")
            println("8d terminate ${c.id}: oracle ${E3Outcome.kindOf(o)}; port ${E3Outcome.kindOf(p.message)}")
            assertEquals(E3Outcome.kindOf(o), E3Outcome.kindOf(p.message), "${c.id}: kind")
            assertTrue(WATCHDOG.matches(o), "${c.id}: the oracle's text")
            assertTrue(WATCHDOG.matches(p.message), "${c.id}: the port's text")
            assertTrue(oracle.second, "${c.id}: the oracle is terminated")
            assertTrue(cancel.isTerminated, "${c.id}: the port is terminated")
        }
    }

    @Test
    fun aTerminateBeforeThePublicRouteFailsAsTheOracle() {
        for (c in RoutingEngineF0Test.CASES) {
            val oracle = oracleTerminatedFirst(c)
            val cancel = RouteCancel()
            cancel.terminate()
            val p = assertIs<RouteOutcome.Failed>(route(request(c), cancel), "${c.id}: Failed")
            val o = assertNotNull(oracle.first, "${c.id}: the oracle's message")
            println("8d public terminate ${c.id}: oracle ${E3Outcome.kindOf(o)}; port ${E3Outcome.kindOf(p.message)}")
            assertEquals(E3Outcome.kindOf(o), E3Outcome.kindOf(p.message), "${c.id}: kind")
            assertTrue(WATCHDOG.matches(o), "${c.id}: the oracle's text")
            assertTrue(WATCHDOG.matches(p.message), "${c.id}: the port's text")
            assertTrue(oracle.second, "${c.id}: the oracle is terminated")
            assertTrue(cancel.isTerminated, "${c.id}: the port is terminated")
        }
    }

    /**
     * Step 8d's leftover (the planner's ruling, session 39): the constructor's exception passes through the public [route],
     * as Forager's caller relies on (`BRouterTrailRouter.kt:122-131` at Forager 947f1be2 catches what the constructor
     * throws). A profile path that does not exist, on F0's first case: the port's [route] and the oracle's constructor
     * with the same call each throw; the same exception class and the same message, the path (and its file name) read as
     * `*`. Before either is compared, the port's call is asserted to throw rather than return.
     */
    @Test
    fun aMissingProfileThrowsThroughRouteAsTheOracle() {
        val c = RoutingEngineF0Test.CASES.first()
        val missing = File(OracleCalls.oracleDir, "profiles2/no-such-profile-s39.brf")
        assertFalse(missing.exists(), "the profile does not exist")
        val oracle = runCatching { oracleBuilt(c, missing) }.exceptionOrNull()
        val port = runCatching { route(requestWith(c, missing), RouteCancel()) }
        val pe = assertNotNull(port.exceptionOrNull(), "the port's route() throws (it returned ${port.getOrNull()?.let { it::class.simpleName }})")
        val oe = assertNotNull(oracle, "the oracle's constructor throws")
        fun masked(m: String?) = m?.replace(missing.absolutePath, "*")?.replace(missing.name, "*")
        println("8d missing profile: oracle ${oe::class.qualifiedName} \"${masked(oe.message)}\"; port ${pe::class.qualifiedName} \"${masked(pe.message)}\"")
        assertEquals(oe::class.qualifiedName, pe::class.qualifiedName, "the exception's class")
        assertEquals(masked(oe.message), masked(pe.message), "the message, the path read as *")
    }

    @Test
    fun theCancelReportsItsFlag() {
        val cancel = RouteCancel()
        assertFalse(cancel.isTerminated, "before terminate()")
        cancel.terminate()
        assertTrue(cancel.isTerminated, "after terminate()")
    }

    private class Digests(val nodes: String, val heights: String) {
        companion object {
            fun of(p: RouteOutcome.Found) = Digests(
                E3Outcome.fnv1a(p.iLon.indices.map { p.iLon[it] to p.iLat[it] }),
                E3Outcome.fnv1aShorts(p.sElev.toList()),
            )
        }
    }

    private companion object {
        /** `RoutingEngine.java:1976`: kind 10's text; the seconds are wall time. */
        val WATCHDOG = Regex("operation killed by thread-priority-watchdog after \\d+ seconds")

        /** The request as Forager's `BRouterTrailRouter.buildEngine` makes it (`:142-160`), on the case's tiles and limit. */
        fun request(c: E3Case) = requestWith(c, File(OracleCalls.oracleDir, "profiles2/${c.profile}.brf"))

        /** [request] with the profile file given. */
        fun requestWith(c: E3Case, profile: File) = RouteRequest(
            startLon = c.startLon,
            startLat = c.startLat,
            endLon = c.endLon,
            endLat = c.endLat,
            routeData = c.segments.toOkioPath(),
            profile = profile.toOkioPath(),
            params = mapOf("alternativeidx" to "0", "profile:use_dynamic_range" to "0"),
            maxRunningTimeMillis = c.maxRunningTimeMillis,
        )

        /** The oracle's constructor with Forager's calls ([OracleCalls]) on the profile file given; not run. */
        fun oracleBuilt(c: E3Case, profile: File): btools.router.RoutingEngine {
            val collector = btools.router.RoutingParamCollector()
            val waypoints = collector.getWayPointList("${c.startLon},${c.startLat}|${c.endLon},${c.endLat}")
            val rc = btools.router.RoutingContext()
            collector.setParams(
                rc, waypoints,
                hashMapOf("profile" to profile.absolutePath, "alternativeidx" to "0", "profile:use_dynamic_range" to "0"),
            )
            return btools.router.RoutingEngine(null, null, c.segments, waypoints, rc, btools.router.RoutingEngine.BROUTER_ENGINEMODE_ROUTING)
        }

        /** The oracle with Forager's calls ([OracleCalls]), `terminate()` before `doRun`: its message and `isTerminated()`. */
        fun oracleTerminatedFirst(c: E3Case): Pair<String?, Boolean> {
            val collector = btools.router.RoutingParamCollector()
            val waypoints = collector.getWayPointList("${c.startLon},${c.startLat}|${c.endLon},${c.endLat}")
            val rc = btools.router.RoutingContext()
            collector.setParams(
                rc, waypoints,
                hashMapOf(
                    "profile" to File(OracleCalls.oracleDir, "profiles2/${c.profile}.brf").absolutePath,
                    "alternativeidx" to "0",
                    "profile:use_dynamic_range" to "0",
                ),
            )
            val engine = btools.router.RoutingEngine(null, null, c.segments, waypoints, rc, btools.router.RoutingEngine.BROUTER_ENGINEMODE_ROUTING)
            engine.quite = true
            engine.terminate()
            engine.doRun(c.maxRunningTimeMillis)
            return engine.getErrorMessage() to engine.isTerminated()
        }
    }
}
