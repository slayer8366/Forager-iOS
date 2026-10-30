package com.zynergylabs.forager.brouter.mapaccess

import okio.FileHandle
import okio.FileSystem
import okio.Path

/**
 * The part of `java.io.RandomAccessFile` that `PhysicalFile`, `OsmFile` and `NodesCache` call (item B, row 1): `seek`,
 * `readFully(byte[], int, int)`, `length` and `close`, opened read-only, on okio's positional [FileHandle].
 */
internal class ReadOnlyFile(fileSystem: FileSystem, path: Path) {
    fun seek(pos: Long): Unit = TODO("item I step 5a")

    fun length(): Long = TODO("item I step 5a")

    fun readFully(b: ByteArray, off: Int, len: Int): Unit = TODO("item I step 5a")

    fun close(): Unit = TODO("item I step 5a")
}
