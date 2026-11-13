package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.mapaccess.MatchedWaypoint
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Step 7, session 20: `OsmTrack`'s detour methods (`registerDetourForId`, `copyDetours`, `addDetours`, `appendDetours`,
 * `getFromDetourMap`, and `appendTrack`'s detour branch) and the small hint helpers (`getMinDistance`,
 * `getMatchedWaypoint`, `removeVoiceHint`) against the oracle's (RECORD -111). Not reached on Forager's path: there is
 * no via and `turnInstructionMode` is 0 (RECORD -110), so the detours are registered here in test code, on both engines
 * alike. Tracks are `TrackEngines`' seeded walks from Forager's own ends; a detour map is compared by the chain each
 * candidate id gives, as indices into the case's elements by identity, so no position is compared or written.
 */
class TrackDetourTest {
    private class Case(val o: List<btools.router.OsmTrack>, val p: List<OsmTrack>, val oAll: List<Any>, val pAll: List<Any>, val ids: List<Long>)

    private fun res(f: () -> Any?): String = try {
        "v${f()}"
    } catch (e: Throwable) {
        "throws ${e.javaClass.simpleName}"
    }

    private fun chain(h: Any?, all: List<Any>): List<Int>? {
        if (h == null) return null
        val out = ArrayList<Int>()
        var x: Any? = h
        while (x != null) {
            out += TrackEngines.indexOf(all, TrackEngines.field(x, "node"))
            x = TrackEngines.field(x, "nextHolder")
        }
        return out
    }

    private fun mapKind(t: Any): String = TrackEngines.field(t, "detourMap")?.javaClass?.simpleName ?: "null"

    private fun compareMaps(c: Case, ot: btools.router.OsmTrack, pt: OsmTrack, id: String): Int {
        assertEquals(mapKind(ot), mapKind(pt), "$id: the detour map's kind")
        var found = 0
        for (k in c.ids) {
            val oc = chain(ot.getFromDetourMap(k), c.oAll)
            assertEquals(oc, chain(pt.getFromDetourMap(k), c.pAll), "$id: getFromDetourMap, candidate ${c.ids.indexOf(k)}")
            if (oc != null) found++
        }
        return found
    }

    private fun register(rnd: Random, c: Case, ot: btools.router.OsmTrack, pt: OsmTrack, count: Int) {
        repeat(count) {
            val k = c.ids[rnd.nextInt(c.ids.size)]
            val e = if (rnd.nextInt(10) == 0) -1 else rnd.nextInt(c.oAll.size)
            ot.registerDetourForId(k, if (e < 0) null else c.oAll[e] as btools.router.OsmPathElement)
            pt.registerDetourForId(k, if (e < 0) null else c.pAll[e] as OsmPathElement)
        }
    }

    private fun originView(nodes: List<Any>, all: List<Any>): List<Int> =
        nodes.map { TrackEngines.indexOf(all, TrackEngines.field(it, "origin")) }

