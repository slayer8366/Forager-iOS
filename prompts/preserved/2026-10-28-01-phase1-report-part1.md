# BRouter port, Phase 1: verify and plan (partial: the premises and item A)

Dispatch 2026-10-28-01 and its Amendment 1 (RECORD -3, -4), restarted at -5. Written by Hazel Cairnwright, the
second session on this dispatch, 2026-10-28.

**Status: partial.** This session stopped at the dispatch's long-conversation limit ("past about 200k tokens,
push and hand off", dispatch "Working here"), after the premises and item A. **Items B to K are not answered
here.** What this session found that bears on them is under "Found on the way". It is input for the next
session, not an answer to those items.

**Marks.** **[R]** read, with file and line at the stated commit. **[C]** computed, with the evidence file under
`2026-10-28-brouter-port-verify/`. **[I]** inferred. **[?]** could not determine.
- Forager paths are at `pre-main` `e2969abe4f98674c24ce3913ce97be568184a4de`.
- `A` is `app/src/main/java/com/zynergylabs/forager/app/`.
- `J` is `third_party/brouter/src/main/java/btools/`. It is byte-identical to upstream BRouter `v1.7.10`, so `J`
  line numbers hold for upstream too.

## Bases

- **Forager `pre-main` is still `e2969abe4`** on the remote, and the local clone is at it **[C: `00-bases.txt`]**.
- **Forager-iOS `main` has moved past the dispatch commit `0bfbad9`,** by two commits:
  - `d02932b`, Amendment 1;
  - `507dbfb`, RECORD -5.

  They change only `RECORD.md` and add the amendment file **[C: `00-bases.txt`]**. Neither changes a premise.
  `brouter-port` was at `0bfbad9` when this session started.
- **The upstream clone the first session left** (scratch, `hazel/upstream/brouter`) checks out:
  - HEAD `4d2639af77ea5ed9c30d3e400764eb6f9e8522da`;
  - tag `v1.7.10`, matching the remote tag;
  - no local or ignored changes.

  **[C: `00-bases.txt`]**
- **The vendored tree is identical to upstream,** checked with `third_party/brouter/README.md`'s own check:
  - all 101 files compared, 0 differ;
  - the module counts are 27 + 11 + 10 + 20 + 33;
  - `LICENSE` is identical.

  **[C: `01-vendored-vs-upstream.txt`]**

## Premises (dispatch "Verify first", and Amendment 1 item C)

Each line was read at `e2969abe4` **[R: `02-premise-reads.txt`, which quotes every line cited here]**.

