package com.zynergylabs.forager.brouter.mapaccess

import com.zynergylabs.forager.brouter.codec.DataBuffers
import com.zynergylabs.forager.brouter.codec.MicroCache2
import java.io.File
import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Step 5d, item 3 (RECORD -70): every micro-cache of each captured tile decoded through
 * `OsmFile.createMicroCache(..., wayValidator = null, waypointMatcher = null, reallyDecode = true, hollowNodes = null)`,
 * as `BRouterRouteWays.kt:84` at Forager `a0535ece` calls it, by both engines. Each returns a `MicroCache2`.
 *
 * The cache is then read through its own public reader, node by node in index order, as `OsmNode.parseNodeBody2`
 * (`OsmNode.java:117-152`) reads it, but without weaving: every node's id (`getIdForIndex`, `getAndClear`, so
 * `expandId` and `shrinkId`), turn restrictions, elevation, node description, and every link's offset, direction,
 * way description and geometry bytes. One text per cache, compared line by line; a failure names the cache's index
 * and the field, never a value (E1). Only counts and one digest per tile are printed.
 */
class MicroCache2DecodeTest {
    @Test
    fun w75n40() = decodeTile(File(com.zynergylabs.forager.brouter.tiles.TestTiles.segments, "W75_N40.rd5"))

    @Test
    fun w75n35() = decodeTile(File(com.zynergylabs.forager.brouter.tiles.TestTiles.segments, "W75_N35.rd5"))

    @Test
    fun w70n40() = decodeTile(File(com.zynergylabs.forager.brouter.tiles.TestTiles.segments, "W70_N40.rd5"))

    @Test
    fun e170s50() = decodeTile(File(com.zynergylabs.forager.brouter.tiles.TestTiles.segments, "E170_S50.rd5"))

    @Test
    fun f0Fixture() = decodeTile(File(com.zynergylabs.forager.brouter.tiles.TestTiles.f0, "W75_N40.rd5"))

    /** What one cache's text holds, summed over a tile. */
    private class Counts {
        var nodes = 0L
        var cleared = 0L
        var restrictions = 0L
        var nodeDescriptions = 0L
        var links = 0L
        var reverse = 0L
        var wayDescriptions = 0L
        var geometryBytes = 0L
        var dataBytes = 0L

        override fun toString() =
            "nodes $nodes (cleared $cleared), restrictions $restrictions, node descriptions $nodeDescriptions, links $links " +
                "(reverse $reverse), way descriptions $wayDescriptions, geometry bytes $geometryBytes, data bytes $dataBytes"
    }

    /** The reader calls both caches answer, so one walk serves both engines. */
    private interface Reader {
        val size: Int
        val dataSize: Int
        fun idForIndex(i: Int): Long
        fun getAndClear(id: Long): Boolean
        fun readBoolean(): Boolean
        fun readShort(): Short
        fun readInt(): Int
        fun unsigned(): Int
        fun signed(): Int
        fun endPointer(): Int
        fun fully(n: Int): ByteArray
        fun until(end: Int): ByteArray?
        fun more(): Boolean
    }

    private class OracleReader(val mc: btools.codec.MicroCache2) : Reader {
        override val size get() = mc.getSize()
        override val dataSize get() = mc.getDataSize()
        override fun idForIndex(i: Int) = mc.getIdForIndex(i)
        override fun getAndClear(id: Long) = mc.getAndClear(id)
        override fun readBoolean() = mc.readBoolean()
        override fun readShort() = mc.readShort()
        override fun readInt() = mc.readInt()
        override fun unsigned() = mc.readVarLengthUnsigned()
        override fun signed() = mc.readVarLengthSigned()
        override fun endPointer() = mc.getEndPointer()
        override fun fully(n: Int) = ByteArray(n).also { mc.readFully(it) }
        override fun until(end: Int): ByteArray? = mc.readDataUntil(end)
        override fun more() = mc.hasMoreData()
    }

