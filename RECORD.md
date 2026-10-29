# RECORD.md

Forager-iOS's record of dispatched work and rulings, kept by hand. It is separate from Forager's record
(slayer8366/Forager, `RECORD.md` on `records-after-173`) by the owner's ruling at -1.

It follows Forager's form:
- each piece of work opens with an intent, written before anything is built, saying:
  - what will change;
  - where the change stops;
  - what it is expected to do;
  - when it counts as finished or abandoned;
- it closes with exactly one terminal entry saying what happened;
- rulings are `decision` entries, quoting the owner verbatim.

This file is append-only. Nothing already committed is edited or removed; a correction is a new entry that
points at the one it corrects. IDs run `2026-10-28-1`, `2026-10-28-2`, ..., cited as -1, -2, ...; each
entry names the next free ID. Timestamps come from `date -u`.

Forager's `check_record.py` and `check_prompts.py` are named in its record's header, but neither was found
on Forager's `pre-main` or `records-after-173` at 2026-10-28T19:44Z. Entries here are checked by reading
only.

## Entries

---

**Kind:** decision
**ID:** 2026-10-28-1
**Timestamp:** 2026-10-28T19:51:19Z
**Title:** Forager-iOS keeps its own record, starting with the BRouter port (owner)
**Decision:** asked who should dispatch the BRouter port while Forager's planner was writing Forager's
record (it had committed at 19:37Z), the owner, verbatim: "Option 2. But keep the record separate. Start
the record for our iOS app with this port, the Android version stays separate. Save work on Forager-iOS
repo".
- Option 2 read: "You tell Rowan to stop writing to the record until I'm done. I record today's rulings and
  the dispatch, then start Hazel in this cloud session, where builds can run locally. That's faster for a
  ~13,000-line port. Rowan pulls before writing again."
- With the record kept separate, nothing is written to Forager's record, so Rowan has nothing to pause.
- **The planner's reading, not ruled:** "the Android version stays separate" is read as the Android app's
  code and record staying in slayer8366/Forager, untouched by iOS work. Whether it also changes ruling a
  in -2 (Android switching to the Kotlin engine once proven), or how much code iOS shares with Android
  (Option B), is put to the owner rather than assumed.
- **Calls by the planner (technical; the owner may overrule):**
  - records and dispatches on `main`; code on branches, merged on the owner's word;
  - a `CLAUDE.md` here that points at Forager's rather than copying it.
**Notes:** Written by the planner (a cloud session; not Rowan Waymark) by hand. Next free ID -2.

---

**Kind:** decision
**ID:** 2026-10-28-2
**Timestamp:** 2026-10-28T19:52:15Z
**Title:** The rulings this record starts from, carried from Forager's iOS feasibility report (owner,
2026-10-26 to 2026-10-28)
**Decision:** each was made in conversation and is recorded, with the options it chose between, in
Forager's `docs/audits/2026-10-26-ios-multiplatform-feasibility-report.md` (Forager `pre-main`, last
changed at `c8b88248b`). The owner, verbatim:
- **Approach:** "Option B, file the report on pre-main", then, after asking what Option C offers, "Option B
  stands". Option B is Kotlin Multiplatform with Compose Multiplatform.
- **Routing on iOS:** "Port BRouter to Kotlin".
- **The port:** "a recommended, b Forager-only, c download in CI":
  - a: port into shared Kotlin, prove it identical to the Java engine on the JVM, then switch Android to it;
  - b: port only the path Forager reaches;
  - c: fetch the larger test sample in CI.
- **Alerts on iOS:** "be back by a certain time, and display when the sundown time is", then "1 acceptable,
  2 full sundown line". Back by alerts at the set time, and a late alert (after the walker is home) is
  accepted. The full sundown line is shown. The three sundown alerts are not promised.
- **Building without a Mac:** "I don't have a Mac and I don't care to get one". iOS builds run on GitHub's
  macOS runners.
- **Device checks:** "3 no iPhone". iOS checks are simulator-only, and device behaviour stays unverified on
  hardware.
**Notes:** Forager's own record carries none of these as of `records-after-173` `591b2683`; its two
mentions of "a later iOS app" (lines 21624 and 21627) are in an earlier maps plan. Next free ID -3.

---

**Kind:** intent
**ID:** 2026-10-28-3
**Timestamp:** 2026-10-28T19:52:15Z
**Title:** BRouter in Kotlin for Forager-iOS, Phase 1: verify and plan (dispatch 2026-10-28-01, Hazel)
**Change:** Phase 1 changes nothing in any app. One report, `docs/audits/2026-10-28-brouter-port-verify.md`
on branch `brouter-port`, answering:
- A: method-level reach from Forager's calls;
- B: a replacement for each JDK API;
- C: the project layout;
- D: the oracle;
- E: the differential tests;
- F: where pinned test tiles live;
- G: CI;
- H: floating point;
- I: the Phase 2 order;
- J: the mechanism prediction.

