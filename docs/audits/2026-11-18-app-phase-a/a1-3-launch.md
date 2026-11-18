# A1-3a: the app launched on the iOS simulator in CI, a screenshot, and what it shows (observe only)

Plumb Ledger, session 82, 2026-11-18. Branch `app`.

Base, confirmed at start: `git fetch origin`; `git status -sb` printed `## app...origin/app`; `git rev-parse --short HEAD`
and `git rev-parse --short origin/app` both printed `294d81a`; `git status --porcelain` printed nothing.

Spec: the pre-build report, sections 5, 7 (T9) and 8 (A1-3), read with
`git show origin/brouter-port-5b:docs/audits/2026-11-18-app-phase-a/a1-prebuild.md` (lines 363-443, 499-563); A1-2's
findings, `docs/audits/2026-11-18-app-phase-a/a1-2-xcode-project.md`. Governing rules: Forager's `CLAUDE.md`, read from
`/home/user/Forager/CLAUDE.md`.

The owner's rulings carried, verbatim: "Option B stands"; "I don't have a Mac and I don't care to get one"; "3 no iPhone";
"no coordinates are committed". So: unsigned simulator builds only, the placeholder identity
`com.zynergylabs.forager.ios.dev` / `Forager dev`, and every result here is **simulator-checked**, never "checked".
This step gates on the drawn-frame line and on the control only. The colour check on `after.png` is printed, not gating:
A1-3b gates it.

## 0. Step 0: `brouter.yml` on app-only pushes (RECORD -205)

Commit `5cbbf1b`: `on.push.paths-ignore` in `.github/workflows/brouter.yml` gains `app/**`, `iosApp/**` and
`.github/workflows/app.yml` beside `docs/**`, with a comment citing RECORD -205 and RECORD -204. Nothing else in that
file changed (`git diff --stat` printed `1 file changed, 6 insertions(+)`). `pull_request:` is unfiltered and was left so.

GitHub's rule, read today from `https://docs.github.com/en/actions/reference/workflows-and-actions/workflow-syntax`:
"If any path names do not match patterns in `paths-ignore`, even if some path names match the patterns, the workflow
will run." So **a mixed push runs the engine's workflow**: a push touching `app/App.kt` and `tools/png-region.py` runs
`brouter.yml`, because `tools/png-region.py` matches no ignored pattern. Only a push whose every changed file is under
`docs/`, `app/`, `iosApp/` or is `app.yml` skips it. The same page: `paths` and `paths-ignore` cannot both be used for
one event, and a diff of more than 3,000 files may not be filtered as expected. This dispatch's own commits that add
`tools/png-region*.py` are therefore mixed with respect to that rule and **will** run `brouter.yml` (inferred from the
rule; the run list is read in section 4). The `5cbbf1b` push itself touches `brouter.yml` and starts one full engine
run; it is not waited for here (the planner reads it).

## 1. Design (written before the workflow changed)

### Where the launch runs: `app-bundle` extended, not a new job

The launch steps are added to the end of the existing `app-bundle` job, after its bundle check. Reasons:
- The app launched is then, by construction, the one `xcodebuild` built in the same job, at
  `build/xcode/Build/Products/Debug-iphonesimulator/ForagerApp.app`; nothing older can be on a fresh runner.
- A separate job would need the `.app` carried by `upload-artifact`/`download-artifact`, which zips the files and does
  not keep Unix permissions (documented limitation of the artifact actions, not re-read today: **unverified**); the
  executable bit on the 39,640-byte stub and the `.debug.dylib` (A1-2 section 3) would be at risk, and a second macOS
  runner would cost a second cold boot.
- The cost: `app-bundle`'s time grows by the simulator's. The job's timeout stays 45 minutes.

The whole `.app` directory is installed (`xcrun simctl install "$UDID" "$APP"`), so the stub, `ForagerApp.debug.dylib`,
`__preview.dylib` and `compose-resources/` all go in; a step lists the installed container's files to show it.

### The pixel check: `tools/png-region.py`, and its self-test on Linux

Standard library only. 8-bit RGB (colour type 2) and RGBA (6), non-interlaced, filter types 0 to 4; anything else is
refused with exit 2 and the word UNSUPPORTED, never read as a colour. Reads the middle 80 % of the height, counts pixels
whose every channel is within a tolerance of the colour (alpha ignored), prints the header, the ancillary chunk names
(so an `iCCP`, `sRGB` or `cHRM` chunk, the colour-profile question of pre-build section 5, is visible), the three
commonest colours and the fraction. Exit 0 at or above the threshold, 1 below, 2 unreadable. The control tests for exit
**1** exactly, so an unreadable `before.png` cannot pass as a failed match.

`tools/png-region-test.py` writes synthetic PNGs with its own encoder, which applies each filter forward, and checks the
reader byte for byte on random pixels with each filter alone and all five mixed (type 3 included, which the pre-build's
prototype did not exercise), then the verdicts through the command line, the band, the commonest colours, and the
refusals. A Linux job `png-region` in `app.yml` runs it.

### The launch steps, in order

1. Print `xcrun simctl help` and `xcrun simctl help <sub>` for `list`, `create`, `boot`, `bootstatus`, `io`, `install`,
   `launch`, `listapps`, `get_app_container`, `spawn`, before any is relied on.
2. `xcrun simctl list devicetypes` and `list runtimes`; fail with a named message unless `iPhone 17` and
   `com.apple.CoreSimulator.SimRuntime.iOS-26-2` are both listed.
3. `xcrun simctl create a1-sim "iPhone 17" com.apple.CoreSimulator.SimRuntime.iOS-26-2`; `bootstatus -b` (boots and
   waits), with `timeout` around it.
4. `before.png` by `xcrun simctl io "$UDID" screenshot --type=png`. **Gating, the control:** `png-region.py` on it,
   colour `E0218A`, tolerance 24, threshold 0.98 must exit 1.
5. `simctl install`, then `simctl launch --terminate-running-process --stdout=<abs path> --stderr=<abs path>`; poll the
   stdout file up to 120 s for `A1: first frame drawn`. **Gating:** the job fails if the line does not come.
6. `after.png` the same way.
7. **Printed, not gating:** `png-region.py` on `after.png` at tolerance 24 (its exit status printed and ignored), so the
   size, colour type, chunks, commonest colours and fraction are in the log; `listapps` filtered to the bundle and
   `get_app_container`'s listing; whether `~/Library/Logs/DiagnosticReports` holds a report naming `ForagerApp`; whether
   the pid `launch` printed is alive 10 s after the screenshot (`kill -0`; simulator apps are host processes,
   inferred).
8. Artifact `a1-3-simulator`, `if: always()`: `before.png`, `after.png`, the app's stdout and stderr, the simctl
   output, the help text, any crash report.

Tolerance 24 for the control is deliberately the wide one: a wider tolerance makes the control harder to fail, so if the
home screen fails at 24 it fails at any narrower value A1-3b chooses.

## 2. Predictions (written before the workflow changed)

- **Does `println` from Kotlin/Native reach `simctl launch --stdout`? Predicted yes.** Kotlin 2.3.10's runtime
  writes `println` with an unbuffered `::write(STDOUT_FILENO, ...)` on every non-Android target
  (`kotlin-native/runtime/src/main/cpp/Porting.cpp:57-67` at tag `v2.3.10`, read today; `Console.kt:26` declares
  `println` external). `--stdout=<path>` should make fd 1 of the launched process that file. Because the write is
  unbuffered, the line should be in the file the moment it is printed, not at exit. What is **not** known: that
  `simctl launch --stdout` takes a host path as I expect (its help is printed first), and that the SwiftUI host does
  not reopen fd 1. If the line does not come, the next step is evidence (the stderr file, the unified log via
  `simctl spawn ... log show`), and only then the pre-build's `NSLog` fallback.
