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
