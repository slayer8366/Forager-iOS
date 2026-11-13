package com.zynergylabs.forager.brouter.mapaccess

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * RECORD -50's second test: equal-hash tree bins come out of [NodeHashMap] in the same order from fresh maps given the
 * same inserts. Java breaks such ties on `System.identityHashCode` (`HashMap.java:2063`), which differs between JVM
 * runs; the port's stand-in numbers each key at its first tie-break, so the order is fixed (RECORD -50, -53).
 *
 * It runs in `commonTest`, so on the JVM and the simulator (RECORD -53). It has no oracle: the order it checks is the
 * port's own, compared across fresh maps. The oracle comparison is `NodeHashMapOrderTest`, which refuses these bins.
 *
 * The inserts, the same in every map, each map from fresh [OsmNode] objects:
 * 1. **a tie group,** 32 keys on one anti-diagonal (`ilon + ilat` = 1000, `OsmNode.hashCode`), and 8 keys of hash
 *    5,096, which share its bin at 4,096 slots. The bin becomes a tree at its 9th key (`treeifyBin`), and the rest
 *    arrive through `putTreeVal`;
 * 2. **ordinary keys** up to 3,072 entries, the threshold of 4,096 slots, none of them in that bin at any size;
 * 3. **one more,** which resizes to 8,192 slots. The bin `split`s: 32 keys stay and 8 move (bit 4,096), both over
 *    `UNTREEIFY_THRESHOLD`, so both halves are `treeify`d again;
 * 4. **removes** of 26 of the 32 through equal, fresh keys (`removeTreeNode`, and `untreeify` once the tree is small),
 *    a re-put of an equal object over a present key, and some ordinary removes;
 * 5. **a second tie group,** 10 new keys of hash 1000 into the same bin, so it becomes a tree again, mixing keys that
 *    already hold a number with new ones.
 *
 * Seen failing by a revert of the stand-in to a random number fixed at first request (the step 5b report).
 */
class NodeHashMapTieBreakTest {
    @Test
    fun equalHashTreeBinsIterateInOneOrderAcrossFreshMaps() {
        val runs = (0 until 3).map { runInserts() }
        val first = runs[0]

        for ((m, run) in runs.withIndex()) {
            // The check is evidence only if it saw ties on each path that could fail it.
            assertTrue(run.idsAssigned > 0, "map $m gave no key a tie-break number")
            assertTrue(run.callsAfterGroup > 0, "map $m: no tie-break in treeifyBin or putTreeVal ($run)")
            assertTrue(run.callsAfterResize > run.callsBeforeResize, "map $m: no tie-break in split's treeify ($run)")
            assertTrue(run.callsAtEnd > run.callsAfterResize, "map $m: no tie-break after the removes ($run)")
            assertEquals(run.expectedAtEnd, run.orderAtEnd.sorted(), "map $m: the final iteration is not the live keys")
        }
        for (m in 1 until runs.size) {
            assertEquals(first.orderAfterGroup, runs[m].orderAfterGroup, "map $m's order after the tie group differs from map 0's")
            assertEquals(first.orderAfterResize, runs[m].orderAfterResize, "map $m's order after the resize differs from map 0's")
            assertEquals(first.orderAtEnd, runs[m].orderAtEnd, "map $m's final order differs from map 0's")
        }
        println(
            "5b tie-break determinism: 3 maps, ${first.orderAtEnd.size} keys at the end; numbers given " +
                "${first.idsAssigned}; tie-break calls ${first.callsAfterGroup}, ${first.callsBeforeResize}, " +
                "${first.callsAfterResize}, ${first.callsAtEnd}",
        )
    }

    private class Run(
        val idsAssigned: Int,
        val callsAfterGroup: Int,
        val callsBeforeResize: Int,
        val callsAfterResize: Int,
        val callsAtEnd: Int,
        val orderAfterGroup: List<Long>,
        val orderAfterResize: List<Long>,
        val orderAtEnd: List<Long>,
        val expectedAtEnd: List<Long>,
    ) {
        override fun toString() =
            "tie-break calls $callsAfterGroup, $callsBeforeResize, $callsAfterResize, $callsAtEnd; numbers $idsAssigned"
    }

    private fun code(ilon: Int, ilat: Int): Long = (ilon.toLong() shl 32) or (ilat.toLong() and 0xFFFFFFFFL)

    private fun order(map: NodeHashMap): List<Long> = map.valuesIterator().asSequence().map { code(it.ilon, it.ilat) }.toList()

    private fun spread(h: Int) = h xor (h ushr 16)

    private fun runInserts(): Run {
        val map = NodeHashMap(4096)
        val live = LinkedHashSet<Long>()
        fun put(ilon: Int, ilat: Int) {
            map.put(OsmNode(ilon, ilat))
            live.add(code(ilon, ilat))
        }
        fun remove(ilon: Int, ilat: Int) {
            assertEquals(code(ilon, ilat), map.remove(OsmNode(ilon, ilat))?.let { code(it.ilon, it.ilat) })
            live.remove(code(ilon, ilat))
        }

        // 1. The tie group (hash 1000) and its bin-mates (hash 5096), interleaved.
        val group = (0 until 32).map { 10 + 3 * it }
        val mates = (0 until 8).map { 50 + 5 * it }
        for (ilon in group.take(20)) put(ilon, 1000 - ilon)
        for (ilon in mates) put(ilon, 5096 - ilon)
        for (ilon in group.drop(20)) put(ilon, 1000 - ilon)
        val callsAfterGroup = map.tieBreakCalls
        val orderAfterGroup = order(map)

        // 2. Ordinary keys, each of its own hash, none whose low 12 spread bits put it in the group's bin.
        val ordinary = ArrayList<Int>()
        var j = 0
        while (live.size < 3072) {
            val ilon = 200_000 + 3 * j++
            if ((spread(ilon + 7) and 0xFFF) == 1000) continue
            put(ilon, 7)
            ordinary.add(ilon)
        }
        val callsBeforeResize = map.tieBreakCalls

        // 3. One more: 3,073 entries pass the threshold of 3,072 and the table doubles.
        while (true) {
            val ilon = 200_000 + 3 * j++
            if ((spread(ilon + 7) and 0xFFF) == 1000) continue
            put(ilon, 7)
            ordinary.add(ilon)
            break
        }
        val callsAfterResize = map.tieBreakCalls
        val orderAfterResize = order(map)

        // 4. Removes from the tree through fresh equal keys, a re-put over a present key, and ordinary removes.
        for (ilon in group.take(26)) remove(ilon, 1000 - ilon)
        map.put(OsmNode(group[30], 1000 - group[30]))
        for (k in 0 until ordinary.size step 97) remove(ordinary[k], 7)

        // 5. A second tie group into the same bin.
        for (i in 0 until 10) put(500 + 7 * i, 1000 - (500 + 7 * i))

        return Run(
            idsAssigned = map.tieBreakIdsAssigned,
            callsAfterGroup = callsAfterGroup,
            callsBeforeResize = callsBeforeResize,
            callsAfterResize = callsAfterResize,
            callsAtEnd = map.tieBreakCalls,
            orderAfterGroup = orderAfterGroup,
            orderAfterResize = orderAfterResize,
            orderAtEnd = order(map),
            expectedAtEnd = live.sorted(),
        )
    }
}
