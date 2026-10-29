package com.zynergylabs.forager.brouter.util

// The Java-semantics helpers B rows 13 and 15 call for, so the port computes what the JDK computes on every target.

/**
 * Java's `Math.round(double)`: the JDK's bit-level algorithm (JDK 7 onward), not Kotlin's `roundToLong`, which differs
 * on Native at 0.49999999999999994 and 2^52 + 1 (item H). Written from memory of the JDK source (its `src.zip` is not
 * in the container); `JavaLangTest` compares it with `Math.round` on about 3.07 million inputs.
 */
internal fun javaRound(a: Double): Long {
    val longBits = a.toRawBits()
    val biasedExp = (longBits and EXP_BIT_MASK) shr (SIGNIFICAND_WIDTH - 1)
    val shift = (SIGNIFICAND_WIDTH - 2 + EXP_BIAS) - biasedExp
    if ((shift and -64L) == 0L) { // shift >= 0 && shift < 64
        // a is finite with pow(2, -64) <= ulp(a) < 1
        var r = (longBits and SIGNIF_BIT_MASK) or (SIGNIF_BIT_MASK + 1)
        if (longBits < 0) {
            r = -r
        }
        // (r shr shift) is floor(a * 2); adding 1 and shifting once more gives floor(a + 1/2).
        return ((r shr shift.toInt()) + 1) shr 1
    } else {
        // a is finite with abs(a) < 1/2, or a mathematical integer, or infinite, or NaN. Kotlin's toLong is Java's
        // (long) cast on every target: towards zero, saturating, NaN to 0.
        return a.toLong()
    }
}

private const val SIGNIFICAND_WIDTH = 53
private const val EXP_BIAS = 1023
private const val EXP_BIT_MASK = 0x7FF0000000000000L
private const val SIGNIF_BIT_MASK = 0x000FFFFFFFFFFFFFL

/**
 * Java's `Character.isWhitespace(char)`: true for exactly 25 `char`s on JDK 21. Kotlin's `Char.isWhitespace()` differs
 * (it accepts the no-break spaces U+00A0, U+2007, U+202F, and U+0085). `JavaLangTest` compares all 65,536.
 */
internal fun javaIsWhitespace(c: Char): Boolean {
    val code = c.code
    return code in 0x09..0x0D || code in 0x1C..0x1F || code == 0x20 || code == 0x1680 ||
        code in 0x2000..0x2006 || code in 0x2008..0x200A || code == 0x2028 || code == 0x2029 ||
        code == 0x205F || code == 0x3000
}

/**
 * Java's `String.split(regex)` (limit 0), following `Pattern.split`'s loop: no match returns the input whole; a match
 * at index 0 yields a leading empty string; trailing empty strings are dropped. Java skips a zero-width match at index
 * 0 and Kotlin's regex engines step past zero-width matches differently, so a zero-width match is refused as
 * unsupported rather than answered: no regex the reached code passes can match empty (`JavaLangTest` lists them).
 */
internal fun String.javaSplit(regex: String): Array<String> {
    val list = ArrayList<String>()
    var index = 0
    for (m in Regex(regex).findAll(this)) {
        if (m.range.isEmpty()) {
            throw UnsupportedOperationException("unsupported: javaSplit on a zero-width match of \"$regex\"")
        }
        list.add(substring(index, m.range.first))
        index = m.range.last + 1
    }
    // If no match was found, return this
    if (index == 0) return arrayOf(this)
    list.add(substring(index))
    var resultSize = list.size
    while (resultSize > 0 && list[resultSize - 1].isEmpty()) {
        resultSize--
    }
    return Array(resultSize) { list[it] }
}
