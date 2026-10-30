package com.zynergylabs.forager.brouter.mapaccess

import com.zynergylabs.forager.brouter.codec.DataBuffers
import com.zynergylabs.forager.brouter.codec.MicroCache
import com.zynergylabs.forager.brouter.codec.WaypointMatcher
import com.zynergylabs.forager.brouter.expressions.BExpressionContextWay
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath

/**
 * Port of BRouter 1.7.10's `btools.mapaccess.NodesCache`, its file half (step 5a): the tile cache and the segment
 * reads. The graph half (`obtainNonHollowNode`, `expandHollowLinkTargets`, `hasHollowLinkTargets`, `getStartNode`,
 * `getGraphNode`, `matchWaypointsToNodes`, `preloadPosition`) needs `OsmNode`, `MatchedWaypoint` and the weaver, and
 * arrives with steps 5b and 5c. Java's `File segmentDir` is an okio [Path] read through [fileSystem] (item B, row 2).
 */
internal class NodesCache(
    private val fileSystem: FileSystem,
    segmentDir: Path,
    ctxWay: BExpressionContextWay?,
    forceSecondaryData: Boolean,
    maxmem: Long,
    oldCache: NodesCache?,
    detailed: Boolean,
) {
    private val segmentDir: Path = segmentDir
    private var secondarySegmentsDir: Path? = null

    var nodesMap: OsmNodesMap
    private val expCtxWay: BExpressionContextWay? = ctxWay
    private val lookupVersion: Int
    private val lookupMinorVersion: Int
    private val forceSecondaryData: Boolean = forceSecondaryData
    private var currentFileName: String? = null

    private val fileCache: HashMap<String, PhysicalFile?>
    private val dataBuffers: DataBuffers

    private var fileRows: Array<Array<OsmFile>?>

    var waypointMatcher: WaypointMatcher? = null

    var first_file_access_failed: Boolean = false
    var first_file_access_name: String? = null

    private var cacheSum = 0L
    private var maxmemtiles: Long = maxmem / 8
    private val detailed: Boolean = detailed

    private var garbageCollectionEnabled = false
    private var ghostCleaningDone = false

    private var cacheSumClean = 0L
    private var ghostSum = 0L
    private var ghostWakeup = 0L

    /** Java: `!Boolean.getBoolean("disableDirectWeaving")`. Forager never sets the property. */
    private val directWeaving = true

    init {
        nodesMap = OsmNodesMap()
        nodesMap.maxmem = (2L * maxmem) / 3L
        // Java reads ctxWay.meta before its own null check, so a null context throws here, as there.
        lookupVersion = ctxWay!!.meta!!.lookupVersion.toInt()
        lookupMinorVersion = ctxWay.meta.lookupMinorVersion.toInt()
        ctxWay.setDecodeForbidden(detailed)

        first_file_access_failed = false
        first_file_access_name = null

        if (fileSystem.metadataOrNull(segmentDir)?.isDirectory != true) {
            // Java prints File.getAbsolutePath(); Forager passes an absolute folder, so the path prints as given.
            throw RuntimeException("segment directory $segmentDir does not exist")
        }

        if (oldCache != null) {
            fileCache = oldCache.fileCache
            dataBuffers = oldCache.dataBuffers
            secondarySegmentsDir = oldCache.secondarySegmentsDir

            // re-use old, virgin caches (if same detail-mode)
            if (oldCache.detailed == detailed) {
                fileRows = oldCache.fileRows
                for (fileRow in fileRows) {
                    if (fileRow == null) continue
                    for (osmf in fileRow) {
                        cacheSum += osmf.setGhostState()
                    }
                }
            } else {
                fileRows = arrayOfNulls(180)
            }
        } else {
            fileCache = HashMap(4)
            fileRows = arrayOfNulls(180)
            dataBuffers = DataBuffers()
            secondarySegmentsDir = StorageConfigHelper.getSecondarySegmentDir(fileSystem, segmentDir)
        }
        ghostSum = cacheSum
    }

    fun formatStatus(): String =
        "collecting=$garbageCollectionEnabled noGhosts=$ghostCleaningDone cacheSum=$cacheSum " +
            "cacheSumClean=$cacheSumClean ghostSum=$ghostSum ghostWakeup=$ghostWakeup"

    fun clean(all: Boolean) {
        for (fileRow in fileRows) {
            if (fileRow == null) continue
            for (osmf in fileRow) {
                osmf.clean(all)
            }
        }
    }

    // if the cache sum exceeded a threshold,
    // clean all ghosts and enable garbage collection
    private fun checkEnableCacheCleaning() {
        if (cacheSum < maxmemtiles) {
            return
        }
        for (i in fileRows.indices) {
            val fileRow = fileRows[i] ?: continue
            for (osmf in fileRow) {
                if (garbageCollectionEnabled && !ghostCleaningDone) {
                    cacheSum -= osmf.cleanGhosts()
                } else {
                    cacheSum -= osmf.collectAll()
                }
            }
        }
        if (garbageCollectionEnabled) {
            ghostCleaningDone = true
            maxmemtiles *= 2
        } else {
            cacheSumClean = cacheSum
            garbageCollectionEnabled = true
        }
    }

    fun loadSegmentFor(ilon: Int, ilat: Int): Int {
        val mc = getSegmentFor(ilon, ilat)
        return mc?.getSize() ?: 0
    }

    fun getSegmentFor(ilon: Int, ilat: Int): MicroCache? {
        try {
            val lonDegree = ilon / 1000000
            val latDegree = ilat / 1000000
            var osmf: OsmFile? = null
            val fileRow = fileRows[latDegree]
            val ndegrees = fileRow?.size ?: 0
            for (i in 0 until ndegrees) {
                if (fileRow!![i].lonDegree == lonDegree) {
                    osmf = fileRow[i]
                    break
                }
            }
            if (osmf == null) {
                val created = fileForSegment(lonDegree, latDegree)
                osmf = created
                fileRows[latDegree] = Array(ndegrees + 1) { i -> if (i < ndegrees) fileRow!![i] else created }
            }
            currentFileName = osmf.filename

            if (!osmf.hasData()) {
                return null
            }

            var segment = osmf.getMicroCache(ilon, ilat)

            if (segment == null || (waypointMatcher != null && (waypointMatcher as WaypointMatcherImpl).useDynamicRange)) {
                checkEnableCacheCleaning()
                segment = osmf.createMicroCache(
                    ilon, ilat, dataBuffers, expCtxWay, waypointMatcher, if (directWeaving) nodesMap else null,
                )
                cacheSum += segment.getDataSize()
            } else if (segment.ghost) {
                segment.unGhost()
                ghostWakeup += segment.getDataSize()
            }
            return segment
        } catch (re: IOException) {
            throw RuntimeException(re.message)
        } catch (re: RuntimeException) {
            throw re
        } catch (e: Exception) {
            throw RuntimeException("error reading datafile $currentFileName: $e", e)
        }
    }

    private fun fileForSegment(lonDegree: Int, latDegree: Int): OsmFile {
        val lonMod5 = lonDegree % 5
        val latMod5 = latDegree % 5

        val lon = lonDegree - 180 - lonMod5
        val slon = if (lon < 0) "W" + (-lon) else "E$lon"
        val lat = latDegree - 90 - latMod5

        val slat = if (lat < 0) "S" + (-lat) else "N$lat"
        val filenameBase = slon + "_" + slat

        currentFileName = "$filenameBase.rd5"

        var ra: PhysicalFile? = null
        if (!fileCache.containsKey(filenameBase)) {
            var f: Path? = null
            if (!forceSecondaryData) {
                val primary = segmentDir / "$filenameBase.rd5"
                if (fileSystem.exists(primary)) {
                    f = primary
                }
            }
            if (f == null) {
                // Java's new File((File) null, name) is the bare name, read against the process's working folder;
                // a relative okio path is read against the same folder.
                val secondary = secondarySegmentsDir?.let { it / "$filenameBase.rd5" } ?: "$filenameBase.rd5".toPath()
                if (fileSystem.exists(secondary)) {
                    f = secondary
                }
            }
            if (f != null) {
                currentFileName = f.name
                ra = PhysicalFile(fileSystem, f, dataBuffers, lookupVersion, lookupMinorVersion)
            }
            fileCache[filenameBase] = ra
        }
        ra = fileCache[filenameBase]
        val osmf = OsmFile(ra, lonDegree, latDegree, dataBuffers)

        if (first_file_access_name == null) {
            first_file_access_name = currentFileName
            first_file_access_failed = osmf.filename == null
        }

        return osmf
    }

    fun close() {
        for (f in fileCache.values) {
            // Java ignores an IOException from close, as here.
            f?.close()
        }
    }

    fun getElevationType(ilon: Int, ilat: Int): Int {
        val lonDegree = ilon / 1000000
        val latDegree = ilat / 1000000
        val fileRow = fileRows[latDegree]
        val ndegrees = fileRow?.size ?: 0
        for (i in 0 until ndegrees) {
            if (fileRow!![i].lonDegree == lonDegree) {
                return fileRow[i].elevationType.toInt()
            }
        }
        return 3
    }
}
