# s65: FK9's guard, the port's own clock past the limit (Plumb Ledger, session 65)

Review `docs/reviews/2026-11-13-changed-comparisons.md` (branch `review/changed-comparisons`), should-fix 4, verbatim:
"The guard would be: when the port reports a timeout, its own clock has passed the limit." The planner reverses its
refusal in RECORD -127 decision 1 and adds the guard. Base `ee1a001` on `brouter-port-5b`. Test file only; no port code.

## The design (written before any run)
**Can the test pass the port a clock?** Yes. `RoutingEngineStopTest.port(...)` already builds
`RoutingEngine(..., clock)` with the `EngineClock` it is given (`RoutingEngineStopTest.kt:207`), and `againstOracle`
gives it `EngineClock.MONOTONIC` (`:142`). No port code needs to change.

**The clock.** A private `RecordingClock` in the test file wraps `EngineClock.MONOTONIC`: each `nowMillis()` reads the
real clock, returns that value unchanged, and records the first value read, the last value read and the number of reads.
The port therefore runs on exactly the clock it ran on before; the wrapper only watches. Chosen over a driven clock
because FK9 is the comparison against the oracle on real time, and a driven clock would make it a different test.

**Where.** `againstOracle` gains a parameter `clockGuard: Boolean = false`. Only FK9 passes `true`. With `false` the port
gets `EngineClock.MONOTONIC` as before and no new check runs, so FK10, FK11 and every other test are unchanged in what
they run and what they assert.

**The assertion.** For each FK9 case, when the port's message is a timeout (`E3Outcome.kindOf(p.message) ==
"family:timeout"`), `last - first > 50` (the `maxRunningTime` FK9 passes) on the recording clock, where `first` is the
port's start read (`RoutingEngine.kt:85`, `doRouting`'s `startTime`) and `last` is its last read. A case that fails it
joins FK9's existing `mismatches` list as `"port's clock not past the limit at its timeout (<elapsed> ms over <reads>
reads)"`, so the failure comes out through FK9's existing `assertTrue`. The elapsed milliseconds per case are printed
(a timing, no coordinate).

