package com.zynergylabs.forager.brouter.expressions

import com.zynergylabs.forager.brouter.util.JavaStringTokenizer

/**
 * Port of BRouter 1.7.10's `btools.expressions.BExpression`: one node of a profile's expression tree, its parser and
 * its float evaluation, in Java's operand order. Comparisons on `Float` here are IEEE's, as Java's are.
 */
internal class BExpression {
    private var typ = 0
    private var op1: BExpression? = null
    private var op2: BExpression? = null
    private var op3: BExpression? = null
    private var numberValue = 0f
    private var variableIdx = 0
    private var lookupNameIdx = -1
    private var lookupValueIdxArray: IntArray? = null
    private var doNotChange = false

    private fun markLookupIdxUsed(ctx: BExpressionContext): Int {
        var nodeCount = 1
        if (lookupNameIdx >= 0) {
            ctx.markLookupIdxUsed(lookupNameIdx)
        }
        op1?.let { nodeCount += it.markLookupIdxUsed(ctx) }
        op2?.let { nodeCount += it.markLookupIdxUsed(ctx) }
        op3?.let { nodeCount += it.markLookupIdxUsed(ctx) }
        return nodeCount
    }

    // Evaluate the expression
    fun evaluate(ctx: BExpressionContext?): Float = when (typ) {
        OR_EXP -> if (op1!!.evaluate(ctx) != 0f) 1f else (if (op2!!.evaluate(ctx) != 0f) 1f else 0f)
        XOR_EXP -> if ((op1!!.evaluate(ctx) != 0f) xor (op2!!.evaluate(ctx) != 0f)) 1f else 0f
        AND_EXP -> if (op1!!.evaluate(ctx) != 0f) (if (op2!!.evaluate(ctx) != 0f) 1f else 0f) else 0f
        ADD_EXP -> op1!!.evaluate(ctx) + op2!!.evaluate(ctx)
        SUB_EXP -> op1!!.evaluate(ctx) - op2!!.evaluate(ctx)
        MULTIPLY_EXP -> op1!!.evaluate(ctx) * op2!!.evaluate(ctx)
        DIVIDE_EXP -> divide(op1!!.evaluate(ctx), op2!!.evaluate(ctx))
        MAX_EXP -> max(op1!!.evaluate(ctx), op2!!.evaluate(ctx))
        MIN_EXP -> min(op1!!.evaluate(ctx), op2!!.evaluate(ctx))
        EQUAL_EXP -> if (op1!!.evaluate(ctx) == op2!!.evaluate(ctx)) 1f else 0f
        GREATER_EXP -> if (op1!!.evaluate(ctx) > op2!!.evaluate(ctx)) 1f else 0f
        LESSER_EXP -> if (op1!!.evaluate(ctx) < op2!!.evaluate(ctx)) 1f else 0f
        SWITCH_EXP -> if (op1!!.evaluate(ctx) != 0f) op2!!.evaluate(ctx) else op3!!.evaluate(ctx)
        ASSIGN_EXP -> ctx!!.assign(variableIdx, op1!!.evaluate(ctx))
        LOOKUP_EXP -> ctx!!.getLookupMatch(lookupNameIdx, lookupValueIdxArray!!)
        NUMBER_EXP -> numberValue
        VARIABLE_EXP -> ctx!!.getVariableValue(variableIdx)
        FOREIGN_VARIABLE_EXP -> ctx!!.getForeignVariableValue(variableIdx)
        VARIABLE_GET_EXP -> ctx!!.getLookupValue(lookupNameIdx)
        NOT_EXP -> if (op1!!.evaluate(ctx) == 0f) 1f else 0f
        else -> throw IllegalArgumentException("unknown op-code: $typ")
    }

    // Try to collapse the expression if logically possible
    private fun tryCollapse(): BExpression {
        val o1 = op1
        val o2 = op2
        return when (typ) {
            OR_EXP -> if (NUMBER_EXP == o1!!.typ) {
                if (o1.numberValue != 0f) o1 else o2!!
            } else if (NUMBER_EXP == o2!!.typ) {
                if (o2.numberValue != 0f) o2 else o1
            } else {
                this
            }
            AND_EXP -> if (NUMBER_EXP == o1!!.typ) {
                if (o1.numberValue == 0f) o1 else o2!!
            } else if (NUMBER_EXP == o2!!.typ) {
                if (o2.numberValue == 0f) o2 else o1
            } else {
                this
            }
            ADD_EXP -> if (NUMBER_EXP == o1!!.typ) {
                if (o1.numberValue == 0f) o2!! else this
            } else if (NUMBER_EXP == o2!!.typ) {
                if (o2.numberValue == 0f) o1 else this
            } else {
                this
            }
            SWITCH_EXP -> if (NUMBER_EXP == o1!!.typ) {
                if (o1.numberValue == 0f) op3!! else o2!!
            } else {
                this
            }
            else -> this
        }
    }

