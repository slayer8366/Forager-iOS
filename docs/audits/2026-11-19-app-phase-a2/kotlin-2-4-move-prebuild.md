# Kotlin 2.4.20 move: pre-build report, before any pin moves

Plumb Ledger, session 88, written 2026-11-18 (UTC) for the A2 folder dated 2026-11-19. Branch `app`. A report only:
no build file and no workflow changes in either repository. Throwaway experiments ran in the session's scratch folder
(`s88/`) and are not committed.

**The ruling this serves, verbatim.** Asked how the map should be tried, given that MapLibre Compose 0.20.0 needs Kotlin
2.4.20 and our pin is 2.3.10, the owner chose: "Move Kotlin to 2.4.20". The option's text as shown to them: "Follows the
plan with the current wrapper. Kotlin changes in Forager's Android app and here together, and the routing engine's
bit-for-bit tests must be rerun on the new compiler." It is decided and is not reopened here. Where something below
bears on it, the ruling is quoted beside the finding and the decision is left to the owner.

Other rulings carried, verbatim, not reopened: "Option B stands"; "I don't have a Mac and I don't care to get one";
"3 no iPhone" (every iOS result is at best **simulator-checked**); unsigned simulator builds only; "no coordinates are
committed" (E1); the map stays on MapLibre.

**Bases, confirmed at start.** Forager-iOS: `git fetch origin`; `git branch --show-current` printed `app`;
`git rev-parse --short HEAD` and `origin/app` both printed `8e747fa`; `git status --short` printed nothing. Forager
(Android), read-only: `git fetch origin`; `origin/pre-main` is **`f29db48a2`**. The local checkout at
`/home/user/Forager` is on another commit (`137b1427c`), so every Android citation below is `git show f29db48:<path>`
with that commit's line numbers. Forager's `CLAUDE.md` was read at `f29db48`; it differs from the checkout's copy in one
bullet ("Main changes only by a promotion from pre-main, with two exceptions the owner has ruled", `CLAUDE.md:310` at
`f29db48`), which matters to section 4.

**Labels.** **[R]** read from a file at a cited line or a URL fetched today (2026-11-18); **[C]** a command run today
in this container, with its log in scratch; **[I]** inferred; **[?]** could not be determined. Versions written as
`0.` followed by six digits (MapLibre Native FFI) are written without the leading `0.`, so the team's E1 scan does not
read them as a coordinate.

## 1. Which Kotlin exactly

### What "2.4.20" is today

- **2.4.20 is a stable release, published 2026-09-07.** Maven Central's
  `org/jetbrains/kotlin/kotlin-gradle-plugin/maven-metadata.xml` (fetched today, `lastUpdated` 2026-11-11) lists
  `2.4.20-Beta1`, `-Beta2`, `-RC`, `-RC2`, `-RC3`, then `2.4.20` with no suffix **[R]**. The `kotlin-stdlib-2.4.20.pom`
  answers `last-modified: Mon, 07 Sep 2026 08:24:28 GMT` **[R]**.
- **The newest stable in the 2.4 line is 2.4.21,** published 2026-10-08 (`kotlin-stdlib-2.4.21.pom`,
  `last-modified: Thu, 08 Oct 2026 07:57:54 GMT`) **[R]**. The same metadata lists `2.4.21-RC` and `2.4.21` and no
  later 2.4.x **[R]**. JetBrains' compatibility guide says "The current Stable version of Kotlin is 2.4.21"
  (`kotlinlang.org/docs/multiplatform/multiplatform-compatibility-guide.html`, fetched today) **[R]**.
- **Ahead of it: 2.5.0-RC** (published 2026-11-11, `last-modified` of its stdlib POM), which the metadata names as
  `<latest>` and, oddly, `<release>` **[R]**. It is a release candidate, not stable, and is not considered here.
- JetBrains' published compatibility row covers the two patch releases together: **Kotlin 2.4.20–2.4.21: Gradle
  7.6.3–9.7.0, Android Gradle plugin 8.5.2–9.3.1, Xcode 26.4** (same page, "Version compatibility") **[R]**. The row
  for **2.3.10**, our pin and Forager's, is **Gradle 7.6.3–9.0.0, AGP 8.2.2–9.0.0, Xcode 26.0** **[R]**, unchanged from
  what Phase 1 read on 2026-10-29 (`docs/audits/2026-10-28-brouter-port-verify.md:1170-1172` at `8e747fa`).

### What wrapper 0.20.0 requires, from its published files

