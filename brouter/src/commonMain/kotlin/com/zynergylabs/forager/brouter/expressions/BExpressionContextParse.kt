package com.zynergylabs.forager.brouter.expressions

import com.zynergylabs.forager.brouter.util.javaIsWhitespace
import com.zynergylabs.forager.brouter.util.javaSplit
import com.zynergylabs.forager.brouter.util.javaTrim
import okio.FileSystem
import okio.Path

// The profile parser of BRouter 1.7.10's `BExpressionContext` (`parseFile`, `_parseFile`, `parseToken`,
// `_parseToken`), split from `BExpressionContext.kt` to keep files small. Line numbers cite `BExpressionContext.java`.
// Java reads through a `java.io.File`; the port takes an okio [FileSystem] and [Path], as a parameter (the seam).

private const val CONTEXT_TAG = "---context:"
private const val MODEL_TAG = "---model:"

/** `BExpressionContext.java:813-853`. */
internal fun BExpressionContext.parseFile(
    fileSystem: FileSystem,
    file: Path,
    readOnlyContext: String?,
    keyValues: Map<String, String>?,
) {
    if (!fileSystem.exists(file)) {
        throw IllegalArgumentException("profile ${file.name} does not exist")
    }
    try {
        if (readOnlyContext != null) {
            linenr = 1
            val realContext = context
            context = readOnlyContext
            expressionList = parseExpressions(fileSystem, file, keyValues)
            variableData = FloatArray(variableNumbers.size)
            evaluate(lookupData) // lookupData is dummy here - evaluate just to create the variables
            context = realContext
        }
        linenr = 1
        minWriteIdx = variableData?.size ?: 0
        expressionList = parseExpressions(fileSystem, file, null)
        lastAssignedExpression = null

        // determine the build-in variable indices
        val varNames = getBuildInVariableNames()
        nBuildInVars = varNames.size
        val idxs = IntArray(nBuildInVars)
        buildInVariableIdx = idxs
        for (vi in varNames.indices) {
            idxs[vi] = getVariableIdx(varNames[vi], false)
        }

        val readOnlyData = variableData
        val data = FloatArray(variableNumbers.size)
        variableData = data
        for (i in 0 until minWriteIdx) {
            data[i] = readOnlyData!![i]
        }
    } catch (e: IllegalArgumentException) {
        throw IllegalArgumentException("ParseException ${file.name} at line $linenr: ${e.message}")
    } catch (e: Exception) {
        throw RuntimeException(e)
    }
    if (expressionList!!.isEmpty()) {
        throw IllegalArgumentException("${file.name} does not contain expressions for context $context (old version?)")
    }
}

/** `BExpressionContext.java:855-877` (`_parseFile`). Java's `FileReader` decodes as UTF-8 on JDK 18 and Android. */
private fun BExpressionContext.parseExpressions(
    fileSystem: FileSystem,
    file: Path,
    keyValues: Map<String, String>?,
): List<BExpression> {
    parseText = fileSystem.read(file) { readUtf8() }
    parsePos = 0
    readerDone = false
    val result = ArrayList<BExpression>()

    // if injected keyValues are present, create assign expressions for them
    if (keyValues != null) {
        for (key in keyValues.keys) {
            val value = keyValues[key]!!
            result.add(BExpression.createAssignExpressionFromKeyValue(this, key, value))
        }
    }

    while (true) {
        val exp = BExpression.parse(this, 0) ?: break
        result.add(exp)
    }
    parseText = null
    return result
}

/** `BExpressionContext.java:971-984`. Ends: each pass reads one raw token, and the raw tokens end in null. */
internal fun BExpressionContext.parseToken(): String? {
    while (true) {
        val token = parseRawToken() ?: return null
        if (token.startsWith(CONTEXT_TAG)) {
            inOurContext = token.substring(CONTEXT_TAG.length) == context
        } else if (token.startsWith(MODEL_TAG)) {
            modelClass = token.substring(MODEL_TAG.length).javaTrim()
        } else if (inOurContext) {
            return token
        }
    }
}

/**
 * `BExpressionContext.java:987-1037` (`_parseToken`). Ends: each pass either returns or consumes one character, and
 * the text is finite.
 */
private fun BExpressionContext.parseRawToken(): String? {
    val sb = StringBuilder(32)
    val sbcom = StringBuilder(32)
    var inComment = false
    val text = parseText!!
    while (true) {
        val ic = if (readerDone || parsePos >= text.length) -1 else text[parsePos++].code
        if (ic < 0) {
            if (sb.isEmpty()) return null
            readerDone = true
            return sb.toString()
        }
        val c = ic.toChar()
        if (c == '\n') linenr++

        if (inComment) {
            sbcom.append(c)
            if (c == '\r' || c == '\n') inComment = false
            if (!inComment) {
                readNoStartWays(sbcom.toString())
                sbcom.clear()
            }
            continue
        }
        if (javaIsWhitespace(c)) {
            if (sb.isNotEmpty()) return sb.toString() else continue
        }
        if (c == '#' && sb.isEmpty()) inComment = true else sb.append(c)
    }
}

/** `BExpressionContext.java:1002-1025`: a `noStartWay` comment, once `check_start_way` is a variable. */
private fun BExpressionContext.readNoStartWays(comment: String) {
    val num = variableNumbers["check_start_way"]
    if (num != null && noStartWays.isEmpty() && comment.contains("noStartWay")) {
        var v = comment.javaTrim()
        val savar = v.javaSplit("\\|")
        if (savar.size == 4) {
            v = savar[3].substring(savar[3].indexOf("=") + 1).javaTrim()
            val sa = v.javaSplit(";")
            for (s in sa) {
                val sa2 = s.javaSplit(",")
                val name = sa2[0]
                val value = sa2[1]
                val nidx = getLookupNameIdx(name)
                if (nidx == -1) break
                val vidx = getLookupValueIdx(nidx, value)
                val tmp = IntArray(noStartWays.size + 2)
                if (noStartWays.isNotEmpty()) noStartWays.copyInto(tmp, 0, 0, noStartWays.size)
                noStartWays = tmp
                noStartWays[noStartWays.size - 2] = nidx
                noStartWays[noStartWays.size - 1] = vidx
            }
        }
    }
}
