package com.zynergylabs.forager.brouter.mapaccess

import com.zynergylabs.forager.brouter.oracle.OracleCalls
import com.zynergylabs.forager.brouter.oracle.e3.RGenerator
import com.zynergylabs.forager.brouter.oracle.e3.strata.E3RProfilesTest
import com.zynergylabs.forager.brouter.oracle.e3.strata.E3RTest
import com.zynergylabs.forager.brouter.oracle.e3.strata.E3R
import com.zynergylabs.forager.brouter.tiles.TestTiles
import java.io.File
import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Step 5c (Phase 1 report, item I; RECORD -66): the matched waypoint (crosspoint, node pair, radius, direction, and the
 * whole `wayNearest` list) for every generated end of stratum R, on the port and on the oracle. R's ends are
 * [RGenerator]'s, with each profile's own seed (E3RTest, E3RProfilesTest), 59 cases a profile, two ends a case. NT's ends
 * wait until NT is built. Only counts and a digest are printed; a failure names case ids and field names, no coordinate.
 */
class WaypointMatchTest {
    private class Tally(var ends: Int = 0, var cases: Int = 0, var unmatched: Int = 0, var reverse: Int = 0, var ties: Int = 0, var differing: Int = 0)

    @Test
    fun everyEndOfStratumRMatchesTheOraclesWaypoint() {
        val seeds = linkedMapOf(E3RTest.PROFILE to E3RTest.SEED) + E3RProfilesTest.SEEDS
        assertEquals(26, seeds.size, "R's profiles are E4's 26")
        val t = Tally()
        val firstDiffs = ArrayList<String>()
        val md = MessageDigest.getInstance("SHA-256")
        for ((profile, seed) in seeds) {
            val gen = RGenerator(TestTiles.segments, seed)
            val cases = try {
                gen.generate("R", profile, E3R.N, 20 * E3R.N, TestTiles.segments).cases
            } finally {
                gen.close()
            }
            assertEquals(E3R.N, cases.size, "R-$profile is short")
            val engines = WaypointMatchEngines(File(OracleCalls.oracleDir, "profiles2/$profile.brf"), TestTiles.segments)
            for (c in cases) {
                val o = engines.run(c)
                t.cases++
                t.reverse += o.oracleChoseReverse
                t.ties += o.oracleTies
                for (i in o.oracle.indices) {
                    t.ends++
                    if (o.oracle[i].contains("cross=null")) t.unmatched++
                    md.update((o.oracle[i] + "\n").toByteArray())
                    if (o.oracle[i] != o.port.getOrNull(i)) {
                        t.differing++
                        if (firstDiffs.size < 10) {
                            firstDiffs += "${c.id} end $i: " + WaypointMatchEngines.differingFields(o.oracle[i], o.port.getOrNull(i) ?: "")
                        }
                    }
                }
            }
        }
        val digest = md.digest().joinToString("") { "%02x".format(it) }
        println("5c R: ${t.cases} cases, ${t.ends} ends, unmatched ${t.unmatched}, oracle chose the reverse entry at ${t.reverse}, " +
            "radius ties at the head ${t.ties}, ends differing ${t.differing}; oracle transcript SHA-256 $digest")
        assertEquals(2 * 26 * E3R.N, t.ends, "not every end of R was compared")
        assertEquals(0, t.differing, "ends differing from the oracle: ${t.differing}; first: $firstDiffs")
        // The sample must hold the cases the comparator's second line decides, or its revert could not fail here.
        assertTrue(t.reverse > 0, "no end chose the reverse entry, so WaypointMatcherImpl.java:71 decides nothing here")
    }
}
