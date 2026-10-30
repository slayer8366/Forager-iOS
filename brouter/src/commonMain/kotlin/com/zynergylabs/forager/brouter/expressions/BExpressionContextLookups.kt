package com.zynergylabs.forager.brouter.expressions

import com.zynergylabs.forager.brouter.util.JavaStringTokenizer

// The lookup table of BRouter 1.7.10's `BExpressionContext` (`parseMetaLine`, `finishMetaParsing`, `addLookupValue`),
// split from `BExpressionContext.kt` to keep files small. Line numbers cite `BExpressionContext.java`.

/** `BExpressionContext.java:288-311`: one line of `lookups.dat` for this context. */
internal fun BExpressionContext.parseMetaLine(line: String) {
    parsedLines++
    val tk = JavaStringTokenizer(line, ' ')
    var name = tk.nextToken()
    val value = tk.nextToken()
    val idx = name.indexOf(';')
    if (idx >= 0) name = name.substring(0, idx)

    if (!fixTagsWritten) {
        fixTagsWritten = true
        if ("way" == context) {
            addLookupValue("reversedirection", "yes", null)
        } else if ("node" == context) {
            addLookupValue("nodeaccessgranted", "yes", null)
        }
    }
    if ("reversedirection" == name) return // this is hardcoded
    if ("nodeaccessgranted" == name) return // this is hardcoded
    val newValue = addLookupValue(name, value, null)

    // add aliases
    while (newValue != null && tk.hasMoreTokens()) newValue.addAlias(tk.nextToken())
}

/** `BExpressionContext.java:313-322`. */
internal fun BExpressionContext.finishMetaParsing() {
    if (parsedLines == 0 && "global" != context) {
        throw IllegalArgumentException("lookup table does not contain data for context $context (old version?)")
    }

    // post-process metadata:
    lookupDataFrozen = true

    lookupIdxUsed = BooleanArray(lookupValues.size)
}

/**
 * `BExpressionContext.java:542-721`: adds a lookup value for [name] to [lookupData2], or, when that is null, to the
 * context's own array, creating unknown names and values.
 *
 * With a non-null [lookupData2], an unknown value of a name whose values include `*` enters Java's "asterisk block"
 * (`:576-693`: unit conversion through `String.format`). That block is **unsupported** here (RECORD -39) and throws
 * [UnsupportedOperationException] naming it, rather than giving a value. No caller in BRouter's router passes a
 * non-null array (the step 4 report), so Forager's calls never reach it.
 */
internal fun BExpressionContext.addLookupValue(name: String, value: String, lookupData2: IntArray?): BExpressionLookupValue? {
    var newValue: BExpressionLookupValue? = null
    var num = lookupNumbers[name]
    if (num == null) {
        if (lookupData2 != null) {
            // do not create unknown name for external data array
            return newValue
        }

        // unknown name, create
        num = lookupValues.size
        lookupNumbers[name] = num
        lookupNames.add(name)
        lookupValues.add(arrayOf(BExpressionLookupValue(""), BExpressionLookupValue("unknown")))
        lookupHistograms.add(IntArray(2))
        lookupData = lookupData.copyOf(lookupData.size + 1)
    }

    // look for that value
    var values = lookupValues[num]
    var histo = lookupHistograms[num]
    var i = 0
    var bFoundAsterix = false
    while (i < values.size) {
        val v = values[i]
        if (v.equals("*")) bFoundAsterix = true // Java's equals(String): the primary value only
        if (v.matches(value)) break
        i++
    }
    if (i == values.size) {
        if (lookupData2 != null) {
            // do not create unknown value for external data array,
            // record as 'unknown' instead
            lookupData2[num] = 1 // 1 == unknown
            if (bFoundAsterix) {
                throw UnsupportedOperationException(
                    "unsupported: addLookupValue's asterisk block (BExpressionContext.java:576-693, unit conversion by " +
                        "String.format) for $name=$value; RECORD -39, -40",
                )
            }
            return newValue
        }

        if (i == 500) {
            return newValue
        }
        // unknown value, create
        histo = histo.copyOf(values.size + 1)
        newValue = BExpressionLookupValue(value)
        values = values + newValue
        lookupHistograms[num] = histo
        lookupValues[num] = values
    }

    histo[i]++

    // finally remember the actual data
    if (lookupData2 != null) lookupData2[num] = i else lookupData[num] = i
    return newValue
}
