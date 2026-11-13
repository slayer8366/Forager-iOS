package com.zynergylabs.forager.brouter.router

/**
 * Port of BRouter 1.7.10's `btools.router.OsmNogoPolygon`, the type only: T2 keeps none of its methods (Forager passes
 * no nogo polygons). `RoutingContext`'s nogo half tests `is OsmNogoPolygon` as Java does, and the four methods it
 * would then call are a named "unsupported" (RECORD -74). `addVertex`, `calcBoundingCircle` and `isOnLine` are not
 * ported.
 */
internal class OsmNogoPolygon(closed: Boolean) : OsmNodeNamed() {
    class Point internal constructor(lon: Int, lat: Int) {
        val y: Int = lat
        val x: Int = lon
    }

    val points: MutableList<Point> = ArrayList()
    val isClosed: Boolean = closed

    init {
        isNogo = true
        name = ""
    }

    @Suppress("UNUSED_PARAMETER")
    fun intersects(lon0: Int, lat0: Int, lon1: Int, lat1: Int): Boolean = unsupported("intersects")

    @Suppress("UNUSED_PARAMETER")
    fun isOnPolyline(px: Long, py: Long): Boolean = unsupported("isOnPolyline")

    @Suppress("UNUSED_PARAMETER")
    fun isWithin(px: Long, py: Long): Boolean = unsupported("isWithin")

    @Suppress("UNUSED_PARAMETER")
    fun distanceWithinPolygon(lon1: Int, lat1: Int, lon2: Int, lat2: Int): Double = unsupported("distanceWithinPolygon")

    private fun unsupported(method: String): Nothing = throw UnsupportedOperationException("unsupported: OsmNogoPolygon.$method")
}
