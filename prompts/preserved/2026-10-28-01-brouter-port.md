# Dispatch 2026-10-28-01: BRouter in Kotlin for Forager-iOS, Phase 1: verify and plan (a coder, Opus)

Written by the Forager-iOS planner, a cloud session working for the owner, 2026-10-28. It is not Rowan
Waymark, who keeps Forager's own record. RECORD -3 (this repository's record), building on -1 and -2.
Paths and lines in Forager are read at **Forager `pre-main` `e2969abe4f98674c24ce3913ce97be568184a4de`**.
Forager-iOS `main` is the commit that adds this file. Verify both are still the remote heads before you
start, and say so if either is not.

**Authorisation.** The owner, verbatim:
- 2026-10-27: "Port BRouter to Kotlin".
- 2026-10-27, on the port's three open choices: "a recommended, b Forager-only, c download in CI".
- 2026-10-28: "Go ahead and have a coder start the port on a separate branch".
- 2026-10-28, on who dispatches it: "Option 2. But keep the record separate. Start the record for our iOS
  app with this port, the Android version stays separate. Save work on Forager-iOS repo".

The choices those rulings answered, and the evidence behind them, are in Forager's
`docs/audits/2026-10-26-ios-multiplatform-feasibility-report.md` (on Forager `pre-main`, last changed at
`c8b88248b`), sections "Update, 2026-10-27: routing ruled, and what the port involves" onward. Read it.

Describe products generically. Commits end with `Co-Authored-By: Claude <noreply@anthropic.com>` and no
model name.

## Change

**Phase 1 (this dispatch) changes nothing in any app.** It produces one report and stops.

**Phase 2 (on the planner's go, after the owner rules on Phase 1's questions)** builds, in this repository:
- a Kotlin Multiplatform port of the part of BRouter v1.7.10 that Forager's calls reach;
- compiled for the JVM and for iOS;
- proven to route identically to the Java engine on the JVM, with the Java engine as a test-only oracle.

