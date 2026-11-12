package com.zynergylabs.forager.brouter.tiles

/**
 * Step 10 (RECORD -137, decision 4): how a common test learns a path the build or CI hands it. On the JVM it is the system
 * property [jvmProperty] that `oracleJvm()` in brouter/build.gradle.kts already sets for jvmTest. On iOS it is the
 * environment variable [iosVariable], read with `platform.posix.getenv` inside the simulator's test process: the
 * `ios-smoke` job exports `SIMCTL_CHILD_<iosVariable>`, Gradle's test task passes its environment to `xcrun simctl spawn`,
 * and simctl hands each `SIMCTL_CHILD_`-prefixed variable to the child with the prefix stripped. Null when it is not set.
 */
internal expect fun testSetting(jvmProperty: String, iosVariable: String): String?

/** Which mechanism [testSetting] reads on this platform, for failure messages. */
internal expect fun testSettingSource(jvmProperty: String, iosVariable: String): String

/**
 * Step 10, session 46: the environment variable [name] of this test process on both platforms (on iOS the `ios-smoke` job
 * exports `SIMCTL_CHILD_<name>`, as for [testSetting]); null when it is not set. [onJvm] tells the two apart, so a test may
 * skip on the JVM where CI sets nothing and fail on iOS where CI must.
 */
internal expect fun testEnvironment(name: String): String?

internal expect val onJvm: Boolean

/**
 * Step 10, session 49 (RECORD -140, decision 2): the running test thread's stack, as text for one printed line. On iOS
 * `pthread_get_stacksize_np(pthread_self())` in bytes and whether it is the main thread (`pthread_main_np`); on the JVM
 * the thread's name only (its stack is `-Xss`'s or the JVM's default, not read here).
 */
internal expect fun testThreadStack(): String
