package com.zynergylabs.forager.brouter.mapaccess

/** Port of BRouter 1.7.10's `btools.mapaccess.OsmNodePairSet`. Step 5b's stub (item I): signatures only. */
internal class OsmNodePairSet(maxTempNodeCount: Int) {
    fun addTempPair(n1: Long, n2: Long): Unit = TODO("step 5b")

    fun freezeTempPairs(): Unit = TODO("step 5b")

    fun clearTempPairs(): Unit = TODO("step 5b")

    fun size(): Int = TODO("step 5b")

    fun getFreezeCount(): Int = TODO("step 5b")

    fun hasPair(n1: Long, n2: Long): Boolean = TODO("step 5b")
}
