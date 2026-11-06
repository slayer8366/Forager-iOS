package com.zynergylabs.forager.brouter.oracle.e3.strata

import com.zynergylabs.forager.brouter.oracle.e3.E3Case
import com.zynergylabs.forager.brouter.oracle.e3.E3Stability
import com.zynergylabs.forager.brouter.tiles.TestTiles
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Stratum F0h (RECORD -57, -58): the eight routes of the route cutter's `routes.json` on Forager's heighted square
 * (`segments-heighted/W75_N40.rd5` at Forager d6e2a0415, [TestTiles.f0h]) with Forager's profile, through E3's six
 * regimes. It is the data that can fail the heights digest: F0's own fixture has no heights.
 *
 * The ends are `server/pmtiles-worker/test/fixtures/rd5/routes.json` at Forager d6e2a0415, each route's `from` and `to`
 * as written there; `BRouterHeightedRouteTest.kt`'s `ROUTES` (`:90-99` there) carries the same sixteen ends.
 *
 * Reach evidence: R1 agrees with BRouter's command-line router on the same square, as Forager's own test holds it
 * (`BRouterHeightedRouteTest.kt:46-59` there): the point count and the heights digest of
 * `app/src/test/resources/routing/brouter-cli/heighted/<name>.geojson` there, computed by `tools/geojson-heights.py`
 * (its output is `step1b/12-f0h-expected.tsv`). Only counts and digests are carried here, no coordinates beyond Forager's
 * own ends (E1, as [E3F0Test]).
 */
class E3F0hTest {
    @Test
    fun f0hIsStableUnderE3AndR1HasTheCommandLineRoutersHeights() {
        E3Strata.jdk("F0h")
        val cases = ENDS.map { (name, e) -> E3Case("F0h-$name", TestTiles.f0h, e[0], e[1], e[2], e[3], PROFILE) }
        val r = E3Stability.run("F0h", cases, File(E3Strata.work, "F0h"), E3Strata.agent, E3Strata.workers, perCaseSeconds = 300)
        E3Strata.report(r)
        val r1 = r.verdicts.associate { it.case.id.removePrefix("F0h-") to it.outcomes.getValue("R1") }
        assertEquals(ENDS.keys.associateWith { "route" }, r1.mapValues { it.value.kind }, "F0h: every route is found")
        assertEquals(
            EXPECTED,
            r1.mapValues { it.value.nodes to it.value.heightDigest },
            "F0h: R1's point count and heights digest are not the command-line router's",
        )
    }

    internal companion object {
        const val PROFILE = "hiking-mountain" // Forager's RoutingProfile.PROFILE_FILE

        /** name to (from lat, from lon, to lat, to lon), `routes.json` at Forager d6e2a0415, in its order. */
        val ENDS = linkedMapOf(
            "inside-across-the-degree-line" to doubleArrayOf(41.2742564, -73.9978932, 41.3036912, -74.0159457),
            "inside-short" to doubleArrayOf(41.2741743, -74.0013283, 41.2788313, -74.0189684),
            "inside-long" to doubleArrayOf(41.2772811, -74.0422743, 41.292043, -73.996678),
            "edge-north-a" to doubleArrayOf(41.3047796, -74.0006515, 41.3053493, -74.0355387),
            "edge-north-b" to doubleArrayOf(41.3061883, -73.9991069, 41.3020883, -74.0438051),
            "detour-west" to doubleArrayOf(41.2940064, -74.0832124, 41.2535731, -74.0887379),
            "outside-west-island" to doubleArrayOf(41.2749253, -73.9994484, 41.2684229, -74.1114362),
            "outside-west-moved" to doubleArrayOf(41.2749253, -73.9994484, 41.2588594, -74.1204499),
        )

        /**
         * name to (points, heights digest) of `brouter-cli/heighted/<name>.geojson` at Forager d6e2a0415, from
         * `python3 -I tools/geojson-heights.py` (2026-11-04). Every point there has a height: 3,630 over the eight.
         */
        val EXPECTED = mapOf(
            "inside-across-the-degree-line" to (226 to "602d4109bb916eca"),
            "inside-short" to (167 to "7dd5bc2f418a942e"),
            "inside-long" to (306 to "81ca705bd6652037"),
            "edge-north-a" to (485 to "44ac5ed74f690531"),
            "edge-north-b" to (522 to "3bf34cc4b7d132bb"),
            "detour-west" to (445 to "78f521e1799fa575"),
            "outside-west-island" to (737 to "144b2f26f2ecec83"),
            "outside-west-moved" to (742 to "ee68cd97a2dfa1b5"),
        )
    }
}
