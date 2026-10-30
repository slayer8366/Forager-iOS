package com.zynergylabs.forager.brouter.mapaccess

import com.zynergylabs.forager.brouter.codec.DataBuffers
import okio.FileSystem
import okio.Path

/** Port of BRouter 1.7.10's `btools.mapaccess.PhysicalFile`: one `.rd5` file's header, open for reading. */
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
    var fileName: String = TODO("item I step 5a")
    var divisor: Int = 80
    var elevationType: Byte = 3

    fun close(): Unit = TODO("item I step 5a")

    companion object {
        fun checkFileIntegrity(fileSystem: FileSystem, f: Path): String? = TODO("item I step 5a")
    }
}
