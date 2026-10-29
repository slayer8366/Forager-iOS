package com.zynergylabs.forager.brouter.util

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import btools.util.CompactLongMap as JCompactLongMap
import btools.util.FrozenLongMap as JFrozenLongMap

/** Item I, step 2a: `CompactLongMap` and `FrozenLongMap` on seeded keys, every answer compared with the oracle's. */
class LongMapTest {
    /** The outcome of one call: its value, or the exception's class and message. */
    private fun <T> outcome(call: () -> T): Any? = try {
        call()
    } catch (e: RuntimeException) {
        "${e::class.java.name}: ${e.message}"
    }

    private fun keys(r: Random, n: Int): LongArray = when (r.nextInt(3)) {
        0 -> LongArray(n) { r.nextLong() }
        1 -> LongArray(n) { r.nextLong(-n * 4L, n * 4L) }
        else -> LongArray(n) { it.toLong() * 3 }.also { it.shuffle(r) }
    }.distinct().toLongArray()

    @Test
    fun compactAndFrozenMapsAnswerAsTheOracleDoes() {
        for (seed in 1..12) for (n in intArrayOf(0, 1, 2, 7, 1_000, 40_000)) {
            val r = Random(seed * 100 + n)
            val ks = keys(r, n)
            val j = JCompactLongMap<String>()
            val k = CompactLongMap<String>()
            val probe = { key: Long, where: String ->
                assertEquals(outcome { j.get(key) }, outcome { k.get(key) }, "$where: get($key)")
                assertEquals(outcome { j.contains(key) }, outcome { k.contains(key) }, "$where: contains($key)")
            }
            for ((i, key) in ks.withIndex()) {
                val v = "v$i"
                if (r.nextBoolean()) assertEquals(outcome { j.put(key, v) }, outcome { k.put(key, v) }, "put $i")
                else assertEquals(outcome { j.fastPut(key, v) }, outcome { k.fastPut(key, v) }, "fastPut $i")
                assertEquals(j.size(), k.size(), "size after $i")
                if (r.nextInt(10) == 0) {
                    val old = ks[r.nextInt(i + 1)]
                    assertEquals(outcome { j.put(old, "w$i") }, outcome { k.put(old, "w$i") }, "re-put $old")
                    probe(old, "after re-put")
                    probe(r.nextLong(), "random")
                }
            }
            val fj = JFrozenLongMap(j)
            val fk = FrozenLongMap(k)
            assertEquals(fj.size(), fk.size(), "seed $seed n $n: frozen size")
            assertContentEquals(fj.keyArray, fk.getKeyArray(), "seed $seed n $n: frozen keys")
            assertEquals(fj.valueList, fk.getValueList(), "seed $seed n $n: frozen values")
            for (i in 0 until minOf(n, 2_000)) {
                val key = if (i % 2 == 0) ks[r.nextInt(ks.size)] else r.nextLong()
                assertEquals(outcome { fj.get(key) }, outcome { fk.get(key) }, "frozen get($key)")
                assertEquals(outcome { fj.contains(key) }, outcome { fk.contains(key) }, "frozen contains($key)")
                assertEquals(outcome { fj.put(key, "f$i") }, outcome { fk.put(key, "f$i") }, "frozen put($key)")
            }
            assertEquals(outcome { fj.get(5L) }, outcome { fk.get(5L) }, "frozen get(5)")
            assertEquals(outcome { fj.put(Long.MIN_VALUE, "x") }, outcome { fk.put(Long.MIN_VALUE, "x") }, "frozen put(MIN)")
            assertEquals(outcome { fj.fastPut(1L, "x") }, outcome { fk.fastPut(1L, "x") }, "frozen fastPut")
            assertEquals(fj.valueList, fk.getValueList(), "seed $seed n $n: frozen values after puts")
        }
    }

    @Test
    fun duplicateKeysFailAsTheOracleFails() {
        // fastPut skips the duplicate check, so a duplicate surfaces in _add's merge or in moveToFrozenArrays ("late check").
        for (seed in 1..40) {
            val r = Random(seed)
            val ks = keys(r, r.nextInt(2, 300))
            val dup = ks[r.nextInt(ks.size)]
            val at = r.nextInt(ks.size + 1)
            val stream = ks.toMutableList().apply { add(at, dup) }
            val j = JCompactLongMap<String>()
            val k = CompactLongMap<String>()
            val logJ = ArrayList<Any?>()
            val logK = ArrayList<Any?>()
            // Up to and including the first failure: after one, the map's arrays are half merged, and what fails next is
            // the JVM's own null and bounds checks, whose messages differ between Java and Kotlin by construction.
            var failed = false
            for ((i, key) in stream.withIndex()) {
                val oj = outcome { j.fastPut(key, "v$i") }
                logJ.add(oj); logK.add(outcome { k.fastPut(key, "v$i") })
                logJ.add(j.size()); logK.add(k.size())
                if (oj is String) { failed = true; break }
            }
            if (!failed) {
                logJ.add(outcome { JFrozenLongMap(j).keyArray.toList() })
                logK.add(outcome { FrozenLongMap(k).getKeyArray().toList() })
            }
            assertEquals(true, logJ.any { it is String && "duplicate key" in it }, "seed $seed: the oracle saw the duplicate")
            assertEquals(logJ, logK, "seed $seed: duplicate $dup at $at")
        }
    }
}
