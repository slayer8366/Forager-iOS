package com.zynergylabs.forager.brouter

import com.zynergylabs.forager.brouter.codec.DataBuffers
import com.zynergylabs.forager.brouter.codec.MicroCache
import com.zynergylabs.forager.brouter.expressions.BExpressionContextWay
import com.zynergylabs.forager.brouter.expressions.BExpressionMetaData
import com.zynergylabs.forager.brouter.mapaccess.GeometryDecoder
import com.zynergylabs.forager.brouter.mapaccess.OsmFile
import com.zynergylabs.forager.brouter.mapaccess.OsmLink
import com.zynergylabs.forager.brouter.mapaccess.OsmNode
import com.zynergylabs.forager.brouter.mapaccess.OsmNodesMap
import com.zynergylabs.forager.brouter.mapaccess.PhysicalFile
import okio.FileSystem
import okio.Path

// The route-ways reader's public entry (plan item 4; RECORD -172, the planner's decision on its shape): one function
// beside route(), added to the surface Route.kt declares. Forager's BRouterRouteWays.read (BRouterRouteWays.kt:59-123 at
// Forager a0535ece) up to, not including, Forager's own parts: which cells a circle covers, the square file's name, the
// messages, the end-pair dedupe, the tag-map split and the conversion to positions stay Forager's. Design and reasons:
// docs/audits/2026-11-13-brouter-port-closing/s68-route-ways-entry.md.

/**
 * One square file's read, as Forager asks for it. The cells must lie in [square]: a square file carries no position of
 * its own but its name, which is Forager's, so the entry cannot check it (Forager groups its cells by square first).
 */
class RouteWaysRequest(
    /** The square's `.rd5` file. */
    val square: Path,
    /** The `lookups.dat` the square was built with. */
    val lookups: Path,
    /** The micro-cache cells to read, in this order: cell i is (lonCells[i], latCells[i]), in 1/[divisor] degree. */
    val lonCells: IntArray,
    val latCells: IntArray,
    /** The cell size the caller computed its cells with: 32 for Forager's squares. */
    val divisor: Int,
)

/**
 * One link that has a description: its points in micro-degrees (the node, the link's decoded geometry, the target), and
 * `getKeyValueDescription(reverse, bitmap)`'s text unchanged.
 */
class RouteWaysLink(val iLat: IntArray, val iLon: IntArray, val description: String)

/** What a route-ways read returns. */
sealed interface RouteWaysOutcome {
    /** The square's divisor and its links with a description, in walk order. */
    class Read(val divisor: Int, val links: List<RouteWaysLink>) : RouteWaysOutcome

    /** The square's cells are 1/[divisor] degree, not the 1/[RouteWaysRequest.divisor] asked for; no cell was read. */
    class OtherDivisor(val divisor: Int) : RouteWaysOutcome
}

/**
 * Reads one square's links, blocking; the caller picks the thread. A bad `lookups.dat` leaves as the engine's
 * `RuntimeException`, a missing or bad square as okio's `IOException`, both unchanged.
 */
fun readRouteWays(request: RouteWaysRequest): RouteWaysOutcome {
    require(request.lonCells.size == request.latCells.size) { "lonCells and latCells differ in size" }
    // BRouterRouteWays.kt:60-62: the way context on lookups.dat alone, no profile. One per call, shared by its cells.
    val meta = BExpressionMetaData()
    val wayContext = BExpressionContextWay(meta)
    meta.readMetaData(FileSystem.SYSTEM, request.lookups)
    val geometry = GeometryDecoder() // :72
    val buffers = DataBuffers() // :77, one per square
    val square = PhysicalFile(FileSystem.SYSTEM, request.square, buffers, -1, -1) // :78, before the try, as there
    try {
        // :80, where Forager words its own message: cells of another size would read other cells (design file).
        if (square.divisor != request.divisor) return RouteWaysOutcome.OtherDivisor(square.divisor)
        val cell = 1_000_000L / square.divisor
        val links = mutableListOf<RouteWaysLink>()
        for (c in request.lonCells.indices) {
            val lonCell = request.lonCells[c]
            val latCell = request.latCells[c]
            val degree = OsmFile(square, (lonCell * cell / 1_000_000).toInt(), (latCell * cell / 1_000_000).toInt(), buffers) // :82
            if (!degree.hasData()) continue // :83, the cell's degree has no data in this square
            // :84; with reallyDecode true the call never returns null (OsmFile.kt:137-139), kept as Forager's line.
            val cache = degree.createMicroCache(lonCell, latCell, buffers, null, null, true, null) ?: continue
            readCell(cache, OsmNodesMap(), wayContext, geometry, links) // :85, a nodes map per cell
        }
        return RouteWaysOutcome.Read(square.divisor, links)
    } finally {
        square.close() // :115
    }
}

/** `BRouterRouteWays.kt:86-111`: each node the cache gives, each of its links with a description, its points and text. */
private fun readCell(
    cache: MicroCache,
    nodes: OsmNodesMap,
    wayContext: BExpressionContextWay,
    geometry: GeometryDecoder,
    links: MutableList<RouteWaysLink>,
) {
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
                val lat = mutableListOf(node.ilat)
                val lon = mutableListOf(node.ilon)
                var t = link.geometry?.let { geometry.decodeGeometry(it, node, target, reverse) }
                while (t != null) {
                    lat += t.ilat
                    lon += t.ilon
                    t = t.next
                }
                lat += target.ilat
                lon += target.ilon
                links += RouteWaysLink(lat.toIntArray(), lon.toIntArray(), wayContext.getKeyValueDescription(reverse, bitmap))
            }
            link = link.getNext(node)
        }
    }
}
