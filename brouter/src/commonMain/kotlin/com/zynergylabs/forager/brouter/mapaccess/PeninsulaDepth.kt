package com.zynergylabs.forager.brouter.mapaccess

/**
 * Step 10, session 49 (RECORD -140, decision 2; for -61): how deep [OsmNodesMap]'s `minVisitIdInSubtree` walk went.
 * Written by the walk only, read and reset by tests only; nothing in the port reads it, so no result depends on it.
 * Since session 52 (RECORD -144) the walk keeps its own stack in place of recursion, and this counts that stack's size.
 *
 * - [current]: the walk's present depth, the number of live frames on its stack: one more on each push, one less on
 *   each pop. It is back to 0 when each top-level walk ends.
 * - [deepest]: the largest [current] reached since the last [reset] (the root frame is depth 1): the depth Java's
 *   recursion reaches on the same walk, as the recursive port counted it before session 52.
 * - [overflowsCaught]: always 0 since session 52. `cleanupPeninsulas` had a JVM-only `StackOverflowError` catch that
 *   counted here; with no recursion nothing there can overflow, so the catch is removed and nothing writes this. It is
 *   kept so the tests that print it ("overflows caught") read as before; any other value would be a fault.
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
