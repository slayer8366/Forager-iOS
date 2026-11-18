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
- Image `macos-26-arm64`, version 20261102 then, after a dot, 0473.1 (written apart here only because the E1 scan,
  `scan.sh`, reads a run of four digits after a dot as a possible coordinate; it is a runner image version, not one).
  The log's "Included Software" line points at that image's Readme under the tag path
  `actions/runner-images/blob/macos-26-arm64/<that version without its last .1>/images/macos/macos-26-arm64-Readme.md`
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

## 2. Version choices

**Sources, all fetched 2026-11-18.**
- Compose Multiplatform's compatibility page,
  `https://kotlinlang.org/docs/multiplatform/compose-compatibility-and-versioning.html`. It has **no per-version Kotlin
  table**. It says: "The latest Compose Multiplatform is always compatible with the latest Kotlin"; since 1.8.0, use at
  least Kotlin 2.1.0, and "upgrade to Kotlin 2.2.20 for projects targeting platforms with rapidly evolving support, such
  as iOS and web"; and "Compose Multiplatform requires the Compose Compiler Gradle plugin at the same version as the
  Kotlin Multiplatform plugin". Its Jetpack mapping lists 1.12.2 as the newest (Jetpack Compose 1.12.1). The brief's
  "official compatibility table" for Kotlin does not exist on that page; the per-release Kotlin minimums are in the
  changelog, below.
- Compose Multiplatform's changelog, `https://raw.githubusercontent.com/JetBrains/compose-multiplatform/master/CHANGELOG.md`
  (saved in scratch, 7,659 lines), and its releases page `https://github.com/JetBrains/compose-multiplatform/releases`.
  `gh api` on that repository was refused ("GitHub access to this repository is not enabled for this session"), so the
  releases were read through the web page.
- Maven Central's `org/jetbrains/compose/compose-gradle-plugin/maven-metadata.xml`: newest versions `1.12.2`, then
  `1.13.0-alpha01`, `-alpha02`, `-beta01`, `-beta02`; `lastUpdated 20261104123324`.
- Kotlin's releases page, `https://kotlinlang.org/docs/releases.html`: 2.3.10 (5 Feb 2026, bug fix), 2.3.20, 2.3.21,
  2.4.0 (3 Jun 2026), 2.4.10, 2.4.20, 2.4.21 (8 Oct 2026, latest stable); 2.5.0 planned for December 2026.
- Kotlin Multiplatform's compatibility guide,
  `https://kotlinlang.org/docs/multiplatform/multiplatform-compatibility-guide.html`: the row for KMP plugin `2.3.10`
  reads Gradle `7.6.3–9.0.0`, AGP `8.2.2–9.0.0`, Xcode `26.0`. The `2.4.20–2.4.21` row reads Gradle `7.6.3–9.7.0`,
  AGP `8.5.2–9.3.1`, Xcode `26.4`.

**What each Compose Multiplatform line needs of Kotlin, for iOS** (changelog lines, as numbered in the saved copy):
| Line | Status today | Kotlin for native (iOS) | Where |
|---|---|---|---|
| 1.10.x | stable, older | "Kotlin 2.2 is required for native and web platforms" | `:2067` |
| 1.11.x | stable, older | "Kotlin 2.3 is required for native and web platforms" | `:1149` |
| 1.12.0 to 1.12.2 | **1.12.2 is the latest stable** (15 Oct 2026, "Latest" on the releases page) | no new minimum stated in 1.12.0's or 1.12.2's notes (`:135-177`, `:529-675` read) so 1.11's "Kotlin 2.3" stands | `:135` |
| 1.13.0 | beta (`beta02`, 4 Nov 2026) | "The minimal supported Kotlin version for k/native is 2.4.0" | `:193` |

So **Compose Multiplatform 1.12.2 is the newest stable release whose stated Kotlin minimum for iOS (2.3) the repo's
Kotlin 2.3.10 meets. The 1.13 line, when it is stable, needs Kotlin 2.4.0 or later for iOS.** 1.11.0 also dropped
Apple x86_64 targets (`:1160`), which matches `:brouter`'s choice of no `iosX64` (`brouter/build.gradle.kts:2`).

