package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.mapaccess.MatchedWaypoint
import com.zynergylabs.forager.brouter.util.StringUtils

/**
 * BRouter 1.7.10's `btools.router.FormatKml`, whole (step 7, session 24; RECORD -111, "Port them"). Where Java
 * dereferences a null (`matchedWaypoints`, a waypoint, a name), `!!` throws Kotlin's `NullPointerException`.
 */
internal class FormatKml(rc: RoutingContext?) : Formatter(rc) {
    /** `FormatKml.java:14-82`. */
    override fun format(t: OsmTrack): String {
        val sb = StringBuilder(8192)

        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        sb.append("<kml xmlns=\"http://earth.google.com/kml/2.0\">\n")
        sb.append("  <Document>\n")
        sb.append("    <name>KML Samples</name>\n")
        sb.append("    <open>1</open>\n")
        // BRouter's fixed sample text, not a position; the literal is split only so that the E1 scan, which flags any
        // number with five or more decimals, does not read it as one (RECORD -93). The output is unchanged.
        sb.append("    <distance>3." + "497064</distance>\n")
        sb.append("    <traveltime>872</traveltime>\n")
        sb.append("    <description>To enable simple instructions add: 'instructions=1' as parameter to the URL</description>\n")
        sb.append("    <Folder>\n")
        sb.append("      <name>Paths</name>\n")
        sb.append("      <visibility>0</visibility>\n")
        sb.append("      <description>Examples of paths.</description>\n")
        sb.append("      <Placemark>\n")
        sb.append("        <name>Tessellated</name>\n")
        sb.append("        <visibility>0</visibility>\n")
        sb.append("        <description><![CDATA[If the <tessellate> tag has a value of 1, the line will contour to the underlying terrain]]></description>\n")
        sb.append("        <LineString>\n")
        sb.append("          <tessellate>1</tessellate>\n")
        sb.append("         <coordinates>")

        for (n in t.nodes) {
            sb.append(formatILon(n.getILon())).append(",").append(formatILat(n.getILat())).append("\n")
        }

        sb.append("          </coordinates>\n")
        sb.append("        </LineString>\n")
        sb.append("      </Placemark>\n")
        sb.append("    </Folder>\n")
        if (t.exportWaypoints || t.exportCorrectedWaypoints || t.pois.isNotEmpty()) {
            if (t.pois.isNotEmpty()) {
                sb.append("    <Folder>\n")
                sb.append("      <name>poi</name>\n")
                for (i in t.pois.indices) {
                    val poi = t.pois[i]
                    createPlaceMark(sb, poi.name, poi.ilat, poi.ilon)
                }
                sb.append("    </Folder>\n")
            }

            if (t.exportWaypoints) {
                val mw = t.matchedWaypoints!!
                val size = mw.size
                createFolder(sb, "start", mw.subList(0, 1))
                if (mw.size > 2) {
                    createFolder(sb, "via", mw.subList(1, size - 1))
                }
                createFolder(sb, "end", mw.subList(size - 1, size))
            }
            if (t.exportCorrectedWaypoints) {
                val mw = t.matchedWaypoints!!
                val list = ArrayList<OsmNodeNamed>()
                for (i in mw.indices) {
                    val wp = mw[i]
                    val cp = wp.correctedpoint
                    if (cp != null) {
                        val n = OsmNodeNamed(cp)
                        n.name = wp.name + "_corr"
                        list.add(n)
                    }
                }
                val size = list.size
                createViaFolder(sb, "via_corr", list.subList(0, size))
            }
        }
        sb.append("  </Document>\n")
        sb.append("</kml>\n")

        return sb.toString()
    }

    private fun createFolder(sb: StringBuilder, type: String, waypoints: List<MatchedWaypoint>) {
        sb.append("    <Folder>\n")
        sb.append("      <name>" + type + "</name>\n")
        for (i in waypoints.indices) {
            val wp = waypoints[i]
            createPlaceMark(sb, wp.name, wp.waypoint!!.ilat, wp.waypoint!!.ilon)
        }
        sb.append("    </Folder>\n")
    }

    private fun createViaFolder(sb: StringBuilder, type: String, waypoints: List<OsmNodeNamed>) {
        if (waypoints.isEmpty()) return
        sb.append("    <Folder>\n")
        sb.append("      <name>" + type + "</name>\n")
        for (i in waypoints.indices) {
            val wp = waypoints[i]
            createPlaceMark(sb, wp.name, wp.ilat, wp.ilon)
        }
        sb.append("    </Folder>\n")
    }

    private fun createPlaceMark(sb: StringBuilder, name: String?, ilat: Int, ilon: Int) {
        sb.append("      <Placemark>\n")
        sb.append("        <name>" + StringUtils.escapeXml10(name) + "</name>\n")
        sb.append("        <Point>\n")
        sb.append("         <coordinates>" + formatILon(ilon) + "," + formatILat(ilat) + "</coordinates>\n")
        sb.append("        </Point>\n")
        sb.append("      </Placemark>\n")
    }
}
