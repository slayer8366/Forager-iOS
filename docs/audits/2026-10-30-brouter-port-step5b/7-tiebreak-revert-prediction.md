# Step 5b, fourth session: the tie-break stand-in's revert, predicted before any run

RECORD -50: the new test "is seen failing by a revert of the stand-in". Written and pushed before the run.

**The edit (one line),** `NodeHashMap.kt`, in `identityStandIn`:

    node.tieBreakId = tieBreakIdsAssigned   ->   node.tieBreakId = kotlin.random.Random.nextInt(1, Int.MAX_VALUE)

That is an arbitrary value fixed at first request, as HotSpot's identity hash is: the reverted port behaves as the
oracle does, one order per run. The counter's increment is on the line above, untouched, so `tieBreakIdsAssigned`
still counts and the test's coverage assertions still pass. The failure must come from the order.

**Mechanism:** with random numbers, the 32 equal-hash keys of step 1 rank differently in each of the test's three
maps. `treeify` places the first 9 by rank and `moveRootToFront` puts the tree's root first in the bin's chain;
`putTreeVal` then links each of the next 23 directly after its tree parent (`HashMap.java:2169`), which depends on its
rank. So the bin's chain, and with it `valuesIterator()`'s order, differs between maps.

**Predicted result:**
- the build log has 0 compile errors (`kotlin.random.Random` is common stdlib);
- the results folder is emptied before the run;
- **`NodeHashMapTieBreakTest`: 1 of 1 fails,** an assertion failure whose message begins "map 1's order after the tie
  group differs from map 0's" (the first order assertion; the coverage assertions before it pass);
- **every other test passes, 100 of 100.** None of them reaches `tieBreakOrder`:
  - `NodeHashMapOrderTest` refuses equal-hash tree buckets, and asserts it refused none;
  - `CollectOutreachersTest`'s 12 region loads made 0 tie-breaks on the patched oracle (`6-tiebreak/regions.txt`);
  - `GraphDecodeTest`'s transcript never depends on `hmap`'s order (`GraphTranscript.kt:8-9`). It does decode W75_N40
    (106, 130) sub 740, where the oracle tie-breaks 19 times, but the text is sorted by position;
- **so 1 failure of 101.**

**Odds of a false pass, estimated [I]:** the revert passes only if all three maps give one order at all three
checkpoints with independent random numbers. Already at the first checkpoint, each of the 23 `putTreeVal` inserts
lands after a parent chosen by its random rank among the keys before it, so two maps agreeing on all of them is far
below 1 in a million. Not measured.

A failure of any other shape is not this revert's, and the result is not cited.

**Restore:** from the copy saved before the edit, never from git. Afterwards `cmp` against the saved copy and a `grep`
for the forward line.
