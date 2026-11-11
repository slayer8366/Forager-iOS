package com.zynergylabs.forager.brouter.router

/**
 * BRouter 1.7.10's `btools.router.Formatter`. Step 7, session 19: the three statics that `RoutingEngine.doRouting:238`
 * reaches (`getFormattedTime2`, `getFormattedEnergy`, `format1`). The rest, with the `Format*` classes, is a later
 * session (RECORD -111).
 */
internal abstract class Formatter {
    companion object {
        fun getFormattedTime2(s: Int): String {
            var seconds = (s + 0.5).toInt()
            val hours = seconds / 3600
            val minutes = (seconds - hours * 3600) / 60
            seconds = seconds - hours * 3600 - minutes * 60
            var time = ""
            if (hours != 0) time = "" + hours + "h "
            if (minutes != 0) time = time + minutes + "m "
            if (seconds != 0) time = time + seconds + "s"
            return time
        }

        fun getFormattedEnergy(energy: Int): String = format1(energy / 3600000.0) + "kwh"

        /** Private in Java; internal here so that `TrackTimeTest` reaches it as the oracle's is reached, by name. */
        internal fun format1(n: Double): String {
            val s = "" + (n * 10 + 0.5).toLong()
            val len = s.length
            return s.substring(0, len - 1) + "." + s[len - 1]
        }
    }
}
