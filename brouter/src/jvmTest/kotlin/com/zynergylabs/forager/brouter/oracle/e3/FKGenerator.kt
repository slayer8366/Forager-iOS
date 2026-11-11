package com.zynergylabs.forager.brouter.oracle.e3

import btools.codec.TagValueValidator
import btools.codec.WaypointMatcher
import btools.router.RoutingContext
import btools.router.RoutingEngine
import btools.router.RoutingParamCollector
import com.zynergylabs.forager.brouter.oracle.OracleCalls
import java.io.Closeable
import java.io.File
import java.io.RandomAccessFile
import java.util.Random
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

/**
 * FK's generator (Phase 1 report, E4, "the failure kinds"; RECORD -79, -80), for the kinds built from the tiles: 1 to 6.
 * Kinds 7 and 8 are byte edits of a copy of E170_S50 ([cut], [flip]); kinds 9 to 11 are R's generator with a plan
 * ([E3Case.Plan]). Every draw comes from one `java.util.Random` per kind, seeded in the test; nothing the port does takes
 * part, and only kind 4 is selected by the oracle's outcome (-80). Geometry is read with the oracle's own decoder
 * ([TileNodes]); it chooses points only, and the oracle's message is the verdict.
 *
 * The draws use [TILES], the three smaller tiles: W75_N40's eight million nodes would be decoded whole for the islands and
 * the node draws, and no kind needs it. Distances are on a sphere of 6,371 km, longitude scaled by the cosine of latitude.
 *
 * - **Usable node** (an end that matches): a node drawn uniformly over a tile's nodes, the tile uniform, kept when the
 *   profile can use one of its links (the decoder's matcher calls, access type at least 2; `MicroCache2.java:159-167`).
 * - **Kinds 1 and 2, "in water, more than 250 m from any way":** a point uniform over a tile, kept when no node of its cell
 *   and the eight around it lies within [FAR_METRES]. A cell is at least 2.2 km wide in these tiles, so every node within
 *   that distance is among them. Whether the point is water is not read: the tiles hold no water [I].
 * - **Kinds 3 and 4, a ferry-only island:** the components of each tile's graph of usable links, once with
 *   `trekking-noferries` and once with `trekking` (the two differ only in `allow_ferries`). An island node lies in a
 *   `trekking-noferries` component of at most [ISLAND_MAX] nodes, every link of it inside the tile and no node of it in
 *   the tile's outermost ring of cells (a link leaving the tile may be stored at its far end only), whose `trekking`
 *   component has at least [MAINLAND_MIN] nodes: it is left only by a ferry. The mainland node is drawn from the tile's
 *   largest `trekking-noferries` component. [ISLAND_MAX] is under `MAXNODES_ISLAND_CHECK` (500, `RoutingEngine.java:50`).
 * - **Kind 5, an end on a way the profile cannot use:** a node none of whose cell's and eight neighbours' usable links
 *   (the test's profile, `hiking-mountain` since RECORD -95), geometry included, passes within [FAR_METRES] of it.
 * - **Kind 6:** a point uniform over W80_N40's square, which is not in the folder.
 */
class FKGenerator(private val segments: File) : Closeable {
    private val tiles: List<TileNodes> = TILES.map { TileNodes(File(segments, it)) }

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

    private val validators = HashMap<String, TagValueValidator>()

    /** A profile's way context, as the engine's constructor leaves it after parsing the profile. */
    private fun validator(profile: String): TagValueValidator = validators.getOrPut(profile) {
        val collector = RoutingParamCollector()
        val waypoints = collector.getWayPointList("170.5,-45.5|170.6,-45.6")
        val rc = RoutingContext()
        collector.setParams(
            rc, waypoints,
            hashMapOf(
                "profile" to File(OracleCalls.oracleDir, "profiles2/$profile.brf").absolutePath,
                "alternativeidx" to "0",
                "profile:use_dynamic_range" to "0",
            ),
        )
        RoutingEngine(null, null, segments, waypoints, rc, RoutingEngine.BROUTER_ENGINEMODE_ROUTING)
        rc.expctxWay
    }

