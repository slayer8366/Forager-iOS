# Step 5b, fifth session: question A's data, predicted before any run

RECORD -59, A: count the oracle's `StackOverflowError` catch (`OsmNodesMap.java:62-64`) on a patched copy over
`CollectOutreachersTest`'s region loads at the default stack, then re-run only that class at `-Xss1g` from a scratch
init script, never committed. No fix is chosen. Written and pushed before any of these runs.

**The patched copy** (scratch only): Forager's `third_party/brouter/.../OsmNodesMap.java` (pre-main `d6e2a0415`), compiled
`--release 11` against the oracle jar, is bytecode-identical to the jar's own `OsmNodesMap.class` (`javap -c -p`, with
constant-pool numbers and spacing normalised: 0 lines differ) [C]. The patch adds one static counter and a `System.err`
line inside the catch, and a load marker in the static initializer. After it, `javap` differs only in
`cleanupPeninsulas` (the catch block), the new field and `<clinit>`; **`minVisitIdInSubtree`, the recursion, is
byte-identical**, so its frames are the oracle's [C]. `cleanupPeninsulas`'s own frame, one per call at the recursion's
base, gains at most a few operand slots [I].

**The runs:**
1. **Probe** (the way `tieBreakOrder` was counted, RECORD -53): `ProbeMain regions` on the four tiles, its own JVM,
   main thread, the JVM's default stack (`ThreadStackSize` read from `-XX:+PrintFlagsFinal`), the patched class first
   on the classpath, with each region's oracle `C` line printed.
2. **jvmTest, default stack:** only `CollectOutreachersTest`, with the patched class first on the test classpath (scratch
   init script). This is beyond -59's letter: it counts the oracle's catch in the test's own JVM and thread, where the
   oracle's `C` lines are read, and shows the patched class leaves those lines unchanged.
3. **jvmTest, `-Xss1g`:** the same, with `-Xss1g`, as -59 says.

**Predicted [I], reasons: the oracle's `C` line, 38715 41099 41099 in mode 1, has been the same in both runs that reached
it, while the port's catch count changed between them (3, then 2):**
- **the oracle's catch: 0** in all three runs; its `C` lines at W75_N40 (105,130) sub 735: mode 1 `C 38715 41099 41099`,
  mode 2 `C 21909 41099 41099`, unchanged at both stacks;
- **run 2:** the port's catch fires 1 or more times (it did in both runs so far); test 1 fails at `:141` as forward;
  test 2 fails at a region or at `:116`;
- **run 3:** the port's catch fires 0 times; the port's `C` lines equal the oracle's; **test 1 passes**; test 2 fails at
  `:116` ("canEscape did not answer both ways: false/true 0/3587"), since that assertion counts the oracle's answers;
- that `-Xss1g` reaches the Gradle test worker thread is [I]; the port's catch going silent would show it did.

**If the oracle's catch fires anywhere, or its `C` line differs between stacks:** that is -59's stop. It is reported to
the planner, and nothing is changed.
