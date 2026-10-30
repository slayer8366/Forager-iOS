#!/bin/bash
# Usage: revert-check.sh <name> <file> <old-line-text> <new-line-text>
# Step 3's revert checks (RECORD -38). Runner rules (Forager CLAUDE.md, Testing): the prediction is committed before the
# run (<name>-prediction.txt); the file is saved to a copy before the edit and restored from that copy, never from git;
# the old JUnit XML is deleted first, and summarize.py refuses any result when the build log has compile errors; the
# forward change is confirmed present after (the file equals HEAD's, where it is committed).
set -u
here=$(cd "$(dirname "$0")" && pwd); name=$1; F=$2; old=$3; new=$4
out="$here/../4-revert-$name.txt"; log="$here/../4-revert-$name-jvmTest.log"; saved="$here/saved-$name"
cd /home/user/forager-ios
{
echo "# Step 3, revert check '$name', local, $(date -u +%FT%TZ)"
echo "# HEAD $(git rev-parse --short HEAD); tree clean before: $( [ -z "$(git status --porcelain)" ] && echo yes || echo NO)"
echo "# prediction, committed before this run: $(git log -1 --format=%h -- "$here/../4-revert-$name-prediction.txt")"
cp "$F" "$saved"
echo "# saved copy of $F: sha256 $(sha256sum < "$saved" | cut -c1-16)"
python3 -I - "$F" "$old" "$new" <<'PY'
import sys
f, old, new = sys.argv[1:4]
s = open(f).read()
n = s.count(old)
print("# occurrences of the old text: %d" % n)
if n != 1: sys.exit(3)
open(f, 'w').write(s.replace(old, new))
PY
erc=$?
echo "# the edit (diff saved -> reverted):"; diff "$saved" "$F"
echo "# lines differing: $(diff "$saved" "$F" | grep -c '^<')"
} > "$out.head"
if [ $erc -ne 0 ]; then echo "edit refused"; cat "$out.head"; rm -f "$out.head" "$saved"; exit 3; fi
rm -rf brouter/build/test-results/jvmTest
./gradlew :brouter:jvmTest -x msunReference --console=plain --tests 'com.zynergylabs.forager.brouter.codec.*' 2>&1 | grep -v 'JAVA_TOOL_OPTIONS' > "$log"
echo "gradle exit: ${PIPESTATUS[0]}" >> "$log"
python3 -I "$here/summarize.py" brouter/build/test-results/jvmTest "$log" "# summary (JUnit XML)" > "$out.body"
rc=$?
cp "$saved" "$F"
{
echo
echo "# summarize exit: $rc (2 = refused: compile errors or no XML)"
echo "# restored from the saved copy; cmp saved vs file: $(cmp -s "$saved" "$F" && echo identical || echo DIFFERENT)"
echo "# file vs HEAD (the forward change is committed): $(git show HEAD:"$F" | cmp -s - "$F" && echo identical || echo DIFFERENT)"
echo "# forward line present after: $(grep -cF -- "$old" "$F") occurrence(s)"
echo "# git status after: $( [ -z "$(git status --porcelain -- ':!docs')" ] && echo 'clean outside docs/' || git status --porcelain | tr '\n' ' ')"
} > "$out.tail"
cat "$out.head" "$out.body" "$out.tail" > "$out"; rm -f "$out.head" "$out.body" "$out.tail" "$saved"
cat "$out"
