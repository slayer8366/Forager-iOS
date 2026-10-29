10-tests: evidence for item E, the differential tests (part 8, Hazel, 2026-10-29)

E is a design. Nothing here routes, and no tile was downloaded. The runs below compute facts the
design depends on: which scale-table rows differ, which profiles switch on which features, and the
sample-size arithmetic.

00-bases-part8.txt          bases at the start: Forager pre-main d100a4776 (unchanged), the read-only
                            clone at e2969abe4 and clean, Forager-iOS main 87d9250, brouter-port 6736856,
                            and part 3's scratch zip re-checked against fetch-brouter.sh:12-13.

tools/TableRows.java        CheapRuler's 1,800-row table (calcKxKyFromILat copied from the vendored
                            CheapRuler.java:35-49), built with Math.cos and with StrictMath.cos.
logs/table-javac.log        its compile (exit 0).
logs/table-run.log          three runs on HotSpot 21.0.12.1 (Ubuntu build), amd64: default flags;
                            -XX:+UnlockDiagnosticVMOptions -XX:-UseLibmIntrinsic; -Xint.
                            Output SHA-256s (files in scratch, hazel/part8/table/out/):
                              4ab07de6... math.txt, math-xint.txt  (= part 6's jvm-amd64/scale-table.txt)
                              ee004dc0... strict.txt, strict-nointrinsic.txt, strict-xint.txt,
                                          math-nointrinsic.txt
tools/table_rows.py         compares two tables row by row; a row matters for routing when kx or ky
                            differs (CheapRuler.java:56-57 hand out only those); maps each row to its
                            latitude band and brouter.de tile row.
logs/rows-*.txt             its five comparisons:
  rows-part6-vs-part8.txt     check on the copy: 0 of 1,800 rows differ from part 6's JVM table.
  rows-math-vs-strict.txt     60 cos rows, 54 kx/ky rows (8 of them in the N40 tile row).
  rows-math-vs-native-x64.txt 8 cos rows (part 6's count), 6 kx/ky rows.
  rows-math-vs-native-arm64.txt 3 cos rows, 3 kx/ky rows.
  rows-nointrinsic-vs-strict.txt 0: with the libm intrinsic off, HotSpot's Math.cos table is StrictMath's.

tools/profile_features.py   each of the release zip's 26 profiles: the effective value of each T2
                            feature switch (the profile's literal, or RoutingContext's default).
logs/profile-features.tsv   its output. The first run missed mtb.brf's indented "---context:global"
                            line; the turnInstructionMode count (23) against grep (24 files) caught it.
                            Fixed (the script now fails on a profile with no global section) and rerun:
                            24 and 16 match grep.
logs/crossing-reach.txt     lookups.dat (blob 60e59d08, the pinned one) holds estimated_crossing_class
                            at :862-868; the 24 profiles that assign priorityclassifier.

tools/sample_size.py        exact sample sizes: n = ceil(ln(beta)/ln(1-p)), checked by the inequality
                            at n and n-1; clean-run bounds 1 - beta^(1/n); Clopper-Pearson intervals by
                            bisection, checked against the closed form at x = 0.
logs/sample-size.txt        its output.

Reproduce: javac -d <cls> tools/TableRows.java; java -cp <cls> TableRows <math> <strict> (add the two
flags for the no-intrinsic run); python3 -I tools/table_rows.py <name> <A> <B>;
python3 -I tools/profile_features.py <unzipped brouter-1.7.10/profiles2>; python3 -I tools/sample_size.py.
The zip is the release asset pinned by Forager's fetch-brouter.sh:11-13.
