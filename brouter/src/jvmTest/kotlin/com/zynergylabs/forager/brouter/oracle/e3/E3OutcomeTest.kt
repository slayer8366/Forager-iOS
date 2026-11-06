package com.zynergylabs.forager.brouter.oracle.e3

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The tuple's stored line form and its heights digest (RECORD -58). The digest is computed twice, here and by
 * `tools/geojson-heights.py`, which writes F0h's expected values from the command-line router's GeoJSON; the vector's
 * values are that tool's output for the same shorts (`fnv1a_shorts`, run 2026-11-04), so the two cannot drift apart.
 */
class E3OutcomeTest {
    @Test
    fun theHeightsDigestIsTheToolsOnTheSameShorts() {
        assertEquals("cbf29ce484222325", E3Outcome.fnv1aShorts(emptyList()), "FNV-1a's offset basis for no heights")
        val shorts = listOf<Short>(0, 1, -1, Short.MAX_VALUE, Short.MIN_VALUE, 1047, 1655)
        assertEquals("b25849ee14a86736", E3Outcome.fnv1aShorts(shorts))
    }

    @Test
    fun aKindHoldingABarReadsBackWhole() {
        val o = E3Outcome("a|b||c", 3, 245, 59, "e1c87c83aa86a99a", "b25849ee14a86736")
        assertEquals("a|b||c|3|245|59|e1c87c83aa86a99a|b25849ee14a86736", o.toField())
        assertEquals(o, E3Outcome.fromField(o.toField()))
        val route = E3Outcome("route", 226, 5302, 5047, "9ca48d05a90f4dcd", "602d4109bb916eca")
        assertEquals(route, E3Outcome.fromField(route.toField()))
    }
}
