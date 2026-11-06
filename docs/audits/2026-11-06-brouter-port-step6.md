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
