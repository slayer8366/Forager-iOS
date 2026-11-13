package com.zynergylabs.forager.brouter.router

import java.lang.reflect.InvocationTargetException
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Step 7, session 23 (RECORD -111, "Port them"): `Formatter` whole, against the oracle's, method by method, over
 * seeded inputs: every command 0-20, 99-101 and seeded ints, roundabout exits around 0 and at the int limits, and
 * `timode` -1 to 10. The oracle's instance is a `FormatCsv` (its own `Formatter` constructors are package-private); the
 * port's is a test subclass, since `Format*` are ported after. `formatILon`/`formatILat` take seeded ints (no
 * position of Forager's), compared by digest and length all the same; times by text.
 */
class FormatterTest {
    private val oFormatter = Class.forName("btools.router.Formatter")
    private val oracle: Any = Class.forName("btools.router.FormatCsv")
        .getConstructor(btools.router.RoutingContext::class.java).newInstance(btools.router.RoutingContext())
    private val port = object : Formatter(RoutingContext(com.zynergylabs.forager.brouter.util.OracleLibm)) {
        override fun format(t: OsmTrack): String = ""
    }

    private fun res(f: () -> Any?): String = try {
        "ok:" + f()
    } catch (e: InvocationTargetException) {
        "throws " + e.targetException.javaClass.name + ": " + e.targetException.message
    } catch (e: Throwable) {
        "throws " + e.javaClass.name + ": " + e.message
    }

    private fun digest(s: String): String =
        java.security.MessageDigest.getInstance("SHA-256").digest(s.toByteArray()).joinToString("") { "%02x".format(it) }
            .take(16) + "/" + s.length

    private fun oCall(name: String, vararg a: Any): Any? {
        val types = a.map { if (it is Int) Int::class.javaPrimitiveType else Float::class.javaPrimitiveType }
        val m = oFormatter.getDeclaredMethod(name, *types.toTypedArray()).also { it.isAccessible = true }
        return m.invoke(oracle, *a)
    }

    @Test
    fun formatterMatchesTheOracle() {
        val rnd = Random(20261123L)
        val cmds = (-1..20).toList() + listOf(99, 100, 101, Int.MIN_VALUE, Int.MAX_VALUE) + List(8) { rnd.nextInt() }
        val exits = (-12..12).toList() + listOf(Int.MIN_VALUE, Int.MAX_VALUE, Int.MIN_VALUE + 1)
        val timodes = (-1..10).toList()
        var n = 0
        for (c in cmds) {
            for (t in timodes) {
                assertEquals(res { oCall("getJsonCommandIndex", c, t) }, res { port.getJsonCommandIndex(c, t) }, "getJsonCommandIndex($c, $t)")
                n++
            }
            for (x in exits) {
                for (t in timodes) {
                    assertEquals(res { oCall("getCommandString", c, x, t) }, res { port.getCommandString(c, x, t) }, "getCommandString($c, $x, $t)")
                    assertEquals(res { oCall("getCommandStringXX", c, x, t) }, res { port.getCommandStringXX(c, x, t) }, "getCommandStringXX($c, $x, $t)")
                    assertEquals(res { oCall("getSymbolString", c, x, t) }, res { port.getSymbolString(c, x, t) }, "getSymbolString($c, $x, $t)")
                    assertEquals(res { oCall("getMessageString", c, x, t) }, res { port.getMessageString(c, x, t) }, "getMessageString($c, $x, $t)")
                    n += 4
                }
                assertEquals(res { oCall("getLocusSymbolString", c, x) }, res { port.getLocusSymbolString(c, x) }, "getLocusSymbolString($c, $x)")
                assertEquals(res { oCall("getLocusAction", c, x) }, res { port.getLocusAction(c, x) }, "getLocusAction($c, $x)")
                assertEquals(res { oCall("getOruxAction", c, x) }, res { port.getOruxAction(c, x) }, "getOruxAction($c, $x)")
                assertEquals(res { oCall("getCruiserCommandString", c, x) }, res { port.getCruiserCommandString(c, x) }, "getCruiserCommandString($c, $x)")
                assertEquals(res { oCall("getCruiserMessageString", c, x) }, res { port.getCruiserMessageString(c, x) }, "getCruiserMessageString($c, $x)")
                n += 5
            }
        }
        // Positions: seeded ints across the whole range and near the offsets, by digest and length only (E1).
        val ints = listOf(0, 1, -1, 9, 10, 99999, 100000, 999999, 1000000, 90000000, 180000000, Int.MIN_VALUE, Int.MAX_VALUE,
            Int.MIN_VALUE + 180000000, Int.MIN_VALUE + 90000000, 180000000 - 1, 180000000 + 1, 90000000 - 1) +
            List(2000) { rnd.nextInt() } + List(2000) { rnd.nextInt(0, 360000001) }
        for ((k, i) in ints.withIndex()) {
            assertEquals(digest(res { oCall("formatILon", i) }), digest(res { Formatter.formatILon(i) }), "formatILon, int $k")
            assertEquals(digest(res { oCall("formatILat", i) }), digest(res { Formatter.formatILat(i) }), "formatILat, int $k")
            n += 2
        }
        // Times: seconds as floats, from 0 up, negative, across the Gregorian cutover and to the long limits.
        val times = listOf(0f, 0.0004f, 0.0005f, 0.001f, 1f, 59.999f, 86399.99f, 86400f, -0.001f, -1f, -86400f,
            1.0e9f, 1.7e9f, 4.1e9f, 2.5e11f, 2.6e11f, -12219292000f, -12219293000f, -12219294000f, -6.2e10f,
            -6.3e10f, -6.4e10f, -1.0e12f, 1.0e14f, 9.2e15f, 9.3e15f, -9.3e15f, 3.0e38f, -3.0e38f, Float.NaN,
            Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY) +
            List(1500) { rnd.nextFloat() * 100000f } + List(1500) { (rnd.nextDouble(-1.3e11, 2.6e11)).toFloat() } +
            List(300) { Float.fromBits(rnd.nextInt()) }
        for ((k, t) in times.withIndex()) {
            assertEquals(res { oCall("getFormattedTime3", t) }, res { Formatter.getFormattedTime3(t) }, "getFormattedTime3, time $k ($t)")
            n++
        }
        assertEquals(res { (oracle as btools.router.Formatter).read("x") }, res { port.read("x") }, "read")
        assertEquals(oFormatter.getDeclaredField("MESSAGES_HEADER").also { it.isAccessible = true }.get(null), Formatter.MESSAGES_HEADER)
        assertEquals(oFormatter.getDeclaredField("dateformat").also { it.isAccessible = true }.get(null), Formatter.dateformat)
        println("7 formatter: $n calls compared, ${ints.size} ints, ${times.size} times")
    }
}
