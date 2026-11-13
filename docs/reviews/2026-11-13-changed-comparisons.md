# Review: five comparisons changed after a result was seen

Reviewer: Samphire Witness. Dispatch 2026-11-13-01 from Cedar Datum. First review.

Status: IN PROGRESS (this first commit holds the method and the predictions only; none of the five changes has been read yet).

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
