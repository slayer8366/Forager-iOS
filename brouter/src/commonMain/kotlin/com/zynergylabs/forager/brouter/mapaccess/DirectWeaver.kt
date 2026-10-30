package com.zynergylabs.forager.brouter.mapaccess

import com.zynergylabs.forager.brouter.codec.DataBuffers
import com.zynergylabs.forager.brouter.codec.StatCoderContext
import com.zynergylabs.forager.brouter.codec.TagValueValidator
import com.zynergylabs.forager.brouter.codec.WaypointMatcher
import com.zynergylabs.forager.brouter.util.ByteDataWriter

/** Port of BRouter 1.7.10's `btools.mapaccess.DirectWeaver`. Step 5b's stub (item I): signatures only. */
internal class DirectWeaver(
    bc: StatCoderContext,
    dataBuffers: DataBuffers,
    lonIdx: Int,
    latIdx: Int,
    divisor: Int,
    wayValidator: TagValueValidator?,
    waypointMatcher: WaypointMatcher?,
    hollowNodes: OsmNodesMap,
) : ByteDataWriter(null) {
    init {
        TODO("step 5b")
    }

    fun expandId(id32: Int): Long = TODO("step 5b")
}
