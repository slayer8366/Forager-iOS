# BRouter port, step 5d: the route-ways path (RECORD -70)

Plumb Ledger, seventh session, on `brouter-port-5b`, from `d1de74b`. Tags: [R] read from source or output,
[C] computed or run here, [I] inferred, [?] unknown.

## Status
- Item 1 (the defect): fixed. Item 2 (reach): done, 440 unported Java lines, under the stop (session 7).
- Item 3 (session 8): ported and the decode test passing forward; its revert is predicted and pushed, **not yet run**.
  `OsmNode.kt`'s throwing branch is wired to a **stub** `parseNodeBody2`. Items 4 to 6: not started. Handed off at the
  planner's message (04:35Z).

## 1. The `differingFields` defect (-69)
- **Cause** [R]: `WaypointMatchEngines.differingFields` split each end's line on spaces, and `n1=(lon, lat)` prints
  as two tokens, so the bare `lat)` token was "named" by itself (`substringBefore('=')` of a token with no `=` is the
  token).
- **Fix** [R]: the line is split at the keys `line` and `run` write (`ok cross n1 n2 r diff next name type`), and only
  a key from that fixed list is printed (`WaypointMatchEngines.kt`, `differingFields` and `fields`).
- **Seen** [C] (`2026-11-06-brouter-port-step5d/1-defect.txt`): a scratch revert of `WaypointMatcherImpl.kt:39`, never
  committed, `WaypointMatchTest` alone, 0 compile errors, restored from the saved copy each time. Before the fix the
  message held 33 coordinate fragments; after, 0, with the same field lists and the same 2,105.

## 2. Reach of `BRouterRouteWays.read` (done)
- **Inputs** [R]: `03-reach/tools/entries-routeways.txt` (every BRouter call, field and `new` in `BRouterRouteWays.kt:59-123`
  at `a0535ece`, each cited to its line); `exclusions-routeways.txt`, one tier W (the call's own null arguments at `:84`:
  no `DirectWeaver`, no `WaypointMatcher`, no `TagValueValidator`; and three `toString`s no reached site stringifies).
  W is applied alone, because T2's S row cuts `createMicroCache -> MicroCache2`, the edge this path takes.
  The engine source is equal at Forager HEAD, `a0535ece` and `e2969abe4` (empty `git diff`) [C].
- **Result** [C] (`03-reach/out/RW/`, beside T2): unexcluded 175 methods; tier W 141 methods, all 15 cuts used.
- **Harness** [C]: `tools/RouteWaysCalls.java` makes `read`'s calls in order, under JaCoCo 0.8.14 (fetched from Maven
  Central to scratch): 129 methods ran, **0 ran that the static reach missed**, 11 reached but not run on this sample.
- **JDK** [C]: 27 classes, 87 members, every one also in T2's `jdk-use.txt`. No API outside Phase 1's item B table.
- **Not ported** [C] (`out/RW/not-ported.tsv`): 27 methods, **440 Java lines**, under the 600 stop: `MicroCache2`
  (4 methods, 251), `LinkedListContainer` (all 5, 34; no file), `parseNodeBody2` (37), `OsmNode(long)` (4),
  `MicroCache.expandId`/`getIdForIndex` (7), `ByteArrayUnifier.unify` (28, now refuses), `ByteDataReader` 5 methods (26),
  `ByteDataWriter` 8 methods (53). Plus 2 refusing branches to replace: `OsmFile.kt:140-143`, `OsmNode.kt:96-100`.
- The check against the port was by name per Kotlin file, then by hand for refusing bodies [C]; an overload with the
  same name and another signature would read as ported. I checked `addLink`'s two by hand; the rest not [?].
- **A finding:** T2's list holds `parseNodeBody2`, yet the port left it out (`OsmNode.kt:15`). So "on T2" is not
  "ported", and the list above is read from the port, not from T2 [R].

## 3. The port and the `MicroCache2` decode test (session 8, from `d293fd4`)
- **Stubs** [C] (`0604a41`): every row of `not-ported.tsv` given its signature with a stub body; `OsmFile.kt`'s
  `hollowNodes == null` branch wired to the stub `MicroCache2` (see "calls beyond scope"); `OsmNode.parseNodeBody`
  wired to a stub `parseNodeBody2`.
