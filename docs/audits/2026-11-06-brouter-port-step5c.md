# BRouter port, step 5c: waypoint matching, and the route-ways test (RECORD -66)

Plumb Ledger, sixth session, on `brouter-port-5b`, from `98c01e6`. The session handed off at the planner's message
(180k at 03:55Z), with the revert running. Tags: [R] read from source or output, [C] computed or run here,
[I] inferred, [?] unknown.

## Status
- **5c: ported and passing. The revert bites as predicted:** 2,105 ends differ, inside the predicted 1,364 to 2,718.
  The full suite was read inside the revert run.
- **Route-ways test: stopped before writing.** `BRouterRouteWays` calls code the port does not have. The planner was
  messaged about it, and its answer is pending.
- **CI:** the stub head's run (42158645249) was read. The head's run (42159504256) was in progress at handoff.

## 5c, waypoint matching
- **What was ported.** `MatchedWaypoint.kt` (its fields), `WaypointMatcherImpl.kt` (all of Java's class, in Java's
  order; 252 lines) and `NodesCache.matchWaypointsToNodes`/`preloadPosition` (`NodesCache.java:292-345`) [R].
  - The `NodesCache` methods had been left for 5c by its own header.
  - The seam goes first, as step 2b ruled (Phase 2 report, "Calls beyond the amendment").
  - Every `getDirection` (that is, `atan2`) and `getLonLatToMeterScales` goes through `LibmSeam`.
  - The test passes `OracleLibm`, which is 2b's logic-run seam. So this was a logic run, as (a) to (c) were settled
    in 2b. No `atan2` question came up [C].
  - `Double.compare` is written out (`javaDoubleCompare`), and the sort is `sortWith`, which is stable like
    `Collections.sort` [R].
- **Not ported:** `MatchedWaypoint.writeToStream`/`readFromStream`. They are on T0's reach list only; T1 and T2 have 0 of
  them (`03-reach/out/T*/reached-methods.tsv`) [R].
- **The test** is `WaypointMatchTest` with `WaypointMatchEngines` (jvmTest).
  - **The cases:** R's 26 profiles, each with its own seed (`E3RTest`, `E3RProfilesTest`, whose companions are now
    `internal`), 59 cases per profile.
  - **The ends:** both ends of each case, from the oracle's `RoutingParamCollector.getWayPointList`.
  - **The call:** `matchWaypointsToNodes(ends, 250, new OsmNodePairSet(500))` on both engines, as Forager's path calls
    it. That is one call, because Forager passes `use_dynamic_range=0` (`BRouterTrailRouter.kt:154` at `a0535ece`), and
    no profile sets `waypointCatchingRange` [R].
  - **What is compared, bit for bit for each end:** the result (or the exception), the crosspoint, `node1`/`node2`, the
    radius, `directionDiff`, `directionToNext`, the name, the type, and the whole `wayNearest` list.
  - **What it outputs:** counts and one SHA-256 only. A failure names case ids and field names, never a coordinate.
- **Over the stubs:** 1 test, 1 failure, the stub's `NotImplementedError`; 0 compile errors (`2026-11-06-brouter-port-step5c/1-stubs.txt`) [C].
- **Forward:** 1 of 1 passes; 0 compile errors (`2-forward.txt`) [C]. The numbers:
  - 1,534 cases and **3,068 ends, 0 differing**;
  - 350 ends unmatched, and those are compared too;
  - on 3 cases the oracle throws "datafile ... not found" from a neighbouring tile that is missing, and the port's
    message is equal;
  - the oracle chose the reverse entry at 1,364 ends, and 2,718 ends have a radius tie at the head;
  - the transcript's SHA-256 is `f26818e8…6907`.
- **NT's ends are not compared:** NT is not built yet. They are owed when it is.
- **The revert:** the comparator's second line becomes `0`. Its prediction was pushed first, in `51c43b3`
  (`3-revert-prediction.md`): `WaypointMatchTest` fails with 1,364 <= N <= 2,718 ends differing, and the only other
  failures are `CollectOutreachersTest`'s 2. **Observed** (`4-revert.txt`) [C]:
  - 0 compile errors;
  - 102 tests and 3 failures: `CollectOutreachersTest`'s 2, and `WaypointMatchTest` with "ends differing from the
    oracle: 2105", naming n1, n2, diff and name;
  - the file was restored from the saved copy, with the forward line present and `git status` clean.

  **The prediction held.** Its point estimate ("near the top") missed. Every other test in the suite passed with the
  revert in place, so nothing else is red [C].
- **Not covered:**
  - the island-pair branch of `start()`, because the set is always empty here;
  - the dynamic-range branch, which Forager's path cannot reach because of `use_dynamic_range=0` [R];
  - the `WAYPOINT_TYPE_DIRECT` branches.

