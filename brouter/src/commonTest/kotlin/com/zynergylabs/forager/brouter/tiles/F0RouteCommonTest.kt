package com.zynergylabs.forager.brouter.tiles

import com.zynergylabs.forager.brouter.RouteCancel
import com.zynergylabs.forager.brouter.RouteOutcome
import com.zynergylabs.forager.brouter.RouteRequest
import com.zynergylabs.forager.brouter.route
import okio.FileSystem
import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

/**
 * Step 10, part 2 (RECORD -137, decision 4): stratum F0, Forager's own two routes, through the port's public `route()`
 * (so `MsunLibm`), held to F0's two rows in brouter/src/jvmTest/resources/e3/expected-1b.tsv, copied below. The ends are
 * Forager's own (RoutingEngineF0Test's CASES; E1 allows them); `hiking-mountain`, memory class 64 (RoutingContext's
 * default, as E3Case's), no time limit, Forager's params.
 *
 * Five of the row's six fields are held: kind, nodes, distance, nodeDigest, heightsDigest. The sixth, cost, is not in
 * `RouteOutcome.Found` (the public call returns the track's positions, distance and heights only), so it cannot be read
 * through `route()`; the row's cost is kept below for the record and not asserted. The tupleDigest needs the cost too.
 *
 * Paths come from [testSetting]: the tiles folder as [PinnedTileReadTest]'s; the profiles folder (`profiles2/`, which holds
 * `lookups.dat`) is `<oracle dir>/profiles2`, the oracle dir being jvmTest's `brouter.oracle.dir` on the JVM and
 * `BROUTER_ORACLE_DIR` in the simulator's process. Missing settings or files fail with their names.
 */
class F0RouteCommonTest {
    @Test
    fun f0CasesThroughRouteMatchExpected1b() {
        val tiles = setting("brouter.tiles.dir", "BROUTER_TILES_DIR").toPath() / "f0"
        val profile = setting("brouter.oracle.dir", "BROUTER_ORACLE_DIR").toPath() / "profiles2" / "hiking-mountain.brf"
        for (p in listOf(tiles / "W75_N40.rd5", profile, profile.parent!! / "lookups.dat")) {
            if (!FileSystem.SYSTEM.exists(p)) fail("$p does not exist or cannot be read in this process")
        }
        val mismatches = mutableListOf<String>()
        for (c in CASES) {
            val request = RouteRequest(
                c.startLon, c.startLat, c.endLon, c.endLat, tiles, profile,
                mapOf("alternativeidx" to "0", "profile:use_dynamic_range" to "0"), 0L,
            )
            val got = when (val o = route(request, RouteCancel())) {
                is RouteOutcome.Found -> listOf(
                    "route", o.iLat.size.toString(), o.distance.toString(),
                    fnv1a(o.iLon, o.iLat), fnv1aShorts(o.sElev),
                )
                is RouteOutcome.Failed -> listOf(o.message, "0", "-1", fnv1a(IntArray(0), IntArray(0)), fnv1aShorts(ShortArray(0)))
            }
            val want = listOf(c.kind, c.nodes.toString(), c.distance.toString(), c.nodeDigest, c.heightsDigest)
            println("step10 F0 ${c.id}: kind|nodes|distance|nodeDigest|heightsDigest port ${got.joinToString("|")}; expected ${want.joinToString("|")} (cost ${c.cost}, not readable through route())")
            FIELDS.forEachIndexed { i, f -> if (got[i] != want[i]) mismatches += "${c.id} $f: port ${got[i]}, expected ${want[i]}" }
        }
        assertEquals(emptyList(), mismatches, "F0 through route() against expected-1b.tsv")
    }

    private class Case(
        val id: String, val startLat: Double, val startLon: Double, val endLat: Double, val endLon: Double,
        val kind: String, val nodes: Int, val cost: Int, val distance: Int, val nodeDigest: String, val heightsDigest: String,
    )

    private companion object {
        val FIELDS = listOf("kind", "nodes", "distance", "nodeDigest", "heightsDigest")

        // Ends: RoutingEngineF0Test.CASES. Values: expected-1b.tsv's F0 rows (kind, nodes, cost, distance, nodeDigest,
        // heightsDigest), written on the no-intrinsic oracle (RECORD -88).
        val CASES = listOf(
            Case("F0-inside-across-the-degree-line", 41.2742564, -73.9978932, 41.3036912, -74.0159457,
                "route", 226, 5302, 5047, "9ca48d05a90f4dcd", "afc27854ee968af5"),
            Case("F0-inside-long", 41.2772811, -74.0422743, 41.292043, -73.996678,
                "route", 306, 7405, 6713, "7d242f8dd2c428b4", "4f6849ea59b5f775"),
        )

        fun setting(jvmProperty: String, iosVariable: String): String =
            testSetting(jvmProperty, iosVariable) ?: fail("${testSettingSource(jvmProperty, iosVariable)} is not set")

        /** E3Outcome.fnv1a: 64-bit FNV-1a over each (lon, lat), each int's four bytes low first; unpadded hex. */
        fun fnv1a(lon: IntArray, lat: IntArray): String {
            var h = -0x340d631b7bdddcdbL
            fun mix(v: Int) {
                for (shift in 0 until 32 step 8) {
                    h = h xor ((v ushr shift) and 0xff).toLong()
                    h *= 0x100000001b3L
                }
            }
            for (i in lon.indices) {
                mix(lon[i])
                mix(lat[i])
            }
            return h.toULong().toString(16)
        }

        /** E3Outcome.fnv1aShorts: each short's two bytes low first. */
        fun fnv1aShorts(heights: ShortArray): String {
            var h = -0x340d631b7bdddcdbL
            for (s in heights) {
                val v = s.toInt()
                for (shift in 0 until 16 step 8) {
                    h = h xor ((v ushr shift) and 0xff).toLong()
                    h *= 0x100000001b3L
                }
            }
            return h.toULong().toString(16)
        }
    }
}
