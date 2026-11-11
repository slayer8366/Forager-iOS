# Item I, step 6: the path model (session 10, piece 1 of several)

Coder: Plumb Ledger. Dispatch: RECORD -92; rulings during the session: -93 (the coordinate scan), -94 (`OsmTrack`).
Branch `brouter-port-5b`. Evidence: `docs/audits/2026-11-06-brouter-port-step6/`.

Marks: **[R]** read in a source named beside it; **[C]** computed or run here, with the output named; **[I]** inferred,
not checked; **[?]** open.

## 1. The merge from `brouter-port`

- `git merge --no-ff origin/brouter-port` (`fcd35ff`) into `b65548f` gave `eac836d` [C]. The merge touched no file under
  `brouter/src/commonMain` [C, `git status`].
- **Two conflicts, both resolved by keeping 5b's side, which contains `brouter-port`'s** [C]:
  - `brouter/build.gradle.kts`, three hunks. Both branches had added F0h's fetch, with different comment wording, and 5b
    had also added the drop-off fetch and its `dependsOn`/system property.
  - `TestTiles.kt`, two hunks: the same F0h comment, and 5b's `dropoff` property.
- **Full `:brouter:jvmTest` after the merge:** `compileKotlinJvm`, `compileTestKotlinJvm` and `jvmTest` executed, 0 ` e: `
  lines. 121 tests, 2 failed, both `CollectOutreachersTest` (-61) [C]. 121 = 5b's 111 + `E3CoverageRuleTest` 2,
  `E3ExpectedFileTest` 6 and `E3OutcomeTest` 2 [C, `tools/junit-counts.py`].
- **The coordinate scan was not honoured on this push.** It read 12, and the push was chained after it without a test.
  All 12 lines are in four files byte-identical to `origin/brouter-port` [C]. The planner ruled at -93 that 10 are
  Forager's own published ends and 2 are false positives (a float list, and a diff timestamp on two lines); there is no
  rewrite. Since then every push runs only on a 0 from `03-scan.sh`, which removes Forager's own ends by exact value
  (routes.json and points.json at a0535ece) before matching [C].
- **CI, run 42246987233 on `eac836d`** [C, job logs read]:
  - `jvm`: 121 tests, the same 2 red, 0 ` e: `; `compileKotlinIosArm64` and `compileKotlinIosSimulatorArm64` ran, BUILD
    SUCCESSFUL;
  - `ios-smoke`: 24 of 24 passed, **simulator-checked**; the arm64 JVM step's outcome was success, 7 of 7, and its gate
    printed "OK: all 2 classes ran".

## 2. atomicfu 0.33.0

- Pinned in `gradle/libs.versions.toml` as `atomicfu = "0.33.0"`, and added as `commonMain` `implementation(libs.atomicfu)`,
  a library with no plugin, as Phase 1's probe used it [C, `a9fb1d0`]. `jvmCompileClasspath` resolves
  `atomicfu:0.33.0 -> atomicfu-jvm:0.33.0` [C].
- **CI, run 42248137190 on `a9fb1d0`, before any code used it:** both `compileKotlinIos*` ran, BUILD SUCCESSFUL, 0 ` e: `;
  jvm 121 with the same 2 red; simulator-checked 24 of 24; arm64 7 of 7 with its gate [C]. With no code using it, this
  shows the klibs build with the dependency declared, not that its classes link [I].
- **The run on `de4d994`, the first where `ProfileCache` uses `SynchronizedObject`, is the full check.** It is read
  below in §5, or by the next session if this one ends first.

## 3. Step 6's files (sent to the planner; step 7 follows step 6, -94)

New, under `brouter/src/commonMain/kotlin/com/zynergylabs/forager/brouter/router/`. The Java `btools.router` classes:

| Class | Java lines [C, `wc -l`] | State at `de4d994` |
|---|---|---|
| `RoutingContext` | 579 | configuration half ported (`:1-284`, `:555-579`); nogo and distance half (`:285-554`) not yet; may become two files |
| `ProfileCache` | 142 | ported, with the lock |
| `OsmPathModel`, `OsmPrePath`, `StdModel`, `RoutingIslandException` | 20, 26, 36, 4 | ported |
| `OsmPath`, `StdPath` | 434, 356 | stubs |
| `OsmPathElement`, `MessageData`, `OsmNodeNamed`, `RoutingParamCollector`, `SearchBoundary`, `AreaInfo`, `OsmNogoPolygon` | 127, 104, 100, 374, 87, 71, 488 | not yet written |
| `OsmTrack` (step 7's) | — | the type alone; `containsNode` throws `"unsupported: OsmTrack.containsNode, step 7"` (-94) |

The Java total is 2,948 lines with comments [C]; Phase 1's 2,068 counts differently [?].
Tests go under `brouter/src/jvmTest/kotlin/.../router/`. No existing `commonMain` file has changed [C, `git diff --stat`
from `a9fb1d0`].

## 4. Piece 1: `RoutingContext`'s configuration half and `ProfileCache`

- **Stubs first, then tests seen failing** (`d6835aa`). Over the stubs, `compileKotlinJvm` and `compileTestKotlinJvm` ran
  with 0 ` e: ` lines, and the 2 tests failed on `NotImplementedError: step 6: ProfileCache`, 26 of 26 profiles in the read
  test [C].
- **`RoutingContextReadTest`.** Each engine's own `ProfileCache.parseProfile` fills a `RoutingContext` from Forager's
  parameters, set by the oracle's `RoutingParamCollector` through the existing `ExpressionsEngines.foragerRoutingContext`.
  Every public instance field of the oracle's `RoutingContext` typed boolean, int, long, double, float, Integer or String
  (76 of them [C]) is compared by name.
  - **Result:** equal on all 20 profiles that use `StdModel` [C].
  - The 6 car profiles name `KinematicModel` or `KinematicNoCostModel`. Neither is in step 6's row, so the port throws
    `UnsupportedOperationException("unsupported: path model <name>")`, and the test asserts exactly 6 such refusals [C].
  - Forager routes with `hiking-mountain.brf` (two references at a0535ece) [R]. So the Kinematic models are off its path
    [I: the planner may want to rule this].
- **`ProfileCacheLockTest`.** Two threads start `parseProfile` together. An okio `ForwardingFileSystem` lingers 150 ms on
  each metadata read of the profile and records the most threads inside at once. With the lock it reads 1 [C].
- **Reverts** (`01-revert-predictions.txt` written before each run; `02-*`). The runner restores each file from a saved
  copy, the XML is deleted before every run, and the forward change was confirmed present afterwards [C]:
  - **A, lock removed:** `ProfileCacheLockTest` fails, "threads inside parseProfile at once expected:<1> but was:<2>"; the
    read test passes. As predicted [C].
  - **B, `maxSpeed`'s foot branch divided as float.** The first form did not compile (1 ` e: ` line, not read). The second
    form fails the read test on `hiking-mountain.brf` and `shortest.brf`, each with only `maxSpeed` differing (the oracle
    6/3.6 as a double, bits 0x3ffaaaaaaaaaaaaa; the port the float quotient widened, bits 0x3ffaaaaac0000000). As predicted [C]. That these two are the only foot profiles is [I].
- **Full `:brouter:jvmTest` before the push:** 123 tests, only `CollectOutreachersTest`'s 2 red, 0 ` e: ` [C].
  `compileTestKotlinJvm` was up to date from the identical previous compile; `jvmTest` executed and wrote all 54 XML
  files fresh [C].
- **Differences from Java, each named in the KDoc:**
  - `profileBaseDir` and `debugProfileCache` are not read (JVM system properties; Forager sets neither [I]);
  - `lastUseTime` is a monotonic mark, so least-recently-used ties within one millisecond differ from Java [I];
  - `setAlternativeIdx` is the property's setter (a JVM signature clash otherwise);
  - Java's `String.hashCode` is written out (`javaHashCode`), because Kotlin/Native does not promise Java's value.

## 5. CI on `de4d994`

Not yet read when this report was written. Run id and result: see the handoff's first item.

## 6. Handoff (for session 11)

1. **Read CI on `de4d994`** (or the latest push), especially `compileKotlinIosArm64` and `compileKotlinIosSimulatorArm64`
   with `ProfileCache` now using `SynchronizedObject`. That is atomicfu's real klib check (-91). Report jvm 123 with only
   `CollectOutreachersTest`'s 2 red, simulator-checked 24, and arm64 7 with its gate.
2. **Next pieces, in order:**
   - (a) `OsmNodeNamed`, `OsmNogoPolygon`, `AreaInfo`, then `RoutingContext`'s nogo and distance half (`:285-554`, into
     `RoutingContextNogo.kt`, with its private lists made `internal`);
   - (b) `RoutingParamCollector` on Forager's strings and generated ones;
   - (c) `MessageData`, `OsmPathElement`, then `OsmPath` and `StdPath`, compared on decoded links with the oracle's costs.
     Two debts land there: -62's `interpolateEle` `short` cast on F0h's cut ends, with a revert that bites, and -42's way
     getters. Then the step's revert: one `(int)` cast moved inside a sum in `StdPath`, its prediction pushed first;
   - (d) `SearchBoundary`.
