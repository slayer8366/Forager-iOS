# App port survey: method and predictions (committed before any Forager source is read)

Dispatch 2026-11-12-01, from the planner. Researcher: Madrone Gauge. Read-only survey of the Forager Android
app as input for planning the iOS app port.

## What had been read when this was written

- This repository's `CLAUDE.md` and `docs/process/researcher-profile.md`, and `RECORD.md` lines 53 to 79.
- From the export of Forager `pre-main` at `12eb9697`: `CLAUDE.md` lines 251 to 259,
  `docs/process/researcher-profile.md`, and the whole of
  `docs/audits/2026-10-26-ios-multiplatform-feasibility-report.md` (415 lines, the baseline).
- Nothing else. No file under `app/`, `third_party/`, `server/` or any build file had been opened.

## Method

1. **Source.** The export only. No fetch from and no write to slayer8366/Forager. No build is run. The export
   has no git history, so "what changed" is compared by content against the baseline report's own figures.
2. **Counting.** `find`, `wc -l`, `grep -c`, `grep -l` over the export. Each count's command is stored in this
   folder beside its output. A count that the baseline made with a compiler ("66 files fail") cannot be
   re-run by an import scan, and is reported as "not re-run", with the nearest scan figure given as a scan
   figure only. The baseline itself showed that an import scan misses `Math`, `String.format`, `System` and
   `synchronized`, so scan figures for "free of Android" are upper bounds.
3. **Reading.** A file is read by `grep -n` and then a window of at most 80 lines. No whole source file is
   printed. Forager's `RECORD.md` is read only by `grep -n` and at most 40 lines around a hit.
4. **Android-bound, by API.** For each API family, a fixed import pattern is grepped over `app/src/main`; the
   files are counted and grouped by top-level package. Patterns are stored in the evidence folder.
5. **Routing.** Every file under the export containing `btools.` is listed. Each main-source caller is read at
   the call sites. The port's public surface is read from this repository's branch `brouter-port-5b`
   (`git show`, read-only, nothing checked out).
6. **External facts.** Fetched from the vendor's own page where the network allows; the URL and the date are
   given. A fact that could not be fetched is marked [?] and listed under "Not verified". No version number
   is given from memory.
7. **Marks.** [R] read at source (file, line). [C] counted by a named command. [I] inferred. [?] not verified.
8. **The Restricted Object.** Not read, not followed, not named. If a path leads into it, the survey stops
   there and says so.
9. **No coordinates.** Fixtures are cited by file path only.
10. **Corrections.** Any figure corrected after first being written is shown with before and after.

## Predictions (made before reading; scored in the report)

| Q | Prediction |
|---|---|
| 1a | Gradle modules: **2** (`:app` and `:brouter`). |
| 1b | User-facing screens or surfaces (top-level destinations, not dialogs): **14**. |
| 1c | Kotlin under `app/src/main`: **640 files, 105,000 lines**. |
| 2a | `domain/` has grown from 227 files and 16,871 lines to about **255 files, 19,500 lines**. |
| 2b | Files importing `org.maplibre.android`: **32** (was 30). |
| 2c | The baseline's Android-bound table misses at least one API family the app uses: **yes**. |
| 2d | New since the baseline besides `domain/nextturn/`: **3** other new packages. |
| 3a | The map SDK is MapLibre Native for Android: **yes**. |
| 3b | A Google Play service is a dependency (fused location): **yes**. |
| 3c | A Java-only library other than BRouter is used in main code: **yes** (the MGRS library). |
| 4a | Main-source files that import `btools.`: **3**. |
| 4b | The port's surface already covers the route-ways reader and the snap-to-network call: **no**. |
| 4c | Both known adapter requirements are visible at source, and at least **2** more are found. |
| 5a | A signed build or TestFlight is possible with no paid Apple account: **no**. Price: **99 USD a year**. |
| 5b | A simulator build and simulator UI tests need no Apple account: **yes**. |
| 6a | Android-bound main files (import `android.` or `androidx.` outside Compose, MapLibre, CameraX): **330**. |
| 6b | Domain files with an `android` import: **0 to 3**. |
| 6c | Test files: **760**, `@Test`: **5,600**; instrumented test files: **25**; a screenshot-test framework: **no**. |
