package probe
import kotlin.test.Test

// JVM only: finds inputs at BRouter's multiply-add sites where a fused evaluation (Math.fma) gives a different result
// from Java's unfused one, and records StrictMath (fdlibm) beside Math for the library functions.
class JvmOnlyTest {
    @Test fun findFmaSensitive() {
        val l = ArrayList<String>(); val g = Lcg(5)
        var found = 0; var tried = 0
        while (found < 40 && tried < 50_000_000) {
            tried++
            val lon = -180.0 + (g.next().toLong() and 0x7fffffff) / 2147483647.0 * 360.0
            val x = lon + 180.0
            val unf = (x * 1000000.0 + 0.5).toInt(); val fus = Math.fma(x, 1000000.0, 0.5).toInt()
            if (unf != fus) { l += "readPosition ${hex(lon)}"; found++ }
        }
        l += "#readPosition tried=$tried found=$found"
        found = 0; tried = 0
        while (found < 40 && tried < 50_000_000) {
            tried++
            val cosangle = ((g.next() and 0x7fffffff) / 2147483647.0) * 2 - 1
            val base = listOf(30f, 60f, 90f, 120f, 150f, 180f, 250f, 400f)[g.next().and(0x7fffffff) % 8]
            val unf = ((1.0 - cosangle) * base + 0.2).toInt(); val fus = Math.fma(1.0 - cosangle, base.toDouble(), 0.2).toInt()
            if (unf != fus) { l += "turncost ${hex(cosangle)} ${hexF(base)}"; found++ }
        }
        l += "#turncost tried=$tried found=$found"
        found = 0; tried = 0
        while (found < 40 && tried < 50_000_000) {
            tried++
            val dist = (g.next() and 0x7fffffff) % 3000 + 1
            val cf = 1f + ((g.next() and 0x7fffffff) % 100000) / 10000f
            val unf = (dist * cf + 0.5f); val fus = Math.fma(dist.toFloat(), cf, 0.5f)
            if (unf.toInt() != fus.toInt()) { l += "section $dist ${hexF(cf)}"; found++ }
        }
        l += "#section tried=$tried found=$found"
        found = 0; tried = 0
        while (found < 40 && tried < 50_000_000) {
            tried++
            val dlon = ((g.next() % 3000) * 0.0743); val dlat = ((g.next() % 3000) * 0.1112)
            val unf = Math.round(Math.sqrt(dlat * dlat + dlon * dlon)); val fus = Math.round(Math.sqrt(Math.fma(dlat, dlat, dlon * dlon)))
            if (unf != fus) { l += "distance ${hex(dlon)} ${hex(dlat)}"; found++ }
        }
        l += "#distance tried=$tried found=$found"
        writeText("$IO/in/fma-sensitive.txt", l.joinToString("\n", postfix = "\n"))
    }

    @Test fun strictVsMath() {
        val l = ArrayList<String>()
        var cosDiff = 0
        for (i in 0 until 1800) {
            val lat = DEG_TO_RAD * ((i * 100000 + 50000) * 1e-6 - 90)
            if (Math.cos(lat) != StrictMath.cos(lat)) cosDiff++
        }
        l += "scale-table cos Math!=StrictMath: $cosDiff of 1800"
        val g = Lcg(42); var aDiff = 0; var n = 0
        for (i in 0 until 300000) {
            val range = when (i % 4) { 0 -> 50; 1 -> 2000; 2 -> 100000; else -> 2000000 }
            val dx = g.next() % range; val dy = g.next() % range
            if (dx == 0 && dy == 0) continue
            n++; if (Math.atan2(dy.toDouble(), dx.toDouble()) != StrictMath.atan2(dy.toDouble(), dx.toDouble())) aDiff++
        }
        l += "atan2 Math!=StrictMath: $aDiff of $n"
        l += "java.version ${System.getProperty("java.version")} vm ${System.getProperty("java.vm.name")} arch ${System.getProperty("os.arch")}"
        writeText("$IO/out/$platformTag/jvm-only.txt", l.joinToString("\n", postfix = "\n"))
    }
}
