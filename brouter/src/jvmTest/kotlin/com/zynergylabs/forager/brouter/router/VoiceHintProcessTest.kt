package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.mapaccess.MatchedWaypoint
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Step 7, session 21 (RECORD -113): `processVoiceHints`' hint body, `startSection` and `VoiceHintProcessor` as one
 * unit, through `processVoiceHints`, against the oracle's. Tracks come from `TrackEngines`' walks from Forager's own
 * ends. Hints are off on Forager's path (the head returns), so each case switches them on in test code on both
 * engines alike: `hasDirectRouting`, or a detour map registered over the track's own node ids with elements of the
 * walk's group (so `startSection` finds sections), plus seeded `turnInstructionMode`, catching range, roundabouts,
 * transport mode and matched waypoints. Every Java field of every resulting hint is compared by Java's name, in order;
 * messages by their index in the group's elements, other values by digest, so no position appears in a failure (E1).
 */
class VoiceHintProcessTest {
    private val oHint = Class.forName("btools.router.VoiceHint")
    private val oMsg = Class.forName("btools.router.MessageData")
    private val hintFields = oHint.declaredFields.filter { !java.lang.reflect.Modifier.isStatic(it.modifiers) }
        .onEach { it.isAccessible = true }
    private val msgFields = oMsg.declaredFields.filter { !java.lang.reflect.Modifier.isStatic(it.modifiers) }
        .onEach { it.isAccessible = true }
    private val oCmd = oHint.getDeclaredField("cmd").also { it.isAccessible = true }
    private val oBad = oHint.getDeclaredField("badWays").also { it.isAccessible = true }

    private fun portField(p: Any, name: String): Any? =
        p.javaClass.getDeclaredField(name).also { it.isAccessible = true }.get(p)

    private fun digest(s: String): String =
        java.security.MessageDigest.getInstance("SHA-256").digest(s.toByteArray()).take(8)
            .joinToString("") { "%02x".format(it) }

    /** A value as both engines can agree on it: floats by bits, messages by index in the group or by their fields. */
    private fun view(v: Any?, msgs: List<Any?>, port: Boolean): String = when {
        v == null -> "null"
        v is Float -> "f${v.toRawBits()}"
        v is Double -> "d${v.toRawBits()}"
        v is List<*> -> v.joinToString(",", "[", "]") { view(it, msgs, port) }
        v.javaClass == oMsg || v is MessageData -> {
            val i = msgs.indexOfFirst { it === v }
            if (i >= 0) "msg#$i" else "msg{" + msgFields.joinToString(";") {
                it.name + "=" + view(if (port) portField(v, it.name) else it.get(v), msgs, port)
            } + "}"
        }
        else -> v.toString()
    }

    /** A hint's fields, by Java's names; each value digested, so the failure message never holds a position. */
    private fun hintView(h: Any, msgs: List<Any?>, port: Boolean): List<Pair<String, String>> =
        hintFields.map { f -> f.name to digest(view(if (port) portField(h, f.name) else f.get(h), msgs, port)) }

    private fun res(f: () -> Unit): String = try {
        f(); "ok"
    } catch (e: Throwable) {
        "throws ${e.javaClass.simpleName}"
    }

    private class Outcome(val result: String, val hints: List<List<Pair<String, String>>>)

