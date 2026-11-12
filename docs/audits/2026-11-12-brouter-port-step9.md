# BRouter port, step 9: the groups through the port

Plumb Ledger, session 39, dispatched by Cedar Datum: step 8d's two leftovers, then step 9's first groups. Branch
`brouter-port-5b`, from `e1940ed`.

Tags: **[R]** read in the source at the file and line named; **[C]** counted or shown by a tool's output, named;
**[I]** inferred, not shown. Java is BRouter 1.7.10 as vendored in Forager (`third_party/brouter/src/main/java/btools/`).

## 1. Session 39

### 1.1 The constructor's exception through `route()` (step 8d's leftover)

Test `RouteTest.aMissingProfileThrowsThroughRouteAsTheOracle`, committed before any run (`8aef87e`; the commit names what
is compared).

Forward run [C: `RouteTest`'s XML, single class]: 7 tests, 0 failures, 0 errors. Printed: oracle
`java.lang.IllegalArgumentException "profile * does not exist"`; port `java.lang.IllegalArgumentException "profile * does
not exist"`. Same kind, same message.

Revert, PREDICTION (pushed before the run): `Route.kt`'s engine construction wrapped so that an exception from the
constructor returns `RouteOutcome.Failed(message)`. Expected: `RouteTest` 7 tests, exactly 1 failure,
`aMissingProfileThrowsThroughRouteAsTheOracle`, message beginning "the port's route() throws (it returned Failed)"; the
other 6 pass (none of them reaches a throwing constructor). Restored `cmp`-equal by a scratch copy of `revert.sh`.

Revert, OBSERVED [C: the scratch `revert.sh`'s output, `2026-11-12-brouter-port-step9/01-ctor-revert-out.txt`; the edit,
`01-ctor-revert.diff`]: compile errors 0, 1 fresh XML; `RouteTest` 7 tests, 1 failure,
`aMissingProfileThrowsThroughRouteAsTheOracle`, "the port's route() throws (it returned Failed)", as predicted. Restored:
`cmp` equal; `git status` clean after, the forward `Route.kt` unchanged (the revert's `catch` string: 0 occurrences).
