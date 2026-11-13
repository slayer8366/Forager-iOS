package com.zynergylabs.forager.brouter.mapaccess

/** Port of BRouter 1.7.10's `btools.mapaccess.OsmLinkHolder`. */
internal interface OsmLinkHolder {
    fun setNextForLink(holder: OsmLinkHolder?)

    fun getNextForLink(): OsmLinkHolder?
}
