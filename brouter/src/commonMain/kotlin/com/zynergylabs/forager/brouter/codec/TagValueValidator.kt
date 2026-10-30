package com.zynergylabs.forager.brouter.codec

/**
 * BRouter 1.7.10's `btools.codec.TagValueValidator` (`TagValueValidator.java`). Four of its five methods: T2 reaches
 * `accessType`, `unify`, `isLookupIdxUsed` and `checkStartWay`; `setDecodeForbidden` is not reached and is not ported.
 */
internal interface TagValueValidator {
    /** 0 = nothing, 1 = no matching, 2 = normal. */
    fun accessType(tagValueSet: ByteArray): Int

    fun unify(tagValueSet: ByteArray, offset: Int, len: Int): ByteArray

    fun isLookupIdxUsed(idx: Int): Boolean

    fun checkStartWay(ab: ByteArray): Boolean
}
