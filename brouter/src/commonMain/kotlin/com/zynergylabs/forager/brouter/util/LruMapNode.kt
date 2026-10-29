package com.zynergylabs.forager.brouter.util

// btools/util/LruMapNode.java: fields only, all 9 kept lines (no body to stub).
internal abstract class LruMapNode {
    internal var nextInBin: LruMapNode? = null // next entry for hash-bin
    internal var next: LruMapNode? = null // next in lru sequence (towards mru)
    internal var previous: LruMapNode? = null // previous in lru sequence (towards lru)

    var hash: Int = 0
}