    /** One case's four coordinates (startLat, startLon, endLat, endLon), or why the draw was rejected. */
    sealed class Drawn {
        class Ends(val e: DoubleArray) : Drawn()
        class Rejected(val why: String) : Drawn()
    }

    /** Kind [kind]'s draw, 1 to 4 or 6, with [profile] for the usable ends; kind 5 is [usableNode] and [unusableNode]. */
    fun draw(kind: Int, rnd: Random, profile: String): Drawn = when (kind) {
        1 -> water(rnd)?.let { w -> usable(rnd, profile)?.let { u -> ends(w, point(u)) } } ?: Drawn.Rejected(lastWhy)
        2 -> usable(rnd, profile)?.let { u -> water(rnd)?.let { w -> ends(point(u), w) } } ?: Drawn.Rejected(lastWhy)
        3, 4 -> island(rnd, start = kind == 3)
        6 -> usable(rnd, profile)?.let { u ->
            ends(point(u), doubleArrayOf(E3Case.six(40.0 + 5 * rnd.nextDouble()), E3Case.six(-80.0 + 5 * rnd.nextDouble())))
        } ?: Drawn.Rejected(lastWhy)
        else -> error("FKGenerator.draw draws kinds 1 to 4 and 6, not $kind")
    }

    /**
     * Kinds 7 and 8's ends, and the three extra cases': a usable node of E170_S50 whose 3 x 3 cells lie in the tile, and a
     * second, the source of a usable link drawn uniformly over those cells' usable links, not the first. Four draws.
     */
    fun local(rnd: Random, profile: String): Drawn {
        val t = 0
        check(tiles[t].file.name == "E170_S50.rd5")
        val tile = tiles[t]
        val total = cumulative[t].last()
        val k = (rnd.nextDouble() * total).toLong().coerceAtMost(total - 1)
        val id = nodeAt(t, k)
        val (ci, cj) = cellOf(tile, id)
        for (di in -1..1) for (dj in -1..1) {
            if (!tile.holds(ci + di, cj + dj)) return Drawn.Rejected("the node's 3 x 3 cells leave the tile")
        }
        val around = links(tile, ci to cj, profile, geometry = false)
        if (around.none { it[0] == id || it[1] == id }) return Drawn.Rejected("the drawn node has no link $profile can use")
        val others = around.map { it[0] as Long }.filter { it != id }
        if (others.isEmpty()) return Drawn.Rejected("no other usable link in the node's 3 x 3 cells")
        return ends(point(id), point(others[rnd.nextInt(others.size)]))
    }

    /**
     * Kind 5's draws taken separately (RECORD -82): one draw of a [profile]-usable start node, or one draw of an end node
     * with no [profile]-usable link within [FAR_METRES]. Three draws from [rnd] each.
     */
    fun usableNode(rnd: Random, profile: String): Drawn =
        usable(rnd, profile)?.let { Drawn.Ends(point(it)) } ?: Drawn.Rejected(lastWhy)

    fun unusableNode(rnd: Random, profile: String): Drawn =
        unusable(rnd, profile)?.let { Drawn.Ends(point(it)) } ?: Drawn.Rejected(lastWhy)

    private var lastWhy = ""

    private fun reject(why: String): Nothing? {
        lastWhy = why
        return null
    }

    private fun ends(s: DoubleArray, e: DoubleArray) = Drawn.Ends(doubleArrayOf(s[0], s[1], e[0], e[1]))

    private fun point(id: Long) = doubleArrayOf(E3Case.six(lat(id)), E3Case.six(lon(id)))

    private fun drawNode(rnd: Random): Pair<Int, Long> {
        val t = rnd.nextInt(tiles.size)
        val total = cumulative[t].last()
        val k = (rnd.nextDouble() * total).toLong().coerceAtMost(total - 1)
        return t to nodeAt(t, k)
    }

