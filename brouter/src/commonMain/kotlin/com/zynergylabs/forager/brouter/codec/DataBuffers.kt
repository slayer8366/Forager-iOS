package com.zynergylabs.forager.brouter.codec

import com.zynergylabs.forager.brouter.util.BitCoderContext

/** BRouter 1.7.10's `btools.codec.DataBuffers`: re-usable buffers for the decoder. STUB (step 3, tests first). */
internal class DataBuffers(iobuffer: ByteArray) {
    var iobuffer: ByteArray = TODO("step 3: DataBuffers")
    var tagbuf1: ByteArray = TODO("step 3: DataBuffers")
    var bctx1: BitCoderContext = TODO("step 3: DataBuffers")
    var bbuf1: ByteArray = TODO("step 3: DataBuffers")
    var ibuf1: IntArray = TODO("step 3: DataBuffers")
    var ibuf2: IntArray = TODO("step 3: DataBuffers")
    var ibuf3: IntArray = TODO("step 3: DataBuffers")
    var alon: IntArray = TODO("step 3: DataBuffers")
    var alat: IntArray = TODO("step 3: DataBuffers")

    constructor() : this(ByteArray(0))
}
