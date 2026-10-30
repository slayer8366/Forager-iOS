package com.zynergylabs.forager.brouter.expressions

import okio.FileSystem
import okio.Path

/** Port of BRouter 1.7.10's `btools.expressions.BExpressionMetaData`. Step 4 stub: not yet ported. */
internal class BExpressionMetaData {
    var lookupVersion: Short = -1
    var lookupMinorVersion: Short = -1
    var minAppVersion: Short = -1

    fun registerListener(context: String, ctx: BExpressionContext) { TODO("step 4: registerListener") }

    fun readMetaData(fileSystem: FileSystem, lookupsFile: Path) { TODO("step 4: readMetaData") }
}
