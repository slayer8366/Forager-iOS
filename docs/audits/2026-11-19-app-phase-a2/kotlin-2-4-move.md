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
