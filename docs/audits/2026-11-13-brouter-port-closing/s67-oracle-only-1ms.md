# s67: the oracle alone at 1 ms on FK9, committed as a test (Plumb Ledger, session 67)

Review `docs/reviews/2026-11-13-changed-comparisons.md` (branch `review/changed-comparisons`, `ed8a899`), should-fix 5,
verbatim: "**-126: commit the oracle-only run.** The 1 ms evidence came from a scratch test that was deleted. Evidence
for a changed comparison should be rerunnable from the tree."
Base `baed90c` on `brouter-port-5b` (verified against `git ls-remote` before any work; tree clean). One file changes,
`router/RoutingEngineStopTest.kt`: one test and one private helper are added; no existing test, assertion or helper and
no port code changes. The file goes from 280 lines to about 350, under 400, and the new test uses the class's private
helpers (`cases`, `oracle`, `params`, `Run`), so it goes in this class, not a sibling.

## Can "10 of 10 target-island-check" fail on a correct oracle? (read, not run)

Read in `third_party/brouter/src/main/java/btools/router/RoutingEngine.java` (Forager's vendored v1.7.10, read only),
and checked against the oracle jar itself (`brouter/build/oracle/brouter-1.7.10-ro.jar`, `javap -c -l`): the jar's line
table puts `"target-island-check"` at `:961`, `_findTrack`'s first `monitorenter` at `:1965`, and the limit's
`currentTimeMillis` at `:1981`, so the jar is the source read.

- `startTime = System.currentTimeMillis()` is taken once at the top of `doRouting` (`:201`), before the waypoints are
  matched and the tiles loaded (`tryFindTrack`, `:935`, `matchWaypointsToNodes` `:1609`).
- `tryFindTrack` then runs `findTrack("target-island-check", ...)` for each section (`:952-965`; `:961` when
  `inverseRouting` is false, skipped when the section's first waypoint is a beeline, `:954`), before any pass.
- The limit is checked only at `_findTrack`'s loop head (`:1979-1983`): `if (System.currentTimeMillis() - startTime >
  timeout)`, with `timeout = maxRunningTime` here (`fastPartialRecalc` is `false` at `:961`).
- **The check is strict.** Source `>`; bytecode `lcmp` then `ifle` past the throw. At `doRun(1)` it strikes only when
  the two wall-clock readings differ by 2 ms or more. An elapsed reading of exactly 1 ms does not strike.
- **The island check is bounded:** `nodeLimit = MAXNODES_ISLAND_CHECK` (500, `:50`, `:953`), counted down per node
  (`:2056-2057`). It stops after 500 nodes or on reaching the start.

**So a fast machine can reach `pass0` at 1 ms, by the Java.** A case strikes in the island check only if matching,
tile loading and up to 500 node expansions take long enough for the wall clock to read 2 ms past `startTime` while the
island check is still looping. Nothing in the Java makes that true: on a machine (or a warm JIT, or a warm page cache)
fast enough to finish all of it before the clock reads `startTime + 2`, the first strike is `pass0`, and on a correct
oracle. Session 34's file called this "a guess", rightly. A wall clock stepped backward during the run has the same
effect. **"10 of 10 target-island-check" on a plain `doRun(1)` is a timing of this machine, not a fact of the Java, and
is not asserted.**

## The assertion that is true by the Java

What -126 needs is the Java's answer to: when the clock is already past the limit at the first check, which operation
strikes? The test makes that condition hold on the oracle itself, without a clock of its own (the oracle cannot be
given one):

- **Held run.** The oracle's `openSet` (a private field, `:41`, assigned only there) is read by reflection. The test
  thread takes its monitor, then starts `doRun(1)` on a thread of its own. The first `synchronized (openSet)` the run
  meets in execution order is `_findTrack`'s at `:1965`, inside the first `_findTrack`, before its first loop-head check
  (the other three monitors, `:1986`, `:2353`, `:2363`, are inside the loop or outside the run; `:341`'s
  `openSet.clear()` in `doRouting`'s `finally` takes no monitor). The test polls until the run's thread is `BLOCKED` on
  that object (`ThreadMXBean.getThreadInfo`: the lock's identity hash and class equal `openSet`'s), reads
  `System.currentTimeMillis()` as `t`, waits until the same clock reads `t + 5` or later, and releases.
  `startTime` was read before the block, so at the first check `now - startTime >= 5 > 1`: the first check strikes,
  under the first `_findTrack`'s operation name.
- **Which operation that is** depends on the inputs only, not on time: `inverseRouting` is false (`hiking-mountain.brf`
  sets no `inverseRouting`), the waypoints are `lon,lat|lon,lat` with no third token and no `straight` parameter, so no
  section starts with a beeline from parsing (`RoutingParamCollector.java:35-44`, `:193-199`). Matching can in principle
  mark a waypoint a beeline (`NodesCache.java:313`, `RoutingEngine.java:1646-1667`); whether it does for FK9's inputs
  is a fact of the seeded inputs, and session 34's run (10 of 10 island check at 1 ms) shows it does not for any of
  them [R, file 12; held to the test's run].
- **Asserted (true by the Java under the stated conditions):** kinds (`E3Outcome.kindOf`) equal `{family:timeout=10}`,
  and first words of the oracle's messages equal `{target-island-check=10}`. The conditions: the wall clock does not
  step backward by 4 ms or more between `startTime` and the first check; FK9's draws are the 10 rows of `expected-1b`
  (asserted already by `cases`). Neither condition depends on the speed of the machine.
- **Also run and printed, not asserted:** the plain `doRun(1)` of session 34's file, the same 10 cases, its first-word
  and kind counts. It is the run the record cites, now rerunnable; it is printed only, because it can fail on a correct
  oracle (above). Per the dispatch, any run in this session where either count is not 10 of 10 is a finding and a stop.
- If the run's thread ends or 120 s pass without blocking on `openSet`, the test fails naming the case; the test does
  not fall back to an unheld run.

Prints: counts and first words only (E1), no coordinates and no whole messages.

## What this test is and is not

It checks the oracle, so no port plant can fail it. It does not show that the port strikes in the island check;
that is `theTimeLimitReadsTheInjectedClock`'s job, on a clock the test drives. What it adds is that -126's corrected
text (`target-island-check` where `pass0` had been written by hand) is the oracle's own answer under the same
condition, rerunnable from the tree.

## Predictions

**Unplanted, each of at least 5 runs** (filter the one method, `--rerun-tasks` or `cleanJvmTest`): 0 ` e: `, fresh XML,
1 test, 0 failures. Held: first words `{target-island-check=10}`, kinds `{family:timeout=10}`, by the Java. Plain: I
expect `{target-island-check=10}` and `{family:timeout=10}` on this machine as in session 34 [I, from file 12; a
timing, not asserted].

**The control** (`revert.sh`, on the test file, filter the one method): the one string `mapOf("target-island-check" to
n)` becomes `mapOf("pass0" to n)`. It compiles (0 ` e: `), fresh XML, 1 test, 1 failure; the kinds assertion, before
it, passes; the failure is the first-words assertion, JUnit 4's object form:
`java.lang.AssertionError: 8c FK9, the oracle alone at 1 ms, held: first words of the messages expected:<{pass0=10}> but was:<{target-island-check=10}>`.
Restored from the saved copy, `cmp` equal, the new test still in the tree (`git diff` shows it).

**CI on the test's commit:** `jvm` TOTAL 246 tests, 0 failures (245 before, plus this one); `ios-smoke` 58 and 7 with
the gate line `OK: all 2 classes ran` (this test is JVM-only, so unmoved); 0 ` e: ` in every job log; the test's two
print lines as predicted for the held run.
