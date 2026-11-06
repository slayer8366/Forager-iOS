import sys, collections
rows=[l.rstrip('\n').split('\t') for l in open(sys.argv[1])]
h=rows[0]; rows=rows[1:]; ix={k:i for i,k in enumerate(h)}
draws=[int(r[0]) for r in rows]
print("rows", len(rows), "draws 1..n contiguous:", draws==list(range(1,len(rows)+1)))
res=collections.Counter(r[ix['result']] for r in rows)
print("results", dict(res))
taken=[r for r in rows if r[ix['result']].startswith('taken')]
for s in sorted(set(r[ix['result']] for r in taken)):
    t=[r for r in taken if r[ix['result']]==s]
    ids=[r[1] for r in t]
    print(s, len(t), "ids rising:", ids==sorted(ids), "draws rising:", [int(r[0]) for r in t]==sorted(int(r[0]) for r in t), "last draw", t[-1][0])
rej=[r for r in rows if not r[ix['result']].startswith('taken')]
print("rejected", len(rej), "== draws - taken:", len(rej)==len(rows)-len(taken))
routed=[r for r in rows if r[1]!='-' and r[2]!='-']
print("routed", len(routed), "R2-equals-R1 false:", sum(1 for r in routed if r[ix['R2-equals-R1']]!='true'))
print("panic true (routed):", sum(1 for r in routed if r[ix['R2-panic']]=='true'), "with collections:", sum(1 for r in routed if r[ix['R2-collected-lines']] not in ('0','-')))
print("reach readings:", dict(collections.Counter(r[ix['reach']] for r in rej).most_common(12)))
