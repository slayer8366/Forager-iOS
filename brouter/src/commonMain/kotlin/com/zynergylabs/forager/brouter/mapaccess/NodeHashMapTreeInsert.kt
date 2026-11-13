package com.zynergylabs.forager.brouter.mapaccess

import com.zynergylabs.forager.brouter.mapaccess.NodeHashMap.Node
import com.zynergylabs.forager.brouter.mapaccess.NodeHashMap.TreeNode

// `java.util.HashMap.TreeNode`'s lookup and insertion, for [NodeHashMap]. Line numbers are the pinned Temurin
// 21.0.13+9's `java/util/HashMap.java`. `comparableClassFor` is always null for `OsmNode` keys (see [NodeHashMap]), so
// the `kc` branches are folded: an equal-hash comparison goes straight to `pr.find` or to `tieBreakOrder`. `treeify`
// takes the map, which Java's does not, so that it can reach the tie-break's stand-in counter.

/** `root` (`HashMap.java:1979-1985`). */
internal fun TreeNode.root(): TreeNode {
    var r = this
    while (true) {
        r = r.parent ?: return r
    }
}

/** `moveRootToFront` (`HashMap.java:1990-2010`). Its `assert checkInvariants` has no effect on order. */
internal fun moveRootToFront(tab: Array<Node?>?, root: TreeNode?) {
    if (root == null || tab == null || tab.isEmpty()) return
    val n = tab.size
    val index = (n - 1) and root.hash
    val first = tab[index] as TreeNode?
    if (root !== first) {
        tab[index] = root
        val rp = root.prev
        val rn = root.next
        if (rn != null) (rn as TreeNode).prev = rp
        if (rp != null) rp.next = rn
        if (first != null) first.prev = root
        root.next = first
        root.prev = null
    }
}

/** `find` with `kc` null (`HashMap.java:2017-2042`): `compareComparables` is never reached. */
internal fun TreeNode.find(h: Int, k: OsmNode): TreeNode? {
    var p: TreeNode? = this
    do {
        val q: TreeNode = p!!
        val pl = q.left
        val pr = q.right
        val ph = q.hash
        if (ph > h) {
            p = pl
        } else if (ph < h) {
            p = pr
        } else if (q.key === k || k.equals(q.key)) {
            return q
        } else if (pl == null) {
            p = pr
        } else if (pr == null) {
            p = pl
        } else {
            val found = pr.find(h, k)
            if (found != null) return found
            p = pl
        }
    } while (p != null)
    return null
}

/** `getTreeNode` (`HashMap.java:2047-2049`). */
internal fun TreeNode.getTreeNode(h: Int, k: OsmNode): TreeNode? = (if (parent != null) root() else this).find(h, k)

/** `treeify` (`HashMap.java:2071-2111`), with the map passed for `tieBreakOrder`. */
internal fun TreeNode.treeify(map: NodeHashMap, tab: Array<Node?>) {
    var root: TreeNode? = null
    var x: TreeNode? = this
    while (x != null) {
        val next = x.next as TreeNode?
        x.left = null
        x.right = null
        if (root == null) {
            x.parent = null
            x.red = false
            root = x
        } else {
            val k = x.key
            val h = x.hash
            var p: TreeNode = root
            while (true) {
                val pk = p.key
                val ph = p.hash
                val dir = if (ph > h) -1 else if (ph < h) 1 else map.tieBreakOrder(k, pk)
                val xp = p
                val child = if (dir <= 0) p.left else p.right
                if (child == null) {
                    x.parent = xp
                    if (dir <= 0) xp.left = x else xp.right = x
                    root = balanceInsertion(root!!, x)
                    break
                }
                p = child
            }
        }
        x = next
    }
    moveRootToFront(tab, root)
}

/**
 * `putTreeVal` (`HashMap.java:2133-2177`): the existing entry for an equal key, or null after inserting a new one.
 * The equal-hash branch searches both children once (`:2149-2157`) before `tieBreakOrder`, as Java's does.
 */
