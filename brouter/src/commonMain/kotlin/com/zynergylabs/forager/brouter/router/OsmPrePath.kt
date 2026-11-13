package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.mapaccess.OsmLink
import com.zynergylabs.forager.brouter.mapaccess.OsmNode

/** Port of BRouter 1.7.10's `btools.router.OsmPrePath`: a light path, for the first segment's angle and priority. */
internal abstract class OsmPrePath {
    protected var sourceNode: OsmNode? = null
    protected var targetNode: OsmNode? = null
    protected var link: OsmLink? = null
    var next: OsmPrePath? = null

    fun init(origin: OsmPath, link: OsmLink, rc: RoutingContext) {
        this.link = link
        this.sourceNode = origin.getTargetNode()
        this.targetNode = link.getTarget(sourceNode)
        initPrePath(origin, rc)
    }

    protected abstract fun initPrePath(origin: OsmPath, rc: RoutingContext)
}
