#!/bin/bash
# Usage: ci-counts.sh <run id> <absolute out-file>. Downloads the run's artifacts into a fresh scratch folder and counts
# tests and failures per job from the JUnit XML itself (python -I), with the job and step conclusions from the API.
set -u
run=$1; out=$2; tok=$(gh auth token)
w=$(mktemp -d /tmp/claude-0/-home-user/c9851708-fbbe-5ce6-a5f8-ed278a238cce/scratchpad/ci-$run-XXXX)
api() { curl -sS -L -H "Authorization: Bearer $tok" "https://api.github.com/repos/slayer8366/Forager-iOS/$1"; }
{
echo "# CI run $run, read $(date -u +%FT%TZ)"
api "actions/runs/$run" | python3 -I -c "import sys,json; r=json.load(sys.stdin); print('# head', r['head_sha'], 'status', r['status'], 'conclusion', r['conclusion'])"
api "actions/runs/$run/jobs" | python3 -I -c "
import sys,json
for j in json.load(sys.stdin)['jobs']:
    print('job', j['name'], j['conclusion']); [print('   step', s['number'], s['name'][:70], '->', s['conclusion']) for s in j['steps']]"
for a in $(api "actions/runs/$run/artifacts" | python3 -I -c "import sys,json; [print(x['id'],x['name'],sep=':') for x in json.load(sys.stdin)['artifacts']]"); do
  id=${a%%:*}; name=${a#*:}; mkdir -p "$w/$name"; api "actions/artifacts/$id/zip" > "$w/$name.zip"; (cd "$w/$name" && unzip -q -o "../$name.zip")
  echo "## artifact $name"
  python3 -I - "$w/$name" <<'PY'
import sys,os,glob,xml.etree.ElementTree as ET
root=sys.argv[1]
for d in sorted({os.path.dirname(p) for p in glob.glob(root+'/**/TEST-*.xml',recursive=True)}):
    t=f=e=s=0; bad=[]
    for p in sorted(glob.glob(d+'/TEST-*.xml')):
        x=ET.parse(p).getroot(); t+=int(x.get('tests'));f+=int(x.get('failures'));e+=int(x.get('errors'));s+=int(x.get('skipped'))
        for c in x.iter('testcase'):
            if c.find('failure') is not None or c.find('error') is not None: bad.append(x.get('name').split('.')[-1]+'.'+c.get('name'))
        so=x.find('system-out'); txt=so.text if so is not None and so.text else ''
        for line in txt.splitlines():
            if line.startswith(('record:','JVM:','calcAngle branches')): print('   out', line[:200])
    print(' ', os.path.relpath(d,root), 'tests', t, 'failures', f, 'errors', e, 'skipped', s)
    for b in bad: print('    failed', b)
PY
done
} > "$out" 2>&1
cat "$out"