3. **For step 8 (-94):** check with the coverage reader that `OsmPath.java:350`'s `containsNode` true branch is not
   covered on R's R6 runs.
4. **Tools:**
   - scratch `revert.sh` (copy-restore, compile-error count, cmp);
   - `03-scan.sh` here: run as `n=$(scan.sh origin/brouter-port-5b HEAD); [ "$n" = 0 ] && git push`. It needs
     `forager-ends.txt` beside it, rebuilt from Forager's routes.json (both) and points.json at a0535ece.

**Commands** (clone `/home/user/forager-ios-5a`; `S` is session 10's scratch,
`/tmp/claude-0/-home-user-Forager-iOS/3a3820fc-8d2c-5da2-aa77-f5956bee2927/scratchpad/plumb/6/`):
- CI: `gh run list --branch brouter-port-5b --limit 3`, then
  `gh run view --job <id> --log > $S/ci.log; grep -E 'tests completed|Task :brouter:compileKotlinIos|BUILD|TOTAL|OK: all' $S/ci.log`
  and `grep -c ' e: ' $S/ci.log`.
- The piece's tests: `./gradlew --max-workers=2 :brouter:jvmTest --tests 'com.zynergylabs.forager.brouter.router.*' --console=plain > $S/r.log 2>&1`,
  then `python3 -I tools/junit-counts.py brouter/build/test-results/jvmTest`.
- The full run: `./gradlew --max-workers=2 :brouter:jvmTest --continue --console=plain > $S/full.log 2>&1`. Expect 123
  tests with only `CollectOutreachersTest`'s 2 red.
- A revert: `$S/rev/revert.sh <name> <file> '<old>' '<new>' '<test filter>'`, its prediction written to
  `$S/rev/predictions.txt` first.
- The gated push: `n=$($S/scan.sh origin/brouter-port-5b HEAD); [ "$n" = 0 ] && git push origin brouter-port-5b`. A copy
  of `scan.sh` is `03-scan.sh` here; `$S/forager-ends.txt` (63 values) is rebuilt by
  `git -C /home/user/slayer8366/forager show a0535ece:<file>` for
  `app/src/test/resources/routing/dropoff/points.json`, `server/pmtiles-worker/test/fixtures/rd5/routes.json` and
  `server/pmtiles-worker/test/fixtures/rd5/two-squares/routes.json`, then
  `grep -oE '\-?[0-9]{1,3}\.[0-9]{5,}' | sort -u`.
- **A scan lesson:** evidence files that print doubles (for example a field-compare message) trip the scan. Write such
  values as IEEE-754 bits, with a note, as `02-revert-B2-maxspeed-result.txt` does.

## 7. Unverified

- That atomicfu's classes link into both iOS klibs: pending CI on `de4d994` [?].
- That Forager never sets `profileBaseDir`, and never uses a Kinematic profile on its path [I].
- That `refTrack` is always null on Forager's path (-94) [I, to be checked at step 8].

## 8. Session 11 (Plumb Ledger): piece 2, and the handoff for session 12

**Done** (`55ea56e` and this commit):
- `OsmNodeNamed` (fields and both constructors, as T2 keeps), `OsmNogoPolygon` and `AreaInfo` (types and fields; T2
  keeps none of their methods). The methods the nogo half would call for a nogo polygon or a weighted nogo circle are
  a named "unsupported" (`OsmNogoPolygon.intersects`, `isOnPolyline`, `isWithin`, `distanceWithinPolygon`;
  `OsmNodeNamed.distanceWithinRadius`, `decodeNogo`). Not ported: `addVertex`, `calcBoundingCircle`, `isOnLine`,
  `OsmNodeNamed.toString`, `AreaInfo.checkAreaInfo` and its getters (it needs `createNewLookupData`, not ported).
