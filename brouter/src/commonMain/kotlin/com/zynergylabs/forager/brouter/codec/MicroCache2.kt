package com.zynergylabs.forager.brouter.codec

import com.zynergylabs.forager.brouter.util.IByteArrayUnifier

/**
 * Port of BRouter 1.7.10's `btools.codec.MicroCache2`, the four methods `BRouterRouteWays.read` reaches (RECORD -72,
 * `out/RW/not-ported.tsv`): the decoding constructor (`MicroCache2.java:35-249`), `readUnified`, `expandId` and
 * `shrinkId`. `OsmFile.createMicroCache` builds one when it is given no hollow-nodes map.
 *
 * Not ported, as nothing reached calls them: the `(size, databuffer, lonIdx, latIdx, divisor)` constructor,
 * `isInternal`, `encodeMicroCache` and the rest of the encoder.
 *
 * Java's `validBits[n >> 5] |= 1 << n` shifts by `n & 31`; the port writes that mask out ([bit]), so no platform's
 * shift rule is relied on.
 */
internal class MicroCache2(
    bc: StatCoderContext,
    dataBuffers: DataBuffers,
    lonIdx: Int,
    latIdx: Int,
    divisor: Int,
    wayValidator: TagValueValidator?,
    waypointMatcher: WaypointMatcher?,
) : MicroCache(null) {
    private val lonBase: Int
    private val latBase: Int
    private val cellsize: Int

    init {
        cellsize = 1000000 / divisor
        lonBase = lonIdx * cellsize
        latBase = latIdx * cellsize

        val wayTagCoder = TagValueCoder(bc, dataBuffers, wayValidator)
        val nodeTagCoder = TagValueCoder(bc, dataBuffers, null)
        val nodeIdxDiff = NoisyDiffCoder(bc)
        val nodeEleDiff = NoisyDiffCoder(bc)
        val extLonDiff = NoisyDiffCoder(bc)
        val extLatDiff = NoisyDiffCoder(bc)
        val transEleDiff = NoisyDiffCoder(bc)

        size = bc.decodeNoisyNumber(5)
        val faid = if (size > dataBuffers.ibuf2.size) IntArray(size) else dataBuffers.ibuf2
        val fapos = if (size > dataBuffers.ibuf3.size) IntArray(size) else dataBuffers.ibuf3
        this.faid = faid
        this.fapos = fapos

        val alon = if (size > dataBuffers.alon.size) IntArray(size) else dataBuffers.alon
        val alat = if (size > dataBuffers.alat.size) IntArray(size) else dataBuffers.alat

        if (debug) println("*** decoding cache of size=$size for lonIdx=$lonIdx latIdx=$latIdx")

        bc.decodeSortedArray(faid, 0, size, 29, 0)

        for (n in 0 until size) {
            val id64 = expandId(faid[n])
            alon[n] = (id64 shr 32).toInt()
            alat[n] = id64.toInt() // Java: (int) (id64 & 0xffffffff), the mask being int -1 widened to long -1L
        }

        val netdatasize = bc.decodeNoisyNumber(10)
        ab = if (netdatasize > dataBuffers.bbuf1.size) ByteArray(netdatasize) else dataBuffers.bbuf1
        aboffset = 0

        val validBits = IntArray((size + 31) shr 5)

        var finaldatasize = 0

        val reverseLinks = LinkedListContainer(size, dataBuffers.ibuf1)

        var selev = 0
        for (n in 0 until size) { // loop over nodes
            val ilon = alon[n]
            val ilat = alat[n]

            // future escapes (turn restrictions?)
            var trExceptions: Short = 0
            var featureId = bc.decodeVarBits()
            if (featureId == 13) {
                fapos[n] = aboffset
                validBits[n shr 5] = validBits[n shr 5] or bit(n) // mark dummy-node valid
                continue // empty node escape (delta files only)
            }
            while (featureId != 0) {
                val bitsize = bc.decodeNoisyNumber(5)

                if (featureId == 2) { // exceptions to turn-restriction
                    trExceptions = bc.decodeBounded(1023).toShort()
                } else if (featureId == 1) { // turn-restriction
                    writeBoolean(true)
                    writeShort(trExceptions.toInt()) // exceptions from previous feature
                    trExceptions = 0

                    writeBoolean(bc.decodeBit()) // isPositive
                    writeInt(ilon + bc.decodeNoisyDiff(10)) // fromLon
                    writeInt(ilat + bc.decodeNoisyDiff(10)) // fromLat
                    writeInt(ilon + bc.decodeNoisyDiff(10)) // toLon
                    writeInt(ilat + bc.decodeNoisyDiff(10)) // toLat
                } else {
                    for (i in 0 until bitsize) bc.decodeBit() // unknown feature, just skip
                }
                featureId = bc.decodeVarBits()
            }
            writeBoolean(false)

            selev += nodeEleDiff.decodeSignedValue()
            writeShort(selev.toShort().toInt()) // Java: writeShort((short) selev), the short widened back to int
            val nodeTags = nodeTagCoder.decodeTagValueSet()
            writeVarBytes(nodeTags?.data)

            val links = bc.decodeNoisyNumber(1)
            if (debug) println("***   decoding node $ilon/$ilat with links=$links")
            for (li in 0 until links) {
                var sizeoffset = 0
                val nodeIdx = n + nodeIdxDiff.decodeSignedValue()

                var dlonRemaining: Int
                var dlatRemaining: Int

                var isReverse = false
                if (nodeIdx != n) { // internal (forward-) link
                    dlonRemaining = alon[nodeIdx] - ilon
                    dlatRemaining = alat[nodeIdx] - ilat
                } else {
                    isReverse = bc.decodeBit()
                    dlonRemaining = extLonDiff.decodeSignedValue()
                    dlatRemaining = extLatDiff.decodeSignedValue()
                }
                if (debug) {
                    println("***     decoding link to ${ilon + dlonRemaining}/${ilat + dlatRemaining} extern=${nodeIdx == n}")
                }

                val wayTags = wayTagCoder.decodeTagValueSet()

                val linkValid = wayTags != null || wayValidator == null
                if (linkValid) {
                    val startPointer = aboffset
                    sizeoffset = writeSizePlaceHolder()

                    writeVarLengthSigned(dlonRemaining)
                    writeVarLengthSigned(dlatRemaining)

                    validBits[n shr 5] = validBits[n shr 5] or bit(n) // mark source-node valid
                    if (nodeIdx != n) { // valid internal (forward-) link
                        reverseLinks.addDataElement(nodeIdx, n) // register reverse link
                        finaldatasize += 1 + aboffset - startPointer // reserve place for reverse
                        validBits[nodeIdx shr 5] = validBits[nodeIdx shr 5] or bit(nodeIdx) // mark target-node valid
                    }
                    writeModeAndDesc(isReverse, wayTags?.data)
                }

                if (!isReverse) { // write geometry for forward links only
                    var matcher = if (wayTags == null || wayTags.accessType < 2) null else waypointMatcher
                    val ilontarget = ilon + dlonRemaining
                    val ilattarget = ilat + dlatRemaining
                    if (matcher != null) {
                        // Java dereferences wayValidator here unchecked; a null one is an NPE there and at `!!` here.
                        val useAsStartWay = wayValidator!!.checkStartWay(wayTags!!.data!!)
                        if (!matcher.start(ilon, ilat, ilontarget, ilattarget, useAsStartWay)) {
                            matcher = null
                        }
                    }

                    val transcount = bc.decodeVarBits()
                    if (debug) println("***       decoding geometry with count=$transcount")
                    var count = transcount + 1
                    for (i in 0 until transcount) {
                        val dlon = bc.decodePredictedValue(dlonRemaining / count)
                        val dlat = bc.decodePredictedValue(dlatRemaining / count)
                        dlonRemaining -= dlon
                        dlatRemaining -= dlat
                        count--
                        val elediff = transEleDiff.decodeSignedValue()
                        if (wayTags != null) {
                            writeVarLengthSigned(dlon)
                            writeVarLengthSigned(dlat)
                            writeVarLengthSigned(elediff)
                        }

                        matcher?.transferNode(ilontarget - dlonRemaining, ilattarget - dlatRemaining)
                    }
                    matcher?.end()
                }
                if (linkValid) {
                    injectSize(sizeoffset)
                }
            }
            fapos[n] = aboffset
        }

        // calculate final data size
        var finalsize = 0
        var startpos = 0
        for (i in 0 until size) {
            val endpos = fapos[i]
            if ((validBits[i shr 5] and bit(i)) != 0) {
                finaldatasize += endpos - startpos
                finalsize++
            }
            startpos = endpos
        }
        // append the reverse links at the end of each node
        val abOld = ab!!
        val sizeOld = size
        val abNew = ByteArray(finaldatasize)
        val faidNew = IntArray(finalsize)
        val faposNew = IntArray(finalsize)
        ab = abNew
        this.faid = faidNew
        this.fapos = faposNew
        aboffset = 0
        size = 0

        startpos = 0
        for (n in 0 until sizeOld) {
            val endpos = fapos[n] // faposOld
            if ((validBits[n shr 5] and bit(n)) != 0) {
                val len = endpos - startpos
                abOld.copyInto(abNew, aboffset, startpos, startpos + len) // Java: System.arraycopy
                if (debug) println("*** copied $len bytes from $aboffset for node $n")
                aboffset += len

                val cnt = reverseLinks.initList(n)
                if (debug) println("*** appending $cnt reverse links for node $n")

                for (ri in 0 until cnt) {
                    val nodeIdx = reverseLinks.getDataElement()
                    val sizeoffset = writeSizePlaceHolder()
                    writeVarLengthSigned(alon[nodeIdx] - alon[n])
                    writeVarLengthSigned(alat[nodeIdx] - alat[n])
                    writeModeAndDesc(true, null)
                    injectSize(sizeoffset)
                }
                faidNew[size] = faid[n] // faidOld
                faposNew[size] = aboffset
                size++
            }
            startpos = endpos
        }
        init(size)
    }

    /** Reads [len] bytes at the read position through [u], and moves past them (`MicroCache2.java:29-33`). */
    fun readUnified(len: Int, u: IByteArrayUnifier): ByteArray {
        val b = u.unify(ab!!, aboffset, len)
        aboffset += len
        return b
    }

    /** `MicroCache2.java:251-266`: the id's bits interleave lon (even) and lat (odd), 15 of each. */
    override fun expandId(id32: Int): Long {
        var id = id32 // Java reassigns its parameter
        var dlon = 0
        var dlat = 0

        var bm = 1
        while (bm < 0x8000) {
            if ((id and 1) != 0) dlon = dlon or bm
            if ((id and 2) != 0) dlat = dlat or bm
            id = id shr 2
            bm = bm shl 1
        }

        val lon32 = lonBase + dlon
        val lat32 = latBase + dlat

        // Java: ((long) lon32) << 32 | lat32, where lat32 widens with its sign, as toLong() does.
        return (lon32.toLong() shl 32) or lat32.toLong()
    }

    /** `MicroCache2.java:268-282`, the inverse of [expandId]. */
    override fun shrinkId(id64: Long): Int {
        val lon32 = (id64 shr 32).toInt()
        val lat32 = id64.toInt()
        val dlon = lon32 - lonBase
        val dlat = lat32 - latBase
        var id32 = 0

        var bm = 0x4000
        while (bm > 0) {
            id32 = id32 shl 2
            if ((dlon and bm) != 0) id32 = id32 or 1
            if ((dlat and bm) != 0) id32 = id32 or 2
            bm = bm shr 1
        }
        return id32
    }

    private companion object {
        /** Java's `1 << n` for an int `n`, which shifts by `n & 31`. */
        fun bit(n: Int): Int = 1 shl (n and 31)
    }
}
