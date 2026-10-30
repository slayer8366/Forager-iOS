package com.zynergylabs.forager.brouter.mapaccess

import com.zynergylabs.forager.brouter.util.LibmSeam

/**
 * Port of BRouter 1.7.10's `btools.mapaccess.OsmPos`. [calcDistance] takes the [LibmSeam] that Java's static cos table
 * becomes in the port (`util/LibmSeam.kt`), as `CheapRuler.distance` does.
 */
internal interface OsmPos {
    fun getILat(): Int

    fun getILon(): Int

    fun getSElev(): Short

    fun getElev(): Double

    fun calcDistance(libm: LibmSeam, p: OsmPos): Int

    fun getIdFromPos(): Long
}
