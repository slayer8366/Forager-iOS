package com.zynergylabs.forager.brouter.oracle.e3.strata

import com.zynergylabs.forager.brouter.oracle.e3.DLGenerator
import com.zynergylabs.forager.brouter.oracle.e3.E3Select
import com.zynergylabs.forager.brouter.tiles.TestTiles
import kotlin.math.floor
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Strata DL-lat and DL-lon (Phase 1 report, E4): ends on opposite sides of 40°N (`W75_N35`, `W75_N40`) and of 70°W
 * (`W75_N40`, `W70_N40`), on `hiking-mountain` (RECORD -51). Reach, read from R1's route (R2 equal to it, RECORD -52):
 * "the found route has nodes on both sides", a node on the line counting in the tile above or east of it, as the tiles
 * hold it. The seeds are fixed here and pushed before any run of them (RECORD -47).
 */
class E3DLTest {
    @Test
    fun dlLatIsSelectedByReachAndCountedAgainstE3sStop() = run(DLGenerator.Line.LAT40, SEED_LAT)

    @Test
    fun dlLonIsSelectedByReachAndCountedAgainstE3sStop() = run(DLGenerator.Line.LON70, SEED_LON)

    private fun run(line: DLGenerator.Line, seed: Long) {
        val stratum = line.stratum
        E3Strata.jdk(stratum)
        println("E3 $stratum seed: $seed; profile ${E3Targeted.PROFILE}")
        val gen = DLGenerator(TestTiles.segments, line, seed)
        val selection = try {
            println("E3 $stratum nodes near the line: ${gen.nodeCountsNearTheLine}")
            val raw = gen.draws(E3Targeted.PROFILE, TestTiles.segments)
            // Before routing: every case's ends lie in the two tiles, one in each, by the tile names their degrees give,
            // read independently of the generator's own cell test.
            val checked = object : Iterator<E3Select.Draw> {
                override fun hasNext() = raw.hasNext()
                override fun next(): E3Select.Draw = raw.next().also { d ->
                    val c = d.case ?: return@also
                    val ends = listOf(tileName(c.startLat, c.startLon), tileName(c.endLat, c.endLon))
                    assertTrue(
                        ends[0] != ends[1] && ends.toSet() == setOf(line.below, line.above),
                        "${c.id}: ends in $ends, not one in each of ${line.below} and ${line.above}",
                    )
                }
            }
            E3Targeted.select(stratum, checked, listOf(stratum)) { r -> reach(line, stratum, r) }
        } finally {
            gen.close()
        }
        E3Targeted.report(selection, mapOf(stratum to E3Targeted.stability(stratum, selection)))
    }

    private fun reach(line: DLGenerator.Line, stratum: String, r: E3Select.Run): E3Select.Mark {
        val e = r.r1Extent ?: return E3Select.Mark(null, "no route: ${r.r1.kind}")
        val (min, max) = if (line.byLatitude) e[2] to e[3] else e[0] to e[1]
        val at = line.degree * 1_000_000 // the line in the engine's micro-degrees
        return if (min < at && at <= max) E3Select.Mark(stratum, "nodes on both sides") else E3Select.Mark(null, "route on one side")
    }

    /** The 5° tile holding a point, named as brouter.de names it: its south-west corner, e.g. `W75_N40.rd5`. */
    private fun tileName(lat: Double, lon: Double): String {
        val lon0 = floor(lon / 5).toInt() * 5
        val lat0 = floor(lat / 5).toInt() * 5
        return "${if (lon0 < 0) "W" else "E"}${kotlin.math.abs(lon0)}_${if (lat0 < 0) "S" else "N"}${kotlin.math.abs(lat0)}.rd5"
    }

    private companion object {
        const val SEED_LAT = 20261201L
        const val SEED_LON = 20261202L
    }
}
