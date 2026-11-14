#!/usr/bin/env bash
# Captures and checks the cosine-row tiles' release (session 73; docs/audits/2026-11-13-brouter-port-closing/
# s73-cos-tiles-capture.md), for .github/workflows/publish-cos-tiles.yml. By analogy with capture.sh, which it does not
# change or call: capture.sh, tiles.tsv and the release they made stay as they are.
#
#   tools/tile-check/capture-cos.sh <out dir> <tile base url> [tile list]
#
# 1. Fetches the oracle (tools/fetch-oracle.sh) and compiles CosTileCheck against it. With COS_ORACLE_ZIP set (local runs
#    only), that file is put where fetch-oracle.sh keeps the zip, so fetch-pinned.sh checks it against the same pins and
#    does not download it.
# 2. Downloads each tile in the list (default cos-tiles.tsv beside this script) once from <tile base url>/<tile>.rd5,
#    keeping the response headers, and checks it in BRouter 1.7.10 with CosTileCheck in a folder of its own: as TileCheck,
#    plus each listed row's node count against a floor and one route per row between ends picked from the tile's own nodes.
#    Nothing printed holds a coordinate. The user agent is capture.sh's unless TILE_UA is set.
# 3. Writes tile-pins.tsv (columns as tools/tile-pins.tsv; each row's counts and the ends digests as "#" lines, which
#    tools/fetch-tiles.sh skips), reads the five tiles back through tools/fetch-tiles.sh against it, and writes the notes.
# Any failure exits non-zero with "REFUSED: <tile>: <reason>" or "REFUSED: <reason>", and nothing is left to publish.
# On success <out> holds assets/ (the five .rd5 files and tile-pins.tsv: exactly six), release-notes.md, headers/ (each
# tile's response headers), and tag (brouter-cos-tiles-<capture date, UTC>).
set -euo pipefail

usage="usage: capture-cos.sh <out dir> <tile base url> [tile list]"
out=${1:?$usage}; base=${2:?$usage}
here=$(cd "$(dirname "$0")" && pwd)
tools=$(dirname "$here")
list=${3:-$here/cos-tiles.tsv}
ua=${TILE_UA:-"ForagerIOS-CI/1.0 (github.com/slayer8366/Forager-iOS)"}
mkdir -p "$out"
out=$(cd "$out" && pwd)
if [ -n "$(ls -A "$out")" ]; then echo "REFUSED: $out is not empty" >&2; exit 1; fi
assets="$out/assets"
mkdir -p "$assets" "$out/headers"
# Beside <out>, so each checked tile can be hard-linked into assets/ (same filesystem): the asset is the checked inode.
work=$(mktemp -d "$(dirname "$out")/cos-tile-check-work.XXXXXX")
# On any failure, assets/ is emptied too, so a failed run leaves nothing that could be uploaded.
trap 'rc=$?; rm -rf "$work"; if [ $rc -ne 0 ]; then rm -rf "$assets"; fi' EXIT

sha256_of() {
  if command -v sha256sum >/dev/null 2>&1; then sha256sum "$1" | cut -d' ' -f1; else shasum -a 256 "$1" | cut -d' ' -f1; fi
}
header() { { grep -i "^$1:" "$2" || true; } | tail -1 | cut -d' ' -f2- | tr -d '\r'; }

# 1. The oracle and the check.
mkdir -p "$work/oracle"
if [ -n "${COS_ORACLE_ZIP:-}" ]; then cp "$COS_ORACLE_ZIP" "$work/oracle/brouter-1.7.10.zip"; fi
"$tools/fetch-oracle.sh" "$work/oracle"
mkdir -p "$work/classes"
javac -cp "$work/oracle/brouter-1.7.10-ro.jar" -d "$work/classes" "$here/CosTileCheck.java"

