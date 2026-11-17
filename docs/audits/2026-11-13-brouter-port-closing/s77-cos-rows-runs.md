# Session 77: the cosine rows' group, its runs

Coder Plumb Ledger, session 77. Branch `brouter-port-5b`, base `cf83cf3b7ecf4202ec9918be1dcb0ec63c2574dc` (checked:
`git fetch origin brouter-port-5b`, then `git rev-parse HEAD origin/brouter-port-5b` both gave it; `git status --porcelain`
printed nothing). This is dispatch 7 of session 72's order of work (`s72-cosine-rows-tiles-prebuild.md` §6, item 7).
Marks: [C] confirmed by a run or a read named here, [I] inferred, [U] unverified. No coordinate and no node id is written
here.

The question this answers, for the owner: 13 rows of the scale table differ by 1 ulp in `kx` between the oracle's table and
the production (msun) table; five were covered; these 8 (474, 488, 502, 663, 675, 1046, 1053, 1079) were not. Does the
difference change any route on them?

## 1. What is there now [C, read at `cf83cf3`]

- **Logic run, `RoutingEngineStep9Test`** (`brouter/src/jvmTest/.../router/RoutingEngineStep9Test.kt`): per stratum, the
  generator's draws in order, taken where the id is a row of `expected-1b.tsv`, within `E3Targeted.CAP` draws (`:71-82`);
  the oracle in this JVM held to the row's tuple digest (`:84-85`); the port through `RoutingEngineF0Test.port`
  (`RoutingContext(OracleLibm)`, `RoutingEngineF0Test.kt:52-55`) compared on six fields (`:86-95`); one test per stratum.
- **Production run, `RoutingEngineProductionRecordTest`** (`.../router/RoutingEngineProductionRecordTest.kt`): gated on
  `BROUTER_PRODUCTION_RECORD` (`:67-71`, `:275`); per group the oracle in this JVM and `portMsun` (`RoutingContext(MsunLibm)`,
  `:278-281`). **What it gates:** only that every case gave an outcome (`:264`), that cases equal the group's rows (`:265`),
  and that equal plus the split add up to the cases (`:266`). A difference from the oracle is counted and printed (`:268-271`),
  never failed. The `jvm-production` job runs it with `--tests '*RoutingEngineProductionRecordTest'` (`brouter.yml:284`).
  `portMsun` hard-codes `MsunLibm` (`:281`), so no seam can be passed to it.
- **The simulator:** `LbEngineCommonTest` calls `Step10Cases.check("LB", <nt folder>/lb-cases.tsv, LbExpected.ROWS)`
  (`LbEngineCommonTest.kt:13-14`). `Step10Cases.check` runs `EngineRun.run(...)` (`Step10Cases.kt:96`), which is
  `runTrack(...)` with its default `libm = MsunLibm` (`EngineRun.kt:158-159, 170-172`): **the simulator runs production and
  holds it to the oracle's tuples exactly** (`Step10Cases.kt:98-108`). `check` takes no seam; `runTrack` does.
  A case line's eighth field is its tiles folder (`Step10Cases.kt:94`; written by `NtCasesWriteTest.writeCases`, `:151`).
- **Its constant's holder:** `Step10CommonDigestTest.constantEqualsTheFilesRows` (`:77-92`) reads `expected-1b.tsv` only
  (`:14`), so it cannot hold a constant to `expected-cosrows.tsv`.
- **The gate:** `SIMULATOR_CLASSES`, 24 names (`brouter.yml:61-85`); `tools/junit-ran.py --simulator`.
- **The tiles on the simulator:** `ios-smoke` fetches `fetchCosTiles` (`brouter.yml:108`) and exports
  `SIMCTL_CHILD_BROUTER_COS_TILES_DIR` (`:123`), which no test reads yet (`:120-121`). The JVM writer step and the simulator
  run on one runner, so an absolute folder in a case line's eighth field is readable there, as F0h's and FK 7/8's are [I,
  by analogy; to be confirmed on the simulator].
