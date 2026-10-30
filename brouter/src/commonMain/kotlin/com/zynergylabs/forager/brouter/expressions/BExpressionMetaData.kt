package com.zynergylabs.forager.brouter.expressions

import com.zynergylabs.forager.brouter.util.javaReadLines
import com.zynergylabs.forager.brouter.util.javaTrim
import okio.FileSystem
import okio.Path

private const val CONTEXT_TAG = "---context:"
private const val VERSION_TAG = "---lookupversion:"
private const val MINOR_VERSION_TAG = "---minorversion:"
private const val VARLENGTH_TAG = "---readvarlength"
private const val MIN_APP_VERSION_TAG = "---minappversion:"

/**
 * Port of BRouter 1.7.10's `btools.expressions.BExpressionMetaData`: reads `lookups.dat` and hands each context's lines
 * to the context registered for it. Java reads through a `java.io.File` and a `FileReader`; the port takes an okio
 * [FileSystem] and [Path] (the seam) and splits the text as `BufferedReader.readLine` does ([javaReadLines]). Java's
 * `FileReader` decodes as UTF-8 on JDK 18 and later and on Android; the port reads UTF-8.
 */
internal class BExpressionMetaData {
    var lookupVersion: Short = -1
    var lookupMinorVersion: Short = -1
    var minAppVersion: Short = -1

    // Java's HashMap; on the JVM Kotlin's HashMap is java.util.HashMap, so finishMetaParsing runs in Java's order.
    private val listeners = HashMap<String, BExpressionContext>()

    fun registerListener(context: String, ctx: BExpressionContext) {
        listeners[context] = ctx
    }

    /** `BExpressionMetaData.java:33-73`. Every exception, the contexts' own included, leaves as `RuntimeException(e)`. */
    fun readMetaData(fileSystem: FileSystem, lookupsFile: Path) {
        try {
            val text = fileSystem.read(lookupsFile) { readUtf8() }
            var ctx: BExpressionContext? = null
            for (raw in javaReadLines(text)) {
                val line = raw.javaTrim()
                if (line.isEmpty() || line.startsWith("#")) continue
                if (line.startsWith(CONTEXT_TAG)) {
                    ctx = listeners[line.substring(CONTEXT_TAG.length)]
                    continue
                }
                if (line.startsWith(VERSION_TAG)) {
                    lookupVersion = line.substring(VERSION_TAG.length).toShort() // Short.parseShort on the JVM
                    continue
                }
                if (line.startsWith(MINOR_VERSION_TAG)) {
                    lookupMinorVersion = line.substring(MINOR_VERSION_TAG.length).toShort()
                    continue
                }
                if (line.startsWith(MIN_APP_VERSION_TAG)) {
                    minAppVersion = line.substring(MIN_APP_VERSION_TAG.length).toShort()
                    continue
                }
                if (line.startsWith(VARLENGTH_TAG)) { // tag removed...
                    continue
                }
                ctx?.parseMetaLine(line)
            }
            for (c in listeners.values) {
                c.finishMetaParsing()
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}
