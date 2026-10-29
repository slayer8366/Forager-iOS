#!/usr/bin/env python3
"""Part 8 (item E): which scale-table rows differ between two tables, and which latitude band and
brouter.de tile row each one falls in.

Each table file has 1,800 lines "row lat cos kx ky" (bits in hex), as written by part 6's probe and by
TableRows.java. A row "differs for routing" when its kx or ky bits differ: those are the only values
CheapRuler hands out (CheapRuler.java:56-57, getLonLatToMeterScales). Row r covers ilat in
[r*100000, (r+1)*100000), so latitudes [r/10 - 90, (r+1)/10 - 90) (CheapRuler.java:21-22, :31, :57).
A brouter.de tile is named for its south-west corner on a 5-degree grid, so the band's tile row is
N or S of floor(lat/5)*5.

Usage: python3 -I table_rows.py <name> <tableA> <tableB>   (prints a report; exit 1 on a short table)
"""
import math
import sys


def read(path):
    rows = {}
    with open(path) as f:
        for line in f:
            p = line.split()
            if len(p) != 5:
                continue
            rows[int(p[0])] = p[1:]
    return rows


def tile_row(lat_lo):
    s = math.floor(lat_lo / 5) * 5
    return ("N%d" % s) if s >= 0 else ("S%d" % -s)


def main():
    name, a_path, b_path = sys.argv[1:4]
    a, b = read(a_path), read(b_path)
    if len(a) != 1800 or len(b) != 1800 or set(a) != set(range(1800)) or set(b) != set(range(1800)):
        print("SHORT TABLE: A=%d B=%d" % (len(a), len(b)))
        sys.exit(1)
    cos_diff = [r for r in range(1800) if a[r][1] != b[r][1]]
    lat_diff = [r for r in range(1800) if a[r][0] != b[r][0]]
    kxky_diff = [r for r in range(1800) if a[r][2:4] != b[r][2:4]]
    print("# %s" % name)
    print("# A = %s" % a_path)
    print("# B = %s" % b_path)
    print("rows compared: 1800 of 1800")
    print("lat differs: %d" % len(lat_diff))
    print("cos differs: %d" % len(cos_diff))
    print("kx or ky differs: %d" % len(kxky_diff))
    print("cos differs but kx and ky equal: %d" % len(set(cos_diff) - set(kxky_diff)))
    print("kx or ky differs but cos equal: %d" % len(set(kxky_diff) - set(cos_diff)))
    print("row\tlat_from\tlat_to\ttile_row\tkx_differs\tky_differs")
    for r in kxky_diff:
        lo = r / 10 - 90
        hi = (r + 1) / 10 - 90
        print("%d\t%.1f\t%.1f\t%s\t%s\t%s" % (r, lo, hi, tile_row(lo),
                                               a[r][2] != b[r][2], a[r][3] != b[r][3]))


if __name__ == "__main__":
    main()
