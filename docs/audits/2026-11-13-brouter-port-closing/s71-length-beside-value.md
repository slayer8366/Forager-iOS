# s71: the plain text's length beside the by-value digest (Plumb Ledger, session 71)

Base `23e7c8c0e1ddddd0f6ec6c564e6612035a2ce224` on `brouter-port-5b`, checked against `git ls-remote` and a clean tree
before any work. Item: the reviewer's should-fix 60-1 (review branch `review/changed-comparisons`,
`docs/reviews/2026-11-14-test-changes-since-163.md:279`, plant P60 at `:91` and `:274`).

## The owner's rulings, verbatim

- Record -163: "Leave the port's printing as it is and record this as a known platform difference: on iOS a printed
  number in these exports can differ from Java's in its last digit while meaning the same value. The simulator test that
  is red now is changed to compare those numbers by value, and that change goes to the independent reviewer."
- Record -177, on finding 60-1, the owner chose "Also compare length", whose text was: "The reviewer's proposal: store
  and compare the text's length beside the value. A last-digit difference still passes; a longer or shorter number
  (trailing zero, exponent form) fails on the simulator. About one short coder session, then a look by the reviewer. If
  iOS does print a different length somewhere, the test goes red and comes back to you."

## Where the plain length went in session 60 (read from the code)

- `FormatDigestsCommonTest.kt:71-74` (at the base): for every output `k` the test takes
  `s = FormatNumberText.forDigest(k, texts[k], ...)` and compares `s.length` and the digest of `s` with the row. For GPX
  (`k == 1`) and JSON (`k == 2`), `s` is the canonical text (`FormatNumberText.kt:27-31`), so `s.length` is the canonical
  length, in which every number at the twelve places is 16 (or 8) hex digits whatever its printed length
  (`FormatNumberText.kt:94-99`). The plain text's length is not compared anywhere in this test.
- `FormatDigestsJvmTest.kt:43-49` (at the base): the same, against the port's production run on the JVM.
- The stored rows (`FormatDigests.kt:22-96` at the base) no longer hold the plain GPX and JSON lengths: session 60's
  change `28e2a79` regenerated them, replacing the second and third numbers of every row's lengths with the canonical
  lengths (for example `F0h-inside-across-the-degree-line`, `FormatDigests.kt:27`: 16218 and 13637 before, 18601 and
  16020 after). Hints, KML and CSV lengths and digests and every mode were unchanged by `28e2a79` (checked by script: 0
  of 69 rows differ outside GPX/JSON).
- The plain lengths survive in git: `FormatDigests.kt` at `f33e8a1` (session 58, the last version before `28e2a79`).
  64 of 69 rows have different GPX/JSON lengths there; the other 5 had none of the twelve places to change (no route, or
  no number at a by-value place), so their canonical length equals their plain length.

## Design: what is stored and compared

- `FormatOutputs.Row` gains `textLengths: List<Int>`: the plain text's length of GPX and JSON, in that order (two
  numbers). `FormatDigests.r(...)` takes them as a third string, for example `"16218 13637"`.
- Source of the values: the GPX and JSON lengths of `FormatDigests.kt` at `f33e8a1`, copied by script by row id. Those
  were made from the port's production run on the JVM, which `HintsFormatsLogicTest` holds equal to the oracle's text
  exactly, character for character, so on the JVM they are Java's lengths. They are not drawn from the oracle anew.
- `FormatDigestsJvmTest` holds each row's `textLengths` to the plain length of the port's production run on the JVM
  (`p.texts[1].length`, `p.texts[2].length`), beside the existing fields, so the values cannot drift. It does not run the
  oracle; the link to Java's length is `HintsFormatsLogicTest`'s exact text comparison, as before.
- `FormatDigestsCommonTest` (JVM and simulator): for GPX and JSON an output counts as equal only when the canonical
  length, the canonical digest AND the plain text's length all equal the row's. A difference in plain length is reported
  as "text length here A stored B", beside the existing by-value fields.
- Unchanged: the canonical form, the twelve places, the canonical length and digest, hints, KML and CSV, every other
  test. No port, engine or printing change. No test added.

So a last-digit difference at a tie (same value, same length) still passes; a longer or shorter text at the twelve
places (a trailing zero, an exponent form, a widened `Float`) now fails, on the JVM and on the simulator.

## Predictions

