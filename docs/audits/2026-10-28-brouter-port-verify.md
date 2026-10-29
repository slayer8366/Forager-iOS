# BRouter port, Phase 1: verify and plan (partial: the premises, items A to I; H's Android question in part)

Dispatch 2026-10-28-01 and its Amendment 1 (RECORD -3, -4), restarted at -5. Written by Hazel Cairnwright, the
second session on this dispatch, 2026-10-28.

**Status: partial.** This session stopped at the dispatch's long-conversation limit ("past about 200k tokens,
push and hand off", dispatch "Working here"), after the premises and item A. **Items B to K are not answered
here.** What this session found that bears on them is under "Found on the way". It is input for the next
session, not an answer to those items.

**Part 2** (a third session, Hazel, 2026-10-28) answered **item B** only and stopped at its own token limit
(about 150k, set by the planner for this helper). C, D, F, G and H, which were part 2's, are not answered; what
part 2 found that bears on C and G is under "Handoff to the next session".

**Part 3** (a fourth session, Hazel, 2026-10-29) answered **C**, with Amendment 1's three Android questions, and **D**,
the items RECORD -8 gave it. E to K are not answered here: F and G are part 4's; H and E are part 5's; I, J and K are
part 6's.

**Part 4** (a fifth session, Hazel, 2026-10-29) answered **F**, the test tiles in CI. **F is a stop for the owner's ruling**:
the options and a recommendation are written, and nothing was chosen or created. **G is not answered.** Part 4 stopped
after F at the planner's handoff limit ("when your own estimate passes about 110k, finish the item you are on, push, and
hand off"). The reading the planner listed had already brought the harness count to about 110k before F began, and F
ended at about 175k. Of G, only the repository's visibility was checked, because F's recommendation depends on it.

**Part 5** (a sixth session, Hazel, 2026-10-29) answered **G**, written for F's ruling (below), and **part of H**: the hash-order
question B left open, and the inventory of floating-point sites. **H's floating-point classification and its simulator test
plan are not answered.** Part 5 stopped at the planner's handoff limit (about 200k by the harness's count); the required
reading took about 145k of it. **G carries a finding against C** (JetBrains' published Gradle range for Kotlin 2.3.10 ends at
9.0.0), for the planner; it is not a stop.

**Part 6** (a seventh session, Hazel, 2026-10-29) answered **H's steps 1 and 2**: which floating-point sites can choose a route,
and whether the JVM and Kotlin/Native can differ there, with scratch runs on the JVM, `linuxX64` and `linuxArm64` under qemu.
**H's steps 3 (hash order) and 4 (the simulator test) are not answered.** Step 3's evidence was gathered in the same runs and is
committed; its findings are under "Handoff", as input. Part 6 stopped at the planner's handoff limit: the harness count had
passed it during step 2 (about 290k when step 2 was pushed), which this session noticed late. E is not started.

**Part 7** (an eighth session, Hazel, 2026-10-29) wrote **H's step 3** (hash order) and designed **H's step 4** (the simulator
test, not built or run). It answered **the planner's Android question in part**: two Android source files were read, and the
rest is listed as not read. It stopped at the planner's handoff message (05:01Z, past 180k by the harness's count). **E, I, J and
K are not answered.** New in step 3: the oracle's own `HashMap` order is not a function of the input where a bucket holds nine
or more nodes with equal `ilon + ilat`.

**Part 8** (a ninth session, Hazel, 2026-10-29) designed **E**, the differential tests, as a design fixed before any result is
read. Nothing was routed and no tile was downloaded. E's facts were computed in `10-tests/`. **I, J and K are not answered**: they
are part 9's (RECORD -16). New in E: no shipped profile switches on `consider_crossing` or `correctMisplacedViaPoints`; the
latter's body cannot be reached through Forager's two-waypoint call; and HotSpot builds `StrictMath`'s table when its libm
intrinsic is switched off.

**Part 9** (a tenth session, Hazel, 2026-10-29) wrote **I**, the Phase 2 order, with each branch on the rulings still to come.
**J and K are not answered**, and the single list of the owner's choices is not written: the planner asked for a handoff after I
(its message, 10:43Z: the harness count had passed 180k). What part 9 found for J and K is under "Handoff", as input. New in I:
the packages layer cleanly bottom-up once interface callbacks are set aside; the oracle's own runs (capture, E3, expected values)
come before the router, so E3's stop is met early; the via-point code is 429 lines; fdlibm under (c) is 726 lines, and `atan2`
alone 314, which (b) also needs in test code.

**F is ruled.** The owner chose "Forager-iOS release (Recommended)", relayed by the planner to part 5 on 2026-10-29, to be
recorded as RECORD -12. It was not yet on `main` when part 5 read `main` at `7043843`.

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

### B. A replacement for each JDK API on the path

Written by the third session on this dispatch (Hazel, part 2). The list is T2's 48 JDK classes
**[C: `03-reach/out/T2/jdk-use.txt`]**; T3's list is the same 48 classes **[C: diff of the two files]**.

**How the candidates were checked.**
- **Maven metadata,** read from the Google mirror of Maven Central the dispatch names, saved under
  `04-libraries/metadata/`, with `04-libraries/summary-raw.txt` **[C]**.
- **A scratch Kotlin Multiplatform probe** (`04-libraries/probe-kmp/`, built outside the repository):
  - Gradle 9.7.0, Kotlin 2.3.10, targets `jvm`, `iosArm64`, `iosSimulatorArm64`;
  - common code using each candidate below;
  - `jvmTest`, `compileKotlinIosArm64` and `compileKotlinIosSimulatorArm64` all passed on this Ubuntu x86_64
    container **[C: `probe-kmp/build.log`]**;
  - the JVM test reads `W75_N40.rd5` at three offsets through okio and through `RandomAccessFile` and requires
    the same bytes: 2 tests, 0 failures **[C: `probe-kmp/TEST-probe.ProbeTest.xml`]**;
  - both iOS klibs carry `compiler_version=2.3.10`, `abi_version=2.3.0` **[C: `probe-kmp/klib-manifests.txt`]**.
- **Not checked:** nothing in the probe ran on iOS. Compiling a klib proves the API exists for the target, not
  that it behaves the same there. That needs the simulator (item G).

**Libraries.** Two, at most. Each version is the newest release in its metadata.

| Library | Version | Metadata `lastUpdated` | Publishes `jvm`, `iosArm64`, `iosSimulatorArm64` | Built against |
|---|---|---|---|---|
| `com.squareup.okio:okio` | **3.18.2** | 2026-09-04 | **Yes**: `jvmApiElements`, `iosArm64ApiElements`, `iosSimulatorArm64ApiElements` in `okio-3.18.2.module`; the three per-target artifacts' own metadata list 3.18.2 too; and the probe resolved and compiled against all three | `kotlin-stdlib` 2.1.21 (the module's common variant) |
| `org.jetbrains.kotlinx:atomicfu` (only if a lock is kept, row 8) | **0.33.0** | 2026-05-29 | Resolved and compiled for all three in the probe | not read |

**[C: `04-libraries/metadata/`, `04-libraries/module/okio-3.18.2.module`, `probe-kmp/build.log`]**

`kotlinx-io-core` 0.9.1 was looked up **[C]** but not evaluated. Whether it has a positional read was not
checked **[?]**.

**Each JDK API, with its replacement.**

| # | JDK use on the path | Where | Replacement in common code | Mark |
|---|---|---|---|---|
| 1 | **`RandomAccessFile`**: `seek`, `readFully(byte[],int,int)`, `length`, `close` | `PhysicalFile`, `OsmFile`, `NodesCache` | okio `FileSystem.SYSTEM.openReadOnly(path)` gives a `FileHandle`: `read(fileOffset, array, off, n)` in a loop for `readFully`; `size()`; `close()`. The read is positional, so there is no shared file pointer. The loop must throw an `IOException` on a short read, as `readFully` throws `EOFException`. `NodesCache.java:202` turns that into "error reading datafile ...", which Forager classifies (`BRouterFailures.kt:18-49`). | **[C]** probe, JVM bytes equal; iOS compile only |
| 2 | **`File`**: `exists`, `isDirectory`, `getName`, `getParentFile`, `getAbsolutePath`, `lastModified`, `equals`, `separatorChar`, two constructors | 8 classes | okio `Path` and `FileSystem.SYSTEM.metadataOrNull(path)` (`isDirectory`, `lastModifiedAtMillis`); `Path.DIRECTORY_SEPARATOR` | **[C]** probe compiles `metadataOrNull`; the rest **[I]** |
| 3 | **`BufferedReader`/`FileReader`**: `readLine`, `read`, `close` | `BExpressionContext`, `BExpressionMetaData`, `StorageConfigHelper` | okio `source(path).buffer()`: `readUtf8Line()`; `readUtf8CodePoint()` for `read()`. `FileReader` decodes with the platform charset and `read()` returns UTF-16 units. They differ only outside the Basic Multilingual Plane, which the pinned files would need to be checked for. | **[C]** `readUtf8Line` compiles; the charset point **[I]** |
| 4 | `FileInputStream`, `BufferedInputStream`, `DataInputStream` | `OsmTrack.readBinary` | **Not ported.** The method is reached, but its body after `filename != null` never runs for Forager (`OsmTrack.java:235`, part 1). The port returns early, or fails "unsupported" if a filename arrives. | **[R]** part 1; **[I]** |
| 5 | `FileWriter`, `PrintWriter`, `StringWriter`, `Writer`, `Date`, `PrintStream` (`System.out`/`err`), `StackTraceElement` | logging: `RoutingEngine.java:112-132`, `:146`, `:158-163` (`logThrowable` feeds `logInfo`) | **Not ported.** A logger interface whose default does nothing. Part 1's tier E shows logging is off for Forager. | **[R]** |
| 6 | **`TreeSet`/`SortedSet`** | `RoutingEngine.recalcTrack`, `:1568`, over `directMap`'s keys, empty for Forager | `directMap.keys.sorted()` from the standard library. No library. | **[R]** part 1 |
| 7 | **The `volatile` terminate flag** | `RoutingEngine.java:69`, read once per pass (`:1975-1977`) | `@kotlin.concurrent.Volatile` from the common standard library. It compiled for all three targets with no opt-in, and on the JVM the field carries `ACC_VOLATILE`. Its memory semantics on Kotlin/Native were not tested. | **[C: `probe-kmp/jvm-volatile-flag.txt`]**; Native **[I]** |
| 8 | **`synchronized`**: 2 blocks in `_findTrack` (`:1965`, `:1986`); `ProfileCache`'s `static synchronized` methods | | **`_findTrack`: drop.** `openSet` is private, and every use is inside `RoutingEngine` (`:41`, `:341`, `:841`, `:1965-2014`), on the searching thread. **`ProfileCache`:** Forager runs one route at a time (`ONE_ROUTE_AT_A_TIME`, `BRouterTrailRouter.kt:201`, `:123`) on one thread (`:209`), so the lock does nothing for Forager today. Options: (a) keep it with atomicfu 0.33.0 `SynchronizedObject` (the probe compiled it, and its JVM test ran); (b) drop it and make one-at-a-time part of the port's contract. **Recommend (a):** a silent race on iOS would be worse than one small dependency. | **[R]** greps; **[C]** probe |
| 9 | **`System.currentTimeMillis`** | the time limit (`:1979-1983`), the terminate message's seconds (`:1976`), timeout resets (`:1733`, `:1797`), the early exit (`:2047-2049`) | `kotlin.time.TimeSource.Monotonic.markNow()` / `elapsedNow()`, common standard library (compiled in the probe). Monotonic is stricter than wall-clock time, which a clock change would otherwise move. **Time decides an outcome only through the timeout:** which failure, and when. The early exit at `:2047` needs `fastPartialRecalc` with a `matchPath`, which comes from a reference track Forager never passes. | **[C]** compiles; `:2047` **[I]**, from part 1's `rawTrackPath` cut, not traced here |
| 10 | `System.arraycopy`; `Arrays.equals`/`fill`/`hashCode(float[])`; `Collections.sort` | `DirectWeaver`, `MicroCache`, `CacheNode`, `VarWrapper`, `WaypointMatcherImpl.java:257` | `copyInto`; `contentEquals`/`fill`/`contentHashCode`; `sortWith`, which is stable, as `Collections.sort` is | **[I]** |
| 11 | **Number parsing**: `Double.parseDouble`, `Float.parseFloat`, `Short.parseShort`, `Integer.parseInt`/`valueOf`; `Double.compare`, `Double.isNaN` | waypoints (`RoutingParamCollector.java:90`), profile constants (`BExpression.java:234`, `:401`; `BExpressionContext.java:267`) | `toDouble()`, `toFloat()`, `toShort()`, `toInt()`, `compareTo`, `isNaN()`. **Whether Kotlin/Native parses to the same bits as the JVM is item H's question.** | **[I]**; see H |
| 12 | **`Float.toString`** | `BExpressionContext.getKeyValueDescription` (`:225`, `:241`), reached | `Float.toString()`. Its text feeds a cost only through `StdPath.java:98-130` (`estimated_crossing_class=`), which needs `consider_crossing`. That defaults to off (`RoutingContext.java:122`), and the pinned profile does not set it. Otherwise the text goes into messages Forager does not read. | **[R]** |
| 13 | **Strings**: `split(regex)`, `matches`, `replaceAll`, `toLowerCase()`, `equalsIgnoreCase`, `StringTokenizer`, `Character.isWhitespace` | `RoutingParamCollector.java:26`, `BExpressionContext`, `BExpression` | `Regex`, with a small `javaSplit` that drops trailing empty strings as Java does; `lowercase()` (locale-free, where Java's uses the default locale; only `outputFormat` at `RoutingParamCollector.java:238-240`, which Forager never sets); `equals(ignoreCase = true)`; a small tokenizer. **`Character.isWhitespace` (`BExpressionContext.java:1031`, the profile tokenizer) needs its own function:** Kotlin's `Char.isWhitespace()` also counts no-break spaces, which Java's does not. | **[R]** sites; the semantics **[I]**, not tested |
| 14 | `String.format(Locale.US, "%3.1f", ...)` | `BExpressionContext.addLookupValue`, unknown-value branch (`:572-696`), never run on the pinned `lookups.dat` (part 1) | There is no `String.format` in common code. **Recommend a named "unsupported" failure** in that branch, rather than porting Java's formatter rounding untested. | **[R]** part 1; recommendation **[I]** |
| 15 | **`Math`**: `sqrt`, `sin`, `cos`, `atan2`, `exp`, `abs`, `min`, `max`, `round(double)`, `toDegrees` | 13 classes | `kotlin.math`, which compiled for all three targets. **Two do not map one to one:** `Math.round(NaN)` is 0 in Java, while `roundToLong()` throws for NaN in Kotlin; `Math.toDegrees` must be written as Java writes it, a multiply by a constant. Whether `sin`, `cos`, `atan2`, `exp` give the same bits on Native is item H's question. | **[C]** compiles; the rest **[I]** |
| 16 | `Class.forName`, `getDeclaredConstructor`, `newInstance`; `Package.getImplementationVersion` | `RoutingContext.java:86-97`; `OsmTrack.java:26` | No reflection in common code. A fixed table: the default `StdModel`, and anything else is "unsupported" (part 1, premise 4). The version string becomes a constant. | **[R]** |
| 17 | `System.getProperty`, `Boolean.getBoolean` | part 1's tier S | Constants, at the values Forager runs with | **[R]** part 1 |
| 18 | **`HashMap`, `ArrayList`, `Iterator` and the rest of `java.util`** | throughout | Kotlin collections. **A risk this table does not settle:** on the JVM, Kotlin's `HashMap` is `java.util.HashMap`, but on Native it is another implementation, so its iteration order may differ. Reached code iterates hash maps at `BExpressionContext.java:453`, `:524`, `:863`; `BExpressionMetaData.java:67`; `RoutingContext.java:107`; `RoutingParamCollector.java:188`, `:260`; `NodesCache.java:393`; `OsmNodesMap.java:181`. Whether order changes a result at any of them was **not determined**. | **[R]** sites; effect **[?]** |

**What B concludes.**
- **Random-access reads and the file system** take one library, okio 3.18.2. An `expect`/`actual` over
  `RandomAccessFile` and POSIX `pread` would also do, at roughly 50 lines with no dependency **[I]**. okio is
  recommended because it also covers rows 2 and 3.
- **The sorted maps and sets need no library** (row 6).
- **The terminate flag and the clock** are in Kotlin's common standard library (rows 7, 9).
- **Formatting:** none stays on a path Forager runs (rows 5, 12, 14).
- **Against the planner's prediction** ("random-access file reads and the sorted maps are the only JDK pieces
  needing a library or new code"):
  - right about random access;
  - wrong about the sorted maps;
  - it misses the small pieces of new code in rows 13 to 16;
  - it misses the lock (row 8);
  - it misses the `HashMap` order question (row 18).

  **[I]**

### C. The project layout, and how Forager's Android app takes the port in

Written by the fourth session on this dispatch (Hazel, part 3, 2026-10-29). The scope is tier T2, by the owner's
"Any profile" (RECORD -8).

**Base check first [C: `05-layout/00-bases-part3.txt`].**
- **Forager `pre-main` has moved,** from `e2969abe4` to `d100a4776`: 32 commits.
- **No premise path changed.** The diff is empty for:
  - `third_party/`;
  - the routing sources, tests, resources and assets;
  - `gradle/`, `gradle.properties`, `settings.gradle.kts` and the build files;
  - `server/pmtiles-worker/test/rd5/`.
- **No changed line mentions `btools` or BRouter.** The moved base changes no premise, so it is not a stop.
- **Every Forager read below is at `e2969abe4`,** the read-only clone, still at that commit.
- **Forager-iOS `main`** moved to `38bbb23`, by RECORD -6 to -8 and two filed reports only.

**Recommendations, one of each.**

| Question | Recommendation | Basis |
|---|---|---|
| Gradle | **9.7.0**, by the wrapper, with `distributionSha256Sum` set | Forager's own (`gradle/wrapper/gradle-wrapper.properties:3`) **[R]**. Part 2's probe and this part's Kotlin probe built with it **[C]**. Forager's wrapper has no `distributionSha256Sum` (the whole file is 7 lines) **[R]**, so pinning the checksum is new here, not copied. Its value was not read **[?]**. |
| Kotlin | **2.3.10 exactly,** Forager's pin, moved only together with Forager | the probe below **[C]** |
| Modules | **One KMP library module, `:brouter`,** in `brouter/` | see "Modules" |
| Targets | **`jvm` (JVM target 17), `iosArm64`, `iosSimulatorArm64`.** No `androidTarget`, no `iosX64`. | see "Targets" |
| Packages | **Rename to `com.zynergylabs.forager.brouter.<upstream sub-package>`,** keeping every class and file name | see "Packages" |
| MIT notice | **`brouter/LICENSE`,** upstream's file unchanged, plus a provenance README and one header line per file | see "The MIT notice" |

**Modules.**
- **Source sets:**
  - `commonMain` holds all ported code;
  - `commonTest` holds unit tests that run on every target, the iOS simulator included (item G);
  - `jvmTest` holds the differential tests against the oracle (item D), on the JVM only.
- **No `jvmMain` or `iosMain`.** okio covers the file reads (B, rows 1 to 3), so the port needs no `expect`/`actual`.
  Keeping it that way is what lets Forager vendor the source (below) **[I]**.
- **Why one module rather than upstream's five:**
  - Kotlin's `internal` is per module, so one module lets everything but the entry point stay `internal` **[I]**;
  - Forager already merges the five into one source tree (`third_party/brouter/README.md:6`) **[R]**.

**Targets.**
- **`jvm`, with JVM target 17,** as Forager's app compiles (`app/build.gradle.kts:316-317`, `:529`) **[R]**.
- **Android takes the `jvm` variant,** so there is no `androidTarget`, no Android Gradle plugin in the port's build,
  and no Android SDK in its CI.
  - The JVM consumer below compiled and ran against the `jvm` jar **[C]**.
  - The Android-build half is under "Android consumer" below.
- **`iosArm64` for phones, `iosSimulatorArm64` for CI's simulator.** Part 2 compiled both klibs on this Ubuntu
  container **[C, part 2]**.
- **`iosX64` (Intel simulators) is left out.** Whether any GitHub macOS runner label the project would use is still
  Intel is item G's question **[?]**.

**Packages.** Rename. **What decides it [I]:**
- The oracle (item D) puts `btools.router.RoutingEngine` and the rest on the same JVM test classpath as the port.
- With the same names, a class loader loads one copy of each name, so one engine shadows the other. The differential
  tests would need class-loader isolation, or would compare an engine with itself.
- At Android's switch, Forager's `:brouter` (`settings.gradle.kts:21`) and the port could not both be in the app under
  the same names (duplicate classes) **[I]**. Renamed, Forager can carry both while it compares them.

**Traceability is kept by the path below the prefix:**
- `btools/router/RoutingEngine.java` becomes `com/zynergylabs/forager/brouter/router/RoutingEngine.kt`;
- every class keeps its upstream name.

The prefix sits under the namespace Forager's own code uses (`com.zynergylabs.forager.app`, `BRouterTrailRouter.kt:1`)
**[R]**. The exact prefix is a naming choice, listed under "Decided beyond scope".

**The MIT notice.**
- **`brouter/LICENSE`:** upstream's `LICENSE`, unchanged.
  - Forager's copy is identical to upstream's (part 1, `01-vendored-vs-upstream.txt`).
  - It begins "The MIT License (MIT) / Copyright (c) 2019 BRouter contributors" (`third_party/brouter/LICENSE:1-3`)
    **[R]**.
  - MIT asks that the notice go with "all copies or substantial portions", and a line-by-line port is one **[I]**.
- **`brouter/README.md`:**
  - a Kotlin port of BRouter v1.7.10 (upstream `4d2639af`), MIT;
  - how its paths map to upstream's;
  - what is ported (tier T2) and what fails as unsupported.
- **One header line per ported file,** naming its upstream file. That is for traceability; MIT does not ask for it
  **[I]**.
- **In the apps:**
  - Forager's open-source page already credits BRouter under MIT, "Built into the app from BRouter's source"
    (`app/src/main/assets/credits/software.json:333-338`) **[R]**. Whether that wording changes at the switch is
    Forager's call.
  - The iOS app will need its own credits page. That is later iOS work, not Phase 2.
- **No further licence question arises from this layout [I].** OpenStreetMap's ODbL attribution concerns the tiles
  (item F), not the code.

#### Amendment 1: how Forager's Android app takes the port in

**Nothing below changes slayer8366/Forager now.** Each route is something Forager's planner would do at the switch,
under Forager's record. None is a stop for Phase 1.

| Route | What Forager would do at the switch | What it needs | Kotlin coupling |
|---|---|---|---|
| **Published artifact, Maven Central** | add one coordinate | **an account** (a Sonatype namespace) and signing keys **[I]**: a stop if chosen | the binary rule below |
| **Published artifact, GitHub Packages** | add a repository and a credential | a read token in Forager's CI, even for a public package **[I, not checked]**: a change to Forager's settings | the binary rule below |
| **Published artifact, a Maven layout committed in Forager-iOS,** read at a commit-pinned URL | add a repository URL with the commit in it, plus Gradle dependency verification | nothing new; it is unusual and was not tried **[I]** | the binary rule below |
| **Gradle composite build** | `includeBuild` a Forager-iOS checkout pinned by submodule or a CI step | a second checkout in every Forager build and CI run. Forager's build then configures the port's KMP build, iOS targets included. | Gradle and plugin versions shared in one build **[I]** |
| **Vendored Kotlin source** at a pinned Forager-iOS commit, as `third_party/brouter/` is today | copy `brouter/src/commonMain` and the `LICENSE`; add a README with the same `cmp` check as `third_party/brouter/README.md:7-16`; compile it as a plain Kotlin/JVM module | nothing new: no account, no token, no network at build time | **source only:** it must compile under Forager's 2.3.10 |

**Recommendation: vendored source [I].**
- **Forager already works this way,** for a reason it recorded: BRouter publishes no Maven artifact
  (`third_party/brouter/README.md:18`) **[R]**.
- **It needs no account and no secret.**
- **Forager's own compiler compiles it,** so the binary-metadata question below does not arise on Android.
  - The source has to compile under 2.3.10.
  - **Tested:** the probe's common source compiled and ran as a plain `kotlin("jvm")` 2.3.10 module
    **[C: `kotlin-probe/logs/consumer-c2310-vendored.log`]**.
- **The proof carries by byte identity:** Forager's check would show its copy equals the commit at which the
  differential tests passed.

**What it asks of the port:**
- `commonMain` only, with no `expect`/`actual`;
- dependencies limited to okio, and atomicfu if B row 8's lock is kept.

B's design already meets both **[I]**.

**What it costs Forager at the switch [I]:**
- a second copy to re-vendor on each port fix;
- a Kotlin/JVM plugin entry, which its catalog lacks: `[plugins]` holds no `kotlin-jvm` (`gradle/libs.versions.toml:129-134`)
  **[R]**;
- an okio version change. OkHttp 4.12.0 requires okio 3.6.0 **[C: `05-layout/okhttp/okhttp-4.12.0.pom`, `.module`]**,
  and Forager declares no okio of its own **[R: grep of `libs.versions.toml` and `app/build.gradle.kts`]**. With the
  port's okio 3.18.2, Gradle would resolve 3.18.2 for OkHttp too.

**The composite build is the second choice.** It fits a developer trying the port locally, not Forager's CI.

#### Amendment 1: which Kotlin version the port may compile with

**Tested [C: `05-layout/kotlin-probe/`, `logs/summary.txt`, `logs/metadata-versions.txt`].**
- **Producers:** a small KMP library, standing in for the port's surface. It has:
  - a sealed result, an open class overridden by the consumer, an inline function;
  - a `@Volatile` field and `TimeSource.Monotonic`.
- **The four producer builds:**
  - Kotlin 2.3.10 (with the iOS targets);
  - 2.3.21;
  - 2.4.21, the newest stable;
  - 2.4.21 with `languageVersion`/`apiVersion` 2.3 and `coreLibrariesVersion` 2.3.10.
- **Consumers:**
  - a Kotlin/JVM build at JVM target 17, Gradle 9.7.0;
  - compiled and run with **Kotlin 2.3.10**;
  - and, as the negative control, with **2.2.21**.
- **Each consumer run** starts with `clean`. Its result line comes from the program's own output.
- **The predictions were written into the summary before any run.** All held.

| Producer (metadata `mv` read from the jar) | 2.3.10 consumer | Its runtime stdlib | 2.2.21 consumer (control) |
|---|---|---|---|
| 2.3.10 (`mv` 2.3.0) | compiles, runs | 2.3.10 | compiles, runs |
| 2.3.21 (`mv` 2.3.0) | compiles, runs | **moves to 2.3.21** | not run |
| 2.4.21 (`mv` 2.4.0) | compiles, runs, no warning | **moves to 2.4.21** | **refused, 35 errors:** "The binary version of its metadata is 2.4.0, expected version is 2.2", on the probe's jar and on the stdlib |
| 2.4.21, pinned to 2.3 (`mv` 2.3.0, POM names stdlib 2.3.10) | compiles, runs | 2.3.10 | compiles, runs |
| Vendored source, compiled by the consumer | compiles, runs | 2.3.10 | not run |

**What this shows [C]:**
- A compiler reads binaries one minor version newer than itself and refuses two:
  - 2.3.10 read 2.4.0;
  - 2.2.21 refused 2.4.0.
- So a published or composite port may compile with 2.3.x or 2.4.x for Forager's 2.3.10, and not with 2.5 or later.
- **An unpinned newer compiler still changes Forager.** Its POM names its own stdlib, and Gradle resolved the
  consumer's runtime stdlib upward, to 2.4.21. Forager's app would then ship a newer stdlib than its compiler, by
  nobody's choice.
- **`coreLibrariesVersion` with `languageVersion`/`apiVersion` 2.3** keeps both the metadata and the stdlib at
  Forager's level.

**Recommendation: compile the port with 2.3.10, exactly as Forager does. Neither side has to move.**
- If iOS later needs a newer compiler, for example for a newer Xcode on CI's runners (item G; not checked):
  - keep `languageVersion` and `apiVersion` at 2.3 and `coreLibrariesVersion` at 2.3.10 until Forager moves;
  - with vendored source, the language pin alone keeps the source compilable under 2.3.10 **[I: not tested]**.

**Not tested:**
- iOS klib metadata across compiler versions. Forager's Android app never reads a klib.
- A real phone.

**Android consumer** (the same check in an Android app build at Forager's settings: AGP 9.3.1, `kotlin-android`
2.3.10, `builtInKotlin=false`, `newDsl=false`, JVM target 17, minSdk 26):
- **Its compileSdk is 35,** the only platform in this container. Forager uses 37 (`app/build.gradle.kts:243`)
  **[R]**.
- **Result [C: `kotlin-probe/logs/android-summary.txt`, `android-*.log`]:** `clean :app:assembleDebug` compiled,
  dexed and produced an APK against the 2.3.10, 2.4.21, and pinned 2.4.21 libraries, with no `e:` lines.
  - The dependency tree resolves `portprobe` to `portprobe-jvm`, so an Android build takes the KMP `jvm` variant.
  - Against the unpinned 2.4.21 library, the app's stdlib moved from 2.3.10 to 2.4.21, as on the JVM.
  - The only warnings are deprecations of the build settings themselves (`builtInKotlin=false`, `newDsl=false`, the
    `kotlin-android` plugin), which Forager's build shares **[I]**.

#### Amendment 1: what the port exposes in place of the seven classes

**Today's shapes cannot be kept exactly [R]:**
- **`RoutingEngine` and `PhysicalFile` take a `java.io.File`** (`BRouterTrailRouter.kt:212-213`;
  `RoutingDataFolder.kt:49`). Common code has no such type; B row 2 replaces it with okio's `Path`.
- **Forager uses the Java side's own shapes:**
  - it writes the field `quite` (`:161`);
  - it calls Java getters (`getErrorMessage()`, `getFoundTrack()`, `getILat()`; `:136`, `:142`, `:99`), choosing
    "the getters, not properties" because `RoutingEngine` has protected fields of those names (`:135`).
  - In Kotlin these become properties, and the call shapes change.
- **Forager's cancel test subclasses `RoutingEngine` and overrides `doRun`** (`BRouterTrailRouterCancelTest.kt:58-64`,
  part 1). Kotlin classes are final unless opened.

