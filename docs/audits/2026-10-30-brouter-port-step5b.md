# Step 5b (`mapaccess`, the graph): report

Dispatch 2026-10-28-01, Amendment 3 Part B; RECORD -49, with -24, -32, -34 and -38. The secondary coder (its name is
the owner's to choose), second session. Branch `brouter-port-5b`, cut from `origin/brouter-port` at `9f41799`, the
planner's merge of 5a. The base was checked against the remote first: `git ls-remote` gave `brouter-port` at
`9f41799dc20a`, so the primary had not pushed since [C]. Evidence in `2026-10-30-brouter-port-step5b/`.

**Status (2026-10-30, fourth session, Plumb Ledger): partial, red. Handed off at the planner's request** (182k tokens,
08:00Z). The map is written (`1a5a25f`); the forward run is 101 tests with 3 failures. See "Fourth session" and its
handoff, which replace the third session's.

**Earlier status (third session): partial. Handed off at the planner's request** (180,380 tokens at
07:40Z), after RECORD -50's answer. This session wrote no code. It traced `put`'s callers and counted `tieBreakOrder` on
the regions, the routes and part of the decodes; see "Third session" and the handoff at the end, which replaces the
second session's. Done and pushed by the second session:
- 5a closed (`4cc7f20`);
- the sizing (`3495f64`, sent to the planner);
- the tests seen failing on their stubs (`c53fd51`);
- the port except the map (`a8e0231`);
- the revert's prediction (`6cf89c8`);
- the three conditions from data (`966d810`).

**The map is not written** (Amendment 3: "size it ... and report the count before writing it"). So the decode tests
cannot pass yet, the revert cannot run, and the branch is red in CI on the map's stub alone. The handoff at the end
says what remains.

Tags: [R] read (file and line), [C] computed (log in the evidence folder), [I] inferred, [?] could not determine.

## Mechanism prediction (before building)
- `OsmFile.createMicroCache`'s `TODO` becomes Java's two lines (`OsmFile.java:141-142`): construct a `DirectWeaver` over
  the cache's `StatCoderContext`, then return `MicroCache.emptyNonVirgin`. The weaver decodes straight into `OsmNode` and
  `OsmLink` objects through the codec step 3 ported, and registers each link target it has not seen as a hollow node in
  `OsmNodesMap` [R: `DirectWeaver.java:19-171`].
- The digest test decodes every non-empty micro-cache of each captured tile on both engines, one fresh `OsmNodesMap`
  per cache, and hashes, per cache, the nodes (position, elevation, description, `visitID`), every link from each node
  in chain order (target, direction, way description, geometry), each node's turn restrictions, and the hollow nodes
  the cache left in the map. If the weaver is right, every digest is equal; one inverted direction flag changes the
  link chain of every cache with an internal link.

## The `hmap` sizing (option (i), RECORD -24)

**Source.** `java/util/HashMap.java` from the oracle's own JDK, Temurin 21.0.13+9: `tools/fetch-temurin.sh` checked the
tarball against its pins (207,858,352 bytes, SHA-256 `d7f5d5cf…6d1c`), and the source is in that JDK's `lib/src.zip`.
The extracted file is 2,587 lines, SHA-256 `71c8d822…aabf2` [C: `1-hmap-sizing/provenance.txt`]. No other source was
needed.

**What `OsmNodesMap` uses.** One map, `new HashMap<>(4096)` (`OsmNodesMap.java:16`), through four calls: `get`
(`:223`), `remove` (`:229`), `put` (`:239`) and iteration of `values()` in `collectOutreachers` (`:181`) [R, Forager's
`third_party` copy, which equals upstream v1.7.10 at 0 of 101 files differing:
`2026-10-28-brouter-port-verify/01-vendored-vs-upstream.txt`].

**The count, method by method** [C: `1-hmap-sizing/hashmap-spans-out.tsv`, each span from its declaration to its
matching brace; "code" excludes blank and comment-only lines]:

