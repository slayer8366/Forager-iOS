package com.zynergylabs.forager.brouter.expressions

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Item I, step 4, the handoff's parse checks: on every one of the oracle zip's 26 profiles, both engines built as
 * `ProfileCache` builds them, then the lookups' versions, and for the way and the node context the oracle's private
 * `nBuildInVars`, `expressionList` (through its `toString`) and `expressionNodeCount`, read by reflection, against the
 * port's.
 */
class ProfileParseTest {
    @Test
    fun theLookupsVersionsAreTheOracles() = ExpressionsEngines.forEveryProfile { profile ->
        val rc = ExpressionsEngines.foragerRoutingContext(profile)
        val oracle = ExpressionsEngines.oracle(profile, rc)
        val port = ExpressionsEngines.port(profile, rc)
        assertEquals(oracle.meta.lookupVersion, port.meta.lookupVersion, "lookupVersion")
        assertEquals(oracle.meta.lookupMinorVersion, port.meta.lookupMinorVersion, "lookupMinorVersion")
        assertEquals(oracle.meta.minAppVersion, port.meta.minAppVersion, "minAppVersion")
    }

    @Test
    fun everyProfileParsesToTheOraclesExpressions() = ExpressionsEngines.forEveryProfile { profile ->
        val rc = ExpressionsEngines.foragerRoutingContext(profile)
        val oracle = ExpressionsEngines.oracle(profile, rc)
        val port = ExpressionsEngines.port(profile, rc)
        for ((name, o, p) in listOf(Triple("way", oracle.way, port.way), Triple("node", oracle.node, port.node))) {
            assertEquals(ExpressionsEngines.oracleField(o, "nBuildInVars"), p.nBuildInVars, "$name nBuildInVars")
            assertEquals(ExpressionsEngines.oracleField(o, "expressionNodeCount"), p.expressionNodeCount, "$name expressionNodeCount")
            assertEquals(
                ExpressionsEngines.oracleField(o, "expressionList").toString(),
                p.expressionList.toString(),
                "$name expressionList",
            )
        }
    }
}
