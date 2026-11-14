# Review: the test changes made since RECORD -163 (sessions 60, 62, 64, 65, 66, 67, 68)

Reviewer: Samphire Witness. Second review. My first is `docs/reviews/2026-11-13-changed-comparisons.md`.

Status: METHOD AND PREDICTIONS ONLY. Nothing below has been read or run yet beyond the list of commit subjects.

Code under review: branch `brouter-port-5b` of slayer8366/Forager-iOS at `e9eeddb522f536d76c04c0b2f606d19897777404`, in a detached worktree of my own.
Record under review: `RECORD.md` on `origin/ccr-8ee6e614-32enzt`, entries -163 to -174, treated as claims.

## Marks

- [R] read at source by me (file and line given).
- [C] computed or run by me (command or run given).
- [I] inferred.
- [?] not verified.

A sentence carries one mark. Read, run and inferred are not mixed.

## Method

For each session's change, four questions:

1. Does the test assert what its record entry says it asserts?
2. Could a wrong port pass it? If so, name the wrong port.
3. Did the change loosen anything beyond what was stated?
4. Is my should-fix item closed, as -167, -170, -171 and -173 claim?

For session 68 also: does `RouteWays.kt` match Forager's walk (`BRouterRouteWays.kt` lines 59 to 123), is the claim "a body passing a literal `false` for reverse would pass" true, and how much does it matter.

Steps, in order:

1. Read each diff from the commits themselves, then the findings file, then the record entry. The entry is read last so that I form a view of the diff before I am told what it does.
2. Where a claim is testable on the JVM, test it with a scratch plant in my worktree: one line edited, the affected class run with Gradle on the JVM target, the JUnit XML read.
3. A plant counts only if: the build log has 0 lines containing ` e: `; the XML is newer than the edit; the failure is one this edit could cause; and the file is restored from a copy I saved before editing (not from git), with `cmp` equal afterwards.
4. I choose plants the coders did not run. I read their plant lists first to avoid repeating them.
5. I cannot run the iOS simulator here. Anything about iOS is taken from CI logs or the record and is written "simulator-checked".
6. I change nothing on `brouter-port-5b`, commit nothing in the code worktree, and do not contact the coder.

## Predictions (made from the dispatch's one-line descriptions and the commit subjects only)

These are predictions, not findings.

| Session | Claim in short | Predicted verdict | Why I guess so |
|---|---|---|---|
| s60 | 12 printed numbers compared by value on the simulator | justified in part | The ruling gives authority. I expect the "value" comparison to be done by putting both texts into a canonical form, and I expect that form to be wider than twelve places: a canonical form applied by pattern will also cover numbers nobody listed. I also expect the JVM comparison to have stayed by text. |
| s62 | JVM flag and strict `SearchBoundaryTest` | justified by independent evidence | This is my own should-fix 1, and I ran the strict form myself in the first review. Risk: the flag was put on one task only, or the strict comparison keeps an escape. |
| s64 | common sibling held to F0's stored row | justified in part | A stored row is a number written down from a past run. If the row was produced by the port, the test holds the port to itself. I expect it to catch a change, not an error present from the start. |
| s65 | FK9 guard with `RecordingClock` | justified by independent evidence | Added assertion, loosens nothing. Risk: the guard reads a clock that the test itself drives, so it is true by construction. |
| s66 | floors in `ReadBinaryTest`, loosening narrowed to one text | justified in part | A floor (`>=`) closes "never reached" and leaves "reached less". I expect the narrowed text to be exact for JDK 21 and expect a question about what happens on another JDK. I expect the breadth floor to be weaker than an equality the coder could have written. |
| s67 | oracle-only 1 ms test, holds `openSet` monitor by reflection | justified in part | It makes the evidence rerunnable, which closes my item. But a test that holds a private monitor of the oracle is fragile, and it tests the oracle, not the port; I expect it to pass whatever the port does (which is right for its purpose) and to depend on timing. |
| s68 | `readRouteWays` and `RouteWaysEntryTest` | justified in part | The coder's own claim says a literal `false` for reverse passes, so one input of the walk is not exercised. I expect the body to match Forager's walk on the main path and the test data to lack a case where reverse matters. |

Should-fix closure, predicted: 1 closed; 4 closed; 2 closed in part (floor, not exact); 3 closed; 5 closed.

Overall prediction: no blocking finding; two or three should-fix findings, most likely on s60 (width of the canonical form), s66 (floor against equality) and s68 (the reverse flag).

Mechanism prediction for my plants: a plant in a number's low digits will pass the s60 simulator comparison only if the canonical form rounds; on the JVM, where I can run, it will fail because the JVM still compares text.

## Baseline

The first run of this review was lost when the container restarted. Only the sections above had been pushed (`b231a5b`). They are kept as they were. Everything from here on was written after the restart.

State found on resuming, each checked [C]:

- My clone on `review/changed-comparisons` at `b231a5b`, equal to the remote head by `git ls-remote`, tree clean.
- The code worktree at `/home/user/forager-ios-review-wt`, detached at `e9eeddb`: `git status` clean, `git diff` empty. No edit from a lost plant was left behind, so nothing had to be restored.
- `origin/brouter-port-5b` had moved from `e9eeddb` to `2184691` (two commits, session 69).

**Session 69 was added to this review after the predictions above were committed.** It has no row in the prediction table and I do not add one after the fact. What I expected of it is written under "Plants" below, before any plant ran.

Baseline run at `e9eeddb`, no edit [C: `./gradlew :brouter:jvmTest --tests '*SearchBoundaryTest' --tests '*EngineStopCommonTest' --tests '*RoutingEngineStopTest' --tests '*ReadBinaryTest' --tests '*RouteWaysEntryTest'`]: `BUILD SUCCESSFUL`, 0 lines containing ` e: `, 5 XML files all newer than the start stamp, 20 tests, 0 failures, 0 errors. JDK: this container's OpenJDK 21.0.12.1, not CI's.

- `ReadBinaryTest` printed `116 null-pointer, 1433 other`, 32 tracks.
- `RoutingEngineStopTest` FK9 printed 10 cases, 0 mismatches, and the port's clock per case from 51 to 88 ms.
- The oracle-only test printed `{target-island-check=10}` and `{family:timeout=10}` for both the plain and the held run.
- `SearchBoundaryTest` printed `radius throws=1380`, `distance throws=1368`.

## Plants: what I will run, and what I predict, written before any plant ran

