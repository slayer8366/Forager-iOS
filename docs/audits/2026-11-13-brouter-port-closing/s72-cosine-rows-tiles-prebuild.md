# Session 72: tiles for the 8 cosine rows outside the pinned tiles, a pre-build report

Plumb Ledger, session 72, dispatched by the planner. Branch `brouter-port-5b`, base `6e2d8796f0a9a303b040a1302db17c47daea4613`
(checked: `git ls-remote origin brouter-port-5b` gave that head, the tree was clean). This report builds nothing. No code,
workflow or test changed; this file is the only change. Tiles were downloaded to the session's scratch folder only.

The owner's ruling this serves, verbatim: "Cover them now". The option chosen read: "Before the app phase: I price the tiles
properly first (sizes, which hold roads in each band), then dispatch the workflow edits, you run the release, and a coder
builds the new group. Several sessions plus your release run and an E1 ruling."

Marks: [C] confirmed by reading or running, with the place named; [I] inferred; [?] not determined.

## 1. The result in short

- **Five tiles cover all 8 rows**, the smallest candidate of each band in every case. None needed a larger fallback.
- **Each tile holds nodes in every row it is meant to cover**, counted twice, by the port's reader and by `LBGenerator`
  (the oracle's reader), with equal counts.
- **They total 29,773,970 bytes** today.
- **All five open** in the port's `checkDataFile` and in the oracle's `TileCheck`. `TileCheck` gave PASS on all five, each
  with one route.
- **`LBGenerator` as it stands is the wrong generator for the new group.** It draws node-weighted, so one row takes 68% of
  the draws, and in the first 150 cases row 1079 was never drawn. A per-row draw reaches 59 in-band cases in every row
  within 92 to 189 draws (§3.3).
- **E1 is the owner's question.** Five new tiles need route ends for `capture.sh`'s check unless the check changes. Four
  alternatives that commit no new coordinates are in §5.3.
- **A finding outside the brief:** `tools/tile-check/tiles.tsv` already commits 12 coordinates that are not Forager's own
  ends, and `scan.sh` cannot see them (§5.2).

## 2. Part 1: the tiles, measured

### 2.1 The rows and their bands

A row r is `ilat` from r × 100,000 to (r + 1) × 100,000 (`LBGenerator.kt:14-16`; `CheapRuler.kt:53`). The 8 rows, with
the 5-degree tile band each lies in (whole degrees only, E1):

| rows | band | candidates the brief named |
|---|---|---|
| 474, 488 | 45 to 40 south | E145_S45, W75_S45, E170_S45 |
| 502 | 40 to 35 south | E170_S40, E175_S40 |
| 663, 675 | 25 to 20 south | E15_S25, E25_S25, W50_S25 |
| 1046 | 10 to 15 north | W95_N10, W20_N10, E120_N10 |
| 1053, 1079 | 15 to 20 north | W20_N15, W95_N15, E100_N15 |

### 2.2 The five tiles, today's bytes

Downloaded 2026-11-14 from 06:27 to 06:28 UTC from `https://brouter.de/brouter/segments4/<name>.rd5`, into
`.../scratchpad/tiles72/`, never into the clone. The request carried a generic user agent and nothing else.

| tile | bytes | SHA-256 | Last-Modified | ETag |
|---|---:|---|---|---|
| W20_N15 | 2,622,004 | `4a75d41fa9df78bdc6fd4390acbee84cb1fd9ad8db45c277fd1296b6f247a387` | Sat, 14 Nov 2026 01:03:01 GMT | `"6af7b345-280234"` |
| E15_S25 | 4,854,579 | `53122860d6c615ea8f2206251c8e09c32d67d500605c9342f0560e1963d1541a` | Sat, 14 Nov 2026 01:03:01 GMT | `"6af7b345-4a1333"` |
| E145_S45 | 6,314,148 | `36ed6746f38a4c8446c8204c34c8dda79b12067dfaab7e073a3fc47a0481713a` | Sat, 14 Nov 2026 01:03:01 GMT | `"6af7b345-6058a4"` |
| W95_N10 | 7,472,321 | `dd17b04afaf86f3bc07afc1f3ba91b14e83882a2b6681f972f11b5229e08464e` | Sat, 14 Nov 2026 01:03:01 GMT | `"6af7b345-7204c1"` |
| E170_S40 | 8,510,918 | `651632d7f3dcac284a12c5c82d8aa801a0bc87b65592c38f7208201ff1f0b344` | Sat, 14 Nov 2026 01:03:01 GMT | `"6af7b345-81ddc6"` |
| **total** | **29,773,970** | | | |

**These bytes are today's only.** brouter.de rewrites its tiles in place (`tools/fetch-tiles.sh:11-12`; RECORD -45: the
release's tiles carried `Last-Modified` 2026-10-30 01:03:01, these carry 2026-11-14 01:03:01). The published release will
hold whatever the server serves on the day of the owner's run, with its own sizes and digests. Every count below is for
these bytes. The release's counts must be measured again on the release's bytes (§6, dispatch 4).

Each size equals the planner's figure from brouter.de's headers byte for byte. Equal size does not show equal content.

### 2.3 Nodes in each row

**How measured** [C]: a scratch test, never committed, run once through Gradle's `jvmTest` on this clone (log
`.../scratchpad/s72-gradle.log`, 0 lines matching `^e: ` or ` e: `, `BUILD SUCCESSFUL`). It was deleted after the runs,
and `git status --porcelain --untracked-files=all` then printed nothing, with HEAD still `6e2d879`. A saved copy is at
`.../scratchpad/S72MeasureTest.kt.saved`.

