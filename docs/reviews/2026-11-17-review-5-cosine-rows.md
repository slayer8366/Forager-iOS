# Review 5: the cosine rows (sessions 75, 75b, 76, 77)

Reviewer: Samphire Witness. Date: 2026-11-17. Code under review: `origin/brouter-port-5b` at `e703277` (checked:
`git fetch origin`, then `git rev-parse origin/brouter-port-5b`; the worktree was moved there, detached). Range
`9d37170^..e703277`. Read-only on the code: nothing in the worktree was edited, no plant was run.

Marks: [R] read in the tree, a committed log or a CI log; [C] run by me; [I] inferred; [U] unverified. Every iOS result
is "simulator-checked", and none is from a run of mine. No coordinate and no node id is written here.

## 1. Verdict

**The claim holds on the evidence, with nothing blocking.** On run 44020606098 (head `31939ed`, the last code commit;
the two commits after it touch one findings file only), I read in the job logs: the logic run 472 of 472 on the oracle's
table with the oracle off its row in 0; the production record 59 of 59 equal in each of the 8 rows on msun's table with
the oracle off its row in 0; and on the simulator (simulator-checked) 472 of 472 equal on six fields and the tuple
digest. Each run printed "own row read by 59" for every row. The oracle's and the port's values are computed
independently and compared on all six fields.

Three things should be fixed before the claim is leaned on later, none of which changes today's numbers:

1. The JVM half of the claim is a reading of one run, held by no gate. The production class asserts neither equality nor
   that its cases are the selected ones, and its job has no check that the class ran.
2. "Every case read its own row" is true, but it follows from how the cases were selected. It is not a second finding
   and should not be worded as one.
3. One class comment says the production run "holds" the oracle to its row. It counts and prints.

## 2. Findings

### Blocking

None.

### Should fix

**S1. The production run does not assert that its cases are the selected ones.**
`RoutingEngineCosRowsProductionRecordTest.kt:57` counts `offRow`; `:81-82` print it as the last column; no assertion
reads it (`:78-80`, `:93-94` are the only assertions). `CosRowsRun.kt:26-28` says "The logic and production runs also
run the oracle in their JVM and hold it to each row's tuple digest". That is true of the logic run
(`RoutingEngineCosRowsTest.kt:55`) and not of the production run.
- How it would mislead: if the generator ever redraws differently in that job (a JDK change, a re-pinned tile), the
  class passes, and "59 | 59 | 0" would be about cases nobody selected. Only the last column would show it.
- Today: the last column is 0 in all 8 rows (`jvm-production` log, the `cosrows production record: COS...` lines) [R].
- Fix: assert `offRow == 0` per row. That judges the case list, not the port, so it does not break "recorded, not
  judged". Correct the comment either way.

**S2. The JVM half of the claim has no standing check, and its job cannot tell that the class ran.**
- `brouter.yml:288-290` passes two `--tests` filters. Gradle fails only when no filter matches at all
  (`tools/junit-ran.py:7-8` says so itself). The job has no `junit-ran.py` step (`:291-300`). A renamed or moved class
  leaves the job green with `tests 2`.
- The class never fails a difference (its own comment, `:21-25` of the class file), and on the `jvm` job it returns at
  the gate (`:33-37`). So msun against the oracle on the JVM is asserted in no job. The simulator half is asserted
  (`Step10Cases.kt:109`, held by `SIMULATOR_CLASSES`, `brouter.yml:68`).
- How it would mislead: a later green `jvm-production` says nothing about these 472 unless someone reads the lines.
- Fix: add `python3 tools/junit-ran.py brouter/build/test-results/jvmTest <the two classes>` to the job; and say in the
  answer that the JVM figure is a reading of run 44020606098, while the simulator figure is gated on every push.
- How I know: read; the Gradle behaviour is from the script's own header, not from a run of mine [I].

**S3. "Every case read its own row" is implied by the selection; the wording presents it as a finding.**
- A case is kept only if the oracle's route lies inside its row's band (`E3CosRowsTest.kt:61-64`) with both ends in it
  (`:58`). Every link distance reads the row of the link's mid latitude (`CheapRuler.kt:53, 57`). So any run that
  returns the selected route reads that row, on any table. Session 76 says this of its own check
  (`s76-cos-rows-selection.md:196-197`: "cannot fail here independently of the keep rule"); the rejection column is 0 in
  all 8 rows (`:181-188`).
