package com.zynergylabs.forager.brouter.router

import kotlin.time.TimeSource

/**
 * The engine's clock (step 8c, RECORD -124): milliseconds, read only as differences, for the time limit and the watchdog's
 * seconds. Java reads `System.currentTimeMillis()` (`RoutingEngine.java:201`, `:1976`, `:1981`, `:1797`); the port reads
 * this, given to [RoutingEngine]'s constructor, so a test can drive time and no commonMain logic reads the system's.
 */
internal fun interface EngineClock {
    fun nowMillis(): Long

    companion object {
        /**
         * Time since this clock was made, on the platform's monotonic source. Java's wall clock moves when the system time
         * is set and this does not; the difference changes only when a limit strikes across such a change.
         */
        val MONOTONIC: EngineClock = object : EngineClock {
            private val start = TimeSource.Monotonic.markNow()

            override fun nowMillis(): Long = start.elapsedNow().inWholeMilliseconds
        }
    }
}
