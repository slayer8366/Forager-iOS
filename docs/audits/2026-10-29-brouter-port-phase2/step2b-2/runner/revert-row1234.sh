#!/bin/bash
# Step 2b second part, the revert check. Restores from a copy saved before editing, never from git; refuses results on
# compile errors (run-jvmtest.sh, summarize.py); confirms the forward change after. Usage: revert-row1234.sh <absolute out>
set -u
here=$(cd "$(dirname "$0")" && pwd); out=$1
F=brouter/src/commonMain/kotlin/com/zynergylabs/forager/brouter/util/LibmSeam.kt
cd /home/user/forager-ios
cp "$F" "$here/saved-LibmSeam.kt"
sed -i 's|^    private val table: Array<DoubleArray> = CheapRuler.buildScaleTable(::msunCos)$|    private val table: Array<DoubleArray> = CheapRuler.buildScaleTable { x -> msunCos(x).let { c -> if (x.toRawBits() == 0x3fe2ae97d5ad8ebdL) Double.fromBits(c.toRawBits() + 1) else c } } // REVERT CHECK|' "$F"
{ echo "# HEAD $(git rev-parse --short HEAD)"; echo "# the edit:"; diff "$here/saved-LibmSeam.kt" "$F"; echo "# lines differing: $(diff "$here/saved-LibmSeam.kt" "$F" | grep -c '^<')"; } > "$out.head"
"$here/run-jvmtest.sh" "$out.body" "# Step 2b second part revert check, local, $(date -u +%FT%TZ): row 1234's cos one ulp up in MsunLibm; all of jvmTest"
rc=$?
cp "$here/saved-LibmSeam.kt" "$F"
{ echo; echo "# run-jvmtest exit: $rc"; echo "# restored from the saved copy: $(cmp -s "$here/saved-LibmSeam.kt" "$F" && echo identical || echo DIFFERENT)"; echo "# file vs HEAD (forward change committed): $(git show HEAD:"$F" | cmp -s - "$F" && echo identical || echo DIFFERENT)"; } > "$out.tail"
cat "$out.head" "$out.body" "$out.tail" > "$out"; rm -f "$out.head" "$out.body" "$out.tail" "$here/saved-LibmSeam.kt"
