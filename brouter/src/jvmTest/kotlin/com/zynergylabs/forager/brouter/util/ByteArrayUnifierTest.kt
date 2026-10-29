package com.zynergylabs.forager.brouter.util

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Item I, step 2a: `ByteArrayUnifier`'s one kept method is its constructor (T2's port list), which sets private fields
 * only. Nothing of it is observable, so this is not a differential test: it shows the constructor runs, and that the
 * unkept `unify` fails as a named "unsupported" rather than returning a plausible value.
 */
class ByteArrayUnifierTest {
    @Test
    fun constructsAndRefusesUnifyAsUnsupported() {
        ByteArrayUnifier(16384, false) // OsmNodesMap.java:18's arguments
        val e = assertFailsWith<UnsupportedOperationException> { ByteArrayUnifier(4, true).unify(ByteArray(3), 0, 3) }
        assertTrue(e.message!!.startsWith("unsupported"), e.message)
    }
}
