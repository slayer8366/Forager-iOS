package com.zynergylabs.forager.brouter.tiles

/**
 * Session 69: [RouteWaysCommonTest]'s stored rows, one per `readRouteWays` call on the F0h ends' cells. Drawn on the JVM
 * from `readRouteWays` itself, so they are the port's own output; they reach the Java engine only through
 * `RouteWaysEntryTest` (jvmTest), which holds the same entry on the same points and cells to the Java walk link by link.
 * Each row: the call's id (end name, from or to, square index), the square's divisor, the count and [EngineRun.fnv1a]
 * digest of the cells asked, the link count, the total point count, and [EngineRun.fnv1aText] over every link's
 * description, iLat and iLon in walk order. No position and no cell index (E1). Design:
 * `docs/audits/2026-11-13-brouter-port-closing/s69-route-ways-common-sibling.md`.
 */
internal object RouteWaysExpected {
    class Row(
        val id: String, val divisor: Int, val cells: Int, val cellsDigest: String, val links: Int, val points: Int,
        val digest: String,
    )

    /** The divisor `OtherDivisor` carries when the first end's square is asked for at 1/80. */
    const val OTHER_DIVISOR = 32

    val ROWS: List<Row> = listOf(
        Row("inside-across-the-degree-line/from square 1 of 1", 32, 1, "eab707d2107f927e", 405, 2457, "74388590e84ee41e"),
        Row("inside-across-the-degree-line/to square 1 of 1", 32, 1, "b0b89cf6e48981d8", 314, 2848, "85fb29699c620855"),
        Row("inside-short/from square 1 of 1", 32, 2, "475693d3a581681a", 601, 4467, "b57311da86782b58"),
        Row("inside-short/to square 1 of 1", 32, 1, "65e2a8ffd7bf13c9", 196, 2010, "2acb56ed725f81c3"),
        Row("inside-long/from square 1 of 1", 32, 1, "774a121705c0c53c", 72, 888, "7c7f76557a71ad18"),
        Row("inside-long/to square 1 of 1", 32, 1, "758253d9c9b579af", 261, 2344, "e58deeb5a066e445"),
        Row("edge-north-a/from square 1 of 1", 32, 2, "22e720668dbb1fae", 575, 5192, "4a71766fcd1fb6f5"),
        Row("edge-north-a/to square 1 of 1", 32, 1, "2155e1ebef6ac6d", 127, 1138, "8867edf025245ca8"),
        Row("edge-north-b/from square 1 of 1", 32, 2, "22e720668dbb1fae", 575, 5192, "4a71766fcd1fb6f5"),
        Row("edge-north-b/to square 1 of 1", 32, 1, "2155e1ebef6ac6d", 127, 1138, "8867edf025245ca8"),
        Row("detour-west/from square 1 of 1", 32, 1, "9957f1780596f12e", 70, 1345, "81ef8ccb213fd333"),
        Row("detour-west/to square 1 of 1", 32, 1, "4e81fd80f8cc831f", 125, 1605, "a4ea9684005b5645"),
        Row("outside-west-island/from square 1 of 1", 32, 2, "475693d3a581681a", 601, 4467, "b57311da86782b58"),
        Row("outside-west-island/to square 1 of 1", 32, 1, "e581c94a4535df0a", 104, 1724, "4e35740cee012d7"),
        Row("outside-west-moved/from square 1 of 1", 32, 2, "475693d3a581681a", 601, 4467, "b57311da86782b58"),
        Row("outside-west-moved/to square 1 of 1", 32, 1, "e581c94a4535df0a", 104, 1724, "4e35740cee012d7"),
    )
}
