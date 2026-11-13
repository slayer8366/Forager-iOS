# s63: plant (b) alone, predicted first (Plumb Ledger, session 63)

RECORD -164: "Owed: plant (b) alone, predicted first, on the simulator". Base `647741c` on `brouter-port-5b`.
In s60 plant (b) was masked in GPX by plant (a); this runs it alone.

## The edit (the planted commit, one line)
`FormatGpx.kt:176`: `.append("" + ti)` becomes `.append("" + (ti + 1.0))`. The `speed` line (`:175`, `:177`) is NOT
changed, so `<locus:rteSpeed>` keeps its value; only the `<locus:rteTime>` value moves by 1.0.

## Prediction (written before the plant; derived from the code at `647741c` and the stored rows)
**Where `:176` is reached.** Only in `formatAsGpx`'s mode-7 branch (`FormatGpx.kt:159`), per hint, when
`getVoiceHintTime(i + 1) != lastRteTime`. Hint times increase along the route and the last comparison is against the
end node's time, so any route with at least one hint and a positive travel time reaches it at least once (inferred, not
run). JSON, KML, CSV and the hints text never pass through `:176` (no other caller in `brouter/src`).
**Stored rows in mode 7** (`FormatDigests.kt`): F0 0 of 2 (modes 2, 3); F0h 1 of 8 (`F0h-detour-west`); NT 7 of 59.
All 8 have a track with hints (hints lengths 921; 249 to 26624; none is a no-route row: the three short NT rows,
hints length 5, are modes 3, 3, 5).
**Canonical GPX.** `FormatNumberText` replaces each `<locus:rteTime>` content by 16 hex digits of the value's bits, so
the GPX **length is unchanged and only the digest changes** on those 8 rows. `ti + 1.0` is always a different double.

Predicted, per item:
1. Simulator (`ios-smoke`), `step10 hints-formats stored` lines: F0 2 of 2 run, gpx equal 2; F0h 8 of 8, gpx equal 7;
   NT 59 of 59, gpx equal 52. hints, json, kml, csv equal on every row (2, 8, 59). 8 problem lines, all
   `gpx`, all `mode 7`, each with here-length = stored-length and a different digest.
2. Simulator: `ios-smoke` fails with exactly 1 failing test, `FormatDigestsCommonTest`; 57 tests; `GpxNumbersCommonTest`
   passes (it asserts only that lines were printed); `FormatNumberTextTest` passes (made-up text). Gate line
   `OK: all 2 classes ran` present.
3. JVM (`jvm`): 244 tests, 3 failures: `FormatGpxTest` (mode 7 against the oracle; it asserts `locus:rteTime` is
   reached), `HintsFormatsLogicTest` (port against oracle, F0h and NT include mode-7 cases), `FormatDigestsJvmTest`
   (the 8 rows' recomputed GPX digests). `FormatterTest` passes (it tests `Formatter`'s own methods, not `:176`; it failed
   in s60 for plant (a)). `FormatDigestsCommonTest` passes on the JVM (only F0 runs there, modes 2 and 3).
4. `jvm-production` succeeds (`RoutingEngineProductionRecordTest` records, asserts only that cases ran).
   `ios-memory` succeeds (no format output is compared there).
5. ` e: ` lines: 0 in every job.

Could not derive: the number of `<locus:rteTime>` elements per route (it does not change any count above).
