Q7, the measurement (run 2 of dispatch 2026-10-29-01, 2026-10-29). Method: the report's "Method" section, commit 634abf7,
as amended in run 1 (primary build -O2 -ffp-contract=off; atan2 added).

WHAT WAS BUILT. bionic's libm sources at android-17.0.0_r1 (bionic 06356e41), fetched by run 1 (sources.tsv rows):
upstream-freebsd/lib/msun/src/ s_cos.c (which #includes e_rem_pio2.c inline, INLINE_REM_PIO2), k_cos.c, k_sin.c,
k_rem_pio2.c, e_atan2.c, s_atan.c, with math_private.h, freebsd-compat.h, and android/include/machine/endian.h (fetched
this run). Compiled on this container (Linux x86_64, glibc 2.39) with
  clang 18.1.3 and gcc 13.3.0, both: -O2 -ffp-contract=off -fno-builtin -fno-math-errno
  -include freebsd-compat.h (libm/Android.bp:440-452 at 06356e41 gives -include freebsd-compat.h, -fno-builtin,
  -fno-math-errno; build/soong cc/config/global.go:138-139 gives -ffp-contract=off).
Two shim headers stand in for bionic's own libc headers (tools/bionic-shim.h, tools/bionic-shim2.h): <stdint.h>, which
bionic's <sys/types.h> includes (libc/include/sys/types.h:35) and glibc's does not; __double_t/__float_t, which bionic's
<math.h> defines as double/float (libc/include/math.h:25-28); and the two-level __CONCAT that bionic's <sys/cdefs.h> has
(libc/include/sys/cdefs.h:82-83) and glibc's one-level form lacks (it broke only rnintl(), long double, off the path).
Linked with -lm for glibc's floor() and fabs() (exact IEEE operations, the same on any correct libm); nm shows cos,
__kernel_cos, __kernel_sin, __kernel_rem_pio2, atan2 and atan defined (T) in the executables (logs/build-*.log).
The harness prints FLT_EVAL_METHOD=0, sizeof(double_t)=8, amd64_defined=0: the same preprocessor branches of
math_private.h as arm64 takes (IEEE_WORD_ORDER = BYTE_ORDER, STRICT_ASSIGN plain, irint = (int) cast).

INPUTS. tools/TableRows.java (copied unchanged from brouter-port 10-tests/tools/) wrote HotSpot's Math.cos and
StrictMath.cos tables (OpenJDK 21.0.12.1, amd64, defaults). tools/Gen.java regenerates part 6's atan2 sample (the probe's
atan2.txt result file is not in the brouter-port evidence): ProbeTest.kt:5 Lcg, :42-51 atan2Sample, seed 42, and
writes dy dx Math.atan2 StrictMath.atan2 as bits.

CHECKS FROM OUTSIDE THE SCRIPTS, all passed:
- HotSpot Math.cos table == 09-float/part6/results/jvm-amd64/scale-table.txt byte for byte (cmp).
- Math.cos vs StrictMath.cos: 60 of 1,800 rows differ (results/jvm-math-vs-strict.txt), as part 8 found. Part 8's
  10-tests/logs/rows-math-vs-strict.txt lists the 54 rows where kx or ky differs; the 60 here are those 54 plus the 6
  it counts as "cos differs but kx and ky equal" (rows 129, 161, 436, 506, 538, 1638).
- Pairs regenerated: 299,974 (wc -l), Math.atan2 != StrictMath.atan2 at 0 (part 6's libm-check-jvm.txt: "0 of 299974");
  glibc 2.39 atan2 equal to HotSpot's at 249,759 of 299,974, the figure part 6 recorded on the same glibc.
- The harness recomputes each row's lat from its index as calcKxKyFromILat does: 0 mismatches against the file.
- wc -l: msun-cos 1,800 rows (+1 comment line), msun-atan2 299,974.
- clang's and gcc's outputs are byte for byte identical (cmp; SHA-256 in results/sha256-and-wc.txt).

RESULTS (both compilers; results/cmp-*.txt hold every differing row):
  msun cos   vs StrictMath.cos  : 13 of 1,800 rows differ, every one by 1 ulp, every one with |lat| <= pi/4, where
                                  s_cos.c calls __kernel_cos(x, 0) directly and no argument reduction runs
                                  (results/rows-msun-vs-strict.txt). The ~900 rows with |lat| > pi/4 all agree.
  msun cos   vs Math.cos (Intel): 59 of 1,800 rows differ.
  msun atan2 vs StrictMath.atan2: 0 of 299,974.
  msun atan2 vs Math.atan2      : 0 of 299,974 (Math.atan2 is the Java fdlibm on HotSpot; the report's Q4).
  msun cos   vs this host's glibc 2.39 cos: 1,745 of 1,800 equal (context only; part 6: glibc == Math.cos at 1,792).

WHAT THIS DOES NOT SHOW. This is bionic's C compiled by this host's clang/gcc for x86_64 with contraction off, not a
phone's libm.so; a phone's arm64 build by Android's clang is modelled, not run. The 13-row result is a property of the
C source (k_cos.c's polynomial is not fdlibm 5.3's; handoff item 2), so it is expected to hold on arm64 too, but
that is inferred. jvm-pairs.txt and msun-atan2.txt (13 MB each) are not committed; their SHA-256 and line counts are,
and Gen.java and at2 regenerate them.