- `simctl list` shows `iPhone 17` and `iOS 26.2` (both seen in an `ios-smoke` log, pre-build section 5).
- `before.png` is RGBA or RGB, 8-bit, about 1206 x 2622 (iPhone 17's points times 3: **inferred**, not measured);
  its commonest colours are the home screen's wallpaper colours, the control fails it with a fraction near 0.
- `after.png`: the commonest colour in the band is `E0218A` or a profile-shifted neighbour of it, above 0.98 of the band.
  If the PNG carries an `iCCP` chunk naming Display P3, the stored value may differ from `E0218A` by more than a few
  units (inferred, not seen).
- The drawn-frame line appears within about 10 s of launch.
- The app's process is alive after the screenshot; no crash report.
- The extra time in `app-bundle`: create and boot 30 to 90 s, the rest under 30 s.
- `brouter.yml` runs on this dispatch's pushes that touch `tools/`; it does not run on a push touching only
  `app.yml` and `docs/`.

## 3. Results against predictions

All from run **44249852981** (`app`, head `8a1aafd`), job `app-bundle` attempt 2, job id **134628156762**, unless
another job is named. Line numbers are in that job's log as fetched with
`gh api repos/slayer8366/Forager-iOS/actions/jobs/134628156762/logs` (2,427 lines). All simulator-checked.

- **The drawn-frame line appeared, captured by `simctl launch --stdout`.** Log 2154-2161: `launch at 06:52:37`,
  `com.zynergylabs.forager.ios.dev: 17171`, `waited 8 s`, then under `--- app stdout` the line `A1: first frame drawn`,
  and `OK: the app printed 'A1: first frame drawn'`. The artifact's `app-stdout.txt` is 22 bytes, exactly
  `A1: first frame drawn\n` (`od -c`, read locally); `app-stderr.txt` is 0 bytes. **The prediction held**: `println`
  from Kotlin/Native reached the file; no `NSLog` fallback and no change to `app/` was needed. The 8 s include
  `simctl launch` itself, which returned after about 7 s (06:52:37 to the pid line at 06:52:44).
- **`simctl help`** (log 1297-1511, artifact `simctl-help.txt`): `launch` takes `--stdout=<path>`, `--stderr=<path>`,
  `--terminate-running-process`, and "(Cannot be combined with --stdout or --stderr)" for `--console`;
  `io ... screenshot [--type=<type>] [--display=<display>] [--mask=<policy>] <file or url>`, "Default is png";
  `listapps <device>`; `get_app_container <device> <id> [<container>]`, default `app`; `bootstatus -b` "Boot the device if
  it isn't already booted". Every flag the pre-build wrote from memory exists as written.
- **Device and runtime**: `iPhone 17 (com.apple.CoreSimulator.SimDeviceType.iPhone-17)` and
  `iOS 26.2 (...) - com.apple.CoreSimulator.SimRuntime.iOS-26-2` listed (artifact `devicetypes.txt:6`,
  `runtimes.txt:2`); log 1664 `OK: iPhone 17 and iOS 26.2 are both listed`. The image also lists iOS 26.4 and 26.5.
- **Boot**: create at 06:47:00 (log 1679), `bootstatus` reached `isTerminal=YES` at 06:48:57 after 1:54 and the step
  printed `booted 06:49:09` (log 2096). **Not predicted**: the next command, `xcrun simctl list devices | grep`, took
  2 min 2 s (its output is stamped 06:51:11, log 2097); cause not determined.
- **`before.png`, the control: failed as required.** Log 2115-2122: `1206x2622, bit depth 8, colour type 6, interlace 0`;
  chunks `sRGB eXIf`; band rows 262 to 2359 (2,530,188 pixels); commonest `FFFFFF` (246,718), `A1DF77` (28,263),
  `98E0D7` (14,649); `FAIL: 98/2530188 ... within 24 of E0218A`; `OK: the control: the colour check failed on
  before.png (exit 1)`. It is the home screen (viewed locally from the artifact: widgets, icons, wallpaper).
- **`after.png`, printed only**: `file` says `PNG image data, 1206 x 2622, 8-bit/color RGBA, non-interlaced`; `sips`
  says `space: RGB`, `profile: sRGB IEC61966-2.1`, `hasAlpha: yes`, `bitsPerSample: 8`; png-region (log 2234-2238):
  chunks `sRGB eXIf`, **commonest colour `E0218A`, 2,530,188 pixels, the whole band**, and the line
  `PASS: 2530188/2530188 pixels` (fraction 1, printed to four places) `within 24 of E0218A`, exit 0 (not gating). **The observed colour is the
  requested sRGB value exactly**: the pre-build's concern that a display profile would shift it (inferred there) did
  not occur; the PNG is tagged sRGB, not Display P3.
- **The install carried the whole `.app`**: `listapps` has an entry for `com.zynergylabs.forager.ios.dev`,
  `CFBundleDisplayName = "Forager dev"`, `ApplicationType = User` (artifact `listapps.txt:365-380`); the installed
  container holds `ForagerApp` (39,640 bytes, executable), `ForagerApp.debug.dylib` (38,876,776), `__preview.dylib`,
  `compose-resources/`, `Info.plist`, `PkgInfo` (log, section "the installed app container"), the same files and sizes
  as A1-2's build listing.
- **The process**: `pid 17171 is alive`, `ps` shows elapsed `00:23` and the command path inside the device's bundle
  container (log 2275-2277). So the simulator app is a host process visible to `kill -0` and `ps`, as inferred.
- **Crash reports**: `none in ~/Library/Logs/DiagnosticReports` (log 2279).
- **The simulator's log** for `process == "ForagerApp"`, last 3 minutes: 165 lines (artifact `sim-log.txt`), UIKit
  scene and keyboard lines naming `androidx.compose.ui.window.OverlayInputView`; nothing read as an error (the tail is
  in the log; the whole file was not reviewed line by line).
- **Time**: job `app-bundle` 06:40:28 to 06:53:52, **13 min 24 s**; the launch steps (help to the artifact)
  06:46:49 to 06:53:16, **6 min 27 s**, of which create and boot 4 min 11 s (2 min 9 s boot, 2 min 2 s the unexplained
  `list devices`), `before.png` and the control 1 min 0 s (10 s sleep, the screenshot written 30 s after the step began
  at log 2111, png-region about 29 s in pure Python on 3.2 million pixels), install and launch 35 s, after and the
  printed checks 27 s. The prediction (create and boot 30 to 90 s, the rest under 30 s) was too low on both.
- **`brouter.yml` on this dispatch's pushes**: `f7afc7a` (docs only) and `8a1aafd` (`app.yml` only) started no
  `brouter` run; `08c874c` and `002174c` (each touching `tools/`) started runs 44249664086 and 44249712556, as the rule
  predicts. `5cbbf1b`'s own run is 44249298838. None of the three was waited for or read here (the planner reads them).
- **The self-test on Linux**: job `png-region` 134628226791 (and 134617571535, attempt 1): `Python 3.12.3`,
  `png-region-test: 37 cases, 0 failed`, 37 lines `ok`.

### Cross-check from the artifact

Artifact `a1-3-simulator`, id 14405181718 (log 2347), downloaded with `gh api .../artifacts/14405181718/zip`, run
locally with `tools/png-region.py` (Python 3.13): `before.png` printed the same header, chunks, three colours and
`FAIL: 98/2530188`, exit 1; `after.png` the same `E0218A: 2530188 pixels`, `PASS`, exit 0, so CI and local agree.
Whole image (`--band 1`), tolerance 0: `after.png` `E0218A` 3,117,069 of 3,162,132, then `000000` 38,530 (the Dynamic
Island, seen in the image) and `FFFFFF` 3,339 (the status bar's clock and icons); `before.png` 0. Tolerance sweep on the
band (`before` / `after`): 0: 0 / all; 4: 0 / all; 8: 0 / all; 24: 98 / all; 48: 8,135 / all. SHA-256 of `after.png`
`890a0a3a...27c4bd`, of `before.png` `508f0bf7...a36505`. Nothing from the artifact is committed.

## 4. Runs and iterations

| Run, attempt | Head | `app-bundle` | What it showed |
|---|---|---|---|
| 44249664108 | `08c874c` | cancelled | superseded by `8a1aafd` before its macOS jobs started; its `png-region` job was in the same state as below |
| 44249712593 | `002174c` | cancelled | the same |
| 44249852981, attempt 1 | `8a1aafd` | **failure**, job 134617571020 | **red in A1-2's build step, before any launch step ran**: `xcodebuild: error: Unable to find a destination matching the provided destination specifier: { generic:1, platform:iOS Simulator }`, the only ineligible destination `Any iOS Device, error:iOS 26.0 is not installed` (log 223-227). Same image `macos-26-arm64`, version `20261112`, build `0504.1` as A1-2's green run 44246914973 (job 134607971756, 05:59 today), same command; this dispatch changed no step at or before it. |
| 44249852981, attempt 2 | `8a1aafd` | **success**, job 134628156762 | `gh run rerun --failed`, nothing changed: the build passed and every launch step ran as in section 3 |

One iteration, and it fixed nothing: the rerun was the evidence (the same commit and image green 8 minutes later), so the
destination failure is intermittent, not caused by this diff. Its cause is **not determined**: the inference is that
CoreSimulator had not finished loading the simulator runtimes when `xcodebuild` asked for destinations 23 s into the job,
but nothing in the log shows it. It is recorded as an open risk in section 7, not worked around.

## 5. Proposed for A1-3b: tolerance and threshold

Proposed: **tolerance 8 per channel, threshold 0.98, the middle 80 % band, colour `E0218A`.** Reasoning, from the one
observed run:
- The observed band is `E0218A` exactly, at every pixel, in an sRGB-tagged lossless PNG, so any tolerance from 0 up
  passes it. The tolerance only buys room for a future runner image or Compose release that dithers or colour-manages
  differently, which has not been seen; 8 is that room, kept small.
- The control's margin: the home screen has 0 band pixels within 8 of `E0218A` (98 at 24, 8,135 at 48), so at 8 the
  control fails it by the whole threshold, and so would any blank white or black window.
- The threshold 0.98 is the pre-build's and is not tight: the band is 100 %. It leaves room for an overlay the size of a
  status bar entering the band, not for a different screen.
- For A1-3b's revert (`App()` drawing a different colour), the colour chosen must differ from `E0218A` by more than 8 in
  at least one channel, or the check cannot go red; any colour clear of the home screen's commonest ones works.
- The control in A1-3a uses tolerance 24, which is wider than proposed; A1-3b can keep the control at the gating
  tolerance (8) or at 24. At 24 it is the stronger control (section 1); I would keep 24 for the control and 8 for the
  gate. That is a choice for the planner.

## 6. Confirmed vs inferred

Confirmed (simulator-checked, from the CI log lines cited in section 3, and the artifact read locally):
- `println` from the Kotlin/Native app reaches `simctl launch --stdout=<file>`; the line was in the file within 8 s.
- The control fails on the home screen; `after.png`'s band is `E0218A` at every pixel; both PNGs are 1206 x 2622,
  8-bit RGBA, non-interlaced, tagged sRGB, with `sRGB` and `eXIf` chunks.
- The installed app holds the stub and `ForagerApp.debug.dylib`; the process was alive about 23 s after launch; no crash
  report.
- The `simctl` flags used exist as written (their help printed); `iPhone 17` and iOS 26.2 exist on the image.
- GitHub's `paths-ignore` rule as quoted in section 0 (the docs page, read today); the run list matches it for this
  dispatch's four pushes.
- The self-test on Linux: 37 cases; and that it can fail: with the Average filter's left neighbour planted out of
  `tools/png-region.py` (saved copy at `scratchpad/s82/png-region.py.saved`, restored from it, `cmp` identical,
  `grep -c PLANTED` 0), `png-region-test: 37 cases, 10 failed`, every failure a case that uses filter 3 (e.g.
  `FAIL decode RGB filters (3,): 828 of 851 pixels differ`; `FAIL solid app colour, RGB, filter 3 passes at
  tolerance 0: png-region: FAIL: 40/1600 ...`), none in a case without it. Run locally (Python 3.13.18), not in CI.

Inferred:
- That the line was printed from the draw phase is the code's design (`app/src/commonMain/.../App.kt:22-25`, the line
  is printed inside `drawBehind`); the log shows the line and the colour, not which phase printed it.
- That `000000` in the whole image is the Dynamic Island and `FFFFFF` the status bar (from viewing the image).
- That the attempt-1 destination failure is a runner-side race and not this diff (one red, one green, same commit).

## 7. Could not determine

- **Why `xcodebuild` found no simulator destination on attempt 1.** One occurrence in the five `app-bundle` build attempts on
  this branch so far (`4b01c3b`, `a393570`, `93f8d47` green; `8a1aafd` attempt 1 red, attempt 2 green). If it recurs it will turn A1's gate red for a reason unrelated to the app.
  The next evidence, if the planner wants it, is a step before the build printing `xcrun simctl list runtimes` and
  `xcodebuild -showdestinations` (not added here: it changes A1-2's step and was not asked for).
- Why `xcrun simctl list devices` took 2 min 2 s after boot.
- Whether the same colour holds on other device types or runtimes (only `iPhone 17`, iOS 26.2 ran).

## 8. Premises in this brief or the pre-build that were wrong

- The pre-build's "Expected job time" (section 5): create, boot and the rest were predicted at 30 to 90 s and under
  30 s; measured 4 min 11 s and 2 min 2 s. The `app-bundle` job is now 13 min 24 s.
- The pre-build's caution that the stored colour may differ from the sRGB value (section 5, item 2, inferred there):
  not so on this image; the value is exact and the PNG is tagged sRGB.
- The pre-build's "1206 x 2622 is a test size of my choosing, not a measured screenshot size": it is the measured size.
- No premise of this brief was found wrong. Its statement that `paths-ignore` runs a push "unless ALL its changed files
  match" agrees with the docs.

## 9. Decided beyond scope

- The launch went into `app-bundle` (section 1's reasons) rather than a new job.
- The control requires exit 1 exactly, and the control's tolerance is the wide one, 24.
- `png-region.py` prints the ancillary chunk names, and `sips` was added to the printed checks, to answer the
  colour-profile question directly.
- A 10 s sleep before `before.png`, so the control reads the drawn home screen rather than a boot frame.
- The unified-log dump for the app's process, kept in the artifact, for diagnosis.
- `app.yml`'s `paths` gained `tools/png-region.py` and `tools/png-region-test.py`, and its header comment was updated
  (it said nothing there runs a simulator).
- Two superseded `app` runs (44249664108, 44249712593) were cancelled before their macOS jobs started, to free the
  runners for `8a1aafd`'s run, which holds the same jobs and more. No `brouter` run was cancelled.
- The failed job was rerun once unchanged (section 4) rather than changed.
- `08c874c` was pushed before its E1 scan was read; the scan printed 3 (three four-decimal fractions in the self-test's
  expected lines, not coordinates). Fixed in `002174c` by formatting them from the counts; the scan printed 0 there and
  on every other commit of this dispatch.

## 10. Not checked

- The `brouter.yml` runs for `5cbbf1b`, `08c874c` and `002174c`: not waited for and not read.
- Whether `simctl launch --stdout` keeps capturing after the first line (only one line is printed).
- The artifact's `sim-log.txt` beyond its last 40 lines.
- Whether the line or the colour would survive the app being backgrounded, rotated or relaunched.
- A1-3b's revert run, which belongs to A1-3b.
- E1: no coordinates are committed by this dispatch (the scan, section 9). The artifact's `before.png` shows the
  simulator's stock Maps widget of North America with no location marked; it is in an artifact, not in the repository.

# A1-3b: the colour check on `after.png` made gating, and a revert run that shows it going red

Plumb Ledger, session 83, 2026-11-18. Branch `app`. Appended; sections 0 to 10 above are A1-3a's and are not edited.

Base, confirmed at start: `git fetch origin`; `git status -sb` printed `## app...origin/app`; `git rev-parse --short HEAD`
and `git rev-parse --short origin/app` both printed `ed2fca8`; `git status --porcelain` printed nothing.

The planner's decisions (RECORD -206), as given: the colour check on `after.png` becomes gating with
`tools/png-region.py`, colour `E0218A`, tolerance 8 per channel, threshold 0.98, the middle band as the script defines it
(default `--band 0.8`, `tools/png-region.py:160`); the control on `before.png` stays at tolerance 24 and must still fail
with exit 1 exactly; before the `xcodebuild` build step, `xcrun simctl list runtimes` and `xcodebuild -showdestinations`
for the scheme are printed, not gating, with no retry or workaround. Governing rules: Forager's `CLAUDE.md`, Testing,
re-read today from `/home/user/Forager/CLAUDE.md` (the revert-check rules: a red counts only if the reverted build
compiled and ran, each failure must be one this revert could produce, and the forward state is confirmed after the
restore). Every result is **simulator-checked**.

## 11. Design and predictions (written before the workflow changed)

### The workflow change (one commit)

- A new step after "Generate the Xcode project" and before "Build the app for the simulator": prints
  `xcrun simctl list runtimes` and `xcodebuild -project iosApp/ForagerApp.xcodeproj -scheme ForagerApp
  -showdestinations`, each with its exit status, into the log and into `build/a1-3/`. `continue-on-error: true` and
  every command `|| echo`, so it cannot gate. It adds no retry, wait or change to the build step.
- The tolerance-24 printed check on `after.png` is replaced by a gating step of its own, right after `after.png`:
  `png-region.py build/a1-3/after.png --colour E0218A --tolerance 8 --threshold 0.98`; exit 0 passes; any other exit
  (1, a mismatch; 2, unreadable) fails the job with a line naming the exit status. The printed step keeps everything
  else it printed (`file`, `sips`, listapps, container, process, crash reports, the sim log) and still runs after a
  failure (`!cancelled()`), so a red colour check arrives with the same evidence as a green one.
- The control step is not changed.

### Predictions for the green run (step 2)

- The diagnostic step lists iOS 26.2 (and 26.4, 26.5, as in A1-3a) under `== Runtimes ==`, and `-showdestinations`
  lists at least `{ platform:iOS Simulator, ... name:Any iOS Simulator Device }` among available destinations. If the
  destination error recurs in the build, the prediction is that the diagnostic output shows the runtimes listed but no
  generic simulator destination available (inferred, the A1-3a race hypothesis; not seen).
- The control: unchanged from A1-3a, `FAIL: 98/2530188 ...` at tolerance 24, then `OK: the control: the colour check
  failed on before.png (exit 1)`.
- The drawn-frame line: `OK: the app printed 'A1: first frame drawn'`.
- The gate: `png-region: common colour E0218A: 2530188 pixels`, then `png-region: PASS: 2530188/2530188 pixels` (the
  fraction printed as 1 to four places) `within 8 of E0218A` (threshold 0.98 printed to four places), then my step's
  `OK:` line.

### Predictions for the revert run (step 3)

- The revert: `A1_COLOUR` at `app/src/commonMain/kotlin/com/zynergylabs/forager/app/App.kt:11` changes from
  `Color(0xFFE0218A)` to `Color(0xFF1E6FD9)`, a mid blue. Its distance from `E0218A` per channel is R 194, G 78, B 79,
  every channel far beyond 8. From the home screen's three commonest band colours in A1-3a (`FFFFFF`, `A1DF77`,
  `98E0D7`) its largest per-channel distance is 225, 131 and 122 respectively, so it cannot be mistaken for the home
  screen; it is also not black or white, the Dynamic Island and status-bar colours. Only that one constant changes.
- The build compiles (a constant of the same type): no line matching `^e: ` or ` e: ` in the xcodebuild log, and
  `** BUILD SUCCEEDED **` present.
- The control fails on `before.png` exactly as in the green run (the home screen does not depend on the app).
- The drawn-frame line appears (the line is printed by the same draw lambda, whatever the colour).
- The gate goes red, and only the gate: `png-region: common colour 1E6FD9: 2530188 pixels`, then the exact failure line
  expected, `png-region: FAIL: 0/2530188 pixels` (fraction 0 to four places) `within 8 of E0218A` (threshold 0.98 to
  four places), then my step's `FAIL:` line naming exit 1. If the stored value is not exactly `1E6FD9` (no colour
  management was seen in A1-3a, but this is a different colour), the count may differ from 0 only if some pixel is
  within 8 of `E0218A`, which a blue screen cannot be.
- The restore (one commit editing the constant back) makes `git diff <step-2 commit> HEAD -- app iosApp .github tools`
  print nothing, and its run is green as in step 2.

## 12. Step 2 runs (recorded as they arrive)

Commit `f56cbe5` (the workflow change), run **44255674662**.

- **Attempt 1: `app-bundle` red, job 134636660132, in the build step, the destination error again.** Image
  `macos-26-arm64`, version `20261112`, build `0504.1` (log line 18; written split, as in section 4, because the
  E1 scan reads the joined form as a four-decimal number), Xcode 26.0.1, build 17A400. The new diagnostic step
  (log 214-237): `xcrun simctl list runtimes` started at 07:01:52 and printed at 07:02:44, **52 s later**, listing
  `iOS 26.2 (26.2 - 23C54) - com.apple.CoreSimulator.SimRuntime.iOS-26-2`, iOS 26.4 and 26.5, and the tvOS, watchOS
  and visionOS 26.2/26.4/26.5 runtimes. `xcodebuild -showdestinations` (07:02:44 to 07:02:49) printed **no "Available
  destinations" section at all**, only `Ineligible destinations for the "ForagerApp" scheme:` and
  `{ platform:iOS, id:dvtdevice-DVTiPhonePlaceholder-iphoneos:placeholder, name:Any iOS Device, error:iOS 26.0 is not
  installed. ... }`; no exit line was printed, so it exited 0. One second later the build (log 262-267):
  `xcodebuild: error: Unable to find a destination matching the provided destination specifier:`
  `{ generic:1, platform:iOS Simulator }`, the same ineligible list, `SDKROOT = iphonesimulator26.0`, exit code 70.
  The other three jobs green: `app-klibs` 134636660184, `app-framework` 134636659789, `png-region` 134636660164.
- What this shows against A1-3a's inference (section 4: "CoreSimulator had not finished loading the simulator runtimes"):
  **the runtimes were listed by `simctl` five seconds before `xcodebuild` reported no simulator destination**, so
  "the runtimes were not yet loaded" in the plain sense does not explain it. That `simctl list runtimes` took 52 s
  suggests CoreSimulator was slow to come up on this runner (inferred). Why `xcodebuild` then saw no iOS Simulator
  destination at all is **not determined**.
- Rerun of the failed job with `gh run rerun 44255674662 --failed` at 07:07:26, nothing changed (result below).
- **Attempt 2 (the one rerun): `app-bundle` red again, job 134639110582, the same error.** 07:07:32 to 07:08:04.
  Image `macos-26-arm64`, version `20261112`, build `0504.1` (log 18). `simctl list runtimes` took **6 s** this time
  (07:07:48 to 07:07:55, log 214-229) and listed the same twelve runtimes, iOS 26.2 first; `-showdestinations` again
  printed no available destination, only the same ineligible `Any iOS Device` (log 235-236); the build failed one second
  later with `xcodebuild: error: Unable to find a destination matching the provided destination specifier:`
  `{ generic:1, platform:iOS Simulator }`, exit code 70 (log 262-267). So a slow CoreSimulator start is not needed for
  the failure either.
- **Run 44255674662 is red. Step 2 did not reach the launch steps in either attempt, so the gating colour check has not
  run yet, green or red.** The brief allows one rerun; two attempts have failed on the same symptom, so per Forager's
  `CLAUDE.md` (Bug fixing: "Two failed fix attempts on the same symptom means stop guessing") I stopped here. **Step 3,
  the revert run, was not pushed**: with the build failing before the app exists, a red from it would be the build, not
  the check, and the project's rule forbids citing that.

### What the runner headers show (read after attempt 2, more data rather than a third attempt)

Every `app-bundle` build on this branch, with the runner image each job's log names (log lines 16-20 of each):

| Run, attempt | Head | Job | Image version, build | Build step |
|---|---|---|---|---|
| 44245585616 | `4b01c3b` | 134603625845 | `20261112`, `0504.1` | `** BUILD SUCCEEDED **` |
| 44246226134 | `a393570` | 134605728656 | `20261102`, `0473.1` | `** BUILD SUCCEEDED **` (the run failed elsewhere) |
| 44246914973 | `93f8d47` | 134607971756 | `20261112`, `0504.1` | `** BUILD SUCCEEDED **` (log 1207), 05:59 |
| 44249852981, 1 | `8a1aafd` | 134617571020 | `20261112`, `0504.1` | destination error, 06:31 |
| 44249852981, 2 | `8a1aafd` | 134628156762 | `20261102`, `0473.1` | green, 06:40 |
| 44255674662, 1 | `f56cbe5` | 134636660132 | `20261112`, `0504.1` | destination error, 07:02 |
| 44255674662, 2 | `f56cbe5` | 134639110582 | `20261112`, `0504.1` | destination error, 07:07 |

So: every destination error so far is on image `0504.1`, and every one is after 06:30 today; `0504.1` built green twice
before 06:05; the one green after 06:30 was on the older image `0473.1`. The runner's macOS is `26.7.2` (`25G332`) on all
of them. Read from the log headers; **what changed is not determined**. Whether `0504.1` runners now fail every time,
or whether the earlier `0504.1` greens and today's reds differ in something the log does not print, is not known, and
the evidence does not license a fix. The diagnostic output now in every run is what the next look would start from.

## 13. Results, and the disclosure sections for A1-3b

Commits: `f45063c` (step 1, predictions), `f56cbe5` (step 2, the workflow change), `434e288` and `7ccd24d` (step 2's
record; see "Decided beyond scope"), and this section's commit. No commit touches `app/`.

### Confirmed (simulator-side facts from the CI logs cited above)
- The diagnostic step runs before the build and prints both outputs (jobs 134636660132 and 134639110582, log 214-237).
- The destination error recurred twice in a row on the same commit with nothing changed between attempts.
- When it occurs, `simctl list runtimes` lists iOS 26.2 and `xcodebuild -showdestinations` for the scheme lists no
  available destination at all, only the ineligible `Any iOS Device` (iOS 26.0 platform not installed).
- **A premise in A1-3a's section 4 was wrong (my own record):** it says the rerun was "the same commit and image green
  8 minutes later". The rerun's job 134628156762 ran on image `20261102`/`0473.1`, not `0504.1` (its log lines 16-20).
  The earlier "same image as A1-2's green run" statement (attempt 1 against 134607971756) is correct. So A1-3a's
  evidence that the error was intermittent on one image is weaker than written: the one recovery came with an image
  change.

### Inferred
- That the failure is on the runner side and not in this branch: the build step and everything before it are unchanged
  since `93f8d47`, which built green on the same image version. Not proven: the diagnostic step is new before the build.
  It cannot by itself explain attempt 1 of `8a1aafd`, which failed without it.
- That `simctl` taking 52 s in attempt 1 reflects a slow CoreSimulator start; attempt 2 took 6 s and failed the same way.

### Could not determine
- Why `xcodebuild` sees no iOS Simulator destination on these runners while `simctl` lists the runtimes.
- Whether it is tied to image `0504.1`, to a later change on those runners, or to something not printed.
- The gating check's behaviour on a real `after.png`, green or red: it has not run.

### Premises in the brief that were wrong
- The brief treats the destination error as intermittent ("seen once in A1-3a"). It has now failed three of the last
  four build attempts, and the one green among them was on a different image. "Intermittent" is not established.
- None other found.

### Decided beyond scope
- The diagnostic step is `continue-on-error: true` with a 5-minute timeout, so even a hang in it cannot gate; its two
  outputs are also kept in the `a1-3-simulator` artifact (`pre-build-runtimes.txt`, `pre-build-destinations.txt`).
- The printed tolerance-24 check on `after.png` was removed, replaced by the gating tolerance-8 one, which prints the
  same header, chunks and commonest colours.
- After the second red I read the runner image of every earlier `app-bundle` job (the table above) instead of a third
  attempt.
- `434e288` quoted the image version joined, which the E1 scan counts (scan printed **1**; a version string, not a
  coordinate). I fixed it in `7ccd24d` (scan 0) rather than resetting the unpushed commit, because Forager's
  `CLAUDE.md` forbids resetting or amending unpushed commits; both were pushed together, so **`434e288` is a pushed
  commit whose own scan printed 1**, the thing the brief said not to repeat. Recorded, not hidden.
- Stopped before step 3 (above).

### Not checked
- Steps 3 and 4 of the brief: the revert run, the restore, the green gating run.
- `brouter.yml`: the docs-only and `app.yml`-only pushes of this dispatch started no `brouter` run (run list read after
  `f56cbe5`); not read further.
- The A1 closing list is not written: A1's last gate has not run, so what A1 shows is still A1-3a's section 6 plus
  the two facts above.

## 14. Session 84 (A1-3b diagnosis): predictions, written before the diagnostic workflow exists

Coder "Plumb Ledger", session 84. This session gathers data on the `app-bundle` destination error of sections 12 and 13;
it fixes nothing and changes no existing workflow step. Base, confirmed at start: `git fetch origin`; branch `app`;
`git rev-parse --short HEAD` and `git rev-parse --short origin/app` both printed `5487223`; `git status --porcelain`
printed nothing. Governing rules: Forager's `CLAUDE.md` (Bug fixing: two failed attempts on one symptom means more
data, not a third hypothesis), read today from `/home/user/Forager/CLAUDE.md`. Every iOS result is **simulator-checked**.
Image versions are written as "version V, build B", never joined (the E1 scan, section 13).

