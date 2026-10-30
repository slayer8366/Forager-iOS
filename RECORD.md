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

---

**Kind:** outcome
**ID:** 2026-10-28-17
**Timestamp:** 2026-10-29T10:32:31Z
**Continues:** 2026-10-28-3, 2026-10-28-16
**Title:** Phase 1, part 8: item E designed, fixed before data; the helper overran because the planner's
session restarted
**Observed:** Hazel (Opus), report updated on `brouter-port` at `18c4a0f` (evidence), `358cbc3` (E) and `529cea9`.
Filed unedited as `prompts/preserved/2026-10-28-01-phase1-report-part8.md` (SHA-256 equal to the branch's
file). Nothing was routed and no tile was downloaded.
- **The overrun, the planner's slip:** the watcher read 187,780 tokens at 05:12:43Z, but the planner's worker
  process was restarted around then and the handoff message was never sent. The helper's transcript ends at
  277,941. Its final message was lost in the restart and was read back from that transcript at 10:31Z.
- **E, in brief:**
  - **Strata** with fixed seeds: random (R), Forager's two routes (F0), the 5-degree lines at 40N and at
    70W (two strata), the memory-collection path (MP64), "memory limit reached" (ML5), memory panic mode
    (PM5), waypoint near-ties (NT), latitude bands (LB), the crossing case (CR), and 11 failure kinds.
  - **Tiles for the release:** `W75_N40`, `W75_N35`, `W70_N40` and `E170_S50`, plus Forager's sparse
    fixture; about 129.8 MB.
  - **Profiles:** all 26 in the zip, plus one of E's own for the crossing case.
  - **"Identical":** failure kind, exact node list, cost and distance. Messages are exact, except timeout
    and cancel, which are compared by family. Expected values are stored as digests.
  - **The oracle is checked against itself** under four regimes, with a positive control. More than 5%
    unstable in any stratum stops Phase 2.
  - **The oracle is one pinned HotSpot on x86_64,** and E says it cannot speak for Forager's Android app.
  - **Sample:** exact n, 59 for a 5% rate and 299 for 1%, at 95%. 2,118 cases; the iOS fallback is 312
    plus every targeted case.
- **Findings that correct earlier text:**
  - **`correctMisplacedViaPoints` cannot run through Forager's two-waypoint call.** This corrects the
    planner's option text quoted in -8, which listed it among the four features to be ported. Porting it
    untested, leaving it out, or widening the entry point to take via points is a choice for the owner.
  - part 6's "8 glibc rows": only 6 change `kx` or `ky`;
  - C's sketched API carries no cost, though "identical" includes it;
  - with HotSpot's `cos` intrinsic switched off (`-XX:-UseLibmIntrinsic`), its table equals `StrictMath`'s,
    so option (c) would have an exact oracle on the JVM.
- **Checked by the planner:**
  - only the report and `10-tests/` changed (17 files since `6736856`);
  - `sample-size.txt`: 59 and 299 with both inequalities shown; 26 × 59 = 1,534, pooled bound 0.001951;
    eight targeted strata of 59, plus 110 and 2, total 2,118;
  - the five tile sizes sum to 129,828,703 bytes;
  - `rows-math-vs-strict.txt`: cos differs 60, `kx` or `ky` 54; `rows-nointrinsic-vs-strict.txt`: 0 of
    1,800 on every count;
  - the via-point claim, read at source: `RoutingEngine.java:1023` calls `snapPathConnection` only when
    the flag is set, `:1265-1266` returns at once unless the waypoint's name starts with "via" or "rt",
    and `RoutingParamCollector.java:48-50` renames the first and last waypoints "from" and "to". Forager
    passes two (`BRouterTrailRouter.kt:149`).
- **Not answered:** the rest of the Android question, I, J, K.
**Notes:** Written by Cedar Datum by hand. Next free ID -18.

---

**Kind:** decision
**ID:** 2026-10-28-18
**Timestamp:** 2026-10-29T10:32:31Z
**Continues:** 2026-10-28-10, 2026-10-28-16
**Title:** The rest of the Android question goes to the researcher, now (owner)
**Decision:** asked who should finish the Android maths question, the owner chose "Researcher, now
(Recommended)". The option read: "The iOS team's researcher runs for the first time, read-only, alongside
part 8. That saves time, and it keeps Hazel's last part to its three items. On this first run it proposes
three names for you to pick from, as Forager's roles did." Rejected: "Hazel, in part 9".
- Part 8 had finished by the time the answer was read, so the researcher runs alongside part 9 instead.
**Notes:** Written by Cedar Datum by hand. Next free ID -19.

---

**Kind:** intent
**ID:** 2026-10-28-19
**Timestamp:** 2026-10-29T10:33:29Z
**Continues:** 2026-10-28-15, 2026-10-28-16, 2026-10-28-18
**Title:** Do Android phones compute BRouter's cosine and atan2 as the test Java does? (dispatch 2026-10-29-01, the
researcher's first run)
**Change:** nothing in any app. One report, `docs/research/2026-10-29-android-math.md` with an evidence folder, on
branch `research/android-math`, answering:
- what ART does with `Math.cos` and `Math.atan2`;
- which implementation bionic supplies, and whether it changed across API 26 to 37;
- what HotSpot computes, on x86_64 and aarch64;
- whether `kotlin.math` on the JVM calls `java.lang.Math`;
- what can be read about Apple's;
- a bounded measurement on the table's 1,800 inputs, if the sources allow it.
**Scope boundary:** writes only `research/android-math` in slayer8366/Forager-iOS, cut from `main` at the commit
adding this entry. Reads published source at named commits. Does not touch slayer8366/Forager, Forager-iOS `main`,
`RECORD.md`, `prompts/` or `brouter-port`, any account, phone or emulator image.
**Baseline:** Forager `pre-main` `d100a4776` (10:33Z); Forager-iOS `brouter-port` `529cea9`.
**Prediction (outcome, planner):**
- every path in ART, interpreted or compiled, ends in bionic's C library for both functions;
- bionic's `cos` is not HotSpot's default algorithm, so some of the 1,800 table rows differ between Android and
  HotSpot's default `Math.cos`;
- the implementation changed at least once between API 26 and 37, so phones on different Android versions can
  disagree with each other;
- `kotlin.math.cos` on the JVM is `java.lang.Math.cos`;
- Apple's implementation cannot be read at source.
**Finish line:** the report and evidence pushed on `research/android-math`, every claim marked, and the final message
with three proposed names; the researcher stops. The planner records the outcome and brings the answer and the names
to the owner.
**Abort conditions:**
- an owner-level choice;
- anything that would change slayer8366/Forager;
- an account or agreement;
- a licence question;
- two failed attempts at one symptom.
**Dispatch-file:** prompts/preserved/2026-10-29-01-android-math.md
**Notes:**
- Written by Cedar Datum by hand.
- Runs on Fable (-10).
- Part 9 of dispatch 2026-10-28-01 (I, J and K, continuing -3) starts alongside it, on `brouter-port`; the two
  write different branches.
- Next free ID -20.

---

