package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.oracle.OracleCalls
import com.zynergylabs.forager.brouter.oracle.e3.CosRowsGenerator
import com.zynergylabs.forager.brouter.oracle.e3.E3Case
import com.zynergylabs.forager.brouter.oracle.e3.E3CosRowsExpected
import com.zynergylabs.forager.brouter.oracle.e3.E3Expected
import com.zynergylabs.forager.brouter.oracle.e3.E3Outcome
import com.zynergylabs.forager.brouter.oracle.e3.strata.E3Targeted
import com.zynergylabs.forager.brouter.tiles.TestTiles
import com.zynergylabs.forager.brouter.util.LibmSeam
import com.zynergylabs.forager.brouter.util.MsunLibm
import com.zynergylabs.forager.brouter.util.OracleLibm
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import java.io.File
import kotlin.test.assertEquals

/**
 * The cosine rows' group, rebuilt for its runs (session 77; s72 §4.5 "Jobs"). Shared by the logic run
 * ([RoutingEngineCosRowsTest]), the production run ([RoutingEngineCosRowsProductionRecordTest]) and the simulator's case
 * writer (`NtCasesWriteTest`), so all three run the same cases.
 *
 * **Cases:** per row of [CosRowsGenerator.ROWS], [CosRowsGenerator.draws] with stratum `COS<row>` and
 * [E3Targeted.PROFILE], in draw order, taken where a draw's id is one of that stratum's rows in `expected-cosrows.tsv`,
 * within [E3Targeted.CAP] draws: as `RoutingEngineStep9Test.run` takes LB's from `expected-1b.tsv`. The logic and
 * production runs also run the oracle in their JVM and hold it to each row's tuple digest, which is what shows a rebuilt
 * case is the selected one.
 *
 * **[port]** is `RoutingEngineF0Test.port`'s calls with [libm] in the [RoutingContext], so a counting seam can be passed.
 * Nothing of a case but its id is printed (E1).
 */
internal object CosRowsRun {
    /** `expected-cosrows.tsv`'s rows, file order. */
    val rows: List<E3Expected.Row> by lazy {
        E3Expected.read(CosRowsRun::class.java.getResource(E3CosRowsExpected.RESOURCE)!!.readText()).rows
    }

    /** The table's (c) rows, as [CosRowsGenerator.cRows] computes them: msun's `kx` or `ky` not the oracle's, bit for bit. */
    val cRows: List<Int> by lazy {
        (0 until 1800).filter { r ->
            val p = MsunLibm.scales(r)
            val o = OracleLibm.scales(r)
            p[0].toRawBits() != o[0].toRawBits() || p[1].toRawBits() != o[1].toRawBits()
        }
    }

    /** The table row of a stratum `COS<row>`. */
    fun rowOf(stratum: String): Int = stratum.removePrefix("COS").toInt()

    /** Each row's cases, in [CosRowsGenerator.ROWS] order, each list in draw order; every id of the file is there. */
    fun cases(): Map<Int, List<E3Case>> {
        val out = LinkedHashMap<Int, List<E3Case>>()
        CosRowsGenerator(TestTiles.cosRows).use { gen ->
            for (row in CosRowsGenerator.ROWS) {
                val stratum = CosRowsGenerator.stratum(row)
                val ids = rows.filter { it.stratum == stratum }.map { it.id }.toSet()
                assertEquals(59, ids.size, "$stratum's rows in expected-cosrows")
                val draws = gen.draws(row, stratum, E3Targeted.PROFILE)
                val cases = ArrayList<E3Case>()
                var n = 0
                while (cases.size < ids.size && n < E3Targeted.CAP) {
                    n++
                    val c = draws.next().case ?: continue
                    if (c.id in ids) cases += c
                }
                assertEquals(ids, cases.map { it.id }.toSet(), "$stratum's cases are expected-cosrows' rows, within $n draws")
                out[row] = cases
            }
        }
        return out
    }

    /** `RoutingEngineF0Test.port` with [libm] in the context. An Exception is an outcome; an Error is not caught. */
    fun port(c: E3Case, libm: LibmSeam, base: File = OracleCalls.oracleDir): E3Outcome = try {
        val collector = RoutingParamCollector()
        val waypoints = collector.getWayPointList("${c.startLon},${c.startLat}|${c.endLon},${c.endLat}")
        val rc = RoutingContext(libm)
        val params = mapOf(
            "profile" to File(base, "profiles2/${c.profile}.brf").absolutePath,
            "alternativeidx" to "0",
            "profile:use_dynamic_range" to "0",
        )
        collector.setParams(rc, waypoints, params)
        rc.memoryclass = c.memoryclass
        val engine = RoutingEngine(FileSystem.SYSTEM, c.segments.toOkioPath(), waypoints, rc, RoutingEngine.BROUTER_ENGINEMODE_ROUTING)
        engine.quite = true
        engine.doRun(c.maxRunningTimeMillis)
        val message = engine.getErrorMessage()
        val track = engine.getFoundTrack()
        val pairs = track.nodes.map { it.getILon() to it.getILat() }
        E3Outcome(
            E3Outcome.kindOf(message), pairs.size, track.cost, track.distance, E3Outcome.fnv1a(pairs),
            E3Outcome.fnv1aShorts(track.nodes.map { it.getSElev() }),
        )
    } catch (e: Exception) {
        E3Outcome("threw:" + (e.message ?: e::class.simpleName), 0, -1, -1, E3Outcome.fnv1a(emptyList()), E3Outcome.fnv1aShorts(emptyList()))
    }

    /**
     * The two row assertions (s72 §4.5), on [read] (case id to the rows its counted port run read), each failure naming its
     * row or ids; empty when both hold. Per case: its own row (its id's prefix) is among the rows read. Per group: the rows
     * read by all cases, intersected with [cRows], are exactly [CosRowsGenerator.ROWS]. [label] names the table in prints.
     */
    fun rowAssertion(label: String, read: Map<String, Set<Int>>): List<String> {
        val failures = ArrayList<String>()
        for (row in CosRowsGenerator.ROWS) {
            val stratum = CosRowsGenerator.stratum(row)
            val ids = read.keys.filter { it.substringBefore('-') == stratum }
            val notOwn = ids.filter { row !in read.getValue(it) }
            if (notOwn.isNotEmpty()) failures += "row $row: cases that did not read row $row on $label: $notOwn"
            val seen = ids.flatMap { read.getValue(it) }.filter { it in cRows }.toSortedSet()
            println("cosrows $label $stratum: ${ids.size} cases; own row read by ${ids.size - notOwn.size}; (c) rows read $seen")
        }
        val group = read.values.flatten().filter { it in cRows }.toSortedSet()
        println("cosrows $label group: (c) rows read by all cases $group")
        if (group != CosRowsGenerator.ROWS.toSortedSet()) {
            failures += "the group's (c) rows read on $label are $group, not ${CosRowsGenerator.ROWS}: " +
                "missing ${CosRowsGenerator.ROWS - group}, extra ${group - CosRowsGenerator.ROWS.toSet()}"
        }
        return failures
    }

    /** The six fields of E3Outcome on which [a] and [b] differ, by the names the brief and Step 9 use. */
    fun differing(a: E3Outcome, b: E3Outcome): List<String> = listOfNotNull(
        "kind".takeIf { a.kind != b.kind },
        "nodes".takeIf { a.nodes != b.nodes },
        "cost".takeIf { a.cost != b.cost },
        "distance".takeIf { a.distance != b.distance },
        "nodeDigest".takeIf { a.nodeDigest != b.nodeDigest },
        "heightsDigest".takeIf { a.heightDigest != b.heightDigest },
    )
}
