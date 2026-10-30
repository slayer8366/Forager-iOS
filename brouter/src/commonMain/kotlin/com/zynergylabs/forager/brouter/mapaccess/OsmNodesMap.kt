package com.zynergylabs.forager.brouter.mapaccess

import com.zynergylabs.forager.brouter.util.ByteArrayUnifier
import com.zynergylabs.forager.brouter.util.LibmSeam

/** Port of BRouter 1.7.10's `btools.mapaccess.OsmNodesMap`. Step 5b's stub (item I): signatures only. */
internal class OsmNodesMap {
    private val hmap = NodeHashMap(4096)
    private val abUnifier = ByteArrayUnifier(16384, false)

    var nodesCreated: Int = 0
    var maxmem: Long = 0
    var lastVisitID: Int = 1000
    var baseID: Int = 1000

    var destination: OsmNode? = null
    var currentPathCost: Int = 0
    var currentMaxCost: Int = 1000000000

    var endNode1: OsmNode? = null
    var endNode2: OsmNode? = null

    var cleanupMode: Int = 0

    fun cleanupAndCount(nodes: Array<OsmNode>): Unit = TODO("step 5b")

    fun isInMemoryBounds(npaths: Int, extend: Boolean): Boolean = TODO("step 5b")

    fun canEscape(n0: OsmNode): Boolean = TODO("step 5b")

    fun clearTemp(): Unit = TODO("step 5b")

    fun collectOutreachers(libm: LibmSeam): Unit = TODO("step 5b")

    fun getByteArrayUnifier(): ByteArrayUnifier = TODO("step 5b")

    fun get(ilon: Int, ilat: Int): OsmNode? = TODO("step 5b")

    fun remove(node: OsmNode): Unit = TODO("step 5b")

    fun put(node: OsmNode): OsmNode? = TODO("step 5b")
}