So Forager's adapter changes at the switch whichever option is taken.

**Recommendation: a small entry point, which Forager wraps behind its own `TrailRouter`.**
- `TrailRouter` is already Forager's port for the engine (`domain/routing/TrailRouter.kt:15-28`) **[R]**.
- The engine's classes stay `internal`.

A sketch, not a fixed API **[I]**:

```kotlin
// commonMain: the port's public surface, all of it
class RouteRequest(
    val startLon: Double, val startLat: Double, val endLon: Double, val endLat: Double,
    val routeData: okio.Path, val profile: okio.Path,
    val params: Map<String, String>,      // Forager's: alternativeidx=0, profile:use_dynamic_range=0
    val maxRunningTimeMillis: Long,
)
class RouteCancel { fun terminate(); val isTerminated: Boolean }    // the volatile flag, B row 7
sealed interface RouteOutcome {
    class Found(val iLat: IntArray, val iLon: IntArray, val distance: Int) : RouteOutcome   // track.nodes, track.distance
    class Failed(val message: String) : RouteOutcome                                       // the engine's own message
}
fun route(request: RouteRequest, cancel: RouteCancel): RouteOutcome    // blocking; the caller picks the thread
fun checkDataFile(file: okio.Path): String?                            // RoutingDataFolder.headerProblem's check
```

**Why [I]:**
- **It is exactly what A found Forager calling, and nothing more:**
  - the request;
  - `doRun` and `terminate()`;
  - `getErrorMessage()`, and the found track's nodes and distance;
  - the header check.

  The surface the differential tests have to prove is therefore the surface Forager uses.
- **iOS gets the same call.** With the engine `internal`, an iOS framework built from it shows only this surface.
- **Forager's domain stays in Forager:**
  - `Failed` carries the engine's message unchanged, so `BRouterFailures.classify` (`BRouterFailures.kt:18-49`) goes
    on naming the failures;
  - the adapter keeps its one-route lock, its engine thread and its cancel-and-wait (`BRouterTrailRouter.kt:120-178`).

**What it changes for Forager at the switch:**
- the adapter's calls;
- its cancel test, which would fake the entry point rather than subclass the engine.

Both are Forager's work.

**The other option, keeping the seven classes public under their upstream names, is not recommended.**
- It does not spare Forager a rewrite (above).
- It makes the whole engine public API on both platforms **[I]**.

### D. The oracle

**What was checked [C: `06-oracle/`].**
- **The release zip,** fetched from `fetch-brouter.sh:11`'s URL to scratch:
  - 6,724,983 bytes, matching `:13`;
  - SHA-256 `023fec3b…ad01532`, matching `:12` (`logs/fetch.log`).
- **Two jars in it:**
  - `brouter-1.7.10-all.jar`, which `fetch-brouter.sh:31` and `make-expected.sh:15` use, with the map creator, server,
    protobuf and osmosis classes;
  - `brouter-1.7.10-ro.jar`, 345,776 bytes, SHA-256 `598b647f…dbd972`: only the five router packages and
    `btools/server`.
  - Both manifests say `Implementation-Version: 1.7.10` (`logs/jars.txt`).
- **The zip's `hiking-mountain.brf` and `lookups.dat`** have the git blob hashes `fetch-brouter.sh:14` pins. It carries
  26 `.brf` profiles in all (`logs/profiles.txt`).
- **The jar's classes are the vendored source's classes.** In `btools/{codec,expressions,mapaccess,router,util}`:
  - the `-ro` jar's 115 class files have the same names as the vendored source compiled fresh from Forager's clone
    (`javac` 21, `--release 11`, as `third_party/brouter/build.gradle.kts:14-16`);
  - all are class-file major 55;
  - **all 115 give identical `javap -c -p` output** once constant-pool indices are removed (`logs/ro-vs-fresh-build.txt`).
  - The same holds against part 1's build, and between `-ro` and `-all` (`ro-vs-vendored-build.txt`, `ro-vs-all.txt`).
- **The comparison can fail.** The same source compiled with string concatenation inlined differs in 43 of 115
  classes (`negative-control.txt`).
- **The jar routes as the source does.** Part 1's harness (`03-reach/tools/ForagerCalls.java`: Forager's calls, 18
  cases on the fixture tile) printed identical lines on the `-ro` jar and on the fresh source build, and both match
  part 1's `harness-run.log` (`logs/harness-jar.log`, `harness-source.log`).

**What that comparison does not cover [I]:**
- what `javap -c -p` does not print: debug attributes, and constant-pool entries that no instruction or declaration
  shows;
- and anything that runs differently on another JVM.

Both options run on the same test JVM, so the last does not separate them.

**The two options.**

| | Release jar (`-ro`), fetched and checked | Source copied from Forager's `third_party/brouter/` |
|---|---|---|
| The bytecode Forager runs | **The same instructions** as Forager's source build **[C]** | The same, by construction |
| In Forager-iOS | a pinned URL, size and two SHA-256s | 101 Java files, 19,327 lines, a second MIT copy, and a Java compile in the test build |
| Pin | already checked in Forager (`fetch-brouter.sh:12-13`, `:23-28`) | the README's `cmp` check against upstream |
| Network in CI | one GitHub release download per cold cache | none |
| If the release asset disappears | the oracle goes with it, unless a copy is kept (the same hosting question as F) | unaffected |
| Precedent in Forager | Forager's expected routes come from the release jar's command-line router (`make-expected.sh:15`) **[R]** | Forager's app compiles this source (`settings.gradle.kts:21-22`) **[R]** |

**Recommendation: the release jar, `brouter-1.7.10-ro.jar`, fetched and checked.**
- **Why:** on the JVM the copied source would add no fidelity, since its bytecode is the same instructions **[C]**.
  The jar keeps 19,327 lines of Java out of the repository, and its pin is the one Forager already checks.
- **Why `-ro` over `-all`:** the router classes are identical in both **[C]**, and `-ro` leaves the map creator,
  protobuf and osmosis off the test classpath. Forager's own scripts use `-all`, so this is listed under "Decided
  beyond scope".
- **The source option stays the fallback** if a download in CI proves unreliable, with the same instructions.

**How the oracle runs in tests [I].**
1. **A Gradle task in the port's build** downloads the zip into `build/`:
   - it checks the size and SHA-256 as `fetch-brouter.sh:23-28` does;
   - it extracts `brouter-1.7.10-ro.jar` and checks that file's own SHA-256 (`598b647f…`).

   Nothing is committed.
2. **Only `jvmTest` depends on it,** as a file dependency. It never reaches `commonMain`, the iOS targets or anything
   vendored.
3. **Both engines load in one test JVM, no isolation needed,** because the port is renamed (C, "Packages").
4. **Each differential case drives both engines through the same calls Forager makes.**
   - Part 1's `ForagerCalls.java` is the model.
   - What "identical" compares is item E's.
5. **The two engines run one after the other,** not at once. Each has its own static profile cache under its own
   class names, so neither warms the other's **[I]**.

**Input for E, not a choice:** the zip's 26 profiles are pinned by the same SHA-256. Under "Any profile" (-8) they are
a ready source of profiles that switch on turn restrictions, bike mode and the other T2 features.

### E. The differential tests (part 8: a design, fixed before any result is read)

Written by the ninth session on this dispatch (Hazel, part 8, 2026-10-29). **A design.** No case was routed and no tile was
downloaded. The facts the design rests on were computed in `10-tests/`: which scale-table rows differ, which profiles switch on
which features, and the sample-size arithmetic. Bases: Forager `pre-main` is still `d100a4776` (05:10Z), so no premise check is
needed **[C: `10-tests/00-bases-part8.txt`]**.

**Fixed before data.** Every choice marked **[FBD]** is fixed at this commit. In Phase 2 one changes only by a commit that says
why, made before any port result exists for the cases it touches (the dispatch: "Fix choices before data"). Where a choice
waits on an owner ruling (part 6's (a) to (c), part 7's (i) to (iii)), each branch is written and none is chosen. Every **[FBD]**
choice is also **[I]**: a design, not a measurement.

#### E1. One case, and what "identical" compares

**A case [FBD]** is:
- the tiles in the segment folder;
- the start and the end (two waypoints, as Forager sends);
- the profile, and the params map;
- the time limit, a cancel plan, and `memoryclass`.

Both engines get Forager's call, as part 1's `ForagerCalls.java` and D's step 4 do: `getWayPointList("lon,lat|lon,lat")`;
`setParams` with `alternativeidx=0` and `profile:use_dynamic_range=0`; `quite = true`; `doRun(maxRunningTime)`; `terminate()`
(`BRouterTrailRouter.kt:146-174`) **[R]**.

**The outcome compared [FBD]:**

| Field | Read from, on the oracle | Compared |
|---|---|---|
| kind | `getErrorMessage()`: null is a route; otherwise its family (below) | exactly |
| node list | `track.nodes`, each `(ilon, ilat)`, in order | exactly: the length and every pair |
| cost | `OsmTrack.cost` (`OsmTrack.java:393`) | exactly |
| distance | `OsmTrack.distance` (`:390`), which Forager reads (`BRouterTrailRouter.kt:115`) | exactly |
| message | the engine's string | exactly, except the two timing families |

- **Distance is added to the dispatch's three** (node list, cost, failure kind), because Forager reads it (premise 3's
  correction). A call beyond the dispatch.
- **Recorded, not asserted:** elevations, ascent, time, energy and voice hints. Forager reads none of them; part 6 classed them
  O. A difference there is reported as a finding.
- **C's sketched surface has no cost.** `RouteOutcome.Found` carries nodes and distance only. The tests read the cost through
  an `internal` accessor, which `jvmTest` and `commonTest` can see in the same module **[I]**. A finding for C, not a change to it.

**Failure kinds, by family [FBD].** Lines are `RoutingEngine.java` unless named **[R]**.

| Family | Thrown at | Forager's name for it (`BRouterFailures.kt:18-49`) | Compared |
|---|---|---|---|
| `from-position not mapped…`, `to-position not mapped…` | `:1629` | EndOutsideData | exactly |
| `start island detected for section N`, `target island…` | `:958`, `:963` | NoPath | exactly |
| `no track found`, `no track found at pass=N` | `:477`, `:1771`, `:1775` | NoPath | exactly |
| `datafile … not found`, `segment directory … does not exist` | `NodesCache` | DataMissing | exactly |
| `file of size …`, `top index checksum error`, and the rest of Forager's list | `PhysicalFile`, `OsmFile` | DataCorrupt | exactly |
| `memory limit reached` | `:2028` | EngineFailed | exactly |
| `<operation> timeout after N seconds` | `:1982` | EngineFailed | **family only**; the operation and N are recorded |
| `operation killed by thread-priority-watchdog after N seconds` | `:1976` | EngineFailed | **family only**; N is recorded |
| anything else | `:812` | EngineFailed | exactly, and listed as a finding |

The timing families' N and operation depend on the wall clock (`:1976`, `:1981`), so no exact string is stable **[R][I]**.

**The stored form [FBD].** For each case: the kind, the node count, the cost, the distance, and a 64-bit FNV-1a digest of the
tuple's bits (part 7 step 4's digest, integer-only).
- **No coordinates are committed.** So the expected values add no OpenStreetMap data to the repository **[I]**.
- **On the JVM the oracle runs live beside the port** (D), and its result is also checked against the stored digest. A moved
  oracle (another JDK, another jar) then shows as an oracle failure, not a port failure.
- **iOS compares against the stored digests,** since no oracle runs there.
- **On a mismatch,** the JVM test prints both node lists to its log.
- **The expensive runs are not per push [FBD].** E3's extra oracle runs belong to a task run when expected values are written or
  rewritten. How long any of it takes is K's (part 9) **[?]**.

#### E2. Which Java the oracle is

- **HotSpot, one pinned build, on x86_64 [FBD].** The expected values are written by, and valid for, one JDK, whose vendor, exact
  version and `os.arch` head the stored file. The `jvm` job (G) runs that JDK. Which vendor's build is pinned is for Phase 2 with
  G's workflow. Parts 6 and 7 and this part ran Ubuntu's OpenJDK 21.0.12.1 **[C]**.
- **What E compares:** the port against the unmodified 1.7.10 jar (D) in the same HotSpot process. So E can prove "the port
  routes as the Java engine does on this HotSpot", and nothing wider.
- **What E cannot speak for:**
  - **Forager's Android app today.** It runs the Java engine on Android's runtime, whose `Math.cos` and `Math.atan2` return C's
    `cos` and `atan2` (part 7). Whether they give HotSpot's bits is open and is not settled here. E's results say nothing about it,
    either way.
  - **HotSpot on another CPU.** The `macos-26` runner's HotSpot is arm64. Part 7's step 4 records its table without failing;
    E's expected values are not claimed there.
  - **HotSpot of another version.** `Math.cos` is an intrinsic, not `StrictMath`: on this HotSpot the two differ at 60 table rows
    (part 6) **[C]**. A JDK update can move it.
- **A HotSpot fact E uses [C: `10-tests/logs/table-run.log`, `rows-nointrinsic-vs-strict.txt`].** With
  `-XX:+UnlockDiagnosticVMOptions -XX:-UseLibmIntrinsic`, this HotSpot's `Math.cos` builds the same 1,800-row table as
  `StrictMath.cos`, bit for bit. `-Xint` gives the default table. Shown for the table only; what else the flag changes was not
  checked **[?]**. E uses it under (c) (E6).

#### E3. Does the oracle agree with itself? (before any expected value is fixed)

Two things can make the oracle's answer depend on more than its input:
- **`hmap`'s order** follows identity hashes where nine or more nodes with one `ilon + ilat` share a tree bucket (part 7);
- **the profile cache evicts by the clock** (`ProfileCache.java:114`, `:122`) **[R]**. Whether a reparsed profile can route
  differently from a cached one was not read **[?]**.

**So each case runs under four regimes before its expected value is fixed [FBD]:**
1. **R1, alone:** a fresh JVM, this case only, on a new thread. **Its outcome is the expected value.**
2. **R2, repeated:** in R1's JVM, the same case again at once, on another new thread.
3. **R3, in the suite:** one JVM running the whole case list in a fixed shuffled order (the seed in the test source), on one
   thread.
4. **R4, perturbed:** a fresh JVM, after asking `System.identityHashCode` of 997 fresh objects on the run's thread.

**What is compared:** the outcome tuple, and the oracle's log of each collection: "collected, nodes/paths before=… after=…"
(`RoutingEngine.java:2025`) and "memory limit reached, enabled memory panic mode" (`:2031`).
- **The log needs no change to the jar:** a `debug.txt` in the folder above the profile folder switches it on (`:111-121`) **[R]**.
- **What the log adds is string building only,** at the lines read (`:906`, `:913-921`, `:943`, `:1809`; `cacheStats`,
  `formatStatus`) **[R]**. Even so, R1 runs without `debug.txt`, and a fifth run with it must give R1's tuple. Otherwise the log
  does not describe the expected-value run.
- **A sixth run under JaCoCo's agent** on the jar records which lines each case reached: the reach evidence in E4. It too must give
  R1's tuple.

**A case is stable** when all six runs agree on the tuple, and the logged runs agree on the collection counts. R2 to R4 run
with `debug.txt`, so the logged runs are those three and the fifth.
- **Unstable cases** are counted per stratum and named. They are kept out of every exact assertion, and compared on kind only
  where all six runs agree on the kind.
- **A stop for Phase 2 [FBD]:** if more than 5% of any stratum is unstable, stop and report. The comparison is not shrunk to fit.

