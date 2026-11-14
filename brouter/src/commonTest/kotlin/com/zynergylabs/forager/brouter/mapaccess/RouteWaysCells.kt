package com.zynergylabs.forager.brouter.mapaccess

import kotlin.math.PI
import kotlin.math.cos

/**
 * `RouteWaysTest`'s point and cell helpers, moved here unchanged (session 68, a pure move: only `private` dropped and the
 * companion's indentation) so that `RouteWaysEntryTest` reads the same points and cells. Session 69 moved this file from
 * jvmTest to commonTest for `RouteWaysCommonTest`, changing only `cells`' `java.lang.Math` calls: `kotlin.math.PI` and
 * `cos` (the same on the JVM), and `Math.toRadians(lat)` as `lat * (PI / 180.0)`, bit-equal on JDK 21 (design file
 * `docs/audits/2026-11-13-brouter-port-closing/s69-route-ways-common-sibling.md`).
 */
internal object RouteWaysCells {
    const val DIVISOR = 32
    const val CELL = 1_000_000L / DIVISOR
    const val RADIUS = 150.0 // BRouterRouteWaysTest.kt:27 at a0535ece
    const val EARTH_MEAN_RADIUS_METERS = 6_371_008.8 // GeoDistance.kt:34 at a0535ece

    fun micro(degrees: Double): Long = (degrees * 1_000_000.0 + 0.5).toLong()

    /** `BRouterRouteWays.kt:63-70`: the cells 150 m around the point. */
    fun cells(lat: Double, lng: Double): List<Pair<Long, Long>> {
        val perLat = PI * EARTH_MEAN_RADIUS_METERS / 180.0
        val perLng = perLat * cos(lat * (PI / 180.0))
        val dLat = micro(RADIUS / perLat)
        val dLng = micro(RADIUS / perLng)
        val lon0 = micro(lng + 180.0)
        val lat0 = micro(lat + 90.0)
        val lonCells = (lon0 - dLng) / CELL..(lon0 + dLng) / CELL
        val latCells = (lat0 - dLat) / CELL..(lat0 + dLat) / CELL
        return lonCells.flatMap { lc -> latCells.map { ac -> lc to ac } }
    }

    /** `RoutingDataFolder.squareFileName` (`RoutingDataFolder.kt:59-67` at a0535ece) for a cell's corner. */
    fun squareFile(ilon: Long, ilat: Long): String {
        val lonDegree = ((ilon / 1e6 - 180.0 + 180.0) * 1_000_000.0 + 0.5).toInt() / 1_000_000
        val latDegree = ((ilat / 1e6 - 90.0 + 90.0) * 1_000_000.0 + 0.5).toInt() / 1_000_000
        val lon = lonDegree - 180 - lonDegree % 5
        val lat = latDegree - 90 - latDegree % 5
        return "${if (lon < 0) "W${-lon}" else "E$lon"}_${if (lat < 0) "S${-lat}" else "N$lat"}.rd5"
    }

    /** name to (from lat, from lon, to lat, to lon): `E3F0hTest.kt` `ENDS` on brouter-port, `routes.json` at Forager d6e2a0415. */
    val F0H_ENDS = linkedMapOf(
        "inside-across-the-degree-line" to doubleArrayOf(41.2742564, -73.9978932, 41.3036912, -74.0159457),
        "inside-short" to doubleArrayOf(41.2741743, -74.0013283, 41.2788313, -74.0189684),
        "inside-long" to doubleArrayOf(41.2772811, -74.0422743, 41.292043, -73.996678),
        "edge-north-a" to doubleArrayOf(41.3047796, -74.0006515, 41.3053493, -74.0355387),
        "edge-north-b" to doubleArrayOf(41.3061883, -73.9991069, 41.3020883, -74.0438051),
        "detour-west" to doubleArrayOf(41.2940064, -74.0832124, 41.2535731, -74.0887379),
        "outside-west-island" to doubleArrayOf(41.2749253, -73.9994484, 41.2684229, -74.1114362),
        "outside-west-moved" to doubleArrayOf(41.2749253, -73.9994484, 41.2588594, -74.1204499),
    )
}
