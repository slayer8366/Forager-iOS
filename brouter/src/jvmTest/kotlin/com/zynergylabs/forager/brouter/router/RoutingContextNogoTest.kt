package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.mapaccess.MatchedWaypoint
import com.zynergylabs.forager.brouter.mapaccess.OsmNode
import com.zynergylabs.forager.brouter.util.OracleLibm
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import btools.mapaccess.MatchedWaypoint as OracleMatchedWaypoint
import btools.mapaccess.OsmNode as OracleNode
import btools.router.OsmNodeNamed as OracleNamed
import btools.router.RoutingContext as OracleContext

/**
 * Item I, step 6 piece 2: `RoutingContext`'s nogo and distance half (`RoutingContext.java:285-554`) against the
 * oracle, on seeded random points about 150 m around one centre. The port runs on the oracle's own scale table
 * ([OracleLibm]), so this compares logic; msun's table is compared with it in `util`. Waypoints are set as `RoutingEngine` sets them
 * (`setWaypoint`, not a nogo), mixed with unweighted nogo circles. Every compared double is compared by its bits.
 * Weighted nogo circles and nogo polygons are not on Forager's path: the port names them "unsupported", and the last
 * case shows it where the oracle computes a cost.
 */
class RoutingContextNogoTest {
    private val lon0 = 105_900_000
    private val lat0 = 131_200_000

    private class Pair(val p: OsmNodeNamed, val o: OracleNamed)

    private fun pair(r: Random, nogo: Boolean, weight: Double = Double.NaN): Pair {
        val lon = lon0 + r.nextInt(-2000, 2001)
        val lat = lat0 + r.nextInt(-2000, 2001)
        val p = OsmNodeNamed(OsmNode(lon, lat))
        val o = OracleNamed(OracleNode(lon, lat))
        val radius = r.nextInt(0, 120).toDouble()
        p.radius = radius
        o.radius = radius
        p.isNogo = nogo
        o.isNogo = nogo
        p.nogoWeight = weight
        o.nogoWeight = weight
        p.name = "n"
        o.name = "n"
        return Pair(p, o)
    }

    private fun contexts(nogos: List<Pair>): kotlin.Pair<RoutingContext, OracleContext> {
        val prc = RoutingContext(OracleLibm)
        val orc = OracleContext()
        if (nogos.isNotEmpty()) {
            prc.nogopoints = nogos.mapTo(ArrayList()) { it.p }
            orc.nogopoints = nogos.mapTo(ArrayList()) { it.o }
        }
        return prc to orc
    }

    private fun state(prc: RoutingContext): String =
        "nogoCost=${prc.nogoCost.toRawBits()} shortest=${prc.shortestmatch} wf=${prc.wayfraction.toRawBits()} " +
            "lon=${prc.ilonshortest} lat=${prc.ilatshortest} end=${prc.isEndpoint} " +
            "radii=${prc.nogopoints?.map { it.radius.toRawBits() }}"

    private fun state(orc: OracleContext): String =
        "nogoCost=${orc.nogoCost.toRawBits()} shortest=${orc.shortestmatch} wf=${orc.wayfraction.toRawBits()} " +
            "lon=${orc.ilonshortest} lat=${orc.ilatshortest} end=${orc.isEndpoint} " +
            "radii=${orc.nogopoints?.map { it.radius.toRawBits() }}"

    @Test
    fun calcDistanceAndTheWaypointCycleMatchTheOracle() {
        val r = Random(6_02)
        var matches = 0
        for (case in 0 until 3000) {
            val nogos = List(r.nextInt(0, 3)) { pair(r, nogo = true) }
            val (prc, orc) = contexts(nogos)
            val wp = pair(r, nogo = false)
            val endpoint = r.nextBoolean()
            val pending = if (r.nextInt(4) == 0) pair(r, nogo = false) else null
            prc.setWaypoint(wp.p, pending?.p, endpoint)
            orc.setWaypoint(wp.o, pending?.o, endpoint)
            repeat(2) { step ->
                if (step == 1) assertEquals(orc.checkPendingEndpoint(), prc.checkPendingEndpoint(), "case $case pending")
                val lon1 = lon0 + r.nextInt(-2500, 2501)
                val lat1 = lat0 + r.nextInt(-2500, 2501)
                val lon2 = if (r.nextInt(10) == 0) lon1 else lon0 + r.nextInt(-2500, 2501)
                val lat2 = if (lon2 == lon1 && r.nextBoolean()) lat1 else lat0 + r.nextInt(-2500, 2501)
                prc.nogoCost = 0.0
                orc.nogoCost = 0.0
                assertEquals(orc.calcDistance(lon1, lat1, lon2, lat2), prc.calcDistance(lon1, lat1, lon2, lat2), "case $case step $step")
                assertEquals(state(orc), state(prc), "case $case step $step")
                if (prc.shortestmatch) matches++
            }
            assertEquals(orc.nogoChecksums.toList(), prc.getNogoChecksums().toList(), "case $case checksums")
            prc.unsetWaypoint()
            orc.unsetWaypoint()
            assertEquals(state(orc), state(prc), "case $case unset")
        }
        assertTrue(matches > 500, "shortest matches seen: $matches")
    }

