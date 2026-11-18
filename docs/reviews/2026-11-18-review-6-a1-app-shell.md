# Review 6: step A1, the empty app shell (sessions 80 to 85)

Reviewer: Samphire Witness. Date: 2026-11-18. Code under review: `origin/app` at `df49ee5` (checked: `git fetch origin`,
then `git rev-parse origin/app` printed `df49ee5c9c99c4c412c038ecb912d2d4e07d54ef`; the worktree was moved there,
detached). Range `382204f..df49ee5`. Read-only on the code: nothing in the worktree was edited. `origin/app` moved to
`3273c0c` while I worked (two commits, one new file under `docs/audits/2026-11-19-app-phase-a2/`); those are not
reviewed here.

Marks: [R] read in the tree, in a CI log or in a CI artifact; [C] run by me; [I] inferred; [U] unverified. Every iOS
result is "simulator-checked", and none is from a run of mine: I cannot run Xcode or a simulator. No coordinate is
written here. Runner image versions are written apart ("version V, build B"), and fractions are rounded, for the scan.

## 1. Verdict

**The six claims hold on the evidence, with nothing blocking, and one of them is worded past its evidence.**

- Claims 1, 2, 3, 4 and 6 hold as stated. I read the four engine logs of run 44237677010 and every count equals the
  baseline. I read the green run 44262884089 and the revert run 44261245328 line by line, downloaded both
  `a1-3-simulator` artifacts, and my own runs of `tools/png-region.py` on the four PNGs print the same lines CI printed;
  a second, independent decoder (Pillow) gives the same counts. The revert run failed at the gate only, on a build that
  compiled, with a message naming the reverted colour.
- Claim 5 holds for the decision (Xcode 26.2 has destinations on six of six machines sampled) and not for its reason as
  worded. "Xcode 26.0.1 has no simulator platform on the runners" is true of every sample from 06:31 UTC on. It was
  not true of four app builds between 05:38 and 06:45 the same morning, and simulators still run under 26.0.1 today:
  the engine's `ios-smoke` job ran 61 simulator tests under it in the same minutes as the diagnostic.
- The gate on `after.png` is sound: I could find no way for it to pass without the app's colour on the screen of the
  device this run created, after the app this run built was launched on it.

Six things should be fixed, most of them before A2 edits the same job and the same script:

1. `png-region.py` exits 1, the status the control accepts as its pass, on three kinds of broken PNG.
2. The self-test passes unchanged with the per-channel test, the threshold test or the Paeth rule broken.
3. The Xcode reason is stated without its time and scope, and a count in the closing list is off by one.
4. `brouter.yml` ignores `app/build.gradle.kts`, a build script of the engine's own Gradle build.
5. The edited `brouter.yml` has not completed a run, and the findings do not say so.
6. The closing list's "not shown" is missing eight items.

## 2. Findings

### Blocking

None.

### Should fix

