package com.zynergylabs.forager.brouter.util

/**
 * Port of btools/util/SortedHeap.java (by ab): memory efficient and lightning fast heap to get the lowest-key value of a
 * set of key-object pairs. Kept: the methods T2 reaches; `getPeakSize`, `getExtract` and `SortedBin.add` are not ported.
 */
internal class SortedHeap<V> {
    private var size = 0
    private var peaksize = 0 // written by sortUp as in Java; its reader, getPeakSize, is not reached
    private lateinit var first: SortedBin
    private lateinit var second: SortedBin
    private var firstNonEmpty: SortedBin? = null

    init {
        clear()
    }

    /**
     * @return the lowest key value, or null if none
     */
    fun popLowestKeyValue(): V? {
        val ne = firstNonEmpty ?: return null
        size--
        val minBin = ne.getMinBin()
        @Suppress("UNCHECKED_CAST")
        return minBin.dropLowest() as V
    }

    private class SortedBin(val binsize: Int, val parent: SortedHeap<*>) {
        var next: SortedBin? = null
        var nextNonEmpty: SortedBin? = null
        val al = IntArray(binsize) // key array
        val vla = arrayOfNulls<Any?>(binsize) // value array
        var lv = 0 // low value
        var lp = binsize // low pointer

        fun next(): SortedBin {
            val n = next
            if (n != null) return n
            return SortedBin(binsize shl 1, parent).also { next = it }
        }

        fun dropLowest(): Any? {
            val lpOld = lp
            if (++lp == binsize) {
                unlink()
            } else {
                lv = al[lp]
            }
            val res = vla[lpOld]
            vla[lpOld] = null
            return res
        }

        fun unlink() {
            var neBin = parent.firstNonEmpty
            if (neBin === this) {
                parent.firstNonEmpty = nextNonEmpty
                return
            }
            while (true) {
                // Java dereferences neBin unguarded: a bin not in the chain is a NullPointerException there too.
                val next = neBin!!.nextNonEmpty
                if (next === this) {
                    neBin.nextNonEmpty = nextNonEmpty
                    return
                }
                neBin = next
            }
        }

        // unrolled version of SortedBin.add (not ported) for binsize = 4
        fun add4(key: Int, value: Any?) {
            var p = lp--
            if (p == 4 || key < al[p]) {
                al[p - 1] = key
                lv = key
                vla[p - 1] = value
                return
            }
            al[p - 1] = al[p]
            lv = al[p - 1]
            vla[p - 1] = vla[p]
            p++

            if (p == 4 || key < al[p]) {
                al[p - 1] = key
                vla[p - 1] = value
                return
            }
            al[p - 1] = al[p]
            vla[p - 1] = vla[p]
            p++

            if (p == 4 || key < al[p]) {
                al[p - 1] = key
                vla[p - 1] = value
                return
            }
            al[p - 1] = al[p]
            vla[p - 1] = vla[p]

            al[p] = key
            vla[p] = value
        }

