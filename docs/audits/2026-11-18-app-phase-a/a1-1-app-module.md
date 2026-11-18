# A1-1: the `:app` module, its iOS framework link, and `app.yml`

Plumb Ledger, session 80, 2026-11-18. Branch `app`, cut from `brouter-port` at `382204f`.

Base, confirmed at start: `git fetch origin`; `git branch --show-current` printed `app`; `git rev-parse HEAD origin/app`
printed `382204fee51ec59e13b50556117d947fe9d6e197` twice; `git status --porcelain` printed nothing.

Spec: the pre-build report (session 79b), read with `git show origin/brouter-port-5b:docs/audits/2026-11-18-app-phase-a/a1-prebuild.md`,
sections 2, 3, 7 and 8; the planner's decisions T1, T2, T3, T5, T6, T7, T8, T12 (RECORD -201). Governing rules: Forager's
`CLAUDE.md` (read from `/home/user/Forager/CLAUDE.md`).

The owner's rulings carried, verbatim: "Option B stands"; "I don't have a Mac and I don't care to get one"; "3 no iPhone";
"Map first (Recommended)"; "Placeholder for now (Recommended)"; "New branch \"app\" (Recommended)"; "no coordinates are
committed". Every macOS result below is "simulator-checked" at most; this step runs no simulator at all, so its macOS
results are "linked on a macOS runner", nothing more.

## 1. Design

**Build files.**
- `gradle/libs.versions.toml`: a version `composeMultiplatform = "1.12.2"`; libraries `compose-runtime`
  (`org.jetbrains.compose.runtime:runtime`), `compose-foundation` (`org.jetbrains.compose.foundation:foundation`),
  `compose-ui` (`org.jetbrains.compose.ui:ui`), all `version.ref = "composeMultiplatform"`; plugins `kotlin-compose`
  (`org.jetbrains.kotlin.plugin.compose`, `version.ref = "kotlin"`) and `compose-multiplatform` (`org.jetbrains.compose`,
  `version.ref = "composeMultiplatform"`). `kotlin` stays `2.3.10`; Gradle stays `9.0.0`. No Material3.
- Root `build.gradle.kts`: the two new plugins `apply false`, beside the existing Kotlin Multiplatform one.
- `settings.gradle.kts`: `include(":app")`; in `dependencyResolutionManagement`, Google's Maven with
  `content { includeGroupByRegex("androidx\\..*") }`, so every non-androidx group still resolves from Maven Central only.
  `pluginManagement` unchanged (both new plugins are on the Gradle Plugin Portal / Maven Central, as scratch showed).

**`app/`.**
- `app/build.gradle.kts`: Kotlin Multiplatform, the Compose compiler plugin, the Compose Multiplatform plugin; targets
  `iosArm64()` and `iosSimulatorArm64()` only, each with `binaries.framework { baseName = "ForagerApp"; isStatic = true }`;
  `commonMain` depends on the three catalog libraries. No dependency on `:brouter`. No jvm target (section 6).
- `app/src/commonMain/kotlin/com/zynergylabs/forager/app/App.kt`: `App()` is one `Box` filling the screen whose
  `drawBehind` draws the colour (filled rectangle over the whole draw area) and, on its first call only, prints
  `A1: first frame drawn`. The colour and the line come from the same draw lambda, so the line cannot be printed by a
  frame that did not draw the colour.
- `app/src/iosMain/kotlin/com/zynergylabs/forager/app/MainViewController.kt`:
  `fun MainViewController(): UIViewController = ComposeUIViewController { App() }`.

**The colour:** `Color(0xFFE0218A)`, ARGB `FF E0 21 8A`: opaque, red 224, green 33, blue 138 (a magenta), as
`A1_COLOUR` in `app/src/commonMain/kotlin/com/zynergylabs/forager/app/App.kt:11`. The line is `FIRST_FRAME_LINE` at `:14`.

**`.github/workflows/app.yml`.** Pushes to `app` touching `app/**`, `build.gradle.kts`, `settings.gradle.kts`, `gradle/**`,
`gradle.properties`, `gradlew` or the workflow itself. `permissions: contents: read`. Actions pinned by the same SHAs as
`brouter.yml` (`actions/checkout` v7.0.1, `actions/setup-java` v6.1.0); Temurin `21.0.13+9` as there. No cache (T8).
- `app-klibs`, `ubuntu-24.04`: `./gradlew --no-daemon --console=plain :app:compileKotlinIosSimulatorArm64
  :app:compileKotlinIosArm64`, output kept in a file; prints the counts of lines matching `^e: ` and ` e: `.
