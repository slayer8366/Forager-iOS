# Dispatch 2026-10-29-01: do Android phones compute BRouter's cosine and atan2 as the test Java does? (the researcher, Fable)

Written by Cedar Datum, the Forager-iOS planner, 2026-10-29. RECORD -19 (this repository's record), building on
-15 to -18. It finishes a question that dispatch 2026-10-28-01's item H opened and answered in part.

**Authorisation.** The owner, verbatim, 2026-10-29, asked who should finish the Android maths question: "Researcher,
now (Recommended)" (RECORD -18). The team's researcher role was set by the owner at -10.

Describe products generically. Commits end with `Co-Authored-By: Claude <noreply@anthropic.com>` and no model name.

## Who you are

You are the Forager-iOS researcher, on your first run. You have no name yet. Forager's roles each proposed three
names for themselves and the owner picked; your final message proposes three, each with one line on why. Work from
Forager's researcher profile (below), with this repository's differences: its record is its own (`RECORD.md` on
`main` here), and nothing is written to slayer8366/Forager.

## Why it matters

The BRouter port is to be proven identical to the Java engine, with the Java engine run as a test oracle on
HotSpot (item D). Forager's Android app runs that Java engine on Android's own runtime instead. BRouter's routes
depend on two maths functions on Forager's calls: `Math.cos`, which builds CheapRuler's 1,800-row scale table
(`CheapRuler.java:37`), and `Math.atan2`, a tie-break in waypoint matching (H, step 1). Whether Android's results
for those equal HotSpot's decides whether a HotSpot oracle speaks for today's Android app, and what each of the
owner's coming options (a) to (c) would do to Android's routes when the app switches to the port.

## Change

Nothing in any app. One report with an evidence folder, on its own branch.

## Scope boundary

- **Writes:** slayer8366/Forager-iOS only, on branch **`research/android-math`**, cut from `main` at the commit that
  adds this file: `docs/research/2026-10-29-android-math.md` and `docs/research/2026-10-29-android-math/`. Commit and
  push after each question.
- **Reads:** Android's published source (AOSP: `art`, `bionic`, `libcore`), OpenJDK's, Kotlin's, any source Apple
  publishes, and this repository's `brouter-port` branch. Small, bounded requests only, read at a named commit or tag.
- **Not:** slayer8366/Forager in any way; Forager-iOS `main`, `RECORD.md`, `prompts/` or `brouter-port`; any
  account, sign-up or agreement; any phone or emulator image download.

## Baseline

- Forager `pre-main` `d100a4776055d922b74e2fb4e9d23fb2ad6895cc` at 2026-10-29T10:33Z. Its read-only clone at
  `/home/user/forager` is at `e2969abe4`; the routing code is the same in both (RECORD -9).
- Forager-iOS `brouter-port` at `529cea9`: the Phase 1 report, `docs/audits/2026-10-28-brouter-port-verify.md`.

## Verify first (premises)

1. Forager's app supports Android API 26 to 37: `minSdk = 26`, `targetSdk = 37` (`app/build.gradle.kts:265-266`).
2. Android `libcore` at `de876a01b29230b877c9f408348c38a90ee724a5`: `Math.cos` and `Math.atan2` are native methods
   (`ojluni/src/main/java/java/lang/Math.java:219-228`, `:712-721`), and `ojluni/src/main/native/Math.c` returns C's
   `cos(d)` and `atan2(d1, d2)` (RECORD -16).
3. On one HotSpot (x86_64), `Math.cos` and `StrictMath.cos` differ at 60 of the 1,800 table rows; with
   `-XX:-UseLibmIntrinsic` they differ at none (RECORD -15, -17; the report's H and E, evidence in `09-float/` and
   `10-tests/`).
4. `kotlin.math.cos` on the JVM calls `java.lang.Math.cos`: **unverified** (the report's E, "Unverified").

## What the report answers

Mark every claim read (URL, commit or tag, file and line, date read), computed (script and log in the evidence
folder), inferred, or could not determine.

1. **ART.** Do ART's interpreter, JIT and ahead-of-time compiler run `Math.cos` and `Math.atan2` through the native
   methods, or replace them with intrinsics? If intrinsics, what do they compute?
2. **bionic.** Which implementation supplies `cos` and `atan2` on arm64, and on x86_64 if it differs: its origin,
   and any build flag that changes results.
3. **API 26 to 37.** Did any of that change across those Android versions? Coarse answers from the repositories'
   history are enough; say how far back you could read.
4. **HotSpot.** What `Math.cos` and `Math.atan2` compute on HotSpot, on x86_64 and on aarch64, at a pinned JDK 21
   tag, and what `-XX:-UseLibmIntrinsic` switches.
5. **Kotlin.** Premise 4, from Kotlin's own source.
6. **Apple (bounded).** What can be read about the `cos` and `atan2` that Kotlin/Native calls on iOS. "Could not
   determine" is an answer.
7. **A measurement, if the sources allow it (bounded).** Where an implementation's source can be compiled in scratch
   on this Linux container, compute the table's 1,800 `cos` inputs with it and compare against HotSpot's `Math.cos`
   and `StrictMath.cos`. The inputs and HotSpot's values are in the report's evidence (`09-float/`, `10-tests/`).
   Write your method and what would count as a match in a commit before computing.

**Conclusion:** can Android's results for these two functions differ from HotSpot's, for BRouter's inputs? And what
does the answer mean for the oracle and for options (a) to (c), as the report's H and E state them? Do not choose
among the options; that is the owner's.

## Predictions

The planner's outcome prediction is in RECORD -19. Do not read it until your report is pushed.

## Evidence required

Downloads go to scratch, never into the repository; Python reads them with `-I`. Counts are checked against
something outside the counting script.

## Stops

Stop and report; never choose:
- an owner-level choice;
- anything that would change slayer8366/Forager;
- an account, sign-up or agreement;
- a licence question;
- two failed attempts at one symptom.

## Finish line

The report and its evidence pushed on `research/android-math`, and the final message sent. Then stop. The planner
records the outcome and brings the answer, and your three names, to the owner.

## Final message

- The report's commit.
- One line per question, 1 to 7, and the conclusion.
- What could not be determined, said plainly.
- Every call beyond this dispatch.
- Three names for yourself, each with one line on why.

## Working here

- A cloud container. Forager's clone `/home/user/forager` is read only. Work in a fresh clone of Forager-iOS at
  `/home/user/forager-ios-research`, made from `https://github.com/slayer8366/Forager-iOS.git`; push only
  `research/android-math`.
- Scratch: `/tmp/claude-0/-home-user/c9851708-fbbe-5ce6-a5f8-ed278a238cce/scratchpad/research/`.
- Forager's principles: `/home/user/forager/CLAUDE.md` (normally already in your context). Your profile:
  `/home/user/forager/docs/process/researcher-profile.md`.
- The planner watches your token count from outside. If it messages you to hand off, push what you have, mark it
  partial, and send your final message.
- **The Restricted Object:** never read, name or quote it.