- What the assertion does show: the engine took the seam it was handed, so the table under test was the one read. That
  is worth having. It does not show how often a row was read (`CountingLibm.reads`, `CountingLibm.kt:27`, has no
  caller) or that the row's value moved any number.
- Sentences that get ahead: `s77-cos-rows-runs.md:146-147`, "Every case read its own (c) row on msun's table, so the
  differing value was read in every case", stated under a [C] heading. The seam records that a row was fetched, not that
  `kx` was used; that each reader uses element 0 is true by my reading (`CheapRuler.kt:58`,
  `WaypointMatcherImpl.kt:77`, `CheapAngleMeter.kt:18`) but is an inference there. And `:221-222`, "every one of which
  read its own row of the table", placed as if it strengthened the 472.
- Suggested wording for the owner: "each case's route lies inside its row's band, and the port fetched that row from
  the table under test".

### Notes

**N1. The untraced `FormatJson.kt:330` is closed: it cannot reach these runs.** [R]
`speedText` is called only from `FormatJson.kt:110` and `FormatGpx.kt:316, 364`. A search of `commonMain` for a
constructor call of `FormatJson`, `FormatGpx`, `FormatKml` or `FormatCsv` finds only the four class declarations, and
`RoutingEngine*.kt` names none of them. `CosRowsRun.port` (`CosRowsRun.kt:75-98`) and `EngineRun.runTrack`
(`EngineRun.kt:46-75`) read the track's fields and format nothing. The other direct `MsunLibm` uses in `commonMain` are
`RoutingContext.kt:19` (the default), `Route.kt:75` (the public call) and `SearchBoundary.kt:14` (a default; no
constructor call in `commonMain`). No `RoutingContext()` without a seam exists in `commonMain`. So the production run
reads msun's table and the logic run the oracle's (`OracleLibm.kt:10-11` calls the Java's own `CheapRuler`).
Latent, outside this claim: a formatter with `showspeed` in a logic run would read msun's table at `:330`, uncounted.

**N2. What 472 of 472 can show.** Link distances are rounded to whole metres (`OsmNode.kt:95`,
`OsmPathElement.kt:54`), so a 1-ulp `kx` moves a result only at a rounding boundary [I, the cost path was not traced].
The coder's own 0.1% plant left 7 of 59 cases unchanged (`s77-cos-rows-runs.md:177-178`). So 472 of 472 is the
expected outcome and bounds nothing beyond "not on these". `s77:224` says "a sample of 59 per row, not every route";
keep that sentence in what the owner is told.

**N3. "1 ulp in `kx`" is a carried figure.** `s77-cos-rows-runs.md:9`. The new code checks only that `kx` or `ky`
differs bit for bit (`CosRowsRun.kt:40-46`, `CosRowsGenerator.kt:32-36`), and that the differing rows are the 13
(`CosRowsCommonDigestTest.kt:34`). Which element, and by how much, comes from earlier work I did not re-read [U].

**N4. Failure messages name the wrong file for this group.** `Step10Cases.kt:109` ("against expected-1b.tsv") and
`NtCasesWriteTest.kt:149` ("cases are expected-1b's rows") are reached with the cosine rows' group
(`NtCasesWriteTest.kt:142`). A red run would send its reader to the wrong file.

**N5. The design table names a file that was not built.** `s77-cos-rows-runs.md:45` gives
`jvmTest/.../oracle/e3/CosRowsCases.kt`; the file is `router/CosRowsRun.kt` (named correctly at `:262`).

**N6. Session 75 never records its own P3.** `s75-cos-tiles-pins.md:104` predicts CI on the green commit; the file
ends at the local run (`:288-289`). Run 43995165286 on `22092e7` concluded success [R, conclusion only; logs not read].

**N7. `SIMCTL_CHILD_BROUTER_COS_TILES_DIR` is exported in four steps and read by nothing** (`brouter.yml:125, 200, 220,
237`; the comment at `:121-123` says so). Remove it, or have the class hold each line's eighth field to it.

**N8. The mac writer's cases are not shown to be the selected ones by an oracle run there.** The iOS jobs' writer
rebuilds the cases on an arm64 JVM and runs no oracle. What shows identity is the simulator's result: 472 equal against
472 stored tuples that are all distinct (counted by me [C]). Sound, but the findings do not spell out that step.

**N9. `jvm-production` ran 35 m 13 s of its 45-minute limit** (job log, `BUILD SUCCESSFUL in 35m 13s`) [R]. The new
class is 32 s of it. A timeout would be red, not silent.

## 3. The eight questions

**1. Can each new check fail, and where does it run?** Reachability first. `jvmTest` leaves out the `e3.strata`
package (`brouter/build.gradle.kts`, `filter { excludeTestsMatching(e3StrataPackage) }`); the `jvm` job runs the rest
unfiltered (`brouter.yml:29`).

| class | job, by which line | asserts, on what | a path that passes unseen? |
|---|---|---|---|
| `CosTilePinsTest` | `jvm` (`:29`) | the pin file's size and SHA-256, its five rows against the test's own pins, each fetched tile's size and SHA-256, the two folders | no. Seen red in CI by design (`s75:267-269`) [R, the coder's]. The constant is the release's: I fetched the asset, 2,491 bytes, same SHA-256, `cmp` equal to `tools/tile-pins-cos.tsv` [C] |
| `CosRowCountsTest` | `jvm` | the port reader's node count per row against the pin file's `# row` lines; the 8 pairs, once each | no. All 8 printed equal in CI [R] |
| `E3CosRowsTest` | none; `e3Strata` only, local | selection, row assertion, stability | not in CI (stated, `s76:308`) |
| `E3CosRowsExpectedFileTest` | `jvm` | 472 rows, 59 per prefix, stratum equals prefix, SHA-256 | no. I recomputed the SHA-256 [C] |
| `RoutingEngineCosRowsTest` | `jvm` | oracle off row empty; port equal on six fields and digest; row assertion | no. `CosRowsRun.cases` asserts 59 ids per row and the id sets (`CosRowsRun.kt:58, 67`); the test asserts 472 (`:30` of the test) |
| `RoutingEngineCosRowsProductionRecordTest` | `jvm-production` (`:288-290`); in `jvm` it returns at its gate | counts add up, 59 per row, every id ran, row assertion. **Not** equality, **not** off-row | yes: S1, S2 |
| `CosRowsCommonDigestTest` | `jvm` | the constant equals the file in order; its row lists equal the tables' | no |
| `CosRowsEngineCommonTest` | `ios-smoke` (`:128`, gated `:68, 138`); in `jvm` it returns at "unset" (`Step10Cases.kt:41-43`) | six fields and tuple digest against the stored rows; own row; group rows | no on the simulator (question 5) |

