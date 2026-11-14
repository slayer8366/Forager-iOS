# Review 4: session 73's capture tooling for the five cosine-row tiles

Samphire Witness, independent reviewer. Code under review: `brouter-port-5b` at
`37293d1a56f04ef4ca803181062855b153d0ad53`, commits `s73:` on `2bc6e2c..37293d1` (8 commits, 6 files added, 747 lines;
`git diff --stat`, run). Read-only on that code. Marks: [read], [run], [inferred]. No coordinates and no node ids are
written here.

Setup checked [run]: this branch's remote head was `50d44fa`; the worktree was clean and is now detached at `37293d1`.

## 1. Predictions, written after reading the six files and before any run

- P1. Nothing printed by `CosTileCheck` on a passing run holds an id or a coordinate. [from reading `:66-173`]
- P2. The digit mask can be bypassed: `main` catches `Exception` only (`CosTileCheck.java:59`), so an `Error` (out of
  memory, stack overflow, an assertion) leaves by the JVM's own stack trace, unmasked. Whether any such message can
  carry a position: I expect no, since the JVM's own messages for those hold no data. Also the mask leaves hex letters;
  I expect BRouter prints no hex id.
- P3. The oracle itself prints nothing with `quite = true` and writes no track file (`outfileBase` is null). To be
  tested by reading its source and by searching everything a run wrote.
- P4. The ends are recoverable by anyone without the digest: the tile is a public release asset, the rule is public
  code, and the attempt number is published. So the digest adds no leak beyond what the fixed rule already gives; with
  it, at most 16 candidates per row confirm in microseconds. I expect to reproduce a published digest from the tile
  alone in scratch.
- P5. The row count overstates "road nodes": it counts every stored node, of any way kind in the tile, whether or not
  the hiking profile can use it. I expect a count of nodes the profile can match to be lower, by an amount I cannot
  guess.
- P6. Same bytes give the same ends: I expect equal digests over two runs, and equal under a different locale and a
  different default hash seed (none is used: `LinkedHashMap`, a sorted list).
- P7. A route between two in-band ends reads the row in the oracle's distance: I expect a counter on the oracle's
  cosine lookup to show the row's index read during the route, for every row tried.
- P8. Refusals: I expect each of the coder's seven to reproduce. I expect to make the lookup-version check fail by
  editing a scratch `lookups.dat`, and the nothing-decoded check to be unreachable in practice (a tile that opens and
  passes integrity but decodes nothing would then also hold no row nodes, so the row floor fires anyway). 
- P9. A tile that changes between download and check: not possible to pass wrongly, since one file is downloaded,
  checked, hard-linked and hashed. A partial download: curl `--fail` without a length check may accept a short body if
  the server closes cleanly; the integrity check should then refuse it. I expect a truncated tile to be refused at
  every cut point I try, with one doubt: a cut exactly at a boundary the integrity check accepts.
- P10. `fetch-tiles.sh` reads the dry-run pin file from a `file://` folder and passes; with one byte of a tile
  changed it refuses.
- P11. Workflow: a failed publish step after the draft is created leaves a draft; a same-day rerun then refuses at the
  tag step (the release list sees drafts with a write token). A rerun on a later day would capture afresh under a new
  tag and leave the old draft behind. [inferred, cannot run]
- P12. The uploaded artifact holds `headers/`, which the release does not; I expect no position in it but it is
  outside what the coder's ruling text names. Injection through `out/tag`: the tag is written to `GITHUB_ENV` unquoted
  of newlines; `cat` of a multi-line file could set a second variable. The artifact comes from the same run's capture
  job, so this needs the capture job compromised. Expect: note only.
- P13. The old path is byte-identical to `2bc6e2c` (the diff stat already shows six added files and no changed one).
- P14. One thing I expect to find wrong: `TOOLS_SHA` is `7e5c449`, and the checks of the tools at the head must be
  equal to that commit's; I expect them equal.

## 2. Verdict

