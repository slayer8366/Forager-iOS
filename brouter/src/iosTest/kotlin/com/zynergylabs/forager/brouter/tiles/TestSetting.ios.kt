package com.zynergylabs.forager.brouter.tiles

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.posix.getenv

@OptIn(ExperimentalForeignApi::class)
internal actual fun testSetting(jvmProperty: String, iosVariable: String): String? = getenv(iosVariable)?.toKString()

internal actual fun testSettingSource(jvmProperty: String, iosVariable: String): String =
    "environment variable $iosVariable in the simulator's process (the ios-smoke job exports SIMCTL_CHILD_$iosVariable)"

@OptIn(ExperimentalForeignApi::class)
internal actual fun testEnvironment(name: String): String? = getenv(name)?.toKString()

internal actual val onJvm: Boolean = false

@OptIn(ExperimentalForeignApi::class)
internal actual fun testThreadStack(): String =
    "stack size ${platform.posix.pthread_get_stacksize_np(platform.posix.pthread_self())} bytes " +
        "(pthread_get_stacksize_np); main thread ${platform.posix.pthread_main_np() == 1}"
