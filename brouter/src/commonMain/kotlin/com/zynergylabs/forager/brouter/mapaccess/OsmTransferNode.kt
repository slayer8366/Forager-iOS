package com.zynergylabs.forager.brouter.mapaccess

/** Port of BRouter 1.7.10's `btools.mapaccess.OsmTransferNode`: one intermediate point of a link's geometry. */
internal class OsmTransferNode {
    var next: OsmTransferNode? = null
    var ilon: Int = 0
    var ilat: Int = 0
    var selev: Short = 0
}
