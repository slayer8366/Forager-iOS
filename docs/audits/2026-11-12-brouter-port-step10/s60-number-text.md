# s60: the number text compared by value (Plumb Ledger, session 60)

The owner's ruling (RECORD -163), carried out on `brouter-port-5b`: design `f0c21b9` (in its message, before any run),
change `28e2a79`, plant `f026e5e`, revert `0497284`. The port's printing is not changed; two comments name the
difference (`FormatGpx.kt`, `FormatJson.kt` headers).
**Design.** GPX's and JSON's digest and length are taken over a canonical text (`FormatNumberText`, commonTest): each
floating number printed with the platform's own text is replaced by the 16 hex digits of `toDouble().toRawBits()` (8 of
`toFloat().toRawBits()` for `speedText`'s `Float`); every other character kept; a text that does not parse stays text.
Hints, KML and CSV: plain text, as before. The stored GPX/JSON length is the canonical text's: it pins every kept
character and the count of replaced numbers. Rows regenerated from the port's production run on the JVM; every row's
hints, KML and CSV values came out unchanged. `HintsFormatsLogicTest` not changed.
**The twelve places and their recognition.**
- GPX by enclosing element: `:162`, `:195`, `:276`, `:507`, `:529` `<ele>` (every `<ele>` in FormatGpx); `:166`
  `<locus:rteDistance>`, `:171` `<locus:rteTime>`, `:172` `<locus:rteSpeed>`: Double. `:332` via GPX `:311`, `:359`:
  `<brouter:speed>`, Float.
- JSON under a line `"coordinates": [`: `:104` third number of each inner `[a, b, c]` line (Float `:332` when the track
  has `showspeed`); `:184`, `:223` the third element line of a flat (Point) array, Double. Positions (first two) stay text.
**No longer caught:** a different text for the same value at those places. **Still caught:** a different value there;
any difference elsewhere in GPX/JSON, positions included; hints, KML, CSV byte for byte.
**Unit test** `FormatNumberTextTest` (5 tests, made-up numbers): failed 4 of 5 over the stub on the JVM (the fifth
checks the outputs left unchanged), passes on the JVM and the simulator.
**After the change, run 43364528773 (`28e2a79`), simulator-checked:** `ios-smoke` 57 tests, 0 failures; `OK: all 2
classes ran`.

| group | rows run | hints | gpx | json | kml | csv |
|---|---|---|---|---|---|---|
| F0 | 2 of 2 | 2 | 2 | 2 | 2 | 2 |
| F0h | 8 of 8 | 8 | 8 | 8 | 8 | 8 |
| NT | 59 of 59 | 59 | 59 | 59 | 59 | 59 |

`jvm` 244 tests, 0 failures; `jvm-production` success; `ios-memory` 173 (ML5 59, MP64 59, PM5 55), R 240, ladder 20 of
20, all equal. ` e: ` 0. E1 on the simulator log: decimals 0, format text 0.
**Plant** (`f026e5e`): (a) `formatPos` `i == 5` to `i == 6`; (b) `ti` to `ti + 1.0` at GPX `:176` (s59's `:171`).
Prediction: simulator GPX, JSON, KML differ on every routed case, hints and CSV equal, routes equal, (b) masked by (a)
in GPX; JVM FormatterTest, FormatGpxTest, FormatJsonTest, FormatKmlTest, HintsFormatsLogicTest, FormatDigestsJvmTest,
FormatDigestsCommonTest fail. **Result, run 43371249259:** as predicted. Simulator (simulator-checked): 57 tests, 1
failure (FormatDigestsCommonTest); gpx/json/kml equal F0 0 of 2, F0h 0 of 8, NT 3 of 59 (the 3 with no route); hints
and CSV equal on all; engine comparisons mismatches 0. JVM: 7 failures, exactly the seven named. E1 0, ` e: ` 0.
**Revert** `0497284`: `git diff --stat 28e2a79 HEAD -- brouter/src` empty. (b) alone was not run separately.
**Stops:** none hit.
