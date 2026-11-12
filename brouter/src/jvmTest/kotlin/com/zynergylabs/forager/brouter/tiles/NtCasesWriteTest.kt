package com.zynergylabs.forager.brouter.tiles

import com.zynergylabs.forager.brouter.oracle.e3.E3Case
import com.zynergylabs.forager.brouter.oracle.e3.E3Expected
import com.zynergylabs.forager.brouter.oracle.e3.NTGenerator
import com.zynergylabs.forager.brouter.oracle.e3.strata.E3Targeted
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

/**
 * Step 10, session 46 (RECORD -138, decision 2): NT's cases for the simulator, drawn at run time on this JVM and written
 * to the file [GATE] names, never committed. Gated: with [GATE] unset it prints one line and does nothing (the `jvm` job).
 * `ios-smoke` sets it to `brouter/build/step10/nt-cases.tsv`, outside `test-results/`, which is all that job uploads.
 *
 * The draws are `RoutingEngineStep9Test`'s for NT: [NTGenerator] on [TestTiles.segments] with `E3NTTest`'s seed 20261205,
 * [E3Targeted.PROFILE], in draw order, kept where a draw's id is one of NT's rows in expected-1b.tsv, within
 * [E3Targeted.CAP] draws. No oracle run. Each line: id, profile, memory class, then the four ends as this JVM's
 * `Double.toString` writes them into the waypoint text (`port`'s `"$startLon,$startLat|$endLon,$endLat"`): start lon,
 * start lat, end lon, end lat. Nothing of a case but its id is printed; ids, counts and the path only (E1). A draw that
 * names a stored id but not this file's shape (another tiles folder, a time limit, a plan) fails with its id.
 */
class NtCasesWriteTest {
    @Test
    fun ntCasesAreWrittenForTheSimulator() {
        val path = System.getenv(GATE)
        if (path.isNullOrEmpty()) {
            println("step10 NT cases: $GATE is unset; not written")
            return
        }
        val rows = E3Expected.read(javaClass.getResource(E3Expected.RESOURCE)!!.readText()).rows
            .filter { it.stratum == "NT" }.associateBy { it.id }
        assertEquals(59, rows.size, "NT's rows in expected-1b")
        val lines = ArrayList<String>()
        val seen = HashSet<String>()
        var n = 0
        val gen = NTGenerator(TestTiles.segments, 20261205L)
        try {
            val draws = gen.draws("NT", E3Targeted.PROFILE)
            while (seen.size < rows.size && n < E3Targeted.CAP) {
                n++
                val c = draws.next().case ?: continue
                if (c.id !in rows) continue
                seen += c.id
                if (c.segments != TestTiles.segments || c.maxRunningTimeMillis != 0L || c.plan != E3Case.Plan.NONE) {
                    fail("${c.id}: not a plain case on the segments folder; the file cannot carry it")
                }
                lines += listOf(
                    c.id, c.profile, c.memoryclass.toString(),
                    c.startLon.toString(), c.startLat.toString(), c.endLon.toString(), c.endLat.toString(),
                ).joinToString("\t")
            }
        } finally {
            gen.close()
        }
        val out = File(path)
        out.parentFile.mkdirs()
        out.writeText(lines.joinToString("\n", postfix = "\n"))
        val missing = rows.keys - seen
        println("step10 NT cases: ${seen.size} of ${rows.size} stored ids drawn within $n draws; written to $path; missing ${missing.sorted()}")
    }

    companion object {
        const val GATE = "BROUTER_STEP10_NT_CASES"
    }
}
