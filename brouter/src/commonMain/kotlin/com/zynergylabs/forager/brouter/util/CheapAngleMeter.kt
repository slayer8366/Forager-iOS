package com.zynergylabs.forager.brouter.util

import kotlin.math.abs
import kotlin.math.sqrt

/**
 * BRouter 1.7.10's btools.util.CheapAngleMeter, every method the port reaches (Phase 1 report, 03-reach T2): the angle
 * defined by three points, delivering its cosine on the fly. The scale table and atan2 come through the [LibmSeam].
 */
internal class CheapAngleMeter(private val libm: LibmSeam) {
    /** Java's getCosAngle(): the cosine of the last [calcAngle], 1.0 when its points coincide; 0.0 before the first. */
    var cosAngle: Double = 0.0
        private set

    /** CheapAngleMeter.java:14-48. */
    fun calcAngle(lon0: Int, lat0: Int, lon1: Int, lat1: Int, lon2: Int, lat2: Int): Double {
        val lonlat2m = CheapRuler.getLonLatToMeterScales(libm, lat1)
        val lon2m = lonlat2m[0]
        val lat2m = lonlat2m[1]
        val dx10 = (lon1 - lon0) * lon2m
        val dy10 = (lat1 - lat0) * lat2m
        val dx21 = (lon2 - lon1) * lon2m
        val dy21 = (lat2 - lat1) * lat2m

        val dd = sqrt((dx10 * dx10 + dy10 * dy10) * (dx21 * dx21 + dy21 * dy21))
        if (dd == 0.0) {
            cosAngle = 1.0
            return 0.0
        }
        var sinp = (dy10 * dx21 - dx10 * dy21) / dd
        val cosp = (dy10 * dy21 + dx10 * dx21) / dd
        cosAngle = cosp

        var offset = 0.0
        var s2 = sinp * sinp
        if (s2 > 0.5) {
            if (sinp > 0.0) {
                offset = 90.0
                sinp = -cosp
            } else {
                offset = -90.0
                sinp = cosp
            }
            s2 = cosp * cosp
        } else if (cosp < 0.0) {
            sinp = -sinp
            offset = if (sinp > 0.0) -180.0 else 180.0
        }
        return offset + sinp * (57.4539 + s2 * (9.57565 + s2 * (4.30904 + s2 * 2.56491)))
    }

    companion object {
        /**
         * Math.toDegrees in the oracle's JDK: `angrad * 57.29577951308232` (read: javap -c of OpenJDK 21.0.12.1's
         * java.lang.Math, RADIANS_TO_DEGREES), not the older `angrad * 180.0 / PI`. CheapLogicRunTest checks it against the
         * oracle's getAngle at 2,399,792 evaluations.
         */
        private const val RADIANS_TO_DEGREES: Double = 57.29577951308232

        /** CheapAngleMeter.java:50-56: atan2 of the longitude and latitude differences, in degrees. */
        fun getAngle(libm: LibmSeam, lon1: Int, lat1: Int, lon2: Int, lat2: Int): Double {
            val xdiff = (lat2 - lat1).toDouble()
            val ydiff = (lon2 - lon1).toDouble()
            return libm.atan2(ydiff, xdiff) * RADIANS_TO_DEGREES
        }

        /** CheapAngleMeter.java:58-61. */
        fun getDirection(libm: LibmSeam, lon1: Int, lat1: Int, lon2: Int, lat2: Int): Double =
            normalize(getAngle(libm, lon1, lat1, lon2, lat2))

        /** CheapAngleMeter.java:63-66. `360 * (int) (...)` is int arithmetic in Java, and wraps; so it does here. */
        fun normalize(a: Double): Double =
            if (a >= 360) a - (360 * (a / 360).toInt())
            else if (a < 0) a - (360 * ((a / 360).toInt() - 1))
            else a

        /** CheapAngleMeter.java:68-73. Kotlin's `%` on doubles is Java's (truncating, the dividend's sign). */
        fun getDifferenceFromDirection(b1: Double, b2: Double): Double {
            var r = (b2 - b1) % 360.0
            if (r < -180.0) r += 360.0
            if (r >= 180.0) r -= 360.0
            return abs(r)
        }
    }
}
