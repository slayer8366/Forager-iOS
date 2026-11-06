#!/usr/bin/env bash
# E1 scan of the staged diff's added lines, with Forager's own ends (routes.json, points.json at a0535ece) removed by exact value first.
# RECORD -93. Prints the count. Usage: scan.sh [git diff args, default --cached]
S=$(dirname "$0")
git diff "${@:---cached}" | grep -E '^\+' | python3 -I -c '
import sys,re
ends=sorted(set(open(sys.argv[1]).read().split()),key=len,reverse=True)
pat=re.compile(r"(?<![0-9.])(?:"+"|".join(re.escape(e.lstrip("-")) for e in ends)+r")(?![0-9])") if ends else None
for l in sys.stdin:
    print(pat.sub("X",l) if pat else l,end="")' "$S/forager-ends.txt" | grep -cE '[0-9]{1,3}\.[0-9]{5,}|node=[0-9]{8,9},[0-9]{8,9}|(^|[^0-9])1[0-9]{8},1[0-9]{8}'