| # | Premise | Finding |
|---|---|---|
| 1 | Seven classes imported, at `BRouterTrailRouter.kt:3-7` and `RoutingDataFolder.kt:3-4` | **Confirmed.** No other main-source file imports `btools`. One test imports `RoutingEngine` (`app/src/test/.../routing/BRouterTrailRouterCancelTest.kt:3`); see premise 3. |
| 2 | Not BRouter's thread | **Confirmed:** `J/router/RoutingEngine.java:32` `extends Thread`; `BRouterTrailRouter.kt:170` `doRun`, `:174` `terminate()`; `volatile` flag at `RoutingEngine.java:69`. |
| 3 | Forager reads only the track's points (`:105-114`) | **Wrong in part.** It also reads `track.distance` (`BRouterTrailRouter.kt:115`, as `lengthMetres`). It calls `getErrorMessage()` (`:136`) and `getFoundTrack()` (`:142`), and writes `quite = true` (`:161`). `firstRoutePoint` reads `track.nodes.firstOrNull()` (`:93`). The cancel test subclasses `RoutingEngine`, overrides `doRun` and reads `isTerminated` (`BRouterTrailRouterCancelTest.kt:58-64`, `:81`). The rest stands: no output format is called. |
| 4 | Path model by reflection, unused | **Confirmed:** `RoutingContext.java:86-97` (`:88` default `StdModel`, `:91` `Class.forName`), `BExpressionContext.java:979`, `MODEL_TAG` `"---model:"` at `:31`. The profile has 0 `---model:` lines and matches its pins (`f2bed1a5…`, `60e59d08…`). |
| 5 | Class-level reach 76 of 101 classes, 16,296 of 19,327 lines; ~13,400 unverified | **The class-level figures are confirmed exactly** by a different tool: a `jdeps -verbose:class` closure from the seven classes gives 76 source files and 16,296 lines **[C: `03-reach/out/class-level-closure.txt`]**. The 19,327 total matches `wc -l`. The ~13,400 estimate is replaced by item A's method-level figures. |
| 6 | JDK use | **Source-level counts confirmed:** 37 of the 76 import `java.*` **[C]**. `RandomAccessFile` appears in 2 files, `OsmFile.java` and `PhysicalFile.java` **[R]**; in bytecode `NodesCache` uses the type too **[C: `T3/jdk-use.txt`]**. **On the method-level path the picture differs:** see "Found on the way", JDK use. |
| 7 | Today's comparison, 2 routes on one tile | **Confirmed:** 152,665 bytes, SHA-256 `ff6d9d8e…5ec5990`; two `brouter-cli/*.geojson`, written by `make-expected.sh:14-22`. |
| 8 | Pinned-download precedent | **Confirmed:** `fetch-brouter.sh:11-12` (URL, SHA-256) and `:28` (check). It also pins the size (`:13`, checked `:23-27`). |
| 9 | Tile source | **The citation is confirmed:** `docs/navigation/2026-10-03-trail-navigation-research.md:58` names `https://brouter.de/brouter/segments4/` "(rd5 files dated 2026-10-03)". **Byte stability was not checked** (item F, not reached). |
| C | Forager's build (Amendment 1) | **All confirmed:** AGP 9.3.1 (`gradle/libs.versions.toml:2`), Kotlin 2.3.10 (`:7`), KSP 2.3.10 (`:48`), `android.builtInKotlin=false` (`gradle.properties:9`), Gradle 9.7.0 (`gradle/wrapper/gradle-wrapper.properties:3`), `:brouter` (`settings.gradle.kts:21-22`). |

## A. Method-level reach

### Forager's calls, the entry points

Read from `A/routing/BRouterTrailRouter.kt` and `A/routing/RoutingDataFolder.kt` **[R]**. Written out as
`03-reach/tools/entries.txt`.

**Route request** (`BRouterTrailRouter.buildEngine`, `:146-163`, and `routingEngine`, `:212-213`):
1. `new RoutingParamCollector()`.
2. `getWayPointList("<startLng>,<startLat>|<endLng>,<endLat>")`: exactly two points, no third field (`:149`).
3. `new RoutingContext()`.
4. `setParams(context, waypoints, {profile: <dir>/hiking-mountain.brf, alternativeidx: "0", profile:use_dynamic_range: "0"})`
   (`:151-159`).
5. `new RoutingEngine(null, null, routeData, waypoints, context, BROUTER_ENGINEMODE_ROUTING)`.
6. `quite = true` (`:161`).

**Run and cancel:** `doRun(30_000)` on its own thread (`:170`, `MAX_RUNNING_TIME_MILLIS` `:199`);
`terminate()` on cancel (`:174`).

**Reads:**
- `getErrorMessage()` (`:136`) and `getFoundTrack()` (`:142`);
- `track.nodes` (`:93`, `:105`, `:108`, `:114`), each node's `getILat()`/`getILon()` (`:99`, `:114`);
- `track.distance` (`:115`).

**Data check:** `new PhysicalFile(file, new DataBuffers(), -1, -1).close()` (`RoutingDataFolder.kt:49`), on
every `.rd5` in the folder before the engine runs.

**Test only:** `isTerminated()`; a subclass of `RoutingEngine` overriding `doRun` (`BRouterTrailRouterCancelTest.kt:58-81`).

### How it was found

1. **Upper bound, static.**
   - The vendored source was compiled with `javac --release 11`, as Forager's `:brouter` compiles it
     (`third_party/brouter/build.gradle.kts`). The compile was clean: an empty log, `03-reach/out/javac-vendored.log`.
   - Rapid type analysis ran over `javap -v -p` of all 115 class files (`03-reach/tools/reach.py`):
     - a method is reached from an entry point or a reached method;
     - a virtual call reaches an override only in a class that reached code instantiates;
     - a static initializer is reached with its class's first use;
     - an override of a JDK method in an instantiated class (`equals`, `hashCode`, `compare`, `run`,
       `toString`, ...) counts as reached, because the JDK may call it.
