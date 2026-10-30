package com.zynergylabs.forager.brouter.codec

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import btools.codec.NoisyDiffCoder as JNoisyDiffCoder
import btools.codec.StatCoderContext as JStatCoderContext

/**
 * Item I, step 3: the oracle's three-pass `NoisyDiffCoder` encoder writes two interleaved coders' dictionaries and values,
 * with a raw var-bits marker between them, as `DirectWeaver` reads several coders from one stream. The oracle and the port
 * decode it; values and reading positions are compared exactly.
 */
class NoisyDiffCoderTest {
    @Test
    fun threePassStreamsDecodeIdentically() {
        var compared = 0
        var expected = 0
        for (seed in 1..20) {
            val r = Random(seed)
            val n = if (seed == 1) 0 else r.nextInt(1, 3_000)
            expected += n
            val scaleA = 1 shl r.nextInt(0, 20)
            val scaleB = 1 shl r.nextInt(0, 20)
            val a = IntArray(n) { r.nextInt(-scaleA, scaleA + 1) }
            val b = IntArray(n) { if (r.nextInt(50) == 0) r.nextInt(-(1 shl 24), 1 shl 24) else r.nextInt(-scaleB, scaleB + 1) }
            val encA = JNoisyDiffCoder()
            val encB = JNoisyDiffCoder()
            var buf = ByteArray(0)
            for (pass in 1..3) {
                buf = ByteArray(1 shl 20)
                val ctx = JStatCoderContext(buf)
                encA.encodeDictionary(ctx)
                encB.encodeDictionary(ctx)
                for (i in 0 until n) {
                    encA.encodeSignedValue(a[i])
                    ctx.encodeVarBits(i and 7)
                    encB.encodeSignedValue(b[i])
                }
                ctx.closeAndGetEncodedLength()
            }
            val dj = JStatCoderContext(buf)
            val dk = StatCoderContext(buf)
            val cjA = JNoisyDiffCoder(dj)
            val ckA = NoisyDiffCoder(dk)
            val cjB = JNoisyDiffCoder(dj)
            val ckB = NoisyDiffCoder(dk)
            assertEquals(dj.readingBitPosition, dk.getReadingBitPosition(), "seed $seed: after the dictionaries")
            for (i in 0 until n) {
                val at = "seed $seed value $i"
                val vA = ckA.decodeSignedValue()
                assertEquals(cjA.decodeSignedValue(), vA, "$at: coder A")
                assertEquals(a[i], vA, "$at: coder A round trip")
                assertEquals(dj.decodeVarBits(), dk.decodeVarBits(), "$at: marker")
                val vB = ckB.decodeSignedValue()
                assertEquals(cjB.decodeSignedValue(), vB, "$at: coder B")
                assertEquals(b[i], vB, "$at: coder B round trip")
                assertEquals(dj.readingBitPosition, dk.getReadingBitPosition(), "$at: reading position")
                compared++
            }
        }
        assertEquals(expected, compared, "values compared")
        assertEquals(true, expected > 0, "no values were generated")
    }
}
