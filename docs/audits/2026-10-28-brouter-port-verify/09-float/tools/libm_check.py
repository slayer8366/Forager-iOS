# Recomputes the probe's atan2 and scale-table cos values with this host's C library (Python's math module), as a check
# from outside the Kotlin probe. usage: python3 -I libm_check.py <out dir>
import sys, math, struct, os, platform
d = sys.argv[1]
def h(x): return format(struct.unpack('>Q', struct.pack('>d', x))[0], '016x')
n = eq = 0
for line in open(os.path.join(d, 'atan2.txt')):
    p = line.split()
    if len(p) < 3: continue
    n += 1; eq += h(math.atan2(float(p[0]), float(p[1]))) == p[2]
print(f'atan2: {eq} of {n} equal to host libm')
n = eq = 0
for line in open(os.path.join(d, 'scale-table.txt')):
    p = line.split()
    if len(p) < 3: continue
    lat = struct.unpack('>d', bytes.fromhex(p[1]))[0]
    n += 1; eq += h(math.cos(lat)) == p[2]
print(f'scale-table cos: {eq} of {n} equal to host libm')
print('host libc:', platform.libc_ver(), 'python', platform.python_version())
