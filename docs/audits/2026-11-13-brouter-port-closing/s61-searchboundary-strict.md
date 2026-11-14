# s61: SearchBoundaryTest compares every throw by class and message (RECORD -165)

The owner's ruling, RECORD -165: "Add the flag (Recommended)".

Base: `brouter-port-5b` at `b0fbf62`, checked against `git ls-remote` before starting.

## The change

- `brouter/src/jvmTest/.../router/SearchBoundaryTest.kt`: every `isInBoundary` and `getBoundaryDistance` throw is
  compared by class AND message. The `IllegalArgumentException`-only condition on the message (lines 69-72) and its
  comment go. Sampling, seeds and reach assertions are unchanged.
- `brouter/build.gradle.kts`: `-XX:-OmitStackTraceInFastThrow` in the `tasks.named<Test>("jvmTest")` block only, citing
  RECORD -165. Not in `oracleJvm()`, which `e3Strata` shares.

## The plant

`CheapRuler.getLonLatToMeterScales` (commonMain): the row is one lower when `ilat` is negative, i.e.
`ilat / SCALE_CACHE_INCREMENT - 1` for `ilat < 0`. In the test the port's table is `OracleLibm`, which looks the row up
through Java's `CheapRuler.getLonLatToMeterScales(row * 100000)`, so a negative row throws Java's own
`ArrayIndexOutOfBoundsException`.

## Predictions (written before any run)

The plant acts on two ranges, and they predict differently:

- `ilat <= -100000`: Java throws `Index -k out of bounds for length 1800`; the planted port throws the same class with
  `Index -(k+1) out of bounds for length 1800`. Class equal, message one lower.
- `-100000 < ilat < 0`: Java's row is 0 (truncation toward zero) and returns a value; the planted port's row is -1 and
  throws. Class differs (`null` against `ArrayIndexOutOfBoundsException`).

The second range looks reachable from the committed sampling (read, not run): a centre in the `lat = 0` corner band
(`pos`, about 1 in 74 cases) has four probes within 20,000 of the centre, so with a positive radius the distance's
midpoint latitude falls in `(-10000, 0)` about half the time; `getBoundaryDistance` in directions 1 and 3 looks the row up
at the probe's own latitude, which is in that range for the same probes.

- **(a) committed test + plant: predicted FAIL**, against the dispatch's expected pass, on a class mismatch of the form
  `case N probe K ... : isInBoundary, exception` (or `direction D: exception`) with expected `<null>` and actual
  `<class java.lang.ArrayIndexOutOfBoundsException>`. If it passes instead, the reading above of what the sample reaches
  is wrong, and the gap is as the reviewer described.
- **(b) strict test + plant: predicted FAIL**, either on the same class mismatch or, if a both-throw case comes first,
  on a message mismatch `expected: <Index -k out of bounds for length 1800> but was: <Index -(k+1) out of bounds for
  length 1800>`.
- **(c) strict test + unplanted port: predicted PASS** with the flag on. Without it, a JIT-compiled fast throw on one
  side only could give a `null` message and fail; the flag is what removes that.

If (a) fails, the dispatch's expected result for (a) has not happened, which is a stop: it is reported, and no second
plant is tried.

## Results

**Status: STOPPED after (a). Partial.** Steps 3-8 of the dispatch were not done; the test and `build.gradle.kts` are
unchanged at this commit.

**(a) committed test + plant: FAILED**, as predicted above and against the dispatch's expected pass. Run with the
supplied `revert.sh` (filter `*SearchBoundaryTest`), plant exactly as described above (the runner's diff shows the one
line in `CheapRuler.kt`). Runner output: build log has 0 `e: ` lines; 1 fresh JUnit XML; tests 1, failures 1. The
message:

`case 9 probe 12 cost 0 radius 6652: isInBoundary, exception expected:<null> but was:<class java.lang.ArrayIndexOutOfBoundsException>`

This is the shape predicted for the `-100000 < ilat < 0` range (a positive radius, the oracle returns, the planted port
throws): a failure this plant can produce, not a stale one. After the run `cmp` against the runner's saved copy was
equal, `git status --short` and `git diff --stat` were empty.

**What this means for the item.** As the dispatch specifies the plant, it is caught by the committed test on class
alone, so it does not demonstrate the reviewer's gap. The gap itself is still predicted to exist for `ilat <= -100000`
only (both throw, same class, message one lower), but showing it needs a different plant, for example the row one lower
only when `ilat <= -SCALE_CACHE_INCREMENT`. Choosing that plant is a change to the experiment the dispatch fixed, so it
is left to the planner. The dispatch's stop applies: "The predicted result of (a), (b) or (c) does not happen. Do not
try a second hypothesis; report what you saw."

**Not checked:** (b), (c), the strict test change, the flag, the full `jvmTest` run. Which of the two ranges the
reviewer's own plant hit is not known here; their runs were not reproduced beyond this one.

## Session 62

