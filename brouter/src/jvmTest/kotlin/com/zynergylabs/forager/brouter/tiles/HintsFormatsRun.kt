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
import com.zynergylabs.forager.brouter.oracle.e3.strata.E3Targeted
import com.zynergylabs.forager.brouter.router.OsmTrack
import com.zynergylabs.forager.brouter.router.PathEngines
import com.zynergylabs.forager.brouter.router.RoutingEngineF0Test
import com.zynergylabs.forager.brouter.util.LibmSeam
import okio.Path.Companion.toOkioPath
import java.io.File
import java.nio.file.Files
import kotlin.test.assertEquals

/**
 * Step 10, session 57 (RECORD -150, decision 1): the turn hints and the four formats ([FormatOutputs]) of every route
 * group the JVM tests build, port against oracle. The groups are built as `RoutingEngineProductionRecordTest` builds them
 * (FK left out: its cases are edited tiles, not routes for the formats): F0, F0h, NT, LB, CR (E's crossing profile in a
 * scratch copy of `profiles2/`), DL-lat, DL-lon, MP64, ML5, PM5, and R's routed cases (its 20 StdModel profiles, the
 * cases whose expected-1b row has nodes; R_FIRST, when not null, keeps only the first that many of each profile).
 *
 * Each case runs once on each side with the hints on: `turnInstructionMode` is [FormatOutputs.modeFor] the case's place
 * in its group (2 to 9 in turn), written after `setParams` on both sides. The port runs as [EngineRun.runTrack] runs it,
 * on the library it is given: `OracleLibm` for a logic run, `MsunLibm` for a production run. Every output is compared by
 * length and 64-bit FNV-1a digest; for the hints also with the angle left out, so a production record can tell a hint
 * that differs only in its angle. The texts never leave this object: only counts, ids, digests and where a pair first
 * differs ([FormatOutputs.diff]: a hint's index and field name, or a character index and two character classes) are
 * returned (E1).
 */
internal object HintsFormatsRun {
    val R_FIRST: Int? = null

    class Group(val name: String, val cases: List<E3Case>, val base: File)

    /** One group's counts: cases, routes (both sides found a track), and per output how many are equal. */
    class Tally(val group: String) {
        var cases = 0
        var routes = 0
        var routeEqual = 0
        val equal = IntArray(5)
        var hintsBeyondAngle = 0
        val problems = ArrayList<String>()
        var seconds = 0.0

        fun line(prefix: String): String =
            "$prefix $group: cases $cases, routes $routes, six fields equal $routeEqual; " +
                FormatOutputs.OUTPUTS.indices.joinToString("; ") { "${FormatOutputs.OUTPUTS[it]} equal ${equal[it]} differ ${cases - equal[it]}" } +
                "; hints differing beyond the angle $hintsBeyondAngle; %.1f s".format(seconds)
    }

    private val expected by lazy { E3Expected.read(javaClass.getResource(E3Expected.RESOURCE)!!.readText()).rows }

    private fun rowsOf(stratum: String) = expected.filter { it.stratum == stratum }.associateBy { it.id }

    /** Runs [block] on each group in turn; CR's scratch profiles folder lives only while its block runs. */
    fun forEachGroup(block: (Group) -> Unit) {
        val o = OracleCalls.oracleDir
        block(Group("F0", RoutingEngineF0Test.CASES, o))
        block(Group("F0h", f0h(), o))
        block(Group("NT", nt(), o))
        LBGenerator(TestTiles.segments, 20261207L).use { block(Group("LB", select(rowsOf("LB"), it.draws("LB", E3Targeted.PROFILE)), o)) }
        val base = Files.createTempDirectory("s57-cr").toFile()
        try {
            File(o, "profiles2").copyRecursively(File(base, "profiles2"))
            File(base, "profiles2/${E3Profiles.CROSSING}.brf").writeText(E3Profiles.crossing())
            val gen = RGenerator(TestTiles.segments, 20261206L)
            val cr = try {
                select(rowsOf("CR"), gen.draws("CR", E3Profiles.CROSSING, 64, TestTiles.segments))
            } finally {
                gen.close()
            }
            block(Group("CR", cr, base))
        } finally {
            base.deleteRecursively()
        }
        block(Group("DL-lat", dl(DLGenerator.Line.LAT40, E3DLTest.SEED_LAT), o))
        block(Group("DL-lon", dl(DLGenerator.Line.LON70, E3DLTest.SEED_LON), o))
        block(Group("MP64", memory("MP64", "MP64", 64, E3MemoryTest.SEED_MP64), o))
        block(Group("ML5", memory("ML5", "M5", 5, E3MemoryTest.SEED_M5), o))
        block(Group("PM5", memory("PM5", "M5", 5, E3MemoryTest.SEED_M5), o))
        block(Group("R", routedR(), o))
    }

