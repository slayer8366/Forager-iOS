# Step 5b, fifth session: B's redraw, and the revert of its filter, predicted before any run

RECORD -59, B: a draw holding an equal-hash tree bucket is discarded and drawn again, up to 20 draws per seed, every
redraw counted; order equality is asserted on every accepted draw; a seed with no draw accepted within its cap fails.
Written and pushed before the forward run and before the revert.

**The change** (`NodeHashMapOrderTest.kt`): `runSeed` becomes `runDraw(seed, draw, r)`, which returns null for a refused
draw. Each seed keeps one `Random(seed)`; a redraw carries on from where the refused draw left the stream, on fresh
maps. Draw 1 of every seed is therefore the same draw the fourth session ran.

**Forward, predicted:**
- 0 compile errors;
- `NodeHashMapOrderTest` passes. Seeds 5, 6, 10 and 12 are accepted at draw 1 (they were not refused in the fourth
  session's run, `8-forward-run-s4/forward-log-excerpt.txt:175-182`). Seeds 1, 2, 3, 4, 7, 8, 9 and 11 are refused at
  draw 1 at the same ops as then (9213, 15389, 21335, 9087, 13792, 17160, 20611, 7063), so redraws are at least 8.
  How many more is not predicted: about 8 of 12 first draws were refused, so a draw is accepted with odds near 1 in 3
  and each refused seed needs a few draws [I]. A seed refused 20 times running would be about 0.67^20, under 1 in 1,000
  per seed [I];
- the whole class: `jvmTest` 101 tests, **2 failures**, both `CollectOutreachersTest`, with the fourth session's
  messages (A, untouched here).

**The revert (one line),** in `runDraw`:

    if (refused) return null   ->   if (false) return null

The refusal is still detected and printed (the line above it is untouched), but the draw is no longer discarded.

**Mechanism:** with the filter gone, seeds 1, 2, 3, 4, 7, 8, 9 and 11 run on past their equal-hash tree buckets. There
`treeify` and `putTreeVal` break the tie on `System.identityHashCode` in the reference (`HashMap.java:2063`) and on the
stand-in's counter in the port, and the two orders differ wherever the tie decides the tree's root (which
`moveRootToFront` puts first in the bin's chain) or the parent a new node is linked after (`HashMap.java:2169`).

**Predicted result:**
- 0 compile errors; results folder emptied first;
- **`NodeHashMapOrderTest`: 1 of 1 fails, on order:** a message beginning `seed N draw 1 op M: iteration order` (or
  `seed N draw 1 end: iteration order`), with N one of 1, 2, 3, 4, 7, 8, 9, 11 and M a multiple of 200 at or after that
  seed's refusal op above. Most likely N = 1, at op 9400 or soon after [I]. Redraws are 0 (every draw 1 is accepted).
- a `put`, `get` or `remove` mismatch is not predicted: answers go by `equals`, which ties do not touch;
- every other test is as forward: `CollectOutreachersTest` 2 of 2 fail with the forward's messages, the rest pass;
- **so 3 failures of 101.**

**Odds of a false pass [I], not measured, and not small enough to wave off:** a two-way tie at a leaf parent links the
new node after that parent whichever way it goes, so some ties leave the chain unchanged. The order differs only where
a tie moves the root or the parent. Eight seeds run on with ties, each for thousands of ops and through resizes (which
re-`treeify` every tree bin), so at least one visible difference is likely; a pass would itself be a finding (that the
filter guards nothing on these seeds), and would be reported, not counted as the filter's evidence.

A failure of any other shape is not this revert's, and the result is not cited.

**Restore:** by `revert-runner.sh` from its saved copy, never from git; `restore.txt` (`cmp`, the forward line) and
`git diff --stat` read after it.
