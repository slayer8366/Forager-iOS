package com.zynergylabs.forager.brouter.util

// Stub (item I, step 2a, "tests first, over stubs"): btools/util/BitCoderContext.java's kept methods.
internal open class BitCoderContext(ab: ByteArray) {
    fun reset(ab: ByteArray): Unit = TODO("step 2a stub")
    fun reset(): Unit = TODO("step 2a stub")
    fun encodeVarBits2(value: Int): Unit = TODO("step 2a stub")
    fun encodeVarBits(value: Int): Unit = TODO("step 2a stub")
    fun decodeVarBits2(): Int = TODO("step 2a stub")
    fun decodeVarBits(): Int = TODO("step 2a stub")
    fun encodeBit(value: Boolean): Unit = TODO("step 2a stub")
    fun decodeBit(): Boolean = TODO("step 2a stub")
    fun encodeBounded(max: Int, value: Int): Unit = TODO("step 2a stub")
    fun decodeBounded(max: Int): Int = TODO("step 2a stub")
    fun decodeBits(count: Int): Int = TODO("step 2a stub")
    fun decodeBitsReverse(count: Int): Int = TODO("step 2a stub")
    fun closeAndGetEncodedLength(): Int = TODO("step 2a stub")
    fun getWritingBitPosition(): Int = TODO("step 2a stub")
    fun getReadingBitPosition(): Int = TODO("step 2a stub")

    companion object {
        val vl_values: IntArray get() = TODO("step 2a stub")
        val vl_length: IntArray get() = TODO("step 2a stub")
    }
}
