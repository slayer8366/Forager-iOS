package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.mapaccess.MatchedWaypoint
import com.zynergylabs.forager.brouter.mapaccess.OsmPos
import com.zynergylabs.forager.brouter.util.CompactLongMap
import com.zynergylabs.forager.brouter.util.FrozenLongMap
import okio.EOFException
import okio.Path.Companion.toPath
import okio.buffer
import kotlin.math.abs

/**
 * BRouter 1.7.10's `btools.router.OsmTrack`. Step 7, session 19: the reached core (step 7's report, §5): the fields,
 * `OsmPathElementHolder`, `addNode`, `copyDetours`, `buildMap`, `readBinary`'s null guard, `containsNode` (replacing
 * RECORD -94's stub), `getLink`, `appendTrack`, `getTotalSeconds`, `prepareSpeedProfile` and `processVoiceHints`' head.
 * Session 20 (RECORD -111): the detour methods (`registerDetourForId`, `addDetours`, `appendDetours`,
 * `getFromDetourMap`), `getMatchedWaypoint`, `getMinDistance` and `removeVoiceHint`. Session 22: the hint body of
 * `processVoiceHints` (with `startSection`). Session 23: the file branch of `readBinary` (`writeBinary` is not ported).
 * Session 24: `aggregateMessages`, for `FormatCsv`.
 * On Forager's path the reference track `OsmPath` takes is most likely always null (RECORD -94 [I], step 8 checks).
 */
internal class OsmTrack {
    var endPoint: MatchedWaypoint? = null
    var nogoChecksums: LongArray? = null
    var profileTimestamp: Long = 0L
    var isDirty: Boolean = false

    var showspeed: Boolean = false
    var showSpeedProfile: Boolean = false
    var showTime: Boolean = false

    var params: Map<String, String>? = null

    var pois: MutableList<OsmNodeNamed> = ArrayList()

    class OsmPathElementHolder {
        var node: OsmPathElement? = null
        var nextHolder: OsmPathElementHolder? = null
    }

    var nodes: MutableList<OsmPathElement> = ArrayList()

    private var nodesMap: CompactLongMap<OsmPathElementHolder>? = null

    private var detourMap: CompactLongMap<OsmPathElementHolder>? = null

    var voiceHints: VoiceHintList? = null

    var message: String? = null
    var messageList: MutableList<String>? = null

    var name: String = "unset"

    var matchedWaypoints: MutableList<MatchedWaypoint>? = null
    var exportWaypoints: Boolean = false
    var exportCorrectedWaypoints: Boolean = false

    var distance: Int = 0
    var ascend: Int = 0
    var plainAscend: Int = 0
    var cost: Int = 0
    var energy: Int = 0
    var iternity: MutableList<String>? = null

    fun addNode(node: OsmPathElement) {
        nodes.add(0, node)
    }

    fun registerDetourForId(id: Long, detour: OsmPathElement?) {
        val map = detourMap ?: CompactLongMap<OsmPathElementHolder>().also { detourMap = it }
        val nh = OsmPathElementHolder()
        nh.node = detour
        var h = map.get(id)
        if (h != null) {
            while (h!!.nextHolder != null) {
                h = h.nextHolder
            }
            h.nextHolder = nh
        } else {
            map.fastPut(id, nh)
        }
    }

    fun copyDetours(source: OsmTrack) {
        detourMap = source.detourMap?.let { FrozenLongMap(it) }
    }

    fun addDetours(source: OsmTrack) {
        val map = detourMap
        if (map != null) {
            val tmpDetourMap = CompactLongMap<OsmPathElementHolder>()

            (map as FrozenLongMap<OsmPathElementHolder>).getValueList()
            val oldidlist = map.getKeyArray()
            for (i in oldidlist.indices) {
                val id = oldidlist[i]
                val v = map.get(id)

                tmpDetourMap.put(id, v!!)
            }

            val sourceMap = source.detourMap
            if (sourceMap != null) {
                val idlist = (sourceMap as FrozenLongMap<OsmPathElementHolder>).getKeyArray()
                for (i in idlist.indices) {
                    val id = idlist[i]
                    val v = sourceMap.get(id)
                    if (!tmpDetourMap.contains(id) && source.nodesMap!!.contains(id)) {
                        tmpDetourMap.put(id, v!!)
                    }
                }
            }
            detourMap = FrozenLongMap(tmpDetourMap)
        }
    }

