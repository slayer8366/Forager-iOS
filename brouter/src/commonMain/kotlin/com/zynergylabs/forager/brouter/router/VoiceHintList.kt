package com.zynergylabs.forager.brouter.router

/**
 * BRouter 1.7.10's `btools.router.VoiceHintList`, whole. Step 7, session 19: the constructor, the fields and
 * `setTransportMode(boolean, boolean)`, which `OsmTrack.processVoiceHints`' head reaches; session 20 the rest
 * (RECORD -111).
 */
internal class VoiceHintList {
    private var transportMode: Int = TRANS_MODE_BIKE
    internal var turnInstructionMode: Int = 0
    internal var list: MutableList<VoiceHint> = ArrayList()

    fun setTransportMode(isCar: Boolean, isBike: Boolean) {
        transportMode = if (isCar) TRANS_MODE_CAR else (if (isBike) TRANS_MODE_BIKE else TRANS_MODE_FOOT)
    }

    fun setTransportMode(mode: Int) {
        transportMode = mode
    }

    fun getTransportMode(): String = when (transportMode) {
        TRANS_MODE_FOOT -> "foot"
        TRANS_MODE_CAR -> "car"
        else -> "bike"
    }

    fun transportMode(): Int = transportMode

    fun getLocusRouteType(): Int {
        if (transportMode == TRANS_MODE_CAR) {
            return 0
        }
        if (transportMode == TRANS_MODE_BIKE) {
            return 5
        }
        return 3 // foot
    }

    companion object {
        internal const val TRANS_MODE_NONE = 0
        internal const val TRANS_MODE_FOOT = 1
        internal const val TRANS_MODE_BIKE = 2
        internal const val TRANS_MODE_CAR = 3
    }
}
