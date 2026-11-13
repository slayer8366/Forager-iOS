package com.zynergylabs.forager.brouter.tiles

import com.zynergylabs.forager.brouter.router.EngineClock
import com.zynergylabs.forager.brouter.router.RoutingContext
import com.zynergylabs.forager.brouter.router.RoutingEngine
import com.zynergylabs.forager.brouter.router.RoutingParamCollector
import com.zynergylabs.forager.brouter.util.MsunLibm
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

/**
 * Step 10, session 54: the four clock-driven tests of jvmTest's `RoutingEngineStopTest` that need no oracle, mirrored in
 * common code so they run on the iOS simulator too. FK 9 to 11 have no oracle on iOS; these hold the port to Java's text
 * (`RoutingEngine.java:1975-1983`, `:318-328`, `:812`) on a clock the test drives, through the existing [EngineClock]
 * seam. Each assertion is the JVM test's, unchanged; the case is F0's first (Forager's own ends, E1 allows them; F0's
 * tile, `hiking-mountain`, memory class 64) in place of FK10's first, which needs jvmTest's generator. Paths as
 * [F0RouteCommonTest]'s. The JVM tests stay where they are.
 *
 * Not mirrored: the FK 9 to 11 comparisons and the progress count (they run the oracle), and the mid-run cancel (a JVM
 * `Thread`). Session 64 (RECORD -148, decision 2): the 30 s limit on a stopped clock, held to the oracle's own run on the
 * JVM, has a sibling here held to F0's stored row instead; the JVM test stays as it is.
 *
 * The [Error] test answers RECORD -126's question on Native: a `kotlin.Error` thrown from inside the run (the clock) is
 * caught by the engine's `catch (e: Error)`, which terminates and keeps its `toString()`.
 */
class EngineStopCommonTest {
    /** `:1979-1983` on a clock the test drives: the start reads 0, every later read 30,001. */
    @Test
    fun theTimeLimitReadsTheInjectedClock() {
        var reads = 0
        val p = run(30_000L, cancelFirst = false) { if (reads++ == 0) 0L else 30_001L }
        assertEquals("target-island-check timeout after 30 seconds", p.message, ID)
        assertEquals(listOf(0, 0, 0), listOf(p.nodes, p.cost, p.distance), "$ID: the empty track")
        assertEquals(false, p.terminated, ID)
    }

    /** `:1975-1977` on a clock the test drives: the start reads 0, every later read 7,000. */
    @Test
    fun theWatchdogsSecondsReadTheInjectedClock() {
        var reads = 0
        val p = run(0L, cancelFirst = true) { if (reads++ == 0) 0L else 7_000L }
        assertEquals("operation killed by thread-priority-watchdog after 7 seconds", p.message, ID)
        assertEquals(true, p.terminated, ID)
    }

    /** `:325-328` and `:812`: an Error is caught, `cleanOnOOM()` terminates, the message is its `toString()`. */
    @Test
    fun anErrorDuringTheRunIsCaughtAndTerminates() {
        val error = ClockError("the clock failed")
        var reads = 0
        val p = run(30_000L, cancelFirst = false) { if (reads++ == 0) 0L else throw error }
        assertEquals(error.toString(), p.message, ID)
        assertEquals(true, p.terminated, "$ID: cleanOnOOM calls terminate")
        assertEquals(listOf(0, 0, 0), listOf(p.nodes, p.cost, p.distance), "$ID: the empty track")
    }

    /** `:320-324` and `:812`: an Exception that is not an IllegalArgumentException is caught; a RuntimeException's message. */
    @Test
    fun anExceptionDuringTheRunIsCaughtWithoutTerminating() {
        var reads = 0
        val p = run(30_000L, cancelFirst = false) { if (reads++ == 0) 0L else throw IllegalStateException("the clock stopped") }
        assertEquals("the clock stopped", p.message, ID)
        assertEquals(false, p.terminated, ID)
    }