### What was read before predicting (fetched today, 2026-11-18)

- **The image's software list on runner-images `main`**,
  https://github.com/actions/runner-images/blob/main/images/macos/macos-26-arm64-Readme.md: it still names image
  version 20261102, build 0473.1 (not 20261112). Xcode table: 26.6 (default, 17F113, also `/Applications/Xcode.app`),
  26.5, 26.4.1 (symlink `Xcode_26.4.app`), 26.3, 26.2, 26.1.1 (symlink `Xcode_26.1.app`), 26.0.1 (17A400, symlink
  `Xcode_26.0.app`). Installed simulators: iOS 26.2, 26.4 and 26.5 only (likewise tvOS, watchOS, visionOS). SDK
  table: the 26.0 SDKs belong to Xcode 26.0.1, 26.1 to 26.1.1, 26.2 to 26.2 and 26.3, 26.4 to 26.4.1, 26.5 to 26.5
  and 26.6 (as summarised by the fetch; not read cell by cell).
- **The image's toolset on runner-images `main`**,
  https://raw.githubusercontent.com/actions/runner-images/main/images/macos/toolsets/toolset-26.json: per Xcode,
  `install_runtimes` is `"none"` for 26.6, 26.3, 26.1.1 and **26.0.1**; `"default"` for 26.5 and 26.4.1; and for
  26.2 `iOS: ["26.2"]` plus the default watchOS, tvOS, visionOS. Runtimes are installed by `xcodebuild
  -downloadPlatform <platform> [-buildVersion <v>]` (`images/macos/scripts/helpers/Xcode.Installer.psm1:209,216`, same
  date). So no image of this toolset installs the iOS 26.0 platform that Xcode 26.0.1 names as missing.
