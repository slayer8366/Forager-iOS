package com.zynergylabs.forager.brouter.codec

/** BRouter 1.7.10's `btools.codec.WaypointMatcher` (`WaypointMatcher.java`), all four methods. */
internal interface WaypointMatcher {
    fun start(ilonStart: Int, ilatStart: Int, ilonTarget: Int, ilatTarget: Int, useAsStartWay: Boolean): Boolean

    fun transferNode(ilon: Int, ilat: Int)

    fun end()

    fun hasMatch(lon: Int, lat: Int): Boolean
}
