package com.zynergylabs.forager.brouter.expressions

// Port of `BExpressionContext`'s lookups half (`parseMetaLine`, `finishMetaParsing`, `addLookupValue`;
// `BExpressionContext.java:289-319`, `:540-726`). Step 4 stub.

internal fun BExpressionContext.parseMetaLine(line: String) { TODO("step 4: parseMetaLine") }

internal fun BExpressionContext.finishMetaParsing() { TODO("step 4: finishMetaParsing") }

/**
 * `BExpressionContext.java:540`. When [lookupData2] is not null and the name has a `*` value, Java converts the value
 * (`:576-693`); every caller in BRouter passes null, so the port names that block "unsupported" (RECORD -40).
 */
internal fun BExpressionContext.addLookupValue(name: String, value: String, lookupData2: IntArray?): BExpressionLookupValue? =
    TODO("step 4: addLookupValue")