| Group | Methods (HashMap.java lines) | Java lines | code |
|---|---|---|---|
| Declarations | 5 constants (`:245, 250, 260, 267, 275`); `Node`'s fields and constructor (`:281-292`); fields `table`, `size`, `modCount`, `threshold`, `loadFactor` | 22 | 21 |
| Hash helpers | `hash` (`:336-339`) 4; `comparableClassFor` (`:345-362`) 18, reached but null at its first test, since `OsmNode` is not `Comparable` (`OsmNode.java:14`); `tableSizeFor` (`:377-380`) 4 | 26 | 26 |
| Constructors | `HashMap(int, float)` (`:445-456`) 12; `HashMap(int)` (`:469-471`) 3 | 15 | 15 |
| `get` | `get` (`:562-565`) 4; `getNode` (`:573-591`) 19 | 23 | 23 |
| `put` | `put` (`:617-619`) 3; `putVal` (`:631-672`) 42; `resize` (`:683-755`) 73; `treeifyBin` (`:761-780`) 20 | 138 | 138 |
| `remove` | `remove` (`:803-807`) 5; `removeNode` (`:819-858`) 40 | 45 | 45 |
| Iteration | `values` (`:1039-1046`) 8; `Values`' class line and `iterator` (`:1048, 1051`) 2; `HashIterator`'s fields, constructor, `hasNext`, `nextNode` (`:1581-1612`, `:1624`) 33; `ValueIterator` (`:1631-1634`) 4 | 47 | 44 |
| Node factories | `newNode`, `replacementNode`, `newTreeNode`, `replacementTreeNode` (`:1908-1925`) | 12 | 12 |
| Tree bins: shape and lookup | `TreeNode`'s fields and constructor (`:1966-1974`) 9; `root` 7; `moveRootToFront` 21; `find` 26; `getTreeNode` 3; `tieBreakOrder` 9 | 75 | 75 |
| Tree bins: insert | `treeify` (`:2071-2111`) 41; `putTreeVal` (`:2133-2177`) 45; `balanceInsertion` (`:2383-2436`) 54; `rotateLeft` 17; `rotateRight` 17 | 174 | 172 |
| Tree bins: remove and resize | `removeTreeNode` (`:2189-2285`) 97; `balanceDeletion` (`:2438-2528`) 91; `untreeify` (`:2117-2128`) 12; `split` (`:2297-2342`) 46 | 246 | 242 |
| **Total reached** | | **823** | **813** |

Not counted, with why: `compareComparables` (`:369-372`), never called for `OsmNode` keys because `kc` stays null; the
three empty `afterNode*` hooks (`:1941-1943`); `checkInvariants` (`:2533-2553`), reached only from an `assert` and
with no effect on order; `HashIterator.remove` (`:1614-1623`), never called. Of the 823 lines, **575 are tree bins**
[C].

**The Kotlin estimate [I]: about 950 code lines, about 1,050 with comments; range 850 to 1,200.**
- Start from 813 code lines; `comparableClassFor` becomes one documented constant `null` (−16).
- Java assigns inside conditions at least 99 times in these spans (`if ((p = tab[i]) == null)`), counted by a regex
  that undercounts [C: `1-hmap-sizing/embedded-assignments.tsv`]. Kotlin needs a statement for each: about +100 to +130.
- Java's multi-variable `for (TreeNode<K,V> r = this, p;;)` loops become `while` loops with declarations: about +40.
- Specialised to `OsmNode`, so no generics. **Corrected after the first push:** keys and values stay separate.
  `OsmNodesMap.put` passes `node, node`, but a `put` of a second node equal to a present one keeps the old key and
  stores the new value (`HashMap.java:660-667`), and `NodesCache.java:284-288` does exactly that. The line count is
  unchanged.
