# A1 pre-build report: the module layout, an Xcode project made on a runner, an empty Compose Multiplatform app on the simulator

Plumb Ledger, session 79 (skeleton, `565faa2`) and session 79b (every section below), 2026-11-18. A report only: nothing
is built in the product tree. Session 79 was killed by a container restart after the skeleton; 79b continues the file
and pushes after each section.

Base: `brouter-port-5b` at `0f9e0fd` (code), `565faa2` (this file's skeleton), clean tree, confirmed by 79b with
`git fetch origin`, `git merge --ff-only origin/brouter-port-5b` ("Already up to date") and `git rev-parse HEAD`.

Governing rules: Forager's `CLAUDE.md`, read from `/home/user/Forager/CLAUDE.md` and diffed against `origin/pre-main`
(`f29db48`): one line differs (the "Main changes only by a promotion" bullet, which on `pre-main` adds two owner-ruled
exceptions for Forager's workflow files and Play tracks). Nothing in that difference bears on A1.

## 1. What is there now

All at `0f9e0fd` unless stated.

**Modules.** One. `settings.gradle.kts:16` `include(":brouter")`; `rootProject.name = "forager-ios"` (`:15`). Repositories
are `gradlePluginPortal()` and `mavenCentral()` for plugins (`:3-6`) and `mavenCentral()` only for dependencies (`:9-13`).
**No `google()` repository.** Compose Multiplatform's artifacts (`org.jetbrains.compose.*`) are on Maven Central, but its
transitive androidx artifacts (lifecycle, annotation and similar KMP-published androidx libraries) are published to
Google's Maven; whether an iOS-only Compose Multiplatform app resolves with `mavenCentral()` alone is tested in section 2.
The root `build.gradle.kts:2-4` declares the Kotlin Multiplatform plugin `apply false`.

**Versions.**
- Kotlin `2.3.10`: `gradle/libs.versions.toml:4`, with the comment at `:3`: "Kotlin 2.3.10 exactly, Forager's pin, moved
  only together with Forager (Phase 1 report, item C, Amendment 1)". Forager's own pin is the same, `2.3.10`, at
  `gradle/libs.versions.toml:7` on Forager `pre-main` (`f29db48`); Forager's KSP is `2.3.10` (`:48`).
- Gradle `9.0.0`: `gradle/wrapper/gradle-wrapper.properties` (`distributionUrl`, with a `distributionSha256Sum`). Forager
  itself is on Gradle `9.7.0` (its wrapper on `f29db48`); the two repositories do not share a Gradle version today.
- The catalog has four versions (`kotlin`, `okio 3.18.2`, `jacoco 0.8.16`, `atomicfu 0.33.0`; `libs.versions.toml:4-12`),
  three libraries and one plugin (`kotlin-multiplatform`). No Compose, no coroutines, no serialization.
- `gradle.properties`: `org.gradle.jvmargs=-Xmx3g -Dfile.encoding=UTF-8` and `kotlin.code.style=official`, nothing else.

**`:brouter`'s targets.** `brouter/build.gradle.kts:110-117`: `jvm` (JVM target 17), `iosArm64()`,
`iosSimulatorArm64()`. No `androidTarget`, no `iosX64` (the comment at `:1-2`). No `binaries.framework` anywhere: the
module produces klibs and a test executable only, never a framework. Source sets on disk: `commonMain`, `commonTest`,
`iosTest`, `jvmTest`. `commonMain` depends on okio (`api`, because okio's `Path` is in the entry point's signatures, `:121-122`)
and atomicfu (`implementation`, `:124`).

**The engine's test harness that A1 must not disturb.** `jvmTest` depends on six fetch/build tasks (`oracleJvm()`,
`:150-175`), sets libm-intrinsic JVM flags by `os.arch` (`:166-170`) and runs against the BRouter 1.7.10 oracle jar. Any
change to the root build or to `:brouter`'s plugin set reaches these tasks; an app module beside `:brouter` that does not
touch `brouter/build.gradle.kts` does not (inferred from Gradle's project model; checked only by reading).

**CI (`.github/workflows/brouter.yml`).** Triggers on every push except `docs/**` (`:6-10`) and on pull requests. Four jobs:
- `jvm` (`:18-44`), `ubuntu-24.04`: `:brouter:jvmTest`, then both iOS klib compiles (no link on Linux).
- `ios-smoke` (`:49-167`), `runs-on: macos-26` (`:50`, "arm64; not macos-latest, which moves"), timeout 45 min. It
  selects Xcode with `sudo xcode-select -s /Applications/Xcode_26.0.1.app` (`:99-104`, the step named "Kotlin 2.3.10's
  published Xcode is 26.0"), prints `xcodebuild -version`, the runtimes and the devices. It then fetches tiles and the
  oracle (`:108-109`), draws the generated cases on the runner's JVM (`:114-117`), and runs `./gradlew --no-daemon
  :brouter:iosSimulatorArm64Test` (`:118-128`), passing environment into the simulator process as `SIMCTL_CHILD_*`.
  **It never builds an app, never calls `xcodebuild` to build, never calls `simctl install/launch`.** The simulator is
  reached only through the Kotlin Gradle plugin's test task, which links a test executable
  (`linkDebugTestIosSimulatorArm64`) and runs it on a simulator device the plugin picks; the job does not name a device.
  Gating is by `tools/junit-ran.py --simulator` over a named class list (`:61-86`, `:136-138`).
- `ios-memory` (`:176-268`), `macos-26`, timeout 95 min, the same Xcode selection; memory groups, R's fallback, the stack
  ladder.
- `jvm-production` (`:273-309`), `ubuntu-24.04`.
Actions are pinned by commit SHA (`actions/checkout` v7.0.1, `actions/setup-java` v6.1.0, `actions/upload-artifact`
v7.0.2; `:22-23`, `:40`). Java is Temurin `21.0.13+9` in every job.

**What the runner actually is** (read from the `ios-smoke` log of run `44030325384`, job `133860432798`, on `dee43df`,
fetched with `gh api .../actions/jobs/133860432798/logs` today):
- Image `macos-26-arm64`, version `20261102.0473.1`; its software list is
  `https://github.com/actions/runner-images/blob/macos-26-arm64/20261102.0473/images/macos/macos-26-arm64-Readme.md`
  (log lines 16-19).
- `xcodebuild -version` after the select: `Xcode 26.0.1`, build `17A400` (log 226-227).
- Simulator runtimes on that image: iOS 26.2 (23C54), 26.4 (26.4.1, 23E254a), 26.5 (23F77) (log 229-231). **No iOS 26.0
  runtime is listed**, although Xcode 26.0.1 is the selected Xcode; the Kotlin test task nonetheless ran on one of the
  listed runtimes and passed. Devices include `iPhone 17`, `iPhone 17 Pro`, `iPhone 17 Pro Max` on each runtime and
  `iPhone 16e` on 26.2 (log 241-271).
- Kotlin/Native `2.3.10` for `macos-aarch64` is downloaded by the build (`downloadKotlinNativeDistribution`, log 479-480),
  with LLVM 19 essentials and libffi as one-time downloads (log 481-487): about 70 s that a cache would remove.

**How long the jobs take** (same run, from `gh run view 44030325384 --json jobs`):
- `ios-smoke` 23 min 15 s in total. Steps: checkout 3 s; setup-java 8 s; probe 4 s; select Xcode and list simulators 11 s;
  fetch tiles and oracle 49 s; draw cases on the JVM 4 min 57 s; `iosSimulatorArm64Test` 16 min 39 s, of which the
  Kotlin/Native download and compile to the first `linkDebugTest` is about 2 min 30 s (log 479-496) and the test run
  itself about 13 min 40 s (log 497-500).
- `jvm` 34 min 39 s; `ios-memory` 55 min 44 s; `jvm-production` 22 min 24 s.
- So a macOS job that only installs Kotlin/Native, compiles a small module and links it costs about 3 to 4 minutes of
  runner time before any app work (inferred from the step times above, not measured on an app).

**Observed in passing, not part of A1.** The latest run, `44231196963` on `0f9e0fd`, was still in progress when read;
its `jvm-production` job had failed in 47 s: `curl: (22) The requested URL returned error: 503` inside
`:brouter:msunReference` (job `134556396929`'s log). That is a remote host refusing a pinned source fetch, not a code
failure; reported, not touched.

2. Version choices - pending
3. The module layout - pending
4. An Xcode project with no Mac - pending
5. Launch and screenshot on the simulator in CI - pending
6. What needs the owner - pending
7. Which decisions are technical - pending
8. The order of work for A1 - pending
9. What A1 will not show - pending
