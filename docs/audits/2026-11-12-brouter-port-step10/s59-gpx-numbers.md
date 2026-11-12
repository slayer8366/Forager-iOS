# s59: which numbers print differently in GPX mode 7 (Plumb Ledger, session 59)

A diagnosis; nothing in the port, CI or the build changed. Test `GpxNumbersCommonTest` (`69ffcd1`): for NT-0009, -0031,
-0045, -0053, -0061 (each stored in mode 7) it walks the hints as `FormatGpx`'s mode-7 branch does and prints each
floating value appended there (d `distanceToNext` `:166`, t `ti` `:171`, s `speed` `:172`) with its raw bits and `"" + x`.
JVM: a local run (JDK 21.0.12, `NtCasesWriteTest` then the test, `BROUTER_STEP10_NT_CASES` set); the `jvm` job does
not set the variable. Simulator: run 43329465498, `ios-smoke` (52 tests, 1 failure: `FormatDigestsCommonTest`, the same
5 GPX rows as session 58; the new test passed). **Simulator-checked.**
**Counts.**
- 1,287 values on each side (d 429, t 429, s 429; per route 273, 9, 36, 957, 12 = 3 x hints: every hint has a time).
  The same (case, index, kind) keys on both sides.
- **Raw bits: equal on all 1,287.** The values are the same; only text differs.
- **Text differs on 5: one per route, all `t` (the time `ti`), none `d` or `s`.** That accounts for each route's GPX
  digest (same length, another digest).

## The kind of difference
All 5: the same number of digits (17 significant), plain form, no exponent, no trailing `.0`; **only the last digit
differs, JVM even (2) against simulator one higher (3).** Indices: NT-0009 hint 2, NT-0031 1, NT-0045 1, NT-0053 4,
NT-0061 3. The texts (17-digit times) trip E1's decimal pattern, so the table with both texts is kept out of the
repository, in the session's scratchpad as `s59-gpx-numbers.tsv` (case, index, kind, bits, JVM, simulator).
Shape: `ti` is a `Float` difference widened to `Double` (low 29 mantissa bits zero), so its exact decimal value ends
in `...5` at the 18th significant digit: it lies exactly halfway between two 17-digit decimals.

## Which rule
- The Java documentation of `Double.toString(double)` (JDK 19+): of the shortest decimals that round to the value,
  the one closest to it; **if two are equally close, the one whose least significant digit is even.** The JVM's
  text follows that tie rule. Kotlin/Native's text picks the other candidate (rounds the tie up, from these 5 [I]), for the same bits.
- The simulator's text is **neither longer nor shorter**: the same length, the other half of a tie.
- **Both round-trip** (tested on the JVM, `parseDouble` then raw bits): both texts give the original bits for all 5.

## Other floating numbers printed with the platform's text (formats and hints)
- `FormatGpx.kt` (all `Double`): `:166`, `:171`, `:172` (mode 7); `:162` `selev / 4.0` (mode 7), `:195` the same (mode 6);
  `:276` `n.getElev()` (track points, every mode); `:507`, `:529` `getElev()` (waypoints, `exportWaypoints`).
- `FormatJson.kt`: `:104` `getElev()` `Double` (coordinates); `:184` `selev / 4.0` `Double` (features); `:223` `elev`
  `Double` (`formatAsWaypoint`); `:332` `speedText` `"" + Float` (JSON `:106` and GPX `:311`, `:359`, when `showspeed`).
- `FormatKml`, `FormatCsv`, `VoiceHint*`: none; `Formatter.kt:267`, `:277` are `Int`/`Long`. The brief's grep misses
  `:162`, `:195`, `:276` and JSON `:104`, `:184`, `:223` (string `+` a `Double`); found by reading.
- Why elevations did not differ: `selev / 4.0` is a multiple of 0.25, so its shortest decimal has at most two
  decimals and is unique (no tie at the shortest length); both platforms print it alike [I: elevations were not printed].
**Stops:** none hit. Premise "three floating numbers" in the branch: there is a fourth, `:162` (an elevation, not printed).
