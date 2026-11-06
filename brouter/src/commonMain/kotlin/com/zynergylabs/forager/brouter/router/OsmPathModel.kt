package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.expressions.BExpressionContextNode
import com.zynergylabs.forager.brouter.expressions.BExpressionContextWay

/** Port of BRouter 1.7.10's `btools.router.OsmPathModel`. */
internal abstract class OsmPathModel {
    abstract fun createPrePath(): OsmPrePath?

    abstract fun createPath(): OsmPath

    abstract fun init(expctxWay: BExpressionContextWay?, expctxNode: BExpressionContextNode?, keyValues: Map<String, String>?)
}