- **The port's reader:** `PhysicalFile` (`MapaccessEngines.portPhysical`), then `OsmFile` per degree square and
  `createMicroCache(..., reallyDecode = true, ...)` per cell (`OsmFile.kt:113`). Every node's `ilat` was binned by
  `ilat / 100,000`. No node fell outside its tile's latitudes.
- **`LBGenerator`**, with each tile alone in its own folder: `cRows` and `bandCounts` (`LBGenerator.kt:29-38`, which reads
  through `TileNodes`, the oracle's reader).

| tile | rows covered | nodes in the row (port reader) | `LBGenerator.bandCounts` | equal | tile's nodes | degree squares with data |
|---|---|---|---|---|---:|---|
| E145_S45 | 474, 488 | 474: 5,159; 488: 16,239 | {474=5159, 488=16239} | yes | 231,750 | 16 of 25 |
| E170_S40 | 502 | 502: 1,124 | {502=1124} | yes | 554,299 | 8 of 25 |
| E15_S25 | 663, 675 | 663: 2,523; 675: 7,788 | {663=2523, 675=7788} | yes | 127,474 | 25 of 25 |
| W95_N10 | 1046 | 1046: 96,104 | {1046=96104} | yes | 414,279 | 5 of 25 |
| W20_N15 | 1053, 1079 | 1053: 10,205; 1079: 2,461 | {1053=10205, 1079=2461} | yes | 174,360 | 11 of 25 |

- **`LBGenerator` finds every row** [C]. On each tile alone, `cRows` is 13 and `bands` holds exactly that tile's target
  rows. With all five tiles in one folder, `bands` holds exactly the 8 rows.
- **"Road nodes"** here means the nodes the tile stores. BRouter's tiles hold the routing network's nodes. That they are
  all on ways the hiking profile can use is [I], not checked. §3.3 routes on them.
- **The smallest row is 502,** with 1,124 nodes, 0.2% of E170_S40's. It still gave 59 in-band routes in 122 draws (§3.3).

### 2.4 Each tile opens

- **The port's `checkDataFile`** (`Route.kt:102`): `null`, meaning it reads, for all five [C].
- **The oracle's header read** (`MapaccessEngines.oracleHeaderProblem`): `null` for all five [C].
- **The oracle's `TileCheck`** (`tools/tile-check/TileCheck.java` at `6e2d879`), compiled against `brouter-1.7.10-ro.jar`
  from `brouter/build/oracle` and run with `-XX:-UseLibmIntrinsic`, as `capture.sh:43-44` runs it. Its route ends were
  each tile's first accepted `LBGenerator` draw, written to a scratch file and never printed here. Results [C]:

| tile | lookup version (tile / lookups.dat) | version-checked open | checkFileIntegrity | cells decoded, non-empty, nodes | route | result |
|---|---|---|---|---|---|---|
| E145_S45 | 11 / 11.2 | ok | ok (null) | 16,384, 6,210, 231,750 | 50 nodes, 1,512 m | PASS |
| E15_S25 | 11 / 11.2 | ok | ok (null) | 25,600, 13,952, 127,474 | 104 nodes, 4,437 m | PASS |
| E170_S40 | 11 / 11.2 | ok | ok (null) | 8,192, 3,074, 554,299 | 29 nodes, 3,033 m | PASS |
| W20_N15 | 11 / 11.2 | ok | ok (null) | 11,264, 3,774, 174,360 | 99 nodes, 6,621 m | PASS |
| W95_N10 | 11 / 11.2 | ok | ok (null) | 5,120, 2,417, 414,279 | 237 nodes, 6,503 m | PASS |

- **The two readers agree.** The oracle's decoded node count equals the port reader's total for every tile.
- **The JDK differed from CI's.** This was the container's OpenJDK 21.0.12, not CI's Temurin 21.0.13+9. `TileCheck`
  decodes and routes; nothing in it depends on the JDK build that I know of [I].

## 3. How the new group would see the bands

### 3.1 `LBGenerator` on all five, as it stands

With the five tiles in one folder (symlinks in scratch) and LB's seed 20261207 [C]:

- **Shares of the draw** (node-weighted, `LBGenerator.kt:41-44, 81-86`): 474 3.6%, 488 11.5%, 502 0.8%, 663 1.8%,
  675 5.5%, 1046 67.9%, 1053 7.2%, 1079 1.7%.
- **1,180 draws** (`E3Targeted.CAP`) gave 1,101 accepted geometries. By start row: 474 44, 488 139, 502 6, 663 18,
  675 66, 1046 743, 1053 71, 1079 14. Rejected: 48 for no node in the target's cells, 3 for the end equal to the start,
  28 for the end outside the band.
- **The first 150 accepted, routed in process** (oracle `E3CaseRun.run`, unlogged). Per start row, in the form
  [drawn, oracle routed, oracle route inside the band]:
  - 474 [6, 3, 3]; 488 [18, 15, 9]; 502 [3, 2, 1]; 663 [1, 1, 0]; 675 [6, 5, 3]; 1046 [107, 103, 83]; 1053 [9, 9, 6];
  - **1079 was never drawn.**
- **So a 59-case group built like LB's** (first 59 in-band in draw order, `E3LBTest.kt:35-41`) would be mostly row 1046.
  It would very likely miss row 1079 and probably 663 [I, from the shares; not run to 59].

### 3.2 Who lists the shared folder: why the new tiles need a folder of their own

`git grep` for `listFiles` and `.list()` under `brouter/` finds two readers of a tiles folder by listing [C]:
- `RGenerator.kt:36` (every `.rd5`, unless `only` names one);
- `LBGenerator.kt:26` (every `.rd5`).

`DLGenerator.kt:40` and `NTGenerator.kt:41-42` name their tiles. The engine opens a tile by its name. Callers that pass
`TestTiles.segments` [C, `git grep -n 'RGenerator(\|LBGenerator('`]:

- **`RGenerator` without `only`, so a new tile changes the draw.** `RGenerator.kt:96` draws a tile uniformly by
  `rnd.nextInt(tiles.size)`, so one more tile changes every later draw:
  - `RouteTest.kt:69`, `GraphHalfTest.kt:43`, `WaypointMatchTest.kt:32`;
  - `E3CRTest.kt:29`, `E3R.kt:22`, `E3RSequenceTest.kt:24`;
  - `RoutingEngineProductionRecordTest.kt:161, 178`, `RoutingEngineRTest.kt:39`, `RoutingEngineStep9Test.kt:60`;
  - `StartPathTest.kt:58`, `HintsFormatsRun.kt:79, 112`, `NtCasesWriteTest.kt:82, 130`.
- **`E3RSequenceTest.kt:36` would fail outright.** It holds R's sequence to a pinned SHA-256.
- **Every stored R and CR row would stop being drawn.** The stored rows in `expected-1b.tsv` are matched by draw id, so
  `seen` would never fill.
- **`RGenerator` with `only`, so no change:** NT, the memory groups, FK.
- **`LBGenerator`:** `E3LBTest.kt:21`, `RoutingEngineProductionRecordTest.kt:83`, `RoutingEngineStep9Test.kt:46`,
  `HintsFormatsRun.kt:74`, `NtCasesWriteTest.kt:77`. The new rows would join `bands` and `cumulative`
  (`LBGenerator.kt:36-44`), so LB's draws would be reshuffled the same way.

**The claim holds:** a new tile in `segments/` reshuffles R, CR and LB and breaks the R sequence pin. The new tiles go in a
folder of their own, as F0h's and the drop-off fixture's do (`TestTiles.kt:25-37`). `TilePinsTest.kt:45` asserts exactly
5 pinned files, from its own pin file. It does not list a folder, so a separate pin file leaves it alone [C].

### 3.3 A per-row draw reaches every row

This was a scratch variant of LB's own candidate (`LBGenerator.kt:81-98`, copied, not changed). The start is drawn
uniformly from one row's nodes only, one sequence per row (seed 20261207 + row, my scratch choice). It drew until 59
routes lay inside the band, by `E3LBTest.kt:36-40`'s rule on the oracle's extent, or until 1,180 draws. Each in-band case
was also run through the port with `MsunLibm` wrapped in a counting `LibmSeam` (§4.5) [C]:

| row | draws | rejected before routing | oracle routed | routes in the band | port read the row (of in-band) | oracle + port, ms |
|---|---:|---:|---:|---:|---:|---:|
| 474 | 189 | 17 | 87 | 59 | 59 | 4,308 |
| 488 | 119 | 7 | 99 | 59 | 59 | 7,820 |
| 502 | 122 | 35 | 74 | 59 | 59 | 604 |
| 663 | 123 | 14 | 96 | 59 | 59 | 2,301 |
| 675 | 119 | 13 | 90 | 59 | 59 | 4,878 |
| 1046 | 92 | 2 | 87 | 59 | 59 | 25,348 |
| 1053 | 106 | 13 | 88 | 59 | 59 | 2,696 |
| 1079 | 95 | 26 | 69 | 59 | 59 | 9,002 |

"Oracle routed" counts every routed draw, in band or not; the loop stopped at 59 in band.

**A scratch observation, beyond what was asked, and not evidence for the record:**
- On all 472 in-band cases, and on all 150 of §3.1's, the port on msun's table gave the oracle's six fields.
- It was one local run, on today's unpinned bytes, on the container's JDK, with no stability check and no planted error.
  It says the group, once built, has a chance of being green. It does not answer -24's question for these rows.

## 4. Part 2: how tiles are captured, published, pinned, fetched and used today, and what a second set changes

### 4.1 `publish-tiles.yml` on `main`

`git show origin/main:.github/workflows/publish-tiles.yml`, SHA-256 `6b0bedba...a54fe7ae`, equal to -44's copy. The copy on
`brouter-port-5b` is byte-equal (`git diff --stat` empty).

**What it hard-codes** [C]:
- `TOOLS_SHA: 5d0076179af84f0dd88b57ae83898ee6db86f1b9` (`:18`). Both jobs check out that commit (`:31-33`, `:67-70`), so
  `capture.sh`, `tiles.tsv`, `TileCheck.java` and the templates are that commit's.
- F0's URL, size and SHA-256 (`:20-22`).
- The capture command, `tools/tile-check/capture.sh out https://brouter.de/brouter/segments4 "$F0_URL" ...` (`:46`).
- **Seven assets:** `if [ "$count" != 7 ]` (`:93`), and the step name "with the seven assets" (`:86`).
- The guard: `workflow_dispatch` from `refs/heads/main` only (`:57`). The push trigger names `brouter-port` only (`:9-11`),
  so a push to `brouter-port-5b` runs nothing.
- The release target, `--target "$GITHUB_SHA"` (`:88`; RECORD -43).

**What refuses a rerun** [C]:
- `:75-81` refuses when the tag in `out/tag`, or a release with it, exists.
- The tag is `brouter-tiles-<UTC date of the capture>` (`capture.sh:99`). So a rerun on 2026-10-30 is refused. On any
  other day it is not refused: it captures fresh copies of the same four tiles and F0 under a new tag. The existing
  release is never touched either way. The four are recaptured unless `tiles.tsv` at `TOOLS_SHA` changes.

**The smallest edit** that publishes the new tiles as a new release, leaves `brouter-tiles-2026-10-30` alone and recaptures
none of the four. Two shapes; the planner's call, each needing the owner's word for `main`:

- **(P1) Edit `publish-tiles.yml` in place.** Three changes in effect:
  1. `TOOLS_SHA` (`:18`) to a new tools commit holding the new capture's files (§4.2);
  2. the capture `run:` (`:46`) to the new script, e.g. `tools/tile-check/capture-cosrows.sh out
     https://brouter.de/brouter/segments4 "$F0_URL" "$F0_BYTES" "$F0_SHA256"`;
  3. the count (`:93`, and `:86`'s name) to the new release's asset count: 5 tiles + `README.md` + `tile-pins.tsv` = 7
     if F0 is a control only and not an asset, 8 if F0 is attached again.

  The refusal (`:75-81`), the readback (`:90-97`) and the target are reused unchanged. The file then no longer
  reproduces the 2026-10-30 run; history keeps that version.
- **(P2) A second workflow file,** e.g. `publish-cosrows-tiles.yml`: a copy of the first with those three lines changed.
  The first file stays as it is. That matches Forager's "new capability is a new function, class, or path", at the cost
  of a second file on `main`.

**Either way the new script must write a distinct tag prefix,** e.g. `brouter-cosrows-tiles-<date>`, so the release cannot
be taken for, or collide with, a later `brouter-tiles-<date>` [I: a design point, not a measured failure].

### 4.2 `capture.sh` and `tiles.tsv`: what they assume, and what each new tile's check needs

**`capture.sh` assumes** [C, `tools/tile-check/capture.sh`]:
- `tiles.tsv` sits beside it, by name (`:76`). Each row is `tile lat1 lon1 lat2 lon2`, and every row needs four ends for
  the route `check` (`:58, 70`).
- **F0's control needs a `W75_N40` row.** `f0_ends` is set only on that row (`:60`), and F0 is checked between those ends
  (`:83`). Under `set -u`, a list without `W75_N40` stops at `:83` with an unbound variable [I: read, not run].
- **Exactly 5 pin rows** (`:102-103`): four tiles and F0.
- Every tile's pin path is `segments/<tile>.rd5` (`:74`), and F0's is `f0/W75_N40.rd5` (`:84`).
- The README and notes templates are `tools/tile-release/README.md` and `release-notes.md` (`:97-98`). Both say "four"
  tiles (`README.md` "The four brouter.de tiles"; `release-notes.md:1`), so a new release needs its own templates.
- The tag prefix is `brouter-tiles-` (`:99`).
- `TileCheck` is given the ends as arguments and prints them on its route line (`TileCheck.java:81`), so they appear in
  the public Actions log. E1 speaks of commits. Whether a public log counts is for the owner [?].

**What each new tile's check needs:**
- **Today's check:** the lookup version, the version-checked open, `checkFileIntegrity`, every cell decoded with a
  non-zero count, and one route between two committed ends (`TileCheck.java:37-83`). The route needs four numbers per
  tile.
- **What this new set exists for:** that each tile holds nodes in the rows it covers. That needs no coordinate. It needs
  the tile's row list, numbers 474 to 1079, which E1 does not reach.

### 4.3 The E1 question, for the owner

E1 as the record holds it (RECORD -84): "E1 stands as written: no coordinates are committed." Forager's own route ends are
the one allowance (RECORD -58, "never coordinates beyond Forager's own ends (E1, as F0)").

**Committing the ends as today** would put 20 new numbers in `tools/tile-check/tiles-cosrows.tsv`: four ends for each of
five tiles. They would be chosen points in each tile, like the town points the existing three rows hold. That needs the
owner's explicit exception to E1.

**Alternatives that commit no new coordinate.** This goes to the owner as a ruling; I do not choose.

- **(E1-a) No route in the capture check.** `TileCheck` keeps the lookup version, the open, the integrity check and the
  decode count, and drops the route for these tiles.
  - Proves: the bytes are an intact rd5 that 1.7.10 opens and decodes.
  - Loses: proof at capture time that 1.7.10 routes on the tile. The new group's selection routes 59 per row through the
    oracle later, so a tile that cannot route is caught one dispatch later, after the release exists.
- **(E1-b) Ends derived at run time from the tile.** The check picks its two ends from the tile's own nodes by a fixed
  rule in code, for example the first in-band `LBGenerator`-style draw with a fixed seed, as this session's `TileCheck`
  run did.
  - Proves: what today's route check proves, on every tile.
  - Loses: chosen, human-readable route ends. The ends depend on the captured bytes, so they cannot be named before the
    run.
  - It needs `TileCheck` changed so the route line prints counts only, not the ends. Otherwise the ends go into the
    public log.
- **(E1-c) A row check in place of the route.** The capture counts each tile's nodes per target row and refuses the tile
  unless every row it is listed for has nodes. `tiles-cosrows.tsv` then holds `tile` and its row numbers only.
  - Proves: exactly the property this release exists for, on the release's own bytes, before publishing.
  - Loses: the route, as (E1-a).
  - It can be combined with (E1-b) to keep a route too.
- **(E1-d) Ends committed as Forager-style ends.** Not available: Forager has no route ends in these tiles, so this falls
  back to the exception above.

### 4.4 Pins, fetch, folders: how a second set would be wired, by analogy with `fetchF0h`

**Today** [C]:
- **The fetch scripts.** `tools/fetch-tiles.sh <dir> <base url> [pin file]` (`:21-24`) fetches every row of a pin file
  through `fetch-pinned.sh`, by size and SHA-256. It takes the pin file as its third argument, so it serves a second set
  unchanged. `fetch-pinned.sh` keeps a file already matching its pins (`:42-47`).
- **`fetchTiles`** (`brouter/build.gradle.kts:40-58`): the release URL `brouter-tiles-2026-10-30` (`:40`), `build/tiles`
  (`:41`) and `tools/tile-pins.tsv` (`:42`).
- **`fetchF0h`** (`:60-72`): `tools/fetch-f0h.sh` into `build/tiles-f0h`.
- **The test JVM.** `oracleJvm()` (`:126-152`) makes every oracle test JVM depend on `fetchOracle, msunReference,
  fetchTiles, fetchF0h, fetchDropoff` (`:127`). It passes the folders as system properties (`:128-133`):
  `brouter.tiles.dir`, `brouter.tiles.f0h.dir`, `brouter.tiles.dropoff.dir`, `brouter.tiles.pins`. `jvmTest` and
  `e3Strata` both call it (`:155`, `:172`).
- **`TestTiles`** (`TestTiles.kt:10-42`) reads them: `segments`, `f0`, `f0h`, `dropoff`, `pinFile`.
- **The simulator.** The `ios-smoke` job runs `fetchTiles fetchOracle` explicitly (`brouter.yml:107-108`). The JVM writer
  step then writes case files (`:113-116`). The simulator gets `SIMCTL_CHILD_BROUTER_TILES_DIR`, `_ORACLE_DIR` and
  `_STEP10_NT_CASES` (`:117-124`). A case line with an eighth field carries its own tiles folder (`Step10Cases.kt:24, 78`;
  `NtCasesWriteTest.kt:151`), which is how F0h and FK 7 and 8 reach the simulator with no variable of their own.

**A second set, by analogy (what would change):**
1. **`tools/tile-pins-cosrows.tsv`:** a byte-for-byte copy of the new release's `tile-pins.tsv`, as `tools/tile-pins.tsv`
   is of the first (`fetch-tiles.sh:14-15`).
2. **`brouter/build.gradle.kts`:** a `tilesCosRowsRelease` URL, a `cosRowsDir = build/tiles-cosrows`, and a
   `fetchCosRowsTiles` Exec running `fetch-tiles.sh <dir> <url> <pin file>`, with inputs and outputs as `fetchTiles` has.
   Add it to `oracleJvm()`'s `dependsOn` and a `systemProperty("brouter.tiles.cosrows.dir", ...)`. With `capture.sh`'s
   path rule the tiles land in `build/tiles-cosrows/segments/`, a folder no existing test lists.
3. **`TestTiles`:** a `cosRows` property reading that property, `File(dir, "segments")`.
4. **A pin test like `TilePinsTest`** for the new file, seen failing first with one size off by one, as -45's step 1 was.
5. **`brouter.yml`:**
   - Add the new task to `ios-smoke`'s and `ios-memory`'s explicit fetch steps (`:108`, `:188`) for clarity. It would
     run anyway through the writer's `jvmTest`, by `oracleJvm()`.
   - **No new `SIMCTL_CHILD_` variable** if the writer puts the new folder in each line's eighth field [I: read from
     `Step10Cases.kt:78`, not run].
   - `jvm` and `jvm-production` pick the fetch up from `oracleJvm()` with no workflow change.

### 4.5 The new test group

**The generator.**
- **Not `LBGenerator` unchanged** (§3.1). A new class, beside it and not threaded into it, per Forager's "new capability
  is a new function, class, or path".
- **What it does:** `LBGenerator`'s draw with the start drawn from one named row, one `java.util.Random` sequence per
  row, on `TestTiles.cosRows`.
- **The seed or seeds** are fixed in the class and pushed before any run, as `E3LBTest.kt:51-53`'s 20261207.
- **Size** is the planner's call, with numbers:
  - 59 per row is 472 cases, E's n per stratum (`E3Expected.kt:29-42`);
  - a smaller quota, e.g. 8 per row, is 64 cases;
  - §3.3 shows 59 per row is reachable within 189 draws on these bytes.

**Making sure the 8 rows are read, and asserting it.**
- **How -137's counter did it:** a scratch patch at `CheapRuler.kt:53`, `getLonLatToMeterScales`, never committed
  (`docs/audits/2026-11-12-brouter-port-step9/41-rows-touched-run.txt:1`).
- **A committed test needs no `commonMain` change.** `RoutingContext(internal val libm: LibmSeam = MsunLibm)`
  (`RoutingContext.kt:19`) takes the seam, and every row read goes through `LibmSeam.scales(row)` (`LibmSeam.kt:16`,
  called from `CheapRuler.kt:53`).
- **The counting seam:** a test-only `LibmSeam` that wraps `OracleLibm` or `MsunLibm` and records each `row` passed to
  `scales`. This session's scratch did exactly that (§3.3).
- **The two assertions:**
  - per case: the case's own row is in its set;
  - per group: the union of rows read, intersected with the 13 `cRows`, equals {474, 488, 502, 663, 675, 1046, 1053,
    1079}.

  A group that never reaches a row then fails by name.
- **On the simulator:** `EngineRun.runTrack(..., libm: LibmSeam = MsunLibm)` (`EngineRun.kt:46-52`) takes a seam, so the
  common test can count the same way.
- **The planted error that proves the comparison bites:** a scratch change to the production table at one of the 8 rows
  must make the production record differ, as -136's scratch change made LB differ on 59 of 59.

**Where the expected rows go.**
- **Today:** the oracle's tuples are written to `brouter/src/jvmTest/resources/e3/expected-1b.tsv` (2,134 lines), by
  `E3ExpectedWriteTest` from each stratum's stability table listed in `E3Expected.STRATA` (`E3Expected.kt:29-42`). The
  simulator's copy is a constant per group (`LbExpected.ROWS`), held to the file by `Step10CommonDigestTest.kt:38`.
- **For the new group,** either:
  - a new `STRATA` entry and rows appended to `expected-1b.tsv`; or
  - a separate resource, so 1b's file stays 1b's.

  The planner's call. Either way no coordinate is stored (`E3Expected.kt:10`).

**Stability.** Selection and stability run in the `e3Strata` task (`build.gradle.kts:169-184`), on Temurin 21.0.13+9 only,
locally, as `E3LBTest` did. LB's run was 99 draws, 59 of 59 stable, in 4 m 18 s
(`docs/audits/2026-10-29-brouter-port-phase2/step1b/25-lb-e3Strata.log`).

**Jobs.**
- **Logic run in `jvm`:** a class like `RoutingEngineStep9Test` with `OracleLibm`, plus the row assertion.
- **Production run in `jvm-production`:** a group added to `RoutingEngineProductionRecordTest`, which records and gates
  its own counts only (`brouter.yml:263-279`).
- **The simulator:**
  - a new commonTest class through `Step10Cases.check`, beside `LbEngineCommonTest`;
  - its expected constant;
  - its lines in `NtCasesWriteTest`, with the eighth field.
- **The gate:** a new line in `SIMULATOR_CLASSES` (`brouter.yml:61-85`), e.g.
  `com.zynergylabs.forager.brouter.tiles.CosRowsEngineCommonTest`. A class not listed there is not gated
  (`brouter.yml:57-60`).

## 5. CI cost

**Download.**
- **29,773,970 bytes more** in each job whose Gradle run reaches the new fetch: `jvm`, `jvm-production`, `ios-smoke` and
  `ios-memory`, through `oracleJvm()` and the writer's `jvmTest`.
- **About 119 MB per push** across the four jobs. Today's tile set is 129,845,193 bytes per job (four tiles and F0, from
  `tools/tile-pins.tsv`), so this is 23% more.
