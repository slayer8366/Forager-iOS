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
 *
 * Its third field, `overflowsCaught`, counted the JVM-only `StackOverflowError` catch `cleanupPeninsulas` had before
 * session 52; with no recursion that catch went and nothing wrote the field, so session 54 removed it (RECORD -147,
 * decision 3).
 *
 * One process-wide object: the walk is not reentrant across threads in the tests that read it (one route at a time).
 */
internal object PeninsulaDepth {
    var current: Int = 0
    var deepest: Int = 0

    fun reset() {
        current = 0
        deepest = 0
    }
}
