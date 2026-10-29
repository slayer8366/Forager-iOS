package com.zynergylabs.forager.brouter.util

import com.zynergylabs.forager.brouter.msun.msunAtan2
import com.zynergylabs.forager.brouter.msun.msunCos

/**
 * The seam for the two kinds of run (dispatch 2026-10-28-01, Amendment 3 Part B; Phase 1 report, E6): the two library
 * results the port takes from outside IEEE arithmetic. CheapRuler's scale table is built from cos, and
 * CheapAngleMeter.getAngle calls atan2. It is a parameter, not a global, so the engine can carry it (item I, step 8).
 * - **Production runs** use [MsunLibm]: Android's msun cos and atan2, ported in `msun/`.
 * - **Logic runs** (jvmTest's OracleLibm) inject the oracle's own table and its Math.atan2, so the port's logic is
 *   proven exactly against the oracle.
 */
internal interface LibmSeam {
    /** Row [row] (0 until 1,800) of the scale table as [kx, ky]: CheapRuler.java's SCALE_CACHE[row]. Shared; never written. */
    fun scales(row: Int): DoubleArray

    /** atan2 where CheapAngleMeter.getAngle calls java.lang.Math.atan2. */
    fun atan2(y: Double, x: Double): Double
}

/** Production: the table built once from [msunCos], and [msunAtan2]. */
internal object MsunLibm : LibmSeam {
    override fun scales(row: Int): DoubleArray = TODO("step 2b: the table from msunCos")

    override fun atan2(y: Double, x: Double): Double = TODO("step 2b: msunAtan2")
}