Nothing in slayer8366/Forager changes, in either phase. Whether and when Forager's Android app switches to
the port (ruling a's last step) is outside this dispatch.

## Scope boundary

- **Writes:** slayer8366/Forager-iOS only, on branch **`brouter-port`**, cut from Forager-iOS `main` at
  the commit that adds this file. Phase 1 writes only `docs/audits/2026-10-28-brouter-port-verify.md`
  and an evidence folder beside it (`docs/audits/2026-10-28-brouter-port-verify/`). Commit and push
  after every step.
- **Reads:** slayer8366/Forager at the commit above (a read-only clone is at `/home/user/forager`);
  BRouter's own repository at tag `v1.7.10`; Maven and Google Maven metadata; public documentation.
- **Not:**
  - slayer8366/Forager, in any way: no branch, no PR, no comment, no file;
  - Forager-iOS `main`, `RECORD.md`, `prompts/`;
  - any account (Apple, Cloudflare, GitHub settings);
  - any phone;
  - downloading map tiles, in Phase 1.

## Baseline

- **Forager `pre-main`:** `e2969abe4f98674c24ce3913ce97be568184a4de`. BRouter, Forager's routing code and
  `domain/routing/` are unchanged there since `b2c4951e`, where the report measured them (counted from the
  diff at writing).
- **BRouter source:** v1.7.10, vendored unmodified in Forager's `third_party/brouter/` from upstream
  commit `4d2639af77ea5ed9c30d3e400764eb6f9e8522da` (`third_party/brouter/README.md`).

Every claim below is a premise to re-check, not a fact.

## Verify first (premises, at Forager `e2969abe4`)

`A` is `app/src/main/java/com/zynergylabs/forager/app/`; `B` is
`third_party/brouter/src/main/java/btools/`.

1. **Forager imports seven BRouter classes:**
   - `RoutingEngine`, `RoutingContext`, `RoutingParamCollector`, `OsmTrack`, `OsmNodeNamed` in
     `A/routing/BRouterTrailRouter.kt:3-7`;
   - `PhysicalFile`, `DataBuffers` in `A/routing/RoutingDataFolder.kt:3-4`.
2. **Forager does not use BRouter's thread.**
   - `B/router/RoutingEngine.java:32` declares `extends Thread`;
   - Forager calls `engine.doRun(maxRunningTimeMillis)` directly on a coroutine
     (`BRouterTrailRouter.kt:170`), and cancels with `engine.terminate()` (`:174`).
3. **Forager reads only the track's points:** `track.nodes` and `getILat()`/`getILon()`
   (`BRouterTrailRouter.kt:105-114`). It does not call BRouter's output formats.
4. **The path model comes by reflection, and Forager's profile does not use it.**
   - `Class.forName(className)` at `B/router/RoutingContext.java:91`;
   - the name comes from the profile (`B/expressions/BExpressionContext.java:979`);
   - the pinned `app/src/main/assets/routing/profiles2/hiking-mountain.brf` has no model line, so the
     default `StdModel` applies (`RoutingContext.java:88`).
5. **Class-level reach** (the report, computed with a script that treats any reference to a class as
   reaching all of it): 76 of 101 classes and 16,296 of 19,327 lines. That set includes `B/router/Format*.java`
   (1,562 lines) and `B/router/VoiceHint*.java` (1,342 lines), which Forager does not read. The
   ~13,400-line estimate for a Forager-only port is **unverified**.
6. **JDK use:**
   - 37 of the 76 reachable files import `java.*`;
   - `RandomAccessFile` appears in 2 files;
   - `TreeMap`, `TreeSet`, `NavigableMap` and `SortedSet` are used;
   - date and number formatting is used, mostly in output code.
7. **Today's comparison with BRouter's own router** is 2 routes on one tile:
   - `app/src/test/resources/routing/segments/W75_N40.rd5`, 152,665 bytes, SHA-256
     `ff6d9d8e439c3cdf914b1566619f64466d9af993c28070ea893425e7f5ec5990`;
   - with `brouter-cli/*.geojson` written by `make-expected.sh` (`app/src/test/resources/routing/README.md`).
8. **The pinned-download precedent:** `server/pmtiles-worker/test/rd5/fetch-brouter.sh:11-12` (the URL and
   SHA-256 of `brouter-1.7.10.zip`) and `:28` (the check).
9. **The usual tile source** is `https://brouter.de/brouter/segments4/`
   (`docs/navigation/2026-10-03-trail-navigation-research.md:58`). Whether it serves stable bytes at one URL
   is **unverified**.

## What the Phase 1 report answers

Mark every claim **read** (file and line at the stated commit), **computed** (cite the script or log in the
evidence folder), **inferred** or **could not determine**.

- **A. Method-level reach.**
  - Start from exactly the calls Forager makes (list them, with the parameters `BRouterTrailRouter.kt`
    and `RoutingDataFolder.kt` set) and the pinned profile.
  - Give which classes and methods run, as the port list (class, methods kept, lines), with its total
    against the ~13,400 estimate.
  - Say how it was found, and what that method cannot see (reflection, profile-driven branches, error
    paths).
- **B. A replacement for each JDK API on that list,** in Kotlin common code. Each candidate library gets:
  - its exact version, read from Maven metadata;
  - confirmation that it publishes `jvm`, `iosArm64` and `iosSimulatorArm64`.

  Cover in particular:
  - random-access reads of rd5 files;
  - the sorted maps and sets;
  - the `volatile` terminate flag;
  - `System.currentTimeMillis` and other time reads;
  - any formatting that stays.
- **C. The project layout:**
  - the Gradle version;
  - the Kotlin version (Forager pins 2.3.10; say whether to match it);
  - the module or modules, and the targets;
  - package names (keep `btools.*` for traceability to upstream, or rename);
  - where BRouter's MIT notice goes.

  Recommend one of each.
- **D. The oracle.** How the Java engine runs in tests on the JVM. The options are the 1.7.10 release jar,
  fetched and checked against the same SHA-256 as `fetch-brouter.sh:12`, or the source copied from
  Forager's `third_party/brouter/`. Recommend one.
- **E. The differential tests,** written down before any result is read (Forager planner rule: "Fix
  choices before data"):
  - **The inputs:** route pairs, tiles, and the failure cases (no route found, the time limit, a cancel
    mid-search, a degree line crossed).
  - **What "identical" means:** the node list exactly, the cost, and the failure kind.
  - **The sample size.**
- **F. The test tiles in CI** (ruling c). Where does a pinned copy live? Check with directory listings and
  HTTP `HEAD` requests only (no tile downloads) whether `brouter.de/brouter/segments4/` keeps a tile's
  bytes stable: `Last-Modified`, `ETag`, any dated archive. Give options with a recommendation. **This is
  a stop:** it needs a ruling.
- **G. CI.** What runs where:
  - JVM tests and the iOS klib compile on `ubuntu`;
  - iOS simulator tests on a GitHub macOS runner (free on public repositories; confirm Forager-iOS is
    public).

  Say when each job is worth adding.
- **H. Floating point.** List the double arithmetic on the reached path whose result can choose between
  routes. Say whether the JVM and Kotlin/Native could differ there, and how to test that on iOS, in the
  simulator in CI.
- **I. The Phase 2 order.** Packages bottom-up, each step with its tests, its size and its push points.
- **J. The mechanism prediction:** how the port will make routes identical. It is written before any
  building, and the planner adds it to the record.
- **The four disclosure sections:** confirmed vs inferred; could not determine; premises that were wrong;
  decided beyond scope.

## Predictions

- **Outcome (planner), written before Phase 1:**
  - the Forager-only port is between 11,000 and 14,000 lines;
  - random-access file reads and the sorted maps are the only JDK pieces needing a library or new code;
  - the release jar serves as the oracle;
  - brouter.de regenerates tiles in place, so a pinned copy has to be hosted elsewhere;
  - identical routes on the JVM are reachable, and float differences are the main risk on iOS.
- **Mechanism (coder):** in the Phase 1 report, item J.

## Evidence required

- **Phase 1:**
  - scratch builds are allowed **outside** the repository, each cited with its log in the evidence folder;
  - counts are checked against something outside the counting script (CLAUDE.md, "A check that passes
    because it never saw the data that could fail it").
- **Phase 2, for when it is dispatched:**
  - tests first, over stubs, each seen failing for its reason;
  - revert checks under CLAUDE.md's runner rules: compile log first, restore from a saved copy, forward
    change confirmed afterwards;
  - counts read from the JUnit XML.

## Stops

Stop and report; never choose:
- an owner-level choice, F among them;
- a wrong premise;
- anything that would change slayer8366/Forager;
- a licence question beyond carrying BRouter's MIT notice and OpenStreetMap's ODbL attribution;
- anything needing an account or an agreement;
- a moved base that changes a premise;
- two failed attempts at one symptom.

## Finish line

Phase 1 is done when the report and its evidence folder are pushed on `brouter-port` and the final message
is sent. Then stop. The planner files the report beside this dispatch and brings its choices to the owner;
Phase 2 waits for the go.

## Final message

- The report's path and commit.
- One line per item, A to J.
- The stops hit.
- Every call made beyond this dispatch.
- What is unverified, said plainly.

## Working here

- **This runs in a cloud container, not the owner's laptop.**
  - Forager's clone: `/home/user/forager`, read only.
  - Forager-iOS clone: `/home/user/forager-ios`, push allowed on `brouter-port`.
- **Gradle may run in this container** for scratch probes. Forager's "CI only" rule is about the laptop's
  memory, which does not apply here. This is the planner's call, recorded at -3.
- **Scratch** goes in
  `/tmp/claude-0/-home-user/c9851708-fbbe-5ce6-a5f8-ed278a238cce/scratchpad/hazel/`, never in either
  repository. Delete it when the task ends.
- **Maven Central** may answer 429. The mirror
  `https://maven-central.storage-download.googleapis.com/maven2/` worked in this session.
- **The Restricted Object:** never read, name or quote it.
- **Long conversation:** past about 200k tokens, push and hand off.
- **Ambiguity:** stop and report.
