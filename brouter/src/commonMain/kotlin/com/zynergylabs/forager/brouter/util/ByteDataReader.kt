package com.zynergylabs.forager.brouter.util

// Stub (item I, step 2a): btools/util/ByteDataReader.java's kept methods.
internal open class ByteDataReader {
    constructor(byteArray: ByteArray?)
    constructor(byteArray: ByteArray?, offset: Int)

    fun reset(byteArray: ByteArray?): Unit = TODO("step 2a stub")
    fun readInt(): Int = TODO("step 2a stub")
    fun readLong(): Long = TODO("step 2a stub")
    fun readByte(): Byte = TODO("step 2a stub")
    fun readVarLengthSigned(): Int = TODO("step 2a stub")
    fun readVarLengthUnsigned(): Int = TODO("step 2a stub")
    fun hasMoreData(): Boolean = TODO("step 2a stub")
}
