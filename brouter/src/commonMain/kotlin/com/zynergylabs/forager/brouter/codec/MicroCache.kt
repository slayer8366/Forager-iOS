package com.zynergylabs.forager.brouter.codec

import com.zynergylabs.forager.brouter.util.ByteDataWriter

/** BRouter 1.7.10's `btools.codec.MicroCache`, its reached methods. STUB (step 3, tests first). */
internal open class MicroCache protected constructor(ab: ByteArray?) : ByteDataWriter(ab) {
    protected var faid: IntArray? = null
    protected var fapos: IntArray? = null
    protected var size: Int = 0
    var virgin: Boolean = true
    var ghost: Boolean = false

    protected fun init(size: Int): Unit = TODO("step 3: init")

    fun getSize(): Int = TODO("step 3: getSize")

    fun getDataSize(): Int = TODO("step 3: getDataSize")

    fun getAndClear(id64: Long): Boolean = TODO("step 3: getAndClear")

    protected fun startPos(n: Int): Int = TODO("step 3: startPos")

    fun collect(threshold: Int): Int = TODO("step 3: collect")

    fun unGhost(): Unit = TODO("step 3: unGhost")

    open fun shrinkId(id64: Long): Int = TODO("step 3: shrinkId")

    companion object {
        var debug: Boolean = false
        val emptyNonVirgin: MicroCache get() = TODO("step 3: emptyNonVirgin")

        fun emptyCache(): MicroCache = TODO("step 3: emptyCache")
    }
}
