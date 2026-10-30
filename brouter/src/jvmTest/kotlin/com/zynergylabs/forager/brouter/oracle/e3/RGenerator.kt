package com.zynergylabs.forager.brouter.oracle.e3

import java.io.File
import java.util.Random
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

/**
 * Stratum R's generator (Phase 1 report, E4): air distance log-uniform from 100 m to 100 km, within one tile or across a
 * 5° line; start and end drawn on the tiles' own nodes ([TileNodes]), each moved a random 0 to 100 m and rounded to six
 * decimals. Cases are kept in generation order; nothing the port does takes part in choosing them.
 *
 * One candidate, all draws from one `java.util.Random(seed)` (its algorithm is fixed by its own specification):
 * 1. a tile, uniform over [tiles]; a start node, uniform over that tile's nodes;
 * 2. a distance `100 m × 1000^u` and a bearing, uniform; the target point that far from the start node;
 * 3. the end node: the node nearest the target in the target's cell and its eight neighbours, in whichever tile holds
 *    them; none, or a target outside every tile, rejects the candidate;
 * 4. each end moved by `100 m × v` on a uniform bearing, rounded to six decimals; an end moved outside every tile
 *    rejects the candidate.
 * Distances use a sphere of radius 6,371 km with longitude scaled by the cosine of latitude. They choose points only;
 * nothing asserts them.
 *
 * MP64's, ML5's and PM5's generator is this one (E4: "R's generator, `memoryclass` 64, 20 to 100 km in `W75_N40`"): [only]
 * keeps one tile, so both ends are drawn in it, and the distance is `minMetres × ratio^u`, `20 km × 5^u` for them. The
 * defaults are R's, and R's sequence is pinned by `E3RSequenceTest`.
 */