Phase 2, on the go, builds the Forager-only Kotlin Multiplatform port, proven identical to the Java engine
on the JVM. The owner, verbatim: "Go ahead and have a coder start the port on a separate branch".
**Scope boundary:** writes only in slayer8366/Forager-iOS, on `brouter-port` cut from `main` at the commit
adding this entry; Phase 1 writes only the report and its evidence folder. It reads slayer8366/Forager at
`pre-main` `e2969abe4` and BRouter upstream at `v1.7.10`. It does not touch slayer8366/Forager in any way,
Forager-iOS `main`, `RECORD.md`, `prompts/`, accounts, phones, or tile downloads.
**Baseline:** Forager `pre-main` `e2969abe4f98674c24ce3913ce97be568184a4de`; Forager-iOS `main` at this
entry's commit; BRouter v1.7.10, upstream `4d2639af77ea5ed9c30d3e400764eb6f9e8522da`.
**Prediction (outcome, planner):**
- the Forager-only port is between 11,000 and 14,000 lines;
- random-access file reads and the sorted maps are the only JDK pieces needing a library or new code;
- the release jar serves as the oracle;
- brouter.de regenerates tiles in place, so a pinned copy has to be hosted elsewhere;
- identical routes on the JVM are reachable, and float differences are the main risk on iOS.
**Prediction (mechanism, coder):** to follow by continuation, from the Phase 1 report's item J.
**Finish line:** the report and evidence pushed on `brouter-port`, every claim marked read, computed,
inferred or could not determine, with the four disclosure sections; Hazel stops. The planner files the
report beside the dispatch and brings its choices (F among them) to the owner. Phase 2 waits for the go.
**Abort conditions:**
- an owner-level choice;
- a wrong premise;
- anything that would change slayer8366/Forager;
- a licence question beyond MIT and ODbL notices;
- an account or agreement;
- a moved base that changes a premise;
- two failed attempts at one symptom.
**Dispatch-file:** prompts/preserved/2026-10-28-01-brouter-port.md
**Notes:**
- Written by the planner by hand.
- Planner's call: Gradle may run in the cloud container. Forager's "CI only" rule exists for the laptop's
  memory, which does not apply there.
- Next free ID -4.

---

**Kind:** decision
**ID:** 2026-10-28-4
**Timestamp:** 2026-10-28T20:00:11Z
**Continues:** 2026-10-28-1, 2026-10-28-3
**Title:** Timing in Phase 2; the Kotlin port is for Android too (owner); dispatch 2026-10-28-01
Amendment 1
**Decision:** the owner was told:
- the port will not route better than Java (same algorithm, identical routes by design);
- on Android, Kotlin and Java should run at about the same speed, inferred and not measured;
- on iOS the speed is unknown;
- without an iPhone, phone speed cannot be measured.

The owner, verbatim: "Yes, add timing to Phase 2. But let's do kotlin for Android also".
- **Timing:** Phase 2 times the Kotlin port against the Java engine on the same routes on the JVM, and on
  the iOS simulator in CI, with the plan written in Phase 1 before any timing runs (Amendment 1, item K).
  No pass mark for speed is fixed; identical routes stay the bar.
- **Android:** Forager's Android app will use the Kotlin port instead of the Java engine once it is proven
  identical. This confirms ruling a's last step (-2), and it answers one half of -1's open reading: "the
  Android version stays separate" did not mean Android keeps the Java engine. The switch is Forager's
  work, for Forager's record and planner. Phase 1's item C now also says how Forager's Android build takes
  the port in, which Kotlin version it may compile with (Forager is on 2.3.10), and what it exposes.
- **Still open from -1:** whether iOS reuses Android's Kotlin code beyond the router (Option B's sharing),
  now that the two apps live in separate repositories.
**Dispatch-file:** prompts/preserved/2026-10-28-01-brouter-port-amendment-1.md
**Notes:** Written by the planner by hand. Hazel, mid-Phase 1, is told by message to read the amendment
file itself. Next free ID -5.

---

**Kind:** decision
**ID:** 2026-10-28-5
**Timestamp:** 2026-10-28T20:01:37Z
**Continues:** 2026-10-28-3, 2026-10-28-4
**Title:** Phase 1 restarted with a fresh Hazel after the first was stopped (owner)
**Observed:** the first Hazel, started at -3, was stopped before it pushed anything. When the planner sent it
Amendment 1, the session reported it "stopped by the user". The time it stopped is not known. It left:
- no commit; `brouter-port` stays at `0bfbad9e`;
- no report;
- a scratch clone of upstream BRouter, `scratchpad/hazel/upstream/brouter`, at `v1.7.10`
  (`4d2639af77ea5ed9c30d3e400764eb6f9e8522da`, origin `github.com/abrensch/brouter`, no local changes).
**Decision:** the owner, verbatim: "Start a fresh Hazel on Phase 1". A fresh Hazel starts on the dispatch
and Amendment 1 together. Same branch, scope, stops and finish line as -3.
**Notes:** Written by the planner by hand. Next free ID -6.

