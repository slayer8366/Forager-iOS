package probe
import kotlin.math.*

// Each function mirrors one BRouter v1.7.10 expression, in Java's evaluation order and types.
fun hex(d: Double) = d.toRawBits().toULong().toString(16).padStart(16, '0')
fun hexF(f: Float) = f.toRawBits().toUInt().toString(16).padStart(8, '0')

// FMA detectors: a*b-c with a*b = 1 - 2^-54 (double) / 1 - 2^-24 (float). Unfused: 0. Fused: -2^-54 / -2^-24.
fun msubD(a: Double, b: Double, c: Double): Double = a * b - c
fun msubF(a: Float, b: Float, c: Float): Float = a * b - c
fun maddD(a: Double, b: Double, c: Double): Double = a * b + c

// RoutingParamCollector.java:96-97
fun readPosition(lon: Double): Int = ((lon + 180.0) * 1000000.0 + 0.5).toInt()
// StdPath.java:89 and :228 (float multiply-add, then promoted)
fun turncost(cosangle: Double, turncostbase: Float): Int = ((1.0 - cosangle) * turncostbase + 0.2).toInt()
fun sectionCostTail(dist: Int, costfactor: Float): Double { var s = 0.0; s += dist * costfactor + 0.5f; return s }
// CheapRuler.java:72-78 core
fun cheapDistance(dlon: Double, dlat: Double): Double = sqrt(dlat * dlat + dlon * dlon)

// CheapRuler.java:20-47 (the scale table)
const val DEG_TO_RAD = PI / 180.0
fun calcKxKyFromILat(ilat: Int): DoubleArray {
    val lat = DEG_TO_RAD * (ilat * 1e-6 - 90)
    val cos = cos(lat)
    val cos2 = 2 * cos * cos - 1
    val cos3 = 2 * cos * cos2 - cos
    val cos4 = 2 * cos * cos3 - cos2
    val cos5 = 2 * cos * cos4 - cos3
    val kx = (111.41513 * cos - 0.09455 * cos3 + 0.00012 * cos5) * 1e-6 * 1000
    val ky = (111.13209 - 0.56605 * cos2 + 0.0012 * cos4) * 1e-6 * 1000
    return doubleArrayOf(lat, cos, kx, ky)
}
// CheapAngleMeter.java:50-66; Math.toDegrees in JDK 21 is angrad * 57.29577951308232
fun getAngle(lon1: Int, lat1: Int, lon2: Int, lat2: Int): Double {
    val xdiff = (lat2 - lat1).toDouble(); val ydiff = (lon2 - lon1).toDouble()
    return atan2(ydiff, xdiff) * 57.29577951308232
}
fun normalize(a: Double): Double =
    if (a >= 360) a - (360 * (a / 360).toInt()) else if (a < 0) a - (360 * ((a / 360).toInt() - 1)) else a
// Java Math.round(double) for the distance sites, as the port would have to write it on Native: floor(x + 0.5)
fun javaRoundViaFloor(x: Double): Long = floor(x + 0.5).toLong()

// OsmNodesMap.collectOutreachers (OsmNodesMap.java:178-208), with OsmNode.vanish (OsmNode.java:205-217) reduced to
// its effect on links. Nodes are keyed as OsmNode is: equals on (ilon, ilat), hashCode ilon + ilat.
class Node(val ilon: Int, val ilat: Int, val id: Int) {
    val links = ArrayList<Node>(); var visitID = 0; var hollow = false
    override fun equals(other: Any?) = other is Node && other.ilon == ilon && other.ilat == ilat
    override fun hashCode() = ilon + ilat
}
class OutreachSim(val hmap: HashMap<Node, Node>) {
    var nodesCreated = 0; var lastVisitID = 1
    fun run(dest: Node, maxCost: Int, pathCost: Int, dist: (Node, Node) -> Int): Int {
        val stack = ArrayList<Node>()
        nodesCreated = 0
        for (n in hmap.values) { n.visitID = lastVisitID; nodesCreated++; stack.add(n) }
        lastVisitID++
        while (stack.isNotEmpty()) {
            val n = stack.removeAt(stack.size - 1)
            n.visitID = lastVisitID
            for (t in n.links.toList()) if (t.visitID != lastVisitID) { t.visitID = lastVisitID; nodesCreated++; stack.add(t) }
            if (dist(n, dest) > maxCost - pathCost + 100) vanish(n)
            if (n.links.isEmpty()) nodesCreated--
        }
        return nodesCreated
    }
    private fun vanish(n: Node) {
        if (n.hollow) return
        for (t in n.links.toList()) if (!t.hollow) { n.links.remove(t); t.links.remove(n) }
    }
}

// Run as an executable: the FMA detectors and BRouter-shaped multiply-adds, with operands read at run time.
fun fmaReport(): String {
    val one = readText("$IO/in/seed.txt").trim().toDouble()
    val a = one + 2.0.pow(-27); val b = one - 2.0.pow(-27)
    val af = (one + 2.0.pow(-13)).toFloat(); val bf = (one - 2.0.pow(-13)).toFloat()
    return listOf(
        "platform $platformTag",
        "msubD ${hex(msubD(a, b, one))} (unfused 0000000000000000, fused bc90000000000000)",
        "msubF ${hexF(msubF(af, bf, one.toFloat()))} (unfused 00000000, fused b2800000)",
        "maddD ${hex(maddD(a, b, -one))} (unfused 0000000000000000, fused bc90000000000000)",
        "cheapDistance ${hex(cheapDistance(a, b))}",
        "turncost ${turncost(a - one, 90f)} readPosition ${readPosition(a)} section ${hex(sectionCostTail(7, af))}",
    ).joinToString("\n")
}
