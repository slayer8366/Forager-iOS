# Session 75: pinning the cosine-row tile release

Coder Plumb Ledger, session 75. Branch `brouter-port-5b`, base `27cb05a071e9d4c93ff1414e9acb5db8ec76ae26` (checked with
`git ls-remote origin brouter-port-5b`, which gave that head, and `git status --short`, which printed nothing). This is
dispatch 5 of session 72's order of work (`s72-cosine-rows-tiles-prebuild.md` §6, item 5). Marks: [C] confirmed by a
run or a read named here, [I] inferred, [U] unverified. No coordinate and no node id is written here.

## 1. What is there now [C, read]

- **The release's pin file.** `https://github.com/slayer8366/Forager-iOS/releases/download/brouter-cos-tiles-2026-11-17/tile-pins.tsv`,
  fetched into scratch only: 2,491 bytes, SHA-256 `b8135f5e38cf38b784d8d84eddecb9fd4f2669b7890dde8aaa29f7c940f683dc`.
  Read whole: three `#` header lines (tools commit `4dc6c23`, run 43985424626), the column row, five data rows (asset,
  path `segments/<tile>.rd5`, bytes, sha256, source URL, Last-Modified `Mon, 16 Nov 2026 01:03:01 GMT`, ETag, captured
  2026-11-17), then 14 `#` lines from `CosTileCheck`: 8 `# row` lines (tile, row, `nodes=`, attempt, track nodes, in
  band, distance in metres) and 5 `# ends` digest lines. No id, `ilon`, `ilat` or coordinate. Sizes equal the brief's.
- **The fetch.** `tools/fetch-tiles.sh <dir> <base url> [pin file]` takes the pin file as its third argument
  (`fetch-tiles.sh:21-24`), skips `#` lines and the column row (`:30`), and sends each row through `fetch-pinned.sh`,
  which refuses a file whose size or SHA-256 differs (`fetch-pinned.sh:27-40, 57-61`). It needs no change.
- **The build.** `fetchTiles` (`brouter/build.gradle.kts:40-58`) and `fetchF0h` (`:64-72`); `oracleJvm()` (`:126-149`)
  makes every oracle test JVM (`jvmTest`, `e3Strata`) depend on the fetches and passes each folder as a system property.
- **`TestTiles`** (`brouter/src/jvmTest/.../tiles/TestTiles.kt`) reads those properties. The commonTest side reads a
  property on the JVM and an environment variable on the simulator through `setting(prop, env)`
  (`Step10Cases.kt:54`, `PinnedTileReadTest.kt:26-27`).
- **The listers of `segments/`:** `RGenerator.kt:36` and `LBGenerator.kt:26` (s72 §3.2). `E3RSequenceTest` holds R's
  sequence to a SHA-256 (`E3RSequenceTest.kt:36, 42`). It is in the `e3.strata` package, which `jvmTest` excludes
  (`build.gradle.kts:156`); it runs only in `e3Strata`, on Temurin 21.0.13+9 passed as `-Pbrouter.e3.jdk`
  (`:169-185`). So no CI job runs it (see §2.5).

## 2. Design

### 2.1 Files

| file | what |
|---|---|
| `tools/tile-pins-cos.tsv` | the release's `tile-pins.tsv`, byte for byte (new name; `tools/tile-pins.tsv` untouched) |
| `brouter/build.gradle.kts` | `fetchCosTiles`: `fetch-tiles.sh build/tiles-cos <release url> tools/tile-pins-cos.tsv`; `oracleJvm()` depends on it and passes `brouter.tiles.cos.dir` and `brouter.tiles.cos.pins` |
| `TestTiles.kt` | `cosRows` = `<brouter.tiles.cos.dir>/segments`, and `cosPinFile` |
| `tiles/CosTilePinsTest.kt` | the pin test (§2.3) |
| `tiles/CosRowCountsTest.kt` | the row counts by the port's reader (§2.4) |
| `.github/workflows/brouter.yml` | `:brouter:fetchCosTiles` in `ios-smoke`'s and `ios-memory`'s explicit fetch steps, and `SIMCTL_CHILD_BROUTER_COS_TILES_DIR` beside each `SIMCTL_CHILD_BROUTER_TILES_DIR`. No simulator test, no `SIMULATOR_CLASSES` change. |

