# Counts each named span of HashMap.java: from its declaration line to its matching closing brace.
# Lines: total in span; code: not blank and not a comment-only line (//, /*, *, */).
import sys, re
src = open(sys.argv[1]).read().split('\n')
spec = [l.split('\t') for l in open(sys.argv[2]).read().strip().split('\n') if l and not l.startswith('#')]

def is_code(s):
    t = s.strip()
    return not (t == '' or t.startswith('//') or t.startswith('/*') or t.startswith('*') or t.startswith('*/'))

def strip(s):
    s = re.sub(r'"(\\.|[^"\\])*"', '""', s)
    s = re.sub(r"'(\\.|[^'\\])*'", "''", s)
    s = re.sub(r'//.*', '', s)
    return s

def span(start):
    # start is 1-based; find first '{' at or after start (skipping comment lines); then match braces.
    depth = 0; opened = False; incomment = False
    for i in range(start - 1, len(src)):
        line = src[i]
        if incomment:
            if '*/' in line: incomment = False; line = line.split('*/', 1)[1]
            else: continue
        line = strip(line)
        while '/*' in line:
            a, b = line.split('/*', 1)
            if '*/' in b: line = a + b.split('*/', 1)[1]
            else: line = a; incomment = True
        for ch in line:
            if ch == '{': depth += 1; opened = True
            elif ch == '}': depth -= 1
        if opened and depth == 0:
            return i + 1
        if not opened and line.strip().endswith(';'):
            return i + 1  # a declaration, not a block
    raise SystemExit('no end for %d' % start)

tot = code = 0
print('group\tname\tfirst\tlast\tlines\tcode\tnote')
for row in spec:
    group, name, first = row[0], row[1], int(row[2])
    last = int(row[3]) if len(row) > 3 and row[3] else span(first)
    note = row[4] if len(row) > 4 else ''
    lines = last - first + 1
    c = sum(1 for k in range(first - 1, last) if is_code(src[k]))
    print('%s\t%s\t%d\t%d\t%d\t%d\t%s' % (group, name, first, last, lines, c, note))
    if group != 'x':
        tot += lines; code += c
print('TOTAL (groups other than x)\t\t\t\t%d\t%d' % (tot, code))
