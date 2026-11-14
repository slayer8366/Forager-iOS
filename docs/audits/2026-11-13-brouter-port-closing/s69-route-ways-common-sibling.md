# s69: the route-ways entry's common sibling, so `readRouteWays` runs on the iOS simulator (Plumb Ledger, session 69)

Base `e9eeddb` on `brouter-port-5b`, verified against `git ls-remote origin brouter-port-5b` before any work; tree clean.
This file is written, committed and pushed before any build or test run in this session. One probe ran before it, not
a test: a 10-line Java program on this container's JDK 21 (below, "The pure move").

## What was read (file and line)

- `brouter/src/commonMain/kotlin/com/zynergylabs/forager/brouter/RouteWays.kt` (120 lines): `readRouteWays` at `:59-90`,
  the per-link points at `:105-114` (the node, the decoded chain, then the target at `:112-113`).
- `brouter/src/jvmTest/.../mapaccess/RouteWaysEntryTest.kt`: the entry against the Java walk on the drop-off points and
  the F0h ends; its digest is `java.security.MessageDigest` SHA-256 (`:93`), a JVM-only API.
- `brouter/src/jvmTest/.../mapaccess/RouteWaysCells.kt` (48 lines): `cells` uses `Math.PI`, `Math.cos`, `Math.toRadians`
  (`:17-19`); `squareFile`, `micro` and `F0H_ENDS` are plain Kotlin.
- `brouter/src/commonTest/.../tiles/EngineRun.kt:84-125`: the common tests' digest helpers, `fnv1a(lon, lat)` (64-bit
  FNV-1a over int pairs), `fnv1aShorts` and `fnv1aText(s)` (64-bit FNV-1a over UTF-8 bytes). These are the helpers the
  other common tests use (`F0EngineCommonTest`, `EngineStopCommonTest:104-105`, `Step10Cases`); no second one is made.
- `brouter/src/commonTest/.../tiles/TestSetting.kt`: `testSetting(jvmProperty, iosVariable)`, `testEnvironment`,
  `onJvm`; `Step10Cases.kt:30-41`: `ntFile`, which fails on iOS when `BROUTER_STEP10_NT_CASES` is unset.
- `brouter/build.gradle.kts:126-133`: `oracleJvm()` depends on `fetchF0h` and `fetchDropoff` and sets
  `brouter.tiles.f0h.dir` and `brouter.oracle.dir` for jvmTest.
- `.github/workflows/brouter.yml`:
  - `ios-smoke`'s simulator step (`:88-95`) is `./gradlew --no-daemon :brouter:iosSimulatorArm64Test` with **no
    `--tests` filter**: every commonTest class runs on the simulator. It exports only `BROUTER_TILES_DIR`,
    `BROUTER_ORACLE_DIR` and `BROUTER_STEP10_NT_CASES`.
  - The gate line `OK: all 2 classes ran` is `tools/junit-ran.py` over `brouter/build/test-results/jvmTest` with
    `$ARM64_JVM_CLASSES` (`:51-54`, `:115-122`): it gates the **arm64 JVM step's two named classes**, not the simulator.
    So the gate does not select or name simulator classes, and a new commonTest class needs no change there.
  - The writer step before the simulator step (`:84-87`) runs `:brouter:jvmTest --tests ...NtCasesWriteTest`, which
    through `oracleJvm()` runs `fetchF0h`; `NtCasesWriteTest.kt:143-156` writes `f0h-cases.tsv` beside the NT file, each
    line's eighth field the heighted square's folder (`TestTiles.f0h`, absolute).
  - `ios-memory` runs `iosSimulatorArm64Test` only with `--tests` filters naming its own classes (`:165-169`, `:186`,
    `:202`), so the new class does not run there.
  - The drop-off fixture (`build/tiles-dropoff`) is fetched on the macOS runner too (same `oracleJvm()` dependency) but
    no variable names it to the simulator.

## What the test stores