    var lastorigin: OsmPathElement? = null

    fun appendDetours(source: OsmTrack) {
        if (detourMap == null) {
            detourMap = if (source.detourMap == null) null else CompactLongMap()
        }
        if (source.detourMap != null) {
            val map = detourMap!!
            val pos = nodes.size - source.nodes.size + 1
            // Java reads this element and never uses it; read here too, so that an index out of range throws as Java's.
            if (pos > 0) {
                nodes[pos]
            }
            for (node in source.nodes) {
                val id = node.getIdFromPos()
                val nh = OsmPathElementHolder()
                if (node.origin == null && lastorigin != null) {
                    node.origin = lastorigin
                }
                nh.node = node
                lastorigin = node
                var h = map.get(id)
                if (h != null) {
                    while (h!!.nextHolder != null) {
                        h = h.nextHolder
                    }
                    h.nextHolder = nh
                } else {
                    map.fastPut(id, nh)
                }
            }
        }
    }

    fun buildMap() {
        val map = CompactLongMap<OsmPathElementHolder>()
        for (node in nodes) {
            val id = node.getIdFromPos()
            val nh = OsmPathElementHolder()
            nh.node = node
            var h = map.get(id)
            if (h != null) {
                while (h!!.nextHolder != null) {
                    h = h.nextHolder
                }
                h.nextHolder = nh
            } else {
                map.fastPut(id, nh)
            }
        }
        nodesMap = FrozenLongMap(map)
    }

    fun containsNode(node: OsmPos): Boolean = nodesMap!!.contains(node.getIdFromPos())

    fun getLink(n1: Long, n2: Long): OsmPathElement? {
        var h = nodesMap!!.get(n2)
        while (h != null) {
            val e1 = h.node!!.origin
            if (e1 != null && e1.getIdFromPos() == n1) {
                return h.node
            }
            h = h.nextHolder
        }
        return null
    }

    fun appendTrack(t: OsmTrack) {
        val ourSize = nodes.size
        if (ourSize > 0 && t.nodes.size > 1) {
            val olde = nodes[ourSize - 1]
            t.nodes[1].origin = olde
        }
        val t0 = if (ourSize > 0) nodes[ourSize - 1].getTime() else 0f
        val e0 = if (ourSize > 0) nodes[ourSize - 1].getEnergy() else 0f
        val c0 = if (ourSize > 0) nodes[ourSize - 1].cost else 0
        for (i in 0 until t.nodes.size) {
            val e = t.nodes[i]
            if (i == 0 && ourSize > 0 && nodes[ourSize - 1].getSElev() == Short.MIN_VALUE) {
                nodes[ourSize - 1].setSElev(e.getSElev())
            }
            if (i > 0 || ourSize == 0) {
                e.setTime(e.getTime() + t0)
                e.setEnergy(e.getEnergy() + e0)
                e.cost = e.cost + c0
                val m = e.message
                if (m != null) {
                    if (!(m.lon == e.getILon() && m.lat == e.getILat())) {
                        m.lon = e.getILon()
                        m.lat = e.getILat()
                    }
                }
                nodes.add(e)
            }
        }

        val tv = t.voiceHints
        if (tv != null) {
            if (ourSize > 0) {
                for (hint in tv.list) {
                    hint.indexInTrack = hint.indexInTrack + ourSize - 1
                }
            }
            val v = voiceHints
            if (v == null) {
                voiceHints = tv
            } else {
                v.list.addAll(tv.list)
            }
        } else {
            if (detourMap == null) {
                detourMap = t.detourMap
            } else {
                addDetours(t)
            }
        }

        distance += t.distance
        ascend += t.ascend
        plainAscend += t.plainAscend
        cost += t.cost
        energy = nodes[nodes.size - 1].getEnergy().toInt()

        showspeed = showspeed or t.showspeed
        showSpeedProfile = showSpeedProfile or t.showSpeedProfile
    }

