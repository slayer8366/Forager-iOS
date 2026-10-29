# Dispatch 2026-10-28-01, Amendment 4: the tile release by a workflow the owner runs

Written by Cedar Datum, the Forager-iOS planner, 2026-10-29. RECORD -30, continuing -28 and -29. It replaces Amendment
3's Part A. Part B stands, and its step 1b still waits for the release.

**Authorisation.** The owner, verbatim, 2026-10-29, after the session's proxy refused to create releases (RECORD -29):
"A button for you (Recommended)". The option read: "I add one small workflow file to main. Main changes only on your
word, and choosing this is that word, for this one file. You open Forager-iOS's Actions page on GitHub and press 'Run
workflow'. It downloads fresh copies from brouter.de, checks each in BRouter 1.7.10 the same way Linden did, and
publishes the release with the attribution only if all pass. A session then pins the published files. These are new
files, not today's. Not yet known: whether the repository lets workflows create releases; the first run shows it."

Rejected: "Let a push trigger it", which routes around the session's refusal; "Hold the release".

Describe products generically. Commits end with `Co-Authored-By: Claude <noreply@anthropic.com>` and no model name.

## Change

Linden writes **`.github/workflows/publish-tiles.yml`** on `brouter-port`. The planner reads it and copies it unchanged
to `main`, and the owner presses "Run workflow". GitHub shows that button only for a workflow on the default branch
(RECORD -29, read at source).

**Two jobs:**
1. **`capture`, with `contents: read`.**
   - Check out `brouter-port`'s tools at a commit SHA written into the file.
   - Set up the pinned JDK, and fetch the oracle with `tools/fetch-oracle.sh`.
   - Download each of the four tiles once from `brouter.de/brouter/segments4/`, keeping the response headers.
   - Fetch F0's fixture from slayer8366/Forager at `e2969abe4`, and refuse it unless it matches its pinned size and
     SHA-256.
   - Check each brouter.de tile in the 1.7.10 oracle as step 1 did: lookup version, the version-checked open,
     `checkFileIntegrity`, every cell decoded, one route.
   - Write each file's size and SHA-256 into a pins file and into the README's table.
   - Hand the files to the next job as a workflow artifact of this run.

   **Any failure fails the run, and nothing is published.**
2. **`publish`, with `contents: write`.** It runs only for `workflow_dispatch` from `main`, and only after `capture`
   succeeds.
   - Take the artifact from the same run, so the published bytes are the checked bytes.
   - Refuse if the tag already exists.
   - Create a **draft** release, tagged `brouter-tiles-<capture date, UTC>`, targeting the tools commit. Its notes are
     `tools/tile-release/release-notes.md`.
   - Upload the five data files, the README and the pins file.
   - Download each asset back and compare it with the pins.
   - Only then publish the release.

**Triggers:**
- `workflow_dispatch`;
- `push` to `brouter-port` when this file or the check harness changes, as a **dry run**: `capture` only, `publish`
  skipped.

**Prove it before the copy:**
- the dry run is green on `brouter-port`, read from the run with the four tiles' check lines;
- one dry run is made to fail by a single changed character, for example one byte of F0's pinned SHA-256, and it must
  refuse.

**Then report to the planner:**
- the commit;
- the dry runs;
- the file's full text;
- what the owner will see and press, in a few plain lines.

## Scope boundary

- **Writes:** `brouter-port` only:
  - the workflow;
  - the harness under `tools/`, moved there from the report's evidence if needed;
  - the Phase 2 report.
- **Not:**
  - `main`;
  - running `workflow_dispatch`;
  - any release, tag or upload, by any route;
  - Amendment 3's Part B steps.

## Stops

- **F0 cannot be fetched from Forager anonymously in CI.** Report the options; do not commit F0 or a token.
- **The dry run fails twice on one symptom.**
- **Anything that would publish from a session,** or from a push.
- **Anything that would change slayer8366/Forager, an account or a setting.**

## After the owner's run

A later session pins the release:
- read each asset anonymously;
- write `tools/tile-pins.tsv` from them;
- wire `tools/fetch-tiles.sh` into the build, with its fetch test seen failing first;
- read CI.

Then Amendment 3's Part B continues at step 1b. If the run shows the repository will not let workflows create
releases, that comes back to the owner.
