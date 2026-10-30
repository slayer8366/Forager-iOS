package com.zynergylabs.forager.brouter.mapaccess

import com.zynergylabs.forager.brouter.util.ByteArrayUnifier
import com.zynergylabs.forager.brouter.util.LibmSeam

/**
 * Port of BRouter 1.7.10's `btools.mapaccess.OsmNodesMap`: the hollow nodes of a search, and the bookkeeping that
 * counts and prunes the woven graph. `main` and its `addLinks` helper are Java's own test code and are not ported.
 *
 * Three differences from Java, each named:
 * - [hmap] is option (i) of RECORD -24, `java.util.HashMap`'s order in common code ([NodeHashMap]), as RECORD -50
 *   answers it.
 * - `cleanupPeninsulas` catches `StackOverflowError` in Java (`OsmNodesMap.java:62-64`) and carries on. The port keeps
 *   that catch on the JVM and logs it; on iOS there is no catch, and an overflow ends the process
 *   ([runCatchingStackOverflow], RECORD -50). How deep the recursion goes on the captured tiles is measured in the step
 *   5b report.
 * - [collectOutreachers] takes the [LibmSeam] for `calcDistance`, as `CheapRuler.distance` does.
 */
internal class OsmNodesMap {
    private val hmap = NodeHashMap(4096)

    private val abUnifier = ByteArrayUnifier(16384, false)

    private val testKey = OsmNode()

    var nodesCreated: Int = 0
    var maxmem: Long = 0
    private var currentmaxmem: Long = 4000000 // start with 4 MB
    var lastVisitID: Int = 1000
    var baseID: Int = 1000

    var destination: OsmNode? = null
    var currentPathCost: Int = 0
    var currentMaxCost: Int = 1000000000

    var endNode1: OsmNode? = null
    var endNode2: OsmNode? = null

    var cleanupMode: Int = 0

    fun cleanupAndCount(nodes: Array<OsmNode>) {
        if (cleanupMode == 0) {
            justCount(nodes)
        } else {
            cleanupPeninsulas(nodes)
        }
    }

    private fun justCount(nodes: Array<OsmNode>) {
        for (i in nodes.indices) {
            val n = nodes[i]
            if (n.firstlink != null) {
                nodesCreated++
            }
        }
    }

    private fun cleanupPeninsulas(nodes: Array<OsmNode>) {
        baseID = lastVisitID++
        for (i in nodes.indices) { // loop over nodes again just for housekeeping
            val n = nodes[i]
            if (n.firstlink != null) {
                if (n.visitID == 1) {
                    // Java: try { ... } catch (StackOverflowError soe) { }. JVM only; see the class comment.
                    runCatchingStackOverflow { minVisitIdInSubtree(null, n) }
                }
            }
        }
    }

    private fun minVisitIdInSubtree(source: OsmNode?, n: OsmNode): Int {
        if (n.visitID == 1) n.visitID = baseID // border node
        else n.visitID = lastVisitID++
        var minId = n.visitID
        nodesCreated++

        var l = n.firstlink
        while (l != null) {
            val link: OsmLink = l
            l = link.getNext(n) // Java's for-update, taken before the body so that `continue` keeps it

            val t = link.getTarget(n)!!
            if (t === source) continue
            if (t.isHollow()) continue

            var minIdSub = t.visitID
            if (minIdSub == 1) {
                minIdSub = baseID
            } else if (minIdSub == 0) {
                val nodesCreatedUntilHere = nodesCreated
                minIdSub = minVisitIdInSubtree(n, t)
                if (minIdSub > n.visitID) { // peninsula ?
                    nodesCreated = nodesCreatedUntilHere
                    n.unlinkLink(link)
                    t.unlinkLink(link)
                }
            } else if (minIdSub < baseID) {
                continue
            } else if (cleanupMode == 2) {
                minIdSub = baseID // in tree-mode, hitting anything is like a gateway
            }
            if (minIdSub < minId) minId = minIdSub
        }
        return minId
    }