# 2. The tiles: one download each, then the check.
pins="$assets/tile-pins.tsv"
notes="$work/notes.tsv"
: > "$notes"
{
  run=${GITHUB_RUN_ID:+$GITHUB_SERVER_URL/$GITHUB_REPOSITORY/actions/runs/$GITHUB_RUN_ID}
  echo "# Tile pins written by tools/tile-check/capture-cos.sh at tools commit $(git -C "$here" rev-parse HEAD 2>/dev/null || echo unknown),"
  echo "# in run ${run:-(none: not in Actions)}."
  echo "# Each brouter.de tile was checked in BRouter 1.7.10 by CosTileCheck before its row was written. Columns as tools/tile-pins.tsv."
  printf 'asset\tpath\tbytes\tsha256\tsource\tlast_modified\tetag\tcaptured\n'
} > "$pins"
date=$(date -u +%F)
first=""; captured=""; tiles=0
while IFS=$'\t' read -r tile rows _rest <&3; do
  case "$tile" in '#'* | '' | tile) continue ;; esac
  if [ -z "${rows:-}" ]; then echo "REFUSED: $tile: no rows listed in $list" >&2; exit 1; fi
  mkdir -p "$work/segs/$tile"
  file="$work/segs/$tile/$tile.rd5"
  url="$base/$tile.rd5"
  captured=$(date -u +%FT%TZ)
  first=${first:-$captured}
  if ! curl --fail --silent --show-error --location --user-agent "$ua" \
      --dump-header "$out/headers/$tile.txt" --output "$file" "$url"; then
    echo "REFUSED: $tile: download from $url failed" >&2; exit 1
  fi
  echo "$tile.rd5: downloaded from $url at $captured"
  if ! java -XX:+UnlockDiagnosticVMOptions -XX:-UseLibmIntrinsic -cp "$work/oracle/brouter-1.7.10-ro.jar:$work/classes" \
      CosTileCheck "$work/oracle/profiles2" "$work/segs/$tile" "$tile" "$rows" "$notes"; then
    echo "REFUSED: $tile: CosTileCheck failed (its FAIL line above gives the reason)" >&2; exit 1
  fi
  ln "$file" "$assets/$tile.rd5"
  lm=$(header last-modified "$out/headers/$tile.txt")
  etag=$(header etag "$out/headers/$tile.txt")
  printf '%s\tsegments/%s\t%s\t%s\t%s\t%s\t%s\t%s\n' "$tile.rd5" "$tile.rd5" "$(wc -c < "$file" | tr -d ' ')" \
    "$(sha256_of "$file")" "$url" "${lm:--}" "${etag:--}" "$captured" >> "$pins"
  tiles=$((tiles + 1))
done 3< "$list"
last=$captured
{
  echo "# The check's own counts on these bytes (CosTileCheck: floor 500 nodes per row; 16 attempts at most per row):"
  cat "$notes"
} >> "$pins"

# 3. Exactly five, read back by the reader the publish job and the later pinning use.
rows_written=$(grep -vc -e '^#' -e '^asset' "$pins")
if [ "$tiles" != 5 ] || [ "$rows_written" != 5 ]; then
  echo "REFUSED: $tiles tiles checked and $rows_written rows in $pins, expected 5" >&2; exit 1
fi
"$tools/fetch-tiles.sh" "$work/readback" "file://$assets" "$pins"
count=$(find "$assets" -type f | wc -l | tr -d ' ')
if [ "$count" != 6 ]; then echo "REFUSED: $count files in $assets, expected 6" >&2; exit 1; fi

awk -F'\t' '!/^#/ && $1 != "asset" {
  printf "| `%s` | %s | `%s` | brouter.de, Last-Modified %s |\n", $1, $3, $4, $6 }' "$pins" > "$work/table.md"
awk -v d="$date" -v f="$first" -v l="$last" -v table="$work/table.md" '
  $0 == "@FILES_TABLE@" { while ((getline row < table) > 0) print row; next }
  { gsub(/@CAPTURE_DATE@/, d); gsub(/@CAPTURE_FIRST@/, f); gsub(/@CAPTURE_LAST@/, l); print }' \
  "$tools/tile-release-cos/release-notes.md" > "$out/release-notes.md"
if grep -n '@[A-Z_]*@' "$out/release-notes.md"; then echo "REFUSED: a placeholder is left in the release notes" >&2; exit 1; fi
echo "brouter-cos-tiles-$date" > "$out/tag"

echo "== tag $(cat "$out/tag")"
cat "$pins"
ls -l "$assets"