    // Try to evaluate the expression if all operands are constant
    private fun tryEvaluateConstant(): BExpression {
        val o1 = op1
        val o2 = op2
        val o3 = op3
        if (o1 != null && NUMBER_EXP == o1.typ &&
            (o2 == null || NUMBER_EXP == o2.typ) &&
            (o3 == null || NUMBER_EXP == o3.typ)
        ) {
            val exp = BExpression()
            exp.typ = NUMBER_EXP
            exp.numberValue = evaluate(null)
            return exp
        }
        return this
    }

    private fun max(v1: Float, v2: Float): Float = if (v1 > v2) v1 else v2

    private fun min(v1: Float, v2: Float): Float = if (v1 < v2) v1 else v2

    private fun divide(v1: Float, v2: Float): Float {
        if (v2 == 0f) throw IllegalArgumentException("div by zero")
        return v1 / v2
    }

    override fun toString(): String {
        if (typ == NUMBER_EXP) {
            return "" + numberValue
        }
        if (typ == VARIABLE_EXP) {
            return "vidx=$variableIdx"
        }
        val sb = StringBuilder("typ=$typ ops=(")
        addOp(sb, op1)
        addOp(sb, op2)
        addOp(sb, op3)
        sb.append(')')
        return sb.toString()
    }

    private fun addOp(sb: StringBuilder, e: BExpression?) {
        if (e != null) {
            sb.append('[').append(e.toString()).append(']')
        }
    }

