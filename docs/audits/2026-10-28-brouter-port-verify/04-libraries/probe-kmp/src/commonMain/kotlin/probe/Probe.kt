package probe

import kotlin.concurrent.Volatile
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.time.TimeSource
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.buffer
import okio.use

/** RandomAccessFile(seek, readFully, length, close) in common code, through okio's FileHandle. */
class RandomRead(path: String) {
    private val handle = FileSystem.SYSTEM.openReadOnly(path.toPath())
    fun length(): Long = handle.size()
    fun readFully(pos: Long, buf: ByteArray, off: Int, len: Int) {
        var done = 0
        while (done < len) {
            val n = handle.read(pos + done, buf, off + done, len - done)
            if (n <= 0) throw IllegalStateException("EOF at ${pos + done}")
            done += n
        }
    }
    fun close() = handle.close()
}

fun lastModified(path: String): Long? = FileSystem.SYSTEM.metadataOrNull(path.toPath())?.lastModifiedAtMillis
fun firstLine(path: String): String? = FileSystem.SYSTEM.source(path.toPath()).buffer().use { it.readUtf8Line() }

class Engine {
    @Volatile private var terminated = false
    fun terminate() { terminated = true }
    fun isTerminated() = terminated
    private val start = TimeSource.Monotonic.markNow()
    fun elapsedMillis(): Long = start.elapsedNow().inWholeMilliseconds
}

object Cache : SynchronizedObject() {
    private var n = 0
    fun bump(): Int = synchronized(this) { ++n }
}

fun maths(x: Double): Double = sqrt(x) + sin(x) + cos(x) + atan2(x, 1.0) + exp(-x) + (x * 2.5).roundToLong()
fun splitWaypoints(s: String): List<String> = s.split(Regex(";|\\|"))
fun floatText(f: Float): String = f.toString()
