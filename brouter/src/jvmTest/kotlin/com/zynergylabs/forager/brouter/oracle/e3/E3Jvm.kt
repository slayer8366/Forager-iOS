package com.zynergylabs.forager.brouter.oracle.e3

/**
 * One E3 regime's JVM (Phase 1 report, E3), started fresh by [E3Launcher]. Each regime runs on a new thread; R4 first
 * asks System.identityHashCode of [PERTURBATION] fresh objects on that thread. Prints one `control <order>` line.
 * The cases join this JVM in a later session; for now it runs the positive control alone.
 */
object E3Jvm {
    const val PERTURBATION = 997

    @JvmStatic
    fun main(args: Array<String>) {
        val regime = args.single()
        require(regime == "R1" || regime == "R4") { "unsupported regime '$regime': only R1 and R4 are built so far" }
        var line: String? = null
        val run = Thread {
            var used = 0L // the hashes are summed and printed, so the calls cannot be dropped as dead code
            if (regime == "R4") repeat(PERTURBATION) { used += System.identityHashCode(Any()) }
            line = "control " + TreeBinControl.groupOrder() + " perturbation-sum " + used
        }
        run.start()
        run.join()
        println(line ?: error("the regime's thread printed nothing"))
    }
}
