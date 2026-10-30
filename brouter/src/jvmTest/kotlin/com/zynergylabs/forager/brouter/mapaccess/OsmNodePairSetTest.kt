package com.zynergylabs.forager.brouter.mapaccess

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Item I, step 5b: `OsmNodePairSet`, which `RoutingEngine` keeps as its island-pair memory (`RoutingEngine.java:51,
 * 1815`) and `WaypointMatcherImpl` reads. Seeded operations on both engines, every answer compared as it is given:
 * temporary pairs up to and past the capacity (`OsmNodePairSet.java:33`), freezing, clearing, `hasPair` both ways
 * round on present and absent pairs, `size` and `getFreezeCount`. Node ids come from a small pool so that pairs repeat.
 */
class OsmNodePairSetTest {
    @Test
    fun seededOperationsAnswerAsTheOracleDoes() {
        var hits = 0
        var overflows = 0
        for (seed in 1..20) {
            val r = Random(seed)
            val capacity = if (seed % 4 == 0) 3 else r.nextInt(5, 300)
            val o = btools.mapaccess.OsmNodePairSet(capacity)
            val p = OsmNodePairSet(capacity)
            val ids = LongArray(40) { (r.nextLong() and 0x7fffffffffffL) }
            var temp = 0
            for (op in 0 until 5_000) {
                val at = "seed $seed op $op"
                when (r.nextInt(10)) {
                    in 0..4 -> {
                        val a = ids[r.nextInt(ids.size)]
                        val b = ids[r.nextInt(ids.size)]
                        o.addTempPair(a, b)
                        p.addTempPair(a, b)
                        if (++temp > capacity) overflows++
                    }
                    5 -> {
                        o.freezeTempPairs()
                        p.freezeTempPairs()
                        temp = 0
                    }
                    6 -> {
                        o.clearTempPairs()
                        p.clearTempPairs()
                        temp = 0
                    }
                    else -> {
                        val a = ids[r.nextInt(ids.size)]
                        val b = ids[r.nextInt(ids.size)]
                        val ho = o.hasPair(a, b)
                        assertEquals(ho, p.hasPair(a, b), "$at: hasPair")
                        if (ho) hits++
                    }
                }
                assertEquals(o.size(), p.size(), "$at: size")
                assertEquals(o.freezeCount, p.getFreezeCount(), "$at: getFreezeCount")
            }
        }
        println("5b OsmNodePairSet: 20 seeds, hasPair true $hits times, temporary pairs past capacity $overflows")
        assertTrue(hits > 0 && overflows > 0, "hits $hits, overflows $overflows")
    }
}
