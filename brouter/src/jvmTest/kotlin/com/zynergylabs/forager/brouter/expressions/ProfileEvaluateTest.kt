package com.zynergylabs.forager.brouter.expressions

import java.io.File
import java.util.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import btools.expressions.BExpressionContext as OracleContext

/**
 * Item I, step 4, the handoff's vectors: tag sets from the oracle's own `generateRandomValues(Random(seed))`, some with
 * keys forced to `1000 + x` (the numeric values a tile carries), encoded by the oracle's `encode`. Each is evaluated by
 * both engines in both directions, and the built-in variables are compared by raw bits, with `getKeyValueDescription`,
 * and on the way context `accessType` and `checkStartWay`. The vectors come from a third set of oracle contexts, so the
 * two engines under comparison see exactly the same calls.
 */
class ProfileEvaluateTest {
    private val vectorsPerProfile = 300

    /** [count] encoded tag sets from [source]: random values, and in every third one up to three keys at 1000 + x. */
    private fun vectors(source: OracleContext, seed: Long, count: Int): List<ByteArray> {
        val rnd = Random(seed)
        val out = ArrayList<ByteArray>()
        for (i in 0 until count) {
            val ld = source.generateRandomValues(rnd)
            if (i % 3 == 2) repeat(1 + rnd.nextInt(3)) { ld[1 + rnd.nextInt(ld.size - 1)] = 1000 + rnd.nextInt(30_000) }
            source.encode(ld)?.let { out += it } // null when every tag is 0: no description to evaluate
        }
        return out
    }

    private fun bits(f: Float): Int = java.lang.Float.floatToRawIntBits(f)

    private fun compareOne(name: String, o: OracleContext, p: BExpressionContext, ab: ByteArray, n: Int, k: Int) {
        for (inverse in listOf(false, true)) {
            o.evaluate(inverse, ab)
            p.evaluate(inverse, ab)
            for (idx in 0 until n) {
                assertEquals(bits(o.getBuildInVariable(idx)), bits(p.getBuildInVariable(idx)), "$name vector $k inverse $inverse built-in $idx")
            }
            assertEquals(o.getKeyValueDescription(inverse, ab), p.getKeyValueDescription(inverse, ab), "$name vector $k inverse $inverse description")
        }
    }

    private fun check(profile: File, index: Int) {
        val rc = ExpressionsEngines.foragerRoutingContext(profile)
        val source = ExpressionsEngines.oracle(profile, rc)
        val oracle = ExpressionsEngines.oracle(profile, rc)
        val wayN = ExpressionsEngines.oracleField(oracle.way, "nBuildInVars") as Int
        val nodeN = ExpressionsEngines.oracleField(oracle.node, "nBuildInVars") as Int
        val wayVectors = vectors(source.way, 4_000L + index, vectorsPerProfile)
        val nodeVectors = vectors(source.node, 4_500L + index, vectorsPerProfile)
        // A sample that lost most of its vectors to null encodings would pass without testing much: count them.
        check(wayVectors.size > vectorsPerProfile / 2 && nodeVectors.size > vectorsPerProfile / 4) {
            "${profile.name}: only ${wayVectors.size} way and ${nodeVectors.size} node vectors encoded"
        }
        val port = ExpressionsEngines.port(profile, rc) // after the vectors: their count is checked on a stub run too
        for ((k, ab) in wayVectors.withIndex()) {
            compareOne("way", oracle.way, port.way, ab, wayN, k)
            assertEquals(oracle.way.accessType(ab), port.way.accessType(ab), "way vector $k accessType")
            assertEquals(oracle.way.checkStartWay(ab), port.way.checkStartWay(ab), "way vector $k checkStartWay")
        }
        for ((k, ab) in nodeVectors.withIndex()) compareOne("node", oracle.node, port.node, ab, nodeN, k)
    }

    @Test
    fun everyProfileEvaluatesTheOraclesVectorsToTheOraclesBits() {
        var index = 0
        ExpressionsEngines.forEveryProfile { profile -> check(profile, index++) }
    }
}
