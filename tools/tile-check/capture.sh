#!/usr/bin/env bash
# Captures and checks the tile release's files (dispatch 2026-10-28-01, Amendment 4), for .github/workflows/publish-tiles.yml.
#
#   tools/tile-check/capture.sh <out dir> <tile base url> <F0 url> <F0 bytes> <F0 sha256>
#
# 1. Fetches the oracle (tools/fetch-oracle.sh) and Forager's fixture F0, refused unless it matches the pins given. F0
#    comes first, so a wrong pin fails before any large download.
# 2. Downloads each tile in tiles.tsv once from <tile base url>/<tile>.rd5, keeping the response headers, and checks it
#    in BRouter 1.7.10 with TileCheck, in a folder of its own: lookup version, the version-checked open,
#    checkFileIntegrity, every cell decoded, one route. Then F0 the same way, as step 1's control.
# 3. Writes tile-pins.tsv from the checked files, fills the README's table and the notes' date from it, and reads the five
#    data files back through tools/fetch-tiles.sh against it.
# Any failure exits non-zero. On success <out> holds assets/ (the five .rd5 files, README.md, tile-pins.tsv),
# release-notes.md, headers/ (each tile's response headers), and tag (brouter-tiles-<capture date, UTC>).
set -euo pipefail

usage="usage: capture.sh <out dir> <tile base url> <F0 url> <F0 bytes> <F0 sha256>"
out=${1:?$usage}; base=${2:?$usage}; f0_url=${3:?$usage}; f0_bytes=${4:?$usage}; f0_sha256=${5:?$usage}
here=$(cd "$(dirname "$0")" && pwd)
tools=$(dirname "$here")
mkdir -p "$out"
out=$(cd "$out" && pwd)
if [ -n "$(ls -A "$out")" ]; then echo "REFUSED: $out is not empty" >&2; exit 1; fi
assets="$out/assets"
mkdir -p "$assets" "$out/headers"
work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT

sha256_of() {
  if command -v sha256sum >/dev/null 2>&1; then sha256sum "$1" | cut -d' ' -f1; else shasum -a 256 "$1" | cut -d' ' -f1; fi
}
header() { { grep -i "^$1:" "$2" || true; } | tail -1 | cut -d' ' -f2- | tr -d '\r'; }

# 1. The oracle and F0.
"$tools/fetch-oracle.sh" "$work/oracle"
f0=forager-fixture-W75_N40.rd5
"$tools/fetch-pinned.sh" "$assets/$f0" "$f0_url" "$f0_bytes" "$f0_sha256"
f0_captured=$(date -u +%FT%TZ)
mkdir -p "$work/classes"
javac -cp "$work/oracle/brouter-1.7.10-ro.jar" -d "$work/classes" "$here/TileCheck.java"
check() { # <segments dir> <tile> <lat1> <lon1> <lat2> <lon2>; exits non-zero on any failed check
  java -XX:+UnlockDiagnosticVMOptions -XX:-UseLibmIntrinsic \
    -cp "$work/oracle/brouter-1.7.10-ro.jar:$work/classes" TileCheck "$work/oracle/profiles2" "$@"
}

# 2. The four tiles: one download each, then the check.
pins="$assets/tile-pins.tsv"
{
  run=${GITHUB_RUN_ID:+$GITHUB_SERVER_URL/$GITHUB_REPOSITORY/actions/runs/$GITHUB_RUN_ID}
  echo "# Tile pins written by tools/tile-check/capture.sh at tools commit $(git -C "$here" rev-parse HEAD 2>/dev/null || echo unknown),"
  echo "# in run ${run:-(none: not in Actions)}."
  echo "# Each brouter.de tile was checked in BRouter 1.7.10 before its row was written. Columns as tools/tile-pins.tsv."
  printf 'asset\tpath\tbytes\tsha256\tsource\tlast_modified\tetag\tcaptured\n'
} > "$pins"
date=$(date -u +%F)
first=""
while IFS=$'\t' read -r tile lat1 lon1 lat2 lon2 <&3; do
  case "$tile" in '#'* | '' | tile) continue ;; esac
  [ "$tile" = W75_N40 ] && f0_ends=("$lat1" "$lon1" "$lat2" "$lon2")
  mkdir -p "$work/segs/$tile"
  file="$work/segs/$tile/$tile.rd5"
  url="$base/$tile.rd5"
  captured=$(date -u +%FT%TZ)
  first=${first:-$captured}
  curl --fail --silent --show-error --location \
    --user-agent "ForagerIOS-CI/1.0 (github.com/slayer8366/Forager-iOS)" \
    --dump-header "$out/headers/$tile.txt" --output "$file" "$url"
  echo "$tile.rd5: downloaded from $url at $captured"
  check "$work/segs/$tile" "$tile" "$lat1" "$lon1" "$lat2" "$lon2"
  ln "$file" "$assets/$tile.rd5"
  lm=$(header last-modified "$out/headers/$tile.txt")
  etag=$(header etag "$out/headers/$tile.txt")
  printf '%s\tsegments/%s\t%s\t%s\t%s\t%s\t%s\t%s\n' "$tile.rd5" "$tile.rd5" "$(wc -c < "$file" | tr -d ' ')" \
    "$(sha256_of "$file")" "$url" "${lm:--}" "${etag:--}" "$captured" >> "$pins"
done 3< "$here/tiles.tsv"
last=$captured

# F0, the control: between W75_N40's ends, as in step 1.
echo "F0 ($f0), the control, saved as W75_N40.rd5 in a folder of its own:"
mkdir -p "$work/segs/F0"
cp "$assets/$f0" "$work/segs/F0/W75_N40.rd5"
check "$work/segs/F0" W75_N40 "${f0_ends[@]}"
printf '%s\tf0/W75_N40.rd5\t%s\t%s\t%s\t-\t-\t%s\n' "$f0" "$(wc -c < "$assets/$f0" | tr -d ' ')" \
  "$(sha256_of "$assets/$f0")" "$f0_url" "$f0_captured" >> "$pins"

# 3. The README's table and the notes' date, from the pins.
awk -F'\t' '!/^#/ && $1 != "asset" {
  src = ($6 == "-") ? "Forager'"'"'s test fixture (below)" : "brouter.de, Last-Modified " $6
  printf "| `%s` | %s | `%s` | %s |\n", $1, $3, $4, src }' "$pins" > "$work/table.md"
fill() { # <template> <dest>
  awk -v d="$date" -v f="$first" -v l="$last" -v table="$work/table.md" '
    $0 == "@FILES_TABLE@" { while ((getline row < table) > 0) print row; next }
    { gsub(/@CAPTURE_DATE@/, d); gsub(/@CAPTURE_FIRST@/, f); gsub(/@CAPTURE_LAST@/, l); print }' "$1" > "$2"
  if grep -n '@[A-Z_]*@' "$2"; then echo "REFUSED: a placeholder is left in $2" >&2; exit 1; fi
}
fill "$tools/tile-release/README.md" "$assets/README.md"
fill "$tools/tile-release/release-notes.md" "$out/release-notes.md"
echo "brouter-tiles-$date" > "$out/tag"

# The five data files, read back by the reader the publish job and the later pinning use.
rows=$(grep -vc -e '^#' -e '^asset' "$pins")
if [ "$rows" != 5 ]; then echo "REFUSED: $rows rows in $pins, expected 5" >&2; exit 1; fi
"$tools/fetch-tiles.sh" "$work/readback" "file://$assets" "$pins"
echo "== tag $(cat "$out/tag")"
cat "$pins"
ls -l "$assets"
