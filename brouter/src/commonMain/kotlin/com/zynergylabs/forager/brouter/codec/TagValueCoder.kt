package com.zynergylabs.forager.brouter.codec

import com.zynergylabs.forager.brouter.util.BitCoderContext

/**
 * Port of BRouter 1.7.10's `btools.codec.TagValueCoder`, the decoder half: the decoding constructor reads the Huffman
 * tree of tag value sets, and [decodeTagValueSet] walks it per code. The encoder (`encodeTagValueSet`,
 * `encodeDictionary`, `identityMap`, `pass`, `nextTagValueSetId`, `TagValueSet` and its comparator) and the
 * no-argument constructor are write-side only.
 */
internal class TagValueCoder(bc: BitCoderContext, buffers: DataBuffers, validator: TagValueValidator?) {
    // Java's order: the tree is read first, then the context is kept for the codes.
    private val tree: Any? = decodeTree(bc, buffers, validator)
    private val bc: BitCoderContext = bc

    /** Java's `decodeTagValueSet`. The walk ends at a leaf: the tree is finite, and every step goes one level down. */
    fun decodeTagValueSet(): TagValueWrapper? {
        var node = tree
        while (node is TreeNode) {
            val nextBit = bc.decodeBit()
            node = if (nextBit) node.child2 else node.child1
        }
        return node as TagValueWrapper?
    }

    /**
     * Java's `decodeTree`. Its loop ends at the first zero delta, as Java's does; on a stream that never gives one it
     * runs as Java's would (past the buffer's end the context reads zero bits).
     */
    private fun decodeTree(bc: BitCoderContext, buffers: DataBuffers, validator: TagValueValidator?): Any? {
        val isNode = bc.decodeBit()
        if (isNode) {
            val node = TreeNode()
            node.child1 = decodeTree(bc, buffers, validator)
            node.child2 = decodeTree(bc, buffers, validator)
            return node
        }

        val buffer = buffers.tagbuf1
        val ctx = buffers.bctx1
        ctx.reset(buffer)

        var inum = 0
        var lastEncodedInum = 0

        var hasdata = false
        while (true) {
            val delta = bc.decodeVarBits()
            if (!hasdata) {
                if (delta == 0) {
                    return null
                }
            }
            if (delta == 0) {
                ctx.encodeVarBits(0)
                break
            }
            inum += delta

            val data = bc.decodeVarBits()

            if (validator == null || validator.isLookupIdxUsed(inum)) {
                hasdata = true
                ctx.encodeVarBits(inum - lastEncodedInum)
                ctx.encodeVarBits(data)
                lastEncodedInum = inum
            }
        }

        val len = ctx.closeAndGetEncodedLength()
        val res: ByteArray = if (validator == null) buffer.copyOfRange(0, len) else validator.unify(buffer, 0, len)

        val accessType = validator?.accessType(res) ?: 2
        if (accessType > 0) {
            val w = TagValueWrapper()
            w.data = res
            w.accessType = accessType
            return w
        }
        return null
    }

    class TreeNode {
        var child1: Any? = null
        var child2: Any? = null
    }
}