**Ready with one should-fix first.** No blocking finding. The check does what the ruling's text says, nothing it wrote
in any run of mine holds a position, every refusal I tried refused, and the old path is byte-identical. The should-fix
is one line in the workflow and is inherited from `publish-tiles.yml`; the owner may reasonably take it as it is.

## 3. Findings

### Should-fix

- **S1. The tag check lets a second line through into the publish job's environment.** [read, run in scratch]
  `publish-cos-tiles.yml:66-68` reads `out/tag` from the artifact, tests it with `case ... brouter-cos-tiles-*`, and
  writes `TAG=$tag` to `GITHUB_ENV`. The glob matches across a newline: a tag file of two lines passed the same three
  lines in scratch and wrote a second variable (control: the old prefix was refused). The artifact is the only data
  that crosses from the `contents: read` job to the `contents: write` job, and a variable such as `BASH_ENV` set there
  would run a file from the artifact in the later steps. It needs the capture job to be subverted first (its code is
  pinned by SHA, its actions by SHA, and the tag is the script's own `date`, `capture-cos.sh:109`), so the chance is
  small. The coder's line was added "because the tag comes from an artifact file" (s73 report, section 7) and does not
  close that. A pattern that admits only the prefix and a date would. `publish-tiles.yml:75-81` on `main` has the same
  shape. This is the one change that is cheap now and needs another word from the owner once the file is on `main`.

### Notes

- **N1. The ends are not printed, and they are not secret.** [run] From the published tile and the public rule alone,
  with a scratch class that walks the tile in the opposite order to `CosTileCheck`, I recomputed all five digests
  equal to the ones in the pin file's `#` lines, in seconds. No brute force is needed: the rule is public, the tile is
  a public asset, and the attempt number is published, so the ends of every row follow. If the rule were unknown, a
  search over pairs obeying the end rule is at most the product of the rows' node counts (under 10^8 hashes for the
  largest tile), also seconds. So the 16 hex digits hide nothing and add nothing beyond what "a fixed rule" already
  gives away. The ruling's own words are met ("The ends are no longer printed to the public Actions log"). The ends
  are nodes of public map data picked by arithmetic, not anyone's place. Worth saying to the owner in those words so
  that "not printed" is not read as "cannot be known".
- **N2. "Road nodes": the count is of every node the tile stores in the band.** [read `CosTileCheck.java:114-118`;
  run] It does not ask whether Forager's profile can use the node. In a scratch sample of 64 evenly spaced starts per
  row, the oracle refused to match the start in 0 to 10 of 64 and the end in 0 to 13 of 64 (worst row 474: 41 of 64
  routed; row 502, the smallest: 49 of 64). So as a count of nodes the hiking profile can route from, it overstates
  by up to roughly a third in the worst row sampled; the smallest row would still stand near 860 against the floor of
  500. As a count of nodes on stored ways it is exact. The log, the pin file and the notes all say "nodes", not "road
  nodes", which is accurate. Which way kinds the tile stores I did not determine.
- **N3. The last byte of a tile is not checked by the oracle.** [run] One tile cut by exactly 1 byte passed the whole
  check; so did the same tile with its last byte changed and with 8 bytes appended. Cuts of 2, 3, 4, 1,000, 100,000
  bytes, a half, nine tenths and all but 8 bytes were refused, and 27 of 28 single-byte changes were refused (the one
  that passed was the last byte). `capture-cos.sh` keeps the response headers but does not hold the size to
  `Content-Length`. curl itself refuses a body shorter than its `Content-Length` [inferred, curl's documented
  behaviour], so a short download is unlikely to get this far. Whatever is published is pinned as it is.
