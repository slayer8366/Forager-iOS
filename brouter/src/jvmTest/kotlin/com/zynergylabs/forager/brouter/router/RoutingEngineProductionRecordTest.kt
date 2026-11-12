package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.oracle.OracleCalls
import com.zynergylabs.forager.brouter.oracle.e3.DLGenerator
import com.zynergylabs.forager.brouter.oracle.e3.E3Case
import com.zynergylabs.forager.brouter.oracle.e3.E3CaseRun
import com.zynergylabs.forager.brouter.oracle.e3.E3Expected
import com.zynergylabs.forager.brouter.oracle.e3.E3Outcome
import com.zynergylabs.forager.brouter.oracle.e3.E3Profiles
import com.zynergylabs.forager.brouter.oracle.e3.E3Select
import com.zynergylabs.forager.brouter.oracle.e3.FKGenerator
import com.zynergylabs.forager.brouter.oracle.e3.LBGenerator
import com.zynergylabs.forager.brouter.oracle.e3.NTGenerator
import com.zynergylabs.forager.brouter.oracle.e3.RGenerator
import com.zynergylabs.forager.brouter.oracle.e3.strata.E3DLTest
import com.zynergylabs.forager.brouter.oracle.e3.strata.E3F0hTest
import com.zynergylabs.forager.brouter.oracle.e3.strata.E3MemoryTest
import com.zynergylabs.forager.brouter.oracle.e3.strata.E3R
import com.zynergylabs.forager.brouter.oracle.e3.strata.E3RProfilesTest
import com.zynergylabs.forager.brouter.oracle.e3.strata.E3RTest
import com.zynergylabs.forager.brouter.oracle.e3.strata.E3Targeted
import com.zynergylabs.forager.brouter.tiles.TestTiles
import com.zynergylabs.forager.brouter.util.MsunLibm
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Step 9's production run (RECORD -24, -135): the port **as it ships**, `cos` and `atan2` from bionic's FreeBSD msun
 * ([MsunLibm]), against the one oracle there is, run in this JVM with HotSpot's libm intrinsic off (fdlibm). msun's table
 * differs from that oracle's at 13 rows (`CheapProductionRecordTest`), so by -24 any difference here comes from those
 * rows. The owner's chosen option asks the tests to "separately record whether the 13 rows ever change a route": this
 * test records it, per group; it does not judge it.
 *
 * **Gate:** runs only when the environment variable `BROUTER_PRODUCTION_RECORD` is set to a non-empty value (-135: an
 * environment variable read here, no build change). Unset, it does nothing but print one line saying so, so the default
 * `jvmTest` and CI are unchanged in length.
 *
 * **Cases:** the logic tests' own, built the same way: F0 ([RoutingEngineF0Test.CASES]); R on its 20 StdModel profiles,
 * 59 each (1,180; the six `---model:` profiles are refusals in [RoutingEngineRTest] and are left out); DL-lat, DL-lon,
 * MP64, ML5, PM5, NT, LB, CR selected from their generators by `expected-1b.tsv`'s row ids within [E3Targeted.CAP] draws,
 * as [RoutingEngineDLTest], [RoutingEngineMemoryTest] and [RoutingEngineStep9Test] select them; FK 1 to 8 from
 * [RoutingEngineFKTest]'s builders (FK7's and FK8's case lists repeated from its two tests, which build them inline);
 * F0h ([E3F0hTest.ENDS]). FK 9 to 11 are timing cases and are left out.
 *
 * **Per case:** the oracle in this JVM ([E3CaseRun]), the port through [RoutingEngine] as [RoutingEngineF0Test.port] runs
 * it but on [MsunLibm]; compared on E3Outcome's six fields. The kind is compared exactly, except FK7's, which is
 * compared by family (numbers read as `*`) as [RoutingEngineFKTest] compares it; FK7's exact-kind differences are counted
 * beside. Whether the oracle in this JVM matches its `expected-1b.tsv` row is counted too (the logic tests assert it).
 *
 * **Recorded, not asserted.** It asserts only that every case produced an outcome on both sides and that each group's
 * counts add up to its rows. It prints, per group: cases, equal on all six fields, and of the rest how many differ in
 * kind, nodes, cost, distance, the node digest and the heights digest, and how the differing cases split: cost only; the
 * same nodes (node digest equal) with a different distance; a different route (node digest differs); other. Counts only
 * (E1).
 */
