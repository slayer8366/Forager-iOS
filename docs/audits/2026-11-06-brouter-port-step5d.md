# BRouter port, step 5d: the route-ways path (RECORD -70)

Plumb Ledger, seventh session, on `brouter-port-5b`, from `d1de74b`. Tags: [R] read from source or output,
[C] computed or run here, [I] inferred, [?] unknown.

## Status
- Item 1 (the defect): fixed. Item 2 (reach): done, 440 unported Java lines, under the stop. Items 3 to 6: not started; handed off at the planner's message.

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

## Wrong premises in -70
- None found in -70 itself. -67's ~300-line estimate was low: 440 [C].

## Unverified
- Items 3 to 6 are not started. CI on `e26e13d` and on this head is not read [?].
- The forward `WaypointMatchTest` with the fix was not run on its own; the two scratch reverts ran it with the
  revert in place [C]. CI's `jvm` run will run it forward.

## Handoff (seventh session's, at the planner's message, 04:19Z)
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
