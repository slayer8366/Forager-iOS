package com.zynergylabs.forager.brouter.msun

import okio.Buffer
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Step 2b: the port's `atan2` over the research's 299,974 pairs, as CheapAngleMeter.getAngle calls it
 * (atan2((double) dy, (double) dx)). The port's lines are written as at2.c writes its own ("dy dx bits") and hashed;
 * the SHA-256 must equal the research's for bionic's C (q7/results/sha256-and-wc.txt). jvmTest compares the same pairs
 * row by row against a fresh C build and against StrictMath.atan2, which says which rows differ when this fails.
 */
class MsunAtan2PairsTest {
    @Test
    fun atan2OverThePairsHashesAsBionicsDoes() {
        val out = Buffer()
        var n = 0
        forEachAtan2Pair { dy, dx ->
            out.writeUtf8("$dy $dx ${hex(msunAtan2(dy.toDouble(), dx.toDouble()))}\n")
            n++
        }
        assertEquals(299974, n, "pairs regenerated")
        assertEquals(MSUN_ATAN2_PAIRS_SHA256, out.sha256().hex(), "SHA-256 of the port's at2.c-form lines")
    }
}
