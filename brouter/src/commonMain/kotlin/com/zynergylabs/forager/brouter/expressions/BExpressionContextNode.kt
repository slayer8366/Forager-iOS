package com.zynergylabs.forager.brouter.expressions

private val BUILD_IN_VARIABLES = arrayOf("initialcost")

/** Port of BRouter 1.7.10's `btools.expressions.BExpressionContextNode`: the node context and its one variable. */
internal class BExpressionContextNode(hashSize: Int, meta: BExpressionMetaData?) : BExpressionContext("node", hashSize, meta) {
    /** Java's `BExpressionContextNode(BExpressionMetaData)`: hash size 4096. */
    constructor(meta: BExpressionMetaData?) : this(4096, meta)

    override fun getBuildInVariableNames(): Array<String> = BUILD_IN_VARIABLES

    fun getInitialcost(): Float = getBuildInVariable(0)
}
