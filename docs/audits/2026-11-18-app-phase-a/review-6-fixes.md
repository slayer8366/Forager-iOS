# Review 6 fixes: S1 to S4 and S6 (session 87, Plumb Ledger)

The review: `docs/reviews/2026-11-18-review-6-a1-app-shell.md` (Samphire Witness), read at
`/home/user/forager-ios-review`. Base: `app` at `8bce937` (HEAD and `origin/app` equal after `git fetch origin`, tree
clean). `git diff --stat df49ee5..8bce937 -- . ':!docs'` is empty, so the review's line numbers (taken at `df49ee5`)
hold for every file edited here. Every iOS result is "simulator-checked" ("3 no iPhone"). No coordinate is written here;
fractions are rounded or split for the E1 scan.

## 1. Predictions (written and pushed before any code)

### Step 1, S1 (`tools/png-region.py`)
New self-test cases, run against the unfixed script (the self-test runs the tool through its command line,
`png-region-test.py:92-94`):
- a file cut inside IEND's CRC: Python's `struct.error` from `png-region.py:40`, a traceback, **exit 1**;
- a header chunk of 12 bytes: `struct.error` from `:104`, **exit 1**;
- width 0: `FAIL: 0/0`, **exit 1** (`:175-179`);
- height 0: `FAIL: 0/0`, **exit 1**;
- a band that selects no rows (2 rows, `--band` 0.01: `skip` rounds to 1, `rows[1:1]` is empty, `:132-133`):
  `FAIL: 0/0`, **exit 1**. Not in the reviewer's three; it is the brief's rule "return 2 when the band holds no
  pixels" reached without a zero dimension.
All five cases FAIL in the self-test before the fix and pass after it (exit 2 with an `UNSUPPORTED` line). The
existing 37 cases stay green.

### Step 2, S2 (the self-test)
- (a) Three cases, one channel out by 9 at tolerance 8 and the other two exact, one per channel: exit 1 today. One
  case per channel because `:141` has two `and`s and a pixel with one channel out catches only the mutations that
  join that channel by `or`: R out catches `(r and g) or b`, G out catches `r or (g and b)`.
- (b) 97, 98 and 99 of 100 pixels the colour (band 1), threshold 0.98: exit 1, 0, 0 today. The exact 98 case holds
  `>=` against `>`.
- (c) Paeth from hand-written bytes: a 2x2 RGB image whose second row is filter 4, raw bytes and expected pixels
  written as literals in the test. Pixel (1,1) is built on the two ties that matter: R has `pa == pc` < `pb`
  (a=10, b=25, c=20: the rule gives `a`, 10), G has `pb == pc` < `pa` (a=25, b=10, c=20: the rule gives `b`, 10).
  Passes today. Found while designing it: a tie `pa == pb` with `pa <= pc` exists only when a = b = c, so `pa <= pb`
  made `pa < pb` alone is an equivalent mutation that no input can detect.
- Refusals, each asserting exit 2 and its own message: cut in half (`truncated`), no IEND (`no IEND chunk`), no IDAT
  (`no IDAT chunk`), a stream that does not inflate (`does not inflate`), a wrong data length (`image data is`), an
  unknown filter byte (`filter type 7`). All pass today (the reviewer saw the first four exit 2; the other two go
  through `:63-64` and `:88-89`).
