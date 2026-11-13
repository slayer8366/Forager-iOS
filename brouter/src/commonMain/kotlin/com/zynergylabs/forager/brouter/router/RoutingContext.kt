package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.expressions.BExpressionContextNode
import com.zynergylabs.forager.brouter.expressions.BExpressionContextWay
import com.zynergylabs.forager.brouter.mapaccess.GeometryDecoder
import com.zynergylabs.forager.brouter.mapaccess.OsmLink
import com.zynergylabs.forager.brouter.util.CheapAngleMeter
import com.zynergylabs.forager.brouter.util.LibmSeam
import com.zynergylabs.forager.brouter.util.MsunLibm

/**
 * Port of BRouter 1.7.10's `btools.router.RoutingContext`, its configuration half (`RoutingContext.java:1-284` and
 * the path factories `:555-579`). The nogo and distance half (`:285-554`) is in `RoutingContextNogo.kt`.
 *
 * Field names are Java's, underscores included, so the oracle comparison (`RoutingContextReadTest`) matches them by
 * name.
 */
@Suppress("PropertyName", "ktlint:standard:property-naming")
internal class RoutingContext(internal val libm: LibmSeam = MsunLibm) {
    fun getAlternativeIdx(min: Int, max: Int): Int = if (alternativeIdx < min) min else if (alternativeIdx > max) max else alternativeIdx

    /** Java's public field; its `setAlternativeIdx` is this property's setter (a JVM signature clash otherwise). */
    var alternativeIdx: Int = 0
    var localFunction: String? = null
    var profileTimestamp: Long = 0
    var keyValues: Map<String, String>? = null
    var rawTrackPath: String? = null
    var rawAreaPath: String? = null

    fun getProfileName(): String {
        var name = localFunction ?: "unknown"
        // Java's own: the cut uses localFunction's length (it is only reached when localFunction ends with ".brf").
        if (name.endsWith(".brf")) name = name.substring(0, localFunction!!.length - 4)
        val idx = name.lastIndexOf('/') // File.separatorChar, '/' on the JVM, Android and iOS
        if (idx >= 0) name = name.substring(idx + 1)
        return name
    }

    var expctxWay: BExpressionContextWay? = null
    var expctxNode: BExpressionContextNode? = null
    var geometryDecoder: GeometryDecoder = GeometryDecoder()
    var memoryclass: Int = 64
    var carMode: Boolean = false
    var bikeMode: Boolean = false
    var footMode: Boolean = false
    var considerTurnRestrictions: Boolean = false
    var processUnusedTags: Boolean = false
    var forceSecondaryData: Boolean = false
    var pass1coefficient: Double = 0.0
    var pass2coefficient: Double = 0.0
    var elevationpenaltybuffer: Int = 0
    var elevationmaxbuffer: Int = 0
    var elevationbufferreduce: Int = 0
    var cost1speed: Double = 0.0
    var additionalcostfactor: Double = 0.0
    var changetime: Double = 0.0
    var buffertime: Double = 0.0
    var waittimeadjustment: Double = 0.0
    var inittimeadjustment: Double = 0.0
    var starttimeoffset: Double = 0.0
    var transitonly: Boolean = false
    var waypointCatchingRange: Double = 0.0
    var correctMisplacedViaPoints: Boolean = false
    var correctMisplacedViaPointsDistance: Double = 0.0
    var continueStraight: Boolean = false
    var useDynamicDistance: Boolean = false
    var buildBeelineOnRange: Boolean = false

    var pm: OsmPathModel? = null

    /**
     * Java loads `className` by reflection. The port has [StdModel] only (step 6's row): a profile's `---model:` line
     * (the six car profiles' `KinematicModel` and `KinematicNoCostModel`) is a named "unsupported".
     */
    private fun setModel(className: String?) {
        if (className != null) throw UnsupportedOperationException("unsupported: path model $className")
        pm = StdModel()
        initModel()
    }

    fun initModel() {
        pm!!.init(expctxWay, expctxNode, keyValues)
    }

    fun getKeyValueChecksum(): Long {
        var s = 0L
        // Java adds the two hashCodes as ints, then widens the sum.
        keyValues?.forEach { (k, v) -> s += (javaHashCode(k) + javaHashCode(v)).toLong() }
        return s
    }

