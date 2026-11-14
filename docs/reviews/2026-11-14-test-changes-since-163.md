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

To be filled in.
