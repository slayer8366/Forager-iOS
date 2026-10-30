#!/usr/bin/env bash
# Step 5b probe: the plain jar twice (two JVMs), then the shadowed classes with the whole-tile pass, then routes.
set -u
P=$(cd "$(dirname "$0")" && pwd)
J=$P/../temurin/jdk-21.0.13+9/bin
B=/home/user/forager-ios-5a/brouter/build
JAR=$B/oracle/brouter-1.7.10-ro.jar
F="-XX:+UnlockDiagnosticVMOptions -XX:-UseLibmIntrinsic --add-opens=java.base/java.util=ALL-UNNAMED"
T="W75_N40=$B/tiles/segments/W75_N40.rd5 W75_N35=$B/tiles/segments/W75_N35.rd5 W70_N40=$B/tiles/segments/W70_N40.rd5 E170_S50=$B/tiles/segments/E170_S50.rd5 F0=$B/tiles/f0/W75_N40.rd5"
cd "$P"
for r in jarA jarB; do
  { time $J/java $F -cp out/main:out/probe:$JAR probe.ProbeMain decode $B/oracle runs/$r.txt false $T > runs/$r.log 2>&1; } 2>> runs/$r.time
  echo "$r exit $?" >> runs/status.txt
done
{ time $J/java $F -cp out/main:out/probe:out/shadow:$JAR probe.ProbeMain decode $B/oracle runs/shadow.txt true $T > runs/shadow.log 2>&1; } 2>> runs/shadow.time
echo "shadow exit $?" >> runs/status.txt
{ time $J/java $F -cp out/main:out/probe:out/shadow:$JAR probe.ProbeMain routes $B/oracle runs/routes.txt $B/tiles/segments $B/tiles/f0 > runs/routes.log 2>&1; } 2>> runs/routes.time
echo "routes exit $?" >> runs/status.txt
echo done >> runs/status.txt