    fun readGlobalConfig() {
        val g = expctxWay!! // Java: "just one of them..."
        setModel(g.modelClass)
        fun v(name: String, default: Float): Float = g.getVariableValue(name, default)
        carMode = 0f != v("validForCars", 0f)
        bikeMode = 0f != v("validForBikes", 0f)
        footMode = 0f != v("validForFoot", 0f)
        consider_crossing = 0f != v("consider_crossing", 0f)
        crossing_Prio_H = v("crossing_Prio_H", 0f).toInt()
        crossing_Prio_L = v("crossing_Prio_L", 0f).toInt()
        cost_ToLeft_from_H_class1 = v("cost_ToLeft_from_H_class1", 0f).toInt()
        cost_ToLeft_from_H_class2 = v("cost_ToLeft_from_H_class2", 0f).toInt()
        cost_ToLeft_from_H_class3 = v("cost_ToLeft_from_H_class3", 0f).toInt()
        cost_ToLeft_from_H_class4 = v("cost_ToLeft_from_H_class4", 0f).toInt()
        cost_ToLeft_from_H_class5 = v("cost_ToLeft_from_H_class5", 0f).toInt()
        cost_ToLeft_from_H_class6 = v("cost_ToLeft_from_H_class6", 0f).toInt()
        cost_ToRight_from_H_class1 = v("cost_ToRight_from_H_class1", 0f).toInt()
        cost_ToRight_from_H_class2 = v("cost_ToRight_from_H_class2", 0f).toInt()
        cost_ToRight_from_H_class3 = v("cost_ToRight_from_H_class3", 0f).toInt()
        cost_ToRight_from_H_class4 = v("cost_ToRight_from_H_class4", 0f).toInt()
        cost_ToRight_from_H_class5 = v("cost_ToRight_from_H_class5", 0f).toInt()
        cost_ToRight_from_H_class6 = v("cost_ToRight_from_H_class6", 0f).toInt()
        waypointCatchingRange = v("waypointCatchingRange", 250f).toDouble()
        considerTurnRestrictions = 0f != v("considerTurnRestrictions", if (footMode) 0f else 1f)
        correctMisplacedViaPoints = 0f != v("correctMisplacedViaPoints", 0f)
        correctMisplacedViaPointsDistance = v("correctMisplacedViaPointsDistance", 400f).toDouble() // 0 == don't use distance
        continueStraight = 0f != v("continueStraight", 0f)
        processUnusedTags = 0f != v("processUnusedTags", 0f)
        forceSecondaryData = 0f != v("forceSecondaryData", 0f)
        pass1coefficient = v("pass1coefficient", 1.5f).toDouble()
        pass2coefficient = v("pass2coefficient", 0f).toDouble()
        // Java's float products, then (int): Kotlin's Float.toInt() saturates and maps NaN to 0, as Java's cast does.
        elevationpenaltybuffer = (v("elevationpenaltybuffer", 5f) * 1000000).toInt()
        elevationmaxbuffer = (v("elevationmaxbuffer", 10f) * 1000000).toInt()
        elevationbufferreduce = (v("elevationbufferreduce", 0f) * 10000).toInt()
        cost1speed = v("cost1speed", 22f).toDouble()
        additionalcostfactor = v("additionalcostfactor", 1.5f).toDouble()
        changetime = v("changetime", 180f).toDouble()
        buffertime = v("buffertime", 120f).toDouble()
        waittimeadjustment = v("waittimeadjustment", 0.9f).toDouble()
        inittimeadjustment = v("inittimeadjustment", 0.2f).toDouble()
        starttimeoffset = v("starttimeoffset", 0f).toDouble()
        transitonly = v("transitonly", 0f) != 0f
        showspeed = 0f != v("showspeed", 0f)
        showSpeedProfile = 0f != v("showSpeedProfile", 0f)
        inverseRouting = 0f != v("inverseRouting", 0f)
        showTime = 0f != v("showtime", 0f)
        val tiMode = v("turnInstructionMode", 0f).toInt()
        if (tiMode != 1) { // automatic selection from coordinate source
            turnInstructionMode = tiMode
        }
        turnInstructionCatchingRange = v("turnInstructionCatchingRange", 40f).toDouble()
        turnInstructionRoundabouts = v("turnInstructionRoundabouts", if (footMode) 0f else 1f) != 0f
        totalMass = v("totalMass", 90f).toDouble()
        maxSpeed = if (footMode) v("maxSpeed", 6f) / 3.6 else v("maxSpeed", 45f) / 3.6
        S_C_x = v("S_C_x", 0.5f * 0.45f).toDouble()
        defaultC_r = v("C_r", 0.01f).toDouble()
        bikerPower = v("bikerPower", 100f).toDouble()
        useDynamicDistance = v("use_dynamic_range", 1f) == 1f
        buildBeelineOnRange = v("add_beeline", 0f) == 1f
        val test = v("check_start_way", 1f) == 1f
        if (!test) freeNoWays()
    }

