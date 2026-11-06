#!/usr/bin/env bash
# Fetches Forager's drop-off fixture for step 5d's route-ways test (RECORD -65, -70 item 4, -74): the drop-off square
# segments/W75_N40.rd5 and its points.json, read at Forager commit a0535ece from raw.githubusercontent.com, each refused
# unless its size and SHA-256 match the pins below (computed from the blobs at that commit with
# `git show a0535ece:<path> | wc -c` and `| sha256sum`). Not in the tile release or tools/tile-pins.tsv. Nothing is
# committed. Pattern of tools/fetch-f0h.sh.
#
#   tools/fetch-dropoff.sh <dir>   leaves <dir>/dropoff/segments/W75_N40.rd5 (alone in its folder) and <dir>/dropoff/points.json
set -euo pipefail

dir=${1:?usage: fetch-dropoff.sh <dir>}
here=$(cd "$(dirname "$0")" && pwd)

base=https://raw.githubusercontent.com/slayer8366/Forager/a0535ece18dd27b4f86ebc311f201089868b33ed/app/src/test/resources/routing/dropoff

mkdir -p "$dir/dropoff/segments"
"$here/fetch-pinned.sh" "$dir/dropoff/segments/W75_N40.rd5" "$base/segments/W75_N40.rd5" 4475 \
  a332b322768cdfddf9dcc69714e762f362d1d2258a9bf1d68b2fc72b2978d8f7
"$here/fetch-pinned.sh" "$dir/dropoff/points.json" "$base/points.json" 1103 \
  2872493a9d8ee66df13c21b7d21f031fc45604076493988427b4e815a0536b5a
echo "the drop-off fixture ready in $dir/dropoff"
