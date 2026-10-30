package com.zynergylabs.forager.brouter.mapaccess

import java.util.IdentityHashMap
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Item I, step 5b, option (i) (RECORD -24): `hmap`'s port, [NodeHashMap], iterates in `java.util.HashMap`'s order on
 * seeded `OsmNode` keys, and answers `get`, `put` and `remove` as it does. The reference is a `java.util.HashMap(4096)`
 * of the oracle's own `OsmNode`s (`OsmNodesMap.java:16`), in this JVM.
 *
 * The keys, per seed:
 * - **forced tree bins:** groups of 9 to 24 keys whose spread hashes share their low 16 bits, so each group shares one
 *   bin at every table size up to 65,536 and becomes a tree (`HashMap.java:649`). Every key in a group has its own hash;
 * - **ordinary keys,** at random positions, some placed on one anti-diagonal so that they share a hash (`ilon + ilat`,
 *   `OsmNode.java:260`). They are refused if their spread hash would join a forced bin.
 * - **The generator refuses equal-hash tree buckets** (H step 4): a tree bin holding two keys of equal hash breaks the
 *   tie on `System.identityHashCode` (`HashMap.java:2063`), which no port can reproduce. After every operation the
 *   reference's table is read, and a tree bin holding equal hashes stops the seed as refused, never as passed.
 *
 * The operations: `put` of new keys and of a second object equal to a present key (which keeps the old key and returns
 * the old value, `HashMap.java:660-667`, as `NodesCache.java:284-288` relies on), `get` and `remove` of present and
 * absent keys, up to 14,000 keys so the table grows from 4,096 to 16,384 and splits its tree bins
 * (`HashMap.java:2297`). The full iteration order is compared every 200 operations and at the end.
 */
class NodeHashMapOrderTest {
    @Test
    fun iterationOrderAndAnswersEqualJavaUtilHashMapsOnSeededOsmNodeKeys() {
        val totals = LongArray(Seen.entries.size)
        for (seed in 1..12) {
            val seen = runSeed(seed)
            for (i in totals.indices) totals[i] += seen[i]
        }
        val text = Seen.entries.joinToString(", ") { "${it.name.lowercase()} ${totals[it.ordinal]}" }
        println("5b hmap order: 12 seeds; $text")
        // The check is evidence only if it saw what could fail it.
        for (s in listOf(Seen.TREE_BINS, Seen.PUT_INTO_TREE, Seen.REMOVE_FROM_TREE, Seen.RESIZE_WITH_TREE, Seen.TREE_TO_LIST, Seen.EQUAL_HASH_LIST_BINS)) {
            assertTrue(totals[s.ordinal] > 0, "never seen: $s ($text)")
        }
        assertEquals(0L, totals[Seen.REFUSED.ordinal], "seeds refused for an equal-hash tree bucket ($text)")
    }

    private enum class Seen { OPS, ORDER_CHECKS, TREE_BINS, PUT_INTO_TREE, REMOVE_FROM_TREE, RESIZE_WITH_TREE, TREE_TO_LIST, EQUAL_HASH_LIST_BINS, MAX_SIZE, REFUSED }

    private class Key(val id: Int, val ilon: Int, val ilat: Int, val oracle: btools.mapaccess.OsmNode, val port: OsmNode)

    private fun spread(h: Int) = h xor (h ushr 16)

