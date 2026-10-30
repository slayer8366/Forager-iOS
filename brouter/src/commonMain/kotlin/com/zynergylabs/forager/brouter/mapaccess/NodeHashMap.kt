package com.zynergylabs.forager.brouter.mapaccess

/**
 * `OsmNodesMap.hmap` (`OsmNodesMap.java:16`, `new HashMap<OsmNode, OsmNode>(4096)`), ported as option (i) of RECORD -24:
 * a common-code copy of `java.util.HashMap`'s order for this one map, so that `values()` iterates as the oracle's does.
 * The source is the pinned Temurin 21.0.13+9's `java/util/HashMap.java` (SHA-256 `71c8d822…`, step 5b report); line
 * numbers below are that file's.
 *
 * Only what `OsmNodesMap` reaches is ported: `get`, `put`, `remove` and `values().iterator()`, with the tree bins they
 * reach. The tree code is in `NodeHashMapTreeInsert.kt` and `NodeHashMapTreeRemove.kt`. Keys and values stay separate
 * (RECORD -50), since `NodesCache.getGraphNode` puts an existing value back under a new, equal key
 * (`NodesCache.java:284-288`).
 *
 * Two places where `java.util.HashMap`'s behaviour is not a function of the keys alone, both named:
 * - **`comparableClassFor` is always null.** `OsmNode` is not `Comparable`, so `HashMap.java:346` returns null and
 *   `compareComparables` is never reached. Every equal-hash comparison in a tree bin goes straight to [tieBreakOrder].
 * - **[tieBreakOrder]'s identity hash** has a fixed stand-in, below.
 */
internal class NodeHashMap(initialCapacity: Int) {
    /** `Node<K,V>` (`HashMap.java:281-292`). Only the fields; the `Map.Entry` methods are not reached. */
    internal open class Node(val hash: Int, val key: OsmNode, var value: OsmNode, var next: Node?)

    /**
     * `TreeNode<K,V>` (`HashMap.java:1966-1974`). Java's extends `LinkedHashMap.Entry`, whose `before`/`after` links only
     * `LinkedHashMap` uses; they are not ported.
     */
    internal class TreeNode(hash: Int, key: OsmNode, value: OsmNode, next: Node?) : Node(hash, key, value, next) {
        var parent: TreeNode? = null
        var left: TreeNode? = null
        var right: TreeNode? = null
        var prev: TreeNode? = null // needed to unlink next upon deletion
        var red: Boolean = false
    }

    private var table: Array<Node?>? = null
    private var size = 0
    private var modCount = 0
    private var threshold: Int
    private val loadFactor: Float = DEFAULT_LOAD_FACTOR

    /** How many keys this map has given a tie-break number ([identityStandIn]). Read by the determinism test. */
    var tieBreakIdsAssigned: Int = 0
        private set

    /** How many times [tieBreakOrder] has run in this map. Read by the determinism test, to see which paths tie. */
    var tieBreakCalls: Int = 0
        private set

    init {
        // HashMap(int) -> HashMap(int, float) (`HashMap.java:445-456`, `:469-471`).
        require(initialCapacity >= 0) { "Illegal initial capacity: $initialCapacity" }
        threshold = tableSizeFor(if (initialCapacity > MAXIMUM_CAPACITY) MAXIMUM_CAPACITY else initialCapacity)
    }

    /** Java's `get(key)` (`HashMap.java:562-565`): the value whose key equals [key] (same `ilon` and `ilat`), or null. */
    fun get(key: OsmNode): OsmNode? = getNode(key)?.value

    /** `getNode` (`HashMap.java:573-591`). */
    private fun getNode(key: OsmNode): Node? {
        val tab = table ?: return null
        val n = tab.size
        if (n <= 0) return null
        val hash = hash(key)
        val first = tab[(n - 1) and hash] ?: return null
        if (first.hash == hash && (first.key === key || key.equals(first.key))) return first // always check first node
        var e = first.next
        if (e != null) {
            if (first is TreeNode) return first.getTreeNode(hash, key)
            do {
                if (e!!.hash == hash && (e.key === key || key.equals(e.key))) return e
                e = e.next
            } while (e != null)
        }
        return null
    }

    /** Java's `put(node, node)` (`HashMap.java:617-619`): the previous value under a key equal to [node], or null. */
    fun put(node: OsmNode): OsmNode? = putVal(hash(node), node, node)

