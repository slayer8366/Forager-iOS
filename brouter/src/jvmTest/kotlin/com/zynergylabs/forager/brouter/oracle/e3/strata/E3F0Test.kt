package com.zynergylabs.forager.brouter.oracle.e3.strata

import com.zynergylabs.forager.brouter.oracle.e3.E3Case
import com.zynergylabs.forager.brouter.oracle.e3.E3Stability
import com.zynergylabs.forager.brouter.tiles.TestTiles
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Stratum F0 (Phase 1 report, E4): Forager's own two routes on its sparse `W75_N40.rd5`, with its profile, through E3's
 * six regimes. Its reach evidence: R1 agrees with BRouter's command-line router, as Forager's own test holds it
 * (`BRouterTrailRouterTest.kt:57-64` at Forager e2969abe4): the point count and `track-length` of
 * `app/src/test/resources/routing/brouter-cli/<name>.geojson` there (226 and 5047; 306 and 6713). Only counts and lengths
 * are carried here, so no coordinates beyond Forager's own ends are committed (E1).
 */
class E3F0Test {
    @Test
    fun f0IsStableUnderE3AndR1IsTheCommandLineRoute() {
        val cases = listOf(
            E3Case("F0-inside-across-the-degree-line", TestTiles.f0, 41.2742564, -73.9978932, 41.3036912, -74.0159457, PROFILE),
            E3Case("F0-inside-long", TestTiles.f0, 41.2772811, -74.0422743, 41.292043, -73.996678, PROFILE),
        )
        val r = E3Stability.run("F0", cases, File(E3Strata.work, "F0"), E3Strata.agent, E3Strata.workers, perCaseSeconds = 300)
        E3Strata.report(r)
        val r1 = r.verdicts.associate { it.case.id to it.outcomes.getValue("R1") }
        assertEquals(listOf("route", "route"), r1.values.map { it.kind })
        assertEquals(226 to 5047, r1.getValue("F0-inside-across-the-degree-line").let { it.nodes to it.distance })
        assertEquals(306 to 6713, r1.getValue("F0-inside-long").let { it.nodes to it.distance })
    }

    private companion object {
        const val PROFILE = "hiking-mountain" // Forager's RoutingProfile.PROFILE_FILE
    }
}
