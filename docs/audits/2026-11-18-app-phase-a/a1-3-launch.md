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

*(filled in step 4)*

## 4. Runs

*(filled in step 4)*

## 5. Proposed for A1-3b: tolerance and threshold

*(filled in step 4)*

## 6. Confirmed vs inferred

*(filled in step 4)*

## 7. Could not determine

*(filled in step 4)*

## 8. Premises in this brief or the pre-build that were wrong

*(filled in step 4)*

## 9. Decided beyond scope

*(filled in step 4)*

## 10. Not checked

*(filled in step 4)*
