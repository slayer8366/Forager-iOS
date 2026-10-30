package com.zynergylabs.forager.brouter.codec

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import com.zynergylabs.forager.brouter.util.BitCoderContext
import btools.codec.DataBuffers as JDataBuffers
import btools.codec.TagValueCoder as JTagValueCoder
import btools.codec.TagValueValidator as JTagValueValidator
import btools.util.BitCoderContext as JBitCoderContext

/**
 * Item I, step 3: the oracle's three-pass `TagValueCoder` encoder writes a Huffman tree of tag value sets and a stream of
 * codes; the oracle and the port decode both. Each decoded wrapper's bytes and access type, every reading position, and
 * the order and arguments of every validator call are compared exactly.
 */
class TagValueCoderTest {
    /** One tag value set as the engine stores it: var-bits (index delta, value) pairs ending in a zero delta. */
    private fun tagValueSet(r: Random): ByteArray? {
        if (r.nextInt(12) == 0) return null
        val pairs = if (r.nextInt(12) == 0) 0 else r.nextInt(1, 20)
        val buf = ByteArray(256)
        val c = JBitCoderContext(buf)
        for (p in 0 until pairs) {
            c.encodeVarBits(r.nextInt(1, 12))
            c.encodeVarBits(if (r.nextBoolean()) r.nextInt(4) else r.nextInt(1 shl 10))
        }
        c.encodeVarBits(0)
        return buf.copyOf(c.closeAndGetEncodedLength())
    }

    /** The same rule on both sides, recording every call in order. */
    private class Rule(val log: MutableList<String>) {
        fun accessType(d: ByteArray): Int = (d.size % 3).also { log += "accessType ${d.contentToString()} = $it" }
        fun unify(d: ByteArray, off: Int, len: Int): ByteArray = d.copyOfRange(off, off + len).also { log += "unify $off $len ${it.contentToString()}" }
        fun isLookupIdxUsed(idx: Int): Boolean = (idx % 3 != 1).also { log += "isLookupIdxUsed $idx = $it" }
    }

    private class JValidator(val rule: Rule) : JTagValueValidator {
        override fun accessType(tagValueSet: ByteArray): Int = rule.accessType(tagValueSet)
        override fun unify(tagValueSet: ByteArray, offset: Int, len: Int): ByteArray = rule.unify(tagValueSet, offset, len)
        override fun isLookupIdxUsed(idx: Int): Boolean = rule.isLookupIdxUsed(idx)
        override fun setDecodeForbidden(decodeForbidden: Boolean): Unit = error("not called by the decoder")
        override fun checkStartWay(ab: ByteArray): Boolean = error("not called by the decoder")
    }

    private class KValidator(val rule: Rule) : TagValueValidator {
        override fun accessType(tagValueSet: ByteArray): Int = rule.accessType(tagValueSet)
        override fun unify(tagValueSet: ByteArray, offset: Int, len: Int): ByteArray = rule.unify(tagValueSet, offset, len)
        override fun isLookupIdxUsed(idx: Int): Boolean = rule.isLookupIdxUsed(idx)
        override fun checkStartWay(ab: ByteArray): Boolean = error("not called by the decoder")
    }

    @Test
    fun oracleEncodedTreesAndCodesDecodeIdentically() {
        var compared = 0
        var expected = 0
        var calls = 0
        for (seed in 1..24) {
            val r = Random(seed)
            val pool = List(if (seed == 1) 1 else r.nextInt(1, 120)) { tagValueSet(r) }
            val n = if (seed == 2) 0 else r.nextInt(1, 2_000)
            expected += n
            // Skewed picks, so the Huffman tree has depth; seed 2 encodes no set at all (the encoder's dummy leaf).
            val picks = IntArray(n) { r.nextInt(r.nextInt(pool.size) + 1) }
            val enc = JTagValueCoder()
            var buf = ByteArray(0)
            for (pass in 1..3) {
                buf = ByteArray(1 shl 20)
                val ctx = JBitCoderContext(buf)
                enc.encodeDictionary(ctx)
                for (i in 0 until n) {
                    enc.encodeTagValueSet(pool[picks[i]])
                    ctx.encodeVarBits(i and 3)
                }
                ctx.closeAndGetEncodedLength()
            }
            val withValidator = seed % 2 == 0
            val logJ = mutableListOf<String>()
            val logK = mutableListOf<String>()
            val dj = JBitCoderContext(buf)
            val dk = BitCoderContext(buf)
            val cj = JTagValueCoder(dj, JDataBuffers(), if (withValidator) JValidator(Rule(logJ)) else null)
            val ck = TagValueCoder(dk, DataBuffers(), if (withValidator) KValidator(Rule(logK)) else null)
            assertEquals(dj.readingBitPosition, dk.getReadingBitPosition(), "seed $seed: after the tree")
            assertEquals(logJ, logK, "seed $seed: validator calls")
            // Seed 2 encodes no set: its tree is the encoder's dummy leaf, an empty set, which returns before any
            // validator call, in the oracle too (logJ is empty there). Every other validator seed must call it.
            if (withValidator && n > 0) assertEquals(true, logK.isNotEmpty(), "seed $seed: the validator was never called")
            calls += logK.size
            for (i in 0 until n) {
                val at = "seed $seed code $i"
                val wj = cj.decodeTagValueSet()
                val wk = ck.decodeTagValueSet()
                assertEquals(wj == null, wk == null, "$at: null")
                if (wj != null && wk != null) {
                    assertContentEquals(wj.data, wk.data, "$at: data")
                    assertEquals(wj.accessType, wk.accessType, "$at: accessType")
                }
                if (!withValidator) {
                    // Without a validator the decoder returns the set itself, or null for an empty one.
                    val original = pool[picks[i]]
                    if (original == null || JBitCoderContext(original).decodeVarBits() == 0) assertEquals(null, wk, "$at: empty set")
                    else { assertContentEquals(original, wk!!.data, "$at: round trip"); assertEquals(2, wk.accessType, "$at: accessType") }
                }
                assertEquals(dj.decodeVarBits(), dk.decodeVarBits(), "$at: marker")
                assertEquals(dj.readingBitPosition, dk.getReadingBitPosition(), "$at: reading position")
                compared++
            }
        }
        assertEquals(expected, compared, "codes compared")
        assertEquals(true, calls > 0, "no validator call was compared")
    }
}
