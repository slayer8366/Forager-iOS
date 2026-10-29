# Bottom-up layering of T2's kept files, from (1) T2's reached-method call edges and
# (2) jdeps class-level references, both restricted to files T2 keeps (kept_lines > 0).
# Usage: python3 -I layers.py <port-list.tsv> <edges.tsv> <jdeps-class.txt> <mode: calls|calls+types>
import sys, re, collections
port, edges, jdeps, mode = sys.argv[1:5]
kept = {}
for i, line in enumerate(open(port)):
    if i == 0: continue
    f, fl, kl, mk, mf, *_ = line.rstrip('\n').split('\t')
    if int(kl) > 0: kept[f[:-5]] = int(kl)          # btools/x/Y
def fileof(cls):
    c = cls.replace('.', '/').split('$')[0]
    return c if c in kept else None
g = collections.defaultdict(set)
n_call = n_type = 0
for line in open(edges):
    a, b, why = line.rstrip('\n').split('\t')
    if a in ('JDK', 'FORAGER'): continue
    cls = lambda m: m.split('(')[0].rsplit('.', 1)[0]
    fa, fb = fileof(cls(a)), fileof(cls(b))
    if fa and fb and fa != fb: g[fa].add(fb); n_call += 1
if mode == 'calls+types':
    for line in open(jdeps):
        m = re.match(r'\s+(btools\S+)\s+->\s+(btools\S+)\s+classes', line)
        if not m: continue
        fa, fb = fileof(m.group(1)), fileof(m.group(2))
        if fa and fb and fa != fb: g[fa].add(fb); n_type += 1
# Tarjan SCC
idx = {}; low = {}; st = []; on = set(); sccs = []; c = [0]
sys.setrecursionlimit(10000)
def sc(v):
    idx[v] = low[v] = c[0]; c[0] += 1; st.append(v); on.add(v)
    for w in g[v]:
        if w not in idx: sc(w); low[v] = min(low[v], low[w])
        elif w in on: low[v] = min(low[v], idx[w])
    if low[v] == idx[v]:
        comp = []
        while True:
            w = st.pop(); on.discard(w); comp.append(w)
            if w == v: break
        sccs.append(sorted(comp))
for v in sorted(kept):
    if v not in idx: sc(v)
comp_of = {v: i for i, comp in enumerate(sccs) for v in comp}
# layer = longest path to a sink over the condensation
memo = {}
def layer(i):
    if i in memo: return memo[i]
    deps = {comp_of[w] for v in sccs[i] for w in g[v]} - {i}
    memo[i] = 0 if not deps else 1 + max(layer(d) for d in deps)
    return memo[i]
print(f"mode={mode} files={len(kept)} lines={sum(kept.values())} call-edge-hits={n_call} type-edge-hits={n_type} sccs={len(sccs)}")
by = collections.defaultdict(list)
for i in range(len(sccs)): by[layer(i)].append(i)
for L in sorted(by):
    tot = 0
    print(f"--- layer {L}")
    for i in sorted(by[L], key=lambda i: sccs[i]):
        lines = sum(kept[v] for v in sccs[i]); tot += lines
        names = ' '.join(v.replace('btools/', '') for v in sccs[i])
        print(f"  {lines:6d}  {'SCC '+str(len(sccs[i]))+': ' if len(sccs[i])>1 else ''}{names}")
    print(f"  layer total {tot}")
