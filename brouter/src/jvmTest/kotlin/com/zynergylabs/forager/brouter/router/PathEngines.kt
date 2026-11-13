package com.zynergylabs.forager.brouter.router

import btools.router.ProfileCache as OracleProfileCache
import com.zynergylabs.forager.brouter.expressions.ExpressionsEngines
import com.zynergylabs.forager.brouter.mapaccess.MatchedWaypoint
import com.zynergylabs.forager.brouter.mapaccess.NodesCache
import com.zynergylabs.forager.brouter.mapaccess.OsmLink
import com.zynergylabs.forager.brouter.mapaccess.OsmNode
import com.zynergylabs.forager.brouter.mapaccess.OsmNodePairSet
import com.zynergylabs.forager.brouter.mapaccess.WaypointMatchEngines
import com.zynergylabs.forager.brouter.util.OracleLibm
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import java.io.File
import java.lang.reflect.Modifier

/**
 * Step 6: the two engines' paths side by side. Java's `OsmPath`, `StdPath` and `MessageData` are package private, so the
 * oracle's paths are made through `RoutingContext`'s public `createPath` by reflection, and both engines' paths are
 * written by reflection over **Java's own field names** ([PATH_FIELDS], `OsmPath`'s then `StdPath`'s), so a field the
 * port lacks or computes differently shows by name. Floats and doubles are written as their bits. A node is written as
 * its position, an `OsmPathElement` as its whole `origin` chain (position, height, cost, message), a `MessageData` as
 * its 22 fields. The port runs on [OracleLibm] (step 2b's rule: logic runs inject the oracle's table).
 *
 * The routing contexts are read as `RoutingContextReadTest` reads them: Forager's parameters on the oracle's
 * `RoutingParamCollector`, then each engine's `ProfileCache.parseProfile`.
 */
internal object PathEngines {
    private val oPath = Class.forName("btools.router.OsmPath")
    private val oStd = Class.forName("btools.router.StdPath")
    private val oMsg = Class.forName("btools.router.MessageData")
    private val oTrack = Class.forName("btools.router.OsmTrack")
    private val oCreate1 = btools.router.RoutingContext::class.java.getMethod("createPath", btools.mapaccess.OsmLink::class.java)
    private val oCreate2 = btools.router.RoutingContext::class.java
        .getMethod("createPath", oPath, btools.mapaccess.OsmLink::class.java, oTrack, java.lang.Boolean.TYPE)

    private fun names(c: Class<*>) = c.declaredFields.filter { !Modifier.isStatic(it.modifiers) }.map { it.name }

    val PATH_FIELDS: List<String> = names(oPath) + names(oStd)
    val MESSAGE_FIELDS: List<String> = names(oMsg)

    fun field(o: Any, name: String): Any? {
        var c: Class<*>? = o.javaClass
        while (c != null) {
            val f = try {
                c.getDeclaredField(name)
            } catch (e: NoSuchFieldException) {
                null
            }
            if (f != null && !Modifier.isStatic(f.modifiers)) return f.apply { isAccessible = true }.get(o)
            c = c.superclass
        }
        error("${o.javaClass.name} has no field $name")
    }

    fun setField(o: Any, name: String, v: Any?) {
        var c: Class<*>? = o.javaClass
        while (c != null) {
            val f = try {
                c.getDeclaredField(name)
            } catch (e: NoSuchFieldException) {
                null
            }
            if (f != null) return f.apply { isAccessible = true }.set(o, v)
            c = c.superclass
        }
        error("${o.javaClass.name} has no field $name")
    }

    private fun isNode(v: Any) = v is OsmNode || v is btools.mapaccess.OsmNode

    fun fmt(v: Any?): String = when {
        v == null -> "null"
        v is Float -> "f%08x".format(v.toRawBits())
        v is Double -> "d%016x".format(v.toRawBits())
        v is Number || v is Boolean || v is String -> v.toString()
        v.javaClass.simpleName == "MessageData" -> MESSAGE_FIELDS.joinToString(" ", "{", "}") { "$it=${fmt(field(v, it))}" }
        v.javaClass.simpleName == "OsmPathElement" -> element(v)
        isNode(v) -> "n${field(v, "ilon")}:${field(v, "ilat")}"
        v.javaClass.simpleName == "OsmLink" -> "link(${fmt(field(v, "n1"))} ${fmt(field(v, "n2"))})"
        else -> v.javaClass.simpleName
    }