    @Test
    fun detourMethodsMatchTheOracle() {
        val fcx = PathEngines.contexts(TrackEngines.foragerProfile())!!
        val walks = try {
            TrackEngines.cases().withIndex().flatMap { (ci, c) -> TrackEngines.walks(fcx, c, 7301L + ci, 10, 25) }
        } finally {
            fcx.release()
        }
        val groups = walks.size / 5
        assertTrue(groups >= 4, "too few walks: ${walks.size}")
        val rnd = Random(20261113L)
        var found = 0
        var thrown = 0
        var registered = 0
        val outcomes = HashMap<String, Int>()
        for (g in 0 until groups) {
            val ws = walks.subList(g * 5, g * 5 + 5)
            val built = ws.map { TrackEngines.tracks(it) }
            val oAll = built.flatMap { it.first.nodes }
            val pAll = built.flatMap { it.second.nodes }
            val ids = (oAll.map { it.idFromPos } + List(4) { oAll[it].idFromPos xor (1L shl (it + 3)) }).distinct()
            val c = Case(built.map { it.first }, built.map { it.second }, oAll, pAll, ids)
            val id = "group $g"
            // registerDetourForId on fresh tracks, then copyDetours into a third.
            val (oa, pa) = TrackEngines.tracks(ws[0])
            assertEquals(null, chain(oa.getFromDetourMap(ids[0]), oAll), "$id: no map")
            assertEquals(null, pa.getFromDetourMap(ids[0]), "$id: no map")
            val n = rnd.nextInt(1, 40)
            register(rnd, c, oa, pa, n)
            registered += n
            found += compareMaps(c, oa, pa, "$id registered")
            val (od, pd) = TrackEngines.tracks(ws[0])
            od.copyDetours(oa); pd.copyDetours(pa)
            // a second copy from the map just emptied: recorded, so both engines' outcome (an NPE) shows in the log
            run { val ro = res { btools.router.OsmTrack().copyDetours(oa) }; val rp = res { OsmTrack().copyDetours(pa) }; outcomes.merge("second copy o=$ro p=$rp", 1, Int::plus) }
            found += compareMaps(c, od, pd, "$id copied")
            // addDetours: onto a frozen map, from a source with a frozen map and a built nodes map; and the edge cases.
            for (variant in 0 until 5) {
                val vid = "$id addDetours variant $variant"
                val (ot, pt) = TrackEngines.tracks(ws[0])
                when (variant) {
                    0, 1, 2 -> { // a fresh map per copy: Java's FrozenLongMap empties the map it is built from, so one source cannot be copied twice
                        val (ox, px) = TrackEngines.tracks(ws[0]); register(rnd, c, ox, px, n); ot.copyDetours(ox); pt.copyDetours(px) }
                    3 -> register(rnd, c, ot, pt, 3) // a map that is not frozen
                    else -> {}
                }
                val (os, ps) = TrackEngines.tracks(ws[1])
                if (variant != 2) { os.buildMap(); ps.buildMap() }
                if (variant != 1) {
                    val (osr, psr) = TrackEngines.tracks(ws[1])
                    register(rnd, c, osr, psr, rnd.nextInt(1, 30))
                    os.copyDetours(osr); ps.copyDetours(psr)
                }
                val r = res { ot.addDetours(os) }
                assertEquals(r, res { pt.addDetours(ps) }, "$vid: result")
                if (r.startsWith("throws")) thrown++
                outcomes.merge("add $variant $r", 1, Int::plus)
                found += compareMaps(c, ot, pt, vid)
            }
            // appendTrack's detour branch (no hints): the first takes the source's map, the second adds to it.
            run {
                val (o1, p1) = TrackEngines.tracks(ws[2])
                val (o2, p2) = TrackEngines.tracks(ws[3])
                val (o3, p3) = TrackEngines.tracks(ws[4])
                for ((ox, px) in listOf(o2 to p2, o3 to p3)) {
                    ox.buildMap(); px.buildMap()
                    val (or, pr) = TrackEngines.tracks(ws[1])
                    register(rnd, c, or, pr, rnd.nextInt(1, 20))
                    ox.copyDetours(or); px.copyDetours(pr)
                }
                val ot = btools.router.OsmTrack()
                val pt = OsmTrack()
                for ((step, pair) in listOf(o1 to p1, o2 to p2, o3 to p3).withIndex()) {
                    val r = res { ot.appendTrack(pair.first) }
                    assertEquals(r, res { pt.appendTrack(pair.second) }, "$id appendTrack $step: result")
                    outcomes.merge("appendTrack $step $r", 1, Int::plus)
                    found += compareMaps(c, ot, pt, "$id appendTrack $step")
                }
            }
            // appendDetours, last, since it sets the origin of a source's first element: twice, so lastorigin is used.
            run {
                val (ot, pt) = TrackEngines.tracks(ws[0])
                if (g % 3 == 1) register(rnd, c, ot, pt, 5)
                // a fresh source map, as above: copyDetours empties the one it freezes
                if (g % 3 == 2) { val (ox, px) = TrackEngines.tracks(ws[0]); register(rnd, c, ox, px, n); ot.copyDetours(ox); pt.copyDetours(px) }
                for (s in 1..2) {
                    val (os, ps) = TrackEngines.tracks(ws[s])
                    if (s == 1 || g % 2 == 0) register(rnd, c, os, ps, rnd.nextInt(1, 10))
                    val r = res { ot.appendDetours(os) }
                    assertEquals(r, res { pt.appendDetours(ps) }, "$id appendDetours $s: result")
                    if (r.startsWith("throws")) thrown++
                    outcomes.merge("appendDetours $s $r", 1, Int::plus)
                    assertEquals(TrackEngines.indexOf(oAll, TrackEngines.field(ot, "lastorigin")), TrackEngines.indexOf(pAll, pt.lastorigin), "$id appendDetours $s: lastorigin")
                    assertEquals(originView(os.nodes, oAll), originView(ps.nodes, pAll), "$id appendDetours $s: origins")
                    found += compareMaps(c, ot, pt, "$id appendDetours $s")
                }
            }
        }
        println("7 detours: $groups groups, ${walks.size} walks, $registered registered, $found chains found, $thrown thrown, outcomes ${outcomes.toSortedMap()}")
        assertTrue(found > 1000, "too few chains compared: $found")
        assertTrue(outcomes.keys.any { it.startsWith("add 0 v") } && outcomes.keys.any { it.startsWith("add 3 throws") },
            "addDetours' merge and its cast were not both reached: ${outcomes.keys}")
    }

