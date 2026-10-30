package com.zynergylabs.forager.brouter.expressions

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Step 4's prediction 2 (`step4/0-prediction.txt`): the port formats floats with Kotlin's `Float.toString`, which on the
 * JVM is Java's own and on Kotlin/Native is a separate implementation. Two places reach it: `getKeyValueDescription`,
 * for a raw value v >= 1000 as `(v - 1000) / 100f`, and `BExpression.toString` for a number. This test pins Java's
 * strings so the simulator run says whether Native's agree. Expected values were printed by `Float.toString` on
 * OpenJDK 21.0.12 (CI's JVM is Temurin 21.0.13), not by Kotlin: the range as Java's `String.hashCode` and length of
 * the strings joined by '\n', the literals one by one.
 */
class FloatTextCommonTest {
    @Test
    fun rawValuesAboveOneThousandFormatAsJavaDoes() {
        val sb = StringBuilder()
        for (x in 0 until 30_000) { // v = 1000 + x, v below 31,000 as ProfileEvaluateTest's forced keys
            if (x > 0) sb.append('\n')
            sb.append((x / 100f).toString())
        }
        val text = sb.toString()
        var h = 0
        for (c in text) h = 31 * h + c.code // Java's String.hashCode, written out so no platform's hashCode is trusted
        println("FloatTextCommonTest: range hash $h length ${text.length}; samples 0.07=${(7 / 100f)} 1.0E-4=${1.0E-4f}")
        assertEquals("-1244348794 195999", "$h ${text.length}", "x / 100f for x in 0 until 30000, joined by newlines")
    }

    @Test
    fun literalsFormatAsJavaDoes() {
        val values = floatArrayOf(
            0f, 1f, -1f, 0.5f, 3.5f, 0.01f, 0.07f, 0.001f, 1.0E-4f, 9999f, 10000f, 1.0E7f, 1.2345678E7f, 100000f,
            Float.POSITIVE_INFINITY, Float.NaN, -0f, 299.99f, 1f / 3f,
        )
        val java = listOf(
            "0.0", "1.0", "-1.0", "0.5", "3.5", "0.01", "0.07", "0.001", "1.0E-4", "9999.0", "10000.0", "1.0E7",
            "1.2345678E7", "100000.0", "Infinity", "NaN", "-0.0", "299.99", "0.33333334",
        )
        val port = values.map { it.toString() }
        println("FloatTextCommonTest: literals $port")
        assertEquals(java, port)
    }
}
