package com.zynergylabs.forager.brouter.tiles

internal actual fun testSetting(jvmProperty: String, iosVariable: String): String? = System.getProperty(jvmProperty)

internal actual fun testSettingSource(jvmProperty: String, iosVariable: String): String =
    "system property $jvmProperty (set by brouter/build.gradle.kts for jvmTest)"

internal actual fun testEnvironment(name: String): String? = System.getenv(name)

internal actual val onJvm: Boolean = true

internal actual fun testThreadStack(): String = "JVM thread ${Thread.currentThread().name}; stack size not read"
