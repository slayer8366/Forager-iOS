# The Forager Android app as it stands: a survey for planning the iOS app port

Dispatch 2026-11-12-01 (planner). Researcher: Madrone Gauge. Read-only. Nothing in Forager was changed, built or
fetched; nothing here is a decision.

**Source:** an export of Forager `pre-main` at `12eb9697` (no git history). The port's surface was read from this
repository's `origin/brouter-port-5b` at `65737ec` with `git show`. External pages were read on 2026-11-12.
**Marks:** [R] read at source (file, line), [C] counted by a command in the evidence folder
(`docs/research/2026-11-12-app-port-survey/`), [I] inferred, [?] not verified. Paths without a top folder are
under `app/src/main/java/com/zynergylabs/forager/app/`. Counts by import scan are bounds, not compiler results.

## Premises of this dispatch that are wrong (question 8)

1. **The snap-to-network call is not a gap.** At source it is an ordinary route call through the same engine run:
   `BRouterTrailRouter.firstRoutePoint` (`routing/BRouterTrailRouter.kt:91-104`) routes from the point to itself
   or toward a target and reads the first node. The port's `route()` already returns what it needs. What differs
   is the acceptance rule: one node is an answer for a snap, and fewer than 2 is a failure for a route. [R]
2. **The route-ways reader is the real gap, and next-turn depends on it.** The classes it needs are ported but
   `internal`; the port has no public function for it. Four `domain/nextturn/` files and `domain/wayto/WayToEnds.kt`
   use `RouteWays`. [R][C]
3. **RECORD -111, -129 and -134 are not on `origin/main`.** `origin/main`'s record ends at -102 (`d22a302`). The
   three entries were found on `origin/ccr-7e682f25-4tqqon` (highest ID -151) and read there. [C]
4. **Forager's exported `RECORD.md` ends on 2026-10-06** (12,247 lines), before the baseline report. No Forager
   ruling after that date can be read from the export. [C]
5. **"Exact alarms" and "any Google Play service": the app has neither.** The wake-up alarm is inexact by design
   (`alert/AndroidWakeUpAlarms.kt:21-22, 36`) and no file imports `com.google.android.gms` or Firebase. [R][C]
6. **The baseline's "seven BRouter classes" is now fifteen,** because `routing/BRouterRouteWays.kt` was added
   after it. [C]

## Short answer

- The app is one `:app` module plus the vendored Java `:brouter`: 892 Kotlin files, 117,306 lines in
  `app/src/main`, up from the baseline's "about 98k". `ui/` is 441 files (63% of the lines); `domain/` is 288
  files and has no `android` import. [C]
- It has five tabs, a floating search, and about 20 surfaces. It runs four things without the screen: the
  recording service (location, the 15 s watches), an inexact wake-up alarm, a periodic backup worker, and
  MapLibre's offline downloads. [R][I]
- Since the baseline: `domain/` grew by 61 files, MapLibre files went from 30 to 39, tests from 696 files to
  1,012. New features include next-turn, route plotting, the drop-off map, trail heights, trip map downloads and
  the route-ways reader. `photo/` and `data/remote/` are unchanged to the line. The "66 files" compiler figure
  was not re-run (no build allowed); the scan figure is 74 domain files with a JVM-only API. [C]
- 418 of 892 files import a platform or JVM-library API; 234 if Compose itself is left out. The map (39 files)
  and the camera are the two renderer-level pieces. Everything else has a named Kotlin Multiplatform route, with
  three exceptions that need a design: `createFromAsset` for the fungi index (not available off Android), the
  periodic backup, and the 15 s tick that drives the watches. [C][R]
- Routing: three main files touch BRouter. `route()`, `RouteCancel` and `checkDataFile` cover the router and the
  snapper. They do not cover the route-ways reader. The adapter owes 12 behaviours listed in question 4. [R]
- No Mac: a simulator build and simulator tests need no Apple account [I]; a signed build needs a certificate and
  a provisioning profile as GitHub's guide lists them [R], which come with the Apple Developer Program, "$99
  annual membership" [R].
- Size: 16 screen or tab composables, 1,012 test files with 6,876 `@Test`, 574 of them on Robolectric or
  `AndroidJUnit4`, 1 instrumented test, no screenshot-test library. [C]
- 9 open decisions for the owner and 9 for the planner are listed in question 7.

## Predictions, scored

Committed at `6f9b4b8` before any source was read.

