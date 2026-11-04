#!/usr/bin/env python3
"""The expected values of stratum F0h (RECORD -58): from each GeoJSON BRouter's command-line router wrote, the point
count and E3's heights digest, so that only counts and digests are committed, never coordinates (Phase 1 report, E1).

    python3 -I tools/geojson-heights.py <file.geojson>...

Prints one tab-separated line per file: name, points, points with a height, the digest, the lowest and highest height.

How the GeoJSON writes a height (FormatJson.java:109 in BRouter 1.7.10, Forager's third_party copy at d6e2a0415):
`n.getSElev() == Short.MIN_VALUE ? "" : ", " + n.getElev()`, and getElev() is `selev / 4.` (OsmPathElement.java:43-44).
So a point has a third value only when it has a height, and that value is the engine's short in quarter metres divided
by four, written by Java's Double.toString. A quarter is exact in binary, and Double.toString writes a double that
reads back to itself, so the rule used here is: selev = the third value times 4, which must be a whole number in a
short's range (checked exactly, in decimal, and refused otherwise); a point with no third value is Short.MIN_VALUE.
(The engine writes speed there instead when the profile sets `showspeed`, FormatJson.java:110; the router's README in
Forager says these files carry heights, and every value is checked to be a whole number of quarter metres.)

The digest is E3Outcome.fnv1aShorts (brouter/src/jvmTest/.../oracle/e3/E3Case.kt): 64-bit FNV-1a over each short's two
bytes, low byte first, printed as Java's Long.toHexString prints it (unsigned hex, no leading zeros).
"""
import json
import os
import sys
from decimal import Decimal

SHORT_MIN = -32768
MASK = (1 << 64) - 1


def fnv1a_shorts(values):
    h = 0xcbf29ce484222325
    for s in values:
        v = s & 0xffff
        for shift in (0, 8):
            h ^= (v >> shift) & 0xff
            h = (h * 0x100000001b3) & MASK
    return format(h, "x")


def selev_of(point):
    if len(point) < 3:
        return SHORT_MIN
    if len(point) != 3:
        raise SystemExit(f"a point has {len(point)} values: {point}")
    q = point[2] * 4
    if q != q.to_integral_value():
        raise SystemExit(f"height {point[2]} is not a whole number of quarter metres")
    q = int(q)
    if not (SHORT_MIN < q <= 32767):
        raise SystemExit(f"height {point[2]} is {q} quarter metres, outside a short (or the no-height marker)")
    return q


def main(paths):
    if not paths:
        raise SystemExit(__doc__)
    for path in paths:
        with open(path, encoding="utf-8") as f:
            doc = json.load(f, parse_float=Decimal)
        features = doc["features"]
        if len(features) != 1:
            raise SystemExit(f"{path}: {len(features)} features, not 1")
        coords = features[0]["geometry"]["coordinates"]
        selevs = [selev_of(p) for p in coords]
        heighted = [s for s in selevs if s != SHORT_MIN]
        lo = min(heighted) / 4 if heighted else "-"
        hi = max(heighted) / 4 if heighted else "-"
        name = os.path.basename(path).removesuffix(".geojson")
        print(f"{name}\t{len(selevs)}\t{len(heighted)}\t{fnv1a_shorts(selevs)}\t{lo}\t{hi}")


if __name__ == "__main__":
    main(sys.argv[1:])
