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

**Plant, observed** [C] (`21-f0h-plant-out.txt`, `21-f0h-plant.diff`; scratch copy of `revert.sh`): compile errors 0,
1 fresh XML, 1 test, 1 failure, `9 F0h: 8 cases; kinds {route=8}; 8 mismatches`, each of the 8 on `[heights digest]`
alone. As predicted. Restored from the saved copy, `cmp` equal; `git status` clean after.

### 3.2 NT's plant (RECORD -133, decision 1)

**Whether the plant can bite, from the code** [R]. `WaypointMatcherImpl.kt`'s `updateWayList` appends one entry and sorts
the whole `wayNearest` (stable `sortWith`, then the list cut to 5); `end()` calls it twice per link, once per direction
(`_w_`, `_w2_`), then takes `wayNearest[0]` for the waypoint's `node1`, `node2`, crosspoint, radius and diff. So the list is
not one entry per link: it holds both directions of every link seen. NT's construction (`NTGenerator`, steps 2 to 4)
puts the start on junction J between two links A = N1-J and B = J-N2 that lie on one line, so A's entry in direction
N1 to J and B's in direction J to N2 tie on both keys (-133, finding 1: all 59), and step 3 makes that pair the two
smallest. Without the plant, the entry added first stays first among the tied pair; with the list reversed before each
sort, the one added last goes first. So the reversal swaps which link is first in every NT case: A's entry gives
`node1` = N1, `node2` = J; B's gives `node1` = J, `node2` = N2. No STOP.

**The plant:** `ways.reverse()` inserted before `ways.sortWith(comparator)` in `updateWayList`. Run: the class
`RoutingEngineStep9Test` (NT, LB, CR).

**Prediction (pushed before the run).**
- The swap itself [R]: all 59 NT cases start from the other link.
- NT's 3 non-routes (1 "to-position not mapped", 2 "target island detected") pass: the end's mapping and the target's
  island check do not read the start's link [I].
- NT's 56 routes: **56 fail**, at least on `nodes` and the position digest; cost may move too. This count is a guess [I].
  The reason I expect it: `getStartPath` makes paths n1 to n2 and n2 to n1 over the matched link with the crosspoint at
  J. On link B the forward path leaves J at once; on link A the useful path is N1 to J, which ends at J after its
  crosspoint, J, so I expect J twice at the head of the track. If the port (and the Java) drops a repeated node, or the
  search makes the same tree from J either way, fewer fail, down to 0, and that would be the finding -133 names.
- LB and CR: 0 mismatches each. Their starts are not built on an exact tie, so a reversal of tied entries should not
  reach them [I]; a failure there would mean an exact tie that their construction does not name.

