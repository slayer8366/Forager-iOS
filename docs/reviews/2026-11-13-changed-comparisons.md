# Review: five comparisons changed after a result was seen

Reviewer: Samphire Witness. Dispatch 2026-11-13-01 from Cedar Datum. First review.

Status: COMPLETE. All five read, three planted-error checks run, verdict and scoring at the end.

Code under review: branch `brouter-port-5b` of slayer8366/Forager-iOS, read at head `b359671` in a detached worktree of my own.
Record under review: `RECORD.md` as checked out in `/home/user/Forager-iOS`.

## Marks

- [R] read at source by me (file and line given).
- [C] computed or run by me (command given).
- [I] inferred.
- [?] not verified.

## Method

The question, for each of the five: was the change justified by evidence that does not depend on the port's own result, or did it make a failing test pass?

For each change I do five things, in this order.

1. Read the change itself as a diff, from the commits the record entry names, with commit ids and file:line.
2. Establish what result had been seen before the change, and by whom.
3. Take the evidence offered for the change and check it at its source: the Java line, an oracle-only run, or the wording of a ruling. Evidence that is only "the port gave this" does not count.
4. State what the test could catch before the change and what it can catch after. Where there is one, name a wrong port that the changed test would now pass.
5. Check the record's account of the change, treating every sentence as a claim.

Then at least one revert check of my own: a planted error in a scratch copy of the code, never committed, one Gradle run at a time, restored from a saved copy and not from git. The build log is checked for compile errors before a result is cited, and each failure is checked to be one the planted error could produce.

I change no test, no port code and no ruling. iOS results cannot be run here: anything about the simulator is the record's claim and is "simulator-checked" at most.

## Predictions (made before reading any entry, commit or test)

These are predictions from the one-line descriptions in the dispatch alone. They are not findings.

