package com.zynergylabs.forager.brouter.util

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Step 7, session 24: `StringUtils` whole against the oracle's, on seeded strings drawn from the escaped characters,
 * ASCII, and other code units (surrogates and non-ASCII included), plus the empty string and null (Java and Kotlin
 * both throw `NullPointerException` there; compared by class, since the message is each runtime's own, RECORD -116).
 */
class StringUtilsTest {
    private fun res(f: () -> String): String = try {
        "ok:" + f()
    } catch (e: Throwable) {
        // A stub's message names it; any other exception is compared by class only (its message is the runtime's own).
        "throws " + e.javaClass.name + (if (e is NotImplementedError) ": " + e.message else "")
    }

    @Test
    fun stringUtilsMatchesTheOracle() {
        val rnd = Random(20261132L)
        val special = "&<>'\"\t\n\r\\/"
        val inputs = ArrayList<String?>(listOf(null, "", "plain", special, "&&", "a/b\\c"))
        repeat(20000) {
            val n = rnd.nextInt(0, 40)
            inputs += buildString {
                repeat(n) {
                    append(
                        when (rnd.nextInt(4)) {
                            0 -> special[rnd.nextInt(special.length)]
                            1 -> (32 + rnd.nextInt(95)).toChar()
                            2 -> rnd.nextInt(0, 0x10000).toChar()
                            else -> "äöü€中"[rnd.nextInt(5)]
                        }
                    )
                }
            }
        }
        for ((i, s) in inputs.withIndex()) {
            assertEquals(res { btools.util.StringUtils.escapeXml10(s) }, res { StringUtils.escapeXml10(s) }, "escapeXml10, input $i")
            assertEquals(res { btools.util.StringUtils.escapeJson(s) }, res { StringUtils.escapeJson(s) }, "escapeJson, input $i")
        }
        println("7 StringUtils: ${inputs.size} inputs, both methods")
    }
}
