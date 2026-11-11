package com.zynergylabs.forager.brouter.router

/**
 * BRouter 1.7.10's `btools.router.VoiceHint`, the type and `indexInTrack` only: `OsmTrack.appendTrack` shifts it when
 * tracks are joined (`OsmTrack.java:365-367`). The class is the hint code's session (RECORD -111).
 */
internal class VoiceHint {
    internal var indexInTrack: Int = 0
}
