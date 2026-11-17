package com.zynergylabs.forager.brouter.util

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.LongAdder

/**
 * A test-only [LibmSeam] that counts every row passed to [scales] and hands each call to [inner] unchanged (session 76; s72
 * §4.5). Every scale row the engine reads goes through `LibmSeam.scales` (`CheapRuler.getLonLatToMeterScales`), and the
 * engine takes its seam from `RoutingContext(libm)`, so a `RoutingContext(CountingLibm(OracleLibm))` records the rows a
 * logic run reads, and `CountingLibm(MsunLibm)` the rows a production run reads. One instance per run: the counts are
 * never reset. [atan2] is passed through and not counted.
 */
internal class CountingLibm(private val inner: LibmSeam) : LibmSeam {
    private val counts = ConcurrentHashMap<Int, LongAdder>()

    override fun scales(row: Int): DoubleArray {
        counts.computeIfAbsent(row) { LongAdder() }.increment()
        return inner.scales(row)
    }

    override fun atan2(y: Double, x: Double): Double = inner.atan2(y, x)

    /** The rows passed to [scales] so far, ascending. */
    val rows: Set<Int> get() = counts.keys.toSortedSet()

    /** How many times [row] was passed to [scales]. */
    fun reads(row: Int): Long = counts[row]?.sum() ?: 0L
}
