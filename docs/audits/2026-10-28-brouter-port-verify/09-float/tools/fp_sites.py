#!/usr/bin/env python3
"""Item H: list floating-point and hash-iteration sites in the reached methods.

Usage: fp_sites.py <javap -v -c -p output> <reached-methods.tsv> <out dir>

Reads part 1's javap dump (all BRouter classes, compiled with line tables) and
part 1's reached-method list for one tier. For each reached method it records,
by source line:
  arith   double/float arithmetic opcodes (dadd ... fneg)
  cmp     double/float comparisons (dcmpl, dcmpg, fcmpl, fcmpg)
  conv    conversions to or from double/float (d2i, f2i, i2f, ...)
  math    calls to java/lang/Math or StrictMath
  boxed   calls to java/lang/Float or java/lang/Double (parse, toString, compare...)
  fstr    a float or double printed into a string: StringBuilder.append(F/D),
          String.valueOf(F/D), or an invokedynamic string concatenation
          (Java 9+ bytecode) taking an F or D argument
  hashit  keySet/values/entrySet/forEach on a Map type, iterator() on a Set type
It writes sites.tsv (one row per method and category) and summary.txt.
"""
import re, sys, collections, os

ARITH = {'dadd','dsub','dmul','ddiv','drem','dneg','fadd','fsub','fmul','fdiv','frem','fneg'}
CMP = {'dcmpl','dcmpg','fcmpl','fcmpg'}
CONV = {'d2i','d2l','d2f','f2i','f2l','f2d','i2d','i2f','l2d','l2f'}

javap, reached_tsv, outdir = sys.argv[1:4]
os.makedirs(outdir, exist_ok=True)

reached = set()
with open(reached_tsv) as f:
    next(f)
    for line in f:
        p = line.rstrip('\n').split('\t')
        reached.add((p[0], p[1], p[2]))

cls_re = re.compile(r'^Classfile .*/classes/(btools/\S+)\.class$')
meth_re = re.compile(r'^  (?:[\w<>\[\], .$]*?)\b([\w$<>]+)\((.*)\)(?: throws [\w.$, ]+)?;$')
static_re = re.compile(r'^  static \{\};$')
desc_re = re.compile(r'^    descriptor: (\S+)$')
ins_re = re.compile(r'^\s+(\d+): (\w+)\s*(.*)$')
line_re = re.compile(r'^\s+line (\d+): (\d+)$')

methods = {}  # (cls, name, desc) -> {'ins': [(off, op, rest)], 'lines': [(line, off)]}
cur_cls = None; cur = None; pending_name = None; in_lnt = False
with open(javap) as f:
    for raw in f:
        line = raw.rstrip('\n')
        m = cls_re.match(line)
        if m:
            cur_cls = m.group(1); cur = None; pending_name = None; continue
        if cur_cls is None:
            continue
        if static_re.match(line):
            pending_name = '<clinit>'; continue
        m = meth_re.match(line)
        if m and not line.startswith('   '):
            name = m.group(1)
            simple = cur_cls.split('/')[-1]
            if name == cur_cls.replace('/', '.') or name == simple or name.endswith('.' + simple):
                name = '<init>'
            pending_name = name; continue
        m = desc_re.match(line)
        if m and pending_name:
            key = (cur_cls, pending_name, m.group(1))
            cur = {'ins': [], 'lines': []}
            methods[key] = cur
            pending_name = None; in_lnt = False; continue
        if cur is None:
            continue
        if line.strip() == 'LineNumberTable:':
            in_lnt = True; continue
        if in_lnt:
            m = line_re.match(line)
            if m:
                cur['lines'].append((int(m.group(1)), int(m.group(2)))); continue
            in_lnt = False
        m = ins_re.match(line)
        if m:
            cur['ins'].append((int(m.group(1)), m.group(2), m.group(3)))

def src_line(lines, off):
    best = None
    for ln, o in sorted(lines, key=lambda x: x[1]):
        if o <= off:
            best = ln
    return best

