package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.expressions.ExpressionsEngines
import okio.FileSystem
import java.lang.reflect.Modifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import btools.router.ProfileCache as OracleProfileCache
import btools.router.RoutingContext as OracleContext

/**
 * Item I, step 6 (RECORD -92): `RoutingContext` read from each profile. Both engines' contexts are filled by their own
 * `ProfileCache.parseProfile` from Forager's parameters (the oracle's `RoutingParamCollector`, as
 * [ExpressionsEngines.foragerRoutingContext] sets them up). Then every public field of the oracle's `RoutingContext`
 * typed boolean, int, long, double, float, Integer or String is compared with the port's field of the same name.
 *
 * The six car profiles name `KinematicModel` or `KinematicNoCostModel` (`---model:`), outside step 6's row. Forager
 * routes with `hiking-mountain.brf`. The port refuses those six with a named "unsupported", and this test asserts that.
 */
class RoutingContextReadTest {
    private val compared = setOf(
        java.lang.Boolean.TYPE, Integer.TYPE, java.lang.Long.TYPE, java.lang.Double.TYPE, java.lang.Float.TYPE,
        Integer::class.java, String::class.java,
    )

    private fun oracleFields() = OracleContext::class.java.declaredFields
        .filter { Modifier.isPublic(it.modifiers) && !Modifier.isStatic(it.modifiers) && it.type in compared }

    @Test
    fun everyProfileReadsTheSameRoutingContext() {
        val fields = oracleFields()
        assertEquals(76, fields.size, "oracle fields to compare (76 in 1.7.10)")
        val kinematic = ArrayList<String>()
        ExpressionsEngines.forEveryProfile { profile ->
            val orc = ExpressionsEngines.foragerRoutingContext(profile)
            OracleProfileCache.parseProfile(orc)
            OracleProfileCache.releaseProfile(orc)
            val model = orc.pm.javaClass.name
            val prc = RoutingContext()
            prc.localFunction = orc.localFunction
            prc.keyValues = orc.keyValues
            prc.memoryclass = orc.memoryclass
            if (model != "btools.router.StdModel") {
                val e = runCatching { ProfileCache.parseProfile(prc, FileSystem.SYSTEM) }.exceptionOrNull()
                assertEquals("unsupported: path model $model", (e as? UnsupportedOperationException)?.message, "$e")
                kinematic += profile.name
                ProfileCache.releaseProfile(prc)
                return@forEveryProfile
            }
            ProfileCache.parseProfile(prc, FileSystem.SYSTEM)
            ProfileCache.releaseProfile(prc)
            assertTrue(prc.pm is StdModel, "pm is ${prc.pm}")
            assertEquals(orc.profileName, prc.getProfileName(), "getProfileName")
            val diffs = ArrayList<String>()
            for (f in fields) {
                val o = f.get(orc)
                val p = runCatching {
                    RoutingContext::class.java.getDeclaredField(f.name).apply { isAccessible = true }.get(prc)
                }.getOrElse { "<missing: $it>" }
                if (o != p) diffs += "${f.name}: oracle $o, port $p"
            }
            assertTrue(diffs.isEmpty(), "${diffs.size} fields differ: $diffs")
        }
        assertEquals(6, kinematic.size, "profiles refused as Kinematic: $kinematic")
    }
}
