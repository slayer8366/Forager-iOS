package com.zynergylabs.forager.brouter.oracle.e3

import java.io.File
import java.util.Random

/**
 * One E3 regime's JVM (Phase 1 report, E3), started fresh by [E3Launcher]. Every regime's thread prints its own
 * positive-control line (`control <regime> <order> perturbation-sum <n>`) before its case, so each fresh JVM's control is
 * kept in every regime.
 *
 * - `E3Jvm R1|R4` alone: the control only ([E3ControlTest]).
 * - `E3Jvm <regime> <base> <caseFile> <caseId>`, one case: `R1R2` (R1 on a new thread with no `debug.txt` in [base];
 *   then `debug.txt` is created and R2 runs the case again on another new thread), `R4` (997 identity hashes first),
 *   `R5` (`debug.txt` present) or `R6` (no `debug.txt`; the launcher adds JaCoCo's agent).
 * - `E3Jvm R3 <base> <caseFile> all`: the whole list on one thread, shuffled by [R3_SEED], with `debug.txt`.
 *
 * Prints `outcome <regime> <id> <tuple>`, `log <regime> <id> <line>` for each collection line, `millis <regime> <id> <ms>`.
 */
object E3Jvm {
    const val PERTURBATION = 997

    /** R3's fixed shuffle (E3: "a fixed shuffled order, the seed in the test source"). */
    const val R3_SEED = 20261030L

    @JvmStatic
    fun main(args: Array<String>) {
        val regime = args[0]
        if (args.size == 1) {
            require(regime == "R1" || regime == "R4") { "the control alone runs as R1 or R4, not '$regime'" }
            onNewThread { println(control(regime, perturb = regime == "R4")) }
            return
        }
        require(args.size == 4) { "usage: E3Jvm <regime> <base> <caseFile> <caseId|all>" }
        val base = File(args[1])
        val debug = File(base, "debug.txt")
        val cases = E3Case.readList(File(args[2]))
        when (regime) {
            "R3" -> {
                require(args[3] == "all") { "R3 runs the whole list" }
                debug.createNewFile()
                val order = cases.toMutableList().also { it.shuffle(Random(R3_SEED)) }
                onNewThread {
                    println(control("R3", perturb = false))
                    for (c in order) report("R3", c, base)
                }
            }
            "R1R2", "R4", "R5", "R6" -> {
                val case = cases.single { it.id == args[3] }
                when (regime) {
                    "R1R2" -> {
                        check(!debug.exists()) { "R1 runs without debug.txt" }
                        onNewThread { println(control("R1", false)); report("R1", case, base) }
                        debug.createNewFile()
                        onNewThread { println(control("R2", false)); report("R2", case, base) }
                    }
                    "R4" -> { debug.createNewFile(); onNewThread { println(control("R4", true)); report("R4", case, base) } }
                    "R5" -> { debug.createNewFile(); onNewThread { println(control("R5", false)); report("R5", case, base) } }
                    else -> { check(!debug.exists()); onNewThread { println(control("R6", false)); report("R6", case, base) } }
                }
            }
            else -> error("unsupported regime '$regime'")
        }
    }

    private fun control(regime: String, perturb: Boolean): String {
        var used = 0L // the hashes are summed and printed, so the calls cannot be dropped as dead code
        if (perturb) repeat(PERTURBATION) { used += System.identityHashCode(Any()) }
        return "control $regime ${TreeBinControl.groupOrder()} perturbation-sum $used"
    }

    private fun report(regime: String, case: E3Case, base: File) {
        val t0 = System.nanoTime()
        val r = E3CaseRun.run(case, base)
        val ms = (System.nanoTime() - t0) / 1_000_000
        println("outcome $regime ${case.id} ${r.outcome.toField()}")
        for (line in r.collections) println("log $regime ${case.id} $line")
        println("millis $regime ${case.id} $ms")
    }

    /** Runs [body] on a new thread and waits; a throw there fails this JVM rather than vanishing with the thread. */
    private fun onNewThread(body: () -> Unit) {
        var failure: Throwable? = null
        val t = Thread { try { body() } catch (e: Throwable) { failure = e } }
        t.start()
        t.join()
        failure?.let { throw it }
    }
}