- **runner-images policy**, https://github.com/actions/runner-images/blob/main/README.md line 136: "only three
  major.minor versions of platform tools and simulator runtimes will be available for installed Xcode".
- **The two release notes.** https://github.com/actions/runner-images/releases/tag/macos-26-arm64%2F20261112%2E0504
  (released 13 Nov 02:12): its Added, Updated and Deleted lists name no Xcode, simulator runtime or Apple SDK (only
  the `Xcodes` utility, 2.1.0 to 2.2.0, OpenSSL, .NET, browsers and the like).
  https://github.com/actions/runner-images/releases/tag/macos-26-arm64%2F20261102%2E0473 (03 Nov) likewise names none.
- **Issue #13570**, https://github.com/actions/runner-images/issues/13570, "[macOS] Deprecation of simulator runtimes
  for Xcode 26.0.1 on macOS 15 on January 26th" (closed 2026-02-18). It is about **macOS 15**, not macos-26; it gives
  the workaround `xcodebuild -downloadPlatform <iOS|...> -buildVersion <v>`, and a user (not a maintainer) reports the
  download as about 10 GB and 14 minutes.
- **Apple**, "Downloading and installing additional Xcode components" (read from its JSON form,
  https://developer.apple.com/tutorials/data/documentation/xcode/downloading-and-installing-additional-xcode-components.json):
  platform support and simulator runtimes are optional components; command line `xcodebuild -downloadPlatform
  <iOS|watchOS|tvOS|visionOS> [-exportPath <p> -buildVersion <v> -architectureVariant <universal|arm64>]`,
  `-downloadAllPlatforms`, `-importPlatform <dmg>`; newer components found by `-checkForNewerComponents` are stored in
  `~/Library/Developer/Packages/`. **The page names no on-disk location for installed runtimes or platform support.**
  So the diagnostic does not guess one: it prints `xcrun simctl runtime list -v` (which prints each runtime's own
  path), `xcrun simctl runtime match list -v` (which SDK maps to which runtime), and lists the directories those
  outputs and common practice point at (`/Library/Developer/CoreSimulator/Images`, `/Library/Developer/CoreSimulator/Volumes`,
  `/Library/Developer/CoreSimulator/Cryptex`, `/System/Library/AssetsV2/com_apple_MobileAsset_*Simulator*`,
  `~/Library/Developer/Packages`, `~/Library/Developer/CoreSimulator`, each Xcode's `Contents/Developer/Platforms`).
  Which of these exist is a result, not a premise.

### The diagnostic's design

- `.github/workflows/app-diagnose.yml`, new; nothing else in `.github/` changes. `workflow_dispatch`, and push to `app`
  when that file changes. `permissions: contents: read`. `runs-on: macos-26`, a matrix of 6 replicas
  (`fail-fast: false`) so 6 separate VMs are sampled in one run, 25-minute timeout. Actions pinned by the SHAs
  `app.yml` uses.
- Each replica, every command ending `|| echo`: image (`$ImageOS`, `$ImageVersion`, and the job header's own lines),
  a machine identifier (the first 12 hex of the SHA-256 of `hostname` and of the IOPlatformUUID; neither printed
  raw), `sysctl hw.model`, `sw_vers`, `date -u`; `ls -d /Applications/Xcode*.app` and `xcode-select -p`; CoreSimulator's
  version; `xcrun simctl list runtimes`, `xcrun simctl help runtime`, `xcrun simctl runtime list -v`, `xcrun simctl
  runtime match list -v`; the directory listings above; XcodeGen fetched with `tools/fetch-pinned.sh` exactly as
  `app-bundle` does and `xcodegen generate`; then **for each installed Xcode** (real directories, symlinks resolved and
  de-duplicated), with `DEVELOPER_DIR=<app>/Contents/Developer` and no `xcode-select -s`: `xcodebuild -version`,
  `-showsdks`, `-showdestinations` for the scheme, and one summary line ("Available destinations" section yes/no;
  any `platform:iOS Simulator` entry in it yes/no; the first `error:` text). **Xcode 26.0.1 runs first and again last**,
  so that if a newer Xcode's first `xcodebuild` changes CoreSimulator's state, the before/after pair for 26.0.1 shows
  it (an order effect is a possibility, not a hypothesis I hold). Nothing is built. Everything is uploaded as one
  artifact per replica.
- Side effect, recorded in advance: `brouter.yml` runs on any push whose changed files are not all in its
  `paths-ignore` (`docs/**`, `app/**`, `iosApp/**`, `app.yml`), so the push adding `app-diagnose.yml` will also start
  a `brouter` run. `brouter.yml` may not be edited, so this is accepted, not avoided.

### Predictions

1. **Image**: every replica `macos-26-arm64`, version 20261112, build 0504.1 (the release has been out since 13 Nov),
   macOS 26.7.2 (25G332). Possibly an odd replica on version 20261102, build 0473.1 if the rollout is mixed, as
   job 134628156762 was at 06:40 today.
2. **Machine identifiers**: all 6 different (each job is a fresh VM). That makes "the same machine" untestable from
   inside a job; what the matrix samples is VMs of one image, not physical hosts.
3. **Xcodes**: `Xcode_26.0.1`, `26.1.1`, `26.2`, `26.3`, `26.4.1`, `26.5`, `26.6` plus the symlinks `Xcode_26.0`,
   `26.1`, `26.4`, `26.5.0`-style aliases and `Xcode.app`; `xcode-select -p` the default, Xcode 26.6 (or its
   `Xcode.app` alias).
4. **Runtimes**: `simctl list runtimes` lists iOS 26.2, 26.4, 26.5 and the tvOS, watchOS, visionOS 26.2/26.4/26.5, as
   in jobs 134636660132 and 134639110582. No iOS 26.0 or 26.1 runtime on any replica.
5. **Per Xcode `-showdestinations`** (the core prediction; inferred from the toolset, not seen):
   - **26.0.1**: no "Available destinations" section, the ineligible "Any iOS Device, error:iOS 26.0 is not
     installed" line, no `platform:iOS Simulator` entry, on **every** replica, first and last pass alike.
   - **26.1.1**: the same shape, naming iOS 26.1.
   - **26.2 and 26.3** (iOS SDK 26.2; runtime 26.2 installed): an available section with `platform:iOS Simulator`
     entries.
   - **26.4.1** (SDK 26.4; runtime 26.4): available, with iOS Simulator entries.
   - **26.5 and 26.6** (SDK 26.5; runtime 26.5): available, with iOS Simulator entries.
6. **What this prediction cannot explain**: two `app-bundle` builds under Xcode 26.0.1 went green on version
   20261112, build 0504.1 earlier today (jobs 134603625845 and 134607971756, section 12's table). If prediction 5
   holds on every replica, those greens stay unexplained by this data. If **any** replica shows an available iOS
   Simulator destination under 26.0.1, the finding is that VMs of one image differ, and the per-replica outputs
   (runtime match list, directory listings) are where the difference would be read.
7. If every replica shows an available iOS Simulator destination under 26.0.1, the failure has gone away for now, and
   I say so and rerun nothing.

Note on the commit of this section (`4e090cb`): its two release URLs carried the tag joined with a dot, which the E1 scan
counts (it printed **2**; release tags, not coordinates). Fixed in the next commit by writing the dot as `%2E` (the same
URL); not amended or reset, by Forager's `CLAUDE.md` push-before-you-tidy rule. Both were pushed together; the diff
`origin/app` (`5487223`) to the fix scans 0.

## 15. Session 84 results: six replicas, the table, the answers, options for the planner

Commit `bccce50` adds `.github/workflows/app-diagnose.yml` (step 2). Run **44257759986** (`app-diagnose`), all six jobs
completed green (green means only that the jobs ran; no command in them can fail). Jobs: replica 1 134643447484,
2 134643447431, 3 134643447539, 4 134643447096, 5 134643447399, 6 134643447611; started 07:17:52 to 07:20:53 UTC.
Artifacts `app-diagnose-replica-1` to `-6` hold every output. Every result here is **simulator-checked**: nothing was
built or launched.

### The table (each cell read from that replica's `99-summary.txt`, the log line `SUMMARY replica=N pass=P ...`)

"Sim" is the count of `platform:iOS Simulator` entries inside the "Available destinations" section; "none" means the
section is absent and the only entry is the ineligible "Any iOS Device, error:iOS 26.x is not installed".

| Replica | Job | Image version, build (log line 18) | Hostname hash | 26.0.1 (first) | 26.1.1 | 26.2 | 26.3 | 26.4.1 | 26.5 | 26.6 | 26.0.1 (last) |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | 134643447484 | 20261102, 0473.1 | b44d1e73525c | none (26.0) | none (26.1) | 34 | 34 | 34 | 34 | 34 | none (26.0) |
| 2 | 134643447431 | 20261102, 0473.1 | 584909cf0cc2 | none (26.0) | none (26.1) | 34 | 34 | 34 | 34 | 34 | none (26.0) |
| 3 | 134643447539 | 20261102, 0473.1 | bd677c6411fd | none (26.0) | none (26.1) | 34 | 34 | 34 | 34 | 34 | none (26.0) |
| 4 | 134643447096 | 20261112, 0504.1 | 626e015dacac | none (26.0) | none (26.1) | 34 | 34 | 34 | 34 | 34 | none (26.0) |
| 5 | 134643447399 | 20261112, 0504.1 | 6f9126b87987 | none (26.0) | none (26.1) | 34 | 34 | 34 | 34 | 34 | none (26.0) |
| 6 | 134643447611 | 20261102, 0473.1 | 7aba08b77eb6 | none (26.0) | none (26.1) | 34 | 34 | 34 | 34 | 34 | none (26.0) |

Per Xcode (replica 1's files, identical in substance on all six): the iOS Simulator SDK from `-showsdks` and the OS
versions the available iOS Simulator destinations offer.

| Xcode (build) | iOS Simulator SDK | iOS SDK build, and the runtime `simctl runtime match list -v` chooses for it | Destination OS versions |
|---|---|---|---|
| 26.0.1 (17A400) | `iphonesimulator26.0` | 23A339, chosen "23A339" (no named runtime) | none |
| 26.1.1 | `iphonesimulator26.1` | 23B77, chosen "23B77" (no named runtime) | none |
| 26.2 | `iphonesimulator26.2` | 23C53, chosen "iOS 26.2 (26.2 - 23C54)" | 26.2, 26.4.1, 26.5 |
| 26.3 | `iphonesimulator26.2` | 23C57, chosen "23C57" (no named runtime) | 26.2, 26.4.1, 26.5 |
| 26.4.1 | `iphonesimulator26.4` | 23E252, chosen "23E252" (no named runtime) | 26.2, 26.4.1, 26.5 |
| 26.5 | `iphonesimulator26.5` | chosen "iOS 26.5 (26.5 - 23F77)" | 26.2, 26.4.1, 26.5 |
| 26.6 (17F113, default) | `iphonesimulator26.5` | 23F81a, chosen "23F81a" (no named runtime) | 26.2, 26.4.1, 26.5 |

Other per-replica facts (`00-machine.txt`, `01-xcodes.txt`, `02`, `04`, `06`): macOS 26.7.2 (25G332), `VirtualMac2,1`,
3 CPUs, CoreSimulator 1051.55 on all six; `xcode-select -p` is `/Applications/Xcode_26.6.app/Contents/Developer`;
`Xcode_26.0.app`, `26.1.app`, `26.4.app` and the `.0` names are symlinks; `simctl list runtimes` prints the same twelve
runtimes on all six (iOS 26.2 23C54, 26.4.1 23E254a, 26.5 23F77, and tvOS, watchOS, visionOS 26.2/26.4/26.5), every
one `State: Ready`, each a "Patchable Cryptex Disk Image" under
`/System/Library/AssetsV2/com_apple_MobileAsset_iOSSimulatorRuntime/<hash>.asset/AssetData/Restore/` mounted at
`/Library/Developer/CoreSimulator/Volumes/iOS_<build>`; the iOS images are 9.7G to 9.9G each (`runtime list -v`). The
runtime set, states and image paths hash identically on all six once the per-VM UUIDs and listing order are removed.
`/Library/Developer/CoreSimulator/Images`, `.../Profiles/Runtimes` and `~/Library/Developer/Packages` are absent.
`~/Library/Developer/Xcode/SDKToSimulatorIndexMapping.plist` is 13033 bytes dated Nov 2 05:31 on the 0473.1 VMs and
13201 bytes dated Nov 12 16:15 on the 0504.1 VMs: image-build dates, not touched during the jobs; its contents were not
printed. The platform-UUID hash is the same on every VM of one image (`ce43e64017f5` on 0473.1, `1ae894a6e5a6` on
0504.1), so it identifies the image's VM template, not a machine; the hostname hash differs on all six.

### Against the predictions (section 14)

1. Image: **wrong.** I predicted all six on version 20261112, build 0504.1, with possibly an odd one on the older image;
   four of six ran on version 20261102, build 0473.1. Both versions are in rotation now.
2. Identifiers all different: right for the hostname hash; the platform-UUID hash is per image, not per VM.
3. Xcodes and `xcode-select -p`: right.
4. Runtimes: right, on all six.
5. Per Xcode: right on every cell of the table, including 26.1.1.
6. **What the data cannot explain, now sharper than predicted:** the image version is not the variable. Job
   134628156762 (version 20261102, build 0473.1) built green at 06:40 today with the identical `xcodebuild` command under
   Xcode 26.0.1 (its log 199-220: the command, `SDKROOT = iphonesimulator26.0`; log 1207 `** BUILD SUCCEEDED **`), and
   its own `simctl list runtimes` (log 1652-1654) listed the same three iOS runtimes, no 26.0. At 07:18, four VMs of
   that same image version show no iOS Simulator destination at all under Xcode 26.0.1. Likewise 0504.1 was green at
   05:59 (job 134607971756) and red from 06:31. Timeline of `app-bundle` under 26.0.1 today: green 05:38 (job 134603625845, 0504.1) and 05:59;
   red 06:31; green 06:40; red 07:02, 07:07; and 12 of 12 diagnostic passes at 07:18 to 07:21 show no destination.
7. Not the case: the failure has not gone away.

### The four answers

- **(a) Do machines of the same image version differ?** Not in anything this diagnostic measured. Within each image
  version, and across the two, the runtime set and states, the runtime match list, the SDKs and the 26.0.1 destination
  output are identical (normalised hashes, all six). The only differences seen are listing order, per-VM UUIDs,
  hostnames, and the per-image index plist above. Six VMs sampled, 4 of one version and 2 of the other.
- **(b) Under Xcode 26.0.1, is the iOS 26.0 platform present anywhere?** No: on 0 of 6 replicas, in both the first
  and the last pass. No iOS 26.0 runtime exists on disk on any VM, and `simctl runtime match list -v` under 26.0.1
  maps its iOS SDK (23A339) to no named runtime.
- **(c) Which Xcode versions show an available iOS Simulator destination on every replica?** 26.2, 26.3, 26.4.1, 26.5
  and 26.6, each with 34 iOS Simulator entries on all six, offering runtimes 26.2, 26.4.1 and 26.5. 26.0.1 and 26.1.1
  show none on any replica.
- **(d) What runner-images says** (all fetched today):
  - The toolset for this image, https://raw.githubusercontent.com/actions/runner-images/main/images/macos/toolsets/toolset-26.json:
    `"install_runtimes": "none"` for 26.0.1 (and 26.1.1, 26.3, 26.6); iOS 26.2 explicitly for 26.2; the defaults for
    26.4.1 and 26.5. So **no current macos-26 image is built to give Xcode 26.0.1 its iOS platform.**
  - Policy, https://github.com/actions/runner-images/blob/main/README.md line 136: "only three major.minor versions
    of platform tools and simulator runtimes will be available for installed Xcode".
  - Release notes for both versions (URLs in section 14): no Xcode, runtime or Apple SDK change in either.
  - The image README on `main`, https://github.com/actions/runner-images/blob/main/images/macos/macos-26-arm64-Readme.md:
    still the 20261102 version; simulators iOS 26.2, 26.4, 26.5 only.
  - Issues: no macos-26 issue for "iOS 26.0 is not installed" was found (issue search via `gh api search/issues`,
    queries listed in the session; `gh api` on the repository's own endpoints was refused, so pages were read on the
    web). The nearest: https://github.com/actions/runner-images/issues/13570 (macOS 15, January 2026), the same
    removal of Xcode 26.0.1's runtimes on macOS 15, workaround `xcodebuild -downloadPlatform <platform> -buildVersion
    <v>`, a user's report of "10GB" and "14 minute delay" (not a maintainer's figure), and the maintainer restoring the
    iOS runtime there on request. https://github.com/actions/runner-images/issues/13435 (December 2025) shows that
    macos-26 images then **did** carry iOS 26.0 and 26.1 runtimes; they are gone now. When they went is not determined.

### Kotlin's side (step 4; primary sources fetched today; no version changed)

- Kotlin's compatibility page, https://kotlinlang.org/docs/multiplatform/multiplatform-compatibility-guide.html: the
  row for Kotlin Multiplatform plugin 2.3.10 lists Xcode **26.0** (also 2.3.0 and 2.3.20-2.3.21; 2.4.0 onward lists
  26.4). The page says nothing about a newer Xcode.
- Kotlin's source at tag `v2.3.10`: `XcodeVersionService.kt` reports `XcodeVersionTooHighWarning` when the selected
  Xcode is above `XcodeVersion.maxTested` and `kotlin.apple.xcodeCompatibility.nowarn` is not set; the diagnostic is
  severity WARNING (`KotlinToolingDiagnostics.kt:1493`), text "The selected Xcode version (...) is higher than the
  maximum known to the Kotlin Gradle Plugin. Stability in such configuration hasn't been tested". It is a warning, not
  an error. But `native/utils/src/org/jetbrains/kotlin/konan/target/Xcode.kt:46` at that tag reads
  `val maxTested = XcodeVersion(16, 0)`, which contradicts the page's "26.0": by the source, the Xcode 26.0.1 in use
  today is already above `maxTested`. Yet neither `app-framework` job 134636659789 nor green `app-bundle` job
  134607971756 contains the warning's text ("Too High", "maximum known", "Maximum tested": no match). Whether the
  check runs in this build at all is **not determined**.
- So, from these sources: moving to Xcode 26.2 or later goes past the Xcode the Kotlin page names for 2.3.10, and the
  plugin's documented reaction is at most a suppressible warning, not a failure. Whether Kotlin/Native 2.3.10 links
  correctly against a 26.2-to-26.5 SDK is not established by any source read; only a build would show it.

### Options for the planner (options only; none applied)

1. **Build `app-bundle` with Xcode 26.2** (SDK 26.2; the iOS 26.2 runtime the launch already boots).
   For: an iOS Simulator destination on 6/6 VMs of both image versions; the toolset installs iOS 26.2 for 26.2 by
   name, so it is the most deliberately provided pair. Against: past the Xcode 26.0 Kotlin's page names for 2.3.10;
   `app-framework` links under 26.0.1, so the two jobs would differ unless both move; runner-images keeps "three"
   runtime versions, so 26.2's runtime will in time be dropped too.
2. **Build with Xcode 26.4.1, 26.5 or 26.6** (6/6 available, same evidence). Further from Kotlin's documented 26.0;
   26.6 is the image default (no `xcode-select` needed) but has no runtimes installed for it in the toolset.
3. **Name a concrete simulator destination (by id or name) under 26.0.1.** Against, from the data: under 26.0.1 there
   is no available iOS Simulator destination of any kind on any VM, the platform itself is reported "not installed",
   so a named destination would be expected to fail the same way (inferred; not run).
4. **Download the iOS 26.0 platform in the job** (`xcodebuild -downloadPlatform iOS -buildVersion 26.0` under 26.0.1,
   Apple's documented command). For: keeps Xcode at 26.0.1, Kotlin's documented line. Against: each installed iOS
   runtime on these VMs is 9.7G to 9.9G; a user on #13570 reports about 10 GB and 14 minutes; no cache (T8), so every
   run pays it; adds a dependency on Apple's download service. Not timed here.
5. **Retry and hope.** Against: 12 of 12 passes on both image versions show no destination; the greens are unexplained.

**My recommendation (mine, not a decision):** option 1, Xcode 26.2 for `app-bundle`, first as a single
confirming run, read for `** BUILD SUCCEEDED **`, the in-xcodebuild framework link, and any Kotlin Xcode warning; with
the planner deciding whether `app-framework` moves with it. Option 4 is the one that keeps Kotlin's documented Xcode,
at a cost I have not measured.

### Confirmed (CI logs and artifacts cited above)
- 26.0.1 and 26.1.1 have no available iOS Simulator destination on 6/6 VMs, both images, both passes for 26.0.1;
  26.2 to 26.6 have 34 on 6/6.
- No iOS 26.0 runtime is on any sampled VM; the runtime set is identical on all six.
- Both image versions are in rotation at 07:17 to 07:21 today, and both fail the same way under 26.0.1.
- Image 20261102, build 0473.1 built green under 26.0.1 at 06:40 today with the same runtimes listed.
- The toolset installs no runtimes for 26.0.1; neither release changed Xcode or runtimes.

### Inferred
- That Xcode 26.x counts its iOS platform as installed only when a runtime of its own SDK's major.minor is present
  (consistent with all 7 Xcodes on all 6 VMs; not documented in anything read).
- That the earlier greens depended on something outside the image's disk state (for example something fetched or
  cached at run time). Nothing printed shows what.
- That a concrete destination under 26.0.1 would fail too (option 3).

### Could not determine
- Why 26.0.1 resolved a generic iOS Simulator destination at 05:59 and 06:40 today and not since, on the same images.
- The contents of `SDKToSimulatorIndexMapping.plist` (not printed) and whether Xcode refreshes it or any similar index
  over the network.
- Whether Kotlin's Xcode-version check runs in this build, given `maxTested = 16.0` in the source and no warning in
  the logs.
- When runner-images stopped shipping iOS 26.0/26.1 runtimes on macos-26.

### Premises in this brief that were wrong
- "Failing jobs ran on image version 20261112 (build 0504.1). One green ran on 20261102 (build 0473.1)": true of the
  record, but the implied distinction does not hold. VMs of 20261102, build 0473.1 fail the same way today (replicas
  1, 2, 3, 6). The image version is not what separates green from red.
- "Which other Xcode on the image has an iOS platform matching an installed runtime" assumed one exists by matching;
  in fact five do, and three of them (26.3, 26.4.1, 26.6) offer destinations without `simctl` choosing a named runtime
  for their SDK, so "matching" is looser than an exact runtime match.
- Kotlin's maximum Xcode: the brief points to the compatibility page; Kotlin's own source disagrees with it (above).

### Decided beyond scope
- 6 replicas rather than 4; 26.0.1 run first and last; `xcodebuild -checkFirstLaunchStatus` and per-Xcode
  `simctl runtime match list -v` added to each Xcode's pass (read-only). `-checkFirstLaunchStatus` printed nothing and
  no exit line, so exit 0, for every Xcode pass on every replica (48 of 48 files).
- A platform-UUID hash was printed beside the hostname hash; it turned out to be per image, as recorded.
- The `brouter` run the push started (44257759907, `bccce50`; foreseen in section 14) was left running and not read.
- Section 14's first commit `4e090cb` scanned 2 (release URLs), fixed in `6127120` before push, as recorded there.

### Not checked
- No build under any other Xcode; no `-downloadPlatform` timing; no named-destination attempt.
- Issue #13220 (named as the original of #13435) was not read.
- The `brouter` run's result.
- No device: every iOS statement here is simulator-checked, and most are not even that (destination listings only).

## 16. Session 85 (A1-3b, the fix of RECORD -208, then the gate): predictions, written before the workflow changes

Coder "Plumb Ledger", session 85. Base, confirmed at start: `git fetch origin`; branch `app`; `git rev-parse HEAD` and
`git rev-parse origin/app` both printed `f0cd4662bedcb9d78f0fac5a59968a20419583a8`; `git status -sb` printed only
`## app...origin/app`. Governing rules: Forager's `CLAUDE.md` (Testing: the revert-check rules), read today from
`/home/user/Forager/CLAUDE.md`. Every iOS result is **simulator-checked**.

The planner's decision (RECORD -208), as briefed: "Both macOS jobs of `app.yml`, `app-framework` and `app-bundle`, select
Xcode 26.2 (`/Applications/Xcode_26.2.app`) instead of 26.0.1. `brouter.yml` stays on 26.0.1."

### The workflow change (one commit, `app.yml` only)

- Both "Select Xcode" steps: `sudo xcode-select -s /Applications/Xcode_26.2.app`, then `xcodebuild -version` into a file
  and the log; the step **fails** unless the file's first line is exactly `Xcode 26.2` (the version the workflow names,
  held in one variable per step). The step name and the header comment cite RECORD -208 and the reason in one line.
- Nothing else changes: the diagnostic step before the build stays, the build, the bundle check, the control, the
  launch, the gate and the printed steps are as they are.
- `brouter.yml` ignores pushes that touch only `app.yml` (its `paths-ignore`, lines 11-15), so this push starts `app`
  only.

### Predictions for the confirming run (step 2)

- **Both macOS jobs, the select step:** `Xcode 26.2` then `Build version ...` (the build number is not predicted; read
  from the log), then the step's `OK:` line. Section 15's diagnostic printed `-version` for 26.2 on all six VMs, so a
  missing `Xcode_26.2.app` is not expected.
- **`app-framework`:** the link task runs (`> Task :app:linkDebugFrameworkIosSimulatorArm64` with no outcome suffix), the
  listing shows `ForagerApp.framework` with its binary, `lipo -info` prints `Non-fat file: ... is architecture: arm64`
  (the same shape as under 26.0.1), and the step's `OK:` line. The link uses the 26.2 SDK (inferred; the KGP passes the
  selected Xcode's SDK path, which the `--info` arguments would show if printed).
- **`app-bundle`, the diagnostic:** `simctl list runtimes` lists the same twelve runtimes (iOS 26.2, 26.4.1, 26.5 and the
  others); `-showdestinations` now prints an "Available destinations" section with iOS Simulator entries, including
  `{ platform:iOS Simulator, ... name:Any iOS Simulator Device }`; 34 such entries, as in section 15 for 26.2.
- **`app-bundle`, the build:** `SDKROOT = iphonesimulator26.2`; Gradle's framework link runs inside `xcodebuild`
  (`> Task :app:linkDebugFrameworkIosSimulatorArm64` plain header); `** BUILD SUCCEEDED **`; the bundle check's `OK:` line.
- **The control:** `OK: the control: the colour check failed on before.png (exit 1)` (the home screen; iOS 26.2 runtime as
  before, so the same shape as in A1-3a).
- **The drawn-frame line:** `OK: the app printed 'A1: first frame drawn'`.
- **The gate (never yet executed):** `png-region: common colour E0218A: 2530188 pixels`, `png-region: PASS:
  2530188/2530188 pixels ... within 8 of E0218A`, then the step's `OK:` line. The pixel count assumes the same device
  (iPhone 17) and screenshot size as A1-3a.
- **A Kotlin warning naming Xcode:** predicted **absent** in both jobs. Section 15 found `maxTested = 16.0` in Kotlin's
  source at `v2.3.10`, so 26.0.1 was already "above" it, and no warning appeared under 26.0.1; whatever kept it silent is
  not tied to the Xcode's version number (inferred), so 26.2 should be equally silent. If it does appear, it is the
  WARNING-severity "higher than the maximum known to the Kotlin Gradle Plugin" text, not a failure.
- **The ICU linker warning:** predicted **unchanged**, `ld: warning: object file (...libicu.icudtl_dat.o) was built for
  newer 'iOS-simulator' version (18.5) than being linked (16.0)` inside the `xcodebuild` log. It compares the object's
  build version with the deployment target 16.0, neither of which the Xcode version changes. A newer linker may print
  other warnings as well; none is predicted.

### Predictions for the revert and the restore (step 3)

As section 11 states them, unchanged: `A1_COLOUR` to `Color(0xFF1E6FD9)`; 0 lines under `^e: ` and ` e: `;
`** BUILD SUCCEEDED **`; the control fails on `before.png`; the drawn-frame line appears; the gate fails with
`png-region: common colour 1E6FD9: 2530188 pixels`, `png-region: FAIL: 0/2530188 pixels ... within 8 of E0218A`, then
the step's `FAIL:` line naming exit 1. The restore edits the constant back; `git diff <step-2 commit> HEAD -- app iosApp
.github tools` prints nothing; its run is green as in step 2.

## 17. Session 85, step 2: the confirming run under Xcode 26.2 (recorded as it arrived)

Commit `243f6ec` (the workflow change, `app.yml` only), run **44259385803**, **green, all four jobs**. Before the push, the
two new select steps were extracted from the YAML (identical text in both jobs) and run locally with stub `sudo`,
`xcode-select` and `xcodebuild`: first line `Xcode 26.0.1` printed `FAIL: the selected Xcode prints 'Xcode 26.0.1',
expected 'Xcode 26.2'` and exit 1; `Xcode 26.2.1` likewise exit 1; `Xcode 26.2` printed the `OK:` line and exit 0; an
`xcodebuild` exiting 3 ended the step with exit 3 (pipefail). That is a local stub control of the shell logic only; the
check has not been seen red on a runner.

- **`app-framework`, job 134648760740**, 07:30:21 to 07:35:15. Image `macos-26-arm64`, version 20261102, build 0473.1
  (log 17-18). Select step: `Xcode 26.2`, `Build version 17C52`, `OK: the selected Xcode is Xcode 26.2` (log 176-178).
  Link step 07:30:41 to 07:35:03 (log 191, 1368); `> Task :app:linkDebugFrameworkIosSimulatorArm64` (log 1333, plain
  header); `Non-fat file: app/build/bin/iosSimulatorArm64/debugFramework/ForagerApp.framework/ForagerApp is
  architecture: arm64` (log 1413), `file` prints `current ar archive` (log 1414, as in the 26.0.1 job 134636659789), and
  `OK: the framework was linked in this job and its binary is arm64` (log 1415).
- **`app-bundle`, job 134648760070**, 07:30:21 to 07:43:06. Image `macos-26-arm64`, version 20261112, build 0504.1 (log
  18). Select step: `Xcode 26.2`, `Build version 17C52`, `OK: the selected Xcode is Xcode 26.2` (log 177-179).
  - Diagnostic (log 221-282, 07:30:41 to 07:30:49): the same twelve runtimes, iOS 26.2 (23C54), 26.4 (26.4.1 - 23E254a),
    26.5 (23F77) first; `-showdestinations` under `/Applications/Xcode_26.2.app` prints `Available destinations for the
    "ForagerApp" scheme:` with **34** `platform:iOS Simulator` entries, among them `{ platform:iOS Simulator,
    id:dvtdevice-DVTiOSDeviceSimulatorPlaceholder-iphonesimulator:placeholder, name:Any iOS Simulator Device }`, and
    `Any iOS Device` now listed as available. No "Ineligible" section.
  - Build 07:30:49 to 07:35:38: `SDKROOT = iphonesimulator26.2` (log 304); inside `xcodebuild`, `> Task
    :app:linkDebugFrameworkIosSimulatorArm64` (log 1054) and `> Task :app:embedAndSignAppleFrameworkForXcode SKIPPED`
    (log 1072, as under 26.0.1 in job 134628156762); `** BUILD SUCCEEDED **` (log 1291). 0 lines matching `^e: ` or
    ` e: ` (counted on the whole job log).
  - Bundle check: `OK: ForagerApp.app built unsigned for the simulator, com.zynergylabs.forager.ios.dev, 'Forager dev',
    arm64, framework linked inside xcodebuild` (log 1380). Device type and runtime: `OK: iPhone 17 and iOS 26.2 are both
    listed` (log 1748).
  - Create 07:35:51, booted 07:37:53 (log 1763, 2128); the `simctl list devices` line after boot came at 07:40:11, over
    two minutes later (a slow runner; not gating, recorded).
  - The control: `png-region: FAIL: 98/2530188 pixels (0 to four places) within 24 of E0218A, threshold 0.98` (log 2153,
    written here without the four-place figures, which the E1 scan counts), then `OK: the control: the colour check
    failed on before.png (exit 1)` (log 2154). `before.png` is 1206x2622, band rows 262 to 2359.
  - Launch at 07:41:30, `pid=16657`, `waited 18 s`, app stdout `A1: first frame drawn` (log 2191), `OK: the app printed
    'A1: first frame drawn'` (log 2193).
  - **The gate, executed for the first time:** `png-region: common colour E0218A: 2530188 pixels` (log 2223),
    `png-region: PASS: 2530188/2530188 pixels ... within 8 of E0218A, threshold 0.98` (log 2224; fraction 1 to four
    places), `OK: the colour check passed on after.png (E0218A, tolerance 8, threshold 0.98)` (log 2225).
- `app-klibs` 134648760395 (07:30:21 to 07:31:32) and `png-region` 134648760523 (07:30:21 to 07:30:26) green.
- **Kotlin warning naming Xcode: absent** in both macOS logs. Searched every line containing "xcode" (any case) for
  "tested", "supported", "compatib", "maximum" or "higher": no match in either, outside the step scripts. As predicted.
- **ICU linker warning: unchanged**, `ld: warning: object file (.../app/build/xcode-frameworks/Debug/iphonesimulator26.2/
  ForagerApp.framework/ForagerApp[1523](libicu.icudtl_dat.o)) was built for newer 'iOS-simulator' version (18.5) than
  being linked (16.0)` (log 1250); only the SDK directory in the path differs from job 134628156762. The full set of
  `warning` lines in the 26.2 `app-bundle` log is the same as in the 26.0.1 job 134628156762 (the ICU line, the
  `appintentsmetadataprocessor` "Metadata extraction skipped" line, git's `hint:` line), and the same in the two
  `app-framework` logs (git's `hint:` only). As predicted; no new linker warning.
- Against section 16's predictions: all held. The `Build version` (17C52) was not predicted.

## 18. Session 85, step 3 and results: the revert run, the restore, and the disclosure sections

### The revert run

Commit `ab933c9`: one line, `app/src/commonMain/kotlin/com/zynergylabs/forager/app/App.kt:11`, `Color(0xFFE0218A)` to
`Color(0xFF1E6FD9)` (`git diff --stat`: 1 file, 1 insertion, 1 deletion). A copy of the file was saved before the edit.
Run **44261245328**, **red, `app-bundle` only** (job 134654851453, 07:44:37 to 07:56:26; image version 20261102, build
0473.1, log 18). Read in this order:

1. **The build, before anything else:** the job checked out `ab933c9c5340dddc034213c13c5401e024ae026e` (log 109); 0
   lines matching `^e: ` and 0 matching ` e: ` in the whole job log; `SDKROOT = iphonesimulator26.2` (log 304); `>
   Task :app:linkDebugFrameworkIosSimulatorArm64` inside `xcodebuild` (log 1054); `** BUILD SUCCEEDED **` (log 1291);
   the bundle check's `OK:` line (log 1380). So the reverted build compiled and ran; the result below is this build's.
2. **The control still failed on `before.png`:** `png-region: FAIL: 98/2530188 pixels ... within 24 of E0218A` (log
   2126), `OK: the control: the colour check failed on before.png (exit 1)` (log 2127).
3. **The drawn-frame line still appeared:** app stdout `A1: first frame drawn` (log 2164), `OK: the app printed 'A1:
   first frame drawn'` (log 2166), `waited 12 s` (log 2162).
4. **The failure is the gate's own, naming the colour it saw:** `png-region: common colour 1E6FD9: 2530188 pixels` (log
   2196), then **`png-region: FAIL: 0/2530188 pixels (0 to four places) within 8 of E0218A, threshold 0.98`** (log 2197;
   four-place figures not written here, for the E1 scan), then `FAIL: the colour check on after.png exited 1 (E0218A,
   tolerance 8, threshold 0.98)` (log 2198) and `##[error]Process completed with exit code 1.` (log 2199), the job's only
   `##[error]` line. The printed step after it still ran (`===== after.png`, log 2246; `pid 14222 is alive`, log 2290).
   Every line here is one this edit could produce: the colour named is the reverted constant's, exactly.
- The other three jobs green: `app-klibs` 134654851703 (07:44:38 to 07:45:50), `app-framework` 134654851780 (07:44:39 to
  07:49:18; image version 20261112, build 0504.1; `Xcode 26.2` log 176, `OK:` log 1414), `png-region` 134654851995.

### The restore

Commit `c514813`, the constant edited back (`sed` on the one line, not `git checkout`). `cmp` against the copy saved
before the revert: identical. `git diff 243f6ec HEAD -- app iosApp .github tools` printed nothing (0 bytes), before and
after the push. So the step-2 workflow change is intact and `app/` is as it was. Both commits stay in history.

Run **44262884089**, **green, all four jobs**. `app-bundle` job 134660261407, 07:57:13 to 08:10:21 (image version
20261112, build 0504.1): checked out `c5148130c91b0ba1c46d714f6337e55051819998` (log 109); `Xcode 26.2`, `Build version
17C52`, `OK:` (log 177-179); `^e: ` 0, ` e: ` 0; ICU warning unchanged (log 1250); `** BUILD SUCCEEDED **` (log 1291);
bundle check `OK:` (log 1380); control `OK:` (log 2188); `OK: the app printed 'A1: first frame drawn'` (log 2227);
`png-region: common colour E0218A: 2530188 pixels` (log 2257), `png-region: PASS: 2530188/2530188 pixels ... within 8 of
E0218A, threshold 0.98` (log 2258), `OK: the colour check passed on after.png ...` (log 2259). `app-framework`
134660260591 (07:57:13 to 08:00:50; version 20261102, build 0473.1): `Xcode 26.2` (log 176), `OK:` (log 178), `Non-fat
file: ... architecture: arm64` (log 1411), `OK: the framework was linked ...` (log 1413). `app-klibs` 134660260197
(07:57:13 to 07:58:22), `png-region` 134660260831 (07:57:14 to 07:57:19).

### Runs and job times, this session

| Run | Head | Result | `png-region` | `app-klibs` | `app-framework` | `app-bundle` |
|---|---|---|---|---|---|---|
| 44259385803 | `243f6ec` (Xcode 26.2) | success | 134648760523, 5 s | 134648760395, 1 min 11 s | 134648760740, 4 min 54 s (0473.1) | 134648760070, 12 min 45 s (0504.1) |
| 44261245328 | `ab933c9` (revert) | **failure, at the gate** | 134654851995, 12 s | 134654851703, 1 min 12 s | 134654851780, 4 min 39 s (0504.1) | 134654851453, 11 min 49 s (0473.1) |
| 44262884089 | `c514813` (restore) | success | 134660260831, 5 s | 134660260197, 1 min 9 s | 134660260591, 3 min 37 s (0473.1) | 134660261407, 13 min 8 s (0504.1) |

Image builds in brackets (all `macos-26-arm64`; 0473.1 is version 20261102, 0504.1 is version 20261112). Xcode 26.2
built and linked on both image versions, in both jobs.

No `brouter` run was started by this session's pushes (`app/**`, `app.yml` and `docs/**` are all in its
`paths-ignore`); the run list shows only `app` runs for `243f6ec`, `ab933c9` and `c514813`.

### Confirmed (CI logs cited above; every iOS fact simulator-checked)
- Under Xcode 26.2 (build 17C52) both macOS jobs build: the framework links (`app-framework`, three runs), and the
  unsigned app builds with Gradle linking the framework inside `xcodebuild` (`app-bundle`, three runs).
- `-showdestinations` under 26.2 offers 34 iOS Simulator destinations including the generic one (run 44259385803).
- The select step's version check passed in six jobs; its failure branch was shown only with local stubs.
- The gate ran for the first time and passed (44259385803, 44262884089); it failed on the reverted colour with its own
  line naming `1E6FD9` (44261245328), on a build shown to have compiled and run.
- The restore left `app iosApp .github tools` identical to the step-2 commit.
- No Kotlin warning naming Xcode under 26.2; the ICU linker warning unchanged; no new warning line.

### Inferred
- That 26.2 will keep working on these runners: true for the three runs read, across both image versions; not more.
- The green run's `waited 85 s` (log 2223) is mostly `simctl launch` itself (launch at 08:07:56, `pid=` at 08:09:22, log
  2220-2222), against the step's 120 s wait and its 5-minute timeout. A slower runner could reach the 120 s limit with
  the app healthy (inferred; not seen red).

### Could not determine
- Why Kotlin's Xcode check prints nothing (section 15's `maxTested = 16.0` question stands; 26.2 changed nothing there).
- Why Xcode 26.0.1 built green three times today before failing (section 15).

