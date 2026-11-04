#!/usr/bin/env bash
# Fetches stratum F0h's fixture (RECORD -58): Forager's segments-heighted/W75_N40.rd5, the route cutter's Harriman
# square with 3" heights, read at Forager commit d6e2a0415 from raw.githubusercontent.com. A commit-addressed raw file is
# as fixed as a release asset, and publish-tiles fetched F0 itself that way (F0_URL, .github/workflows/publish-tiles.yml).
# It is not in the tile release and not in tools/tile-pins.tsv: a new release needs the owner's publish-tiles run
# (RECORD -58, its second correction to -57). The pins are Forager's own (app/src/test/resources/routing/README.md at
# d6e2a0415), and equal the blob at that commit. Nothing is committed.
#
#   tools/fetch-f0h.sh <dir>     leaves <dir>/f0h/W75_N40.rd5, alone in its folder
set -euo pipefail

dir=${1:?usage: fetch-f0h.sh <dir>}
here=$(cd "$(dirname "$0")" && pwd)

url=https://raw.githubusercontent.com/slayer8366/Forager/d6e2a0415e5fc50a43210f78120ceedb9e2e6de4/app/src/test/resources/routing/segments-heighted/W75_N40.rd5
size=178867
sha256=14f595e8469455bcdcc600239102cb7d60641185759fcae29cfed444af8a965c

mkdir -p "$dir/f0h"
"$here/fetch-pinned.sh" "$dir/f0h/W75_N40.rd5" "$url" "$size" "$sha256"
echo "F0h's fixture ready in $dir/f0h"
