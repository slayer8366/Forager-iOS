package com.zynergylabs.forager.brouter.mapaccess

import com.zynergylabs.forager.brouter.mapaccess.NodeHashMap.Node
import com.zynergylabs.forager.brouter.mapaccess.NodeHashMap.TreeNode

// `java.util.HashMap.TreeNode`'s removal, untreeing and resize split, for [NodeHashMap]. Line numbers are the pinned
// Temurin 21.0.13+9's `java/util/HashMap.java`.

/** `untreeify` (`HashMap.java:2117-2128`). */
internal fun Node.untreeify(map: NodeHashMap): Node? {
    var hd: Node? = null
    var tl: Node? = null
    var q: Node? = this
    while (q != null) {
        val p = map.replacementNode(q, null)
        if (tl == null) hd = p else tl.next = p
        tl = p
        q = q.next
    }
    return hd
}

/** `removeTreeNode` (`HashMap.java:2189-2285`). */
internal fun TreeNode.removeTreeNode(map: NodeHashMap, tab: Array<Node?>?, movable: Boolean) {
    if (tab == null || tab.isEmpty()) return
    val n = tab.size
    val index = (n - 1) and hash
    var first = tab[index] as TreeNode?
    var root = first
    val succ = next as TreeNode?
    val pred = prev
    if (pred == null) {
        first = succ
        tab[index] = first
    } else {
        pred.next = succ
    }
    if (succ != null) succ.prev = pred
    if (first == null) return
    if (root!!.parent != null) root = root.root()
    if (movable) {
        val rl = root.left
        if (root.right == null || rl == null || rl.left == null) {
            tab[index] = first.untreeify(map) // too small
            return
        }
    }
    val p = this
    val pl = left
    val pr = right
    val replacement: TreeNode
    if (pl != null && pr != null) {
        var s: TreeNode = pr
        while (true) { // find successor
            s = s.left ?: break
        }
        val c = s.red // swap colors
        s.red = p.red
        p.red = c
        val sr = s.right
        val pp = p.parent
        if (s === pr) { // p was s's direct parent
            p.parent = s
            s.right = p
        } else {
            val sp = s.parent
            p.parent = sp
            if (sp != null) {
                if (s === sp.left) sp.left = p else sp.right = p
            }
            s.right = pr
            pr.parent = s
        }
        p.left = null
        p.right = sr
        if (sr != null) sr.parent = p
        s.left = pl
        pl.parent = s
        s.parent = pp
        if (pp == null) {
            root = s
        } else if (p === pp.left) {
            pp.left = s
        } else {
            pp.right = s
        }
        replacement = sr ?: p
    } else if (pl != null) {
        replacement = pl
    } else if (pr != null) {
        replacement = pr
    } else {
        replacement = p
    }
    if (replacement !== p) {
        val pp = p.parent
        replacement.parent = pp
        if (pp == null) {
            root = replacement
            replacement.red = false
        } else if (p === pp.left) {
            pp.left = replacement
        } else {
            pp.right = replacement
        }
        p.left = null
        p.right = null
        p.parent = null
    }

    val r = if (p.red) root else balanceDeletion(root, replacement)

    if (replacement === p) { // detach
        val pp = p.parent
        p.parent = null
        if (pp != null) {
            if (p === pp.left) {
                pp.left = null
            } else if (p === pp.right) {
                pp.right = null
            }
        }
    }
    if (movable) moveRootToFront(tab, r)
}

/** `balanceDeletion` (`HashMap.java:2438-2528`). */
internal fun balanceDeletion(rootIn: TreeNode?, xIn: TreeNode?): TreeNode? {
    var root = rootIn
    var x = xIn
    while (true) {
        if (x == null || x === root) return root
        var xp = x.parent
        if (xp == null) {
            x.red = false
            return x
        } else if (x.red) {
            x.red = false
            return root
        }
        val xpl = xp.left
        if (xpl === x) {
            var xpr = xp.right
            if (xpr != null && xpr.red) {
                xpr.red = false
                xp.red = true
                root = rotateLeft(root, xp)
                xp = x.parent
                xpr = xp?.right
            }
            if (xpr == null) {
                x = xp
            } else {
                val sl = xpr.left
                var sr = xpr.right
                if ((sr == null || !sr.red) && (sl == null || !sl.red)) {
                    xpr.red = true
                    x = xp
                } else {
                    if (sr == null || !sr.red) {
                        if (sl != null) sl.red = false
                        xpr.red = true
                        root = rotateRight(root, xpr)
                        xp = x.parent
                        xpr = xp?.right
                    }
                    if (xpr != null) {
                        xpr.red = xp?.red ?: false
                        sr = xpr.right
                        if (sr != null) sr.red = false
                    }
                    if (xp != null) {
                        xp.red = false
                        root = rotateLeft(root, xp)
                    }
                    x = root
                }
            }
        } else { // symmetric
            var xplv = xpl
            if (xplv != null && xplv.red) {
                xplv.red = false
                xp.red = true
                root = rotateRight(root, xp)
                xp = x.parent
                xplv = xp?.left
            }
            if (xplv == null) {
                x = xp
            } else {
                var sl = xplv.left
                val sr = xplv.right
                if ((sl == null || !sl.red) && (sr == null || !sr.red)) {
                    xplv.red = true
                    x = xp
                } else {
                    if (sl == null || !sl.red) {
                        if (sr != null) sr.red = false
                        xplv.red = true
                        root = rotateLeft(root, xplv)
                        xp = x.parent
                        xplv = xp?.left
                    }
                    if (xplv != null) {
                        xplv.red = xp?.red ?: false
                        sl = xplv.left
                        if (sl != null) sl.red = false
                    }
                    if (xp != null) {
                        xp.red = false
                        root = rotateRight(root, xp)
                    }
                    x = root
                }
            }
        }
    }
}

/**
 * `split` (`HashMap.java:2297-2342`): a tree bin divided between `index` and `index + bit` on a resize. It takes the
 * map, which Java's passes too, here also for `treeify`'s tie-break counter.
 */
internal fun TreeNode.split(map: NodeHashMap, tab: Array<Node?>, index: Int, bit: Int) {
    var loHead: TreeNode? = null
    var loTail: TreeNode? = null
    var hiHead: TreeNode? = null
    var hiTail: TreeNode? = null
    var lc = 0
    var hc = 0
    var e: TreeNode? = this
    while (e != null) {
        val next = e.next as TreeNode?
        e.next = null
        if ((e.hash and bit) == 0) {
            e.prev = loTail
            if (loTail == null) loHead = e else loTail.next = e
            loTail = e
            ++lc
        } else {
            e.prev = hiTail
            if (hiTail == null) hiHead = e else hiTail.next = e
            hiTail = e
            ++hc
        }
        e = next
    }

    if (loHead != null) {
        if (lc <= NodeHashMap.UNTREEIFY_THRESHOLD) {
            tab[index] = loHead.untreeify(map)
        } else {
            tab[index] = loHead
            if (hiHead != null) loHead.treeify(map, tab) // (else is already treeified)
        }
    }
    if (hiHead != null) {
        if (hc <= NodeHashMap.UNTREEIFY_THRESHOLD) {
            tab[index + bit] = hiHead.untreeify(map)
        } else {
            tab[index + bit] = hiHead
            if (loHead != null) hiHead.treeify(map, tab)
        }
    }
}
