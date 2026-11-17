package com.zynergylabs.forager.brouter.oracle.e3

import com.zynergylabs.forager.brouter.util.MsunLibm
import com.zynergylabs.forager.brouter.util.OracleLibm
import java.io.File
import java.util.Random
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow

/**
 * The cosine rows' generator (session 76; s72 §3.3 and §4.5): [LBGenerator]'s draw with the start drawn from one named
 * row only, one `java.util.Random` sequence per row, seeded from [SEEDS]. A class of its own beside [LBGenerator], which is
 * not changed (Forager's "new capability is a new function, class, or path").
 *
 * **The rows:** [ROWS], the 8 (c) rows outside the four pinned tiles (s72 §2.1), in the five tiles of
 * `TestTiles.cosRows` (release brouter-cos-tiles-2026-11-17). Row r is the band of `ilat` from r × 100,000 to
 * (r + 1) × 100,000, as in [LBGenerator].
 *
 * **A draw for row r:** a start node drawn uniformly from the nodes in r's band (the band must lie in one tile, as
 * [LBGenerator.nodesIn] requires); then exactly [LBGenerator]'s rest: a distance of 100 m × 1000^u, a target east or
 * west at that distance, at a latitude drawn uniformly in r's band, the end the node nearest the target
 * ([RGenerator.nearest]). Rejected when there is none, when it is the start, or when it lies outside r's band. The start
 * index takes one `nextDouble`, as [LBGenerator]'s does over its cumulative counts, so each draw takes four values.
 */
class CosRowsGenerator(private val segments: File) : AutoCloseable {
    /** Only [RGenerator.nearest], [RGenerator.lat] and [RGenerator.lon] are used; its own seed draws nothing here. */
    private val gen = RGenerator(segments, 0L)
    private val tiles = segments.listFiles { f -> f.name.endsWith(".rd5") }!!.sortedBy { it.name }.map { TileNodes(it) }

    /** Every (c) row of the table, as [LBGenerator.cRows]: production against the oracle, `kx` or `ky` differing. */
    val cRows: List<Int> = (0 until 1800).filter { r ->
        val p = MsunLibm.scales(r)
        val o = OracleLibm.scales(r)
        p[0].toRawBits() != o[0].toRawBits() || p[1].toRawBits() != o[1].toRawBits()
    }

    /** Each of [ROWS] with the node ids in its band, over all the tiles. */
    val bands: Map<Int, LongArray> = ROWS.associateWith { r -> nodesIn(r) }

    val bandCounts: Map<Int, Int> get() = bands.mapValues { it.value.size }

    /** [LBGenerator]'s band scan, for one row. */
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

    /** Row [row]'s draws, its own sequence from [SEEDS]; ids `<prefix>-0001` on. */
    fun draws(row: Int, prefix: String, profile: String): Iterator<E3Select.Draw> {
        val nodes = bands.getValue(row)
        require(nodes.isNotEmpty()) { "row $row: no node in its band in ${segments.name}" }
        return iterator {
            val rnd = Random(SEEDS.getValue(row))
            var k = 0
            while (true) { // ends: the selector stops drawing at its cap
                k++
                when (val c = candidate(rnd, row, nodes)) {
                    is String -> yield(E3Select.Draw(k, null, c))
                    else -> {
                        val e = c as DoubleArray
                        yield(E3Select.Draw(k, E3Case("%s-%04d".format(prefix, k), segments, e[0], e[1], e[2], e[3], profile), null))
                    }
                }
            }
        }
    }

    private fun candidate(rnd: Random, row: Int, nodes: LongArray): Any {
        val total = nodes.size.toLong()
        val start = nodes[(rnd.nextDouble() * total).toLong().coerceAtMost(total - 1).toInt()]
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

    companion object {
        /** The 8 rows (s72 §2.1), ascending. */
        val ROWS = listOf(474, 488, 502, 663, 675, 1046, 1053, 1079)

        /**
         * One seed per row, fixed here and pushed before any run (session 76's design commit): LB's 20261207
         * (`E3LBTest`) plus the row, the seeds s72 §3.3's scratch run used.
         */
        val SEEDS: Map<Int, Long> = ROWS.associateWith { 20261207L + it }

        /** The id prefix and stratum name of row [row]'s cases. */
        fun stratum(row: Int) = "COS$row"
    }
}
