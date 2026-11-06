package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.mapaccess.OsmPos

/**
 * BRouter 1.7.10's `btools.router.OsmTrack`, the type only (RECORD -94): step 6's `OsmPath` takes a reference track,
 * and step 7 ports the class into this same file. On Forager's path the reference track is most likely always null
 * (`alternativeidx=0`; RECORD -94 [I], to be checked at step 8 with the coverage reader).
 */
internal class OsmTrack {
    fun containsNode(node: OsmPos): Boolean = throw NotImplementedError("unsupported: OsmTrack.containsNode, step 7")
}
