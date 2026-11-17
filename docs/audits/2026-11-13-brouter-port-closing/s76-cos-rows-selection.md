# Session 76: the cosine rows' group, its selection

Coder Plumb Ledger, session 76. Branch `brouter-port-5b`, base `cd9d1c540e605ff8deff23a00e00ea6348a5ca22` (checked:
`git ls-remote origin brouter-port-5b` gave that head, `git status --short` printed nothing). This is dispatch 6 of
session 72's order of work (`s72-cosine-rows-tiles-prebuild.md` §6, item 6). Marks: [C] confirmed by a run or a read
named here, [I] inferred, [U] unverified. No coordinate and no node id is written here.

The owner's ruling this serves, verbatim: "Cover them now". The planner's decisions, as dispatched: 59 cases per row,
472 in all, drawn per row; the expected rows in a resource of their own beside `expected-1b.tsv`; a new generator class
beside `LBGenerator`.

## 1. What is there now [C, read at `cd9d1c5`]

- **`LBGenerator`** (`brouter/src/jvmTest/.../oracle/e3/LBGenerator.kt`): node-weighted start over every (c) row in every
  tile of the folder it lists (`:26, 36-44, 81-86`); the rest of a draw at `:87-97`. s72 §3.1 showed it is the wrong draw
  for these rows (one row takes 68%).
- **`E3LBTest`** (`.../oracle/e3/strata/E3LBTest.kt`): `E3Strata.jdk` (`:19`), seed `20261207L` fixed in the class
  (`:52`), ends checked in one row before routing (`:27-34`), `E3Targeted.select` with reach "route in the band" read from
  R1's extent (`:35-41`), then `E3Targeted.stability` and `report` (`:45`).
- **`E3Targeted`** (`.../strata/E3Targeted.kt`): N 59, CAP 20 × 59 = 1,180 draws (`:15-16`); selection by R1 with R2
  equal to it (`E3Select.kt:126`).
- **The stability rule:** `E3Stability.kt:32-34`: a case is stable when R1 to R6 give one tuple and the logged runs R2,
  R3, R4, R5 give one list of collection lines. Six regimes per case in five JVMs: R1+R2 in one fresh JVM, R3 one JVM
  over the whole list, R4, R5, R6 a fresh JVM each (`:54-62`). E3's stop: more than 5% unstable (`:43`), applied by
  `E3Strata.report` (`E3Strata.kt:34-50`), which also requires the control to move between R1 and R4 (`:47-48`).
- **1b's expected file:** `E3Expected.RESOURCE` `/e3/expected-1b.tsv` (`E3Expected.kt:19`), its strata listed with
  count and stability table in `E3Expected.STRATA` (`:29-42`), no coordinate stored (`:10`), written by
  `E3ExpectedWriteTest` from a fresh R1 per case compared with the logged R1 (`E3ExpectedWriteTest.kt:43-73`).
- **The seam:** `LibmSeam.scales(row)` (`LibmSeam.kt:16`), called only through `CheapRuler.getLonLatToMeterScales`
  (`CheapRuler.kt:53`); the engine's seam is `RoutingContext(internal val libm: LibmSeam = MsunLibm)`
  (`RoutingContext.kt:19`). `git grep MsunLibm` in `commonMain` finds two other uses that do not take the context's
  seam: `SearchBoundary`'s default (`SearchBoundary.kt:14`; `git grep "SearchBoundary("` in `commonMain` finds no
  constructor call) and `FormatJson.kt:330` (output formatting, which `doRun` does not reach [I, not traced]).
- **The tiles:** `TestTiles.cosRows` (`TestTiles.kt:44-47`), five tiles, release `brouter-cos-tiles-2026-11-17`, fetched
  by `fetchCosTiles` for every `oracleJvm()` task, `e3Strata` among them (`build.gradle.kts:150-151`).
- **`e3Strata`** (`build.gradle.kts:195-211`): refuses to start without `-Pbrouter.e3.jdk`; `E3Strata.jdk` refuses any
  JDK but Eclipse Adoptium 21.0.13+9-LTS amd64 (`E3Strata.kt:17, 23-31`). Session 75 ran it with
  `tools/fetch-temurin.sh` into scratch and `-Pbrouter.e3.jdk=<home>` (`s75-cos-tiles-pins.md` §4.3). This session did
  the same: `tools/fetch-temurin.sh` into scratch printed `Temurin 21.0.13+9 ready` (pins matched) [C].