---

**Kind:** outcome
**ID:** 2026-10-28-6
**Timestamp:** 2026-10-28T20:29:25Z
**Continues:** 2026-10-28-3, 2026-10-28-5
**Title:** Phase 1, part 1: premises and item A done; Hazel stopped at the long-conversation limit
**Observed:** Hazel (Opus), report `docs/audits/2026-10-28-brouter-port-verify.md` on `brouter-port`
`433a8a8`, evidence in `4f54810` and `6c97d9f`. Filed unedited as
`prompts/preserved/2026-10-28-01-phase1-report-part1.md`. Hazel stopped at about 340k tokens against the
dispatch's "past about 200k tokens, push and hand off". The session reported 370,024 tokens.
- **Item A, method-level reach from Forager's calls,** in four tiers of cuts:
  - T0 14,700 lines, 640 methods;
  - T1 11,759 lines, 515 methods;
  - T2 11,218 lines, 481 methods;
  - T3 10,014 lines, 450 methods.

  The report recommends porting T2 and making T3's profile cuts named "unsupported" failures. The
  class-level 76 classes and 16,296 lines are reproduced.
- **Checked by the planner, not taken from the report:**
  - only the two allowed paths changed (40 files);
  - each tier's figures match its `summary.txt`;
  - in every tier's `compare-with-jacoco.txt`, "ran but not statically reached (unsound if any): 0";
  - the test tile covers 43.4% (T0) to 69.1% (T3) of the kept lines;
  - the Forager clone is unchanged at `e2969abe4`.
