package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.mapaccess.MatchedWaypoint
import com.zynergylabs.forager.brouter.util.javaSplit

/**
 * Port of BRouter 1.7.10's `btools.router.RoutingParamCollector`: the methods Forager reaches. Forager's
 * `BRouterTrailRouter.buildEngine` calls the constructor, `getWayPointList("lon,lat|lon,lat")` and `setParams` with
 * three keys, `profile`, `alternativeidx` and `profile:use_dynamic_range`.
 *
 * - [getWayPointList] and its two `readPosition`s are ported whole (`RoutingParamCollector.java:23-53`, `:86-99`),
 *   the `d`/`m`/name third field included.
 * - [setParams] (`:133-247`) ports the keys Forager passes: `profile` (`localFunction`), `alternativeidx` and
 *   `profile:` keys (`keyValues`), and ignores every key Java ignores. Every other key Java acts on is refused with
 *   an [UnsupportedOperationException] ("unsupported: ...") before the context is touched: the nogo keys
 *   (`nogoLats` when not empty, `nogos`, `polylines`, `polygons`), `straight`, `pois`, `heading`, `direction`, the
 *   `roundTrip` keys, `allowSamewayback`, `turnInstructionMode`, `timode`, `turnInstructionFormat`,
 *   `exportWaypoints`, `exportCorrectedWaypoints`, `format` and `trackFormat`.
 * - **More than one `profile:` key is refused too.** Java keeps them in a `HashMap`, and `_parseFile` makes one
 *   assignment per key in that map's order (`BExpressionContext.java:863`); whether that order can change a result
 *   was not read (verify report, hash-order table, [?]). With one key there is no order.
 * - **Not ported** (no Forager call reaches them): `readPositions`, `getUrlParams`, `setProfileParams`,
 *   `parseNogoPolygons`, `readPoisList`, `readNogoList`, `readNogos` and the two `readNogo`s.
 *
 * Numbers are parsed with Kotlin's `toDouble`/`toInt`, which are Java's `Double.parseDouble`/`Integer.parseInt` on
 * the JVM; on native their grammar for unusual text (whitespace, hex, `d`/`f` suffixes) is [?]. Forager passes
 * `Double.toString` output.
 */
internal class RoutingParamCollector {
    /** `RoutingParamCollector.java:23-53`. */
    fun getWayPointList(lonLats: String?): MutableList<OsmNodeNamed> {
        if (lonLats == null) throw IllegalArgumentException("lonlats parameter not set")

        val coords = lonLats.javaSplit(";|\\|") // use both variantes
        if (coords.size < 1 || !coords[0].contains(",")) {
            throw IllegalArgumentException("we need one lat/lon point at least!")
        }

        val wplist = ArrayList<OsmNodeNamed>()
        for (i in coords.indices) {
            val lonLat = coords[i].javaSplit(",")
            if (lonLat.size < 1) throw IllegalArgumentException("we need one lat/lon point at least!")
            wplist.add(readPosition(lonLat[0], lonLat[1], "via$i")) // Java's index error when there is no comma
            if (lonLat.size > 2) {
                if (lonLat[2] == "d") {
                    wplist[wplist.size - 1].wpttype = MatchedWaypoint.WAYPOINT_TYPE_DIRECT
                } else if (lonLat[2] == "m") {
                    wplist[wplist.size - 1].wpttype = MatchedWaypoint.WAYPOINT_TYPE_MEETING
                } else {
                    wplist[wplist.size - 1].name = lonLat[2]
                    wplist[wplist.size - 1].wpttype = MatchedWaypoint.WAYPOINT_TYPE_MEETING
                }
            }
        }

        if (wplist[0].name!!.startsWith("via")) wplist[0].name = "from"
        if (wplist[wplist.size - 1].name!!.startsWith("via")) {
            wplist[wplist.size - 1].name = "to"
        }

        return wplist
    }

    /** `RoutingParamCollector.java:86-91`. `toDouble` is Java's `Double.parseDouble` on the JVM. */
    private fun readPosition(vlon: String?, vlat: String?, name: String): OsmNodeNamed {
        if (vlon == null) throw IllegalArgumentException("lon $name not found in input")
        if (vlat == null) throw IllegalArgumentException("lat $name not found in input")

        return readPosition(vlon.toDouble(), vlat.toDouble(), name)
    }

    /** `RoutingParamCollector.java:93-99`. Kotlin's `toInt` saturates and maps NaN to 0, as Java's `(int)` does. */
    private fun readPosition(lon: Double, lat: Double, name: String): OsmNodeNamed {
        val n = OsmNodeNamed()
        n.name = name
        n.ilon = ((lon + 180.0) * 1000000.0 + 0.5).toInt()
        n.ilat = ((lat + 90.0) * 1000000.0 + 0.5).toInt()
        return n
    }

    /**
     * `RoutingParamCollector.java:133-247`, for Forager's keys (see the class doc). [wplist] is only read by Java's
     * `straight` key, which is refused. `keyValues` is replaced by a new map holding the old entries and the new one.
     */
    @Suppress("UNUSED_PARAMETER")
    fun setParams(rctx: RoutingContext, wplist: List<OsmNodeNamed>, params: Map<String, String>?) {
        if (params == null) return
        if (params.size == 0) return

        // Refused before anything is set, so a refused call leaves rctx as it was.
        val nogoLats = params["nogoLats"]
        if (nogoLats != null && nogoLats.length > 0) unsupported("nogoLats")
        for (key in params.keys) if (key in REFUSED) unsupported(key)
        val profileKeys = params.keys.filter { it.startsWith("profile:") }
        val merged = LinkedHashMap(rctx.keyValues ?: emptyMap())
        for (key in profileKeys) merged[key.substring(8)] = params.getValue(key)
        if (profileKeys.isNotEmpty() && merged.size > 1) {
            throw UnsupportedOperationException(
                "unsupported: RoutingParamCollector.setParams with more than one profile: key (${merged.keys})",
            )
        }

        if (params.containsKey("profile")) {
            rctx.localFunction = params["profile"]
        }
        for ((key, value) in params) {
            if (key == "alternativeidx") {
                rctx.alternativeIdx = value.toInt() // Java's Integer.parseInt on the JVM
            } else if (key.startsWith("profile:")) {
                rctx.keyValues = merged
            }
            // ignore other params
        }
    }

    private fun unsupported(key: String): Nothing =
        throw UnsupportedOperationException("unsupported: RoutingParamCollector.setParams key \"$key\"")

    private companion object {
        /** The keys Java's `setParams` acts on that Forager never passes (`RoutingParamCollector.java:155-243`). */
        val REFUSED = setOf(
            "nogos", "polylines", "polygons", "straight", "pois", "heading", "direction", "roundTripDistance",
            "roundTripDirectionAdd", "roundTripPoints", "allowSamewayback", "turnInstructionMode", "timode",
            "turnInstructionFormat", "exportWaypoints", "exportCorrectedWaypoints", "format", "trackFormat",
        )
    }
}