    /**
     * Session 64 (RECORD -148, decision 2): the JVM's `forager30sLimitOnAStoppedClockRoutesAsTheOracleWithNoLimit`, held
     * to F0's stored row in place of the oracle. Forager's own limit (`doRun(30_000)`) on a clock that never moves: the
     * limit never strikes, the route is the row. The row and its ends are [F0EngineCommonTest]'s first case, compared as
     * that test compares it (six fields and the tuple digest).
     */
    @Test
    fun forager30sLimitOnAStoppedClockRoutesAsF0sStoredRow() {
        val c = F0EngineCommonTest.CASES.first()
        assertEquals(ID, c.id)
        val (tiles, profile) = paths()
        val collector = RoutingParamCollector()
        val waypoints = collector.getWayPointList("${c.startLon},${c.startLat}|${c.endLon},${c.endLat}")
        val rc = RoutingContext(MsunLibm)
        collector.setParams(
            rc, waypoints,
            mapOf("profile" to profile.toString(), "alternativeidx" to "0", "profile:use_dynamic_range" to "0"),
        )
        rc.memoryclass = 64
        val engine = RoutingEngine(FileSystem.SYSTEM, tiles, waypoints, rc, RoutingEngine.BROUTER_ENGINEMODE_ROUTING) { 0L }
        engine.quite = true
        engine.doRun(30_000L)
        val t = engine.getFoundTrack()
        val got = EngineRun.Outcome(
            EngineRun.kindOf(engine.getErrorMessage()), t.nodes.size, t.cost, t.distance,
            EngineRun.fnv1a(IntArray(t.nodes.size) { t.nodes[it].getILon() }, IntArray(t.nodes.size) { t.nodes[it].getILat() }),
            EngineRun.fnv1aShorts(ShortArray(t.nodes.size) { t.nodes[it].getSElev() }),
        )
        println("step10 stop $ID stopped clock: port ${got.toField()} tuple ${got.tupleDigest}")
        assertEquals(c.want.toField(), got.toField(), "$ID: 30 s limit on a stopped clock against expected-1b.tsv")
        assertEquals(c.tupleDigest, got.tupleDigest, "$ID: tuple digest")
    }

    private class ClockError(message: String) : Error(message)

    /** One run's result, as `RoutingEngineStopTest.Run`: the message, the found track's three numbers, the flag. */
    private class Run(val message: String?, val nodes: Int, val cost: Int, val distance: Int, val terminated: Boolean)

    /** As `RoutingEngineStopTest.port`, on F0's first case: `terminate()` first if [cancelFirst], then `doRun`. */
    private fun run(maxRunningTime: Long, cancelFirst: Boolean, clock: EngineClock): Run {
        val (tiles, profile) = paths()
        val collector = RoutingParamCollector()
        val waypoints = collector.getWayPointList(LON_LATS)
        val rc = RoutingContext(MsunLibm)
        collector.setParams(
            rc, waypoints,
            mapOf("profile" to profile.toString(), "alternativeidx" to "0", "profile:use_dynamic_range" to "0"),
        )
        rc.memoryclass = 64
        val engine = RoutingEngine(FileSystem.SYSTEM, tiles, waypoints, rc, RoutingEngine.BROUTER_ENGINEMODE_ROUTING, clock)
        engine.quite = true
        if (cancelFirst) engine.terminate()
        engine.doRun(maxRunningTime)
        val t = engine.getFoundTrack()
        val p = Run(engine.getErrorMessage(), t.nodes.size, t.cost, t.distance, engine.isTerminated())
        println("step10 stop $ID: kind ${EngineRun.kindOf(p.message)} (${p.message?.length ?: 0} chars)|${p.nodes}|${p.cost}|${p.distance}|terminated ${p.terminated}")
        return p
    }

    private fun paths(): Pair<Path, Path> {
        val tiles = setting("brouter.tiles.dir", "BROUTER_TILES_DIR").toPath() / "f0"
        val profile = setting("brouter.oracle.dir", "BROUTER_ORACLE_DIR").toPath() / "profiles2" / "hiking-mountain.brf"
        for (p in listOf(tiles / "W75_N40.rd5", profile, profile.parent!! / "lookups.dat")) {
            if (!FileSystem.SYSTEM.exists(p)) fail("$p does not exist or cannot be read in this process")
        }
        return tiles to profile
    }

    private companion object {
        const val ID = "F0-inside-across-the-degree-line"

        // Ends: RoutingEngineF0Test.CASES' first (Forager's own; E1 allows them), as the waypoint text, so both platforms
        // parse the same characters.
        const val LON_LATS = "-73.9978932,41.2742564|-74.0159457,41.3036912"

        fun setting(jvmProperty: String, iosVariable: String): String =
            testSetting(jvmProperty, iosVariable) ?: fail("${testSettingSource(jvmProperty, iosVariable)} is not set")
    }
}
