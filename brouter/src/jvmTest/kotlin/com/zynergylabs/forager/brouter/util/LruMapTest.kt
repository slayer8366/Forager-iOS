package com.zynergylabs.forager.brouter.util

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import btools.util.LruMap as JLruMap
import btools.util.LruMapNode as JLruMapNode

/**
 * Item I, step 2a: `LruMap` on seeded keys, driven as BRouter's caches drive it (`get`; on a hit `touch`, on a miss
 * `removeLru` then `put`). Hashes collide on purpose and include negatives, so bins hold chains.
 */
class LruMapTest {
    private class JNode(val id: Int, h: Int) : JLruMapNode() {
        init { hash = h }
        override fun equals(other: Any?) = other is JNode && other.id == id
        override fun hashCode() = hash
    }

    private class KNode(val id: Int, h: Int) : LruMapNode() {
        init { hash = h }
        override fun equals(other: Any?) = other is KNode && other.id == id
        override fun hashCode() = hash
    }

    private fun hashOf(id: Int, spread: Int): Int = when (id % 5) {
        0 -> Int.MIN_VALUE + id % spread
        1 -> -(id % spread) - 1
        else -> (id * 31) % spread
    }

    @Test
    fun cacheTrafficHitsAndEvictsAsTheOracleDoes() {
        // maxSize from 2: at 1, the oracle's removeLru reads `lru.previous` on a null lru (LruMap.java:80-81, read).
        for (seed in 1..8) for (bins in intArrayOf(1, 7, 64)) for (maxSize in intArrayOf(2, 5, 100)) {
            val r = Random(seed)
            val spread = 1 + r.nextInt(50)
            val j = JLruMap(bins, maxSize)
            val k = LruMap(bins, maxSize)
            val logJ = ArrayList<String>()
            val logK = ArrayList<String>()
            repeat(20_000) {
                val id = r.nextInt(maxSize * 3 + 2)
                val h = hashOf(id, spread)
                val hj = j.get(JNode(id, h)) as JNode?
                val hk = k.get(KNode(id, h)) as KNode?
                if (hj != null) { j.touch(hj); logJ.add("hit ${hj.id}") } else {
                    logJ.add("evict ${(j.removeLru() as JNode?)?.id}"); j.put(JNode(id, h)); logJ.add("put $id")
                }
                if (hk != null) { k.touch(hk); logK.add("hit ${hk.id}") } else {
                    logK.add("evict ${(k.removeLru() as KNode?)?.id}"); k.put(KNode(id, h)); logK.add("put $id")
                }
            }
            assertEquals(logJ, logK, "seed $seed bins $bins maxSize $maxSize")
        }
    }
}
