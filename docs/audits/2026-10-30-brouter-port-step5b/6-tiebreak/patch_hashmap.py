# Adds tie-break counters to a copy of Temurin 21.0.13+9's HashMap.java. Each edit is by line number, with the line's
# expected text checked first, so a different source stops the patch.
import sys
p = sys.argv[1]
L = open(p).read().split('\n')
def at(n, expect):
    if L[n-1].strip() != expect: raise SystemExit(f'line {n}: {L[n-1]!r} is not {expect!r}')
at(275, 'static final int MIN_TREEIFY_CAPACITY = 64;')
at(2058, 'static int tieBreakOrder(Object a, Object b) {')
at(2095, 'dir = tieBreakOrder(k, pk);')
at(2158, 'dir = tieBreakOrder(k, pk);')
# Highest line first, so earlier numbers stay valid.
L[2158-1] = L[2158-1].replace('dir = tieBreakOrder(k, pk);', 'probeTie(k, pk, 1); dir = tieBreakOrder(k, pk);')
L[2095-1] = L[2095-1].replace('dir = tieBreakOrder(k, pk);', '{ probeTie(k, pk, 0); dir = tieBreakOrder(k, pk); }')
L.insert(2058-1, '        static void probeTie(Object k, Object pk, int site) { PROBE_TIES[(k != null && k.getClass().getName().equals("btools.mapaccess.OsmNode") && pk != null && pk.getClass().getName().equals("btools.mapaccess.OsmNode")) ? site : site + 2]++; }')
L.insert(275, '    /** Probe (step 5b, scratch): tieBreakOrder calls. [0] treeify, [1] putTreeVal, with both keys btools.mapaccess.OsmNode; [2], [3] any other keys. */\n    public static final long[] PROBE_TIES = new long[4];')
open(p, 'w').write('\n'.join(L))
print('patched')
