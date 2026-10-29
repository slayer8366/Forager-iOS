#!/usr/bin/env bash
# Fetches the oracle for the port's JVM differential tests (Phase 1 report, item D): BRouter v1.7.10's release zip,
# pinned exactly as Forager pins it (server/pmtiles-worker/test/rd5/fetch-brouter.sh:11-13 at Forager e2969abe4),
# then the read-only router jar and the profiles from inside it, the jar checked against its own pin (item D,
# 06-oracle/logs/jars.txt). Nothing is committed; everything goes in <dir>, which the build puts under build/.
#
#   tools/fetch-oracle.sh <dir>     leaves <dir>/brouter-1.7.10-ro.jar and <dir>/profiles2/
set -euo pipefail

dir=${1:?usage: fetch-oracle.sh <dir>}
here=$(cd "$(dirname "$0")" && pwd)

zip_url=https://github.com/abrensch/brouter/releases/download/v1.7.10/brouter-1.7.10.zip
zip_size=6724983
zip_sha256=023fec3ba997758e8cd7ab9e1bae52e962af3f00b57683e3de86b84ffad01532
jar_size=345776
jar_sha256=598b647f2384a22a43495de7aee44f31e037062ec9c317e3bcc58cf799dbd972

mkdir -p "$dir"
dir=$(cd "$dir" && pwd)
"$here/fetch-pinned.sh" "$dir/brouter-1.7.10.zip" "$zip_url" "$zip_size" "$zip_sha256"

work="$dir/unzipped"
rm -rf "$work" "$dir/profiles2"
unzip -q -o "$dir/brouter-1.7.10.zip" 'brouter-1.7.10/brouter-1.7.10-ro.jar' 'brouter-1.7.10/profiles2/*' -d "$work"
# The jar is checked by the same script, through a file URL, so one checker refuses both.
rm -f "$dir/brouter-1.7.10-ro.jar"
"$here/fetch-pinned.sh" "$dir/brouter-1.7.10-ro.jar" "file://$work/brouter-1.7.10/brouter-1.7.10-ro.jar" \
  "$jar_size" "$jar_sha256"
mv "$work/brouter-1.7.10/profiles2" "$dir/profiles2"
rm -rf "$work"
echo "oracle ready in $dir"
