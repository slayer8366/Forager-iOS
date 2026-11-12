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

## 2. Session 40

Plumb Ledger, session 40, dispatched by Cedar Datum: one CI change, NT's second plant, FK 1 to 8 and F0h through the
port. Branch `brouter-port-5b`, from `1bf57ca`, fast-forward only.

### 2.1 CI stops running on docs-only pushes (`33de34b`)

`.github/workflows/brouter.yml`: the bare `push:` at line 7 [R] gains `paths-ignore:` with `'docs/**'`; `pull_request:`
unchanged; nothing else moved (`git diff --stat`: 2 insertions) [C]. From here, docs go up with the next code push.

### 2.2 NT's second plant: STOP, a false premise in -132

-132's reading says NT's cases are drawn for two start links whose *radii* differ by at most 4 ulps. They are not:
- `NTGenerator.kt:26-27` [R]: the start is exactly on the junction J, "every link at J has radius 0 ... so the comparator
  orders them by `directionDiff` alone"; `:93` rejects a draw whose crosspoint is not J, `:95` one whose two links' radii
  differ, `:97` one whose `directionDiff` gap is over 4 ulps. `E3NTTest.kt:43` marks a case only when
  `first.radius == second.radius`. The 4 ulps are between the two `directionDiff`s, not the radii.
- `docs/audits/2026-10-29-brouter-port-phase2/step1b/23-nt-select.tsv` [C: `grep -o 'gap [0-9]* ulps' | sort | uniq -c`]:
  the 59 taken rows read `gap 0 ulps`, all 59.

