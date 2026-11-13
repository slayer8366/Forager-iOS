package com.zynergylabs.forager.brouter.util

/**
 * Port of btools/util/ByteArrayUnifier.java: the constructor and `unify(ab, offset, len)` (`ByteArrayUnifier.java:26-53`),
 * which `OsmNode.parseNodeBody2` reaches through `MicroCache2.readUnified` for node descriptions (step 5d, RECORD -74).
 * The one-argument `unify(ab)` is not ported: nothing in the port calls it.
 */
internal class ByteArrayUnifier(size: Int, validateImmutability: Boolean) : IByteArrayUnifier {
    private val size: Int = size
    private val byteArrayCache: Array<ByteArray?> = arrayOfNulls(size)
    private val crcCrosscheck: IntArray? = if (validateImmutability) IntArray(size) else null

    override fun unify(ab: ByteArray, offset: Int, len: Int): ByteArray {
        val crc = Crc32.crc(ab, offset, len)
        val idx = (crc and 0xfffffff) % size
        val abc = byteArrayCache[idx]
        if (abc != null && abc.size == len) {
            var i = 0
            while (i < len) {
                if (ab[offset + i] != abc[i]) {
                    break
                }
                i++
            }
            if (i == len) {
                return abc
            }
        }
        if (crcCrosscheck != null) {
            val abold = byteArrayCache[idx]
            if (abold != null) {
                val crcold = Crc32.crc(abold, 0, abold.size)
                if (crcold != crcCrosscheck[idx]) {
                    throw IllegalArgumentException("ByteArrayUnifier: immutablity validation failed!")
                }
            }
            crcCrosscheck[idx] = crc
        }
        val nab = ByteArray(len)
        ab.copyInto(nab, 0, offset, offset + len)
        byteArrayCache[idx] = nab
        return nab
    }
}
