package com.zynergylabs.forager.brouter.util

/**
 * Port of btools/util/ByteArrayUnifier.java's one kept method, the constructor (T2's port list). The class implements
 * `IByteArrayUnifier`, so `unify` must exist; it is not on the port list, so it refuses as unsupported rather than
 * answering. Whether `OsmNode.java:134`'s `readUnified` reaches it on Forager's tiles is a step 5b question.
 */
internal class ByteArrayUnifier(size: Int, validateImmutability: Boolean) : IByteArrayUnifier {
    private val size: Int = size
    private val byteArrayCache: Array<ByteArray?> = arrayOfNulls(size)
    private val crcCrosscheck: IntArray? = if (validateImmutability) IntArray(size) else null

    override fun unify(ab: ByteArray, offset: Int, len: Int): ByteArray =
        throw UnsupportedOperationException(
            "unsupported: ByteArrayUnifier.unify is not on T2's port list (step 2a); see OsmNode.java:134 for step 5b",
        )
}
