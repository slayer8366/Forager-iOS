package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.mapaccess.OsmLink
import com.zynergylabs.forager.brouter.mapaccess.OsmLinkHolder
import com.zynergylabs.forager.brouter.mapaccess.OsmNode
import com.zynergylabs.forager.brouter.mapaccess.OsmTransferNode
import com.zynergylabs.forager.brouter.mapaccess.TurnRestriction
import kotlin.jvm.JvmField

/**
 * Port of BRouter 1.7.10's `btools.router.OsmPath`: one path in the search, ending in a link.
 *
 * Java's casts are kept exactly (step 6 report §8): `cost += rc.nogoCost` is `(int) (cost + nogoCost)`; float compares
 * against `0.` are float against double, done here as Java does them. `(lon0 + lat1) >> 1` in the start-direction
 * branch is a slip in Java, but that branch is a named "unsupported" (RECORD -97): Forager passes no heading, and
 * `rc.startDirectionValid` is set only at `RoutingEngine.java:1931-1932`. `AreaInfo`'s check (`:110-112`) refuses too,
 * since Forager sets no area info. Java's static `seg` (the direct segments' counter) is a companion `var`.
 */
internal abstract class OsmPath : OsmLinkHolder {
    /** The cost of that path (a modified distance). */
    var cost: Int = 0

    /** The elevation assumed for that path, which can have a value when the corresponding node has not. */
    var selev: Short = 0
    var airdistance: Int = 0 // distance to endpos

    @JvmField // Java's field; the getter below is Java's getSourceNode()
    protected var sourceNode: OsmNode? = null
    @JvmField // Java's field; the getter below is Java's getTargetNode()
    protected var targetNode: OsmNode? = null
    @JvmField // Java's field; the getter below is Java's getLink()
    protected var link: OsmLink? = null
    var originElement: OsmPathElement? = null
    var myElement: OsmPathElement? = null
    private var nextForLink: OsmLinkHolder? = null
    var treedepth: Int = 0

    // the position of the waypoint just before this path position (for angle calculation)
    var originLon: Int = 0
    var originLat: Int = 0

    // the classifier of the segment just before this paths position
    protected var lastClassifier: Float = 0f
    protected var lastInitialCost: Float = 0f
    protected var priorityclassifier: Int = 0
    protected var bitfield: Int = PATH_START_BIT

    private fun getBit(mask: Int): Boolean = (bitfield and mask) != 0

    private fun setBit(mask: Int, bit: Boolean) {
        if (getBit(mask) != bit) bitfield = bitfield xor mask
    }

    fun didEnterDestinationArea(): Boolean = !getBit(HAD_DESTINATION_START_BIT) && getBit(IS_ON_DESTINATION_BIT)

    var message: MessageData? = null

    open fun init(link: OsmLink) {
        this.link = link
        targetNode = link.getTarget(null)
        selev = targetNode!!.getSElev()
        originLon = -1
        originLat = -1
    }

    open fun init(origin: OsmPath, link: OsmLink, refTrack: OsmTrack?, detailMode: Boolean, rc: RoutingContext) {
        if (origin.myElement == null) {
            origin.myElement = OsmPathElement.create(origin)
        }
        this.originElement = origin.myElement
        this.link = link
        this.sourceNode = origin.targetNode
        this.targetNode = link.getTarget(sourceNode)
        this.cost = origin.cost
        this.lastClassifier = origin.lastClassifier
        this.lastInitialCost = origin.lastInitialCost
        this.bitfield = origin.bitfield
        this.priorityclassifier = origin.priorityclassifier
        init(origin)
        addAddionalPenalty(refTrack, detailMode, origin, link, rc)
    }

    protected abstract fun init(orig: OsmPath)

    protected abstract fun resetState()

