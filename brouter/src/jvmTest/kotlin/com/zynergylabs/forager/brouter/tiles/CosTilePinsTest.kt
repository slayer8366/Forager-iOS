package com.zynergylabs.forager.brouter.tiles

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Session 75 (session 72's dispatch 5): the cosine rows' five tiles, fetched from the release brouter-cos-tiles-2026-11-17
 * by the build's fetchCosTiles task (tools/fetch-tiles.sh with tools/tile-pins-cos.tsv, which refuses a file whose size
 * or SHA-256 differs), are checked here again, independently of the script, in the style of [TilePinsTest].
 *
 * The pins are held here as well as in the pin file, so an edit to the file alone cannot pass a different tile through.
 * The file itself is held to the release asset's own SHA-256, so it stays a byte-for-byte copy.
 *
 * The tiles must not land in [TestTiles.segments]: RGenerator and LBGenerator list that folder, and one more tile there
 * would change R's, CR's and LB's draws and break E3RSequenceTest's pin (s72 §3.2).
 */
class CosTilePinsTest {
    private class Pin(val asset: String, val path: String, val bytes: Long, val sha256: String) {
        override fun toString() = "$asset\t$path\t$bytes\t$sha256"
    }

    @Test
    fun pinFileIsTheReleasesOwn() {
        val file = TestTiles.cosPinFile
        assertEquals(RELEASE_PIN_FILE_BYTES, file.length(), "tools/tile-pins-cos.tsv's bytes against the release asset's")
        assertEquals(RELEASE_PIN_FILE_SHA256, TestTiles.sha256(file), "tools/tile-pins-cos.tsv's SHA-256 against the release asset's")
        val lines = file.readLines().filter { it.isNotEmpty() && !it.startsWith("#") }
        assertEquals(COLUMNS, lines.first().split('\t'), "the pin file's header row")
        val rows = lines.drop(1).map { it.split('\t') }
        rows.forEach { assertEquals(COLUMNS.size, it.size, "columns in row ${it.first()}") }
        assertEquals(
            PINS.joinToString("\n"),
            rows.joinToString("\n") { Pin(it[0], it[1], it[2].toLong(), it[3]).toString() },
            "tools/tile-pins-cos.tsv's asset, path, bytes and sha256 against the test's pins",
        )
    }

    @Test
    fun eachFetchedTileMatchesItsPin() {
        val root = TestTiles.cosRows.parentFile
        var checked = 0
        for (pin in PINS) {
            val file = root.resolve(pin.path)
            assertTrue(file.isFile, "${pin.asset}: no file at $file; the fetch did not leave it")
            assertEquals(pin.bytes, file.length(), "${pin.asset}: bytes")
            assertEquals(pin.sha256, TestTiles.sha256(file), "${pin.asset}: SHA-256")
            println("${pin.asset}: ${file.length()} bytes, SHA-256 ${pin.sha256}, at ${pin.path} under the cos folder")
            checked++
        }
        assertEquals(5, checked, "the five cosine-row tiles")
        assertEquals(
            PINS.map { it.asset }.sorted(),
            TestTiles.cosRows.list()!!.sorted(),
            "the cos folder holds the five tiles and nothing else",
        )
    }

    @Test
    fun existingSegmentsFolderIsUnchanged() {
        assertNotEquals(
            TestTiles.segments.canonicalFile, TestTiles.cosRows.canonicalFile,
            "the cosine rows' tiles have a folder of their own",
        )
        val listed = TestTiles.segments.list()!!.sorted()
        println("segments lists ${listed.size} files: ${listed.joinToString()}")
        assertEquals(SEGMENTS_BEFORE, listed, "the existing segments folder lists exactly the four tiles it held before session 75")
        val strays = listed.intersect(PINS.map { it.asset }.toSet())
        assertTrue(strays.isEmpty(), "cosine-row tiles in the existing segments folder: $strays")
    }

    private companion object {
        val COLUMNS = listOf("asset", "path", "bytes", "sha256", "source", "last_modified", "etag", "captured")

        /** The release asset tile-pins.tsv of brouter-cos-tiles-2026-11-17 (run 43985424626), read by session 75. */
        const val RELEASE_PIN_FILE_BYTES = 2_491L
        const val RELEASE_PIN_FILE_SHA256 = "b8135f5e38cf38b784d8d84eddecb9fd4f2669b7890dde8aaa29f7c940f683dc"

        /** The four tiles of tools/tile-pins.tsv whose path is under segments/, as fetchTiles leaves them. */
        val SEGMENTS_BEFORE = listOf("E170_S50.rd5", "W70_N40.rd5", "W75_N35.rd5", "W75_N40.rd5")

        // The release brouter-cos-tiles-2026-11-17's own tile-pins.tsv, as fetched and read by session 75.
        val PINS = listOf(
            Pin("E145_S45.rd5", "segments/E145_S45.rd5", 6_316_104L,
                "cb3074754c0f90686f39bfe018aa89ee3df40917b2b316bca9edc01e1e1709a1"),
            Pin("E170_S40.rd5", "segments/E170_S40.rd5", 8_513_057L,
                "147a6c28fc967ec47fda647fe71cad2140261f4b008b9e5d637f94567871c521"),
            Pin("E15_S25.rd5", "segments/E15_S25.rd5", 4_854_933L,
                "9ba3ab2d7456668c78d83e72fe6f5f1a1b7c322a8cc683c833814f20330f57c6"),
            Pin("W95_N10.rd5", "segments/W95_N10.rd5", 7_472_453L,
                "420054ba06efb2e1396957807d0d9f2fddbf21cbf96e98c87bc87376e4fb6231"),
            Pin("W20_N15.rd5", "segments/W20_N15.rd5", 2_622_128L,
                "fe8514fdcc89cf39e7d74e29b0868da77b0169ea6330dde5fc368ad04b60f402"),
        )
    }
}