    /** F0h's 8 cases on the heighted square (the simulator's constants are made from them, with F0's and NT's). */
    fun f0h(): List<E3Case> = E3F0hTest.ENDS.map { (name, e) -> E3Case("F0h-$name", TestTiles.f0h, e[0], e[1], e[2], e[3], E3F0hTest.PROFILE) }

    /** NT's 59 cases, in draw order. */
    fun nt(): List<E3Case> = NTGenerator(TestTiles.segments, 20261205L).use { select(rowsOf("NT"), it.draws("NT", E3Targeted.PROFILE)) }

    private fun routedR(): List<E3Case> {
        val rows = rowsOf("R")
        val seeds = linkedMapOf(E3RTest.PROFILE to E3RTest.SEED) + E3RProfilesTest.SEEDS
        assertEquals(26, seeds.size, "R's profiles (E4)")
        val cases = ArrayList<E3Case>()
        var all = 0
        for ((profile, seed) in seeds) {
            val model = File(OracleCalls.oracleDir, "profiles2/$profile.brf").readLines().firstOrNull { it.startsWith("---model:") }
            if (model != null) continue
            val gen = RGenerator(TestTiles.segments, seed)
            val generated = try {
                gen.generate("R", profile, E3R.N, 20 * E3R.N, TestTiles.segments).cases
            } finally {
                gen.close()
            }
            all += generated.size
            val routed = generated.filter { (rows[it.id]?.outcome?.nodes ?: 0) > 0 }
            cases += if (R_FIRST == null) routed else routed.take(R_FIRST)
        }
        assertEquals(1_180, all, "R's StdModel cases")
        return cases
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
        assertEquals(rows.size, cases.size, "cases against the stratum's rows")
        return cases
    }

    /** One side's run: its six-field outcome, the five texts, and the hints text without angles. */
    class Side(val outcome: EngineRun.Outcome, val routed: Boolean, val texts: List<String>, val hintsNoAngle: String)

    fun port(c: E3Case, base: File, mode: Int, libm: LibmSeam): Side {
        val profile = File(base, "profiles2/${c.profile}.brf")
        val p = EngineRun.runTrack(ends(c), profile.toOkioPath(), c.segments.toOkioPath(), c.memoryclass, 0L, mode, libm)
        val t = p.track
        val rc = p.rc
        if (t == null || rc == null) return Side(p.outcome, false, List(5) { "no track" }, "no track")
        return Side(p.outcome, t.nodes.isNotEmpty(), FormatOutputs.texts(t, rc), FormatOutputs.res { FormatOutputs.hintsText(t, false) })
    }

    fun oracle(c: E3Case, base: File, mode: Int): Side {
        val collector = btools.router.RoutingParamCollector()
        val waypoints = collector.getWayPointList(ends(c))
        val rc = btools.router.RoutingContext()
        val params = hashMapOf(
            "profile" to File(base, "profiles2/${c.profile}.brf").absolutePath,
            "alternativeidx" to "0",
            "profile:use_dynamic_range" to "0",
        )
        collector.setParams(rc, waypoints, params)
        rc.memoryclass = c.memoryclass
        rc.turnInstructionMode = mode
        val engine = btools.router.RoutingEngine(null, null, c.segments, waypoints, rc, btools.router.RoutingEngine.BROUTER_ENGINEMODE_ROUTING)
        engine.quite = true
        engine.doRun(0L)
        val t = engine.foundTrack
        val n = t?.nodes ?: emptyList()
        val outcome = EngineRun.Outcome(
            EngineRun.kindOf(engine.errorMessage), n.size, t?.cost ?: -1, t?.distance ?: -1,
            EngineRun.fnv1a(IntArray(n.size) { n[it].iLon }, IntArray(n.size) { n[it].iLat }),
            EngineRun.fnv1aShorts(ShortArray(n.size) { n[it].sElev }),
        )
        if (t == null) return Side(outcome, false, List(5) { "no track" }, "no track")
        return Side(
            outcome, n.isNotEmpty(),
            listOf(
                FormatOutputs.res { oracleHints(t, true) },
                FormatOutputs.res { btools.router.FormatGpx(rc).format(t) },
                FormatOutputs.res { btools.router.FormatJson(rc).format(t) },
                FormatOutputs.res { btools.router.FormatKml(rc).format(t) },
                FormatOutputs.res { btools.router.FormatCsv(rc).format(t) },
            ),
            FormatOutputs.res { oracleHints(t, false) },
        )
    }

