package com.zynergylabs.forager.brouter.expressions

import btools.router.RoutingContext
import btools.router.RoutingParamCollector
import com.zynergylabs.forager.brouter.oracle.OracleCalls
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import java.io.File
import btools.expressions.BExpressionContext as OracleContext
import btools.expressions.BExpressionContextNode as OracleNode
import btools.expressions.BExpressionContextWay as OracleWay
import btools.expressions.BExpressionMetaData as OracleMeta

/**
 * Item I, step 4: both engines' expression contexts, built as `ProfileCache.java:88-95` builds them (the twelfth
 * session's handoff, item 5): the way context with hash size `memoryclass * 512`, the node context with 0,
 * `node.setForeignContext(way)`, then `readMetaData`, then `parseFile(profile, "global", keyValues)` for each.
 * `memoryclass` and `keyValues` are read from the oracle's own `RoutingContext` after Forager's calls set it up.
 */
internal object ExpressionsEngines {
    val profilesDir = File(OracleCalls.oracleDir, "profiles2")
    val lookups = File(profilesDir, "lookups.dat")

    /** The oracle zip's profiles (RECORD -39: 26; E's crossing profile does not exist yet). */
    val profiles: List<File> = profilesDir.listFiles { f -> f.name.endsWith(".brf") }!!.sortedBy { it.name }

    /**
     * The oracle's `RoutingContext` after `RoutingParamCollector.setParams` with Forager's parameters, as
     * `OracleCalls.route` passes them (`BRouterTrailRouter.kt:146-178` at Forager e2969abe4), for [profile].
     */
    fun foragerRoutingContext(profile: File): RoutingContext {
        val collector = RoutingParamCollector()
        val waypoints = collector.getWayPointList("8.7,49.4|8.71,49.41")
        val rc = RoutingContext()
        val params = hashMapOf(
            "profile" to profile.absolutePath,
            "alternativeidx" to "0",
            "profile:use_dynamic_range" to "0",
        )
        collector.setParams(rc, waypoints, params)
        return rc
    }

    class Oracle(val meta: OracleMeta, val way: OracleWay, val node: OracleNode)

    class Port(val meta: BExpressionMetaData, val way: BExpressionContextWay, val node: BExpressionContextNode)

    /** The oracle's contexts, built and parsed in `ProfileCache`'s order. */
    fun oracle(profile: File, rc: RoutingContext = foragerRoutingContext(profile)): Oracle {
        val meta = OracleMeta()
        val way = OracleWay(rc.memoryclass * 512, meta)
        val node = OracleNode(0, meta)
        node.setForeignContext(way)
        meta.readMetaData(lookups)
        way.parseFile(profile, "global", rc.keyValues)
        node.parseFile(profile, "global", rc.keyValues)
        return Oracle(meta, way, node)
    }

    /** The port's contexts, built and parsed in the same order, through the okio seam. */
    fun port(profile: File, rc: RoutingContext = foragerRoutingContext(profile)): Port {
        val meta = BExpressionMetaData()
        val way = BExpressionContextWay(rc.memoryclass * 512, meta)
        val node = BExpressionContextNode(0, meta)
        node.setForeignContext(way)
        meta.readMetaData(FileSystem.SYSTEM, lookups.toOkioPath())
        way.parseFile(FileSystem.SYSTEM, profile.toOkioPath(), "global", rc.keyValues)
        node.parseFile(FileSystem.SYSTEM, profile.toOkioPath(), "global", rc.keyValues)
        return Port(meta, way, node)
    }

    /** A private field of the oracle's `BExpressionContext`, read by reflection (the handoff's parse checks). */
    fun oracleField(ctx: OracleContext, name: String): Any? =
        OracleContext::class.java.getDeclaredField(name).apply { isAccessible = true }.get(ctx)

    /**
     * Runs [check] on every profile and fails once at the end with how many failed and why, so one profile's failure
     * does not hide the others'. Fails too unless all 26 were checked, so a short sample cannot pass.
     */
    fun forEveryProfile(check: (File) -> Unit) {
        val failures = ArrayList<String>()
        for (profile in profiles) {
            try {
                check(profile)
            } catch (t: Throwable) {
                failures += "${profile.name}: $t"
            }
        }
        for (f in failures) println("forEveryProfile: $f")
        if (profiles.size != 26 || failures.isNotEmpty()) {
            // Grouped by cause, so a count read from the XML says whether every profile failed for the same reason.
            val causes = failures.groupingBy { it.substringAfter(": ") }.eachCount()
            throw AssertionError(
                "${failures.size} of ${profiles.size} profiles failed (26 expected, RECORD -39); by cause: $causes; first: " +
                    failures.take(2).joinToString(" | "),
            )
        }
    }
}