## 2. Design

### 2.1 Files

| file | what |
|---|---|
| `oracle/e3/CosRowsGenerator.kt` | the generator (§2.2), with `ROWS`, `SEEDS` and `stratum(row)` |
| `util/CountingLibm.kt` | the counting seam (§2.3) |
| `oracle/e3/strata/E3CosRowsTest.kt` | the stratum test in `e3Strata` (§2.4) |
| `oracle/e3/E3CosRowsExpected.kt` | the new resource's name, listing and log reader (§2.5) |
| `oracle/e3/strata/E3CosRowsExpectedWriteTest.kt` | the writer for the new resource (§2.5) |
| `resources/e3/expected-cosrows.tsv` | the 472 rows, after the runs |
| `oracle/e3/E3CosRowsExpectedFileTest.kt` | the ordinary `jvm` test that holds the file (§2.6), after the runs |
| `docs/audits/2026-11-13-brouter-port-closing/s76/` | the 8 committed stability tables and 8 selection tables, and the log's key lines |

All under `brouter/src/jvmTest/`. No `commonMain` change, no engine class, no change to `LBGenerator`, `E3LBTest`,
`E3Expected`, `E3ExpectedWriteTest`, `E3ExpectedFileTest` or `expected-1b.tsv`.

### 2.2 The generator, and the seeds

`CosRowsGenerator(segments)`: `LBGenerator`'s band scan for each of the 8 rows (`ROWS` = 474, 488, 502, 663, 675, 1046,
1053, 1079), and `draws(row, prefix, profile)`, one `java.util.Random` sequence per row. A draw: the start uniformly from
the row's nodes (one `nextDouble`, the form `LBGenerator.kt:83` uses over its cumulative counts), then `LBGenerator`'s
`:87-97` unchanged: distance, side, target latitude in the row's band, the nearest node, the three rejections.

**Seeds, fixed in the generator class (`CosRowsGenerator.SEEDS`) and pushed before any run:** LB's 20261207 plus the row:

| row | 474 | 488 | 502 | 663 | 675 | 1046 | 1053 | 1079 |
|---|---|---|---|---|---|---|---|---|
| seed | 20261681 | 20261695 | 20261709 | 20261870 | 20261882 | 20262253 | 20262260 | 20262286 |

