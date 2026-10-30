package com.zynergylabs.forager.brouter.mapaccess

import com.zynergylabs.forager.brouter.codec.DataBuffers
import com.zynergylabs.forager.brouter.expressions.ExpressionsEngines
import com.zynergylabs.forager.brouter.oracle.OracleCalls
import com.zynergylabs.forager.brouter.tiles.TestTiles
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import java.io.File
import java.io.IOException
import java.nio.file.Files

/** Both engines' file layers, built the same way, for step 5a's tests (item I). */
object MapaccessEngines {
    private val profile: File get() = File(OracleCalls.oracleDir, "profiles2/hiking-mountain.brf")

    /** Any value: the file layer only passes it on to `OsmNodesMap.maxmem` and the cache-cleaning threshold. */
    private const val MAXMEM = 64L * 1024 * 1024

    /** The captured tiles: the four brouter.de tiles and F0's fixture (Phase 1 report, item E4). */
    val capturedTiles: List<File>
        get() = listOf("W75_N40", "W75_N35", "W70_N40", "E170_S50").map { File(TestTiles.segments, "$it.rd5") } +
            File(TestTiles.f0, "W75_N40.rd5")

    val e170s50: File get() = File(TestTiles.segments, "E170_S50.rd5")

    /** E170_S50's degree squares start at (lon + 180, lat + 90) = (350, 40). */
    const val E170_LON_DEGREE = 350
    const val E170_LAT_DEGREE = 40

    fun oracleCache(dir: File): btools.mapaccess.NodesCache =
        btools.mapaccess.NodesCache(dir, ExpressionsEngines.oracle(profile).way, false, MAXMEM, null, false)

    internal fun portCache(dir: File): NodesCache =
        NodesCache(FileSystem.SYSTEM, dir.toOkioPath(), ExpressionsEngines.port(profile).way, false, MAXMEM, null, false)

    fun oraclePhysical(file: File): btools.mapaccess.PhysicalFile =
        btools.mapaccess.PhysicalFile(file, btools.codec.DataBuffers(), -1, -1)

    internal fun portPhysical(file: File): PhysicalFile =
        PhysicalFile(FileSystem.SYSTEM, file.toOkioPath(), DataBuffers(), -1, -1)

    /** Forager's `RoutingDataFolder.headerProblem` (`RoutingDataFolder.kt:48-53` at Forager e2969abe), on the oracle. */
    fun oracleHeaderProblem(file: File): String? = try {
        oraclePhysical(file).close()
        null
    } catch (e: IOException) {
        e.message ?: e.javaClass.simpleName
    }

    /** A package-private or private field of an oracle object. */
    fun field(obj: Any, name: String): Any? =
        obj.javaClass.getDeclaredField(name).apply { isAccessible = true }.get(obj)

    /** A call's outcome as one comparable line: its value, or the exception's class and message. Errors propagate. */
    inline fun outcome(block: () -> Any?): String = try {
        "ok:${block()}"
    } catch (e: Exception) {
        "threw ${e.javaClass.name}: ${e.message}"
    }

    /** The oracle's (lonDegree, latDegree) squares of E170_S50 with no data, in tile-index order. */
    fun e170EmptyDegrees(): List<Pair<Int, Int>> = e170Degrees().filter { !it.second }.map { it.first }

    /** The oracle's (lonDegree, latDegree) squares of E170_S50 with data, in tile-index order. */
    fun e170DataDegrees(): List<Pair<Int, Int>> = e170Degrees().filter { it.second }.map { it.first }

    private fun e170Degrees(): List<Pair<Pair<Int, Int>, Boolean>> {
        val pf = oraclePhysical(e170s50)
        try {
            val buffers = btools.codec.DataBuffers()
            return (0 until 25).map { tileIndex ->
                val lonDegree = E170_LON_DEGREE + tileIndex / 5
                val latDegree = E170_LAT_DEGREE + tileIndex % 5
                (lonDegree to latDegree) to btools.mapaccess.OsmFile(pf, lonDegree, latDegree, buffers).hasData()
            }
        } finally {
            pf.close()
        }
    }

    /** A point inside a degree square, in the engine's integer coordinates. */
    fun pointIn(degree: Pair<Int, Int>): Pair<Int, Int> = (degree.first * 1_000_000 + 412_345) to (degree.second * 1_000_000 + 587_654)

    /** A fresh folder holding [bytes] as `E170_S50.rd5`. */
    fun folderWithE170(bytes: ByteArray): File {
        val folder = Files.createTempDirectory("mapaccess-5a").toFile()
        File(folder, "E170_S50.rd5").writeBytes(bytes)
        return folder
    }
}