## The route-ways test: stopped on a gap
I checked reachability first, and the port cannot run `BRouterRouteWays.read` [R].
- **The call:** `BRouterRouteWays.kt:84` calls `createMicroCache(..., null, null, true, null)` with `hollowNodes` null.
  Java then decodes through `new MicroCache2(bc, …)` (`OsmFile.java:140`; `MicroCache2.java:35`, about 215 lines).
- **What follows it:** `OsmNode(long)` (`OsmNode.java:57`), `parseNodeBody`, which goes on to `parseNodeBody2`
  (`:112`, `:117`), and `MicroCache.getIdForIndex` (`MicroCache.java:180`).
- **What the port does there:**
  - it throws "unsupported" at `OsmFile.kt:140-143`;
  - `MicroCache2.kt` is a declaration only;
  - `OsmNode.kt:15` lists `OsmNode(long)` and `parseNodeBody2` as not ported;
  - `getIdForIndex` is absent.
- **The wrong premise:** -64's "No new code to port" matched by class and method name, not by overload. The decoder's
  constructor is on T0 and T1 and is absent from T2. The option the owner chose ("since the decoding is already
  ported") rests on it too.

I sent this to the planner as -66's stop. I neither wrote the test nor ported the decoder. The points (the drop-off
fixture and F0h's ends) are not fetched or pinned yet.

## Wrong premises in -66 and -64
- -64: "No new code to port". About 300 Java lines are missing (above).
- -65/-66: the route-ways test can be "a modest test" placed next to `GraphDecodeTest`. It cannot, until the decoder
  above is ported.

## Calls beyond scope
- `E3RTest`/`E3RProfilesTest` companions made `internal` (one word each) so 5c reads R's seeds instead of copying them.
- Exceptions compared as outcomes on both engines.
- NodesCache's two methods ported in 5c (its header assigned them there).

## CI
- **Run 42158645249, on the stubs `f43ed49`:** `jvm` failure, 102 tests and 3 failed (`CollectOutreachersTest` 2,
  `WaypointMatchTest` 1, as intended), 0 ` e: ` lines. `ios-smoke` success (simulator-checked). I did not read the two
  `compileKotlinIos*` lines, the simulator count or the arm64 gate in that log [?].
- **Run 42159504256, on the head `3f691a7`:** in progress at handoff, not read [?].

## A defect found by the revert
`WaypointMatchEngines.differingFields` splits on spaces. So a field such as `n1=(lon, lat)` leaves a bare coordinate
token in the failure message. That is harmless while the test passes. But a red CI run would print coordinate
fragments of R's ends, which are brouter.de tile nodes, not Forager's own (E1). Fix it by keeping only the `key=`
names.

## Unverified
- CI on the head [?].
- The full forward suite was not run separately. The revert run covers every test except `WaypointMatchTest`'s
  forward, which passed on its own run [C].

## Handoff: what remains, in order (sixth session's)
Clone `/home/user/forager-ios-5a`, branch `brouter-port-5b`. First check that `origin/brouter-port-5b` is the commit that
added this report. Scratch: `/tmp/claude-0/-home-user-Forager-iOS/3a3820fc-8d2c-5da2-aa77-f5956bee2927/scratchpad/plumb/5c/`.

0. **Done (the revert finished and was read; see above).** For the record: the revert was It was started at about 03:40Z, as
   `bash docs/audits/2026-10-30-brouter-port-step5b/revert-runner.sh /home/user/forager-ios-5a <scratch>/5c/revert
   brouter/src/commonMain/kotlin/com/zynergylabs/forager/brouter/mapaccess/WaypointMatcherImpl.kt '        javaDoubleCompare(mw1.directionDiff, mw2.directionDiff)' '        0'`.
   At handoff the working tree held the reverted line, uncommitted. **Before anything else:**
   - run `git diff --stat`;
   - if `WaypointMatcherImpl.kt` differs, restore it from `<scratch>/5c/revert/WaypointMatcherImpl.kt.saved`, or if
     the scratch is gone, from `51c43b3`, which holds the forward;
   - confirm the forward line is present.
1. Fix `differingFields` (the defect above) and push it.
2. Optionally, run the full forward `./gradlew --max-workers=2 --offline :brouter:jvmTest --continue`. Expect red only on
   `CollectOutreachersTest` (2, RECORD -61).
3. Read CI on the head (`gh run list -R slayer8366/Forager-iOS -b brouter-port-5b`, then the job logs). Read the `jvm`
   count, the ` e: ` lines, both `compileKotlinIos*`, the simulator (simulator-checked), and arm64 with its gate.
4. The route-ways test waits on the planner's answer to the gap above.
