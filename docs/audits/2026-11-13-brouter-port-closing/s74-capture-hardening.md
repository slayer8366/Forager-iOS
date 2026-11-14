# Session 74: hardening the cosine-row capture tooling (review S1 and the tile's size)

Coder Plumb Ledger. Branch `brouter-port-5b`, base `37293d1a56f04ef4ca803181062855b153d0ad53` (checked with
`git ls-remote origin brouter-port-5b`, tree clean). The review is `docs/reviews/2026-11-14-session-73-capture.md` on
`origin/review/changed-comparisons`: its finding S1 and its note N3. Marks: [C] confirmed by a run or a read named here,
[I] inferred, [U] unverified. No coordinate and no node id is written here.

## 1. Design and predictions (written before any run)

### 1.1 What is there now [C, read]

- `publish-cos-tiles.yml:66-68`: `tag=$(cat out/tag)`, then `case "$tag" in brouter-cos-tiles-*)`, then
  `echo "TAG=$tag" >> "$GITHUB_ENV"`. A `case` glob `*` matches a newline, and `$(cat ...)` keeps inner newlines and
  trailing spaces and drops only trailing newlines. `:67` also echoes the file's content on refusal.
- `capture-cos.sh:61` sets `date=$(date -u +%F)`; `:109` writes `echo "brouter-cos-tiles-$date" > "$out/tag"`. Nothing
  checks `date`'s output.
- `capture-cos.sh:71-74` downloads with `curl --fail --location --dump-header`; `:76-79` runs CosTileCheck; `:80` links
  the file into `assets/`; `:83-84` pins `wc -c` and the SHA-256 of whatever was downloaded. No size is compared with
  the server's. The headers file holds every response curl saw: with a proxy, a `HTTP/1.1 200 Connection Established`
  block comes first (session 73's `run2/headers/W20_N15.txt`, read), and with `--location` a redirect's block would too.

### 1.2 The tag, in the workflow

Replace `:66-68` with, in order:

1. `export LC_ALL=C`, so no locale changes what a bracket class matches.
2. Read the file whole with its trailing newlines kept: `raw=$(cat out/tag; printf x)`, then `raw=${raw%x}`.
3. Match `raw` against `^brouter-cos-tiles-(D{4}-D{2}-D{2})\n$` with bash `=~`, where `D` is the explicit class
   `[0123456789]` and `\n` is a real newline. Bash's `=~` does not set `REG_NEWLINE`, so `^` and `$` are the ends of the
   whole string [I, POSIX `regcomp` without `REG_NEWLINE`; to be seen in the runs below].
4. Rebuild the tag from the matched digits only: `tag="brouter-cos-tiles-${BASH_REMATCH[1]}"`.
5. Byte check: `printf '%s\n' "$tag" | cmp -s - out/tag`. This closes what step 2 cannot see: bash drops NUL bytes in a
   command substitution, so a file with a NUL in it could match in step 3; `cmp` compares the file's own bytes.
6. Write `printf 'TAG=%s\n' "$tag" >> "$GITHUB_ENV"`. The value is built from 10 matched characters, all digits or
   `-`, so no newline can be in it whatever the file held.

On any refusal, one line: `REFUSED: out/tag is not exactly one line of the form brouter-cos-tiles-YYYY-MM-DD (<n>
bytes)`. The file's content is not echoed, so nothing from the artifact reaches the log, where a line starting `::`
would be read as a workflow command [I, GitHub's documented workflow commands].

The form is checked as a shape (four digits, two, two), not as a calendar date. A tag such as
`brouter-cos-tiles-2026-13-45` would pass; it is harmless as a tag name and the script cannot write it (1.3).

### 1.3 The tag, in the script

At `:61`, right after `date=$(date -u +%F)`, refuse unless `date` matches `^D{4}-D{2}-D{2}$` (same class), with
`REFUSED: date -u +%F gave an unexpected value`. At `:109`, write with `printf 'brouter-cos-tiles-%s\n' "$date"`. So the
script can only write the one form, one line, and the check comes before any download.

### 1.4 The size, in the script

Right after the download at `:71-74` and before CosTileCheck:

- Take the `Content-Length` lines of the last response only: the lines after the last line starting `HTTP/` in the
  headers file, carriage returns removed, name matched without regard to case.
- Refuse `REFUSED: <tile>: no Content-Length in the server's response headers` if there is none.
- Refuse `REFUSED: <tile>: Content-Length is not one whole number` if there is more than one, or it holds anything but
  digits.
- Refuse `REFUSED: <tile>: downloaded <n> bytes but Content-Length says <m>` if `wc -c` differs.
- On a match, print `<tile>.rd5: <n> bytes, equal to Content-Length`.

This needs nothing in `CosTileCheck.java`. As the brief says, it does not try to catch bytes changed or appended by the
server itself: a server that sends a wrong `Content-Length` with a matching body passes.

### 1.5 How each is made to fail

- The tag: the run block of the step is taken out of the YAML with a YAML parser (not copied by hand), cut before the
  `refs=` line, and run with `bash -e` against a scratch `out/tag` and a scratch `GITHUB_ENV`. Old: from
  `37293d1`'s file. New: from the new file. Cases: the good tag; two lines (the reviewer's demonstration, second line
  `BASH_ENV=...`); a wrong prefix; a wrong date shape; an empty file; trailing spaces; a trailing second line; plus,
  beyond the brief, a trailing blank line, no final newline, and a NUL before the newline.
- The size: a small local HTTP server in scratch serves the five tiles copied from session 73's `run2/assets/`. For one
  tile it sends `Transfer-Encoding: chunked` together with a `Content-Length` of the true size, and a body one byte
  short. With chunked encoding curl reads the body to the last chunk and does not hold it to `Content-Length` [I, to be
  seen; if curl refuses it, then that shows curl's own refusal, and I fall back to a `curl` wrapper first on `PATH` that
  calls the real curl and then cuts the file, run against the same server]. For the missing header it sends that tile
  chunked with no `Content-Length`. The whole script runs, with `COS_ORACLE_ZIP` set to the local oracle zip, so only
  the local server is fetched from.

### 1.6 Predictions

- P1. Old lines: the two-line file passes and the env file holds two lines, the second the injected variable.
- P2. Old lines: trailing spaces, a trailing second line, a wrong date shape, a trailing blank line and no final newline
  all pass; a wrong prefix and an empty file are refused.
- P3. New lines: every case but the good tag is refused with the one message; the NUL case is refused by `cmp` not by
  the match. The good tag gives an env file of exactly `TAG=brouter-cos-tiles-2026-11-14` and a newline (`cmp` against
  a file written by hand).
- P4. Old script, one tile served one byte short with a true `Content-Length`: curl exits 0, and since the reviewer
  found a one-byte cut passes CosTileCheck (N3), the whole run exits 0 and pins the short tile. If the cut tile is
  refused by CosTileCheck instead, the before-case still shows the script did not compare sizes, but N3 does not hold
  for that tile.
- P5. Old script, no `Content-Length`: the run exits 0.
- P6. New script: the short tile is refused by name before CosTileCheck runs; no header is refused by name; `assets/`
  is gone after both; the honest local run exits 0.
- P7. The dry run against brouter.de: five PASS. If brouter.de has not rebuilt since `Sat, 14 Nov 2026 01:03:01 GMT`,
  the five SHA-256 and the five ends digests equal session 73's (s73 §5.2, §5.3).
- P8. YAML parses, `bash -n` passes, every path the workflow runs exists at the new `TOOLS_SHA`.