The tiles land in `build/tiles-cos/segments/`, a folder of their own; `build/tiles/segments/` is never written by the
new task. The fetch is `fetch-tiles.sh` with a pin file, not a new script, because that script already refuses by size
and SHA-256 per row (the same refusal `fetchF0h` gets through `fetch-pinned.sh`). The variable name follows the
property: `brouter.tiles.cos.dir` and `BROUTER_COS_TILES_DIR`, as `brouter.tiles.dir` and `BROUTER_TILES_DIR`.

### 2.2 The release URL

`https://github.com/slayer8366/Forager-iOS/releases/download/brouter-cos-tiles-2026-11-17`, the form the brief gives,
as `fetchTiles` holds its own (`build.gradle.kts:40`).

### 2.3 The pin test (`CosTilePinsTest`), in the style of `TilePinsTest`

The pins are held in the test as well as in the file, as `TilePinsTest` does, so an edit to the file alone cannot pass a
different tile.

1. `pinFileIsTheReleasesOwn`: the committed file's SHA-256 equals the release asset's (`b8135f5e...`, 2,491 bytes), its
   column row is `TilePinsTest`'s, and its five rows' asset, path, bytes and sha256 equal the test's pins.
2. `eachFetchedTileMatchesItsPin`: each of the five files is under `TestTiles.cosRows`, with its pinned bytes and
   SHA-256; exactly 5 checked.
3. `existingSegmentsFolderIsUnchanged`: `TestTiles.segments` lists exactly `E170_S50.rd5`, `W70_N40.rd5`,
   `W75_N35.rd5`, `W75_N40.rd5` (the four of `tools/tile-pins.tsv` whose path starts `segments/`), nothing else, and none
   of the five new names; and `TestTiles.cosRows` is a different folder from `TestTiles.segments`.

**Seen failing first:** the red commit holds `E145_S45.rd5`'s pinned bytes in the test as 6,316,105, one more than the
release's 6,316,104. The file and the fetch are right, so the fetch succeeds and only the test's own pin is wrong.

### 2.4 The row counts (`CosRowCountsTest`)

- **Reader:** the port's, as session 72's scratch measure used it (`s72` §2.3): `MapaccessEngines.portPhysical(file)`
  (`PhysicalFile`), then `OsmFile` per degree square in the engine's absolute frame (lon degree + 180, lat degree + 90),
  then `createMicroCache(..., reallyDecode = true, ...)` per cell, then `MicroCache.getIdForIndex(k)`; `ilat` is the
  id's low 32 bits. Not `CosTileCheck`, not `TileNodes`, not `LBGenerator`.
- **Row rule:** row = `ilat / 100_000` (`LBGenerator.kt:64`, `rowOf`).
- **Expected:** read from `tools/tile-pins-cos.tsv`'s `# row` lines (`# row<TAB>tile<TAB>row<TAB>nodes=N...`). The test
  names the 8 (tile, row) pairs; it fails if any of them has no line, if a line is there twice, or if a `# row` line
  names a pair not in the 8.
- **Assertion:** every pair's count by the port's reader equals the line's `nodes=`. On failure the message lists each
  differing pair as `row <r> in <tile>: port reader <n>, release <m>`.
- **Printed:** per tile, the total node count; per row, the two counts. No id, no coordinate.

### 2.5 The R sequence pin

`E3RSequenceTest` runs only in `e3Strata` on Temurin 21.0.13+9 (§1). I will try to run it locally with
`tools/fetch-temurin.sh` after the change, with the new fetch in place. If that cannot be done here, it is reported as not
checked. Independently of it, `existingSegmentsFolderIsUnchanged` shows the folder `RGenerator` lists holds the same four
files, which is the input R's sequence depends on [I: that R's draw depends on the folder only through its listing,
`RGenerator.kt:36, 96`, read in s72 §3.2].

### 2.6 The plant

With `revert.sh` (copy, edit, run, restore from the copy, refuse on compile errors or stale XML): the row rule changed
from `ilat / 100_000` to `(ilat + 1_000) / 100_000` in `CosRowCountsTest`, running that class only. Expected: the test
fails, and its message names the rows whose counts moved [I: which rows move depends on how many nodes lie in the top
0.001 degree of each row; I expect most or all 8].

## 3. Predictions (before any run)

- **P1.** Locally, `fetchCosTiles` fetches five files into `build/tiles-cos/segments/`, each "matches its pins", and
  `build/tiles/segments/` keeps its four.
