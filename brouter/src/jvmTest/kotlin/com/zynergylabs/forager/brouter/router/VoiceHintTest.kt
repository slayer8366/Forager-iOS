package com.zynergylabs.forager.brouter.router

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Step 7, session 20: `VoiceHint` whole and `VoiceHintList` against the oracle's, on synthetic hint inputs built from
 * seeds (RECORD -111). Positions stay zero (E1): a hint's `ilon`, `ilat` and `selev` are never set. Java's fields are
 * package private, so the oracle's are set and read by reflection, by Java's own names; every field of the port's is
 * compared after each step, message objects by their index in the case's list, floats and doubles by bits.
 */
class VoiceHintTest {
    private val oHint = Class.forName("btools.router.VoiceHint")
    private val oMsg = Class.forName("btools.router.MessageData")
    private val hintFields = oHint.declaredFields.filter { !java.lang.reflect.Modifier.isStatic(it.modifiers) }
        .onEach { it.isAccessible = true }

    private fun m(c: Class<*>, name: String, vararg types: Class<*>) =
        c.getDeclaredMethod(name, *types).also { it.isAccessible = true }

    private fun setO(o: Any, name: String, v: Any?) {
        o.javaClass.getDeclaredField(name).also { it.isAccessible = true }.set(o, v)
    }

    private fun portField(p: Any, name: String): Any? =
        p.javaClass.getDeclaredField(name).also { it.isAccessible = true }.get(p)

    /** A call's result, or the class of what it threw, so that both engines' exceptions compare as values. */
    private fun res(f: () -> Any?): String = try {
        val v = f()
        if (v is Float) "f${v.toRawBits()}" else "v$v"
    } catch (e: java.lang.reflect.InvocationTargetException) {
        "throws ${e.targetException.javaClass.simpleName}"
    } catch (e: Throwable) {
        "throws ${e.javaClass.simpleName}"
    }

    private val boundaries = listOf(
        -200, -181, -180, -179, -178, -160, -159, -158, -136, -135, -134, -111, -110, -109, -101, -100, -99, -96, -95,
        -94, -86, -85, -84, -61, -60, -59, -56, -55, -54, -46, -45, -44, -36, -35, -34, -31, -30, -29, -22, -21, -20,
        -11, -10, -9, -6, -5, -4, 0, 4, 5, 6, 9, 10, 11, 20, 21, 22, 29, 30, 31, 34, 35, 36, 44, 45, 46, 54, 55, 56,
        59, 60, 61, 84, 85, 86, 94, 95, 96, 99, 100, 101, 109, 110, 111, 134, 135, 136, 158, 159, 160, 178, 179, 180,
        181, 200,
    ).map { it.toFloat() } + listOf(-179.5f, 179.5f, -0.5f, 0.5f, 110.5f, 59.5f)

    private fun angle(rnd: Random): Float = when (rnd.nextInt(8)) {
        0, 1, 2 -> boundaries[rnd.nextInt(boundaries.size)]
        3 -> boundaries[rnd.nextInt(boundaries.size)] + (rnd.nextInt(3) - 1) * Math.ulp(1f) * 64
        4 -> Float.fromBits(rnd.nextInt())
        else -> rnd.nextDouble(-200.0, 200.0).toFloat()
    }

    private val keyValues = listOf(
        null, "", "highway=give_way", "highway=stop", "highway=give_way direction=backward",
        "highway=stop direction=backward", "highway=traffic_signals", "reversedirection=yes highway=residential",
        "highway=residential", "reversedirection=yes",
    )

    /** One message on each engine with the same seeded values. */
    private fun msg(rnd: Random): Pair<Any, MessageData> {
        val o = oMsg.getDeclaredConstructor().also { it.isAccessible = true }.newInstance()
        val p = MessageData()
        val turn = angle(rnd)
        val prio = if (rnd.nextInt(5) == 0) rnd.nextInt() else rnd.nextInt(-2, 40)
        val mask = rnd.nextInt(32)
        val way = keyValues[rnd.nextInt(keyValues.size)]
        val node = keyValues[rnd.nextInt(keyValues.size)]
        val time = if (rnd.nextBoolean()) Float.fromBits(rnd.nextInt()) else rnd.nextInt(0, 5000).toFloat()
        setO(o, "turnangle", turn); p.turnangle = turn
        setO(o, "priorityclassifier", prio); p.priorityclassifier = prio
        setO(o, "classifiermask", mask); p.classifiermask = mask
        setO(o, "wayKeyValues", way); p.wayKeyValues = way
        setO(o, "nodeKeyValues", node); p.nodeKeyValues = node
        setO(o, "time", time); p.time = time
        return o to p
    }

