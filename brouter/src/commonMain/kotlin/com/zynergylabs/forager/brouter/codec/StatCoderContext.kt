package com.zynergylabs.forager.brouter.codec

import com.zynergylabs.forager.brouter.util.BitCoderContext

/**
 * Port of BRouter 1.7.10's `btools.codec.StatCoderContext`, the decoder half. The encoders (`encode*`) and the bit
 * statistics (`assignBits`, `getBitReport`, `statsPerName`, `lastbitpos`) are write-side only and not ported; the tests
 * write their streams with the oracle's own encoder.
 */
internal class StatCoderContext(ab: ByteArray) : BitCoderContext(ab) {
    /** Java's `decodeNoisyNumber`: the low [noisybits] bits plain, the rest var-bits. */
    fun decodeNoisyNumber(noisybits: Int): Int {
        val value = decodeBits(noisybits)
        return value or (decodeVarBits() shl noisybits)
    }

    /** Java's `decodeNoisyDiff`: as [decodeNoisyNumber], offset by half the noisy range, with a sign bit when non-zero. */
    fun decodeNoisyDiff(noisybits: Int): Int {
        var value = 0
        if (noisybits > 0) {
            value = decodeBits(noisybits) - (1 shl (noisybits - 1))
        }
        var val2 = decodeVarBits() shl noisybits
        if (val2 != 0) {
            if (decodeBit()) {
                val2 = -val2
            }
        }
        return value + val2
    }

    /** Java's `decodePredictedValue`. The loop ends: `p` is non-negative unless predictor is Int.MIN_VALUE, and then it does not run. */
    fun decodePredictedValue(predictor: Int): Int {
        var p = if (predictor < 0) -predictor else predictor
        var noisybits = 0
        while (p > 1023) {
            noisybits++
            p = p shr 1
        }
        return predictor + decodeNoisyDiff(noisybits + noisy_bits[p])
    }

    /**
     * Java's `decodeSortedArray`: [subsize] sorted values into [values] from [offset], the bits from [nextbitpos] down,
     * over the prefix [value]. The recursion ends: each level lowers `nextbitpos` by one and stops below 0.
     */
    fun decodeSortedArray(values: IntArray, offset: Int, subsize: Int, nextbitpos: Int, value: Int) {
        var v = value // Java reassigns its parameters
        if (subsize == 1) { // last-choice shortcut
            if (nextbitpos >= 0) {
                v = v or decodeBitsReverse(nextbitpos + 1)
            }
            values[offset] = v
            return
        }
        if (nextbitpos < 0) {
            var o = offset
            var s = subsize
            while (s-- > 0) {
                values[o++] = v
            }
            return
        }

        val size1 = decodeBounded(subsize)
        val size2 = subsize - size1

        if (size1 > 0) {
            decodeSortedArray(values, offset, size1, nextbitpos - 1, v)
        }
        if (size2 > 0) {
            decodeSortedArray(values, offset + size1, size2, nextbitpos - 1, v or (1 shl nextbitpos))
        }
    }

    companion object {
        // Java: private static final int[1024], filled in a static block; internal here so the test can read it.
        internal val noisy_bits: IntArray = IntArray(1024).also { bits ->
            for (i in 0 until 1024) {
                var p = i
                var noisybits = 0
                while (p > 2) { // ends: p is 0..1023 and halves
                    noisybits++
                    p = p shr 1
                }
                bits[i] = noisybits
            }
        }
    }
}
