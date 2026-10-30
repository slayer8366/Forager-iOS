package com.zynergylabs.forager.brouter.mapaccess

/** Port of BRouter 1.7.10's `btools.mapaccess.TurnRestriction`. Step 5b's stub (item I): signatures only. */
internal class TurnRestriction {
    var isPositive: Boolean = false
    var exceptions: Short = 0
    var fromLon: Int = 0
    var fromLat: Int = 0
    var toLon: Int = 0
    var toLat: Int = 0
    var next: TurnRestriction? = null

    fun exceptBikes(): Boolean = TODO("step 5b")

    fun exceptMotorcars(): Boolean = TODO("step 5b")

    companion object {
        fun isTurnForbidden(
            first: TurnRestriction?,
            fromLon: Int,
            fromLat: Int,
            toLon: Int,
            toLat: Int,
            bikeMode: Boolean,
            carMode: Boolean,
        ): Boolean = TODO("step 5b")
    }
}
