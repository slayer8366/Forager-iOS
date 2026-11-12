package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.oracle.OracleCalls
import com.zynergylabs.forager.brouter.oracle.e3.E3Case
import com.zynergylabs.forager.brouter.oracle.e3.E3CaseRun
import com.zynergylabs.forager.brouter.oracle.e3.E3Expected
import com.zynergylabs.forager.brouter.oracle.e3.FKGenerator
import com.zynergylabs.forager.brouter.oracle.e3.strata.E3FKTest
import com.zynergylabs.forager.brouter.tiles.TestTiles
import java.io.File
import java.nio.file.Files
import java.util.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Step 9 (session 40): FK's kinds 1 to 8 through the port, 84 rows of `expected-1b.tsv` (kinds 1 to 6 ten each, kind 7
 * thirteen with FK7-x1, FK7-x2 and FK7-c0, kind 8 eleven with FK8-x1).
 *
 * The cases are built as [E3FKTest] builds them, with its seeds and caps, in draw order: kinds 1, 2 and 6 the generator's
 * first ten kept draws (`Random(SEED + kind)`); kind 3 (`SEED + 3`) and kind 4 (`SEED + 4`) the draws whose ids
 * (`FK<k>-NNNN`, the draw's number) are the kind's rows, within the cap, which 1b chose by the oracle's R1; kind 5
 * (`SEED + 5`) a start and an end drawn separately, within [E3FKTest.CAP5]; kinds 7 and 8 one pair of ends from
 * `SEED + 7`, each case in its own folder holding only its edited copy of E170_S50 (the same cuts, flips and extra cases).
 * [FKGenerator] keeps only caches between calls, so a kind's draws do not depend on the kinds drawn before it.
 *
 * What is compared, per case:
 * 1. the oracle in this JVM ([E3CaseRun]) held to its row: the whole tuple's digest for an `exact` row, the kind for an
 *    `outcome` row (FK7-x1, FK7-x2, FK8-x1);
 * 2. the kind's family, port against oracle, as 1b reads it: kind 7's message with its numbers read as `*`, every other
 *    kind exactly (`E3FKTest.familyOf`);
 * 3. the track fields, port against oracle: nodes, cost, distance, the position digest and the heights digest.
 * A port outcome that names a refusal ("unsupported", "left out", or a throw) is counted and printed by its kind, as the
 * comparison's mismatch; it is a finding to record, not to fix. Only kinds, digests and counts are printed (E1).
 */
class RoutingEngineFKTest {
    @Test
    fun fk1RoutesThroughThePortAsThroughTheOracle() = run("FK1", firstTen(1))

    @Test
    fun fk2RoutesThroughThePortAsThroughTheOracle() = run("FK2", firstTen(2))

    @Test
    fun fk3RoutesThroughThePortAsThroughTheOracle() = run("FK3", byRow("FK3", 3))

    @Test
    fun fk4RoutesThroughThePortAsThroughTheOracle() = run("FK4", byRow("FK4", 4))

    @Test
    fun fk5RoutesThroughThePortAsThroughTheOracle() = run("FK5", kind5())

    @Test
    fun fk6RoutesThroughThePortAsThroughTheOracle() = run("FK6", firstTen(6))

    @Test
    fun fk7RoutesThroughThePortAsThroughTheOracle() = edited { tiles, e, tile ->
        val end = FKGenerator.dataEnd(tile)
        val size = tile.length()
        val cuts = listOf(200L, 201L, 1_000L, end / 4, end / 2, 3 * end / 4, end - 1, end + 1, end + 56, end + 111)
        val cases = cuts.mapIndexed { i, len -> case(tiles, "FK7-%02d".format(i + 1), e) { FKGenerator.cut(tile, len, it) } } +
            case(tiles, "FK7-x1", e) { FKGenerator.cut(tile, end, it) } +
            case(tiles, "FK7-x2", e) { FKGenerator.cut(tile, size - 1, it) } +
            case(tiles, "FK7-c0", e) { it.mkdirs(); tile.copyTo(File(it, tile.name)) }
        run("FK7", cases)
    }

    @Test
    fun fk8RoutesThroughThePortAsThroughTheOracle() = edited { tiles, e, tile ->
        val cases = (0 until N).map { 8 + 20 * it }.mapIndexed { i, at ->
            case(tiles, "FK8-%02d".format(i + 1), e) { FKGenerator.flip(tile, at, it) }
        } + case(tiles, "FK8-x1", e) { FKGenerator.flip(tile, 203, it) }
        run("FK8", cases)
    }

    private fun run(label: String, cases: List<E3Case>) {
        val rows = rows(label)
        assertEquals(rows.keys, cases.map { it.id }.toSet(), "$label's cases are expected-1b's rows")
        val mismatches = ArrayList<String>()
        val portKinds = HashMap<String, Int>()
        for (c in cases) {
            val row = rows.getValue(c.id)
            val o = E3CaseRun.run(c, OracleCalls.oracleDir).outcome
            when (row.compare) {
                "exact" -> assertEquals(row.tupleDigest, E3Expected.fnv1aText(o.toField()), "${c.id}: the oracle in this JVM against expected-1b")
                "outcome" -> assertEquals(row.outcome.kind, o.kind, "${c.id}: the oracle's kind in this JVM against expected-1b")
                else -> error("${c.id}: compare '${row.compare}' is not FK 1 to 8's")
            }
            val p = RoutingEngineF0Test.port(c, OracleCalls.oracleDir)
            portKinds.merge(familyOf(label, p.kind), 1, Int::plus)
            val differ = listOfNotNull(
                "family".takeIf { familyOf(label, o.kind) != familyOf(label, p.kind) },
                "nodes".takeIf { o.nodes != p.nodes },
                "cost".takeIf { o.cost != p.cost },
                "distance".takeIf { o.distance != p.distance },
                "node digest".takeIf { o.nodeDigest != p.nodeDigest },
                "heights digest".takeIf { o.heightDigest != p.heightDigest },
            )
            if (differ.isNotEmpty()) {
                mismatches += "${c.id}: $differ (oracle ${familyOf(label, o.kind)}|${o.nodes}|${o.cost}|${o.distance}; " +
                    "port ${familyOf(label, p.kind)}|${p.nodes}|${p.cost}|${p.distance})"
            }
        }
        val kinds = rows.values.groupingBy { familyOf(label, it.outcome.kind) }.eachCount()
        println("9 $label: ${cases.size} cases; oracle kinds $kinds; port kinds $portKinds; ${mismatches.size} mismatches")
        mismatches.take(20).forEach { println("9 $label mismatch $it") }
        assertTrue(mismatches.isEmpty(), "9 $label: ${mismatches.size} mismatches; first ${mismatches.take(5)}")
    }

    private fun rows(label: String) = E3Expected.read(javaClass.getResource(E3Expected.RESOURCE)!!.readText()).rows
        .filter { it.stratum == "FK" && it.id.startsWith("$label-") }.associateBy { it.id }

    /** Kinds 1, 2 and 6: the generator's first [N] kept draws within [E3FKTest.CAP]; nothing chosen by the oracle. */
    private fun firstTen(kind: Int): List<E3Case> = generator { gen ->
        val profile = profileOf(kind)
        val rnd = Random(E3FKTest.SEED + kind)
        val cases = ArrayList<E3Case>()
        var draws = 0
        while (cases.size < N && draws < E3FKTest.CAP) { // ends: draws rises by one each pass
            draws++
            val d = gen.draw(kind, rnd, profile)
            if (d is FKGenerator.Drawn.Ends) {
                cases += E3Case("FK$kind-%02d".format(cases.size + 1), TestTiles.segments, d.e[0], d.e[1], d.e[2], d.e[3], profile)
            }
        }
        cases
    }

    /** Kinds 3 and 4: 1b's draws whose ids are the kind's rows (1b chose them by the oracle's R1), within [E3FKTest.CAP]. */
    private fun byRow(label: String, kind: Int): List<E3Case> = generator { gen ->
        val ids = rows(label).keys
        val profile = profileOf(kind)
        val rnd = Random(E3FKTest.SEED + kind)
        val cases = ArrayList<E3Case>()
        var k = 0
        while (cases.size < ids.size && k < E3FKTest.CAP) { // ends: k rises by one each pass
            k++
            val d = gen.draw(kind, rnd, profile)
            val id = "$label-%04d".format(k)
            if (d is FKGenerator.Drawn.Ends && id in ids) {
                cases += E3Case(id, TestTiles.segments, d.e[0], d.e[1], d.e[2], d.e[3], profile)
            }
        }
        cases
    }

    /** Kind 5: a usable start then an unusable end, each one draw, within [E3FKTest.CAP5]. */
    private fun kind5(): List<E3Case> = generator { gen ->
        val profile = profileOf(5)
        val rnd = Random(E3FKTest.SEED + 5)
        val cases = ArrayList<E3Case>()
        var draws = 0
        var start: DoubleArray? = null
        while (cases.size < N && draws < E3FKTest.CAP5) { // ends: draws rises by one each pass
            draws++
            val d = if (start == null) gen.usableNode(rnd, profile) else gen.unusableNode(rnd, profile)
            if (d is FKGenerator.Drawn.Ends) {
                if (start == null) {
                    start = d.e
                } else {
                    cases += E3Case("FK5-%02d".format(cases.size + 1), TestTiles.segments, start[0], start[1], d.e[0], d.e[1], profile)
                    start = null
                }
            }
        }
        cases
    }

    /** Kinds 7 and 8: one pair of ends in E170_S50 from `SEED + 7`, and a scratch folder for the edited copies. */
    private fun edited(body: (File, DoubleArray, File) -> Unit) {
        val e = generator { gen ->
            val rnd = Random(E3FKTest.SEED + 7)
            var draws = 0
            var pair: DoubleArray? = null
            while (pair == null && draws < E3FKTest.CAP) { // ends: draws rises by one each pass
                draws++
                val d = gen.local(rnd, E3FKTest.PROFILE)
                if (d is FKGenerator.Drawn.Ends) pair = d.e
            }
            pair ?: error("FK: no pair of ends for kinds 7 and 8 within ${E3FKTest.CAP} draws")
        }
        val tiles = Files.createTempDirectory("s9-fk").toFile()
        try {
            body(tiles, e, File(TestTiles.segments, "E170_S50.rd5"))
        } finally {
            tiles.deleteRecursively()
        }
    }

    private fun case(tiles: File, id: String, e: DoubleArray, make: (File) -> Unit): E3Case {
        val dir = File(tiles, id)
        make(dir)
        return E3Case(id, dir, e[0], e[1], e[2], e[3], E3FKTest.PROFILE)
    }

    private fun <T> generator(body: (FKGenerator) -> T): T {
        val gen = FKGenerator(TestTiles.segments)
        try {
            return body(gen)
        } finally {
            gen.close()
        }
    }

    private companion object {
        const val N = 10

        /** As `E3FKTest.profileOf`: kinds 3 and 4 on `trekking-noferries`, every other on `hiking-mountain`. */
        fun profileOf(kind: Int) = if (kind == 3 || kind == 4) "trekking-noferries" else E3FKTest.PROFILE

        /** As `E3FKTest.familyOf`: kind 7's message with its numbers read as `*`; every other kind exactly. */
        fun familyOf(label: String, kind: String) = if (label == "FK7") kind.replace(Regex("\\d+"), "*") else kind
    }
}
