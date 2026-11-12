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
