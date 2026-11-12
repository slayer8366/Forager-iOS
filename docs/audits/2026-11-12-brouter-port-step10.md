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

## §2 Sessions 46 and 47 (Plumb Ledger, 2026-11-12): F0 on six fields; NT, LB and CR on the simulator

On `brouter-port-5b` from `a4b3ce1`, branch `s46` pushed fast-forward. Every iOS result here is **simulator-checked**
(`ios-smoke`, `macos-26`, iosSimulatorArm64). Evidence: `2026-11-12-brouter-port-step10/ios-smoke-run5..7-*.txt` (grep
extracts of each `ios-smoke` log, timestamps cut). Session 46 ended at the handoff call with the NT work uncommitted in the
tree; session 47 checked it, committed it and read CI. Marks as above.

**Session 46: F0 through the engine, six fields (RECORD -138, decision 1).**
1. `938b14f`: `EngineRun` (commonTest) drives `RoutingEngine` as jvmTest's `RoutingEngineF0Test.port` does, on `MsunLibm`,
   and computes the tuple digest in common code (FNV-1a over `kind|nodes|cost|distance|nodeDigest|heightsDigest`, as
   `E3Expected.row`). `F0EngineCommonTest` holds F0's two rows on all six fields and the stored tuple digest;
   `Step10CommonDigestTest` (jvmTest) holds the common digest to the stored one on F0's rows by name and on all 2,126 rows.
   `Found` unchanged; `F0RouteCommonTest` stays at five fields.
2. **The planted error** (the test has no stub to be red over): `4dc33ec` changed `OsmTrack.appendTrack`'s
   `cost += t.cost` to `cost += t.cost + 1`. Prediction, written in that commit before any run: four mismatches, both rows'
   cost and tupleDigest (port 5303 and 7406), every other field equal, `F0RouteCommonTest` passing. JVM, locally: exactly
   those four. Simulator, run 43175838279 (read by the planner, not re-read here): `ios-smoke` 27 tests, 1 failure,
   `F0EngineCommonTest`, the port's cost 7406 against 7405, the other fields equal. `9bac0fc` restored the line from a
   copy saved before the plant, equal to `938b14f`.
3. **CI on `9bac0fc`** (run 43176688650): simulator **27 of 27**; `F0EngineCommonTest` 1 test, 0 failures, printing
   `F0-inside-across-the-degree-line` route|226|5302|5047|9ca48d05a90f4dcd|afc27854ee968af5 tuple e5aadbeff215a44e and
   `F0-inside-long` route|306|7405|6713|7d242f8dd2c428b4|4f6849ea59b5f775 tuple 177bcede53dc1beb, each equal to its
   expected (kind|nodes|cost|distance|nodeDigest|heightsDigest). arm64 7 of 7, "OK: all 2 classes ran". `ios-smoke` 5m27s.
   **F0 on all six fields and the tuple digest: simulator-checked.**

**Session 46 (written), 47 (checked, committed): NT (decision 2), `a557b58`.**
- `NtCasesWriteTest` (jvmTest, gated on `BROUTER_STEP10_NT_CASES`, unset in the `jvm` job): NT's draws as
  `RoutingEngineStep9Test`'s (`NTGenerator`, seed 20261205, `E3Targeted.PROFILE`, within `E3Targeted.CAP`), no oracle run;
  the cases with a stored id are written, one line each (id, profile, memory class, the four ends as the JVM's
  `Double.toString`), to `brouter/build/step10/nt-cases.tsv`.
- `NtEngineCommonTest` (commonTest): reads that file, routes each case through `EngineRun`, and holds it to its row of
  `NtExpected` on six fields and the tuple digest; a case with no stored id is counted, not failed; all 59 ids must appear.
- `NtExpected` (commonTest): NT's 59 rows; `Step10CommonDigestTest` holds it equal to the tsv's NT rows on the JVM.
- `ios-smoke`: one step before the simulator step (the writer on the runner's JVM, `-x msunReference` as the arm64 step)
  and one variable, `SIMCTL_CHILD_BROUTER_STEP10_NT_CASES`; nothing removed. The diff's one deleted line is the comment
  above the variables, extended. No build change, no new dependency.
- Checked before the commit: the E1 scan on the staged diff 0; `NtExpected` has no decimal (`grep -cE '[0-9]+\.[0-9]+'`
  0), and by type a row cannot hold an end (id `String`, kind `String`, nodes/cost/distance `Int`, three hex digests);
  every `println` and message in both tests carries ids, counts, paths, field names or tuple digests only. JVM locally:
  the writer 59 of 59 stored ids within 89 draws; the common test 59 of 59 equal; with the variable unset both print one
  line and do nothing.
- **CI on `a557b58`** (run 43177102690): the arm64 JVM's writer **59 of 59 stored ids within 89 draws** (as on x86_64
  locally: same count, same draw count); simulator **28 of 28**; `NtEngineCommonTest` **59 of 59 stored ids, 0 not
  stored, 59 equal on all six fields and the tuple digest, 0 mismatches, 0 missing**, 111 s. arm64 7 of 7, OK.
  `ios-smoke` 6m48s. `jvm`: 203 tests, 2 failed, both `CollectOutreachersTest` (-61); ` e: ` 0; `compileKotlinIosArm64`
  and `compileKotlinIosSimulatorArm64` BUILD SUCCESSFUL. `jvm-production`: 19 groups, R 1,180 of 1,180, success.
  **NT: simulator-checked.**