    fun isInMemoryBounds(npaths: Int, extend: Boolean): Boolean {
        // long total = nodesCreated * 76L + linksCreated * 48L;
        var total = nodesCreated * 95L + npaths * 200L

        if (extend) {
            total += 100000

            // when extending, try to have 1 MB  space
            val delta = total + 1900000 - currentmaxmem
            if (delta > 0) {
                currentmaxmem += delta
                if (currentmaxmem > maxmem) {
                    currentmaxmem = maxmem
                }
            }
        }
        return total <= currentmaxmem
    }

    private var nodes2check: MutableList<OsmNode>? = null

    // is there an escape from this node
    // to a hollow node (or destination node) ?
    fun canEscape(n0: OsmNode): Boolean {
        val nodes2check = nodes2check!!
        var sawLowIDs = false
        lastVisitID++
        nodes2check.clear()
        nodes2check.add(n0)
        while (nodes2check.isNotEmpty()) {
            val n = nodes2check.removeAt(nodes2check.size - 1)
            if (n.visitID < baseID) {
                n.visitID = lastVisitID
                nodesCreated++
                var l = n.firstlink
                while (l != null) {
                    val t = l.getTarget(n)!!
                    nodes2check.add(t)
                    l = l.getNext(n)
                }
            } else if (n.visitID < lastVisitID) {
                sawLowIDs = true
            }
        }
        if (sawLowIDs) {
            return true
        }

        nodes2check.add(n0)
        while (nodes2check.isNotEmpty()) {
            val n = nodes2check.removeAt(nodes2check.size - 1)
            if (n.visitID == lastVisitID) {
                n.visitID = lastVisitID
                nodesCreated--
                var l = n.firstlink
                while (l != null) {
                    val t = l.getTarget(n)!!
                    nodes2check.add(t)
                    l = l.getNext(n)
                }
                n.vanish()
            }
        }

        return false
    }

    private fun addActiveNode(nodes2check: MutableList<OsmNode>, n: OsmNode) {
        n.visitID = lastVisitID
        nodesCreated++
        nodes2check.add(n)
    }

    fun clearTemp() {
        nodes2check = null
    }

    fun collectOutreachers(libm: LibmSeam) {
        val nodes2check = ArrayList<OsmNode>(nodesCreated)
        this.nodes2check = nodes2check
        nodesCreated = 0
        val values = hmap.valuesIterator()
        while (values.hasNext()) {
            addActiveNode(nodes2check, values.next())
        }

        lastVisitID++
        baseID = lastVisitID

        while (nodes2check.isNotEmpty()) {
            val n = nodes2check.removeAt(nodes2check.size - 1)
            n.visitID = lastVisitID

            var l = n.firstlink
            while (l != null) {
                val t = l.getTarget(n)!!
                if (t.visitID != lastVisitID) {
                    addActiveNode(nodes2check, t)
                }
                l = l.getNext(n)
            }
            val destination = destination
            if (destination != null && currentMaxCost < 1000000000) {
                val distance = n.calcDistance(libm, destination)
                if (distance > currentMaxCost - currentPathCost + 100) {
                    n.vanish()
                }
            }
            if (n.firstlink == null) {
                nodesCreated--
            }
        }
    }

    fun getByteArrayUnifier(): ByteArrayUnifier = abUnifier

    /** The node at the given position, if the map holds one. */
    fun get(ilon: Int, ilat: Int): OsmNode? {
        testKey.ilon = ilon
        testKey.ilat = ilat
        return hmap.get(testKey)
    }

    fun remove(node: OsmNode) {
        if (node !== endNode1 && node !== endNode2) { // keep endnodes in hollow-map even when loaded
            hmap.remove(node) // (needed for escape analysis)
        }
    }

    /** Puts a node into the map; returns the previous node equal to it, else null. */
    fun put(node: OsmNode): OsmNode? = hmap.put(node)
}
