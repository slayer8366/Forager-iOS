# Step 5d, item 4: ByteArrayUnifierTest's revert, predicted before it is run

Pushed before the run (planner's conditions on the CI red of run 42165333186, RECORD -74 follow-up).

- **Edit (one line, scratch, never committed):** `ByteArrayUnifier.kt`, the hit test `if (i == len) {` becomes
  `if (i == len + 1) {`. A cached array equal to the input is then never returned: every call allocates.
- **Predicted:** 0 compile errors; `ByteArrayUnifierTest` 3 tests, 2 failures: `unifyAsTheOracleWithoutValidation` and
  `unifyAsTheOracleWithValidation`, each at the first call the oracle reuses (the same call index in both, as the input
  sequence is the same), the message naming that call; `constructs` passes. The bytes stay equal, so it is the reuse
  pattern (same-as) that differs, not the bytes (the message does not distinguish them; the line is the call's).
- **Run by** the scratch one-class runner `one-revert.sh`.
