package com.zynergylabs.forager.brouter.expressions

import com.zynergylabs.forager.brouter.util.LruMapNode

/** Port of BRouter 1.7.10's `btools.expressions.VarWrapper`: a result-variable set, unified by hash and content. */
internal class VarWrapper : LruMapNode() {
    var vars: FloatArray? = null

    override fun hashCode(): Int = hash

    override fun equals(other: Any?): Boolean {
        val n = other as VarWrapper // Java casts unchecked: a ClassCastException for anything else
        if (hash != n.hash) {
            return false
        }
        return vars.contentEquals(n.vars) // Java's Arrays.equals(float[], float[]): equal by floatToIntBits
    }
}
