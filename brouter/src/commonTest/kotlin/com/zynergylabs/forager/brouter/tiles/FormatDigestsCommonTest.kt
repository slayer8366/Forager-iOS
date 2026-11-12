package com.zynergylabs.forager.brouter.tiles

import com.zynergylabs.forager.brouter.router.OsmTrack
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

/**
 * Step 10, session 57 (RECORD -150, decision 2): the turn hints and the four formats on the platform the test runs on,
 * held to [FormatDigests]' constants, which were made from the port's production run on the JVM. F0's 2 cases are
 * [F0EngineCommonTest]'s; F0h's 8 and NT's 59 are read from the run-time files [Step10Cases] names (never committed), so
 * on the JVM with the variable unset only F0 runs, and on iOS `ios-smoke` exports it. Each case runs as [EngineRun.runTrack]
 * runs it (`MsunLibm`) with its stored row's mode; per output its length and FNV-1a digest must equal the row's. A case
 * whose id has no stored row is counted, not failed; every stored row must be met.
 *
 * Per group it prints cases, and equal per output; a difference prints the id, the output, both lengths and digests,
 * never text (E1). On the simulator a difference with no plant applied is RECORD -121's platform difference.
 */
class FormatDigestsCommonTest {
    private class Case(val id: String, val ends: String, val profile: Path, val tiles: Path, val memoryclass: Int)

    @Test
    fun hintsAndFormatsEqualTheStoredDigestsOfThePortsProductionRun() {
        val version = OsmTrack.version
        OsmTrack.version = FormatDigests.ORACLE_VERSION
        try {
            val oracle = setting("brouter.oracle.dir", "BROUTER_ORACLE_DIR").toPath() / "profiles2"
            val tilesDir = setting("brouter.tiles.dir", "BROUTER_TILES_DIR").toPath()
            val problems = ArrayList<String>()
            val f0 = F0EngineCommonTest.CASES.map {
                Case(it.id, "${it.startLon},${it.startLat}|${it.endLon},${it.endLat}", oracle / "hiking-mountain.brf", tilesDir / "f0", 64)
            }
            check("F0", f0, FormatDigests.F0, problems)
            val nt = Step10Cases.ntFile("hints-formats")
            if (nt != null) {
                check("F0h", fileCases(nt.parent!! / "f0h-cases.tsv", oracle, tilesDir / "segments"), FormatDigests.F0H, problems)
                check("NT", fileCases(nt, oracle, tilesDir / "segments"), FormatDigests.NT, problems)
            }
            problems.take(40).forEach { println("step10 hints-formats stored problem $it") }
            assertEquals(0, problems.size, "hints and formats against the stored digests (${problems.size} differences; first: ${problems.firstOrNull()})")
        } finally {
            OsmTrack.version = version
        }
    }

    private fun check(group: String, cases: List<Case>, rows: List<FormatOutputs.Row>, problems: MutableList<String>) {
        val stored = rows.associateBy { it.id }
        val equal = IntArray(5)
        val seen = HashSet<String>()
        var notStored = 0
        for (c in cases) {
            val row = stored[c.id]
            if (row == null) {
                notStored++
                continue
            }
            if (!seen.add(c.id)) fail("$group: ${c.id} appears twice")
            val r = EngineRun.runTrack(c.ends, c.profile, c.tiles, c.memoryclass, 0L, row.mode)
            val t = r.track
            val rc = r.rc
            val texts = if (t != null && rc != null) FormatOutputs.texts(t, rc) else List(5) { "no track" }
            for (k in 0 until 5) {
                val d = EngineRun.fnv1aText(texts[k])
                if (texts[k].length == row.lengths[k] && d == row.digests[k]) equal[k]++
                else problems += "$group ${c.id} mode ${row.mode} ${FormatOutputs.OUTPUTS[k]}: here ${texts[k].length}/$d stored ${row.lengths[k]}/${row.digests[k]}"
            }
        }
        val missing = stored.keys - seen
        if (missing.isNotEmpty()) problems += "$group: ${missing.size} stored rows not met"
        println(
            "step10 hints-formats stored $group: ${seen.size} of ${rows.size} stored rows run, $notStored not stored; " +
                FormatOutputs.OUTPUTS.indices.joinToString("; ") { "${FormatOutputs.OUTPUTS[it]} equal ${equal[it]}" },
        )
    }

    /** The cases of a run-time file (`Step10Cases.check`'s lines: id, profile, memoryclass, four ends, optional tiles). */
    private fun fileCases(path: Path, profiles: Path, segments: Path): List<Case> {
        if (!FileSystem.SYSTEM.exists(path)) fail("$path does not exist or cannot be read in this process")
        return FileSystem.SYSTEM.read(path) { readUtf8() }.lines().filter { it.isNotEmpty() }.map { line ->
            val f = line.split('\t')
            if (f.size != 7 && f.size != 8) fail("a line of $path has ${f.size} fields, not 7 or 8 (id ${f[0]})")
            Case(f[0], "${f[3]},${f[4]}|${f[5]},${f[6]}", profiles / "${f[1]}.brf", if (f.size == 8) f[7].toPath() else segments, f[2].toInt())
        }
    }

    private fun setting(jvmProperty: String, iosVariable: String): String =
        testSetting(jvmProperty, iosVariable) ?: fail("${testSettingSource(jvmProperty, iosVariable)} is not set")
}
