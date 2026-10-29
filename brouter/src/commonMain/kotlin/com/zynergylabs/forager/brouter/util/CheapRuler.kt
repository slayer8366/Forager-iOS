package com.zynergylabs.forager.brouter.util

/** Tests-first stub (step 2b, second part). */
internal object CheapRuler {
    const val SCALE_CACHE_LENGTH: Int = 1800
    const val SCALE_CACHE_INCREMENT: Int = 100000

    fun buildScaleTable(cos: (Double) -> Double): Array<DoubleArray> = TODO("step 2b")

    fun calcKxKyFromILat(ilat: Int, cos: (Double) -> Double): DoubleArray = TODO("step 2b")

    fun getLonLatToMeterScales(libm: LibmSeam, ilat: Int): DoubleArray = TODO("step 2b")

    fun distance(libm: LibmSeam, ilon1: Int, ilat1: Int, ilon2: Int, ilat2: Int): Double = TODO("step 2b")
}
