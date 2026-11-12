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
