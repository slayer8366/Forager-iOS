package com.zynergylabs.forager.brouter.util

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import btools.util.BitCoderContext as JBitCoderContext

/**
 * Item I, step 2a: seeded random streams through the oracle's coder (BRouter 1.7.10's `btools.util.BitCoderContext`)
 * and the port's, both ways. Every encoded byte, every decoded value and every bit position is compared exactly.
 */
class BitCoderContextTest {
    @Test
    fun varBitsLookupTablesEqualTheOracles() {
        assertContentEquals(JBitCoderContext.vl_values, BitCoderContext.vl_values)
        assertContentEquals(JBitCoderContext.vl_length, BitCoderContext.vl_length)
    }

    /** A value spread that reaches every branch of `decodeVarBits`: lengths up to 12, 13 to 23, 25 to 47, and longer. */
    private fun value(r: Random): Int = when (r.nextInt(6)) {
        0 -> r.nextInt(16)
        1 -> r.nextInt(4096)
        2 -> r.nextInt(1 shl 20)
        3 -> r.nextInt(1 shl 24, Int.MAX_VALUE)
        4 -> r.nextInt() // negatives too: the coder takes any int
        else -> (1 shl r.nextInt(31)) + r.nextInt(-2, 3)
    }

    private class Op(val kind: Int, val max: Int, val value: Int)

    private fun ops(r: Random, n: Int): List<Op> = List(n) {
        when (val kind = r.nextInt(4)) {
            2 -> Op(kind, 0, r.nextInt(2))
            3 -> {
                // Below 2^30: read at BitCoderContext.java:190-197, from max >= 2^30 `im <= max` holds forever (im wraps to MIN,
                // then 0), so the oracle writes until the array ends.
                val max = if (r.nextBoolean()) r.nextInt(64) else r.nextInt(1 shl 30)
                Op(kind, max, if (max == 0) 0 else r.nextInt(max) + r.nextInt(2))
            }
            else -> Op(kind, 0, value(r))
        }
    }

    @Test
    fun encodedStreamsAreByteIdenticalAndDecodeIdenticallyBothWays() {
        for (seed in 1..20) {
            val r = Random(seed)
            val ops = ops(r, 5_000)
            val aJ = ByteArray(200_000)
            val aK = ByteArray(200_000)
            val j = JBitCoderContext(aJ)
            val k = BitCoderContext(aK)
            for ((i, op) in ops.withIndex()) {
                when (op.kind) {
                    0 -> { j.encodeVarBits(op.value); k.encodeVarBits(op.value) }
                    1 -> { j.encodeVarBits2(op.value); k.encodeVarBits2(op.value) }
                    2 -> { j.encodeBit(op.value == 1); k.encodeBit(op.value == 1) }
                    else -> { j.encodeBounded(op.max, op.value); k.encodeBounded(op.max, op.value) }
                }
                assertEquals(j.writingBitPosition, k.getWritingBitPosition(), "seed $seed op $i: writing position")
            }
            assertEquals(j.closeAndGetEncodedLength(), k.closeAndGetEncodedLength(), "seed $seed: encoded length")
            assertContentEquals(aJ, aK, "seed $seed: encoded bytes")

            // Decode the oracle's bytes with the port and the port's bytes with the oracle (equal, as just shown).
            val dj = JBitCoderContext(aK)
            val dk = BitCoderContext(ByteArray(1))
            dk.reset(aJ)
            val rd = Random(seed + 1000)
            for ((i, op) in ops.withIndex()) {
                val (vj, vk) = when (op.kind) {
                    0, 1 -> if (rd.nextBoolean()) dj.decodeVarBits() to dk.decodeVarBits() else dj.decodeVarBits2() to dk.decodeVarBits2()
                    2 -> (if (dj.decodeBit()) 1 else 0) to (if (dk.decodeBit()) 1 else 0)
                    else -> dj.decodeBounded(op.max) to dk.decodeBounded(op.max)
                }
                assertEquals(vj, vk, "seed $seed op $i: decoded value")
                assertEquals(dj.readingBitPosition, dk.getReadingBitPosition(), "seed $seed op $i: reading position")
                // The round trip itself, where the coder's contract covers the input (non-negative, within max).
                if (op.value >= 0 && (op.kind != 3 || op.value <= op.max)) assertEquals(op.value, vk, "seed $seed op $i: round trip")
            }
        }
    }

