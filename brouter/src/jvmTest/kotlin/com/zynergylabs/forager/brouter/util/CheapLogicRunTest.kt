package com.zynergylabs.forager.brouter.util

import java.lang.management.ManagementFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Step 2b's logic run (Amendment 3 Part B, "The seam for the two kinds of run"; Phase 1 report, E6): the port with the
 * oracle's own table and atan2 injected (OracleLibm) equals the oracle bit for bit. Every corpus counts its inputs, and
 * calcAngle's counts each of its five branches, so a pass says which inputs it saw.
 */
class CheapLogicRunTest {
    @Test
    fun theJvmRunsWithLibmIntrinsicsOff() {
        val args = ManagementFactory.getRuntimeMXBean().inputArguments
        assertTrue("-XX:+UnlockDiagnosticVMOptions" in args && "-XX:-UseLibmIntrinsic" in args, "JVM arguments: $args")
        val hotspot = ManagementFactory.getPlatformMXBean(com.sun.management.HotSpotDiagnosticMXBean::class.java)
        assertEquals("false", hotspot.getVMOption("UseLibmIntrinsic").value, "UseLibmIntrinsic")
        // The behaviour, not only the setting: with the intrinsic off, Math.cos is fdlibm's at every table row (E6 [C]).
        // With it on, they differ at some rows (Phase 1 part 6: 60 on one HotSpot; this step's scratch run, 0-prediction.txt).
        val rows = (0 until 1800).filter { Math.cos(oracleLat(it)).toRawBits() != StrictMath.cos(oracleLat(it)).toRawBits() }
        assertEquals(emptyList(), rows, "rows where Math.cos is not StrictMath.cos")
        println("JVM: ${System.getProperty("java.vm.vendor")} ${System.getProperty("java.vm.version")} ${System.getProperty("os.arch")}; $args")
    }

    @Test
    fun portTableArithmeticGivesTheOraclesTableFromTheOraclesCos() {
        val port = CheapRuler.buildScaleTable { Math.cos(it) }
        assertEquals(1800, port.size)
        val bad = (0 until 1800).filter { row ->
            val o = OracleLibm.scales(row)
            port[row][0].toRawBits() != o[0].toRawBits() || port[row][1].toRawBits() != o[1].toRawBits()
        }
        assertEquals(emptyList(), bad, "rows where the port's table differs from the oracle's")
    }

    @Test
    fun distanceEqualsTheOraclesWithItsTable() {
        val r = Lcg(2026102902L)
        var n = 0
        var zero = 0
        val bad = ArrayList<String>()
        for (i in 0 until 200000) { // a fixed count, so it ends
            val ilon1 = r.between(0, 360000000)
            val ilat1 = r.between(0, 179999999)
            val (ilon2, ilat2) = when (i % 4) {
                0 -> r.between(0, 360000000) to r.between(0, 179999999) // anywhere
                1 -> (ilon1 + r.between(-5000, 5000)) to (ilat1 + r.between(-5000, 5000)).coerceIn(0, 179999999) // a segment
                2 -> (ilon1 + r.between(-500000, 500000)) to (ilat1 + r.between(-500000, 500000)).coerceIn(0, 179999999)
                else -> ilon1 to ilat1 // the same point
            }
            val got = CheapRuler.distance(OracleLibm, ilon1, ilat1, ilon2, ilat2)
            val want = btools.util.CheapRuler.distance(ilon1, ilat1, ilon2, ilat2)
            if (got.toRawBits() != want.toRawBits()) bad.add("$ilon1 $ilat1 $ilon2 $ilat2: ${bits(got)} vs ${bits(want)}")
            if (want == 0.0) zero++
            n++
        }
        assertEquals(200000, n)
        assertTrue(zero >= 50000, "zero distances: $zero")
        assertEquals(emptyList(), bad.take(20), "${bad.size} of $n distances differ")
    }

