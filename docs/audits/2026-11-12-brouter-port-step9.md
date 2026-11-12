# BRouter port, step 9: the groups through the port

Plumb Ledger, session 39, dispatched by Cedar Datum: step 8d's two leftovers, then step 9's first groups. Branch
`brouter-port-5b`, from `e1940ed`.

Tags: **[R]** read in the source at the file and line named; **[C]** counted or shown by a tool's output, named;
**[I]** inferred, not shown. Java is BRouter 1.7.10 as vendored in Forager (`third_party/brouter/src/main/java/btools/`).

## 1. Session 39

### 1.1 The constructor's exception through `route()` (step 8d's leftover)

Test `RouteTest.aMissingProfileThrowsThroughRouteAsTheOracle`, committed before any run (`8aef87e`; the commit names what
is compared).

Forward run [C: `RouteTest`'s XML, single class]: 7 tests, 0 failures, 0 errors. Printed: oracle
`java.lang.IllegalArgumentException "profile * does not exist"`; port `java.lang.IllegalArgumentException "profile * does
not exist"`. Same kind, same message.

Revert, PREDICTION (pushed before the run): `Route.kt`'s engine construction wrapped so that an exception from the
constructor returns `RouteOutcome.Failed(message)`. Expected: `RouteTest` 7 tests, exactly 1 failure,
`aMissingProfileThrowsThroughRouteAsTheOracle`, message beginning "the port's route() throws (it returned Failed)"; the
other 6 pass (none of them reaches a throwing constructor). Restored `cmp`-equal by a scratch copy of `revert.sh`.

Revert, OBSERVED [C: the scratch `revert.sh`'s output, `2026-11-12-brouter-port-step9/01-ctor-revert-out.txt`; the edit,
`01-ctor-revert.diff`]: compile errors 0, 1 fresh XML; `RouteTest` 7 tests, 1 failure,
`aMissingProfileThrowsThroughRouteAsTheOracle`, "the port's route() throws (it returned Failed)", as predicted. Restored:
`cmp` equal; `git status` clean after, the forward `Route.kt` unchanged (the revert's `catch` string: 0 occurrences).

E1: the commit `4b9409e` was pushed with the scan reading 2 (my order of commands committed on the scan instead of after reading it). Both hits were non-position: the two `diff -u` header timestamps (seconds to nine places) in `01-ctor-revert.diff`. Fixed in the next commit by dropping the header timestamps; no position or height was in either hit.

### 1.2 The `OsmPath` coverage, by a changed method

Counts in `2026-11-11-brouter-port-step8/18-coverage-osmpath.txt` (session 39's heading), commit `3f925e6`: one run of
`RoutingEngineRTest` with a scratch counter patch (never committed; both files restored `cmp`-equal). Java :280-283 not
reached (`startDirectionValid` true 0 times of 11540 start points); :350's `refTrack != null` true 0 times of 189134014,
so `containsNode` and the penalty not reached. No test added. The `:279` -> `:278` correction was NOT made: see §1.6, Q1.

### 1.3 Step 9's count

[C: one `awk` over `expected-1b.tsv`'s rows] 2,126 rows: R 1534, F0 2, DL-lat 59 + DL-lon 59, MP64 59, ML5 59, PM5 55,
FK 114 (FK1-6 10 each, FK7 13, FK8 11, FK9-11 10 each), NT 59, LB 59, CR 59, F0h 8. FK 1 to 8: 84. The planner's count is
right. [C: `git grep -l '"G"\|GGenerator\|E3GTest'` over `brouter/src/jvmTest/.../router` and `RouteTest.kt`, per group]
NT, LB, CR: no hit. FK: `RoutingEngineStopTest` only (FK9-11). F0h: `RoutingParamCollectorTest`, `StartPathTest`,
`StdPathTest`, `TrackEngines`, which use F0h's ends for parts (matching, start paths, StdPath), not a route compared on
the six fields.

### 1.4 NT, LB, CR through the port

`RoutingEngineStep9Test` (new), as `RoutingEngineMemoryTest`: 1b's generator, seed and arguments from `E3NTTest`,
`E3LBTest`, `E3CRTest`; the draws whose ids are the stratum's rows, within `E3Targeted.CAP`; the oracle held to each row's
digest, then the six fields compared. CR's own profile (`trekking-crossing`) is written into one scratch copy of the
oracle's `profiles2/`, read by both engines; for that, `RoutingEngineF0Test.port` takes a `base` folder, default the
oracle's (no comparison changed).

First run [C: the class's XML, single class; 0 compile errors]: 3 tests, 0 failures; PASSED ON FIRST RUN, all three.
- NT: 59 cases within 89 draws; kinds route 56, "to-position not mapped" 1, "target island detected" 2; 0 mismatches.
- LB: 59 cases within 99 draws; route 59; 0 mismatches.
- CR: 59 cases within 83 draws; route 59; 0 mismatches.

Planted errors, PREDICTION (pushed before any of the three runs; each by the scratch `revert.sh`, filter one method):
- NT: `WaypointMatcherImpl.kt:39`, the tie-break reversed (`mw2.directionDiff, mw1.directionDiff`). NT's cases are
  chosen for two start links at one radius within 4 ulps, so the other link is matched first. Expected: `ntRoutes...`
  fails with mismatches, at least 1 and I guess most of the 59 (node digest at least); the oracle's digest asserts pass.
- LB: `CheapRuler.kt:53`, the row read one too high (`ilat / SCALE_CACHE_INCREMENT + 1`). Every distance in the run
  moves. Expected: `lbRoutes...` fails, mismatches on most of the 59 (cost at least).
- CR: `StdPath.kt:206`, the crossing class read one character early (`classIndex + 24, classIndex + 25`, the `=`), so
  no class matches and the added turn cost is 0. Expected: `crRoutes...` fails, mismatches on most of the 59 (cost at
  least; each case was taken for reaching `StdPath.java:105-113`).

Planted errors, OBSERVED [C: the scratch `revert.sh`'s outputs, `02-`, `03-`, `04-*-plant-out.txt`, edits in the `.diff`
beside them; each: 0 compile errors, 1 fresh XML, restored `cmp` equal; `git status` clean after all three]:
- NT: **NOT CAUGHT, against the prediction.** 1 test, 0 failures; the run printed 59 cases within 89 draws, 0 mismatches,
  so the reversed tie-break ran and moved no outcome. [I] `updateWayList` (`WaypointMatcherImpl.kt:232-237`) keeps up to
  5 matched entries and the sort decides only their order; that order does not reach the six fields on these 59 cases.
  So NT's test passes identically with and without that change: flagged, as Forager's Testing rules ask, as possibly not
  covering the near-tie it is named for. No other plant chosen (Q2).
- LB: caught as predicted: 1 failure, "52 mismatches" of 59 (first: nodes, cost, distance, both digests).
- CR: caught as predicted: 1 failure, "50 mismatches" of 59 (first: nodes, cost, distance, both digests).

### 1.5 CI reads

`8aef87e` (the constructor-exception test; run 43151665608) [C: `gh run view --log`, grepped]:
- jvm: TOTAL tests 182, failures 2, errors 0, skipped 0 in 82 files. FAILED lines: only
  `CollectOutreachersTest > collectOutreachersWithoutADestination` and `> collectOutreachersWithADestinationVanishesFarNodes`
  (the known two; not touched), and the task line. ` e: ` 0. The new test ran (its print in the log: both
  `IllegalArgumentException "profile * does not exist"`). JVM tests step 23.4 min (the previous run, `7ae5c31`, 23.9): not
  near 60. iOS klibs: success; `compileKotlinIosArm64` and `compileKotlinIosSimulatorArm64` both run.
- ios-smoke, simulator-checked: commonTest on the iOS simulator TOTAL 24, 0 failures; the arm64 JVM half TOTAL 7, 0
  failures; the gate ("Gating - the arm64 JVM step ran every class it names") success.
- The caught-overflow print (`OsmNodesMap.cleanupPeninsulas caught a StackOverflowError`, -61): 4 lines in the log.
`554887e` (RoutingEngineStep9Test): not finished when this was written; the next session reads it.

### 1.6 [I], [?], and questions for the planner

- [?] Q1, a false premise (a stop on that point): the prompt asked to correct file 18's `:279` to `:278`. At Forager
  `947f1be2`, `OsmPath.java:278` is `if (isStartpoint)` and `:279` is `if (rc.startDirectionValid)`; 18 names `:279` for
  startDirectionValid, which is right. Not changed. If the planner meant to add `:278` for `isStartpoint`, say so.
- [?] Q2: NT's planted error (the tie-break reversed) was not caught. Is NT's port test to be held to another plant (for
  example in the radius compare at `WaypointMatcherImpl.kt:37`, or `MAX_POINTS`), or is "near-ties do not reach the
  outcome on these 59" a finding for the record? Not chosen here.
- [I] The counters (§1.2) cover R's 1534 cases only, through the port; F0's two are not in that run.
- [C] "None" claims: NT, LB, CR had no port test before (the `git grep` in §1.3, no hit); the scratch counters are not in
  the tree (`git grep -c S39Counters`: no output).
- E1: one pushed commit (`4b9409e`) read 2 on the scan, both diff-header timestamps, fixed in `100d00d` (§1.1).
- Stops hit: Q1 only. No test outside the task newly failing (CI's two are the known `CollectOutreachersTest`).

### 1.7 Groups done and not done; Next

Done: NT, LB, CR (59 each, 0 mismatches; LB and CR plants caught, NT's not). Not done: FK 1 to 8 (84 rows), F0h (8).

Next:
1. Read CI on `554887e` and later pushes (TOTAL, FAILED, ` e: `, iOS compile, gate, jvm minutes: three more groups add
   about 1 min locally).
2. FK 1 to 8 through the port: `E3FKTest` builds kinds 1, 2, 6 in its test body (`Random(SEED + kind)`, `gen.draw`,
   ids `FK<k>-NN`), kinds 3 and 5 in private `kind3`/`kind5`, kind 4 by `E3Select` from `Random(SEED + 4)`, kinds 7 and 8
   on edited copies of E170_S50 under `E3Strata.work` (`edited(...)`), with E's own FK profiles (`E3Stability.ownProfiles`,
   `profileOf`). Kind 7's family reads numbers as `*`. Expect named "unsupported" or left-out guards on some kinds;
   record them, do not fix.
3. F0h's 8 cases (`E3F0hTest`, `TestTiles.f0h`).
4. Q1 and Q2 answered.