### Premises in the brief that were wrong
- None found. The brief's 26.2 reason ("the lowest Xcode offering destinations on all six machines sampled in run
  44257759986") matches section 15's table.

### Decided beyond scope
- The version check's failure branch was exercised locally with stub commands before the push (described in section 17),
  since it cannot be seen red on a runner without breaking the job on purpose.
- The two 26.2 select steps write `build/xcodebuild-version.txt` (the file the check reads); `mkdir -p build` added
  before it. Nothing reads the file afterwards and it is not uploaded.
- For the revert I also compared the printed steps' and the other jobs' state, and the app-framework logs of all three
  runs for the same warning search.

### Not checked
- The version check red on a runner.
- The `brouter` run 44257759907 (`bccce50`, started in session 84), still in progress when read at the end; not waited for.
- The uploaded artifacts (`a1-3-simulator`, `a1-2-xcode`) of this session's runs were not downloaded; the PNGs were
  read only through `png-region.py`'s log lines.
- Any device: every iOS statement here is simulator-checked.

## 19. A1's closing list

Every iOS item is **simulator-checked** on GitHub's `macos-26` runners; the owner has no Mac and no iPhone ("3 no
iPhone"). Run ids are `app.yml` runs on branch `app` unless named otherwise.

### What A1 has shown, and the run that shows it