**S1. `png-region.py` exits 1 on three kinds of broken PNG, and the control reads exit 1 as "the check failed on the
colour".**
- Where: `tools/png-region.py:40` (the CRC is unpacked with no length check), `:104` (the header is unpacked with no
  length check), `:175-179` (zero pixels gives `FAIL`, exit 1); the caller `main` catches only `Unsupported` and
  `OSError` (`:164`), so any other exception leaves Python with a traceback and exit 1. The control:
  `.github/workflows/app.yml:243-244`. The promise: `tools/png-region.py:11-13` and `app.yml:236-237` ("an unreadable
  PNG (exit 2) cannot pass as a failed match").
- How I know [C]: I made PNGs in the scratchpad and ran the script at `df49ee5` on each. A file cut inside a chunk's
  CRC: `struct.error: unpack requires a buffer of 4 bytes`, exit 1. A header chunk of 12 bytes: `struct.error`, exit 1.
  Width 0, and height 0: `FAIL: 0/0 pixels`, exit 1. The other broken files I tried exit 2 as promised: cut in half, no
  end chunk, signature only, empty, short image data, a cut compressed stream.
- How it would mislead: the control prints `OK: the control: the colour check failed on before.png (exit 1)` when
  nothing was measured. A cut screenshot is unlikely. The likely case is A2: it will add options to this script, and
  any bug there that raises exits 1 and is read as a control that held.
- Fix: catch every exception in `main` and return 2; return 2 when the band holds no pixels; add both to the self-test.

**S2. The self-test passes unchanged with three parts of the check broken, and covers fewer refusals than it says.**
- How I know [C]: three copies of the script and the test in the scratchpad, one edit each, the test run on each. The
  tree was not touched.
  - `:141`, the three `and` made `or` (a pixel then matches if any one channel is close): `37 cases, 0 failed`. No case
    has one channel inside the tolerance and another outside (`png-region-test.py:120-132`: white and "two units off"
    are out, or in, on all three channels at once).
  - `:176`, `frac >= a.threshold` made `frac > 0.5`: `37 cases, 0 failed`. The cases are whole, half and none
    (`:116-126`, `:139-142`); nothing lies between half and whole, so 0.98 is never exercised.
  - `:53`, Paeth's tie rule changed (`<=` to `<`, twice): `37 cases, 0 failed`. The test's encoder calls the tool's own
    `paeth` (`png-region-test.py:47`), so for filter 4 "pixels known in advance, byte for byte" (`:5-7`) is a round trip
    through shared code. Session 82's plant (`a1-3-launch.md:218-222`) was in the Average filter, which the test does
    hold.
  - Refusals: six kinds are tested (`png-region-test.py:160-171`). Not tested: a cut file, no end chunk, no image data
    chunk, a stream that does not inflate, a wrong data length, an unknown filter byte, and S1's three. `app.yml:30`
    says "the refusals".
  - `--band` is not validated (`png-region.py:132-133`, `:160`). `--band 1.2` on the green run's `after.png` printed
    `band rows -262 to -1 (262 rows ...)` and passed: it read the bottom tenth only. CI uses the default, so this bites
    nobody today.
- Today's reader is right all the same: `paeth` at `:50-57` is the PNG specification's rule as I read it [R], and my
  Pillow decode of the four real screenshots gives the same band counts as the script, pixel for pixel in total and in
  the three commonest colours [C].
- How it would mislead: A2 extends this script for a map screenshot. A green self-test would be read as holding the
  tolerance and threshold logic, which it does not hold.
- Fix: one case with a single channel out by more than the tolerance; one case at 0.97 and one at 0.99 of the band;
  Paeth checked against fixed bytes written in the test, not against the tool's function; the missing refusals.

**S3. "Xcode 26.0.1 has no simulator platform on the runners" is stated without its time or scope; and "three times"
should be four.**
- Where: `app.yml:6-7`; claim 5 as given to me; `a1-3-launch.md:976` and `:911` ("built green three times ... 05:38,
  05:59, 06:40").
- What the runs show [R]:
  - Run 44257759986: on all six replicas, `xcodebuild -showdestinations` under 26.0.1 printed only the ineligible "Any
    iOS Device, error:iOS 26.0 is not installed", first pass and last, and 34 simulator destinations under 26.2. I read
    all six `SUMMARY` blocks, and the raw destination output of replicas 1 and 4 (jobs 134643447484 and 134643447096,
    log 1240-1247 and 1454-1463; 1249-1256 and 1463-1472). Four replicas were image version 20261102, build 0473.1, and
    two were version 20261112, build 0504.1 (log 18 of each). The table at `a1-3-launch.md:563-568` matches.
  - Four app builds under 26.0.1 resolved the generic simulator destination and built that morning: the three the
    findings name, and `a393570`'s, 05:43:49 to 05:48:47, `** BUILD SUCCEEDED **` (run 44246226134, job 134605728656,
    log 171, 213, 1207). The findings' own table lists all four (`a1-3-launch.md:380-384`); the closing list counts
    three.
  - Simulators run under 26.0.1 now: `brouter.yml` run 44257759907, `ios-smoke` job 134643447456, `Xcode 26.0.1` (log
    226), `TOTAL: tests 61` (547), `OK on the iOS simulator: all 25 classes ran` (2213), 07:18 to 07:33, the same
    minutes as the diagnostic.
- So the supported sentence is: "from 06:31 UTC on 2026-11-18, `xcodebuild` under 26.0.1 offered no iOS Simulator
  destination in every sample; it did in four builds earlier that day, and why is not known; `simctl` still runs
  simulators under 26.0.1." The findings say the middle part honestly (`:605-610`, `:700`). The workflow comment and the
  claim drop it.
- How it would mislead: a reader of `app.yml` concludes the cause is known and that the engine's CI on 26.0.1 is
  broken. Neither is so. And whatever made 26.0.1 work until 06:31 and then stop is unknown, so nothing shows 26.2 is
  safe from it.
- Fix: reword `app.yml:6-7`; correct "three" to four at `:911` and `:976` by a dated correction, not by editing.

**S4. `brouter.yml` now ignores `app/build.gradle.kts`, which is a build script of the engine's own Gradle build.**
- Where: `.github/workflows/brouter.yml:11-15`; `settings.gradle.kts:21-22` puts `:brouter` and `:app` in one build.
- Not ignored, so the engine still runs on them [R]: the root `build.gradle.kts`, `settings.gradle.kts`, `gradle/**`
  (the catalog and the wrapper), `gradle.properties`, `gradlew`, `brouter/**`, `tools/**`, `brouter.yml`. The comment
  at `brouter.yml:8-10` is accurate on that.
- What can reach the engine and still skip its CI:
  1. An `app/build.gradle.kts` that fails to configure. Gradle configures every project of the build before running
     `:brouter:` tasks [I: Gradle's default; no engine log line shows it either way, `:app` appears 0 times in the four
     logs of run 44237677010]. The push that broke it runs `app.yml` only. The engine goes red at the next push that
     touches anything else, and that commit takes the blame.
  2. Configuration reaching across from the app's script (`rootProject`, `allprojects`, `project(":brouter")`, a build
     service). Unlikely, and nothing forbids it.
  3. Any other build file placed under `app/` later.
