package com.zynergylabs.forager.brouter.expressions

import com.zynergylabs.forager.brouter.codec.TagValueValidator

/** Port of BRouter 1.7.10's `btools.expressions.BExpressionContextWay`. Step 4 stub. */
internal class BExpressionContextWay(hashSize: Int, meta: BExpressionMetaData?) :
    BExpressionContext("way", hashSize, meta), TagValueValidator {
    /** Java's `BExpressionContextWay(BExpressionMetaData)`: hash size 4096. */
    constructor(meta: BExpressionMetaData?) : this(4096, meta)

    override fun getBuildInVariableNames(): Array<String> = TODO("step 4: BExpressionContextWay.getBuildInVariableNames")

    fun getCostfactor(): Float = TODO("step 4: getCostfactor")

    override fun accessType(tagValueSet: ByteArray): Int = TODO("step 4: accessType")

    /** Kept on the way context (RECORD -39), though `TagValueValidator` here does not declare it. */
    fun setDecodeForbidden(decodeForbidden: Boolean) { TODO("step 4: setDecodeForbidden") }
}
