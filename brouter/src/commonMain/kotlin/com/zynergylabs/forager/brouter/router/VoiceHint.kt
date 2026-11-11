package com.zynergylabs.forager.brouter.router

/**
 * BRouter 1.7.10's `btools.router.VoiceHint`, whole: a voice hint's input and result data. Step 7, session 20
 * (RECORD -111). Not reached on Forager's path, where BRouter's hints stay off (RECORD -110).
 */
internal class VoiceHint {
    internal var ilon: Int = 0
    internal var ilat: Int = 0
    internal var selev: Short = 0
    internal var cmd: Int = 0
    internal var oldWay: MessageData? = null
    internal var goodWay: MessageData? = null
    internal var badWays: MutableList<MessageData>? = null
    internal var distanceToNext: Double = 0.0
    internal var indexInTrack: Int = 0

    fun getTime(): Float = TODO("step 7: VoiceHint, session 20 (RECORD -111)")

    internal var angle: Float = Float.MAX_VALUE
    internal var lowerBadWayAngle: Float = -181f
    internal var higherBadWayAngle: Float = 181f

    internal var turnAngleConsumed: Boolean = false
    internal var needsRealTurn: Boolean = false
    internal var maxBadPrio: Int = -1

    internal var roundaboutExit: Int = 0

    fun isRoundabout(): Boolean = TODO("step 7: VoiceHint, session 20 (RECORD -111)")

    fun addBadWay(badWay: MessageData?): Unit = TODO("step 7: VoiceHint, session 20 (RECORD -111)")

    fun getExitNumber(): Int = TODO("step 7: VoiceHint, session 20 (RECORD -111)")

    fun calcCommand(): Unit = TODO("step 7: VoiceHint, session 20 (RECORD -111)")

    fun formatGeometry(): String = TODO("step 7: VoiceHint, session 20 (RECORD -111)")

    fun hasGiveWay(): Boolean = TODO("step 7: VoiceHint, session 20 (RECORD -111)")

    companion object {
        internal const val C = 1 // continue (go straight)
        internal const val TL = 2 // turn left
        internal const val TSLL = 3 // turn slightly left
        internal const val TSHL = 4 // turn sharply left
        internal const val TR = 5 // turn right
        internal const val TSLR = 6 // turn slightly right
        internal const val TSHR = 7 // turn sharply right
        internal const val KL = 8 // keep left
        internal const val KR = 9 // keep right
        internal const val TLU = 10 // U-turn
        internal const val TRU = 11 // Right U-turn
        internal const val OFFR = 12 // Off route
        internal const val RNDB = 13 // Roundabout
        internal const val RNLB = 14 // Roundabout left
        internal const val TU = 15 // 180 degree u-turn
        internal const val BL = 16 // Beeline routing
        internal const val EL = 17 // exit left
        internal const val ER = 18 // exit right
        internal const val END = 100 // end point

        fun is180DegAngle(angle: Float): Boolean = TODO("step 7: VoiceHint, session 20 (RECORD -111)")
    }
}
