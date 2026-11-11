package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.expressions.ExpressionsEngines
import com.zynergylabs.forager.brouter.oracle.e3.strata.E3F0hTest
import java.lang.reflect.Modifier
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import btools.router.OsmNodeNamed as OracleNamed
import btools.router.RoutingContext as OracleContext
import btools.router.RoutingParamCollector as OracleCollector

/**
 * Step 6: `RoutingParamCollector` against the oracle's.
 *
 * - `getWayPointList` on Forager's own form (`"lon,lat|lon,lat"`, Kotlin's `Double.toString`, as
 *   `BRouterTrailRouter.buildEngine`) for F0h's 16 ends, then on seeded text: 1 to 5 points, both separators, the
 *   `d`/`m`/name third field, exponents, signs, whitespace, trailing and empty pieces, and broken input. Each
 *   `OsmNodeNamed` is compared by Java's field names; a throw is compared by class and message.
 * - `setParams` with Forager's three keys on every profile, every public primitive, `Integer` and `String` field of
 *   the context compared by name (as `RoutingContextReadTest`), and `keyValues`; then keys Java ignores, `alternativeidx`
 *   values, and null or empty params.
 * - Each key Java acts on that the port refuses: the oracle is shown to change something with it, and the port to
 *   throw "unsupported" and leave its context as it was. Two `profile:` keys likewise.
 */
class RoutingParamCollectorTest {
    private val compared = setOf(
        java.lang.Boolean.TYPE, Integer.TYPE, java.lang.Long.TYPE, java.lang.Double.TYPE, java.lang.Float.TYPE,
        Integer::class.java, String::class.java,
    )
    private val ctxFields = OracleContext::class.java.declaredFields
        .filter { Modifier.isPublic(it.modifiers) && !Modifier.isStatic(it.modifiers) && it.type in compared }
    private val nodeFields = (OracleNamed::class.java.declaredFields.toList() + btools.mapaccess.OsmNode::class.java.getDeclaredField("ilon") +
        btools.mapaccess.OsmNode::class.java.getDeclaredField("ilat"))
        .filter { !Modifier.isStatic(it.modifiers) }.onEach { it.isAccessible = true }

    private fun portField(cls: Class<*>, name: String): java.lang.reflect.Field {
        var c: Class<*>? = cls
        while (c != null) {
            runCatching { return c!!.getDeclaredField(name).apply { isAccessible = true } }
            c = c.superclass
        }
        error("port ${cls.simpleName} has no field $name")
    }

    private fun nodeText(o: Any, isOracle: Boolean): String = nodeFields.joinToString(" ") { f ->
        "${f.name}=" + (if (isOracle) f.get(o) else portField(OsmNodeNamed::class.java, f.name).get(o))
    }

    private fun outcome(isOracle: Boolean, s: String?): String = runCatching {
        if (isOracle) {
            OracleCollector().getWayPointList(s).joinToString(" | ") { nodeText(it, true) }
        } else {
            RoutingParamCollector().getWayPointList(s).joinToString(" | ") { nodeText(it, false) }
        }
    }.getOrElse { "throws ${it.javaClass.name}: ${it.message}" }

    private fun lonLatText(rnd: Random): String {
        val lon = rnd.nextDouble(-180.0, 180.0)
        val lat = rnd.nextDouble(-90.0, 90.0)
        fun num(v: Double): String = when (rnd.nextInt(8)) {
            0 -> "%.3f".format(java.util.Locale.ROOT, v)
            1 -> "%.6e".format(java.util.Locale.ROOT, v)
            2 -> " $v"
            3 -> if (v >= 0) "+$v" else "$v"
            4 -> listOf("abc", "", "1e400", "NaN", "-0.0", "0x1p3", "8.7d", "1_0").random(rnd)
            else -> v.toString()
        }
        val third = when (rnd.nextInt(6)) { 0 -> ",d"; 1 -> ",m"; 2 -> ",stop${rnd.nextInt(9)}"; 3 -> ",via7"; else -> "" }
        return when (rnd.nextInt(30)) {
            0 -> num(lon) // no comma
            1 -> ""
            else -> "${num(lon)},${num(lat)}$third" + if (rnd.nextInt(20) == 0) ",x,y" else ""
        }
    }

