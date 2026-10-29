Item H, part 6 (Hazel, 2026-10-29): evidence for steps 2 and 3.

The probe (../probe/) was built and run in scratch (hazel/part6/fp-probe), with Gradle 9.7.0 and Kotlin 2.3.10.
  Inputs: ../tools/make_corpus.py <io>/in <lookups.dat> <27 .brf>; then jvmTest --tests probe.JvmOnlyTest (writes
  in/fma-sensitive.txt), then jvmTest linuxX64Test, then build/bin/linuxArm64/debugTest/test.kexe under
  ~/.konan/dependencies/qemu-aarch64-static-5.1.0-linux-2/qemu-aarch64 -L <aarch64 glibc 2.25 sysroot>.
  The output directories must exist before the native tests run (their writeText makes one level only); the first
  run2 attempt failed for that reason, and the logs here are the rerun.
Results
  junit-counts.txt               10 of 10 tests on each platform, plus 2 JVM-only, timestamped 04:40Z.
  compare-*.txt                  ../tools/compare.py, line by line, three platform pairs.
  libm-check-*.txt               ../tools/libm_check.py: probe values against the host C library (glibc 2.39).
  fma-run.txt, fma-disasm.txt    fused-multiply detectors run on arm64/x64 executables; llvm-objdump scan.
  fma-sensitive-search.txt       JVM search with Math.fma for inputs where fusion would change an int (none found).
  results/<platform>/            fma, hash, scale table, conversion edge cases; jvm-only.txt (Math against StrictMath).
  io-sha256.txt                  SHA-256 of every input and output file, including the large ones not committed.
  kn-reads.txt                   what was read in the Kotlin/Native distribution, with SHA-256 of each file read.
Logs
  logs-arm64-build.log is the first build, with the wrong float detector (2^-12, whose product is exact);
  logs-arm64-build2.log is after the fix to 2^-13. fma-run.txt is from the second build.
