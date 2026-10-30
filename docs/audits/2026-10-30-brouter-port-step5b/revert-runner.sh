#!/usr/bin/env bash
# Step 5b's revert runner (dispatch 2026-10-28-01, item I), under Forager's CLAUDE.md runner rules:
# - the file is copied aside before the edit, and restored from that copy, never from git;
# - the results folder is emptied before the run, so no earlier XML can be read as this run's;
# - the build log is read for compile errors before any result, and a run with any is refused;
# - afterwards the restored file must equal the saved copy byte for byte and hold the forward line.
#
#   revert-runner.sh <repo> <out-dir> [<file> <forward line> <reverted line>]
#
# With three arguments more, it reverts that line of that file instead of DirectWeaver's (fourth session: the
# tie-break stand-in's revert, 7-tiebreak-revert-prediction.md). The rules above are the same for both.
set -uo pipefail

repo=${1:?usage: revert-runner.sh <repo> <out-dir> [<file> <forward> <reverted>]}
out=${2:?usage: revert-runner.sh <repo> <out-dir> [<file> <forward> <reverted>]}
file=${3:-brouter/src/commonMain/kotlin/com/zynergylabs/forager/brouter/mapaccess/DirectWeaver.kt}
forward=${4:-'node.addLink(link, isReverse, node2)'}
reverted=${5:-'node.addLink(link, !isReverse, node2)'}

cd "$repo"
mkdir -p "$out"
saved="$out/$(basename "$file").saved"
cp "$file" "$saved"
[ "$(grep -c -F "$forward" "$file")" = 1 ] || { echo "REFUSED: the forward line is not in $file exactly once"; exit 1; }

# The one edit.
python3 -I - "$file" "$forward" "$reverted" <<'EOF'
import sys
p, a, b = sys.argv[1:4]
s = open(p).read()
assert s.count(a) == 1
open(p, 'w').write(s.replace(a, b))
EOF
diff "$saved" "$file" > "$out/revert.diff"

rm -rf brouter/build/test-results/jvmTest
./gradlew --max-workers=2 --offline :brouter:jvmTest --continue > "$out/revert-build.log" 2>&1
echo "gradle exit $?" >> "$out/revert-build.log"

# Restore first, whatever the run did: from the saved copy.
cp "$saved" "$file"
cmp "$saved" "$file" && echo "restored: identical to the saved copy" > "$out/restore.txt"
grep -n -F "$forward" "$file" >> "$out/restore.txt" || echo "FORWARD LINE MISSING" >> "$out/restore.txt"

errors=$(grep -c '^e: ' "$out/revert-build.log")
echo "compile errors in the revert's build log: $errors" > "$out/revert-result.txt"
if [ "$errors" != 0 ]; then
  echo "REFUSED: the reverted build did not compile; no result is read" >> "$out/revert-result.txt"
  exit 1
fi
python3 -I tools/junit-counts.py brouter/build/test-results/jvmTest | grep TOTAL >> "$out/revert-result.txt"
python3 -I - brouter/build/test-results/jvmTest >> "$out/revert-result.txt" <<'EOF'
import glob, sys, xml.etree.ElementTree as ET
for f in sorted(glob.glob(sys.argv[1] + '/TEST-*.xml')):
    t = ET.parse(f).getroot()
    for tc in t.findall('testcase'):
        fl = tc.find('failure')
        if fl is not None:
            print(f"FAILED {t.get('name').split('.')[-1]}.{tc.get('name')}: {fl.get('type')}: {(fl.get('message') or '')[:400]}")
EOF
cat "$out/revert-result.txt" "$out/restore.txt"
