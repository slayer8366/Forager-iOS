package com.zynergylabs.forager.brouter.expressions

import com.zynergylabs.forager.brouter.codec.TagValueValidator

private val BUILD_IN_VARIABLES = arrayOf(
    "costfactor", "turncost", "uphillcostfactor", "downhillcostfactor", "initialcost", "nodeaccessgranted",
    "initialclassifier", "trafficsourcedensity", "istrafficbackbone", "priorityclassifier", "classifiermask", "maxspeed",
    "uphillcost", "downhillcost", "uphillcutoff", "downhillcutoff", "uphillmaxslope", "downhillmaxslope",
    "uphillmaxslopecost", "downhillmaxslopecost",
)

/**
 * Port of BRouter 1.7.10's `btools.expressions.BExpressionContextWay`: the way context, its 20 built-in variables in
 * Java's order (each getter reads its index), and the access check the decoder calls.
 */
internal class BExpressionContextWay(hashSize: Int, meta: BExpressionMetaData?) :
    BExpressionContext("way", hashSize, meta), TagValueValidator {
    /** Java's `BExpressionContextWay(BExpressionMetaData)`: hash size 4096. */
    constructor(meta: BExpressionMetaData?) : this(4096, meta)

    private var decodeForbidden = true

    override fun getBuildInVariableNames(): Array<String> = BUILD_IN_VARIABLES

    fun getCostfactor(): Float = getBuildInVariable(0)
    fun getTurncost(): Float = getBuildInVariable(1)
    fun getUphillCostfactor(): Float = getBuildInVariable(2)
    fun getDownhillCostfactor(): Float = getBuildInVariable(3)
    fun getInitialcost(): Float = getBuildInVariable(4)
    fun getNodeAccessGranted(): Float = getBuildInVariable(5)
    fun getInitialClassifier(): Float = getBuildInVariable(6)
    fun getTrafficSourceDensity(): Float = getBuildInVariable(7)
    fun getIsTrafficBackbone(): Float = getBuildInVariable(8)
    fun getPriorityClassifier(): Float = getBuildInVariable(9)
    fun getClassifierMask(): Float = getBuildInVariable(10)
    fun getMaxspeed(): Float = getBuildInVariable(11)
    fun getUphillcost(): Float = getBuildInVariable(12)
    fun getDownhillcost(): Float = getBuildInVariable(13)
    fun getUphillcutoff(): Float = getBuildInVariable(14)
    fun getDownhillcutoff(): Float = getBuildInVariable(15)
    fun getUphillmaxslope(): Float = getBuildInVariable(16)
    fun getDownhillmaxslope(): Float = getBuildInVariable(17)
    fun getUphillmaxslopecost(): Float = getBuildInVariable(18)
    fun getDownhillmaxslopecost(): Float = getBuildInVariable(19)

    /** `BExpressionContextWay.java:113-126`: 2 = accessible, 1 = no matching, 0 = nothing. */
    override fun accessType(tagValueSet: ByteArray): Int {
        evaluate(false, tagValueSet)
        var minCostFactor = getCostfactor()
        if (minCostFactor >= 9999f) {
            setInverseVars()
            val reverseCostFactor = getCostfactor()
            if (reverseCostFactor < minCostFactor) {
                minCostFactor = reverseCostFactor
            }
        }
        return if (minCostFactor < 9999f) 2 else if (decodeForbidden) (if (minCostFactor < 10000f) 1 else 0) else 0
    }

    /** Kept on the way context (RECORD -39), though `TagValueValidator` here does not declare it. */
    fun setDecodeForbidden(decodeForbidden: Boolean) {
        this.decodeForbidden = decodeForbidden
    }
}
