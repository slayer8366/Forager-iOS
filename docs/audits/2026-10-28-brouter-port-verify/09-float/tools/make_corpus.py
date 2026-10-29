# Builds the parse corpus for the fp probe (part 6, item H).
# usage: python3 -I make_corpus.py <out dir> <lookups.dat> <brf files...>
import sys, os, random, struct
from fractions import Fraction
out, lookups, brfs = sys.argv[1], sys.argv[2], sys.argv[3:]
os.makedirs(out, exist_ok=True)
toks = set()
for f in brfs:
    for line in open(f, encoding='utf-8', errors='replace'):
        line = line.split('#', 1)[0]
        for t in line.split():
            toks.add(t)
look = set()
for line in open(lookups, encoding='utf-8', errors='replace'):
    if line.startswith('---') or ';' not in line:
        continue
    rest = line.split(';', 1)[1].split()
    for w in rest[1:]:
        look.add(w)
with open(os.path.join(out, 'profile-tokens.txt'), 'w') as f:
    for t in sorted(toks): f.write(t + '\n')
with open(os.path.join(out, 'lookup-values.txt'), 'w') as f:
    for t in sorted(look): f.write(t + '\n')
# stress: float32 halfway cases and near-halfway, plus random decimals
rnd = random.Random(20261029)
def f32_bits_to_frac(b):
    v = struct.unpack('>f', struct.pack('>I', b))[0]
    return Fraction(v)
stress = []
for _ in range(40000):
    e = rnd.randint(0x30, 0x4e)  # exponents ~1e-8 .. 1e10
    m = rnd.getrandbits(23)
    b = (e << 23) | m
    lo = f32_bits_to_frac(b); hi = f32_bits_to_frac(b + 1)
    mid = (lo + hi) / 2
    for q in (mid, mid + Fraction(1, 10**40), mid - Fraction(1, 10**40)):
        # exact decimal expansion of q (finite for mid; truncated for others) to 60 significant digits
        n, d = q.numerator, q.denominator
        ip = n // d; r = n % d; digs = []
        for _ in range(60):
            r *= 10; digs.append(str(r // d)); r %= d
        stress.append(str(ip) + '.' + ''.join(digs).rstrip('0') if any(c != '0' for c in digs) else str(ip))
for _ in range(60000):
    nd = rnd.randint(1, 20)
    s = ''.join(rnd.choice('0123456789') for _ in range(nd))
    p = rnd.randint(0, nd)
    s = (s[:p] + '.' + s[p:]) if p < nd else s
    if rnd.random() < 0.3: s += 'e' + str(rnd.randint(-30, 30))
    if rnd.random() < 0.2: s = '-' + s
    stress.append(s)
with open(os.path.join(out, 'stress-decimals.txt'), 'w') as f:
    for t in stress: f.write(t + '\n')
# waypoint-like doubles as text: Kotlin/Java Double.toString style is produced on each platform; here plain decimals
with open(os.path.join(out, 'seed.txt'), 'w') as f: f.write('1\n')
print('profile tokens', len(toks), 'lookup values', len(look), 'stress', len(stress))
