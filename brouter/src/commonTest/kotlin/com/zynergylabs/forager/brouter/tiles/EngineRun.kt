package com.zynergylabs.forager.brouter.tiles

import com.zynergylabs.forager.brouter.router.OsmTrack
import com.zynergylabs.forager.brouter.router.RoutingContext
import com.zynergylabs.forager.brouter.router.RoutingEngine
import com.zynergylabs.forager.brouter.router.RoutingParamCollector
import com.zynergylabs.forager.brouter.util.LibmSeam
import com.zynergylabs.forager.brouter.util.MsunLibm
import okio.FileSystem
import okio.Path

/**
 * Step 10, session 46 (RECORD -138, decision 1): the engine driven in common code as jvmTest's `RoutingEngineF0Test.port`
 * drives it, with `MsunLibm` (the public call's maths) in place of the oracle's, so the six fields of jvmTest's `E3Outcome`
 * (kind, nodes, cost, distance, the node digest, the heights digest) are read on iOS too. `route()` is not used: its
 * `Found` carries no cost (RECORD -138).
 */
internal object EngineRun {
    /** jvmTest's `E3Outcome`, its six fields; [toField] is `E3Outcome.toField`. */
    data class Outcome(
        val kind: String, val nodes: Int, val cost: Int, val distance: Int, val nodeDigest: String, val heightsDigest: String,
    ) {
        fun toField(): String = "$kind|$nodes|$cost|$distance|$nodeDigest|$heightsDigest"

        /** The tuple digest, as `E3Expected.row` computes it: [fnv1aText] over [toField]. */
        val tupleDigest: String get() = fnv1aText(toField())
    }

    /**
     * `port(c, base)`: [lonLats] is the waypoint text `"startLon,startLat|endLon,endLat"` (the JVM writes it from the
     * case's doubles; here it is passed as text, so both platforms parse the same characters), [profile] the `.brf` file,
     * [segments] the tiles folder. `doRun(maxRunningTimeMillis)`, `quite`. An Exception gives `threw:`, as `port`'s.
     */
    fun run(lonLats: String, profile: Path, segments: Path, memoryclass: Int = 64, maxRunningTimeMillis: Long = 0L): Outcome =
        runTrack(lonLats, profile, segments, memoryclass, maxRunningTimeMillis).outcome

    /** [run]'s outcome with the found track and the context it ran with (both null when it threw). */
    class TrackRun(val outcome: Outcome, val track: OsmTrack?, val rc: RoutingContext?)

    /**
     * [run], keeping the track and the context. Session 55: [turnInstructionMode], when given, is written to the context
     * after `setParams` (which refuses `timode`), as the oracle side of `HintsFormatsRun` (jvmTest) does.
     * Session 57 (RECORD -150): [libm] is the context's library, `MsunLibm` (the port as it ships, a production run) unless
     * a jvmTest passes `OracleLibm` (a logic run).
     */
    fun runTrack(
        lonLats: String, profile: Path, segments: Path, memoryclass: Int = 64, maxRunningTimeMillis: Long = 0L,
        turnInstructionMode: Int? = null, libm: LibmSeam = MsunLibm,
    ): TrackRun = try {
        val collector = RoutingParamCollector()
        val waypoints = collector.getWayPointList(lonLats)
        val rc = RoutingContext(libm)
        val params = mapOf(
            "profile" to profile.toString(),
            "alternativeidx" to "0",
            "profile:use_dynamic_range" to "0",
        )
        collector.setParams(rc, waypoints, params)
        rc.memoryclass = memoryclass
        if (turnInstructionMode != null) rc.turnInstructionMode = turnInstructionMode
        val engine = RoutingEngine(FileSystem.SYSTEM, segments, waypoints, rc, RoutingEngine.BROUTER_ENGINEMODE_ROUTING)
        engine.quite = true
        engine.doRun(maxRunningTimeMillis)
        val message = engine.getErrorMessage()
        val track = engine.getFoundTrack()
        val lon = IntArray(track.nodes.size) { track.nodes[it].getILon() }
        val lat = IntArray(track.nodes.size) { track.nodes[it].getILat() }
        val heights = ShortArray(track.nodes.size) { track.nodes[it].getSElev() }
        TrackRun(Outcome(kindOf(message), lon.size, track.cost, track.distance, fnv1a(lon, lat), fnv1aShorts(heights)), track, rc)
    } catch (e: Exception) {
        TrackRun(
            Outcome("threw:" + (e.message ?: e::class.simpleName), 0, -1, -1, fnv1a(IntArray(0), IntArray(0)), fnv1aShorts(ShortArray(0))),
            null, null,
        )
    }

    /** `E3Outcome.kindOf`: the timeout and watchdog messages by family; every other message exactly. */
    fun kindOf(message: String?): String = when {
        message == null -> "route"
        Regex(".* timeout after \\d+ seconds").matches(message) -> "family:timeout"
        message.startsWith("operation killed by thread-priority-watchdog after ") -> "family:watchdog"
        else -> message.replace('\t', ' ').replace('\n', ' ')
    }

    private const val OFFSET = -0x340d631b7bdddcdbL // 0xcbf29ce484222325
    private const val PRIME = 0x100000001b3L

    /** `E3Outcome.fnv1a`: 64-bit FNV-1a over each (lon, lat), each int's four bytes low first; unpadded hex. */
    fun fnv1a(lon: IntArray, lat: IntArray): String {
        var h = OFFSET
        fun mix(v: Int) {
            for (shift in 0 until 32 step 8) {
                h = h xor ((v ushr shift) and 0xff).toLong()
                h *= PRIME
            }
        }
        for (i in lon.indices) {
            mix(lon[i])
            mix(lat[i])
        }
        return h.toULong().toString(16)
    }

    /** `E3Outcome.fnv1aShorts`: each short's two bytes low first. */
    fun fnv1aShorts(heights: ShortArray): String {
        var h = OFFSET
        for (s in heights) {
            val v = s.toInt()
            for (shift in 0 until 16 step 8) {
                h = h xor ((v ushr shift) and 0xff).toLong()
                h *= PRIME
            }
        }
        return h.toULong().toString(16)
    }

    /** `E3Expected.fnv1aText`: 64-bit FNV-1a over [s]'s UTF-8 bytes; unpadded hex, as `java.lang.Long.toHexString`. */
    fun fnv1aText(s: String): String {
        var h = OFFSET
        for (b in s.encodeToByteArray()) {
            h = h xor (b.toLong() and 0xff)
            h *= PRIME
        }
        return h.toULong().toString(16)
    }
}
