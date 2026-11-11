package com.zynergylabs.forager.brouter.oracle.e3

import java.io.File

/**
 * 1b's expected values (RECORD -88; Phase 1 report, E1 to E3): the stored file's form, written by
 * [com.zynergylabs.forager.brouter.oracle.e3.strata.E3ExpectedWriteTest] from each case's R1 and read by [E3ExpectedFileTest].
 *
 * The file is tab-separated UTF-8. Lines starting `# ` are the head; the first line after them names the columns:
 * `stratum id compare kind nodes cost distance nodeDigest heightsDigest tupleDigest`. No coordinate is stored (E1).
 * - The head's `# jdk ` line names the JDK the values were written on: `java.vendor`, `java.runtime.version`, `os.arch` (E2).
 * - `tupleDigest` is a 64-bit FNV-1a over the UTF-8 bytes of the tuple as [E3Outcome.toField] writes it,
 *   `kind|nodes|cost|distance|nodeDigest|heightsDigest`: integer arithmetic only, so any platform recomputes it.
 * - `compare` says how a later run is held to the row: `exact` (the whole tuple); `kind` and `family` (the kind only: a case
 *   unstable at write time, each named on a `# excluded ` line); `outcome` (FK's extra cases, RECORD -80 and -85: FK7-x1 on its
 *   kind; FK7-x2 and FK8-x1 on FK7-c0's tuple, which is checked).
 */
object E3Expected {
    const val RESOURCE = "/e3/expected-1b.tsv"
    const val COLUMNS = "stratum\tid\tcompare\tkind\tnodes\tcost\tdistance\tnodeDigest\theightsDigest\ttupleDigest"

    /** The pinned oracle JDK (RECORD -47, E2), as [com.zynergylabs.forager.brouter.oracle.e3.strata.E3Strata.jdk] checks it. */
    const val JDK = "java.vendor=Eclipse Adoptium; java.runtime.version=21.0.13+9-LTS; os.arch=amd64"

    /** Where the strata's committed logs are, from the brouter project folder (Gradle's test working directory). */
    const val LOGS = "../docs/audits/2026-10-29-brouter-port-phase2/step1b"

    /** Each stratum, its case count (RECORD -88) and the committed stability table its R1 was logged in. */
    val STRATA: Map<String, Pair<Int, String>> = linkedMapOf(
        "F0" to (2 to "14-f0-stability.tsv"),
        "F0h" to (8 to "12-f0h-stability.tsv"),
        "R" to (1534 to "16-r-26-profiles-stability.tsv"),
        "DL-lat" to (59 to "17-dl-lat-stability.tsv"),
        "DL-lon" to (59 to "18-dl-lon-stability.tsv"),
        "MP64" to (59 to "19-mp64-stability.tsv"),
        "ML5" to (59 to "20-ml5-stability.tsv"),
        "PM5" to (55 to "20-pm5-stability.tsv"),
        "NT" to (59 to "23-nt-stability.tsv"),
        "CR" to (59 to "24-cr-stability.tsv"),
        "LB" to (59 to "25-lb-stability.tsv"),
        "FK" to (114 to "30-fk-stability.tsv"),
    )

    /**
     * Cases logged in a table of their own, by id prefix, in place of their stratum's (RECORD -96, -98): FK kind 5, rebuilt on
     * `hiking-mountain`; `30-fk-stability.tsv` still holds its `car-fast` run.
     */
    val OWN_TABLES: Map<String, List<Pair<String, String>>> = mapOf("FK" to listOf("FK5-" to "33-fk5-cap1000-stability.tsv"))

    /** [stratum]'s logged R1, by id: its table from [STRATA], with the ids of each [OWN_TABLES] prefix read from that table. */
    fun loggedFor(stratum: String): Map<String, Pair<String, E3Outcome>> {
        var log = logged(STRATA.getValue(stratum).second)
        for ((prefix, table) in OWN_TABLES[stratum].orEmpty()) {
            val own = logged(table)
            check(own.isNotEmpty() && own.keys.all { it.startsWith(prefix) }) { "$table: not only $prefix ids" }
            log = log.filterKeys { !it.startsWith(prefix) } + own
        }
        return log
    }

    /** FK's extra cases (RECORD -80, -85), compared on outcome; the two that route are held to the intact control's tuple. */
    val OUTCOME_ONLY = setOf("FK7-x1", "FK7-x2", "FK8-x1")
    val SAME_AS_CONTROL = setOf("FK7-x2", "FK8-x1")
    const val CONTROL = "FK7-c0"

    class Row(val stratum: String, val id: String, val compare: String, val outcome: E3Outcome, val tupleDigest: String) {
        fun toLine() = listOf(
            stratum, id, compare, outcome.kind, outcome.nodes, outcome.cost, outcome.distance, outcome.nodeDigest,
            outcome.heightDigest, tupleDigest,
        ).joinToString("\t")
    }

    class Stored(val head: List<String>, val rows: List<Row>) {
        val jdk get() = head.single { it.startsWith("# jdk ") }.removePrefix("# jdk ")

        /** Each `# excluded <stratum> <id> <reason>` line, as (stratum, id) to reason. */
        val excluded: Map<Pair<String, String>, String>
            get() = head.filter { it.startsWith("# excluded ") }.associate {
                val f = it.removePrefix("# excluded ").split(' ', limit = 3)
                (f[0] to f[1]) to f[2]
            }
    }

    /** 64-bit FNV-1a over [s]'s UTF-8 bytes. */
    fun fnv1aText(s: String): String {
        var h = -0x340d631b7bdddcdbL // 0xcbf29ce484222325
        for (b in s.toByteArray(Charsets.UTF_8)) {
            h = h xor (b.toLong() and 0xff)
            h *= 0x100000001b3L
        }
        return java.lang.Long.toHexString(h)
    }

    fun row(stratum: String, id: String, compare: String, o: E3Outcome) = Row(stratum, id, compare, o, fnv1aText(o.toField()))

    fun write(head: List<String>, rows: List<Row>, file: File) {
        require(head.all { it.startsWith("# ") && '\n' !in it })
        file.writeText((head + COLUMNS + rows.map { it.toLine() }).joinToString("\n") + "\n")
    }

    fun read(text: String): Stored {
        val lines = text.lines().filter { it.isNotEmpty() }
        val head = lines.takeWhile { it.startsWith("# ") }
        check(lines[head.size] == COLUMNS) { "the stored file's column line is not $COLUMNS" }
        val rows = lines.drop(head.size + 1).map { line ->
            val f = line.split('\t')
            check(f.size == 10) { "a stored row has 10 fields, not ${f.size}: $line" }
            Row(f[0], f[1], f[2], E3Outcome(f[3], f[4].toInt(), f[5].toInt(), f[6].toInt(), f[7], f[8]), f[9])
        }
        return Stored(head, rows)
    }

    /** The committed table [name]: id to (verdict, R1's tuple). */
    fun logged(name: String): Map<String, Pair<String, E3Outcome>> {
        val f = File(LOGS, name)
        check(f.isFile) { "no committed stability table at ${f.absolutePath}" }
        val lines = f.readLines().filter { it.isNotBlank() }
        check(lines[0].split('\t').let { it[0] == "id" && it[1] == "verdict" && it[5] == "R1-tuple" }) { "$name: not a stability table" }
        return lines.drop(1).associate { l ->
            val c = l.split('\t')
            c[0] to (c[1] to E3Outcome.fromField(c[5]))
        }
    }
}
