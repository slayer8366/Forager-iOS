# Do Android phones compute BRouter's cosine and atan2 as the test Java does?

Dispatch 2026-10-29-01 (`prompts/preserved/2026-10-29-01-android-math.md`), RECORD -19. Written by the Forager-iOS
researcher (first run, unnamed), 2026-10-29. Branch `research/android-math`, cut from `main` at `490b0e3`. Evidence
folder: `docs/research/2026-10-29-android-math/`.

**Status: run 2 done, closed at the planner's message (11:09Z, context past 180k by the harness's count).** Run 1 handed
off at 10:50Z after questions 1 to 6. Run 2 (this session) did Q7, the measurement, and the reads marked **run 2** below:
questions 1, 2, 4, 5 and 7 are answered; 3 is partial (its ends read, the intermediate ART releases inferred); 6 is a
bounded "not what iOS ships" from a clone of Apple's published Libm. The handoff at the end says what remains.

**Marks.** Every claim is **[R]** read (URL, commit or tag, file and line, date), **[C]** computed (script and log in
the evidence folder), **[I]** inferred, or **[?]** could not determine. Every fetched file is one row of
`sources.tsv` (date read, bytes, SHA-256, file, URL); the bodies stayed in scratch.

## Bases, checked 2026-10-29

- Forager's read-only clone `/home/user/forager` is at `e2969abe` on `pre-main` **[C]** (`git log -1`). The dispatch names
  `pre-main` `d100a4776`; RECORD -9 says the routing code is the same in both. Not re-diffed here.
- Forager-iOS `brouter-port` is at `529cea9` **[C]** (`git log -1 origin/brouter-port`); the Phase 1 report read from it.
- This branch's merge base with `origin/main` is `490b0e3`, the commit that adds the dispatch **[C]**.
- This container: Ubuntu 24.04, gcc 13.3.0, clang 18.1.3, OpenJDK 21.0.12.1 (`21.0.12.1+1-1-24.04.4-Ubuntu`, amd64),
  Python 3.13.17 **[C]**. The JDK is the same build as the oracle's (`10-tests/logs/table-run.log`). No aarch64
  cross-compiler and no qemu. The CPU reports `fma` **[C]** (`/proc/cpuinfo`).

## Premises (dispatch "Verify first")

| # | Premise | Checked |
|---|---|---|
| 1 | `minSdk = 26`, `targetSdk = 37` at `app/build.gradle.kts:265-266` | **[R]** confirmed at `e2969abe`, those lines |
| 2 | `libcore` `de876a01b`: `Math.cos`/`atan2` native, `Math.c` returns C's `cos`/`atan2` | **[R]** re-read at release tags: `android-17.0.0_r1` (libcore `4ebfb391`) `Math.java:231` `public static native double cos`, `Math.c:30-31` `return cos(d)`, `:85-86` `return atan2(d1, d2)`, `:27` `#include <math.h>`; and `android-8.0.0_r1` (libcore `8924c3d5`) `Math.java:155`, `:500`; `Math.c:30`, `:85` |
| 3 | HotSpot x86_64: `Math.cos` vs `StrictMath.cos` differ at 60 of 1,800 rows; 0 with `-XX:-UseLibmIntrinsic` | evidence read: `rows-math-vs-strict.txt` says 60, `rows-nointrinsic-vs-strict.txt` says 0 **[R]**; not recomputed (Q7 not reached) |
| 4 | `kotlin.math.cos` on the JVM calls `java.lang.Math.cos` | **confirmed [R]**, Q5 |

## Method, written before any source was fetched (commit `634abf7`)

**Reads.** One file per request, at a named tag or commit, through `tools/fetch.sh`, which logs date, bytes and SHA-256 of
each body to `sources.tsv`. Tags were resolved to commits with `git ls-remote` (`tags/tag-commits.txt`,
`tags/*-pinned.txt`, `tags/*-bisect-commits.txt`). Pins:

