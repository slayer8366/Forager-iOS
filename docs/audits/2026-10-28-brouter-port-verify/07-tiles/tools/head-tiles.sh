#!/usr/bin/env bash
# Item F: HTTP HEAD only, on a few rd5 tiles at brouter.de. No GET of any .rd5 file.
# Usage: head-tiles.sh <out file> <tile name>...
set -u
out="$1"; shift
base="https://brouter.de/brouter/segments4"
{
  echo "# run at $(date -u +%Y-%m-%dT%H:%M:%SZ), curl --head (HEAD method), no redirects followed"
  for t in "$@"; do
    echo "## $t"
    curl -sS --head --max-time 30 "$base/$t" | tr -d '\r'
  done
} >> "$out"
