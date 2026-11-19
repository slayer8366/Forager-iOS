# Kotlin move to the 2.4 line's patch 21: the move, and the engine compared against a baseline

Session 89 (coder "Plumb Ledger"), 2026-11-19. Branch `app-kotlin`, cut from `app` at `820224b`. Nothing here is merged
anywhere; no pull request is opened. The plan followed is sections 3, 5 and 7 of
`docs/audits/2026-11-19-app-phase-a2/kotlin-2-4-move-prebuild.md` (session 88), whose claims were treated as premises.

Marks: **[C]** confirmed by a command run, a log read or a file read in this session; **[I]** inferred; **[?]** not
determined. Every iOS result here is **simulator-checked**, never "checked".

## The owner's words, verbatim

- "Move Kotlin to 2.4.20", then, asked whether to keep that or take the later patch: "2.4.21".
- "Both on branches, one merge word".
- "Forager's own planner and coder".

These are recorded as RECORD -214 (`RECORD.md:8157-8178` on branch `ccr-8ee6e614-32enzt` at `98e60ff`; that entry is
not on `main`, whose `RECORD.md` ends at -198 as fetched today) **[C]**.

*My reading, labelled as mine:* the pin is the 2.4 line's patch release 21, written in the catalog as the version
string; this branch is "on branches" for this repository; nothing for Forager is written by this session.

## 1. Scratch repeat on the ruled version

**Setup [C].** `git archive 820224b` into scratch twice (`s89/k2421` for the compiles, `s89/k2421t` for the JVM tests),
then in each: the one catalog line `kotlin = "2.3.10"` to the ruled version, and `settings.gradle.kts` pointed at the
local retrying mirror (session 86's `mirror.py`, copied unchanged), as session 88 did. `diff -r` against an untouched
archive of `820224b` shows those two files only. Local JDK: OpenJDK 21.0.12.1 (Ubuntu), not CI's Temurin.

**Compiles [C: scratch `s89/compile.log`].**
`./gradlew --no-daemon :brouter:compileKotlinJvm :brouter:compileKotlinIosArm64 :brouter:compileKotlinIosSimulatorArm64
:app:compileKotlinIosArm64 :app:compileKotlinIosSimulatorArm64`:

- `BUILD SUCCESSFUL in 2m 42s`, exit 0.
- All five compile task headers printed with no suffix (not `SKIPPED`, not `UP-TO-DATE`, not `NO-SOURCE`). The tasks
  that did print `SKIPPED` are `checkKotlinGradlePluginConfigurationErrors` and the Compose resource generators of `:app`
  (no resources), none of them a compile.
- Error lines: 0 matching `^e: `, 0 matching ` e: `.
- Warning lines (`^w: `): 10. Nine are the three known `commonMain` warnings once per `:brouter` compile task
  (`BExpressionContextWay.kt:16:10`, `StdPath.kt:52:29`, `StdPath.kt:242:37`, three each), the same three session 88
  listed for 2.3.10 and patch 20. The tenth is Gradle's `Native task 'iosSimulatorArm64Test' is disabled` on a Linux host.
- Kotlin/Native for Linux at the ruled version was downloaded through the mirror
  (`kotlin-native-prebuilt-2.4.21-linux-x86_64.tar.gz`, 219,973,234 bytes, in scratch `s89/mirror.log`).
- The compiler that built them: all four iOS klib manifests (`brouter` and `app`, `iosArm64` and `iosSimulatorArm64`)
  read `compiler_version=2.4.21`, `abi_version=2.4.0`; `CheapRuler.class` from the JVM compile carries
  `@Metadata mv=[2,4,0]`.

**`:brouter:jvmTest` [C: scratch `s89/jvm.log`, XML in `s89/k2421t/brouter/build/test-results/jvmTest`].**
`./gradlew --no-daemon :brouter:jvmTest`, as `brouter.yml`'s `jvm` job runs it, with `BROUTER_PRODUCTION_RECORD` unset:

- `BUILD SUCCESSFUL in 54m 34s`, exit 0; `compileKotlinJvm`, `compileTestKotlinJvm` and `jvmTest` headers unsuffixed.
- `tools/junit-counts.py` over the results: `TOTAL: tests 262, failures 0, errors 0, skipped 0 in 116 files`; 116 XML
  files on disk.
- 0 lines `^e: `, 0 lines ` e: `.
- The compiler and library that ran: `OraclePinTest.class` (test source set) carries `mv=[2,4,0]`, and
  `:brouter:dependencies --configuration jvmTestRuntimeClasspath` resolves `kotlin-stdlib:2.4.21`.

**What step 1 shows:** at the ruled version, on this container's JDK, everything this repository builds on Linux
compiles with no error line, and the JVM suite runs 262 tests green. **What it does not show:** CI's Temurin JDK, any
link or run on the Mac, and any comparison of output; that is CI's job, below. It is not compared with session 88's
scratch results, which were on patch 20.

## 2. Baseline commit B

**B is `35a8223`** (full: `35a8223837e4a3623ce94c58f91fc80babec775f`), parent `820224b`. No version change. Two edits
**[C: `git show 35a8223`]**:

- `.github/workflows/app.yml`: `branches: [app]` to `branches: [app, app-kotlin]`, with a comment line citing RECORD -214.
- `gradle/libs.versions.toml`: the comment above the Kotlin pin. It keeps "moved only together with Forager (Phase 1
  report, item C, Amendment 1)", drops the version number from the comment (so the comment does not have to change
  again in M), and says the move to the 2.4 line's patch 21 is the owner's ruling, made on branches in both repositories
  and merged on one word (RECORD -214). The pin line itself still reads 2.3.10.

