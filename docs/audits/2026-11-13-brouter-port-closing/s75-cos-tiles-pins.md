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
