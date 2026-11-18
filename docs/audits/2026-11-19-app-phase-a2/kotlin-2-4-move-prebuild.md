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
