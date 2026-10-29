package com.zynergylabs.forager.brouter.util

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Item I, step 2a: the port's Java-semantics helpers against Java's answers (`JavaLangCases`), on every target this
 * module's tests run on. On the JVM, `JavaLangTest` adds the exhaustive and seeded comparisons against the JDK.
 */
class JavaLangCommonTest {
    @Test
    fun javaRoundGivesJavasAnswers() {
        for ((input, expected) in JavaLangCases.round) assertEquals(expected, javaRound(input), "javaRound($input)")
    }

    @Test
    fun javaIsWhitespaceGivesJavasAnswers() {
        for ((c, expected) in JavaLangCases.whitespace) {
            assertEquals(expected, javaIsWhitespace(c), "javaIsWhitespace(U+${c.code.toString(16)})")
        }
    }

    @Test
    fun javaSplitGivesJavasAnswers() {
        for ((args, expected) in JavaLangCases.split) {
            assertEquals(expected, args.first.javaSplit(args.second).toList(), "\"${args.first}\".javaSplit(\"${args.second}\")")
        }
    }
}
