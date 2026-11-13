# s68: the route-ways reader's public entry (plan item 4), tests first against the Java engine (Plumb Ledger, session 68)

Specification: RECORD `2026-10-28-172` (the planner's decision on the entry's shape), with `-74` (step 5d's route-ways
comparison). Base `fd717d1` on `brouter-port-5b`, verified against `git ls-remote origin brouter-port-5b` before any
work; tree clean. This file is written, committed and pushed before any build or test run in this session.

## What was read (file and line)

Forager's reader, `/home/user/Forager/app/src/main/java/com/zynergylabs/forager/app/routing/BRouterRouteWays.kt`
(137 lines, read in this session from the read-only Android clone), `read` at lines 59 to 123:

- **Way context and lookups, one per read:** `BExpressionMetaData()`, `BExpressionContextWay(meta)`, then
  `meta.readMetaData(lookups)` at lines 60 to 62. One `read` call covers every square the circle touches, so the one
  way context is shared across cells and squares.
- **Geometry decoder, one per read:** `GeometryDecoder()` at line 72, shared across cells and squares.
- **Squares:** the cells are grouped by square file name (line 73), then per square: a missing file throws Forager's
  `IOException("no route square ...")` (line 76); `DataBuffers()` per square (line 77); `PhysicalFile(file, buffers,
  -1, -1)` (line 78), outside the `try`; the divisor check, Forager's message, inside the `try` (line 80); the cells
  (lines 81 to 112); `square.close()` in the `finally` (line 115).
- **Cells:** per cell, a new `OsmFile(square, lonDegree, latDegree, buffers)` with the degree as
  `cell * CELL / 1_000_000` (line 82, `CELL = 1_000_000 / 32`, line 123); `if (!degree.hasData()) continue` (line 83);
  `createMicroCache(lonCell, latCell, buffers, null, null, true, null) ?: continue` (line 84); a new `OsmNodesMap()`
  per cell (line 85).
- **Nodes and links:** for `i in 0 until cache.size`, `getIdForIndex(i)`, `getAndClear(id)`, `OsmNode(id)`,
  `parseNodeBody(cache, nodes, wayContext)` (lines 86 to 90); every link from `firstlink` by `getNext(node)` (lines 91,
  110); only links with a `descriptionBitmap` (lines 93, 94); points: the node, `decodeGeometry(geometry, node, target,
  reverse)`'s chain, the target (lines 97 to 105); then Forager's own parts: the end-pair dedupe (lines 106 to 108),
  `tagsOf` (line 108, 134 to 135) and `positionOf` (line 131).

The port, `brouter/src/commonMain/kotlin/com/zynergylabs/forager/brouter/`:

- `Route.kt` (109 lines): the public surface's conventions. Raw values out (`RouteOutcome.Found`'s parallel `iLat`,
  `iLon` `IntArray`s, `:69`); a sealed outcome (`:68-72`); the engine's constructor exceptions pass through (`:79-80`,
  `:85`); `FileSystem.SYSTEM` (`:85`, `:105`). Its header (`:15-18`) says the engine's classes are internal.
- `mapaccess/OsmFile.kt:72`: `hasData()` is `microCaches != null`, false when the degree has no data in the square
  (`:45`, `fileOffset == index[tileIndex]`).
- `mapaccess/OsmFile.kt:113-143`: the seven-argument `createMicroCache` returns `MicroCache.emptyCache()` for a cell
  with no bytes (`:122-124`), `null` only when `reallyDecode` is false (`:137-139`), and a `MicroCache2` when
  `hollowNodes` is null (`:141-142`). So with `reallyDecode = true`, as line 84 passes, the `?: continue` of line 84 can
  never fire [read; not run]; an empty cell yields a cache of size 0, which walks no node and so yields nothing.
- `mapaccess/GeometryDecoder.kt:25-28`: it returns its last result again when the geometry array is the same object
  (`===`) and the direction is the same; otherwise it decodes. The arrays come from `ByteDataReader.readDataUntil`
  (`util/ByteDataReader.kt:100-108`), a new array per link read (`OsmNode.kt:141`).
- `expressions/BExpressionContext.kt:124-137`: `getKeyValueDescription` decodes the bitmap's bytes into `lookupData`
  afresh on every call (`decode`, `:99-121`), and the text depends only on those bytes, `inverseDirection` and the
  lookup tables read from `lookups.dat`.
- `OsmNode.kt:110-146`: `parseNodeBody2` unifies descriptions through the way context (`readUnified(descSize,
  expCtxWay)`, `:139`) and node descriptions through the nodes map's unifier (`:111`, `:127`). Unifying returns an
  equal byte array, possibly a shared one.
