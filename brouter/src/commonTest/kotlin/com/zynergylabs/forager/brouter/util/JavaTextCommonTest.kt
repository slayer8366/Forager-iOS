package com.zynergylabs.forager.brouter.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Item I, step 4: the port's Java text helpers against Java's answers (`JavaTextCases`), on every target this module's
 * tests run on. On the JVM, `JavaTextTest` adds seeded and exhaustive comparisons against the JDK.
 */
class JavaTextCommonTest {
    @Test
    fun javaStringTokenizerGivesJavasTokens() {
        for ((args, expected) in JavaTextCases.tokens) {
            val tk = JavaStringTokenizer(args.first, args.second)
            assertEquals(expected.size, tk.countTokens(), "countTokens of \"${args.first}\" by '${args.second}'")
            val got = ArrayList<String>()
            // Bounded: a string of n chars has at most n tokens, so a tokenizer that does not advance fails, not hangs.
            for (i in 0..args.first.length) if (tk.hasMoreTokens()) got += tk.nextToken() else break
            assertEquals(expected, got, "tokens of \"${args.first}\" by '${args.second}'")
            assertEquals(0, tk.countTokens(), "countTokens at the end of \"${args.first}\"")
            assertFailsWith<NoSuchElementException>("nextToken past the end of \"${args.first}\"") { tk.nextToken() }
        }
    }

    @Test
    fun javaTrimGivesJavasAnswers() {
        for ((input, expected) in JavaTextCases.trim) assertEquals(expected, input.javaTrim(), "\"$input\".javaTrim()")
    }

    @Test
    fun javaReadLinesGivesJavasLines() {
        for ((input, expected) in JavaTextCases.lines) assertEquals(expected, javaReadLines(input), "javaReadLines(\"$input\")")
    }
}