- **P2.** On the red commit, locally and in CI `jvm`: 255 tests (251 + 3 + 1), 2 failures, both in `CosTilePinsTest`:
  `pinFileIsTheReleasesOwn` (the E145_S45 row differs) and `eachFetchedTileMatchesItsPin` (`E145_S45.rd5: bytes`,
  expected 6316105, actual 6316104). Every other test passes, `CosRowCountsTest` among them. 0 compile errors.
- **P3.** On the green commit, CI `jvm`: `TOTAL:` 255, 0 failures. `jvm-production`: its total unchanged. `ios-smoke`:
  simulator total 60, gate line `OK on the iOS simulator: all 24 classes ran`. `ios-memory`: 7, its gate OK.
  All simulator-checked. 0 compile errors in each job.
- **P4.** The new fetch runs in all four jobs (`jvm`, `jvm-production` and both iOS jobs' writer and fetch steps, by
  `oracleJvm()`), adding a few seconds each (about 30 MB; s72 §5).
- **P5.** The port's reader counts on the release's bytes equal the release's `# row` lines in all 8 rows.
- **P6.** The plant fails `CosRowCountsTest` naming rows; the restore leaves the file equal to its copy.
- **P7.** `scan.sh` prints 0 on every commit.

## 4. Results

(Written as they come.)

### 4.1 The red commit, run locally first [C]

`./gradlew --max-workers=2 :brouter:jvmTest --tests '*CosTilePinsTest' --tests '*CosRowCountsTest' --tests '*.TilePinsTest'`
on the container's OpenJDK 21.0.12 (not CI's Temurin), log `.../scratchpad/s75/red-local.log`: 0 lines matching `^e: `
or ` e: `, 6 tests, 2 failed, as P2 predicted for these classes.

- **P1 held.** `fetchCosTiles` fetched the five from the release, each "matches its pins", "5 tiles ready in
  .../build/tiles-cos". `build/tiles/segments/` still lists its four (`existingSegmentsFolderIsUnchanged` passed, printing
  `E170_S50.rd5, W70_N40.rd5, W75_N35.rd5, W75_N40.rd5`).
- **The two failures are the planted pin and nothing else:** `pinFileIsTheReleasesOwn`: `...E145_S45.rd5	631610[5]...
  but was ...631610[4]...`; `eachFetchedTileMatchesItsPin`: `E145_S45.rd5: bytes expected:<6316105> but was:<6316104>`.
  The file's own SHA-256 check passed before the row comparison, so the committed file is the release's.
- **P5 held: the port's reader gives the release's count in all 8 rows** (`CosRowCountsTest` passed):

| tile | row | port reader | release `# row` | tile's nodes, port reader (release) | tile's nodes, s72 on 14 Nov bytes |
|---|---:|---:|---:|---:|---:|
| E145_S45 | 474 | 5,159 | 5,159 | 231,962 | 231,750 |
| E145_S45 | 488 | 16,239 | 16,239 | | |
| E170_S40 | 502 | 1,124 | 1,124 | 554,495 | 554,299 |
| E15_S25 | 663 | 2,523 | 2,523 | 127,490 | 127,474 |
| E15_S25 | 675 | 7,788 | 7,788 | | |
| W95_N10 | 1046 | 96,104 | 96,104 | 414,290 | 414,279 |
| W20_N15 | 1053 | 10,205 | 10,205 | 174,367 | 174,360 |
| W20_N15 | 1079 | 2,461 | 2,461 | | |

  **The whole-tile totals moved** (by 7 to 212 nodes per tile) while the 8 rows did not. So the port's reader did read
  different bytes from 14 November's: the files changed elsewhere, not in these rows' counts. The id-set comparison is in
  §5.

### 4.2 The plant, and two more [C]

With `.../scratchpad/tools/revert.sh` (copy saved, one exact edit, the class run, restored from the copy, `cmp` equal, 0
compile errors, 1 fresh XML file each time; `sha256sum` of each file after equal to before, `git status` clean):

