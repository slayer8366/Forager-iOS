package com.zynergylabs.forager.brouter.expressions

import okio.FileSystem
import okio.Path

// Port of `BExpressionContext`'s profile reader (`parseFile`, `_parseFile`, `_parseToken`; `BExpressionContext.java:809-
// 1039`). Java reads through a `java.io.File`; the port takes an okio [FileSystem] and [Path] (the seam). Step 4 stub.

internal fun BExpressionContext.parseFile(
    fileSystem: FileSystem,
    file: Path,
    readOnlyContext: String?,
    keyValues: Map<String, String>?,
) { TODO("step 4: parseFile") }