- `RoutingContext`'s nogo and distance half, as extensions in `RoutingContextNogo.kt` (T2's nine kept methods;
  `prepareNogoPoints` and `allInOneNogo` are not ported). `nogopoints_all`, `keepnogopoints` and `pendingEndpoint` are
  `internal`. `libm` is now a constructor `val`.
- `RoutingContextNogoTest`, 4 tests: `calcDistance` through `setWaypoint`/`checkPendingEndpoint`/`unsetWaypoint` (3,000
  cases), `cleanNogoList`/`restoreNogoList` (2,000), `checkMatchedWaypointAgainstNogos` with its two refusals (3,000), and
  the unsupported nogos (2,000). The port runs on `OracleLibm`. Seen failing over `TODO` stubs (4 of 4,
  `NotImplementedError`). On `MsunLibm` the first run differed at the last bits of one radius: that is the scale table, not
  the logic, so the test uses the oracle's table, as `WaypointMatchEngines` does.
- **Revert C** (`04-revert-C-wayfraction-result.txt`): `wayfraction = 1.0 - wayfraction` became a no-op. Predicted: only
  the `calcDistance` test fails, on `wf=`. Observed: exactly that (1 of 4, `wf=` bits differ). Compile errors 0, the XML
  was fresh, and the file was restored (cmp equal) by the new copy-restore runner.
- **Full run:** 127 tests, only `CollectOutreachersTest`'s 2 red.
- **Rulings received (planner, RECORD -97):** `StdPath` uses `kotlin.math.exp`. Forager never reads time or energy. If it
  ever does (an ETA, say), exp must move behind `LibmSeam` as an msun port, as -24 did for cos and atan2. Native exp is
  [?]. `OsmPath`'s start-direction branch (`OsmPath.java:282-283`, `Math.sin/cos`) is a named "unsupported", with a test
  showing it. For step 8: `:282-283` should read not covered on R's R6 runs, as for `:350`'s `containsNode` (-94).

**Next, for session 12** (in this order):
1. `MessageData` (kept: `copy`, `getPrio`, the four `is*` masks and `toString`; `toString` formats a float, native [?]) and
   `OsmPathElement` (all but the stream methods; `calcDistance` takes `libm`). Both were read; neither is written yet.
2. `OsmPath` and `StdPath` (read in full this session). Java's casts to keep exactly:
   - `cost += rc.nogoCost` is `cost = (cost + nogoCost).toInt()`;
   - `ehbd += -delta_h_micros - dist * downhillcutoff` is a float sum, then `toInt()`;
   - `message.linkelevationcost += elevationCost` is an int plus a float, then `toInt()`;
   - `interpolateEle` is `(…).toInt().toShort()`;
   - `(lon0 + lat1) >> 1` is a bug in Java, kept as it is;
   - `static int seg` becomes a companion `var`;
   - Float against `0.` compares become `!= 0.0f`.
3. **The cost comparison: a finding.** The port's `NodesCache` has no graph half (`getGraphNode`, `obtainNonHollowNode`,
   `expandHollowLinkTargets`, `NodesCache.kt:14-16`), so `RoutingEngine.getStartPath` cannot be copied on the port yet.
   Proposed: decode F0h's cells on both engines with `GraphEngines.OracleDecoder`/`PortDecoder` (seeded). For each node,
   take each link to a non-hollow target and:
   - build the start path with `createPath(OsmLink(null, n1))`;
   - cut it with a waypoint at the link's midpoint (`setWaypoint(wp, false)`, then `createPath(start, link, null, detail)`,
     with detail true and false);
   - extend it one link further, plain and with an end waypoint (`setWaypoint(wp, true)`).
   Compare by reflection, by Java's field names: `cost`, `selev`, `bitfield`, `ehbd`/`ehbu`, the float bits, the message
   fields and the `originElement` chain. Do this on all 20 Std profiles, the port on `OracleLibm`. These cut points are
   link midpoints on F0h, not Forager's 16 ends; say so, or match the ends once the graph half exists. The planner should
   rule on which.
4. **Reverts.**
   - `interpolateEle`'s cast: drop `.toShort()` truncation by rounding instead. Predict the cut-path `selev`/`originElement`
     mismatches in detail mode only.
   - `StdPath`'s `(int)` cast moved inside a sum: **`hiking-mountain` has `turncost 0`**, so a turncost revert
     (`((1-cos)*base).toInt() + 0.2.toInt()`) bites only on profiles with turncost > 0. Predict which profiles from their
     `turncost`, and push the prediction first.
5. -42's way getters: `OsmPath` and `StdPath` call 19 of the 20 (not `getTrafficSourceDensity`) and the node's
   `getInitialcost`. Step 3's comparison exercises them [I until it runs].
6. Then `RoutingParamCollector` (kept: `getWayPointList`, both `readPosition`, `setParams`) and `SearchBoundary` (T2 keeps
   no method: type only).

**Tools:** `$S/rev/revert.sh` was missing after the restart, so it was rewritten. It copy-restores, refuses on any `^e: `
line or stale XML, and checks with cmp. Scratch is `$S/s11/`.

**Unverified:** native `kotlin.math.exp` and float `toString` [?]; that `startDirectionValid` is false on Forager's path
[I, step 8]; that the nogo half's msun-table results differ from the oracle only through the scale table [I].

## 9. Session 12 (Plumb Ledger): `NodesCache`'s graph half (5b's debt, RECORD -99)

**Ported:** `getGraphNode`, `obtainNonHollowNode`, `expandHollowLinkTargets`, `hasHollowLinkTargets` and `getStartNode`
(`NodesCache.java:213-300`), in `NodesCache.kt`, whose header now says so. `obtainNonHollowNode` passes `expCtxWay!!` to
`parseNodeBody`; the constructor has already refused a null context, as Java's does, so it cannot fire.

**`GraphHalfTest`** (one test): F0's two ends, F0h's eight, and the first 20 of R's on `hiking-mountain` (its own seed,
`E3RTest.SEED`). For each, both engines match the two ends, make a second cache over the first (as `resetCache`), and run
`_findTrack`'s graph steps (`RoutingEngine.java:1911-1928`): the four graph nodes, both obtains and expands, then a
search-like walk of up to 300 nodes that obtains each hollow target (`:2152`) and asks `hasHollowLinkTargets` (`:2006`).
Every result is written, then every node reachable from the four in `GraphTranscript`'s form (fields, links, geometry), and
the two texts are compared line by line. Cleanup modes 1 and 2, and mode 1 again with a 64 KiB `maxmem`, which turns on the
collection branch (`NodesCache.java:229-231`). `getStartNode` runs on a fresh cache, on the start's id and on the id
beside it (its null return).
- Over `TODO` stubs: fails, `NotImplementedError: ... graph half` (fresh XML, 0 compile errors).
- On the port: passes. 30 cases, 90 runs, 1,378,140 lines equal; `getStartNode` null 90 times, `obtain` false 21 times;
  digest `9b79c9aa0668b0e2…`.
