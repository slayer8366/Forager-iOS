package com.zynergylabs.forager.brouter.oracle.e3

import java.io.File
import java.lang.management.ManagementFactory
import java.util.concurrent.TimeUnit

/**
 * Starts [E3Jvm] in a fresh JVM: this JVM's java, its `-XX:` flags (the libm setting build.gradle.kts picks for the
 * arch, so every regime's oracle is the no-intrinsic one), its classpath, and any [extraJvmArgs] (R6's agent).
 */
object E3Launcher {
    class Run(val regime: String, val output: List<String>) {
        /** Each `control <regime> <order> …` line, as (regime, order). */
        val controls: List<Pair<String, String>> = output.filter { it.startsWith("control ") }.map {
            val f = it.split(' ')
            f[1] to f[2]
        }

        /** The control-only run's one order ([E3ControlTest]). */
        val control: String get() = controls.single().second
    }

    /** The control alone, in a fresh JVM. */
    fun run(regime: String): Run = launch(regime, listOf(regime), emptyList(), 120)

    /** One regime over cases, in a fresh JVM; [timeoutSeconds] bounds the whole JVM. */
    fun run(regime: String, args: List<String>, extraJvmArgs: List<String>, timeoutSeconds: Long): Run =
        launch(regime, listOf(regime) + args, extraJvmArgs, timeoutSeconds)

    private fun launch(regime: String, args: List<String>, extraJvmArgs: List<String>, timeoutSeconds: Long): Run {
        val java = File(System.getProperty("java.home"), "bin/java").path
        val flags = ManagementFactory.getRuntimeMXBean().inputArguments.filter { it.startsWith("-XX:") }
        val cmd = listOf(java) + flags + extraJvmArgs +
            listOf("-cp", System.getProperty("java.class.path"), E3Jvm::class.java.name) + args
        // Output goes to a file, so a hung child cannot block a read: waitFor's timeout always applies.
        val log = File.createTempFile("e3-$regime-", ".log")
        val process = ProcessBuilder(cmd).redirectErrorStream(true).redirectOutput(log).start()
        if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
            process.destroyForcibly()
            error("$regime: the JVM did not exit within $timeoutSeconds s; killed. Output: ${log.readText()}")
        }
        val output = log.readLines()
        log.delete()
        check(process.exitValue() == 0) { "$regime: exit ${process.exitValue()}; output: $output" }
        check(output.any { it.startsWith("control ") }) { "$regime: no control line; output: $output" }
        return Run(regime, output)
    }
}
