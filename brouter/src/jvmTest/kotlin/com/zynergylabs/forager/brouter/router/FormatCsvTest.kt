package com.zynergylabs.forager.brouter.router

import java.io.File
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Step 7, session 24 (RECORD -111, "Port them"): `FormatCsv` whole, with `OsmTrack.aggregateMessages`,
 * `MessageData.toMessage` and `add`, and `Formatter.write`, against the oracle's. Tracks are `TrackEngines`' walks from
 * Forager's own ends (E1's exception, RECORD -93), each also seeded alike in both engines: some messages' way or node
 * tags cleared or set, so that `aggregateMessages` both merges and splits. An empty track is included. `format`'s text
 * and `write`'s file bytes are compared by digest and length only, since they carry positions (E1).
 */
class FormatCsvTest {
    private fun digest(s: String): String =
        java.security.MessageDigest.getInstance("SHA-256").digest(s.toByteArray()).joinToString("") { "%02x".format(it) }
            .take(16) + "/" + s.length

    private fun res(f: () -> String): String = try {
        f()
    } catch (e: Throwable) {
        "throws " + e.javaClass.name + ": " + e.message
    }

    private fun setMsg(e: Any, name: String, v: Any?) {
        val m = TrackEngines.field(e, "message") ?: return
        var c: Class<*>? = m.javaClass
        while (c != null) {
            val f = c.declaredFields.firstOrNull { it.name == name }
            if (f != null) { f.isAccessible = true; f.set(m, v); return }
            c = c.superclass
        }
        error("no field $name")
    }

    @Test
    fun formatCsvMatchesTheOracle() {
        val dir = kotlin.io.path.createTempDirectory("formatcsv").toFile()
        val fcx = PathEngines.contexts(TrackEngines.foragerProfile())!!
        try {
            val rnd = Random(20261131L)
            val walks = TrackEngines.cases().withIndex().flatMap { (ci, c) -> TrackEngines.walks(fcx, c, 7601L + ci, 6, 60) }
            val pairs = walks.map { TrackEngines.tracks(it) }.toMutableList()
            pairs += btools.router.OsmTrack() to OsmTrack()
            assertTrue(pairs.size >= 20, "too few tracks: ${pairs.size}")
            var lines = 0
            var seeded = 0
            for ((i, pr) in pairs.withIndex()) {
                val (ot, pt) = pr
                if (i % 2 == 1) for (k in ot.nodes.indices) {
                    when (rnd.nextInt(6)) {
                        0 -> { setMsg(ot.nodes[k], "wayKeyValues", null); setMsg(pt.nodes[k], "wayKeyValues", null); seeded++ }
                        1 -> { setMsg(ot.nodes[k], "nodeKeyValues", "seeded=$k"); setMsg(pt.nodes[k], "nodeKeyValues", "seeded=$k"); seeded++ }
                        2 -> { setMsg(ot.nodes[k], "nodeKeyValues", null); setMsg(pt.nodes[k], "nodeKeyValues", null); seeded++ }
                        3 -> { setMsg(ot.nodes[k], "wayKeyValues", "seeded=1"); setMsg(pt.nodes[k], "wayKeyValues", "seeded=1"); seeded++ }
                    }
                }
                val id = "track $i"
                val o = res { btools.router.FormatCsv(fcx.o).format(ot) }
                val p = res { FormatCsv(fcx.p).format(pt) }
                if (o.startsWith("throws") || p.startsWith("throws")) {
                    assertEquals(if (o.startsWith("throws")) o else digest(o), if (p.startsWith("throws")) p else digest(p), "$id: format")
                }
                assertEquals(digest(o), digest(p), "$id: format")
                val oa = res { ot.aggregateMessages().joinToString("\n") }
                val pa = res { pt.aggregateMessages().joinToString("\n") }
                assertEquals(digest(oa), digest(pa), "$id: aggregateMessages")
                lines += o.count { it == '\n' }
                if (i % 5 == 0) {
                    val of = File(dir, "o$i.csv")
                    val pf = File(dir, "p$i.csv")
                    btools.router.FormatCsv(fcx.o).write(of.absolutePath, ot)
                    FormatCsv(fcx.p).write(pf.absolutePath, pt)
                    assertEquals(digest(String(of.readBytes(), Charsets.ISO_8859_1)), digest(String(pf.readBytes(), Charsets.ISO_8859_1)), "$id: write's bytes")
                }
            }
            println("7 FormatCsv: ${pairs.size} tracks, $lines lines, $seeded messages seeded")
            assertTrue(lines > 500, "too few lines: $lines")
        } finally {
            fcx.release()
            dir.deleteRecursively()
        }
    }
}
