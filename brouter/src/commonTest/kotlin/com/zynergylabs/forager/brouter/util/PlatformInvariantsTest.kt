package com.zynergylabs.forager.brouter.util

import com.zynergylabs.forager.brouter.msun.msunCos
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * H step 4's platform-invariants test (Phase 1 report, "Part 7, step 4"), as far as step 2b reaches: what CheapRuler and
 * CheapAngleMeter rest on, checked the same way on the JVM and on the iOS simulator.
 * - Exact values, written here: fused multiply-add never formed; Java's double-to-int conversion; Java's `%`.
 * - The production scale table (msun's cos) and the production angles (msun's atan2, through getAngle and getDirection),
 *   by chunk digest against constants written on the CI JVM from outside the port (PlatformInvariantsExpectedTest).
 * - A positive control on every platform: one row's cos moved by one ulp changes its chunk's digest.
 * Not yet here (later steps): number parsing and printing, min/max, the Math.round replacement's distance sites, hmap.
 */
class PlatformInvariantsTest {
    @Test
    fun multiplyThenAddIsNeverFused() {
        // (1 + 2^-27)^2 = 1 + 2^-26 + 2^-54, which rounds to 1 + 2^-26: unfused, x * x - y is 0; fused, it is 2^-54.
        val x = Double.fromBits(0x3FF0000002000000L)
        val y = Double.fromBits(0x3FF0000004000000L)
        assertEquals(0L, (x * x - y).toRawBits(), "double: x * x - y")
        // The table's own shape, 2 * cos * cos - 1, with the same detector.
        assertEquals(0L, (2 * (x / 2) * x - y).toRawBits(), "double: 2 * (x / 2) * x - y")
        // Float: (1 + 2^-12)^2 = 1 + 2^-11 + 2^-24, a tie that rounds to even, 1 + 2^-11.
        val fx = Float.fromBits(0x3F800800)
        val fy = Float.fromBits(0x3F801000)
        assertEquals(0, (fx * fx - fy).toRawBits(), "float: fx * fx - fy")
    }

    @Test
    fun doubleToIntIsJavas() {
        // JLS 5.1.3: NaN to 0, toward zero, saturating. normalize's (int) (a / 360).
        val cases = listOf(
            Double.NaN to 0, Double.POSITIVE_INFINITY to Int.MAX_VALUE, Double.NEGATIVE_INFINITY to Int.MIN_VALUE,
            3e9 to Int.MAX_VALUE, -3e9 to Int.MIN_VALUE, -0.9 to 0, 0.9 to 0, -1.5 to -1, 27777777.77 to 27777777,
        )
        for ((d, i) in cases) assertEquals(i, d.toInt(), "toInt($d)")
        // normalize's 360 * (int) (...) is int arithmetic, so it wraps: 360 * 27,777,777 = 9,999,999,720 = 1,410,065,128 mod 2^32.
        assertEquals(1410065128, 360 * (1e10 / 360).toInt())
        assertEquals(8589934872.0, CheapAngleMeter.normalize(1e10))
    }

    @Test
    fun remainderIsJavas() {
        // JLS 15.17.3: the sign of the dividend, truncating; getDifferenceFromDirection's (b2 - b1) % 360.0.
        assertEquals(-10.0, -370.0 % 360.0)
        assertEquals(10.0, 370.0 % -360.0)
        assertEquals(0.5, 720.5 % 360.0)
        assertEquals((-0.0).toRawBits(), (-0.0 % 360.0).toRawBits())
        assertEquals(5.0, 5.0 % Double.POSITIVE_INFINITY)
        assertTrue((Double.POSITIVE_INFINITY % 360.0).isNaN())
        assertTrue((5.0 % 0.0).isNaN())
        // getDifferenceFromDirection: each wrap branch, and 180 on both sides of the boundary.
        assertEquals(20.0, CheapAngleMeter.getDifferenceFromDirection(10.0, 350.0))
        assertEquals(20.0, CheapAngleMeter.getDifferenceFromDirection(350.0, 10.0))
        assertEquals(180.0, CheapAngleMeter.getDifferenceFromDirection(0.0, 180.0))
        assertEquals(180.0, CheapAngleMeter.getDifferenceFromDirection(0.0, -180.0))
        assertEquals(0.0, CheapAngleMeter.getDifferenceFromDirection(10.0, 730.0))
    }

