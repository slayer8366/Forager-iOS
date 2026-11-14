# Session 73: capture tooling for the five cosine-row tiles

Plumb Ledger, session 73, dispatched by the planner. Branch `brouter-port-5b`, base
`2bc6e2cc2393ee344d45e13ecba0833dfd41daf4` (checked: `git ls-remote origin brouter-port-5b` gave that head; `git status
--short` printed nothing). Results are written into this file as they come; sections marked "pending" are not run yet.

Marks: [C] confirmed by reading or running, with the place named; [I] inferred; [?] not determined.

## 1. The owner's rulings, verbatim

- On the 8 cosine rows no test reaches: "Cover them now".
- On how the five new tiles are checked at capture: "Run-time ends + row counts (Recommended)", whose text was: "Nothing
  committed. The check picks route ends from the tile's own nodes by a fixed rule each run, routes between them in the
  oracle, and also counts road nodes in each of the 8 rows. The ends are no longer printed to the public Actions log.
  Keeps the proof that the oracle routes on the tile and proves the rows are there. Most work of the three."
- E1 stands: "no coordinates are committed", with Forager's own route ends allowed, and one exception the owner made: the
  three existing pairs of town points in `tools/tile-check/tiles.tsv` stay.

The planner's decisions (the planner's, not the owner's): a second workflow file with its own capture script and its own
tag prefix; `publish-tiles.yml`, `capture.sh` and `tiles.tsv` not edited; the five tiles `E145_S45` (rows 474, 488),
`E170_S40` (502), `E15_S25` (663, 675), `W95_N10` (1046), `W20_N15` (1053, 1079).

## 2. What I rechecked from session 72's report (a claim about `6e2d879`)

- `tools/tile-check/` at `2bc6e2c` holds `TileCheck.java` (87 lines), `capture.sh` (107), `tiles.tsv` (8) [C, `wc -l`].
  The only commit since `6e2d879` is `2bc6e2c`, the report itself, docs only [C, `git show --stat`]. So §4.2's reading of
  `capture.sh` and `TileCheck.java` still describes the tree.
- `TileCheck.java:81` prints the route's ends on its route line [C]. The new check must not.
- `fetch-tiles.sh:29` reads `asset path bytes sha256` and skips lines starting `#` (`:30`) [C]. It takes the pin file as
  its third argument (`:24`), so a second pin file needs no change to it.
- `publish-tiles.yml` on `main`: the shape below is modelled on it [C, `git show origin/main:...`].

## 3. Design

### 3.1 New files, nothing threaded into the existing path

| file | what |
|---|---|
| `tools/tile-check/cos-tiles.tsv` | `tile` and `rows` (comma-separated row indices). No coordinates. |
| `tools/tile-check/CosTileCheck.java` | the check, a new class. `TileCheck.java` is not touched. |
| `tools/tile-check/capture-cos.sh` | the capture, by analogy with `capture.sh`. |
| `tools/tile-release-cos/release-notes.md` | the release notes template: the five tiles, the ODbL notice, the files table. |
| `.github/workflows/publish-cos-tiles.yml` | the second workflow, `workflow_dispatch` only. |

### 3.2 The check, per tile (`CosTileCheck`)

Run as `TileCheck` is run (`capture.sh:43-44`): BRouter 1.7.10's `-ro` jar, `-XX:-UseLibmIntrinsic`, Forager's profile
`hiking-mountain.brf`, `alternativeidx=0`, `profile:use_dynamic_range=0`.

1. **As `TileCheck` today**, copied, not shared: lookup version against `lookups.dat`; the version-checked open;
   `checkFileIntegrity`; every cell decoded with `reallyDecode = true`, counting nodes; fail if nothing decodes.
2. **Row counts.** A row r is `ilat` in `[r × 100,000, (r + 1) × 100,000)` (session 72 §2.1). While decoding, every node
   id from `MicroCache.getIdForIndex` is split as BRouter does (`ilon = id >> 32`, `ilat = (int) id`; port
   `OsmNode.kt:51-53`) and binned by `ilat / 100,000`. For each row the tile is listed for, the count is printed and the
   row fails if it is below the floor.
3. **Run-time ends, one route per listed row** (so a two-row tile proves both rows route):
   - B = the ids of the row's nodes, sorted ascending (by `ilon`, then `ilat`), n = |B|.
   - Attempt k, for k = 0 to 15 (16 attempts at most): start = B[⌊n(2k + 1) / 32⌋], the middle node of the k-th of 16
     equal slices of B, west to east.
   - End = the first B[j], j after the start, with `ilon_j − ilon_s ≥ 10,000` and `|ilat_j − ilat_s| ≤ 10,000` (0.01
     degree each way). If there is none, the attempt is "no end" and counts as tried.
   - Route start to end in the oracle, waypoints set from the nodes' own `ilon`/`ilat` (no string, no rounding). The
     attempt passes when `getErrorMessage()` is null and the track has nodes.
   - The first passing attempt is the row's route. The row fails if all 16 fail.
   - Both ends lie in the row's band by construction, so the route's own distance reads the row [I: that a route between
     two in-band ends stays mostly in the band; the count of the track's nodes in the band is printed to show it].
   - Deterministic: the rule uses no clock, no random source and no hash order; the same tile bytes give the same B and
     the same attempts [I until the two runs in §5 agree].
4. **Printed:** counts, node counts, attempt numbers, route length in metres, the track's node count and how many of its
   nodes lie in the band, and a digest: SHA-256 of the chosen start and end ids (8 bytes each, big-endian, rows in listed
   order), first 16 hex digits. **Never printed:** any id, `ilon`, `ilat` or coordinate. A failed attempt's error message
   is printed with every digit replaced by `#`, because BRouter's messages could carry a number I have not foreseen.
5. Ends with `PASS` or `FAIL: <reasons>`, exit 1 on any failure, as `TileCheck` does.

### 3.3 The floor: 500 nodes per row

- Session 72 measured, on 2026-11-14's bytes, 1,124 nodes at the lowest (row 502) and 2,461 at the next (1079) [C, s72
  §2.3]. Row 502 still gave 59 in-band oracle routes in 122 per-row draws (s72 §3.3).