    /** The [k]th node of tile [t], counting its cells in (lon, lat) order. */
    private fun nodeAt(t: Int, k: Long): Long {
        val c = cumulative[t]
        var lo = 0
        var hi = c.size - 1
        while (lo < hi) { // ends: hi - lo halves each pass
            val mid = (lo + hi) / 2
            if (c[mid] > k) hi = mid else lo = mid + 1
        }
        val tile = tiles[t]
        val side = tile.cellsPerSide
        val ids = tile.cell(tile.lonDeg0 * tile.divisor + lo / side, tile.latDeg0 * tile.divisor + lo % side)
        return ids[(k - (if (lo == 0) 0L else c[lo - 1])).toInt()]
    }

    /** A usable node for [profile] (three draws from [rnd]), or null. */
    private fun usable(rnd: Random, profile: String): Long? {
        val (t, id) = drawNode(rnd)
        val tile = tiles[t]
        val found = links(tile, cellOf(tile, id), profile, geometry = false).any { it[0] == id || it[1] == id }
        return if (found) id else reject("the drawn node has no link $profile can use")
    }

    /** A water point: uniform over a tile, no node within [FAR_METRES] in its 3 x 3 cells. Three draws. */
    private fun water(rnd: Random): DoubleArray? {
        val tile = tiles[rnd.nextInt(tiles.size)]
        val lat = E3Case.six(tile.latDeg0 - 90 + 5 * rnd.nextDouble())
        val lon = E3Case.six(tile.lonDeg0 - 180 + 5 * rnd.nextDouble())
        val lonIdx = ((lon + 180) * tile.divisor).toInt()
        val latIdx = ((lat + 90) * tile.divisor).toInt()
        if (!tile.holds(lonIdx, latIdx)) return reject("the point is on the tile's edge")
        for (di in -1..1) for (dj in -1..1) {
            if (!tile.holds(lonIdx + di, latIdx + dj)) return reject("the point's 3 x 3 cells leave the tile")
            for (id in tile.cell(lonIdx + di, latIdx + dj)) {
                if (metres(lat, lon, lat(id), lon(id)) <= FAR_METRES) return reject("a node within $FAR_METRES m of the point")
            }
        }
        return doubleArrayOf(lat, lon)
    }

    /** Kind 5's end: a node with no `profile`-usable link passing within [FAR_METRES]. Three draws. */
    private fun unusable(rnd: Random, profile: String): Long? {
        val (t, id) = drawNode(rnd)
        val tile = tiles[t]
        val la = lat(id)
        val lo = lon(id)
        val (ci, cj) = cellOf(tile, id)
        for (di in -1..1) for (dj in -1..1) {
            if (!tile.holds(ci + di, cj + dj)) return reject("the node's 3 x 3 cells leave the tile")
        }
        for (link in links(tile, ci to cj, profile, geometry = true)) {
            val pts = link.drop(2).chunked(2)
            for (k in 0 until pts.size - 1) {
                if (segmentMetres(la, lo, pts[k], pts[k + 1]) <= FAR_METRES) return reject("a link $profile can use within $FAR_METRES m")
            }
        }
        return id
    }

    /** Per tile, the island nodes and the largest component's nodes, both sorted; built on first use. */
    private val islands: List<Pair<LongArray, LongArray>> by lazy { tiles.map { islandsOf(it) } }

    /** Island counts per tile, for the log: tile, island nodes, island components, mainland nodes. */
    val islandCounts: String get() = tiles.indices.joinToString("; ") { t ->
        "${tiles[t].file.name}: ${islands[t].first.size} island nodes, ${islandComponents[t]} islands, ${islands[t].second.size} mainland nodes"
    }

    private val islandComponents = IntArray(TILES.size)

    private fun island(rnd: Random, start: Boolean): Drawn {
        val t = rnd.nextInt(tiles.size)
        val (isl, main) = islands[t]
        if (isl.isEmpty()) return Drawn.Rejected("no ferry-only island in ${tiles[t].file.name}")
        val i = isl[rnd.nextInt(isl.size)]
        val m = main[rnd.nextInt(main.size)]
        return if (start) ends(point(i), point(m)) else ends(point(m), point(i))
    }

