package com.zynergylabs.forager.brouter.mapaccess

/**
 * Port of BRouter 1.7.10's `btools.mapaccess.MatchedWaypoint` (`MatchedWaypoint.java`), its fields (step 5c, item I).
 * `writeToStream` and `readFromStream` are reached only at T0 (`03-reach/out/T0/reached-methods.tsv`), not from
 * Forager's path (T2, T1: absent), and are not ported.
 */
internal class MatchedWaypoint {
    var node1: OsmNode? = null
    var node2: OsmNode? = null
    var crosspoint: OsmNode? = null
    var waypoint: OsmNode? = null
    var correctedpoint: OsmNode? = null
    var name: String? = null // waypoint name used in error messages
    var radius: Double = 0.0 // distance in meter between waypoint and crosspoint
    var wpttype: Byte = WAYPOINT_TYPE_SHAPING
    var indexInTrack: Int = 0
    var directionToNext: Double = -1.0
    var directionDiff: Double = 361.0

    var wayNearest: MutableList<MatchedWaypoint> = ArrayList()
    var hasUpdate: Boolean = false

    companion object {
        const val WAYPOINT_TYPE_SHAPING: Byte = 1 // route next to this point
        const val WAYPOINT_TYPE_MEETING: Byte = 2 // visit this point
        const val WAYPOINT_TYPE_DIRECT: Byte = 3 // from this point go direct to next = beeline routing

        /** `MatchedWaypoint.java:50-72`; session 23, for `OsmTrack.readBinary`'s file branch (RECORD -111). */
        fun readFromStream(dis: okio.BufferedSource): MatchedWaypoint =
            TODO("step 7, session 23 stub: MatchedWaypoint.readFromStream")
    }
}