## Can the guard fail on a correct port? (traced, not run)
The port reads its clock at four places, all in `RoutingEngine.kt`: `:85` (start), `:549` (the reset before
re-tracking, Java `:1797`), `:719` (the watchdog's seconds, read only when `terminated`), `:724` (the limit check). No
other commonMain file reads an `EngineClock` (grep for `nowMillis`).
- On a correct port, "timeout after" comes only from `:725`, thrown right after `:724` read a value `t` with
  `t - startTime > 50`, where `startTime` is the value read at `:85` or, after the reset, at `:549`.
- After that throw nothing reads the clock: the pass loop's catch (`:511-522`) rebuilds `foundRawTrack` and rethrows,
  `doRouting`'s catches and `finally` (`:114-127`) read no clock, and `:719` needs `terminated`, which FK9 never
  sets (`Cancel.NONE`). So on a correct port the last read **is** the read that tripped the limit.
- `MONOTONIC` never goes back, so `startTime >= first`, and `last - first >= last - startTime > 50`. All values are the
  same truncated whole milliseconds the port compared, so there is no rounding gap: on a correct port the guard holds
  by construction, whatever the machine's speed. **Prediction: it cannot fail falsely on a correct port.**
- What it does not check: measured from the first read, not from the latest reset, it would pass a port whose limit
  strikes early in re-tracking if the passes before took over 50 ms. It is the review's guard as worded ("its own clock
  has passed the limit"), not a check that the strike came at the first loop head past the limit.

## The plant (one line, `RoutingEngine.kt:724`)
`if (clock.nowMillis() - startTime > timeout) {` becomes `if (clock.nowMillis() - startTime <= timeout) {`.

This is RECORD -127's revert (b), `docs/audits/2026-11-11-brouter-port-step8/14-revert-time-limit-flipped.txt`, not
session 64's `>= 0`. Why: the review names revert (b) ("seen red under revert (b)"), -127 records FK9 passing it
(10 cases, 0 mismatches, port first words `target-island-check` 10), and it is the exact complement of the guard: the
planted port throws only when its own clock is **not** past the limit. Session 64's `>= 0` would also be caught but adds
nothing here: it strikes at the same first loop head for the same elapsed time.

**Prediction under the plant.** The build compiles (0 ` e: ` lines). For each case the port's first loop head, in the
target island check, reads `t` with `t - first <= 50` (session 34 saw it strike there on 10 of 10) and throws
`target-island-check timeout after 0 seconds`. That is the last read, so `last - first <= 50` and the guard fails.
FK9's other comparisons hold as in session 34: kind `family:timeout` on both sides, both messages match `TIMEOUT_50`,
both found tracks empty, `terminated` false on both. Predicted FK9 failure:
`8c FK9: 10 mismatches; first [FK9-....: [port's clock not past the limit at its timeout (<n> ms over <r> reads)] (oracle
family:timeout ...; port family:timeout ...), ...]`, with every listed reason the guard's and no other, `n` at most 50.
The count is 10 if every case strikes in the island check, as in session 34; it is timing, so fewer is possible, but
at least one (inferred: the island check's setup has never been seen to take 50 ms here). A case whose first loop head
came after 50 ms would run the passes unthrown and strike after re-tracking's reset with `last - first > 50`, and the
guard would pass it: that is the gap named above.
The other tests in the class under the plant, from session 34's run: `forager30sLimitOnAStoppedClock...` and
`theTimeLimitReadsTheInjectedClock` fail as then; the other six pass. Predicted class result: 9 tests, 3 failures
(FK9, those two).

**Prediction with no plant.** FK9 passes, 10 cases, 0 mismatches, every case's elapsed above 50 ms; the class 9 of 9.
Run at least 5 times; one failure in 5 is a finding.

## Results (appended after the runs; the sections above are unchanged since `a85e7b8`)
The test landed in `890bc42` as designed, with two details settled while writing it: a port that reports a timeout but
never read its clock also fails the guard (elapsed `null`), rather than throwing in the test; and the per-case print
gives the read count beside the elapsed time.

**Baseline, before the guard** (FK9's method alone, `--rerun-tasks`): 0 ` e: `, fresh XML, 1 test, 0 failures; first
words `{target-island-check=2, pass0=18}`.

**Unplanted port, with the guard, the whole class, 5 runs** (each `--rerun-tasks`, a stamp touched before each, 0 ` e: `
lines and 1 fresh XML each): 9 tests, 0 failures, every run. FK9's port clock, ms first read to last, per case:
- run 1: 51, 52, 51, 51, 51, 52, 52, 51, 51, 54 (first words `{pass0=20}`)
- run 2: 51, 56, 51, 51, 51, 51, 51, 53, 51, 51 (`{pass0=20}`)
- run 3: 52, 56, 51, 51, 51, 52, 51, 51, 51, 51 (`{target-island-check=3, pass0=17}`)
- run 4: 51, 53, 53, 51, 51, 51, 51, 52, 51, 51 (`{pass0=20}`)
- run 5: 51, 56, 51, 51, 51, 51, 52, 51, 51, 51 (`{pass0=20}`)
50 of 50 cases above 50 ms; the least is 51, the first whole millisecond past the limit, as the trace predicts for a
port whose last read is the one that tripped. Run 3's first case read the clock only 35 times, so it likely struck early, perhaps in the island check [I: the
first-words count does not split port from oracle], and still
read 52.

**Plant, revert (b), `revert.sh` name `s65p`, filter `*RoutingEngineStopTest`.** The edit applied once at
`RoutingEngine.kt:724` (`> timeout` to `<= timeout`). 0 ` e: ` lines; 1 fresh XML (stamp 19:26:12, XML 19:27:42).
Class: 9 tests, 3 failures, as predicted: FK9, `theTimeLimitReadsTheInjectedClock` (`expected:<[target-island-check]
timeout after 30 se...> but was:<[re-tracking] timeout after 30 se...>`, as in session 34) and
`forager30sLimitOnAStoppedClockRoutesAsTheOracleWithNoLimit` (oracle route, port `family:timeout`). FK9's message, first
of five shown:
`java.lang.AssertionError: 8c FK9: 10 mismatches; first [FK9-0001: [port's clock not past the limit at its timeout (13 ms
over 2 reads)] (oracle family:timeout (29 chars)|0|0|0|1443 links|terminated false; port family:timeout (43
chars)|0|0|0|0 links|terminated false), ...]`.
All 10 cases fail, and each case's reason list holds the guard's reason and nothing else; elapsed 13, 28, 25, 4, 5, 6,
8, 2, 5, 5 ms, each over 2 reads (`:85`, then the first `:724`); first words `{target-island-check=10, pass0=10}`.
**FK9's other comparisons passed this wrong port on all 10 cases** (kind, both messages against `TIMEOUT_50`, the empty
track, `terminated`): the review's point, seen. Port 43 characters is `target-island-check timeout after 0 seconds`,
the oracle's 29 `pass0 timeout after 0 seconds` [I, from the lengths; messages are not printed].
After: `cmp` of the port against the saved copy equal; the test file `cmp` equal to a copy taken before the plant, so
the guard was in the tree throughout.

**CI, run 43492461810 on `890bc42`.** All four jobs `success`, 0 ` e: ` lines in each log:
- `jvm`: TOTAL 245 tests, 0 failures (245 before; this change adds an assertion, not a test).
  `RoutingEngineStopTest` 9 tests, 0 failures. FK9: 10 cases, 0 mismatches, `{pass0=20}`; the port's clock per case 51,
  51, 52, 53, 51, 52, 51, 51, 51, 51 ms, all past 50.
- `ios-smoke`: TOTAL 58, 0 failures; arm64 TOTAL 7, 0 failures; gate `OK: all 2 classes ran`. Unchanged, as expected for
  a JVM-only change. Simulator-checked.
- `ios-memory`: TOTALs 3, 1, 1, 0 failures. `jvm-production`: TOTAL 2, 0 failures.

**Not shown.** The guard runs only on the JVM (FK9 has no oracle on iOS). The plant ran once; the 50 ms cases in CI are
one machine's timing. The gap named in the design (a limit that strikes early in re-tracking after slow passes) was
not planted. The unplanted runs show 50 of 50 local cases and 10 of 10 CI cases at 51 to 56 ms; that the guard can
never fail on a correct port rests on the trace above, not on these counts.
