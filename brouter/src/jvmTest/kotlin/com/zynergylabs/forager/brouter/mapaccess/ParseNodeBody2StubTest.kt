package com.zynergylabs.forager.brouter.mapaccess

import com.zynergylabs.forager.brouter.codec.DataBuffers
import com.zynergylabs.forager.brouter.codec.MicroCache2
import com.zynergylabs.forager.brouter.tiles.TestTiles
import com.zynergylabs.forager.brouter.util.ByteArrayUnifier
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Step 5d, item 0 (RECORD -74): while `OsmNode.parseNodeBody2` is not ported, a `MicroCache2` reaching it through
 * `parseNodeBody` must fail as a named "unsupported", never decode as nothing. Reaches it with the port's first cache of
 * F0's fixture, built as `BRouterRouteWays.kt:84` at Forager `a0535ece` builds it (`hollowNodes = null`).
 * Removed when item 4 ports the method.
 */
class ParseNodeBody2StubTest {
    @Test
    fun stubThrowsNamedUnsupported() {
        var reached = false
        GraphEngines.forEachCache(File(TestTiles.f0, "W75_N40.rd5")) { square, c ->
            if (reached) return@forEachCache
            val mc = square.port.createMicroCache(c.lonIdx, c.latIdx, DataBuffers(), null, null, true, null)
            assertTrue(mc is MicroCache2, "the port did not return a MicroCache2")
            val e = assertFailsWith<NotImplementedError> {
                OsmNode().parseNodeBody(mc, OsmNodesMap(), ByteArrayUnifier(16, false))
            }
            assertEquals("unsupported: parseNodeBody2 not yet ported (step 5d)", e.message)
            reached = true
        }
        assertTrue(reached, "no cache reached parseNodeBody2")
    }
}
