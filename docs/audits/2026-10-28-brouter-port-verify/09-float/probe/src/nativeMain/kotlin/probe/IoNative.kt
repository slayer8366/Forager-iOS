@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlin.experimental.ExperimentalNativeApi::class)
package probe
import kotlinx.cinterop.*
import platform.posix.*
actual fun readText(path: String): String {
    val f = fopen(path, "rb") ?: error("cannot open $path")
    val out = ArrayList<Byte>()
    val buf = ByteArray(65536)
    while (true) {
        val n = buf.usePinned { fread(it.addressOf(0), 1u, buf.size.convert(), f).toInt() }
        if (n <= 0) break
        for (i in 0 until n) out.add(buf[i])
    }
    fclose(f)
    return out.toByteArray().decodeToString()
}
actual fun writeText(path: String, text: String) {
    val dir = path.substringBeforeLast('/')
    mkdir(dir, 0x1ffu)
    val f = fopen(path, "wb") ?: error("cannot open $path")
    val bytes = text.encodeToByteArray()
    bytes.usePinned { fwrite(it.addressOf(0), 1u, bytes.size.convert(), f) }
    fclose(f)
}
actual val platformTag: String = "native-" + Platform.cpuArchitecture.name.lowercase()
