package com.zynergylabs.forager.brouter.tiles

import com.zynergylabs.forager.brouter.tiles.FormatOutputs.Row

/**
 * Step 10, session 57 (RECORD -150, decision 2): the five outputs' lengths and FNV-1a digests ([FormatOutputs]) for F0's 2,
 * F0h's 8 and NT's 59 routes, made from the PORT's own production run on the JVM (`MsunLibm`, as the simulator runs it),
 * not from the oracle: on the simulator the two must be equal exactly, and a difference there is a platform difference
 * (RECORD -121). `FormatDigestsJvmTest` (jvmTest) holds every row to the value recomputed from the port's production run,
 * so the constants cannot drift; `FormatDigestsCommonTest` recomputes them on the simulator. Each row: id, mode, five
 * lengths, five digests (hints, GPX, JSON, KML, CSV). [ORACLE_VERSION] is the `OsmTrack.version` both runs set (RECORD
 * -118), held to the oracle's by the jvmTest. No position, no text (E1).
 */
internal object FormatDigests {
    const val ORACLE_VERSION = "1.7.10"

    val F0: List<Row> = listOf(
    )

    val F0H: List<Row> = listOf(
    )

    val NT: List<Row> = listOf(
    )

    private fun r(id: String, mode: Int, lengths: String, digests: String) =
        Row(id, mode, lengths.split(' ').map { it.toInt() }, digests.split(' '))
}