rows = []
missing = []
counts = collections.Counter()
methods_with = collections.defaultdict(set)
for key in sorted(reached):
    if key not in methods:
        missing.append(key); continue
    md = methods[key]
    cats = collections.defaultdict(list)
    for off, op, rest in md['ins']:
        cat = None; what = op
        if op in ARITH: cat = 'arith'
        elif op in CMP: cat = 'cmp'
        elif op in CONV: cat = 'conv'
        elif op.startswith('invoke'):
            mm = re.search(r'// (?:Interface)?Method ([\w/$]+)\.([\w<>$]+):(\S+)', rest)
            if mm:
                owner, name, d = mm.groups()
                what = owner.split('/')[-1] + '.' + name + d
                if owner in ('java/lang/Math', 'java/lang/StrictMath'): cat = 'math'
                elif owner in ('java/lang/Float', 'java/lang/Double'): cat = 'boxed'
                elif owner == 'java/lang/StringBuilder' and name == 'append' and (d.startswith('(F)') or d.startswith('(D)')): cat = 'fstr'
                elif owner == 'java/lang/String' and name == 'valueOf' and (d.startswith('(F)') or d.startswith('(D)')): cat = 'fstr'
                elif name in ('keySet', 'values', 'entrySet', 'forEach') and owner in ('java/util/Map', 'java/util/HashMap', 'java/util/LinkedHashMap', 'java/util/TreeMap', 'java/util/AbstractMap'): cat = 'hashit'
                elif name == 'iterator' and owner in ('java/util/Set', 'java/util/HashSet', 'java/util/LinkedHashSet'): cat = 'hashit'
        if op == 'invokedynamic' and 'makeConcatWithConstants' in rest:
            dm = re.search(r'makeConcatWithConstants:\((\S*?)\)', rest)
            # F or D as a whole parameter type, not inside an L...; class name
            params = re.findall(r'\[*(?:L[^;]+;|[ZBCSIJFD])', dm.group(1)) if dm else []
            if any(t in ('F', 'D') for t in params):
                cat = 'fstr'; what = 'concat(' + ''.join(t if len(t) == 1 else 'L' for t in params) + ')'
        if cat:
            cats[cat].append((src_line(md['lines'], off), what))
    for cat, items in cats.items():
        counts[cat] += len(items)
        methods_with[cat].add(key)
        by_line = collections.OrderedDict()
        for ln, what in items:
            by_line.setdefault(ln, collections.Counter())[what] += 1
        detail = '; '.join(f"{ln}:" + ','.join(f"{w}x{n}" if n > 1 else w for w, n in c.items()) for ln, c in by_line.items())
        rows.append((key[0], key[1], key[2], cat, len(items), detail))

with open(os.path.join(outdir, 'sites.tsv'), 'w') as f:
    f.write('class\tmethod\tdescriptor\tcategory\tcount\tlines\n')
    for r in rows:
        f.write('\t'.join(str(x) for x in r) + '\n')
with open(os.path.join(outdir, 'summary.txt'), 'w') as f:
    f.write(f'reached methods in list: {len(reached)}\n')
    f.write(f'reached methods found in javap: {len(reached) - len(missing)}\n')
    f.write(f'reached methods not found in javap: {len(missing)}\n')
    for k in missing:
        f.write(f'  missing: {k}\n')
    f.write(f'methods parsed from javap: {len(methods)}\n')
    for cat in ('arith', 'cmp', 'conv', 'math', 'boxed', 'fstr', 'hashit'):
        f.write(f'{cat}: {counts[cat]} instructions in {len(methods_with[cat])} methods\n')
    anyfp = set().union(*(methods_with[c] for c in ('arith', 'cmp', 'conv', 'math', 'boxed', 'fstr')))
    f.write(f'methods with any floating-point site: {len(anyfp)}\n')
print(open(os.path.join(outdir, 'summary.txt')).read())