    fun freeNoWays() {
        expctxWay?.freeNoWays()
    }

    var startDirection: Int? = null
    var startDirectionValid: Boolean = false
    var forceUseStartDirection: Boolean = false
    var roundTripDistance: Int? = null
    var roundTripDirectionAdd: Int? = null
    var roundTripPoints: Int? = null
    var allowSamewayback: Boolean = false
    var anglemeter: CheapAngleMeter = CheapAngleMeter(libm)
    var ai: AreaInfo? = null
    var poipoints: MutableList<OsmNodeNamed>? = null
    var nogopoints: MutableList<OsmNodeNamed>? = null

    // Java's private fields, `internal` here for the nogo half in `RoutingContextNogo.kt`.
    internal var nogopoints_all: MutableList<OsmNodeNamed>? = null // full list not filtered for wayoints-in-nogos
    internal var keepnogopoints: MutableList<OsmNodeNamed>? = null
    internal var pendingEndpoint: OsmNodeNamed? = null

    var nogoCost: Double = 0.0
    var isEndpoint: Boolean = false
    var shortestmatch: Boolean = false
    var wayfraction: Double = 0.0
    var ilatshortest: Int = 0
    var ilonshortest: Int = 0
    var inverseDirection: Boolean = false
    var showspeed: Boolean = false
    var showSpeedProfile: Boolean = false
    var inverseRouting: Boolean = false
    var showTime: Boolean = false
    var hasDirectRouting: Boolean = false
    var outputFormat: String = "gpx"
    var exportWaypoints: Boolean = false
    var exportCorrectedWaypoints: Boolean = false
    var firstPrePath: OsmPrePath? = null

    /** 0=none, 1=auto, 2=locus, 3=osmand, 4=comment-style, 5=gpsies-style (Java's comment). */
    var turnInstructionMode: Int = 0
    var turnInstructionCatchingRange: Double = 0.0
    var turnInstructionRoundabouts: Boolean = false

    // Speed computation model (for bikes).
    var totalMass: Double = 0.0
    var maxSpeed: Double = 0.0
    var S_C_x: Double = 0.0
    var defaultC_r: Double = 0.0
    var bikerPower: Double = 0.0

    var consider_crossing: Boolean = false
    var crossing_Prio_H: Int = 0
    var crossing_Prio_L: Int = 0
    var cost_ToLeft_from_H_class1: Int = 0
    var cost_ToLeft_from_H_class2: Int = 0
    var cost_ToLeft_from_H_class3: Int = 0
    var cost_ToLeft_from_H_class4: Int = 0
    var cost_ToLeft_from_H_class5: Int = 0
    var cost_ToLeft_from_H_class6: Int = 0
    var cost_ToRight_from_H_class1: Int = 0
    var cost_ToRight_from_H_class2: Int = 0
    var cost_ToRight_from_H_class3: Int = 0
    var cost_ToRight_from_H_class4: Int = 0
    var cost_ToRight_from_H_class5: Int = 0
    var cost_ToRight_from_H_class6: Int = 0

    fun createPrePath(origin: OsmPath, link: OsmLink): OsmPrePath? {
        val p = pm!!.createPrePath()
        p?.init(origin, link, this)
        return p
    }

    fun createPath(link: OsmLink): OsmPath {
        val p = pm!!.createPath()
        p.init(link)
        return p
    }

    fun createPath(origin: OsmPath, link: OsmLink, refTrack: OsmTrack?, detailMode: Boolean): OsmPath {
        val p = pm!!.createPath()
        p.init(origin, link, refTrack, detailMode, this)
        return p
    }
}

/** Java's `String.hashCode()`, written out: Kotlin/Native does not promise Java's value. */
internal fun javaHashCode(s: String): Int {
    var h = 0
    for (c in s) h = 31 * h + c.code
    return h
}
