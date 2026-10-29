package com.zynergylabs.forager.brouter

import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Amendment 2's smoke test: one commonTest test, run on iosSimulatorArm64 on macos-26, to answer whether Kotlin 2.3.10
 * links and runs a simulator test there before the port starts. What it proves is that it ran, read as a count from the
 * JUnit XML; its assertion is incidental, so it is listed "not shown to bite". It goes through okio because the port's
 * file reads depend on okio's iOS klib linking.
 */
class ToolchainSmokeTest {
    @Test
    fun okioSplitsATilePathOnThisTarget() {
        val tile = "segments4/W75_N40.rd5".toPath()
        assertEquals("W75_N40.rd5", tile.name)
        assertEquals("segments4", tile.parent?.name)
    }
}
