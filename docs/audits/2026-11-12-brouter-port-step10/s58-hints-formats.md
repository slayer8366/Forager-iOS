# Session 58: the hints and formats production record, the simulator's digests, the JVM plant

Plumb Ledger, session 58. Head `f33e8a1` (code), run 43319416821. Lengths, digests, ids and counts only (E1).

## Production record (port on MsunLibm against the oracle; local run, then `jvm-production`, equal tables)

| group | cases | routes | route differs | hints | gpx | json | kml | csv | hints beyond angle |
|---|---|---|---|---|---|---|---|---|---|
| F0 | 2 | 2 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| F0h | 8 | 8 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| NT | 59 | 56 | 0 | 1 | 0 | 0 | 0 | 0 | 0 |
| LB, CR, DL-lat, DL-lon, MP64 | 59 each | 59 each | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| ML5 | 59 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| PM5 | 55 | 55 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| R | 968 | 968 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |

The one hints difference: NT-0062, mode 8, hint 380, field angle (as session 56 found). No format differs. The
expectation (`s58-production-expectation.md`, committed first) held. The routes' table: every group 0 differing.

## Stored digests (FormatDigests.kt, 69 rows from the port's production run on the JVM)

- JVM (`jvm` job and local): `FormatDigestsJvmTest` 69 of 69 equal on all five outputs; `FormatDigestsCommonTest`
  locally with the case files written: 69 of 69 equal. `jvm` 238 tests, 0 failures, test step 40 min.
- **Simulator (ios-smoke), simulator-checked: a STOP (RECORD -121's question).** F0 2 of 2 and F0h 8 of 8 equal on
  every output. NT: hints, JSON, KML, CSV 59 of 59 equal; **GPX 54 of 59: 5 differ, all mode 7** (old locus style),
  each with the same length and a different digest: NT-0009, NT-0031, NT-0045, NT-0053, NT-0061. No plant applied.
  Equal on the JVM. Nothing changed. Not diagnosed. The mode-7 branch of `FormatGpx` alone prints Doubles made from
  the hints' times and speeds (`"" + ti`, `"" + speed`); a candidate, not shown.
- ios-smoke 51 tests, 1 failure (this one); every route comparison equal; arm64 7 of 7, `OK: all 2 classes ran`.
- ios-memory unchanged, simulator-checked: memory 173 (ML5 59, MP64 59, PM5 55), R 240, ladder 20 of 20.

## Planted error, JVM only (`cmdAngle < -21f` to `-25f`, VoiceHint.kt 91; scratch revert.sh, cmp-equal restore)

Whole class (no filter property): fails, 1,395 differences. Six fields equal in every case. Differ, hints / gpx /
json: F0 1/1/1, F0h 5/5/5, NT 19/19/19, LB 14/14/14, CR 13/13/13, DL-lat 26/26/26, DL-lon 18/18/18, MP64 51/51/51,
ML5 0 (no routes), PM5 51/51/51, R 151/150/151; total 349/348/349; each hints difference beyond the angle. KML and
CSV 0. As predicted in kind; the counts are higher than guessed (MP64 and PM5 51 of 59 and 55).

## E1

Staged-diff scans 0 on each commit. Simulator log, timestamps cut: positions 0, `<gpx|<kml|coordinates|<trkpt` 0;
` e: ` 0 in jvm and ios-smoke.

## Not done

Item 7 (the simulator's plant in `Formatter.formatPos`): not started, because of the STOP above. The GPX mode-7
platform difference is the planner's to rule on; FormatDigestsCommonTest stays red on the simulator until then.