    private fun islandsOf(tile: TileNodes): Pair<LongArray, LongArray> {
        val nf = components(tile, "trekking-noferries")
        val tr = components(tile, "trekking")
        val bySize = nf.members.values.sortedWith(compareBy<List<Long>>({ -it.size }, { it.min() }))
        val mainland = bySize.first().sorted().toLongArray()
        val island = ArrayList<Long>()
        var count = 0
        for (members in bySize) {
            if (members.size > ISLAND_MAX) continue
            if (members.any { it in nf.open || onEdge(tile, it) }) continue
            val trSize = tr.sizeOf(members.first())
            if (trSize < MAINLAND_MIN) continue
            count++
            island += members
        }
        islandComponents[tiles.indexOf(tile)] = count
        return island.sorted().toLongArray() to mainland
    }

    private class Components(val root: Map<Long, Long>, val members: Map<Long, List<Long>>, val open: Set<Long>) {
        fun sizeOf(id: Long): Int = root[id]?.let { members.getValue(it).size } ?: 0
    }

    /** The tile's components of [profile]-usable links (union-find); `open` holds the nodes of any that leave the tile. */
    private fun components(tile: TileNodes, profile: String): Components {
        val parent = HashMap<Long, Long>()
        fun find(x: Long): Long {
            var r = x
            while (true) { // ends: each step climbs one level toward the root
                val p = parent.getOrPut(r) { r }
                if (p == r) break
                r = p
            }
            var y = x
            while (parent.getValue(y) != r) { // ends: path compression, toward r
                val n = parent.getValue(y)
                parent[y] = r
                y = n
            }
            return r
        }
        val leaving = HashSet<Long>()
        val side = tile.cellsPerSide
        for (i in 0 until side) for (j in 0 until side) {
            for (l in links(tile, tile.lonDeg0 * tile.divisor + i to tile.latDeg0 * tile.divisor + j, profile, false, around = 0)) {
                val from = l[0] as Long
                val to = l[1] as Long
                val a = find(from)
                val b = find(to)
                if (a != b) parent[max(a, b)] = min(a, b)
                if (!inside(tile, to)) leaving += from
            }
        }
        val root = parent.keys.toList().associateWith { find(it) }
        val members = root.entries.groupBy({ it.value }, { it.key })
        val open = HashSet<Long>()
        for (n in leaving) open += members.getValue(root.getValue(n))
        return Components(root, members, open)
    }

    /** In the tile's outermost ring of cells, where a link leaving the tile may be stored at its far end only. */
    private fun onEdge(tile: TileNodes, id: Long): Boolean {
        val (i, j) = cellOf(tile, id)
        val side = tile.cellsPerSide
        return i - tile.lonDeg0 * tile.divisor !in 1 until side - 1 || j - tile.latDeg0 * tile.divisor !in 1 until side - 1
    }

    private fun inside(tile: TileNodes, id: Long) = tile.holds(
        TileNodes.ilon(id) / (1_000_000 / tile.divisor), TileNodes.ilat(id) / (1_000_000 / tile.divisor),
    )

    private fun cellOf(tile: TileNodes, id: Long) =
        TileNodes.ilon(id) / (1_000_000 / tile.divisor) to TileNodes.ilat(id) / (1_000_000 / tile.divisor)