    /** `OsmTrack.java:168-188`. */
    fun aggregateMessages(): List<String> {
        val res = ArrayList<String>()
        var current: MessageData? = null
        for (n in nodes) {
            val m = n.message
            if (m != null && m.wayKeyValues != null) {
                val md = m.copy()
                if (current != null) {
                    if (current.nodeKeyValues != null || current.wayKeyValues != md.wayKeyValues) {
                        res.add(current.toMessage()!!)
                    } else {
                        md.add(current)
                    }
                }
                current = md
            }
        }
        if (current != null) {
            res.add(current.toMessage()!!)
        }
        return res
    }

    /** `OsmTrack.java:190-208`. */
    fun aggregateSpeedProfile(): List<String> {
        val res = ArrayList<String>()
        var vmax = -1
        var vmaxe = -1
        var vmin = -1
        var extraTime = 0
        for (i in nodes.size - 1 downTo 1) {
            val n = nodes[i]
            val m = n.message
            val vnode = getVNode(i)
            if (m != null && (vmax != m.vmax || vmin != m.vmin || vmaxe != m.vmaxExplicit || vnode < m.vmax || extraTime != m.extraTime)) {
                vmax = m.vmax
                vmin = m.vmin
                vmaxe = m.vmaxExplicit
                extraTime = m.extraTime
                res.add("" + i + "," + vmaxe + "," + vmax + "," + vmin + "," + vnode + "," + extraTime)
            }
        }
        return res
    }

    /** `OsmTrack.java:417-423`. */
    private fun getVNode(i: Int): Int {
        val m1 = if (i + 1 < nodes.size) nodes[i + 1].message else null
        val m0 = if (i < nodes.size) nodes[i].message else null
        val vnode0 = m1?.vnode0 ?: 999
        val vnode1 = m0?.vnode1 ?: 999
        return if (vnode0 < vnode1) vnode0 else vnode1
    }

    /** `OsmTrack.java:397-405`. */
    fun getVoiceHint(i: Int): VoiceHint? {
        val vh = voiceHints ?: return null
        for (hint in vh.list) {
            if (hint.indexInTrack == i) {
                return hint
            }
        }
        return null
    }

    /** `OsmTrack.java:551-559`. */
    fun getVoiceHintTime(i: Int): Float {
        val list = voiceHints!!.list
        if (list.isNotEmpty() && i < list.size) {
            return list[i].getTime()
        }
        if (nodes.isEmpty()) {
            return 0f
        }
        return nodes[nodes.size - 1].getTime()
    }

    fun getMatchedWaypoint(idx: Int): MatchedWaypoint? {
        val wps = matchedWaypoints ?: return null
        for (wp in wps) {
            if (idx == wp.indexInTrack) {
                return wp
            }
        }
        return null
    }

    fun getTotalSeconds(): Int {
        val s = if (nodes.size < 2) 0f else nodes[nodes.size - 1].getTime() - nodes[0].getTime()
        return (s + 0.5).toInt()
    }

    fun getFromDetourMap(id: Long): OsmPathElementHolder? {
        val map = detourMap ?: return null
        return map.get(id)
    }

    fun prepareSpeedProfile(rc: RoutingContext) {
        // sendSpeedProfile = rc.keyValues != null && rc.keyValues.containsKey( "vmax" );
    }