    private fun element(e0: Any): String {
        val sb = StringBuilder("[")
        var e: Any? = e0
        var i = 0
        while (e != null && i < 1000) {
            if (i > 0) sb.append(" <- ")
            sb.append("e${field(e, "ilon")}:${field(e, "ilat")}:${field(e, "selev")}:${field(e, "cost")}:${fmt(field(e, "message"))}")
            e = field(e, "origin")
            i++
        }
        return sb.append("]").toString()
    }

    /** One path, every field by Java's name. */
    fun state(p: Any?): String = if (p == null) "null" else PATH_FIELDS.joinToString(" ") { "$it=${fmt(field(p, it))}" }

    fun cost(p: Any): Int = field(p, "cost") as Int

    /** A pair of routing contexts on one profile, or null when the profile is not a `StdModel` one (6 are Kinematic). */
    class Contexts(val profile: File, val o: btools.router.RoutingContext, val p: RoutingContext) {
        fun release() {
            OracleProfileCache.releaseProfile(o)
            ProfileCache.releaseProfile(p)
        }
    }

    fun contexts(profile: File): Contexts? {
        val orc = ExpressionsEngines.foragerRoutingContext(profile)
        OracleProfileCache.parseProfile(orc)
        if (orc.pm.javaClass.name != "btools.router.StdModel") {
            OracleProfileCache.releaseProfile(orc)
            return null
        }
        val prc = RoutingContext(OracleLibm)
        prc.localFunction = orc.localFunction
        prc.keyValues = orc.keyValues
        prc.memoryclass = orc.memoryclass
        ProfileCache.parseProfile(prc, FileSystem.SYSTEM)
        return Contexts(profile, orc, prc)
    }

    /** The profiles whose path model is `StdModel`, as the oracle reads them (20 of the 26). */
    fun stdProfiles(): List<File> = ExpressionsEngines.profiles.filter { f ->
        val c = contexts(f)
        c?.release()
        c != null
    }

    fun oCreate(rc: btools.router.RoutingContext, link: btools.mapaccess.OsmLink): Any = oCreate1.invoke(rc, link)

    fun oCreate(rc: btools.router.RoutingContext, origin: Any, link: btools.mapaccess.OsmLink, detail: Boolean): Any =
        try {
            oCreate2.invoke(rc, origin, link, null, detail)
        } catch (e: java.lang.reflect.InvocationTargetException) {
            throw e.targetException
        }

    /** The call's result, or the exception's class and message, so a throw on one engine is compared as a value. */
    inline fun outcome(block: () -> Any?): String = try {
        block().toString()
    } catch (e: RuntimeException) {
        "${e.javaClass.name.substringAfterLast('.')}: ${e.message}"
    } catch (e: NotImplementedError) {
        "NotImplementedError: ${e.message}"
    }

    /** Runs [block], writes the path it makes (or what it threw) as one line, and returns the path or null. */
    inline fun outcomeOr(sb: StringBuilder, block: () -> Any): Any? = try {
        block().also { sb.append("path ").append(state(it)).append('\n') }
    } catch (e: RuntimeException) {
        sb.append("throw ${e.javaClass.name.substringAfterLast('.')}: ${e.message}\n")
        null
    }

    /**
     * The graph as `_findTrack` prepares it (`RoutingEngine.java:1879-1928`): the ends matched on a first cache, a second
     * cache over it (`resetCache`), its cleanup mode, the four graph nodes, both starts obtained and expanded. Null with
     * the reason when the ends do not match or a start cannot be obtained; both engines are asked the same.
     */
    class OracleGraph(segments: File, val rc: btools.router.RoutingContext, wps: List<btools.router.OsmNodeNamed>, detailed: Boolean) {
        val mw: List<btools.mapaccess.MatchedWaypoint>
        val nc: btools.mapaccess.NodesCache
        val first: btools.mapaccess.NodesCache
        var nodes: List<btools.mapaccess.OsmNode> = emptyList()
        var refusal: String? = null

