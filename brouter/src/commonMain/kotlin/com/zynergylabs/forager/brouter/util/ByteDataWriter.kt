package com.zynergylabs.forager.brouter.util

/**
 * Port of btools/util/ByteDataWriter.java (by ab): fast data-writing to a byte-array. Kept: the constructor and the two
 * var-length writers T2 reaches.
 */
internal open class ByteDataWriter(byteArray: ByteArray?) : ByteDataReader(byteArray) {
    fun writeVarLengthSigned(v: Int) {
        writeVarLengthUnsigned(if (v < 0) ((-v) shl 1) or 1 else v shl 1)
    }

    fun writeVarLengthUnsigned(v: Int) {
        val ab = ab!!
        var rest = v // Java reassigns its parameter; a Kotlin parameter is a val
        var i7 = rest and 0x7f
        rest = rest ushr 7
        if (rest == 0) {
            ab[aboffset++] = i7.toByte()
            return
        }
        ab[aboffset++] = (i7 or 0x80).toByte()

        i7 = rest and 0x7f
        rest = rest ushr 7
        if (rest == 0) {
            ab[aboffset++] = i7.toByte()
            return
        }
        ab[aboffset++] = (i7 or 0x80).toByte()

        i7 = rest and 0x7f
        rest = rest ushr 7
        if (rest == 0) {
            ab[aboffset++] = i7.toByte()
            return
        }
        ab[aboffset++] = (i7 or 0x80).toByte()

        i7 = rest and 0x7f
        rest = rest ushr 7
        if (rest == 0) {
            ab[aboffset++] = i7.toByte()
            return
        }
        ab[aboffset++] = (i7 or 0x80).toByte()

        ab[aboffset++] = rest.toByte()
    }
}
