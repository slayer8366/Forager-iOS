package com.zynergylabs.forager.brouter.expressions

/** Port of BRouter 1.7.10's `btools.expressions.BExpressionContextNode`. Step 4 stub. */
internal class BExpressionContextNode(hashSize: Int, meta: BExpressionMetaData?) : BExpressionContext("node", hashSize, meta) {
    /** Java's `BExpressionContextNode(BExpressionMetaData)`: hash size 4096. */
    constructor(meta: BExpressionMetaData?) : this(4096, meta)

    override fun getBuildInVariableNames(): Array<String> = TODO("step 4: BExpressionContextNode.getBuildInVariableNames")

    fun getInitialcost(): Float = TODO("step 4: getInitialcost")
}