internal fun TreeNode.putTreeVal(map: NodeHashMap, tab: Array<Node?>, h: Int, k: OsmNode, v: OsmNode): TreeNode? {
    var searched = false
    val root = if (parent != null) root() else this
    var p: TreeNode = root
    while (true) {
        val ph = p.hash
        val dir: Int
        if (ph > h) {
            dir = -1
        } else if (ph < h) {
            dir = 1
        } else {
            val pk = p.key
            if (pk === k || k.equals(pk)) return p
            if (!searched) {
                searched = true
                val l = p.left
                if (l != null) {
                    val q = l.find(h, k)
                    if (q != null) return q
                }
                val r = p.right
                if (r != null) {
                    val q = r.find(h, k)
                    if (q != null) return q
                }
            }
            dir = map.tieBreakOrder(k, pk)
        }
        val xp = p
        val child = if (dir <= 0) p.left else p.right
        if (child == null) {
            val xpn = xp.next
            val x = map.newTreeNode(h, k, v, xpn)
            if (dir <= 0) xp.left = x else xp.right = x
            xp.next = x
            x.parent = xp
            x.prev = xp
            if (xpn != null) (xpn as TreeNode).prev = x
            moveRootToFront(tab, balanceInsertion(root, x))
            return null
        }
        p = child
    }
}

/** `rotateLeft` (`HashMap.java:2347-2363`). */
internal fun rotateLeft(root: TreeNode?, p: TreeNode?): TreeNode? {
    var newRoot = root
    val r = p?.right
    if (p != null && r != null) {
        val rl = r.left
        p.right = rl
        if (rl != null) rl.parent = p
        val pp = p.parent
        r.parent = pp
        if (pp == null) {
            newRoot = r
            r.red = false
        } else if (pp.left === p) {
            pp.left = r
        } else {
            pp.right = r
        }
        r.left = p
        p.parent = r
    }
    return newRoot
}

/** `rotateRight` (`HashMap.java:2365-2381`). */
internal fun rotateRight(root: TreeNode?, p: TreeNode?): TreeNode? {
    var newRoot = root
    val l = p?.left
    if (p != null && l != null) {
        val lr = l.right
        p.left = lr
        if (lr != null) lr.parent = p
        val pp = p.parent
        l.parent = pp
        if (pp == null) {
            newRoot = l
            l.red = false
        } else if (pp.right === p) {
            pp.right = l
        } else {
            pp.left = l
        }
        l.right = p
        p.parent = l
    }
    return newRoot
}

/** `balanceInsertion` (`HashMap.java:2383-2436`). */
internal fun balanceInsertion(rootIn: TreeNode, xIn: TreeNode): TreeNode {
    var root: TreeNode? = rootIn
    var x: TreeNode = xIn
    x.red = true
    while (true) {
        var xp = x.parent
        if (xp == null) {
            x.red = false
            return x
        }
        var xpp = xp.parent
        if (!xp.red || xpp == null) return root!!
        val xppl = xpp.left
        if (xp === xppl) {
            val xppr = xpp.right
            if (xppr != null && xppr.red) {
                xppr.red = false
                xp.red = false
                xpp.red = true
                x = xpp
            } else {
                if (x === xp.right) {
                    x = xp
                    root = rotateLeft(root, x)
                    xp = x.parent
                    xpp = xp?.parent
                }
                if (xp != null) {
                    xp.red = false
                    if (xpp != null) {
                        xpp.red = true
                        root = rotateRight(root, xpp)
                    }
                }
            }
        } else {
            if (xppl != null && xppl.red) {
                xppl.red = false
                xp.red = false
                xpp.red = true
                x = xpp
            } else {
                if (x === xp.left) {
                    x = xp
                    root = rotateRight(root, x)
                    xp = x.parent
                    xpp = xp?.parent
                }
                if (xp != null) {
                    xp.red = false
                    if (xpp != null) {
                        xpp.red = true
                        root = rotateLeft(root, xpp)
                    }
                }
            }
        }
    }
}
