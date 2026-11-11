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