These are the seeds s72 §3.3's scratch run used, so its draw counts are a prediction for this run (§3). They are held in
the generator rather than the test so the next dispatch's logic and production runs redraw the same cases from one
place (as `RoutingEngineStep9Test` repeats LB's seed; here it would not need to).

### 2.3 The counting seam

`CountingLibm(inner: LibmSeam)`: counts every `row` passed to `scales` (a concurrent map of counters), passes each call
to `inner` unchanged, passes `atan2` through. `rows` gives the set read. One instance per run. It wraps `OracleLibm`
here (the oracle's own table, the logic run); it wraps `MsunLibm` for a production run the same way. It lives in
`jvmTest`; the simulator will need its own in `commonTest` (the next dispatch's).

**What it counts and what it does not.** The oracle runs in child JVMs (`E3Launcher`) and its `CheapRuler` is static Java,
so its own row reads cannot be counted without changing the oracle. The rows are counted on the port's run of the same
case on the oracle's table, whose logic is held equal to the oracle's elsewhere (steps 8 and 9) [I: that the port reads
the rows the oracle reads; what is confirmed is the port's reads].

### 2.4 The stratum test, `E3CosRowsTest`, in `e3Strata`

1. `E3Strata.jdk("COS")`: refuses anything but the pinned Temurin.
2. Asserts 13 (c) rows and that the 8 are among them; prints each row's node count.
3. For each row in order, `E3Targeted.select` with label and stratum `COS<row>` (ids `COS<row>-0001` on), 59 cases, cap
   1,180 draws (`E3Targeted.CAP`, the stated draw limit), its draws checked before routing to have both ends in the
   row. **Mark:** no route, or the R1 route leaving the row's band (`E3LBTest.kt:35-41`'s rule): rejected. Otherwise the
   case runs once through the port in this JVM with `RoutingContext(CountingLibm(OracleLibm))` (the calls of
   `RoutingEngineF0Test.port`); **kept only if its own row is among the rows read**; else rejected as "the port on the
   oracle's table did not read row r".
4. After all 8 selections and before any stability run, the row assertion; each failure names its row:
   - every row has 59 kept within its cap, else `row r: k of 59 kept after n draws (cap 1180)`;
   - every kept case's own row is among its recorded rows (the keep rule, checked again on the recorded sets: it can
     fail only if the keep logic and the record disagree, so it is not separate evidence);
   - the union of rows read by all kept cases, intersected with the 13, equals the 8, else naming the missing and extra.
5. `E3Targeted.stability` per row (work folder `<work>/COS<row>`), then `E3Strata.report` per row (E3's controls and
   5% stop), all reported before the first failure is thrown. Prints per row: draws, kept, stable of 59; and the total
   time.

The port's outcome is not compared. That is the next dispatch's logic run.

### 2.5 The new resource, and how it is listed and held

- **`E3CosRowsExpected`**: `RESOURCE` `/e3/expected-cosrows.tsv`; `LOGS` `../docs/audits/2026-11-13-brouter-port-closing/s76`;
  `STRATA`, by analogy with `E3Expected.STRATA`: each `COS<row>` to (59, `cos<row>-stability.tsv`); `logged(name)` reads a
  committed table as `E3Expected.logged` does. The file's form is `E3Expected`'s (its `write`, `read`, `row`, `COLUMNS`),
  unchanged. `E3Expected.STRATA` is not touched, so `E3ExpectedFileTest.everyStratumHasItsCount` and the rest assert what
  they did.
- **The writer, `E3CosRowsExpectedWriteTest`** (a class of its own; `E3ExpectedWriteTest` would re-run every 1b stratum
  and needs their work folders): for each `COS<row>`, the case list the stability run left in `<work>/COS<row>/cases.tsv`,
  each case's R1 once more in a fresh JVM (`E3Stability.launch("R1R2")`, as `E3ExpectedWriteTest.kt:50`), compared with
  the R1 logged in the committed `cos<row>-stability.tsv`. Any difference, any case logged other than `stable`, or a
  count other than 59 is a stop and no file is written. Every row is `exact`. Head: the `# jdk ` line and the tuple and
  compare lines. Prints the row count and the file's SHA-256. Copied by hand into `src/jvmTest/resources/e3/`.
- **So "stable" means:** stable by `E3Stability.kt:32-34` over the six regimes of §1, plus the same R1 a seventh time
  in the writer.

### 2.6 The ordinary `jvm` test, `E3CosRowsExpectedFileTest`

Reads the resource; two tests:
- `rowsAreFiftyNinePerCosRow`: 472 rows, ids distinct, and the count per id prefix (before the first `-`) equals
  `STRATA`'s, each 59; each row's stratum column equals its id prefix.
- `fileIsItsPinnedDigest`: SHA-256 of the resource's bytes equals a constant pinned in `E3CosRowsExpected`.

**Seen failing first** with `revert.sh`: one row's tuple digest changed by one character in the resource (expected: the
digest test fails, the count test passes); then one row's id prefix changed from `COS474` to `COS488` (expected: both
fail, the count naming COS474 58 and COS488 60).

### 2.7 The row assertion's plant

`revert.sh` runs `jvmTest` only, and this test runs only in `e3Strata`. So a scratch copy of `revert.sh` with the one
Gradle line changed to `:brouter:e3Strata -Pbrouter.e3.jdk=<home> --tests '*E3CosRowsTest*'` and the XML folder to
`test-results/e3Strata` (same copy, edit, restore from the copy, `cmp`, compile-error refusal; this copy counts both
`^e: ` and ` e: `). The plant, in `CountingLibm.scales`: `counts.computeIfAbsent(row) ...` guarded by `if (row != 474)`,
so the run is unchanged except that row 474 is never recorded. Expected: no COS474 candidate is kept; COS474 draws to its
cap of 1,180; the test fails before any stability run with `row 474: 0 of 59 kept after 1180 draws (cap 1180)` and the
group's `missing [474]`; the other 7 rows select as in the real run. After: the file `cmp`-equal to its saved copy and
`git diff HEAD` empty.

## 3. Predictions (before any run)

