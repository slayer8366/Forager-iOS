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

Results follow in later commits.