**Tried in scratch, and it held (a fact for compiling, not for linking or running).** A throwaway two-file project at
`/tmp/claude-0/-home-user/2bc41cf9-5db8-5578-a058-248445c292c6/scratchpad/s79/cmp/` (never committed): Gradle 9.0.0
(the repo's own wrapper copied), plugins `org.jetbrains.kotlin.multiplatform 2.3.10`, `org.jetbrains.kotlin.plugin.compose
2.3.10`, `org.jetbrains.compose 1.12.2`; one module `:app` with `iosArm64()` and `iosSimulatorArm64()`, each with
`binaries.framework { baseName = "ForagerApp"; isStatic = true }`; `commonMain` depends on Compose Multiplatform 1.12.2's
`runtime`, `foundation` and `ui`; `App()` is a `Box` filling the screen with one solid colour, and `iosMain` has
`MainViewController() = ComposeUIViewController { App() }`. Run on this Linux container:
`./gradlew --no-daemon :app:compileKotlinIosSimulatorArm64 :app:compileKotlinIosArm64 :app:linkDebugFrameworkIosSimulatorArm64`
gave `BUILD SUCCESSFUL in 1m 19s` (`run9.log`): both iOS klibs compiled, 0 lines starting `w: ` or `e: `, the plugin's
own `checkIosSimulatorArm64MainComposeLibrariesCompatibility` and `checkIosArm64MainComposeLibrariesCompatibility` ran
and passed, and `linkDebugFrameworkIosSimulatorArm64` was `SKIPPED`, because a framework links only on macOS. **So
nothing here shows the framework links, or that the app runs; that needs the macOS runner (A1's first dispatch).**

What else the scratch build showed:
- **Google's Maven repository is required, `mavenCentral()` alone is not enough.** With only Maven Central for
  dependencies (the repo's `settings.gradle.kts:9-13` as it stands), resolution failed with 15 "Could not find" lines,
  13 distinct, all `androidx.*` artifacts pulled by Compose Multiplatform's iOS variants, for example
  `androidx.compose.runtime:runtime-saveable:1.12.1`, `androidx.lifecycle:lifecycle-common:2.9.4`,
  `androidx.annotation:annotation:1.9.1` (`run7-central-only.log`). Adding Google's Maven, restricted by content filter
  to `androidx.*` groups, resolved everything. The changelog says the same since 1.6 (`:5873`: "`google()` maven
  repository is now required"). The engine's `:brouter` resolves nothing from there (inferred: none of its four
  catalog entries is an androidx artifact), but a new repository in `settings.gradle.kts` is visible to every module,
  so it should carry a content filter that admits only `androidx` groups.
- **Compose Multiplatform raises `kotlin-stdlib` in the app module to 2.3.20.** `dependencyInsight` on
  `iosSimulatorArm64CompileKlibraries`: "org.jetbrains.kotlin:kotlin-stdlib:2.3.20 ... By conflict resolution: between
  versions 2.3.20, 2.3.10, ...", requested by `org.jetbrains.skiko:skiko-iossimulatorarm64:0.150.2`. That is the app
  module's own dependency graph; `:brouter`'s graphs are its own and do not change unless `:brouter` itself gains a
  dependency (inferred from how Gradle resolves per project; not run against the real repo). Whether Kotlin/Native
  links the Maven stdlib at all, or only the one bundled with the 2.3.10 compiler, was not determined. kotlinx-coroutines
  resolves to 1.9.0 in the same graph.
- **The root build's plugin classpath stays on Kotlin 2.3.10.** `buildEnvironment` shows `kotlin-gradle-plugin:2.3.10`
  and `kotlin-gradle-plugin-api:2.3.10` with no upgrade arrow; the Compose Gradle plugin adds only
  `hot-reload-gradle-plugin 1.2.0`.
- **This container's network returns spurious 404s from Maven Central.** Twelve parallel `curl`s of one existing POM
  gave 10 × 200 and 2 × 404. Gradle caches a 404 as "missing", so the first runs failed on random artifacts; the
  scratch build went through a local retrying mirror (`scratchpad/s79/tools/mirror.py`). This is this container's
  path only; GitHub's runners resolve from the real hosts. Recorded because a coder repeating the probe here will meet
  it.

**Does Kotlin have to move? No, for A1.** 1.12.2 works with 2.3.10 by its own changelog and by the scratch compile.
Kotlin would have to move only to take Compose Multiplatform 1.13 or later (Kotlin 2.4.0 for iOS, `:193`).

**What a Kotlin move would risk for the engine** (inferred, from the repo's own record of what the engine is held to):
the catalog comment pins Kotlin "moved only together with Forager" (`libs.versions.toml:3`), and Forager is on 2.3.10
(Forager `f29db48`, `gradle/libs.versions.toml:7`). A compiler change touches every compiled number path of the engine:
Kotlin/JVM's bytecode for `jvmTest`'s differential runs against the BRouter 1.7.10 oracle, and Kotlin/Native's LLVM
code for the simulator runs held to the same expected values (`msun` cos and atan2 tables, the cosine rows, the
memory groups and the stack ladder). A move would need, at least, `jvm`, `jvm-production`, `ios-smoke` and
`ios-memory` all green with unchanged counts, and `e3Strata` rerun on the oracle's JDK, before it could be called free.
Kotlin 2.4's own Xcode row is 26.4 (the guide above), so it would also move the selected Xcode from 26.0.1, which the
stack ladder's results depend on (inferred: stack sizes and the simulator runtime come with the Xcode). None of this is
needed for A1.

**Exact pins proposed for A1** (each line in `gradle/libs.versions.toml`, exact, no ranges):
| Entry | Version | Why |
|---|---|---|
| `kotlin` | `2.3.10` (unchanged) | the engine's and Forager's pin |
| plugin `org.jetbrains.kotlin.plugin.compose` | `version.ref = "kotlin"` | the compatibility page: same version as the KMP plugin |
| plugin `org.jetbrains.compose` and `compose-multiplatform` libraries | `1.12.2` | latest stable; Kotlin 2.3 for native |
| Gradle wrapper | `9.0.0` (unchanged) | 2.3.10's supported range ends at 9.0.0 |
| Xcode | `26.0.1` (`/Applications/Xcode_26.0.1.app`, build 17A400), as `brouter.yml` | 2.3.10's Xcode is 26.0; it is on the runner image (`macos-26-arm64` Readme, Xcode table, row `26.0.1 17A400`) |
| simulator runtime | iOS `26.2` (the oldest on the image) with device type `iPhone 17`, named explicitly | so the device does not move when the image's default does |
| Material3 | **not used in A1** | 1.12.2 ships `material3:1.12.0-alpha03`, an alpha (changelog `:167`); A1 needs `foundation` only |

The runner image's Readme
(`https://raw.githubusercontent.com/actions/runner-images/main/images/macos/macos-26-arm64-Readme.md`, fetched today)
is the same image version the CI log showed in section 1, and lists Xcode 26.6 as the default, 26.0.1 to 26.5 also
installed, and simulator runtimes iOS 26.2, 26.4 and 26.5 only. Building against the iOS 26.0 simulator SDK and
running on a 26.2 runtime is what `ios-smoke` already does for its test executable (section 1); for an app bundle it is
inferred to work the same way and is shown only by A1's first run.

## 3. The module layout

**What constrains it.**
- `:brouter` is held bit-for-bit to its oracle, and its build file carries that harness (section 1). A1 should not
  edit `brouter/build.gradle.kts` at all.
- The owner ruled "Share the domain now (Recommended)" (-153), and the planner read it narrowly in the same entry:
  "before anything is written there the planner brings the owner a concrete proposal: which repository holds the
  shared module, how the Android build consumes it". That proposal is B1. A1 therefore should not create the shared
  module, nor decide where it lives: either would answer B1's question before the owner hears it.
- The plan lists "the module layout and how Android uses a library with no Android target" among the planner's
  decisions (plan `:170-175` on `origin/main`).
- The Android app is in another repository (Forager, Gradle 9.7.0, AGP 9.3.1, Kotlin 2.3.10; section 1 and Forager
  `f29db48` `gradle/libs.versions.toml:2,7`). `:brouter` has no Android target.

**Option A: one app module beside `:brouter`, and an Xcode-side folder that is not a Gradle module.**
```
settings.gradle.kts      include(":brouter"), include(":app")
brouter/                 unchanged
app/                     KMP + Compose Multiplatform; targets iosArm64, iosSimulatorArm64; binaries.framework (static)
  src/commonMain/        App() - the Compose UI
  src/iosMain/           MainViewController() = ComposeUIViewController { App() }
iosApp/                  the Xcode side: the spec or project (section 4), Info.plist, a Swift entry of a few lines
```
- The iOS framework is produced by `:app` (`binaries.framework`, static, one `baseName`). Static means the framework
  links into the app executable and nothing is embedded or signed at run time (inferred from what a static framework
  is; the A1 run shows it).
- `:app` does not depend on `:brouter` in A1 (an empty app needs no routing). It gains `implementation(project(":brouter"))`
  in Phase C. This keeps A1's link time and A1's failures free of the engine.
- A future shared domain module (B1's answer) would sit as a third module beside these two, for example `:domain`,
  with `:app` depending on it, **if** B1 puts it in this repository; if B1 puts it in Forager, this repository consumes
  it as a published or included build. Option A leaves both open.
- Cost: the root build gains two plugin declarations (`apply false`) and `settings.gradle.kts` gains Google's Maven
  repository (section 2). Both are visible to `:brouter`'s configuration, which is what the before-and-after check below
  is for.

**Option B: create `:domain` (empty) and `:app` now.**
- Pro: the shape Phase B wants exists from day one, and `:app`'s imports are right from the first commit.
- Con: it decides B1's "which repository holds the shared module" by building it here, before the owner hears B1's
  proposal. It is also a module with no code and no reader, which Forager's `CLAUDE.md` advises against ("Don't build
  speculative ... logic"). Rejected for A1 for those two reasons.

**Option C: put the app in `:brouter`, or in the root project.**
- Rejected: it changes the engine's build file and plugin set, which every engine check depends on.

**Option D: an app module per platform from the start (`:iosApp` as Gradle, later `:androidApp`).**
- An Android app module here would duplicate Forager's Android app, which ships from Forager. Nothing for Android is
  built in this repository until B1 is answered. Rejected for A1.

**What an Android target would need later** (none of it in A1):
- For a KMP library module, Google's page (`https://developer.android.com/kotlin/multiplatform/plugin`, fetched today)
  says the supported way is the `com.android.kotlin.multiplatform.library` plugin; using `com.android.library` for KMP
  "depends on Android Gradle Plugin APIs that are deprecated and require opt-in in Android Gradle plugin 9.0 and
  higher", "expected to be removed ... in Android Gradle plugin 10.0"; and "There isn't a direct replacement for
  configuring a Kotlin Multiplatform module using `com.android.application` plugin. To migrate, extract your Android
  application to a separate Gradle module." So an Android target on `:app`, `:brouter` or a shared module would be an
  `android { }` block from that plugin, and the Android application stays its own module (in Forager).
- Kotlin 2.3.10's supported AGP range is `8.2.2–9.0.0` (compatibility guide, section 2) and Forager uses AGP 9.3.1.
  Whether 9.3.1 with Kotlin 2.3.10 works is Forager's fact today (its build does), but it is outside the table.
  Adding an Android target **in this repository** would bring AGP into this build: also outside A1.
- Whether Forager's Android app could consume `:brouter`'s existing `jvm` variant without an Android target (Gradle's
  variant matching between `androidJvm` consumers and `jvm` producers) was not tried and is B1's question; the survey
  also left it "Not verified" (survey `:288`).

**Recommendation (mine, Plumb Ledger's): Option A**, with the module named `:app` in `app/`, the Xcode side in
`iosApp/`, the framework `baseName` a fixed technical name (for example `ForagerApp`, not the public display name;
section 6), and no dependency from `:app` on `:brouter` until Phase C. The before-and-after check below is the evidence
that A1 leaves the engine's build as it was.

**The before-and-after check (run in scratch today, never committed).** Two copies of the tree at `565faa2`
(`git archive HEAD`): `scratchpad/s79/repo` as it is, and `scratchpad/s79/repo2` with Option A applied as A1 would apply
it: `include(":app")`; the probe's `app/` module from section 2; the root `build.gradle.kts` gaining
`alias(libs.plugins.kotlin.compose) apply false` and `alias(libs.plugins.compose.multiplatform) apply false`; the
catalog gaining `composeMultiplatform = "1.12.2"` and those two plugin entries; Google's Maven added to
`dependencyResolutionManagement` with `content { includeGroupByRegex("androidx\\..*") }`. In both copies only the
repository URLs were pointed at the local retrying mirror (section 2). `scratchpad/s79/tools/deps.sh` then printed
`:brouter:dependencies` for six configurations and the root `buildEnvironment`, in each copy.
- `:brouter`'s `iosSimulatorArm64CompileKlibraries`, `iosArm64CompileKlibraries`, `jvmCompileClasspath`,
  `jvmRuntimeClasspath` and `jvmTestRuntimeClasspath`: **byte-identical before and after** (`diff` prints nothing).
  Each is a real tree, not an empty report: for example the iOS one lists okio 3.18.2, atomicfu 0.33.0 and
  `kotlin-stdlib:2.3.10`, and the jvmTest one also kotlin-test 2.3.10, junit 4.13.2 and jacoco 0.8.16 with asm 9.11.1.
  The oracle jar is a file dependency and does not appear in this report in either copy, so this check says nothing
  about it; it is unchanged by construction because `brouter/build.gradle.kts` is not edited.
- The sixth, `commonMainMetadataElements`, is a consumable configuration and failed to resolve identically in both
  copies; it carries no evidence either way.
- Root `buildEnvironment`: the after copy adds five coordinates (the Compose Gradle plugin and its marker 1.12.2, its
  `hot-reload-gradle-plugin` 1.2.0, the Compose compiler plugin and its marker 2.3.10) and changes or removes none;
  every `kotlin-gradle-plugin*` entry stays 2.3.10.
- In the after copy, `./gradlew --no-daemon :app:compileKotlinIosSimulatorArm64 :brouter:compileKotlinIosSimulatorArm64
  :brouter:compileKotlinJvm` ended `BUILD SUCCESSFUL` (`scratchpad/s79/repo2-compile.log`).
- Not run: any of `:brouter`'s tests in the after copy (they need the oracle, tiles and the msun build; they are CI's
  job on A1's first push). So "A1 leaves the engine's resolved inputs as they were" is shown; "the engine's results are
  unchanged" is predicted, and A1's first CI run on the four existing jobs is what shows it.

## 4. An Xcode project with no Mac

Nobody can open Xcode, so whatever is committed is edited as text and judged only by what the runner does with it.
Unsigned simulator builds only (the owner: "When a signed build is wanted (Recommended)", -153).

**What the runner has** (Readme of `macos-26-arm64`, fetched today, section 2): Xcode 26.0.1 to 26.6, Ruby 3.4.12,
CocoaPods 1.17.0, Homebrew 7.0.11. **Neither XcodeGen nor Tuist is on the image**: `grep -ciE "xcodegen|tuist"` over the
Readme prints `0`. Either would be installed by the job.

**What Kotlin documents for joining the framework to an Xcode project** ("direct integration",
`https://kotlinlang.org/docs/multiplatform/multiplatform-direct-integration.html`, fetched today): a Run Script build
phase, placed before Compile Sources, running `cd "<root>"` then `./gradlew :<module>:embedAndSignAppleFrameworkForXcode`;
User Script Sandboxing disabled; "Based on dependency analysis" off for that phase. Every option below can use it, or
can instead have the job run Gradle's `linkDebugFrameworkIosSimulatorArm64` first and point the Xcode build at the
framework with `FRAMEWORK_SEARCH_PATHS` and `OTHER_LDFLAGS` (the second way is not on that page; inferred from how
Xcode links a framework).

**Option 1: a `project.pbxproj` written by hand and committed** (`iosApp/ForagerApp.xcodeproj/project.pbxproj`, plus
Info.plist and the Swift entry).
- Committed: an old-style plist of a few hundred lines with 24-hex-digit object IDs that cross-reference each other
  (inferred size; a starting point could be copied from JetBrains' project wizard, which ships one, not fetched today).
- Runs on the runner: `xcodebuild` only. Pinned: by itself; nothing to install.
- Fragile: every later change (a file, a resource, a build setting, a capability for location in Phase F) is a
  hand edit of cross-referenced IDs that nobody can check in Xcode; a wrong ID shows up only as an `xcodebuild` parse
  or "missing file" error on the runner, about 10 minutes per try (section 5). Reviewers read a diff of IDs, not intent.

**Option 2: a spec committed, the project generated on the runner by XcodeGen.**
- Committed: `iosApp/project.yml` (readable YAML: target, sources, Info.plist keys, build settings, the Run Script
  phase), the Swift entry, and a pinned install line. The generated `.xcodeproj` is not committed (or is uploaded as
  an artifact so a reviewer can read it).
- Runs on the runner: download the release zip, check it, `xcodegen generate --spec iosApp/project.yml`, then
  `xcodebuild`.
- Pinned how: XcodeGen's releases page (`https://github.com/yonaskolb/XcodeGen/releases`, fetched today) shows
  **2.47.0 released 17 Nov 2026** (yesterday, "Latest") and **2.46.0 released 16 Jul 2026**. I downloaded
  `https://github.com/yonaskolb/XcodeGen/releases/download/2.46.0/xcodegen.zip` today: 4,278,764 bytes, SHA-256
  `4d9e34b62172d645eed6457cac13fc222569974098ef4ee9c3368bedf0196806`, containing `xcodegen/bin/xcodegen`, which `file`
  reports as a "Mach-O universal binary with 2 architectures" (x86_64 and arm64). It cannot run here (Linux). The
  job would fetch that URL and refuse it unless size and SHA-256 match, as the repo's `tools/fetch-pinned.sh` already
  does for every other fetched input. `brew install xcodegen` is not pinnable to a version and is not proposed.
  The README (`https://github.com/yonaskolb/XcodeGen`, fetched today) asks for "the latest stable (non-beta) version of
  Xcode"; the job selects 26.0.1, not the image's newest (26.6). Whether 2.46.0 generates a project 26.0.1 opens is
  shown only on the runner.
- Fragile: one more tool and a download from github.com per job; a new XcodeGen version is a pin change, by choice.
  The spec is short and its errors are XcodeGen's validation messages, before `xcodebuild` runs (inferred from the tool's
  purpose, not seen).

**Option 3: a spec committed, the project generated on the runner by Tuist.**
- Committed: `Project.swift` (a Swift manifest) and a pinned Tuist version.
- Runs on the runner: install Tuist, `tuist generate` (it compiles the Swift manifest first), then `xcodebuild`.
- Pinned how: Tuist's releases page (`https://github.com/tuist/tuist/releases`, fetched today) shows only
  `4.219.0-canary.15` to `.21`, all "Pre-release", all 17 Nov; a stable tag was not on the first page and was not
  looked for further. Whether `generate` needs an account was not stated on the page fetched.
- Fragile: a larger tool, a fast-moving release stream, and a Swift manifest a no-Xcode team edits blind. Nothing it
  does is needed by A1 that Option 2 does not do.

**Option 4: no Xcode project; the `.app` bundle assembled by hand.**
- Kotlin/Native can link an iOS executable itself (`binaries.executable()`), and a simulator `.app` is a directory
  holding the executable and an `Info.plist` (plus an ad-hoc signature, which `codesign -s -` gives without an account).
  The entry point would be Kotlin: a `main` calling `UIApplicationMain` with an app delegate class written in Kotlin.
- Committed: Info.plist and a few Gradle tasks; nothing for Xcode. Runs on the runner: Gradle, `codesign`, `simctl`.
- Fragile: off every path JetBrains documents for Compose Multiplatform, so each later need (Compose resources
  bundled into the app, asset catalogs, launch screen, entitlements, and the signed build the owner will one day want)
  is solved by this project alone. None of this was tried; all of it is inferred. Not recommended.

**Recommendation (mine): Option 2, XcodeGen 2.46.0, pinned by URL, size and SHA-256 as above,** with the documented
Run Script phase (`embedAndSignAppleFrameworkForXcode`) so the build follows the path Kotlin documents, and
`ENABLE_USER_SCRIPT_SANDBOXING = NO`, `CODE_SIGNING_ALLOWED = NO` in the spec. Why 2.46.0 and not 2.47.0: 2.47.0 is one
day old today; 2.46.0 has had four months. Either is a one-line pin change. The spec and the Swift entry (a SwiftUI
`App` hosting `MainViewController()` through `UIViewControllerRepresentable`, about 20 lines, the wizard's shape; not
fetched today) are the only iOS-side files A1 commits.

## 5. Launch and screenshot on the simulator in CI

Nothing in this section has run on a Mac. The command names are Apple's (`xcodebuild`, `xcrun simctl`); their exact
flags are written from the tools' usage as I know it and were **not** checked against a `--help` on a runner today
(none is reachable from here), so the first dispatch prints `xcrun simctl help` for each subcommand it uses before
relying on it. Every result is "simulator-checked".

**The job, in order** (a new workflow file, section 7, decision T5; same runner label and Xcode as `ios-smoke`):
```
# 0. checkout, setup-java (Temurin 21.0.13+9, as brouter.yml), select Xcode 26.0.1, print xcodebuild -version
sudo xcode-select -s /Applications/Xcode_26.0.1.app
# 1. XcodeGen, pinned (section 4); refuse unless size and SHA-256 match
bash tools/fetch-pinned.sh build/xcodegen/xcodegen-2.46.0.zip \
  https://github.com/yonaskolb/XcodeGen/releases/download/2.46.0/xcodegen.zip 4278764 <the SHA-256 in section 4>
unzip -q -o build/xcodegen/xcodegen-2.46.0.zip -d build/xcodegen
build/xcodegen/xcodegen/bin/xcodegen generate --spec iosApp/project.yml
# 2. build the app for the simulator, unsigned; the Run Script phase runs :app:embedAndSignAppleFrameworkForXcode
xcodebuild -project iosApp/ForagerApp.xcodeproj -scheme ForagerApp -configuration Debug \
  -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' -derivedDataPath build/xcode \
  ARCHS=arm64 CODE_SIGNING_ALLOWED=NO build
APP=build/xcode/Build/Products/Debug-iphonesimulator/ForagerApp.app
# 3. a device of our own, named runtime and device type, so the image's defaults cannot move it
UDID=$(xcrun simctl create a1-sim "iPhone 17" com.apple.CoreSimulator.SimRuntime.iOS-26-2)
xcrun simctl boot "$UDID"; xcrun simctl bootstatus "$UDID" -b
# 4. the control: a screenshot before the app is installed must FAIL the colour check
xcrun simctl io "$UDID" screenshot build/a1/before.png
# 5. install, launch with the app's stdout captured, wait for the app's own line (bounded)
xcrun simctl install "$UDID" "$APP"
xcrun simctl launch --terminate-running-process --stdout=build/a1/app-stdout.txt "$UDID" <bundle id>
timeout 120 bash -c 'until grep -q "^A1: first frame drawn" build/a1/app-stdout.txt; do sleep 1; done'
# 6. the screenshot, and the checks that can fail
xcrun simctl io "$UDID" screenshot build/a1/after.png
```
`tools/fetch-pinned.sh` takes `<dest> <url> <size> <sha256>` (its usage line, `tools/fetch-pinned.sh:6`). I ran it
here today on the zip downloaded in section 4, through a `file://` URL: with the pins above it printed "matches its
pins (4278764 bytes, SHA-256 4d9e34b6...6806)" and exited 0; with a SHA-256 of zeros it printed "REFUSED ... has
SHA-256 4d9e34b6...6806, pinned 0000..." and exited 1, leaving no file at the destination. The runtime
identifier `com.apple.CoreSimulator.SimRuntime.iOS-26-2` and the device name `iPhone 17` are both printed
in the `ios-smoke` log of section 1 (log 229 and 246), so they exist on today's image.

**How the check is made able to fail.** A screenshot alone is a check that cannot fail: a crashed app leaves the home
screen, and a blank app leaves a white or black window, and both are valid PNGs. Three things, each of which turns red
on its own:
1. **A line the app logs only after drawing.** `App()` draws a full-screen `Box` in one fixed colour and, from the
   draw phase (a `drawBehind` on that box, guarded so it prints once), prints `A1: first frame drawn`. Printing from
   the draw phase, not from composition, means the line exists only if Compose reached the GPU-backed draw (inferred
   from Compose's phases; the A1 run shows whether stdout from `println` in a Kotlin/Native app reaches
   `simctl launch --stdout`, which is **not verified**; if it does not, `NSLog` through `platform.Foundation` and
   `xcrun simctl spawn "$UDID" log show --predicate ...` is the fallback). Step 5 fails if the line does not come within
   120 s.
2. **The colour, read from the PNG's pixels.** A check reads `after.png` and requires that at least 98 % of the pixels
   in the middle 80 % of the screen's height (clear of the status bar and the home indicator) are within a tolerance of
   the app's colour, and prints the three commonest colours it saw. I wrote it as a prototype in scratch
   (`scratchpad/s79/png/png_region.py`, stdlib `zlib` only, 8-bit RGB and RGBA, no interlace) and ran it today on
   synthetic PNGs: a one-colour image passes (`24960/24960`); a white one fails (`0/24960`, exit 1); a half-coloured one
   fails (half the pixels, exit 1); a colour two units off passes at tolerance 4 and fails at tolerance 1. On a 1206 × 2622
   PNG saved by Pillow 12.3.0 with filter types 1, 2 and 4 in its rows, it read 99.98 % and passed, and a grey "home
   screen" of the same size failed. Filter type 3 was not exercised, and 1206 × 2622 is a test size of my choosing,
   not a measured screenshot size. **The colour**: a saturated, non-system colour, chosen so neither the home screen nor
   a blank window can match it. **The tolerance is not guessed**: a simulator screenshot may be tagged with a display
   colour profile and its stored values may differ from the sRGB value the app asked for (inferred, not seen), so the
   first run prints the commonest colour it read, and the tolerance is set from that one observed value, recorded with
   it.
3. **The control in the same job.** Step 4's `before.png` (the home screen, no app) is run through the same check and
   the job fails unless that check fails. So a checker that passes everything turns the job red, every run.
Also printed, not gating: `xcrun simctl listapps` (or `get_app_container`) showing the bundle installed, and whether a
crash report for the app's process exists under the runner's `~/Library/Logs/DiagnosticReports`.

**Where the screenshot goes.** `actions/upload-artifact`, pinned to the SHA `brouter.yml` already uses
(`cf430e030ddbb5b0abf93d22962f4752f3646cd9`, v7.0.2), artifact `a1-simulator`, holding `before.png`, `after.png`,
`app-stdout.txt` and the generated `.xcodeproj` (so the project nobody can open can at least be read), with `if:
always()`. Nothing there is committed. E1 ("no coordinates are committed") is not touched by an empty app's
screenshot; it will be by A2's map screenshots, which show places (flagged in section 6).

**Expected job time** (inferred from section 1's measured steps, not measured on an app): checkout, Java and Xcode
select about 25 s; XcodeGen fetch and generate under 10 s; Kotlin/Native download about 70 s, plus compiling and linking
Compose Multiplatform into a framework, not measured (the Linux klib compile alone took 1 min 19 s in scratch, and
linking with Skia is the slow part; my estimate 2 to 5 min); the Xcode build of a 20-line Swift app about 1 min;
`simctl create`, `boot` and `bootstatus` 30 to 90 s; install, launch and screenshots under 30 s. **About 6 to 10
minutes in all**, timeout proposed at 30. A cache of `~/.konan` and `~/.gradle` would cut the first two items; it is a
decision (section 7), not a default.

6. What needs the owner - pending
7. Which decisions are technical - pending
8. The order of work for A1 - pending
9. What A1 will not show - pending
