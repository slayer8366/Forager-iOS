package com.zynergylabs.forager.brouter.util

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.LongAdder

/**
 * A test-only [LibmSeam] that counts every row passed to [scales] and hands each call to [inner] unchanged (session 76; s72
 * §4.5). The engine's scale-row reads go through `LibmSeam.scales` (`CheapRuler.getLonLatToMeterScales`), and the
 * engine takes its seam from `RoutingContext(libm)`, so a `RoutingContext(CountingLibm(OracleLibm))` records the rows a
 * logic run reads, and `CountingLibm(MsunLibm)` the rows a production run reads. Not everything goes through the seam:
 * the formatters' `FormatJson.speedText` uses `MsunLibm` directly (`FormatJson.kt:330`), uncounted; its callers are
 * methods of `FormatJson` and `FormatGpx`, which these runs do not construct (session 78, review 5 S3 and N1). What is
 * recorded is that a row was fetched; how often is kept ([reads]) but not asserted anywhere, and a fetch does not show
 * that the value moved a number. One instance per run: the counts are never reset. [atan2] is passed through and not
 * counted.
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
