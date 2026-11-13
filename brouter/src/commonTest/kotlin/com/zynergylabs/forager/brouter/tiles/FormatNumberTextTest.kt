package com.zynergylabs.forager.brouter.tiles

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Session 60 (RECORD -163): [FormatNumberText]'s recognition on small hand-written GPX and JSON fragments with made-up
 * numbers (no real position). At the twelve places a number becomes its value's bits; outside them it stays text; two
 * texts of the same `Double` that differ only in the last digit at a tie (a `Float` widened to a `Double`, as
 * `FormatGpx:171`'s `ti`) canonicalise alike, and a different value does not. Runs on the JVM and on the simulator.
 */
class FormatNumberTextTest {
    // A Float (0.37f * 28) widened to a Double: its exact decimal lies halfway between two 17-digit decimals, and both
    // read back to the same bits, 4024b85200000000. Built at run time: the digits only, then the point.
    private val tieDigitsEven = "10360000610351562"
    private val tieDigitsOdd = "10360000610351563"
    private fun pointAfter2(digits: String) = digits.substring(0, 2) + "." + digits.substring(2)
    private val tieEven = pointAfter2(tieDigitsEven)
    private val tieOdd = pointAfter2(tieDigitsOdd)
    private val tieBits = "4024b85200000000"

    private fun gpxFragment(ti: String) =
        " <wpt lon=\"1.5\" lat=\"2.5\"><ele>12.25</ele><name>left</name>" +
            "<extensions><locus:rteDistance>40.0</locus:rteDistance><locus:rteTime>$ti</locus:rteTime>" +
            "<locus:rteSpeed>2.0</locus:rteSpeed><locus:rtePointAction>3</locus:rtePointAction></extensions></wpt>\n" +
            "   <trkpt lon=\"1.5\" lat=\"2.5\"><ele>-3.75</ele><extensions><brouter:speed>1.5</brouter:speed>" +
            "<brouter:voicehint>TL;40,3.5</brouter:voicehint></extensions></trkpt>\n" +
            "    <time>12</time>\n    <turn-angle>7.5</turn-angle>\n <desc>9.75</desc>\n"

    @Test
    fun gpxNumbersAtTheTwelvePlacesBecomeBitsAndEverythingElseStaysText() {
        val expected =
            " <wpt lon=\"1.5\" lat=\"2.5\"><ele>4028800000000000</ele><name>left</name>" +
                "<extensions><locus:rteDistance>4044000000000000</locus:rteDistance><locus:rteTime>$tieBits</locus:rteTime>" +
                "<locus:rteSpeed>4000000000000000</locus:rteSpeed><locus:rtePointAction>3</locus:rtePointAction></extensions></wpt>\n" +
                "   <trkpt lon=\"1.5\" lat=\"2.5\"><ele>c00e000000000000</ele><extensions><brouter:speed>3fc00000</brouter:speed>" +
                "<brouter:voicehint>TL;40,3.5</brouter:voicehint></extensions></trkpt>\n" +
                "    <time>12</time>\n    <turn-angle>7.5</turn-angle>\n <desc>9.75</desc>\n"
        assertEquals(expected, FormatNumberText.gpx(gpxFragment(tieEven)))
        assertEquals(expected, FormatNumberText.forDigest(1, gpxFragment(tieEven), false))
    }

    @Test
    fun gpxTieTextsCanonicaliseAlikeAndADifferentValueDoesNot() {
        assertNotEquals(gpxFragment(tieEven), gpxFragment(tieOdd))
        assertEquals(FormatNumberText.gpx(gpxFragment(tieEven)), FormatNumberText.gpx(gpxFragment(tieOdd)))
        assertNotEquals(FormatNumberText.gpx(gpxFragment(tieEven)), FormatNumberText.gpx(gpxFragment("10.5")))
    }

    private fun jsonFragment(e: String) =
        "      \"properties\": {\n        \"track-length\": \"12.5\",\n        \"times\": [0.5, 1.5, 2.5]\n      },\n" +
            "      \"geometry\": {\n        \"type\": \"LineString\",\n        \"coordinates\": [\n" +
            "          [1.5, 2.5, $e],\n          [1.5, 2.5],\n          [3.5, 4.5, 1.5],\n        ]\n      }\n" +
            "      \"geometry\": {\n        \"type\": \"Point\",\n        \"coordinates\": [\n" +
            "          1.5,\n          2.5,\n          12.25\n        ]\n      }\n"

    @Test
    fun jsonThirdCoordinateBecomesBitsAndEverythingElseStaysText() {
        val head = "      \"properties\": {\n        \"track-length\": \"12.5\",\n        \"times\": [0.5, 1.5, 2.5]\n      },\n" +
            "      \"geometry\": {\n        \"type\": \"LineString\",\n        \"coordinates\": [\n"
        val point = "      \"geometry\": {\n        \"type\": \"Point\",\n        \"coordinates\": [\n" +
            "          1.5,\n          2.5,\n          4028800000000000\n        ]\n      }\n"
        assertEquals(
            head + "          [1.5, 2.5, $tieBits],\n          [1.5, 2.5],\n          [3.5, 4.5, 3ff8000000000000],\n        ]\n      }\n" + point,
            FormatNumberText.json(jsonFragment(tieEven), false),
        )
        // showspeed: a LineString's third number is FormatJson.speedText's Float; a Point's elevation is still a Double.
        assertEquals(
            head + "          [1.5, 2.5, 3fc00000],\n          [1.5, 2.5],\n          [3.5, 4.5, 3fc00000],\n        ]\n      }\n" + point,
            FormatNumberText.json(jsonFragment("1.5"), true),
        )
        assertEquals(FormatNumberText.json(jsonFragment(tieEven), false), FormatNumberText.forDigest(2, jsonFragment(tieEven), false))
    }

    @Test
    fun jsonTieTextsCanonicaliseAlikeAndADifferentValueDoesNot() {
        assertNotEquals(jsonFragment(tieEven), jsonFragment(tieOdd))
        assertEquals(FormatNumberText.json(jsonFragment(tieEven), false), FormatNumberText.json(jsonFragment(tieOdd), false))
        assertNotEquals(FormatNumberText.json(jsonFragment(tieEven), false), FormatNumberText.json(jsonFragment("10.5"), false))
    }

    @Test
    fun theOtherOutputsAreUnchanged() {
        for (k in listOf(0, 3, 4)) assertEquals(gpxFragment(tieEven), FormatNumberText.forDigest(k, gpxFragment(tieEven), true))
    }
}
