package com.zynergylabs.forager.brouter.oracle

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Item E4's failure kinds that need no tile, the oracle's half only (the planner's ruling, 2026-10-29: the port's half of
 * each kind arrives with the step that ports it; FK 6's at step 5a). Expected values are the oracle's own messages.
 */
class OracleFailureKindsTest {
    @Test
    fun fk6DataMissingAFolderWithoutTheTile() {
        val folder = Files.createTempDirectory("fk6-no-tile").toFile()
        try {
            val result = OracleCalls.route(ACROSS_FROM, ACROSS_TO, folder, maxRunningTimeMillis = 0L)
            // NodesCache.java:330-332 (Forager e2969abe4): the first tile read fails, so the engine names the tile.
            assertEquals("datafile W75_N40.rd5 not found", result.errorMessage)
            assertEquals(0, result.nodeCount)
        } finally {
            folder.deleteRecursively()
        }
    }

    private companion object {
        // Forager's 'inside-across-the-degree-line' points, (lat, lon) (RoutingTestData.kt at Forager e2969abe4,
        // as part 1's ForagerCalls.java quotes them). Their tile, W75_N40, is deliberately absent.
        val ACROSS_FROM = doubleArrayOf(41.2742564, -73.9978932)
        val ACROSS_TO = doubleArrayOf(41.3036912, -74.0159457)
    }
}
