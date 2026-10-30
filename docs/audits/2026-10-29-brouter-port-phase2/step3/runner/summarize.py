# Usage: summarize.py <results-dir> <build-log> <title-line>
# Refuses (exit 2) when the build log holds compile errors or no XML exists, so stale results are never read.
# Step 3's copy of step2a/runner/summarize.py: every test case is listed, pass or fail, whatever its package.
import sys, glob, os, xml.etree.ElementTree as ET
rdir, log, title = sys.argv[1], sys.argv[2], sys.argv[3]
text = open(log, errors='replace').read()
errs = [l for l in text.splitlines() if l.startswith('e: ')]
print(title)
print("# JDK: " + os.popen('java -version 2>&1 | grep -v "^Picked up" | head -1').read().strip())
print("# compile tasks in the build log: " + ", ".join(l.split()[2] + (" " + l.split()[3] if len(l.split()) > 3 else "") for l in text.splitlines() if l.startswith('> Task :brouter:compile')))
print("# compile errors in the build log (lines starting 'e: '): %d" % len(errs))
for l in errs[:20]: print("#   " + l)
files = sorted(glob.glob(os.path.join(rdir, 'TEST-*.xml')))
if errs or not files:
    print("REFUSED: compile errors or no JUnit XML; no result is read from this run"); sys.exit(2)
T = F = E = S = 0; cases = []
for f in files:
    s = ET.parse(f).getroot()
    t, fl, er, sk = (int(s.get(k)) for k in ('tests', 'failures', 'errors', 'skipped'))
    T += t; F += fl; E += er; S += sk
    print("%s: tests %d, failures %d, errors %d, skipped %d" % (s.get('name'), t, fl, er, sk))
    for c in s.findall('testcase'):
        bad = c.find('failure') if c.find('failure') is not None else c.find('error')
        name = "%s.%s" % (s.get('name').split('.')[-1], c.get('name'))
        if bad is not None:
            msg = (bad.get('message') or '').splitlines()[0][:300] if bad.get('message') else ''
            frame = next((l.strip() for l in (bad.text or '').splitlines() if 'com.zynergylabs.forager.brouter' in l and 'Test' not in l), '')
            cases.append("FAIL %s | %s | %s | %s" % (name, bad.get('type'), msg, frame))
        else:
            cases.append("PASS %s" % name)
print("TOTAL: tests %d, failures %d, errors %d, skipped %d in %d files" % (T, F, E, S, len(files)))
print("\n# Each test case: outcome | failure type | message (first line) | first frame in the port's main code")
for c in cases: print(c)
