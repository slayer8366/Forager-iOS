# BRouter port, Phase 1: verify and plan (partial: the premises, items A and B)

Dispatch 2026-10-28-01 and its Amendment 1 (RECORD -3, -4), restarted at -5. Written by Hazel Cairnwright, the
second session on this dispatch, 2026-10-28.

**Status: partial.** This session stopped at the dispatch's long-conversation limit ("past about 200k tokens,
push and hand off", dispatch "Working here"), after the premises and item A. **Items B to K are not answered
here.** What this session found that bears on them is under "Found on the way". It is input for the next
session, not an answer to those items.

**Part 2** (a third session, Hazel, 2026-10-28) answered **item B** only and stopped at its own token limit
(about 150k, set by the planner for this helper). C, D, F, G and H, which were part 2's, are not answered; what
part 2 found that bears on C and G is under "Handoff to the next session".

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

### Not answered in part 2 (left for the next session)

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

### Could not determine

- **Whether the sample reaches every path a real phone's data would.** It does not: 47 methods of T3 never ran.
  Which inputs reach them is item E's work.
- **How much of each kept method is dead inside it** (branch level). The counts are per method.
- **Whether `brouter.de` serves stable bytes.** Not checked.
- **Part 2, item B:**
  - whether any replacement behaves the same on iOS: nothing ran there;
  - whether `HashMap` iteration order changes a result at any of the nine sites in B row 18;
  - whether `kotlinx-io` has a positional read;
  - the okio version Forager already gets through OkHttp.

### Premises that were wrong

- **Premise 3, in part.** Forager also reads `track.distance` (`BRouterTrailRouter.kt:115`), so `recalcTrack`'s
  distance is part of what the port must reproduce. It also depends on `getErrorMessage`, `getFoundTrack` and
  `quite`, and its test on `isTerminated` and an overridable `doRun`.
- **Premise 6's sorted maps, in part.** At source level they are used. On the method-level path only one sorted
  iteration remains, over an empty map for Forager's calls. Date formatting does not remain on the path at all.
- **The ~13,400 estimate (premise 5)** was marked unverified. Measured, it falls between T0 and T1.

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
