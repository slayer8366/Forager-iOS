package com.zynergylabs.forager.brouter.oracle.e3

import btools.codec.DataBuffers
import btools.mapaccess.OsmFile
import btools.mapaccess.PhysicalFile
import java.io.Closeable
import java.io.File
import java.io.RandomAccessFile

/**
 * A tile's nodes, read with the oracle's own reader (Phase 1 report, E4: "start and end are drawn on the tile's own
 * nodes, read with the oracle's own reader"): `PhysicalFile`, then `OsmFile.createMicroCache(…, reallyDecode = true, …)`
 * per cell, as step 1's `TileCheck.java` decoded them. Cells are addressed with absolute indices (degree × divisor), so a
 * node id is `ilon << 32 | ilat` in the engine's own micro-degrees (`MicroCache2.expandId`).
 */
class TileNodes(val file: File) : Closeable {
    /** The tile's south-west corner in whole degrees of the engine's ilon/ilat frame (lon + 180, lat + 90). */
    val lonDeg0: Int
    val latDeg0: Int
    private val buffers = DataBuffers()
    private val pf = PhysicalFile(file, buffers, -1, -1)
    val divisor: Int = pf.divisor
    private val files = HashMap<Int, OsmFile?>()

    init {
        val m = Regex("([EW])(\\d+)_([NS])(\\d+)\\.rd5").matchEntire(file.name) ?: error("not a tile name: ${file.name}")
        val lon = m.groupValues[2].toInt() * (if (m.groupValues[1] == "W") -1 else 1)
        val lat = m.groupValues[4].toInt() * (if (m.groupValues[3] == "S") -1 else 1)
        lonDeg0 = lon + 180
        latDeg0 = lat + 90
    }

    /** Cells per side of the tile. */
    val cellsPerSide: Int get() = 5 * divisor

    /** True when the absolute cell ([lonIdx], [latIdx]) lies in this tile. */
    fun holds(lonIdx: Int, latIdx: Int): Boolean =
        lonIdx - lonDeg0 * divisor in 0 until cellsPerSide && latIdx - latDeg0 * divisor in 0 until cellsPerSide

    /** The node ids of one cell, in the file's order; empty where the degree or the cell has no data. */
    fun cell(lonIdx: Int, latIdx: Int): LongArray {
        require(holds(lonIdx, latIdx)) { "cell ($lonIdx, $latIdx) is outside ${file.name}" }
        val lonDeg = lonIdx / divisor
        val latDeg = latIdx / divisor
        val osmf = files.getOrPut(lonDeg * 1000 + latDeg) {
            OsmFile(pf, lonDeg, latDeg, buffers).takeIf { it.hasData() }
        } ?: return LongArray(0)
        val mc = osmf.createMicroCache(lonIdx, latIdx, buffers, null, null, true, null) ?: return LongArray(0)
        return LongArray(mc.getSize()) { mc.getIdForIndex(it) }
    }

    override fun close() {
        // PhysicalFile.ra is package-private (PhysicalFile.java:18), so it is closed by reflection, as TileCheck did.
        val f = PhysicalFile::class.java.getDeclaredField("ra")
        f.isAccessible = true
        (f.get(pf) as RandomAccessFile?)?.close()
    }

    companion object {
        fun ilon(id: Long): Int = (id ushr 32).toInt()

        fun ilat(id: Long): Int = id.toInt()
    }
}
