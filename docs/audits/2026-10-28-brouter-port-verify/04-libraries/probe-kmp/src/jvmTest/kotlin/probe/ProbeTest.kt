package probe
import kotlin.test.Test
import kotlin.test.assertEquals
class ProbeTest {
    @Test fun readsTileBytes() {
        val tile = System.getProperty("tile")
        val r = RandomRead(tile)
        val raf = java.io.RandomAccessFile(tile, "r")
        val a = ByteArray(4096); val b = ByteArray(4096)
        for (pos in longArrayOf(0L, 1000L, r.length() - 4096)) {
            r.readFully(pos, a, 0, 4096); raf.seek(pos); raf.readFully(b, 0, 4096)
            assertEquals(b.toList(), a.toList(), "bytes at $pos")
        }
        assertEquals(raf.length(), r.length()); r.close(); raf.close()
    }
    @Test fun volatileFlagAndLock() { val e = Engine(); e.terminate(); assertEquals(true, e.isTerminated()); assertEquals(1, Cache.bump()) }
}
