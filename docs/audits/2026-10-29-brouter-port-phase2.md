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