    private class PortReader(val mc: MicroCache2) : Reader {
        override val size get() = mc.getSize()
        override val dataSize get() = mc.getDataSize()
        override fun idForIndex(i: Int) = mc.getIdForIndex(i)
        override fun getAndClear(id: Long) = mc.getAndClear(id)
        override fun readBoolean() = mc.readBoolean()
        override fun readShort() = mc.readShort()
        override fun readInt() = mc.readInt()
        override fun unsigned() = mc.readVarLengthUnsigned()
        override fun signed() = mc.readVarLengthSigned()
        override fun endPointer() = mc.getEndPointer()
        override fun fully(n: Int) = ByteArray(n).also { mc.readFully(it) }
        override fun until(end: Int): ByteArray? = mc.readDataUntil(end)
        override fun more() = mc.hasMoreData()
    }

    private fun hex(b: ByteArray?) = b?.joinToString("") { "%02x".format(it) } ?: "null"

    /** One cache as lines `field<TAB>value`, the field first so a failure can name it alone. */
    private fun text(r: Reader, counts: Counts?): String = buildString {
        fun line(field: String, value: Any?) = append(field).append('\t').append(value).append('\n')
        line("size", r.size)
        line("data size", r.dataSize)
        counts?.let { it.dataBytes += r.dataSize }
        for (i in 0 until r.size) {
            val id = r.idForIndex(i)
            line("node id", id)
            counts?.let { it.nodes++ }
            val cleared = r.getAndClear(id)
            line("cleared", cleared)
            if (!cleared) continue
            counts?.let { it.cleared++ }
            while (r.readBoolean()) {
                line("restriction", "${r.readShort()} ${r.readBoolean()} ${r.readInt()} ${r.readInt()} ${r.readInt()} ${r.readInt()}")
                counts?.let { it.restrictions++ }
            }
            line("node elevation", r.readShort())
            val nodeDescSize = r.unsigned()
            line("node description", if (nodeDescSize == 0) "null" else hex(r.fully(nodeDescSize)))
            if (nodeDescSize != 0) counts?.let { it.nodeDescriptions++ }
            while (r.more()) {
                val end = r.endPointer()
                line("link dlon", r.signed())
                line("link dlat", r.signed())
                val sizecode = r.unsigned()
                line("link reverse", (sizecode and 1) != 0)
                val descSize = sizecode shr 1
                line("way description", if (descSize > 0) hex(r.fully(descSize)) else "null")
                val geometry = r.until(end)
                line("geometry", hex(geometry))
                counts?.let {
                    it.links++
                    if ((sizecode and 1) != 0) it.reverse++
                    if (descSize > 0) it.wayDescriptions++
                    it.geometryBytes += geometry?.size ?: 0
                }
            }
        }
    }

    private fun decodeTile(file: File) {
        val digest = MessageDigest.getInstance("SHA-256")
        val counts = Counts()
        var caches = 0
        val oracleBuffers = btools.codec.DataBuffers()
        val portBuffers = DataBuffers()

        GraphEngines.forEachCache(file) { square, c ->
            val om = square.oracle.createMicroCache(c.lonIdx, c.latIdx, oracleBuffers, null, null, true, null)
            val pm = square.port.createMicroCache(c.lonIdx, c.latIdx, portBuffers, null, null, true, null)
            assertTrue(om is btools.codec.MicroCache2, "cache $caches: the oracle did not return a MicroCache2")
            assertTrue(pm is MicroCache2, "cache $caches: the port did not return a MicroCache2")
            val o = text(OracleReader(om), counts)
            val p = text(PortReader(pm), null)
            if (o != p) {
                val lo = o.split('\n')
                val lp = p.split('\n')
                val at = (0 until maxOf(lo.size, lp.size)).first { lo.getOrNull(it) != lp.getOrNull(it) }
                val field = (lo.getOrNull(at) ?: lp.getOrNull(at) ?: "").substringBefore('\t')
                fail("${file.parentFile.name}/${file.name} cache $caches: line ${at + 1} of ${lo.size}/${lp.size} differs, field [$field]")
            }
            assertEquals(c.ids.size, om.getSize(), "cache $caches: node count is not the reader's")
            digest.update(o.toByteArray())
            caches++
        }

        println(
            "5d MicroCache2 decode ${file.parentFile.name}/${file.name}: $caches caches; $counts; " +
                "digest ${digest.digest().joinToString("") { "%02x".format(it) }}",
        )
        assertTrue(caches > 0, "${file.name}: no cache decoded")
        assertTrue(counts.links > 0, "${file.name}: no link read")
    }
}
