package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.mapaccess.MatchedWaypoint
import com.zynergylabs.forager.brouter.mapaccess.NodesCache
import com.zynergylabs.forager.brouter.mapaccess.OsmNodePairSet
import com.zynergylabs.forager.brouter.util.SortedHeap
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

/**
 * BRouter 1.7.10's `btools.router.RoutingEngine`, step 8 (RECORD -90; the step 8 report's §1 places every method).
 *
 * Only what Forager's call reaches is ported (`BRouterTrailRouter.kt:143-209` at Forager `947f1be2`): the routing mode,
 * two waypoints, no output or log file, `quite`. What §1 leaves out throws a named "unsupported"; via points (-24) are one.
 * Java's `Thread` base is not kept: Forager calls `doRun` on a thread of its own.
 */
internal class RoutingEngine(
    private val fileSystem: FileSystem,
    private val segmentDir: Path,
    private val waypoints: MutableList<OsmNodeNamed>,
    private val routingContext: RoutingContext,
    private val engineMode: Int,
) {
    private var nodesCache: NodesCache? = null
    private val openSet = SortedHeap<OsmPath>()
    private var matchedWaypoints: MutableList<MatchedWaypoint>? = null
    private var nodeLimit = 0
    private val islandNodePairs = OsmNodePairSet(MAXNODES_ISLAND_CHECK)
    private var foundTrack: OsmTrack = OsmTrack()
    private var errorMessage: String? = null

    /** BRouter's spelling. Forager sets it; with no output file it only stops a GPX print, which the port never makes. */
    var quite: Boolean = false

    init {
        // RoutingEngine.java:111-131: a debug log or a stack sampler starts when its file sits two folders above the
        // profile. Neither is ported (§1); refusing beats routing without the log the file asks for.
        val base = javaParent(routingContext.localFunction)?.let { javaParent(it) }
        if (base != null) {
            for (name in listOf("debug.txt", "stacks.txt")) {
                if (fileSystem.exists(base.toPath() / name)) {
                    throw UnsupportedOperationException("RoutingEngine: $name beside the profile folder (the engine's log) is not ported (step 8 report §1)")
                }
            }
        }
        ProfileCache.parseProfile(routingContext, fileSystem)
    }

    fun doRun(maxRunningTime: Long): Unit = TODO("RoutingEngine.doRun: step 8a, RECORD -90")

    fun getFoundTrack(): OsmTrack = foundTrack

    fun getErrorMessage(): String? = errorMessage

    companion object {
        const val BROUTER_ENGINEMODE_ROUTING = 0
        private const val MAXNODES_ISLAND_CHECK = 500

        /** `java.io.File.getParent` on a `/`-separated path: null when there is no separator [I: no other form reaches it]. */
        private fun javaParent(p: String?): String? {
            if (p == null) return null
            val i = p.lastIndexOf('/')
            return when {
                i < 0 -> null
                i == 0 -> if (p.length > 1) "/" else null
                else -> p.substring(0, i)
            }
        }
    }
}
