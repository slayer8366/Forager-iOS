package com.zynergylabs.forager.brouter.oracle.e3.strata

import com.zynergylabs.forager.brouter.oracle.e3.E3Case
import com.zynergylabs.forager.brouter.oracle.e3.E3Select
import com.zynergylabs.forager.brouter.oracle.e3.E3Stability
import com.zynergylabs.forager.brouter.oracle.e3.FKGenerator
import com.zynergylabs.forager.brouter.oracle.e3.RGenerator
import com.zynergylabs.forager.brouter.tiles.TestTiles
import java.io.File
import java.util.Random
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * FK, the failure kinds (Phase 1 report, E4; RECORD -79, -80): 11 kinds of 10 cases, and three named extra cases outside the
 * 110, all through E3's six regimes ([E3Stability]). A kind's family is the oracle's R1 kind on its first case (for kind 7
 * with its numbers read as `*`), and all 10 must reach it; a kind that does not is printed as `E3 FK STOP` and fails the
 * test after everything is reported.
 *
 * - **Kinds 1, 2, 3, 5 and 6** ([FKGenerator]): the first 10 draws the generator keeps, out of at most [CAP] draws; nothing
 *   is chosen by the oracle. Profiles: `hiking-mountain`, kind 3 `trekking-noferries` (E4), kind 5 `car-fast`.
 * - **Kind 4:** island draws ([FKGenerator]), selected by the oracle's R1 ([E3Select]) for "target island detected" (-80),
 *   counted, capped at [CAP].
 * - **Kinds 7 and 8:** one pair of ends in E170_S50 ([FKGenerator.local]), each case in a folder holding only its edited copy
 *   of E170_S50: 10 cuts to lengths that the header check refuses as too short (`PhysicalFile.java:111-123`; the data end
 *   D is `fileIndex[24]`), and 10 bytes flipped (XOR 0xFF) among bytes 8 to 191, inside the 200 bytes the top index CRC
 *   covers and outside the last index entry (bytes 192 to 199, which moves D).
 * - **Extra cases (-80), compared on outcome:** FK7-x1 cut at D (compared by the exception class the logged regimes log),
 *   FK7-x2 cut to the tile's size less 1, FK8-x1 byte 203 flipped. FK7-c0, the same ends on an intact copy, is a reading
 *   beside them, not a case of FK.
 * - **Kinds 9, 10 and 11** ([E3Case.Plan]): R's generator as MP64 used it (20 km × 5^u in W75_N40, `hiking-mountain`), one
 *   sequence, draw k going to kind 9, 10 or 11 by k mod 3; a void case (its measured run misses the plan's condition) is not
 *   counted (E4). Selected by E3Select, capped at 3 × [CAP].
 *
 * The seeds are fixed here and pushed before any run.
 */
class E3FKTest {
    @Test
    fun fkReachesOneFamilyPerKindInAllTen() {
        E3Strata.jdk("FK")
        val segments = TestTiles.segments
        val tiles = File(E3Strata.work, "FK-tiles").also { it.deleteRecursively(); it.mkdirs() }
        val byKind = LinkedHashMap<String, MutableList<E3Case>>()
        val stops = ArrayList<String>()
        val gen = FKGenerator(segments)
        try {
            for (kind in listOf(1, 2, 3, 5, 6)) {
                val profile = profileOf(kind)
                val rnd = Random(SEED + kind)
                val cases = ArrayList<E3Case>()
                val rejected = LinkedHashMap<String, Int>()
                var draws = 0
                while (cases.size < N && draws < CAP) { // ends: draws rises by one each pass
                    draws++
                    when (val d = gen.draw(kind, rnd, profile)) {
                        is FKGenerator.Drawn.Rejected -> rejected.merge(d.why, 1, Int::plus)
                        is FKGenerator.Drawn.Ends -> cases += E3Case(
                            "FK$kind-%02d".format(cases.size + 1), segments, d.e[0], d.e[1], d.e[2], d.e[3], profile,
                        )
                    }
                }
                println("E3 FK kind $kind: ${cases.size} of $N after $draws draws (cap $CAP); profile $profile; rejected $rejected")
                if (kind == 3) println("E3 FK islands: ${gen.islandCounts}")
                byKind["FK$kind"] = cases
            }

            val rnd = Random(SEED + 7)
            var draws = 0
            var pair: DoubleArray? = null
            val rejected = LinkedHashMap<String, Int>()
            while (pair == null && draws < CAP) { // ends: draws rises by one each pass
                draws++
                when (val d = gen.local(rnd, PROFILE)) {
                    is FKGenerator.Drawn.Rejected -> rejected.merge(d.why, 1, Int::plus)
                    is FKGenerator.Drawn.Ends -> pair = d.e
                }
            }
            println("E3 FK kinds 7 and 8 ends: drawn at draw $draws; rejected $rejected")
            val e = pair ?: error("FK: no pair of ends for kinds 7 and 8 within $CAP draws")
            val tile = File(segments, "E170_S50.rd5")
            val end = FKGenerator.dataEnd(tile)
            val size = tile.length()
            println("E3 FK E170_S50: size $size, data end D $end")
            fun edited(id: String, make: (File) -> Unit): E3Case {
                val dir = File(tiles, id)
                make(dir)
                return E3Case(id, dir, e[0], e[1], e[2], e[3], PROFILE)
            }
            val cuts = listOf(200L, 201L, 1_000L, end / 4, end / 2, 3 * end / 4, end - 1, end + 1, end + 56, end + 111)
            byKind["FK7"] = cuts.mapIndexed { i, len -> edited("FK7-%02d".format(i + 1)) { FKGenerator.cut(tile, len, it) } }.toMutableList()
            println("E3 FK kind 7 cuts: ${cuts.map { if (it > end / 2) "D${if (it >= end) "+" else ""}${it - end}" else "$it" }}")
            val flips = (0 until N).map { 8 + 20 * it }
            byKind["FK8"] = flips.mapIndexed { i, at -> edited("FK8-%02d".format(i + 1)) { FKGenerator.flip(tile, at, it) } }.toMutableList()
            println("E3 FK kind 8 bytes: $flips")
            byKind["extra"] = mutableListOf(
                edited("FK7-x1") { FKGenerator.cut(tile, end, it) },
                edited("FK7-x2") { FKGenerator.cut(tile, size - 1, it) },
                edited("FK8-x1") { FKGenerator.flip(tile, 203, it) },
                edited("FK7-c0") { it.mkdirs(); tile.copyTo(File(it, tile.name)) },
            )

            val islandRnd = Random(SEED + 4)
            val islandDraws = iterator {
                var k = 0
                while (true) { // ends: the selector stops drawing at its cap
                    k++
                    when (val d = gen.draw(4, islandRnd, profileOf(4))) {
                        is FKGenerator.Drawn.Rejected -> yield(E3Select.Draw(k, null, d.why))
                        is FKGenerator.Drawn.Ends -> yield(
                            E3Select.Draw(k, E3Case("FK4-%04d".format(k), segments, d.e[0], d.e[1], d.e[2], d.e[3], profileOf(4)), null),
                        )
                    }
                }
            }
            val s4 = E3Select.select("FK4", islandDraws, listOf("FK4"), N, CAP, File(E3Strata.work, "FK4-select"), E3Strata.workers, PER_CASE_SECONDS) { r ->
                val k = r.r1.kind
                if (k.startsWith("target island detected")) E3Select.Mark("FK4", k) else E3Select.Mark(null, "not an island: $k")
            }
            selectionReport(s4)
            byKind["FK4"] = s4.taken.getValue("FK4").map { it.case }.toMutableList()
        } finally {
            gen.close()
        }

        val plans = listOf(E3Case.Plan.LIMIT50, E3Case.Plan.CANCEL_BEFORE, E3Case.Plan.CANCEL_MID)
        val labels = listOf("FK9", "FK10", "FK11")
        val r = RGenerator(segments, SEED + 9, only = "W75_N40.rd5", minMetres = 20_000.0, ratio = 5.0)
        val families = HashMap<String, String>()
        val misses = HashMap<String, Int>()
        val s911 = try {
            val base = r.draws("FK", PROFILE, 64, segments)
            val draws = iterator {
                while (true) { // ends: the selector stops drawing at its cap
                    val d = base.next()
                    val i = (d.number - 1) % 3
                    val c = d.case?.let {
                        E3Case("%s-%04d".format(labels[i], d.number), it.segments, it.startLat, it.startLon, it.endLat, it.endLon, it.profile, it.memoryclass, 0L, plans[i])
                    }
                    yield(E3Select.Draw(d.number, c, d.rejected))
                }
            }
            E3Select.select("FK9-11", draws, labels, N, 3 * CAP, File(E3Strata.work, "FK9-11-select"), E3Strata.workers, PER_CASE_SECONDS) { run ->
                val label = labels[plans.indexOf(run.case.plan)]
                val k = run.r1.kind
                when {
                    k.startsWith("void") -> E3Select.Mark(null, k)
                    families.getOrPut(label) { k } == k -> E3Select.Mark(label, k)
                    else -> {
                        misses.merge(label, 1, Int::plus)
                        E3Select.Mark(null, "outside $label's family ${families[label]}: $k")
                    }
                }
            }
        } finally {
            r.close()
        }
        selectionReport(s911)
        for (label in labels) {
            byKind[label] = s911.taken.getValue(label).map { it.case }.toMutableList()
            if ((misses[label] ?: 0) > 0) stops += "$label: ${misses[label]} non-void cases outside the first one's family"
        }

        val all = byKind.values.flatten()
        val result = E3Stability.run("FK", all, File(E3Strata.work, "FK"), E3Strata.agent, E3Strata.workers, PER_CASE_SECONDS)
        val verdicts = result.verdicts.associateBy { it.case.id }
        for ((label, cases) in byKind) {
            if (label == "extra") continue
            val kinds = cases.map { verdicts.getValue(it.id).outcomes.getValue("R1").kind }
            val family = kinds.firstOrNull()?.let { familyOf(label, it) }
            val inFamily = kinds.count { familyOf(label, it) == family }
            val unstable = cases.count { !verdicts.getValue(it.id).stable }
            println("E3 FK $label: family '$family' (${cases.firstOrNull()?.id}); in family $inFamily of ${cases.size}; unstable $unstable")
            if (cases.size < N || inFamily < cases.size) stops += "$label: $inFamily of ${cases.size} in the family, of $N"
            for (c in cases) verdicts.getValue(c.id).measured["R1"]?.let { println("E3 FK $label ${c.id} measured R1: $it") }
        }
        for (c in byKind.getValue("extra")) {
            val v = verdicts.getValue(c.id)
            println("E3 FK ${c.id}: ${if (v.stable) "stable" else "UNSTABLE"}; R1 ${v.outcomes.getValue("R1").toField()}; thrown ${v.thrown}")
        }
        for (s in stops) println("E3 FK STOP $s")
        E3Strata.report(result)
        assertTrue(stops.isEmpty(), "FK: $stops")
    }

    private fun selectionReport(s: E3Select.Selection) {
        println("E3 ${s.label} selection: ${s.draws} draws counted, ${s.routed} routed; table ${s.table}")
        for ((why, k) in s.rejected) println("E3 ${s.label} rejected: $k $why")
        for ((stratum, runs) in s.taken) {
            println("E3 $stratum taken: ${runs.size} of $N${if (runs.size < N) " (SHORT)" else ""}; last taken at draw ${runs.lastOrNull()?.draw}")
        }
    }

    private fun profileOf(kind: Int) = when (kind) {
        3, 4 -> "trekking-noferries"
        5 -> "car-fast"
        else -> PROFILE
    }

    /** Kind 7's family is the message with its numbers read as `*` (-80); every other kind's is its kind exactly. */
    private fun familyOf(label: String, kind: String) = if (label == "FK7") kind.replace(Regex("\\d+"), "*") else kind

    private companion object {
        const val SEED = 20261208L
        const val N = 10
        const val CAP = 20 * N
        const val PROFILE = "hiking-mountain"
        const val PER_CASE_SECONDS = 600L
    }
}
