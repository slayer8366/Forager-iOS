package com.zynergylabs.forager.brouter.tiles

import com.zynergylabs.forager.brouter.router.FormatCsv
import com.zynergylabs.forager.brouter.router.FormatGpx
import com.zynergylabs.forager.brouter.router.FormatJson
import com.zynergylabs.forager.brouter.router.FormatKml
import com.zynergylabs.forager.brouter.router.OsmTrack
import com.zynergylabs.forager.brouter.router.RoutingContext

/**
 * Step 10, session 55: the five outputs of a route the engine found ([OUTPUTS]): the turn hints as a text of their
 * fields ([hintsText]), and `FormatGpx`, `FormatJson`, `FormatKml`, `FormatCsv` `format(track)` with the context the
 * route ran with. Each is reduced to its length and a 64-bit FNV-1a digest over its UTF-8 bytes ([EngineRun.fnv1aText]);
 * the text itself is never printed, stored or put in a message (E1: it is full of positions).
 *
 * `jvmTest`'s `FormatDigestsOracleTest` writes the same five texts from the oracle's track (the same text for the hints,
 * read by reflection) and holds [FormatDigests]' constants to them.
 */
internal object FormatOutputs {
    val OUTPUTS = listOf("hints", "gpx", "json", "kml", "csv")

    /**
     * The turn hints as a text: the mode and the count, then per hint its index in the track, command, roundabout exit,
     * position and height as integers, `distanceToNext` and `angle` as raw bits, and `formatGeometry()`. No double is
     * formatted here, so this output does not go through the platform's number formatting except `formatGeometry`'s ints.
     * Session 57: with [withAngle] false the angle's field is left empty, so a production record can tell a hint that
     * differs only in its angle (RECORD -150) from one that differs in anything else.
     */
    fun hintsText(t: OsmTrack, withAngle: Boolean = true): String {
        val v = t.voiceHints ?: return "none\n"
        val sb = StringBuilder()
        sb.append(v.turnInstructionMode).append(';').append(v.list.size).append('\n')
        for (h in v.list) {
            sb.append(h.indexInTrack).append(';').append(h.cmd).append(';').append(h.roundaboutExit).append(';')
                .append(h.ilon).append(';').append(h.ilat).append(';').append(h.selev.toInt()).append(';')
                .append(h.distanceToNext.toRawBits()).append(';').append(if (withAngle) h.angle.toRawBits().toString() else "").append(';')
                .append(h.formatGeometry()).append('\n')
        }
        return sb.toString()
    }

    /** What a call returned, or "throws" and the exception's class name (the message is each runtime's own). */
    fun res(f: () -> String): String = try {
        f()
    } catch (e: Exception) {
        "throws " + (e::class.simpleName ?: "?")
    }

    /** The five texts, in [OUTPUTS]' order. */
    fun texts(t: OsmTrack, rc: RoutingContext): List<String> = listOf(
        res { hintsText(t) },
        res { FormatGpx(rc).format(t) },
        res { FormatJson(rc).format(t) },
        res { FormatKml(rc).format(t) },
        res { FormatCsv(rc).format(t) },
    )

    /**
     * One route's stored values: its id, the mode it ran with, and per output its length and digest. Session 71
     * (RECORD -177): [textLengths], the plain text's length of GPX and JSON (outputs 1 and 2), beside their by-value
     * length and digest.
     */
    class Row(val id: String, val mode: Int, val lengths: List<Int>, val digests: List<String>, val textLengths: List<Int>)

    /** The turn-instruction mode for the [index]th case of a group: 2 to 9 in turn (the survey, item 5). */
    fun modeFor(index: Int): Int = 2 + index % 8

    /** The fields of one line of [hintsText] after its header, in order. */
    val HINT_FIELDS = listOf("indexInTrack", "cmd", "roundaboutExit", "ilon", "ilat", "selev", "distanceToNext", "angle", "geometry")

    /**
     * Session 57: where two hints texts first differ, without any of their text (E1): the header's field, or the first
     * differing hint's index and field name, or which list is longer.
     */
    fun hintsDiff(a: String, b: String): String {
        val la = a.lines()
        val lb = b.lines()
        if (la[0] != lb[0]) {
            val ha = la[0].split(';')
            val hb = lb[0].split(';')
            return if (ha.getOrNull(0) != hb.getOrNull(0)) "header: mode" else "header: count"
        }
        for (i in 1 until minOf(la.size, lb.size)) {
            if (la[i] == lb[i]) continue
            val fa = la[i].split(';', limit = HINT_FIELDS.size)
            val fb = lb[i].split(';', limit = HINT_FIELDS.size)
            val k = HINT_FIELDS.indices.firstOrNull { fa.getOrNull(it) != fb.getOrNull(it) } ?: -1
            return "hint ${i - 1} field ${HINT_FIELDS.getOrElse(k) { "?" }}"
        }
        return "lengths ${la.size} and ${lb.size} lines"
    }

    /**
     * Session 57: where two texts first differ, without any of their text (E1): the character index and the class of
     * the character there on each side (digit, sign, point, letter, other, or end when that text is shorter).
     */
    fun textDiff(a: String, b: String): String {
        val n = minOf(a.length, b.length)
        val i = (0 until n).firstOrNull { a[it] != b[it] } ?: n
        return "char $i: ${charClass(a, i)} / ${charClass(b, i)}"
    }

    private fun charClass(s: String, i: Int): String {
        if (i >= s.length) return "end"
        val c = s[i]
        return when {
            c in '0'..'9' -> "digit"
            c == '-' || c == '+' -> "sign"
            c == '.' -> "point"
            c.isLetter() -> "letter"
            else -> "other"
        }
    }

    /** [hintsDiff] for the hints (output 0), [textDiff] for a format. */
    fun diff(output: Int, a: String, b: String): String = if (output == 0) hintsDiff(a, b) else textDiff(a, b)
}