- `expressions/BExpressionMetaData.kt:33-66`: `readMetaData` wraps every exception, a missing file included, as
  `RuntimeException(e)`, as Java does.
- `mapaccess/PhysicalFile.kt:30-79`: the constructor opens the file (`ReadOnlyFile`, `:32`, an okio `FileHandle`,
  `ReadOnlyFile.kt:14`) and then can throw (`:42`, `:57`, `:69`, or a short read) without closing it.

## The details the brief asked to settle

1. **One way context and one geometry decoder per read in Forager; one per square call in the entry.** The entry is
   per square, so it builds its own way context (and reads `lookups.dat`) and its own `GeometryDecoder` on each call,
   and shares them across that call's cells, as Forager shares them across cells. Does that change any output? By the
   code, no: the description text depends only on the bitmap's bytes and the direction (`BExpressionContext.kt:124`),
   and unifying through a different context returns equal bytes; the decoder's cache is keyed on array identity, an
   array is never shared between two squares (each link's geometry is a new array read from its own cell), and a cache
   hit returns what a decode of the same bytes in the same direction would give. [Inferred from the code.] The test
   checks it empirically: its Java side is Forager's walk as `RouteWaysTest` makes it, with one context and one decoder
   per point across all its squares; the entry side calls the entry once per square. Where a point's circle touches
   only one square, the per-square split is not exercised; the test prints how many entry calls it made per set, so
   the reader can see whether any point spans two squares.
2. **`OsmNodesMap` and `DataBuffers` lifetimes.** As Forager: one `DataBuffers` per square (line 77), shared by the
   square's header read, every `OsmFile` and every `createMicroCache`; one `OsmNodesMap` per cell (line 85).
3. **A cell with no data or a null micro-cache yields nothing.** As Forager, both `continue` (lines 83, 84). The first
   happens when the cell's degree has no data in the square. The second cannot happen with `reallyDecode = true`
   (above); the entry keeps the same `?: continue` so the walk is Forager's line for line, and this file records that
   it is unreachable. A cell whose degree has data but whose cell has no bytes gives an empty cache and so no link.
4. **The square is closed on every path once it is open.** `PhysicalFile` is constructed before the `try`, as at line
   78, and closed in the `finally`, so every path after construction closes it: the divisor refusal, a normal return,
   and any exception from the walk. **A finding, not fixed here:** when `PhysicalFile`'s own constructor throws after
   opening the file (`PhysicalFile.kt:32` then `:42`, `:57`, `:69` or a short read), the file handle is not closed, and
   no caller can close it, since the instance is never returned. That is the same in Forager's reader (line 78), in
   the port's `checkDataFile` (`Route.kt:105`) and in Java's `PhysicalFile`. Closing it needs a change to
   `PhysicalFile`, an existing engine class, which this item does not make. Reported to the planner.

## The signature (new file `brouter/src/commonMain/kotlin/com/zynergylabs/forager/brouter/RouteWays.kt`)

`Route.kt` is 109 lines; the entry with its three types and comments is about 100 more, so it goes in a new file in
the same package, and `Route.kt` is not touched (its header comment, "all of it", is left as is; the new file's header
says it extends that surface by RECORD -172).

```kotlin
/** One square file's read, as Forager asks for it. */
class RouteWaysRequest(
    /** The square's `.rd5` file. */
    val square: Path,
    /** The `lookups.dat` the square was built with. */
    val lookups: Path,
    /** The micro-cache cells to read, in this order: cell i is (lonCells[i], latCells[i]), in 1/[divisor] degree. */
    val lonCells: IntArray,
    val latCells: IntArray,
    /** The cell size the caller computed its cells with: 32 for Forager's squares. */
    val divisor: Int,
)

/** One link that has a description: its points (the node, the decoded geometry, the target) and the description text. */
class RouteWaysLink(val iLat: IntArray, val iLon: IntArray, val description: String)

sealed interface RouteWaysOutcome {
    /** The square's divisor and its links, in walk order. */
    class Read(val divisor: Int, val links: List<RouteWaysLink>) : RouteWaysOutcome

    /** The square's cells are not 1/[RouteWaysRequest.divisor] degree; no cell was read. */
    class OtherDivisor(val divisor: Int) : RouteWaysOutcome
}

/** Reads one square's links, blocking; the caller picks the thread. */
fun readRouteWays(request: RouteWaysRequest): RouteWaysOutcome
```

- Points are parallel `iLat`, `iLon` arrays, as `RouteOutcome.Found` carries them; Forager's `positionOf` converts.
- `description` is `getKeyValueDescription(reverse, bitmap)` unchanged; Forager's `tagsOf` splits it.
- The public surface grows by exactly these: one function, its request (`RouteWaysRequest`) and its outcome
  (`RouteWaysOutcome` with its two cases, and `RouteWaysLink`, the item the outcome's list holds). No engine class
  becomes public.
- Order inside the body, Forager's: `lonCells.size == latCells.size` is required first (`require`, an
  `IllegalArgumentException`: a caller's error, not data); then `lookups.dat` is read (a missing or bad file leaves as
  the engine's `RuntimeException`, `BExpressionMetaData.kt:64`); then the square is opened (a missing or bad file leaves
  as okio's `IOException`, as `route()`'s constructor exceptions do); then the divisor; then the cells.
- **Not in the port, as -172 says:** which cells a circle covers, the square file's name, the "no route square" and
  wrong-divisor messages, the end-pair dedupe, the tag-map split, the conversion to positions.
- **Not checked by the entry, recorded:** that each cell lies in the square given. A square file carries no position
  of its own except its name, and the name is Forager's (-172), so the entry cannot check it without taking that over.
  A cell from another square reads the degree at the same place in this file's 5 by 5 grid. Forager groups its cells by
  square name before reading (line 73), so its adapter never passes one; the contract is stated in the request's doc.

## The divisor decision: the entry refuses a divisor the caller did not ask for

The request carries the divisor the caller computed its cells with, and when the square's own divisor differs the
entry returns `OtherDivisor(squareDivisor)` and reads no cell. Reason: the cells' meaning depends on the divisor. A
cell index is in 1/divisor degree, and `OsmFile.createMicroCache` turns it into a sub-index with the square's divisor
(`OsmFile.kt:122`, `subIdx = (latIdx - divisor * latDegree) * divisor + ...`). Cells computed for 1/32 degree read
against a 1/80 square (the old format, `PhysicalFile.kt:64-68`) would read other cells and return plausible links from
the wrong place, or throw from an index out of range, before any caller could see the divisor. Forager checks before
it walks (line 80) for this reason. Returning the divisor only, and leaving the check to the adapter, would let the
entry fabricate a plausible read (Forager's CLAUDE.md: "never a fabricated plausible value"). The message stays
Forager's: `OtherDivisor` carries the square's divisor and the adapter words it. `Read` carries the divisor too, as
-172 asks; it is then always the one asked for.

Rejected: taking no divisor and deriving the cell size from the square (the caller's cells would still be computed at
1/32 and silently mean something else at 1/80); throwing an `IOException` from the entry (the message would become the
port's, and -172 keeps it Forager's).

## The stub

The stub commit declares the four public types in full and `readRouteWays` with a body of
`throw NotImplementedError("readRouteWays: s68 stub, the body comes in the next commit")`. Throwing, not an empty
result: -74's planner call ("stubs throw `NotImplementedError`, as 5c's did"), and an empty `Read` would be a plausible
value. The stub is on no production path (nothing calls it but the new test).

## The test: `brouter/src/jvmTest/kotlin/com/zynergylabs/forager/brouter/mapaccess/RouteWaysEntryTest.kt`

Beside `RouteWaysTest`, same point sets, same cells:

- **Sharing with `RouteWaysTest`, a pure move.** `RouteWaysTest`'s Java walk cannot be reused as it stands: it emits
  the tag map split by `tagsOf` and the dedupe flag, not the raw text, so reusing it would change what it asserts. Its
  point and cell helpers are reused through a pure move: `cells`, `squareFile`, `micro`, `DIVISOR`, `CELL`, `RADIUS`,
  `EARTH_MEAN_RADIUS_METERS` and `F0H_ENDS` move unchanged into a new `internal object RouteWaysCells` in
  `RouteWaysCells.kt` in the same test package, and `RouteWaysTest` imports them by name, so none of its other lines
  changes. Evidence the move changed nothing: `RouteWaysTest`'s two print lines (counts and digest) are equal before
  and after; the digests at `fd717d1`, from earlier runs' logs, are `b7fccbb3...` (dropoff) and `5a5c054d...` (F0h).
  The dropoff points' three-line parse stays where it is in `RouteWaysTest` (it is inside a test method) and is
  repeated in the new class, held by the same count assertion (15).
- **The Java side** is `RouteWaysTest.oracleRead`'s walk on the Java engine (`btools.*`), point by point, one way
  context and one decoder per point as Forager, but recorded per square: the square's `PhysicalFile.divisor` and, per
  link with a description, the points (`iLat`, `iLon` arrays: the node, the decoded chain, the target) and
  `getKeyValueDescription(reverse, bitmap)`. It also counts, as `RouteWaysTest` does, cells (each non-null cache),
  nodes (each `getAndClear` true), links with a description and geometry points.
- **The entry side** calls `readRouteWays` once per square of each point, with the square's path, the oracle's
  `lookups.dat` (`ExpressionsEngines.lookups`, the file `RouteWaysTest` reads), the square's cells in the order
  `RouteWaysTest` reads them, and divisor 32. An exception from the entry fails the test naming the set, the point and
  the exception's class and message (the stub's message carries no value).
- **Compared, per square:** the outcome is `Read`; its divisor equals the Java square's; its link count equals the
  Java walk's. **Per link, in order:** the point count, each `iLat`, each `iLon`, the description text. A failure names
  the set, the point, the square's index (not its name), the link's index and the field, never a value.
- **Totals:** per set, printed, Java side: cells, nodes, links, geometry points; entry side: links and geometry points;
  the entry's two are asserted equal to the Java side's. They should equal `RouteWaysTest`'s own: dropoff cells 18,
  nodes 135, links 120, geometry points 270; F0h cells 21, nodes 3983, links 4858, geometry points 43006 (from
  `RouteWaysTest`'s earlier print, read in the session tools' logs; checked again against its run in this session).
  The entry does not report cells or nodes, so those two are the Java side's only ("nodes where available").
- **A third test, `aSquareOfAnotherDivisorIsNotRead`:** the drop-off square asked for at divisor 80 gives
  `OtherDivisor` carrying the Java `PhysicalFile`'s divisor for that square (32); and unpaired cell arrays give
  `IllegalArgumentException`.
- Prints only counts and one digest per set (of the entry's links, description and points, as text); no value (E1).

## Predictions

**Red over the stub** (the new class alone, `--tests '*RouteWaysEntryTest'`): 0 ` e: ` lines, fresh XML, 3 tests,
3 failures, each the stub's `NotImplementedError` reached through the entry:
- `dropoffPoints`: `dropoff walker: the entry threw NotImplementedError: readRouteWays: s68 stub, the body comes in the
  next commit` (walker is the first point in `points.json`'s order).
- `f0hEnds`: the same with `F0h inside-across-the-degree-line/from`.
- `aSquareOfAnotherDivisorIsNotRead`: `kotlin.NotImplementedError: readRouteWays: s68 stub, ...` uncaught (the first
  call is the divisor case). Its unpaired-arrays assertion comes after and is not reached.

**The body** (the new class and `RouteWaysTest`, one run): 0 ` e: `, fresh XML, 5 tests, 0 failures. The new class
prints: dropoff entry calls 15 (the drop-off tile's `segments` folder holds one square file), cells 18, nodes 135, links
120 and 120, geometry points 270 and 270; F0h cells 21, nodes 3983, links 4858 and 4858, geometry points 43006 and
43006, entry calls 16 or more. `RouteWaysTest` prints the same two lines as before the move.

**The plant** (`revert.sh`, on `RouteWays.kt`, the new class alone): `getKeyValueDescription(reverse, bitmap)` becomes
`getKeyValueDescription(!reverse, bitmap)`. Only a comparison of the text can see it: the points, counts and divisor
are unchanged. `RouteWaysTest`'s earlier prints show every described link has `reverse` false (reverse 0 of 120 and
of 4858), so with the plant `inverseDirection` is true, `ld[0]` is 2 (`BExpressionContext.kt:104`), and the text
gains `reversedirection=yes` (the hardwired value, `BExpressionContextLookups.kt:20`) on every link. Predicted:
0 ` e: `, fresh XML, 3 tests, 2 failures:
- `dropoffPoints`: `dropoff walker: square 1 of 1, link 1 of <n>: field [description]`;
- `f0hEnds`: `F0h inside-across-the-degree-line/from: square 1 of <m>, link 1 of <n>: field [description]`;
- the divisor test passes.
Then restored from the saved copy, `cmp` equal, and the body still in the tree (`git diff` empty against the body's
commit).

**A limit of the plant, stated now:** since no described link in either set is reverse, a body that passed a literal
`false` in place of `reverse` would pass this test too. The data cannot tell those apart; the test covers the
direction only as far as the data does.

**CI on the body's commit:** `jvm` TOTAL 249 tests (246 plus 3), 0 failures; `ios-smoke` 58 and 7 with the gate line
`OK: all 2 classes ran` (no iOS test is added; the new code compiles for iOS there); `jvm-production` 2; `ios-memory`
3, 1, 1; 0 ` e: ` lines in every job log. The red commit's CI run will show a red `jvm` job with these 3 failures,
expected.
