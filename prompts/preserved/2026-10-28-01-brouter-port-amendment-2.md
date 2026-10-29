# Dispatch 2026-10-28-01, Amendment 2: Phase 2 begins, steps 0 and 1 only, then stop

Written by Cedar Datum, the Forager-iOS planner, 2026-10-29. RECORD -25, continuing -3, -4 and -24. Phase 1's
report is `docs/audits/2026-10-28-brouter-port-verify.md` on `brouter-port` at `20e1f92`; its item I is the order
this amendment starts.

**Authorisation.** The owner, verbatim, 2026-10-29, asked whether to start Phase 2: "Start, pause after checks
(Recommended)". The option read: "Run the first steps only: set up the project and CI, then download and check the
test tiles. Two big unknowns get answered cheaply before the 11,000-line port starts: whether Kotlin 2.3.10 builds on
GitHub's Mac runners, and whether BRouter 1.7.10 can still read today's brouter.de tiles. Then you'll be asked to
create the tile release, and for the go on the rest."

Describe products generically. Commits end with `Co-Authored-By: Claude <noreply@anthropic.com>` and no model name.

## Who builds it

The team's primary coder, on its first run (RECORD -10). It has no name yet: its final message proposes three, each
with one line on why, and the owner picks. It works from Forager's coder profile (`docs/process/coder-profile.md` in
slayer8366/Forager, `pre-main`) with this repository's differences: the record is this repository's own, written by
the planner, and nothing is written to slayer8366/Forager.

## Rulings this builds on (RECORD -24, verbatim there)

- **Maths:** "Copy Android's maths". `cos` and `atan2` come from bionic's FreeBSD msun, ported to common Kotlin;
  `hmap` keeps `java.util.HashMap`'s order (part 7's option (i)). Not built in steps 0 and 1.
- **Via points:** "Leave it out". The four methods in `11-plan/via-point-spans.txt` (429 lines) are not ported.
- **Flags:** 25% for speed and for memory (K).
- **The planner's calls:** the oracle runs on `ubuntu-24.04` with `-XX:-UseLibmIntrinsic`; the port's build uses
  Gradle 9.0.0 with Kotlin 2.3.10 exactly; package prefix `com.zynergylabs.forager.brouter`; the `-ro` jar.

## Change

**Step 0, the scaffold,** as item I's table describes it (report line 1691 at `20e1f92`; step 1 is line 1692), with
two planner's changes:
- **Gradle 9.0.0,** not 9.7.0, with its `distributionSha256Sum` read from Gradle's own published checksum.
- **A `macos-26` smoke job now, not at step 2b:** one `commonTest` test, run on `iosSimulatorArm64`, so the Kotlin
  2.3.10 and Xcode question is answered before the port starts. G's full simulator job still joins at 2b.

**Step 1, the tile capture,** as item I describes it, up to the release and not including it:
- download the four tiles E4 names (`W75_N40`, `W75_N35`, `W70_N40`, `E170_S50`) from `brouter.de/brouter/segments4/`
  into scratch, once each;
- load each in the oracle (a route and its integrity check), replacing a failing tile by E4's rule;
- record size, SHA-256, URL, `Last-Modified` and `ETag` in a pin file in the repository;
- copy F0's fixture from Forager's test resources at its SHA-256 (read only);
- write the ODbL 4.2 notices the release will carry.

**Then report how the captured bytes can reach a release on Forager-iOS, with options, and stop.** The tiles sit in a
cloud container that will not last; the owner has no Mac, and a fresh download from brouter.de gives different bytes.
Say which routes exist (for example a CI job that captures and publishes, or files handed to the owner), what each
asks of the owner, and which you recommend. **Create nothing public:** no release, no tag, no upload.

## Scope boundary

- **Writes:** slayer8366/Forager-iOS, branch `brouter-port` only:
  - `brouter/`;
  - the root Gradle files and wrapper;
  - `.github/workflows/`;
  - `tools/`;
  - the tile pin file;
  - a Phase 2 report, `docs/audits/2026-10-29-brouter-port-phase2.md`, with an evidence folder beside it.

  Commit and push after every step and sub-step.
