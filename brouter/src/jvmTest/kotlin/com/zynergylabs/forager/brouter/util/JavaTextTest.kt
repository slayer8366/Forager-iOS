package com.zynergylabs.forager.brouter.util

import java.io.BufferedReader
import java.io.StringReader
import java.util.StringTokenizer
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Item I, step 4: the Java text helpers `expressions` calls (prediction 1, `step4/0-prediction.txt`) against the JDK the
 * oracle runs on: `StringTokenizer` with the two delimiters BRouter passes, `String.trim` on every char and on seeded
 * strings, and `BufferedReader.readLine` on seeded texts, across its 8,192-char buffer edge too. The first test checks
 * `JavaTextCases`' expected values against the JDK.
 */
class JavaTextTest {
    private fun jdkTokens(s: String, delim: Char): List<String> {
        val tk = StringTokenizer(s, delim.toString())
        val out = ArrayList<String>()
        while (tk.hasMoreTokens()) out += tk.nextToken()
        return out
    }

    private fun jdkTrim(s: String): String = (s as java.lang.String).trim()

    private fun jdkLines(s: String): List<String> {
        val br = BufferedReader(StringReader(s))
        return generateSequence { br.readLine() }.toList()
    }

    @Test
    fun theCommonCasesExpectedValuesAreTheJdks() {
        for ((args, expected) in JavaTextCases.tokens) {
            assertEquals(jdkTokens(args.first, args.second), expected, "\"${args.first}\" by '${args.second}'")
        }
        for ((input, expected) in JavaTextCases.trim) assertEquals(jdkTrim(input), expected, "\"$input\".trim()")
        for ((input, expected) in JavaTextCases.lines) assertEquals(jdkLines(input), expected, "readLine over \"$input\"")
    }

    @Test
    fun javaStringTokenizerEqualsStringTokenizer() {
        val pieces = listOf(" ", " ", "|", "||", "a", "b", "yes", "\t", ";", " ", "\n")
        val r = Random(41)
        for (delim in listOf(' ', '|')) repeat(50_000) {
            val s = buildString { repeat(r.nextInt(0, 10)) { append(pieces[r.nextInt(pieces.size)]) } }
            val jdk = StringTokenizer(s, delim.toString())
            val port = JavaStringTokenizer(s, delim)
            // Bounded: a string of n chars has at most n tokens.
            for (i in 0..s.length + 1) {
                assertEquals(jdk.countTokens(), port.countTokens(), "countTokens, \"$s\" by '$delim', after $i tokens")
                val more = jdk.hasMoreTokens()
                assertEquals(more, port.hasMoreTokens(), "hasMoreTokens, \"$s\" by '$delim', after $i tokens")
                if (!more) break
                assertEquals(jdk.nextToken(), port.nextToken(), "nextToken ${i + 1}, \"$s\" by '$delim'")
            }
            val jdkEnd = assertFailsWith<NoSuchElementException> { jdk.nextToken() }
            val portEnd = assertFailsWith<NoSuchElementException>("\"$s\" by '$delim'") { port.nextToken() }
            assertEquals(jdkEnd.message, portEnd.message, "the end's message, \"$s\" by '$delim'")
        }
    }

    @Test
    fun javaTrimEqualsStringTrimOnEveryCharAndOnSeededStrings() {
        for (code in 0..0xFFFF) {
            val c = code.toChar()
            for (s in listOf("$c", "${c}x$c", "x$c", "${c}x")) {
                assertEquals(jdkTrim(s), s.javaTrim(), "U+${code.toString(16)} in \"$s\"")
            }
        }
        val pieces = listOf(" ", "\u0000", "\t", "\n", "\u001f", " ", "!", " ", "　", "a", "=", "\u0085")
        val r = Random(43)
        repeat(100_000) {
            val s = buildString { repeat(r.nextInt(0, 8)) { append(pieces[r.nextInt(pieces.size)]) } }
            assertEquals(jdkTrim(s), s.javaTrim(), "\"$s\"")
        }
    }

    @Test
    fun javaReadLinesEqualsBufferedReaderReadLine() {
        val pieces = listOf("\r", "\n", "\r\n", "a", "b", " ", "#", " ", "\u0085", "\u000b", "\u000c")
        val r = Random(47)
        repeat(100_000) {
            val s = buildString { repeat(r.nextInt(0, 10)) { append(pieces[r.nextInt(pieces.size)]) } }
            assertEquals(jdkLines(s), javaReadLines(s), "\"$s\"")
        }
        // BufferedReader reads 8,192 chars at a time: a "\r\n" split across that edge is still one line end.
        for (n in 8_185..8_198) for (end in listOf("\r\n", "\r", "\n", "\r\r", "\n\r")) {
            val s = "a".repeat(n) + end + "b" + end
            assertEquals(jdkLines(s), javaReadLines(s), "${n} chars, then \"$end\"")
        }
    }
}
