package com.zynergylabs.forager.brouter.router

/**
 * BRouter 1.7.10's `btools.router.VoiceHint`, whole: a voice hint's input and result data. Step 7, session 20
 * (RECORD -111). Not reached on Forager's path, where BRouter's hints stay off (RECORD -110).
 */
internal class VoiceHint {
    internal var ilon: Int = 0
    internal var ilat: Int = 0
    internal var selev: Short = 0
    internal var cmd: Int = 0
    internal var oldWay: MessageData? = null
    internal var goodWay: MessageData? = null
    internal var badWays: MutableList<MessageData>? = null
    internal var distanceToNext: Double = 0.0
    internal var indexInTrack: Int = 0

    fun getTime(): Float = oldWay?.time ?: 0f

    internal var angle: Float = Float.MAX_VALUE
    internal var lowerBadWayAngle: Float = -181f
    internal var higherBadWayAngle: Float = 181f

    internal var turnAngleConsumed: Boolean = false
    internal var needsRealTurn: Boolean = false
    internal var maxBadPrio: Int = -1

    internal var roundaboutExit: Int = 0

    fun isRoundabout(): Boolean = roundaboutExit != 0

    fun addBadWay(badWay: MessageData?) {
        if (badWay == null) {
            return
        }
        val b = badWays ?: ArrayList<MessageData>().also { badWays = it }
        b.add(badWay)
    }

    fun getExitNumber(): Int = roundaboutExit

    fun calcCommand() {
        val bw = badWays
        if (bw != null) {
            for (badWay in bw) {
                if (badWay.isBadOneway()) {
                    continue
                }
                if (lowerBadWayAngle < badWay.turnangle && badWay.turnangle < goodWay!!.turnangle) {
                    lowerBadWayAngle = badWay.turnangle
                }
                if (higherBadWayAngle > badWay.turnangle && badWay.turnangle > goodWay!!.turnangle) {
                    higherBadWayAngle = badWay.turnangle
                }
            }
        }

        var cmdAngle = angle

        // fall back to local angle if otherwise inconsistent
        if (angle == Float.MAX_VALUE) {
            cmdAngle = goodWay!!.turnangle
        }
        if (cmd == BL) return

        if (roundaboutExit > 0) {
            cmd = RNDB
        } else if (roundaboutExit < 0) {
            cmd = RNLB
        } else if (is180DegAngle(cmdAngle) && cmdAngle <= -179f && higherBadWayAngle == 181f && lowerBadWayAngle == -181f) {
            cmd = TU
        } else if (cmdAngle < -159f) {
            cmd = TLU
        } else if (cmdAngle < -135f) {
            cmd = TSHL
        } else if (cmdAngle < -45f) {
            // a TL can be pushed in either direction by a close-by alternative
            if (cmdAngle < -95f && higherBadWayAngle < -30f && lowerBadWayAngle < -180f) {
                cmd = TSHL
            } else if (cmdAngle > -85f && lowerBadWayAngle > -180f && higherBadWayAngle > -10f) {
                cmd = TSLL
            } else {
                if (cmdAngle < -110f) {
                    cmd = TSHL
                } else if (cmdAngle > -60f) {
                    cmd = TSLL
                } else {
                    cmd = TL
                }
            }
        } else if (cmdAngle < -21f) {
            if (cmd != KR) { // don't overwrite KR with TSLL
                cmd = TSLL
            }
        } else if (cmdAngle < -5f) {
            if (lowerBadWayAngle < -100f && higherBadWayAngle < 45f) {
                cmd = TSLL
            } else if (lowerBadWayAngle >= -100f && higherBadWayAngle < 45f) {
                cmd = KL
            } else {
                if (lowerBadWayAngle > -35f && higherBadWayAngle > 55f) {
                    cmd = KR
                } else {
                    cmd = C
                }
            }
        } else if (cmdAngle < 5f) {
            if (lowerBadWayAngle > -30f) {
                cmd = KR
            } else if (higherBadWayAngle < 30f) {
                cmd = KL
            } else {
                cmd = C
            }
        } else if (cmdAngle < 21f) {
            // a TR can be pushed in either direction by a close-by alternative
            if (lowerBadWayAngle > -45f && higherBadWayAngle > 100f) {
                cmd = TSLR
            } else if (lowerBadWayAngle > -45f && higherBadWayAngle <= 100f) {
                cmd = KR
            } else {
                if (lowerBadWayAngle < -55f && higherBadWayAngle < 35f) {
                    cmd = KL
                } else {
                    cmd = C
                }
            }
        } else if (cmdAngle < 45f) {
            cmd = TSLR
        } else if (cmdAngle < 135f) {
            if (cmdAngle < 85f && higherBadWayAngle < 180f && lowerBadWayAngle < 10f) {
                cmd = TSLR
            } else if (cmdAngle > 95f && lowerBadWayAngle > 30f && higherBadWayAngle > 180f) {
                cmd = TSHR
            } else {
                // Java compares these two with double literals (110., 60.): the float widens, the result is the same.
                if (cmdAngle.toDouble() > 110.0) {
                    cmd = TSHR
                } else if (cmdAngle.toDouble() < 60.0) {
                    cmd = TSLR
                } else {
                    cmd = TR
                }
            }
        } else if (cmdAngle < 159f) {
            cmd = TSHR
        } else if (is180DegAngle(cmdAngle) && cmdAngle >= 179f && higherBadWayAngle == 181f && lowerBadWayAngle == -181f) {
            cmd = TU
        } else {
            cmd = TRU
        }
    }

