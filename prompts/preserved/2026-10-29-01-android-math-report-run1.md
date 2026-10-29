# Do Android phones compute BRouter's cosine and atan2 as the test Java does?

Dispatch 2026-10-29-01 (`prompts/preserved/2026-10-29-01-android-math.md`), RECORD -19. Written by the Forager-iOS
researcher (first run, unnamed), 2026-10-29. Branch `research/android-math`, cut from `main` at `490b0e3`. Evidence
folder: `docs/research/2026-10-29-android-math/`.

**Status: partial, handed off at the planner's message (10:50Z, context past 180k).** Questions 1, 2, 4 and 5 are
answered; 3 and 6 are partial; 7, the measurement, was not reached. The handoff at the end says exactly which reads
and runs remain.

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
7. **Measurement:** not reached.

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

### Q2. bionic: which implementation, and flags. **Answered for API 37, with one symbol-level check left open.**

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
- **Flags [R].** libm's own: `:440-452` `-include freebsd-compat.h`, `-fno-builtin`, `-fno-math-errno`, warnings only.
  Global, `build/soong` at `android-17.0.0_r1` (commit `6722dd88`), `cc/config/global.go:138-139`:
  `// Turn off FMA which got enabled by default in clang-r445002 (http://b/218805949)` / `"-ffp-contract=off",`.
  The list it sits in also carries `-fno-exceptions` and `-fno-strict-aliasing` (`:141-147`), so it is the
  all-targets C/C++ list **[I: the list's name was not read]**. No `-ffp-contract` or fast-math flag in
  `cc/config/arm64_device.go` or `x86_64_device.go` (grep).
- **One thing open:** the libm module links `whole_static_libs: ["libarm-optimized-routines-math"]` (`:384`). If that
  library defined `cos` or `atan2`, it would duplicate msun's symbols in the same link, so it is inferred not to
  **[I]**; its source list was not read **[?]**.
- **On x86_64 at API 26 (bionic `f711b078`): Intel-contributed assembly [R].** `libm/Android.bp:448`
  `x86_64/s_cos.S`, `:436` `x86_64/e_atan2.S`, `:446` `x86_64/s_atan.S`, with `exclude_srcs` of the C files at `:458`,
  `:468`, `:470`. arm64 at API 26 used C for both (`:307-336`: only `ceil`, `fenv`, `fma`, `floor`, `lrint`, `rint`,
  `sqrt`, `trunc` in assembly).

### Q3. API 26 to 37. **Partial: the ends and one bisect read; the intermediate ART releases inferred.**

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

### Q6. Apple, bounded. **Could not determine.**

- `https://opensource.apple.com/source/Libm/` returned HTTP 404 on 2026-10-29 (`sources.tsv`) **[R]**.
- `https://api.github.com/repos/apple-oss-distributions/Libm` returned HTTP 403 from this session's proxy with "GitHub
  access to this repository is not enabled for this session" **[R]**: an access policy of this container, not an answer
  from GitHub. Whether Apple publishes any Libm source, and for which OS or CPU, is therefore **[?]** here.
- What is read: Kotlin/Native's `cos` and `atan2` are direct calls to the C library's (part 6,
  `09-float/part6/kn-reads.txt` 3: `Kotlin_math_cos -> tail call double @cos`), so on iOS they are Apple's system
  library's, and nothing about its algorithm or bits was determined **[I][?]**.

### Q7. The measurement. **Not reached.** Method above; handoff below.

## Conclusion

**Can Android's results for these two functions differ from HotSpot's, for BRouter's inputs?**

- **`cos`, arm64 phones: yes, from the oracle's default `Math.cos` [R][I].** Today's Android app computes CheapRuler's
  table with bionic's `cos`: FreeBSD msun, fdlibm's family, compiled without contraction, the same arithmetic at every
  API level 26 to 37 at the table's inputs (Q2, Q3). HotSpot x86_64's default `Math.cos` is Intel's libm (Q4), which
  differs from fdlibm at 60 of the 1,800 rows (part 8). Unless msun happens to reproduce Intel's bits, which nothing in
  the code arranges, Android phones differ from the default oracle on about those rows. **Whether Android equals
  `StrictMath.cos` (fdlibm) at all 1,800 rows is the measurement, not reached:** it turns on whether FreeBSD's `k_cos.c`
  body is fdlibm 5.3's, which was not read **[?]**.
- **`cos`, x86_64 Android (emulators, Chromebooks): API 26 to 33 Intel assembly, API 34 on msun [R].** Whether the
  Intel assembly's bits equal HotSpot x86_64's Intel stub was not measured **[?]**.
- **`atan2`: HotSpot is fdlibm 5.3 in Java everywhere (Q4); Android arm64 is FreeBSD's `e_atan2.c`/`s_atan.c`,
  fdlibm-derived, unfused.** Expected equal; not measured, and `s_atan.c`'s body against fdlibm's not read **[?]**.
- **HotSpot aarch64 is a third thing:** its `cos` stub uses fused multiply-add (Q4), so a HotSpot on an arm64 runner is
  not expected to reproduce either x86_64's or Android's table **[I]**.

**What it means for the oracle and the options, as H and E state them (not choosing):**

- **The oracle.** A HotSpot x86_64 oracle run with the default `Math.cos` does not stand for today's Android app on
  `cos`; the no-intrinsic oracle (`-XX:-UseLibmIntrinsic`, fdlibm) is the one that can, if Q7 shows msun equals
  `StrictMath` on the table. For `atan2` the two oracles are the same thing (Java fdlibm), and the question is only
  whether msun's `atan2` equals it. On an arm64 runner the switch is `-XX:DisableIntrinsic=_dcos`, not
  `-XX:-UseLibmIntrinsic`, and the default stub there is a fourth implementation.
- **(a) constants written on the CI JVM.** With the default JVM, the constants are Intel's bits: Android phones' table
  changes at the switch on the rows where Intel and msun differ (about 60 if msun is fdlibm). With the no-intrinsic
  JVM, the constants are fdlibm's: phones' table is unchanged at the switch if Q7 shows msun equals fdlibm.
- **(b) each platform's `cos`/`atan2`.** On Android the port calls `kotlin.math.cos` → `java.lang.Math.cos` → bionic's
  `cos` (Q5, Q1): the same bits as today, so Android routes do not change at the switch. iOS is Apple's library, unread.
- **(c) fdlibm ported.** The same everywhere; equal to today's Android phones on `cos` and `atan2` if Q7 shows msun
  equals fdlibm at the inputs, and equal to the no-intrinsic oracle on the JVM (part 8). Differs from the default
  x86_64 oracle at 60 rows and from the aarch64 stub at an unmeasured count.

## Sources read (commit and date; the full log with SHA-256 is `sources.tsv`)

| Repository | Pin | Files |
|---|---|---|
| `platform/art` | `android-17.0.0_r1` = `b753cf97923c3695338d21466fa14c57b480a59a`, read 2026-10-29 | `compiler/optimizing/intrinsics_arm64.cc`, `intrinsics_x86_64.cc`, `runtime/intrinsics_list.h`, `runtime/entrypoints/quick/quick_entrypoints_list.h`, `quick_entrypoints.h`, `quick_default_init_entrypoints.h`, `runtime/arch/arm64/entrypoints_init_arm64.cc`, `runtime/arch/x86_64/entrypoints_init_x86_64.cc`, `runtime/interpreter/` (listing), `interpreter_switch_impl-inl.h`, `interpreter_common.cc`, `interpreter_common.h`, `mterp/nterp.cc` |
| `platform/art` | `android-8.0.0_r1` = `699cb9a11b1dce1515a4f591274b90269962cfec` | `compiler/optimizing/intrinsics_arm64.cc`, `intrinsics_x86_64.cc`, `compiler/intrinsics_list.h`, `runtime/arch/arm64/entrypoints_init_arm64.cc`, `runtime/arch/x86_64/entrypoints_init_x86_64.cc`, `runtime/interpreter/interpreter_intrinsics.cc`, `runtime/entrypoints/quick/quick_entrypoints_list.h` |
| `platform/bionic` | `android-17.0.0_r1` = `06356e41c5ed7b12220b24c05ba4fdb873b126a7` | `libm/Android.bp`, `libm/` listings, `libm/upstream-freebsd/lib/msun/src/{s_cos,k_cos,k_sin,e_rem_pio2,k_rem_pio2,e_atan2,s_atan}.c`, `math_private.h`, `libm/freebsd-compat.h`, `libm/fpmath.h` |
| `platform/bionic` | `android-8.0.0_r1` = `f711b078b9656f7dbb52a046a7f992a450e8fa13` | `libm/Android.bp`, `libm/`, `libm/x86_64/`, `libm/arm64/` listings, the same seven msun files and `math_private.h`, `libm/x86_64/{s_cos,e_atan2,s_atan}.S`, `libc/private/bionic_asm.h` |
| `platform/bionic` | `android-9.0.0_r1` … `android-16.0.0_r1` (`tags/bionic-bisect-commits.txt`) | `libm/Android.bp` at each |
| `platform/build/soong` | `android-17.0.0_r1` = `6722dd8833db7482df1a2543ca3fcf67ddf0f7b1`; `android-8.0.0_r1` = `23b35797`; 12, 13, 14 (`tags/soong-bisect-commits.txt`) | `cc/config/global.go`; at 17 also `arm64_device.go`, `x86_64_device.go` |
| `platform/libcore` | `android-17.0.0_r1` = `4ebfb391ee0109d16abe6e2bc965724b940928c5`; `android-8.0.0_r1` = `8924c3d52f89a77d8f1067b85be8e69230638e3a` | `ojluni/src/main/native/Math.c`, `ojluni/src/main/java/java/lang/Math.java` |
| `openjdk/jdk21u` | `jdk-21.0.12.1-ga` = `eac07229cf6cacc8a22eb79f4929e7fed61338b7` | `opto/library_call.cpp`, `cpu/x86/stubGenerator_x86_64.cpp`, `stubGenerator_x86_64_cos.cpp`, `globals_x86.hpp`, `templateInterpreterGenerator_x86_64.cpp`, `c1_LIRGenerator_x86.cpp`, `vm_version_x86.cpp`, `cpu/aarch64/stubGenerator_aarch64.cpp`, `macroAssembler_aarch64_trig.cpp`, `globals_aarch64.hpp`, `templateInterpreterGenerator_aarch64.cpp`, `c1_LIRGenerator_aarch64.cpp`, `vm_version_aarch64.cpp`, `stubRoutines_aarch64.cpp`, `share/runtime/globals.hpp`, `sharedRuntime.hpp`, `sharedRuntime.cpp`, `sharedRuntimeTrans.cpp`, `sharedRuntimeTrig.cpp`, `stubRoutines.hpp`, `share/classfile/vmIntrinsics.cpp`, `vmIntrinsics.hpp`, `java/lang/Math.java`, `StrictMath.java`, `FdLibm.java`, `make/autoconf/flags-cflags.m4`, `make/hotspot/lib/CompileJvm.gmk` |
| `JetBrains/kotlin` | `v2.3.10` = `679366a83f99851b42f64795f10ed803ff011c73` | `libraries/stdlib/jvm/src/kotlin/util/MathJVM.kt` |
| `llvm/llvm-project` | `llvmorg-14.0.0` | `clang/docs/ReleaseNotes.rst` |
| Apple | 2026-10-29 | `opensource.apple.com/source/Libm/` (404); `api.github.com/repos/apple-oss-distributions/Libm` (403, session policy) |

Tag-to-commit maps: `tags/tag-commits.txt`, `tags/jdk21u-pinned.txt`, `tags/kotlin-pinned.txt`,
`tags/*-bisect-commits.txt` (all `git ls-remote`, `tags/ls-remote-date.txt`).

## Disclosures

**Confirmed vs inferred.** Marked inline. The inferences that carry weight: the intermediate ART releases route as both
ends do; Android 12 and earlier were compiled with a pre-clang-14 default; soong's `-ffp-contract=off` list is the
all-targets list; the aarch64 HotSpot stub's fused operations change bits; Kotlin's inline call compiles to a direct
`Math.cos` invocation.

**Could not determine.** Whether FreeBSD's `k_cos.c` and `s_atan.c` bodies are fdlibm 5.3's (the deciding read for
bit equality with `StrictMath`); any computed comparison (Q7); the HotSpot aarch64 stub's table; the commit that removed
bionic's x86_64 assembly; Android 12's clang version; `libarm-optimized-routines-math`'s symbol list; whether
`FDLIBM_CFLAGS` reaches `sharedRuntimeTrig.cpp`; `math_private.h`'s 452 changed lines; anything about Apple's library.

**Premises that were wrong.** None of the dispatch's four. Of the Phase 1 report's text: E2 and step 4 treat
`-XX:-UseLibmIntrinsic` as a HotSpot-wide switch, and step 4 plans the `macos-26` (arm64) job around the same JDK
flags; the flag is x86-only (Q4). Part 7's "HotSpot's `Math.cos` on x86_64 is neither glibc's nor `StrictMath`'s"
stands. My own prediction 4 was wrong about aarch64 and about `atan2`'s mechanism, recorded above.

**Decided beyond scope.** One: an `atan2` comparison was added to Q7's method (not run). The `-ffp-contract=on`
model build in the method commit was withdrawn after Q3 showed it would not stand for any Android release. No
owner-level choice was made; the options are left as the report's H and E state them.

**Calls beyond the dispatch.** None that changed anything outside this branch. Requests made: about 110 single-file
reads (`sources.tsv`), four `git ls-remote` tag listings, two Apple-related page requests.

## Handoff: what remains, exactly

1. **Q7, the measurement.** Fetch (per `sources.tsv` URLs, bionic `06356e41`) `s_cos.c`, `k_cos.c`, `k_sin.c`,
   `e_rem_pio2.c`, `k_rem_pio2.c`, `e_atan2.c`, `s_atan.c`, `math_private.h`, `freebsd-compat.h`, `fpmath.h` and
   `upstream-freebsd/android/include/machine/endian.h`; check whether `s_cos.c` includes `e_rem_pio2.c` inline
   (`INLINE_REM_PIO2`) before deciding the compile list. Build with `clang -O2 -ffp-contract=off -fno-builtin` (and gcc
   as a second compiler) on x86_64; run the 1,800 inputs and the 299,974 `atan2` pairs; run `TableRows.java` for
   HotSpot's values and check them against the evidence as the method says; write counts and differing rows to the
   evidence folder. If `k_cos.c` is fdlibm 5.3's, predict 0 rows against `StrictMath`.
2. **Read `k_cos.c` and `s_atan.c` bodies against fdlibm 5.3** (`sharedRuntimeTrig.cpp:571` `__kernel_cos`;
   `FdLibm.java:1232` `Atan`, `:1350` `Atan2`).
3. **`external/arm-optimized-routines`'s Android.bp at `android-17.0.0_r1`:** confirm `libarm-optimized-routines-math`
   defines neither `cos` nor `atan2`.
4. **Soong `global.go` at 17, lines about 95 to 120:** the name of the list holding `-ffp-contract=off`.
5. **Android 12's clang version** (`cc/config/global.go` `ClangDefaultVersion` at `android-12.0.0_r1`), to close the
   "before clang 14" inference.
6. **The commit removing `libm/x86_64/*.S`** (between `android-13.0.0_r1` and `android-14.0.0_r1`).
7. **`CompileJvm.gmk`:** whether `FDLIBM_CFLAGS` is applied to `sharedRuntimeTrig.cpp`.
8. **Apple:** a browser or an account with access to `apple-oss-distributions` on GitHub, which this session's policy
   refused; stop at a sign-up.