- **Downloads:** the BRouter 1.7.10 release zip (D's pin), Gradle, Kotlin and okio from their usual repositories,
  and the four named tiles, into scratch or Gradle's caches, never into the repository.
- **Not:**
  - slayer8366/Forager in any way;
  - Forager-iOS `main`, `RECORD.md`, `prompts/`, `research/*`;
  - any release, tag, upload, account, or GitHub setting;
  - steps 1b onward. Item I lets steps 2 to 4 run while the release waits; the owner's "pause after checks"
    holds them too.

## Baseline

- Forager-iOS `brouter-port` `20e1f92`; `main` `3fc5cef` (RECORD -24).
- Forager `pre-main` `d100a4776` at 2026-10-29T19:54Z; the read-only clone at `/home/user/forager` is `e2969abe4`,
  with the same routing code (RECORD -9).

## Verify first (premises)

1. GitHub Actions runs on Forager-iOS. Not checked. If it is off, turning it on is a settings change: stop.
2. Gradle 9.0.0 with Kotlin 2.3.10's multiplatform plugin builds the three targets. JetBrains' table pairs them
   (RECORD -14); not built here. Part 2's probe built with Gradle 9.7.0.
3. `macos-26` carries Xcode 26.0.1, and Kotlin 2.3.10 warns above Xcode 16.0 (RECORD -14). Whether it links and runs
   a simulator test there is the question.
4. The tile sizes in part 4's listing are from 2026-10-29T03:54Z and will have changed. That is expected, not a
   wrong premise.
5. Whether BRouter 1.7.10 reads today's brouter.de tiles is unknown (Phase 1, F and E).

## Predictions (outcome, planner)

- Actions is on, and the `ubuntu-24.04` job is green within two pushes of the scaffold.
- Kotlin 2.3.10 links and runs the smoke test on `macos-26`, printing a warning about the Xcode version.
- BRouter 1.7.10 reads all four tiles, so none is replaced.

## Evidence required

- **Tests first, over stubs,** each seen failing for its reason (item I, "How every step runs").
- **One revert check per step,** under Forager's `CLAUDE.md` runner rules: compile log first, restore from a saved
  copy, forward change confirmed afterwards.
- **Counts read from the JUnit XML.**
- **CI results read from the run itself,** matched to the commit pushed. The GitHub tools for this session load
  through ToolSearch (`actions_list`, `get_job_logs`).

## Stops

Stop and report; never choose:
- Actions being off;
- Kotlin 2.3.10 failing to build or run on `macos-26` after one fix attempt. Report the options, among them C's
  language pin with a newer compiler.
- a tile the 1.7.10 oracle cannot read, if E4's replacement rule also fails;
- anything public, the release among them;
- anything that would change slayer8366/Forager;
- an account or a setting;
- a wrong premise;
- two failed attempts at one symptom.

## Finish line

Steps 0 and 1 are pushed on `brouter-port`, and the `ubuntu-24.04` job is green with its counts read. The smoke job's
result on `macos-26` is read, pass or fail. The tiles are captured and checked, with the pin file pushed, and the
release routes are reported. Then stop and send the final message. The planner records it and brings the release
and the go on step 1b onward to the owner.

## Final message

- Commits, and the CI runs with their commits and results.
- Each prediction above, held or not.
- The failing-first evidence and the revert checks.
- The tiles: size, SHA-256, `Last-Modified`, and whether the oracle read each.
- The release routes, with a recommendation.
- Every call beyond this amendment; what is unverified.
- Three names for yourself, each with one line on why.

## Working here

- A cloud container. Forager-iOS clone: `/home/user/forager-ios`, on `brouter-port`; never check out another branch
  there. Forager's clone `/home/user/forager` is read only.
- Scratch: `/tmp/claude-0/-home-user/c9851708-fbbe-5ce6-a5f8-ed278a238cce/scratchpad/phase2/`. Phase 1's scratch
  (`.../hazel/`) holds the release zip and probes; re-check before relying on any of it.
- Maven Central may answer 429; the mirror `https://maven-central.storage-download.googleapis.com/maven2/` worked
  before. Do not commit a mirror into the build.
- The planner watches your token count from outside. If it messages you to hand off, push what you have, mark it
  partial, and send your final message.
- **The Restricted Object:** never read, name or quote it.