| Q | Predicted | Found | Score |
|---|---|---|---|
| 1a | 2 Gradle modules | 2 | right |
| 1b | 14 screens | 16 screen or tab composables; 20 surfaces as grouped below | low |
| 1c | 640 files, 105,000 lines | 892 files, 117,306 lines | wrong (files low by 252) |
| 2a | domain 255 files, 19,500 lines | 288 files, 22,418 lines | low |
| 2b | 32 MapLibre files | 39 | low |
| 2c | baseline table misses an API family: yes | yes (WorkManager, connectivity, intents and sharing, the level, vibration, Do Not Disturb, sea-level conversion) | right |
| 2d | 3 other new packages | not scoreable without history; at least 6 features by dated documents | not scored |
| 3a | MapLibre Native for Android: yes | yes | right |
| 3b | a Google Play service: yes | none | wrong |
| 3c | a Java-only library besides BRouter: yes (MGRS) | yes, 1 file | right |
| 4a | 3 main files import `btools.` | 3 | right |
| 4b | port does not cover route-ways and snap | route-ways not covered; snap covered | half |
| 4c | both known requirements visible, at least 2 more | both visible; 10 more | right |
| 5a | no signed build without a paid account; 99 USD a year | "$99 annual membership" read; the "no" is read from Apple's comparison page in part | right, part [?] |
| 5b | simulator build and tests need no account: yes | not stated on any page fetched | [?] |
| 6a | 330 Android-bound files outside Compose | 234 | high |
| 6b | 0 to 3 domain files with an `android` import | 0 | right |
| 6c | 760 test files, 5,600 `@Test`, 25 instrumented, no screenshot framework | 1,012; 6,876; 1; none | counts wrong, framework right |

## 1. What the app is today

**Modules** (`settings.gradle.kts`, read whole): `:app`, and `:brouter` at `third_party/brouter` (101 Java files,
19,327 lines, unchanged from the baseline). [R][C]
**Source sets** (Kotlin files): `main` 892, `test` 1,007, `testDebug` 4, `androidTest` 1. No Java in `app`. [C]
**Build facts** (`gradle/libs.versions.toml`, `app/build.gradle.kts`): AGP 9.3.1, Kotlin 2.3.10, compileSdk and
targetSdk 37, minSdk 26, JVM 17, Compose BOM 2026.08.00 with Material3 1.5.0-alpha26, MapLibre Android 13.5.0,
Room 2.8.4, DataStore 1.2.1, WorkManager 2.12.0, CameraX 1.6.2, Retrofit 2.12.0, OkHttp 4.12.0. [R]

**Kotlin per top-level package** (`01-count-packages.tsv`) [C]:

| Package | Files | Lines | | Package | Files | Lines |
|---|---|---|---|---|---|---|
| `ui` | 441 | 74,328 | | `routing` | 8 | 685 |
| `domain` | 288 | 22,418 | | `alert` | 5 | 703 |
| `data` | 93 | 10,711 | | `location` | 5 | 479 |
| `map` | 16 | 1,910 | | `sensor` | 4 | 391 |
| `photo` | 14 | 2,822 | | `importgpx` | 4 | 252 |
| (root) | 8 | 1,773 | | `net` | 3 | 107 |
| `service` | 2 | 611 | | `export` | 1 | 116 |

Inside `ui`: `availability` 175, `map` 117, `log` 90, `track` 15, `motion` 12, `route` 10, `theme` 10, others 12.
Inside `domain`: 174 direct, `model` 48, `nextturn` 14, `mapdata` 12, `route` 10, `wayhome` 9, `wayto` 7, others
14. [C]

**Surfaces.** There is no navigation library (`ui/log/RecordsTab.kt:62`, comment); one activity hosts everything
and a second takes GPX files. The tab list is `CompactTab` (`ui/availability/AvailabilityNavigationUi.kt:109-115`).
The one-line descriptions are from `README.md:18-52` [R]; the grouping into rows is mine [I].

