package com.zynergylabs.forager.brouter.msun

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Step 2b: signed zeros, NaNs, infinities, subnormals and every reduction boundary, against edges.c's output from
 * bionic's C. A NaN result is checked as a NaN, not by its bits: the C reference is an x86_64 build, where cos(Inf)
 * returns the negative default NaN (fff8...) that an arm64 phone does not (MsunReference.kt, the "c 7ff0..." line), so
 * NaN payloads are outside the bit-for-bit claim. "u" lines are k_rem_pio2.c's range: the port must refuse them, by name.
 */
class MsunEdgesTest {
    private fun check(line: EdgeLine, got: () -> Double): String? {
        val value = try {
            got()
        } catch (e: MsunUnsupportedException) {
            return if (line.op == "u") null else "${line.text}: refused, ${e.message}"
        }
        if (line.op == "u") return "${line.text}: returned ${hex(value)}, expected MsunUnsupportedException"
        val same = if (line.result.isNaN()) value.isNaN() else value.toRawBits() == line.result.toRawBits()
        return if (same) null else "${line.text}: port ${hex(value)}"
    }

    @Test
    fun cosEdges() {
        val lines = msunEdges.filter { it.op == "c" || it.op == "u" }
        assertEquals(45, lines.size, "cos edge lines")
        assertEquals(3, lines.count { it.op == "u" }, "lines in k_rem_pio2.c's range")
        val wrong = lines.mapNotNull { l -> check(l) { msunCos(l.args[0]) } }
        assertEquals(emptyList(), wrong, "cos edges that differ from bionic's C")
    }

    @Test
    fun atan2Edges() {
        val lines = msunEdges.filter { it.op == "a" }
        assertEquals(59, lines.size, "atan2 edge lines")
        val wrong = lines.mapNotNull { l -> check(l) { msunAtan2(l.args[0], l.args[1]) } }
        assertEquals(emptyList(), wrong, "atan2 edges that differ from bionic's C")
    }

    @Test
    fun theRefusalIsNamedAndCarriesItsArgument() {
        val x = Double.fromBits(0x413921fb00000000L)
        assertEquals(1647099.0, x, "the threshold's value")
        val e = runCatching { msunCos(-x) }.exceptionOrNull()
        assertTrue(e is MsunUnsupportedException, "msunCos(-1,647,099.0) threw ${e?.let { it::class.simpleName }}")
        assertEquals("cos", e.function)
        assertEquals(-x, e.argument)
        val below = Double.fromBits(0x413921faffffffffL)
        assertTrue(runCatching { msunCos(below) }.isSuccess, "the largest double below the threshold is computed")
    }
}
