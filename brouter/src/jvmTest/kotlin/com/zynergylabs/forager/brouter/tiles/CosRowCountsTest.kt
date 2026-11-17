package com.zynergylabs.forager.brouter.tiles

import com.zynergylabs.forager.brouter.codec.DataBuffers
import com.zynergylabs.forager.brouter.mapaccess.MapaccessEngines
import com.zynergylabs.forager.brouter.mapaccess.OsmFile
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Session 75 (session 72's dispatch 5, "re-measure §2.3's counts on the release's bytes"): each of the 8 cosine rows holds,
 * in the release's tile, the number of nodes the release's own `# row` lines state. The counts are taken by the PORT's
 * reader (PhysicalFile, OsmFile, MicroCache), not by the capture's CosTileCheck (the oracle's Java) and not by TileNodes
 * or LBGenerator, so the release's figures are checked by a reader that did not write them.
 *
 * Expected counts are read from tools/tile-pins-cos.tsv (the release's pin file, byte for byte), never typed here. A row
 * is `ilat / 100_000` (LBGenerator.rowOf). Only counts are printed: no node id, `ilon`, `ilat` or coordinate.
 */
class CosRowCountsTest {
    @Test
    fun eachRowHoldsTheReleasesNodeCount() {
        val expected = expectedFromPinFile(TestTiles.cosPinFile)
        val problems = ArrayList<String>()
        for ((tile, rows) in ROWS.entries) {
            val counted = countByRow(File(TestTiles.cosRows, "$tile.rd5"), tile)
            for (row in rows) {
                val port = counted[row] ?: 0
                val release = expected.getValue(tile to row)
                println("cos rows | $tile | row $row | port reader $port | release $release")
                if (port != release) problems += "row $row in $tile: port reader $port, release $release"
            }
        }
        assertTrue(problems.isEmpty(), "rows whose node count differs from the release's # row line: $problems")
    }

    /** The release's `# row` lines, as (tile, row) to nodes. Fails unless they name exactly the 8 pairs, once each. */
    private fun expectedFromPinFile(file: File): Map<Pair<String, Int>, Int> {
        val found = LinkedHashMap<Pair<String, Int>, Int>()
        val duplicates = ArrayList<Pair<String, Int>>()
        for (line in file.readLines()) {
            if (!line.startsWith("# row\t")) continue
            val f = line.split('\t')
            val key = f[1] to f[2].toInt()
            val nodes = f[3].removePrefix("nodes=").also { require(it != f[3]) { "no nodes= field in a # row line of $key" } }.toInt()
            if (found.put(key, nodes) != null) duplicates += key
        }
        val wanted = ROWS.flatMap { (tile, rows) -> rows.map { tile to it } }.toSet()
        assertTrue(duplicates.isEmpty(), "# row lines given twice in ${file.name}: $duplicates")
        assertEquals(
            wanted.sortedBy { it.second }, found.keys.sortedBy { it.second },
            "the (tile, row) pairs of ${file.name}'s # row lines against the 8 cosine rows",
        )
        return found
    }

    /** Every node of [file], by row, through the port's reader in the engine's absolute degree frame. */
    private fun countByRow(file: File, tile: String): Map<Int, Int> {
        val m = TILE_NAME.matchEntire(tile) ?: error("not a tile name: $tile")
        val lonDeg0 = m.groupValues[2].toInt() * (if (m.groupValues[1] == "W") -1 else 1) + 180
        val latDeg0 = m.groupValues[4].toInt() * (if (m.groupValues[3] == "S") -1 else 1) + 90
        val pf = MapaccessEngines.portPhysical(file)
        val byRow = HashMap<Int, Int>()
        var total = 0L
        try {
            val div = pf.divisor
            val db = DataBuffers()
            for (lo in 0 until 5) for (la in 0 until 5) {
                val osmf = OsmFile(pf, lonDeg0 + lo, latDeg0 + la, db)
                if (!osmf.hasData()) continue
                for (i in 0 until div) for (j in 0 until div) {
                    val mc = osmf.createMicroCache((lonDeg0 + lo) * div + i, (latDeg0 + la) * div + j, db, null, null, true, null)
                        ?: continue
                    for (k in 0 until mc.getSize()) {
                        val ilat = mc.getIdForIndex(k).toInt()
                        byRow.merge(ilat / 100_000, 1, Int::plus)
                        total++
                    }
                }
            }
        } finally {
            pf.ra?.close()
        }
        println("cos rows | $tile | port reader | nodes in the tile $total")
        return byRow
    }

    private companion object {
        val TILE_NAME = Regex("([EW])(\\d+)_([NS])(\\d+)")

        /** The 8 cosine rows and the release tile each lies in (s72 §2.1). */
        val ROWS = linkedMapOf(
            "E145_S45" to listOf(474, 488),
            "E170_S40" to listOf(502),
            "E15_S25" to listOf(663, 675),
            "W95_N10" to listOf(1046),
            "W20_N15" to listOf(1053, 1079),
        )
    }
}