| # | Surface | Hosted in | What it does |
|---|---|---|---|
| 1 | Shell: five tabs as a bottom bar or a rail | `ui/availability/AvailabilityScreen.kt`, `AvailabilityCompactScaffold.kt`, `AvailabilityNavigationUi.kt` | One layout at every window size |
| 2 | Search (floats over the tabs) | `ui/availability/AvailabilitySearchUi.kt`, `ScaffoldHeaderSearch.kt` | Species (bundled list), place, radius, month |
| 3 | List tab | `ui/availability/AvailabilityResultsUi.kt` | The ranking for the search; needs the network |
| 4 | Seasonal tab | `ui/availability/AvailabilitySeasonalUi.kt` | Weather for the region and the rain-to-fruiting test |
| 5 | Maps tab | `ui/availability/AvailabilityCompactMapUi.kt` and `ui/map/` (117 files) | Three base maps, overlays, sightings, stacks and fans, bubbles |
| 6 | Map layers, map type, quick settings | `ui/map/MapLayersSheet.kt`, `MapTypePanel.kt`, `ui/availability/MapQuickSettings.kt` | Overlay switches; sundown and off-track settings |
| 7 | Navigation display | `ui/map/NavigationView.kt`, `MapNavigationView.kt` | Way home, way to a waypoint, next turn [I by name] |
| 8 | Compass and elevation strip | `ui/availability/CompassElevationStrip.kt` | Heading, height, the sundown line [I by name] |
| 9 | Track recording controls and export | `ui/track/` (15 files) | Start, stop, back by, GPX export |
| 10 | Route plotting and the route card | `ui/route/` (10 files) | Plan a trip on the map |
| 11 | Journal, Entries side | `ui/log/JournalTab.kt`, `CartographyScreen.kt`, `CartographyEntry*Screen.kt` | Trip write-ups that keep finds, photos, waypoints, tracks |
| 12 | Journal, Records side | `ui/log/RecordsTab.kt`, `RecordDetailsSheet.kt` | All, Finds, Tracks, Waypoints, Offline maps |
| 13 | Find detail, report, gallery, drafts | `ui/log/LogEntryDetailScreen.kt`, `LogEntryReportScreen.kt`, `FindsGalleryScreen.kt`, `EntriesDrafts.kt` | One find and its photos |
| 14 | In-app camera | `ui/log/InAppCameraDialog.kt`, `InAppCameraHost.kt`, `photo/` | Capture, RAW and DNG, grid, level, timer |
| 15 | Photo viewer and picker | `ui/log/PhotoViewerDialog.kt`, `PullPhotoPickerScreen.kt` | View and import photos |
| 16 | Tools drawer and Trip Planner | `ui/availability/ScreenToolsDrawer.kt` | Planned trips and the days worth going |
| 17 | Settings, Sources and credits, Open source software | `ui/availability/AvailabilitySettingsPages.kt`, `ui/credits/` | Units, theme, night maps, map, backup |
| 18 | Offline maps and trip map downloads | `ui/availability/AvailabilityOfflineMapsUi.kt`, `TripDownloadMapsUi.kt` | Download, list and delete regions |
| 19 | Backup and restore | `ui/backup/BackupSection.kt`, `RestoreLoadingPage.kt` | The journal to a folder the walker chooses |
| 20 | GPX import (its own activity) | `importgpx/GpxImportActivity.kt` | Opens `.gpx` from other apps (`AndroidManifest.xml:198-235`) |

**Background behaviour** [R at the lines given]:

| Behaviour | Where | Mechanism |
|---|---|---|
| Track recording | `service/TrackRecordingService.kt:155, 514` | Foreground service, type `location`, `START_STICKY`, an ongoing notification |
| Sundown and Back by watches | `TrackRecordingService.kt:278, 288, 559, 562`; `service/RecordingWatches.kt` | A 15 s tick inside the service, plus every fix |
| Wake-up at a set time | `alert/AndroidWakeUpAlarms.kt:36, 75`; `domain/WakeUpAlarms.kt:19` | Inexact `setAndAllowWhileIdle` into `WakeUpAlarmReceiver`; kinds `BACK_BY`, `SUNDOWN` |
| Alerts | `alert/AndroidAlertDelivery.kt:196-475` | Three channels (off-track, sundown, back by), vibration, Back by actions "I'm back" and "later" |
| Scheduled backup | `data/backup/ScheduledBackup.kt:37, 76`; `AndroidBackupNotifier.kt:113-116` | WorkManager periodic worker; a retry receiver enqueues a one-time run |
| Offline map download | `map/MapLibreOfflineMapRepository.kt` (511 lines) | MapLibre's offline manager, in-process [I: not read line by line] |
| Network state | `net/AndroidNetworkSignal.kt:27-64` | A connectivity callback as a flow |
| Background-run check | `alert/AndroidBackgroundRunCheck.kt:20` | Reads whether battery optimisation is off |

## 2. What changed since the feasibility report

The baseline read `b2c4951e` (filed at `a9c37eb3`). Its own counts re-run on `12eb9697`
(`05-baseline-recount.sh`, `.tsv`) [C]:

| Count | Baseline | Now | Holds? |
|---|---|---|---|
| `domain/` files, lines | 227; 16,871 | 288; 22,418 | no, +61 files |
| Domain files failing as common code | 66 (compiler) | not re-run; scan union 74 | see note |
| Domain files importing `java.time` | 27 | 27 | yes |
| Domain files importing `java.util.UUID` | 8 | 9 | near |
| Domain files with an `android` import | none stated | 0 | yes |
| Domain interfaces (ports) | 9 named and "about 60 more" | 70 declarations | yes |
| Files importing `org.maplibre.android` | 30 (26 `ui/map`, 4 `map`) | 39 (35, 4) | no, +9 |
| `map/MapLibreOfflineMapRepository.kt` lines | 494 | 511 | near |
| `photo/` lines | 2,822 | 2,822 | yes |
| `data/remote/` lines | 463 | 463 | yes |
| `SupportSQLiteDatabase` references in `Migrations.kt` | 20 | 20 | yes |
| Files importing `android.util.Log` | 80 | 98 | no |
| Material icon import lines | 181 | 190 | near |
| Files importing Compose; their lines | 213; 44,186 | 267; 52,110 | no |
| Kotlin lines in `app/src/main` | "about 98k" | 117,306 | no |
| Test files; `@Test` | 696; 5,121 | 1,012; 6,876 | no |
| Test files on Robolectric or `AndroidJUnit4` | 365 | 574 | no |
| Domain test files; plain; Robolectric; no `@Test` | 138; 125; 5; 8 | 191; 171 (by subtraction); 8; 12 | no |
| BRouter Java files, lines | 101; 19,327 | 101; 19,327 | yes |
| BRouter classes the app imports | 7 | 15 | no |

