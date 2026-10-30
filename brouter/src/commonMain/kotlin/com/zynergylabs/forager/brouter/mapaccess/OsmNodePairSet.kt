package com.zynergylabs.forager.brouter.mapaccess

import com.zynergylabs.forager.brouter.util.CompactLongMap

/**
 * Port of BRouter 1.7.10's `btools.mapaccess.OsmNodePairSet`: a set of node pairs. `tempSize` and `getMaxTmpNodes` are
 * not reached (T2) and not ported.
 */
internal class OsmNodePairSet(maxTempNodeCount: Int) {
    private val n1a: LongArray
    private val n2a: LongArray
    private var tempNodes = 0
    private var maxTempNodes = 0
    private var npairs = 0
    private var freezecount = 0

    init {
        maxTempNodes = maxTempNodeCount
        n1a = LongArray(maxTempNodes)
        n2a = LongArray(maxTempNodes)
    }

    private class OsmNodePair {
        var node2: Long = 0
        var next: OsmNodePair? = null
    }

    private var map: CompactLongMap<OsmNodePair>? = null

    fun addTempPair(n1: Long, n2: Long) {
        if (tempNodes < maxTempNodes) {
            n1a[tempNodes] = n1
            n2a[tempNodes] = n2
            tempNodes++
        }
    }

    fun freezeTempPairs() {
        freezecount++
        for (i in 0 until tempNodes) {
            addPair(n1a[i], n2a[i])
        }
        tempNodes = 0
    }

    fun clearTempPairs() {
        tempNodes = 0
    }

    private fun addPair(n1: Long, n2: Long) {
        if (map == null) {
            map = CompactLongMap()
        }
        npairs++

        var e = getElement(n1, n2)
        if (e == null) {
            e = OsmNodePair()
            e.node2 = n2

            var e0 = map!!.get(n1)
            if (e0 != null) {
                while (e0!!.next != null) {
                    e0 = e0.next
                }
                e0.next = e
            } else {
                map!!.fastPut(n1, e)
            }
        }
    }

    fun size(): Int = npairs

    fun getFreezeCount(): Int = freezecount

    fun hasPair(n1: Long, n2: Long): Boolean = map != null && (getElement(n1, n2) != null || getElement(n2, n1) != null)

    private fun getElement(n1: Long, n2: Long): OsmNodePair? {
        var e = map!!.get(n1)
        while (e != null) {
            if (e.node2 == n2) {
                return e
            }
            e = e.next
        }
        return null
    }
}
