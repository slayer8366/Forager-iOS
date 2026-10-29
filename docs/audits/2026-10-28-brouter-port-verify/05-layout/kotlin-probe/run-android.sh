#!/usr/bin/env bash
# Android consumer at Forager's settings (AGP 9.3.1, kotlin-android 2.3.10, builtInKotlin=false, jvmTarget 17, minSdk 26),
# compiling and dexing against the probe library from each producer. `clean` first in every run.
set -u
K="$(cd "$(dirname "$0")" && pwd)"; G=/root/.gradle/wrapper/dists/gradle-9.7.0-bin/d4tj7w02tcgubx9zk9hbippn6/gradle-9.7.0/bin/gradle
SUM="$K/logs/android-summary.txt"; echo "# Android consumer probe, $(date -u +%Y-%m-%dT%H:%M:%SZ). Prediction: all three compile and dex; k2421 moves the app's stdlib to 2.4.21." > "$SUM"
for tag in k2310ios k2421 k2421lv23; do
  log="$K/logs/android-$tag.log"
  (cd "$K/android" && rm -rf app/build build && $G --console=plain clean :app:assembleDebug -Ptag=$tag -Prepo="$K/repo" && $G --console=plain -q :app:dependencies --configuration debugRuntimeClasspath -Ptag=$tag -Prepo="$K/repo" | grep -E 'kotlin-stdlib|portprobe') > "$log" 2>&1
  rc=$?
  echo "android against $tag: exit $rc; compileDebugKotlin ran: $(grep -c '> Task :app:compileDebugKotlin$' "$log"); dex ran: $(grep -cE '> Task :app:(dexBuilderDebug|mergeExtDexDebug|mergeLibDexDebug)$' "$log"); apk: $(ls "$K/android/app/build/outputs/apk/debug/" 2>/dev/null | grep -c '\.apk$'); e-lines: $(grep -cE '^e: ' "$log"); first failure: $(grep -m1 -E '^e: |What went wrong' -A2 "$log" | tr '\n' ' ' | cut -c1-300)" >> "$SUM"
  echo "    resolved: $(grep -oE 'portprobe:[^ ]*|kotlin-stdlib:[^ ]*( -> [^ ]*)?' "$log" | sort -u | tr '\n' ' ')" >> "$SUM"
done
echo "# finished $(date -u +%Y-%m-%dT%H:%M:%SZ)" >> "$SUM"; $G --stop >/dev/null 2>&1