    private fun ends(c: E3Case) = "${c.startLon},${c.startLat}|${c.endLon},${c.endLat}"

    /** [FormatOutputs.hintsText] over the oracle's hints (package-private fields, read by reflection). */
    private fun oracleHints(t: btools.router.OsmTrack, withAngle: Boolean): String {
        val v = t.voiceHints ?: return "none\n"
        val list = PathEngines.field(v, "list") as List<*>
        val sb = StringBuilder()
        sb.append(PathEngines.field(v, "turnInstructionMode")).append(';').append(list.size).append('\n')
        for (h in list) {
            h as btools.router.VoiceHint
            fun f(name: String) = PathEngines.field(h, name)
            sb.append(f("indexInTrack")).append(';').append(f("cmd")).append(';').append(f("roundaboutExit")).append(';')
                .append(f("ilon")).append(';').append(f("ilat")).append(';').append((f("selev") as Short).toInt()).append(';')
                .append((f("distanceToNext") as Double).toRawBits()).append(';')
                .append(if (withAngle) (f("angle") as Float).toRawBits().toString() else "").append(';')
                .append(h.formatGeometry()).append('\n')
        }
        return sb.toString()
    }

    /**
     * [g]'s cases, port on [libm] against the oracle; [onCase] sees each case's id, mode and port side (for the stored
     * constants). The port's `OsmTrack.version` is the oracle's while it runs (RECORD -118), and restored after.
     */
    fun compare(g: Group, libm: LibmSeam, onCase: ((E3Case, Int, Side) -> Unit)? = null): Tally {
        val tally = Tally(g.name)
        val t0 = System.nanoTime()
        val version = OsmTrack.version
        OsmTrack.version = btools.router.OsmTrack.version
        try {
            for ((i, c) in g.cases.withIndex()) {
                val mode = FormatOutputs.modeFor(i)
                val p = port(c, g.base, mode, libm)
                val o = oracle(c, g.base, mode)
                onCase?.invoke(c, mode, p)
                tally.cases++
                if (p.routed && o.routed) tally.routes++
                if (p.outcome.tupleDigest == o.outcome.tupleDigest) tally.routeEqual++
                else tally.problems += "${g.name} ${c.id} route: port ${p.outcome.tupleDigest} oracle ${o.outcome.tupleDigest}"
                for (k in 0 until 5) {
                    val a = p.texts[k]
                    val b = o.texts[k]
                    if (a.length == b.length && EngineRun.fnv1aText(a) == EngineRun.fnv1aText(b)) {
                        tally.equal[k]++
                        continue
                    }
                    tally.problems += "${g.name} ${c.id} mode $mode ${FormatOutputs.OUTPUTS[k]}: port ${a.length}/${EngineRun.fnv1aText(a)} " +
                        "oracle ${b.length}/${EngineRun.fnv1aText(b)}; first difference ${FormatOutputs.diff(k, a, b)}"
                    if (k == 0 && (p.hintsNoAngle.length != o.hintsNoAngle.length ||
                            EngineRun.fnv1aText(p.hintsNoAngle) != EngineRun.fnv1aText(o.hintsNoAngle))
                    ) {
                        tally.hintsBeyondAngle++
                        tally.problems += "${g.name} ${c.id} hints beyond the angle: first difference ${FormatOutputs.hintsDiff(p.hintsNoAngle, o.hintsNoAngle)}"
                    }
                }
            }
        } finally {
            OsmTrack.version = version
        }
        tally.seconds = (System.nanoTime() - t0) / 1e9
        return tally
    }
}
