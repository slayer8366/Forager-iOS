package com.zynergylabs.forager.brouter.mapaccess

import com.zynergylabs.forager.brouter.RouteWaysOutcome
import com.zynergylabs.forager.brouter.RouteWaysRequest
import com.zynergylabs.forager.brouter.expressions.ExpressionsEngines
import com.zynergylabs.forager.brouter.mapaccess.RouteWaysCells.CELL
import com.zynergylabs.forager.brouter.mapaccess.RouteWaysCells.DIVISOR
import com.zynergylabs.forager.brouter.mapaccess.RouteWaysCells.F0H_ENDS
import com.zynergylabs.forager.brouter.mapaccess.RouteWaysCells.cells
import com.zynergylabs.forager.brouter.mapaccess.RouteWaysCells.squareFile
import com.zynergylabs.forager.brouter.readRouteWays
import com.zynergylabs.forager.brouter.tiles.TestTiles
import okio.Path.Companion.toOkioPath
import java.io.File
import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Plan item 4 (RECORD -172; design `docs/audits/2026-11-13-brouter-port-closing/s68-route-ways-entry.md`): the public
 * entry [readRouteWays] against the Java engine's walk, link by link, on `RouteWaysTest`'s two point sets and cells.
 *
 * The Java side is `RouteWaysTest`'s oracle walk (Forager's `BRouterRouteWays.read`, one way context and one geometry
 * decoder per point across its squares), recorded per square: the square's divisor and, per link with a description,
 * its points and `getKeyValueDescription(reverse, bitmap)`. The entry is called once per square. Compared: the outcome's
 * kind, the divisor, the link count, and per link the point count, each `iLat`, each `iLon` and the description. A
 * failure names the set, the point, the square's index, the link's index and the field, never a value (E1). Only counts
 * and one digest per set are printed.
 */
class RouteWaysEntryTest {
    @Test
    fun dropoffPoints() {
        compare("dropoff", File(TestTiles.dropoff, "segments"), dropoff())
    }

    @Test
    fun f0hEnds() {
        val points = F0H_ENDS.flatMap { (name, e) -> listOf("$name/from" to (e[0] to e[1]), "$name/to" to (e[2] to e[3])) }
        assertEquals(16, points.size, "F0h: end count")
        compare("F0h", TestTiles.f0h, points)
    }

    @Test
    fun aSquareOfAnotherDivisorIsNotRead() {
        val (_, at) = dropoff().first()
        val squares = cells(at.first, at.second).groupBy { (lc, ac) -> squareFile(lc * CELL, ac * CELL) }
        val (name, squareCells) = squares.entries.first()
        val file = File(File(TestTiles.dropoff, "segments"), name)
        val oracle = btools.mapaccess.PhysicalFile(file, btools.codec.DataBuffers(), -1, -1)
        val javaDivisor = try {
            oracle.divisor
        } finally {
            oracle.close()
        }
        val lon = IntArray(squareCells.size) { squareCells[it].first.toInt() }
        val lat = IntArray(squareCells.size) { squareCells[it].second.toInt() }
        val outcome = readRouteWays(RouteWaysRequest(file.toOkioPath(), ExpressionsEngines.lookups.toOkioPath(), lon, lat, 80))
        val other = assertIs<RouteWaysOutcome.OtherDivisor>(outcome, "dropoff square asked at 1/80: outcome")
        assertEquals(javaDivisor, other.divisor, "dropoff square asked at 1/80: divisor")
        assertFailsWith<IllegalArgumentException>("unpaired cells") {
            readRouteWays(RouteWaysRequest(file.toOkioPath(), ExpressionsEngines.lookups.toOkioPath(), intArrayOf(1, 2), intArrayOf(1), DIVISOR))
        }
    }

    /** `RouteWaysTest.dropoffPoints`'s parse of Forager's drop-off `points.json`. */
    private fun dropoff(): List<Pair<String, Pair<Double, Double>>> {
        val text = File(TestTiles.dropoff, "points.json").readText()
        val points = Regex(""""(\w+)":\s*\{[^}]*"lat":\s*(-?[\d.]+),\s*"lng":\s*(-?[\d.]+)\s*\}""").findAll(text)
            .map { it.groupValues[1] to (it.groupValues[2].toDouble() to it.groupValues[3].toDouble()) }.toList()
        assertEquals(15, points.size, "points.json: point count")
        return points
    }

    private class Link(val iLat: IntArray, val iLon: IntArray, val description: String)

    private class Square(val divisor: Int, val links: List<Link>)

    private class Totals {
        var cells = 0
        var nodes = 0
        var links = 0
        var geometryPoints = 0L
        var entryCalls = 0
        var entryLinks = 0
        var entryGeometryPoints = 0L
    }

