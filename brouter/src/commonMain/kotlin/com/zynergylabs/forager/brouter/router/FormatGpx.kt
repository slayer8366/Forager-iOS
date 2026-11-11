package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.mapaccess.MatchedWaypoint
import okio.FileSystem

/**
 * BRouter 1.7.10's `btools.router.FormatGpx`: stubs only (step 7, session 27; RECORD -111, "Port them"), so that
 * `FormatGpxTest` is seen failing over them before the port (RECORD -101).
 */
internal class FormatGpx(rc: RoutingContext?) : Formatter(rc) {
    override fun format(t: OsmTrack): String = TODO("FormatGpx.format: step 7, session 27")

    fun formatAsGpx(sb: StringBuilder, t: OsmTrack): Unit = TODO("FormatGpx.formatAsGpx: step 7, session 27")

    fun formatAsWaypoint(n: OsmNodeNamed): String = TODO("FormatGpx.formatAsWaypoint: step 7, session 27")

    fun formatGpxHeader(sb: StringBuilder): Unit = TODO("FormatGpx.formatGpxHeader: step 7, session 27")

    fun formatGpxFooter(sb: StringBuilder): Unit = TODO("FormatGpx.formatGpxFooter: step 7, session 27")

    fun formatWaypointGpx(sb: StringBuilder, n: OsmNodeNamed, type: String?): Unit =
        TODO("FormatGpx.formatWaypointGpx(OsmNodeNamed): step 7, session 27")

    fun formatWaypointGpx(sb: StringBuilder, wp: MatchedWaypoint, type: String?): Unit =
        TODO("FormatGpx.formatWaypointGpx(MatchedWaypoint): step 7, session 27")

    override fun read(filename: String): OsmTrack? = read(filename, FileSystem.SYSTEM)

    fun read(filename: String, fileSystem: FileSystem): OsmTrack? = TODO("FormatGpx.read: step 7, session 27")

    companion object {
        fun getWaypoint(ilon: Int, ilat: Int, name: String?, desc: String?): String =
            TODO("FormatGpx.getWaypoint: step 7, session 27")
    }
}
