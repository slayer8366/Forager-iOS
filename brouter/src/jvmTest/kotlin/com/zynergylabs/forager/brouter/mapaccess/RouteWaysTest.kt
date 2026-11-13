package com.zynergylabs.forager.brouter.mapaccess

import com.zynergylabs.forager.brouter.codec.DataBuffers
import com.zynergylabs.forager.brouter.expressions.BExpressionContextWay
import com.zynergylabs.forager.brouter.expressions.BExpressionMetaData
import com.zynergylabs.forager.brouter.expressions.ExpressionsEngines
import com.zynergylabs.forager.brouter.mapaccess.RouteWaysCells.CELL
import com.zynergylabs.forager.brouter.mapaccess.RouteWaysCells.DIVISOR
import com.zynergylabs.forager.brouter.mapaccess.RouteWaysCells.F0H_ENDS
import com.zynergylabs.forager.brouter.mapaccess.RouteWaysCells.cells
import com.zynergylabs.forager.brouter.mapaccess.RouteWaysCells.squareFile
import com.zynergylabs.forager.brouter.tiles.TestTiles
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import java.io.File
import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Step 5d, item 4 (RECORD -65, -70, -74): Forager's route-ways read, `BRouterRouteWays.read` (`BRouterRouteWays.kt:59-123`
 * at Forager a0535ece), made as the same calls on both engines, 150 m around each point (`BRouterRouteWaysTest.kt:27`).
 *
 * For each micro-cache cell the read covers: `createMicroCache(lonCell, latCell, buffers, null, null, true, null)`, then
 * for each node in index order that `getAndClear` gives, `OsmNode(id)` and `parseNodeBody(cache, nodes, wayContext)`, then
 * every link with a description bitmap from `firstlink` by `getNext`. Per link, compared: `isReverse(node)`, the tag map
 * of `wayContext.getKeyValueDescription(reverse, bitmap)` (split as `tagsOf`), the geometry in order (the node, then
 * `GeometryDecoder.decodeGeometry`'s transfer nodes, then the target, as micro-degree pairs), and whether Forager's
 * end-pair dedupe keeps it. A failure names the point, the line and the field, never a value (E1). Only counts and one
 * digest per point set are printed.
 *
 * Points: the 15 of Forager's drop-off `points.json` on its drop-off square, and F0h's 16 ends (`E3F0hTest.kt` `ENDS` on
 * `brouter-port`, from `routes.json` at Forager d6e2a0415) on F0h's heighted square.
 */
class RouteWaysTest {
    @Test
    fun dropoffPoints() {
        val text = File(TestTiles.dropoff, "points.json").readText()
        val points = Regex(""""(\w+)":\s*\{[^}]*"lat":\s*(-?[\d.]+),\s*"lng":\s*(-?[\d.]+)\s*\}""").findAll(text)
            .map { it.groupValues[1] to (it.groupValues[2].toDouble() to it.groupValues[3].toDouble()) }.toList()
        assertEquals(15, points.size, "points.json: point count")
        compare("dropoff", File(TestTiles.dropoff, "segments"), points)
    }

    @Test
    fun f0hEnds() {
        val points = F0H_ENDS.flatMap { (name, e) -> listOf("$name/from" to (e[0] to e[1]), "$name/to" to (e[2] to e[3])) }
        assertEquals(16, points.size, "F0h: end count")
        compare("F0h", TestTiles.f0h, points)
    }

    private class Totals {
        var cells = 0
        var nodes = 0
        var links = 0
        var kept = 0
        var reverse = 0
        var geometryPoints = 0L
        var tags = 0L
        override fun toString() =
            "cells $cells, nodes $nodes, links with a description $links (reverse $reverse, kept $kept), geometry points $geometryPoints, tags $tags"
    }

    private fun compare(set: String, routeData: File, points: List<Pair<String, Pair<Double, Double>>>) {
        val digest = MessageDigest.getInstance("SHA-256")
        val totals = Totals()
        for ((name, at) in points) {
            val o = oracleRead(at.first, at.second, routeData, totals)
            val p = try {
                portRead(at.first, at.second, routeData)
            } catch (e: Throwable) {
                throw AssertionError("$set $name: the port threw ${e::class.simpleName}: ${e.message}", e)
            }
            if (o != p) {
                val lo = o.split('\n')
                val lp = p.split('\n')
                val i = (0 until maxOf(lo.size, lp.size)).first { lo.getOrNull(it) != lp.getOrNull(it) }
                val field = (lo.getOrNull(i) ?: lp.getOrNull(i) ?: "").substringBefore('\t')
                fail("$set $name: line ${i + 1} of ${lo.size}/${lp.size} differs, field [$field]")
            }
            digest.update(o.toByteArray())
        }
        println(
            "5d route ways $set: ${points.size} points; $totals; lookups.dat ${TestTiles.sha256(ExpressionsEngines.lookups)}; " +
                "digest ${digest.digest().joinToString("") { "%02x".format(it) }}",
        )
        assertTrue(totals.links > 0, "$set: no way read")
    }

    /** One link as lines `field<TAB>value`. */
    private class Out(val totals: Totals?) {
        val sb = StringBuilder()
        val seen = HashSet<String>()
        fun line(field: String, value: Any?) {
            sb.append(field).append('\t').append(value).append('\n')
        }
        fun link(cell: String, node: Int, reverse: Boolean, description: String, geometry: List<Long>) {
            val tags = tagsOf(description)
            val ends = listOf(geometry.first(), geometry.last()).sorted()
            val kept = seen.add("${ends[0]}:${ends[1]}:${geometry.size}")
            line("link", "$cell node $node")
            line("isReverse", reverse)
            line("tags", tags.toSortedMap())
            line("geometry", geometry.joinToString(",") { "${it shr 32}/${it and 0xffffffffL}" })
            line("kept", kept)
            totals?.let {
                it.links++
                if (reverse) it.reverse++
                if (kept) it.kept++
                it.geometryPoints += geometry.size
                it.tags += tags.size
            }
        }
    }

    private fun oracleRead(lat: Double, lng: Double, routeData: File, totals: Totals): String {
        val meta = btools.expressions.BExpressionMetaData()
        val wayContext = btools.expressions.BExpressionContextWay(meta)
        meta.readMetaData(ExpressionsEngines.lookups)
        val out = Out(totals)
        val geometry = btools.mapaccess.GeometryDecoder()
        for ((name, squareCells) in cells(lat, lng).groupBy { (lc, ac) -> squareFile(lc * CELL, ac * CELL) }) {
            val file = File(routeData, name)
            assertTrue(file.isFile, "no route square $name")
            val buffers = btools.codec.DataBuffers()
            val square = btools.mapaccess.PhysicalFile(file, buffers, -1, -1)
            try {
                assertEquals(DIVISOR, square.divisor, "$name: divisor")
                for ((lonCell, latCell) in squareCells) {
                    val degree = btools.mapaccess.OsmFile(square, (lonCell * CELL / 1_000_000).toInt(), (latCell * CELL / 1_000_000).toInt(), buffers)
                    if (!degree.hasData()) continue
                    val cache = degree.createMicroCache(lonCell.toInt(), latCell.toInt(), buffers, null, null, true, null) ?: continue
                    out.line("cell", "$lonCell $latCell size ${cache.getSize()}")
                    totals.cells++
                    val nodes = btools.mapaccess.OsmNodesMap()
                    for (i in 0 until cache.getSize()) {
                        val id = cache.getIdForIndex(i)
                        if (!cache.getAndClear(id)) continue
                        totals.nodes++
                        val node = btools.mapaccess.OsmNode(id)
                        node.parseNodeBody(cache, nodes, wayContext)
                        var link: btools.mapaccess.OsmLink? = node.firstlink
                        while (link != null) {
                            val bitmap = link.descriptionBitmap
                            if (bitmap != null) {
                                val target = link.getTarget(node)
                                val reverse = link.isReverse(node)
                                val pts = buildList {
                                    add(pos(node.ilon, node.ilat))
                                    var t = link!!.geometry?.let { geometry.decodeGeometry(it, node, target, reverse) }
                                    while (t != null) {
                                        add(pos(t.ilon, t.ilat))
                                        t = t.next
                                    }
                                    add(pos(target.ilon, target.ilat))
                                }
                                out.link("$lonCell $latCell", i, reverse, wayContext.getKeyValueDescription(reverse, bitmap), pts)
                            }
                            link = link.getNext(node)
                        }
                    }
                }
            } finally {
                square.close()
            }
        }
        return out.sb.toString()
    }

    private fun portRead(lat: Double, lng: Double, routeData: File): String {
        val meta = BExpressionMetaData()
        val wayContext = BExpressionContextWay(meta)
        meta.readMetaData(FileSystem.SYSTEM, ExpressionsEngines.lookups.toOkioPath())
        val out = Out(null)
        val geometry = GeometryDecoder()
        for ((name, squareCells) in cells(lat, lng).groupBy { (lc, ac) -> squareFile(lc * CELL, ac * CELL) }) {
            val file = File(routeData, name)
            assertTrue(file.isFile, "no route square $name")
            val buffers = DataBuffers()
            val square = PhysicalFile(FileSystem.SYSTEM, file.toOkioPath(), buffers, -1, -1)
            try {
                assertEquals(DIVISOR, square.divisor, "$name: divisor")
                for ((lonCell, latCell) in squareCells) {
                    val degree = OsmFile(square, (lonCell * CELL / 1_000_000).toInt(), (latCell * CELL / 1_000_000).toInt(), buffers)
                    if (!degree.hasData()) continue
                    val cache = degree.createMicroCache(lonCell.toInt(), latCell.toInt(), buffers, null, null, true, null) ?: continue
                    out.line("cell", "$lonCell $latCell size ${cache.getSize()}")
                    val nodes = OsmNodesMap()
                    for (i in 0 until cache.getSize()) {
                        val id = cache.getIdForIndex(i)
                        if (!cache.getAndClear(id)) continue
                        val node = OsmNode(id)
                        node.parseNodeBody(cache, nodes, wayContext)
                        var link: OsmLink? = node.firstlink
                        while (link != null) {
                            val bitmap = link.descriptionBitmap
                            if (bitmap != null) {
                                val target = link.getTarget(node)!!
                                val reverse = link.isReverse(node)
                                val pts = buildList {
                                    add(pos(node.ilon, node.ilat))
                                    var t = link!!.geometry?.let { geometry.decodeGeometry(it, node, target, reverse) }
                                    while (t != null) {
                                        add(pos(t.ilon, t.ilat))
                                        t = t.next
                                    }
                                    add(pos(target.ilon, target.ilat))
                                }
                                out.link("$lonCell $latCell", i, reverse, wayContext.getKeyValueDescription(reverse, bitmap), pts)
                            }
                            link = link.getNext(node)
                        }
                    }
                }
            } finally {
                square.close()
            }
        }
        return out.sb.toString()
    }

    private companion object {
        fun pos(ilon: Int, ilat: Int): Long = ilon.toLong() shl 32 or (ilat.toLong() and 0xffffffffL)

        /** `BRouterRouteWays.kt` `tagsOf` at a0535ece. */
        fun tagsOf(description: String): Map<String, String> =
            description.split(' ').filter { '=' in it }.associate { it.substringBefore('=') to it.substringAfter('=') }
    }
}
