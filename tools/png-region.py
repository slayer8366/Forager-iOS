#!/usr/bin/env python3
"""The A1 pixel check (pre-build section 5, T9): how much of a PNG's middle band is one colour.

Usage: png-region.py FILE --colour RRGGBB [--tolerance N] [--threshold F] [--band F]

Reads an 8-bit RGB or RGBA, non-interlaced PNG (all five row filters) with the standard library only. Takes the middle
--band of the image's height (default 0.8, clear of the status bar and the home indicator), counts the pixels whose
every channel is within --tolerance of --colour (alpha ignored), prints the PNG's header, its ancillary chunk names,
the fraction and the three commonest colours in the band.

Exit status: 0 when the fraction is at least --threshold; 1 when it is below; 2 when the file cannot be read or is a
kind of PNG this reader does not support (said so, never guessed), when the band holds no pixels, and on any exception
at all (review 6 S1: a cut CRC or a short header used to raise and exit 1). A caller that needs "the check failed on the
colour" tests for 1 exactly, so an unreadable file cannot stand in for a failed match.
"""
import argparse
import collections
import struct
import sys
import zlib

SIGNATURE = b"\x89PNG\r\n\x1a\n"
CHANNELS = {2: 3, 6: 4}  # colour type -> channels; the only two supported


class Unsupported(Exception):
    pass


def read_chunks(data):
    if data[:8] != SIGNATURE:
        raise Unsupported("not a PNG (bad signature)")
    pos = 8
    while pos < len(data):
        if pos + 8 > len(data):
            raise Unsupported("truncated chunk header at byte %d" % pos)
        length, ctype = struct.unpack(">I4s", data[pos:pos + 8])
        body = data[pos + 8:pos + 8 + length]
        if len(body) != length:
            raise Unsupported("truncated %s chunk" % ctype.decode("latin-1"))
        crc = struct.unpack(">I", data[pos + 8 + length:pos + 12 + length])[0]
        if zlib.crc32(ctype + body) & 0xFFFFFFFF != crc:
            raise Unsupported("CRC mismatch in %s chunk" % ctype.decode("latin-1"))
        yield ctype.decode("latin-1"), body
        pos += 12 + length
        if ctype == b"IEND":
            return
    raise Unsupported("no IEND chunk")


def paeth(a, b, c):
    p = a + b - c
    pa, pb, pc = abs(p - a), abs(p - b), abs(p - c)
    if pa <= pb and pa <= pc:
        return a
    if pb <= pc:
        return b
    return c


def unfilter(raw, width, height, bpp):
    """Returns the image's rows as bytearrays, the filter byte removed and the filter undone."""
    stride = width * bpp
    if len(raw) != height * (stride + 1):
        raise Unsupported("image data is %d bytes, expected %d" % (len(raw), height * (stride + 1)))
    rows = []
    prev = bytearray(stride)
    for y in range(height):
        start = y * (stride + 1)
        ftype = raw[start]
        line = bytearray(raw[start + 1:start + 1 + stride])
        if ftype == 0:
            pass
        elif ftype == 1:  # Sub
            for i in range(bpp, stride):
                line[i] = (line[i] + line[i - bpp]) & 0xFF
        elif ftype == 2:  # Up
            for i in range(stride):
                line[i] = (line[i] + prev[i]) & 0xFF
        elif ftype == 3:  # Average
            for i in range(stride):
                left = line[i - bpp] if i >= bpp else 0
                line[i] = (line[i] + ((left + prev[i]) >> 1)) & 0xFF
        elif ftype == 4:  # Paeth
            for i in range(stride):
                left = line[i - bpp] if i >= bpp else 0
                upleft = prev[i - bpp] if i >= bpp else 0
                line[i] = (line[i] + paeth(left, prev[i], upleft)) & 0xFF
        else:
            raise Unsupported("row %d has filter type %d" % (y, ftype))
        rows.append(line)
        prev = line
    return rows


