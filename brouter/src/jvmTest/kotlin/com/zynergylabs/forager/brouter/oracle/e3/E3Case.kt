package com.zynergylabs.forager.brouter.oracle.e3

import java.io.File
import java.util.Locale

/**
 * One case (Phase 1 report, E1): the tile folder, the two ends as Forager sends them, the profile, `memoryclass` and the
 * time limit. The params map is Forager's own and fixed (see [E3CaseRun]); no stratum built so far has a cancel plan.
 * A generated case's ends are rounded to six decimals by its generator, so `ilon` and `ilat` are exact (E4); F0's are
 * Forager's own, kept as given. The line form writes each with Double.toString, which reads back to the same double.
 */
class E3Case(
    val id: String,
    val segments: File,
    val startLat: Double,
    val startLon: Double,
    val endLat: Double,
    val endLon: Double,
    val profile: String,
    val memoryclass: Int = 64,
    val maxRunningTimeMillis: Long = 0L,
) {
    /** One tab-separated line, the form a child JVM reads its case list in. */
    fun toLine(): String = listOf(
        id, segments.absolutePath, startLat, startLon, endLat, endLon, profile, memoryclass,
        maxRunningTimeMillis,
    ).joinToString("\t")

    companion object {
        /** [d] rounded to six decimals, the form E4 writes a generated end in. */
        fun six(d: Double): Double = String.format(Locale.ROOT, "%.6f", d).toDouble()

        fun fromLine(line: String): E3Case {
            val f = line.split('\t')
            require(f.size == 9) { "a case line has 9 fields, not ${f.size}: $line" }
            return E3Case(
                f[0], File(f[1]), f[2].toDouble(), f[3].toDouble(), f[4].toDouble(), f[5].toDouble(), f[6],
                f[7].toInt(), f[8].toLong(),
            )
        }

        fun writeList(cases: List<E3Case>, file: File) = file.writeText(cases.joinToString("\n") { it.toLine() } + "\n")

        fun readList(file: File): List<E3Case> = file.readLines().filter { it.isNotBlank() }.map { fromLine(it) }
    }
}

/**
 * E1's compared tuple for one run: the kind (the message, null for a route; the two timing families by family only),
 * the node count, cost and distance, and a 64-bit FNV-1a digest over every node's (ilon, ilat) in order. Comparing the
 * digest stands in for comparing the node list pair by pair, up to a 2^-64 collision. It is E3's comparison only, not
 * the stored form's digest, which is written with the expected values.
 */
data class E3Outcome(val kind: String, val nodes: Int, val cost: Int, val distance: Int, val nodeDigest: String) {
    fun toField(): String = "$kind|$nodes|$cost|$distance|$nodeDigest"

    companion object {
        fun fromField(s: String): E3Outcome {
            val f = s.split('|')
            val n = f.size
            // The kind is a message and may hold '|'; the last four fields are numbers.
            return E3Outcome(f.subList(0, n - 4).joinToString("|"), f[n - 4].toInt(), f[n - 3].toInt(), f[n - 2].toInt(), f[n - 1])
        }

        /** E1's failure-kind table: the timeout and watchdog messages compare by family; every other message exactly. */
        fun kindOf(message: String?): String = when {
            message == null -> "route"
            Regex(".* timeout after \\d+ seconds").matches(message) -> "family:timeout"
            message.startsWith("operation killed by thread-priority-watchdog after ") -> "family:watchdog"
            else -> message.replace('\t', ' ').replace('\n', ' ')
        }

        fun fnv1a(pairs: List<Pair<Int, Int>>): String {
            var h = -0x340d631b7bdddcdbL // 0xcbf29ce484222325
            fun mix(v: Int) {
                for (shift in 0 until 32 step 8) {
                    h = h xor ((v ushr shift) and 0xff).toLong()
                    h *= 0x100000001b3L
                }
            }
            for ((lon, lat) in pairs) {
                mix(lon)
                mix(lat)
            }
            return java.lang.Long.toHexString(h)
        }
    }
}
