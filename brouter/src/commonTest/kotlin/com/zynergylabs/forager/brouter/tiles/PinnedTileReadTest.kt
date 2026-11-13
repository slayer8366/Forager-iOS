package com.zynergylabs.forager.brouter.tiles

import okio.FileSystem
import okio.HashingSink
import okio.Path.Companion.toPath
import okio.blackholeSink
import okio.buffer
import okio.use
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

/**
 * Step 10's first test (RECORD -137, decision 4; the step 10 survey, (f) 1): one pinned tile opened with okio's
 * `FileSystem.SYSTEM` and held to its row in tools/tile-pins.tsv, size and SHA-256 (okio's own hashing; no new
 * dependency). The tile is F0's fixture, `f0/W75_N40.rd5`, the smallest pinned file and the one F0's routes read.
 *
 * The tiles' folder comes from [testSetting]: on the JVM, jvmTest's `brouter.tiles.dir` (the tiles are already fetched
 * there, so this test runs for real on the JVM, not skipped); on the simulator, `BROUTER_TILES_DIR`. A missing setting or
 * a missing file fails with its name, never a crash. The pin row's values are copied below; where the pin file's path is
 * given (the JVM's `brouter.tiles.pins`), the copy is held to the file's row, so it cannot drift unseen.
 */
class PinnedTileReadTest {
    @Test
    fun f0FixtureOpensAndMatchesItsPin() {
        val source = testSettingSource("brouter.tiles.dir", "BROUTER_TILES_DIR")
        val dir = testSetting("brouter.tiles.dir", "BROUTER_TILES_DIR")
            ?: fail("no tiles folder: $source is not set")
        val tile = dir.toPath() / PIN_PATH
        println("step10 tile: tiles folder from $source = $dir; opening $tile")
        val metadata = FileSystem.SYSTEM.metadataOrNull(tile)
            ?: fail("$tile does not exist or cannot be read in this process (folder ${if (FileSystem.SYSTEM.exists(dir.toPath())) "exists" else "missing"})")
        val hashing = HashingSink.sha256(blackholeSink())
        val read = FileSystem.SYSTEM.source(tile).buffer().use { src -> hashing.buffer().use { src.readAll(it) } }
        val sha = hashing.hash.hex()
        println("step10 tile: $tile size=${metadata.size} read=$read sha256=$sha")
        assertEquals(PIN_BYTES, metadata.size, "$tile: size against tile-pins.tsv")
        assertEquals(PIN_BYTES, read, "$tile: bytes read against tile-pins.tsv")
        assertEquals(PIN_SHA256, sha, "$tile: SHA-256 against tile-pins.tsv")

        val pins = testSetting("brouter.tiles.pins", "BROUTER_TILE_PINS")
        if (pins == null) {
            println("step10 tile: pin file not given on this platform; the copied row is held to it on the JVM")
        } else {
            val row = FileSystem.SYSTEM.read(pins.toPath()) { readUtf8() }.lines()
                .map { it.split('\t') }.singleOrNull { it.size >= 4 && it[1] == PIN_PATH }
                ?: fail("$pins: no single row for $PIN_PATH")
            assertEquals(listOf(PIN_PATH, PIN_BYTES.toString(), PIN_SHA256), row.subList(1, 4), "the copied row against $pins")
        }
    }

    private companion object {
        // tools/tile-pins.tsv, row forager-fixture-W75_N40.rd5: path, bytes, sha256.
        const val PIN_PATH = "f0/W75_N40.rd5"
        const val PIN_BYTES = 152665L
        const val PIN_SHA256 = "ff6d9d8e439c3cdf914b1566619f64466d9af993c28070ea893425e7f5ec5990"
    }
}
