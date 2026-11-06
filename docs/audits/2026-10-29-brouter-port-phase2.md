# BRouter port, Phase 2: steps 0 and 1 (dispatch 2026-10-28-01, Amendment 2)

Written by the team's primary coder (first run, not yet named), 2026-10-29, for Cedar Datum, the planner. The amendment is
`prompts/preserved/2026-10-28-01-brouter-port-amendment-2.md` on `main` at `c5c9411`. Phase 1's report,
`docs/audits/2026-10-28-brouter-port-verify.md` at `20e1f92`, is the plan; "item I" below is its Phase 2 order.

**Status (2026-10-29, second session):** steps 0 and 1 done. The tiles are checked and pinned, and the release routes are
below. **No release exists:** the release waits for the owner's word, and steps 1b onward wait for the go.

**Status (2026-10-29, third session, Linden Caliper, Amendment 3 Part A):** **stopped at Part A's step 2.** The proxy refused
the draft release with a 403. **No release, draft or tag exists.** Steps 3 to 7 were not run, and the release waits for the
planner (section "Amendment 3, Part A" below).

**Status (2026-10-29, fourth session, Linden Caliper, Amendment 4):** the workflow is written and dry-run on `brouter-port`:
green twice, and refused once by a one-character change (section "Amendment 4" below). It waits for the planner's copy to
`main` and the owner's "Run workflow". Nothing was published, tagged or uploaded.

**Status (2026-10-29, fifth session, Linden Caliper, Amendment 3 Part B, step 2a):** **partial, handed off on the planner's
word** after sub-step 1. The stubs and tests are pushed (`3cd61ad`) and seen failing for their reason; the port (sub-step 2) and
the revert check (sub-step 3) are not started. See "Step 2a" below, and its handoff.

**Status (2026-10-29, sixth session, Linden Caliper, Amendment 3 Part B, step 2a):** **partial, stopped on a wrong
premise.** The port is written and pushed (`612bb0d`). 24 of the 25 tests pass locally, and the revert check held its
prediction. The 25th,
`BitCoderContextTest.rawBitReadsOnRandomBytesMatchTheOracle`, cannot finish under any port: its loop does not end on the
oracle alone. Changing it is the planner's call (section "Step 2a, sub-step 2" below).

**Status (2026-10-29, seventh session, Linden Caliper, Amendment 3 Part B, step 2a close):** the planner's ruling on the
25th test (RECORD -33) is applied (`d5371c7`): case 1 capped at 1,000,000 ops per seed, a second case with no reset to the
position bound. Locally, with no filter: 26 tests, 0 failures. Each case bites on its own one-flipped-bit edit (`9ce54ee`).
**One premise in the ruling is wrong:** the second case never reaches `fillBuffer`'s end-of-array branch (section "Step 2a
close" below). CI run 40970686102 on `9ce54ee` succeeded: `jvm` 26 tests, 0 failures, with the iOS klibs compiled;
`ios-smoke` 4 tests, 0 failures (simulator-checked).

**Status (2026-10-29, eighth session, Linden Caliper, Amendment 3 Part B, step 2b, first part):** bionic's msun `cos` and
`atan2` ported to common Kotlin (`6c3de2d`), proven bit for bit against bionic's C built in CI from pinned sources. CI run
40973074241 on `6c3de2d` succeeded: `jvm` 37 tests, 0 failures, klibs compiled with 0 `e:` lines; `ios-smoke` 10 tests,
0 failures (simulator-checked). The revert check failed the three predicted tests at the predicted rows (`e7dafe5`).
`CheapRuler`, `CheapAngleMeter`, the seam and the simulator job are the next session's.

**Status (2026-10-29, ninth session, Linden Caliper, Amendment 3 Part B, step 2b, second part):** `CheapRuler`,
`CheapAngleMeter` and the seam are ported and pushed (`35a1fb6` tests first, `073851b` the port, `c4e2eac` the revert).
Locally, jvmTest gives 54 tests and 0 failures. CI run 40975094092 on `073851b` succeeded: `jvm` 54 tests, 0 failures, klibs compiled; `ios-smoke` 18 tests, 0 failures (simulator-checked). The arm64 HotSpot step did not run its tests (below). Wrapped up on the planner's word, at about
180k tokens.

**Status (2026-10-30, tenth and eleventh sessions, Linden Caliper, Amendment 3 Part B, step 3):** `codec`'s decoders are
ported and green (CI `jvm` 62 tests, 0 failures, on `4436711`), and all four reverts failed as predicted. The arm64 JVM
step runs under `-XX:DisableIntrinsic=_dcos` and read 7 of 7 on `7bf6a3d` (section "Step 3" below).

