package com.zynergylabs.forager.brouter.oracle.e3

import java.io.File
import java.util.Random
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

/**
 * Strata DL-lat and DL-lon's generator (Phase 1 report, E4): "ends on opposite sides of 40°N (`W75_N40`, `W75_N35`) and of
 * 70°W (`W75_N40`, `W70_N40`)". E4 fixes the line, the two tiles, the start and end drawn on the tiles' own nodes
 * ([TileNodes]), the move of 0 to 100 m and the six decimals. The rest is this generator's, fixed before any run of it
 * (RECORD -47) and reported as a call: R's distance law, and a start drawn near enough the line to cross it.
 *
 * One candidate, all draws from one `java.util.Random(seed)`:
 * 1. the start's side, uniform over the two tiles; the end is in the other;
 * 2. an air distance `100 m × 1000^u` (R's law, [RGenerator]);
 * 3. a start node, uniform over the start tile's nodes in the rows of cells (columns, for a line of longitude) that lie
 *    within that distance of the line, counted with the narrowest cell either tile has across the line;
 * 4. a bearing, uniform; the target point that far from the start node. A target not inside the other tile rejects the
 *    candidate;
 * 5. the end node: the node nearest the target in the target's cell and its eight neighbours, in the other tile only;
 *    none rejects the candidate;
 * 6. each end moved by `100 m × v` on a uniform bearing and rounded to six decimals. A start no longer in the start tile,
 *    or an end no longer in the other, rejects the candidate: every case kept has its ends in the two tiles, one each.
 *
 * Which side a point is on is which of the two tiles holds its cell. The tiles share the line as an edge and span the same
 * range along it, so "in the other tile" is "across the line, inside the tiles". Distances use a sphere of radius
 * 6,371 km with longitude scaled by the cosine of latitude, as [RGenerator]'s; they choose points only.
 */
class DLGenerator(segments: File, private val line: Line, private val seed: Long) {
    /** The line and its two tiles. [degree] is in the engine's frame: latitude + 90, or longitude + 180. */
    enum class Line(val stratum: String, val byLatitude: Boolean, val degree: Int, val below: String, val above: String) {
        LAT40("DL-lat", true, 40 + 90, "W75_N35.rd5", "W75_N40.rd5"),
        LON70("DL-lon", false, -70 + 180, "W75_N40.rd5", "W70_N40.rd5"),
    }

    private val sides: List<TileNodes> = listOf(line.below, line.above).map { TileNodes(File(segments, it)) }
    private val divisor = sides[0].divisor.also { d ->
        require(sides.all { it.divisor == d }) { "${line.stratum}: the two tiles' divisors differ" }
    }
    private val lineIdx = line.degree * divisor

    /** The narrowest cell across the line, in metres: a degree of latitude, or of longitude at the tiles' poleward edge. */
    private val cellMetres: Double = run {
        val degreeMetres = 6_371_000.0 * PI / 180.0
        if (line.byLatitude) {
            degreeMetres / divisor
        } else {
            val poleward = sides.maxOf { maxOf(kotlin.math.abs(it.latDeg0 - 90), kotlin.math.abs(it.latDeg0 - 90 + 5)) }
            degreeMetres * cos(Math.toRadians(poleward.toDouble())) / divisor
        }
    }

    /** Rows needed for the longest distance, 100 km; never more than the tile has. */
    private val maxRows = minOf(sides[0].cellsPerSide, ceil(100_000.0 / cellMetres).toInt() + 1)

    /** Per side, per row from the line: the cumulative node count over the row's cells, in the row's own order. */
    private val rowCells: List<List<LongArray>> = sides.indices.map { s -> (0 until maxRows).map { r -> rowCumulative(s, r) } }

    /** Per side, the cumulative node count over rows 0, 1, … from the line. */
    private val rowCum: List<LongArray> = rowCells.map { rows ->
        var sum = 0L
        LongArray(rows.size) { r -> sum += rows[r].lastOrNull() ?: 0L; sum }
    }

    val nodeCountsNearTheLine: Map<String, Long> get() = sides.indices.associate { sides[it].file.name to rowCum[it].last() }

    class Generated(val cases: List<E3Case>, val candidates: Int, val rejected: Map<String, Int>)

