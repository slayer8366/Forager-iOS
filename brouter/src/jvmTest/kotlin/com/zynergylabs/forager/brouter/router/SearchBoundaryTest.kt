package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.mapaccess.OsmNode
import com.zynergylabs.forager.brouter.util.OracleLibm
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Step 6: `SearchBoundary` against the oracle's, on seeded positions. Java's bounds are private, so they are read
 * through behaviour: `isInBoundary` on points placed at and around each bound and around the radius, and
 * `getBoundaryDistance` in all four directions, with an undefined direction's exception compared by class and
 * message. `getFileName` on the same positions, both hemispheres. Forager never reaches this class (only a field at
 * `RoutingEngine.java:88`); it is on T2's port list. The port's distance runs on [OracleLibm] (step 2b).
 */
class SearchBoundaryTest {
    private val step = 5_000_000

    private fun jn(n: OsmNode) = btools.mapaccess.OsmNode(n.ilon, n.ilat)

    /** A position near a 5-degree corner (so the bounds' edges are reached) or anywhere, inside BRouter's range. */
    private fun pos(rnd: Random): OsmNode = if (rnd.nextBoolean()) {
        OsmNode(rnd.nextInt(0, 360_000_001), rnd.nextInt(0, 180_000_001))
    } else {
        OsmNode(rnd.nextInt(0, 73) * step + rnd.nextInt(-3, 4), rnd.nextInt(0, 37) * step + rnd.nextInt(-3, 4))
    }

    /** Points on, just inside and just outside each of the inner and outer bounds' edges, and near the centre. */
    private fun probes(c: OsmNode, rnd: Random): List<OsmNode> {
        val lon = (c.ilon / step) * step
        val lat = (c.ilat / step) * step
        val edgesLon = listOf(lon - step, lon + 2 * step, lon - 1_000_000, lon + 6_000_000)
        val edgesLat = listOf(lat - step, lat + 2 * step, lat - 1_000_000, lat + 6_000_000)
        val out = ArrayList<OsmNode>()
        for (e in edgesLon) for (d in -1..1) out += OsmNode(e + d, lat + rnd.nextInt(-step, 2 * step))
        for (e in edgesLat) for (d in -1..1) out += OsmNode(lon + rnd.nextInt(-step, 2 * step), e + d)
        repeat(4) { out += OsmNode(c.ilon + rnd.nextInt(-20_000, 20_001), c.ilat + rnd.nextInt(-20_000, 20_001)) }
        return out
    }

    @Test
    fun searchBoundaryMatchesTheOracle() {
        val rnd = Random(20261111L)
        var cases = 0
        val seen = HashMap<String, Int>()
        repeat(3000) { i ->
            val c = pos(rnd)
            val radius = when (rnd.nextInt(4)) { 0 -> 0; 1 -> -rnd.nextInt(1, 1000); else -> rnd.nextInt(1, 30_000) }
            val direction = rnd.nextInt(-1, 6)
            val o = btools.router.SearchBoundary(jn(c), radius, direction)
            val p = SearchBoundary(c, radius, direction, OracleLibm)
            assertEquals(btools.router.SearchBoundary.getFileName(jn(c)), SearchBoundary.getFileName(c), "case $i: getFileName")
            for ((k, q) in probes(c, rnd).withIndex()) {
                for (cost in intArrayOf(0, 1 + rnd.nextInt(100_000))) {
                    val ov = o.isInBoundary(jn(q), cost)
                    assertEquals(ov, p.isInBoundary(q, cost), "case $i probe $k cost $cost radius $radius: isInBoundary")
                    val mode = if (radius > 0) "radius" else if (cost == 0) "outer" else "inner"
                    seen.merge("$mode $ov", 1, Int::plus)
                    cases++
                }
                val od = runCatching { o.getBoundaryDistance(jn(q)) }
                val pd = runCatching { p.getBoundaryDistance(q) }
                assertEquals(od.exceptionOrNull()?.javaClass, pd.exceptionOrNull()?.javaClass, "case $i probe $k direction $direction: exception")
                assertEquals(od.exceptionOrNull()?.message, pd.exceptionOrNull()?.message, "case $i probe $k direction $direction: message")
                assertEquals(od.getOrNull(), pd.getOrNull(), "case $i probe $k direction $direction: getBoundaryDistance")
                seen.merge(if (od.isSuccess) "direction $direction" else "undefined direction", 1, Int::plus)
            }
        }
        println("SearchBoundaryTest: $cases isInBoundary cases; ${seen.toSortedMap()}")
        // The sample must reach both answers in every mode and every direction, or it could not fail a wrong bound.
        for (m in listOf("radius", "outer", "inner")) for (b in listOf(true, false)) {
            assertTrue((seen["$m $b"] ?: 0) > 0, "no case of $m $b")
        }
        for (d in 0..3) assertTrue((seen["direction $d"] ?: 0) > 0, "no case of direction $d")
        assertTrue((seen["undefined direction"] ?: 0) > 0, "no undefined direction")
    }
}
