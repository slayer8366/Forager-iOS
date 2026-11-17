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
