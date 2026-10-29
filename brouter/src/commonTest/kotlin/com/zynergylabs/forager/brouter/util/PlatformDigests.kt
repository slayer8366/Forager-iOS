package com.zynergylabs.forager.brouter.util

import com.zynergylabs.forager.brouter.msun.forEachAtan2Pair

// H step 4's digests (Phase 1 report, "Part 7, step 4"): integer-only, so the same on every platform; one per chunk, so a
// mismatch says where without a file. Shared by commonTest (the port, on the JVM and the simulator) and jvmTest (the
// expected values, from outside the port).

/** FNV-1a, 64-bit, over each value's raw bits, low byte first. */
internal class Fnv64 {
    private var h: Long = 0xcbf29ce484222325uL.toLong()

    fun add(bits: Long) {
        var b = bits
        for (i in 0 until 8) { // eight bytes: a fixed count, so it ends
            h = (h xor (b and 0xff)) * 0x100000001b3L
            b = b ushr 8
        }
    }

    fun add(d: Double) = add(d.toRawBits())

    fun hex(): String = h.toULong().toString(16).padStart(16, '0')
}

internal const val TABLE_ROWS_PER_CHUNK = 100
internal const val ANGLE_PAIRS_PER_CHUNK = 10000

/** One scale-table row as the digest reads it: lat, cos, kx, ky. */
internal class TableRow(val lat: Double, val cos: Double, val kx: Double, val ky: Double)

/** 18 digests, one per 100 of the 1,800 rows. The loop runs over a fixed range, so it ends. */
internal fun tableChunkDigests(rowOf: (Int) -> TableRow): List<String> {
    val out = ArrayList<String>()
    var f = Fnv64()
    for (row in 0 until CheapRuler.SCALE_CACHE_LENGTH) {
        val r = rowOf(row)
        f.add(r.lat); f.add(r.cos); f.add(r.kx); f.add(r.ky)
        if (row % TABLE_ROWS_PER_CHUNK == TABLE_ROWS_PER_CHUNK - 1) {
            out.add(f.hex()); f = Fnv64()
        }
    }
    return out
}

/** Each pair's four sign combinations (lon2, lat2): every quadrant, and normalize's three branches. */
internal val PAIR_SIGNS = intArrayOf(1, 1, 1, -1, -1, 1, -1, -1)

internal class AngleDigests(val chunks: List<String>, val pairs: Int, val evaluations: Int)

/**
 * getAngle and getDirection from (0, 0) to each research pair (dy, dx) under the four signs: atan2(dy, dx) as the research
 * ran it, in each quadrant. One digest per 10,000 pairs; 30 for the 299,974. forEachAtan2Pair's loop is a fixed 300,000.
 */
internal fun angleChunkDigests(
    getAngle: (Int, Int, Int, Int) -> Double,
    getDirection: (Int, Int, Int, Int) -> Double,
): AngleDigests {
    val out = ArrayList<String>()
    var f = Fnv64()
    var pairs = 0
    var evaluations = 0
    forEachAtan2Pair { dy, dx ->
        for (k in 0 until 4) {
            val lon2 = PAIR_SIGNS[2 * k] * dy
            val lat2 = PAIR_SIGNS[2 * k + 1] * dx
            f.add(getAngle(0, 0, lon2, lat2))
            f.add(getDirection(0, 0, lon2, lat2))
            evaluations++
        }
        pairs++
        if (pairs % ANGLE_PAIRS_PER_CHUNK == 0) {
            out.add(f.hex()); f = Fnv64()
        }
    }
    if (pairs % ANGLE_PAIRS_PER_CHUNK != 0) out.add(f.hex())
    return AngleDigests(out, pairs, evaluations)
}

/** Prints a digest list as the Kotlin source PlatformInvariantsExpected.kt carries. */
internal fun digestSource(name: String, digests: List<String>): String =
    digests.chunked(4).joinToString(",\n", "internal val $name: List<String> = listOf(\n", ",\n)") { line ->
        line.joinToString(", ", "    ") { "\"$it\"" }
    }
