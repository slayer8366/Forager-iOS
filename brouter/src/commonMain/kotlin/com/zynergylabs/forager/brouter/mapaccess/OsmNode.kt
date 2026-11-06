package com.zynergylabs.forager.brouter.mapaccess

import com.zynergylabs.forager.brouter.codec.MicroCache
import com.zynergylabs.forager.brouter.codec.MicroCache2
import com.zynergylabs.forager.brouter.util.CheapRuler
import com.zynergylabs.forager.brouter.util.IByteArrayUnifier
import com.zynergylabs.forager.brouter.util.LibmSeam
import com.zynergylabs.forager.brouter.util.javaRound
import kotlin.math.max

/**
 * Port of BRouter 1.7.10's `btools.mapaccess.OsmNode`. A node is also a link (Java's "technical inheritance"): the first
 * link woven to a node may be the node object itself.
 *
 * Not ported: `toString`. `OsmNode(long)` and `parseNodeBody2` are ported in step 5d (RECORD -70, -74): Forager's
 * route-ways read reaches them (`BRouterRouteWays.kt:88-89` at a0535ece).
 */
internal open class OsmNode : OsmLink, OsmPos {
    /** The latitude. */
    var ilat: Int = 0

    /** The longitude. */
    var ilon: Int = 0

    /** The elevation. */
    var selev: Short = Short.MIN_VALUE

    /** The node tags, if any. */
    var nodeDescription: ByteArray? = null

    var firstRestriction: TurnRestriction? = null

    var visitID: Int = 0

    /**
     * Not in Java: [NodeHashMap]'s stand-in for `System.identityHashCode`, given at this node's first tie-break in a
     * tree bin and kept for the object's life, as HotSpot keeps an identity hash (RECORD -50, -53). 0 means not yet given.
     */
    var tieBreakId: Int = 0

    /** The links to other nodes. */
    var firstlink: OsmLink? = null

    constructor()

    constructor(ilon: Int, ilat: Int) {
        this.ilon = ilon
        this.ilat = ilat
    }

    /** Java: `ilon = (int) (id >> 32); ilat = (int) (id & 0xffffffff);` (`OsmNode.java:57-60`). The int mask widens to -1L, so `ilat` is the low 32 bits. */
    constructor(id: Long) {
        ilon = (id shr 32).toInt()
        ilat = id.toInt()
    }

    fun addTurnRestriction(tr: TurnRestriction) {
        tr.next = firstRestriction
        firstRestriction = tr
    }

    // interface OsmPos
    final override fun getILat(): Int = ilat

    final override fun getILon(): Int = ilon

    final override fun getSElev(): Short = selev

    final override fun getElev(): Double = selev / 4.0

    fun addLink(link: OsmLink, isReverse: Boolean, tn: OsmNode) {
        if (link === firstlink) {
            throw IllegalArgumentException("UUUUPS")
        }

        if (isReverse) {
            link.n1 = tn
            link.n2 = this
            link.next = tn.firstlink
            link.previous = firstlink
            tn.firstlink = link
            firstlink = link
        } else {
            link.n1 = this
            link.n2 = tn
            link.next = firstlink
            link.previous = tn.firstlink
            tn.firstlink = link
            firstlink = link
        }
    }

    /** Java: `(int) Math.max(1.0, Math.round(CheapRuler.distance(...)))`, with the scale table taken from [libm]. */
    final override fun calcDistance(libm: LibmSeam, p: OsmPos): Int =
        max(1.0, javaRound(CheapRuler.distance(libm, ilon, ilat, p.getILon(), p.getILat())).toDouble()).toInt()

    /**
     * Java decodes a `MicroCache2` here and rejects any other cache. A `MicroCache2` is built when `hollowNodes` is null
     * (`OsmFile.java:139-140`), as Forager's route-ways read calls it (`BRouterRouteWays.kt:84` at a0535ece).
     */
    fun parseNodeBody(mc: MicroCache, hollowNodes: OsmNodesMap, expCtxWay: IByteArrayUnifier) {
        if (mc is MicroCache2) {
            parseNodeBody2(mc, hollowNodes, expCtxWay)
        } else {
            throw IllegalArgumentException("unknown cache version: " + mc::class)
        }
    }