    private fun runSeed(seed: Int): LongArray {
        val seen = LongArray(Seen.entries.size)
        val r = Random(seed)
        val reference = HashMap<btools.mapaccess.OsmNode, btools.mapaccess.OsmNode>(4096)
        val port = NodeHashMap(4096)
        val oracleIds = IdentityHashMap<btools.mapaccess.OsmNode, Int>()
        val portIds = IdentityHashMap<OsmNode, Int>()
        var nextId = 0

        fun newKey(ilon: Int, ilat: Int): Key {
            val id = nextId++
            val o = btools.mapaccess.OsmNode(ilon, ilat)
            val p = OsmNode(ilon, ilat)
            oracleIds[o] = id
            portIds[p] = id
            return Key(id, ilon, ilat, o, p)
        }

        // Forced groups: each a target for the spread hash's low 16 bits.
        val targets = List(24) { r.nextInt(1 shl 16) }.distinct()
        val usedHashes = HashSet<Int>()
        val pool = ArrayList<Key>()
        for (t in targets) {
            val size = r.nextInt(9, 25)
            var made = 0
            while (made < size) {
                val s = (r.nextInt(1 shl 16) shl 16) or t
                val h = s xor (s ushr 16) // spread is its own inverse
                if (!usedHashes.add(h)) continue
                val ilon = r.nextInt(170_000_000, 190_000_000)
                pool.add(newKey(ilon, h - ilon))
                made++
            }
        }
        val forcedLow = targets.toSet()
        // Ordinary keys, with runs on one anti-diagonal (equal hashes), never joining a forced bin.
        while (pool.size < 14_000) {
            val ilon = r.nextInt(100_000_000, 260_000_000)
            val ilat = r.nextInt(40_000_000, 140_000_000)
            val run = if (r.nextInt(50) == 0) r.nextInt(2, 6) else 1
            for (k in 0 until run) {
                val lon = ilon + k * 7
                val lat = ilat - k * 7
                val h = lon + lat
                if ((spread(h) and 0xffff) in forcedLow) continue
                if (run == 1 && !usedHashes.add(h)) continue
                pool.add(newKey(lon, lat))
            }
        }
        pool.shuffle(r)

        val present = ArrayList<Key>()
        var cursor = 0
        var lastTable = 0
        var lastTreeCount = 0

        fun idOf(o: btools.mapaccess.OsmNode?) = o?.let { oracleIds.getValue(it) }
        fun idOfPort(p: OsmNode?) = p?.let { portIds.getValue(it) }

        fun checkOrder(where: String) {
            seen[Seen.ORDER_CHECKS.ordinal]++
            val expected = reference.values.map { idOf(it) }
            val actual = port.valuesIterator().asSequence().map { idOfPort(it) }.toList()
            assertEquals(expected, actual, "seed $seed $where: iteration order")
        }

        val totalOps = 40_000
        for (op in 1..totalOps) {
            seen[Seen.OPS.ordinal]++
            val kind = r.nextInt(100)
            val where = "seed $seed op $op"
            // The bin this operation changes, if it changes one: only there can a new tree bin, or a tree turning back
            // into a list, appear without a resize.
            var touched: Int? = null
            var treeBefore = false
            when {
                kind < 55 && cursor < pool.size -> { // put a new key
                    val k = pool[cursor++]
                    touched = k.oracle.hashCode()
                    treeBefore = Inspect.binIsTree(reference, touched)
                    assertEquals(idOf(reference.put(k.oracle, k.oracle)), idOfPort(port.put(k.port)), "$where: put")
                    present.add(k)
                    if (treeBefore) seen[Seen.PUT_INTO_TREE.ordinal]++
                }
                kind < 62 && present.isNotEmpty() -> { // put a second object equal to a present key
                    val k = present[r.nextInt(present.size)]
                    val twin = newKey(k.ilon, k.ilat)
                    assertEquals(idOf(reference.put(twin.oracle, twin.oracle)), idOfPort(port.put(twin.port)), "$where: re-put")
                }
                kind < 80 -> { // get, present or absent
                    val k = if (present.isNotEmpty() && r.nextBoolean()) present[r.nextInt(present.size)] else pool[r.nextInt(pool.size)]
                    val probeO = btools.mapaccess.OsmNode(k.ilon, k.ilat)
                    val probeP = OsmNode(k.ilon, k.ilat)
                    assertEquals(idOf(reference[probeO]), idOfPort(port.get(probeP)), "$where: get")
                }
                present.isNotEmpty() -> { // remove, mostly present
                    val k = if (r.nextInt(10) > 0) present.removeAt(r.nextInt(present.size)) else pool[r.nextInt(pool.size)]
                    touched = k.oracle.hashCode()
                    treeBefore = Inspect.binIsTree(reference, touched)
                    val probeO = btools.mapaccess.OsmNode(k.ilon, k.ilat)
                    val probeP = OsmNode(k.ilon, k.ilat)
                    val removed = idOf(reference.remove(probeO))
                    assertEquals(removed, idOfPort(port.remove(probeP)), "$where: remove")
                    if (treeBefore && removed != null) seen[Seen.REMOVE_FROM_TREE.ordinal]++
                }
            }

            seen[Seen.MAX_SIZE.ordinal] = maxOf(seen[Seen.MAX_SIZE.ordinal], reference.size.toLong())
            val table = Inspect.tableLength(reference)
            var refused = false
            if (table != lastTable) { // a resize: every tree bin was split (HashMap.java:2297), so read them all
                val trees = Inspect.treeBins(reference)
                if (lastTreeCount > 0 && lastTable != 0) seen[Seen.RESIZE_WITH_TREE.ordinal]++
                refused = trees.any { Inspect.equalHashInBin(reference, it) }
                lastTreeCount = trees.size
                lastTable = table
            } else if (touched != null) {
                val treeAfter = Inspect.binIsTree(reference, touched)
                if (treeAfter) refused = Inspect.equalHashInBin(reference, Inspect.indexOf(reference, touched))
                if (treeBefore && !treeAfter && Inspect.binNonEmpty(reference, Inspect.indexOf(reference, touched))) {
                    seen[Seen.TREE_TO_LIST.ordinal]++
                }
            }
            if (refused) {
                seen[Seen.REFUSED.ordinal]++
                println("5b hmap order: seed $seed refused at op $op, an equal-hash tree bucket")
                return seen
            }
            if (op % 200 == 0) {
                val trees = Inspect.treeBins(reference)
                lastTreeCount = trees.size
                seen[Seen.TREE_BINS.ordinal] = maxOf(seen[Seen.TREE_BINS.ordinal], trees.size.toLong())
                checkOrder(where)
            }
        }
        seen[Seen.EQUAL_HASH_LIST_BINS.ordinal] += Inspect.equalHashListBins(reference).toLong()
        checkOrder("seed $seed end")
        return seen
    }

