# Item I, step 6: the path model (session 10, piece 1 of several)

Coder: Plumb Ledger. Dispatch: RECORD -92; rulings during the session: -93 (the coordinate scan), -94 (`OsmTrack`).
Branch `brouter-port-5b`. Evidence: `docs/audits/2026-11-06-brouter-port-step6/`.

Marks: **[R]** read in a source named beside it; **[C]** computed or run here, with the output named; **[I]** inferred,
not checked; **[?]** open.

## 1. The merge from `brouter-port`

- `git merge --no-ff origin/brouter-port` (`fcd35ff`) into `b65548f` gave `eac836d` [C]. The merge touched no file under
  `brouter/src/commonMain` [C, `git status`].
- **Two conflicts, both resolved by keeping 5b's side, which contains `brouter-port`'s** [C]:
  - `brouter/build.gradle.kts`, three hunks. Both branches had added F0h's fetch, with different comment wording, and 5b
    had also added the drop-off fetch and its `dependsOn`/system property.
  - `TestTiles.kt`, two hunks: the same F0h comment, and 5b's `dropoff` property.
- **Full `:brouter:jvmTest` after the merge:** `compileKotlinJvm`, `compileTestKotlinJvm` and `jvmTest` executed, 0 ` e: `
  lines. 121 tests, 2 failed, both `CollectOutreachersTest` (-61) [C]. 121 = 5b's 111 + `E3CoverageRuleTest` 2,
  `E3ExpectedFileTest` 6 and `E3OutcomeTest` 2 [C, `tools/junit-counts.py`].
- **The coordinate scan was not honoured on this push.** It read 12, and the push was chained after it without a test.
  All 12 lines are in four files byte-identical to `origin/brouter-port` [C]. The planner ruled at -93 that 10 are
  Forager's own published ends and 2 are false positives (a float list, and a diff timestamp on two lines); there is no
  rewrite. Since then every push runs only on a 0 from `03-scan.sh`, which removes Forager's own ends by exact value
  (routes.json and points.json at a0535ece) before matching [C].
- **CI, run 42246987233 on `eac836d`** [C, job logs read]:
  - `jvm`: 121 tests, the same 2 red, 0 ` e: `; `compileKotlinIosArm64` and `compileKotlinIosSimulatorArm64` ran, BUILD
    SUCCESSFUL;
  - `ios-smoke`: 24 of 24 passed, **simulator-checked**; the arm64 JVM step's outcome was success, 7 of 7, and its gate
    printed "OK: all 2 classes ran".

## 2. atomicfu 0.33.0

- Pinned in `gradle/libs.versions.toml` as `atomicfu = "0.33.0"`, and added as `commonMain` `implementation(libs.atomicfu)`,
  a library with no plugin, as Phase 1's probe used it [C, `a9fb1d0`]. `jvmCompileClasspath` resolves
  `atomicfu:0.33.0 -> atomicfu-jvm:0.33.0` [C].
- **CI, run 42248137190 on `a9fb1d0`, before any code used it:** both `compileKotlinIos*` ran, BUILD SUCCESSFUL, 0 ` e: `;
  jvm 121 with the same 2 red; simulator-checked 24 of 24; arm64 7 of 7 with its gate [C]. With no code using it, this
  shows the klibs build with the dependency declared, not that its classes link [I].
- **The run on `de4d994`, the first where `ProfileCache` uses `SynchronizedObject`, is the full check.** It is read
  below in §5, or by the next session if this one ends first.

## 3. Step 6's files (sent to the planner; step 7 follows step 6, -94)

New, under `brouter/src/commonMain/kotlin/com/zynergylabs/forager/brouter/router/`. The Java `btools.router` classes:

