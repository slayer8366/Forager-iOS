#!/usr/bin/env bash
# Revert runner for step 1 pinning (CLAUDE.md runner rules): revert.sh <label> [gradle args...]
# Edits W70_N40.rd5's pinned bytes 5292622 -> 5292623 in tools/tile-pins.tsv. Saves a copy before editing and restores
# from it (never from git); deletes the results folder first; refuses to cite results when the log has compile errors;
# after restoring, confirms the file equals the saved copy and HEAD's.
set -u
cd /home/user/forager-ios
L="$1"; shift
F=tools/tile-pins.tsv
S=/tmp/claude-0/-home-user/c9851708-fbbe-5ce6-a5f8-ed278a238cce/scratchpad/rv
cp "$F" "$S/$L.saved"
python3 - "$F" <<'PY' || { echo "REFUSED: edit did not apply once"; exit 2; }
import sys
p = sys.argv[1]
s = open(p).read()
old = "W70_N40.rd5\tsegments/W70_N40.rd5\t5292622\t"
if s.count(old) != 1: sys.exit(1)
open(p, 'w').write(s.replace(old, old.replace("5292622", "5292623")))
PY
echo "== the edit"; diff "$S/$L.saved" "$F" | cut -c1-120
rm -rf brouter/build/test-results/jvmTest
timeout 1200 ./gradlew :brouter:jvmTest --console=plain --tests 'com.zynergylabs.forager.brouter.tiles.*' "$@" > "$S/$L.log" 2>&1
echo "gradle exit $?"
cp "$S/$L.saved" "$F"
cmp "$S/$L.saved" "$F" && echo "restored from the saved copy"
git diff --quiet -- "$F" && echo "file matches HEAD after restore" || echo "WARNING: file differs from HEAD after restore"
NE=$(grep -c '^e: ' "$S/$L.log")
echo "compile errors in the log: $NE"
if [ "$NE" != "0" ]; then grep '^e: ' "$S/$L.log" | head -5; echo "REFUSED: the reverted build did not compile; results not read"; exit 3; fi
echo "== fetch lines"; grep -E 'REFUSED|fetched from|present|tiles ready|Execution failed|> Task :brouter:(fetchTiles|jvmTest)' "$S/$L.log" | cut -c1-220
if [ -d brouter/build/test-results/jvmTest ]; then
  python3 tools/junit-counts.py brouter/build/test-results/jvmTest | sed 's/ (brouter.*//'
  python3 - <<'PY'
import glob, xml.etree.ElementTree as ET
for f in sorted(glob.glob('/home/user/forager-ios/brouter/build/test-results/jvmTest/TEST-*.xml')):
    for c in ET.parse(f).getroot().findall('testcase'):
        st = 'PASS'
        for k in ('failure', 'error'):
            e = c.find(k)
            if e is not None: st = 'FAIL: ' + (e.get('message') or '')[:700]
        print(f"{c.get('classname').split('.')[-1]}.{c.get('name')}: {st}")
PY
else
  echo "no results folder: jvmTest did not run"
fi
