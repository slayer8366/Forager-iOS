#!/usr/bin/env python3
"""Part 8 (item E): which of the release zip's profiles switch on each T2 feature.

Reads each .brf's `---context:global` section (up to the next `---context:`), takes every
`assign <name> [=] <value>` there (comments after `#` removed), and gives each feature its effective
value: the profile's literal if it assigns one, otherwise RoutingContext's default
(Forager e2969abe4, third_party/brouter/src/main/java/btools/router/RoutingContext.java):
  validForCars :118 0, validForBikes :119 0, validForFoot :120 0, consider_crossing :122 0,
  considerTurnRestrictions :146 (footMode ? 0 : 1), correctMisplacedViaPoints :148 0,
  continueStraight :151 0, turnInstructionMode :177 0, use_dynamic_range :200 1 (Forager injects 0),
  add_beeline :201 0, processUnusedTags :154 0, pass2coefficient :158 0.
A value that is not a literal (true/false/number) is printed as `expr:<text>` and not resolved.
A name assigned twice in the global section is flagged. Prints a TSV and a summary, and checks
that every .brf in the folder was read (count against `ls`).

Usage: python3 -I profile_features.py <profiles2 dir>
"""
import os
import re
import sys

FEATURES = [
    ("validForCars", "0"), ("validForBikes", "0"), ("validForFoot", "0"),
    ("consider_crossing", "0"), ("considerTurnRestrictions", None),
    ("correctMisplacedViaPoints", "0"), ("continueStraight", "0"),
    ("turnInstructionMode", "0"), ("use_dynamic_range", "1"), ("add_beeline", "0"),
    ("processUnusedTags", "0"), ("pass2coefficient", "0"),
]
ASSIGN = re.compile(r"^\s*assign\s+([A-Za-z_][A-Za-z0-9_]*)\s*=?\s*(.*?)\s*$")


def literal(v):
    v = v.strip()
    if v in ("true", "false"):
        return "1" if v == "true" else "0"
    try:
        f = float(v)
        return "1" if f != 0 and f == 1 else ("0" if f == 0 else v)
    except ValueError:
        return "expr:" + v


def global_assigns(path):
    out, dup, in_global = {}, [], False
    seen_global = [False]
    with open(path, encoding="utf-8", errors="replace") as f:
        for line in f:
            if line.strip().startswith("---context:"):
                # indented context lines occur (mtb.brf:13); the first run missed them, caught by the
                # turnInstructionMode count against grep (24 files assign it, the first run found 23)
                in_global = line.strip().startswith("---context:global")
                seen_global[0] = seen_global[0] or in_global
                continue
            if not in_global:
                continue
            m = ASSIGN.match(line.split("#", 1)[0])
            if m:
                name, val = m.group(1), m.group(2)
                if name in out:
                    dup.append(name)
                out[name] = literal(val)
    if not seen_global[0]:
        raise SystemExit("NO GLOBAL CONTEXT in " + path)
    return out, dup


def main():
    d = sys.argv[1]
    files = sorted(x for x in os.listdir(d) if x.endswith(".brf"))
    print("# profiles read: %d (.brf files in folder: %d)" % (len(files), len([x for x in os.listdir(d) if x.endswith('.brf')])))
    print("profile\t" + "\t".join(n for n, _ in FEATURES) + "\tfootMode\tduplicates")
    rows = []
    for fn in files:
        a, dup = global_assigns(os.path.join(d, fn))
        foot = a.get("validForFoot", "0") == "1"
        vals = []
        for name, default in FEATURES:
            if name in a:
                vals.append(a[name])
            elif name == "considerTurnRestrictions":
                vals.append("(0)" if foot else "(1)")
            else:
                vals.append("(%s)" % default)
        rows.append((fn, vals))
        print(fn + "\t" + "\t".join(vals) + "\t" + ("1" if foot else "0") + "\t" + ",".join(dup))
    print("# values in parentheses are RoutingContext defaults (the profile does not assign the name)")
    print("# summary: profiles where the effective value is on (1 or a non-zero literal)")
    for i, (name, _) in enumerate(FEATURES):
        on = [fn for fn, v in rows if v[i].strip("()") not in ("0",) and not v[i].startswith("expr:")]
        ex = [fn for fn, v in rows if v[i].startswith("expr:")]
        print("%s: on in %d: %s%s" % (name, len(on), ", ".join(on) if on else "-",
                                    ("; unresolved expr in %d: %s" % (len(ex), ", ".join(ex))) if ex else ""))
    both = [fn for fn, v in rows if v[1].strip("()") == "1" and v[3].strip("()") == "1"]
    print("bikeMode and consider_crossing both on: %d: %s" % (len(both), ", ".join(both) if both else "-"))


if __name__ == "__main__":
    main()
