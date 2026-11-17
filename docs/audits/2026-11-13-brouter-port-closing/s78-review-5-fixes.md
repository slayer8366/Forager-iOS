# Session 78: review 5's four fixes (S1, S2, S3, N4)

Coder Plumb Ledger, session 78. Branch `brouter-port-5b`, base `e7032774aae2626faeeee8ee2075baf34722a93c` (checked:
`git fetch origin brouter-port-5b`, then `git rev-parse HEAD origin/brouter-port-5b` both gave it; `git status --porcelain`
printed nothing). The review is `docs/reviews/2026-11-17-review-5-cosine-rows.md` in the review clone (read there at
`d89108f`, not copied here). Marks: [C] confirmed by a run or a read named here, [I] inferred, [U] unverified. No coordinate
and no node id is written here.

## 1. Prediction, written before any code commit

No test is added or removed, so the counts of run 44020606098 (head `31939ed`) should not change on the last code commit:

| job | `TOTAL:` lines (tests) | `^e: ` | ` e: ` | gate lines |
|---|---|---:|---:|---|
| `jvm` | 262 | 0 | 0 | none (no gate step) |
| `jvm-production` | 3 | 0 | 0 | new: `OK: all 2 classes ran` |
| `ios-smoke` | 61 (simulator) and 7 (arm64 JVM step) | 0 | 0 | `OK on the iOS simulator: all 25 classes ran`; `OK: all 2 classes ran` (arm64) |
| `ios-memory` | 3, 1, 1 | 0 | 0 | the memory groups and R ran, unchanged |

All four jobs green; the `cosrows production record: COS...` lines' last column 0 in all 8 rows, now asserted.

## 2. Results

Filled in after the code commits (section 3 onward).

## 3. What changed [C, read in the diff]

| commit | fix | files |
|---|---|---|
| `1f5e5da` | S1 | `RoutingEngineCosRowsProductionRecordTest.kt` (per-row `assertEquals(0, offRow, ...)` after the row's line is printed; class comment); `CosRowsRun.kt` (comment says what each run does) |
| `ad66112` | S2 | `.github/workflows/brouter.yml` (`jvm-production`: step "Gating - both production classes ran", `tools/junit-ran.py`, default mode; the script is unchanged) |
| `66b053b` | S3 | `s77-cos-rows-runs.md` (section 11 appended, dated, quoting both sentences; nothing above it edited); `CountingLibm.kt` (comment only) |
| `dee43df` | N4 | `Step10Cases.kt` (`rowsFile`, default `expected-1b.tsv`, message only, placed before `onCase`); `CosRowsEngineCommonTest.kt` (passes `expected-cosrows.tsv`); `NtCasesWriteTest.kt` (`writeCases`' `rowsFile`, default `expected-1b`, message only; the COS call passes `expected-cosrows`) |

## 4. Plants and local runs [C]

Each plant: copy saved, one exact string edited, run, restored from the copy, `cmp` equal, `git diff HEAD` empty (the
S1 plant ran before its commit, so there it was `cmp` against a saved copy of the forward file), 0 lines under `^e: ` and
0 under ` e: `, one fresh XML.

- **S1**, with `BROUTER_PRODUCTION_RECORD=1` as the job sets it. Forward: 1 test, 0 failures, 472 cases, last column 0 in
  all 8 rows, no "is unset" line. Plant: the compared stored digest suffixed for the last stratum only. Failure:
  `COS<last row>: oracle runs in this JVM off the stored row's tuple digest; the rebuilt cases are not the selected ones
  expected:<0> but was:<59>`. The seven rows before it passed, as the plant predicts.
- **S2**, `tools/junit-ran.py` on scratch folders holding the two classes' XML (the cos class's from the S1 plant run, the
  first class's from a run with the gate unset): both present, exit 0, `OK: all 2 classes ran`; the cos XML removed,
  exit 1, `FAIL: 1 of 2 classes did not run: ...RoutingEngineCosRowsProductionRecordTest`; the first removed, exit 1,
  naming `RoutingEngineProductionRecordTest`.
- **N4**, the writer run into scratch (`step10 COS cases: 472 of 472 stored ids built`), then `CosRowsEngineCommonTest` on
  the JVM with the variable set: forward 472 of 472 equal. Plant `got.cost != w.cost + 1`: `COS through the engine against
  expected-cosrows.tsv (id, fields, tuple digests)`. Plant the writer's COS id set less its first row: `COS's cases are
  expected-cosrows's rows`.

## 5. CI, run 44030325384 on `dee43df` [C, job logs]

All four jobs success. 0 lines under `^e: ` and 0 under ` e: ` in each log.

| job | log lines | `TOTAL:` (tests) | gate lines |
|---|---:|---|---|
| `jvm` | 3225 | 262 | none |
| `jvm-production` | 598 | 3 | `OK: all 2 classes ran` (new; 2 and 1 test cases ran); cos last column 0 in all 8 rows; 472 cases, 0 differing |
| `ios-smoke` | 2412 | 61 and 7 | `OK on the iOS simulator: all 25 classes ran`; `OK: all 2 classes ran` |
| `ios-memory` | 763 | 3, 1, 1 | all 3 and all 1 classes ran on the iOS simulator |

The prediction held on every count. `ios-smoke`, simulator-checked: `step10 COS engine: ... equal on all six fields and
the tuple digest 472; mismatches 0`.

## 6. Not checked, and limits

- The new `jvm-production` gate uses `junit-ran.py`'s default mode, which counts a class that returned at its gate as run
  (shown above: the first class's gate-unset XML passed). The job sets the variable at job level, so this bites only if
  that line is removed. The `--simulator` mode checks for "is unset; not run" but prints "on the iOS simulator"; changing
  either was beyond the brief.
- The N4 messages on the simulator: seen on the JVM only.
- §6 of `s77-cos-rows-runs.md` ("[C] Each case read its own (c) row on both tables and on the simulator") carries the
  same reading as the two corrected sentences; the brief named two, so section 11 corrects two.
- No full local `jvmTest`; the full suite is CI's 262.
