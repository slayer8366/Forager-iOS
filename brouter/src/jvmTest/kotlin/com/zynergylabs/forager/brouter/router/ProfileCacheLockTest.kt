package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.expressions.ExpressionsEngines
import okio.FileMetadata
import okio.FileSystem
import okio.ForwardingFileSystem
import okio.Path
import okio.Path.Companion.toOkioPath
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * `ProfileCache`'s lock (the owner, RECORD -91): two threads call `parseProfile` at once, and a file system that
 * lingers on every read of the profile's metadata counts how many threads are inside at the same time. Java's
 * `static synchronized` lets in one. Without the lock both threads overlap, so the count reads 2.
 */
class ProfileCacheLockTest {
    private val profile = File(ExpressionsEngines.profilesDir, "hiking-mountain.brf")

    private class Lingering(private val watched: Path) : ForwardingFileSystem(FileSystem.SYSTEM) {
        val inside = AtomicInteger()
        val most = AtomicInteger()

        override fun metadataOrNull(path: Path): FileMetadata? {
            if (path != watched) return super.metadataOrNull(path)
            most.accumulateAndGet(inside.incrementAndGet(), ::maxOf)
            try {
                Thread.sleep(150)
                return super.metadataOrNull(path)
            } finally {
                inside.decrementAndGet()
            }
        }
    }

    @Test
    fun parseProfileLetsOneThreadInAtATime() {
        ProfileCache.setSize(2)
        val fs = Lingering(profile.toOkioPath())
        val start = CountDownLatch(1)
        val rcs = List(2) { ExpressionsEngines.foragerRoutingContext(profile) }.map { orc ->
            RoutingContext().apply {
                localFunction = orc.localFunction
                keyValues = orc.keyValues
                memoryclass = orc.memoryclass
            }
        }
        val errors = arrayOfNulls<Throwable>(2)
        val threads = rcs.mapIndexed { i, rc ->
            Thread {
                start.await()
                errors[i] = runCatching { ProfileCache.parseProfile(rc, fs) }.exceptionOrNull()
            }.apply { start() }
        }
        start.countDown()
        threads.forEach { it.join(30_000) }
        errors.forEach { if (it != null) throw it }
        rcs.forEach { assertNotNull(it.expctxWay, "a thread's profile was not parsed") }
        assertEquals(1, fs.most.get(), "threads inside parseProfile at once")
        rcs.forEach { ProfileCache.releaseProfile(it) }
    }
}
