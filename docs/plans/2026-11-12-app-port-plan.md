# The plan for the rest of the port: finishing BRouter, then the app

Written by Cedar Datum, the planner, on 2026-11-12, at the owner's word: "Let's file the plan for the rest of the
port in the repo, and finish where we're at with the BRouter port."

This file is a plan, not a record. Where it and `RECORD.md` disagree, the record wins and this file is wrong. Each
phase below gets its own intent entry in the record before work starts. Entry numbers in brackets are `RECORD.md` IDs
(`2026-10-28-N`, written `-N`).

Marks: **[ruled]** the owner's ruling, with its entry; **[planner]** the planner's technical call, which the owner may
overrule; **[survey]** the researcher's reading in `docs/research/2026-11-12-app-port-survey.md` on the branch
`research/app-port-survey`, not re-checked by the planner unless it says so; **[open]** not decided.

## 1. What is ruled and is not asked again

| Ruling | The owner's words | Entry |
|---|---|---|
| Approach | "Option B stands": Kotlin Multiplatform with Compose Multiplatform | -2 |
| Routing on iOS | "Port BRouter to Kotlin" | -2 |
| Alerts on iOS | "be back by" at the set time, and the full sundown line; the three sundown alerts are not promised | -2 |
| No Mac | "I don't have a Mac and I don't care to get one": builds run on GitHub's macOS runners | -2 |
| No iPhone | "3 no iPhone": every iOS check is a simulator check, reported as "simulator-checked" | -2 |
| The port's maths | "Copy Android's maths (Recommended)" | -24 |
| Via points | "Leave it out (Recommended)" | -24 |
| Hints, formats, heights | "Port it all"; "Port them"; "Add heights at step 8 (Recommended)"; Forager's own next-turn code is ported for iOS too ("Both") | -111 |
| The deep recursion | "Rewrite without recursion (Recommended)" | -144 |
| The recursive code | "Keep the old code safe on a separate branch": `brouter-port-5b-recursive-walk` | -145 |
| The first iOS build | "Map first (Recommended)": the app shell and the map screen | -153 |
| Shared code | "Share the domain now (Recommended)": one shared module, moved piece by piece | -153 |
| Apple Developer Program | "When a signed build is wanted (Recommended)" | -153 |
| The camera | "Out of the first release (Recommended)" | -153 |
| A renderer of our own | "Forget the renderer for now. I'll think about it another time." The map stays on MapLibre | -157 |

The team (-3, -11): the planner; two coders; a researcher; an independent reviewer; a UX auditor; an iOS designer, who
has not been started (-13: "After Phase 1 is fine").

## 2. Part one: finishing the BRouter port

### Where it stands

- Steps 1 to 9 are closed. On the JVM, 2,126 cases equal the Java engine in logic runs (-134) and 1,742 equal it as
  the port ships (-137).
- Step 10, the simulator: 802 cases equal their stored results (-143, -146); the recursion is rewritten and runs in a
  512 KB stack (-146); the JVM suite is fully green (-148, -154); the hints and the four export formats equal the
  Java engine's on 1,445 cases in a logic run (-154).
- The code is on `brouter-port-5b`. It is not merged into `brouter-port` or `main`.

### What is left, in order

| # | Item | State | Whose |
|---|---|---|---|
| 1 | **GPX's number text on iOS.** In one hint mode, GPX prints three doubles with the platform's own text, and 5 of 59 NT routes come out differently on the simulator (-156). The diagnosis is running (session 59). | Diagnosis running | Then the owner's: what the port does about printing a double as Java does (no JDK code is copied, -119) |
| 2 | **The fix for item 1,** as ruled, with the simulator's digest test as its failing test. | Waits on 1 | Planner, coder |
| 3 | **The formats' planted error on the simulator,** one planted commit and its revert (-139 allows it on this branch). | Not started | Coder |
| 4 | **A public entry for the route-ways reader.** Its classes are ported but `internal`; Forager's next-turn code and `WayToEnds` need it [survey]. Tests first, against the Java engine, as step 5c's were. | Not started | Planner decides its shape with the adapter (part two, phase C) |
| 5 | **The stopped-clock test's common sibling** (-148, decision 2). | Not started | Coder |
| 6 | **The closing list for step 10:** every "not covered" named in -121 to -156, in one place, each with where it is owed. | Not started | Planner writes it from the record |
| 7 | **The merge of `brouter-port-5b` into `brouter-port`.** The owner's condition in -61, the red test, is met (-148). | Waits on the owner's word | Owner |
| 8 | **The record branch's PR to `main`.** `main`'s record ends at -102. | Waits on the owner's word | Owner |

