package com.zynergylabs.forager.brouter.mapaccess

/**
 * Port of BRouter 1.7.10's `btools.mapaccess.OsmLink`: a link between two nodes, seen from either end. A link knows its
 * two nodes ([n1] the source of the forward direction, [n2] its target) and one chain pointer per direction ([next] in
 * [n1]'s chain, [previous] in [n2]'s). Java's `protected` fields are `internal`: `OsmNode` (a subclass in Java's package)
 * reads them. The commented-out checking variants Java keeps beside three methods are not ported.
 */
internal open class OsmLink {
    /** The way tags, valid for both directions. */
    var descriptionBitmap: ByteArray? = null

    /** The intermediate nodes, null for none, valid for both directions. */
    var geometry: ByteArray? = null

    // a link logically knows only its target, but for the reverse link, source and target are swapped
    internal var n1: OsmNode? = null
    internal var n2: OsmNode? = null

    // same for the next-link-for-node pointer: previous applies to the reverse link
    internal var previous: OsmLink? = null
    internal var next: OsmLink? = null

    private var reverselinkholder: OsmLinkHolder? = null
    private var firstlinkholder: OsmLinkHolder? = null

    constructor()

    constructor(source: OsmNode?, target: OsmNode?) {
        n1 = source
        n2 = target
    }

    /** The relevant target node for the given source. */
    fun getTarget(source: OsmNode?): OsmNode? = if (n2 !== source && n2 != null) n2 else n1

    /** The relevant next pointer for the given source. */
    fun getNext(source: OsmNode?): OsmLink? = if (n2 !== source && n2 != null) next else previous

    /** Resets this link for the given direction. */
    internal fun clear(source: OsmNode?): OsmLink? {
        val n: OsmLink?
        if (n2 != null && n2 !== source) {
            n = next
            next = null
            n2 = null
            firstlinkholder = null
        } else if (n1 != null && n1 !== source) {
            n = previous
            previous = null
            n1 = null
            reverselinkholder = null
        } else {
            throw IllegalArgumentException("internal error: setNext: unknown source")
        }
        if (n1 == null && n2 == null) {
            descriptionBitmap = null
            geometry = null
        }
        return n
    }

    fun setFirstLinkHolder(holder: OsmLinkHolder?, source: OsmNode?) {
        if (n2 != null && n2 !== source) {
            firstlinkholder = holder
        } else if (n1 != null && n1 !== source) {
            reverselinkholder = holder
        } else {
            throw IllegalArgumentException("internal error: setFirstLinkHolder: unknown source")
        }
    }

    fun getFirstLinkHolder(source: OsmNode?): OsmLinkHolder? {
        if (n2 != null && n2 !== source) {
            return firstlinkholder
        } else if (n1 != null && n1 !== source) {
            return reverselinkholder
        } else {
            throw IllegalArgumentException("internal error: getFirstLinkHolder: unknown source")
        }
    }

    fun isReverse(source: OsmNode?): Boolean = n1 !== source && n1 != null

    fun isBidirectional(): Boolean = n1 != null && n2 != null

    fun isLinkUnused(): Boolean = n1 == null && n2 == null

    fun addLinkHolder(holder: OsmLinkHolder, source: OsmNode?) {
        val firstHolder = getFirstLinkHolder(source)
        if (firstHolder != null) {
            holder.setNextForLink(firstHolder)
        }
        setFirstLinkHolder(holder, source)
    }
}
