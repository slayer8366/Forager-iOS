package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.mapaccess.OsmNode
import com.zynergylabs.forager.brouter.util.OracleLibm
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Step 6: `MessageData` and `OsmPathElement` against the oracle's, on seeded values. Java's `MessageData` is package
 * private, so the oracle's is made and read by reflection, by Java's own field names; every field of the port's is
 * compared after `copy`, so a field `copy` drops shows by name. The port's distance runs on [OracleLibm] (step 2b).
 */
class PathElementTest {
    private val oracleClass = Class.forName("btools.router.MessageData")
    private val fields = oracleClass.declaredFields.filter { !java.lang.reflect.Modifier.isStatic(it.modifiers) }
        .onEach { it.isAccessible = true }

    private fun newOracle(): Any = oracleClass.getDeclaredConstructor().also { it.isAccessible = true }.newInstance()

    private fun portValue(d: MessageData, name: String): Any? =
        MessageData::class.java.getDeclaredField(name).also { it.isAccessible = true }.get(d)

    /** Every field of Java's set to the same seeded value on both, so that the two can be compared field by field. */
    private fun fill(rnd: Random, o: Any, p: MessageData) {
        for (f in fields) {
            val v: Any? = when (f.type) {
                Int::class.javaPrimitiveType -> rnd.nextInt()
                Float::class.javaPrimitiveType -> Float.fromBits(rnd.nextInt())
                Short::class.javaPrimitiveType -> rnd.nextInt().toShort()
                String::class.java -> if (rnd.nextInt(4) == 0) null else "k${rnd.nextInt(1000)}"
                else -> error("MessageData.${f.name}: a field type the test does not know, ${f.type}")
            }
            f.set(o, v)
            MessageData::class.java.getDeclaredField(f.name).also { it.isAccessible = true }.set(p, v)
        }
    }

    private fun bits(v: Any?): Any? = if (v is Float) v.toRawBits() else v

    @Test
    fun messageDataMatchesTheOracle() {
        assertEquals(22, fields.size, "MessageData's field count is not Java's")
        val rnd = Random(20261106L)
        val copyM = oracleClass.getDeclaredMethod("copy").also { it.isAccessible = true }
        val prioM = oracleClass.getDeclaredMethod("getPrio").also { it.isAccessible = true }
        val masks = listOf("isBadOneway", "isRoundabout", "isLinktType", "isGoodForCars").map { n ->
            oracleClass.getDeclaredMethod(n).also { it.isAccessible = true }
        }
        repeat(2000) { i ->
            val o = newOracle()
            val p = MessageData()
            if (i > 0) fill(rnd, o, p) // the first case is the defaults
            for (f in fields) assertEquals(bits(f.get(o)), bits(portValue(p, f.name)), "case $i: ${f.name}")
            val oc = copyM.invoke(o)
            val pc = p.copy()
            for (f in fields) assertEquals(bits(f.get(oc)), bits(portValue(pc, f.name)), "case $i: copy's ${f.name}")
            assertEquals(o.toString(), p.toString(), "case $i: toString")
            assertEquals(prioM.invoke(o), p.getPrio(), "case $i: getPrio")
            val pm = listOf(p.isBadOneway(), p.isRoundabout(), p.isLinktType(), p.isGoodForCars())
            assertEquals(masks.map { it.invoke(o) }, pm, "case $i: masks")
        }
    }

    @Test
    fun osmPathElementMatchesTheOracle() {
        val rnd = Random(20261107L)
        var lastO: btools.router.OsmPathElement? = null
        var lastP: OsmPathElement? = null
        repeat(3000) { i ->
            val ilon = rnd.nextInt(0, 360_000_000)
            val ilat = rnd.nextInt(0, 180_000_000)
            val selev = rnd.nextInt().toShort()
            val o = btools.router.OsmPathElement.create(ilon, ilat, selev, lastO)
            val p = OsmPathElement.create(ilon, ilat, selev, lastP)
            assertEquals(lastO?.toString(), p.origin?.toString(), "case $i: origin")
            assertEquals(listOf(o.iLon, o.iLat, o.sElev, o.elev.toRawBits(), o.idFromPos, o.toString(), o.time.toRawBits(), o.energy.toRawBits()),
                listOf(p.getILon(), p.getILat(), p.getSElev(), p.getElev().toRawBits(), p.getIdFromPos(), p.toString(), p.getTime().toRawBits(), p.getEnergy().toRawBits()),
                "case $i: getters")
            // A nearby point and a far one, so calcDistance's max(1.0, ...) and its rounding are both reached.
            val dx = if (i % 2 == 0) rnd.nextInt(-3, 4) else rnd.nextInt(-200_000, 200_000)
            val q = btools.mapaccess.OsmNode(ilon + dx, (ilat + dx).coerceIn(0, 180_000_000))
            assertEquals(o.calcDistance(q), p.calcDistance(OracleLibm, OsmNode(q.ilon, q.ilat)), "case $i: calcDistance")
            if (lastO != null) assertEquals(o.positionEquals(lastO), p.positionEquals(lastP!!), "case $i: positionEquals")
            assertEquals(true, p.positionEquals(OsmPathElement.create(ilon, ilat, 0, null)), "case $i: positionEquals self")
            val s = rnd.nextInt().toShort()
            o.setSElev(s)
            p.setSElev(s)
            assertEquals(o.sElev, p.getSElev(), "case $i: setSElev")
            // With a message: time and energy read and written through it.
            if (i % 3 == 0) {
                val om = newOracle()
                val pm = MessageData()
                btools.router.OsmPathElement::class.java.getField("message").set(o, om)
                p.message = pm
                val t = Float.fromBits(rnd.nextInt())
                val e = Float.fromBits(rnd.nextInt())
                o.setTime(t); o.setEnergy(e)
                p.setTime(t); p.setEnergy(e)
                assertEquals(o.time.toRawBits() to o.energy.toRawBits(), p.getTime().toRawBits() to p.getEnergy().toRawBits(), "case $i: time and energy")
            } else {
                o.setTime(1f); p.setTime(1f)
                assertEquals(o.time.toRawBits(), p.getTime().toRawBits(), "case $i: setTime without a message")
            }
            lastO = o
            lastP = p
        }
    }
}
