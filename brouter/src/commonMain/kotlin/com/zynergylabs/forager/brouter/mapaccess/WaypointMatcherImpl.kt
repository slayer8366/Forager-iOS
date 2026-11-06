package com.zynergylabs.forager.brouter.mapaccess

import com.zynergylabs.forager.brouter.codec.WaypointMatcher
import com.zynergylabs.forager.brouter.util.CheapAngleMeter
import com.zynergylabs.forager.brouter.util.CheapRuler
import com.zynergylabs.forager.brouter.util.LibmSeam
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Port of BRouter 1.7.10's `btools.mapaccess.WaypointMatcherImpl` (step 5c, item I), in Java's order. The decoder feeds
 * it the geometry of every way the profile allows; it keeps, per waypoint, the nearest segment and both of its
 * directions in `wayNearest`, sorted by radius and then by the difference from the direction to the next waypoint.
 * That second key is where `atan2` decides a tie (item I's "(a) to (c)"); it reaches `atan2` through [libm], step 2b's
 * seam, as every `CheapRuler` and `CheapAngleMeter` call does.
 */
internal class WaypointMatcherImpl(
    private val libm: LibmSeam,
    private val waypoints: MutableList<MatchedWaypoint>,
    maxDistance: Double,
    private val islandPairs: OsmNodePairSet,
) : WaypointMatcher {
    private var lonStart = 0
    private var latStart = 0
    private var lonTarget = 0
    private var latTarget = 0
    private var anyUpdate = false
    private var lonLast = 0
    private var latLast = 0
    var useAsStartWay: Boolean = true
    private var maxWptIdx = 0
    private var maxDistance: Double = maxDistance
    var useDynamicRange: Boolean = false

    // sort result list (WaypointMatcherImpl.java:66-73)
    private val comparator = Comparator<MatchedWaypoint> { mw1, mw2 ->
        val cmpDist = javaDoubleCompare(mw1.radius, mw2.radius)
        if (cmpDist != 0) return@Comparator cmpDist
        javaDoubleCompare(mw1.directionDiff, mw2.directionDiff)
    }

    init {
        var md = maxDistance
        var last: MatchedWaypoint? = null
        if (md < 0.0) {
            this.maxDistance *= -1
            md *= -1
            useDynamicRange = true
        }

        for (mwp in waypoints) {
            mwp.radius = md
            if (last != null && mwp.directionToNext == -1.0) {
                last.directionToNext = CheapAngleMeter.getDirection(
                    libm, last.waypoint!!.ilon, last.waypoint!!.ilat, mwp.waypoint!!.ilon, mwp.waypoint!!.ilat,
                )
            }
            last = mwp
        }
        // last point has no angle so we are looking back
        val lastidx = waypoints.size - 2
        // Java dereferences `last` here, so an empty list throws a NullPointerException; so does this.
        if (lastidx < 0) {
            last!!.directionToNext = -1.0
        } else {
            last!!.directionToNext = CheapAngleMeter.getDirection(
                libm, last.waypoint!!.ilon, last.waypoint!!.ilat, waypoints[lastidx].waypoint!!.ilon, waypoints[lastidx].waypoint!!.ilat,
            )
        }
        maxWptIdx = waypoints.size - 1
    }

    private fun checkSegment(lon1: Int, lat1: Int, lon2: Int, lat2: Int) {
        // todo: bounding-box pre-filter

        val lonlat2m = CheapRuler.getLonLatToMeterScales(libm, (lat1 + lat2) shr 1)
        val dlon2m = lonlat2m[0]
        val dlat2m = lonlat2m[1]

        val dx = (lon2 - lon1) * dlon2m
        val dy = (lat2 - lat1) * dlat2m
        val d = sqrt(dy * dy + dx * dx)

        if (d == 0.0) return

        for (i in 0 until waypoints.size) {
            if (!useAsStartWay && i == 0) continue
            val mwp = waypoints[i]

            if (mwp.wpttype == MatchedWaypoint.WAYPOINT_TYPE_DIRECT &&
                (i == 0 || waypoints[i - 1].wpttype == MatchedWaypoint.WAYPOINT_TYPE_DIRECT)
            ) {
                if (mwp.crosspoint == null) {
                    val cp = OsmNode()
                    cp.ilon = mwp.waypoint!!.ilon
                    cp.ilat = mwp.waypoint!!.ilat
                    mwp.crosspoint = cp
                    mwp.hasUpdate = true
                    anyUpdate = true
                }
                continue
            }

            val wp = mwp.waypoint!!
            val x1 = (lon1 - wp.ilon) * dlon2m
            val y1 = (lat1 - wp.ilat) * dlat2m
            val x2 = (lon2 - wp.ilon) * dlon2m
            val y2 = (lat2 - wp.ilat) * dlat2m
            val r12 = x1 * x1 + y1 * y1
            val r22 = x2 * x2 + y2 * y2
            var radius = abs(if (r12 < r22) y1 * dx - x1 * dy else y2 * dx - x2 * dy) / d

            if (radius <= mwp.radius) {
                var s1 = x1 * dx + y1 * dy
                var s2 = x2 * dx + y2 * dy

                if (s1 < 0.0) {
                    s1 = -s1
                    s2 = -s2
                }
                if (s2 > 0.0) {
                    radius = sqrt(if (s1 < s2) r12 else r22)

                    if (radius > mwp.radius) {
                        continue
                    }
                }
                // new match for that waypoint
                mwp.radius = radius // shortest distance to way
                mwp.hasUpdate = true
                anyUpdate = true
                // calculate crosspoint
                val cp = mwp.crosspoint ?: OsmNode().also { mwp.crosspoint = it }
                if (s2 < 0.0) {
                    val wayfraction = -s2 / (d * d)
                    val xm = x2 - wayfraction * dx
                    val ym = y2 - wayfraction * dy
                    cp.ilon = (xm / dlon2m + wp.ilon).toInt()
                    cp.ilat = (ym / dlat2m + wp.ilat).toInt()
                } else if (s1 > s2) {
                    cp.ilon = lon2
                    cp.ilat = lat2
                } else {
                    cp.ilon = lon1
                    cp.ilat = lat1
                }
            }
        }
    }

    override fun start(ilonStart: Int, ilatStart: Int, ilonTarget: Int, ilatTarget: Int, useAsStartWay: Boolean): Boolean {
        if (islandPairs.size() > 0) {
            val n1 = (ilonStart.toLong() shl 32) or ilatStart.toLong()
            val n2 = (ilonTarget.toLong() shl 32) or ilatTarget.toLong()
            if (islandPairs.hasPair(n1, n2)) {
                return false
            }
        }
        lonStart = ilonStart
        lonLast = lonStart
        latStart = ilatStart
        latLast = latStart
        lonTarget = ilonTarget
        latTarget = ilatTarget
        anyUpdate = false
        this.useAsStartWay = useAsStartWay
        return true
    }

    override fun transferNode(ilon: Int, ilat: Int) {
        checkSegment(lonLast, latLast, ilon, ilat)
        lonLast = ilon
        latLast = ilat
    }

    override fun end() {
        checkSegment(lonLast, latLast, lonTarget, latTarget)
        if (anyUpdate) {
            for (mwp in waypoints) {
                if (mwp.hasUpdate) {
                    var angle = CheapAngleMeter.getDirection(libm, lonStart, latStart, lonTarget, latTarget)
                    var diff = CheapAngleMeter.getDifferenceFromDirection(mwp.directionToNext, angle)

                    mwp.hasUpdate = false

                    var mw = copyOf(mwp, OsmNode(lonStart, latStart), OsmNode(lonTarget, latTarget), "_w_", diff)
                    updateWayList(mwp.wayNearest, mw)

                    // revers
                    angle = CheapAngleMeter.getDirection(libm, lonTarget, latTarget, lonStart, latStart)
                    diff = CheapAngleMeter.getDifferenceFromDirection(mwp.directionToNext, angle)
                    mw = copyOf(mwp, OsmNode(lonTarget, latTarget), OsmNode(lonStart, latStart), "_w2_", diff)
                    updateWayList(mwp.wayNearest, mw)

                    val way = mwp.wayNearest[0]
                    mwp.crosspoint!!.ilon = way.crosspoint!!.ilon
                    mwp.crosspoint!!.ilat = way.crosspoint!!.ilat
                    mwp.node1 = OsmNode(way.node1!!.ilon, way.node1!!.ilat)
                    mwp.node2 = OsmNode(way.node2!!.ilon, way.node2!!.ilat)
                    mwp.directionDiff = way.directionDiff
                    mwp.radius = way.radius
                }
            }
        }
    }

    /** WaypointMatcherImpl.java:194-206 and 213-225, the two copies `end` makes, field for field in Java's order. */
    private fun copyOf(mwp: MatchedWaypoint, node1: OsmNode, node2: OsmNode, tag: String, diff: Double): MatchedWaypoint {
        val mw = MatchedWaypoint()
        mw.waypoint = OsmNode().also { it.ilon = mwp.waypoint!!.ilon; it.ilat = mwp.waypoint!!.ilat }
        mw.crosspoint = OsmNode().also { it.ilon = mwp.crosspoint!!.ilon; it.ilat = mwp.crosspoint!!.ilat }
        mw.node1 = node1
        mw.node2 = node2
        mw.name = mwp.name + tag + mwp.crosspoint!!.hashCode()
        mw.radius = mwp.radius
        mw.directionDiff = diff
        mw.directionToNext = mwp.directionToNext
        return mw
    }

    override fun hasMatch(lon: Int, lat: Int): Boolean {
        for (mwp in waypoints) {
            if (mwp.waypoint!!.ilon == lon && mwp.waypoint!!.ilat == lat &&
                (mwp.radius < this.maxDistance || mwp.crosspoint != null)
            ) {
                return true
            }
        }
        return false
    }

    // check limit of list size (avoid long runs)
    fun updateWayList(ways: MutableList<MatchedWaypoint>, mw: MatchedWaypoint) {
        ways.add(mw)
        // use only shortest distances by smallest direction difference. Java's Collections.sort is stable; so is sortWith.
        ways.sortWith(comparator)
        if (ways.size > MAX_POINTS) ways.removeAt(MAX_POINTS)
    }

    private companion object {
        const val MAX_POINTS = 5

        /** Java's `Double.compare`: numeric order, then -0.0 below 0.0 and NaN above everything, by the raw bits. */
        fun javaDoubleCompare(d1: Double, d2: Double): Int {
            if (d1 < d2) return -1
            if (d1 > d2) return 1
            val b1 = d1.toBits() // Java's doubleToLongBits: every NaN collapses to one pattern
            val b2 = d2.toBits()
            return if (b1 == b2) 0 else if (b1 < b2) -1 else 1
        }
    }
}
