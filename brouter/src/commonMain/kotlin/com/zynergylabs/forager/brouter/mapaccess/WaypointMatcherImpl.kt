package com.zynergylabs.forager.brouter.mapaccess

import com.zynergylabs.forager.brouter.codec.WaypointMatcher

/**
 * BRouter 1.7.10's `btools.mapaccess.WaypointMatcherImpl`, its declaration only: step 5a's `NodesCache.getSegmentFor`
 * reads [useDynamicRange] through a cast. The class is step 5c's (item I); nothing in step 5a constructs one.
 */
internal abstract class WaypointMatcherImpl : WaypointMatcher {
    abstract val useDynamicRange: Boolean
}