    /** `putVal` with `onlyIfAbsent = false`, `evict = true` (`HashMap.java:631-672`). */
    private fun putVal(hash: Int, key: OsmNode, value: OsmNode): OsmNode? {
        var tab = table
        if (tab == null || tab.isEmpty()) {
            tab = resize()
        }
        val n = tab.size
        val i = (n - 1) and hash
        var p = tab[i]
        if (p == null) {
            tab[i] = newNode(hash, key, value, null)
        } else {
            var e: Node?
            if (p.hash == hash && (p.key === key || key.equals(p.key))) {
                e = p
            } else if (p is TreeNode) {
                e = p.putTreeVal(this, tab, hash, key, value)
            } else {
                var binCount = 0
                while (true) {
                    e = p!!.next
                    if (e == null) {
                        p.next = newNode(hash, key, value, null)
                        if (binCount >= TREEIFY_THRESHOLD - 1) treeifyBin(tab, hash) // -1 for 1st
                        break
                    }
                    if (e.hash == hash && (e.key === key || key.equals(e.key))) break
                    p = e
                    ++binCount
                }
            }
            if (e != null) { // existing mapping for key
                val oldValue = e.value
                e.value = value // onlyIfAbsent is false
                return oldValue
            }
        }
        ++modCount
        if (++size > threshold) resize()
        return null
    }

    /** `resize` (`HashMap.java:683-755`). */
    private fun resize(): Array<Node?> {
        val oldTab = table
        val oldCap = oldTab?.size ?: 0
        val oldThr = threshold
        var newCap = 0
        var newThr = 0
        if (oldCap > 0) {
            if (oldCap >= MAXIMUM_CAPACITY) {
                threshold = Int.MAX_VALUE
                return oldTab!!
            }
            newCap = oldCap shl 1
            if (newCap < MAXIMUM_CAPACITY && oldCap >= DEFAULT_INITIAL_CAPACITY) newThr = oldThr shl 1 // double threshold
        } else if (oldThr > 0) { // initial capacity was placed in threshold
            newCap = oldThr
        } else { // zero initial threshold signifies using defaults
            newCap = DEFAULT_INITIAL_CAPACITY
            newThr = (DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY).toInt()
        }
        if (newThr == 0) {
            val ft = newCap.toFloat() * loadFactor
            newThr = if (newCap < MAXIMUM_CAPACITY && ft < MAXIMUM_CAPACITY.toFloat()) ft.toInt() else Int.MAX_VALUE
        }
        threshold = newThr
        val newTab = arrayOfNulls<Node>(newCap)
        table = newTab
        if (oldTab != null) {
            for (j in 0 until oldCap) {
                var e = oldTab[j] ?: continue
                oldTab[j] = null
                if (e.next == null) {
                    newTab[e.hash and (newCap - 1)] = e
                } else if (e is TreeNode) {
                    e.split(this, newTab, j, oldCap)
                } else { // preserve order
                    var loHead: Node? = null
                    var loTail: Node? = null
                    var hiHead: Node? = null
                    var hiTail: Node? = null
                    while (true) {
                        val next = e.next
                        if ((e.hash and oldCap) == 0) {
                            if (loTail == null) loHead = e else loTail.next = e
                            loTail = e
                        } else {
                            if (hiTail == null) hiHead = e else hiTail.next = e
                            hiTail = e
                        }
                        e = next ?: break
                    }
                    if (loTail != null) {
                        loTail.next = null
                        newTab[j] = loHead
                    }
                    if (hiTail != null) {
                        hiTail.next = null
                        newTab[j + oldCap] = hiHead
                    }
                }
            }
        }
        return newTab
    }

    /** `treeifyBin` (`HashMap.java:761-780`). */
    private fun treeifyBin(tab: Array<Node?>, hash: Int) {
        val n = tab.size
        if (n < MIN_TREEIFY_CAPACITY) {
            resize()
            return
        }
        val index = (n - 1) and hash
        var e = tab[index] ?: return
        var hd: TreeNode? = null
        var tl: TreeNode? = null
        while (true) {
            val p = replacementTreeNode(e, null)
            if (tl == null) {
                hd = p
            } else {
                p.prev = tl
                tl.next = p
            }
            tl = p
            e = e.next ?: break
        }
        tab[index] = hd
        if (hd != null) hd.treeify(this, tab)
    }

    /** Java's `remove(key)` (`HashMap.java:803-807`): the removed value, or null. */
    fun remove(key: OsmNode): OsmNode? = removeNode(hash(key), key)?.value

    /** `removeNode` with `value = null`, `matchValue = false`, `movable = true` (`HashMap.java:819-858`). */
    private fun removeNode(hash: Int, key: OsmNode): Node? {
        val tab = table ?: return null
        val n = tab.size
        if (n <= 0) return null
        val index = (n - 1) and hash
        var p = tab[index] ?: return null
        var node: Node? = null
        if (p.hash == hash && (p.key === key || key.equals(p.key))) {
            node = p
        } else {
            var e = p.next
            if (e != null) {
                if (p is TreeNode) {
                    node = p.getTreeNode(hash, key)
                } else {
                    do {
                        if (e!!.hash == hash && (e.key === key || key.equals(e.key))) {
                            node = e
                            break
                        }
                        p = e
                        e = e.next
                    } while (e != null)
                }
            }
        }
        if (node != null) { // matchValue is false
            if (node is TreeNode) {
                node.removeTreeNode(this, tab, true)
            } else if (node === p) {
                tab[index] = node.next
            } else {
                p.next = node.next
            }
            ++modCount
            --size
            return node
        }
        return null
    }