| # | Entry | Prediction | Why I guess so |
|---|---|---|---|
| 1 | -107 (step 6, session 16) | justified in part | No description given beyond the step. Base rate guess: a real reason exists, but the changed comparison lost some reach that nobody restored. |
| 2 | -116 (step 7, session 23) | justified by independent evidence | No description given. Guess: a harness or oracle-side defect shown by an oracle-only observation. |
| 3 | -119 (step 7, session 26, `formatDecimal3` above 2^63, owner's "Stop at the limit") | justified in part | An owner's ruling does not depend on the port's result, so the narrowing is authorised. But I expect the ruling to leave a range untested, and I expect the limit to have been found by watching the port fail. |
| 4 | -126 (step 8c, "pass0 timeout" to "target-island-check timeout", pattern widened) | justified in part | I expect the new expected text to be what Java really prints (checkable at the Java source), and the widening to be unjustified, since -127 already says it blinded a test. |
| 5 | -148 (red since step 5b, green by an addition to setup) | justified by independent evidence | Guess: the setup lacked something the Java engine has in real use, and the assertion stayed as fixed. Lower confidence: a setup addition can also steer a test away from the failing case. |

Overall prediction: no change was made in bad faith; at least two of the five leave the test weaker than the record says.

Mechanism prediction for the revert check: on the change where a pattern was widened (4), a planted error in the text the pattern matches will be caught only if the widening was later narrowed again.

## Baseline

- Code worktree: `origin/brouter-port-5b` at `b359671` [C: `git rev-parse HEAD`]. Record: `RECORD.md` at `2fc95d3` of the planner's checkout [C: `git log -1`].
- Java source read at `third_party/brouter/src/main/java/btools/` of the Forager checkout. Paths below as `btools/...`.
- Test paths below are under `brouter/src/jvmTest/kotlin/com/zynergylabs/forager/brouter/`, port paths under `brouter/src/commonMain/kotlin/com/zynergylabs/forager/brouter/`.
- Gradle works here: no 429 in any run [C: `grep -c ' 429 '` on each log, 0]. JDK here is OpenJDK 21.0.12.1, not CI's Temurin 21.0.13 [C: `java -version`].

## 1. RECORD -107: `SearchBoundaryTest` (step 6, session 16)

**(a) The change.** Commit `7e822c4`, the same commit that ports `SearchBoundary`. `router/SearchBoundaryTest.kt`, diff against the red-stub commit `ca11871` in `01-SearchBoundaryTest-ca11871-to-7e822c4.diff` (44 lines) [R]. Two changes:

1. `isInBoundary` (now lines 57 to 61): each call is wrapped in `runCatching`; a throw is compared by exception class, and the value by `getOrNull()`. Before, the oracle's call was bare, so a throw from the oracle ended the test as an error.
2. `getBoundaryDistance` (now lines 70 to 73): the exception message was compared for every throw; now only when the oracle's throw is an `IllegalArgumentException`.

The two changes are not separate commits. That the port was unchanged between them is the coder's statement in `docs/audits/2026-11-06-brouter-port-step6/14-searchboundary-port-passing.txt` [R] and cannot be checked from git [?].

**(b) What had been seen, and by whom.** Plumb Ledger, the coder, per file 14 [R]: first an uncaught `java.lang.ArrayIndexOutOfBoundsException` with no stack; then, after change 1, "case 180 probe 2 direction 3: message expected:<Index -6 out of bounds for length 1800> but was:<null>". The second is a port result: the oracle gave a message, the port gave none.

**(c) The evidence, checked.**

- Change 1. In the old code the oracle is called first (`val ov = o.isInBoundary(jn(q), cost)`), so the uncaught throw came from the oracle's line or could have [R, the diff]. `btools/util/CheapRuler.java:57` is `SCALE_CACHE[ilat / SCALE_CACHE_INCREMENT]` with length 1800 (line 21) and increment 100000 (line 22) [R]. A latitude below -99,999 or at or above 180,000,000 throws in Java. This stands without the port's result.
- Change 2. The offered reason is that a JIT-compiled index error can lose its message, marked [I] by the coder and by the planner. HotSpot does have that behaviour (a hot implicit exception is replaced by a preallocated one with no message and no stack; the switch is `-XX:-OmitStackTraceInFastThrow`) [I, from my knowledge of HotSpot, not read at a source here]. The "no stack" in the first observation fits it. But nobody ran anything to confirm it. I did, below.

**(d) My runs** (scratch copy, never committed; restored from saved copies and compared with `cmp` against the code worktree, equal) [C: `./gradlew :brouter:jvmTest --tests '*SearchBoundaryTest'`, excerpts in `01-run*.txt`]:

| Run | Test | Port | JVM flag | Result |
|---|---|---|---|---|
| 1B | strict: messages compared on every throw, `isInBoundary` too | as committed | `-XX:-OmitStackTraceInFastThrow` | PASSED, 0 ` e: `, 1 fresh XML |
| 1A | strict | as committed | none | PASSED, 0 ` e: `, 1 fresh XML (0.58 s test time) |
| 1P-committed | as committed | planted: scale row one lower when negative (`CheapRuler.kt:53`) | none | PASSED, `compileKotlinJvm` ran, 0 ` e: ` |
| 1P-strict | strict | same planted error | none | FAILED: "case 3 probe 0 direction 3: message expected:<Index -4[8] out of bounds for l...> but was:<Index -4[9] ..." |

What they show:

- The port's index messages equal Java's on all 2,748 throwing calls (1,380 + 1,368) when messages are present [C, 1B and 1A]. So the loosening hides no present error.
- The null message did not reproduce here in two strict runs [C]. It is run-dependent. I could not confirm the JIT explanation directly; I can say the strict comparison passes on this machine with and without the flag.
- The stricter route the planner named exists and works: with the flag, the strict test passes [C, 1B].
- The committed test is blind to a wrong index on the throwing path; the strict test is not [C, 1P]. The failure in 1P-strict is one the planted error can produce (the index one lower), so it is not a stale result.

**What the test could catch before and after.** Before: any difference in class or message of a `getBoundaryDistance` throw; an `isInBoundary` throw was not handled at all (the test would error on the oracle's own throw, so it could not pass). After: a port that returns a value where Java throws, or throws another class, is still caught. **A wrong port the changed test passes:** one that computes the wrong table row for an out-of-range latitude and still falls outside the table (shown by 1P). The harm of that wrong port is small: both engines throw, and Forager never reaches this class (`RoutingEngine.java:88` only holds a field, the test's own header says [R, header; the Java line not re-read]).

**(e) The record's account.**

- "Plumb changed `SearchBoundaryTest` twice, with the port unchanged": the two changes are real [R]; "port unchanged" is the coder's word only [?].
- "the test now compares those throws by class, and by message only for Java's own `IllegalArgumentException`": accurate [R].
- "It touches only the throwing cases, not the 168,000 compared results": **inaccurate in its count.** The 168,000 include 1,380 `isInBoundary` calls that throw ("radius throws=1380") and are compared by class only [R, file 14; C, run 1B prints the same]. The values compared are 166,620. The commit title "168,000 isInBoundary cases" carries the same overcount.
- "A stricter route, a test-JVM flag that keeps those messages, changes the build and is the owner's call": right, and the route works [C].
- The planner called this "a check loosened after the data was seen". That is the right name for change 2. Change 1 is not a loosening: it made an unhandled oracle throw comparable.

**Finding for 1.** Change 1: justified, by the Java line. Change 2: made after a port-side mismatch on an inferred cause that was not tested; my runs support the cause indirectly and show no error hidden, and show the loosening was avoidable.

## 2. RECORD -116: `ReadBinaryTest` (step 7, session 23, changed in session 24)

**(a) The change.** Commit `60bb212`, `router/ReadBinaryTest.kt`, diff in `02-ReadBinaryTest-60bb212.diff` (62 lines) [R]. Before: `debugInfo` compared whole by digest and length on every read. After (now lines 130 to 148): if either text contains `"Error reading rawTrack: "`, then both must contain it, the text up to and including it must be equal, and each side must have at least one character after it; otherwise the whole text is compared as before. The result comparison (line 129) is unchanged. A counter `errorPaths` is added and printed.

**(b) What had been seen, and by whom.** Plumb, session 23: the test passed tracks 0 to 29 and failed at track 30, the first empty one, the result equal, the debug text 145 characters on the oracle and 93 on the port [?, as the record reports; I did not find or read file 16]. The change was then ruled by the planner, not chosen by the coder, and committed red over the stubs (`60bb212`) before the port went in (`12af1e9`) [R, `git log`].

**(c) The evidence, checked.**

- `btools/router/OsmTrack.java:258` is `t.cost = last_pe.cost;` and `last_pe` is null when `n` is 0 (lines 251 to 257) [R]. Lines 286 to 290 catch `Exception` and append `"Error reading rawTrack: " + e` [R]. So the text after the prefix is the JVM's `toString()` of its own exception, as the planner says.
- The lengths fit: 145 minus 93 is 52, which is `: ` plus the 50 characters of JDK 21's helpful message for that line, `Cannot read field "cost" because "last_pe" is null` [C, counted by hand; I, that this is the text]. The record marks the cause [I]; the arithmetic supports it.
- Unreachable on Forager's path: Java reaches `readBinary` only at `RoutingEngine.java:914` with `rawTrackPath` [R]; the port refuses that branch outright (`router/RoutingEngine.kt:263-264`, `unsupported("rawTrackPath (the nearby track)")`) [R]. That Forager's `pre-main` never sets it is the planner's `git grep` [?].
- This evidence is the Java source and the JDK's documented behaviour. It does not depend on the port's result, though the port's result is how it was found.

**(d) What the test can catch before and after.** Before: any difference in the debug text, including the exception's own text; on this JVM that passed for the cut-short files of tracks 0 to 29 [I, from "passes tracks 0 to 29"]. After: on the error path, everything up to the prefix, that both engines took the path, and the result. **Wrong ports the changed test passes:** (i) a port whose catch writes any non-empty text at all after the prefix; (ii) a port that reaches the catch through a different exception than Java's, as long as the result fields and the text before the prefix agree. Both are the ruling's intent for the null-pointer case. But the loosening is wider than the evidence: it covers every catch, and `21-s24-readbinary-port-passing.txt` reports 1,549 of 4,522 debug texts on the error path, "the EOF cuts and the two empty tracks" [R]. The end-of-file texts were equal on the JVM before the change and are no longer compared.

Also: `errorPaths` is printed (line 157) but never asserted. The only reach assertion is `nonNull > 50 && reads > 1000` (line 158) [R]. If the empty tracks dropped out of the setup, the error path would go untested and the test would stay green.

**(e) The record's account.** The Java lines are cited correctly [R]. "It asserts that both engines took that path and that each wrote some text after the prefix": accurate [R]. "On every other path, `debugInfo` is compared whole, as now": accurate [R]. The title and the reasoning speak of the null-pointer wording only; the entry does not say that about a third of the debug texts (the end-of-file cuts too) moved to the looser comparison. That is in the coder's file 21, not in the record. "Disclosed as what it is: a comparison changed after a result was seen": right.

**Finding for 2.** Justified by the Java source. Two should-fix items: assert `errorPaths > 0` (better, a floor near the known count), and narrow the loosening on the JVM to texts that differ for a named reason, or say in the record that the end-of-file texts are no longer compared.

## 3. RECORD -119: `FormatJsonTest` (step 7, session 26; test changed in session 27)

Two changes sit in this entry.

**(a) The changes.** `router/FormatJsonTest.kt`, diff `3108507..f1a5f22` in `03-FormatJsonTest-3108507-to-f1a5f22.diff` (77 lines) [R].

1. Seeded speeds, commit `7ddd4ef` (now lines 145 to 161): when the speed profile is on, with probability one half, about two thirds of the nodes' messages get seeded `vmax`, `vmin`, `vmaxExplicit`, `vnode0`, `vnode1`, `extraTime`, the same values in both engines. No assertion is touched in this commit [R: the diff holds no changed assertion outside change 2].
2. `formatDecimal3`, commit `f1a5f22` (now lines 83 to 100): before, every tested float was compared with Java's `DecimalFormat("0.###")`. After, a finite value with magnitude at or above 2^63 must throw the port's `UnsupportedOperationException` whose message contains "2^63" and "RECORD -119"; every other value, the infinities and NaN included, is still compared with Java. A new assertion requires both sides of the limit to be reached.

**(b) What had been seen, and by whom.** Plumb, session 26 [?, as the record reports]: run 1, every JSON comparison equal and the test's own reached-branch check on the speed profile failing; run 2, `formatDecimal3` failing near 2.8e22, 17 digits on Java against 16.

**(c) The evidence, checked.**

- Seeded speeds: in Java only the kinematic paths write those fields (`KinematicPath.java:132`, `KinematicNoCostPath.java:127`; the default is -1 at `MessageData.java:31`) [R, for `vmaxExplicit`; the other five fields not grepped]. So an empty speed profile in both engines is a fact about Java and about the test's inputs, not about the port. The failing check was the test's own demand that a branch be reached; the change makes the test reach more. It cannot turn a wrong port green: the JSON comparison stayed exact. One side effect: the new draws come from the same seeded generator, so every later case in the stream differs from the cases fixed at `3108507` [R, the diff]. The comparison was already green on the old cases, so nothing was escaped.
- The limit: the evidence is the owner's ruling, quoted in the record as "Stop at the limit (Recommended)". I can read the ruling only in the record [?]. A ruling does not depend on the port's result in the sense that matters: it does not say the port is right above 2^63, it says the port must refuse there. The port does: `router/FormatJson.kt:271-278` throws for `binExp > 62` [R]. `formatDecimal3` has one caller, the "times" array (`FormatJson.kt:79`), matching `FormatJson.java:82-85` [R], on `getTime()`, a `float` (`OsmPathElement.java:47-48`) [R].
- The limit itself was found by watching the port fail, as I predicted. That does not make it arbitrary: 2^63 is where the port's exact integer arithmetic in a `Long` ends (`FormatJson.kt:273-274`) [R].

**(d) What the test can catch before and after.** Before: any digit difference at any magnitude; it failed on 16 of 25,294 values at or above 2^63 [?, RECORD -120's count]. After: any digit difference below 2^63; above, only that the port refuses. **A wrong port the changed test passes:** none that prints wrong digits. The loss is a different one: the port now throws where Java prints, so a track whose time is at or above 2^63 seconds fails to format. No route reaches that. By this repository's own testing rule, reducing coverage is the owner's to decide, and the owner decided it.

**(e) The record's account.** "the comparison and the reached-branch list are unchanged" for the seeding: accurate [R]. "the test checks every value below the limit and checks for the error above it": accurate [R]. "Not yet shown: that the port matches Java on every tested value from 3e17 up to 2^63": an honest gap, closed by -120's count (1,982 values, 0 differ) which I did not recompute [?]. "`FormatJson.java:82-85`" and "`OsmPathElement.java:47`": correct [R]. One imprecision: the entry says Plumb seeded the speeds "then" (after run 1) "in `7ddd4ef`"; the commit order fits [R, `git log`], the run order is the coder's word [?].

**Finding for 3.** Both changes justified. The seeding by the Java source; the limit by the owner's ruling, which is authority and not evidence, and is honestly labelled as a narrowing.

## 4. RECORD -126 and -127: `RoutingEngineStopTest` (step 8c, sessions 33 and 34)

**(a) The change.** Commit `af875c6`, `router/RoutingEngineStopTest.kt`, diff in `04-RoutingEngineStopTest-af875c6.diff` (34 lines) [R]. Three hunks and no more:

1. Line 86: `theTimeLimitReadsTheInjectedClock` expected "pass0 timeout after 30 seconds", now "target-island-check timeout after 30 seconds". This test has no oracle beside it: the port runs on a clock that reads 0 once and 30,001 after.
2. Line 243: `TIMEOUT_50`, the pattern FK9 holds both the oracle's and the port's message to, gains `target-island-check`.
3. The class comment that describes both.

The file has two commits in all, `124e6da` (red) and `af875c6` [C: `git log -- <file>`]. The order in `486588d..94db5d1` is: CI limit `3840315`, oracle-only evidence `47dcb5d` (23:56), the test change `af875c6` (23:57), the port `1d1488b` (00:33) [C: `git log`]. So the evidence and the change do precede the port's commit, as -127 says.

**(b) What had been seen, and by whom.** Plumb, session 33, with the port held as a patch: 8 of 9 pass, and the injected-clock test gives "target-island-check" where "pass0" was expected [?, the record's report]. FK9 had passed.

**(c) The evidence, checked.**

- `btools/router/RoutingEngine.java:961`: `findTrack("target-island-check", ...)`, inside a loop over the sections (`:952`) that runs before any pass; its last argument, `fastPartialRecalc`, is `false`, so the limit there is the whole `maxRunningTime` (`:1980`) [R]. A section whose first point is a direct waypoint is skipped (`:954`) [R].
- `:1979-1983`: the limit is checked at `_findTrack`'s loop head and the message is `operationName + " timeout after " + (timeout / 1000) + " seconds"` [R].
- So on a clock already past the limit at the first check, Java says "target-island-check". The old expected text was wrong by the Java. This stands without the port's result.
- The oracle-only run (`docs/audits/.../12-oracle-only-1ms-fk9.txt`, commit `47dcb5d`): expectation written before the run, 10 of 10 "target-island-check" at a 1 ms limit [R, the file]. Its scratch test was deleted and is in no commit, so the run cannot be repeated from the tree [R, the file says so]. I did not rebuild it [?]. The Java lines carry the point without it.

**(d) My planted error** [C: `./gradlew :brouter:jvmTest --tests '*RoutingEngineStopTest'`; excerpt in `04-run4P-no-limit-in-island-check.txt`]. In the scratch copy, the port's limit check (`RoutingEngine.kt:722`) was made to skip the operation "target-island-check". The build compiled (`compileKotlinJvm` ran, 0 ` e: `), 9 tests ran, 1 failed:

- `theTimeLimitReadsTheInjectedClock` FAILED: "expected:<[target-island-check] timeout after 30 se...> but was:<[pass0] timeout after 30 se...>".
- FK9 PASSED: "0 mismatches; first words of the messages, both sides: {pass0=20}".

The planted port gives exactly the text the test expected before the change. **So the old expected text would have passed a port that never applies the limit in the island check, and the corrected text catches it.** Change 1 made the test stronger, not weaker. The failure is one this plant can produce. The file was restored from a saved copy and compared equal with the code worktree [C: `cmp`].

**What the widened FK9 can and cannot catch.** Before: it refused "target-island-check" on either side. That refused a port whose limit strikes at once, and it would equally have refused the oracle's own correct answer on a machine slow enough for the island check to pass 50 ms. After: FK9 holds each side to the message's form, the kind, the empty track and `terminated`. **Wrong ports the widened FK9 passes:** a port whose limit strikes at once (Plumb's revert (b), reported in -127; I did not rerun it [?]); and a port with no limit in the island check (my run). The first is caught by the stopped-clock and injected-clock tests, the second by the injected-clock test. Neither of those has an oracle beside it.

**(e) The record's account.**

- -126, "The premise is false by the Java": right [R].
- -126, "The expected text was session 32's hand-written reading, never an oracle's output": consistent with the test, which calls only the port there [R, lines 82 to 89].
- -126, "FK9's pattern has the same gap ... It passes only while the island check finishes inside the 50 ms limit, on both engines": right by the Java [I].
- -127, "The test change is what -126 allowed and no more: the one expected text, the one pattern, and the class comment": accurate [R].
- -127, "widening FK9's pattern made FK9 blind to a time limit that strikes at once ... What still catches that error is the two tests on a driven clock, which have no oracle beside them": accurate, and candid. My run adds a second error FK9 is blind to.
- -127, "In CI both engines answered pass0 on 20 of 20": I read the same line in the `jvm` job of a later run, 43277598212 on `4e311ce` [C: `gh api .../jobs/131390061349/logs | grep '8c FK9'`]. The run -127 cites, 43128205990, I did not read [?].
- -127, decision 1, "A guard would be a second comparison change after a result": I read this differently. See should-fix.

**Finding for 4.** My prediction was wrong on the widening. Both parts are justified by the Java. The corrected text is the stronger test. The widening removed a check FK9 only had by accident of a wrong pattern, and the record says what was lost.

## 5. RECORD -148: `CollectOutreachersTest` (red since step 5b; -60, -61, -147)

**(a) The change.** Commit `041d8b5`, `mapaccess/CollectOutreachersTest.kt`, diff in `05-CollectOutreachersTest-041d8b5.diff`: 18 lines added, none removed [R]. After the existing round (collection, every-37th `canEscape`, `clearTemp`, map state traced), a second round on the same map: `collectOutreachers` on both engines, `canEscape` on every 37th node of the same list, each answer traced with `nodesCreated` and `lastVisitID` on both sides, `clearTemp`, the map state traced again, then the existing `compare(where, ot, pt)`. The assertion at line 134, `escapes[0] > 0 && escapes[1] > 0`, is untouched.

**(b) What had been seen, and by whom.** Since step 5b: "canEscape did not answer both ways: false/true 0/3587" [R, -60 and -61]. The counts come from the oracle's answers alone (`escapes[if (oe) 1 else 0]++`, lines 104 and 122) [R]. So the red was a statement about the oracle on these inputs. The port's result played no part in it.

**(c) The evidence, checked.**

- `RoutingEngine.java:2012` calls `collectOutreachers()`, `:2016` calls `canEscape(p3.getTargetNode())`, `:2020` calls `clearTemp()`, inside the check out of memory bounds (`:2008`) [R]. The engine does make these calls again at each later check.
- `btools/mapaccess/OsmNodesMap.java:129-163`: `canEscape` returns "true" as soon as it meets a node marked earlier, and otherwise walks the nodes again, counts them back down and vanishes them [R]. That fits Plumb's reading of why the first round never answered "false".
- CI, run 43277598212 on `4e311ce`, conclusion "success"; the `jvm` job prints "5b collectOutreachers with destination: 12 regions, nodes no longer reachable after it 60118, canEscape false/true 1616/5558" and both tests PASSED; without a destination "0/3587" [C: `gh api .../runs/43277598212/jobs` and `.../jobs/131390061349/logs | grep`]. The record's figures match. I did not read the iOS jobs' logs: their results are the record's, simulator-checked at most [?].

**(d) My planted error** [C: `./gradlew :brouter:jvmTest --tests '*CollectOutreachersTest'`; excerpt in `05-run5P-false-branch-does-not-walk.txt`, the region's description cut out by me]. The worry: a node already vanished has few links left, so the new "false" answers might run the false branch on single nodes only and prove little. In the scratch copy the port's `canEscape` false branch (`OsmNodesMap.kt:247`) was made not to walk on from the first node. The build compiled, 2 tests ran, 1 failed:

- the with-destination test FAILED on a second-round line: "oracle [F 1258 false 809 1076] port [F 1258 false 853 1076]", `nodesCreated` 44 apart;
- the without-destination test PASSED (it has no "false" answer, so it cannot see this).

So the second round runs the false branch over many nodes, and the trace compares its effect. Before the change no call answered "false", so no error in that branch could be seen by this test at all. The file was restored from a saved copy; every file under `brouter/src` and the build file compared equal with the code worktree afterwards [C: `cmp` over `git ls-files`].

**What the test can catch before and after.** Before: nothing in `canEscape`'s false branch; and it was red on its own reach assertion. After: the false branch on 1,616 calls, compared line by line. **A wrong port the changed test would pass:** none that the change lets through, since nothing was removed. What remains unseen by this test: `canEscape` on a map that has grown between two collections, and asked about the open paths' targets, which is what the engine does. The test asks at once, on the same map, about every 37th node of the earlier list. That state is covered, if at all, by the memory groups (-147's plant 1) [?].

**(e) The record's account.**

- "`CollectOutreachersTest`'s diff: 18 lines added, none removed. The assertion and the regions are as they were": accurate [R].
- "The added calls are the ones `RoutingEngine.java` makes in its memory path, made on both engines and traced on both": **true of the calls, too strong as written.** The engine makes them after more routing and asks about open paths' targets (`:2014-2016`); the test makes them back to back and asks about a fixed sample. It is the engine's sequence of methods on a state the test builds.
- -147's decision 2 said session 54 "finds, from one memory case, the map state at a call that answers false, and adds that region to the test". That is not what was done: no region was added. -148 says so in its own decision 1 ("A second state of the same regions carries out -61's call"). The substitution is disclosed; -148's "What was added" bullet does not mark it as a departure from -147.
- "-61 is closed in full ... the coverage assertion is met where the engine's own sequence makes both answers": met, yes [C, CI line]; "the engine's own sequence" with the caveat above.

**Finding for 5.** Justified. The red did not depend on the port, the assertion did not move, and the addition makes the test see a branch it could not see. My plant shows the new reach is real.

One thing outside the five: this test's failure message names the map file and the cell indexes of the region. I cut them from my evidence. Whether that message meets E1 is for the planner [?].

## Verdict

### Blocking

None. No change of the five turned a wrong port green that I could find, and three planted errors were caught where the changed tests claim reach.

### Should-fix

1. **-107, `SearchBoundaryTest`: take the stricter route.** With `-XX:-OmitStackTraceInFastThrow` on the test JVM, a test that compares the message on every throw passes [C, run 1B]. The committed test passes a port with a wrong out-of-range table row; the strict one fails it [C, runs 1P]. The flag changes the build, so it is the owner's call, as the planner said. Until then the cause of the null message stays inferred: it did not reproduce here.
2. **-116, `ReadBinaryTest`: assert the error path is reached.** `errorPaths` is printed and not asserted (lines 157 and 158). Without the two empty tracks the test stays green and tests nothing of the ruling.
3. **-116: the loosening is wider than its evidence.** It was argued for the null-pointer text and applied to every catch, 1,549 of 4,522 debug texts by the coder's count. Either narrow it on the JVM or record its breadth.
4. **-127, FK9: add the guard.** The planner refused it as "a second comparison change after a result". An assertion that is only added, predicted before it runs and seen red under revert (b), loosens nothing; the discipline is against moving a comparison toward a result. The guard would be: when the port reports a timeout, its own clock has passed the limit. This is the planner's ruling to revisit, not mine to make.
5. **-126: commit the oracle-only run.** The 1 ms evidence came from a scratch test that was deleted. Evidence for a changed comparison should be rerunnable from the tree.

### Notes

- -119: the limit is authorised by the owner, not shown by evidence, and nothing needs showing above it since the port refuses there. The count below the limit (-120: 1,982 values from 3e17 to 2^63, 0 differ) I did not recompute [?].
- -119: the seeded speeds draw from the same generator, so the cases after them are not the cases fixed at the red commit. Harmless here; worth knowing when a seed is called "fixed".
- -148: the second round is a state the test builds, not one the engine was caught in. -147's decision 2 asked for the latter.
- All my runs were on this machine's OpenJDK 21.0.12.1, not CI's JDK. Nothing here says anything about iOS; every iOS figure cited is the record's and is simulator-checked.
- The dispatch's five were each findable from their entries. The restricted object was not met.

### Where the record was inaccurate

1. -107: "not the 168,000 compared results". 1,380 of the 168,000 are throws compared by class only; 166,620 values are compared.
2. -116: silent on the breadth of the loosening (the end-of-file cuts too).
3. -148: "The added calls are the ones `RoutingEngine.java` makes" overstates; and the departure from -147's decision 2 is not marked where the addition is described.

Where the planner was right: every Java line cited in -116, -119, -126 and -148 that I read says what the record says it says; -127's account of its own ruling's cost is accurate; each of the five was correctly named as a change after a result, except that -107's first change and -148's addition are not loosenings at all.

### One line each

1. **-107: justified in part.** Wrapping the oracle's own throw is justified by `CheapRuler.java:57`; dropping the message comparison rests on an untested inference, hides no present error, and was avoidable.
2. **-116: justified by independent evidence.** `OsmTrack.java:286-290` appends the JVM's own exception text, which a port in common code cannot and should not copy.
3. **-119: justified in part.** The seeded speeds are justified by the Java source; the limit at 2^63 is justified by the owner's ruling, which is authority to narrow and not evidence, and which I could read only in the record.
4. **-126: justified by independent evidence.** `RoutingEngine.java:961` and `:1979-1983` put the first strike in the island check, and a planted port without that limit fails the corrected test where it would have passed the old one.
5. **-148: justified by independent evidence.** The red came from the oracle's own counts, nothing was removed, and a planted error in the newly reached branch is caught.

**Overall: justified in part.** Three of five stand on evidence independent of the port; two stand in part, one of them on the owner's word; none is "not justified".

## Predictions, scored

| # | Predicted | Found | Score |
|---|---|---|---|
| 1 | justified in part | justified in part | Hit. The reason held: a real cause, and reach lost that nobody restored. |
| 2 | justified by independent evidence | justified by independent evidence | Hit, though I guessed a harness defect and it was the JVM's wording. |
| 3 | justified in part | justified in part | Hit on the line. Half the reasoning missed: the range below the limit was closed by -120, not left untested. |
| 4 | justified in part (widening unjustified) | justified by independent evidence | **Miss.** The widening is what the Java requires; I took -127's candour about its cost for an admission that it was wrong. |
| 5 | justified by independent evidence | justified by independent evidence | Hit. The lower-confidence worry (setup steering away from the failing case) was tested by a plant and did not hold. |

4 of 5 on the line. Overall prediction, "at least two leave the test weaker than the record says": held for -107 (the count) and -116 (the breadth), weakly. Mechanism prediction for the revert check: not tested as written; in its place, FK9 passed a planted port as predicted in spirit, and the driven-clock test caught it.

## Not verified

- Anything on iOS (the record's claims, simulator-checked).
- The order of the coder's local runs within a session, wherever git shows one commit.
- -120's counts for `formatDecimal3`; -127's revert (b); CI run 43128205990; Forager's `pre-main` never setting `rawTrackPath`.
- The JIT cause of -107's null message: supported, not reproduced.
- The owner's words in -119, read only as the record quotes them.
