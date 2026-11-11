# BRouter port, step 7: the track and hints (verify first)

Plumb Ledger, session 18, dispatched by Cedar Datum under RECORD -109. Branch `brouter-port-5b`, from `7125739`.
This session reads and reports. It builds nothing: no `commonMain` or test file is changed, and `router/OsmTrack.kt`
stays the type with its one stub (-94).

Tags: **[R]** read in the source at the file and line named; **[C]** counted or shown by a tool's output, named;
**[I]** inferred, not shown. Java is BRouter 1.7.10 as vendored in Forager (`third_party/brouter/src/main/java/btools/`,
tag `v1.7.10`, per its README), the same source Phase 1's T2 measured (the file line counts below equal T2's `file_lines`
column for all nine files [C]). Forager is `slayer8366/Forager` `pre-main` at `947f1be2`.

## 1. Step 7's files

Phase 1's item I, step 7 (report at `20e1f92`, line 1702): "`OsmTrack`, `VoiceHint`, `VoiceHintList`,
`VoiceHintProcessor` ... `Formatter` and `Format*` only as far as A found methods reached", 1,367 lines in 9 files.
T2's port list (`2026-10-28-brouter-port-verify/03-reach/out/T2/port-list.tsv`) gives the same nine and the same sum [C]:

| Java class (`btools/router/`) | Java lines [C, `wc -l`] | T2 kept lines | T2 kept methods |
|---|---|---|---|
| `OsmTrack` | 587 | 444 | 16 of 27: `addDetours`, `addNode`, `appendTrack`, `buildMap`, `copyDetours`, `getFromDetourMap`, `getLink`, `getMatchedWaypoint`, `getMinDistance`, `getTotalSeconds`, `prepareSpeedProfile`, `processVoiceHints`, `readBinary`, `registerDetourForId`, `removeVoiceHint`, `startSection` (plus `OsmPathElementHolder`) |
| `VoiceHint` | 223 | 201 | 5 of 9: `addBadWay`, `calcCommand`, `hasGiveWay`, `is180DegAngle`, `isRoundabout` |
| `VoiceHintList` | 61 | 33 | 2 of 5: `setTransportMode(boolean,boolean)`, `transportMode` |
| `VoiceHintProcessor` | 529 | 529 | 6 of 6: the constructor, `process`, `postProcess`, `checkForNextNoneMotorway`, `checkStraightHold`, `sumNonConsumedWithinCatchingRange` |
| `Formatter` | 569 | 84 | 3 of 22: `getFormattedTime2`, `getFormattedEnergy`, `format1` |
| `FormatGpx` | 573 | 26 | 0 of 10 |
| `FormatJson` | 261 | 22 | 0 of 7 |
| `FormatKml` | 116 | 15 | 0 of 5 |
| `FormatCsv` | 43 | 13 | 0 of 4 |
| **9 files** | **2,962** | **1,367** | **32** |

