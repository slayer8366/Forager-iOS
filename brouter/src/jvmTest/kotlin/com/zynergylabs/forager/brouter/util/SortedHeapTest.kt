package com.zynergylabs.forager.brouter.util

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import btools.util.SortedHeap as JSortedHeap

/**
 * Item I, step 2a: `SortedHeap` under seeded mixes of adds and pops with equal keys, **the pop sequence exact**. The heap
 * decides ties in the search (the open set), so which of two equal keys comes out first is part of what is compared.
 */
class SortedHeapTest {
    /** Runs one seeded op stream through both heaps and returns the two event logs: each pop's value, and the sizes. */
    private fun run(seed: Int, ops: Int, keyRange: Int, addPercent: Int, clearEvery: Int = 0): Pair<List<Any?>, List<Any?>> {
        val r = Random(seed)
        val j = JSortedHeap<Int>()
        val k = SortedHeap<Int>()
        val logJ = ArrayList<Any?>()
        val logK = ArrayList<Any?>()
        var id = 0
        for (i in 0 until ops) {
            if (clearEvery > 0 && i % clearEvery == clearEvery - 1) {
                j.clear(); k.clear()
            } else if (r.nextInt(100) < addPercent) {
                val key = if (keyRange == 0) r.nextInt() else r.nextInt(keyRange)
                j.add(key, id); k.add(key, id)
                id++
            } else {
                logJ.add(j.popLowestKeyValue()); logK.add(k.popLowestKeyValue())
            }
            logJ.add(-j.size - 1); logK.add(-k.getSize() - 1) // sizes logged as negatives, apart from the values
        }
        while (true) { // drain: every value left comes out in the oracle's order
            val vj = j.popLowestKeyValue()
            logJ.add(vj); logK.add(k.popLowestKeyValue())
            if (vj == null) break
        }
        return logJ to logK
    }

    @Test
    fun allKeysEqualPopInTheOraclesOrder() {
        for (seed in 1..10) {
            val (j, k) = run(seed, ops = 20_000, keyRange = 1, addPercent = 60)
            assertEquals(j, k, "seed $seed")
        }
    }

    @Test
    fun fewDistinctKeysPopInTheOraclesOrder() {
        for (seed in 1..10) for (keyRange in intArrayOf(2, 3, 16, 1000)) {
            val (j, k) = run(seed, ops = 20_000, keyRange = keyRange, addPercent = 55 + seed)
            assertEquals(j, k, "seed $seed, keys 0 until $keyRange")
        }
    }

    @Test
    fun aLargeHeapThroughManyBinsPopsInTheOraclesOrder() {
        // 200,000 adds before any pop push the bins to 2^16 and beyond; then a mixed tail.
        for ((seed, keyRange) in listOf(1 to 7, 2 to 100_000, 3 to 0)) {
            val (j, k) = run(seed, ops = 300_000, keyRange = keyRange, addPercent = 80)
            assertEquals(j, k, "seed $seed, key range $keyRange")
        }
    }

    @Test
    fun clearAndEmptyPopsMatchTheOracle() {
        for (seed in 1..5) {
            val (j, k) = run(seed, ops = 10_000, keyRange = 5, addPercent = 50, clearEvery = 97)
            assertEquals(j, k, "seed $seed")
        }
    }
}
