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
        fileSystem.write(filename.toPath()) { writeUtf8(format(t)) }
    }

    open fun read(filename: String): OsmTrack? = null

    abstract fun format(t: OsmTrack): String

    fun getJsonCommandIndex(cmd: Int, timode: Int): Int = when (cmd) {
        VoiceHint.TLU -> 10
        VoiceHint.TU -> 15
        VoiceHint.TSHL -> 4
        VoiceHint.TL -> 2
        VoiceHint.TSLL -> 3
        VoiceHint.KL -> 8
        VoiceHint.C -> 1
        VoiceHint.KR -> 9
        VoiceHint.TSLR -> 6
        VoiceHint.TR -> 5
        VoiceHint.TSHR -> 7
        VoiceHint.TRU -> 11
        VoiceHint.RNDB -> 13
        VoiceHint.RNLB -> 14
        VoiceHint.BL -> 16
        VoiceHint.EL -> if (timode == 2 || timode == 9) 17 else 8
        VoiceHint.ER -> if (timode == 2 || timode == 9) 18 else 9
        VoiceHint.OFFR -> 12
        else -> throw IllegalArgumentException("unknown command: " + cmd)
    }

    fun getCommandString(cmd: Int, roundaboutExit: Int, timode: Int): String = when (cmd) {
        VoiceHint.TLU -> "TU"
        VoiceHint.TU -> "TU"
        VoiceHint.TSHL -> "TSHL"
        VoiceHint.TL -> "TL"
        VoiceHint.TSLL -> "TSLL"
        VoiceHint.KL -> "KL"
        VoiceHint.C -> "C"
        VoiceHint.KR -> "KR"
        VoiceHint.TSLR -> "TSLR"
        VoiceHint.TR -> "TR"
        VoiceHint.TSHR -> "TSHR"
        VoiceHint.TRU -> "TRU"
        VoiceHint.RNDB -> "RNDB" + roundaboutExit
        VoiceHint.RNLB -> "RNLB" + (-roundaboutExit)
        VoiceHint.BL -> "BL"
        VoiceHint.EL -> if (timode == 2 || timode == 9) "EL" else "KL"
        VoiceHint.ER -> if (timode == 2 || timode == 9) "ER" else "KR"
        VoiceHint.OFFR -> "OFFR"
        VoiceHint.END -> "END"
        else -> throw IllegalArgumentException("unknown command: " + cmd)
    }

    fun getCommandStringXX(c: Int, roundaboutExit: Int, timode: Int): String = when (c) {
        VoiceHint.TLU -> "TLU"
        VoiceHint.TU -> "TU"
        VoiceHint.TSHL -> "TSHL"
        VoiceHint.TL -> "TL"
        VoiceHint.TSLL -> "TSLL"
        VoiceHint.KL -> "KL"
        VoiceHint.C -> "C"
        VoiceHint.KR -> "KR"
        VoiceHint.TSLR -> "TSLR"
        VoiceHint.TR -> "TR"
        VoiceHint.TSHR -> "TSHR"
        VoiceHint.TRU -> "TRU"
        VoiceHint.RNDB -> "RNDB" + roundaboutExit
        VoiceHint.RNLB -> "RNLB" + (-roundaboutExit)
        VoiceHint.BL -> "BL"
        VoiceHint.EL -> if (timode == 2 || timode == 9) "EL" else "KL"
        VoiceHint.ER -> if (timode == 2 || timode == 9) "ER" else "KR"
        VoiceHint.OFFR -> "OFFR"
        else -> "unknown command: " + c
    }

    fun getSymbolString(cmd: Int, roundaboutExit: Int, timode: Int): String = when (cmd) {
        VoiceHint.TLU, VoiceHint.TRU, VoiceHint.TU -> "TU"
        VoiceHint.TSHL -> "TSHL"
        VoiceHint.TL -> "Left"
        VoiceHint.TSLL -> "TSLL"
        VoiceHint.KL -> "TSLL"
        VoiceHint.C -> "Straight"
        VoiceHint.KR -> "TSLR"
        VoiceHint.TSLR -> "TSLR"
        VoiceHint.TR -> "Right"
        VoiceHint.TSHR -> "TSHR"
        VoiceHint.RNDB -> "RNDB" + roundaboutExit
        VoiceHint.RNLB -> "RNLB" + (-roundaboutExit)
        VoiceHint.BL -> "BL"
        VoiceHint.EL -> if (timode == 2 || timode == 9) "EL" else "KL"
        VoiceHint.ER -> if (timode == 2 || timode == 9) "ER" else "KR"
        VoiceHint.OFFR -> "OFFR"
        else -> throw IllegalArgumentException("unknown command: " + cmd)
    }

    fun getLocusSymbolString(cmd: Int, roundaboutExit: Int): String = when (cmd) {
        VoiceHint.TLU -> "u-turn_left"
        VoiceHint.TU -> "u-turn"
        VoiceHint.TSHL -> "left_sharp"
        VoiceHint.TL -> "left"
        VoiceHint.TSLL -> "left_slight"
        VoiceHint.KL -> "stay_left"
        VoiceHint.C -> "straight"
        VoiceHint.KR -> "stay_right"
        VoiceHint.TSLR -> "right_slight"
        VoiceHint.TR -> "right"
        VoiceHint.TSHR -> "right_sharp"
        VoiceHint.TRU -> "u-turn_right"
        VoiceHint.RNDB -> "roundabout_e" + roundaboutExit
        VoiceHint.RNLB -> "roundabout_e" + (-roundaboutExit)
        VoiceHint.BL -> "beeline"
        VoiceHint.EL -> "exit_left"
        VoiceHint.ER -> "exit_right"
        else -> throw IllegalArgumentException("unknown command: " + cmd)
    }

    fun getMessageString(cmd: Int, roundaboutExit: Int, timode: Int): String = when (cmd) {
        VoiceHint.TLU -> "u-turn"
        VoiceHint.TU -> "u-turn"
        VoiceHint.TSHL -> "sharp left"
        VoiceHint.TL -> "left"
        VoiceHint.TSLL -> "slight left"
        VoiceHint.KL -> "keep left"
        VoiceHint.C -> "straight"
        VoiceHint.KR -> "keep right"
        VoiceHint.TSLR -> "slight right"
        VoiceHint.TR -> "right"
        VoiceHint.TSHR -> "sharp right"
        VoiceHint.TRU -> "u-turn"
        VoiceHint.RNDB -> "Take exit " + roundaboutExit
        VoiceHint.RNLB -> "Take exit " + (-roundaboutExit)
        VoiceHint.EL -> if (timode == 2 || timode == 9) "exit left" else "keep left"
        VoiceHint.ER -> if (timode == 2 || timode == 9) "exit right" else "keep right"
        else -> throw IllegalArgumentException("unknown command: " + cmd)
    }

    fun getLocusAction(cmd: Int, roundaboutExit: Int): Int = when (cmd) {
        VoiceHint.TLU -> 13
        VoiceHint.TU -> 12
        VoiceHint.TSHL -> 5
        VoiceHint.TL -> 4
        VoiceHint.TSLL -> 3
        VoiceHint.KL -> 9
        VoiceHint.C -> 1
        VoiceHint.KR -> 10
        VoiceHint.TSLR -> 6
        VoiceHint.TR -> 7
        VoiceHint.TSHR -> 8
        VoiceHint.TRU -> 14
        VoiceHint.RNDB -> 26 + roundaboutExit
        VoiceHint.RNLB -> 26 - roundaboutExit
        VoiceHint.EL -> 9
        VoiceHint.ER -> 10
        else -> throw IllegalArgumentException("unknown command: " + cmd)
    }

    fun getOruxAction(cmd: Int, roundaboutExit: Int): Int = when (cmd) {
        VoiceHint.TLU, VoiceHint.TU, VoiceHint.TRU -> 1003
        VoiceHint.TSHL -> 1019
        VoiceHint.TL -> 1000
        VoiceHint.TSLL -> 1017
        VoiceHint.KL -> 1015
        VoiceHint.C -> 1002
        VoiceHint.KR -> 1014
        VoiceHint.TSLR -> 1016
        VoiceHint.TR -> 1001
        VoiceHint.TSHR -> 1018
        VoiceHint.RNDB, VoiceHint.RNLB -> 1008 + roundaboutExit
        VoiceHint.EL -> 1015
        VoiceHint.ER -> 1014
        else -> throw IllegalArgumentException("unknown command: " + cmd)
    }

    fun getCruiserCommandString(cmd: Int, roundaboutExit: Int): String = when (cmd) {
        VoiceHint.TLU -> "TLU"
        VoiceHint.TU -> "TU"
        VoiceHint.TSHL -> "TSHL"
        VoiceHint.TL -> "TL"
        VoiceHint.TSLL -> "TSLL"
        VoiceHint.KL -> "KL"
        VoiceHint.C -> "C"
        VoiceHint.KR -> "KR"
        VoiceHint.TSLR -> "TSLR"
        VoiceHint.TR -> "TR"
        VoiceHint.TSHR -> "TSHR"
        VoiceHint.TRU -> "TRU"
        VoiceHint.RNDB -> "RNDB" + roundaboutExit
        VoiceHint.RNLB -> "RNLB" + (-roundaboutExit)
        VoiceHint.BL -> "BL"
        VoiceHint.EL -> "EL"
        VoiceHint.ER -> "ER"
        VoiceHint.OFFR -> "OFFR"
        else -> throw IllegalArgumentException("unknown command: " + cmd)
    }

    fun getCruiserMessageString(cmd: Int, roundaboutExit: Int): String = when (cmd) {
        VoiceHint.TLU -> "u-turn left"
        VoiceHint.TU -> "u-turn"
        VoiceHint.TSHL -> "sharp left"
        VoiceHint.TL -> "left"
        VoiceHint.TSLL -> "slight left"
        VoiceHint.KL -> "keep left"
        VoiceHint.C -> "straight"
        VoiceHint.KR -> "keep right"
        VoiceHint.TSLR -> "slight right"
        VoiceHint.TR -> "right"
        VoiceHint.TSHR -> "sharp right"
        VoiceHint.TRU -> "u-turn right"
        VoiceHint.RNDB -> "take exit " + roundaboutExit
        VoiceHint.RNLB -> "take exit " + (-roundaboutExit)
        VoiceHint.BL -> "beeline"
        VoiceHint.EL -> "exit left"
        VoiceHint.ER -> "exit right"
        VoiceHint.OFFR -> "offroad"
        else -> throw IllegalArgumentException("unknown command: " + cmd)
    }

    companion object {
        const val MESSAGES_HEADER =
            "Longitude\tLatitude\tElevation\tDistance\tCostPerKm\tElevCost\tTurnCost\tNodeCost\tInitialCost\tWayTags\tNodeTags\tTime\tEnergy"

        fun formatILon(ilon: Int): String = formatPos(ilon - 180000000)

        fun formatILat(ilat: Int): String = formatPos(ilat - 90000000)

        private fun formatPos(p0: Int): String {
            var p = p0
            val negative = p < 0
            if (negative) p = -p
            val ac = CharArray(12)
            var i = 11
            while (p != 0 || i > 3) {
                ac[i--] = ('0'.code + (p % 10)).toChar()
                p /= 10
                if (i == 5) ac[i--] = '.'
            }
            if (negative) ac[i--] = '-'
            return ac.concatToString(i + 1, 12)
        }

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

        fun getFormattedTime3(time: Float): String {
            // Java: `new SimpleDateFormat(dateformat, Locale.US)` in UTC over `new Date((long) (time * 1000f))`. Its
            // calendar is `GregorianCalendar`'s default: Julian before 1582-10-15T00:00Z, Gregorian from then; `yyyy`
            // is the year of era (BC years count back from 1), zero-padded to four digits; ASCII digits (Locale.US).
            val ms = (time * 1000f).toLong()
            val days = floorDiv(ms, 86400000L)
            val msOfDay = ms - days * 86400000L
            val y: Long
            val mo: Long
            val d: Long
            if (ms >= GREGORIAN_CUTOVER_MS) {
                val z = days + 719468L
                val era = floorDiv(z, 146097L)
                val doe = z - era * 146097L
                val yoe = (doe - doe / 1460L + doe / 36524L - doe / 146096L) / 365L
                val doy = doe - (365L * yoe + yoe / 4L - yoe / 100L)
                val mp = (5L * doy + 2L) / 153L
                d = doy - (153L * mp + 2L) / 5L + 1L
                mo = if (mp < 10L) mp + 3L else mp - 9L
                y = yoe + era * 400L + (if (mo <= 2L) 1L else 0L)
            } else {
                val c = days + 2440588L + 32082L
                val dd = floorDiv(4L * c + 3L, 1461L)
                val e = c - floorDiv(1461L * dd, 4L)
                val m = (5L * e + 2L) / 153L
                d = e - (153L * m + 2L) / 5L + 1L
                mo = m + 3L - 12L * (m / 10L)
                y = dd - 4800L + m / 10L
            }
            val yearOfEra = if (y <= 0L) 1L - y else y
            return pad(yearOfEra, 4) + "-" + pad(mo, 2) + "-" + pad(d, 2) + "T" + pad(msOfDay / 3600000L, 2) + ":" +
                pad(msOfDay / 60000L % 60L, 2) + ":" + pad(msOfDay / 1000L % 60L, 2) + "." + pad(msOfDay % 1000L, 3) + "Z"
        }

        private const val GREGORIAN_CUTOVER_MS = -12219292800000L

        private fun floorDiv(a: Long, b: Long): Long {
            val q = a / b
            return if (a % b != 0L && (a < 0L) != (b < 0L)) q - 1L else q
        }

        private fun pad(v: Long, width: Int): String = v.toString().padStart(width, '0')
    }
}
