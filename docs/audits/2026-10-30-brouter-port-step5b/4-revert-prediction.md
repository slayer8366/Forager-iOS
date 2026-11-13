# Step 5b revert: the prediction, written and pushed before any run

Item I's candidate for 5b: "one link direction flag inverted in `DirectWeaver`".

**The edit (one token):** `DirectWeaver.kt:165`, the internal-link weave, Java's `DirectWeaver.java:161`:

    node.addLink(link, isReverse, node2)   ->   node.addLink(link, !isReverse, node2)

**Why this flag:** for an internal link `isReverse` is always `false` (it is set only on the external branch,
`DirectWeaver.java:104`). Inverting it here changes the woven direction only. It does not change which bits are read,
so the stream stays aligned, the CRC check in `OsmFile` stays quiet, and the failure must come from the digest, not
from a decoding crash. (Inverting the decoded bit at `:104` instead would misalign the stream and fail as a checksum
error, which tests alignment, not direction.)

**Mechanism:** `addLink(link, true, node2)` sets `n1 = node2`, `n2 = node` (`OsmNode.java:85-91`). From `node`'s side,
`getTarget` still gives `node2` and `getNext` still gives the old first link, so the target and the chain order are
unchanged. `isReverse(node)` becomes `true`, where the oracle has `false`, and the opposite holds from `node2`'s side.
`GeometryDecoder` is called with the flipped direction, so the `G` line under that link differs as well.

**Predicted result, run when the map exists** (the decode tests cannot run past `NodeHashMap`'s stub until then):
- the build log has 0 compile errors;
- the results folder is emptied before the run;
- **`GraphDecodeTest`: 5 of 5 fail.** Each fails at its tile's first cache holding an internal link with way tags, in
  scenario `known` (the first compared). The first differing line is an `L` line with the same target coordinates in
  both texts, differing only in the fourth field, the reverse flag: `false` against `true`, one way or the other;
- **`CollectOutreachersTest`: 2 of 2 fail,** on a text difference at an `L` line in the same way;
- **every other test passes,** `NodeHashMapOrderTest` and `OsmNodePairSetTest` among them, since neither decodes a
  tile;
- **so 10 failures, if the planner's answer leaves the test count at 100.**

A failure of any other shape is not this revert's, and the result is not cited.

**Restore:** from a copy of `DirectWeaver.kt` saved before the edit, never from git. Afterwards `cmp` against the
saved copy, a `grep` for the forward line, and a forward re-run.

## Addendum, fourth session, before the run
**The total above is wrong by its own lines.** The per-class predictions sum to 7 (`GraphDecodeTest` 5,
`CollectOutreachersTest` 2), not 10. The "10" is an arithmetic slip of the second session's, carried into both
handoffs. The per-class lines are the prediction; its total is **7 failures of 101**, with RECORD -50's new test in
`commonTest`, which decodes no tile and so passes. Nothing else in the prediction changes.

## Addendum, fifth session: restated for a red forward, before the run (RECORD -59)
The forward is red on `CollectOutreachersTest` (`10-forward-run-s5/`): test 1 at `:141`, mode 1 of segments/W75_N40,
where the port's catch fires and the oracle's does not (`12-question-a/`); test 2 at a region or at its own `:116`
coverage assertion, varying between runs. So that class's failures count toward the revert only where they are this
revert's own:
- **`GraphDecodeTest`: 5 of 5 fail,** as above (an `L` line, the reverse flag);
- **`CollectOutreachersTest`: 2 of 2 fail at segments/W75_N40 mode 0,** the first region each test compares, on an `L`
  line differing only in its fourth field. Mode 0 runs no peninsula cleanup (`OsmNodesMap.java:38-39`, `justCount`), so
  no overflow precedes it, and the forward never fails at mode 0. A failure of this class at mode 1 or 2, or at `:116`,
  is the forward's, not the revert's, and is not counted as confirming it;
- **every other test passes** (`NodeHashMapOrderTest` and `NodeHashMapTieBreakTest` decode no tile);
- **so 7 failures of 101**, the same total, now with every one of the 7 required to carry the revert's own message.
