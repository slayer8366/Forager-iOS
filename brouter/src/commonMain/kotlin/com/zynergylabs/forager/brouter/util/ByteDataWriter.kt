package com.zynergylabs.forager.brouter.util

/**
 * Port of btools/util/ByteDataWriter.java (by ab): fast data-writing to a byte-array. Kept: the constructor and the two
 * var-length writers T2 reaches.
 */
internal open class ByteDataWriter(byteArray: ByteArray?) : ByteDataReader(byteArray) {
    // Step 5d (RECORD -70): the eight writers `MicroCache2`'s decoder reaches (`ByteDataWriter.java:14-108`).
    fun writeInt(v: Int) {
        val ab = ab!!
        ab[aboffset++] = ((v shr 24) and 0xff).toByte()
        ab[aboffset++] = ((v shr 16) and 0xff).toByte()
        ab[aboffset++] = ((v shr 8) and 0xff).toByte()
        ab[aboffset++] = (v and 0xff).toByte()
    }

    fun writeBoolean(v: Boolean) {
        ab!![aboffset++] = (if (v) 1 else 0).toByte()
    }

    fun writeShort(v: Int) {
        val ab = ab!!
        ab[aboffset++] = ((v shr 8) and 0xff).toByte()
        ab[aboffset++] = (v and 0xff).toByte()
    }

    /** Java's `System.arraycopy(sa, offset, ab, aboffset, len)`, which throws before copying when a range is short. */
    fun write(sa: ByteArray, offset: Int, len: Int) {
        val ab = ab!!
        if (len < 0 || offset < 0 || offset + len > sa.size || aboffset < 0 || aboffset + len > ab.size) {
            throw IndexOutOfBoundsException("arraycopy: $len bytes from $offset of byte[${sa.size}] to $aboffset of byte[${ab.size}]")
        }
        sa.copyInto(ab, aboffset, offset, offset + len)
        aboffset += len
    }

    fun writeVarBytes(sa: ByteArray?) {
        if (sa == null) {
            writeVarLengthUnsigned(0)
        } else {
            val len = sa.size
            writeVarLengthUnsigned(len)
            write(sa, 0, len)
        }
    }

    fun writeModeAndDesc(isReverse: Boolean, sa: ByteArray?) {
        val len = sa?.size ?: 0
        val sizecode = (len shl 1) or (if (isReverse) 1 else 0)
        writeVarLengthUnsigned(sizecode)
        if (len > 0) {
            write(sa!!, 0, len)
        }
    }

    /** Reserves one byte and returns its offset, for [injectSize] to fill with a size prefix. */
    fun writeSizePlaceHolder(): Int = aboffset++

    fun injectSize(sizeoffset: Int) {
        val ab = ab!!
        var size = 0
        val datasize = aboffset - sizeoffset - 1
        var v = datasize
        do {
            v = v shr 7
            size++
        } while (v != 0)
        if (size > 1) { // doesn't fit -> shift the data after the placeholder
            // Java's System.arraycopy copies an overlapping range as if through a temporary array; copyInto does too.
            if (sizeoffset + size + datasize > ab.size) {
                throw IndexOutOfBoundsException("arraycopy: last destination index ${sizeoffset + size + datasize} out of bounds for byte[${ab.size}]")
            }
            ab.copyInto(ab, sizeoffset + size, sizeoffset + 1, sizeoffset + 1 + datasize)
        }
        aboffset = sizeoffset
        writeVarLengthUnsigned(datasize)
        aboffset = sizeoffset + size + datasize
    }

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