- **The module compiles** for both iOS targets on Linux, no compile errors: A1-1 run 44237677080; most recently
  `app-klibs` in run 44262884089 (job 134660260197).
- **The framework links** for the simulator, arm64: A1-1 run 44237677080 (Xcode 26.0.1); under Xcode 26.2, `app-framework`
  in runs 44259385803 (job 134648760740), 44261245328 (134654851780) and 44262884089 (134660260591).
- **The engine's counts unchanged with `:app` in the build:** `brouter.yml` run 44237677010 on `8c8d323` (A1-1 section
  on CI: `jvm` 262 tests, `jvm-production` 3, `ios-smoke` 61 and 7, `ios-memory` 3, 1, 1, each with no failures and no
  compile errors), with `:brouter`'s dependency reports byte-identical with and without `:app` (A1-1, local). That run
  used Xcode 26.0.1, which `brouter.yml` still selects.
- **The Xcode project is generated and the unsigned app builds:** A1-2 run 44245585616 (job 134603625845, 26.0.1); under
  26.2 runs 44259385803, 44261245328, 44262884089 (`Created project at ...`, `** BUILD SUCCEEDED **`, the bundle check's
  `OK:` line; sections 17 and 18).
- **The bundle check seen red:** A1-2 run 44246226134 (job 134605728656), the identifier branch only, with the build
  shown green in that job; restored green in run 44246914973.
