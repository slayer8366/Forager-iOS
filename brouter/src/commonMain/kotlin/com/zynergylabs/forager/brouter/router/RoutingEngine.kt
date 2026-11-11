package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.mapaccess.MatchedWaypoint
import com.zynergylabs.forager.brouter.mapaccess.NodesCache
import com.zynergylabs.forager.brouter.mapaccess.OsmLink
import com.zynergylabs.forager.brouter.mapaccess.OsmNode
import com.zynergylabs.forager.brouter.mapaccess.OsmNodePairSet
import com.zynergylabs.forager.brouter.mapaccess.OsmPos
import com.zynergylabs.forager.brouter.util.SortedHeap
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

/**
 * BRouter 1.7.10's `btools.router.RoutingEngine`, step 8 (RECORD -90; the step 8 report's §1 places every method).
 *
 * Only what Forager's call reaches is ported (`BRouterTrailRouter.kt:143-209` at Forager `947f1be2`): the routing mode,
 * two waypoints, no output or log file, `quite`. What §1 leaves out throws a named "unsupported"; via points (-24) are one.
 * Java's `Thread` base is not kept: Forager calls `doRun` on a thread of its own. `synchronized (openSet)` is not kept:
 * its only other reader, `getOpenSet`, is left out (§1).
 *
 * 8a ports matching and the search without memory pressure. Still to come, each a `TODO` naming its sub-step: the memory
 * path (8b); terminate, the time limit, the progress count and `catch (Error)` (8c).
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
    private var finished = false
    private var matchedWaypoints: MutableList<MatchedWaypoint>? = null
    private var linksProcessed = 0
    private var nodeLimit = 0 // used for target island search
    private val islandNodePairs = OsmNodePairSet(MAXNODES_ISLAND_CHECK)
    private var foundTrack: OsmTrack = OsmTrack()
    private var foundRawTrack: OsmTrack? = null
    private var alternativeIndex = 0
    private var errorMessage: String? = null
    private var airDistanceCostFactor = 0.0
    private var lastAirDistanceCostFactor = 0.0
    private var guideTrack: OsmTrack? = null
    private var matchPath: OsmPathElement? = null
    private val libm get() = routingContext.libm

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

    // RoutingEngine.java:170-197, the routing mode only (§1).
    fun doRun(maxRunningTime: Long) {
        if (engineMode != BROUTER_ENGINEMODE_ROUTING) unsupported("engine mode $engineMode")
        require(waypoints.size >= 2) { "we need two lat/lon points at least!" }
        doRouting(maxRunningTime)
    }

    // RoutingEngine.java:199-361. No output or log file, `quite` (§1): the found track is the alternative asked for.
    private fun doRouting(maxRunningTime: Long) {
        try {
            if (maxRunningTime != 0L) TODO("RoutingEngine: the time limit, step 8c, RECORD -90")
            if (routingContext.allowSamewayback) unsupported("allowSamewayback")
            val nsections = waypoints.size - 1
            val refTracks = arrayOfNulls<OsmTrack>(nsections) // used ways for alternatives
            val lastTracks = arrayOfNulls<OsmTrack>(nsections)
            val messageList = ArrayList<String>()
            var i = 0
            while (true) {
                val track = findTrack(refTracks, lastTracks)
                if (routingContext.ai != null) unsupported("area info (routingContext.ai)")
                track.message = "track-length = " + track.distance + " filtered ascend = " + track.ascend +
                    " plain-ascend = " + track.plainAscend + " cost=" + track.cost
                if (track.energy != 0) {
                    track.message += " energy=" + Formatter.getFormattedEnergy(track.energy) + " time=" +
                        Formatter.getFormattedTime2(track.getTotalSeconds())
                }
                track.name = "brouter_" + routingContext.getProfileName() + "_" + i
                messageList.add(track.message!!)
                track.messageList = messageList
                if (i == routingContext.getAlternativeIdx(0, 3)) {
                    foundTrack = track
                    alternativeIndex = i
                } else {
                    i++
                    continue
                }
                break
            }
        } catch (e: IllegalArgumentException) {
            logException(e)
        } catch (e: Exception) {
            logException(e)
        } finally {
            ProfileCache.releaseProfile(routingContext)
            nodesCache?.close()
            nodesCache = null
            openSet.clear()
            finished = true // this signals termination to outside
        }
    }

    // RoutingEngine.java:696-809.
    private fun postElevationCheck(track: OsmTrack) {
        var lastPt: OsmPathElement? = null
        var startPt: OsmPathElement? = null
        var lastElev = Short.MIN_VALUE
        var startElev = Short.MIN_VALUE
        var endElev = Short.MIN_VALUE
        var startIdx = 0
        var endIdx = -1
        var dist = 0
        val ourSize = track.nodes.size
        for (idx in 0 until ourSize) {
            val n = track.nodes[idx]
            if (n.getSElev() == Short.MIN_VALUE && lastElev != Short.MIN_VALUE && idx < ourSize - 1) {
                // start one point before entry point to get better elevation results
                if (idx > 1) startElev = track.nodes[idx - 2].getSElev()
                if (startElev == Short.MIN_VALUE) startElev = lastElev
                startIdx = idx
                startPt = lastPt
                dist = 0
                if (lastPt != null) dist += n.calcDistance(libm, lastPt)
            } else if (n.getSElev() != Short.MIN_VALUE && lastElev == Short.MIN_VALUE && startElev != Short.MIN_VALUE) {
                // end one point behind exit point to get better elevation results
                if (idx + 1 < track.nodes.size) endElev = track.nodes[idx + 1].getSElev()
                if (endElev == Short.MIN_VALUE) endElev = n.getSElev()
                endIdx = idx
                var tmpPt = track.nodes[if (startIdx > 1) startIdx - 2 else startIdx - 1]
                val diffElev = endElev - startElev
                dist += tmpPt.calcDistance(libm, startPt!!)
                dist += n.calcDistance(libm, lastPt!!)
                var distRest = dist
                var incline = diffElev / (dist / 100.0)
                var lastMsg = ""
                var tmpincline = 0.0
                var startincline = 0.0
                var selev = track.nodes[if (startIdx > 1) startIdx - 2 else startIdx - 1].getSElev().toDouble()
                var hasInclineTags = false
                for (i in startIdx - 1 until endIdx + 1) {
                    val tmp = track.nodes[i]
                    if (tmp.message != null) {
                        val md = tmp.message!!.copy()
                        val msg = md.wayKeyValues!!
                        if (msg != lastMsg) {
                            val revers = msg.contains("reversedirection=yes")
                            var pos = msg.indexOf("incline=")
                            if (pos != -1) {
                                hasInclineTags = true
                                var s = msg.substring(pos + 8)
                                pos = s.indexOf(" ")
                                if (pos != -1) s = s.substring(0, pos)
                                if (s.isNotEmpty()) {
                                    try {
                                        var ind = s.indexOf("%")
                                        if (ind != -1) s = s.substring(0, ind)
                                        ind = s.indexOf("°")
                                        if (ind != -1) s = s.substring(0, ind)
                                        // Double.parseDouble on the JVM; Native's String.toDouble is step 10's [?].
                                        tmpincline = s.trim().toDouble()
                                        if (revers) tmpincline *= -1
                                    } catch (e: NumberFormatException) {
                                        tmpincline = 0.0
                                    }
                                }
                            } else {
                                tmpincline = 0.0
                            }
                            if (startincline == 0.0) {
                                startincline = tmpincline
                            } else if (startincline < 0 && tmpincline > 0) {
                                // for the way up find the exit point
                                val diff = endElev - selev
                                tmpincline = diff / (distRest / 100.0)
                            }
                        }
                        lastMsg = msg
                    }
                    val tmpdist = tmp.calcDistance(libm, tmpPt)
                    distRest -= tmpdist
                    if (hasInclineTags) incline = tmpincline
                    selev = selev + (tmpdist / 100.0 * incline)
                    tmp.setSElev(selev.toInt().toShort()) // Java's (short) on a double: to int, then the low 16 bits
                    // Java dereferences tmp.message here without a check; every node of the re-tracked track has one
                    // (compileTrack, :2288-2290) [I].
                    tmp.message!!.ele = selev.toInt().toShort()
                    tmpPt = tmp
                }
                dist = 0
            } else if (n.getSElev() != Short.MIN_VALUE && lastElev == Short.MIN_VALUE && startIdx == 0) {
                // fill at start
                for (i in 0 until idx) track.nodes[i].setSElev(n.getSElev())
            } else if (n.getSElev() == Short.MIN_VALUE && idx == track.nodes.size - 1) {
                // fill at end
                startIdx = idx
                for (i in startIdx until track.nodes.size) track.nodes[i].setSElev(lastElev)
            } else if (n.getSElev() == Short.MIN_VALUE) {
                if (lastPt != null) dist += n.calcDistance(libm, lastPt)
            }
            lastElev = n.getSElev()
            lastPt = n
        }
    }

    // RoutingEngine.java:811-814. Kotlin/JVM's toString is Java's; Native's is step 10's [?].
    private fun logException(t: Throwable) {
        errorMessage = if (t is RuntimeException) t.message else t.toString()
    }

    // RoutingEngine.java:858-877; the dynamic-range branch is left out (use_dynamic_range=0, §1).
    private fun findTrack(refTracks: Array<OsmTrack?>, lastTracks: Array<OsmTrack?>): OsmTrack {
        while (true) {
            try {
                return tryFindTrack(refTracks, lastTracks)
            } catch (rie: RoutingIslandException) {
                if (routingContext.useDynamicDistance) unsupported("use_dynamic_range")
                islandNodePairs.freezeTempPairs()
                nodesCache!!.clean(true)
                matchedWaypoints = null
            }
        }
    }

    // RoutingEngine.java:879-1052, less what §1 leaves out. Java reassigns its refTracks and lastTracks only when there are
    // more waypoints than sections, which two unmatched waypoints and no extra ones never give [I]; that is checked.
    // Java returns null when a segment is null (:1018-1019); searchRoutedTrack never returns null, so neither does this.
    private fun tryFindTrack(refTracks: Array<OsmTrack?>, lastTracks: Array<OsmTrack?>): OsmTrack {
        val totaltrack = OsmTrack()
        val nUnmatched = waypoints.size
        var hasDirectRouting = false
        check(lastTracks.size >= waypoints.size - 1) { "RoutingEngine: more sections than tracks (:900-904) is not reached" }
        for (wp in waypoints) {
            if (wp.wpttype == MatchedWaypoint.WAYPOINT_TYPE_DIRECT) hasDirectRouting = true
        }
        // check for a track for that target: the nearby track is left out (§1)
        if (!hasDirectRouting && lastTracks[waypoints.size - 2] == null && routingContext.rawTrackPath != null) {
            unsupported("rawTrackPath (the nearby track)")
        }
        var mwps = matchedWaypoints
        if (mwps == null) { // could exist from the previous alternative level
            mwps = ArrayList()
            matchedWaypoints = mwps
            for (i in 0 until nUnmatched) {
                val mwp = MatchedWaypoint()
                mwp.waypoint = waypoints[i]
                mwp.name = waypoints[i].name
                mwp.wpttype = waypoints[i].wpttype
                mwps.add(mwp)
            }
            val startSize = mwps.size
            matchWaypointsToNodes(mwps)
            check(startSize >= mwps.size) { "RoutingEngine: waypoints added by matching (:938-942) is not reached" }

            routingContext.checkMatchedWaypointAgainstNogos(mwps)

            // detect target islands: restricted search in inverse direction
            if (routingContext.inverseRouting) unsupported("inverseRouting")
            routingContext.inverseDirection = !routingContext.inverseRouting
            airDistanceCostFactor = 0.0
            for (i in 0 until mwps.size - 1) {
                nodeLimit = MAXNODES_ISLAND_CHECK
                if (mwps[i].wpttype == MatchedWaypoint.WAYPOINT_TYPE_DIRECT) continue
                val seg = findTrack("target-island-check", mwps[i + 1], mwps[i], null, null, false)
                if (seg == null && nodeLimit > 0) {
                    throw IllegalArgumentException("target island detected for section $i")
                }
            }
            routingContext.inverseDirection = false
            nodeLimit = 0
        } else {
            check(lastTracks.size >= mwps.size - 1) { "RoutingEngine: more sections than tracks (:965-969) is not reached" }
        }

        routingContext.hasDirectRouting = hasDirectRouting

        OsmPath.seg = 1 // set segment counter
        for (i in 0 until mwps.size - 1) {
            if (lastTracks[i] != null) {
                if (refTracks[i] == null) refTracks[i] = OsmTrack()
                refTracks[i]!!.addNodes(lastTracks[i]!!)
            }
            val wptIndex = i
            val seg = searchTrack(mwps[i], mwps[i + 1], refTracks[i])
            // continueStraight acts only before a middle waypoint (:1003-1004), which two waypoints never have.
            if (routingContext.continueStraight && i < mwps.size - 2) unsupported("continueStraight")

            if (routingContext.ai != null) unsupported("area info (routingContext.ai)")

            if (routingContext.correctMisplacedViaPoints &&
                mwps[i].wpttype != MatchedWaypoint.WAYPOINT_TYPE_DIRECT &&
                mwps[i].wpttype != MatchedWaypoint.WAYPOINT_TYPE_MEETING &&
                !routingContext.allowSamewayback
            ) {
                snapPathConnection(mwps[i])
            }
            if (wptIndex > 0) mwps[wptIndex].indexInTrack = totaltrack.nodes.size - 1

            totaltrack.appendTrack(seg)
            lastTracks[i] = seg
        }

        postElevationCheck(totaltrack)

        recalcTrack(totaltrack)

        mwps[mwps.size - 1].indexInTrack = totaltrack.nodes.size - 1
        totaltrack.matchedWaypoints = mwps
        totaltrack.processVoiceHints(routingContext)
        totaltrack.prepareSpeedProfile(routingContext)

        totaltrack.showTime = routingContext.showTime
        totaltrack.params = routingContext.keyValues

        if (routingContext.poipoints != null) unsupported("pois")

        return totaltrack
    }

    /**
     * RoutingEngine.java:1265-1267, the guard only: via points are left out (RECORD -24). Java returns false at once for a
     * waypoint whose name starts with neither `via` nor `rt`; past that it would move a misplaced via point.
     */
    private fun snapPathConnection(startWp: MatchedWaypoint): Boolean {
        val name = startWp.name!!
        if (!name.startsWith("via") && !name.startsWith("rt")) return false
        unsupported("via points (correctMisplacedViaPoints on waypoint '$name'; RECORD -24)")
    }

    // RoutingEngine.java:1482-1591.
    private fun recalcTrack(t: OsmTrack) {
        var totaldist = 0
        var totaltime = 0
        var lasttime = 0f
        var speedMin = 9999f
        val directMap = HashMap<Int, Int>()
        var tmptime: Float
        var speed: Float
        var dist: Int
        var angle: Double

        var ascend = 0.0
        var ehb = 0.0
        val ourSize = t.nodes.size

        var eleStart = Short.MIN_VALUE
        var eleEnd = Short.MIN_VALUE
        val eleFactor = if (routingContext.inverseRouting) 0.25 else -0.25

        for (i in 0 until ourSize) {
            val n = t.nodes[i]
            if (n.message == null) n.message = MessageData()
            var nLast: OsmPathElement? = null
            if (i == 0) {
                angle = 0.0
                dist = 0
            } else if (i == 1) {
                angle = 0.0
                nLast = t.nodes[0]
                dist = nLast.calcDistance(libm, n)
            } else {
                val lon0 = t.nodes[i - 2].getILon()
                val lat0 = t.nodes[i - 2].getILat()
                val lon1 = t.nodes[i - 1].getILon()
                val lat1 = t.nodes[i - 1].getILat()
                val lon2 = t.nodes[i].getILon()
                val lat2 = t.nodes[i].getILat()
                angle = routingContext.anglemeter.calcAngle(lon0, lat0, lon1, lat1, lon2, lat2)
                nLast = t.nodes[i - 1]
                dist = nLast.calcDistance(libm, n)
            }
            n.message!!.linkdist = dist
            n.message!!.turnangle = angle.toFloat()
            totaldist += dist
            totaltime = (totaltime + n.getTime()).toInt() // Java's int += float
            tmptime = n.getTime() - lasttime
            if (dist > 0) {
                speed = dist / tmptime * 3.6f
                speedMin = minOf(speedMin, speed) // Math.min(float, float) on the JVM
            }
            if (tmptime == 1f) { // no time used here
                directMap[i] = dist
            }

            lasttime = n.getTime()

            val ele = n.getSElev()
            if (ele != Short.MIN_VALUE) eleEnd = ele
            if (eleStart == Short.MIN_VALUE) eleStart = ele

            if (nLast != null) {
                val eleLast = nLast.getSElev()
                if (eleLast != Short.MIN_VALUE) {
                    ehb = ehb + (eleLast - ele) * eleFactor
                }
                val filter = elevationFilter(n)
                if (ehb > 0) {
                    ascend += ehb
                    ehb = 0.0
                } else if (ehb < filter) {
                    ehb = filter
                }
            }
        }

        t.ascend = ascend.toInt()
        t.plainAscend = ((eleStart - eleEnd) * eleFactor + 0.5).toInt()

        t.distance = totaldist

        for (key in directMap.keys.sorted()) {
            val value = directMap.getValue(key)
            val addTime = value / (speedMin / 3.6f)

            var addEnergy = 0.0
            if (key > 0) {
                val gravity = 9.81 // in meters per second^(-2)
                val incline = if (t.nodes[key - 1].getSElev() == Short.MIN_VALUE || t.nodes[key].getSElev() == Short.MIN_VALUE) {
                    0.0
                } else {
                    (t.nodes[key - 1].getElev() - t.nodes[key].getElev()) / value
                }
                val fRoll = routingContext.totalMass * gravity * (routingContext.defaultC_r + incline)
                val spd = speedMin / 3.6
                addEnergy = value * (routingContext.S_C_x * spd * spd + fRoll)
            }
            for (j in key until ourSize) {
                val n = t.nodes[j]
                n.setTime(n.getTime() + addTime)
                n.setEnergy(n.getEnergy() + addEnergy.toFloat())
            }
        }
        t.energy = t.nodes[t.nodes.size - 1].getEnergy().toInt()
    }

    // RoutingEngine.java:1600-1606.
    private fun elevationFilter(n: OsmPos): Double {
        val nc = nodesCache
        if (nc != null) {
            val r = nc.getElevationType(n.getILon(), n.getILat())
            if (r == 1) return -5.0
        }
        return -10.0
    }

    // RoutingEngine.java:1609-1689; the dynamic-range retry and the beeline points are left out (§1).
    private fun matchWaypointsToNodes(unmatchedWaypoints: MutableList<MatchedWaypoint>) {
        resetCache(false)
        val range = routingContext.waypointCatchingRange
        val ok = nodesCache!!.matchWaypointsToNodes(libm, unmatchedWaypoints, range, islandNodePairs)
        if (!ok && routingContext.useDynamicDistance) unsupported("use_dynamic_range")
        if (!ok) {
            for (mwp in unmatchedWaypoints) {
                if (mwp.crosspoint == null) throw IllegalArgumentException(mwp.name + "-position not mapped in existing datafile")
            }
        }
        if (routingContext.useDynamicDistance && routingContext.buildBeelineOnRange) unsupported("add_beeline")
    }

    // RoutingEngine.java:1691-1706; the beeline segment is left out (§1). The nearby track is always null here.
    private fun searchTrack(startWp: MatchedWaypoint, endWp: MatchedWaypoint, refTrack: OsmTrack?): OsmTrack {
        try {
            if (startWp.wpttype == MatchedWaypoint.WAYPOINT_TYPE_DIRECT) unsupported("a beeline segment (waypoint type direct)")
            return searchRoutedTrack(startWp, endWp, refTrack)
        } finally {
            routingContext.restoreNogoList()
        }
    }

    // RoutingEngine.java:1708-1805, with no nearby track (§1). The timeout's dirty track (:1749-1758) is 8c's.
    private fun searchRoutedTrack(startWp: MatchedWaypoint, endWp: MatchedWaypoint, refTrack: OsmTrack?): OsmTrack {
        var track: OsmTrack? = null
        val airDistanceCostFactors = doubleArrayOf(routingContext.pass1coefficient, routingContext.pass2coefficient)

        for (cfi in airDistanceCostFactors.indices) {
            if (cfi > 0) lastAirDistanceCostFactor = airDistanceCostFactors[cfi - 1]
            airDistanceCostFactor = airDistanceCostFactors[cfi]

            if (airDistanceCostFactor < 0.0) continue

            var t = findTrack(if (cfi == 0) "pass0" else "pass1", startWp, endWp, track, refTrack, false)
            if (routingContext.ai != null) unsupported("area info (routingContext.ai)")

            if (t == null && track != null && matchPath != null) {
                // ups, didn't find it, use a merge
                t = mergeTrack(matchPath!!, track)
            }
            if (t != null) {
                track = t
            } else {
                throw IllegalArgumentException("no track found at pass=$cfi")
            }
        }
        if (track == null) throw IllegalArgumentException("no track found")

        if (refTrack == null) { // with no nearby track, wasClean and isDirty are both false
            track.endPoint = endWp
            track.nogoChecksums = routingContext.getNogoChecksums()
            track.profileTimestamp = routingContext.profileTimestamp
            track.isDirty = false
            foundRawTrack = track
        }

        // final run for verbose log info and detail nodes
        airDistanceCostFactor = 0.0
        lastAirDistanceCostFactor = 0.0
        guideTrack = track
        try {
            return findTrack("re-tracking", startWp, endWp, null, refTrack, false)
                ?: throw IllegalArgumentException("error re-tracking track")
        } finally {
            guideTrack = null
        }
    }

    // RoutingEngine.java:1808-1816.
    private fun resetCache(detailed: Boolean) {
        val maxmem = routingContext.memoryclass * 1024L * 1024L // in MB
        nodesCache = NodesCache(
            fileSystem, segmentDir, routingContext.expctxWay, routingContext.forceSecondaryData, maxmem, nodesCache, detailed,
        )
        islandNodePairs.clearTempPairs()
    }

    // RoutingEngine.java:1818-1829.
    private fun getStartPath(n1: OsmNode, n2: OsmNode, mwp: MatchedWaypoint, endPos: OsmNodeNamed?, sameSegmentSearch: Boolean): OsmPath? {
        if (endPos != null) endPos.radius = 1.5
        val p = getStartPath(n1, n2, OsmNodeNamed(mwp.crosspoint!!), endPos, sameSegmentSearch)

        // special case: start+end on same segment
        if (p != null && p.cost >= 0 && sameSegmentSearch && endPos != null && endPos.radius < 1.5) {
            p.treedepth = 0 // hack: mark for the final-check
        }
        return p
    }

    // RoutingEngine.java:1832-1870.
    private fun getStartPath(n1: OsmNode, n2: OsmNode, wp: OsmNodeNamed, endPos: OsmNodeNamed?, sameSegmentSearch: Boolean): OsmPath? {
        try {
            routingContext.setWaypoint(wp, if (sameSegmentSearch) endPos else null, false)
            var bestPath: OsmPath? = null
            var bestLink: OsmLink? = null
            val startLink = OsmLink(null, n1)
            val startPath = routingContext.createPath(startLink)
            startLink.addLinkHolder(startPath, null)
            var minradius = 1e10
            var link = n1.firstlink
            while (link != null) {
                val nextNode = link.getTarget(n1)!!
                // border node, dead end, itself, or not just that link
                if (!nextNode.isHollow() && nextNode.firstlink != null && nextNode !== n1 && nextNode === n2) {
                    wp.radius = 1.5
                    val testPath = routingContext.createPath(startPath, link, null, guideTrack != null)
                    testPath.airdistance = if (endPos == null) 0 else nextNode.calcDistance(libm, endPos)
                    if (wp.radius < minradius) {
                        bestPath = testPath
                        minradius = wp.radius
                        bestLink = link
                    }
                }
                link = link.getNext(n1) // after the body, as Java's for-update
            }
            if (bestLink != null) bestLink.addLinkHolder(bestPath!!, n1)
            if (bestPath != null) bestPath.treedepth = 1
            return bestPath
        } finally {
            routingContext.unsetWaypoint()
        }
    }

    // RoutingEngine.java:1872-1887.
    private fun findTrack(
        operationName: String,
        startWp: MatchedWaypoint,
        endWp: MatchedWaypoint?,
        costCuttingTrack: OsmTrack?,
        refTrack: OsmTrack?,
        fastPartialRecalc: Boolean,
    ): OsmTrack? {
        try {
            val wpts2 = ArrayList<OsmNode>()
            wpts2.add(startWp.waypoint!!)
            if (endWp != null) wpts2.add(endWp.waypoint!!)
            routingContext.cleanNogoList(wpts2)

            val detailed = guideTrack != null
            resetCache(detailed)
            nodesCache!!.nodesMap.cleanupMode = if (detailed) 0 else (if (routingContext.considerTurnRestrictions) 2 else 1)
            return findTrackInner(operationName, startWp, endWp, costCuttingTrack, refTrack, fastPartialRecalc)
        } finally {
            routingContext.restoreNogoList()
            nodesCache!!.clean(false) // clean only non-virgin caches
        }
    }

    // RoutingEngine.java:1890-2261 (`_findTrack`). The memory path is 8b's; terminate and the time limit are 8c's; the
    // early exit needs fastPartialRecalc, which only the nearby track sets (§1).
    private fun findTrackInner(
        operationName: String,
        startWp: MatchedWaypoint,
        endWp: MatchedWaypoint?,
        costCuttingTrack: OsmTrack?,
        refTrack: OsmTrack?,
        fastPartialRecalc: Boolean,
    ): OsmTrack? {
        check(!fastPartialRecalc) { "RoutingEngine: fastPartialRecalc is set only by the nearby track, left out (§1)" }
        val nc = nodesCache!!
        val guide = guideTrack
        val verbose = guide != null

        var maxTotalCost = if (guide != null) guide.cost + 5000 else 1000000000
        var firstMatchCost = 1000000000

        matchPath = null
        var nodesVisited = 0

        val startNodeId1 = startWp.node1!!.getIdFromPos()
        val startNodeId2 = startWp.node2!!.getIdFromPos()
        val endNodeId1 = if (endWp == null) -1L else endWp.node1!!.getIdFromPos()
        val endNodeId2 = if (endWp == null) -1L else endWp.node2!!.getIdFromPos()
        var endPos: OsmNodeNamed? = null

        var sameSegmentSearch = false
        val start1 = nc.getGraphNode(startWp.node1!!)
        val start2 = nc.getGraphNode(startWp.node2!!)
        if (endWp != null) {
            val end1 = nc.getGraphNode(endWp.node1!!)
            val end2 = nc.getGraphNode(endWp.node2!!)
            nc.nodesMap.endNode1 = end1
            nc.nodesMap.endNode2 = end2
            endPos = OsmNodeNamed(endWp.crosspoint!!)
            sameSegmentSearch = (start1 === end1 && start2 === end2) || (start1 === end2 && start2 === end1)
        }
        if (!nc.obtainNonHollowNode(start1)) return null
        nc.expandHollowLinkTargets(start1)
        if (!nc.obtainNonHollowNode(start2)) return null
        nc.expandHollowLinkTargets(start2)

        routingContext.startDirectionValid = routingContext.forceUseStartDirection || fastPartialRecalc
        routingContext.startDirectionValid = routingContext.startDirectionValid and
            (routingContext.startDirection != null && !routingContext.inverseDirection)

        val startPath1 = getStartPath(start1, start2, startWp, endPos, sameSegmentSearch)
        val startPath2 = getStartPath(start2, start1, startWp, endPos, sameSegmentSearch)

        // check for an INITIAL match with the cost-cutting-track
        if (costCuttingTrack != null) {
            val pe1 = costCuttingTrack.getLink(startNodeId1, startNodeId2)
            if (pe1 != null) {
                var c = startPath1!!.cost - pe1.cost
                if (c < 0) c = 0
                if (c < firstMatchCost) firstMatchCost = c
            }
            val pe2 = costCuttingTrack.getLink(startNodeId2, startNodeId1)
            if (pe2 != null) {
                var c = startPath2!!.cost - pe2.cost
                if (c < 0) c = 0
                if (c < firstMatchCost) firstMatchCost = c
            }
        }

        if (startPath1 == null) return null
        if (startPath2 == null) return null

        openSet.clear()
        addToOpenset(startPath1)
        addToOpenset(startPath2)
        val openBorderList = ArrayList<OsmPath>(4096)

        while (true) {
            val path = openSet.popLowestKeyValue()
            if (path == null) {
                if (openBorderList.isEmpty()) break
                TODO("RoutingEngine: the open border list (memory panic mode), step 8b, RECORD -90")
            }

            if (path.airdistance == -1) continue

            if (DIRECT_WEAVING && nc.hasHollowLinkTargets(path.getTargetNode()!!)) {
                if (!nc.nodesMap.isInMemoryBounds(openSet.getSize(), false)) {
                    TODO("RoutingEngine: the memory path (collectOutreachers, panic mode), step 8b, RECORD -90")
                }
            }

            if (nodeLimit > 0) { // check node-limit for target island search
                if (--nodeLimit == 0) return null
            }

            nodesVisited++
            linksProcessed++

            val currentLink = path.getLink()!!
            val sourceNode = path.getSourceNode()!!
            val currentNode = path.getTargetNode()!!

            if (currentLink.isLinkUnused()) continue

            val currentNodeId = currentNode.getIdFromPos()
            val sourceNodeId = sourceNode.getIdFromPos()

            if (!path.didEnterDestinationArea()) {
                islandNodePairs.addTempPair(sourceNodeId, currentNodeId)
            }

            if (path.treedepth != 1) {
                if (path.treedepth == 0) { // hack: sameSegment Paths marked treedepth=0 to pass above check
                    path.treedepth = 1
                }

                if ((sourceNodeId == endNodeId1 && currentNodeId == endNodeId2) ||
                    (sourceNodeId == endNodeId2 && currentNodeId == endNodeId1)
                ) {
                    // track found, compile
                    val t = compileTrack(path, verbose)
                    t.showspeed = routingContext.showspeed
                    t.showSpeedProfile = routingContext.showSpeedProfile
                    return t
                }

                // check for a match with the cost-cutting-track
                if (costCuttingTrack != null) {
                    val pe = costCuttingTrack.getLink(sourceNodeId, currentNodeId)
                    if (pe != null) {
                        // remember first match cost for fast termination of partial recalcs
                        var parentcost = path.originElement?.cost ?: 0

                        // hitting start-element of costCuttingTrack?
                        val c = path.cost - parentcost - pe.cost
                        if (c > 0) parentcost += c

                        if (parentcost < firstMatchCost) firstMatchCost = parentcost

                        val costEstimate = path.cost + path.elevationCorrection() + (costCuttingTrack.cost - pe.cost)
                        if (costEstimate <= maxTotalCost) {
                            matchPath = OsmPathElement.create(path)
                        }
                        if (costEstimate < maxTotalCost) {
                            maxTotalCost = costEstimate
                        }
                    }
                }
            }

            val firstLinkHolder = currentLink.getFirstLinkHolder(sourceNode)
            var lh = firstLinkHolder
            while (lh != null) {
                (lh as OsmPath).airdistance = -1 // invalidate the entry in the open set
                lh = lh.getNextForLink()
            }

            if (path.treedepth > 1) {
                val isBidir = currentLink.isBidirectional()
                sourceNode.unlinkLink(currentLink)

                // if the counterlink is alive and does not yet have a path, remove it
                if (isBidir && currentLink.getFirstLinkHolder(currentNode) == null && !routingContext.considerTurnRestrictions) {
                    currentNode.unlinkLink(currentLink)
                }
            }

            // recheck cutoff before doing expensive stuff
            val addDiff = 100
            if (path.cost + path.airdistance > maxTotalCost + addDiff) continue

            nc.nodesMap.currentMaxCost = maxTotalCost
            nc.nodesMap.currentPathCost = path.cost
            nc.nodesMap.destination = endPos

            routingContext.firstPrePath = null

            var link = currentNode.firstlink
            while (link != null) {
                val nextNode = link.getTarget(currentNode)!!
                if (nc.obtainNonHollowNode(nextNode) && nextNode.firstlink != null && nextNode !== sourceNode) {
                    val prePath = routingContext.createPrePath(path, link)
                    if (prePath != null) {
                        prePath.next = routingContext.firstPrePath
                        routingContext.firstPrePath = prePath
                    }
                }
                link = link.getNext(currentNode) // after the body, as Java's for-update
            }

            link = currentNode.firstlink
            while (link != null) {
                val l: OsmLink = link
                run body@{
                val nextNode = l.getTarget(currentNode)!!

                if (!nc.obtainNonHollowNode(nextNode)) return@body // border node?
                if (nextNode.firstlink == null) return@body // don't care about dead ends
                if (nextNode === sourceNode) return@body // border node?

                if (guide != null) {
                    val gidx = path.treedepth + 1
                    if (gidx >= guide.nodes.size) return@body
                    val guideNode = guide.nodes[if (routingContext.inverseRouting) guide.nodes.size - 1 - gidx else gidx]
                    val nextId = nextNode.getIdFromPos()
                    if (nextId != guideNode.getIdFromPos()) {
                        // not along the guide-track, discard, but register for voice-hint processing
                        if (routingContext.turnInstructionMode > 0) {
                            val detour = routingContext.createPath(path, l, refTrack, true)
                            if (detour.cost >= 0.0 && nextId != startNodeId1 && nextId != startNodeId2) {
                                guide.registerDetourForId(currentNode.getIdFromPos(), OsmPathElement.create(detour))
                            }
                        }
                        return@body
                    }
                }

                var bestPath: OsmPath? = null

                var isFinalLink = false
                val targetNodeId = nextNode.getIdFromPos()
                if (currentNodeId == endNodeId1 || currentNodeId == endNodeId2) {
                    if (targetNodeId == endNodeId1 || targetNodeId == endNodeId2) isFinalLink = true
                }

                var holder = firstLinkHolder
                while (holder != null) {
                    val otherPath = holder as OsmPath
                    try {
                        if (isFinalLink) {
                            endPos!!.radius = 1.5 // 1.5 meters is the upper limit that will not change the unit-test result..
                            routingContext.setWaypoint(endPos, true)
                        }
                        val testPath = routingContext.createPath(otherPath, l, refTrack, guide != null)
                        if (testPath.cost >= 0 && (bestPath == null || testPath.cost < bestPath.cost) &&
                            (testPath.getSourceNode()!!.getIdFromPos() != testPath.getTargetNode()!!.getIdFromPos())
                        ) {
                            bestPath = testPath
                        }
                    } finally {
                        if (isFinalLink) routingContext.unsetWaypoint()
                    }
                    holder = holder.getNextForLink()
                }
                if (bestPath != null) {
                    bestPath.airdistance = if (isFinalLink) 0 else nextNode.calcDistance(libm, endPos!!)

                    // `boundary` is never set (§1), so inRadius is true.
                    val limit = if (lastAirDistanceCostFactor != 0.0) maxTotalCost * lastAirDistanceCostFactor else maxTotalCost.toDouble()
                    if (isFinalLink || bestPath.cost + bestPath.airdistance <= limit + addDiff) {
                        // add only if this may beat an existing path for that link
                        var dominator = l.getFirstLinkHolder(currentNode)
                        while (dominator != null) {
                            val dp = dominator as OsmPath
                            if (dp.airdistance != -1 && bestPath.definitlyWorseThan(dp)) break
                            dominator = dominator.getNextForLink()
                        }

                        if (dominator == null) {
                            bestPath.treedepth = path.treedepth + 1
                            l.addLinkHolder(bestPath, currentNode)
                            addToOpenset(bestPath)
                        }
                    }
                }
                }
                link = l.getNext(currentNode) // after the body, as Java's for-update
            }
        }

        if (nodesVisited < MAXNODES_ISLAND_CHECK && islandNodePairs.getFreezeCount() < 5) {
            throw RoutingIslandException()
        }

        return null
    }

    // RoutingEngine.java:2263-2267.
    private fun addToOpenset(path: OsmPath) {
        if (path.cost >= 0) {
            openSet.add(path.cost + (path.airdistance * airDistanceCostFactor).toInt(), path)
        }
    }

    // RoutingEngine.java:2269-2319.
    private fun compileTrack(path: OsmPath, @Suppress("UNUSED_PARAMETER") verbose: Boolean): OsmTrack {
        var element: OsmPathElement? = OsmPathElement.create(path)

        // for final track, cut endnode
        if (guideTrack != null && element!!.origin != null) element = element.origin

        val totalTime = element!!.getTime()
        val totalEnergy = element.getEnergy()

        val track = OsmTrack()
        track.cost = path.cost
        track.energy = path.getTotalEnergy().toInt()

        var distance = 0

        while (element != null) {
            if (guideTrack != null && element.message == null) element.message = MessageData()
            val nextElement = element.origin
            // ignore double element
            if (nextElement != null && nextElement.positionEquals(element)) {
                element = nextElement
                continue
            }
            if (routingContext.inverseRouting) {
                element.setTime(totalTime - element.getTime())
                element.setEnergy(totalEnergy - element.getEnergy())
                track.nodes.add(element)
            } else {
                track.nodes.add(0, element)
            }

            if (nextElement != null) distance += element.calcDistance(libm, nextElement)
            element = nextElement
        }
        track.distance = distance
        track.buildMap()

        // for final track..
        val guide = guideTrack
        if (guide != null) track.copyDetours(guide)
        return track
    }

    // RoutingEngine.java:2321-2350.
    private fun mergeTrack(match: OsmPathElement, oldTrack: OsmTrack): OsmTrack {
        var element: OsmPathElement? = match
        val track = OsmTrack()
        track.cost = oldTrack.cost

        while (element != null) {
            track.addNode(element)
            element = element.origin
        }
        var lastId = 0L
        val id1 = match.getIdFromPos()
        val id0 = match.origin?.getIdFromPos() ?: 0L
        var appending = false
        for (n in oldTrack.nodes) {
            if (appending) track.nodes.add(n)
            val id = n.getIdFromPos()
            if (id == id1 && lastId == id0) appending = true
            lastId = id
        }
        track.buildMap()
        return track
    }

    fun getFoundTrack(): OsmTrack = foundTrack

    fun getErrorMessage(): String? = errorMessage

    private fun unsupported(what: String): Nothing =
        throw UnsupportedOperationException("RoutingEngine: $what is not supported (step 8 report §1)")

    companion object {
        const val BROUTER_ENGINEMODE_ROUTING = 0
        private const val MAXNODES_ISLAND_CHECK = 500

        /** Java's `!Boolean.getBoolean("disableDirectWeaving")`: a JVM property Forager never sets (§1) [I]. */
        private const val DIRECT_WEAVING = true

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
