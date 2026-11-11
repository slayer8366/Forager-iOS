package com.zynergylabs.forager.brouter.router

import okio.FileSystem
import okio.Path.Companion.toPath

/**
 * BRouter 1.7.10's `btools.router.Formatter`. Step 7, session 19: the three statics that `RoutingEngine.doRouting:238`
 * reaches (`getFormattedTime2`, `getFormattedEnergy`, `format1`). Session 23 (RECORD -111, "Port them"): the rest,
 * whole; the `Format*` classes subclass it.
 */
internal abstract class Formatter {
    internal var rc: RoutingContext? = null

    constructor()

    constructor(rc: RoutingContext?) {
        this.rc = rc
    }

    /** `Formatter.java:49-53`. Java's `FileWriter` writes in the platform's default charset; this writes UTF-8. */
    fun write(filename: String, t: OsmTrack, fileSystem: FileSystem = FileSystem.SYSTEM) {
        TODO("step 7, session 23 stub: Formatter.write")
    }

    open fun read(filename: String): OsmTrack? = TODO("step 7, session 23 stub: Formatter.read")

    abstract fun format(t: OsmTrack): String

    fun getJsonCommandIndex(cmd: Int, timode: Int): Int = TODO("step 7, session 23 stub: Formatter.getJsonCommandIndex")

    fun getCommandString(cmd: Int, roundaboutExit: Int, timode: Int): String =
        TODO("step 7, session 23 stub: Formatter.getCommandString")

    fun getCommandStringXX(c: Int, roundaboutExit: Int, timode: Int): String =
        TODO("step 7, session 23 stub: Formatter.getCommandStringXX")

    fun getSymbolString(cmd: Int, roundaboutExit: Int, timode: Int): String =
        TODO("step 7, session 23 stub: Formatter.getSymbolString")

    fun getLocusSymbolString(cmd: Int, roundaboutExit: Int): String =
        TODO("step 7, session 23 stub: Formatter.getLocusSymbolString")

    fun getMessageString(cmd: Int, roundaboutExit: Int, timode: Int): String =
        TODO("step 7, session 23 stub: Formatter.getMessageString")

    fun getLocusAction(cmd: Int, roundaboutExit: Int): Int = TODO("step 7, session 23 stub: Formatter.getLocusAction")

    fun getOruxAction(cmd: Int, roundaboutExit: Int): Int = TODO("step 7, session 23 stub: Formatter.getOruxAction")

    fun getCruiserCommandString(cmd: Int, roundaboutExit: Int): String =
        TODO("step 7, session 23 stub: Formatter.getCruiserCommandString")

    fun getCruiserMessageString(cmd: Int, roundaboutExit: Int): String =
        TODO("step 7, session 23 stub: Formatter.getCruiserMessageString")

    companion object {
        const val MESSAGES_HEADER =
            "Longitude\tLatitude\tElevation\tDistance\tCostPerKm\tElevCost\tTurnCost\tNodeCost\tInitialCost\tWayTags\tNodeTags\tTime\tEnergy"

        fun formatILon(ilon: Int): String = TODO("step 7, session 23 stub: Formatter.formatILon")

        fun formatILat(ilat: Int): String = TODO("step 7, session 23 stub: Formatter.formatILat")

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

        const val dateformat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

        fun getFormattedTime3(time: Float): String = TODO("step 7, session 23 stub: Formatter.getFormattedTime3")
    }
}
