package com.zynergylabs.forager.brouter.tiles

import com.zynergylabs.forager.brouter.oracle.OracleCalls
import com.zynergylabs.forager.brouter.oracle.e3.DLGenerator
import com.zynergylabs.forager.brouter.oracle.e3.E3Case
import com.zynergylabs.forager.brouter.oracle.e3.E3Expected
import com.zynergylabs.forager.brouter.oracle.e3.E3Profiles
import com.zynergylabs.forager.brouter.oracle.e3.E3Select
import com.zynergylabs.forager.brouter.oracle.e3.LBGenerator
import com.zynergylabs.forager.brouter.oracle.e3.NTGenerator
import com.zynergylabs.forager.brouter.oracle.e3.RGenerator
import com.zynergylabs.forager.brouter.oracle.e3.strata.E3DLTest
import com.zynergylabs.forager.brouter.oracle.e3.strata.E3Targeted
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

/**
 * Step 10, session 46 (RECORD -138, decision 2): NT's cases for the simulator, drawn at run time on this JVM and written
 * to the file [GATE] names, never committed. Gated: with [GATE] unset it prints one line and does nothing (the `jvm` job).
 * `ios-smoke` sets it to `brouter/build/step10/nt-cases.tsv`, outside `test-results/`, which is all that job uploads.
 *
 * The draws are `RoutingEngineStep9Test`'s for NT: [NTGenerator] on [TestTiles.segments] with `E3NTTest`'s seed 20261205,
 * [E3Targeted.PROFILE], in draw order, kept where a draw's id is one of NT's rows in expected-1b.tsv, within
 * [E3Targeted.CAP] draws. No oracle run. Each line: id, profile, memory class, then the four ends as this JVM's
 * `Double.toString` writes them into the waypoint text (`port`'s `"$startLon,$startLat|$endLon,$endLat"`): start lon,
 * start lat, end lon, end lat. Nothing of a case but its id is printed; ids, counts and the path only (E1). A draw that
 * names a stored id but not this file's shape (another tiles folder, a time limit, a plan) fails with its id.
 *
 * Session 47: LB and CR the same way, beside the NT file in its folder ([LB_FILE], [CR_FILE]), so `ios-smoke` needs no
 * other variable: LB is `RoutingEngineStep9Test`'s [LBGenerator], seed 20261207, [E3Targeted.PROFILE]; CR its
 * [RGenerator], seed 20261206, [E3Profiles.CROSSING] at memory class 64. CR's profile is not in the oracle's `profiles2/`,
 * so it is written as [E3Profiles.crossing] makes it into [PROFILES]`/` in the same folder, with the oracle's `lookups.dat`
 * beside it (the engine reads `lookups.dat` from the profile's folder). The profile is the oracle's pinned `trekking.brf`
 * with E4's assigns; it holds no position.
 *
 * Session 48: DL-lat and DL-lon the same way ([DL_LAT_FILE], [DL_LON_FILE]): `RoutingEngineDLTest`'s [DLGenerator] draws,
 * [E3DLTest.SEED_LAT] and [E3DLTest.SEED_LON], [E3Targeted.PROFILE], on [TestTiles.segments] (not the drop-off set).
 */
class NtCasesWriteTest {
    @Test
    fun ntCasesAreWrittenForTheSimulator() {
        val path = System.getenv(GATE)
        if (path.isNullOrEmpty()) {
            println("step10 NT cases: $GATE is unset; not written")
            return
        }
        val nt = File(path)
        val dir = nt.parentFile
        dir.mkdirs()
        NTGenerator(TestTiles.segments, 20261205L).use { write("NT", it.draws("NT", E3Targeted.PROFILE), nt) }
        LBGenerator(TestTiles.segments, 20261207L).use { write("LB", it.draws("LB", E3Targeted.PROFILE), File(dir, LB_FILE)) }
        val profiles = File(dir, PROFILES)
        profiles.mkdirs()
        File(profiles, "${E3Profiles.CROSSING}.brf").writeText(E3Profiles.crossing())
        File(OracleCalls.oracleDir, "profiles2/lookups.dat").copyTo(File(profiles, "lookups.dat"), overwrite = true)
        val cr = RGenerator(TestTiles.segments, 20261206L)
        try {
            write("CR", cr.draws("CR", E3Profiles.CROSSING, 64, TestTiles.segments), File(dir, CR_FILE))
        } finally {
            cr.close()
        }
        for ((line, seed, name) in listOf(
            Triple(DLGenerator.Line.LAT40, E3DLTest.SEED_LAT, DL_LAT_FILE),
            Triple(DLGenerator.Line.LON70, E3DLTest.SEED_LON, DL_LON_FILE),
        )) {
            val dl = DLGenerator(TestTiles.segments, line, seed)
            try {
                write(line.stratum, dl.draws(E3Targeted.PROFILE, TestTiles.segments), File(dir, name))
            } finally {
                dl.close()
            }
        }
    }

    private fun write(stratum: String, draws: Iterator<E3Select.Draw>, out: File) {
        val rows = E3Expected.read(javaClass.getResource(E3Expected.RESOURCE)!!.readText()).rows
            .filter { it.stratum == stratum }.associateBy { it.id }
        assertEquals(59, rows.size, "$stratum's rows in expected-1b")
        val lines = ArrayList<String>()
        val seen = HashSet<String>()
        var n = 0
        while (seen.size < rows.size && n < E3Targeted.CAP) {
            n++
            val c = draws.next().case ?: continue
            if (c.id !in rows) continue
            seen += c.id
            if (c.segments != TestTiles.segments || c.maxRunningTimeMillis != 0L || c.plan != E3Case.Plan.NONE) {
                fail("${c.id}: not a plain case on the segments folder; the file cannot carry it")
            }
            lines += listOf(
                c.id, c.profile, c.memoryclass.toString(),
                c.startLon.toString(), c.startLat.toString(), c.endLon.toString(), c.endLat.toString(),
            ).joinToString("\t")
        }
        out.writeText(lines.joinToString("\n", postfix = "\n"))
        val missing = rows.keys - seen
        println("step10 $stratum cases: ${seen.size} of ${rows.size} stored ids drawn within $n draws; written to $out; missing ${missing.sorted()}")
    }

    companion object {
        const val GATE = "BROUTER_STEP10_NT_CASES"
        const val LB_FILE = "lb-cases.tsv"
        const val CR_FILE = "cr-cases.tsv"
        const val DL_LAT_FILE = "dl-lat-cases.tsv"
        const val DL_LON_FILE = "dl-lon-cases.tsv"
        const val PROFILES = "profiles2"
    }
}
