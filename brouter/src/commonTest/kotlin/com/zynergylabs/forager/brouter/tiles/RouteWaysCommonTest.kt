package com.zynergylabs.forager.brouter.tiles

import com.zynergylabs.forager.brouter.RouteWaysOutcome
import com.zynergylabs.forager.brouter.RouteWaysRequest
import com.zynergylabs.forager.brouter.mapaccess.RouteWaysCells.CELL
import com.zynergylabs.forager.brouter.mapaccess.RouteWaysCells.DIVISOR
import com.zynergylabs.forager.brouter.mapaccess.RouteWaysCells.F0H_ENDS
import com.zynergylabs.forager.brouter.mapaccess.RouteWaysCells.cells
import com.zynergylabs.forager.brouter.mapaccess.RouteWaysCells.squareFile
import com.zynergylabs.forager.brouter.readRouteWays
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.fail

/**
 * Session 69: `RouteWaysEntryTest`'s common sibling, so [readRouteWays] runs on the iOS simulator. On the F0h ends
 * (Forager's own route ends, [F0H_ENDS]; E1 allows them) and their cells ([cells], grouped by [squareFile]), one call per
 * square at divisor 32, each held to its [RouteWaysExpected] row: the outcome is `Read`, the divisor, the cells asked
 * (count and digest), the link count, the total point count and a digest over every link's description, iLat and iLon.
 * The rows are the port's own JVM output; `RouteWaysEntryTest` ties that output to the Java walk. Nothing but counts,
 * digests and the ends' names is printed or put in a message (E1).
 *
 * The heighted square's folder: on the JVM the property `brouter.tiles.f0h.dir` (as `TestTiles.f0h`); on the simulator,
 * where no variable names it, the eighth field of `f0h-cases.tsv` beside the NT file ([Step10Cases.ntFile]), the route
 * [F0hEngineCommonTest] takes to the same square. Design:
 * `docs/audits/2026-11-13-brouter-port-closing/s69-route-ways-common-sibling.md`.
 */
class RouteWaysCommonTest {
    @Test
    fun f0hEndsReadAsStored() {
        val folder = f0hFolder()
        val lookups = lookups()
        val points = F0H_ENDS.flatMap { (name, e) -> listOf("$name/from" to (e[0] to e[1]), "$name/to" to (e[2] to e[3])) }
        assertEquals(16, points.size, "F0h: end count")
        val got = mutableListOf<RouteWaysExpected.Row>()
        for ((name, at) in points) {
            val bySquare = cells(at.first, at.second).groupBy { (lc, ac) -> squareFile(lc * CELL, ac * CELL) }
            for ((s, entry) in bySquare.entries.withIndex()) {
                val id = "$name square ${s + 1} of ${bySquare.size}"
                val square = folder / entry.key
                if (!FileSystem.SYSTEM.exists(square)) fail("$id: its square does not exist or cannot be read in this process")
                val sc = entry.value
                val lon = IntArray(sc.size) { sc[it].first.toInt() }
                val lat = IntArray(sc.size) { sc[it].second.toInt() }
                val outcome = readRouteWays(RouteWaysRequest(square, lookups, lon, lat, DIVISOR))
                val read = outcome as? RouteWaysOutcome.Read ?: fail("$id: field [kind] is ${outcome::class.simpleName}")
                val text = StringBuilder()
                var total = 0
                for ((i, link) in read.links.withIndex()) {
                    if (link.iLat.size != link.iLon.size) fail("$id, link ${i + 1}: iLat and iLon differ in size")
                    total += link.iLat.size
                    text.append(link.description).append('\t').append(link.iLat.joinToString(",")).append('\t')
                        .append(link.iLon.joinToString(",")).append('\n')
                }
                got += RouteWaysExpected.Row(
                    id, read.divisor, lon.size, EngineRun.fnv1a(lon, lat), read.links.size, total, EngineRun.fnv1aText(text.toString()),
                )
            }
        }
        for (r in got) {
            println("s69 route ways row: Row(\"${r.id}\", ${r.divisor}, ${r.cells}, \"${r.cellsDigest}\", ${r.links}, ${r.points}, \"${r.digest}\"),")
        }
        println("s69 route ways common F0h: ${points.size} points, ${got.size} calls, links ${got.sumOf { it.links }}, points ${got.sumOf { it.points }}")

        val want = RouteWaysExpected.ROWS
        assertEquals(want.map { it.id }, got.map { it.id }, "F0h: the calls' ids, in order, against RouteWaysExpected")
        val mismatches = mutableListOf<String>()
        for ((w, g) in want.zip(got)) {
            listOfNotNull(
                "divisor ${w.divisor}/${g.divisor}".takeIf { w.divisor != g.divisor },
                "cells ${w.cells}/${g.cells}".takeIf { w.cells != g.cells },
                "cellsDigest".takeIf { w.cellsDigest != g.cellsDigest },
                "links ${w.links}/${g.links}".takeIf { w.links != g.links },
                "points ${w.points}/${g.points}".takeIf { w.points != g.points },
                "digest".takeIf { w.digest != g.digest },
            ).forEach { mismatches += "${w.id}: $it" }
        }
        assertEquals(emptyList(), mismatches, "F0h route ways against RouteWaysExpected (stored/got)")
    }

