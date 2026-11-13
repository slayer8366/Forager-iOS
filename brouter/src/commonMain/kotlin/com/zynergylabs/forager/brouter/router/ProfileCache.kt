package com.zynergylabs.forager.brouter.router

import com.zynergylabs.forager.brouter.expressions.BExpressionContextNode
import com.zynergylabs.forager.brouter.expressions.BExpressionContextWay
import com.zynergylabs.forager.brouter.expressions.BExpressionMetaData
import com.zynergylabs.forager.brouter.expressions.parseFile
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import kotlin.time.TimeSource

/**
 * Port of BRouter 1.7.10's `btools.router.ProfileCache`: the parsed profiles, kept for reuse.
 *
 * Java's three `static synchronized` methods lock the class. Here they lock [lock], atomicfu's `SynchronizedObject`
 * (the owner, RECORD -91), so two routes at once cannot race on [apc] on iOS either.
 *
 * Differences from Java, each with no effect on Forager's calls:
 * - **`profileBaseDir` is not read.** Java reads that JVM system property first. Forager does not set it, and iOS has
 *   no system properties, so the port always takes Java's null branch: the profile is `localFunction` itself.
 * - **`debugProfileCache` is not read**, and its two debug prints are left out (it is false unless that property is set).
 * - **`lastUseTime` is a monotonic time mark**, not `System.currentTimeMillis()`. It is only compared to choose the
 *   least recently used slot. Java's ties within one millisecond keep the first slot; here they are much rarer [I].
 */
internal class ProfileCache private constructor() {
    private var expctxWay: BExpressionContextWay? = null
    private var expctxNode: BExpressionContextNode? = null
    private var lastProfileFile: Path? = null
    private var lastProfileTimestamp: Long = 0
    private var profilesBusy: Boolean = false
    private var lastUseTime: TimeSource.Monotonic.ValueTimeMark = TimeSource.Monotonic.markNow()

    companion object {
        private val lock = SynchronizedObject()
        private var lastLookupFile: Path? = null
        private var lastLookupTimestamp: Long = 0
        private var apc = arrayOfNulls<ProfileCache>(1)

        /** Java's `File.lastModified()`: 0 when the file does not exist or its time is unknown. */
        private fun lastModified(fileSystem: FileSystem, file: Path): Long =
            fileSystem.metadataOrNull(file)?.lastModifiedAtMillis ?: 0L

        fun setSize(size: Int): Unit = synchronized(lock) {
            apc = arrayOfNulls(size)
        }

        /** Java's `parseProfile(RoutingContext)`, with the okio seam the port's file reads take. */
        fun parseProfile(rc: RoutingContext, fileSystem: FileSystem): Boolean = synchronized(lock) {
            val profileFile = rc.localFunction!!.toPath()
            val profileDir = profileFile.parent
            rc.profileTimestamp = (lastModified(fileSystem, profileFile) + rc.getKeyValueChecksum()) shl 24
            val lookupFile = profileDir?.div("lookups.dat") ?: "lookups.dat".toPath()

            // invalidate cache at lookup-table update
            if (!(lookupFile == lastLookupFile && lastModified(fileSystem, lookupFile) == lastLookupTimestamp)) {
                if (lastLookupFile != null) {
                    println("******** invalidating profile-cache after lookup-file update ******** ")
                }
                apc = arrayOfNulls(apc.size)
                lastLookupFile = lookupFile
                lastLookupTimestamp = lastModified(fileSystem, lookupFile)
            }

            var lru: ProfileCache? = null
            var unusedSlot = -1

            // check for re-use
            for (i in apc.indices) {
                val pc = apc[i]
                if (pc != null) {
                    if (!pc.profilesBusy && profileFile == pc.lastProfileFile) {
                        if (rc.profileTimestamp == pc.lastProfileTimestamp) {
                            rc.expctxWay = pc.expctxWay
                            rc.expctxNode = pc.expctxNode
                            rc.readGlobalConfig()
                            pc.profilesBusy = true
                            return@synchronized true
                        }
                        lru = pc // name-match but timestamp-mismatch -> we overide this one
                        unusedSlot = -1
                        break
                    }
                    if (lru == null || lru.lastUseTime > pc.lastUseTime) {
                        lru = pc
                    }
                } else if (unusedSlot < 0) {
                    unusedSlot = i
                }
            }

            val meta = BExpressionMetaData()
            val way = BExpressionContextWay(rc.memoryclass * 512, meta)
            val node = BExpressionContextNode(0, meta)
            rc.expctxWay = way
            rc.expctxNode = node
            node.setForeignContext(way)
            meta.readMetaData(fileSystem, lookupFile)
            way.parseFile(fileSystem, profileFile, "global", rc.keyValues)
            node.parseFile(fileSystem, profileFile, "global", rc.keyValues)
            rc.readGlobalConfig()
            if (rc.processUnusedTags) {
                way.setAllTagsUsed()
            }

            if (lru == null || unusedSlot >= 0) {
                lru = ProfileCache()
                if (unusedSlot >= 0) apc[unusedSlot] = lru
            }
            lru.lastProfileTimestamp = rc.profileTimestamp
            lru.lastProfileFile = profileFile
            lru.expctxWay = rc.expctxWay
            lru.expctxNode = rc.expctxNode
            lru.profilesBusy = true
            lru.lastUseTime = TimeSource.Monotonic.markNow()
            false
        }

        fun releaseProfile(rc: RoutingContext): Unit = synchronized(lock) {
            for (pc in apc) {
                // only the thread that holds the cached instance can release it
                if (pc != null && rc.expctxWay === pc.expctxWay && rc.expctxNode === pc.expctxNode) {
                    pc.profilesBusy = false
                    break
                }
            }
            rc.expctxWay = null
            rc.expctxNode = null
        }
    }
}
