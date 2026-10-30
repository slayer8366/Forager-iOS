package com.zynergylabs.forager.brouter.codec

import com.zynergylabs.forager.brouter.util.ByteDataWriter
import kotlin.jvm.JvmField

/**
 * Port of BRouter 1.7.10's `btools.codec.MicroCache`, its reached methods (`MicroCache.java:21-205`). Not ported, as
 * T2 reaches none of them: `finishNode`, `discardNode`, `getIdForIndex`, `expandId`, `isInternal`, `encodeMicroCache`,
 * `compareWith`, `calcDelta`, `addDelta`.
 */
internal open class MicroCache protected constructor(ab: ByteArray?) : ByteDataWriter(ab) {
    protected var faid: IntArray? = null
    protected var fapos: IntArray? = null

    // A field, as in Java: a Kotlin property's getter would clash with getSize() on the JVM.
    @JvmField
    protected var size: Int = 0

    private var delcount = 0
    private var delbytes = 0
    private var p2size = 0 // next power of 2 of size

    // cache control: a virgin cache can be put to ghost state for later recovery
    var virgin: Boolean = true
    var ghost: Boolean = false

    /**
     * Java's `init`. The loop ends for any size >= 0 (p2size halves to 0 at the latest); every caller passes an array
     * length or `size - delcount`, never below 0. A negative size would not end, as in Java.
     */
    protected fun init(size: Int) {
        this.size = size
        delcount = 0
        delbytes = 0
        p2size = 0x40000000
        while (p2size > size) {
            p2size = p2size shr 1
        }
    }

    fun getSize(): Int = size

    fun getDataSize(): Int = ab?.size ?: 0

    /** Java's `getAndClear`: a binary search for the id; the loop ends as `offset` halves to 0. */
    fun getAndClear(id64: Long): Boolean {
        if (size == 0) {
            return false
        }
        val id = shrinkId(id64)
        val a = faid!!
        val fapos = fapos!!
        var offset = p2size
        var n = 0
        while (offset > 0) {
            val nn = n + offset
            if (nn < size && a[nn] <= id) {
                n = nn
            }
            offset = offset shr 1
        }
        if (a[n] == id) {
            if ((fapos[n] and 0x80000000.toInt()) == 0) {
                aboffset = startPos(n)
                aboffsetEnd = fapos[n]
                fapos[n] = fapos[n] or 0x80000000.toInt() // mark deleted
                delbytes += aboffsetEnd - aboffset
                delcount++
                return true
            }
            // else: marked as deleted; Java's RuntimeException here is commented out
        }
        return false
    }

    protected fun startPos(n: Int): Int = if (n > 0) fapos!![n - 1] and 0x7fffffff else 0

    /** Java's `collect`. When nothing is left, `faid` and `fapos` are dropped and `ab` is kept, as in Java. */
    fun collect(threshold: Int): Int {
        if (delcount <= threshold) {
            return 0
        }
        virgin = false
        val nsize = size - delcount
        if (nsize == 0) {
            faid = null
            fapos = null
        } else {
            val ab = ab!!
            val faid = faid!!
            val fapos = fapos!!
            val nfaid = IntArray(nsize)
            val nfapos = IntArray(nsize)
            var idx = 0
            val nab = ByteArray(ab.size - delbytes)
            var nabOff = 0
            for (i in 0 until size) {
                val pos = fapos[i]
                if ((pos and 0x80000000.toInt()) == 0) {
                    val start = startPos(i)
                    val end = fapos[i]
                    val len = end - start
                    ab.copyInto(nab, nabOff, start, start + len)
                    nfaid[idx] = faid[i]
                    nabOff += len
                    nfapos[idx] = nabOff
                    idx++
                }
            }
            this.faid = nfaid
            this.fapos = nfapos
            this.ab = nab
        }
        val deleted = delbytes
        init(nsize)
        return deleted
    }

    fun unGhost() {
        ghost = false
        delcount = 0
        delbytes = 0
        for (i in 0 until size) {
            fapos!![i] = fapos!![i] and 0x7fffffff // clear deleted flags
        }
    }

    open fun shrinkId(id64: Long): Int {
        throw IllegalArgumentException("shrinkId for empty cache")
    }

    companion object {
        var debug: Boolean = false

        val emptyNonVirgin: MicroCache = MicroCache(null).also { it.virgin = false }

        fun emptyCache(): MicroCache = MicroCache(null) // Java: "TODO: singleton?"
    }
}