class RoutingEngineProductionRecordTest {
    private val expected = E3Expected.read(javaClass.getResource(E3Expected.RESOURCE)!!.readText()).rows
    private val lines = ArrayList<String>()

    @Test
    fun productionRunIsRecordedAgainstTheOracle() {
        val gate = System.getenv(GATE)
        if (gate.isNullOrEmpty()) {
            println("production record: $GATE is unset; not run (RECORD -135)")
            return
        }
        val t0 = System.nanoTime()
        record("F0", rowsOf("F0"), RoutingEngineF0Test.CASES)
        recordR()
        record("DL-lat", rowsOf("DL-lat"), dl(DLGenerator.Line.LAT40, E3DLTest.SEED_LAT))
        record("DL-lon", rowsOf("DL-lon"), dl(DLGenerator.Line.LON70, E3DLTest.SEED_LON))
        record("MP64", rowsOf("MP64"), memory("MP64", "MP64", 64, E3MemoryTest.SEED_MP64))
        record("ML5", rowsOf("ML5"), memory("ML5", "M5", 5, E3MemoryTest.SEED_M5))
        record("PM5", rowsOf("PM5"), memory("PM5", "M5", 5, E3MemoryTest.SEED_M5))
        NTGenerator(TestTiles.segments, 20261205L).use { gen ->
            record("NT", rowsOf("NT"), select(rowsOf("NT"), gen.draws("NT", E3Targeted.PROFILE)))
        }
        LBGenerator(TestTiles.segments, 20261207L).use { gen ->
            record("LB", rowsOf("LB"), select(rowsOf("LB"), gen.draws("LB", E3Targeted.PROFILE)))
        }
        recordCR()
        val fk = RoutingEngineFKTest()
        record("FK1", fk.rows("FK1"), fk.firstTen(1))
        record("FK2", fk.rows("FK2"), fk.firstTen(2))
        record("FK3", fk.rows("FK3"), fk.byRow("FK3", 3))
        record("FK4", fk.rows("FK4"), fk.byRow("FK4", 4))
        record("FK5", fk.rows("FK5"), fk.kind5())
        record("FK6", fk.rows("FK6"), fk.firstTen(6))
        fk.edited { tiles, e, tile -> // as RoutingEngineFKTest.fk7RoutesThroughThePortAsThroughTheOracle
            val end = FKGenerator.dataEnd(tile)
            val size = tile.length()
            val cuts = listOf(200L, 201L, 1_000L, end / 4, end / 2, 3 * end / 4, end - 1, end + 1, end + 56, end + 111)
            val cases = cuts.mapIndexed { i, len -> fk.case(tiles, "FK7-%02d".format(i + 1), e) { FKGenerator.cut(tile, len, it) } } +
                fk.case(tiles, "FK7-x1", e) { FKGenerator.cut(tile, end, it) } +
                fk.case(tiles, "FK7-x2", e) { FKGenerator.cut(tile, size - 1, it) } +
                fk.case(tiles, "FK7-c0", e) { it.mkdirs(); tile.copyTo(File(it, tile.name)) }
            record("FK7", fk.rows("FK7"), cases, family = { RoutingEngineFKTest.familyOf("FK7", it) })
        }
        fk.edited { tiles, e, tile -> // as RoutingEngineFKTest.fk8RoutesThroughThePortAsThroughTheOracle
            val cases = (0 until RoutingEngineFKTest.N).map { 8 + 20 * it }.mapIndexed { i, at ->
                fk.case(tiles, "FK8-%02d".format(i + 1), e) { FKGenerator.flip(tile, at, it) }
            } + fk.case(tiles, "FK8-x1", e) { FKGenerator.flip(tile, 203, it) }
            record("FK8", fk.rows("FK8"), cases)
        }
        val f0hCases = E3F0hTest.ENDS.map { (name, e) -> E3Case("F0h-$name", TestTiles.f0h, e[0], e[1], e[2], e[3], E3F0hTest.PROFILE) }
        record("F0h", rowsOf("F0h"), f0hCases)
        println("production record: group | cases | equal | differing | kind nodes cost distance nodeDigest heightsDigest | " +
            "costOnly sameNodesDistance differentRoute other | oracleOffRow | extra")
        lines.forEach { println(it) }
        println("production record: ${lines.size} groups in %.1f s".format((System.nanoTime() - t0) / 1e9))
    }

