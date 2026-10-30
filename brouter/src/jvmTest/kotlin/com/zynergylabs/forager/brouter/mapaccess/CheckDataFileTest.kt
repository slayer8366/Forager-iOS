package com.zynergylabs.forager.brouter.mapaccess

import com.zynergylabs.forager.brouter.checkDataFile
import okio.Path.Companion.toOkioPath
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Item C's `checkDataFile`, against Forager's `RoutingDataFolder.headerProblem` run on the oracle's `PhysicalFile`. */
class CheckDataFileTest {
    @Test
    fun eachCapturedTileReadsOnBothEngines() {
        for (file in MapaccessEngines.capturedTiles) {
            assertNull(MapaccessEngines.oracleHeaderProblem(file), file.path)
            assertNull(checkDataFile(file.toOkioPath()), file.path)
        }
    }

    @Test
    fun aMissingFileGivesTheOraclesMessage() {
        val folder = Files.createTempDirectory("check-data-file").toFile()
        try {
            val missing = File(folder, "W80_N40.rd5")
            val expected = MapaccessEngines.oracleHeaderProblem(missing)
            // RandomAccessFile's FileNotFoundException: the path, then the OS's reason.
            assertEquals("${missing.path} (No such file or directory)", expected)
            assertEquals(expected, checkDataFile(missing.toOkioPath()))
        } finally {
            folder.deleteRecursively()
        }
    }
}