Note on the 66: the baseline's figure came from a compiler; this survey could not build. The scan union (a `java`
import, `Math.`, `String.format`, `Locale.`, `System.`, `synchronized` or `javaClass`) finds 74 of 288 domain
files. The baseline showed a scan and the compiler disagree, so 74 is a scan figure and not a new "66". [C][I]

**What exists now that the baseline does not mention.** Without history this is by content and by the 47
documents dated after the baseline (`06-docs-dated-after-baseline.txt`, names only, not read) [C][I]:

| New | Where | Evidence it postdates the baseline |
|---|---|---|
| Next turn | `domain/nextturn/` (14 files) | `docs/audits/2026-11-06-01-next-turn-completion-report.md` |
| Route plotting and the route card | `ui/route/` (10), `domain/route/` (10) | audits dated 2026-10-30 and 2026-11-06 |
| Route-ways reader | `routing/BRouterRouteWays.kt`, `domain/routing/RouteWays.kt` | its header: "way-to dispatch 2026-10-28" (`:24`) [R] |
| Trail heights on a route | `BRouterTrailRouter.kt:47, 216-228` | "dispatch 2026-10-30-06" [R] |
| Drop-off map | `domain/dropoff/` (4), `data/dropoffmap/` (4) | audit dated 2026-11-04 |
| Trip map downloads | `ui/availability/TripDownloadMaps*.kt` | audit dated 2026-11-05 |
| Sea-level altitude | `location/AndroidSeaLevelConverter.kt` | audits dated 2026-10-28 |
| Offline store read | `map/MapLibreStoreRead*.kt` | audit dated 2026-11-05 |

Not datable from the export, and not named in the baseline: `domain/mapdata/` (12 files), `data/mapdetail/`,
`data/routedata/` (7), `domain/routedata/` (3), `domain/relief/`, `ui/motion/` (12). [?]

## 3. What is Android-bound, by API

Import scan of `app/src/main` (`04-api-scan.sh`, `.tsv`, one file list per family) [C]. 418 of 892 files import
`android.`, `androidx.`, MapLibre, Retrofit, OkHttp, Okio, BRouter or the MGRS library; 234 when Compose imports
are left out; 21 of those are bound by `android.util.Log` alone.

