package com.zynergylabs.forager.brouter.mapaccess

import com.zynergylabs.forager.brouter.codec.DataBuffers
import com.zynergylabs.forager.brouter.expressions.ExpressionsEngines
import com.zynergylabs.forager.brouter.tiles.TestTiles
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Item I, step 5b: `OsmNodesMap.collectOutreachers` on decoded regions, under option (i) (RECORD -24), where its count
 * must be exact. It iterates `hmap.values()` (`OsmNodesMap.java:181`), so the count and the visit ids it leaves depend on
 * the map's order. Beside it, what `RoutingEngine` calls with it (`RoutingEngine.java:2008-2026`): `canEscape` on
 * nodes after the collection, `clearTemp`, and `isInMemoryBounds`.
 *
 * A region is a 3 by 3 block of micro-caches, loaded one by one into one map in row order, as `NodesCache` loads a
 * search's neighbourhood. Its centre is the largest cache of the tile's first degree square with data. Each region is
 * loaded in cleanup modes 0, 1 and 2.
 */
class CollectOutreachersTest {
    private val tiles = listOf(
        File(TestTiles.segments, "W75_N40.rd5"),
        File(TestTiles.segments, "W70_N40.rd5"),
        File(TestTiles.segments, "E170_S50.rd5"),
        File(TestTiles.f0, "W75_N40.rd5"),
    )

    /** The `collectOutreachers` branch with no destination (`OsmNodesMap.java:197` not taken). */
    @Test
    fun collectOutreachersWithoutADestination() {
        var regions = 0
        val escapes = IntArray(2)
        forEachRegion { where, o, p ->
            regions++
            val ot = GraphTranscript(OracleGraph)
            val pt = GraphTranscript(PortGraph)
            o.map.collectOutreachers()
            p.map.collectOutreachers(logicLibm)
            val oh = ot.mapState(o.map)
            val ph = pt.mapState(p.map)
            ot.reachable(oh, holders = false)
            pt.reachable(ph, holders = false)

            // canEscape on every 37th node reached, as RoutingEngine.java:2016 calls it after the collection.
            val oNodes = reachableNodes(OracleGraph, oh)
            val pNodes = reachableNodes(PortGraph, ph)
            for (i in oNodes.indices step 37) {
                val oe = o.map.canEscape(oNodes[i])
                val pe = if (i < pNodes.size) p.map.canEscape(pNodes[i]) else null
                escapes[if (oe) 1 else 0]++
                ot.line("E $i $oe ${o.map.nodesCreated} ${o.map.lastVisitID}")
                pt.line("E $i $pe ${p.map.nodesCreated} ${p.map.lastVisitID}")
            }
            o.map.clearTemp()
            p.map.clearTemp()

            // isInMemoryBounds over both arguments, with maxmem set as NodesCache sets it for 64 MB.
            o.map.maxmem = 2L * 64 * 1024 * 1024 / 3
            p.map.maxmem = o.map.maxmem
            for (npaths in intArrayOf(0, 1000, 20000, 200000)) for (extend in booleanArrayOf(false, true, false)) {
                ot.line("B $npaths $extend ${o.map.isInMemoryBounds(npaths, extend)}")
                pt.line("B $npaths $extend ${p.map.isInMemoryBounds(npaths, extend)}")
            }
            compare(where, ot, pt)
        }
        println("5b collectOutreachers without destination: $regions regions, canEscape false/true ${escapes[0]}/${escapes[1]}")
        assertTrue(regions == tiles.size * 3, "regions: $regions")
    }

