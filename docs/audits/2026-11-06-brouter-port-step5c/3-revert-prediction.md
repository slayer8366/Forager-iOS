# 5c revert prediction (written and pushed before the revert runs)

**The edit (one line):** in `brouter/src/commonMain/.../mapaccess/WaypointMatcherImpl.kt`, the comparator's second line,
`javaDoubleCompare(mw1.directionDiff, mw2.directionDiff)` (Java's `WaypointMatcherImpl.java:71`), becomes `0`. The
comparator then orders by radius only, and `sortWith`, stable like `Collections.sort`, keeps insertion order on a tie.

**Mechanism:** `end()` inserts each segment's forward entry (`_w_`) and then its reverse (`_w2_`) with the same radius,
so every matched end has at least one radius tie (2,718 ends with a tie at the head, `2-forward.txt`). Reverted, a
forward entry always stays ahead of its own reverse, so `wayNearest[0]` can never be a `_w2_` entry, and the node pair
the port chooses is swapped wherever the oracle chose the reverse.

**Predicted, on the full `jvmTest` run by the runner:**
- 0 compile errors in the reverted build;
- `WaypointMatchTest` fails, its one assertion "ends differing from the oracle: N", with **1,364 <= N <= 2,718**:
  at least the 1,364 ends where the oracle chose the reverse entry (their n1/n2, name and diff differ), at most every
  matched end (3,068 less 350 unmatched). The 350 unmatched ends cannot differ. My point estimate is near the top of
  the range, because the whole `wayNearest` list is compared and its order moves whenever any tied pair in it had the
  reverse first [I];
- the first-ten list names fields `n1`, `n2`, `name`, `diff` (and the `|` list fields);
- the other failures are exactly `CollectOutreachersTest`'s two (RECORD -61); nothing else fails;
- the file restored from the runner's saved copy, equal to it byte for byte, forward line present.
