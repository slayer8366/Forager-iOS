package com.zynergylabs.forager.brouter.util

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import btools.util.ByteDataReader as JByteDataReader
import btools.util.ByteDataWriter as JByteDataWriter

/** Item I, step 2a: seeded streams through the oracle's `ByteDataWriter`/`ByteDataReader` and the port's, both ways. */
class ByteDataTest {
    private fun value(r: Random): Int = when (r.nextInt(5)) {
        0 -> r.nextInt(-200, 200)
        1 -> (1 shl (7 * r.nextInt(1, 5))) + r.nextInt(-2, 3) // the 7-bit group boundaries
        2 -> listOf(Int.MIN_VALUE, Int.MIN_VALUE + 1, Int.MAX_VALUE, -1, 0)[r.nextInt(5)]
        else -> r.nextInt()
    }

    @Test
    fun varLengthStreamsAreByteIdenticalAndReadIdenticallyBothWays() {
        for (seed in 1..20) {
            val r = Random(seed)
            val n = 5_000
            val signed = BooleanArray(n) { r.nextBoolean() }
            val values = IntArray(n) { value(r) }
            val aJ = ByteArray(5 * n)
            val aK = ByteArray(5 * n)
            val wj = JByteDataWriter(aJ)
            val wk = ByteDataWriter(aK)
            for (i in 0 until n) {
                if (signed[i]) { wj.writeVarLengthSigned(values[i]); wk.writeVarLengthSigned(values[i]) }
                else { wj.writeVarLengthUnsigned(values[i]); wk.writeVarLengthUnsigned(values[i]) }
            }
            assertContentEquals(aJ, aK, "seed $seed: written bytes")
            val end = wj.size()
            val rj = JByteDataReader(aK.copyOf(end))
            val rk = ByteDataReader(aJ.copyOf(end))
            for (i in 0 until n) {
                assertEquals(rj.hasMoreData(), rk.hasMoreData(), "seed $seed value $i: hasMoreData")
                val vj = if (signed[i]) rj.readVarLengthSigned() else rj.readVarLengthUnsigned()
                val vk = if (signed[i]) rk.readVarLengthSigned() else rk.readVarLengthUnsigned()
                assertEquals(vj, vk, "seed $seed value $i (${values[i]}, signed ${signed[i]})")
            }
            assertEquals(rj.hasMoreData(), rk.hasMoreData(), "seed $seed: hasMoreData at the end")
        }
    }

    @Test
    fun fixedWidthReadsOnRandomBytesMatchTheOracle() {
        for (seed in 1..20) {
            val r = Random(seed)
            val bytes = ByteArray(2_048).also { r.nextBytes(it) }
            val offset = r.nextInt(16)
            val rj = JByteDataReader(bytes, offset)
            val rk = ByteDataReader(bytes, offset)
            // An upper bound on the bytes read so far: every read stays inside the array, and no read re-syncs the two
            // readers, so a port that consumed a different number of bytes fails on the reads after it.
            var bound = offset
            var i = 0
            while (bound + 8 <= bytes.size) {
                when (r.nextInt(4)) {
                    0 -> { assertEquals(rj.readInt(), rk.readInt(), "seed $seed op $i: readInt"); bound += 4 }
                    1 -> { assertEquals(rj.readLong(), rk.readLong(), "seed $seed op $i: readLong"); bound += 8 }
                    2 -> { assertEquals(rj.readByte(), rk.readByte(), "seed $seed op $i: readByte"); bound += 1 }
                    else -> {
                        val signed = r.nextBoolean()
                        val vj = if (signed) rj.readVarLengthSigned() else rj.readVarLengthUnsigned()
                        val vk = if (signed) rk.readVarLengthSigned() else rk.readVarLengthUnsigned()
                        assertEquals(vj, vk, "seed $seed op $i: varLength, signed $signed")
                        bound += 5
                    }
                }
                assertEquals(rj.hasMoreData(), rk.hasMoreData(), "seed $seed op $i: hasMoreData")
                i++
            }
            rj.reset(bytes); rk.reset(bytes)
            assertEquals(rj.readLong(), rk.readLong(), "seed $seed: readLong after reset")
        }
    }

    @Test
    fun aNullArrayHasNoDataOnBoth() {
        assertEquals(JByteDataReader(null).hasMoreData(), ByteDataReader(null).hasMoreData())
        assertEquals(JByteDataReader(null, 3).hasMoreData(), ByteDataReader(null, 3).hasMoreData())
        val rj = JByteDataReader(ByteArray(2)).apply { reset(null) }
        val rk = ByteDataReader(ByteArray(2)).apply { reset(null) }
        assertEquals(rj.hasMoreData(), rk.hasMoreData())
        assertEquals(false, rk.hasMoreData())
    }
}
