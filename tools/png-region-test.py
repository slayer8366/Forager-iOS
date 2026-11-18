#!/usr/bin/env python3
"""Self-test for tools/png-region.py on synthetic PNGs made here, standard library only. Runs on Linux (app.yml's
png-region job). Usage: python3 tools/png-region-test.py. Prints one line per case and exits 1 if any case fails.

The PNGs are written by an encoder in this file that applies each of the five row filters forward, so the reader's
inverse filters are checked against pixels known in advance, byte for byte, including filter type 3 (Average) on rows
whose left and upper neighbours are not zero.
"""
import importlib.util
import os
import random
import struct
import subprocess
import sys
import tempfile
import zlib

HERE = os.path.dirname(os.path.abspath(__file__))
TOOL = os.path.join(HERE, "png-region.py")
_spec = importlib.util.spec_from_file_location("png_region", TOOL)
png_region = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(png_region)

APP = (0xE0, 0x21, 0x8A)
WHITE = (0xFF, 0xFF, 0xFF)


def chunk(ctype, body):
    return struct.pack(">I", len(body)) + ctype + body + struct.pack(">I", zlib.crc32(ctype + body) & 0xFFFFFFFF)


def filter_row(ftype, line, prev, bpp):
    out = bytearray(len(line))
    for i in range(len(line)):
        left = line[i - bpp] if i >= bpp else 0
        up = prev[i]
        upleft = prev[i - bpp] if i >= bpp else 0
        if ftype == 0:
            pred = 0
        elif ftype == 1:
            pred = left
        elif ftype == 2:
            pred = up
        elif ftype == 3:
            pred = (left + up) >> 1
        else:
            pred = png_region.paeth(left, up, upleft)
        out[i] = (line[i] - pred) & 0xFF
    return out


def encode(pixels, width, height, alpha=False, filters=(0,), depth=8, colour_type=None, interlace=0, extra=()):
    """pixels: list of rows, each a list of (r, g, b) tuples. Row y uses filters[y % len(filters)]."""
    bpp = 4 if alpha else 3
    raw = bytearray()
    prev = bytearray(width * bpp)
    for y in range(height):
        line = bytearray()
        for px in pixels[y]:
            line += bytes(px) + (b"\xff" if alpha else b"")
        ftype = filters[y % len(filters)]
        raw.append(ftype)
        raw += filter_row(ftype, line, prev, bpp)
        prev = line
    ct = colour_type if colour_type is not None else (6 if alpha else 2)
    ihdr = struct.pack(">IIBBBBB", width, height, depth, ct, 0, 0, interlace)
    out = png_region.SIGNATURE + chunk(b"IHDR", ihdr)
    for ctype, body in extra:
        out += chunk(ctype, body)
    return out + chunk(b"IDAT", zlib.compress(bytes(raw))) + chunk(b"IEND", b"")


def solid(colour, w, h):
    return [[colour] * w for _ in range(h)]


results = []


def case(name, ok, detail=""):
    results.append(ok)
    print("%s %s%s" % ("ok  " if ok else "FAIL", name, (": " + detail) if detail else ""))


def write(tmp, name, data):
    path = os.path.join(tmp, name)
    with open(path, "wb") as f:
        f.write(data)
    return path


def run(path, *args):
    p = subprocess.run([sys.executable, "-I", TOOL, path] + list(args), capture_output=True, text=True)
    return p.returncode, p.stdout + p.stderr


