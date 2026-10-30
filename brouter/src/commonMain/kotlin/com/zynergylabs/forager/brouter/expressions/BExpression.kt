package com.zynergylabs.forager.brouter.expressions

/** Port of BRouter 1.7.10's `btools.expressions.BExpression`. Step 4 stub: not yet ported. */
internal class BExpression {
    var doNotChange: Boolean = false

    fun evaluate(ctx: BExpressionContext?): Float = TODO("step 4: BExpression.evaluate")

    companion object {
        fun parse(ctx: BExpressionContext, level: Int): BExpression? = TODO("step 4: BExpression.parse")

        fun createAssignExpressionFromKeyValue(ctx: BExpressionContext, key: String, value: String): BExpression = TODO("step 4: createAssignExpressionFromKeyValue")
    }
}
