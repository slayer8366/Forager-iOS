# Parse an nginx autoindex page into name<TAB>date<TAB>size. Usage: python3 -I parse-index.py <index.html>
import re, sys
pat = re.compile(r'<a href="([^"]+)">[^<]*</a>\s+(\d\d-\w{3}-\d{4} \d\d:\d\d)\s+(\S+)')
n = 0
for line in open(sys.argv[1], encoding="utf-8", errors="replace"):
    m = pat.search(line)
    if m:
        print("\t".join(m.groups())); n += 1
print(f"# entries parsed: {n}", file=sys.stderr)
