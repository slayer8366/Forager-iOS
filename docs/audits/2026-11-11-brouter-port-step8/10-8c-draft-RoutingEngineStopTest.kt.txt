package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.oracle.OracleCalls
import com.zynergylabs.forager.brouter.oracle.e3.E3Case
import com.zynergylabs.forager.brouter.oracle.e3.E3CaseRun
import com.zynergylabs.forager.brouter.oracle.e3.E3Expected
import com.zynergylabs.forager.brouter.oracle.e3.E3Outcome
import com.zynergylabs.forager.brouter.oracle.e3.RGenerator
import com.zynergylabs.forager.brouter.oracle.e3.strata.E3FKTest
import com.zynergylabs.forager.brouter.tiles.TestTiles
import com.zynergylabs.forager.brouter.util.OracleLibm
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Step 8c (RECORD -90, -124; the step 8 report's §1): terminate, the time limit, the progress count and `catch (Error)`.
 *
 * **Port against the oracle, E3's FK 9 to 11** (Phase 1 report, E4; RECORD -80): the cases are the draws of 1b's
 * [RGenerator] as [E3FKTest] builds them (its seed `SEED + 9`, `W75_N40` only, 20 km times 5^u, draw k to kind 9, 10 or 11
 * by k mod 3) whose ids are FK9's, FK10's and FK11's rows in `expected-1b.tsv`, within 1b's cap of 3 × [E3FKTest.CAP]
 * draws. Each runs the plan's variant only (not E3CaseRun's measured run, whose void test is a timing of this machine):
 * kind 9 `doRun(50)`; kind 10 `terminate()` then `doRun(0)`; kind 11 `doRun(0)` on its own thread and `terminate()` once
 * `getLinksProcessed()` passes [E3CaseRun.CANCEL_AFTER_LINKS], as [E3CaseRun] does.
 *
 * **What is compared where the oracle's result depends on wall time** (said here before the first port result): not a
 * timing. Kind 9's message is `<operation> timeout after <limit / 1000> seconds` (`RoutingEngine.java:1982`); which
 * operation the limit strikes in (pass0, pass1 or re-tracking) depends on wall time, the rest does not, so each side must
 * match `(pass0|pass1|re-tracking) timeout after 0 seconds` exactly. Kinds 10 and 11's message is `operation killed by
 * thread-priority-watchdog after <s> seconds` (`:1976`), whose seconds are wall time, so each side must match it with
 * any digits there. Beside the message: the kind (`E3Outcome.kindOf`, held to the row's), the found track's nodes, cost
 * and distance (the empty track on both sides), `isTerminated()` (kinds 10, 11), and for kind 11 a progress count past the
 * cancel point on both sides. The count at the cancel itself is wall time and is not compared.
 *
 * **The progress count, exactly:** kind 11's cases run to the end with no cancel and no limit; `getLinksProcessed()` is
 * compared exactly, with the outcome.
 *
 * **The port's clock** ([EngineClock]) is injected; the oracle reads `System.currentTimeMillis()`. The port runs against
 * the oracle on [EngineClock.MONOTONIC]; the tests that need a time of their own give it a clock they drive, and are held
 * to Java's text (`:1976`, `:1982`, `:318-328`, `:812`), not to the oracle, which cannot be given a clock. No E3 row
 * reaches `catch (Error)` (no row's kind is an Error's `toString`), so it is tested through the clock only.
 * Only digests and counts are printed (E1).
 */
class RoutingEngineStopTest {
    @Test
    fun fk9TimesOutThroughThePortAsThroughTheOracle() = againstOracle("FK9", 50L, Cancel.NONE, TIMEOUT_50)

    @Test
    fun fk10IsKilledBeforeTheRunThroughThePortAsThroughTheOracle() = againstOracle("FK10", 0L, Cancel.BEFORE, WATCHDOG)

    @Test
    fun fk11IsKilledMidRunThroughThePortAsThroughTheOracle() = againstOracle("FK11", 0L, Cancel.MID, WATCHDOG)

    @Test
    fun fk11CountsLinksThroughThePortAsThroughTheOracle() {
        val mismatches = ArrayList<String>()
        for (c in cases("FK11")) {
            val o = oracle(c, 0L, Cancel.NONE)
            val p = port(c, 0L, Cancel.NONE, EngineClock.MONOTONIC)
            if (o.toString() != p.toString()) mismatches += "${c.id}: oracle ${o.digest()}; port ${p.digest()}"
            println("8c FK11 count ${c.id}: oracle ${o.links} links, ${o.kind}; port ${p.links} links, ${p.kind}")
        }
        assertTrue(mismatches.isEmpty(), "8c FK11 count: ${mismatches.size} mismatches; first ${mismatches.take(5)}")
    }

    /** Forager's own limit (`doRun(30_000)`), on a clock that never moves: the limit never strikes, the route is the oracle's. */
    @Test
    fun forager30sLimitOnAStoppedClockRoutesAsTheOracleWithNoLimit() {
        val c = cases("FK10").first()
        val o = oracle(c, 0L, Cancel.NONE)
        val p = port(c, 30_000L, Cancel.NONE) { 0L }
        assertEquals("route", o.kind, "${c.id}: the oracle routes")
        assertEquals(o.toString(), p.toString(), "${c.id}: oracle ${o.digest()}; port ${p.digest()}")
    }

    /** `:1979-1983` on a clock the test drives: the start reads 0, every later read 30,001. */
    @Test
    fun theTimeLimitReadsTheInjectedClock() {
        val c = cases("FK10").first()
        var reads = 0
        val p = port(c, 30_000L, Cancel.NONE) { if (reads++ == 0) 0L else 30_001L }
        assertEquals("pass0 timeout after 30 seconds", p.message, c.id)
        assertEquals(listOf(0, 0, 0), listOf(p.nodes, p.cost, p.distance), "${c.id}: the empty track")
        assertEquals(false, p.terminated, c.id)
    }

    /** `:1975-1977` on a clock the test drives: the start reads 0, every later read 7,000. */
    @Test
    fun theWatchdogsSecondsReadTheInjectedClock() {
        val c = cases("FK10").first()
        var reads = 0
        val p = port(c, 0L, Cancel.BEFORE) { if (reads++ == 0) 0L else 7_000L }
        assertEquals("operation killed by thread-priority-watchdog after 7 seconds", p.message, c.id)
        assertEquals(true, p.terminated, c.id)
    }

    /** `:325-328` and `:812`: an Error is caught, `cleanOnOOM()` terminates, the message is its `toString()`. */
    @Test
    fun anErrorDuringTheRunIsCaughtAndTerminates() {
        val c = cases("FK10").first()
        val error = ClockError("the clock failed")
        var reads = 0
        val p = port(c, 30_000L, Cancel.NONE) { if (reads++ == 0) 0L else throw error }
        assertEquals(error.toString(), p.message, c.id)
        assertEquals(true, p.terminated, "${c.id}: cleanOnOOM calls terminate")
        assertEquals(listOf(0, 0, 0), listOf(p.nodes, p.cost, p.distance), "${c.id}: the empty track")
    }

    /** `:320-324` and `:812`: an Exception that is not an IllegalArgumentException is caught; a RuntimeException's message. */
    @Test
    fun anExceptionDuringTheRunIsCaughtWithoutTerminating() {
        val c = cases("FK10").first()
        var reads = 0
        val p = port(c, 30_000L, Cancel.NONE) { if (reads++ == 0) 0L else throw IllegalStateException("the clock stopped") }
        assertEquals("the clock stopped", p.message, c.id)
        assertEquals(false, p.terminated, c.id)
    }

    private class ClockError(message: String) : Error(message)

    private enum class Cancel { NONE, BEFORE, MID }

    /** One run's result: the engine's message, the found track's three numbers, the progress count, the flag. */
    private data class Run(val message: String?, val nodes: Int, val cost: Int, val distance: Int, val links: Int, val terminated: Boolean) {
        val kind get() = E3Outcome.kindOf(message)

        /** The message is not printed (E1 allows it, but a long one would crowd the log); its kind and length are. */
        fun digest() = "$kind (${message?.length ?: 0} chars)|$nodes|$cost|$distance|$links links|terminated $terminated"
    }

    private fun againstOracle(label: String, maxRunningTime: Long, cancel: Cancel, fixed: Regex) {
        val rows = rows(label)
        val mismatches = ArrayList<String>()
        val operations = HashMap<String, Int>()
        for (c in cases(label)) {
            val row = rows.getValue(c.id)
            val o = oracle(c, maxRunningTime, cancel)
            val p = port(c, maxRunningTime, cancel, EngineClock.MONOTONIC)
            for (r in listOf(o, p)) r.message?.substringBefore(' ')?.let { operations.merge(it, 1, Int::plus) }
            val differ = listOfNotNull(
                "oracle kind against expected-1b".takeIf { o.kind != row.outcome.kind },
                "kind".takeIf { o.kind != p.kind },
                "oracle message's fixed part".takeIf { o.message == null || !fixed.matches(o.message) },
                "port message's fixed part".takeIf { p.message == null || !fixed.matches(p.message) },
                "track".takeIf { listOf(o.nodes, o.cost, o.distance) != listOf(p.nodes, p.cost, p.distance) },
                "terminated".takeIf { o.terminated != p.terminated },
                "oracle count at the cancel".takeIf { cancel == Cancel.MID && o.links <= E3CaseRun.CANCEL_AFTER_LINKS },
                "port count at the cancel".takeIf { cancel == Cancel.MID && p.links <= E3CaseRun.CANCEL_AFTER_LINKS },
            )
            if (differ.isNotEmpty()) mismatches += "${c.id}: $differ (oracle ${o.digest()}; port ${p.digest()})"
        }
        println("8c $label: ${rows.size} cases; ${mismatches.size} mismatches; first words of the messages, both sides: $operations")
        mismatches.take(20).forEach { println("8c $label mismatch $it") }
        assertTrue(mismatches.isEmpty(), "8c $label: ${mismatches.size} mismatches; first ${mismatches.take(5)}")
    }

    private fun rows(label: String) = E3Expected.read(javaClass.getResource(E3Expected.RESOURCE)!!.readText()).rows
        .filter { it.stratum == "FK" && it.id.startsWith("$label-") }.associateBy { it.id }

    /** [label]'s cases, in draw order: 1b's draws whose ids are its rows, within 1b's cap (as [RoutingEngineMemoryTest]). */
    private fun cases(label: String): List<E3Case> {
        val rows = rows(label)
        assertEquals(10, rows.size, "$label's rows in expected-1b")
        val labels = listOf("FK9", "FK10", "FK11")
        val found = ArrayList<E3Case>()
        val gen = RGenerator(TestTiles.segments, E3FKTest.SEED + 9, only = "W75_N40.rd5", minMetres = 20_000.0, ratio = 5.0)
        try {
            val draws = gen.draws("FK", E3FKTest.PROFILE, 64, TestTiles.segments)
            var n = 0
            while (found.size < rows.size && n < 3 * E3FKTest.CAP) {
                n++
                val d = draws.next()
                val it = d.case ?: continue
                val id = "%s-%04d".format(labels[(d.number - 1) % 3], d.number)
                if (id in rows) found += E3Case(id, it.segments, it.startLat, it.startLon, it.endLat, it.endLon, it.profile, it.memoryclass)
            }
        } finally {
            gen.close()
        }
        assertEquals(rows.keys, found.map { it.id }.toSet(), "$label's cases are expected-1b's rows, within 1b's cap")
        return found
    }

    private fun oracle(c: E3Case, maxRunningTime: Long, cancel: Cancel): Run {
        val collector = btools.router.RoutingParamCollector()
        val waypoints = collector.getWayPointList("${c.startLon},${c.startLat}|${c.endLon},${c.endLat}")
        val rc = btools.router.RoutingContext()
        collector.setParams(rc, waypoints, params(c))
        rc.memoryclass = c.memoryclass
        val engine = btools.router.RoutingEngine(null, null, c.segments, waypoints, rc, btools.router.RoutingEngine.BROUTER_ENGINEMODE_ROUTING)
        engine.quite = true
        drive(cancel, { engine.terminate() }, { engine.getLinksProcessed() }) { engine.doRun(maxRunningTime) }
        val t = engine.getFoundTrack()
        return Run(engine.getErrorMessage(), t.nodes.size, t.cost, t.distance, engine.getLinksProcessed(), engine.isTerminated())
    }

    private fun port(c: E3Case, maxRunningTime: Long, cancel: Cancel, clock: EngineClock): Run {
        val collector = RoutingParamCollector()
        val waypoints = collector.getWayPointList("${c.startLon},${c.startLat}|${c.endLon},${c.endLat}")
        val rc = RoutingContext(OracleLibm)
        collector.setParams(rc, waypoints, params(c))
        rc.memoryclass = c.memoryclass
        val engine = RoutingEngine(FileSystem.SYSTEM, c.segments.toOkioPath(), waypoints, rc, RoutingEngine.BROUTER_ENGINEMODE_ROUTING, clock)
        engine.quite = true
        drive(cancel, { engine.terminate() }, { engine.getLinksProcessed() }) { engine.doRun(maxRunningTime) }
        val t = engine.getFoundTrack()
        return Run(engine.getErrorMessage(), t.nodes.size, t.cost, t.distance, engine.getLinksProcessed(), engine.isTerminated())
    }

    private fun params(c: E3Case) = hashMapOf(
        "profile" to File(OracleCalls.oracleDir, "profiles2/${c.profile}.brf").absolutePath,
        "alternativeidx" to "0",
        "profile:use_dynamic_range" to "0",
    )

    /** As [E3CaseRun]'s plans: [run] on this thread, after a [terminate], or on its own thread with a cancel mid-run. */
    private fun drive(cancel: Cancel, terminate: () -> Unit, links: () -> Int, run: () -> Unit) {
        when (cancel) {
            Cancel.NONE -> run()
            Cancel.BEFORE -> {
                terminate()
                run()
            }
            Cancel.MID -> {
                var failure: Throwable? = null
                val t = Thread { try { run() } catch (e: Throwable) { failure = e } }
                t.start()
                // Ends: the search thread finishes or the count passes the cancel point.
                while (t.isAlive && links() <= E3CaseRun.CANCEL_AFTER_LINKS) Thread.sleep(1)
                terminate()
                t.join()
                failure?.let { throw it }
            }
        }
    }

    private companion object {
        /** Kind 9 at `doRun(50)`: `50 / 1000` is 0 (`:1982`); the operation is wall time. */
        val TIMEOUT_50 = Regex("(pass0|pass1|re-tracking) timeout after 0 seconds")

        /** Kinds 10 and 11 (`:1976`): the seconds are wall time. */
        val WATCHDOG = Regex("operation killed by thread-priority-watchdog after \\d+ seconds")
    }
}