- **The app launches, prints its line, and its screenshot is its colour:** A1-3a run 44249852981 attempt 2 (job
  134628156762; screenshot read by the printed tolerance-24 check then); under 26.2 with the gate, runs 44259385803
  (job 134648760070) and 44262884089 (job 134660261407): `A1: first frame drawn`, then `PASS: 2530188/2530188 pixels
  within 8 of E0218A`.
- **The colour gate seen red and green:** red in run 44261245328 (job 134654851453), `png-region: FAIL: 0/2530188 pixels
  within 8 of E0218A` with `common colour 1E6FD9`, on a build with 0 compile-error lines and `** BUILD SUCCEEDED **`;
  green in runs 44259385803 and 44262884089. The control (the check failing on the home screen's `before.png`) held in
  all three.

### What A1 has NOT shown

- A real iPhone. Nothing has run on a device.
- Any other device or runtime: only an iPhone 17 simulator on iOS 26.2.
- Anything after the first frame: no interaction, no lifecycle, no second frame checked.
- A release or signed build: Debug, unsigned (`CODE_SIGNING_ALLOWED=NO`), simulator SDK only.
- That the generated project opens in Xcode: it is only built by `xcodebuild` on a runner.
- The engine inside the app: `:app` does not depend on `:brouter`.
- The Debug stub executable: the app's code is in `ForagerApp.debug.dylib`; the `ForagerApp` executable checked by
  `lipo` is the stub that loads it (A1-2 section 3).
- The ICU linker warning against deployment target 16.0: an object in the Compose framework was built for iOS 18.5; its
  effect on an iOS 16 or 17 device is untested (no such runtime on the runners).
- Only some branches of the bundle check seen red: the identifier branch only; display name, arm64, missing bundle and
  missing Gradle header have not been seen red. Likewise the new Xcode-version check has been seen red only with local
  stubs, not on a runner.
- Why Xcode 26.0.1 built green three times on 2026-11-18 (05:38, 05:59, 06:40 UTC) before failing on every later
  attempt and every diagnostic pass (sections 12 and 15).
- That Xcode 26.2 is supported by Kotlin 2.3.10: it is past what Kotlin's compatibility page lists for 2.3.10 (Xcode
  26.0). No warning was printed and every check passed, which is evidence of this build working, not of support.
- That the runner will keep 26.2: runner-images keeps "only three major.minor versions" of simulator runtimes, so the
  iOS 26.2 runtime, and with it 26.2's destinations, will in time be dropped. The diagnostic step before the build and
  the version check are what will show it.
