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

    fun setTransportMode(mode: Int): Unit = TODO("step 7: VoiceHintList, session 20 (RECORD -111)")

    fun getTransportMode(): String = TODO("step 7: VoiceHintList, session 20 (RECORD -111)")

    fun transportMode(): Int = TODO("step 7: VoiceHintList, session 20 (RECORD -111)")

    fun getLocusRouteType(): Int = TODO("step 7: VoiceHintList, session 20 (RECORD -111)")

    companion object {
        internal const val TRANS_MODE_NONE = 0
        internal const val TRANS_MODE_FOOT = 1
        internal const val TRANS_MODE_BIKE = 2
        internal const val TRANS_MODE_CAR = 3
    }
}
