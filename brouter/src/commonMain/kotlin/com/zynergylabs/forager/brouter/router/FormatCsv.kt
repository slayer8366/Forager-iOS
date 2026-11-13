package com.zynergylabs.forager.brouter.router

/**
 * BRouter 1.7.10's `btools.router.FormatCsv`, whole (step 7, session 24; RECORD -111, "Port them"). Java's
 * `BufferedWriter` over a `StringWriter` is a [StringBuilder] here.
 */
internal class FormatCsv(rc: RoutingContext?) : Formatter(rc) {
    /** `FormatCsv.java:14-23`. */
    override fun format(t: OsmTrack): String {
        return try {
            val sw = StringBuilder()
            writeMessages(sw, t)
            sw.toString()
        } catch (ex: Exception) {
            "Error: " + ex.message
        }
    }

    /** `FormatCsv.java:25-32`. */
    fun writeMessages(bw: StringBuilder?, t: OsmTrack) {
        dumpLine(bw, MESSAGES_HEADER)
        for (m in t.aggregateMessages()) {
            dumpLine(bw, m)
        }
        // Java closes its BufferedWriter here; a StringBuilder needs no closing.
    }

    /** `FormatCsv.java:34-41`. With no writer Java prints to `System.out`; this prints with [println]. */
    private fun dumpLine(bw: StringBuilder?, s: String) {
        if (bw == null) {
            println(s)
        } else {
            bw.append(s)
            bw.append("\n")
        }
    }
}