- **P1, draws per row** (draws counted until the 59th kept). s72 §3.3 on the 14 November bytes with these seeds: 474 189,
  488 119, 502 122, 663 123, 675 119, 1046 92, 1053 106, 1079 95. Since then the release's bytes differ; 7 of the 8 rows'
  node sets are equal and row 675's differs by 2 nodes in, 2 out (`s75` §5.2), so I predict the same counts within a few
  draws in 7 rows, and possibly a different sequence from some draw on in row 675. The extra rule (R2 equal to R1) and the
  row-read rule rejected none in s72 (59 of 59 read their row there, on msun's table), so I predict no rejection for
  either here [I].
- **P2, kept:** 59 of 59 in every row, within the cap; the group reads exactly the 8 (c) rows.
- **P3, stability:** 59 of 59 stable in each row (LB was, `25-lb-e3Strata.log`); E3's stop passes. Time about 35 to 60
  minutes in all (s72 §5), on 4 cores here (3 workers).
- **P4, the writer:** 472 rows, R1 equal to its log in all 472, no stop.
- **P5, the plant:** as §2.7, failing naming row 474 in two messages.
- **P6, the file test plants:** as §2.6.
- **P7, CI on the last code commit:** `jvm` `TOTAL:` 257 (255 + the 2 tests of §2.6), 0 failures; `jvm-production`
  unchanged (2); `ios-smoke` 60 and 7, `OK on the iOS simulator: all 24 classes ran`; `ios-memory` 3, 1, 1, its gates OK;
  0 compile errors in each. Simulator results are simulator-checked.
- **P8:** `git diff --stat cd9d1c5 -- brouter/src/jvmTest/resources/e3/expected-1b.tsv
  brouter/src/jvmTest/kotlin/com/zynergylabs/forager/brouter/oracle/e3/E3Expected.kt` prints nothing at the end.
- **P9:** `scan.sh` prints 0 on every commit.

## 4. Results

(Written as they come.)

### 4.1 The selection, on the pinned Temurin [C]

`./gradlew --max-workers=2 :brouter:e3Strata -Pbrouter.e3.jdk=<scratch>/temurin/jdk-21.0.13+9 --tests '*E3CosRowsTest*'`
at `a974603`, log `.../scratchpad/s76/sel.log`. JDK line: `java.vendor=Eclipse Adoptium; java.runtime.version=21.0.13+9-LTS;
os.arch=amd64`. (c) rows: the 13; the 8 rows' node counts 5,159, 16,239, 1,124, 2,523, 7,788, 96,104, 10,205, 2,461,
equal to `s75` §4.1's.

| row | draws (to the 59th kept) | s72 §3.3 | routed | kept | own row read (of kept) | rejected: route leaves band | rejected: port did not read the row |
|---|---:|---:|---:|---:|---:|---:|---:|
| 474 | 178 | 189 | 162 | 59 | 59 | 20 | 0 |
| 488 | 120 | 119 | 113 | 59 | 59 | 35 | 0 |
| 502 | 120 | 122 | 92 | 59 | 59 | 13 | 0 |
| 663 | 117 | 123 | 95 | 59 | 59 | 24 | 0 |
| 675 | 132 | 119 | 120 | 59 | 59 | 28 | 0 |
| 1046 | 89 | 92 | 82 | 59 | 59 | 17 | 0 |
| 1053 | 106 | 106 | 90 | 59 | 59 | 28 | 0 |
| 1079 | 86 | 95 | 67 | 59 | 59 | 8 | 0 |

- Per row, the (c) rows its kept cases read are its own row only; the group read exactly
  `[474, 488, 502, 663, 675, 1046, 1053, 1079]`. The row assertion passed. Selection of all 8 rows: 228 s.
- **P1 partly wrong.** The counts are near s72's but not the same in 7 rows (only 1053 equal); I had predicted the same
  within a few draws in 7 rows. 474 differs by 11 and 675 by 13. The rule here is not s72's: a candidate must also have R2
  equal to R1, routed in a child JVM, and the "no route" counts differ from s72's "oracle routed" figures [I: the cause
  of each difference was not traced]. P1's "no rejection by the row-read rule" held: 0 in every row.
- **The row assertion's per-case check cannot fail here** independently of the keep rule (§2.4 item 4); the evidence
  that it bites is the plant (§4.4).

### 4.2 Stability, on the pinned Temurin [C]

Same run (started 03:14:23 UTC; `E3CosRowsTest` PASSED, `BUILD SUCCESSFUL in 11m 41s`, exit 0; 0 lines matching `^e: ` or
` e: ` in the log; its one XML file written 03:26, after the start). Committed beside this file in `s76/`: the 8 stability
tables (`cos<row>-stability.tsv`, 59 rows each), the 8 selection tables (`cos<row>-select.tsv`, one row per counted
draw) and the log's `E3` lines (`e3strata-cos-rows.log`). None holds an end: tuples, digests, counts, ids, tile names.

