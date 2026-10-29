#!/bin/bash
# Step 2a close: the one-flipped-bit check on the port's input, for one case of BitCoderContextTest's raw-read tests.
# Usage: bite-check.sh <out-file> <line> <label>. Edits one line: that case's `val k = BitCoderContext(bytes)` gets a copy
# of the bytes with bit 0 of byte 100 flipped (as PortLoopCheckBite did). Runner rules (Forager CLAUDE.md, Testing): restore
# from a copy saved before editing, never from git; refuse results when the build log has compile errors (run-jvmtest.sh
# deletes the old XML first and refuses); confirm the forward change after.
set -u
here=$(cd "$(dirname "$0")" && pwd); out=$1; line=$2; label=$3; tmp="$here/tmp-$(basename "$out")"
F=brouter/src/jvmTest/kotlin/com/zynergylabs/forager/brouter/util/BitCoderContextTest.kt
cd /home/user/forager-ios
{
echo "# HEAD $(git rev-parse --short HEAD); tree clean before: $( [ -z "$(git status --porcelain)" ] && echo yes || echo NO)"
cp "$F" "$here/saved-BitCoderContextTest.kt"
echo "# saved copy: $(sha256sum < "$here/saved-BitCoderContextTest.kt" | cut -c1-16)"
echo "# line $line before: $(sed -n "${line}p" "$F")"
sed -i "${line}s/val k = BitCoderContext(bytes)\$/val k = BitCoderContext(bytes.copyOf().also { it[100] = (it[100].toInt() xor 1).toByte() })/" "$F"
echo "# the edit (diff saved -> bitten):"; diff "$here/saved-BitCoderContextTest.kt" "$F"
echo "# lines differing: $(diff "$here/saved-BitCoderContextTest.kt" "$F" | grep -c '^<')"
} > "$tmp.head"
"$here/run-jvmtest.sh" "$tmp.body" "# Step 2a close: bite check $label, local, $(date -u +%FT%TZ): BitCoderContextTest.kt:$line, bit 0 of byte 100 flipped in the port's copy of the input; full run, no filter"
rc=$?
cp "$here/saved-BitCoderContextTest.kt" "$F"
{
echo
echo "# summarize exit: $rc (2 = refused: compile errors or no XML)"
echo "# restored from the saved copy; cmp saved vs file: $(cmp -s "$here/saved-BitCoderContextTest.kt" "$F" && echo identical || echo DIFFERENT)"
echo "# file vs HEAD (the forward change is committed): $(git show HEAD:"$F" | cmp -s - "$F" && echo identical || echo DIFFERENT)"
echo "# git status after: $( [ -z "$(git status --porcelain)" ] && echo clean || git status --porcelain | tr '\n' ' ')"
echo "# forward change present after: op cap line $(grep -c 'i < 1_000_000' "$F"), second case $(grep -c 'fun rawBitReadsWithoutResetsToThePositionBoundMatchTheOracle' "$F")"
} > "$tmp.tail"
cat "$tmp.head" "$tmp.body" "$tmp.tail" > "$out"; rm -f "$tmp.head" "$tmp.body" "$tmp.tail"
