package com.zynergylabs.forager.brouter.msun

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Step 2b, JVM side: the port row by row against this run's C build, and against the JVM's StrictMath (fdlibm) as the
 * research measured it (RECORD -23): `atan2` equal at all 299,974 pairs, `cos` different at exactly 13 table rows.
 */
class MsunAgainstCTest {
    @Test
    fun cosEqualsTheCBuildRowByRow() {
        val wrong = MsunCBuild.cosLines.mapNotNull { line ->
            val (row, lat, cos) = line.split(' ')
            val got = msunCos(fromHex(lat))
            if (hex(got) == cos) null else "row $row: port ${hex(got)} C $cos"
        }
        assertEquals(emptyList(), wrong, "${wrong.size} of ${MsunCBuild.cosLines.size} rows")
    }

    @Test
    fun cosDiffersFromStrictMathAtTheResearchsThirteenRows() {
        // research/android-math q7/results/rows-msun-vs-strict.txt.
        val research = listOf(474, 488, 502, 663, 675, 1046, 1053, 1079, 1295, 1297, 1310, 1311, 1318)
        val rows = (0 until 1800).filter { row ->
            val lat = cheapRulerLat(row)
            msunCos(lat).toRawBits() != StrictMath.cos(lat).toRawBits()
        }
        assertEquals(research, rows)
    }

    @Test
    fun atan2EqualsTheCBuildAndStrictMathPairByPair() {
        val c = MsunCBuild.atan2Lines
        assertEquals(299974, c.size, "C pairs")
        var i = 0
        val againstC = mutableListOf<String>()
        val againstStrict = mutableListOf<String>()
        forEachAtan2Pair { dy, dx ->
            val got = msunAtan2(dy.toDouble(), dx.toDouble())
            val (cy, cx, cBits) = c[i].split(' ')
            if (cy != "$dy" || cx != "$dx") error("pair $i: generated ($dy, $dx), the C's file has ($cy, $cx)")
            if (hex(got) != cBits) againstC += "pair $i ($dy, $dx): port ${hex(got)} C $cBits"
            val strict = StrictMath.atan2(dy.toDouble(), dx.toDouble())
            if (got.toRawBits() != strict.toRawBits()) againstStrict += "pair $i ($dy, $dx): port ${hex(got)} Strict ${hex(strict)}"
            i++
        }
        assertEquals(299974, i, "pairs regenerated")
        assertEquals(emptyList(), againstC.take(20), "${againstC.size} pairs differ from the C build")
        assertEquals(emptyList(), againstStrict.take(20), "${againstStrict.size} pairs differ from StrictMath.atan2")
    }
}