### What the port will not have shown when it closes

Named so that nobody reads "done" as more than it is:

- **A real iPhone.** Every iOS result is from the simulator, on a debug build.
- **Speed and memory on a device** (-24's 25% flag has no iOS measurement).
- **8 of the 13 table rows** where the port's cosine differs from desktop Java's lie in no test tile (-137).
- **Android's own runtime.** The Java engine was run on a desktop JVM, not on a phone.
- **Deep recursion elsewhere in the engine.** The stack ladder covers four cases in one tile (-146).
- **Five tests whose comparison or setup changed after a result** wait for the independent reviewer: -107, -116,
  -119, -126, -148.
- R's 940 remaining foot and bike cases and the three timing kinds have no simulator run (-143).

## 3. Part two: the app

### What the app is [survey; the first four counts re-counted by the planner, -152]

- One Android module and the vendored Java BRouter: 892 Kotlin files, 117,306 lines. `ui` is 441 files; `domain` is
  288 files with no Android import.
- Five tabs and about 20 screens. Four things run without the screen: the recording service with its 15 s watches,
  an inexact wake-up alarm, a periodic backup, and offline map downloads.
- Android has 1,012 test files with 6,876 tests; 574 run on Robolectric and cannot run on iOS as they are.

### The phases

Each phase ends with something that runs in CI and a record entry saying what it showed and what it did not.

**Phase A. The shell and the map (first, -153).**
- A1. The module layout and an Xcode project produced on a runner, with an empty Compose Multiplatform app that
  launches on the simulator and is screenshotted there. [planner]
- A2. The map spike: one screen showing the map from the project's own tile server and style, through MapLibre.
  First through the Compose wrapper, which is marked beta on iOS [survey]. If it fails, through MapLibre's own iOS
  SDK in a native view, before any other screen is started (-153).
- A3. The offline store and the tile archive format tried on the simulator. Both are unverified [survey].
- What it will not show: frame rate, memory or battery, which need a phone.

**Phase B. The shared domain (-153).**
- B1. A proposal to the owner before anything is written into Forager: which repository holds the shared module, how
  the Android build uses it, who dispatches the Android side, and the first piece to move. This repository's
  `CLAUDE.md` still forbids writing to Forager and is changed only on that word. [planner, -153]
- B2. The pieces that block sharing, in the order the survey's counts suggest: the logging interface (98 files);
  `java.util`, `java.io` and `java.nio` (125); `java.time` (75, and it runs through the sundown and "back by"
  logic). [open: the order]
- B3. Each piece moves with its Android tests still passing; the Android app ships from the shared code as it goes.

**Phase C. Routing in the app.**
- C1. The adapter around `route()`. It owes 12 behaviours, each read from Forager's Android caller [survey]:
  1. catch what the engine's constructor throws (-129);
  2. refuse a route of fewer than 2 points (-134);
  3. but accept one point for a snap, and fail only on none;
  4. check the data folder first: it exists, holds a tile, holds the tiles for both ends, and each reads;
  5. one route at a time in the process;
  6. cancel by `terminate()`, wait until the engine has stopped, then rethrow;
  7. run the engine on its own thread;
  8. a 30-second limit;
  9. the fixed parameters `alternativeidx=0` and `profile:use_dynamic_range=0`;
  10. name the failure from the engine's message;
  11. convert the raw values: positions, and heights in quarter metres with "no height" kept as none, never zero;
  12. put the profile and `lookups.dat` where the engine can read them, and pass a path.
- C2. The route-ways reader's public entry (part one, item 4), then Forager's next-turn code (-111).
- C3. Routing data downloads, which are the app's own code [survey].

**Phase D. The designer's first job (-13).** Where the iOS app follows iPhone habits and where it matches Android,
with the survey as its input. It can run beside phases A and B.

**Phase E. The remaining screens,** in an order the owner rules on with the designer's work [open]. Storage (two
databases, 19 migrations) and settings come with the first screen that needs them.