**Kind:** outcome
**ID:** 2026-10-28-20
**Timestamp:** 2026-10-29T10:49:11Z
**Continues:** 2026-10-28-3, 2026-10-28-17
**Title:** Phase 1, part 9: item I, the Phase 2 order, done; J and K not written
**Observed:** Hazel (Opus), report updated on `brouter-port` at `775f122` (I, with `11-plan/`) and `7b2b403`. Filed
unedited as `prompts/preserved/2026-10-28-01-phase1-report-part9.md` (SHA-256 equal to the branch's file). The
planner's watcher read 194,590 tokens at 10:43:21Z with nothing yet pushed; the planner told Hazel to finish and
push I and not start J or K. The harness's final count was 230,100.
- **I, the order:**
  - 0, scaffold; 1, tile capture; 1b, the oracle's own runs (E3 and the expected values);
  - 2a and 2b, `util`, with the simulator job joining at 2b; 3, `codec`; 4, `expressions`;
  - 5a to 5c, `mapaccess`; 6 to 8d, `router`;
  - 9, the full JVM run; 10, the simulator run; 11, timing; 12, the Phase 2 report.

  Each step has its tests, a revert, its size and push points, and the rulings it waits on.
  - The packages build bottom-up; every upward dependency is a callback through a type the lower package
    declares.
  - 11,188 lines to port, or 10,759 without the via-point code.
  - The oracle-only steps come first, so E3's 5% stop is met before the 5,444 lines of `router`.
  - Branches written for (a) to (c) ((c) needs 726 lines of fdlibm), (i) to (iii), and the via-point choice.
- **J and K:** not written; part 9's working is in the report's handoff, marked as input only.
- **Checked by the planner:** only the report and `11-plan/` changed (7 files since `529cea9`).
  `via-point-spans.txt` names the four methods reached only through `snapPathConnection`, with their callers and
  spans, totalling 429 lines; 11,188 less 429 is 10,759.
- **Why the overrun:** the report is now 2,448 lines, and part 9's reading list took it from about 61k to about
  143k before any work. -16's watcher fixed the count's visibility, not the reading's size.
- **Not answered:** J, K, and the rest of the Android question (the researcher, -19).

**The planner's calls (technical; the owner may overrule):**
- **Part 10 writes J and K only,** reading the dispatch, Amendment 1 and named line ranges of the report rather
  than whole sections.
- **The planner writes the list of owner choices itself,** from this record and the report, rather than giving a
  helper another long read.
**Notes:** Written by Cedar Datum by hand. Next free ID -21.

---

**Kind:** outcome
**ID:** 2026-10-28-21
**Timestamp:** 2026-10-29T10:57:52Z
**Continues:** 2026-10-28-19
**Title:** Research 2026-10-29-01, run 1: Android's cosine and atan2 come from FreeBSD's maths library; the measurement
not reached
**Observed:** the researcher (Fable), branch `research/android-math` at `634abf7` (method and predictions, before any
source was read) and `0e37ccb` (the report, partial). Filed unedited as
`prompts/preserved/2026-10-29-01-android-math-report-run1.md` (SHA-256 equal to the branch's file). The planner's
watcher read 186,680 tokens at 10:50:52Z with only the method pushed; the planner told the researcher to write up and
hand off. The harness's final count was 240,287.
- **ART (answered):** the interpreter, the JIT and ahead-of-time code all end in the C library's `cos` and `atan2`.
- **bionic (answered):** at API 37 both are FreeBSD msun C, of the fdlibm family, on every architecture, built
  without fused multiply-add. At API 26, x86_64 used Intel assembly, and arm64 the same C.
- **API 26 to 37 (partial):** on arm64, the sources change nothing on the table's path. x86_64 dropped the
  assembly at API 34.
- **HotSpot (answered, except the aarch64 stub's bits):**
  - x86_64's default `Math.cos` is Intel's library, under `UseLibmIntrinsic`, an x86-only diagnostic flag;
  - aarch64's is a fused-multiply variant of fdlibm, switched off by `-XX:DisableIntrinsic=_dcos`;
  - **`Math.atan2` is never intrinsified:** it is Java's fdlibm on every CPU.
- **Kotlin (answered):** `kotlin.math.cos` and `atan2` on the JVM are `java.lang.Math`'s. The premise is confirmed.
- **Apple:** could not determine. The source page is gone, and the GitHub API was refused by this session's
  access policy.
- **Measurement:** not reached; the method is committed.
- **Conclusion, as far as read:** Android arm64 phones can differ from the default x86_64 HotSpot oracle on `cos`.
  Whether Android equals `StrictMath` (fdlibm) at all 1,800 rows, and on `atan2`, is the measurement.
- **Correction to the Phase 1 report:** `-XX:-UseLibmIntrinsic` exists only on x86. E2, H's step 4 and K assume it,
  or HotSpot's default `cos`, for the `macos-26` job, where HotSpot uses the aarch64 stub instead.
- **The planner's prediction (-19), scored:**
  - right: ART ends in bionic; `kotlin.math` is `java.lang.Math`;
  - right on mechanism, unmeasured on rows: bionic's `cos` is not HotSpot's default algorithm;
  - wrong for arm64: "changed at least once between API 26 and 37". Only x86_64 changed; on arm64 the table's
    path did not.
  - right, for a weaker reason than predicted: Apple's source could not be read.
- **Checked by the planner, at source:**
  - OpenJDK `jdk21u` `jdk-21.0.12.1-ga`: `library_call.cpp:1874-1876` returns false for `_datan2` ("not yet
    correctly implemented"); `UseLibmIntrinsic` is declared in `globals_x86.hpp:214` and absent from
    `globals_aarch64.hpp`; `macroAssembler_aarch64_trig.cpp` holds 19 `fmaddd`.
  - bionic `android-17.0.0_r1` `libm/Android.bp:34`, `:68`, `:79`, `:98` list msun's `e_atan2.c`, `k_cos.c`,
    `s_atan.c` and `s_cos.c`.
  - Only the research files changed (12 files since `490b0e3`).
- **Three names proposed:** Larch Plumbline, Alder Benchmark, Madrone Gauge. Two repeat a Forager name: "Plumbline"
  is Thistle Plumbline's surname, and "Alder" is a member of Forager's forecast team.
**The planner's call:** run 2 of the same dispatch, on the measurement and the read of `k_cos.c` and `s_atan.c`
against fdlibm first; the handoff's smaller reads after, if they fit. The answer decides which of (a) to (c) the
planner recommends.
**Notes:** Written by Cedar Datum by hand. Next free ID -22.

---

**Kind:** outcome
**ID:** 2026-10-28-22
**Timestamp:** 2026-10-29T11:01:45Z
**Continues:** 2026-10-28-3, 2026-10-28-4, 2026-10-28-20
**Title:** Phase 1, part 10: items J and K done; Phase 1's report complete apart from the Android measurement; the
coder's mechanism prediction
**Observed:** Hazel (Opus), report updated on `brouter-port` at `08fea32` (J), `6b73fa4` (K) and `20e1f92`. Filed
unedited as `prompts/preserved/2026-10-28-01-phase1-report-part10.md` (SHA-256 equal to the branch's file). The
harness counted 172,818 tokens: the first part under its limit since -11, with a reading list of named line
ranges.

**Prediction (mechanism, coder), continuing -3** (the report's J, lines 1755 to 1881 at `20e1f92`):
- **How:** each reached Java method becomes one Kotlin function, with the same statements in the same order on
  the same primitive types. That gives the same bits for integer arithmetic, basic IEEE operations, conversions,
  parsing and printing. The search turns doubles into `int` costs before choosing (`OsmNode.java:103`;
  `OsmPath.java:166`, `:315`, `:384`).
- **Only four sites fall outside that:** the scale table (`cos`), `atan2` in the waypoint tie-break, `hmap`'s
  order, and `roundToLong`. Step 2a writes `roundToLong` out as Java's.
- **A one-ulp table difference moves no route:** at most 2.2e-5 per route, 0.047 over all 2,118 cases.
  Predicted: 0 route differences from the table, anywhere.
- **Recorded differences predicted, by option:**
  - (a) with `atan2` by (c): 0 everywhere.
  - (a) with `atan2` by (b), or (b): 0 on the JVM; on iOS, NT about 6 of 59 (1 to 14), at the route's start
    only. Under (b), LB 0 of 59.
  - (c): 0 on the JVM, even against the default oracle; 0 on iOS.
  - `hmap` (i): 0 on both. (ii): 0 on the JVM; on iOS MP64 about 3 of 59, ML5 0, PM5 about 1. (iii): those
    counts on both platforms, in the same set of cases.
  - E3: 0 unstable cases.
  - On iOS, FK 7 and 8 (20 cases) fail message equality on the first run, because `NodesCache.java:202` puts
    the exception's text into the message.
- **Where Phase 2's first failures come from, ranked:** integer, bit and byte transliteration in `util` and
  `codec`; text handling in `expressions`; decoding order in `DirectWeaver`; ties and overflow near the
  1e9 sentinel in the search; on iOS, the harness before any route runs. At least 80% of defects changing an
  asserted value on the JVM will be integer, bit or text slips, none a floating-point library difference; 10
  to 30 such defects (low confidence).

**K, the timing plan:**
- **What is timed:** each stable, identical case of at least 1 ms, oracle and port in one JVM, in its own task,
  on `ubuntu-24.04`.
- **How:** one untimed warm-up pass; k = 5 (3 if over 4 hours), fixed from step 1b's oracle times; a seeded
  random order with the engines alternating; an A/A control for noise.
- **Reported:** the median per-case port/Java ratio with a 95% bootstrap interval, the 10th and 90th
  percentiles, and the total ratio, by stratum and by time decile.
- **Memory:** allocated bytes per route, and GC counts and time.
- **iOS:** the port on the simulator against HotSpot arm64 on the same `macos-26` runner. The report says
  simulator speed is not phone speed, and that Android's runtime is not timed either.
- **Proposed flag S:** the port is slower when the median ratio's 95% interval lies wholly above 1 + m. The
  margin m is the owner's to set.
- **Optional flags:** L, a route found within Forager's 30,000 ms by the oracle but not by the port; and a
  memory flag with its own margin. No flag fails the build.

- **Checked by the planner:**
  - only the report and `12-prediction/j-figures.txt` changed since `7b2b403`;
  - `j-figures.txt` lists the 12 in-tile rows of 54, the glibc in-tile row 431, and the bound's arithmetic;
  - read at Forager `e2969abe4`: `OsmNode.java:103`, the `(int) Math.max(1.0, Math.round(...))`;
    `WaypointMatcherImpl.java:69-71`, the radius-then-`directionDiff` comparator; `:189-190`; and
    `NodesCache.java:202`, `"error reading datafile " + currentFileName + ": " + e`.
- **Not settled by J:** whether `HotSpot`'s `-XX:-UseLibmIntrinsic` holds on the `macos-26` job. It does not exist
  on aarch64 (-21), and K times HotSpot arm64 there.
- **Phase 1's items:** A to K are answered. The Android measurement is the researcher's run 2 (-21).
**Notes:** Written by Cedar Datum by hand. Next free ID -23.

---

**Kind:** terminal
**ID:** 2026-10-28-23
**Timestamp:** 2026-10-29T11:14:30Z
**Closes:** 2026-10-28-19
**Title:** Research 2026-10-29-01 done: Android's cosine table differs from every HotSpot setting; atan2 agrees
everywhere
**Observed:** the researcher (Fable), run 2, branch `research/android-math` at `c5893b6` (the measurement), `1ff88aa`
and `736a4c1`. `1ff88aa` dropped four sections by a faulty edit, and `736a4c1` restored them minutes later. Filed
unedited as `prompts/preserved/2026-10-29-01-android-math-report-run2.md` (SHA-256 equal to the branch's file).
The watcher read 188,920 tokens at 11:09:34Z, after the measurement was pushed, and the planner asked for a wrap-up.
The harness's final count was 221,913.
- **The measurement (computed):**
  - **What was built:** bionic's FreeBSD msun sources at `android-17.0.0_r1`, compiled with Android's own flags
    (`-ffp-contract=off -fno-builtin -fno-math-errno`) by clang 18 and by gcc 13. The two outputs are byte for
    byte identical.
  - **`cos`:** against `StrictMath.cos` (fdlibm), 13 of 1,800 rows differ, each by 1 ulp, all where only
    `__kernel_cos` runs. Against HotSpot's default `Math.cos`, 59 of 1,800.
  - **`atan2`:** 0 of 299,974 against both.
  - **Outside checks:** HotSpot's table equals part 6's byte for byte; Math against Strict is 60 of 1,800 again;
    the pairs regenerate to 299,974.
- **Why the 13 (read):** FreeBSD's `k_cos.c` keeps fdlibm's six constants but evaluates them differently.
  `s_atan.c` and `e_atan2.c` are fdlibm's algorithm line for line.
- **Also read:**
  - ARM's optimized routines supply no double `cos` or `atan2` on Android;
  - the contraction flag sits in soong's `commonGlobalCflags`;
  - Android 12 built with clang 12.0.7;
  - Apple's `Libm` on GitHub is a 2021 tree without arm64, not what iOS ships.
- **Could not determine:**
  - the commit removing Android's x86_64 assembly;
  - whether HotSpot's fdlibm flags reach its C++ trig;
  - a phone's own `libm.so` bits; the result is modelled on x86_64 and inferred for arm64 from the source;
  - whether the 13 rows change `kx` or `ky`;
  - iOS's own `cos` and `atan2`.
- **Conclusion:** no HotSpot setting reproduces today's Android cosine table. The no-intrinsic (fdlibm) oracle is
  nearest, at 13 rows; the default is at 59. `atan2` is the same on HotSpot and Android at every pair tried.
- **Checked by the planner:**
  - only research files changed on the branch;
  - all report sections are present at `736a4c1`;
  - `q7/README.txt` and the comparison files read: four `atan2` comparisons at 0 of 299,974; `cos` differences
    against Math and against Strict listed row by row;
  - `rows-msun-vs-strict.txt` lists the 13 rows. Five of them (39.55, 39.75, 41.05, 41.15 and 41.85 degrees)
    fall in the latitude bands of E's tiles `W75_N35` and `W75_N40`.
- **The dispatch's finish line is met:** the report and evidence are pushed, and the three names came in run 1
  (-21).
**Notes:** Written by Cedar Datum by hand. Next free ID -24.

---

**Kind:** decision
**ID:** 2026-10-28-24
**Timestamp:** 2026-10-29T17:25:28Z
**Continues:** 2026-10-28-3, 2026-10-28-17, 2026-10-28-21, 2026-10-28-22, 2026-10-28-23
**Title:** Phase 1's owner choices: the port copies Android's maths; via points left out; a 25% flag; the researcher
is Madrone Gauge (owner)
**Decision:** the owner chose, from four questions asked together after Phase 1's report and the research closed:
- **Same route on iPhone and Android:** "Copy Android's maths (Recommended)". The option read: "The port carries its
  own copy of the cosine code Android phones use today, plus a copy of Java's map ordering for very long searches.
  iPhone and Android then compute the same thing, and Android's routes don't change when it switches. The
  desktop-Java tests still prove the port's logic exactly, and separately record whether the 13 rows ever change a
  route. About 700 lines of maths code, plus the map ordering, which isn't sized yet."
  - Rejected: "Copy Java's reference maths" (H's option (c)) and "Each phone uses its own" (H's (b)).
  - **Read as:**
    - `cos` from bionic's FreeBSD msun, ported to common Kotlin. This is a fourth option beside H's (a) to (c),
      offered from -23's measurement.
    - `atan2` from the same source. It equals HotSpot's on all 299,974 pairs, so the oracle covers it exactly.
    - `hmap` in `java.util.HashMap`'s order, part 7's option (i).
- **Via points:** "Leave it out (Recommended)". The option read: "429 lines that can't run through Forager's call
  aren't ported, so nothing untested ships. If Forager ever adds routes with stops along the way, it gets ported
  then, with tests." Rejected: "Port it untested"; "Port it and allow stops".
  - This corrects -8's scope: the port is tier T2 less the four via-point methods, 10,759 lines (-20).
- **Speed flag:** "25% slower (Recommended)". The option read: "The port copies Java line for line, so on the same
  machine it should land close to Java. 25% sits above ordinary timing wobble while still catching a real
  slowdown." Rejected: 10%, 50%.
  - The question covered speed and memory together, so the margin m is 0.25 for flag S and for the memory flag
    (K).
- **The researcher's name:** "Madrone Gauge (Recommended)". Rejected: Larch Plumbline, Alder Benchmark.
  - Profile: `docs/process/researcher-profile.md`, added to the planner profile's team table.

**The planner's calls that follow (technical; the owner may overrule):**
- **The oracle** runs on `ubuntu-24.04` (x86_64) with `-XX:-UseLibmIntrinsic`: fdlibm, 13 rows from Android rather
  than 59.
- **Two kinds of run:**
  - E's logic runs inject the oracle's own table through the test seam, so the port's logic is proven exactly;
  - production runs use the msun table and record any route difference.
- **The port's own build** uses Gradle 9.0.0, inside JetBrains' tested range for Kotlin 2.3.10, rather than
  Forager's 9.7.0 (-14). Forager takes the port as source, so the port's Gradle does not reach Forager. Step 0 checks
  it, along with Kotlin 2.3.10 against `macos-26`'s Xcode 26.0.1.
- **Package prefix** `com.zynergylabs.forager.brouter`; the `-ro` jar as oracle; runner labels `ubuntu-24.04` and
  `macos-26`; E's thresholds.
- **The intake route for Forager's Android app** (vendored source) stays C's recommendation, for Forager's planner
  at the switch.
**Notes:** Written by Cedar Datum by hand. The answers came after the planner's worker restarted; the time is the
recording time. Next free ID -25.

---

**Kind:** decision
**ID:** 2026-10-28-25
**Timestamp:** 2026-10-29T19:54:10Z
**Continues:** 2026-10-28-3, 2026-10-28-24
**Title:** Phase 2 starts with steps 0 and 1 only, then pauses (owner); Amendment 2
**Decision:** asked whether to start Phase 2, the owner chose "Start, pause after checks (Recommended)". The option
read: "Run the first steps only: set up the project and CI, then download and check the test tiles. Two big unknowns
get answered cheaply before the 11,000-line port starts: whether Kotlin 2.3.10 builds on GitHub's Mac runners, and
whether BRouter 1.7.10 can still read today's brouter.de tiles. Then you'll be asked to create the tile release, and
for the go on the rest."
- **Rejected:** "Start the whole port"; "Not yet".
- **The question stated the cost** as "4 to 8 million tokens over several days (Phase 1's helpers used about 2.8
  million)", marked as an estimate from Phase 1's rate.
- **Phase 1's helper total, summed from the counts in -7 to -23:** 2,761,254 tokens, about 2.76 million, over ten
  coder parts and two research runs (part 8's figure is its transcript's last context, the harness count having been
  lost in the restart).

**Change, scope, stops and finish line:** `prompts/preserved/2026-10-28-01-brouter-port-amendment-2.md`.
- **Step 0:** the scaffold, with Gradle 9.0.0 and a `macos-26` smoke job added now.
- **Step 1:** the four tiles captured into scratch, each checked in the oracle and pinned. Then the routes to a
  release are reported, and the coder stops.
- **Nothing public is created.** The release and the go on step 1b onward come back to the owner.

**Prediction (outcome, planner):**
- Actions is on, and `ubuntu-24.04` is green within two pushes;
- Kotlin 2.3.10 links and runs the smoke test on `macos-26` with an Xcode warning;
- BRouter 1.7.10 reads all four tiles.

**Who builds:** the team's primary coder (Opus), on its first run, proposing three names.
**Dispatch-file:** prompts/preserved/2026-10-28-01-brouter-port-amendment-2.md
**Notes:** Written by Cedar Datum by hand. The go was answered after a worker restart; the time is the recording
time. Next free ID -26.

---

**Kind:** outcome
**ID:** 2026-10-28-26
**Timestamp:** 2026-10-29T20:16:33Z
**Continues:** 2026-10-28-3, 2026-10-28-25
**Title:** Phase 2, step 0 done: the scaffold builds, and CI is green on Linux and on the Mac runner
(simulator-checked); step 1 handed to a fresh session
**Observed:** the primary coder (Opus, first run, not yet named), on `brouter-port`:
- `d33c2f0`: bases, the planner's FK 6 ruling, and the coder's mechanism prediction, written before building;
- `fea07f8`: step 0;
- `27f4132`: step 0's results and step 1's handoff.

The Phase 2 report, `docs/audits/2026-10-29-brouter-port-phase2.md`, is filed unedited as
`prompts/preserved/2026-10-28-01-phase2-report-step0.md` (SHA-256 equal). The watcher read 181,469 tokens at
20:12:51Z, and the planner told the coder to close step 0 and hand off. The harness's final count was 234,033.
- **The planner's ruling on FK 6, given by message during the run:**
  - item I's step 0 test "one case per tile-free failure kind on both engines (FK 6), failing on the stub" cannot
    pass until step 5a ports the file layer;
  - the coder offered (a) a red job by design, (b) a separate non-blocking task, or (c) the oracle half only;
  - **the planner ruled (c):** each step's port-side tests arrive with the step that ports them. (b) moves a
    written test out of the default run, which Forager's `CLAUDE.md` treats as silencing. (a) leaves a job red
    for five steps, where a real failure cannot be told from the planned one.
- **Step 0:**
  - Gradle 9.0.0, with the wrapper's checksum from Gradle's published one; Kotlin 2.3.10; okio 3.18.2;
  - the `:brouter` module for `jvm` 17, `iosArm64` and `iosSimulatorArm64`, with C's entry point as stubs;
  - `tools/fetch-pinned.sh` and `fetch-oracle.sh`, which refuse the oracle unless size and SHA-256 match;
  - `.github/workflows/brouter.yml`: `jvm` on `ubuntu-24.04`, and `ios-smoke` on `macos-26` with Xcode 26.0.1.
- **CI, run 40946531992 on `fea07f8`, both jobs `success`:**
  - `jvm` (Temurin 21.0.13+9): 3 tests, 0 failures, both iOS klibs compiled;
  - `ios-smoke`: 1 test, 0 failures, linked and run on the iOS simulator. **Simulator-checked.**
  - Run 40947359130 on `27f4132`, docs only, was in progress at recording.
- **Revert check:**
  - one character of the oracle zip's pinned SHA-256 changed: the fetch refused both the cached and a fresh copy,
    and no test ran. Restored from a saved copy, and the forward change confirmed afterwards.
  - A second bite check, on the test's own jar hash, failed 1 of 3 as it should.
  - FK 6's oracle half and the smoke test are "not shown to bite".
- **Predictions (-25), scored:**
  - Actions on: held;
  - `ubuntu-24.04` green within two pushes: held, on the first;
  - Kotlin 2.3.10 links and runs on `macos-26`: held (simulator-checked);
  - **an Xcode warning: did not hold.** No warning appears in the job log, and why is unknown. KGP 2.3.10's
    `maxTested` is 16.0 (-14), so the warning's logic was read, not seen run.
  - BRouter 1.7.10 reads the tiles: not yet scored.
- **The coder's own slip, caught by its test:** it predicted FK 6's message wrongly. The oracle says "datafile
  W75_N40.rd5 not found" (`NodesCache.java:326-332`), and the test now asserts that.
- **Step 1, part done:** the four tiles were downloaded once at 20:09:48–58Z into scratch, sizes equal to
  `Content-Length`, all `Last-Modified` 2026-10-29 01:03:01 GMT. None has been loaded in the oracle yet. Hashes are
  in `step1-handoff/tiles-downloaded.txt`.
- **Calls beyond the amendment:**
  - a root `.gitignore`;
  - `tools/junit-counts.py`;
  - the oracle fetched by a Gradle task;
  - okio as an `api` dependency;
  - `-XX:+UnlockDiagnosticVMOptions` with `-XX:-UseLibmIntrinsic`;
  - **the CI oracle JDK pinned as Temurin 21.0.13+9,** where Phase 1 measured on OpenJDK 21.0.12.1. To be settled
    before step 1b writes any expected value.
- **Found for step 2b:** `macos-26` carries simulator runtimes 26.2, 26.4.1 and 26.5, not 26.0. Which one ran the
  test is not in the log.
- **Checked by the planner:**
  - the tree is clean, and the step's 16 untracked evidence files that a stop hook flagged at 20:13Z were
    committed by the coder in `27f4132`;
  - the run and job conclusions, through the GitHub API;
  - the `ios-smoke` log's JUnit XML: `tests="1" failures="0" errors="0"`, testcase
    `okioSplitsATilePathOnThisTarget[iosSimulatorArm64]`, JDK Temurin 21.0.13+9 aarch64.
- **Three names proposed:** Linden Caliper, Aspen Tally, Rowan Tracewell. Two repeat Forager names: "Aspen" is on
  Forager's Pi team and "Tally" is Sorrel Tally's surname; "Rowan" is Forager's planner.
**Notes:** Written by Cedar Datum by hand. Next free ID -27.

---

**Kind:** outcome
**ID:** 2026-10-28-27
**Timestamp:** 2026-10-29T20:33:36Z
**Continues:** 2026-10-28-3, 2026-10-28-25, 2026-10-28-26
**Title:** Phase 2, step 1 done up to the release: all four tiles load in the 1.7.10 oracle and are pinned; nothing
public created
**Observed:** the primary coder (Opus, second session), on `brouter-port`:
- `4da988d`: the tiles in the oracle;
- `1df847c`: `tools/tile-pins.tsv`, `tools/fetch-tiles.sh`, F0 staged;
- `91754bc`: the ODbL notices in `tools/tile-release/`;
- `4fd1ed9` and `0a9999a`: results and the release routes.

The Phase 2 report is filed unedited as `prompts/preserved/2026-10-28-01-phase2-report-step1.md` (SHA-256 equal). The
watcher read 183,613 tokens at 20:28:38Z, after the last push. The harness's final count was 194,134. Phase 2 so far:
428,167 tokens over two sessions.
- **The tiles (computed):** the scratch bytes were re-hashed against step 0's record first; nothing was downloaded
  again.
  - Each tile, in a folder holding only itself, passed the lookup version (11), the version-checked open and
    `checkFileIntegrity`; every cell decoded; and one `hiking-mountain` route was found.
  - Nodes decoded: `W75_N40` 8,136,499; `W75_N35` 260,055; `W70_N40` 233,163; `E170_S50` 50,074. None was
    replaced.
  - **The planner's prediction "BRouter 1.7.10 reads all four tiles" held.**
- **F0's fixture:** copied read-only at its pinned SHA-256. Run as a control, its route equals Forager's recorded
  1.7.10 route (5,047 m, 226 points).
- **A defect found in step 0's draft harness:** it passed `MicroCache.debug` (false) as `reallyDecode`, so its node
  count could only read zero. It did, on every tile and on F0, which is how it was caught.
- **Bite and revert checks:**
  - four damaged copies of `E170_S50` were each refused for their own reason;
  - item I's revert, `E170_S50`'s pinned size one byte high, was refused by the fetch ("is 1061589 bytes, pinned
    1061590"). Restored from a saved copy.
- **CI:** runs on all five commits `success`. On `0a9999a` (run 40949273886), `jvm` read 3 of 3 and `ios-smoke` 1 of 1.
  Neither job reads the tiles yet, so step 1's evidence is the local logs.
- **The release routes, recommending A:**
  - **A:** a session holding the staged files creates a draft release, uploads the six assets, reads each back
    against the pins, then publishes. One yes and a tag name from the owner; keeps the checked bytes.
  - **B:** recapture and re-pin first, then A.
  - **C:** a CI job downloads, checks and publishes. New bytes, pinned only after.
  - **D:** the owner uploads by browser. Six uploads, and no route carries 114 MiB to the owner.
  - **E:** chunks on a temporary branch, which bends ruling c.

  Read-only checks found admin permission through the session's proxy, 0 releases, and `uploads.github.com`
  reachable. Whether the proxy passes a 114 MiB upload is unverified.
- **Calls beyond the amendment:**
  - the pin file's path and form;
  - F0's asset name `forager-fixture-W75_N40.rd5`;
  - `fetch-tiles.sh`, not yet wired into CI;
  - route endpoints chosen from geography;
  - the check run on the container's OpenJDK 21.0.12.1, not CI's Temurin.
- **Checked by the planner:**
  - the tree is clean at `0a9999a`, and step 1 wrote only `tools/` and the Phase 2 report folder;
  - `tools/tile-pins.tsv` read: the five rows' sizes and SHA-256s equal `sha256sum` of the six staged files in
    scratch, F0 included;
  - the README and release notes in `tools/tile-release/` read in full: OpenStreetMap attribution, the ODbL 1.0 URI
    and the copyright page, and each file's source. The staged README is byte-identical to the committed one.
**Notes:** Written by Cedar Datum by hand. Next free ID -28.

---

**Kind:** decision
**ID:** 2026-10-28-28
**Timestamp:** 2026-10-29T21:17:36Z
**Continues:** 2026-10-28-3, 2026-10-28-25, 2026-10-28-27
**Title:** The tile release goes ahead; Phase 2 continues through the port with built-in stops; the primary coder is
Linden Caliper (owner); Amendment 3
**Decision:** the owner chose, from three questions asked together after step 1:
- **The release:** "Yes, upload these (Recommended)". The option read: "A session creates a draft release under tag
  'brouter-tiles-2026-10-29', uploads the 5 files and the README, and downloads each one back to check it against its
  fingerprint. Only then does it publish. A failed upload leaves nothing public. If the session's network won't pass
  the 114 MB file, it stops and reports."
  - Rejected: "Yes, but recapture first"; "Not yet".
  - This is the owner's word on the public action RECORD -12 left open.
- **The rest of Phase 2:** "Continue, built-in stops (Recommended)". The option read: "Runs in Hazel's order, one
  checked session per step. It stops for you if the Java engine proves unstable on more than 5% of any test set, for
  anything public, on a wrong premise, or after two failed fixes. I'll report at each milestone: the Java engine's own
  runs, then each package, the full JVM run, the iPhone simulator run and the timing."
  - Rejected: "Only the Java runs, then pause"; "Pause after the release".
  - The question gave the cost as "3 to 6 million more tokens over several days", estimated, not measured.
- **The primary coder's name:** "Linden Caliper (Recommended)". Rejected: Aspen Tally, Rowan Tracewell.
  - Profile: `docs/process/coder-profile.md`, added to the team table.

**Dispatch:** `prompts/preserved/2026-10-28-01-brouter-port-amendment-3.md`.
- **Part A:** the release exactly as ruled. Draft, upload, read back against the pins, publish, read back
  anonymously; then the pinned-tile fetch wired into the build.
- **Part B:** steps 1b to 12 in item I's order, changed by -24's rulings:
  - `cos` and `atan2` from bionic's msun, ported to common Kotlin;
  - `hmap` (i);
  - via points left out;
  - flags at 25%.
- **Milestone reports** at 1b, each package, 9, 10, 11 and 12.

**The planner's calls:**
- the tag targets `91754bc`. Checked at 21:17Z: the pin file and the notices are unchanged from there to
  `0a9999a`, and the six staged files still match their pins after the worker restart.
- the oracle's JDK is CI's Temurin 21.0.13+9, after step 1b checks its no-intrinsic `cos` table against Phase 1's
  OpenJDK 21.0.12.1 measurement. A difference is a stop.
**Notes:** Written by Cedar Datum by hand. The answers came after a worker restart; the time is the recording time.
Next free ID -29.

---

**Kind:** outcome
**ID:** 2026-10-28-29
**Timestamp:** 2026-10-29T21:26:14Z
**Continues:** 2026-10-28-3, 2026-10-28-28
**Title:** Amendment 3 Part A stopped at step 2: the session's proxy refuses to create releases; nothing public exists
**Observed:** Linden Caliper (Opus), on `brouter-port` at `6e56d2f` and `469b33b`. The Phase 2 report is filed unedited as
`prompts/preserved/2026-10-28-01-phase2-report-partA.md` (SHA-256 equal). The harness counted 125,723 tokens at its
first stop.
- **Step 1:** the six staged files equal their pins, and the README equals `91754bc`'s.
- **The route used:** the session's agent proxy, with the container's placeholder token. GitHub authenticated it as
  `slayer8366`, admin. No token was made.
- **Step 2:**
  - Attempt 1: `POST /releases` drew a 415 from the proxy, for a missing `Content-Type`.
  - Attempt 2, with the header, drew a 403 from the proxy: **"Creating, editing, or deleting releases is not
    permitted for this session type."**
- **Afterwards:** 0 releases, drafts included, and 0 tags. Steps 3 to 7 were not run.
- **Checked by the planner:**
  - the release list through the GitHub tools is empty;
  - the report's account of both attempts was read at `469b33b`;
  - the part's commits touch only the Phase 2 report and its folder.
- **A wrong premise, the planner's:** step 1's read-only checks (admin permission, `uploads.github.com` reachable) were
  taken as pricing route A, and the planner offered it to the owner as feasible. A write policy at the proxy was
  invisible to those checks. The owner's "Yes, upload these" was given on that premise, so a new route goes back to
  the owner.
- **Read at source for the new routes:** GitHub's "Events that trigger workflows", `workflow_dispatch`, fetched
  2026-10-29 through `docs.github.com/api/article/body`. "This event will only trigger a workflow run if the workflow
  file exists on the default branch"; the "Run workflow" button needs it there too.

**The planner's call (technical):** step 2a starts now. Item I lets steps 2 to 4 run while the release waits (line
1692), and they need no tile. Step 1b and steps 5 on wait for the release.
**Notes:** Written by Cedar Datum by hand. Next free ID -30.

---

**Kind:** decision
**ID:** 2026-10-28-30
**Timestamp:** 2026-10-29T22:00:52Z
**Continues:** 2026-10-28-3, 2026-10-28-28, 2026-10-28-29
**Title:** The tile release is made by a workflow the owner runs (owner); Amendment 4
**Decision:** asked how the release should be made after the session's proxy refused (-29), the owner chose "A button
for you (Recommended)". The option read: "I add one small workflow file to main. Main changes only on your word, and
choosing this is that word, for this one file. You open Forager-iOS's Actions page on GitHub and press 'Run workflow'.
It downloads fresh copies from brouter.de, checks each in BRouter 1.7.10 the same way Linden did, and publishes the
release with the attribution only if all pass. A session then pins the published files. These are new files, not
today's. Not yet known: whether the repository lets workflows create releases; the first run shows it."
- **Rejected:**
  - "Let a push trigger it", which routes around this session type's refusal to create releases;
  - "Hold the release".
- **The owner's word on `main` covers one file,** `.github/workflows/publish-tiles.yml`, copied unchanged from
  `brouter-port` after the planner reads it.
- **Linden's checked copies of 20:09Z are superseded.** The release will hold the workflow's capture. Their pins on
  `brouter-port` are replaced by the release's.
- **Linden's final Part A message** was read back from its log after the container restart. It agrees with -29: no
  retry, no other route tried, the tree clean at `469b33b`.
- **Correction to -29,** which said step 2a "starts now": it did not start. The container restarted, and the release
  question came first. Step 2a follows Amendment 4's workflow session.

**Dispatch-file:** prompts/preserved/2026-10-28-01-brouter-port-amendment-4.md
**Notes:** Written by Cedar Datum by hand. Next free ID -31.

---

**Kind:** outcome
**ID:** 2026-10-28-31
**Timestamp:** 2026-10-29T22:16:16Z
**Continues:** 2026-10-28-3, 2026-10-28-30
**Title:** The tile release workflow is written and dry-run; the planner copies it to `main` on the owner's word
**Observed:** Linden Caliper (Opus), on `brouter-port`:
- `aa144a0` and `5d00761`: the tools, with the check moved to `tools/tile-check/` and failing on any failed check;
- `d54b95c`: `.github/workflows/publish-tiles.yml`;
- `4d667f4`: the planted failure;
- `41be45e`: the restore;
- `f57b003`: the report.

The Phase 2 report is filed unedited as `prompts/preserved/2026-10-28-01-phase2-report-amendment4.md` (SHA-256 equal).
The harness's final count was 194,712, after the planner's 180k message at 22:14Z.
- **The workflow has two jobs:**
  - **`capture`,** with `contents: read`, runs the tools from `brouter-port` at `TOOLS_SHA` `5d00761`;
  - **`publish`,** with `contents: write`, runs only when
    `success() && github.event_name == 'workflow_dispatch' && github.ref == 'refs/heads/main'`. It creates a draft,
    reads all seven assets back against the pins, then publishes.
  - The push trigger is limited to `brouter-port`.
- **Dry runs, all with `publish` skipped:**
  - **40963971506 on `d54b95c`: green.** F0 was fetched anonymously from `raw.githubusercontent.com`, and the four
    tiles and the F0 control each read `PASS` on Temurin 21.0.13+9.
  - **40964144495 on `4d667f4`: refused,** naming the planted character: "has SHA-256 ff6d…5990, pinned ff6d…5991".
  - **40964254673 on `41be45e`: green.** The file was restored from a saved copy and equals `d54b95c`'s.
- **The coder's prediction "new bytes" failed:** brouter.de still served the 20:09Z bytes at 22:12Z.
- **Calls beyond the amendment:**
  - the README and notes made into templates the run fills;
  - `TileCheck` exiting 1 on failure, shown to bite;
  - a stale-tools check on push;
  - the tag check also refusing a draft;
  - `concurrency` on `publish`.
- **Never run:** the `publish` job, so whether the repository lets workflows create releases is unknown. No linter ran:
  downloading one was refused by the session's permission check, and each `run:` block passed `bash -n` instead.
- **Checked by the planner, before the copy:**
  - **read in full:** the workflow at `f57b003` (SHA-256 `69576e41…f0a677`, unchanged since `d54b95c`), and every file
    it runs at `5d00761`: `tools/tile-check/capture.sh`, `tools/fetch-tiles.sh`, `tiles.tsv`, and the README and notes
    templates that go public. Nothing in them writes to GitHub except `publish`'s `gh` calls.
  - **the four action pins match their release tags** by `git ls-remote`:
    - checkout v7.0.1 `3d3c42e5…`;
    - setup-java v6.1.0 `b843ebf9…`;
    - upload-artifact v7.0.2 `cf430e03…`;
    - download-artifact v8.0.2 `9000827c…`.
  - the stop hook's three untracked `amendment4/` logs were committed by Linden in `f57b003`; the tree is clean.
- **The copy:** `.github/workflows/publish-tiles.yml` is copied byte for byte to `main` in this commit (SHA-256
  `69576e41…f0a677`), on the owner's word at -30. It is the only code on `main`.
**Notes:** Written by Cedar Datum by hand. The owner's run comes next; then a session pins the release, and step 2a
starts. Next free ID -32.

---

**Kind:** outcome
**ID:** 2026-10-28-32
**Timestamp:** 2026-10-29T22:30:59Z
**Continues:** 2026-10-28-3, 2026-10-28-28, 2026-10-28-31
**Title:** Phase 2, step 2a, sub-step 1: the `util` stubs and 22 tests pushed and seen failing; the port goes to the next
session
**Observed:** Linden Caliper (Opus), on `brouter-port`:
- `3cd61ad`: 12 `internal` stubs, 8 `jvmTest` classes, 2 `commonTest` files, and the step's section with its
  prediction;
- `41cc4ca`: the handoff.

The watcher read 182,359 tokens at 22:29:03Z with nothing pushed, and the planner asked for the tests-first push and a
handoff. The harness's final count was 195,246.
- **Failing first (computed, local):**
  - 0 compile errors; the JUnit XML reads 25 tests, 21 failures;
  - every failure is `NotImplementedError` from its own stub;
  - the 3 earlier tests pass;
  - one new test, a check of the common tests' expected values against the JDK, passes by design and is listed
    "not shown to bite".
- **The tests cover:**
  - the coders both ways, byte for byte;
  - `SortedHeap`'s exact pop order on tie-heavy seeds;
  - the maps on seeded keys;
  - `Crc32`;
  - `Math.round` on about 3.07 million inputs;
  - `isWhitespace` on all 65,536 chars;
  - `split` with the 14 regexes the reached code passes.
- **CI on `3cd61ad` and `41cc4ca`** (runs 40966293309 and 40966347514) were in progress at recording. Both are
  expected red over the stubs, `jvm` with 21 failures and `ios-smoke` with 3. Within a step, item I's method pushes the
  tests before the port, so the branch is red until the port's push. That push is the next session's first job.
- **Calls beyond the amendment:**
  - classes `internal`;
  - `moveToFrozenArrays` `internal`;
  - `ByteArrayUnifier.unify` as a named "unsupported";
  - `commonTest` cases for the helpers, so the simulator checks them too.
- **A reach question for step 5b:** `OsmNode.java:134` calls `readUnified`, and through it `unify`. Neither is on the
  port list. Whether that line runs on Forager's tiles could not be determined.
- **Checked by the planner:**
  - the two commits touch only `brouter/src/` and the Phase 2 report's folder;
  - the tree is clean;
  - the runs exist on both commits.
**Notes:** Written by Cedar Datum by hand. Next free ID -33.

---

**Kind:** outcome
**ID:** 2026-10-28-33
**Timestamp:** 2026-10-29T22:56:53Z
**Continues:** 2026-10-28-3, 2026-10-28-32
**Title:** Phase 2, step 2a: `util` ported, 24 of 25 tests green; one test from sub-step 1 can never finish; the
planner's ruling
**Observed:** Linden Caliper (Opus), on `brouter-port`:
- `612bb0d`: the port, 10 stub files filled, with logs, scratch harnesses and the run scripts;
- `87e7fc9`: the revert check.

The watcher read 185,584 tokens at 22:55:08Z. The harness's final count was 200,380.
- **The port (computed, local):** 0 compile errors; 24 tests, 0 failures, for the 24 that can finish. `util/` is
  1,282 Kotlin lines in 12 files, 992 of them code.
- **The 25th test, `BitCoderContextTest.rawBitReadsOnRandomBytesMatchTheOracle` (sub-step 1's, `3cd61ad`), can never
  end:**
  - its loop ends only at bit 32,640, but about one op in 250 resets both contexts;
  - on the oracle alone, three seeds ran 20 million ops each and reached bits 26,863, 25,224 and 23,290
    (`2-loop/LoopCheck-out.txt`, read by the planner);
  - over the stubs it could not show this, since every stub threw on its first call. This is the family in Forager's
    `CLAUDE.md`: "A check that passes because it never saw the data that could fail it".
  - The same comparison, capped at 1 million ops per seed in a scratch harness, ran 20 million ops over 20 seeds
    with 0 mismatches. It bites: one flipped bit gave 56,167 mismatches.
  - The coder stopped rather than change the test. This is Amendment 3's wrong-premise stop.
- **Revert check:** `SortedHeap.kt:112` (`SortedHeap.java:139`), `<` to `<=`. Of 24 tests, exactly the four
  `SortedHeapTest` cases failed, each on its pop sequence. The prediction held. Restored from a saved copy.
- **CI, runs 40969246695 and 40969414415:** not read to the end. The `jvm` job is expected to hit its 30-minute
  timeout on the 25th test. That would leave the iOS klib step unrun and the job red until the test is fixed.
- **Wrong premises:** the handoff's "25 tests, 0 failures" finish line; `getMinBin` has 31 unrolled steps, not 30.
- **Checked by the planner:**
  - the commits touch only `util/` and the Phase 2 report's folder;
  - the tree is clean at `87e7fc9`;
  - the loop-check log reads as reported.
- **The planner's slip:** while sending a handoff message, the planner sent the word "placeholder" to the finished
  sub-step 1 session (`aa35ff27`), which resumed. It read the message, did nothing, and said so. Told it was a
  mistake, it replied "No action taken". Nothing was written or pushed by it.

**The planner's ruling on the 25th test (a test-harness detail):** the coder's option (a), plus one case.
- **The loop gets an op cap of 1,000,000 per seed.** The position bound and both equalities stay.
- **A second case per seed, with no in-loop reset, runs to the position bound.** This reaches the end of the byte array
  and `fillBuffer`'s end-of-array branch, which no version of the first case reaches.
- **Each new loop's termination is checked on the oracle alone before the push,** with its op count logged.
- **The fixed test must be seen to bite,** by the one-flipped-bit check run on the port.
- **Why this is not a weakened test:** the test never finished, so it never asserted anything. Capping it makes it
  assert on 1 million ops per seed, and the second case adds coverage it never had.
**Notes:** Written by Cedar Datum by hand. Next free ID -34.

---

**Kind:** outcome
**ID:** 2026-10-28-34
**Timestamp:** 2026-10-29T23:10:55Z
**Continues:** 2026-10-28-3, 2026-10-28-33
**Title:** Phase 2, step 2a closed: `util` green, 26 tests on the JVM, iOS klibs compiled, simulator 4 of 4; the
end-of-array branch is left to step 5b
**Observed:** Linden Caliper (Opus), on `brouter-port`:
- `d5371c7`: the ruling applied;
- `316ac1f` and `9ce54ee`: the bite runs;
- `5676f99` and `c225648`: the report and CI.

The watcher read 185,285 tokens at 23:08:51Z. The harness's count at the report was 199,537.
- **CI, run 40970686102 on `9ce54ee`: success.**
  - `jvm`: 26 tests, 0 failures; both iOS klibs compiled, with no `e:` lines.
  - `ios-smoke`: 4 tests, 0 failures. **Simulator-checked.**
  - The planner read the run's conclusion through the GitHub API.
- **The two cases:**
  - **Case 1,** capped at 1,000,000 ops per seed with resets kept: 20 seeds end at the cap.
  - **Case 2,** no reset, to the position bound: 20 seeds end after 3,976 to 4,250 ops each.
  - The test's printed counts equal the oracle-alone counts on all 40 seed-cases.
- **Each case bites on its own flip:** bit 0 of byte 100 failed only case 1, at op 206, and only case 2, at op 108.
  Each op is the first read covering that bit, worked out on the oracle before the run.
- **The coder's own wrong prediction** (op 1,775 for run A) came from a committed log missing its first three lines.
  The harness was re-run and the log replaced.
- **The planner's ruling (-33) rested on a wrong premise.** Case 2 does not reach `fillBuffer`'s end-of-array branch:
  - `idx` peaks at 4,079 to 4,084, below `idxMax` 4,095;
  - past the bound, Java's unguarded `ab[++idx]` throws within 15 ops;
  - a control without the bound reaches the branch on 19 of 20 seeds, so the detector works.

**The planner's ruling on that branch (technical):** option (i). Case 2 stays as built.
- Making the port reproduce Java's `ArrayIndexOutOfBoundsException` (option (ii)) would test a failure Forager never
  relies on.
- Whether production reaches the branch is a question for real tiles. **Step 5b decodes every micro-cache of each
  captured tile through both engines.** It is to report whether `fillBuffer`'s end-of-array branch runs there, or
  record it as unreached.
- **Step 2a is closed.** The two earlier runs, 40969246695 and 40969414415, were left to their 30-minute timeout and
  were not read.
**Notes:** Written by Cedar Datum by hand. Next: step 2b. Next free ID -35.

---

**Kind:** outcome
**ID:** 2026-10-28-35
**Timestamp:** 2026-10-29T23:30:35Z
**Continues:** 2026-10-28-3, 2026-10-28-24, 2026-10-28-34
**Title:** Phase 2, step 2b, first part: Android's `cos` and `atan2` ported to common Kotlin, bit for bit with bionic's C,
on the JVM and on the simulator
**Observed:** Linden Caliper (Opus), on `brouter-port`:
- `4bc9e7b`: tests first;
- `6c3de2d`: the port;
- `e7dafe5`: the revert check;
- `b7543f3`: the report.

The watcher read 180,358 tokens at 23:25:49Z. The harness's final count was 211,307. This is the first session to fit
tests-first and the port in one session.
- **Failing first:** run 40972738575 failed as intended. `jvm` read 37 tests, 8 failures; `ios-smoke` read 10 tests, 5
  failures. Every failure was a stub's `NotImplementedError` or the refusal test's type check.
- **The port:**
  - `cos` from `s_cos.c` (with `e_rem_pio2.c` inline), `k_cos.c` and `k_sin.c`; `atan2` from `e_atan2.c` and
    `s_atan.c`;
  - in `brouter/.../msun/`, from bionic `06356e41`, all ten sources matching the research's SHA-256s, with Sun's
    notice kept;
  - finite |x| >= 1,647,099.0 throws a named `MsunUnsupportedException`.
- **CI, run 40973074241 on `6c3de2d`: success** (the planner read the conclusion through the GitHub API).
  - `jvm`: 37 tests, 0 failures; iOS klibs compiled.
  - `ios-smoke`: 10 tests, 0 failures. **Simulator-checked,** with the msun comparison over all 1,800 table rows and the
    299,974 `atan2` pairs.
- **The reference:** bionic's C, built in the `ubuntu-24.04` job on every run, with clang and gcc.
  - It is refused unless the two compilers agree and both outputs equal the research's SHA-256s.
  - Against `StrictMath`: `atan2` differs at 0 pairs; `cos` differs at exactly the research's 13 rows.
- **Revert check:** `k_cos`'s `C1` raised by one ulp. Predicted from the same edit in bionic's C: rows 502, 1297 and
  1318, and exactly 3 tests failing. **The result was exactly that.** Restored from a saved copy.
- **Calls beyond the amendment:**
  - NaN results compared as NaN, not by payload;
  - the package `…brouter.msun`;
  - **a new CI dependency on `android.googlesource.com` on every run.** An outage there fails `jvm`. The planner
    accepts it for now and will revisit it if it flakes.
- **Checked by the planner:**
  - the commits touch only `brouter/`, `tools/msun-ref/` and the report's folder;
  - the tree is clean at `b7543f3`;
  - **no mirror in the build:** a grep of the Gradle files, `gradle.properties` and `brouter.yml` finds no mirror or
    `googleapis` entry. The mirror script exists only as evidence.
  - `brouter/build.gradle.kts:22-33` registers the C reference task.
**Notes:** Written by Cedar Datum by hand. Next: step 2b's second part. Next free ID -36.

---

**Kind:** outcome
**ID:** 2026-10-28-36
**Timestamp:** 2026-10-29T23:49:45Z
**Continues:** 2026-10-28-3, 2026-10-28-35
**Title:** Milestone: the `util` package is complete (steps 2a and 2b), green on the JVM and on the simulator; one CI step
found masked
**Observed:** Linden Caliper (Opus), on `brouter-port`:
- `35a1fb6`: tests first;
- `073851b`: the port;
- `c4e2eac`: the revert check;
- `bc7c1b8` and `c8a32e6`: the report.

The watcher read 183,227 tokens at 23:43:54Z, and the planner asked for a wrap-up. The session went on to the revert check
and the simulator job, and **the harness's final count was 254,582, an overrun.**
- **CI, run 40975094092 on `073851b`: success** (conclusion read by the planner).
  - `jvm`: 54 tests, 0 failures; iOS klibs compiled.
  - `ios-smoke`: 18 tests, 0 failures. **Simulator-checked.**
- **Failing first:** locally, 13 of 17 new tests failed on the stubs. The four that passed do not call the port. The CI
  run on the tests-first commit lost `ios-smoke` to a Maven Central 429, so **the simulator has no failing-first
  evidence.** Its one-ulp positive control in `commonTest` ran there green.
- **Results:**
  - **Logic run (the oracle's table through the seam):** bit for bit on the table, 200,000 distances, 200,000 angle
    cases with all five branches, and 2,399,792 `getAngle`/`getDirection` evaluations.
  - **Production run (msun's table):** `cos` and `kx` differ from the oracle by one ulp at the 13 known rows, and `ky` at
    none; `getDirection` differs at 0 of 1,199,896.
  - The `jvmTest` JVM is shown to run `-XX:-UseLibmIntrinsic`, and the check of that can fail (60 rows differ without
    the flag).
- **Revert check:** row 1234's `cos` one ulp up. 4 failures against 3 predicted. The extra one is a test that also
  compares every chunk, so the failure can come from this edit. JVM only.
- **The seam:** `LibmSeam` is a parameter of every `CheapRuler` and `CheapAngleMeter` call. Production passes
  `MsunLibm`; the logic run passes the oracle's own `SCALE_CACHE` and `Math.atan2`.
- **Found masked:** the simulator job's "recorded, not gating" step was meant to run the arm64 HotSpot half of H step
  4. **It never ran its tests:** its `jvmTest` depends on the C reference, which does not compile against the macOS SDK.
  Both it and its counts step are `continue-on-error` (`brouter.yml:68-79`, read by the planner), so the job read
  green. This is the family in Forager's `CLAUDE.md` of a check that never saw its data.

**The planner's ruling (technical):** the next session fixes this before step 3's port.
- The step runs its two classes with `-x msunReference`, as the coder proposed.
- Differences there stay recorded, not gating, but **a run with no tests, or no XML, fails the job visibly.**
  `continue-on-error` may not mask a step that did not run.
**Milestone:** the `util` package (steps 2a and 2b) is done. The planner reports it to the owner.
**Notes:** Written by Cedar Datum by hand. Next: the arm64 step's fix, then step 3 (`codec`). Next free ID -37.

---

**Kind:** outcome
**ID:** 2026-10-28-37
**Timestamp:** 2026-10-30T00:03:18Z
**Continues:** 2026-10-28-3, 2026-10-28-36
**Title:** The masked step's fix works as a gate, and shows the arm64 JVM tests still never run; step 3 pushed unfinished
**Observed:** Linden Caliper (Opus), on `brouter-port`:
- `4dd46db`: the fix;
- `efb99ed`: the planted failure;
- `106e868`: the restore;
- `9fe7a70`: step 3 work in progress, never compiled or run, pushed on the planner's word.

The watcher read 182,695 tokens at 00:01:01Z. The harness's final count was 195,913.
- **The fix:**
  - the step runs `CheapLogicRunTest` and `CheapProductionRecordTest` with `-x msunReference`;
  - a new gating step runs `tools/junit-ran.py`, which fails the job if any named class left no XML or ran no
    test. Gradle's `--tests` passes when only one of several filters matches, so the old counts could not catch a
    misnamed class.
  - Locally on x86_64: 7 tests, 0 failures. With one misnamed class, Gradle exited 0 and the check exited 1.
- **CI, read by the planner:** runs 40976352957 (`4dd46db`), 40976367336 (`efb99ed`) and 40976378547 (`106e868`) are all
  `failure`. In each, `jvm` succeeded and `ios-smoke` failed at the gating step.
- **The `ios-smoke` log of 40976378547:**
  - the arm64 step's only XML is Gradle's "failed to execute tests": "Process 'Gradle Test Executor 1' finished with
    non-zero exit value 1";
  - the gate printed "FAIL: 2 of 2 classes did not run".
  - **The gate is doing its job:** the step that read green at -36 is now red for the right reason.
- **The likely cause (inferred, not yet seen):** the test JVM is started with `-XX:-UseLibmIntrinsic`, which exists
  only on x86 (-21, read at source in `globals_x86.hpp`). An aarch64 HotSpot would refuse it and not start. The
  executor's stderr is not in the log.
- **The branch is red on `ios-smoke` from `4dd46db` on,** until this is fixed.
- **Step 3 (`codec`), started:**
  - the `-ro` jar does carry the encoders (javap), so no test moves to step 5;
  - the stubs and tests are written but never compiled.
- **Checked by the planner:** the run conclusions, the `ios-smoke` job's steps, and the gating step's log, all through
  the GitHub API.

**The planner's call (technical):** the next session sees the arm64 failure's real cause before fixing it.
- For example, a CI step that prints the test JVM's start-up error on `macos-26`. Forager's rule: see the failure
  before writing the fix.
- **The fix keeps the oracle's x86_64 setting exactly as it is.** On aarch64 it uses that platform's own switch
  (`-XX:DisableIntrinsic=_dcos`, -21) or none, and says which and why.
- Then step 3 continues from `9fe7a70`.
**Notes:** Written by Cedar Datum by hand. Next free ID -38.

---

**Kind:** outcome
**ID:** 2026-10-28-38
**Timestamp:** 2026-10-30T00:24:01Z
**Continues:** 2026-10-28-3, 2026-10-28-37
**Title:** The arm64 test JVM fixed, its cause seen first; step 3 (`codec`) ported, 62 tests green; the planner's rulings
**Observed:** Linden Caliper (Opus), on `brouter-port`:
- `f034bb0`: a probe;
- `b3182d8`: step 3 compiled and seen failing;
- `4436711`: the port;
- `a0b04e1`: the arm64 fix;
- `d99cc46`: logs and the revert.

The watcher read 183,441 tokens at 00:19:34Z. The harness's final count was 190,155.
- **Task 1, the cause seen before the fix.** On `macos-26` (`os.arch aarch64`, Temurin 21.0.13+9),
  `java -XX:+UnlockDiagnosticVMOptions -XX:-UseLibmIntrinsic -version` printed "Unrecognized VM option
  'UseLibmIntrinsic'" and "Error: Could not create the Java Virtual Machine." This matched -37's inference.
- **The probe's measurement on aarch64,** over 200,000 inputs: `Math.cos` differs from `StrictMath.cos` at 11,628 by
  default and at 0 under `-XX:DisableIntrinsic=_dcos`, with and without the JIT; `atan2` differs at 0.
- **The fix:** `brouter/build.gradle.kts` picks the flags by `os.arch`.
  - x86_64 keeps `-XX:-UseLibmIntrinsic` exactly.
  - aarch64 uses `-XX:DisableIntrinsic=_dcos`.
  - Any other architecture fails the build rather than guessing.
- **CI, read by the planner:** runs 40978719644 (`a0b04e1`) and 40978865021 (`d99cc46`) are `success` in both jobs, the
  gate included. In 40978865021's `ios-smoke` log:
  - the simulator ran 18 tests, 0 failures (**simulator-checked**);
  - the arm64 JVM step ran **7 tests, 1 failure** (recorded, not gating), and the gate printed "OK: all 2 classes ran".
  - **The one failure is `theJvmRunsWithLibmIntrinsicsOff`,** which looks for the x86 flag in the JVM's arguments. It
    is the test's assumption, not a maths difference.
  - **The other 6 pass on arm64 HotSpot,** among them `productionTableDiffersFromTheOraclesOnlyAtTheThirteenRows`.
    So arm64 HotSpot under `_dcos` builds the same table as x86_64 without the intrinsic. That answers Phase 1 part 6's
    open question for this setting.
- **Step 3:**
  - **the one compile error** was a stub's JVM name clash (`size` against `getSize()`), fixed with `@JvmField`;
  - **failing first:** 8 of 8 tests seen failing on stub `TODO`s;
  - **the port:** the decoder halves of `DataBuffers`, `StatCoderContext`, `NoisyDiffCoder`, `TagValueCoder` and
    `MicroCache`;
  - **CI on `4436711`:** `jvm` 62 tests, 0 failures, with the 5 codec classes at 8 of 8.
- **Two test expectations corrected, beyond "compile errors only":**
  - **(1) `StatCoderContextTest`'s generator** drew predictors up to 2^28. The oracle's own encode/decode round-trips
    only up to 24 noisy bits, misdecodes beyond, and leaves the stream misaligned. Predictors are now kept under 2^25;
    the round-trip assertion is unchanged.
  - **(2) `TagValueCoderTest`** expected the validator to be called on the empty set. The oracle does not call it there
    either. The check now applies when the set is non-empty.
- **Revert:**
  - `btools/codec` has no sign-extension site, so the revert was made in `BitCoderContext.fillBuffer`'s byte read
    (`util`), which the decoders read through;
  - 3 of 8 failed as predicted;
  - **`DataBuffers`' and `MicroCache`'s tests did not fail,** so no revert has shown they bite.
- **Checked by the planner:**
  - the commits touch only `brouter/`, `tools/jvm-probe/`, the workflow and the report's folder;
  - the tree is clean;
  - the run conclusions and the `ios-smoke` log lines quoted above.

**The planner's rulings (technical):**
- **Corrections (1) and (2) stand.** In both, the oracle itself fails the old expectation, and the port equalled the
  oracle at every comparison. The bar is port equals oracle.
  - **For (1), one condition:** when steps 5a and 5b decode the captured tiles, they log the largest noisy-bit count
    the decoders meet. If any exceeds the generator's range, that is a finding, and the test widens.
- **Two more revert checks inside `codec`,** each predicted first: one in a decoder (for example `StatCoderContext`),
  and one in `MicroCache`, so that `MicroCacheTest` is shown to bite. `DataBuffers` is allocation only; say whether a
  revert can bite there at all.
- **`theJvmRunsWithLibmIntrinsicsOff` checks the flag that belongs to the architecture it runs on,** so the arm64 step
  records no false failure.
**Notes:** Written by Cedar Datum by hand. Next: those three items, then step 4 (`expressions`). Next free ID -39.

---

**Kind:** outcome
**ID:** 2026-10-28-39
**Timestamp:** 2026-10-30T00:36:28Z
**Continues:** 2026-10-28-3, 2026-10-28-38
**Title:** Step 3's reverts all bite, as predicted; the arm64 step reads 7 of 7; step 4 started; the planner's rulings
**Observed:** Linden Caliper (Opus), on `brouter-port`:
- `2215741`: the runner and three predictions, committed before any run;
- `db53413`: a runner fix;
- `0f4fe0b`: the results;
- `7bf6a3d`: the arch-aware flag test;
- `599dd40`: step 4 work in progress, not compiled and not run.

The watcher read 183,144 tokens at 00:34:11Z. The harness's final count was 205,613.
- **The three reverts,** each 1 of 8 failing exactly as predicted, each restored from a saved copy:
  - **a decoder:** `StatCoderContext.kt:72` (Java `:250`) dropping the right branch's bit. It failed at seed 1, op 3,
    kind 3.
  - **`MicroCache`:** `MicroCache.kt:57` (Java `:103`), `<=` to `<`. It failed at "n 2 op 0: getAndClear(247) expected
    true but was false", so `MicroCacheTest` bites.
  - **`DataBuffers`:** `bctx1` given its own array. It failed `DataBuffersTest` only, since `TagValueCoder` repoints
    `bctx1` before writing.
- **The flag test is arch-aware.** CI run 40980103184 on `7bf6a3d` is `success` in both jobs. Its `ios-smoke` log (read by
  the planner):
  - the simulator ran 18 of 18 (**simulator-checked**);
  - the arm64 JVM step ran **7 tests, 0 failures**, with `theJvmRunsWithLibmIntrinsicsOff` PASSED and the gate "OK: all
    2 classes ran".
  - **-38's three items are closed.**
- **Step 4 (`expressions`), started:**
  - **26 profiles, not 27:** E's crossing profile does not exist yet.
  - **The pinned `lookups.dat` has no `*` value,** so the block holding all nine `String.format` calls
    (`BExpressionContext.java:576-693`) cannot run on it.
  - Three small data classes were ported ahead of their tests, a departure from tests-first that the coder disclosed.
- **Not kept up:** the Phase 2 report has no step 3 section and no current status line.

**The planner's rulings (technical):**
- **`BExpressionContextWay.setDecodeForbidden`** is ported as a member of the way context. T2's port list keeps it, and
  "Any profile" (-8) keeps T2.
- **`showErrors` and `disableExpressionCache`** become constants at Java's default, `false`. They are read with
  `Boolean.getBoolean` (`BExpressionContext.java:85`, `:118`), and a grep of Forager's app finds no
  `System.setProperty`, so Forager runs with both `false`.
- **The asterisk block becomes the named "unsupported" unit,** tested with a made-up lookups file that has a `*` value.
- **The next session first brings the Phase 2 report up to date:** a step 3 section and a current status line.
**Notes:** Written by Cedar Datum by hand. Next: step 4 continued. Next free ID -40.

---

**Kind:** outcome
**ID:** 2026-10-28-40
**Timestamp:** 2026-10-30T00:48:28Z
**Continues:** 2026-10-28-3, 2026-10-28-39
**Title:** Step 4 in progress: the report brought up to date, port drafts written outside the build; the asterisk
ruling corrected
**Observed:** Linden Caliper (Opus), on `brouter-port`:
- `c90f440`: the Phase 2 report's step 3 section and status line, as -39 asked;
- `18e8386`: step 4 work in progress.

The watcher read 181,440 tokens at 00:46:38Z. The harness's final count was 198,123.
- **Step 4's state:**
  - the mechanism prediction, written before any code;
  - drafts of `BExpressionContext` (split into three files of 379, 113 and 166 lines) and `BExpression` (365), kept in the
    evidence folder, outside `commonMain`, so the tests can still be seen failing on stubs.
  - **Never compiled.** Not yet written: the stubs, `BExpressionMetaData`, the two contexts, the text helpers, and every
    test.
  - The module under `brouter/` is unchanged since `599dd40` and does not compile.
- **The report's status line gives wrong draft sizes** (346, 117, 175, 338; `wc -l` reads 379, 113, 166, 365). The coder
  found this after its last push; the next session corrects it.
- **A finding that corrects -39's reasoning,** checked by the planner at source:
  - `addLookupValue(String, String, int[])` (`BExpressionContext.java:540`) has three callers, all in the same class
    (`:299`, `:300`, `:304`), and all pass `null` for the array;
  - a grep of all of `btools` finds no other caller;
  - the asterisk block sits under `if (lookupData2 != null)` (`:572`).
  - **So no lookups file reaches it through Forager's calls.** The pinned file's lack of a `*` value is true but is not
    the reason. T2 lists the method, not the block, so the block came in with the method.

**The planner's ruling, corrected (technical):**
- **The block stays a named "unsupported",** now because it is unreachable through Forager's calls.
- **Its test calls `addLookupValue(name, value, int[])` directly,** with a non-null array, on both engines: the oracle
  records the value, and the port throws the named "unsupported". A made-up lookups file is not needed.
**Notes:** Written by Cedar Datum by hand. Next: step 4 continued, from `18e8386`. Next free ID -41.

---

**Kind:** outcome
**ID:** 2026-10-28-41
**Timestamp:** 2026-10-30T01:03:52Z
**Continues:** 2026-10-28-3, 2026-10-28-40
**Title:** Step 4: Java's text helpers ported, stubs compile, and step 4's six tests seen failing; the port is next
**Observed:** Linden Caliper (Opus), on `brouter-port`:
- `090d2e5`: the report's sizes corrected;
- `c350bda`: `util/JavaText.kt`, tests first;
- `0c17b17`: the stubs;
- `b70987e`: a CI change;
- `35dea70`: step 4's tests and evidence, with the test list in the report.

The watcher read 180,254 tokens at 01:02:11Z. The harness's final count was 192,868.
- **The test list had lived only in a message.** The twelfth session's items 5 and 6 were not in the report. The planner
  sent them verbatim, with -40's correction, at the coder's request, and they are now in the report's step 4 section.
- **`JavaText`:** 6 of 7 failed on the stubs, each on its own helper, then 7 of 7 passed. Its common cases also run on
  the simulator. No revert yet.
- **The stubs:** the module compiles; locally 69 tests, 0 failures.
- **Step 4's tests,** with a prediction written first:
  - locally 75 tests, 6 failures, the six step 4 tests, all on the stub `setForeignContext`;
  - the 26-profile tests read "26 of 26 … setForeignContext=26";
  - the oracle threw `IllegalArgumentException` on 17 of the 26 malformed texts.
  - **Limit, disclosed:** every test fails in the shared setup, not at the member it exists for. Which tests bite where
    is shown only once the port lands.
- **The CI change (`b70987e`):** the `jvm` job's iOS klibs step runs after a red JVM step too (`if: !cancelled()`), so
  a tests-first commit still shows whether iOS compiles. The job still fails when the JVM step does.
- **CI read by the planner:**
  - `c350bda` and `b70987e`: `success`;
  - `090d2e5`: `failure`, as expected for a docs-only commit on `18e8386`'s uncompiled tree;
  - `35dea70`: in progress at recording, expected red on the stubs.
- **Calls beyond the amendment:**
  - a placeholder `BExpressionContext` so `JavaText` could compile;
  - `readMetaData(FileSystem, Path)`;
  - one extra test, for an unknown value without an asterisk;
  - the asterisk test first adds a `*` value through Java's own `addLookupValue(…, null)`;
  - the port's message "unsupported: addLookupValue's asterisk block".
- **Unverified:**
  - every CI count;
  - the iOS klibs locally (Maven Central 429);
  - `parseFile`'s `RuntimeException` branch;
  - text decoding, okio against Java's `FileReader`.

**The planner's call (technical):**
- The b70987e CI change stands. It shows more and hides nothing.
- **Step 4's revert, after the port, adds a second check.** It changes the parser, not only the evaluator's `min`, so the
  parse tests are shown to bite at their own member.
**Notes:** Written by Cedar Datum by hand. Next: step 4's port. Next free ID -42.

---

**Kind:** outcome
**ID:** 2026-10-28-42
**Timestamp:** 2026-10-30T01:27:05Z
**Continues:** 2026-10-28-3, 2026-10-28-41
**Title:** Milestone: steps 3 and 4 complete (`codec`, `expressions`); the port now waits on the tile release
**Observed:** Linden Caliper (Opus), on `brouter-port`:
- `aa0d918`: the port;
- `94b769e`: `FloatTextCommonTest`;
- `f03a4c2`: the reverts;
- `b706c4e`: the handoff, written into the report.

The harness counted 167,253 tokens, under the watcher's limit.
- **CI, all `success`** (the planner read the conclusions through the API; the counts are the coder's, read from the logs):
  - **40983982019 on `aa0d918`:** `jvm` 75 tests, 0 failures, iOS klibs compiled; `ios-smoke` 21 of 21
    (**simulator-checked**); arm64 JVM step 7 of 7, gate OK.
  - **40984156792 on `94b769e`:** `jvm` 77 of 77; simulator 23 of 23.
  - **40984413979 and 40984753472:** the same counts.
- **The port:**
  - the four drafts moved in;
  - `BExpressionMetaData` and the way and node contexts written;
  - the largest file 379 lines;
  - all 26 profiles checked in every pass.
- **`Float.toString` on the simulator** (the coder's prediction 2): a new common test with Java's strings as expected values
  passed there, with the same hash and length over 30,000 values and 19 literals. **Kotlin/Native printed Java's strings
  for every value tried.**
- **Reverts, both predicted first:**
  - **`max` for `min` (`BExpression.kt:41`):** 2 of 8 failed. The evaluate test failed on 20 of 26 profiles, and the
    parse test on 4, where a constant `min` folds differently.
  - **The parser, `tryCollapse` removed (`:183`):** 1 of 8 failed, at `expressionNodeCount` on 23 of 26 profiles. Only the
    parse test sees it.
- **Unverified:**
  - 19 of the 20 named way getters and the node's `getInitialcost` are never called by a test. The tests read by index.
    Step 6's path model calls them.
  - `parseFloat`, `parseShort` and `HashMap` order on Native;
  - the `RuntimeException` branch;
  - UTF-8 only.
- **Checked by the planner:**
  - the tree is clean at `b706c4e`;
  - the four runs' conclusions;
  - the ten `expressions` files exist;
  - `brouter/src/commonMain` holds 3,589 Kotlin lines over its packages.

**Milestone:** steps 2a to 4 are done. That is `util`, the maths, `codec` and `expressions`, about 3,570 of the 10,759
Java lines (computed from item I's sizes: 1,302 + 156 + 550 + 1,562).

**Cost, counted from this record:**
- Phase 2's sessions so far total about 2.96 million tokens. Steps 0 and 1 took about 0.43 million; steps 2a to 4 and
  the release workflow, about 2.53 million.
- At that rate the remaining ~7,200 lines would take roughly 5 million more (inferred, linear). That puts the
  continuation above the 3 to 6 million estimate the owner was given at -28. The planner reports this to the owner.

**Blocked:** steps 1b and 5a onward need the tiles, so they wait on the owner's run of `publish-tiles` (-30, -31). No
session is started until then.
**Notes:** Written by Cedar Datum by hand. Next free ID -43.

---

**Kind:** outcome
**ID:** 2026-10-28-43
**Timestamp:** 2026-10-30T02:46:13Z
**Continues:** 2026-10-28-30, 2026-10-28-31
**Title:** The owner's run of `publish-tiles` failed at the draft release, with a 403; the cause seen at source; nothing
published
**Observed:** the owner ran `publish-tiles` from `main` (run 40993150859, `workflow_dispatch`, 2026-10-30T02:41:27Z) and
reported "Exit code 1". Read by the planner from the run's jobs and log:
- **`capture`: success.** F0 was fetched and pinned, the four tiles were downloaded fresh and each passed the 1.7.10
  check, and the artifact was uploaded.
- **`publish`: failure at "Create a draft release targeting TOOLS_SHA".** `gh release create` got "HTTP 403: Resource not
  accessible by integration (https://api.github.com/repos/slayer8366/Forager-iOS/releases)".
- **The token held "Contents: write"** (the log's "GITHUB_TOKEN Permissions").
- **Afterwards:** 0 releases and 0 tags; no rulesets.
- **The cause, read at source:** GitHub's REST reference for creating a release (`/en/rest/releases/releases`, fetched
  2026-10-30), verbatim: "If the commit identified by target_commitish … adds or modifies any file under
  .github/workflows/ relative to the repository's default branch, the authenticating token must be authorized to modify
  workflows. Otherwise, this endpoint returns 404 Not Found; some authentication paths surface 403 Resource not
  accessible by integration instead." And: "The GITHUB_TOKEN available to GitHub Actions cannot be authorized for this."
- **Confirmed for this run by `git`:** the target, `TOOLS_SHA` `5d00761` on `brouter-port`, holds `.github/workflows/brouter.yml`
  and not `publish-tiles.yml`. `main` holds only `publish-tiles.yml`. So the target modifies workflow files relative to
  `main`.
- **A slip shared by coder and planner:** the workflow targeted the tools commit by design (-31), and neither checked
  the endpoint's rule. Linden had listed whether workflows may create releases as unverified.

**The planner's call (technical, within the owner's word at -30 for this one file):** the release targets the run's own
commit on `main` (`GITHUB_SHA`), which by definition modifies no workflow file relative to `main`.
- Everything the run executes still comes from `TOOLS_SHA`.
- The pins file already records the tools commit in its header, so provenance is unchanged.
- Linden makes the change on `brouter-port` and its dry run must pass. The planner then copies the file to `main`
  unchanged, and the owner runs it again.
**Notes:** Written by Cedar Datum by hand. Next free ID -44.

---

**Kind:** outcome
**ID:** 2026-10-28-44
**Timestamp:** 2026-10-30T02:50:00Z
**Continues:** 2026-10-28-30, 2026-10-28-43
**Title:** The release target fixed and copied to `main`; the owner's second run is next
**Observed:** Linden Caliper (Opus), `ae8ec40` on `brouter-port`. The harness counted 111,084 tokens.
- **The change:** `gh release create` targets `"$GITHUB_SHA"`, the `workflow_dispatch` run's own commit on `main`, in place of
  `"$TOOLS_SHA"`. The step's name and the comments say so and cite -43. Every file the run executes still comes from
  `TOOLS_SHA` `5d00761`.
- **Dry run 40993755473 on `ae8ec40`: success.** `capture` checked out `5d00761`, the four tiles and the F0 control each
  printed PASS, and `publish` was skipped. **The dry run cannot test the target itself;** only the owner's run can.
- **Checked by the planner:**
  - the diff's only non-comment changes are the step's name and `--target` (`git diff b706c4e ae8ec40`);
  - the new file's SHA-256 is `6b0bedba…a54fe7ae`;
  - the public README and release-notes templates at `5d00761` say nothing about the target, so they stay true.
- **The copy:** `.github/workflows/publish-tiles.yml` is copied byte for byte to `main` in this commit (SHA-256
  `6b0bedba3c02a8b941ed01e9bddd1d022ef4807592897eeb1d06877ea54fe7ae`). This is within the owner's word at -30 for this
  one file.
- **Unverified:** whether `GITHUB_SHA` clears the 403. That rests on GitHub's REST text quoted in -43.
**Notes:** Written by Cedar Datum by hand. Next free ID -45.
