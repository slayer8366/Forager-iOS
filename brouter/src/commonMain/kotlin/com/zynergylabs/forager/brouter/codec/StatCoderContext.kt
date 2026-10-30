package com.zynergylabs.forager.brouter.codec

import com.zynergylabs.forager.brouter.util.BitCoderContext

/** BRouter 1.7.10's `btools.codec.StatCoderContext`, the decoder half. STUB (step 3, tests first). */
internal class StatCoderContext(ab: ByteArray) : BitCoderContext(ab) {
    fun decodeNoisyNumber(noisybits: Int): Int = TODO("step 3: decodeNoisyNumber")

    fun decodeNoisyDiff(noisybits: Int): Int = TODO("step 3: decodeNoisyDiff")

    fun decodePredictedValue(predictor: Int): Int = TODO("step 3: decodePredictedValue")

    fun decodeSortedArray(values: IntArray, offset: Int, subsize: Int, nextbitpos: Int, value: Int): Unit =
        TODO("step 3: decodeSortedArray")

    companion object {
        internal val noisy_bits: IntArray get() = TODO("step 3: noisy_bits")
    }
}
