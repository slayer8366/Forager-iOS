package com.zynergylabs.forager.brouter.mapaccess

import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath

/** Port of BRouter 1.7.10's `btools.mapaccess.StorageConfigHelper`, the two methods T2 reaches. */
internal object StorageConfigHelper {
    fun getSecondarySegmentDir(fileSystem: FileSystem, segmentDir: Path): Path? =
        getStorageLocation(fileSystem, segmentDir, "secondary_segment_dir=")

    private fun getStorageLocation(fileSystem: FileSystem, segmentDir: Path, tag: String): Path? {
        var res: Path? = null
        val configFile = segmentDir / "storageconfig.txt"
        // Java catches every Exception here and ignores it: a missing storageconfig.txt is the usual case, and the
        // answer is then null. The port catches only the IOException a read can throw; anything else propagates.
        try {
            fileSystem.read(configFile) {
                while (true) {
                    var line = readUtf8Line() ?: break
                    line = line.trim { it <= ' ' } // String.trim, Java's definition
                    if (line.startsWith("#")) continue
                    if (line.startsWith(tag)) {
                        val path = line.substring(tag.length).trim { it <= ' ' }
                        var candidate: Path? = if (path.startsWith("/")) path.toPath() else segmentDir / path
                        if (!fileSystem.exists(candidate!!)) candidate = null
                        res = candidate
                        break
                    }
                }
            }
        } catch (e: IOException) {
            res = null
        }
        return res
    }
}