| API | Files (top packages) | Used for | On iOS today |
|---|---|---|---|
| Map: MapLibre Native Android 13.5.0 (`org.maplibre`) | 39 (`ui/map` 35, `map` 4) | The map, its layers, offline regions | MapLibre Compose: "On Android, iOS, and Desktop, the map renders with MapLibre Native"; "Offline map downloads are available on every platform except the browser"; "The API is not yet stable. Expect breaking changes between minor releases" [R, maplibre.org]. Its README rates iOS "Beta" [R, GitHub]. The other route is MapLibre Native for iOS hosted in a `UIKitView` [R, Kotlin docs, for `UIKitView`; I for MapLibre in it]. Whether either covers the offline store read and the PMTiles base map is [?] |
| Camera: CameraX, camera2 | 3 + 2 (`photo`) | Capture, RAW, DNG | Not on Google's Kotlin Multiplatform list [R]. AVFoundation in `iosMain` [?] |
| Location (`android.location`) | 5 (`location` 4, `ui/map` 1), plus the service | Fixes, last known fix, sea-level conversion | Core Location in `iosMain`. Apple's background-location page did not render when fetched [?] |
| Sensors | 5 (`sensor` 4, `ui/map` 1) | Compass (rotation vector, accelerometer, magnetometer), level, pressure, declination | Core Motion and Core Location heading in `iosMain` [?]; the simulator has none of them (baseline) |
| Notifications | 5 (`alert` 3, `service` 1, `data/backup` 1) | Recording, off-track, sundown, back by, backup | User Notifications in `iosMain`; ruled: back by at the set time. Apple's page did not render when fetched [?] |
| Alarms and pending intents | 5 | The inexact wake-up; notification actions | No counterpart for running code at a time (baseline, ruled) |
| WorkManager | 2 (`data/backup`) | Periodic backup | Not on Google's list [R]. iOS background tasks run when the system chooses (baseline) [?] |
| Room and SQLite | 25 (`data/local`) | Two databases: the journal (version 24, 19 migrations) and the fungi index (version 1, `createFromAsset`); 12 entities, 9 DAOs, 99 suspend functions and 4 flows, no blocking DAO function | Room is on Google's list for iOS [R]. Its page says migrations take `SQLiteConnection`, DAO functions must be `suspend`, and `createFromAsset` is "unavailable in platforms other than Android" [R]. That page's samples now use `androidx.room3` [R] |
| DataStore | 13 (`data/repository`) | Settings | On Google's list for iOS [R] |
| Retrofit | 4 (`data/remote`) | iNaturalist, Open-Meteo | JVM only. Ktor's Darwin engine: "It uses NSURLSession under the hood", artifact `ktor-client-darwin` [R] |
| OkHttp, Okio | 9 (`map` 3, `data/dropoffmap` 2, others 4) | Map tile client and retry, route-area and drop-off downloads, the user agent | Ktor as above; Okio is already the port's file API [R, `brouter/build.gradle.kts:97`] |
| kotlinx.serialization | 19 | DTOs, stored tiles, credits | Multiplatform already [I] |
| Permissions | 11 | Location (fine, coarse), camera, notifications, media location | Per-framework requests in `iosMain`; no common API in Compose Multiplatform [?] |
| Activity results | 6 | Create, open and tree document pickers; photo picker; take picture | Document and photo pickers in `iosMain` [?] |
| Intents | 14 | `geo:` links, share (`ACTION_SEND` 2), view (4), app settings, the iNaturalist app | `UIApplication.openURL` and share sheets in `iosMain` [?] |
| Uri, ContentResolver, MediaStore, FileProvider | 14 + 2 | Photo import and export, GPX in and out, the backup folder | File URLs and security-scoped bookmarks in `iosMain` [?] |
| `android.content.Context` | 66 | Reaches every adapter | Goes away behind the ports [I] |
| `android.util.Log` | 98 | Logging | A logging port (baseline) |
| `android.graphics` | 17 (`ui/map` 10) | Bitmaps for markers, photos, thumbnails | Compose `ImageBitmap` or Skia in common [?] |
| Exif and media | 8 (`photo` 6) | Orientation, location scrub | ImageIO in `iosMain` [?] |
| Connectivity | 1 (`net`) | Online or not | Network framework in `iosMain` [?] |
| Views and windows | 22 | Edge to edge, orientation, keep-out insets, `AndroidView` for the map | `UIKitView`; window handling in `iosMain` [R for `UIKitView`] |
| Lifecycle, activity | 22, 29 | ViewModels, back handling | Lifecycle and ViewModel are on Google's list for iOS [R] |
| Compose | 267 | The UI | Compose Multiplatform (ruled). 41 files read `LocalContext`, `LocalView` or `LocalConfiguration`; 58 import Material icons; 10 use `R` or `stringResource` (30 strings) |
| `java.time` | 75 (`domain` 27, `ui` 41) | Dates everywhere | `kotlinx-datetime` (baseline) [?] |
| `java.io`, `java.nio` | 75 (`domain` 22, `photo` 12, `routing` 7) | Files | Okio [I] |
| `java.util` | 50 | UUID, Locale, collections | Kotlin common where it exists [?] |
| MGRS (`mil.nga`), Java-only | 1 (`domain/MgrsConverter.kt`) | Grid references | No Kotlin Multiplatform artifact found; not searched [?] |
| BRouter (`btools.`), Java-only | 3 (`routing`) | Question 4 | The port |
| Launcher and deep links | manifest | `geo:` and iNaturalist queries (`:61-75`), GPX view and send filters (`:198-235`) | Document types and URL schemes in the Xcode project's Info.plist [?] |
| Google Play services | 0 | none | none needed |

**From `server/`:** one project, `server/pmtiles-worker/`. The app reads it for vector tiles, styles, relief
tiles and route areas. The host name is in `data/routedata/RouteAreaClient.kt`, `map/OfflineStyle.kt`,
`map/TrailStyle.kt`, `map/OfflineReliefTileStore.kt`, `domain/OfflineMapRepository.kt` and `ui/map/Basemap.kt`.
Other hosts: iNaturalist, Open-Meteo (forecast and archive), OpenTopoMap, OpenStreetMap tiles (`README.md:140-151`). [R]

## 4. How the Android app uses the routing engine

Every `btools.` reference is in `07-btools-references.txt` (20 lines) [C]. Three main files and two test files.

| File | Lines | BRouter classes | What it does | Port covers it? |
|---|---|---|---|---|
| `routing/BRouterTrailRouter.kt` | 233 | `RoutingEngine`, `RoutingContext`, `RoutingParamCollector`, `OsmNodeNamed`, `OsmTrack`, `OsmPathElement` | Builds and runs one route; also the snap's engine run | Yes: `route()` mirrors `buildEngine` and `runToEnd` (`Route.kt:78-99`) |
| `routing/RoutingDataFolder.kt` | 72 | `PhysicalFile`, `DataBuffers` | Folder check before the engine | Part: `checkDataFile` is the per-file header check only |
| `routing/BRouterRouteWays.kt` | 137 | `PhysicalFile`, `DataBuffers`, `OsmFile`, `OsmNode`, `OsmLink`, `OsmNodesMap`, `GeometryDecoder`, `BExpressionContextWay`, `BExpressionMetaData` | Reads every way near a point straight from the tiles, with `lookups.dat` and no profile | No public entry. All nine classes exist in the port's `commonMain` as `internal`, and the port's own notes name this reader (`OsmNode.kt:15-16`, `MicroCache2.kt:6`) |
| `routing/BRouterTrailSnapper.kt` | 32 | none directly | `nearestTrail` by a route to itself or toward a target | Yes, through `route()` |
| `routing/BRouterFailures.kt` | 50 | none (reads message text) | Names a failure from the engine's message prefix | Adapter work; needs the port's messages to equal Java's |
| `routing/RoutingProfile.kt` | 51 | none | Installs `hiking-mountain.brf` and `lookups.dat` from assets | Adapter work [I: file not read; from `BRouterTrailRouter.kt:62` and `BRouterRouteWays.kt:39-40`] |
| `routing/TrailRoutingEngine.kt`, `WayHomeRouting.kt` | 50, 60 | none | Wiring [? not read] | n/a |

