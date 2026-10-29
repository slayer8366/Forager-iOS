#!/usr/bin/env python3
"""Fails unless every named test class ran: its JUnit XML is present and holds a test case that was not skipped.

    tools/junit-ran.py <results-dir> <class>...

Written for a step whose test failures are recorded, not gating (brouter.yml's arm64 JVM step; RECORD -36): a failure
there does not fail the job, but a run that never reached a named class does. Gradle's --tests filter fails the build
only when no filter matches at all, so one misnamed class among several would otherwise go unnoticed, and a build that
stops before testing leaves no XML. Every class is checked and printed before the exit: 0 when all ran, 1 otherwise,
2 on a usage error.
"""
import pathlib
import sys
import xml.etree.ElementTree as ET

if len(sys.argv) < 3:
    print("usage: junit-ran.py <results-dir> <class>...")
    sys.exit(2)
results = pathlib.Path(sys.argv[1])
names = sys.argv[2:]
not_run = []
for name in names:
    path = results / f"TEST-{name}.xml"
    if not path.is_file():
        print(f"{name}: did not run, no {path}")
        not_run.append(name)
        continue
    cases = ET.parse(path).getroot().findall("testcase")
    ran = [c for c in cases if c.find("skipped") is None]
    failed = [c for c in ran if c.find("failure") is not None or c.find("error") is not None]
    print(f"{name}: {len(cases)} test cases, {len(ran)} ran, {len(failed)} failed ({path})")
    if not ran:
        print(f"{name}: did not run, no test case that was not skipped")
        not_run.append(name)
if not_run:
    print(f"FAIL: {len(not_run)} of {len(names)} classes did not run: " + " ".join(not_run))
    sys.exit(1)
print(f"OK: all {len(names)} classes ran")
