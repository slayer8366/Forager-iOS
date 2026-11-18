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
  `macos-26-arm64`, version `20261112.0504.1` (log line 18), Xcode 26.0.1, build 17A400. The new diagnostic step
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