        // unrolled loop for performance sake: 31 steps, as in Java (SortedHeap.java:138-199)
        fun getMinBin(): SortedBin {
            var minBin = this
            var bin = this
            bin = bin.nextNonEmpty ?: return minBin
            if (bin.lv < minBin.lv) minBin = bin
            bin = bin.nextNonEmpty ?: return minBin
            if (bin.lv < minBin.lv) minBin = bin
            bin = bin.nextNonEmpty ?: return minBin
            if (bin.lv < minBin.lv) minBin = bin
            bin = bin.nextNonEmpty ?: return minBin
            if (bin.lv < minBin.lv) minBin = bin
            bin = bin.nextNonEmpty ?: return minBin
            if (bin.lv < minBin.lv) minBin = bin
            bin = bin.nextNonEmpty ?: return minBin
            if (bin.lv < minBin.lv) minBin = bin
            bin = bin.nextNonEmpty ?: return minBin
            if (bin.lv < minBin.lv) minBin = bin
            bin = bin.nextNonEmpty ?: return minBin
            if (bin.lv < minBin.lv) minBin = bin
            bin = bin.nextNonEmpty ?: return minBin
            if (bin.lv < minBin.lv) minBin = bin
            bin = bin.nextNonEmpty ?: return minBin
            if (bin.lv < minBin.lv) minBin = bin
            bin = bin.nextNonEmpty ?: return minBin
            if (bin.lv < minBin.lv) minBin = bin
            bin = bin.nextNonEmpty ?: return minBin
            if (bin.lv < minBin.lv) minBin = bin
            bin = bin.nextNonEmpty ?: return minBin
            if (bin.lv < minBin.lv) minBin = bin
            bin = bin.nextNonEmpty ?: return minBin
            if (bin.lv < minBin.lv) minBin = bin
            bin = bin.nextNonEmpty ?: return minBin
            if (bin.lv < minBin.lv) minBin = bin
            bin = bin.nextNonEmpty ?: return minBin
            if (bin.lv < minBin.lv) minBin = bin
            bin = bin.nextNonEmpty ?: return minBin
            if (bin.lv < minBin.lv) minBin = bin
            bin = bin.nextNonEmpty ?: return minBin
            if (bin.lv < minBin.lv) minBin = bin
            bin = bin.nextNonEmpty ?: return minBin
            if (bin.lv < minBin.lv) minBin = bin
            bin = bin.nextNonEmpty ?: return minBin
            if (bin.lv < minBin.lv) minBin = bin
            bin = bin.nextNonEmpty ?: return minBin
            if (bin.lv < minBin.lv) minBin = bin
            bin = bin.nextNonEmpty ?: return minBin
            if (bin.lv < minBin.lv) minBin = bin
            bin = bin.nextNonEmpty ?: return minBin
            if (bin.lv < minBin.lv) minBin = bin
            bin = bin.nextNonEmpty ?: return minBin
            if (bin.lv < minBin.lv) minBin = bin
            bin = bin.nextNonEmpty ?: return minBin
            if (bin.lv < minBin.lv) minBin = bin
            bin = bin.nextNonEmpty ?: return minBin
            if (bin.lv < minBin.lv) minBin = bin
            bin = bin.nextNonEmpty ?: return minBin
            if (bin.lv < minBin.lv) minBin = bin
            bin = bin.nextNonEmpty ?: return minBin
            if (bin.lv < minBin.lv) minBin = bin
            bin = bin.nextNonEmpty ?: return minBin
            if (bin.lv < minBin.lv) minBin = bin
            bin = bin.nextNonEmpty ?: return minBin
            if (bin.lv < minBin.lv) minBin = bin
            bin = bin.nextNonEmpty ?: return minBin
            if (bin.lv < minBin.lv) minBin = bin
            return minBin
        }
    }

    /**
     * add a key value pair to the heap
     *
     * @param key   the key to insert
     * @param value the value to insert object
     */
    fun add(key: Int, value: V) {
        size++

        if (first.lp == 0 && second.lp == 0) { // both full ?
            sortUp()
        }
        if (first.lp > 0) {
            first.add4(key, value)
            if (firstNonEmpty !== first) {
                first.nextNonEmpty = firstNonEmpty
                firstNonEmpty = first
            }
        } else { // second bin not full
            second.add4(key, value)
            if (first.nextNonEmpty !== second) {
                second.nextNonEmpty = first.nextNonEmpty
                first.nextNonEmpty = second
            }
        }
    }

    private fun sortUp() {
        if (size > peaksize) {
            peaksize = size
        }

        // determine the first array big enough to take them all
        var cnt = 8 // value count of first 2 bins is always 8
        var tbin = second // target bin
        var lastNonEmpty = second
        do {
            tbin = tbin.next()
            val nentries = tbin.binsize - tbin.lp
            if (nentries > 0) {
                cnt += nentries
                lastNonEmpty = tbin
            }
        } while (cnt > tbin.binsize)

        val al_t = tbin.al
        val vla_t = tbin.vla
        var tp = tbin.binsize - cnt // target pointer

        // unlink any higher, non-empty arrays
        val otherNonEmpty = lastNonEmpty.nextNonEmpty
        lastNonEmpty.nextNonEmpty = null

        // now merge the content of these non-empty bins into the target bin
        while (true) {
            val ne = firstNonEmpty ?: break
            // copy current minimum to target array
            val minBin = ne.getMinBin()
            al_t[tp] = minBin.lv
            vla_t[tp++] = minBin.dropLowest()
        }

        tp = tbin.binsize - cnt
        tbin.lp = tp // new target low pointer
        tbin.lv = tbin.al[tp]
        tbin.nextNonEmpty = otherNonEmpty
        firstNonEmpty = tbin
    }

    fun clear() {
        size = 0
        first = SortedBin(4, this)
        second = SortedBin(4, this)
        firstNonEmpty = null
    }

    fun getSize(): Int {
        return size
    }
}
