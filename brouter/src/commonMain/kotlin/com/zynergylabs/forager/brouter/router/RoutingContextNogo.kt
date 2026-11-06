package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.mapaccess.MatchedWaypoint
import com.zynergylabs.forager.brouter.mapaccess.OsmNode
import com.zynergylabs.forager.brouter.util.CheapRuler
import com.zynergylabs.forager.brouter.util.javaRound
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

/*
 * Port of BRouter 1.7.10's `RoutingContext.java:285-554`, the nogo and distance half, as extensions on
 * [RoutingContext] (whose Java-private lists are `internal` for them). T2 keeps nine of its eleven methods; the two it
 * drops, `prepareNogoPoints` and `allInOneNogo`, are not ported. Nogo polygons and weighted nogo circles reach methods
 * named "unsupported" (see [OsmNogoPolygon] and [OsmNodeNamed]).
 */

/** Restore the full nogolist previously saved by [cleanNogoList]. */
internal fun RoutingContext.restoreNogoList() {
    nogopoints = nogopoints_all
}

private fun RoutingContext.isInside(wp: OsmNode, nogo: OsmNodeNamed): Boolean =
    wp.calcDistance(libm, nogo) < nogo.radius &&
        (
            nogo !is OsmNogoPolygon ||
                if (nogo.isClosed) nogo.isWithin(wp.ilon.toLong(), wp.ilat.toLong()) else nogo.isOnPolyline(wp.ilon.toLong(), wp.ilat.toLong())
            )

/** Clean the nogolist by removing nogos with waypoints within; the full list is kept for [restoreNogoList]. */
internal fun RoutingContext.cleanNogoList(waypoints: List<OsmNode>) {
    nogopoints_all = nogopoints
    val all = nogopoints ?: return
    val nogos = ArrayList<OsmNodeNamed>()
    for (nogo in all) {
        var goodGuy = true
        for (wp in waypoints) {
            if (isInside(wp, nogo)) goodGuy = false
        }
        if (goodGuy) nogos.add(nogo)
    }
    nogopoints = if (nogos.isEmpty()) null else nogos
}

internal fun RoutingContext.checkMatchedWaypointAgainstNogos(matchedWaypoints: MutableList<MatchedWaypoint>) {
    val all = nogopoints ?: return
    val theSize = matchedWaypoints.size
    if (theSize < 2) return
    var removed = 0
    val newMatchedWaypoints = ArrayList<MatchedWaypoint>()
    var prevMwp: MatchedWaypoint? = null
    var prevMwpIsInside = false
    for (i in 0 until theSize) {
        val mwp = matchedWaypoints[i]
        var isInsideNogo = false
        val wp = mwp.crosspoint!!
        for (nogo in all) {
            if (nogo.nogoWeight.isNaN() && isInside(wp, nogo)) {
                isInsideNogo = true
                break
            }
        }
        if (isInsideNogo) {
            var useAnyway = false
            if (prevMwp == null) {
                useAnyway = true
            } else if (mwp.wpttype == MatchedWaypoint.WAYPOINT_TYPE_DIRECT) {
                useAnyway = true
            } else if (prevMwp.wpttype == MatchedWaypoint.WAYPOINT_TYPE_DIRECT) {
                useAnyway = true
            } else if (prevMwpIsInside) {
                useAnyway = true
            } else if (i == theSize - 1) {
                throw IllegalArgumentException("last wpt in restricted area ")
            }
            if (useAnyway) {
                prevMwpIsInside = true
                newMatchedWaypoints.add(mwp)
            } else {
                removed++
                prevMwpIsInside = false
            }
        } else {
            prevMwpIsInside = false
            newMatchedWaypoints.add(mwp)
        }
        prevMwp = mwp
    }
    if (newMatchedWaypoints.size < 2) {
        throw IllegalArgumentException("a wpt in restricted area ")
    }
    if (removed > 0) {
        matchedWaypoints.clear()
        matchedWaypoints.addAll(newMatchedWaypoints)
    }
}

internal fun RoutingContext.getNogoChecksums(): LongArray {
    val cs = LongArray(3)
    val list = nogopoints
    val n = list?.size ?: 0
    for (i in 0 until n) {
        val nogo = list!![i]
        cs[0] += nogo.ilon.toLong()
        cs[1] += nogo.ilat.toLong()
        // 10 is an arbitrary constant to get sub-integer precision in the checksum
        cs[2] += (nogo.radius * 10.0).toLong()
    }
    return cs
}

internal fun RoutingContext.setWaypoint(wp: OsmNodeNamed, endpoint: Boolean) {
    setWaypoint(wp, null, endpoint)
}

internal fun RoutingContext.setWaypoint(wp: OsmNodeNamed, pendingEndpoint: OsmNodeNamed?, endpoint: Boolean) {
    keepnogopoints = nogopoints
    val list = ArrayList<OsmNodeNamed>()
    list.add(wp)
    keepnogopoints?.let { list.addAll(it) }
    nogopoints = list
    isEndpoint = endpoint
    this.pendingEndpoint = pendingEndpoint
}

