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

**The colour:** (filled in by step 2's commit).

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

(Pending.)

## 4. Confirmed vs inferred

(Pending.)

## 5. Could not determine

(Pending.)

## 6. Premises in the brief or the pre-build that were wrong

(Pending.)

## 7. Decided beyond scope

(Pending.)

## 8. Not checked

(Pending.)
