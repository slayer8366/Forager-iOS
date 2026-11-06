package com.zynergylabs.forager.brouter.mapaccess

import com.zynergylabs.forager.brouter.codec.WaypointMatcher
import com.zynergylabs.forager.brouter.util.LibmSeam

/**
 * Port of BRouter 1.7.10's `btools.mapaccess.WaypointMatcherImpl` (step 5c, item I). STUB: every method throws, so that
 * `WaypointMatchTest` is seen failing before the port exists.
 */
internal class WaypointMatcherImpl(
    private val libm: LibmSeam,
    waypoints: MutableList<MatchedWaypoint>,
    maxDistance: Double,
    islandPairs: OsmNodePairSet,
) : WaypointMatcher {
    var useDynamicRange: Boolean = false

    override fun start(ilonStart: Int, ilatStart: Int, ilonTarget: Int, ilatTarget: Int, useAsStartWay: Boolean): Boolean =
        TODO("step 5c stub")

    override fun transferNode(ilon: Int, ilat: Int): Unit = TODO("step 5c stub")

    override fun end(): Unit = TODO("step 5c stub")

    override fun hasMatch(lon: Int, lat: Int): Boolean = TODO("step 5c stub")
}
