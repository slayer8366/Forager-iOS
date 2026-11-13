Step 5d (RECORD -70 item 2): reach of BRouterRouteWays.read's calls (Forager a0535ece, BRouterRouteWays.kt:59-123),
beside T0-T3, never in their place. Tools: ../tools/entries-routeways.txt, ../tools/exclusions-routeways.txt (tier W,
applied alone), ../tools/RouteWaysCalls.java (the dynamic harness). Engine compiled as ../tools/run.sh does.
  SRC_ROOT=<src> TIERS=W python3 -I reach.py javap.txt spans.tsv entries-routeways.txt exclusions-routeways.txt jdk-javap out/RW
Unexcluded (no tier): 175 methods. Tier W: 141 methods, 12 of 12 edges and 3 of 3 methods used.
JaCoCo 0.8.14 on the harness (dropoff tile: pin_a 800 m, pin_d 150 m; Phase 1's W75_N40 tile: two points): 129 ran,
0 ran but not statically reached, 11 reached but not run (compare-with-jacoco.txt).
JDK: 27 classes, 87 members, every one also in T2's jdk-use.txt (none new to Phase 1's item B table).
not-ported.tsv: checked against brouter/src/commonMain at e26e13d by name per Kotlin file, then by hand for refusals.
not-ported.tsv: 27 methods, 440 Java lines by method span (spans.tsv), plus 2 partial branches; under -70's 600-line stop.
