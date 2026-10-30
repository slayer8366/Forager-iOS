package com.zynergylabs.forager.brouter.oracle.e3

import btools.mapaccess.OsmNode

/**
 * E3's positive control (Phase 1 report, E3): part 7's twelve-node equal-hash map, `09-float/part7/TreeBinOrder.java`
 * at 20e1f92, ported line for line. Twelve OsmNodes share one `ilon + ilat` (OsmNode.hashCode), more than HashMap's
 * TREEIFY_THRESHOLD of 8, so their bin is a tree ordered by System.identityHashCode (HashMap.tieBreakOrder). Built as
 * OsmNodesMap builds `hmap`: `HashMap(4096)`, with 2,000 distinct-sum keys beside the group, as part 7 built it.
 */
object TreeBinControl {
    private const val GROUP = 12
    private const val SPREAD = 2000

    private fun keys(): List<OsmNode> {
        val k = ArrayList<OsmNode>()
        for (i in 0 until GROUP) k.add(OsmNode(105_000_000 + 7 * i, 131_000_000 - 7 * i))
        var s = 12345L
        for (i in 0 until SPREAD) {
            s = s * 6364136223846793005L + 1442695040888963407L
            val dx = ((s ushr 33) % 400_000).toInt()
            val dy = ((s ushr 13) % 400_000).toInt()
            k.add(OsmNode(105_100_000 + dx, 130_500_000 + dy))
        }
        return k
    }

    /** The group's iteration order: the insertion indices 0 to 11, in the order HashMap.values() gives them. */
    fun groupOrder(): String {
        val keys = keys()
        val m = HashMap<OsmNode, OsmNode>(4096)
        for (n in keys) m[n] = n
        val b = StringBuilder()
        for (n in m.values) {
            val idx = keys.indexOf(n) // equals is on (ilon, ilat): the key's position in the insertion list
            if (idx < GROUP) b.append(idx).append(',')
        }
        return b.toString()
    }
}
