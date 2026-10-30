#!/usr/bin/env bash
# Step 5b tieBreakOrder count (scratch). The plain oracle jar, with Temurin 21.0.13+9's own HashMap.java patched into
# java.base with counters at its two tieBreakOrder call sites (patch_hashmap.py). Probe as for -34 (patch_probe.py).
set -u
J=/tmp/claude-0/-home-user/c9851708-fbbe-5ce6-a5f8-ed278a238cce/scratchpad/5b/temurin/jdk-21.0.13+9/bin
F="-XX:+UnlockDiagnosticVMOptions -XX:-UseLibmIntrinsic --add-opens=java.base/java.util=ALL-UNNAMED --patch-module java.base=/tmp/claude-0/-home-user/c9851708-fbbe-5ce6-a5f8-ed278a238cce/scratchpad/5b/tiebreak/out/patch"
CP=/tmp/claude-0/-home-user/c9851708-fbbe-5ce6-a5f8-ed278a238cce/scratchpad/5b/tiebreak/out/probe:/home/user/forager-ios-5a/brouter/build/oracle/brouter-1.7.10-ro.jar
TL="W75_N40=/home/user/forager-ios-5a/brouter/build/tiles/segments/W75_N40.rd5 W75_N35=/home/user/forager-ios-5a/brouter/build/tiles/segments/W75_N35.rd5 W70_N40=/home/user/forager-ios-5a/brouter/build/tiles/segments/W70_N40.rd5 E170_S50=/home/user/forager-ios-5a/brouter/build/tiles/segments/E170_S50.rd5 F0=/home/user/forager-ios-5a/brouter/build/tiles/f0/W75_N40.rd5"
RL="segments/W75_N40=/home/user/forager-ios-5a/brouter/build/tiles/segments/W75_N40.rd5 segments/W70_N40=/home/user/forager-ios-5a/brouter/build/tiles/segments/W70_N40.rd5 segments/E170_S50=/home/user/forager-ios-5a/brouter/build/tiles/segments/E170_S50.rd5 f0/W75_N40=/home/user/forager-ios-5a/brouter/build/tiles/f0/W75_N40.rd5"
cd /tmp/claude-0/-home-user/c9851708-fbbe-5ce6-a5f8-ed278a238cce/scratchpad/5b/tiebreak
case "${1:-all}" in
  smoke) $J/java $F -cp $CP probe.ProbeMain regions /home/user/forager-ios-5a/brouter/build/oracle runs/smoke.txt $RL > runs/smoke.log 2>&1; echo "smoke exit $?";;
  all)
    { time $J/java $F -cp $CP probe.ProbeMain regions /home/user/forager-ios-5a/brouter/build/oracle runs/regions.txt $RL > runs/regions.log 2>&1; } 2>> runs/regions.time; echo "regions exit $?" >> runs/status.txt
    { time $J/java $F -cp $CP probe.ProbeMain routes /home/user/forager-ios-5a/brouter/build/oracle runs/routes.txt /home/user/forager-ios-5a/brouter/build/tiles/segments /home/user/forager-ios-5a/brouter/build/tiles/f0 > runs/routes.log 2>&1; } 2>> runs/routes.time; echo "routes exit $?" >> runs/status.txt
    { time $J/java $F -cp $CP probe.ProbeMain decode /home/user/forager-ios-5a/brouter/build/oracle runs/decode.txt true $TL > runs/decode.log 2>&1; } 2>> runs/decode.time; echo "decode exit $?" >> runs/status.txt
    echo done >> runs/status.txt;;
esac