- 500 is under half the lowest measured. brouter.de rebuilds its tiles from OSM data in place; a road network in a 0.1
  degree band losing half its nodes between two rebuilds would be a sign of a broken or partial file, not of map edits
  [I].
- 500 is about 8 times the 59 starts a 59-per-row group would draw, so a row above it has room for that group's
  rejections (35 of 122 draws rejected before routing on row 502 in s72 §3.3) [I, from one measurement].
- The floor is a constant in `CosTileCheck`. The exact counts are not pinned anywhere in the script; each run prints them
  and writes them as comment lines in its pin file (`#` lines, which `fetch-tiles.sh:30` skips), so the release carries
  its own counts beside its pins.

### 3.4 The capture script (`capture-cos.sh <out dir> <tile base url> [tile list]`)

1. Refuses a non-empty `<out>`. Fetches the oracle with `tools/fetch-oracle.sh` into a work folder beside `<out>`, and
   compiles `CosTileCheck` against it. With `COS_ORACLE_ZIP` set, that file is placed where `fetch-oracle.sh` puts the
   zip first, so `fetch-pinned.sh:42-47` checks it against the same size and SHA-256 and does not download it. This is
   for local runs only (this session may reach brouter.de and nothing else); the workflow does not set it.
2. For each tile in the list (default `cos-tiles.tsv` beside it; the optional third argument is for the failing runs in
   §6), in a folder of its own: one download from `<base>/<tile>.rd5` keeping the response headers, then `CosTileCheck`.
   Any failure stops the script with `REFUSED: <tile>: <reason>`. The user agent is `capture.sh`'s, unless `TILE_UA` is
   set (for local runs, so no project detail is sent).
3. Writes `tile-pins.tsv` (columns as `tools/tile-pins.tsv`: asset, path `segments/<tile>.rd5`, bytes, sha256, source,
   last_modified, etag, captured), then the counts as `#` lines, refuses unless exactly 5 data rows, reads the five back
   through `tools/fetch-tiles.sh` against it, and writes the release notes and the tag.
4. Tag: `brouter-cos-tiles-<UTC date>`. The old prefix is `brouter-tiles-` (`capture.sh:99`); no `brouter-tiles-<date>`
   can equal a `brouter-cos-tiles-<date>`.
5. **Assets: exactly six,** the five `.rd5` files and `tile-pins.tsv`, as the brief says ("holding exactly the five tiles
   and their pin file"). The old release also attached a `README.md`; here its content (the licence and the files table)
   goes in the release notes instead. That is my reading of the brief; flagged in §9.
6. No F0 control. The old capture routed F0 as a known-good control for its committed ends (`capture.sh:80-83`); the new
   check has no committed ends to control for, and its failure paths are shown here (§6) instead.

### 3.5 The workflow (`publish-cos-tiles.yml`)

Modelled on `publish-tiles.yml`; the line-by-line differences are in §7 once written.

## 4. Predictions (before any run)

- P1. All five tiles PASS on today's bytes. Row counts equal or near s72 §2.3's (5,159; 16,239; 1,124; 2,523; 7,788;
  96,104; 10,205; 2,461) if brouter.de has not rebuilt since; otherwise near them. All 8 at or above 500.
- P2. Every row finds a route within 16 attempts, most within the first 3 [I: s72 §3.3 rejected 2% to 29% of draws
  before routing].
- P3. Two runs on the same bytes print the same ends digest per tile.
- P4. A row the tile does not hold (row 474 given to `E170_S40`) counts 0 and stops the script naming the tile and the
  floor.
- P5. A truncated tile fails at the open or the integrity check and stops the script naming the tile.
- P6. A pin file with one SHA-256 changed makes `fetch-tiles.sh` refuse that asset by name.
- P7. A route that cannot be found (a scratch profile copy that forbids every way) makes the row fail after 16 attempts.
- P8. The scan prints 0 on every commit, and no output of any run holds a coordinate.

## 5. Dry run against brouter.de (pending)

## 6. Each check seen failing (pending)

## 7. The workflow, line by line against `publish-tiles.yml` (pending)

## 8. What only the real run will show (pending)

## 9. Wrong in the brief, decided beyond scope, could not determine (pending)
