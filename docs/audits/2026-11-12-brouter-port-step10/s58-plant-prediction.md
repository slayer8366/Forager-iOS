# Session 58: prediction for the planted error in HintsFormatsLogicTest (written before the run)

Plumb Ledger, session 58. JVM only, never pushed as code. The plant, by a scratch copy of `revert.sh`:
`VoiceHint.kt` line 91, `cmdAngle < -21f` to `cmdAngle < -25f` (a turn of -21 to -25 degrees is no longer a slight
left; it falls to the next branch: keep left or slight left by the bad ways). The class has no filter property, so it
runs whole (all eleven groups, `OracleLibm`).

- **The test fails** (its one method), at the end, after every group has run.
- **Routes (the six fields):** equal in every case: the hints are made after the route.
- **Hints:** differ wherever a hint's angle falls in [-25, -21), and a changed command can also change how the
  hints are merged and counted. Some cases in most groups with turns (guess: 1 to 10 per 59-case group, tens in R);
  F0 and F0h 0 to 2 (guess). Every such case also counts in "differing beyond the angle" (the field is cmd).
- **GPX and JSON:** differ in a subset of the hints' cases (the modes that write the commands; guess: most of them).
- **KML and CSV:** 0 differ: neither carries the hints.
- **Restore:** `cmp`-equal to the saved copy; `git diff` on `VoiceHint.kt` empty after.
