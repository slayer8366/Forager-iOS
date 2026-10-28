#!/usr/bin/env bash
# Reproduces dispatch 2026-10-28-01 item A (method-level reach of BRouter v1.7.10 from Forager's calls).
#   run.sh <Forager clone at e2969abe4> <work dir> <jacoco dir with org.jacoco.agent-0.8.14-runtime.jar and org.jacoco.cli-0.8.14-nodeps.jar>
# Needs a JDK 21 (javac, javap, jdeps) and python3. Run in 2026-10-28 with OpenJDK 21.0.12.1.
set -euo pipefail
F=$(cd "$1" && pwd); W=$2; J=$3; T=$(cd "$(dirname "$0")" && pwd)
SRC=$F/third_party/brouter/src/main/java
mkdir -p "$W"/classes "$W"/harness "$W"/data/profiles2 "$W"/data/segments
(cd "$SRC" && find . -name '*.java' | sort > "$W"/sources.txt && javac --release 11 -encoding UTF-8 -g -d "$W"/classes @"$W"/sources.txt) > "$W"/javac-vendored.log 2>&1
(cd "$W"/classes && find . -name '*.class' | sort > "$W"/classes.txt && javap -v -p $(cat "$W"/classes.txt)) > "$W"/javap.txt
java "$T"/SourceSpans.java "$SRC" > "$W"/spans.tsv
# the dynamic lower bound: Forager's calls, its tests' cases, under JaCoCo
cp "$F"/app/src/main/assets/routing/profiles2/{hiking-mountain.brf,lookups.dat} "$W"/data/profiles2/
cp "$F"/app/src/test/resources/routing/segments/W75_N40.rd5 "$W"/data/segments/
javac --release 11 -cp "$W"/classes -d "$W"/harness "$T"/ForagerCalls.java > "$W"/javac-harness.log 2>&1
java -javaagent:"$J"/org.jacoco.agent-0.8.14-runtime.jar=destfile="$W"/jacoco.exec,includes=btools.* \
  -cp "$W"/classes:"$W"/harness ForagerCalls "$W"/data/profiles2 "$W"/data/segments "$W"/work > "$W"/harness-run.log
java -jar "$J"/org.jacoco.cli-0.8.14-nodeps.jar report "$W"/jacoco.exec --classfiles "$W"/classes --sourcefiles "$SRC" \
  --xml "$W"/jacoco.xml --csv "$W"/jacoco.csv
# the static upper bound, per tier of exclusions, each checked against what ran
for t in "T0:" "T1:F" "T2:F,E,S" "T3:F,E,S,P"; do
  n=${t%%:*}; tiers=${t#*:}
  SRC_ROOT=$SRC TIERS=$tiers python3 -I "$T"/reach.py "$W"/javap.txt "$W"/spans.tsv "$T"/entries.txt "$T"/exclusions.txt "$W"/jdk-javap "$W"/$n
  python3 -I "$T"/compare_dynamic.py "$W"/jacoco.xml "$W"/$n/reached-methods.tsv "$W"/$n/compare-with-jacoco.txt
  python3 -I "$T"/jdk_use.py "$W"/javap.txt "$W"/$n/reached-methods.tsv "$W"/$n/jdk-use.txt > /dev/null
done
# the feasibility report's class-level count, re-derived with jdeps (any reference pulls in the whole class)
jdeps -verbose:class -filter:none "$W"/classes | grep -- '->' > "$W"/jdeps-class.txt
