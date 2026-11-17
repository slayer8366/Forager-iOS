package com.zynergylabs.forager.brouter.oracle.e3

import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Holds the cosine rows' stored expected values ([E3CosRowsExpected], session 76) so the file cannot drift unseen: its row
 * count, its count per id prefix against [E3CosRowsExpected.STRATA], and the SHA-256 of its bytes. Runs no oracle: it is
 * cheap, and part of jvmTest.
 */
class E3CosRowsExpectedFileTest {
    private val bytes = checkNotNull(javaClass.getResource(E3CosRowsExpected.RESOURCE)) {
        "no ${E3CosRowsExpected.RESOURCE} on the test classpath"
    }.readBytes()

    @Test
    fun rowsAreFiftyNinePerCosRow() {
        val rows = E3Expected.read(bytes.toString(Charsets.UTF_8)).rows
        assertEquals(E3CosRowsExpected.ROW_COUNT, rows.size, "rows in ${E3CosRowsExpected.RESOURCE}")
        assertEquals(rows.size, rows.map { it.id }.distinct().size, "an id repeats")
        val perPrefix = rows.groupingBy { it.id.substringBefore('-') }.eachCount()
        assertEquals(E3CosRowsExpected.STRATA.mapValues { it.value.first }, perPrefix, "rows per id prefix")
        val wrongStratum = rows.filter { it.stratum != it.id.substringBefore('-') }.map { it.id }
        assertEquals(emptyList(), wrongStratum, "rows whose stratum is not their id prefix")
    }

    @Test
    fun fileIsItsPinnedDigest() {
        val sha = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
        assertEquals(E3CosRowsExpected.SHA256, sha, "SHA-256 of ${E3CosRowsExpected.RESOURCE}")
    }
}
