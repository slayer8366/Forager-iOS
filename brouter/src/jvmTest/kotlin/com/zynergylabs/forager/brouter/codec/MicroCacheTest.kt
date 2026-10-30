package com.zynergylabs.forager.brouter.codec

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotSame
import btools.codec.MicroCache as JMicroCache

/**
 * Item I, step 3: `MicroCache`'s reached methods against the oracle's.
 * - The empty cache, which is what the engine's direct-weaving path holds (`OsmFile.java:126`, `:143`).
 * - Filled caches under a seeded mix of `getAndClear`, `collect` and `unGhost`. `shrinkId` is abstract in effect (the
 *   base throws; `MicroCache2`'s is not reached), so both sides' test subclasses use the identity, and are filled with the
 *   same ids, node ends and bytes. After every operation the whole state is compared: the result, the read window, the
 *   ids, the ends with their deleted flags, the bytes, and the flags.
 */
class MicroCacheTest {
    @Test
    fun emptyCacheIsTheOraclesEmptyCache() {
        val j = JMicroCache.emptyCache()
        val k = MicroCache.emptyCache()
        assertNotSame(k, MicroCache.emptyCache(), "a new instance per call, as in Java")
        assertEquals(j.size, k.getSize())
        assertEquals(j.dataSize, k.getDataSize())
        assertEquals(j.virgin, k.virgin)
        assertEquals(j.ghost, k.ghost)
        assertEquals(j.getAndClear(42L), k.getAndClear(42L))
        assertEquals(j.collect(0), k.collect(0))
        j.unGhost()
        k.unGhost()
        assertEquals(j.virgin, k.virgin, "collect(0) on an empty cache leaves virgin")
        val mj = assertFailsWith<IllegalArgumentException> { j.shrinkId(1L) }.message
        val mk = assertFailsWith<IllegalArgumentException> { k.shrinkId(1L) }.message
        assertEquals(mj, mk)
        assertEquals(JMicroCache.emptyNonVirgin.virgin, MicroCache.emptyNonVirgin.virgin)
        assertEquals(false, MicroCache.emptyNonVirgin.virgin)
        assertEquals(JMicroCache.debug, MicroCache.debug)
    }

    private class JCache(ab: ByteArray) : JMicroCache(ab) {
        override fun shrinkId(id64: Long): Int = id64.toInt()
        fun fill(ids: IntArray, ends: IntArray) { faid = ids; fapos = ends; init(ids.size) }
        fun state(): String = "size ${getSize()} data ${getDataSize()} off $aboffset end $aboffsetEnd virgin $virgin ghost $ghost " +
            "faid ${faid?.contentToString()} fapos ${fapos?.contentToString()} ab ${ab?.contentToString()}"
    }

    private class KCache(ab: ByteArray) : MicroCache(ab) {
        override fun shrinkId(id64: Long): Int = id64.toInt()
        fun fill(ids: IntArray, ends: IntArray) { faid = ids; fapos = ends; init(ids.size) }
        fun state(): String = "size ${getSize()} data ${getDataSize()} off $aboffset end $aboffsetEnd virgin $virgin ghost $ghost " +
            "faid ${faid?.contentToString()} fapos ${fapos?.contentToString()} ab ${ab?.contentToString()}"
    }

    @Test
    fun filledCachesFollowTheOracleUnderSeededOperations() {
        val sizes = listOf(0, 1, 2, 3, 4, 7, 8, 9, 15, 16, 17, 63, 64, 65, 100, 255, 256, 257, 500, 1000)
        var ops = 0
        var hits = 0
        var collected = 0
        for ((seed, n) in sizes.withIndex()) {
            val r = Random(seed + 1)
            var id = r.nextInt(-1000, 1000)
            val ids = IntArray(n) { id += r.nextInt(1, 50); id }
            var end = 0
            val ends = IntArray(n) { end += r.nextInt(0, 20); end }
            val bytes = ByteArray(end + if (r.nextBoolean()) r.nextInt(1, 10) else 0).also { r.nextBytes(it) }
            val j = JCache(bytes.copyOf()).apply { fill(ids.copyOf(), ends.copyOf()) }
            val k = KCache(bytes.copyOf()).apply { fill(ids.copyOf(), ends.copyOf()) }
            assertEquals(j.state(), k.state(), "n $n: filled")
            repeat(400) { i ->
                val at = "n $n op $i"
                when (r.nextInt(10)) {
                    in 0..6 -> {
                        val probe = if (n > 0 && r.nextInt(4) != 0) ids[r.nextInt(n)] else r.nextInt(-2000, ids.lastOrNull() ?: 0 + 2000)
                        val hj = j.getAndClear(probe.toLong())
                        assertEquals(hj, k.getAndClear(probe.toLong()), "$at: getAndClear($probe)")
                        if (hj) hits++
                    }
                    7, 8 -> {
                        val t = r.nextInt(0, 12)
                        val cj = j.collect(t)
                        assertEquals(cj, k.collect(t), "$at: collect($t)")
                        if (cj > 0) collected++
                    }
                    else -> {
                        val g = r.nextBoolean()
                        j.ghost = g
                        k.ghost = g
                        j.unGhost()
                        k.unGhost()
                    }
                }
                assertEquals(j.state(), k.state(), "$at: state")
                ops++
            }
        }
        assertEquals(sizes.size * 400, ops)
        assertEquals(true, hits > 0 && collected > 0, "hits $hits, collects that removed bytes $collected")
    }
}
