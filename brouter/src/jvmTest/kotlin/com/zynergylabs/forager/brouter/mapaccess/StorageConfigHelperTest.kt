package com.zynergylabs.forager.brouter.mapaccess

import com.zynergylabs.forager.brouter.mapaccess.MapaccessEngines.outcome
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

/** `StorageConfigHelper.getSecondarySegmentDir`, and `NodesCache.fileForSegment`'s use of it, against the oracle. */
class StorageConfigHelperTest {
    @Test
    fun theSecondaryFolderIsReadAsTheOracleReadsIt() {
        val root = Files.createTempDirectory("storage-config").toFile()
        try {
            val segments = File(root, "segments").apply { mkdirs() }
            val secondary = File(root, "secondary").apply { mkdirs() }
            val configs = listOf(
                null,
                "secondary_segment_dir=../secondary\n",
                "# secondary_segment_dir=../secondary\n",
                "\t  secondary_segment_dir= ../secondary  \n",
                "additional_maptool_dir=x\nsecondary_segment_dir=${secondary.path}\n",
                "secondary_segment_dir=../absent\n",
                "secondary_segment_dir=../secondary\nsecondary_segment_dir=${secondary.path}\n",
                "",
            )
            var found = 0
            for (config in configs) {
                val file = File(segments, "storageconfig.txt")
                if (config == null) file.delete() else file.writeText(config)
                val oracle = btools.mapaccess.StorageConfigHelper.getSecondarySegmentDir(segments)?.path
                val port = StorageConfigHelper.getSecondarySegmentDir(FileSystem.SYSTEM, segments.toOkioPath())?.toString()
                assertEquals(oracle, port, "config: $config")
                if (oracle != null) found++
            }
            assertEquals(4, found)
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun aTileOnlyInTheSecondaryFolderIsOpenedByBoth() {
        val root = Files.createTempDirectory("storage-config").toFile()
        try {
            val segments = File(root, "segments").apply { mkdirs() }
            val secondary = File(root, "secondary").apply { mkdirs() }
            MapaccessEngines.e170s50.copyTo(File(secondary, "E170_S50.rd5"))
            File(segments, "storageconfig.txt").writeText("secondary_segment_dir=../secondary\n")
            // A square with no data: the file is opened and its sub-index skipped, with no cell decoded (step 5b's).
            val (ilon, ilat) = MapaccessEngines.pointIn(MapaccessEngines.e170EmptyDegrees().first())
            val oracle = MapaccessEngines.oracleCache(segments)
            val port = MapaccessEngines.portCache(segments)
            assertEquals("ok:0", outcome { oracle.loadSegmentFor(ilon, ilat) })
            assertEquals("ok:0", outcome { port.loadSegmentFor(ilon, ilat) })
            assertEquals(false, oracle.first_file_access_failed)
            assertEquals(false, port.first_file_access_failed)
            assertEquals("E170_S50.rd5", port.first_file_access_name)
            assertEquals(oracle.getElevationType(ilon, ilat), port.getElevationType(ilon, ilat))
            oracle.close()
            port.close()
        } finally {
            root.deleteRecursively()
        }
    }
}
