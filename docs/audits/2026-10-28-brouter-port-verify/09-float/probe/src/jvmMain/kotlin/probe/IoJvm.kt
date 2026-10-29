package probe
import java.io.File
actual fun readText(path: String): String = File(path).readText()
actual fun writeText(path: String, text: String) { File(path).parentFile.mkdirs(); File(path).writeText(text) }
actual val platformTag: String = "jvm-" + System.getProperty("os.arch")