New `brouter/src/commonTest/kotlin/com/zynergylabs/forager/brouter/tiles/RouteWaysCommonTest.kt` and its rows in
`RouteWaysExpected.kt` beside it (as `F0hExpected.kt` holds F0h's rows).

**Inputs: the F0h ends only**, `RouteWaysCells.F0H_ENDS` (Forager's own route ends, already committed in jvmTest and, as
the same eight pairs, in `F0EngineCommonTest`/`EngineStopCommonTest`'s waypoint text for the first). 16 points (eight
names, from and to), the cells 150 m around each by `RouteWaysCells.cells`, grouped by `squareFile`, one
`readRouteWays` call per square, divisor 32, `lookups.dat` at `<oracle dir>/profiles2/lookups.dat` (on the JVM the same
file as `ExpressionsEngines.lookups`, `OracleCalls.oracleDir` from the same property). The drop-off points are not used:
their coordinates live only in Forager's fetched `points.json`, and reaching it on the simulator needs a new variable.

**Per call, one row** (16 predicted, one per point, since s68 found no F0h point's circle touching two squares):
- id: `<end name>/<from|to> square <i> of <n>` (names only, as s68's messages);
- the outcome's kind must be `Read`;
- divisor;
- cell count asked and an `EngineRun.fnv1a` digest of the cells asked (lon cells, lat cells), **an addition beyond the
  brief's list**: the cells come from `cos` on each platform's own libm (`kotlin.math.cos`), so if the simulator's
  `cos` gave another cell at a boundary, this field says so and separates a platform-math difference from a reader
  difference. A digest and a count, never a cell index (E1: whether a cell index counts as a coordinate is not settled,
  so none is stored or printed);
- link count;
- total point count (sum of every link's `iLat.size`; `iLat.size == iLon.size` is asserted per link);
- digest: `EngineRun.fnv1aText` over the concatenation, in walk order, of each link's
  `description + "\t" + iLat.joinToString(",") + "\t" + iLon.joinToString(",") + "\n"` (the same per-link text
  `RouteWaysEntryTest` feeds its SHA-256, `:117`, hashed with the common helper instead).

**Provenance of the rows.** They are drawn on the JVM from `readRouteWays` itself (the class run once with an empty row
list prints each row as a Kotlin line; counts and digests only). They are therefore **the port's own output**, anchored
to the Java engine only through `RouteWaysEntryTest`, which compares the same entry on the same F0h points, link by
link, against the Java walk, and must pass in the same run and in the same commit range. On the simulator the class
holds the port to the port's JVM output; a difference there is a platform difference, not a Java difference.

**Compared:** every field of every row; mismatches are collected and asserted at the end as one list, each entry the
id and the field, with stored and got for the counts (divisor, cells, links, points) and no digest value. The row count
and the id order are asserted too, so a call that disappears or moves fails.

**A second test, `aSquareOfAnotherDivisorIsNotRead`:** the first F0h point's square, a 1/32 square, asked at 1/80 gives
`OtherDivisor` carrying 32 (stored; the JVM test holds the same square's divisor to the Java `PhysicalFile`'s), and
unpaired cell arrays give `IllegalArgumentException`, as `RouteWaysEntryTest.aSquareOfAnotherDivisorIsNotRead`.

N = 2 tests.

## Where the heighted square comes from on each platform (a decision, with the rejected alternatives)

- JVM: `testSetting("brouter.tiles.f0h.dir", ...)` + `/f0h`, the property `TestTiles.f0h` reads.
- Simulator: no variable names it. The folder is read from `f0h-cases.tsv` beside the NT file
  (`Step10Cases.ntFile`), the eighth field of its lines, which must all be equal; this is the route `F0hEngineCommonTest`
  and `FormatDigestsCommonTest` already take to the same square. Unset on iOS fails, as `ntFile` does; it never skips.
- Rejected: a new `SIMCTL_CHILD_BROUTER_TILES_F0H_DIR` in `ios-smoke` (a workflow change the brief did not ask for,
  when an existing route reaches the same file); deriving `<tiles dir>/../tiles-f0h/f0h` (an unstated build-layout
  assumption).

## The pure move

`RouteWaysCells.kt` moves from `jvmTest/.../mapaccess/` to `commonTest/.../mapaccess/` (same package, so
`RouteWaysTest` and `RouteWaysEntryTest` keep their imports and do not change). Only `cells` changes:
`Math.PI` to `kotlin.math.PI`, `Math.cos` to `kotlin.math.cos` (on the JVM both are `java.lang.Math`'s), and
`Math.toRadians(lat)`, which has no common counterpart, to `lat * (PI / 180.0)`. The probe before this file: on this
container's JDK 21.0.12, `Math.toRadians(x) == x * (Math.PI / 180.0)` for all 15 distinct F0h end latitudes and 10
million random values, 0 mismatches; the form `x / 180.0 * Math.PI` differs on 4 of the end latitudes, so it is not used.
Evidence the move changed nothing: `RouteWaysTest`'s and `RouteWaysEntryTest`'s print lines (counts and digests) before
the move (a run at `e9eeddb`) and after, equal byte for byte.

## Predictions

**Before the move, at `e9eeddb`** (`RouteWaysTest` and `RouteWaysEntryTest`): 0 ` e: `, 5 tests, 0 failures; digests
`b7fccbb3...`, `5a5c054d...` (RouteWaysTest), `069e566d...`, `45a3d729...` (RouteWaysEntryTest), as s68 printed.

**After the move, with the new class and its rows** (the three classes, one run): 0 ` e: `, fresh XML, 7 tests,
0 failures; the four print lines equal to the run before. The new class: 16 rows, every divisor 32, link counts summing
to 4858 and point counts to 43006 (s68's F0h totals for the same points and cells).

**JVM TOTAL in CI:** 249 + 2 = 251, 0 failures.
**Simulator, `ios-smoke`:** TOTAL 58 + 2 = 60, 0 failures; the arm64 JVM step's TOTAL 7 unchanged; gate line
`OK: all 2 classes ran` unchanged. A simulator TOTAL other than 60 means the class did not run (or something else
changed) and is a stop. The class's printed line must appear in the simulator's XML. Simulator-checked.
**`jvm-production`:** 2. **`ios-memory`:** 3, 1, 1 (the class does not run there).

**The plant** (`revert.sh`, `RouteWays.kt`, delete the two lines `lat += target.ilat` and `lon += target.ilon`, the
new class alone on the JVM): it compiles (`target` is still used by `decodeGeometry`). Every link loses its last point,
so each row's point count falls by exactly its link count and its digest changes; divisor, cells and link count are
unchanged. Predicted: 0 ` e: `, fresh XML, 2 tests, 1 failure, `f0hEndsReadAsStored`, its message listing for each of
the 16 rows with at least one link `points <stored>/<stored - links>` and `digest`, and no `divisor`, `cells`, `links`
or `kind` entry; `aSquareOfAnotherDivisorIsNotRead` passes. Then the file restored from the saved copy, `cmp` equal,
`git diff HEAD` empty.

**Limits stated now:** the simulator compares against the port's JVM output, not the Java engine; as in s68, no F0h
link is reverse and no point spans two squares, so neither is exercised on the simulator either.
