Item H evidence (part 5, Hazel, 2026-10-29).

tools/fp_sites.py <javap dump> <reached-methods.tsv> <out dir>
  Reads part 1's `javap -v -c -p` dump of the vendored BRouter classes (scratch:
  hazel/build/javap.txt, compiled with line tables, class major version 55) and one
  tier's reached-method list (03-reach/out/T2/reached-methods.tsv). Lists, per reached
  method and source line: double/float arithmetic, comparisons, conversions, Math calls,
  Float/Double calls, floats printed into strings, and hash-collection iteration.

out/T2/summary.txt, out/T2/sites.tsv
  The run on tier T2 (the owner's "Any profile" scope, RECORD -8).

Checks against something outside the script:
  - 481 of 481 reached methods were found in the dump.
  - 923 methods parsed = 923 `descriptor: (` lines in the dump (grep -c).
  - The first run reported 0 floats printed into strings. That was the script, not the
    code: Java 11 bytecode concatenates strings with invokedynamic
    makeConcatWithConstants, which the first version did not read. The dump holds 29
    such call sites taking a D and 12 taking an F (grep). Fixed; the rerun finds 4 in
    reached methods.
  - Hash iteration: the script finds 7 reached methods. B's row 18 lists 9 source sites.
    3 of B's 9 (BExpressionContext.java:453 dumpStatistics, :524 variableName,
    RoutingParamCollector.java:260 setProfileParams) are in methods no tier reaches
    (grep of T0-T3 reached-methods.tsv: 0 each; no caller of setProfileParams or
    dumpStatistics in BRouter or Forager main sources; variableName's one caller is an
    error message at BExpressionContext.java:517, in an unreached method). The script
    adds RoutingEngine.java:1568 (keySet into a TreeSet), which B listed under sorted
    sets instead (row 6).
  - A wider pass over every java.util iteration call in reached methods found no other
    Map or Set iteration: Map.keySet 2, Map.entrySet 2, Map.values 3, Set.iterator 3,
    Collection.iterator 3 (the three values() results), SortedSet.iterator 1; the rest
    are List iterations.