- **Its Gradle module metadata requires `kotlin-stdlib` 2.4.20.** `maplibre-compose-iossimulatorarm64-0.20.0.module`
  on Maven Central (fetched today, 11,342 bytes; the 1-byte copy in session 86's scratch was a proxy failure), variant
  `iosSimulatorArm64ApiElements-published`: `org.jetbrains.kotlin:kotlin-stdlib` `{"requires": "2.4.20"}` **[R]**. The
  common module `maplibre-compose-0.20.0.module` says the same, and was written by Gradle 9.7.1 (`createdBy`) **[R]**.
  Its other requirements, same variant: Compose `foundation` and `components-resources` 1.12.1, `atomicfu` 0.33.0,
  `kotlinx-coroutines-core` 1.11.0, `kotlinx-io-core` 0.9.1, `lifecycle-runtime-compose` 2.11.0 (JetBrains), spatialk
  `geojson` and `units` 0.8.0, `htmlconverter` 1.1.1, and MapLibre Native FFI and its Metal runtime at `202610.0` **[R]**.
- **Its klib's manifest** (`maplibre-compose-iosSimulatorArm64Main-0.20.0.klib`, 2,671,425 bytes, SHA-256
  `96b5b826...c3eefd`, from session 86's Gradle cache; size equal to the `.module`'s `size` field) reads
  `abi_version=2.4.0`, `metadata_version=2.4.0`, `compiler_version=2.4.20` **[C: `unzip` of `default/manifest`]**.
- **So the hard floor is two-part.** The klib ABI 2.4.0 means a Kotlin/Native compiler of the 2.4 line can read it
  (2.3.10 cannot: below); the module metadata's `requires 2.4.20` means Gradle resolves `kotlin-stdlib` to at least
  2.4.20, and a build whose compiler is older than its stdlib is what Phase 1 called shipping "a newer stdlib than its
  compiler, by nobody's choice" (`2026-10-28-brouter-port-verify.md:562-565`). **2.4.20 is the lowest compiler that
  meets both without a mismatch [I, from the two files; 2.4.0 and 2.4.10 were not tried].** 2.4.21 meets both too
  **[I: same reasoning; tried below in section 3 only if time allowed, see there]**.
- **The error seen with our 2.3.10** (session 86, `:app:compileKotlinIosSimulatorArm64` with the wrapper added; log
  `s86/exp-020f.log:33-36` in scratch, re-read today) **[C, session 86]**:
  `w: KLIB resolver: Skipping '.../maplibre-compose-iosSimulatorArm64Main-0.20.0.klib' having incompatible ABI version
  '2.4.0'. The library was produced by '2.4.20' compiler.` then `e: KLIB resolver: Could not find
  ".../maplibre-compose-iosSimulatorArm64Main-0.20.0.klib" in [.../kotlin-native-prebuilt-linux-x86_64-2.3.10/klib/common,
  ...]`, `BUILD FAILED`.

### 2.4.20 or 2.4.21: a question for the owner, not a substitution

The ruling says "Move Kotlin to 2.4.20". A later patch, **2.4.21**, exists and is the line's newest stable (above).
Both meet the wrapper (above) and share one JetBrains compatibility row (Gradle, AGP and Xcode alike). What 2.4.21
changes over 2.4.20 was **not read** (its changelog is on GitHub, which this session cannot fetch: `api.github.com` and
`github.com/JetBrains/kotlin/releases` answered 403 for repositories not attached to the session) **[?]**. Nothing found
today makes 2.4.20 unusable. This report therefore plans for **2.4.20 exactly**, as ruled, and lists the patch as an
owner question in section 6 (first option: 2.4.20, as said).

## 2. Forager's side, read from `origin/pre-main` (`f29db48`)

Nothing here changes Forager. This repository's `CLAUDE.md` says "Nothing for the iOS app is written to Forager's record or
to slayer8366/Forager"; who makes Forager's change is the owner's to decide (section 4).

### Every place the Kotlin version reaches

Forager's build has two modules: `:app` and `:brouter` (`settings.gradle.kts:17-22`) **[R]**. No other Gradle build is
in the tree (`git ls-tree -r` lists `build.gradle.kts`, `app/build.gradle.kts`, `third_party/brouter/build.gradle.kts`,
`gradle/older-sdk-tests.gradle.kts` and the catalog) **[R]**.

