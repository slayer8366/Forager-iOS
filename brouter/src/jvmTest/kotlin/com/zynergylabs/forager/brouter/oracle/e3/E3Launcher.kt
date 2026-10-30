package com.zynergylabs.forager.brouter.oracle.e3

import java.io.File
import java.lang.management.ManagementFactory
import java.util.concurrent.TimeUnit

/**
 * Starts [E3Jvm] in a fresh JVM: this JVM's java, its `-XX:` flags (the libm setting build.gradle.kts picks for the
 * arch, so every regime's oracle is the no-intrinsic one), and its classpath.
 */
object E3Launcher {
    class Run(val regime: String, val control: String, val output: List<String>)

    fun run(regime: String): Run {
        val java = File(System.getProperty("java.home"), "bin/java").path
        val flags = ManagementFactory.getRuntimeMXBean().inputArguments.filter { it.startsWith("-XX:") }
        val cmd = listOf(java) + flags + listOf("-cp", System.getProperty("java.class.path"), E3Jvm::class.java.name, regime)
        // Output goes to a file, so a hung child cannot block a read: waitFor's timeout always applies.
        val log = File.createTempFile("e3-$regime-", ".log")
        val process = ProcessBuilder(cmd).redirectErrorStream(true).redirectOutput(log).start()
        if (!process.waitFor(120, TimeUnit.SECONDS)) {
            process.destroyForcibly()
            error("$regime: the JVM did not exit within 120 s; killed. Output: ${log.readText()}")
        }
        val output = log.readLines()
        log.delete()
        check(process.exitValue() == 0) { "$regime: exit ${process.exitValue()}; output: $output" }
        val lines = output.filter { it.startsWith("control ") }
        check(lines.size == 1) { "$regime: expected one control line, got ${lines.size}; output: $output" }
        return Run(regime, lines.single().removePrefix("control ").substringBefore(" perturbation-sum"), output)
    }
}
