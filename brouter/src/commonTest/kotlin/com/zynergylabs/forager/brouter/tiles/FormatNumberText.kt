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

    fun gpx(text: String): String = TODO("session 60 stub")

    fun json(text: String, showspeed: Boolean): String = TODO("session 60 stub")
}
