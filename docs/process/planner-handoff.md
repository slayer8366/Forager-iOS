# Planner handoff

Written 2026-11-06, about 18:17Z, at the owner's word: "Write a hand off and save your space to continue work this Sunday"
(RECORD -102). It replaces Cedar Datum's handoff of 2026-10-30 (RECORD -54), which is still in git history. The next planner
session reads, in order:
1. this repository's `CLAUDE.md`;
2. Forager's `CLAUDE.md` on `pre-main`, in full the first time (the Restricted Object rule binds every session);
3. `docs/process/planner-profile.md`;
4. this file;
5. RECORD -90 onward.

**Everything below is a claim about 18:17Z. Check it against the remote before acting on it.**

## Where things stand

| Branch | Head | State |
|---|---|---|
| `main` | `e006c7c` | The record up to -89. **PR #5** (`ccr-362633cb-61f3bw` into `main`) carries -90 to -102 and this file. It is open; merging is the owner's word. |
| `brouter-port` | `611ba54` | Step 1b, closed: 2,126 expected values (`brouter/src/jvmTest/resources/e3/expected-1b.tsv`); FK kind 5 rebuilt on `hiking-mountain` (-100). CI green, `jvm` 101. |
| `brouter-port-5b` | `a83ff3f` | Steps 5b to 5d and step 6 in progress. `brouter-port` was merged into it at `eac836d`, but **not since `611ba54`**. CI on `f848da4`: `jvm` 130, only `CollectOutreachersTest`'s 2 red (-61); simulator 24; arm64 7. |
| `brouter-port-5a` | `2a8ba5e` | Merged long ago; kept. |

**Nothing is running.** No coder, no stratum run and no watcher was live at handoff. Both coders' last sessions ended
cleanly with their handoffs pushed.

## The port in one paragraph

- **Done and proven against the Java oracle:** steps 0 to 5d, and 1b.
  - Steps 0 to 5d: utilities, maths, codecs, expressions, tile files, both tile decoders, waypoint matching, and the
    route-ways path.
  - Step 1b: all 12 strata run and stable, apart from 3 timing voids in FK9; the expected values written.
