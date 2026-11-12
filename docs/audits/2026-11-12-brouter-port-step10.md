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

## §1 Session 45 (Plumb Ledger, 2026-11-12): one tile, then F0, on the simulator

On `brouter-port-5b` from `7f97edd`. Every iOS result here is **simulator-checked** (`ios-smoke`, `macos-26`,
iosSimulatorArm64). Evidence: `2026-11-12-brouter-port-step10/` (grep extracts of each `ios-smoke` log). Marks as above.

**Done.**
1. `PinnedTileReadTest` (commonTest, so one source runs on both platforms): opens `f0/W75_N40.rd5` (F0's fixture, the
   smallest pinned file and the one F0 reads) with okio's `FileSystem.SYSTEM`, holds size, bytes read and SHA-256 (okio's
   `HashingSink`, no new dependency) to the pin row copied in. The path comes from `expect fun testSetting` in commonTest:
   jvmTest's actual reads the system property `oracleJvm()` already sets (`brouter.tiles.dir`), so on the JVM the test
   runs for real, and also holds the copied row to `tile-pins.tsv` (`brouter.tiles.pins`); iosTest's actual reads
   `platform.posix.getenv("BROUTER_TILES_DIR")`. Why iosTest and not a skip: the JVM already has the tiles. Bites on the
   JVM: a size off by one fails "size against tile-pins.tsv expected:<152666> but was:<152665>" (reverted from a copy, no
   compile error, forward change confirmed present).
2. `ios-smoke`: one new step before the simulator's tests, `./gradlew --no-daemon :brouter:fetchTiles :brouter:fetchOracle`
   (the build's own tasks, so the release URL and pins are not copied into CI), and on the simulator step
   `SIMCTL_CHILD_BROUTER_TILES_DIR` and `SIMCTL_CHILD_BROUTER_ORACLE_DIR` (workspace `brouter/build/tiles`, `.../oracle`).
   No step removed or reordered; the arm64 steps untouched. That Gradle hands its environment to `xcrun simctl spawn` is
   [C] in KGP 2.3.10's bytecode (`KotlinNativeTest` puts `SystemGetEnvSource.getAllEnvironmentVariables` into
   `processOptions.environment`); that simctl strips the prefix is now shown by run 2.
3. `F0RouteCommonTest` (commonTest): F0's two cases (Forager's own ends, `RoutingEngineF0Test.CASES`) through the public
   `route()`, so `MsunLibm`, `hiking-mountain`, memory class 64 (RoutingContext's default), no limit, Forager's params;
   F0's two expected-1b rows copied in. Bites on the JVM: a distance off by one fails "F0-inside-long distance: port 6713,
   expected 6714" (reverted from a copy).

**CI, read per push** (simulator TOTAL; arm64 TOTAL; jvm TOTAL; `ios-smoke` minutes):

| run | commit | simulator | new tests on the simulator | arm64 | jvm | ios-smoke |
|---|---|---|---|---|---|---|
| 43168088740 | `bee36ec` red | 25, 1 failure | Pinned red: "no tiles folder: environment variable BROUTER_TILES_DIR ... is not set" | 7, 0; OK: all 2 classes ran | 196, 2 failures (CollectOutreachersTest's two, -61) | 5m22s |
| 43169639835 | `eeefbf9` | 25, 0 | Pinned passed, size=152665 read=152665 sha256=ff6d9d8e...5990 | 7, 0; OK | 196, 2 failures (the same two) | 4m27s |
| 43170250016 | `72744e6` red | 26, 1 failure | F0 red: "environment variable BROUTER_ORACLE_DIR ... is not set"; Pinned passed | 7, 0; OK | 197, 2 failures (the same two) | 5m49s |
| 43171038917 | `71d7281` | 26, 0 | F0 passed, both cases; Pinned passed | 7, 0; OK | 197, 2 failures (the same two) | 4m45s |

` e: ` 0 in every log read; `compileKotlinIosArm64` and `compileKotlinIosSimulatorArm64` ran in each jvm job read, BUILD
SUCCESSFUL. `jvm-production` succeeded in each run read. No crash of the simulator's test process.

**What the simulator can open (shown).** A host path, directly: the test process read
`/Users/runner/work/Forager-iOS/Forager-iOS/brouter/build/tiles/f0/W75_N40.rd5`, all 152,665 bytes, SHA-256 equal to its
pin; and `.../build/oracle/profiles2/hiking-mountain.brf` with `lookups.dat` (F0 routed). Environment variables reach it
through the `SIMCTL_CHILD_` prefix. Not tried, since not needed: the simulator's data container, the test bundle's
resources.

**F0 on the simulator (simulator-checked).** Both cases equal F0's stored values in each field `route()` returns:
`F0-inside-across-the-degree-line` route|226|5047|9ca48d05a90f4dcd|afc27854ee968af5, `F0-inside-long`
route|306|6713|7d242f8dd2c428b4|4f6849ea59b5f775 (kind|nodes|distance|nodeDigest|heightsDigest). **Five of six fields:**
cost is not in `RouteOutcome.Found`, so the public `route()` cannot give it, nor the tupleDigest (which covers cost). The
prompt's "six fields through route()" does not hold as stated [C, `Route.kt:69`]; I held the five and asserted nothing
about cost rather than route around the public call.

**-61's recursion depth.** No test-only counter exists in `OsmNodesMap.cleanupPeninsulas` (`grep` for depth/counter in
`mapaccess`: none); none added. Not measured. That F0's runs did not overflow the router's thread on the simulator is
shown only for these two routes.

**[I]/[?].** [I] F0 is a small fixture; a large tile (W75_N40, 119.8 MB) through the same path is not yet read on the
simulator. [?] cost on iOS. [?] the stack depth on the simulator's thread.

**For the owner's question (-137, decision 5)**, by what is now known:
- *Generated on the macOS job's JVM at run time:* evidence for: that job already runs a JVM with every tile fetched (the
  arm64 step's `jvmTest`, OK all 2 classes, each run), and the simulator reads host files the job writes (shown). Not
  shown: the generators running on arm64 HotSpot draw the same ends as on x86_64 (they use `java.util.Random` and tiles;
  not run there), nor the ordering cost (the arm64 step runs after the simulator's today).
- *A CI artifact from the Linux job:* evidence for: `upload-artifact` is already in both jobs, and a file under the
  workspace reaches the simulator (shown). Not shown: `ios-smoke` waiting on `jvm` (a `needs:`, which serialises about
  20-30 minutes ahead of it), and whether an artifact counts as "committed" under E1.
- *Ported generators (common code):* evidence for: commonTest reads tiles on the simulator (shown). Not shown: that a
  common port of `java.util.Random`'s sequence and the generators reproduce the JVM's ends; nothing of it exists.

**Questions for the planner.**
1. Cost: widen what F0 is held to on iOS by a test-side engine run (commonTest can reach `RoutingEngine`, as
   `RoutingEngineF0Test.port` does), or add cost to `RouteOutcome.Found`, or accept five fields through `route()`?
2. A depth counter for -61 is a change to `OsmNodesMap`; is it wanted, test-only?

**Next.** (1) The planner's call on cost; (2) a read of a large pinned tile (`segments/W75_N40.rd5`, 119.8 MB) on the
simulator, for memory and time; (3) the owner's ruling on decision 5, then the case list.

**Stops hit.** None of the listed stops. The false premise (six fields through `route()`) is reported above.