- **N4. The digit mask.** [read, run] Every error string goes through it (`CosTileCheck.java:61, 83, 87, 88, 156`).
  It leaves signs, dots and digit counts, and hex letters; I found no oracle message that prints hex (read of the
  1.7.10 source vendored in Forager, every `throw new` under `router`, `mapaccess`, `codec`, `util`,
  `expressions`). The ways round it: an `Error` or an exception outside the `try` (`:54`, `:64`) leaves by the JVM's
  own stack trace. Those can carry the rows argument or a path, not a position. The engine catches `Error` itself and
  logs only when its info log is on, which it is not (`outfileBase` null, `quite` true, no `debug.txt` beside the
  profiles) [read]. I could not make an out-of-memory error happen (heaps of 8, 24 and 48 MB all passed), so that
  path is read, not seen.
- **N5. Nothing fixes the list to the 8 rows.** [run] A list with a row removed passes, as designed: the list is the
  specification. It is pinned through `TOOLS_SHA`, and I checked the committed list against the 8 rows of the brief
  and each row's band against its tile's name [read].
- **N6. The "nothing decoded" check cannot fail on its own.** [read] `:124` is reached only after the file opens;
  if nothing decodes, every row holds 0 nodes and `:130` fails too. I did not build a tile that would show it.
  The lookup-version check I did see fail, both ways (see section 4).
- **N7. A failed publish leaves a draft.** [inferred, not run] After `gh release create --draft` succeeds, a failure
  in the readback or the publish step leaves the draft and its assets, visible to collaborators only. A rerun the same
  day then refuses at the tag step, since the release list includes drafts for a token with write access. A run on a
  later day takes a new tag and leaves the old draft. Either way the owner deletes the draft by hand. Fails closed.
- **N8. A dispatch from another branch that holds the file runs the capture job.** [inferred] `brouter-port-5b` holds
  the file, so once the workflow exists on `main` it can be dispatched from that branch: capture runs with read
  access, downloads from brouter.de and uploads the artifact; publish is skipped by the guard. Same as the old file.
- **N9. The artifact holds more than the release.** [run] `out/` holds `headers/` (brouter.de's response headers for
  each tile), `release-notes.md` and `tag` as well as the six assets. In my run the header files held the status
  line, server, date, type, length, Last-Modified, ETag and nothing else. `Last-Modified` and `ETag` go from the
  server into the public pin file and release notes without any filter; brouter.de is already trusted for the tiles.
- **N10. In CI the project's user agent goes to brouter.de** (`capture-cos.sh:27`), as for the first release. [read]
- **N11. The pin path is `segments/<tile>.rd5`,** the same folder name as the old pins. [run] It keeps the new tiles
  out of the shared folder only if the later fetch gives a directory of its own, as session 72 planned. The published
  pin file cannot be changed afterwards without breaking its byte-for-byte copy, so this is the moment to be sure.

## 4. What I ran, and how each was seen able to fail

All in scratch, on the container's JDK (not the pinned Temurin), worktree clean before and after [run].

