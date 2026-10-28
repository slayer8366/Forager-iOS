#!/usr/bin/env python3
"""Compares the static reach (reach.py's reached-methods.tsv) with the methods JaCoCo saw run (dispatch
2026-10-28-01, item A). A method that ran but is not in the static set means the static analysis is unsound.

  python3 -I compare_dynamic.py <jacoco.xml> <reached-methods.tsv> <out.txt>
"""
import sys
import xml.etree.ElementTree as ET
from collections import Counter

xml_path, reached_path, out_path = sys.argv[1:4]
root = ET.parse(xml_path).getroot()
ran, all_jacoco = set(), set()
lines_cov = {}
for pkg in root.findall('package'):
    for c in pkg.findall('class'):
        cname = c.get('name')
        for m in c.findall('method'):
            k = (cname, m.get('name'), m.get('desc'))
            all_jacoco.add(k)
            mc = {x.get('type'): (int(x.get('covered')), int(x.get('missed'))) for x in m.findall('counter')}
            if mc.get('METHOD', (0, 0))[0] > 0:
                ran.add(k)
            lines_cov[k] = mc.get('LINE', (0, 0))

static = set()
with open(reached_path, encoding='utf-8') as f:
    next(f)
    for line in f:
        p = line.rstrip('\n').split('\t')
        static.add((p[0], p[1], p[2]))

with open(out_path, 'w', encoding='utf-8') as out:
    def w(s=''):
        out.write(s + '\n')
        print(s)
    w(f'methods JaCoCo instruments: {len(all_jacoco)}; ran: {len(ran)}')
    w(f'methods in the static reach: {len(static)} (of which JaCoCo instruments {len(static & all_jacoco)})')
    bad = sorted(ran - static)
    w(f'ran but not statically reached (unsound if any): {len(bad)}')
    for k in bad:
        w('  ' + '.'.join(k[:2]) + k[2])
    only_static = sorted((static & all_jacoco) - ran)
    w(f'statically reached, instrumented, but did not run on this sample: {len(only_static)}')
    by_class = Counter(k[0] for k in only_static)
    for c, n in sorted(by_class.items(), key=lambda x: (-x[1], x[0])):
        w(f'  {n:4d} {c}')
    lc = sum(lines_cov[k][0] for k in static & all_jacoco)
    lm = sum(lines_cov[k][1] for k in static & all_jacoco)
    w(f'lines (JaCoCo executable lines) in statically reached methods: {lc + lm}; covered on this sample: {lc} ({100.0 * lc / (lc + lm):.1f}%)')
    w('')
    w('statically reached, did not run (full list):')
    for k in only_static:
        w('  ' + '.'.join(k[:2]) + k[2] + f'  lines {lines_cov[k][0]}/{sum(lines_cov[k])}')
