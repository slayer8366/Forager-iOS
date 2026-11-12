# BRouter port, step 10 (the simulator run): survey

Plumb Ledger, session 44, 2026-11-12, on `brouter-port-5b`. A survey only: no code for step 10 was written. Every iOS
result cited is **simulator-checked**. Line numbers are from `grep -n`. Marks: [C] read in source, [I] inferred, [?] open.

**The plan's row** (`docs/audits/2026-10-28-brouter-port-verify.md:1705`): "first, one test that reads a tile from the
runner's disk (part 5's open question). Then the stored digests, the full list or E5's fallback of 312 plus every targeted
case, by K"; needs step 9; covers E6's iOS rows. Part 5's question is `:1010` (E7, "[?]. Every route stratum on iOS depends
on it") and `:1876` (tiles from the runner's disk, test resource paths, Kotlin/Native's stack, the 7 GB runner, FK 7 and 8's
message). E5's fallback (`:958-960`): every targeted, FK and F0 case plus the first 12 random cases per profile (312),
bounding the rate below 0.956% on a clean run, reported as weaker than the JVM's. E6's iOS production row (`:982`): under
(c), "exact against the no-intrinsic oracle's digests".

**(a) What runs on the simulator today [C].** `ios-smoke` runs `:brouter:iosSimulatorArm64Test` (`brouter.yml:76`), all of
commonTest; `brouter/build.gradle.kts:102-104` gives commonTest only `kotlin("test")`. 24 tests in 10 classes:
`JavaTextCommonTest` 3, `JavaLangCommonTest` 3, `PlatformInvariantsTest` 6, `CheapProductionTableTest` 2,
`FloatTextCommonTest` 2, `ToolchainSmokeTest` 1, `MsunAtan2PairsTest` 1, `MsunCosTableTest` 2, `MsunEdgesTest` 3,
`NodeHashMapTieBreakTest` 1 (counts by `grep -c '@Test'`). None routes: no commonTest file calls `RoutingEngine(`.

**(b) Tiles on iOS [C, then ?].** No test reads a tile file on iOS today. The only tile name in commonTest is a path
string, `ToolchainSmokeTest.kt:16` (`"segments4/W75_N40.rd5".toPath()`, split, never opened). The tiles are fetched by
`fetchTiles`, hung off jvmTest only (`build.gradle.kts:38-39`, "only jvmTest sees the tiles"), and passed as system
properties (`:128-133`), which a Native test cannot read. How a tile would reach the simulator: the macOS job fetches it
with the same `tools/fetch-tiles.sh` and pins, then the test binary gets the path. That the simulator's process can open a
host path, and that an environment variable reaches it (simctl forwards `SIMCTL_CHILD_`-prefixed variables), is [I], not
checked: that is the first red test's job.

**(c) The stored digests [C].** `brouter/src/jvmTest/resources/e3/expected-1b.tsv` (2,134 lines; RECORD -88): per case
stratum, id, compare, the six fields and a 64-bit FNV-1a `tupleDigest` of `kind|nodes|cost|distance|nodeDigest|heightsDigest`
(`E3Case.kt:79`), written on the no-intrinsic oracle (JDK 21.0.13+9, amd64). It cannot be read from commonTest on iOS as it
stands: it is a jvmTest resource read by `javaClass.getResource` (`E3Expected.kt:19`), and both reader and file are
jvmTest's. It also holds no ends: the cases' coordinates come from jvmTest generators (`RGenerator`, `LBGenerator`, ...)
drawing on `java.util.Random` and the tile files, so an iOS run needs the case list (ends, profile, memory class) committed
or generated in common code, beside the digests.

**(d) jvmTest only, and why [C].** Every route test: `RouteTest`, `RoutingEngineStopTest`, `RoutingEngineF0Test`,
`RoutingEngineProductionRecordTest`, the e3 strata through `E3CaseRun` (which runs the oracle jar, `E3CaseRun.kt:10`), the
FK and NT generators; 120 jvmTest files in all. The oracle is BRouter 1.7.10's JVM jar and cannot run on Native; the logic
runs also need `OracleLibm`, the oracle's own table and `Math.atan2` (jvmTest's `OracleLibm.kt:9`). An iOS run compares the
port with `MsunLibm` against the stored no-intrinsic digests (E6 under (c)); session 43's JVM run, 1,742 of 1,742 equal on
the same library, is the evidence that the digests are the right target for it [I].

**(e) Questions the record leaves for step 10.**
- Recursion depth and the missing catch (-50, RECORD.md:2162-2166; -61, :2580): Java catches `StackOverflowError` in
  `cleanupPeninsulas`; iOS cannot (`StackOverflowCatch.ios.kt:4`). Measured depth 5,679 frames; step 10 measures the real
  stack on the router's thread, and the owner decides `CollectOutreachersTest` after it ("Decide after step 10", -61).
- Kotlin/Native number formatting (-121, :4535) [?].
- `catch (Error)` on Kotlin/Native, where out-of-memory normally ends the process (-126, :4751).
- FK7-x1's message text (-80, :3204): compared by exception class; its message is the JVM's, "step 10 must read it on
  iOS". (`grep FK7-x1` gives 3 hits; the other two, :3345 and :3360 in -85, record its R1 outcome.)
- Not in the record as a question but in the plan (`:1876`): the 7 GB runner's memory, and run time on the simulator (K).

**(f) Proposed order, in sessions [estimate].**
1. One test that reads one pinned tile on the simulator: fetch in the macOS job, path to the test, open with okio,
   assert its size and SHA-256 against `tile-pins.tsv`. **First red test:** that test, red because no tile is on the
   simulator's path. (1 session.)
2. One route: F0's cases on the simulator against their stored digests (profiles2 must reach it too); measures depth
   on the router's thread for -61. (1 to 2.)
3. The case list and digests into commonTest (a generated Kotlin source or a fetched file; a ruling, since it touches
   how `expected-1b.tsv` is carried). (1 to 2.)
4. The full list or E5's 312 fallback by K's time; FK7-x1's message, number formatting and `catch (Error)` read on the way.
   (2 to 3.) About 5 to 8 sessions in all.
