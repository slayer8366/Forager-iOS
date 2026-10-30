package com.zynergylabs.forager.brouter.mapaccess

import com.zynergylabs.forager.brouter.codec.DataBuffers
import com.zynergylabs.forager.brouter.codec.MicroCache
import com.zynergylabs.forager.brouter.util.ByteDataReader
import com.zynergylabs.forager.brouter.util.Crc32
import okio.FileSystem
import okio.IOException
import okio.Path

/**
 * Port of BRouter 1.7.10's `btools.mapaccess.PhysicalFile`: one `.rd5` file's header, open for reading. Java's `File`
 * becomes an okio [Path] read through [fileSystem] (item B, row 2); `getName` is [Path.name].
 */
internal class PhysicalFile(
    fileSystem: FileSystem,
    f: Path,
    dataBuffers: DataBuffers,
    lookupVersion: Int,
    lookupMinorVersion: Int,
) {
    var ra: ReadOnlyFile? = null
    var fileIndex: LongArray = LongArray(25)
    var fileHeaderCrcs: IntArray? = null
    var creationTime: Long = 0
    var fileName: String = f.name
    var divisor: Int = 80
    var elevationType: Byte = 3

    init {
        val iobuffer = dataBuffers.iobuffer
        val ra = ReadOnlyFile(fileSystem, f)
        this.ra = ra
        ra.readFully(iobuffer, 0, 200)
        val fileIndexCrc = Crc32.crc(iobuffer, 0, 200)
        var dis = ByteDataReader(iobuffer)
        for (i in 0 until 25) {
            val lv = dis.readLong()
            val readVersion = (lv shr 48).toShort()
            if (i == 0 && lookupVersion != -1 && readVersion.toInt() != lookupVersion) {
                throw IOException(
                    "lookup version mismatch (old rd5?) lookups.dat=" + lookupVersion + " " + f.name + "=" + readVersion,
                )
            }
            fileIndex[i] = lv and 0xffffffffffffL
        }

        // read some extra info from the end of the file, if present
        val len = ra.length()
        val pos = fileIndex[24]
        var extraLen = 8 + 26 * 4
        if (len != pos) { // Java: "if (len == pos) return; // old format o.k."
            if ((len - pos) > extraLen) {
                extraLen++
            }
            if (len < pos + extraLen) { // > is o.k. for future extensions!
                throw IOException("file of size " + len + " too short, should be " + (pos + extraLen))
            }
            ra.seek(pos)
            ra.readFully(iobuffer, 0, extraLen)
            dis = ByteDataReader(iobuffer)
            creationTime = dis.readLong()
            val crcData = dis.readInt()
            divisor = if (crcData == fileIndexCrc) {
                80 // old format
            } else if ((crcData xor 2) == fileIndexCrc) {
                32 // new format
            } else {
                throw IOException("top index checksum error")
            }
            val crcs = IntArray(25)
            fileHeaderCrcs = crcs
            for (i in 0 until 25) {
                crcs[i] = dis.readInt()
            }
            // Java wraps this read in try/catch (Exception) {}: it reads index 112 of a 65,636-byte buffer, which
            // cannot throw, so the port has no catch to swallow anything.
            elevationType = dis.readByte()
        }
    }

    fun close() {
        // Java: ra.close() inside catch (Exception ee) {}. okio's close throws only an IOException; that is ignored
        // here as there, and Forager never reads it.
        try {
            ra?.close()
        } catch (ee: IOException) {
        }
    }

    companion object {
        /**
         * Java's `checkFileIntegrity(File)`: every micro-cache's checksum, with no cell decoded (`MicroCache.debug` is
         * false, so `reallyDecode` is false). Null when the file is whole; otherwise it throws.
         */
        fun checkFileIntegrity(fileSystem: FileSystem, f: Path): String? {
            var pf: PhysicalFile? = null
            try {
                val dataBuffers = DataBuffers()
                pf = PhysicalFile(fileSystem, f, dataBuffers, -1, -1)
                val div = pf.divisor
                for (lonDegree in 0 until 5) { // doesn't really matter..
                    for (latDegree in 0 until 5) { // ..where on earth we are
                        val osmf = OsmFile(pf, lonDegree, latDegree, dataBuffers)
                        if (osmf.hasData()) {
                            for (lonIdx in 0 until div) {
                                for (latIdx in 0 until div) {
                                    osmf.createMicroCache(
                                        lonDegree * div + lonIdx, latDegree * div + latIdx, dataBuffers, null, null,
                                        MicroCache.debug, null,
                                    )
                                }
                            }
                        }
                    }
                }
            } finally {
                pf?.close()
            }
            return null
        }
    }
}
