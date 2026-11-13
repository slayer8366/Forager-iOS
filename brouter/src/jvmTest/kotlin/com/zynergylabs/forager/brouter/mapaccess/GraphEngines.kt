package com.zynergylabs.forager.brouter.mapaccess

import com.zynergylabs.forager.brouter.codec.DataBuffers
import com.zynergylabs.forager.brouter.codec.MicroCache
import com.zynergylabs.forager.brouter.expressions.ExpressionsEngines
import com.zynergylabs.forager.brouter.oracle.OracleCalls
import java.io.File

/** Both engines' graph decoders, driven through `OsmFile.createMicroCache` as `NodesCache.getSegmentFor` calls it. */
internal object GraphEngines {
    /** Forager's profile; its way context is the validator `NodesCache` passes (`NodesCache.java:189`). */
    val profile: File get() = File(OracleCalls.oracleDir, "profiles2/hiking-mountain.brf")

    /** A tile file's south-west degree square, as (lon + 180, lat + 90), from its name. */
    fun corner(file: File): Pair<Int, Int> {
        val name = file.name
        val lon = name.substring(1, name.indexOf('_')).toInt().let { if (name[0] == 'W') -it else it }
        val latPart = name.substring(name.indexOf('_') + 1, name.indexOf('.'))
        val lat = latPart.substring(1).toInt().let { if (latPart[0] == 'S') -it else it }
        return (lon + 180) to (lat + 90)
    }

    /** One non-empty micro-cache: where it is, and its node ids as the oracle's own `MicroCache2` reads them. */
    class Cache(val lonDegree: Int, val latDegree: Int, val subIdx: Int, val lonIdx: Int, val latIdx: Int, val bytes: Int, val ids: LongArray) {
        override fun toString() = "degree ($lonDegree, $latDegree) sub $subIdx (lonIdx $lonIdx, latIdx $latIdx, $bytes bytes, ${ids.size} nodes)"
    }

    /** Both engines' views of one degree square. */
    class Square(val oracle: btools.mapaccess.OsmFile, val port: OsmFile, val divisor: Int)

    /** Both engines' `PhysicalFile` for [file], open for the whole of [block]. */
    fun <T> withTile(file: File, block: (btools.mapaccess.PhysicalFile, PhysicalFile) -> T): T {
        val opf = MapaccessEngines.oraclePhysical(file)
        val ppf = MapaccessEngines.portPhysical(file)
        try {
            return block(opf, ppf)
        } finally {
            opf.close()
            ppf.close()
        }
    }

    /** Every non-empty micro-cache of [file], degree square by square in tile-index order, then by sub-index. */
    fun forEachCache(file: File, block: (Square, Cache) -> Unit) = withTile(file) { opf, ppf -> forEachCacheIn(file, opf, ppf, false, block) }

    /** As [forEachCache], over files already open; with [firstSquareOnly], only the first square that has data. */
    fun forEachCacheIn(file: File, opf: btools.mapaccess.PhysicalFile, ppf: PhysicalFile, firstSquareOnly: Boolean, block: (Square, Cache) -> Unit) {
        val (lon0, lat0) = corner(file)
        for (tileIndex in 0 until 25) {
            val lonDegree = lon0 + tileIndex / 5
            val latDegree = lat0 + tileIndex % 5
            val of = btools.mapaccess.OsmFile(opf, lonDegree, latDegree, btools.codec.DataBuffers())
            val pf = OsmFile(ppf, lonDegree, latDegree, DataBuffers())
            if (!of.hasData()) continue
            val divisor = opf.divisor
            val square = Square(of, pf, divisor)
            var buf = ByteArray(1 shl 16)
            for (subIdx in 0 until divisor * divisor) {
                var size = of.getDataInputForSubIdx(subIdx, buf)
                if (size == 0) continue
                if (size > buf.size) {
                    buf = ByteArray(size)
                    size = of.getDataInputForSubIdx(subIdx, buf)
                }
                val lonIdx = divisor * lonDegree + subIdx % divisor
                val latIdx = divisor * latDegree + subIdx / divisor
                val mc = btools.codec.MicroCache2(btools.codec.StatCoderContext(buf), btools.codec.DataBuffers(), lonIdx, latIdx, divisor, null, null)
                val ids = LongArray(mc.getSize()) { mc.getIdForIndex(it) }
                block(square, Cache(lonDegree, latDegree, subIdx, lonIdx, latIdx, size, ids))
            }
            if (firstSquareOnly) return
        }
    }

    /** The oracle's decode of [c] into a fresh map in [mode]; with [seeded], every node of the cache is first in the map, hollow. */
    class OracleDecoder {
        private val way = ExpressionsEngines.oracle(profile).way
        private val buffers = btools.codec.DataBuffers()

        /** Caches whose returned segment was the oracle's `MicroCache.emptyNonVirgin`, size 0 (RECORD -32's data). */
        var emptyNonVirginReturns = 0

        fun decode(square: Square, c: Cache, seeded: Boolean, mode: Int): Pair<btools.mapaccess.OsmNodesMap, List<btools.mapaccess.OsmNode>> {
            val map = btools.mapaccess.OsmNodesMap()
            map.cleanupMode = mode
            val seeds = if (!seeded) emptyList() else c.ids.map { id ->
                btools.mapaccess.OsmNode((id shr 32).toInt(), id.toInt()).also {
                    it.setHollow()
                    map.put(it)
                }
            }
            val segment = square.oracle.createMicroCache(c.lonIdx, c.latIdx, buffers, way, null, true, map)
            check(segment === btools.codec.MicroCache.emptyNonVirgin && segment.getSize() == 0) { "oracle segment for $c is not emptyNonVirgin" }
            emptyNonVirginReturns++
            return map to seeds
        }
    }

    /** The port's decode, as [OracleDecoder.decode]. */
    class PortDecoder {
        private val way = ExpressionsEngines.port(profile).way
        private val buffers = DataBuffers()

        fun decode(square: Square, c: Cache, seeded: Boolean, mode: Int): Pair<OsmNodesMap, List<OsmNode>> {
            val map = OsmNodesMap()
            map.cleanupMode = mode
            val seeds = if (!seeded) emptyList() else c.ids.map { id ->
                OsmNode((id shr 32).toInt(), id.toInt()).also {
                    it.setHollow()
                    map.put(it)
                }
            }
            val segment = square.port.createMicroCache(c.lonIdx, c.latIdx, buffers, way, null, true, map)
            check(segment === MicroCache.emptyNonVirgin && segment.getSize() == 0) { "port segment for $c is not emptyNonVirgin" }
            return map to seeds
        }
    }
}
