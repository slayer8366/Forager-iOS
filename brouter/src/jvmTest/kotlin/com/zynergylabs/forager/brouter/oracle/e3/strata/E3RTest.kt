package com.zynergylabs.forager.brouter.oracle.e3.strata

import kotlin.test.Test

/** Stratum R on Forager's own profile, the first of E4's 26 ([E3R]); the other 25 are [E3RProfilesTest]. */
class E3RTest {
    @Test
    fun rOnHikingMountainIsCountedAgainstE3sStop() = E3R.run(PROFILE, SEED)

    private companion object {
        const val PROFILE = "hiking-mountain" // Forager's own profile: the first of E4's 26
        const val SEED = 20261030L
    }
}
