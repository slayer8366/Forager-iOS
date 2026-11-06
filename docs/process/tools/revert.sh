#!/usr/bin/env bash
# Revert runner (Forager's Known pitfalls): saves a copy, edits one exact string (must occur once), runs the filter,
# refuses results when the build log has compile errors or the XML predates the edit, restores from the copy, cmp's.
# Usage: revert.sh <name> <file> <old> <new> <test filter>   (run from the clone root)
set -u
R=$(dirname "$0"); name=$1; f=$2; old=$3; new=$4; filt=$5
cp "$f" "$R/$name.saved" || exit 2
python3 -I -c 'import sys
p,o,n=sys.argv[1:4]; s=open(p).read(); c=s.count(o)
if c!=1: sys.exit("old string occurs %d times"%c)
open(p,"w").write(s.replace(o,n))' "$f" "$old" "$new" || exit 3
touch "$R/$name.stamp"; sleep 1
diff -u "$R/$name.saved" "$f" > "$R/$name.diff"
timeout 1500 ./gradlew --max-workers=2 :brouter:jvmTest --tests "$filt" --console=plain > "$R/$name-gradle.log" 2>&1
cp "$R/$name.saved" "$f"
cmp "$R/$name.saved" "$f" && echo "restored: cmp equal"
e=$(grep -c '^e: ' "$R/$name-gradle.log")
echo "compile errors: $e"
if [ "$e" != 0 ]; then echo "REFUSED: compile errors, results not read"; grep '^e: ' "$R/$name-gradle.log" | head -3; exit 4; fi
fresh=$(find brouter/build/test-results/jvmTest -name 'TEST-*.xml' -newer "$R/$name.stamp" | wc -l)
echo "fresh XML files: $fresh"
[ "$fresh" = 0 ] && { echo "REFUSED: no fresh XML"; exit 5; }
find brouter/build/test-results/jvmTest -name 'TEST-*.xml' -newer "$R/$name.stamp" | xargs python3 -I tools/junit-counts.py 2>/dev/null | tail -1 > "$R/$name-counts.txt" || true
python3 -I tools/junit-counts.py brouter/build/test-results/jvmTest | tail -1 >> "$R/$name-counts.txt"
find brouter/build/test-results/jvmTest -name 'TEST-*.xml' -newer "$R/$name.stamp" -exec grep -ho 'testcase name="[^"]*"\|message="[^"]\{0,200\}' {} + | grep -B1 '^message' > "$R/$name-failures.txt"
cat "$R/$name-counts.txt"; head -c 1500 "$R/$name-failures.txt"
