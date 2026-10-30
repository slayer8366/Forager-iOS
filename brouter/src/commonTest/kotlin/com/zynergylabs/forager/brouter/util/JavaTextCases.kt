package com.zynergylabs.forager.brouter.util

/**
 * Item I, step 4: edge inputs for the Java text helpers in `JavaText.kt`, with Java's answers. They live in commonTest so
 * the iOS simulator runs them against the port (`JavaTextCommonTest`); `JavaTextTest` in jvmTest checks every expected
 * value here against the JDK itself, so none of them is taken from memory.
 */
internal object JavaTextCases {
    /**
     * `new StringTokenizer(str, delim)`, then `nextToken()` while `hasMoreTokens()`: (str, delim) to the tokens. The two
     * delimiters are the two BRouter's expressions pass (`BExpressionContext.java:291`, `BExpression.java:194`).
     */
    val tokens: List<Pair<Pair<String, Char>, List<String>>> = listOf(
        ("" to ' ') to emptyList(),
        (" " to ' ') to emptyList(),
        ("   " to ' ') to emptyList(),
        ("a" to ' ') to listOf("a"),
        (" a  b " to ' ') to listOf("a", "b"),
        ("highway primary prim 1" to ' ') to listOf("highway", "primary", "prim", "1"),
        ("a\tb c" to ' ') to listOf("a\tb", "c"), // only the given delimiter splits, not other whitespace
        ("a b" to ' ') to listOf("a b"),
        ("" to '|') to emptyList(),
        ("|" to '|') to emptyList(),
        ("a|b" to '|') to listOf("a", "b"),
        ("|a||b|" to '|') to listOf("a", "b"), // empty tokens are dropped
        ("a b|c" to '|') to listOf("a b", "c"),
        ("primary|secondary|" to '|') to listOf("primary", "secondary"),
    )

    /** `String.trim()`: Java strips every char at or below U+0020 from both ends, and nothing else. */
    val trim: List<Pair<String, String>> = listOf(
        "" to "",
        " " to "",
        "a" to "a",
        "  a b  " to "a b",
        "\t a \n" to "a",
        "\u0000a\u0000" to "a",
        "\u001fa\u0001" to "a",
        "\r\n" to "",
        " a " to " a ", // no-break space is not trimmed by Java
        " a　" to " a　", // nor are Unicode spaces
        "\u0085a\u0085" to "\u0085a\u0085", // nor NEL
        "!a!" to "!a!",
        "\u007fa" to "\u007fa",
        " x = 1 " to "x = 1",
    )

    /** `BufferedReader.readLine()` until null, over the text: a line ends at `\n`, `\r` or `\r\n`, and nothing else. */
    val lines: List<Pair<String, List<String>>> = listOf(
        "" to emptyList(),
        "a" to listOf("a"),
        "a\n" to listOf("a"),
        "a\r\n" to listOf("a"),
        "a\r" to listOf("a"),
        "a\n\n" to listOf("a", ""),
        "\n" to listOf(""),
        "\r" to listOf(""),
        "\r\n\r\n" to listOf("", ""),
        "a\r\rb" to listOf("a", "", "b"),
        "a\n\rb" to listOf("a", "", "b"),
        "a\r\n\nb" to listOf("a", "", "b"),
        "a b\u0085c" to listOf("a b\u0085c"), // LS and NEL are not line ends
        " ---context:way \n# c\n\nhighway primary\n" to listOf(" ---context:way ", "# c", "", "highway primary"),
    )
}