    /**
     * Random reads, with a reset of both contexts about one op in 250. The resets keep the reading position below about
     * bit 22,000, so the loop ends at its op cap, 1,000,000 per seed (the planner's ruling, RECORD -33): without the cap
     * it never ended (Phase 2 report, step 2a sub-step 2). The position bound stays, as the guard on Java's unguarded
     * `ab[++idx]` reads.
     */
    @Test
    fun rawBitReadsOnRandomBytesMatchTheOracle() {
        for (seed in 1..20) {
            val r = Random(seed)
            val bytes = ByteArray(4_096).also { r.nextBytes(it) }
            val j = JBitCoderContext(bytes)
            val k = BitCoderContext(bytes)
            var i = 0
            while (j.readingBitPosition < (bytes.size - 16) * 8 && i < 1_000_000) {
                val count = r.nextInt(0, 33)
                val (vj, vk) = when (r.nextInt(5)) {
                    0 -> j.decodeBits(count) to k.decodeBits(count)
                    1 -> j.decodeBitsReverse(count) to k.decodeBitsReverse(count)
                    2 -> j.decodeVarBits() to k.decodeVarBits()
                    3 -> (if (j.decodeBit()) 1 else 0) to (if (k.decodeBit()) 1 else 0)
                    else -> {
                        if (r.nextInt(50) == 0) { j.reset(); k.reset() }
                        j.decodeBounded(count) to k.decodeBounded(count)
                    }
                }
                assertEquals(vj, vk, "seed $seed op $i: value")
                assertEquals(j.readingBitPosition, k.getReadingBitPosition(), "seed $seed op $i: reading position")
                i++
            }
            println("rawBitReadsOnRandomBytesMatchTheOracle seed $seed: $i ops, reading position ${j.readingBitPosition}")
        }
    }

    /**
     * The same reads with no reset, so each seed runs to the position bound (about 4,100 ops) and reads the last part of
     * the array, which the first case never reaches (the planner's ruling, RECORD -33). It stops short of `fillBuffer`'s
     * end-of-array branch: the buffer looks ahead at most 31 bits, so the oracle's `idx` stays at or below 4,084 of its
     * `idxMax` 4,095 (step 2a's close, `2-loop/LoopCheck2-out.txt`).
     */
    @Test
    fun rawBitReadsWithoutResetsToThePositionBoundMatchTheOracle() {
        for (seed in 1..20) {
            val r = Random(seed)
            val bytes = ByteArray(4_096).also { r.nextBytes(it) }
            val j = JBitCoderContext(bytes)
            val k = BitCoderContext(bytes)
            var i = 0
            while (j.readingBitPosition < (bytes.size - 16) * 8) {
                val count = r.nextInt(0, 33)
                val (vj, vk) = when (r.nextInt(5)) {
                    0 -> j.decodeBits(count) to k.decodeBits(count)
                    1 -> j.decodeBitsReverse(count) to k.decodeBitsReverse(count)
                    2 -> j.decodeVarBits() to k.decodeVarBits()
                    3 -> (if (j.decodeBit()) 1 else 0) to (if (k.decodeBit()) 1 else 0)
                    else -> j.decodeBounded(count) to k.decodeBounded(count)
                }
                assertEquals(vj, vk, "seed $seed op $i: value")
                assertEquals(j.readingBitPosition, k.getReadingBitPosition(), "seed $seed op $i: reading position")
                i++
            }
            println("rawBitReadsWithoutResetsToThePositionBoundMatchTheOracle seed $seed: $i ops, reading position ${j.readingBitPosition}")
        }
    }
}
