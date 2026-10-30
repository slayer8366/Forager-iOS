package com.zynergylabs.forager.brouter.mapaccess

/**
 * `OsmNodesMap.hmap`: the planner's ruling (RECORD -24) is a common-code copy of `java.util.HashMap`'s order for this one
 * map, option (i). Sized in the step 5b report; **not written until the planner answers** (Amendment 3, Part B). This is
 * the stub the tests compile against: signatures only.
 */
internal class NodeHashMap(initialCapacity: Int) {
    /** Java's `get(key)`: the node equal to [key] (same `ilon` and `ilat`), or null. */
    fun get(key: OsmNode): OsmNode? = TODO("hmap, option (i): waits for the planner's answer")

    /** Java's `put(node, node)`: the previous node equal to [node], or null. */
    fun put(node: OsmNode): OsmNode? = TODO("hmap, option (i): waits for the planner's answer")

    /** Java's `remove(key)`: the removed node, or null. */
    fun remove(key: OsmNode): OsmNode? = TODO("hmap, option (i): waits for the planner's answer")

    /** Java's `values().iterator()`, in `java.util.HashMap`'s order. */
    fun valuesIterator(): Iterator<OsmNode> = TODO("hmap, option (i): waits for the planner's answer")
}