**Both workflows ran on B [C: `actions/runs?head_sha=35a8223…`]:**

| Workflow | Run | Attempt | Result |
|---|---|---|---|
| `brouter` | 44465935981 | 1 | success, all four jobs |
| `app` | 44465935991 | 2 | success; attempt 1's `app-klibs` failed on a download, below |

**`app` attempt 1, `app-klibs` (job 135363607175), an outside failure, rerun.** Gradle failed while configuring the root
project, before compiling anything: "Could not resolve org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.10. … Could not
parse POM https://plugins.gradle.org/m2/org/jetbrains/kotlin/kotlin-gradle-plugin/2.3.10/kotlin-gradle-plugin-2.3.10.pom
… Could not find org.jetbrains.kotlin:kotlin-gradle-plugins-bom:2.3.10", `BUILD FAILED in 22s`. The BOM exists:
`curl` of `repo.maven.apache.org/…/kotlin-gradle-plugins-bom-2.3.10.pom` from this container returned 200, 4606 bytes,
and the other seven jobs on the same commit resolved the same plugin. So it was a download error, not the commit's. The
failed job alone was rerun (`POST /actions/jobs/135363607175/rerun`); attempt 2's `app-klibs` is job 135367322585,
`BUILD SUCCESSFUL in 55s`. The other three `app` jobs are attempt 1's, carried into attempt 2.

