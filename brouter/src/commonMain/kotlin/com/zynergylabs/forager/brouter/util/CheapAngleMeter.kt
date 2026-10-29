package com.zynergylabs.forager.brouter.util

/** Tests-first stub (step 2b, second part). */
internal class CheapAngleMeter(private val libm: LibmSeam) {
    var cosAngle: Double = 0.0
        private set

    fun calcAngle(lon0: Int, lat0: Int, lon1: Int, lat1: Int, lon2: Int, lat2: Int): Double = TODO("step 2b")

    companion object {
        fun getAngle(libm: LibmSeam, lon1: Int, lat1: Int, lon2: Int, lat2: Int): Double = TODO("step 2b")

        fun getDirection(libm: LibmSeam, lon1: Int, lat1: Int, lon2: Int, lat2: Int): Double = TODO("step 2b")

        fun normalize(a: Double): Double = TODO("step 2b")

        fun getDifferenceFromDirection(b1: Double, b2: Double): Double = TODO("step 2b")
    }
}