def main():
    rnd = random.Random(20261118)
    with tempfile.TemporaryDirectory() as tmp:
        # 1. Decoding, byte for byte: random pixels, each filter type alone and all five mixed, RGB and RGBA.
        w, h = 37, 23
        pixels = [[tuple(rnd.randrange(256) for _ in range(3)) for _ in range(w)] for _ in range(h)]
        for alpha in (False, True):
            for filters in ((0,), (1,), (2,), (3,), (4,), (0, 1, 2, 3, 4), (3, 4, 1, 2, 0)):
                kind = "RGBA" if alpha else "RGB"
                path = write(tmp, "rand.png", encode(pixels, w, h, alpha, filters))
                header, _, rows, ch = png_region.read_png(path)
                got = [[tuple(r[x:x + 3]) for x in range(0, w * ch, ch)] for r in rows]
                bad = sum(1 for y in range(h) for x in range(w) if got[y][x] != pixels[y][x])
                case("decode %s filters %s" % (kind, filters), bad == 0 and header["colour_type"] == (6 if alpha else 2),
                     "%d of %d pixels differ" % (bad, w * h))

        # 2. Verdicts through the command line: exit status and the verdict line.
        for alpha in (False, True):
            for f in range(5):
                path = write(tmp, "app.png", encode(solid(APP, 40, 50), 40, 50, alpha, (f,)))
                code, out = run(path, "--colour", "0xFFE0218A")
                case("solid app colour, %s, filter %d passes at tolerance 0" % ("RGBA" if alpha else "RGB", f),
                     code == 0 and "PASS: 1600/1600" in out and "common colour E0218A: 1600 pixels" in out, out.strip().splitlines()[-1])
        path = write(tmp, "white.png", encode(solid(WHITE, 40, 50), 40, 50, False, (3,)))
        code, out = run(path, "--colour", "E0218A", "--tolerance", "24")
        case("white fails with exit 1", code == 1 and "FAIL: 0/1600" in out and "common colour FFFFFF" in out, out.strip().splitlines()[-1])
        half = [[APP if x < 20 else WHITE for x in range(40)] for _ in range(50)]
        path = write(tmp, "half.png", encode(half, 40, 50, True, (0, 1, 2, 3, 4)))
        code, out = run(path, "--colour", "E0218A")
        case("half app colour fails with exit 1", code == 1 and "FAIL: 800/1600" in out, out.strip().splitlines()[-1])
        near = (0xE2, 0x1F, 0x8C)  # two units off in every channel
        path = write(tmp, "near.png", encode(solid(near, 40, 50), 40, 50, False, (4,)))
        code4, out4 = run(path, "--colour", "E0218A", "--tolerance", "2")
        code1, out1 = run(path, "--colour", "E0218A", "--tolerance", "1")
        case("two units off passes at tolerance 2, fails at 1", code4 == 0 and code1 == 1,
             "%d / %d" % (code4, code1))
        # The band: 50 rows, band 0.8 skips 5 rows at each end. Outside it white, inside the colour.
        banded = [[WHITE] * 40 if y < 5 or y >= 45 else [APP] * 40 for y in range(50)]
        path = write(tmp, "band.png", encode(banded, 40, 50, False, (3,)))
        code, out = run(path, "--colour", "E0218A")
        case("white outside the middle band is not read", code == 0 and "band rows 5 to 44 (40 rows, 1600 pixels)" in out,
             out.strip().splitlines()[-1])
        inv = [[APP] * 40 if y < 5 or y >= 45 else [WHITE] * 40 for y in range(50)]
        path = write(tmp, "inv.png", encode(inv, 40, 50, False, (3,)))
        code, out = run(path, "--colour", "E0218A")
        case("the colour only outside the band fails", code == 1 and "FAIL: 0/1600" in out, out.strip().splitlines()[-1])
        three = [[APP] * 20 + [WHITE] * 15 + [(0, 0, 0)] * 5 for _ in range(50)]
        path = write(tmp, "three.png", encode(three, 40, 50, False, (2,)))
        code, out = run(path, "--colour", "E0218A", "--band", "1")
        lines = [l for l in out.splitlines() if "common colour" in l]
        case("the three commonest colours, in order", lines == [
            "png-region: common colour E0218A: 1000 pixels (0.5000)",
            "png-region: common colour FFFFFF: 750 pixels (0.3750)",
            "png-region: common colour 000000: 250 pixels (0.1250)"], " | ".join(lines))
        path = write(tmp, "chunks.png", encode(solid(APP, 4, 4), 4, 4, extra=((b"sRGB", b"\x00"),)))
        code, out = run(path, "--colour", "E0218A", "--band", "1")
        case("ancillary chunk names are printed", "ancillary chunks: sRGB" in out, "")

        # 3. Unsupported kinds exit 2, never 0 or 1.
        good = encode(solid(APP, 4, 4), 4, 4)
        bad_crc = bytearray(good)
        bad_crc[-20] ^= 0xFF  # inside IDAT's body
        for name, data in (
            ("16-bit", encode(solid(APP, 4, 4), 4, 4, depth=16)),
            ("palette", encode(solid(APP, 4, 4), 4, 4, colour_type=3)),
            ("interlaced", encode(solid(APP, 4, 4), 4, 4, interlace=1)),
            ("bad CRC", bytes(bad_crc)),
            ("not a PNG", b"GIF89a" + b"\x00" * 40),
        ):
            path = write(tmp, "u.png", data)
            code, out = run(path, "--colour", "E0218A")
            case("%s is refused with exit 2" % name, code == 2 and "UNSUPPORTED" in out, out.strip().splitlines()[-1])
        code, out = run(os.path.join(tmp, "missing.png"), "--colour", "E0218A")
        case("a missing file is refused with exit 2", code == 2, out.strip())

    failed = results.count(False)
    print("png-region-test: %d cases, %d failed" % (len(results), failed))
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main())
