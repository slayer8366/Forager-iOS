# Step 5d, item 3: the MicroCache2 decode test's revert, predicted before it is run

Pushed before the run (RECORD -70, item 3). Forward head: the commit that adds this file's parent change
("5d item 3: port ...").

- **Edit (one line, scratch, never committed):** `MicroCache2.kt`, the reverse link appended for each internal link,
  `writeVarLengthSigned(alat[nodeIdx] - alat[n])` becomes `writeVarLengthSigned(alat[n] - alat[nodeIdx])`. One decoded
  field, the reverse link's latitude offset, has its sign flipped. The varint of `-v` is as long as that of `v`, so no
  byte offset moves and no other field can differ first.
- **Predicted:** 0 compile errors; `MicroCache2DecodeTest` 5 tests, 5 failures, each message naming field
  `[link dlat]` and no other field. Expected in cache 0 of each tile (any cache with an internal link between nodes of
  different latitude would do; cache 0 is predicted, not required).
- **Run by** the scratch one-class runner (same rules as `2026-10-30-brouter-port-step5b/revert-runner.sh`: saved copy,
  emptied results, build log checked for ` e: ` before any result, restored from the saved copy, forward line confirmed).
