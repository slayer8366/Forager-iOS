package com.zynergylabs.forager.brouter.oracle.e3

import org.jacoco.core.analysis.Analyzer
import org.jacoco.core.analysis.CoverageBuilder
import org.jacoco.core.tools.ExecFileLoader
import java.io.File
import java.util.jar.JarFile

/**
 * The coverage reader (step 1b, RECORD -51, -77): the first code to read R6's JaCoCo `.exec` files. It reads named source
 * lines of the oracle and says whether R6 executed them, as NT's and CR's reach (E4).
 *
 * - **RECORD -50's rule:** coverage counts as reach evidence only for a case whose R6 tuple equals its R1 tuple. [reach]
 *   refuses the reading otherwise, and says so; it never reads the file.
 * - **The class files are the ones R6 ran:** the oracle jar on this JVM's classpath, which every regime's JVM is started
 *   with ([E3Launcher]). JaCoCo matches execution data to a class by a checksum of its bytes, so a class whose data names
 *   it but whose checksum differs (another build) is refused, never read as "not covered".
 * - **A line is covered** when R6 executed at least one of its instructions. A named line with no instructions (a blank,
 *   a brace, a comment, or the wrong file) is refused: it cannot be read either way.
 * - **A span is reached** when every line in it is covered; a reading is reached when every span is.
 */
object E3Coverage {
    /** Lines [first] to [last] of [source], a path under the oracle's sources, e.g. `btools/router/StdPath.java`. */
    class Span(val source: String, val first: Int, val last: Int = first) {
        init {
            require(source.endsWith(".java") && first in 1..last) { "a span is a .java path and lines first..last: $source:$first-$last" }
        }

        val pkg: String get() = source.substringBeforeLast('/')
        val file: String get() = source.substringAfterLast('/')
        override fun toString() = "${file}:$first" + if (last != first) "-$last" else ""
    }

    /** One line's instructions as R6 ran them. */
    class Line(val number: Int, val covered: Int, val total: Int)

    /**
     * A reading: [reached] is null when it was refused ([refused] says why), else whether every span's lines are covered.
     * [lines] holds every named line's counts, for the log.
     */
    class Reading(val reached: Boolean?, val refused: String?, val lines: Map<Span, List<Line>>) {
        /** One field for `select.tsv`: the verdict and each span's covered lines over its lines. */
        fun describe(): String = when (reached) {
            null -> "coverage refused: $refused"
            else -> (if (reached) "covered" else "not covered") + " (" + lines.entries.joinToString("; ") { (s, ls) ->
                "$s ${ls.count { it.covered > 0 }}/${ls.size} lines"
            } + ")"
        }
    }

    /** -50's rule, then [read]. */
    fun reach(r1: E3Outcome, r6: E3Outcome, exec: File, spans: List<Span>): Reading =
        if (r6 != r1) {
            Reading(null, "R6's tuple differs from R1's (RECORD -50): R1 ${r1.toField()}, R6 ${r6.toField()}", emptyMap())
        } else {
            val lines = read(exec, spans)
            Reading(lines.values.all { ls -> ls.all { it.covered > 0 } }, null, lines)
        }

    /** Every named line's instruction counts from [exec]. Throws on an empty or foreign file, or a line with no code. */
    fun read(exec: File, spans: List<Span>): Map<Span, List<Line>> {
        require(exec.isFile && exec.length() > 0) { "no execution data at $exec" }
        val loader = ExecFileLoader().also { it.load(exec) }
        check(loader.sessionInfoStore.infos.isNotEmpty()) { "$exec holds no session: not a JaCoCo agent's dump" }
        check(loader.executionDataStore.contents.isNotEmpty()) { "$exec holds no class data" }
        val builder = CoverageBuilder()
        val analyzer = Analyzer(loader.executionDataStore, builder)
        for (pkg in spans.map { it to it.source.removeSuffix(".java") }.distinctBy { it.second }) {
            val (span, outer) = pkg
            val classes = classFiles(outer)
            check(classes.isNotEmpty()) { "no class file for $span on the classpath" }
            for ((name, bytes) in classes) analyzer.analyzeClass(bytes, name)
        }
        check(builder.noMatchClasses.isEmpty()) {
            "$exec has data for ${builder.noMatchClasses.map { it.name }} whose checksum is not the classpath's: another build, refused"
        }
        return spans.associateWith { span ->
            val sf = builder.sourceFiles.singleOrNull { it.packageName == span.pkg && it.name == span.file }
                ?: error("$span: the class files name no such source")
            (span.first..span.last).map { n ->
                val c = sf.getLine(n).instructionCounter
                check(c.totalCount > 0) { "$span: line $n has no instructions; a named line must hold code" }
                Line(n, c.coveredCount, c.totalCount)
            }
        }
    }

    /** [outer]'s class file and its nested ones (`Outer$1` …), from the jar this JVM loads it from. */
    private fun classFiles(outer: String): List<Pair<String, ByteArray>> {
        val url = ClassLoader.getSystemResource("$outer.class") ?: return emptyList()
        check(url.protocol == "jar") { "$outer is loaded from $url, not the oracle jar" }
        val jarPath = File(java.net.URI(url.path.substringBefore("!/")))
        return JarFile(jarPath).use { jar ->
            jar.entries().asSequence()
                .filter { it.name == "$outer.class" || (it.name.startsWith("$outer$") && it.name.endsWith(".class")) }
                .map { it.name to jar.getInputStream(it).use { s -> s.readBytes() } }
                .toList()
        }
    }

    /** NT's reach (E4): the comparator's second line, `WaypointMatcherImpl.java:71` at v1.7.10. */
    val NT = listOf(Span("btools/mapaccess/WaypointMatcherImpl.java", 71))

    /** CR's reach (E4): `StdPath.java:101-103` (the description is printed) and `:105-113` (a class is read). */
    val CR = listOf(Span("btools/router/StdPath.java", 101, 103), Span("btools/router/StdPath.java", 105, 113))
}