    /**
     * The [profile]-usable forward links decoded in the cells within [around] of [cell]: each as source id, target id,
     * then, with [geometry], the polyline's (lat, lon) pairs from source through the transfer nodes to target.
     */
    private fun links(tile: TileNodes, cell: Pair<Int, Int>, profile: String, geometry: Boolean, around: Int = 1): List<List<Any>> {
        val out = ArrayList<List<Any>>()
        var current: ArrayList<Any>? = null
        var target: Pair<Int, Int>? = null
        val matcher = object : WaypointMatcher {
            override fun start(ilonStart: Int, ilatStart: Int, ilonTarget: Int, ilatTarget: Int, useAsStartWay: Boolean): Boolean {
                val l = arrayListOf<Any>(NTGenerator.id(ilonStart, ilatStart), NTGenerator.id(ilonTarget, ilatTarget))
                if (!geometry) {
                    out += l
                    return false // no geometry wanted: the decoder then skips transferNode and end
                }
                l += ilatStart / 1e6 - 90.0
                l += ilonStart / 1e6 - 180.0
                current = l
                target = ilonTarget to ilatTarget
                return true
            }

            override fun transferNode(ilon: Int, ilat: Int) {
                current!! += ilat / 1e6 - 90.0
                current!! += ilon / 1e6 - 180.0
            }

            override fun end() {
                val l = current!!
                l += target!!.second / 1e6 - 90.0
                l += target!!.first / 1e6 - 180.0
                out += l
                current = null
            }

            override fun hasMatch(lon: Int, lat: Int) = false
        }
        val v = validator(profile)
        for (di in -around..around) for (dj in -around..around) {
            if (tile.holds(cell.first + di, cell.second + dj)) tile.decodeLinks(cell.first + di, cell.second + dj, v, matcher)
        }
        return out
    }

    private fun lat(id: Long) = TileNodes.ilat(id) / 1e6 - 90.0

    private fun lon(id: Long) = TileNodes.ilon(id) / 1e6 - 180.0

    private fun metres(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val m = 6_371_000.0 * PI / 180.0
        return hypot((lat2 - lat1) * m, (lon2 - lon1) * m * cos(Math.toRadians(lat1)))
    }

    /** The distance from (la, lo) to the segment between two (lat, lon) points, in the plane around (la, lo). */
    private fun segmentMetres(la: Double, lo: Double, a: List<Any>, b: List<Any>): Double {
        val m = 6_371_000.0 * PI / 180.0
        val k = cos(Math.toRadians(la))
        val ax = ((a[1] as Double) - lo) * m * k
        val ay = ((a[0] as Double) - la) * m
        val bx = ((b[1] as Double) - lo) * m * k
        val by = ((b[0] as Double) - la) * m
        val dx = bx - ax
        val dy = by - ay
        val len2 = dx * dx + dy * dy
        val t = if (len2 == 0.0) 0.0 else ((-ax * dx - ay * dy) / len2).coerceIn(0.0, 1.0)
        return hypot(ax + t * dx, ay + t * dy)
    }

    override fun close() = tiles.forEach { it.close() }

    companion object {
        val TILES = listOf("E170_S50.rd5", "W70_N40.rd5", "W75_N35.rd5")
        const val FAR_METRES = 300.0
        const val ISLAND_MAX = 400
        const val MAINLAND_MIN = 10_000

        /** `fileIndex[24]`, the end of the tile's data: the last of the 25 longs in its first 200 bytes, low 48 bits. */
        fun dataEnd(tile: File): Long = RandomAccessFile(tile, "r").use { f ->
            f.seek(24 * 8L)
            f.readLong() and 0xffffffffffffL
        }

        /** A copy of [tile] cut to [length] bytes, as `<dir>/<tile's name>`. */
        fun cut(tile: File, length: Long, dir: File): File {
            dir.mkdirs()
            val out = File(dir, tile.name)
            val bytes = tile.readBytes()
            require(length in 0 until bytes.size) { "a cut is shorter than the tile" }
            out.writeBytes(bytes.copyOf(length.toInt()))
            return out
        }

        /** A copy of [tile] with byte [at] XORed with 0xFF, as `<dir>/<tile's name>`. */
        fun flip(tile: File, at: Int, dir: File): File {
            dir.mkdirs()
            val out = File(dir, tile.name)
            val bytes = tile.readBytes()
            bytes[at] = (bytes[at].toInt() xor 0xff).toByte()
            out.writeBytes(bytes)
            return out
        }
    }
}