So the count asked for: on 59 of 59 NT cases the two nearest start links' radii are exactly equal (both 0, the start on
J), and on 59 of 59 their `directionDiff`s are exactly equal too [C, the select table, the oracle's own probe]. The
comparator returns 0 for that pair under the true order, under session 39's reversed tie-break and under the Float plant
alike, so the stable sort keeps insertion order: neither plant can reorder the near-tied pair [I, from the code].
The Float plant changes 0.0 to 0.0f and back, and can reorder only entries of nonzero radius, below the radius-0 ones;
`wayNearest[0]` is unchanged [I]. A run would pass whole by construction, and "NT passes whole" would then say nothing
about decision 2's question. Per the dispatch's stop on a false premise, **the plant was not run** and no order count
under it was made. For the planner: NT's near-tie is an exact tie of both keys in all 59 cases, so no comparator plant on
radius or `directionDiff` can reach it; what decides the pair is the order the links are added in (`end()`'s insertion
order, `WaypointMatcherImpl.kt:176-193`), which a plant there could test.

### 2.3 FK 1 to 8 through the port

`RoutingEngineFKTest` (new, `34277b6`, the comparison stated in its commit before any run): the cases built as
`E3FKTest` builds them (seeds `SEED + kind`, its caps; kinds 3 and 4 by 1b's draw ids; kinds 7 and 8 on edited copies of
E170_S50 in a scratch folder); per case the oracle held to its row (exact: the whole tuple's digest; outcome: the kind),
then port against oracle on the family (kind 7's numbers as `*`) and the five track fields.

First run [C: the class's XML, 0 compile errors, `10-fk-first-run.txt`]: 8 tests, 0 failures; PASSED ON FIRST RUN, all 8.
- FK1 10: "from-position not mapped" 10. FK2 10: "to-position not mapped" 10. FK5 10: "to-position not mapped" 10.
- FK3 10: "no track found at pass=0" 10. FK4 10: "target island detected for section 0" 10.
- FK6 10: "datafile W80_N40.rd5 not found" 10.
- FK7 13: "file of size * too short, should be *" 10, "to-position not mapped" 1 (x1), route 2 (x2, c0).
- FK8 11: "top index checksum error" 10, route 1 (x1).
- 84 cases, 0 mismatches. No refusal by name: no port outcome was an "unsupported", a "left out" or a throw (the port's
  kinds above are all the oracle's).

#### Plants, predictions (pushed before any plant run)

Each by a scratch copy of `revert.sh`, one exact string, the FK class alone, restored `cmp`-equal.
- **A, kinds 1, 2, 5** (position not mapped): `RoutingEngine.kt:475` `val range = routingContext.waypointCatchingRange`
  made `* 4` (250 m to 1,000 m). Predicted: FK1, FK2, FK5 fail on the cases with a usable link between 300 m and the cells
  the matcher preloads (about 1 km); the count in each is a guess (I would guess most of FK5, fewer of FK1 and FK2, whose
  points are in water with no *node* within 300 m). The failing cases' port family becomes a route or another failure.
  FK3, FK4, FK6, FK7, FK8 pass: the nearest link of a mapped point is unchanged, and the file errors come first [I].
- **B, kinds 3, 4** (islands): `:291` `if (seg == null && nodeLimit > 0)` made `nodeLimit < 0` (no island is ever
  detected). Predicted: FK4 10 of 10 fail on family (the port's family then most likely "no track found at pass=0", a
  guess); FK3 passes (its island is the start, the inverse search from the mainland target uses up the node limit and
  detects nothing in either build) [I]; every other kind passes.
- **C, kind 6** (missing file): `NodesCache.kt:369` `first_file_access_failed = osmf.filename == null` made `= false`.
  Predicted: FK6 10 of 10 fail on family, the port reading "*-position not mapped" (which side is a guess); others pass.
- **D, kind 7** (too short): `PhysicalFile.kt:56` `if (len < pos + extraLen)` made `if (len < pos)`. Predicted: FK7-08,
  -09, -10 (cut to D+1, D+56, D+111) fail on family (the port reading past the file's end; the message a guess); FK7-01
  to -07 pass (still shorter than D, same message: the "should be" number is unchanged), x1, x2, c0 pass; others pass.
- **E, kind 8** (top index CRC): `PhysicalFile.kt:69` `throw IOException("top index checksum error")` made `32` (the
  mismatch read as the new format). Predicted: FK8-01 to -10 fail on family (route or another failure, a guess per
  case); FK8-x1 passes; others pass.

#### Plants, observed (`11-fk-plant[A-E].diff`, `-out.txt`)

Each run: 0 compile errors, 1 fresh XML, restored `cmp`-equal [C, the runner's output]; 8 tests, 1 failure each.
- **A** (range × 4): FK5 9 of 10 failed (port: route 6, "no track found at pass=0" 3, still not mapped 1); **FK1 and FK2
  0 of 10**, not as I guessed "fewer": with no node within 300 m, nothing came into range at 1 km either [I]. Others 0.
- **B** (no island check): FK4 10 of 10, port "no track found at pass=0", as predicted; FK3 and the rest 0.
- **C** (missing file not flagged): FK6 10 of 10, port "to-position not mapped", as predicted; others 0.
- **D** (too-short check at `len < pos`): FK7-08, -09, -10 failed, as predicted; others 0. The port's outcome read
  **"route" with an empty track** (0 nodes): the read past the end throws `EOFException`, whose message is null;
  `NodesCache.kt:188-189` wraps it as `RuntimeException(re.message)` as `NodesCache.java:197-198` does, and
  `RoutingEngine.kt:234` (Java `:812`) keeps a RuntimeException's message, so `errorMessage` is null and `kindOf`
  reads null as "route" [R for the lines; I that Java does the same under this edit, not run].
- **E** (CRC mismatch read as the new format): FK8-01 to -10 failed (port: route on all 11); x1 and others 0.
All five caught; every failure is one its own edit could produce.

### 2.4 F0h: not done

A draft test (six fields, as F0's) was written but never compiled or run. It was taken out of the tree at the handoff,
not pushed. No plant was made.

### 2.5 CI reads

Not read this session. At the handoff, the runs on `554887e`, `4b584a4`, `e309e45` had finished (`failure`, the expected
`CollectOutreachersTest` pair or more, unread); `1bf57ca`, `33de34b` and `34277b6` were still running. `3cfdbe9` (docs only)
did not start a push run, as item 1 intends [C: `gh run list`, which shows no row for it].

### 2.6 [I], [?], and E1

- E1, session 39's `4b9409e` [C, scan.sh's three forms]: `scan.sh 4b9409e^ 4b9409e` and `scan.sh 4b9409e^..4b9409e`
  read **2** (that commit's own added lines); `scan.sh 4b9409e` (one argument: the commit against the working tree)
  reads 0, and so does the range `e1940ed 1bf57ca` (the fix removes the lines). The two readings come from the two forms.
  This session: every staged diff scanned before its commit with
  `ENDS=…/forager-ends.txt bash /home/user/Forager-iOS/docs/process/tools/scan.sh` (default `--cached`), all 0.
- [?] Plant D: a RuntimeException with a null message reads as success with an empty track, in BRouter's own code as
  much as in the port [I]. No R case is known to reach it.

### 2.7 Questions for the planner

1. -132's NT premise is false (§2.2): radii equal 59 of 59 (0), `directionDiff`s equal 59 of 59 (gap 0). Is the next
   NT plant to be on `end()`'s insertion order (`WaypointMatcherImpl.kt:176-193`), or is NT sent to the reviewer as not
   showing the near-tie reaching an outcome?
2. Plant D's null-message "route" (§2.6): flag it for step 10, or leave it?

### 2.8 Groups done and not done; Next

Done: FK 1 to 8 (84, 0 mismatches; five plants, all caught). Not done: NT's second plant (stopped), F0h (8), CI reads.

Next:
1. Read CI on `554887e` and every later code push (`33de34b`, `34277b6`): TOTAL, FAILED, ` e: `, iOS, gate, jvm minutes.
2. F0h: the draft from the scratch folder or written new; compile, commit the comparison, run, then one heights plant with
   its prediction pushed first.
3. NT, per the planner's answer to Q1.

## 3. Session 41

Plumb Ledger, session 41, dispatched by Cedar Datum: F0h through the port, NT's plant (RECORD -133, decision 1), one
question from the Java (-133, decision 2), the assembled count. Branch `brouter-port-5b`, from `696dd18`, fast-forward
only.

### 3.1 F0h through the port

The comparison was stated in `e8ed646` before any run: F0h's 8 cases (`E3F0hTest.ENDS`, in order); per case the oracle
in this JVM held to its row's whole-tuple digest in `expected-1b.tsv` (compare `exact`), then port against oracle on the
six fields of `E3Outcome` (kind, nodes, cost, distance, position digest, heights digest).

First run, single class, local JVM [C] (`2026-11-12-brouter-port-step9/20-f0h-first-run.txt`): 8 cases, kinds
`{route=8}`, 0 mismatches; JUnit 1 test, 0 failures.

**Plant, prediction (pushed before the run).** In `OsmPathElement.kt`'s `create(path: OsmPath)` (the one-argument
`create`, `OsmPathElement.java:84-90`), the height handed on is pushed up by one: `n.getSElev()` becomes
`(n.getSElev() + 1).toShort()`. That `create` makes the track's own nodes (`compileTrack`'s elements and each path's
`myElement`); the transfer nodes inside a link (the four-argument `create` from `OsmPath`) are not touched. The search's
costs read `OsmPath`'s own `selev` and the `OsmNode`'s, never an `OsmPathElement`'s [R, `grep -n getSElev`], and
`recalcTrack`'s distance is `calcDistance` over positions only, so: all 8 cases mismatch, each on `heights digest` only;
kind, nodes, cost, distance and the position digest stay equal; the test fails with `9 F0h: 8 mismatches`. Where this is
a guess [I]: that no F0h node has no height (a `Short.MIN_VALUE` lifted by one would also change `postElevationCheck`'s
filling; it would still show as heights only).