    private fun view(v: Any?, msgs: List<Pair<Any, MessageData>>, port: Boolean): Any? = when {
        v == null -> null
        v is Float -> "f${v.toRawBits()}"
        v is Double -> "d${v.toRawBits()}"
        v is List<*> -> v.map { view(it, msgs, port) }
        v.javaClass == oMsg || v is MessageData ->
            "msg#" + msgs.indexOfFirst { (if (port) it.second else it.first) === v }
        else -> v
    }

    private fun compareFields(o: Any, p: Any, msgs: List<Pair<Any, MessageData>>, id: String) {
        for (f in hintFields) {
            assertEquals(view(f.get(o), msgs, false), view(portField(p, f.name), msgs, true), "$id: ${f.name}")
        }
    }

    @Test
    fun voiceHintMatchesTheOracle() {
        assertEquals(16, hintFields.size, "VoiceHint's field count is not Java's")
        for (c in oHint.declaredFields.filter { java.lang.reflect.Modifier.isStatic(it.modifiers) }) {
            c.isAccessible = true
            val pc = VoiceHint::class.java.getDeclaredField(c.name).also { it.isAccessible = true }
            assertEquals(c.get(null), pc.get(null), "VoiceHint.${c.name}")
        }
        val rnd = Random(20261111L)
        val getTime = m(oHint, "getTime")
        val isRoundabout = m(oHint, "isRoundabout")
        val addBadWay = m(oHint, "addBadWay", oMsg)
        val getExitNumber = m(oHint, "getExitNumber")
        val calcCommand = m(oHint, "calcCommand")
        val formatGeometry = m(oHint, "formatGeometry")
        val hasGiveWay = m(oHint, "hasGiveWay")
        val is180 = m(oHint, "is180DegAngle", Float::class.javaPrimitiveType!!)
        val cmds = HashMap<Int, Int>()
        var giveWays = 0
        var badWayCount = 0
        var thrown = 0
        val presets = listOf(0, VoiceHint.C, VoiceHint.KR, VoiceHint.BL, VoiceHint.END)
        repeat(30000) { i ->
            val id = "case $i"
            val o = oHint.getDeclaredConstructor().also { it.isAccessible = true }.newInstance()
            val p = VoiceHint()
            val msgs = ArrayList<Pair<Any, MessageData>>()
            fun newMsg(): Pair<Any, MessageData> = msg(rnd).also { msgs += it }
            if (i > 0) { // the first case is the defaults
                val good = newMsg()
                setO(o, "goodWay", good.first); p.goodWay = good.second
                if (rnd.nextInt(4) != 0) {
                    val old = if (rnd.nextInt(3) == 0) good else newMsg()
                    setO(o, "oldWay", old.first); p.oldWay = old.second
                }
                val cmd = if (rnd.nextInt(3) == 0) rnd.nextInt(0, 20) else presets[rnd.nextInt(presets.size)]
                setO(o, "cmd", cmd); p.cmd = cmd
                if (rnd.nextBoolean()) {
                    val a = angle(rnd)
                    setO(o, "angle", a); p.angle = a
                }
                if (rnd.nextInt(10) < 3) {
                    val r = rnd.nextInt(-5, 6)
                    setO(o, "roundaboutExit", r); p.roundaboutExit = r
                }
                if (rnd.nextInt(8) == 0) {
                    val lo = angle(rnd); val hi = angle(rnd)
                    setO(o, "lowerBadWayAngle", lo); p.lowerBadWayAngle = lo
                    setO(o, "higherBadWayAngle", hi); p.higherBadWayAngle = hi
                }
                val idx = rnd.nextInt(0, 5000)
                val dist = rnd.nextDouble(0.0, 3000.0)
                val consumed = rnd.nextBoolean(); val real = rnd.nextBoolean(); val prio = rnd.nextInt(-1, 40)
                setO(o, "indexInTrack", idx); p.indexInTrack = idx
                setO(o, "distanceToNext", dist); p.distanceToNext = dist
                setO(o, "turnAngleConsumed", consumed); p.turnAngleConsumed = consumed
                setO(o, "needsRealTurn", real); p.needsRealTurn = real
                setO(o, "maxBadPrio", prio); p.maxBadPrio = prio
                repeat(rnd.nextInt(0, 5)) {
                    if (rnd.nextInt(6) == 0) {
                        addBadWay.invoke(o, null); p.addBadWay(null)
                    } else {
                        val b = newMsg()
                        addBadWay.invoke(o, b.first); p.addBadWay(b.second)
                        badWayCount++
                    }
                }
            }
            compareFields(o, p, msgs, "$id after set-up")
            assertEquals(res { getTime.invoke(o) }, res { p.getTime() }, "$id: getTime")
            assertEquals(res { isRoundabout.invoke(o) }, res { p.isRoundabout() }, "$id: isRoundabout")
            assertEquals(res { getExitNumber.invoke(o) }, res { p.getExitNumber() }, "$id: getExitNumber")
            val og = res { formatGeometry.invoke(o) }
            assertEquals(og, res { p.formatGeometry() }, "$id: formatGeometry")
            val gw = res { hasGiveWay.invoke(o) }
            assertEquals(gw, res { p.hasGiveWay() }, "$id: hasGiveWay")
            if (gw == "vtrue") giveWays++
            val oc = res { calcCommand.invoke(o) }
            assertEquals(oc, res { p.calcCommand() }, "$id: calcCommand")
            if (oc.startsWith("throws")) thrown++
            compareFields(o, p, msgs, "$id after calcCommand")
            cmds.merge(p.cmd, 1, Int::plus)
            val a = if (i % 2 == 0) angle(rnd) else boundaries[i % boundaries.size]
            assertEquals(is180.invoke(null, a), VoiceHint.is180DegAngle(a), "$id: is180DegAngle($a)")
        }
        println("7 voice hint: 30000 cases, $badWayCount bad ways, $giveWays give-ways, $thrown thrown, commands ${cmds.toSortedMap()}")
        val made = setOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 13, 14, 15, 16)
        assertTrue(cmds.keys.containsAll(made), "the sample did not reach every command calcCommand makes: ${cmds.keys}")
        assertTrue(giveWays > 0, "no give-way reached")
    }

    @Test
    fun voiceHintListMatchesTheOracle() {
        val oList = btools.router.VoiceHintList::class.java
        val setInt = m(oList, "setTransportMode", Int::class.javaPrimitiveType!!)
        val setBool = m(oList, "setTransportMode", Boolean::class.javaPrimitiveType!!, Boolean::class.javaPrimitiveType!!)
        val rnd = Random(20261112L)
        fun state(o: btools.router.VoiceHintList, p: VoiceHintList, id: String) {
            assertEquals(o.transportMode, p.getTransportMode(), "$id: getTransportMode")
            assertEquals(o.transportMode(), p.transportMode(), "$id: transportMode()")
            assertEquals(o.locusRouteType, p.getLocusRouteType(), "$id: getLocusRouteType")
            assertEquals(TrackEngines.field(o, "transportMode"), TrackEngines.field(p, "transportMode"), "$id: the field")
            assertEquals(TrackEngines.field(o, "turnInstructionMode"), p.turnInstructionMode, "$id: turnInstructionMode")
            assertEquals((TrackEngines.field(o, "list") as List<*>).size, p.list.size, "$id: list")
        }
        state(btools.router.VoiceHintList(), VoiceHintList(), "fresh")
        val modes = (-2..5).toList() + List(40) { rnd.nextInt() }
        for (mode in modes) {
            val o = btools.router.VoiceHintList()
            val p = VoiceHintList()
            setInt.invoke(o, mode)
            p.setTransportMode(mode)
            state(o, p, "mode $mode")
        }
        for (car in listOf(false, true)) for (bike in listOf(false, true)) {
            val o = btools.router.VoiceHintList()
            val p = VoiceHintList()
            setBool.invoke(o, car, bike)
            p.setTransportMode(car, bike)
            state(o, p, "car $car bike $bike")
        }
    }
}