No comparison of a value with itself was found. `CosRowsCommonDigestTest.kt:28` compares each stored digest with one
recomputed from the fields, which is a real check of the common FNV code.

**2. Does "every case read its own row" hold?** Yes as stated, with S3's limit on what it means. The seam is the only
road to the table for the engine (N1). Production wraps `MsunLibm`, logic wraps `OracleLibm`. The group assertion
would fail if the two tables did not differ at the 8 rows in that JVM, since the rows are intersected with the rows
computed to differ (`CosRowsRun.kt:115-117`); on the simulator the 13 are a constant held on the JVM
(`CosRowsCommonDigestTest.kt:34`), and the simulator's table is held by `CheapProductionTableTest`, already in the gate
and outside this range [R, its comment only]. Threads: `RoutingEngine.kt:20` says the Java's `Thread` base is not kept;
I found no thread or coroutine start in that file [R].

**3. Is the production comparison sound?** Yes. The oracle is the Java engine (`E3CaseRun.kt:3-5` imports
`btools.router`), the port is the Kotlin engine with msun's table (`CosRowsRun.kt:78, 86`), and six fields are compared
(`RoutingEngineCosRowsProductionRecordTest.kt:58-61`). Not equal by construction: all 472 stored rows are kind `route`
with 472 distinct tuples [C], so no pair is equal by both sides failing alike; and the coder's table plant moved 52 of
59 in one row [R, not re-run].