- `app-framework`, `macos-26`: Xcode 26.0.1 selected as `brouter.yml` selects it; `./gradlew --no-daemon --console=plain --info
  :app:linkDebugFrameworkIosSimulatorArm64` with output kept; then the listing step, which fails if the link task's
  header carries an outcome other than a plain run (`SKIPPED`, `UP-TO-DATE`, `NO-SOURCE`, `FROM-CACHE`), if the header
  is absent, if the framework directory or its binary is absent, or if `lipo -info` does not name `arm64`; and prints the
  directory, `lipo -info` and `file` on the binary. `--info` is there for section 6 of the brief (which stdlib the link
  used): it makes the Kotlin Gradle plugin print the compiler's arguments.

## 2. Predictions (written before any build file changed)

**Local, on this Linux container.**
- P1. `:app:compileKotlinIosSimulatorArm64` and `:app:compileKotlinIosArm64` end `BUILD SUCCESSFUL`, 0 `e: ` lines.
- P2. `:brouter`'s resolved graphs are byte-identical against `382204f` for every configuration compared; the root
  `buildEnvironment` only gains the Compose Gradle plugin, its hot-reload plugin and the Compose compiler plugin.
- P3. The listing step's script, run here on the Linux log (where the link is `SKIPPED` and no binary exists), fails.

**CI, `app.yml` on the code commit.**
- P4. `app-klibs`: both compile task headers printed without an outcome suffix, `^e: ` count 0, ` e: ` count 0,
  `BUILD SUCCESSFUL`.
- P5. `app-framework`: `> Task :app:linkDebugFrameworkIosSimulatorArm64` printed with no suffix; `BUILD SUCCESSFUL`;
  the listing prints `app/build/bin/iosSimulatorArm64/debugFramework/ForagerApp.framework` holding `ForagerApp`,
  `Headers/`, `Modules/` and `Info.plist`; `lipo -info` says the binary is a non-fat file of architecture `arm64`; `file`
  calls it an `ar` archive (a static framework's binary). Link time 2 to 6 minutes (a guess; the first Compose link in
  this project).
- P6. The stdlib (brief item 6): the link's argument list names no `kotlin-stdlib` klib from the Gradle cache; the
  stdlib used is the one inside the Kotlin/Native 2.3.10 distribution (inferred from how Kotlin/Native ships its stdlib;
  to be read from the `--info` log, or reported as not shown).

**CI, `brouter.yml` on the same commit** (it runs on every push outside `docs/**`).
- P7. `jvm` TOTAL 262, 0 failures; `jvm-production` 3 with `OK: all 2 classes ran`; `ios-smoke` 61 and 7 with
  `OK on the iOS simulator: all 25 classes ran` and `OK: all 2 classes ran`; `ios-memory` 3, 1, 1 with its two gate lines;
  0 compile errors under `^e: ` and ` e: ` in each job.

## 3. Results against predictions

Code commit: `8c8d3234cee8503e6eb873919ba8cf64b487e328`. Scan (`scan.sh HEAD^ HEAD`) printed 0 for it and for the skeleton
`3a3fcb2`.

### Local, on this Linux container

**How the network was handled.** Against the real hosts, the first `./gradlew` in the committed tree failed with 10
"Could not find" jars of the plugin classpath (`scratchpad/s80/compile1.log`); eight retries with `--refresh-dependencies`
failed with 2 to 6 each, and six without it failed identically on `kotlin-stdlib-2.3.10.jar` and
`kotlin-script-runtime-2.3.10.jar` (Gradle had cached them as missing). So, as session 79 did, the checks ran in two
scratch copies through the retrying mirror (`scratchpad/s79/tools/mirror.py`, port 18089):
- `scratchpad/s80/before`: `git archive 382204f`.
- `scratchpad/s80/after`: the working tree of this commit (`git ls-files -co --exclude-standard`).
In both, only the repository URLs in `settings.gradle.kts` were changed (portal, central, google to the mirror; the
androidx `content` filter kept); `diff` of the committed and the scratch `settings.gradle.kts` shows only those four
lines. Nothing committed points at the mirror.