    /** Port of `OsmNode.java:117-152`. */
    fun parseNodeBody2(mc: MicroCache2, hollowNodes: OsmNodesMap, expCtxWay: IByteArrayUnifier) {
        val abUnifier = hollowNodes.getByteArrayUnifier()

        // read turn restrictions
        while (mc.readBoolean()) {
            val tr = TurnRestriction()
            tr.exceptions = mc.readShort()
            tr.isPositive = mc.readBoolean()
            tr.fromLon = mc.readInt()
            tr.fromLat = mc.readInt()
            tr.toLon = mc.readInt()
            tr.toLat = mc.readInt()
            addTurnRestriction(tr)
        }

        selev = mc.readShort()
        val nodeDescSize = mc.readVarLengthUnsigned()
        nodeDescription = if (nodeDescSize == 0) null else mc.readUnified(nodeDescSize, abUnifier)

        while (mc.hasMoreData()) {
            // read link data
            val endPointer = mc.getEndPointer()
            val linklon = ilon + mc.readVarLengthSigned()
            val linklat = ilat + mc.readVarLengthSigned()
            val sizecode = mc.readVarLengthUnsigned()
            val isReverse = (sizecode and 1) != 0
            var description: ByteArray? = null
            val descSize = sizecode shr 1
            if (descSize > 0) {
                description = mc.readUnified(descSize, expCtxWay)
            }
            val geometry = mc.readDataUntil(endPointer)

            addLink(linklon, linklat, description, geometry, hollowNodes, isReverse)
        }
        hollowNodes.remove(this)
    }

    open fun addLink(
        linklon: Int,
        linklat: Int,
        description: ByteArray?,
        geometry: ByteArray?,
        hollowNodes: OsmNodesMap,
        isReverse: Boolean,
    ) {
        if (linklon == ilon && linklat == ilat) {
            return // skip self-ref
        }

        var tn: OsmNode? = null // find the target node
        var link: OsmLink? = null

        // ...in our known links
        var l = firstlink
        while (l != null) {
            val t = l.getTarget(this)!!
            if (t.ilon == linklon && t.ilat == linklat) {
                tn = t
                if (isReverse || (l.descriptionBitmap == null && !l.isReverse(this))) {
                    link = l // the correct one that needs our data
                    break
                }
            }
            l = l.getNext(this)
        }
        if (tn == null) { // .. not found, then check the hollow nodes
            tn = hollowNodes.get(linklon, linklat) // target node
            if (tn == null) { // node not yet known, create a new hollow proxy
                tn = OsmNode(linklon, linklat)
                tn.setHollow()
                hollowNodes.put(tn)
                link = tn
                addLink(tn, isReverse, tn) // technical inheritance: link instance in node
            }
        }
        if (link == null) {
            link = OsmLink()
            addLink(link, isReverse, tn)
        }
        if (!isReverse) {
            link.descriptionBitmap = description
            link.geometry = geometry
        }
    }

    fun isHollow(): Boolean = selev.toInt() == -12345

    fun setHollow() {
        selev = -12345
    }

    final override fun getIdFromPos(): Long = (ilon.toLong() shl 32) or ilat.toLong()

    open fun vanish() {
        if (!isHollow()) {
            var l = firstlink
            while (l != null) {
                val target = l.getTarget(this)!!
                val nextLink = l.getNext(this)
                if (!target.isHollow()) {
                    unlinkLink(l)
                    if (!l.isLinkUnused()) {
                        target.unlinkLink(l)
                    }
                }
                l = nextLink
            }
        }
    }

    fun unlinkLink(link: OsmLink) {
        val n = link.clear(this)

        if (link === firstlink) {
            firstlink = n
            return
        }
        var l = firstlink
        while (l != null) {
            // if ( l.isReverse( this ) )
            if (l.n1 !== this && l.n1 != null) { // isReverse inline
                val nl = l.previous
                if (nl === link) {
                    l.previous = n
                    return
                }
                l = nl
            } else if (l.n2 !== this && l.n2 != null) {
                val nl = l.next
                if (nl === link) {
                    l.next = n
                    return
                }
                l = nl
            } else {
                throw IllegalArgumentException("unlinkLink: unknown source")
            }
        }
    }

    final override fun equals(other: Any?): Boolean = (other as OsmNode).ilon == ilon && other.ilat == ilat

    final override fun hashCode(): Int = ilon + ilat
}
