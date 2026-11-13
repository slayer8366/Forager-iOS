package com.zynergylabs.forager.brouter

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
fun readRouteWays(request: RouteWaysRequest): RouteWaysOutcome =
    throw NotImplementedError("readRouteWays: s68 stub, the body comes in the next commit")
