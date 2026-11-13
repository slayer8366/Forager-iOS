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
 * - `minVisitIdInSubtree` is Java's recursion rewritten as a loop over a stack of its own (RECORD -144, the owner's
 *   ruling), so its depth no longer uses the thread's stack, and `cleanupPeninsulas` has no `StackOverflowError` catch
 *   (`OsmNodesMap.java:62-64`): nothing there can overflow. Where Java's recursion would overflow and its catch skip the
 *   rest of a subtree's cleanup, the port finishes it; elsewhere the walk is Java's, step for step (see the function).
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
                    // Java: try { minVisitIdInSubtree(null, n); } catch (StackOverflowError soe) { }. The walk below keeps
                    // its own stack (RECORD -144), so no stack overflow can reach here and the catch is gone: where Java
                    // overflows and skips the rest of a subtree, the port finishes it (the one behaviour difference).
                    minVisitIdInSubtree(n)
                }
            }
        }
    }

    /**
     * One frame of Java's `minVisitIdInSubtree(source, n)` recursion, held in [walk] instead of on the thread's stack
     * (RECORD -144). [source], [n] and [minId] are the call's parameters and its local; [next] is its loop variable
     * after Java's for-update (`nextLink`); [child], [childLink] and [nodesCreatedUntilHere] are the locals live across
     * the recursive call (`t`, `l`, `nodesCreatedUntilHere`), set while a child's frame is above this one, else null.
     */
    private class WalkFrame {
        var source: OsmNode? = null
        lateinit var n: OsmNode
        var minId = 0
        var next: OsmLink? = null
        var child: OsmNode? = null
        var childLink: OsmLink? = null
        var nodesCreatedUntilHere = 0
    }

    /** The explicit stack: [walk]`[0 until depth]` are the live frames; the objects above `depth` are kept for reuse. */
    private val walk = ArrayList<WalkFrame>()

    /**
     * Java's `minVisitIdInSubtree(null, root)` without recursion (RECORD -144, the owner's ruling). It visits the same
     * nodes in the same order and writes the same fields at the same moments as Java's recursion, when Java does not
     * overflow. How each step of the recursion maps to the loop:
     * - **A call** `minVisitIdInSubtree(source, n)` is `enter`: a frame is pushed and the call's prologue runs there and
     *   then (visitID, minId, `nodesCreated++`), the same moment the call would run it.
     * - **The link loop** runs on the top frame, from its [WalkFrame.next], exactly as Java's for-loop: `nextLink` is taken
     *   before the body, then the same tests in the same order (`t == source`, hollow, `visitID` 1, 0, below baseID,
     *   tree mode), each `continue` going to the next link.
     * - **The recursive call** (`visitID == 0`): the frame saves `t`, `l` and `nodesCreatedUntilHere`, which Java keeps
     *   live across the call, and the child's frame is pushed by `enter`. The loop goes on with the child, so the
     *   child's whole subtree is walked before anything more of the parent's links, as in Java.
     * - **"return minId"**: when a frame's links run out, its minId is the returned value and the frame is popped. If a
     *   frame is below it, that frame resumes exactly where Java's call returns: `minIdSub` is the returned value; if
     *   `minIdSub > n.visitID` (peninsula) `nodesCreated` is restored and the link is unlinked from both ends; then
     *   `if (minIdSub < minId) minId = minIdSub`; then its link loop goes on from the saved `nextLink`.
     * - When the root's frame is popped the walk ends; Java's root call's return value is unused, as here.
     *
     * [PeninsulaDepth] (read by tests only, never by the port) now counts the explicit stack's size: its `current` is the
     * number of live frames, one more on each push and one less on each pop, so its `deepest` is the depth Java's
     * recursion reaches (the root is 1), as the recursive port counted it before.
     */
    private fun minVisitIdInSubtree(root: OsmNode) {
        var depth = 0
        fun enter(source: OsmNode?, n: OsmNode) {
            if (depth == walk.size) walk.add(WalkFrame())
            val f = walk[depth++]
            if (++PeninsulaDepth.current > PeninsulaDepth.deepest) PeninsulaDepth.deepest = PeninsulaDepth.current
            // The call's prologue, at the moment of the call.
            if (n.visitID == 1) n.visitID = baseID // border node
            else n.visitID = lastVisitID++
            f.source = source
            f.n = n
            f.minId = n.visitID
            f.next = n.firstlink
            f.child = null
            f.childLink = null
            nodesCreated++
        }

        enter(null, root)
        var returned = 0 // the value the last popped frame returned
        while (depth > 0) {
            val f = walk[depth - 1]
            val n = f.n
            val returnedFrom = f.child
            if (returnedFrom != null) {
                // Where Java's recursive call returns: minIdSub = minVisitIdInSubtree(n, t).
                val link = f.childLink!!
                val minIdSub = returned
                if (minIdSub > n.visitID) { // peninsula ?
                    nodesCreated = f.nodesCreatedUntilHere
                    n.unlinkLink(link)
                    returnedFrom.unlinkLink(link)
                }
                if (minIdSub < f.minId) f.minId = minIdSub
                f.child = null
                f.childLink = null
            }
            var descended = false
            var l = f.next
            while (l != null) {
                val link: OsmLink = l
                l = link.getNext(n) // Java's for-update, taken before the body so that `continue` keeps it
                f.next = l

                val t = link.getTarget(n)!!
                if (t === f.source) continue
                if (t.isHollow()) continue

                var minIdSub = t.visitID
                if (minIdSub == 1) {
                    minIdSub = baseID
                } else if (minIdSub == 0) {
                    // Java's recursive call: keep what is live across it, then walk the child's subtree first.
                    f.nodesCreatedUntilHere = nodesCreated
                    f.child = t
                    f.childLink = link
                    enter(n, t)
                    descended = true
                    break
                } else if (minIdSub < baseID) {
                    continue
                } else if (cleanupMode == 2) {
                    minIdSub = baseID // in tree-mode, hitting anything is like a gateway
                }
                if (minIdSub < f.minId) f.minId = minIdSub
            }
            if (!descended) {
                // Java's "return minId": pop the frame; the frame below, if any, resumes at its call's return.
                returned = f.minId
                f.source = null
                depth--
                PeninsulaDepth.current--
            }
        }
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
