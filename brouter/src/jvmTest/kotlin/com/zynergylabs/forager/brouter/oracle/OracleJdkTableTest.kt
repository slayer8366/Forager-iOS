package com.zynergylabs.forager.brouter.oracle

import com.zynergylabs.forager.brouter.util.OracleLibm
import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Step 1b's JDK check (Amendment 3 Part B, "The oracle's JDK"): the oracle's no-intrinsic `Math.cos` table on the test
 * JVM equals Phase 1's measurement on OpenJDK 21.0.12.1, and a difference is a stop.
 *
 * Phase 1's measurement is `math-nointrinsic.txt`, written by `10-tests/tools/TableRows.java` under
 * `-XX:+UnlockDiagnosticVMOptions -XX:-UseLibmIntrinsic`, SHA-256 [PHASE1_NOINTRINSIC_SHA256]
 * (`10-tests/logs/table-run.log` at 20e1f92; Phase 1 report, E2). This test writes the same 1,800 lines with this JVM's
 * `Math.cos`, byte for byte in TableRows' form, and hashes them. It then ties those lines to the oracle itself: every
 * row's kx and ky must equal the oracle's own SCALE_CACHE, built in this process by btools.util.CheapRuler's static
 * initialiser. CheapLogicRunTest compares Math.cos with StrictMath.cos inside one JVM; it does not compare across JDKs.
 */
class OracleJdkTableTest {
    @Test
    fun noIntrinsicCosTableEqualsPhase1sMeasurement() {
        val text = StringBuilder()
        val oracleDiffers = ArrayList<Int>()
        for (i in 0 until SCALE_CACHE_LENGTH) { // a fixed count, so it ends
            // TableRows.row, line for line (calcKxKyFromILat, CheapRuler.java:35-49 at 1.7.10), in Java's evaluation order.
            val ilat = i * SCALE_CACHE_INCREMENT + SCALE_CACHE_INCREMENT / 2
            val lat = DEG_TO_RAD * (ilat * ILATLNG_TO_LATLNG - 90)
            val cos = Math.cos(lat)
            val cos2 = 2 * cos * cos - 1
            val cos3 = 2 * cos * cos2 - cos
            val cos4 = 2 * cos * cos3 - cos2
            val cos5 = 2 * cos * cos4 - cos3
            val kx = (111.41513 * cos - 0.09455 * cos3 + 0.00012 * cos5) * ILATLNG_TO_LATLNG * KILOMETERS_TO_METERS
            val ky = (111.13209 - 0.56605 * cos2 + 0.0012 * cos4) * ILATLNG_TO_LATLNG * KILOMETERS_TO_METERS
            text.append(i).append(' ').append(hex(lat)).append(' ').append(hex(cos)).append(' ')
                .append(hex(kx)).append(' ').append(hex(ky)).append('\n') // PrintWriter.println on Linux
            val oracle = OracleLibm.scales(i)
            if (oracle[0].toRawBits() != kx.toRawBits() || oracle[1].toRawBits() != ky.toRawBits()) oracleDiffers.add(i)
        }
        val sha = MessageDigest.getInstance("SHA-256").digest(text.toString().toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        println(
            "oracle JDK: ${System.getProperty("java.vm.vendor")} ${System.getProperty("java.runtime.version")} " +
                "${System.getProperty("os.arch")}; ${text.lines().size - 1} rows; no-intrinsic Math.cos table SHA-256 $sha",
        )
        assertEquals(emptyList(), oracleDiffers, "rows where these kx, ky differ from the oracle's own SCALE_CACHE")
        assertEquals(PHASE1_NOINTRINSIC_SHA256, sha, "STOP (Amendment 3 Part B): the table differs from Phase 1's")
    }

    private fun hex(d: Double): String = "%016x".format(d.toRawBits())

    private companion object {
        const val PHASE1_NOINTRINSIC_SHA256 = "ee004dc088a12bfa64fdba242a02f12f138bb994d86a73a846d97c22da87b379"
        const val SCALE_CACHE_LENGTH = 1800
        const val SCALE_CACHE_INCREMENT = 100000
        const val ILATLNG_TO_LATLNG = 1e-6
        const val KILOMETERS_TO_METERS = 1000
        val DEG_TO_RAD = Math.PI / 180.0
    }
}
