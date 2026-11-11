package com.zynergylabs.forager.brouter.router

/**
 * Port of BRouter 1.7.10's `btools.router.MessageData`: the fields and the methods T2 keeps (`copy`, `getPrio`,
 * `isBadOneway`, `isRoundabout`, `isLinktType`, `isGoodForCars`, `toString`). Not ported, being unreached on Forager's
 * path: `isGoodOneway`. Session 24: `toMessage` and `add`, for `FormatCsv` (RECORD -111). Java's `clone()` is a field-by-field copy, as [copy] is.
 * `toString` formats `turnangle` with Kotlin's `Float.toString`, which is Java's on the JVM; on native it is [?].
 */
internal class MessageData {
    var linkdist: Int = 0
    var linkelevationcost: Int = 0
    var linkturncost: Int = 0
    var linknodecost: Int = 0
    var linkinitcost: Int = 0

    var costfactor: Float = 0f
    var priorityclassifier: Int = 0
    var classifiermask: Int = 0
    var turnangle: Float = 0f
    var wayKeyValues: String? = null
    var nodeKeyValues: String? = null

    var lon: Int = 0
    var lat: Int = 0
    var ele: Short = 0

    var time: Float = 0f
    var energy: Float = 0f

    // speed profile
    var vmaxExplicit: Int = -1
    var vmax: Int = -1
    var vmin: Int = -1
    var vnode0: Int = 999
    var vnode1: Int = 999
    var extraTime: Int = 0

    /** `MessageData.java:38-57`. */
    fun toMessage(): String? = TODO("step 7, session 24 stub: MessageData.toMessage")

    /** `MessageData.java:59-65`. */
    fun add(d: MessageData): Unit = TODO("step 7, session 24 stub: MessageData.add")

    /** `MessageData.java:67-73`: Java's shallow `clone()`. */
    fun copy(): MessageData {
        val d = MessageData()
        d.linkdist = linkdist
        d.linkelevationcost = linkelevationcost
        d.linkturncost = linkturncost
        d.linknodecost = linknodecost
        d.linkinitcost = linkinitcost
        d.costfactor = costfactor
        d.priorityclassifier = priorityclassifier
        d.classifiermask = classifiermask
        d.turnangle = turnangle
        d.wayKeyValues = wayKeyValues
        d.nodeKeyValues = nodeKeyValues
        d.lon = lon
        d.lat = lat
        d.ele = ele
        d.time = time
        d.energy = energy
        d.vmaxExplicit = vmaxExplicit
        d.vmax = vmax
        d.vmin = vmin
        d.vnode0 = vnode0
        d.vnode1 = vnode1
        d.extraTime = extraTime
        return d
    }

    override fun toString(): String = "dist=$linkdist prio=$priorityclassifier turn=$turnangle"

    fun getPrio(): Int = priorityclassifier

    fun isBadOneway(): Boolean = (classifiermask and 1) != 0

    fun isRoundabout(): Boolean = (classifiermask and 4) != 0

    fun isLinktType(): Boolean = (classifiermask and 8) != 0

    fun isGoodForCars(): Boolean = (classifiermask and 16) != 0
}
