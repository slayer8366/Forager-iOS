package com.zynergylabs.forager.brouter.mapaccess

/**
 * iOS has no catch: a Kotlin/Native thread cannot catch a stack overflow, so one ends the process instead of being
 * swallowed as in Java (`OsmNodesMap.java:62-64`). A known difference (RECORD -50); see the common declaration.
 */
internal actual fun runCatchingStackOverflow(block: () -> Unit): Boolean {
    block()
    return false
}