    companion object {
        private const val OR_EXP = 10
        private const val AND_EXP = 11
        private const val NOT_EXP = 12
        private const val ADD_EXP = 20
        private const val MULTIPLY_EXP = 21
        private const val DIVIDE_EXP = 22
        private const val MAX_EXP = 23
        private const val EQUAL_EXP = 24
        private const val GREATER_EXP = 25
        private const val MIN_EXP = 26
        private const val SUB_EXP = 27
        private const val LESSER_EXP = 28
        private const val XOR_EXP = 29
        private const val SWITCH_EXP = 30
        private const val ASSIGN_EXP = 31
        private const val LOOKUP_EXP = 32
        private const val NUMBER_EXP = 33
        private const val VARIABLE_EXP = 34
        private const val FOREIGN_VARIABLE_EXP = 35
        private const val VARIABLE_GET_EXP = 36

        // Parse the expression and all subexpression
        fun parse(ctx: BExpressionContext, level: Int): BExpression? = parse(ctx, level, null)

        private fun parse(ctx: BExpressionContext, level: Int, optionalToken: String?): BExpression? {
            var e = parseRaw(ctx, level, optionalToken) ?: return null

            if (ASSIGN_EXP == e.typ) {
                // manage assined an injected values
                val lae = ctx.lastAssignedExpression!!
                val assignedBefore = lae[e.variableIdx]
                if (assignedBefore != null && assignedBefore.doNotChange) {
                    e.op1 = assignedBefore // was injected as key-value
                    assignedBefore.doNotChange = false // protect just once, can be changed in second assignement
                }
                lae[e.variableIdx] = e.op1
            } else if (!ctx.skipConstantExpressionOptimizations) {
                // try to simplify the expression
                if (VARIABLE_EXP == e.typ) {
                    val ae = ctx.lastAssignedExpression!![e.variableIdx]
                    if (ae != null && ae.typ == NUMBER_EXP) {
                        e = ae
                    }
                } else {
                    e = e.tryCollapse()
                    e = e.tryEvaluateConstant()
                }
            }
            if (level == 0) {
                // mark the used lookups after the expression is collapsed to not mark lookups as used that appear
                // in the profile but are de-activated by constant expressions
                val nodeCount = e.markLookupIdxUsed(ctx)
                ctx.expressionNodeCount += nodeCount
            }
            return e
        }

        private fun parseRaw(ctx: BExpressionContext, level: Int, optionalToken: String?): BExpression? {
            var brackets = false
            var operator = ctx.parseToken()
            if (optionalToken != null && optionalToken == operator) {
                operator = ctx.parseToken()
            }
            if ("(" == operator) {
                brackets = true
                operator = ctx.parseToken()
            }

            if (operator == null) {
                if (level == 0) return null else throw IllegalArgumentException("unexpected end of file")
            }

            if (level == 0) {
                if ("assign" != operator) {
                    throw IllegalArgumentException("operator $operator is invalid on toplevel (only 'assign' allowed)")
                }
            }

            val exp = BExpression()
            var nops = 3
            var ifThenElse = false

            if ("switch" == operator) {
                exp.typ = SWITCH_EXP
            } else if ("if" == operator) {
                exp.typ = SWITCH_EXP
                ifThenElse = true
            } else {
                nops = 2 // check binary expressions
                when (operator) {
                    "or" -> exp.typ = OR_EXP
                    "and" -> exp.typ = AND_EXP
                    "multiply" -> exp.typ = MULTIPLY_EXP
                    "divide" -> exp.typ = DIVIDE_EXP
                    "add" -> exp.typ = ADD_EXP
                    "max" -> exp.typ = MAX_EXP
                    "min" -> exp.typ = MIN_EXP
                    "equal" -> exp.typ = EQUAL_EXP
                    "greater" -> exp.typ = GREATER_EXP
                    "sub" -> exp.typ = SUB_EXP
                    "lesser" -> exp.typ = LESSER_EXP
                    "xor" -> exp.typ = XOR_EXP
                    else -> {
                        nops = 1 // check unary expressions
                        if ("assign" == operator) {
                            parseAssignTarget(ctx, level, exp)
                        } else if ("not" == operator) {
                            exp.typ = NOT_EXP
                        } else {
                            nops = 0 // check elemantary expressions
                            parseElementary(ctx, operator, exp)
                        }
                    }
                }
            }

            // parse operands
            if (nops > 0) {
                exp.op1 = parse(ctx, level + 1, if (exp.typ == ASSIGN_EXP) "=" else null)
            }
            if (nops > 1) {
                if (ifThenElse) checkExpectedToken(ctx, "then")
                exp.op2 = parse(ctx, level + 1, null)
            }
            if (nops > 2) {
                if (ifThenElse) checkExpectedToken(ctx, "else")
                exp.op3 = parse(ctx, level + 1, null)
            }
            if (brackets) {
                checkExpectedToken(ctx, ")")
            }
            return exp
        }

        // BExpression.java:155-168, split out of parseRaw
        private fun parseAssignTarget(ctx: BExpressionContext, level: Int, exp: BExpression) {
            if (level > 0) throw IllegalArgumentException("assign operator within expression")
            exp.typ = ASSIGN_EXP
            val variable = ctx.parseToken() ?: throw IllegalArgumentException("unexpected end of file")
            if (variable.indexOf('=') >= 0) {
                throw IllegalArgumentException("variable name cannot contain '=': $variable")
            }
            if (variable.indexOf(':') >= 0) {
                throw IllegalArgumentException("cannot assign context-prefixed variable: $variable")
            }
            exp.variableIdx = ctx.getVariableIdx(variable, true)
            if (exp.variableIdx < ctx.minWriteIdx) {
                throw IllegalArgumentException("cannot assign to readonly variable $variable")
            }
        }

        // BExpression.java:171-238, split out of parseRaw
        private fun parseElementary(ctx: BExpressionContext, operator: String, exp: BExpression) {
            var idx = operator.indexOf('=')
            if (idx >= 0) {
                exp.typ = LOOKUP_EXP
                val name = operator.substring(0, idx)
                val values = operator.substring(idx + 1)

                exp.lookupNameIdx = ctx.getLookupNameIdx(name)
                if (exp.lookupNameIdx < 0) {
                    throw IllegalArgumentException("unknown lookup name: $name")
                }
                val tk = JavaStringTokenizer(values, '|')
                val nt = tk.countTokens()
                val nt2 = if (nt == 0) 1 else nt
                val arr = IntArray(nt2)
                exp.lookupValueIdxArray = arr
                for (ti in 0 until nt2) {
                    val value = if (ti < nt) tk.nextToken() else ""
                    arr[ti] = ctx.getLookupValueIdx(exp.lookupNameIdx, value)
                    if (arr[ti] < 0) {
                        throw IllegalArgumentException("unknown lookup value: $value")
                    }
                }
            } else if (operator.indexOf(':').also { idx = it } >= 0) {
                // use of variable values, e.g. "lesser v:maxheight my_height"
                if (operator.startsWith("v:")) {
                    val name = operator.substring(2)
                    exp.typ = VARIABLE_GET_EXP
                    exp.lookupNameIdx = ctx.getLookupNameIdx(name)
                } else {
                    val context = operator.substring(0, idx)
                    val varname = operator.substring(idx + 1)
                    exp.typ = FOREIGN_VARIABLE_EXP
                    exp.variableIdx = ctx.getForeignVariableIdx(context, varname)
                }
            } else if (ctx.getVariableIdx(operator, false).also { idx = it } >= 0) {
                exp.typ = VARIABLE_EXP
                exp.variableIdx = idx
            } else if ("true" == operator) {
                exp.numberValue = 1f
                exp.typ = NUMBER_EXP
            } else if ("false" == operator) {
                exp.numberValue = 0f
                exp.typ = NUMBER_EXP
            } else {
                try {
                    exp.numberValue = operator.toFloat() // Java's Float.parseFloat on the JVM
                    exp.typ = NUMBER_EXP
                } catch (nfe: NumberFormatException) {
                    throw IllegalArgumentException("unknown expression: $operator")
                }
            }
        }

        private fun checkExpectedToken(ctx: BExpressionContext, expected: String) {
            val token = ctx.parseToken()
            if (expected != token) {
                throw IllegalArgumentException("unexpected token: $token, expected: $expected")
            }
        }

        fun createAssignExpressionFromKeyValue(ctx: BExpressionContext, key: String, value: String): BExpression {
            val e = BExpression()
            e.typ = ASSIGN_EXP
            e.variableIdx = ctx.getVariableIdx(key, true)
            val op = BExpression()
            e.op1 = op
            op.typ = NUMBER_EXP
            op.numberValue = value.toFloat()
            op.doNotChange = true
            ctx.lastAssignedExpression!![e.variableIdx] = op
            return e
        }
    }
}