| row | draws | kept | selection's R1 and R2 equal E3's | kinds (R1) | stable (six regimes) | over 5% |
|---|---:|---:|---|---|---:|---|
| 474 | 178 | 59 | 59 of 59 | route 59 | 59 of 59 | false |
| 488 | 120 | 59 | 59 of 59 | route 59 | 59 of 59 | false |
| 502 | 120 | 59 | 59 of 59 | route 59 | 59 of 59 | false |
| 663 | 117 | 59 | 59 of 59 | route 59 | 59 of 59 | false |
| 675 | 132 | 59 | 59 of 59 | route 59 | 59 of 59 | false |
| 1046 | 89 | 59 | 59 of 59 | route 59 | 59 of 59 | false |
| 1053 | 106 | 59 | 59 of 59 | route 59 | 59 of 59 | false |
| 1079 | 86 | 59 | 59 of 59 | route 59 | 59 of 59 | false |

- 472 of 472 stable by `E3Stability.kt:32-34` (one tuple over R1 to R6, one collection-line list over R2 to R5), each in
  five JVMs (`:54-62`); every case's control moved between R1 and R4 (`E3Strata.kt:47-48` did not fail).
- **Time:** 699 s in all (selection 228 s). P3 predicted 35 to 60 minutes; it took 11.7. P2 and P3 otherwise held.

### 4.3 The expected rows, and the file test [C]

- **The writer** (`--tests '*E3CosRowsExpectedWriteTest*'` on the pinned Temurin, log `.../scratchpad/s76/writer.log`, 0
  compile-error lines, PASSED, `BUILD SUCCESSFUL in 1m 59s`): 59 cases per row, R1 equal to the committed log in 472 of
  472 (`r1-vs-log.tsv`: 472 `true`), 0 stops; `wrote 472 rows; SHA-256 405e15ad22fed772a3e362fcc2a3117eb4400bb9156348646fdbce68741ea419`.
  P4 held.
- **`brouter/src/jvmTest/resources/e3/expected-cosrows.tsv`**: copied from `<work>/expected-cos/` byte for byte (SHA-256
  above). 477 lines: 4 head lines, the column line, 472 rows, all `exact`. No coordinate: the columns are
  `E3Expected.COLUMNS`.
- **`E3CosRowsExpectedFileTest`**, locally on the container's OpenJDK (not CI's Temurin), with `E3ExpectedFileTest`:
  8 tests passed, 0 compile-error lines, both XML files fresh.
