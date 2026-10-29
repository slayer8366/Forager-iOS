# BRouter port, Phase 1: verify and plan (partial: the premises, items A to D, F and G; H in part, steps 1 and 2 of 4)

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

### H. Floating point, and hash order (parts 5 and 6: answered in part)

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

### Not answered yet (parts 6 and 7, in RECORD -11's order)

- ~~H's step 3, hash order~~: written by part 7, above;
- H's step 4, the simulator test (not started);
- E, the differential tests;
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