    private fun rowsOf(stratum: String) = expected.filter { it.stratum == stratum }.associateBy { it.id }

    /** R as [RoutingEngineRTest] builds it, on its 20 StdModel profiles only. */
    private fun recordR() {
        val rows = rowsOf("R")
        val seeds = linkedMapOf(E3RTest.PROFILE to E3RTest.SEED) + E3RProfilesTest.SEEDS
        assertEquals(26, seeds.size, "R's profiles (E4)")
        val cases = ArrayList<E3Case>()
        for ((profile, seed) in seeds) {
            val model = File(OracleCalls.oracleDir, "profiles2/$profile.brf").readLines()
                .firstOrNull { it.startsWith("---model:") }
            if (model != null) continue
            val gen = RGenerator(TestTiles.segments, seed)
            try {
                cases += gen.generate("R", profile, E3R.N, 20 * E3R.N, TestTiles.segments).cases
            } finally {
                gen.close()
            }
        }
        assertEquals(1_180, cases.size, "R's StdModel cases")
        record("R", rows.filterKeys { id -> cases.any { it.id == id } }, cases)
    }

    /** CR as [RoutingEngineStep9Test] runs it: E's crossing profile added to a scratch copy of `profiles2/`. */
    private fun recordCR() {
        val base = Files.createTempDirectory("s43-cr").toFile()
        try {
            File(OracleCalls.oracleDir, "profiles2").copyRecursively(File(base, "profiles2"))
            File(base, "profiles2/${E3Profiles.CROSSING}.brf").writeText(E3Profiles.crossing())
            val gen = RGenerator(TestTiles.segments, 20261206L)
            try {
                record("CR", rowsOf("CR"), select(rowsOf("CR"), gen.draws("CR", E3Profiles.CROSSING, 64, TestTiles.segments)), base)
            } finally {
                gen.close()
            }
        } finally {
            base.deleteRecursively()
        }
    }

    private fun dl(line: DLGenerator.Line, seed: Long): List<E3Case> {
        val gen = DLGenerator(TestTiles.segments, line, seed)
        try {
            return select(rowsOf(line.stratum), gen.draws(E3Targeted.PROFILE, TestTiles.segments))
        } finally {
            gen.close()
        }
    }

    private fun memory(stratum: String, label: String, memoryclass: Int, seed: Long): List<E3Case> {
        val gen = RGenerator(TestTiles.segments, seed, only = E3MemoryTest.TILE, minMetres = 20_000.0, ratio = 5.0)
        try {
            return select(rowsOf(stratum), gen.draws(label, E3Targeted.PROFILE, memoryclass, TestTiles.segments))
        } finally {
            gen.close()
        }
    }

    /** The draws whose ids are the stratum's rows, in draw order, within 1b's cap, as the logic tests select them. */
    private fun select(rows: Map<String, E3Expected.Row>, draws: Iterator<E3Select.Draw>): List<E3Case> {
        val cases = ArrayList<E3Case>()
        val seen = HashSet<String>()
        var n = 0
        while (seen.size < rows.size && n < E3Targeted.CAP) {
            n++
            val c = draws.next().case ?: continue
            if (c.id !in rows || !seen.add(c.id)) continue
            cases += c
        }
        return cases
    }

