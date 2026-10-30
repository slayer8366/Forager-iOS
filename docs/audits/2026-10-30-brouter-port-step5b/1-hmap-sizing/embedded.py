# Counts assignments inside expressions ("(x = ...)" with a single '=') per reached span of HashMap.java.
import sys, re
src = open(sys.argv[1]).read().split('\n')
pat = re.compile(r'\(\s*(?:\([^()]*\)\s*)?[A-Za-z_][\w.\[\]]*\s*=(?!=)')
chain = re.compile(r'\b[A-Za-z_]\w*\s*=\s*[A-Za-z_]\w*\s*=(?!=)')
tot = 0
for line in open(sys.argv[2]).read().strip().split('\n')[1:]:
    f = line.split('\t')
    if len(f) < 6 or f[0] in ('x',) or not f[2].isdigit():
        continue
    a, b = int(f[2]), int(f[3])
    n = sum(len(pat.findall(src[k])) + len(chain.findall(src[k])) for k in range(a - 1, b))
    tot += n
    if n: print('%s\t%d' % (f[1], n))
print('TOTAL\t%d' % tot)
