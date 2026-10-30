package com.zynergylabs.forager.brouter.expressions

import com.zynergylabs.forager.brouter.util.IByteArrayUnifier

/**
 * Port of BRouter 1.7.10's `btools.expressions.BExpressionContext`. Step 4 stub: every member that computes throws
 * `NotImplementedError` ("step 4: ..."), so step 4's tests can be seen failing before the port lands. The port is split
 * across this file, `BExpressionContextLookups.kt` (the lookups half) and `BExpressionContextParse.kt` (the profile
 * reader), to keep each file under about 400 lines (`modular-code`). The constructor does nothing yet: Java's
 * registers the context with [meta] and builds the result caches.
 */
internal abstract class BExpressionContext protected constructor(
    internal var context: String,
    hashSize: Int,
    val meta: BExpressionMetaData?,
) : IByteArrayUnifier {
    /** Java's `BExpressionContext(String, BExpressionMetaData)`: hash size 4096 (`BExpressionContext.java:102-104`). */
    protected constructor(context: String, meta: BExpressionMetaData?) : this(context, 4096, meta)

    // Read by the parse checks, as the oracle's private fields of the same names are read by reflection.
    internal var nBuildInVars = 0
    internal var expressionList: List<BExpression>? = null
    internal var expressionNodeCount = 0

    internal abstract fun getBuildInVariableNames(): Array<String>

    protected fun setInverseVars() { TODO("step 4: setInverseVars") }

    fun getBuildInVariable(idx: Int): Float = TODO("step 4: getBuildInVariable($idx)")

    fun decode(ld: IntArray, inverseDirection: Boolean, ab: ByteArray) { TODO("step 4: decode") }

    fun getKeyValueDescription(inverseDirection: Boolean, ab: ByteArray): String = TODO("step 4: getKeyValueDescription")

    fun evaluate(inverseDirection: Boolean, ab: ByteArray) { TODO("step 4: evaluate") }

    override fun unify(ab: ByteArray, offset: Int, len: Int): ByteArray = TODO("step 4: unify")

    fun getOutputVariableIndex(name: String, mustExist: Boolean): Int = TODO("step 4: getOutputVariableIndex")

    fun setForeignContext(foreignContext: BExpressionContext?) { TODO("step 4: setForeignContext") }

    fun getVariableValue(name: String, defaultValue: Float): Float = TODO("step 4: getVariableValue")

    fun getLookupNameIdx(name: String): Int = TODO("step 4: getLookupNameIdx")

    fun isLookupIdxUsed(idx: Int): Boolean = TODO("step 4: isLookupIdxUsed")

    fun setAllTagsUsed() { TODO("step 4: setAllTagsUsed") }

    fun checkStartWay(ab: ByteArray): Boolean = TODO("step 4: checkStartWay")

    fun freeNoWays() { TODO("step 4: freeNoWays") }
}
