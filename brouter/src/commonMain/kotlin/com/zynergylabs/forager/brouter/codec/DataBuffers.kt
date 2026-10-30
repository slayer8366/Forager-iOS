package com.zynergylabs.forager.brouter.codec

import com.zynergylabs.forager.brouter.util.BitCoderContext

/** Port of BRouter 1.7.10's `btools.codec.DataBuffers`: re-usable buffers for the decoder. */
internal class DataBuffers(iobuffer: ByteArray) {
    var iobuffer: ByteArray = iobuffer
    var tagbuf1: ByteArray = ByteArray(256)
    var bctx1: BitCoderContext = BitCoderContext(tagbuf1)
    var bbuf1: ByteArray = ByteArray(65636)
    var ibuf1: IntArray = IntArray(4096)
    var ibuf2: IntArray = IntArray(2048)
    var ibuf3: IntArray = IntArray(2048)
    var alon: IntArray = IntArray(2048)
    var alat: IntArray = IntArray(2048)

    constructor() : this(ByteArray(65636))
}