Who calls the ports (all in `domain/`, so shared once ported) [C]: `TrailRouter` from `wayhome/FindWayHomeUseCase`,
`wayto/FindWayToWaypointUseCase`, `routedata/` (2 files) and `ui/availability/AvailabilityWaypointRoute.kt`;
`TrailSnapper` from `wayhome/` (2 files); `RouteWays` from `nextturn/` (4 files) and `wayto/WayToEnds.kt`.

**What the adapter must do, read from the Android caller** [R]:

1. **Catch what the constructor throws** (RECORD -129): confirmed. `BRouterTrailRouter.kt:122-131` catches every
   `Exception` but `CancellationException` and returns `EngineFailed(e.message ?: class name)`. The port lets
   them pass (`Route.kt:80`, "The constructor's exceptions pass through").
2. **Refuse a `Found` of fewer than 2 nodes** (RECORD -134): confirmed, `:106-113`, as `EngineFailed("no error
   reported and N route points")`, logged.
3. **But accept one node for a snap,** and fail only on zero (`:91-104`).
4. **Check the folder first** (`RoutingDataFolder.kt:26-41`): it is a directory; it holds at least one `.rd5`;
   the square file for the start and for the end exists (`squareFileName`, `:59-67`, integer arithmetic copied
   from BRouter); every file's header reads. Only the last is `checkDataFile`.
5. **One route at a time in the process** (`:120`, `:198`), because the engine's profile cache is shared and one
   route's memory is the bound.
6. **Cancel by `terminate()`, then wait without being cancellable until the engine has stopped, then rethrow**
   (`:166-175`). A cancelled call returns nothing and logs nothing. `RouteCancel` gives the flag; the wait is
   the adapter's.
7. **Run the engine on its own thread** (`:205-207`), so a call can be resumed to cancel it. Android uses a Java
   executor; the iOS adapter needs a Kotlin/Native equivalent.
8. **A 30,000 ms limit** (`:196`) as `maxRunningTimeMillis`; a timeout comes back as `EngineFailed`.
9. **The fixed parameters** `alternativeidx=0` and `profile:use_dynamic_range=0` (`:153-154, 189`).
10. **Name the failure from the message** (`BRouterFailures.kt:18-39`): start or end not mapped, no path, data
    missing, data corrupt, else `EngineFailed` with the text kept.
11. **Convert the raw values** (`:228, 231`): positions are micro-degrees offset by 90 and 180; a height is
    `sElev / 4.0`, and `Short.MIN_VALUE` is `null`, never 0.
