package com.zynergylabs.forager.brouter.oracle.e3

import com.zynergylabs.forager.brouter.util.MsunLibm
import com.zynergylabs.forager.brouter.util.OracleLibm
import java.io.File
import java.util.Random
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow

/**
 * LB's generator (Phase 1 report, E4: "start and end in the (c) rows inside the tiles"; RECORD -77 item 4).
 *
 * **The (c) rows, as step 2b set them:** the rows of `CheapRuler`'s 1,800-row scale table where the production table
 * (msun's `cos`, [MsunLibm]) differs from the oracle's ([OracleLibm]); `CheapProductionRecordTest` pins them at 13. Row r is
 * the band of `ilat` from r × 100,000 to (r + 1) × 100,000 (`CheapRuler.java:57`), 0.1° of latitude. The rows inside the
 * captured tiles are those whose band lies in some tile.
 *
 * **A draw:** a start node drawn uniformly from every node in those bands, over all the tiles (node-weighted, as R draws);
 * a distance as R's (100 m × 1000^u); a target at that distance east or west of the start, at a latitude drawn uniformly in
 * the start's band; the end is the node nearest the target ([RGenerator.nearest]). Rejected when there is none, when it is
 * the start, or when it lies outside the start's band. Both ends are the nodes' own positions, so both are in the band.
 */
class LBGenerator(private val segments: File, private val seed: Long) : AutoCloseable {
    private val gen = RGenerator(segments, seed)
    private val tiles = segments.listFiles { f -> f.name.endsWith(".rd5") }!!.sortedBy { it.name }.map { TileNodes(it) }

    /** Every (c) row of the table: production against the oracle, `kx` or `ky` differing. */
    val cRows: List<Int> = (0 until 1800).filter { r ->
        val p = MsunLibm.scales(r)
        val o = OracleLibm.scales(r)
        p[0].toRawBits() != o[0].toRawBits() || p[1].toRawBits() != o[1].toRawBits()
    }

    /** The (c) rows whose band lies in some tile, each with the node ids in its band, over all tiles. */
    val bands: Map<Int, LongArray> = cRows.associateWith { r -> nodesIn(r) }.filterValues { it.isNotEmpty() }

    val bandCounts: Map<Int, Int> get() = bands.mapValues { it.value.size }

    private val rows = bands.keys.sorted()
    private val cumulative = LongArray(rows.size).also { c ->
        var s = 0L
        for (i in rows.indices) { s += bands.getValue(rows[i]).size; c[i] = s }
    }

    private fun nodesIn(row: Int): LongArray {
        val lo = row * 100_000
        val hi = lo + 100_000
        val out = ArrayList<Long>()
        for (t in tiles) {
            val d = t.divisor
            val tileLo = t.latDeg0 * 1_000_000
            if (lo < tileLo || hi > tileLo + 5_000_000) continue // the band must lie in the tile
            val j0 = (lo.toLong() * d / 1_000_000).toInt()
            val j1 = ((hi - 1).toLong() * d / 1_000_000).toInt()
            for (i in 0 until t.cellsPerSide) for (j in j0..j1) {
                if (!t.holds(t.lonDeg0 * d + i, j)) continue
                for (id in t.cell(t.lonDeg0 * d + i, j)) if (TileNodes.ilat(id) in lo until hi) out += id
            }
        }
        return out.toLongArray()
    }

    fun rowOf(id: Long) = TileNodes.ilat(id) / 100_000

    fun draws(prefix: String, profile: String): Iterator<E3Select.Draw> = iterator {
        val rnd = Random(seed)
        var k = 0
        while (true) { // ends: the selector stops drawing at its cap
            k++
            when (val c = candidate(rnd)) {
                is String -> yield(E3Select.Draw(k, null, c))
                else -> {
                    val e = c as DoubleArray
                    yield(E3Select.Draw(k, E3Case("%s-%04d".format(prefix, k), segments, e[0], e[1], e[2], e[3], profile), null))
                }
            }
        }
    }

    private fun candidate(rnd: Random): Any {
        val total = cumulative.last()
        val k = (rnd.nextDouble() * total).toLong().coerceAtMost(total - 1)
        val b = cumulative.indexOfFirst { it > k }
        val row = rows[b]
        val start = bands.getValue(row)[(k - (if (b == 0) 0L else cumulative[b - 1])).toInt()]
        val d = 100.0 * 1000.0.pow(rnd.nextDouble())
        val east = rnd.nextBoolean()
        val tIlat = row * 100_000 + rnd.nextDouble() * 100_000
        val sLat = gen.lat(start)
        val sLon = gen.lon(start)
        val m = 6_371_000.0 * PI / 180.0
        val tLon = sLon + (if (east) 1 else -1) * d / (m * cos(Math.toRadians(sLat)))
        val end = gen.nearest(tIlat / 1e6 - 90.0, tLon) ?: return "no node in the target's 3 x 3 cells, or the target is outside every tile"
        if (end == start) return "the end's node is the start"
        if (rowOf(end) != row) return "the end's node is outside the start's band"
        return doubleArrayOf(E3Case.six(sLat), E3Case.six(sLon), E3Case.six(gen.lat(end)), E3Case.six(gen.lon(end)))
    }

    override fun close() {
        gen.close()
        tiles.forEach { it.close() }
    }
}
