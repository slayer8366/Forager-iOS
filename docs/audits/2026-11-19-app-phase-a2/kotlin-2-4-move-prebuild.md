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
  Kotlin 2.4.20, with that module's metadata at 2.4.0, despite `kotlin-metadata-jvm 2.2.0` in its POM. Whether that
  reader was on the processor path at all was not determined (a dependency listing of the KSP classpath did not finish
  in time) **[?]**. **What it does not show:** AGP 9.3.1 with `kotlin-android` 2.4.20 and `builtInKotlin=false`/`newDsl=false`;
  Forager's own entities, DAOs and `MigrationTestHelper` tests; the Compose compiler on Forager's screens; D8; Robolectric;
  lint. Those are Forager CI's to show (section 5).