    protected fun addAddionalPenalty(refTrack: OsmTrack?, detailMode: Boolean, origin: OsmPath, link: OsmLink, rc: RoutingContext) {
        val sourceNode = this.sourceNode!!
        val targetNode = this.targetNode!!
        val description = link.descriptionBitmap
        if (description == null) { // could be a beeline path
            val m = MessageData()
            message = m
            m.turnangle = 0f
            m.time = 1f
            m.energy = 0f
            m.priorityclassifier = 0
            m.classifiermask = 0
            m.lon = targetNode.getILon()
            m.lat = targetNode.getILat()
            m.ele = Short.MIN_VALUE
            m.linkdist = sourceNode.calcDistance(rc.libm, targetNode)
            m.wayKeyValues = "direct_segment=$seg"
            seg++
            return
        }

        val recordTransferNodes = detailMode
        rc.nogoCost = 0.0

        // extract the 3 positions of the first section
        var lon0 = origin.originLon
        var lat0 = origin.originLat

        var lon1 = sourceNode.getILon()
        var lat1 = sourceNode.getILat()
        var ele1 = origin.selev

        var linkdisttotal = 0

        message = if (detailMode) MessageData() else null

        val isReverse = link.isReverse(sourceNode)

        // evaluate the way tags
        val way = rc.expctxWay!!
        way.evaluate(rc.inverseDirection xor isReverse, description)

        // and check if is useful
        val ai = rc.ai
        if (ai != null && ai.polygon!!.isWithin(lon1.toLong(), lat1.toLong())) {
            throw UnsupportedOperationException("unsupported: AreaInfo.checkAreaInfo (OsmPath.java:145-146)")
        }

        // calculate the costfactor inputs
        val costfactor = way.getCostfactor()
        val isTrafficBackbone = cost == 0 && way.getIsTrafficBackbone() > 0f
        val lastpriorityclassifier = priorityclassifier
        priorityclassifier = way.getPriorityClassifier().toInt()

        // *** add initial cost if the classifier changed
        val newClassifier = way.getInitialClassifier()
        val newInitialCost = way.getInitialcost()
        val classifierDiff = newClassifier - lastClassifier
        if (newClassifier.toDouble() != 0.0 && lastClassifier.toDouble() != 0.0 &&
            (classifierDiff > 0.0005 || classifierDiff < -0.0005)
        ) {
            val initialcost = if (rc.inverseDirection) lastInitialCost else newInitialCost
            if (initialcost >= 1000000.0) {
                cost = -1
                return
            }
            val iicost = initialcost.toInt()
            message?.let { it.linkinitcost += iicost }
            cost += iicost
        }
        lastClassifier = newClassifier
        lastInitialCost = newInitialCost

        // *** destination logic: no destination access in between
        val classifiermask = way.getClassifierMask().toInt()
        val newDestination = (classifiermask and 64) != 0
        val oldDestination = getBit(IS_ON_DESTINATION_BIT)
        if (getBit(PATH_START_BIT)) {
            setBit(PATH_START_BIT, false)
            setBit(CAN_LEAVE_DESTINATION_BIT, newDestination)
            setBit(HAD_DESTINATION_START_BIT, newDestination)
        } else {
            if (oldDestination && !newDestination) {
                if (getBit(CAN_LEAVE_DESTINATION_BIT)) {
                    setBit(CAN_LEAVE_DESTINATION_BIT, false)
                } else {
                    cost = -1
                    return
                }
            }
        }
        setBit(IS_ON_DESTINATION_BIT, newDestination)

        var transferNode: OsmTransferNode? = if (link.geometry == null) null
        else rc.geometryDecoder.decodeGeometry(link.geometry, sourceNode, targetNode, isReverse)

        var nsection = 0
        while (true) {
            originLon = lon1
            originLat = lat1

            val lon2: Int
            val lat2: Int
            var ele2: Short
            val originEle2: Short

            if (transferNode == null) {
                lon2 = targetNode.ilon
                lat2 = targetNode.ilat
                originEle2 = targetNode.selev
            } else {
                lon2 = transferNode.ilon
                lat2 = transferNode.ilat
                originEle2 = transferNode.selev
            }
            ele2 = originEle2

            var isStartpoint = lon0 == -1 && lat0 == -1

            // check turn restrictions (n detail mode (=final pass) no TR to not mess up voice hints)
            if (nsection == 0 && rc.considerTurnRestrictions && !detailMode && !isStartpoint) {
                if (if (rc.inverseDirection) {
                        TurnRestriction.isTurnForbidden(sourceNode.firstRestriction, lon2, lat2, lon0, lat0, rc.bikeMode || rc.footMode, rc.carMode)
                    } else {
                        TurnRestriction.isTurnForbidden(sourceNode.firstRestriction, lon0, lat0, lon2, lat2, rc.bikeMode || rc.footMode, rc.carMode)
                    }
                ) {
                    cost = -1
                    return
                }
            }

            // if recording, new MessageData for each section (needed for turn-instructions)
            val m0 = message
            if (m0 != null && m0.wayKeyValues != null) {
                originElement!!.message = m0
                message = MessageData()
            }

            var dist = rc.calcDistance(lon1, lat1, lon2, lat2)

            var stopAtEndpoint = false
            if (rc.shortestmatch) {
                if (rc.isEndpoint) {
                    stopAtEndpoint = true
                    ele2 = interpolateEle(ele1, ele2, rc.wayfraction)
                } else {
                    // we just start here, reset everything
                    cost = 0
                    resetState()
                    lon0 = -1 // reset turncost-pipe
                    lat0 = -1
                    isStartpoint = true

                    if (recordTransferNodes) {
                        if (rc.wayfraction > 0.0) {
                            ele1 = interpolateEle(ele1, ele2, 1.0 - rc.wayfraction)
                            originElement = OsmPathElement.create(rc.ilonshortest, rc.ilatshortest, ele1, null)
                        } else {
                            originElement = null // prevent duplicate point
                        }
                    }

                    if (rc.checkPendingEndpoint()) {
                        dist = rc.calcDistance(rc.ilonshortest, rc.ilatshortest, lon2, lat2)
                        if (rc.shortestmatch) {
                            stopAtEndpoint = true
                            ele2 = interpolateEle(ele1, ele2, rc.wayfraction)
                        }
                    }
                }
            }

            message?.let { it.linkdist += dist }
            linkdisttotal += dist

            // apply a start-direction if appropriate (by faking the origin position)
            if (isStartpoint) {
                if (rc.startDirectionValid) {
                    // RECORD -97: Java's `Math.sin`/`Math.cos` branch (`OsmPath.java:282-283`) is not ported.
                    throw UnsupportedOperationException("unsupported: OsmPath's start direction (OsmPath.java:282-283)")
                } else {
                    lon0 = lon1 - (lon2 - lon1)
                    lat0 = lat1 - (lat2 - lat1)
                }
            }
            val angle = rc.anglemeter.calcAngle(lon0, lat0, lon1, lat1, lon2, lat2)
            val cosangle = rc.anglemeter.cosAngle

            // *** elevation stuff
            var deltaH = 0.0
            if (ele2 == Short.MIN_VALUE) ele2 = ele1
            if (ele1 != Short.MIN_VALUE) {
                deltaH = (ele2 - ele1) / 4.0
                if (rc.inverseDirection) {
                    deltaH = -deltaH
                }
            }

            val elevation = if (ele2 == Short.MIN_VALUE) 100.0 else ele2 / 4.0

            var sectionCost = processWaySection(rc, dist.toDouble(), deltaH, elevation, angle, cosangle, isStartpoint, nsection, lastpriorityclassifier)
            if ((sectionCost < 0.0 || costfactor > 9998.0 && !detailMode) || sectionCost + cost >= 2000000000.0) {
                cost = -1
                return
            }

            if (isTrafficBackbone) {
                sectionCost = 0.0
            }

            cost += sectionCost.toInt()

            // compute kinematic
            computeKinematic(rc, dist.toDouble(), deltaH, detailMode)

            message?.let { m ->
                m.turnangle = angle.toFloat()
                m.time = getTotalTime().toFloat()
                m.energy = getTotalEnergy().toFloat()
                m.priorityclassifier = priorityclassifier
                m.classifiermask = classifiermask
                m.lon = lon2
                m.lat = lat2
                m.ele = originEle2
                m.wayKeyValues = way.getKeyValueDescription(isReverse, description)
            }

            if (stopAtEndpoint) {
                if (recordTransferNodes) {
                    val e = OsmPathElement.create(rc.ilonshortest, rc.ilatshortest, originEle2, originElement)
                    originElement = e
                    e.cost = cost
                    message?.let { e.message = it }
                }
                if (rc.nogoCost < 0) {
                    cost = -1
                } else {
                    cost = (cost + rc.nogoCost).toInt()
                }
                return
            }

            if (transferNode == null) {
                // *** penalty for being part of the reference track
                if (refTrack != null && refTrack.containsNode(targetNode) && refTrack.containsNode(sourceNode)) {
                    val reftrackcost = linkdisttotal
                    cost += reftrackcost
                }
                selev = ele2
                break
            }
            transferNode = transferNode.next

            if (recordTransferNodes) {
                val e = OsmPathElement.create(lon2, lat2, originEle2, originElement)
                originElement = e
                e.cost = cost
            }
            lon0 = lon1
            lat0 = lat1
            lon1 = lon2
            lat1 = lat2
            ele1 = ele2
            nsection++
        }

        // check for nogo-matches (after the *actual* start of segment)
        if (rc.nogoCost < 0) {
            cost = -1
            return
        } else {
            cost = (cost + rc.nogoCost).toInt()
        }

        // add target-node costs
        val targetCost = processTargetNode(rc)
        if (targetCost < 0.0 || targetCost + cost >= 2000000000.0) {
            cost = -1
            return
        }
        cost += targetCost.toInt()
    }

