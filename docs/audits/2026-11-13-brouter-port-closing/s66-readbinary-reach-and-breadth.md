# s66: ReadBinaryTest, the error path asserted reached and -116's loosening narrowed (Plumb Ledger, session 66)

Review `docs/reviews/2026-11-13-changed-comparisons.md` (branch `review/changed-comparisons`), should-fix 2 and 3,
verbatim: "**-116, `ReadBinaryTest`: assert the error path is reached.** `errorPaths` is printed and not asserted (lines
157 and 158). Without the two empty tracks the test stays green and tests nothing of the ruling." and "**-116: the
loosening is wider than its evidence.** It was argued for the null-pointer text and applied to every catch, 1,549 of
4,522 debug texts by the coder's count. Either narrow it on the JVM or record its breadth."
Base `4705321` on `brouter-port-5b`. One file changes, `router/ReadBinaryTest.kt`. No port code changes, except the
Part B plant inside `revert.sh`, restored from a saved copy.

## What the error path holds (read, not run)
Java's catch is `OsmTrack.java:286-290`: `"Error reading rawTrack: " + e`, nothing appended after it, so the text after
the prefix (the "tail") is the exception's `toString()` and ends the debug text. The port's is
`OsmTrack.kt:556-561` (the append at `:560`), the same shape. On this test's inputs, `newEp`, `nogoChecksums` and `ep.waypoint` are never
null, so two exceptions reach the catch:
- **The null-pointer case.** `t.cost = last_pe.cost` (`OsmTrack.java:258`) when `n` is 0 and the target matched. Only
  the two empty tracks (the last two, indices 30 and 31 if the walks give 30 tracks, as file 21's run did) write
  `n = 0`. Java's tail on JDK 21 with the helpful message is expected to be
  `java.lang.NullPointerException: Cannot read field "cost" because "last_pe" is null` (the review's [I], from the
  lengths 145 and 93); the port's `lastPe!!` gives `java.lang.NullPointerException` with no message [I, Kotlin's
  `!!`].
- **The end-of-file cuts.** A cut that ends before the waypoint, the name or the last node throws from
  `DataInputStream` (Java) or okio (port), both `java.io.EOFException` with no message on the JVM [I, from the
  libraries; the run checks it]. A cut inside the checksums is caught inside (`:262-272`) and is not an error.

### File layout of an empty track (from `writeBinary`, `OsmTrack.java:217-230`, `MatchedWaypoint.java:35-48`)
43 bytes of waypoint, the name (`L` bytes), `n` (4), no nodes, 3 checksums (24), the dirty flag (1), the profile stamp
(8): `80 + L` bytes, `n` ending at `size - 33`. So every one of the fixed cuts `size-1, -8, -9, -16, -17, -24, -25`
keeps `n` and, the cut variants using target `0/0`, throws the null pointer; the fixed cuts `0, 1, 32, 40, 41, 43`
all end inside the waypoint or the name (or, for `L = 0` and cut 43, before `n`) and throw end-of-file, on every track.

## Part A (should-fix 2): the error path asserted reached, split
Two counters beside `errorPaths`, each counted where `errorPaths++` is now (an error-path debug text, both sides
having taken the path):
- `nullPointerPaths`: the oracle's tail starts with `java.lang.NullPointerException`. The prefix match, not the full
  JDK text, so that a JIT fast throw with no message (the cause -107 inferred) or another JDK wording still counts.
- `otherErrorPaths`: every other error-path text.

New assertions, placed **after** the existing `nonNull > 50 && reads > 1000`, which stays as it is:
- `nullPointerPaths >= 74`, message `"null-pointer case (the empty tracks) reached $nullPointerPaths times, floor 74"`.
- `otherErrorPaths >= 6 * tracks.size`, message `"other error-path texts (end-of-file cuts) reached $otherErrorPaths
  times, floor ${6 * tracks.size}"`.
- `errorPaths == nullPointerPaths + otherErrorPaths` is true by construction; not asserted.

**How the floors were chosen: from the setup's structure, not from a count seen.** A read with `withDebug` true at
`shift 0`, `sameProfile` true is never skipped by line 110, and every whole file is never skipped. Null pointer, per
empty track: the whole file at the three targets that match (`0/0`, `19/-19`, `-19/19`; the others are 20 or 21 out) x
5 shifts x 2 profiles = 30, plus the 7 fixed tail cuts once each = 37 at least; two empty tracks, 74. The 7 are
distinct from each other and from the 6 fixed head cuts for both names (`""`, 80 bytes; `"end"`, 83). End of file:
the 6 fixed head cuts once each on every track = `6 * tracks.size` at least. Random cuts and the 1-in-4 kept reads
only add. The floors therefore hold whatever the seeded cuts and walks give, and need no change if the tiles or the
walks move. One empty track dropped gives at most about 53 [I, from the expected 3.25 debug reads per cut], under 74,
so it fails too.

**Is the setup deterministic?** Seeded (`Random(20261124L)`, walk seeds `7501 + ci`); the classification depends on
the oracle's exception class only, not its message, so a JIT fast throw does not move it. I expect two runs to give
identical counts [I]; the unplanted run is made twice to see. Floors, not exact counts, even so: an exact count would
pin the walks and the tiles, which this assertion is not about.

