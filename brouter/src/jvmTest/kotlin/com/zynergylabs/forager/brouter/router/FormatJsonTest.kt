package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.mapaccess.MatchedWaypoint
import com.zynergylabs.forager.brouter.mapaccess.OsmNode
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Step 7, session 25 (RECORD -111, "Port them"): `FormatJson` whole against the oracle's, with
 * `OsmTrack.aggregateSpeedProfile` (and its `getVNode`). Tracks are `TrackEngines`' walks from Forager's own ends (E1's
 * exception, RECORD -93), as in `FormatKmlTest`. Each is given alike in both engines a seeded setup that Forager's
 * path never sets, switched on in test code: voice hints through `processVoiceHints` under every
 * `turnInstructionMode` 0 to 9 (4 and 9 write each hint's geometry), `showspeed`, `showSpeedProfile`, an itinerary,
 * POIs, matched and corrected waypoints of every type with the export flags, and names that need JSON escaping. A
 * thrown result is compared by exception class only, the message being each runtime's own (RECORD -116); a stub's
 * message is kept so that it names the stub. Texts are compared by digest and length only (E1).
 */
class FormatJsonTest {
    /** RECORD -118: the port's `OsmTrack.version` is `null` by default; set from the oracle's own value at run time. */
    @kotlin.test.BeforeTest
    fun setVersion() {
        OsmTrack.version = btools.router.OsmTrack.version
    }

    private fun digest(s: String): String =
        java.security.MessageDigest.getInstance("SHA-256").digest(s.toByteArray()).joinToString("") { "%02x".format(it) }
            .take(16) + "/" + s.length

    private fun res(f: () -> String): String = try {
        f()
    } catch (e: Throwable) {
        "throws " + e.javaClass.name + (if (e is NotImplementedError) ": " + e.message else "")
    }

    private fun same(o: String, p: String, what: String) =
        assertEquals(if (o.startsWith("throws")) o else digest(o), if (p.startsWith("throws")) p else digest(p), what)

    private fun setField(o: Any, name: String, v: Any?) {
        var c: Class<*>? = o.javaClass
        while (c != null) {
            val f = c.declaredFields.firstOrNull { it.name == name }
            if (f != null) { f.isAccessible = true; f.set(o, v); return }
            c = c.superclass
        }
        error("no field $name")
    }

    /** Way and node description bytes from the oracle's graph around each case's ends (both engines get copies). */
    private fun descriptions(fcx: PathEngines.Contexts): List<ByteArray> {
        val out = ArrayList<ByteArray>()
        for (c in TrackEngines.cases()) {
            val og = PathEngines.OracleGraph(c.segments, fcx.o, PathEngines.waypoints(c), true)
            try {
                for (n in og.nodes) {
                    n.nodeDescription?.let { out += it.copyOf() }
                    var l = n.firstlink
                    while (l != null) {
                        l.descriptionBitmap?.let { out += it.copyOf() }
                        l = l.getNext(n)
                    }
                }
            } finally {
                og.close()
            }
        }
        return out
    }

    @Test
    fun formatDecimal3MatchesJavasDecimalFormat() {
        val df = java.text.NumberFormat.getInstance(java.util.Locale.ENGLISH) as java.text.DecimalFormat
        df.applyPattern("0.###")
        val rnd = Random(20261151L)
        val fixed = listOf(0f, -0f, 0.0005f, 0.0015f, 0.0025f, -0.0005f, 0.1f, 1.2345f, 2.5f, 999.9995f, 1e-3f, 1e-4f,
            -1e-4f, 16777216f, 1e10f, 3e17f, 12345678e12f, Float.MAX_VALUE, -Float.MAX_VALUE, Float.MIN_VALUE,
            Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY)
        val vs = fixed + List(100000) { Float.fromBits(rnd.nextInt()) } +
            List(100000) { rnd.nextInt(-2000000, 20000000) / 1000f + rnd.nextInt(-3, 4) * 0.0005f } +
            List(50000) { (rnd.nextInt(0, 1 shl 20) / 1024f) }
        // RECORD -119 (the owner's ruling, "stop at the limit"): every tested value below 2^63 in magnitude is compared
        // with Java exactly; every finite tested value at or above 2^63 must throw the port's UnsupportedOperationException
        // naming the limit and RECORD -119, never digits. Infinities print no digits and stay compared with Java.
        val limit = Math.scalb(1.0, 63) // 2^63, exact as a double
        var n = 0
        var over = 0
        for (v in vs) {
            if (v.isFinite() && kotlin.math.abs(v.toDouble()) >= limit) {
                val e = assertFailsWith<UnsupportedOperationException>("bits ${v.toRawBits()}") { FormatJson.formatDecimal3(v) }
                assertTrue(e.message!!.contains("2^63") && e.message!!.contains("RECORD -119"), "bits ${v.toRawBits()}: ${e.message}")
                over++
            } else {
                assertEquals(df.format(v.toDouble()), res { FormatJson.formatDecimal3(v) }, "bits ${v.toRawBits()}")
                n++
            }
        }
        assertTrue(n > 0 && over > 0, "both sides of the -119 limit reached: $n below, $over at or above")
        println("7 FormatJson.formatDecimal3: $n floats compared with Java, $over at or above 2^63 throwing (RECORD -119)")
    }

    @Test
    fun formatJsonMatchesTheOracle() {
        val fcx = PathEngines.contexts(TrackEngines.foragerProfile())!!
        try {
            val rnd = Random(20261152L)
            val names = listOf("", "start", "a\"b", "back\\slash", "tab\there\nnl\r", "/x/", "äöü€中", "\u0001")
            val descs = descriptions(fcx)
            assertTrue(descs.size > 5, "too few descriptions: ${descs.size}")
            val walks = TrackEngines.cases().withIndex().flatMap { (ci, c) -> TrackEngines.walks(fcx, c, 7801L + ci, 12, 60) }
            val pairs = walks.map { TrackEngines.tracks(it) } + listOf(btools.router.OsmTrack() to OsmTrack())
            val reached = HashMap<String, Int>()
            fun hit(k: String) { reached[k] = (reached[k] ?: 0) + 1 }
            var hints = 0
            var waypointTexts = 0
            for ((i, pr) in pairs.withIndex()) {
                val (ot, pt) = pr
                val pos = pt.nodes.map { it.getILon() to it.getILat() }.ifEmpty { listOf(0 to 0) }
                fun at() = pos[rnd.nextInt(pos.size)]
                fun name() = if (rnd.nextInt(30) == 0) null else names[rnd.nextInt(names.size)]
                fun elev(): Short = if (rnd.nextInt(3) == 0) Short.MIN_VALUE else rnd.nextInt(-400, 36000).toShort()
                if (i % 3 != 0) {
                    // Hints on, alike on both engines, as VoiceHintProcessTest switches them (direct routing).
                    val mode = (i / 3 + i) % 10
                    fcx.o.hasDirectRouting = true; fcx.p.hasDirectRouting = true
                    fcx.o.turnInstructionMode = mode; fcx.p.turnInstructionMode = mode
                    val round = rnd.nextBoolean()
                    fcx.o.turnInstructionRoundabouts = round; fcx.p.turnInstructionRoundabouts = round
                    fcx.o.turnInstructionCatchingRange = 40.0; fcx.p.turnInstructionCatchingRange = 40.0
                    val oh = res { ot.processVoiceHints(fcx.o); "" }
                    val ph = res { pt.processVoiceHints(fcx.p); "" }
                    assertEquals(oh, ph, "track $i: processVoiceHints")
                }
                val nm = if (rnd.nextInt(4) == 0) names[rnd.nextInt(names.size)] else "unset"
                setField(ot, "name", nm); pt.name = nm
                for (f in listOf("distance", "ascend", "plainAscend", "cost", "energy")) {
                    val v = if (rnd.nextBoolean()) 0 else rnd.nextInt(-5, 1000000)
                    setField(ot, f, v)
                    when (f) { "distance" -> pt.distance = v; "ascend" -> pt.ascend = v; "plainAscend" -> pt.plainAscend = v; "cost" -> pt.cost = v; else -> pt.energy = v }
                }
                val ss = rnd.nextInt(3) == 0; val sp = rnd.nextInt(3) == 0
                setField(ot, "showspeed", ss); pt.showspeed = ss
                setField(ot, "showSpeedProfile", sp); pt.showSpeedProfile = sp
                // Speeds on the nodes' messages, alike in both engines: only the kinematic paths write them, and step 6's
                // paths keep MessageData's defaults, so without these the speed profile is always empty (session 26).
                if (sp && rnd.nextBoolean()) {
                    for (k in ot.nodes.indices) {
                        val om = TrackEngines.field(ot.nodes[k], "message") ?: continue
                        val pm = pt.nodes[k].message ?: continue
                        if (rnd.nextInt(3) != 0) continue
                        val vx = rnd.nextInt(-1, 130); val vn = rnd.nextInt(-1, 60); val ve = rnd.nextInt(-1, 130)
                        val v0 = rnd.nextInt(0, 1000); val v1 = rnd.nextInt(0, 1000); val xt = rnd.nextInt(0, 30)
                        setField(om, "vmax", vx); pm.vmax = vx
                        setField(om, "vmin", vn); pm.vmin = vn
                        setField(om, "vmaxExplicit", ve); pm.vmaxExplicit = ve
                        setField(om, "vnode0", v0); pm.vnode0 = v0
                        setField(om, "vnode1", v1); pm.vnode1 = v1
                        setField(om, "extraTime", xt); pm.extraTime = xt
                    }
                }
                if (rnd.nextInt(4) == 0) {
                    val it = List(rnd.nextInt(1, 4)) { k -> "step $k ${names[rnd.nextInt(names.size)]}" }
                    ot.iternity = ArrayList(it); pt.iternity = ArrayList(it)
                }
                if (i % 4 != 0) {
                    repeat(rnd.nextInt(0, 5)) {
                        val (x, y) = at(); val n = name(); val e = elev()
                        val op = btools.router.OsmNodeNamed(btools.mapaccess.OsmNode(x, y)).also { it.name = n; it.selev = e }
                        val pp = OsmNodeNamed(OsmNode(x, y)).also { it.name = n; it.selev = e }
                        @Suppress("UNCHECKED_CAST") (TrackEngines.field(ot, "pois") as MutableList<Any>).add(op); pt.pois.add(pp)
                    }
                    val ow = ArrayList<btools.mapaccess.MatchedWaypoint>()
                    val pw = ArrayList<MatchedWaypoint>()
                    repeat(rnd.nextInt(1, 6)) {
                        val (x, y) = at(); val n = name(); val e = elev()
                        val type = rnd.nextInt(0, 4).toByte()
                        val o = btools.mapaccess.MatchedWaypoint(); val p = MatchedWaypoint()
                        o.waypoint = btools.mapaccess.OsmNode(x, y).also { it.selev = e }; p.waypoint = OsmNode(x, y).also { it.selev = e }
                        o.name = n; p.name = n; o.wpttype = type; p.wpttype = type
                        hit("wpttype $type")
                        if (rnd.nextBoolean()) {
                            val (cx, cy) = at(); val ce = elev()
                            o.correctedpoint = btools.mapaccess.OsmNode(cx, cy).also { it.selev = ce }
                            p.correctedpoint = OsmNode(cx, cy).also { it.selev = ce }
                        }
                        ow += o; pw += p
                    }
                    setField(ot, "matchedWaypoints", ow); pt.matchedWaypoints = pw
                    val ew = rnd.nextBoolean(); val ec = rnd.nextBoolean()
                    setField(ot, "exportWaypoints", ew); pt.exportWaypoints = ew
                    setField(ot, "exportCorrectedWaypoints", ec); pt.exportCorrectedWaypoints = ec
                    if (ew) hit("exportWaypoints"); if (ec) hit("exportCorrectedWaypoints")
                    if (pt.pois.isNotEmpty()) hit("pois")
                } else hit("no pois or export")

                val osp = res { ot.aggregateSpeedProfile().joinToString("\n") }
                val psp = res { pt.aggregateSpeedProfile().joinToString("\n") }
                same(osp, psp, "track $i: aggregateSpeedProfile")
                val o = res { btools.router.FormatJson(fcx.o).format(ot) }
                val p = res { FormatJson(fcx.p).format(pt) }
                same(o, p, "track $i: format")
                if (!o.startsWith("throws")) {
                    val vh = pt.voiceHints
                    if (vh != null && vh.list.isNotEmpty()) {
                        hints += vh.list.size
                        hit("voicehints mode ${vh.turnInstructionMode}")
                    }
                    if (o.contains("\"speedprofile\"")) hit("speedprofile")
                    if (o.contains("\"times\"")) hit("times") else hit("no times")
                    if (o.contains("\"iternity\"")) hit("iternity")
                    if (pt.showspeed && pt.nodes.size > 1) hit("showspeed")
                } else hit("format thrown")

                // formatAsWaypoint on a POI-like node: no description, or way or node bytes from the graph.
                for (k in 0 until 2) {
                    val (x, y) = at(); val e = elev()
                    val d = if (rnd.nextInt(4) == 0) null else descs[rnd.nextInt(descs.size)]
                    val on = btools.router.OsmNodeNamed(btools.mapaccess.OsmNode(x, y)).also { it.selev = e; it.nodeDescription = d?.copyOf() }
                    val pn = OsmNodeNamed(OsmNode(x, y)).also { it.selev = e; it.nodeDescription = d?.copyOf() }
                    val ow = res { btools.router.FormatJson(fcx.o).formatAsWaypoint(on) }
                    val pw = res { FormatJson(fcx.p).formatAsWaypoint(pn) }
                    same(ow, pw, "track $i waypoint $k: formatAsWaypoint")
                    if (ow.startsWith("throws")) hit("waypoint thrown") else { waypointTexts++; if (ow.contains("\"message\"")) hit("waypoint message") }
                }
            }
            // A null routing context: formatAsWaypoint's catch wraps what is thrown (`FormatJson.java:208-210`).
            val on = btools.router.OsmNodeNamed(btools.mapaccess.OsmNode(1, 2)).also { it.nodeDescription = descs[0].copyOf() }
            val pn = OsmNodeNamed(OsmNode(1, 2)).also { it.nodeDescription = descs[0].copyOf() }
            same(res { btools.router.FormatJson(null).formatAsWaypoint(on) }, res { FormatJson(null).formatAsWaypoint(pn) }, "null rc: formatAsWaypoint")

            println("7 FormatJson: ${pairs.size} tracks, $hints hints, $waypointTexts waypoint texts, reached ${reached.toSortedMap()}")
            assertTrue(hints > 50, "too few hints: $hints")
            for (k in listOf("voicehints mode 4", "voicehints mode 9", "speedprofile", "times", "no times", "iternity",
                "showspeed", "pois", "exportWaypoints", "exportCorrectedWaypoints", "no pois or export", "wpttype 2",
                "wpttype 3", "wpttype 1", "waypoint message")) {
                assertTrue((reached[k] ?: 0) > 0, "not reached: $k")
            }
        } finally {
            fcx.release()
        }
    }
}
