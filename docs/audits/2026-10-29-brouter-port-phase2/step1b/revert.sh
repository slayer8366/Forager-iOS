#!/usr/bin/env bash
# Revert runner (CLAUDE.md runner rules): revert.sh <file> <old-text> <new-text> <label> <tests filter>
# Saves a copy before editing and restores from it (never from git); deletes the results folder first; refuses to cite
# results when the log has compile errors or the test task did not run; after restoring, confirms the file equals the
# saved copy and HEAD's.
set -u
cd /home/user/forager-ios
F="$1"; OLD="$2"; NEW="$3"; L="$4"; T="$5"
S=/tmp/claude-0/-home-user/c9851708-fbbe-5ce6-a5f8-ed278a238cce/scratchpad/rv
mkdir -p "$S"
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
timeout 1200 ./gradlew :brouter:jvmTest --console=plain --tests "$T" > "$S/$L.log" 2>&1
echo "gradle exit $?"
cp "$S/$L.saved" "$F"
cmp "$S/$L.saved" "$F" && echo "restored from the saved copy"
git diff --quiet -- "$F" && echo "file matches HEAD after restore" || echo "WARNING: file differs from HEAD after restore"
NE=$(grep -c '^e: ' "$S/$L.log")
echo "compile errors in the log: $NE"
if [ "$NE" != "0" ]; then grep '^e: ' "$S/$L.log" | head -5; echo "REFUSED: the reverted build did not compile; results not read"; exit 3; fi
grep -q '^> Task :brouter:jvmTest' "$S/$L.log" || { echo "REFUSED: jvmTest did not run"; exit 4; }
python3 tools/junit-counts.py brouter/build/test-results/jvmTest | sed 's/ (brouter.*//'
python3 - <<'PY'
import glob, xml.etree.ElementTree as ET
for f in sorted(glob.glob('/home/user/forager-ios/brouter/build/test-results/jvmTest/TEST-*.xml')):
    r = ET.parse(f).getroot()
    for c in r.findall('testcase'):
        st = 'PASS'
        for k in ('failure', 'error'):
            e = c.find(k)
            if e is not None: st = 'FAIL: ' + (e.get('message') or '')[:600]
        print(f"{c.get('classname').split('.')[-1]}.{c.get('name')}: {st}")
    so = r.find('system-out')
    if so is not None and so.text: print("system-out:", so.text.strip()[:400])
PY