- **The row rule** (the brief's plant), `CosRowCountsTest.kt:76`, `ilat / 100_000` to `(ilat + 1_000) / 100_000`:
  failed, 1 test, 1 failure, naming 7 of the 8 rows: `row 474 in E145_S45: port reader 5134, release 5159, row 488 ...
  16259 ... 16239, row 663 ... 2527 ... 2523, row 675 ... 7942 ... 7788, row 1046 ... 96413 ... 96104, row 1053 ...
  10117 ... 10205, row 1079 ... 2343 ... 2461`. Row 502 did not move under it (1,124 both ways): no node in that row's
  top 0.001 degree or the row below's moved across. So that plant does not show row 502's comparison can fail.
- **One expected count, for row 502** (beyond the brief's one plant, to cover the row the first did not move):
  `tools/tile-pins-cos.tsv`'s `nodes=1124` to `nodes=1125`: failed with exactly `row 502 in E170_S40: port reader
  1124, release 1125`.
- **A missing line:** row 1079's `# row` renamed `# rox`: failed at the pair check, `the (tile, row) pairs of
  tile-pins-cos.tsv's # row lines against the 8 cosine rows expected:<[... (W20_N15, 1079)]> but was:<[...`.

### 4.3 The R sequence pin, run locally on the pinned Temurin [C]

`tools/fetch-temurin.sh` fetched Temurin 21.0.13+9 (matched its pins) into scratch. Then
`./gradlew :brouter:e3Strata -Pbrouter.e3.jdk=<that home> --tests '*E3RSequenceTest'` on the red commit's tree (log
`.../scratchpad/s75/rseq.log`, 0 compile errors): the JDK line printed `Eclipse Adoptium; 21.0.13+9-LTS; amd64`, then
`E3 R-sequence: 1534 cases; SHA-256 ed378c85...e43d6c`, the pinned value, PASSED. `e3Strata` runs `oracleJvm()`, so
`fetchCosTiles` ran before it.

Its control, so that pass is not one that could not fail: a copy of the release's `W20_N15.rd5` put in
`build/tiles/segments/`, the same run again (`rseq-control.log`): `R's sequence changed expected:<[ed378c85...]> but
was:<[85a70a42...]>`, FAILED. With the same stray file, `CosTilePinsTest.existingSegmentsFolderIsUnchanged` failed too
(`segctl.log`): `expected:<[E170_S50.rd5, W70_N40.rd5, W75_N35.rd5, W75_N40.rd5]> but was:<[E170_S50.rd5, W20_N15.rd5,
...]>`. The stray copy was removed after each run; the folder lists its four again.

No CI job runs `E3RSequenceTest` (§1). This is the only run of it here, on the container, not in CI.

## 5. The rebuilt bytes: did the bands change, or did the check not read the new bytes?

The 14 November copies survive: `.../scratchpad/tiles72/<tile>/<tile>.rd5` (downloaded 06:27 UTC; sizes and SHA-256
equal s72 §2.2's, and equal to `tiles73/run2`, `tiles74/run1` and `review73/run1` by size). Compared with the release's
bytes as fetched into `brouter/build/tiles-cos/segments/` (each matching its pin). Scratch only: a Python region diff
(`.../scratchpad/s75/scripts/rd5diff.py`) and a scratch Kotlin test on the port's reader
(`.../scratchpad/s75/S75CompareTest.kt.saved`, log `.../scratchpad/s75/compare.log`, 0 compile errors), deleted after
the run with `git status --porcelain --untracked-files=all` then empty. Nothing printed an id or a coordinate.

### 5.1 Where the bytes differ [C]

The region diff parses each file as `PhysicalFile`/`OsmFile` lay it out (200-byte file index, then per degree square
a cell index of 32 x 32 ints and the cells, then a 113-byte trailer). Its own check: its non-empty cell totals equal
`CosTileCheck`'s `non-empty` on the same bytes for all ten files (old: 6,210, 3,074, 13,952, 2,417, 3,774, as s72 §2.4;
new: the same, except E15_S25 13,954, as the release log line 240 says).

| tile | size change | file index | trailer | degree squares whose cell index differs | non-empty cells: equal / differ / in one file only | cells overlapping each target row: byte-equal / differ |
|---|---:|---|---|---:|---|---|
| E145_S45 | +1,956 | differs | differs (creation time, CRCs) | 5 of 16 | 6,195 / 15 / 0 | 474: 259 / 0; 488: 391 / 0 |
| E170_S40 | +2,139 | differs | differs | 3 of 8 | 3,047 / 27 / 0 | 502: 71 / 0 |
| E15_S25 | +354 | differs | differs | 3 of 25 | 13,931 / 21 / 2 | 663: 361 / 0; 675: 411 / **4** |
| W95_N10 | +132 | differs | differs | 2 of 5 | 2,408 / 9 / 0 | 1046: 305 / 0 |
| W20_N15 | +124 | differs | differs | 2 of 11 | 3,768 / 6 / 0 | 1053: 229 / 0; 1079: 56 / 0 |

So the rebuild changed 6 to 27 cells per tile (plus 2 new cells in E15_S25), and every byte offset after the first
changed cell moves, which is why the whole-file SHA-256 changes. A byte-by-byte count of differing bytes would count
those shifted offsets, so it is not given. Of the 8 rows, 7 lie wholly in byte-equal cells. Row 675's 415 cells
include 4 that changed.

### 5.2 The nodes, by the port's reader [C]

| tile | nodes 14 Nov / release | ids only in the 14 Nov file / only in the release | row | id-set digest 14 Nov | id-set digest release | equal sets |
|---|---|---|---:|---|---|---|
| E145_S45 | 231,750 / 231,962 | 165 / 377 | 474 | `6a8c1472ef165663` | `6a8c1472ef165663` | yes |
| | | | 488 | `c3f6f259dca25e47` | `c3f6f259dca25e47` | yes |
| E170_S40 | 554,299 / 554,495 | 92 / 288 | 502 | `e0e488e3014ccd71` | `e0e488e3014ccd71` | yes |
| E15_S25 | 127,474 / 127,490 | 24 / 40 | 663 | `445191e8d391ecdb` | `445191e8d391ecdb` | yes |
| | | | 675 | `bd846fa8c2a25bd6` | `f895535c0fc5363a` | **no** |
| W95_N10 | 414,279 / 414,290 | 10 / 21 | 1046 | `e9edb7f56e34320c` | `e9edb7f56e34320c` | yes |
| W20_N15 | 174,360 / 174,367 | 2 / 9 | 1053 | `b1e23480dc01137e` | `b1e23480dc01137e` | yes |
| | | | 1079 | `6da4cac52412ee66` | `6da4cac52412ee66` | yes |

The digest is the first 16 hex digits of SHA-256 over the row's ids sorted, 8 bytes each, big-endian. Its control: row
1079's digest with one id dropped is `44fa633c3da3463f`, not `6da4cac52412ee66`. In E15_S25, 4 ids that changed lie in
the tile's target rows, all in row 675 (the other rows' sets are equal), and row 675's count is 7,788 both times: 2 ids
left and 2 came in. A node's id is its position, so this is 2 nodes moved, or 2 removed and 2 added, inside the band.

