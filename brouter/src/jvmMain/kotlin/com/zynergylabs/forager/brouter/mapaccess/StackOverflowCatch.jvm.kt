package com.zynergylabs.forager.brouter.mapaccess

/** The JVM keeps Java's catch (`OsmNodesMap.java:62-64`) and logs it, where Java is silent (RECORD -50). */
internal actual fun runCatchingStackOverflow(block: () -> Unit): Boolean =
    try {
        block()
        false
    } catch (soe: StackOverflowError) {
        System.err.println(
            "brouter: OsmNodesMap.cleanupPeninsulas caught a StackOverflowError; that subtree's cleanup is skipped, " +
                "as in Java (OsmNodesMap.java:62-64)",
        )
        true
    }
