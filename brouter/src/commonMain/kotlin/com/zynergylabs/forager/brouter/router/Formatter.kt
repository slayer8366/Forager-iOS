package com.zynergylabs.forager.brouter.router

/**
 * BRouter 1.7.10's `btools.router.Formatter`. Step 7, session 19: the three statics that `RoutingEngine.doRouting:238`
 * reaches (`getFormattedTime2`, `getFormattedEnergy`, `format1`). The rest, with the `Format*` classes, is a later
 * session (RECORD -111).
 */
internal abstract class Formatter {
    companion object {
        fun getFormattedTime2(s: Int): String = TODO("step 7 stub: Formatter.getFormattedTime2")

        fun getFormattedEnergy(energy: Int): String = TODO("step 7 stub: Formatter.getFormattedEnergy")

        /** Private in Java; internal here so that `TrackTimeTest` reaches it as the oracle's is reached, by name. */
        internal fun format1(n: Double): String = TODO("step 7 stub: Formatter.format1")
    }
}
