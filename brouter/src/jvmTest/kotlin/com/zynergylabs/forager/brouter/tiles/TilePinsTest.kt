package com.zynergylabs.forager.brouter.tiles

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Item I, step 1 (Phase 1 report; Amendment 3 Part B; RECORD -45): each tile is fetched from the release and checked
 * against its pin. The fetch is the build's fetchTiles task (tools/fetch-tiles.sh, which refuses a file whose size or
 * SHA-256 differs); this test then checks the files again, independently of the script.
 *
 * The pins are held here as well as in tools/tile-pins.tsv, as OraclePinTest holds the jar's, so an edit to the pin
 * file alone cannot pass a different tile through: the fetch would accept the file the edited pins describe, and
 * [pinFileIsTheReleasesOwn] would fail on the edited row.
 */
class TilePinsTest {
    private class Pin(val asset: String, val path: String, val bytes: Long, val sha256: String) {
        override fun toString() = "$asset\t$path\t$bytes\t$sha256"
    }

    @Test
    fun pinFileIsTheReleasesOwn() {
        val lines = TestTiles.pinFile.readLines().filter { it.isNotEmpty() && !it.startsWith("#") }
        assertEquals(COLUMNS, lines.first().split('\t'), "the pin file's header row")
        val rows = lines.drop(1).map { it.split('\t') }
        rows.forEach { assertEquals(COLUMNS.size, it.size, "columns in row ${it.first()}") }
        assertEquals(
            PINS.joinToString("\n"),
            rows.joinToString("\n") { Pin(it[0], it[1], it[2].toLong(), it[3]).toString() },
            "tools/tile-pins.tsv's asset, path, bytes and sha256 against the release's (RECORD -45)",
        )
    }

    @Test
    fun eachFetchedTileMatchesItsPin() {
        var checked = 0
        for (pin in PINS) {
            val file = TestTiles.dir.resolve(pin.path)
            assertTrue(file.isFile, "${pin.asset}: no file at $file; the fetch did not leave it")
            assertEquals(pin.bytes, file.length(), "${pin.asset}: bytes")
            assertEquals(pin.sha256, TestTiles.sha256(file), "${pin.asset}: SHA-256")
            println("${pin.asset}: ${file.length()} bytes, SHA-256 ${pin.sha256}, at ${pin.path}")
            checked++
        }
        assertEquals(5, checked, "the four tiles and F0's fixture")
    }

    private companion object {
        val COLUMNS = listOf("asset", "path", "bytes", "sha256", "source", "last_modified", "etag", "captured")

        // The release brouter-tiles-2026-10-30's own tile-pins.tsv (SHA-256 ccfc380e...), each data asset of which
        // the planner downloaded and checked against these figures (RECORD -45).
        val PINS = listOf(
            Pin("W75_N40.rd5", "segments/W75_N40.rd5", 119_765_681L,
                "c94a4d34c364ddbd082d5ce68bfd24aca1756035b66e485aff4486ea5c1fd6fb"),
            Pin("W75_N35.rd5", "segments/W75_N35.rd5", 3_572_520L,
                "a7113b6c2aaedce4325bc0002c9aadaa29feb20f3808df863c71735d7b0cdb1b"),
            Pin("W70_N40.rd5", "segments/W70_N40.rd5", 5_292_622L,
                "0c305efb144b419b1083422a5b62301615f9023fcd67ea77f8342f3cd0906149"),
            Pin("E170_S50.rd5", "segments/E170_S50.rd5", 1_061_705L,
                "f1710c3285d037d676bcfe6a87964b66e2e2d20dd1bd964b7d0a71e98458cf5c"),
            Pin("forager-fixture-W75_N40.rd5", "f0/W75_N40.rd5", 152_665L,
                "ff6d9d8e439c3cdf914b1566619f64466d9af993c28070ea893425e7f5ec5990"),
        )
    }
}
