package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.expressions.BExpressionContextNode
import com.zynergylabs.forager.brouter.expressions.BExpressionContextWay

/** Port of BRouter 1.7.10's `btools.router.StdModel`, the path model of every profile without a `---model:` line. */
internal class StdModel : OsmPathModel() {
    override fun createPrePath(): OsmPrePath? = null

    override fun createPath(): OsmPath = StdPath()

    var ctxWay: BExpressionContextWay? = null
    var ctxNode: BExpressionContextNode? = null

    override fun init(expctxWay: BExpressionContextWay?, expctxNode: BExpressionContextNode?, keyValues: Map<String, String>?) {
        ctxWay = expctxWay
        ctxNode = expctxNode
        // Java's `BExpressionContext expctxGlobal = expctxWay;` is a dead local; not ported.
    }
}
