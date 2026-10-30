#!/usr/bin/env bash
# Fetches the oracle's JDK for e3Strata (RECORD -47): Eclipse Temurin 21.0.13+9, Linux x64, JDK, HotSpot, the JDK CI's
# setup-java gives the oracle (RECORD -46). From Adoptium's own GitHub release of temurin21-binaries, tag jdk-21.0.13+9.
# The pins are Adoptium's published ones, read on 2026-10-30 from two of its own sources, which agree:
#   - the release's .sha256.txt beside the tarball (URL below with .sha256.txt appended);
#   - https://api.adoptium.net/v3/assets/release_name/eclipse/jdk-21.0.13%2B9 (os linux, architecture x64, image_type
#     jdk, jvm_impl hotspot, heap_size normal): package.checksum and package.size; release timestamp 2026-10-22T08:58:06Z.
# The SHA-256 computed from the downloaded tarball equals both. Nothing is committed.
#
#   tools/fetch-temurin.sh <dir>     leaves the tarball in <dir> and the JDK home at <dir>/jdk-21.0.13+9;
#                                    prints that home on its last line. Pass it to e3Strata as -Pbrouter.e3.jdk=<home>.
set -euo pipefail

dir=${1:?usage: fetch-temurin.sh <dir>}
here=$(cd "$(dirname "$0")" && pwd)

name=OpenJDK21U-jdk_x64_linux_hotspot_21.0.13_9.tar.gz
url=https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.13%2B9/$name
size=207858352
sha256=d7f5d5cf865a2902b70ff549b97586a7d3f234e016f041ab447ea740c7d66d1c

mkdir -p "$dir"
"$here/fetch-pinned.sh" "$dir/$name" "$url" "$size" "$sha256"

# Extracted fresh from the pinned tarball every time, into its own empty directory, so the home is the tarball's.
home="$dir/jdk-21.0.13+9"
unpack="$dir/unpack"
rm -rf "$home" "$unpack"
mkdir "$unpack"
tar -xzf "$dir/$name" -C "$unpack"
[ -d "$unpack/jdk-21.0.13+9" ] || { echo "REFUSED $name: no jdk-21.0.13+9/ at its top" >&2; exit 1; }
mv "$unpack/jdk-21.0.13+9" "$home"
rmdir "$unpack"

# The tarball's own release file must name the JDK pinned; a mismatch here means the pin names some other build.
grep -qx 'IMPLEMENTOR="Eclipse Adoptium"' "$home/release" || { echo "REFUSED: release IMPLEMENTOR is not Eclipse Adoptium" >&2; exit 1; }
grep -qx 'JAVA_RUNTIME_VERSION="21.0.13+9-LTS"' "$home/release" || { echo "REFUSED: release JAVA_RUNTIME_VERSION is not 21.0.13+9-LTS" >&2; exit 1; }
echo "Temurin 21.0.13+9 ready"
echo "$home"
