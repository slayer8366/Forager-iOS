@file:OptIn(ExperimentalForeignApi::class)

package com.zynergylabs.forager.brouter.tiles

import com.zynergylabs.forager.brouter.mapaccess.PeninsulaDepth
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.alloc
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.value
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import platform.posix.fclose
import platform.posix.fflush
import platform.posix.fopen
import platform.posix.fputs
import platform.posix.pthread_attr_destroy
import platform.posix.pthread_attr_init
import platform.posix.pthread_attr_setstacksize
import platform.posix.pthread_attr_t
import platform.posix.pthread_create
import platform.posix.pthread_get_stacksize_np
import platform.posix.pthread_join
import platform.posix.pthread_main_np
import platform.posix.pthread_self
import platform.posix.pthread_tVar
import kotlin.test.Test
import kotlin.test.fail

/**
 * Step 10, the stack ladder: session 51 (RECORD -142, decision 1) made it, one MP64 case read by eye; session 52 (RECORD
 * -144, the owner's ruling "Rewrite without recursion") makes it assert. [LADDER_CASES], each run first on the test's own
 * thread and then through [EngineRun] on threads made with an explicit stack size, joined before the next: [SIZES], 8 MB
 * down to 512 KB. **Every case must survive every size and give its stored row** (all six fields and the tuple digest)
 * with the same deepest walk as on the test's thread. Before each rung it prints "ladder: starting <case> <bytes>"; after
 * it, "ladder: survived <case> <bytes>; equal to its stored row <b>; deepest <n>" and the thread's own stack as
 * `pthread_get_stacksize_np` reads it. A stack overflow ends the process on iOS (no catch there), so a rung that ends it
 * leaves its "starting" line as the last ladder line, and the step fails: that is the test's red.
 *
 * The thread is a POSIX one: `pthread_attr_setstacksize` on an attribute, `pthread_create` with a [staticCFunction]
 * entry ([rungEntry]) and a [StableRef] to the [Rung] as its argument, `pthread_join`. Kotlin/Native's `Worker` takes no
 * stack size. On the new memory manager (the default since Kotlin 1.7.20) the runtime attaches a foreign thread itself
 * when a C-to-Kotlin bridge such as a [staticCFunction] is entered (`initRuntimeIfNeeded` is deprecated: "Initializing
 * runtime is not possible in the new memory model"), and nothing is frozen, so the engine's objects and [PeninsulaDepth]
 * are the same on that thread. The entry catches every Throwable and hands back its class name only, so no throw leaves
 * the thread (an uncaught one would end the process and read as an overflow).
 *
 * Each ladder line is also appended, and flushed, to `ladder.txt` beside the NT file, so it outlives a process that ends:
 * the test's printed output reaches the log only through the JUnit XML, which a process that ends may not complete.
 *
 * Gated on [LADDER_GATE] (only `ios-memory`'s last step sets it, as `SIMCTL_CHILD_`[LADDER_GATE]); unset, it prints one
 * line and does nothing. Native only: the question is a native thread's stack (RECORD -143). Nothing of a
 * case but its id is printed or written (E1): sizes, booleans, depths and the outcome's tuple digest equality.
 */
internal const val LADDER_GATE = "BROUTER_STEP10_LADDER"

/** 8 MB, 4 MB, 2 MB, 1 MB, 512 KB (RECORD -142, decision 1), in that order. */
internal val SIZES: List<Long> = listOf(8L shl 20, 4L shl 20, 2L shl 20, 1L shl 20, 512L shl 10)

/** One rung's work and what it saw; written on the rung's thread, read after the join. */
private class Rung(val lonLats: String, val profile: Path, val segments: Path, val memoryclass: Int) {
    var outcome: EngineRun.Outcome? = null
    var deepest = 0
    var stackBytes: Long = -1
    var mainThread = true
    var thrown: String? = null
}

private fun rungEntry(arg: COpaquePointer?): COpaquePointer? {
    val rung = arg!!.asStableRef<Rung>().get()
    try {
        rung.stackBytes = pthread_get_stacksize_np(pthread_self()).toLong()
        rung.mainThread = pthread_main_np() == 1
        PeninsulaDepth.reset()
        rung.outcome = EngineRun.run(rung.lonLats, rung.profile, rung.segments, rung.memoryclass)
        rung.deepest = PeninsulaDepth.deepest
    } catch (t: Throwable) {
        rung.thrown = t::class.simpleName ?: "Throwable"
    }
    return null
}

/** [rung] on a new thread of [stackBytes], joined. */
private fun onThread(stackBytes: Long, rung: Rung) {
    val ref = StableRef.create(rung)
    try {
        memScoped {
            val attr = alloc<pthread_attr_t>()
            check(pthread_attr_init(attr.ptr) == 0) { "pthread_attr_init failed" }
            try {
                val set = pthread_attr_setstacksize(attr.ptr, stackBytes.convert())
                if (set != 0) fail("pthread_attr_setstacksize($stackBytes) returned $set")
                val thread = alloc<pthread_tVar>()
                val created = pthread_create(thread.ptr, attr.ptr, staticCFunction(::rungEntry), ref.asCPointer())
                if (created != 0) fail("pthread_create with a $stackBytes-byte stack returned $created")
                val joined = pthread_join(thread.value, null)
                if (joined != 0) fail("pthread_join returned $joined")
            } finally {
                pthread_attr_destroy(attr.ptr)
            }
        }
    } finally {
        ref.dispose()
    }
}

