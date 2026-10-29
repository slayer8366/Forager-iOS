# Compares the fp probe's outputs between two platforms, file by file and line by line.
# usage: python3 -I compare.py <out dir A> <out dir B> [max examples]
import sys, os
a, b = sys.argv[1], sys.argv[2]
mx = int(sys.argv[3]) if len(sys.argv) > 3 else 5
names = sorted(set(os.listdir(a)) & set(os.listdir(b)))
print('A =', a); print('B =', b)
print('only in A:', sorted(set(os.listdir(a)) - set(os.listdir(b))))
print('only in B:', sorted(set(os.listdir(b)) - set(os.listdir(a))))
for n in names:
    la = open(os.path.join(a, n)).read().split('\n'); lb = open(os.path.join(b, n)).read().split('\n')
    diff = [(i, x, y) for i, (x, y) in enumerate(zip(la, lb)) if x != y]
    print(f'{n}: lines A={len(la)-1} B={len(lb)-1} differing={len(diff)}' + ('' if len(la) == len(lb) else ' LENGTH MISMATCH'))
    for i, x, y in diff[:mx]:
        print(f'   line {i+1}\n     A: {x[:200]}\n     B: {y[:200]}')