- **Test** [R]: `jvmTest/.../mapaccess/MicroCache2DecodeTest.kt`. Every non-empty micro-cache of the five captured
  tiles (`GraphEngines.forEachCache`), through `createMicroCache(lonIdx, latIdx, buffers, null, null, true, null)` as
  `BRouterRouteWays.kt:84` at `a0535ece` calls it, on both engines; each cache read through its public reader in
  `parseNodeBody2`'s order (ids via `getIdForIndex`/`getAndClear`, restrictions, elevation, node description, each
  link's offsets, direction, way description, geometry bytes), compared line by line. A failure names cache index and
  field only (E1).
- **Over the stubs** [C]: 0 compile errors; 5 tests, 5 failures, each at cache 0, field `[size]` (XML written by that
  run, 04:31:11).
- **Forward** [C] (`7a81a4b`): 0 compile errors; 5 of 5 pass; 25,072 caches compared (f0 47, W70_N40 3,904, W75_N35 683,
  W75_N40 19,671, E170_S50 767). Counts and digests, from the test's own output:
  - f0/W75_N40: 7,191 nodes, 0 restrictions, 16,992 links (8,496 reverse), 9,010 way descriptions; `3939d242...6f0c`
  - W70_N40: 233,165 nodes, 586 restrictions, 547,878 links; `93c1d12c...86c2`
  - W75_N35: 260,090 nodes, 3,294 restrictions, 672,467 links; `817c0e6a...8e`
  - W75_N40: 8,137,833 nodes, 47,890 restrictions, 20,581,766 links; `33904ce4...8023`
  - E170_S50: 50,088 nodes, 181 restrictions, 127,389 links; `0da306c1...db8c7`
- **Ported** [C]: `MicroCache2.kt` (constructor, `readUnified`, `expandId`, `shrinkId`; 293 lines), `LinkedListContainer.kt`
  (new, 48), `ByteDataReader.kt` (+5 methods), `ByteDataWriter.kt` (+8 methods), `MicroCache.kt` (`getIdForIndex`,
  `expandId`). Overloads checked by signature against `reached-methods.tsv`: `write([BII)V` only (not `write([B)V`),
  `readFully([B)V`, `writeShort(I)V`; none left out [R].
- **Not yet ported** [R]: `ByteArrayUnifier.unify` (still refuses), `OsmNode(long)` (stub: sets nothing),
  `parseNodeBody2` (stub: empty). These are item 4's; no test reaches them yet.
- **Not seen by the sample** [?]: the `featureId == 13` empty-node escape (delta files only) and whether `injectSize`'s
  shift branch (a link over 127 bytes) ran; both plausible on W75_N40, not instrumented.
- **Revert** [C]: prediction pushed in `ae97c7b` (`2026-11-06-brouter-port-step5d/3-decode-revert-prediction.md`).
  **Not run.**

## Calls beyond scope
- The stub step wired `OsmFile.kt`'s `hollowNodes == null` branch to the stub `MicroCache2` before the test passed, so
  the test over the stubs reached the stubs rather than the old "unsupported" throw (which would have been a failure
  no stub could change). The throw was gone from `0604a41` on; nothing outside tests calls that branch (Forager's
  `NodesCache` path weaves directly). The same for `OsmNode.parseNodeBody`, which now calls the stub `parseNodeBody2`:
  **at this head it silently decodes nothing** instead of throwing, until item 4 ports it.
- The dispatch says "through `tools/fetch-pinned.sh`": that script already exists here as the generic fetcher. Item 4
  should add `tools/fetch-dropoff.sh` calling it, as `fetch-f0h.sh` does on `brouter-port`.

## Wrong premises in -70
- None found in -70 itself. -67's ~300-line estimate was low: 440 [C].

## Unverified
- Items 3 to 6 are not started. CI on `e26e13d` and on this head is not read [?].
- The forward `WaypointMatchTest` with the fix was not run on its own; the two scratch reverts ran it with the
  revert in place [C]. CI's `jvm` run will run it forward.

## Handoff (eighth session's, at the planner's message, 04:35Z)
Clone `/home/user/forager-ios-5a`, branch `brouter-port-5b`; check `origin/brouter-port-5b` is the commit that added
this section. Nothing is running; the tree is clean. Scratch as session 7's (below), plus `run/` (the stub and forward
logs of the decode test). `one-revert.sh` is the one-class runner.
1. **Run the decode revert** (item 3), exactly as predicted in `3-decode-revert-prediction.md`:
   `S=/tmp/claude-0/-home-user-Forager-iOS/3a3820fc-8d2c-5da2-aa77-f5956bee2927/scratchpad/plumb/5d;
   bash $S/one-revert.sh $S/rev3 brouter/src/commonMain/kotlin/com/zynergylabs/forager/brouter/codec/MicroCache2.kt
   'writeVarLengthSigned(alat[nodeIdx] - alat[n])' 'writeVarLengthSigned(alat[n] - alat[nodeIdx])' '*MicroCache2DecodeTest*'`.
   Read `$S/rev3/result.txt` (0 compile errors first) and `restore.txt` (identical, forward line 1), then `git diff`
   empty. Commit counts and fields only to `3-decode-revert.txt`.
