package com.zynergylabs.forager.brouter.util

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import btools.util.Crc32 as JCrc32

/** Item I, step 2a: `Crc32.crc` on seeded arrays, offsets and lengths, against the oracle's. */
class Crc32Test {
    @Test
    fun crcOfSeededSlicesEqualsTheOracles() {
        for (seed in 1..2_000) {
            val r = Random(seed)
            val ab = ByteArray(r.nextInt(1, 600)).also { r.nextBytes(it) }
            val offset = r.nextInt(ab.size)
            val len = r.nextInt(ab.size - offset + 1)
            assertEquals(JCrc32.crc(ab, offset, len), Crc32.crc(ab, offset, len), "seed $seed: offset $offset len $len")
        }
        // Every byte value at least once, and the empty slice (the initial register, returned as is).
        val all = ByteArray(256) { it.toByte() }
        assertEquals(JCrc32.crc(all, 0, 256), Crc32.crc(all, 0, 256))
        assertEquals(JCrc32.crc(all, 7, 0), Crc32.crc(all, 7, 0))
    }
}