    fun generate(profile: String, n: Int, maxCandidates: Int, segments: File): Generated {
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
            cases += E3Case("%s-%03d".format(line.stratum, cases.size + 1), segments, e[0], e[1], e[2], e[3], profile)
        }
        return Generated(cases, candidates, rejected)
    }

    /** The side ([sides] index) whose tile holds the point, or null when neither does. */
    fun sideOf(lat: Double, lon: Double): Int? = sides.indices.firstOrNull { holds(it, lat, lon) }

    private fun candidate(rnd: Random): Any {
        val s = rnd.nextInt(2)
        val d = 100.0 * 1000.0.pow(rnd.nextDouble())
        val rows = minOf(maxRows, ceil(d / cellMetres).toInt())
        val total = rowCum[s][rows - 1]
        if (total == 0L) return "no node within the distance of the line"
        val k = (rnd.nextDouble() * total).toLong().coerceAtMost(total - 1)
        val start = pick(s, k)
        val (tLat, tLon) = offset(lat(start), lon(start), d, 2 * PI * rnd.nextDouble())
        val other = 1 - s
        if (!holds(other, tLat, tLon)) return "the target is not across the line inside the other tile"
        val end = nearest(other, tLat, tLon) ?: return "no node in the target's 3 x 3 cells in the other tile"
        val (sLat, sLon) = offset(lat(start), lon(start), 100.0 * rnd.nextDouble(), 2 * PI * rnd.nextDouble())
        val (eLat, eLon) = offset(lat(end), lon(end), 100.0 * rnd.nextDouble(), 2 * PI * rnd.nextDouble())
        val out = doubleArrayOf(E3Case.six(sLat), E3Case.six(sLon), E3Case.six(eLat), E3Case.six(eLon))
        if (!holds(s, out[0], out[1]) || !holds(other, out[2], out[3])) return "an end moved off its side"
        return out
    }

    /** The absolute cell (lonIdx, latIdx) of row [r] from the line, position [i] along it, on side [s]. */
    private fun cellAt(s: Int, r: Int, i: Int): Pair<Int, Int> {
        val t = sides[s]
        val across = if (s == 1) lineIdx + r else lineIdx - 1 - r // side 1 is the tile above the line
        return if (line.byLatitude) (t.lonDeg0 * divisor + i) to across else across to (t.latDeg0 * divisor + i)
    }

    private fun rowCumulative(s: Int, r: Int): LongArray {
        var sum = 0L
        return LongArray(sides[s].cellsPerSide) { i ->
            val (lo, la) = cellAt(s, r, i)
            sum += sides[s].cell(lo, la).size
            sum
        }
    }

    private fun pick(s: Int, k: Long): Long {
        val r = firstAbove(rowCum[s], k)
        val inRow = k - (if (r == 0) 0L else rowCum[s][r - 1])
        val cells = rowCells[s][r]
        val i = firstAbove(cells, inRow)
        val (lo, la) = cellAt(s, r, i)
        return sides[s].cell(lo, la)[(inRow - (if (i == 0) 0L else cells[i - 1])).toInt()]
    }

    /** The first index whose cumulative count exceeds [k]. */
    private fun firstAbove(c: LongArray, k: Long): Int {
        var lo = 0
        var hi = c.size - 1
        while (lo < hi) { // ends: hi - lo halves each pass
            val mid = (lo + hi) / 2
            if (c[mid] > k) hi = mid else lo = mid + 1
        }
        return lo
    }

    private fun nearest(s: Int, lat: Double, lon: Double): Long? {
        val tile = sides[s]
        val lonIdx = ((lon + 180) * divisor).toInt()
        val latIdx = ((lat + 90) * divisor).toInt()
        var best: Long? = null
        var bestD = Double.MAX_VALUE
        for (di in -1..1) for (dj in -1..1) {
            if (!tile.holds(lonIdx + di, latIdx + dj)) continue
            for (id in tile.cell(lonIdx + di, latIdx + dj)) {
                val dy = lat(id) - lat
                val dx = (lon(id) - lon) * cos(Math.toRadians(lat))
                val dd = dx * dx + dy * dy
                if (dd < bestD) { bestD = dd; best = id }
            }
        }
        return best
    }

    private fun holds(s: Int, lat: Double, lon: Double): Boolean =
        sides[s].holds(((lon + 180) * divisor).toInt(), ((lat + 90) * divisor).toInt())

    private fun offset(lat: Double, lon: Double, metres: Double, bearing: Double): Pair<Double, Double> {
        val m = 6_371_000.0 * PI / 180.0 // metres per degree of latitude
        return (lat + metres * cos(bearing) / m) to (lon + metres * sin(bearing) / (m * cos(Math.toRadians(lat))))
    }

    private fun lat(id: Long) = TileNodes.ilat(id) / 1e6 - 90.0

    private fun lon(id: Long) = TileNodes.ilon(id) / 1e6 - 180.0

    fun close() = sides.forEach { it.close() }
}
