#!/usr/bin/env python3
"""Every JDK class, method and field the reached BRouter methods use, read from their bytecode (dispatch
2026-10-28-01, item B). Also: monitors (synchronized blocks), volatile fields of kept classes, and string concatenation.

  python3 -I jdk_use.py <javap.txt> <reached-methods.tsv> <out.txt>
"""
import re
import sys
from collections import defaultdict

javap_path, reached_path, out_path = sys.argv[1:4]
reached = set()
with open(reached_path, encoding='utf-8') as f:
    next(f)
    for line in f:
        p = line.rstrip('\n').split('\t')
        reached.add((p[0], p[1], p[2]))

INSN = re.compile(r'^\s+\d+: (\w+)\s*(?:#\d+(?:,\s*\d+)?)?\s*(?://\s*(\w+) (.*))?$')
uses = defaultdict(lambda: defaultdict(set))  # jdk class -> member -> set of BRouter classes using it
monitors = defaultdict(int)
concat = 0
volatile_fields = []
cur_cls = None
cur = None
pending = None
in_code = False
with open(javap_path, encoding='utf-8') as f:
    for raw in f:
        line = raw.rstrip('\n')
        m = re.match(r'^(?:[a-z]+ )*(?:class|interface) ([\w.$]+)', line)
        if m and not line.startswith(' '):
            cur_cls = m.group(1).replace('.', '/')
            cur = None
            continue
        if re.match(r'^  [^ #{}]', line) and not line.startswith('  minor') and not line.startswith('  major') \
                and not line.startswith('  flags') and not line.startswith('  this_class') and not line.startswith('  super_class') \
                and not line.startswith('  interfaces') and not line.startswith('  Constant pool') and not line.startswith('  SourceFile') \
                and not line.startswith('  InnerClasses') and not line.startswith('  Signature') and not line.startswith('  NestM') \
                and not line.startswith('  NestHost') and not line.startswith('  Compiled') and not line.startswith('  Last') \
                and not line.startswith('  SHA') and not line.startswith('  EnclosingMethod'):
            pending = line.strip()
            cur = None
            in_code = False
            continue
        if line.startswith('    descriptor: ') and pending is not None:
            desc = line.split(': ', 1)[1].strip()
            if '(' in pending or pending == 'static {};':
                d = pending.rstrip(';')
                if d == 'static {}':
                    name = '<clinit>'
                else:
                    head = d.split('(')[0].split()
                    name = head[-1]
                    if name == cur_cls.replace('/', '.'):
                        name = '<init>'
                cur = (cur_cls, name, desc)
            else:
                if 'volatile' in pending.split():
                    volatile_fields.append((cur_cls, pending))
                cur = None
            pending = None
            continue
        if line.startswith('    Code:'):
            in_code = True
            continue
        if cur is None or cur not in reached or not in_code:
            continue
        mi = INSN.match(line)
        if not mi:
            continue
        op, kind, rest = mi.groups()
        if op == 'monitorenter':
            monitors[cur] += 1
        if kind in ('Method', 'InterfaceMethod', 'Field'):
            mm = re.match(r'^(?:"?([^":]+?)"?\.)?"?([^":.]+)"?:(.*)$', rest.strip())
            if not mm:
                continue
            owner, nm, d = mm.groups()
            owner = owner or cur_cls
            if owner.startswith('btools/') or owner.startswith('['):
                continue
            uses[owner][f'{nm}{d}' if kind != 'Field' else f'field {nm}'].add(cur_cls.split('/')[-1])
        elif kind == 'class':
            c = rest.strip().strip('"')
            for t in (re.findall(r'L([^;]+);', c) or [c]):
                if not t.startswith('btools/') and not t.startswith('['):
                    uses[t][f'({op})'].add(cur_cls.split('/')[-1])
        elif kind == 'InvokeDynamic':
            concat += 1

with open(out_path, 'w', encoding='utf-8') as out:
    def w(s=''):
        out.write(s + '\n')
    w(f'reached methods: {len(reached)}')
    w(f'JDK classes used: {len(uses)}')
    for c in sorted(uses):
        users = set().union(*uses[c].values())
        w(f'{c}  (used in {len(users)} BRouter classes: {" ".join(sorted(users))})')
        for mname in sorted(uses[c]):
            w(f'    {mname}')
    w('')
    w(f'synchronized blocks (monitorenter) in reached methods: {sum(monitors.values())}')
    for k, n in sorted(monitors.items()):
        w(f'  {k[0]}.{k[1]}{k[2]}: {n}')
    w(f'string concatenations (invokedynamic) in reached methods: {concat}')
    w('volatile fields in all classes:')
    for c, d in volatile_fields:
        w(f'  {c}: {d}')
print(open(out_path).read())
