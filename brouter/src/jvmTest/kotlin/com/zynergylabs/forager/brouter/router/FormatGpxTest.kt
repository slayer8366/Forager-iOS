package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.mapaccess.MatchedWaypoint
import com.zynergylabs.forager.brouter.mapaccess.OsmNode
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Step 7, session 25 (RECORD -111, "Port them"): `FormatGpx` whole against the oracle's, with `OsmTrack.getVoiceHint`
 * and `getVoiceHintTime`. Tracks are `TrackEngines`' walks from Forager's own ends (E1's exception, RECORD -93), as in
 * `FormatKmlTest`. Each is given alike in both engines a seeded setup that Forager's path never sets, switched on in
 * test code: voice hints through `processVoiceHints` under every `turnInstructionMode` 0 to 9 (each of 2 to 9 has its
 * own GPX branch), or none; the message list (with a null entry at times), params, `showTime`, `showspeed`, way and
 * node tags set or cleared on the nodes' messages, POIs with and without descriptions, matched waypoints placed on the
 * track's own indices with names that do and do not start with `via`, `from`, `to` and `rt`, corrected points, and the
 * export flags. `formatAsWaypoint`, `getWaypoint` and `read` (on the oracle's own GPX texts and on crafted lines,
 * written to a temporary directory) are compared too. A thrown result is compared by exception class only (RECORD
 * -116); a stub's message is kept so that it names the stub. Texts are compared by digest and length only (E1).
 */
class FormatGpxTest {
    /**
     * RECORD -118: the port's `OsmTrack.version` is `null` by default; set from the oracle's own value at run time, as
     * in `FormatJsonTest`. Added in session 27: patch 29's test predates -118 (an input, not a change to the comparison).
     */
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
        "throws " + e.javaClass.name + (if (e is NotImplementedError) ": " + e.message else "") +
            (e.cause?.let { c -> " / " + c.javaClass.name + (if (c is NotImplementedError) ": " + c.message else "") } ?: "")
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

    private fun setMsg(e: Any, name: String, v: Any?) {
        val m = TrackEngines.field(e, "message") ?: return
        setField(m, name, v)
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

    /** A read track as the fields `read` fills: each node's position and elevation. */
    private fun readView(t: Any?): String {
        if (t == null) return "null"
        @Suppress("UNCHECKED_CAST")
        val nodes = TrackEngines.field(t, "nodes") as List<Any>
        return nodes.joinToString(";") { "${TrackEngines.field(it, "ilon")},${TrackEngines.field(it, "ilat")},${TrackEngines.field(it, "selev")}" }
    }

    @Test
    fun formatGpxMatchesTheOracle() {
        val dir = kotlin.io.path.createTempDirectory("formatgpx").toFile()
        val fcx = PathEngines.contexts(TrackEngines.foragerProfile())!!
        try {
            val rnd = Random(20261153L)
            val names = listOf("", "start", "a&b", "<via>", "via 1", "from here", "to there", "rt7", "it's \"x\"", "äöü€中")
            val descs = descriptions(fcx)
            assertTrue(descs.size > 5, "too few descriptions: ${descs.size}")
            val walks = TrackEngines.cases().withIndex().flatMap { (ci, c) -> TrackEngines.walks(fcx, c, 7901L + ci, 16, 60) }
            val pairs = walks.map { TrackEngines.tracks(it) } + listOf(btools.router.OsmTrack() to OsmTrack())
            val reached = HashMap<String, Int>()
            fun hit(k: String) { reached[k] = (reached[k] ?: 0) + 1 }
            var hints = 0
            var texts = 0
            var thrown = 0
            var reads = 0
            for ((i, pr) in pairs.withIndex()) {
                val (ot, pt) = pr
                val last = pt.nodes.size - 1
                val pos = pt.nodes.map { it.getILon() to it.getILat() }.ifEmpty { listOf(0 to 0) }
                fun at() = pos[rnd.nextInt(pos.size)]
                fun name(): String? = if (rnd.nextInt(40) == 0) null else names[rnd.nextInt(names.size)]
                fun elev(): Short = if (rnd.nextInt(3) == 0) Short.MIN_VALUE else rnd.nextInt(-400, 36000).toShort()
                if (i % 7 != 6) {
                    val mode = (i / 7 + i) % 10
                    val direct = rnd.nextBoolean()
                    fcx.o.hasDirectRouting = direct; fcx.p.hasDirectRouting = direct
                    fcx.o.turnInstructionMode = mode; fcx.p.turnInstructionMode = mode
                    val round = rnd.nextBoolean()
                    fcx.o.turnInstructionRoundabouts = round; fcx.p.turnInstructionRoundabouts = round
                    fcx.o.turnInstructionCatchingRange = 40.0; fcx.p.turnInstructionCatchingRange = 40.0
                    val car = rnd.nextInt(3) == 0
                    val bike = !car && rnd.nextBoolean()
                    fcx.o.carMode = car; fcx.p.carMode = car
                    fcx.o.bikeMode = bike; fcx.p.bikeMode = bike
                    if (!direct) {
                        // A detour map instead of direct routing (as VoiceHintProcessTest's kind 1).
                        val own = ot.nodes.map { it.idFromPos }
                        repeat(if (own.isEmpty()) 0 else rnd.nextInt(1, 20)) {
                            val k = own[rnd.nextInt(own.size)]
                            val e = rnd.nextInt(ot.nodes.size)
                            ot.registerDetourForId(k, ot.nodes[e]); pt.registerDetourForId(k, pt.nodes[e])
                        }
                    }
                    val oh = res { ot.processVoiceHints(fcx.o); "" }
                    val ph = res { pt.processVoiceHints(fcx.p); "" }
                    assertEquals(oh, ph, "track $i: processVoiceHints")
                }
                // Tags on the nodes' messages, set or cleared alike.
                for (k in ot.nodes.indices) {
                    when (rnd.nextInt(8)) {
                        0 -> { setMsg(ot.nodes[k], "wayKeyValues", null); setMsg(pt.nodes[k], "wayKeyValues", null) }
                        1 -> { setMsg(ot.nodes[k], "nodeKeyValues", "seeded=$k"); setMsg(pt.nodes[k], "nodeKeyValues", "seeded=$k") }
                        2 -> { setMsg(ot.nodes[k], "wayKeyValues", "highway=path"); setMsg(pt.nodes[k], "wayKeyValues", "highway=path") }
                    }
                }
                val nm = if (rnd.nextInt(3) == 0) names[rnd.nextInt(names.size)] else "unset"
                setField(ot, "name", nm); pt.name = nm
                if (rnd.nextInt(25) != 0) {
                    val ml = ArrayList(List(rnd.nextInt(1, 4)) { k -> if (k > 0 && rnd.nextInt(4) == 0) null else "msg $k ${names[rnd.nextInt(names.size)]}" })
                    setField(ot, "messageList", ArrayList(ml)); pt.messageList = ArrayList(ml) as MutableList<String>
                } else hit("messageList null")
                when (rnd.nextInt(3)) {
                    0 -> {}
                    1 -> { setField(ot, "params", LinkedHashMap<String, String>()); pt.params = LinkedHashMap() }
                    else -> {
                        val m = LinkedHashMap<String, String>()
                        repeat(rnd.nextInt(1, 4)) { m["k$it"] = "v${rnd.nextInt(100)}" }
                        setField(ot, "params", LinkedHashMap(m)); pt.params = LinkedHashMap(m)
                        hit("params")
                    }
                }
                val st = rnd.nextBoolean(); val ss = rnd.nextBoolean()
                setField(ot, "showTime", st); pt.showTime = st
                setField(ot, "showspeed", ss); pt.showspeed = ss
                if (i % 5 != 4) {
                    repeat(rnd.nextInt(0, 4)) {
                        val (x, y) = at(); val n = name(); val e = elev()
                        val d = if (rnd.nextBoolean()) null else descs[rnd.nextInt(descs.size)]
                        val op = btools.router.OsmNodeNamed(btools.mapaccess.OsmNode(x, y)).also { it.name = n; it.selev = e; it.nodeDescription = d?.copyOf() }
                        val pp = OsmNodeNamed(OsmNode(x, y)).also { it.name = n; it.selev = e; it.nodeDescription = d?.copyOf() }
                        @Suppress("UNCHECKED_CAST")
                        (TrackEngines.field(ot, "pois") as MutableList<Any>).add(op); pt.pois.add(pp)
                        hit("poi")
                    }
                    val ow = ArrayList<btools.mapaccess.MatchedWaypoint>()
                    val pw = ArrayList<MatchedWaypoint>()
                    val count = rnd.nextInt(2, 7)
                    for (q in 0 until count) {
                        val (x, y) = at(); val n = name(); val e = elev()
                        val type = rnd.nextInt(1, 4).toByte()
                        val idx = when {
                            q == 0 -> 0
                            q == count - 1 -> last
                            rnd.nextInt(6) == 0 -> -1
                            else -> rnd.nextInt(0, last + 1)
                        }
                        val o = btools.mapaccess.MatchedWaypoint(); val p = MatchedWaypoint()
                        o.waypoint = btools.mapaccess.OsmNode(x, y).also { it.selev = e }; p.waypoint = OsmNode(x, y).also { it.selev = e }
                        o.name = n; p.name = n; o.wpttype = type; p.wpttype = type; o.indexInTrack = idx; p.indexInTrack = idx
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
                } else hit("no pois or waypoints")

                val o = res { btools.router.FormatGpx(fcx.o).format(ot) }
                val p = res { FormatGpx(fcx.p).format(pt) }
                same(o, p, "track $i: format")
                val mode = pt.voiceHints?.turnInstructionMode ?: 0
                if (o.startsWith("throws")) {
                    thrown++; hit("thrown mode $mode")
                } else {
                    texts++
                    val n = pt.voiceHints?.list?.size ?: 0
                    hints += n
                    hit("mode $mode" + if (n > 0) " with hints" else "")
                    if (o.contains("<brouter:speed>")) hit("brouter:speed")
                    if (o.contains("<brouter:params>")) hit("brouter:params")
                    if (o.contains("<locus:rteTime>")) hit("locus:rteTime")
                    if (o.contains("<src>beeline</src>")) hit("locus beeline")
                    if (o.contains("<desc>beeline</desc>")) hit("brouter beeline")
                    if (o.contains("<time>") && o.contains("<rte>")) hit("rte time")
                    if (o.contains("<desc>") && o.contains("<wpt")) hit("wpt desc")
                    // read: the oracle's own text, written to a temporary file, read back by both engines.
                    val f = java.io.File(dir, "t$i.gpx")
                    f.writeText(o)
                    same(res { readView(btools.router.FormatGpx(fcx.o).read(f.path)) }, res { readView(FormatGpx(fcx.p).read(f.path)) }, "track $i: read")
                    reads++
                }
                // formatAsWaypoint and getWaypoint on POI-like nodes: no description, or way or node bytes.
                for (k in 0 until 2) {
                    val (x, y) = at(); val e = elev(); val n = name()
                    val d = if (rnd.nextInt(3) == 0) null else descs[rnd.nextInt(descs.size)]
                    val on = btools.router.OsmNodeNamed(btools.mapaccess.OsmNode(x, y)).also { it.name = n; it.selev = e; it.nodeDescription = d?.copyOf() }
                    val pn = OsmNodeNamed(OsmNode(x, y)).also { it.name = n; it.selev = e; it.nodeDescription = d?.copyOf() }
                    same(res { btools.router.FormatGpx(fcx.o).formatAsWaypoint(on) }, res { FormatGpx(fcx.p).formatAsWaypoint(pn) }, "track $i waypoint $k: formatAsWaypoint")
                    same(res { btools.router.FormatGpx(null).formatAsWaypoint(on) }, res { FormatGpx(null).formatAsWaypoint(pn) }, "track $i waypoint $k: formatAsWaypoint, null rc")
                    val ds = if (rnd.nextBoolean()) null else names[rnd.nextInt(names.size)]
                    same(res { btools.router.FormatGpx.getWaypoint(x, y, n, ds) }, res { FormatGpx.getWaypoint(x, y, n, ds) }, "track $i waypoint $k: getWaypoint")
                }
            }
            // read on crafted lines: line ends \n, \r and \r\n, a missing lat, a missing lon, bad numbers, no file.
            val crafted = listOf(
                "<trkpt lon=\"1.5\" lat=\"2.25\">\r<trkpt lon=\"-3\" lat=\"4\">\r\n<trkpt lon=\"5\" lat=\"6\">",
                "<trkpt lon=\"7\">\n<trkpt  lat=\"8\" lon=\"9\">\n",
                "<trkpt lon=\" 10 \" lat=\"1e1\">\n<trkpt lon=\"11d\" lat=\"12f\">",
                "<trkpt lon=\"x\" lat=\"1\">",
                "<trkpt lat=\"1\">",
                "<trkpt lon=\"1",
                "",
            )
            for ((k, s) in crafted.withIndex()) {
                val f = java.io.File(dir, "c$k.gpx")
                f.writeText(s)
                same(res { readView(btools.router.FormatGpx(fcx.o).read(f.path)) }, res { readView(FormatGpx(fcx.p).read(f.path)) }, "crafted $k: read")
                reads++
            }
            val missing = java.io.File(dir, "missing.gpx").path
            same(res { readView(btools.router.FormatGpx(fcx.o).read(missing)) }, res { readView(FormatGpx(fcx.p).read(missing)) }, "missing: read")

            println("7 FormatGpx: ${pairs.size} tracks, $texts texts, $thrown thrown, $hints hints, $reads reads, reached ${reached.toSortedMap()}")
            assertTrue(hints > 50, "too few hints: $hints")
            for (m in 2..9) assertTrue((reached["mode $m with hints"] ?: 0) > 0, "not reached: mode $m with hints")
            for (k in listOf("brouter:speed", "brouter:params", "locus:rteTime", "locus beeline", "brouter beeline",
                "rte time", "wpt desc", "exportWaypoints", "exportCorrectedWaypoints", "poi", "messageList null")) {
                assertTrue((reached[k] ?: 0) > 0, "not reached: $k")
            }
        } finally {
            fcx.release()
            dir.deleteRecursively()
        }
    }
}
