# Request to Forager's planner: move Kotlin to the 2.4 patch release the owner ruled

From the Forager-iOS planner, 2026-11-19. This file lives in Forager-iOS on purpose: nothing for the iOS
app is written to slayer8366/Forager from this side. The owner hands it over. It is a request, not a
dispatch, and Forager's planner decides how Forager's work is done under Forager's rules.

## The owner's words

- On the map wrapper for iOS: "Move Kotlin to 2.4.20".
- On the version, after the pre-build showed a later patch release exists: "2.4.21".
- On the order: "Both on branches, one merge word".
- On who does Forager's part: "Forager's own planner and coder".

## What is asked

Move Forager's Kotlin pin from its current 2.3 version to the ruled 2.4 patch release, on a branch,
proven green by Forager's own CI and review, and **not merged** until the owner gives one word that
covers both repositories.

## What the iOS side found, as premises for Forager to check, not facts

Read at `origin/pre-main` `f29db48` on 2026-11-18. Forager may have moved since.

- `gradle/libs.versions.toml` line 7 pins Kotlin; line 48 pins KSP to the same number; the comment at
  line 46 says KSP tracks Kotlin. That cannot hold after the move: Maven Central lists no KSP 2.4
  release, the newest being a 2.3 patch. Verified by the iOS planner from Maven Central's listing.
- The iOS coder's reading, not verified by the iOS planner: the compose and serialization plugins
  follow the Kotlin pin; JetBrains' compatibility table puts Forager's Android Gradle plugin and Gradle
  versions inside the published range for the new Kotlin; Room depends on an older
  `kotlin-metadata-jvm` and is the likeliest point of friction; the vendored BRouter is plain Java and
  is untouched; CI names no Kotlin version.
- A scratch probe of Room through KSP on plain Kotlin/JVM built on three compiler and KSP pairings
  with byte-identical generated code. It ran on the earlier 2.4 patch release, not the ruled one, and
  it was not an Android build: there is no Android SDK in the iOS container.
- Nothing here shows Forager's app building, passing its tests, or starting on a phone.

The full reasoning is in Forager-iOS, branch `app`,
`docs/audits/2026-11-19-app-phase-a2/kotlin-2-4-move-prebuild.md`, section 2.

## What the iOS side does meanwhile

Moves its own pin on a branch, compares the engine's four CI jobs against a baseline on the parent
commit, and merges nothing. It reports to the owner when its branch is proven or when the comparison
shows a difference.
