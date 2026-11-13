# Step 5d, item 4: the route-ways test's revert (drop one tag), predicted before it is run

Pushed before the run (RECORD -70, -74). Forward head: `7072d5b` ("5d item 4: port OsmNode(long), ...").

- **Edit (one line, scratch, never committed):** `BExpressionContext.kt:131`, in `getKeyValueDescription`,
  `if (value.isNotEmpty()) {` becomes `if (value.isNotEmpty() && lookupNames[inum] != "highway") {`. The port's
  description of every way then lacks its `highway` tag; nothing else in the decode or the geometry changes.
- **Predicted:** 0 compile errors; `RouteWaysTest` 2 tests, 2 failures, each naming field `[tags]` and no other field,
  at the first point of each set: `dropoff walker` and `F0h inside-across-the-degree-line/from` (each has at least one
  way in its cells; the tag is predicted present on the first link of each, not checked before this run).
- **Run by** the scratch one-class runner `one-revert.sh` (saved copy, emptied results, build log checked for ` e: `
  before any result, restored from the saved copy, forward line confirmed).