    @Test
    fun productionTableMatchesTheJvmDigests() {
        var rows = 0
        val got = tableChunkDigests { row -> rows++; productionTableRow(row) }
        assertEquals(1800, rows, "rows digested")
        assertEquals(18, got.size, "chunks")
        assertEquals(18, EXPECTED_TABLE_CHUNKS.size, "expected chunks")
        val bad = got.indices.filter { got[it] != EXPECTED_TABLE_CHUNKS[it] }
        for (c in bad) {
            for (row in c * TABLE_ROWS_PER_CHUNK until (c + 1) * TABLE_ROWS_PER_CHUNK) {
                val r = productionTableRow(row)
                println("table row $row: ${r.lat.toRawBits().toULong().toString(16)} ${r.cos.toRawBits().toULong().toString(16)} " +
                    "${r.kx.toRawBits().toULong().toString(16)} ${r.ky.toRawBits().toULong().toString(16)}")
            }
        }
        assertEquals(emptyList(), bad, "table chunks (of 100 rows) whose digest differs from the CI JVM's")
    }

    @Test
    fun productionAnglesMatchTheJvmDigests() {
        val got = angleChunkDigests(
            { a, b, c, d -> CheapAngleMeter.getAngle(MsunLibm, a, b, c, d) },
            { a, b, c, d -> CheapAngleMeter.getDirection(MsunLibm, a, b, c, d) },
        )
        assertEquals(299974, got.pairs, "pairs")
        assertEquals(4 * 299974, got.evaluations, "evaluations")
        assertEquals(30, got.chunks.size, "chunks")
        assertEquals(30, EXPECTED_ANGLE_CHUNKS.size, "expected chunks")
        val bad = got.chunks.indices.filter { got.chunks[it] != EXPECTED_ANGLE_CHUNKS[it] }
        assertEquals(emptyList(), bad, "angle chunks (of 10,000 pairs) whose digest differs from the CI JVM's")
    }

    @Test
    fun oneUlpInOneRowChangesItsChunksDigest() {
        // H step 4's positive control: row 500's cos moved up one ulp, kx and ky recomputed from it.
        val got = tableChunkDigests { row ->
            if (row != 500) productionTableRow(row) else {
                val r = productionTableRow(row)
                val moved = Double.fromBits(r.cos.toRawBits() + 1)
                val kxky = CheapRuler.calcKxKyFromILat(row * CheapRuler.SCALE_CACHE_INCREMENT + CheapRuler.SCALE_CACHE_INCREMENT / 2) { moved }
                TableRow(r.lat, moved, kxky[0], kxky[1])
            }
        }
        assertEquals(18, got.size)
        assertNotEquals(EXPECTED_TABLE_CHUNKS[5], got[5], "chunk 5 with row 500 moved")
        assertEquals(EXPECTED_TABLE_CHUNKS.filterIndexed { i, _ -> i != 5 }, got.filterIndexed { i, _ -> i != 5 }, "the other chunks")
    }
}

/** A production row: lat and cos as the production table's builder sees them, kx and ky from the production table. */
internal fun productionTableRow(row: Int): TableRow {
    var lat = Double.NaN
    var cos = Double.NaN
    CheapRuler.calcKxKyFromILat(row * CheapRuler.SCALE_CACHE_INCREMENT + CheapRuler.SCALE_CACHE_INCREMENT / 2) { x ->
        lat = x
        msunCos(x).also { cos = it }
    }
    val kxky = MsunLibm.scales(row)
    return TableRow(lat, cos, kxky[0], kxky[1])
}
