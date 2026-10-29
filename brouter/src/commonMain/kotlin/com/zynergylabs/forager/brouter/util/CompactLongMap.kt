package com.zynergylabs.forager.brouter.util

// Stub (item I, step 2a): btools/util/CompactLongMap.java.
internal open class CompactLongMap<V> {
    open fun put(id: Long, value: V): Boolean = TODO("step 2a stub")
    open fun fastPut(id: Long, value: V): Unit = TODO("step 2a stub")
    open fun get(id: Long): V? = TODO("step 2a stub")
    open fun size(): Int = TODO("step 2a stub")
    fun contains(id: Long): Boolean = TODO("step 2a stub")
    protected open fun contains(id: Long, doPut: Boolean): Boolean = TODO("step 2a stub")
    internal fun moveToFrozenArrays(faid: LongArray, flv: MutableList<V?>): Unit = TODO("step 2a stub")
}
