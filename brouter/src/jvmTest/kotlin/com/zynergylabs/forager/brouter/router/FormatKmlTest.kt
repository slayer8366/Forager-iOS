package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.mapaccess.MatchedWaypoint
import com.zynergylabs.forager.brouter.mapaccess.OsmNode
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Step 7, session 24 (RECORD -111, "Port them"): `FormatKml` whole against the oracle's. Tracks are `TrackEngines`'
 * walks from Forager's own ends (E1's exception, RECORD -93). Each is given alike in both engines a seeded export
 * setup: `exportWaypoints` and `exportCorrectedWaypoints` on or off, 0 to 6 POIs and 1 to 5 matched waypoints placed
 * at the track's own node positions, corrected points present or not, and names that need XML escaping. On Forager's
 * path no POI or waypoint export is set, so this is switched on in test code. A thrown result (a null name) is
 * compared by exception class only, the message being each runtime's own (RECORD -116). Texts are compared by digest
 * and length only (E1).
 */
class FormatKmlTest {
    private fun digest(s: String): String =
        java.security.MessageDigest.getInstance("SHA-256").digest(s.toByteArray()).joinToString("") { "%02x".format(it) }
            .take(16) + "/" + s.length

    private fun res(f: () -> String): String = try {
        f()
    } catch (e: Throwable) {
        // A stub's message names it; any other exception is compared by class only (its message is the runtime's own).
        "throws " + e.javaClass.name + (if (e is NotImplementedError) ": " + e.message else "")
    }

    private fun setField(o: Any, name: String, v: Any?) {
        var c: Class<*>? = o.javaClass
        while (c != null) {
            val f = c.declaredFields.firstOrNull { it.name == name }
            if (f != null) { f.isAccessible = true; f.set(o, v); return }
            c = c.superclass
        }
        error("no field $name")
    }

    @Test
    fun formatKmlMatchesTheOracle() {
        val fcx = PathEngines.contexts(TrackEngines.foragerProfile())!!
        try {
            val rnd = Random(20261133L)
            val names = listOf("", "start", "a&b", "<via>", "it's \"x\"", "tab\there\nnl\r", "äöü€中", null)
            val walks = TrackEngines.cases().withIndex().flatMap { (ci, c) -> TrackEngines.walks(fcx, c, 7701L + ci, 6, 50) }
            val pairs = walks.map { TrackEngines.tracks(it) } + listOf(btools.router.OsmTrack() to OsmTrack())
            var placemarks = 0
            var thrown = 0
            for ((i, pr) in pairs.withIndex()) {
                val (ot, pt) = pr
                val pos = pt.nodes.map { it.getILon() to it.getILat() }.ifEmpty { listOf(0 to 0) }
                fun at() = pos[rnd.nextInt(pos.size)]
                fun name() = if (rnd.nextInt(30) == 0) null else names[rnd.nextInt(names.size - 1)]
                if (i % 4 != 0) {
                    repeat(rnd.nextInt(0, 7)) {
                        val (x, y) = at(); val nm = name()
                        val op = btools.router.OsmNodeNamed(btools.mapaccess.OsmNode(x, y)).also { it.name = nm }
                        val pp = OsmNodeNamed(OsmNode(x, y)).also { it.name = nm }
                        (TrackEngines.field(ot, "pois") as MutableList<Any>).add(op); pt.pois.add(pp)
                    }
                    val ow = ArrayList<btools.mapaccess.MatchedWaypoint>()
                    val pw = ArrayList<MatchedWaypoint>()
                    repeat(rnd.nextInt(1, 6)) {
                        val (x, y) = at(); val nm = name()
                        val o = btools.mapaccess.MatchedWaypoint(); val p = MatchedWaypoint()
                        o.waypoint = btools.mapaccess.OsmNode(x, y); p.waypoint = OsmNode(x, y)
                        o.name = nm; p.name = nm
                        if (rnd.nextBoolean()) { val (cx, cy) = at(); o.correctedpoint = btools.mapaccess.OsmNode(cx, cy); p.correctedpoint = OsmNode(cx, cy) }
                        ow += o; pw += p
                    }
                    setField(ot, "matchedWaypoints", ow); pt.matchedWaypoints = pw
                    val ew = rnd.nextBoolean(); val ec = rnd.nextBoolean()
                    setField(ot, "exportWaypoints", ew); pt.exportWaypoints = ew
                    setField(ot, "exportCorrectedWaypoints", ec); pt.exportCorrectedWaypoints = ec
                }
                val o = res { btools.router.FormatKml(fcx.o).format(ot) }
                val p = res { FormatKml(fcx.p).format(pt) }
                if (o.startsWith("throws") || p.startsWith("throws")) {
                    assertEquals(if (o.startsWith("throws")) o else digest(o), if (p.startsWith("throws")) p else digest(p), "track $i: format")
                    thrown++
                } else {
                    assertEquals(digest(o), digest(p), "track $i: format")
                    placemarks += Regex("<Placemark>").findAll(o).count()
                }
            }
            println("7 FormatKml: ${pairs.size} tracks, $placemarks placemarks, $thrown thrown")
            assertTrue(placemarks > 100, "too few placemarks: $placemarks")
        } finally {
            fcx.release()
        }
    }
}