| Class | Java lines [C, `wc -l`] | State at `de4d994` |
|---|---|---|
| `RoutingContext` | 579 | configuration half ported (`:1-284`, `:555-579`); nogo and distance half (`:285-554`) not yet; may become two files |
| `ProfileCache` | 142 | ported, with the lock |
| `OsmPathModel`, `OsmPrePath`, `StdModel`, `RoutingIslandException` | 20, 26, 36, 4 | ported |
| `OsmPath`, `StdPath` | 434, 356 | stubs |
| `OsmPathElement`, `MessageData`, `OsmNodeNamed`, `RoutingParamCollector`, `SearchBoundary`, `AreaInfo`, `OsmNogoPolygon` | 127, 104, 100, 374, 87, 71, 488 | not yet written |
| `OsmTrack` (step 7's) | — | the type alone; `containsNode` throws `"unsupported: OsmTrack.containsNode, step 7"` (-94) |

The Java total is 2,948 lines with comments [C]; Phase 1's 2,068 counts differently [?].
Tests go under `brouter/src/jvmTest/kotlin/.../router/`. No existing `commonMain` file has changed [C, `git diff --stat`
from `a9fb1d0`].

## 4. Piece 1: `RoutingContext`'s configuration half and `ProfileCache`

- **Stubs first, then tests seen failing** (`d6835aa`). Over the stubs, `compileKotlinJvm` and `compileTestKotlinJvm` ran
  with 0 ` e: ` lines, and the 2 tests failed on `NotImplementedError: step 6: ProfileCache`, 26 of 26 profiles in the read
  test [C].
- **`RoutingContextReadTest`.** Each engine's own `ProfileCache.parseProfile` fills a `RoutingContext` from Forager's
  parameters, set by the oracle's `RoutingParamCollector` through the existing `ExpressionsEngines.foragerRoutingContext`.
  Every public instance field of the oracle's `RoutingContext` typed boolean, int, long, double, float, Integer or String
  (76 of them [C]) is compared by name.
  - **Result:** equal on all 20 profiles that use `StdModel` [C].
  - The 6 car profiles name `KinematicModel` or `KinematicNoCostModel`. Neither is in step 6's row, so the port throws
    `UnsupportedOperationException("unsupported: path model <name>")`, and the test asserts exactly 6 such refusals [C].
  - Forager routes with `hiking-mountain.brf` (two references at a0535ece) [R]. So the Kinematic models are off its path
    [I: the planner may want to rule this].
- **`ProfileCacheLockTest`.** Two threads start `parseProfile` together. An okio `ForwardingFileSystem` lingers 150 ms on
  each metadata read of the profile and records the most threads inside at once. With the lock it reads 1 [C].
- **Reverts** (`01-revert-predictions.txt` written before each run; `02-*`). The runner restores each file from a saved
  copy, the XML is deleted before every run, and the forward change was confirmed present afterwards [C]:
  - **A, lock removed:** `ProfileCacheLockTest` fails, "threads inside parseProfile at once expected:<1> but was:<2>"; the
    read test passes. As predicted [C].
  - **B, `maxSpeed`'s foot branch divided as float.** The first form did not compile (1 ` e: ` line, not read). The second
    form fails the read test on `hiking-mountain.brf` and `shortest.brf`, each with only `maxSpeed` differing (the oracle
    6/3.6 as a double, bits 0x3ffaaaaaaaaaaaaa; the port the float quotient widened, bits 0x3ffaaaaac0000000). As predicted [C]. That these two are the only foot profiles is [I].
- **Full `:brouter:jvmTest` before the push:** 123 tests, only `CollectOutreachersTest`'s 2 red, 0 ` e: ` [C].
  `compileTestKotlinJvm` was up to date from the identical previous compile; `jvmTest` executed and wrote all 54 XML
  files fresh [C].
- **Differences from Java, each named in the KDoc:**
  - `profileBaseDir` and `debugProfileCache` are not read (JVM system properties; Forager sets neither [I]);
  - `lastUseTime` is a monotonic mark, so least-recently-used ties within one millisecond differ from Java [I];
  - `setAlternativeIdx` is the property's setter (a JVM signature clash otherwise);
  - Java's `String.hashCode` is written out (`javaHashCode`), because Kotlin/Native does not promise Java's value.

## 5. CI on `de4d994`

Not yet read when this report was written. Run id and result: see the handoff's first item.

## 6. Handoff (for session 11)

1. **Read CI on `de4d994`** (or the latest push), especially `compileKotlinIosArm64` and `compileKotlinIosSimulatorArm64`
   with `ProfileCache` now using `SynchronizedObject`. That is atomicfu's real klib check (-91). Report jvm 123 with only
   `CollectOutreachersTest`'s 2 red, simulator-checked 24, and arm64 7 with its gate.
2. **Next pieces, in order:**
   - (a) `OsmNodeNamed`, `OsmNogoPolygon`, `AreaInfo`, then `RoutingContext`'s nogo and distance half (`:285-554`, into
     `RoutingContextNogo.kt`, with its private lists made `internal`);
   - (b) `RoutingParamCollector` on Forager's strings and generated ones;
   - (c) `MessageData`, `OsmPathElement`, then `OsmPath` and `StdPath`, compared on decoded links with the oracle's costs.
     Two debts land there: -62's `interpolateEle` `short` cast on F0h's cut ends, with a revert that bites, and -42's way
     getters. Then the step's revert: one `(int)` cast moved inside a sum in `StdPath`, its prediction pushed first;
   - (d) `SearchBoundary`.
3. **For step 8 (-94):** check with the coverage reader that `OsmPath.java:350`'s `containsNode` true branch is not
   covered on R's R6 runs.
4. **Tools:**
   - scratch `revert.sh` (copy-restore, compile-error count, cmp);
   - `03-scan.sh` here: run as `n=$(scan.sh origin/brouter-port-5b HEAD); [ "$n" = 0 ] && git push`. It needs
     `forager-ends.txt` beside it, rebuilt from Forager's routes.json (both) and points.json at a0535ece.

## 7. Unverified

- That atomicfu's classes link into both iOS klibs: pending CI on `de4d994` [?].
- That Forager never sets `profileBaseDir`, and never uses a Kinematic profile on its path [I].
- That `refTrack` is always null on Forager's path (-94) [I, to be checked at step 8].
