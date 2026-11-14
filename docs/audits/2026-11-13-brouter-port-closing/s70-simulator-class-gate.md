# s70: a gate that fails the simulator jobs when a named class did not run there (Plumb Ledger, session 70)

Review finding 69-1 (`docs/reviews/2026-11-14-test-changes-since-163.md:307` and `:360` on
`origin/review/changed-comparisons`): nothing in CI fails if a commonTest class stops running on the iOS simulator. The
line `OK: all 2 classes ran` is about the arm64 JVM step only.

Base `2184691` on `brouter-port-5b`, verified against `git ls-remote origin brouter-port-5b` before any work; tree clean.
This file is written, committed and pushed before any run of the gate, local or in CI.

## What was read (file and line, at `2184691`)

- `.github/workflows/brouter.yml`, all 242 lines.
  - `ios-smoke` (`:49`): `ARM64_JVM_CLASSES` (`:54-56`); the simulator step (`:88-95`) runs
    `:brouter:iosSimulatorArm64Test` with no `--tests` filter, results in `brouter/build/test-results/iosSimulatorArm64Test`;
    its counts step (`:96-100`); the existing gate (`:115-124`) runs `tools/junit-ran.py` over
    `brouter/build/test-results/jvmTest` with `$ARM64_JVM_CLASSES` only.
  - `ios-memory` (`:138`): the memory groups step (`:159-169`) filters to `Mp64EngineCommonTest`, `Ml5EngineCommonTest`,
    `Pm5EngineCommonTest`; its XML is copied to `brouter/build/test-results/ios-memory-groups` as the first line of R's
    step (`:185`), because Gradle empties `iosSimulatorArm64Test` on each run. R's step (`:178-186`) runs
    `REngineCommonTest`; its XML is copied to `brouter/build/test-results/ios-r-fallback` as the first line of the
    ladder's step (`:201`). The ladder's own XML stays in `iosSimulatorArm64Test`.
- `tools/junit-ran.py` (38 lines): for each named class it needs `TEST-<class>.xml` in the folder (`:24-28`) and at least
  one `testcase` without a `skipped` child (`:30-35`); prints one line per class, then `OK: all N classes ran` (`:38`) or
  `FAIL: ...` and exit 1 (`:36-37`); fewer than two arguments is a usage error, exit 2 (`:16-18`). It does not look at
  skipped cases when another case ran, nor at what a test printed.
- `tools/junit-counts.py`: prints the `TOTAL:` lines; unchanged here.
- The commonTest source tree: 28 test classes (`MemoryEngineCommonTest.kt` holds three). Five test classes return early
  and pass when a job variable is unset, printing `<...> is unset; not run`:
  `MemoryEngineCommonTest.kt:26-28` (MP64, ML5, PM5), `REngineCommonTest.kt:23-25`, and iosTest's
  `StackLadderIosTest.kt:132-134`. `Step10Cases.ntFile` (`Step10Cases.kt:32-42`) prints the same words on the JVM only
  and fails on iOS. In `ios-smoke` those five each report `tests 1, skipped 0` with `system-out` holding the "not run"
  line (base run 43535294553, job 132262700481, read from its log).
- Base run 43535294553 on `2184691`: `ios-smoke` `TOTAL: tests 60, failures 0, errors 0, skipped 0 in 29 files` and
  `TOTAL: tests 7 ... in 2 files`, `OK: all 2 classes ran`; `ios-memory` 3 in 3 files, 1, 1; `jvm` 251 in 109 files;
  `jvm-production` 2.

## Design

### What "ran on the simulator" means

`junit-ran.py`'s present check would pass a class that returned early: `Mp64EngineCommonTest` in `ios-smoke` is
`tests 1, skipped 0` and did nothing. A gate built on that alone would pass the memory groups even if `ios-memory` lost
`SIMCTL_CHILD_BROUTER_STEP10_MEMORY`. So the script gets one new option, `--simulator`, and with it a class ran only if:

1. `TEST-<class>.xml` exists in the folder;
2. it holds at least one `testcase`, and its header's `tests` is above 0;
3. no `testcase` is skipped;
4. its `system-out` does not hold `is unset; not run`, the words every early return above prints.

Without `--simulator` the script behaves exactly as now, byte for byte, so the arm64 gate line and its output are
unchanged. The new behaviour is a separate function in the same file; the old path is not edited.

Every line the new mode prints names the simulator, so it cannot be read as the arm64 JVM gate.

### Which classes, and why a named list

A named list, in the job's `env` beside `ARM64_JVM_CLASSES`, as that list is read by both the step and the gate there.

- Against a list derived from the source tree at CI time: the finding names three ways a class stops running there,
  "moved, renamed, filtered". A derived list follows a moved or renamed class, so it would stay green on two of the
  three; a named list goes red on all three. The cost is that a new commonTest class is not gated until someone adds
  it; that is written in the workflow comment.
