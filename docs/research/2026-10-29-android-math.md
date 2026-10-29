# Do Android phones compute BRouter's cosine and atan2 as the test Java does?

Dispatch 2026-10-29-01 (`prompts/preserved/2026-10-29-01-android-math.md`), RECORD -19. Written by the Forager-iOS
researcher (first run, unnamed), 2026-10-29. Branch `research/android-math`, cut from `main` at `490b0e3`. Evidence
folder: `docs/research/2026-10-29-android-math/`.

**Marks.** Every claim is **[R]** read (URL, commit or tag, file and line, date), **[C]** computed (script and log in
the evidence folder), **[I]** inferred, or **[?]** could not determine.

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
| 2 | `libcore` `de876a01b`: `Math.cos`/`atan2` native, `Math.c` returns C's `cos`/`atan2` | taken from part 7's read (`09-float/part7/sources.txt`); re-read at a release tag below |
| 3 | HotSpot x86_64: `Math.cos` vs `StrictMath.cos` differ at 60 of 1,800 rows; 0 with `-XX:-UseLibmIntrinsic` | **[C]** evidence read: `rows-math-vs-strict.txt` says 60, `rows-nointrinsic-vs-strict.txt` says 0; recomputed below in Q7 on the same JDK build |
| 4 | `kotlin.math.cos` on the JVM calls `java.lang.Math.cos` | **unverified**; Q5 |

## Method, written before any source was fetched

**Reads.** One file per request, at a named tag or commit, through `tools/fetch.sh`, which logs date, bytes and SHA-256 of
each body to `downloads/sources.tsv` (the bodies stay in scratch; the log is committed as `sources.tsv`). Tags are resolved
to commits with `git ls-remote` (`tags/tag-commits.txt`). Pins:

- AOSP `art`, `bionic`, `libcore`, `build/soong` at `android-17.0.0_r1` (API 37, the newest release tag) and
  `android-8.0.0_r1` (API 26, Forager's `minSdk`). Intermediate tags only where the two ends differ.
- OpenJDK `jdk21u` at `jdk-21.0.12.1-ga` (commit `eac07229`), the tag of the JDK this container and the oracle run.
- Kotlin at `v2.3.10`, Forager's pinned version (`gradle/libs.versions.toml:7`).
- Apple: whatever Apple publishes, read at a named URL; nothing downloaded beyond a page.

**Q7, the measurement.** Where the implementation that bionic builds `cos` from is C that compiles here, it is compiled in
scratch with clang 18 and gcc 13 on x86_64 and run over the table's 1,800 inputs, `ilat = i * 100000 + 50000`,
`lat = (PI/180) * (ilat * 1e-6 - 90)`, exactly `CheapRuler.calcKxKyFromILat` (`CheapRuler.java:35-49` at `e2969abe`).
HotSpot's `Math.cos` and `StrictMath.cos` for the same inputs are recomputed with this container's JDK by part 8's
`TableRows.java` (copied unchanged into `tools/`), and those two files must equal the evidence's: the Math table byte for
byte against `09-float/part6/results/jvm-amd64/scale-table.txt` (sha256 recorded), and the Math-vs-Strict count must be 60
(`10-tests/logs/rows-math-vs-strict.txt`). If either check fails, the measurement is not cited.

- **A match** is bit equality of the 64-bit pattern of `cos(lat)` for a row. Two implementations match on the table when
  0 of 1,800 rows differ. The report gives the count of differing rows for each pair, and the rows.
- **Flags.** `-O2 -ffp-contract=off` (no fused multiply-add) is the primary build, because it is what the C text says.
  A second build with `-ffp-contract=on -mfma` is a model of what a compiler that contracts within expressions, as clang
  does by default on an FMA target such as arm64, would produce: clang decides which expressions to contract in its
  front end (`llvm.fmuladd`), so the set of contracted sites is the same for x86_64-with-FMA as for arm64, but the model
  is still a model, and is marked **[C, model]**, never claimed as arm64's bits. Whether bionic's build contracts is Q2's
  read of its flags; if it does not, the model is reported and not used.
- **Counts checked outside the script:** `wc -l` of each table file is 1,800; the row index of every line is its line
  number minus one; the HotSpot checks above.
- **`atan2`, a call beyond the dispatch's letter (Q7 names only `cos`).** Done because it is cheap once `cos` is built:
  the same C `atan2` over part 6's 299,974 integer pairs (the LCG of `ProbeTest.kt:5` and `:43-50`, seed 42, reproduced in
  Java and C), compared with HotSpot's `Math.atan2` and `StrictMath.atan2` from the same JDK. A match is bit equality of
  `atan2(dy, dx)`. The pair count must be 299,974 (part 6's figure) or the run is not cited.

## Predictions, mine, written before any source was fetched (the planner's are in RECORD -19, unread)

1. **ART:** `Math.cos` and `Math.atan2` are compiler intrinsics on arm64 and x86_64, implemented as a call to the C
   library's `cos`/`atan2` through a runtime entry point, not as inline code; the interpreter calls the native method,
   which also calls the C library. Every path ends at bionic's function. Confidence: high for the compilers, medium for
   the interpreter's own intrinsic list.
2. **bionic:** on arm64, FreeBSD's msun (fdlibm-derived) `s_cos.c`/`k_cos.c`/`e_rem_pio2.c` and `e_atan2.c`/`s_atan.c`;
   on x86_64, Intel-contributed assembly if it is still in the tree. No `-ffp-contract=off` in libm's build file, so the
   arm64 build may contract. Confidence: medium.
3. **API 26 to 37:** no change for double-precision `cos` and `atan2` on arm64 across the range; ARM's optimized
   routines were added for other functions. Confidence: medium.
4. **HotSpot:** x86_64 `Math.cos` is Intel's libm algorithm as a generated stub under `UseLibmIntrinsic`; aarch64 has a
   port of the same algorithm, and whether `-XX:-UseLibmIntrinsic` switches it there is the open point. `Math.atan2` has
   no stub on either: C2 calls `SharedRuntime::datan2`, a C++ fdlibm port, so it equals `StrictMath.atan2`. Confidence:
   high for x86_64 and `atan2`, low for the aarch64 flag.
5. **Kotlin:** `kotlin.math.cos` on the JVM is an inline call to `java.lang.Math.cos`. Confidence: high.
6. **Apple:** the current libm source is not published; could not determine. Confidence: high.
7. **Measurement:** msun `cos` without contraction differs from `StrictMath.cos` at a few rows (FreeBSD rewrote
   `k_cos.c`), and from HotSpot's `Math.cos` at about 60, like `StrictMath`; the contraction model differs from both at
   more rows. msun `atan2` equals both HotSpot values at all 299,974 pairs without contraction. Confidence: medium.

**Conclusion predicted:** yes, Android's results can differ from HotSpot's for BRouter's inputs, for `cos` on arm64
phones; `atan2` probably not. So a HotSpot oracle does not stand for today's Android app on `cos`, and option (a) would
change the table on phones at the switch wherever bionic and HotSpot differ.

## Answers

Not yet written. Each question is answered in its own commit, in order.
