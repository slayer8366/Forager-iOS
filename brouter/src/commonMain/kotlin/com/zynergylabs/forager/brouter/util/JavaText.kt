package com.zynergylabs.forager.brouter.util

// Item I, step 4: the Java text helpers `expressions` calls (prediction 1, `step4/0-prediction.txt`), so the port reads
// profiles and lookups the way the JDK does on every target. `JavaTextTest` compares each with the JDK.

/**
 * Java's `java.util.StringTokenizer(str, delims)` with one delimiter `char` and `returnDelims` false, the only form
 * `expressions` uses (`BExpressionContext.java:291` with " ", `BExpression.java:194` with "|"). Runs of the delimiter
 * separate tokens and empty tokens are never returned. Kotlin's `split` keeps empty tokens, so it is not used. Written
 * from the JDK's documented algorithm (skip delimiters, scan to the next one); `JavaTextTest` compares it with the JDK.
 */
internal class JavaStringTokenizer(private val str: String, private val delim: Char) {
    private var currentPosition = 0
    private var newPosition = -1 // where hasMoreTokens left the next token's start, or -1, as the JDK caches it

    private fun skipDelimiters(startPos: Int): Int {
        var position = startPos
        while (position < str.length && str[position] == delim) position++
        return position
    }

    private fun scanToken(startPos: Int): Int {
        var position = startPos
        while (position < str.length && str[position] != delim) position++
        return position
    }

    fun hasMoreTokens(): Boolean {
        newPosition = skipDelimiters(currentPosition)
        return newPosition < str.length
    }

    /** Throws `NoSuchElementException` with no message when no token is left, as the JDK does. */
    fun nextToken(): String {
        currentPosition = if (newPosition >= 0) newPosition else skipDelimiters(currentPosition)
        newPosition = -1
        if (currentPosition >= str.length) throw NoSuchElementException()
        val start = currentPosition
        currentPosition = scanToken(currentPosition)
        return str.substring(start, currentPosition)
    }

    fun countTokens(): Int {
        var count = 0
        var currpos = currentPosition
        while (currpos < str.length) {
            currpos = skipDelimiters(currpos)
            if (currpos >= str.length) break
            currpos = scanToken(currpos) // advances at least one char: str[currpos] is not the delimiter here
            count++
        }
        return count
    }
}

/**
 * Java's `String.trim()`: strips every char at or below U+0020 from both ends, and nothing else. Kotlin's `trim()` strips
 * Unicode whitespace instead, which takes the no-break spaces and U+2000 to U+200A that Java keeps, and keeps the control
 * chars below U+0009 that Java strips. Returns the receiver itself when nothing is stripped, as Java does.
 */
internal fun String.javaTrim(): String {
    var st = 0
    var len = length
    while (st < len && this[st] <= ' ') st++
    while (st < len && this[len - 1] <= ' ') len--
    return if (st > 0 || len < length) substring(st, len) else this
}

/**
 * The lines `BufferedReader.readLine()` returns over [text], until it returns null: a line ends at `\n`, at `\r`, or at
 * `\r\n`, and at nothing else (not U+2028, not NEL); the terminator is not part of the line; text after the last
 * terminator is a last line only if it is not empty. Decoding the file's bytes is the caller's.
 */
internal fun javaReadLines(text: String): List<String> {
    val lines = ArrayList<String>()
    var i = 0
    while (i < text.length) {
        var j = i
        while (j < text.length && text[j] != '\n' && text[j] != '\r') j++
        lines += text.substring(i, j)
        if (j < text.length) j += if (text[j] == '\r' && j + 1 < text.length && text[j + 1] == '\n') 2 else 1
        i = j // i < j here unless j == text.length: the loop advances or ends
    }
    return lines
}
