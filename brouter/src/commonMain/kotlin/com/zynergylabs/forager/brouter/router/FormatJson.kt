package com.zynergylabs.forager.brouter.router

/**
 * BRouter 1.7.10's `btools.router.FormatJson` (step 7, session 25; RECORD -111, "Port them"). Stubs, seen failing
 * first (RECORD -101).
 */
internal class FormatJson(rc: RoutingContext?) : Formatter(rc) {
    /** `FormatJson.java:19-180`. */
    override fun format(t: OsmTrack): String = TODO("FormatJson.format: step 7, session 25")

    /** `FormatJson.java:199-212`. */
    fun formatAsWaypoint(n: OsmNodeNamed): String = TODO("FormatJson.formatAsWaypoint: step 7, session 25")

    companion object {
        /**
         * Java's `DecimalFormat` from `NumberFormat.getInstance(Locale.ENGLISH)` with pattern `0.###`, as
         * `FormatJson.java:84-86` formats a node's time (a `float`, widened to `double`).
         */
        internal fun formatDecimal3(v: Float): String = TODO("FormatJson.formatDecimal3: step 7, session 25")
    }
}