| Where | Today | What Kotlin 2.4.20 asks of it (primary source, fetched today) |
|---|---|---|
| The pin, `gradle/libs.versions.toml:7` | `kotlin = "2.3.10"`; its comment (`:3-6`) records why it left 2.2.20: the classic `kotlin-android` plugin at 2.2.20 "fails to apply against AGP 9.3.1's newer extension type hierarchy" | becomes `2.4.20`. Nothing in the comment ties the pin to 2.3.10 specifically; it says 2.3.10 "applies cleanly" **[R]**. |
| Plugins on `version.ref = "kotlin"`, `libs.versions.toml:131-133` | `kotlin-android`, `kotlin-compose` (the Compose compiler plugin), `kotlin-serialization` | move with the pin automatically. The Compose compiler has shipped inside Kotlin since 2.0, so it has no version of its own to choose **[R: catalog; I: Compose compiler packaging]**. |
| Android Gradle plugin, `:2` | `9.3.1` | **inside** JetBrains' range for 2.4.20–2.4.21, AGP 8.5.2–9.3.1 **[R: kotlinlang compatibility guide]**. Today it is **outside** 2.3.10's range (8.2.2–9.0.0), as Phase 1 found (`2026-10-28-brouter-port-verify.md:1170`). The move brings Forager into a published pair; it does not force an AGP change. |
| Gradle wrapper, `gradle/wrapper/gradle-wrapper.properties:3` | `9.7.0` (no `distributionSha256Sum`) | inside 7.6.3–9.7.0 for 2.4.20 **[R]**, at its top. Outside 2.3.10's 7.6.3–9.0.0 today. No change forced. |
| KSP, `:46-48`, plugin at `:134` | `ksp = "2.3.10"`, with the comment "KSP2's versioning tracks Kotlin directly ... so this stays equal to the `kotlin` version above" | **The comment's rule cannot be followed at 2.4.20: no KSP 2.4.x exists.** Maven Central's `symbol-processing-gradle-plugin/maven-metadata.xml` (fetched today, `lastUpdated` 2026-11-13) lists `2.3.0` to `2.3.14` and nothing above **[R]**. JetBrains' own KSP quickstart pairs **Kotlin 2.4.21 with KSP 2.3.12** (`kotlinlang.org/docs/ksp-quickstart.html`, fetched today) **[R]**. So KSP 2.3.x is versioned on its own line, and "2.3.10 = 2.3.10" was a coincidence the comment read as a rule (inferred from the two sources; KSP's own release notes are on GitHub, not reachable from this session **[?]**). Forager's `STATUS.md:340` already showed the two diverging (KSP "latest 2.3.11" beside Kotlin 2.3.10) **[R]**. Which KSP versions support KGP 2.4.20 is not stated in any source reachable here; the scratch probe below tried 2.3.10 and 2.3.14. **This is a decision Forager's planner has to make** (section 6, T-F1). |
| `gradle.properties:9`, `:13` | `android.builtInKotlin=false` (KSP needs the standalone plugin), `android.newDsl=false` | unchanged by the move as far as read; whether KGP 2.4.20's `kotlin-android` still accepts `newDsl=false` against AGP 9.3.1 was **not tested** (no Android SDK here, below) **[?]**. Phase 1's Android probe saw both flags print deprecation warnings with 2.3.10 (`2026-10-28-brouter-port-verify.md:584-585`). |
| Compiler options, `app/build.gradle.kts:527-531` | `jvmTarget` 17 only | no `languageVersion`, `apiVersion` or `coreLibrariesVersion` anywhere (`git grep` over `*.kts` and `gradle.properties` finds none) **[R]**, so the language level moves to 2.4 with the compiler. Java `compileOptions` 17 (`:315-318`) is unaffected. |
| Room, `:45`; `ksp(libs.androidx.room.compiler)` at `app/build.gradle.kts:568`, `kspTest` at `:601` | `room = "2.8.4"` | **the most likely friction.** `room-compiler-processing-2.8.4.pom` (Google's Maven, fetched today) depends on `kotlin-metadata-jvm` **2.2.0** at runtime and `symbol-processing-api` `2.0.10-1.0.24` **[R]**. Kotlin 2.4.20 writes class metadata version 2.4.0 (the wrapper's klib above reads `metadata_version=2.4.0`; the JVM side is inferred to match) **[I]**. A 2.2.0 metadata reader is two minor versions behind that. 2.8.5 (Google's Maven, `last-modified` 2026-09-09) has the same `kotlin-metadata-jvm 2.2.0` **[R]**; no Room 2.9 is listed **[R: `room-compiler/maven-metadata.xml`]**. The probe below is the evidence. |
| `kotlinx-serialization-json`, `:22` | `1.11.0` | its changelog: "1.11.0 / 2026-04-10 ... based on Kotlin 2.3.20"; 1.12.0 (2026-10-19) and 1.13.0 (2026-11-10) are "based on Kotlin 2.4.10" (`raw.githubusercontent.com/Kotlin/kotlinx.serialization/master/CHANGELOG.md`, fetched today) **[R]**. A runtime built on an older Kotlin is readable by a newer compiler **[I]**; whether the 2.4.20 serialization plugin's generated code needs anything 1.11.0 lacks was **not tested** **[?]**. Not forced to move by any source read. |
| `kotlinx-coroutines`, `:24`, `:31` | `1.11.0` | "Kotlin was updated to 2.2.20" (`kotlinx.coroutines` `CHANGES.md`, Version 1.11.0, fetched today) **[R]**. Older than the compiler; not forced to move **[I]**. |
| Compose BOM `2026.08.00` (`:11`), Material3 `1.5.0-alpha26` (`:19`), Robolectric `4.16.1` (`:44`) | Google and Robolectric libraries | no Kotlin-version coupling found in what was read; they are compiled libraries the compiler reads **[I]**. |
| R8 override, root `build.gradle.kts:1-14` | `com.android.tools:r8:9.3.31` on the buildscript classpath, for a dex that ART's verifier rejected on a phone (RECORD -741, `docs/audits/2026-10-08-launch-verifyerror-report.md`) | not version-coupled to Kotlin, but **a compiler change changes the bytecode D8 dexes,** and the last time dexing went wrong here it showed only on a phone, after CI was green. Release is `isMinifyEnabled = false` (`app/build.gradle.kts:307`), so R8 does not shrink or rewrite Kotlin metadata **[R]**. See section 8. |
| The vendored Java BRouter, `third_party/brouter/build.gradle.kts` | a `java-library` compiled by `javac` with `options.release = 11`, "no dependencies beyond the JDK" | **unaffected by the Kotlin compiler.** It contains no Kotlin source (`git ls-tree` lists 101 Java files and no `.kt`) and its build applies no Kotlin plugin **[R]**. Its bytecode comes from the runner's JDK (Temurin 21, `ci.yml:108-112`), which the move does not touch. The Kotlin code that wraps it in `:app` (`BRouterTrailRouter` and the router's tests) is recompiled by the new compiler, so Forager's router tests still need to run; they test the wrapper, not the engine's arithmetic **[I]**. |
| CI, `.github/workflows/ci.yml` | JDK 21 (`:108-112`); `setup-gradle`; Robolectric's cache keyed on `hashFiles('gradle/libs.versions.toml')` (`:127`) | names no Kotlin version **[R]**. The catalog edit changes the Robolectric cache key, so the first run re-downloads the android-all jars **[I]**. The same is true of `play-premain-signed.yml` and `play-production.yml` (JDK 21, no Kotlin) **[R]**. |
| Documents | `STATUS.md:31-32` ("Kotlin 2.3.10, AGP 9.3.1, Gradle wrapper 9.7.0"), `STATUS.md:339-340` (a dated dependency table); audits that record "compiled ... with Kotlin 2.3.10" as provenance (for example `docs/audits/2026-10-06-walks-2026-10-05-estimate-replay-and-clock.md:150`) | the snapshots go stale; the audits are historical records and stay as written **[R]**. No Forager rule pins Kotlin itself: `CLAUDE.md` asks only for exact pins ("Pin dependency versions ... rather than open ranges") **[R]**. |

