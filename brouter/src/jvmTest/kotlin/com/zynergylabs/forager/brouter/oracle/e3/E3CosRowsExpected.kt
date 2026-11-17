package com.zynergylabs.forager.brouter.oracle.e3

import java.io.File

/**
 * The cosine rows' expected values (session 76): a resource of their own, `e3/expected-cosrows.tsv`, beside
 * `expected-1b.tsv` and never appended to it, so 1b's file, its line count and its digest pins do not move.
 *
 * **Form:** [E3Expected]'s, unchanged: `# ` head lines (with the `# jdk ` line), the column line [E3Expected.COLUMNS], then
 * one row per case, `stratum id compare kind nodes cost distance nodeDigest heightsDigest tupleDigest`, written by
 * [E3Expected.write] and read by [E3Expected.read]. No coordinate is stored (E1). Every row is `exact`: the writer
 * refuses to write if a case was logged unstable.
 *
 * **Listed and held, by analogy with [E3Expected.STRATA]:** [STRATA] lists each row's stratum (`COS474` ... `COS1079`, the
 * stratum name and id prefix [CosRowsGenerator.stratum]) with its case count and the committed stability table its R1 was
 * logged in, under [LOGS]. [E3Expected.STRATA] is not changed, so the existing strata assert what they did.
 * A jvmTest class, added once the file is written, holds it to its row count, [STRATA]'s counts per id prefix, and a digest.
 */
object E3CosRowsExpected {
    const val RESOURCE = "/e3/expected-cosrows.tsv"

    /** Where the group's committed logs are, from the brouter project folder (Gradle's test working directory). */
    const val LOGS = "../docs/audits/2026-11-13-brouter-port-closing/s76"

    /** Each row's stratum, its case count and its committed stability table. */
    val STRATA: Map<String, Pair<Int, String>> = linkedMapOf<String, Pair<Int, String>>().also { m ->
        for (row in CosRowsGenerator.ROWS) m[CosRowsGenerator.stratum(row)] = 59 to "cos$row-stability.tsv"
    }

    /** [name]'s committed stability table under [LOGS]: id to (verdict, R1's tuple), as [E3Expected.logged] reads its own. */
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
