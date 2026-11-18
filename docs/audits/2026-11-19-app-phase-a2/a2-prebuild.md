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
3. `MapStyleReload.kt:126-129` turns it into `Style.Builder().fromUri(...)` / `.fromJson(...)` /
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
- **Source of the Worker:** `W/src/index.ts` (684 lines). It serves the styles (`index.ts:500-520` and on), fonts and
  sprites from R2 (`assetKeyFor`, `index.ts:427-441`; patterns at `:424-425`), tiles from PMTiles archives on R2 through a
  generic `/{name}/{z}/{x}/{y}.{ext}` path, and TileJSON at `/{name}.json` (`index.ts:317`). Routing data is under
  `/routing/` with its own key check (`index.ts:497-499`, `W/src/routing/handler.ts`).
- **Key, header, referer:** none for the map. Read from `index.ts`: the only gate before the style, font, sprite and tile
  routes is "POST is 405" (`:492`); `ALLOWED_ORIGINS` (`:36`, `resolveAllowedOrigin` `:118-126`) only decides the
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
  serves plain z/x/y tiles (`index.ts:145-146`, `:261-281`). Offline, Android uses MapLibre's own offline database, not a
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