**What Forager would change, at the least [I, from the table]:** the catalog's `kotlin` to `2.4.20`; `ksp` to a KSP 2.3.x
that works with it, and its comment corrected; Room only if the probe or Forager's build shows it must move.

### What Forager's own rules say about a change like this

- **Where it lands: pre-main.** "Work lands on pre-main, and experiments on dev" (`CLAUDE.md:310` at `f29db48`); "pre-main
  is the working branch. Small tasks merge into it directly. A large feature that isn't experimental is built on its own
  branch, which merges into pre-main when done" (`docs/process/planner-profile.md:85`) **[R]**.
- **What must pass before it reaches the phone build:** "Every app PR gets an independent review by Sorrel before any
  build reaches the owner ... The order: Hazel builds to green → Sorrel reviews → the S22 check for anything visual → a
  build to the owner → a merge on the owner's word" (`planner-profile.md:96`); "Merges happen only on the owner's word"
  (`:97`) **[R]**. CI on pre-main runs `build` (APK and unit tests), `street-removable`, `lint`, `release-compile`,
  `route-cutter` and `no-debug-sources` (`ci.yml:60-767`, job names) **[R]**.
- **Promotion to main:** "CI green on pre-main, at the exact commit promoted; the UX auditor's report against Forager's
  QA philosophy ...; the owner's approval for public use" (`planner-profile.md:76-79`) **[R]**. Neither exception applies:
  the catalog is not under `.github/workflows/` (`:87`) **[R]**. A compiler move is not a database migration, so "the
  upgrade check" (`:83`) is not triggered by its wording, unless Room's generated code changed what is stored **[I]**.
- **Who writes it:** "The planner writes the audit-index rows ... and the record entries. Coders don't" (`:90`) **[R]**,
  under Forager's record, not this one.
