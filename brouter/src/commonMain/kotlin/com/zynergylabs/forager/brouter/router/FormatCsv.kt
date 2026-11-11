package com.zynergylabs.forager.brouter.router

/**
 * BRouter 1.7.10's `btools.router.FormatCsv`, whole (step 7, session 24; RECORD -111, "Port them"). Java's
 * `BufferedWriter` over a `StringWriter` is a [StringBuilder] here.
 */
internal class FormatCsv(rc: RoutingContext?) : Formatter(rc) {
    /** `FormatCsv.java:14-23`. */
    override fun format(t: OsmTrack): String = TODO("step 7, session 24 stub: FormatCsv.format")

    /** `FormatCsv.java:25-32`. */
    fun writeMessages(bw: StringBuilder?, t: OsmTrack): Unit = TODO("step 7, session 24 stub: FormatCsv.writeMessages")
}