- `E3F0hTest`'s companion is now `internal`, so the test reads Forager's ends from it and carries none of its own.

**Revert D, predicted before it ran** (`obtainNonHollowNode`'s direct-weaving return, `return true // direct weaving...`
made `return false`): `GraphHalfTest` fails, and only it among the filter's classes. The first differing line is an
`obtain` line, oracle `true`, port `false`, in the first case, `F0-inside-across-the-degree-line`, mode 1. Nothing before
it differs: the node is woven all the same, so the graph and every `hasHollow` stay equal; only the returned value changes.

**Revert D, observed** (`rev/D-directweave-*`): 0 compile errors, 1 fresh XML, file restored (cmp equal; `git diff` against
`bcba78d` empty after). `GraphHalfTest` failed at `F0-inside-across-the-degree-line mode 1 ...: line 3`, an `obtain` line,
oracle `true`, port `false`: as predicted. The texts' lengths differ too (6,915 lines against 5,391): `getStartNode`
returns null when its obtain does, so its reachable nodes are not written. That follows from the same edit; the prediction
spoke only of the first differing line.

**`MessageData` and `OsmPathElement`** (`router/`). `MessageData`: every field, `copy` (Java's shallow `clone`), `getPrio`,
`isBadOneway`, `isRoundabout`, `isLinktType`, `isGoodForCars` and `toString` (T2's list); `toMessage`, `add` and
`isGoodOneway` are not ported (unreached). `OsmPathElement`: T2's 16 methods; `setAngle` and the stream methods are not
ported. `calcDistance` takes `libm`. `OsmPath`'s stub gains `cost`, `originElement` and `message`, which
`OsmPathElement.create(path)` reads; `create(path)` itself waits for `OsmPath` to be tested.
- **`PathElementTest`**, 2 tests: `MessageData` against the oracle's by reflection over Java's 22 field names (defaults,
  then 1,999 seeded fillings, each before and after `copy`), with `toString`, `getPrio` and the masks; `OsmPathElement` on
  3,000 seeded points (getters, `getElev`, `getIdFromPos`, `toString`, `calcDistance` near and far on `OracleLibm`,
  `positionEquals`, time and energy with and without a message). Passes. It was written over the code, not over stubs, so
  reverts E and F stand for the failing run.
- **Revert E, predicted:** `copy` drops `extraTime` (`d.extraTime = extraTime` removed). Only
  `messageDataMatchesTheOracle` fails, at `case 1: copy's extraTime` (case 0 is the defaults, where both are 0).
- **Revert F, predicted:** `calcDistance`'s `max(1.0, ...)` made `max(0.0, ...)`. Only `osmPathElementMatchesTheOracle`
  fails, on `calcDistance`, at an even case (a near point, `dx` from -3 to 3, where a distance rounds to 0).
- **Revert E, observed** (`rev/E-copy-extratime-*`): 0 compile errors, 1 fresh XML, restored (cmp equal). 1 of 2 failed,
  `messageDataMatchesTheOracle` at `case 1: copy's extraTime`, port 0: as predicted.
- **Revert F, observed** (`rev/F-caldist-max-*`): 0 compile errors, 1 fresh XML, restored (cmp equal). 1 of 2 failed,
  `osmPathElementMatchesTheOracle` at `case 0: calcDistance`, oracle 1, port 0: as predicted. `git status` was clean after
  both.

**Full runs:** on `bcba78d`, 128 tests; on `f848da4`, 130; both fresh (`--rerun-tasks`), 0 ` e: ` lines, only
`CollectOutreachersTest`'s 2 red. The gated E1 scan read 0 before each push.

**Next, for session 13** (in this order; -99's order, with piece 1 done):
1. **`OsmPath` and `StdPath`**, with -97's rulings (`kotlin.math.exp`; the start-direction branch, `OsmPath.java:282-283`,
   a named "unsupported" with a test) and §8's list of casts. `OsmPath`'s stub already holds `cost`, `originElement` and
   `message`; the rest of its fields go beside them. `OsmPathElement.create(path)` is first reached there and is
   compared with it.
2. **The path-cost comparison from Forager's real ends:** `RoutingEngine.getStartPath`'s steps (`RoutingEngine.java:
   1818-1870`) copied into test code over the graph half. `GraphHalfTest`'s `OracleSide`/`PortSide` already make the
   two caches and the four graph nodes as `_findTrack` does (`:1911-1928`), so the comparison can start from them. F0's
   and F0h's ends (from `E3F0hTest.ENDS`, now `internal`), and a fixed sample of R's ends on the 20 Std profiles (seeds
   in `E3RProfilesTest.SEEDS`). The link-midpoint comparison may be added beside it.
3. **Reverts:** `StdPath`'s `(int)` cast, on the profiles whose `turncost` is above 0 (read from the 20 profiles and named
   in the prediction before it runs); -62's `interpolateEle` `short` cast on F0h's cut ends.
4. Then `RoutingParamCollector` and `SearchBoundary`.

**Unverified:** that the 64 KiB run reaches the collection branch of `obtainNonHollowNode` (`cacheSum` is private on both
engines and not read) [I]; that `obtainNonHollowNode`'s `getAndClear`/`parseNodeBody` branch is reached at all under
direct weaving [I, not counted]; native `Float.toString` in `MessageData.toString` [?]; the rest of §8's list.

## 10. Session 13 (Plumb Ledger): partial, handed off on the planner's call

**Stopped early:** the planner asked for a handoff (its watcher read 183,054). None of the dispatch's five steps is finished.
Nothing in `brouter/` changed; this commit adds only drafts and this section.

**Done:**
- Read: both CLAUDE.md files (Testing, Building, Known pitfalls in full), the coder profile, RECORD -90, -93, -96, -97,
  -99 and -101, and §8 and §9 here. The base was `a83ff3f`, matching `git ls-remote`.
- **A wrong premise:** the dispatch put the coder profile at `/home/user/forager-ios-5a/docs/process/coder-profile.md`. It
  is not on this branch. It is on `main` (`/home/user/Forager-iOS/docs/process/coder-profile.md`), and was read there.
- **The first full `jvmTest` at `a83ff3f` (the baseline) failed before any test ran.** Maven Central returned HTTP 429 (Too
  Many Requests) for okio-jvm 3.18.2, annotations 13.0 and atomicfu-jvm 0.33.0. That is infrastructure, not this
  branch's code. The retry got the dependencies and was still running tests at handoff, so its count is not read [?].
- **Drafted, never compiled and never run** (`2026-11-06-brouter-port-step6/s13-drafts/*.kt.txt`, kept out of the
  source sets so CI is not touched):
  - `OsmPath.kt` and `StdPath.kt`, full ports. §8's casts are kept. The ruling at -97 is applied: `kotlin.math.exp`, and
    the start-direction branch refuses as `unsupported: OsmPath's start direction (OsmPath.java:282-283)`. `AreaInfo`'s
    check also refuses. `seg` is a companion `var`.
  - `PathEngines.kt` (test helper): writes paths by reflection over Java's own field names; prepares the graph as
    `_findTrack` does, on both engines.
  - `StdPathTest.kt`, 3 tests: paths over decoded links on the 20 Std profiles, from F0's and F0h's ends;
    `startDirectionIsUnsupported`; and `interpolateEleMatchesTheOracle`.
  - `StartPathTest.kt`: step 3's comparison. `getStartPath` is copied in test code, then extended once with the end
    waypoint on the final link. It runs F0's and F0h's ends plus 5 of R's per Std profile, in plain and detail mode.
  - **None of these has been seen failing over stubs (-101).** That run is still owed. The stub needs
    `interpolateEle` (a `TODO`) added first, so that the tests compile.
- **Reverts D, E and F were not re-run.** Their result files are still missing.

**Next, for session 14** (in this order):
1. Run the baseline `jvmTest --rerun-tasks` at the head and read its count. If 429s recur, report them; do not
   work around them.
2. Reverts D, E and F, re-run with `revert.sh` and §9's edits, then committed. Redact 9-digit positions in the failure
   files and run the E1 scan.
3. Copy `PathEngines`, `StdPathTest` and `StartPathTest` into `brouter/src/jvmTest/.../router/`, and add the stub's
   `interpolateEle`. See all of them fail over the stubs: fresh XML, 0 ` e: `, `NotImplementedError`.
4. Then put the drafts of `OsmPath.kt` and `StdPath.kt` in place. Compile, check, and run both tests passing. Read the
   draft first, line by line, against Java; it has not been compiled.
5. The `(int)`-cast revert (the profiles with `turncost` above 0, read from the brf files and named in the prediction)
   and -62's `interpolateEle` revert, each with its prediction pushed before it runs.
6. `RoutingParamCollector` and `SearchBoundary`.

**Unverified:** the whole draft [?]; the baseline count at `a83ff3f` in this container [?]; CI was not read for this
docs-only commit.

## 11. Session 14 (Plumb Ledger): reverts D to F committed; `OsmPath` and `StdPath` ported; revert G

**Base:** `4815a6f`, matching `git ls-remote`. The baseline (130 tests, 2 failed, both `CollectOutreachersTest`, -61;
0 ` e: `) was run at `a83ff3f` before this session and read by the planner; it was not re-run. All result files below
are in `2026-11-06-brouter-port-step6/`, 9-digit numbers redacted, and the gated E1 scan read 0 before each push.

**Reverts D, E and F, re-run at `4815a6f`** (RECORD -101), with §9's exact edits through `revert.sh` (run from a copy in
scratch, so nothing was written under `main`'s checkout). Predictions pushed first (`05-revert-DEF-rerun-predictions.txt`,
`7928c2a`); results in `05-revert-*-rerun-result.txt` (`ecf4506`). Each: 0 compile errors, 1 fresh XML, restored (cmp
equal), and each matched both its prediction and session 12's observation:
- D: `GraphHalfTest` failed at line 3, an `obtain` line, oracle `true`, port `false`, in
  `F0-inside-across-the-degree-line` mode 1 (lengths 6,915/5,391, as §9).
