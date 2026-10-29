package com.zynergylabs.forager.brouter.util

/**
 * Item I, step 2a: edge inputs for the Java-semantics helpers, with Java's answers. They live in commonTest so the iOS
 * simulator runs them against the port (`JavaLangCommonTest`); `JavaLangTest` in jvmTest checks every expected value
 * here against the JDK itself, so none of them is taken from memory.
 */
internal object JavaLangCases {
    /** `Math.round(double)`: input to Java's answer. The first two are where Native's `roundToLong` differs (item H). */
    val round: List<Pair<Double, Long>> = listOf(
        0.49999999999999994 to 0L,
        4503599627370497.0 to 4503599627370497L, // 2^52 + 1
        -0.49999999999999994 to 0L,
        -4503599627370497.0 to -4503599627370497L,
        0.5 to 1L,
        -0.5 to 0L,
        1.5 to 2L,
        2.5 to 3L,
        -2.5 to -2L,
        -1.5 to -1L,
        4503599627370495.5 to 4503599627370496L, // 2^52 - 0.5
        9007199254740991.0 to 9007199254740991L, // 2^53 - 1
        0.0 to 0L,
        -0.0 to 0L,
        Double.MIN_VALUE to 0L,
        Double.NaN to 0L,
        Double.POSITIVE_INFINITY to Long.MAX_VALUE,
        Double.NEGATIVE_INFINITY to Long.MIN_VALUE,
        1.0e19 to Long.MAX_VALUE,
        -1.0e19 to Long.MIN_VALUE,
        9.223372036854776E18 to Long.MAX_VALUE, // 2^63
        -9.223372036854776E18 to Long.MIN_VALUE,
        12345.678 to 12346L,
    )

    /** `Character.isWhitespace(char)`. Kotlin's own `Char.isWhitespace()` says true to the no-break spaces and to NEL. */
    val whitespace: List<Pair<Char, Boolean>> = listOf(
        ' ' to true,
        '\t' to true,
        '\n' to true,
        '\u000B' to true,
        '\u000C' to true,
        '\r' to true,
        '\u001C' to true,
        '\u001F' to true,
        ' ' to true,
        ' ' to true,
        ' ' to true,
        ' ' to true,
        ' ' to true,
        ' ' to true,
        ' ' to true,
        ' ' to true,
        '　' to true,
        ' ' to false, // no-break spaces: not whitespace in Java
        ' ' to false,
        ' ' to false,
        '\u0085' to false,
        '᠎' to false,
        '​' to false,
        '﻿' to false,
        'a' to false,
        '#' to false,
        '\u0000' to false,
    )

    /** `String.split(regex)` with the regexes BRouter's reached code passes: (input, regex) to Java's array. */
    val split: List<Pair<Pair<String, String>, List<String>>> = listOf(
        ("a,b,,c,," to ",") to listOf("a", "b", "", "c"),
        ("," to ",") to listOf(),
        (",,," to ",") to listOf(),
        ("" to ",") to listOf(""),
        (",a" to ",") to listOf("", "a"),
        ("abc" to ",") to listOf("abc"),
        ("8.1,47.2;8.2,47.3|8.3,47.4" to ";|\\|") to listOf("8.1,47.2", "8.2,47.3", "8.3,47.4"),
        ("|;|" to ";|\\|") to listOf(),
        ("a||b|" to "\\|") to listOf("a", "", "b"),
        ("5ft" to "ft") to listOf("5"),
        ("ft5" to "ft") to listOf("", "5"),
        ("5'6" to "'") to listOf("5", "6"),
        ("120cm" to "cm") to listOf("120"),
        ("30mph" to "mph") to listOf("30"),
        ("5knot" to "knot") to listOf("5"),
        ("knot" to "k") to listOf("", "not"),
        ("3.5st" to "st") to listOf("3.5"),
        ("7.5t" to "t") to listOf("7.5"),
        ("tt" to "t") to listOf(),
        ("2kg" to "kg") to listOf("2"),
        ("10lbs" to "lbs") to listOf("10"),
        ("a;b;" to ";") to listOf("a", "b"),
    )
}
