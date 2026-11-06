# BRouter port, step 5d: the route-ways path (RECORD -70)

Plumb Ledger, seventh session, on `brouter-port-5b`, from `d1de74b`. Tags: [R] read from source or output,
[C] computed or run here, [I] inferred, [?] unknown.

## Status
- Item 1 (the `differingFields` defect): fixed. Items 2 to 6: in progress.

## 1. The `differingFields` defect (-69)
- **Cause** [R]: `WaypointMatchEngines.differingFields` split each end's line on spaces, and `n1=(lon, lat)` prints
  as two tokens, so the bare `lat)` token was "named" by itself (`substringBefore('=')` of a token with no `=` is the
  token).
- **Fix** [R]: the line is split at the keys `line` and `run` write (`ok cross n1 n2 r diff next name type`), and only
  a key from that fixed list is printed (`WaypointMatchEngines.kt`, `differingFields` and `fields`).
- **Seen** [C] (`2026-11-06-brouter-port-step5d/1-defect.txt`): a scratch revert of `WaypointMatcherImpl.kt:39`, never
  committed, `WaypointMatchTest` alone, 0 compile errors, restored from the saved copy each time. Before the fix the
  message held 33 coordinate fragments; after, 0, with the same field lists and the same 2,105.
