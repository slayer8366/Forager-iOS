package com.zynergylabs.forager.brouter.codec

/**
 * BRouter 1.7.10's `btools.codec.MicroCache2`, its declaration only (item I, step 3). T2 reaches none of its seven
 * methods: the engine decodes with `DirectWeaver`, and `OsmNode.parseNodeBody`'s `instanceof MicroCache2` needs the type
 * alone. No constructor is reached, so none is callable.
 */
internal class MicroCache2 private constructor() : MicroCache(null)
