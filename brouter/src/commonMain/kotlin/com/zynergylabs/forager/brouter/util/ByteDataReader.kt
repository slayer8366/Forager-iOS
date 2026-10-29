package com.zynergylabs.forager.brouter.util

/**
 * Port of btools/util/ByteDataReader.java (by ab): fast data-reading from a byte-array. Kept: the methods T2 reaches.
 * `ab` is nullable because Java's constructors and `reset` accept null; a read on a null array throws, as in Java.
 */
internal open class ByteDataReader {
    protected var ab: ByteArray?
    protected var aboffset: Int = 0
    protected var aboffsetEnd: Int

    constructor(byteArray: ByteArray?) {
        ab = byteArray
        aboffsetEnd = byteArray?.size ?: 0
    }

    constructor(byteArray: ByteArray?, offset: Int) {
        ab = byteArray
        aboffset = offset
        aboffsetEnd = byteArray?.size ?: 0
    }

    fun reset(byteArray: ByteArray?) {
        ab = byteArray
        aboffset = 0
        aboffsetEnd = byteArray?.size ?: 0
    }

    fun readInt(): Int {
        val ab = ab!!
        val i3 = ab[aboffset++].toInt() and 0xff
        val i2 = ab[aboffset++].toInt() and 0xff
        val i1 = ab[aboffset++].toInt() and 0xff
        val i0 = ab[aboffset++].toInt() and 0xff
        return (i3 shl 24) + (i2 shl 16) + (i1 shl 8) + i0
    }

    fun readLong(): Long {
        val ab = ab!!
        val i7 = (ab[aboffset++].toInt() and 0xff).toLong()
        val i6 = (ab[aboffset++].toInt() and 0xff).toLong()
        val i5 = (ab[aboffset++].toInt() and 0xff).toLong()
        val i4 = (ab[aboffset++].toInt() and 0xff).toLong()
        val i3 = (ab[aboffset++].toInt() and 0xff).toLong()
        val i2 = (ab[aboffset++].toInt() and 0xff).toLong()
        val i1 = (ab[aboffset++].toInt() and 0xff).toLong()
        val i0 = (ab[aboffset++].toInt() and 0xff).toLong()
        return (i7 shl 56) + (i6 shl 48) + (i5 shl 40) + (i4 shl 32) + (i3 shl 24) + (i2 shl 16) + (i1 shl 8) + i0
    }

    fun readByte(): Byte {
        val i0 = ab!![aboffset++].toInt() and 0xff
        return i0.toByte()
    }

    fun readVarLengthSigned(): Int {
        val v = readVarLengthUnsigned()
        return if ((v and 1) == 0) v shr 1 else -(v shr 1)
    }

    fun readVarLengthUnsigned(): Int {
        val ab = ab!!
        var b: Byte
        b = ab[aboffset++]
        var v = b.toInt() and 0x7f
        if (b >= 0) return v
        b = ab[aboffset++]
        v = v or ((b.toInt() and 0x7f) shl 7)
        if (b >= 0) return v
        b = ab[aboffset++]
        v = v or ((b.toInt() and 0x7f) shl 14)
        if (b >= 0) return v
        b = ab[aboffset++]
        v = v or ((b.toInt() and 0x7f) shl 21)
        if (b >= 0) return v
        b = ab[aboffset++]
        v = v or ((b.toInt() and 0xf) shl 28)
        return v
    }

    fun hasMoreData(): Boolean {
        return aboffset < aboffsetEnd
    }
}
