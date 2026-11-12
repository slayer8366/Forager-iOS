package com.zynergylabs.forager.brouter.tiles

import kotlin.test.Test

/**
 * Step 10, session 48 (RECORD -139, decision 2): FK's kinds 1 to 8, failures by design, through [Step10Cases]: each kind's
 * cases from `fk<k>-cases.tsv` beside the NT file, written at run time by `NtCasesWriteTest` from `RoutingEngineFKTest`'s
 * cases, held to that kind's rows of [FkExpected] as `RoutingEngineFKTest` compares port and oracle: the kind exactly (kind 7
 * by its family, its numbers read as `*`), and the track fields (nodes, cost, distance, the node and heights digests). The
 * stored tuple digest is held too, except for kind 7. Kind 6's folder is the segments folder, where its tile is missing;
 * each case of kinds 7 and 8 names its own folder of one edited tile, made by the JVM step.
 *
 * FK7-x1 (RECORD -80: its message text on iOS is step 10's to read): the port's message for that case is printed with every
 * digit as `*` and any path as `<path>`, beside the stored row's family, with whether the two are the same text. Nothing
 * else of a kind is printed (E1 as in [Step10Cases]).
 */
class FkEngineCommonTest {
    private fun kind(k: Int, onCase: ((String, EngineRun.Outcome) -> Unit)? = null) {
        val label = "FK$k"
        val nt = Step10Cases.ntFile(label) ?: return
        val rows = FkExpected.ROWS.filter { it.id.startsWith("$label-") }
        val family: (String) -> String = if (k == 7) ({ it.replace(Regex("\\d+"), "*") }) else ({ it })
        Step10Cases.check(label, nt.parent!! / "fk$k-cases.tsv", rows, family = family, holdTuple = k != 7, onCase = onCase)
    }

    @Test
    fun fk1CasesFromTheRunTimeFileThroughTheEngineMatchExpected1b() = kind(1)

    @Test
    fun fk2CasesFromTheRunTimeFileThroughTheEngineMatchExpected1b() = kind(2)

    @Test
    fun fk3CasesFromTheRunTimeFileThroughTheEngineMatchExpected1b() = kind(3)

    @Test
    fun fk4CasesFromTheRunTimeFileThroughTheEngineMatchExpected1b() = kind(4)

    @Test
    fun fk5CasesFromTheRunTimeFileThroughTheEngineMatchExpected1b() = kind(5)

    @Test
    fun fk6CasesFromTheRunTimeFileThroughTheEngineMatchExpected1b() = kind(6)

    @Test
    fun fk7CasesFromTheRunTimeFileThroughTheEngineMatchExpected1b() = kind(7) { id, got ->
        if (id == "FK7-x1") {
            val stored = FkExpected.ROWS.single { it.id == id }.outcome.kind.replace(Regex("\\d+"), "*")
            val port = got.kind.replace(Regex("/[^\\s,;:'\"()]+"), "<path>").replace(Regex("\\d+"), "*")
            println("step10 FK7-x1 message: port '$port'; stored family '$stored'; same text ${port == stored}")
        }
    }

    @Test
    fun fk8CasesFromTheRunTimeFileThroughTheEngineMatchExpected1b() = kind(8)
}
