# Session 58: expectation for the hints and formats production record (written before the run)

Plumb Ledger, session 58. `RoutingEngineProductionRecordTest.hintsAndFormatsProductionRunIsRecordedAgainstTheOracle`,
the port on `MsunLibm` against the oracle, groups and modes as `HintsFormatsLogicTest` (RECORD -150, decision 1).
Written and committed before the method's first run. Counts marked (guess) are guesses.

- **Groups and cases:** F0 2, F0h 8, NT 59, LB 59, CR 59, DL-lat 59, DL-lon 59, MP64 59, ML5 59, PM5 55, R 968;
  every case runs; routes as in the logic table (all routed).
- **Routes:** 0 differ in every group (the routes' production record: 1,742 of 1,742 equal, RECORD -154).
- **Hints:** NT 1 differs: NT-0062, hint 380, field angle (RECORD -150). Elsewhere 0 to 3 in all (guess), in R if
  anywhere, each an angle only. Hints differing beyond the angle: 0 in every group.
- **GPX, KML, CSV:** 0 differ in every group (guess: where they carry an angle it is printed rounded, so a residue
  of 1e-15 does not show).
- **JSON:** 0 differ (guess, less sure: if a format prints the angle unrounded, NT-0062 differs there too, and then
  in that format only, by a character inside the angle).
- **Stop if:** any format differs other than through NT-0062's angle, or any hint differs beyond the angle.