- Totals unchanged, no test added: `jvm` 251 tests, simulator (`ios-smoke`) 60, both 0 failures.
- Plant P60 (`FormatGpx.kt:281`: `"<ele>" + n.getElev() + "</ele>"` to `... + "0</ele>"`), filter `*FormatDigests*`:
  - On the base: both digest tests PASS (the reviewer's result, to be seen here first).
  - After the change: `FormatDigestsCommonTest` FAILS (on the JVM only F0's 2 rows run): "hints and formats against the
    stored digests (2 differences; first: F0 F0-inside-across-the-degree-line mode 2 gpx: text length here A stored
    12xxx ...)" with A longer than stored and the by-value fields equal. `FormatDigestsJvmTest` FAILS on about 66 rows
    (every row with a route that has elevations; fewer if some route has none), named "stored row against the port's
    production run".
- Second control (a last-digit difference of the same value still passes): no new plant. `FormatNumberTextTest.kt:47`
  and `:79` already show two tie texts of one value (the same 18 characters long) giving one canonical text; the new
  check compares only `length`, which two equal-length texts share by construction. Cited, not added.
- Simulator: I predict green. Session 59 reported the five texts that differ from Java's as the same length ("neither
  longer nor shorter"), and session 60's run 43364528773 was green on the canonical comparison; the plain length is
  the only new field. The risk is a place session 59 did not measure; if the simulator goes red on a length, that is a
  real result for the owner and is left red and reported.

## Results

(appended below as they come)

**Plant P60 on the base (`0cadfe3`, test code as session 60 left it), filter `*FormatDigests*`, JVM:** both digest tests
PASSED, as the reviewer saw. Evidence: `compileKotlinJvm` ran, 0 lines with ` e: ` (and 0 with `e: ` at line start), 2
fresh JUnit XML files, "TOTAL: tests 2, failures 0"; `FormatDigestsCommonTest` F0 gpx equal 2 of 2, `FormatDigestsJvmTest`
gpx equal on every group; `FormatGpx.kt` restored `cmp`-equal, `git status` clean after.

**The change, `c401e25`.** `FormatOutputs.Row.textLengths` (`FormatOutputs.kt:58-63`), the 69 rows' third string
(`FormatDigests.kt:28-102`, copied by id from `f33e8a1`), the comparison in `FormatDigestsCommonTest.kt:77-87` and the
hold in `FormatDigestsJvmTest.kt:48-59`. Local JVM run of `*FormatDigests*` and `*FormatNumberText*`: all pass, every
group equal on every output (F0 2, F0h 8, NT 59), so the copied plain lengths equal the port's production run today on
all 69 rows. 0 ` e: `, `compileTestKotlinJvm` ran.

**Plant P60 after the change, filter `*FormatDigests*`, JVM:** 0 ` e: ` (and 0 `e: ` at line start), `compileKotlinJvm`
ran, 2 fresh XML, "TOTAL: tests 2, failures 1"; `FormatGpx.kt` restored `cmp`-equal; `git diff HEAD` empty after (the
forward change is committed at `c401e25` and still in the tree).
- `FormatDigestsJvmTest` FAILED: "stored digests against the port's production run (64 differences; first: F0h
  F0h-inside-across-the-degree-line: stored row against the port's production run (gpx/json text length here
  16444/13637 stored 16218/13637))". The 64 rows it printed (8 F0h, 56 NT) all have mode, canonical lengths and digests
  equal to the stored row and JSON's plain length equal; only GPX's plain length is longer, on all 64. 64 is the number
  of rows whose GPX/JSON lengths session 60 changed, that is the rows with a number at the twelve places; the other 5
  (3 NT with no route, F0's 2) have none. Specific to this edit: only an added character makes the GPX text longer with
  the value unchanged, and JSON (not touched by the plant) is equal.
- `FormatDigestsCommonTest` PASSED, against my prediction. Cause, read from the stored rows: on the JVM it runs F0's 2
  rows only (reviewer note 60-5), and F0's plain and canonical lengths are equal (`FormatDigests.kt:28-29`: "12491
  11957" both ways), so F0's GPX and JSON have no number at any of the twelve places and nothing by-value can be planted
  in what that test sees on the JVM. The test's new length check is therefore not exercised by a JVM run in CI; on the
  simulator it runs F0h and NT. Wrong premise in my design: the predicted common-test message. A local run with the case
  files follows.

**`FormatDigestsCommonTest` on the JVM with F0h's and NT's case files (local only, beyond the brief's filter, to reach
the path the simulator runs).** `NtCasesWriteTest` wrote the run-time files under `brouter/build/step10/` (never
committed), then `BROUTER_STEP10_NT_CASES` set for the runs below.
- Forward, `--rerun`: PASSED; F0 2 of 2, F0h 8 of 8, NT 59 of 59 rows run, every output equal on every row. 0 ` e: `.
- Plant P60 (revert runner): FAILED, "hints and formats against the stored digests (64 differences; first: F0h
  F0h-inside-across-the-degree-line mode 2 gpx text length here 16444 stored 16218; gpx: here 18601/6d2258ac8973934a
  stored 18601/6d2258ac8973934a)". The by-value length and digest are equal; only the text length differs. Problems
  printed (first 40): 8 F0h and 32 NT, every one "gpx text length"; per group gpx equal F0h 0 of 8, NT 3 of 59 (the 3
  with no route), JSON, hints, KML, CSV equal on all. 0 ` e: `, `compileKotlinJvm` ran, 1 fresh XML, "tests 1,
  failures 1", restored `cmp`-equal, `git diff HEAD` empty after (forward change committed in `c401e25`).

**Second control (a last-digit difference of one value still passes).** Not planted in the port: none of the twelve
places can be made to print another text of the same value and length by a one-line edit without inventing the tie
itself. Cited instead: `FormatNumberTextTest.kt:47` (GPX) and `:79` (JSON) show two tie texts of one value, both 18
characters, giving one canonical text; the new check compares `texts[k].length` only (`FormatDigestsCommonTest.kt:78-79`),
which two texts of equal length share. That the new check is not stricter than length on the real outputs is shown by the
forward runs: every one of the 69 rows equal on the JVM, through both tests. Whether the simulator's five differing
texts are the same length as Java's is what CI answers below.

**CI, run 43558149498 on `c401e25` (the test commit).**
- `ios-smoke` (simulator-checked): success. "TOTAL: tests 60, failures 0, errors 0, skipped 0 in 29 files"; "OK on the
  iOS simulator: all 24 classes ran"; 0 ` e: `. `FormatDigestsCommonTest` on the simulator: F0 2 of 2, F0h 8 of 8, NT 59
  of 59 rows run, gpx and json equal on every row, so on all 69 rows the simulator's GPX and JSON texts have Java's
  length as well as Java's values at the twelve places. Simulator green, as predicted. `FormatNumberTextTest` 5 of 5.
