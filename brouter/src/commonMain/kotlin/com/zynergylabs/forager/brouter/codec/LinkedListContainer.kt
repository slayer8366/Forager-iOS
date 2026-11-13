package com.zynergylabs.forager.brouter.codec

/**
 * Port of BRouter 1.7.10's `btools.codec.LinkedListContainer` (`LinkedListContainer.java:6-78`): a list of lists of
 * integers, in one array of (prev, data) cells. `MicroCache2`'s decoder keeps its reverse links here.
 */
internal class LinkedListContainer(nlists: Int, defaultbuffer: IntArray?) {
    private var ia: IntArray = defaultbuffer ?: IntArray(nlists * 4) // prev, data, prev, data, ...
    private var size = 0
    private val startpointer: IntArray = IntArray(nlists) // 0=void, odd=head-data-cell
    private var listpointer = 0

    /** Adds [data] to list [listNr]. */
    fun addDataElement(listNr: Int, data: Int) {
        if (size + 2 > ia.size) {
            resize()
        }
        ia[size++] = startpointer[listNr]
        startpointer[listNr] = size
        ia[size++] = data
    }

    /** Starts reading list [listNr]; returns its number of entries. */
    fun initList(listNr: Int): Int {
        var cnt = 0
        listpointer = startpointer[listNr]
        var lp = listpointer
        while (lp != 0) {
            lp = ia[lp - 1]
            cnt++
        }
        return cnt
    }

    /** The next element of the list [initList] started, last added first; throws when none is left, as Java does. */
    fun getDataElement(): Int {
        if (listpointer == 0) {
            throw IllegalArgumentException("no more element!")
        }
        val data = ia[listpointer]
        listpointer = ia[listpointer - 1]
        return data
    }

    private fun resize() {
        ia = ia.copyOf(2 * ia.size)
    }
}