### 5.3 What the release's own log shows [C]

The release run's capture job (43985424626, job 133712234164, read with `gh api .../actions/jobs/<id>/logs` into
`.../scratchpad/s75/release-capture.log`) prints a `decoded` line per tile. Its node totals are the release's bytes'
(231,962; 554,495; 127,490; 414,290; 174,367, lines 218, 230, 240, 252, 262), equal to the port reader's in 4.1, and
different from the 14 November dry run's (231,750; 554,299; 127,474; 414,279; 174,360; `tiles74/run1.log` lines 11,
24, 35, 48, 59). E15_S25's `non-empty` is 13,954 there against 13,952. So the release's figures that were identical to
the dry run's were the per-row ones only; its per-tile figures were not.

### 5.4 The answer

- **The check read the new bytes.** Its own per-tile totals in the release log differ from the dry run's, and agree with
  a different reader (the port's) on the release's bytes.
- **The map data in 7 of the 8 rows did not change.** Their id sets are equal and every cell overlapping them is
  byte-equal. The rebuild changed 6 to 27 cells per tile elsewhere in each tile.
- **Row 675 did change,** and its count did not show it: 2 ids out, 2 in, the same 7,788, in 4 changed cells. Its
  route, distance and ends digest were also unchanged in the release log, which is consistent with the changed nodes
  lying away from the 16 sorted start positions and their ends [I: not traced].
- **What this does not show:** whether anything other than node ids changed inside the 7 rows' cells (they are
  byte-equal, so nothing in them did) and what changed in row 675's 4 cells beyond its node ids (way tags or links were
  not compared). It also says nothing about rows outside the 8. And a count check cannot see an equal-count change: an
  id-set digest per row would, if the capture ever needs it (not proposed here; the planner's call).