    @Test
    fun aSquareOfAnotherDivisorIsNotRead() {
        val (lat0, lon0) = F0H_ENDS.values.first().let { it[0] to it[1] }
        val (name, sc) = cells(lat0, lon0).groupBy { (lc, ac) -> squareFile(lc * CELL, ac * CELL) }.entries.first()
        val square = f0hFolder() / name
        val lon = IntArray(sc.size) { sc[it].first.toInt() }
        val lat = IntArray(sc.size) { sc[it].second.toInt() }
        val outcome = readRouteWays(RouteWaysRequest(square, lookups(), lon, lat, 80))
        val other = assertIs<RouteWaysOutcome.OtherDivisor>(outcome, "F0h square asked at 1/80: outcome")
        assertEquals(RouteWaysExpected.OTHER_DIVISOR, other.divisor, "F0h square asked at 1/80: divisor")
        assertFailsWith<IllegalArgumentException>("unpaired cells") {
            readRouteWays(RouteWaysRequest(square, lookups(), intArrayOf(1, 2), intArrayOf(1), DIVISOR))
        }
    }

    private fun lookups(): Path {
        val oracle = testSetting("brouter.oracle.dir", "BROUTER_ORACLE_DIR")
            ?: fail("${testSettingSource("brouter.oracle.dir", "BROUTER_ORACLE_DIR")} is not set")
        val p = oracle.toPath() / "profiles2" / "lookups.dat"
        if (!FileSystem.SYSTEM.exists(p)) fail("$p does not exist or cannot be read in this process")
        return p
    }

    private fun f0hFolder(): Path {
        val folder = if (onJvm) {
            // The iOS variable name is never read: this branch runs on the JVM only.
            (testSetting("brouter.tiles.f0h.dir", "-") ?: fail("system property brouter.tiles.f0h.dir is not set")).toPath() / "f0h"
        } else {
            val nt = Step10Cases.ntFile("route ways") ?: fail("no NT file on the simulator")
            val cases = nt.parent!! / "f0h-cases.tsv"
            if (!FileSystem.SYSTEM.exists(cases)) fail("$cases does not exist or cannot be read in this process")
            val folders = FileSystem.SYSTEM.read(cases) { readUtf8() }.lines().filter { it.isNotEmpty() }
                .map { it.split('\t') }.map { if (it.size == 8) it[7] else fail("a line of $cases has ${it.size} fields, not 8") }
                .distinct()
            (folders.singleOrNull() ?: fail("$cases names ${folders.size} tiles folders, not 1")).toPath()
        }
        if (!FileSystem.SYSTEM.exists(folder)) fail("$folder does not exist or cannot be read in this process")
        return folder
    }
}
