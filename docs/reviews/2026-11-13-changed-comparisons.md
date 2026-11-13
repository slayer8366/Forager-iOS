# Review: five comparisons changed after a result was seen

Reviewer: Samphire Witness. Dispatch 2026-11-13-01 from Cedar Datum. First review.

Status: PARTIAL. Done: 1 (-107), 2 (-116), 3 (-119). Untouched: 4 (-126), 5 (-148). Verdict and scoring not yet written.

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