internal fun RoutingContext.checkPendingEndpoint(): Boolean {
    val pending = pendingEndpoint ?: return false
    isEndpoint = true
    nogopoints!![0] = pending
    pendingEndpoint = null
    return true
}

internal fun RoutingContext.unsetWaypoint() {
    nogopoints = keepnogopoints
    pendingEndpoint = null
    isEndpoint = false
}

internal fun RoutingContext.calcDistance(lon1In: Int, lat1In: Int, lon2In: Int, lat2In: Int): Int {
    var lon1 = lon1In
    var lat1 = lat1In
    var lon2 = lon2In
    var lat2 = lat2In
    val lonlat2m = CheapRuler.getLonLatToMeterScales(libm, (lat1 + lat2) shr 1)
    val dlon2m = lonlat2m[0]
    val dlat2m = lonlat2m[1]
    var dx = (lon2 - lon1) * dlon2m
    var dy = (lat2 - lat1) * dlat2m
    var d = sqrt(dy * dy + dx * dx)

    shortestmatch = false

    val list = nogopoints
    if (list != null && list.isNotEmpty() && d > 0.0) {
        for (ngidx in 0 until list.size) {
            val nogo = list[ngidx]
            val x1 = (lon1 - nogo.ilon) * dlon2m
            val y1 = (lat1 - nogo.ilat) * dlat2m
            val x2 = (lon2 - nogo.ilon) * dlon2m
            val y2 = (lat2 - nogo.ilat) * dlat2m
            val r12 = x1 * x1 + y1 * y1
            val r22 = x2 * x2 + y2 * y2
            var radius = abs(if (r12 < r22) y1 * dx - x1 * dy else y2 * dx - x2 * dy) / d

            if (radius < nogo.radius) { // 20m
                var s1 = x1 * dx + y1 * dy
                var s2 = x2 * dx + y2 * dy

                if (s1 < 0.0) {
                    s1 = -s1
                    s2 = -s2
                }
                if (s2 > 0.0) {
                    radius = sqrt(if (s1 < s2) r12 else r22)
                    if (radius > nogo.radius) continue
                }
                if (nogo.isNogo) {
                    if (nogo !is OsmNogoPolygon) { // nogo is a circle
                        if (nogo.nogoWeight.isNaN()) {
                            // default nogo behaviour (ignore completely)
                            nogoCost = -1.0
                        } else {
                            // nogo weight, compute distance within the circle
                            nogoCost = nogo.distanceWithinRadius(lon1, lat1, lon2, lat2, d) * nogo.nogoWeight
                        }
                    } else if (nogo.intersects(lon1, lat1, lon2, lat2)) {
                        // nogo is a polyline/polygon, we have to check there is indeed
                        // an intersection in this case (radius check is not enough).
                        if (nogo.nogoWeight.isNaN()) {
                            // default nogo behaviour (ignore completely)
                            nogoCost = -1.0
                        } else {
                            if (nogo.isClosed) {
                                // compute distance within the polygon
                                nogoCost = nogo.distanceWithinPolygon(lon1, lat1, lon2, lat2) * nogo.nogoWeight
                            } else {
                                // for a polyline, just add a constant penalty
                                nogoCost = nogo.nogoWeight
                            }
                        }
                    }
                } else {
                    shortestmatch = true
                    nogo.radius = radius // shortest distance to way
                    // calculate remaining distance
                    if (s2 < 0.0) {
                        wayfraction = -s2 / (d * d)
                        val xm = x2 - wayfraction * dx
                        val ym = y2 - wayfraction * dy
                        ilonshortest = (xm / dlon2m + nogo.ilon).toInt()
                        ilatshortest = (ym / dlat2m + nogo.ilat).toInt()
                    } else if (s1 > s2) {
                        wayfraction = 0.0
                        ilonshortest = lon2
                        ilatshortest = lat2
                    } else {
                        wayfraction = 1.0
                        ilonshortest = lon1
                        ilatshortest = lat1
                    }

                    // here it gets nasty: there can be nogo-points in the list
                    // *after* the shortest distance point. In case of a shortest-match
                    // we use the reduced way segment for nogo-matching, in order not
                    // to cut our escape-way if we placed a nogo just in front of where we are
                    if (isEndpoint) {
                        wayfraction = 1.0 - wayfraction
                        lon2 = ilonshortest
                        lat2 = ilatshortest
                    } else {
                        nogoCost = 0.0
                        lon1 = ilonshortest
                        lat1 = ilatshortest
                    }
                    dx = (lon2 - lon1) * dlon2m
                    dy = (lat2 - lat1) * dlat2m
                    d = sqrt(dy * dy + dx * dx)
                }
            }
        }
    }
    return max(1.0, javaRound(d).toDouble()).toInt()
}
