package com.zynergylabs.forager.brouter.util

/**
 * The logic run's seam: the oracle's own scale table, read from BRouter 1.7.10's btools.util.CheapRuler in this JVM (its
 * SCALE_CACHE, built by its static initialiser with this JVM's Math.cos), and this JVM's Math.atan2, which the oracle's
 * getAngle calls. jvmTest runs with -XX:-UseLibmIntrinsic (build.gradle.kts), as the oracle does (RECORD -24);
 * CheapLogicRunTest.theJvmRunsWithLibmIntrinsicsOff checks that it really does.
 */
internal object OracleLibm : LibmSeam {
    override fun scales(row: Int): DoubleArray =
        btools.util.CheapRuler.getLonLatToMeterScales(row * CheapRuler.SCALE_CACHE_INCREMENT)

    override fun atan2(y: Double, x: Double): Double = Math.atan2(y, x)
}

/** The table's latitude for [row], as CheapRuler.java:36 computes it for ilat = row * 100000 + 50000. */
internal fun oracleLat(row: Int): Double = (Math.PI / 180.0) * ((row * 100000 + 50000) * 1e-6 - 90)

/** A 64-bit LCG (the research's constants, MsunInputs.forEachAtan2Pair), for the logic run's corpora. */
internal class Lcg(private var s: Long) {
    fun next(): Int {
        s = s * 6364136223846793005L + 1442695040888963407L
        return (s ushr 33).toInt()
    }

    /** In [lo, hi], both inclusive; hi - lo below 2^31 - 1. */
    fun between(lo: Int, hi: Int): Int = lo + next() % (hi - lo + 1)
}

internal fun bits(d: Double): String = java.lang.Long.toHexString(d.toRawBits())
