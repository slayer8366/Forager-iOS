package com.zynergylabs.forager.brouter.expressions

import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Item I, step 4, the handoff's malformed text: profiles that are wrong in one way each, parsed by both engines built as
 * `ProfileCache` builds them. Where the oracle throws, the port must throw the same exception class with the same
 * message; where it does not, both must parse to the same expressions. Several cases sit on prediction 1's text
 * differences (whitespace Java does not count as whitespace, `\r\n`, a no-break space).
 */
class MalformedProfileTest {
    private val way = "---context:global\nassign validForBikes = true\n---context:way\n"
    private val node = "\n---context:node\nassign initialcost = 0\n"

    /** Each text is a whole profile file. Several are expected to parse; the count of throws is printed and checked. */
    private val texts = listOf(
        "",
        "---context:way\n",
        way + "assign costfactor = nosuchvariable" + node,
        way + "assign costfactor = highway=nosuchvalue" + node,
        way + "assign costfactor = nosuchkey=primary" + node,
        way + "assign costfactor = 1.2.3" + node,
        way + "assign costfactor = add 1" + node,
        way + "assign costfactor =" + node,
        way + "assign = 1" + node,
        way + "assign costfactor = ( 1 )" + node,
        way + "nosuchkeyword costfactor = 1" + node,
        way + "assign costfactor = switch highway=primary 1" + node,
        way + "assign costfactor = 1" + node, // a no-break space: not whitespace in Java
        way + "assign\tcostfactor\u000b=\u001c1" + node, // whitespace in Java, though not the space char
        "---context:global\r\nassign x = 1\r\n---context:way\r\nassign costfactor = x\r\n---context:node\r\nassign initialcost = 0\r\n",
        way + "assign costfactor = 1e5x" + node,
        way + "assign costfactor = -" + node,
        way + "assign costfactor = 1 # a comment\nassign costfactor = 2" + node,
        "---context:global\nassign x = 1\n---context:way\nassign x = 2\nassign costfactor = 1" + node,
        way + "assign costfactor = 1\n---context:node\nassign initialcost = switch",
        way + "assign costfactor = 1\n---context:node\nassign initialcost = way:costfactor",
        way + "assign costfactor = 1\n---context:node\nassign initialcost = way:nosuchvariable",
        way + "assign costfactor = 1" + node + "---context:nosuchcontext\nassign x = 1\n",
        way + "assign costfactor = 99999999999999999999999999999999999999999" + node,
        way + "assign costfactor = highway=primary|" + node,
        way + "assign costfactor = highway=" + node,
    )

    /** What one engine did with a profile: the exception's class and message, or the two expression lists. */
    private fun outcome(parse: () -> Pair<String, String>): String = try {
        val (w, n) = parse()
        "parsed: way $w; node $n"
    } catch (e: Exception) {
        "threw ${e::class.java.name}: ${e.message}"
    }

    @Test
    fun malformedProfilesFailWithTheOraclesExceptionAndMessage() {
        val dir = Files.createTempDirectory("step4-malformed").toFile()
        val files = texts.mapIndexed { i, text -> File(dir, "malformed$i.brf").apply { writeText(text) } }
        val oracle = files.map { file ->
            val rc = ExpressionsEngines.foragerRoutingContext(file)
            outcome {
                val e = ExpressionsEngines.oracle(file, rc)
                ExpressionsEngines.oracleField(e.way, "expressionList").toString() to
                    ExpressionsEngines.oracleField(e.node, "expressionList").toString()
            }
        }
        val oracleThrew = oracle.count { it.startsWith("threw") }
        println("MalformedProfileTest: the oracle threw on $oracleThrew of ${texts.size} texts")
        for ((i, o) in oracle.withIndex()) println("MalformedProfileTest: text $i, oracle $o")
        // The check is only about errors if most of the texts are errors to the oracle; checked before the port runs.
        assertTrue(oracleThrew >= texts.size / 2, "the oracle threw on only $oracleThrew of ${texts.size} texts")
        val failures = ArrayList<String>()
        for ((i, file) in files.withIndex()) {
            val rc = ExpressionsEngines.foragerRoutingContext(file)
            val p = outcome {
                val e = ExpressionsEngines.port(file, rc)
                e.way.expressionList.toString() to e.node.expressionList.toString()
            }
            if (oracle[i] != p) failures += "text $i: oracle ${oracle[i]}; port $p"
        }
        assertEquals(emptyList(), failures, "${failures.size} of ${texts.size} texts differ")
    }
}
