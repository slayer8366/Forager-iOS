package com.zynergylabs.forager.brouter.mapaccess

import okio.EOFException
import okio.FileHandle
import okio.FileSystem
import okio.Path

/**
 * The part of `java.io.RandomAccessFile` that `PhysicalFile`, `OsmFile` and `NodesCache` call (item B, row 1): `seek`,
 * `readFully(byte[], int, int)`, `length` and `close`, opened read-only, on okio's positional [FileHandle]. okio reads
 * by offset, so the file pointer `seek` sets is kept here.
 */
internal class ReadOnlyFile(fileSystem: FileSystem, path: Path) {
    private val handle: FileHandle = fileSystem.openReadOnly(path)
    private var position = 0L

    fun seek(pos: Long) {
        position = pos
    }

    fun length(): Long = handle.size()

    /**
     * `RandomAccessFile.readFully`: [len] bytes or an `EOFException` with no message, as Java's (`RandomAccessFile.java`,
     * JDK 21: `if (count < 0) throw new EOFException()`). Each pass either throws or adds at least one byte to `n`,
     * so the loop ends within [len] passes. okio returns -1 at the end of the file, never 0 for a non-empty read; the
     * check takes 0 as a short read too, where Java would spin.
     */
    fun readFully(b: ByteArray, off: Int, len: Int) {
        var n = 0
        while (n < len) {
            val count = handle.read(position + n, b, off + n, len - n)
            if (count <= 0) throw EOFException()
            n += count
        }
        position += len
    }

    fun close() {
        handle.close()
    }
}
