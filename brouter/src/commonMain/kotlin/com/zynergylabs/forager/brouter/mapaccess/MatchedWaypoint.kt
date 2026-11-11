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
        fun readFromStream(dis: okio.BufferedSource): MatchedWaypoint {
            val mwp = MatchedWaypoint()
            val node1 = OsmNode()
            val node2 = OsmNode()
            val crosspoint = OsmNode()
            val waypoint = OsmNode()
            mwp.node1 = node1
            mwp.node2 = node2
            mwp.crosspoint = crosspoint
            mwp.waypoint = waypoint

            node1.ilat = dis.readInt()
            node1.ilon = dis.readInt()
            node2.ilat = dis.readInt()
            node2.ilon = dis.readInt()
            crosspoint.ilat = dis.readInt()
            crosspoint.ilon = dis.readInt()
            waypoint.ilat = dis.readInt()
            waypoint.ilon = dis.readInt()
            mwp.radius = Double.fromBits(dis.readLong())
            mwp.wpttype = dis.readByte()
            val len = dis.readShort().toInt()
            val bytes = ByteArray(len)
            dis.readFully(bytes)
            // Java's `new String(bytes)` decodes in the default charset, UTF-8 from JDK 18 (the oracle runs on 21).
            mwp.name = bytes.decodeToString()
            return mwp
        }
    }
}
