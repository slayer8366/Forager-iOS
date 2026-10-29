# BRouter port, Phase 2: steps 0 and 1 (dispatch 2026-10-28-01, Amendment 2)

Written by the team's primary coder (first run, not yet named), 2026-10-29, for Cedar Datum, the planner. The amendment is
`prompts/preserved/2026-10-28-01-brouter-port-amendment-2.md` on `main` at `c5c9411`. Phase 1's report,
`docs/audits/2026-10-28-brouter-port-verify.md` at `20e1f92`, is the plan; "item I" below is its Phase 2 order.

Tags: **[R]** read (file and line at a stated commit, or URL and date); **[C]** computed, with its log in
`2026-10-29-brouter-port-phase2/`; **[I]** inferred; **[?]** could not determine. iOS results are "simulator-checked".

## Bases [C: `2026-10-29-brouter-port-phase2/00-bases.txt`]

- **Forager-iOS `brouter-port`** is `20e1f92`, as the amendment states.
- **Forager-iOS `main`** is `c5c9411`, not the amendment's `3fc5cef`. The move adds RECORD -25 and the amendment itself
  (`git diff --stat 3fc5cef c5c9411`: `RECORD.md` and the amendment file, 179 insertions). It changes no premise.
- **Forager `pre-main`** is `d100a4776` (`git ls-remote`, 2026-10-29T19:59Z), as stated. The read-only clone is `e2969abe4`.
- **Actions on Forager-iOS:** no workflow exists yet (`list_workflows`: 0). Whether Actions is enabled cannot be read through
  this session's proxy (`actions/permissions`: HTTP 403 "not permitted through this proxy") **[?]**. The first push of a
  workflow answers it: a run appears or it does not.

## The planner's change to item I's step 0 row (ruling, 2026-10-29)

Item I's step 0 tests were "the oracle jar's SHA-256; one case per tile-free failure kind on both engines (FK 6, data
missing: a folder without the tile), failing on the stub" (report line 1691 at `20e1f92`). Step 0 ports no code, so the
port half of FK 6 could not pass before step 5a, while the amendment's finish line needs the `ubuntu-24.04` job green. I
asked rather than chose. The planner ruled option (c), verbatim in substance:
- step 0 carries the oracle pin test and **the oracle half of FK 6**;
- the port half of FK 6 is written at step 5a, over stubs, seen failing, then made green there;
- rejected: keeping it red for five steps (a red-by-design job cannot tell a real failure from the planned one), and moving
  a written test out of the default run (silencing, which Forager's `CLAUDE.md` rules out);
- **the rule from here on:** each step's port-side tests arrive with the step that ports them, so the default run means
  "everything ported so far passes";
- the oracle half of FK 6 is listed "not shown to bite" unless a revert of its own bites; step 0's named revert stays the
  oracle pin's SHA-256 changed by one character, which the fetch must refuse.

## Mechanism prediction (written before building)

**Step 0.**
- **The build:** Gradle 9.0.0 by the wrapper, `distributionSha256Sum` from Gradle's published checksum; one KMP module
  `:brouter` with Kotlin 2.3.10 exactly, targets `jvm` (JVM target 17), `iosArm64`, `iosSimulatorArm64`; okio 3.18.2.
  No atomicfu yet: B row 8's lock is step 6's.
- **The oracle reaches the JVM tests** through one Gradle task that calls `tools/fetch-pinned.sh` for the release zip (size
  and SHA-256 as Forager's `fetch-brouter.sh:23-28`), extracts `brouter-1.7.10-ro.jar` and `profiles2/`, and checks the jar's
  own SHA-256 with the same script. `jvmTest` alone depends on it, as a file dependency. A wrong pin fails the task, so the
  tests never run on an unpinned jar.
- **The tests:** (1) the oracle pin, asserting that `btools.router.RoutingEngine` is loaded from a file whose SHA-256 is the
  pin held in the test source (a second copy, so a build-file pin edit is caught); (2) the oracle half of FK 6, a folder
  without the tile, asserting the engine's own message; (3) the smoke test in `commonTest`, which on `macos-26` answers only
  whether Kotlin 2.3.10 links and runs a simulator test. Its assertion is incidental; what it proves is read from the JUnit
  XML's count for `iosSimulatorArm64`.
- **The entry point** is C's sketch as stubs, each body `TODO()`. No test calls it in step 0 (the ruling above).
- **CI:** `jvm` on `ubuntu-24.04` runs `jvmTest` and both iOS klib compiles; `ios-smoke` on `macos-26` selects
  `Xcode_26.0.1` and runs `iosSimulatorArm64Test`. Each job prints its JUnit XML into the log, so counts are read from the run.

**Step 1.**
- The four tiles are downloaded once each with `curl`, headers kept; each is loaded in the oracle by a scratch harness that
  runs `PhysicalFile.checkFileIntegrity` and one route between two of the tile's own nodes.
- The pin file records each tile's size, SHA-256, source URL, `Last-Modified` and `ETag`. Its revert check runs
  `tools/fetch-pinned.sh` on the captured copies with one size off by one, which must refuse.

**Predicted outcomes** are the planner's (amendment, "Predictions"); scored below.

## Step 0: results

Not yet run.

## Step 1: results

Not yet run.

## How the captured bytes can reach a release

Not yet written.

## Confirmed vs inferred

Not yet written.

## Could not determine

Not yet written.

## Premises that were wrong

- Forager-iOS `main` was `c5c9411`, not `3fc5cef`; the move is RECORD -25 and the amendment only, so no premise changes.

## Decided beyond scope

Not yet written.
