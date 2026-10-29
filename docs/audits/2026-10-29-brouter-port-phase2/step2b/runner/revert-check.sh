#!/bin/bash
# Step 2b: the revert check. k_cos.c's C1 raised by one ulp (0x3FA555555555554C to ...4D) in MsunCos.kt, one line.
# Prediction, made before the run by building the same edit in bionic's C (predict-C1.txt): the table's cos differs at rows
# 502, 1297 and 1318 only; no edge line changes; the StrictMath-difference rows become the other 10 of the 13. So exactly
# three tests fail: MsunCosTableTest.cosEqualsBionicAtEveryTableRow and MsunAgainstCTest.cosEqualsTheCBuildRowByRow
# (rows 502, 1297, 1318), and MsunAgainstCTest.cosDiffersFromStrictMathAtTheResearchsThirteenRows.
# Runner rules (Forager CLAUDE.md, Testing): restore from a copy saved before editing, never from git; refuse results when
# the build log has compile errors (run-jvmtest.sh deletes the old XML first and summarize.py refuses); confirm the
# forward change after.
set -u
# Usage: revert-check.sh <absolute out-file>. A relative path lands beside the repository (it did once, and was moved).
here=$(cd "$(dirname "$0")" && pwd); out=$1
F=brouter/src/commonMain/kotlin/com/zynergylabs/forager/brouter/msun/MsunCos.kt
cd /home/user/forager-ios
{
echo "# HEAD $(git rev-parse --short HEAD); tree clean before, outside this runner's folder: $( [ -z "$(git status --porcelain -- . ':!docs/audits/2026-10-29-brouter-port-phase2/step2b')" ] && echo yes || echo NO)"
cp "$F" "$here/saved-MsunCos.kt"
echo "# saved copy: $(sha256sum < "$here/saved-MsunCos.kt" | cut -c1-16)"
sed -i 's|^private const val C1 = 4.16666666666666019037e-02 /\* 0x3FA55555, 0x5555554C \*/$|private val C1 = Double.fromBits(0x3FA555555555554DL) /* REVERT CHECK: one ulp up */|' "$F"
echo "# the edit (diff saved -> reverted):"; diff "$here/saved-MsunCos.kt" "$F"
echo "# lines differing: $(diff "$here/saved-MsunCos.kt" "$F" | grep -c '^<')"
} > "$out.head"
"$here/run-jvmtest.sh" "$out.body" "# Step 2b revert check, local, $(date -u +%FT%TZ): MsunCos.kt C1 one ulp up; the msun classes" --tests 'com.zynergylabs.forager.brouter.msun.*'
rc=$?
cp "$here/saved-MsunCos.kt" "$F"
{
echo
echo "# run-jvmtest exit: $rc"
echo "# restored from the saved copy; cmp saved vs file: $(cmp -s "$here/saved-MsunCos.kt" "$F" && echo identical || echo DIFFERENT)"
echo "# file vs HEAD (the forward change is committed): $(git show HEAD:"$F" | cmp -s - "$F" && echo identical || echo DIFFERENT)"
echo "# C1 line after: $(grep -n '^private const val C1' "$F")"
} > "$out.tail"
cat "$out.head" "$out.body" "$out.tail" > "$out"; rm -f "$out.head" "$out.body" "$out.tail" "$here/saved-MsunCos.kt"
