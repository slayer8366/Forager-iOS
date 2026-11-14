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

## Result (written after the run), run 43466735338 on the planted commit `a531a9e`
Commits: prediction `80df05c`, plant `a531a9e`, revert `337def4` (`git diff --stat 647741c 337def4 -- brouter/src`
empty). E1 scan 0 on each. ` e: ` lines 0 in all four job logs, so the results count as evidence.
**Deviation from the dispatch:** the prediction was committed before the plant, but it was not pushed on its own. My
push was chained after `scan.sh`, which exits 1 while printing 0, so the push was skipped. `80df05c` reached the
remote in the same push as `a531a9e` (remote went `647741c..a531a9e`). The text of the prediction is unchanged since
`80df05c`.

Scored item by item:
1. Simulator `step10 hints-formats stored` (simulator-checked). **Held.** F0 2 of 2 run, gpx equal 2. F0h 8 of 8,
   gpx equal 7. NT 59 of 59, gpx equal 52. hints, json, kml and csv were equal on all 2, 8 and 59 rows. There were 8
   problem lines, all `gpx`, all `mode 7` (`F0h-detour-west`, `NT-0009`, `-0031`, `-0045`, `-0053`, `-0061`, `-0069`,
   `-0079`), and each had the same length here as stored with a different digest. So the by-value comparison at
   `<locus:rteTime>` catches a different value on every one of the 8 real routes that reach `:176`.
2. `ios-smoke` failure. **Held.** `TOTAL: tests 57, failures 1`; the one failure is
   `FormatDigestsCommonTest.hintsAndFormatsEqualTheStoredDigestsOfThePortsProductionRun`. Gate `OK: all 2 classes ran`
   is present (that step: 7 tests, 0 failures).
3. `jvm` failure. **Held.** `TOTAL: tests 244, failures 3`: `FormatGpxTest`, `FormatDigestsJvmTest`,
   `HintsFormatsLogicTest`. `FormatterTest` passed. `FormatDigestsCommonTest` passed on the JVM.
4. **Held.** `jvm-production` success (`TOTAL: tests 2, failures 0`). `ios-memory` success (each step 0 failures).
5. **Held.** ` e: ` 0 in every job.

I did not read the problem text of the 3 JVM failures (only their names), and I did not run CI on the revert (not
waited for, as the dispatch allows). The inferred condition that every mode-7 route with hints reaches `:176` was not
checked separately; the 8 of 8 result fits it. Stops: none hit.