**Session 47: LB and CR, `314b77c`.**
- `LbExpected`, `CrExpected`: 59 rows each (all `exact`), generated from the tsv; held to it by `Step10CommonDigestTest`.
- `NtCasesWriteTest` extended: LB (`LBGenerator`, seed 20261207, `E3Targeted.PROFILE`) and CR (`RGenerator`, seed
  20261206, `trekking-crossing`, memory class 64), as `RoutingEngineStep9Test`, written to `lb-cases.tsv` and
  `cr-cases.tsv` beside the NT file. CR's profile is not among the oracle's, so `E3Profiles.crossing()` (the pinned
  `trekking.brf` with E4's assigns, no position) is written with the oracle's `lookups.dat` into `step10/profiles2/`.
- `NtEngineCommonTest`'s check moved into `Step10Cases`, unchanged but for the group's name, its rows and the profiles
  folder; `LbEngineCommonTest` and `CrEngineCommonTest` use it. The files lie beside the NT file, so **no CI change**.
- JVM locally: writer NT 59/59 (89 draws), LB 59/59 (99), CR 59/59 (83); each common test 59 of 59 equal.
- **CI on `314b77c`** (run 43178576702): writer on the arm64 JVM NT 59 of 59 (89 draws), **LB 59 of 59 (99), CR 59 of
  59 (83)**; simulator **30 of 30**; `LbEngineCommonTest` **59 of 59 equal** (34 s), `CrEngineCommonTest` **59 of 59
  equal** (115 s), NT 59 of 59 (123 s); 0 not stored, 0 mismatches, 0 missing in each. arm64 7 of 7, OK. `ios-smoke`
  9m59s. `jvm`: 207 tests, 2 failed, both `CollectOutreachersTest` (-61); ` e: ` 0; both `compileKotlinIos*` BUILD
  SUCCESSFUL. `jvm-production`: 19 groups, R 1,180 of 1,180, success. **LB and CR: simulator-checked.**

**How E1 was kept.** Committed: only rows of ids, Ints and digests (scan 0 on each staged diff). Case files: under
`brouter/build/step10/`, outside `test-results/`, the only folder `ios-smoke` uploads or prints; `build/` is git-ignored.
Printed: ids, counts and paths. **The check on the log**, per run, on the `ios-smoke` job log fetched with
`gh api repos/slayer8366/Forager-iOS/actions/jobs/<job>/logs`, its leading BOM and timestamps cut
(`sed -E 's/^\xEF\xBB\xBF//; s/^[0-9T:.-]+Z //'`, since every raw timestamp matches the pattern):
`sed -n '<write step>p' log | grep -cE '[0-9]{1,3}\.[0-9]{5,}'` and `grep 'step10 ' log | grep -cE '[0-9]{1,3}\.[0-9]{5,}'`.
On `a557b58`: write step (lines 351-427) 0, the 9 `step10` lines 0. On `314b77c`: write step (lines 351-430) 0, the 13 `step10` lines 0.
The whole cut log: one hit, `FloatTextCommonTest`'s literal list (an existing test's constants, not a position).

**[I]/[?].** [I] That the arm64 JVM draws what x86_64 drew is shown for these three generators by the counts and by every
case matching its stored row, not in general. [I] No planted error was run for LB or CR; their check is NT's code, which
the F0 plant showed red on the simulator only through F0's own test. [?] Whether the planner's read of run 43175838279
also showed F0-inside-across-the-degree-line's 5303 (predicted); the summary handed to me names only 7406. [?] Simulator
time: the three groups take 4m32s of `ios-smoke`'s 9m59s; the memory groups and R would add more.

**Questions for the planner.** 1. A plant for LB/CR's path (a wrong ownProfiles, or one row's cost off by one in the
case file at run time), or is NT's shared code with F0's plant enough? 2. Keep growing one writer step (it now runs 3
generators, 80 s), or split per group when the memory groups come?

**Next.** (1) The memory groups the same way (their memory class is in each line already); (2) R; (3) -61's depth, on its
own (decision 3).

**Stops hit.** None. No position in any log read, XML or artifact by the check above; no simulator route differing from
its row; the arm64 JVM drew the same cases; no CI change beyond NT's step and variable.