    @Test
    fun wayPointListMatchesTheOracle() {
        var forager = 0
        for ((_, e) in E3F0hTest.ENDS) {
            val s = "${e[1]},${e[0]}|${e[3]},${e[2]}" // lon,lat|lon,lat, as BRouterTrailRouter.kt
            val o = outcome(true, s)
            assertEquals(o, outcome(false, s), "Forager's form, an F0h case")
            assertTrue(!o.startsWith("throws") && o.contains("name=from") && o.contains("name=to"), "Forager's form refused: $o")
            forager++
        }
        val rnd = Random(20261111L)
        val kinds = HashMap<String, Int>()
        var cases = 0
        for (s in listOf(null, "", "|", ";;", "8.7,49.4|", "|8.7,49.4", "8.7,49.4||8.71,49.41", "8.7,49.4;8.71,49.41,d")) {
            assertEquals(outcome(true, s), outcome(false, s), "fixed case \"$s\"")
            cases++
        }
        repeat(4000) { i ->
            val n = 1 + rnd.nextInt(5)
            val sb = StringBuilder()
            repeat(n) { k -> if (k > 0) sb.append(if (rnd.nextBoolean()) "|" else ";"); sb.append(lonLatText(rnd)) }
            if (rnd.nextInt(15) == 0) sb.append(if (rnd.nextBoolean()) "|" else ";")
            val s = sb.toString()
            val o = outcome(true, s)
            assertEquals(o, outcome(false, s), "case $i \"$s\"")
            val kind = if (o.startsWith("throws")) o.substringBefore(":") else "list"
            kinds.merge(kind, 1, Int::plus)
            for (t in listOf("wpttype=3", "wpttype=2", "name=stop")) if (o.contains(t)) kinds.merge(t, 1, Int::plus)
            cases++
        }
        println("RoutingParamCollectorTest: $forager Forager-form ends, $cases other cases; ${kinds.toSortedMap()}")
        for (k in listOf("list", "wpttype=3", "wpttype=2", "name=stop", "throws java.lang.NumberFormatException",
            "throws java.lang.IllegalArgumentException", "throws java.lang.ArrayIndexOutOfBoundsException")) {
            assertTrue((kinds[k] ?: 0) > 0, "no case of $k: $kinds")
        }
    }

    /** The oracle's waypoints, and the port's copied from them, so that `setParams` is checked apart from `getWayPointList`. */
    private fun wps(): Pair<List<OracleNamed>, List<OsmNodeNamed>> {
        val o = OracleCollector().getWayPointList("8.7,49.4|8.71,49.41")
        return o to o.map { n -> OsmNodeNamed().also { it.ilon = n.ilon; it.ilat = n.ilat; it.name = n.name; it.wpttype = n.wpttype } }
    }

    /** Every compared field by name, `keyValues`, the nogo and poi lists' sizes and the waypoints' types. */
    private fun oracleState(rc: OracleContext, w: List<OracleNamed>): List<String> =
        ctxFields.map { "${it.name}=${it.get(rc)}" } + "keyValues=${rc.keyValues}" + "nogopoints=${rc.nogopoints?.size}" +
            "poipoints=${rc.poipoints?.size}" + "wpttypes=${w.map { it.wpttype }}"

    private fun portState(rc: RoutingContext, w: List<OsmNodeNamed>): List<String> =
        ctxFields.map { "${it.name}=${portField(RoutingContext::class.java, it.name).get(rc)}" } + "keyValues=${rc.keyValues}" +
            "nogopoints=${rc.nogopoints?.size}" + "poipoints=${rc.poipoints?.size}" + "wpttypes=${w.map { it.wpttype }}"

    private fun bothSet(params: Map<String, String>?, profileTo: (OracleContext, RoutingContext) -> Unit = { _, _ -> }): Pair<String, String> {
        val (ow, pw) = wps()
        val orc = OracleContext()
        val prc = RoutingContext()
        profileTo(orc, prc)
        val o = runCatching { OracleCollector().setParams(orc, ow, params?.let { HashMap(it) }); oracleState(orc, ow).joinToString("\n") }
            .getOrElse { "throws ${it.javaClass.name}: ${it.message}" }
        val p = runCatching { RoutingParamCollector().setParams(prc, pw, params?.let { HashMap(it) }); portState(prc, pw).joinToString("\n") }
            .getOrElse { "throws ${it.javaClass.name}: ${it.message}" }
        return o to p
    }