    /** The branch with a destination and a cost bound (`OsmNodesMap.java:197-202`): far nodes vanish. */
    @Test
    fun collectOutreachersWithADestinationVanishesFarNodes() {
        var regions = 0
        var vanished = 0L
        val escapes = IntArray(2)
        forEachRegion { where, o, p ->
            regions++
            // The destination is the region's first hollow node by position; the bound is the median distance to it.
            val oh = OracleGraph.hollowNodes(o.map).sortedWith(compareBy({ it.ilon }, { it.ilat }))
            val ph = PortGraph.hollowNodes(p.map).sortedWith(compareBy({ it.ilon }, { it.ilat }))
            val oNodes = reachableNodes(OracleGraph, oh)
            val pNodes = reachableNodes(PortGraph, ph)
            val distances = oNodes.map { it.calcDistance(oh[0]) }.sorted()
            o.map.destination = oh[0]
            p.map.destination = ph.getOrNull(0)
            o.map.currentPathCost = 0
            p.map.currentPathCost = 0
            o.map.currentMaxCost = distances[distances.size / 2]
            p.map.currentMaxCost = o.map.currentMaxCost

            o.map.collectOutreachers()
            p.map.collectOutreachers(logicLibm)
            val ot = GraphTranscript(OracleGraph)
            val pt = GraphTranscript(PortGraph)
            ot.reachable(ot.mapState(o.map), holders = false)
            pt.reachable(pt.mapState(p.map), holders = false)
            vanished += oNodes.size - ot.counts[GraphTranscript.Count.NODES.ordinal]

            // canEscape on every 37th node reached before the collection: vanished ones may now be cut off.
            for (i in oNodes.indices step 37) {
                val oe = o.map.canEscape(oNodes[i])
                val pe = if (i < pNodes.size) p.map.canEscape(pNodes[i]) else null
                escapes[if (oe) 1 else 0]++
                ot.line("E $i $oe ${o.map.nodesCreated} ${o.map.lastVisitID}")
                pt.line("E $i $pe ${p.map.nodesCreated} ${p.map.lastVisitID}")
            }
            o.map.clearTemp()
            p.map.clearTemp()
            ot.reachable(ot.mapState(o.map), holders = false)
            pt.reachable(pt.mapState(p.map), holders = false)
            compare(where, ot, pt)
        }
        println("5b collectOutreachers with destination: $regions regions, nodes no longer reachable after it $vanished, canEscape false/true ${escapes[0]}/${escapes[1]}")
        assertTrue(vanished > 0, "the vanish branch was never taken: the cost bound cut nothing off")
        assertTrue(escapes[0] > 0 && escapes[1] > 0, "canEscape did not answer both ways: false/true ${escapes[0]}/${escapes[1]}")
    }

    private class Loaded<M>(val map: M)

    private fun <N : Any, L : Any> reachableNodes(g: GraphAccess<N, L>, start: List<N>): List<N> {
        val seen = java.util.IdentityHashMap<N, Boolean>()
        val out = ArrayList<N>()
        val queue = ArrayDeque<N>()
        for (s in start) if (seen.put(s, true) == null) queue.add(s)
        while (queue.isNotEmpty()) {
            val n = queue.removeFirst()
            out.add(n)
            var l = g.firstLink(n)
            while (l != null) {
                val t = g.target(l, n) ?: break
                if (seen.put(t, true) == null) queue.add(t)
                l = g.next(l, n)
            }
        }
        return out
    }

    private fun compare(where: String, o: GraphTranscript<*, *>, p: GraphTranscript<*, *>) {
        val difference = GraphTranscript.firstDifference(o.text(), p.text())
        if (difference != null) fail("$where: $difference")
    }

    /** Each tile's region in each cleanup mode, loaded into one fresh map per engine. */
    private fun forEachRegion(block: (String, Loaded<btools.mapaccess.OsmNodesMap>, Loaded<OsmNodesMap>) -> Unit) {
        val oWay = ExpressionsEngines.oracle(GraphEngines.profile).way
        val pWay = ExpressionsEngines.port(GraphEngines.profile).way
        for (file in tiles) GraphEngines.withTile(file) { opf, ppf ->
            // The tile's first degree square with data, and its largest cache.
            val caches = ArrayList<Pair<GraphEngines.Square, GraphEngines.Cache>>()
            GraphEngines.forEachCacheIn(file, opf, ppf, firstSquareOnly = true) { square, c -> caches.add(square to c) }
            val (square, centre) = caches.maxBy { it.second.bytes }
            val byIdx = caches.associateBy { it.second.lonIdx to it.second.latIdx }
            val cells = (-1..1).flatMap { dy -> (-1..1).mapNotNull { dx -> byIdx[(centre.lonIdx + dx) to (centre.latIdx + dy)]?.second } }
            for (mode in 0..2) {
                val om = btools.mapaccess.OsmNodesMap().also { it.cleanupMode = mode }
                val pm = OsmNodesMap().also { it.cleanupMode = mode }
                val ob = btools.codec.DataBuffers()
                val pb = DataBuffers()
                for (c in cells) {
                    square.oracle.createMicroCache(c.lonIdx, c.latIdx, ob, oWay, null, true, om)
                    square.port.createMicroCache(c.lonIdx, c.latIdx, pb, pWay, null, true, pm)
                }
                block("${file.parentFile.name}/${file.name} around $centre, ${cells.size} caches, mode $mode", Loaded(om), Loaded(pm))
            }
        }
    }
}
