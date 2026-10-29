package com.zynergylabs.forager.brouter.msun

import java.io.File

/**
 * The C reference's outputs, built by tools/msun-ref/build-ref.sh before every jvmTest run (brouter/build.gradle.kts,
 * msunReference). The script exits non-zero unless the sources match their pins, clang and gcc agree, and two of the
 * three outputs carry the research's SHA-256; so a file read here is a fresh build of bionic's C, never a stale one.
 */
internal object MsunCBuild {
    private val dir: File by lazy {
        val p = System.getProperty("brouter.msunref.dir") ?: error("brouter.msunref.dir is not set (build.gradle.kts)")
        File(p).also { require(it.isDirectory) { "no C reference at $it" } }
    }

    /** tab.c's lines without its header: "row lat cos". */
    val cosLines: List<String> by lazy { File(dir, "msun-cos.txt").readLines().filter { !it.startsWith("#") } }

    /** at2.c's lines: "dy dx atan2". */
    val atan2Lines: List<String> by lazy { File(dir, "msun-atan2.txt").readLines() }

    /** edges.c's lines: "c x result" or "a y x result". */
    val edgeLines: List<String> by lazy { File(dir, "msun-edges.txt").readLines() }
}
