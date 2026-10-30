package com.zynergylabs.forager.brouter.mapaccess

import com.zynergylabs.forager.brouter.codec.DataBuffers
import com.zynergylabs.forager.brouter.mapaccess.MapaccessEngines.field
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Item I, step 5a: each captured tile's header, sub-indexes and integrity check, port against the oracle. */
class TileHeaderTest {
    @Test
    fun eachCapturedTilesHeaderEqualsTheOracles() {
        var checked = 0
        for (file in MapaccessEngines.capturedTiles) {
            val oracle = MapaccessEngines.oraclePhysical(file)
            val port = MapaccessEngines.portPhysical(file)
            try {
                assertEquals(field(oracle, "fileName"), port.fileName, file.path)
                assertContentEquals(field(oracle, "fileIndex") as LongArray, port.fileIndex, file.path)
                assertContentEquals(field(oracle, "fileHeaderCrcs") as IntArray?, port.fileHeaderCrcs, file.path)
                assertEquals(oracle.creationTime, port.creationTime, file.path)
                assertEquals(oracle.divisor, port.divisor, file.path)
                assertEquals(oracle.elevationType, port.elevationType, file.path)
                println("5a header ${file.parentFile.name}/${file.name}: divisor ${port.divisor}, elevationType " +
                    "${port.elevationType}, creationTime ${port.creationTime}, fileIndex[24] ${port.fileIndex[24]}")
                checked++
            } finally {
                oracle.close()
                port.close()
            }
        }
        assertEquals(5, checked)
    }

    @Test
    fun eachCapturedTilesSubIndexesEqualTheOracles() {
        var degreesWithData = 0
        for (file in MapaccessEngines.capturedTiles) {
            val oracle = MapaccessEngines.oraclePhysical(file)
            val port = MapaccessEngines.portPhysical(file)
            // The file's own square: its name's corner, as (lon + 180, lat + 90).
            val lon = file.name.substring(1, file.name.indexOf('_')).toInt().let { if (file.name[0] == 'W') -it else it }
            val latPart = file.name.substring(file.name.indexOf('_') + 1, file.name.indexOf('.'))
            val lat = latPart.substring(1).toInt().let { if (latPart[0] == 'S') -it else it }
            try {
                for (tileIndex in 0 until 25) {
                    val lonDegree = lon + 180 + tileIndex / 5
                    val latDegree = lat + 90 + tileIndex % 5
                    val o = btools.mapaccess.OsmFile(oracle, lonDegree, latDegree, btools.codec.DataBuffers())
                    val p = OsmFile(port, lonDegree, latDegree, DataBuffers())
                    val where = "${file.path} degree ($lonDegree, $latDegree)"
                    assertEquals(o.hasData(), p.hasData(), where)
                    assertEquals(o.filename, p.filename, where)
                    assertContentEquals(field(o, "posIdx") as IntArray?, p.posIdx, where)
                    if (p.hasData()) degreesWithData++
                }
            } finally {
                oracle.close()
                port.close()
            }
        }
        println("5a sub-indexes: 125 degree squares compared, $degreesWithData with data")
    }

    @Test
    fun eachCapturedTilePassesTheIntegrityCheckOnBothEngines() {
        for (file in MapaccessEngines.capturedTiles) {
            assertNull(btools.mapaccess.PhysicalFile.checkFileIntegrity(file), file.path)
            assertNull(PhysicalFile.checkFileIntegrity(FileSystem.SYSTEM, file.toOkioPath()), file.path)
        }
    }
}
