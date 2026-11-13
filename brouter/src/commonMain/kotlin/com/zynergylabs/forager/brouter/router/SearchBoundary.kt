package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.mapaccess.OsmNode
import com.zynergylabs.forager.brouter.util.LibmSeam
import com.zynergylabs.forager.brouter.util.MsunLibm

/**
 * Port of BRouter 1.7.10's `btools.router.SearchBoundary`, whole (`SearchBoundary.java`, 87 lines): the constructor,
 * `getFileName`, `isInBoundary` and `getBoundaryDistance`. Forager does not reach it (it is only a field at
 * `RoutingEngine.java:88`); it is on T2's port list. Java's `int` division truncates toward zero, as Kotlin's does.
 * Distances run on [libm] (`OsmNode.calcDistance`), [MsunLibm] by default; `SearchBoundaryTest` compares it with the
 * oracle on `OracleLibm`.
 */
internal class SearchBoundary(n: OsmNode, private val radius: Int, internal val direction: Int, private val libm: LibmSeam = MsunLibm) {
    private val p = OsmNode(n.ilon, n.ilat)
    private val minlon0: Int
    private val minlat0: Int
    private val maxlon0: Int
    private val maxlat0: Int
    private val minlon: Int
    private val minlat: Int
    private val maxlon: Int
    private val maxlat: Int

    init {
        val lon = (n.ilon / 5000000) * 5000000
        val lat = (n.ilat / 5000000) * 5000000
        minlon0 = lon - 5000000
        minlat0 = lat - 5000000
        maxlon0 = lon + 10000000
        maxlat0 = lat + 10000000
        minlon = lon - 1000000
        minlat = lat - 1000000
        maxlon = lon + 6000000
        maxlat = lat + 6000000
    }

    fun isInBoundary(n: OsmNode, cost: Int): Boolean {
        if (radius > 0) {
            return n.calcDistance(libm, p) < radius
        }
        if (cost == 0) {
            return n.ilon > minlon0 && n.ilon < maxlon0 && n.ilat > minlat0 && n.ilat < maxlat0
        }
        return n.ilon > minlon && n.ilon < maxlon && n.ilat > minlat && n.ilat < maxlat
    }

    fun getBoundaryDistance(n: OsmNode): Int = when (direction) {
        0 -> n.calcDistance(libm, OsmNode(n.ilon, minlat))
        1 -> n.calcDistance(libm, OsmNode(minlon, n.ilat))
        2 -> n.calcDistance(libm, OsmNode(n.ilon, maxlat))
        3 -> n.calcDistance(libm, OsmNode(maxlon, n.ilat))
        else -> throw IllegalArgumentException("undefined direction: $direction")
    }

    companion object {
        fun getFileName(n: OsmNode): String {
            val lon = (n.ilon / 5000000) * 5000000
            val lat = (n.ilat / 5000000) * 5000000
            val dlon = lon / 1000000 - 180
            val dlat = lat / 1000000 - 90
            val slon = if (dlon < 0) "W" + (-dlon) else "E$dlon"
            val slat = if (dlat < 0) "S" + (-dlat) else "N$dlat"
            return slon + "_" + slat + ".trf"
        }
    }
}