- **P1, held.** In `after`: `./gradlew --no-daemon --console=plain :app:compileKotlinIosSimulatorArm64
  :app:compileKotlinIosArm64` printed `> Task :app:compileKotlinIosSimulatorArm64` and `> Task :app:compileKotlinIosArm64`
  with no suffix, also `checkIosSimulatorArm64MainComposeLibrariesCompatibility` and `checkIosArm64MainComposeLibrariesCompatibility`,
  and `BUILD SUCCESSFUL in 20s`; lines matching `^e: ` 0, ` e: ` 0, `^w: ` 0 (`scratchpad/s80/after-compile.log`).
- **P2, held.** In each copy: `./gradlew --no-daemon -q :brouter:dependencies` (no `--configuration`, so every
  configuration of `:brouter`: 99 configuration headings in the report, 147 "No dependencies" sections among them, none
  "FAILED" or "Could not"), `:brouter:dependencies --configuration iosSimulatorArm64TestCompileKlibraries` separately, and
  the root `buildEnvironment`.
  - `:brouter:dependencies`: `diff` prints nothing; both files 1,035 lines, SHA-256 `0c395a25...56e2` for both. The
    report is a resolved one: for example `jvmTestRuntimeClasspath` lists okio 3.18.2, atomicfu 0.33.0, `kotlin-stdlib`
    `2.1.21 -> 2.3.10`, kotlin-test 2.3.10 with kotlin-test-junit.
  - `iosSimulatorArm64TestCompileKlibraries` alone: identical (okio 3.18.2, `kotlin-stdlib:2.3.10`, atomicfu 0.33.0,
    kotlin-test 2.3.10).
  - `buildEnvironment`: the after copy adds `org.jetbrains.kotlin.plugin.compose...gradle.plugin:2.3.10` with
    `compose-compiler-gradle-plugin:2.3.10`, `org.jetbrains.compose...gradle.plugin:1.12.2` with `compose-gradle-plugin:1.12.2`
    and `hot-reload-gradle-plugin` `{prefer 1.2.0} -> 1.2.0`, and one constraint line inside `kotlin-gradle-plugins-bom`
    (`compose-compiler-gradle-plugin:2.3.10 (c)`). Nothing removed, no version changed; every `kotlin-gradle-plugin*`
    entry is `2.3.10` with no arrow. (The rest of the diff is the tree's `\---` becoming `+---` because the multiplatform
    plugin is no longer the last root entry.)
  - What this does not show: the oracle jar and other file inputs (not in a dependency report; unchanged because
    `brouter/` is not edited), and `:brouter`'s test results (CI, P7).
- **P3, held.** The listing step's `run:` block was extracted from `app.yml` and run with `bash -e`:
  (a) in `after`, after a real `./gradlew --console=plain --info :app:linkDebugFrameworkIosSimulatorArm64` on Linux
  (`> Task :app:linkDebugFrameworkIosSimulatorArm64 SKIPPED`, `Skipping task ... as task onlyIf 'Task is enabled' is false`,
  `BUILD SUCCESSFUL in 9s`): three FAIL lines, exit 1. Then three contrived trees, with `lipo` stubbed to print arm64:
  (b) plain header and a binary present: `OK: ...`, exit 0; (c) `SKIPPED` header with a binary present: FAIL, exit 1;
  (d) plain header, no binary: FAIL, exit 1. So a skipped link fails the step even if a binary were somehow present,
  and a missing binary fails it even if the header looked right.

### CI, `app.yml`, run `44237677080` on `8c8d323` (both jobs `success`)

Logs read with `gh api .../actions/jobs/<id>/logs`, saved as `scratchpad/s80/ci-<id>.log`. Log line numbers below are
in those files.

- **P4, held.** `app-klibs`, job `134577660565`, 04:29:32 to 04:31:07 UTC (1 min 35 s). `> Task :app:compileKotlinIosSimulatorArm64`
  (log 208) and `> Task :app:compileKotlinIosArm64` (215), no suffix; `BUILD SUCCESSFUL in 1m 26s` (217); the count step
  printed `lines matching '^e: ': 0` (230) and `lines matching ' e: ': 0` (231). Counted over the job's own `tee`'d
  file, where lines carry no timestamp prefix, so `^e: ` can match there.
