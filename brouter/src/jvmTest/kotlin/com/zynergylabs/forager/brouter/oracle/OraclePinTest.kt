package com.zynergylabs.forager.brouter.oracle

import btools.router.RoutingEngine
import java.io.File
import java.security.MessageDigest
import java.util.jar.JarFile
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Item I, step 0: the oracle on the test classpath is the pinned jar. The pin is held here as well as in
 * tools/fetch-oracle.sh, so an edit to the build's pin alone cannot pass a different jar through.
 */
class OraclePinTest {
    @Test
    fun routingEngineLoadsFromThePinnedReadOnlyJar() {
        val jar = File(RoutingEngine::class.java.protectionDomain.codeSource.location.toURI())
        println(
            "oracle JVM: ${System.getProperty("java.vendor")} ${System.getProperty("java.runtime.version")} " +
                "${System.getProperty("os.arch")}; jar ${jar.absolutePath}",
        )
        assertEquals("brouter-1.7.10-ro.jar", jar.name)
        assertEquals(345_776L, jar.length())
        assertEquals(PINNED_JAR_SHA256, sha256(jar))
        JarFile(jar).use { assertEquals("1.7.10", it.manifest.mainAttributes.getValue("Implementation-Version")) }
    }

    private fun sha256(file: File): String =
        MessageDigest.getInstance("SHA-256").digest(file.readBytes()).joinToString("") { "%02x".format(it) }

    private companion object {
        // Phase 1 report, item D (06-oracle/logs/jars.txt at 20e1f92).
        const val PINNED_JAR_SHA256 = "598b647f2384a22a43495de7aee44f31e037062ec9c317e3bcc58cf799dbd972"
    }
}
