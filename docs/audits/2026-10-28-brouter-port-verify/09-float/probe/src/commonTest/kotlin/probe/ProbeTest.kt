package probe
import kotlin.math.*
import kotlin.test.Test

class Lcg(var s: Long) { fun next(): Int { s = s * 6364136223846793005L + 1442695040888963407L; return (s ushr 33).toInt() } }

class ProbeTest {
    private val out = "$IO/out/$platformTag"
    private fun w(name: String, lines: List<String>) = writeText("$out/$name.txt", lines.joinToString("\n", postfix = "\n"))

    @Test fun fma() {
        val one = readText("$IO/in/seed.txt").trim().toDouble()   // 1.0, read at run time so nothing folds
        val a = one + 2.0.pow(-27); val b = one - 2.0.pow(-27)
        val af = (one + 2.0.pow(-13)).toFloat(); val bf = (one - 2.0.pow(-13)).toFloat()
        val l = ArrayList<String>()
        l += "msubD ${hex(msubD(a, b, one))}"
        l += "msubF ${hexF(msubF(af, bf, one.toFloat()))}"
        l += "maddD ${hex(maddD(a, b, -one))}"
        // the JVM-found inputs where a fused evaluation would differ are appended by the JVM-only test's file
        val sens = readText("$IO/in/fma-sensitive.txt").lines().filter { it.isNotBlank() }
        for (s in sens) {
            val p = s.split(' ')
            when (p[0]) {
                "readPosition" -> l += "readPosition ${p[1]} ${readPosition(Double.fromBits(p[1].toULong(16).toLong()))}"
                "turncost" -> l += "turncost ${p[1]} ${p[2]} ${turncost(Double.fromBits(p[1].toULong(16).toLong()), Float.fromBits(p[2].toUInt(16).toInt()))}"
                "section" -> l += "section ${p[1]} ${p[2]} ${hex(sectionCostTail(p[1].toInt(), Float.fromBits(p[2].toUInt(16).toInt())))}"
                "distance" -> l += "distance ${p[1]} ${p[2]} ${hex(cheapDistance(Double.fromBits(p[1].toULong(16).toLong()), Double.fromBits(p[2].toULong(16).toLong())))}"
            }
        }
        w("fma", l)
    }

    @Test fun scaleTable() {
        val l = ArrayList<String>()
        for (i in 0 until 1800) {
            val r = calcKxKyFromILat(i * 100000 + 100000 / 2)
            l += "$i ${hex(r[0])} ${hex(r[1])} ${hex(r[2])} ${hex(r[3])}"
        }
        w("scale-table", l)
    }

    @Test fun atan2Sample() {
        val g = Lcg(42); val l = ArrayList<String>()
        for (i in 0 until 300000) {
            val range = when (i % 4) { 0 -> 50; 1 -> 2000; 2 -> 100000; else -> 2000000 }
            val dx = g.next() % range; val dy = g.next() % range
            if (dx == 0 && dy == 0) continue
            val ang = getAngle(0, 0, dy, dx)
            l += "$dy $dx ${hex(atan2(dy.toDouble(), dx.toDouble()))} ${hex(normalize(ang))}"
        }
        w("atan2", l)
    }

    @Test fun expSinCos() {
        val l = ArrayList<String>()
        for (d in 1..20000) l += "exp-dist $d ${hex(exp(-d / 100.0))}"
        for (k in -2000..2000) { val inc = k / 10000.0; l += "exp-tobler $k ${hex(exp(-3.5 * abs(inc + 0.05)))}" }
        for (deg in 0 until 3600) { val dir = deg / 10.0 * DEG_TO_RAD; l += "sincos $deg ${hex(sin(dir))} ${hex(cos(dir))}" }
        w("exp-sin-cos", l)
    }

    private fun parseLines(file: String, outName: String) {
        val l = ArrayList<String>()
        for (t in readText("$IO/in/$file").lines()) {
            if (t.isEmpty()) continue
            val f = try { hexF(t.toFloat()) } catch (e: NumberFormatException) { "NFE" }
            val d = try { hex(t.toDouble()) } catch (e: NumberFormatException) { "NFE" }
            l += "$t $f $d"
        }
        w(outName, l)
    }
    @Test fun parseProfiles() = parseLines("profile-tokens.txt", "parse-profile-tokens")
    @Test fun parseLookups() = parseLines("lookup-values.txt", "parse-lookup-values")
    @Test fun parseStress() = parseLines("stress-decimals.txt", "parse-stress")