| Question | What | Result |
|---|---|---|
| 1 | The whole of `capture-cos.sh` against brouter.de (a plain user agent, the local oracle zip), all five tiles | exit 0, five PASS, counts and digests equal to the coder's; same bytes as the coder's (same sizes and SHA-256) |
| 1 | Search of the log and every file under `out/` (not the tiles) for the chosen ends: ids, both integers of each, hex forms, degrees at 3 to 6 decimals; 223 patterns | 0 hits. Control: the scratch file holding the ends gave 48 |
| 1 | The same for all 16 candidate ends of every row (128 pairs, 3,492 patterns) over 65 files: every passing, refusing and no-route log and output | 0 hits. Control: 762 |
| 1 | No-route runs through the real class with a scratch profile, two tiles | 48 attempts, only "not mapped" and one masked "not found"; exit 1 |
| 1 | The six committed files for numbers of 4 or more decimals | 0 each. Control line: 1 |
| 2 | Row counts | all 8 equal to session 72's and the coder's |
| 2 | Same bytes, same ends: three runs; Arabic, Turkish and Hindi locales; another time zone; libm intrinsics on; my own reversed walk | the same digest every time. Seen failing: with the file encoding forced to UTF-16 the oracle cannot read its lookup table and both rows fail closed |
| 2 | Does the route read the row: the oracle's scale table changed in memory, the row's own entry times 1.5 | the distance moved for all 8 rows (for example 1,828 m to 2,363 m) |
| 2 | The control: the two neighbouring rows changed, then every row but the row's own | the distance did not move for any of the 8; every track node lies in its row. So the route's distance reads that row and no other |
| 3 | Lookup version: `lookups.dat` set to 10; then the tile's own version byte changed | both refused, by the version line and the version-checked open (and by the top index checksum for the changed tile) |
| 3 | Truncations and byte changes | N3 |
| 3 | Lists: four tiles; a wrong row; a tile twice; no final newline; a tile with no rows; a non-empty out folder | all exit non-zero with `assets/` gone. CRLF line ends pass and give the same pins |
| 3 | `fetch-tiles.sh` on the dry-run pin file from a `file://` folder | "5 tiles ready", the `#` lines skipped. A changed byte: refused by SHA-256. A byte short: refused by size |
| 3 | A tile that changes between download and check | [read] one file is downloaded, checked, hard-linked and hashed (`:67-84`); no second fetch |
| 4 | S1 in scratch | above |
| 5 | `git diff 2bc6e2c 37293d1` | six files added, none changed; `TileCheck.java`, `capture.sh`, `tiles.tsv`, `publish-tiles.yml`, the three fetch scripts and `tile-pins.tsv` identical. `git diff 7e5c449 37293d1 -- tools` empty, so `TOOLS_SHA` holds the tools I ran |

The workflow, by reading [read unless marked]: `permissions: {}` at the top, `contents: read` on capture, `contents:
write` on publish only; no `${{ }}` of outside data in any `run:`; the four action pins are character for character
those of `origin/main`'s `publish-tiles.yml`; the diff against that file is the one the coder's section 7 lists and no
more; it creates, views, downloads and edits only `$TAG`, which must start `brouter-cos-tiles-`, so it cannot name the
old release (`brouter-tiles-...`), and it has no delete and no overwrite; the tile list at `TOOLS_SHA` names none of
the four old tiles. Checking out `7e5c449` by SHA from a run on `main` [inferred]: the checkout action fetches a full
SHA directly and GitHub serves any commit reachable from a branch of the same repository; `7e5c449` is on
`origin/brouter-port-5b` [run]. It would fail closed if that branch were deleted or rewritten.

Record -185's claims that I could test held: eight commits, the trailer on each, six files added and none changed, the
line counts, the dry run's counts and attempts, the digests.

## 5. Predictions scored

- Held: P1, P3, P4, P6, P7, P10, P13, P14. P8 held (version check made to fail; nothing-decoded redundant, by
  reading). P12 held and became S1. P11 is inferred only.
- P2: half. The bypass exists as predicted; I did not see it, and found no message it could expose.
- P5: held in direction; the size (up to about a third in one row, by a sample of 64) I had not guessed.
- P9: my doubt was right and my main expectation wrong: a truncation by exactly one byte passes (N3). I predicted
  refusal "at every cut point I try".
- Not predicted: that appended bytes and a changed last byte pass; the UTF-16 failure.

## 6. Not checked

- The real run: Temurin 21.0.13+9, the bytes of that day, the Actions log, the release steps, the draft's behaviour
  on failure, the checkout by SHA. All workflow behaviour is read or inferred; nothing ran on GitHub.
- `actionlint` and `shellcheck`: not installed, not run. `bash -n` and a YAML parse only.
- Which kinds of way a tile stores, so what "road" covers (N2).
- An `Error` leaving the check (N4), and the nothing-decoded check firing (N6).
- What a GitHub runner's response headers hold (mine came through a local proxy).
- Whether `gh release create` removes its own draft when an upload fails.
- The oracle's source was read from Forager's vendored copy of v1.7.10, not from the jar's own bytes, apart from the
  field and method names I took with `javap`.
- The floor, the 16 attempts and the 0.01 degree step as choices; the engine and tests (the item changes none).