- **P5, held, link time inside the guess.** `app-framework`, job `134577660279`, 04:29:32 to 04:33:42 (4 min 10 s).
  Runner image `macos-26-arm64` (log 17), its version printed at log 18 (a date, then a four-digit build, then `.1`;
  written apart for the E1 scan). `Xcode 26.0.1`, `Build version 17A400` (170-171).
  - The link step: started 04:29:44 (184), ended 04:33:33 (1359), `BUILD SUCCESSFUL in 3m 47s` (1355), `5 actionable
    tasks: 5 executed` (1356). `> Task :app:linkDebugFrameworkIosSimulatorArm64` with no suffix (1324), followed by
    `Task ':app:linkDebugFrameworkIosSimulatorArm64' is not up-to-date because: No history is available.` The link task
    itself ran from 04:32:08 (1324) to 04:33:32 (1354): **about 1 min 24 s**. Before it, the Kotlin/Native
    2.3.10 `macos-aarch64` bundle was downloaded (1058) and moved into `~/.konan` (1064-1066, 04:31:24 to 04:31:45).
  - The listing step: the grep echoed the plain header (1384) and nothing for the suffixed pattern; `ls -laR` showed
    `ForagerApp` (118,978,440 bytes), `Headers/ForagerApp.h` (6,967), `Info.plist` (945), `Modules/module.modulemap`
    (129) (1385-1403); `Non-fat file: app/build/bin/iosSimulatorArm64/debugFramework/ForagerApp.framework/ForagerApp is
    architecture: arm64` (1404); `file`: `current ar archive` (1405); `OK: the framework was linked in this job and its
    binary is arm64` (1406).
  - The path the pre-build inferred (`app/build/bin/iosSimulatorArm64/debugFramework/`) is the path the plugin wrote.
