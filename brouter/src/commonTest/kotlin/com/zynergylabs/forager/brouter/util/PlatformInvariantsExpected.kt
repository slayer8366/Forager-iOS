package com.zynergylabs.forager.brouter.util

// H step 4's expected digests (PlatformDigests.kt), written on the JVM by PlatformInvariantsExpectedTest, which regenerates
// them from outside the port and fails if they move: the table from bionic's C cos through the oracle's arithmetic, the
// angles from the oracle's own getAngle and getDirection. Written locally on OpenJDK 21.0.12.1 (x86_64, amd64) with
// -XX:-UseLibmIntrinsic before the port existed; the CI jvm job (Temurin 21.0.13+9, ubuntu-24.04, x86_64) checks them on
// every run. Change them only by a deliberate commit that says why.

/** 18 digests, rows 0-99, 100-199, ..., 1700-1799: lat, cos, kx, ky of each row. */
internal val EXPECTED_TABLE_CHUNKS: List<String> = listOf(
    "ea4210e831972a74", "daf50cffc1163a72", "4d8db266992ec9c0", "08d2b644a387ce5a",
    "3e8a28495737234f", "8dc9c2991cebf9e8", "f6f06909e13a8363", "2376e0e833ce758a",
    "2e6d23ce0cd4a60d", "33d899c52a32b689", "a415bd6caf03de51", "0d47a4aacf924679",
    "1fb130b5d3d95d40", "cbf66173f950da2d", "45b319af5e2d51d8", "98f14df8061117d7",
    "09c804733ff1e5e8", "2d49237ccc68520d",
)

/** 30 digests, pairs 0-9,999, ..., 290,000-299,973 (forEachAtan2Pair's order): getAngle, getDirection, four signs each. */
internal val EXPECTED_ANGLE_CHUNKS: List<String> = listOf(
    "97891f18c709d33c", "c5b083337e7bda1c", "97e823e03d0a770c", "1d2d159102d15cd5",
    "8c7267420d8cd1a6", "bc9123ef7f85be55", "4dddf67c81a43c1c", "4f25f73f5f8deeb3",
    "b010fa208317b1f9", "1dc3820d0ad353cb", "22885ad1432321f4", "8ec7b9af3182f1fb",
    "544f01e5f7e34943", "d261ccb31e136089", "077760113e0535d2", "55522d437d05db70",
    "fd5aee52b40b4110", "880cf56bf6072bb1", "780064a5c33275e3", "a70708ca159aed93",
    "37a6fc5c0860583f", "e49ec9771f676c3e", "1346e5f2b4520ada", "c6f60f7885b04c16",
    "2a0ba60fa62b7d51", "5a5ee2b367bf6a47", "e3cc80e436c97474", "e2dd80bca492bdd1",
    "d082b71a5442b107", "a410b7aaa9df8ba1",
)
