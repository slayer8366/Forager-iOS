package com.zynergylabs.forager.brouter.codec

/**
 * Port of BRouter 1.7.10's `btools.codec.NoisyDiffCoder`, the decoder half: the decoding constructor reads the noisy-bit
 * count, and [decodeSignedValue] reads each value with it. The encoder's passes (`encodeSignedValue`,
 * `encodeDictionary`, `count`, `tot`, `freqs`, `pass`) and the no-argument constructor are write-side only.
 */
internal class NoisyDiffCoder(bc: StatCoderContext) {
    private val noisybits: Int = bc.decodeVarBits()
    private val bc: StatCoderContext = bc

    fun decodeSignedValue(): Int = bc.decodeNoisyDiff(noisybits)
}
