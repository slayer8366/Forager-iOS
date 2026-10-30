#!/usr/bin/env bash
# Fetches JaCoCo's runtime agent for E3's sixth regime, R6 (Phase 1 report, E3; RECORD -46): org.jacoco.agent 0.8.16,
# classifier runtime, from Maven Central. 0.8.16 is the <release> and <latest> of
# https://repo1.maven.org/maven2/org/jacoco/org.jacoco.agent/maven-metadata.xml as read on 2026-10-30 (lastUpdated
# 20261026104303). Central publishes no .sha256 for it; the SHA-256 below was computed from the downloaded jar, whose
# SHA-1 (4325ec32eef129534b61041814d25ec88e3023ce) and MD5 equal Central's own .sha1 and .md5. Nothing is committed.
#
#   tools/fetch-jacoco.sh <dir>     leaves <dir>/jacocoagent-0.8.16-runtime.jar
set -euo pipefail

dir=${1:?usage: fetch-jacoco.sh <dir>}
here=$(cd "$(dirname "$0")" && pwd)

url=https://repo1.maven.org/maven2/org/jacoco/org.jacoco.agent/0.8.16/org.jacoco.agent-0.8.16-runtime.jar
size=314470
sha256=37ae2f374ad77e85c59c6e5b46565226c3ac3cb460ac19de0bcb844ac2f43e33

mkdir -p "$dir"
"$here/fetch-pinned.sh" "$dir/jacocoagent-0.8.16-runtime.jar" "$url" "$size" "$sha256"
echo "jacoco agent ready in $dir"
