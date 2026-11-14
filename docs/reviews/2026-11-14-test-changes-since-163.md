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
- **Note (65-2): on the JVM the guard is the only test that sees a limit that is too short on a real clock** [C, P65: 14 other tests in the two classes passed]. The driven-clock tests jump from 0 to 30,001 against 30,000, so any limit from 1 to 30,000 passes them [R, `RoutingEngineStopTest.kt:82-90`, `EngineStopCommonTest.kt`'s copy]. A limit that is too long is seen only by those driven-clock tests, and only if it exceeds 30,001 [R]; the guard passes it by design.
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
- **Note (66-3): in the null-pointer case the port's text after the prefix is still unconstrained beyond being non-empty** [R, lines 151 to 153 and 167]. That is -116's ruling, unchanged, and now it is as narrow as its evidence. A port that reached the catch through another exception on an empty track would still pass if the result fields agree. Named in my first review; not new.
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

**Could a wrong port pass the test:** not one I could build. P68b, the geometry decoded in the other direction (`decodeGeometry(it, node, target, !reverse)`) [C: 0 ` e: `, `compileKotlinJvm` ran, fresh XML, restored, `cmp` equal]: 3 tests, 2 failures, "dropoff walker: square 1 of 1, link 5 of 8: field [iLat] at point 2" and "F0h inside-across-the-degree-line/from: square 1 of 1, link 1 of 405: field [iLat] at point 2"; the divisor test passed. The coder's plant was on the description, mine on the points; both fields bite. The test compares against a live Java walk in the same JVM, link by link (`RouteWaysEntryTest.kt:112-124`), with the link count checked first, so a dropped or added link fails too [R].

Findings:

- **Note (68-1): the reverse flag is dead on this walk by the engine's own code, not by a shortage of data** (above). The "Not checked" line in the coder's file and any later brief that asks for "a described link with reverse true" asks for something that cannot exist here.
- **Note (68-2): one way context and one decoder per square call, where Forager shares one across squares** [R, `RouteWays.kt:60-63` against Forager 60-62 and 72]. No point in either set spans two squares [C, baseline: 15 calls for 15 points, 16 for 16], so this is argued from the code, not run. The coder says so. I read `GeometryDecoder`'s cache as keyed on array identity and did not find a way for it to differ, but I did not run it [I].
- **Note (68-3): the entry cannot check that a cell lies in the square it is given** [R, `RouteWays.kt:23-25`]. Stated in the request's doc and in the coder's file. A caller that passes a cell from another square gets plausible links from the wrong place. Forager groups by square first, so its adapter cannot do this; a new caller could.
- The file-handle leak when `PhysicalFile`'s constructor throws is the coder's finding, the same in Forager and in Java; I did not check it [?].
- Nothing loosened: a new entry and a new test [R].
