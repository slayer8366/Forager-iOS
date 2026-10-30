# Step 5a (`mapaccess`, the file layer): report

Dispatch 2026-10-28-01, Amendment 3 Part B; RECORD -48. Secondary coder (unnamed), branch `brouter-port-5a`, cut from
`origin/brouter-port` at `2ada78c`. Evidence in `2026-10-30-brouter-port-step5a/`.

**Status (2026-10-30, second session): done.** Item I's three parts were done in the first session: tests seen failing
first, the port, the revert. The second session read CI on `9f41799`, the planner's merge of this branch into
`brouter-port` (RECORD -49); its counts are below. The first session handed off partial at the planner's request, before
reading CI.

Tags: [R] read, [C] computed (log in the evidence folder), [I] inferred, [?] could not determine.

## What was done
- **Tests first** (`26d0224`): stubs and 10 tests in `jvmTest/.../mapaccess/` (header, sub-indexes, integrity;
  FK 6, 7, 8 at ten cases each; `checkDataFile`; `StorageConfigHelper`). Run 1: 9 of 10 failed on `NotImplementedError`,
  and fk8 failed on its own count (E170_S50 has 3 squares with data; the draft assumed 5). The test was fixed. Run 2:
  10 of 10 failed on `NotImplementedError`, 0 compile errors [C: `1-tests-over-stubs*.txt`].
- **The port** (`6770b86`): `ReadOnlyFile` (okio `FileHandle`), `PhysicalFile` (and `checkFileIntegrity`), `OsmFile`,
  `StorageConfigHelper`, `NodesCache`'s file half and `checkDataFile`. 91 of 91 JVM tests, 0 compile errors [C:
  `2-port-jvmtest.txt`]. One token (`!!`) was removed after that run and before the commit; the forward re-run after
  the revert covers it: 91 of 91, 0 compile errors [C: `4-revert-result.txt`].
- **Revert** (prediction `fe0546c`, pushed before the run): `throw EOFException()` became `break` in `readFully`.
  Exactly as predicted: 91 tests, 1 failure, `fk7DataCorruptShort` at cut 1, expected `EOFException`, actual
  `file of size 1 too short, should be 112`. The log had 0 compile errors and the results folder was emptied first. The
  file was restored from a saved copy, and the forward line is present [C: `3-revert-prediction.md`,
  `4-revert-result.txt`].

## Findings
- **FK 7 and 8 do not reach `NodesCache.java:202` at the file layer.** `:197-198` catches `IOException` and rethrows
  `RuntimeException(re.getMessage())`, so the class name is not in the text [R: Forager's `third_party` copy; C: the
  oracle's own outcomes in `2-port-jvmtest.txt`, e.g. `RuntimeException: top index checksum error`]. This corrects the
  Phase 1 report's [I] at its FK note. What `RoutingEngine` does with it is not traced [?].
- **Two short copies pass both engines' header checks:** a cut exactly at `fileIndex[24]` ("old format o.k.") and a
  cut losing only the last byte [C]. `checkDataFile` returns null for both, on the oracle as on the port.
- **Sub-index flips pass `checkDataFile`** on both engines. Only the integrity check and the engine's read catch them [C].
- **A header cut gives the engine a null message:** `RuntimeException: null` on both engines [C].

## Calls beyond the amendment
- **`NodesCache` is split.** Its graph half (`obtainNonHollowNode`, `expandHollowLinkTargets`,
  `hasHollowLinkTargets`, `getStartNode`, `getGraphNode`, `matchWaypointsToNodes`, `preloadPosition`) needs 5b and 5c
  types, and waits for them.
- **`OsmNodesMap` and `WaypointMatcherImpl`** are declaration-only stubs.
- **`OsmFile`'s decode** is `TODO("DirectWeaver, item I step 5b")`. Its `MicroCache2` branch is a named unsupported.
- **`checkFileIntegrity`** (not on T2's list) is ported for the integrity test.
- **`StorageConfigHelper`** catches only `IOException`, not every `Exception`.
- **`posIdx` is `internal`,** for the test.
- **The FK cases are file-layer cases** (`checkDataFile`, the integrity check, `loadSegmentFor`). The route-level
  messages wait for step 8.

## The two conditions
- **-38:** 5a decodes no cell. `checkFileIntegrity` passes `reallyDecode = false`, and every `loadSegmentFor` probe is
  on an empty square or fails before the decode [R: the tests]. The noisy-bit count is left to 5b.
- **-34:** `fillBuffer` is called only from the decode methods (`BitCoderContext.kt:72, 97, 174, 184, 191` [R]), and
  5a calls none of them. Its end-of-array branch cannot run in 5a [I]. Left to 5b.

## CI, read by the second session
Run 41010445171 on `9f41799` (workflow `brouter.yml`, branch `brouter-port`), both jobs `success`. Read from the two job
logs through `gh api .../actions/jobs/<id>/logs`; the lines are copied with their numbers into
`2026-10-30-brouter-port-step5a/5-ci-9f41799.txt` [C].
- **`jvm`** (job 123914875566, `ubuntu-24.04`): **91 tests, 0 failures, 0 errors, 0 skipped, in 41 files** (log line 701).
  That is the 81 before 5a plus 5a's 10, as RECORD -49 reads it on `2a8ba5e`.
- **Both iOS klibs:** `compileKotlinIosArm64` (line 642) and `compileKotlinIosSimulatorArm64` (line 646) ran, and the step
  ended `BUILD SUCCESSFUL in 40s` (line 648). **0 lines with ` e: `** in the whole log. Each klib printed one `w:`, the
  existing parameter-name warning at `BExpressionContextWay.kt:16`.
- **Simulator** (job 123914875841, `macos-26`): `iosSimulatorArm64Test` ran (line 353); **23 tests, 0 failures, in 9
  files** (line 377). **Simulator-checked.** None of the 23 is a 5a test (all ten are `jvmTest`), so the file layer itself
  has still not run on iOS.
- **The arm64 JVM step:** outcome `success` (line 578); **7 tests, 0 failures, in 2 files** (line 620); the gate line,
  **"OK: all 2 classes ran"** (line 623), after `CheapLogicRunTest` 5 of 5 and `CheapProductionRecordTest` 2 of 2.
- These are my own readings; they equal the planner's on `2a8ba5e` (91, 0 errors, 23, 7). `9f41799`'s tree is
  identical to `2a8ba5e`'s (both `fce6203`, `git rev-parse <commit>^{tree}` [C]), so CI on `26d0224`, `6770b86` and `fe0546c` was not read separately.

## Unverified
- **iOS messages:** okio's FileNotFound text on Native, and `EOFException`'s `simpleName` [?].
- **iOS file descriptors:** a failed `PhysicalFile` constructor leaves its handle open, as Java does. The JVM's
  `RandomAccessFile` cleaner may reclaim it; nothing on iOS will [I].

## Handoff
Nothing remains for 5a. Its file layer runs on iOS first at step 10 (RECORD -49). Step 5b continues on
`brouter-port-5b`, with its own report, `2026-10-30-brouter-port-step5b.md`.