class RGenerator(
    segments: File,
    private val seed: Long,
    only: String? = null,
    private val minMetres: Double = 100.0,
    private val ratio: Double = 1000.0,
) {
    private val tiles: List<TileNodes> = segments.listFiles { f -> f.name.endsWith(".rd5") && (only == null || f.name == only) }!!
        .sortedBy { it.name }.map { TileNodes(it) }.also { require(it.isNotEmpty()) { "no tile ${only ?: "*.rd5"} in $segments" } }

    /** Per tile, the cumulative node count over its cells in (lon, lat) order: decoded once, for the uniform draw. */
    private val cumulative: List<LongArray> = tiles.map { t ->
        val side = t.cellsPerSide
        val c = LongArray(side * side)
        var sum = 0L
        for (i in 0 until side) for (j in 0 until side) {
            sum += t.cell(t.lonDeg0 * t.divisor + i, t.latDeg0 * t.divisor + j).size
            c[i * side + j] = sum
        }
        c
    }

    val nodeCounts: Map<String, Long> get() = tiles.indices.associate { tiles[it].file.name to cumulative[it].last() }

    class Generated(val cases: List<E3Case>, val candidates: Int, val rejected: Map<String, Int>)

    fun generate(stratum: String, profile: String, n: Int, maxCandidates: Int, segments: File): Generated {
        val rnd = Random(seed)
        val cases = ArrayList<E3Case>()
        val rejected = LinkedHashMap<String, Int>()
        var candidates = 0
        while (cases.size < n && candidates < maxCandidates) { // ends: candidates rises by one each pass
            candidates++
            val why = candidate(rnd)
            if (why is String) {
                rejected.merge(why, 1, Int::plus)
                continue
            }
            val e = why as DoubleArray
            cases += E3Case("%s-%s-%03d".format(stratum, profile, cases.size + 1), segments, e[0], e[1], e[2], e[3], profile)
        }
        return Generated(cases, candidates, rejected)
    }

    /**
     * The candidate sequence for a targeted stratum ([E3Select], RECORD -51), endless, in generation order: draw `k` is
     * `<prefix>-%04d` with its number, at [memoryclass]. Its draws are [generate]'s, one per pass.
     */
    fun draws(prefix: String, profile: String, memoryclass: Int, segments: File): Iterator<E3Select.Draw> = iterator {
        val rnd = Random(seed)
        var k = 0
        while (true) { // ends: the caller takes at most its cap
            k++
            when (val c = candidate(rnd)) {
                is String -> yield(E3Select.Draw(k, null, c))
                else -> {
                    val e = c as DoubleArray
                    val id = "%s-%04d".format(prefix, k)
                    yield(E3Select.Draw(k, E3Case(id, segments, e[0], e[1], e[2], e[3], profile, memoryclass), null))
                }
            }
        }
    }

    /** One candidate: the four rounded coordinates (startLat, startLon, endLat, endLon), or why it was rejected. */
    private fun candidate(rnd: Random): Any {
        val t = rnd.nextInt(tiles.size)
        val total = cumulative[t].last()
        val k = (rnd.nextDouble() * total).toLong().coerceAtMost(total - 1)
        val start = pick(t, k)
        val d = minMetres * ratio.pow(rnd.nextDouble())
        val (tLat, tLon) = offset(lat(start), lon(start), d, 2 * PI * rnd.nextDouble())
        val end = nearest(tLat, tLon) ?: return "no node in the target's 3 x 3 cells, or the target is outside every tile"
        val (sLat, sLon) = offset(lat(start), lon(start), 100.0 * rnd.nextDouble(), 2 * PI * rnd.nextDouble())
        val (eLat, eLon) = offset(lat(end), lon(end), 100.0 * rnd.nextDouble(), 2 * PI * rnd.nextDouble())
        val out = doubleArrayOf(E3Case.six(sLat), E3Case.six(sLon), E3Case.six(eLat), E3Case.six(eLon))
        if (tileOf(out[0], out[1]) == null || tileOf(out[2], out[3]) == null) return "an end moved outside every tile"
        return out
    }

    private fun pick(t: Int, k: Long): Long {
        val c = cumulative[t]
        var lo = 0
        var hi = c.size - 1
        while (lo < hi) { // ends: hi - lo halves each pass; the first cell whose cumulative count exceeds k
            val mid = (lo + hi) / 2
            if (c[mid] > k) hi = mid else lo = mid + 1
        }
        val tile = tiles[t]
        val side = tile.cellsPerSide
        val ids = tile.cell(tile.lonDeg0 * tile.divisor + lo / side, tile.latDeg0 * tile.divisor + lo % side)
        return ids[(k - (if (lo == 0) 0L else c[lo - 1])).toInt()]
    }

    private fun nearest(lat: Double, lon: Double): Long? {
        val home = tileOf(lat, lon) ?: return null
        val lonIdx = ((lon + 180) * home.divisor).toInt()
        val latIdx = ((lat + 90) * home.divisor).toInt()
        var best: Long? = null
        var bestD = Double.MAX_VALUE
        for (di in -1..1) for (dj in -1..1) {
            val tile = tiles.firstOrNull { it.divisor == home.divisor && it.holds(lonIdx + di, latIdx + dj) } ?: continue
            for (id in tile.cell(lonIdx + di, latIdx + dj)) {
                val dy = lat(id) - lat
                val dx = (lon(id) - lon) * cos(Math.toRadians(lat))
                val dd = dx * dx + dy * dy
                if (dd < bestD) { bestD = dd; best = id }
            }
        }
        return best
    }

    private fun tileOf(lat: Double, lon: Double): TileNodes? = tiles.firstOrNull {
        it.holds(((lon + 180) * it.divisor).toInt(), ((lat + 90) * it.divisor).toInt())
    }

    private fun offset(lat: Double, lon: Double, metres: Double, bearing: Double): Pair<Double, Double> {
        val m = 6_371_000.0 * PI / 180.0 // metres per degree of latitude
        return (lat + metres * cos(bearing) / m) to (lon + metres * sin(bearing) / (m * cos(Math.toRadians(lat))))
    }

    private fun lat(id: Long) = TileNodes.ilat(id) / 1e6 - 90.0

    private fun lon(id: Long) = TileNodes.ilon(id) / 1e6 - 180.0

    fun close() = tiles.forEach { it.close() }
}
