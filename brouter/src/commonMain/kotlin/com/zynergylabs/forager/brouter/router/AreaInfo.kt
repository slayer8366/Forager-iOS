package com.zynergylabs.forager.brouter.router

/**
 * Port of BRouter 1.7.10's `btools.router.AreaInfo`, the type and its fields only: T2 keeps none of its methods
 * (`RoutingContext.ai` is set only by BRouter's round-trip search, which Forager does not use). `checkAreaInfo` needs
 * `BExpressionContext.createNewLookupData`, which is not ported either; it and the three percentage getters are not
 * ported.
 */
internal class AreaInfo(dir: Int) {
    var direction: Int = dir
    var numForest: Int = -1
    var numRiver: Int = -1
    var polygon: OsmNogoPolygon? = null
    var ways: Int = 0
    var greenWays: Int = 0
    var riverWays: Int = 0
    var elevStart: Double = 0.0
    var elev50: Int = 0

    companion object {
        const val RESULT_TYPE_NONE = 0
        const val RESULT_TYPE_ELEV50 = 1
        const val RESULT_TYPE_GREEN = 4
        const val RESULT_TYPE_RIVER = 5
    }
}
