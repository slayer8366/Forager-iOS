package com.zynergylabs.forager.brouter.codec

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertSame
import btools.codec.DataBuffers as JDataBuffers

/** Item I, step 3: `DataBuffers`' sizes are the oracle's, the given `iobuffer` is kept, and `bctx1` writes into `tagbuf1`. */
class DataBuffersTest {
    @Test
    fun buffersHaveTheOraclesSizes() {
        val j = JDataBuffers()
        val k = DataBuffers()
        assertEquals(j.iobuffer.size, k.iobuffer.size, "iobuffer")
        assertEquals(j.tagbuf1.size, k.tagbuf1.size, "tagbuf1")
        assertEquals(j.bbuf1.size, k.bbuf1.size, "bbuf1")
        assertEquals(j.ibuf1.size, k.ibuf1.size, "ibuf1")
        assertEquals(j.ibuf2.size, k.ibuf2.size, "ibuf2")
        assertEquals(j.ibuf3.size, k.ibuf3.size, "ibuf3")
        assertEquals(j.alon.size, k.alon.size, "alon")
        assertEquals(j.alat.size, k.alat.size, "alat")
    }

    @Test
    fun givenIobufferIsKeptAndBctx1WritesTagbuf1() {
        val io = ByteArray(7)
        val j = JDataBuffers(io)
        val k = DataBuffers(io)
        assertSame(io, k.iobuffer)
        for (v in listOf(5, 0, 300, 70_000)) { j.bctx1.encodeVarBits(v); k.bctx1.encodeVarBits(v) }
        assertEquals(j.bctx1.closeAndGetEncodedLength(), k.bctx1.closeAndGetEncodedLength())
        assertContentEquals(j.tagbuf1, k.tagbuf1)
        assertEquals(true, k.tagbuf1.any { it != 0.toByte() }, "bctx1 wrote nothing into tagbuf1")
    }
}
