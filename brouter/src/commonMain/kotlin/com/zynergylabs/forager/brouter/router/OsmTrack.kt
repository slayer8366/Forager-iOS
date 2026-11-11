package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.mapaccess.MatchedWaypoint
import com.zynergylabs.forager.brouter.mapaccess.OsmPos
import com.zynergylabs.forager.brouter.util.CompactLongMap
import com.zynergylabs.forager.brouter.util.FrozenLongMap

/**
 * BRouter 1.7.10's `btools.router.OsmTrack`. Step 7, session 19: the reached core (step 7's report, §5): the fields,
 * `OsmPathElementHolder`, `addNode`, `copyDetours`, `buildMap`, `readBinary`'s null guard, `containsNode` (replacing
 * RECORD -94's stub), `getLink`, `appendTrack`, `getTotalSeconds`, `prepareSpeedProfile` and `processVoiceHints`' head.
 * The hint body, the detour methods and the file branch of `readBinary` are the hint code's sessions (RECORD -111).
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

    fun copyDetours(source: OsmTrack) {
        detourMap = source.detourMap?.let { FrozenLongMap(it) }
    }

    /** `OsmTrack.java:91-116`, a detour method: the hint code's session (RECORD -111). */
    fun addDetours(source: OsmTrack): Unit = TODO("step 7: hint code, a later session (RECORD -111)")

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

    fun getTotalSeconds(): Int {
        val s = if (nodes.size < 2) 0f else nodes[nodes.size - 1].getTime() - nodes[0].getTime()
        return (s + 0.5).toInt()
    }

    fun prepareSpeedProfile(rc: RoutingContext) {
        // sendSpeedProfile = rc.keyValues != null && rc.keyValues.containsKey( "vmax" );
    }

    /** `OsmTrack.java:452-460`, the head Forager's path reaches; the hint body (`:462-534`) is a later session. */
    fun processVoiceHints(rc: RoutingContext) {
        val v = VoiceHintList()
        voiceHints = v
        v.setTransportMode(rc.carMode, rc.bikeMode)
        v.turnInstructionMode = rc.turnInstructionMode

        if (detourMap == null && !rc.hasDirectRouting) {
            // only when no direct way points
            return
        }
        TODO("step 7: hint code, a later session (RECORD -111)")
    }

    companion object {
        fun readBinary(
            filename: String?,
            newEp: OsmNodeNamed?,
            nogoChecksums: LongArray?,
            profileChecksum: Long,
            debugInfo: StringBuilder?,
        ): OsmTrack? {
            if (filename != null) {
                // `OsmTrack.java:236-292`, the file branch: a later session (RECORD -111).
                TODO("step 7: hint code, a later session (RECORD -111)")
            }
            return null
        }
    }
}