    @Test
    fun processVoiceHintsMatchesTheOracle() {
        assertEquals(16, hintFields.size, "VoiceHint's field count is not Java's")
        val fcx = PathEngines.contexts(TrackEngines.foragerProfile())!!
        try {
            val walks = TrackEngines.cases().withIndex().flatMap { (ci, c) -> TrackEngines.walks(fcx, c, 7401L + ci, 10, 25) }
            val groups = walks.size / 5
            assertTrue(groups >= 4, "too few walks: ${walks.size}")
            val rnd = Random(20261121L)
            val ranges = listOf(40.0, 0.0, 10.0, 100.0)
            val oracle = ArrayList<Outcome>()
            val port = ArrayList<Outcome>()
            val ids = ArrayList<String>()
            val cmds = HashMap<Int, Int>()
            var badWays = 0
            var registered = 0
            var waypoints = 0
            for (g in 0 until groups) {
                val ws = walks.subList(g * 5, g * 5 + 5)
                val built = ws.map { TrackEngines.tracks(it) }
                val oAll = built.flatMap { it.first.nodes }
                val pAll = built.flatMap { it.second.nodes }
                val oMsgs = oAll.map { TrackEngines.field(it, "message") }
                val pMsgs = pAll.map { TrackEngines.field(it, "message") }
                for (j in 0 until 5) {
                    val i = g * 5 + j
                    val id = "case $i"
                    val (ot, pt) = TrackEngines.tracks(ws[j])
                    val kind = i % 4
                    // Switch hints on, alike on both engines: direct routing (kinds 0, 3) or a detour map (1, 2, 3).
                    val direct = kind == 0 || kind == 3
                    fcx.o.hasDirectRouting = direct; fcx.p.hasDirectRouting = direct
                    val mode = rnd.nextInt(0, 10)
                    fcx.o.turnInstructionMode = mode; fcx.p.turnInstructionMode = mode
                    val range = if (rnd.nextInt(5) == 0) rnd.nextDouble(0.0, 200.0) else ranges[rnd.nextInt(ranges.size)]
                    fcx.o.turnInstructionCatchingRange = range; fcx.p.turnInstructionCatchingRange = range
                    val round = rnd.nextBoolean()
                    fcx.o.turnInstructionRoundabouts = round; fcx.p.turnInstructionRoundabouts = round
                    val car = rnd.nextInt(3) == 0
                    val bike = !car && rnd.nextBoolean()
                    fcx.o.carMode = car; fcx.p.carMode = car
                    fcx.o.bikeMode = bike; fcx.p.bikeMode = bike
                    if (kind != 0) {
                        val (osrc, psrc) = if (kind == 2) TrackEngines.tracks(ws[j]) else ot to pt
                        val own = ot.nodes.map { it.idFromPos }
                        val n = rnd.nextInt(1, 40)
                        repeat(n) {
                            val k = own[rnd.nextInt(own.size)]
                            val e = if (rnd.nextInt(10) == 0) -1 else rnd.nextInt(oAll.size)
                            osrc.registerDetourForId(k, if (e < 0) null else oAll[e] as btools.router.OsmPathElement)
                            psrc.registerDetourForId(k, if (e < 0) null else pAll[e] as OsmPathElement)
                        }
                        registered += n
                        if (kind == 2) { ot.copyDetours(osrc); pt.copyDetours(psrc) } // frozen, as appendTrack leaves it
                    }
                    if (rnd.nextInt(3) != 0) {
                        val k = rnd.nextInt(0, 6)
                        val idx = List(k) { rnd.nextInt(-1, ot.nodes.size + 1) }
                        val type = List(k) { (rnd.nextInt(1, 4)).toByte() }
                        val ow = idx.indices.map { q -> btools.mapaccess.MatchedWaypoint().also { it.indexInTrack = idx[q]; it.wpttype = type[q] } }
                        val pw = idx.indices.map { q -> MatchedWaypoint().also { it.indexInTrack = idx[q]; it.wpttype = type[q] } }
                        btools.router.OsmTrack::class.java.getDeclaredField("matchedWaypoints").also { it.isAccessible = true }.set(ot, ArrayList(ow))
                        pt.matchedWaypoints = ArrayList(pw)
                        waypoints += k
                    }
                    val or = res { ot.processVoiceHints(fcx.o) }
                    @Suppress("UNCHECKED_CAST")
                    val oh = (ot.voiceHints?.let { TrackEngines.field(it, "list") } as List<Any>?) ?: emptyList()
                    if (or == "ok") for (h in oh) {
                        cmds.merge(oCmd.get(h) as Int, 1, Int::plus)
                        if ((oBad.get(h) as List<*>?)?.isNotEmpty() == true) badWays++
                    }
                    oracle += Outcome(or, oh.map { hintView(it, oMsgs, false) })
                    val pr = res { pt.processVoiceHints(fcx.p) }
                    port += Outcome(pr, (pt.voiceHints?.list ?: emptyList()).map { hintView(it, pMsgs, true) })
                    ids += id
                }
            }
            val hints = oracle.sumOf { it.hints.size }
            val thrown = oracle.count { it.result != "ok" }
            println("7 process voice hints: ${ids.size} cases, $registered detours registered, $waypoints waypoints, " +
                "$hints hints, $badWays with bad ways, $thrown thrown, commands ${cmds.toSortedMap()}")
            // Coverage, on the oracle's side alone: the sample reaches the body and makes hints of several kinds.
            assertTrue(hints > 200 && cmds.size >= 5 && badWays > 20, "too little reached: $hints hints, $badWays, ${cmds.keys}")
            for (k in ids.indices) {
                val o = oracle[k]
                val p = port[k]
                assertEquals(o.result, p.result, "${ids[k]}: result")
                assertEquals(o.hints.size, p.hints.size, "${ids[k]}: hint count")
                for (h in o.hints.indices) {
                    for (f in o.hints[h].indices) {
                        assertEquals(o.hints[h][f], p.hints[h][f], "${ids[k]}: hint $h, ${o.hints[h][f].first}")
                    }
                }
            }
        } finally {
            fcx.release()
        }
    }
}