**A positive control.** In each regime the harness also builds part 7's twelve-node equal-hash map
(`09-float/part7/TreeBinOrder.java`) and records its order. R1 and R4 must give different orders (part 7 saw five orders in five
builds). If they do not, the regimes do not move identity hashes, and E3 has not been run.

#### E4. The inputs

**Tiles [FBD],** chosen from part 4's listing (`07-tiles/logs/segments4-index-0354Z.tsv`). The sizes are the listing's at 03:54Z;
the bytes captured in Phase 2 will differ (F).

| Tile | Bytes (listing) | Why it is in the sample |
|---|---|---|
| `W75_N40.rd5` | 119,750,873 | Forager's fixture square (premise 7). Dense enough (the New York area) for searches past 42,100 nodes, with rural trail country for the hiking profile **[I]**. 8 of the 54 rows where `Math.cos` and `StrictMath.cos` give different `kx` or `ky` lie in it, between 40.6° and 44.8° **[C: `rows-math-vs-strict.txt`]**. It holds 1° lines. |
| `W75_N35.rd5` | 3,571,019 | The 5° file line at 40°N, with New Jersey on both sides between 75°W and 74°W **[I: geography, not read from the tile]**. Two more of the 54 rows (39.2°, 39.7°). |
| `W70_N40.rd5` | 5,292,557 | The 5° file line at 70°W, crossed by roads in Maine and on Cape Cod **[I: geography]**. |
| `E170_S50.rd5` | 1,061,589 | The `E` and `S` branches of the tile name (`NodesCache.java:348-355`) **[R]**, and one of the 54 rows (−45.9°, Dunedin **[I]**). Small, for the fast cases. |
| Forager's sparse `W75_N40.rd5` | 152,665 | Stratum F0: Forager's own two routes. SHA-256 `ff6d9d8e…` (premise 7). |