    private fun compare(set: String, routeData: File, points: List<Pair<String, Pair<Double, Double>>>) {
        val digest = MessageDigest.getInstance("SHA-256")
        val t = Totals()
        for ((name, at) in points) {
            val bySquare = cells(at.first, at.second).groupBy { (lc, ac) -> squareFile(lc * CELL, ac * CELL) }
            val java = oracleRead(bySquare, routeData, t)
            for ((s, entry) in bySquare.entries.withIndex()) {
                val where = "$set $name: square ${s + 1} of ${bySquare.size}"
                val squareCells = entry.value
                val request = RouteWaysRequest(
                    File(routeData, entry.key).toOkioPath(),
                    ExpressionsEngines.lookups.toOkioPath(),
                    IntArray(squareCells.size) { squareCells[it].first.toInt() },
                    IntArray(squareCells.size) { squareCells[it].second.toInt() },
                    DIVISOR,
                )
                val outcome = try {
                    readRouteWays(request)
                } catch (e: Throwable) {
                    throw AssertionError("$set $name: the entry threw ${e::class.simpleName}: ${e.message}", e)
                }
                t.entryCalls++
                val read = outcome as? RouteWaysOutcome.Read ?: fail("$where: field [outcome] is ${outcome::class.simpleName}")
                val o = java[s]
                if (read.divisor != o.divisor) fail("$where: field [divisor]")
                if (read.links.size != o.links.size) fail("$where: field [link count] ${o.links.size}/${read.links.size}")
                for (i in o.links.indices) {
                    val ol = o.links[i]
                    val pl = read.links[i]
                    val at2 = "$where, link ${i + 1} of ${o.links.size}"
                    if (pl.iLat.size != ol.iLat.size || pl.iLon.size != ol.iLon.size) fail("$at2: field [point count]")
                    if (!pl.iLat.contentEquals(ol.iLat)) fail("$at2: field [iLat] at point ${pl.iLat.indices.first { pl.iLat[it] != ol.iLat[it] } + 1}")
                    if (!pl.iLon.contentEquals(ol.iLon)) fail("$at2: field [iLon] at point ${pl.iLon.indices.first { pl.iLon[it] != ol.iLon[it] } + 1}")
                    if (pl.description != ol.description) fail("$at2: field [description]")
                    t.entryLinks++
                    t.entryGeometryPoints += pl.iLat.size
                    digest.update("${pl.description}\t${pl.iLat.joinToString(",")}\t${pl.iLon.joinToString(",")}\n".toByteArray())
                }
            }
        }
        println(
            "s68 route ways entry $set: ${points.size} points, ${t.entryCalls} entry calls; Java walk: cells ${t.cells}, nodes ${t.nodes}, " +
                "links with a description ${t.links}, geometry points ${t.geometryPoints}; entry: links ${t.entryLinks}, " +
                "geometry points ${t.entryGeometryPoints}; digest ${digest.digest().joinToString("") { "%02x".format(it) }}",
        )
        assertEquals(t.links, t.entryLinks, "$set: links, entry against Java")
        assertEquals(t.geometryPoints, t.entryGeometryPoints, "$set: geometry points, entry against Java")
        assertTrue(t.links > 0, "$set: no way read")
    }

    /** `RouteWaysTest.oracleRead`'s walk on the Java engine, recorded per square. */
    private fun oracleRead(bySquare: Map<String, List<Pair<Long, Long>>>, routeData: File, t: Totals): List<Square> {
        val meta = btools.expressions.BExpressionMetaData()
        val wayContext = btools.expressions.BExpressionContextWay(meta)
        meta.readMetaData(ExpressionsEngines.lookups)
        val geometry = btools.mapaccess.GeometryDecoder()
        val squares = mutableListOf<Square>()
        for ((name, squareCells) in bySquare) {
            val file = File(routeData, name)
            assertTrue(file.isFile, "no route square")
            val buffers = btools.codec.DataBuffers()
            val square = btools.mapaccess.PhysicalFile(file, buffers, -1, -1)
            val links = mutableListOf<Link>()
            try {
                for ((lonCell, latCell) in squareCells) {
                    val degree = btools.mapaccess.OsmFile(square, (lonCell * CELL / 1_000_000).toInt(), (latCell * CELL / 1_000_000).toInt(), buffers)
                    if (!degree.hasData()) continue
                    val cache = degree.createMicroCache(lonCell.toInt(), latCell.toInt(), buffers, null, null, true, null) ?: continue
                    t.cells++
                    val nodes = btools.mapaccess.OsmNodesMap()
                    for (i in 0 until cache.getSize()) {
                        val id = cache.getIdForIndex(i)
                        if (!cache.getAndClear(id)) continue
                        t.nodes++
                        val node = btools.mapaccess.OsmNode(id)
                        node.parseNodeBody(cache, nodes, wayContext)
                        var link: btools.mapaccess.OsmLink? = node.firstlink
                        while (link != null) {
                            val bitmap = link.descriptionBitmap
                            if (bitmap != null) {
                                val target = link.getTarget(node)
                                val reverse = link.isReverse(node)
                                val lat = mutableListOf(node.ilat)
                                val lon = mutableListOf(node.ilon)
                                var tn = link.geometry?.let { geometry.decodeGeometry(it, node, target, reverse) }
                                while (tn != null) {
                                    lat += tn.ilat
                                    lon += tn.ilon
                                    tn = tn.next
                                }
                                lat += target.ilat
                                lon += target.ilon
                                links += Link(lat.toIntArray(), lon.toIntArray(), wayContext.getKeyValueDescription(reverse, bitmap))
                                t.links++
                                t.geometryPoints += lat.size
                            }
                            link = link.getNext(node)
                        }
                    }
                }
                squares += Square(square.divisor, links)
            } finally {
                square.close()
            }
        }
        return squares
    }
}
