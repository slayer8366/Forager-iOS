package com.zynergylabs.forager.brouter.codec

import com.zynergylabs.forager.brouter.util.BitCoderContext

/** BRouter 1.7.10's `btools.codec.TagValueCoder`, the decoder half. STUB (step 3, tests first). */
internal class TagValueCoder(bc: BitCoderContext, buffers: DataBuffers, validator: TagValueValidator?) {
    init {
        TODO("step 3: TagValueCoder")
    }

    fun decodeTagValueSet(): TagValueWrapper? = TODO("step 3: decodeTagValueSet")

    class TreeNode {
        var child1: Any? = null
        var child2: Any? = null
    }
}
