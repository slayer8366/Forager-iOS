#!/usr/bin/env bash
# Fetches every tile in tools/tile-pins.tsv into <dir> and refuses any whose size or SHA-256 differs from its pin
# (dispatch 2026-10-28-01, Amendment 2, Phase 2 step 1; Phase 1 report, items E4 and I). Each row goes through
# tools/fetch-pinned.sh, so one checker refuses the oracle and the tiles alike.
#
#   tools/fetch-tiles.sh <dir> <base url> [pin file]
#
# <base url> is the folder the assets are served from, with no trailing slash: the Forager-iOS release
# (https://github.com/slayer8366/Forager-iOS/releases/download/brouter-tiles-2026-10-30, RECORD -45; the build's
# fetchTiles task in brouter/build.gradle.kts passes it), or file:///... for a local copy.
# Each row's asset is fetched from <base url>/<asset> and lands at <dir>/<path>. brouter.de rewrites its tiles in place
# (Phase 1, item F), so the release is the only source of the pinned bytes.
#
# tools/tile-pins.tsv is a byte-for-byte copy of that release's own tile-pins.tsv asset (SHA-256 ccfc380e...; Phase 2
# step 1 pinning, RECORD -45). Its columns, tab-separated, one row per data asset: asset, the release asset's name |
# path, where the fetch puts it under <dir> (the engine finds a tile by its file name, so F0's fixture keeps W75_N40.rd5
# in a folder of its own, f0/) | bytes | sha256 | source, provenance only and never fetched | last_modified and etag,
# brouter.de's response headers ("-" for F0's fixture) | captured, UTC. Only the first four are read here.
set -euo pipefail

dir=${1:?usage: fetch-tiles.sh <dir> <base url> [pin file]}
base=${2:?usage: fetch-tiles.sh <dir> <base url> [pin file]}
here=$(cd "$(dirname "$0")" && pwd)
pins=${3:-$here/tile-pins.tsv}

mkdir -p "$dir"
dir=$(cd "$dir" && pwd)
count=0
while IFS=$'\t' read -r asset path bytes sha256 _rest; do
  case "$asset" in '#'* | '' | asset) continue ;; esac
  "$here/fetch-pinned.sh" "$dir/$path" "$base/$asset" "$bytes" "$sha256"
  count=$((count + 1))
done < "$pins"
if [ "$count" -eq 0 ]; then
  echo "REFUSED: no pinned rows read from $pins" >&2
  exit 1
fi
echo "$count tiles ready in $dir"
