#!/bin/bash
# Usage: run-jvmtest.sh <out-file> <title-line> [extra gradle args]
# Deletes the previous JUnit XML first, runs :brouter:jvmTest, then summarises only if the build log has no compile errors.
set -u
out=$1; title=$2; shift 2
here=$(cd "$(dirname "$0")" && pwd)
cd /home/user/forager-ios
rm -rf brouter/build/test-results/jvmTest
./gradlew -I "$here/../mirror.init.gradle.kts" :brouter:jvmTest --console=plain "$@" > "$here/gradle-$(basename "$out" .txt).log" 2>&1
echo "gradle exit: $?" >> "$here/gradle-$(basename "$out" .txt).log"
python3 -I "$here/summarize.py" brouter/build/test-results/jvmTest "$here/gradle-$(basename "$out" .txt).log" "$title" > "$out"
echo "summarize exit: $?"; tail -1 "$here/gradle-$(basename "$out" .txt).log"
