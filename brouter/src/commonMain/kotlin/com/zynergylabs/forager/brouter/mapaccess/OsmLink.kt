package com.zynergylabs.forager.brouter.mapaccess

/** Port of BRouter 1.7.10's `btools.mapaccess.OsmLink`. Step 5b's stub (item I): signatures only. */
internal open class OsmLink {
    var descriptionBitmap: ByteArray? = null
    var geometry: ByteArray? = null
    internal var n1: OsmNode? = null
    internal var n2: OsmNode? = null
    internal var previous: OsmLink? = null
    internal var next: OsmLink? = null

    constructor()

    constructor(source: OsmNode?, target: OsmNode?) {
        TODO("step 5b")
    }

    fun getTarget(source: OsmNode?): OsmNode? = TODO("step 5b")

    fun getNext(source: OsmNode?): OsmLink? = TODO("step 5b")

    internal fun clear(source: OsmNode?): OsmLink? = TODO("step 5b")

    fun setFirstLinkHolder(holder: OsmLinkHolder?, source: OsmNode?): Unit = TODO("step 5b")

    fun getFirstLinkHolder(source: OsmNode?): OsmLinkHolder? = TODO("step 5b")

    fun isReverse(source: OsmNode?): Boolean = TODO("step 5b")

    fun isBidirectional(): Boolean = TODO("step 5b")

    fun isLinkUnused(): Boolean = TODO("step 5b")

    fun addLinkHolder(holder: OsmLinkHolder, source: OsmNode?): Unit = TODO("step 5b")
}
