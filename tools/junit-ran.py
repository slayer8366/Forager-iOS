#!/usr/bin/env python3
"""Fails unless every named test class ran: its JUnit XML is present and holds a test case that was not skipped.

    tools/junit-ran.py <results-dir> <class>...

Written for a step whose test failures are recorded, not gating (brouter.yml's arm64 JVM step; RECORD -36): a failure
there does not fail the job, but a run that never reached a named class does. Gradle's --tests filter fails the build
only when no filter matches at all, so one misnamed class among several would otherwise go unnoticed, and a build that
stops before testing leaves no XML. Every class is checked and printed before the exit: 0 when all ran, 1 otherwise,
2 on a usage error.

    tools/junit-ran.py --simulator <results-dir> <class>...

Session 70 (review finding 69-1): the same question for the iOS simulator's results, stricter, in its own function so the
mode above is unchanged. A class ran only if its XML is present, it holds a test case and its header's tests is above 0,
no test case is skipped, and its system-out does not hold NOT_RUN: the words each commonTest or iosTest class prints when
it returns early because a job variable is unset (MemoryEngineCommonTest.kt, REngineCommonTest.kt,
StackLadderIosTest.kt), which JUnit XML records as a passed test. Every line it prints names the simulator, so it is not
read as the arm64 JVM gate. Same exit codes.
"""
import pathlib
import sys
import xml.etree.ElementTree as ET

NOT_RUN = "is unset; not run"


def simulator_ran(results, names):
    not_run = []
    for name in names:
        path = results / f"TEST-{name}.xml"
        if not path.is_file():
            print(f"{name}: did not run on the iOS simulator, no {path}")
            not_run.append(name)
            continue
        suite = ET.parse(path).getroot()
        header = int(suite.get("tests", "0"))
        cases = suite.findall("testcase")
        skipped = [c for c in cases if c.find("skipped") is not None]
        failed = [c for c in cases if c.find("failure") is not None or c.find("error") is not None]
        out = "".join(e.text or "" for e in suite.iter("system-out"))
        print(f"{name}: on the iOS simulator, {len(cases)} test cases, {len(cases) - len(skipped)} ran, "
              f"{len(skipped)} skipped, {len(failed)} failed ({path})")
        reasons = []
        if not cases or header <= 0:
            reasons.append(f"tests=\"{header}\" with {len(cases)} test cases")
        if skipped:
            reasons.append(f"{len(skipped)} skipped: " + " ".join(c.get("name", "?") for c in skipped))
        if NOT_RUN in out:
            line = next(l for l in out.splitlines() if NOT_RUN in l)
            reasons.append(f"it returned early: {line.strip()}")
        if reasons:
            print(f"{name}: did not run on the iOS simulator, " + "; ".join(reasons))
            not_run.append(name)
    if not_run:
        print(f"FAIL on the iOS simulator: {len(not_run)} of {len(names)} classes did not run: " + " ".join(not_run))
        sys.exit(1)
    print(f"OK on the iOS simulator: all {len(names)} classes ran ({results})")
    sys.exit(0)


if len(sys.argv) >= 2 and sys.argv[1] == "--simulator":
    if len(sys.argv) < 4:
        print("usage: junit-ran.py --simulator <results-dir> <class>...")
        sys.exit(2)
    simulator_ran(pathlib.Path(sys.argv[2]), sys.argv[3:])

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