- **The four `Format*` rows keep no method.** Their 76 lines are fields, imports and braces, which `reach.py` keeps for
  any class referenced as a type by reached code (`reach.py:579-616`) [R]. The references are `doRouting`'s `new
  FormatGpx(...)` and the like (`RoutingEngine.java:249`, `:270-277`, `:299-302`, `:312`), on branches T2 itself cuts
  (`summary.txt`, "UNUSED exclusion edge" for `FormatGpx.format`, `FormatJson.format`, `FormatKml.format`, `FormatGpx.read`,
  `Formatter.write`) [C].
- `OsmTrack.containsNode` is not in T2's list [C]: T2's tier F cuts it (alternative 0). Step 6 holds it as the stub -94
  ruled, and `OsmPath.kt` calls it.
- No other class belongs to step 7. T2's other unported `btools.router` files are `RoutingEngine` (step 8),
  `RoutingHelper`, `AreaReader`, `SuspectInfo` and the five `Kinematic*` (0 kept lines each) [C].

## 2. Reachability on Forager's path, traced to callers

**Forager's call** [R, `app/src/main/java/com/zynergylabs/forager/app/routing/BRouterTrailRouter.kt`]: two waypoints
from `getWayPointList("lon,lat|lon,lat")` (`:145`); params `profile` (`hiking-mountain.brf`), `alternativeidx=0`,
`profile:use_dynamic_range=0` (`:147-155`); `RoutingEngine(null, null, routeData, waypoints, context, ROUTING)` (`:210`),
so `outfileBase` and `logfileBase` are null; `quite = true` (`:157`); `doRun` (`:167`). **What Forager reads back:**
`getErrorMessage()` (`:133`), and from `getFoundTrack()` only `nodes` and `distance` (`:107-114`, `:95`), and of each node
`getILat`, `getILon` and **`getSElev`** (`:216-220`). The oracle harness makes the same call
(`jvmTest/.../oracle/OracleCalls.kt:22-40`) [R].

Three facts fix most of step 7's reach:

- **`turnInstructionMode` is 0.** `RoutingContext.java:176-179` assigns the profile's value only when it is not 1;
  `hiking-mountain.brf:57` says 1, so the field keeps its default 0 (`:252`) [R]. Forager passes none of the request keys
  that set it (`RoutingParamCollector.java:223-231`) [R]. All 26 profiles in the oracle's zip say 1 or nothing
  (`grep` of `brouter/build/oracle/profiles2/*.brf`: 24 assign 1, `rail` and `river` have no line) [C], so this holds on
  every profile, not only Forager's.
- **`hasDirectRouting` is false.** It is set at `RoutingEngine.java:985` from `tryFindTrack`'s local, which turns true only
  for a `DIRECT` waypoint (a `d` suffix, `RoutingParamCollector.java:37-38`, or a beeline added by
  `matchWaypointsToNodes` under `useDynamicDistance`, `RoutingEngine.java:1633-1667`), extra waypoints, or more sections
  than `lastTracks` (the assignments at `:887`, `:903`, `:907`, `:939`, `:977`) [R]. Forager has none: no suffix, `use_dynamic_range=0`, one
  section [I for `use_dynamic_range` reaching `useDynamicDistance`, `RoutingContext.java:200`; step 6's
  `RoutingParamCollectorTest` compares that context with the oracle's].
- **No detour is ever registered.** `registerDetourForId` has two callers: `RoutingEngine.java:2194`, inside
  `if (routingContext.turnInstructionMode > 0)` (`:2191`), and `:1164` in `snapRoundaboutConnection`, called only from
  `snapPathConnection` (`:1356`), which returns false unless the start waypoint's name begins `via` or `rt` (`:1266-1267`);
  Forager's are `from` and `to` (`RoutingParamCollector.java:48-51`) [R]. So every `detourMap` stays null.

Hence `processVoiceHints` returns at `OsmTrack.java:457-460`, after making an empty `VoiceHintList`. Phase 1 said the same
(report line 178), and its JaCoCo run of Forager's calls agrees class by class (`03-reach/out/jacoco.csv`):
`VoiceHint`, `VoiceHintProcessor` and all four `Format*` covered 0 instructions; `OsmTrack` 10 methods covered,
`VoiceHintList` 2, `Formatter` 3 [C]. That sample is Forager's own cases, not E's strata.

### Per method

Callers are `RoutingEngine` (step 8) unless named. "Reached" means on Forager's call; "message only" means the result goes
into `track.message`, which neither Forager nor the port's `RouteOutcome` reads.

| Method | Caller(s) on Forager's path | Mark |
|---|---|---|
| `OsmTrack()`, fields, `OsmPathElementHolder` | `tryFindTrack:880`, `compileTrack:2280` | **reached** |
| `buildMap` | `compileTrack:2312` | **reached** |
| `getLink` | `_findTrack:1942`, `:1950`, `:2097` on `costCuttingTrack`, which is pass 0's track during pass 1 (`searchRoutedTrack:1749`; `pass1coefficient` 1.5, `pass2coefficient` 0, `RoutingContext.java:157-158`) | **reached** [I that `hiking-mountain` sets neither coefficient negative; it does not name them, `grep`] |
| `appendTrack` | `tryFindTrack:1032`, once, with `ourSize == 0` | **reached**, but only its one-section branch: `:344-345` (height fill), `:337` and `addDetours` at `:376` are not |
| `copyDetours` | `compileTrack:2316` (re-tracking, `guideTrack` set), source's map null | **reached** (null case only) |
| `readBinary` | `tryFindTrack:914` with `rawTrackPath`, which nothing assigns | **reached, guard only**: returns null at `:235`; the file branch `:236-294` is not |
| `prepareSpeedProfile` | `tryFindTrack:1043` | **reached** (empty body) |
| `processVoiceHints` | `tryFindTrack:1042` | **reached, `:453-460` only** |
| `getTotalSeconds` | `doRouting:238` when `track.energy != 0`; `getTime:2409` (Forager does not call it) | **reached, message only** [C: JaCoCo shows `Formatter` covered, which only `:238` calls] |
| `addNode` | `mergeTrack:2328` (timeout or "sloppy merge", `searchRoutedTrack:1727`, `:1754`, `:1765`); `doGetInfo:383-384`; `getExtraSegment:1083-1084` | **unknown**: only on a timeout with a partial match, whose track is not returned (FK 9 to 11 may reach it; JaCoCo's 1 ms case did not) |
| `containsNode` | `OsmPath.java:350` with `refTrack`; `tryFindTrack:1006` under `continueStraight` and a via | **not reached**: needs `alternativeidx > 0` (`tryFindTrack:988-991`) or a via point (-24) |
| `addDetours` | `appendTrack:376` | **not reached**: needs a non-null `detourMap` (a detour, above) |
| `registerDetourForId` | `:2194`, `:1164` | **not reached**: needs `turnInstructionMode > 0`, or a via |
| `getFromDetourMap` | `snapRoundaboutConnection:1149-1214`, `snapPathConnection:1379` | **not reached**: needs a via |
| `removeVoiceHint` | `snapPathConnection:1445` | **not reached**: needs a via |
| `getMatchedWaypoint`, `getMinDistance`, `startSection` | `processVoiceHints:497`, `:528`, `:510-518` | **not reached**: after the return at `:457-460` |
| `VoiceHintList()`, `setTransportMode(boolean,boolean)` | `processVoiceHints:453-454` | **reached** (state never read) |
| `VoiceHintList.transportMode` | `processVoiceHints:526` | **not reached** |
| `VoiceHint`, all | `processVoiceHints:476-516` and `VoiceHintProcessor` | **not reached** |
| `VoiceHintProcessor`, all | `processVoiceHints:526-532` | **not reached** |
| `Formatter.getFormattedEnergy`, `format1`, `getFormattedTime2` | `doRouting:238` | **reached, message only** |
| `FormatGpx`, `FormatJson`, `FormatKml`, `FormatCsv` | `doRouting:247-312` behind `outfileBase != null`, `!quite`, the `reportFormat` system property or `logfileBase != null` | **not reached** |

**Which `Format*` output does Forager take? None.** With `outfileBase` null and `quite` set, `doRouting` takes the `else`
at `:295`, and with no `reportFormat` property (Forager sets none, `git grep`) [C] it prints nothing (`:297-303`);
`logfileBase` is null (`:310`) [R]. Forager reads the `OsmTrack` object directly.

**Step 7 on Forager's path, in lines:** of T2's 1,367, `VoiceHintProcessor` (529), `VoiceHint` (201) and the `Format*`
bodies (76) are not reached; nor are about 140 of `OsmTrack`'s kept lines (the hint body of `processVoiceHints`, the
detour methods, `readBinary`'s file branch, `removeVoiceHint`, `startSection`, `getMinDistance`, `getMatchedWaypoint`)
[I: spans read from `reached-methods.tsv`, summed by hand]. `Formatter`'s 84 are reached but message only. **What is
reached and read is `OsmTrack`'s node list, its `distance`, and the heights on its nodes.**

## 3. Test plan (under -101: each test seen failing over the stubs first)

All tests are JVM differential tests against the oracle in the same process, built on step 6's `PathEngines`
(`jvmTest/.../router/PathEngines.kt`), which already makes paths on decoded links in both engines by reflection over
Java's field names. Each test is written and run red over stubs (`NotImplementedError`) before the port it checks.

**E1 discipline, for every test below:**
- Tracks are built in memory from the pinned tiles; no position, node id (`getIdFromPos` packs the position) or
  `OsmPathElement.toString` (`"${ilon}_$ilat"`) is written to a committed file, a test name or an assertion message.
- Comparisons are by node index, count, boolean, field bits and a 64-bit digest. A failure message names the case number,
  the field and the index, never the value of a position.
- Committed evidence is counts and digests only, scanned with `scan.sh` before every commit.
- Start points are Forager's own ends (F0's 2, F0h's 8), under E1's standing exception (-93); no other end is written.
- No voice hint is produced on Forager's path (§2), so none is recorded or asserted. If the planner rules the hint code in
  (stop 2), its tests use synthetic `VoiceHint` and `MessageData` inputs with zero positions, never hints off a route.

**Can be compared with the oracle now, on its own (session 19):**

| Test | What it drives | Asserts (oracle against port) | Red over stubs because |
|---|---|---|---|
| `TrackMapTest` | a track of `OsmPathElement`s made from step 6's path chains (seeded walks over decoded links from Forager's ends, some out and back so nodes repeat and `nextHolder` chains form), `addNode` then `buildMap` | `getLink(a, b)` for each consecutive pair, each reversed pair and seeded non-pairs: the index of the element returned, or null; `containsNode` for every visited node | `buildMap`, `getLink`, `containsNode` throw |
| `AppendTrackTest` | one segment into an empty track (Forager's case, `ourSize == 0`); two segments, with and without the first's last height `Short.MIN_VALUE` | node count; per node the bits of time and energy, cost, height, and whether `message.lon/lat` equal the node's; `distance`, `ascend`, `plainAscend`, `cost`, `energy`; `voiceHints` and `detourMap` null or not | `appendTrack` throws |
| `TrackHeadTest` | `processVoiceHints` on a built track under each of step 6's `StdPath` profiles (`PathEngines.stdProfiles`), Forager's params; `prepareSpeedProfile`; `readBinary(null, ...)`; `copyDetours` from a track with no detours | `voiceHints` non-null and empty; its transport mode and `turnInstructionMode` (by reflection); `readBinary` null; `detourMap` null | each throws |
| `TrackTimeTest` | `getTotalSeconds` on built tracks and on synthetic time spans (zero, one node, exact halves, negative spans); `Formatter`'s three statics on seeded ints and doubles (`format1` is private: reflection) | equal ints and strings | each throws |

These touch no coordinate in an assertion and no hint. `TrackTimeTest` asserts time and energy formatting on synthetic
inputs, where Phase 1 records time and energy at route level rather than asserting them (report line 755); see stop 5.

**Must wait for step 8's routes:** the track as `compileTrack` makes it (the end-node cut, `MessageData` on every
element, `copyDetours` from the guide track); `postElevationCheck` and `recalcTrack`'s `ascend`, `plainAscend` and
`distance`; `track.message`, `name` and `messageList`; `foundRawTrack`'s fields; `mergeTrack`'s `addNode` on a timeout
(8c, FK 9 to 11); and the node count, distance and heights digest per case on F0, F0h and R (E3's stored form, -58's
heights digest). Step 8's coverage reader also shows §2's "not reached" marks hold on R (as -94 asks for `:350`).

## 4. The revert: -90's premise does not hold

-90 (from item I, line 1702): "one distance rounding replaced by `roundToLong`". **No distance is rounded in step 7's
nine files** [C: `grep -n "round\|0\.5\|(int)\|(long)\|Math\."` over them]. The roundings there are:
- `OsmTrack.java:427`, `return (int) (s + 0.5);` in `getTotalSeconds`: a time, message only on Forager's path;
- `Formatter.java:94` (time) and `:113` (energy): message only;
- `VoiceHint.java:197`, `:209`: hint text, not reached.

`OsmTrack.distance` is a sum of ints (`appendTrack:380`; `compileTrack:2306` in step 8). The one distance rounding on the
track's path is `OsmPathElement.java:78`, `Math.round(CheapRuler.distance(...))`, which is **step 6's**, ported as
`javaRound` at `router/OsmPathElement.kt:54`. Replacing that `javaRound` with `roundToLong` could not be caught by any
test: Kotlin/JVM's `roundToLong` is `Math.round` [I: Kotlin's stdlib, not read here], so the JVM sees no change; on Native
the two differ only just below one half and at 2^52 and above (`JavaLang.kt:6-8`), and `max(1.0, ...)` masks the first
while no link reaches the second [I]. A revert there is a check that cannot fail (Forager's CLAUDE.md, Known pitfalls).

**Candidates on reached code, for the planner to choose:**
- **(a) `OsmTrack.java:346`, `if (i > 0 || ourSize == 0)` made `if (i > 0)`:** the first node is dropped in Forager's
  one-section case. `AppendTrackTest` fails on node count, and step 8's F0 node count would too [I, predicted].
- **(b) `OsmTrack.java:323`, `getLink`'s `e1.getIdFromPos() == n1` made `== n2`:** `TrackMapTest` fails on the first pair.
- **(c) -90's words applied to the nearest rounding, `OsmTrack.java:427` made `roundToInt()`:** they agree for every
  span of zero or more, so only `TrackTimeTest`'s negative spans catch it, and negative spans do not occur on Forager's
  path [I]. Not recommended.

## 5. Sessions

Each about 120k of working context, Plumb on `brouter-port-5b`, tests red over stubs before the port.

- **Session 19, `OsmTrack`'s reached core** (about 300 Java lines): the type with all its fields and
  `OsmPathElementHolder`; `addNode`, `buildMap`, `containsNode` (replacing -94's stub), `getLink`, `appendTrack`,
  `copyDetours`, `readBinary`'s null guard, `prepareSpeedProfile`, `processVoiceHints`' head with `VoiceHintList`'s
  constructor, fields and `setTransportMode`, `getTotalSeconds`; `Formatter`'s three statics if kept (stop 3). Tests: the
  four in §3, each committed red first. Revert: the planner's choice from §4, with its result file committed under
  `step7/`, scanned.
- **Session 20, only if the planner or owner rules the unreached code in (stop 2):** `VoiceHint` and `VoiceHintProcessor`
  (730 lines) and `OsmTrack`'s hint body, detour methods, `removeVoiceHint`, `startSection`, `getMinDistance`,
  `getMatchedWaypoint`, `readBinary`'s file branch. Tests on synthetic hint inputs and synthetic detour maps, compared with
  the oracle; a revert such as `VoiceHintProcessor`'s catching-range comparison flipped. `VoiceHintProcessor.process` (270
  lines) and `postProcess` (165) may need a session each, so this is one or two sessions. If ruled out, these become named
  "unsupported" throws with a test that shows each (-74), inside session 19.
- **Then step 8a,** which compares the track through the routes (§3, "must wait").

## Unverified

- That `profile:use_dynamic_range=0` reaches `useDynamicDistance` as false: inferred from step 6's context comparison,
  not traced here line by line.
- That no stratum of E (DL, MP64, ML5, NT, CR, LB, PM5, FK) passes a parameter that sets `turnInstructionMode` or a
  `DIRECT` waypoint: `OracleCalls.kt` and `ExpressionsEngines.kt` pass Forager's three keys [R]; the strata's own
  generators were not read.
- `addNode`'s reach on a timeout (FK 9 to 11): not traced to a case.
- That `track.energy` is non-zero on every Forager route: shown only for JaCoCo's sample (`Formatter` covered).
- Kotlin/JVM's `roundToLong` being `Math.round`: from the stdlib's documented behaviour, not read in source.
- The ~140 unreached `OsmTrack` lines: summed by hand from method spans, not by a tool.
- Nothing was built or run: no test, no CI read. The branch's last CI is -108's on `7491f3a`.

## Stops for the planner or owner

1. **-90's revert has no target in step 7** (§4). Its one distance rounding is step 6's, and a revert there cannot fail
   on either target. A replacement revert is the planner's call; (a) is offered.
2. **Scope (for the owner, via the planner): `VoiceHint`, `VoiceHintProcessor` and `OsmTrack`'s hint and detour code are
   not reached on Forager's path, nor on any profile in the oracle's zip without request keys** (§2; JaCoCo agrees). T2
   keeps them because it keeps code the profile could reach (T3, which ties the port to the profile, cuts them; Phase 1
   report lines 162-208). Options: port them under T2 as -90 says, tested on synthetic inputs (session 20); or make them
   named "unsupported" throws (-74). About 870 Java lines turn on this.
3. **Scope: no `Format*` method is reached** (§1, §2). -90's "`Format*` as far as reached" is therefore zero; T2's 76
   kept lines are declarations only. `Formatter`'s three reached statics feed only `track.message`, which the port's
   `RouteOutcome` does not carry. Whether `Formatter`, `Format*` and `track.message` are ported at all is a scope call.
4. **A premise of Phase 1 found false since it was written: Forager now reads heights.** Phase 1 (report lines 755 and
   2692) lists elevations among the values "Forager reads none of"; Forager's `trailRouteOf` reads `getSElev()` on every
   node since `54c33b99` (2026-10-30, "Trail heights"), the day after Phase 1 (`20e1f92`, 2026-10-29) [R, `git log -S`].
   The port's `RouteOutcome.Found` (`brouter/.../Route.kt:40`) carries `iLat`, `iLon` and `distance`, no heights. This is
   step 8d's surface and E's assertions, not step 7's code, but step 7's `appendTrack` sets heights (`:344-345`). Whether
   heights join the outcome and become asserted rather than recorded is for the planner, and the outcome's surface for
   the owner.
5. **-90's "the hints are recorded, not asserted (E1)" would record nothing:** the hint list is empty on every Forager
   route (§2), so a recorded hint per case is a check that cannot fail. Separately, `TrackTimeTest` would assert time and
   energy formatting on synthetic inputs, which no E1 rule forbids but Phase 1 records rather than asserts at route
   level; the planner may prefer it recorded.
6. **None of step 7 needs an assertion E1 forbids,** if the plan in §3 is kept. Nothing here is stopped on E1.

## 6. Session 19: `OsmTrack`'s reached core

Plumb Ledger, session 19, `brouter-port-5b` `916cb8f` to the head that carries this section. Trailer on every commit
`Co-Authored-By: Claude <noreply@anthropic.com>`; E1 scan 0 on every staged diff [R].

**Done** (§5's session 19, with -111's rulings):
- **Stubs and tests, red first** (`bf48f79`): `OsmTrack`'s fields and the reached methods as stubs, `VoiceHintList`'s
  constructor, fields and `setTransportMode`, `VoiceHint`'s type with `indexInTrack` only (`appendTrack` shifts it),
  `Formatter`'s three statics. Tests: `TrackMapTest`, `AppendTrackTest`, `TrackHeadTest`, `TrackTimeTest` (3 tests), on
  tracks built by `TrackEngines` (jvmTest): seeded walks from Forager's own ends that take the same link index in both
  engines, back links included, each step a detail-mode `createPath`, the last element's `origin` chain added with
  `addNode` as `compileTrack` does. Compared by identity index, bits, booleans and digests; no position, no hint.
- **The port** (`cbd31d6`): `addNode`, `copyDetours`, `buildMap`, `containsNode` (replacing -94's stub), `getLink`,
  `appendTrack`, `getTotalSeconds`, `prepareSpeedProfile`, `readBinary`'s null guard, `processVoiceHints`' head
  (`OsmTrack.java:452-460`), `setTransportMode`, `getFormattedTime2`, `getFormattedEnergy`, `format1`. Left as
  `TODO("step 7: hint code, a later session (RECORD -111)")`: the hint body after the early return, `addDetours`,
  `readBinary`'s file branch. `Formatter`'s other members and the rest of `VoiceHintList` and `VoiceHint` are not stubbed
  yet; they come with their sessions.

**Runs** [R, read from fresh XML]:
- Over the stubs: 6 tests, 6 failed, 0 ` e: `; every failure a `NotImplementedError` naming a stub (`OsmTrack.addNode`
  for the four track tests, `Formatter.getFormattedTime2`, `OsmTrack.getTotalSeconds`). Run twice: after the first, a
  literal in `TrackTimeTest`'s double list (0.0499 followed by one more 9) was cut to `0.0499` because the E1 scan's decimal pattern caught it;
  it was re-run red the same way before the commit. Not a comparison change: no port existed yet.
- On the port: 6 tests, 0 failed, 0 ` e: `. `TrackMapTest` 60 tracks, 51,230 nodes, 31,309 repeated nodes (so
  `nextHolder` chains are walked), 95,020 links found. `AppendTrackTest` 40 one-section, 20 two-section, 20 with a missing
  height, 79,562 nodes. `TrackHeadTest` 20 profiles, 100 runs, the three transport modes reached. `TrackTimeTest` 40 built
  tracks, 513 synthetic spans, 3,014 ints and 3,010 doubles through `Formatter`.
- Full fresh `:brouter:jvmTest --rerun-tasks` before the code push: 144 tests, 2 failures, both `CollectOutreachersTest`
  (-106), 0 ` e: `; no XML older than the run.

**Revert (a)** (`OsmTrack.java:346` in the port, `if (i > 0 || ourSize == 0)` made `if (i > 0)`; the planner's choice,
-110). Prediction pushed first (`c898514`, `step7/01-revert-predictions.txt`), then `revert.sh` (a scratch copy, so its
working files stay out of `main`'s checkout). Observed as predicted (`step7/02-revert-a-first-node-dropped-result.txt`,
`b9602d8`): 0 compile errors, 4 fresh XML files, 6 tests, 2 failures. `AppendTrackTest` at mode 0, the first case, walk 0,
"node count expected:<564> but was:<563>". `TrackTimeTest`'s built tracks at walk 0 on seconds and time (15,747 against
15,739), energy equal. The other four passed. Restored by `cmp` from the saved copy; the forward line is present and the
tree clean.

**CI on `cbd31d6`** (run 43005311784, read by Plumb): `jvm` 144 tests, 2 failures, both `CollectOutreachersTest`;
0 ` e: `; `compileKotlinIosArm64` and `compileKotlinIosSimulatorArm64` both ran. `ios-smoke` passed: simulator 24 of 24
(**simulator-checked**), arm64 JVM 7 of 7, and the gate step reports every class ran.

**Unverified:**
- [I] Whether a two-section track with hints, or with a detour map on both sides, behaves as Java's: not reached by these
  tests (`addDetours` is a TODO; no hint is made). That is the hint session's.
- [I] `getTotalSeconds` and `format1` on Native: tested on the JVM only. `(s + 0.5).toInt()` and `Double.toLong()` saturate
  as Java's casts do on both platforms by Kotlin's spec [I, not read here]; no Native test covers them yet.
- [?] `TrackHeadTest` asserts `copyDetours` from a track with no detours gives null, which a stub returning null would also
  pass; it bit only because the stub threw. A detour map to copy needs `registerDetourForId`, the hint session's.

**Next, for session 20** (the hint code, -111):
1. `VoiceHint` whole and `VoiceHintList`'s rest, stubs first, tests on synthetic hint inputs with zero positions.
2. `OsmTrack`'s detour methods (`registerDetourForId`, `addDetours`, `appendDetours`, `getFromDetourMap`) and the hint
   body of `processVoiceHints`, with `getMinDistance`, `startSection`, `getMatchedWaypoint`, `removeVoiceHint`; then
   strengthen `TrackHeadTest`'s `copyDetours` with a real detour map.
3. `VoiceHintProcessor` (`process`, `postProcess`), likely its own session; a revert on its catching-range comparison.
4. Then `readBinary`'s file branch and the formats (`Formatter` whole, `FormatGpx`, `FormatJson`, `FormatKml`,
   `FormatCsv`), compared by digest.