    /**
     * `OsmTrack.java:452-534`. Forager's path reaches only the head (`:452-460`) and returns there; the hint body is
     * ported in session 22 and tested with `VoiceHintProcessor` and `startSection` as one unit (RECORD -113, -114).
     */
    fun processVoiceHints(rc: RoutingContext) {
        val v = VoiceHintList()
        voiceHints = v
        v.setTransportMode(rc.carMode, rc.bikeMode)
        v.turnInstructionMode = rc.turnInstructionMode

        if (detourMap == null && !rc.hasDirectRouting) {
            // only when no direct way points
            return
        }
        var nodeNr = nodes.size - 1
        var node: OsmPathElement? = nodes[nodeNr]
        while (node != null) {
            node = node.origin
        }

        node = nodes[nodeNr]
        val inputs = ArrayList<VoiceHint>()
        while (node != null) {
            val origin = node.origin
            if (origin != null) {
                if (nodeNr == nodes.size - 1) {
                    val input = VoiceHint()
                    inputs.add(0, input)
                    input.ilat = node.getILat()
                    input.ilon = node.getILon()
                    input.selev = node.getSElev()
                    input.goodWay = node.message
                    input.oldWay = node.message
                    input.indexInTrack = nodes.size - 1
                    input.cmd = VoiceHint.END
                }
                val input = VoiceHint()
                inputs.add(input)
                input.ilat = origin.getILat()
                input.ilon = origin.getILon()
                input.selev = origin.getSElev()
                input.indexInTrack = --nodeNr
                input.goodWay = node.message
                input.oldWay = if (origin.message == null) node.message else origin.message
                if (rc.turnInstructionMode == 8 ||
                    rc.turnInstructionMode == 4 ||
                    rc.turnInstructionMode == 2 ||
                    rc.turnInstructionMode == 9
                ) {
                    val mwpt = getMatchedWaypoint(nodeNr)
                    if (mwpt != null && mwpt.wpttype == MatchedWaypoint.WAYPOINT_TYPE_DIRECT) {
                        input.cmd = VoiceHint.BL
                        input.angle = if (nodeNr == 0) origin.message!!.turnangle else node.message!!.turnangle
                        input.distanceToNext = node.calcDistance(rc.libm, origin).toDouble()
                    }
                }
                val map = detourMap
                if (map != null) {
                    val detours = map.get(origin.getIdFromPos())
                    if (nodeNr >= 0 && detours != null) {
                        var h: OsmPathElementHolder? = detours
                        while (h != null) {
                            val e = h.node
                            input.addBadWay(startSection(e, origin))
                            h = h.nextHolder
                        }
                    }
                }
            }
            node = node.origin
        }

        val transportMode = v.transportMode()
        val vproc = VoiceHintProcessor(rc.turnInstructionCatchingRange, rc.turnInstructionRoundabouts, transportMode)
        val results = vproc.process(inputs)

        val minDistance = getMinDistance().toDouble()
        val resultsLast = vproc.postProcess(results, rc.turnInstructionCatchingRange, minDistance)
        for (hint in resultsLast) {
            v.list.add(hint)
        }
    }

    /** `OsmTrack.java:573-586`, reached only from `processVoiceHints`' hint body. */
    private fun startSection(element: OsmPathElement?, root: OsmPathElement): MessageData? {
        var e = element
        var cnt = 0
        while (e != null && e.origin != null) {
            if (e.origin!!.getILat() == root.getILat() && e.origin!!.getILon() == root.getILon()) {
                return e.message
            }
            e = e.origin
            if (cnt++ == 1000000) {
                throw IllegalArgumentException("ups: $root->$element")
            }
        }
        return null
    }

    internal fun getMinDistance(): Int {
        val v = voiceHints
        if (v != null) {
            return when (v.transportMode()) {
                VoiceHintList.TRANS_MODE_CAR -> 20
                VoiceHintList.TRANS_MODE_FOOT -> 3
                else -> 5
            }
        }
        return 2
    }

    fun removeVoiceHint(i: Int) {
        val v = voiceHints
        if (v != null) {
            var remove: VoiceHint? = null
            for (vh in v.list) {
                if (vh.indexInTrack == i) {
                    remove = vh
                }
            }
            if (remove != null) {
                v.list.remove(remove)
            }
        }
    }

