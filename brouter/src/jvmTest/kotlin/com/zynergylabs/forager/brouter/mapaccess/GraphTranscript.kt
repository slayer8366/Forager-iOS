package com.zynergylabs.forager.brouter.mapaccess

import java.util.IdentityHashMap

/**
 * A decoded graph written out as text, the same way for both engines (step 5b, item I: "a digest per cache of nodes,
 * links, elevations, way descriptions and turn restrictions"). Two engines agree on a cache when their texts are equal;
 * the digest is the text's SHA-256. Every list is in an order the engine itself fixes (link chains, the seeds' id order)
 * or sorted by position (the map's hollow nodes), so the text never depends on `hmap`'s iteration order.
 */
internal class GraphTranscript<N : Any, L : Any>(private val g: GraphAccess<N, L>) {
    private val sb = StringBuilder()
    private val decoder = g.newGeometryDecoder()

    /** Counts over what was written, so a run can show what it covered. */
    val counts = LongArray(Count.entries.size)

    enum class Count { NODES, LINKS, GEOMETRY, DESCRIBED_LINKS, DESCRIBED_NODES, RESTRICTIONS, LINK_SELF, LINK_TARGET, LINK_OWN, HOLLOW_TARGETS }

    private fun count(c: Count, by: Int = 1) {
        counts[c.ordinal] += by.toLong()
    }

    fun text(): String = sb.toString()

    fun line(s: String) {
        sb.append(s).append('\n')
    }

    /** One node: its fields, its turn restrictions, and every link in its chain with what the link carries. */
    fun node(n: N, holders: Boolean) {
        count(Count.NODES)
        val desc = g.description(n)
        if (desc != null) count(Count.DESCRIBED_NODES)
        line("N ${g.ilon(n)} ${g.ilat(n)} ${g.selev(n)} ${g.isHollow(n)} ${g.visitID(n)} ${hex(desc)}")
        val restrictions = g.restrictions(n)
        count(Count.RESTRICTIONS, restrictions.size)
        restrictions.forEach { line("R $it") }

        val targets = ArrayList<N>()
        var l = g.firstLink(n)
        var guard = 0
        while (l != null) {
            if (++guard > 100_000) {
                line("LOOP")
                break
            }
            val t = g.target(l, n)
            if (t == null) {
                line("L null-target")
                break
            }
            targets.add(t)
            count(Count.LINKS)
            val obj = when {
                l === n -> "self".also { count(Count.LINK_SELF) }
                l === t -> "target".also { count(Count.LINK_TARGET) }
                else -> "own".also { count(Count.LINK_OWN) }
            }
            if (g.isHollow(t)) count(Count.HOLLOW_TARGETS)
            val linkDesc = g.linkDescription(l)
            if (linkDesc != null) count(Count.DESCRIBED_LINKS)
            line("L ${g.ilon(t)} ${g.ilat(t)} ${g.isHollow(t)} ${g.isReverse(l, n)} ${g.isBidirectional(l)} $obj ${hex(linkDesc)} ${hex(g.geometry(l))}")
            if (g.geometry(l) != null) {
                count(Count.GEOMETRY)
                line("G ${g.geometryText(decoder, l, n, t)}")
            }
            if (holders) line("H ${g.linkHolderText(l, n, t)}")
            l = g.next(l, n)
        }

        if (restrictions.isNotEmpty()) {
            // Every turn through this node between its link targets and the restrictions' own end points, in four modes.
            val points = LinkedHashSet<Pair<Int, Int>>()
            targets.forEach { points.add(g.ilon(it) to g.ilat(it)) }
            for (r in restrictions) {
                val f = r.split(' ')
                points.add(f[2].toInt() to f[3].toInt())
                points.add(f[4].toInt() to f[5].toInt())
            }
            val bits = StringBuilder()
            for (from in points) for (to in points) for (mode in 0 until 4) {
                bits.append(if (g.turnForbidden(n, from.first, from.second, to.first, to.second, mode and 1 != 0, mode and 2 != 0)) '1' else '0')
            }
            line("T ${points.size} $bits")
        }
    }

    /** The map's state: its counters, then its hollow nodes sorted by position, each with its link chain. */
    fun mapState(map: Any): List<N> {
        line("C ${g.nodesCreated(map)} ${g.lastVisitID(map)} ${g.baseID(map)}")
        val hollow = g.hollowNodes(map).sortedWith(compareBy<N>({ g.ilon(it) }, { g.ilat(it) }))
        line("M ${hollow.size}")
        return hollow
    }

    /** Every node reachable from [start], breadth first in link-chain order, each written once. */
    fun reachable(start: List<N>, holders: Boolean) {
        val seen = IdentityHashMap<N, Boolean>()
        val queue = ArrayDeque<N>()
        for (s in start) if (seen.put(s, true) == null) queue.add(s)
        while (queue.isNotEmpty()) {
            val n = queue.removeFirst()
            node(n, holders)
            var l = g.firstLink(n)
            var guard = 0
            while (l != null && ++guard <= 100_000) {
                val t = g.target(l, n) ?: break
                if (seen.put(t, true) == null) queue.add(t)
                l = g.next(l, n)
            }
        }
    }

    companion object {
        private val HEX = "0123456789abcdef".toCharArray()

        fun hex(b: ByteArray?): String {
            if (b == null) return "-"
            val out = CharArray(b.size * 2)
            for (i in b.indices) {
                out[2 * i] = HEX[(b[i].toInt() shr 4) and 15]
                out[2 * i + 1] = HEX[b[i].toInt() and 15]
            }
            return String(out)
        }

        /** The first line where [a] and [b] differ, with both lines, or null when they are equal. */
        fun firstDifference(a: String, b: String): String? {
            if (a == b) return null
            val la = a.split('\n')
            val lb = b.split('\n')
            for (i in 0 until maxOf(la.size, lb.size)) {
                val x = la.getOrNull(i)
                val y = lb.getOrNull(i)
                if (x != y) return "line ${i + 1} of ${la.size}/${lb.size}: oracle [$x] port [$y]"
            }
            return "texts differ"
        }
    }
}