    /**
     * Java's `values().iterator()` (`HashMap.java:1039-1051`, `ValueIterator` at `:1631-1634`), in `java.util.HashMap`'s
     * order. `HashIterator`'s fail-fast check is kept (`:1604-1605`); its `remove` (`:1614-1623`) is not reached.
     */
    fun valuesIterator(): Iterator<OsmNode> = ValueIterator()

    /** `HashIterator` (`HashMap.java:1581-1612`) with `ValueIterator`'s `next`. */
    private inner class ValueIterator : Iterator<OsmNode> {
        private var next: Node? = null
        private val expectedModCount = modCount
        private var index = 0

        init {
            val t = table
            if (t != null && size > 0) { // advance to first entry
                while (index < t.size) {
                    next = t[index++]
                    if (next != null) break
                }
            }
        }

        override fun hasNext(): Boolean = next != null

        override fun next(): OsmNode {
            val e = next
            if (modCount != expectedModCount) throw ConcurrentModificationException()
            if (e == null) throw NoSuchElementException()
            next = e.next
            val t = table
            if (next == null && t != null) {
                while (index < t.size) {
                    next = t[index++]
                    if (next != null) break
                }
            }
            return e.value
        }
    }

    /** `newNode` (`HashMap.java:1908-1910`). */
    private fun newNode(hash: Int, key: OsmNode, value: OsmNode, next: Node?): Node = Node(hash, key, value, next)

    /** `replacementNode` (`HashMap.java:1913-1915`), used by `untreeify`. */
    internal fun replacementNode(p: Node, next: Node?): Node = Node(p.hash, p.key, p.value, next)

    /** `newTreeNode` (`HashMap.java:1918-1920`), used by `putTreeVal`. */
    internal fun newTreeNode(hash: Int, key: OsmNode, value: OsmNode, next: Node?): TreeNode =
        TreeNode(hash, key, value, next)

    /** `replacementTreeNode` (`HashMap.java:1923-1925`), used by `treeifyBin`. */
    private fun replacementTreeNode(p: Node, next: Node?): TreeNode = TreeNode(p.hash, p.key, p.value, next)

    /**
     * `TreeNode.tieBreakOrder` (`HashMap.java:2058-2066`), reached only for two keys of equal hash in one tree bin.
     *
     * Java first orders by class name (`:2061-2062`). Every key in `hmap` is exactly `OsmNode` (the step 5b report traces
     * every `put`), so that step always reads 0. A key of another class would be ordered by name in Java; the port fails
     * there as a named "unsupported" instead of differing silently (RECORD -53).
     *
     * Java then orders by `System.identityHashCode` (`:2063`), an arbitrary value HotSpot fixes on an object at its first
     * request, which differs from one JVM run to the next. RECORD -50's stand-in: each key gets a number from this map's
     * counter at its first tie-break, kept on the [OsmNode] object itself (RECORD -53), since HotSpot's identity hash
     * belongs to the object and `untreeify`, `treeifyBin` and `split` replace entries. Any consistent numbering is one
     * the oracle could produce in some run, and this one is the same every time. `a` is numbered before `b`, as Java
     * evaluates `:2063` left to right.
     */
    internal fun tieBreakOrder(a: OsmNode, b: OsmNode): Int {
        tieBreakCalls++
        if (a::class != b::class) {
            throw UnsupportedOperationException(
                "unsupported: NodeHashMap.tieBreakOrder on keys of two classes (${a::class.simpleName}, " +
                    "${b::class.simpleName}); Java orders these by class name (HashMap.java:2061)",
            )
        }
        return if (identityStandIn(a) <= identityStandIn(b)) -1 else 1
    }

    /** The stand-in for `System.identityHashCode`: fixed at first request (RECORD -50, -53). */
    private fun identityStandIn(node: OsmNode): Int {
        if (node.tieBreakId == 0) {
            tieBreakIdsAssigned++
            node.tieBreakId = tieBreakIdsAssigned
        }
        return node.tieBreakId
    }

    internal companion object {
        const val DEFAULT_INITIAL_CAPACITY = 1 shl 4 // HashMap.java:238
        const val MAXIMUM_CAPACITY = 1 shl 30 // :245
        const val DEFAULT_LOAD_FACTOR = 0.75f // :250
        const val TREEIFY_THRESHOLD = 8 // :260
        const val UNTREEIFY_THRESHOLD = 6 // :267
        const val MIN_TREEIFY_CAPACITY = 64 // :275

        /** `hash` (`HashMap.java:336-339`); keys are never null here. `OsmNode.hashCode` is `ilon + ilat`. */
        fun hash(key: OsmNode): Int {
            val h = key.hashCode()
            return h xor (h ushr 16)
        }

        /** `tableSizeFor` (`HashMap.java:377-380`). */
        fun tableSizeFor(cap: Int): Int {
            val n = -1 ushr (cap - 1).countLeadingZeroBits()
            return if (n < 0) 1 else if (n >= MAXIMUM_CAPACITY) MAXIMUM_CAPACITY else n + 1
        }
    }
}
