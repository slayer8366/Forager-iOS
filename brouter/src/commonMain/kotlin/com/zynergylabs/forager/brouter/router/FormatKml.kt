package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.mapaccess.MatchedWaypoint
import com.zynergylabs.forager.brouter.util.StringUtils

/**
 * BRouter 1.7.10's `btools.router.FormatKml`, whole (step 7, session 24; RECORD -111, "Port them"). Where Java
 * dereferences a null (`matchedWaypoints`, a waypoint, a name), `!!` throws Kotlin's `NullPointerException`.
 */
internal class FormatKml(rc: RoutingContext?) : Formatter(rc) {
    /** `FormatKml.java:14-82`. */
    override fun format(t: OsmTrack): String = TODO("step 7, session 24 stub: FormatKml.format")
}