- **Three files, each under about 400 lines** (Forager's modular-code rule): the table, `get`, `put`, `remove`,
  `resize` and iteration; tree insert and lookup; tree removal and `split`.

**Can tree buckets occur for `OsmNode` keys? Yes.** [R]
- `OsmNode.hashCode()` is `ilon + ilat` (`OsmNode.java:260`). Every node on one anti-diagonal shares a hash, and nodes
  with equal hashes share a bin at every table size.
- A bin becomes a tree when a `put` makes it 9 long (`HashMap.java:649`, `TREEIFY_THRESHOLD` 8 at `:260`), provided
  the table has at least 64 slots (`:763`). The table starts at 4,096 here (`OsmNodesMap.java:16` through
  `tableSizeFor`), so that condition always holds.
- **A tree bin holding two different nodes with equal hashes is not reproducible bit for bit, by any port.** `OsmNode`
  is not `Comparable`, so `treeify` (`:2095`) and `putTreeVal` (`:2158`) break the tie with `tieBreakOrder`, which
  compares class names (equal) and then `System.identityHashCode` (`:2063`). `putTreeVal` links each new node after its
  tree parent (`:2164-2169`) and `moveRootToFront` moves the root to the head of the bin (`:1990`), so the identity
  hashes reach iteration order. Whether two oracle runs even agree there is [I]: the identity hash is not part of the
  program. **Tree bins whose hashes all differ are deterministic:** every comparison is on `hash` [R].
- Whether either kind occurs on the captured tiles is [?] for now. The decode harness will measure, on the oracle, the
  longest bin and any tree bin as the tiles' caches are loaded into one map.

**For the planner, not chosen here:** what the port does when it reaches `tieBreakOrder`. The test, as dispatched,
refuses equal-hash tree buckets in its generator, so it does not decide this.

## Tests first, over stubs (item I, step 5b)
Nine tests in `jvmTest/.../mapaccess/`, with stubs for every 5b class and for the map (`NodeHashMap.kt`, signatures
only). Run 2 counted: **0 compile errors; 100 tests, 9 failures, each a `NotImplementedError` from a 5b stub or from 5a's
`TODO("DirectWeaver, item I step 5b")` in `OsmFile`; the earlier 91 pass** [C: `2-tests-over-stubs.txt`]. Run 1 failed
`CollectOutreachersTest`'s two on the harness's own bug (the tile was closed before the region loaded), not on a stub;
the harness was fixed and that run is not counted.
- **`GraphDecodeTest`, one test per captured tile (5):** every non-empty micro-cache decoded on both engines through
  `OsmFile.createMicroCache`, four times each: *known* (every node of the cache already a hollow node in the map, ids
  from the oracle's own `MicroCache2`; the text covers every node, isolated ones included, each link's geometry through
  `GeometryDecoder`, link holders, and every turn its restrictions decide in four modes) and *fresh* in cleanup modes
  0, 1 and 2 (the map's counters and every node reachable from the hollow nodes left). A cache passes when the two
  texts are equal; the per-tile digest is the SHA-256 of the texts. It also asserts that every oracle decode returns
  `MicroCache.emptyNonVirgin` with size 0 (RECORD -32's data).
- **`CollectOutreachersTest` (2):** a 3 by 3 block of caches around the largest cache of four tiles' first data
  square, loaded into one map per engine in modes 0, 1 and 2; `collectOutreachers` without a destination (then
  `canEscape` on every 37th node, `clearTemp`, `isInMemoryBounds`), and with a destination and the median distance as
  the cost bound, where the test fails unless some node is cut off.
- **`NodeHashMapOrderTest` (1):** the port's map against `java.util.HashMap(4096)` of the oracle's `OsmNode`s, 12 seeds
  of 40,000 operations up to 14,000 keys: forced tree bins of distinct hashes, anti-diagonal runs of equal hashes in list
  bins, re-puts of equal nodes, gets and removes. Order compared every 200 operations. The generator refuses
  equal-hash tree buckets by construction, and the reference's table is read after every operation that changes a bin
  (and at every resize): an equal-hash tree bin stops the seed as refused, and the test fails on any refusal. It also
  fails unless it saw tree bins, puts into and removes from them, a resize with tree bins, a tree turning back into a
  list, and equal hashes in list bins. This needs `--add-opens java.base/java.util` on `jvmTest` (a call beyond the
  dispatch, below).
- **`OsmNodePairSetTest` (1):** 20 seeds of 5,000 operations on both engines, every answer compared.

## The port, except the map
Ported line for line from 1.7.10 (Forager's `third_party` copy): `OsmLink`, `OsmNode`, `OsmPos`, `OsmLinkHolder`,
`OsmTransferNode`, `TurnRestriction`, `OsmNodePairSet`, `GeometryDecoder`, `DirectWeaver`, and `OsmNodesMap` with its
`hmap` behind `NodeHashMap`, still a stub. `OsmFile.createMicroCache`'s `TODO` is now Java's two lines (weave, return
`MicroCache.emptyNonVirgin`).
- **Local `jvmTest`: 0 compile errors; 100 tests, 8 failures, every one a `NotImplementedError` from `NodeHashMap`'s
  stub; `OsmNodePairSetTest` passes** (13,766 `hasPair` hits, 4,411 temporary pairs past capacity). Both iOS klibs
  compile with no `e:` lines [C: `3-port-without-the-map.txt`]. **`OsmNodePairSetTest` is the only 5b test shown to
  pass on the port so far;** the other eight cannot run past the map.
- **A test changed after the stub run:** `CollectOutreachersTest`'s destination case now also calls `canEscape` after
  the collection, and fails unless `canEscape` answers both ways. The case without a destination samples only nodes
  that reach a hollow node, so it could only ever see `true`. It was still failing on a stub when changed.
- Java-identity comparisons (`==` on nodes and links) are Kotlin's `===`, because `OsmNode` overrides `equals`.

**Calls beyond the dispatch, for the planner:**
- **`calcDistance` and `collectOutreachers` take the `LibmSeam`,** as `CheapRuler.distance` does, so the engine can carry
  the table (step 8). Java's signatures have no parameter.
- **`cleanupPeninsulas` has no `StackOverflowError` catch.** Java swallows an overflow there (`OsmNodesMap.java:62-64`);
  common Kotlin has no such type, and a Kotlin/Native thread cannot catch a stack overflow. The port lets an overflow
  end the search. The recursion depth on the captured tiles is measured below.
- **`parseNodeBody`'s `MicroCache2` branch is a named "unsupported"** (RECORD -32's condition, answered below), and
  `parseNodeBody2`, `OsmNode(long)` and the `toString`s are not ported (not reached).
- **`jvmTest` opens `java.util` to reflection** (`brouter/build.gradle.kts`), for the order test only.

## The three open conditions, from data
**How the data was taken** [C: `5-probe/`]. A scratch harness decodes every non-empty micro-cache of the five captured
tiles on the oracle (25,072 caches: W75_N40 19,671, W70_N40 3,904, E170_S50 767, W75_N35 683, F0 47), four times each
as the test does (known in mode 0; fresh in modes 0, 1 and 2), and writes one digest per cache and scenario.
- **The plain jar ran twice, in two JVMs: the 25,072 lines are identical** (`cmp`; SHA-256 `282555f9…6685a` both
  times). **No cache decoded differently between runs, so the E3-like stop is not met.**
- **The counters** come from copies of four 1.7.10 classes (Forager's `third_party` source, equal to upstream), with
  counters added by a script (`5-probe/src/patch.py`), first on the classpath. **That run's 25,072 lines are identical
  to the plain jar's,** so the counters changed no decode. The same classes ran F0's two routes on both folders, and
  they reproduce E3's expected results, (226, 5047) and (306, 6713).

**-38, the largest noisy-bit count: 19, at W70_N40, degree (113, 133), sub-index 794** (a 46-byte cache), an argument
`decodePredictedValue` passed to `decodeNoisyDiff`. No call passed more than 24. By caller [C:
`5-probe/shadow-counters.txt`]:
- `decodePredictedValue`: 19, against the generator's at most 24 (`StatCoderContextTest`, kind 2);
- other `decodeNoisyDiff` calls (`NoisyDiffCoder`'s stream-read counts and `DirectWeaver`'s fixed 10): 14, first at
  W75_N40 (105, 130) sub 51. The generator's kind 1 draws 0 to 14, so the tiles reach its edge and do not pass it;
- `decodeNoisyNumber`: 10, `DirectWeaver`'s fixed 1, 5 and 10; the generator draws 0 to 16.
- **Nothing exceeds the generator's range, so -38 records no finding and the test does not widen.**

**-34, `fillBuffer`'s end-of-array branch: it runs on real tiles, 40,772 times, but never in a micro-cache's stream.**
- **The cache streams: 0 times in all 25,072 caches,** 83 of them larger than `DataBuffers`' 65,636-byte `iobuffer`
  (82 in W75_N40, up to 191,419 bytes, and 1 in E170_S50). Those 83 are decoded from an array of exactly their own size
  (`OsmFile.java:128-130`), so their streams end where the array ends, and still none read past it [C:
  `5-probe/sizes.txt`].
- **Every hit is a `BitCoderContext` over a way's tag bitmap, 1 to 10 bytes long.** First at W75_N40 (105, 130) sub 0,
  on a 1-byte array, through `BExpressionContext.decode` (`:204`) from `evaluate` (`:409`), from
  `BExpressionContextWay.accessType` (`:116`), from `TagValueCoder.decodeTree` (`:135`), as the validator reads each way
  tag set. F0's two routes hit it 9,402 times on the same path.
- **So the branch is on production's path, in step 4's expression decoding over step 2a's `BitCoderContext`.**
  `GraphDecodeTest` drives it on the port through the validator, so it is the first test to compare that branch's
  results with the oracle's. It can do so only once the map exists.

**-32, `OsmNode.java:134`'s `readUnified`: it does not run here, and cannot under Forager's settings.** Reachability
first [R]:
- `readUnified` at `:134` is called only by `parseNodeBody2`, and `parseNodeBody2` only by `parseNodeBody` (`:112`), for
  a `MicroCache2`.
- `parseNodeBody` has two callers. One is `AreaReader.java:176`, which T2 does not reach (0 methods,
  `03-reach/out/T2/port-list.tsv`). The other is `NodesCache.obtainNonHollowNode` (`:227`), which calls it only when
  `segment.getAndClear(id)` is true. That needs a segment with entries (`MicroCache.java:93-95`).
- The segment comes from `OsmFile.createMicroCache` (`NodesCache.java:189`), which returns `emptyCache()`,
  `emptyNonVirgin` (both empty), or a `MicroCache2` only when `hollowNodes` is null. That happens only under
  `-DdisableDirectWeaving` (`NodesCache.java:58`), which nothing in Forager sets (`git grep`: only the two reads at
  `NodesCache.java:58` and `RoutingEngine.java:94`), and which the port fixes off.

The data agrees [C]:
- every one of the 125,360 decodes the probe made (25,072 caches times four, plus the whole-tile pass) returned
  `emptyNonVirgin` with size 0;
- `parseNodeBody`, `parseNodeBody2` and the `:134` call counted 0 over the decodes and over F0's two routes on both
  folders.

**So `unify` is not in scope, and `parseNodeBody`'s `MicroCache2` branch stays a named "unsupported".**

**Two more findings from the same data, for the planner:**
- **`hmap` tree buckets on the captured tiles** [C]:
  - **Production-like loads: none.** Each cache decoded into a fresh map: longest bin 3, at most 422 nodes. Every cache
    of each tile loaded into one map in mode 1: longest bin 6, at most 3,615 nodes. F0's routes: longest bin 3.
  - **The test's own "known" seeding: one.** That seeding puts every node of one cache in the map first (up to 15,582
    nodes). At W75_N40 (106, 130) sub 740 (105,494 bytes), a bin reached 9 and became a tree, **and that tree holds
    equal hashes.** The decode test's texts never use the map's order (hollow nodes are sorted), so the oracle's
    identity-hash tie there cannot change a comparison. But a port whose `tieBreakOrder` fails as "unsupported" would
    fail `GraphDecodeTest.w75n40` at that cache. That bears on the planner's answer.
- **The peninsula recursion reaches depth 5,679** (W75_N40 (106, 130) sub 801, 108,012 bytes, fresh, mode 1). No
  `StackOverflowError` was caught on this JVM's default stack. **Whether Kotlin/Native on iOS survives 5,679 frames is
  [?]**: no iOS thread has run it. A secondary iOS thread's default stack is smaller than a JVM thread's [I]. The port
  has no catch (above), so an overflow there would end the search, where Java on Android may survive it. This is for
  step 10's simulator run, and for the planner.

## CI, read from the runs
- **`4cc7f20`, run 41011251736, and `3495f64`, run 41011525077: `success`.** These are before any 5b test (conclusions
  read through the API).
- **`6cf89c8`, run 41014149582: `jvm` failure, `ios-smoke` success** [C: job logs]:
  - `jvm`: 100 tests, 8 failures, every one at `NodeHashMap`'s stub. The log names the `hmap` stub in the
    `NotImplementedError` on 8 lines;
  - both iOS klibs compiled after the tests, with 0 ` e: ` lines;
  - `ios-smoke`: simulator 23 of 23 (**simulator-checked**), and the arm64 JVM step 7 of 7 with "OK: all 2 classes ran".
  - `c53fd51` (run 41012954617) and `a8e0231` (run 41014086817) are `failure`, as expected over stubs. Their logs were
    not read.
- **Not yet known: `GraphDecodeTest`'s time in CI.** The oracle alone took 6 min 48 s for W75_N40's four decodes per
  cache [C: `5-probe/stability.txt`], and the test does that on both engines and writes longer texts, so it may take
  about 15 minutes [I]. The `jvm` job's limit is 30 minutes (`brouter.yml:18`). Whether W75_N40's decode belongs in
  every push's `jvmTest`, or in a separate task as E3's strata are, is the planner's call. Nothing was moved.

## Unverified
- **Every 5b test except `OsmNodePairSetTest` has yet to run on the port:** the decode, region and order tests cannot
  get past the map's stub.
- The revert: predicted and pushed (`4-revert-prediction.md`), not run.
- Kotlin/Native at a recursion depth of 5,679 [?]; iOS has run no 5b code. All 5b tests are `jvmTest`, because they
  compare with the oracle.
- `GraphDecodeTest`'s CI time [?].

## Third session: RECORD -50's answer, before the handoff
Base: `brouter-port-5b` at `508121e`, equal to `origin/brouter-port-5b` after a fetch [C]. RECORD -50 was read from the
planner's clone.

**`put`'s callers (RECORD -50: "check the callers of `put` first") [R, Forager's `third_party` 1.7.10]:**
- `OsmNodesMap.put` (`OsmNodesMap.java:238-239`, `hmap.put(node, node)`) is the map's only `put`. Its callers:
  - `OsmNode.java:177-179`: `new OsmNode(linklon, linklat)`, reached from `DirectWeaver.java:163` for each new link target;
  - `NodesCache.java:282-284` (`getGraphNode`): `new OsmNode(template.ilon, template.ilat)`. The template may be a
    waypoint's node, but only its coordinates are copied;
  - `NodesCache.java:288`: `existing`, the value `put` just returned, which is already in the map;
  - `NodesCache.java:271-273` (`getStartNode`, from `RoutingEngine.java:605`): `new OsmNode(id)`;
  - `AreaReader.java:155-174`: its own map, with `new OsmNode(id)`. T2 does not reach it.
- `get`'s key is `testKey`, a plain `new OsmNode()` (`OsmNodesMap.java:20`). `remove`'s keys come from `get`
  (`DirectWeaver.java:43-48`) or are `this` in `parseNodeBody2` (`OsmNode.java:152`, unreachable, RECORD -32).
- `OsmNode` has subclasses: `OsmNodeNamed` (`OsmNodeNamed.java:12`) and `OsmNogoPolygon` (`OsmNogoPolygon.java:16`).
  **Neither can reach the map** by any path above.
- **So every key is exactly `btools.mapaccess.OsmNode`.** `tieBreakOrder`'s class-name comparison (`HashMap.java:2061`)
  always reads 0, so it is not ported. The port's `OsmNode` is `open` (`OsmNode.kt:19`) and has no subclass yet.
- **Not a wrong premise:** RECORD -50 foresaw the one-class case ("if more than one class can ever be a key").

**`tieBreakOrder` on the oracle (RECORD -50's "data owed"), partial** [C: `6-tiebreak/`]:
- **Method:**
  - Temurin 21.0.13+9's own `HashMap.java` (SHA-256 `71c8d822…`, as sized), with counters added at its two call sites,
    `treeify` (`:2095`) and `putTreeVal` (`:2158`), split by whether both keys are `btools.mapaccess.OsmNode`
    (`patch_hashmap.py`);
  - patched into `java.base` with `--patch-module`, under the plain 1.7.10 `-ro` jar and the oracle's flags;
  - the probe is `5-probe`'s, copied, with each scenario's counter delta written as a `# tie` line
    (`patch_probe.py`). It adds `CollectOutreachersTest`'s region loads: the first square with data, its largest
    cache, the 3 by 3 block, modes 0 to 2;
  - `run.sh` runs the regions, then the routes, then the decodes.
- **Why the probe and not `jvmTest`:** `jvmTest` runs on the container's JDK, 21.0.12.1 (`java -version`; `executable` is
  set only for `e3Strata`, `brouter/build.gradle.kts:138`). Patching 21.0.13's `HashMap` into it would not be sound.
- **The region loads: 0.** 4 tiles times 3 modes, each 6 caches, and no key of any class tie-broken (`regions.txt`).
  Each of `CollectOutreachersTest`'s two tests makes these same 12 loads.
- **F0's two routes, on both folders: 0** (`routes.txt`). The routes reproduce E3's (226, 5047) and (306, 6713).
- **The decodes: not finished at the handoff.** 8,756 of 25,072 cache lines were copied while the run was still going
  (`decode-partial-at-handoff.txt`). So far there is **one tie-break site:**
  - W75_N40 (106, 130) sub 740, in the "known" scenario's seeding, `treeify` 19 times, `putTreeVal` 0, and no other key
    class. That is the equal-hash tree bin the second session's probe found there;
  - **the counter reads non-zero,** so the zeros above are not a dead counter.
- **Not yet known:**
  - the decode total;
  - whether the patched run's 25,072 cache lines equal jarA's. Check with
    `grep -E '^(W75_N40|W75_N35|W70_N40|E170_S50|F0) ' decode.txt | sha256sum`, which must read `282555f9…6685a`. That
    command reproduces jarA's digest from `jarA.txt` [C].

**Nothing else was built.** No `NodeHashMap`, no test, no `cleanupPeninsulas` change, and no revert run.

## Fourth session: the map written; the forward run red on 3; handed off
Base: `brouter-port-5b` at `7ce67e2`, equal to `origin/brouter-port-5b` after a fetch [C]. RECORD -50 and -53 read.

**Done, in `1a5a25f` (pushed):**
- `NodeHashMap` in three files, from the pinned `HashMap.java` (SHA-256 `71c8d822…` re-checked [C]), keys and values
  separate: `NodeHashMap.kt` (core, iterator, stand-in), `NodeHashMapTreeInsert.kt`, `NodeHashMapTreeRemove.kt`.
- The stand-in as -50 and -53 answer: `OsmNode.tieBreakId`, one counter per map (`tieBreakIdsAssigned`), and a named
  "unsupported" in `tieBreakOrder` when the classes differ. The guard has no test [unverified].
- **Calls beyond the handoff's design (for the planner to confirm):**
  - a second counter, `tieBreakCalls`, read only by the new test, so it can show ties in `treeifyBin`/`putTreeVal`, in
    `split`'s `treeify` and after the removes;
  - the JVM's "log when it fires" is one `System.err` line; the port has no logging facility;
  - `revert-runner.sh` takes an optional file, forward line and reverted line, with its defaults unchanged.
- `cleanupPeninsulas`: `expect`/`actual` `runCatchingStackOverflow` (new `jvmMain` and `iosMain` source sets; the first
  `actual`s in this module). The klibs were not compiled locally [unverified]; CI on `1a5a25f` was not read.
- `NodeHashMapTieBreakTest` in `commonTest`.
- Predictions pushed before any revert: `7-tiebreak-revert-prediction.md`, and an addendum to `4-revert-prediction.md`
  (its total was 10; its own per-class lines sum to 7).

**The forward run** (step 5), `:brouter:jvmTest --max-workers=2 --offline` from an emptied results folder, 6 min 10 s,
0 `e:` lines: **101 tests, 3 failures, 0 errors** [C: `8-forward-run-s4/`].
- **Passed:** `NodeHashMapTieBreakTest` (3 maps, 3,025 keys, 50 numbers given, tie-break calls 156, 156, 324, 369, so
  ties were seen at the group inserts, in `split` and after the removes); `GraphDecodeTest` 5 of 5, with the real map.
- **`CollectOutreachersTest`, 2 of 2 fail,** both at segments/W75_N40 around (105, 130) sub 735, 6 caches: mode 1
  `oracle [C 38715 41099 41099] port [C 39396 37387 37387]`, mode 2 `oracle [C 21909 41099 41099] port [C 22106 39455
  39455]`, each at line 1 of the transcript. **The port's catch fired 3 times** in that class (log lines 107, 108, 114).
  - Whether the oracle overflowed there too is [?]: its catch is silent.
  - **Not diagnosed; no fix tried.** [I], untested: on one thread, the port's recursion frame may be larger than the
    oracle's, so the port overflows where the oracle does not, or at another point. If so, RECORD -50's premise that
    the JVM catch "keeps logic runs equal" fails on this region. That is a question for the planner, not a fix to choose.
- **`NodeHashMapOrderTest` fails its own refusal assertion:** 8 of 12 seeds refused for an equal-hash tree bucket
  (seeds 1, 2, 3, 4, 7, 8, 9, 11; log lines 175-183). This test had never run past the stub. Its generator does not keep
  equal-hash tree buckets out. No order or answer mismatch was reported before a refusal [I: `runSeed` not re-read].
  Not touched: RECORD -50 says the order test "keeps refusing" these buckets, so changing the generator or the
  assertion is the planner's call.

**Not done:** both reverts, the tie-break count, CI, and closing the report. The third session's count run was stopped
with its session, incomplete (`8-forward-run-s4/tiebreak-stopped-run-s3.txt`).

## Handoff: what remains, in order (fourth session's, replacing the third's)
Clone `/home/user/forager-ios-5a`, branch `brouter-port-5b`. First check that `origin/brouter-port-5b` is the commit that
added this section. Scratch is `/tmp/claude-0/-home-user/c9851708-fbbe-5ce6-a5f8-ed278a238cce/scratchpad/5b/`.
1. **Read CI on that commit:** `jvm` (expect 101 with these 3 failures), both klibs' `e:` lines (the first `actual`s),
   the simulator count (24 with the new test, simulator-checked), and arm64 with its gate.
2. **Two questions go to the planner** (sent by the planner, from this report): the `CollectOutreachersTest` overflow
   and the order test's refusals, both above.
3. **Data before any fix, on the overflow:** re-run only `CollectOutreachersTest` with a larger test stack from a
   scratch init script, never committed (`allprojects { tasks.withType<Test>().configureEach { jvmArgs("-Xss1g") } }`,
   `./gradlew --max-workers=2 --offline -I <init> :brouter:jvmTest --tests '*CollectOutreachersTest*'`). Write the
   prediction first. Read whether the catch line still prints, and whether the oracle's `C` line changes from the
   values above (if it does, the oracle overflowed on the default stack too). That `-Xss` reaches the test worker
   thread is [I].
4. **The reverts,** only once the forward is green, or after their predictions are restated for a red forward:
   - `bash docs/audits/2026-10-30-brouter-port-step5b/revert-runner.sh /home/user/forager-ios-5a <scratch>/revert-dw`
     against `4-revert-prediction.md` (7 failures of 101);
   - `bash docs/audits/2026-10-30-brouter-port-step5b/revert-runner.sh /home/user/forager-ios-5a <scratch>/revert-tb
     brouter/src/commonMain/kotlin/com/zynergylabs/forager/brouter/mapaccess/NodeHashMap.kt
     'node.tieBreakId = tieBreakIdsAssigned' 'node.tieBreakId = kotlin.random.Random.nextInt(1, Int.MAX_VALUE)'`
     against `7-tiebreak-revert-prediction.md` (1 failure of 101);
   - after each, the runner's `restore.txt` must show `cmp` identical and the forward line, and `git diff --stat`
     must still show the forward change.
5. **The tie-break count:** move scratch `tiebreak/runs` aside, then rebuild and run as the third session's handoff,
   step 1, below, says. Report the totals, every `# tie` line, and the cache-line SHA-256 check.
6. **Close:** a section answering RECORD -50 point by point; mark this report done; push; tell the planner.

## Handoff: what remains, in order (third session's, superseded by the fourth's above)
A fresh session reads this handoff, RECORD -50, and the files named here. It works in clone `/home/user/forager-ios-5a`,
branch `brouter-port-5b`, and first checks that `origin/brouter-port-5b` is the commit that added this section.
Scratch is `/tmp/claude-0/-home-user/c9851708-fbbe-5ce6-a5f8-ed278a238cce/scratchpad/5b/`; check it before relying on
anything there.

1. **Finish the `tieBreakOrder` count.**
   - If `<scratch>/5b/tiebreak/runs/status.txt` reads `decode exit 0` and `done`, copy `runs/decode.txt`, without its
     `# classpath` line, to `6-tiebreak/decode.txt`.
   - Otherwise rebuild and re-run from `6-tiebreak/`:
     - copy the pinned `HashMap.java` (`<scratch>/5b/jdksrc/java.base/java/util/`) to `tiebreak/hm/java/util/`;
     - run `patch_hashmap.py` on it;
     - `javac --patch-module java.base=hm -d out/patch` it;
     - run `patch_probe.py` on a copy of `5-probe/src/ProbeMain.java`;
     - `javac -cp <oracle jar>` it, with `Probe.java`, into `out/probe`;
     - `run.sh all`. It takes about 7 minutes, single-threaded.
   - Report:
     - the `# total` and `# grand total` lines;
     - every `# tie` line;
     - the cache-line SHA-256 check above.
2. **Write `NodeHashMap`** as RECORD -50 answers, replacing the stub `mapaccess/NodeHashMap.kt`. The design worked out in
   this session, ready to type:
   - **Three files:**
     - `NodeHashMap.kt`: constants, `hash`, `tableSizeFor`, the constructor, `get`/`getNode`, `put`/`putVal`, `resize`,
       `treeifyBin`, `remove`/`removeNode`, the value iterator, the nested `Node` and `TreeNode` classes, and
       `tieBreakOrder` with its stand-in;
     - `NodeHashMapTreeInsert.kt`: `root`, `moveRootToFront`, `find`, `getTreeNode`, `treeify`, `putTreeVal`,
       `balanceInsertion`, `rotateLeft`, `rotateRight`, as extension or top-level functions;
     - `NodeHashMapTreeRemove.kt`: `untreeify`, `removeTreeNode`, `balanceDeletion`, `split`.
   - `DEFAULT_INITIAL_CAPACITY` (`HashMap.java:238`) is needed by `resize`. The sizing's table omits it.
   - **`comparableClassFor` is always null** (`OsmNode` is not `Comparable`), so in `treeify` and `putTreeVal` the
     equal-hash branch goes straight to `tieBreakOrder` (after `putTreeVal`'s one-time child search), and `find` goes
     straight to `pr.find(...) ?: pl`.
   - `treeify` and `split` take the map, which Java's do not, so they can reach the stand-in's counter.
   - **The stand-in (RECORD -50):**
     - a field on the port's `OsmNode`, `var tieBreakId: Int = 0`, and one counter per map;
     - `identityStandIn(node)`: if `tieBreakId` is 0, increment the map's `tieBreakIdsAssigned` and assign it;
     - `tieBreakOrder(a, b) = if (identityStandIn(a) <= identityStandIn(b)) -1 else 1`. `a` is evaluated before `b`, as
       Java's `:2063`.
     - **Why on the node, not the entry:** HotSpot's identity hash belongs to the object. `untreeify`, `treeifyBin` and
       `split` replace entries, and the number must survive that.
     - Put the reasoning comment at the site, citing RECORD -50.
   - **A call for the planner, not yet made:** a guard in `tieBreakOrder` that throws
     `UnsupportedOperationException("unsupported: ...")` (the port's form, e.g. `JavaLang.kt:56`) when
     `a::class != b::class`. That is the only case where Java's class-name step would order differently. It never fires
     on today's callers.
3. **The new test** goes in `commonTest`, so it runs on the JVM and the simulator. It needs one equal-hash tree bin of
   about 30 or more nodes on one anti-diagonal (`ilon + ilat` constant), enough ordinary keys to force a resize (and so
   `split` and re-`treeify`), and some removes.
   - It builds three or more fresh maps, each from fresh `OsmNode` objects, with the same inserts, and asserts:
     - every map's full `valuesIterator()` order equals the first's;
     - `tieBreakIdsAssigned > 0` in each, so it saw a tie.
   - **The revert, one line:** `node.tieBreakId = tieBreakIdsAssigned` becomes
     `node.tieBreakId = kotlin.random.Random.nextInt(1, Int.MAX_VALUE)`. That is an arbitrary value fixed at first
     request, as HotSpot's is.
   - Keep the counter's increment on its own line, so the coverage assertion still passes and the failure is the order's.
   - Write and push the prediction before the run, with its odds of a false pass estimated.
4. **`cleanupPeninsulas`** (RECORD -50):
   - on the JVM, keep Java's `StackOverflowError` catch (`OsmNodesMap.java:62-64`) and log when it fires;
   - on iOS there is no catch, recorded as a known difference;
   - this needs a small `expect`/`actual`. The comment at `OsmNodesMap.kt:13-16` and `:64` changes with it.
5. **Run `:brouter:jvmTest --max-workers=2 --offline`** from an emptied `brouter/build/test-results/jvmTest`. Expect
   100 tests plus the new one, 0 failures, counted from the XML.
6. **The `DirectWeaver` revert:**
   - run `bash docs/audits/2026-10-30-brouter-port-step5b/revert-runner.sh /home/user/forager-ios-5a <scratch-out>`;
   - compare its result with `4-revert-prediction.md`: 10 failures, now plus 0 from the new test, which decodes no tile;
   - then the new test's revert, the same way.
   - The runner reads the compile log first, restores from a saved copy, and checks with `cmp` and the forward line.
7. **Read CI** on the final push:
   - `jvm`'s count and job time. Propose a split if it is over 20 of its 30 minutes;
   - both klibs' `e:` lines;
   - the simulator count, now 24 if the new test is in `commonTest` (simulator-checked);
   - arm64 with its gate line.
8. **Mark this report done,** with a section answering RECORD -50 point by point. Push, and tell the planner the branch
   is ready.

## Handoff of the second session (superseded by the one above)
1. **Read the planner's answer to the sizing** (RECORD, or its message). It decides `NodeHashMap` and, in particular,
   what the port does at `tieBreakOrder`, which the test's "known" seeding reaches once in W75_N40 (above).
2. **Write `NodeHashMap` as answered**, replacing the stub in `mapaccess/NodeHashMap.kt`, in three files under about
   400 lines each (sizing above). Keys and values stay separate (`HashMap.java:660-667`).
3. **Run `:brouter:jvmTest --max-workers=2`** from an emptied `brouter/build/test-results/jvmTest`. Expect 100 tests and
   0 failures. Read the counts from the XML, and check each 5b test's printed coverage line: the order test must have
   seen every kind it asserts. A decode difference names its tile, cache, scenario and first differing line.
4. **Run the revert:** `bash docs/audits/2026-10-30-brouter-port-step5b/revert-runner.sh <repo> <scratch-dir>`. It saves
   `DirectWeaver.kt`, inverts `:165`, refuses on any compile error, restores from the saved copy and checks it with
   `cmp`. Compare its failures with `4-revert-prediction.md` (10 failures, each at an `L` line differing only in the
   reverse flag). Copy its outputs into the evidence folder, and push.
5. **Read CI on that push:** `jvm`'s count and time, both klibs' `e:` lines, the simulator count, and the arm64 gate.
6. **Set this report's status to done**, and tell the planner the branch is ready to merge into `brouter-port`.
