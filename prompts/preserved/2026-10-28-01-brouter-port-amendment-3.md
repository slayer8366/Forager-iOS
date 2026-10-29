# Dispatch 2026-10-28-01, Amendment 3: the tile release, then Phase 2 through the port (steps 1b to 12)

Written by Cedar Datum, the Forager-iOS planner, 2026-10-29. RECORD -28, continuing -3, -24, -25 and -27. Amendment 2's
evidence rules, scope rules and "Working here" carry on, except where this amendment changes them.

**Authorisation.** The owner, verbatim, 2026-10-29, after steps 0 and 1:
- **The release:** "Yes, upload these (Recommended)". The option read: "A session creates a draft release under tag
  'brouter-tiles-2026-10-29', uploads the 5 files and the README, and downloads each one back to check it against its
  fingerprint. Only then does it publish. A failed upload leaves nothing public. If the session's network won't pass
  the 114 MB file, it stops and reports."
- **The rest of Phase 2:** "Continue, built-in stops (Recommended)". The option read: "Runs in Hazel's order, one
  checked session per step. It stops for you if the Java engine proves unstable on more than 5% of any test set, for
  anything public, on a wrong premise, or after two failed fixes. I'll report at each milestone: the Java engine's own
  runs, then each package, the full JVM run, the iPhone simulator run and the timing."
- **The coder's name:** "Linden Caliper (Recommended)". Profile: `docs/process/coder-profile.md` on `main`.

Describe products generically. Commits end with `Co-Authored-By: Claude <noreply@anthropic.com>` and no model name.

## Part A: the release, exactly as ruled

1. **Check first:** the six staged files in `.../scratchpad/phase2/release-staging/` against `tools/tile-pins.tsv` (size
   and SHA-256), and `README.md` against `tools/tile-release/README.md` (byte for byte). Any mismatch: stop.
2. **Create a draft release** on slayer8366/Forager-iOS:
   - tag `brouter-tiles-2026-10-29`, targeting `91754bc`, the first commit holding both the pin file and the notices.
     Neither has changed since (checked at 21:17Z).
   - title "BRouter test tiles, 2026-10-29";
   - body: `tools/tile-release/release-notes.md`.
3. **Upload the six assets.** The four tiles, `forager-fixture-W75_N40.rd5` and `README.md`, each under the asset name
   the pin file gives.
4. **Read every asset back from the draft.** Download each through the API and check its size and SHA-256 against the
   pins, and the README against the committed file. Any mismatch: do not publish; stop.
5. **Publish,** by setting the release to non-draft.
6. **Read each asset back once more, anonymously,** with no token, from its public URL. This answers Phase 1's open
   question of whether a public asset downloads without one.
7. **Wire `tools/fetch-tiles.sh` into the build** where a test first needs a tile, and add one test that fetches each
   tile and checks its pin. That is item I's step 1 test, "after the release: both jobs fetch each tile and check its
   pin". Seen failing first by a one-character pin change (item I's revert), then green in CI.

**Stops in Part A:**
- an upload the proxy refuses, or a draft that cannot be created. Leave any draft as it is and report; the planner
  decides whether it is deleted;
- any read-back mismatch;
- anything public other than this one release. No other tag, release or upload.

## Part B: steps 1b to 12, in item I's order (Phase 1 report, lines 1655 to 1754 at `20e1f92`)

**One session per step or sub-step,** each reading this amendment, the RECORD entries the planner names, item I's row for
its step, and the Phase 2 report's handoff. Nothing more unless the step needs it. Each session ends with a push and a
handoff for the next.

**The rulings change item I as follows (RECORD -24):**
- **Step 2b, `cos` and `atan2`: "Copy Android's maths".**
  - Port bionic's FreeBSD msun functions to common Kotlin, keeping msun's evaluation order. That is `s_cos.c` with its
    inline `e_rem_pio2.c`, `k_cos.c`, `k_sin.c`, and `e_atan2.c` with `s_atan.c`, from bionic `android-17.0.0_r1`, as
    the research measured (`research/android-math`, `q7/`).
  - Arguments that would need `k_rem_pio2.c` (huge arguments, which no latitude reaches) become a named "unsupported"
    failure rather than untested code.
  - **Tests, bit for bit:**
    - `cos` against the research's msun table, at all 1,800 table inputs (13 rows differ from `StrictMath`, by design);
    - `atan2` against `StrictMath.atan2` on the 299,974 pairs, where msun and fdlibm agree everywhere;
    - edge values: signed zeros, NaN, infinities, subnormals.
  - **An independent reference in CI:** the `ubuntu-24.04` job may build bionic's C files from their pinned commit with
    Android's flags (`-ffp-contract=off -fno-builtin -fno-math-errno`), as the research did, and compare against them.
- **The seam for the two kinds of run:**
  - **logic runs** inject the oracle's own table through E's seam, so the port's logic is proven exactly against the
    oracle;
  - **production runs** use the msun table and record any route difference, by stratum, per E6 and J.
- **Step 5b, `hmap`:** part 7's option (i), a common-code copy of `java.util.HashMap`'s order for this one map. Size it
  at the start of the step and report the count before writing it.
- **Step 8, via points:** the four methods in `11-plan/via-point-spans.txt` are not ported. If anything reaches their
  guard with a waypoint named other than "from" and "to", it fails as a named "unsupported".
- **K's flags:** margin 0.25 for speed (S) and memory.

**The planner's calls (RECORD -24, -28):**
- **The oracle** runs on `ubuntu-24.04` with `-XX:+UnlockDiagnosticVMOptions -XX:-UseLibmIntrinsic`.
- **The oracle's JDK** is CI's pinned Temurin 21.0.13+9. Step 1b first checks that the no-intrinsic `Math.cos` table on
  that JDK equals Phase 1's measurement on OpenJDK 21.0.12.1. A difference is a stop.
- **The rest of the step 0 ruling:** each step's port-side tests arrive with the step that ports them. The default run
  means "everything ported so far passes".

**Milestones, where a session reports and the planner records before the next step starts:** 1b; each package (2a and
2b, 3, 4, 5a to 5c, 6 and 7, 8a to 8d); 9; 10; 11; 12.

**Stops in Part B** (the owner's "built-in stops", and Amendment 2's):
- **E3:** more than 5% of any stratum unstable in the oracle's own runs;
- **anything public,** beyond Part A's release;
- **a wrong premise,** among them a ruled option proving infeasible (for example, msun's `cos` not reproducible bit
  for bit in common Kotlin);
- **two failed attempts at one symptom;**
- **anything that would change slayer8366/Forager, any account or setting;**
- **a simulator failure** that one fix does not clear;
- **a merge into `main`.** Code merges into `main` only on the owner's word (this repository's `CLAUDE.md`).

## Finish line

Phase 2 ends with step 12, the Phase 2 report:
- E's results against its figures;
- J scored;
- K's figures with the 25% flags;
- every iOS result marked simulator-checked.

Then the planner records the terminal for -3, and the owner decides about `main` and about Forager's switch.

## Working here

As Amendment 2's "Working here". The GitHub API's release endpoints are reached the way step 1's session priced them,
through this session's proxy. No other write to GitHub is made besides pushes to `brouter-port` and Part A's one
release.
