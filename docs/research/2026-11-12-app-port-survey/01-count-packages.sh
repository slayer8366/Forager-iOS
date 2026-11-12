#!/bin/sh
# usage: 01-count-packages.sh <export root>. Kotlin files and lines per top-level package of app/src/main.
cd "$1" || exit 1
A=app/src/main/java/com/zynergylabs/forager/app
printf 'package\tfiles\tlines\n'
for d in "$A"/*/; do n=$(find "$d" -name '*.kt' | wc -l); l=$(find "$d" -name '*.kt' -print0 | xargs -0 cat | wc -l); printf '%s\t%s\t%s\n' "$(basename "$d")" "$n" "$l"; done
n=$(find "$A" -maxdepth 1 -name '*.kt' | wc -l); l=$(cat "$A"/*.kt | wc -l); printf '(root)\t%s\t%s\n' "$n" "$l"
n=$(find app/src/main -name '*.kt' | wc -l); l=$(find app/src/main -name '*.kt' -print0 | xargs -0 cat | wc -l); printf 'TOTAL app/src/main\t%s\t%s\n' "$n" "$l"
echo; echo 'second-level packages (files):'
for p in domain ui data; do for d in "$A/$p"/*/; do printf '%s/%s\t%s\n' "$p" "$(basename "$d")" "$(find "$d" -name '*.kt' | wc -l)"; done; printf '%s (direct)\t%s\n' "$p" "$(find "$A/$p" -maxdepth 1 -name '*.kt' | wc -l)"; done
echo; echo 'source sets (kt files):'
for s in app/src/*/; do printf '%s\t%s\n' "$s" "$(find "$s" -name '*.kt' | wc -l)"; done
printf 'third_party/brouter java\t%s\t%s\n' "$(find third_party/brouter -name '*.java' | wc -l)" "$(find third_party/brouter -name '*.java' -print0 | xargs -0 cat | wc -l)"
