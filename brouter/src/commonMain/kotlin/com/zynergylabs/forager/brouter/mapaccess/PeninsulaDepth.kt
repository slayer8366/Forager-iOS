package com.zynergylabs.forager.brouter.mapaccess

/**
 * Step 10, session 49 (RECORD -140, decision 2; for -61): how deep [OsmNodesMap]'s `minVisitIdInSubtree` recursion went.
 * Written by the walk only, read and reset by tests only; nothing in the port reads it, so no result depends on it.
 *
 * - [current]: the walk's present depth: one more on entry to `minVisitIdInSubtree`, one less on its one return.
 *   `cleanupPeninsulas` sets it to 0 after each top-level call, so a JVM overflow caught there (which skips the
 *   decrements of the frames it unwinds) does not carry into the next subtree.
 * - [deepest]: the largest [current] reached since the last [reset] (the root call is depth 1).
 * - [overflowsCaught]: the top-level calls `runCatchingStackOverflow` reported cut short since the last [reset] (JVM only;
 *   on iOS an overflow ends the process).
 *
 * One process-wide object: the walk is not reentrant across threads in the tests that read it (one route at a time).
 */
internal object PeninsulaDepth {
    var current: Int = 0
    var deepest: Int = 0
    var overflowsCaught: Int = 0

    fun reset() {
        current = 0
        deepest = 0
        overflowsCaught = 0
    }
}
