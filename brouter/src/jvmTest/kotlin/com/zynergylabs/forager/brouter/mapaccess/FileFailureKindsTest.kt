package com.zynergylabs.forager.brouter.mapaccess

import com.zynergylabs.forager.brouter.checkDataFile
import com.zynergylabs.forager.brouter.mapaccess.MapaccessEngines.outcome
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Item E4's FK 6, 7 and 8 at the file layer (item I, step 5a), ten cases each. Each case is compared on three entry
 * points: Forager's header check (`checkDataFile`), the integrity check, and `NodesCache.loadSegmentFor`, the engine's
 * read. The engine's own route message for FK 6 is built in `NodesCache.preloadPosition` (`:330-332`), which arrives
 * with the graph half; this test checks the two fields it is built from.
 */
class FileFailureKindsTest {
    @Test
    fun fk6DataMissing() {
        val folder = MapaccessEngines.folderWithE170(MapaccessEngines.e170s50.readBytes())
        try {
            for ((name, square) in MISSING_SQUARES) {
                val degree = (square.first + name.length % 5) to (square.second + name.length % 3)
                val (ilon, ilat) = MapaccessEngines.pointIn(degree)
                val oracle = MapaccessEngines.oracleCache(folder)
                val port = MapaccessEngines.portCache(folder)
                assertEquals("ok:0", outcome { oracle.loadSegmentFor(ilon, ilat) }, name)
                assertEquals("ok:0", outcome { port.loadSegmentFor(ilon, ilat) }, name)
                assertEquals(true, oracle.first_file_access_failed, name)
                assertEquals(true, port.first_file_access_failed, name)
                assertEquals(name, oracle.first_file_access_name)
                assertEquals(name, port.first_file_access_name)
                val missing = File(folder, name)
                assertEquals(MapaccessEngines.oracleHeaderProblem(missing), checkDataFile(missing.toOkioPath()), name)
                oracle.close()
                port.close()
            }
        } finally {
            folder.deleteRecursively()
        }
    }

    @Test
    fun fk7DataCorruptShort() {
        val bytes = MapaccessEngines.e170s50.readBytes()
        val dataEnd = MapaccessEngines.oraclePhysical(MapaccessEngines.e170s50).let { pf ->
            (MapaccessEngines.field(pf, "fileIndex") as LongArray)[24].toInt().also { pf.close() }
        }
        // Three cuts inside the 200-byte top index (a short read), five in the data or footer, one exactly at the data's
        // end ("old format o.k.", PhysicalFile.java:115), one losing only the last byte.
        val cuts = listOf(1, 100, 199, 200, 201, dataEnd / 2, dataEnd - 1, dataEnd, dataEnd + 1, bytes.size - 1)
        val empty = MapaccessEngines.pointIn(MapaccessEngines.e170EmptyDegrees().first())
        for (cut in cuts) {
            // The integrity check is skipped at the data's end: both engines then read the sub-indexes with divisor 80,
            // and take cell sizes from data bytes, which can ask for an allocation of up to 2 GB.
            compare("FK 7, cut to $cut of ${bytes.size}", bytes.copyOf(cut), empty, integrity = cut != dataEnd)
        }
    }

    @Test
    fun fk8DataCorruptChecksum() {
        val bytes = MapaccessEngines.e170s50.readBytes()
        val pf = MapaccessEngines.oraclePhysical(MapaccessEngines.e170s50)
        val fileIndex = MapaccessEngines.field(pf, "fileIndex") as LongArray
        pf.close()
        val empty = MapaccessEngines.pointIn(MapaccessEngines.e170EmptyDegrees().first())
        // Five bytes of the top index: long 0's version, longs 4, 12 and 18, and long 24's lowest byte.
        val flips = mutableListOf(1 to empty, 37 to empty, 100 to empty, 150 to empty, 199 to empty)
        // Five sub-index bytes, entries 1 to 5's lowest byte, taking E170_S50's squares with data in turn (it has 3;
        // a first draft assumed 5 and failed on its own count, not on the stub).
        val dataDegrees = MapaccessEngines.e170DataDegrees()
        for (i in 0 until 5) {
            val degree = dataDegrees[i % dataDegrees.size]
            val tileIndex = (degree.first - MapaccessEngines.E170_LON_DEGREE) * 5 + degree.second - MapaccessEngines.E170_LAT_DEGREE
            val fileOffset = if (tileIndex > 0) fileIndex[tileIndex - 1].toInt() else 200
            flips += (fileOffset + 4 * (i + 1) + 3) to MapaccessEngines.pointIn(degree)
        }
        assertEquals(10, flips.size)
        for ((offset, point) in flips) {
            val copy = bytes.copyOf()
            copy[offset] = (copy[offset].toInt() xor 0xff).toByte()
            compare("FK 8, byte $offset flipped", copy, point, integrity = true)
        }
    }

    /** One corrupt copy, on three entry points; each port outcome must equal the oracle's. */
    private fun compare(case: String, copy: ByteArray, point: Pair<Int, Int>, integrity: Boolean) {
        val folder = MapaccessEngines.folderWithE170(copy)
        try {
            val file = File(folder, "E170_S50.rd5")
            val header = MapaccessEngines.oracleHeaderProblem(file)
            assertEquals(header, checkDataFile(file.toOkioPath()), "$case: checkDataFile")
            var whole = "skipped"
            if (integrity) {
                whole = outcome { btools.mapaccess.PhysicalFile.checkFileIntegrity(file) }
                assertEquals(whole, outcome { PhysicalFile.checkFileIntegrity(FileSystem.SYSTEM, file.toOkioPath()) }, "$case: integrity")
            }
            val oracle = MapaccessEngines.oracleCache(folder)
            val port = MapaccessEngines.portCache(folder)
            val read = outcome { oracle.loadSegmentFor(point.first, point.second) }
            assertEquals(read, outcome { port.loadSegmentFor(point.first, point.second) }, "$case: loadSegmentFor")
            assertEquals(oracle.first_file_access_failed, port.first_file_access_failed, case)
            oracle.close()
            port.close()
            println("5a $case: checkDataFile=$header | integrity=$whole | loadSegmentFor=$read")
            assertTrue(header != null || whole != "ok:null" || read.startsWith("threw") || case.contains("cut to"), case)
        } finally {
            folder.deleteRecursively()
        }
    }

    private companion object {
        /** Ten squares whose tile is not in the folder, as (lonDegree, latDegree) of their corner; W80_N40 is E4's. */
        val MISSING_SQUARES = listOf(
            "W80_N40.rd5" to (100 to 130),
            "E175_S50.rd5" to (355 to 40),
            "E165_S50.rd5" to (345 to 40),
            "E170_S45.rd5" to (350 to 45),
            "E170_S55.rd5" to (350 to 35),
            "W75_N40.rd5" to (105 to 130),
            "E0_N0.rd5" to (180 to 90),
            "W5_S5.rd5" to (175 to 85),
            "W180_S90.rd5" to (0 to 0),
            "E175_N85.rd5" to (355 to 175),
        )
    }
}