- **A wrong premise, the planner's:** dispatch premise 3 said Forager reads only the track's points. It
  also reads `track.distance` (`BRouterTrailRouter.kt:115`), which `recalcTrack` sets
  (`RoutingEngine.java:1565`); both read by the planner. The same claim stands in Forager's report
  (`docs/audits/2026-10-26-ios-multiplatform-feasibility-report.md`, routing update, "Only the track's
  points") and is corrected here, not there.
- **For the owner, with the full Phase 1 report:** whether ruling b's "Forager-only" means the pinned
  profile only (T3) or Forager's calls with any profile (T2).
- **Not answered:** B to K.
**Decision:** the planner continues Phase 1 with a fresh helper, as Forager's planner profile does past
about 200k tokens. The remaining items are split so that no helper overruns: part 2 is B, C (with
Amendment 1's Android questions), D, F, G and H; part 3 is E, I, J and K, which build on part 2.
**Notes:** Written by the planner by hand. Next free ID -7.

---

**Kind:** outcome
**ID:** 2026-10-28-7
**Timestamp:** 2026-10-28T20:40:59Z
**Continues:** 2026-10-28-3, 2026-10-28-6
**Title:** Phase 1, part 2: item B done; Hazel stopped early at the planner's 150k limit
**Observed:** Hazel (Opus), report updated on `brouter-port` `5577491`. Filed unedited as
`prompts/preserved/2026-10-28-01-phase1-report-part2.md`. The session reported 147,607 tokens.
- **Item B:** every one of T2's 48 JDK classes has a common-Kotlin replacement:
  - okio 3.18.2 for files and random-access reads;
  - optionally atomicfu 0.33.0 for one lock;
  - the standard library for the volatile flag and the clock;
  - no sorted-collection library (one `TreeSet` becomes `sorted()`);
  - no formatting on Forager's path.

  A scratch KMP build (Gradle 9.7.0, Kotlin 2.3.10) compiled for the JVM, `iosArm64` and
  `iosSimulatorArm64`, and read the fixture tile identically through okio and `RandomAccessFile`.
  The report also lists semantic gaps to carry into tests: `split`, whitespace, `Math.round(NaN)`,
  `toDegrees`, short reads. Open: whether `HashMap` iteration order on Kotlin/Native changes a result
  at nine sites.
- **Checked by the planner:**
  - only the two allowed paths changed (19 files);
  - the probe's JUnit XML reads tests 2, failures 0;
  - Maven Central's okio release is 3.18.2, and its `iosarm64` module exists.
- **Provenance resolved:** the Gradle 9.7.0 and Kotlin/Native 2.3.10 caches Hazel found unexplained were
  downloaded by the planner's own compiler probes earlier in this session.
- **The planner's prediction against B:** right on random-access reads; wrong on the sorted maps; missed
  the lock, the small new functions and the hash-map order question.
- **Not answered:** C, D, E, F, G, H, I, J, K.
**Cost so far:** part 1 370,024 tokens and part 2 147,607, each re-reading the dispatch, the report and
CLAUDE.md before working. At that rate the nine items left would take several more helpers. The pace is
put to the owner.
**Notes:** Written by the planner by hand. Next free ID -8.

---

**Kind:** decision
**ID:** 2026-10-28-8
**Timestamp:** 2026-10-29T03:30:55Z
**Continues:** 2026-10-28-3, 2026-10-28-7
**Title:** Full Phase 1 as dispatched; the port covers any profile (owner)
**Decision:** the owner chose, from two questions:
- **Pace:** "Full Phase 1 as dispatched". The option read: "All 9 remaining items answered before any
  building starts. About 5 to 9 more helpers (inferred from two so far)." Rejected:
  - "Lean Phase 1 (Recommended)", which moved H, I, J and K into Phase 2;
  - "Pause Phase 1".
- **Scope:** "Any profile". The option read: "About 11,200 lines. Those four features are ported too, so
  other BRouter profiles would work, at roughly 1,200 more lines to port and prove identical."
  - This is the report's tier T2 (11,218 lines, 481 methods).
  - Turn restrictions, `correctMisplacedViaPoints`, turn instructions and bike mode are ported, not
    refused.
  - Rejected: "Hiking profile only (Recommended)", the T3 tier with named "unsupported" failures.
  - Ruling b's "Forager-only" therefore means Forager's calls with any profile.

**The planner's order for the remaining items,** each part a fresh helper reading the dispatch, Amendment
1, the record and the report, with the same 150k-token handoff:
- part 3: C (with Amendment 1's Android questions) and D;
- part 4: F and G;
- part 5: H and E;
- part 6: I, J and K.

F stays a stop for an owner ruling.
**Notes:** Written by the planner by hand. Next free ID -9.

---

**Kind:** outcome
**ID:** 2026-10-28-9
**Timestamp:** 2026-10-29T03:51:13Z
**Continues:** 2026-10-28-3, 2026-10-28-4, 2026-10-28-8
**Title:** Phase 1, part 3: items C and D done, with Amendment 1's three Android questions; no stop hit
**Observed:** Hazel (Opus), report updated on `brouter-port` at `28c9d65` (C) and `956f694` (D). Filed
unedited as `prompts/preserved/2026-10-28-01-phase1-report-part3.md` (SHA-256 equal to the branch's file).
The harness counted 212,857 tokens; the session's own estimate was about 150k.
- **C, layout (all inferred, each backed by a probe or a read):**
  - Gradle 9.7.0, Forager's version;
  - Kotlin 2.3.10 exactly;
  - one KMP module `:brouter`, `commonMain` only, differential tests in `jvmTest`;
  - targets `jvm` (JVM 17), `iosArm64`, `iosSimulatorArm64`; no `androidTarget`, no `iosX64`;
  - packages renamed to `com.zynergylabs.forager.brouter.<sub-package>`, class and file names kept, so the
    oracle and the port share one test JVM;
  - upstream's `LICENSE` unchanged, with a provenance README.
- **C, how Forager takes the port in:** vendored Kotlin source at a pinned Forager-iOS commit, as
  `third_party/brouter/` is today. No account, no secret, and Forager's own compiler builds it. Maven
  Central would need an account (a stop if chosen); the composite build is second.
- **C, Kotlin version (computed):**
  - a 2.3.10 consumer read 2.4.21 output, and a 2.2.21 consumer refused it with 35 errors;
  - an unpinned newer compiler moved the consumer's stdlib upward;
  - pinning `languageVersion`/`apiVersion` 2.3 and `coreLibrariesVersion` 2.3.10 held both at 2.3.
  - An Android build at Forager's settings agreed (compileSdk 35, not Forager's 37) and took the `jvm`
    variant. Neither side has to move.
- **C, what the port exposes:** a small entry point that Forager wraps behind its `TrailRouter`; the engine
  stays `internal`. Today's shapes (`java.io.File`, `quite`, the Java getters, the cancel test's subclass)
  cannot be kept in common Kotlin, so Forager's adapter changes at the switch either way.
- **D, oracle:** the release jar `brouter-1.7.10-ro.jar`, fetched and checked in a `jvmTest`-only task.
  - The zip matches `fetch-brouter.sh`'s size and SHA-256.
  - Its 115 router classes give identical `javap -c -p` to the vendored source built fresh; a negative
    control differs in 43, so the comparison can fail.
  - Part 1's harness printed identical output on the jar and on the source.
- **Checked by the planner:**
  - only the report and its evidence folder changed (51 files since `5577491`);
  - Forager's clone still at `e2969abe4` with a clean tree;
  - `fetch-brouter.sh:12-13`, `app/build.gradle.kts:243, 316-317, 529`, `libs.versions.toml:129-134`, the
    credits entry, `BRouterTrailRouter.kt:135-136, 142, 161, 212-213` and `TrailRouter.kt:15-28` read as
    cited;
  - the probe's control log holds 35 `e:` lines naming metadata 2.4.0;
  - `ro-vs-fresh-build.txt` reads identical 115, the negative control identical 72, different 43;
  - the two harness logs are byte-identical.
- **Moved base:** Forager `pre-main` is now `d100a4776` (32 commits after `e2969abe4`). Checked
  independently: 153 files changed, none on a premise path, and no changed line mentions `btools`,
  BRouter or `TrailRouter`. The only routing-named file is a 6-line change to
  `AvailabilityViewModelOfflineRoutesTest.kt`. No premise moved, so it is not a stop; reads stay at
  `e2969abe4`.
- **Calls beyond the dispatch, by Hazel:**
  - the package prefix;
  - the `-ro` jar rather than Forager's `-all`;
  - the Android consumer probe, using the container's Android SDK;
  - compiler downloads to `~/.gradle`;
  - a blob-less fetch of Forager into scratch.
- **For the owner at Phase 1's end, batched:** the intake route (vendored source), the Kotlin pin, the
  package prefix and the entry-point shape. Each is a recommendation, not yet a ruling.
- **The planner's prediction against D:** right; the release jar serves as the oracle.
- **Not answered:** E, F, G, H, I, J, K.
**Notes:** Written by the planner by hand. Part 4 (F and G) starts next, per -8. Next free ID -10.

---

**Kind:** decision
**ID:** 2026-10-28-10
**Timestamp:** 2026-10-29T04:03:19Z
**Title:** A team for the iOS project; the planner is Cedar Datum (owner)
**Decision:** the owner, verbatim, 2026-10-29: "We need a development team for the iOS project. You're the
planner so you need a name. Two coders, one primary and the other secondary when the primary is busy. One
researcher. One independent reviewer, one UX auditor." Asked whether anything else needs a role, and offered
three names for the planner (Yarrow Meridian, Linden Bearing, Cedar Datum), the owner: "Cedar Datum is the
iOS planner".
- **The planner of -1 to -9** ("a cloud session; not Rowan Waymark") is the same role, now Cedar Datum.
- **Profile:** `docs/process/planner-profile.md`, which works from Forager's planner profile and lists what
  differs here. The other roles propose their own names on their first run, as in Forager, and get a profile
  here when named.
- **Calls by the planner (the owner may overrule):**
  - coders on Opus; researcher, reviewer and UX auditor on Fable, following Forager's -1134;
  - the secondary coder works from the same profile, on separate tasks;
  - Hazel Cairnwright finishes Phase 1 of the BRouter port; the primary coder takes over from Phase 2;
  - keeping iOS level with Android is the planner's duty, checked at the start of each phase, not a role;
  - token counts are kept in the record's outcome entries, since Forager's accountant counts only the
    laptop's logs.
- **Proposed, not ruled:**
  - a designer, since deciding where iOS follows iOS habits and where it matches Android is design work;
  - a person with an iPhone, which no agent can be. It needs TestFlight and so a paid Apple developer
    account, not needed until there is an app to test.
**Notes:** Written by Cedar Datum by hand. Next free ID -11.

---

**Kind:** outcome
**ID:** 2026-10-28-11
**Timestamp:** 2026-10-29T04:04:05Z
**Continues:** 2026-10-28-3, 2026-10-28-8, 2026-10-28-9
**Title:** Phase 1, part 4: item F answered and stopped for the owner's ruling; G not started
**Observed:** Hazel (Opus), report updated on `brouter-port` at `858187a` (F) and `eee9a20`. Filed unedited as
`prompts/preserved/2026-10-28-01-phase1-report-part4.md` (SHA-256 equal to the branch's file). The harness
counted 184,071 tokens; Hazel reported about 110k spent on the required reading alone, and handed off at the
planner's limit before starting G.
- **F, from directory listings and `HEAD` requests only:**
  - `segments4/` lists 1,142 tiles, 10.03 GB, all dated 29-Oct-2026 01:03;
  - the `ETag` is the modification time and length in hex, not a content hash;
  - `diff/W75_N40/` holds 10 non-empty diffs named by 10 different MD5s, one a day from 20 to 29 October,
    plus the zero-byte diff upstream writes for the current tile;
  - no dated archive at brouter.de; the Wayback Machine could not be determined.

  A SHA-256 pin on a brouter.de URL would break within about a day (inferred from the listings). Six
  options, recommending a release on Forager-iOS; nothing chosen, hosted or created. **A stop for the
  owner's ruling.**
- **G:** not started, apart from Forager-iOS being public (an anonymous API read: `private: False`).
- **Checked by the planner:**
  - only the report and its evidence folder changed (16 files since `956f694`);
  - a fresh `HEAD` on `W75_N40.rd5` at 04:03Z matched Hazel's `Content-Length`, `Last-Modified` and `ETag`;
  - a fresh listing of `diff/W75_N40/` matched Hazel's 11 lines exactly;
  - `sources.txt` quotes GitHub's 2 GiB release-asset limit, 100 MiB file block, 10 GiB LFS figures and
    7-day cache expiry, each under its URL.
- **The planner's prediction against F:** right; brouter.de regenerates tiles in place.
- **Not answered:** E, G, H, I, J, K.

**The planner's calls, from this part (technical; the owner may overrule):**
- **The handoff limit goes back to about 200k by the harness's count,** Forager's own rule. With about 110k
  of required reading per helper, the 110k limit left no room for work. Each helper reads the report's
  status, tables, handoff and disclosures, and the sections its items build on, rather than all of it.
- **New order:** part 5, G and H; part 6, E, after F's ruling; part 7, I, J and K. E comes after H because
  its failure cases depend on H's floating-point findings, and its tiles on F.

**Correction to -10:** it says the owner was "asked whether anything else needs a role". The owner asked
that, of the planner; the planner answered with the designer and the person with an iPhone, both still
proposals.
**Notes:** Written by Cedar Datum by hand. Next free ID -12.

---

**Kind:** decision
**ID:** 2026-10-28-12
**Timestamp:** 2026-10-29T04:07:06Z
**Continues:** 2026-10-28-3, 2026-10-28-10, 2026-10-28-11
**Title:** F ruled: the pinned test tiles live in a release on Forager-iOS; the team adds an iOS designer
(owner)
**Decision:** the owner chose, from two questions asked together:
- **F, where the pinned test tiles live:** "Forager-iOS release (Recommended)". The option read: "The chosen
  tiles are copied from brouter.de once and attached to a release on the Forager-iOS GitHub page. Tests
  download each one and check its fingerprint, the way Forager already checks BRouter itself. No account
  and no cost, and nothing is committed to the repository, which matches your earlier ruling ('download in
  CI'). It needs one action on your word: creating the release. GitHub allows files up to 2 GiB each, with
  no download limit."
  - Rejected: "Git LFS in Forager-iOS" (the owner's 10 GiB a month, and reopening ruling c); "Storage on
    another service" (an account, and probably a CI secret).
  - **When the release is made:** in Phase 2, once E has chosen the tiles and they are captured. Creating it
    is a public action, so the planner brings it with the Phase 2 go rather than reading this ruling as that
    word. Part 4's handoff stands: each captured tile is first loaded in the 1.7.10 oracle, and ODbL 4.2's
    notices go with the release.
  - Part 5, running G, was told of the ruling by message at 04:06Z, with no release, tag or upload to make.
- **A designer:** "Add an iOS designer (Recommended)". The option read: "Its own role in Forager-iOS,
  proposing its own name like the others. Keeps the iOS record separate from Android's, as you ruled."
  - Rejected: "Borrow Sage Wayfinder" (crosses the records' separation); "Not yet".
  - Added to the team table in `docs/process/planner-profile.md`. Fable is the planner's call, as for
    Forager's designer.
**Notes:** Written by Cedar Datum by hand. Next free ID -13.

---

**Kind:** decision
**ID:** 2026-10-28-13
**Timestamp:** 2026-10-29T04:09:51Z
**Continues:** 2026-10-28-12
**Title:** The iOS designer starts after Phase 1 of the BRouter port (owner)
**Decision:** told the designer would start once Phase 1 ends, with its first job deciding where the iOS app
follows iPhone habits and where it matches Android, the owner said "Yes on the designer role". Asked whether
that meant starting it now instead, the owner, verbatim: "After Phase 1 is fine".
**Notes:** Written by Cedar Datum by hand. Next free ID -14.

---

**Kind:** outcome
**ID:** 2026-10-28-14
**Timestamp:** 2026-10-29T04:17:02Z
**Continues:** 2026-10-28-3, 2026-10-28-11, 2026-10-28-12
**Title:** Phase 1, part 5: item G done, written for F's ruling; item H in part
**Observed:** Hazel (Opus), report updated on `brouter-port` at `8adb3da` (H's evidence) and `d24259e`. Filed
unedited as `prompts/preserved/2026-10-28-01-phase1-report-part5.md` (SHA-256 equal to the branch's file). The
harness counted 194,805 tokens. Nothing under `.github/`, no CI run, and no release, tag or upload.
- **G:**
  - `ubuntu-24.04` runs `jvmTest` and the iOS klib compiles from Phase 2's first commit;
  - `macos-26` with Xcode 26.0.1 runs the simulator tests, added once `commonTest` holds a test whose answer
    can differ on iOS;
  - `macos-26` is Apple silicon ("3 (M1)", arm64), so dropping `iosX64` holds;
  - the tiles come from the Forager-iOS release with size and SHA-256 checks, no secret;
  - a workflow sketch as text, `08-ci/workflow-sketch.yml.txt`.
- **G, Kotlin 2.3.10 against the runner's Xcode:** JetBrains' table pairs 2.3.10 with Xcode 26.0, and the
  images carry 26.0.1. But KGP 2.3.10's `XcodeVersion.maxTested` is 16.0, so a warning ("Xcode Version Too
  High") is expected on every Xcode 26 (inferred). Whether 2.3.10 links and runs simulator tests there is
  unverified.
- **H, hash order:** three of B's nine `HashMap` sites are in methods no tier reaches, which corrects B row 18
  and -7's "nine sites". Of 7 reached sites, 5 cannot change a route (read). Open: variable numbering in
  `_parseFile` with more than one `profile:` key, and `OsmNodesMap.collectOutreachers` on the memory-pressure
  path.
- **H, floating point:** 68 of T2's 481 methods use double arithmetic or library maths (computed, listed in
  `09-float/out/T2/`). Not done: which choose between routes, whether the JVM and Kotlin/Native can differ at
  each, and the simulator test.
- **A finding against C, not a stop:** JetBrains' table gives Kotlin 2.3.10 with Gradle 7.6.3 to 9.0.0 and AGP
  8.2.2 to 9.0.0. C's Gradle 9.7.0 and Forager's AGP 9.3.1 are outside it; they built without error in parts 2
  and 3. C's text is unchanged.
- **Checked by the planner:**
  - only the report and its evidence folder changed (7 files since `eee9a20`);
  - KGP 2.3.10's bytecode sets `maxTested` to `XcodeVersion(16, 0)` (`javap -c -p` on
    `kotlin-native-utils-2.3.10.jar`);
  - the live kotlinlang.org table at 04:15Z reads "2.3.10 7.6.3–9.0.0 8.2.2–9.0.0". The same table pairs
    2.4.20 to 2.4.21 with Gradle 7.6.3 to 9.7.0 and AGP 8.5.2 to 9.3.1, which contains Forager's versions.
- **Correction, from the helper's own transcript:** Hazel reported "the required reading took about 145k".
  The transcript's per-call context reads 60,723 tokens before the first tool call (the prompt, tools and
  injected `CLAUDE.md` files), about 106k once the reading was done, and 192,120 at the end. So reading took
  about 45k, and about 61k is fixed before any reading. -11's "about 110k of required reading" came from a
  helper's estimate and is wrong the same way.
- **Not answered:** E, the rest of H, I, J, K.

**The planner's calls (technical; the owner may overrule):**
- **New order:** part 6 finishes H; part 7, E; part 8, I, J and K. E's failure cases depend on H.
- **Hazel's handoff list for H stands as part 6's scope.**
- **The Gradle and AGP finding** goes into the batch for the owner at Phase 1's end, with C's other
  recommendations.
**Notes:** Written by Cedar Datum by hand. Next free ID -15.

---

**Kind:** outcome
**ID:** 2026-10-28-15
**Timestamp:** 2026-10-29T04:48:20Z
**Continues:** 2026-10-28-3, 2026-10-28-14
**Title:** Phase 1, part 6: item H steps 1 and 2 done; steps 3 and 4 not written; the helper ran far past its
limit
**Observed:** Hazel (Opus), report updated on `brouter-port` at `5b8936f` (step 1), `906a878` (step 2) and
`ae5260b`. Filed unedited as `prompts/preserved/2026-10-28-01-phase1-report-part6.md` (SHA-256 equal to the
branch's file). **The harness counted 312,645 tokens against a 200k limit;** Hazel noticed late, finished step 2,
pushed and handed off.
- **Step 1, which floating-point sites can choose a route:** all 72 methods classified by hand, with lines.
  - 30 can change a route, cost, node list or failure kind on Forager's calls with some profile; 5 more only
    with a parameter Forager never passes.
  - Only two library functions reach a route on Forager's calls: `cos`, which builds CheapRuler's 1,800-row
    scale table (`CheapRuler.java:37`, `Math.cos`), and `atan2`, a tie-break in waypoint matching.
- **Step 2, JVM against Kotlin/Native** (scratch probe on the JVM, `linuxX64` and `linuxArm64` under qemu, 10 of
  10 tests each):
  - same results for IEEE arithmetic, number parsing and number printing;
  - different results from the C maths library: against the JVM, `cos` differs at 8 of 1,800 table rows and
    `atan2` at 50,215 of 299,974 inputs (glibc 2.39);
  - the JVM is not one answer either: `Math.cos` and `StrictMath.cos` differ at 60 of 1,800 rows on one
    HotSpot;
  - no fused multiply-add in Kotlin code on arm64 (disassembly, with a positive control);
  - `roundToLong` on Native is `floor(x + 0.5)`; argued equal to `Math.round` at BRouter's clamped distance
    sites (inferred).

  Linux runs stand for iOS in arithmetic, fusion, parsing, printing and `HashMap` order, not in Apple's
  `cos` or `atan2`. Three options for the table and for `atan2`, not chosen: (a) constants written on the CI
  JVM; (b) compute per platform and accept differences; (c) port fdlibm.
- **Step 3, `HashMap` order: evidence committed, not written into H.** Kotlin/Native's `HashMap` iterates in
  insertion order. In a simplified copy of `collectOutreachers`, `nodesCreated` differed in 14 of 20 graphs; it
  feeds the memory-limit failure (`RoutingEngine.java:2026-2031`), so order can change the failure kind there
  (inferred). `_parseFile` cannot change a result (read, inferred).
- **Step 4, the simulator test:** not started; a draft probe is in `09-float/probe/`.
- **Checked by the planner:**
  - only the report and its evidence folder changed (47 files since `d24259e`);
  - `classification-summary.txt` reads 72 classified of 72, none missing. `sites.tsv` holds 74
    method-and-signature entries, 68 with floating point and 6 with hash iteration only. The classification
    keys by name, so two overloaded pairs (`readPosition`, `getStartPath`) share a verdict; both are classed
    as able to change a route.
  - the compare logs read: 8 differing table rows and 50,215 differing `atan2` lines (JVM against `linuxX64`);
    the Math-against-StrictMath log reads 60 of 1,800; `libm-check-x64` and `-arm64` agree with those counts.
- **A question the report does not ask, for part 7:** Forager's app runs the Java engine on Android's runtime,
  not on HotSpot. Whether Android's `Math.cos` and `Math.atan2` give HotSpot's bits decides whether option (a)
  would change routes on Android phones at the switch. Not checked.
- **Not answered:** H steps 3 (written) and 4, E, I, J, K.

**The planner's calls (technical; the owner may overrule):**
- **Part 7:** H step 3 written from part 6's evidence, step 4 designed, and the Android question above. Then
  part 8, E; part 9, I, J and K.
- **The token limit is watched from outside.** The helpers cannot see the harness's count, and three of six
  parts overran it. The planner reads the helper's transcript usage with a background script and messages it
  to hand off past about 180k.
- **Options (a) to (c) go to the owner at Phase 1's end,** since (b) lets iOS routes differ from Android's. With
  a recommendation once E and the Android question are in.
**Notes:** Written by Cedar Datum by hand. Next free ID -16.

---

**Kind:** outcome
**ID:** 2026-10-28-16
**Timestamp:** 2026-10-29T05:04:16Z
**Continues:** 2026-10-28-3, 2026-10-28-15
**Title:** Phase 1, part 7: H steps 3 and 4 written; the Android question answered in part; the outside token
watch worked
**Observed:** Hazel (Opus), report updated on `brouter-port` at `765b65c` (step 3), `2b0a289` (step 4) and
`6736856`. Filed unedited as `prompts/preserved/2026-10-28-01-phase1-report-part7.md` (SHA-256 equal to the
branch's file). The planner's watcher read 181,430 tokens at 05:01:13Z and the planner messaged a handoff; the
harness's final count was 196,186.
- **Step 3, `HashMap` order:**
  - Kotlin/Native's `HashMap` keeps insertion order;
  - `_parseFile` need not copy the JVM's order;
  - in `collectOutreachers` only `nodesCreated` depends on order, which feeds the memory-limit failure. The
    14-of-20 figure is from a simplified copy of the loop, and the path is reachable on Forager's calls (a
    search past about 42,100 nodes).
- **A new finding, against the oracle itself:** where nine or more `OsmNode`s share one hash bucket,
  `java.util.HashMap` breaks ties by `System.identityHashCode`. Five builds in one JVM gave five different
  orders; three JVMs repeated the same five. So the oracle's order there depends on the process's history.
  Three port options for `hmap`, (i) to (iii), not chosen. Before E fixes expected results on that path, the
  Java engine is to be run twice in one JVM to see whether it agrees with itself.
- **Step 4, the simulator test, designed only:** inputs generated in code; expected values as digests in the
  test source, regenerated by `jvmTest` on a pinned JDK; a one-ulp positive control; a table of what a
  mismatch means under (a) to (c). Under (c) the JVM port differs from the HotSpot oracle at 60 table rows.
- **The Android question, in part:** Android's `Math.cos` and `Math.atan2` are native methods that return C's
  `cos` and `atan2`. Not determined: ART's compiler intrinsics, bionic's implementation, API 26 to 37, and
  whether the bits equal HotSpot's.
- **Checked by the planner:**
  - only the report and `09-float/part7/` changed (7 files since `ae5260b`);
  - `treebin-order.txt`: runs 2 and 3 print the same five differing orders, controls 5 of 5;
  - read at source: OpenJDK `jdk21u` tag `jdk-21.0.12.1-ga`, `HashMap.java:2058-2066`, `tieBreakOrder`
    falling back to `System.identityHashCode`; Android `libcore` `de876a01b`, `Math.c`, `Math_cos` returning
    `cos(d)` and `Math_atan2` returning `atan2(d1, d2)`.
- **Not answered:** the rest of the Android question, E, I, J, K.

**The planner's calls (technical; the owner may overrule):**
- **Part 8 is E,** now. Part 9 is I, J and K.
- **The rest of the Android question** is put to the owner as a choice: the team's researcher now, in parallel
  with part 8, or Hazel in part 9. Starting a role that has not run before is the owner's word to give.
- **The `hmap` options (i) to (iii)** join (a) to (c) in the batch for the owner at Phase 1's end.
**Notes:** Written by Cedar Datum by hand. Next free ID -17.
