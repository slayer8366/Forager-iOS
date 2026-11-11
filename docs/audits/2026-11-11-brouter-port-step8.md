# BRouter port, step 8: `RoutingEngine` and the entry point

Plumb Ledger, session 29, dispatched by Cedar Datum under RECORD -90 (step 8a). Branch `brouter-port-5b`, from `461e4d6`
(step 7 closed, -121).

Tags: **[R]** read in the source at the file and line named; **[C]** counted or shown by a tool's output, named;
**[I]** inferred, not shown. Java is BRouter 1.7.10 as vendored in Forager (`third_party/brouter/src/main/java/btools/`),
`router/RoutingEngine.java`, 2,443 lines [C: `wc -l`]. Forager is `slayer8366/Forager` `pre-main` at `947f1be2`; its caller
is `app/src/main/java/com/zynergylabs/forager/app/routing/BRouterTrailRouter.kt` (`BRT` below).

## 1. The survey: every method and field group, and its sub-step

Method ranges are from one `grep -n` of the signatures and the closing braces at two spaces' indent [C].

**What Forager asks of the engine** [R: `BRT:143-159`, `:166-174`, `:208-209`]: two waypoints from
`getWayPointList("lon,lat|lon,lat")` (no third field, so no `d` waypoint type, `RoutingParamCollector.java:37-43`);
`setParams` with exactly `profile`, `alternativeidx=0` and `profile:use_dynamic_range=0`; the engine built with
`outfileBase` and `logfileBase` null and `BROUTER_ENGINEMODE_ROUTING`; `quite = true`; `doRun(30_000)`; `terminate()` on a
cancel; then `getErrorMessage()` and `getFoundTrack()` (its nodes' `ILat`, `ILon`, `SElev`). A `grep` of `BRT` for every
other public method of the engine finds none [C]. Its profile is `hiking-mountain.brf` (`BRT:152`), which sets
`add_beeline false` and `turnInstructionMode 1` and none of `correctMisplacedViaPoints`, `continueStraight` or
`inverseRouting` [C: `grep` of Forager's `assets/routing/profiles2/`]. None of the oracle's 26 profiles switches any of
`correctMisplacedViaPoints`, `continueStraight`, `inverseRouting`, `add_beeline` or `allowSamewayback` on (11 assign
`correctMisplacedViaPoints = false`) [C: `grep` of `brouter/build/oracle/profiles2/`].

| Java lines | Method or field group | Sub-step | Why |
|---|---|---|---|
| 34 | `BROUTER_ENGINEMODE_ROUTING` | 8a | Forager's mode (`BRT:209`) |
| 35-38 | the modes `SEED`, `GETELEV`, `GETINFO`, `ROUNDTRIP` | left out | Forager passes `ROUTING` only (`BRT:209`) [R] |
| 40-67, 77-90 | `nodesCache`, `openSet`, `finished`, `waypoints`, `matchedWaypoints`, `linksProcessed` (written), `nodeLimit`, `MAXNODES_ISLAND_CHECK`, `islandNodePairs`, `engineMode`, `foundTrack`, `foundRawTrack` (written only), `alternativeIndex`, `errorMessage`, `routingContext`, `segmentDir`, `airDistanceCostFactor`, `lastAirDistanceCostFactor`, `guideTrack`, `matchPath`, `quite` | 8a | the search's state |
| 45, 52 | `extraWaypoints`, `useNodePoints` | left out | never assigned anything but `null` and `false` in the class (`:45`, `:52`, `:898`) [C: `grep`]; their branch (`:884-899`, `:1633`) cannot run |
| 56, 58, 60 | `MAX_STEPS_CHECK`, `ROUNDTRIP_DEFAULT_DIRECTIONADD`, `MAX_DYNAMIC_RANGE` | left out | round trip, and the dynamic range Forager sets to 0 |
| 69, 86-87 | `terminated`, `startTime`, `maxRunningTime` | 8c | terminate and the time limit |
| 47 (read) | `linksProcessed` read by `getLinksProcessed` | 8c | the progress count |
| 66, 71-76, 95 | `outputMessage`, `outfileBase`, `logfileBase`, `infoLogEnabled`, `infoLogWriter`, `stackSampler`, `outfile` | left out | output and log files: Forager passes both bases null (`BRT:209`); see the constructor row for the two log files |
| 88 | `boundary` | 8a, as `null` | public, never assigned in the class [C: `grep`]; `inRadius` is then always true (`:2231`). A non-null boundary is left out [I: only BRouter's server code sets it] |
| 92 | `extract` | left out | never read or written past its declaration [C: `grep`] |
| 94 | `directWeaving` | 8a, as `true` | `Boolean.getBoolean("disableDirectWeaving")`, a JVM property Forager never sets [I] |
| 96-100 | the five-argument constructor | left out | Forager calls the six-argument one (`BRT:209`) |
| 102-138 | the six-argument constructor | 8a | `ProfileCache.parseProfile` (`:133`). The `debug.txt` and `stacks.txt` logs (`:111-131`) are left out: they switch on only when such a file exists two folders above the profile [R]. The port throws a named "unsupported" if either exists, rather than silently not logging [I: Forager's profile folder holds neither; E3's logged regime puts `debug.txt` there on purpose, so the port is compared in E3's unlogged regime only] |
| 140-164 | `hasInfo`, `logInfo`, `logThrowable` | 8a, as no-ops | with both bases null and no `debug.txt` (refused above), `hasInfo()` is false and `logInfo` writes nowhere [R: `:141`, `:145-155`] |
| 166-168 | `run()` (`Thread`) | left out | Forager calls `doRun` on its own thread (`BRT:167`) |
| 170-197 | `doRun` | 8a: the `ROUTING` branch and its two-waypoint check; the other branches left out (modes) | |
| 199-361 | `doRouting` | 8a: the section loop, `findTrack`, the message and name (`:236-242`), the `getAlternativeIdx(0, 3)` pick (`:292`), `foundTrack`, the three catches and the `finally` (`:318-360`) | |
| 205-222 | `allowSamewayback` | left out | set only by the `allowSamewayback` parameter (`RoutingParamCollector.java:219-220`) [R], which Forager never passes |
| 233 | `routingContext.ai != null` | left out | `ai` is the `GETINFO` mode's [I] |
| 243-290, 293-296, 308-311 | `outfileBase` branch, `reportFormat=CSV`, the GPX print when `!quite`, `logfileBase` | left out | bases null, `quite = true` (`BRT:158`); `reportFormat` is a JVM property Forager never sets [I] |
| 325-328 | `catch (Error)`: `cleanOnOOM` | 8c (through `terminate`) | |
| 363-485 | `doGetInfo` | left out | `GETELEV`/`GETINFO` modes |
| 487-692 | `doRoundTrip` (487-521), `buildPointsFromCircle` (523-536), `getRandomDirectionFromData` (538-692) | left out | `ROUNDTRIP` mode |
| 696-809 | `postElevationCheck` | 8a | always called (`:1037`); fills heights (-111) |
| 811-814 | `logException` | 8a | sets `errorMessage` |
| 817-852 | `doSearch` | left out | public, the seed search; Forager never calls it [C: `grep` of `BRT`] |
| 854-856 | `cleanOnOOM` | 8c | calls `terminate()` |
| 858-877 | `findTrack(refTracks, lastTracks)` | 8a: the island retry; the `useDynamicDistance` branch (`:863-871`) left out (`use_dynamic_range=0`, `BRT:154`) | |
| 879-1052 | `tryFindTrack` | 8a, with these left out: `extraWaypoints` (`:884-899`, above); the nearby track (`:913-925`: `readBinary` of `rawTrackPath`, which only the `rawTrackPath` parameter sets [I]; with it null `readBinary` returns null [R: step 7's `ReadBinaryTest`]); `inverseRouting` (`:943-947`, `:995-999`); `continueStraight` (`:1004-1016`); `ai` (`:1020`); `poipoints` (`:1048-1049`, the `pois` parameter, `RoutingParamCollector.java:203`) | |
| 1022-1027 | the call to `snapPathConnection` | **via points, -24**: guarded by `correctMisplacedViaPoints`, off in all 27 profiles [C, above] | |
| 1054-1092 | `getExtraSegment` (39 lines) | left out, -24 | via points |
| 1094-1262 | `snapRoundaboutConnection` (169 lines) | left out, -24 | via points |
| 1265-1480 | `snapPathConnection` (216 lines) | left out, -24: the port keeps the guard (`:1265-1266`, `from`/`to` return at once) and throws a named "unsupported" past it | |
| `OsmTrack.java:442-446` | `getFromDetourMap` (5 lines; -24's fourth method) | already ported in step 7 (-111's "`OsmTrack` whole"; step 7 report §15) | recorded, not changed |
| 1482-1591 | `recalcTrack` | 8a | always called (`:1039`) |
| 1600-1606 | `elevationFilter` | 8a | called from `recalcTrack` or `postElevationCheck` [I: not yet read past its signature] |
| 1609-1689 | `matchWaypointsToNodes` | 8a; the dynamic-range retry (`:1615-1625`) and the beeline points (`:1633-1688`) left out (`use_dynamic_range=0`; `add_beeline false`) | |
| 1691-1706 | `searchTrack` | 8a; the beeline segment (`:1697-1702`, `WAYPOINT_TYPE_DIRECT`) left out: only a `d` waypoint, a `straight` parameter (`RoutingParamCollector.java:198`) or the beeline points set it [R] | |
| 1708-1805 | `searchRoutedTrack` | 8a: the two passes, the sloppy merge, the reference track, the re-tracking; the nearby-track branch (`:1717-1735`) left out (above); the timeout catch's dirty track (`:1749-1758`) 8c | |
| 1808-1816 | `resetCache` | 8a (`memoryclass` passed through; its pressure is 8b's) | |
| 1818-1829, 1832-1870 | `getStartPath` (both) | 8a | |
| 1872-1887 | `findTrack(operationName, …)` | 8a | |
| 1890-2261 | `_findTrack` | 8a, except: `terminated` and the timeout (`:1974-1983`) 8c; the memory path (`:2006-2038`: `isInMemoryBounds`, `collectOutreachers`, `canEscape`, panic mode, "memory limit reached") 8b; the early exit (`:2042-2053`, `fastPartialRecalc`, true only on the nearby track's re-routing, `:1720`) left out | |
| 2263-2267 | `addToOpenset` | 8a | |
| 2269-2319 | `compileTrack` | 8a | |
| 2321-2350 | `mergeTrack` | 8a | reached by the sloppy merge (`:1762`) without a nearby track |
| 2352-2386 | `getPathPeak`, `getOpenSet` | left out | Forager never calls them [C: `grep` of `BRT`] |
| 2388-2426, 2440-2442 | `isFinished`, `getDistance`, `getAscend`, `getPlainAscend`, `getTime`, `getFoundInfo`, `getAlternativeIndex`, `getFoundRawTrack`, `getOutfile` | left out | Forager never calls them [C]; not declared, so nothing can return a made-up value |
| 2392-2394 | `getLinksProcessed` | 8c | E3's FK 11 reads it |
| 2412-2414, 2428-2430 | `getFoundTrack`, `getErrorMessage` | 8a | |
| 2432-2438 | `terminate`, `isTerminated` | 8c | |
| (none) | `route()` and `checkDataFile` over the engine (`Route.kt`) | 8d | `RouteOutcome.Found` has no heights yet; -111 adds them, so 8d changes the public surface [R: `Route.kt:39-43`] |

**Counts** [C, from the ranges above]: via points 39 + 169 + 216 = 424 lines in `RoutingEngine`, plus `getFromDetourMap`'s 5
in `OsmTrack`, which step 7 ported: -24's 429.

**Classes outside `RoutingEngine` it calls that are not ported** (each class name in `RoutingEngine.java` that names a
file under `btools/`, checked for a declaration in `commonMain` by `git grep`) [C]:
- `AreaReader` (1 use, in `doGetInfo`): left out with that mode.
- `StackSampler` (3 uses, the `stacks.txt` log): left out, refused as above.
- Every other class it names is ported: `AreaInfo`, `CheapAngleMeter`, `CheapRuler`, `CompactLongMap`, the four `Format*`,
  `Formatter`, `MatchedWaypoint`, `MessageData`, `NodesCache`, `OsmLink`, `OsmLinkHolder`, `OsmNode`, `OsmNodeNamed`,
  `OsmNodePairSet`, `OsmNogoPolygon`, `OsmPath`, `OsmPathElement`, `OsmPos`, `OsmPrePath`, `OsmTrack`, `ProfileCache`,
  `RoutingContext`, `RoutingIslandException`, `SearchBoundary`, `SortedHeap`.
- The six car profiles' `KinematicModel` is refused by `RoutingContext` already (-89's ruling).

Nothing found that cannot be placed between "ported" and "left out".

## 2. 8a's test plan

**Strata, in order:** F0 (Forager's own two routes on `W75_N40.rd5`, `hiking-mountain`) this session; then R on its 20
StdModel profiles (1,180 cases; the 354 Kinematic cases check the refusal, -89) and DL-lat and DL-lon (59 each). Each
case is taken from the stratum's own generator or list, as 1b's harness builds it, never rebuilt by hand.

**What is compared, per case** (-111; E1): the port's engine and the oracle's engine are each run in this JVM with
Forager's calls (as `E3CaseRun.attempt`'s unlogged branch: `getWayPointList`, `setParams` with the three parameters,
`quite = true`, `doRun(0)`), and the two outcomes are compared as `E3Outcome`: the kind (the error message, or `route`),
the node count, the cost, the distance, the FNV-1a digest over every route point's `(ilon, ilat)` in order, and the one over
every route point's `SElev` in order (`Short.MIN_VALUE` included). The oracle's in-process outcome is also held to
`expected-1b.tsv`'s stored row for the case, by `tupleDigest` where the row's `compare` is `exact`, so the comparison is
anchored to 1b's values and not only to a second run. Nothing with a position is printed or committed: digests and
counts only (E1).

**Red first:** the port's `RoutingEngine` with 8a's methods as `TODO` stubs naming RECORD -90; the F0 test seen failing
with a `NotImplementedError` naming a stub, over fresh XML with 0 ` e: `.

**The revert I predict** (to run once 8a passes on F0): in `_findTrack`'s port (Java `:2218`), the best-path comparison
`testPath.cost < bestPath.cost` flipped to `>`, so the costliest of a link's paths is kept. Prediction: at least one of
F0's two routes fails, on its cost or its node digest first [I: which of the two, and which field, is not predicted].

## 3. Session 29 (partial: stopped at the planner's handoff call)

Plumb Ledger, session 29, `brouter-port-5b` `461e4d6` to the head that carries this section. Trailer on every commit
`Co-Authored-By: Claude <noreply@anthropic.com>`. No stop hit.

**Done:**
1. **§1 and §2** pushed alone first (`22266d1`).
2. **Red over stubs** (`15a4aa2`, red on purpose; `step8/01-red-f0.txt`): `router/RoutingEngine.kt` with the constructor
   (profile parse; `debug.txt`/`stacks.txt` refused) and `doRun` as a `TODO` naming RECORD -90; `RoutingEngineF0Test` runs
   F0's two cases through the oracle in this JVM (`E3CaseRun.run` on the oracle's folder), holds each to its
   `expected-1b.tsv` row by `tupleDigest` (`exact`), then compares the port field by field (kind, nodes, cost, distance,
   node digest, heights digest). Filtered run: fresh XML, 1 test, 1 failure, `NotImplementedError` naming
   `RoutingEngine.doRun`, 0 ` e: `; the oracle-against-expected assertion passed before it. Full fresh
   `:brouter:jvmTest --rerun-tasks`: 77 XML, 160 tests, 3 failures (CollectOutreachersTest 2, RoutingEngineF0Test 1 on
   purpose), 0 ` e: ` (two grep hits are test output text, not compiler lines), no Maven 429 [R].
3. **8a's port, written, not compiled or run:** `step8/02-partial-routingengine-8a.patch`, marked partial, not in the
   tree. It covers `doRun`, `doRouting` (no output/log branches), `postElevationCheck`, `logException`, both `findTrack`s,
   `tryFindTrack`, `snapPathConnection`'s guard (-24, throws past it), `recalcTrack`, `elevationFilter`,
   `matchWaypointsToNodes`, `searchTrack`, `searchRoutedTrack`, `resetCache`, both `getStartPath`s, `_findTrack`
   (as `findTrackInner`), `addToOpenset`, `compileTrack`, `mergeTrack`. The memory path and the open border list are
   `TODO`s naming 8b; the time limit (`doRun` with a non-zero limit) a `TODO` naming 8c; `catch (Error)` not yet ported
   (8c). Every option §1 leaves out throws `UnsupportedOperationException` naming it.
4. **CI:** `15a4aa2` (run 43085259189) was still in progress at the handoff; not read.
5. **E1:** 0 on both staged diffs; no hit to fix.

**[I]/[?]:**
- [I] The patch's `postElevationCheck` dereferences `tmp.message` with `!!` where Java does without a check; Java's
  NPE would carry a helpful message, the port's would not. Believed unreached (re-tracked nodes all carry a message).
- [I] Java's `for`-update `link.getNext(...)` runs after the body; the patch keeps that order in all three link loops.
- [I] `continueStraight` refused only before a middle waypoint, as Java acts only there.
- [?] `toDouble`, `Throwable.toString` on Native: step 10.

**Questions for the planner:**
- §1's `RouteOutcome.Found` has no heights; -111's heights change the public surface at 8d. Confirm that is wanted there.
- The test compares in E3's unlogged regime only (the port refuses `debug.txt`). Is that the reading -90 intends?

**Next, for session 30:**
1. Read CI for `15a4aa2` (TOTAL lines, ` e: `, both `compileKotlinIos*`, the gate; simulator-checked).
2. Apply `step8/02-partial-routingengine-8a.patch` (`git apply`), compile, fix compile errors only, read the patch
   against Java `:858-1052`, `:1609-2350` once more, then run `*RoutingEngineF0Test`.
3. On a pass: full fresh `jvmTest --rerun-tasks`, push; then revert per §2 (prediction pushed first).
4. Then R (20 StdModel profiles; Kinematic refusals) and DL-lat/DL-lon, per §2.

## 4. Session 30

Plumb Ledger, session 30, `brouter-port-5b` `2fddfcc` to the head that carries this section. Trailer on every commit
`Co-Authored-By: Claude <noreply@anthropic.com>`. E1 scan on every staged diff: 0, no hit to fix.

**Done:**
1. **CI for `15a4aa2`** (run 43085259189): JVM `TOTAL: tests 160, failures 3` in 77 files (CollectOutreachersTest 2, F0
   red on purpose); 0 ` e: `; iOS klibs (`compileKotlinIosArm64`, `compileKotlinIosSimulatorArm64`) BUILD SUCCESSFUL;
   commonTest on the simulator 24 tests, 0 failures; the arm64 gate 7 tests, 0 failed. Simulator-checked.
2. **The 8a port** (`4dcd049`): session 29's patch applied clean and compiled with 0 errors and no change.
   `RoutingEngineF0Test`, fresh XML: 1 test, 0 failures; both routes equal the oracle on all six fields. Full fresh
   `:brouter:jvmTest --rerun-tasks`: 77 XML, 160 tests, 2 failures (CollectOutreachersTest, known), 0 ` e: ` (one
   earlier attempt stopped at `msunReference` on an HTTP 503 fetching a pinned source; the retry ran clean).
   **CI for `4dcd049`** (run 43088039556): JVM 160 tests, 2 failures; 0 ` e: `; both iOS klibs BUILD SUCCESSFUL;
   simulator 24/0; arm64 gate 7/0. Simulator-checked.
3. **§2's revert** (`step8/03-revert-findtrack-cost.txt`; prediction pushed first, `70c4c20`): `testPath.cost <`
   flipped to `>` at `RoutingEngine.kt:841` (Java `:2219`). Compiled, 0 ` e: `, fresh XML: F0 1 test, **0 failures.
   The prediction failed:** F0 does not catch it. Restored `cmp`-equal; the port equal to `4dcd049` after. A later
   diagnostic (below) shows R catches it.
4. **R and DL** (`377d109`, red on purpose; `step8/04-red-r-dl.txt`): `RoutingEngineRTest` (26 profiles x 59 from
   `RGenerator` with 1b's seeds) and `RoutingEngineDLTest` (`DLGenerator`'s draws with 1b's seeds, the ids of
   `expected-1b`'s rows within 1b's cap), compared as F0's test, the oracle first held to its row. Both red, fresh XML,
   0 ` e: `: R 1 test 1 failure, DL 2 tests 2 failures, each a `NotImplementedError` naming the memory path, step 8b.
   Full fresh run: 79 XML, 163 tests, 5 failures (CollectOutreachersTest 2; R 1, DL 2 on purpose), 0 ` e: `.
   `E3DLTest`'s companion made `internal` (visibility only) so the new test reads its seeds instead of copying them.
5. **Diagnostics, not committed** (each test's port call wrapped to count the 8b stub and go on; restored from saved
   copies, `cmp` equal): R: all 1534 oracle runs equal their rows; 354 Kinematic cases refused by name; of the 1180
   StdModel cases **151 reach the 8b stub**, the other 1029 (818 routes, 211 errors) equal the oracle on all six fields.
   DL-lat **36 of 59** reach the stub, 23 equal; DL-lon **15 of 59**, 44 equal. 0 mismatches anywhere. With §2's
   revert applied: F0 and DL 0 mismatches, **R 176 mismatches** (the first 20 all on nodes or cost and the digests).

**[I]/[?]:**
- [I] F0 misses §2's revert because a link rarely holds more than one undominated path on F0's two routes; not counted.
- [I] Neither R nor DL passed on first run, so no second revert was owed; the one revert R's comparison does catch is
  shown only through the uncommitted diagnostic, since the committed test stops at the first 8b stub.
- [?] Kotlin/Native numbers and `Throwable.toString`: step 10. iOS: simulator-checked only.

**Questions for the planner:**
1. §2 put R and DL in 8a, but 151 of R's StdModel cases and 51 of DL's 118 reach the memory path (8b) at memory class
   64. Read as a false premise of §2's strata, reported, not chosen: 8a cannot be green on R or DL before 8b. Should R
   and DL stay red until 8b (as committed), or should 8a's comparison be split by whether a case reaches the memory path?
   (The second would change a test's comparison after a port result, so it is yours to rule.)
2. F0 has never been seen failing on a revert of the ported search, only over the stub. Is R's catch of §2's revert
   (diagnostic) enough, or should F0 carry a revert of its own?

**Is 8a complete?** The port of 8a's methods is in, compiles, and equals the oracle on F0 and on every R and DL case
that does not reach the memory path. Its strata are not green: R and DL wait on 8b. Not complete by §2's terms.

**Next:**
1. The planner's ruling on question 1.
2. 8b: the memory path (`collectOutreachers`, panic mode) and the open border list; then R and DL run whole, and §2's
   revert re-run on the committed R test.
3. CI for this head (docs only). **CI for `377d109`** (run 43092122738), read: JVM 163 tests, 5 failures
   (CollectOutreachersTest 2, R 1, DL 2, as local); 0 ` e: `; both iOS klibs BUILD SUCCESSFUL; simulator 24/0; arm64
   gate 7/0. Simulator-checked.
4. 8c (time limit, `catch (Error)`), 8d (heights in `RouteOutcome.Found`, -122).

## 5. Session 31

Plumb Ledger, session 31, `brouter-port-5b` `0ab64b6` to the head that carries this section (fast-forward only).
Trailer on every commit `Co-Authored-By: Claude <noreply@anthropic.com>`. E1 scan on every staged diff: 0, no hit to fix.

**Done:**
1. **A revert F0 catches** (`step8/05-revert-f0-endnode-cut.txt`; prediction pushed first, `b62ea53`): compileTrack's
   final-track end-node cut removed (`RoutingEngine.kt:896`, Java `:2272-2275`). Chosen because every route ends with
   the re-tracking pass, which compiles through that line. Predicted: nodes the first field to differ, the port one more,
   cost equal. Observed: 0 ` e: `, fresh XML, F0 1 test 1 failure, "nodes expected:<226> but was:<227>"; the printed
   tuple: kind and cost (5302) equal, distance 5047 against 5086, both digests differ. **The prediction held.** Restored
   `cmp` equal. F0 has now been seen red on a wrong port.
2. **8b, the memory path** (`543693e`): `RoutingEngine.java:1970-2040` into `findTrackInner`: `memoryPanicMode`,
   `needNonPanicProcessing`, the open border list back into the open set when it empties, `collectOutreachers`, the
   `canEscape` sweep, `clearTemp`, the second bounds check, "memory limit reached" and panic mode; the two `logInfo`
   lines not kept (quite, no log). Compiled with 0 ` e: ` and no fix. **R and DL pass on first run** over the port:
   R 1534 cases (oracle route 968, refused 354, error 212), 0 mismatches; DL-lat 59 (501 draws) and DL-lon 59 (326
   draws), 0 mismatches. Full fresh `:brouter:jvmTest --rerun-tasks`: 79 XML, 163 tests, 2 failures
   (CollectOutreachersTest), 0 ` e: `.
3. **MP64, ML5, PM5** (`31aa7b1`, `RoutingEngineMemoryTest`): 1b's `RGenerator` as `E3MemoryTest` builds it (its seeds
   and tile; the 20 km, ratio 5 arguments repeated, held by each oracle run's digest against its row), the ids of
   `expected-1b`'s rows within 1b's cap, compared as DL. **All three pass on first run:** MP64 59 (86 draws), ML5 59
   (246), PM5 55 (1180), 0 mismatches each (3 tests, fresh XML). `E3MemoryTest`'s companion made `internal` (visibility
   only), as `E3DLTest`'s was in session 30.
4. **-90's revert** (`step8/06-revert-isinmemorybounds.txt`; prediction pushed first, `e501548`): `return total <=
   currentmaxmem` to `>` in `OsmNodesMap.isInMemoryBounds` (`:123`; the prediction said `:122`, same string). 0 ` e: `,
   4 fresh XML, 7 tests, 6 failures. MP64 59/59, PM5 55/55, R 805, DL-lat 59/59, DL-lon 59/59 mismatches, all with the
   port "memory limit reached"; ML5 0. **The prediction held** on every stratum it named; F0 (not predicted) also red, on
   kind. Restored `cmp` equal.
5. **A revert ML5 alone catches** (`step8/07-revert-ml5-limit-message.txt`; prediction pushed first, `7e24058`): the
   limit's message (`RoutingEngine.kt:719`) to "memory limit". Only ML5's rows carry the limit. Observed: ML5 59/59 on
   [kind] only; F0, R, DL-lat, DL-lon, MP64, PM5 0. **Held.** Restored `cmp` equal.
6. **CollectOutreachersTest (-61), not touched:** its 2 failures are unchanged with the engine driving
   `collectOutreachers`: the same `C` line (`oracle [C 38715 41099 41099] port [C 39396 37387 ...]`) and "canEscape did
   not answer both ways: false/true 0/3587" as 5b's record. No news.
7. **CI read:** `543693e` (run 43098746396): JVM `TOTAL: tests 163, failures 2` (CollectOutreachersTest), 0 ` e: `,
   `compileKotlinIosArm64` and `compileKotlinIosSimulatorArm64` BUILD SUCCESSFUL, simulator 24/0, arm64 gate 7/0
   ("success"). `31aa7b1` (run 43099991963): JVM 166 tests, 2 failures (the same), 0 ` e: `, both iOS klibs, simulator
   24/0, gate 7/0. Simulator-checked. `e501548`'s run was cancelled by the next push (docs only).

**[I]/[?]:**
- [I] The stratum-alone reverts: ML5's is item 5. PM5's and MP64's were not run. PM5's candidate is a panic-mode line
  (only memory class 5 panics in 1b), but ML5's cases panic too (54 of 59), so a panic edit may not be PM5's alone.
  MP64 showed neither panic nor the limit in 1b, and R and DL at memory class 64 reach the same collection, so no
  revert MP64 alone catches has been found. Both are caught by -90's revert, with R and DL.
- [I] ML5's own revert moves a message, not a decision; a wrong choice of when to throw the limit is not yet shown caught.
- [I] No stack overflow or out-of-memory in any memory-path run. **Corrected (the planner's, RECORD -124):** the
  parenthesis that stood here, "(the 5b graph half is iterative)", was false. `OsmNodesMap.kt:58-65`'s
  `cleanupPeninsulas` is recursive and, on the JVM, catches a `StackOverflowError` and carries on, printing one line
  to stderr, as Java does (-61's rejected option was the rewrite without recursion). Observed: CI's log on `31aa7b1`
  prints that line 3 times, all by `CollectOutreachersTest`, none by the six route groups (the planner's reading, -124);
  session 32's fresh full run on `8deeb46` (80 XML, 166 tests) gives `grep -c 'caught a StackOverflowError'` 3 over
  the log and 3 over the XML, `CollectOutreachersTest` 3, every other class 0. On iOS there is no catch (-50; step 10).
- [?] Kotlin/Native numbers and `Throwable.toString`: step 10. iOS: simulator-checked only.

**Questions for the planner:**
1. Is a revert that MP64 alone catches required, given that R and DL reach the same memory path at memory class 64? If
   so, which property of MP64 (20 to 100 km in one tile) should it target?
2. Is ML5's message revert enough for ML5, or should it also carry a revert of when the limit is thrown?

**Are 8a and 8b complete?** 8a: R and DL now pass whole and F0 has caught a wrong port; by -123's terms, complete. 8b:
the memory path is ported and its three strata pass, -90's revert bites as predicted; complete except for the
stratum-alone reverts of MP64 and PM5 (question 1).

**Next:**
1. The planner's answers to questions 1 and 2; if owed, the MP64 and PM5 reverts, predictions pushed first.
2. CI for this head (docs only).
3. 8c (terminate, the time limit, the progress count, `catch (Error)`), 8d (heights in `RouteOutcome.Found`, -122).

## 6. Session 32 (partial: stopped at the planner's handoff call)

Plumb Ledger, session 32, `brouter-port-5b` `8deeb46` to the head that carries this section (fast-forward only).
Trailer on every commit `Co-Authored-By: Claude <noreply@anthropic.com>`. E1 scan on every staged diff: 0, no hit to fix.
No source file changed in this session; every commit is docs.

**Done:**
1. **§5's line 264 corrected in place** (the planner's correction, -124): the false "(the 5b graph half is iterative)"
   replaced by what was observed. A fresh full `:brouter:jvmTest --rerun-tasks` on `8deeb46`: 80 XML, `TOTAL: tests
   166, failures 2` (both `CollectOutreachersTest`, -61, unchanged), 0 ` e: `. `grep -c 'caught a StackOverflowError'`:
   3 over the log, 3 over the XML; per class `CollectOutreachersTest` 3, each of the other 79 classes 0.
2. **Revert (a), the limit's condition** (`step8/08-revert-limit-condition.txt`; prediction `1adb40c`, result `fdc5ec9`):
   `maxTotalCost < 1000000000` to `<=` (always true: every panic becomes the limit). Cannot loop; run under 1200 s per
   part all the same. **Held on all seven groups:** PM5 55 of 55 (port the limit, oracle route), ML5 0, MP64 0, F0
   passed, DL-lat 0, DL-lon 0, R 0 of 1534. Restored `cmp` equal. R and DL passing also shows none of their cases panics.
3. **Revert (b), panic mode's refill** (`step8/09-revert-panic-refill.txt`; prediction `71a466e`, result in this
   section's commit): the refill loop's `openSet.add` removed (the parked paths dropped). Cannot loop. **The prediction
   failed on its numbers:** PM5 48 of 55 (predicted at least 50), ML5 53 of 59 (predicted 54), every printed mismatch the
   port's "no track found at pass=0"; the groups held exactly (MP64, F0, DL-lat, DL-lon, R 0). Restored `cmp` equal.
4. **8c: not started in code.** Read: Java `doRun`, `doRouting` (`:199-236`, `:312-361`), `logException`, `cleanOnOOM`,
   `_findTrack`'s loop head (`:1960-1990`), `searchRoutedTrack` (`:1708-1805`), `mergeTrack`, the getters; Forager's
   `BRouterTrailRouter.kt:117-180` (`doRun(maxRunningTimeMillis)` on its own engine thread; `terminate()` on a cancel,
   then a non-cancellable join; reads `getErrorMessage()` then `getFoundTrack()`; never `getLinksProcessed`,
   `isTerminated` or `getFoundRawTrack`). Drafted, **not compiled or run**: `step8/10-8c-draft-EngineClock.kt.txt` (an
   injected `fun interface EngineClock`, with a monotonic default for callers) and
   `step8/10-8c-draft-RoutingEngineStopTest.kt.txt` (FK 9, 10, 11 against the oracle on the kind and the message's fixed
   part; the progress count exactly on FK11's cases run to the end; Forager's 30 s limit on a stopped clock; the limit,
   the watchdog's seconds, an Error and an Exception through a driven clock). It needs `E3FKTest`'s companion made
   `internal` and a clock argument on the port's constructor.
5. **CI:** no code push this session, so no code CI to read. `CollectOutreachersTest`'s 2 failures are unchanged in the
   local full run (-61); not touched.

**[I]/[?]:**
- [I] No stack overflow in any route group on the JVM: shown by the count in item 1 (route classes 0). iOS: step 10.
- [I] No hang in either revert: all four parts printed a BUILD line (7m 36s, 11m 12s, 7m 54s, 11m 11s), under 1200 s.
- [?] Revert (b)'s misses: 7 PM5 cases equal without the refill, and 6 ML5 cases not moved. Only the first 20 mismatches
  are printed, so which cases they are was not seen.
- [I] Found while reading for 8c: the `maxRunningTime / 3` case (`:1980`) needs `fastPartialRecalc`, which the port holds
  false (`check`, §1: only the nearby track sets it), so no test can reach it; it can be ported as written, not shown.
  The dirty track (`:1749-1758`) writes `foundRawTrack` only, which nothing in Forager's path reads; also not testable.
- [I] No E3 row reaches `catch (Error)` (no row's kind is an Error's `toString`): its draft test drives it through the
  clock, against Java's text, not the oracle.

**Questions for the planner:**
1. Revert (b) failed on its counts but held on every group and the direction. Is a second try owed (a new prediction,
   e.g. one of the two flags, which can loop and so runs under the limit), or does (b) as observed close 8b?
2. Is a test of the `maxRunningTime / 3` case owed, when the only path to it is left out (§1)?

**Are 8b and 8c complete?** 8b: both reverts are run; (a) held, (b) failed on numbers only; complete if the planner rules
on question 1. 8c: not complete; only reading and an unrun draft.

**Next:**
1. The planner's ruling on question 1; if owed, revert (b)'s second try, prediction pushed first.
2. 8c from the drafts: the tests over a stub (`terminate`, `isTerminated`, `getLinksProcessed` as `TODO`, plus the
   existing time-limit `TODO`), seen red with the `NotImplementedError` named, pushed red; then the port (`@Volatile
   terminated`, `startTime`/`maxRunningTime` from the clock, the loop head, the re-tracking reset `:1797`, the dirty
   track, `catch (e: Error) { cleanOnOOM(); logException(e) }`); CI read.
3. 8c's revert (-90): `terminated` written but not read, prediction pushed first.
4. 8d (heights in `RouteOutcome.Found`, -122).

## 7. Session 33 (stopped: a false premise in a draft test, found by the first port result)

Plumb Ledger, session 33, `brouter-port-5b` `3fc4341` to the head that carries this section (fast-forward only). Trailer on
every commit `Co-Authored-By: Claude <noreply@anthropic.com>`. E1 scan on every staged diff: 0, no hit to fix.

**Done:**
1. **Red, pushed (`124e6da`).** `RoutingEngineStopTest` from session 32's draft, copied unchanged;
   `EngineClock.kt` as drafted; `RoutingEngine` takes `clock: EngineClock = EngineClock.MONOTONIC`; stubs
   `getLinksProcessed`, `terminate`, `isTerminated` as `TODO` naming 8c, the time-limit `TODO` kept; `E3FKTest`'s
   companion `private` to `internal` (no comparison changed). Run of the class: fresh XML, 0 ` e: `, `tests 9, failures
   9`, every one a `NotImplementedError`: "the time limit" 5, "terminate" 2, "the progress count" 2. The commit message
   states how the port takes time and what is compared where the oracle depends on wall time, before any port result.
2. **The port, run once, not committed to the tree:** saved as `step8/11-8c-port.patch` (applies to `124e6da`; applying
   it reproduces the tested file, `cmp` equal; the tree was restored to `124e6da`'s file). It ports `@Volatile
   terminated`, `startTime`/`maxRunningTime` from the clock (`:201-203`), the loop head (`:1975-1984`, the plain limit;
   `maxRunningTime / 3` left out and named, its guard the `check` on `fastPartialRecalc`, RECORD -125), the re-tracking
   reset (`:1797`), the timeout's dirty raw track (`:1749-1758`), `catch (e: Error) { cleanOnOOM(); logException(e) }`,
   `cleanOnOOM`, `terminate`, `isTerminated`, `getLinksProcessed`. Not ported: `logThrowable` and the execution-time
   line (the info log, §1).
   - Class run on the port: `tests 9, failures 1`, 0 ` e: `, fresh XML. FK9 10 cases 0 mismatches (both sides'
     operation `pass0`, 20 of 20), FK10 0, FK11 0, the FK11 count exact, the 30 s stopped clock, the watchdog's seconds,
     the Error and the Exception tests pass.
   - **Fails: `theTimeLimitReadsTheInjectedClock`**: expected `pass0 timeout after 30 seconds`, the port gives
     `target-island-check timeout after 30 seconds`. The port is Java's here: `tryFindTrack` runs
     `findTrack("target-island-check", ...)` (`RoutingEngine.java:961`; port `:290`) before `searchRoutedTrack`, and
     that is the first pass through `_findTrack`'s loop head. The draft's expected operation is a false premise. **No fix
     made** (the port matches Java; changing the test's expected text would change a comparison after a port result).
   - Full fresh `:brouter:jvmTest --rerun-tasks` on the port: 81 of 81 XML fresh, 0 ` e: `, `TOTAL: tests 175, failures
     2`: that test and `CollectOutreachersTest` 1 of 2 (`collectOutreachersWithADestinationVanishesFarNodes`), BUILD in
     33m 56s.
3. **Reverts: not run** (the stop came first; there is no passing port in the tree to revert).
4. **CI on `124e6da`** (run 43118062551): jvm job `TOTAL: tests 175, failures 11` (StopTest 9, the red; CollectOutreachers
   2, -61, expected, not touched), 0 ` e: `; the JVM step took 29m 29s and the job's 30-minute limit cancelled "iOS klibs"
   (`compileKotlinIosArm64` not seen to finish). ios-smoke: success, `compileKotlinIosSimulatorArm64` ran, 24 tests 0
   failures, the gate 7 tests 0 failures: simulator-checked, over the stubs (the port has had no iOS compile).

**[I]/[?]:**
- [I] No compile error in any local run: `grep -c '^e: '` over the red, port-class and full logs: 0, 0, 0; CI `grep -c ' e: '`: 0.
- [?] `CollectOutreachersTest` failed 1 of 2 locally (not 2 of 2 as in sessions 31-32 and in CI), and `grep -c 'caught a
  StackOverflowError'` over the full log reads 0 (3 in session 32). The class does not reach `RoutingEngine` (`grep -n
  'RoutingEngine(' CollectOutreachersTest.kt`: none) and the patch touches only `router/RoutingEngine.kt`, so the change is
  not this patch's; the cause is not known (the stack overflow's depth varies by run?). Not touched.
- [?] FK9's fixed part `(pass0|pass1|re-tracking)` has the same gap as the failing test: a limit striking in the island
  check would read `target-island-check timeout after 0 seconds` and fail it on either side. It did not here (20 of 20
  `pass0`), so FK9 passing depends on the island check taking under 50 ms on both sides.
- [?] Kotlin/Native: `catch (e: Error)` is `kotlin.Error` in commonMain and compiles for the JVM; it has not been compiled
  for iOS (patch not pushed). Whether Native raises a catchable `Error` for out-of-memory or a stack overflow is step 10's.
- [I] The jvm CI job's 30 minutes: 3fc4341's JVM step took 28m 10s, 124e6da's 29m 29s with the stub class failing fast; the
  ported class runs about 1.5 min locally, so the jvm job will likely be cancelled before "iOS klibs" on every push.

**Questions for the planner:**
1. `theTimeLimitReadsTheInjectedClock` expects `pass0`; Java gives `target-island-check`. May the test's expected text be
   corrected to Java's (`target-island-check timeout after 30 seconds`), and FK9's fixed part widened to
   `(target-island-check|pass0|pass1|re-tracking)`? Both are changes to a comparison after a port result, so not mine.
2. The jvm job's 30-minute limit: raise it, or move the 8c class elsewhere? A CI change, not mine.

**Is 8c complete?** No: the port exists as a patch, one test fails on a false premise, and neither revert is run.

**Next:**
1. The planner's ruling on question 1; then the patch applied, the class and a full fresh run, commit, push, CI read.
2. 8c's revert (-90): `terminated` written but not read, prediction pushed first, under `timeout` 1200 s.
3. The second revert, in the time limit, the same way.
4. The ruling on question 2.
5. 8d (heights in `RouteOutcome.Found`, -122).