12. **Put the profile and `lookups.dat` where the engine can read them,** and hand a path, not a `java.io.File`.
    `TrailRouter.route` still takes a `File` (`:80`; the baseline's finding stands), as do `TrailSnapper` and
    `RouteWays.near`.

**Not covered by the port and not adapter work:** the route-ways read (above), and downloading routing data
(`data/routedata/`, 7 files, one importing OkHttp; `domain/routedata/`, 3 files), which is the app's own code. [C][I]

**Not verified:** whether an Android app module can consume the port as built (targets `jvm`, `iosArm64`,
`iosSimulatorArm64`; "no androidTarget", `brouter/build.gradle.kts:1-2`). [?]

## 5. What a build and a release without a Mac need

| Need | What | Source |
|---|---|---|
| A runner | Labels `macos-latest`, `macos-15`, `macos-26` (arm64, 3 cores, 7 GB) and Intel labels. "Use of the standard GitHub-hosted runners is free and unlimited on public repositories." | GitHub, runners reference [R] |
| A signed build | Four secrets: `BUILD_CERTIFICATE_BASE64` (a `.p12`), `P12_PASSWORD`, `BUILD_PROVISION_PROFILE_BASE64` (a `.mobileprovision`), `KEYCHAIN_PASSWORD` | GitHub, "Sign Xcode applications" [R] |
| The certificate and profile | Certificates, Identifiers and Profiles, and App Store Connect, are reached after enrolling | Apple, compare memberships [R, table columns did not render] |
| The membership | "$99 annual membership"; "Enroll as an individual or organization." | Apple, programs page [R] |
| TestFlight | Managed in App Store Connect. Up to 100 internal testers with a team role; up to 10,000 external testers after the first build is "approved by App Review for TestFlight" | Apple, TestFlight page [R] |
| Upload from CI with an App Store Connect API key | Not fetched | [?] |
| Creating the certificate request with no Mac | Not fetched; the baseline also left it unverified | [?] |
| An Xcode project with no Xcode to edit it | Generated in CI from text (baseline named two tools, untried) | baseline [?] |

**With no account at all** [I, from the port's own CI already running `iosSimulatorArm64` tests, and Kotlin's
docs]: compile shared code for iOS on Linux (baseline, built); build for the simulator on a macOS runner; run
common Compose UI tests there with `./gradlew :shared:iosSimulatorArm64Test` [R, Kotlin docs]; take simulator
screenshots [?]. Apple's free tier allows on-device runs only through Xcode on a Mac, with profiles that expire
after 7 days [R], so it does not help here.

**Cannot be verified without a physical iPhone** (baseline's list, restated; nothing here contradicts it):
recording in the background on a real walk; delivery of the Back by notification at its time on a locked phone;
GPS accuracy; battery use; the compass, barometer and level; the camera, RAW and DNG; offline tiles and touch on
a real screen.

## 6. The size of it (counts only)

| What | Count | How |
|---|---|---|
| Composables named `…Screen`, `…Tab` or `…Pane` | 16 | grep [C] |
| Surfaces as grouped in question 1 | 20 | [I] |
| Dialogs; sheets (distinct composable names) | 7; 1 | grep [C] |
| Files importing any platform or JVM-library API | 418 of 892 | `04` [C] |
| The same, leaving Compose out | 234 | `04` [C] |
| Of those, bound by `android.util.Log` alone | 21 | `04` [C] |
| Compose files; of them also importing Android, MapLibre or CameraX | 267; 65 | `05` [C] |
| Domain files; with an `android` import; with a JVM-only API by scan | 288; 0; 74 | `05` [C] |
| Test files; `@Test` | 1,012; 6,876 | `05` [C] |
| Plain JVM tests (no Robolectric, no `AndroidJUnit4`) | 438 files | 1,012 less 574 [C] |
| Robolectric or `AndroidJUnit4` | 574 files | `05` [C] |
| Compose UI tests (`createComposeRule` and kin), on Robolectric | 353 files | `05` [C] |
| Instrumented (device) tests | 1 file, `photo/CameraXTorchInstrumentedTest.kt` | `05` [C] |
| Screenshot-test library | none in the build; 34 test files capture an image in a test | `05`, `03` [C] |

**Which kinds have an iOS-simulator equivalent in Compose Multiplatform:**

| Android kind | iOS simulator equivalent | Mark |
|---|---|---|
| Plain JUnit 4 unit test | `kotlin.test` in `commonTest`, run by `iosSimulatorArm64Test`; the port already does this | [R, this repo's `brouter/src/commonTest`, 43 files] |
| Compose UI test with `createComposeRule` | `runComposeUiTest` in `commonTest`: "does not rely on JUnit's `TestRule`"; "The API is Experimental" | [R, Kotlin docs] |
| Robolectric test of an Android class | none; the class it tests is the Android adapter | [I] |
| Instrumented test | none without a device | [I] |
| Image capture in a UI test | not stated on the page fetched | [?] |

The same page says common Compose tests cannot run under Android's local test configuration, only as
`connectedAndroidTest` or on the JVM desktop target. A shared UI test would therefore not replace today's 353
Robolectric Compose tests on Android without a device or a desktop target. [R][I]

## 7. Open decisions

No recommendation is made; none of these has only one option standing.

**The owner's (product, money, public actions):**

1. **Apple Developer Program, and when.** Now: signing and TestFlight can be proven early; costs "$99" a year
   from the day of enrolment. Later: no cost until a signed build is wanted; every iOS check until then is an
   unsigned simulator build.
2. **What the first runnable iOS build contains.** Options the evidence supports: (a) the shell and the screens
   with no platform piece (List, Seasonal, Journal reading); (b) the map first, as the main screen and the
   largest unknown (39 files, a pre-1.0 wrapper); (c) routing first, since the port is nearly done. Each defers
   the others.
3. **The order of the remaining surfaces,** in particular where recording and Back by (safety-relevant, and
   unverifiable without a phone) fall.
4. **Whether the camera, RAW and DNG are in the first iOS release.** In: a second implementation of 2,822 lines'
   worth of behaviour that the simulator cannot exercise. Out: finds carry imported photos only.
5. **Whether Android moves to the shared code piece by piece or after iOS works.** Piece by piece: one copy of
   the safety logic throughout, but each move changes the shipping Android app and its 1,012 test files. After:
   Android is untouched for now, and two copies drift meanwhile. This repository's `CLAUDE.md` says nothing for
   iOS is written to Forager, so the first option also needs the owner's word on where shared code lives.
6. **The fungi index on iOS.** `createFromAsset` is not available off Android. Options: copy the bundled file
   into place before Room opens it; or build the index at first run. Either changes how 1 of the 2 databases
   opens.
7. **Scheduled backup on iOS.** Options: manual backup only; or best-effort background runs at the system's
   choosing. The second cannot promise a schedule.
8. **Device evidence.** No iPhone is ruled. Whether to add a TestFlight tester or a device service, and when
   (the baseline raised both; neither is chosen).
9. **The iOS app's public identity:** its name and bundle identifier, and whether it uses the same tile worker
   as Android (the worker is the project's own; its cost under more traffic was not looked at [?]).

**The planner's (technical):**

1. Whether the port gains a public route-ways entry, and its shape (next-turn needs it).
2. Whether the folder check (`RoutingDataFolder.check`) moves into shared code beside `checkDataFile` or stays
   in each adapter.
3. The map: MapLibre Compose (one API, "not yet stable", iOS "Beta") or MapLibre Native for iOS in a
   `UIKitView` (stable SDK, a second implementation of 39 files' worth). The offline store read and PMTiles
   decide it and are unverified.
4. The module layout under AGP 9.3.1 (the baseline's Finding 4), and how Android consumes a library with no
   Android target.
5. The `java.time` replacement and its order against the 75 files that import it.
6. Room: stay on 2.8.x or follow Google's page to `androidx.room3`; the 19 migrations move to
   `SQLiteConnection` either way.
7. Networking: Ktor in shared code, or keep Retrofit on Android and write iOS clients behind the same ports.
8. Tests: which of the 353 Compose UI tests are rewritten as common tests, given they cannot run under
   Android's local configuration.
9. How the Xcode project is produced on a runner, and what drives the sundown line on iOS without the service's
   15 s tick (the baseline left this to a dispatch).

## Not verified

- No build was run. The "66 files" figure, and any claim that a file compiles as common code, is untested here.
- Apple's pages on background location and on local notifications returned only their titles. Nothing about
  either is confirmed from Apple in this report.
- App Store Connect API keys, creating a signing certificate with no Mac, and whether a simulator build needs
  any account: not fetched.
- MapLibre Compose's current version, how it links MapLibre Native on iOS, and whether its offline API covers
  `MapLibreOfflineMapRepository.kt`, the store read and the PMTiles base map.
- Whether Compose Multiplatform's Material3 has the APIs of Jetpack Material3 `1.5.0-alpha26` (carried from the
  baseline).
- `kotlinx-datetime`, a Kotlin Multiplatform MGRS library, and image capture in common UI tests: not looked up.
- `routing/TrailRoutingEngine.kt`, `routing/WayHomeRouting.kt`, `routing/RoutingProfile.kt`,
  `service/RecordingWatches.kt` and all of `data/routedata/` were listed and not read.
- Which packages are new since the baseline, where no dated document names them.
- The surface table's rows 7 and 8 are named from file names, not from reading the composables.

## Corrections and slips

- The first version of `04-api-scan.sh` tried to write a temporary file inside the export (a leftover line).
  It failed or was removed in the same run; `find -newer` over the export then showed 0 files changed. The line
  was deleted and the scan re-run. The same version had a pattern `grep -E` cannot read (a lookahead), which
  gave 199 for a figure now replaced by "234, of which 21 by `Log` alone".
- A first look for RECORD -129 and -134 searched `origin/main` and found nothing; see premise 3.

## Sources

Read 2026-11-12:
- MapLibre Compose: https://maplibre.org/maplibre-compose/ and https://github.com/maplibre/maplibre-compose
- Android, Kotlin Multiplatform and Jetpack libraries: https://developer.android.com/kotlin/multiplatform
- Android, Room for Kotlin Multiplatform: https://developer.android.com/kotlin/multiplatform/room
- Kotlin, testing Compose Multiplatform UI: https://kotlinlang.org/docs/multiplatform/compose-test.html
- Kotlin, UIKit integration: https://kotlinlang.org/docs/multiplatform/compose-uikit-integration.html
- Ktor, client engines: https://ktor.io/docs/client-engines.html
- GitHub, hosted runners: https://docs.github.com/en/actions/reference/runners/github-hosted-runners
- GitHub, signing Xcode applications: https://docs.github.com/en/actions/how-tos/deploy/deploy-to-third-party-platforms/sign-xcode-applications
- Apple, Developer Program: https://developer.apple.com/programs/
- Apple, compare memberships: https://developer.apple.com/support/compare-memberships/
- Apple, TestFlight: https://developer.apple.com/testflight/

Fetched, text did not render:
- https://developer.apple.com/documentation/corelocation/handling-location-updates-in-the-background
- https://developer.apple.com/documentation/usernotifications/scheduling-a-notification-locally-from-your-app

Local: the Forager export at `12eb9697`; its
`docs/audits/2026-10-26-ios-multiplatform-feasibility-report.md`; this repository's `origin/brouter-port-5b` at
`65737ec` and `RECORD.md` on `origin/ccr-7e682f25-4tqqon`.
