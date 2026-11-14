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