- **The 13 (c) rows** are listed in `CheapProductionRecordTest.kt:15`; `CosRowsGenerator.cRows` computes them (`:32-36`).

## 2. Design

All new code in `brouter/src/jvmTest` or `brouter/src/commonTest`; nothing under `commonMain`.

| file | what |
|---|---|
| `jvmTest/.../oracle/e3/CosRowsCases.kt` | the 472 cases rebuilt: per row, `CosRowsGenerator.draws(row, COS<row>, PROFILE)` in order, taken where the id is a row of `expected-cosrows.tsv`, within `E3Targeted.CAP` (as `RoutingEngineStep9Test.run` takes them); and the port run with a given seam (`RoutingEngineF0Test.port`'s calls, `RoutingContext(libm)`) |
| `jvmTest/.../router/RoutingEngineCosRowsTest.kt` | **step 1**, the logic run (`jvm`) |
| `jvmTest/.../router/RoutingEngineCosRowsProductionRecordTest.kt` | **step 2**, the production run (`jvm-production`) |
| `commonTest/.../tiles/CosRowsEngineCommonTest.kt`, `CosRowsExpected*.kt`, `CommonCountingLibm.kt` | **step 3**, the simulator class, its constant, the common counting seam |
| `jvmTest/.../tiles/CosRowsCommonDigestTest.kt` | step 3, the constant held to the file |

**Step 1, logic.** One test over the group. Per case: the oracle in this JVM (`E3CaseRun.run`) held to its row's tuple digest
(as Step 9, so the rebuilt case is shown to be the selected one); the port on `CountingLibm(OracleLibm)`, one counter per case,
compared with the row on all six fields and the tuple digest (`exact`). Per case: its own row is among the rows read. Group: the
union of rows read, intersected with the 13 (c) rows, equals the 8. All differences are collected and printed per row before
one assertion; a failure names ids and fields, never ends.

**Step 2, production.** A class of its own beside `RoutingEngineProductionRecordTest`, not a method in it: its `portMsun`
cannot take a seam and its `record` prints no ids, so either would be a change threaded into working code. Same gate variable
(`RoutingEngineProductionRecordTest.GATE`). Per case: the oracle in this JVM, the port on `CountingLibm(MsunLibm)`. It
asserts what the existing record asserts (every case an outcome, cases equal rows, equal plus differing add up) and the same
two row assertions on msun's table; a difference from the oracle is recorded, never failed. Prints per row: cases, equal,
differing, the per-field counts and the existing record's split, in the existing record's column order; and per differing case
its id and the fields that differ. The `jvm-production` job's Gradle line gains `--tests '*RoutingEngineCosRowsProductionRecordTest'`
(its current filter does not match the new name).

**Step 3, simulator.** `CosRowsEngineCommonTest` through `Step10Cases.check("COS", <nt folder>/cos-cases.tsv,
CosRowsExpected.ROWS, ...)`. The cases reach it as F0h's do: `NtCasesWriteTest` writes `cos-cases.tsv` beside the NT file with
the eighth field `TestTiles.cosRows`' absolute path (no workflow change for the tiles: `fetchCosTiles` already runs in
`ios-smoke`). Counting: `Step10Cases.check` gains one parameter, `libm: LibmSeam = MsunLibm`, passed to `EngineRun.runTrack`
(every existing caller unchanged, by the default); the common class passes a `CommonCountingLibm(MsunLibm)` and reads it per
case in `onCase`. The constant is generated from `expected-cosrows.tsv` (ids, ints and digests only), split across files under
400 lines; `CosRowsCommonDigestTest` holds it to the file in file order, and holds the common class's 13 (c) rows to
`CosRowsGenerator.cRows`. `SIMULATOR_CLASSES` gains the class (25).

