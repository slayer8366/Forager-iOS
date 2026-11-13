package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.mapaccess.OsmNode
import java.io.File
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Step 7, session 23 (RECORD -111): `OsmTrack.readBinary`'s file branch (`OsmTrack.java:233-307`), with
 * `MatchedWaypoint.readFromStream` and `OsmPathElement.readFromStream`, against the oracle's. Files are written by the
 * oracle's own `writeBinary`, from tracks of `TrackEngines`' walks from Forager's own ends (-93) and from an empty
 * track, with seeded end points, names (ASCII and not), checksums, dirty flags and profile stamps, into a temporary
 * directory that is deleted after. Each file is read by both engines with the target moved by 0 or 19 to 21 units,
 * checksums moved by 0, 20 or 21, the profile stamp equal or not, with and without `debugInfo`; and again cut short at
 * seeded lengths, and as a missing file. Results are compared field by field by digest and length, the debug text
 * by digest and length, so no position appears in a failure (E1). On Java's catch path the debug text is compared
 * only up to and including "Error reading rawTrack: ", and each side must write text after it (RECORD -116).
 */
class ReadBinaryTest {
    private val ERROR_PREFIX = "Error reading rawTrack: "

    private fun digest(s: String): String =
        java.security.MessageDigest.getInstance("SHA-256").digest(s.toByteArray()).joinToString("") { "%02x".format(it) }
            .take(16) + "/" + s.length

    private fun f(o: Any?, name: String): Any? = if (o == null) null else TrackEngines.field(o, name)

    private fun node(n: Any?): String = if (n == null) "null" else "${f(n, "ilat")},${f(n, "ilon")}"

    private fun view(t: Any?, port: Boolean): String {
        if (t == null) return "null"
        @Suppress("UNCHECKED_CAST")
        val nodes = f(t, "nodes") as List<Any>
        val sb = StringBuilder()
        val ep = f(t, "endPoint")
        sb.append("ep=").append(listOf("node1", "node2", "crosspoint", "waypoint").joinToString(";") { node(f(ep, it)) })
        sb.append(" r=").append(f(ep, "radius")).append(" w=").append(f(ep, "wpttype")).append(" name=").append(f(ep, "name"))
        sb.append(" n=").append(nodes.size).append(" cost=").append(f(t, "cost")).append(" dirty=").append(f(t, "isDirty"))
        for (pe in nodes) {
            sb.append(" [").append(f(pe, "ilat")).append(',').append(f(pe, "ilon")).append(',').append(f(pe, "selev"))
                .append(',').append(f(pe, "cost")).append(',').append(TrackEngines.indexOf(nodes, f(pe, "origin"))).append(']')
        }
        val map = f(t, "nodesMap")
        sb.append(" map=").append(map != null)
        if (map != null) {
            sb.append(" contains=").append(nodes.joinToString("") { pe ->
                if (port) (if ((t as OsmTrack).containsNode(pe as OsmPathElement)) "1" else "0")
                else (if ((t as btools.router.OsmTrack).containsNode(pe as btools.mapaccess.OsmPos)) "1" else "0")
            })
        }
        return sb.toString()
    }

    private fun res(f: () -> String): String = try {
        f()
    } catch (e: Throwable) {
        "throws " + e.javaClass.name + ": " + e.message
    }