- E: 1 of 2, `messageDataMatchesTheOracle` at `case 1: copy's extraTime`, port 0.
- F: 1 of 2, `osmPathElementMatchesTheOracle` at `case 0: calcDistance`, oracle 1, port 0.

**Tests over the stubs** (`128dffd`, red on purpose, said in its message; `06-stubs-failing-StdPath-StartPath.txt`).
`PathEngines`, `StdPathTest` and `StartPathTest` went into `jvmTest` unchanged from session 13's drafts; `OsmPath`'s stub
gained `interpolateEle` as a `TODO`. That was all they needed to compile. Run on the two classes: 0 ` e: `, 2 fresh XML,
4 of 4 failed, each `NotImplementedError: ... step 6: OsmPath`.

**The port** (`bce3a3a`). The drafts were read against `OsmPath.java` and `StdPath.java` method by method (fields, both
`init`s, `addAddionalPenalty` in full, `interpolateEle`, the getters; `StdPath`'s `init`, `resetState`,
`processWaySection` with both crossing blocks, `processTargetNode`, `elevationCorrection`, `definitlyWorseThan`,
`calcIncline`, `computeKinematic`, `solveCubic`). No logic difference was found; each of §8's casts is kept. Changes made:
- `rc.anglemeter.getCosAngle()` became `rc.anglemeter.cosAngle` (the port's property): a compile error.
- `@JvmField` on `sourceNode`, `targetNode` and `link`: their getters clashed with Java's `getSourceNode()` and the rest
  on the JVM. The field names that `PathEngines` reads by reflection are unchanged.
- Java line citations corrected: `AreaInfo` refusal `OsmPath.java:145-146`; crossing blocks `StdPath.java:98-116` and
  `:121-139`; `interpolateEle` `OsmPath.java:388-393`; `StartPathTest`'s start cut `:245-261` and end cut `:242-244`.
  The start-direction citation `:282-283` was right.
- Filtered run (`StdPathTest`, `StartPathTest`, `GraphHalfTest`, `PathElementTest`): 0 ` e: `, 4 fresh XML, 7 of 7 pass.
  `StdPathTest`: 20 profiles, 10 cases, 105,401 paths (104 refused), 1,358,473 origin links. `StartPathTest`: 20
  profiles, 600 runs, 1,024 start paths, 500 cut starts in detail mode, but only **6 final links and 4 same-segment
  runs**, so the end cut and the pending endpoint are thinly covered [I].
- **Full fresh `:brouter:jvmTest --rerun-tasks`:** 0 ` e: `, no 429s; Gradle: 134 tests completed, 2 failed, both
  `CollectOutreachersTest` (-61); 132 PASSED lines. **A discrepancy:** 10 of the 59 fresh XML files were empty (0 bytes),
  among them `StdPathTest`'s and `StartPathTest`'s. The 49 others read 110 tests, 2 failures; the 10 classes have 24
  PASSED lines in the Gradle log, and 110 + 24 = 134. Cause not found [?]; disk was not full. The 7 path tests' pass
  is from the filtered run's XML, read before the full run (`08-port-passing-and-full-run.txt`).

**Revert G** (StdPath's `(int)` cast moved inside the sum; §8 item 4). Prediction pushed in `bce3a3a` before the run
(`07-revert-G-turncost-int-prediction.txt`): turncost read from the 20 brf files, 0 on hiking-mountain, rail, river and
shortest, above 0 on the other 16; the failure would name one of the 16, expected fastbike-asia-pacific first, 2 of 4
failing. **Observed** (`07-revert-G-turncost-int-result.txt`): 0 compile errors, fresh XML, restored (cmp equal).
`StdPathTest` 1 of 3, `pathsMatchTheOracleOnEveryStdProfile` on `fastbike-asia-pacific.brf`,
`F0-inside-across-the-degree-line`, detail false, path line 2; `StartPathTest` 1 of 1, same profile and case, line 2. As
predicted. My filter `*Std*PathTest` missed `StartPathTest`, so it ran as a second revert with the same edit (G2). The
runner's 200-character message limit cuts off the port's side, so "port lower" was not read [?].

**Not done:** -62's `interpolateEle` revert (left for room); `RoutingParamCollector`, `SearchBoundary`.

**CI on `bce3a3a`** (run 42969696836, attempt 1, read with `gh`): `jvm` 134 tests completed, 2 failed, both
`CollectOutreachersTest` (-61); JUnit XML `TOTAL: tests 134, failures 2` in 59 files (none empty there, unlike this
container); 0 ` e: ` lines; `compileKotlinIosArm64` and `compileKotlinIosSimulatorArm64` both ran. `ios-smoke` passed:
simulator 24 of 24 (**simulator-checked**), arm64 7 of 7, with its gate step. So `@JvmField` in common code compiles
for iOS.

**Unverified:** the 10 empty XML files [?]; the port's side of revert G's lines [?]; the end-cut coverage of
`StartPathTest` (6 final links) [I]; the container's empty XML files did not occur in CI.

**Next, for session 15** (in this order):
1. Find why the full run left 10 empty XML files (re-run once and compare; check whether anything else touched
   `test-results` during it).
2. -62's `interpolateEle` revert (`.toInt().toShort()` with the truncation changed), its prediction pushed first: detail
   mode only, `StartPathTest`'s cut starts, and `StdPathTest`'s `interpolateEleMatchesTheOracle`.
3. Widen `StartPathTest`'s end-cut sample past 6 final links, or say why it cannot be.
4. `RoutingParamCollector` and `SearchBoundary`, each test seen failing over stubs first.

## 12. Session 15 (Plumb Ledger): the empty XML, revert G's port value, reverts H and I, the end cut widened

**Base:** `7282c67`, matching `git ls-remote`. Result files are in `2026-11-06-brouter-port-step6/`, 9-digit numbers
redacted; the E1 scan read 0 on the staged diff before each push. `revert.sh` was run unchanged from a copy in scratch;
for each revert below the whole failure message was read from the fresh XML (ElementTree), not the runner's 200-character
cut, after the runner's own refusal checks had passed.

**1. The empty XML files** (`10-full-run-7282c67.txt`). One full fresh `:brouter:jvmTest --rerun-tasks`, nothing else
running: 0 ` e: `, 0 HTTP 429; Gradle "134 tests completed, 1 failed"; 133 PASSED lines; 59 XML files, **0 empty**, TOTAL
134 tests, 1 failure. Counts agree three ways; session 14's 10 empty files did not recur. Data: a 15 s sampler of the XML
directory showed the 59 files' mtimes all within 5 ms at the task's end, so Gradle writes them together at the end; a
second process overlapping that moment could leave files empty [I, a hypothesis, not observed]. Nothing in CI or the build
was changed. The second full run (below) also had 0 empty.

**A test outside the task changed outcome with no code change:** in that baseline,
`CollectOutreachersTest.collectOutreachersWithoutADestination` **passed** (it was red in session 14's run and in CI on
`bce3a3a`; `7282c67` differs from `bce3a3a` in docs only). In the second full run it was red again. RECORD's Question A
already records this test's outcome varying run to run at the default stack (the port's stack overflow, JIT timing [I]).
Reported, not touched. It is a newly passing result, not a newly failing one, so not a stop.

**2. Revert G's port value** (`11-revert-G3-port-value-result.txt`). The same edit, filter `*St*PathTest` (both classes in
one run): 0 compile errors, 2 fresh XML, restored cmp equal, 2 of 4 failed as before. Full messages: `StdPathTest` oracle
`cost=26923`, port `cost=26914`; `StartPathTest` oracle `cost=2847`, port `cost=2842`; cost is the only token that differs
in each. **Port lower: confirmed.** By 9 and 5, not 1, since a link sums several sections' turn costs [I: not counted].