    @Test fun printing() {
        val l = ArrayList<String>()
        for (v in 1000..201000) l += "f $v ${((v - 1000) / 100f)}"     // BExpressionContext.java:225
        val g = Lcg(7)
        for (i in 0 until 20000) {
            val lon = -180.0 + (g.next().toLong() and 0x7fffffff) / 2147483647.0 * 360.0
            val s = lon.toString(); l += "d ${hex(lon)} $s ${hex(s.toDouble())}"
        }
        w("print", l)
    }

    @Test fun conversions() {
        val l = ArrayList<String>()
        val ds = mutableListOf(0.49999999999999994, 0.5, 1.5, 2.5, -0.5, -1.5, 4503599627370497.0, 1e300, -1e300, 2147483647.5, -2147483648.7, 3.0e9)
        for (n in 1..5000) ds += (n + 0.5).nextDown()
        for (n in 1..5000) ds += (n + 0.5)
        for (d in ds) l += "d ${hex(d)} toInt=${d.toInt()} toLong=${d.toLong()} roundToLong=${d.roundToLong()} floorHalf=${javaRoundViaFloor(d)} kround=${hex(round(d))}"
        for (f in listOf(Float.NaN, 3e9f, -3e9f, 2.5f, -2.5f, 16777217f)) l += "f ${hexF(f)} toInt=${f.toInt()}"
        l += "nan toInt=${Double.NaN.toInt()} toLong=${Double.NaN.toLong()}"
        l += "minmax ${hex(min(-0.0, 0.0))} ${hex(max(-0.0, 0.0))} ${hex(min(Double.NaN, 1.0))} ${hexF(min(-0.0f, 0.0f))}"
        l += "rem ${hex(-725.0 % 360.0)} ${hex(1e17 % 360.0)}"
        w("conversions", l)
    }

    @Test fun hashOrder() {
        val l = ArrayList<String>()
        // OsmNodesMap's hmap: new HashMap<>(4096), keyed by OsmNode (hashCode ilon + ilat)
        val g = Lcg(99)
        val nodes = (0 until 3000).map { Node(180_000_000 + g.next() % 500_000, 130_000_000 + g.next() % 500_000, it) }
        val m = HashMap<Node, Node>(4096)
        for (n in nodes) m[n] = n
        l += "order " + m.values.joinToString(",") { it.id.toString() }
        for (n in nodes.filter { it.id % 3 == 0 }) m.remove(n)
        for (n in nodes.filter { it.id % 6 == 0 }) m[n] = n
        l += "order-after-remove " + m.values.joinToString(",") { it.id.toString() }
        for (s in listOf("use_dynamic_range", "profile", "alternativeidx", "hiking-mountain", "äöü", "")) l += "strhash $s ${s.hashCode()}"
        // collectOutreachers on a random graph: nodesCreated after one call, with this platform's HashMap order
        for (seed in 1L..20L) {
            val r = Lcg(seed)
            val ns = (0 until 400).map { Node(1000 * (it % 20) + r.next() % 7, 1000 * (it / 20) + r.next() % 7, it) }
            for (i in ns.indices) { val j = (r.next() and 0x7fffffff) % ns.size; if (j != i && ns[j] !in ns[i].links) { ns[i].links += ns[j]; ns[j].links += ns[i] } }
            for (i in ns.indices) { if (i + 1 < ns.size && i % 20 != 19 && ns[i + 1] !in ns[i].links) { ns[i].links += ns[i + 1]; ns[i + 1].links += ns[i] } }
            for (n in ns) if ((r.next() and 0x7fffffff) % 10 == 0) n.hollow = true
            val hm = HashMap<Node, Node>(4096); for (n in ns) hm[n] = n
            val dest = Node(19000, 19000, -1)
            val sim = OutreachSim(hm)
            val created = sim.run(dest, 20000, 0) { a, b -> abs(a.ilon - b.ilon) + abs(a.ilat - b.ilat) }
            val vanished = ns.filter { it.links.isEmpty() }.map { it.id }.sorted().hashCode()
            l += "outreach seed=$seed nodesCreated=$created linklessSetHash=$vanished"
        }
        w("hash", l)
    }
}
