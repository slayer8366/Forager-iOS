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
