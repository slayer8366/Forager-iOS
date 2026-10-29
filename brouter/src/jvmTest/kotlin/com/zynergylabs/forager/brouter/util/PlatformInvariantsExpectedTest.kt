package com.zynergylabs.forager.brouter.util

import com.zynergylabs.forager.brouter.msun.MsunCBuild
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The JVM half of H step 4's platform-invariants test: it regenerates PlatformInvariantsExpected.kt's constants from
 * outside the port and fails if they moved, printing the new source. Run in the jvm job on ubuntu-24.04, x86_64, Temurin
 * 21.0.13+9 (brouter.yml), with -XX:-UseLibmIntrinsic.
 * - **The table:** lat and cos from the fresh build of bionic's C (MsunCBuild), and kx and ky by [javaKxKy], this file's
 *   own copy of CheapRuler.java:35-49, which is first pinned to the oracle: fed the oracle's Math.cos, it must give the
 *   oracle's table at every row. So the constants are msun's table through the oracle's arithmetic, and not the port's.
 * - **The angles:** the oracle's own getAngle and getDirection. Its Math.atan2 is the one msun matches at every pair
 *   (CheapProductionRecordTest), so these are also the production values.
 */
class PlatformInvariantsExpectedTest {
    @Test
    fun tableConstantsAreMsunsCosThroughTheOraclesArithmetic() {
        val pinned = (0 until 1800).filter { row ->
            val k = javaKxKy(row) { Math.cos(it) }
            val o = OracleLibm.scales(row)
            k[0].toRawBits() != o[0].toRawBits() || k[1].toRawBits() != o[1].toRawBits()
        }
        assertEquals(emptyList(), pinned, "rows where this file's CheapRuler copy, fed Math.cos, is not the oracle's table")
        val c = MsunCBuild.cosLines.map { line -> line.split(' ').let { f -> listOf(f[0].toLong(), f[1].toULong(16).toLong(), f[2].toULong(16).toLong()) } }
        assertEquals(1800, c.size, "C rows")
        val got = tableChunkDigests { row ->
            check(c[row][0] == row.toLong()) { "C row ${c[row][0]} at $row" }
            val lat = Double.fromBits(c[row][1])
            val cos = Double.fromBits(c[row][2])
            val k = javaKxKy(row) { x -> check(x.toRawBits() == lat.toRawBits()) { "row $row: lat is not tab.c's" }; cos }
            TableRow(lat, cos, k[0], k[1])
        }
        println(digestSource("EXPECTED_TABLE_CHUNKS", got))
        assertEquals(EXPECTED_TABLE_CHUNKS, got)
    }

    @Test
    fun angleConstantsAreTheOraclesOwn() {
        val got = angleChunkDigests(
            { a, b, c, d -> btools.util.CheapAngleMeter.getAngle(a, b, c, d) },
            { a, b, c, d -> btools.util.CheapAngleMeter.getDirection(a, b, c, d) },
        )
        assertEquals(299974, got.pairs)
        println(digestSource("EXPECTED_ANGLE_CHUNKS", got.chunks))
        assertEquals(EXPECTED_ANGLE_CHUNKS, got.chunks)
    }

    /** CheapRuler.java:35-49 for ilat = row * 100000 + 50000, as Java evaluates it, with [cos] in place of Math.cos. */
    private fun javaKxKy(row: Int, cos: (Double) -> Double): DoubleArray {
        val ilat = row * 100000 + 100000 / 2
        val lat = (Math.PI / 180.0) * (ilat * 1e-6 - 90)
        val c = cos(lat)
        val c2 = 2 * c * c - 1
        val c3 = 2 * c * c2 - c
        val c4 = 2 * c * c3 - c2
        val c5 = 2 * c * c4 - c3
        return doubleArrayOf(
            (111.41513 * c - 0.09455 * c3 + 0.00012 * c5) * 1e-6 * 1000,
            (111.13209 - 0.56605 * c2 + 0.0012 * c4) * 1e-6 * 1000,
        )
    }
}