    /** Java: `(short) (e1 * (1. - fraction) + e2 * fraction)`, a double narrowed through `int` to `short`. */
    fun interpolateEle(e1: Short, e2: Short, fraction: Double): Short {
        if (e1 == Short.MIN_VALUE || e2 == Short.MIN_VALUE) {
            return Short.MIN_VALUE
        }
        return (e1 * (1.0 - fraction) + e2 * fraction).toInt().toShort()
    }

    protected abstract fun processWaySection(
        rc: RoutingContext, dist: Double, delta_h: Double, elevation: Double, angle: Double, cosangle: Double,
        isStartpoint: Boolean, nsection: Int, lastpriorityclassifier: Int,
    ): Double

    protected abstract fun processTargetNode(rc: RoutingContext): Double

    protected open fun computeKinematic(rc: RoutingContext, dist: Double, delta_h: Double, detailMode: Boolean) {
    }

    abstract fun elevationCorrection(): Int

    abstract fun definitlyWorseThan(p: OsmPath): Boolean

    fun getSourceNode(): OsmNode? = sourceNode

    fun getTargetNode(): OsmNode? = targetNode

    fun getLink(): OsmLink? = link

    override fun setNextForLink(holder: OsmLinkHolder?) {
        nextForLink = holder
    }

    override fun getNextForLink(): OsmLinkHolder? = nextForLink

    open fun getTotalTime(): Double = 0.0

    open fun getTotalEnergy(): Double = 0.0

    companion object {
        private const val PATH_START_BIT = 1
        private const val CAN_LEAVE_DESTINATION_BIT = 2
        private const val IS_ON_DESTINATION_BIT = 4
        private const val HAD_DESTINATION_START_BIT = 8

        /** Java's `static int seg = 1`, the direct segments' counter. */
        var seg: Int = 1
    }
}