    fun formatGeometry(): String {
        val oldPrio = oldWay?.priorityclassifier?.toFloat() ?: 0f
        val sb = StringBuilder(30)
        sb.append(' ').append(oldPrio.toInt())
        appendTurnGeometry(sb, goodWay!!)
        val bw = badWays
        if (bw != null) {
            for (badWay in bw) {
                sb.append(" ")
                appendTurnGeometry(sb, badWay)
            }
        }
        return sb.toString()
    }

    private fun appendTurnGeometry(sb: StringBuilder, msg: MessageData) {
        sb.append("(").append((msg.turnangle + 0.5).toInt()).append(")").append(msg.priorityclassifier)
    }

    fun hasGiveWay(): Boolean {
        val o = oldWay
        val nodeKv = o?.nodeKeyValues
        if (o != null && nodeKv != null) {
            val giveWay = nodeKv.contains("highway=give_way") || nodeKv.contains("highway=stop")
            return if (o.wayKeyValues!!.contains("reversedirection=yes")) {
                giveWay && nodeKv.contains("direction=backward")
            } else {
                giveWay && !nodeKv.contains("direction=backward")
            }
        }
        return false
    }

    companion object {
        internal const val C = 1 // continue (go straight)
        internal const val TL = 2 // turn left
        internal const val TSLL = 3 // turn slightly left
        internal const val TSHL = 4 // turn sharply left
        internal const val TR = 5 // turn right
        internal const val TSLR = 6 // turn slightly right
        internal const val TSHR = 7 // turn sharply right
        internal const val KL = 8 // keep left
        internal const val KR = 9 // keep right
        internal const val TLU = 10 // U-turn
        internal const val TRU = 11 // Right U-turn
        internal const val OFFR = 12 // Off route
        internal const val RNDB = 13 // Roundabout
        internal const val RNLB = 14 // Roundabout left
        internal const val TU = 15 // 180 degree u-turn
        internal const val BL = 16 // Beeline routing
        internal const val EL = 17 // exit left
        internal const val ER = 18 // exit right
        internal const val END = 100 // end point

        fun is180DegAngle(angle: Float): Boolean = kotlin.math.abs(angle) <= 180f && kotlin.math.abs(angle) >= 179f
    }
}
