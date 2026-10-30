package com.zynergylabs.forager.brouter.mapaccess

import com.zynergylabs.forager.brouter.tiles.TestTiles
import java.io.File
import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Item I, step 5b: every micro-cache of each captured tile decoded by both engines through `OsmFile.createMicroCache`
 * (`DirectWeaver`), with a digest per cache of nodes, links, elevations, way descriptions and turn restrictions.
 *
 * Each cache is decoded four times on each engine:
 * - **known:** every node of the cache already in the map as a hollow node, as when its neighbours were loaded first.
 *   Every decoded node is then held by the test, so the text covers all of them, isolated ones included; it also adds
 *   each link's geometry, its link holders, and every turn the node's restrictions decide;
 * - **fresh, in cleanup modes 0, 1 and 2** (`RoutingEngine.java:1881`): an empty map, as for the first cache loaded.
 *   The text is the map's counters and every node reachable from the hollow nodes the cache left, so peninsula cleanup
 *   shows as the nodes it cut off.
 */
class GraphDecodeTest {
    @Test
    fun w75n40() = decodeTile(File(TestTiles.segments, "W75_N40.rd5"))

    @Test
    fun w75n35() = decodeTile(File(TestTiles.segments, "W75_N35.rd5"))

    @Test
    fun w70n40() = decodeTile(File(TestTiles.segments, "W70_N40.rd5"))

    @Test
    fun e170s50() = decodeTile(File(TestTiles.segments, "E170_S50.rd5"))

    @Test
    fun f0Fixture() = decodeTile(File(TestTiles.f0, "W75_N40.rd5"))

    private fun decodeTile(file: File) {
        val oracle = GraphEngines.OracleDecoder()
        val port = GraphEngines.PortDecoder()
        val digest = MessageDigest.getInstance("SHA-256")
        val counts = LongArray(GraphTranscript.Count.entries.size)
        var caches = 0
        var cleanupCuts = 0L
        val started = System.nanoTime()

        GraphEngines.forEachCache(file) { square, c ->
            caches++
            fun compare(scenario: String, o: GraphTranscript<*, *>, p: GraphTranscript<*, *>) {
                val difference = GraphTranscript.firstDifference(o.text(), p.text())
                if (difference != null) fail("${file.parentFile.name}/${file.name} $c, $scenario: $difference")
                digest.update("$scenario\n".toByteArray())
                digest.update(o.text().toByteArray())
            }

            // Known: every node of the cache in the map first.
            val (om, os) = oracle.decode(square, c, seeded = true, mode = 0)
            val (pm, ps) = port.decode(square, c, seeded = true, mode = 0)
            assertTrue(os.none { it.isHollow }, "$c: an id from MicroCache2 was not decoded by the oracle's DirectWeaver")
            val ot = GraphTranscript(OracleGraph)
            val pt = GraphTranscript(PortGraph)
            os.forEach { ot.node(it, holders = true) }
            ps.forEach { pt.node(it, holders = true) }
            ot.mapState(om).forEach { ot.node(it, holders = false) }
            pt.mapState(pm).forEach { pt.node(it, holders = false) }
            compare("known", ot, pt)
            for (i in counts.indices) counts[i] += ot.counts[i]

            // Fresh, in each cleanup mode.
            var mode0Reached = 0L
            for (mode in 0..2) {
                val (om2, _) = oracle.decode(square, c, seeded = false, mode = mode)
                val (pm2, _) = port.decode(square, c, seeded = false, mode = mode)
                val ot2 = GraphTranscript(OracleGraph)
                val pt2 = GraphTranscript(PortGraph)
                ot2.reachable(ot2.mapState(om2), holders = false)
                pt2.reachable(pt2.mapState(pm2), holders = false)
                compare("fresh mode $mode", ot2, pt2)
                val reached = ot2.counts[GraphTranscript.Count.NODES.ordinal]
                if (mode == 0) mode0Reached = reached else cleanupCuts += mode0Reached - reached
            }
        }

        val names = GraphTranscript.Count.entries.joinToString(", ") { "${it.name.lowercase()} ${counts[it.ordinal]}" }
        println(
            "5b decode ${file.parentFile.name}/${file.name}: $caches caches, oracle emptyNonVirgin returns " +
                "${oracle.emptyNonVirginReturns}; known: $names; nodes cut by cleanup modes 1 and 2: $cleanupCuts; " +
                "digest ${digest.digest().joinToString("") { "%02x".format(it) }}; ${(System.nanoTime() - started) / 1_000_000} ms",
        )
        assertTrue(caches > 0, "${file.name}: no cache decoded")
        assertEquals(caches * 4, oracle.emptyNonVirginReturns, "${file.name}: every oracle decode returns emptyNonVirgin")
    }
}