2. **Line spans** come from the JDK's own parser (`com.sun.source`, `03-reach/tools/SourceSpans.java`).
   - A method's span runs from its modifiers to its closing brace, plus the comment block directly above it.
   - "Kept lines" are each file's lines minus the spans of unreached methods and unkept classes. Fields, imports
     and class braces are always kept.
   - Checked by hand on `J/router/OsmNodeNamed.java`: 100 lines, less 26-33, 35-81 and 83-99, leaves 28, the
     tool's figure.
3. **Cuts, in tiers** (`03-reach/tools/exclusions.txt`). Each cut is a call edge or a method, with the line
   whose condition Forager's call, its environment, or the pinned profile fixes:
   - **F**, Forager's own call:
     - engine mode ROUTING;
     - `outfileBase`/`logfileBase` null and `quite`;
     - a request map with three keys and two waypoints;
     - alternative 0;
     - the never-started thread;
     - fields nobody assigns (`rawTrackPath`, `boundary`).
   - **E**, the environment Forager never creates:
     - no `debug.txt`/`stacks.txt` beside the profile folder;
     - so `hasInfo()` is false and logging is off.
   - **S**, JVM system properties Forager never sets (`disableDirectWeaving`, `earlyDuplicateCheck`, `reportFormat`).
   - **P**, the pinned profile:
     - foot mode, so no turn restrictions and no `solveCubic`;
     - `correctMisplacedViaPoints` off;
     - `turnInstructionMode` left at 0, so `processVoiceHints` returns at `OsmTrack.java:457-459`.
4. **Lower bound, dynamic.**
   - `03-reach/tools/ForagerCalls.java` makes Forager's calls in Forager's order under JaCoCo 0.8.14.
   - It runs the cases Forager's own routing tests use:
     - the two fixture routes, plus one route repeated (warm profile cache);
     - two snaps (a point to itself), and a full-route snap;
     - an end and a start with no trail near;
     - an island;
     - a 1 ms time limit, and a terminate before the search;
     - a missing folder;
     - four damaged files (cut to 100,000 bytes, cut to 150 bytes, a header byte, the tile data).
   - Its outputs match Forager's tests and BRouter's command-line router:
     - 226 points / 5,047 m and 306 / 6,713 m, as in the `brouter-cli/*.geojson` "track-length" and point counts;
     - the same messages `BRouterTrailRouterTest` names;
     - the cut-to-150 file's empty success, which `RoutingDataFolder.kt:15-18` describes.

     **[C: `03-reach/out/harness-run.log`]**
5. **The check against something outside the counting script.** Each tier's static set was compared with the
   methods JaCoCo saw run (`compare_dynamic.py`). **In every tier, 0 methods ran that the static set lacks**, so no
   cut removed code that runs on this sample **[C: `out/T*/compare-with-jacoco.txt`]**.

### Results

