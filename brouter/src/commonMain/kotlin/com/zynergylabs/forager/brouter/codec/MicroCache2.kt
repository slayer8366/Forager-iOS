package com.zynergylabs.forager.brouter.codec

import com.zynergylabs.forager.brouter.util.IByteArrayUnifier

/** STUB (step 5d, item 3): BRouter 1.7.10's `btools.codec.MicroCache2`, the reached methods' signatures only. */
internal class MicroCache2(
    bc: StatCoderContext,
    dataBuffers: DataBuffers,
    lonIdx: Int,
    latIdx: Int,
    divisor: Int,
    wayValidator: TagValueValidator?,
    waypointMatcher: WaypointMatcher?,
) : MicroCache(null) {
    init {
        faid = IntArray(0)
        fapos = IntArray(0)
        init(0)
    }

    fun readUnified(len: Int, u: IByteArrayUnifier): ByteArray = ByteArray(0)

    override fun expandId(id32: Int): Long = 0L

    override fun shrinkId(id64: Long): Int = 0
}
