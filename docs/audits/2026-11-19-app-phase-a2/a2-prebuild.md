# A2 pre-build report: the map spike, before anything is built

Plumb Ledger, session 86, written 2026-11-18 (UTC) for the A2 folder dated 2026-11-19. Branch `app`. A report only:
nothing in the product tree or in any workflow changes here. Throwaway experiments ran in the session's scratch folder
and are not committed.

Base, confirmed at start: `git fetch origin`; `git status -sb` printed `## app...origin/app`; `git rev-parse --short HEAD`
and `git rev-parse --short origin/app` both printed `df49ee5`; `git status --porcelain` printed nothing.

Sources: the plan, `git show origin/main:docs/plans/2026-11-12-app-port-plan.md` (Phase A, lines 88-95; open decision 5,
line 166); the survey, `git show origin/research/app-port-survey:docs/research/2026-11-12-app-port-survey.md`; A1's
findings, `docs/audits/2026-11-18-app-phase-a/` on `app` at `df49ee5`. The Android app is read from
`/home/user/Forager` **at `origin/pre-main` = `f29db48`** (fetched today; the local checkout is on another branch,
`137b1427`, so every Android citation below is `git show f29db48:<path>`, and line numbers are that commit's). Forager's
`CLAUDE.md` was read; its only difference between the two commits is the "Main changes only by a promotion" bullet.

The owner's rulings carried, verbatim, not reopened: "Option B stands"; "I don't have a Mac and I don't care to get
one"; "3 no iPhone"; "Map first (Recommended)"; "Forget the renderer for now. I'll think about it another time.";
unsigned simulator builds only; the placeholder identity; "no coordinates are committed" (E1). Every iOS statement
below is at best **simulator-checked**; this report ran nothing on macOS at all.

**E1 in this file.** No coordinate, map centre, bounding box or place-naming tile is written here. Where the Android
code or a served file holds one, the file and line are cited with "a coordinate is here". Version strings that the
team's scan would read as a coordinate (MapLibre Native FFI's `0.` followed by six digits) are written with the leading
`0.` dropped and said so each time.

Paths shortened below: `A/` is `app/src/main/java/com/zynergylabs/forager/app/` in Forager; `W/` is
`server/pmtiles-worker/` in Forager.

## 1. What Android's map is, read from the code

### The library

- **MapLibre Native for Android, `org.maplibre.gl:android-sdk`, version 13.5.0.** `gradle/libs.versions.toml:43`
  (`maplibre = "13.5.0"`) and `:97` (the library entry); applied at `app/build.gradle.kts:565`
  (`implementation(libs.maplibre.android.sdk)`). The pin's comment (`libs.versions.toml:32-42`) says it was confirmed
  against Maven Central's metadata and that a bump means re-taking the credits.