**4. Are the cases the selected ones?** Yes, on three legs. (a) The file is the selection: its ids are, per row and in
order, the "taken" rows of the committed selection tables and the ids of the stability tables; its tuples equal both
tables' R1; every stored tuple digest recomputes; all 472 logged stable [C, my script over the committed files].
(b) A rebuilt case is held to that file by an oracle run in the `jvm` job (0 off) and counted in `jvm-production`
(0 off, S1). (c) An id carries its draw number and the tuple carries a digest of the whole route, and the tuples are
distinct, so "same id, another case" would show.

**5. The simulator class.** The gate holds it: it is the 25th name (`brouter.yml:68`); the log has `1 test cases, 1
ran, 0 skipped, 0 failed` for it and `OK on the iOS simulator: all 25 classes ran` [R]. It cannot return silently
there: `Step10Cases.ntFile` fails off the JVM when the variable is unset (`Step10Cases.kt:40-46`); a missing file
fails at `:63-64`; an empty or short file fails at `:110`; a missing tiles folder fails per case at `:86`. If the
writer step fails, the simulator step is skipped and the gate step (`if: always()`) fails every class.

**6. `Step10Cases.check`'s new parameter.** No existing caller's behaviour changes [R]. Before: `EngineRun.run(...)`,
which is `runTrack(...).outcome` with `libm = MsunLibm` (`EngineRun.kt:34-35, 48`). Now: `runTrack(..., libm =
libm).outcome` with the default `MsunLibm` (`Step10Cases.kt:58, 87`). None of the nine other callers passes `libm`;
the two trailing-lambda callers (`MemoryEngineCommonTest.kt:36`, `REngineCommonTest.kt:34`) still bind to `onCase`,
which stays last. Their groups ran and were gated in the same CI run.

**7. Wording ahead of evidence.** S3's two sentences (`s77:146-147`, `:221-222`), S1's comment (`CosRowsRun.kt:26-28`),
and `CountingLibm.kt:8-9` ("Every scale row the engine reads goes through `LibmSeam.scales`"), written flat while
`s76:33` marks the one exception as not traced. N1 now supports it for the engine.

**8. The coder's "not checked" lists.** Honest, and incomplete by these: that the production class asserts no off-row
count (S1); that `jvm-production` has no class-ran check (S2); that "1 ulp in `kx`" was not re-measured (N3); that the
mac writer's cases rest on the simulator's result for identity (N8); `FormatJson.kt:330` is in session 76's list
(`s76:281`) and dropped from session 77's, though it bears on the production count the same way.

## 4. What I ran, and what I only read

Ran [C]:
- `git fetch`, `git rev-parse`, `git diff --stat` over the range and over `31939ed..e703277`.
- `gh api` for run 44020606098, its four jobs' logs, and the conclusions of the runs on `22092e7` and `5f280f7`. In the
  four logs: 0 lines matching `^e: ` and 0 matching ` e: `; `TOTAL:` 262, 3, 61 and 7, 3/1/1; the gate lines; every
  `cosrows` and `step10 COS` line quoted above.
- A script over the committed files: `expected-cosrows.tsv` against the 8 selection and 8 stability tables (ids, order,
  R1 tuples, verdicts), every tuple digest recomputed, kinds, distinct tuples.
- A search of the 16 committed tables, the committed log and the four CI logs for long decimals or pairs of them: none.
- The release's `tile-pins.tsv` fetched and compared with the committed copy; SHA-256 of `expected-cosrows.tsv`.

Read only [R]: every code file in the range, the three findings files, the plan's sections 3.3, 4.4, 4.5 and 6,
`tools/junit-ran.py`, the seam's readers in `commonMain`.

Did not do:
- No Gradle run, no plant, no `e3Strata`. Every plant result above is the coder's.
- No prediction was committed before reading, unlike my earlier reviews: nothing here was a run whose outcome I could
  have predicted.
- The red run's logs on `bd2801d` and the logs of the runs on `22092e7` and `5f280f7`.
- `E3Select`, `E3Stability`, `E3Strata`, `TileNodes`, `RGenerator.nearest`: used by the selection, read only at their
  call sites.
- Whether cost, as distinct from distance, passes through an unrounded use of `kx` (N2).
- `CheapProductionTableTest` and `CheapProductionRecordTest` beyond their headers (N3, question 2).
- The simulator: I cannot run it. Its results are the CI log's, simulator-checked.
