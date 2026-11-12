package com.zynergylabs.forager.brouter.tiles

import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import kotlin.test.assertEquals
import kotlin.test.fail
import kotlin.time.TimeSource

/**
 * Step 10 (RECORD -138, decision 2): one group's cases, read from a file `NtCasesWriteTest` (jvmTest) wrote at run time
 * on a JVM, routed through the engine ([EngineRun], `MsunLibm`) and held to the group's stored rows on the six fields and
 * the stored tuple digest. Session 46 wrote this inside [NtEngineCommonTest]; session 47 moved it here, unchanged but for
 * the group's name, its rows and the profiles folder, so LB and CR run the same check.
 *
 * The NT file is named by [VARIABLE]; the other groups' files lie beside it ([NtCasesWriteTest]'s names). On iOS the
 * `ios-smoke` job exports `SIMCTL_CHILD_`[VARIABLE]; unset there, the test fails. On the JVM unset (the `jvm` job), it
 * prints one line and does not run.
 *
 * E1: the file holds ends; nothing of a case but its id is printed or put in a message. Mismatches name the id and the
 * field, with the tuple digests only.
 *
 * Session 48, for F0h and FK: a line may carry an eighth field, the tiles folder of that case (F0h's heighted square, a
 * folder of FK 7's or 8's edited copies, written by the JVM step); a seven-field line uses `<tiles dir>/segments`. [check]
 * also takes how kinds are read ([family]; FK 7's numbers as `*`), whether the tuple digest is held ([holdTuple]; not for
 * FK 7, whose kind is held by family), and [onCase], which sees each case's id and the port's outcome.
 */
internal object Step10Cases {
    const val VARIABLE = "BROUTER_STEP10_NT_CASES"

    /** The NT file's path, or null on the JVM when [VARIABLE] is unset (after printing why). */
    fun ntFile(stratum: String): Path? {
        val file = testEnvironment(VARIABLE)
        if (file.isNullOrEmpty()) {
            if (onJvm) {
                println("step10 $stratum engine: $VARIABLE is unset; not run")
                return null
            }
            fail("environment variable $VARIABLE is not set in the simulator's process (ios-smoke exports SIMCTL_CHILD_$VARIABLE)")
        }
        return file.toPath()
    }

    /** [stratum]'s cases in [path] against [rows]; profiles from [ownProfiles], else the oracle's `profiles2`. */
    fun check(
        stratum: String,
        path: Path,
        rows: List<NtExpected.Row>,
        ownProfiles: Path? = null,
        family: (String) -> String = { it },
        holdTuple: Boolean = true,
        onCase: ((String, EngineRun.Outcome) -> Unit)? = null,
    ) {
        val segments = setting("brouter.tiles.dir", "BROUTER_TILES_DIR").toPath() / "segments"
        val profiles = ownProfiles ?: (setting("brouter.oracle.dir", "BROUTER_ORACLE_DIR").toPath() / "profiles2")
        for (p in listOf(path, segments, profiles / "lookups.dat")) {
            if (!FileSystem.SYSTEM.exists(p)) fail("$p does not exist or cannot be read in this process")
        }
        val stored = rows.associateBy { it.id }
        val lines = FileSystem.SYSTEM.read(path) { readUtf8() }.lines().filter { it.isNotEmpty() }
        val seen = LinkedHashSet<String>()
        val notStored = ArrayList<String>()
        val mismatches = ArrayList<String>()
        var equal = 0
        val t0 = TimeSource.Monotonic.markNow()
        for (line in lines) {
            val f = line.split('\t')
            if (f.size != 7 && f.size != 8) fail("a line of $path has ${f.size} fields, not 7 or 8 (id ${f[0]})")
            val id = f[0]
            val row = stored[id]
            if (row == null) {
                notStored += id
                continue
            }
            if (!seen.add(id)) fail("$id appears twice in $path")
            val profile = profiles / "${f[1]}.brf"
            if (!FileSystem.SYSTEM.exists(profile)) fail("$id: $profile does not exist")
            val tiles = if (f.size == 8) f[7].toPath() else segments
            if (!FileSystem.SYSTEM.exists(tiles)) fail("$id: its tiles folder does not exist or cannot be read in this process")
            val got = EngineRun.run("${f[3]},${f[4]}|${f[5]},${f[6]}", profile, tiles, f[2].toInt())
            onCase?.invoke(id, got)
            val w = row.outcome
            val differ = listOfNotNull(
                "kind".takeIf { family(got.kind) != family(w.kind) },
                "nodes".takeIf { got.nodes != w.nodes },
                "cost".takeIf { got.cost != w.cost },
                "distance".takeIf { got.distance != w.distance },
                "nodeDigest".takeIf { got.nodeDigest != w.nodeDigest },
                "heightsDigest".takeIf { got.heightsDigest != w.heightsDigest },
                "tupleDigest".takeIf { holdTuple && got.tupleDigest != row.tupleDigest },
            )
            if (differ.isEmpty()) equal++ else mismatches += "$id $differ (tuple port ${got.tupleDigest}, stored ${row.tupleDigest})"
        }
        val missing = stored.keys - seen
        println(
            "step10 $stratum engine: ${lines.size} cases in the file; ${seen.size} of ${stored.size} stored ids; ${notStored.size} not stored " +
                "${notStored.take(20)}; equal on ${if (holdTuple) "all six fields and the tuple digest" else "the kind's family and the five track fields"} " +
                "${equal}; mismatches ${mismatches.size}; " +
                "missing ${missing.size}; ${t0.elapsedNow().inWholeSeconds} s",
        )
        mismatches.forEach { println("step10 $stratum engine mismatch $it") }
        assertEquals(emptyList(), mismatches, "$stratum through the engine against expected-1b.tsv (id, fields, tuple digests)")
        assertEquals(emptySet(), missing, "$stratum's stored ids the run-time file does not name")
    }

    private fun setting(jvmProperty: String, iosVariable: String): String =
        testSetting(jvmProperty, iosVariable) ?: fail("${testSettingSource(jvmProperty, iosVariable)} is not set")
}