    @Test
    fun setParamsMatchesTheOracleOnForagersKeys() {
        assertEquals(76, ctxFields.size, "oracle fields to compare (76 in 1.7.10)")
        var profiles = 0
        ExpressionsEngines.forEveryProfile { profile ->
            for (udr in listOf("0", "1")) {
                val params = mapOf("profile" to profile.absolutePath, "alternativeidx" to "0", "profile:use_dynamic_range" to udr)
                val (o, p) = bothSet(params)
                assertEquals(o, p, "${profile.name}, use_dynamic_range $udr")
                assertTrue(o.contains("localFunction=${profile.absolutePath}") && o.contains("keyValues={use_dynamic_range=$udr}"), o)
            }
            profiles++
        }
        val others = listOf(
            null, emptyMap(), mapOf("alternativeidx" to "3"), mapOf("alternativeidx" to "-2"), mapOf("alternativeidx" to "+1"),
            mapOf("alternativeidx" to "x"), mapOf("alternativeidx" to ""), mapOf("lonlats" to "1,2|3,4", "foo" to "bar"),
            mapOf("nogoLats" to "", "nogoLons" to "1", "nogoRadi" to "5"), mapOf("profile" to "p.brf"), mapOf("profile:" to "v"),
            mapOf("profile:a" to "1", "profile" to "q"),
        )
        for (params in others) {
            val (o, p) = bothSet(params)
            assertEquals(o, p, "params $params")
        }
        // An existing keyValues with the same key is replaced, as Java's put does.
        val (o, p) = bothSet(mapOf("profile:a" to "2")) { orc, prc -> orc.keyValues = hashMapOf("a" to "1"); prc.keyValues = mapOf("a" to "1") }
        assertEquals(o, p, "an existing keyValues")
        println("RoutingParamCollectorTest: $profiles profiles x 2 on Forager's keys, ${others.size + 1} other params")
    }

    @Test
    fun keysForagerNeverPassesAreRefusedAsUnsupported() {
        val refused = linkedMapOf(
            "nogoLats" to mapOf("nogoLats" to "49.4", "nogoLons" to "8.7", "nogoRadi" to "50"),
            "nogos" to mapOf("nogos" to "8.7,49.4,50"),
            "polylines" to mapOf("polylines" to "8.7,49.4,8.71,49.41"),
            "polygons" to mapOf("polygons" to "8.7,49.4,8.71,49.41,8.72,49.4"),
            "straight" to mapOf("straight" to "1"),
            "pois" to mapOf("pois" to "8.7,49.4,cafe"),
            "heading" to mapOf("heading" to "90"),
            "direction" to mapOf("direction" to "90"),
            "roundTripDistance" to mapOf("roundTripDistance" to "5000"),
            "roundTripDirectionAdd" to mapOf("roundTripDirectionAdd" to "10"),
            "roundTripPoints" to mapOf("roundTripPoints" to "7"),
            "allowSamewayback" to mapOf("allowSamewayback" to "1"),
            "turnInstructionMode" to mapOf("turnInstructionMode" to "2"),
            "timode" to mapOf("timode" to "2"),
            "turnInstructionFormat" to mapOf("turnInstructionFormat" to "osmand"),
            "exportWaypoints" to mapOf("exportWaypoints" to "1"),
            "exportCorrectedWaypoints" to mapOf("exportCorrectedWaypoints" to "1"),
            "format" to mapOf("format" to "KML"),
            "trackFormat" to mapOf("trackFormat" to "JSON"),
        )
        val (fo, fp) = bothSet(emptyMap())
        assertEquals(fo, fp, "the fresh contexts")
        for ((key, params) in refused) {
            val (o, p) = bothSet(params)
            assertTrue(!o.startsWith("throws"), "$key: the oracle threw: $o")
            assertNotEquals(fo, o, "$key: the oracle changed nothing with it, so the refusal shows nothing")
            assertEquals("throws java.lang.UnsupportedOperationException: unsupported: RoutingParamCollector.setParams key \"$key\"", p, key)
        }
        val (o2, p2) = bothSet(mapOf("profile:a" to "1", "profile:b" to "2"))
        assertTrue(o2.contains("keyValues={") && !o2.startsWith("throws"), o2)
        assertTrue(p2.startsWith("throws java.lang.UnsupportedOperationException: unsupported: RoutingParamCollector.setParams with more than one profile: key"), p2)
        // A refused call leaves the port's context as it was.
        val prc = RoutingContext()
        val before = portState(prc, emptyList())
        runCatching { RoutingParamCollector().setParams(prc, emptyList(), mapOf("profile" to "p", "alternativeidx" to "2", "heading" to "1")) }
        assertEquals(before, portState(prc, emptyList()), "the port's context after a refusal")
        println("RoutingParamCollectorTest: ${refused.size} refused keys, each changing the oracle's context")
    }
}
