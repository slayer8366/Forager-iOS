package com.zynergylabs.forager.brouter.util

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Item I, step 2a: the Java-semantics helpers (B rows 13 and 15) against the JDK the oracle runs on: `Math.round` at
 * edges and on seeded doubles, `Character.isWhitespace` on every `char`, and `String.split` with each regex BRouter's
 * reached code passes, on seeded strings. The first test checks `JavaLangCases`' expected values against the JDK.
 */
class JavaLangTest {
    @Test
    fun theCommonCasesExpectedValuesAreTheJdks() {
        for ((input, expected) in JavaLangCases.round) assertEquals(Math.round(input), expected, "Math.round($input)")
        for ((c, expected) in JavaLangCases.whitespace) assertEquals(Character.isWhitespace(c), expected, "U+${c.code.toString(16)}")
        for ((args, expected) in JavaLangCases.split) {
            assertEquals((args.first as java.lang.String).split(args.second).toList(), expected, "\"${args.first}\".split(\"${args.second}\")")
        }
    }

    @Test
    fun javaRoundEqualsMathRound() {
        val inputs = ArrayList<Double>()
        for (n in -5_000..5_000) {
            val half = n + 0.5
            inputs += listOf(half, Math.nextDown(half), Math.nextUp(half), n.toDouble())
        }
        for (e in 0..1_100) {
            val p = Math.scalb(1.0, e - 1_074)
            inputs += listOf(p, -p, Math.nextDown(p), Math.nextUp(p), p + 0.5, p - 0.5)
        }
        val r = Random(2026)
        repeat(1_000_000) { inputs += java.lang.Double.longBitsToDouble(r.nextLong()) }
        repeat(1_000_000) { inputs += r.nextDouble(-1.0e7, 1.0e7) }
        for (x in inputs) assertEquals(Math.round(x), javaRound(x), "javaRound($x), bits ${java.lang.Double.doubleToRawLongBits(x)}")
    }

    @Test
    fun javaIsWhitespaceEqualsCharacterIsWhitespaceOnEveryChar() {
        for (code in 0..0xFFFF) {
            val c = code.toChar()
            assertEquals(Character.isWhitespace(c), javaIsWhitespace(c), "U+${code.toString(16)}")
        }
    }

    @Test
    fun javaSplitEqualsStringSplitForEveryRegexTheReachedCodePasses() {
        // RoutingParamCollector.java:26, :32 and on; BExpressionContext.java:604-681, :1008-1013 (read at Forager 994b4384).
        val regexes = listOf(";|\\|", ",", "\\|", "ft", "'", "cm", "mph", "knot", "k", "st", "kg", "lbs", "t", ";")
        val pieces = listOf("a", "1", ".", ",", ";", "|", "'", "f", "t", "ft", "c", "m", "cm", "mph", "k", "kn", "knot", "s", "st", "kg", "lbs", " ", " ")
        val r = Random(7)
        for (regex in regexes) repeat(20_000) {
            val s = buildString { repeat(r.nextInt(0, 9)) { append(pieces[r.nextInt(pieces.size)]) } }
            assertEquals((s as java.lang.String).split(regex).toList(), s.javaSplit(regex).toList(), "\"$s\".split(\"$regex\")")
        }
    }
}
