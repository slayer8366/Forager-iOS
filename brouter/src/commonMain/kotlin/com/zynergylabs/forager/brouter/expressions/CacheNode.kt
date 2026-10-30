package com.zynergylabs.forager.brouter.expressions

import com.zynergylabs.forager.brouter.util.LruMapNode

/** Port of BRouter 1.7.10's `btools.expressions.CacheNode`: a tag description and its evaluated variables. */
internal class CacheNode : LruMapNode() {
    var ab: ByteArray? = null
    var vars: FloatArray? = null

    override fun hashCode(): Int = hash

    override fun equals(other: Any?): Boolean {
        val n = other as CacheNode
        if (hash != n.hash) {
            return false
        }
        if (ab == null) {
            return true // hack: null = crc match only
        }
        return ab.contentEquals(n.ab)
    }
}