    private fun record(
        group: String,
        rows: Map<String, E3Expected.Row>,
        cases: List<E3Case>,
        base: File = OracleCalls.oracleDir,
        family: (String) -> String = { it },
    ) {
        var equal = 0
        var offRow = 0
        var exactKind = 0
        val field = IntArray(6)
        val split = IntArray(4)
        var outcomes = 0
        for (c in cases) {
            val row = rows[c.id]
            val o = E3CaseRun.run(c, base).outcome
            val p = portMsun(c, base)
            outcomes++
            when (row?.compare) {
                "exact" -> if (row.tupleDigest != E3Expected.fnv1aText(o.toField())) offRow++
                "outcome" -> if (row.outcome.kind != o.kind) offRow++
                else -> offRow++
            }
            if (o.kind != p.kind) exactKind++
            val d = booleanArrayOf(
                family(o.kind) != family(p.kind), o.nodes != p.nodes, o.cost != p.cost, o.distance != p.distance,
                o.nodeDigest != p.nodeDigest, o.heightDigest != p.heightDigest,
            )
            if (d.none { it }) {
                equal++
                continue
            }
            d.forEachIndexed { i, b -> if (b) field[i]++ }
            split[
                when {
                    d[0] -> 3
                    d[4] -> 2
                    d[3] -> 1
                    d[2] && !d[1] && !d[5] -> 0
                    else -> 3
                },
            ]++
        }
        assertEquals(cases.size, outcomes, "$group: every case produced an outcome")
        assertEquals(rows.size, cases.size, "$group: cases against the group's rows")
        assertEquals(cases.size, equal + split.sum(), "$group: equal and differing add up to the cases")
        val extra = if (group == "FK7") "exactKindDiffers=$exactKind" else "-"
        val line = "production record: $group | ${cases.size} | $equal | ${cases.size - equal} | ${field.joinToString(" ")} | " +
            "${split.joinToString(" ")} | $offRow | $extra"
        println(line)
        lines += line
    }

    companion object {
        const val GATE = "BROUTER_PRODUCTION_RECORD"

        /** [RoutingEngineF0Test.port] with the production libm: the same calls, `RoutingContext(MsunLibm)`. */
        fun portMsun(c: E3Case, base: File): E3Outcome = try {
            val collector = RoutingParamCollector()
            val waypoints = collector.getWayPointList("${c.startLon},${c.startLat}|${c.endLon},${c.endLat}")
            val rc = RoutingContext(MsunLibm)
            val params = mapOf(
                "profile" to File(base, "profiles2/${c.profile}.brf").absolutePath,
                "alternativeidx" to "0",
                "profile:use_dynamic_range" to "0",
            )
            collector.setParams(rc, waypoints, params)
            rc.memoryclass = c.memoryclass
            val engine = RoutingEngine(FileSystem.SYSTEM, c.segments.toOkioPath(), waypoints, rc, RoutingEngine.BROUTER_ENGINEMODE_ROUTING)
            engine.quite = true
            engine.doRun(c.maxRunningTimeMillis)
            val message = engine.getErrorMessage()
            val track = engine.getFoundTrack()
            val pairs = track.nodes.map { it.getILon() to it.getILat() }
            E3Outcome(
                E3Outcome.kindOf(message), pairs.size, track.cost, track.distance, E3Outcome.fnv1a(pairs),
                E3Outcome.fnv1aShorts(track.nodes.map { it.getSElev() }),
            )
        } catch (e: Exception) {
            // As RoutingEngineF0Test.port: an Exception is an outcome; an Error is not caught.
            E3Outcome("threw:" + (e.message ?: e::class.simpleName), 0, -1, -1, E3Outcome.fnv1a(emptyList()), E3Outcome.fnv1aShorts(emptyList()))
        }
    }
}