/** One ladder case: its group, the group's run-time file beside the NT file, the group's stored rows, and its id. */
private class LadderCase(val group: String, val file: String, val rows: List<NtExpected.Row>, val id: String)

/**
 * The deepest case of each memory group, as `MemoryEngineCommonTest`'s depth lines named them in run 43212726248 (each
 * 5,679 deep), and MP64-0002 (3,378 deep), the case that run's ladder ended the process on at 1 MB (RECORD -143). Named
 * here, not found: finding each group's deepest on the simulator would run its 55 to 59 cases again in this step.
 * MP64-0002 first, so the reading -143 recorded is the first one this test repeats.
 */
private val LADDER_CASES: List<LadderCase> = listOf(
    LadderCase("MP64", "mp64-cases.tsv", Mp64Expected.ROWS, "MP64-0002"),
    LadderCase("MP64", "mp64-cases.tsv", Mp64Expected.ROWS, "MP64-0005"),
    LadderCase("ML5", "ml5-cases.tsv", Ml5Expected.ROWS, "M5-0060"),
    LadderCase("PM5", "pm5-cases.tsv", Pm5Expected.ROWS, "M5-0304"),
)

class StackLadderIosTest {
    @Test
    fun deepCasesOnThreadsOfFallingStackSizeSurviveAndGiveTheirStoredRows() {
        if (testEnvironment(LADDER_GATE).isNullOrEmpty()) {
            println("ladder: $LADDER_GATE is unset; not run (RECORD -142)")
            return
        }
        val nt = Step10Cases.ntFile("MP64") ?: return
        val log = nt.parent!! / "ladder.txt"
        FileSystem.SYSTEM.delete(log)
        fun say(line: String) {
            println(line)
            fflush(null)
            val f = fopen(log.toString(), "a") ?: fail("cannot open $log for appending")
            try {
                fputs(line + "\n", f)
                fflush(f)
            } finally {
                fclose(f)
            }
        }
        val segments = (testSetting("brouter.tiles.dir", "BROUTER_TILES_DIR") ?: fail("BROUTER_TILES_DIR is not set")).toPath() / "segments"
        val profiles = (testSetting("brouter.oracle.dir", "BROUTER_ORACLE_DIR") ?: fail("BROUTER_ORACLE_DIR is not set")).toPath() / "profiles2"

        val failures = ArrayList<String>()
        var rungs = 0
        for (case in LADDER_CASES) {
            val row = case.rows.firstOrNull { it.id == case.id } ?: fail("ladder: ${case.id} is not a stored ${case.group} row")
            val f = FileSystem.SYSTEM.read(nt.parent!! / case.file) { readUtf8() }.lines()
                .map { it.split('\t') }.firstOrNull { it[0] == case.id }
                ?: fail("ladder: ${case.id} is not in ${case.file}")
            if (f.size != 7) fail("ladder: ${case.id}'s line of ${case.file} has ${f.size} fields, not 7")
            val template = Rung("${f[3]},${f[4]}|${f[5]},${f[6]}", profiles / "${f[1]}.brf", segments, f[2].toInt())

            // On the test's own thread first: the depth every rung must repeat.
            PeninsulaDepth.reset()
            val own = EngineRun.run(template.lonLats, template.profile, template.segments, template.memoryclass)
            val ownDeepest = PeninsulaDepth.deepest
            val ownEqual = own.tupleDigest == row.tupleDigest && own == row.outcome
            say("ladder: case ${case.id} (${case.group}); on the test's thread deepest $ownDeepest, equal to its stored row $ownEqual; ${testThreadStack()}")
            if (!ownEqual) failures += "${case.id} on the test's thread: unequal to its stored row"
            if (ownDeepest == 0) failures += "${case.id} on the test's thread: did not enter the walk"

            for (size in SIZES) {
                say("ladder: starting ${case.id} $size")
                val rung = Rung(template.lonLats, template.profile, template.segments, template.memoryclass)
                onThread(size, rung)
                rungs++
                val thrown = rung.thrown
                val got = rung.outcome
                if (thrown != null || got == null) {
                    failures += "${case.id} $size: ${thrown?.let { "threw $it" } ?: "no outcome"}"
                    say("ladder: ended ${case.id} $size without an outcome; ${thrown?.let { "threw $it" } ?: "no outcome"}")
                    continue
                }
                val equal = got.tupleDigest == row.tupleDigest && got == row.outcome
                if (!equal) failures += "${case.id} $size: unequal to its stored row"
                if (rung.deepest != ownDeepest) failures += "${case.id} $size: deepest ${rung.deepest}, not $ownDeepest"
                say(
                    "ladder: survived ${case.id} $size; equal to its stored row $equal; " +
                        "deepest ${rung.deepest}; the thread's stack ${rung.stackBytes} bytes (pthread_get_stacksize_np); " +
                        "main thread ${rung.mainThread}",
                )
            }
        }
        say("ladder: done; ${LADDER_CASES.size} cases, $rungs of ${LADDER_CASES.size * SIZES.size} rungs ran; failures ${failures.size}")
        if (failures.isNotEmpty()) fail("ladder: ${failures.size} failures: $failures")
    }
}
