package com.zynergylabs.forager.brouter.oracle.e3

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Reads 1b's stored expected values ([E3Expected]; RECORD -88) and holds every row to the R1 its stratum logged in the
 * committed stability tables. The writer ([com.zynergylabs.forager.brouter.oracle.e3.strata.E3ExpectedWriteTest]) wrote each
 * row from a fresh R1 and refused to write if any differed from its log; this test keeps the stored file and the logs one.
 * Runs no oracle: it is cheap, and part of jvmTest.
 */
class E3ExpectedFileTest {
    private val stored = E3Expected.read(
        checkNotNull(javaClass.getResource(E3Expected.RESOURCE)) { "no ${E3Expected.RESOURCE} on the test classpath" }.readText(),
    )

    @Test
    fun headNamesThePinnedJdk() = assertEquals(E3Expected.JDK, stored.jdk)

    @Test
    fun everyStratumHasItsCount() {
        val counts = stored.rows.groupingBy { it.stratum }.eachCount()
        assertEquals(E3Expected.STRATA.mapValues { it.value.first }, counts)
        assertEquals(stored.rows.size, stored.rows.map { it.stratum to it.id }.distinct().size, "a row repeats")
    }

    @Test
    fun everyTupleDigestIsItsTuple() {
        val wrong = stored.rows.filter { it.tupleDigest != E3Expected.fnv1aText(it.outcome.toField()) }.map { it.id }
        assertTrue(wrong.isEmpty(), "tuple digests that are not their tuple's: $wrong")
    }

    @Test
    fun everyRowIsTheR1ItsStratumLogged() {
        val wrong = ArrayList<String>()
        for ((stratum, spec) in E3Expected.STRATA) {
            val log = E3Expected.logged(spec.second)
            val rows = stored.rows.filter { it.stratum == stratum }
            val ids = rows.map { it.id }.toSet()
            val ofStratum = if (stratum == "R") log.filterKeys { it.startsWith("R-") } else log
            if (ofStratum.keys != ids) wrong += "$stratum: ids ${ids - ofStratum.keys} not logged, ${ofStratum.keys - ids} not stored"
            for (r in rows) {
                val logged = ofStratum[r.id]?.second ?: continue
                val same = if (r.compare == "exact") r.outcome == logged else r.outcome.kind == logged.kind
                if (!same) wrong += "$stratum ${r.id} (${r.compare}): stored ${r.outcome.toField()}, logged ${logged.toField()}"
            }
        }
        assertTrue(wrong.isEmpty(), "stored rows unlike their stratum's logged R1: $wrong")
    }

    @Test
    fun exclusionsAreNamedAndOnlyThem() {
        val notExact = stored.rows.filter { it.compare != "exact" && it.compare != "outcome" }.map { it.stratum to it.id }.toSet()
        assertEquals(notExact, stored.excluded.keys, "rows kept out of exact values, against the head's named exclusions")
        for ((stratum, spec) in E3Expected.STRATA) {
            // A case its stratum logged as unstable is never stored as exact.
            val unstable = E3Expected.logged(spec.second).filter { it.value.first != "stable" }.keys
            for (id in unstable.filter { u -> stored.rows.any { it.stratum == stratum && it.id == u } }) {
                assertTrue((stratum to id) in stored.excluded, "$stratum $id was logged unstable but is not excluded")
            }
        }
    }

    @Test
    fun fkExtraCasesAreComparedOnOutcome() {
        val fk = stored.rows.filter { it.stratum == "FK" }.associateBy { it.id }
        for (id in E3Expected.OUTCOME_ONLY) assertEquals("outcome", fk.getValue(id).compare, id)
        val control = fk.getValue(E3Expected.CONTROL).outcome
        for (id in E3Expected.SAME_AS_CONTROL) assertEquals(control, fk.getValue(id).outcome, "$id against ${E3Expected.CONTROL}")
    }
}