- **Time:** in session 68's saved `jvm` job log (job 132233342101, a scratch copy, lines 189-201), the five pinned
  files printed within about 3 s of each other. That is [I] a download of seconds.

**Test time** [I, from these yardsticks]:
- **Locally, per oracle-plus-port pair:**
  - 83 ms over §3.3's 690 routed draws (56,957 ms in all);
  - 180 ms over §3.1's node-weighted 150 (86 ms oracle, 94 ms port);
  - row 1046 is the slow row, about 290 ms a pair.
- **In CI:** LB's 59 cases took 3.9 s in `jvm-production` and 28 s on the simulator (saved CI logs of session 68, jobs
  `jvm-production` and `ios-smoke`), a ratio of about 7. Step 10's memory groups measured 11.2
  (`2026-11-12-brouter-port-step10.md:412-421`).
- **Estimates:**

| group size | `jvm` logic run | `jvm-production` | `ios-smoke` simulator | e3Strata selection, local |
|---|---|---|---|---|
| 8 × 59 = 472 | 40 to 85 s | 40 to 85 s | 5 to 8 min (port share × 7 to 11) | about 35 to 60 min (LB's 4 m 18 s per 59, × 8) |
| 8 × 8 = 64 | 5 to 12 s | 5 to 12 s | 0.7 to 1.1 min | a few minutes |

- **Headroom** on the head's runs (job wall clock from the session 68 logs):
  - `jvm` about 34 of 60 min;
  - `ios-smoke` about 21 of 45;
  - `jvm-production` about 24 of 45;
  - `ios-memory` about 66 of 95. The writer only writes the new file there, which takes seconds.

## 6. The order of work, one dispatch each

Steps marked **OWNER** need the owner.

1. **OWNER: the E1 ruling** (§4.3): committed ends, (E1-a), (E1-b), (E1-c), or (E1-b) with (E1-c). Everything after
   depends on it. The planner also rules (P1) or (P2) (§4.1) and the group's size (§4.5). Those are technical, but (P2)
   adds a file to `main`.
2. **A coder, on `brouter-port-5b`, tools only:**
   - the new capture script and its tile list (by the ruling: tile names and rows, or with ends);
   - the `TileCheck` change if (E1-a/b/c), with its route line printing no ends;
   - the new README and notes templates (five tiles, ODbL notice as today);
   - the distinct tag prefix.
   - **The dry run:** the push trigger names `brouter-port` only (`publish-tiles.yml:9-11`), so the coder runs the
     script locally into scratch against brouter.de, as this session reached it, and reports PASS per tile and the row
     counts.
   - The coder then edits the workflow (P1 or P2) on the branch with `TOOLS_SHA` set to that tools commit.
3. **OWNER: the word for `main`.** The planner copies the workflow file to `main` byte for byte, as -44 did. -30's word
   covered one file, copied unchanged, so this edit needs a new word.
4. **OWNER: the release run.** On GitHub:
   1. open `slayer8366/Forager-iOS` and select the **Actions** tab;
   2. in the left list, choose **publish-tiles** (or the new file's name under P2);
   3. click **Run workflow**;
   4. leave **Use workflow from** at **Branch: main** (the guard at `:57` refuses anything else);
   5. click the green **Run workflow**.

   There are no inputs: the workflow declares `workflow_dispatch` with none (`:8`). Both jobs must end green. The
   release is then published by `:98-99`.
5. **A coder: pin.**
   - `tools/tile-pins-cosrows.tsv` as a copy of the release's;
   - the fetch task, the property, `TestTiles.cosRows` and the pin test, seen failing first;
   - **re-measure §2.3's counts on the release's bytes,** which are not today's.
6. **A coder: the group's selection.**
   - The per-row generator, its seed or seeds pushed first, and the e3Strata stratum test with the row assertion;
   - selection and stability locally on the pinned Temurin;
   - expected rows written where the planner ruled.
7. **A coder: the runs.**
   - The logic class in `jvm`, the production group in `jvm-production`, the writer lines, the commonTest class and
     constant, and the `SIMULATOR_CLASSES` line;
   - the counting-seam assertion in each;
   - the planted table error seen to bite;
   - CI read (`ios-smoke` simulator-checked).
8. **The planner** reads CI and records. The owner hears the answer for the 8 rows.

## 7. Confirmed vs inferred

- **Confirmed by running:**
  - the base head and clean tree;
  - the five downloads, sizes, digests and headers;
  - the port reader's per-row counts and their equality with `LBGenerator.bandCounts`;
  - `checkDataFile` and the oracle header read on all five;
  - `TileCheck` PASS on all five, its node totals equal to the port reader's;
  - §3.1's draw shares and first-150 routing;
  - §3.3's per-row reach;
  - the scratch file removed and the tree clean after.
- **Confirmed by reading** (file and line given in place):
  - every hard-coded value and refusal in `publish-tiles.yml` and `capture.sh`;
  - the fetch wiring;
  - the callers that list `segments`;
  - `E3RSequenceTest`'s pin;
  - the counting seam's place;
  - the simulator's eighth field;
  - the `SIMULATOR_CLASSES` gate.
- **Inferred:**
  - `capture.sh` failing at `:83` without a `W75_N40` row (not run);
  - that no new `SIMCTL_CHILD_` variable is needed;
  - every CI time in §5;
  - that the stored nodes all lie on usable ways;
  - that a 59-case node-weighted group would miss 1079 and 663 (from the shares, not run to 59).

## 8. Could not determine

- **The release's own bytes and counts.** They exist only after the owner's run. Today's will not be them.
- **Whether a route end printed in a public Actions log falls under E1.** E1 as recorded speaks of commits.
- **Simulator time for the new rows.** Estimated by ratio only. Row 1046's longer routes may cost more there.
- **The CI JDK.** Every measurement here ran on the container's OpenJDK 21.0.12, not CI's Temurin 21.0.13+9.
- **Which RECORD copy is current.** The record read is `/home/user/Forager-iOS/RECORD.md` (7,099 lines), as the brief
  says. `origin/main`'s `RECORD.md` is 6,184 lines.

## 9. Premises in this brief or the record that were wrong

- **"`tiles.tsv` commits a route's ends per tile. Rule E1 is no coordinates committed beyond Forager's own route ends."**
  `tools/tile-check/tiles.tsv` rows 6 to 8 already commit 12 numbers that are not Forager's ends. They are the town points
  for `W75_N35`, `W70_N40` and `E170_S50`, at 4 decimals, committed in `aa144a0` on 2026-10-29, before -84 restated E1.
  None is in `forager-ends.txt` (checked by exact value). `scan.sh`'s pattern needs 5 or more decimals, so it cannot see
  them. Reported, not touched. Whether they stand is the owner's question, beside §4.3's.
- **"No test reaches" the 8 rows:** true as far as checked. LB's own log names only the five in-tile rows in `bands`
  (`25-lb-e3Strata.log`), and -137's counter read only those five.
- **The planner's sizes:** all five equal today's bytes exactly. Not wrong, but equal size is not equal content.
- **"Dispatch the workflow edits, you run the release":** one more step sits between. The push-triggered dry run does not
  fire for `brouter-port-5b` (`publish-tiles.yml:9-11`), so the tools commit is dry-run locally, or the trigger changes.
- **"A coder builds the new group":** the group's selection runs in `e3Strata`, locally on the pinned Temurin only
  (`build.gradle.kts:169-178`), not in CI. Its expected rows come from that local run.

## 10. Decided beyond scope

- **I ran `TileCheck` with ends derived at run time** from each tile (first accepted `LBGenerator` draw), written to a
  scratch file only. This was to check the oracle opens and routes on each tile without committing or printing a
  coordinate.
- **I ran a per-row draw** (§3.3) that is not in the tree, to show a row-stratified generator reaches every row. Its seed
  rule (20261207 + row) is mine, for scratch only.
- **I compared the port on msun's table with the oracle** on those cases (§3.3, 472 of 472 and 150 of 150 equal). It was
  not asked for and is not evidence for the record (no stability, no plant, unpinned bytes).
- **I added the E1 option (E1-c)** and the tag-prefix point (§4.1). Both are offered, not chosen.
