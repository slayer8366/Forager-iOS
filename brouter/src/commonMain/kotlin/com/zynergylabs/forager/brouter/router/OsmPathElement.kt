package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.mapaccess.OsmPos
import com.zynergylabs.forager.brouter.util.CheapRuler
import com.zynergylabs.forager.brouter.util.LibmSeam
import com.zynergylabs.forager.brouter.util.javaRound
import kotlin.math.max

/**
 * Port of BRouter 1.7.10's `btools.router.OsmPathElement`: a path's element, its position, height and cost, with the
 * methods T2 keeps. Not ported, being unreached on Forager's path: `setAngle`, `writeToStream` and `readFromStream`.
 * [calcDistance] takes the scale table from its seam, as `OsmNode`'s does (step 2b's rule).
 */
internal open class OsmPathElement protected constructor() : OsmPos {
    private var ilat: Int = 0 // latitude
    private var ilon: Int = 0 // longitude
    private var selev: Short = 0 // longitude

    var message: MessageData? = null // description

    var cost: Int = 0

    var origin: OsmPathElement? = null

    // interface OsmPos
    final override fun getILat(): Int = ilat

    final override fun getILon(): Int = ilon

    final override fun getSElev(): Short = selev

    fun setSElev(s: Short) {
        selev = s
    }

    final override fun getElev(): Double = selev / 4.0

    fun getTime(): Float = message?.time ?: 0f

    fun setTime(t: Float) {
        message?.time = t
    }

    fun getEnergy(): Float = message?.energy ?: 0f

    fun setEnergy(e: Float) {
        message?.energy = e
    }

    final override fun getIdFromPos(): Long = (ilon.toLong() shl 32) or ilat.toLong()

    /** Java: `(int) Math.max(1.0, Math.round(CheapRuler.distance(...)))`, with the scale table taken from [libm]. */
    final override fun calcDistance(libm: LibmSeam, p: OsmPos): Int =
        max(1.0, javaRound(CheapRuler.distance(libm, ilon, ilat, p.getILon(), p.getILat())).toDouble()).toInt()

    override fun toString(): String = "${ilon}_$ilat"

    fun positionEquals(e: OsmPathElement): Boolean = this.ilat == e.ilat && this.ilon == e.ilon

    companion object {
        /** `OsmPathElement.java:119-126`; session 23, for `OsmTrack.readBinary`'s file branch (RECORD -111). */
        fun readFromStream(dis: okio.BufferedSource): OsmPathElement =
            TODO("step 7, session 23 stub: OsmPathElement.readFromStream")

        /** `OsmPathElement.java:84-90`. */
        fun create(path: OsmPath): OsmPathElement {
            val n = path.getTargetNode()!!
            val pe = create(n.getILon(), n.getILat(), n.getSElev(), path.originElement)
            pe.cost = path.cost
            pe.message = path.message
            return pe
        }

        /** `OsmPathElement.java:92-99`. */
        fun create(ilon: Int, ilat: Int, selev: Short, origin: OsmPathElement?): OsmPathElement {
            val pe = OsmPathElement()
            pe.ilon = ilon
            pe.ilat = ilat
            pe.selev = selev
            pe.origin = origin
            return pe
        }
    }
}
