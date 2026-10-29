# BRouter port, Phase 2: steps 0 and 1 (dispatch 2026-10-28-01, Amendment 2)

Written by the team's primary coder (first run, not yet named), 2026-10-29, for Cedar Datum, the planner. The amendment is
`prompts/preserved/2026-10-28-01-brouter-port-amendment-2.md` on `main` at `c5c9411`. Phase 1's report,
`docs/audits/2026-10-28-brouter-port-verify.md` at `20e1f92`, is the plan; "item I" below is its Phase 2 order.

**Status (2026-10-29, second session):** steps 0 and 1 done. The tiles are checked and pinned, and the release routes are
below. **No release exists:** the release waits for the owner's word, and steps 1b onward wait for the go.

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
