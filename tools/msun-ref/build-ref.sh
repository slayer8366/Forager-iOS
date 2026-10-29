#!/usr/bin/env bash
# The C reference for step 2b (dispatch 2026-10-28-01, Amendment 3, Part B): bionic's FreeBSD msun cos and atan2 at
# android-17.0.0_r1 (bionic 06356e41), built with Android's flags as research/android-math's Q7 built them, and run over
# the same inputs. Run by the build before jvmTest (brouter/build.gradle.kts, msunReference), locally and in CI.
#
#   tools/msun-ref/build-ref.sh <outdir>
#
# Writes <outdir>/msun-cos.txt (tab.c over the 1,800 table latitudes), <outdir>/msun-atan2.txt (at2.c over the
# 299,974 pairs Gen.java regenerates) and <outdir>/msun-edges.txt (edges.c over edges.txt). Exits non-zero, and writes
# no outputs, unless: every source matches its pin; clang and gcc both build it; their outputs are byte for byte equal;
# and msun-cos.txt and msun-atan2.txt have the SHA-256 the research recorded for its own build (q7/results/
# sha256-and-wc.txt at research/android-math 736a4c1), which is the check from outside this script.
#
# tab.c, at2.c, Gen.java, TableRows.java and the three headers are research/android-math's (q7/tools/ at 736a4c1),
# unchanged. edges.c and edges.txt are this step's.
set -euo pipefail

here=$(cd "$(dirname "$0")" && pwd)
out=${1:?usage: build-ref.sh <outdir>}
base=https://android.googlesource.com/platform/bionic/+/06356e41c5ed7b12220b24c05ba4fdb873b126a7
cos_sha=068e61ebdac53bce65fc23ab0af023ba9048375484287d14b8c1949eaad7a5c7
atan2_sha=466fb82d7bccec4d017c61fa86ad4d0d467c3f3281e5cc646ff818d1fbd43dfa

sha256_of() {
  if command -v sha256sum >/dev/null 2>&1; then sha256sum "$1" | cut -d' ' -f1; else shasum -a 256 "$1" | cut -d' ' -f1; fi
}

work="$out/work"
rm -rf "$work" "$out/msun-cos.txt" "$out/msun-atan2.txt" "$out/msun-edges.txt"
mkdir -p "$work/src" "$work/inc/machine" "$work/dl"

# The sources: fetched as base64 (?format=TEXT), decoded, and refused unless size and SHA-256 match sources.tsv.
grep -v '^#' "$here/sources.tsv" | while IFS=$'\t' read -r name size sha path; do
  curl -sS --fail -o "$work/dl/$name.b64" "$base/$path?format=TEXT"
  base64 -d < "$work/dl/$name.b64" > "$work/dl/$name"
  bash "$here/../fetch-pinned.sh" "$work/src/$name" "file://$work/dl/$name" "$size" "$sha"
done
cp "$here/machine-endian.h" "$work/inc/machine/endian.h"

# The inputs: TableRows.java writes the table (its column 2 is lat, computed as CheapRuler computes it); Gen.java
# regenerates the pairs from ProbeTest.kt's LCG.
java "$here/TableRows.java" "$work/jvm-math.txt" "$work/jvm-strict.txt"
java "$here/Gen.java" "$work/jvm-pairs.txt"

libm="s_cos.c k_cos.c k_sin.c k_rem_pio2.c e_atan2.c s_atan.c"
for cc in clang gcc; do
  b="$work/b-$cc"; mkdir -p "$b"
  echo "## $cc: $($cc --version | head -1)"
  objs=""
  for f in $libm; do
    "$cc" -c -O2 -ffp-contract=off -fno-builtin -fno-math-errno -I"$work/inc" -I"$work/src" \
      -include "$here/bionic-shim.h" -include "$work/src/freebsd-compat.h" -include "$here/bionic-shim2.h" \
      -o "$b/${f%.c}.o" "$work/src/$f"
    objs="$objs $b/${f%.c}.o"
  done
  for h in tab at2 edges; do
    # shellcheck disable=SC2086
    "$cc" -O2 -ffp-contract=off -fno-builtin -fno-math-errno -o "$b/$h" "$here/$h.c" $objs -lm
  done
  nm "$b/tab" | grep -E ' T (cos|__kernel_cos|__kernel_sin|__kernel_rem_pio2)$'
  nm "$b/at2" | grep -E ' T (atan2|atan)$'
  "$b/tab" "$work/jvm-strict.txt" > "$b/msun-cos.txt"
  "$b/at2" "$work/jvm-pairs.txt" > "$b/msun-atan2.txt"
  "$b/edges" "$here/edges.txt" > "$b/msun-edges.txt"
done

for f in msun-cos.txt msun-atan2.txt msun-edges.txt; do
  cmp "$work/b-clang/$f" "$work/b-gcc/$f"
done
echo "clang and gcc outputs equal"
wc -l "$work/b-clang/"msun-*.txt
got=$(sha256_of "$work/b-clang/msun-cos.txt")
[ "$got" = "$cos_sha" ] || { echo "msun-cos.txt SHA-256 $got, the research's $cos_sha"; exit 1; }
got=$(sha256_of "$work/b-clang/msun-atan2.txt")
[ "$got" = "$atan2_sha" ] || { echo "msun-atan2.txt SHA-256 $got, the research's $atan2_sha"; exit 1; }
echo "msun-cos.txt and msun-atan2.txt equal the research's SHA-256"
cp "$work/b-clang/msun-cos.txt" "$work/b-clang/msun-atan2.txt" "$work/b-clang/msun-edges.txt" "$out/"