- **About 129.8 MB in all** **[C: the sum of the listing's sizes]**, every file far under a release asset's 2 GiB (F).
- **F0's fixture becomes a release asset too,** copied from Forager's test resources at its SHA-256, with its ODbL notice
  (`app/src/test/resources/routing/README.md`) **[R]**. A call beyond the dispatch.
- **Whether 1.7.10 reads today's brouter.de tiles is still unknown** (part 4). Each tile is loaded in the oracle at capture. One
  that fails is replaced by the listing tile nearest in size in the same tile row, recorded before any port result.
- **Not a criterion:** glibc's differing rows. They predict nothing about Apple's (part 6), and Forager-iOS has no Linux target.
  Of part 6's 8 glibc rows only 6 change `kx` or `ky`; rows 128 and 1671 change `cos` alone **[C: `rows-math-vs-native-x64.txt`]**.
  The handoff's "rows 128, 253, 261, 431 and four more" therefore overcounts by two for routing.

**Profiles [FBD]: all 26 in the release zip, plus one of E's own.**
- **Why all 26:** "Any profile" (RECORD -8), and they are pinned by the zip's SHA-256 (D). Forager's `hiking-mountain.brf` is one
  of them (the same git blob, D). What they switch on **[C: `10-tests/logs/profile-features.tsv`]**:
  - turn restrictions: 24, all but the two foot profiles `hiking-mountain` and `shortest`;
  - turn instructions (`turnInstructionMode = 1`): 24;
  - bike mode (`validForBikes`): 16; car mode: 7.
- **What none of them switches on [C]:** `consider_crossing`, `correctMisplacedViaPoints`, `continueStraight`,
  `processUnusedTags`, `add_beeline`, and `pass2coefficient`.
- **So the crossing case needs a profile of E's own [FBD]:** `trekking.brf` (bike mode, and it assigns `priorityclassifier`) with
  `consider_crossing = true`, `crossing_Prio_H = 1`, `crossing_Prio_L = 1000`, and `cost_ToLeft_from_H_classK` and
  `cost_ToRight_from_H_classK` set to 100·K for K from 1 to 6. The costs are non-zero so a misread crossing class changes the cost.
  - `estimated_crossing_class` is a node key of the pinned `lookups.dat` (`:862-868`, values 0 to 6) **[C: `crossing-reach.txt`]**,
    so the cost is reachable wherever a captured tile carries the tag.
  - Whether any captured tile does is read from the coverage run at capture **[?]**.
- **`correctMisplacedViaPoints` cannot run through Forager's call [R].** `snapPathConnection` returns at once unless the waypoint's
  name starts with `via` or `rt` (`RoutingEngine.java:1265-1266`), and Forager's two are `from` and `to`. T2 ports it (RECORD -8),
  but no case through the port's entry point can reach its body. **A finding for I and the planner**, under Forager's "check
  reachability before measuring behaviour": port it untested, or give the entry point named waypoints. Not decided here.

**Strata [FBD].**
- **Every case comes from a generator** with its seed in the test source.
- **Start and end are drawn on the tile's own nodes,** read with the oracle's own reader. Each is moved by a random 0 to 100 m and
  written with six decimals, so `ilon` and `ilat` are exact.
- **A stratum's reach is taken from the oracle,** E3's log and coverage runs, never from the port.
- **Cases are kept in generation order** until the stratum is full. The port's results take no part in choosing them.

| Stratum | n | Generator | Reach evidence, from the oracle |
|---|---|---|---|
| **R**, random | 59 per profile: 1,534 | air distance log-uniform from 100 m to 100 km, within one tile or across a 5° line | none needed; kinds counted |
| **F0**, Forager's own | 2 | `inside-across-the-degree-line` and `inside-long` on Forager's sparse tile with Forager's profile | R1 agrees with the command-line router's `geojson` (premise 7) |
| **DL-lat**, **DL-lon** | 59 each | ends on opposite sides of 40°N (`W75_N40`, `W75_N35`) and of 70°W (`W75_N40`, `W70_N40`) | the found route has nodes on both sides |
| **MP64**, collecting | 59 | R's generator, `memoryclass` 64, 20 to 100 km in `W75_N40` | at least one "collected" line |
| **ML5**, **PM5** | 59 each | MP64's generator with `memoryclass` 5 | "memory limit reached" (`:2028`); panic mode (`:2031`) |
| **NT**, near-ties | 59 | a start nearest a junction node, and an end placed so the two ways' `directionDiff`s differ by at most 4 ulps on the oracle's JVM (`WaypointMatcherImpl.java:66-73`, `:194-235`) | coverage of the comparator's second line (`:71`) |
| **LB**, latitude bands | 59 | start and end in the (c) rows inside the tiles; under (b), in Apple's rows (E6) | the route lies in the band |
| **CR**, crossing | 59 | R's generator with E's crossing profile | coverage of `StdPath.java:101-103` (the description is printed) and `:105-113` (a class is read) |
| **FK**, failure kinds | 10 per kind: 110 | constructed, below | the oracle's message is in the kind's family |

- **`memoryclass` 5 is outside Forager's calls,** which keep 64 (part 7).
  - **Why:** its cap is 3,495,253 bytes [computed: `2 × 5 MiB / 3`, `NodesCache.java:68`, `RoutingEngine.java:1812`], below the
    4,000,000 at which the first collection starts (`OsmNodesMap.java:24`). So the limit or panic mode is decided at the first
    collection. At 64 the cap is 44,739,242 bytes, about 470,000 nodes [computed: 44,739,242 / 95], a search the sample cannot
    promise.
  - **What it costs:** the port has to keep `memoryclass` as a field (a call beyond the dispatch, for I). It also sizes the
    expression cache (`ProfileCache.java:88`) and the tile cache (`RoutingEngine.java:1812`) **[R]**, so these cases differ from
    Forager's in more than the cap.
  - **`memoryclass` 64 cases** that reach the limit or panic mode in any stratum are counted and reported, never padded.
- **A stratum that cannot be filled** from 20 times its size in candidates is reported short, with its n and its own bound (E5).
  It is never filled from another generator.
- **The time limit is off in every case but FK 9 [FBD]:** `maxRunningTime = 0` disables it (`RoutingEngine.java:1979`) **[R]**.
  Forager passes 30,000 ms (`BRouterTrailRouter.kt:199`), but a limit makes the outcome depend on machine speed, and the
  simulator will be slower than the JVM. A call beyond Forager's calls, so that only FK 9 meets the clock.

**The failure kinds, 11, 10 cases each [FBD]:**
1. **Start not mapped:** a start in water more than 250 m (`waypointCatchingRange`'s default, `RoutingContext.java:143`) from
   any way.
2. **End not mapped:** the same at the end.
3. **Start island,** and 4. **target island:** an end on an island reached only by ferry, with `trekking-noferries.brf`; then
   the reverse.
5. **No track found:** an end on a way the profile cannot use, with no connection it can.
6. **Data missing:** an end in a square whose tile is not in the folder (`W80_N40`, not captured).
7. **Data corrupt, short:** a copy of `E170_S50` cut short, made by the test.
8. **Data corrupt, checksum:** a copy with one index byte flipped, made by the test.
9. **The time limit:** `maxRunningTime = 50` on a case whose R1 run takes at least 1 s, measured in the same run; otherwise
   the case is void and not counted.
10. **A cancel before start:** `terminate()` before `doRun`. `terminated` is never reset (its only write is `:2433`), so the run
    fails at the loop's first pass (`:1975-1976`) **[R]**.
11. **A cancel mid-search:** `doRun` on its own thread; the test thread calls `terminate()` once `getLinksProcessed()` (`:2392`)
    passes 10,000, on a case whose R1 processes at least 100,000 links.

Kinds 9 to 11 compare the family only. Kind 11 needs a progress count in the port (a call beyond, for I). Kinds 1, 2 and 6
overlap Forager's `RoutingDataFolder` check, which C's `checkDataFile` covers separately.

#### E5. The sample size [C: `10-tests/logs/sample-size.txt`, `tools/sample_size.py`]

**What each figure is about.** A stratum is a population defined by its generator, tiles, profiles and params. Each figure is a
claim about that population only, not about routes in general or other tiles (Forager's `CLAUDE.md`, "A derived figure carried
across a boundary"). The required result in every exact assertion is zero discrepancies.

**Detection, exact.** If the port differs from the oracle on a fraction p of a stratum's cases, a clean run of n cases has
probability (1 − p)^n. The smallest n with (1 − p)^n ≤ β, for β = 0.05, is n = ⌈ln β / ln(1 − p)⌉. The script checks the
inequality itself at n and n − 1:
- **p = 5%:** ln 0.05 / ln 0.95 = 58.40, so **n = 59**: 0.95^59 = 0.0485 ≤ 0.05 < 0.95^58 = 0.0510.
- **p = 1%:** ln 0.05 / ln 0.99 = 298.07, so **n = 299**: 0.99^299 = 0.04954 ≤ 0.05 < 0.99^298 = 0.05004.

**The figures, [FBD]:**
- **R: 59 per profile, 26 profiles, 1,534 cases.**
  - Per profile: a defect touching at least 5% of that profile's routes is seen with probability 0.9515 or more (0.9980 at 10%).
  - Pooled: a clean run bounds the rate below **0.195%**, one-sided at 95% (1 − 0.05^(1/1,534) = 0.001951).
- **Each targeted stratum, 59:** a clean run bounds its rate below 4.95%.
- **FK, 110:** constructed, not sampled. Each case reaches its kind or does not; no rate is claimed.
- **F0, 2:** Forager's own check, carried over; no rate is claimed.
- **Total: 2,118 cases.**

**Where a difference is expected by design** (iOS under (b), `hmap` under (ii) or (iii)), the count is reported with its exact
two-sided 95% Clopper–Pearson interval, never as a pass. For n = 1,534: 0 gives 0 to 0.24%, and 5 give 0.11% to 0.76%. For
n = 59: 0 gives 0 to 6.1%.

**If K finds the full list too slow on the simulator [FBD]:** iOS runs every targeted, FK and F0 case, plus the first 12 random
cases per profile, 312. That is at least the 299 needed for p = 1%, and a clean run bounds the rate below 0.956%. A weaker claim
than the JVM's, and it is to be reported as such.

**Not used:** the rule of three (3/p: 60 and 300) and 9/p (180 and 900). The script prints them beside the exact figures only to
show the difference.

#### E6. What "identical" means, by platform and by the open options

**Two kinds of run [FBD]:**
- **Logic runs:** the port with library behaviour equal to the oracle's, asserted exactly equal to the oracle.
- **Production runs:** the port as it would ship. Asserted where the option makes equality the design; **recorded** where it does
  not, with E5's interval.

Where the two differ, the port needs a test seam: one internal interface for the scale table and `atan2`, and `hmap`'s map type
as a constructor parameter. A call beyond the dispatch, for I.

**The table and `atan2`, by part 6's options:**

| | (a) table constants written on the CI JVM | (b) each platform's `cos` and `atan2` | (c) fdlibm ported |
|---|---|---|---|
| **JVM, production** | exact on every stable case. The constants equal the oracle's `Math.cos` on the pinned JDK; part 7's invariants test guards that | exact: on the JVM `kotlin.math` calls `java.lang.Math` **[I, not read]**, in the oracle's own process | **recorded** against the default oracle: different by design at 54 rows. **Exact** against the oracle run with `-XX:-UseLibmIntrinsic`, whose table is `StrictMath`'s **[C]** |
| **JVM, logic** | the production run | the production run | the port with HotSpot's table injected, exact against the default oracle |
| **iOS, production** | exact against the stored digests if `atan2` takes (c); if it takes (b), NT is **recorded** | **recorded**: Apple's C library is on the path. This count is what (b) costs against HotSpot | exact against the no-intrinsic oracle's digests |
| **iOS, logic** | if `atan2` takes (b): the port with fdlibm `atan2`, exact | the port with the JVM's table (constants in the test source) and fdlibm `atan2`, exact | the production run |

- **(a) cannot hold `atan2` as constants** (part 7), so under (a) `atan2` takes (b) or (c). The rows above say which.
- **fdlibm's `atan2` stands for HotSpot's `Math.atan2` only where tried:** equal at 299,974 pairs (part 6). NT is where a
  difference would show.
- **Under (c), E needs a second oracle JVM** with the flag, run as its own Gradle test task. What the flag changes beyond the table
  was not checked **[?]**.
- **LB under (b) on iOS:** Apple's rows are known only after the invariants test first runs on the simulator. Then LB is generated
  in those rows that fall inside the tiles, by the rule above. If none does, LB reports that, and capturing a tile for it is a new
  decision.

**`hmap` order, by part 7's options.** This touches MP64, ML5 and PM5 only. Below about 42,100 nodes, hash order cannot change a
route (part 7) **[R][I]**.

| | (i) `java.util.HashMap`'s order in common code | (ii) each platform's `HashMap` | (iii) insertion order everywhere |
|---|---|---|---|
| **JVM** | exact on stable cases | exact on stable cases | production **recorded**; a logic run with `java.util.HashMap` injected, exact |
| **iOS** | exact on stable cases | production **recorded**; no logic run is possible on iOS | exact against the JVM port's own digests (the platforms agree); against the oracle, **recorded** |

Unstable cases (E3) are outside every exact assertion, under every option.

#### E7. What E leaves uncovered

- **Forager's Android app,** and HotSpot on other CPUs or versions (E2).
- **Phones under (b):** whether a phone's C library equals the simulator's **[?]** (part 6).
- **Other tiles, other profiles, more than two waypoints, other params:** outside the strata's populations (E5).
- **`correctMisplacedViaPoints`'s body:** unreachable through the entry point (E4).
- **Whether a simulator test can read tiles from the runner's disk** (part 5) **[?]**. Every route stratum on iOS depends on it;
  part 7's invariants test does not.
- **Run time,** on either job (K) **[?]**.

### F. The test tiles in CI (ruling c): where a pinned copy lives

Written by the fifth session on this dispatch (Hazel, part 4, 2026-10-29). **F is a stop for the owner's ruling.**
Below are the options and a recommendation. Nothing was chosen, hosted, uploaded or created.

**Bases [C: `07-tiles/00-bases-part4.txt`].**
- Forager `pre-main` is still `d100a4776`, the head RECORD -9 checked, so no new premise check is needed.
- Forager-iOS `main` is at `a283961` (RECORD -9).
- Forager reads stay at `e2969abe4`, in the read-only clone, whose tree is clean.

**What brouter.de serves.** Checked with directory listings and `HEAD` only. **No tile was downloaded.**
- **`segments4/` lists 1,142 `.rd5` tiles, 10,034,474,318 bytes in all, every one dated 29-Oct-2026 01:03**
  **[C: `07-tiles/logs/segments4-index-0354Z.tsv`]**.
  - Sizes: median 1,226,172 bytes; smallest 4,433; largest 252,948,700 (`E5_N45`).
  - The fixture's square, `W75_N40.rd5`, is 119,750,873 bytes in full. Forager's fixture is a sparse 152,665-byte square
    of the same name (premise 7).
- **Forager recorded the same URL's files as "rd5 files dated 2026-10-03"**
  (`docs/navigation/2026-10-03-trail-navigation-research.md:58`) **[R]**. The URLs are the same and the files are newer.
- **`HEAD` on three tiles** (`W75_N40`, `E5_N45`, `W15_S45`) returned **[C: `logs/head-run1.txt`]**:
  - `Last-Modified: Thu, 29 Oct 2026 01:03:01 GMT`;
  - a `Content-Length` equal to the listing's size;
  - `Accept-Ranges: bytes`;
  - an `ETag` such as `"6ae29b45-72340d9"`.
- **The `ETag` is not a content hash.** It decodes as the modification time and the length in hex: 2026-10-29T01:03:01Z
  and 119,750,873 for `W75_N40` **[C: `logs/etag-decode.txt`]**. Neither header can pin content, and both change on
  every rewrite **[I]**.
- **`segments4/diff/` shows the tiles regenerated in place, daily.**
  - Upstream's `Rd5DiffManager` names each diff after the MD5 of an earlier tile (`Rd5DiffManager.java:44`, `:54`). It
    also writes a zero-byte diff named after the current tile's MD5 (`:57-59`), and skips tiles under 1 MiB (`:36`)
    **[R, upstream `4d2639af`; quoted in `07-tiles/sources.txt`]**. The Android app asks for `diff/<tile>/<md5 of its
    copy>.df5` (`DownloadWorker.java:340`) **[R]**.
  - The listing follows that rule: 585 diff folders and 585 tiles of at least 1 MiB, with the same names
    **[C: `logs/diff-crosscheck.txt`]**.
  - **`diff/W75_N40/` holds 10 non-empty diffs, one dated each day from 20 to 29 October, and one zero-byte diff**
    **[C: `logs/segments4-diff-W75_N40-index-0356Z.tsv`]**. Their names are 10 different MD5s, so 10 earlier contents.
    So `W75_N40.rd5` had 11 different contents at one URL in 10 days **[I]**. That brouter.de runs this code, rather
    than a later version of it, is also inferred.
- **No dated archive at brouter.de.**
  - `segments4/` holds only the tiles, `diff/`, `storageconfig.txt` and a zero-byte file named
    `0_timestamps_are_map_snapshot_time_in_CET_timezone` **[C]**.
  - The diffs only lead forward, from an old tile to the current one, and go back 10 days. No earlier tile can be
    rebuilt from them **[I, from the source lines above]**.
  - `brouter.de/brouter/` is an HTML page, not a listing. The offline page says only "The Map-Snapshot date is about 2
    days before the timestamp of the routing data files". It states no update rate, archive or data licence
    **[R: `07-tiles/sources.txt`]**.
- **The Wayback Machine's capture index was asked about `W75_N40.rd5`** (an index query; no capture was requested).
  Both attempts were reset by the far end **[C: `logs/wayback-cdx-W75_N40.txt`]**, so this **could not be determined**.
- **v1.7.10 is still upstream's newest tag** **[C: `00-bases-part4.txt`]**. Which map creator brouter.de runs, and so
  whether the 1.7.10 engine reads today's tiles, cannot be told without a download **[?]**.
- **A second `HEAD`, 5.5 minutes after the first (04:00:35Z), returned the same headers apart from `Date`**
  **[C: `logs/head-run2.txt`]**. That shows only that nothing changed within those minutes. The daily rewrite is shown
  by `diff/`, not by the two runs.

**Conclusion: a SHA-256 pin on a brouter.de URL would break within about a day [I, from the computed listings].** The
planner's prediction holds: "brouter.de regenerates tiles in place, so a pinned copy has to be hosted elsewhere".

**Ruling c limits the options.** Forager's feasibility report states it as "fetched in CI, not committed. Rejected: more
fixtures in the repository" (`docs/audits/2026-10-26-ios-multiplatform-feasibility-report.md:304-306`) **[R]**.

**The options.** GitHub's figures were read from its own documentation on 2026-10-29; each line is quoted, with its URL,
in `07-tiles/sources.txt` **[R]**.

| | Where the pinned copy lives | What it needs | Size limits | Fits ruling c |
|---|---|---|---|---|
| 1 | **brouter.de itself,** pinned by SHA-256 | nothing | none | **Fails:** the bytes change daily (above) |
| 2 | **A release on Forager-iOS** holding the tiles, captured once from brouter.de in Phase 2. CI fetches each by URL and checks its size and SHA-256, as `fetch-brouter.sh:23-28` does. | **One repository action, by the owner or on the owner's word: create the release and upload the assets.** No account, setting or secret. **Forager-iOS is public** **[C: `logs/forager-ios-visibility.txt`]**. That a public release asset downloads with no token is inferred, not tested **[I]**. | Each file "under 2 GiB". "There is no limit on the total size of a release, nor bandwidth usage." | **Yes** |
| 3 | **Git LFS in Forager-iOS** | **The owner's LFS quota.** Every CI download counts against the owner's bandwidth. Past the quota, a $0 budget blocks LFS for the rest of the month; otherwise it is billed, which is an account matter and a stop. | GitHub Free and Pro: 10 GiB storage and 10 GiB bandwidth a month | **No in effect:** the tiles are tracked in the repository **[I]**. Needs ruling c reopened. |
| 4 | **Plain git in Forager-iOS** | a commit | Files over 100 MiB are blocked (`W75_N40` in full is 114.2 MiB), with a warning over 50 MiB. Repositories "ideally less than 1 GB". History keeps every byte. | **No:** this is the rejected "more fixtures in the repository" |
| 5 | **The GitHub Actions cache** | nothing | 10 GB per repository; entries unused for 7 days are removed | **Not a pinned copy:** a refill from brouter.de gets new bytes and fails the pin |
| 6 | **A bucket elsewhere,** for example Cloudflare R2 | **An account,** and perhaps a CI secret: a stop under this dispatch | not read | Yes |

**ODbL, the same for options 2, 3, 4 and 6.**
- Hosting the tiles publicly "Publicly Convey[s]" a derivative database of OpenStreetMap. ODbL 4.2 then asks for:
  - the licence or its URI;
  - kept notices, in "a location (such as a relevant directory) where users would be likely to look for it".

  **[R: ODbL 1.0, `sources.txt`]**
- For a release, that would be OpenStreetMap's attribution and the ODbL URI in the release notes and in a README asset.
  Forager's fixture README does the same (`server/pmtiles-worker/test/fixtures/rd5/README.md:7-9`) **[R]**.
- That is within the dispatch's "carrying ... OpenStreetMap's ODbL attribution". **No further licence question was
  found [I].** Whether ODbL asks more of a re-host was not examined. If the owner wants it examined, it is a licence
  question, and so a stop.

**Recommendation [I]: option 2, a release on Forager-iOS.**
- **Forager already trusts this form:** `fetch-brouter.sh` pins a GitHub release asset by URL, size and SHA-256
  (`:11-13`, `:23-28`) **[R]**. D's oracle is fetched the same way.
- **It meets ruling c as written:** the tiles are fetched in CI, not committed.
- **The only new act is creating the release, and that act is the owner's.** No account is needed and no setting
  changes.
- **GitHub sets no total or bandwidth limit on release assets,** so hosting does not cap E's sample size.
- **The risk:** anyone with write access can delete or replace an asset.
  - The SHA-256 pin catches a replaced asset.
  - A deleted asset makes CI fail loudly rather than pass.
  - A tile can be captured again from brouter.de, but never byte for byte (above).

**Left to E (part 5), not decided here:**
- **Which tiles, and so the size.** The choice is between:
  - full brouter.de squares: median 1.2 MB; `W75_N40` 119.8 MB; the largest 252.9 MB;
  - sparse squares built by BRouter 1.7.10's map creator from a pinned query, as Forager's fixture was: 152,665 bytes,
    built by "the same steps the Pi runs" (`server/pmtiles-worker/test/fixtures/rd5/README.md:21`) **[R]**.
- **The capture, for Phase 2.** Each tile is downloaded once and checked to load in the 1.7.10 oracle (the format
  question above). Then it is hashed and handed to whoever creates the release.

**The question for the owner, as one:** "Where should the pinned test tiles for the port's differential tests live?
(1) A release on Forager-iOS, which you create, holding tiles captured once from brouter.de; CI fetches them by URL and
SHA-256, as Forager fetches BRouter itself. This is recommended: no account, no setting, and it keeps ruling c's 'not
committed'. (2) Git LFS in Forager-iOS, which counts each CI download against your 10 GiB monthly LFS bandwidth and
reopens ruling c. (3) A storage bucket on another service, which needs an account."

### G. CI: what runs where, and when each job is worth adding

Written by the sixth session on this dispatch (Hazel, part 5, 2026-10-29). **Nothing was written under `.github/` and no
CI ran.** The sketch is text, in `08-ci/workflow-sketch.yml.txt`. Sources are quoted, with URL, date and SHA-256, in
`08-ci/sources.txt`.

**Bases [C].** Forager `pre-main` is still `d100a4776` (`git ls-remote`, 04:05Z), so no premise check is needed. Forager-iOS
`main` is `7043843`; `brouter-port` was `eee9a20` when this part started.

**Forager-iOS is public** (part 4, `07-tiles/logs/forager-ios-visibility.txt`) **[C]**. GitHub's billing page: "The following
usage remains free ...: Standard GitHub-hosted runners in public repositories", and "Larger runners are billed for public
repositories" **[R: `08-ci/sources.txt`]**. So both jobs below use standard labels only.

**What runs where.**

| Job | Runner label | Runs | Worth adding when |
|---|---|---|---|
| `jvm` | `ubuntu-24.04` (pinned, not `-latest`) | `jvmTest`: the port's unit tests and the differential tests against the oracle; then `compileKotlinIosArm64` and `compileKotlinIosSimulatorArm64` | **From Phase 2's first commit.** Tests over stubs need a runner to be seen failing, and the klib compile catches a JVM-only API in `commonMain` the day it is written. Part 2 compiled both iOS klibs on this Ubuntu x86_64 container with Kotlin 2.3.10 **[C: `04-libraries/probe-kmp/build.log`]**. Linking iOS binaries was not tried on Linux and is not expected there **[I]**. |
| `ios-simulator` | **`macos-26`** (pinned by name) with **Xcode 26.0.1** selected | `iosSimulatorArm64Test`: `commonTest` on the simulator | **Once `commonTest` holds a test whose answer can differ on iOS:** H's checks (hash order, floating point) and routes whose expected output was written on the JVM. Before that it would only re-run what the JVM job proves. |

**The macOS label [R: `08-ci/sources.txt`].**
- **Apple silicon.** GitHub's table for public repositories lists `macos-latest`, `macos-14`, `macos-15`, `macos-26` and
  `xcode-27` as "3 (M1)", "arm64". Only `macos-15-intel` and `macos-26-intel` are Intel. **C's dropping `iosX64` holds**
  for the recommended label.
- **`macos-latest` now points to the `macos-26` image** (its link in that table), so it is not used: it moves.
- **Xcode on the images:**
  - `macos-26` (image `20261013.0424.1`): 26.0.1, 26.1.1, 26.2, 26.3, 26.4.1, 26.5, 26.6 (default);
  - `macos-15` (image `20260907.0337.1`): 16.0 to 16.4 (default 16.4), 26.0.1, 26.1.1, 26.2, 26.3;
  - `macos-14`: 15.0.1 to 16.2 (default 15.4).

**Does Kotlin/Native 2.3.10 support that Xcode?** Three JetBrains sources, which do not agree:
- **The published table** (kotlinlang.org, "Version compatibility", read 2026-10-29) gives **Kotlin 2.3.10 with Xcode 26.0**
  **[R]**. `Xcode_26.0.1` is installed on `macos-26` and `macos-15`, so **either label supports C's "2.3.10 exactly"**, with
  26.0.1 selected explicitly. `macos-26` is recommended as the newer image; the choice between the two is mine **[I]**.
- **The Kotlin/Native 2.3.10 distribution** builds Apple targets with an Xcode 26.0.1 toolchain
  (`targetToolchain.macos_x64 = target-toolchain-xcode_26.0.1_17A400`, `konan.properties:92`), and sets
  `minimalXcodeVersion=12.5` (`:29`) **[C: read from the distribution in `~/.konan`]**. 17A400 is the build of
  `Xcode_26.0.1` on both images **[R]**.
- **The Gradle plugin's own warning threshold is lower.** `kotlin-native-utils` 2.3.10 sets `XcodeVersion.maxTested` to
  **16.0**, and KGP 2.3.10's `XcodeVersionService` reports "Xcode Version Too High for Kotlin Gradle Plugin" whenever the
  selected Xcode compares above it, by major then minor **[C: `javap` of both jars, SHA-256s in `08-ci/sources.txt`]**. So
  **every Xcode on either image would print that warning,** 26.0.1 included **[I, from the bytecode; not run on a Mac]**. It is a
  warning, suppressible by a `gradle.properties` flag, not an error.
- **Not tested [?]:** whether 2.3.10 links and runs `iosSimulatorArm64Test` with Xcode 26.0.1 on that runner, and which iOS
  simulator runtimes the image holds for 26.0.1. The first CI run in Phase 2 is that test. Every iOS result is to be reported as
  "simulator-checked" (`CLAUDE.md` of this repository).

**A finding against C, for the planner [R].** The same table gives Kotlin 2.3.10 with **Gradle 7.6.3–9.0.0** and **Android Gradle
plugin 8.2.2–9.0.0**. C recommends Gradle 9.7.0 with Kotlin 2.3.10, Forager's own pair, and Forager builds with AGP 9.3.1. Both
are **outside JetBrains' published range**. The first Kotlin line whose range covers Gradle 9.7.0 and AGP 9.3.1 is 2.4.20–2.4.21
(Xcode 26.4). Parts 2 and 3 built with Gradle 9.7.0 and Kotlin 2.3.10 without error **[C]**, so the pair works for what was
built; it is not a supported pair. This does not change Forager and is not a stop in the dispatch. Whether the port keeps C's
Gradle 9.7.0, drops to 9.0.0 for the port alone, or waits for Forager to move Kotlin is for the planner and owner.

**Where G depended on F, written for the ruling [I].**
- **Tiles reach CI by URL from a Forager-iOS release,** each checked for size and SHA-256 before use, as `fetch-brouter.sh:23-28`
  checks BRouter's zip, and as D's oracle is fetched. One small script serves both (`tools/fetch-pinned.sh` in the sketch).
- **No secret and no token.** A public release asset is fetched anonymously. That is inferred, not tested (F's disclosure stands).
- **Both jobs fetch the tiles.** The simulator test reads them from the runner's disk; that a simulator test process can read a
  host path was not checked **[?]**.
- **Caching is optional.** An `actions/cache` entry keyed by the tiles' SHA-256s saves downloads. A miss refetches from the
  release and re-checks the pin, so the cache never becomes the pinned copy (F's option 5).
- **The jobs that read tiles wait for the release,** which the owner creates in Phase 2. Until then the `jvm` job runs tests that
  need no tile.

### H. Floating point, and hash order (parts 5 to 7; the Android question in part)

Written by the sixth session (Hazel, part 5, 2026-10-29). **The hash-order question is answered. The floating-point list is
computed but not yet classified, and the simulator test plan is not written.** Both are for the next session.

**Method [C: `09-float/`].** `09-float/tools/fp_sites.py` reads part 1's `javap` dump of the vendored classes and T2's 481
reached methods, and lists every floating-point and hash-iteration instruction by source line (`09-float/out/T2/sites.tsv`).
Checked against things outside it: 481 of 481 methods found; 923 methods parsed against 923 `descriptor: (` lines in the dump.
Its first run reported no float printed into a string, which was the script's blind spot (Java 11 bytecode concatenates with
`invokedynamic`), not the code's; fixed, it finds 4 (`09-float/README.txt`).

**Hash order: B's nine sites, re-checked [C][R].**
- **Three of B's nine are not on the reached path at any tier** (T0 to T3 list 0 of each):
  `BExpressionContext.java:453` (`dumpStatistics`), `:524` (`variableName`) and `RoutingParamCollector.java:260`
  (`setProfileParams`). Nothing in BRouter or Forager main sources calls `setProfileParams` or `dumpStatistics`;
  `variableName`'s one caller builds an error message at `BExpressionContext.java:517`, in an unreached method. B row 18 named
  them from source, not from the reached set. **Correction to B.**
- **The scan finds seven reached sites,** six of B's and `RoutingEngine.java:1568`, which B listed as a sorted set (row 6).
  A wider pass over every `java.util` iteration call in the reached methods found no other map or set iteration.

| Site | What is iterated | Can order change a result? |
|---|---|---|
| `RoutingParamCollector.java:188`, `setParams` | the caller's params map | **Not for Forager's calls [R].** Order matters only where two keys write one field: `heading`/`direction`; `turnInstructionMode`/`timode`/`turnInstructionFormat`; `format`/`trackFormat`; and the order `profile:` keys enter `keyValues`. Forager passes three keys, none in conflict (`BRouterTrailRouter.kt:151-158`). The order is the caller's map's, so the port's entry point fixes it. |
| `BExpressionContext.java:863`, `_parseFile` | `keyValues` (the `profile:` params) | **Not for Forager's calls [R]:** one key, `use_dynamic_range`. With more, each key becomes a constant assignment to its own variable (`BExpression.java:395-404`); order changes only the variable numbering. **Whether numbering can change a result was not read [?].** |
| `RoutingContext.java:107`, `getKeyValueChecksum` | `keyValues` | **No [R]:** a sum of hash codes, which does not depend on order. Its value feeds the profile cache key (`ProfileCache.java:45`), so it needs `String.hashCode` to be consistent on one platform, not equal across two [I]. |
| `BExpressionMetaData.java:67`, `readMetaData` | the contexts (`way`, `node`) | **Only a failure's message [R]:** `finishMetaParsing` touches its own context (`BExpressionContext.java:310-319`). With two contexts missing data, order picks which one the exception names. A valid `lookups.dat` throws nothing. |
| `NodesCache.java:393`, `close` | open files | **No [R]:** closes each, ignoring `IOException`. |
| `RoutingEngine.java:1568`, `recalcTrack` | `directMap.keySet()` into a `TreeSet` | **No [R]:** sorted. |
| `OsmNodesMap.java:181`, `collectOutreachers` | all nodes in `hmap` | **Possibly, not determined [?].** Every node in `hmap` is visited, and whether it vanishes depends on its own distance only (`:198-203`), so the vanished set does not depend on order **[I]**. The re-visits, and so `nodesCreated`, do, because `vanish()` unlinks neighbours as it goes (`OsmNode.java:205-217`) **[I]**. `nodesCreated` feeds the memory-pressure decisions. This is the memory-pressure path, which never ran on the sample (A); **E has to reach it** for the port to be compared there. |

- **For Forager's calls, at the sites read, hash order changes no route [R][I].** Two sites are open: variable numbering under
  more `profile:` keys, and `collectOutreachers`.
- **What the port does about it [I]:** keep `HashMap` where order cannot matter; at `collectOutreachers`, either show by test
  that order cannot change the route, or iterate in an order both platforms share. Kotlin/Native's `HashMap` iteration order
  itself was **not read or tested** in this part; the answer above needs it only for `collectOutreachers`.
- **Hash codes on the path [C], for the next session:** `String.hashCode` (`RoutingContext.getKeyValueChecksum`,
  `RoutingEngine.doRouting`), `Arrays.hashCode` (`BExpressionContext.evaluate`, a cache key), `OsmNode.hashCode`
  (`ilon + ilat`, `OsmNode.java:259-261`; used in `WaypointMatcherImpl.end`), and `Object.hashCode` through `LruMap.put` and
  `removeLru`. Whether any feeds a choice rather than a cache was not read.

**Floating point: the computed list, not yet classified [C: `09-float/out/T2/`].**
- **68 of T2's 481 reached methods** hold a floating-point instruction: 383 arithmetic instructions in 39 methods, 226
  comparisons in 38, 239 conversions in 45, 63 `Math` calls in 21, 35 `Float`/`Double` calls in 10, 4 floats printed into
  strings.
- **The library functions whose last bit is not fixed by IEEE 754** are the next session's first reading: `Math.cos` in
  `CheapRuler.calcKxKyFromILat` (`:37`, the latitude scale table built at class load); `Math.sin`/`cos` in
  `OsmPath.addAddionalPenalty` (`:282-283`); `Math.exp` in `StdPath.calcIncline` (`:283`) and `computeKinematic` (`:310`);
  `Math.atan2` with `toDegrees` in `CheapAngleMeter.getAngle` (`:54`). `sqrt` (correctly rounded on both) appears in
  `CheapRuler.distance`, `WaypointMatcherImpl.checkSegment`, `RoutingContext.calcDistance` and `CheapAngleMeter.calcAngle`.
- **Parsing and printing:** `Float.parseFloat` (profile numbers, `BExpression.java:234`, `:401`; `getLookupValue` `:267`),
  `Double.parseDouble` (waypoints, `RoutingParamCollector.java:90`; `RoutingEngine.postElevationCheck` `:760`),
  `Float.toString` (`BExpressionContext.java:225`).
- **Not done:** which of these choose between routes, whether the JVM and Kotlin/Native can differ at each, and the simulator
  test. One cheap test the list suggests, not designed or run: the latitude scale table has 1,800 entries computed from fixed
  inputs, so its bits can be written on the JVM and compared in full on the simulator.

#### Part 6, step 1: which sites can choose between routes

Written by the seventh session (Hazel, part 6, 2026-10-29). Every method in `09-float/out/T2/sites.tsv` is classified by hand
from its source, with the reason and line, in `09-float/out/T2/classification.tsv` **[R]**. `09-float/tools/classify.py`
writes the table and checks it against `sites.tsv`: 72 of 72 methods classified, none missing, none extra **[C:
`out/T2/classification-summary.txt`]**. The 72 are part 5's 68 floating-point methods, counted by descriptor (66 names:
`getStartPath` and `readPosition` each have two overloads), plus the 6 that only iterate a hash map.

| Class | Meaning | Methods |
|---|---|---|
| **R** | can choose between routes, or change the cost, the node list or the failure kind, on Forager's calls with some profile ("Any profile", RECORD -8) | 30 |
| **Rp** | the same, but only with a request parameter Forager never passes: a heading, nogos, via points | 5 |
| **P** | parse or setup: turns profile text into the numbers R sites use | 4 |
| **D** | output Forager reads: `track.distance` (`BRouterTrailRouter.kt:115`) | 2 |
| **O** | output Forager does not read: time, energy, elevations, ascend, turn angles, voice hints | 17 |
| **M** | messages, logs, debug text | 6 |
| **H** | hash iteration only (step 3) | 6 |
| **X** | a branch the port fails as "unsupported" or never runs (B rows 4, 14) | 2 |

**What the 30 R methods do with floating point [R].** Of the 30, 19 use only IEEE basic operations (add, subtract,
multiply, divide, `sqrt`, compare, conversions). 5 read CheapRuler's scale table, 3 call a library function, 2 parse text
and 1 prints a float. They form five chains:
- **The scale table.** `CheapRuler.calcKxKyFromILat` builds 1,800 `(kx, ky)` pairs with `Math.cos` at class load
  (`CheapRuler.java:20-47`). Every distance on the path multiplies by them:
  - the per-link cost distance (`RoutingContext.calcDistance`, `OsmPath.java:238`);
  - the air distance in the open-set key and the pruning bound (`OsmNode.calcDistance`, `RoutingEngine.java:1854`, `:1994`,
    `:2230-2234`);
  - waypoint matching (`WaypointMatcherImpl.checkSegment`);
  - the turn angle (`CheapAngleMeter.calcAngle` → `StdPath.java:89`).
- **Waypoint matching** picks each waypoint's way and its crosspoint, which becomes the route's first or last node:
  - radius tests (`WaypointMatcherImpl.java:117-135`, `NodesCache.java:298-300`, `RoutingEngine.java:1621`, `:1825`, `:1855`);
  - the crosspoint, truncated to int (`WaypointMatcherImpl.java:145-146`; `RoutingContext.java:504-523`);
  - **the tie-break between equally near ways,** by `directionDiff` (comparator, `WaypointMatcherImpl.java:66-73`, then
    `wayNearest.get(0)`, `:229-235`). `directionDiff` comes from `Math.atan2` through `CheapAngleMeter.getDirection`
    (`:52`, `:61`, `:189`, `:211`). Two ways meeting at the node nearest a waypoint have exactly equal radii, so this
    tie-break is reached whenever a waypoint's nearest point is a junction **[I]**.
- **The cost formula.** `OsmPath.addAddionalPenalty`, `StdPath.processWaySection` and `processTargetNode`,
  `OsmPath.interpolateEle`, and the profile evaluator (`BExpression.evaluate` and friends): float arithmetic cast to int at
  `OsmPath.java:166`, `:315`, `:384`.
- **The search.** The open-set key `cost + (int)(airdistance * airDistanceCostFactor)` (`RoutingEngine.java:1994`, `:2022`,
  `:2265`); the pruning bound `maxTotalCost * lastAirDistanceCostFactor + addDiff` (`:2234`); pass selection (`:1743`).
- **Text to numbers.** The waypoints (`RoutingParamCollector.java:90`, `:96-97`), lookup values (`getLookupValue`), profile
  literals and `profile:` values (P). One float is printed where a cost can read it: `getKeyValueDescription` (`:225`), used
  by the crossing penalty (`StdPath.java:98-130`), which needs `bikeMode` and `consider_crossing`, both profile settings,
  and reads one character.

**What does not choose [R].**
- **Time, energy and voice hints (O)** never feed a cost or the node list. `computeKinematic` returns early outside
  `detailMode` (`StdPath.java:292`). No voice-hint class removes a node or sets a cost (grep of `VoiceHint*.java`).
- **The `exp` calls** (`StdPath.java:283`, `:310`) are O.
- **The `sin`/`cos` at `OsmPath.java:282-283`** are Rp: they need `startDirectionValid` (`RoutingEngine.java:1931`).
- **The `atan2` in `snapPathConnection`** (`RoutingEngine.java:1326-1327`) is Rp: that method returns at once unless a
  waypoint is named `via…` or `rt…` (`:1266-1267`), and Forager's two are `from` and `to` (`RoutingParamCollector.java:48-50`).
- **Of the library functions on the path, two reach a route on Forager's calls: `cos` (the table) and `atan2` (the
  tie-break).** `track.distance` (D) depends on the table too.

#### Part 6, step 2: can the JVM and Kotlin/Native differ at those sites?

**Method [C: `09-float/probe/`, `09-float/part6/`].**
- **The scratch probe** is a Kotlin Multiplatform project built outside the repository with Gradle 9.7.0 and Kotlin 2.3.10.
  `commonMain` mirrors each R chain's expressions in Java's types and evaluation order. `commonTest` writes their results as
  bit patterns.
- **It ran on three platforms:**
  - the JVM: HotSpot 21.0.12.1 on amd64;
  - Kotlin/Native `linuxX64`;
  - Kotlin/Native `linuxArm64`, cross-compiled here and run under JetBrains' `qemu-aarch64` 5.1.0, with the aarch64 glibc
    2.25 sysroot.

  Each ran 10 of 10 tests, timestamped 04:40Z, with no compile error in the logs (`part6/junit-counts.txt`, `part6/logs-*.log`).
- **Inputs:**
  - the 928 tokens of all 27 `.brf` files (26 from the release zip, plus Forager's pinned profile);
  - the 611 value names in `lookups.dat`;
  - 180,000 stress decimals, including 120,000 at or within 10^-40 of float halfway points (`tools/make_corpus.py`);
  - 299,974 integer `atan2` pairs;
  - the 1,800-row scale table;
  - 220,001 numbers printed.
- **Compared line by line** with `tools/compare.py`. The libm results were also recomputed with the host's C library through
  Python (`tools/libm_check.py`), a check from outside the probe.
- **Kotlin/Native's toolchain was read** where it decides the answer (`09-float/part6/kn-reads.txt`):
  - the compiler jar;
  - the `linux_x64` runtime bitcode, disassembled with the bundled clang;
  - `konan.properties`;
  - the native stdlib sources.

**Results [C unless marked].**

| Kind | JVM vs `linuxX64` | JVM vs `linuxArm64` | `linuxX64` vs `linuxArm64` | What decides it |
|---|---|---|---|---|
| IEEE basic operations, with the fused-multiply detectors | 0 differ | 0 differ | 0 differ | Kotlin/Native does not fuse (below) |
| `cos`, the scale table (rows of `lat, cos, kx, ky`) | **8 of 1,800** | **3 of 1,800** | 5 of 1,800 | the C library: Kotlin/Native's `cos` is a direct call to C's `cos` (`part6/kn-reads.txt` 3) |
| `atan2` (with `toDegrees` and `normalize`) | **50,215 of 299,974** | **50,150** | 449 | the same, C's `atan2` |
| `exp`, `sin`, `cos` (O and Rp sites) | 64 of 27,601 | 53 | 27 | the same |
| `toFloat`, `toDouble`, accepted or rejected, and bits | 0 of 181,539 | 0 | 0 | Kotlin/Native's own parser (Harmony's, in its C++ runtime), the same on each target **[I]** |
| `Float.toString`, `Double.toString`, round trip | 0 of 220,001 | 0 | 0 | Kotlin/Native's own `NumberConverter` |
| `roundToLong` | 2 of 10,012 | 2 | 0 | **the stdlib itself differs** (below) |
| `HashMap` iteration order | differs | differs | the same | step 3 |

- **`linuxX64` equals the host C library exactly:** `atan2` 299,974 of 299,974, `cos` 1,800 of 1,800 (glibc 2.39). The JVM
  equals it at 249,759 and 1,792 (`part6/libm-check-*.txt`). So the differences are the C library's against the JVM's, not
  the probe's.
- **The JVM is not one answer either:** on the same HotSpot, `Math.cos` and `StrictMath.cos` differ at **60 of the 1,800**
  table rows. `Math.atan2` equals `StrictMath.atan2` at all 299,974 (`part6/results/jvm-amd64/jvm-only.txt`). The oracle's
  table therefore depends on which JVM runs it. A HotSpot on another CPU, or Android's runtime, may build a different table
  **[I]**.

**Multiply-add on arm64: not fused [C][R].**
- **Detectors.** `a*b - c` with `a*b = 1 - 2^-54` gives `0` unfused and `-2^-54` fused; the float form uses `1 - 2^-26`. Both
  print the unfused result on `linuxArm64`, in debug and release executables and in the test binary (`part6/fma-run.txt`).
  The first float detector was wrong: with `2^-12` its product was exact. It was fixed to `2^-13` and rerun; the logs of
  both builds are kept.
- **Disassembly** of both arm64 executables with `llvm-objdump`: no `kfun:` symbol (722 in release, 2,288 in debug) holds
  an `fmadd`, `fmsub`, `fnmadd` or `fnmsub` (`part6/fma-disasm.txt`). The only fused instruction in either binary is in the
  runtime's C++ `Kotlin_native_NumberConverter_bigIntDigitGeneratorInstImpl`, compiled from an `llvm.fmuladd`. That is the
  positive control: the scan can see a fused instruction where one exists.
- **Why** **[R]:**
  - the backend never sets fast-math flags. No class under `org/` in the compiler jar references `LLVMSetFastMathFlags`,
    `fmuladd`, `unsafe-fp-math` or `fp-contract`; 0 of 1,518;
  - every target compiles its IR with `clang -cc1 -emit-obj -disable-llvm-passes -x ir` and no `-ffp-contract`
    (`konan.properties:170-176`, `:222-225`, `:582-585`).
- **For iOS this is inferred, not run** **[I]:** iOS uses the same backend and the same flags, plus `-mllvm -fast-isel=false
  -mllvm -global-isel=false` in debug, which does not enable fusion.
- **Would fusion have mattered?** A JVM search with `Math.fma` found no input, in 50,000,000 random tries at each of four
  int-producing multiply-add sites, where fused and unfused give different ints (`part6/fma-sensitive-search.txt`). The four
  sites were `readPosition`, `turncost`, the section cost and the rounded distance. Rare, not impossible.

**Kotlin's own standard library differs between the JVM and Native [C][R].**
- **`roundToLong`.** On the JVM it is `Math.round`; on Native it is `floor(x + 0.5).toLong()`
  (`nativeMain/kotlin/math/math.kt:590-595`). They differ at `0.49999999999999994` and `2^52+1`, the 2 lines above.
  - BRouter's three `Math.round` sites take a distance and clamp it with `Math.max(1.0, …)` (`OsmNode.java:103`,
    `OsmPathElement.java:78`, `RoutingContext.java:554`).
  - For `x >= 0.5` and below `2^52`, `floor(x + 0.5)` equals `Math.round(x)`, and below `0.5` the clamp gives 1 either way.
    So the port can write `floor(x + 0.5)` on every platform at these sites **[I]**: argued here, with no counterexample in
    the 10,012 probe values, which include `n + 0.5` and the double just below it for n up to 5,000.
- **`kotlin.math.round`** is `rint`, which rounds halves to even (`part6/kn-reads.txt` 3). It is not `Math.round`. A port that
  used it would be wrong on every platform **[R]**.
- **The same on all three** **[C]:** `toInt` (NaN to 0, saturating), `min`/`max` with `-0.0` and NaN, and `%` (exact
  `fmod`).

**What a `linuxX64` or `linuxArm64` run can and cannot stand for on iOS.**
- **It stands for** **[I]**, because the iOS build compiles the same Kotlin through the same backend, and the same
  runtime C++ for an arm64 target:
  - IEEE arithmetic in Kotlin code;
  - the absence of fusion;
  - `toInt`, `min`/`max`;
  - parsing and printing; the arm64 run covers the one fused instruction in the number printer;
  - `HashMap` order;
  - `String.hashCode`.
- **It cannot stand for** `cos`, `sin`, `exp` and `atan2`. On iOS these are Apple's C library. Two glibc builds already
  disagree with each other (449 `atan2` results, 5 table rows), so no Linux run says anything about Apple's results
  **[C][I]**.
- **Whether the simulator's C library gives the same bits as a phone's** could not be determined **[?]**.

**At each R chain, for Forager's calls.**
- **The scale table (`cos`).** It **can differ, and did on every pair of platforms tried.** A different entry moves every
  distance in its 0.1-degree latitude band by about an ulp. That changes a route's ints only where a value sits within an
  ulp of a rounding boundary: the `Math.round` of a distance, or the `(int)` casts in the cost. Both `track.distance` (D)
  and the cost can change. Whether a route changes on real tiles was **not determined [?]**; that is E's work.
- **The waypoint tie-break (`atan2`).** It **can differ** (50,215 of 299,974). The comparator decides by `directionDiff`
  only between candidates with exactly equal radius. Every match has at least two such candidates: `end()` adds each way
  twice, once per direction, with the same radius (`WaypointMatcherImpl.java:194-227`). Ways that meet at the nearest node
  add more.
  - The search starts from both directions of the chosen way (`RoutingEngine.java:1937-1938`), so a flip between one way's
    two directions should not change the route **[I]**.
  - A flip between two different ways at a junction could. That needs their `directionDiff`s within an ulp, a near-tie
    that integer geometry can produce but that was not constructed here **[?]**.
- **Everything else on the R path** is IEEE basic arithmetic, parsing, `toInt` or `min`/`max`. These **cannot differ**,
  given the same table and no fusion **[C][I]**.
- **What this means for the port** **[I]**, for the planner (options, not decided):
  - **(a)** put the scale table into the port as 3,600 constants, written from the oracle's JVM in CI. Then iOS reads the
    same bits as the CI oracle, and no C library is involved;
  - **(b)** compute it with `kotlin.math.cos` on each platform, and accept iOS differences from the oracle;
  - **(c)** port fdlibm's `cos` (`StrictMath`). It is the same everywhere, but differs from the HotSpot oracle at 60 rows.

  `atan2` has the same three options. For (a) the domain is every integer pair, so a ported fdlibm `atan2` is the
  constant-free form. The JVM's `Math.atan2` equals `StrictMath.atan2` on all 299,974 pairs tried.

#### Part 7, step 3: hash order

Written by the eighth session (Hazel, part 7, 2026-10-29), from part 6's committed evidence and one new run
(`09-float/part7/`). Bases re-checked: Forager `pre-main` is still `d100a4776` **[C: `part7/00-bases-part7.txt`]**.

**Kotlin/Native's `HashMap` iterates in insertion order [R][C].** It appends at `length++`
(`nativeWasmMain/kotlin/collections/HashMap.kt:361`), marks a removal with a tombstone (`:395`) and iterates by index
(`:576`); re-read in part 7 from part 6's unpacked 2.3.10 stdlib sources. The probe's 3,000 `OsmNode`-shaped keys came back
as 0, 1, 2, … on both native targets and in bucket order on the JVM (`part6/results/*/hash.txt`). The code is Kotlin with no
platform call, so `linuxX64` stands for iOS here **[I]**. The JVM's order is `java.util.HashMap`'s, which the oracle uses.

**`_parseFile` with more than one `profile:` key: the port need not copy the JVM's order [R][I].** The injected keys are
assigned in `keyValues.keySet()` order (`BExpressionContext.java:860-865`), each to its own index as a constant
(`BExpression.java:395-404`), before the profile's own expressions. Order changes only which index each key gets, and every
later read goes by name (`BExpressionContext.java:836-838`). Re-read in part 7 at those lines; part 6's further reading
(`:439-446`, `:512-524`) was not repeated. Forager passes one key, `use_dynamic_range`, so its calls have no order to
choose.

**`OsmNodesMap.collectOutreachers`: order changes a count, and the count can change the failure kind [R][C][I].**
- **Where order enters, read in the real loop** (`OsmNodesMap.java:178-208`) **[R]:**
  - every node in `hmap` is pushed in `hmap.values()` order (`:181-183`);
  - a popped node pushes each neighbour again unless that neighbour was already popped or re-pushed (`:192-197`);
  - each pop of a node with no links left takes one off `nodesCreated` (`:204-205`), and `vanish()` removes links as it
    goes (`OsmNode.java:205-217`).

  So `nodesCreated` depends on the pop order.
- **What does not depend on order [I], argued from the same lines:**
  - a node is pushed by the first of its neighbours to be popped, because a link is removed only when one of its own ends
    vanishes, and an end vanishes only after it has pushed its neighbours (`:188-203`). So every node reachable from `hmap`
    is visited in any order;
  - whether a node vanishes depends on its own distance and on three fields fixed for the call (`:198-202`);
  - so the set of vanished nodes, the links left and the visit IDs are the same in any order. `canEscape`, which runs next
    (`RoutingEngine.java:2013-2019`), reads only those. Only the count differs.
- **The count, measured on a simplified copy of the loop [C, on a reduction].** This result is not from BRouter's code. It
  is from `OutreachSim` (`09-float/probe/src/commonMain/kotlin/probe/Probe.kt:44-71`), part 6's Kotlin copy of the loop. It
  differs from the real one in six ways:
  - links are a `List` of neighbours, not `OsmLink` chains;
  - `vanish` is reduced to unlinking both ends;
  - every node is in `hmap`;
  - the distance is Manhattan, not CheapRuler's;
  - the guard at `:198` is taken as true;
  - the graphs are 20 random ones of 400 nodes, not tiles.

  On those graphs, `nodesCreated` differs between the JVM and native in **14 of 20**, by exactly one each time, the JVM higher.
  The set of nodes left with no links is equal in 20 of 20, and `linuxX64` equals `linuxArm64` in 20 of 20 (recounted from
  the committed files: `part7/outreach-recount.txt`).
- **What the count decides [R].**
  - **Whether this path runs on Forager's calls.** A collection starts when `nodesCreated * 95 + npaths * 200` passes
    `currentmaxmem` (`OsmNodesMap.java:107-122`; `RoutingEngine.java:2006-2012`). `currentmaxmem` starts at 4,000,000
    (`OsmNodesMap.java:24`). Forager never sets `memoryclass`: no match under `app/src/main`, so it keeps 64
    (`RoutingContext.java:54`). The cap is then `2 * 64 MiB / 3` = 44,739,242 bytes (`RoutingEngine.java:1812`,
    `NodesCache.java:68`). **So a search holding about 42,100 nodes reaches `collectOutreachers` on Forager's calls
    [computed: 4,000,000 / 95].** A's sample never reached it.
  - **What happens after a collection** (`RoutingEngine.java:2024-2031`, `OsmNodesMap.java:110-122`):
    - with `extend`, `currentmaxmem` rises to `total + 1,900,000`, capped at the 44,739,242;
    - the check fails only when `total` passes that cap. The engine then throws "memory limit reached", or, with no cost
      bound yet, turns on memory panic mode.
  - **So a count one node apart moves `total` by 95 bytes [computed].** It changes the failure kind only when `total` sits
    within 95 bytes of the cap **[I]**. It also moves `currentmaxmem` by 95 bytes whenever the cap is not reached, and so
    the step at which the next collection starts **[I]**. Each collection's vanish threshold uses the cost of the path in
    hand (`RoutingEngine.java:2143-2144`).
  - **Whether a moved collection changes a route was not traced [?].**
- **New in part 7: the oracle's own order is not a function of the input in one case [R][C].**
  - **What the source says [R].** `java.util.HashMap` turns a bucket into a tree once it holds more than eight keys, in a
    table of at least 64 (`HashMap.java:260`, `:275`, `:646-649`, `:763`; `OsmNodesMap`'s table starts at 4,096, `OsmNodesMap.java:16`).
    - Iteration follows the bucket's `next` links (`:1601-1612`).
    - In a tree bucket, a new key is linked in after its tree parent (`:2161-2173`), and the root is moved to the front
      (`:1990-2010`).
    - Between two keys with equal hashes whose class is not `Comparable`, the tree picks a side by
      `System.identityHashCode` (`tieBreakOrder`, `:2058-2066`). `OsmNode` is not `Comparable` (`OsmNode.java:14`), and
      its hash is `ilon + ilat` (`:259-261`), so every node on one anti-diagonal shares a hash.
    - Source: OpenJDK `jdk21u` at tag `jdk-21.0.12.1-ga`, `src/java.base/share/classes/java/util/HashMap.java`, read
      2026-10-29 (`part7/sources.txt`). It is the version of this container's HotSpot, 21.0.12.1 (Ubuntu build); whether
      Ubuntu patches it was not checked.
  - **What it does [C: `part7/treebin-order.txt`, `part7/TreeBinOrder.java`].** The program uses BRouter's own `OsmNode`,
    compiled from the vendored tree with no error. Twelve nodes share one `ilon + ilat`, and 2,000 others are spread out.
    Each map is a `new HashMap<>(4096)` built from fresh objects with the same coordinates, in the same order.
    - **Within one JVM, five builds put the twelve in five different orders.** The 2,000 spread keys came back in the same
      order in 5 of 5 builds, the control.
    - **Three separate JVMs repeated the same five orders,** so the order follows the process's history of identity hashes,
      not the coordinates **[C][I]**.
  - **What it means [I].** Where a real `hmap` bucket holds nine or more nodes and some share `ilon + ilat`, the oracle's
    `collectOutreachers` count depends on what the process did before. No port can reproduce that, and two oracle runs in
    different test orders may disagree with each other. **Whether real tiles fill a bucket that far was not determined
    [?].** At a load of at most 0.75 it is rare under even hashing. Nodes bunched along one anti-diagonal make it likelier.
- **What the port can do at `hmap`, offered and not chosen (the planner's call, Phase 2) [I]:**
  - **(i) Copy `java.util.HashMap`'s order for this one map in common code:** the table size, the hash spread, the resize
    split and the tree buckets. Tree buckets need `TreeNode`'s list handling, and its balancing too, because the
    balancing decides which node goes to the front. It matches the oracle except at equal-hash keys in a tree bucket, where
    nothing can.
  - **(ii) Use each platform's `HashMap`.** On the JVM this is `java.util.HashMap`, so the JVM port equals the oracle with
    the same exception. On iOS the order is insertion order, which differs from the oracle **[C, on the reduction]**.
  - **(iii) An insertion-ordered map on every platform.** The platforms agree with each other, and both can differ from the
    oracle at this count.
- **Is running the real code needed before Phase 2? Not to start Phase 2; yes before E's expected results on this path
  are fixed [I].**
  - **What is settled without it:**
    - the dependence on order is read in the real loop above;
    - the simplified copy shows only that it is not zero on toy graphs;
    - the tree-bucket finding shows that matching the oracle exactly is impossible in one case, whichever of (i) to (iii)
      is taken.
  - **What only the real code can show:** how often the count differs on real tiles, and whether that ever moves a
    decision. One cheap form needs no port: run the Java engine twice in one JVM on a route long enough to collect, and
    compare its own log line, "collected, nodes/paths before=… after=…" (`RoutingEngine.java:2025`).
    - If the oracle agrees with itself, it shows the count is stable on that route.
    - If it does not, exact comparison on this path is off for any port, and E compares something coarser there.

    It belongs to E (part 8) or to Phase 2's first step; the planner decides which.
- **For Forager's calls, the routes found below about 42,100 nodes cannot depend on hash order at any reached site [R][I].**
  Above it, the failure kind can **[I]**, and the route is undetermined **[?]**.

#### Part 7, step 4: the simulator test (design only)

Written by the eighth session (Hazel, part 7, 2026-10-29). **A design, not built or run:** no workflow file, no CI run, and
nothing under `.github/`. It is G's `ios-simulator` job (`08-ci/workflow-sketch.yml.txt`) running one `commonTest` class, a
**platform-invariants test**, beside the route tests E will define. Every choice below is **[I]** unless marked.

**It needs no file on the simulator.** Whether a simulator test process can read a path on the runner's disk is still
**[?]** (part 5). Part 6's draft writes its results to files (`probe/src/commonTest/kotlin/probe/ProbeTest.kt:9`). The
design does without files:
- **the inputs are generated in common code** with integer arithmetic only. That covers the 1,800 table rows at
  `ilat = i * 100000 + 50000` (as `ProbeTest.kt:35-36`), and the `atan2` pairs from the same 64-bit LCG as `ProbeTest.kt:5`
  and `:43-50`;
- **the expected values are constants in the test source.** They are digests (FNV-1a, 64-bit, over each result's raw bits,
  integer-only so the same on every platform), one per chunk of 100 table rows and one per 10,000 `atan2` pairs. A chunk
  digest says where a mismatch is without a file or a log. The full lines go to standard output only on a mismatch.
- **Why digests and not the values:** the table alone is 7,200 doubles. A `longArrayOf` that size sits near the JVM's 64 KB
  method limit, and `commonTest` compiles for the JVM too. The `atan2` corpus is 4.8 MB as hex.

**Where the expected values come from: the oracle's JVM, pinned.**
- A `jvmTest` in the same class computes the same digests with `java.lang.Math` (`Math.cos`, `Math.atan2`) and fails if they
  differ from the constants. It prints the new values, so the constants change only by a deliberate commit.
- That test runs in the `jvm` job on `ubuntu-24.04`, x86_64, with the JDK pinned to an exact version. The constants'
  comment names the JDK's vendor, version and CPU.
- Part 6 found the JVM is not one answer: `Math.cos` and `StrictMath.cos` differ at 60 of 1,800 rows on one HotSpot
  **[C: part 6]**. So a JDK update can move the oracle, and this test is where that shows.
- **The `macos-26` job runs the same `jvmTest` as well,** on HotSpot for arm64. It is recorded and does not fail the
  build. It answers part 6's open question, whether HotSpot on arm64 builds the same table.

**What is compared.**

| Check | Compared how | Expected from |
|---|---|---|
| fused-multiply detectors (`Probe.kt:8-11`, double and float) | exact value | constants (0 unfused) |
| `toInt`, `min`/`max` with `-0.0` and NaN, `%`, the port's `Math.round` replacement at the three distance sites | exact value | constants |
| number parsing (the 27 profiles' tokens, `lookups.dat`'s values, stress decimals) | bits, by digest | the JVM, `Float.parseFloat`/`Double.parseDouble` |
| number printing (`Float.toString` where a cost reads it, `BExpressionContext.java:225`) | string, by digest | the JVM |
| the scale table: `lat`, `cos`, `kx`, `ky` for 1,800 rows | bits, by chunk digest | the JVM, `Math.cos` |
| `atan2` through `getAngle` and `normalize` | bits, by chunk digest | the JVM, `Math.atan2` |
| `hmap` iteration order (step 3) | sequence, by digest | depends on step 3's (i) to (iii), below |

**Checks on the check,** under CLAUDE.md's "a check that passes because it never saw the data that could fail it".
- **The counts.** Each loop counts what it computed: 1,800 rows, and the `atan2` total. The test fails on a short count.
- **It must hold the inputs that can fail it.** The table check covers all 1,800 rows, so it holds the 60 rows where
  `Math.cos` and `StrictMath.cos` differ, and the 8 where glibc 2.39 differs from the JVM (part 6). The `atan2` corpus has to
  hold the 50,215 pairs where glibc differs from the JVM: part 6's generator, reused.
- **A positive control, on every platform:** one row's `cos` moved by one ulp has to change its chunk's digest.

**What a mismatch would mean, under each of part 6's options.** The options are part 6's, for the table and for `atan2`.
None is chosen here.

| Mismatch on the simulator in | (a) constants written on the CI JVM | (b) each platform's `cos`/`atan2` | (c) fdlibm ported to Kotlin |
|---|---|---|---|
| the scale table | **a port bug**: the constants were built or loaded wrongly. Apple's C library is not on the path. | **expected:** Apple's C library. The test records which rows differ and does not fail. The route tests decide. | **a port or compiler bug.** The fdlibm port is IEEE operations only. |
| `atan2` | **(a) cannot cover `atan2`:** its inputs are any pair of integer differences. Under (a), `atan2` takes (b) or (c). | as for the table | as for the table |
| what the expected values are | the CI JVM's `Math.cos`, which the constants copy | the CI JVM's `Math`, which iOS is not expected to match | `StrictMath` on the CI JVM, which the port has to equal at every input |
| the same check in the `jvm` job | **the oracle's JDK moved.** Pin the JDK back, or rewrite the constants on purpose. | the oracle's JDK moved | the port's fdlibm against `StrictMath`: a port bug. Against `Math.cos` it differs at 60 rows **[C: part 6]**: the oracle's own table, not a failure |
| parsing, printing, fusion, `toInt`, rounding | **a compiler or runtime change**, under every option. These have no platform library under them. | | |

**What a pass would license [I].**
- **Under (a) and (c), a pass on the simulator stands for phones too.** No C library is on the path, and `iosArm64` uses the
  same backend as `iosSimulatorArm64`.
- **Under (b), a pass or a mismatch says only what the simulator's C library does.** Whether a phone's equals it was not
  determined **[?]**.
- **Under (c), the JVM port differs from the oracle at the table's 60 rows.** The oracle is the unmodified Java engine, which
  calls `Math.cos`. So E's differential tests on the JVM would meet that difference too, not only iOS.

**`hmap` order, by step 3's options.**
- **(i):** the expected order is `java.util.HashMap`'s, written on the CI JVM, and a mismatch is a port bug. The test's keys
  must not put nine or more nodes in one bucket with equal `ilon + ilat`. There the JVM's own order is not fixed (step 3), so
  the generator checks that it has not built such a bucket.
- **(ii):** iOS is expected to differ from the JVM. The check only records it, and E has to allow for it.
- **(iii):** the expected order is insertion order on every platform, and a mismatch is a bug.

**When it is added (G's rule, "once `commonTest` holds a test whose answer can differ on iOS"):** with the Phase 2 step that
ports `CheapRuler` and `CheapAngleMeter`. The step's place in the order is I's, in part 9.

#### Part 7: Android's `Math.cos` and `Math.atan2` (partial)

Written by the eighth session (Hazel, part 7, 2026-10-29). **Partial.** The planner called the handoff at 05:01Z, after two
files had been read. Sources, with URL, date and SHA-256, are in `09-float/part7/sources.txt`. They are AOSP `libcore` at
commit `de876a01b29230b877c9f408348c38a90ee724a5`, the head of `main` when read on 2026-10-29; its committer date is
2025-03-26.

**What was read [R].**
- **Android replaces the JDK's Java bodies with native methods.** `ojluni/src/main/java/java/lang/Math.java:219-228`
  comments out the JDK's `cos`, which delegates to `StrictMath.cos`, under "Android-changed: Reimplement in native". It
  declares `@CriticalNative public static native double cos(double a)` in its place. `atan2` is the same at `:712-721`.
  Source: <https://android.googlesource.com/platform/libcore/+/de876a01b29230b877c9f408348c38a90ee724a5/ojluni/src/main/java/java/lang/Math.java>,
  read 2026-10-29.
- **The native bodies call the C library.** `ojluni/src/main/native/Math.c:30-31` is `Math_cos(jdouble d) { return cos(d); }`
  and `:85-86` is `Math_atan2(jdouble d1, jdouble d2) { return atan2(d1, d2); }`. Both are registered at `:149` and `:151`,
  and the file includes `<math.h>` (`:27`). On Android, that is the platform's C library, bionic **[I]**. Source:
  <https://android.googlesource.com/platform/libcore/+/de876a01b29230b877c9f408348c38a90ee724a5/ojluni/src/main/native/Math.c>,
  read 2026-10-29.

**Not read, so not determined [?].**
- whether ART's compilers (JIT and ahead-of-time) replace these calls with intrinsics that call something else (`art/`,
  the intrinsics list and the arm64 code generator);
- which source bionic builds `cos` and `atan2` from on arm64, and with which flags, including whether fused multiply-add is
  allowed (`bionic/libm/Android.bp`);
- whether that differs across Forager's range of Android versions (`minSdk = 26`, `app/build.gradle.kts:265`);
- any computed comparison with HotSpot.

**Whether the results can differ from HotSpot's: not determined. Inferred that they can [I].**
- On the native-method path, Android's answer is bionic's `cos` and `atan2`.
- HotSpot's `Math.cos` on x86_64 is neither glibc's nor `StrictMath`'s: it differs from glibc at 8 of 1,800 rows and from
  `StrictMath` at 60 **[C: part 6]**. Two glibc builds already differ from each other.
- Bionic is a third C library, so equal bits would be a coincidence, not something the code ensures.

**What it would mean, if they differ [I]. To be confirmed by the reads above.**
- **Today:** Forager's Android app builds CheapRuler's table and the `atan2` tie-break with the phone's C library, not
  with HotSpot's. A HotSpot oracle in CI does not stand for what phones run now.
- **At the switch of Forager's app to the port:**
  - **(a) HotSpot constants:** the table on phones changes from bionic's to HotSpot's wherever they differ. Routes can
    change at the switch.
  - **(b) each platform's functions:** on Android the port calls the same functions the Java engine calls now, assuming
    `kotlin.math.cos` on Android is `Math.cos` (not read). Android's routes are unchanged at the switch, and iOS differs
    from Android.
  - **(c) fdlibm:** the same everywhere, and it differs from today's phones and from the HotSpot oracle wherever each of
    those differs from fdlibm.
- **For the differential tests:** an oracle on HotSpot proves the port matches HotSpot. It does not prove the port matches
  today's Android app. That would take the Java engine run on Android's runtime, on an emulator or a device, which nothing
  in G or the step 4 design provides.

### I. The Phase 2 order

Written by the tenth session on this dispatch (Hazel, part 9, 2026-10-29). **A plan, not built.** Bases: Forager `pre-main` is
still `d100a4776` (`git ls-remote`, 10:36Z), so no premise check is needed; Forager-iOS `main` is `490b0e3`, which adds only
`RECORD.md` entries and two files under `prompts/` since `87d9250` **[C]**. Every order and size choice below is **[I]** unless
marked.

**Why this order: packages bottom-up, and the oracle's own work before the router.**
- **The packages layer as util, codec, expressions, mapaccess, router [C: `11-plan/package-edges.txt`].** Over T2's reached
  call edges, every edge from a lower package to a higher one is a call through an interface or `Object` that the lower
  package declares: `LruMap` calling `equals`/`hashCode` (`util/LruMapNode.java:3` is abstract), `OsmNode.calcDistance` taking
  an `OsmPos` (`mapaccess/OsmPos.java:9`), `OsmLink.addLinkHolder` taking an `OsmLinkHolder` (`OsmLinkHolder.java:8`), and
  `TagValueCoder.decodeTree` taking a `TagValueValidator` (`codec/TagValueValidator.java:4`) **[R]**. So each step compiles
  against the steps below it, and its tests can fake what sits above.
- **A file-level layering by the same edges** (`11-plan/layers-calls.txt`) shows the same picture, with one 14-file cycle
  (3,675 lines) that consists of exactly those callbacks plus `router`'s path classes calling `expressions` and `mapaccess`.
  The cycle is broken by the package order above, not by splitting files **[C][I]**.
- **The oracle's own work comes early.** The tile capture, E3's self-agreement runs and E's expected values need only the
  1.7.10 jar (D) and the tiles. Run before the router is ported, E3's stop (more than 5% of a stratum unstable) is met before
  5,444 lines of `router` are written, not after.

**How every step runs (the dispatch's Phase 2 evidence and the coder profile, applied to each).**
1. **Tests first, over stubs:** each stub is a Kotlin signature whose body is `TODO()`. The tests are pushed and seen failing,
   each with `NotImplementedError` from the stub it targets (their reason). A test that passes over stubs is listed as "not
   shown to bite".
2. **The port of the step's files,** line by line from the reached methods (A's `reached-methods.tsv` gives each method's span).
   Pushed when `jvmTest` is green, with the counts read from the JUnit XML against the number of cases the step defines.
3. **One revert check per step,** on its own scratch branch: one named edit; the compile log read first; the file restored from
   a saved copy; the forward change confirmed afterwards. Each step below names its candidate edit.
4. **Push points:** after 1, after 2, and after 3 at least; the large steps push per sub-step. Every push runs G's `jvm` job.

**Sizes** are the Java lines A's tool keeps for T2 per file (`03-reach/out/T2/port-list.tsv`), which sum to 11,218 **[C]**. Kotlin
lines will differ; they are not predicted here. Test code is not sized.

| Step | What | Java lines kept (files) | Waits on | Tests (all differential against the oracle on the JVM unless named) | Candidate revert |
|---|---|---|---|---|---|
| **0** | **Scaffold.** Bases re-checked. The `brouter/` KMP module as C recommends (Gradle wrapper with its checksum, Kotlin 2.3.10, `jvm` 17, `iosArm64`, `iosSimulatorArm64`; okio 3.18.2; atomicfu 0.33.0 if B row 8's lock is kept); `brouter/LICENSE` and `README.md`; `tools/fetch-pinned.sh`; D's oracle fetch task; C's entry point (`route`, `RouteRequest`, `RouteOutcome`, `RouteCancel`, `checkDataFile`) as stubs; G's `jvm` job, the first file under `.github/workflows/` | 0 (build files) | the Gradle/Kotlin ruling below; Actions being enabled on the repository (not checked; turning it on is a settings change, so a stop) | the oracle jar's SHA-256; one case per tile-free failure kind on both engines (FK 6, data missing: a folder without the tile), failing on the stub | the oracle pin's SHA-256 changed by one character: the fetch must refuse |
| **1** | **Tile capture** (E4, F's ruling at RECORD -12). Download the four tiles from brouter.de once; load each in the oracle (a route and `checkFileIntegrity`), replacing a failing one by E4's rule; record size, SHA-256, URL, `Last-Modified` and `ETag` in a pin file; copy F0's fixture from Forager's test resources at its SHA-256 (read only; Forager unchanged); hand the files, hashes and ODbL 4.2 notices to the owner, who creates the release | 0 | **the planner's Phase 2 go naming the download, and the owner's word on the release** (a public action, RECORD -12). Steps 2 to 4 proceed meanwhile; steps 5 on wait | after the release: both jobs fetch each tile and check its pin | one tile's size in the pin file off by one: the fetch must refuse |
| **1b** | **The oracle's own runs** (E3, E4, E5): the case generators with their seeds; E3's six runs per case and the positive control; the instability count per stratum and E3's 5% stop; then the expected digests (2,118 cases, the JDK's vendor, version and `os.arch` at the head of the file). Under (c), also the no-intrinsic oracle's digests (E6). Each case's R1 time is logged for K | 0 (test code) | the release; (a) to (c) for the second oracle | the generators' counts against E5's figures; the control's orders differ between R1 and R4 | the control's 997-object perturbation removed: the R1/R4 control must fail |
| **2a** | **`util`, all but the two below:** `BitCoderContext`, `ByteDataReader`/`Writer`, `CompactLongMap`, `FrozenLongMap`, `SortedHeap`, `LruMap`, `LruMapNode`, `Crc32`, `ByteArrayUnifier`, `IByteArrayUnifier`; plus the small Java-semantics helpers B rows 13 and 15 call for: Java's `Math.round` written out (part 6: Native's `roundToLong` is `floor(x + 0.5)`), `Character.isWhitespace`, Java's `split`. `StackSampler` (30) is logging and is not ported (B row 5) | 1,332 (13), of which 30 not ported | nothing | seeded random streams through Java's and the port's coders, both ways; `SortedHeap` under a seeded mix of adds and pops with equal keys, **the pop sequence exact** (it decides ties in the search); the maps on seeded keys; the helpers at edge inputs (`0.49999999999999994`, `2^52 + 1`, NaN, no-break spaces) | `SortedHeap`'s tie order changed (one comparison `<` to `<=`) |
| **2b** | **`CheapRuler`, `CheapAngleMeter`,** with H step 4's platform-invariants test, and **G's `ios-simulator` job added here** (G's rule; H step 4) | 156 (2), plus per option below | **(a) to (c)** | H step 4's checks, chunk digests written on the pinned JVM; on the simulator, the same class | one table row's `cos` moved by an ulp: the chunk digest must change (H step 4's positive control, run as a revert) |
| **3** | **`codec`:** `MicroCache`, `StatCoderContext`, `TagValueCoder`, `NoisyDiffCoder`, `DataBuffers`, `TagValueValidator`, `WaypointMatcher`, `TagValueWrapper`; `MicroCache2`'s kept lines are its declaration | 550 (9) | nothing for synthetic streams; the release for tile streams | streams encoded by the oracle's own encoder classes, if the `-ro` jar carries them (**not checked [?]**); otherwise decoded from tiles in step 5 | a sign extension dropped (`and 0xff` removed at one byte read) |
| **4** | **`expressions`:** `BExpressionContext`, `BExpression`, `BExpressionContextWay`/`Node`, `BExpressionMetaData`, `BExpressionLookupValue`, `CacheNode`, `VarWrapper`. B row 14's `String.format` branch becomes a named "unsupported" failure, as B recommends | 1,562 (8) | nothing: the 26 profiles and `lookups.dat` come in the oracle's zip (D), and E's crossing profile is E's own | each of the 27 profiles parsed by both; then the way and node contexts evaluated on seeded random lookup vectors drawn from `lookups.dat`, **every output variable's bits** and `getKeyValueDescription`'s text compared; malformed profile text gives the oracle's message | one operator's evaluation swapped (`max` for `min` in `BExpression`) |
| **5a** | **`mapaccess`, the file layer:** `PhysicalFile`, `OsmFile`, `NodesCache`, `StorageConfigHelper`, on okio's `FileHandle` (B rows 1 and 2) | 752 (4) | the release | each captured tile's header and integrity check; FK 6, 7 and 8's messages on missing, short and byte-flipped copies; `checkDataFile` | the short-read check in the `readFully` loop removed (B row 1): FK 7 must fail |
| **5b** | **`mapaccess`, the graph:** `DirectWeaver`, `GeometryDecoder`, `OsmNode`, `OsmLink`, `OsmTransferNode`, `OsmLinkHolder`, `OsmPos`, `TurnRestriction`, `OsmNodePairSet`, `OsmNodesMap` (`hmap`) | 1,122 (10), plus (i)'s map if ruled | the release; **(i) to (iii)** | every micro-cache of each captured tile decoded by both, a digest per cache of nodes, links, elevations, way descriptions and turn restrictions; `collectOutreachers` on decoded regions, by branch below | one link direction flag inverted in `DirectWeaver` |
| **5c** | **Waypoint matching:** `WaypointMatcherImpl`, `MatchedWaypoint` (the `atan2` tie-break) | 300 (2) | the release; (a) to (c) for `atan2` | the matched waypoint (way, node pair, radius, direction) for every generated endpoint of R and NT; NT's comparison here is the first evidence on the tie-break, before any route | the comparator's second line (`WaypointMatcherImpl.java:71`) dropped |
| **6** | **`router`, the path model:** `RoutingContext`, `OsmPath`, `StdPath`, `OsmPathElement`, `MessageData`, `StdModel`, `OsmPathModel`, `OsmPrePath`, `OsmNodeNamed`, `ProfileCache` (B row 8's lock), `RoutingParamCollector`, `RoutingIslandException`, `SearchBoundary`, `AreaInfo`, `OsmNogoPolygon` | 2,068 (15) | B row 8 (a) or (b) | `RoutingParamCollector` on Forager's strings and generated ones; `RoutingContext` read from each profile; each path extension's cost on decoded links compared to the oracle's, from a test class in the oracle's package (package-private access on the classpath, **[I]**, to be confirmed in the step) | one `(int)` cast moved inside a sum in `StdPath` |
| **7** | **`router`, the track and its hints:** `OsmTrack`, `VoiceHint`, `VoiceHintList`, `VoiceHintProcessor` (turn instructions, ported under T2 and recorded, not asserted, by E1); `Formatter` and `Format*` only as far as A found methods reached | 1,367 (9) | nothing beyond 6 | exercised through step 8's routes; the hints recorded per E1 | (with step 8) one distance rounding replaced by `roundToLong` |
| **8** | **`RoutingEngine` and the entry point,** with E's four port requirements: the `internal` cost accessor (E1), the table and `atan2` seam and `hmap`'s map type as a parameter (E6, where the options need them), `memoryclass` as a field (E4), and a progress count a second thread can read (FK 11). Sub-steps, each pushed: **8a** matching and the search without memory pressure; **8b** the memory-pressure path (collection, limit, panic mode); **8c** terminate, the time limit and the progress count; **8d** `route()` and `checkDataFile` over the engine | 2,009 (1), less 429 if the via-point code is left out | **the via-point choice**; (i) to (iii) for 8b | F0 first (Forager's own two routes); then the strata 8a to 8c reach: R and DL in 8a, MP64, ML5 and PM5 in 8b, FK 9 to 11 in 8c | 8b: the `isInMemoryBounds` comparison flipped at `:2026`'s port; 8c: `terminated` written but not read |
| **9** | **The full differential run on the JVM:** E's 2,118 cases, asserted or recorded per E6 under the options ruled; JUnit XML counts against 2,118 less E3's unstable cases | 0 | everything above | E as designed | none new: each step's revert stands |
| **10** | **The simulator run:** first, one test that reads a tile from the runner's disk (part 5's open question). Then the stored digests, the full list or E5's fallback of 312 plus every targeted case, by K | 0 | step 9 | E6's iOS rows | none new |
| **11** | **Timing,** by K (part 10's item) | 0 | step 10 | K's plan | none |
| **12** | **The Phase 2 report:** results against E's figures; J scored; K's figures with their flags | 0 | | | |

**Totals [C]:** 11,218 Java lines kept, of which 30 (`StackSampler`) are not ported, so **11,188 to port** before the options
below; **10,759** if the via-point code is left out. **About 25 to 30 pushes** at three per step and per sub-step.

**Branches on rulings still to come.** Each is written so that the step can start on the day the ruling arrives.

- **(a) to (c), the scale table and `atan2` (step 2b; also 1b and 5c).** Lines from `11-plan/fdlibm-spans.txt` **[C: OpenJDK
  `jdk21u` at `jdk-21.0.12.1-ga`, `FdLibm.java`]**.
  - **(a) constants:** a generator task writes the 1,800 rows' `kx` and `ky` from the pinned JDK's `Math.cos` into a generated
    source; the invariants test's JVM half regenerates and fails if the JDK moved. Stored as `Long` bit patterns in a string
    decoded at class load, or split across functions, because a 3,600-value literal in one function sits at the JVM's 64 KB
    method limit (H step 4) **[I]**. `atan2` then takes (b) or (c):
    - with (c) for `atan2`: fdlibm `Atan` and `Atan2` and the bit helpers, **314 lines (171 code)**, tested exactly against
      `StrictMath.atan2` on part 6's 299,974 pairs and the edge values (signed zeros, NaN, infinities, subnormals). No seam;
    - with (b) for `atan2`: no fdlibm in `commonMain`, but the iOS logic run for NT needs that same fdlibm `atan2` in test code,
      behind the seam.
  - **(b) each platform's functions:** `kotlin.math.cos` and `atan2`. The seam is needed, and for the iOS logic run the test code
    carries the JVM's table (the same 64 KB point) and fdlibm `atan2` (314 lines, test-only). LB on iOS is generated only after
    the invariants test's first simulator run names Apple's rows (E6).
  - **(c) fdlibm:** `Cos`, `__kernel_cos`, `__kernel_sin`, `RemPio2` and the bit helpers, plus `Atan` and `Atan2`: **726 lines
    (379 code)**. `KernelRemPio2` (230 lines) serves only arguments above 2^19·π/2 (`RemPio2`, `FdLibm.java:628`) and a latitude
    never reaches it, so it becomes a named "unsupported" failure rather than code ported untested (`CLAUDE.md`, unsupported
    returns). Tested exactly against `StrictMath` at the 1,800 table inputs and the 299,974 pairs. Step 1b adds the second oracle
    JVM with `-XX:-UseLibmIntrinsic` (E2, E6), and step 2b the seam for the JVM logic run with HotSpot's table.
- **(i) to (iii), `hmap` (step 5b, then 8b).**
  - **(i)** a common-code copy of `java.util.HashMap`'s order for this one map: table size, hash spread, resize split and tree
    buckets with `TreeNode`'s balancing (part 7). Not sized: the parts of OpenJDK's `HashMap.java` it needs were not counted
    **[?]**. Tested by iteration order equal to `java.util.HashMap`'s on seeded `OsmNode` keys, with the generator refusing
    equal-hash tree buckets (H step 4). Then `collectOutreachers`' count exact.
  - **(ii)** `HashMap`: nothing to write. On the JVM it is the oracle's map; on iOS the counts are recorded (E6).
  - **(iii)** an insertion-ordered map everywhere, with the map type as a constructor parameter so the JVM logic run injects
    `java.util.HashMap` (E6).
- **`correctMisplacedViaPoints` (step 8; E's sample).** Its body is four methods, **429 lines**, reached only through
  `snapPathConnection`'s call at `RoutingEngine.java:1027`, which returns at once for waypoints named `from` and `to`
  (`:1265-1266`) **[R][C: `11-plan/via-point-spans.txt`]**.
  - **Port it untested:** the 429 lines go in, listed in the report as ported code no test reaches. A unit test that hands
    `snapPathConnection` made-up tracks is not offered: `CLAUDE.md` asks for behaviour to be exercised through its real entry
    point.
  - **Leave it out:** the call and the name guard are ported; past the guard, a waypoint named `via…` or `rt…` fails as
    "unsupported". Nothing through the entry point can reach it, so no route changes. 429 lines fewer. This narrows RECORD -8's
    "Any profile" for this one feature, which is why it is the owner's.
  - **Widen the entry point:** `RouteRequest` takes named waypoints. E gains a stratum, 59 cases at E5's p = 5% (total 2,177),
    with a profile of E's own that switches the feature on, since none of the 26 does (E4). Forager's adapter would not use
    it. The 429 lines go in, tested.

**Stops inside Phase 2 that this order meets early, by design.**
- E3's 5% instability stop: step 1b, before the router.
- A tile that 1.7.10 cannot read (part 4's open question): step 1, at capture.
- A simulator test that cannot read the runner's disk (part 5): step 10's first test. If it cannot, that is a stop: how tiles
  reach the simulator becomes a new decision.
- G's first `ios-simulator` run (step 2b) is the first test of Kotlin 2.3.10 with Xcode 26.0.1 on `macos-26` (G). A failure there
  that a pin cannot fix is a stop, not a reason to change the Kotlin pin alone (C: "moved only together with Forager").

**Rulings and inputs the order waits on, by step:** the Gradle and Kotlin pair (G's finding, step 0); Actions on the repository
(step 0, not checked); the download and the release (step 1); (a) to (c) (steps 1b, 2b, 5c); B row 8's lock (step 6); (i) to (iii)
(steps 5b, 8b); the via-point choice (step 8); K's flag margin (step 11). The researcher's Android answer bears on (a) to (c)'s
ruling, not on the order.

### Not answered yet (parts 6 to 9, in RECORD -11's order)

- ~~H's step 3, hash order~~: written by part 7, above;
- ~~H's step 4, the simulator test~~: designed by part 7, above (not built or run);
- H's Android question: in part (part 7, above). ART's intrinsics and bionic's `cos` and `atan2` were not read;
- ~~E, the differential tests~~: designed by part 8, above (not built or run);
- ~~I, the Phase 2 order~~: written by part 9, above;
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
- **Part 2** added item B and `04-libraries/` (one commit on `brouter-port`; its hash is in the final message).
  - **Reproduce B's probe:** copy `04-libraries/probe-kmp/` to scratch and run `gradle jvmTest
    compileKotlinIosArm64 compileKotlinIosSimulatorArm64` with Gradle 9.7.0. The test reads Forager's tile at the
    path hard-coded in `build.gradle.kts`.
  - **The container already holds** a Gradle 9.7.0 distribution (`~/.gradle/wrapper/dists/gradle-9.7.0-bin`) and
    the Kotlin/Native 2.3.10 toolchain (`~/.konan/kotlin-native-prebuilt-linux-x86_64-2.3.10`). Part 2 did not
    download them and does not know who did **[?]**. The system `gradle` is 8.14.3.
  - The probe is also left in scratch, at `hazel/probe-kmp/`.
- **Found by part 2 that bears on C and G** (input, not answers):
  - **iOS klibs compile on this Ubuntu x86_64 container** with Kotlin 2.3.10, for `iosArm64` and
    `iosSimulatorArm64`, with no Mac **[C: `04-libraries/probe-kmp/build.log`, `klib-manifests.txt`]**. That is
    the first half of G's "the iOS klib compile on `ubuntu`". Linking and running iOS tests were not tried.
  - **Gradle 9.7.0 with Kotlin 2.3.10,** Forager's pair, builds a KMP module with these targets **[C]**.
  - **Newest Kotlin:** 2.4.21 stable, 2.5.0-Beta2 newest overall; the newest 2.3.x is 2.3.21
    **[C: `04-libraries/metadata/org_jetbrains_kotlin_kotlin-stdlib.xml`]**.
  - **okio 3.18.2 is built against Kotlin 2.1.21,** older than Forager's 2.3.10 **[C: its `.module`]**. Forager
    already has okio on its classpath through OkHttp 4.12.0 (`gradle/libs.versions.toml:21`, `:91`) **[R]**; which
    okio version that brings was not read **[?]**.
- **Next, in the dispatch's order:**
  - C, including Amendment 1's three Android questions;
  - D (the release jar and `fetch-brouter.sh:12`'s SHA-256 are already confirmed);
  - E, built on the 47 methods the sample did not reach;
  - F, `HEAD` requests only;
  - G, H, I, J, K.

- **Part 3** added C and D, with `05-layout/` and `06-oracle/` (commits `28c9d65` for C; D's hash is in the final
  message).
  - **Reproduce C:** run `05-layout/kotlin-probe/run.sh` and `run-android.sh` from a copy in scratch. They need:
    - Gradle 9.7.0 at the wrapper path hard-coded there;
    - JDK 21;
    - for the Android half, an Android SDK with platform 35 and build-tools 35.0.0, which this container has at
      `/opt/android-sdk`.
  - **Reproduce D:** fetch the zip as `fetch-brouter.sh` does, then run `06-oracle/tools/compare-classes.sh <-ro jar
    unpacked> <vendored source compiled with javac --release 11>`. Run part 1's `ForagerCalls.java` on each, as
    `06-oracle/README.txt` says.
  - **Scratch** for part 3 is `hazel/part3/`: the Kotlin probe and its file repo, the oracle zip and the unpacked jars,
    and a blob-less bare fetch of Forager `pre-main`. Left in place, like parts 1 and 2's, until Phase 1 ends.
- **Found by part 3 that bears on later items** (input, not answers):
  - **For F:** the release zip's own SHA-256 pin and size are confirmed live at GitHub. Whether a tile source is
    stable is still unchecked.
  - **For G:**
    - an Android build with AGP 9.3.1 runs in this container;
    - whether Kotlin/Native 2.3.10 supports the Xcode on the macOS runner chosen was not checked. It decides whether
      C's "2.3.10 exactly" holds for iOS.
  - **For E:**
    - the oracle zip carries 26 pinned profiles, for "Any profile";
    - D's harness lines already compare node count, distance and a node hash between two engines.
  - **For H:** the oracle and Forager's source build are the same instructions on the JVM, so any JVM-side
    difference the port shows is the port's.
- **Next:** part 4, F (`HEAD` requests only; a stop for the owner's ruling) and G.

- **Part 4** added F, with `07-tiles/` (commit `858187a`, then this handoff commit). **G was not started**, at the
  planner's handoff limit. The planner decides which part takes G.
  - **Reproduce F:**
    - list `https://brouter.de/brouter/segments4/`, `diff/` and `diff/W75_N40/`, each parsed with `07-tiles/tools/parse-index.py`;
    - run `07-tiles/tools/head-tiles.sh <out> <tile>...`, which sends `HEAD` only.

    The listings change daily, so a rerun will give other dates and MD5s, but the same pattern.
  - **Scratch** for part 4 is `hazel/part4/`: the raw listings (SHA-256s in `07-tiles/sources.txt`), and the five
    GitHub documentation pages as markdown, under `part4/docs/`. **`part4/docs/runners.md` (GitHub-hosted runners) was
    fetched but not read.** G should fetch it again rather than rely on it. Left in place until Phase 1 ends.
  - **Done towards G, for whoever takes it:** Forager-iOS is public, by GitHub's API read with no credentials at
    2026-10-29T04:00:37Z (`"visibility": "public"`) **[C: `07-tiles/logs/forager-ios-visibility.txt`]**. Nothing else
    of G was checked:
    - the macOS runner label, and whether it is Apple silicon;
    - the Xcode installed on it;
    - whether Kotlin/Native 2.3.10 supports that Xcode, on which C's "2.3.10 exactly" depends;
    - when each job is worth adding.
  - **Found by part 4 that bears on E (part 5)** (input, not answers):
    - **Tile sizes, if E uses full brouter.de squares:** median 1,226,172 bytes; `W75_N40` 119,750,873 bytes; the
      largest 252,948,700. A sparse square like Forager's fixture is 152,665 bytes (`07-tiles/logs/segments4-index-0354Z.tsv`).
    - **Whether BRouter 1.7.10 reads today's brouter.de tiles is unknown.** The first step of any capture is to load each
      tile in the oracle.
    - **The pin cannot be to brouter.de,** so E's tiles depend on F's ruling for where they live. Their SHA-256s are
      written when they are captured, which is Phase 2.
    - **One square's tile is today's file only;** no earlier version can be fetched (F).
  - **Nothing found by part 4 bears on H.**

- **Part 5** added G and part of H, with `08-ci/` and `09-float/` (commits `8adb3da` for H's evidence; G and this text in the
  final message's commit).
  - **Reproduce H's scan:** `python3 -I 09-float/tools/fp_sites.py <javap dump> 03-reach/out/T2/reached-methods.tsv <out>`. The
    dump is part 1's `hazel/build/javap.txt` in scratch (rebuild: `javap -v -c -p` over the vendored classes compiled with
    `-g`).
  - **Reproduce G's reads:** the URLs in `08-ci/sources.txt` (GitHub's pages through `docs.github.com/api/article/body`), and
    `javap -c -p -constants` on `XcodeVersion` in `kotlin-native-utils-2.3.10.jar` and `XcodeVersionService` in
    `kotlin-gradle-plugin-2.3.10-gradle813.jar`, both in `~/.gradle`.
  - **Scratch** for part 5 is `hazel/part5/`: the six fetched pages and the extracted classes. Left in place until Phase 1 ends.
  - **H is not finished. What remains, in order:**
    1. classify the floating-point sites in `09-float/out/T2/sites.tsv` by whether they can choose between routes (cost
       comparisons, the open set's ordering, waypoint matching, pruning bounds) or only shape output (`track.distance`) or
       messages; start from the library functions listed under H;
    2. for each, whether the JVM and Kotlin/Native can differ: IEEE basic operations against library functions, parsing and
       printing, float-to-int conversion, and whether Kotlin/Native fuses multiply-add on arm64. Run what can be run here: this
       container can build and run Kotlin/Native `linuxX64` tests, which exercise the same native stdlib code (parsing,
       printing, `HashMap` order, hash codes) but not Apple's `libm`;
    3. Kotlin/Native's `HashMap` iteration order, needed for `collectOutreachers`;
    4. the simulator test: JVM-written expected bits for the latitude table and the other library-function sites, compared in
       `commonTest` on `iosSimulatorArm64` in G's `ios-simulator` job.
  - **E depends on H** (RECORD -11), so the planner decides whether H's remainder goes before E or with it.
  - **Found by part 5 that bears on later items** (input, not answers):
    - **For C, and the owner:** JetBrains' table puts Gradle 9.7.0 and AGP 9.3.1 outside Kotlin 2.3.10's range (G).
    - **For E:** `collectOutreachers`'s order question is one more reason the sample must reach the memory-pressure path; and
      the profile-parameter conflicts at `setParams` matter only if the port's entry point takes a general params map.
    - **For K:** the macOS standard runner is 3 M1 cores and 7 GB; the Ubuntu runner's figures were not read.

- **Part 6** added H's steps 1 and 2: `09-float/tools/classify.py`, `out/T2/classification.tsv`, `09-float/probe/` and
  `09-float/part6/` (commits `5b8936f` for step 1, `906a878` for step 2, then this handoff). **H is not finished; E is not
  started.** The planner decides whether H's steps 3 and 4 go to part 7 before E, or with it.
  - **Reproduce step 1:** `python3 -I 09-float/tools/classify.py 09-float/out/T2/sites.tsv <out>`. The reasons in its table
    were read by hand and are the evidence; the script only checks coverage.
  - **Reproduce step 2:** `09-float/part6/README.txt`. It needs:
    - Gradle 9.7.0 and Kotlin/Native 2.3.10, already in `~/.gradle` and `~/.konan`;
    - for `linuxArm64`, JetBrains' aarch64 toolchain and `qemu-aarch64` 5.1.0, which part 6 let Kotlin/Native download into
      `~/.konan/dependencies`.
  - **Scratch** for part 6 is `hazel/part6/`:
    - the probe and its build;
    - the full outputs (their SHA-256s are in `part6/io-sha256.txt`);
    - the unpacked compiler classes, the stdlib sources and the disassembled runtime.

    Left in place until Phase 1 ends.
  - **H step 3, hash order: evidence gathered, not written up** (input, not the answer):
    - **Kotlin/Native's `HashMap` iterates in insertion order [R][C].**
      - It appends at `length++` (`nativeWasmMain/kotlin/collections/HashMap.kt:361`), marks removals with a tombstone
        (`:395`), iterates by index (`:576`) and compacts in order (`:270-285`).
      - The probe's 3,000 `OsmNode`-shaped keys came back as 0, 1, 2, … on both native targets, and in bucket order on the JVM
        (`part6/results/*/hash.txt`).
      - The code is Kotlin with no platform calls, so `linuxX64` stands for iOS here **[I]**.
      - `String.hashCode` was equal on all three for the 6 strings tried.
    - **`collectOutreachers` [C, on a reduction].** The probe ports its loop (`Probe.kt`, `OutreachSim`), with `vanish` reduced
      to unlinking both ends, and runs it on 20 random 400-node graphs:
      - `nodesCreated` differs between the JVM and native in **14 of 20**;
      - the set of nodes left with no links is equal in 20 of 20.

      So which nodes vanish does not depend on order; the count does. My reading of why **[I]**: each component's search
      starts at the last of its nodes in iteration order, and a node's `firstlink == null` test (`OsmNodesMap.java:204`)
      depends on whether a vanishing neighbour was popped first.

      `nodesCreated` feeds `isInMemoryBounds` (`OsmNodesMap.java:106-120`). After a collection, that decides three ways
      (`RoutingEngine.java:2026-2031`):
      - a "memory limit reached" failure;
      - memory panic mode;
      - carrying on.

      It also moves `currentmaxmem` for later collections. **So hash order can change the failure kind on the
      memory-pressure path [I].** Whether it can change a route was not traced.
    - **A design consequence for the port [I]:** "identical" is measured against the Java engine, whose order is
      `java.util.HashMap`'s. To match on iOS, this one map has to iterate in `java.util.HashMap`'s order in common code. A
      `LinkedHashMap` on every platform would make the platforms agree with each other, but not with the oracle. This is a
      Phase 2 choice for the planner.
    - **`_parseFile` with more than one `profile:` key [R], my reading:**
      - the injected keys are distinct, each becomes a constant assignment, and all run before the profile's own expressions
        (`BExpressionContext.java:860-865`, `BExpression.java:395-404`);
      - their indices all fall below `minWriteIdx` (`BExpressionContext.java:820-828`);
      - results are read by name through `buildInVariableIdx` (`:836-838`, `:439-446`);
      - the one index-order loop, `:512-524`, is in methods no tier reaches (part 5).

      So numbering cannot change a result **[I]**. Forager passes one key anyway.
    - **Still to do for step 3:** write the above into H, and say for each of the two sites whether the port must replicate
      the JVM's order.
  - **H step 4, the simulator test: not started.** Part 6's findings that bear on it (input):
    - **A platform-invariants test in `commonTest`** would compare, on `iosSimulatorArm64`, against expected files written on
      the CI JVM:
      - the fused-multiply detectors (an exact expected value; a mismatch means the compiler changed);
      - the 1,800-row scale table and a fixed `atan2` corpus, bit for bit;
      - the parse and print corpora;
      - an order-dependent `HashMap` output.

      `09-float/probe/` is a working draft of all of these except the expected-file comparison.
    - **What a table or `atan2` mismatch means depends on the planner's choice in step 2's options:**
      - under (a) or (c), a mismatch is a port bug;
      - under (b), it is Apple's C library, expected, and the routes have to be compared instead.
    - **Still open from part 5:** whether a simulator test process can read a file from the runner's disk, and whether the
      simulator's C library equals a phone's.
  - **For part 7 (E), from H so far** (input):
    - **The oracle's JVM must be pinned.** On one HotSpot, `Math.cos` differs from `StrictMath.cos` at 60 of the 1,800 table
      rows. Expected routes written on one JVM, CPU or OS may not hold on another **[C][I]**. Whether HotSpot on arm64 (the
      `macos-26` runner) builds the same table as on x86_64 was not checked **[?]**.
    - **Cases that can tell the platforms apart,** for E's sample:
      - a waypoint whose nearest point is a junction, where the two ways' `directionDiff` nearly tie (`atan2`);
      - routes in latitude bands whose table rows differ. Against glibc 2.39 these are rows 128, 253, 261, 431 and four more
        (`part6/compare-jvm-amd64-vs-native-x64.txt`); Apple's rows are unknown until the simulator runs;
      - the memory-pressure path with a small memory limit, where hash order changes `nodesCreated`;
      - a profile with `bikeMode` and `consider_crossing`, for the one printed float. Which of the 26 profiles set them was
        not checked.
    - **`track.distance` (D)** depends on the table too, so a distance comparison on iOS shares its fate.
    - **For the comparison itself:**
      - IEEE arithmetic, parsing, printing and `toInt` agreed on every value tried;
      - Kotlin/Native does not fuse.

      A port that keeps Java's types and evaluation order can only differ from the oracle through the table, `atan2`,
      `HashMap` order and `roundToLong`. The last has a fix (`floor(x + 0.5)`, step 2) **[C][I]**.

- **Part 7** wrote H's step 3 and designed step 4, and answered the Android question in part, with `09-float/part7/`. Its
  commits are `765b65c` (step 3), `2b0a289` (step 4), then this handoff. **E is not started.**
  - **Reproduce step 3's new run:** `09-float/part7/README.txt` (`TreeBinOrder.java` over the vendored `OsmNode`, JDK 21).
  - **Scratch** for part 7 is `hazel/part7/`. It holds the compiled vendored classes, OpenJDK's `HashMap.java` and the two
    Android files. Left in place until Phase 1 ends.
  - **The Android question, what remains** (for whichever part the planner gives it), read from Android's published
    source at a pinned commit:
    - ART's intrinsics for `Math.cos` and `Math.atan2`: the intrinsics list, the arm64 code generator, and the
      entrypoints those call;
    - bionic's `libm/Android.bp`: which source `cos` and `atan2` come from on arm64, and the build flags (`-ffp-contract`);
    - whether this changed across API 26 to 37;
    - then whether a computed comparison is worth it. Compiling bionic's `cos` for aarch64 with its own flags and running it
      under part 6's `qemu-aarch64` would give the table rows against HotSpot.
  - **For part 8 (E), from part 7** (input, not answers):
    - **The memory-pressure path is reachable on Forager's calls.** `collectOutreachers` runs once a search holds about
      42,100 nodes (`currentmaxmem` starts at 4,000,000 and each node counts 95; Forager keeps `memoryclass` 64). E's sample
      needs routes long enough to reach it. Its failure kinds are "memory limit reached" and memory panic mode
      (`RoutingEngine.java:2026-2031`).
    - **There, the oracle may not agree with itself.** In a `HashMap` bucket with nine or more nodes sharing `ilon + ilat`,
      the order follows identity hashes (step 3). Before E fixes expected results on that path, run the Java engine twice in
      one JVM on such a route and compare its "collected, nodes/paths before=… after=…" log line (`RoutingEngine.java:2025`).
      If the two runs differ, compare that path's outcome more coarsely.
    - **Equality there depends on step 3's (i) to (iii)** for `hmap`. Under (ii) or (iii), `nodesCreated` can differ from
      the oracle's by design.
    - **The step 4 design keeps route tests and platform invariants apart.** Under part 6's (b), the simulator's table and
      `atan2` are expected to differ, and E's route comparison on the simulator carries the pass or fail. E should say
      what "identical" means on iOS under each of (a) to (c).
    - **Route tests on the simulator read tiles from disk.** Whether a simulator test process can read the runner's disk
      is still not determined. The invariants test avoids the question, and E's tests cannot.
    - **If Android's `cos` differs from HotSpot's** (not yet determined), a HotSpot oracle does not stand for today's
      Android app. E may need a case run on Android's runtime, or a statement that it compares against HotSpot only.

- **Part 8** designed E, with `10-tests/` (commits `18c4a0f` for the evidence, `358cbc3` for E, then this handoff). **I, J and
  K are not answered.**
  - **Reproduce part 8:** `10-tests/README.txt`. It needs the release zip (Forager's `fetch-brouter.sh:11-13` pin), part 6's
    scratch outputs for the native tables, and JDK 21.
  - **Scratch** for part 8 is `hazel/part8/`: the zip's profiles re-extracted, the six tables, and drafts. Left in place until
    Phase 1 ends.
  - **For I, the Phase 2 order** (input, not answers). E asks the port for things the port list does not name:
    - **an `internal` cost accessor** for the tests, since C's sketched `RouteOutcome.Found` carries none;
    - **a test seam for the scale table and `atan2`,** and **`hmap`'s map type as a constructor parameter**, needed for the
      logic runs under (b), (c) and (iii) (E6). Under (a) with fdlibm `atan2` and under (i), neither is needed;
    - **`memoryclass` kept as a field,** for ML5 and PM5 (E4). If I hard-codes 64, those strata go, and the memory limit and
      panic mode are reached only if 64-class searches happen to reach them;
    - **a progress count** that a second thread can read, for FK 11;
    - **the capture, as one Phase 2 step before any differential test:** the four tiles and F0's fixture, each loaded in the
      oracle, hashed, and handed to the owner for the release; then the expected-value task (E3's six runs, 2,118 cases).
  - **For I, an open choice found by E (not decided):** `correctMisplacedViaPoints` is ported by RECORD -8 but its body cannot
    run through Forager's two-waypoint call (`RoutingEngine.java:1265-1266`). Port it untested, or give the entry point named
    waypoints. That is the planner's or the owner's.
  - **For J, what E fixes that J predicts against:** a port with Java's types and evaluation order differs from the oracle only
    through the scale table, `atan2`, `hmap` order and `roundToLong` (parts 6 and 7). E6's table says, for each of (a) to (c) and
    (i) to (iii), which differences are assertion failures and which are recorded. J can predict the recorded counts.
  - **For K, the run counts [computed from E's figures]:**
    - writing expected values: 6 oracle runs × 2,118 cases = 12,708, plus a second oracle JVM under (c);
    - each push, on the `jvm` job: the port and the live oracle on 2,118 cases, plus logic runs where E6 needs them;
    - each push, on the simulator: 2,118 cases, or E5's fallback of 312 random plus every targeted, FK and F0 case.
    - How long one route takes on either platform was not measured **[?]**.
  - **Still open from earlier parts, touching E:** whether 1.7.10 reads today's brouter.de tiles (part 4); whether a simulator
    test can read the runner's disk (part 5); the rest of the Android question (part 7, the planner's choice of who).

- **Part 9** wrote I, with `11-plan/` (commit `775f122`, then this handoff). **J and K are not answered, and the list of the
  owner's choices near the top is not written,** at the planner's handoff (10:43Z).
  - **Reproduce I's layering:** `python3 -I 11-plan/tools/layers.py <T2 port-list.tsv> <scratch build/reach-T2/edges.tsv>
    <scratch build/jdeps-class.txt> calls`. The two inputs in scratch are part 1's. `package-edges.txt` was written by an inline
    script over the same `edges.tsv`; its output is committed, the script is not.
  - **Scratch** for part 9 is `hazel/part9/`: the layering script, OpenJDK's `FdLibm.java` and `StrictMath.java` (SHA-256s in
    `11-plan/fdlibm-spans.txt`), and I's draft.
  - **For J, part 9's working, not an answer** (the next part writes J, and may discard all of this):
    - *The mechanism as part 9 saw it [I]:* every reached Java method becomes one Kotlin function with the same statements in
      the same order on the same primitive types, so each value is the same bits wherever the operation is integer arithmetic,
      IEEE basic arithmetic, a conversion or a parse (part 6: equal on the JVM and Native, no fusion). The search's choices are
      `int` costs in BRouter's own `SortedHeap`, whose tie order is fixed by insertion order, so equal values give an equal
      route. Only the scale table, `atan2`, `hmap` order and `Math.round` sit outside that, and I's step 2a writes `Math.round`
      out as Java does.
    - *Why a one-ulp table difference should almost never move a route [I, an estimate, not measured]:* it changes an `int` only
      where a cost or distance lies within about 1e-13 of a rounding boundary. At about 1e5 casts per route that is of the
      order of 1e-8 per route, so 0 expected in 2,118 cases. If (c)'s JVM run or (b)'s iOS run records any difference in R or LB,
      this estimate is wrong.
    - *Where recorded differences should appear if any do [I]:* under `atan2` by (b) on iOS, in NT only, at the matched waypoint
      (the first or last part of the node list), not mid-route; part 9's rough figure was 0 to 8 of 59, about 2. Under (ii) or
      (iii), in MP64, ML5 and PM5 only. A second collection's timing follows `nodesCreated`, so `SortedHeap`'s rebuild after it can
      break ties between equal-cost paths differently: differences there should be **equal-cost** node lists, rarely a different
      kind. Part 9's rough figures: 0 to 10 of 59 in MP64; 0 to 3 of 59 in each of ML5 and PM5.
    - *E3 [I]:* tree buckets need nine entries in one bucket, under 1e-7 per bucket at a load of 0.76 by a Poisson tail, and
      `ilon + ilat` keys spread over consecutive buckets. Part 9 expected 0 unstable cases in every stratum.
    - *Where Phase 2's first discrepancies will come from [I]:* transliteration slips (sign extension of bytes, `>>` against `>>>`,
      `int` and `long` mixing), not floating point outside the four sites. Scorable from the commits that fix them.
  - **For K, part 9's working, not an answer:**
    - *Machines [R: part 5's `runners.md`, read 04:11Z]:* `ubuntu-24.04` for public repositories is 4 CPUs, 16 GB, x64; `macos-26`
      is 3 (M1), 7 GB, arm64. The CPU model is not in the table, so the run should print it (`/proc/cpuinfo`; on macOS
      `sysctl machdep.cpu.brand_string`).
    - *Ideas, not chosen:* time each case around the call only, in one JVM, engines alternating which goes first per case, a
      `System.gc()` outside the timed region between cases; per case the median of the repetitions, then the per-case ratio
      port/Java, summarised by its median with a bootstrap interval and the 10th and 90th percentiles; cases under 1 ms and
      cases whose outcomes differ kept out of the ratio and counted. Repetitions fixed from step 1b's logged R1 times before any
      port time is read. An A/A control (the oracle timed against itself) to measure the noise before any margin is applied.
      Memory on the JVM as allocated bytes per route (`ThreadMXBean.getThreadAllocatedBytes`, since `doRun` runs on the
      caller's thread, premise 2). On `macos-26`, the port on HotSpot arm64 and on the simulator in the same job, so the
      simulator is compared with the same machine. **Not checked [?]:** whether Kotlin/Native test binaries are built
      unoptimised by default (timing a debug binary would mislead) and whether KGP 2.3.10 can run an optimised one on the
      simulator.
    - *A flag that ties speed to something the user sees [I]:* any route the JVM finds within Forager's 30,000 ms
      (`BRouterTrailRouter.kt:199`) that the simulator takes longer than 30,000 ms to find. The margin itself is the owner's.
    - *Not measured anywhere in the plan:* ART on Android phones (Amendment 1 makes the port Android's too), and phones.
  - **For the owner-choices list (not written):** I's "Rulings and inputs the order waits on" names every ruling the order
    needs. With C's and B's recommendations, D's, G's label and finding, E's calls beyond, and K's margin, that is the list.

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
- **Part 2 (item B):**
  - confirmed by computation: the Maven metadata, okio's published targets, and the probe's JVM bytes and
    three-target compile;
  - inferred: every Kotlin/Native behaviour, and every semantic difference named in B's table.
- **Inferred:**
  - that each tier's cuts are complete for Forager in general, not only on the sample;
  - that parse-time code is deterministic for the pinned files;
  - the recommendation to port T2 with T3 as named failures;
  - the semantics notes above.

- **Part 3 (C and D):**
  - **Confirmed by computation:**
    - the moved base and that no premise path changed;
    - the Kotlin metadata rule, one version ahead and not two, in both directions;
    - the stdlib moving with an unpinned newer compiler;
    - the Android consumer at Forager's settings;
    - the oracle's size, SHA-256 and profile pins;
    - the jar's classes against the vendored source (with a negative control);
    - the jar and the source routing the harness identically.
  - **Inferred:**
    - every recommendation in C and D;
    - the class-name clash that decides the package rename;
    - what GitHub Packages and Maven Central need;
    - that the language pin alone keeps vendored source compilable under 2.3.10.

- **Part 4 (F):**
  - **Confirmed by computation:**
    - the listing's 1,142 tiles, all dated 29 October;
    - the `HEAD` headers, and the `ETag` decoding to modification time and length;
    - the `diff/` layout matching upstream's naming rule (585 = 585);
    - `W75_N40`'s 10 distinct earlier MD5s in 10 days;
    - v1.7.10 as upstream's newest tag;
    - Forager-iOS's visibility, public (the one piece of G done).
  - **Confirmed by reading:** ruling c's wording, GitHub's published limits, and ODbL 4.2.
  - **Inferred:**
    - that brouter.de runs upstream's diff code;
    - that distinct MD5 names mean distinct tiles;
    - that no earlier tile can be rebuilt;
    - that LFS counts as "committed" under ruling c;
    - that a public release asset downloads without a token;
    - the recommendation itself.

- **Part 5 (G, part of H):**
  - **Confirmed by computation:** the bases; the scan's counts against the dump; three of B's sites unreached at every tier; KGP
    2.3.10's `maxTested` of 16.0 and the comparison that uses it; the Kotlin/Native distribution's Xcode 26.0.1 toolchain.
  - **Confirmed by reading:** GitHub's runner table, billing lines and image Xcode lists; JetBrains' compatibility table; each
    hash-iteration site's code.
  - **Inferred:** that every Xcode 26 prints KGP's warning; that iOS linking needs a Mac; that a public asset fetches without a
    token; the effect of order at `collectOutreachers`; every recommendation in G.

- **Part 6 (H, steps 1 and 2):**
  - **Confirmed by computation:**
    - the classification's coverage of `sites.tsv` (72 of 72);
    - the probe's results on the JVM, `linuxX64` and `linuxArm64` (qemu), and `linuxX64`'s libm values against the host C
      library;
    - no fused multiply-add in Kotlin code on arm64, by detectors and disassembly, with a positive control;
    - `roundToLong` differing between the JVM and Native;
    - `Math.cos` against `StrictMath.cos` on one JVM.
  - **Confirmed by reading:**
    - each site's class and reason;
    - Kotlin/Native's code-generation flags, the libm calls in its runtime, and its `roundToLong`, `round` and `HashMap`
      source.
  - **Inferred:**
    - that iOS does not fuse either (same backend and flags, not run);
    - that parsing, printing and `HashMap` order on iOS match the Linux runs;
    - how often a one-ulp table difference changes an int;
    - that a flip between one way's two directions leaves the route alone;
    - `floor(x + 0.5)` matching `Math.round` at BRouter's distance sites;
    - the options (a) to (c).

- **Part 7 (H steps 3 and 4, the Android question in part):**
  - **Confirmed by computation:**
    - the bases;
    - part 6's `collectOutreachers` counts recounted from the committed files (14 of 20, by one);
    - `java.util.HashMap` ordering BRouter's own `OsmNode` keys differently on each build when they share a hash in a tree
      bucket, with a control that did not move.
  - **Confirmed by reading:**
    - `collectOutreachers`, `isInMemoryBounds` and the memory-pressure branch;
    - `_parseFile`'s injected keys;
    - Kotlin/Native's `HashMap` lines;
    - OpenJDK 21.0.12.1's `HashMap` tree-bucket code;
    - Android `libcore`'s `Math.cos` and `Math.atan2`, which are native methods calling C's `cos` and `atan2`.
  - **Inferred:**
    - that order changes only the count, not the vanished set;
    - that a one-node difference can move the failure kind and later collections;
    - that real tiles rarely fill a bucket that far;
    - that `<math.h>` on Android is bionic;
    - that Android's results can differ from HotSpot's;
    - what each of (a) to (c) means at the switch;
    - every choice in the step 4 design.

- **Part 8 (E):**
  - **Confirmed by computation:**
    - the bases, and part 3's scratch zip against its pin;
    - the scale table copied from `CheapRuler` equals part 6's JVM table at 1,800 of 1,800 rows;
    - `Math.cos` against `StrictMath.cos` on this HotSpot: 60 rows differ in `cos`, 54 in `kx` or `ky`, 8 of those in the N40 tile
      row; glibc 2.39 (part 6's outputs): 8 in `cos`, 6 in `kx` or `ky`;
    - with `-XX:-UseLibmIntrinsic`, `Math.cos`'s table equals `StrictMath`'s; with `-Xint`, it equals the default;
    - the 26 profiles' switches, with the turn-instruction and bike counts matching `grep` (24, 16);
    - `estimated_crossing_class` in the pinned `lookups.dat`;
    - every sample-size figure, each checked by its defining inequality;
    - the tiles' sizes, read from part 4's listing.
  - **Confirmed by reading:** the failure families' throw sites; the `debug.txt` switch and what its log lines build;
    `terminated`'s one write and `getLinksProcessed`; `snapPathConnection`'s guard; `memoryclass`'s three uses; the profile
    cache's clock reads; the tile-name rule; the crossing guard; Forager's call lines.
  - **Inferred:**
    - every **[FBD]** choice in E;
    - the tiles' geography (New Jersey, Maine, Cape Cod, Dunedin), not read from any tile;
    - that `kotlin.math` on the JVM calls `java.lang.Math`;
    - that the info log changes nothing but strings (read at the lines named; E3 checks it by running);
    - that `memoryclass` 5 decides the limit or panic mode at the first collection;
    - that digests in the repository add no OpenStreetMap data.

- **Part 9 (I):**
  - **Confirmed by computation:** the bases; T2's per-file lines summing to 11,218 by package; the package edges and that every
    upward one is a callback; the via-point methods' 429 lines; fdlibm's line spans.
  - **Confirmed by reading:** the four interfaces and the abstract class behind the upward edges; the via-point call sites; the
    memory branch at `RoutingEngine.java:2026`; `StrictMath.cos` and `atan2` calling `FdLibm`; `RemPio2`'s argument ranges;
    the public-repository runner sizes (part 5's saved page).
  - **Inferred:** the order itself, every step's tests and candidate revert, the push count, and that a test class in the oracle's
    package can reach its package-private classes.

### Could not determine

- **Whether the sample reaches every path a real phone's data would.** It does not: 47 methods of T3 never ran.
  Which inputs reach them is item E's work.
- **How much of each kept method is dead inside it** (branch level). The counts are per method.
- **Whether `brouter.de` serves stable bytes.** Answered by part 4 (F): it does not.
- **Part 2, item B:**
  - whether any replacement behaves the same on iOS: nothing ran there;
  - whether `HashMap` iteration order changes a result at any of the nine sites in B row 18;
  - whether `kotlinx-io` has a positional read;
  - the okio version Forager already gets through OkHttp.

- **Part 3:**
  - `distributionSha256Sum` for Gradle 9.7.0 (not read);
  - whether a GitHub macOS runner label the project would use is still Intel;
  - whether GitHub Packages requires a token to read a public package (not checked);
  - iOS klib metadata across compiler versions (not tested);
  - whether Kotlin/Native 2.3.10 supports the runners' Xcode.

- **Part 4 (F):**
  - whether the Wayback Machine holds dated copies of a tile (both index queries were reset);
  - which map creator brouter.de runs, and so whether the 1.7.10 engine reads today's tiles;
  - what the zero-byte `0_timestamps_are_map_snapshot_time_in_CET_timezone` file implies for the listed times;
  - the owner's GitHub plan, and so the LFS quota that applies (Free and Pro are the same, 10 GiB);
  - option 6's limits, which were not read.

- **Part 5:**
  - whether Kotlin 2.3.10 links and runs simulator tests with Xcode 26.0.1 on `macos-26` (Phase 1 runs no CI);
  - which simulator runtimes the images hold;
  - whether a simulator test process can read a path on the runner's disk;
  - whether variable numbering can change a result at `_parseFile`, and whether order changes a route at `collectOutreachers`;
  - Kotlin/Native's `HashMap` iteration order (not read or tested);
  - H's floating-point questions, all of them (not reached).

- **Part 6:**
  - Apple's `cos`, `sin`, `exp` and `atan2` results: no Apple platform here. Linux results do not stand for them.
  - Whether the simulator's C library equals a phone's.
  - Whether a scale-table difference changes a route on real tiles, and whether the `atan2` tie-break between two different
    ways ever flips (no near-tie was constructed).
  - Whether HotSpot on arm64 builds the same table as HotSpot on x86_64.
  - Kotlin/Native's code generation for iOS, run rather than read.

- **Part 7:**
  - whether a moved collection, or memory panic mode reached in a different place, changes a route;
  - how often real tiles put nine or more nodes with one `ilon + ilat` into one `hmap` bucket;
  - whether Ubuntu's OpenJDK build patches `HashMap`;
  - ART's intrinsics for `Math.cos` and `Math.atan2`, bionic's `cos` and `atan2` and their flags, and any change across
    API 26 to 37 (not read: handoff);
  - whether Android's `Math.cos` and `Math.atan2` give HotSpot's bits;
  - whether a simulator test process can read the runner's disk (still open; step 4 avoids it).

- **Part 8:**
  - whether each stratum can be filled from the captured tiles: in particular ML5, PM5, NT within 4 ulps, and the island
    kinds (the reports will say);
  - whether any captured tile carries `estimated_crossing_class`;
  - what `-XX:-UseLibmIntrinsic` changes beyond the scale table;
  - whether a reparsed profile can route differently from a cached one (E3 is built to find out);
  - how long one route takes on the JVM or the simulator;
  - still open from earlier parts: whether 1.7.10 reads today's tiles; whether a simulator test can read the runner's disk.

- **Part 9:**
  - whether the `-ro` jar carries BRouter's encoder classes (step 3's synthetic streams);
  - how much of OpenJDK's `HashMap` option (i) needs;
  - whether GitHub Actions is enabled on Forager-iOS (step 0);
  - J and K: not reached.

### Premises that were wrong

- **Premise 3, in part.** Forager also reads `track.distance` (`BRouterTrailRouter.kt:115`), so `recalcTrack`'s
  distance is part of what the port must reproduce. It also depends on `getErrorMessage`, `getFoundTrack` and
  `quite`, and its test on `isTerminated` and an overridable `doRun`.
- **Premise 6's sorted maps, in part.** At source level they are used. On the method-level path only one sorted
  iteration remains, over an empty map for Forager's calls. Date formatting does not remain on the path at all.
- **The ~13,400 estimate (premise 5)** was marked unverified. Measured, it falls between T0 and T1.

- **Part 3:**
  - **Forager `pre-main` moved** (`d100a4776`). No premise path changed, so this is a moved base, not a wrong premise
    (`05-layout/00-bases-part3.txt`).
  - **Amendment 1's account of Forager's build** is confirmed again, unchanged at `d100a4776`.

- **Part 4:**
  - **Premise 9's open question is answered.** brouter.de does not keep a tile's bytes stable at one URL: the files
    listed as dated 2026-10-03 are dated 2026-10-29, and `W75_N40` had 10 earlier contents in 10 days. The citation
    itself stands.

- **Part 5:**
  - **B row 18, in part:** three of its nine "reached" hash-iteration sites are not reached at any tier (H).
  - **C's Gradle 9.7.0 with Kotlin 2.3.10** sits outside JetBrains' published range (G). C's own claim, that the pair builds,
    stands; "supported" does not.

- **Part 6:**
  - **B row 15, in part:** it names `Math.round(NaN)` as the one difference from `roundToLong`. On Native, `roundToLong` also
    differs for ordinary values (`0.49999999999999994`, `2^52+1`), because it is `floor(x + 0.5)` there. B row 15's open
    question (do `sin`, `cos`, `atan2`, `exp` give the same bits on Native?) is answered for Linux: they do not.
  - **The planner's prediction "float differences are the main risk on iOS"** holds in part. Basic arithmetic does not differ,
    and Kotlin/Native does not fuse. The risk is two library functions (`cos` for the table, `atan2` for the tie-break), plus
    two things that are not floating point: `HashMap` order and `roundToLong`.
  - **The part 6 prompt's "Include whether multiply-add can be fused on arm64":** it cannot be in Kotlin code on
    `linuxArm64`, but it is in Kotlin/Native's own C++ runtime (the number printer). Its output still matched the JVM on all
    220,001 values printed.

- **Part 7:**
  - **Part 6's "To match on iOS, this one map has to iterate in `java.util.HashMap`'s order"** cannot be met in full. Where a
    bucket holds nine or more nodes with equal hashes, the oracle's own order follows identity hashes.
  - **Part 6's step 3 list said `_parseFile` was settled by reading `:439-446` and `:512-524`.** Part 7 re-read only
    `:818-840`, `:855-868` and `BExpression.java:393-405`, which agree. The rest is part 6's reading, not repeated.

- **Part 8:**
  - **Parts 6 and 7's handoff named "a profile with `bikeMode` and `consider_crossing`" as a case from the 26.** None of the
    26 switches on `consider_crossing` **[C]**. E writes its own profile.
  - **Part 6's handoff listed "rows 128, 253, 261, 431 and four more" as bands whose table rows differ.** For routing only 6 of
    the 8 matter: rows 128 and 1671 differ in `cos` and in neither `kx` nor `ky` **[C]**.
  - **RECORD -8's option text, "Those four features are ported too, so other BRouter profiles would work",** does not hold for
    `correctMisplacedViaPoints` on Forager's call. A profile that switches it on reaches only the early return
    (`RoutingEngine.java:1265-1266`) **[R]**.
  - **The dispatch's "identical" includes the cost, which C's sketched surface does not return.** The tests read it internally.
  - **My own slips, caught before they reached the report:**
    - the profile parser missed `mtb.brf`'s indented context line; the count against `grep` caught it, and the script now fails
      on a profile with no global section;
    - a `grep` cut short by `head` read as `estimated_crossing_class` being absent from `lookups.dat`; a full count showed it at
      `:862-868`;
    - `TableRows.java`'s header cited the wrong `CheapRuler` lines; fixed, and rerun so the committed source made the logs.

- **Part 9:**
  - **Tier T2's file list shows the format classes with declarations kept** (`Formatter`, `FormatGpx` and the rest, 160 lines
    with at most 3 methods). Step 7 ports them only as far as A found methods reached; A's figures count the declarations.
  - **Part 9's own slip:** the first layering run parsed method names wrongly on descriptors holding `/` and failed; fixed before
    any output was used.

### Decided beyond scope

- **Part 2:**
  - **atomicfu was added as a candidate** (B row 8). The dispatch did not name it; the lock question needed one.
  - **The probe pins Gradle 9.7.0 and Kotlin 2.3.10,** matching Forager, for the probe only. That is not item C's
    answer.
  - **Part 2 stopped after one of its six items,** at the planner's token limit for this helper, rather than
    choosing which of C to H to skip.

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
- **Part 3:**
  - **The package prefix** `com.zynergylabs.forager.brouter` is a naming choice made here. C asked for keep-or-rename,
    and the prefix is offered for the planner to accept.
  - **The `-ro` jar** over the `-all` jar Forager's scripts use. Identical router classes, fewer classes on the
    classpath.
  - **The release zip was downloaded to scratch,** to check its pins and compare its classes. It is BRouter's own
    release, not a map tile, and nothing from it is committed.
  - **Kotlin 2.3.21, 2.4.21 and 2.2.21, AGP 9.3.1 and their dependencies** were downloaded to `~/.gradle` for the
    probes, from the Google mirror and Google's Maven.
  - **The Android consumer probe** was not asked for. The Android SDK was found in the container and made the
    Amendment's question testable in Forager's own build configuration.
  - **A blob-less bare fetch of Forager `pre-main`** went into scratch to read the moved base. The read-only clone was
    not touched.
- **Part 4:**
  - **The Wayback Machine's capture index was queried** for one tile URL, as a dated-archive listing. It is not a
    brouter.de listing or a `HEAD`, so it is named here. No capture was requested, and both queries failed.
  - **Three `diff/` listings and one `HEAD` on a zero-byte `.df5`** were read alongside the tile `HEAD`s. A `.df5` is a
    diff, not a tile, and the one sent a `HEAD` holds 0 bytes.
  - **Two documentation pages were fetched in full,** brouter.de's offline page and ODbL 1.0, together with five
    GitHub documentation pages through GitHub's article API.
  - **Option 6 names Cloudflare R2 as an example** of an external bucket. Its limits were not read.
  - **The listings were kept in scratch,** not committed, at 133 KB. Their parsed form and SHA-256s are committed.
  - **One piece of G was done:** whether Forager-iOS is public, because option 2 depends on it. Everything else of G was
    left, at the planner's handoff limit.
  - **Part 4 stopped after F** rather than starting G, by the planner's handoff limit. That limit was set for this part;
    it is not a stop in the dispatch.
- **Part 5:**
  - **G was answered before H,** against the order H, G in the prompt's "G and H": with about 55k tokens left after the reading,
    G could be finished and H could not. H's evidence was pushed first (`8adb3da`).
  - **The label `macos-26` over `macos-15`,** both carrying Xcode 26.0.1, and `ubuntu-24.04` pinned over `ubuntu-latest`.
  - **JetBrains' jars were read with `javap`** from `~/.gradle`, beyond the documentation page, because the page and the tooling
    disagree.
  - **Six documentation pages were fetched** to scratch: GitHub's runners and billing pages, three runner-image readmes, and
    kotlinlang.org's compatibility guide.
  - **The finding against C is reported, not acted on.** C's text is unchanged; G states the conflict.
- **Part 6:**
  - **`linuxArm64` under qemu** was run as well as `linuxX64`. The prompt named `linuxX64`; arm64 was the only way to run
    Kotlin/Native's arm64 code generation here. For it, Kotlin/Native downloaded JetBrains' aarch64 toolchain (90,283,015
    bytes) and `qemu-aarch64` 5.1.0 (2,401,518 bytes) from `download.jetbrains.com` into `~/.konan/dependencies`.
  - **Kotlin/Native's compiler jar, runtime bitcode and stdlib sources were read,** unpacked into scratch, to answer from code
    rather than from what the platform is said to do.
  - **Step 3's evidence was gathered before step 2 was pushed,** in the same probe runs, and is committed with step 2.
  - **The options (a) to (c) for the table and `atan2`** are offered, not chosen. The choice decides what the simulator test
    compares.
  - **The handoff limit was passed** during step 2 (about 290k when step 2 was pushed, against about 200k). This session read
    the count late. It finished step 2, pushed, and handed off without writing steps 3 and 4.
- **Part 7:**
  - **One run beyond part 6's evidence** (`TreeBinOrder.java`). Step 3's claim about the oracle's order needed it. It
    compiled the vendored BRouter tree into scratch.
  - **Fetched from outside:** OpenJDK `jdk21u`'s `HashMap.java` at two tags, and two Android `libcore` files plus their
    repository metadata. All are listed in `part7/sources.txt`.
  - **Step 3 offers three designs for `hmap`, (i) to (iii), and a check on the oracle alone.** Neither is chosen.
  - **Step 4 uses digests in source in place of expected files,** to avoid the open question of file access on the
    simulator. That is a design choice for the planner.
  - **The Android question was cut short at the planner's handoff,** after two files. It is marked partial, and the reads
    that remain are listed in the handoff.

- **Part 8:**
  - **The distance joins "identical"** beside the node list, cost and failure kind, because Forager reads it.
  - **Values Forager does not read are recorded, not asserted:** elevations, ascent, time, energy, voice hints.
  - **One scratch run beyond the dispatch's evidence:** the table under `-XX:-UseLibmIntrinsic` and `-Xint`, which E6 uses
    under (c).
  - **Strata outside Forager's calls:** `memoryclass` 5 (ML5, PM5); the time limit off (`maxRunningTime = 0`) in every case but
    FK 9; E's own crossing profile.
  - **F0's fixture as a release asset,** so Forager's own two routes are E's first cases.
  - **What E asks of the port** (for I): an internal cost accessor, the table and `atan2` seam, `hmap`'s map type as a
    parameter, `memoryclass` as a field, a progress count.
  - **Thresholds chosen here:** β = 0.05; p = 5% per profile and per stratum; the 5% instability stop; 20 times a stratum's size
    in candidates; the tile-replacement rule; the 312-case fallback on iOS; 997 objects in R4; 4 ulps for NT; 50 ms and 1 s for
    FK 9; 10,000 and 100,000 links for FK 11.
  - **JaCoCo's agent on the oracle jar** for reach, a new test dependency.
  - **Stored digests, not coordinates,** for the expected values.

- **Part 9:**
  - **The tile capture and the oracle's own runs are placed before the router** (I's steps 1 and 1b), so E3's stop is met early.
    The dispatch asked for packages bottom-up; this puts oracle-only steps among them.
  - **Leaving the via-point code out is written with a named "unsupported" past the name guard,** and **`KernelRemPio2` under (c)
    as "unsupported"** for arguments a latitude never reaches. Both are offered within their branches, not chosen.
  - **OpenJDK's `FdLibm.java` and `StrictMath.java`** were fetched from `openjdk/jdk21u` at `jdk-21.0.12.1-ga` to size (c), as part 7
    fetched `HashMap.java`. The local `src.zip` is a dangling link.
  - **A candidate revert per step,** and step 0's check that Actions is enabled, are named here; neither was asked for.
  - **Part 9 stopped after I at the planner's message;** J, K and the owner-choices list are left with notes in "Handoff".