    /** `java.util.HashMap`'s table, read by reflection (jvmTest opens `java.util` for this; `brouter/build.gradle.kts`). */
    private object Inspect {
        private val tableField = java.util.HashMap::class.java.getDeclaredField("table").apply { isAccessible = true }
        private val nodeClass = Class.forName("java.util.HashMap\$Node")
        private val nextField = nodeClass.getDeclaredField("next").apply { isAccessible = true }
        private val hashField = nodeClass.getDeclaredField("hash").apply { isAccessible = true }
        private val treeNodeName = "java.util.HashMap\$TreeNode"

        private fun table(m: java.util.HashMap<*, *>): Array<*>? = tableField.get(m) as Array<*>?

        fun tableLength(m: java.util.HashMap<*, *>) = table(m)?.size ?: 0

        private fun bin(m: java.util.HashMap<*, *>, index: Int): List<Any> {
            val t = table(m) ?: return emptyList()
            return generateSequence(t[index]) { nextField.get(it) }.toList()
        }

        fun indexOf(m: java.util.HashMap<*, *>, hashCode: Int): Int = (tableLength(m) - 1) and (hashCode xor (hashCode ushr 16))

        fun binIsTree(m: java.util.HashMap<*, *>, hashCode: Int): Boolean {
            val t = table(m) ?: return false
            return t[indexOf(m, hashCode)]?.javaClass?.name == treeNodeName
        }

        fun binNonEmpty(m: java.util.HashMap<*, *>, index: Int) = table(m)?.getOrNull(index) != null

        fun treeBins(m: java.util.HashMap<*, *>): Set<Int> {
            val t = table(m) ?: return emptySet()
            return t.indices.filter { t[it]?.javaClass?.name == treeNodeName }.toSet()
        }

        fun equalHashInBin(m: java.util.HashMap<*, *>, index: Int): Boolean {
            val hashes = bin(m, index).map { hashField.getInt(it) }
            return hashes.size != hashes.toSet().size
        }

        fun equalHashListBins(m: java.util.HashMap<*, *>): Int {
            val t = table(m) ?: return 0
            return t.indices.count { i ->
                t[i] != null && t[i]!!.javaClass.name != treeNodeName && bin(m, i).map { hashField.getInt(it) }.let { it.size != it.toSet().size }
            }
        }
    }
}
