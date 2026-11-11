package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.mapaccess.MatchedWaypoint
import com.zynergylabs.forager.brouter.mapaccess.OsmPos
import com.zynergylabs.forager.brouter.util.CompactLongMap

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

    fun addNode(node: OsmPathElement): Unit = TODO("step 7 stub: OsmTrack.addNode")

    fun copyDetours(source: OsmTrack): Unit = TODO("step 7 stub: OsmTrack.copyDetours")

    fun buildMap(): Unit = TODO("step 7 stub: OsmTrack.buildMap")

    fun containsNode(node: OsmPos): Boolean = TODO("step 7 stub: OsmTrack.containsNode")

    fun getLink(n1: Long, n2: Long): OsmPathElement? = TODO("step 7 stub: OsmTrack.getLink")

    fun appendTrack(t: OsmTrack): Unit = TODO("step 7 stub: OsmTrack.appendTrack")

    fun getTotalSeconds(): Int = TODO("step 7 stub: OsmTrack.getTotalSeconds")

    fun prepareSpeedProfile(rc: RoutingContext): Unit = TODO("step 7 stub: OsmTrack.prepareSpeedProfile")

    fun processVoiceHints(rc: RoutingContext): Unit = TODO("step 7 stub: OsmTrack.processVoiceHints")

    companion object {
        fun readBinary(
            filename: String?,
            newEp: OsmNodeNamed?,
            nogoChecksums: LongArray?,
            profileChecksum: Long,
            debugInfo: StringBuilder?,
        ): OsmTrack? = TODO("step 7 stub: OsmTrack.readBinary")
    }
}
