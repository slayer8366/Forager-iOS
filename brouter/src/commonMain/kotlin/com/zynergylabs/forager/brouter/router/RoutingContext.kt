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
 * the path factories `:555-579`). The nogo and distance half (`:285-554`) is ported in step 6's later pieces.
 *
 * Field names are Java's, underscores included, so the oracle comparison (`RoutingContextReadTest`) matches them by
 * name. Not yet ported: `poipoints`, `nogopoints`, `ai` and the other fields typed by step 6's unported classes.
 */
@Suppress("PropertyName", "ktlint:standard:property-naming")
internal class RoutingContext(libm: LibmSeam = MsunLibm) {
    fun getAlternativeIdx(min: Int, max: Int): Int = if (alternativeIdx < min) min else if (alternativeIdx > max) max else alternativeIdx

    /** Java's public field; its `setAlternativeIdx` is this property's setter (a JVM signature clash otherwise). */
    var alternativeIdx: Int = 0
    var localFunction: String? = null
    var profileTimestamp: Long = 0
    var keyValues: Map<String, String>? = null
    var rawTrackPath: String? = null
    var rawAreaPath: String? = null

    fun getProfileName(): String = TODO("step 6: RoutingContext")

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

    fun initModel(): Unit = TODO("step 6: RoutingContext")

    fun getKeyValueChecksum(): Long = TODO("step 6: RoutingContext")

    fun readGlobalConfig(): Unit = TODO("step 6: RoutingContext")

    fun freeNoWays(): Unit = TODO("step 6: RoutingContext")

    var startDirection: Int? = null
    var startDirectionValid: Boolean = false
    var forceUseStartDirection: Boolean = false
    var roundTripDistance: Int? = null
    var roundTripDirectionAdd: Int? = null
    var roundTripPoints: Int? = null
    var allowSamewayback: Boolean = false
    var anglemeter: CheapAngleMeter = CheapAngleMeter(libm)
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