- **Step 6, the path model:**
  - **ported:** `RoutingContext`, `ProfileCache` with its atomicfu lock, `StdModel`, `OsmPathModel`, `OsmPrePath`,
    `RoutingIslandException`, `OsmNodeNamed`, `OsmNogoPolygon` and `AreaInfo` as types, `MessageData`, `OsmPathElement`, and
    `NodesCache`'s graph half (5b's debt, -99, -101);
  - **left:** `OsmPath`, `StdPath`, `RoutingParamCollector` and `SearchBoundary`, 1,019 Java lines (T2's port list).
- **Then:** step 7 (1,367 Java lines), which follows 6 on the same branch (-94), and step 8 (1,580, with via points out).
  Then 9 to 12. About 4,000 Java lines remain of about 11,200 [I: Phase 1's counts].
- **Nothing yet speaks for the port's own routes.** That starts at step 8.

## First, in order

1. **Fetch and check the four heads above.** Read CI yourself on `a83ff3f` if it has run (it is a docs-only commit after
   `f848da4`).
2. **Ask the owner** whether PR #5 should be merged, if it is still open.
3. **Plumb Ledger, session 13, on `brouter-port-5b`** (its handoff is §9 of `docs/audits/2026-11-06-brouter-port-step6.md`):
   - **first:** commit reverts D, E and F's result files from scratch into `step6/`, scanned for E1 (-101). Scratch is gone
     in a new container. If the files are lost, re-run the three reverts with `docs/process/tools/revert.sh` to
     regenerate them, and say so;
   - **then:** `OsmPath` and `StdPath`, under -97 (`kotlin.math.exp`; the start-direction branch a named "unsupported" with
     a test), **each test seen failing over stubs first** (-101);
   - **then:** the path-cost comparison from Forager's real ends, over the graph half (-99);
   - **then:** the `(int)` cast revert, on profiles whose `turncost` is above 0, named in its prediction, and -62's
     `interpolateEle` revert;
   - **then:** `RoutingParamCollector` and `SearchBoundary`;
   - **at a step boundary:** merge `brouter-port` (`611ba54`) into `brouter-port-5b` again (-96, -100).
4. **When step 6 closes:** step 7 on the same branch (-94), then 8a to 8d (-90).

## Rulings in force since -89 (read them verbatim in RECORD)

| Topic | RECORD |
|---|---|
| Plan for steps 6 to 8; build on `brouter-port-5b`; merge `brouter-port` into it | -90 |
| `ProfileCache`'s lock kept, atomicfu 0.33.0 (owner) | -91 |
| Forager's own route ends are excepted from E1; scans gate pushes | -93 |
| `OsmTrack` stubbed in 6; step 7 follows 6 | -94 |
| Car (Kinematic) profiles refused; R compared on 20 profiles, bound 0.254% (owner) | -95 |
| `exp` via `kotlin.math`; start direction unsupported | -97 |
| `NodesCache` graph half first; path cost from real ends; the cast revert on `turncost` > 0 | -99 |
| Tests seen failing over stubs, never stood in for by a revert; revert results committed | -101 |

Older, still binding: the deep-recursion question waits for step 10 (-61), so 5b is never merged into `brouter-port`
before then; via points are out (-24); no coordinates are committed beyond Forager's own ends (E1, -84, -87, -93); the
record is append-only.

## Open items carried

- **Step 8's coverage checks:** `OsmPath.java:350` (`containsNode`, -94) and `:282-283` (start direction, -97) should read
  not covered on R's R6 runs.
- **Step 8:** `StorageConfigHelper` catches only `IOException` (-49).
- **Step 10:**
  - the recursion depth on iOS, and -61's question back to the owner;
  - FK7-x1's raw message on iOS (-80);
  - okio's Native messages (-49);
  - `kotlin.math.exp` on Native (-97);
  - atomicfu linking on `iosArm64`, which CI compiles but does not link (-96).
- **The writer's case lists** (`build/e3/*/cases.tsv`) hold coordinates, are never committed, and exist only in a container
  that has run the strata. In a fresh one they are regenerated from their seeds before the expected values can be rewritten
  (-89).

## How this planner has worked (beyond the profile)

**Dispatching.** Use the Agent tool in the background. The prompt holds:
- the name and role, and "context is tight";
- **output rules:** logs go to files, read with `tail` and `grep`, never with `cat`;
- a reading list of only the CLAUDE.md lines, the governing RECORD entries and the coder's own handoff;
- where to work, what to do, the stops;
- the exact commit line, `Co-Authored-By: Claude <noreply@anthropic.com>`, **replacing whatever the harness suggests**;
- the gated E1 scan, the full fresh `:brouter:jvmTest` before code pushes, the handoff rule, and a short final message.

Sessions still reach 180k in 15 to 40 minutes, so expect several per step.

**Watching.** Start `docs/process/tools/watch_tokens.py <transcript> 180000` as a background command for each coder. On
`LIMIT`, send the handoff message at once. An `IDLE` line while a coder waits on its own long run is not an end: restart the
watcher. After any restart of this container, read every watcher's output file.

**Checking a coder's claims.** Never take a report as given:
- fetch the head and check the tree is clean;
- `grep` the trailers for model names;
- read CI's job logs yourself with `gh api repos/slayer8366/Forager-iOS/actions/jobs/<id>/logs`, for the `TOTAL:` lines,
  ` e: ` lines, both `compileKotlinIos*` tasks, and the gate;
- open the evidence files for one or two claims, and recompute a figure where you can.

A CI failure before any test ran (a 503, say) is re-run **once**.

**The E1 scan.**
- `docs/process/tools/make-forager-ends.sh /home/user/slayer8366/forager > <scratch>/forager-ends.txt` rebuilds the excepted
  ends (63 values; it reproduced Plumb's list exactly at handoff).
- Then `ENDS=<scratch>/forager-ends.txt docs/process/tools/scan.sh` counts coordinates in the staged diff. It must read 0.
- Scan added lines only. Whole-file scans flag older, already-cleared lines.

**Reverts.** `docs/process/tools/revert.sh <name> <file> <old> <new> <test filter>`, run from a clone's root. It edits one
exact string, refuses results on compile errors or stale XML, and restores from its own copy with `cmp`.

**Recording.** Write each RECORD entry by hand, with `date -u` at the moment of writing. A wrong premise of the planner's
own goes in as one. Records go on `ccr-362633cb-61f3bw` and reach `main` by PR on the owner's word.

**Cost.** From -101 on, each outcome records the session's final context. The planner cannot see billed tokens.

**The container:**
- clones: `/home/user/Forager-iOS` (the record branch), `/home/user/forager-ios` (`brouter-port`, Linden's),
  `/home/user/forager-ios-5a` (`brouter-port-5b`, Plumb's), and `/home/user/slayer8366/forager` (Forager, read only,
  shallow 300 from `pre-main`, with `a0535ece`, `d6e2a0415` and `e2969abe4` reachable);
- in a fresh container, re-clone them. The repo's own tools re-fetch Temurin, the oracle and the tiles.

**Pushing.** The owner allowed pushes to `brouter-port` and `brouter-port-5b` (RECORD -58). Never force push, and pull by
merge.