**3. -62's `interpolateEle` revert, H** (prediction `09-...-prediction.txt`, pushed in `4ffae38` before the run; result
`09-...-result.txt`). Edit: `+ 0.5` before `.toInt().toShort()`. 0 compile errors, 2 fresh XML, restored. 2 of 4 failed:
- `interpolateEleMatchesTheOracle` at `case 2: interpolateEle(-26070, -484, 0.221) expected:<-20415> but was:<-20414>`:
  early, port one higher, as predicted.
- `StartPathTest` on fastbike-asia-pacific, detail true, a start line, originElement height oracle 102 / port 103, with
  ehbu, time and energy moved, cost not: as predicted, **except the case**: it was `R-fastbike-asia-pacific-002`, not an
  F0h case as predicted. All 8 F0h cases passed under the revert; why their cut starts do not bite is unknown [?].
- `pathsMatchTheOracleOnEveryStdProfile` and `startDirectionIsUnsupported` passed, as predicted.

**4. `StartPathTest`'s end cut, widened** (`61a8e4f`). Each run now also extends every start path over each of its
target's links with a synthetic end waypoint at that link's node midpoint, on both sides, and compares the result
("cut" lines); a new assertion requires at least one hit. These ends are synthetic, not Forager's, and the KDoc says so.
Filtered run: 0 ` e: `, 1 fresh XML, 1 of 1 pass; 600 runs, 1,024 start paths, 6 final links, 4 same-segment runs, 500
cut starts in detail mode, **668 of 1,868 synthetic end cuts hit** (was 6 final links). Full fresh run before the push:
0 ` e: `, 0 429s, 134 completed, 2 failed (both `CollectOutreachersTest`, -61), 59 XML, 0 empty.
**Revert I** (prediction pushed in `61a8e4f`, result `12-revert-I-endcut-ele-result.txt`): the end cut's `ele2 =
interpolateEle(...)` replaced by `ele2 = ele2`. 0 compile errors, 1 fresh XML, restored. Failed on a "cut" line,
fastbike-asia-pacific `F0h-inside-across-the-degree-line` detail false, line 15 of 19: ehbd 1930000/2680000, ehbu
3930000/3180000, cost equal. As predicted. Whether the test before the widening caught it was not checked [?].

**5. `RoutingParamCollector` and `SearchBoundary`: not started** (room). Neither has a port file in `commonMain` yet.
Java: `SearchBoundary.java` 87 lines (constructor, `getFileName`, `isInBoundary`, `getBoundaryDistance`; only a field
in `RoutingEngine.java:88`), `RoutingParamCollector.java` 374 lines.

**CI on `61a8e4f`** (run 42974370259, read with `gh`): `jvm` 134 tests completed, 2 failed, both `CollectOutreachersTest`
(-61); JUnit XML `TOTAL: tests 134, failures 2` in 59 files; 0 ` e: ` lines in either job; `compileKotlinIosArm64` and
`compileKotlinIosSimulatorArm64` both ran. `ios-smoke` passed: simulator 24 of 24 (**simulator-checked**), arm64 7 of 7,
the gate reading "OK: all 2 classes ran". The docs-only commits after it were not read.

**Unverified:** why F0h's cut starts did not bite revert H [?]; whether pre-widening `StartPathTest` caught revert I [?];
the empty-XML cause [I, hypothesis only]; revert G's per-section count [I].

**Next, for session 16** (in this order):
1. Read CI on this branch's head (docs only since `61a8e4f`).
2. `SearchBoundary` (87 lines): stub, its test seen failing over the stub (fresh XML, 0 ` e: `), then the port.
3. `RoutingParamCollector` (374 lines): the same.
4. Optional, if cheap: run revert I against `StartPathTest` as of `016327f` (before the widening) to show whether the
   widening was needed; look at why F0h's cut starts pass revert H.
5. Then the merge of `brouter-port` into 5b at the step boundary (RECORD -105).

## 13. Session 16 (Plumb Ledger): `SearchBoundary` and `RoutingParamCollector` ported; reverts J and K; the merge not pushed (handoff)

**Base:** `d2c6993`, matching `git ls-remote`. Result files `13-` to `18-` in `2026-11-06-brouter-port-step6/`; E1 scan 0 on
every staged diff; every trailer `Co-Authored-By: Claude <noreply@anthropic.com>`. Ended on the planner's handoff call.

**CI on `d2c6993`** (run 42975838550, read with `gh`): `jvm` 134 tests, 2 failed, both `CollectOutreachersTest` (-61); 0
` e: `; both `compileKotlinIos*` ran; `ios-smoke` simulator 24 of 24 (**simulator-checked**), arm64 7 of 7, the gate "OK".

**1. `SearchBoundary`, ported whole** (constructor, `getFileName`, `isInBoundary`, `getBoundaryDistance`; Forager does not
reach it, T2 lists it). Over the stub (`ca11871`, pushed red on purpose): 1 fresh XML, 0 ` e: `, 1 of 1 failed with
`NotImplementedError ... SearchBoundary.getFileName stub` (`13-`). Two test fixes after the port first ran, the port
unchanged: out-of-range latitudes throw in `CheapRuler` in both, so throws are compared by class, and by message only for
Java's own `IllegalArgumentException` (a JIT-compiled index error lost its message in the port [I]); the fixed test was
seen failing over the stub again (`14-`). Passing: 168,000 `isInBoundary` cases, both answers in each of the 3 modes, all 4
directions and the undefined one. Full runs: `ca11871` 135 / 3 failed (the stub and -61's 2); `7e822c4` 135 / 2 (-61's);
60 XML, 0 empty, 0 ` e: `, 0 429s.
**Revert J** (prediction `fa69d49`, result `ae2260f`): inner `maxlon` one degree west. 0 compile errors, 1 fresh XML, cmp
equal, 1 of 1 failed: `case 0 probe 0 direction 3: getBoundaryDistance expected:<133759> but was:<121599>`, one degree
(11 against 10 times 12,160 m). It was the prediction's second branch, the one called less likely.

**2. `RoutingParamCollector`, for Forager's calls.** Forager reaches the constructor, `getWayPointList` and `setParams`
(`BRouterTrailRouter.kt:143-160`). Ported: `getWayPointList` and both `readPosition`s whole; `setParams` for `profile`,
`alternativeidx` and `profile:` keys, with the keys Java ignores ignored. **Refused with "unsupported", before the context
is touched:** the 18 other keys Java acts on, `nogoLats` when not empty, and more than one `profile:` key (hash order into
`_parseFile`, [?] in the verify report). **Not ported, no caller in Forager:** `readPositions`, `getUrlParams`,
`setProfileParams`, `parseNogoPolygons`, `readPoisList`, `readNogoList`, `readNogos`, both `readNogo`s. Over the stubs
(`30293c8`, pushed red on purpose): 3 of 3 failed, each naming its own stub (`16-`). The port passed on its first run:
8 Forager-form cases and 4,008 seeded strings; 26 profiles times 2, with 76 context fields; 19 refused keys, each shown to
change the oracle's context (`17-`). Full runs: `30293c8` 138 / 5 (the 3 stubs and -61's 2); `6cd2ab9` 138 / 2 (-61's); 61
XML, 0 empty, 0 ` e: `, 0 429s.
**Revert K** (prediction `d42ff96`, result `12ae393`): `ilat` truncated, not rounded. 0 compile errors, 1 fresh XML, cmp
equal, 1 of 3 failed, `wayPointListMatchesTheOracle`, "Forager's form", the from node's `ilat` one lower; the setParams
tests passed. All as predicted except the case: F0h case 3 (`edge-north-a`), not the first. Cases 0 to 2 have
micro-degree fractions below 0.5, so truncating does not change them.

**3. The merge: made locally, NOT pushed (partial).** `git merge origin/brouter-port` (`611ba54`) was clean, with no conflicts:
5 commits, FK kind 5 on hiking-mountain plus its docs, 5 jvmTest files changed and no test class added. The E1 scan read 0
on the merge diff. The handoff call came during its full run, which was stopped before it finished, so it is not
evidence. The local merge commit `4f5492f` is left on the local `brouter-port-5b` only, and this section is pushed from
`12ae393`. `expected-1b.tsv` is unchanged by `brouter-port` since the merge base: that branch's report says FK5's rows
are read from kind 5's own table.

**CI on the code commits:** `7e822c4` (run 42979541777) `jvm` 135 / 2 (-61's), 0 ` e: `, both `compileKotlinIos*`;
simulator 24 of 24 (**simulator-checked**), arm64 7 of 7, gate "OK". `ca11871` 135 / 3 (the stub and -61's), ios-smoke the
same. `30293c8`, `6cd2ab9`, `d42ff96` and `12ae393` were not read (still running at handoff).

**Unverified:** native `toDouble`/`toInt` grammar on unusual text [?]; the port's lost exception message [I]; the effect
of `profile:` key order [?] (refused, so not reached).

**Step 6 against T2's port list:** every file is now ported or refused by name. **Step 6 is not complete** until the
boundary merge is pushed and checked.

**Next, for session 17** (in this order):
1. The boundary merge (-96): `git merge origin/brouter-port` into `brouter-port-5b` again (clean in this session), then a
   full fresh `:brouter:jvmTest --rerun-tasks`, push, and read CI. Before it, read CI for `6cd2ab9` (`jvm` 138 / 2
   expected).
2. Record which tests the merge changes (none added; `E3ExpectedFileTest`, `E3FKTest` and `E3ExpectedWriteTest` edited)
   and their counts, against the 138 here.
3. Optional (§12's step 4): revert I against the pre-widening `StartPathTest`, and F0h's cut starts under revert H.

## 14. Session 17 (Plumb Ledger): the boundary merge pushed and checked (RECORD -96)

**Base:** `origin/brouter-port-5b` at `028783c` and `origin/brouter-port` at `611ba54`, both matching `git ls-remote`.
Built on a fresh local branch `s17-merge`; session 16's local `brouter-port-5b` (`4f5492f`) and `s16-handoff` are untouched.
Result file `19-` in `2026-11-06-brouter-port-step6/`. Trailer `Co-Authored-By: Claude <noreply@anthropic.com>`.

**1. The merge.** `git merge --no-ff origin/brouter-port`: clean, no conflicts, commit `7491f3a`, pushed as a fast-forward
(`028783c..7491f3a`). 13 files, +304 / -14: FK kind 5 on hiking-mountain (`E3Expected.kt`, `FKGenerator.kt`,
`E3ExpectedFileTest`, and the strata tests `E3FKTest`, `E3ExpectedWriteTest`), the phase 2 report's section and step 1b's
`32-`/`33-` logs. No CI file and no `expected-1b.tsv` change (`git diff 028783c HEAD` on both: empty) [C].

**2. Against session 16's unpushed merge.** `git diff 4f5492f HEAD` is exactly §13's 60 lines and nothing else, and is
byte-identical to `git diff 12ae393 028783c` (§13's own commit; `4f5492f` was made on `12ae393`). So the merged code trees
match [C].

**3. Full fresh run** (`:brouter:jvmTest --rerun-tasks`, `19-`): Gradle "138 tests completed, 2 failed", both
`CollectOutreachersTest` (-61): `collectOutreachersWithoutADestination`, `collectOutreachersWithADestinationVanishesFarNodes`.
0 ` e: `, 0 429s; 61 XML, 61 non-empty, summing to 138 / 2 / 0 errors. Against §13's 138 / 2: difference 0. Of the
classes the merge changed, only `E3ExpectedFileTest` is in `jvmTest` (6 of 6 passed; 6 `@Test` before and after);
`E3FKTest` and `E3ExpectedWriteTest` are in the `e3.strata` package, which `jvmTest` excludes (`build.gradle.kts:151-156`),
so they were not run here; `brouter-port` ran them on its own branch.

**4. E1 scan** on `git diff 028783c HEAD`: 0. Told apart from `brouter-port`'s lines by diffing: the merge's diff against
`028783c` is byte-identical to `brouter-port`'s own diff from the merge base (`fcd35ff..611ba54`), which also scans 0. So
every added line came from `brouter-port`, and none of them reads [C].

**5. CI on `7491f3a`** (run 42984411711, read with the GitHub tools and `gh` logs): `jvm` TOTAL 138 tests, 2 failures,
0 errors, 61 files; failed both `CollectOutreachersTest` (-61), the job red for them alone; 0 ` e: ` in either log;
`compileKotlinIosArm64` and `compileKotlinIosSimulatorArm64` both ran. `ios-smoke`: simulator TOTAL 24, 0 failed
(**simulator-checked**); arm64 TOTAL 7, 0 failed; the gate "OK: all 2 classes ran". `iosArm64` is compiled, not linked [I].

**Unverified:** the strata classes on this merge (not in `jvmTest`, not run here; `e3Strata` is the expensive task and was
not dispatched); CI for `6cd2ab9` (§13's step 1) was not read, superseded by `7491f3a`'s 138 / 2; §13's carried items
(native number grammar [?], the lost exception message [I], `profile:` key order [?]); the optional revert I / revert H work
(§12 step 4) not done.

**Step 6 against T2's port list.** §3's step 6 classes (the 15 `btools.router` files and `OsmTrack`'s type) all exist in
`commonMain/.../router/`, each with a row in T2's `port-list.tsv` (`2026-10-28-brouter-port-verify/03-reach/out/T2/`),
and none holds a stub but `OsmTrack.containsNode`, which is step 7's by -94 [C]. T2 lists 17 more `btools.router` classes
(the `Kinematic*`, `Format*`, `VoiceHint*`, `RoutingEngine`, `RoutingHelper`, `AreaReader`, `SuspectInfo`) that §3 does not
put in step 6 and that have no file yet [C]; they are taken as later steps' [I]. With the boundary merge pushed and its CI
read as expected, **step 6 is complete** against its list, subject to the planner's reading.
