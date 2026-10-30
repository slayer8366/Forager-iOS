package com.zynergylabs.forager.brouter.mapaccess

import com.zynergylabs.forager.brouter.codec.MicroCache
import com.zynergylabs.forager.brouter.codec.WaypointMatcher
import com.zynergylabs.forager.brouter.expressions.BExpressionContextWay
import okio.FileSystem
import okio.Path

/**
 * Port of BRouter 1.7.10's `btools.mapaccess.NodesCache`, its file half (step 5a): the tile cache and the segment
 * reads. The graph half (`obtainNonHollowNode`, `expandHollowLinkTargets`, `hasHollowLinkTargets`, `getStartNode`,
 * `getGraphNode`, `matchWaypointsToNodes`, `preloadPosition`) needs `OsmNode`, `MatchedWaypoint` and the weaver, and
 * arrives with steps 5b and 5c.
 */
internal class NodesCache(
    private val fileSystem: FileSystem,
    segmentDir: Path,
    ctxWay: BExpressionContextWay?,
    forceSecondaryData: Boolean,
    maxmem: Long,
    oldCache: NodesCache?,
    detailed: Boolean,
) {
    var nodesMap: OsmNodesMap = OsmNodesMap()
    var waypointMatcher: WaypointMatcher? = null
    var first_file_access_failed: Boolean = false
    var first_file_access_name: String? = null

    init {
        TODO("item I step 5a")
    }

    fun clean(all: Boolean): Unit = TODO("item I step 5a")

    fun loadSegmentFor(ilon: Int, ilat: Int): Int = TODO("item I step 5a")

    fun getSegmentFor(ilon: Int, ilat: Int): MicroCache? = TODO("item I step 5a")

    fun close(): Unit = TODO("item I step 5a")

    fun getElevationType(ilon: Int, ilat: Int): Int = TODO("item I step 5a")
}