        init {
            mw = wps.map { n -> btools.mapaccess.MatchedWaypoint().also { it.waypoint = n; it.name = n.name } }
            val maxmem = rc.memoryclass * 1024L * 1024L
            first = btools.mapaccess.NodesCache(segments, rc.expctxWay, false, maxmem, null, false)
            val m = outcome { first.matchWaypointsToNodes(mw, WaypointMatchEngines.RANGE, btools.mapaccess.OsmNodePairSet(WaypointMatchEngines.ISLAND)) }
            nc = btools.mapaccess.NodesCache(segments, rc.expctxWay, false, maxmem, first, detailed)
            nc.nodesMap.cleanupMode = if (detailed) 0 else if (rc.considerTurnRestrictions) 2 else 1
            if (mw.any { it.node1 == null || it.node2 == null }) {
                refusal = "match $m"
            } else {
                val n = listOf(mw[0].node1, mw[0].node2, mw[1].node1, mw[1].node2).map { nc.getGraphNode(it) }
                nc.nodesMap.endNode1 = n[2]
                nc.nodesMap.endNode2 = n[3]
                nodes = n
                for (s in n.subList(0, 2)) {
                    if (!nc.obtainNonHollowNode(s)) refusal = "obtain false"
                    if (refusal == null) nc.expandHollowLinkTargets(s)
                }
            }
        }

        fun close() {
            nc.close()
            first.close()
        }
    }

    /** The port's side, line for line as [OracleGraph]. */
    class PortGraph(segments: File, val rc: RoutingContext, wps: List<btools.router.OsmNodeNamed>, detailed: Boolean) {
        val mw: List<MatchedWaypoint>
        val nc: NodesCache
        val first: NodesCache
        var nodes: List<OsmNode> = emptyList()
        var refusal: String? = null

        init {
            val dir = segments.toOkioPath()
            mw = wps.map { n -> MatchedWaypoint().also { it.waypoint = OsmNode(n.ilon, n.ilat); it.name = n.name } }.toMutableList()
            val maxmem = rc.memoryclass * 1024L * 1024L
            first = NodesCache(FileSystem.SYSTEM, dir, rc.expctxWay!!, false, maxmem, null, false)
            val m = outcome { first.matchWaypointsToNodes(OracleLibm, mw, WaypointMatchEngines.RANGE, OsmNodePairSet(WaypointMatchEngines.ISLAND)) }
            nc = NodesCache(FileSystem.SYSTEM, dir, rc.expctxWay!!, false, maxmem, first, detailed)
            nc.nodesMap.cleanupMode = if (detailed) 0 else if (rc.considerTurnRestrictions) 2 else 1
            if (mw.any { it.node1 == null || it.node2 == null }) {
                refusal = "match $m"
            } else {
                val n = listOf(mw[0].node1!!, mw[0].node2!!, mw[1].node1!!, mw[1].node2!!).map { nc.getGraphNode(it) }
                nc.nodesMap.endNode1 = n[2]
                nc.nodesMap.endNode2 = n[3]
                nodes = n
                for (s in n.subList(0, 2)) {
                    if (!nc.obtainNonHollowNode(s)) refusal = "obtain false"
                    if (refusal == null) nc.expandHollowLinkTargets(s)
                }
            }
        }

        fun close() {
            nc.close()
            first.close()
        }
    }

    /** Forager's way of giving the ends (the oracle's `RoutingParamCollector`, as `GraphHalfTest`). */
    fun waypoints(c: com.zynergylabs.forager.brouter.oracle.e3.E3Case): List<btools.router.OsmNodeNamed> =
        btools.router.RoutingParamCollector().getWayPointList("${c.startLon},${c.startLat}|${c.endLon},${c.endLat}")

    fun startLink(n: btools.mapaccess.OsmNode) = btools.mapaccess.OsmLink(null, n)

    fun startLink(n: OsmNode) = OsmLink(null, n)
}
