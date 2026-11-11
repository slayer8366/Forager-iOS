package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.mapaccess.OsmNode
import com.zynergylabs.forager.brouter.util.LibmSeam
import com.zynergylabs.forager.brouter.util.MsunLibm

/** STUB (step 6, session 16): BRouter 1.7.10's `btools.router.SearchBoundary`, to be ported over `SearchBoundaryTest`. */
internal class SearchBoundary(n: OsmNode, radius: Int, direction: Int, private val libm: LibmSeam = MsunLibm) {
    fun isInBoundary(n: OsmNode, cost: Int): Boolean = TODO("SearchBoundary.isInBoundary stub")

    fun getBoundaryDistance(n: OsmNode): Int = TODO("SearchBoundary.getBoundaryDistance stub")

    companion object {
        fun getFileName(n: OsmNode): String = TODO("SearchBoundary.getFileName stub")
    }
}