    @Test
    fun readBinaryMatchesTheOracle() {
        val dir = kotlin.io.path.createTempDirectory("readbinary").toFile()
        val fcx = PathEngines.contexts(TrackEngines.foragerProfile())!!
        try {
            val rnd = Random(20261124L)
            val tracks = TrackEngines.cases().withIndex().flatMap { (ci, c) -> TrackEngines.walks(fcx, c, 7501L + ci, 3, 40) }
                .map { TrackEngines.tracks(it).first } + listOf(btools.router.OsmTrack(), btools.router.OsmTrack())
            assertTrue(tracks.size >= 20, "too few tracks: ${tracks.size}")
            val names = listOf("", "end", "Weg 7", "Zielpunkt-äöü", "€中", "x".repeat(300))
            var reads = 0
            var nonNull = 0
            var debugs = 0
            var errorPaths = 0
            val seen = HashSet<String>()
            for ((ti, ot) in tracks.withIndex()) {
                val wp = btools.mapaccess.MatchedWaypoint()
                wp.node1 = btools.mapaccess.OsmNode(rnd.nextInt(), rnd.nextInt())
                wp.node2 = btools.mapaccess.OsmNode(rnd.nextInt(), rnd.nextInt())
                wp.crosspoint = btools.mapaccess.OsmNode(rnd.nextInt(), rnd.nextInt())
                wp.waypoint = btools.mapaccess.OsmNode(rnd.nextInt(0, 360000000), rnd.nextInt(0, 180000000))
                wp.radius = if (ti % 5 == 0) rnd.nextDouble() * 1000 else rnd.nextDouble(-1e9, 1e9)
                wp.wpttype = rnd.nextInt(-128, 128).toByte()
                wp.name = names[ti % names.size]
                ot.endPoint = wp
                val nogo = LongArray(3) { if (rnd.nextBoolean()) 0L else rnd.nextLong() }
                ot.nogoChecksums = nogo
                ot.isDirty = rnd.nextBoolean()
                ot.profileTimestamp = if (rnd.nextBoolean()) 0L else rnd.nextLong()
                val file = File(dir, "t$ti.bin")
                ot.writeBinary(file.absolutePath)
                val bytes = file.readBytes()
                val cut = File(dir, "c$ti.bin")
                val cuts = listOf(0, 1, 32, 40, 41, 43) + List(8) { rnd.nextInt(0, bytes.size + 1) } +
                    listOf(bytes.size - 1, bytes.size - 8, bytes.size - 9, bytes.size - 16, bytes.size - 17, bytes.size - 24, bytes.size - 25)
                val variants = ArrayList<Triple<String, Int, Int>>() // file, target dlon, dlat
                for (d in listOf(0 to 0, 19 to -19, -19 to 19, 20 to 0, 0 to -20, 21 to 21, -20 to 5)) variants += Triple(file.name, d.first, d.second)
                for (k in cuts.filter { it in 0..bytes.size }.distinct()) variants += Triple("c$ti-$k", 0, 0)
                variants += Triple("missing", 0, 0)
                for ((name, dlon, dlat) in variants) {
                    val path = when {
                        name == "missing" -> File(dir, "missing-$ti.bin")
                        name.startsWith("c") -> { cut.writeBytes(bytes.copyOf(name.substringAfterLast('-').toInt())); cut }
                        else -> file
                    }
                    for (shift in listOf(0, 20, 21, -20, -21)) {
                        for (sameProfile in listOf(true, false)) {
                            for (withDebug in listOf(true, false)) {
                                if (name != file.name && (shift != 0 || !sameProfile) && rnd.nextInt(4) != 0) continue
                                val epLon = wp.waypoint.ilon - dlon
                                val epLat = wp.waypoint.ilat - dlat
                                val oEp = btools.router.OsmNodeNamed(btools.mapaccess.OsmNode(epLon, epLat))
                                val pEp = OsmNodeNamed(OsmNode(epLon, epLat))
                                val which = rnd.nextInt(3)
                                val want = LongArray(3) { if (it == which) nogo[it] + shift else nogo[it] }
                                val pcs = if (sameProfile) ot.profileTimestamp else ot.profileTimestamp + 1
                                val oDebug = if (withDebug) StringBuilder("pre;") else null
                                val pDebug = if (withDebug) StringBuilder("pre;") else null
                                val o = res { view(btools.router.OsmTrack.readBinary(path.absolutePath, oEp, want.copyOf(), pcs, oDebug), false) }
                                val p = res { view(OsmTrack.readBinary(path.absolutePath, pEp, want.copyOf(), pcs, pDebug), true) }
                                val id = "track $ti, $name, target $dlon/$dlat, shift $shift, profile $sameProfile, debug $withDebug"
                                // A thrown text carries no position (view() is not reached), so it alone is shown in full;
                                // the other side is shown by digest (E1).
                                if (o.startsWith("throws") && p.startsWith("throws")) assertEquals(o, p, "$id: result")
                                if (o.startsWith("throws") != p.startsWith("throws")) kotlin.test.fail(
                                    "$id: result: oracle " + (if (o.startsWith("throws")) o else digest(o)) +
                                        ", port " + (if (p.startsWith("throws")) p else digest(p)))
                                assertEquals(digest(o), digest(p), "$id: result")
                                val oText = oDebug?.toString() ?: "null"
                                val pText = pDebug?.toString() ?: "null"
                                val oErr = oText.indexOf(ERROR_PREFIX)
                                val pErr = pText.indexOf(ERROR_PREFIX)
                                if (oErr >= 0 || pErr >= 0) {
                                    // RECORD -116: on Java's catch path (OsmTrack.java:287-290) the text after BRouter's
                                    // prefix is the runtime's own description of its exception. There, debugInfo is
                                    // compared exactly up to and including the prefix; both engines must have taken the
                                    // path, and each must have written some text after the prefix.
                                    assertTrue(oErr >= 0 && pErr >= 0, "$id: debugInfo: error path taken, oracle ${oErr >= 0}, port ${pErr >= 0}")
                                    val oHead = oText.substring(0, oErr + ERROR_PREFIX.length)
                                    val pHead = pText.substring(0, pErr + ERROR_PREFIX.length)
                                    assertEquals(digest(oHead), digest(pHead), "$id: debugInfo up to the error prefix")
                                    assertTrue(oText.length > oHead.length, "$id: debugInfo: oracle wrote nothing after the error prefix")
                                    assertTrue(pText.length > pHead.length, "$id: debugInfo: port wrote nothing after the error prefix")
                                    errorPaths++
                                } else {
                                    assertEquals(digest(oText), digest(pText), "$id: debugInfo")
                                }
                                reads++
                                if (!o.startsWith("null") && !o.startsWith("throws")) nonNull++
                                if (oDebug != null) { debugs++; seen += oDebug.toString().substringAfter("pre;").take(60).replace(Regex("[-0-9]+"), "#") }
                            }
                        }
                    }
                }
            }
            println("7 readBinary: ${tracks.size} tracks, $reads reads compared, $nonNull tracks returned, $debugs debug texts, $errorPaths on the error path; kinds $seen")
            assertTrue(nonNull > 50 && reads > 1000, "too little reached: $nonNull of $reads")
        } finally {
            fcx.release()
            dir.deleteRecursively()
        }
    }
}
