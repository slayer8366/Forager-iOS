#!/usr/bin/env bash
# Compares two class trees under btools/{codec,expressions,mapaccess,router,util}:
# the class-name sets, each class file's major version, and `javap -c -p` with constant-pool indices removed.
set -u
A=$1; B=$2; OUT=$3
( cd "$A" && find btools/codec btools/expressions btools/mapaccess btools/router btools/util -name '*.class' | sort ) > "$OUT.a.txt"
( cd "$B" && find btools/codec btools/expressions btools/mapaccess btools/router btools/util -name '*.class' | sort ) > "$OUT.b.txt"
echo "classes in A: $(wc -l < "$OUT.a.txt"); in B: $(wc -l < "$OUT.b.txt"); only in A: $(comm -23 "$OUT.a.txt" "$OUT.b.txt" | wc -l); only in B: $(comm -13 "$OUT.a.txt" "$OUT.b.txt" | wc -l)"
comm -3 "$OUT.a.txt" "$OUT.b.txt" | sed 's/^/  name differs: /'
major() { od -An -j6 -N2 -tu1 "$1" | awk '{print $1*256+$2}'; }
echo "major versions A: $(while read -r c; do major "$A/$c"; done < "$OUT.a.txt" | sort | uniq -c | tr '\n' ' ')"
echo "major versions B: $(while read -r c; do major "$B/$c"; done < "$OUT.b.txt" | sort | uniq -c | tr '\n' ' ')"
same=0; diff=0; : > "$OUT.diffs.txt"
norm() { javap -c -p "$1" 2>&1 | sed -E 's/#[0-9]+(, *[0-9]+)?//g; s/^Compiled from.*//' ; }
while read -r c; do
  [ -f "$B/$c" ] || continue
  if cmp -s <(norm "$A/$c") <(norm "$B/$c"); then same=$((same+1)); else diff=$((diff+1)); echo "== $c" >> "$OUT.diffs.txt"; diff <(norm "$A/$c") <(norm "$B/$c") | head -40 >> "$OUT.diffs.txt"; fi
done < "$OUT.a.txt"
echo "javap -c -p, constant-pool indices removed: identical $same, different $diff (details: $(basename "$OUT").diffs.txt)"
