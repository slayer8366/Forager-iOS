package com.zynergylabs.forager.brouter.tiles

/**
 * Session 60 (the owner's ruling, RECORD -163): the text a GPX or JSON output's digest is taken over. Each floating number
 * the port prints with the platform's own text (the twelve places of session 59) is replaced by a canonical form of its
 * VALUE, and every other character is kept as it is, so a number that differs from Java's only in its last digit at a
 * tie digests alike, and anything else is still compared as text.
 *
 * Canonical form: the 16 lower-case hex digits of `text.toDouble().toRawBits()` for a `Double`, the 8 of
 * `text.toFloat().toRawBits()` for a `Float` (`FormatJson.speedText`, `:332`). A text that does not parse is kept as text
 * (so it is compared as text, never passed over).
 *
 * GPX ([gpx]): the content of every `<ele>`, `<locus:rteDistance>`, `<locus:rteTime>`, `<locus:rteSpeed>` element as a
 * `Double` (`FormatGpx` `:162`, `:195`, `:276`, `:507`, `:529`; `:166`, `:171`, `:172`), and of every `<brouter:speed>`
 * as a `Float` (`:332` through `FormatGpx` `:311`, `:359`). Nothing else.
 *
 * JSON ([json]): inside an array opened by a line that reads `"coordinates": [` once trimmed, the third number of each
 * inner `[a, b, c]` line (a LineString's point, `FormatJson:104`; a `Float` when the track has `showspeed`, `:332`), and
 * the third element line of a flat array (a Point's elevation, `:184`, `:223`, always a `Double`). The first two
 * numbers (the positions, printed by the port's own `formatILon`/`formatILat`) and everything else stay text.
 */
internal object FormatNumberText {
    private val GPX_DOUBLE = listOf("ele", "locus:rteDistance", "locus:rteTime", "locus:rteSpeed")
    private const val GPX_FLOAT = "brouter:speed"

    /** [text] for output index [output] of [FormatOutputs.OUTPUTS]: GPX and JSON canonical, the others unchanged. */
    fun forDigest(output: Int, text: String, showspeed: Boolean): String = when (output) {
        1 -> gpx(text)
        2 -> json(text, showspeed)
        else -> text
    }

    fun gpx(text: String): String {
        var s = text
        for (tag in GPX_DOUBLE) s = element(s, tag, false)
        return element(s, GPX_FLOAT, true)
    }

    fun json(text: String, showspeed: Boolean): String {
        val lines = text.split('\n')
        val out = ArrayList<String>(lines.size)
        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            out += line
            i++
            if (line.trim() != "\"coordinates\": [") continue
            var flat = 0
            while (i < lines.size && !lines[i].trim().startsWith("]")) {
                val l = lines[i]
                val t = l.trim()
                out += if (t.startsWith("[")) {
                    // A LineString's point: `[lon, lat]` or `[lon, lat, third]` and an optional trailing comma.
                    val open = l.indexOf('[')
                    val close = l.lastIndexOf(']')
                    val parts = l.substring(open + 1, close).split(", ")
                    if (parts.size == 3) l.substring(0, open + 1) + parts[0] + ", " + parts[1] + ", " + canonical(parts[2], showspeed) + l.substring(close) else l
                } else {
                    // A Point's flat array, one element per line: the third is the elevation.
                    flat++
                    if (flat == 3) {
                        val comma = t.endsWith(",")
                        val num = if (comma) t.dropLast(1) else t
                        l.substring(0, l.indexOf(t)) + canonical(num, false) + (if (comma) "," else "")
                    } else {
                        l
                    }
                }
                i++
            }
        }
        return out.joinToString("\n")
    }

    /** Every `<tag>content</tag>` in [s] with its content made canonical. */
    private fun element(s: String, tag: String, float: Boolean): String {
        val open = "<$tag>"
        val close = "</$tag>"
        val sb = StringBuilder(s.length)
        var from = 0
        while (true) {
            val a = s.indexOf(open, from)
            if (a < 0) break
            val b = s.indexOf(close, a + open.length)
            if (b < 0) break
            sb.append(s, from, a + open.length).append(canonical(s.substring(a + open.length, b), float)).append(close)
            from = b + close.length
        }
        sb.append(s, from, s.length)
        return sb.toString()
    }

    /** The value's bits in hex, or [text] itself when it does not parse. */
    private fun canonical(text: String, float: Boolean): String =
        if (float) {
            text.toFloatOrNull()?.let { it.toRawBits().toUInt().toString(16).padStart(8, '0') } ?: text
        } else {
            text.toDoubleOrNull()?.let { it.toRawBits().toULong().toString(16).padStart(16, '0') } ?: text
        }
}