Each plant is one or two exact-string edits in my worktree, run through a script of my own (`plant.sh`, not committed): it refuses a dirty tree, saves a copy of the file, checks the string occurs once, edits, touches a stamp, runs Gradle on the JVM target, restores from the saved copy, and prints `cmp`, the count of ` e: ` lines and every XML newer than the stamp with its failure messages. I read the coders' plant lists first; none of these was run by them, except P69 which session 69 designed and could not run.

| Plant | Session | Edit | Classes run | Prediction |
|---|---|---|---|---|
| P62 | s62 | `SearchBoundary.kt:40`: the `isInBoundary` distance call rethrows an index error with the same class and one space added to its message | `SearchBoundaryTest` | FAIL at the first `isInBoundary` throw, message `... isInBoundary, message`, the two texts differing by the trailing space. This is the line s62 added at test line 61; nobody has seen it red (the coder's P2 failed at `direction 3: message`, the line I had already run strict in review 1). |
| P64 | s64 | `RoutingEngine.kt:724`: the limit strikes on elapsed time plus 30 per link processed (a limit that counts work, not time) | `EngineStopCommonTest` | 5 tests, 1 failure, the new sibling: expected F0's row, actual `family:timeout|0|0|0|...`. `theTimeLimitReadsTheInjectedClock` passes. If the sibling passes, fewer than 1,000 links are processed before the route ends, and I say so. |
| P65 | s65 | `RoutingEngine.kt:724`: `> timeout` becomes `> timeout / 5` (a limit five times too short) | `RoutingEngineStopTest`, `EngineStopCommonTest` | FK9 fails with the guard's reason and no other, on all or nearly all of 10 cases, elapsed near 11 ms. Every other test in both classes passes: the driven-clock tests jump straight past the limit and the stopped-clock tests never move. So the guard would be the only thing between this wrong port and green. |
| P66 | s66 | `ReadBinaryTest.kt`: one empty track in place of two | `ReadBinaryTest` | FAIL on `null-pointer case (the empty tracks) reached N times, floor 74` with N between 45 and 65. The coder inferred "about 53" and did not plant it. |
| P67 | s67 | test file, two edits: the held run at 50 ms in place of 1 ms, and `HOLD_MS` 60 in place of 5 | the one method | PASS, with the plain line (still 1 ms) unchanged and the held line `{target-island-check=10}`. This repeats the coder's unrecorded scratch check in a form of my own; with FK9's first words at 50 ms being `pass0` in my baseline (`{pass0=20}`), a held run that answers island check shows the hold is what moves the strike. |
| P68a | s68 | `RouteWays.kt`: `val reverse = false` | `RouteWaysEntryTest` | PASS, 3 of 3, the two digests unchanged (`069e566d...`, `45a3d729...`). By my reading of `OsmNode.addLink` no described link on this walk can be reverse, on any data. |
| P68b | s68 | `RouteWays.kt`: `decodeGeometry(it, node, target, !reverse)` | `RouteWaysEntryTest` | FAIL, 2 of 3: `dropoffPoints` and `f0hEnds` on field `[iLat]` or `[iLon]`; the divisor test passes. The coder's plant was on the description only. |
| P60 | s60 | `FormatGpx.kt:281`: a track point's `<ele>` text gets a `0` appended (same value, another text) | `FormatDigestsCommonTest`, `FormatDigestsJvmTest`, `FormatNumberTextTest`, `HintsFormatsLogicTest`, `FormatGpxTest` | The two digest tests PASS (this is the stated loosening, and it shows the stored length no longer pins the text at these places). `HintsFormatsLogicTest` FAILS on GPX. `FormatGpxTest` fails if it prints an elevation. |
| P69 | s69 | at `2184691`, `RouteWays.kt`: the two lines that add the target point removed (session 69's own designed plant, never run) | `RouteWaysCommonTest`, `RouteWaysEntryTest` | `f0hEndsReadAsStored` FAILS listing, for each of 16 rows, `points stored/(stored minus links)` and `digest`; no `links`, `cells` or `divisor` entry. `RouteWaysEntryTest` fails on `[point count]`. |

Two scratch tests, not plants (untracked files, deleted after):

- S60: over the 69 rows, count how often each of the twelve by-value places occurs, how many rows have `showspeed`, and print the test JVM's `-XX` arguments. Prediction: `<brouter:speed>` occurs 0 times and no row has `showspeed`, so the `Float` form is never exercised on real rows; the flag `-XX:-OmitStackTraceInFastThrow` is on the command line.
- S69: at `2184691`, run the Java walk (copied from `RouteWaysEntryTest.oracleRead`) on the F0h ends and compute session 69's row digest from the Java side directly. Prediction: all 16 equal the stored rows. If so, the stored rows are held to the Java by a run of mine, not only through the chain of two tests.

## Session 65: the FK9 guard (`RecordingClock`, `clockGuard = true`), RECORD -170

**Verdict: justified by independent evidence.** My should-fix 4 is closed.

What the change is [R, commit `890bc42`, `router/RoutingEngineStopTest.kt` at `e9eeddb`]: a private `RecordingClock` (line 196) wraps `EngineClock.MONOTONIC`, returns its value unchanged and keeps the first read, the last read and the count. `againstOracle` takes `clockGuard` (line 230); only FK9 passes `true` (line 51). One reason is added to the mismatch list (line 251): when the port's kind is `family:timeout`, last read minus first read must be greater than the limit. Nothing is removed and no existing comparison is changed [R, the diff: 35 lines added, 3 changed, all three the call sites that pass the clock].

Does it assert what -170 says: yes [R]. -170's wording, "last read minus first read must be greater than the limit (50 ms), and a port that never read its clock fails too", is the code at lines 251 to 253.

Is it true by construction (my predicted risk): on a correct port, yes, and that is what a guard should be. The port reads its clock at four places and no other [C: `grep -rn nowMillis brouter/src/commonMain`: `RoutingEngine.kt:85`, `:549`, `:719`, `:724`], and the test does not drive the clock, it only watches it [R, lines 196 to 213]. So the guard is not circular: it can be false, and it was.

Runs:

- Baseline, no edit [C]: FK9 10 cases, 0 mismatches, the port's clock 51 to 88 ms per case.
- P65, a limit five times too short (`> timeout / 5` at `RoutingEngine.kt:724`) [C: build 0 ` e: `, `compileKotlinJvm` ran, 2 fresh XML, file restored, `cmp` equal]: `RoutingEngineStopTest` 10 tests, 1 failure, FK9: "8c FK9: 8 mismatches", each with the one reason "port's clock not past the limit at its timeout (11 ms over N reads)". `EngineStopCommonTest` 5 tests, 0 failures. Every other test in both classes passed this wrong port. The failure names 11 ms, which only a 10 ms limit gives, so it is this edit's.

Findings:

- **Note (65-1): the guard caught 8 of 10 cases, not 10, and which ones is a timing** [C, P65's print line: per case `11 ms/32 reads, 93 ms/2 reads, 51 ms/2 reads, 11 ms/10 reads, ...`]. On two cases the port's first check of the limit came 51 and 93 ms after its start, so the short limit struck there with the clock already past 50 ms and the guard passed them. I predicted "all or nearly all"; it held as nearly all. On a machine where the set-up before the first check takes over 50 ms on every case, this wrong port would pass FK9, guard included. The coder's file names the related gap (a strike early in re-tracking after slow passes) and -170 repeats it; neither names this one, that the guard's reach depends on the set-up being faster than the limit.
- **Note (65-2): on the JVM the guard is the only test that sees a limit that is too short on a real clock** [C, P65: 14 other tests in the two classes passed]. The driven-clock tests jump from 0 to 30,001 against 30,000, so any limit from 1 to 30,000 passes them [R, `RoutingEngineStopTest.kt:83-87`, `EngineStopCommonTest.kt`'s copy]. A limit that is too long is seen only by those driven-clock tests, and only if it exceeds 30,001 [R]; the guard passes it by design.
- Nothing loosened [R].

## Session 66: `ReadBinaryTest` floors and the narrowed loosening, RECORD -171

**Verdict: justified by independent evidence.** My should-fix 2 and 3 are closed. My prediction was "justified in part"; that was a miss on the line, though the two things I expected to find are both there as notes.

What the change is [R, commits `2fb0531`, `aceedc4`, `router/ReadBinaryTest.kt` at `e9eeddb`]:

- Two counters split the error path by the oracle's text after the prefix; two assertions are added after the old reach assertion, which is unchanged: `nullPointerPaths >= 74` (line 188) and `otherErrorPaths >= 6 * tracks.size` (line 189).
- The prefix-only comparison of -116 now applies only when the oracle's text after the prefix equals one constant, JDK 21's text for the `last_pe` null pointer (lines 26 and 167). Every other error-path text is compared whole.

Does it assert what -171 says: yes [R]. -171's caveat on the floors is careful and right: "they would miss one empty track dropping out only if 37 or more of its reads remained".

Runs:

- Baseline [C]: 32 tracks, 116 null-pointer, 1,433 other, pass. So all 1,433 end-of-file texts are equal whole on this JVM, and each of the 116 null-pointer texts is exactly the constant (anything else would have been compared whole against the port's bare `java.lang.NullPointerException` and failed) [I, from the pass and line 167].
- P66, one empty track in place of two [C: 0 ` e: `, `compileTestKotlinJvm` ran, fresh XML, restored, `cmp` equal]: FAILED, "null-pointer case (the empty tracks) reached 69 times, floor 74". The print: 31 tracks, 69 null-pointer, 1,396 other. The message names the floor this edit moves, so it is this edit's.

Findings:

- **Note (66-1): the floor caught the loss of one of the two empty tracks by 5 reads** [C, P66: 69 against 74]. The coder's file says "One empty track dropped gives at most about 53", marked inferred and not planted; planted, it is 69. I predicted 45 to 65 and was also low. The floor is 37 per empty track by the set-up's structure, summed to 74, but one track alone can reach nearly that on this seed. A floor per empty track (each of the two reaches at least 37) would say what the structural argument says and would not depend on the sum. The item I raised, that the error path is asserted reached, is closed either way: with both empty tracks gone the coder saw 0 against 74.
- **Note (66-2): the narrowing fails closed** [R, line 167]. On a JDK that words the null pointer differently the constant does not match, the text is compared whole against the port's `java.lang.NullPointerException`, and the test goes red. That is the right direction. It also means this test is tied to JDK 21's wording; CI and this container both print it [C here; CI's is the coder's and planner's reading].
- **Note (66-3): in the null-pointer case the port's text after the prefix is still unconstrained beyond being non-empty** [R, lines 153, 154 and 167]. That is -116's ruling, unchanged, and now it is as narrow as its evidence. A port that reached the catch through another exception on an empty track would still pass if the result fields agree. Named in my first review; not new.
- Nothing loosened: the only change to a comparison makes 1,433 texts compared whole that were compared by prefix [R].

## Session 67: the oracle alone at 1 ms, held by its `openSet` monitor, RECORD -173

**Verdict: justified by independent evidence.** My should-fix 5 is closed, and by a better test than the one I asked for.

What the change is [R, commit `548a20b`, `router/RoutingEngineStopTest.kt:124-190`, `:351`]: one test and one helper are added, nothing else changes. The helper reads the oracle's private `openSet` by reflection, takes its monitor, starts `doRun(1)` on a thread, polls `ThreadMXBean` until that thread is `BLOCKED` on that object, waits `HOLD_MS` (5) on `System.currentTimeMillis()`, and releases. The test asserts every kind is `family:timeout` and every first word is `target-island-check`. A plain `doRun(1)` is run beside it and printed, not asserted.

The Java it rests on, read by me in Forager's vendored v1.7.10 (`third_party/brouter/src/main/java/btools/router/RoutingEngine.java`; its README names tag `v1.7.10`) [R]:

- `:201` `startTime = System.currentTimeMillis()` at the top of `doRouting`.
- `:961` `findTrack("target-island-check", ...)`.
- `:1965` `synchronized (openSet)` in `_findTrack`, before the loop; `:1979-1983` the limit check at the loop head, `System.currentTimeMillis() - startTime > timeout`, strict.
- The other uses of `openSet` before that point take no monitor (`:341` and `:841` are bare `clear()` calls); the other `synchronized (openSet)` blocks are inside the loop (`:1986`) or in getters (`:2353`, `:2363`) [C: `grep -n openSet`].
- So the first monitor the run meets is `:1965`, `startTime` is already taken, and after a 5 ms hold the first check sees the limit passed. The coder's reading is right. That the jar's line table matches this source is the coder's `javap` reading, which I did not repeat [?].

The coder's finding that a plain 1 ms run is a timing, not a fact of the Java, is right [R, `:1981` is strict and the island check is bounded], and it corrects my own first review, which treated session 34's 10 of 10 as evidence without saying it was a timing.

Runs:

- Baseline [C]: plain and held both `{target-island-check=10}`, `{family:timeout=10}`.
- P67, two edits in the test file: the held run at 50 ms in place of 1 ms and `HOLD_MS` 60 [C: 0 ` e: `, `compileTestKotlinJvm` ran, fresh XML, 1 test, 0 failures, restored, `cmp` equal]: held `{target-island-check=10}`, `{family:timeout=10}`. Without a hold the oracle at 50 ms answers `pass0` on all 10 FK9 cases [C, baseline FK9: first words `{pass0=20}` over both sides; P65's run: the oracle's 10 all `pass0`]. So the hold is what moves the strike into the island check. This is the coder's uncommitted scratch check, repeated independently in another form.

Could a wrong port pass it: every port passes it; it runs no port code [R]. The coder's file says so. It is evidence for -126's corrected text, not a test of the port.

Findings:

- **Note (67-1): the committed test cannot show its own mechanism on these machines** [C: plain and held agree at 1 ms here; the coder reports the same in CI]. With `HOLD_MS` set to 0 it would still pass here [I]. The record says this ("the committed test alone does not show the hold doing anything"). My P67 and the coder's scratch run are the evidence that the hold works, and neither is in the tree. If that matters later, the cheapest fix is a second held case at a limit where the plain run is known to reach `pass0`, asserted to differ.
- **Note (67-2): it fails closed** [R, lines 176 to 181]: a thread that ends or never blocks on that object fails the test with the case named; there is no fallback to an unheld run. Its dependence on a private field name and on `ThreadMXBean` reporting lock identity is named in -173.
- Nothing loosened [R: 73 lines added, none removed].

## Session 68: `readRouteWays` and `RouteWaysEntryTest`, RECORD -174

**Verdict: justified by independent evidence.** My prediction was "justified in part"; a miss on the line. The reverse flag, where I expected the gap, turns out not to be a gap.

**Does `RouteWays.kt` match Forager's walk** (`BRouterRouteWays.kt:59-123`, read only, at Forager `137b1427`, where `read` begins at line 59) [R, both files, line against line]:

| Forager | Port (`RouteWays.kt` at `e9eeddb`) | Same? |
|---|---|---|
| 60-62 way context on `lookups.dat` alone | 60-62 | yes |
| 72 one `GeometryDecoder` per read | 63, one per call (per square) | yes within a square; across squares see 68-2 |
| 77-78 `DataBuffers`, `PhysicalFile(file, buffers, -1, -1)` before the `try` | 64-65 | yes |
| 80 divisor check, throws Forager's message | 68 returns `OtherDivisor`, reads no cell | same effect; the message stays Forager's, as -172 says |
| 82 `OsmFile(square, cell * CELL / 1_000_000, ...)` | 74, `cell = 1_000_000L / square.divisor`, `Long` arithmetic as Forager's | yes |
| 83 `hasData()`, 84 `createMicroCache(lon, lat, buffers, null, null, true, null) ?: continue` | 75, 77 | yes |
| 85 `OsmNodesMap()` per cell | 78 | yes |
| 86-90 `getIdForIndex`, `getAndClear`, `OsmNode(id)`, `parseNodeBody` | 94-98 | yes |
| 91-110 each link by `getNext(node)`, only with a description; points: node, decoded chain, target | 99-117 | yes |
| 106-108 end-pair dedupe, `tagsOf`, `positionOf` | not in the port | left to Forager, as -172 says |
| 115 `square.close()` in `finally` | 82 | yes |

One addition: `require(lonCells.size == latCells.size)` (line 58) [R]. One difference in kind: `link.getTarget(node)!!` (line 103) where Forager's Java type is not null [R]; the same effect.

**Is "a body passing a literal `false` for reverse would pass" true:** yes [C, P68a: `val reverse = false`; 0 ` e: `, `compileKotlinJvm` ran, fresh XML, 3 tests, 0 failures, both printed digests equal to the baseline's, restored, `cmp` equal].

**How much it matters: not at all, on this walk, for any data.** The coder's file says "The data cannot tell those apart". It is stronger than that: no data could [R]. In `OsmNode.addLink` (Java `OsmNode.java:155-190`, port `OsmNode.kt:148-193`) a link gets a description only at `if (!isReverse)`, and the link it is set on is either new (then `addLink(link, false, tn)` sets `n1 = this`, Java `:92-94`) or an existing link reused under `l.descriptionBitmap == null && !l.isReverse(this)` (Java `:168`). `isReverse(source)` is `n1 != source && n1 != null` (`OsmLink.java:123-124`). The walk parses each node as a fresh `OsmNode(id)` that is never put in the nodes map, so its link chain holds only links made in its own `parseNodeBody`. So every described link reached from `node.firstlink` has `n1 == node`, and `link.isReverse(node)` is false. The same holds in Forager's own reader. Passing `link.isReverse(node)` is fidelity to Forager's line; a literal `false` is the same function here. I confirmed the count on the Java side for F0h under session 69 below.

**Could a wrong port pass the test:** not one I could build. P68b, the geometry decoded in the other direction (`decodeGeometry(it, node, target, !reverse)`) [C: 0 ` e: `, `compileKotlinJvm` ran, fresh XML, restored, `cmp` equal]: 3 tests, 2 failures, "dropoff walker: square 1 of 1, link 5 of 8: field [iLat] at point 2" and "F0h inside-across-the-degree-line/from: square 1 of 1, link 1 of 405: field [iLat] at point 2"; the divisor test passed. The coder's plant was on the description, mine on the points; both fields bite. The test compares against a live Java walk in the same JVM, link by link (`RouteWaysEntryTest.kt:117-125`), with the link count checked first, so a dropped or added link fails too [R].

Findings:

- **Note (68-1): the reverse flag is dead on this walk by the engine's own code, not by a shortage of data** (above). The "Not checked" line in the coder's file and any later brief that asks for "a described link with reverse true" asks for something that cannot exist here.
- **Note (68-2): one way context and one decoder per square call, where Forager shares one across squares** [R, `RouteWays.kt:60-63` against Forager 60-62 and 72]. No point in either set spans two squares [C, baseline: 15 calls for 15 points, 16 for 16], so this is argued from the code, not run. The coder says so. I read `GeometryDecoder`'s cache as keyed on array identity and did not find a way for it to differ, but I did not run it [I].
- **Note (68-3): the entry cannot check that a cell lies in the square it is given** [R, `RouteWays.kt:23-25`]. Stated in the request's doc and in the coder's file. A caller that passes a cell from another square gets plausible links from the wrong place. Forager groups by square first, so its adapter cannot do this; a new caller could.
- The file-handle leak when `PhysicalFile`'s constructor throws is the coder's finding, the same in Forager and in Java; I did not check it [?].
- Nothing loosened: a new entry and a new test [R].

## Session 64: the stopped-clock sibling in `EngineStopCommonTest`, RECORD -169

P64 ran and my prediction for it failed, so two follow-up plants are predicted here before they run.

- P64, the limit strikes on elapsed time plus 30 per link processed (`RoutingEngine.kt:724`) [C: 0 ` e: `, `compileKotlinJvm` ran, fresh XML, restored, `cmp` equal]: `EngineStopCommonTest` 5 tests, **0 failures**. The new sibling passed this wrong port and printed F0's row. I predicted a failure and named the alternative: fewer than 1,000 links are processed before the route ends. That is what happened [I, from the pass: 30 per link against 30,000].
- P64b, predicted now: the same edit at 300 per link, `EngineStopCommonTest`. The sibling FAILS with `family:timeout|0|0|0|...` against F0's row (the route needs more than 100 links); the other four pass.
- P64c, predicted now: the P64 edit (30 per link) against the JVM's own stopped-clock test alone, `forager30sLimitOnAStoppedClockRoutesAsTheOracleWithNoLimit`, which runs FK10's first case against a live oracle. It FAILS (oracle a route, port a timeout), because FK10's routes are long ones. If it passes, the JVM test is as short-sighted as the sibling for this kind of wrong port.

Results of the two, and the verdict:

- P64b, 300 per link [C: 0 ` e: `, `compileKotlinJvm` ran, fresh XML, restored, `cmp` equal]: 5 tests, 1 failure, the new sibling: "30 s limit on a stopped clock against expected-1b.tsv expected:<[route|226|5302|5047|...]> but was:<[family:timeout|0|0|0|...]>". As predicted.
- P64c, 30 per link, the JVM test alone [C: same checks]: 1 test, 1 failure: "FK10-0005: oracle route ... 15925 links ...; port family:timeout ... 1001 links", the port's message `pass0 timeout after 30 seconds`. As predicted. 1,001 links is exactly where 30 per link passes 30,000, so the failure is this edit's.

**Verdict: justified by independent evidence.** My prediction was "justified in part", on the guess that the stored row was the port's own. That guess was wrong.

Where the row comes from [R]: the test reads `F0EngineCommonTest.CASES.first()` (`EngineStopCommonTest.kt:78`). Those constants are F0's row of `brouter/src/jvmTest/resources/e3/expected-1b.tsv` (line 9 of that file, field for field equal to `F0EngineCommonTest.kt:52-53`: kind, nodes, cost, distance, both digests, the tuple digest). That file is written from the oracle, and `RoutingEngineF0Test.kt:27-32` holds a live oracle run to it on every JVM run. So the sibling holds the port to the Java, at one remove, not to itself.

Does it assert what -169 says: yes [R, commit `2477888`: one test added, the header comment corrected, nothing else]. It compares six fields and the tuple digest. -169 and the coder's file both say plainly what it cannot see (a limit that never strikes; a moving clock).

On the simulator: the test ran by name at `2184691`, `forager30sLimitOnAStoppedClockRoutesAsF0sStoredRow[iosSimulatorArm64]`, and printed F0's row and tuple digest; `EngineStopCommonTest` 5 tests, 0 failures [C: my reading of CI job 132262700481's log]. Simulator-checked. I cannot run the simulator here.

Findings:

- **Note (64-1): the sibling's reach is one short route** [C, P64 against P64b and P64c]. A limit that counts work instead of time passes it at 30 per link and fails it at 300; the JVM's stopped-clock test, on a route of 15,925 links, fails the same wrong port at 30. So the sibling sees gross errors (a limit that strikes at once, the coder's plant; a fast counter, mine) and not a slow drift. The port is common code and the JVM test runs on every push, so this wrong port would not get through; the sibling alone would not stop it.
- Nothing loosened [R: 36 lines added, 2 changed in a comment].

## Session 62: the JVM flag and the strict `SearchBoundaryTest`, RECORD -167

**Verdict: justified by independent evidence.** My should-fix 1 is closed.

What the change is [R, commit `5ca6310`]: `SearchBoundaryTest.kt` gains a message comparison on every `isInBoundary` throw (now line 61) and compares the message on every `getBoundaryDistance` throw (the `IllegalArgumentException` condition is gone, line 70). `brouter/build.gradle.kts:160-162` adds `jvmArgs("-XX:-OmitStackTraceInFastThrow")` inside the `jvmTest` block only, not in `oracleJvm()`, which `e3Strata` shares. Sampling, seeds and the reach assertions are untouched. No port file changes.

Does it assert what -167 says: yes [R].

Runs:

- Baseline with the flag [C]: pass, `radius throws=1380`, `distance throws=1368`. So 2,748 throws are compared by class and message and are equal.
- **The flag is on the running test JVM** [C: a scratch test of mine printed the JVM's input arguments: `-XX:+UnlockDiagnosticVMOptions, -XX:-UseLibmIntrinsic, -XX:-OmitStackTraceInFastThrow`]. -167 and the coder both list this as not seen; it is seen now, on this machine.
- P62, `SearchBoundary.kt:40`: the `isInBoundary` distance call rethrows an index error with the same class and one space added to its message [C: 0 ` e: `, `compileKotlinJvm` ran, fresh XML, restored, `cmp` equal]: FAILED, "case 153 probe 0 cost 0 radius 8204: isInBoundary, message expected:<...unds for length 1800[]> but was:<...unds for length 1800[ ]>". As predicted. This is the assertion session 62 added; the coder's own plant P2 failed at `direction 3: message`, which is the line I had already run strict in my first review, so line 61 had not been seen red before this.

Could a wrong port pass it: not the one I named in the first review (a wrong out-of-range row), on either method now [C, the coder's (b) and P62].

Findings:

- **Note (62-1): the cause of -107's lost message is still not reproduced** [?]. It did not reproduce for me in the first review with or without the flag, nor for the coder. The flag guards against an inferred cause. -167 says this.
- **Note (62-2): the flag changes the JVM for every `jvmTest` class, not only this one** [R, `build.gradle.kts:160-162`]. It can only add messages and stack traces to exceptions that would have lost them, so I see no way for it to turn a wrong port green [I]. Session 66's exact null-pointer text leans on it: without the flag a fast-thrown null pointer has no message [I]. CI's totals did not move (-167, the planner's reading); nobody compared the tests one by one, and neither did I [?].
- Nothing loosened; one comparison restored and one added [R].

## Session 60: the twelve printed numbers compared by value on the simulator, RECORD -163 decision 5, -164, -168

**Verdict: justified in part.** The part that is justified: the change does what the ruling says, a changed value is still caught on real routes on the simulator, and everything outside the twelve places is still compared as text. The part that rests on authority and not on evidence: how much text the comparison now lets through at those places, which is more than the difference the ruling describes.

The owner's ruling, verbatim from -163: "Record it, compare by value (Recommended)". The option read: "Leave the port's printing as it is and record this as a known platform difference: on iOS a printed number in these exports can differ from Java's in its last digit while meaning the same value. The simulator test that is red now is changed to compare those numbers by value, and that change goes to the independent reviewer. Cost: about one short session. iOS exports would then not be byte-for-byte identical to Java's in the 12 places that print a number this way." The ruling stands. Nothing below re-reads it; where a finding touches it, the finding goes to the planner and the owner with these words beside it.

What the change is [R, commits `f0c21b9`, `28e2a79`]:

- `commonTest/.../tiles/FormatNumberText.kt`: for GPX, the content of every `<ele>`, `<locus:rteDistance>`, `<locus:rteTime>`, `<locus:rteSpeed>` is replaced by the 16 hex digits of `toDoubleOrNull()?.toRawBits()`, and of every `<brouter:speed>` by the 8 of the `Float`'s; for JSON, the third number of a `"coordinates"` entry. A text that does not parse stays text.
- `FormatDigestsCommonTest` and `FormatDigestsJvmTest` take GPX's and JSON's length and digest over that canonical text. Hints, KML and CSV are compared as before.
- The 69 stored rows' GPX and JSON values were regenerated from the port's run on the JVM. `HintsFormatsLogicTest`, the port's text against the oracle's on the JVM, is unchanged.

Is the form wider than the twelve places (my prediction): **no, that part of my prediction failed** [R]. The recognition is by element name and by JSON structure, and I checked each name against the port: every `<ele>` in `FormatGpx.kt` is one of the five listed (lines 167, 200, 281, 512, 534 at `e9eeddb`); `<locus:rteDistance>`, `<locus:rteTime>`, `<locus:rteSpeed>` occur once each (171, 176, 177); `<locus:rtePointAction>` and `<locus:rteComputeType>` are not in the list and stay text; `"coordinates"` opens three arrays in `FormatJson.kt` (104, 186, 224), all listed. The first two numbers of a JSON point, the positions, stay text. I also predicted the JVM comparison stayed by text: it did, in `HintsFormatsLogicTest`; `FormatDigestsJvmTest` moved to the canonical form with the stored rows, as it must.

Runs and CI:

- A changed value is caught on the simulator [C: my reading of CI job 132027752003's log, run 43466735338 on the coder's plant `a531a9e`, time plus 1.0 at `<locus:rteTime>`]: 57 tests, 1 failure, `FormatDigestsCommonTest`; GPX equal F0 2 of 2, F0h 7 of 8, NT 52 of 59; hints, JSON, KML, CSV equal on every row; the 8 rows that differ are all GPX, all mode 7; 0 ` e: `. Simulator-checked. This agrees with -168 line for line.
- The change itself is green on the simulator [C: CI job 131685696329's log, run 43364528773 on `28e2a79`]: 57 tests, 0 failures, all five outputs equal on 2, 8 and 59 rows. Simulator-checked. The same lines are equal at `2184691` [C: job 132262700481's log].
- P60, same value and another text: a `0` appended to every track point's `<ele>` text (`FormatGpx.kt:281`) [C: 0 ` e: `, `compileKotlinJvm` ran, 5 fresh XML, restored, `cmp` equal]. `FormatDigestsJvmTest` PASSED over all 69 rows and `FormatDigestsCommonTest` PASSED. `HintsFormatsLogicTest` FAILED, "1381 differences; first: F0h F0h-inside-across-the-degree-line mode 2 gpx: port 16444/... oracle 16218/..."; `FormatGpxTest` FAILED on a digest and length. As predicted. The first failure's port length is longer than the oracle's, which only an added character gives, so it is this edit's.
- How often each place occurs on the 69 rows [C: a scratch test of mine over the same production run, counts only]: `<ele>` on track points 37,947; `<ele>` on waypoint lines 1,361; `<locus:rteDistance>`, `<locus:rteTime>`, `<locus:rteSpeed>` 755 each; `<brouter:speed>` 0; rows with `showspeed` 0; JSON points with a third number 37,947, with two numbers 551; JSON flat (Point) arrays 0. Every number at a by-value place parsed: 41,573 replaced, the sum of the counts above.

Findings:

- **Should-fix (60-1): the stored length no longer holds the text at the by-value places, and the ruled difference never needed that** [C, P60; R, `FormatDigestsCommonTest.kt:71-74`]. P60 adds one character at 37,947 places and both digest tests stay green, because a number of any length becomes 16 hex digits. The difference the owner ruled on is "in its last digit"; session 59 found all five such texts the same length as Java's, "neither longer nor shorter" (the coder's file, which I read and did not re-measure). So the plain text's length could be stored and compared beside the canonical digest: a last-digit difference would still pass, and a longer or shorter text at those places (a trailing zero, an exponent form, a `Float` printed as a widened `Double`) would fail on the simulator, where today nothing looks at it. The coder's design says openly that such texts are "no longer caught"; the loosening is stated, it is only wider than it had to be. On the JVM this class of error is caught by text [C, P60]. On the simulator it is caught by nothing, and the simulator is the only place Kotlin/Native's own printing runs. This is for the planner to weigh and, if it changes what "compare by value" means, for the owner; the ruling is quoted above.
- **Note (60-2): on the simulator the value is read back by the platform that printed it** [R, `FormatNumberText.kt:94-99`: `toDoubleOrNull`, `toFloatOrNull` in common code]. The stored row is the JVM's reading of the JVM's text; the simulator's side is Kotlin/Native's reading of Kotlin/Native's text. A printer and a parser wrong in the same way would agree with each other. For the five known texts the coder checked on the JVM that both halves of the tie read back to the same bits (session 59's file); nothing checks it in general. I have no evidence of such a fault and cannot run the simulator.
- **Note (60-3): the `Float` form and the JSON Point form are not exercised by any real row** [C, the counts: `<brouter:speed>` 0, `showspeed` 0, flat arrays 0]. For `speedText` (GPX and JSON) and the two JSON feature elevations (`FormatJson.kt:188`, `:227`), the by-value handling is tested only by `FormatNumberTextTest`'s made-up fragments. This is not a loosening: the plain digests before session 60 never saw those places either. But "12 places compared by value on the simulator" is true of the code and not of the data: by my count at most 9 of the 12 occur on the 69 rows, and I could not split the 1,361 waypoint-line elevations between the hint places and the two waypoint-export places [?].
- **Note (60-4): one of the twelve places has a value plant on real routes** (the time; -168 says so). Track-point elevation, hint elevation, distance and speed are reached thousands of times by the stored rows, so a changed value there changes the digest by the same mechanism [I]; none was planted, and I did not plant one either.
- **Note (60-5): the JVM run of `FormatDigestsCommonTest` covers F0 only** (the coder's statement in session 63's file; consistent with P60, where it passed while 8 of 8 F0h rows differed in `HintsFormatsLogicTest`) [I]. On the JVM the rows are held by `FormatDigestsJvmTest`.

## Session 69: `RouteWaysCommonTest` and `RouteWaysExpected`, at `2184691` (added after my predictions)

**Verdict: justified by independent evidence.** The session's own findings file holds its design and predictions only; it was cut off before writing results. So the evidence below is mine and CI's, not the coder's. What I expected is in the Plants table above (P69, S69), written before either ran.

What the change is [R, commit `2184691`]: `RouteWaysCells.kt` moves from jvmTest to commonTest with one line changed (`Math.PI`, `Math.cos`, `Math.toRadians(lat)` become `kotlin.math.PI`, `cos`, `lat * (PI / 180.0)`). `RouteWaysCommonTest` calls `readRouteWays` once per square for the 16 F0h ends and holds each call to a stored row: id, divisor, cell count, a digest of the cells asked, link count, point count, and a digest over every link's description and points. A second test asks a 1/32 square at 1/80. `RouteWaysExpected.ROWS` holds 16 rows.

**Are the stored rows anchored to the Java by anything other than session 68's JVM test:** in the tree, no, and the file says so ("they reach the Java engine only through `RouteWaysEntryTest`") [R, `RouteWaysExpected.kt:5-7`]. The chain is: Java walk equals the port on the JVM (`RouteWaysEntryTest`), the port on the JVM equals the stored rows (`RouteWaysCommonTest` run in `jvmTest`), the stored rows equal the port on the simulator. Each link of the chain is a test that runs on every push. Two things tie it more directly:

- The rows' link counts sum to 4,858 and their point counts to 43,006 [C, added by hand from the file], the Java walk's own totals for the same points in session 68's test [C, baseline print].
- **S69, a scratch test of mine** (untracked, deleted after; the Java walk copied from `RouteWaysEntryTest.oracleRead`, no port code in the walk, the row's digest computed from the Java side with the same `EngineRun.fnv1a` helpers) [C: 0 ` e: `, fresh XML, 1 test, 0 failures, tree clean after]: "java rows 16, stored rows 16, equal 16; described links 4858, reverse true 0". So all 16 stored rows, every field, are what the Java engine itself gives. And on the Java side no described link of the 4,858 is reverse, as the reading under session 68 says.

**Does the simulator run the class** [C: my reading of CI run 43535294553 on `2184691`, job 132262700481 `ios-smoke`, conclusion `success`, 0 ` e: ` lines]: yes. `RouteWaysCommonTest: tests 2, failures 0` under `iosSimulatorArm64Test`, both tests listed by name with `[iosSimulatorArm64]`; the simulator's `TOTAL: tests 60` (58 before); the line "s69 route ways common F0h: 16 points, 16 calls, links 4858, points 43006" printed on the simulator; the 16 row lines the simulator printed are byte for byte the 16 stored rows [C: `cmp` of the two sorted lists]. The line `OK: all 2 classes ran` is present. Simulator-checked. The `jvm` job of the same run: `TOTAL: tests 251, failures 0`, 0 ` e: ` [C: job 132262699914's log]. Both totals are the ones the session predicted.

Runs here, at `2184691`:

- Unplanted [C: 0 ` e: `, fresh XML]: `RouteWaysCommonTest` 2, `RouteWaysEntryTest` 3, `RouteWaysTest` 2, all pass; `RouteWaysEntryTest`'s two digests are equal to my baseline's at `e9eeddb` (`069e566d...`, `45a3d729...`), and `RouteWaysTest`'s two begin `b7fccbb3...` and `5a5c054d...`, as session 68's file gives them before the move. So the move of `RouteWaysCells` changed no cell on this JVM.
- P69, the session's own designed plant, never run by it: the two lines that add the target point removed from `RouteWays.kt` [C: 0 ` e: `, `compileKotlinJvm` ran, 2 fresh XML, restored, `cmp` equal]. `RouteWaysCommonTest` 2 tests, 1 failure, `f0hEndsReadAsStored`: 32 entries, for each of the 16 rows `points stored/got` and `digest` and nothing else, the first "points 2457/2052" (2,457 minus its 405 links). `RouteWaysEntryTest` failed on `[point count]`. Exactly the session's prediction and mine.

Could a wrong port pass it: on the simulator the test holds the port to its own JVM output, so it sees a platform difference, not a wrong port; a wrong port is the JVM test's to catch. The file says so [R]. Including the digest of the cells asked is a good call: the cells come from each platform's own `cos`, and that field separates a platform-math difference from a reader difference.

Findings:

- **Should-fix (69-1, and it reaches past session 69): nothing in CI holds that a named class runs on the simulator** [R, `.github/workflows/brouter.yml` at `2184691`: the simulator step runs `:brouter:iosSimulatorArm64Test` with no filter; the gating step runs `tools/junit-ran.py` over `brouter/build/test-results/jvmTest` with `$ARM64_JVM_CLASSES`, two JVM classes]. The line `OK: all 2 classes ran` is about the arm64 JVM step. It says nothing about the simulator. The record cites that line beside the simulator's count in -164, -167, -169, -170, -171, -173 and -174, and my dispatch asked me to confirm "the gate line present" for session 69; session 69's own design file is the one place that states what the line gates. Today "the simulator ran this class" is established each time by a person reading `TOTAL` and the class list. A commonTest class that stopped running there (moved, renamed, filtered) would leave every job green. `tools/junit-ran.py` already exists; pointing it at `iosSimulatorArm64Test` with the classes that carry a simulator claim (`RouteWaysCommonTest`, `EngineStopCommonTest`, `FormatDigestsCommonTest`, `FormatNumberTextTest`) would make the claim hold without a reader.
- **Note (69-2): the session left no results in its findings file** [R, the file at `2184691` ends at "Limits stated now"]. Its commit message claims the older classes "print the same lines as before the move"; I saw that [C]. Its designed plant had not been run; I ran it [C].
- **Note (69-3): 16 calls, 11 distinct reads** [R, `RouteWaysExpected.kt`: five rows repeat another row's cells digest and digest, where two ends share a point]. Harmless; the count of independent cases is 11.
- **Note (69-4): `lat * (PI / 180.0)` equal to `Math.toRadians(lat)`** is the coder's probe on JDK 21 (10 million values), which I did not repeat [?]. What I saw is that the cells did not move on this JVM for these points [C, the digests]. On the simulator the cells' digest is in every row and was equal [C, CI].
- Nothing loosened: new tests, and a helper moved.

## My five should-fix items from the first review

| Item | Record | Closed? | On what |
|---|---|---|---|
| 1, `SearchBoundaryTest` strict, with the flag | -167 | yes | [R] the diff; [C] baseline, the flag seen on the running JVM, P62 red at the added line |
| 2, `ReadBinaryTest` reach asserted | -171 | yes | [R] lines 188, 189; [C] P66 red (69 against 74); the coder's 0 against 74 |
| 3, -116's loosening narrowed | -171 | yes | [R] lines 26, 167; [C] baseline, 1,433 texts equal whole |
| 4, FK9's guard | -170 | yes | [R] lines 196 to 253; [C] P65 red for the guard's reason alone on 8 of 10 |
| 5, the oracle-only run in the tree | -173 | yes | [R] the Java lines; [C] baseline and P67 |

-173's sentence "all five should-fix items of -163 are now closed" is true.

## Where the record was inaccurate or loose

- The gate line, cited beside simulator counts in seven entries (finding 69-1). No entry says it gates the simulator, so none is false; the placement invites that reading, and my own dispatch took it.
- Session 66's findings file: "One empty track dropped gives at most about 53". Planted, 69 (finding 66-1). The file marks it inferred; -171 repeats it as inferred.
- Session 68's findings file and -174: "No described link in either set has `reverse` true ... the data cannot tell those apart". True, and understated: no data can (finding 68-1).
- I found no entry of -164 to -174 that claims a result its evidence does not carry. Each "Not shown" list I checked names the gap I went looking for, except 65-1.

## Verdicts, one line each

| Session | Verdict | Predicted | Score |
|---|---|---|---|
| s60 | justified in part | justified in part | Hit on the line. Half the reasoning missed: the form is not wider than the twelve places. What is wide is the text accepted at them. |
| s62 | justified by independent evidence | justified by independent evidence | Hit. Neither risk I named was there. |
| s64 | justified by independent evidence | justified in part | **Miss.** The stored row is the oracle's, not the port's. |
| s65 | justified by independent evidence | justified by independent evidence | Hit. The risk I named (true by construction) is real only on a correct port, which is the point of a guard. |
| s66 | justified by independent evidence | justified in part | **Miss** on the line. The floor is weaker than an equality, as predicted, but that is a note, not a part unjustified. |
| s67 | justified by independent evidence | justified in part | **Miss.** It does not depend on timing; the coder found that the run I had asked for did, and built one that does not. |
| s68 | justified by independent evidence | justified in part | **Miss.** The reverse flag cannot be exercised by any data, so it is not a gap in the test. |
| s69 | justified by independent evidence | none (added later) | Not scored. Both of my plant-table predictions for it held. |

3 of 7 on the line. I predicted "in part" five times and it was right once. Should-fix closure: predicted 1, 3, 4, 5 closed and 2 closed in part; found all five closed. Overall prediction: no blocking finding held; "two or three should-fix findings, most likely on s60, s66 and s68" held for s60 only, and a second came from CI, where I had not looked.

Mechanism prediction ("a plant in a number's low digits will pass the s60 simulator comparison only if the canonical form rounds; on the JVM it will fail because the JVM still compares text"): not run as written. The form does not round a `Double` (raw bits), so a changed low digit that changes the value fails on both [I, from the code; the coder's time plant shows it for a whole unit]. P60 ran the other half: the same value in another text passes the by-value comparison and fails the JVM's text comparison [C].

Plant and scratch predictions: 12 of 13 held (P62, P64b, P64c, P65, P66 in outcome, P67, P68a, P68b, P60, P69, S60, S69). P64 failed: I expected the sibling to catch a work-counting limit at 30 per link and it did not. P66's number was outside my range (69 against "45 to 65"), though the outcome held. P65 held as "nearly all" (8 of 10), not all.

**Overall: justified by independent evidence, with one part of s60 resting on the owner's ruling.** No blocking finding. No change of the eight turned a wrong port green that I could find, and each test I could plant against failed for its own reason.

### Blocking

None.

### Should-fix

1. **60-1** (`FormatDigestsCommonTest.kt:71-74`, `FormatNumberText.kt`): keep the plain text's length beside the by-value digest for GPX and JSON, so the simulator still fails a longer or shorter text at the twelve places. [C, P60: a character added at 37,947 places passes both digest tests.] For the planner, and the owner if it bears on the ruling.
2. **69-1** (`.github/workflows/brouter.yml`, the gating step): gate the simulator's class list, not only the arm64 JVM step's; the line `OK: all 2 classes ran` says nothing about the simulator. [R.]

### Notes

60-2, 60-3, 60-4, 60-5; 62-1, 62-2; 64-1; 65-1, 65-2; 66-1, 66-2, 66-3; 67-1, 67-2; 68-1, 68-2, 68-3; 69-2, 69-3, 69-4. Each is under its session above.

## Not checked

- Anything on an iPhone or on the simulator by my own run. Every iOS statement here is from CI logs I read (runs 43535294553, 43466735338, 43364528773) and is simulator-checked.
- CI's JDK. All my runs were on this container's OpenJDK 21.0.12.1.
- CI logs for the runs on sessions 62, 64, 65, 66, 67 and 68's own commits (43447798521, 43479572984, 43492461810, 43504930148, 43516186631, 43526466282). I read the run on `2184691`, which contains all of them, and the two session 60 and 63 runs.
- That the oracle jar's line table matches the vendored Java source (the coder's `javap` reading, session 67).
- The coder's own plants and controls. I did not repeat them, by method; I ran different ones.
- A value plant at any by-value place other than the time (60-4), and the split of waypoint-line elevations between places (60-3).
- Whether Kotlin/Native's number printing differs from Java's anywhere beyond the five ties session 59 found; session 59's counts themselves.
- Two squares in one read (68-2); a square of divisor 80; a missing or bad `lookups.dat` or square; the file-handle leak in `PhysicalFile`'s constructor.
- `Math.toRadians` against `lat * (PI / 180.0)` beyond the points the tests use (69-4).
- The full `jvmTest` suite locally; `e3Strata`; whether any other test's result moved under the new JVM flag (62-2).
- E1 on the coders' commits. My own report holds no coordinate: cases are named, and the only numbers are counts, digests, lengths and milliseconds.
- The Restricted Object was not met. The owner has withdrawn it.

## State on finishing

- The code worktree is back at `e9eeddb`, detached, `git status` clean. It was moved to `2184691` for session 69's runs and moved back. Every planted file was restored from a saved copy, `cmp` equal, and both scratch tests were deleted with the tree clean after each.
- Nothing was pushed to `brouter-port-5b`. No pull request. The coder was not contacted.
