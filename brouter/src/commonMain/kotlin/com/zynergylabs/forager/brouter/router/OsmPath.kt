package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.mapaccess.OsmLink
import com.zynergylabs.forager.brouter.mapaccess.OsmLinkHolder
import com.zynergylabs.forager.brouter.mapaccess.OsmNode

/** Port of BRouter 1.7.10's `btools.router.OsmPath`. A stub until step 6 ports it. */
internal abstract class OsmPath : OsmLinkHolder {
    /** `OsmPath.java:19`, `:31` and `:69`, which `OsmPathElement.create` reads; the rest arrive with the port. */
    var cost: Int = 0
    var originElement: OsmPathElement? = null
    var message: MessageData? = null

    fun getTargetNode(): OsmNode? = TODO("step 6: OsmPath")

    open fun init(link: OsmLink): Unit = TODO("step 6: OsmPath")

    open fun init(origin: OsmPath, link: OsmLink, refTrack: OsmTrack?, detailMode: Boolean, rc: RoutingContext): Unit =
        TODO("step 6: OsmPath")

    override fun setNextForLink(holder: OsmLinkHolder?): Unit = TODO("step 6: OsmPath")

    override fun getNextForLink(): OsmLinkHolder? = TODO("step 6: OsmPath")

    fun interpolateEle(e1: Short, e2: Short, fraction: Double): Short = TODO("step 6: OsmPath")
}
