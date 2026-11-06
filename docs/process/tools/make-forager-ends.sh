#!/usr/bin/env bash
# Rebuilds the list of Forager's own published route ends that the E1 scan excepts (RECORD -93), from Forager's own files
# at a0535ece, so no coordinate list is committed here. Usage: make-forager-ends.sh <forager clone> > forager-ends.txt
set -euo pipefail
G=${1:?usage: make-forager-ends.sh <forager clone>}
for f in server/pmtiles-worker/test/fixtures/rd5/routes.json \
         server/pmtiles-worker/test/fixtures/rd5/two-squares/routes.json \
         app/src/test/resources/routing/dropoff/points.json; do
  git -C "$G" show "a0535ece:$f"
done | grep -oE -- '-?[0-9]{1,3}\.[0-9]{5,}' | sort -u