- **Seen failing first, with `revert.sh`** (one exact edit to the resource, the class run, restored from the saved copy,
  `cmp` equal, 0 lines matching `^e: ` or ` e: `, 1 fresh XML; the resource's SHA-256 checked equal to before after each):
  - plant A, the last hex digit of row `COS474-0001`'s tuple digest `...78` to `...79`: 2 tests, 1 failure,
    `fileIsItsPinnedDigest`: `expected:<4[05e15ad2...]> but was:<4[82f218d7...]>`; the count test passed. As P6.
  - plant B, that row's id `COS474-0001` to `COS488-0001`: 2 tests, 2 failures; `rowsAreFiftyNinePerCosRow`:
    `rows per id prefix expected:<{COS474=59, COS488=59, ...}> but was:<{COS488=60, COS474=58, ...}>`, and the digest
    test. As P6.

### 4.4 The row assertion seen failing [C]

With a scratch copy of `revert.sh` for `e3Strata` (`.../scratchpad/s76/revert-e3.sh`: the same save, one exact edit,
restore from the saved copy and `cmp`; its Gradle line `:brouter:e3Strata -Pbrouter.e3.jdk=<pinned Temurin>`, its XML
folder `test-results/e3Strata`, its compile-error count over `^e: ` and ` e: `). The plant, `CountingLibm.kt:17`:
`counts.computeIfAbsent(row) ...` to `if (row != 474) counts.computeIfAbsent(row) ...`, so row 474 is never recorded.
Log `.../scratchpad/s76/s76rowplant-gradle.log`.

- 0 compile-error lines; 1 fresh XML; `restored: cmp equal`; afterwards `git diff HEAD --stat` and `git status --short`
  printed nothing.
- 1 test, 1 failure, before any stability run (`BUILD FAILED in 6m 43s`):
  `the cosine rows' row assertion: [row 474: 0 of 59 kept after 1180 draws (cap 1180), the group's (c) rows read are
  [488, 502, 663, 675, 1046, 1053, 1079], not [474, ...]: missing [474], extra []]`.
- Row 474 drew to its cap: 1,180 draws, 1,070 routed, 352 rejected as "the port on the oracle's table did not read row
  474" (every in-band candidate with R2 equal to R1), 0 kept. Both failures name row 474 and could come from no other
  edit: the other 7 rows kept 59 each with exactly §4.1's draw counts (120, 120, 117, 132, 89, 106, 86), so their
  selection repeated draw for draw on a second run. P5 held.

### 4.5 CI on the last code commit, `5f280f7` (run 44010575381) [C]

Logs read with `gh api .../actions/jobs/<id>/logs` into `.../scratchpad/s76/ci-<job>.log`.

| job | conclusion | `TOTAL:` lines | gate lines | compile errors (`^e: ` / ` e: `) |
|---|---|---|---|---|
| `jvm` (133796343411) | success | `tests 257, failures 0, errors 0, skipped 0 in 112 files` | (none in this job) | 0 / 0 |
| `jvm-production` (133796343031) | success | `tests 2, failures 0, errors 0, skipped 0 in 1 files` | (none) | 0 / 0 |
| `ios-smoke` (133796343273) | success | `tests 60 ... in 29 files`; `tests 7 ... in 2 files`, each `failures 0` | `OK on the iOS simulator: all 24 classes ran`; `OK: all 2 classes ran` | 0 / 0 |
| `ios-memory` (133796343434) | success | `tests 3`, `tests 1`, `tests 1`, each `failures 0` | `OK on the iOS simulator: all 3 classes ran (.../ios-memory-groups)`; `OK on the iOS simulator: all 1 classes ran (.../ios-r-fallback)` | 0 / 0 |

iOS results are simulator-checked. P7 held: `jvm` 257 = 255 + `E3CosRowsExpectedFileTest`'s 2 (both PASSED in the log),
and 112 files, one more than s75's 111. No 429 or download failure in any job.

### 4.6 What did not move [C]

`git diff --stat cd9d1c5 -- expected-1b.tsv E3Expected.kt E3ExpectedFileTest.kt E3ExpectedWriteTest.kt LBGenerator.kt
E3LBTest.kt brouter/src/commonMain` (full paths) printed nothing. P8 held. `scan.sh` printed 0 on every commit. P9 held.

## 5. Confirmed vs inferred

- [C] Every count, digest and time in §4, from the logs named there.
- [I] That the oracle reads the rows the port reads on the oracle's table (§2.3): the oracle's own reads are not counted.
- [I] That `FormatJson.kt:330`'s direct `MsunLibm` is not reached by `doRun` (§1); not traced.

## 6. Could not determine

- Why 7 rows' draw counts differ from s72 §3.3's by 1 to 13 with the same seeds. Not traced.

## 7. Premises in the brief that were wrong

- The brief predicted (from s72) a long local run; the selection and stability took 11.7 minutes, not 35 to 60.
- `revert.sh` runs `jvmTest` only, so it cannot run the row plant, which lives in `e3Strata`; a scratch copy was used
  (§4.4), with its differences shown there.

## 8. Decided beyond scope

- **The seeds' home and values:** in `CosRowsGenerator.SEEDS`, 20261207 + row (s72's scratch seeds), so the next dispatch
  redraws from one place.
- **Stratum names `COS<row>`**, one per row, which are also the id prefixes and the resource's `stratum` column.
- **The draw limit** is `E3Targeted.CAP` (1,180) per row, the existing cap, not a new number.
- **A writer class of its own** (`E3CosRowsExpectedWriteTest`) rather than a change to `E3ExpectedWriteTest`, and its rule
  that a case logged unstable is a stop (none was).
- **Committed logs** in `s76/`: stability and selection tables per row, and the run's `E3` lines.
- **The file test's** check that each row's stratum column equals its id prefix, beside the three asked for.

## 9. Not checked

- The port's outcomes against the oracle's on these 472 cases (the next dispatch's logic and production runs).
- The rows read on msun's table (`CountingLibm(MsunLibm)`); only the oracle's table was counted.
- `E3CosRowsTest` and the writer in CI: `e3Strata` runs in no CI job; both ran only here, on the pinned Temurin.
- A second full stability run: the plant's run repeated 7 rows' selections draw for draw, but no stability.
