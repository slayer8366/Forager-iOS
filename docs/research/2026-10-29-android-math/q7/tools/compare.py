# compare.py A colA B colB nkey : line by line, the first nkey whitespace columns must match (keys), then compares
# the bits at column colA of A with column colB of B (0-based). Prints counts and every differing line.
import sys
A, ca, B, cb, nk = sys.argv[1], int(sys.argv[2]), sys.argv[3], int(sys.argv[4]), int(sys.argv[5])
la = [l.split() for l in open(A) if l.strip() and not l.startswith('#')]
lb = [l.split() for l in open(B) if l.strip() and not l.startswith('#')]
print(f"# A={A} col {ca}; B={B} col {cb}; keys {nk}")
print(f"lines A {len(la)} B {len(lb)}")
if len(la) != len(lb): print("LINE COUNT MISMATCH"); sys.exit(1)
keymm = diff = 0
for a, b in zip(la, lb):
    if a[:nk] != b[:nk]: keymm += 1; continue
    if a[ca] != b[cb]:
        diff += 1; print("DIFF", ' '.join(a[:nk]), a[ca], b[cb])
print(f"key mismatches {keymm}")
print(f"differing {diff} of {len(la)}")
