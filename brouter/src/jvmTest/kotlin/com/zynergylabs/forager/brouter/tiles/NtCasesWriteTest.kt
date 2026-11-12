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
import com.zynergylabs.forager.brouter.oracle.e3.strata.E3F0hTest
import com.zynergylabs.forager.brouter.oracle.e3.strata.E3MemoryTest
import com.zynergylabs.forager.brouter.oracle.e3.strata.E3R
import com.zynergylabs.forager.brouter.oracle.e3.strata.E3RProfilesTest
import com.zynergylabs.forager.brouter.oracle.e3.strata.E3RTest
import com.zynergylabs.forager.brouter.router.RoutingEngineFKTest
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
 *
 * Session 48, F0h and FK 1 to 8 ([writeCases]): F0h's eight cases as `RoutingEngineF0hTest` builds them ([E3F0hTest.ENDS],
 * on [TestTiles.f0h]), written here at run time like the others rather than committed in common code; FK's as
 * `RoutingEngineFKTest` builds them, one file per kind ([fkFile]). Kind 6's folder is [TestTiles.segments], where its
 * tile is missing; kinds 7 and 8 each case in its own folder under [FK_TILES]`/` beside the NT file, holding only its cut
 * or flipped copy of E170_S50 (`RoutingEngineFKTest.fk7Cases`, `fk8Cases`). A case whose folder is not the segments
 * folder carries it as an eighth field. Edited tiles are written there only, never uploaded.
 *
 * Session 49, the memory groups ([MP64_FILE], [ML5_FILE], [PM5_FILE]): `RoutingEngineMemoryTest`'s draws, [RGenerator] on
 * [TestTiles.segments] with [E3MemoryTest]'s seeds, `only` [E3MemoryTest.TILE], 20 km times 5^u, [E3Targeted.PROFILE]:
 * MP64 labelled "MP64" at memory class 64; ML5 and PM5 each from its own generator over the one "M5" sequence at memory
 * class 5, as that test draws them. The memory class is the line's third field.
 *
 * Session 51, R's fallback ([R_FILE]; RECORD -142, decision 3; E5): the first [RExpected.PER_PROFILE] cases of each of R's
 * 20 StdModel profiles ([RExpected.PROFILES]), each built as `RoutingEngineProductionRecordTest.recordR` builds R: one
 * [RGenerator] per profile on [TestTiles.segments] with its seed ([E3RTest.SEED], [E3RProfilesTest.SEEDS]), `generate`
 * within `20 * E3R.N` candidates. `generate` numbers its cases in order, so the first 12 are R-<profile>-001 to -012 whether
 * it is asked for 12 or 59. The six car profiles are not built (see [RExpected]).
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
        val f0hRows = expected().filter { it.stratum == "F0h" }.map { it.id }.toSet()
        val f0h = E3F0hTest.ENDS.map { (name, e) -> E3Case("F0h-$name", TestTiles.f0h, e[0], e[1], e[2], e[3], E3F0hTest.PROFILE) }
        writeCases("F0h", f0h, f0hRows, File(dir, F0H_FILE))
        val fk = RoutingEngineFKTest()
        val fkTiles = File(dir, FK_TILES)
        fkTiles.deleteRecursively()
        fkTiles.mkdirs()
        val e = fk.editedEnds()
        val tile = File(TestTiles.segments, "E170_S50.rd5")
        val kinds = listOf(
            fk.firstTen(1), fk.firstTen(2), fk.byRow("FK3", 3), fk.byRow("FK4", 4), fk.kind5(), fk.firstTen(6),
            fk.fk7Cases(fkTiles, e, tile), fk.fk8Cases(fkTiles, e, tile),
        )
        kinds.forEachIndexed { i, cases ->
            val label = "FK${i + 1}"
            writeCases(label, cases, fk.rows(label).keys, File(dir, fkFile(i + 1)))
        }
        for ((stratum, label, memoryclass, seed, count, name) in listOf(
            Memory("MP64", "MP64", 64, E3MemoryTest.SEED_MP64, 59, MP64_FILE),
            Memory("ML5", "M5", 5, E3MemoryTest.SEED_M5, 59, ML5_FILE),
            Memory("PM5", "M5", 5, E3MemoryTest.SEED_M5, 55, PM5_FILE),
        )) {
            val gen = RGenerator(TestTiles.segments, seed, only = E3MemoryTest.TILE, minMetres = 20_000.0, ratio = 5.0)
            try {
                write(stratum, gen.draws(label, E3Targeted.PROFILE, memoryclass, TestTiles.segments), File(dir, name), count)
            } finally {
                gen.close()
            }
        }
        val seeds = linkedMapOf(E3RTest.PROFILE to E3RTest.SEED) + E3RProfilesTest.SEEDS
        val rCases = RExpected.PROFILES.flatMap { profile ->
            val gen = RGenerator(TestTiles.segments, seeds[profile] ?: fail("$profile: no seed in R's profiles"))
            try {
                gen.generate("R", profile, RExpected.PER_PROFILE, 20 * E3R.N, TestTiles.segments).cases
            } finally {
                gen.close()
            }
        }
        writeCases("R", rCases, RExpected.ROWS.map { it.id }.toSet(), File(dir, R_FILE))
    }

    private fun expected() = E3Expected.read(javaClass.getResource(E3Expected.RESOURCE)!!.readText()).rows

    /** [cases] built in full (not drawn against a cap), each id one of [ids] and every id there; one line each. */
    private fun writeCases(label: String, cases: List<E3Case>, ids: Set<String>, out: File) {
        assertEquals(ids, cases.map { it.id }.toSet(), "$label's cases are expected-1b's rows")
        val lines = cases.map { c ->
            if (c.maxRunningTimeMillis != 0L || c.plan != E3Case.Plan.NONE) fail("${c.id}: a time limit or a plan; the file cannot carry it")
            val fields = mutableListOf(
                c.id, c.profile, c.memoryclass.toString(),
                c.startLon.toString(), c.startLat.toString(), c.endLon.toString(), c.endLat.toString(),
            )
            if (c.segments != TestTiles.segments) fields += c.segments.absolutePath
            fields.joinToString("\t")
        }
        out.writeText(lines.joinToString("\n", postfix = "\n"))
        val own = cases.count { it.segments != TestTiles.segments }
        println("step10 $label cases: ${cases.size} of ${ids.size} stored ids built; $own in a folder of their own; written to $out")
    }

    private data class Memory(val stratum: String, val label: String, val memoryclass: Int, val seed: Long, val count: Int, val file: String)

    private fun write(stratum: String, draws: Iterator<E3Select.Draw>, out: File, count: Int = 59) {
        val rows = expected().filter { it.stratum == stratum }.associateBy { it.id }
        assertEquals(count, rows.size, "$stratum's rows in expected-1b")
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
        const val F0H_FILE = "f0h-cases.tsv"
        const val FK_TILES = "fk-tiles"
        const val MP64_FILE = "mp64-cases.tsv"
        const val ML5_FILE = "ml5-cases.tsv"
        const val PM5_FILE = "pm5-cases.tsv"
        const val R_FILE = "r-cases.tsv"

        fun fkFile(kind: Int) = "fk$kind-cases.tsv"
        const val PROFILES = "profiles2"
    }
}
