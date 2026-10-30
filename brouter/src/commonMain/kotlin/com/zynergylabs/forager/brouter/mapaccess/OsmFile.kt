package com.zynergylabs.forager.brouter.mapaccess

import com.zynergylabs.forager.brouter.codec.DataBuffers
import com.zynergylabs.forager.brouter.codec.MicroCache
import com.zynergylabs.forager.brouter.codec.TagValueValidator
import com.zynergylabs.forager.brouter.codec.WaypointMatcher

/** Port of BRouter 1.7.10's `btools.mapaccess.OsmFile`: one degree square of a `.rd5` file. */
internal class OsmFile(rafile: PhysicalFile?, var lonDegree: Int, var latDegree: Int, dataBuffers: DataBuffers) {
    var filename: String? = null
    var elevationType: Byte = 3

    /** Java's private `posIdx`; internal so the tests compare it with the oracle's. */
    internal var posIdx: IntArray? = null

    init {
        TODO("item I step 5a")
    }

    fun hasData(): Boolean = TODO("item I step 5a")

    fun getMicroCache(ilon: Int, ilat: Int): MicroCache? = TODO("item I step 5a")

    fun createMicroCache(
        ilon: Int,
        ilat: Int,
        dataBuffers: DataBuffers,
        wayValidator: TagValueValidator?,
        waypointMatcher: WaypointMatcher?,
        hollowNodes: OsmNodesMap?,
    ): MicroCache = TODO("item I step 5a")

    fun createMicroCache(
        lonIdx: Int,
        latIdx: Int,
        dataBuffers: DataBuffers,
        wayValidator: TagValueValidator?,
        waypointMatcher: WaypointMatcher?,
        reallyDecode: Boolean,
        hollowNodes: OsmNodesMap?,
    ): MicroCache? = TODO("item I step 5a")

    fun getDataInputForSubIdx(subIdx: Int, iobuffer: ByteArray): Int = TODO("item I step 5a")

    internal fun setGhostState(): Long = TODO("item I step 5a")

    internal fun collectAll(): Long = TODO("item I step 5a")

    internal fun cleanGhosts(): Long = TODO("item I step 5a")

    internal fun clean(all: Boolean): Unit = TODO("item I step 5a")
}