**The decision this design cannot make (a stop point).** The simulator holds msun's result to the oracle's tuple exactly. If
step 2 finds a case where production differs from the oracle, the simulator class as specified would fail on exactly that
case. Whether to hold the simulator to the oracle's tuples (and let it fail), to a production constant, or otherwise, is the
planner's. So step 3 is built only if step 2 finds 0 differences; otherwise I stop after pushing steps 1 and 2 and report.

**Step 4, plants** (each with `revert.sh` or a scratch copy for the production gate; never committed):
- logic: `OracleLibm.scales` returning a changed `kx` at row 474 (one exact edit in `OracleLibm.kt`). Predict: COS474's cases
  differ (distance at least) and the test fails naming COS474 ids only; the other 7 rows equal.
- production: `MsunLibm.scales` returning a changed `kx` at row 474 (one exact edit in `LibmSeam.kt`, scratch only), the run
  with the gate set. Predict: COS474's equal count drops; the other rows' counts unchanged; the test passes (it records).
- simulator constant: one digest in `CosRowsExpected` changed by one hex digit. Predict: `CosRowsCommonDigestTest` fails naming
  that row; the class's other test passes.

## 3. Predictions (before any run)

- **P1, logic:** 472 of 472 equal to `expected-cosrows.tsv` on the whole tuple; the oracle in this JVM equal to its row in 472;
  each case reads its own row; the group's (c) rows are exactly the 8 (s76 counted the same on this table during selection).
  Locally about 1.5 to 3 minutes.
- **P2, production:** every case reads its own row on msun's table too and the group reads exactly the 8. Equal to the oracle:
  I expect 59 of 59 in most or all rows, as LB's 59 were (`docs/audits/2026-11-12-brouter-port-step9/31-production-run.txt`);
  where a case differs, cost or distance only, not the route [I: a 1-ulp `kx` moves a segment's length by about 1e-16 of it,
  and cost and distance are ints]. I give that low confidence: this is the measurement.
- **P3, the simulator:** 472 cases at LB's 28 s per 59 is about 4 minutes, with row 1046 slower (s72 §5: 5 to 8 minutes);
  `ios-smoke` about 25 to 30 of its 45 minutes.
- **P4, CI on the last code commit:** `jvm` `TOTAL:` 257 + 1 logic + 1 production (its gated early return) + 2 digest
  tests + 1 common class (prints "unset" on the JVM) = **262**; `jvm-production` **3**; `ios-smoke` **61** and 7, `OK on the iOS
  simulator: all 25 classes ran`; `ios-memory` 3, 1, 1. 0 compile errors in each. Simulator results are simulator-checked.
  If step 3 is not built, `jvm` 259 and `ios-smoke` unchanged.
- **P5:** `scan.sh` prints 0 on every commit; `expected-cosrows.tsv`, `expected-1b.tsv`, `tools/tile-pins*.tsv`,
  `publish-*.yml` and `commonMain` unchanged against `cf83cf3`.

## 4. Results

(Written as they come.)

### 4.1 Logic run, locally [C]

`./gradlew --max-workers=2 :brouter:jvmTest --tests '*RoutingEngineCosRowsTest'` at the step 1 commit `3c924cd`, on the
container's OpenJDK 21.0.12 (not CI's Temurin): PASSED, 0 lines matching `^e: ` or ` e: `, its XML fresh. 23.3 s.