- `--band` validation, (0, 1] else exit 2: `--band 1.2` on a solid image of the colour exits **0** today (reads 5 rows
  outside the image's middle, as the reviewer saw), so that case FAILs before the fix; `--band 0` and `--band nan`
  exit 2 today only by way of step 1's fix (no pixels, and `round(nan)` raising), and exit 2 at the argument after it.
- N8: a fully transparent (alpha 0) RGBA image of the colour: exit 0, `PASS`, recorded as current behaviour.
- New case count: 37 + 5 (step 1) + 3 + 3 + 1 + 6 + 3 + 1 = **59**.
- The reviewer's three mutations against the new self-test: `and` to `or` at `:141` turns the G-out and R-out cases
  red (all three, since both `and`s change); `frac >= a.threshold` to `frac > 0.5` turns the 97-of-100 case red; the
  two `<=` at `:53` to `<` turns the Paeth case red (R channel, 20 for 10, so pixel (1,1) reads 210 where 200 is
  written).

### Step 3, S3 (`app.yml` comments only)
No behaviour change. The `app` run on that push is green with the same gate and control lines.

### Step 4, S4 (`brouter.yml`)
`app/**` becomes `app/src/**`; the comment cites review 6 S4 and RECORD -210. Not waited for.

### CI
- Each push that touches `tools/png-region*.py` or `app.yml` starts `app.yml`. The `png-region` job prints
  `png-region-test: 59 cases, 0 failed` once step 2 is in, and `42 cases, 0 failed` after step 1 alone.
- `app-bundle` unchanged: control `FAIL: 98/2530188 ... within 24 of E0218A`, exit 1, `OK: the control`; gate
  `PASS: 2530188/2530188 ... within 8 of E0218A`.
- A finding about the brief, made before pushing: `brouter.yml` ignores only `docs/**`, `app/**`, `iosApp/**` and
  `app.yml` (`brouter.yml:11-15`), so the pushes of steps 1 and 2 (`tools/`) each start a full engine run as well,
  not only step 4. Three engine runs in all, none waited for.

## 2. Results

### Step 1, S1: a broken PNG exits 2
- Red first, as predicted [run locally, Python 3 in this container]: the five new cases against the unfixed script
  printed `png-region-test: 42 cases, 5 failed`. The cut CRC and the 12-byte header each exited 1 with
  `struct.error: unpack requires a buffer of 4 bytes` and `... of 13 bytes`; their tracebacks name `line 40, in
  read_chunks` and `line 104, in read_png` of the unfixed copy. Width 0, height 0 and the empty band each exited 1 with
  `FAIL: 0/0 pixels`. Each failure is the one its case was built for; none belongs to another case.
- The fix (`tools/png-region.py`, `main` and the new `check`): `main` returns 2 on `Unsupported` and `OSError` as before,
  and now on any other exception, printing its type (`UNSUPPORTED or unreadable FILE: error: unpack requires ...`). A
  band with no pixels raises `Unsupported("the band holds no pixels (width W, height H, band B)")`, so it exits 2 with the
  `UNSUPPORTED` line. The module docstring's exit-status paragraph says so.
- Green: `png-region-test: 42 cases, 0 failed`. The 37 earlier cases unchanged.
- Decided: the catch-all covers the whole measurement, not only the read (the brief says "from reading"; the review says
  "catch every exception in `main`"). Anything that raises after the read is equally a file not measured, and A2 will add
  code after the read. Exit 1 now comes only from the `FAIL` verdict line.

### Step 2, S2: the self-test holds the tolerance, the threshold and Paeth; the refusals; `--band`; alpha
Fractions in the quoted lines are written `(...)` for the E1 scan; nothing else is changed in them.
- New cases (`tools/png-region-test.py`, sections 5 to 8): three with one channel out by 9 at tolerance 8; 97, 98 and 99
  of 100 pixels at threshold 0.98; Paeth from a hand-written 2x2 image whose raw bytes and expected pixels are literals
  in the test (neither `png_region.paeth` nor the test's encoder is used), built on a `pa == pc` tie (R) and a
  `pb == pc` tie (G); six refusals, each asserting exit 2 and its own message; `--band` 1.2, 0 and nan; and the
  transparent image.
- Red first [local]: against the step-1 script, `59 cases, 3 failed`, the three `--band` cases and nothing else. `--band
  1.2` exited 0 with `PASS: 200/200 pixels`, as the review saw on a real screenshot. `--band 0` and `--band nan` already
  exited 2 by way of step 1 (`the band holds no pixels (... band 0.0)`, `ValueError: cannot convert float NaN to
  integer`) and failed only on the missing `band must be` message, which is the refusal at the argument this step adds.
  The other 13 new cases passed on the unfixed script: they hold behaviour that was already right, which is what S2
  asked for, and the mutations below are what show they can fail.
- The fix: `parse_band` in `tools/png-region.py`, an argparse type that refuses anything not in (0, 1] (nan included),
  so argparse exits 2 with `argument --band: band must be in (0, 1], got '1.2'`.
- Green [local]: `png-region-test: 59 cases, 0 failed` (was 37 at `8bce937`, 42 after step 1).

#### The reviewer's three mutations, run against the new self-test
Each: the file copied to the scratchpad, one `sed` edit, the edit shown by `diff`, the self-test run, the file restored
from the copy (not git) and `cmp` equal to the copy.
- `:142` (`:141` at `df49ee5`), every ` and ` made ` or `: `test exit 1`, `59 cases, 3 failed`:
  `FAIL only R out by 9 fails at tolerance 8: exit 0: png-region: PASS: 1600/1600 pixels (...) within 8 of E0218A`, and
  the same for G and B.
- `:195` (`:176` at `df49ee5`), `frac >= a.threshold` made `frac > 0.5`: `59 cases, 1 failed`:
  `FAIL 97 of 100 the colour exits 1 at threshold 0.98: exit 0: png-region: PASS: 970/1000 pixels (...) within 0 of
  E0218A`.
- `:54` (`:53` at `df49ee5`), both `<=` made `<`: `59 cases, 1 failed`:
  `FAIL Paeth against hand-written bytes, both ties: 14143c190a32 | 0a1964d2c9ca`. Byte `d2` is 210 where 200 is
  written: the R channel took `c` (20) for `a` (10), as predicted.

#### Narrower mutations, run the same way (beyond the brief)
- One `and` alone made `or`, either one: 3 failed each. My prediction that each case catches only one of them was
  wrong: Python reads `r or g and b` as `r or (g and b)`, so with any single channel out one side of the `or` holds, and
  all three cases go red under either mutation.
- `:56`, `pb <= pc` made `pb < pc`: 1 failed, `0a1964c8d3ca` (G reads 211 for 201: `c` taken for `b`).
- `frac >= a.threshold` made `frac > a.threshold`: 1 failed, `FAIL 98 of 100 the colour exits 0 at threshold 0.98: exit
  1`.
- `pa <= pb` made `pa < pb` alone (the other comparison kept): **0 failed, survives**, as predicted in section 1: a tie
  `pa == pb` with `pa <= pc` needs a = b = c, where every branch returns the same value. It is an equivalent mutation,
  not a gap a case could close.

#### Alpha (review 6 N8): recorded, not decided
`alpha 0 everywhere: today it passes (alpha ignored)` asserts exit 0 and `PASS: 16/16` on a 4x4 RGBA image of the
colour with alpha 0 in every pixel. Alpha is ignored by design (`png-region.py:8`). The case pins today's behaviour so
that a change to it is seen; whether a transparent pixel should count is for the planner to rule. The four real
screenshots are fully opaque per the review [not re-checked here].

### Step 3, S3: the Xcode reason and the `--info` comment (`.github/workflows/app.yml`, comments only)
- **The brief's sentence, and the review's, is contradicted by the record, so I did not write it as given.** Both say
  that from 06:31 UTC on, `xcodebuild` under 26.0.1 offered no iOS Simulator destination in every sample. Read by me
  from CI [R]: job 134617571020 (run 44249852981, attempt 1) started 06:31:31 and failed with `Unable to find a
  destination` (its log 223); job 134628156762 (the same run, attempt 2) started 06:40:28, selected
  `/Applications/Xcode_26.0.1.app` (log 162), printed `Xcode 26.0.1` (171), built with `-destination "generic/platform=iOS
  Simulator"` (215) and printed `** BUILD SUCCEEDED **` at 06:46:45 (1207). The findings' own table says the same
  (`a1-3-launch.md:383-384`, `:610-611`: "red 06:31; green 06:40; red 07:02, 07:07"), and the review itself counts the
  06:40 build among the four greens ("between 05:38 and 06:45"). What holds: no destination at 06:31 and in every sample
  from 07:02 on (jobs 134636660132 at 07:01:37 and 134639110582 at 07:07:32, then run 44257759986).
- The brief's instruction was "Reword to what the evidence supports", so the comment at `app.yml:6-10` now says: no
  destination in the app build at 06:31 and in every sample from 07:02 on (two app builds, then all six machines of run
  44257759986); destinations in four app builds that day (05:38, 05:43, 05:59, 06:40, the last after the first red); why
  not known; `simctl` still runs simulators under 26.0.1 (`ios-smoke`); 26.2 offered destinations on six of six
  machines. Read for it [R]: run 44257759986's six `destinations` jobs, every `SUMMARY` line for 26.0.1 (passes 1 and 8)
  `ios_sim_available=0` with `error:iOS 26.0 is not installed`, and for 26.2 (pass 3) `ios_sim_available=34`; `ios-smoke`
  job 134643447456 of `brouter` run 44257759907 (`bccce50`, success): `Xcode 26.0.1` (log 226), `TOTAL: tests 61,
  failures 0` (547), `OK on the iOS simulator: all 25 classes ran` (2213), at 07:33.
- `app.yml:85-86` (now `:88-90`): the comment no longer says `--info` prints the link's arguments. It says the flag was
  added expecting that, that it does not (`a1-1-app-module.md` section 6, lines 216-219), and that the recorded step
  therefore finds nothing on the stdlib; `--info` stays for the dependency fetches it lists (review 6 N6 read 114 of them
  in job 134660260591). The step itself is unchanged.
- **Left stale, on the brief's instruction:** `.github/workflows/app-diagnose.yml:41`, "The runner's default is bash -e
  -o pipefail". The review (N3) reads the default as `bash -e` without `pipefail`; I did not re-check that. Not edited,
  because `app-diagnose.yml` is not in `brouter.yml`'s ignore list and has its own push trigger, so an edit starts six
  macOS diagnostic machines and a full engine run. The comment sits in a printed, never-failing step after `set +e` and
  `set +o pipefail`, so it misleads a reader, not a check.

### Step 4, S4: `brouter.yml` ignores `app/src/**`, not `app/**`
- `.github/workflows/brouter.yml:15` (was `:13`): `'app/**'` is now `'app/src/**'`. The comment above it (`:8-12`) cites
  RECORD -210 and review 6 S4 and adds `app/` outside `src/` to the list of what runs the engine. Nothing else in the
  file changed (`git diff` of the commit: the comment and that one line). The parsed `paths-ignore` is `docs/**`,
  `app/src/**`, `iosApp/**`, `.github/workflows/app.yml` [run: `yaml.safe_load`].
- Tracked files under `app/` outside `src/` at this commit: `app/build.gradle.kts` only (`git ls-files app`), so this
  change now runs the engine on that one file and on any build file added under `app/` later (review 6 S4 items 1 to 3).
- RECORD -210 is cited because the brief says to; RECORD.md on `origin/main` has no entry past -199 that I could find,
  so -205 to -210 are not checked by me.
- This push starts one full `brouter` run. Not waited for; the planner reads it.

### Step 5, S6 and S3's count: `a1-3-launch.md` section 20
Appended, no earlier line edited (`git diff` of the commit: additions only). (a) four greens under 26.0.1, the fourth
being job 134605728656 of run 44246226134 (its log 215, 204, 1207, read by me); (b) the eight "not shown" items, each
with file and line at `47e25f2`; (c) the control sentence true but not established, with run 44262884089's log 2184
(`000000`, about 65 % of the band, read by me) and the review's viewing of `before.png` (not viewed by me); (d) the five
cancelled engine runs, attributed to the planner and RECORD -205, -206, -208 as the brief states, not read by me.
