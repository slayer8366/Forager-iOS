package com.zynergylabs.forager.brouter.oracle.e3

import com.zynergylabs.forager.brouter.oracle.OracleCalls
import java.io.File
import java.security.MessageDigest

/** E's own profiles (Phase 1 report, E4), made from the oracle's pinned ones and handed to [E3Stability.ownProfiles]. */
object E3Profiles {
    const val CROSSING = "trekking-crossing"

    /**
     * CR's crossing profile (E4): the oracle's `trekking.brf` (bike mode; it assigns `priorityclassifier`) with
     * `consider_crossing = true`, `crossing_Prio_H = 1`, `crossing_Prio_L = 1000`, and `cost_ToLeft_from_H_classK` and
     * `cost_ToRight_from_H_classK` = 100·K for K from 1 to 6 (`RoutingContext.java:122-140`), inserted at the top of its
     * global context. `trekking.brf` assigns none of them, which is checked.
     */
    fun crossing(): String {
        val base = File(OracleCalls.oracleDir, "profiles2/trekking.brf").readText()
        val head = "---context:global"
        val at = base.indexOf(head)
        check(at >= 0 && base.indexOf(head, at + 1) < 0) { "trekking.brf: not one global context" }
        val names = listOf("consider_crossing", "crossing_Prio_H", "crossing_Prio_L") +
            (1..6).flatMap { k -> listOf("cost_ToLeft_from_H_class$k", "cost_ToRight_from_H_class$k") }
        for (n in names) check(!Regex("""\b$n\b""").containsMatchIn(base)) { "trekking.brf already names $n" }
        val assigns = buildString {
            append("\n# E4's crossing assigns (CR), added by E3Profiles\n")
            append("assign consider_crossing = true\nassign crossing_Prio_H = 1\nassign crossing_Prio_L = 1000\n")
            for (k in 1..6) append("assign cost_ToLeft_from_H_class$k = ${100 * k}\nassign cost_ToRight_from_H_class$k = ${100 * k}\n")
        }
        val eol = base.indexOf('\n', at)
        return base.substring(0, eol + 1) + assigns + base.substring(eol + 1)
    }

    fun sha256(text: String): String =
        MessageDigest.getInstance("SHA-256").digest(text.toByteArray()).joinToString("") { "%02x".format(it) }
}