**Phase F. What only a phone can show:** location in the background, notifications, sensors, the camera. Each can be
written and unit-tested here and cannot be verified here. [open: device evidence]

### The dependencies that are not Kotlin, least to most time-consuming

The counts are the survey's. The order is the planner's estimate from those counts and from how much design each
needs; nothing was built to measure it.

| Rank | Dependency | Used for (files) | Where its replacement lives |
|---|---|---|---|
| 1 | Google Play services | none (0) | nowhere |
| 2 | Tile server, map styles, sprites, credits | the map's data | stays as it is; both apps read it |
| 3 | Build scripts (Python, shell) | the fungi index, checks | stays with Android's tooling |
| 4 | Connectivity check | online or not (1) | iOS only |
| 5 | MGRS library (Java) | grid references (1) | shared; no multiplatform library found, not searched |
| 6 | Android logging | logging (98) | shared, behind one interface |
| 7 | BRouter (Java, 101 files) | routing (3 callers) | shared: the Kotlin port |
| 8 | Retrofit, OkHttp | web calls, downloads (13) | shared, on Ktor |
| 9 | DataStore | settings (13) | shared |
| 10 | `java.util`, `java.io`, `java.nio` | UUIDs, locales, files (125) | shared |
| 11 | `java.time` | dates (75) | shared, on kotlinx-datetime |
| 12 | Intents, sharing, deep links, pickers, file access, permissions | links, import, export (about 47) | iOS only, behind interfaces |
| 13 | Exif and Android graphics | photos, markers (25) | images shared, Exif iOS only |
| 14 | Lifecycle, activities, windows, `Context` | app structure (about 139) | shared structure, iOS-only window code |
| 15 | Location and sensors | fixes, compass, level, pressure (10 and the service) | iOS only; the simulator has none of the sensors |
| 16 | Notifications, alarms, WorkManager | alerts, wake-up, backup (12) | iOS only; needs design |
| 17 | CameraX | capture, RAW, DNG (5 files) | iOS only; out of the first release (-153) |
| 18 | Room and SQLite | two databases, 19 migrations (25) | shared |
| 19 | MapLibre for Android | the map (39) | shared if the Compose wrapper holds, else iOS only |
| 20 | Jetpack Compose and its tests | the interface (267 files import it) | shared, on Compose Multiplatform |

### Decisions still the owner's, not yet asked [survey, -152, -153]

1. The order of the screens after the map.
2. How the fungi index opens on iOS: its Android loading method does not exist off Android.
3. Scheduled backup on iOS: manual only, or best-effort when the system allows.
4. Device evidence: whether to add a tester with an iPhone or a device service, and when.
5. The app's public identity: its name, its bundle identifier, and whether it shares Android's tile server.
6. Whether a tile is captured for the 8 untested table rows (-137).
7. An independent reviewer for the five flagged tests.

### Decisions that are the planner's [survey]

The route-ways entry's shape; whether the data-folder check moves into shared code; the module layout and how Android
uses a library with no Android target; the `java.time` replacement's order; the database library's version line;
networking in shared code or behind interfaces; which of Android's 353 interface tests are rewritten as shared tests;
what drives the sundown line on iOS without the 15 s tick.

## 4. What is not known

- Whether MapLibre's Compose wrapper works for this map on iOS, online or offline. Phase A answers it.
- Whether the Android app can build against the port as it is built today, which has no Android target [survey].
- What sharing the domain costs the Android app. No build was run for the survey.
- Anything about iOS that only a phone shows.
- How long any phase takes. This plan gives an order and counts, not dates: nothing has been measured that a date
  could rest on.