2. **Item 4:** port `OsmNode(long)` (`OsmNode.java:57-60`), `parseNodeBody2` (`:117-152`) and `ByteArrayUnifier.unify`
   (`ByteArrayUnifier.java:26-53`) over their stubs, but first write the route-ways test and see it fail over them:
   add `tools/fetch-dropoff.sh` (pattern of `git show origin/brouter-port:tools/fetch-f0h.sh`), pinning from
   `https://raw.githubusercontent.com/slayer8366/Forager/a0535ece18dd27b4f86ebc311f201089868b33ed/app/src/test/resources/routing/dropoff/`
   `segments/W75_N40.rd5` (4,475 bytes) and `points.json` (1,103 bytes) by SHA-256 (compute from
   `git -C /home/user/slayer8366/forager show a0535ece:<path> | sha256sum`), wired into `brouter/build.gradle.kts` as
   `fetchTiles` is. Then the test: for each point, `BRouterRouteWays.kt:59-123`'s calls on both engines (cells from
   `GeoDistance.metersPerDegree`, radius 6,371,008.8 m earth, 150 m as `BRouterRouteWaysTest.kt:27`; way context
   `BExpressionContextWay(meta)` with `lookups.dat`, equal in SHA-256 to the oracle's `profiles2/lookups.dat`,
   `bf8fc3b2...f419`), comparing per link the tag map of `getKeyValueDescription(reverse, bitmap)`, the geometry in
   order and `isReverse`. Points: the 15 of `points.json`, and F0h's 16 ends (`E3F0hTest.kt` `ENDS` on
   `origin/brouter-port`, from `routes.json` at Forager `d6e2a0415`) on F0's fixture `TestTiles.f0` (F0h's own tile is
   not wired on this branch: a call to report). Revert: drop one tag, predicted and pushed first.
3. Items 5 and 6 as -70 sets them.

## Handoff (seventh session's, done), at the planner's message, 04:19Z)
Clone `/home/user/forager-ios-5a`, branch `brouter-port-5b`. Check `origin/brouter-port-5b` is the commit that added
this section. Nothing is running; the tree is clean. Scratch:
`/tmp/claude-0/-home-user-Forager-iOS/3a3820fc-8d2c-5da2-aa77-f5956bee2927/scratchpad/plumb/5d/` (`reach/` holds the
compiled engine, `javap.txt`, `spans.tsv`, the harness, `data/` with the dropoff and Phase 1 tiles and `lookups.dat`;
`jacoco/` the two jars; `one-revert.sh` a one-class revert runner under the runner rules; `defect/` the raw messages,
never to be committed).
1. Read CI on the head: `gh run list -R slayer8366/Forager-iOS -b brouter-port-5b`; the `jvm` count (expect only
   `CollectOutreachersTest`'s 2 red), ` e: ` lines, both `compileKotlinIos*`, simulator (simulator-checked), arm64 gate.
2. Item 3: port `out/RW/not-ported.tsv` into `commonMain`, stubs first (`LinkedListContainer.kt`, `MicroCache2.kt`,
   the `ByteDataReader`/`Writer` methods, `OsmNode(long)`, `parseNodeBody2`, `getIdForIndex`, `ByteArrayUnifier.unify`).
   Then a `MicroCache2DecodeTest` beside `GraphDecodeTest`: every micro-cache of each pinned tile through
   `createMicroCache(..., hollowNodes = null)` on both engines, one digest per cache. Seen failing over the stubs,
   then forward; push a revert prediction (one decoded field wrong), then run it with
   `docs/audits/2026-10-30-brouter-port-step5b/revert-runner.sh`. Replace `OsmFile.kt:140-143` only once it passes.
3. Item 4: `tools/fetch-pinned.sh` after `git show origin/brouter-port:tools/fetch-f0h.sh`, pinning the dropoff
   `segments/W75_N40.rd5` (4,475 bytes) and `points.json` (1,103 bytes) at `a0535ece` by SHA-256; then the route-ways
   test (tags, geometry in order, `isReverse`), radius 150 m as `BRouterRouteWaysTest.kt:27`, plus F0h's ends.
4. Items 5 and 6 as -70 sets them.
