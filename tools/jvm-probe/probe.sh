#!/usr/bin/env bash
# RECORD -37: runs this job's java (JAVA_HOME's, which the test JVM uses: no toolchain is set) with the test JVM's flags
# and prints its own start-up output and exit code, then compares Math.cos with StrictMath.cos under each candidate
# flag. A diagnostic: it prints every result and always exits 0; nothing reads its exit code.
#     tools/jvm-probe/probe.sh
set -u
java="${JAVA_HOME:?JAVA_HOME is not set}/bin/java"
here="$(cd "$(dirname "$0")" && pwd)"
out="$(mktemp -d)"
run() {
  echo "== java $*"
  "$java" "$@" 2>&1
  echo "exit code: $?"
}
uname -m
run -XshowSettings:properties -version 2>&1 | grep -E 'os.arch|java.vm.version|java.home|exit code'
echo "-- the test JVM's flags today (brouter/build.gradle.kts):"
run -XX:+UnlockDiagnosticVMOptions -XX:-UseLibmIntrinsic -version
echo "-- aarch64's switch for cos (RECORD -21):"
run -XX:+UnlockDiagnosticVMOptions -XX:DisableIntrinsic=_dcos -version
"${JAVA_HOME}/bin/javac" -d "$out" "$here/CosProbe.java" || { echo "javac failed; no cos comparison"; exit 0; }
echo "-- Math against StrictMath, JIT allowed:"
run -cp "$out" CosProbe 30
run -XX:+UnlockDiagnosticVMOptions -XX:DisableIntrinsic=_dcos -cp "$out" CosProbe 30
echo "-- Math against StrictMath, interpreter only:"
run -Xint -cp "$out" CosProbe 2
run -Xint -XX:+UnlockDiagnosticVMOptions -XX:DisableIntrinsic=_dcos -cp "$out" CosProbe 2
exit 0
