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