def read_png(path):
    """Returns (header dict, ancillary chunk names, rows, channels)."""
    with open(path, "rb") as f:
        data = f.read()
    header = None
    idat = []
    names = []
    for ctype, body in read_chunks(data):
        if ctype == "IHDR":
            w, h, depth, ctype_, comp, filt, interlace = struct.unpack(">IIBBBBB", body)
            header = dict(width=w, height=h, bit_depth=depth, colour_type=ctype_, interlace=interlace)
            if depth != 8:
                raise Unsupported("bit depth %d (only 8 is supported)" % depth)
            if ctype_ not in CHANNELS:
                raise Unsupported("colour type %d (only 2, RGB, and 6, RGBA, are supported)" % ctype_)
            if comp != 0 or filt != 0:
                raise Unsupported("compression method %d, filter method %d" % (comp, filt))
            if interlace != 0:
                raise Unsupported("interlaced (Adam7) PNG")
        elif ctype == "IDAT":
            idat.append(body)
        elif ctype != "IEND":
            names.append(ctype)
    if header is None:
        raise Unsupported("no IHDR chunk")
    if not idat:
        raise Unsupported("no IDAT chunk")
    try:
        raw = zlib.decompress(b"".join(idat))
    except zlib.error as e:
        raise Unsupported("image data does not inflate: %s" % e)
    ch = CHANNELS[header["colour_type"]]
    return header, names, unfilter(raw, header["width"], header["height"], ch), ch


def measure(rows, width, ch, colour, tolerance, band):
    height = len(rows)
    skip = int(round(height * (1.0 - band) / 2.0))
    band_rows = rows[skip:height - skip]
    r0, g0, b0 = colour
    counts = collections.Counter()
    for line in band_rows:
        for x in range(0, width * ch, ch):
            counts[(line[x], line[x + 1], line[x + 2])] += 1
    total = sum(counts.values())
    match = sum(n for (r, g, b), n in counts.items()
                if abs(r - r0) <= tolerance and abs(g - g0) <= tolerance and abs(b - b0) <= tolerance)
    return skip, len(band_rows), total, match, counts.most_common(3)


def parse_colour(s):
    s = s.lower().removeprefix("0x").removeprefix("#")
    if len(s) == 8:  # AARRGGBB, as Compose writes it
        s = s[2:]
    if len(s) != 6:
        raise argparse.ArgumentTypeError("colour must be RRGGBB or AARRGGBB hex, got %r" % s)
    return tuple(int(s[i:i + 2], 16) for i in (0, 2, 4))


def main(argv=None):
    p = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    p.add_argument("file")
    p.add_argument("--colour", required=True, type=parse_colour)
    p.add_argument("--tolerance", type=int, default=0, help="per channel, 0 to 255 (default 0)")
    p.add_argument("--threshold", type=float, default=0.98, help="fraction required to pass (default 0.98)")
    p.add_argument("--band", type=float, default=0.8, help="middle fraction of the height read (default 0.8)")
    a = p.parse_args(argv)
    try:
        return check(a)
    except (Unsupported, OSError) as e:
        print("png-region: UNSUPPORTED or unreadable %s: %s" % (a.file, e))
        return 2
    except Exception as e:  # review 6 S1: anything else is a file not measured, never exit 1
        print("png-region: UNSUPPORTED or unreadable %s: %s: %s" % (a.file, type(e).__name__, e))
        return 2


def check(a):
    header, names, rows, ch = read_png(a.file)
    print("png-region: %s: %dx%d, bit depth %d, colour type %d, interlace %d" % (
        a.file, header["width"], header["height"], header["bit_depth"], header["colour_type"], header["interlace"]))
    print("png-region: ancillary chunks: %s" % (" ".join(names) if names else "none"))
    skip, nrows, total, match, top = measure(rows, header["width"], ch, a.colour, a.tolerance, a.band)
    want = "%02X%02X%02X" % a.colour
    print("png-region: band rows %d to %d (%d rows, %d pixels)" % (skip, skip + nrows - 1, nrows, total))
    if total == 0:
        raise Unsupported("the band holds no pixels (width %d, height %d, band %s)" % (
            header["width"], header["height"], a.band))
    for (r, g, b), n in top:
        print("png-region: common colour %02X%02X%02X: %d pixels (%.4f)" % (r, g, b, n, n / total))
    frac = match / total
    verdict = "PASS" if frac >= a.threshold else "FAIL"
    print("png-region: %s: %d/%d pixels (%.4f) within %d of %s, threshold %.4f" % (
        verdict, match, total, frac, a.tolerance, want, a.threshold))
    return 0 if verdict == "PASS" else 1

if __name__ == "__main__":
    sys.exit(main())