- AOSP `art`, `bionic`, `libcore`, `build/soong` at `android-17.0.0_r1` (API 37, the newest release tag) and
  `android-8.0.0_r1` (API 26, Forager's `minSdk`). Intermediate tags where the two ends differ.
- OpenJDK `jdk21u` at `jdk-21.0.12.1-ga` (commit `eac07229`), the tag of the JDK this container and the oracle run.
- Kotlin at `v2.3.10` (commit `679366a8`), Forager's pinned version (`gradle/libs.versions.toml:7`).
- Apple: whatever Apple publishes, read at a named URL; nothing downloaded beyond a page.

**Q7, the measurement (not run; the method stands for whoever runs it).** Where the implementation that bionic builds
`cos` from is C that compiles here, compile it in scratch with clang 18 and gcc 13 on x86_64 and run it over the table's
1,800 inputs, `ilat = i * 100000 + 50000`, `lat = (PI/180) * (ilat * 1e-6 - 90)`, exactly `CheapRuler.calcKxKyFromILat`
(`CheapRuler.java:35-49` at `e2969abe`). Recompute HotSpot's `Math.cos` and `StrictMath.cos` for the same inputs with
this container's JDK by part 8's `TableRows.java` unchanged, and require those two files to equal the evidence's: the
Math table byte for byte against `09-float/part6/results/jvm-amd64/scale-table.txt`, and the Math-vs-Strict count 60
(`10-tests/logs/rows-math-vs-strict.txt`). If either check fails, the measurement is not cited.

- **A match** is bit equality of the 64-bit pattern of `cos(lat)` for a row. Two implementations match on the table when
  0 of 1,800 rows differ. Report the count of differing rows for each pair, and the rows.
- **Flags.** `-O2 -ffp-contract=off` is the primary build. After Q2 and Q3 below, it is also the only build that
  stands for Android: every Android release in the range builds libm without contraction. The `-ffp-contract=on -mfma`
  model proposed in the method commit is no longer needed and should not be reported as Android's.
- **Counts checked outside the script:** `wc -l` of each table file is 1,800; each line's row index is its line number
  minus one; the HotSpot checks above.
- **`atan2`, a call beyond the dispatch's letter:** the same C `atan2` over part 6's 299,974 integer pairs (the LCG of
  `ProbeTest.kt:5` and `:43-50`, seed 42), against HotSpot's `Math.atan2` and `StrictMath.atan2`. The pair count must be
  299,974 or the run is not cited. `CheapAngleMeter.getAngle` (`CheapAngleMeter.java:50-55`) calls
  `Math.atan2(ydiff, xdiff)` on integer differences cast to double, so integer pairs are the production domain **[R]**.

## Predictions, mine, written before any source was fetched; outcome beside each

1. **ART:** intrinsics that call the C library through a runtime entry point; the interpreter calls the native method.
   **Right** (Q1). The Android 8 interpreter even called `std::cos` itself, the same library.
2. **bionic:** FreeBSD msun on arm64; Intel assembly on x86_64 if still present; no `-ffp-contract=off` in libm's build
   file. **Half right:** msun on arm64 at both ends; Intel assembly on x86_64 only through API 33; the flag is absent
   from libm's file but present in the global build flags from API 33, and the compiler's own default was off before.
3. **API 26 to 37:** no change on arm64. **Right for arm64's `cos`/`atan2` arithmetic at the table's inputs; wrong in
   that x86_64 changed implementation at API 34.**
4. **HotSpot:** x86_64 Intel stub under `UseLibmIntrinsic`; aarch64 a port of the same algorithm; `atan2` via a C++
   fdlibm port. **Right on x86_64; wrong on aarch64** (it is a fused-multiply variant of fdlibm, not Intel's algorithm,
   and the flag does not exist there); **wrong on `atan2`'s mechanism** (there is no C++ port: C2 refuses the intrinsic
   and every tier runs the Java `FdLibm.Atan2`), same conclusion.
5. **Kotlin:** inline call to `java.lang.Math.cos`. **Right.**
6. **Apple:** could not determine. **Could not determine**, for a different reason than predicted (the index page is
   gone and the mirror is behind an access policy here).
