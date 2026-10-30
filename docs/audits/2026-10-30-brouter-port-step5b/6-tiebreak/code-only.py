import sys
lines = open(sys.argv[1]).read().split('\n')
ranges = [tuple(map(int, r.split('-'))) for r in sys.argv[2:]]
inc = False
for i, l in enumerate(lines, 1):
    s = l.strip()
    was = inc
    if not inc and s.startswith('/*'):
        inc = '*/' not in s
        continue
    if inc:
        if '*/' in s: inc = False
        continue
    if not s or s.startswith('//'): continue
    if any(a <= i <= b for a, b in ranges): print(f"{i}:{l}")