- 49 files under `app/src/main` mention `org.maplibre` at `f29db48` (`git grep -l "org.maplibre"`; 47 Kotlin files plus
  `assets/credits/software.json` and one domain file's comment). The survey counted 39 importers (survey line 158);
  the two counts measure different things (mentions versus imports) and were not reconciled further.
- The map is a classic Android `MapView` inside Compose's `AndroidView` (`A/ui/map/SightingsMapLayout.kt:41`), created
  by `SightingsMap` (`A/ui/map/SightingsMap.kt:147`). So Android itself does not use a Compose map API; it hosts a
  native view. That is the shape of A2's fallback (section 4), not of the wrapper.

### Where the style comes from, and who chooses it

Traced from the caller down:

1. `MapStyleReload.kt:83` calls `requestedMapStyle(...)` (`A/ui/map/BasemapStyles.kt:422`), then `:122` calls
   `mapStyleSourceFor(basemap, night, useOfflineTiles, offlineStyleUrl, ownTrail)` (`BasemapStyles.kt:315`).
2. `mapStyleSourceFor` returns a `MapStyleSource` (`BasemapStyles.kt:242-262`): `Uri` (a style loaded by URL), `Json` (a
   raster style built in code) or `TrailWithTopoOver` (Trail Map by URL with a night raster added).
3. `MapStyleReload.kt:126-130` turns it into `Style.Builder().fromUri(...)` / `.fromJson(...)` /
   `trailWithTopoOverBuilder(...)` and `:134` calls `map.setStyle(builder) { style -> ... }`.
4. The default map type is Trail: `MapMode.DEFAULT = TRAIL` (`A/ui/map/MapMode.kt:64`), and `Basemap.DEFAULT` derives
   from it (`A/ui/map/Basemap.kt:188`). For Trail, by day, online, with the fallback not fired,
   `ownTrailStyleSourceFor` returns `MapStyleSource.Uri(TRAIL_STYLE_URL)` (`BasemapStyles.kt:279`).

So **the project's own style is a hosted vector style document loaded by URL**, `TRAIL_STYLE_URL`
(`A/map/TrailStyle.kt:25`), path `/style/trail-v4.json` on the project's Worker. It is not bundled JSON and not built in
code. The other two map types are raster styles built in code from a tile URL template (`styleJsonFor`,
`BasemapStyles.kt:153`; `singleLayerStyleJson`, `:214`): OpenTopoMap (`Basemap.kt:158-174`) and the Street map
(`ui/map/street/`, an `AddedMapType`, `Basemap.kt:176-181`; not read further). Older Trail styles (v1 to v3, and v4 under
its old name) stay served for offline regions (`TrailStyle.kt:35-59`); the offline style is `/style/offline.json`
(`A/map/OfflineStyle.kt:18`).

Night, the offline store, the `-1513` fallback to a raster when Trail's style fails (`BasemapStyles.kt:273-282`), and
the night topo-over-trail composition are all Android behaviours a first map screen does **not** need.

### The project's own tile server

- **Host.** A Cloudflare Worker on a `workers.dev` subdomain: `forager-pmtiles.<owner's account label>.workers.dev`.
  The full host is in `TrailStyle.kt:25`, `OfflineStyle.kt:18` and `Basemap.kt:154` (and, per the survey's line 235-236,
  in `RouteAreaClient.kt`, `OfflineReliefTileStore.kt`, `OfflineMapRepository.kt`). I have **not** written the account
  label into this repository: it is the owner's personal Cloudflare account name, no file in `Forager-iOS` names it today
  (`git grep workers.dev` on `app`, `origin/main` and `origin/research/app-port-survey` printed nothing), and the survey
  also cited files rather than the host. A2's code would have to commit it (section 7, O1).
- **Source of the Worker:** `W/src/index.ts` (684 lines). It serves the styles (`index.ts:505-530` and on; trail-v4 at `:523`), fonts and
  sprites from R2 (`assetKeyFor`, `index.ts:429-441`; patterns at `:425-426`), tiles from PMTiles archives on R2 through a
  generic `/{name}/{z}/{x}/{y}.{ext}` path, and TileJSON at `/{name}.json` (`index.ts:317`). Routing data is under
  `/routing/` with its own key check (`index.ts:501-503`, `W/src/routing/handler.ts`).
- **Key, header, referer:** none for the map. Read from `index.ts`: the only gate before the style, font, sprite and tile
  routes is "POST is 405" (`:496`); `ALLOWED_ORIGINS` (`:36`, `resolveAllowedOrigin` `:118-126`) only decides the
  `Access-Control-Allow-Origin` response header, which matters to browsers, not to a native app. The routing key
  (`ROUTING_APP_KEY`, `:34`) applies to `/routing/` only. Confirmed by fetching today with no key, no referer and a
  generic user agent (section "What was fetched today" below): every map URL answered 200.
- **Android's own header.** Android replaces MapLibre's `User-Agent` with the app's own (`A/map/MapHttpClient.kt:28-35`,
  installed at `:42-48`, called from `A/map/MapLibreStorage.kt:29`) and adds a tile retry interceptor (`:34`). Nothing on
  the Worker requires that header (above). Whether the third-party raster hosts' policies require an identifying user
  agent was not checked here (A2's first screen uses none of them).

### What the Trail Map style holds (fetched today)

`GET /style/trail-v4.json` returned 200, `application/json`, 361,847 bytes. Read with a script that printed structure
only, never values that could name a place:

- `version` 8, `name` "Forager Trail v4", keys `version, name, metadata, sources, glyphs, sprite, layers`. **No `center`
  and no `zoom`** in the style.
- **Sources (three):** `protomaps`, vector, by TileJSON URL `/us.json` on the Worker (attribution present); `reference`,
  vector, tiles `/reference-trails/{z}/{x}/{y}.mvt`, zoom 10-15 (no attribution field); `dem`, `raster-dem`, tiles
  `/relief/{z}/{x}/{y}.png`, zoom 5-11 (attribution present). All on the Worker. The `{z}/{x}/{y}` here are templates, not
  tiles.
- **Format:** Mapbox Vector Tiles (`.mvt`, `application/x-protobuf`) served from PMTiles archives by the Worker; relief
  is PNG terrain tiles. The app never reads a PMTiles or MBTiles file itself online: the Worker unpacks the archive and
  serves plain z/x/y tiles (`index.ts:144-147`, `:261-281`). Offline, Android uses MapLibre's own offline database, not a
  PMTiles or MBTiles file (`OfflineStyle.kt:3-16`; `MapLibreStorage.kt:7-9`).
- **Sprite:** `/sprites/v6/light` on the Worker (`light.json` and `light@2x.png` both 200 today).
- **Glyphs:** `/fonts/{fontstack}/{range}.pbf` on the Worker; the style asks for Noto Sans Regular, Medium and Italic.
- **Layers:** 182 (85 line, 66 symbol, 29 fill, 1 background, 1 hillshade); the first is the `background` layer.
- **The TileJSON** `/us.json` returned 200 (7,101 bytes) with `tiles` = the Worker's `/us/{z}/{x}/{y}.mvt`, zoom 0-15. It
  carries `bounds` and `center` keys: **coordinates are there** (in the served file; not copied).
- **Attribution:** the style's sources carry it; Android also draws an always-visible caption, Trail's being
  "Protomaps © OpenStreetMap" (`OfflineStyle.kt:28`, reused at `TrailStyle.kt:71`), because MapLibre's own attribution is
  tap-to-reveal (`Basemap.kt:83-94`). A licensing obligation per `OfflineStyle.kt:24-26`.
- The Worker's comments hold coordinates of the archive's edges (`W/src/index.ts:222`, `:325-327`): **coordinates are
  here**.

### Other hosts the map can reach (public, third-party)

- OpenTopoMap raster tiles, host `a.tile.opentopomap.org` (`Basemap.kt:168`).
- The Street map's raster host lives in `A/ui/map/street/` (not read here); the survey names OpenStreetMap tiles
  (survey line 237).
- None is needed for A2's first screen.

### The minimum a first map screen needs

Read from the above, not built:

1. MapLibre Native rendering into a view that fills the screen.
2. One style loaded **by URL**: `TRAIL_STYLE_URL`. Through it, MapLibre fetches the TileJSON, vector tiles, relief
   tiles, sprite and glyphs, all from the one Worker host. Nothing else: no key, no header, no offline store, no
   location, no night.
3. A camera. The style has no centre, so MapLibre's own default applies unless the app sets one (section 6).
4. The attribution: MapLibre's own (on by default in the iOS SDK; unverified for the wrapper) and Android's always-on
   caption, which a later screen owes, not the spike (inferred).

Android's initial camera, for section 6: the Maps tab opens on the search region, else the last location fix, else a
fixed fallback region (`A/ui/availability/CompactMapDisplayRegion.kt:17-23`); the fallback is
`JOURNAL_PICKER_DEFAULT_REGION` (`A/ui/availability/AvailabilityOfflineMapsUi.kt:429-430`), built from a constant at
`AvailabilityOfflineMapsUi.kt:420`: **a coordinate is here**. The camera is moved to the region in
`A/ui/map/MapDataRefresh.kt:94-97` and `:139-142` (`CameraPosition.Builder().target(...).zoom(zoomForRadiusKm(...))`).

### What was fetched today (2026-11-18), with what

`curl` with a generic user agent, no key, no referer, from this container through its proxy:

| URL path on the Worker | Status | Bytes | Type |
|---|---|---|---|
| `/style/trail-v4.json` | 200 | 361,847 | `application/json` |
| `/us.json` | 200 | 7,101 | (TileJSON) |
| `/sprites/v6/light.json` | 200 | 6,682 | |
| `/sprites/v6/light@2x.png` | 200 | 67,331 | |
| `/us/0/0/0.mvt` (the zoom-0 tile: the whole world, not a place) | 200 | 69,760 | `application/x-protobuf` |

This shows the Worker answers a client that is not the Android app. It does not show what a simulator on a GitHub
runner gets (section 5).

## 2. The Compose wrapper today

### What exists

- **MapLibre Compose**, `org.maplibre.compose:maplibre-compose`, repository `https://github.com/maplibre/maplibre-compose`,
  maintained under the MapLibre organisation (POM developer "MapLibre", `gradle.properties` at tag `v0.13.1`; the repo
  is `maplibre/maplibre-compose`). Licence BSD-3-Clause per its `LICENSE` badge (not re-read).
- **Latest release: 0.20.0.** Maven Central's `maven-metadata.xml` for the artifact, fetched today: `<latest>` and
  `<release>` 0.20.0, versions 0.11.0 to 0.20.0, `lastUpdated` 2026-10-10. Tag `v0.20.0` is commit `004a0f3`, dated
  2026-10-09. The default branch had moved on (head `6c52a35`, 2026-11-14) with no newer release.
- **Its iOS status:** "Beta" (the README's Stability table, head `6c52a35`, `README.md:60-66`: Android Beta, iOS Beta,
  macOS native Experimental, Desktop and Web Alpha), with "The public API is still evolving, and minor releases can
  contain breaking changes" (`README.md:56-58`). The survey's "marked beta" (survey line 202) still holds. Before the
  Stability table existed (tags `v0.13.1`, `v0.14.0`), the README showed a progress bar, iOS "90%" (`README.md:45` at
  `v0.14.0`).

### How it ships MapLibre Native on iOS: two different designs, split at 0.15.0

Read from each release's Gradle module metadata on Maven Central and from the tags:

| Line | Kotlin it was built with | Compose MP | How MapLibre reaches iOS | MapLibre on iOS |
|---|---|---|---|---|
| 0.11.x | 2.2.0 | 1.8.2 | the app adds `MapLibre.framework` itself (CocoaPods or SPM) | iOS SDK (classic) |
| 0.12.x | 2.2.21 | 1.9.2 | same | same |
| 0.13.1 (2026-07-05) | **2.3.21** | 1.10.3 | same; the library's own build uses the third-party `spmForKmp` plugin (`buildSrc/src/main/kotlin/util.kt:12-22` at the tag) | iOS SDK 6.25.1 (`gradle.properties`, `maplibreIosVersion`) |
| 0.14.0 (2026-08-07) | **2.3.21** | 1.11.1 | same | iOS SDK 6.25.1 |
| 0.15.0 (2026-08-25) | 2.4.10 | 1.12.0 | **MapLibre Native FFI** klibs from Maven Central, no CocoaPods, no SPM | the C API over MapLibre Native core |
| 0.18.0, 0.19.0 | 2.4.20 | 1.12.0, 1.12.1 | FFI | same |
| 0.20.0 (2026-10-09) | **2.4.20** | 1.12.1 | FFI 202610.0 (written without its leading `0.`) | MapLibre Native core at commit `43e10605` (the FFI tag's submodule), not a tagged iOS release |

Sources: each version's `maplibre-compose-iossimulatorarm64-<v>.module` (its `kotlin-stdlib` and `foundation`
requirements); the tags' `gradle/libs.versions.toml` (`gradle-kotlin`, `gradle-compose`); the FFI tag
`bindings/kotlin/v0.` followed by `202610.0` (split so the scan does not read it as a coordinate), whose
`third_party/maplibre-native` submodule is commit `43e10605cd5b`. The 0.14.0 and 0.17.0 module files did not download
on the first pass (this container's proxy returned 1-byte bodies intermittently; see section 3), so their rows rest on
the tag and on the scratch resolution below.

**0.20.0's iOS shape** (`docs/src/content/docs/getting-started.mdx` at the tag, section "iOS"): "Requires iOS 15.5 or
later"; no CocoaPods or SPM step; the app target adds Other Linker Flags `-l"c++" -lz -framework CoreFoundation
-framework CoreGraphics -framework CoreText -framework Foundation -framework ImageIO -framework Metal -framework
QuartzCore`. The native engine arrives as a Kotlin cinterop klib: `maplibre-native-ffi-runtime-metal-iossimulatorarm64`
202610.0 carries `metal-iosSimulatorArm64Cinterop-maplibreNativeRuntimeMain.klib`, **24,449,551 bytes**, SHA-256
`bbde23ec...87a0` (its `.module` on Maven Central), which is large enough to hold the compiled engine (inferred from
the size and the docs' "iOS needs no runtime artifact"; the klib was not unpacked). The FFI repository describes itself
as "the MapLibre Native map engine as a C API" (`maplibre-native-ffi` `README.md`), with "Until the first part of the
version reaches 1, any release can break the API, the ABI, or documented behavior." Its Apple deployment default is iOS
15.0 (`cmake/platform/apple.cmake:4-10` at head). Note for later: at its head (2026-11-14) the FFI builds from
`mln-rs`, "the Rust port of MapLibre Native" (`cmake/mln_ffi_mln_rs.cmake:1-2`), not from the C++ tree; the 202610.0
tag that 0.20.0 uses still builds from the C++ `maplibre-native` submodule. So the wrapper's next release may carry a
different engine (inferred; not tested).

### Against our pins

Our pins: Kotlin 2.3.10 (`gradle/libs.versions.toml:4`, "moved only together with Forager"), Compose Multiplatform
1.12.2 (`:15`), Xcode 26.2 (`app.yml`), deployment target 16.0 provisional (`iosApp/project.yml:9`), a static framework
(`app/build.gradle.kts:15`), XcodeGen 2.46.0, no CocoaPods. Forager is on Kotlin 2.3.10 too (Forager
`gradle/libs.versions.toml:7` at `f29db48`).

**Run in scratch today (Linux, compile only; the scratch copy's repositories pointed at a local retrying mirror of
Maven Central, Gradle's plugin portal and Google's Maven, explained in section 3; no file of this repository changed).**
`:app` with one extra `commonMain` dependency and a one-line probe, `@Composable fun ProbeMap() { MaplibreMap() }`:

| Wrapper | Command | Result |
|---|---|---|
| 0.20.0 | `:app:compileKotlinIosSimulatorArm64` | **FAILED.** `w: KLIB resolver: Skipping '.../maplibre-compose-iosSimulatorArm64Main-0.20.0.klib' having incompatible ABI version '2.4.0'. The library was produced by '2.4.20' compiler.` then `e: KLIB resolver: Could not find ".../maplibre-compose-iosSimulatorArm64Main-0.20.0.klib"`. Resolution also lifted `kotlin-stdlib` to 2.4.20 and `foundation` 1.12.1 to our 1.12.2. |
| 0.13.1 | `:app:compileKotlinIosSimulatorArm64 :app:compileKotlinIosArm64` | **BUILD SUCCESSFUL in 40s**, no `e:` and no `w:` line. `kotlin-stdlib` resolved to 2.3.21; `foundation` 1.10.3 lifted to 1.12.2. |
| 0.14.0 | the same two tasks | **BUILD SUCCESSFUL in 19s**, no `e:` or `w:`. `kotlin-stdlib` 2.3.21. |

What this shows and does not show: a Kotlin 2.3.10 compiler **refuses** every FFI-line release (0.15.0 onward; 0.20.0
tried, the others inferred from their 2.4.x `kotlin-stdlib` requirement), and **accepts** 0.13.1 and 0.14.0 for
compilation. It does not show that 0.13.1 or 0.14.0 links, runs, or renders: they need `MapLibre.framework` supplied to
the link, and their published Compose (1.10.3, 1.11.1) is older than ours (1.12.2), a mismatch Gradle resolved upward
without complaint at compile time. Whether a library compiled against Compose 1.10.3 or 1.11.1 runs correctly on 1.12.2
is not shown by a compile (binary compatibility of Compose's klibs across minor lines: not verified).

### What would have to move

- **To use the current wrapper (0.15.0 to 0.20.0): Kotlin to 2.4.20 or later.** That pin moves "only together with
  Forager" (`libs.versions.toml:3`), so this is not ours to move; it is an owner and planner decision that touches the
  Android app (section 7, O2). Compose 1.12.2 already meets 0.20.0's 1.12.1. Deployment target 16.0 meets its iOS 15.5.
  No CocoaPods or SPM is needed; the linker flags go into `project.yml`.
- **To stay on Kotlin 2.3.10: wrapper 0.14.0 (or 0.13.1),** the last releases built with Kotlin 2.3.x, which are frozen
  (no fixes reach them: every later release is on 2.4.x), on the classic MapLibre iOS SDK 6.25.1 (or newer, if their
  binding tolerates it: not verified), which our build must supply itself (section 3). Their docs require the Kotlin
  framework to be linked **before** `MapLibre.framework`, "The opposite order can cause broken Compose text rendering on
  iOS because both Compose and MapLibre include HarfBuzz symbols" (`docs/docs/getting-started.md:68-81` at `v0.13.1`,
  citing CMP-8882). Our `OTHER_LDFLAGS` already starts with `-framework ForagerApp` (`iosApp/project.yml`).
- Either way, "beta on iOS" is unchanged.

## 3. The native dependency with no Mac

Which route applies depends on section 2's fork. With the FFI-line wrapper (Kotlin 2.4.x) there is no separate native
dependency to fetch: it is a Maven klib. With wrapper 0.14.0 or 0.13.1, or with the fallback (section 4), MapLibre's
classic iOS SDK, `MapLibre.framework`, must reach the app's link. Three ways, each read today:

### a. Swift Package Manager, declared in `project.yml`

- **XcodeGen 2.46.0 supports it.** `Docs/ProjectSpec.md` at tag `2.46.0` (fetched today from
  `raw.githubusercontent.com/yonaskolb/XcodeGen/2.46.0/Docs/ProjectSpec.md`), section "Swift Package" (line 1245 on):
  top-level `packages:` with `url` and a version form, including `exactVersion: 1.2.1` and `revision: xxxxxx`; a target
  links it with a dependency `- package: <name>` and optional `product:` (lines 604-607, 700-720).
- **The package:** `https://github.com/maplibre/maplibre-gl-native-distribution`, latest stable tag `6.31.0` (tag list
  read today; newer tags are `7.0.0-pre2` and date-named `-globe` builds). Its `Package.swift` at that tag (commit
  `13e41ab`, 2026-09-11) is one `binaryTarget` named `MapLibre`, url
  `https://github.com/maplibre/maplibre-native/releases/download/ios-v6.31.0/MapLibre.dynamic.xcframework.zip`, checksum
  `de3aaa435dd86768b06d90245e630d068dd7eef1491afae7217d1654c52c462a`.
- **Pinned and checksummed?** The package version is pinned by `exactVersion` (a tag, which can be moved) or, stronger,
  by `revision:` (a commit). The binary is checksummed by SPM against the `checksum` in `Package.swift`. I downloaded the
  zip today: **10,312,052 bytes, SHA-256 `de3aaa43...c462a`**, equal to the package's checksum. SPM's checksum for a
  binary target is the SHA-256 of the zip (that equality is the evidence; Apple's docs not re-read).
- **Cost on the runner:** `xcodebuild` resolves packages over the network from github.com at build time (inferred: not
  run). Our generated project is not committed, so no `Package.resolved` is kept; `revision:` stands in for it.

### b. CocoaPods

Not in our build today (A1). The wrapper's own docs warn "CocoaPods will stop receiving new versions of packages in late
2026" (`docs/docs/getting-started.md:86-88` at `v0.13.1`). It would add a tool, a `Podfile`, a workspace and the Kotlin
CocoaPods plugin for one dependency. **Not recommended** (mine).

### c. The xcframework, fetched by `tools/fetch-pinned.sh` and linked by path

- `tools/fetch-pinned.sh <dest> <url> <size> <sha256>` (its usage line, `tools/fetch-pinned.sh:6`) already refuses a
  file unless size and SHA-256 match; A1 uses it for XcodeGen. The pins would be the two figures above (10,312,052 and
  `de3aaa43...c462a`), the same SHA-256 that SPM itself pins, so the two routes check the same bytes.
- Linked in `project.yml` with `- framework: <path>/MapLibre.xcframework` (`ProjectSpec.md:604`, "links to a framework
  or XCFramework") and `embed: true` (`:612`; the default for an application target). It is a **dynamic** framework, so
  it must be embedded in the `.app` (inferred from its name, `MapLibre.dynamic.xcframework`, and the presence of a
  `MapLibre` binary per slice; `file` was not run on it). Unsigned embedding on the simulator: inferred to work, not run.
- The zip unpacks to `MapLibre.xcframework` with slices `ios-arm64` and `ios-arm64_x86_64-simulator`, each a
  `MapLibre.framework` with `Headers` (86 files), `Modules/module.modulemap`, `Assets.car` and `PrivacyInfo.xcprivacy`.
- **This route needs no network beyond one pinned download, adds no package resolution to `xcodebuild`, and reuses a
  tool and a pattern A1 already proved.** My recommendation between a and c: **c** (section 7, T3).

### How Kotlin sees it

- **Wrapper 0.13.1 / 0.14.0:** Kotlin needs no definitions of ours. The wrapper ships its own MapLibre bindings as a
  prebuilt cinterop klib (`maplibre-compose-iosSimulatorArm64Cinterop-MapLibreMain-0.13.1.klib`, 113,666 bytes, listed
  in its `.module`), which is why the compile in section 2 ran on Linux. The framework itself is needed only when the
  executable is linked. Because our Kotlin framework is **static** (`isStatic = true`), the Kotlin link produces an
  archive and does not resolve MapLibre's symbols; Xcode's final link of the app does, against the embedded
  `MapLibre.framework` (inferred from how static archives link; it is the reason the wrapper's docs give linker-order
  advice at the app level). The app's `OTHER_LDFLAGS` would become `-framework ForagerApp -framework MapLibre`, in that
  order (the HarfBuzz note, section 2).
- **The FFI line:** no framework; the app adds the system libraries and frameworks listed in section 2.
- **A cinterop of our own breaks the Linux job, silently.** Tried in scratch: a one-function `probe.def` added to both
  iOS targets of `:app`. The command `./gradlew :app:compileKotlinIosSimulatorArm64` **exited 0**, printed `w: ...
  cross compilation to target 'iosSimulatorArm64' has been disabled because it contains cinterops: 'probe' which
  cannot be processed on host 'linux_x64'`, and the task line read **`> Task :app:compileKotlinIosSimulatorArm64
  SKIPPED`**. `app.yml`'s `app-klibs` job prints the task headers and the `e:` counts but does not fail on `SKIPPED`
  (read from `app.yml`, job `app-klibs`), so it would go green having compiled nothing: a check that passes because it
  never saw the data. Any design with our own cinterop must either move that compile to macOS or make `app-klibs` fail on
  a suffixed header, as `app-framework` already does for its link (`app.yml`, "the link task did not run").

### MapLibre iOS's minimum deployment target

- Classic SDK 6.31.0: `MinimumOSVersion` **12.0** in both slices' `Info.plist` (read today from the unpacked zip).
- Wrapper 0.20.0: "Requires iOS 15.5 or later" (section 2). FFI's Apple default: 15.0 (section 2).
- **None raises our provisional 16.0.** The floor above 16.0, if any, comes from elsewhere: A1 recorded an object in
  the Compose framework built for iOS 18.5 (A1-3, section 19, "What A1 has NOT shown").

### What ran in scratch, exactly

- A copy of `app` at `df49ee5` made by `git archive HEAD` into the session scratch folder, with `brouter/`, `iosApp/`
  and `docs/` removed and `include(":brouter")` commented out, so only `:app` configured. One extra library line in the
  version catalogue, one `implementation(...)` in `app/build.gradle.kts`, one probe file. Nothing was written to the
  repository.
- **The repositories in that copy pointed at a local mirror** (a 50-line Python server in scratch, `127.0.0.1:18080`)
  that forwards to `repo.maven.apache.org/maven2`, `plugins.gradle.org/m2` and `dl.google.com/dl/android/maven2` and
  retries a 404 or a 1-byte body up to six times. Why: through this container's proxy, `curl` on the same Maven URL
  returned `404` with a 1-byte body and then `200` on the next try, and `--refresh-dependencies` failed three times in a
  row on different artifacts each time. The mirror's log for the first wrapper run: 975 answers 200, 3 answers 404
  after retries (artifacts that do not exist upstream, inferred from Gradle succeeding without them), 69 requests that
  needed a second or later try. **This is a property of this container, not of GitHub's runners** (A1's jobs resolve
  from Maven Central directly and have been green).
- Kotlin/Native 2.3.10 for Linux came from the existing `~/.konan` cache. Compile only: no link, no `xcodebuild`, no
  simulator.

## 4. The fallback: MapLibre's own iOS SDK in a native view

### What it takes with our build

Two shapes; the second is my recommendation (section 7, T4):

1. **Kotlin calls MapLibre directly** (`UIKitView { MLNMapView(...) }` written in Kotlin). Kotlin then needs bindings for
   `MapLibre.framework`: our own cinterop `.def` over its headers. Section 3 showed what that does to CI: the Linux
   `app-klibs` compile is **skipped**, exit 0, for both iOS targets. And cinterop needs the framework's headers at build
   time, so the pinned xcframework would have to be fetched before Gradle runs on macOS too. Workable, but it changes
   what the Linux job proves.
2. **Swift makes the map view; Kotlin only hosts it.** The SwiftUI side already exists
   (`iosApp/ForagerApp/ForagerApp.swift`: a `UIViewControllerRepresentable` around `MainViewControllerKt.MainViewController()`).
   Swift `import MapLibre`, builds an `MLNMapView` and hands Kotlin a factory through a Kotlin interface the Swift side
   implements; Kotlin places it with Compose's `UIKitView`. Kotlin never names a MapLibre type, so there is no cinterop
   and the Linux job keeps compiling everything. Shown in scratch today: an `iosMain` file with
   `fun interface NativeMapFactory { fun make(): UIView }` and
   `UIKitView(factory = { factory.make() }, modifier = Modifier.fillMaxSize())` (`androidx.compose.ui.viewinterop.UIKitView`,
   `@OptIn(ExperimentalForeignApi::class)`) compiled with our pins: `:app:compileKotlinIosSimulatorArm64` **BUILD
   SUCCESSFUL in 14s**, no `e:` or `w:`. Not linked, not run.

The MapLibre API the Swift side would use, read today from the 6.31.0 headers (simulator slice):
`initWithFrame:styleURL:` (`MLNMapView.h:210`); delegate methods `mapView:didFinishLoadingStyle:`
(`MLNMapViewDelegate.h:321`), `mapViewDidFinishLoadingMap:` (`:206`), `mapViewDidFailLoadingMap:withError:` (`:219`,
"including a network connection failure or a failure to fetch the style"), `mapViewDidFinishRenderingMap:fullyRendered:`
(`:225`, documented only as `// TODO`), and `mapViewDidBecomeIdle:` (`:299`: "No camera transitions are in progress / All
currently requested tiles have loaded / All fade/transition animations have completed"). Request hooks:
`MLNNetworkConfigurationDelegate`'s `willSendRequest:` and `didReceiveResponse:` (`MLNNetworkConfiguration.h:29-31`).

Build changes for this shape: the pinned xcframework (section 3c) in `project.yml` as a framework dependency, embedded;
the Swift file grows a map view and its delegate; `MainViewController` takes the factory. Nothing in Gradle.

What is lost against the wrapper: the map is not a Compose API, so everything a later screen puts on the map (Android's
39 files of layers and camera code, survey line 202) is written against MapLibre's Objective-C API in Swift, or bridged
to Kotlin by hand. That is the survey's "a second implementation of 39 files' worth" (survey line 384-385), and it is why
the plan tries the wrapper first.

### What counts as "the wrapper failed": a stated trigger

Proposed (mine, for the planner to rule on; section 7, T2). The wrapper is declared failed for A2, and the fallback
dispatched, when **any one** of these is seen in CI, each on a build shown to have compiled (A1's discipline: the
`BUILD SUCCEEDED` line and zero compile-error lines in the same job):

- **F1. It cannot be built with our pins.** Already true of the current wrapper line (0.15.0 to 0.20.0) under Kotlin
  2.3.10 (section 2). If the owner does not move Kotlin, F1 applies to that line now, and only 0.14.0 (or 0.13.1) is
  left to try.
- **F2. It links but does not launch:** no drawn-frame line within the launch step's limit, or a crash report naming
  the app, on two runs.
- **F3. It launches but the map does not render the project's style:** the wrapper's load-failed callback fires with the
  network shown healthy (section 5, check N), or the map-rendered check (section 5, R2 and R3) fails while the same
  style renders in the fallback's own spike. The last clause matters: a failure the fallback shares is the style's or
  the network's, not the wrapper's.
- **F4. Compose breaks around it:** the HarfBuzz text issue the wrapper's docs warn of, seen as a failed text check, if
  one is added (none is proposed for A2).

Not triggers: "beta" labels, API churn, or a slow job. Each trigger is a CI line, so the switch is a decision with a
cause on record, not drift.
