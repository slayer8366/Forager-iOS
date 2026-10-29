package com.zynergylabs.forager.brouter.util

import kotlin.math.PI
import kotlin.math.sqrt

/**
 * BRouter 1.7.10's btools.util.CheapRuler: the methods the port reaches (Phase 1 report, 03-reach T2): the scale table
 * (`<clinit>`, calcKxKyFromILat), getLonLatToMeterScales and distance. `destination` is not reached and is not ported.
 *
 * Cheap-Ruler: https://blog.mapbox.com/fast-geodesic-approximations-with-cheap-ruler-106f229ad016; the original is
 * https://github.com/mapbox/cheap-ruler under the ISC license, as the Java source says.
 *
 * Java's static cache, filled from Math.cos, becomes the [LibmSeam] parameter: production passes [MsunLibm], whose table
 * this object builds from msun's cos; a logic run passes the oracle's own table. Every expression keeps Java's order and
 * types: int arithmetic where Java's is int, and each int widened to double where Java widens it.
 */
internal object CheapRuler {
    // Conversion constants
    const val ILATLNG_TO_LATLNG: Double = 1e-6 // From integer to degrees
    const val KILOMETERS_TO_METERS: Int = 1000
    const val DEG_TO_RAD: Double = PI / 180.0

    // Scale cache constants
    const val SCALE_CACHE_LENGTH: Int = 1800
    const val SCALE_CACHE_INCREMENT: Int = 100000

    /** CheapRuler.java's static initialiser: row i is calcKxKyFromILat(i * 100000 + 50000), for i in 0 until 1,800. */
    fun buildScaleTable(cos: (Double) -> Double): Array<DoubleArray> =
        Array(SCALE_CACHE_LENGTH) { i -> calcKxKyFromILat(i * SCALE_CACHE_INCREMENT + SCALE_CACHE_INCREMENT / 2, cos) }

    /** CheapRuler.java:35-49, with [cos] where Java calls Math.cos. */
    fun calcKxKyFromILat(ilat: Int, cos: (Double) -> Double): DoubleArray {
        val lat = DEG_TO_RAD * (ilat * ILATLNG_TO_LATLNG - 90)
        val cos1 = cos(lat)
        val cos2 = 2 * cos1 * cos1 - 1
        val cos3 = 2 * cos1 * cos2 - cos1
        val cos4 = 2 * cos1 * cos3 - cos2
        val cos5 = 2 * cos1 * cos4 - cos3

        // Multipliers for converting integer longitude and latitude into distance
        // (http://1.usa.gov/1Wb1bv7)
        val kxky = DoubleArray(2)
        kxky[0] = (111.41513 * cos1 - 0.09455 * cos3 + 0.00012 * cos5) * ILATLNG_TO_LATLNG * KILOMETERS_TO_METERS
        kxky[1] = (111.13209 - 0.56605 * cos2 + 0.0012 * cos4) * ILATLNG_TO_LATLNG * KILOMETERS_TO_METERS
        return kxky
    }

    /**
     * The degree-to-meter scales [lon->meter, lat->meter] for [ilat]: row ilat / 100,000, truncating as Java's int
     * division does. Outside 0 until 180,000,000 Java throws ArrayIndexOutOfBoundsException; this throws Kotlin's
     * IndexOutOfBoundsException for the same rows (a negative ilat above -100,000 is row 0 in both).
     */
    fun getLonLatToMeterScales(libm: LibmSeam, ilat: Int): DoubleArray = libm.scales(ilat / SCALE_CACHE_INCREMENT)

    /** CheapRuler.java:74-79: the distance in meters between two integer points. */
    fun distance(libm: LibmSeam, ilon1: Int, ilat1: Int, ilon2: Int, ilat2: Int): Double {
        val kxky = getLonLatToMeterScales(libm, (ilat1 + ilat2) shr 1)
        val dlon = (ilon1 - ilon2) * kxky[0]
        val dlat = (ilat1 - ilat2) * kxky[1]
        return sqrt(dlat * dlat + dlon * dlon) // in m
    }
}