    companion object {
        /**
         * `OsmTrack.java:26`: Java reads it from its jar's manifest (`getImplementationVersion()`). Forager builds BRouter
         * as a plain library with no `Implementation-Version`, so it is `null` here by default and written as Java writes
         * it (`"BRouter-" + version`, so `BRouter-null` when unset). Tests set it from the oracle's own value (RECORD -118).
         */
        var version: String? = null

        fun readBinary(
            filename: String?,
            newEp: OsmNodeNamed?,
            nogoChecksums: LongArray?,
            profileChecksum: Long,
            debugInfo: StringBuilder?,
            fileSystem: okio.FileSystem = okio.FileSystem.SYSTEM,
        ): OsmTrack? {
            var t: OsmTrack? = null
            if (filename != null) {
                // `OsmTrack.java:236-292`, the file branch: session 23 (RECORD -111).
                val f = filename.toPath()
                if (fileSystem.exists(f)) {
                    try {
                        val dis = fileSystem.source(f).buffer()
                        val ep = MatchedWaypoint.readFromStream(dis)
                        val dlon = ep.waypoint!!.ilon - newEp!!.ilon
                        val dlat = ep.waypoint!!.ilat - newEp.ilat
                        val targetMatch = dlon < 20 && dlon > -20 && dlat < 20 && dlat > -20
                        debugInfo?.append("target-delta = " + dlon + "/" + dlat + " targetMatch=" + targetMatch)
                        if (targetMatch) {
                            val nt = OsmTrack()
                            t = nt
                            nt.endPoint = ep
                            val n = dis.readInt()
                            var lastPe: OsmPathElement? = null
                            for (i in 0 until n) {
                                val pe = OsmPathElement.readFromStream(dis)
                                pe.origin = lastPe
                                lastPe = pe
                                nt.nodes.add(pe)
                            }
                            nt.cost = lastPe!!.cost
                            nt.buildMap()

                            // check cheecksums, too
                            val al = LongArray(3)
                            var pchecksum = 0L
                            try {
                                al[0] = dis.readLong()
                                al[1] = dis.readLong()
                                al[2] = dis.readLong()
                            } catch (eof: EOFException) { /* kind of expected */ }
                            try {
                                nt.isDirty = dis.readByte().toInt() != 0
                            } catch (eof: EOFException) { /* kind of expected */ }
                            try {
                                pchecksum = dis.readLong()
                            } catch (eof: EOFException) { /* kind of expected */ }
                            val nogoCheckOk = abs(al[0] - nogoChecksums!![0]) <= 20 &&
                                abs(al[1] - nogoChecksums[1]) <= 20 &&
                                abs(al[2] - nogoChecksums[2]) <= 20
                            val profileCheckOk = pchecksum == profileChecksum

                            if (debugInfo != null) {
                                debugInfo.append(" nogoCheckOk=" + nogoCheckOk + " profileCheckOk=" + profileCheckOk)
                                debugInfo.append(" al=" + formatLongs(al) + " nogoChecksums=" + formatLongs(nogoChecksums))
                            }
                            if (!(nogoCheckOk && profileCheckOk)) return null
                        }
                        dis.close()
                    } catch (e: Exception) {
                        // A known difference (RECORD -116): after BRouter's prefix this writes the port's own exception's
                        // description, not the JVM's. With 0 nodes Java's `last_pe.cost` gives JDK 21's helpful NPE text;
                        // `lastPe!!` gives Kotlin's own, and on Native the class names differ too (e.g. okio.EOFException).
                        debugInfo?.append("Error reading rawTrack: " + e)
                    }
                }
            }
            return t
        }

        private fun formatLongs(al: LongArray): String {
            val sb = StringBuilder()
            sb.append('{')
            for (l in al) {
                sb.append(l)
                sb.append(' ')
            }
            sb.append('}')
            return sb.toString()
        }
    }
}