    @Test
    fun hintHelpersMatchTheOracle() {
        val rnd = Random(20261114L)
        val oMin = btools.router.OsmTrack::class.java.getDeclaredMethod("getMinDistance").also { it.isAccessible = true }
        val oHint = Class.forName("btools.router.VoiceHint")
        val oIndex = oHint.getDeclaredField("indexInTrack").also { it.isAccessible = true }
        @Suppress("UNCHECKED_CAST")
        fun oList(t: btools.router.OsmTrack) = TrackEngines.field(t.voiceHints, "list") as MutableList<Any>
        var removed = 0
        var matched = 0
        for (i in 0 until 400) {
            val id = "case $i"
            val ot = btools.router.OsmTrack()
            val pt = OsmTrack()
            if (i % 5 != 0) {
                ot.voiceHints = btools.router.VoiceHintList()
                pt.voiceHints = VoiceHintList()
                val mode = if (i % 7 == 0) rnd.nextInt() else rnd.nextInt(-1, 5)
                ot.voiceHints.setTransportMode(mode)
                pt.voiceHints!!.setTransportMode(mode)
                assertEquals(oMin.invoke(ot), pt.getMinDistance(), "$id: getMinDistance, mode $mode")
                val k = rnd.nextInt(0, 8)
                val idx = List(k) { rnd.nextInt(0, 6) }
                for (x in idx) {
                    val oh = oHint.getDeclaredConstructor().also { it.isAccessible = true }.newInstance()
                    oIndex.set(oh, x)
                    oList(ot).add(oh)
                    pt.voiceHints!!.list.add(VoiceHint().also { it.indexInTrack = x })
                }
                val oOrig = ArrayList(oList(ot))
                val pOrig = ArrayList(pt.voiceHints!!.list)
                repeat(3) {
                    val r = rnd.nextInt(-1, 7)
                    ot.removeVoiceHint(r)
                    pt.removeVoiceHint(r)
                    val ov = oList(ot).map { TrackEngines.indexOf(oOrig, it) }
                    assertEquals(ov, pt.voiceHints!!.list.map { TrackEngines.indexOf(pOrig, it) }, "$id: removeVoiceHint($r)")
                }
                removed += idx.size - oList(ot).size
            } else {
                assertEquals(oMin.invoke(ot), pt.getMinDistance(), "$id: getMinDistance with no hints")
                ot.removeVoiceHint(0)
                pt.removeVoiceHint(0)
                assertEquals(null, pt.voiceHints, "$id: removeVoiceHint made hints")
            }
            if (i % 4 != 0) {
                val k = rnd.nextInt(0, 6)
                val idx = List(k) { rnd.nextInt(0, 8) }
                val ow = idx.map { x -> btools.mapaccess.MatchedWaypoint().also { it.indexInTrack = x } }
                val pw = idx.map { x -> MatchedWaypoint().also { it.indexInTrack = x } }
                btools.router.OsmTrack::class.java.getDeclaredField("matchedWaypoints").also { it.isAccessible = true }.set(ot, ArrayList(ow))
                pt.matchedWaypoints = ArrayList(pw)
            }
            for (q in -1..8) {
                val o = TrackEngines.indexOf((TrackEngines.field(ot, "matchedWaypoints") as List<*>?) ?: emptyList<Any>(), ot.getMatchedWaypoint(q))
                val p = TrackEngines.indexOf(pt.matchedWaypoints ?: emptyList<Any>(), pt.getMatchedWaypoint(q))
                assertEquals(o, p, "$id: getMatchedWaypoint($q)")
                if (o >= 0) matched++
            }
        }
        println("7 hint helpers: 400 cases, $removed hints removed, $matched waypoints matched")
        assertTrue(removed > 50 && matched > 50, "too little reached: removed $removed, matched $matched")
    }
}