| Tier | Methods reached | Files with code kept (their lines) | Kept lines | of which code | Statically reached, did not run on the sample |
|---|---|---|---|---|---|
| Class level (the report's method) | n/a | 76 (16,296) | 16,296 | n/a | n/a |
| T0: no cuts | 640 | 75 (16,243) | **14,700** | 11,522 | 230 |
| T1: F | 515 | 73 (15,925) | **11,759** | 8,975 | 111 |
| T2: F, E, S | 481 | 72 (15,847) | **11,218** | 8,523 | 78 |
| T3: F, E, S, P | 450 | 72 (15,847) | **10,014** | 7,505 | 47 |
| Ran on the sample (JaCoCo) | 379 | 58 classes | (3,383 executable lines covered of 10,231 in all of BRouter) | | |

**[C: `03-reach/out/T*/summary.txt`, `out/jacoco.csv`]**

Notes on the table:
- **T0 is 1 file and 53 lines below the class-level count.** `J/codec/IntegerFifo3Pass.java` (53 lines) is
  referenced, but by nothing reached **[C]**.
- **"Kept lines" counts as `wc -l` does,** like the 16,296 and the ~13,400 estimate. "Code" excludes blank and
  comment-only lines.
- **The ~13,400 estimate sits between T0 and T1.**
  - The cuts that Forager's own arguments decide (T1) give 11,759.
  - Adding the environment and system properties (T2) gives 11,218.
  - Tying the port to the pinned profile (T3) gives 10,014.
  - Against the planner's predicted 11,000 to 14,000: T1 and T2 fall inside it, and T3 falls below it **[C]**.
- **Per package, at T3:**
  - `router` 4,272 kept lines in 25 files (122 methods);
  - `mapaccess` 2,142 in 16 (96);
  - `expressions` 1,562 in 8 (87);
  - `util` 1,488 in 14 (76);
  - `codec` 550 in 9 (30).

  **[C: from `out/T3/port-list.tsv`]**
- **The largest files at T3 (kept lines, methods kept of methods in file):**
  - `RoutingEngine` 1,584 (28/50);
  - `BExpressionContext` 814 (36/55);
  - `RoutingContext` 541 (20/22);
  - `OsmPath` 426 (18/21);
  - `BExpression` 406 (14/14);
  - `NodesCache` 394 (14/16);
  - `OsmTrack` 375 (10/27);
  - `StdPath` 335 (10/11).

  **[C]**

**The port list** (class, methods kept, lines) is `03-reach/out/T3/port-list.tsv` for T3 and
`out/T1/port-list.tsv` for T1. Every reached method, with its reason and span, is in `reached-methods.tsv`
beside it.

**Recommendation [I]: port T2, with T3's profile cuts as explicit "unsupported" failures.** Where a
T3-cut feature would be switched on, the port should fail with a named error rather than silently route
differently (CLAUDE.md, "an unsupported feature ... returns an explicit 'unsupported'"). That covers a profile
turning on turn restrictions, `correctMisplacedViaPoints`, turn instructions or bike mode. Whether the port
may be tied to the pinned profile at all is a scope question under ruling b. It is listed under "Decided beyond
scope" for the planner and owner, not decided here.

### What this method cannot see

- **Reflection.** `RoutingContext.java:91` is the only reflective load on the path. It is dead for the pinned
  profile (premise 4). `StackSampler.main`'s reflection is not reached **[R]**.
- **Branches within a reached method.**
  - The static set is per method: a reached method keeps all its branches.
  - The cuts above are per call edge, each read by hand from its guard.
  - Inside kept methods there is more code Forager never runs, which this count still includes. Examples:
    - the `debug.txt` branch of `RoutingEngine.<init>` (`:112-132`);
    - `readBinary`'s body after `filename != null` (`OsmTrack.java:235`);
    - the unknown-value branch of `addLookupValue` (`BExpressionContext.java:572-696`).
  - So T1 to T3 are still upper bounds **[I]**.
- **Profile-driven and data-driven paths.**
  - The static analysis ignores conditions, so these are in the set.
  - The dynamic run sees only what one 152 KB tile and these cases reach. 47 of T3's methods never ran on it
    (`out/T3/compare-with-jacoco.txt`), and they are exactly what item E's sample has to reach:
    - the search's memory-pressure path (`OsmNodesMap.canEscape`/`collectOutreachers`, `RoutingEngine.java:2006-2030`);
    - cache garbage collection (`MicroCache.collect`/`unGhost`, `OsmFile.collectAll`/`cleanGhosts`);
    - turn-restriction decoding (`OsmNode.addTurnRestriction`, `TurnRestriction.<init>`);
    - `mergeTrack`, `cleanOnOOM`;
    - parts of the expression evaluator (`divide`, `min`, foreign variables).
- **Error paths** are in the static set, because the tool counts exception handlers as code. Which JDK
  exceptions can actually reach them is not modelled.
- **What the JDK calls back.** These are approximated by "every override of a JDK method in an instantiated
  class", then narrowed by hand:
  - `toString` overrides are kept only where a reached `String.valueOf(Object)` or concatenation site could take
    the type (sites listed in `out/T*/summary.txt`);
  - `Thread.run` is dropped because nothing starts an engine.
- **Forager's side** was read from Kotlin source, not from its compiled bytecode **[R]**.

## Found on the way (input for B to K; not answers to them)

- **JDK use on the reached path** is computed from the bytecode of the reached methods **[C: `out/T*/jdk-use.txt`]**.
  - **JDK classes per tier:** T0 67, T1 55, T2 48, T3 48.
  - **Dropping out by T1 or T2:**
    - `java.text.*` (`DateFormat`, `SimpleDateFormat`, `DecimalFormat`, `NumberFormat`);
    - `TimeZone`, `Locale$Builder`, `Random`, `TreeMap`;
    - the `Data*`/`Buffered*` output streams.
  - **Sorted collections at T1-T3:** only `TreeSet`/`SortedSet`, at one site.
    - The site is `RoutingEngine.recalcTrack`, `:1568`, sorting `directMap`'s integer keys.
    - `directMap` is empty unless there is direct (beeline) routing, which Forager's calls never produce.
    - `TreeMap` is used only on the round-trip path (`AreaReader.java:43`) and in unreached statistics code
      (`StatCoderContext.java:42`).
    - `NavigableMap` is used only in `BExpressionContext.dumpStatistics` (`:450-483`), which is not reached
      **[R][C]**.
  - **Number formatting still in a reached method:** `String.format(Locale.US, "%3.1f", ...)` in
    `BExpressionContext.addLookupValue` (`:612-679`).
    - Its only caller is `parseMetaLine` (`:299-304`), which parses `lookups.dat`.
    - The unknown-value branch holding it (`:572-696`) did not run on the pinned file **[C: JaCoCo line data]**.
    - Parsing a pinned file is deterministic, so it never runs for Forager **[I]**.
  - **`RandomAccessFile`** is used for `seek`, `readFully(byte[], int, int)`, `length` and `close`, in
    `PhysicalFile`, `OsmFile` and `NodesCache` **[C]**.
  - **Also on the reached path** **[C]**:
    - `System.currentTimeMillis` (the time limit, `RoutingEngine.java:1979-1983`);
    - `System.arraycopy`;
    - `System.getProperty` and `Boolean.getBoolean`, the system-property reads, which become constants;
    - `Double.compare` and `Collections.sort` (`WaypointMatcherImpl.java:69-71`, `:257`);
    - `StringTokenizer`;
    - Java regular expressions: `String.split(";|\\|")` at `RoutingParamCollector.java:26`, and `matches`,
      `replaceAll` in `BExpressionContext`;
    - `Float.toString`, `Float.parseFloat`, `Short.parseShort`;
    - the `Math` functions `sqrt`, `sin`, `cos`, `atan2`, `exp`, `round`, `toDegrees`;
    - `Class.getPackage().getImplementationVersion()` in `OsmTrack`'s static initializer (`OsmTrack.java:26`),
      which runs on every route;
    - `synchronized (openSet)` twice in `_findTrack` (`:1965`, `:1986`);
    - `ProfileCache`'s `static synchronized` methods;
    - `File.lastModified`, the profile cache key (`ProfileCache.java:45`).
- **Semantics a port has to keep** **[I]**, each found while reading:
  - **Java's `split` drops trailing empty strings,** and Kotlin's does not. `getWayPointList` and the profile
    parser rely on Java's behaviour.
  - **`recalcTrack` assigns `t.distance`** (`RoutingEngine.java:1565`), the value Forager reads, so it is on the
    result path.
  - **Some engine messages carry a JDK exception's `toString()`,** for example
    `"error reading datafile " + currentFileName + ": " + e` (`NodesCache.java:202`). Forager classifies by
    message prefix (`BRouterFailures.kt:18-49`), so the text after the prefix will differ on Kotlin/Native.
    Item E's "identical" has to say whether messages compare in full or by Forager's classification.
  - **The memory bound is fixed** at `memoryclass = 64` (`RoutingContext.java:54`), giving 64 MB
    (`RoutingEngine.java:1812`). It is not read from the runtime, so it is portable as is.
  - **The search loop reads the `volatile` flag once per pass** (`RoutingEngine.java:1975-1977`). Forager's
    own thread does the cancelling.

## B to K

**Not answered.** Not started beyond the inventory above:
- B, library versions and targets;
- C, layout and the Android questions of Amendment 1;
- D, the oracle;
- E, the differential tests;
- F, tile stability (no HTTP `HEAD` was sent);
- G, CI;
- H, floating point;
- I, the Phase 2 order;
- J, the mechanism prediction;
- K, the timing plan.

## Handoff to the next session

- **Branch `brouter-port`.** Commits:
  - `4f54810`, the bases and premises;
  - `6c97d9f`, item A's evidence;
  - this report.
- **Reproduce item A** with `03-reach/tools/run.sh <Forager clone> <work dir> <jacoco dir>`.
  - It needs JDK 21 and python3.
  - It needs JaCoCo 0.8.14's `org.jacoco.agent-0.8.14-runtime.jar` and `org.jacoco.cli-0.8.14-nodeps.jar` from
    Maven Central (this session used the Google mirror the dispatch names, SHA-1 checked against Maven's `.sha1`).
- **Scratch is left in place** for the next session, under
  `/tmp/claude-0/-home-user/c9851708-fbbe-5ce6-a5f8-ed278a238cce/scratchpad/hazel/`:
  - the upstream clone;
  - `build/` (compiled classes, `javap.txt`, `jacoco.exec`/`.xml`);
  - `downloads/jacoco`.

  It was not deleted because the task has not ended. Delete it when Phase 1 ends.
- **Next, in the dispatch's order:**
  - B: Maven metadata for an `okio` `FileHandle` or an `expect`/`actual` random-access read, against the four
    operations above;
  - C, including Amendment 1's three Android questions;
  - D (the release jar and `fetch-brouter.sh:12`'s SHA-256 are already confirmed);
  - E, built on the 47 methods the sample did not reach;
  - F, `HEAD` requests only;
  - G, H, I, J, K.

## Disclosures

### Confirmed vs inferred

- **Confirmed by computation:**
  - the bases;
  - the vendored tree;
  - every premise line;
  - the class-level count;
  - the four tiers' method and line counts, and their soundness on the sample;
  - the harness matching Forager's tests and the command-line router;
  - the JDK inventory.
- **Confirmed by reading:** each cut's guard, with the line cited in `exclusions.txt`.
- **Inferred:**
  - that each tier's cuts are complete for Forager in general, not only on the sample;
  - that parse-time code is deterministic for the pinned files;
  - the recommendation to port T2 with T3 as named failures;
  - the semantics notes above.

### Could not determine

- **Whether the sample reaches every path a real phone's data would.** It does not: 47 methods of T3 never ran.
  Which inputs reach them is item E's work.
- **How much of each kept method is dead inside it** (branch level). The counts are per method.
- **Whether `brouter.de` serves stable bytes.** Not checked.

### Premises that were wrong

- **Premise 3, in part.** Forager also reads `track.distance` (`BRouterTrailRouter.kt:115`), so `recalcTrack`'s
  distance is part of what the port must reproduce. It also depends on `getErrorMessage`, `getFoundTrack` and
  `quite`, and its test on `isTerminated` and an overridable `doRun`.
- **Premise 6's sorted maps, in part.** At source level they are used. On the method-level path only one sorted
  iteration remains, over an empty map for Forager's calls. Date formatting does not remain on the path at all.
- **The ~13,400 estimate (premise 5)** was marked unverified. Measured, it falls between T0 and T1.

### Decided beyond scope

- **The tiering itself,** and in particular tier P, which ties the port to the pinned profile. That is offered as
  an option, not decided; it needs the planner's or owner's word under ruling b.
- **JaCoCo 0.8.14 was used as a scratch measurement tool,** downloaded to scratch, not a dependency of anything.
  0.8.14 was chosen over the newest 0.8.16 (released 2026-10-26) to avoid a two-day-old artifact.
- **A mid-session fix to the analysis tool:**
  - the first version counted a class as instantiated at its `new` opcode even when the constructor call after it
    was cut, which left `StackSampler.run` and the `Format*` classes reachable;
  - it now instantiates through the constructor edge;
  - all tiers were re-run and re-checked against JaCoCo after the fix, and the figures above are from the fixed
    version.
- **Some rows in `exclusions.txt` are redundant once the fix landed:** their target classes are never
  instantiated. `summary.txt` lists them as unused rather than dropping them.
