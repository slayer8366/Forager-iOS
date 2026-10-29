#!/usr/bin/env bash
# Kotlin metadata compatibility probe (Forager-iOS dispatch 2026-10-28-01, item C, part 3).
# Producers: a KMP library compiled by several Kotlin versions, published to a file repo.
# Consumers: a Kotlin/JVM build (jvmTarget 17, as Forager's app) compiling and running against each.
# Every consumer run starts with `clean`, so no earlier run's output can be read as this one's.
set -u
K="$(cd "$(dirname "$0")" && pwd)"
G=/root/.gradle/wrapper/dists/gradle-9.7.0-bin/d4tj7w02tcgubx9zk9hbippn6/gradle-9.7.0/bin/gradle
REPO="$K/repo"; LOGS="$K/logs"; mkdir -p "$LOGS"
SUM="$LOGS/summary.txt"
{
echo "# Kotlin metadata probe, started $(date -u +%Y-%m-%dT%H:%M:%SZ), Gradle $($G --version 2>/dev/null | grep '^Gradle')"
echo "# Predictions, written before any run:"
echo "#  - consumer 2.3.10 reads producers 2.3.10, 2.3.21, 2.4.21, and 2.4.21 pinned to language/api 2.3"
echo "#  - negative control consumer 2.2.21: reads 2.3.10 (one ahead), FAILS on 2.4.21 (two ahead) with a metadata-version error,"
echo "#    reads 2.4.21 pinned to language/api 2.3"
echo "#  - producer 2.4.21 unpinned names kotlin-stdlib 2.4.21, so the 2.3.10 consumer's runtime stdlib moves to 2.4.21"
} > "$SUM"
produce() { # tag kv extra-props...
  local tag=$1 kv=$2; shift 2
  local log="$LOGS/producer-$tag.log"
  (cd "$K/producer" && rm -rf build .gradle/kotlin && $G --console=plain clean publishAllPublicationsToProbeRepoRepository -Pkv=$kv -Ptag=$tag -Prepo="$REPO" "$@") >"$log" 2>&1
  echo "producer $tag (kotlin $kv $*): exit $?  $(grep -c 'BUILD SUCCESSFUL' "$log") success-lines; jar: $(ls "$REPO/probe/portprobe-jvm/$tag/" 2>/dev/null | grep -c '\.jar$') ; pom stdlib: $(grep -A1 '<artifactId>kotlin-stdlib' "$REPO/probe/portprobe-jvm/$tag/portprobe-jvm-$tag.pom" 2>/dev/null | grep -o '<version>[^<]*' | sed 's/<version>//')" >> "$SUM"
}
consume() { # name kv tag extra...
  local name=$1 kv=$2 tag=$3; shift 3
  local log="$LOGS/consumer-$name.log"
  (cd "$K/consumer" && rm -rf build .gradle/kotlin && $G --console=plain clean run -Pkv=$kv -Ptag=$tag -Prepo="$REPO" "$@" && $G --console=plain -q dependencies --configuration runtimeClasspath -Pkv=$kv -Ptag=$tag -Prepo="$REPO" "$@" | grep -E 'kotlin-stdlib|portprobe') >"$log" 2>&1
  local rc=$?
  echo "consumer $name (kotlin $kv, against $tag $*): exit $rc; run line: $(grep '^consumer kotlin=' "$log" || echo NONE); errors: $(grep -cE '^e: ' "$log"); first error: $(grep -m1 -E '^e: ' "$log" | cut -c1-260)" >> "$SUM"
  echo "    stdlib resolved: $(grep -m1 -oE 'kotlin-stdlib:[^ ]*( -> [^ ]*)?' "$log" | head -1)" >> "$SUM"
}
produce k2310ios 2.3.10 -Pios=true
produce k2321 2.3.21
produce k2421 2.4.21
produce k2421lv23 2.4.21 -Plv=2.3 -PcoreLibs=2.3.10
consume c2310-p2310ios 2.3.10 k2310ios
consume c2310-p2321 2.3.10 k2321
consume c2310-p2421 2.3.10 k2421
consume c2310-p2421lv23 2.3.10 k2421lv23
consume c2310-vendored 2.3.10 none -Pvendored="$K/producer/src/commonMain/kotlin"
consume c2221-p2310ios 2.2.21 k2310ios
consume c2221-p2421 2.2.21 k2421
consume c2221-p2421lv23 2.2.21 k2421lv23
echo "# finished $(date -u +%Y-%m-%dT%H:%M:%SZ)" >> "$SUM"
$G --stop >/dev/null 2>&1