**Status (2026-10-30, twelfth session, Linden Caliper, Amendment 3 Part B, step 4):** **partial, handed off on the planner's word**
(context past 180k). The report's step 3 section is pushed (`c90f440`). For step 4, only drafts are committed, in
`2026-10-29-brouter-port-phase2/step4/`: the mechanism prediction (`0-prediction.txt`), and a draft port of
`BExpressionContext` (three files: 379, 113 and 166 lines) and of `BExpression` (365 lines) in `port-draft/`
[C: `wc -l`, thirteenth session; the twelfth session's figures, 346, 117, 175 and 338, were wrong (RECORD -40)]. That folder is
outside `commonMain` on purpose, so the next session can still see the tests fail on stubs before the port lands.
**Nothing compiles or has run:** `brouter/` is unchanged from `599dd40`, which does not compile. The drafts have never
been compiled. Not written: the stubs, `BExpressionMetaData`'s port, `BExpressionContextWay`, `BExpressionContextNode`,
`util`'s `JavaStringTokenizer`, `javaTrim` and `javaReadLines` (which the drafts import), and every step 4 test.
**Finding [R: grep of `addLookupValue` callers in Forager's vendored `btools/`]:** every caller passes a null array,
so the asterisk block (`BExpressionContext.java:576-693`) cannot run from Forager's calls whatever the lookups file.
Its test has to call `addLookupValue(name, value, int[])` directly.

**Status (2026-10-30, thirteenth session, Linden Caliper, Amendment 3 Part B, step 4):** **partial, handed off on the
planner's word** (context past 180k). Pushed: the draft sizes corrected (`090d2e5`); `util/JavaText.kt`, tests first, 6 of
7 seen failing on stubs, then 7 of 7 locally (`c350bda`); stubs for every class step 4 ports, the module compiling, jvmTest
69 of 69 locally (`0c17b17`); CI's iOS klibs step running after a red JVM step (`b70987e`); step 4's six tests, all seen
failing on the stubs, 75 tests, 6 failures, 0 compile errors, read from the JUnit XML (`step4/5-tests-stubs-junit.txt`).
**CI on these commits was not read.** Next: the port of the drafts into `commonMain` (section "Step 4: the test list").

**Status (2026-10-30, fifteenth session, Linden Caliper, Amendment 3 Part B, step 1 pinning and step 1b's start):** the
release `brouter-tiles-2026-10-30` is pinned on `brouter-port` (`adc8065`), its revert seen failing as predicted
(`490d0ae`), and CI read green on both jobs. Step 1b's JDK check is a test (`fa89911`, revert `8305898`), and E3's
positive control runs R1 and R4 in fresh JVMs (`4be79f0`, revert `d32f2c3`). **Step 1b is partial:** no case generator,
no stratum and no R2, R3, R5 or R6 yet. See "Step 1 pinning and step 1b's start" below, and its handoff.

Tags: **[R]** read (file and line at a stated commit, or URL and date); **[C]** computed, with its log in
`2026-10-29-brouter-port-phase2/`; **[I]** inferred; **[?]** could not determine. iOS results are "simulator-checked".

## Bases [C: `2026-10-29-brouter-port-phase2/00-bases.txt`]

- **Forager-iOS `brouter-port`** is `20e1f92`, as the amendment states.
- **Forager-iOS `main`** is `c5c9411`, not the amendment's `3fc5cef`. The move adds RECORD -25 and the amendment itself
  (`git diff --stat 3fc5cef c5c9411`: `RECORD.md` and the amendment file, 179 insertions). It changes no premise.
- **Forager `pre-main`** is `d100a4776` (`git ls-remote`, 2026-10-29T19:59Z), as stated. The read-only clone is `e2969abe4`.
- **Actions on Forager-iOS:** no workflow exists yet (`list_workflows`: 0). Whether Actions is enabled cannot be read through
  this session's proxy (`actions/permissions`: HTTP 403 "not permitted through this proxy") **[?]**. The first push of a
  workflow answers it: a run appears or it does not.

## The planner's change to item I's step 0 row (ruling, 2026-10-29)

Item I's step 0 tests were "the oracle jar's SHA-256; one case per tile-free failure kind on both engines (FK 6, data
missing: a folder without the tile), failing on the stub" (report line 1691 at `20e1f92`). Step 0 ports no code, so the
port half of FK 6 could not pass before step 5a, while the amendment's finish line needs the `ubuntu-24.04` job green. I
asked rather than chose. The planner ruled option (c), verbatim in substance:
- step 0 carries the oracle pin test and **the oracle half of FK 6**;
- the port half of FK 6 is written at step 5a, over stubs, seen failing, then made green there;
- rejected: keeping it red for five steps (a red-by-design job cannot tell a real failure from the planned one), and moving
  a written test out of the default run (silencing, which Forager's `CLAUDE.md` rules out);
- **the rule from here on:** each step's port-side tests arrive with the step that ports them, so the default run means
  "everything ported so far passes";
- the oracle half of FK 6 is listed "not shown to bite" unless a revert of its own bites; step 0's named revert stays the
  oracle pin's SHA-256 changed by one character, which the fetch must refuse.

## Mechanism prediction (written before building)

**Step 0.**
- **The build:** Gradle 9.0.0 by the wrapper, `distributionSha256Sum` from Gradle's published checksum; one KMP module
  `:brouter` with Kotlin 2.3.10 exactly, targets `jvm` (JVM target 17), `iosArm64`, `iosSimulatorArm64`; okio 3.18.2.
  No atomicfu yet: B row 8's lock is step 6's.
- **The oracle reaches the JVM tests** through one Gradle task that calls `tools/fetch-pinned.sh` for the release zip (size
  and SHA-256 as Forager's `fetch-brouter.sh:23-28`), extracts `brouter-1.7.10-ro.jar` and `profiles2/`, and checks the jar's
  own SHA-256 with the same script. `jvmTest` alone depends on it, as a file dependency. A wrong pin fails the task, so the
  tests never run on an unpinned jar.
- **The tests:** (1) the oracle pin, asserting that `btools.router.RoutingEngine` is loaded from a file whose SHA-256 is the
  pin held in the test source (a second copy, so a build-file pin edit is caught); (2) the oracle half of FK 6, a folder
  without the tile, asserting the engine's own message; (3) the smoke test in `commonTest`, which on `macos-26` answers only
  whether Kotlin 2.3.10 links and runs a simulator test. Its assertion is incidental; what it proves is read from the JUnit
  XML's count for `iosSimulatorArm64`.
- **The entry point** is C's sketch as stubs, each body `TODO()`. No test calls it in step 0 (the ruling above).
- **CI:** `jvm` on `ubuntu-24.04` runs `jvmTest` and both iOS klib compiles; `ios-smoke` on `macos-26` selects
  `Xcode_26.0.1` and runs `iosSimulatorArm64Test`. Each job prints its JUnit XML into the log, so counts are read from the run.

**Step 1.**
- The four tiles are downloaded once each with `curl`, headers kept; each is loaded in the oracle by a scratch harness that
  runs `PhysicalFile.checkFileIntegrity` and one route between two of the tile's own nodes.
- The pin file records each tile's size, SHA-256, source URL, `Last-Modified` and `ETag`. Its revert check runs
  `tools/fetch-pinned.sh` on the captured copies with one size off by one, which must refuse.

**Predicted outcomes** are the planner's (amendment, "Predictions"); scored below.

## Step 0: results

**Status: done.** Commit `fea07f8`; CI run 40946531992, both jobs green [R: `2026-10-29-brouter-port-phase2/step0/ci-run-40946531992.txt`].

**What was built.**
- Gradle 9.0.0 wrapper. `distributionSha256Sum` `8fad3d78…6072b` is Gradle's published checksum, read from
  `services.gradle.org` at 19:57Z; the committed `gradle-wrapper.jar` hashes to `76805e32…493f3`, Gradle's published 9.0.0
  wrapper-jar checksum [C: `step0/gradle-9.0.0-checksums.log`].
- `gradle/libs.versions.toml`: Kotlin 2.3.10, okio 3.18.2, exact. No atomicfu (B row 8's lock is step 6's).
- `:brouter`, targets `jvm` (target 17), `iosArm64`, `iosSimulatorArm64`; `brouter/LICENSE` is upstream's v1.7.10 file
  unchanged (SHA-256 `73408906…3f05e`, equal to `raw.githubusercontent.com/abrensch/brouter/v1.7.10/LICENSE`, read 20:01Z)
  [C]; `brouter/README.md`.
- C's entry point as stubs (`Route.kt`): `RouteRequest`, `RouteCancel`, `RouteOutcome`, `route`, `checkDataFile`.
- `tools/fetch-pinned.sh` (size then SHA-256, as Forager's `fetch-brouter.sh:23-28`; portable to macOS), `tools/fetch-oracle.sh`
  (D's pins: zip 6,724,983 bytes `023fec3b…ad01532`, `-ro` jar 345,776 bytes `598b647f…dbd972`), and the Gradle task
  `fetchOracle` that runs it before `jvmTest`. `tools/junit-counts.py` prints counts from the XML and exits 1 on no XML.
- `.github/workflows/brouter.yml`: `jvm` on `ubuntu-24.04`, `ios-smoke` on `macos-26` with `Xcode_26.0.1` selected. Actions
  pinned by SHA (checkout v7.0.1, setup-java v6.1.0, upload-artifact v7.0.2, read by `git ls-remote` 20:00Z).

**CI, run 40946531992 on `fea07f8` (the first push of a workflow) [R: the run's logs].**

| Job | Runner | Result | Counts (JUnit XML, printed in the log) |
|---|---|---|---|
| `jvm` | `ubuntu-24.04`, Temurin 21.0.13+9 amd64 | success | 3 tests, 0 failures, 0 errors, 0 skipped; both iOS klibs compiled |
| `ios-smoke` | `macos-26-arm64` image 20261021.0445.8, Xcode 26.0.1 (17A400) | success | 1 test (`okioSplitsATilePathOnThisTarget[iosSimulatorArm64]`), 0 failures; `linkDebugTestIosSimulatorArm64` ran |

The `jvm` counts match the three tests the step defines (pin, FK 6 oracle half, smoke) [C]. Local runs agree: 3/0/0/0
[C: `step0/step0-jvmTest-run2.log`]. The iOS result is **simulator-checked**.

**The tests, and what each is evidence of.**
- **Oracle pin** (`OraclePinTest`): `RoutingEngine` loads from `brouter-1.7.10-ro.jar`, 345,776 bytes, SHA-256 the pin held
  in the test, manifest 1.7.10. **Shown to bite** by its own one-character edit, below.
- **FK 6, oracle half** (`OracleFailureKindsTest`): a folder without the tile gives "datafile W75_N40.rd5 not found".
  **Not shown to bite** by a revert (the planner's ruling). It was seen failing once, on my wrong prediction (below), which
  shows the assertion reads the engine's real message; that is not a revert check.
- **Smoke** (`ToolchainSmokeTest`, `commonTest`): **not shown to bite**, by design. Its evidence is the count: it ran on
  `iosSimulatorArm64`.
- No test over a stub, per the ruling: step 0 has no port-side test.

**My prediction that failed [C: `01-step0-predictions.txt`, `step0/step0-jvmTest-run1.log`].** I predicted FK 6's message
as "from-position not mapped in existing datafile" (`RoutingEngine.java:1629`). The oracle gave "datafile W75_N40.rd5 not
found": `NodesCache.preloadPosition` throws it when the first tile read fails (`NodesCache.java:326-332` at Forager
`e2969abe4`), before matching can report "not mapped". My reading stopped one call too high. The test asserted my value, not
the oracle's, so the test was wrong; it now asserts the oracle's message with that line cited.

**Revert check (item I's named edit), run in a scratch clone on its own branch [C: `step0/revert0-*.log`].**
- Forward first, in the clone at `fea07f8`: green, 3/0/0/0, XML stamped 20:10:43.
- Saved `tools/fetch-oracle.sh`; changed `zip_sha256`'s first character, `0` to `1`; `git diff --stat`: 1 file, 1 line.
- Build log read first: 0 `e:` lines. `fetchOracle` FAILED with "REFUSED brouter-1.7.10.zip ... has SHA-256 023fec3b…,
  pinned 123fec3b…", after "present but refused ...; fetching again", so the refusal held for a cached copy and a fresh
  download alike. `jvmTest` never ran; the XML left in the folder (20:10:43) is the forward run's and is not cited as the
  revert's.
- Restored from the saved copy (`cmp` identical; clone clean). The forward change is present in `/home/user/forager-ios`
  (`fea07f8`, clean tree, `zip_sha256=023fec3b…`).

**A second bite check, beyond the named one:** the test's own `PINNED_JAR_SHA256` changed `5` to `6`: 0 `e:` lines; the
XML (stamped 20:12:05, new) shows 3 tests, 1 failure, `OraclePinTest` with "expected:<[6]98b647f…> but was:<[5]98b647f…>";
restored from the saved copy, `cmp` identical [C: `step0/revert0b-reverted.log`].

**Predictions (planner), scored.**
- "Actions is on": **held.** A run appeared for the first workflow push.
- "the `ubuntu-24.04` job is green within two pushes of the scaffold": **held,** on the first.
- "Kotlin 2.3.10 links and runs the smoke test on `macos-26`": **held** (simulator-checked).
- "printing a warning about the Xcode version": **did not hold.** No Xcode-version warning, or any warning, appears between
  "Welcome to Gradle 9.0.0!" and "BUILD SUCCESSFUL" in the whole job log. Phase 1's G inferred the warning from the bytecode
  of `kotlin-native-utils` and KGP; why it did not print was not investigated [?].
- "BRouter 1.7.10 reads all four tiles": **not scored;** step 1 was handed off before any tile was loaded.

**Found on the runner, for step 2b [R: the smoke job's log].** Xcode 26.0.1 is installed on `macos-26`, but no iOS 26.0
simulator runtime is: the runtimes are iOS 26.2, 26.4.1 and 26.5. The test still linked and ran; on which device and runtime
the log does not say [?].

## Step 1: the handoff (first session)

The planner's message at 20:12Z (context past 180k tokens) ended my run after step 0. Before it arrived, step 1's first
action had run; nothing after it did.
- **Done:** the four tiles were downloaded once each from `brouter.de/brouter/segments4/` at 20:09:48Z to 20:09:58Z into
  `<scratch>/phase2/tiles/`, with their response headers. All four answered `200 OK`; each file's size equals its
  `Content-Length`; all four carry `Last-Modified: Thu, 29 Oct 2026 01:03:01 GMT`
  [C: `step1-handoff/tiles-downloaded.txt`, `step1-handoff/*.headers.txt`].
- **Not done:** no tile has been loaded in the oracle; there is no pin file; F0's fixture is not copied; no ODbL notice is
  written; the release routes are not reported.
- **A harness is drafted, not compiled** (`step1-handoff/TileCheck.java.txt`): `javac` refuses `pf.ra.close()` at two lines,
  because `PhysicalFile.ra` is package-private. It prints each tile's lookup version against `lookups.dat` (11.2), opens it
  as the engine does, runs `checkFileIntegrity`, counts the micro-caches and nodes it decodes, and routes once.

## Step 1: results (second session)

**Done up to the release, which does not exist.** Commits `4da988d` (oracle load), `1df847c` (pin file, reader, F0),
`91754bc` (ODbL notices), and the one carrying this section. Logs are in `2026-10-29-brouter-port-phase2/step1/`.

- **The bytes are the handoff's.** The four scratch tiles, re-hashed at 20:17:42Z before anything else, equal
  `step1-handoff/tiles-downloaded.txt` in size and SHA-256 [C: `step1/tiles-recheck.log`]. Nothing was downloaded again.
- **The oracle.** The release zip and `brouter-1.7.10-ro.jar` were re-checked against their pins by `tools/fetch-oracle.sh`
  [C: `step1/oracle-recheck.log`]. The JDK was the container's OpenJDK 21.0.12.1 (Phase 1's), run with
  `-XX:+UnlockDiagnosticVMOptions -XX:-UseLibmIntrinsic`, not CI's Temurin.
- **The harness** (`step1/TileCheck.java.txt`, run by `step1/run-check.sh.txt`) is the handoff's draft with two fixes:
  - `PhysicalFile.ra` is closed by reflection.
  - **Its count now decodes.** The draft copied `checkFileIntegrity`'s call, which passes `MicroCache.debug` (false) as
    `reallyDecode`. That checks each cell's CRC without decoding and returns `null` for every non-empty cell
    (`OsmFile.java:133-148`, `PhysicalFile.java:79` at v1.7.10) [R]. So the draft read `non-empty=0, nodes=0` on every tile.
    That includes F0's fixture, which routes, and that is what exposed it. The superseded runs are kept, with a note
    [C: `step1/superseded/`].
- **Each tile was run alone,** in a folder holding only that tile, so a route can only have read it.

| Tile | Bytes | SHA-256 | Lookup version | Version-checked open | `checkFileIntegrity` | Cells with data / read; nodes decoded | Route (hiking-mountain) | Read |
|---|---:|---|---|---|---|---|---|---|
| `W75_N40` | 119,750,873 | `dd141c3e…d29e6` | 11 (`lookups.dat` 11.2) | ok | ok | 19,671 / 23,552; 8,136,499 | 41.27426,−73.99789 → 41.30369,−74.01595: 226 nodes, 5,047 m | **yes** |
| `W75_N35` | 3,571,019 | `73d58fa5…6a721` | 11 | ok | ok | 683 / 2,048; 260,055 | 39.36,−74.44 → 39.38,−74.41: 322 nodes, 9,471 m | **yes** |
| `W70_N40` | 5,292,557 | `c35113bb…e0521c` | 11 | ok | ok | 3,904 / 10,240; 233,163 | 44.8012,−68.7778 → 44.82,−68.75: 104 nodes, 3,949 m | **yes** |
| `E170_S50` | 1,061,589 | `878455e2…acd584` | 11 | ok | ok | 767 / 3,072; 50,074 | −45.8788,170.5028 → −45.865,170.515: 115 nodes, 2,003 m | **yes** |
| F0, Forager's fixture (control) | 152,665 | `ff6d9d8e…5ec5990` | 11 | ok | ok | 47 / 2,048; 7,191 | the same ends as `W75_N40`: 226 nodes, 5,047 m | yes |

[C: `step1/check-*.log`; full hashes in `tools/tile-pins.tsv`.] Every route's error is `null`. All four tiles carry
`Last-Modified: Thu, 29 Oct 2026 01:03:01 GMT` [C: the handoff's headers].
- **The routes' ends.** `W75_N40`'s are Forager's `inside-across-the-degree-line` ends (`make-expected.sh`). The others are
  town points (Atlantic City, Bangor, Dunedin), chosen from geography, not from the tiles' nodes [I]. Each found a route.
- **The F0 control agrees with Forager's own record.** The harness's route on F0 equals Forager's committed command-line
  result, `brouter-cli/inside-across-the-degree-line.geojson`: `track-length` 5047 and 226 points [C]. So the harness routes
  as the oracle's own command line does on these ends.
- **The bite check.** Four damaged copies of `E170_S50` were each refused for their own reason [C: `step1/bite-harness.log`]:
  - lookup version 11 → 10: "lookup version mismatch (old rd5?) lookups.dat=11 E170_S50.rd5=10";
  - one top-index byte flipped: "top index checksum error";
  - cut to half: "file of size 530794 too short";
  - one mid-file byte flipped: **the open passes and `checkFileIntegrity` alone refuses it** ("checkum error", upstream's
    spelling). So the integrity check bites on cell data that the open never reads.
- **Prediction (-25) "BRouter 1.7.10 reads all four tiles, so none is replaced": held** [C]. E4's replacement rule was not
  needed.
- **Not read in step 1:** whether any tile carries `estimated_crossing_class` (E4's [?], "read from the coverage run at
  capture"). That run is E3's, and I read it as step 1b's. The tiles' internal `creationTime` was not read either.

**The pin file, `tools/tile-pins.tsv`.** One tab-separated row per release asset: asset name, the path the fetch writes, bytes,
SHA-256, source, `Last-Modified`, `ETag`, and capture time. It was generated from the saved headers and fresh hashes, not typed.
- **F0's fixture is pinned as `forager-fixture-W75_N40.rd5`,** fetched to `f0/W75_N40.rd5`. A release cannot hold two
  assets named `W75_N40.rd5` [I: GitHub's asset names are unique within a release, from memory], and the engine finds a tile by its file name, so the asset is renamed and the folder keeps
  the name.
- **`tools/fetch-tiles.sh <dir> <base url>`** reads the pin file and fetches each asset through `tools/fetch-pinned.sh`. It is
  not wired into CI or Gradle: there is no release URL yet.
  - Forward, against the scratch copies over `file://`: 5 of 5 match [C: `step1/fetch-forward.log`].
  - **Revert (item I's step 1 row):** `E170_S50`'s pinned size changed from 1,061,589 to 1,061,590. The fetch refused it with
    "E170_S50.rd5 is 1061589 bytes, pinned 1061590", exit 1. The edit was to data, so there is no compile log; the reader's
    `bash -n` passed. The pin file was restored from a copy saved before the edit, its SHA-256 equals the forward file's, and
    the forward row is present [C: `step1/revert1.log`].

**F0's fixture** was copied read-only from Forager's clone (`e2969abe4`,
`app/src/test/resources/routing/segments/W75_N40.rd5`, blob `434697225fed`) into scratch: 152,665 bytes, SHA-256
`ff6d9d8e…5ec5990`, equal to its pin. Forager's `git status` was empty afterwards [C: `step1/f0-copy.log`]. It is not
committed here (ruling c).

**The ODbL 4.2 notices,** `tools/tile-release/`:
- `README.md` is the release's README asset. It holds OpenStreetMap's attribution, the ODbL 1.0 URI, each asset with its
  size, SHA-256 and source, and F0's notice from Forager's README, kept intact as 4.2(c) asks. The quote was checked word
  for word against the source [C].
- `release-notes.md` is the release body.
- Both rely on 4.2(d), since an `.rd5` file has no place for a notice. Sections beyond 4.2 were not examined, as in Phase 1.

**CI on step 1's commits** [R: the runs, through the GitHub API]:
- runs 40948343921 (`4da988d`), 40948581610 (`1df847c`) and 40948692469 (`91754bc`): each `success`, `jvm` and `ios-smoke`;
- run 40949189161 (`4fd1ed9`): in progress when this was written.

**These runs are not evidence for step 1.** Neither job reads a tile, the pin file or the notices; step 1 changed only
`docs/` and `tools/` files that no build step uses. They would pass identically without step 1. Its evidence is the local
logs above. CI first touches the tiles when a release URL exists and `tools/fetch-tiles.sh` is wired in.

**Staged for the release, in scratch only:** `<scratch>/phase2/release-staging/` holds the six assets, 129,831,426 bytes in all
[C]: the four tiles (hard links to the downloaded files, the same inodes), F0's copy, and `README.md`.

## How the captured bytes can reach a release

**What was checked, read only, to price these.** Nothing was created.
- Through this session's proxy, the GitHub API reports `admin: true` on Forager-iOS, which is public with 0 releases. This is
  despite `gh auth status` calling the container's token invalid [C].
- `uploads.github.com`, where release assets go, answers a GET (`400` from GitHub, not a proxy refusal) [C].
- The proxy runs an "upload gate" [C: its status endpoint]. **Whether a 114 MiB asset upload passes it could not be
  determined** without uploading [?].
- The workflow token's default permissions could not be read: the proxy refuses that API path [C].
- slayer8366/Forager is public, so CI can fetch F0 by URL without a secret [C].
- The GitHub tools loaded in this session list and read releases but have no tool to create one [R: the tool list].

**Why losing these bytes is cheap:** nothing yet depends on them. Step 1b writes the expected digests, and only after the
release. Recapturing means:
- downloading again;
- running the committed harness, about 11 s for all four tiles here [C: the logs' wall times, 8 s of it `W75_N40`];
- rewriting `tools/tile-pins.tsv` and the README table in one commit.

What must hold is one invariant: **the release's bytes equal the pin file's, and the oracle read those bytes.** Every route
below keeps that, and only the first keeps today's bytes.

| | Route | What it asks of the owner | Keeps the oracle-checked bytes | Weak point |
|---|---|---|---|---|
| **A** | **Upload from this container, on the owner's word.** A session holding `release-staging/` creates the release as a **draft** with `gh release create --draft`, the six assets, and `release-notes.md`, under a tag such as `brouter-tiles-2026-10-29` on `brouter-port`. Then `tools/fetch-tiles.sh` reads every asset back through the API and checks it against the pins. **Only then is the draft published.** | **One word:** yes, and the tag's name. Nothing to download, click or check. | **Yes,** and the read-back proves it | **The container may end before the word.** The proxy may refuse a 114 MiB upload [?]. Under a draft, a failed or partial upload is never public. |
| **B** | **Recapture in a fresh session, then A.** Download anew, run the committed harness, re-pin in one commit, then upload as in A. | The same word, if it was given for "the tiles a session has checked", not for these hashes | No: new bytes, checked the same way | The same proxy question as A. Costs a short session. |
| **C** | **A CI job that captures, checks and publishes.** A new workflow downloads the four tiles and F0 (Forager is public), runs the harness against the pinned oracle, uploads with `permissions: contents: write`, and prints the pins for a follow-up commit | The word, and a trigger: a click in Actions, or a push to a marker file | No: new bytes. The pin commit follows the release, so for a while the repository pins nothing that exists | A new workflow of about 100 lines. Whether `workflow_dispatch` can run a workflow that is only on `brouter-port` is from memory, not checked: I believe it must be on the default branch, and `main` is the owner's [I]. The token's default permission could not be read. A capture that is published with no person between |
| **D** | **Files handed to the owner,** who creates the release in a browser | Create the release, drag in six files, type a tag, paste the notes, and **check six SHA-256s**, or trust the transfer | Yes, if the transfer is exact | **No channel from this container to the owner carries 114 MiB.** An artifact's binary file is capped at 15 MB [R: the Artifact tool's limits], and no file-sharing connector is attached. Unusable unless the owner names one. |
| **E** | **Chunks on a temporary branch,** reassembled and published by CI | The word, and that ruling c bends | Yes | It puts 130 MB into the repository's objects: ruling c's rejected form, and an upload now. Listed only to rule it out. |

**Recommendation: A, with B as its fallback [I].**
- A is the only route that asks the owner for nothing but a yes, and the only one that publishes the bytes checked here.
- The draft-then-verify-then-publish order means a failure leaves nothing public.
- If the container is gone, or the proxy refuses the upload, B costs one short session and the same yes.
- **So the word is best asked in this form:** "create the tile release from tiles a session has checked in the 1.7.10 oracle
  and pinned in `tools/tile-pins.tsv`, under tag `<name>`". Then it survives a recapture.
- C asks for a new workflow and a trigger, and it publishes before the pins land. That makes it the most to build, and it is
  the only route where something public can exist that the repository does not pin.

## Amendment 3, Part A: the release (third session, Linden Caliper)

**Stopped at step 2, a draft that could not be created.** `prompts/preserved/2026-10-28-01-brouter-port-amendment-3.md` on
`main` at `60ee807`. Logs are in `2026-10-29-brouter-port-phase2/partA/`.

- **Base [C]:** `brouter-port` at `0a9999a`, equal to the remote. `tools/tile-pins.tsv` and `tools/tile-release/` are
  unchanged from `91754bc` (empty `git diff`). On the remote, `91754bc...brouter-port` is 2 ahead and changes no `tools/` file
  [C: `partA/prechecks.log`].
- **Step 1, the staged files [C: `partA/step1-staged-check.log`]:** each of the 5 pin rows matched its staged file in size
  and SHA-256, read from the pin file by script, not by eye. The staged `README.md` equals
  `91754bc:tools/tile-release/README.md` by `cmp`. The folder holds 6 entries.
- **How the API was reached [C: `partA/access.log`]:**
  - through `HTTPS_PROXY`, the session's agent proxy, with the container's `GH_TOKEN`, a placeholder starting `proxy-`;
  - GitHub authenticated it as `slayer8366`, admin on Forager-iOS, with a token expiring 2026-10-30 00:41:59 UTC. So the
    proxy supplies the credential [I];
  - this is step 1's route. No token was made.
- **Before the first write [C]:** 0 releases, 0 tags, no `brouter*` tag ref.
- **Step 2 [C: `partA/step2-create.log`]:**
  - **Attempt 1:** `POST /releases` → **415 from the proxy:** "Request bodies must declare Content-Type:
    application/json. Resend the JSON body with that header." My request lacked the header. 0 releases afterwards.
  - **Attempt 2,** the same request with the header → **403 from the proxy:** "Creating, editing, or deleting releases is
    not permitted for this session type."
  - Afterwards: 0 releases (drafts included), 0 tags. The proxy listed no relay failure.
- **Not done:**
  - steps 3 to 7: no upload, read-back, publish or anonymous read;
  - `tools/fetch-tiles.sh` not wired into the build;
  - no fetch test, since there is no release URL to fetch from.
  - No other route was tried: not `gh`, the GitHub MCP tools, a workflow, nor an asset upload to a draft someone else
    makes.
- **What the 403 changes in the release routes above [I]:**
  - **A and B** both upload from a session like this one, so both are closed. Whether another session type is allowed
    could not be determined.
  - **C** would create the release with a workflow's own token, outside this proxy. After this refusal, that would be a way
    around a deliberate policy, so it is the owner's decision, not a coder's. It also means new bytes and a re-pin, as
    priced.
  - **D** is unchanged: no channel from here to the owner carries 114 MiB.
  - **An owner-made draft with session uploads** is untested. The refusal names "editing" releases, and an asset upload
    plausibly counts as that.
- **The staged files are still in scratch,** unchanged, so a later route A can reuse them while the container lasts. They
  are 5 of the 6 assets' only copies outside brouter.de, which rewrites its tiles daily. F0's copy has its source in
  Forager.

## Amendment 4: the publishing workflow (fourth session, Linden Caliper)

`prompts/preserved/2026-10-28-01-brouter-port-amendment-4.md` on `main` at `f036b94`. Base: `brouter-port` at `469b33b`,
equal to the remote [C].

**Mechanism prediction (written before building).**
- **`capture`** checks out the tools at the commit written into the file (never the triggering commit's tree), sets up
  Temurin 21.0.13+9, and runs one script, `tools/tile-check/capture.sh`. That script:
  - fetches the oracle with `tools/fetch-oracle.sh`;
  - fetches F0 from `raw.githubusercontent.com` at Forager `e2969abe4` through `tools/fetch-pinned.sh`, which refuses it
    unless size and SHA-256 equal the pins the workflow passes. This comes first, so a bad pin fails before any big
    download;
  - downloads each brouter.de tile once with `curl -D`, keeping its headers;
  - runs step 1's harness on each tile in a folder of its own. The harness now **exits non-zero** on any failed check (lookup
    version, version-checked open, `checkFileIntegrity`, zero cells or nodes decoded, a route that errs or is empty). Step
    1's version only printed;
  - writes the pins file and fills the README's table and the notes' date from it, then reads the five data files back
    through `tools/fetch-tiles.sh` against the new pins. So the dry run exercises the checker the publish job uses.
- **`publish`** runs only when `capture` succeeded and the event is `workflow_dispatch` on `refs/heads/main`. A push never
  satisfies the first half, on any branch. It refuses an existing tag or release of that name, creates a draft targeting the
  tools commit, uploads seven assets, downloads them back and checks them, and only then publishes.
- **The green dry run will show** four tiles at lookup version 11, open ok, integrity ok, nodes above zero, and a route with
  `error=null`. The bytes will differ from 20:09Z's, since brouter.de rewrites them [I].
- **The failing dry run,** with one character of F0's pinned SHA-256 changed, will stop in `capture` at F0 with
  "REFUSED forager-fixture-W75_N40.rd5 ... has SHA-256 ff6d9d8e…, pinned <the changed value>", before any brouter.de
  download. `publish` will be skipped.
- **Unknown until a run:** whether Forager's raw file is served anonymously to a runner (predicted yes, the repository is
  public [I]).

**What was built.** Commits `aa144a0` and `5d00761` (tools), `d54b95c` (the workflow), `4d667f4` (the failing variant),
`41be45e` (restored).
- `tools/tile-check/TileCheck.java`: step 1's harness, which now ends `PASS` or `FAIL: <reasons>` and exits 1 on any failed
  check. `tiles.tsv` holds step 1's route ends. `capture.sh` does the capture in the order predicted.
- `tools/tile-release/README.md` and `release-notes.md` became templates. The 20:09Z table, capture times and date are
  placeholders that `capture.sh` fills from the pins it writes. It refuses a placeholder left unfilled.
- `.github/workflows/publish-tiles.yml`, 94 lines, `TOOLS_SHA` = `5d00761`.

**Before CI [C]:**
- **The harness change bites** (`amendment4/harness-bite.log`). On a good `E170_S50` with route ends at sea, step 1's
  harness printed "from-position not mapped in existing datafile" and **exited 0**. The new one prints `FAIL: no route` and
  exits 1. On step 1's ends both exit 0. On step 1's mid-file damage both exit 1, from the decode's exception.
- **A local capture over `file://`** copies of the 20:09Z tiles: four `PASS`, F0 `PASS`, every check line equal to
  step 1's logs, and the read-back 5 of 5 (`amendment4/local-forward.log`).
- **The stale check bites:** run on real commit pairs, it exits 1 for `aa144a0`→`5d00761`, which differ in `capture.sh`,
  and 0 for `5d00761`→`d54b95c` (`amendment4/stale-check-local.log`).
- Each `run:` block passed `bash -n`. **actionlint was not run:** the session's permission check refused downloading it.

**The dry runs [R: the runs, through the GitHub API; logs in `amendment4/`]:**

| Run | Commit | `capture` | `publish` | What it shows |
|---|---|---|---|---|
| 40963971506 | `d54b95c` | success | skipped | Checked out `5d00761`. Stale check empty. F0 fetched anonymously from `raw.githubusercontent.com` and matched. Four tiles and F0 `PASS` on Temurin 21.0.13+9. Read-back 5 of 5. Tag `brouter-tiles-2026-10-29`. Artifact `tile-release`, 129,538,814 bytes |
| 40964144495 | `4d667f4` | **failure** | skipped | "REFUSED forager-fixture-W75_N40.rd5 … has SHA-256 ff6d…5990, pinned ff6d…5991". It stopped before any brouter.de download, and no artifact was made |
| 40964254673 | `41be45e` | success | skipped | The restored file. The same five `PASS` lines and the same five SHA-256s as run 40963971506. Artifact 129,538,814 bytes |

- **The refusal belongs to this edit.** It names the one changed character. The edit was to the workflow's data, so there is
  no build to fail to compile, and GitHub parsed and ran the file. The restore came from a copy saved before the edit. Its
  SHA-256 equals the saved file's, and the file equals `d54b95c`'s by `git diff --quiet`.
- **CI's check lines equal step 1's** for all five files: lookup version 11, open ok, integrity ok, the same cell and node
  counts, and the same routes (W75_N40 226 nodes / 5,047 m; F0 the same) [C: compared with `step1/check-*.log`]. So
  Temurin 21.0.13+9 on the runner routes these ends as the container's OpenJDK 21.0.12.1 did.
- **A prediction that failed:** I predicted new bytes. brouter.de's four tiles at 22:12Z have the 20:09Z SHA-256s and
  sizes, still `Last-Modified: Thu, 29 Oct 2026 01:03:01 GMT`. The site had not regenerated them since 01:03Z. Nothing
  depends on this. The owner's run will pin whatever it captures.

- **`brouter.yml` on these commits:** `aa144a0` and `5d00761` succeeded. `d54b95c`, `4d667f4` and `41be45e` were in
  progress when this was written (runs 40963971437, 40964144506, 40964254682). They are not evidence here: no Gradle input
  changed.

**The guard.** `publish` carries:
`if: success() && github.event_name == 'workflow_dispatch' && github.ref == 'refs/heads/main'`. A push-triggered run has
`event_name` `push`, so `publish` is skipped on every branch. Both push runs show `publish` skipped. The `push` trigger is
limited to `brouter-port` in any case. A `workflow_dispatch` on another branch, possible only through the API, also skips
it. `success()` with `needs: capture` means a failed capture never reaches it.

**Not exercised by any run: the whole `publish` job.** The artifact download, the tag check, `gh release create --draft`,
the read-back and the publish have never run. What I read for them:
- `gh`'s trunk source on 2026-10-29: `FetchRelease` finds a draft by its pending tag (`pkg/cmd/release/shared/fetch.go:182`),
  and `gh release download` fetches through the asset's API URL (`download/download.go:243`) [R]. The runner's `gh` version
  was not read [?].
- The download-artifact README at `v8.0.2` pairs it with upload-artifact `v7` [R].
- That a same-run artifact download needs no `actions: read` is from memory [I].
- Whether the repository lets `GITHUB_TOKEN` create releases is the owner's question (RECORD -30) [?].
- Each failure there stops before publishing. One before the draft leaves nothing behind. One after it leaves a draft,
  which is not public [I, as in step 1], for the owner or a session to delete.

## Amendment 3, Part B, step 2a: `util` (fifth session, Linden Caliper)

Base: `brouter-port` at `f57b003`, equal to `origin/brouter-port` after `git pull` [R]. The Java ported is Forager's read-only
clone at `994b4384`, `third_party/brouter/src/main/java/btools/util/`; the oracle is the `-ro` jar's own `btools.util`
classes [R: `unzip -l`]. Scope is T2's port list for `btools/util/` less `CheapRuler`, `CheapAngleMeter` (2b) and
`StackSampler` (B row 5): 11 files, 1,302 kept Java lines [C: the `kept_lines` column of `03-reach/out/T2/port-list.tsv` at
`20e1f92`, 1,332 less `StackSampler`'s 30], ported method by method from `reached-methods.tsv`. Plus the three helpers item
I's 2a row names: Java's `Math.round`, `Character.isWhitespace`, Java's `split`.

**Mechanism prediction (written before the port, after the stubs' run):**
1. The port, line for line with Java's `int`/`long` arithmetic, equals the oracle on every 2a test on the JVM: the 22 new
   test cases (21 port-facing plus one check of the common cases' expected values) all pass, 0 failures.
2. `javaRound`, written as the JDK's bit-level `Math.round`, equals `Math.round` on all ~3.07 million inputs, and the
   common cases pass on the iOS simulator, where Native's `roundToLong` would fail 2 of them (item H).
3. `Character.isWhitespace` is true for exactly 25 `char`s on JDK 21: 9 to 13, 28 to 31, U+0020, U+1680, U+2000 to U+2006,
   U+2008 to U+200A, U+2028, U+2029, U+205F, U+3000.
4. **The revert check** (item I: one `<` to `<=` in `SortedHeap`; the one chosen is `getMinBin`'s first comparison,
   `SortedHeap.java:139`) fails all four `SortedHeapTest` cases, each on a pop-sequence mismatch, and no other test.

**Sub-step 1, tests over stubs** [C: `2026-10-29-brouter-port-phase2/step2a/1-stubs-jvmTest.txt`]: the build log has no
compile errors; the JUnit XML reads 25 tests, 21 failures. Every failure is `kotlin.NotImplementedError` thrown in the stub
its test targets (the first port frame of each is in the log). The one pass is
`JavaLangTest.theCommonCasesExpectedValuesAreTheJdks`, which reads only the JDK: **not shown to bite**, by design, since it
checks the expected values `JavaLangCommonTest` asserts, so none of them is from memory.

**Step 2a: the handoff to the port session** (the planner's message at 22:29Z: hand off after the tests-first push).
- **Done:** sub-step 1 only, at `3cd61ad`: 12 stub files under `brouter/src/commonMain/.../brouter/util/` (package
  `com.zynergylabs.forager.brouter.util`, all `internal`), 8 jvmTest classes and 2 commonTest files. Not done: the port, the
  revert check, any CI read of `3cd61ad` (the jvm job will be red by design, 21 failures; the macOS smoke job now also runs
  `JavaLangCommonTest` over stubs, so it will be red with 3 failures by design, not a regression).
- **Port into the stubs, keeping their signatures;** the tests call these exactly. `LruMapNode` and `IByteArrayUnifier` are
  already complete (fields and a declaration only).
- **Calls I made in the stubs, for the next session to keep or report:**
  - `CompactLongMap.moveToFrozenArrays` is `internal`, not `protected`: Java's `protected` includes package access, which
    `FrozenLongMap`'s call on another instance (`FrozenLongMap.java:25`) uses [R].
  - `ByteArrayUnifier.unify` is not on T2's port list (only the constructor is), but the class implements the interface, so
    the stub's `unify` must become a named failure: `UnsupportedOperationException` with a message starting "unsupported",
    which `ByteArrayUnifierTest` asserts. **A reach question for step 5b, not settled here:** `OsmNode.parseNodeBody2`
    (reached) calls `mc.readUnified(nodeDescSize, abUnifier)` at `OsmNode.java:134`, which calls `u.unify` with that
    `ByteArrayUnifier` (`MicroCache2.java:30`) [R], yet neither `readUnified` nor `ByteArrayUnifier.unify` is on the port
    list. Whether that line runs for Forager's tiles: could not determine [?].
  - `ByteDataReader.ab` should be `ByteArray?`: Java's constructors and `reset` accept null (`ByteDataReader.java:16`) [R].
- **Notes for the port, from reading the Java** [R unless marked]:
  - `SortedHeap.getMinBin` assigns inside its conditions; `bin = bin.nextNonEmpty ?: return minBin` keeps the order. Keep
    all 30 unrolled comparisons; the revert check edits the first, `:139`.
  - Kotlin masks `shl`/`shr`/`ushr` counts to 5 bits on every target, as Java does [I, from memory; the tests' counts 0 and
    32 to `decodeBits` exercise it on the JVM only]. `decodeBitsReverse` uses `>>` (arithmetic), the rest `>>>`.
  - `BitCoderContext`'s static block builds a `BitCoderContext` and runs its third loop over the first 1,024 entries again;
    port it as is in the companion's `init`, after the arrays.
  - `Crc32`'s table has literals above `Int.MAX_VALUE`; Kotlin needs `.toInt()` on those. `Boolean.getBoolean
    ("earlyDuplicateCheck")` is `false` (B row 17).
  - Two oracle behaviours the tests steer around, read from the Java: `encodeBounded` never ends for `max >= 2^30`
    (`BitCoderContext.java:190-197`), and `LruMap.removeLru` dereferences a null `lru` at `maxSize` 1 (`LruMap.java:80-81`).
  - `javaRound`: the JDK's `Math.round(double)` bit algorithm (shift from the biased exponent; `(long) a` outside it), not
    `roundToLong`. The JDK's `src.zip` in this container is a dangling link, so it is written from memory and the 3.07 million
    inputs in `JavaLangTest` are its check [I]. `javaSplit`: `Regex(regex).split(this)` with trailing empty strings dropped
    (B row 13). `javaIsWhitespace`: the 25 `char`s of prediction 3; `JavaLangTest` compares all 65,536.
- **Local build:** Maven Central answered 429 here; `-I .../scratchpad/phase2/mirror.init.gradle.kts` (never committed)
  worked. The iOS klib compile was not run locally; CI's `jvm` job runs it.
- **Then:** sub-step 2 is pushed when `jvmTest` reads 25 tests, 0 failures from the XML (prediction 1), with CI read for
  that commit; sub-step 3 is the revert at `SortedHeap.kt`'s line for `SortedHeap.java:139` under the runner rules.

**Step 2a, sub-step 2: the port (sixth session, Linden Caliper)** [C: `step2a/2-port-jvmTest-24.txt`, `step2a/2-loop/`]
- **Base:** `brouter-port` at `41cc4ca`, equal to `origin/brouter-port` after `git pull` [R]. Forager's
  `third_party/brouter/src/main/java/btools/util/` was last changed at `994b4384`, with a clean tree [R: `git log -1`,
  `git status`].
- **The port:** 10 stub files filled, method by method against the Java. `IByteArrayUnifier` and `LruMapNode` are
  unchanged. The 256 CRC literals and `getMinBin`'s 31 unrolled steps are generated from the Java text by script, not
  retyped. `getMinBin` has **31** comparisons (`SortedHeap.java:139` to `:199`), not the 30 the handoff gave [C: `grep -c`].
- **The 24 tests that can finish:** run locally by an include filter that is never committed (`step2a/runner/filter24.txt`).
  The build log has 0 compile errors. The JUnit XML reads **24 tests, 0 failures**, on the first run.
- **The 25th test cannot finish, under any port (the stop).** The first full run sat in
  `rawBitReadsOnRandomBytesMatchTheOracle` for over 10 minutes. A thread dump put the worker in the test's own loop
  (`BitCoderContextTest.kt:105`), with 574 s of CPU, not in port code. The loop ends only when the oracle's reading
  position reaches 32,640 bits. But about one op in 250 resets both contexts to bit 0 (`r.nextInt(5)` then
  `r.nextInt(50)`), and an op advances 7.97 bits on average. Run on the oracle alone, with the test's draws in the test's
  order and a cap of 20 million ops, seeds 1 to 3 never end; the furthest any got is bit 26,863 [C: `2-loop/LoopCheck-out.txt`].
  The sub-step 1 run could not show this, because each stub threw on its first call. The handoff's finish line ("25
  tests, 0 failures") is therefore unreachable as written. I have not changed the test: that is a choice about what it
  covers, and it goes to the planner. Options:
  - **(a)** Add an op cap to the loop condition, keeping the position bound (it guards Java's unguarded `ab[++idx]`
    reads) and both assertions as they are.
  - **(b)** Drop the in-loop reset. This loses the reset-mid-stream coverage.
  - **(c)** Make the reset rarer. Any rate that lets the loop end also makes resets rare per seed.

  My default would be (a), with a cap of about 1 million ops per seed. It is not applied.
- **The 25th test's comparison, run outside the test** [C: `2-loop/PortLoopCheck-out.txt`]. A scratch Java harness
  (`2-loop/PortLoopCheck.java`) runs the same loop, draws and two equalities (value and reading position) on the oracle
  and the port's `brouter-jvm.jar`, capped at 1 million ops per seed. Over 20 seeds it compared **20,000,000 ops**, about
  4 million of each of the five reads, with 80,247 resets and **0 mismatches**. It bites: with one bit flipped at byte
  100 of the port's copy, it reports 56,167 mismatches in 2 million ops (`2-loop/PortLoopCheckBite.diff`,
  `-out.txt`). The ops never reach past bit 21,994, so the last ~1,350 bytes, and `fillBuffer`'s end-of-array branch,
  are not read by this loop, under the test or the harness [C].
- **Calls in the port:**
  - `javaSplit` follows `Pattern.split`'s loop, not `Regex.split` plus trimming. Its results are the same for every
    regex the reached code passes. A zero-width match throws "unsupported" instead of guessing how Java skips it.
  - Java's static `earlyDuplicateCheck` is the constant `false` (B row 17).
  - Java's unguarded null dereferences are `!!`, so they throw where Java's do (`LruMap.removeLru`,
    `CompactLongMap` after freezing).
  - Kotlin's infix operators share one precedence, below `+`, so Java's
    `(value << count) | reverse_byte[b & 0xff] >> (8 - count)` (`BitCoderContext.java:242`) is parenthesised. Without the
    parentheses Kotlin would shift the whole `or`.
  - Write-only Java fields stay as Java has them: `SortedHeap.peaksize`, `BitCoderContext.bm2bits`, and the
    `ByteArrayUnifier` constructor's arrays.
- **Lines:** the Kotlin in `util/` is 1,282 lines across the 12 files (992 not blank or comment). That includes
  `JavaLang.kt`'s 69 lines, which have no Java counterpart. The Java's 11 files are 1,542 lines (1,174 not blank or
  comment), including the methods not ported. The plan's figure, 1,302 kept lines, is T2's `kept_lines` column. I did not
  re-derive what that column counts, so the two Kotlin counts are not set against it line for line [C, `wc -l` and `grep`].
- **Not run locally:** the iOS klib compile and the simulator. CI's `ios-smoke` job compiles the simulator target and runs
  `JavaLangCommonTest` there. CI's `jvm` job will reach the 25th test and be stopped by its 30-minute timeout
  (`.github/workflows/brouter.yml:18`). Its `iOS klibs` step comes after `jvmTest`, so it is expected not to run on this
  commit [I, from the workflow's step order].

**Step 2a, sub-step 3: the revert check** [C: `step2a/3-revert-SortedHeap139.txt`; runner `step2a/runner/revert-check.sh`]
- **The edit:** one line, `SortedHeap.kt:112`, the port of `SortedHeap.java:139` (`getMinBin`'s first comparison):
  `<` to `<=`. The runner confirmed exactly 1 line differs from a copy saved before the edit.
- **The run:** the 24 tests that can finish. The 25th cannot reach `SortedHeap`: only `SortedHeapTest` names it
  [C: `grep -rl SortedHeap brouter/src`]. The old XML is deleted before the run, and the build log has 0 compile errors,
  so the results are this build's.
- **Result:** 24 tests, 4 failures: `fewDistinctKeysPopInTheOraclesOrder`, `allKeysEqualPopInTheOraclesOrder`,
  `aLargeHeapThroughManyBinsPopsInTheOraclesOrder` and `clearAndEmptyPopsMatchTheOracle`. Each is an `AssertionError`
  on the pop-sequence list, seed 1. The other 20 tests pass.
- **Can this revert produce each failure?** Yes. All four are pop-order mismatches in the one class that reaches the
  edited line, and a tie that now goes to the later bin changes pop order. **Prediction 4 held.**
- **After:** the file is restored from the saved copy, not from git. It is identical to the saved copy and to `612bb0d`'s
  file, the tree is clean, and line 112 reads `<` again.

## Amendment 3, Part B, step 2a close: the ruling on the 25th test (seventh session, Linden Caliper)

Base: `brouter-port` at `87e7fc9`, equal to `origin/brouter-port` after `git fetch` [R]. The ruling is RECORD -33, read at
`origin/main` `45df5f3` [R]. Logs are in `2026-10-29-brouter-port-phase2/step2a/`; the runners are `step2a/runner/`, run
from a scratch copy that is byte-identical (`cmp`) and finds the mirror init script, which is never committed.

**Mechanism prediction, written before building** [C: `step2a/4-close-prediction.txt`, 23:00:36Z]:
- **P1:** case 1 ends at the cap on all 20 seeds, 20,000,000 ops.
- **P2:** case 2 ends at the position bound on all 20 seeds, in about 4,100 ops each.
- **P3:** case 2 does not reach `fillBuffer`'s end-of-array branch.
- **P4:** both pass over the port: 26 tests, 0 failures.
- **P5:** each case fails alone on its own flipped bit, on seed 1. For case 1 the prediction was op 1,775; for case 2,
  the first op that reads bit 800.

**The change** (`d5371c7`, `BitCoderContextTest.kt`):
- **Case 1, `rawBitReadsOnRandomBytesMatchTheOracle`:** the loop condition gains `&& i < 1_000_000`. The position bound,
  the draws, the reset and both equalities are unchanged.
- **Case 2, `rawBitReadsWithoutResetsToThePositionBoundMatchTheOracle`:** case 1's loop without the reset, and so
  without its `nextInt(50)` draw. It keeps the same seeds and bytes, the same bound and both equalities, and has no cap.
- **Both cases** print each seed's op count and final reading position after the loop.

**Termination on the oracle alone** [C: `2-loop/LoopCheck2.java`, `LoopCheck2-case1-out.txt`, `LoopCheck2-case2-out.txt`]:
- **Case 1:** all 20 seeds end at the cap, at 1,000,000 ops each, 20,000,000 in all. There are 80,247 resets, and the
  furthest reading position is 21,994 (seed 19). Both figures equal what PortLoopCheck reported on these draws. **P1 held.**
- **Case 2:** all 20 seeds end at the position bound, after 3,976 to 4,250 ops, 81,969 in all. The final positions are
  32,641 to 32,663. **P2 held.**

**A wrong premise in the ruling: case 2 does not reach `fillBuffer`'s end-of-array branch** (P3 held).
- **The branch:** `if (idx++ < idxMax)` is false (`BitCoderContext.java:250`), and the call leaves `idx` above `idxMax`
  (4,095) [R].
- **What case 2 reaches:** read by reflection after every op, the oracle's `idx` peaks at 4,079 to 4,084 per seed and is
  never above 4,095 [C]. The loop stops at reading position 32,640 (byte 4,080), and the buffer looks ahead at most 31
  bits. Firing the branch needs a reading position past about bit 32,744 [I, from the source].
- **Positive control** [C: `2-loop/BranchControl.java`, `-out.txt`]: case 2's draws with no bound, run until the oracle
  throws.
  - The detector reads `idx` above 4,095 on 19 of 20 seeds. On seed 9, `decodeBounded`'s unguarded `ab[++idx]` throws at
    index 4,096 before any `fillBuffer` passes the end.
  - Every seed then throws `ArrayIndexOutOfBoundsException` within 0 to 15 ops: 17 in `decodeVarBits` and 3 in
    `decodeBounded`.
  - So the "not reached" above can read "reached".
- **What follows:** the branch is reachable only past the bound, and past it, Java's unguarded reads throw within a few ops.
  The ruling's "runs to the position bound, so the reads reach the end of the byte array and `fillBuffer`'s
  end-of-array branch" cannot hold for this bound.
- **What I did:** I built case 2 as the ruling words it. It reads bits 21,994 to about 32,660, which case 1 never reaches.
  Its comment says it stops short of the branch.
- **Options for the planner (none applied):**
  - **(i)** Keep case 2 as it is (the current state).
  - **(ii)** Add a third case with no bound that runs until the oracle throws. It would assert that the port throws the
    same exception class at the same op, with values and positions equal until then. It reaches the branch on 19 of 20
    seeds, for 0 to 15 ops each. It makes Java's out-of-bounds failure part of what the port must match, which is a
    contract choice. It is JVM-only, and whether Native's exception class matches was not checked [?].
  - **(iii)** Past the bound, issue only the reads that go through `fillBuffer` (`decodeBits`, `decodeBitsReverse`). On
    the zero padding, `decodeVarBits` falls to `decodeVarBits2` and throws [I, from the source, not run].
  - My default would be (ii), as a separate case, if the branch is wanted at all.

**Over the port** [C: `step2a/4-close-jvmTest-full.txt`]:
- **The run:** full, with no filter. 0 compile errors; **26 tests, 0 failures**. Case 1 took 3.4 s and case 2 0.2 s
  [C: the XML's `time`]. **P4 held.**
- **The test's own counts:** its per-seed op counts and final positions equal the oracle-alone log on all 40
  seed-cases [C: `2-loop/test-per-seed-ops-local.txt`, diffed against `LoopCheck2`'s output].

**The bite** [C: `step2a/5-bite-A-case1.txt`, `5-bite-B-case2.txt`; runner `step2a/runner/bite-check.sh`]
- **The edit, one line per run:** bit 0 of byte 100 is flipped in one case's port input,
  `BitCoderContext(bytes.copyOf().also { it[100] = (it[100].toInt() xor 1).toByte() })`, as PortLoopCheckBite did.
- **The runner rules:** the file is saved before the edit; the old XML is deleted; results are refused on any `e: ` line.
  Afterwards, the file is restored from the saved copy and compared with the saved copy and with HEAD.
- **Run A** (`:97`, case 1): 0 compile errors; 26 tests, **1 failure, case 1 only**: "seed 1 op 206: value
  expected:<43660> but was:<43661>".
- **Run B** (`:131`, case 2): 0 compile errors; 26 tests, **1 failure, case 2 only**: "seed 1 op 108: value
  expected:<113712> but was:<113714>".
- **Can each edit produce its failure?** Yes. On the oracle, case 1's op 206 is `decodeBits(16)` from bit 800 exactly, so
  the flipped bit is the value's bit 0 (+1) [C: `2-loop/Op206.java`]. Case 2's op 108 is the first read covering bit 800,
  `decodeBitsReverse(24)` over bits 778 to 802, where bit 800 carries weight 2 (+2) [C: `2-loop/FirstRead800.java`, run
  before B]. Each run's other case passes.
- **After each run:** the file equals the saved copy and HEAD, and the cap and case 2 are present.
- **P5 for run A was wrong, and the source was a truncated log.** I took op 1,775 from
  `2-loop/PortLoopCheckBite-out.txt` as that harness's first mismatch.
  - That file holds 2 mismatch lines, but the harness prints the first 5.
  - Re-run from the same source on the same port, it prints five, the first at op 206 [C:
    `2-loop/PortLoopCheckBite-rerun-out.txt`]. The totals are equal: 2,000,000 ops and 56,167 mismatches.
  - How the three lines were lost: could not determine [?].
- **Runner notes:**
  - Run A's first log reads "tree clean before: NO". The cause was the runner's own temp parts, written beside its
    output inside the repository. The runner now writes them to its own folder, and run A was run again with the same
    result. The first log is kept as `5-bite-A-case1-run1.txt`.
  - Run B's "NO" is run A's output, untracked at the time; it is the only path B's `git status after` names.

**CI, run 40970686102 on `9ce54ee`: `success`** [R: the job logs; extracts `step2a/6-ci-run-40970686102-jvm.txt`,
`-ios-smoke.txt`]:
- **`jvm`:** JVM tests and **iOS klibs** both `BUILD SUCCESSFUL` (`compileKotlinIosArm64`, `compileKotlinIosSimulatorArm64`),
  with 0 `e: ` lines. The JUnit XML reads **26 tests, 0 failures, 0 errors, 0 skipped, in 12 files**. Case 1 took 2.0 s
  and case 2 0.1 s. CI's 40 per-seed lines equal the local run's (diff).
- **`ios-smoke`:** `BUILD SUCCESSFUL`; **4 tests, 0 failures in 2 files** (`ToolchainSmokeTest` 1, `JavaLangCommonTest` 3).
  This is simulator-checked.
- `d5371c7` and `316ac1f` went up in the same push as `9ce54ee`, so they have no runs of their own. Run 40970872936 is on
  `5676f99`, which changes only the report: not read.
- **Runs 40969246695 (`612bb0d`) and 40969414415 (`87e7fc9`):** both were still `in_progress` at 23:09:09Z [R]. In
  40969246695, `ios-smoke` finished `success`, and `jvm` was still in its JVM-tests step, which started at 22:53:39Z. That
  puts its 30-minute limit at about 23:23Z [I]. Whether they timed out: not read, because the planner asked for the report
  first [?].

**Status: step 2a's finish line is met** (26 tests, 0 failures, locally and in CI, with the klibs compiled). Whether the
step closes with case 2 short of the branch is the planner's call.

## Amendment 3, Part B, step 2b, first part: msun `cos` and `atan2` (eighth session, Linden Caliper)

Base: `brouter-port` at `c225648`, equal to the remote at the start [R: `git status`]. Evidence in
`2026-10-29-brouter-port-phase2/step2b/`.

- **The port [R: the C at bionic `06356e41`, sizes and SHA-256 equal to research/android-math's `sources.tsv`]:**
  `msun/MsunCos.kt` (`s_cos.c`, `e_rem_pio2.c` inlined, `k_cos.c`, `k_sin.c`), `msun/MsunAtan2.kt` (`e_atan2.c`,
  `s_atan.c`), `msun/MsunBits.kt` (GET_HIGH_WORD, GET_LOW_WORD), `msun/MsunUnsupportedException.kt`. Same expressions and
  parenthesisation as the C; Sun's notice kept. C's `goto medium` is a flag; `nan_mix` is `x + y`.
- **The unsupported threshold [R: `e_rem_pio2.c`, `if(ix<0x413921fb)`]:** finite |x| >= 1,647,099.0 exactly (high word
  `0x413921fb`, low word 0) throws `MsunUnsupportedException("cos", x)`. `atan2` has no unsupported range.
- **The C reference's route into CI: built in the `ubuntu-24.04` job, every run** (`tools/msun-ref/build-ref.sh`, a
  Gradle task `jvmTest` depends on). It fetches the ten files, refuses any that miss its pin, compiles with
  `-O2 -ffp-contract=off -fno-builtin -fno-math-errno` by clang and by gcc, requires their outputs equal, and requires
  `msun-cos.txt` and `msun-atan2.txt` to carry the research's SHA-256 (`068e61eb…`, `466fb82d…`). The research's harness
  files are copied unchanged. **Why built rather than digests alone:** a committed digest's generator would never run in
  CI, so nothing would show it still reproduces; the build is ten seconds. The iOS simulator cannot build the C, so
  commonTest reads a generated copy (`MsunReference.kt`, by `gen-kotlin.py`), and `MsunReferenceCommittedTest` fails
  unless it equals the fresh build line for line. [C: CI log, "clang and gcc outputs equal", "equal the research's SHA-256"]
- **Tests:** commonTest (JVM and simulator): the 1,800 table rows (latitudes as CheapRuler computes them, then `cos`); the
  299,974 regenerated pairs hashed in at2.c's form against the research's SHA-256; 45 `cos` and 59 `atan2` edge lines
  (signed zeros, NaNs, infinities, subnormals, every reduction boundary, all three medium iterations, the threshold);
  the refusal. jvmTest adds row-by-row comparison with the C build, `atan2` against `StrictMath.atan2` at every pair
  (0 differ), and `cos` against `StrictMath.cos`, which differs at exactly the research's 13 rows.
- **Failing first [C]:** locally 37 tests, 8 failures (`step2b/1-stubs-jvmTest.log`); CI run 40972738575 on `4bc9e7b`:
  `jvm` 37 and 8, `ios-smoke` 10 and 5, every failure the stub's `NotImplementedError` or the refusal test's type check.
- **The port [C]:** 37 tests, 0 failures locally on the first run (`step2b/2-port-jvmTest.log`), then CI as above.
- **The revert check [C: `step2b/3-revert-C1.txt`, `step2b/predict-C1.txt`]:** `C1` in `k_cos` one ulp up (`…554C` to
  `…554D`), one line. **Predicted before the run** by building the same edit in bionic's C: rows 502, 1297 and 1318
  change; no edge line; the StrictMath rows become the other 10. **The run:** 0 compile errors; 11 msun tests,
  3 failures, the three predicted; both `cos` tests name rows 502, 1297 and 1318, and the StrictMath list is the 10.
  Restored from the saved copy; the file equals HEAD's afterwards.
- **Calls beyond the amendment:**
  - NaN results are checked as NaN, not by bits. The x86_64 reference returns `fff8…` for `cos(Inf)`, which arm64 does not
    [C: `MsunReference.kt`; arm64's default NaN is [I]].
  - The package is `com.zynergylabs.forager.brouter.msun`; the functions are `msunCos` and `msunAtan2`.
  - Maven Central answered 429 twice in this container, so local Gradle used a mirror init script (`step2b/
    mirror.init.gradle.kts`, never in the build). CI does not use it.
  - The C reference is fetched from android.googlesource.com in every CI run; an outage there fails `jvm`.
- **Unverified:**
  - a phone's own `libm.so` (the research's limit, carried);
  - which iOS simulator runtime ran the tests [?];
  - NaN payloads on arm64 [?];
  - that Kotlin/Native never fuses multiply-add. The 13 rows and the digest passing on the simulator are evidence it
    did not here [I].
- **Handoff to step 2b's second part:** `CheapRuler` and `CheapAngleMeter` call `msunCos` and `msunAtan2`; then the seam
  for logic runs and the simulator job. Before writing any loop, check that it terminates. The runner is
  `step2b/runner/revert-check.sh`. Give it an absolute out-path; a relative one landed beside the repository once and was
  moved.

## Amendment 3, Part B, step 2b, second part: `CheapRuler`, `CheapAngleMeter`, the seam (ninth session, Linden Caliper)

Base: `brouter-port` at `b7543f3`, equal to the remote at the start [R: `git status -sb`]. Evidence in
`2026-10-29-brouter-port-phase2/step2b-2/`. The prediction was written before any code (`0-prediction.txt`).

- **The port [R: `CheapRuler.java`, `CheapAngleMeter.java` at 1.7.10; reached methods from T2's `reached-methods.tsv`]:**
  `util/CheapRuler.kt` (the table, `getLonLatToMeterScales`, `distance`) and `util/CheapAngleMeter.kt` (`calcAngle`,
  `cosAngle`, `getAngle`, `getDirection`, `normalize`, `getDifferenceFromDirection`). Java's order is kept, and so is its
  int arithmetic: `normalize`'s `360 * (int) (...)` wraps, as Java's does. `destination` is not reached and is not ported.
  `Math.toDegrees` is `x * 57.29577951308232` [R: `javap -c` of OpenJDK 21.0.12.1's `java.lang.Math`].
- **The seam, in two lines:** `LibmSeam` (`scales(row)`, `atan2`) is a parameter of every `CheapRuler` and `CheapAngleMeter`
  call. Production passes `MsunLibm`, whose table is built once from `msunCos`, with `msunAtan2`. jvmTest's logic run passes
  `OracleLibm`, which holds the oracle's own `SCALE_CACHE` (read from its `btools.util.CheapRuler`) and this JVM's
  `Math.atan2`.
- **The flag [C]:** `CheapLogicRunTest.theJvmRunsWithLibmIntrinsicsOff` reads the JVM's arguments and HotSpot's
  `UseLibmIntrinsic` (`false`). It also requires `Math.cos` to equal `StrictMath.cos` at all 1,800 rows. That half can
  fail: in a scratch run (`0-flag-scratch.txt`), the default flags gave 60 of 1,800 rows differing, and the flag gave 0.
- **Logic run [C, local; CI below]:** the port's table arithmetic, fed the oracle's `Math.cos`, equals the oracle's table
  at all 1,800 rows. CI, Temurin 21.0.13+9, gave the same counts and branch figures (`ci-run-40975094092.txt`). With `OracleLibm`, the port is bit for bit with the oracle at all of these: `distance` at 200,000
  cases (50,000 zero); `calcAngle` and `cosAngle` at 200,000, with each branch counted (dd==0 20,166; +90 39,680; -90 39,456;
  cosp<0 50,453; plain 50,245); `getAngle` and `getDirection` at 2,399,792 evaluations (each pair in four signs); and
  `normalize` and `getDifferenceFromDirection` over 20,021 values, 20,000 pairs and 441 edge pairs.
- **Production run [C]:** the production table is msun's at all 1,800 rows (`CheapProductionTableTest`, commonTest). Against
  the oracle (`CheapProductionRecordTest`), `cos` differs at exactly the 13 known rows, each by one ulp. `kx` differs by one
  ulp, in the same direction, at all 13 rows, and `ky` at none of them. Each row is printed in the test's output
  (`2-port-record.xml`). Production `getDirection` differs at 0 of 1,199,896.
- **H step 4's platform-invariants test, as far as step 2b reaches (commonTest, so on the JVM and the simulator):** fused
  multiply-add never formed (double, float, and the table's `2 * c * c - 1` shape); Java's double-to-int and `%`; and
  the production table (18 chunk digests) and angles (30 chunk digests), against constants. A one-ulp positive control runs
  on every platform. **The constants were written from outside the port, before it existed**
  (`PlatformInvariantsExpectedTest`): the table is bionic's C `cos` through a copy of the Java arithmetic, which is first
  pinned to the oracle's table; the angles are the oracle's own `getAngle`/`getDirection`. Locally they came from OpenJDK
  21.0.12.1. On CI's Temurin 21.0.13+9 they passed unchanged in run 40974977957. Not covered yet: parsing, printing,
  min/max, the rounding sites, `hmap`.
- **Failing first [C]:** locally, 17 new tests had 13 failures, every one the stubs' `NotImplementedError`
  (`1b-stubs-with-constants.txt`). The flag test, the fused-multiply test and the two expected-digest tests passed on the
  stubs, as they should, because none of them reaches the port. CI run 40974977957 on `35a1fb6`: `jvm` failed as intended.
- **The revert check [C: `3-predict-row1234.txt`, `4-revert-row1234.txt`]:** one line in `MsunLibm` moves row 1234's `cos`
  up one ulp. Predicted first: `kx` changes (`…bfbb` to `…bfbc`), `ky` does not, and 3 tests fail. **The run:** 0 compile
  errors; **4 failures**, each naming row 1234 or chunk 12; `kx` as predicted. The fourth,
  `oneUlpInOneRowChangesItsChunksDigest`, also asserts the other 17 chunks against the constants, so it sees chunk 12 move.
  **The prediction missed it**; this edit can cause that failure, so it is not a stale run. The file was restored from the
  saved copy and equals HEAD.
- **The simulator job:** `ios-smoke` is now G's simulator job; its id is kept so earlier runs read the same. It runs all of
  commonTest, then H step 4's JVM half on arm64 HotSpot, which is **recorded and does not gate the job**
  (`continue-on-error`).
- **Calls beyond the amendment:**
  - the seam is a parameter (`LibmSeam`), not a global, following item I's step 8 ("the table and atan2 seam ... as a
    parameter");
  - Java's static methods take the seam as their first argument;
  - the revert edits the production table's builder, not `msunCos`, so that the msun classes stay out of it;
  - an out-of-range `ilat` throws Kotlin's `IndexOutOfBoundsException` where Java throws `ArrayIndexOutOfBoundsException`.
- **Unverified:**
  - which simulator runtime ran the tests [?];
  - "by stratum" in the production record: strata are route-level, and they arrive with E's steps;
  - the revert check ran on the JVM only, not on the simulator;
  - **the arm64 HotSpot step did not run its tests [C: `ci-run-40975094092-ios-smoke.log`]:** jvmTest depends on
    `msunReference`, and bionic's C does not compile against the macOS SDK (`freebsd-compat.h:35`, `digittoint`, then
    `endian.h`). The job stayed green because the step is `continue-on-error`. Part 6's arm64 question is still open.
    For the next session: run it with `-x msunReference`, and only the two classes that do not read the C build
    (`CheapLogicRunTest` and `CheapProductionRecordTest`);
  - **the simulator has no failing-first evidence from CI:** on `35a1fb6`, `ios-smoke` failed before any test because Maven
    Central answered 429 while Gradle resolved the Kotlin plugin. `jvm` failed as intended there: 54 tests, 13 failures,
    the same 13 as locally (`ci-run-40974977957.txt`).
- **Handoff to step 3 (`codec`):** `codec` calls neither `CheapRuler` nor `CheapAngleMeter` [R: grep of `btools/codec`].
  The first callers are in `mapaccess`: `OsmNode.calcDistance` (`OsmNode.java:103`, which needs `javaRound`) and
  `WaypointMatcherImpl` (`:52`, `:61`, `:80`, `:189-212`). Each will need a `LibmSeam` passed in. Runners are in
  `step2b-2/runner/`. Give them absolute out-paths.

## Amendment 3, Part B, step 3: `codec` (tenth and eleventh sessions, Linden Caliper; written up in the twelfth)

This section was written after the fact, in the twelfth session, on the planner's ruling at RECORD -39. Its sources are
RECORD -38 and -39 on `main` at `b945259` [R], the logs in `2026-10-29-brouter-port-phase2/step3/` [C, read again in the
twelfth session], and `gh run list` on 2026-10-30 [R]. Commits: `f034bb0` (the arm64 probe), `b3182d8` (stubs and tests),
`4436711` (the port), `a0b04e1` (the arm64 flags), `d99cc46` (logs and the first revert); then `2215741` (the runner and
three predictions, committed before any run), `db53413` (a runner fix), `0f4fe0b` (the results), `7bf6a3d` (the
arch-aware flag test). That the two halves were two sessions is inferred from RECORD -38 and -39 each giving its own
harness count [I].

- **The arm64 test JVM, cause seen before the fix [R: RECORD -38].** On `macos-26` (aarch64, Temurin 21.0.13+9),
  `-XX:-UseLibmIntrinsic` is an unrecognised option and the JVM does not start. The probe (`f034bb0`) measured, over
  200,000 inputs, `Math.cos` differing from `StrictMath.cos` at 11,628 by default and at 0 under
  `-XX:DisableIntrinsic=_dcos`, with and without the JIT; `atan2` at 0. `brouter/build.gradle.kts` now picks the flag by
  `os.arch` (x86_64 `-XX:-UseLibmIntrinsic`; aarch64 `-XX:DisableIntrinsic=_dcos`; any other architecture fails the
  build).
- **The port [R: RECORD -38]:** the decoder halves of `DataBuffers`, `StatCoderContext`, `NoisyDiffCoder`,
  `TagValueCoder` and `MicroCache`. One compile error on the stubs, a JVM name clash (`size` against `getSize()`), fixed
  with `@JvmField`.
- **Failing first [C: `1-stubs-jvmTest.log`]:** 8 of 8 codec tests failed on the stubs' `TODO`s.
- **Green [C: `2-port-jvmTest.log`; R: RECORD -38]:** 8 of 8 locally; CI on `4436711`, `jvm` 62 tests, 0 failures.
- **Two test expectations corrected, beyond "compile errors only", both upheld at RECORD -38:** `StatCoderContextTest`'s
  predictors kept under 2^25 (the oracle itself misdecodes beyond 24 noisy bits), and `TagValueCoderTest`'s validator
  check applied only to a non-empty set (the oracle does not call it on the empty one). Condition on the first: steps 5a
  and 5b log the largest noisy-bit count the decoders meet on the captured tiles.
- **The four reverts, each predicted first [C]:**
  - item I's, in `BitCoderContext.fillBuffer`'s byte read (`codec` has no sign-extension site of its own): 3 of 8 failed
    as predicted (`3-revert-sign-extension.txt`);
  - RECORD -38's three, run with `runner/revert-check.sh`, which refuses to read results after a compile error, restores
    from a copy saved before the edit, and checks afterwards that the forward line is still present. Each failed 1 of 8,
    as predicted at `2215741`, with 0 compile errors:
    - **a decoder,** `StatCoderContext.kt:72` (Java `:250`) dropping the right branch's bit: failed at seed 1, op 3,
      kind 3 (`4-revert-statcoder-sorted.txt`);
    - **`MicroCache`,** `MicroCache.kt:57` (Java `:103`), `<=` to `<`: failed at "n 2 op 0: getAndClear(247) expected
      true but was false", so `MicroCacheTest` bites (`4-revert-microcache-search.txt`);
    - **`DataBuffers`,** `bctx1` given its own array: failed `DataBuffersTest` only, since `TagValueCoder` repoints
      `bctx1` before writing (`4-revert-databuffers-alias.txt`). So a revert can bite in `DataBuffers`, but only in its
      own test.
- **The flag test, arch-aware (`7bf6a3d`) [C: `5-flag-test-x86_64.txt`; R: RECORD -39, `gh run list`]:**
  `theJvmRunsWithLibmIntrinsicsOff` checks the flag of the architecture it runs on. CI run 40980103184 on `7bf6a3d` is
  `success` in both jobs; by the planner's reading of its `ios-smoke` log, the simulator ran 18 of 18
  (**simulator-checked**) and the arm64 JVM step ran 7 tests, 0 failures, gate "OK: all 2 classes ran". So arm64
  HotSpot under `_dcos` builds the same table as x86_64 without the intrinsic: Phase 1 part 6's open question is
  answered for this setting.
- **CI on the step 4 work in progress:** run 40980560784 on `599dd40` is `failure` [R: `gh run list`], as its commit
  message said it would be: the commit does not compile.
- **Unverified:** the twelfth session did not reread the CI logs of 40978865021 or 40980103184; their counts above are
  the planner's reading at RECORD -38 and -39 [R of the record, not of the logs].

## Step 4: the test list (thirteenth session, Linden Caliper)

The twelfth session's handoff items 5 and 6 reached the planner only in its final message; the planner sent them on at
01:0xZ on 2026-10-30, with RECORD -40's correction, and asked for them to be written here [R: the planner's message]:
- **5. Build the contexts as `ProfileCache.java:88-95` does:** the way context with hash size `memoryclass * 512`, read
  from the oracle's `RoutingContext`; the node context with 0; `node.setForeignContext(way)`; then `readMetaData`, then
  `parseFile(profile, "global", keyValues)` for each.
- **6. Tests:** vectors from the oracle's `generateRandomValues(Random(seed))`, some keys forced to 1000+x, encoded with
  the oracle's `encode`; the built-in variables compared by raw bits in both directions, with `getKeyValueDescription`,
  `accessType` and `checkStartWay`; parse checks on the oracle's private `nBuildInVars`, `expressionList` (through its
  `toString`) and `expressionNodeCount`, by reflection; malformed text, exception class and message; the asterisk test,
  corrected by -40: `addLookupValue(name, value, int[])` called directly with a non-null array on both engines, the
  oracle recording the value and the port throwing the named "unsupported". From -39: all 26 profiles in the oracle zip.

As written (jvmTest, `expressions/`): `ExpressionsEngines` (item 5; `memoryclass` and `keyValues` from the oracle's
`RoutingContext` after `setParams` with `OracleCalls`' parameters); `ProfileParseTest` (lookups versions; the three parse
checks, way and node, 26 profiles); `ProfileEvaluateTest` (300 vectors a context a profile, from a third oracle instance,
every third with 1 to 3 keys at 1000 + x below 31,000); `MalformedProfileTest` (26 texts; the oracle throws on 17, all
`IllegalArgumentException` [C], so `parseFile`'s `RuntimeException(e)` branch is not reached by this set);
`AsteriskUnsupportedTest`. The pinned `lookups.dat` has no `*` value, so the asterisk test first adds one to `maxspeed`
through `addLookupValue(name, "*", null)`, the path BRouter reaches; then the oracle records 1350 for "3.5" (`:687`) and
the port must throw `UnsupportedOperationException` with a message starting "unsupported: addLookupValue's asterisk
block". A companion test, beyond item 6, checks that without a `*` both engines record 1 for an unknown value.
- **On the stubs [C: `step4/4-tests-stubs-jvmTest.log`, `step4/5-tests-stubs-junit.txt`]:** 0 compile errors; 75 tests, 6
  failures; each of the six on `NotImplementedError` "step 4: setForeignContext", as predicted at
  `step4/4-tests-prediction.txt`, the 26-profile tests 26 of 26 by that one cause. So every test fails at the shared
  construction path, not at the member it exists for; that member is first reached when the port lands.

## Step 4: the port (fourteenth session, Linden Caliper)

Prediction first, at `step4/6-port-prediction.txt`: 0 compile errors and 75 of 75 passing; a red, if any, most likely a
text case in the malformed set.
- **What landed:** the four drafts moved from `step4/port-draft/` into `commonMain`, replacing their stubs, with one
  change: the asterisk message now starts "unsupported: addLookupValue's asterisk block", the prefix
  `AsteriskUnsupportedTest` asserts. Written from the Java: `BExpressionMetaData` (okio read, `javaReadLines`,
  `javaTrim`, listeners in a `HashMap` as Java's), `BExpressionContextWay` (the 20 getters in Java's index order,
  `accessType`, `setDecodeForbidden`) and `BExpressionContextNode`. The largest file is `BExpressionContext.kt`, 379
  lines. The drafts directory is removed, so there is one copy.
- **Locally [C: `step4/7-port-jvmTest.log`, `step4/8-port-junit.txt`]:** 0 compile errors (`^e: ` in the log); 75
  tests, 0 failures, 0 errors, 0 skipped, in 33 files, read from the JUnit XML after the results folder was deleted
  before the run. The sample: `forEveryProfile` fails unless 26 profiles were checked (`ExpressionsEngines.kt:90`)
  [R], and `MalformedProfileTest` prints "the oracle threw on 17 of 26 texts", as on the stubs [R].
- **Beyond the amendment: `FloatTextCommonTest` (commonTest), for prediction 2.** Without it the simulator runs no
  test that reaches `Float.toString`, since every step 4 test needs the oracle and is JVM-only. It pins Java's strings,
  printed by `java.lang.Float.toString` on OpenJDK 21.0.12 here (`step4/10-floattext-FT.java`), not by Kotlin: `x / 100f`
  for x below 30,000 (the raw values 1000 + x `getKeyValueDescription` formats) as one hash and length, and 19
  literals. Prediction at `step4/9-floattext-prediction.txt`. Locally [C: `step4/11-floattext-jvmTest.log`,
  `step4/12-floattext-junit.txt`]: 0 compile errors, 77 tests, 0 failures. On the JVM it shows only that the values
  were transcribed right; it cannot fail there for a port reason. Never seen failing: it tests the platform, not port
  code, so there is no stub to fail it on.
- **CI for `aa0d918` (run 40983982019) [R: job logs]:** `success`. `jvm`: JVM tests 75, 0 failures, 0 errors, 0
  skipped, in 33 files; iOS klibs `compileKotlinIosArm64` and `compileKotlinIosSimulatorArm64` both ran, build
  successful. `ios-smoke`: `iosSimulatorArm64Test` ran, 21 tests, 0 failures, in 8 files (simulator-checked; commonTest
  only, no step 4 class among them); the arm64 JVM step's outcome `success`, 7 tests, 0 failures, in 2 files; the gate
  "OK: all 2 classes ran".
- **The reverts, both predicted first at `step4/14-reverts-prediction.txt`, run by `step4/13-revert.sh`** (a copy saved
  before editing and restored from, never git; the results folder deleted before each run; the log's `^e: ` count
  checked before any result is read; after each, the file compared to the saved copy and to HEAD). Each ran the
  expressions package, 8 tests in 5 files.
  - **Revert 1, item I's (`max` for `min`, `BExpression.kt:41`) [C: `step4/15-revert1-min-max-jvmTest.log`]:** 0
    compile errors; 2 of 8 failed, as predicted ("1 or 2"). `ProfileEvaluateTest` on 20 of 26 profiles (predicted at
    most 21, the files containing "min"), by raw-bits mismatches on built-in variables 0 (`costfactor`) and 11
    (`maxspeed`); `ProfileParseTest`'s expressions check on 4 of 26, at `way expressionList`, where a constant `min`
    folds to a different number (`...[0].0...` expected, `...[9999].0...` read: min against max of 0 and 9999). Both
    messages are ones this edit can produce. Restored; the file matched HEAD.
  - **Revert 2, the parser (RECORD -41; `e = e.tryCollapse()` removed from `BExpression.parse`, `:183`) [C:
    `step4/16-revert2-no-collapse-jvmTest.log`]:** 0 compile errors; 1 of 8 failed, as predicted:
    `everyProfileParsesToTheOraclesExpressions`, 23 of 26 profiles, every cause at its own member `way
    expressionNodeCount`, the port's count higher (469 expected, 489 read; 659 expected, 1002 read). `ProfileEvaluateTest`
    passed, as predicted: collapse does not change these profiles' values, so only the parse check can see it.
    Restored; the file matched HEAD.
- **CI for `94b769e` (run 40984156792) [R: job logs]:** `success`. `jvm`: 77 tests, 0 failures, in 34 files; both iOS
  klibs compiled. `ios-smoke`: `iosSimulatorArm64Test` ran, 23 tests, 0 failures, in 9 files (simulator-checked); the
  arm64 JVM step `success`, 7 tests, 0 failures, in 2 files; the gate "OK: all 2 classes ran".
- **Prediction 2, `Float.toString`, on the simulator (simulator-checked):** `FloatTextCommonTest` passed there, printing
  the same range hash and length as Java (-1244348794, 195999) and the same 19 literals, `1.0E-4`, `1.0E7`, `NaN`,
  `-0.0` and `0.33333334` among them [R: the ios-smoke counts step's XML]. So Kotlin/Native's `Float.toString` gave
  Java's strings for every value `getKeyValueDescription` formats from a raw value below 31,000, and for these literals.
  Not shown: any other float, `Float.parseFloat` (the profile reader's numbers) and `Short.parseShort` on Native, and
  the `HashMap` order of item 4 there; no step 4 test runs on the simulator.

### Step 4: the handoff (fourteenth session)

Step 4's parts in Amendment 3 Part B, as dispatched to this session, are done: the port (`aa0d918`), prediction 2's
`Float.toString` on the simulator (`94b769e`), and the two reverts (`f03a4c2`). Nothing is partial.
- **Open, for the planner:**
  - the named way getters (`getTurncost` to `getDownhillmaxslopecost`) and the node's `getInitialcost` are not called
    by any test: the tests read `getBuildInVariable(i)` by index, and only `getCostfactor` is reached, through
    `accessType`. Their indices were checked by reading against `BExpressionContextWay.java`, not by a test;
  - `Float.parseFloat` and `Short.parseShort` on Native, and the `HashMap` orders, as above;
  - `parseFile`'s `RuntimeException(e)` branch is still not reached by any test; text decoding, okio against
    `FileReader`, is still unverified for anything but UTF-8;
  - `FloatTextCommonTest` has never been seen failing: it tests the platform, so there is no stub for it.
- **Next:** the planner's call. No step after 4 was dispatched to this session.

## Step 1 pinning and step 1b's start (fifteenth session, Linden Caliper)

Dispatched by Cedar Datum under Amendment 3 Part B after RECORD -45. **Bases [R]:** `brouter-port` at `ae8ec40` on pull, as
the dispatch said; `main` at `44038c8` after fetch. Evidence is in `2026-10-29-brouter-port-phase2/step1-pinning/` and
`step1b/`.

### Pinning (item I, step 1)

- **`tools/tile-pins.tsv`** is now the release's own `tile-pins.tsv`, byte for byte (SHA-256 `ccfc380e…`) **[C]**. Same
  eight columns; its header is the release's shorter one, so the column notes moved to `tools/fetch-tiles.sh`'s header.
- **The build:** a `fetchTiles` Exec task runs `tools/fetch-tiles.sh` against
  `https://github.com/slayer8366/Forager-iOS/releases/download/brouter-tiles-2026-10-30` into `brouter/build/tiles`, and
  `jvmTest` depends on it, passing `brouter.tiles.dir` and `brouter.tiles.pins` (`adc8065`).
- **The test:** `tiles/TilePinsTest` holds the five pins itself, as `OraclePinTest` holds the jar's. It checks the pin file's
  rows and each fetched file's size and SHA-256 against them. `tiles/TestTiles` is the helper later steps read tiles through.
- **Item I's revert, predicted first** (`step1-pinning/0-prediction.txt`): W70_N40's pinned bytes 5292622 → 5292623.
  - **Run A** (fresh `build/tiles`): the fetch refused that tile with the predicted line, "REFUSED W70_N40.rd5 from …: W70_N40.rd5
    is 5292622 bytes, pinned 5292623"; jvmTest never ran **[C]**.
  - **Run B** (`-x fetchTiles`, all five present): TilePinsTest 2 tests, 1 failure, `pinFileIsTheReleasesOwn` on W70_N40's bytes
    alone **[C]**.
  - 0 compile errors in either log. The pin file was restored from its saved copy and equalled HEAD after each run. A forward run
    between A and B: 2 of 2 **[C]**.
- **CI run 41000692626 on `490d0ae`, `success` [R: job logs]:**
  - `jvm`: 79 tests, 0 failures (77 earlier plus these 2); iOS klibs built, no `e:` line.
  - `ios-smoke`: simulator 23 of 23 (**simulator-checked**); arm64 JVM step `success`, 7 of 7, gate "OK: all 2 classes ran".
  - **Both jobs fetched all five files from the release** with `curl` and no credentials (`fetch-pinned.sh` passes none), on
    ubuntu-24.04 and macos-26 **[R]**. This is the anonymous download RECORD -45 left unproven **[I: no token reaches
    that curl; not checked against a request log]**.

### The JDK check (Amendment 3 Part B, "The oracle's JDK")

- **`CheapLogicRunTest`'s CI pass does not show it [R: the test].** `theJvmRunsWithLibmIntrinsicsOff` compares `Math.cos` with
  `StrictMath.cos` inside one JVM. Equality with Phase 1's table would follow only if `StrictMath.cos` gave the same bits on both
  JDKs, which nothing here checks.
- **So `oracle/OracleJdkTableTest` (`fa89911`)** writes Phase 1's `math-nointrinsic.txt` again, in `TableRows.java`'s form, with the
  test JVM's `Math.cos`. It asserts the SHA-256 is Phase 1's `ee004dc0…b379` (`10-tests/logs/table-run.log` at `20e1f92`), and
  that every row's kx and ky equal the oracle's own `SCALE_CACHE` in that process.
- **Its revert, predicted first** (`step1b/0-jdk-check-prediction.txt`): `-XX:-UseLibmIntrinsic` was dropped on this container's
  OpenJDK 21.0.12.1. The SHA-256 was `4ab07de6…`, Phase 1's default-flags table, as predicted. 1 test, 1 failure, 0 compile errors,
  restored and equal to HEAD (`8305898`) **[C]**.
- **CI run 41000916401 on `8305898`, `jvm` [R: job log]:** "oracle JDK: Eclipse Adoptium 21.0.13+9-LTS amd64; 1800 rows;
  no-intrinsic Math.cos table SHA-256 ee004dc0…b379". Passed, so it equals Phase 1's. 80 tests, 0 failures. **Not a stop.**

### E3's positive control

- **Built:**
  - `oracle/e3/TreeBinControl` ports part 7's `TreeBinOrder.java` line for line;
  - `E3Jvm` runs one regime on a new thread, where R4 first asks `identityHashCode` of 997 fresh objects;
  - `E3Launcher` starts it in a fresh JVM with the test JVM's `-XX:` flags and classpath. Output goes to a file, so the 120 s
    timeout applies even to a hung child.
- **`E3ControlTest` (`4be79f0`):** R1 and R4 must differ. Each runs twice. Locally: R1 `4,0,11,1,9,2,3,5,10,6,7,8,` and R4
  `4,2,0,1,3,9,5,6,7,10,8,11,`. **Each regime repeated itself exactly across fresh JVMs** **[C]**.
- **Item I's revert, predicted first** (`step1b/2-control-prediction.txt`): `PERTURBATION` 997 → 0 gave R1's order in all four runs.
  The test failed at its `assertNotEquals`, 0 compile errors, restored and equal to HEAD (`d32f2c3`) **[C]**. The predicted
  message wording was wrong: I guessed it from memory, and kotlin.test prints "Actual:". The value was right.
- **Only R1 and R4 exist.** R2 (repeated in R1's JVM), R3 (the shuffled suite), R5 (with `debug.txt`) and R6 (under JaCoCo) are
  not built.

### Step 1b's state: partial

- **Done:** the JDK check, and the positive control for R1 and R4.
- **Not started:**
  - the case generators and their seeds;
  - any stratum, and so no E3 stability count. **E3's 5% stop has not been tested on any stratum;**
  - the expected digests and R1 times.

### Calls beyond the amendment (mine; for the planner)

- `tile-pins.tsv` taken verbatim rather than keeping the old header, so its SHA-256 can be checked against the release asset.
- The fetch hangs off `jvmTest`, so `ios-smoke`'s arm64 JVM step fetches the 130 MB too. Item I says both jobs fetch; this is how
  `ios-smoke` does it. `iosSimulatorArm64Test` does not depend on the fetch: no common test reads a tile yet.
- `OracleJdkTableTest` is a permanent test, not a one-off run, so a JDK change on CI is caught on every push.
- E3's control uses Kotlin and a fresh-JVM launcher in `jvmTest`, not part 7's Java file.

### Could not determine, and unverified

- **Whether R1's repeatability holds on CI's Temurin.** It was seen only in this container. `E3ControlTest`'s first CI run is on
  `d32f2c3` and was not read by this session unless noted in the final message.
- **Which oracle class reads a tile's nodes** for E4's "start and end drawn on the tile's own nodes": not looked at **[?]**.
- **R6 needs a JaCoCo agent jar.** That is a new pinned download, so its version and SHA-256 are a call to make. Not priced.

### Step 1b: the handoff (fifteenth session)

For the next session, in order:
1. **Read** this section, E3 to E5 (Phase 1 report at `20e1f92`, lines 806-965), and item I's 1b row (line 1693).
2. **Extend `E3Jvm`** from the control alone to a case list, adding:
   - R2 (a second new thread in R1's JVM);
   - R3 (one JVM, the whole list, a fixed shuffle seed in source);
   - R5 (`debug.txt` in the folder above the profile folder: `RoutingEngine.java:111-121`).
   R6 waits for the planner's call on JaCoCo. Keep each fresh JVM's control line in every regime.
3. **First stratum: F0** (2 cases, Forager's two routes and profile on `TestTiles.f0`). It needs no generator, so it brings
   the harness up end to end. **Then R on one profile:** the generator with its seed, air distance log-uniform from 100 m to
   100 km, ends moved 0 to 100 m, six decimals.
4. **Report each stratum's instability count against E3's 5% stop.** More than 5% unstable is a stop: report it to the
   planner and do not go on.

## Step 1b continued: the harness, F0 and R on one profile (sixteenth session, Linden Caliper)

Dispatched by Cedar Datum under Amendment 3 Part B after RECORD -46. **Bases [R]:** `brouter-port` at `f156946` on pull, as the
dispatch said; `main` at `b723190` after fetch. Evidence is in `2026-10-29-brouter-port-phase2/step1b/`, files 3 to 7.
Predictions were written before each run (`3-strata-prediction.txt`, `7-comparator-bite-prediction.txt`).

### What was built (`8eea3fc`, `525448b`)

- **`E3Jvm` runs a case list** under every regime. Each regime's thread prints its own control line before its case:
  - `R1R2`: one fresh JVM. R1 runs with no `debug.txt`; then `debug.txt` is created and R2 runs the case again on a new thread.
  - `R3`: one JVM, the stratum's whole list, one thread, shuffled by `R3_SEED` (20261030) in source, with `debug.txt`.
  - `R4`: 997 identity hashes, then the control, then the case, with `debug.txt`. `R5`: the same without the perturbation.
  - `R6`: no `debug.txt`, under JaCoCo's agent (`includes=btools.*`), one `.exec` per case.
- **`E3CaseRun`** drives the oracle through Forager's calls (E1), and reads the collection lines (`RoutingEngine.java:2025`, `:2031`)
  the run appended to `debug.txt`. Every JVM gets its own copy of `profiles2/`, so no `debug.txt` reaches another run.
- **`E3Stability`** compares, per case, the six tuples (kind, node count, cost, distance, FNV-1a over every `(ilon, ilat)`) and
  the four logged runs' collection lines, and holds the stratum to the 5% stop. The digest stands in for pair-by-pair
  comparison up to a 2^-64 collision; it is E3's comparison only, not the stored form's digest.
- **`TileNodes`** reads a tile's nodes with the oracle's own reader: `PhysicalFile`, `OsmFile`, `createMicroCache(…, true, …)`,
  as step 1's `TileCheck` decoded them, but with absolute cell indices so a node id is `ilon << 32 | ilat`. This answers step
  1b's open question "which oracle class reads a tile's nodes" **[C: the R run's ends routed on all four tiles]**.
- **`RGenerator`**: one `java.util.Random(seed)`; a tile uniform over the four, a start uniform over its nodes; air distance
  `100 m × 1000^u`; the end is the node nearest the target in its 3 × 3 cells; each end moved `100 m × v` and rounded to six
  decimals. Rejections are counted. Its case list (coordinates) stays in `build/e3`, never committed (E1).
- **The `e3Strata` task.** E1 [FBD]: "the expensive runs are not per push". `jvmTest` now excludes `oracle.e3.strata.*`, and
  `e3Strata` runs only them, with the same JVM flags and properties. **CI does not run it.**
- **R6's agent (RECORD -46):** `org.jacoco.agent` **0.8.16**, classifier `runtime`, from
  `https://repo1.maven.org/maven2/org/jacoco/org.jacoco.agent/maven-metadata.xml`, whose `<release>` and `<latest>` were 0.8.16
  on 2026-10-30 (`lastUpdated` 20261026104303; `5-jacoco-maven-metadata.xml`) **[R]**. Pinned in `tools/fetch-jacoco.sh`:
  314,470 bytes, SHA-256 `37ae2f374ad77e85c59c6e5b46565226c3ac3cb460ac19de0bcb844ac2f43e33` **[C: computed]**. Central has no
  `.sha256` for it (404); the jar's SHA-1 `4325ec32…3ce` and MD5 equal Central's `.sha1` and `.md5` **[C]**.
  - Central answered several fetches here with **429**. The task's own fetch failed on it once; the jar already downloaded
    and checked was placed at the task's destination, where `fetch-pinned.sh` accepted it against the pin.
  - The agent did instrument the oracle: F0's `.exec` names 57 `btools` classes, `RoutingEngine` among them **[C]**.

### Results, locally (OpenJDK 21.0.12.1, amd64, 4 cores) [C]

| Stratum | n (E5) | Unstable | Over 5%? | R1 kinds | `e3Strata` wall time |
|---|---|---|---|---|---|
| **F0** | 2 (2) | **0 of 2** | no | 2 routes | about 11 s |
| **R, `hiking-mountain`** | 59 (59) | **0 of 59** | no | 59 routes | about 3 min 13 s |

- **F0's reach evidence holds:** R1 gives 226 nodes and 5,047 m, and 306 nodes and 6,713 m, as Forager's geojson. The costs, 5,302
  and 7,405, also equal the geojson's, as predicted at low confidence. No collection line on F0: its log comparison is vacuous.
- **R:** 59 cases from 67 candidates (cap 1,180); 8 rejected for no node near the target. Starts: 16 in `E170_S50`, the rest in
  the three northern tiles. **No case crosses 40°N or 70°W** **[C]**, so R's "across a 5° line" is not exercised by this seed;
  DL-lat and DL-lon are built for it. **8 of 59 cases logged collections** (up to 67 lines), so the log comparison was not
  vacuous there. R1 times: 78.5 s in all, 19.3 s the longest.
- **The control moved in every JVM:** R1's order differed from R4's for every case of both strata.
- **No stop.** Both strata are under E3's 5%. Prediction 3 (0 of 59, low confidence) held.

### The comparator bites (`7-comparator-bite-prediction.txt`) [C]

Each mutation in `E3Jvm.report`, restored from a copy saved before editing and then equal to HEAD; 0 `e:` lines in both logs:
- **A, the tuple:** R5 routed to an end 0.0001° north. F0: 2 of 2 unstable, tuple-stable false and log-stable true on both;
  failed with "F0: 2 of 2 unstable, over E3's 5%: a stop for Phase 2", as predicted (`7a-mutation-tuple-f0.log`).
- **B, the log:** R5's collection lines dropped. R: exactly the 8 predicted cases unstable, each tuple-stable and log-unstable;
  "R-hiking-mountain: 8 of 59 unstable, over E3's 5%: a stop for Phase 2", as predicted (`7b-mutation-log-r.log`). The same 59 cases were generated.

### CI [R: job logs]

- **Run 41002963294 on `8eea3fc` and run 41003465269 on `525448b`: both `success`.** On `525448b`, `jvm`: "TOTAL: tests 81,
  failures 0, errors 0, skipped 0 in 37 files", the same 81 as `d32f2c3`, so `jvmTest` ran no stratum test, as intended. `ios-smoke`
  `success` by its job and step conclusions; its counts not read.
- **On CI's Temurin, E3ControlTest's R1 and R4 each repeated themselves across fresh JVMs** ("R1 repeats across fresh JVMs: true;
  R4: true"), with the changed launcher. The control only; no case has run on CI.

### Calls beyond the amendment (mine; for the planner)

- **The strata run in a new `e3Strata` task, outside CI,** on E1's "not per push". R1's repeatability on CI's Temurin is
  therefore still unseen for cases (E3ControlTest shows it for the control only).
- **R3 runs per stratum,** not over one list of every stratum: only two strata exist. When more do, R3 should take the union.
- **The generator's details** (tile uniform, then node uniform; nearest node to the target; rejection rules; spherical metres)
  are mine; E4 fixes the distance law, the move and the six decimals only.
- **`hiking-mountain` first,** as Forager's own profile.
- **Every regime's thread runs the control before its case,** so the control is part of each regime's identity-hash history.

### Could not determine, and unverified

- **Whether these strata are stable on CI's Temurin, or on aarch64:** not run there **[?]**.
- **E3's stop on R's other 25 profiles,** and every targeted stratum: not run.
- **The `.exec` files are not analysed;** R and F0 need no reach evidence (E4). Nothing reads coverage yet.
- **Expected values were not written,** though both strata are shown stable here: the stored form needs part 7 step 4's digest,
  not read this session.

### Step 1b: the handoff (sixteenth session)

1. **Read** this section, and E1, E3 to E5 as before.
2. **R on the other 25 profiles,** one test per profile or one parameterised over them, each with its own seed and 59 cases.
   About 3 min a profile here, so about 80 min for all 25 at 3 workers: run it as `e3Strata`, not on CI.
3. **Report each against the 5% stop;** more than 5% is a stop for the owner.
4. **Then DL-lat and DL-lon,** which need a generator forcing ends to opposite sides; then MP64, ML5, PM5.
5. **Expected values** once a stratum is stable: read part 7 step 4's digest first.

## Step 1b continued: the oracle's JDK, and R on all 26 profiles (seventeenth session, Linden Caliper)

Dispatched by Cedar Datum under Amendment 3 Part B after RECORD -47. **Bases [R]:** `brouter-port` at `0265239` on pull, as the
dispatch said; `main` at `699125d` after fetch. Evidence is in `2026-10-29-brouter-port-phase2/step1b/`, files 8 to 10. The
predictions (`8-temurin-prediction.txt`) were pushed in `8cf8be4`, before any run on Temurin.

### The oracle's JDK (`970df90`)

- **Source [R]:** Adoptium's GitHub release `adoptium/temurin21-binaries`, tag `jdk-21.0.13+9`, asset
  `OpenJDK21U-jdk_x64_linux_hotspot_21.0.13_9.tar.gz`. `api.adoptium.net/v3/assets/release_name/eclipse/jdk-21.0.13%2B9`
  (linux, x64, jdk, hotspot, normal heap) names that link, release timestamp 2026-10-22T08:58:06Z.
- **Pin:** 207,858,352 bytes, SHA-256 `d7f5d5cf865a2902b70ff549b97586a7d3f234e016f041ab447ea740c7d66d1c`. That is Adoptium's
  published checksum, read from two of its own sources that agree: the release's `.sha256.txt` and the API's
  `package.checksum` and `package.size` **[R]**. The SHA-256 computed from the download equals both **[C]**. Adoptium's
  GPG signature was **not** checked.
- **`tools/fetch-temurin.sh <dir>`** fetches it through `fetch-pinned.sh` (size, then SHA-256), extracts it fresh, and refuses
  it unless its `release` file reads `IMPLEMENTOR="Eclipse Adoptium"` and `JAVA_RUNTIME_VERSION="21.0.13+9-LTS"` **[C]**. The
  JDK was fetched into the session's scratch directory; nothing is committed.
- **`e3Strata` runs on it only:** `-Pbrouter.e3.jdk=<home>` sets the test JVM's `executable`. Without it the task refuses
  before any case runs ("e3Strata runs on the oracle's JDK only (RECORD -47)…") **[C: run]**. Every regime's JVM starts from
  the test JVM's `java.home` (`E3Launcher.kt:32`) **[R]**. During the `hiking-mountain` run, `ps` showed R1R2, R3 and R4 JVMs
  started as Temurin's `bin/java -XX:+UnlockDiagnosticVMOptions -XX:-UseLibmIntrinsic` **[C]**. R5 and R6 were not in that
  snapshot; they use the same launcher **[R]**.
- **The head of every stratum's log** (`E3Strata.jdk`) prints `java.vendor`, `java.runtime.version`, `os.arch`,
  `java.vm.name` and `java.home`. It refuses any JDK other than Eclipse Adoptium 21.0.13+9-LTS amd64. All 27 stratum logs here
  read `java.vendor=Eclipse Adoptium; java.runtime.version=21.0.13+9-LTS; os.arch=amd64` **[C]**.
- **R's body moved to `E3R`, unchanged**, so the 25 profiles share it. `E3RTest` keeps `hiking-mountain`'s seed 20261030.
  `E3RProfilesTest` holds the other 25 profiles' seeds, 20261101 to 20261125 in alphabetical order. They were committed and
  pushed in `970df90`, before any run of them. The test checks that the 26 profiles are the oracle's `profiles2/*.brf`.

### F0 and R on `hiking-mountain`, re-run on Temurin (`2ada78c`) [C]

| Stratum | JDK | Unstable | Over 5%? | Earlier (OpenJDK 21.0.12.1, `525448b`) |
|---|---|---|---|---|
| **F0** | Temurin 21.0.13+9 amd64 | **0 of 2** | no | 0 of 2 |
| **R, `hiking-mountain`** | Temurin 21.0.13+9 amd64 | **0 of 59** | no | 0 of 59 |

- **Same cases, same answers.** R generated the same 59 case ids from 67 candidates (8 rejected). **Every R1 tuple equals
  the OpenJDK run's: 59 of 59, and F0's 2 of 2** (`9-*-stability.tsv` against `6-` and `4-`). The control moved in every JVM.
  Predictions 1 to 5 held.
- **The second gap closes for this stratum:** the generator was pushed (`525448b`, `970df90`) before this run.

### R on the other 25 profiles (`10-r-25-profiles-temurin-*`) [C]

All on Temurin 21.0.13+9 amd64. The profiles ran **one after another in one `e3Strata` run** (04:48:18Z to 06:00:53Z, 1 h 12 min),
3 regime JVMs at a time. **Not in parallel:** see the calls below. Each stratum has n = 59 (E5), with 61 to 71 candidates, and
none is short. The control moved in all 1,475 cases.

| Profile | Unstable (of 59) | Routes (R1) | Logged cases |
|---|---|---|---|
| car-eco / car-eco-nocost | 0 / 0 | 51 / 50 | 4 / 3 |
| car-fast / car-fast-nocost | 0 / 0 | 54 / 52 | 3 / 6 |
| car-vario / car-vario-nocost | 0 / 0 | 50 / 43 | 7 / 5 |
| fastbike / -asia-pacific / -lowtraffic / -verylowtraffic | 0 / 0 / 0 / 0 | 53 / 55 / 57 / 57 | 10 / 8 / 4 / 18 |
| gravel / moped / mtb | 0 / 0 / 0 | 56 / 48 / 54 | 14 / 6 / 8 |
| rail / river | 0 / 0 | 2 / 2 | 0 / 0 |
| safety / **shortest** / skating | 0 / **1** / 0 | 54 / 55 / 43 | 7 / 6 / 3 |
| trekking / -ignore-cr / -noferries / -nosteps / -steep | 0 / 0 / 0 / 0 / 0 | 52 / 55 / 53 / 54 / 57 | 5 / 12 / 7 / 13 / 9 |
| vm-forum-liegerad-schnell / vm-forum-velomobil-schnell | 0 / 0 | 51 / 51 | 8 / 5 |

- **No profile passes E3's 5%: no stop.** The highest is `shortest`, at 1 of 59 (1.7%). Over all 26 profiles, R is **1 of
  1,534** unstable.
- **The one unstable case, `R-shortest-025`:** R1 to R5 agree on `route|217|5102|5102|5413e0b4fd811f2e`, and the logged runs
  agree on one collection ("collected, nodes/paths before=48326/395 after=19174/303 maxTotalCost=5194"). **R6, under JaCoCo's
  agent, routed `route|188|5103|5103|a72dd9fe13d1c2c1`:** a different route 1 m longer. The kinds agree in all six. Under E3 it
  is kept out of every exact assertion and compared on kind only. **Why R6 differed was not determined [?].**
  - **[I]** `shortest`'s cost equals its distance, so two routes 1 m apart are a near-tie. A collection ran at maxTotalCost
    5,194 in the logged runs. R6 has no `debug.txt`, so whether it collected, or when, is not visible.
  - **This also bears on E3's own rule:** the coverage run must give R1's tuple. For this case it did not, so its `.exec` does
    not describe the expected-value run.
- **Kinds:** non-routes are the oracle's own errors: "to/from-position not mapped", "target island", "no track found",
  "datafile … not found" for tiles not captured, and once "error re-tracking track" (car-fast-nocost). `rail` and `river` give
  52 and 53 "from-position not mapped" respectively, as predicted.
- **Crossings:** 25 cases have ends on opposite sides of 40°N, and 17 of 70°W, in 21 of the 25 profiles. These are ends only,
  not routes **[C: `10-…-summary.tsv`]**.
- **Prediction 6 partly failed:** "most likely 0 unstable in each" was wrong for `shortest`. "No profile over 5%", "none short"
  and "rail and river mostly non-route" held.

### CI [R: job logs]

- **Run 41004546970 on `970df90`: `success`.** `jvm`: "TOTAL: tests 81, failures 0, errors 0, skipped 0 in 37 files", the same
  81 as before, so no stratum ran there. E3ControlTest: "R1 repeats across fresh JVMs: true; R4: true". `ios-smoke`
  `success` (simulator-checked).
- **Runs on `8cf8be4`, `2ada78c` and the planner's `9f41799`:** `success` by their run conclusions, logs not read. The run on
  this merge, `83cba83`, was in progress at handoff and was not read.

### Calls beyond the amendment (mine; for the planner)

- **The JDK is given to `e3Strata` by a property, not fetched by a Gradle task,** because the dispatch said "into scratch".
  The task refuses without the property, and the run refuses any other JDK.
- **The in-run check includes the arch, amd64,** as RECORD -47 names it. The libm flag is still chosen from Gradle's own JVM's
  `os.arch` (`build.gradle.kts`), which is amd64 here, as Temurin's is.
- **The seeds, 20261101 to 20261125 in alphabetical order, are mine.** E4 fixes only that each seed is in the source.
- **One test runs the 25 in sequence, and stops at the first profile over 5%.**
- **The planner allowed up to 3 profiles at a time during the run. I did not take it.** The one run already held the machine
  at load about 7 on 4 cores, 2% idle **[C: `/proc/loadavg`, `top`, at profile 15]**. More profiles at once would not have
  finished sooner, and would have taken the core left for the second coder. The run was not restarted.
- **`R-shortest-025` is recorded, not investigated:** under the stop, and outside this dispatch.
- **Commit trailers follow the dispatch** (`Co-Authored-By: Claude <noreply@anthropic.com>`, no model name), as this branch's
  history does.

### Could not determine, and unverified

- **Why R6 gave `R-shortest-025` another route [?].** Whether R1 repeats it on CI's Temurin, or on aarch64: no case has run
  there.
- **Whether `shortest`'s instability is Temurin's:** `shortest` never ran on OpenJDK 21.0.12.1 **[?]**.
- **Adoptium's GPG signature on the tarball:** not checked. The pin rests on Adoptium's two published checksums.
- **The `.exec` files are not analysed;** no expected value is written (dispatch).

### Step 1b: the handoff (seventeenth session)

1. **Read** this section and RECORD -47. Run `tools/fetch-temurin.sh <scratch>`, then
   `./gradlew :brouter:e3Strata -Pbrouter.e3.jdk=<scratch>/jdk-21.0.13+9 --tests <class>`. Filter by class: the 25-profile run
   alone takes about 73 min.
2. **R is done on all 26 profiles:** 1 of 1,534 unstable, `R-shortest-025`. The planner may want R6's disagreement looked at
   before any coverage is read as reach evidence.
3. **Next, in the planner's order:** DL-lat and DL-lon (a generator forcing ends to opposite sides; seeds pushed before any
   run), then MP64, ML5, PM5. Each through `E3Strata.jdk` and `E3Strata.report`.
4. **Expected values** once a stratum is stable: read part 7 step 4's digest first.

## Step 1b continued: RECORD -50's rulings, the planner's rulings on E4's targeted strata, and DL's generator (eighteenth session, Linden Caliper): PARTIAL

Dispatched by Cedar Datum under Amendment 3 Part B after RECORD -50. **Bases [R]:** `brouter-port` at `db3b133` after fetch, as
the dispatch said. **Handed off at the planner's request (07:41Z) before any run.** No stratum ran this session.

### RECORD -50's two rulings, as they apply here

- **`R-shortest-025` is unstable under E3's rule** (Phase 1 report at `20e1f92`, line 831). It is counted and named (1 of 59 in
  `shortest`, 1 of 1,534 in R), kept out of every exact assertion, and compared on kind only: all six runs agree on kind `route`.
  When expected values are written, it is marked unstable.
- **Coverage counts as reach evidence only for a case whose R6 tuple equals its R1 tuple.** **No code reads coverage today
  [R: `git grep` for jacoco, `.exec` and coverage]:** R6 writes one `.exec` per case (`E3Stability.kt:45,52`), and nothing parses
  one. So no code was changed for this rule. Its first reader will be NT's or CR's selector, which applies it, seen failing first
  (the planner, ruling below).

### The planner's rulings on E4's targeted strata (2026-10-30, to be recorded as RECORD -51)

I asked two questions: whether E4's reach column selects cases or counts them over the first 59, and which profile these strata
use. While DL was being built, I reported two more things: R1 has no log, and one run can show both panic mode and the limit
(`RoutingEngine.java:1739-1749`, `:1796-1799`, `:1893`, `:1997-1998`, `:2026-2031` at v1.7.10 **[R]**; whether data does is
not known).

**First answer (Q1, Q2), the planner, verbatim in its operative parts:**
- Q1: "(b), selected. ... each stratum is the first 59 candidates, in generation order, whose reach evidence holds. ... The rest
  are rejected candidates, counted;
- ML5 and PM5 share one candidate sequence, MP64's generator with memoryclass 5. ...
- the cap is 20 × 59 = 1,180 candidates per stratum. For the shared ML5/PM5 sequence, the cap is 1,180 for each stratum. A
  stratum still not full is reported short, with its n, and never filled from another generator;
- E3's six runs and the instability count are then taken over the selected 59, as for R. ...
- Where reach is coverage (NT's comparator line, CR's StdPath lines), the selector is the first code that reads coverage.
  RECORD -50's rule applies there: a case counts only if its R6 tuple equals its R1 tuple. Seen failing first when you build it."
  (The planner corrected its own citation: "Cases are kept in generation order" is line 885, not 889.)
- Q2: "hiking-mountain, your default, for DL-lat, DL-lon, MP64, ML5 and PM5. ... Rejected: the uniform draw. Say in the report
  that hiking-mountain switches off turn restrictions, so these strata do not exercise them; R does." **So stated:**
  `hiking-mountain` switches off turn restrictions (Phase 1, E4: 24 of 26 profiles switch them on, all but `hiking-mountain` and
  `shortest`), so DL-lat, DL-lon, MP64, ML5 and PM5 do not exercise them. R does.

**Ruling 1, the reach source, verbatim:** "You are right that R1 has no log; my "R1's log" was a wrong premise, and I will record
it. Option (i): R2, the same case at once in R1's own JVM with debug.txt. The selection pass is E3's R1R2 JVM run over the
candidates, in generation order. A candidate's reach counts only if R2's tuple equals R1's, the same rule as RECORD -50's for
coverage. R3 to R6 then run on the selected 59 only. ML5's mark is R1's kind; MP64's and PM5's marks are R2's log lines (:2025,
:2031). Rejected: (ii) R5, which reads reach after selection should already be done, and (iii) a separate run, which costs a JVM
per candidate for nothing (i) lacks."

**Ruling 2, a run that shows both panic mode and "memory limit reached", verbatim:** "(a), ML5 by its outcome. The strata stay
disjoint, and the outcome decides. ML5 = the run's kind is "memory limit reached", whatever came before. PM5 = R2's log shows
panic mode enabled, and the kind is not the limit. Count and report how many ML5 cases also showed panic. Rejected: (c) both,
which puts one case in two strata and breaks E5's per-stratum sizes; (d) neither, which drops a real path; (b) PM5, which would
put a fixed-message outcome into the stratum J predicts by collections."

### What was written (work in progress, uncompiled, never run)

- **`DLGenerator.kt` (new):** the geometric candidate sequence for DL-lat (40°N: `W75_N35` below, `W75_N40` above) and DL-lon
  (70°W: `W75_N40` west, `W70_N40` east). Per candidate, from one `java.util.Random(seed)`:
  - the start's side, uniform;
  - R's distance law, `100 m × 1000^u`;
  - a start node, uniform over the start tile's nodes in the rows (columns) of cells within that distance of the line;
  - a uniform bearing, and a target, rejected unless it lies inside the other tile;
  - the end, the nearest node to the target in its 3 × 3 cells in the other tile;
  - both ends moved 0 to 100 m and rounded to six decimals, then rejected unless each is still on its own side.

  Its details are my calls; E4 fixes the line, the tiles, node-drawn ends, the move and the six decimals. **It was written before
  ruling 1 and still numbers kept cases `DL-lat-001`…**: under selection, ids should carry the candidate's draw number, so the
  count to the 59th selected case can be read from them.
- **`E3CaseRun`:** `Result.extent`, a route's `minIlon, maxIlon, minIlat, maxIlat` (null when there is no route). This is DL's reach:
  nodes on both sides is `min < line <= max`, with the line at ilat 130,000,000 or ilon 110,000,000, and a node on the line counted
  in the tile above it, as the tiles hold it.
- **`E3Jvm`:** prints `extent <regime> <id> …` for a route, and has an `R1`-alone single-case regime. **Under ruling 1 the `R1`
  regime is not needed:** selection is the `R1R2` JVM. The next session should remove it rather than build on it.
- **Not written:** the seeds, the DL test class, the selector, and MP64's, ML5's and PM5's generators. **Nothing was pushed
  before a run, because nothing ran.**

### Could not determine, and unverified

- Whether `DLGenerator` compiles, and what it generates: not compiled, not run.
- Whether any real run shows both panic mode and the limit: code reading only.
- **My slip:** `tools/fetch-temurin.sh` is committed `100644`, not executable. The seventeenth handoff said to run it directly;
  `bash tools/fetch-temurin.sh <dir>` works. This session re-checked the JDK through it: present, 207,858,352 bytes, SHA-256
  `d7f5d5cf…66d1c`, re-extracted fresh.

### Step 1b: the handoff (eighteenth session)

1. **Read** this section, RECORD -50 and -51 (when written), and E4 (Phase 1 at `20e1f92`, lines 839-932). Then
   `bash tools/fetch-temurin.sh <scratch>` and `./gradlew :brouter:e3Strata -Pbrouter.e3.jdk=<scratch>/jdk-21.0.13+9 --tests <class>`.
2. **Finish the selector (`E3Select`, new file) under ruling 1:**
   - run the `R1R2` JVM over each stratum's candidates in generation order (parallel is fine; keep order when taking);
   - a candidate's reach counts only if R2's tuple equals R1's;
   - keep the first 59 that reach; the rest are rejected and counted; cap 1,180 per stratum; short is reported, never filled;
   - write each candidate's row (id, draw number, R1 and R2 tuples, extent, reach) to a TSV;
   - then run R3 to R6 on the 59. `E3Stability` currently runs all six itself. Either feed it the selection's R1R2 output, or
     re-run R1R2 and also record that the selection's R1 equals E3's.
3. **Before any run:**
   - drop the `R1` regime;
   - give DL ids their draw numbers;
   - write `E3DLTest` (two tests) with the seeds. My proposal is 20261201 (DL-lat) and 20261202 (DL-lon). Not yet fixed;
   - assert in the test, before routing, that every case's ends are in the two tiles, one each;
   - **commit and push. Only then run.**
4. **MP64, ML5 and PM5,** as ruled:
   - MP64 is R's generator in `W75_N40`, 20 to 100 km, `memoryclass` 64; reach is at least one "collected" line in R2's log (`:2025`).
     `RGenerator` must not change R's sequence; check that it does not, against a list saved before any edit.
   - ML5 and PM5 are one sequence at `memoryclass` 5:
     - ML5 is R1's kind "memory limit reached";
     - PM5 is panic mode in R2's log (`:2031`), with a kind other than the limit;
     - count the ML5 cases that also showed panic.
   - Seeds pushed before runs.
5. **Then** E3's 5% stop per stratum, the expected values (part 7 step 4's digest form, with unstable cases marked, including
   `R-shortest-025`), and 1b's milestone report: total against 2,118. NT, LB, CR and FK are not built.

## Step 1b continued: the selector, DL's, MP64's, ML5's and PM5's tests, seeds pushed; no stratum run (nineteenth session, Linden Caliper): PARTIAL

Dispatched by Cedar Datum after RECORD -52. **Base [R]:** `brouter-port` at `fa54097` after fetch, as the dispatch said.
**Stopped at the planner's request (07:58Z, the owner's call to stop the team) with no run underway.** No stratum ran.

### `fa54097` first [C]

- **It compiles:** `./gradlew :brouter:compileTestKotlinJvm` green locally before any edit.
- **CI on `fa54097`, run 41021809372:** completed, conclusion success (both jobs) [R: the run's job list]. Counts not read from
  its log; the counts below are `85008b1`'s.

### What was built (`437e047`, `85008b1`)

- **R's sequence pinned before `RGenerator` was edited (`437e047`).** `E3RSequenceTest` (strata package, so `e3Strata`
  only) generates R's 26 lists exactly as `E3R` does and holds the SHA-256 of their lines (id, the four ends, profile,
  `memoryclass`, time limit; not the tile folder) to `ed378c85…e43d6c`.
  - **Before the edit [C]:** 1,534 cases, that digest; the test failed on its placeholder, as predicted, and printed it
    (`step1b/11-r-sequence-before-edit-e3Strata.log`). The list is saved whole: `step1b/11-r-sequence-before-edit.tsv`, whose
    `sha256sum` is the same value.
  - **After the edit [C]:** the test passed with the same digest, and `cmp` of the new list against the saved one found them
    byte-identical (`step1b/11-r-sequence-after-edit-e3Strata.log`). Both on Temurin 21.0.13+9.
  - **Not seen to bite on a changed sequence [stated]:** no revert was run. What stands outside the check is the `cmp` of the
    whole list against the copy saved before the edit.
- **`E3Select` (new):** the selector under RECORD -51 and -52.
  - Each candidate runs in E3's own `R1R2` JVM (`E3Stability.launch`, now `internal`), as many at once as `E3Strata.workers` (cores - 1), and is taken
    strictly in draw order.
  - Reach counts only when R2's tuple equals R1's; otherwise the candidate is rejected as "R2's tuple differs from R1's".
  - The first 59 per stratum are taken; a candidate for a full stratum of a shared sequence is "already full", counted.
  - **The cap is 1,180 draws per sequence,** a draw rejected before routing counting toward it, as R's generator counts its
    candidates (`RGenerator.generate`). **A call (mine):** the alternative, 1,180 routed candidates, is looser.
  - Draws still running when every stratum is full are neither counted nor written; the pool is shut down without interrupting,
    so no child JVM is left unwatched.
  - Every counted draw is a row of `<work>/<label>-select/select.tsv`: draw, id, R1 and R2 tuples, R2 = R1, R1's extent,
    R2's collected lines, R2's panic line, R1's ms, `memoryclass`, the reach reading, the result. A check holds rows to draws.
  - `E3Select.PANIC` is "enabled memory panic mode": the panic line at `:2031` **also contains "memory limit reached"**
    [R: `RoutingEngine.java:2031` at v1.7.10], so the two are told apart by that part. "memory limit reached" is thrown at one
    site only, `:2028` [R: grep].
- **`E3Targeted` (new, strata):** `hiking-mountain`, n 59, cap 1,180, 600 s per JVM. After selection it runs E3's six regimes
  on the taken cases with `E3Stability.run` unchanged, which **re-runs R1R2** (the handoff's second option), and compares each
  case's selection R1 tuple and R2 lines with E3's, printing every difference and the count of agreements. All strata of one
  sequence are reported before the first E3 failure is thrown; a short stratum fails after E3 has run on what it has.
- **`DLGenerator`:** `generate` replaced by `draws`, an endless numbered sequence; draw 7 is `DL-lat-0007`.
- **`E3DLTest` (new, two tests):** seeds **20261201** (DL-lat) and **20261202** (DL-lon). Before a case is routed, it asserts
  that the case's ends lie in the two tiles, one in each, by tile names computed from the degrees, independent of the
  generator's cell test. Reach: R1's extent has `min < line <= max`, with the line at ilat 130,000,000 or ilon 110,000,000.
- **`RGenerator`:** optional `only` (one tile), `minMetres` and `ratio` (distance `minMetres × ratio^u`); the defaults are R's
  expression unchanged. `draws(prefix, profile, memoryclass, …)` beside `generate`.
- **`E3MemoryTest` (new, two tests):**
  - **MP64,** seed **20261203:** `W75_N40` only, both ends in it, 20 to 100 km log-uniform (`20 km × 5^u`), `memoryclass` 64.
    Reach: at least one "collected" line in R2. Taken cases with the limit or panic mode are counted and printed (E4).
  - **ML5 and PM5,** one sequence, seed **20261204,** `memoryclass` 5, ids `M5-nnnn`. ML5: R1's kind is "memory limit
    reached". PM5: R2 shows the panic line and the kind is not the limit. Neither: rejected. ML5 cases that also showed panic
    are counted.
  - **Calls (mine):** "in `W75_N40`" read as both ends in that tile (the route may leave it), and "20 to 100 km" read as R's
    log-uniform law over that range.
- **`E3Jvm`:** the `R1` regime is dropped, as the eighteenth handoff said.

### CI [R: job logs]

- **`437e047`, run 41022631752:** success.
- **`85008b1`, run 41023085025:** success. `jvm` TOTAL tests 91, failures 0, in 41 files; `ios-smoke` 23, failures 0
  (simulator-checked); the arm64 JVM step 7, failures 0.
- **These counts equal 5a's (91, 23, 7) and say nothing about the new code beyond compiling:** every new test is in the strata
  package, which `jvmTest` excludes.

### Mechanism predictions, written before any run [I unless marked]

- **DL-lat:** fills 59 within the cap. About half of all draws are rejected before routing (a uniform bearing points away
  from the line about half the time). Land on both sides of 40°N in these tiles is New Jersey, roughly 75°W to 74°W [I:
  geography]. Some routed candidates fail with no route.
- **DL-lon:** fills, with more rejects before routing than DL-lat (Maine and Cape Cod only).
- **MP64:** most routed candidates collect at least once (20 km and more on `hiking-mountain` in the New York area); it fills
  early. 0 limit and 0 panic at `memoryclass` 64.
- **ML5 and PM5** [?]: most routed candidates reach a collection. In pass 0, `maxTotalCost` is 1e9, so the first out-of-bounds
  collection enables panic; a later pass with a bound throws the limit (`:2027-2028`). So ML5 should hold many cases that
  also showed panic, and **PM5 is the one that may come up short.**
- **E3:** 0 unstable in each stratum (Phase 1's prediction; R missed it by 1 of 1,534).

### Could not determine, and unverified

- **Not run:** the selector, DL's, MP64's and the ML5/PM5 tests. Nothing about what they select, or how long they take, is
  known. A 20 to 100 km `hiking-mountain` run may take tens of seconds; R's 59 on that profile took 3 minutes in all.
- **`E3Select` has no unit test of its own** (draw order, the cap, a full stratum in a shared sequence). Its one internal
  check is rows against counted draws. The first run's `select.tsv` is the evidence to read: draw numbers 1 to n with no gap,
  taken ids in rising order, and rejected counts summing to draws minus taken.
- `E3RSequenceTest` has not been seen to fail on a changed sequence (above).

### Step 1b: the handoff (nineteenth session)

1. **Read** this section, RECORD -51 and -52, and E4 (Phase 1 at `20e1f92`, lines 839-932). Base: `brouter-port` at the
   commit carrying this section or later; fetch first.
2. **The JDK:** `bash tools/fetch-temurin.sh <scratch>` (now executable too). It prints the home to pass below.
3. **Run DL, one test at a time, nothing edited first** (the seeds are pushed in `85008b1`):
   `./gradlew :brouter:e3Strata -Pbrouter.e3.jdk=<home> --tests '*E3DLTest.dlLat*' --console=plain > <scratch>/12-dl-lat.log 2>&1`,
   then `--tests '*E3DLTest.dlLon*'` into `12-dl-lon.log`.
   - **Before citing anything:** the log has no `e: ` line; its JDK line is Temurin 21.0.13+9; `select.tsv` passes the checks
     under "Could not determine" above.
   - Copy into `step1b/`: the log, `brouter/build/e3/<stratum>-select/select.tsv`, and `brouter/build/e3/<stratum>/stability.tsv`.
   - Record per stratum: draws, routed, rejected by reason, taken (59 or short), the last draw taken, unstable of 59, and
     selection-vs-E3 agreements. Compare with the predictions above. **More than 5% unstable (over 2 of 59) is a stop.**
   - Commit and push after each stratum.
4. **Then** `--tests '*E3MemoryTest.mp64*'`, then `--tests '*E3MemoryTest.ml5*'`, the same way. For ML5/PM5 also record
   ML5-with-panic. MP64: the printed counts of taken cases with the limit or panic at 64.
5. **Then** E3's 5% stop per stratum, the expected values (part 7 step 4's digest form, unstable cases marked, including
   `R-shortest-025`), and 1b's milestone report: total against 2,118. NT, LB, CR and FK are not built.

**Left in scratch:** nothing a fresh session needs. The R list saved before the edit and both logs are in `step1b/11-*`.
`brouter/build/e3/` is build output and is regenerated.

## Step 1b continued: heights in E3's tuple, stratum F0h, and F0 and R re-run (twentieth session, Linden Caliper)

Dispatch RECORD -58 (with -56, -57). Base `brouter-port` `82d310a`, fetched and equal to `origin/brouter-port` before any
write [C]. Forager read-only at `pre-main` `d6e2a0415` for the heighted fixture, its GeoJSON and its routes; nothing else
re-pinned. Oracle JDK: Temurin 21.0.13+9 amd64, every stratum log's first `E3` line [C]. Evidence in `step1b/12-*` to `16-*`.

### What changed (`d52e08c`, `66d345f`)

- **`E3Outcome` gains `heightDigest`** (`E3Case.kt:56-64`): 64-bit FNV-1a over every node's `OsmPathElement.getSElev()`
  in order, `Short.MIN_VALUE` included, each short's two bytes low byte first (`fnv1aShorts`, `E3Case.kt:107-117`). The
  tuple's line form is now `kind|nodes|cost|distance|nodeDigest|heightDigest`. `fromField` takes the last five fields
  and joins the rest as the kind, so a kind holding `|` reads back whole (`E3Case.kt:70-79`); `E3OutcomeTest` holds that
  and pins the digest to the Python tool's output on a fixed vector [C: jvmTest].
- **`E3CaseRun` fills it** from `track.nodes` (`E3CaseRun.kt:57`, `:66`), and for a found route also counts, per 5°
  tile, the nodes with a height and all nodes (`E3CaseRun.kt:58-63`, `tileOf` at `:82-86`). The child prints that as a
  `heights` line (`E3Jvm.kt:81`); `stability.tsv` gains a last column `R1-heights-by-tile` (`E3Stability.kt:95`, `:103`), and the
  stratum report prints it per case (`E3Strata.kt:40`).
- **Old logs:** `step1b/00-tuple-note.txt` says which files carry the old five-field tuple (4-* to 10-*), unedited.
- **F0h's fixture:** `tools/fetch-f0h.sh` fetches Forager's `segments-heighted/W75_N40.rd5` from raw.githubusercontent.com
  at commit `d6e2a0415e5fc50a43210f78120ceedb9e2e6de4` through `tools/fetch-pinned.sh` (178,867 bytes, SHA-256
  `14f595e8…a965c`) into `build/tiles-f0h/f0h/` alone. The build wires it as F0: an `Exec` task (`fetchF0h`) the oracle's
  test JVM depends on, and a folder property, `brouter.tiles.f0h.dir` (`TestTiles.f0h`). The tile release and
  `tools/tile-pins.tsv` are untouched [C: `git diff --stat 82d310a`]. The blob at that commit hashes to the pin [C:
  `git show | sha256sum`], and the fetch accepted it [C].
- **F0h's routes** (`E3F0hTest.kt`): the eight of `server/pmtiles-worker/test/fixtures/rd5/routes.json` at `d6e2a0415`,
  `from` and `to` as written there. `BRouterHeightedRouteTest.kt:90-99` there carries the same sixteen ends [R].
- **F0h's expected values:** `tools/geojson-heights.py` reads each `brouter-cli/heighted/<name>.geojson` at `d6e2a0415`
  and prints the point count and heights digest; its output is `step1b/12-f0h-expected.tsv`. Only counts and digests are
  committed. Over the eight: 3,630 points, all with a height, 5.75 to 413.75 m [C], which equals Forager's README and
  `BRouterHeightedRouteTest.kt` (3,630; 5.75; 413.75) [R].
- **The height rule used** [R: source]: BRouter's GeoJSON writes a third value only when `getSElev() != Short.MIN_VALUE`,
  and writes `getElev()`, which is `selev / 4.` (`FormatJson.java:109`, `OsmPathElement.java:43-44`, Forager's
  `third_party` copy at `d6e2a0415`), by Java's `Double.toString`. A quarter is exact in binary and `Double.toString`
  reads back to the same double, so selev = value × 4. The tool checks, in decimal, that every value × 4 is a whole
  number in a short's range, and refuses the file otherwise; a missing third value is `Short.MIN_VALUE`. All 3,630
  passed. (`FormatJson.java:110` writes speed there instead when the profile sets `showspeed`; Forager's README says
  these are heights, and the quarter-metre check would very likely refuse speeds [I].)

### F0h forward (`12-f0h-*`) [C]

8 cases, **0 of 8 unstable**, 8 of 8 `route`. R1's point count and heights digest equal the command-line router's on
**all eight**, so the test passed (JUnit XML: 1 test, 0 failures; 0 ` e: ` lines). Every R1 node has a height
(`W75_N40:n/n` on each). R1's distances also equal each GeoJSON's `track-length` (5047, 2573, 6713, 10253, 11107, 7619,
13997, 13908) [C: read, not asserted]. `inside-across-the-degree-line` and `inside-long` have the same node digest, cost
and distance on both squares, as Forager's README says of their points [C]. **No height disagreement** with the
command-line router, so the stop on one did not arise. The planner's first prediction held.

### F0 forward (`14-f0-*`) [C]

2 of 2 stable. R1 as before (226, 5302, 5047, `9ca48d05…`; 306, 7405, 6713, `7d242f8d…`), and the heights digests
are the two predicted for all-`Short.MIN_VALUE` lists, `afc27854ee968af5` and `4f6849ea59b5f775`, with
`W75_N40:0/226` and `W75_N40:0/306`.

### The revert (`13-heights-predictions.txt` item 3, `15-*`) [C]

Prediction pushed in `d971358` before the run. `E3CaseRun.kt:66` `fnv1aShorts(heights)` → `fnv1aShorts(emptyList())`,
the file copied to scratch first; old XML deleted. **Predicted:** compiles; jvmTest 93, 0 failures; F0h 8 of 8 stable
and routes; F0h fails at its reach assertion with every digest `cbf29ce484222325` and counts equal; nothing else.
**Observed:** exactly that. 0 ` e: ` lines in both logs; jvmTest 93, 0; F0h 1 test, 1 failure at `E3F0hTest.kt:33`,
the reach assertion's own message. Restored from the copy; then byte-equal to the copy and to HEAD, `git diff` empty, and
`:66` reads `fnv1aShorts(heights)` again.

### R on all 26 profiles under the new tuple (`16-*`) [C]

One `e3Strata` run on `14ba69d`'s tree, 19:46:39Z to 20:47:20Z (`BUILD SUCCESSFUL in 1h 40s`), 0 ` e: ` lines, JUnit XML
3 tests (`E3RSequenceTest`, `E3RTest`, `E3RProfilesTest`), 0 failures. Load about 8 on 4 cores (another session shares
the container); no case has a time limit, so load moves no outcome.

- **Sequence unchanged:** 1,534 cases, SHA-256 `ed378c85…3d6c`, the pinned value (`E3RSequenceTest`).
- **Unstable: 0 of 1,534,** 0 of 59 in every profile, `shortest` and `hiking-mountain` included.
- **`R-shortest-025` was stable this time.** Its R1 tuple's first five fields, `route|217|5102|5102|5413e0b4…`, equal
  the old run's, and all six regimes agreed. In `10-*` its R6 alone routed 1 m longer. So that instability did not
  recur in one more run [C]; why it happened once is still [?]. **This misses the planner's prediction ("stable apart
  from `R-shortest-025`") and my own (item 4, "1 at most").** The miss is towards stability, and it does not lift -50's
  ruling: that ruling keeps the case out of exact assertions because it was seen unstable once, and that is still true.
- **Old against new:** each case's new R1 tuple, without its heights digest, equals the old R1 tuple (`10-*`, `9-r-*`) in
  **1,534 of 1,534** [C: `16-r-summary.txt`]. Adding the digest moved no other field.
- **R1 kinds:** 1,268 routes, 144 `from-position not mapped`, 106 `to-position not mapped`, 16 others.
- **The data -58 owes, nodes with a height on R's 1,268 R1 routes, per tile** [C]:

| Tile | Nodes with a height | Nodes | Share |
|---|---|---|---|
| E170_S50 | 97,521 | 97,558 | 99.96% |
| W70_N40 | 62,271 | 62,364 | 99.85% |
| W75_N35 | 58,824 | 58,874 | 99.92% |
| W75_N40 | 209,308 | 209,481 | 99.92% |

  So the brouter.de tiles do exercise the new field, on every tile. The planner's third prediction held. 353 nodes on
  30 routes have no height, at most 79 in one route. Where they are (voids in the source DEM, or water) is [?], not read.
  Counted from the `heights` line R1's JVM prints, not from the digest.

Progress as recorded during the run:
- **19:58Z, progress:** 5 of 26 profiles done (car-eco, car-eco-nocost, car-fast, car-fast-nocost, car-vario), 0 of 295
  unstable; R1's first five fields equal the old run's (`10-*`) in 295 of 295. Heights on their 257 R1 routes: E170_S50
  13,324 of 13,327 nodes, W70_N40 8,033 of 8,045, W75_N35 5,304 of 5,304, W75_N40 28,694 of 28,729 [C, partial]. Load
  about 8 on 4 cores (another session shares the container); no case has a time limit.
- **20:19Z, progress:** 14 of 26 profiles done (through `mtb`), 0 of 826 unstable; first five fields equal the old run's
  in 826 of 826. Heights on 682 R1 routes: E170_S50 40,753 of 40,758; W70_N40 28,427 of 28,439; W75_N35 30,851 of
  30,860; W75_N40 98,277 of 98,327 [C, partial].

### Mechanism predictions against results (`13-heights-predictions.txt`, pushed in `d971358` before any run)

| Item | Predicted | Observed |
|---|---|---|
| F0h | 8 stable, 8 routes, count and heights equal on all eight, every node heighted | as predicted |
| F0 | 2 stable, tuples as before, heights `afc27854…` and `4f6849ea…`, 0 heighted | as predicted |
| Revert | compiles; jvmTest 93/0; F0h stable but fails at the reach assertion, digests `cbf29ce4…` | as predicted |
| R | sequence unchanged; at most 1 unstable (`R-shortest-025`); first five fields unchanged | 0 unstable; otherwise as predicted |
| R heights | most nodes heighted [I] | 99.85% to 99.96% per tile |

### What this does and does not show

- **Shown:** E3's tuple now carries each node's height, and a check exists that a wrong one fails (F0h, by the revert).
  On the heighted square, the oracle as E3 calls it gives the command-line router's heights on all eight routes. The
  brouter.de tiles carry heights, so R's expected values, once written, will test them too.
- **Not shown:** anything about the port's heights. The port has no path model yet (step 6), so no port output is
  compared. The revert -57 named, breaking the `short` cast in `interpolateEle` (`OsmPath.java:392`), would be a change
  inside the oracle jar. -58 replaced it with the digest revert, which shows that the comparison sees heights, not that
  the cut-end interpolation is covered. Whether F0h's or R's routes have interpolated end heights at all is [?]; I did
  not separate the end nodes.

### Could not determine, and unverified

- Why `R-shortest-025`'s R6 differed once (`10-*`) and not now.
- Where R's 353 height-less nodes lie, and why they have no height.
- Whether any F0h or R route end is a cut point with an interpolated height (above).
- That `showspeed` is off for the command-line router's run of these GeoJSON files: inferred from Forager's README and
  from every value being a whole number of quarter metres, not from the run's own parameters.
- The iOS targets for this change: read in CI only (below).

### Premises that were wrong

- **None of -58's premises about the tree** were wrong where I checked them: `origin/brouter-port` was `82d310a`;
  `E3Case.kt:54` and `E3CaseRun.kt:~52` were the tuple and its fill; the blob at `d6e2a0415` has the pinned size and
  SHA-256; R is 26 profiles and 1,534 cases; F0 is 2 cases.
- **One prediction missed:** `R-shortest-025` was stable (above).
- **-57 and -58 name different reverts.** -58 is the dispatch, so its revert ran; -57's is recorded above as not run.
- **The dispatch's commit trailer** (`Co-Authored-By: Claude <noreply@anthropic.com>`, no model named) differs from the
  session's own attribution line. I followed the dispatch.

### Calls beyond scope (mine; for the planner)

- **`E3OutcomeTest`** in `jvmTest` (so it runs in CI): `fromField` with a kind holding `|`, and the heights digest pinned
  to the Python tool's output on a fixed vector, so the Kotlin and Python digests cannot drift.
- **The per-tile height count** is a new child output line (`heights`) and a new last column in `stability.tsv`, not
  part of the compared tuple.
- **`fetchF0h` hangs off every oracle test JVM,** as F0's fetch does, so CI's `jvm` job fetches 178,867 bytes from
  raw.githubusercontent.com on each run (seen working in CI).
- **The pins sit in `tools/fetch-f0h.sh`,** as JaCoCo's do in `tools/fetch-jacoco.sh`, since `tile-pins.tsv` is the release's.
- **F0h forward ran before the revert,** so the revert's failure is read against a pass on the same data. The order was
  in the pushed prediction.
- **F0h's distances** were checked against `track-length` by reading, not asserted. The reach assertion is the count and
  heights only, as -58 says.
- **F0's all-`Short.MIN_VALUE` heights are predicted and observed, not asserted.** Asserting them would be cheap, but it
  would add a second failure to -58's revert and so change the prediction -58 fixed.
- **`R-sequence` ran in R's `e3Strata` call,** to confirm the sequence under the new code.
- **`.gitignore` gained `__pycache__/`,** after a stray `.pyc` went into `d52e08c`; `66d345f` removes it, without amending.

### CI [R: job log, partial]

- **Read on `9302765`, run 41900384198:** success. `jvm` TOTAL tests 93, failures 0, in 42 files; 0 ` e: ` lines;
  `fetchF0h` ran and fetched F0h's fixture; `E3OutcomeTest` 2 of 2.
- **Not read, because the planner asked for the handoff first:** both `compileKotlinIos*`, the simulator count and
  arm64 with its gate, on this or any later head. **The final head's CI is unread.** The change is `jvmTest` and `tools`
  only, so iOS compiles nothing new [I].

### Step 1b: the handoff (twentieth session)

The next step is the targeted strata, **as the nineteenth session's handoff gives them** (above), with these changes:

1. Base: `brouter-port` at the commit carrying this section or later; fetch first. Every tuple is now
   `kind|nodes|cost|distance|nodeDigest|heightDigest`. `stability.tsv` has a last column, `R1-heights-by-tile`.
   `E3Targeted`'s "selection's R1 equals E3's" now compares heights too.
2. Number the logs from `17-*` in `step1b/`; `12-*` to `16-*` are this session's.
3. Run DL (`dlLat`, then `dlLon`), then MP64, then ML5, exactly as the nineteenth handoff's items 2 to 4 say, with its
   checks before citing anything and its 5% stop. Commit and push after each stratum.
4. Then the expected values (part 7 step 4's digest form), now carrying heights. `R-shortest-025` stays under -50's
   ruling, although it was stable in this session's run.
5. **First, before step 3:** read CI on the head carrying this section (`gh run list --repo slayer8366/Forager-iOS
   --branch brouter-port`, then `gh api repos/slayer8366/Forager-iOS/actions/jobs/<id>/logs` for `jvm` and `ios-smoke`):
   `jvm` 93 tests expected, 0 ` e: ` lines, both `compileKotlinIos*`, the simulator count (simulator-checked), arm64 and
   its gate. That is -58's item 5, left open.
6. **Running: nothing.** R finished at 20:47:20Z (`16-*`); no Gradle or JVM of mine is left running.
7. **Scratch,** `/tmp/claude-0/-home-user-Forager-iOS/3a3820fc-8d2c-5da2-aa77-f5956bee2927/scratchpad/linden/`: the
   Temurin home `temurin/jdk-21.0.13+9`, the revert's saved copy `revert/E3CaseRun.kt.forward`, the R summary script
   `scripts/rsum.py`, the CI log `ci/9302765-jvm.log`. Every log that matters is copied into `step1b/`. Nothing there is
   needed: the JDK comes again from `bash tools/fetch-temurin.sh <dir>`.

## Step 1b continued: the targeted strata DL-lat, DL-lon, MP64 and ML5/PM5, and `canEscape` on MP64 (twenty-first session, Linden Caliper)

Dispatch RECORD -64 (with -51, -52, -61, -62). Base `brouter-port` `3688c9f`, fetched and equal to `origin/brouter-port`
before any write [C]. CI on `3688c9f` is the planner's read at RECORD -63 (run 41910184631, green), cited, not re-read.
Forager read-only at `/home/user/slayer8366/forager` (`d6e2a041`), used only for `third_party/brouter`'s source. Oracle
JDK: Temurin 21.0.13+9 amd64, every log's first `E3` line [C]. Tuple: -58's, `kind|nodes|cost|distance|nodeDigest|heightDigest`.
Evidence in `step1b/17-*` onward. Each prediction file was pushed before its run.

The `select.tsv` checks (the nineteenth handoff's) are made by `step1b/selcheck.py`, whose output for each stratum is
committed beside it as `*-select-check.txt`: draw numbers 1 to n with no gap; taken ids and draws rising; rejected rows
equal to draws minus taken; routed rows with R2's tuple different from R1's.

### DL-lat (`17-*`; prediction `17-dl-lat-prediction.txt`, pushed in `1359307`) [C]

- **Filled:** 59 of 59, the last taken at draw **501** of the cap of 1,180 (`17-dl-lat-e3Strata.log`, "taken" line).
- **Draws:** 501 counted, 70 routed. **Rejected, 442,** by reason (the log's "rejected" lines; the same counts in
  `17-dl-lat-select-check.txt`):
  - before routing, 431: 419 "the target is not across the line inside the other tile", 10 "no node in the target's
    3 x 3 cells in the other tile", 2 "an end moved off its side";
  - after routing, 11, all "no route": 7 "to-position not mapped", 2 "from-position not mapped", 1 "no track found at
    pass=0", 1 "target island detected for section 0";
  - "route on one side": 0; "R2's tuple differs from R1's": 0.
- **Stability:** 0 of 59 unstable (`17-dl-lat-stability.tsv`, no `UNSTABLE` row; the log's "unstable 0 of 59").
- **Selection against E3:** the selection's R1 tuple and R2 lines equal `E3Targeted`'s in **59 of 59**. R1's kinds: route 59.
- **Against the prediction:** filled, 0 unstable and 59 of 59 held. "Taken within about 300 draws" was wrong: 501. The
  bearing rejects were 84% of draws, not about half [C for the count; why, I].
- Selection took about 2 minutes; the whole task 5 m 4 s.

### DL-lon (`18-*`; prediction `18-dl-lon-prediction.txt`, pushed in `a0a1a2a`) [C]

- **Filled:** 59 of 59, the last taken at draw **326** of 1,180 (`18-dl-lon-e3Strata.log`).
- **Draws:** 326 counted, 60 routed. **Rejected, 267:** before routing 266 (258 "the target is not across the line
  inside the other tile", 6 "no node in the target's 3 x 3 cells", 2 "an end moved off its side"); after routing 1 ("no
  route: target island detected for section 0"); "route on one side" 0; "R2's tuple differs" 0.
- **Stability:** 0 of 59 unstable (`18-dl-lon-stability.tsv`).
- **Selection against E3:** equal in **59 of 59**. R1's kinds: route 59.
- **Against the prediction:** filled and 0 unstable held. **Wrong:** I predicted later than DL-lat (700 to 1,000 draws)
  and more no-route rejects; it filled at 326 with one. The nodes near the line are 1,571,877 in `W75_N40` and 152,909 in
  `W70_N40` (the log), so my picture of sparse land on both sides was wrong [C for the counts; the geography, I].
- The whole task 2 m 47 s.

### MP64 (`19-*`; prediction `19-mp64-prediction.txt`, pushed in `7dbe415`) [C]

Run A is the stratum, on the plain oracle. Run B is the same command with the `canEscape` counting copy first on the
classpath (below). Every stratum figure here is run A's.
- **Filled:** 59 of 59, the last taken at draw **86** of 1,180 (`19-mp64-e3Strata.log`).
- **Draws:** 86 counted, 72 routed. **Rejected, 27:** 14 before routing ("no node in the target's 3 x 3 cells, or the
  target is outside every tile"); 13 "no collected line"; "R2's tuple differs" 0 (`19-mp64-select-check.txt`).
- **Taken with the limit: 0; with panic mode: 0** (the log's line, E4's count at `memoryclass` 64).
- **Stability:** 0 of 59 unstable (`19-mp64-stability.tsv`).
- **Selection against E3:** equal in **59 of 59**. R1's kinds: route 59.
- **Against the prediction:** filled within 150 draws, 0 limit, 0 panic, 0 unstable, 59 of 59: all held.
- The whole task 10 m 56 s; run B 11 m 7 s.

### `canEscape` on MP64's runs (RECORD -61) [C unless marked]

- **Who calls it** [R]: `nodesMap.canEscape` has one caller, `RoutingEngine.java:2016`, inside the collection at `:2005-2020`
  (Forager `third_party`, the jar's source; RECORD -61 read the same). So it runs only where a "collected" line is
  logged.
- **The counting copy** (`19-mp64-canescape-counter.diff`; scratch, never on any branch as code): Forager `third_party`'s
  `OsmNodesMap.java`, compiled `--release 11 -cp <oracle jar>` on Temurin 21.0.13+9.
  - Plain, it is bytecode-identical to the jar's class: `javap -c -p`, constant-pool numbers and spacing normalised, 0 of
    884 lines differ.
  - Patched, `canEscape` is renamed `canEscape0`, whose bytecode equals the jar's `canEscape` but for its name and access.
    A new `canEscape` wraps it, counting calls and false answers. A static block appends a load line, and a shutdown hook
    a per-JVM total, to a scratch file; the first three false nodes per JVM are also written. Method by method, the
    other 29 methods are identical to the jar's.
  - Put first on `e3Strata`'s classpath by a scratch init script (`-I`); every child JVM inherits `java.class.path`
    (`E3Launcher.kt`). The log's own line shows the copy at the classpath's head, and all 314 child JVMs wrote a load
    line and a total (`19-mp64-canescape-summary.txt`), so each counted run ran the copy.
  - **A first form was stopped:** it wrote a line per false answer, 124 MB in 90 s, enough I/O to press on the 600 s
    limit. It was killed with no JVM left running, and none of its output is cited. The second form is the one above.
- **The probe changed nothing measured:** run B's `select.tsv` and `stability.tsv` equal run A's in every column but
  `R1-ms` (`19-mp64-canescape-vs-plain.txt`, 0 diff lines each).
- **The count:** `canEscape` answers false in **every one of the 59 taken cases**, in every regime.
  - One run of the 59: **576,094 false of 2,366,007 calls** (24.35%), the same total in R3, R4, R5 and R6, and exactly
    twice it in each R1R2 JVM (R1 and R2 share a JVM).
  - Per case, the selection's R1R2, E3's R1R2 (both twice one run), R4, R5 and R6 give the same calls and false answers
    in 59 of 59 cases (`19-mp64-canescape-per-case.tsv`). The count is deterministic across E3's six regimes.
  - Fewest: `MP64-0025`, 34 false of 171 calls (1 collected line). Most: `MP64-0064`, 112,493 of 525,372 (198 lines).
    Median 2,076 false per case.
  - The 13 routed draws rejected for "no collected line" made 0 calls, as the single caller says they must.
  - Five draws beyond 86 were in flight when MP64 filled. They are not counted, and their JVMs are left out of the
    figures above (four of the five also answered false).
  - The first three false nodes of each JVM, as `ilon,ilat`, are in `19-mp64-canescape-counts.txt`. That is where a
    region for 5b's `CollectOutreachersTest` could be cut [I: not tried].
- **Against the predictions:** the planner's "at least one MP64 case" held. **Mine was wrong:** I guessed a minority of
  cases. It is all 59, and about a quarter of all calls.
- **What it means for 5b** [I]: the false branch is not rare on the router's own path. It is the common answer whenever
  a collection runs on these routes. -60's regions never reached it (0 of 3,587), most likely because `collectOutreachers`
  was driven there outside a routing run, with different visit ids and hollow links. Whether a region cut around one of
  these nodes reproduces false in 5b's test is [?].

### ML5 and PM5, one sequence (`20-*`; prediction `20-m5-prediction.txt`, pushed in `01388f4`) [C]

- **ML5 filled:** 59 of 59, the last taken at draw **246**.
- **PM5 is SHORT:** **55 of 59** at the cap, its last case taken at draw **1,180** (`20-m5-e3Strata.log`). The task fails
  on the short assertion by design. The JUnit message is "M5: short after 1180 draws: {PM5=55} of 59"
  (`20-m5-junit.xml`, `E3MemoryTest.kt:49`); it is the only failure.
- **The stop was sent, and the planner ruled** (technical; RECORD -71, to be written by the planner): PM5 stands at 55
  and is reported short, as E4 says. It is never filled from another generator, and the cap, the seed and the generator
  do not change. The expected values use n = 55. Its own bound, re-derived here: a clean run bounds PM5's instability
  rate below 1 − 0.05^(1/55) = **5.30%**, against 4.95% at 59 [C: arithmetic].
- **Draws:** 1,180 counted, 978 routed (`20-m5-select-check.txt`). **Rejected, 1,066:**
  - 202 before routing ("no node in the target's 3 x 3 cells, or the target is outside every tile");
  - 232 "ML5 already full", every one of them the limit (204 after panic mode);
  - 632 "neither": 551 routes without panic, of which 482 collected at least once and 69 never did; 65 "to-position not
    mapped", 10 "no track found at pass=0", 3 "from-position not mapped", 3 "target island detected for section 0";
  - "R2's tuple differs from R1's": 0.

  The log prints "neither" by its collection count, so it has 40 separate lines; the grouping above is
  `selcheck.py`'s and a reading of `20-m5-select.tsv`'s R1 kinds.
- **Panic and the limit over all 978 routed draws:** 313 showed panic mode. 258 of them ended in the limit; the other 55
  are PM5, all of them, every one a route. 33 hit the limit without panic. So **panic without the limit is 55 of 978
  routed draws (5.6%)**, which is why PM5 is short.
- **ML5 with panic: 54 of 59** (the log's line). The other 5 reached the limit with no panic line. **ML5 is mostly
  panic, then the limit** (the planner's wording): its cases test the panic path and the throw after it, not only the
  throw.
- **Stability:** 0 of 59 unstable in ML5 (`20-ml5-stability.tsv`), 0 of 55 in PM5 (`20-pm5-stability.tsv`).
- **Selection against E3:** equal in **59 of 59** (ML5) and **55 of 55** (PM5). R1's kinds: ML5 "memory limit reached"
  59; PM5 route 55.
- **No case in two strata:** each draw has one result in `select.tsv`, and across all four sequences the 291 taken ids
  are distinct.
- **Against the prediction:** ML5 filling early, most ML5 cases with panic, PM5 the one at risk, 0 R2 differences and 0
  unstable all held. "PM5 fills, but late" was wrong: it is short by 4.
- The whole task 43 m 32 s (selection 26 minutes).

### Summary of the four sequences [C]

| Stratum | Taken | Last draw | Draws (cap 1,180) | Routed | Rejected | Unstable | Selection = E3 |
|---|---:|---:|---:|---:|---:|---:|---:|
| DL-lat | 59 | 501 | 501 | 70 | 442 | 0 of 59 | 59 of 59 |
| DL-lon | 59 | 326 | 326 | 60 | 267 | 0 of 59 | 59 of 59 |
| MP64 | 59 | 86 | 86 | 72 | 27 | 0 of 59 | 59 of 59 |
| ML5 | 59 | 246 | 1,180 (shared) | 978 (shared) | 1,066 (shared) | 0 of 59 | 59 of 59 |
| PM5 | **55 (short)** | 1,180 | (shared) | (shared) | (shared) | 0 of 55 | 55 of 55 |

That is 291 cases, all stable. -64's prediction ("each stratum fills ... 0 unstable; at least one MP64 case has
`canEscape` answer false") held except for PM5's fill.

### CI

**Not read on the final head: handed off at the planner's call (04:25Z) with CI in progress.** [R: `gh run list`]
- Run 42162460699 on `0042a3d` was in progress at handoff; this report commit's own run follows it. The next session
  or the planner reads them: `jvm` count (93 expected), 0 ` e: ` lines, both `compileKotlinIos*`, the simulator count
  (simulator-checked), and arm64 with its gate.
- **Status only, not job logs:** runs 42155257944 (`7dbe415`), 42156448759 (`8c0ceda`), 42156753243 (`454236c`),
  42157935970 (`1d4cd54`), 42157994570 (`4f815de`) and 42158026273 (`01388f4`) all completed `success`. This session
  changed no code: only files under `docs/audits/` (`git diff --stat 3688c9f..HEAD`, outside `docs/audits/` empty).

### Could not determine, and unverified

- **Why the bearing rejects are 84% of DL-lat's draws** and 79% of DL-lon's, against the "about half" predicted. They are
  counted [C]; the cause is not read from `DLGenerator` [?].
- **Where `canEscape`'s false nodes lie,** beyond the first three per JVM, and whether a region cut around one makes 5b's
  `CollectOutreachersTest` see false [?]. Not tried; 5b's branch is outside this dispatch.
- **R1 against R2 inside one R1R2 JVM:** the counter totals per JVM, so R1's and R2's counts are not told apart. That
  they are equal is inferred from the JVM total being exactly twice R4's in 59 of 59 [I].
- **R3's count** is one total over all 59 cases (one JVM). It equals the sum of R4's [C], but it is not per case.
- **The ML5/PM5 stratum logs were run on the plain oracle only;** `canEscape` was counted on MP64 alone, as -64 asked.
- **The geography behind DL-lon's node counts** (1.57 million nodes near 70°W in `W75_N40`) is not checked against a map
  [?].

### Premises that were wrong

- **-64's "each stratum fills its 59 within the cap"** (the planner's prediction, marked [I]): PM5 did not, 55 of 59.
- **Mine, from the nineteenth session:** "PM5 is the one that may come up short" held. But "DL-lon, with more rejects
  before routing than DL-lat" did not: it had fewer, and filled sooner.
- **Mine, at `19-mp64-prediction.txt` item 5:** `canEscape` false "in a minority of cases" was wrong. It is all 59, and
  24% of all calls.
- **The dispatch's search hint `**ID:** 2026-10-28-6`** also matches -60 to -69 as a prefix. -64 is `2026-10-28-64`
  (`RECORD.md:2662` on the record's branch). Read correctly; noted only so the next hint can be exact.
- **The Forager clone is at `d6e2a041`,** not -64's `a0535ece`. Only `third_party/brouter` was read, which -64 records as
  unchanged between the two [R: -64; not re-diffed here].

### Calls beyond scope (mine; for the planner)

- **MP64 ran twice:** run A on the plain oracle is the stratum; run B carries the counter. This gives a check that the
  probe changed nothing (0 diff lines). It cost 11 minutes.
- **The counter's first form was stopped and replaced** (above). The diff committed is the second form's.
- **`selcheck.py` and `cesum.py` are committed in `step1b/`** as the readers of the committed tables, not as tools.
  `19-mp64-canescape-counts.txt` (345 kB) is committed whole.
- **The 5 MP64 draws in flight at fill** are left out of every count, as `E3Select` leaves them out of `select.tsv`.

### Step 1b: the handoff (twenty-first session)

1. **Base:** `brouter-port` at the commit carrying this section or later; fetch first. Logs are numbered to `20-*`; the
   next is `21-*`.
2. **Done:** DL-lat, DL-lon, MP64, ML5 and PM5 (PM5 short at 55, standing by the planner's ruling, RECORD -71). The
   `canEscape` count is in. Nothing here needs re-running.
3. **First, read CI on the head** ("CI" above); it was left unread at handoff.
4. **Next, per -64:** the remaining strata, NT, LB, CR and FK. They are not built. NT and CR select by coverage, so their
   selector is the first code to read JaCoCo's `.exec` files (-51), and it must be seen failing first (-50). Then the
   expected values in part 7 step 4's digest form, carrying heights, with PM5 at n = 55 and its bound 5.30%, and
   `R-shortest-025` under -50's ruling.
4. **For the planner:** `canEscape`'s false nodes, for 5b (-61), are in `19-mp64-canescape-counts.txt`. To count again,
   rebuild the copy: apply `19-mp64-canescape-counter.diff` to Forager `third_party`'s `OsmNodesMap.java`, run `javac
   --release 11 -cp brouter/build/oracle/brouter-1.7.10-ro.jar -d <dir>` with the path in `PROBE_FILE` edited, and pass a
   scratch init script with `-I` that sets `t.classpath = files('<dir>') + t.classpath` on `e3Strata`. Never commit it.
5. **Running: nothing.** The ML5/PM5 task ended at about 04:24Z; no Gradle test JVM or E3 child of mine is left.
6. **Scratch,** `/tmp/claude-0/-home-user-Forager-iOS/3a3820fc-8d2c-5da2-aa77-f5956bee2927/scratchpad/linden/`: the
   Temurin home, `canescape/` (sources, classes, init script, counts), and `scripts/`. Every file that matters is in
   `step1b/`.


### Step 1b: the coverage reader, and the handoff (twenty-second session)

RECORD -77, item 1 done; item 2 (NT) built in part, not run. Logs `step1b/21-*`.

1. **The coverage reader,** `E3Coverage.kt` (jvmTest, `oracle/e3`). It reads R6's `.exec` with JaCoCo's core 0.8.16 (the
   agent's version; a new jvmTest dependency, `gradle/libs.versions.toml` and `brouter/build.gradle.kts`, writes outside
   -77's "jvmTest, tools and docs", decided beyond scope because nothing else on the classpath can analyse execution data:
   the agent jar holds no `Analyzer`). It analyses the oracle jar's own class files, refuses data whose class checksum is
   not the classpath's, refuses a named line with no instructions, and applies -50's rule first: where R6's tuple differs
   from R1's the reading is refused and the file is never opened (`E3CoverageRuleTest`, in jvmTest, 2 tests; jvmTest 95).
2. **Seen failing first** (`E3CoverageTest`, e3Strata; prediction `21-coverage-prediction.txt` pushed first, `dec8499`):
   F0's `inside-long` on `hiking-mountain`, R1 and R6 equal (`route|306|7405|6713|7d242f8dd2c428b4|4f6849ea59b5f775`).
   CR's lines read **not covered**, `StdPath.java:101-103` 0/3 lines and `:105-113` 0/9 (every line 0 of 2 to 11
   instructions), as predicted: they sit in `if (rc.bikeMode)` and `hiking-mountain` is foot only. `RoutingEngine.java:177`
   reads covered. Log `21-coverage-e3Strata.log`. **Bite:** `ls.all { it.covered > 0 }` changed to `it.total > 0` (copy saved
   first, restored from it, `cmp` equal; no compile error in `21-coverage-bite-gradle.log`): the test failed with its own
   message, "CR's lines on a foot profile: covered (StdPath.java:101-103 0/3 lines; StdPath.java:105-113 0/9 lines)
   expected:<false> but was:<true>" (`21-coverage-bite-junit.xml`).
3. **A finding for the planner: NT's reach line is reached by almost every case.** On F0, `WaypointMatcherImpl.java:71` reads
   covered. `end()` adds each link twice, forward and reverse, with the same radius (`:204`, `:223`), and `updateWayList`
   sorts after each add (`:257`), so the comparator falls to `:71` on every matched waypoint. Coverage of `:71` cannot tell a
   near-tie from any case; NT's targeting rests on its construction (the 4-ulp gap), not on this reach. Not a listed stop;
   NT's selector is built to require both. **[R]** for the code reading, **[C]** for F0.
4. **NT, built, not run, no prediction pushed:** `NTGenerator.kt` and the selector's R6 option (`E3Select`'s `r6Agent`,
   `E3Targeted.select(coverage = true)`; `select.tsv` gains an `R6-tuple` column before `reach`). The construction and
   why it must be a straight line through a junction are in `NTGenerator`'s KDoc: with the start exactly on the junction
   every link there has radius 0, and a 4-ulp gap (about 10^-14 degrees) needs two exactly opposite links (a zero integer
   cross product), since integer ends approach a bisector only to about 10^-11 degrees. The generator draws J0 as R does,
   finds such a junction in J0's cell from the links `hiking-mountain` may use (decoded with the oracle's decoder and the
   profile's validator, `TileNodes.decodeLinks`), puts the end along that line, and checks the gap with the oracle's own
   matching in the test JVM (`RoutingEngine.matchWaypointsToNodes` by reflection). **Not yet written:** `strata/E3NTTest.kt`
   (seed next free, 20261205; mark: `E3Coverage.reach(r1, r6!!, exec!!, E3Coverage.NT)`, and a refused reading printed as
   a stop for the planner), its prediction, and any run. **Unknown:** how many such junctions W75_N40 holds; NT may be short.
   `RGenerator`'s draw order is unchanged (`drawNode` makes the same two calls `candidate` made).
5. **Not started:** CR, LB, FK. CR's profile (E's: `trekking.brf` plus the crossing assigns, Phase 1 report lines 868-871) must
   be written into each JVM's `profiles2/` copy (`E3Stability.launch` copies the oracle's only).
6. **Commits:** the first (`dec8499`) ends with `Co-Authored-By: Claude <noreply@anthropic.com>` as -77 asked; the later one
   carries the session's attribution lines, which this session's harness requires.
7. **Running: nothing.** Scratch as before; `22-jvmTest.log` there is the last full jvmTest (95, 0 failures).


## Step 1b continued: NT, CR and LB, and FK's probes (twenty-third session, Linden Caliper)

Dispatch RECORD -77, -78 (rulings -79, -80 by message). Base `brouter-port` `00b9886`, fetched, equal to origin [C]. Oracle JDK
Temurin 21.0.13+9 amd64, each log's first `E3` line [C]. Tuple -58's. Each prediction was pushed before its run (`f410e02`
NT, `4d43ea2` CR and LB). jvmTest 95, 0 failures, before each code push (`23-jvmTest*.log` in scratch) [C]. 0 ` e: ` lines in
each stratum log [C]. `select.tsv` checks by `selcheck.py`, committed as `*-select-check.txt` [C].

### NT (`23-*`; seed 20261205) [C]
- **Filled:** 59 of 59, last taken at draw **89** of 1,180. 59 routed. **Rejected 30, all before routing:** 27 "no junction
  with two exactly opposite links in the drawn node's cell", 2 "no node in the target's 3 x 3 cells", 1 "the oracle's
  matching refused it: to-position not mapped".
- **Reach (-78):** the oracle's 4-ulp check, repeated by the mark. Every taken case reads **gap 0 ulps**, an exact tie. `:71`
  reads covered in 59 of 59, recorded only. 0 coverage stops, because R6 equals R1 in every case. R2 equals R1 in every routed
  case.
- **Stability:** 0 unstable of 59. The selection's R1 and R2 equal E3's in 59 of 59. R1 kinds: 56 route, 2 "target island
  detected for section 0", 1 "to-position not mapped".
- **Against the prediction:** filled as guessed. One taken case's R1 is "to-position not mapped", although the generator's
  in-JVM matching of the same ends did not throw, while another draw's did. Why the two differ is not traced **[?]**. Any
  outcome kind is taken, my call (the tie is broken in matching, before routing). For the planner.

### CR (`24-*`; seed 20261206; profile `trekking-crossing`, SHA-256 `a9cc214d…` in the log) [C]
- **Filled:** 59 of 59, last at draw **83**. 73 routed. **Rejected 24:**
  - 10 before routing: "no node in the target's 3 x 3 cells";
  - 10 "not covered" (101-103 0/3, 105-113 0/9);
  - 4 "not covered" (101-103 3/3, 105-113 0/9).
- **Reach:** every taken case covers both spans in full, so the captured tiles do carry `estimated_crossing_class`. That
  answers Phase 1's [?].
- **Coverage stops:** 0. **Tiles of the 59:** E170_S50 19, W70_N40 13, W75_N35 11, W75_N40 16.
- **Stability:** 0 unstable of 59. The selection's R1 and R2 equal E3's in 59 of 59. R1 kinds: 59 route.
- **Code:** `E3Profiles.crossing()` writes E4's assigns into the top of `trekking.brf`'s global context, and checks that
  `trekking.brf` names none of them. `E3Stability.ownProfiles` writes it into every launch's `profiles2/`, refusing to
  replace an oracle profile.

### LB (`25-*`; seed 20261207, hiking-mountain) [C]
- **(c) rows:** the 13 of step 2b (MsunLibm against OracleLibm, asserted). **Inside the tiles: 5**, as computed: 1295, 1297,
  1310, 1311 and 1318. E170_S50 holds none.
  - Phase 1's "12 in-tile rows" is another comparison **[I]**. For the planner.
- **Filled:** 59 of 59, last at draw **99**. 91 routed. **Rejected 40:**
  - 27 "route leaves the band";
  - 8 before routing: "no node in the target's 3 x 3 cells";
  - 4 no route, "to-position not mapped";
  - 1 no route, "from-position not mapped".
- **Taken by row:** 1295 1, 1297 4, 1310 13, 1311 18, 1318 23.
- **Stability:** 0 unstable of 59. The selection's R1 and R2 equal E3's in 59 of 59. R1 kinds: 59 route. Filled far sooner
  than guessed (draw 600).

### FK: probes only, not built (`26-fk-probes/counts.txt`; scratch probes of the oracle jar, not E3 runs) [C]
Counts by message, no coordinates (-80).
- **Kind 1, water starts:** 15 of 15 "from-position not mapped".
- **Kind 6, ends west of 75°W:** 12 of 12 "datafile W80_N40.rd5 not found".
- **Islands with trekking-noferries:**
  - target on the island: 7 of 32 "target island detected";
  - start on the island: 11 of 20 "no track found at pass=0".
- **Kind 5's E4 construction (car-fast):** 6 "to-position not mapped", 1 "from-position", 3 routed.
- **Cuts of E170_S50:**
  - 5 "file of size N too short, should be 1061704";
  - at dataEnd, "Index -399101066 out of bounds for length 65636";
  - 4 routed (cuts at 1, 100, 199 and size-1).
- **Byte flips:**
  - 4 "top index checksum error";
  - byte 1, "lookup version mismatch";
  - 5 sub-index flips (203 to 219) routed.
- **"start island detected" is unreachable on Forager's path [R]:** it is thrown only when `inverseRouting` is set
  (`RoutingEngine.java:954-958`). That comes from a profile assign (`RoutingContext.java:174`), which no oracle profile has.
  -79 rules kind 3 as "start on a ferry-only island", with its family being the first case's message. On the probes that is
  "no track found at pass=0".
- **Shared families on Forager's path:** kinds 3 and 5 as I first proposed them; kinds 2 and 5 under -80.

### CI
Not read on this session's head: the handoff came first. CI on `00b9886` was read by the planner (-78). **Unverified.**

### Calls beyond scope, and premises that were wrong
- `E3Stability.ownProfiles` and `E3Profiles` are harness code in jvmTest, inside -77's scope.
- Wrong premise: Phase 1's "12 in-tile rows" for LB. Under step 2b's (c) rows there are 5.
- Wrong premise, as -78 already said: `:71` as NT's reach.

### Step 1b: the handoff (twenty-third session)
1. **Done:** NT, CR and LB, each filled with 0 unstable, evidence committed.
2. **Next, first: FK per RECORD -80**, on `brouter-port`, generator and prediction pushed before the run.
   - **Kind 1:** water starts.
   - **Kind 2:** water ends.
   - **Kind 3:** a start on a ferry-only island; family is the first case's message (-79).
   - **Kind 4:** a seeded island generator, cap 200; family "target island detected".
   - **Kind 5:** an end on a way the profile cannot use, with no usable way within 250 m; family "not mapped", shared
     with kind 2.
   - **Kind 6:** W80_N40.
   - **Kind 7:** 10 cuts reaching "file of size * too short, should be *". Plus FK7-x1, the dataEnd cut, compared by
     exception class (step 10 must check its text on iOS), and FK7-x2, one routing cut.
   - **Kind 8:** 10 flips in bytes 8 to 199, family "top index checksum error". Plus FK8-x1, a sub-index flip that routes.
   - **Kinds 9 to 11:** need harness changes to E3CaseRun: R1 timed, then the 50 ms rerun; the two cancel modes; a
     links-processed count.
   - The probe tool is `26-fk-probes/Probe.java`. Its coordinates are in scratch only (`linden/fkprobe/*.txt`).
3. **Then:** read CI on the final head; E5's expected values in a session of their own.
4. **Running: nothing.**

## Step 1b continued: FK built and running (twenty-fourth session, Linden Caliper)

Dispatched under RECORD -79, -80 and -82 (by message). Base `brouter-port` at `ffa06ab`, fetched, equal to origin [C].
- **Before the run:** jvmTest 95 tests, 0 failures, 0 ` e: ` lines; the JUnit XML was fresh at 06:22:53Z (`24-jvmTest.log` in scratch) [C].
- **The generator and the prediction were pushed before the run,** at `49e2711` (`step1b/27-fk-prediction.txt`) [C].
- **The harness:**
  - `E3Case.Plan` covers kinds 9 to 11;
  - `E3CaseRun` makes a measured run first, then the variant: the 50 ms limit, a cancel before `doRun`, or a cancel past
    10,000 links;
  - it logs a `thrown` class for FK7-x1 and a `measured` line;
  - `FKGenerator` and `E3FKTest` build the cases. Their construction is in the KDoc.

### What the running log showed by 06:26Z (`27-fk-e3Strata.log`, counts only) [C]
- **Kinds 1, 2, 3 and 6:** 10 of 10 built each (after 10, 12, 10 and 10 draws). Their families are not read yet.
- **Islands, from the link graphs:** 1 in E170_S50, 12 in W70_N40 and 1 in W75_N35.
- **Kind 4:** full, 10 of 10 by draw 74.
  - Of the 64 rejected draws, 41 were "to-position not mapped" and 23 routed.
  - So 41 graph-found island nodes were not matched by the oracle. Why is [?]. Kind 3 starts on the same kind of node, so
    its family has to be read.
- **Kind 5: short, 9 of 10 at 200 draws.** The rejections by reason:
  - 111 had a car-fast link within 300 m;
  - 68 had a start node with no car-fast link;
  - 12 had 3 × 3 cells that leave the tile.

  **The 200-draw cap for kind 5 was my own choice; -80 set that cap for kind 4.** The planner ruled (-82) that kind 5's
  start and end are drawn separately, capped at 400, and kind 5 is rerun alone.
- **Kinds 7 and 8:** E170_S50 is 1,061,705 bytes, with its data end D at 1,061,592.
  - Cuts: 200, 201, 1000, D/4, D/2, 3D/4, D−1, D+1, D+56 and D+111.
  - Flipped bytes: 8 to 188, in steps of 20.

### The kind 5 change: written, not compiled, not pushed
The patch is `linden/27-kind5-rerun.patch` in scratch (129 lines). It adds:
- `FKGenerator.usableNode` and `unusableNode`;
- in `E3FKTest`, `kind5()`, `CAP5 = 400` and a separate test, `fkKind5ReachesOneFamilyInAllTen`.

I did not compile it while FK was running: a compile would replace the classes that the run's child JVMs load from the
build directory. The working tree was restored to `49e2711`.

### Step 1b: the handoff (twenty-fourth session)
1. **Running:**
   - FK's e3Strata run, Gradle PID 16430, started about 06:23Z;
   - log: `/tmp/claude-0/-home-user-Forager-iOS/3a3820fc-8d2c-5da2-aa77-f5956bee2927/scratchpad/linden/27-fk-e3Strata.log`;
   - work: `brouter/build/e3/FK/`, `FK4-select/` and `FK9-11-select/`.

   It is finished when `BUILD` appears in the log. **It will end FAILED whatever else happens,** because kind 5 is
   short (9) and that is a STOP in the test.
2. **Reading the results:**
   - run `grep -E "E3 FK" <log> | cut -c1-250`. Each kind has a `family … in family x of 10; unstable u` line, and each
     extra case has its own line;
   - check `grep -c " e: " <log>` and `grep "E3 FK: " <log>`, the line with the 5% count;
   - commit the log, `FK/stability.tsv`, `FK4-select/select.tsv` and `FK9-11-select/select.tsv` as
     `step1b/27-fk-*`. **No file holding coordinates is committed (E1).** The `.tsv` files and the log's case lines hold
     them, so strip every coordinate column first, or commit counts and digests only. `step1b/11-r-sequence-before-edit.tsv`
     is left untouched: it goes to the owner (RECORD -83).
3. **Next, after the run:**
   - apply `27-kind5-rerun.patch`;
   - write `step1b/28-fk5-prediction.txt`: family "to-position not mapped in existing datafile", 10 of 10;
   - run the full `:brouter:jvmTest`, then push;
   - run `--tests '*E3FKTest.fkKind5*'` (the full FK test also builds kind 5 the new way after the patch).
4. **Then:**
   - write FK's section, per kind: cases in family, stability, and kind 4's draws;
   - record the three extra cases' outcomes and FK7-x1's class from R2 to R5's `thrown`;
   - read CI on the final head;
   - then E5's expected values.
5. **For the planner:**
   - kinds 9 to 11 voids and any instability are unread until the run ends;
   - kind 10's measured-run precondition ("routes") is my own addition, beyond E4 [I].

## Step 1b continued: FK finished (twenty-fifth session, Linden Caliper)

Dispatched under RECORD -82, -84 and the planner's rulings by message (RECORD -85). Base `brouter-port` at `eb6460d`,
fetched, equal to origin [C]. Evidence in `step1b/27-*` (the first run), `28-*`/`29-*` (kinds 5 and 3 alone) and `30-*`
(the full run on `ce3db16`). **No coordinates are committed (E1):** the `R1-extent` column (BRouter integer coordinates)
is dropped from every selection table, and each file was refused if any decimal or integer coordinate was left [C].

### The first run (`27-fk-e3Strata.log`) went through E3's regimes [C]
-84 read it as stopping in selection. It did not: `E3FKTest.kt:182` was the final assertion, after every case ran. Its
stops were **FK3, 6 of 10** in its family (FK3-05, -07, -08 and -09 routed) and FK5, 9 of 9 (short). Instability was 4 of 113.
The planner corrected -84 (-85). Kind 3's prediction ("no track found at pass=0", `27-fk-prediction.txt` item 3) missed:
the first run's family was "from-position not mapped" [C].

### Kinds 5 and 3, changed and rerun alone (`9a04e27`, `ce3db16`) [C]
- **Kind 5 (-82):** start and end drawn separately, cap 400. Prediction `28-fk5-prediction.txt` pushed first, jvmTest 95 / 0
  failures / 0 ` e: ` lines. **10 of 10 after 194 draws.** Rejections: end, car-fast link within 300 m, 146; end, cells
  leave the tile, 21; start, no car-fast link, 7. No rejection used an oracle outcome. Family "to-position not mapped in
  existing datafile", 10 of 10, 0 unstable. Prediction met.
- **Kind 3 (-85):** selected like kind 4, by R1 for "no track found at pass=0", cap 200. Prediction
  `29-fk3-prediction.txt` pushed first, jvmTest 95 / 0 / 0. **10 of 10 by draw 61**, with 51 rejections by outcome: 35
  "from-position not mapped" (kind 1's family) and 16 "route". 10 of 10 in family, 0 unstable. The fill (10 in 61) beat my
  guess of 1 in 5 or fewer [C]. Both alone tests PASSED (`29-fk35-e3Strata.log`).

### The full run on `ce3db16` (`30-fk-e3Strata.log`): PASSED, 114 cases, 0 ` e: ` lines [C]
Each kind's family comes from its first case; all 10 matched it in every kind:

| Kind | Family (first case) | In family | Unstable |
|---|---|---|---|
| 1 | from-position not mapped in existing datafile | 10/10 | 0 |
| 2 | to-position not mapped in existing datafile | 10/10 | 0 |
| 3 | no track found at pass=0 (FK3-0012) | 10/10 | 0 |
| 4 | target island detected for section 0 (74 draws) | 10/10 | 0 |
| 5 | to-position not mapped in existing datafile | 10/10 | 0 |
| 6 | datafile W80_N40.rd5 not found | 10/10 | 0 |
| 7 | file of size * too short, should be * | 10/10 | 0 |
| 8 | top index checksum error | 10/10 | 0 |
| 9 | family:timeout | 10/10 | 3 |
| 10 | family:watchdog | 10/10 | 0 |
| 11 | family:watchdog | 10/10 | 0 |

- **Instability: 3 of 114, under E3's 5%** (the first run had 4 of 113). It is all in kind 9: FK9-0001, -0037 and -0040
  in both runs, and -0049 in the first run only. In each one, **R3 is voided ("the measured run took under 1 s")** while
  the other five regimes time out. These are timing voids, not differing outcomes. Per -85 (as -50 ruled for
  R-shortest-025) they are counted as unstable and kept out of exact assertions [C].
- **The selection depends on timing near 1 s** [C]. Draw 13 (FK9-0013) was rejected in both runs, but for different
  reasons: a void in the first run (963 ms) and "R2's tuple differs from R1's" in the second (R2 was voided, 1095 ms).
  The cases taken, and the draws at which they were taken (49, 44, 30), are the same in both runs.
- **Extra cases, compared on outcome:**
  - **FK7-x1** (cut at D) gives R1 "to-position not mapped in existing datafile", stable, nothing thrown, in both runs.
    Session 23's probe gave "Index -399101066 out of bounds for length 65636". I reran the probe on the byte-identical
    cut tile: with the probe's ends it gives the index exception, with FK7-x1's ends it gives "not mapped"
    (`30-fk7x1-probe.txt`). **So the outcome follows the ends** [C]. Why those ends read as unmapped from a tile cut at D
    is [?].
  - **FK7-x2** (size − 1) and **FK8-x1** (byte 203 flipped) both route, **identically to the intact control FK7-c0**
    (`route|234|6078|4768|efb52c121f8934c3|c591233bb504b767`, heights E170_S50 234/234), stable [C]. The oracle accepts
    both edits without noticing them.
- Kind 10's precondition ("the measured run routes") is still my own addition, beyond E4 [I].

### E1: earlier committed selection tables hold coordinates as integers [R]
The `R1-extent` column (`E3Select.kt:146`) is each route's bounding box in BRouter integer microdegrees, e.g.
`(lon + 180) × 1e6`. The decimal grep of E1 does not catch it. Row counts with such extents: `17-dl-lat-select.tsv` 59,
`18-dl-lon-select.tsv` 59, `19-mp64-select.tsv` 65, `20-m5-select.tsv` 606, `23-nt-select.tsv` 56,
`24-cr-select.tsv` 66, `25-lb-select.tsv` 86. Reported to the planner; I did not touch them. FK's tables were stripped.

### CI
Read from the job logs by me (`gh run view --log`) [R]:
- **Run 42183317078 on `ce3db16`** (the last code change) and **run 42184654325 on `67012ee`** (the report) both
  concluded `success`. In each: `jvm` 95 PASSED, 0 FAILED, 0 ` e: ` lines; `compileKotlinIosArm64` and
  `compileKotlinIosSimulatorArm64` both ran, BUILD SUCCESSFUL; simulator 23 tests, 0 failures, in 9 files
  (**simulator-checked**); the arm64 JVM step 7 tests, 0 failures, in 2 files, and its gate printed "OK: all 2 classes ran".
- The run for this commit, which changes only the report, was not read past its status.

### Step 1b: the handoff (twenty-fifth session)
1. **Nothing is running.** FK is complete: 11 kinds × 10 plus four extra cases, PASSED on `ce3db16`.
2. **Next: E5's expected values for FK.** Each kind's expected value is its family above, with kind 7's numbers read as
   `*`. Exact assertions skip kind 9's unstable cases (FK9-0001, -0037, -0040; and -0049, which was unstable in the first
   run). FK7-x2 and FK8-x1 expect FK7-c0's tuple. FK7-x1 expects "to-position not mapped" for its own ends, with its
   exception class [?].
3. **For the planner and the owner:** the `R1-extent` columns already committed (E1, above). Fixing `E3Select` so that
   it stops writing the column is a code change I have not made.

## Step 1b finished: the expected values (twenty-sixth session, Linden Caliper)
Dispatch: RECORD -88. Baseline `f308f88`. Commits `7e02bdd` (code and the file), `13506f1` (the failing-first prediction),
and this report's commit. Only `brouter/src/jvmTest/` and `docs/` are touched; no `commonMain` change.

### The stored file: `brouter/src/jvmTest/resources/e3/expected-1b.tsv` [R]
- **216,164 bytes, 2,126 rows**, tab-separated. Per row: stratum, id, `compare`, kind, nodes, cost, distance, node digest,
  heights digest (-58), and the tuple digest. **No coordinates.**
- **The tuple digest** is a 64-bit FNV-1a over the UTF-8 bytes of `kind|nodes|cost|distance|nodeDigest|heightsDigest`,
  the tuple as `E3Outcome.toField` writes it. That byte form is my choice; E1 says only "the tuple's bits" [I]. It uses
  integer arithmetic only. A Python check outside the JVM recomputed all 2,126 digests and got the same values [C].
- **The head** names the JDK: `java.vendor=Eclipse Adoptium; java.runtime.version=21.0.13+9-LTS; os.arch=amd64` (E2). It
  also names the tuple, the `compare` modes, and each exclusion on its own `# excluded` line.
- **Rows by stratum:** F0 2, F0h 8, R 1,534, DL-lat 59, DL-lon 59, MP64 59, ML5 59, PM5 55, NT 59, CR 59, LB 59, FK 114
  (110 plus the four extra cases).
- **`compare`:** 2,120 rows are `exact`, 3 are `family` and 3 are `outcome`.
  - **`family`:** FK9-0001, FK9-0037 and FK9-0040. These are kind 9's timing voids, logged UNSTABLE in FK's run (-85, -86),
    and they are excluded by name.
  - **`outcome`:** FK7-x1 is compared on its kind. FK7-x2 and FK8-x1 are compared on FK7-c0's tuple, and the test checks
    that they hold it (-80, -85).
- **`R-shortest-025` is not excluded.** At write time it went once more through all six regimes, alone, and was stable:
  all six gave `route|217|5102|5102|…` (`31-expected-e3Strata.log`) [C]. So under -88 it is `exact`. -50's instability
  was R6's, once, and it has not recurred in the two runs since. Its cause is still [?].
- **FK9-0049** (unstable in FK's first run only) was stable in the run on record (`30-fk`). So it is `exact` [C].

### The code [R]
- **`E3Expected.kt`:** the file's form, its reader and writer, the per-stratum counts, and the committed stability tables
  each stratum's R1 was logged in.
- **`strata/E3ExpectedWriteTest.kt`** runs under `e3Strata` only, on the pinned Temurin. It reruns R1 for every case:
  E3's own `R1R2` launch, with a fresh JVM, the case alone, a new thread and no `debug.txt`. It compares each R1 with the
  stratum's committed log, and writes the file only if every case is equal.
  - The cases come from each stratum's work folder (`build/e3/<stratum>/cases.tsv`). These folders are never committed,
    because they hold the ends.
  - Every list except FK's was written before `E3Case.Plan` existed, so it has 9 fields. The writer appends `NONE` to a
    copy of each such list [R].
  - CR gets its crossing profile, as CR's own test sets it.
- **`E3ExpectedFileTest.kt`** runs in `jvmTest`, with no oracle. It reads the file and checks six things:
  - the head's JDK;
  - the counts per stratum;
  - that every tuple digest is its tuple's;
  - that every row equals the R1 its stratum logged (whole tuple for `exact`, kind otherwise);
  - that the exclusions are named, and that every logged-unstable case is excluded;
  - FK's extra cases.
- **E1's live oracle beside the stored values** is not run per push here. `E3ExpectedWriteTest` is that run, and it
  belongs to `e3Strata`, as E1 sets it. At step 6 it becomes the port's comparison [I].

### Written across all strata, and the R1 cross-check [C]
- **The run:** `e3Strata --tests '*E3ExpectedWriteTest*'` on Temurin 21.0.13+9, logged in `31-expected-e3Strata.log`.
  BUILD SUCCESSFUL in 19 min 15 s.
- **The first attempt failed in 3 s, before any case ran.** `E3Case.fromLine` refused a 9-field list. That is the old-list
  fix above. It was one attempt at that symptom.
- **R1 at write time equalled the stratum's logged R1 for 2,126 of 2,126 cases**, every stratum included
  (`31-expected-r1-vs-log-summary.txt`). This is no stop.
- **Checked outside the runner:** a Python read of the written file against the twelve committed stability tables gave
  0 rows unlike their log, 0 wrong digests and 0 logged cases missing.
- **The committed file equals the writer's output byte for byte** (`cmp`).
- **One thing I noticed:** `build/e3/F0h/stability.tsv` is left over from session 20's heights revert (`15-revert-*`): its
  heights digests are all the empty digest. The committed `12-f0h` table is the real run, and both the check and the
  writer read that one. F0h's R1 now equals `12-f0h` [C].

### Seen failing first [C]
- **The prediction** was written and pushed before the run, in `13506f1`
  (`31-expected-revert-prediction.txt`). The scratch revert set F0-inside-long's cost from 7405 to 7406, and was never
  committed.
- **The run:** `jvmTest --tests '*E3ExpectedFileTest*'` (`31-expected-revert-gradle.log`).
  - It had 0 ` e: ` lines.
  - The XML's timestamp, 12:36:49Z, falls after the edit at 12:36:48Z.
  - It gave 6 tests and 2 failures, exactly as predicted:
    - `everyTupleDigestIsItsTuple` named `[F0-inside-long]`;
    - `everyRowIsTheR1ItsStratumLogged` named only "F0 F0-inside-long (exact): stored route|306|7406|…, logged
      route|306|7405|…";
    - the other four passed.
- **The restore** was from the copy saved before the edit, not from git. Afterwards the file was `cmp`-equal to that copy
  and to the writer's output, and `git status` was clean.
- **The full `:brouter:jvmTest` before the code push** ran fresh: `jvmTest` and `jvmTestProcessResources` executed, and
  44 XML files were written after the start (12:35:51Z). It gave 101 tests, 0 failures and 0 ` e: ` lines. That is 95 + 6.

### E1
Before each push, the scan of the staged diff (decimals, `node=`, integer microdegree pairs) read 0 [C].

### CI
Read from the job logs by me (`gh run view --log`) [R]:
- **Run 42218972054 on `7e02bdd`** (the code change) concluded `success`.
- **`jvm`:** 101 tests, 0 failures, in 44 files. All six `E3ExpectedFileTest` methods PASSED. 0 ` e: ` lines in the run's
  log.
- **iOS klibs:** `compileKotlinIosArm64` and `compileKotlinIosSimulatorArm64` both ran, BUILD SUCCESSFUL.
- **Simulator:** 23 tests, 0 failures, in 9 files (**simulator-checked**).
- **arm64 JVM step:** 7 tests, 0 failures, in 2 files. Its gate printed "OK: all 2 classes ran".
- **Run 42218998244 on `13506f1`** changes only the prediction file. This report's own run follows it. I read both for
  status only (see the final message).

### Step 1b: the handoff (twenty-sixth session)
1. **Nothing is running.**
2. **1b's finish line is reached on my side.** What remains is the planner's: the planner reads CI, and brings the
   milestone report below to the owner.
3. **To rewrite the values** (another JDK, or a new case list), run `e3Strata --tests '*E3ExpectedWriteTest*'` and copy
   `build/e3/expected/expected-1b.tsv` into `src/jvmTest/resources/e3/`. Then `E3ExpectedFileTest` holds it to the logs.
   - The writer needs every stratum's `build/e3/<stratum>/cases.tsv` and FK's `build/e3/FK-tiles/`.
   - These exist only in this container. A fresh container must rerun the strata to get them back [R].
4. **For step 10 (iOS):** the file is under `jvmTest`. How iOS will read it, a resource or a generated Kotlin file as
   with `MsunReference.kt`, is for that step [?].

### Draft: 1b's milestone report, for the planner to bring to the owner
**What 1b proved [C]:**
- **The 1.7.10 Java engine** agrees with itself under E3's six runs, on every case of every stratum, on the pinned Temurin
  21.0.13+9 x64 with libm's intrinsics off.
- **Its outcome for every case is now stored** as an expected value, from R1, with no coordinates. A fresh R1 at write
  time gave the same tuple as each stratum's logged run, for all 2,126 cases.
- **A test reads the stored file in every `jvmTest`,** and was seen to fail on a one-entry change.
- **1b ran no port code.** It says nothing yet about the Kotlin port. The port is first compared with these values at
  step 6.

**Each stratum's n and E5's bound.** Each bound applies once the port runs clean against these values, and only to that
stratum's population (generator, tiles, profile, params). Each is one-sided at 95%, 1 − 0.05^(1/n):

| Stratum | n | Bound on a clean run |
|---|---|---|
| R (26 profiles × 59) | 1,534 | under 0.195% pooled; per profile, a defect on ≥ 5% is seen with p ≥ 0.9515 |
| DL-lat, DL-lon, MP64, ML5, NT, CR, LB | 59 each | under 4.95% each |
| PM5 (short, -71) | 55 | under 5.30% (weaker than the 59 E5 set) |
| FK | 110 + 4 extra | constructed, not sampled: each kind reaches its family or not; no rate |
| F0 | 2 | Forager's own check; no rate |
| F0h | 8 | heights cases; no rate |

The total is 2,126 cases: E5's 2,118, plus F0h's 8 and FK's 4 extra cases, minus PM5's 4 short.

**What is excluded:**
- **FK9-0001, FK9-0037 and FK9-0040** are kind 9's timing voids. They are compared by family only, and they are named in
  the file.
- **FK's extra cases** are compared on outcome, not exactly.
- **Nothing else.** `R-shortest-025` was stable at write time and is exact.

**What is unverified [?]:**
- whether the values hold on any other JDK, CPU, or Android's runtime (E2: they are claimed for this HotSpot only);
- why R6 differed once for `R-shortest-025` (-50);
- why FK7-x1's ends read as unmapped from a tile cut at D;
- that FK10's precondition, "the measured run routes", is my own addition, beyond E4 [I];
- every iOS claim, which waits for step 10 and will be simulator-checked only;
- how long the full list takes on the simulator (K).

## Confirmed vs inferred

- **Confirmed by running:** the build, the three JVM tests and both klibs, locally and in CI; the smoke test on the
  `macos-26` simulator; the oracle fetch's refusal of a changed pin; the pin test's bite.
- **Confirmed by reading:** Gradle's checksums; the action SHAs; the licence against upstream; the FK 6 message's source line.
- **Inferred:** that the smoke test, being trivial, would fail if the toolchain could not link or run (its value is the
  count, and the count is 1).
- **Step 1, confirmed by running:**
  - the scratch tiles against the handoff's hashes;
  - the four tiles and F0 in the 1.7.10 oracle;
  - the harness refusing four damaged copies;
  - F0's route against Forager's committed result;
  - the pin file through `tools/fetch-tiles.sh`, and its refusal of a size off by one.
- **Step 1, confirmed by reading:** where the engine checks the lookup version (`PhysicalFile.java:102-105`,
  `NodesCache.java:70-71,377`), and why the draft's count read zero (`OsmFile.java:133-148`).
- **Step 1, inferred:**
  - the geography of three routes' ends;
  - that GitHub refuses two assets of one name in a release;
  - that `workflow_dispatch` needs the workflow on the default branch;
  - that a draft release is not public. That last one is GitHub's documented behaviour as I remember it, and was not
    read this session.

## Could not determine

- Whether Actions was enabled before the first push (the settings path is closed to this session); the run answered it.
- Why KGP 2.3.10 printed no Xcode-version warning with Xcode 26.0.1.
- Which simulator device and runtime ran the smoke test.
- Step 1:
  - whether the proxy's upload gate passes a 114 MiB asset upload to `uploads.github.com`, and whether credentials are
    supplied there;
  - the workflow token's default permissions on Forager-iOS (the proxy refuses the path);
  - what ODbL asks beyond 4.2 of a re-host (not examined, as in Phase 1);
  - whether any captured tile carries `estimated_crossing_class` (step 1b's coverage run).
- Part A:
  - whether any session type may create a release through the proxy, and which;
  - whether the proxy would refuse an asset upload to a draft the owner made (not tried);
  - what kind of credential the proxy supplies (inferred to be a GitHub App user token from the response headers).
- Step 2a close:
  - how `PortLoopCheckBite-out.txt` lost its first three mismatch lines;
  - which exception Kotlin/Native throws on the port's out-of-bounds reads (option (ii) would depend on it).

## Premises that were wrong

- Forager-iOS `main` was `c5c9411`, not `3fc5cef`; the move is RECORD -25 and the amendment only, so no premise changes.
- Premise 4 expected the tile sizes to have changed since part 4's 03:54Z listing. They had not: all four equal the
  listing's bytes, and `Last-Modified` (01:03:01Z) is earlier than the listing. Not a stop; it means the captured files are
  the ones part 4 sized.
- My own: FK 6's oracle message (above).
- **My own, from the first session: the handoff harness's decode count** read zero on every tile, because it copied
  `checkFileIntegrity`'s `reallyDecode = false` (step 1, above). Its draft also did not compile. Neither would have
  passed off a bad tile as good, since the open, the integrity check and the route did not depend on the count. But the
  count was written as the evidence that a pass was "not a pass on no data", and it could not have shown that.
- **Step 1's pricing of route A, which I relied on in Part A:** "admin: true through this session's proxy" and
  "`uploads.github.com` reachable" were read with read-only calls. They could not see a write policy at the proxy, which
  refuses release creation for this session type. A check that never sent a write could not have failed on one. Step 1
  listed the 114 MiB upload as unknown, but not the create.
- **My own:** the first create request lacked `Content-Type: application/json` (the 415).
- **Step 2a close, the ruling (RECORD -33):** "a second case ... runs to the position bound. This reaches the end of the
  byte array and `fillBuffer`'s end-of-array branch." At that bound, the oracle's `idx` peaks at 4,084 of 4,095, and the
  branch is never reached. Past the bound, Java's unguarded reads throw within 0 to 15 ops. Built as worded, and reported.
- **Step 2a close, my own P5:** run A's predicted failure (op 1,775) came from a log that lacked its first three lines.
  The real first mismatch is op 206, on the oracle and in the re-run harness alike.

## Decided beyond scope

- **The oracle's JDK in CI:** Temurin 21.0.13+9, pinned exactly in `setup-java`. Item E2 leaves the vendor to Phase 2.
  Phase 1 measured on Ubuntu's OpenJDK 21.0.12.1, which apt cannot pin once superseded. No expected value depends on it yet;
  it must be settled before E3's runs write any.
- **The oracle is fetched by the build,** not by a separate CI step as G's sketch drew it: D's "a Gradle task in the port's
  build", using `tools/fetch-pinned.sh`, so one checker refuses the zip and the jar, locally and in CI.
- **`tools/fetch-oracle.sh`** holds the oracle's pins, as Forager's `fetch-brouter.sh` does; the test holds a second copy of the
  jar's SHA-256.
- **`.gitignore` at the root,** a file outside the amendment's listed writes, so Gradle's outputs cannot be committed.
- **`tools/junit-counts.py`,** so counts come from the XML in CI's log.
- **The JVM tests run with `-XX:+UnlockDiagnosticVMOptions -XX:-UseLibmIntrinsic`** from step 0 (RECORD -24); the unlock flag is
  required (HotSpot refuses the flag alone).
- **okio is an `api` dependency,** because `okio.Path` is in the entry point's signatures.
- **Step 1:**
  - **The pin file's place and form:** `tools/tile-pins.tsv`, tab-separated. Item I names no path.
  - **F0's asset name** `forager-fixture-W75_N40.rd5`, and its fetch path `f0/W75_N40.rd5`.
  - **`tools/fetch-tiles.sh`,** a reader of the pin file. It is not wired into CI or Gradle until a release URL exists.
  - **The notices' place,** `tools/tile-release/`: the README asset and the release body.
  - **F0 run through the harness** as a control; E4 names only the four tiles.
  - **A fourth bite case** (mid-file), so the check covers what `checkFileIntegrity` adds beyond the open.
  - **Three routes' ends chosen from geography,** not from the tiles' nodes.
  - **Read-only GitHub API queries** to price the routes: the repository's permissions and release count, and Forager's
    visibility. A fourth, on the workflow token, was refused by the proxy.
  - **A release-staging folder in scratch,** with the tiles hard-linked, so the six assets sit in one place for route A.
- **Part A:**
  - **Resending the create once with `Content-Type: application/json`** after the proxy's 415. That was my malformed
    request, not a refusal of the release. I judged that fixing the header was not working around a refusal. Nothing
    existed before the resend.
  - **`curl` against the REST API, not `gh release create`:** each call and its reply are logged on its own.
  - **The Part A logs' folder,** `2026-10-29-brouter-port-phase2/partA/`.
- **Amendment 4:**
  - **`tools/tile-release/` made into templates,** a write outside the amendment's list. The README's table has to be
    written by the run, and the notes' "2026-10-29" would be wrong on another day.
  - **`TileCheck` exits 1 on a failed check.** Step 1's copy only printed, and "any failure fails the run" needs an exit
    code.
  - **F0 run as the control,** as in step 1. The amendment names only the four tiles.
  - **A stale check on push:** the dry run fails if the branch's tools differ from `TOOLS_SHA`'s. Otherwise a harness push
    would dry-run the old tools and pass. The trigger paths are the workflow and `tools/tile-check/**`, the amendment's
    "check harness".
  - **A read-back of the five data files at the end of `capture`,** through `tools/fetch-tiles.sh`, so the dry run
    exercises the checker `publish` uses.
  - **The tag check covers drafts:** it refuses a tag ref or any release, drafts included, of that name.
  - **Also chosen:**
    - `concurrency: publish-tiles` on `publish`;
    - the artifact kept 7 days;
    - each tile's headers kept in the artifact, not the release;
    - the release title "BRouter test tiles, <date>";
    - `persist-credentials: false` on both checkouts;
    - no `--retry` on the brouter.de downloads, so each is fetched once;
    - download-artifact `v8.0.2`, `9000827c`, read with `git ls-remote` on 2026-10-29.
- **Step 2a close:**
  - each case prints its per-seed op count and final position, so CI's log carries the counts;
  - the bite was run once per case, each on its own line, rather than once on both;
  - `runner/bite-check.sh` is new; `revert-check.sh` is unchanged;
  - case 2's name, and its comment saying it stops short of the branch;
  - case 2 was built as the ruling words it, although its stated purpose does not hold (see that section).
