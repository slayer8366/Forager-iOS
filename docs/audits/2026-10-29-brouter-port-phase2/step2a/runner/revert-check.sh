#!/bin/bash
# Step 2a, sub-step 3: the revert check at SortedHeap.kt:112, the port of SortedHeap.java:139 (`<` to `<=`).
# Runner rules (Forager CLAUDE.md, Testing): restore from a copy saved before editing, never from git; refuse results when
# the build log has compile errors (run-jvmtest.sh deletes the old XML first and refuses); confirm the forward change after.
set -u
here=$(cd "$(dirname "$0")" && pwd); out=$1
F=brouter/src/commonMain/kotlin/com/zynergylabs/forager/brouter/util/SortedHeap.kt
cd /home/user/forager-ios
{
echo "# HEAD $(git rev-parse --short HEAD); tree clean before: $( [ -z "$(git status --porcelain)" ] && echo yes || echo NO)"
cp "$F" "$here/saved-SortedHeap.kt"
echo "# saved copy: $(sha256sum < "$here/saved-SortedHeap.kt" | cut -c1-16)"
echo "# lines 109-112 before:"; sed -n '109,112p' "$F"
sed -i '112s/if (bin.lv < minBin.lv) minBin = bin/if (bin.lv <= minBin.lv) minBin = bin/' "$F"
echo "# the edit (diff saved -> reverted):"; diff "$here/saved-SortedHeap.kt" "$F"
echo "# lines differing: $(diff "$here/saved-SortedHeap.kt" "$F" | grep -c '^<')"
} > "$out.head"
"$here/run-jvmtest.sh" "$out.body" "# Step 2a, sub-step 3: revert check, local, $(date -u +%FT%TZ): SortedHeap.kt:112 (SortedHeap.java:139) < to <=; the 24 tests that can finish (runner/filter24.txt)" $(cat "$here/filter24.txt")
rc=$?
cp "$here/saved-SortedHeap.kt" "$F"
{
echo
echo "# summarize exit: $rc (2 = refused: compile errors or no XML)"
echo "# restored from the saved copy; cmp saved vs file: $(cmp -s "$here/saved-SortedHeap.kt" "$F" && echo identical || echo DIFFERENT)"
echo "# file vs HEAD (the forward change is committed): $(git show HEAD:"$F" | cmp -s - "$F" && echo identical || echo DIFFERENT)"
echo "# git status after: $( [ -z "$(git status --porcelain)" ] && echo clean || git status --porcelain | tr '\n' ' ')"
echo "# line 112 after: $(sed -n '112p' "$F")"
} > "$out.tail"
cat "$out.head" "$out.body" "$out.tail" > "$out"; rm -f "$out.head" "$out.body" "$out.tail"