**Prediction, unplanted:** pass. `errorPaths` 1,549 as in file 21 if nothing has moved since session 24 [I];
`nullPointerPaths` above 74 and well under 200, `otherErrorPaths` above 192 (32 tracks).

**The plant (test setup, through `revert.sh`, filter `*ReadBinaryTest`).** Line 69:
`.map { TrackEngines.tracks(it).first } + listOf(btools.router.OsmTrack(), btools.router.OsmTrack())` becomes
`.map { TrackEngines.tracks(it).first } + listOf<btools.router.OsmTrack>()`. The empty tracks drop out; the walks'
tracks come first, so the seeded draws for them are unchanged.
**Prediction under the plant:** compiles (0 ` e: `). Every comparison passes. The print shows `30 tracks`,
`nullPointerPaths` 0, `nonNull` and `reads` still far above 50 and 1,000 (file 21: 2,601 and 9,029, less the empty
tracks' share, a few hundred reads), so the old assertion passes; the test then fails on the first new one:
`null-pointer case (the empty tracks) reached 0 times, floor 74`. The second new assertion is not reached; it would
pass (`otherErrorPaths` still above 180).

## Part B (should-fix 3): the loosening narrowed to its evidence
**The condition.** The prefix-only comparison applies when the oracle's tail is exactly
`java.lang.NullPointerException: Cannot read field "cost" because "last_pe" is null`, the JDK 21 text -116 was argued
from. Exact, not a prefix: it is the narrowest text the evidence names. The unplanted Part A run prints the distinct
tails of both sides (digit runs masked) to confirm it is the oracle's actual text before Part B is written; if it is
not, Part B stops and is reported.
On every other error-path text, the whole debug text is compared again as before -116: the head (up to and including the
prefix) by digest, as now, then the tails with `assertEquals`, which together are the whole text. The tails are shown in
full in a failure (exception texts carry no position; a tail holding a run of 5 or more digits is shown by digest
instead, E1). Message `"$id: debugInfo after the error prefix (whole text, RECORD -116 narrowed)"`.
Consequences: a JIT fast throw (oracle tail `java.lang.NullPointerException`) is compared whole and equals the port's;
a JDK with other wording is compared whole and fails, which is a finding there, not a pass.
`assertTrue(oErr >= 0 && pErr >= 0)`, the head comparison and the two "wrote something after" assertions all stay.

**The premise, unverified:** the end-of-file texts were equal on the JVM before -116 (the review's inference). If the
whole comparison fails on any end-of-file text on the unplanted port: stop, restore, report the first differing pair.
**Prediction, unplanted:** pass; every end-of-file tail `java.io.EOFException` on both sides.

**The plant (port, through `revert.sh`, filter `*ReadBinaryTest`).** `OsmTrack.kt:560`:
`debugInfo?.append("Error reading rawTrack: " + e)` becomes
`debugInfo?.append("Error reading rawTrack: " + e.toString().replace("EOFException", "EofException"))`.
It changes the port's tail on end-of-file texts only: the null-pointer tail contains no `EOFException`.
**Prediction under the plant, run twice:**
1. Against the Part A test (the committed -116 comparison), before Part B is written: pass. Prefix equal, both sides
   non-empty after it.
2. Against the Part B test: fails on the first end-of-file text in loop order, track 0's cut 0 (no whole file at any
   target errs; the cut list starts at 0): `track 0, c0-0, target 0/0, shift 0, profile true, debug true: debugInfo
   after the error prefix (whole text, RECORD -116 narrowed) expected:<java.io.E[OF]Exception> but was:<java.io.E[of]
   Exception>` (JUnit's diff brackets as it draws them). The test stops at the first; the number of texts the plant
   would fail if each were checked is `otherErrorPaths` from the unplanted run, all of them.

## Checks on every planted run
0 ` e: ` lines; the XML newer than the plant's stamp; the failure is one this plant can produce; the planted file `cmp`
equal to its saved copy afterwards; the forward change still in the tree (`git diff` of the test against its commit
empty, the new assertions present).

## Results
(Appended after the runs; the sections above are unchanged since `be7d809`.)

All local runs: OpenJDK 21.0.12.1, `:brouter:jvmTest --tests '*ReadBinaryTest'`, `--max-workers=2`. Unplanted runs used
`--rerun-tasks` with a stamp touched before each; planted runs went through `revert.sh`. A first unplanted attempt
did not run the test: `--rerun-tasks` refetched the `msunReference` sources and `curl` got a 503 (`BUILD FAILED`, 0
` e: `, no XML); it was rerun unchanged and is not counted below.

**Part A, landed in `2fb0531`** as designed: `nullPointerPaths` (oracle tail starts with
`java.lang.NullPointerException`) and `otherErrorPaths`, asserted `>= 74` and `>= 6 * tracks.size` after the old
reach assertion, which is unchanged. The test also prints the split and the distinct tails of both sides, digits masked.

**Unplanted, twice** (0 ` e: `, 1 fresh XML each, `20:44:43Z` and `20:45:36Z`): 1 test, 0 failures, both runs
identical: `32 tracks, 9029 reads compared, 2601 tracks returned, 4522 debug texts, 1549 on the error path` (file 21's
figures exactly); `116 null-pointer, 1433 other`; tails `oracle java.io.EOFException`, `port java.io.EOFException`,
`oracle java.lang.NullPointerException: Cannot read field "cost" because "last_pe" is null`,
`port java.lang.NullPointerException`. So the setup is deterministic on this machine (two runs, and file 21's session
24 run); the floors stay floors, as the design says. 116 against the floor of 74; 1,433 against 192.

**Plant A, `revert.sh` name `s66ap`**, the diff exactly as designed (line 69). 0 ` e: `; 1 fresh XML (stamp
`20:45:53`, XML written `20:46:13`). 1 test, 1 failure:
`java.lang.AssertionError: null-pointer case (the empty tracks) reached 0 times, floor 74`, as predicted. The print:
`30 tracks, 8505 reads compared, 2358 tracks returned, 4258 debug texts, 1369 on the error path`, `0 null-pointer,
1369 other`, tails EOF only. So the old `nonNull > 50 && reads > 1000` (2,358 and 8,505) ran first and passed; the
second new assertion would have passed (1,369 >= 180). The test file `cmp` equal to its saved copy after; and `cmp`
equal to a copy taken before the plant, so the forward change was in the tree throughout. 64 of the 1,433 end-of-file
texts belonged to the empty tracks.

**Plant B against the committed -116 comparison, `s66bp1`** (port `OsmTrack.kt:560`, the diff exactly as designed,
run before Part B was written): 0 ` e: `; 1 fresh XML (`20:46:56Z`). 1 test, 0 failures, as predicted. The print shows
the plant took: port tails `java.io.EofException` and `java.lang.NullPointerException`, oracle unchanged; `116
null-pointer, 1433 other`. The port `cmp` equal to its saved copy after.

**Part B, landed in `aceedc4`**: the condition is the oracle's tail equal to
`java.lang.NullPointerException: Cannot read field "cost" because "last_pe" is null` (`JDK21_LAST_PE_NPE`), seen as the
oracle's actual tail in both unplanted Part A runs. Otherwise `assertEquals(shown(oTail), shown(pTail), "$id: debugInfo
after the error prefix (whole text, RECORD -116 narrowed)")`.

**Unplanted** (0 ` e: `, 1 fresh XML `20:47:45Z`): 1 test, 0 failures; counts and tails as above. The premise holds on
this JVM: all 1,433 end-of-file texts are equal whole, oracle and port. The pass also shows that every one of the 116
null-pointer tails was exactly the constant: any other wording would have been compared whole against the port's
`java.lang.NullPointerException` and failed.

**Plant B against Part B, `s66bp2`**, the same diff: 0 ` e: `; 1 fresh XML (stamp `20:47:59`, XML `20:48:11Z`). 1 test,
1 failure, as predicted to the character:
`org.junit.ComparisonFailure: track 0, c0-0, target 0/0, shift 0, profile true, debug true: debugInfo after the error
prefix (whole text, RECORD -116 narrowed) expected:<java.io.E[OF]Exception> but was:<java.io.E[of]Exception>`.
The port `cmp` equal to its saved copy after; the test `cmp` equal to a copy taken before the plant; `git status` showed
only the (then uncommitted) test. The same port edit passed `s66bp1`, so the narrowed comparison fails it where the -116
one passes. It would fail 1,433 texts if each were checked [I: every end-of-file tail contains `EOFException`, and the
plant rewrites each]; the test stops at the first.

**CI, run 43504930148 on `aceedc4`.** All four jobs `success`, 0 ` e: ` lines in each log:
- `jvm`: TOTAL 245 tests, 0 failures (245 before; assertions added, no test). `ReadBinaryTest` 1 test, 0 failures,
  printing the same counts and tails as the local runs: `116 null-pointer, 1433 other`, the oracle's null-pointer tail
  the JDK 21 text. So CI's JDK writes the same text and the narrowed rule applies there as here.
- `ios-smoke`: TOTAL 58, 0 failures; arm64 TOTAL 7, 0 failures; gate `OK: all 2 classes ran`. Simulator-checked.
- `ios-memory`: TOTALs 3, 1, 1, 0 failures. `jvm-production`: TOTAL 2, 0 failures.

**Not shown.**
- Both changes are JVM-only; `ReadBinaryTest` has no iOS counterpart in this change, and on Native the class names
  differ (the port's comment at `OsmTrack.kt:557-559`). Nothing here says anything about iOS.
- Each plant ran once. CI ran the unplanted test once; the two-run determinism is this machine's.
- A JDK that words the null pointer differently, or a JIT fast throw with no message, would be compared whole; the
  latter is argued to pass (both `java.lang.NullPointerException`), not seen, since every oracle tail here carried
  the message.
- The floor 74's structural argument is a reading of the setup, checked only against one count (116); the "about 53
  with one empty track" figure is inferred and was not planted.
- CI was read on `aceedc4` only, not on `2fb0531`.
