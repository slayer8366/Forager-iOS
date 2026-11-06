package com.zynergylabs.forager.brouter.router

import okio.FileSystem

/**
 * Port of BRouter 1.7.10's `btools.router.ProfileCache`: the parsed profiles, kept for reuse. A stub until step 6
 * ports it. Java's `static synchronized` methods keep their lock (the owner, RECORD -91).
 */
internal class ProfileCache private constructor() {
    companion object {
        fun setSize(size: Int): Unit = TODO("step 6: ProfileCache")

        /** Java's `parseProfile(RoutingContext)`, with the okio seam the port's file reads take. */
        fun parseProfile(rc: RoutingContext, fileSystem: FileSystem): Boolean = TODO("step 6: ProfileCache")

        fun releaseProfile(rc: RoutingContext): Unit = TODO("step 6: ProfileCache")
    }
}
