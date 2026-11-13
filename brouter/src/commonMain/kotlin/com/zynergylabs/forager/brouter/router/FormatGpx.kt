package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.mapaccess.MatchedWaypoint
import com.zynergylabs.forager.brouter.util.StringUtils
import okio.FileSystem
import okio.Path.Companion.toPath

/**
 * BRouter 1.7.10's `btools.router.FormatGpx`, whole (step 7, session 25; RECORD -111, "Port them"). Java's
 * `BufferedWriter` over a `StringWriter` is a [StringBuilder] here. Where Java dereferences a null, `!!` throws Kotlin's
 * `NullPointerException`; where it indexes past a list's end, Kotlin's list throws as Java's does. Java's
 * `formatAsGpx` returns its writer's `toString()`, an object identity that `format` drops; this one returns nothing.
 *
 * A known platform difference (the owner's ruling, RECORD -163): a floating number appended here with string `+` (the
 * `<ele>` of hints, track points and waypoints, `<locus:rteDistance>`, `<locus:rteTime>`, `<locus:rteSpeed>`, and
 * `<brouter:speed>` through [FormatJson.speedText]) is printed with the platform's own text, which on iOS can differ
 * from Java's in its last digit at a tie; the value it reads back to does not.
 */
internal class FormatGpx(rc: RoutingContext?) : Formatter(rc) {
    /** `FormatGpx.java:19-31`. Java wraps whatever is thrown in a `RuntimeException`; so does this. */
    override fun format(t: OsmTrack): String {
        try {
            val sw = StringBuilder(8192)
            formatAsGpx(sw, t)
            return sw.toString()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    /** `FormatGpx.java:33-474`. */
    fun formatAsGpx(sb: StringBuilder, t: OsmTrack) {
        val turnInstructionMode = t.voiceHints?.turnInstructionMode ?: 0

        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        if (turnInstructionMode != 9) {
            val ml = t.messageList!!
            for (i in ml.size - 1 downTo 0) {
                var message: String? = ml[i]
                if (i < ml.size - 1) {
                    message = "(alt-index " + i + ": " + message + " )"
                }
                if (message != null) {
                    sb.append("<!-- ").append(message).append(" -->\n")
                }
            }
        }

        if (turnInstructionMode == 4) { // comment style
            sb.append("<!-- \$transport-mode\$").append(t.voiceHints!!.getTransportMode()).append("\$ -->\n")
            sb.append("<!--          cmd    idx        lon        lat d2next  geometry -->\n")
            sb.append("<!-- \$turn-instruction-start\$\n")
            for (hint in t.voiceHints!!.list) {
                // Java's String.format("     $turn$%6s;%6d;%10s;%10s;%6d;%s$\n", ...): right-justified fields, digits
                // in the default locale's (ASCII on the oracle's JVM [I]).
                sb.append("     \$turn\$")
                    .append(getCommandString(hint.cmd, hint.roundaboutExit, turnInstructionMode).padStart(6)).append(';')
                    .append(hint.indexInTrack.toString().padStart(6)).append(';')
                    .append(formatILon(hint.ilon).padStart(10)).append(';')
                    .append(formatILat(hint.ilat).padStart(10)).append(';')
                    .append(hint.distanceToNext.toInt().toString().padStart(6)).append(';')
                    .append(hint.formatGeometry()).append("\$\n")
            }
            sb.append("    \$turn-instruction-end\$ -->\n")
        }
        sb.append("<gpx \n")
        sb.append(" xmlns=\"http://www.topografix.com/GPX/1/1\" \n")
        sb.append(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n")
        if (turnInstructionMode == 9) { // BRouter style
            sb.append(" xmlns:brouter=\"Not yet documented\" \n")
        }
        if (turnInstructionMode == 7) { // old locus style
            sb.append(" xmlns:locus=\"http://www.locusmap.eu\" \n")
        }
        sb.append(" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\" \n")

        if (turnInstructionMode == 3) {
            sb.append(" creator=\"OsmAndRouter\" version=\"1.1\">\n")
        } else {
            sb.append(" creator=\"BRouter-" + OsmTrack.version + "\" version=\"1.1\">\n")
        }
        if (turnInstructionMode == 9) {
            sb.append(" <metadata>\n")
            sb.append("  <name>").append(t.name).append("</name>\n")
            sb.append("  <extensions>\n")
            sb.append("   <brouter:info>").append(t.messageList!![0]).append("</brouter:info>\n")
            val params = t.params
            if (params != null && params.size > 0) {
                sb.append("   <brouter:params><![CDATA[")
                var i = 0
                for (e in params.entries) {
                    if (i++ != 0) sb.append("&")
                    sb.append(e.key).append("=").append(e.value)
                }
                sb.append("]]></brouter:params>\n")
            }
            sb.append("  </extensions>\n")
            sb.append(" </metadata>\n")
        }
        if (turnInstructionMode == 3 || turnInstructionMode == 8) { // osmand style, cruiser
            val vh = t.voiceHints!!
            var lastRteTime = 0f

            sb.append(" <rte>\n")

            var rteTime = t.getVoiceHintTime(0)
            val first = StringBuilder()
            // define start point
            run {
                first.append("  <rtept lat=\"").append(formatILat(t.nodes[0].getILat())).append("\" lon=\"")
                    .append(formatILon(t.nodes[0].getILon())).append("\">\n")
                    .append("   <desc>start</desc>\n   <extensions>\n")
                if (rteTime != lastRteTime) { // add timing only if available
                    val ti: Double = (rteTime - lastRteTime).toDouble()
                    first.append("    <time>").append("" + (ti + 0.5).toInt()).append("</time>\n")
                    lastRteTime = rteTime
                }
                first.append("    <offset>0</offset>\n  </extensions>\n </rtept>\n")
            }
            if (turnInstructionMode == 8) {
                if (t.matchedWaypoints!![0].wpttype == MatchedWaypoint.WAYPOINT_TYPE_DIRECT && vh.list[0].indexInTrack == 0) {
                    // has a voice hint do nothing, voice hint will do
                } else {
                    sb.append(first.toString())
                }
            } else {
                sb.append(first.toString())
            }

            for (i in 0 until vh.list.size) {
                val hint = vh.list[i]
                sb.append("  <rtept lat=\"").append(formatILat(hint.ilat)).append("\" lon=\"")
                    .append(formatILon(hint.ilon)).append("\">\n")
                    .append("   <desc>")
                    .append(if (turnInstructionMode == 3) getMessageString(hint.cmd, hint.roundaboutExit, turnInstructionMode) else getCruiserMessageString(hint.cmd, hint.roundaboutExit))
                    .append("</desc>\n   <extensions>\n")

                rteTime = t.getVoiceHintTime(i + 1)

                if (rteTime != lastRteTime) { // add timing only if available
                    val ti: Double = (rteTime - lastRteTime).toDouble()
                    sb.append("    <time>").append("" + (ti + 0.5).toInt()).append("</time>\n")
                    lastRteTime = rteTime
                }
                sb.append("    <turn>")
                    .append(if (turnInstructionMode == 3) getCommandString(hint.cmd, hint.roundaboutExit, turnInstructionMode) else getCruiserCommandString(hint.cmd, hint.roundaboutExit))
                    .append("</turn>\n    <turn-angle>").append("" + hint.angle.toInt())
                    .append("</turn-angle>\n    <offset>").append("" + hint.indexInTrack).append("</offset>\n  </extensions>\n </rtept>\n")
            }
            sb.append("  <rtept lat=\"").append(formatILat(t.nodes[t.nodes.size - 1].getILat())).append("\" lon=\"")
                .append(formatILon(t.nodes[t.nodes.size - 1].getILon())).append("\">\n")
                .append("   <desc>destination</desc>\n   <extensions>\n")
            sb.append("    <time>0</time>\n")
            sb.append("    <offset>").append("" + (t.nodes.size - 1)).append("</offset>\n  </extensions>\n </rtept>\n")

            sb.append("</rte>\n")
        }

        if (turnInstructionMode == 7) { // old locus style
            val vh = t.voiceHints!!
            var lastRteTime = t.getVoiceHintTime(0)

            for (i in 0 until vh.list.size) {
                val hint = vh.list[i]
                sb.append(" <wpt lon=\"").append(formatILon(hint.ilon)).append("\" lat=\"")
                    .append(formatILat(hint.ilat)).append("\">")
                    .append(if (hint.selev == Short.MIN_VALUE) "" else "<ele>" + (hint.selev / 4.0) + "</ele>")
                    .append("<name>")
                    .append(getMessageString(hint.cmd, hint.roundaboutExit, turnInstructionMode))
                    .append("</name>")
                    .append("<extensions><locus:rteDistance>").append("" + hint.distanceToNext).append("</locus:rteDistance>")
                val rteTime = t.getVoiceHintTime(i + 1)
                if (rteTime != lastRteTime) { // add timing only if available
                    val ti: Double = (rteTime - lastRteTime).toDouble()
                    val speed = hint.distanceToNext / ti
                    sb.append("<locus:rteTime>").append("" + ti).append("</locus:rteTime>")
                        .append("<locus:rteSpeed>").append("" + speed).append("</locus:rteSpeed>")
                    lastRteTime = rteTime
                }
                sb.append("<locus:rtePointAction>").append("" + getLocusAction(hint.cmd, hint.roundaboutExit)).append("</locus:rtePointAction></extensions>")
                    .append("</wpt>\n")
            }
        }
        if (turnInstructionMode == 5) { // gpsies style
            for (hint in t.voiceHints!!.list) {
                // Java's `toLowerCase()` takes the default locale; the symbol strings are ASCII [I].
                sb.append(" <wpt lon=\"").append(formatILon(hint.ilon)).append("\" lat=\"")
                    .append(formatILat(hint.ilat)).append("\">")
                    .append("<name>").append(getMessageString(hint.cmd, hint.roundaboutExit, turnInstructionMode)).append("</name>")
                    .append("<sym>").append(getSymbolString(hint.cmd, hint.roundaboutExit, turnInstructionMode).lowercase()).append("</sym>")
                    .append("<type>").append(getSymbolString(hint.cmd, hint.roundaboutExit, turnInstructionMode)).append("</type>")
                    .append("</wpt>\n")
            }
        }

        if (turnInstructionMode == 6) { // orux style
            for (hint in t.voiceHints!!.list) {
                sb.append(" <wpt lat=\"").append(formatILat(hint.ilat)).append("\" lon=\"")
                    .append(formatILon(hint.ilon)).append("\">")
                    .append(if (hint.selev == Short.MIN_VALUE) "" else "<ele>" + (hint.selev / 4.0) + "</ele>")
                    .append(
                        "<extensions>\n" +
                            "  <om:oruxmapsextensions xmlns:om=\"http://www.oruxmaps.com/oruxmapsextensions/1/0\">\n" +
                            "   <om:ext type=\"ICON\" subtype=\"0\">",
                    ).append("" + getOruxAction(hint.cmd, hint.roundaboutExit))
                    .append(
                        "</om:ext>\n" +
                            "  </om:oruxmapsextensions>\n" +
                            "  </extensions>\n" +
                            " </wpt>\n",
                    )
            }
        }

        for (i in 0..t.pois.size - 1) {
            val poi = t.pois[i]
            formatWaypointGpx(sb, poi, "poi")
        }

        if (t.exportWaypoints) {
            val wps = t.matchedWaypoints!!
            for (i in 0..wps.size - 1) {
                val wt = wps[i]
                if (i == 0) {
                    formatWaypointGpx(sb, wt, if (wt.wpttype == MatchedWaypoint.WAYPOINT_TYPE_DIRECT) "beeline" else "via")
                } else if (i == wps.size - 1) {
                    formatWaypointGpx(sb, wt, "via")
                } else {
                    if (wt.wpttype == MatchedWaypoint.WAYPOINT_TYPE_DIRECT) {
                        formatWaypointGpx(sb, wt, "beeline")
                    } else if (wt.wpttype == MatchedWaypoint.WAYPOINT_TYPE_MEETING) {
                        formatWaypointGpx(sb, wt, "via")
                    } else {
                        formatWaypointGpx(sb, wt, "shaping")
                    }
                }
            }
        }
        if (t.exportCorrectedWaypoints) {
            sb.append("\n")
            val wps = t.matchedWaypoints!!
            for (i in 0..wps.size - 1) {
                val wt = wps[i]
                val cp = wt.correctedpoint
                if (cp != null) {
                    val n = OsmNodeNamed(cp)
                    n.name = wt.name + "_corr"
                    formatWaypointGpx(sb, n, "shaping")
                }
            }
            sb.append("\n")
        }

        sb.append(" <trk>\n")
        if (turnInstructionMode == 9 ||
            turnInstructionMode == 2 ||
            turnInstructionMode == 8 ||
            turnInstructionMode == 4
        ) { // Locus, comment, cruise, brouter style
            sb.append("  <src>").append(t.name).append("</src>\n")
            sb.append("  <type>").append(t.voiceHints!!.getTransportMode()).append("</type>\n")
        } else {
            sb.append("  <name>").append(t.name).append("</name>\n")
        }

        if (turnInstructionMode == 7) {
            sb.append("  <extensions>\n")
            sb.append("   <locus:rteComputeType>").append("" + t.voiceHints!!.getLocusRouteType()).append("</locus:rteComputeType>\n")
            sb.append("   <locus:rteSimpleRoundabouts>1</locus:rteSimpleRoundabouts>\n")
            sb.append("  </extensions>\n")
        }

        // all points
        sb.append("  <trkseg>\n")
        var lastway = ""
        var bNextDirect = false
        var nn: OsmPathElement? = null

        for (idx in 0 until t.nodes.size) {
            val n = t.nodes[idx]
            var sele = if (n.getSElev() == Short.MIN_VALUE) "" else "<ele>" + n.getElev() + "</ele>"
            val hint = t.getVoiceHint(idx)
            val mwpt = t.getMatchedWaypoint(idx)

            if (t.showTime) {
                sele += "<time>" + getFormattedTime3(n.getTime()) + "</time>"
            }
            if (turnInstructionMode == 8) {
                if (mwpt != null &&
                    !mwpt.name!!.startsWith("via") && !mwpt.name!!.startsWith("from") && !mwpt.name!!.startsWith("to")
                ) {
                    sele += "<name>" + mwpt.name + "</name>"
                }
            }
            var bNeedHeader: Boolean
            if (turnInstructionMode == 9) { // trkpt/sym style

                if (hint != null) {

                    if (mwpt != null &&
                        !mwpt.name!!.startsWith("via") && !mwpt.name!!.startsWith("from") && !mwpt.name!!.startsWith("to")
                    ) {
                        sele += "<name>" + mwpt.name + "</name>"
                    }
                    sele += "<desc>" + getCruiserMessageString(hint.cmd, hint.roundaboutExit) + "</desc>"
                    sele += "<sym>" + getCommandString(hint.cmd, hint.roundaboutExit, turnInstructionMode) + "</sym>"
                    if (mwpt != null) {
                        if (mwpt.wpttype == MatchedWaypoint.WAYPOINT_TYPE_MEETING) {
                            sele += "<type>via</type>"
                        } else {
                            sele += "<type>shaping</type>"
                        }
                    }
                    sele += "<extensions>"
                    if (t.showspeed) {
                        sele += "<brouter:speed>" + FormatJson.speedText(n, nn) + "</brouter:speed>"
                    }

                    sele += "<brouter:voicehint>" + getCommandString(hint.cmd, hint.roundaboutExit, turnInstructionMode) + ";" + hint.distanceToNext.toInt() + "," + hint.formatGeometry() + "</brouter:voicehint>"
                    val msg = n.message
                    if (msg != null && msg.wayKeyValues != null && msg.wayKeyValues != lastway) {
                        sele += "<brouter:way>" + msg.wayKeyValues + "</brouter:way>"
                        lastway = msg.wayKeyValues!!
                    }
                    if (msg != null && msg.nodeKeyValues != null) {
                        sele += "<brouter:node>" + msg.nodeKeyValues + "</brouter:node>"
                    }
                    sele += "</extensions>"
                }
                if (idx == 0 && hint == null) {
                    if (mwpt != null && mwpt.wpttype == MatchedWaypoint.WAYPOINT_TYPE_DIRECT) {
                        sele += "<desc>beeline</desc>"
                    } else {
                        sele += "<desc>start</desc>"
                    }
                    sele += "<type>via</type>"
                } else if (idx == t.nodes.size - 1 && hint == null) {
                    sele += "<desc>end</desc>"
                    sele += "<type>via</type>"
                } else {
                    if (mwpt != null && hint == null) {
                        if (mwpt.wpttype == MatchedWaypoint.WAYPOINT_TYPE_DIRECT) {
                            // bNextDirect = true;
                            sele += "<desc>beeline</desc>"
                        } else {
                            sele += "<desc>" + mwpt.name + "</desc>"
                        }
                        if (mwpt.wpttype == MatchedWaypoint.WAYPOINT_TYPE_MEETING) {
                            sele += "<type>via</type>"
                        } else {
                            sele += "<type>shaping</type>"
                        }
                        bNextDirect = false
                    }
                }

                if (hint == null) {
                    val msg = n.message
                    bNeedHeader = (t.showspeed || (msg != null && msg.wayKeyValues != null && msg.wayKeyValues != lastway)) ||
                        (msg != null && msg.nodeKeyValues != null)
                    if (bNeedHeader) {
                        sele += "<extensions>"
                        if (t.showspeed) {
                            sele += "<brouter:speed>" + FormatJson.speedText(n, nn) + "</brouter:speed>"
                        }
                        if (msg != null && msg.wayKeyValues != null && msg.wayKeyValues != lastway) {
                            sele += "<brouter:way>" + msg.wayKeyValues + "</brouter:way>"
                            lastway = msg.wayKeyValues!!
                        }
                        if (msg != null && msg.nodeKeyValues != null) {
                            sele += "<brouter:node>" + msg.nodeKeyValues + "</brouter:node>"
                        }
                        sele += "</extensions>"
                    }
                }
            }

            if (turnInstructionMode == 2) { // locus style new
                if (hint != null) {
                    if (mwpt != null) {
                        val name = mwpt.name!!
                        if (!name.startsWith("via") && !name.startsWith("from") && !name.startsWith("to") && !name.startsWith("rt")) {
                            sele += "<name>" + mwpt.name + "</name>"
                        }
                        if (mwpt.wpttype == MatchedWaypoint.WAYPOINT_TYPE_DIRECT && bNextDirect) {
                            sele += "<src>" + getLocusSymbolString(hint.cmd, hint.roundaboutExit) + "</src><sym>pass_place</sym><type>Shaping</type>"
                            // bNextDirect = false;
                        } else if (mwpt.wpttype == MatchedWaypoint.WAYPOINT_TYPE_DIRECT) {
                            if (idx == 0) {
                                sele += "<sym>pass_place</sym><type>Via</type>"
                            } else {
                                sele += "<sym>pass_place</sym><type>Shaping</type>"
                            }
                            bNextDirect = true
                        } else if (bNextDirect) {
                            sele += "<src>beeline</src><sym>" + getLocusSymbolString(hint.cmd, hint.roundaboutExit) + "</sym><type>Shaping</type>"
                            bNextDirect = false
                        } else {
                            sele += "<sym>" + getLocusSymbolString(hint.cmd, hint.roundaboutExit) + "</sym><type>Via</type>"
                        }
                    } else {
                        sele += "<sym>" + getLocusSymbolString(hint.cmd, hint.roundaboutExit) + "</sym>"
                    }
                } else {
                    if (idx == 0) {
                        val pos = sele.indexOf("<sym")
                        if (pos != -1) {
                            sele = sele.substring(0, pos)
                        }
                        if (mwpt != null && !mwpt.name!!.startsWith("from")) {
                            sele += "<name>" + mwpt.name + "</name>"
                        }
                        if (mwpt != null && mwpt.wpttype == MatchedWaypoint.WAYPOINT_TYPE_DIRECT) {
                            bNextDirect = true
                        }
                        sele += "<sym>pass_place</sym>"
                        sele += "<type>Via</type>"
                    } else if (idx == t.nodes.size - 1) {
                        val pos = sele.indexOf("<sym")
                        if (pos != -1) {
                            sele = sele.substring(0, pos)
                        }
                        if (mwpt != null && mwpt.name != null && !mwpt.name!!.startsWith("to")) {
                            sele += "<name>" + mwpt.name + "</name>"
                        }
                        if (bNextDirect) {
                            sele += "<src>beeline</src>"
                        }
                        sele += "<sym>pass_place</sym>"
                        sele += "<type>Via</type>"
                    } else {
                        if (mwpt != null) {
                            val name = mwpt.name!!
                            if (!name.startsWith("via") && !name.startsWith("from") && !name.startsWith("to") && !name.startsWith("rt")) {
                                sele += "<name>" + mwpt.name + "</name>"
                            }
                            if (mwpt.wpttype == MatchedWaypoint.WAYPOINT_TYPE_DIRECT && bNextDirect) {
                                sele += "<src>beeline</src><sym>pass_place</sym><type>Shaping</type>"
                            } else if (mwpt.wpttype == MatchedWaypoint.WAYPOINT_TYPE_DIRECT) {
                                if (idx == 0) {
                                    sele += "<sym>pass_place</sym><type>Via</type>"
                                } else {
                                    sele += "<sym>pass_place</sym><type>Shaping</type>"
                                }
                                bNextDirect = true
                            } else if (bNextDirect) {
                                sele += "<src>beeline</src><sym>pass_place</sym><type>Shaping</type>"
                                bNextDirect = false
                            } else if (name.startsWith("via") ||
                                name.startsWith("from") ||
                                name.startsWith("to") ||
                                name.startsWith("rt")
                            ) {
                                if (bNextDirect) {
                                    sele += "<src>beeline</src><sym>pass_place</sym><type>Shaping</type>"
                                } else {
                                    sele += "<sym>pass_place</sym><type>Shaping</type>"
                                }
                                bNextDirect = false
                            } else {
                                sele += "<name>" + mwpt.name + "</name>"
                                sele += "<sym>pass_place</sym><type>Via</type>"
                            }
                        }
                    }
                }
            }
            sb.append("   <trkpt lon=\"").append(formatILon(n.getILon())).append("\" lat=\"")
                .append(formatILat(n.getILat())).append("\">").append(sele).append("</trkpt>\n")

            nn = n
        }

        sb.append("  </trkseg>\n")
        sb.append(" </trk>\n")
        sb.append("</gpx>\n")
    }

    /** `FormatGpx.java:476-489`. Java wraps whatever is thrown in a `RuntimeException`; so does this. */
    fun formatAsWaypoint(n: OsmNodeNamed): String {
        try {
            val sw = StringBuilder(8192)
            formatGpxHeader(sw)
            formatWaypointGpx(sw, n, null)
            formatGpxFooter(sw)
            return sw.toString()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    /** `FormatGpx.java:491-498`. */
    fun formatGpxHeader(sb: StringBuilder) {
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        sb.append("<gpx \n")
        sb.append(" xmlns=\"http://www.topografix.com/GPX/1/1\" \n")
        sb.append(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n")
        sb.append(" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\" \n")
        sb.append(" creator=\"BRouter-" + OsmTrack.version + "\" version=\"1.1\">\n")
    }

    /** `FormatGpx.java:500-502`. */
    fun formatGpxFooter(sb: StringBuilder) {
        sb.append("</gpx>\n")
    }

    /** `FormatGpx.java:504-520`. */
    fun formatWaypointGpx(sb: StringBuilder, n: OsmNodeNamed, type: String?) {
        sb.append(" <wpt lon=\"").append(formatILon(n.ilon)).append("\" lat=\"")
            .append(formatILat(n.ilat)).append("\">")
        if (n.getSElev() != Short.MIN_VALUE) {
            sb.append("<ele>").append("" + n.getElev()).append("</ele>")
        }
        if (n.name != null) {
            sb.append("<name>").append(StringUtils.escapeXml10(n.name)).append("</name>")
        }
        val d = n.nodeDescription
        val rc = rc
        if (d != null && rc != null) {
            sb.append("<desc>").append(rc.expctxWay!!.getKeyValueDescription(false, d)).append("</desc>")
        }
        if (type != null) {
            sb.append("<type>").append(type).append("</type>")
        }
        sb.append("</wpt>\n")
    }

    /** `FormatGpx.java:522-535`. */
    fun formatWaypointGpx(sb: StringBuilder, wp: MatchedWaypoint, type: String?) {
        val w = wp.waypoint!!
        sb.append(" <wpt lon=\"").append(formatILon(w.ilon)).append("\" lat=\"")
            .append(formatILat(w.ilat)).append("\">")
        if (w.getSElev() != Short.MIN_VALUE) {
            sb.append("<ele>").append("" + w.getElev()).append("</ele>")
        }
        if (wp.name != null) {
            sb.append("<name>").append(StringUtils.escapeXml10(wp.name)).append("</name>")
        }
        if (type != null) {
            sb.append("<type>").append(type).append("</type>")
        }
        sb.append("</wpt>\n")
    }

    /**
     * `FormatGpx.java:541-571`. Java reads with `InputStreamReader`'s default charset and `BufferedReader.readLine`,
     * which ends a line at `\n`, `\r` or `\r\n`; this reads UTF-8 [I] and splits lines the same way. A coordinate is
     * parsed with `String.toDouble`, Java's `Double.parseDouble` on the JVM [?, Kotlin/Native, step 10].
     */
    override fun read(filename: String): OsmTrack? = read(filename, FileSystem.SYSTEM)

    fun read(filename: String, fileSystem: FileSystem): OsmTrack? {
        val path = filename.toPath()
        if (!fileSystem.exists(path)) {
            return null
        }
        val track = OsmTrack()
        val text = fileSystem.read(path) { readUtf8() }

        for (line in readLines(text)) {
            var idx0 = line.indexOf("<trkpt ")
            if (idx0 >= 0) {
                idx0 = line.indexOf(" lon=\"")
                idx0 += 6
                val idx1 = line.indexOf('"', idx0)
                val ilon = ((line.substring(idx0, idx1).toDouble() + 180.0) * 1000000.0 + 0.5).toInt()
                var idx2 = line.indexOf(" lat=\"")
                if (idx2 < 0) {
                    continue
                }
                idx2 += 6
                val idx3 = line.indexOf('"', idx2)
                val ilat = ((line.substring(idx2, idx3).toDouble() + 90.0) * 1000000.0 + 0.5).toInt()
                track.nodes.add(OsmPathElement.create(ilon, ilat, 0.toShort(), null))
            }
        }
        return track
    }

    companion object {
        /** `FormatGpx.java:537-539`. */
        fun getWaypoint(ilon: Int, ilat: Int, name: String?, desc: String?): String {
            return "<wpt lon=\"" + formatILon(ilon) + "\" lat=\"" + formatILat(ilat) + "\"><name>" + name + "</name>" + (if (desc != null) "<desc>$desc</desc>" else "") + "</wpt>"
        }

        /** `BufferedReader.readLine`'s lines: ended by `\n`, `\r` or `\r\n`; a last line without an end counts. */
        private fun readLines(text: String): List<String> {
            val out = ArrayList<String>()
            var start = 0
            var i = 0
            while (i < text.length) {
                val c = text[i]
                if (c == '\n' || c == '\r') {
                    out += text.substring(start, i)
                    if (c == '\r' && i + 1 < text.length && text[i + 1] == '\n') i++
                    start = i + 1
                }
                i++
            }
            if (start < text.length) out += text.substring(start)
            return out
        }
    }
}