| row | cases | equal to `expected-cosrows.tsv` on the whole tuple | differing | (c) rows read (oracle's table) |
|---|---:|---:|---:|---|
| 474 | 59 | 59 | 0 | [474] |
| 488 | 59 | 59 | 0 | [488] |
| 502 | 59 | 59 | 0 | [502] |
| 663 | 59 | 59 | 0 | [663] |
| 675 | 59 | 59 | 0 | [675] |
| 1046 | 59 | 59 | 0 | [1046] |
| 1053 | 59 | 59 | 0 | [1053] |
| 1079 | 59 | 59 | 0 | [1079] |

The oracle in this JVM was off its stored row in 0 of 472. Each case read its own row (59 of 59 per row); the group's (c)
rows read: exactly `[474, 488, 502, 663, 675, 1046, 1053, 1079]`. No port finding. P1 held (time: 23 s, under my 1.5 to 3
minutes).

### 4.2 Production run, locally [C]

`BROUTER_PRODUCTION_RECORD=1 ./gradlew ... --tests '*RoutingEngineCosRowsProductionRecordTest'` at `72a9826`: PASSED, 0
compile-error lines under both patterns, XML fresh, 24.1 s.

| row | cases | equal to the oracle (six fields) | differing | fields differing | (c) rows read (msun's table) |
|---|---:|---:|---:|---|---|
| 474 | 59 | 59 | 0 | none | [474] |
| 488 | 59 | 59 | 0 | none | [488] |
| 502 | 59 | 59 | 0 | none | [502] |
| 663 | 59 | 59 | 0 | none | [663] |
| 675 | 59 | 59 | 0 | none | [675] |
| 1046 | 59 | 59 | 0 | none | [1046] |
| 1053 | 59 | 59 | 0 | none | [1053] |
| 1079 | 59 | 59 | 0 | none | [1079] |

Oracle off its row: 0 in every row. **On these 472 cases the 1-ulp `kx` difference changes no route, no cost, no distance,
no digest.** Every case read its own (c) row on msun's table, so the differing value was read in every case. P2 held.
The CI run of the same class on Temurin is in §4.6.

**What the record gates** (`RoutingEngineProductionRecordTest.kt:264-266`, repeated in the new class per row): every case
produced an outcome, the cases equal the rows (59), equal plus differing add up. It never fails a difference. The new class
also asserts the two row assertions on msun's table, and that every id of the file ran.

### 4.3 The simulator class, on the JVM [C]; on the simulator, §4.6

Because §4.2 found no difference, step 3 was built as designed (the stop point of §2 did not arise).
- `NtCasesWriteTest` with the variable set to a scratch path: `step10 COS cases: 472 of 472 stored ids built; 472 in a
  folder of their own`; the file has 472 lines, all 8 fields.
- `CosRowsEngineCommonTest` on the JVM with that file (the same code the simulator runs, on the JVM's msun port):
  `step10 COS engine: 472 cases in the file; 472 of 472 stored ids; 0 not stored []; equal on all six fields and the tuple
  digest 472; mismatches 0; missing 0; 11 s`; each row's (c) rows read its own; group exactly the 8.
- `CosRowsCommonDigestTest`: 2 tests passed; `472 of 472 equal to expected-cosrows.tsv in file order`.
- `LbEngineCommonTest` and `FkEngineCommonTest` (8 tests, trailing-lambda callers of `check`) passed after the
  `Step10Cases.check` change.
- `compileTestKotlinIosSimulatorArm64` here: the first attempt failed with 7 `^e: ` lines (`toSortedSet` is JVM-only, in
  `CommonCountingLibm` and the common class); fixed before the commit, then 0 under both patterns. The first JVM compile had
  also failed (6 `^e: ` lines): the new `libm` parameter placed after `onCase` stole the trailing lambdas of
  `MemoryEngineCommonTest` and `REngineCommonTest`; moved before `onCase`.

### 4.4 Plants [C]

Each with `revert.sh` (copy saved, one exact edit, the class run, restored from the copy, `cmp` equal, `^e: ` count 0, fresh
XML); I also counted ` e: ` (0 each) and checked `git status` afterwards (only my uncommitted files, or clean).

| plant | file (scratch, never committed) | result |
|---|---|---|
| logic table: `kx` × 1.001 at row 474 | `OracleLibm.kt` | 1 test, 1 failure: `the port on the oracle's table against expected-cosrows: 52 mismatches: [COS474-0001: [cost, distance, tupleDigest] ...`. COS474 7 equal, 52 differing (35 cost+distance, 12 also heights digest, 4 also node digest, 1 all three); the other 7 rows 59 of 59; oracle off row 0. |
| production table: `kx` × 1.001 at row 474 | `LibmSeam.kt` (`MsunLibm.scales`) | 1 test, 0 failures (it records): `COS474 \| 59 \| 7 \| 52 \| 0 0 52 52 5 13 \| 0 47 5 0 \| 0`; the other 7 rows unchanged at 59 equal. Restored file's SHA-256 checked equal to before. |
| simulator constant: COS474-0001's tuple digest last digit 8 to 9 | `CosRowsExpectedA.kt` | 2 tests, 1 failure: `cosRowsConstantEqualsTheFilesRows`: `the cosine rows' constant against expected-cosrows.tsv, in file order expected:<[[COS474-0001, ...`; printed `471 of 472 ... differing [COS474-0001]`; the other test passed. |
| logic row count: row 474 never recorded | `CountingLibm.kt` | 1 failure: `row 474: cases that did not read row 474 on the oracle's table: [COS474-0001, ...]` and `the group's (c) rows read on the oracle's table are [488, ...]: missing [474], extra []`; port mismatches 0. |
| simulator row count: row 474 never recorded | `CommonCountingLibm.kt` | 1 failure: `cases that did not read their own row on msun's table expected:<[]> but was:<[COS474-0001, ...`; COS474 `own row read by 0`. |

The 1.001 factor is a deliberate large plant (0.1%, about 2e12 ulps), chosen so it must move routes; it shows the comparison
bites, not what 1 ulp does. The production plant ran with `BROUTER_PRODUCTION_RECORD=1` set in my shell, which `revert.sh`'s
Gradle inherits; that it ran, not returned early, is shown by its per-row lines.

### 4.5 What did not move [C]

`git diff --stat cf83cf3 -- brouter/src/commonMain brouter/src/jvmTest/resources tools .github/workflows/publish-*.yml`
printed nothing: `expected-cosrows.tsv`, `expected-1b.tsv`, `tools/tile-pins*.tsv`, `publish-*.yml` and `commonMain` are as
at `cf83cf3`. `scan.sh <commit>^ <commit>` printed 0 on every commit of this session. P5 held.

### 4.6 CI on the last code commit, `31939ed` (run 44020606098) [C]

Logs read with `gh api .../actions/jobs/<id>/logs` into the scratchpad (`s77/ci-<job>.log`). Compile errors counted under
`^e: ` and ` e: ` (CI lines carry a timestamp, so ` e: ` is the pattern that can match there).

| job | conclusion | `TOTAL:` lines | gate lines | compile errors (`^e: ` / ` e: `) |
|---|---|---|---|---|
| `jvm` (133828915912) | success | `tests 262, failures 0, errors 0, skipped 0 in 116 files` | (none in this job) | 0 / 0 |
| `jvm-production` (133828916256) | success | `tests 3, failures 0, errors 0, skipped 0 in 2 files` | (none) | 0 / 0 |
| `ios-smoke` (133828916149) | success | `tests 61 ... in 30 files`; `tests 7 ... in 2 files`; each `failures 0` | `OK on the iOS simulator: all 25 classes ran`; `OK: all 2 classes ran` | 0 / 0 |
| `ios-memory` (133828916159) | success | `tests 3`, `tests 1`, `tests 1`, each `failures 0` | `OK on the iOS simulator: all 3 classes ran (.../ios-memory-groups)`; `OK on the iOS simulator: all 1 classes ran (.../ios-r-fallback)` | 0 / 0 |

No 429 and no download failure in any job. P4 held exactly (262, 3, 61 with 25 classes, 3/1/1).

- **`jvm`, on Temurin 21.0.13+9:** `cosrows logic: 472 cases; oracle in this JVM off its row 0 []; port mismatches 0; 23.4 s`;
  group `[474, 488, 502, 663, 675, 1046, 1053, 1079]`; both production classes printed their "unset" line (2 lines).
- **`jvm-production`, on Temurin:** every row `59 | 59 | 0 | 0 0 0 0 0 0 | 0 0 0 0 | 0`; `472 cases, 0 differing, in 32.0 s`;
  every row's own row read on msun's table, group exactly the 8. The existing record's 19 groups unchanged (all equal).
- **`ios-smoke`, simulator-checked:** the writer printed `step10 COS cases: 472 of 472 stored ids built; 472 in a folder of
  their own`; `CosRowsEngineCommonTest` on `iosSimulatorArm64`: `step10 COS engine: 472 cases in the file; 472 of 472 stored
  ids; 0 not stored []; equal on all six fields and the tuple digest 472; mismatches 0; missing 0; 159 s`; each row read its
  own row (59 of 59), group exactly the 8. The new tiles reached the simulator through the case lines' eighth field with no
  build or workflow change for the tiles. Job wall clock 05:01:34 to 05:23:12, 21.6 of 45 minutes. P3 held (159 s, under
  my 4 minutes).
- **`ios-memory`:** its writer also wrote the 472 cases (it runs the whole writer); its gates unchanged.

## 5. The answer for the owner

On the 472 cases, 59 in each of rows 474, 488, 502, 663, 675, 1046, 1053 and 1079, every one of which read its own row of
the table: the port on msun's table gives the oracle's kind, nodes, cost, distance, node digest and heights digest in 472 of
472, on the JVM (`jvm-production`, Temurin) and on the iOS simulator (simulator-checked). The 1-ulp `kx` difference changed
no route on these cases. This is a sample of 59 per row, not every route through those rows.

## 6. Confirmed vs inferred

- [C] Every count and message in §4, from the local logs and XML named there and the CI logs of run 44020606098.
- [C] Each case read its own (c) row on both tables and on the simulator; the group read exactly the 8 (c) rows.
- [C] The plants bite (§4.4), each failing or moving only on row 474.
- [I] That the oracle reads the rows the port reads: the oracle's own reads are not counted (as s76 §2.3).
- [I] `CommonCountingLibm` is not thread-safe; that the engine reads the table on the calling thread only is inferred from
  `EngineRun.runTrack` calling `doRun` synchronously, not traced into the engine. The simulator's counts (59 of 59 per row)
  are consistent with it.

## 7. Could not determine

- Why the 0.1% plant left 7 of COS474's 59 cases equal (both tables). Plausibly short routes whose integer cost and distance
  do not move [I, not traced]; it does not bear on the result.

## 8. Premises in this brief (and the pre-build) that were wrong or did not hold as written

- s72 §4.4 names the folder `build/tiles-cosrows`, a property `brouter.tiles.cosrows.dir` and a task `fetchCosRowsTiles`; session
  75 built `build/tiles-cos`, `brouter.tiles.cos.dir` and `fetchCosTiles` (`brouter/build.gradle.kts:65-67, 154`). Not a
  problem; the names differ.
- s72 §4.5 "a group added to `RoutingEngineProductionRecordTest`": its `portMsun` hard-codes `MsunLibm`
  (`RoutingEngineProductionRecordTest.kt:281`) and its `record` prints no ids, so the counting and per-id fields asked for
  could not be added without changing working code; and `jvm-production`'s filter `*RoutingEngineProductionRecordTest`
  (`brouter.yml:284` at `cf83cf3`) does not match a new class. Built as a class of its own, with a second `--tests` in that job.
- s72 §4.5 "`EngineRun.runTrack` takes a seam, so the common test can count the same way": true of `runTrack`, but
  `Step10Cases.check` calls `EngineRun.run` and takes no seam (`Step10Cases.kt:96` at `cf83cf3`), so "through
  `Step10Cases.check`" with counting needed one parameter on `check`.
- Unstated in the brief, and worth the planner's eye: the simulator holds a **production** run to the **oracle's** tuples
  (`Step10Cases.kt:98-108`). The new class could only be green because production equals the oracle on all 472; had it not,
  step 3 as specified would have failed on exactly the owner's answer. §2 made that a stop point; it did not arise.
- The brief's "see whether the tiles reach the simulator": `SIMCTL_CHILD_BROUTER_COS_TILES_DIR` already existed (session 75)
  and is still read by no test; the eighth field made it unnecessary.
- `revert.sh` counts only `^e: `; I counted ` e: ` beside it on every plant (0 each).

## 9. Decided beyond scope

- Names: `RoutingEngineCosRowsTest`, `RoutingEngineCosRowsProductionRecordTest`, `CosRowsRun`, `CosRowsEngineCommonTest`,
  `CosRowsExpected` (+ `A`, `B`), `CommonCountingLibm`, `CosRowsCommonDigestTest`, `cos-cases.tsv`, stratum label `COS`.
- One test for the whole group in the logic and production classes (one group assertion), rather than one per row.
- The logic run also runs the oracle in its JVM against each row's digest (Step 9's practice), so a rebuilt case is shown to be
  the selected one.
- `Step10Cases.check`'s new `libm` parameter sits before `onCase` (after it, it broke trailing lambdas in
  `MemoryEngineCommonTest` and `REngineCommonTest`; seen as 6 compile errors and fixed before any commit).
- The constant split in two files of 249 lines (the 400-line rule); the 13 (c) rows as a common constant, held on the JVM
  to the table by `CosRowsCommonDigestTest`.
- `CosRowsCommonDigestTest` is a class of its own rather than lines in `Step10CommonDigestTest`, which reads only 1b's file.
- The comment on `SIMCTL_CHILD_BROUTER_COS_TILES_DIR` in `brouter.yml` updated to say the group reaches its tiles by the
  eighth field.
- Plants of 0.1% rather than 1 ulp; two extra plants (row 474 never recorded, in each counting seam).

## 10. Not checked

- Routes beyond these 472 cases through the 8 rows; nor the other 5 (c) rows (1295, 1297, 1310, 1311, 1318), out of scope.
- The production-table plant on the simulator (only on the JVM); the simulator's counting seam was planted on the JVM only.
- A full local `jvmTest`: only the classes named in §4 ran here; the full suite ran in CI (262).
- Whether the engine ever reads the table off the calling thread (§6).

## 11. Correction, 2026-11-17 (session 78, from review 5's S3)

Added after the fact; the text above is left as it was written. Review 5 (S3) found that two sentences above present
"every case read its own row" as a finding of its own. It is not independent: a case is kept only if the oracle's route
lies inside its row's band with both ends in it (`E3CosRowsTest.kt:58, 61-64`), and every link distance reads the row of
the link's mid latitude (`CheapRuler.kt:53, 57`), so any run that returns the selected route reads that row, on any table.
What the row assertion does show is that the engine took the seam it was handed, so the table under test was the one read.
The counting seam records that a row was fetched; it does not record how often (`CountingLibm.reads` has no caller) or
that the fetched value moved any number.

- §4.2, near lines 146-147: "Every case read its own (c) row on msun's table, so the differing value was read in every
  case." **Corrected reading:** each case's route lies inside its row's band, and the port fetched that row from the
  table under test (msun's). The counter does not show how often the row was read, nor that the differing value moved a
  number; that each reader uses the row's `kx` is an inference from the readers, not something this run measured.
- §5, near lines 221-222: "On the 472 cases, 59 in each of rows 474, 488, 502, 663, 675, 1046, 1053 and 1079, every one of
  which read its own row of the table: ..." **Corrected reading:** on the 472 cases, 59 in each of the 8 rows, each
  case's route lies inside its row's band, and the port fetched that row from the table under test. That follows from
  how the cases were selected; it does not strengthen the 472 of 472, which remain a sample of 59 per row.
