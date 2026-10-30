package com.zynergylabs.forager.brouter.tiles

import java.io.File
import java.security.MessageDigest

/**
 * The pinned tiles (Phase 1 report, item E4) as the build's fetchTiles task leaves them (brouter/build.gradle.kts,
 * `brouter.tiles.dir`): the four brouter.de tiles under `segments/`, F0's fixture under `f0/`.
 */
object TestTiles {
    val dir: File = File(
        System.getProperty("brouter.tiles.dir") ?: error("brouter.tiles.dir is not set; run through Gradle's jvmTest"),
    )

    /** The folder the engine is pointed at for strata R, DL, MP64, ML5, PM5, NT, LB and CR. */
    val segments: File get() = File(dir, "segments")

    /** The folder holding F0's fixture, Forager's sparse `W75_N40.rd5`, alone. */
    val f0: File get() = File(dir, "f0")

    /** The pin file the fetch read (`brouter.tiles.pins`). */
    val pinFile: File = File(
        System.getProperty("brouter.tiles.pins") ?: error("brouter.tiles.pins is not set; run through Gradle's jvmTest"),
    )

    /** SHA-256 of [file], streamed: the largest tile is about 120 MB. */
    fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(1 shl 16)
        file.inputStream().use { input ->
            while (true) {
                val n = input.read(buffer)
                if (n < 0) break
                digest.update(buffer, 0, n)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
