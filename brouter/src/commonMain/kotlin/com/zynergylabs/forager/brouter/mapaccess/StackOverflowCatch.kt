package com.zynergylabs.forager.brouter.mapaccess

/**
 * `OsmNodesMap.cleanupPeninsulas`'s `try { ... } catch (StackOverflowError soe) { }` (`OsmNodesMap.java:62-64`), which
 * drops one subtree's cleanup silently when the recursion overflows. RECORD -50's ruling:
 * - **on the JVM** the catch is kept, so logic runs stay equal to the oracle's, and it is logged when it fires;
 * - **on iOS** there is no catch: a Kotlin/Native thread cannot catch a stack overflow, so an overflow ends the process.
 *   A known difference, recorded in the step 5b report. Step 10's simulator run measures the router's real stack at
 *   the measured depth (5,679 frames); if it overflows, the router runs on a thread with an explicit stack size, a
 *   decision recorded for later.
 *
 * Runs [block] and returns true if it was cut short by a caught stack overflow (JVM only), else false.
 */
internal expect fun runCatchingStackOverflow(block: () -> Unit): Boolean