**Observed** [C] (`22-nt-order-plant-out.txt`, `22-nt-order-plant.diff`): compile errors 0, 1 fresh XML, 3 tests, 1
failure (NT's). `9 NT: 59 cases within 89 draws; kinds {route=56, to-position not mapped ...=1, target island detected
...=2}; 23 mismatches`. `9 LB: ... 0 mismatches`, `9 CR: ... 0 mismatches`, as predicted. Restored from the saved copy,
`cmp` equal; `git status` showed no code change after.
- **Caught, but not as predicted.** 23 of 59 fail, not 56. Of the 20 the test prints (it prints the first 20), all 20 are
  routes and all 20 differ on `cost` alone: nodes, distance and both digests equal. The cost moves by -12 (1), -5 (1),
  -1 (8), +1 (10). The remaining 3 are not printed; their fields and kinds are not known [?].
- So my reason was wrong: no repeated node at J; the same track is found, and only its cost depends on which tied link
  starts it. Where it does not move (36 cases, among them at least the 3 non-routes unless they are among the 3 unprinted),
  the two start links give one cost [I].
- The finding for -133: NT's outcomes **do** depend on which tied link is first, through the cost, in at least 23 of 59.
  The port's order of entries reaching the sort matches the oracle's in all 59 (the unplanted run, 0 mismatches).

### 3.3 Can an unplanted run end with no message and no track? (-133, decision 2)

From the Java only [R], `third_party/brouter/src/main/java/btools/router/RoutingEngine.java`:
1. Start state: `foundTrack = new OsmTrack()` (no nodes), `:62`; `errorMessage = null`, `:67`. Routing mode: `doRun`
   `:170-177` calls `doRouting`; its argument checks throw out of `doRun` (`:174-175`), not a return.
2. Normal end: `foundTrack = track` at `:292` or `:305`, message null. `track` is never null there: `searchRoutedTrack`
   throws "no track found..." rather than return null (`:1770`, `:1773`). Its node count is not shown here [I, at least 1].
3. `:233` `if (routingContext.ai != null) return;`: `ai` is set only in `doGetInfo` (`:626`, cleared `:654`), so not in
   routing mode.
4. The catches `:318-326` all call `logException` (`:811-812`): `errorMessage = t.getMessage()` for a
   `RuntimeException`, `t.toString()` (never null) otherwise. **So yes: a `RuntimeException` with a null message, thrown
   before `:292`/`:305`, ends the run with no message and `foundTrack` still `:62`'s empty track.** Which exceptions in
   BRouter's routing can carry a null message was not searched [?]; its own throws carry text (e.g. `:1976`).
5. Forager: `BRouterTrailRouter.kt:133-139` hands it on as `EngineOutcome.Ran` with the empty track; `foundRoute` then
   refuses it at `:107-111` (`nodes.size < 2`: `EngineFailed("no error reported and 0 route points")`, logged). Not a route.

### 3.4 The assembled count

Rows: one `awk` over `expected-1b.tsv` by stratum, `compare` and kind (`route` or a message) [C]. Port: each test's
printed line, from CI on `34277b6` (run 43155294643, jvm job) [C], except F0h, from this session's single-class run on
`e8ed646` [C] (CI on it was still running at writing).

| Group | Rows (`compare`; route / message) | Port test, cases run | Mismatches | Compared on |
|---|---|---|---|---|
| R | 1534 (exact; 1268 / 266) | `RoutingEngineRTest` 1534: 968 route, 212 error, 354 refused | 0 | 1180 on six fields; 354 on the refusal by name (`threw:unsupported: path model ...`) |
| F0 | 2 (exact; 2 / 0) | `RoutingEngineF0Test` 2 | 0 | six fields |
| F0h | 8 (exact; 8 / 0) | `RoutingEngineF0hTest` 8 | 0 | six fields |
| DL-lat, DL-lon | 59 + 59 (exact; 118 / 0) | `RoutingEngineDLTest` 59 + 59 | 0 | six fields |
| MP64 | 59 (exact; 59 / 0) | `RoutingEngineMemoryTest` 59 | 0 | six fields |
| ML5 | 59 (exact; 0 / 59) | `RoutingEngineMemoryTest` 59 | 0 | six fields; kind is the message's text (`E3Outcome.kindOf`) |
| PM5 | 55 (exact; 55 / 0) | `RoutingEngineMemoryTest` 55 | 0 | six fields |
| NT | 59 (exact; 56 / 3) | `RoutingEngineStep9Test` 59 | 0 | six fields |
| LB, CR | 59 + 59 (exact; 118 / 0) | `RoutingEngineStep9Test` 59 + 59 | 0 | six fields |
| FK1-FK6 | 60 (exact; 0 / 60) | `RoutingEngineFKTest` 10 each | 0 | message text and the five other fields |
| FK7 | 13 (11 exact, 2 outcome; 2 / 11) | `RoutingEngineFKTest` 13 | 0 | message family (digits masked, `familyOf`) and five fields |
| FK8 | 11 (10 exact, 1 outcome; 1 / 10) | `RoutingEngineFKTest` 11 | 0 | message text and five fields |
| FK9 | 10 (7 exact, 3 family; 0 / 10) | `RoutingEngineStopTest` 10 | 0 | kind, message's fixed part, nodes/cost/distance, terminated (no digests) |
| FK10, FK11 | 20 (exact; 0 / 20) | `RoutingEngineStopTest` 10 + 10 | 0 | as FK9; FK11 also the link count at the cancel |
| **Total** | **2,126** | **2,126** | **0** | |

The total is 2,126, equal to `expected-1b.tsv`'s rows. On all six fields: 1,180 R + 2 F0 + 8 F0h + 118 DL + 173 memory
(MP64, ML5, PM5) + 177 NT/LB/CR = **1,658**. On a refusal by name: **354** (R). On a message's text or family with the
track's counts: **114** (FK; of these, FK9-11's 30 without the two digests). Messages among the six-field rows (R's 212,
ML5's 59, NT's 3) are compared by their whole text, `kindOf` folding only timeouts and the watchdog into families.

**The plan's line for step 9** (`2026-10-28-brouter-port-verify.md:1704`): "E's 2,118 cases, asserted or recorded per E6
under the options ruled; JUnit XML counts against 2,118 less E3's unstable cases". What this table does not yet show:
- **2,118 against 2,126.** The plan counts 2,118; `expected-1b.tsv` holds 2,126. The 8 more are not traced here [?]
  (F0h's 8 is one candidate, FK's extra rows, -80/-85, another).
- **"Less E3's unstable cases":** the file names 3 FK9 rows as unstable, compared by family only, and they are in the 2,126
  above. No group shows a number "less" the unstable cases; the step's own figure is not computed.
- **"JUnit XML counts":** each group is one JUnit test; the XML counts tests (193 jvm on `34277b6`), not the 2,126
  cases. The cases come from printed lines, as here.
- **"Asserted or recorded per E6":** every group above is asserted exactly on the JVM; nothing is recorded. E6 asks which
  option was ruled (the table and `atan2`: (a), (b) or (c); `hmap`: (i), (ii) or (iii)) and whether each run is a logic or a
  production run. The table does not name either; R's 354 refusals are neither asserted equal nor recorded with an
  interval.
- **One run:** the counts come from one CI run plus F0h locally, not one run over E.

### 3.5 CI reads

- **`34277b6`** (run 43155294643) [C]: jvm `TOTAL: tests 193, failures 2, errors 0, skipped 0 in 84 files`; the 2 FAILED
  are `CollectOutreachersTest`'s two (not touched); `:brouter:jvmTest FAILED in 21m 56s` (the test step 04:31 to 04:53, about
  22 minutes); ` e: ` 0. `compileKotlinIosArm64` and `compileKotlinIosSimulatorArm64` ran after (BUILD SUCCESSFUL in 32s).
  ios-smoke success, `TOTAL: tests 24 ... 0 failures in 10 files` and `tests 7 ... 0 failures in 2 files`,
  simulator-checked. FK lines: FK1-FK8 84 cases, 0 mismatches each (§3.4). The caught-overflow print
  (`cleanupPeninsulas caught a StackOverflowError`): 2 console lines (and 2 more in the XML dump). The gate: no line
  naming a gate was found by `grep -iE 'gate|allowlist|known fail'` [?]: the job's conclusion is failure.
- **`e8ed646`** (run 43157155479): still in progress at the handoff; not read.

### 3.6 [I], [?], questions, done and not done; Next

[I]: §3.2's reading that the 36 unmoved NT cases share one cost on both links; §3.3's "track has at least 1 node".
[?]: the 3 unprinted NT mismatches; which BRouter routing exceptions can carry a null message; the 8 between 2,118 and
2,126; the gate's line on `34277b6`. "None" claims: LB and CR under NT's plant, 0 mismatches (`22-nt-order-plant-out.txt`).
E1: `scan.sh` on each staged diff, 0 before every commit (one hit, diff-header timestamps, removed before commit).

Questions for the planner:
1. NT's plant was caught (23 of 59, cost only). Does NT still go to the independent reviewer, or does this close -133's
   decision 1?
2. Item 4: a null-message `RuntimeException` leaves no message and an empty track. Forager refuses it at
   `BRouterTrailRouter.kt:107-111`. Is the iOS adapter to be held to the same guard (not read this session)?
3. 2,118 (plan) against 2,126 (`expected-1b.tsv`): which is step 9's figure?

Done: F0h through the port (8, 0 mismatches) and its heights plant (8 of 8 caught, as predicted); NT's order plant
(caught, 23, against a prediction of 56); item 4 from the Java; the assembled count (2,126 run, 0 mismatches); CI on
`34277b6`. Not done: CI on `e8ed646`; the 3 unprinted NT mismatches; the plan's "recorded per E6" and "less unstable".
No STOP was hit.

Next:
1. Read CI on `e8ed646` (F0h's first CI run): TOTAL, FAILED, ` e: `, iOS, the F0h line.
2. The planner's answers to Q1 to Q3.
3. Step 9's figure per E6 once the options and the 2,118 are settled.

## 4. Session 42

Plumb Ledger, session 42, `brouter-port-5b` from `1ba6b06`. Dispatch: step 9's production runs (RECORD -134, decision 4).
**Stopped at step 3** before any code: the build forwards no system property it does not name to the test JVM (§4.3).

### 4.1 Survey: what the JVM production run is held to, and what exists for it

**What E6 says, under (c)** (`docs/audits/2026-10-28-brouter-port-verify.md:980`, the table's "JVM, production" row) [R]:
"**recorded** against the default oracle: different by design at 54 rows. **Exact** against the oracle run with
`-XX:-UseLibmIntrinsic`, whose table is `StrictMath`'s". The 54 are the rows where `Math.cos` (intrinsic) and
`StrictMath.cos` give a different `kx` or `ky` (`:846`). E6 also says (c) "needs a second oracle JVM with the flag, run as
its own Gradle test task" (`:988`). That cell assumes the port's table is `StrictMath`'s.

**What -24 ruled** (`RECORD.md:915`, entry -24) [R]: the owner chose "Copy Android's maths": `cos` from bionic's FreeBSD
msun, "a fourth option beside H's (a) to (c)"; H's (c), "Copy Java's reference maths", was **rejected**. The planner's
calls: "the oracle runs ... with `-XX:-UseLibmIntrinsic`: fdlibm, 13 rows from Android rather than 59"; "production runs
use the msun table and record any route difference".

**What exists today** [R]:
- **One oracle, and it is the no-intrinsic one.** `brouter/build.gradle.kts:141` gives every test JVM of `oracleJvm()`
  (`jvmTest`, `:155`, and `e3Strata`, `:169`) `-XX:+UnlockDiagnosticVMOptions -XX:-UseLibmIntrinsic` on x86_64
  (`:142`, `-XX:DisableIntrinsic=_dcos` on aarch64). The oracle's own child JVMs take the same `-XX:` flags
  (`E3Launcher.kt:32`). `CheapLogicRunTest.kt:27-28` asserts the flag is in force.
- **No oracle with the intrinsic on**: no second oracle task or JVM. `tasks.register` in the build: `e3Strata` only
  (`:166`), on the same `oracleJvm()`.
- **The production table against this oracle: 13 rows, pinned.** `CheapProductionRecordTest.kt:15` (rows 474 ... 1318),
  `:18` asserts `msunCos` differs from the oracle's `Math.cos` at exactly those; CI on `e8ed646` printed "cos differs at 13
  rows, kx or ky at 13" (§4.4). `atan2`: equal at every pair (`:38`).
- **Route-level production runs:** only F0's 2 cases, `RouteTest.kt:102` (`f0ThroughThePublicRouteIsRecorded`), recorded
  by `println` (`:108`), asserting only a `Found` of 2 or more nodes.
- **No stored expected values from any other oracle**: the logic tests compare to `E3CaseRun` live; the no-intrinsic run
  *is* the default. `git grep -n -i 'UseLibmIntrinsic\|production'` over the build and `jvmTest` shows no other (24 lines, the first 20 read,
  all cited above, `LBGenerator.kt:14-28` and `E3LBTest.kt:24` naming the same 13 rows).

**So, plainly:** the cell of E6 the dispatch quotes does not describe this repository. Its "default oracle" (intrinsic on,
54 rows) does not exist here, and its "exact against the no-intrinsic oracle" assumed a port equal to `StrictMath`; the
port is msun, which differs from that oracle at 13 rows. What applies is -24's: one production run, the port with
`MsunLibm`, **recorded** against the one oracle there is (no-intrinsic), the difference confined by design to routes that
touch the 13 rows. Nothing is missing for that run but the test itself; the second oracle JVM E6 names is not needed under
-24 and was never built [R: the grep above].

### 4.2 The production run over every group: not run

Not written, not run (the STOP in §4.3). No counts.

### 4.3 STOP: the gate the dispatch asks for cannot be passed without a build change

The dispatch: "Gate it behind a system property ... pass the property on the command line; if the build does not forward
system properties to the test JVM, STOP and report rather than change the build." [R] The build sets the test JVM's
system properties one by one, by name (`build.gradle.kts:128-133`, `:179-180`); none is taken from Gradle's own `-D`, and
nothing copies `System.getProperties()` (`grep -n 'systemProperty\|getProperties\|gradleProperty'`: those 9 lines and
`:140`, `os.arch` read for the flag, `:165`, `brouter.e3.jdk`). A `gradlew -Dbrouter.production=1` therefore never reaches
the test, and a test gated on it would never run. Stopped; nothing written.

Ways the planner could take, none chosen:
- (a) **one named forward in the build** (`providers.systemProperty("brouter.production")` passed on in `oracleJvm()`),
  absent by default so CI does not run it. A build change, so the planner's.
- (b) **an environment variable** as the gate: a Gradle `Test` JVM inherits the Gradle process's environment by default
  [I, Gradle's documented default, not tried here]. No build change, but not the system property asked for.
- (c) **`JAVA_TOOL_OPTIONS=-Dbrouter.production=1`** on the command line sets the property in the test JVM without a build
  change [I], but it reaches every JVM in the run, the oracle's children too (they inherit the environment), and each prints
  "Picked up JAVA_TOOL_OPTIONS" on stderr, which `E3Launcher.kt:37` merges into the oracle's log
  (`redirectErrorStream(true)`). Whether that breaks the log's parsing was not checked [?]. Not recommended.

The expectation the commit would have stated, kept here for whoever writes it: by -24 and `CheapProductionRecordTest`, a
production case can differ only if its search reads a `kx` or `ky` from one of the 13 rows (latitudes inside the captured
tiles among them: `LBGenerator.kt:14` generates LB there). Groups whose tiles hold none of the 13 should read all equal;
any other count is a guess [?].

### 4.4 CI on `e8ed646` (run 43157155479, F0h's first full run) [C]

- **jvm** (job conclusion failure): `TOTAL: tests 194, failures 2, errors 0, skipped 0 in 85 files`. The only FAILED lines
  are `CollectOutreachersTest`'s two (`collectOutreachersWithoutADestination`, `AssertionError at
  CollectOutreachersTest.kt:141`; `collectOutreachersWithADestinationVanishesFarNodes`, `:116`), as on `34277b6`; not
  touched. `:brouter:jvmTest FAILED`, `BUILD FAILED in 20m 54s`; the test step ran 04:48:17 to 05:09:13, **about 21
  minutes**. ` e: ` 0. `compileKotlinIosArm64` and `compileKotlinIosSimulatorArm64` both ran after, `BUILD SUCCESSFUL in 34s`.
- **F0h's line:** `9 F0h: 8 cases; kinds {route=8}; 0 mismatches`.
- **`grep -c 'caught a StackOverflowError'`: 4** (2 console lines, 2 in the XML dump), as on `34277b6` (-61).
- **ios-smoke** (success): `compileKotlinIosSimulatorArm64`, `TOTAL: tests 24, failures 0, errors 0, skipped 0 in 10
  files`; then `TOTAL: tests 7, failures 0 ... in 2 files` (`CheapLogicRunTest` 5, `CheapProductionRecordTest` 2) and
  **`OK: all 2 classes ran`**, the gate. Simulator-checked. `CheapProductionRecordTest` there printed "cos differs at 13
  rows, kx or ky at 13" (§4.1).

### 4.5 [I], [?], questions; Next

[I]: §4.3 (b) and (c), that a Gradle `Test` JVM inherits the environment and that `JAVA_TOOL_OPTIONS` sets the property.
[?]: whether `JAVA_TOOL_OPTIONS`'s stderr line breaks the oracle's log; how many production cases touch the 13 rows.
"None" claims: no second oracle task (`grep -n 'register<Test>\|tasks.register' brouter/build.gradle.kts`: `e3Strata` at
`:166` only, on `oracleJvm()`, `:169`); no generic property forward (`grep -n 'systemProperty\|getProperties\|gradleProperty'
brouter/build.gradle.kts`: named ones only, §4.3); no stored no-intrinsic expected values beyond the live oracle (§4.1's
`git grep`). No code written; the only change is this section. E1: `scan.sh` on the staged diff, 0 before the commit.

Questions for the planner:
1. **A premise, -134 and this dispatch against -24:** both say the port took E6's option (c), "fdlibm ported"; -24 records
   the owner rejecting (c) for msun, a fourth option, and the oracle running without the intrinsic. E6's (c) cell (54 rows,
   a second oracle) does not apply. Is the production run held to -24's reading, recorded against the no-intrinsic oracle
   with the difference confined to the 13 rows (§4.1)?
2. **The gate (§4.3):** (a) a one-line named forward in the build, (b) an environment variable, or (c) neither?

STOP hit: §4.3 (the build does not forward system properties). Also reported: the premise in Q1.

Next:
1. The planner's answers to Q1 and Q2.
2. The production test over every group as the dispatch's step 3, with the expectation of §4.3 in its commit; one local run;
   counts to an evidence file and §4.2.
3. If under budget: how each differing case differs (cost only, route, kind), counts only.

## 5. Session 43

Plumb Ledger, session 43, `brouter-port-5b` from `06b97f2` (`git ls-remote` checked). Dispatch: step 9's recorded
production run on the terms of RECORD -135 (quoting -24). Done: items 1, 2, 3, 6; item 4 not reached (no difference in
kind); item 5 moot (no differing case).

### 5.1 The recording test (`2763f4c`)

`RoutingEngineProductionRecordTest` (jvmTest, `router`), what it does stated in its commit before any run. Gate: the
environment variable `BROUTER_PRODUCTION_RECORD`, non-empty; unset, one printed line and nothing else. Set: the logic tests'
own cases, built as they build them, through the oracle in this JVM (`E3CaseRun`) and the port on **`MsunLibm`**
(`portMsun`, `RoutingEngineF0Test.port` with `RoutingContext(MsunLibm)`), compared on E3Outcome's six fields. Groups: F0,
R's 1,180 StdModel cases (the six `---model:` profiles left out), DL-lat, DL-lon, MP64, ML5, PM5, NT, LB, CR (selected from
their generators by `expected-1b.tsv`'s row ids within `E3Targeted.CAP`, as the logic tests), FK 1 to 8, F0h. **FK 9 to 11
are timing cases and are left out.** FK7's kind is compared by family, as `RoutingEngineFKTest`; its exact-kind differences
are counted beside. Asserted: only that every case produced an outcome and that the counts add up to the group's rows.
Recorded beside: whether the oracle in this JVM matched its `expected-1b` row (`oracleOffRow`).

Visibility words, named: in `RoutingEngineFKTest`, `rows`, `firstTen`, `byRow`, `kind5`, `edited`, `case` and the companion
object, `private` to `internal`. Nothing else in an existing test changed. FK7's and FK8's case lists are built inline in
that test's two `@Test` bodies, so they are **repeated** in the new test (10 + 3 and 10 + 1 cases); `oracleOffRow` 0 on both
shows they are 1b's. No build change.

### 5.2 The expectation (`a014538`, pushed before the run)

`docs/audits/2026-11-12-brouter-port-step9/30-production-expectation.txt`. By CheapRuler's row rule, 8 of the 13 rows lie in
no captured tile; rows 1295 and 1297 lie in the N35 band, 1310, 1311 and 1318 in N40; E170_S50 none. Guesses (all marked
so): F0, F0h, FK7, FK8, FK1 to 6 zero; R 0 to 10; DL 0 to 3 each; MP64, ML5, PM5 1 to 10 each; NT 0 to 2; LB 1 to 15;
CR 0 to 5; no difference in kind.

### 5.3 The run: 1,742 cases, every one equal

One local run, `BROUTER_PRODUCTION_RECORD=1 timeout 5400 ./gradlew :brouter:jvmTest --rerun --tests '...RoutingEngineProductionRecordTest'`.
**Wall time 1,203 s** (Gradle: `BUILD SUCCESSFUL in 20m 2s`; the test's own line, 1,201.0 s). Evidence:
`31-production-run.txt` (every line verbatim).

| Group | Cases | Equal | Differing | In kind | oracleOffRow |
|---|---|---|---|---|---|
| F0 | 2 | 2 | 0 | 0 | 0 |
| R (StdModel) | 1,180 | 1,180 | 0 | 0 | 0 |
| DL-lat | 59 | 59 | 0 | 0 | 0 |
| DL-lon | 59 | 59 | 0 | 0 | 0 |
| MP64 | 59 | 59 | 0 | 0 | 0 |
| ML5 | 59 | 59 | 0 | 0 | 0 |
| PM5 | 55 | 55 | 0 | 0 | 0 |
| NT | 59 | 59 | 0 | 0 | 0 |
| LB | 59 | 59 | 0 | 0 | 0 |
| CR | 59 | 59 | 0 | 0 | 0 |
| FK1 to FK6 | 10 each | 10 each | 0 | 0 | 0 |
| FK7 | 13 | 13 | 0 | 0 (exact kind too) | 0 |
| FK8 | 11 | 11 | 0 | 0 | 0 |
| F0h | 8 | 8 | 0 | 0 | 0 |

**So, on these cases, the 13 rows change no route:** no kind, node count, cost, distance, node digest or heights digest
differs in any group. My expectation guessed a few in LB and the memory groups; it was wrong on the high side.

**The test is not vacuous** (CLAUDE.md: a check that passes identically is suspect): a scratch plant, never committed, made
`portMsun`'s libm return `kx * 1.01` at the five in-tile rows and ran LB alone. `compileTestKotlinJvm` executed, ` e: ` 0;
LB read **59 cases, 0 equal, 59 differing** (kind 1, nodes 5, cost 59, distance 59, node digest 16, heights 10; split: cost
only 0, same nodes other distance 43, different route 15, other 1). Restored from a saved copy, `git status` clean after.
Evidence: `32-production-plant.txt` (the diff and the lines). So the seam reaches the engine at those rows; msun's own
difference there (an ulp or so in `kx` or `ky`) moves no integer on these 1,742 cases.

**Found on the way:** the first attempt without `--rerun` ran nothing: `jvmTest` was `UP-TO-DATE`, since an environment
variable is not a task input. Not counted as a run (`31-production-run.txt`, line 3). A CI job for this run, if the planner
rules one, starts fresh and is not affected; locally it needs `--rerun` (or `--rerun-tasks`). [R: the log]

### 5.4 CI on `2763f4c` (run 43160257745) [C]

- **jvm** (failure): `TOTAL: tests 195, failures 2, errors 0, skipped 0 in 86 files` (194 + the new test). FAILED lines:
  only `CollectOutreachersTest`'s two (`collectOutreachersWithoutADestination`, `collectOutreachersWithADestinationVanishesFarNodes`),
  as before; not touched. ` e: ` 0. `compileKotlinIosArm64` and `compileKotlinIosSimulatorArm64` ran, `BUILD SUCCESSFUL in 38s`.
  `grep -c 'caught a StackOverflowError'`: 4, as before (-61).
- **The new test's line:** `production record: BROUTER_PRODUCTION_RECORD is unset; not run (RECORD -135)`; its XML time
  `0.003` s.
- **The jvm test step's minutes: 24.5** (05:16:20 to 05:40:51) against about 21 on `e8ed646`. Not the new test (0.003 s):
  the whole runner was slower, e.g. `jvmTest` start to F0h's line 6m21s against 5m09s, F0h to R's line 13m06s against
  11m17s. [C, both runs' logs] Whether a later run returns to 21 is not known [?].
- **ios-smoke** (success): `TOTAL: tests 24, failures 0 ... in 10 files`; `TOTAL: tests 7 ... in 2 files`; **`OK: all 2
  classes ran`**. Simulator-checked.

### 5.5 [I], [?], questions; Next

[I]: none new. [?]: whether the jvm step's 24.5 minutes is runner variance (§5.4); whether cases outside these strata (other
tiles, other latitudes among the 8 rows no captured tile holds) would differ.
"None" claims: no case differs (`grep 'production record: ' 31-production-run.txt`: every group line reads `differing` 0);
no other test newly failing (`grep ' FAILED'` on the CI log: only `CollectOutreachersTest`'s two and the task/build lines);
no build change (`git diff 06b97f2 a014538 --stat`: the new test, `RoutingEngineFKTest.kt` (7 visibility words), the
evidence file). E1: `scan.sh` on each staged diff, 0 before each commit.

Item 5 (how differing cases differ; which pass through a row): moot, no differing case; no scratch count made.

Questions for the planner:
1. -24's "separately record whether the 13 rows ever change a route": on these 1,742 cases, never. Is that the record, or
   does the owner want the run in CI as its own job (~20 minutes locally, with `--rerun` not needed on a fresh runner)?
2. The repeated FK7/FK8 case lists in the new test: acceptable, or should they move to a shared builder (a change to
   `RoutingEngineFKTest` beyond a visibility word, so the planner's)?

Next:
1. The planner's answers to Q1 and Q2.
2. If the run joins CI: a job running only this class with the variable set; its length read on one run.