- The rule for the list: every commonTest class that does its work in that job. That is a superset of the classes the
  record or a findings file cites as simulator evidence (the brief's list is all inside it), and naming the rest costs
  nothing. Classes that return early in a job by design are not named in that job, since naming them would be a claim
  the gate cannot honour; they are named in the job where they run.
- An empty list fails: the script exits 2 on fewer than two arguments (`junit-ran.py:16-18`), and the step does not
  catch that.

`ios-smoke`, `SIMULATOR_CLASSES`, 24 classes (28 commonTest classes less the three memory groups and R, which return
early there):

    com.zynergylabs.forager.brouter.ToolchainSmokeTest
    com.zynergylabs.forager.brouter.expressions.FloatTextCommonTest
    com.zynergylabs.forager.brouter.mapaccess.NodeHashMapTieBreakTest
    com.zynergylabs.forager.brouter.msun.MsunAtan2PairsTest
    com.zynergylabs.forager.brouter.msun.MsunCosTableTest
    com.zynergylabs.forager.brouter.msun.MsunEdgesTest
    com.zynergylabs.forager.brouter.tiles.CrEngineCommonTest
    com.zynergylabs.forager.brouter.tiles.DlEngineCommonTest
    com.zynergylabs.forager.brouter.tiles.EngineStopCommonTest
    com.zynergylabs.forager.brouter.tiles.F0EngineCommonTest
    com.zynergylabs.forager.brouter.tiles.F0RouteCommonTest
    com.zynergylabs.forager.brouter.tiles.F0hEngineCommonTest
    com.zynergylabs.forager.brouter.tiles.FkEngineCommonTest
    com.zynergylabs.forager.brouter.tiles.FormatDigestsCommonTest
    com.zynergylabs.forager.brouter.tiles.FormatNumberTextTest
    com.zynergylabs.forager.brouter.tiles.GpxNumbersCommonTest
    com.zynergylabs.forager.brouter.tiles.LbEngineCommonTest
    com.zynergylabs.forager.brouter.tiles.NtEngineCommonTest
    com.zynergylabs.forager.brouter.tiles.PinnedTileReadTest
    com.zynergylabs.forager.brouter.tiles.RouteWaysCommonTest
    com.zynergylabs.forager.brouter.util.CheapProductionTableTest
    com.zynergylabs.forager.brouter.util.JavaLangCommonTest
    com.zynergylabs.forager.brouter.util.JavaTextCommonTest
    com.zynergylabs.forager.brouter.util.PlatformInvariantsTest

`ios-memory`: the three memory groups from `ios-memory-groups`, and `REngineCommonTest` from `ios-r-fallback`. R is
included because it is commonTest and RECORD -143 cites its 240 of 240 as simulator evidence; the brief's rule covers it.

### Where the steps go

- `ios-smoke`: a new step right after the simulator's counts step, `if: always()`, so a red simulator step still says
  which classes ran. The arm64 steps after it already run on `always()`, so a red gate skips nothing.
- `ios-memory`: one new step at the end, after the ladder's step and before the upload, `if: always()`, reading the two
  copied folders. Placed earlier, a red gate would skip R and the ladder, whose steps run only on success; at the end
  it changes no existing step. If the memory step itself fails, R's step (and its copy) is skipped, and the gate then
  names the missing copy's path; the job is red from the test step in that case anyway.

### Predicted lines

`ios-smoke`, one line per class such as

    com.zynergylabs.forager.brouter.tiles.RouteWaysCommonTest: on the iOS simulator, 2 test cases, 2 ran, 0 skipped, 0 failed (brouter/build/test-results/iosSimulatorArm64Test/TEST-com.zynergylabs.forager.brouter.tiles.RouteWaysCommonTest.xml)

then

    OK on the iOS simulator: all 24 classes ran (brouter/build/test-results/iosSimulatorArm64Test)

The test cases over the 24 lines sum to 55 (60 less the five early-return classes, one each). The old line
`OK: all 2 classes ran` stays, unchanged, in its own step.

`ios-memory`: `OK on the iOS simulator: all 3 classes ran (brouter/build/test-results/ios-memory-groups)` and
`OK on the iOS simulator: all 1 classes ran (brouter/build/test-results/ios-r-fallback)`.

A class that did not run prints `<class>: did not run on the iOS simulator, <reason>` and the last line is
`FAIL on the iOS simulator: k of N classes did not run: <classes>`, exit 1.

### How the gate is shown able to fail

Locally (scratch only, never committed), on simulator XML cut out of the base run's own logs (`ios-smoke` job
132262700481, `ios-memory` job 132262700110):

1. the 24-class folder complete: exit 0;
2. the same folder with `RouteWaysCommonTest`'s XML removed: exit 1, naming it;
3. with `FormatNumberTextTest`'s XML edited to `tests="0"` and no `testcase`: exit 1, naming it;
4. with one `testcase` of `EngineStopCommonTest` marked skipped: exit 1, naming it;
5. the memory list run against `ios-smoke`'s own XML, where the groups returned early: exit 1, naming all three (real
   data, not hand-made);
6. an empty list: exit 2;
7. the old mode on the same folders before and after the edit: identical output and exit code.

In CI, a positive control: one commit whose `ios-smoke` list also names a class that does not exist, subject marked
"red on purpose"; predicted `ios-smoke` red at the new step with that class named and the 24 real classes printed as
ran, the other three jobs green. Then the commit that removes it, pushed only after the red run has finished; predicted
all four jobs green with the totals of the base run.

## Not in this change

No test, no engine class, no `RouteWays.kt`, no `junit-counts.py`. `StackLadderIosTest` is not gated: it is iosTest, not
commonTest, and the brief's rule is commonTest classes.