    @Test
    fun calcAngleEqualsTheOraclesWithItsTable() {
        val r = Lcg(2026102903L)
        val branches = IntArray(5) // 0: dd == 0; 1: s2 > 0.5, sinp > 0; 2: s2 > 0.5, else; 3: cosp < 0; 4: neither
        val bad = ArrayList<String>()
        var n = 0
        for (i in 0 until 200000) { // a fixed count, so it ends
            val reach = when (i % 4) { 0 -> 10; 1 -> 1000; 2 -> 100000; else -> 3000000 }
            val lon0 = r.between(10000000, 350000000)
            val lat0 = r.between(10000000, 170000000)
            val lon1 = if (i % 10 == 0) lon0 else lon0 + r.between(-reach, reach)
            val lat1 = if (i % 10 == 0) lat0 else lat0 + r.between(-reach, reach)
            val lon2 = lon1 + r.between(-reach, reach)
            val lat2 = lat1 + r.between(-reach, reach)
            val port = CheapAngleMeter(OracleLibm)
            val oracle = btools.util.CheapAngleMeter()
            val got = port.calcAngle(lon0, lat0, lon1, lat1, lon2, lat2)
            val want = oracle.calcAngle(lon0, lat0, lon1, lat1, lon2, lat2)
            if (got.toRawBits() != want.toRawBits() || port.cosAngle.toRawBits() != oracle.cosAngle.toRawBits()) {
                bad.add("$lon0 $lat0 $lon1 $lat1 $lon2 $lat2: ${bits(got)} ${bits(port.cosAngle)} vs ${bits(want)} ${bits(oracle.cosAngle)}")
            }
            branches[branchOf(lon0, lat0, lon1, lat1, lon2, lat2)]++
            n++
        }
        assertEquals(200000, n)
        println("calcAngle branches (dd==0, +90, -90, cosp<0, plain): ${branches.toList()}")
        assertTrue(branches.all { it >= 1000 }, "every branch reached at least 1,000 times: ${branches.toList()}")
        assertEquals(emptyList(), bad.take(20), "${bad.size} of $n angles differ")
    }

    @Test
    fun angleFunctionsEqualTheOracles() {
        var n = 0
        val bad = ArrayList<String>()
        fun same(what: String, got: Double, want: Double) {
            if (got.toRawBits() != want.toRawBits()) bad.add("$what: ${bits(got)} vs ${bits(want)}")
            n++
        }
        com.zynergylabs.forager.brouter.msun.forEachAtan2Pair { dy, dx ->
            for (k in 0 until 4) {
                val lon2 = PAIR_SIGNS[2 * k] * dy
                val lat2 = PAIR_SIGNS[2 * k + 1] * dx
                same("getAngle $lon2 $lat2", CheapAngleMeter.getAngle(OracleLibm, 0, 0, lon2, lat2), btools.util.CheapAngleMeter.getAngle(0, 0, lon2, lat2))
                same("getDirection $lon2 $lat2", CheapAngleMeter.getDirection(OracleLibm, 0, 0, lon2, lat2), btools.util.CheapAngleMeter.getDirection(0, 0, lon2, lat2))
            }
        }
        assertEquals(8 * 299974, n, "getAngle and getDirection evaluations")
        val edges = doubleArrayOf(
            0.0, -0.0, 359.99999999999994, 360.0, -360.0, 720.0, -720.5, 1e10, -1e10, 1e300, -1e300,
            Double.MIN_VALUE, -Double.MIN_VALUE, Double.MAX_VALUE, -Double.MAX_VALUE,
            Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 179.99999999999997, 180.0, -180.0,
        )
        val r = Lcg(2026102904L)
        val doubles = edges.toList() + List(20000) { (r.next().toDouble() - 1073741824.0) / 1000.0 }
        for (a in doubles) same("normalize $a", CheapAngleMeter.normalize(a), btools.util.CheapAngleMeter.normalize(a))
        for (i in 0 until 20000) { // a fixed count, so it ends
            val b1 = doubles[i % doubles.size]
            val b2 = doubles[(i * 7 + 3) % doubles.size]
            same("getDifferenceFromDirection $b1 $b2", CheapAngleMeter.getDifferenceFromDirection(b1, b2), btools.util.CheapAngleMeter.getDifferenceFromDirection(b1, b2))
        }
        for (b1 in edges) for (b2 in edges) {
            same("getDifferenceFromDirection $b1 $b2", CheapAngleMeter.getDifferenceFromDirection(b1, b2), btools.util.CheapAngleMeter.getDifferenceFromDirection(b1, b2))
        }
        assertEquals(8 * 299974 + doubles.size + 20000 + edges.size * edges.size, n, "evaluations")
        assertEquals(emptyList(), bad.take(20), "${bad.size} of $n differ")
    }

    /** Which of calcAngle's branches (CheapAngleMeter.java:14-48) these points take, worked from the oracle's own table. */
    private fun branchOf(lon0: Int, lat0: Int, lon1: Int, lat1: Int, lon2: Int, lat2: Int): Int {
        val s = OracleLibm.scales(lat1 / 100000)
        val dx10 = (lon1 - lon0) * s[0]; val dy10 = (lat1 - lat0) * s[1]
        val dx21 = (lon2 - lon1) * s[0]; val dy21 = (lat2 - lat1) * s[1]
        val dd = Math.sqrt((dx10 * dx10 + dy10 * dy10) * (dx21 * dx21 + dy21 * dy21))
        if (dd == 0.0) return 0
        val sinp = (dy10 * dx21 - dx10 * dy21) / dd
        val cosp = (dy10 * dy21 + dx10 * dx21) / dd
        return if (sinp * sinp > 0.5) (if (sinp > 0.0) 1 else 2) else if (cosp < 0.0) 3 else 4
    }
}