Base: `brouter-port-5b` at `ae4dfa2`, checked against `git ls-remote` and a clean `git status` before starting. Session 61's
part above is unchanged; this section continues it with the plant corrected in RECORD -166.

### The plant, P2 (RECORD -166)

The row is one lower only when the row itself is below 0. In `CheapRuler.getLonLatToMeterScales` (commonMain), the one
exact string

- old: `libm.scales(ilat / SCALE_CACHE_INCREMENT)`
- new: `libm.scales((ilat / SCALE_CACHE_INCREMENT).let { if (it < 0) it - 1 else it })`

For `-100000 < ilat < 0` the row is 0 and stays 0. For `ilat <= -100000` the row is `-k` (k >= 1) in Java and `-(k+1)` in
the planted port, and both throw `ArrayIndexOutOfBoundsException` (the port's through `OracleLibm`, which calls Java's
`getLonLatToMeterScales(row * 100000)`). Rows at or above 1800 are not touched. So P2 changes only cases where both
already throw, and only the index in the message.

### Predictions (written and pushed before any run)

Sampling, read not run: rows at or below -1 are reached. A centre in the lowest latitude band (`lat = 0` after
`(c.ilat / step) * step`: the corner branch with latitude index 0, or the uniform branch below 5,000,000; about 1 in 36
centres, so roughly 80 of 3000) gets probes on `lat - step` and `lat - 1_000_000` (each +-1), latitudes around -5,000,000
and -1,000,000, rows around -50 and -10. With a positive radius `isInBoundary` takes the distance at the midpoint latitude
(row around -25 or -5 for a centre near 0); `getBoundaryDistance` in directions 1 and 3 looks the row up at the probe's
own latitude (session 61's reading, not re-derived here). Both are well inside the `ilat <= -100000` range, so P2's
both-throw cases are in the sample. I agree with the planner's three expectations.

- **(a) committed test + P2: predicted PASS.** Every case P2 changes throws the same class on both sides; the committed
  test compares `isInBoundary` throws by class only and `getBoundaryDistance` messages only for
  `IllegalArgumentException`. This is the gap the reviewer described.
- **(b) strict test + P2 (flag on): predicted FAIL** on a message mismatch, first case unknown in advance, of the form
  `case N probe K ... : ... expected:<Index -k out of bounds for length 1800> but was:<Index -(k+1) out of bounds for
  length 1800>`, from either `isInBoundary` (radius mode) or `getBoundaryDistance` (direction 1 or 3). A class mismatch, a
  `null` message, or an index differing by anything but one lower would not be this prediction.
- **(c) strict test + unplanted port (flag on): predicted PASS.** If it fails on a `null` message, the flag has not
  removed the fast throw, which is a stop.

### Session 62 results

**Status: complete.** All three results are as predicted in the section above (committed at `ac33403` before any run).

- **(a) committed test + P2: PASSED.** Run with `revert.sh`, filter `*SearchBoundaryTest`; the runner's diff is the one
  line in `CheapRuler.kt`. Build log 0 ` e: ` lines; 1 fresh XML; tests 1, failures 0. The test's own tally shows the
  sample reached P2's cases: `radius throws=1380`, `distance throws=1368`. Afterwards `cmp` equal, `git status` clean.
- **Strict test and flag, `5ca6310`:** `SearchBoundaryTest` compares class and message on every `isInBoundary` and
  `getBoundaryDistance` throw; the `IllegalArgumentException`-only condition and its comment are gone; sampling, seeds
  and reach assertions unchanged. `-XX:-OmitStackTraceInFastThrow` is in the `jvmTest` block only, citing RECORD -165.
- **(b) strict test + P2: FAILED** on a P2-specific message. Build log 0 ` e: ` lines; 1 fresh XML; tests 1, failures 1:
  `case 3 probe 0 direction 3: message expected:<Index -4[8] out of bounds for l...> but was:<Index -4[9] out of bounds for l...>`
  (JUnit's brackets mark the differing digit: -48 against -49, exactly one lower). Afterwards the port matched HEAD, and
  the test and build changes were byte-equal to copies saved before the run.
- **(c) strict test + unplanted port: PASSED.** Tests 1, failures 0; 0 ` e: ` lines; 1 fresh XML; the same throw tallies.
- **Full `:brouter:jvmTest`** (`cleanJvmTest` then `jvmTest`, 1 h 8 min): tests 244, failures 0, errors 0, skipped 0 in 107
  fresh files, the same as the last CI. A first attempt with `--rerun-tasks` was stopped by the session's 30-minute
  background limit at 128 passed, 0 failed, and is not counted.

**Not checked:** that the flag reaches the test JVM's command line (inferred from the `jvmArgs` call, not observed;
(c) would pass without it unless a fast throw happened to fire); CI on these commits; `e3Strata` (unchanged, not run).
The E1 scan printed 0 on `ac33403`, `5ca6310` and this commit.
