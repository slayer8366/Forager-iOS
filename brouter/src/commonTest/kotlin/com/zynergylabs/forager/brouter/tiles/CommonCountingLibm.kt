package com.zynergylabs.forager.brouter.tiles

import com.zynergylabs.forager.brouter.util.LibmSeam

/**
 * Session 77: jvmTest's `CountingLibm` for common code (that one uses `java.util.concurrent`): records every row passed to
 * [scales] and hands each call to [inner] unchanged; [atan2] is passed through and not recorded. [take] returns the rows
 * recorded since the last [take] and starts a new set, so one instance passed to [Step10Cases.check] is read per case in
 * its `onCase`. Not thread-safe: `EngineRun.runTrack` runs the engine on the calling thread [I: `doRun` is called
 * synchronously there; whether the engine reads the table from another thread was not traced].
 */
internal class CommonCountingLibm(private val inner: LibmSeam) : LibmSeam {
    private var rows = HashSet<Int>()

    override fun scales(row: Int): DoubleArray {
        rows.add(row)
        return inner.scales(row)
    }

    override fun atan2(y: Double, x: Double): Double = inner.atan2(y, x)

    /** The rows read since the last call, ascending; the record starts again empty. */
    fun take(): Set<Int> = rows.sorted().toSet().also { rows = HashSet() }
}