    @Test
    fun cleanAndRestoreNogoListMatchTheOracle() {
        val r = Random(6_03)
        for (case in 0 until 2000) {
            val nogos = List(r.nextInt(0, 5)) { pair(r, nogo = true) }
            val (prc, orc) = contexts(nogos)
            val wps = List(r.nextInt(1, 4)) { pair(r, nogo = false) }
            prc.cleanNogoList(wps.map { it.p })
            orc.cleanNogoList(wps.map { it.o })
            assertEquals(orc.nogopoints?.map { nogos.indexOfFirst { n -> n.o === it } }, prc.nogopoints?.map { nogos.indexOfFirst { n -> n.p === it } }, "case $case clean")
            prc.restoreNogoList()
            orc.restoreNogoList()
            assertEquals(orc.nogopoints?.size, prc.nogopoints?.size, "case $case restore")
        }
    }

    @Test
    fun checkMatchedWaypointAgainstNogosMatchesTheOracle() {
        val r = Random(6_04)
        var thrown = 0
        for (case in 0 until 3000) {
            val nogos = List(r.nextInt(0, 4)) { pair(r, nogo = true) }
            val (prc, orc) = contexts(nogos)
            val pm = ArrayList<MatchedWaypoint>()
            val om = ArrayList<OracleMatchedWaypoint>()
            repeat(r.nextInt(1, 6)) {
                val c = pair(r, nogo = false)
                val type = r.nextInt(1, 4).toByte()
                pm += MatchedWaypoint().apply { crosspoint = c.p; wpttype = type }
                om += OracleMatchedWaypoint().apply { crosspoint = c.o; wpttype = type }
            }
            val pe = runCatching { prc.checkMatchedWaypointAgainstNogos(pm) }.exceptionOrNull()
            val oe = runCatching { orc.checkMatchedWaypointAgainstNogos(om) }.exceptionOrNull()
            assertEquals(oe?.let { it.javaClass.name + ": " + it.message }, pe?.let { it.javaClass.name + ": " + it.message }, "case $case")
            if (oe != null) thrown++
            assertEquals(om.map { it.crosspoint.ilon }, pm.map { it.crosspoint!!.ilon }, "case $case list")
        }
        assertTrue(thrown > 20, "refusals seen: $thrown")
    }

    @Test
    fun weightedNogosAndPolygonsAreUnsupported() {
        val r = Random(6_05)
        var weighted = 0
        var polygons = 0
        for (case in 0 until 2000) {
            val usePolygon = r.nextBoolean()
            val n = pair(r, nogo = true, weight = if (usePolygon) Double.NaN else 2.0)
            val (prc, orc) = contexts(listOf(n))
            if (usePolygon) prc.nogopoints = arrayListOf(OsmNogoPolygon(true).apply { ilon = n.p.ilon; ilat = n.p.ilat; radius = n.p.radius })
            val lon1 = lon0 + r.nextInt(-2500, 2501)
            val lat1 = lat0 + r.nextInt(-2500, 2501)
            val lon2 = lon0 + r.nextInt(-2500, 2501)
            val lat2 = lat0 + r.nextInt(-2500, 2501)
            orc.calcDistance(lon1, lat1, lon2, lat2)
            val e = runCatching { prc.calcDistance(lon1, lat1, lon2, lat2) }.exceptionOrNull()
            if (orc.nogoCost != 0.0 || e != null) {
                val name = if (usePolygon) "OsmNogoPolygon.intersects" else "OsmNodeNamed.distanceWithinRadius (weighted nogo)"
                assertEquals("unsupported: $name", (e as? UnsupportedOperationException)?.message, "case $case: $e")
                if (usePolygon) polygons++ else weighted++
            }
        }
        assertTrue(weighted > 20 && polygons > 20, "weighted $weighted, polygons $polygons")
    }
}
