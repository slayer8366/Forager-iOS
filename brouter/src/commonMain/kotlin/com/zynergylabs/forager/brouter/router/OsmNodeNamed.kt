package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.mapaccess.MatchedWaypoint
import com.zynergylabs.forager.brouter.mapaccess.OsmNode

/**
 * Port of BRouter 1.7.10's `btools.router.OsmNodeNamed`: a waypoint or a nogo point. T2 keeps its fields and two
 * constructors only (`03-reach/out/T2/port-list.tsv`). Forager passes no nogos, so the nogo methods are named
 * "unsupported" here (RECORD -74), never a plausible value: `distanceWithinRadius` (reached from
 * `RoutingContext.calcDistance` only for a weighted nogo circle) and `decodeNogo`. `toString` is not ported.
 */
internal open class OsmNodeNamed : OsmNode {
    var name: String? = null
    var radius: Double = 0.0 // radius of nogopoint (in meters)
    var nogoWeight: Double = 0.0 // weight for nogopoint
    var isNogo: Boolean = false
    var wpttype: Byte = MatchedWaypoint.WAYPOINT_TYPE_SHAPING

    constructor()

    constructor(n: OsmNode) : super(n.ilon, n.ilat)

    @Suppress("UNUSED_PARAMETER")
    fun distanceWithinRadius(lon1: Int, lat1: Int, lon2: Int, lat2: Int, totalSegmentLength: Double): Double =
        throw UnsupportedOperationException("unsupported: OsmNodeNamed.distanceWithinRadius (weighted nogo)")

    companion object {
        @Suppress("UNUSED_PARAMETER")
        fun decodeNogo(s: String): OsmNodeNamed = throw UnsupportedOperationException("unsupported: OsmNodeNamed.decodeNogo")
    }
}
