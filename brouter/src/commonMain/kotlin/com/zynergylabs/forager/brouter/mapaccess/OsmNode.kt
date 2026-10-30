package com.zynergylabs.forager.brouter.mapaccess

import com.zynergylabs.forager.brouter.codec.MicroCache
import com.zynergylabs.forager.brouter.util.IByteArrayUnifier
import com.zynergylabs.forager.brouter.util.LibmSeam

/** Port of BRouter 1.7.10's `btools.mapaccess.OsmNode`. Step 5b's stub (item I): signatures only. */
internal open class OsmNode : OsmLink, OsmPos {
    var ilat: Int = 0
    var ilon: Int = 0
    var selev: Short = Short.MIN_VALUE
    var nodeDescription: ByteArray? = null
    var firstRestriction: TurnRestriction? = null
    var visitID: Int = 0
    var firstlink: OsmLink? = null

    constructor()

    constructor(ilon: Int, ilat: Int) {
        TODO("step 5b")
    }

    fun addTurnRestriction(tr: TurnRestriction): Unit = TODO("step 5b")

    final override fun getILat(): Int = TODO("step 5b")

    final override fun getILon(): Int = TODO("step 5b")

    final override fun getSElev(): Short = TODO("step 5b")

    final override fun getElev(): Double = TODO("step 5b")

    fun addLink(link: OsmLink, isReverse: Boolean, tn: OsmNode): Unit = TODO("step 5b")

    final override fun calcDistance(libm: LibmSeam, p: OsmPos): Int = TODO("step 5b")

    fun parseNodeBody(mc: MicroCache, hollowNodes: OsmNodesMap, expCtxWay: IByteArrayUnifier): Unit = TODO("step 5b")

    open fun addLink(
        linklon: Int,
        linklat: Int,
        description: ByteArray?,
        geometry: ByteArray?,
        hollowNodes: OsmNodesMap,
        isReverse: Boolean,
    ): Unit = TODO("step 5b")

    fun isHollow(): Boolean = TODO("step 5b")

    fun setHollow(): Unit = TODO("step 5b")

    final override fun getIdFromPos(): Long = TODO("step 5b")

    open fun vanish(): Unit = TODO("step 5b")

    fun unlinkLink(link: OsmLink): Unit = TODO("step 5b")

    final override fun equals(other: Any?): Boolean = TODO("step 5b")

    final override fun hashCode(): Int = TODO("step 5b")
}
