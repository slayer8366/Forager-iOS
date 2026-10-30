#!/usr/bin/env bash
# Revert runner (CLAUDE.md runner rules): revert.sh <file> <old-line-text> <new-line-text> <label>
# Saves a copy before editing and restores from it (never from git); deletes the results folder before the run; refuses
# to cite results when the build log has compile errors; after restoring, confirms the file equals the saved copy and
# the tracked tree matches HEAD for that file.
set -u
cd /home/user/forager-ios
F="$1"; OLD="$2"; NEW="$3"; L="$4"
S=/tmp/claude-0/-home-user/c9851708-fbbe-5ce6-a5f8-ed278a238cce/scratchpad/rv
cp "$F" "$S/$L.saved"
python3 - "$F" "$OLD" "$NEW" <<'PY' || { echo "REFUSED: edit did not apply once"; exit 2; }
import sys
p, old, new = sys.argv[1:4]
s = open(p).read()
if s.count(old) != 1: sys.exit(1)
open(p, 'w').write(s.replace(old, new))
PY
echo "== the edit"; diff "$S/$L.saved" "$F"
rm -rf brouter/build/test-results/jvmTest
timeout 590 ./gradlew :brouter:jvmTest --console=plain --tests 'com.zynergylabs.forager.brouter.expressions.*' > "$S/$L.log" 2>&1
echo "gradle exit $?"
cp "$S/$L.saved" "$F"
cmp "$S/$L.saved" "$F" && echo "restored from the saved copy"
git diff --quiet -- "$F" && echo "file matches HEAD after restore" || echo "WARNING: file differs from HEAD after restore"
NE=$(grep -c '^e: ' "$S/$L.log")
echo "compile errors in the log: $NE"
if [ "$NE" != "0" ]; then grep '^e: ' "$S/$L.log" | head -5; echo "REFUSED: the reverted build did not compile; results not read"; exit 3; fi
grep -q 'Task :brouter:compileKotlinJvm$' "$S/$L.log" && echo "compileKotlinJvm ran (not up to date)" || echo "WARNING: compileKotlinJvm did not run in this build"
python3 /home/user/forager-ios/tools/junit-counts.py brouter/build/test-results/jvmTest | sed 's/ (brouter.*//'
python3 - <<'PY'
import glob, xml.etree.ElementTree as ET
for f in sorted(glob.glob('/home/user/forager-ios/brouter/build/test-results/jvmTest/TEST-*.xml')):
    for c in ET.parse(f).getroot().findall('testcase'):
        for k in ('failure', 'error'):
            e = c.find(k)
            if e is not None:
                print(f"FAIL {c.get('classname').split('.')[-1]}.{c.get('name')}: {(e.get('message') or '')[:600]}")
PY
