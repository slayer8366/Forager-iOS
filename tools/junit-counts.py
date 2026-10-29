#!/usr/bin/env python3
"""Prints the counts in JUnit XML files, one line per suite and a total (Phase 2's "counts read from the JUnit XML").

    tools/junit-counts.py <dir-or-file>...

Exits 1 when no XML file is found, so an empty or missing results folder is never read as "0 failures".
"""
import pathlib
import sys
import xml.etree.ElementTree as ET

paths = []
for arg in sys.argv[1:]:
    p = pathlib.Path(arg)
    paths += sorted(p.rglob("TEST-*.xml")) if p.is_dir() else [p]
if not paths:
    print("no JUnit XML found in " + " ".join(sys.argv[1:]))
    sys.exit(1)
total = {"tests": 0, "failures": 0, "errors": 0, "skipped": 0}
for path in paths:
    suite = ET.parse(path).getroot()
    counts = {k: int(suite.get(k, "0")) for k in total}
    cases = suite.findall("testcase")
    if len(cases) != counts["tests"]:
        print(f"{path}: header says {counts['tests']} tests but {len(cases)} testcase elements")
    for k in total:
        total[k] += counts[k]
    print(f"{suite.get('name')}: " + ", ".join(f"{k} {v}" for k, v in counts.items()) + f" ({path})")
print("TOTAL: " + ", ".join(f"{k} {v}" for k, v in total.items()) + f" in {len(paths)} files")