**Every job on B, at the known counts [C: each job's log, `GET /actions/jobs/<id>/logs`]:**

| Job | Job ID | Xcode (`xcodebuild -version`) | What it printed |
|---|---|---|---|
| `jvm` | 135363606679 | none (Linux) | `TOTAL: tests 262, failures 0, errors 0, skipped 0 in 116 files`; `compileKotlinJvm`, `compileTestKotlinJvm`, `compileKotlinIosArm64`, `compileKotlinIosSimulatorArm64` headers unsuffixed |
| `jvm-production` | 135363607120 | none (Linux) | `TOTAL: tests 3, failures 0, errors 0, skipped 0 in 2 files`; `OK: all 2 classes ran` |
| `ios-smoke` | 135363607044 | 26.0.1, `17A400` | `TOTAL: tests 61, failures 0, errors 0, skipped 0 in 30 files`; `OK on the iOS simulator: all 25 classes ran (…/iosSimulatorArm64Test)`; `TOTAL: tests 7, failures 0, errors 0, skipped 0 in 2 files`; `OK: all 2 classes ran` |
| `ios-memory` | 135363607091 | 26.0.1, `17A400` | `TOTAL` 3, 1 and 1 tests, 0 failures each; `OK on the iOS simulator: all 3 classes ran (…/ios-memory-groups)`; `OK on the iOS simulator: all 1 classes ran (…/ios-r-fallback)` |
| `png-region` | 135363606882 | none | the self-test's `ok` lines; success |
| `app-klibs` | 135367322585 (attempt 2) | none (Linux) | `lines matching '^e: ': 0`, `lines matching ' e: ': 0`; both `:app:compileKotlinIos…` headers |
| `app-framework` | 135363607151 | 26.2, `17C52` | `OK: the selected Xcode is Xcode 26.2`; `OK: the framework was linked in this job and its binary is arm64` |
| `app-bundle` | 135363607129 | 26.2, `17C52` | `OK: the control: the colour check failed on before.png (exit 1)`; `A1: first frame drawn`; `OK: the app printed 'A1: first frame drawn'`; `OK: the colour check passed on after.png (E0218A, tolerance 8, threshold 0.98)` |

Error lines in every job's log: 0 matching `^e: ` after the timestamp. The only ` e: ` matches anywhere are the two
lines of `app-klibs`' own counting step that contain the pattern as text. B is green for no reason of its own to stop
on; every iOS result above is simulator-checked.

## 3. Move commit M

**M is `6cdab88`** (full: `6cdab882caa32f142aa3c01114eee34fb9c53f68`). Its diff is the one line in
`gradle/libs.versions.toml`, `kotlin = "2.3.10"` to `kotlin = "2.4.21"`; `git diff --stat` prints `1 file changed, 1
insertion(+), 1 deletion(-)` **[C]**. No Xcode, Gradle, Compose Multiplatform, wrapper or other pin moved, and the build
asked for none. M's parent is a docs-only commit, not B: between B and M sit three commits to this file only, and
`git diff --stat 35a8223 6cdab88^ -- . ':!docs'` prints nothing **[C]**, so B and M differ outside `docs/` by that one
line.

**Both workflows ran on M:**

| Workflow | Run | Attempt | Result |
|---|---|---|---|
| `brouter` | 44473167850 | 1 | success, all four jobs |
| `app` | 44473167855 | 2 | success; attempt 1's `app-klibs` failed on a download, below |

**`app` attempt 1, `app-klibs` (job 135387283943), an outside failure, rerun.** The Gradle wrapper could not download
Gradle itself: "Downloading from https://services.gradle.org/distributions/gradle-9.0.0-bin.zip failed: timeout
(10000ms)", before any build ran (the job's own check printed "no compile task header in the log"). The failed job alone
was rerun; attempt 2's `app-klibs` is job 135392392430, `BUILD SUCCESSFUL in 51s`.

Every job of both runs checked out the commit it claims: each job log's `git log -1 --format=%H` prints `35a8223…` on B
and `6cdab88…` on M (the 17 logs read) **[C]**.

## 4. M compared against B

**How.** For each job, the B log and the M log (`GET /actions/jobs/<id>/logs`) were compared by a scratch script
(`s89/tools/cmp.py`, not committed) that strips the timestamp from each line, then (a) parses every JUnit XML the job
prints after `== brouter/build/test-results/…xml` and lists each test case's verdict, and each file's `<system-out>`
and `<system-err>` text line by line, without the `time`, `timestamp` and `hostname` attributes; (b) lists every `e:`
and `w:` line (repository path normalised) and every task header with a suffix; and (c) lists the gate lines
(`TOTAL:`, `OK…`, `FAIL…`, `BUILD …`, `Xcode …`, the Kotlin/Native directory). The two lists of each kind were diffed.
Its self-test: the B `ios-smoke` log against itself gives 0 differing lines of each kind; the same log against an older
`ios-smoke` log in scratch from an earlier session gives 14 changed output lines (durations), so the diff can turn
non-empty.

**Job IDs compared** (B, then M):

| Job | B | M |
|---|---|---|
| `jvm` | 135363606679 | 135387283782 |
| `jvm-production` | 135363607120 | 135387284594 |
| `ios-smoke` | 135363607044 | 135387284244 |
| `ios-memory` | 135363607091 | 135387284194 |
| `app-klibs` | 135367322585 | 135392392430 |
| `app-framework` | 135363607151 | 135387284298 |
| `app-bundle` | 135363607129 | 135387284420 |
| `png-region` | 135363606882 | 135387284441 |

### 4.1 Counts: the same [C]

| Job | B | M |
|---|---|---|
| `jvm` | `TOTAL: tests 262, failures 0, errors 0, skipped 0 in 116 files` | the same line |
| `jvm-production` | `TOTAL: tests 3 … in 2 files`, `OK: all 2 classes ran` | the same lines |
| `ios-smoke` | 61 in 30 files, `OK on the iOS simulator: all 25 classes ran`, 7 in 2 files, `OK: all 2 classes ran` | the same lines |
| `ios-memory` | 3 in 3 files, 1 in 1, 1 in 1; `all 3 classes ran (…/ios-memory-groups)`; `all 1 classes ran (…/ios-r-fallback)` | the same lines |

Every count line is text-identical between B and M; all have 0 failures, 0 errors, 0 skipped.

### 4.2 Test cases and verdicts: the same [C]

Parsed XML per job, each side: `jvm` 116 files, 262 cases; `jvm-production` 2 files, 3 cases; `ios-smoke` 32 files,
68 cases (61 + 7); `ios-memory` 5 files, 5 cases. The case lists (file, class, name, verdict) diff to **0 lines** in all
four jobs. So `MsunCosTableTest`, `MsunAtan2PairsTest`, `FloatTextCommonTest`, `FormatNumberTextTest`,
`NodeHashMapTieBreakTest` and `PlatformInvariantsTest`, the classes section 5 of the pre-build names as the ones that
would see LLVM contraction, Native number text or hash order, ran and passed on the simulator on both sides
(simulator-checked).

### 4.3 The production record, line by line: the same apart from durations [C]

`jvm-production`'s `<system-out>`: 94 lines each side, of which 72 each contain "production record". **13 lines
differ, and in every one the only differing token is a time in seconds printed with `%.1f s`:** the cosine rows' line
(`cosrows production record: 472 cases, 0 differing, in 23.1 s` against `22.9 s`;
`RoutingEngineCosRowsProductionRecordTest.kt:94`), the ten group rows of the hints-and-formats table (their last column,
`| %.1f s`, `RoutingEngineProductionRecordTest.kt:140`), and the two "groups in … s" totals (`:115`, `:146`). Every case
count, route count, "differing" count and per-field difference count in those lines is the same.

### 4.4 Every test's standard output: the same apart from durations and clock-read counts [C]

`<system-out>` and `<system-err>` lines per side: `jvm` 638, `jvm-production` 94, `ios-smoke` 1442, `ios-memory` 64
(the same count on B and M in each job). Differing lines, each read token by token:

- `jvm`, 19 lines. `GraphDecodeTest` (5): the decode time in ms after each tile's digest; the digests are equal.
  `RoutingEngineCosRowsTest` (1): `… port mismatches 0; 23.4 s` against `24.0 s`. `RoutingEngineMemoryTest` (3): "the
  port's runs N ms"; the deepest recursion depths and case counts are equal. `HintsFormatsLogicTest` (9): each group's
  seconds and the "all routed; … s" total. `RoutingEngineStopTest` (1): the FK9 line, "the port's clock, from its first
  read to its last, per case", a measured span and a clock-read count per case. On B all ten spans read `51 ms`; on M
  eight read `51 ms` and two `52 ms`. That span is a duration the test prints and does not compare
  (`RoutingEngineStopTest.kt:258`), and `52 ms` entries appear in earlier green runs on 2.3.10 kept in scratch (for
  example three in `ci80/jvm.log` and three in `ci67v/132198613840.log`), so I classed it with the durations.
- `jvm-production`, 13 lines: section 4.3.
- `ios-smoke`, 6 lines: the trailing "N s" of the COS, CR, DL-lat, DL-lon, LB and NT engine lines; every count, "equal
  on all six fields" figure and digest count before it is the same.
- `ios-memory`, 4 lines: the trailing "N s" of the MP64, ML5, PM5 and R engine lines. The stack ladder step's output
  (103 lines each side, `ladder:` lines and the ladder class's XML) differs only in the XML's `timestamp`, `hostname`
  and `time` attributes.

**Exclusions, named:** measured times (ms and s), the FK9 clock spans and clock-read counts, and the XML `time`,
`timestamp` and `hostname` attributes. Nothing else was excluded, and nothing else differs.

### 4.5 Compiler diagnostics [C]

- **`e:` lines:** 0 in every job of both runs, by `^e: ` after the timestamp and by ` e: ` (the only ` e: ` matches are
  the two lines of `app-klibs`' own counting step that contain the pattern as text).
- **The three `commonMain` warnings** (`BExpressionContextWay.kt:16:10`, `StdPath.kt:52:29`, `StdPath.kt:242:37`): 24
  lines on each side across the four engine jobs, text-identical.
- **`jvmTest` warnings:** the same files, lines and columns on both sides; the compiler's wording changed for four of
  them (`AsteriskUnsupportedTest.kt:27:9`, `:31:9`, `:48:20` lose the sentence "This will become an error in a future
  release. See …"; `VoiceHintProcessTest.kt:114:86` reads "Cast is redundant." where B read "No cast needed.").
- **New `w:` lines on M, not on B. These are findings, not resolved here:**
  - `w: ⚠️ Native task 'iosSimulatorArm64Test' is disabled`: twice in `jvm`, once in `jvm-production`, once in
    `app-klibs`; on Linux, where the simulator task cannot run (it ran on the Mac in `ios-smoke` and `ios-memory`). It
    names a task, not source.
  - `w: The number of threads 4 is more than the number of processors 3`: once in `ios-smoke` and once in
    `app-framework` on M, printed by the link task (`app-framework`: just before `:app:linkDebugFrameworkIosSimulatorArm64`
    finished); `app-bundle` on M shows the same text through Xcode as `warning: …`. B has it once, in `ios-memory`,
    from the R step's relink; M's `ios-memory` has it once too. It names the machine's processor count, not source.
  No `w:` line on M names a source file that B did not.
- **Task headers:** every compile and link task header (`compileKotlinJvm`, `compileTestKotlinJvm`,
  `compileKotlinIosArm64`, `compileKotlinIosSimulatorArm64`, `compileTestKotlinIosSimulatorArm64`,
  `linkDebugTestIosSimulatorArm64`, `:app:compileKotlinIos…`, `:app:linkDebugFrameworkIosSimulatorArm64`) appears
  unsuffixed in each job on M where it does on B. No compile task is `SKIPPED`. M adds new tasks of Kotlin 2.4's Swift
  package support (`generateSyntheticLinkageSwiftPMImportProjectForCinteropsAndLdDump`, `dumpXcodebuildArgsIphonesimulator`
  and four others) that print `SKIPPED`; none compiles or links.
- **One build difference that is not a result:** in `ios-memory`, B relinked the simulator test binary for the R
  fallback step (`> Task :brouter:linkDebugTestIosSimulatorArm64`, no suffix) where M found it `UP-TO-DATE`. On M that
  binary was linked earlier in the same job (the memory groups step, unsuffixed) by the 2.4.21 distribution on a fresh
  runner, so what ran is still the new compiler's output.

### 4.6 Which compiler built what ran [C unless marked]

- **Kotlin/Native, from each job's own log:** M's `jvm` and `app-klibs` name
  `/home/runner/.konan/kotlin-native-prebuilt-linux-x86_64-2.4.21`; M's `ios-smoke`, `ios-memory`, `app-framework`
  and `app-bundle` name `/Users/runner/.konan/kotlin-native-prebuilt-macos-aarch64-2.4.21`. The same lines on B name
  `…-2.3.10`. So the iOS klibs, the simulator test binaries, the framework and the app on M were built by 2.4.21.
- **The JVM compile:** no job prints the JVM compiler's version or the stdlib on the test classpath, so the version
  string for `compileKotlinJvm` and `compileTestKotlinJvm` is **not in the logs [?]**. What the logs do show: the
  compiler's diagnostic wording changed in the JVM test compile of every engine job (4.5), and `jvm-production`, which
  runs only JVM tasks, printed the new Kotlin Gradle plugin's `Native task … is disabled` line. Both say a different
  Kotlin built the JVM side on M **[I, from the log text]**; that it was exactly 2.4.21 there rests on the same Gradle
  build resolving one plugin version for both the JVM and Native tasks of `:brouter` **[I]** and on the scratch run in
  section 1 (`mv=[2,4,0]`, `kotlin-stdlib:2.4.21`).

### 4.7 Which Xcode each job used [C: `xcodebuild -version` in each log]

| Job | B | M |
|---|---|---|
| `ios-smoke` | Xcode 26.0.1, `17A400` | Xcode 26.0.1, `17A400` |
| `ios-memory` | Xcode 26.0.1, `17A400` | Xcode 26.0.1, `17A400` |
| `app-framework` | Xcode 26.2, `17C52` | Xcode 26.2, `17C52` |
| `app-bundle` | Xcode 26.2, `17C52` | Xcode 26.2, `17C52` |

No Kotlin warning or refusal about the Xcode version appears in any M log (searched for Xcode beside "warn", "tested",
"unsupported" and "compatib"; the one match on each side is the same `ld: warning` about `libicu` being built for a
newer simulator version, 18.5, than linked, 16.0, present on B as well). The published Xcode for this Kotlin line is
26.4 (pre-build section 3d); both selections are below it and both linked and ran.

### 4.8 The app [C]

On M, `app-bundle` printed `OK: the control: the colour check failed on before.png (exit 1)`, `A1: first frame drawn`,
`OK: the app printed 'A1: first frame drawn'` and `OK: the colour check passed on after.png (E0218A, tolerance 8,
threshold 0.98)`, as on B; `png-region`'s pixel count reads `2530188/2530188` on both. `app-framework` printed
`OK: the framework was linked in this job and its binary is arm64`. Simulator-checked.

## 5. Result

**The same.** On every job of both workflows, M gives the same counts, the same test cases with the same verdicts, the
same production record and the same standard output as B, except for measured durations and clock-read counts, which
are named above. There is no new error line. There are two new kinds of `w:` line on M, neither naming source
(section 4.5), reported here for the owner and the planner and not acted on.

The owner's ruling this rests on, as RECORD -214 records it: "2.4.21". And the order: "Both on branches, one merge
word". This branch is not merged anywhere.

## 6. What this does not show

- **The `iosArm64` (device) binary never runs.** Its klibs compile on Linux (`jvm`, `app-klibs`); no device code
  executes anywhere, and with no iPhone it cannot.
- **Nothing here speaks for Forager's Android build.** Forager's move is Forager's planner's and coder's ("Forager's own
  planner and coder"); nothing was written to slayer8366/Forager.
- The simulator runs only the subsets each job names, not the full JVM corpus (pre-build section 3a).
- Kotlin/Native code generation is compared by results only; no disassembly was compared. A contraction or similar
  change that no test's output can see would not show.
- Xcode 26.4, the published one for this Kotlin line, was not tried; moving Xcode is its own step (pre-build T3).

## 7. Disclosure

### Confirmed against inferred

- **Confirmed** (a command run, a log read, a file read, this session): every run ID, job ID, count line, verdict list,
  output diff, warning and Xcode line above; the Kotlin/Native directories in the logs; the scratch compiles and tests
  of section 1; M's diff being one line; the checkout commit of every job.
- **Inferred:** that the JVM compile on M was exactly 2.4.21 (4.6); that both CI failures rerun were outside failures
  (each failed before compiling anything, on a download, and passed on rerun with no change); that the FK9 `52 ms`
  spans are timing (they are printed durations, unasserted, and appear in earlier 2.3.10 runs).

### Could not determine

- The JVM compiler's version string from any CI job's own log: none prints it.
- Why 2.4.21's link prints the processor-count warning in jobs where 2.3.10 did not, and why 2.3.10 relinked the test
  binary for `ios-memory`'s R step and 2.4.21 did not. Neither changes a result; neither was investigated.
- Anything about Xcode 26.4, the device binary, or Forager.

### Premises in this brief that were wrong, or need a qualifier

- **"cite RECORD -214".** RECORD -214 is not on `main`: `main`'s `RECORD.md` ends at -198 (fetched today). It is on
  branch `ccr-8ee6e614-32enzt` (`98e60ff`, `RECORD.md:8155-8178`), which I read. The catalog comment cites it as the
  brief asked; whether that branch lands on `main` before the merge word is the planner's.
- **The pre-build's section 5, item 2,** says the production record has "the one line that carries a duration (`... groups
  in %.1f s`, `:115`)". There are more: every hints-and-formats group row ends in `%.1f s` (`:140`), the second total
  (`:146`) and the cosine rows' line (`RoutingEngineCosRowsProductionRecordTest.kt:94`). 13 lines carry a duration, not
  one. They were excluded as durations, each named.
- **The brief's step 2** expects the pin's comment to "keep 'moved only together with Forager'": kept. I also removed
  the version number from that comment so M could be the pin line alone.
- The rest held as stated: `app.yml` ran only on `app` before B; `brouter.yml` ignores a push that changes only
  `app.yml`; the known counts were exactly those on B.

### Decided beyond scope

- I ran the scratch JVM tests (step 1) in a second scratch copy, at the same time as the compiles, and pushed B while
  they were still running; B does not depend on them. M was pushed only after both were green and B was green.
- The findings file was committed in parts between B and M (docs only), so M's parent is not B (section 3).
- The two reruns were made with `gh api -X POST …/actions/jobs/<id>/rerun`, beyond the read-only `gh` calls the brief
  lists, as the brief's "the failed job may be rerun" requires some call.
- The comparison script and the token-level diff script stay in scratch (`s89/tools/cmp.py`, `s89/tools/tok.py`); no
  gating script was added to the repository.