7. **Measurement:** not reached in run 1. Run 1's method commit said "if `k_cos.c` is fdlibm 5.3's, predict 0 rows
   against `StrictMath`". **Outcome: 13 rows, because `k_cos.c` is not fdlibm 5.3's evaluation** (Q2, run 2); the
   conditional was right and its condition false. `atan2`: 0 of 299,974, as run 1's "expected equal" said.

## Answers

### Q1. ART: interpreter, JIT and ahead-of-time compiler. **Answered.**

All tiers end at the C library's `cos` and `atan2`; ART computes neither itself.

- **The compilers (JIT and AOT share the optimizing backend) treat both as intrinsics, and the intrinsic is a call
  into the C library [R].** `art` at `android-17.0.0_r1` (commit `b753cf97`):
  - `runtime/intrinsics_list.h:159` `V(MathCos, ... "Ljava/lang/Math;", "cos", "(D)D")`; `:164` `MathAtan2`.
  - arm64: `compiler/optimizing/intrinsics_arm64.cc:2895-2899` `GenFPToFPCall` is `codegen->InvokeRuntime(entry,
    invoke)`; `:2905-2906` `VisitMathCos` → `GenFPToFPCall(invoke, codegen_, kQuickCos)`; `:3017-3018` `VisitMathAtan2`
    → `kQuickAtan2`.
  - x86_64: `intrinsics_x86_64.cc:512-519`, `:525-526`, `:658-659`, the same shape.
  - the entry points are the C library's functions: `runtime/entrypoints/quick/quick_entrypoints_list.h:90` `V(Cos,
    double, double)`, `:95` `V(Atan2, double, double, double)`; `runtime/arch/arm64/entrypoints_init_arm64.cc:218`
    `qpoints->SetCos(cos);`, `:223` `qpoints->SetAtan2(atan2);`; `runtime/arch/x86_64/entrypoints_init_x86_64.cc:91`,
    `:96`.
- **The interpreter [R].** At `android-17.0.0_r1` the `runtime/interpreter/` directory (listing read) has no math
  intrinsic handler; `interpreter_common.cc:551-562` dispatches intrinsics only for the signature-polymorphic ones
  (`ART_SIGNATURE_POLYMORPHIC_INTRINSICS_LIST`), so an interpreted `Math.cos` call invokes the method itself, which is
  `@CriticalNative` (premise 2) and returns C's `cos(d)`. `mterp/nterp.cc:1003-1004`: intrinsic methods "will be
  considered hot from the first call", so they are compiled early in any case.
- **At API 26, the same [R].** `art` at `android-8.0.0_r1` (commit `699cb9a1`): `intrinsics_arm64.cc:1855-1856` and
  `:1967-1968`, `intrinsics_x86_64.cc:780-781` and `:910-911` (`kQuickCos`, `kQuickAtan2`);
  `entrypoints_init_arm64.cc:164` `qpoints->pCos = cos;`, `:169` `pAtan2 = atan2`; x86_64 `:88`, `:93`;
  `compiler/intrinsics_list.h:77`, `:82`. The 8.0 interpreter (`runtime/interpreter/interpreter_intrinsics.cc:167`)
  computed `MathCos` itself with `std::cos` (`UNARY_INTRINSIC(MterpMathCos, std::cos, ...)`, dispatched at `:391`), the
  same library function; `MathAtan2` was `UNIMPLEMENTED_CASE` (`:396`), so it ran the native method.
- **Between the two ends: not read; inferred unchanged [I],** since both ends route to the same library symbol.

### Q2. bionic: which implementation, and flags. **Answered for API 37; the symbol-level check closed in run 2.**

- **At `android-17.0.0_r1` (bionic `06356e41`), `cos` and `atan2` on every architecture are FreeBSD's msun, in C
  [R].** `libm/Android.bp`: `libm_common_sources` lists `upstream-freebsd/lib/msun/src/e_atan2.c` (`:34`), `k_cos.c`
  (`:68`), `s_atan.c` (`:79`), `s_cos.c` (`:98`). The per-architecture lists hold only `fenv-*.c` and a few rounding
  and `fma` sources: `libm_arm64_sources` (`:283-287`) is `fenv-arm64.c`; `libm_x86_64_sources` (`:318-`) is
  `fenv-x86_64.c`, `s_fma*.c`, `s_round*.c`, `s_l*round*.c`. No `exclude_srcs` names `cos` or `atan2`. The `libm/`
  tree at that commit has no `arm64/` or `x86_64/` directory (`tree-listings.txt`).
- **Origin:** fdlibm, through FreeBSD. The file headers at API 26 read `@(#)s_cos.c 5.1 93/09/24`, `@(#)k_cos.c 1.3
  95/01/18`, `@(#)e_atan2.c 1.3 95/01/18` (`msun-8-vs-17.diff.txt`); the API 37 copies drop those lines. **Whether
  FreeBSD's `k_cos.c` and `s_atan.c` bodies are fdlibm 5.3's, as HotSpot's `FdLibm.java` and `sharedRuntimeTrig.cpp`
  are, was not read [?].** This is the read that decides whether Android can equal `StrictMath` bit for bit.
- **Run 2, read against fdlibm 5.3 as HotSpot carries it [R].** `k_cos.c` (bionic `06356e41`, `:54-80`) keeps fdlibm's
  six constants C1 to C6 bit for bit but is FreeBSD's rewritten kernel: `r = z*(C1+z*(C2+z*C3)) + w*w*(C4+z*(C5+z*C6))`
  with `w = z*z`, then `w = one-hz; return w + (((one-w)-hz) + (z*r-x*y))`, with no small-x return and no 0.3/0.78125
  branches. HotSpot's `sharedRuntimeTrig.cpp:571-597` `__kernel_cos` is fdlibm 5.3 verbatim: Horner
  `r = z*(C1+z*(C2+z*(C3+z*(C4+z*(C5+z*C6)))))`, `one - (0.5*z - (z*r - x*y))` below 0.3, else the `qx` form. Same
  constants, different operation order, so different roundings: those are Q7's 13 rows, all in the band where only the
  kernel runs. `s_atan.c` (`:36-118`) against `FdLibm.java` `Atan.compute`: the same three tables (`atanhi`, `atanlo`,
  the eleven `aT`), the same four reductions, the same `s1`/`s2` split and the same final expressions, line for line;
  the only differences are the tiny-x cutoff (2^-27 against Java's 2^-29, the same bits either side since the dropped
  term is below half an ulp of x **[I]**) and a `volatile` read of `atanlo[3]` for |x| >= 2^66, outside the
  integer-pair domain. `e_atan2.c` (`:56-123`) against `FdLibm.java` `Atan2.compute`: the same special cases,
  `k = (iy-ix)>>20`, `z = atan(fabs(y/x))`, `pi-(z-pi_lo)` and `(z-pi_lo)-pi`, line for line; NaN handling differs in
  form only (`nan_mix` against `x+y`). `k_sin.c` and `e_rem_pio2.c` were not read line for line **[?]**; their
  agreement at the table's reduced rows is computed only (Q7).
- **Flags [R].** libm's own: `:440-452` `-include freebsd-compat.h`, `-fno-builtin`, `-fno-math-errno`, warnings only.
  Global, `build/soong` at `android-17.0.0_r1` (commit `6722dd88`), `cc/config/global.go:138-139`:
  `// Turn off FMA which got enabled by default in clang-r445002 (http://b/218805949)` / `"-ffp-contract=off",`.
  The list it sits in also carries `-fno-exceptions` and `-fno-strict-aliasing` (`:141-147`), so it is the
  all-targets C/C++ list **[I: the list's name was not read]**. No `-ffp-contract` or fast-math flag in
  `cc/config/arm64_device.go` or `x86_64_device.go` (grep).
- **One thing open:** the libm module links `whole_static_libs: ["libarm-optimized-routines-math"]` (`:384`). If that
  library defined `cos` or `atan2`, it would duplicate msun's symbols in the same link, so it is inferred not to
  **[I]**; its source list was not read **[?]**. **Run 2 [R]:** `external/arm-optimized-routines` at
  `android-17.0.0_r1` (commit `498b3de1`), `Android.bp:88` `libarm-optimized-routines-math`, `srcs`: `cosf`, `exp`,
  `exp2`, `exp2f`, `expf`, `log`, `log2`, `log2f`, `logf`, `pow`, `powf`, `sincosf`, `sinf` and their data files. No
  double `cos`, no `atan2`, no `atan`: msun's are the only definitions. (Beyond scope, one line: double `exp`, `log`
  and `pow` on Android come from this library, not msun or fdlibm; BRouter's `Math.exp` calls are part 6's subject,
  not this dispatch's.) **Run 2 [R]:** the list holding `-ffp-contract=off` is `commonGlobalCflags` (`global.go:32`
  at soong `6722dd88`), so the inference above is confirmed.
- **On x86_64 at API 26 (bionic `f711b078`): Intel-contributed assembly [R].** `libm/Android.bp:448`
  `x86_64/s_cos.S`, `:436` `x86_64/e_atan2.S`, `:446` `x86_64/s_atan.S`, with `exclude_srcs` of the C files at `:458`,
  `:468`, `:470`. arm64 at API 26 used C for both (`:307-336`: only `ceil`, `fenv`, `fma`, `floor`, `lrint`, `rint`,
  `sqrt`, `trunc` in assembly).

### Q3. API 26 to 37. **Partial: the ends and one bisect read; the intermediate ART releases inferred.**

- **Run 2 [R]:** `ClangDefaultShortVersion` is `12.0.7` at `android-12.0.0_r1` (`global.go:149`), `14.0.6` at
  `android-13.0.0_r1` (`:290`), `17` at `android-14.0.0_r1` (`:309`), closing the "before clang 14" inference for
  Android 12. The commit that removed `libm/x86_64/*.S` was **not found [?]**: the `+log` query for
  `android-13.0.0_r1..android-14.0.0_r1` on `libm/x86_64` returned HTTP 401 here, and run 1's stored
  `log-x86_64-s_cos.S.json` turns out to be a 302 redirect page, not a log (disclosed below).

- **arm64 `cos` and `atan2`: the same C source family throughout; the table's arithmetic unchanged [R][I].**
  `diff` of the seven msun files between `android-8.0.0_r1` and `android-17.0.0_r1` (`msun-8-vs-17.diff.txt`): header
  and comment lines; `e_atan2.c` returns `nan_mix(x, y)` instead of `x+y` for NaN inputs and rewrites the `x == 1.0`
  test equivalently; `e_rem_pio2.c` computes the medium-range quotient as `rnint((double_t)x*invpio2)` instead of the
  `0x1.8p52` addition. That medium path is for `|x| > 3pi/4` (`e_rem_pio2.c`, `ix<0x4002d97c` branch first), and the
  table's inputs are `|lat| <= 1.5699 < 2.356`, so no table row reaches it; BRouter's `atan2` inputs are not NaN.
  `math_private.h` changed in 452 lines, not summarised **[?]**.
- **Contraction: no Android release in the range builds libm with fused multiply-add [R][I].** Clang's default was
  `-ffp-contract=off` before clang 14 and `on` from it (`llvm-project` at `llvmorg-14.0.0`,
  `clang/docs/ReleaseNotes.rst:397-400`). Soong's `-ffp-contract=off` is absent at `android-12.0.0_r1` and present at
  `android-13.0.0_r1` (`global.go:115`) and after (`bisect.txt`), with the comment naming the clang that turned FMA on.
  **Inferred [I]:** Android 12 and earlier were built with a clang whose default was off (their clang versions were not
  read **[?]**), so the arm64 `cos` has been the unfused msun arithmetic at every API level 26 to 37. This holds for
  platform builds made with soong; a vendor's own libm build is outside what was read.
- **x86_64 `cos` and `atan2` changed implementation at API 34 [R].** `libm/Android.bp` at each release tag
  (`bisect.txt`): `x86_64/s_cos.S` and `x86_64/e_atan2.S` are listed from `android-8.0.0_r1` through
  `android-13.0.0_r1` (API 33) and absent from `android-14.0.0_r1` (API 34) on. The removing commit was not identified:
  gitiles' log for the deleted path came back empty **[?]**.
- **libcore:** native at both ends (premise 2 above). **ART:** both ends route to the C library (Q1); intermediate
  releases not read **[I]**.
- **How far back could be read:** to `android-8.0.0_r1` directly, for every repository; the tags exist back to 2017
  (`tags/tag-commits.txt`).

### Q4. HotSpot at `jdk-21.0.12.1-ga` (`eac07229`), x86_64 and aarch64. **Answered, except the aarch64 stub's bits.**

- **`Math.cos`, the Java body [R]:** `java/lang/Math.java:202-204` `@IntrinsicCandidate public static double cos(double
  a) { return StrictMath.cos(a); }`; `StrictMath.java:164-166` `return FdLibm.Cos.compute(a)`; `FdLibm.java:29` "Port of
  the 'Freely Distributable Math Library', version 5.3".
- **Every tier makes the same choice: a generated stub if one exists, else a C++ copy of fdlibm [R].** C2:
  `opto/library_call.cpp:1834-1837` `_dcos` → `StubRoutines::dcos()` if not null, else `SharedRuntime::dcos`
  (`runtime/sharedRuntimeTrig.cpp:809`; the file is "copies of the C fdlibm routines", `:31`). C1:
  `c1_LIRGenerator_x86.cpp:930-934`, `c1_LIRGenerator_aarch64.cpp:863-867`. Interpreter:
  `templateInterpreterGenerator_x86_64.cpp:446-451`, `templateInterpreterGenerator_aarch64.cpp:252-256`.
- **x86_64: the stub is Intel's libm [R].** `stubGenerator_x86_64.cpp:3495-3500`: `if (UseLibmIntrinsic &&
  InlineIntrinsics) { ... if (vmIntrinsics::is_intrinsic_available(vmIntrinsics::_dcos)) { StubRoutines::_dcos =
  generate_libmCos(); } }`; `stubGenerator_x86_64_cos.cpp:2-3` "Copyright (c) 2016, 2021, Intel Corporation ... Intel
  Math Library (LIBM) Source Code", range reduction `X =~= N * pi/32 + r` (`:33-45`).
- **`-XX:-UseLibmIntrinsic` is an x86-only, diagnostic flag [R].** `cpu/x86/globals_x86.hpp:214-215`
  `product(bool, UseLibmIntrinsic, true, DIAGNOSTIC, "Use Libm Intrinsics")`. It is not in `share/runtime/globals.hpp`
  nor in `cpu/aarch64/globals_aarch64.hpp` (grep of both). What it switches: whether the x86_64 libm stubs (sin, cos
  and the others under the same guard) are generated at all; with it off every tier falls to `SharedRuntime::dcos`,
  fdlibm in C++. The JDK build defines `FDLIBM_CFLAGS` as `-ffp-contract=off` where the compiler accepts it
  (`make/autoconf/flags-cflags.m4:985-991`); whether `CompileJvm.gmk` applies it to `sharedRuntimeTrig.cpp` was not
  confirmed (grep found no match) **[?]**. Part 8's finding that the no-intrinsic table equals `StrictMath`'s on x86_64
  is consistent with fdlibm C++ compiled without contraction.
- **aarch64: the stub is a fused-multiply variant of fdlibm, and the flag does not exist [R].**
  `stubGenerator_aarch64.cpp:8676-8681` generates `_dsin` and `_dcos` whenever
  `vmIntrinsics::is_intrinsic_available(_dcos)`, with no `UseLibmIntrinsic` test. `is_intrinsic_available` is
  `VM_Version::is_intrinsic_supported(id) && !is_disabled_by_flags(id)` (`vmIntrinsics.cpp:653-655`); `_dcos` is
  disabled by `-XX:-InlineMathNatives` (`:286`, `:307`, together with `sqrt`, `min`/`max` and the others at
  `:280-306`) or by `-XX:DisableIntrinsic=_dcos` (`:657-665`, `ControlIntrinsic`). The stub,
  `macroAssembler_aarch64_trig.cpp`, is "a optimized version of fdlibm sin/cos implementation ... adapted for AARCH64"
  (`:30-31`; Cavium/BellSoft, `:2`), with its changes listed at `:65-76` (tables as doubles, unrolled and vectorised
  loops, constants loaded from tables). It uses fused instructions: `fmaddd`/`fmsubd` at `:293`, `:296`, `:323`,
  `:339`, `:841`, `:853`, `:980`, `:983`, `:1006`, `:1067`, `:1200`, and `fmlavs` at `:795-808`. fdlibm's C has no
  fused operation, so **the aarch64 stub's bits need not equal `StrictMath.cos`'s, nor x86_64's Intel bits [I]**; what
  they are on the table was not measured (no aarch64 here) **[?]**. The report's step 4 `macos-26` run would measure it.
  **Consequence for E2 and step 4 [I]:** `-XX:-UseLibmIntrinsic` cannot be the no-intrinsic switch on an arm64 runner;
  HotSpot rejects an unknown `-XX` option at startup unless `-XX:+IgnoreUnrecognizedVMOptions` is set (standard
  behaviour, not read here). The switch there is `-XX:DisableIntrinsic=_dcos`.
- **`Math.atan2` is never intrinsified: it is the Java `FdLibm.Atan2` on every CPU and tier [R].**
  `library_call.cpp:1874-1877`: `// These intrinsics are not yet correctly implemented` / `case vmIntrinsics::_datan2:
  return false;`. C1's `do_MathIntrinsic` lists (`c1_LIRGenerator_x86.cpp:804-811`, `aarch64:747-753`) do not include
  `_datan2` (grep of both files for `datan2`: none). The interpreter's math entries
  (`templateInterpreterGenerator_x86_64.cpp:400-468`) are `fmaD`, `fmaF`, `sqrt`, `exp`, `log`, `log10`, `sin`, `cos`,
  `pow`, `tan`, `abs`; no `atan2`. No `SharedRuntime::datan2` exists in the files read (grep: `_datan2` appears only in
  `vmIntrinsics.hpp:163`, `vmIntrinsics.cpp:292`, `library_call.cpp:260`, `:1876`). So `Math.atan2` runs its Java body
  (`Math.java:611-613` → `StrictMath.java:580-582` → `FdLibm.java:1350` `Atan2`), pure IEEE operations, which Java
  forbids to contract. This explains part 6's `Math.atan2 == StrictMath.atan2` at 299,974 of 299,974 **[C: part 6]**,
  and it means `-XX:-UseLibmIntrinsic` touches nothing in `atan2`.

### Q5. Kotlin. **Answered: premise 4 confirmed.**

Kotlin `v2.3.10` (commit `679366a8`), `libraries/stdlib/jvm/src/kotlin/util/MathJVM.kt:18` `import java.lang.Math as
nativeMath`; `:57` `public actual inline fun cos(x: Double): Double = nativeMath.cos(x)`; `:119` `public actual inline
fun atan2(y: Double, x: Double): Double = nativeMath.atan2(y, x)` **[R]**. Both are `inline`, so a Kotlin call site on
Android holds a direct `java.lang.Math.cos` invocation and takes Q1's path **[I: the bytecode was not inspected]**.

### Q6. Apple, bounded. **Could not determine.** The commit that removed bionic's x86_64 assembly (HTTP 401 on the `+log` query; see the next
paragraph); who consumes `FDLIBM_CFLAGS` (defined at `flags-cflags.m4:985-998`; no reference in `CompileJvm.gmk`,
`make/hotspot/lib/JvmFlags.gmk` or `make/modules/java.base/lib/CoreLibraries.gmk`, all grepped at `eac07229`), so
whether `sharedRuntimeTrig.cpp` is built without contraction is unread; on x86_64 without `-mfma` no contraction
instruction exists, so part 8's no-intrinsic result stands regardless **[I]**, and the question matters on aarch64 only;
the HotSpot aarch64 stub's table; `math_private.h`'s 452 changed lines; `k_sin.c` and `e_rem_pio2.c` line for line;
whether the 13 cos rows reach `kx`/`ky`; a phone's actual `libm.so` bits. **Apple, bounded (run 2):** `git clone
--depth 1` of `apple-oss-distributions/Libm` succeeded: one tag, `Libm-2026`, commit `17a5f9da`, committed 2021-10-06;
`Source/` holds `ARM`, `Intel` and `PowerPC` directories and the tree has zero mentions of `arm64` or `aarch64`
**[R]**. That is a 32-bit-ARM-era library, not what today's arm64 iOS ships **[I]**; stopped there, as the dispatch
says. What Kotlin/Native's `cos` and `atan2` compute on iOS remains **[?]**.

**A run 1 evidence row that is not what its name says (run 2, [R]).** `sources.tsv`'s `log-x86_64-s_cos.S.json`
(512 bytes) is an HTML `302 Moved` page redirecting to a Google "sorry" address, not a commit log; run 1 recorded it
as a log and cited nothing from it. Any `+log` claim in run 1 that rests on that file is unsupported; the per-file
`+log` rows for the msun sources (`log-s_cos.c.json` and the others, 535 to 540 bytes) were not re-read and may be the
same **[?]**. The `+log` queries are the only googlesource reads refused here; every `+/<commit>/<file>?format=TEXT`
read succeeded.

**Premises that were wrong.** None of the dispatch's four. Of the Phase 1 report's text: E2 and step 4 treat
`-XX:-UseLibmIntrinsic` as a HotSpot-wide switch, and step 4 plans the `macos-26` (arm64) job around the same JDK
flags; the flag is x86-only (Q4). Part 7's "HotSpot's `Math.cos` on x86_64 is neither glibc's nor `StrictMath`'s"
stands. My own prediction 4 was wrong about aarch64 and about `atan2`'s mechanism, recorded above.

**Decided beyond scope.** Run 1: an `atan2` comparison was added to Q7's method (now run); the `-ffp-contract=on`
model build was withdrawn after Q3. Run 2: the pairs were regenerated rather than found (said so in Q7); three shim
lines stood in for bionic's libc headers, each read from bionic; `-lm` was linked for glibc's `floor` and `fabs`,
exact operations. No owner-level choice was made; the options are left as the report's H and E state them.

**Calls beyond the dispatch.** None that changed anything outside this branch. Run 1: about 110 single-file reads,
four `git ls-remote` tag listings, two Apple-related page requests. Run 2: eight single-file reads (`sources.tsv`), one
`+log` request refused with 401, one `git clone --depth 1` of Apple's published Libm into scratch (the dispatch's
"any source Apple publishes"; no account, no sign-up).

## Handoff: what remains, exactly

Run 1's items 1 to 5 and 8 are done above (1 and 2 in Q7 and Q2, 3 in Q2, 4 in Q2, 5 in Q3, 8 in Disclosures). Left:

1. **The commit removing `libm/x86_64/*.S`** (run 1's item 6): needs a `+log` read that this session's proxy refused
   with 401, or a shallow clone of bionic between `android-13.0.0_r1` and `android-14.0.0_r1`.
2. **`FDLIBM_CFLAGS`'s consumer** (run 1's item 7): grep the whole `make/` tree at `eac07229`; matters for aarch64 only.
3. **Whether the 13 `cos` rows change `kx`/`ky`:** run `TableRows.row` with msun's bits (`q7/results/msun-cos-clang.txt`
   column 3) and compare `kx`/`ky` against `jvm-strict.txt`; one small script.
4. **arm64 confirmation:** the same `q7/tools` harness on an arm64 host with Android's clang, or a phone's `libm.so`.
5. **`k_sin.c` and `e_rem_pio2.c` against fdlibm 5.3 line for line**, for completeness; their results agree at the
   table's inputs already.
6. **Apple:** what today's iOS libm computes is unread; Apple's published Libm is a pre-arm64 tree. Stop at a sign-up.