- The comment's evidence, "A1-1 showed the engine's counts unchanged with :app in the build", is one run with one state
  of `app/build.gradle.kts` (26 lines, three dependencies). A2 changes that file.
- Fix: ignore `app/src/**`, not `app/**`. The cost is one engine run per change to the app's build script, which is
  when one is wanted.
- Beside it, cost only: `.github/workflows/app-diagnose.yml` is not ignored, so every edit of the diagnostic starts a
  full engine run (44257759907 was started that way, as `a1-3-launch.md:511-512` foresaw).

**S5. The edited `brouter.yml` has not completed a run, and five engine runs on this branch were cancelled without a
record of who cancelled them.**
- What the run list shows [R] (`gh api .../actions/runs?branch=app`, and each run's jobs): since the `paths-ignore`
  edit at `5cbbf1b`, run 44249298838 has `ios-memory` cancelled; 44249664086 has `jvm` and `ios-memory` cancelled;
  44249712556 has `ios-memory` cancelled; 44257759907 was still in progress at my last read (three jobs green,
  `ios-memory` running). Before the edit, 44246226126 and 44246914935 are cancelled in all four jobs. The last engine
  runs on `app` that finished green are 44237677010 (`8c8d323`) and 44245586042 (`4b01c3b`); the findings never cite the
  second.
- What still holds: the skip itself was observed. No `brouter` run exists for `f56cbe5`, `243f6ec`, `ab933c9` or
  `c514813` [R]. And claim 1 carries to `df49ee5` by diff: `git diff --stat 8c8d323..df49ee5 -- . ':!docs'` lists only
  workflow files, `iosApp/` and the two `tools/png-region*` files; no engine input [C].
- `a1-3-launch.md:260` says of session 82 "No `brouter` run was cancelled". The list now shows the five above. Who
  cancelled them, and when, is not in any findings file [U].
- How it would mislead: "the engine is unchanged" reads as current. `ios-memory` has not finished on this branch since
  `4b01c3b`.
- Fix: read run 44257759907 when it ends and record it; record who cancelled the five.

**S6. Section 19's "not shown" list is missing eight items, and one "shown" sentence was not established by the session
that wrote it.**
- Add to "not shown" (`a1-3-launch.md:961-982`):
  1. The drawn-frame gate has never been seen red. No run lacks the line; the 120 s loop's failing branch
     (`app.yml:270-272`) is unexercised. Claim 4 lists three checks that "can fail"; this gate is not among them and is
     not listed as unshown either.
  2. The control's failing branch (`app.yml:245-246`) and the device-and-runtime check (`:222-225`) have never been
     seen red. The framework listing check was seen red only locally with a stubbed `lipo` (`a1-1-app-module.md:120-126`).
  3. The gate reads the middle 80 % of the height. That the colour reaches the edges, which `ForagerApp.swift:12`
     exists to do, is not gated. (In the green run's `after.png` the bottom tenth is the colour in every pixel and the
     top tenth is the colour plus the island and the status bar [C], so it is true today, by a reading and not a gate.)
  4. Tolerance 8 and threshold 0.98 have never been exercised. Every `after.png` seen passes at tolerance 0 with the
     whole band, and the revert has none of it. Nothing in between has been seen, in CI or in the self-test (S2).
  5. That the line means a frame of the screen's size reached the display (N2).
  6. `app.yml` runs only on pushes to `app` (`app.yml:10-12`): no pull-request trigger, nothing on `main`. `df49ee5`
     itself has no run; the last is 44262884089 on `c514813`, and the three commits after it touch one findings file
     only [C: `git diff --stat c514813..df49ee5`].
  7. The engine under the app's Xcode. The engine's simulator tests run under 26.0.1; the app links under 26.2. When
     the app depends on the engine, the engine code in the app will have been linked by a toolchain its tests did not
     use.
  8. S5: the edited `brouter.yml` has no finished run.
- The sentence: "The control (the check failing on the home screen's `before.png`) held in all three" (`:958-959`).
  Session 85 did not look at its runs' pictures (`:928-929`). In run 44262884089 the log itself differs from the other
  two: the commonest band colour is `000000`, about 65 % of the band (job 134660261407, log 2184), where the other
  runs print `FFFFFF` at about 10 %. The findings do not remark on it. I viewed it (N1): it is the home screen, so the
  sentence is true. It was not known to be true when written.

### Note

**N1. The mostly black `before.png` of run 44262884089 is the home screen before its wallpaper and widgets drew; the
control is still a control.**
- What it is [C: viewed from the artifact, id 14410105256]: the status bar, the two large widgets as blank white
  squares, the eight app icons and the dock, on a black background where earlier runs show the wallpaper. The revert
  run's `before.png` (artifact 14409241105) shows the same screen fully drawn. Both print `98/2530188` within 24; the
  98 pixels are in icons both pictures share.
- Timing [R]: `booted 08:05:58` (log 2162); the screenshot was written between 08:06:54 and 08:07:30 (2164, 2181). So
  about a minute after `bootstatus` returned, the home screen was still not drawn. The 10 s sleep (`app.yml:240`) was
  added "so the control reads the drawn home screen rather than a boot frame" (`a1-3-launch.md:255`); in this run it
  did not achieve that.
- Is the control still meaningful? Yes, for the two things it can show: the reader works on this device's screenshots
  (exit 1, not 2), and the device does not show the app's colour before the app is installed, so the colour in
  `after.png` arrived with the launch. Neither needs the wallpaper. What it never showed, in any run: that a fully
  drawn other screen fails. A black display that renders nothing would "hold" the control the same way. The gate on
  `after.png` is what shows the display renders.
- The same run's `simctl launch` took 85 s to return (log 2220-2223), against 7 to 18 s in other runs. That runner
  was slow throughout. See N3.

**N2. The drawn-frame line shows the draw lambda ran in this process. It does not show the size drawn or that the
frame reached the screen.**
- `App.kt:22-26` [R]: `drawRect(A1_COLOUR)` and then `FirstFrame.logOnce()` inside `drawBehind`. `drawBehind` runs in
  the draw phase, so composition alone cannot print the line [I: from Compose's documented phases; no run separates
  them]. Nothing else prints it (`grep` over `app/` and `iosApp/`: `App.kt:14` and `:37` only [C]).
- What the line cannot say: the size of the area drawn (a zero-size pass would print it too [U]); that the commands
  were flushed to the display (the lambda issues them; presentation comes later [I]); anything after the first call
  (`logged`, `:32-37`, prints once per process).
- The comment at `App.kt:17-18`, "printed only by a frame that drew the colour", is right if "drew" means "issued the
  draw call". The findings are careful here (`a1-3-launch.md:225-226` lists it as inferred).
- Can the line be matched from elsewhere? No [R]. The loop greps the file `simctl launch --stdout` writes
  (`app.yml:252`, `:258`, `:264`), under `build/a1-3/` in a fresh checkout on a fresh machine; no other step writes it.
  The artifact's `app-stdout.txt` is 22 bytes, the line and a newline [C: `od -c`]. If the launch fails the step fails
  (`set -o pipefail`, `:254`, and the default `-e`).
- Is `after.png` from the launched device, after the line, in this run? Yes [R]. `$UDID` is set once by the create step
  (`:230-232`) and used by install, launch and both screenshots; the `after.png` step follows the launch step in the
  same job; the gate step has no `if:`, so it is skipped when the launch step fails.
- For A2: print the drawn size in the line and require it to be non-zero. And the line will stop meaning "the screen
  is ready" once a map loads after the first frame.

**N3. The gating steps of `app.yml`, gone through one by one: I found no way for one to pass without seeing what could
fail it, apart from S1.**
- No `|| true` on a gating command: the ones present are in printed steps (`:60-62`, `:118`, `:292-314`) or guard a
  value that is then compared (`:188-189`). `continue-on-error` is on the diagnostic step only (`:156`).
- Pipes: every step that pipes a gating command into `tee` sets `pipefail` (`:54`, `:77`, `:89`, `:135`, `:170`,
  `:254`). The steps without it (`:98` sets it; `:180-206`, `:219-225`, `:235`) gate on a `grep` of the saved file or
  on the last command of the pipe, so a failed first command fails the check. The default shell is `bash -e` without
  `pipefail` (every step's `shell:` line in job 134660261407 [R]); the comment at `app-diagnose.yml:41` says otherwise
  and is wrong.
- Greps read saved files (`build/xcodebuild.log`, `build/app-framework.log`, the app's stdout), never the job log, so
  a step's echoed command cannot match.
- The wait loop cannot time out green: `found` stays 0 and the step exits 1 (`:262-272`).
- `if:` conditions: `after.png` and the printed step run after a failed launch (`:274`, `:286`); the gate does not.
- False reds are possible: the launch step's 5-minute limit (`:248`) against an 85 s launch, and a screenshot taken
  before the first frame is presented. The findings name the first (`a1-3-launch.md:905-907`).

**N4. The revert check is sound (question 4).** Run 44261245328 [R]: checked out `ab933c9` (log 109); `> Task
:app:compileKotlinIosSimulatorArm64` and the link task with plain headers (1053-1054); `** BUILD SUCCEEDED **` (1291);
the bundle check, the control and the drawn-frame line all `OK` (1380, 2127, 2166); `common colour 1E6FD9: 2530188
pixels`, `FAIL: 0/2530188` (2196-2197); one `##[error]` line in the job (2199). `ab933c9` changes one line,
`App.kt:11` [C: `git show`]. My run of the script on that run's `after.png` gives the same lines, and Pillow gives
`1E6FD9` in every band pixel [C]. The forward change is present: `App.kt:11` at `df49ee5` is `Color(0xFFE0218A)`, and
`git diff 8ed0bba c514813 -- . ':!docs'` is empty [C]. The bundle check's red (run 44246226134, job 134605728656) is
likewise one `FAIL` line naming both identifiers (log 1292), one `##[error]` (1297), on a build that succeeded (1207).

**N5. The engine's counts (question 5), read by me in run 44237677010 on `8c8d323` [R].** `jvm` job 134577660161:
`TOTAL: tests 262, failures 0, errors 0, skipped 0 in 116 files` (log 1686). `jvm-production` 134577660535: `TOTAL:
tests 3` (417), `OK: all 2 classes ran` (535). `ios-smoke` 134577660708: `TOTAL: tests 61 ... in 30 files` (546), `OK
on the iOS simulator: all 25 classes ran` (2212), `TOTAL: tests 7` (2348), `OK: all 2 classes ran` (2351). `ios-memory`
134577660484: `TOTAL: tests 3` (457), `1` (530), `1` (626). All equal the baseline given. What the counts are: the same
tests ran and passed. They are not a comparison of what was resolved or linked; the findings' byte-identical dependency
reports (`a1-1-app-module.md:103-117`) were made locally through a mirror and I did not reproduce them [U].

**N6. Pins and supply chain (question 6).**
- Actions: every `uses:` in `app.yml` and `app-diagnose.yml` is a 40-character SHA, and each SHA is its tag's commit
  [C: `git ls-remote` on the three action repositories for `v7.0.1`, `v6.1.0`, `v7.0.2`].
- XcodeGen: fetched by `tools/fetch-pinned.sh` with a size and a SHA-256 (`app.yml:146-148`); the script refuses and
  deletes a mismatch (`fetch-pinned.sh:56-60`) [R]. Green run: "matches its pins" (job 134660261407, log 192) [R]. I
  did not download it myself. In `app-diagnose.yml:31-36` the fetch ends in `|| echo`, but a refused file is never
  left on disk, so nothing unverified is unzipped or run [R].
- Google's Maven: filtered to `androidx.*` (`settings.gradle.kts:13-15`) [R]. In the green run's `app-framework` log
  (`--info`), all 114 fetches from Google's host are under `androidx/` [R: job 134660260591]. The filter limits what
  Google may serve; it does not stop Maven Central serving an `androidx` group (that would be `exclusiveContent`).
- Catalog: exact versions only (`gradle/libs.versions.toml:4-15`); Gradle 9.0.0 with its distribution SHA-256
  (`gradle/wrapper/gradle-wrapper.properties`) [R].
- Fetched with a version but no content pin kept in this repository [R: host list from the green run's logs]: every
  Maven artifact (no dependency verification file, no lockfile), now including Compose's transitive graph and Google's
  host; the Kotlin/Native bundle, LLVM and libffi from JetBrains' download host; the JDK through `setup-java`; the
  runner's `python3` (3.12.3 printed) and Xcode. `gradle-wrapper.jar` is not validated by a step. All but Google's host
  were already so for the engine.

**N7. The Xcode version check can fail on a runner (question 7).** It compares the first line of `xcodebuild -version`
with the exact string `Xcode 26.2` (`app.yml:78-83`, `:136-141`) [R]. If the image points `Xcode_26.2.app` at a later
patch, as it already does for `Xcode_26.0.app`, `26.1.app` and `26.4.app` (`a1-3-launch.md:585`), the line reads
`Xcode 26.2.1` and the check fails with its own message [I]. If the path is gone, `xcode-select -s` fails first and the
step is red without the check's message [I]. It does not check the build number. The findings say it was seen red only
with stubs (`a1-3-launch.md:790-794`, `:974-975`); that is accurate.

**N8. Small things.**
- `app.yml:85-86` still says `--info` makes the plugin print the link's arguments. It does not, as
  `a1-1-app-module.md:216-219` records; the step at `app.yml:115-118` therefore records nothing useful.
- `app-diagnose.yml` keeps its push trigger (`:10-13`). It has done its job; left as it is, any edit costs six macOS
  machines and a full engine run (S4).
- Alpha is ignored by design (`png-region.py:8`); the self-test's RGBA cases all use opaque alpha
  (`png-region-test.py:60`), so that design is untested. A fully transparent 4x4 image of the colour passes [C]. All
  four real screenshots are fully opaque [C].
- `tools/png-region-test.py` leaves a `tools/__pycache__/` behind when run in the tree (it imports the tool). It is
  ignored by `.gitignore:6`. My own run left one in the review worktree; I removed it.

**N9. For A2, on top of S1 to S6 (question 9).**
1. Decide what the map screenshot asserts before writing its check. A flat-colour fraction will not do, and
   `png-region.py` has no region or histogram mode. A map that loads tiles needs its own "ready" line; the first-frame
   line will be printed before any tile draws.
2. Tiles on a runner: a network fetch in the gate makes the gate depend on a server. A fixture in the repository or a
   pinned download is the alternative.
3. The A1 gate must be retired on purpose. When a map covers the screen, `E0218A` at 0.98 fails. Forager's `CLAUDE.md`
   says silencing a test is never in scope unless asked, so the A2 dispatch should say in words what replaces the
   colour gate and the control.
4. Time margins. This job already takes 12 to 13 minutes; one run needed 85 s for `simctl launch`, two runs needed two
   minutes for `simctl list devices` (`a1-3-launch.md:131-133`, `:816-817`), and the home screen was undrawn a minute
   after boot (N1). A map adds work to the slowest step.
5. MapLibre's binary. Pin it by content (a hash), as XcodeGen is, whichever way it arrives.
6. The deployment target. 16.0 is provisional (`iosApp/project.yml:9`), and one Compose object is already built for
   18.5 (`a1-2-xcode-project.md:152-156`). Only iOS 26.2 runs in CI.
7. The Debug stub. `lipo` checks a 39,640-byte stub (`a1-2-xcode-project.md:147-151`); the app's code is in the
   `.debug.dylib`. The findings list it; the check still says "arm64" about the stub.
8. Two Xcodes in one repository. The engine tests under 26.0.1 and the app links under 26.2 (S6 item 7), and the
   runner-images policy keeps three runtime versions, so 26.2's runtime will be dropped in time
   (`a1-3-launch.md:980-983`).

## 3. What I ran, read and inferred

- Ran [C]: `git fetch`, `git rev-parse`, `git diff --stat`, `git show` in my clone and worktree. `python3 -I
  tools/png-region-test.py` (Python 3.13.18): `37 cases, 0 failed`. Three mutated copies in the scratchpad (S2).
  Twelve hand-made PNGs (S1, N8). `tools/png-region.py` on `before.png` and `after.png` of runs 44262884089 and
  44261245328 at tolerance 24 and 8: exit 1, 1, 0, 0 and 1, 1, 1, 1; the lines for the green run equal CI's (job
  134660261407, log 2181-2187 and 2254-2258). Pillow 12.3.0 on the same four files. `git ls-remote` for the three
  action tags. `gh api` for run lists, job lists, job logs and two artifacts.
- Read [R]: the 17 changed files of the range; the three findings files whole; the logs of runs 44237677010 (four
  jobs), 44246226134 (`app-bundle`), 44261245328 and 44262884089 (`app-bundle`, and the other jobs' key lines),
  44259385803 (`app-bundle` key lines), 44257759986 (six replicas' summaries, two replicas' raw destination output),
  and `ios-smoke` of 44257759907. Two pictures viewed from the artifacts.
- Inferred [I] or unverified [U]: marked where they stand.

## 4. What I did not look at

- No Xcode, no simulator, no Gradle build: I ran no build of `:app` or `:brouter`.
- The local checks of A1-1 (the byte-identical dependency reports, the stubbed listing-step runs) and the local stub
  runs of the version check: not reproduced.
- The generated Xcode project (`a1-2-xcode` artifact) and `iosApp/project.yml` beyond a read: I did not check the
  generated `project.pbxproj` against the findings.
- The web sources the findings cite (runner-images, Kotlin's pages and source, XcodeGen's spec, Apple's page): none
  re-fetched. Section 15's account of them is taken as written [U].
- The XcodeGen zip itself: not downloaded or hashed by me.
- The artifacts' `sim-log.txt`, `listapps.txt` and the simctl help text: not read.
- Runs 44249852981, 44255674662 (both attempts) and 44245585616: job lists only, no logs.
- `RECORD.md` and the pre-build report on another branch: not read.
- `a916953` and `3273c0c` on `app`, which arrived after `df49ee5`.
- The Restricted Object: not read, as Forager's `CLAUDE.md` requires.
