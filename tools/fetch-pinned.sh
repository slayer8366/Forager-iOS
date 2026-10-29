#!/usr/bin/env bash
# Fetches one file and refuses it unless its size and SHA-256 match the pins given (dispatch 2026-10-28-01, Phase 2
# step 0; Phase 1 report, items D and G). The checks follow Forager's server/pmtiles-worker/test/rd5/fetch-brouter.sh
# (lines 23-28 at Forager e2969abe4): size first, then SHA-256. The file reaches <dest> only when both match.
#
#   tools/fetch-pinned.sh <dest> <url> <size> <sha256>
#
# A <dest> that already matches the pins is kept and not fetched again; one that does not is replaced. <url> may be
# file:///... to check a local copy. Portable to the macOS runners: no GNU-only flags.
set -euo pipefail

dest=${1:?usage: fetch-pinned.sh <dest> <url> <size> <sha256>}
url=${2:?usage: fetch-pinned.sh <dest> <url> <size> <sha256>}
size=${3:?usage: fetch-pinned.sh <dest> <url> <size> <sha256>}
sha256=${4:?usage: fetch-pinned.sh <dest> <url> <size> <sha256>}
name=$(basename "$dest")

sha256_of() {
  if command -v sha256sum >/dev/null 2>&1; then
    sha256sum "$1" | cut -d' ' -f1
  else
    shasum -a 256 "$1" | cut -d' ' -f1
  fi
}

# Prints nothing and returns 0 when $1 matches both pins; otherwise prints why and returns 1.
mismatch() {
  local actual_size actual_sha
  actual_size=$(wc -c < "$1" | tr -d ' ')
  if [ "$actual_size" != "$size" ]; then
    echo "$name is $actual_size bytes, pinned $size"
    return 1
  fi
  actual_sha=$(sha256_of "$1")
  if [ "$actual_sha" != "$sha256" ]; then
    echo "$name has SHA-256 $actual_sha, pinned $sha256"
    return 1
  fi
  return 0
}

if [ -f "$dest" ]; then
  if why=$(mismatch "$dest"); then
    echo "$name: present, matches its pins ($size bytes, SHA-256 $sha256)"
    exit 0
  fi
  echo "$name: present but refused ($why); fetching again" >&2
fi

mkdir -p "$(dirname "$dest")"
part="$dest.part"
rm -f "$part"
curl --fail --silent --show-error --location --retry 3 \
  --user-agent "ForagerIOS-CI/1.0 (github.com/slayer8366/Forager-iOS)" \
  --output "$part" "$url"
if ! why=$(mismatch "$part"); then
  rm -f "$part"
  echo "REFUSED $name from $url: $why" >&2
  exit 1
fi
mv "$part" "$dest"
echo "$name: fetched from $url, matches its pins ($size bytes, SHA-256 $sha256)"
