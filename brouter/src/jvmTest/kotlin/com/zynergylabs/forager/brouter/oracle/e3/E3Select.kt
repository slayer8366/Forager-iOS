package com.zynergylabs.forager.brouter.oracle.e3

import java.io.File
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/**
 * Selection for E4's targeted strata (DL-lat, DL-lon, MP64, ML5, PM5), under RECORD -51 and -52:
 * - a stratum is the first [n] candidates, in generation order, whose reach holds, read from the oracle and never the port;
 * - the reach source is R2: E3's own R1R2 JVM ([E3Jvm]) runs each candidate, R1 with no `debug.txt` and then R2 with it,
 *   in one fresh JVM. A candidate's reach counts only if R2's tuple equals R1's (RECORD -52, the same rule as -50's);
 * - the rest are rejected and counted; [cap] (20 × 59) draws per sequence, and a stratum not full by then is short:
 *   reported with its n, never filled from another generator;
 * - one sequence may feed more than one stratum (ML5 and PM5): [mark] sends each candidate to one stratum or none.
 *
 * A draw is one pass of the generator, so a draw rejected before routing (its geometry) counts toward [cap], as R's
 * generator counts it ([RGenerator.generate]). Candidates are routed [workers] at a time and taken strictly in draw order;
 * draws still running when every stratum is full are not counted and not written. Every counted draw is one row of
 * `select.tsv` in [work].
 */
object E3Select {
    /** `RoutingEngine.java:2025` at v1.7.10: the line every collection logs. */
    const val COLLECTED = "collected, nodes/paths"

    /** `:2031`: the line panic mode logs. It also holds "memory limit reached", so the two are told apart by this part. */
    const val PANIC = "enabled memory panic mode"

    /** One draw: its number in generation order, from 1, and its case, or why it was rejected before routing. */
    class Draw(val number: Int, val case: E3Case?, val rejected: String?) {
        init {
            require((case == null) != (rejected == null)) { "draw $number: a case or a reason, not both or neither" }
        }
    }

    /** One candidate's R1 and R2 from its R1R2 JVM. */
    class Run(
        val draw: Int,
        val case: E3Case,
        val r1: E3Outcome,
        val r2: E3Outcome,
        val r2Log: List<String>,
        val r1Extent: IntArray?,
        val r1Millis: Long,
    ) {
        val r2EqualsR1: Boolean get() = r1 == r2
        val collections: Int get() = r2Log.count { it.startsWith(COLLECTED) }
        val panic: Boolean get() = r2Log.any { it.contains(PANIC) }
    }

    /** Reach's verdict on a candidate whose R2 equals its R1: the stratum it goes to, or null; and what was read. */
    class Mark(val stratum: String?, val reading: String)

    class Selection(
        val label: String,
        val taken: Map<String, List<Run>>,
        val draws: Int,
        val routed: Int,
        val rejected: Map<String, Int>,
        val table: File,
    )

    fun select(
        label: String,
        draws: Iterator<Draw>,
        strata: List<String>,
        n: Int,
        cap: Int,
        work: File,
        workers: Int,
        perCaseSeconds: Long,
        mark: (Run) -> Mark,
    ): Selection {
        work.deleteRecursively()
        val caseDir = File(work, "cases").also { it.mkdirs() }
        val taken = strata.associateWith { ArrayList<Run>() }
        val rejected = LinkedHashMap<String, Int>()
        val rows = ArrayList<String>()
        val inflight = ArrayDeque<Pair<Draw, Future<List<String>>?>>()
        fun full() = taken.values.all { it.size >= n }
        var drawn = 0
        var counted = 0
        var routed = 0
        val pool = Executors.newFixedThreadPool(workers)
        try {
            while (true) { // ends: each pass consumes one draw, and at most cap are drawn
                while (inflight.size < 2 * workers && drawn < cap && !full()) {
                    val d = draws.next()
                    drawn++
                    check(d.number == drawn) { "$label: draw ${d.number} came as the ${drawn}th" }
                    val future = d.case?.let { c ->
                        val caseFile = File(caseDir, "${c.id}.tsv").also { E3Case.writeList(listOf(c), it) }
                        pool.submit(Callable { E3Stability.launch("R1R2", c.id, caseFile, work, emptyList(), perCaseSeconds) })
                    }
                    inflight.addLast(d to future)
                }
                if (inflight.isEmpty() || full()) break
                val (d, future) = inflight.removeFirst()
                counted++
                if (future == null) {
                    rejected.merge("before routing: ${d.rejected}", 1, Int::plus)
                    rows += listOf(d.number, "-", "-", "-", "-", "-", "-", "-", "-", "-", d.rejected, "rejected").joinToString("\t")
                    continue
                }
                val run = parse(d, future.get())
                routed++
                val m = if (run.r2EqualsR1) mark(run) else Mark(null, "R2's tuple differs from R1's")
                val result = when {
                    m.stratum == null -> "rejected".also { rejected.merge("reach: ${m.reading}", 1, Int::plus) }
                    taken.getValue(m.stratum).size >= n ->
                        "rejected: ${m.stratum} already full".also { rejected.merge("${m.stratum} already full", 1, Int::plus) }
                    else -> "taken ${m.stratum}".also { taken.getValue(m.stratum) += run }
                }
                rows += listOf(
                    d.number, run.case.id, run.r1.toField(), run.r2.toField(), run.r2EqualsR1,
                    run.r1Extent?.joinToString(",") ?: "-", run.collections, run.panic, run.r1Millis,
                    run.case.memoryclass, m.reading, result,
                ).joinToString("\t")
            }
        } finally {
            // Shut down, not now: an interrupted launcher would leave its child JVM running unwatched.
            pool.shutdown()
            pool.awaitTermination(2 * perCaseSeconds, TimeUnit.SECONDS)
        }
        val table = File(work, "select.tsv")
        table.writeText(
            "draw\tid\tR1-tuple\tR2-tuple\tR2-equals-R1\tR1-extent\tR2-collected-lines\tR2-panic\tR1-ms\tmemoryclass\treach\tresult\n" +
                rows.joinToString("\n") + "\n",
        )
        check(rows.size == counted) { "$label: ${rows.size} rows for $counted counted draws" }
        return Selection(label, taken, counted, routed, rejected, table)
    }

    private fun parse(d: Draw, out: List<String>): Run {
        val c = d.case!!
        fun mine(kind: String, regime: String): List<String> {
            val head = "$kind $regime ${c.id} "
            return out.filter { it.startsWith(head) }.map { it.substring(head.length) }
        }
        return Run(
            d.number, c,
            E3Outcome.fromField(mine("outcome", "R1").single()),
            E3Outcome.fromField(mine("outcome", "R2").single()),
            mine("log", "R2"),
            mine("extent", "R1").singleOrNull()?.split(' ')?.map { it.toInt() }?.toIntArray(),
            mine("millis", "R1").single().toLong(),
        )
    }
}