- **Base discipline:** "Verify your base branch before you start" and the derived-claim rule ("A planner's picture of the
  repository is a claim about the past") apply to whoever does it (`CLAUDE.md`, Known pitfalls) **[R]**. Every Forager
  line number in this section is a claim about `f29db48` and will decay.

### What ran in scratch for Forager's side

- **No Android SDK in this container.** `ANDROID_HOME` and `ANDROID_SDK_ROOT` are unset, and no `android-sdk` or
  `platforms` directory exists under `/`, `/opt` or `~` to depth 4 **[C]**. Phase 1 had compileSdk 35 here; that
  container is gone. So **no copy of Forager's tree was configured or compiled**: AGP cannot configure `:app` without an
  SDK, and the brief says to stop there. Nothing below is an Android build.
- **Instead, one cheap probe of the riskiest coupling, Room through KSP, on plain Kotlin/JVM** (no Android, so it is
  evidence about Room's processor and KSP under the new compiler, not about AGP). A two-module Gradle build in scratch
  (`s88/roomprobe/`): `:lib` holds a `data class` and an `enum` compiled by the probe's own Kotlin; `:app` holds a Room
  `@Entity` that `@Embedded`s the other module's class and stores its enum through a `@TypeConverter`, a `@Dao` with a
  `Flow<List<…>>` query, two `suspend` functions, and a `@Database` with `exportSchema = true`. Room 2.8.4
  (`room-runtime` and `ksp(room-compiler)`, Forager's version), coroutines 1.11.0, Gradle 9.0.0 (this repository's
  wrapper, not Forager's 9.7.0), JDK 21.0.12, JVM target 17. Each run was `clean :app:kspKotlin :app:compileKotlin`
  **[C: `s88/rp-*.log`]**.

| Run | Kotlin | KSP | Result | `:lib`'s class metadata (`javap -v`, `mv`) |
|---|---|---|---|---|
| control, Forager's pair | 2.3.10 | 2.3.10 | `BUILD SUCCESSFUL`, no `e:` | `[2,3,0]` |
| the move, Forager's KSP kept | 2.4.20 | 2.3.10 | `BUILD SUCCESSFUL`, no `e:` | `[2,4,0]` |
| the move, newest KSP | 2.4.20 | 2.3.14 | `BUILD SUCCESSFUL`, no `e:` | `[2,4,0]` |

- **The generated Room code (`Db_Impl.kt`, `SightingDao_Impl.kt`) and the exported schema (`schemas/p.Db/1.json`) are
  byte-identical across all three runs** (`diff -r`, no output) **[C]**. So in this probe the compiler change did not
  change what Room generates or stores.
- **What it shows:** Room 2.8.4's processor, through KSP 2.3.10 or 2.3.14, accepts sources and a sibling module compiled by
  Kotlin 2.4.20, with that module's metadata at 2.4.0, with `kotlin-metadata-jvm` **2.2.0** on the processor path
  (`:app:dependencies` of the 2.4.20/KSP 2.3.10 run lists `org.jetbrains.kotlin:kotlin-metadata-jvm:2.2.0` under
  `kspKotlinProcessorClasspath`) **[C]**. Whether Room called that reader on this probe's types was not determined
  (KSP hands Room its own model of the classpath) **[?]**, so a Forager type that reaches it could still differ. **What it does not show:** AGP 9.3.1 with `kotlin-android` 2.4.20 and `builtInKotlin=false`/`newDsl=false`;
  Forager's own entities, DAOs and `MigrationTestHelper` tests; the Compose compiler on Forager's screens; D8; Robolectric;
  lint. Those are Forager CI's to show (section 5).

## 3. This repository's side

### a. The engine: what a compiler change can do to bit-for-bit results

The port is held to BRouter 1.7.10's Java release jar, the oracle, on the JVM (`brouter/build.gradle.kts:7-20`), and its
`commonTest` re-runs on the iOS simulator (`brouter.yml`, `ios-smoke`, `ios-memory`). What a compiler can move, by
platform **[I unless marked]**:

- **On the JVM, little, and mostly through the standard library.**
  - Double and float arithmetic is IEEE on any JVM 17+, whatever compiler emitted the bytecode; constant folding at
    compile time uses the same IEEE operations **[I]**.
  - The library maths the port uses is `kotlin.math` `sqrt`, `abs`, `min`, `max`, `exp` and `PI` (imports in
    `commonMain`: `exp` once, at `router/StdPath.kt:264` and `:290`; `sqrt` at four files) **[R: grep at `8e747fa`]**.
    On the JVM these compile to `java.lang.Math` calls, so their bits are the JDK's, pinned to Temurin 21.0.13+9 in CI and
    to fdlibm on x86 by `-XX:-UseLibmIntrinsic` (`brouter/build.gradle.kts:161-168`) **[R]**. The two functions that reach
    a route on Forager's calls, `cos` and `atan2` (RECORD -15's step 1, `origin/main:RECORD.md:512-516`), are the port's
    own bit-copied msun code (`brouter/msun/MsunCos.kt`, `MsunAtan2.kt`), not the standard library's **[R]**.
  - What could move is **pure-Kotlin standard library behaviour** the port leans on: `Regex`, `String` splitting and
    case functions, `roundToLong`/`roundToInt`, number-to-text, and the collections' iteration order (on the JVM,
    `HashMap` is `java.util.HashMap`, Phase 1 B row 18) **[R: Phase 1 B rows 11, 13, 15, 18]**. A new compiler also
    brings a new stdlib (2.4.20, by the catalog's `kotlin-stdlib` resolution, seen in section 3e).
- **On Kotlin/Native (the simulator), more.**
  - **LLVM moves from 19 to 21.** Kotlin/Native 2.3.10's `konan.properties` names
    `llvm.macos_arm64.dev=llvm-19-aarch64-macos-dev-79` (`:787`) and 2.4.20's names `llvm-21-aarch64-macos-dev-97` (`:787`)
    **[C: read from both distributions in `~/.konan`]**. A new optimiser can change instruction selection; the one change
    that would alter bits is **floating-point contraction into fused multiply-add**. Phase 1 found none on arm64 with
    2.3.10, by disassembly with a positive control (RECORD -15 step 2, `origin/main:RECORD.md:524`) **[R]**. Whether
    LLVM 21 keeps that is **not known** until a test that would see it runs on arm64 **[?]**.
  - **The Apple toolchain the distribution expects moves from Xcode 26.0.1 to 26.4:**
    `targetToolchain.macos_arm64-ios_simulator_arm64 = target-toolchain-xcode_26.0.1_17A400` in 2.3.10 against
    `target-toolchain-xcode_26.4_17E192` in 2.4.20 (`konan.properties:216` in each) **[C]**. Its default iOS minimum moves
    from 14.0 to 15.0 (`minVersion.ios`, `:59`) **[C]**; the app's provisional 16.0 is above both.
  - **The Native stdlib's own implementations:** `HashMap` (insertion-ordered on Native, RECORD -15 step 3),
    `String.toDouble()`, `Double.toString()`, `Regex`. All are Kotlin code that ships with the compiler and can change
    between releases **[I]**. `exp` on Native calls the platform's C library, which is the simulator runtime's and not
    the compiler's **[I]**.
- **Which tests would show a difference** (counts from the last green runs, session 87, on `app` before `8e747fa`'s
  docs-only commit; logs in scratch `ci87/`) **[C: JUnit totals printed by the jobs]**:

| `brouter.yml` job | What runs | Counts | What it would catch |
|---|---|---|---|
| `jvm` | `:brouter:jvmTest`, then both iOS klib compiles | **262 tests, 116 files**, 0 failures | any JVM change in a route, cost, node list, failure kind or printed number against the oracle; a JVM-only API in `commonMain` |
| `jvm-production` | `RoutingEngineProductionRecordTest` (2), `RoutingEngineCosRowsProductionRecordTest` (1) | **3** | the production table's per-row lines, recorded |
| `ios-smoke` | `commonTest` on the simulator, gated on 25 named classes (`SIMULATOR_CLASSES`), then the arm64 HotSpot half | **61 tests (30 files)**, then **7** (2 files); all 25 classes ran | Native arithmetic, msun's table and pairs (`MsunCosTableTest`, `MsunAtan2PairsTest`), number text (`FloatTextCommonTest`, `FormatNumberTextTest`), hash order (`NodeHashMapTieBreakTest`), the engine groups' routes on Native |
| `ios-memory` | MP64, ML5, PM5; R's fallback; the stack ladder | **3**, **1**, **1** | the memory-limit failure kind on Native, where hash order matters (RECORD -15 step 3) |

- **What no test covers [I, from the job list]:**
  - **the `iosArm64` (device) binary never runs anywhere.** CI compiles its klib on Linux; only `iosSimulatorArm64` code
    executes. A code-generation difference specific to the device target would not be seen (and with "3 no iPhone" it
    cannot be);
  - routes outside the pinned tiles and generated cases (the corpus is what it is);
  - on the simulator, only the subsets each job names (for example "first 12 cases of each StdModel profile" for R,
    `brouter.yml:225`), not the full JVM corpus;
  - speed and memory as numbers: the timing plan's measurements are not part of these gates.
- **What the Phase 1 report said the pin protects.** Not floating point. Item C pinned Kotlin to "2.3.10 exactly, Forager's
  pin, moved only together with Forager" (`2026-10-28-brouter-port-verify.md:428`) for **Forager's intake of the port**:
  vendored source "must compile under Forager's 2.3.10" (`:501`, `:508`); an unpinned newer compiler "still changes
  Forager" because its POM drags the consumer's stdlib upward (`:562-565`); and a compiler reads binaries one minor newer
  than itself and refuses two (`:557-560`). Its recommendation was "compile the port with 2.3.10, exactly as Forager does.
  Neither side has to move" (`:567`). The record took it as a planner's call (RECORD -24, `origin/main:RECORD.md:950-956`,
  "technical; the owner may overrule"), not an owner ruling **[R]**. Bit-for-bit identity was never argued from the
  compiler version; it is argued from the tests above.

### b. Compose Multiplatform

- **1.12.2 is the newest stable** (`compose-gradle-plugin/maven-metadata.xml`, fetched today: `1.12.0`, `1.12.1`, `1.12.2`,
  then `1.13.0-alpha01` to `1.13.0-rc01`) **[R]**. Its klibs were built by Kotlin **2.3.20**, ABI 2.3.0
  (`runtime-iosSimulatorArm64Main-1.12.2.klib` and `foundation-…-1.12.2.klib` manifests, `compiler_version=2.3.20`,
  `abi_version=2.3.0`) **[C]**, which a 2.4.20 compiler reads (older ABI). The minimum the catalog cites ("Kotlin 2.3 is
  required for native and web platforms", Compose 1.11.0's notes, `CHANGELOG.md:1149`) is met by 2.4.20 too **[R]**. The
  compatibility page: "Compose Multiplatform requires the Compose Compiler Gradle plugin applied with the same version as
  the Kotlin Multiplatform plugin" (`kotlinlang.org/docs/multiplatform/compose-compatibility-and-versioning.html`, fetched
  today) **[R]**; our `kotlin-compose` is on `version.ref = "kotlin"` (`gradle/libs.versions.toml:28`), so it follows.
- **1.13 today: `1.13.0-rc01`,** on Maven Central with `lastUpdated` 2026-11-18 10:09 UTC, still a release candidate
  (`<release>1.13.0-rc01</release>` is Maven's newest, not a stable tag) **[R]**. The changelog on `master` tops out at
  `1.13.0-beta02 (November 2026)` (`CHANGELOG.md:1`) **[R]**. Its alpha02 notes say "Kotlin version has been upgraded to
  2.4.20. The minimal supported Kotlin version for k/native is 2.4.0" (`CHANGELOG.md:193`) **[R]**. Not stable, so not
  proposed.
- **Wrapper 0.20.0 was built against Compose 1.12.1** (`foundation` and `components-resources` `requires 1.12.1` in its
  `.module`) **[R]**. With our 1.12.2, Gradle lifts `foundation` 1.12.1 to 1.12.2 and keeps `components-resources` at
  1.12.1 (section 3e's dependency report) **[C]**. **No Compose move is forced by Kotlin 2.4.20** **[I, from the above
  and the compile in 3e]**.

### c. Other pins here that could move with Kotlin

| Pin (`gradle/libs.versions.toml` at `8e747fa`) | Today | With 2.4.20 |
|---|---|---|
| `okio` 3.18.2 (`:6`) | klib built by 2.2.21, ABI 2.2.0 (`okio-iosSimulatorArm64Main-3.18.2.klib` manifest) **[C]** | read by 2.4.20 in 3e's klib compiles **[C]**; no move forced |
| `atomicfu` 0.33.0 (`:12`) | klib built by 2.2.21, ABI 2.2.0 **[C]**; library only, no plugin | read by 2.4.20 **[C]**; the wrapper requires the same 0.33.0 **[R]** |
| Compose compiler plugin (`:28`) | `version.ref = "kotlin"` | moves with the pin by construction |
| Gradle 9.0.0 (`gradle/wrapper/gradle-wrapper.properties:4`) | inside 2.3.10's 7.6.3–9.0.0, at its top | inside 2.4.20's 7.6.3–9.7.0 **[R]**; no move forced. Forager is on 9.7.0; the port took 9.0.0 to stay in 2.3.10's range (RECORD -24). With 2.4.20, 9.7.0 would also be in range: an option, not a need |
| JDK | Temurin `21.0.13+9` on every job (`brouter.yml:35`, `:100`, `:192`, `:292`; `app.yml:54`, `:76`, `:135`), JVM target 17 (`brouter/build.gradle.kts:113`) | no coupling found **[I]**; the oracle's JDK is a separate pin and stays |
| `jacoco` 0.8.16 (`:9`) | reads `.exec` files against the oracle jar's classes, not the port's | not coupled to Kotlin **[I]** |

### d. Xcode

- **JetBrains' row for 2.4.20–2.4.21 says Xcode 26.4** (compatibility guide, fetched today) **[R]**. The distribution's
  own toolchain is 26.4 (`17E192`, above) **[C]**, its `minimalXcodeVersion` is still 12.5 (`konan.properties:29`) **[C]**,
  and **KGP 2.4.20's `XcodeVersion.maxTested` is 26.4** (`javap -c -p` of `XcodeVersion.class` in
  `kotlin-native-utils-2.4.20.jar`: `bipush 26`, `iconst_4`, `putstatic maxTested`) **[C]**, up from 16.0 in 2.3.10
  (RECORD -14).
- **Ours:** the engine jobs select **Xcode 26.0.1** ("Kotlin 2.3.10's published Xcode is 26.0", `brouter.yml:107-109`,
  `:193-195`); the app jobs select **26.2** (RECORD -208, `app.yml:77-87`, `:137-146`) **[R]**.
- **So with 2.4.20, both are below the published 26.4 and below the toolchain the distribution was built with.** That is
  not a refusal (the minimum is 12.5) and not a "too high" warning (both are under 26.4) **[I, from the properties and the
  bytecode]**. Whether 2.4.20 links and runs the simulator tests with 26.0.1 or 26.2 is **only the Mac runner's to show**
  **[?]**. `macos-26` (the image dated 2026-11-02 in that file) carries **26.4.1** (`17E202`) beside 26.0.1, 26.1.1, 26.2, 26.3, 26.5 and
  26.6 (default) (`actions/runner-images` `images/macos/macos-26-arm64-Readme.md:159-165`, fetched today) **[R]**, so moving
  to the published version is possible on the same label. Which Xcode each workflow selects is a technical choice
  (section 6, T3).

### e. What ran in scratch for this repository

**Setup.** Two copies of `app` at `8e747fa` made by `git archive HEAD` into scratch: `s88/base` (unchanged) and `s88/k24`
(one line changed: `kotlin = "2.3.10"` to `"2.4.20"` in `gradle/libs.versions.toml`; `diff -r` shows nothing else). In
both, `settings.gradle.kts` points at a local retrying mirror of Maven Central, Gradle's plugin portal and Google's Maven
(`s88/tools/mirror.py`, session 86's script, on `127.0.0.1:18088`), for the reason section 3 of the A2 pre-build gives
(intermittent 404s through this container's proxy). No other companion had to change for anything below to build: no
Compose, okio, atomicfu, Gradle or JDK move. Local JDK: OpenJDK 21.0.12.1 (Ubuntu), not CI's Temurin 21.0.13+9, so local
JVM results are compared with each other, base against `k24`, not with CI's.

| Run | Command | Result |
|---|---|---|
| `k24` | `:brouter:compileKotlinJvm :brouter:compileKotlinIosArm64 :brouter:compileKotlinIosSimulatorArm64 :app:compileKotlinIosArm64 :app:compileKotlinIosSimulatorArm64` | **`BUILD SUCCESSFUL in 4m 43s`**, **0 `e:` lines**; all five task headers printed without a suffix (not `SKIPPED`) **[C: `s88/k24-compile.log`]**. Kotlin/Native 2.4.20 for Linux was downloaded through the mirror (`kotlin-native-prebuilt-2.4.20-linux-x86_64.tar.gz`, 219,971,249 bytes) |
| the same, `w:` lines | | three `commonMain` warnings, each printed once per compile task: `BExpressionContextWay.kt:16:10` (parameter names `ab`/`tagValueSet` conflict across supertypes) and `StdPath.kt:52:29`, `:242:37` (parameter named differently from the supertype). **The same three appear at 2.3.10** in CI's `jvm` log (session 87, 3 each) **[C]**, so 2.4.20 adds no `commonMain` warning |
| `k24w` (`k24` with `:brouter` left out, wrapper added) | one catalog line `maplibre-compose` 0.20.0, one `implementation(libs.maplibre.compose)` in `app/build.gradle.kts`, the session 86 probe file (`@Composable fun ProbeMap() { MaplibreMap() }`); `:app:compileKotlinIosSimulatorArm64 :app:compileKotlinIosArm64` | **`BUILD SUCCESSFUL in 1m 58s`**, 0 `e:`, both task headers unsuffixed **[C: `s88/k24w.log`]**. The same probe at 2.3.10 failed with the KLIB resolver error in section 1. Resolution (`:app:dependencies --configuration iosSimulatorArm64CompileKlibraries`): `maplibre-compose:0.20.0`, `maplibre-native-ffi` and `-runtime-metal` at `202610.0` (written without the leading `0.`), `kotlin-stdlib -> 2.4.20`, `foundation:1.12.1 -> 1.12.2`, `components-resources:1.12.1` **[C: `s88/k24w-deps.txt`]** |

**What this shows:** with 2.4.20 alone, the engine and the app compile for every target this repository builds on Linux,
and the current wrapper compiles into `:app`. **What it does not show:** any link, any run, any render (only the Mac
runner can link a framework, run `iosSimulatorArm64Test` or build the app), and anything at Xcode 26.0.1 or 26.2 against a
26.4 distribution (section 3d).

**The JVM tests, base against `k24`:** both copies run `./gradlew --no-daemon :brouter:jvmTest`, as `brouter.yml`'s `jvm`
job does, fetching and checking the oracle and tiles through the build's own tasks. Results are written below when both
runs finish (this section is pushed now because the container restarts often).
