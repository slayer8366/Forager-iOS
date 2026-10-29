package com.zynergylabs.forager.brouter.msun

// The inputs and the committed reference, parsed for the step 2b tests (dispatch 2026-10-28-01, Amendment 3, Part B).

internal fun hex(d: Double): String = d.toRawBits().toULong().toString(16).padStart(16, '0')

internal fun fromHex(s: String): Double = Double.fromBits(s.toULong(16).toLong())

/**
 * The table's latitude for [row], as CheapRuler.calcKxKyFromILat computes it (research/android-math q7/tools/
 * TableRows.java, copied there from BRouter's CheapRuler.java:35-49): DEG_TO_RAD * (ilat * 1e-6 - 90).
 */
internal fun cheapRulerLat(row: Int): Double {
    val ilat = row * 100000 + 100000 / 2
    return (kotlin.math.PI / 180.0) * (ilat * 1e-6 - 90)
}

/** One committed tab.c row: the row, its lat and cos as bits. */
internal class CosRow(val row: Int, val lat: Long, val cos: Long)

internal val msunCosRows: List<CosRow> by lazy {
    (MSUN_COS_ROWS_A.lines() + MSUN_COS_ROWS_B.lines()).map { line ->
        val f = line.split(' ')
        CosRow(f[0].toInt(), f[1].toULong(16).toLong(), f[2].toULong(16).toLong())
    }
}

/** One committed edges.c line: "c", "u" (cos, in k_rem_pio2.c's range) or "a"; its operands; the C's result. */
internal class EdgeLine(val op: String, val args: List<Double>, val result: Double, val text: String)

internal val msunEdges: List<EdgeLine> by lazy {
    MSUN_EDGES.lines().map { line ->
        val f = line.split(' ')
        EdgeLine(f[0], f.subList(1, f.size - 1).map(::fromHex), fromHex(f.last()), line)
    }
}

/**
 * The research's 299,974 atan2 pairs, regenerated as its Gen.java regenerates them (research/android-math
 * q7/tools/Gen.java at 736a4c1, from ProbeTest.kt's Lcg and atan2Sample, seed 42). Calls [each] with (dy, dx).
 * Terminates: the loop is a fixed 300,000 iterations.
 */
internal fun forEachAtan2Pair(each: (dy: Int, dx: Int) -> Unit) {
    var s = 42L
    fun next(): Int {
        s = s * 6364136223846793005L + 1442695040888963407L
        return (s ushr 33).toInt()
    }
    for (i in 0 until 300000) {
        val range = when (i % 4) { 0 -> 50; 1 -> 2000; 2 -> 100000; else -> 2000000 }
        val dx = next() % range
        val dy = next() % range
        if (dx == 0 && dy == 0) continue
        each(dy, dx)
    }
}
