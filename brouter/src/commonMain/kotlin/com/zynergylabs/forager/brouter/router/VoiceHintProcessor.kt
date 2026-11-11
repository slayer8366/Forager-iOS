package com.zynergylabs.forager.brouter.router

/**
 * Port of `btools.router.VoiceHintProcessor` (529 Java lines). Step 7, session 21 (RECORD -113): stubs only, so that
 * `VoiceHintProcessTest` is seen failing first. It is tested as one unit with `OsmTrack.processVoiceHints`' hint body
 * and `startSection`, through `processVoiceHints`. Hints are off on Forager's path (`processVoiceHints` returns at its
 * head there), so the test switches them on in test code.
 */
internal class VoiceHintProcessor(catchingRange: Double, private val explicitRoundabouts: Boolean, private val transportMode: Int) {
    internal val SIGNIFICANT_ANGLE = 22.5
    internal val INTERNAL_CATCHING_RANGE_NEAR = 2.0
    internal val INTERNAL_CATCHING_RANGE_WIDE = 10.0

    // private double catchingRange; // range to catch angles and merge turns (unused in Java too)

    private fun sumNonConsumedWithinCatchingRange(inputs: List<VoiceHint>, offset: Int, range: Double): Float =
        TODO("step 7: VoiceHintProcessor, session 21 (RECORD -113)")

    fun process(inputs: List<VoiceHint>): List<VoiceHint> =
        TODO("step 7: VoiceHintProcessor, session 21 (RECORD -113)")

    fun postProcess(inputs: List<VoiceHint>, catchingRange: Double, minRange: Double): List<VoiceHint> =
        TODO("step 7: VoiceHintProcessor, session 21 (RECORD -113)")

    internal fun checkForNextNoneMotorway(inputs: List<VoiceHint>, offset: Int, testsize: Int): Boolean =
        TODO("step 7: VoiceHintProcessor, session 21 (RECORD -113)")

    internal fun checkStraightHold(input: VoiceHint, inputLastSaved: VoiceHint, minRange: Double): Boolean =
        TODO("step 7: VoiceHintProcessor, session 21 (RECORD -113)")
}