- **P6, half held: the argument list is not in the log.** `--info` did not make the Kotlin Gradle plugin print the
  compiler's arguments: no line contains `Arguments` or `konanc` except the recording step's own command (1407-1408).
  What the log does show:
  - `kotlin-stdlib` 2.3.20: only its `.pom` and `.module` were downloaded (904-905), i.e. metadata read during
    resolution. No `kotlin-stdlib` 2.3.20 artifact file (jar, klib or zip) was downloaded anywhere in the job.
  - The only `kotlin-stdlib` artifact file downloaded is `kotlin-stdlib-2.3.10.jar` (1247), fetched together with
    `kotlin-script-runtime-2.3.10.jar`, `kotlin-reflect-1.6.10.jar` and `kotlin-daemon-embeddable-2.3.10.jar`
    (1240-1248): a JVM jar for the compiler's own tool classpath (inferred from its neighbours), not a native klib.
  - 40 `.klib` downloads in the job; none is a `kotlin-stdlib` one.
  - The runner has no cache (T8) and the job started cold, so no 2.3.20 stdlib file could have come from an earlier run
    (inferred: the image's `~/.gradle` was not listed).
  - So the native stdlib the link used is, by elimination, the one inside the Kotlin/Native 2.3.10 bundle at
    `~/.konan/kotlin-native-prebuilt-macos-aarch64-2.3.10` (1064-1066). That is **inferred from what was and was not
    downloaded**, not read from a command line. What would settle it: the link's argument list, for example from
    `-Pkotlin.native.verbose...` style debug output or `--debug`, not tried here (section 7).

### CI, `brouter.yml`, run `44237677010` on `8c8d323`

**Written by session 81, not session 80.** Session 80 was killed while this run was in progress; everything from here
to the end of the file was written by session 81 (Plumb Ledger, 2026-11-18) from the pushed work (`44d9f84`) and from
the four job logs, read with `gh api -X GET repos/slayer8366/Forager-iOS/actions/runs/44237677010/jobs` and
`.../actions/jobs/<id>/logs`, saved as `scratchpad/s81/ci-<id>.log`. Log line numbers below are in those files. The
run: `completed`, `success`, `head_sha` `8c8d3234cee8503e6eb873919ba8cf64b487e328`, 04:29:29 to 05:31:51 UTC.

Compile-error counts are taken over each whole job log; `^e: ` is counted after stripping the runner's timestamp
prefix (otherwise it could never match), ` e: ` on the raw lines.

- **P7, held.** Every count equals the baseline (pre-build section 8, RECORD -197).
  - `jvm`, job `134577660161`, `success`: `TOTAL: tests 262, failures 0, errors 0, skipped 0 in 116 files` (log 1686).
    `^e: ` 0, ` e: ` 0.
  - `jvm-production`, job `134577660535`, `success`: `TOTAL: tests 3, failures 0, errors 0, skipped 0 in 2 files` (417),
    `OK: all 2 classes ran` (535). `^e: ` 0, ` e: ` 0.
  - `ios-smoke`, job `134577660708`, `success`: `TOTAL: tests 61, failures 0, errors 0, skipped 0 in 30 files` (546),
    `OK on the iOS simulator: all 25 classes ran (brouter/build/test-results/iosSimulatorArm64Test)` (2212),
    `TOTAL: tests 7, failures 0, errors 0, skipped 0 in 2 files` (2348), `OK: all 2 classes ran` (2351). `^e: ` 0, ` e: ` 0.
  - `ios-memory`, job `134577660484`, `success`: `TOTAL: tests 3` (457), `TOTAL: tests 1` (530), `TOTAL: tests 1` (626),
    each `failures 0, errors 0, skipped 0`; gate lines `OK on the iOS simulator: all 3 classes ran (brouter/build/test-results/ios-memory-groups)`
    (698) and `OK on the iOS simulator: all 1 classes ran (brouter/build/test-results/ios-r-fallback)` (700). `^e: ` 0, ` e: ` 0.
  - `BUILD FAILED` appears in none of the four logs.

## 4. Confirmed vs inferred

Confirmed (each from a log line or a file cited above):
- `:app` compiles for both iOS targets on Linux with no compile errors (P1 locally; P4 in CI, run `44237677080`).
- `:brouter`'s resolved dependency reports are byte-identical with and without `:app` (P2, local scratch copies).
- The listing step fails on a skipped link and on a missing binary (P3, local, contrived trees).
- The simulator framework links on `macos-26` with Xcode 26.0.1 and is a static arm64 archive at
  `app/build/bin/iosSimulatorArm64/debugFramework/ForagerApp.framework` (P5, job `134577660279`).
- The four `brouter.yml` jobs print the baseline counts on `8c8d323` with no compile errors (P7, run `44237677010`).

Inferred:
- That the native stdlib the link used is the Kotlin/Native 2.3.10 bundle's own (P6): from what was and was not
  downloaded, not from a command line.
- That the `kotlin-stdlib-2.3.10.jar` download is the compiler's JVM tool classpath (from its neighbours in the log).
- That the job's Gradle and Kotlin/Native state started cold (no cache is configured; the image's `~/.gradle` was not
  listed).

## 5. Could not determine

- The Kotlin/Native link's argument list: `--info` did not print it (P6). What would settle it: a link run with
  Gradle `--debug` or the Kotlin/Native compiler's verbose option, not tried.
- Whether the framework works inside an app: not in A1-1's scope (A1-2 builds the bundle, A1-3 launches it).

## 6. Premises in the brief or the pre-build that were wrong

- Session 80's own design (section 1, `app.yml` comment above the link step) expected `--info` to make the Kotlin
  Gradle plugin print the native link's arguments. It did not (P6). The comment in `.github/workflows/app.yml` still
  says it does (`.github/workflows/app.yml:58`); session 81 leaves that comment and step unchanged, since A1-2 adds a
  job rather than editing that one, and records the stale comment here instead.
- No other premise was found wrong. Session 81 did not have session 80's transcript, so a premise session 80 noticed
  but did not write down is not recoverable here.

## 7. Decided beyond scope

Nothing that session 81 can find in the pushed work (`3a3fcb2`, `8c8d323`, `b80c016`, `44d9f84`) beyond the dispatch
and the pre-build's sections 2, 3, 7 and 8. The one addition the design names, `--info` on the link step, is stated in
section 1 with its reason (brief item 6).

## 8. Not checked

- The link's argument list (section 5).
- Anything at run time: no app bundle, no simulator, no launch (A1-2 and A1-3).
- The oracle jar and other file inputs of `:brouter` against `382204f` beyond the dependency reports (unchanged
  because `brouter/` was not edited; that is from `git diff --stat`, not a byte comparison of the inputs).
- Whether session 80 meant to write anything else in these sections: they were written after it was killed, from
  the pushed commits and the logs only.
