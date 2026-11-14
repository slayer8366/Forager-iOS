package com.zynergylabs.forager.brouter.tiles

import com.zynergylabs.forager.brouter.oracle.OracleCalls
import com.zynergylabs.forager.brouter.router.RoutingEngineF0Test
import com.zynergylabs.forager.brouter.util.MsunLibm
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Step 10, session 57 (RECORD -150, decision 2): [FormatDigests]' constants held to the port's own production run on the
 * JVM, recomputed here, so they cannot drift. F0's 2, F0h's 8 and NT's 59 cases, built as [HintsFormatsRun] builds them
 * (the same order, so the same [FormatOutputs.modeFor] modes), each run through [HintsFormatsRun.port] on `MsunLibm`; every
 * stored row's id, mode, five lengths and five digests must equal the recomputed ones, and each group must have one row
 * per case. [FormatDigests.ORACLE_VERSION] is held to the oracle's `OsmTrack.version`, which the run sets (RECORD -118).
 * A row that differs or is missing prints its recomputed row (id, mode, lengths, digests: no text, no position, E1).
 * Session 60 (RECORD -163): GPX's and JSON's length and digest are of [FormatNumberText]'s canonical text, as on the
 * simulator; hints, KML and CSV of the text itself. Session 71 (RECORD -177): each row's GPX and JSON plain text length
 * ([FormatOutputs.Row.textLengths]) is held to the production run's too.
 */
class FormatDigestsJvmTest {
    @Test
    fun storedDigestsAreThePortsProductionRunOnTheJvm() {
        assertEquals(FormatDigests.ORACLE_VERSION, btools.router.OsmTrack.version, "FormatDigests.ORACLE_VERSION against the oracle's")
        val problems = ArrayList<String>()
        val groups = listOf(
            HintsFormatsRun.Group("F0", RoutingEngineF0Test.CASES, OracleCalls.oracleDir),
            HintsFormatsRun.Group("F0h", HintsFormatsRun.f0h(), OracleCalls.oracleDir),
            HintsFormatsRun.Group("NT", HintsFormatsRun.nt(), OracleCalls.oracleDir),
        )
        for (g in groups) {
            val stored = when (g.name) {
                "F0" -> FormatDigests.F0
                "F0h" -> FormatDigests.F0H
                else -> FormatDigests.NT
            }
            val byId = stored.associateBy { it.id }
            val equal = IntArray(5)
            val version = com.zynergylabs.forager.brouter.router.OsmTrack.version
            com.zynergylabs.forager.brouter.router.OsmTrack.version = FormatDigests.ORACLE_VERSION
            try {
                for ((i, c) in g.cases.withIndex()) {
                    val mode = FormatOutputs.modeFor(i)
                    val p = HintsFormatsRun.port(c, g.base, mode, MsunLibm)
                    val canon = p.texts.mapIndexed { k, s -> FormatNumberText.forDigest(k, s, p.showspeed) }
                    val lens = canon.map { it.length }
                    val digs = canon.map { EngineRun.fnv1aText(it) }
                    val row = byId[c.id]
                    // Session 71 (RECORD -177): the plain text's length of GPX and JSON, held to the stored row too.
                    val textLens = listOf(p.texts[1].length, p.texts[2].length)
                    var same = row != null && row.mode == mode && row.textLengths == textLens
                    for (k in 0 until 5) {
                        val textOk = k != 1 && k != 2 || row != null && row.textLengths[k - 1] == textLens[k - 1]
                        if (row != null && row.mode == mode && row.lengths[k] == lens[k] && row.digests[k] == digs[k] && textOk) equal[k]++ else same = false
                    }
                    if (!same) {
                        val textNote = if (row != null && row.textLengths != textLens) " (gpx/json text length here ${textLens.joinToString("/")} stored ${row.textLengths.joinToString("/")})" else ""
                        problems += "${g.name} ${c.id}: stored row against the port's production run$textNote"
                        println("s57row r(\"${c.id}\", $mode, \"${lens.joinToString(" ")}\", \"${digs.joinToString(" ")}\", \"${textLens.joinToString(" ")}\"),")
                    }
                }
            } finally {
                com.zynergylabs.forager.brouter.router.OsmTrack.version = version
            }
            if (stored.size != g.cases.size) problems += "${g.name}: ${stored.size} stored rows for ${g.cases.size} cases"
            println(
                "step10 hints-formats stored jvm ${g.name}: ${g.cases.size} cases, ${stored.size} stored; " +
                    FormatOutputs.OUTPUTS.indices.joinToString("; ") { "${FormatOutputs.OUTPUTS[it]} equal ${equal[it]}" },
            )
        }
        problems.take(40).forEach { println("step10 hints-formats stored jvm problem $it") }
        assertEquals(0, problems.size, "stored digests against the port's production run (${problems.size} differences; first: ${problems.firstOrNull()})")
    }
}
