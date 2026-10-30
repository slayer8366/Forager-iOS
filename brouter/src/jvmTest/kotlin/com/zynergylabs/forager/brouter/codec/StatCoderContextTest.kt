package com.zynergylabs.forager.brouter.codec

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import btools.codec.StatCoderContext as JStatCoderContext

/**
 * Item I, step 3: streams written by the oracle's own encoder (BRouter 1.7.10's `btools.codec.StatCoderContext`, from the
 * `-ro` jar, which carries the encoders: `step3/ro-jar-encoders.txt`), decoded by the oracle and by the port. Every
 * decoded value and every reading bit position is compared exactly.
 */
class StatCoderContextTest {
    @Test
    fun noisyBitsTableEqualsTheOracles() {
        val field = JStatCoderContext::class.java.getDeclaredField("noisy_bits").apply { isAccessible = true }
        assertContentEquals(field.get(null) as IntArray, StatCoderContext.noisy_bits)
    }

    /** Signed values over every length the var-bits code has, bounded so the encoder's `+ (1 << (noisybits - 1))` cannot overflow. */
    private fun signed(r: Random): Int {
        val v = when (r.nextInt(4)) {
            0 -> r.nextInt(8)
            1 -> r.nextInt(2048)
            2 -> r.nextInt(1 shl 20)
            else -> r.nextInt(1 shl 28)
        }
        return if (r.nextBoolean()) -v else v
    }

    private class Op(val kind: Int, val noisybits: Int, val value: Int, val predictor: Int, val sorted: IntArray, val n: Int)

    private fun ops(r: Random, count: Int): List<Op> = List(count) {
        when (val kind = r.nextInt(4)) {
            // DirectWeaver reads noisy numbers with 1, 5 and 10 bits; 0 is the oracle's own edge (decodeBits(0)).
            0 -> Op(kind, r.nextInt(0, 17), signed(r).let { if (it < 0) -it else it }, 0, IntArray(0), 0)
            1 -> Op(kind, r.nextInt(0, 15), signed(r), 0, IntArray(0), 0)
            2 -> {
                // Predictors above 1023 reach decodePredictedValue's shift loop. Below 2^25, so the encoder's noisy-bit
                // count stays at most 24: the oracle's own encode/decode pair does not round-trip from 25 (predictor
                // 2^26 - 1, checked against the -ro jar), and its decode then leaves the stream misaligned for every
                // later op. Seen at 9fe7a70's bound, 2^28: seed 1 op 607, then every round trip after it.
                val predictor = signed(r) shr r.nextInt(3, 8)
                Op(kind, 0, predictor + (signed(r) shr r.nextInt(4, 20)), predictor, IntArray(0), 0)
            }
            else -> {
                // Sorted node ids below 2^30, as DirectWeaver reads them (nextbitpos 29); runs of equal ids and n = 0 too.
                val n = if (r.nextInt(10) == 0) 0 else r.nextInt(1, 200)
                var x = r.nextInt(1 shl 29)
                val gap = 1 shl r.nextInt(0, 16)
                val a = IntArray(maxOf(n, 1)) { if (r.nextInt(8) == 0) x else { x += r.nextInt(1, gap + 1); x } }
                a.sort()
                for (i in a.indices) a[i] = minOf(a[i], (1 shl 30) - 1)
                Op(kind, 0, 0, 0, a, n)
            }
        }
    }

    @Test
    fun oracleEncodedStreamsDecodeIdentically() {
        var compared = 0
        for (seed in 1..20) {
            val r = Random(seed)
            val ops = ops(r, 2_000)
            val buf = ByteArray(1 shl 21)
            val enc = JStatCoderContext(buf)
            for (op in ops) {
                when (op.kind) {
                    0 -> enc.encodeNoisyNumber(op.value, op.noisybits)
                    1 -> enc.encodeNoisyDiff(op.value, op.noisybits)
                    2 -> enc.encodePredictedValue(op.value, op.predictor)
                    else -> enc.encodeSortedArray(op.sorted, 0, op.n, 0x20000000, 0)
                }
            }
            enc.closeAndGetEncodedLength()
            val dj = JStatCoderContext(buf)
            val dk = StatCoderContext(buf)
            for ((i, op) in ops.withIndex()) {
                val at = "seed $seed op $i kind ${op.kind}"
                when (op.kind) {
                    0 -> {
                        val vk = dk.decodeNoisyNumber(op.noisybits)
                        assertEquals(dj.decodeNoisyNumber(op.noisybits), vk, at)
                        if (op.noisybits > 0) assertEquals(op.value, vk, "$at: round trip")
                    }
                    1 -> {
                        val vk = dk.decodeNoisyDiff(op.noisybits)
                        assertEquals(dj.decodeNoisyDiff(op.noisybits), vk, at)
                        assertEquals(op.value, vk, "$at: round trip")
                    }
                    2 -> {
                        val vk = dk.decodePredictedValue(op.predictor)
                        assertEquals(dj.decodePredictedValue(op.predictor), vk, at)
                        assertEquals(op.value, vk, "$at: round trip")
                    }
                    else -> {
                        val aj = IntArray(op.n + 3) { -1 }
                        val ak = IntArray(op.n + 3) { -1 }
                        dj.decodeSortedArray(aj, 1, op.n, 29, 0)
                        dk.decodeSortedArray(ak, 1, op.n, 29, 0)
                        assertContentEquals(aj, ak, at)
                        assertContentEquals(op.sorted.copyOf(op.n), ak.copyOfRange(1, op.n + 1), "$at: round trip")
                    }
                }
                assertEquals(dj.readingBitPosition, dk.getReadingBitPosition(), "$at: reading position")
                compared++
            }
        }
        assertEquals(40_000, compared)
    }
}
