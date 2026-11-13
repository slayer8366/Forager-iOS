package com.zynergylabs.forager.brouter.util

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Item I, step 2a (`3cd61ad`) tested the constructor and `unify`'s refusal, while `unify` was off T2's port list. RECORD
 * -68 put it on the port list and step 5d ported it (RECORD -74), so the refusal's premise has moved: the constructor
 * check is kept, and the refusal is replaced by a differential check against `btools.util.ByteArrayUnifier`.
 *
 * A fixed input sequence (repeats, offsets into a larger array, lengths that collide in a small cache) is unified by both
 * engines with `validateImmutability` false and true. Per call, compared: the returned bytes, and the reuse pattern (the
 * index of the earlier call whose returned instance is the same object, or -1). With validation on, one returned array
 * is then mutated and its slot hit again, and the outcome (exception class and message) compared.
 */
class ByteArrayUnifierTest {
    @Test
    fun constructs() {
        ByteArrayUnifier(16384, false) // OsmNodesMap.java:18's arguments
    }

    @Test
    fun unifyAsTheOracleWithoutValidation() = compare(false)

    @Test
    fun unifyAsTheOracleWithValidation() = compare(true)

    private fun inputs(): List<Triple<ByteArray, Int, Int>> {
        val big = ByteArray(64) { (it * 37 + 11).toByte() }
        val out = mutableListOf<Triple<ByteArray, Int, Int>>()
        for (round in 0 until 3) {
            for (len in listOf(0, 1, 2, 3, 5, 8, 13)) {
                for (offset in listOf(0, 7, 20)) out += Triple(big.copyOf(), offset, len)
            }
            out += Triple(byteArrayOf(1, 2, 3), 0, 3)
            out += Triple(byteArrayOf(9, 1, 2, 3, 9), 1, 3)
            out += Triple(byteArrayOf(1, 2, 4), 0, 3)
        }
        return out
    }

    private fun trace(unify: (ByteArray, Int, Int) -> ByteArray, validate: Boolean, mutate: (ByteArray) -> Unit): List<String> {
        val returned = mutableListOf<ByteArray>()
        val lines = mutableListOf<String>()
        for ((i, input) in inputs().withIndex()) {
            val r = unify(input.first, input.second, input.third)
            val same = returned.indexOfFirst { it === r }
            lines += "call $i bytes ${r.joinToString(",")} same-as $same"
            returned += r
        }
        if (validate) {
            val victim = returned.last { it.isNotEmpty() }
            mutate(victim)
            lines += "after mutation: " + try {
                val again = inputs().last()
                unify(again.first, again.second, again.third).joinToString(",")
            } catch (e: Exception) {
                "${e::class.simpleName}: ${e.message}"
            }
        }
        return lines
    }

    private fun compare(validate: Boolean) {
        val oracle = btools.util.ByteArrayUnifier(4, validate)
        val port = ByteArrayUnifier(4, validate)
        val o = trace({ ab, off, len -> oracle.unify(ab, off, len) }, validate) { it[0] = (it[0] + 1).toByte() }
        val p = trace({ ab, off, len -> port.unify(ab, off, len) }, validate) { it[0] = (it[0] + 1).toByte() }
        assertEquals(o.size, p.size, "validate=$validate: line count")
        val at = o.indices.firstOrNull { o[it] != p[it] }
        assertEquals(null, at, "validate=$validate: line ${at?.plus(1)} differs (${at?.let { p[it].substringBefore(" bytes") }})")
        val reused = o.count { !it.endsWith("same-as -1") && it.startsWith("call") }
        println("ByteArrayUnifier validate=$validate: ${o.size} lines equal, $reused reused; last line ${if (validate) o.last().substringAfter(": ").substringBefore(":") else "a call"}")
    }
}
