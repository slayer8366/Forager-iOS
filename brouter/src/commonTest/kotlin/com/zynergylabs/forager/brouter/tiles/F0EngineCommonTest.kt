package com.zynergylabs.forager.brouter.tiles

import okio.FileSystem
import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

/**
 * Step 10, session 46 (RECORD -138, decision 1): stratum F0, Forager's own two routes, through the engine as
 * `RoutingEngineF0Test.port` drives it ([EngineRun], `MsunLibm`), held to F0's two rows of
 * brouter/src/jvmTest/resources/e3/expected-1b.tsv on all six fields and on the stored tuple digest, which this test
 * recomputes in common code ([EngineRun.fnv1aText]; `Step10CommonDigestTest` holds that against the stored digests on the
 * JVM). The ends are Forager's own (RoutingEngineF0Test's CASES; E1 allows them), `hiking-mountain`, memory class 64, no
 * time limit. Paths as [F0RouteCommonTest]'s.
 */
class F0EngineCommonTest {
    @Test
    fun f0CasesThroughTheEngineMatchExpected1bOnSixFields() {
        val tiles = setting("brouter.tiles.dir", "BROUTER_TILES_DIR").toPath() / "f0"
        val profile = setting("brouter.oracle.dir", "BROUTER_ORACLE_DIR").toPath() / "profiles2" / "hiking-mountain.brf"
        for (p in listOf(tiles / "W75_N40.rd5", profile, profile.parent!! / "lookups.dat")) {
            if (!FileSystem.SYSTEM.exists(p)) fail("$p does not exist or cannot be read in this process")
        }
        val mismatches = mutableListOf<String>()
        for (c in CASES) {
            val got = EngineRun.run("${c.startLon},${c.startLat}|${c.endLon},${c.endLat}", profile, tiles)
            println("step10 F0 engine ${c.id}: port ${got.toField()} tuple ${got.tupleDigest}; expected ${c.want.toField()} tuple ${c.tupleDigest}")
            val w = c.want
            listOfNotNull(
                "kind".takeIf { got.kind != w.kind },
                "nodes".takeIf { got.nodes != w.nodes },
                "cost".takeIf { got.cost != w.cost },
                "distance".takeIf { got.distance != w.distance },
                "nodeDigest".takeIf { got.nodeDigest != w.nodeDigest },
                "heightsDigest".takeIf { got.heightsDigest != w.heightsDigest },
                "tupleDigest".takeIf { got.tupleDigest != c.tupleDigest },
            ).forEach { mismatches += "${c.id} $it" }
        }
        assertEquals(emptyList(), mismatches, "F0 through the engine against expected-1b.tsv (six fields and the tuple digest)")
    }

    private class Case(
        val id: String, val startLat: Double, val startLon: Double, val endLat: Double, val endLon: Double,
        val want: EngineRun.Outcome, val tupleDigest: String,
    )

    private companion object {
        // Ends: RoutingEngineF0Test.CASES. Values: expected-1b.tsv's F0 rows (kind, nodes, cost, distance, nodeDigest,
        // heightsDigest, tupleDigest), written on the no-intrinsic oracle (RECORD -88).
        val CASES = listOf(
            Case("F0-inside-across-the-degree-line", 41.2742564, -73.9978932, 41.3036912, -74.0159457,
                EngineRun.Outcome("route", 226, 5302, 5047, "9ca48d05a90f4dcd", "afc27854ee968af5"), "e5aadbeff215a44e"),
            Case("F0-inside-long", 41.2772811, -74.0422743, 41.292043, -73.996678,
                EngineRun.Outcome("route", 306, 7405, 6713, "7d242f8dd2c428b4", "4f6849ea59b5f775"), "177bcede53dc1beb"),
        )

        fun setting(jvmProperty: String, iosVariable: String): String =
            testSetting(jvmProperty, iosVariable) ?: fail("${testSettingSource(jvmProperty, iosVariable)} is not set")
    }
}
