# s64: the stopped-clock test's common sibling (Plumb Ledger, session 64)

RECORD -148, decision 2, verbatim: "the stopped-clock test gets a common sibling held to F0's stored row. The JVM test
stays as it is. That adds a test and changes none." Base `070e29d` on `brouter-port-5b`.

## The test (written before any run)
`EngineStopCommonTest.forager30sLimitOnAStoppedClockRoutesAsF0sStoredRow`: F0's first case
(`F0-inside-across-the-degree-line`), through `RoutingEngine` with `doRun(30_000)` (Forager's own limit) on an
`EngineClock` that always reads 0, `hiking-mountain`, memory class 64, `MsunLibm`, Forager's params, as the class's
other tests and `EngineRun.runTrack` build it. The outcome is computed with `EngineRun`'s own `kindOf`, `fnv1a` and
`fnv1aShorts` into an `EngineRun.Outcome`, and held to the stored row on all six fields and the tuple digest.

**Where the stored row is read from.** The dispatch names `F0RouteCommonTest`'s helper or row. Its `CASES` and
digests sit in a `private companion object`, so another class cannot read them without a change outside this test's
file (a stop). `F0EngineCommonTest.CASES` holds the same expected-1b.tsv row (same kind, nodes, cost, distance,
nodeDigest, heightsDigest: read at `F0EngineCommonTest.kt:53` and `F0RouteCommonTest.kt:67`, equal), in an `internal`
companion object, and adds the stored tuple digest. The new test reads `F0EngineCommonTest.CASES.first()` (ends and
row together, so no number is copied) and compares as `F0EngineCommonTest` does, which is the engine-level form of
`F0RouteCommonTest`'s comparison: the five fields that one holds plus cost, which `route()` cannot read but the
engine can, plus the tuple digest. Decided beyond the letter of the dispatch: six fields rather than five. Stricter,
same row; reported here so the planner can overrule it.

## What it could catch, and what it could not
Could catch: a port whose 30 s limit strikes without the injected clock having moved, i.e. a limit that compares
something other than elapsed injected time (an inverted or constant comparison, a limit read from a different clock or
from a counter of iterations), and any change that makes a 30 s-limited run differ from F0's stored route on the
stopped clock. It runs on the iOS simulator as well as the JVM.
Could not catch: a limit that never strikes at all (the clock never moves, so "never strikes" is what it expects;
`theTimeLimitReadsTheInjectedClock` covers that side); the oracle comparison the JVM test makes (no oracle on iOS: this
is held to the stored row the oracle wrote, not to a live oracle run); any limit behaviour on a real moving clock.

## The plant (one string, `RoutingEngine.kt:724`)
`clock.nowMillis() - startTime > timeout` becomes `clock.nowMillis() - startTime >= 0`.

Why this plant: it is a limit that strikes whenever a limit is set, whatever the clock says. It is exactly the wrong
port the existing common test cannot see, because that test's clock moves past the limit, so under the plant it gets
the timeout it expects for the wrong reason. A plant that both tests catch (an inverted `<`) would show the new test
bites but not that it adds anything.

**Prediction for the new test (fails).** First pass of the loop at `RoutingEngine.kt:714`: elapsed 0 `>= 0`, so the
first operation throws `IllegalArgumentException("target-island-check timeout after 30 seconds")` (the operation name
the existing test's message shows), caught as an Exception, empty track. The outcome is
`family:timeout|0|0|0|cbf29ce484222325|cbf29ce484222325` (empty FNV-1a is the offset basis), so the six-field
assertion fails with an AssertionError, expected `route|226|5302|5047|9ca48d05a90f4dcd|afc27854ee968af5`, actual the
timeout outcome above.

**Prediction for `theTimeLimitReadsTheInjectedClock` (passes).** Same message, same empty track, not terminated: its
assertions hold under the plant. The watchdog test (limit 0, the `maxRunningTime > 0` branch is not entered) and the
Error and Exception tests (the clock throws on its second read, which is no later than the planted line's read, so
the planted comparison is never evaluated; which read is second, `:549` or `:724`, was not traced) also pass. Predicted class result under the plant: 5 tests, 1 failure, the new test.
