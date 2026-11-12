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

---

**Kind:** outcome
**ID:** 2026-10-28-45
**Timestamp:** 2026-10-30T03:56:12Z
**Continues:** 2026-10-28-3, 2026-10-28-12, 2026-10-28-30, 2026-10-28-44
**Title:** The tile release is published: `brouter-tiles-2026-10-30`, seven assets, each checked by the planner against its pin
**Observed:** the owner ran `publish-tiles` from `main` again (run 40999738961, `workflow_dispatch` on `3a0a492`,
2026-10-30T03:52:09Z) and said "Done". Read by the planner:
- **Both jobs succeeded.**
- **The release:** `https://github.com/slayer8366/Forager-iOS/releases/tag/brouter-tiles-2026-10-30`, "BRouter test tiles,
  2026-10-30".
  - Not a draft, published 2026-10-30T03:52:58Z.
  - Its target is `3a0a4921` (`main`), so -43's fix held.
- **Seven assets:**
  - the four tiles: `W75_N40` 119,765,681 bytes, `W75_N35` 3,572,520, `W70_N40` 5,292,622, `E170_S50` 1,061,705;
  - F0's fixture, 152,665 bytes;
  - `README.md`;
  - `tile-pins.tsv`.
- **The tiles are brouter.de's of 2026-10-30** (`Last-Modified` 01:03:01 GMT), captured at 03:52Z. They supersede Linden's copies
  of 2026-10-29, as -30 said they would.
- **Checked by the planner:** each of the five data files was downloaded from its public release URL and checked against
  the release's own `tile-pins.tsv`. All five match in size and SHA-256 (F0 still `ff6d9d8e…`).
  - The downloads went through this session's proxy, which may add credentials, so an anonymous download is still
    unproven.
  - The README carries OpenStreetMap's attribution, the ODbL 1.0 URI and the copyright page, with no template placeholder
    left. The release notes say the same.
- **This settles the public action RECORD -12 left to the owner.** Item I's step 1 is complete apart from pinning on
  `brouter-port`.

**Next (the planner's call, technical):**
- **A session pins the release on `brouter-port`:**
  - replace `tools/tile-pins.tsv` with the release's;
  - wire `tools/fetch-tiles.sh` into the build, reading the release URL;
  - add item I's step 1 fetch test, seen failing first with one pinned size off by one;
  - read CI.
- **Then step 1b,** the oracle's own runs, the first of the milestones the owner asked to hear about.
**Notes:** Written by Cedar Datum by hand. Next free ID -46.

---

**Kind:** outcome
**ID:** 2026-10-28-46
**Timestamp:** 2026-10-30T04:16:37Z
**Continues:** 2026-10-28-3, 2026-10-28-45
**Title:** Step 1 closed: the release pinned on `brouter-port`; the oracle's JDK equals Phase 1's table; step 1b's control
works on CI
**Observed:** Linden Caliper (Opus), on `brouter-port`:
- `adc8065` and `490d0ae`: the pinning and its revert;
- `fa89911` and `8305898`: `OracleJdkTableTest` and its revert;
- `4be79f0` and `d32f2c3`: E3's control and its revert;
- `f156946`: the report and handoff.

The watcher read 182,796 tokens at 04:09:02Z. The harness's final count was 194,434.
- **Pinning:**
  - `tools/tile-pins.tsv` is the release's own, byte for byte.
  - A `fetchTiles` task fetches the five files from the release into the build, and `jvmTest` depends on it.
  - `TilePinsTest` checks each file against its own copy of the pins.
  - Item I's revert (W70_N40's pinned size off by one) failed as predicted, and was restored from a saved copy.
- **The anonymous download (-45's open point):** both CI jobs fetch the five files with a `curl` that carries no
  credentials. The coder infers that no token reaches it.
- **The oracle's JDK (Amendment 3's stop condition):** `OracleJdkTableTest` rebuilds Phase 1's no-intrinsic table with the
  test JVM's `Math.cos` and asserts Phase 1's SHA-256, `ee004dc0…b379`.
  - It **passed on CI's Temurin 21.0.13+9 amd64**, so the table equals Phase 1's. **Not a stop.**
  - Its revert, the flag dropped, gave Phase 1's default-flags hash, as predicted. The test stays permanent.
- **E3's positive control** (part 7's `TreeBinOrder`, R1 and R4 in fresh JVMs): locally the two orders differ, and each
  regime repeats itself. Its revert, the 997-object perturbation removed, made R4 take R1's order, as predicted.
- **CI, read by the planner:**
  - run 41001173265 on `d32f2c3`, `success`: `jvm` 81 tests, 0 failures. The log shows `OracleJdkTableTest`,
    `E3ControlTest.r1AndR4GiveTheControlDifferentOrders` and both `TilePinsTest` cases passing.
  - The coder read run 41000692626 (simulator 23 of 23, arm64 step 7 of 7, gate OK).
- **Not started in 1b:** the case generators, any stratum, and regimes R2, R3, R5 and R6. **E3's 5% stop has not yet been
  tested.**
- **Calls beyond the amendment:** the pin file taken verbatim; the fetch hung off `jvmTest`, so `ios-smoke`'s arm64 step
  downloads about 130 MB too; `OracleJdkTableTest` kept permanently.

**The planner's call (technical):** R6's JaCoCo agent is pinned at the latest release read from Maven Central's metadata,
by SHA-256, as the oracle jar is. The coder reports the version and its source.
**Notes:** Written by Cedar Datum by hand. Next: step 1b continued. Next free ID -47.

---

**Kind:** outcome
**ID:** 2026-10-28-47
**Timestamp:** 2026-10-30T04:39:03Z
**Continues:** 2026-10-28-3, 2026-10-28-46
**Title:** Step 1b: E3's six regimes run over case lists; F0 0 of 2 and R on `hiking-mountain` 0 of 59 unstable (local JDK);
the planner's rulings
**Observed:** Linden Caliper (Opus), on `brouter-port`:
- `8eea3fc`: E3 over a case list, `e3Strata` and JaCoCo;
- `525448b`: R's generator, the tile-node reader, and R on `hiking-mountain`;
- `e0b5f90`, `240cff5` and `0265239`: the report, the comparator's bite checks and CI.

The watcher read 182,370 tokens at 04:37:45Z. The harness's final count was 187,708.
- **Regimes:** R1 to R6 as E3 defines them, R6 under JaCoCo `org.jacoco.agent` 0.8.16.
  - The version was read from Maven Central's metadata on 2026-10-30, the jar pinned at 314,470 bytes by a computed
    SHA-256. Its SHA-1 and MD5 match Central's.
  - F0's coverage file names 57 `btools` classes, so the agent did instrument the oracle.
- **F0: 0 of 2 unstable.** R1's routes are 226 nodes and 5,047 m, and 306 nodes and 6,713 m, matching Forager's recorded
  geojson in length and cost. F0 logged no collections.
- **R on `hiking-mountain`: 0 of 59 unstable.**
  - The 59 come from 67 candidates; 8 were rejected with no node near the target.
  - All 59 are routes.
  - 8 logged collections, so the log comparison was tested on real data.
  - No case crosses 40°N or 70°W.
- **The positive control moved in every JVM.**
- **The comparator bites, predicted first:**
  - R5's end moved north: F0 2 of 2 unstable;
  - R5's collection lines dropped: exactly the 8 predicted R cases unstable.
- **Time:** `e3Strata` about 11 s for F0 and 3 min 13 s for R on one profile. It is not on CI; `jvmTest` still runs 81 tests.
- **CI:** run 41003465269 on `525448b` `success`. On CI's Temurin the control's R1 and R4 each repeat themselves across
  fresh JVMs.
- **E3's 5% stop:** not reached in either stratum.

**Two gaps the planner finds:**
- **The stability runs used the container's OpenJDK 21.0.12.1, not the oracle's pinned JDK.** That is CI's Temurin
  21.0.13+9 (Amendment 3, -46). The table equals on both (-46), but identity-hash order, which E3 exists to test, is a
  property of the running JVM.
- **R's generator was committed in the same commit as its results** (`525448b`). E's rule "fixed before data" can
  then not be shown for it.

**The planner's rulings (technical):**
- **From here, `e3Strata` and every expected value run on the oracle's JDK:** Temurin 21.0.13+9 amd64. Fetch it into the
  container and pin it by SHA-256 from Adoptium's own published checksum. Say where it came from.
- **F0 and R on `hiking-mountain` are re-run on it first.** That run comes after the generator was committed, so it
  also closes the second gap for this stratum.
- **Each new generator and its seed is committed and pushed before any run of it.**
- **The order stays the coder's:** R's other 25 profiles, then DL-lat and DL-lon, then MP64, ML5 and PM5.
**Notes:** Written by Cedar Datum by hand. Next free ID -48.

---

**Kind:** decision
**ID:** 2026-10-28-48
**Timestamp:** 2026-10-30T05:20:55Z
**Continues:** 2026-10-28-3, 2026-10-28-10, 2026-10-28-47
**Title:** Use the container's resources freely (owner); the secondary coder starts on step 5a alongside step 1b
**Decision:** told that R's other 25 profiles would take about 80 minutes of computer time, the owner, verbatim: "You're
not on the laptop, so take up what you want".
- The container has 4 cores, 15 GB of memory and 22 GB of free disk (`nproc`, `free`, `df` at 05:20Z).

**The planner's calls, following it (technical):**
- **Step 1b's `e3Strata` runs up to 3 profiles at a time,** with each regime still in its own fresh JVMs. No 1b result
  depends on timing. K's timing, at step 11, runs alone.
- **The secondary coder starts now, on step 5a** (`mapaccess`, the file layer), while the primary is busy with 1b. This
  is the role the owner set at -10, "the other secondary when the primary is busy".
  - It works in its own clone, `/home/user/forager-ios-5a`, on its own branch `brouter-port-5a`, cut from `brouter-port`.
  - It writes its own report file, so the two coders never edit the same file: Forager's lesson on serialization
    points. When green, the branch is merged into `brouter-port` by a merge commit, never a rebase.
  - It proposes three names on its first run.
- **Checked:** the primary has already re-run F0 and R on `hiking-mountain` on Temurin 21.0.13+9 (`2ada78c`): 0 of 2 and 0
  of 59 unstable, as on the local JDK.
**Notes:** Written by Cedar Datum by hand. Next free ID -49.

**ID:** 2026-10-28-49
**Timestamp:** 2026-10-30T05:50:00Z
**Continues:** 2026-10-28-28, 2026-10-28-32, 2026-10-28-34, 2026-10-28-38, 2026-10-28-48
**Title:** Step 5a (`mapaccess`, the file layer) verified and merged into `brouter-port`; its file layer has not yet
run on iOS; the secondary coder's three names go to the owner; 5b next, on the secondary
**Decision:** the secondary coder's first session ported step 5a on `brouter-port-5a` (`26d0224`, `6770b86`,
`fe0546c`, `2a8ba5e`). It handed off at the planner's request (183,796 tokens at 05:37Z), with everything done except
reading CI. Report: `docs/audits/2026-10-30-brouter-port-step5a.md`, still marked partial.
- **The coder's evidence (not re-run here):**
  - **tests first:** 10 over stubs. Run 1 failed 9 on `NotImplementedError`, and `fk8` on its own count (E170_S50 has 3
    squares with data, not 5); the test was fixed. Run 2 failed 10 of 10, with 0 compile errors;
  - **the port:** `ReadOnlyFile` on okio's `FileHandle`, `PhysicalFile` with `checkFileIntegrity`, `OsmFile`,
    `StorageConfigHelper`, `NodesCache`'s file half and `checkDataFile`;
  - **the revert:** in `readFully`, `throw EOFException()` became `break`. It was predicted and pushed before the run
    (`fe0546c`). The result was exactly 1 failure, `fk7DataCorruptShort` at cut 1, expected `EOFException`, actual `file
    of size 1 too short, should be 112`. The file was restored from a saved copy, `cmp` showed it identical to that
    copy, and the forward run read 91 of 91.
- **Checked by the planner:**
  - **CI on `2a8ba5e`,** run 41009615362, read from both job logs:
    - `jvm` 91 tests, 0 failures, 41 files. That is the earlier 81 plus 5a's 10;
    - `compileKotlinIosArm64` and `compileKotlinIosSimulatorArm64` both ran, with 0 ` e: ` lines in either log;
    - the simulator ran 23 tests with 0 failures;
    - arm64 ran 7 of 7, and the gate read "OK: all 2 classes ran".
  - **The revert's evidence** in `3-revert-prediction.md` and `4-revert-result.txt` reads as reported: 0 compile errors,
    and a failure only this edit could produce.
  - **The correction to Phase 1** holds at source: `NodesCache.java:197-198`, Forager's `third_party` copy, catches
    `IOException` and rethrows `new RuntimeException(re.getMessage())`, so FK 7 and 8 never reach `:202`.
- **Merged** by the planner into `brouter-port` as `9f41799`, a merge commit with `--no-ff`; its tree is identical to
  `2a8ba5e`. The primary was told first, with the instruction to pull by merge on a rejected push and never rebase.
- **A gap, recorded as unverified:** all ten 5a tests are `jvmTest`, because they compare against the oracle. The
  simulator count is unchanged at 23, so the file layer compiles for iOS and has not run there. okio's Native messages,
  `EOFException`'s name on Native, and the handle a failed `PhysicalFile` constructor leaves open are all [?] or [I].
  **Step 10's simulator run must open and read a real tile through this layer.**
- **The conditions:**
  - **-38** (the largest noisy-bit count on real tiles) and **-34** (whether `fillBuffer`'s end-of-array branch runs)
    move to 5b. 5a decodes no cell: `checkFileIntegrity` passes `reallyDecode = false`, and every `loadSegmentFor`
    probe stops before a decode. That is read from the tests; the call sites at `BitCoderContext.kt:72, 97, 174, 184,
    191` are as reported [I];
  - **-32** (whether `OsmNode.java:134`'s `readUnified` runs) stays with 5b.
- **The coder's calls beyond the amendment, accepted:**
  - `NodesCache` split, with its graph half waiting for 5b and 5c;
  - two declaration-only stubs;
  - `OsmFile`'s decode as a `TODO` for 5b;
  - `checkFileIntegrity` ported for the integrity test;
  - `posIdx` made `internal`.
- **One call to revisit at step 8:** `StorageConfigHelper` catches only `IOException`, where Java catches every
  `Exception`. On a route this can change what fails and how.
- **The names, for the owner:** "Tally Gauge", "Feeler Gauge" and "Plumb Ledger". The first two share "Gauge" with
  the researcher, Madrone Gauge.

**The planner's calls (technical):**
- **Step 5b goes to the secondary coder, in a fresh session,** because the primary is still on 1b. It uses the same
  clone, `/home/user/forager-ios-5a`, on a new branch `brouter-port-5b` cut from `brouter-port` at `9f41799`, and
  writes its own report file.
- **The session's first act:** it reads CI on `9f41799` itself, closes 5a's report with those counts, and pushes.
- **Then 5b up to its sizing stop.** It sizes option (i)'s `hmap` from the pinned JDK's own source, with the tests
  over stubs seen failing, then reports the count and stops before writing the map (Amendment 3).
**Notes:** Written by Cedar Datum by hand. Next free ID -50.

**ID:** 2026-10-28-50
**Timestamp:** 2026-10-30T07:31:28Z
**Continues:** 2026-10-28-10, 2026-10-28-24, 2026-10-28-47, 2026-10-28-49
**Title:** The secondary coder is Plumb Ledger (owner); step 1b's R stratum is done on all 26 profiles, with 1 of
1,534 cases unstable; step 5b's `hmap` is sized, and the planner rules on its tie-break
**Decision:** asked which of the secondary coder's three names to use, the owner, verbatim: "Plumb Ledger
(Recommended)". The option read: "Its reason: \"the work is checking what lines up against the oracle, and writing
down what does not.\" The only one of the three that doesn't share a word with the researcher, Madrone Gauge."
Rejected: "Tally Gauge", "Feeler Gauge". Written into `docs/process/coder-profile.md` and the planner profile's team
table.

**A restart, disclosed.** The session's worker restarted between about 06:30Z and 07:28Z. Both coders finished in that
window, and their final messages reached the planner only from their transcripts.
- Plumb Ledger's two messages carrying the `hmap` count never reached the planner. It waited without an answer and
  handed off at partial.
- Nothing was lost from the repository: both branches were pushed, and their trees were clean.

**Step 1b, Linden's seventeenth session (`970df90` to `db3b133` on `brouter-port`):**
- **The oracle's JDK:**
  - Temurin 21.0.13+9 x64, from Adoptium's GitHub release.
  - Pinned at 207,858,352 bytes, SHA-256 `d7f5d5cf…66d1c`, matching both of Adoptium's published checksums.
  - `tools/fetch-temurin.sh` fetches it into scratch. `e3Strata` refuses to run without it, and every stratum log
    prints the JDK it ran on.
- **R on all 26 profiles: 1 of 1,534 cases unstable,** `R-shortest-025`. In it, R1 to R5 agree, and R6, the coverage
  run under JaCoCo, routed a route 1 m longer.
  - That is 1.7% of `shortest`, under E3's 5% stop.
  - Phase 1 predicted 0 unstable in every stratum, and that prediction missed for this stratum.
  - Why R6 differed is [?].
- **The parallel allowance was not taken.** One stratum run already held the load at about 7 on 4 cores.
- **Checked by the planner:**
  - `origin/brouter-port` is `db3b133`;
  - the 25-profile evidence log shows 24 lines reading "0 of 59" and one reading "R-shortest: 1 of 59";
  - the merge of 5a, `83cba83`, was a pull by merge, never a rebase.
- **The planner's calls:**
  - **`R-shortest-025` is treated as Phase 1's E3 rule says** (Phase 1 report at `20e1f92`, line 831): counted and
    named, kept out of every exact assertion, and compared on kind only;
  - **coverage counts as reach evidence only for a case whose R6 tuple equals its R1 tuple.** That excludes this case's
    `.exec`;
  - **the cause is not investigated now.** It is under the stop, and the rule above already keeps it out of evidence.

**Step 5b, Plumb Ledger's session (`4cc7f20` to `508121e` on `brouter-port-5b`, cut from `9f41799`):**
- **5a closed** with Plumb's own reading of run 41010445171 on `9f41799`: 91, both klibs with 0 `e:` lines, simulator 23
  (simulator-checked), arm64 7 with its gate.
- **Everything but the map is ported.** On `6cf89c8`, run 41014149582, `jvm` read 100 tests with 8 failures.
  - **Checked by the planner from the job log:** all 8 stop at the map's stub, 2 at `NodeHashMap.get` and 6 at `put`.
    The failing classes are `GraphDecodeTest` (5), `CollectOutreachersTest` (2) and `NodeHashMapOrderTest` (1).
- **The `hmap` sizing:** `get`, `put`, `remove` and `values()` reach 823 lines of `HashMap.java`, from the pinned
  Temurin's `lib/src.zip`. 575 of those lines are tree bins. The Kotlin estimate is about 950 code lines (range 850 to
  1,200), in three files.
- **Tree bins can occur.** `OsmNode.hashCode` is `ilon + ilat` (`OsmNode.java:260`, read by the planner). Inside a tree
  bin, nodes with equal hashes are ordered by `tieBreakOrder`, which uses `System.identityHashCode` (`HashMap.java:2063`
  as reported). That makes their order differ from one JVM run to the next. Phase 1 part 7 knew this, which is why item
  I's test refuses equal-hash tree buckets.
- **The re-put is real:** `NodesCache.getGraphNode` puts `existing` back (`NodesCache.java:284-288`, read by the
  planner). So keys and values stay separate.
- **The three conditions, answered from data:**
  - all 25,072 caches were decoded twice on the oracle, in separate JVMs, with identical results;
  - **-38:** the largest noisy-bit count is 19, under the port's 24;
  - **-34:** `fillBuffer`'s end-of-array branch runs 40,772 times, all on way tag bitmaps, and never in a cache stream;
  - **-32:** `readUnified` is unreachable in Forager's configuration. It needs `MicroCache2`, which exists only under
    `-DdisableDirectWeaving`. It ran 0 times, so it stays a named "unsupported".

**The planner's answer on `hmap` (technical, within the owner's option (i)):**
- **Write option (i) as sized,** with keys and values kept separate.
- **At `tieBreakOrder`, the port uses a fixed stand-in for the identity hash.** Each node gets a sequence number from
  one counter per map, assigned at its first tie-break, just as HotSpot assigns an identity hash at first request.
  - **Why this is sound:** an identity hash is an arbitrary value fixed at first request. So any consistent order of the
    nodes is one the oracle could produce in some run, and the stand-in always picks the same one.
  - **What it gives:**
    - wherever `HashMap`'s order is fixed, the port matches it bit for bit;
    - where the oracle's order changes from run to run, the port gives one of its possible outcomes, and the same
      one every time. Android's runtime is in the same position, with its own identity hashes.
  - **Rejected:**
    - a named "unsupported" at the tie-break, which would fail real routes at a point the oracle passes through;
    - leaving the order to Kotlin's own hash, which is unpinned and differs between the JVM and iOS.
- **The class-name comparison before the identity hash** is ported with Java's class names, if more than one class can
  ever be a key. The coder checks the callers of `put` first.
- **Data owed:** how many times `tieBreakOrder` runs on the oracle across 5b's decodes and both F0 routes, counted on
  the instrumented copy as for -34.
- **Tests:**
  - the order test keeps refusing equal-hash tree buckets;
  - one more test shows that equal-hash bins come out in the same order from fresh maps given the same inserts, and is
    seen failing by a revert of the stand-in.

**The planner's calls on Plumb's other items:**
- **Accepted:**
  - `LibmSeam` passed into `calcDistance` and `collectOutreachers`;
  - `jvmTest` opening `java.util` to reflection, as a test-only change;
  - `CollectOutreachersTest` strengthened while still on its stub.
- **`cleanupPeninsulas`:** Java catches `StackOverflowError` there and drops that subtree's cleanup silently
  (`OsmNodesMap.java:62-64`, read by the planner).
  - **On the JVM,** the port keeps the catch so logic runs stay equal, and logs when it fires.
  - **On iOS,** the catch is not possible. The measured depth is 5,679 frames, and **step 10's simulator run measures
    the router's real stack at that depth.**
  - If iOS overflows, the app must run the router on a thread with an explicit stack size. That decision is for later
    and is recorded here.
- **`GraphDecodeTest`'s CI time** is read on its first green run. If the `jvm` job passes 20 of its 30 minutes, a split
  goes to the planner.

**Next (the planner's calls):**
- **Plumb Ledger** writes the map as answered, runs the revert, reads CI, and closes 5b.
- **Linden** continues 1b: DL-lat and DL-lon, then MP64, ML5 and PM5, each generator pushed before its run, then the
  expected values. Then 1b's milestone report.
**Notes:** Written by Cedar Datum by hand. Next free ID -51.

**ID:** 2026-10-28-51
**Timestamp:** 2026-10-30T07:40:28Z
**Continues:** 2026-10-28-17, 2026-10-28-47, 2026-10-28-50
**Title:** E4's targeted strata are selected by the oracle's reach, not counted; they use hiking-mountain
**Decision:** Linden, in session 18 of step 1b, asked two questions on E4's targeted strata (DL-lat, DL-lon, MP64, ML5
and PM5). It built the part common to every answer meanwhile, and chose neither.

**Q1. Does the reach-evidence column select a stratum's cases, or count reach over the first 59?** The planner rules
that **reach selects.**
- **E4 settles it.** "Cases are kept in generation order until the stratum is full" (Phase 1 report at `20e1f92`,
  line 885), and "A stratum that cannot be filled from 20 times its size in candidates is reported short" (line 908).
  Neither makes sense under counting.
- **Three other texts agree:** RECORD -17's names for these strata (line 619 here), J at line 1849, and the open
  question at line 2519.
- **So each stratum is the first 59 candidates,** in generation order, whose reach holds, read from the oracle's R1:
  - its route for DL;
  - its log for MP64, ML5 and PM5.

  It is never read from the port. Rejected candidates are counted.
- **ML5 and PM5** share one candidate sequence, MP64's generator with `memoryclass` 5. Each candidate goes to ML5 or
  PM5 by R1's outcome (`:2028` "memory limit reached", or panic mode at `:2031`), or is rejected. If one run can show
  both, that is a stop.
- **The cap is 1,180 candidates per stratum** (20 × 59). A stratum that is not full by then is reported short, and
  never filled from another generator.
- **E3's six runs** and the instability count are taken over the selected 59, as for R.
- **Where reach is coverage** (NT, CR), the selector is the first code to read coverage. RECORD -50's rule applies to
  it, seen failing first. Linden's note that no code reads coverage today is correct: R6 writes `.exec` files, and
  nothing parses them.

**Q2. Which profile? E4 names none.** The planner rules **`hiking-mountain`**, Linden's default.
- **Why:**
  - it is the profile Forager runs, so these strata test the mechanisms on the app's own path;
  - R already covers all 26 profiles;
  - a uniform draw over the 26 would spend the caps on profiles that rarely route. `rail` and `river` failed with
    "from-position not mapped" on 52 and 53 of 59 in R, as Linden reported.
- **Rejected:** a uniform draw over the 26.
- **What this gives up:** `hiking-mountain` switches off turn restrictions, so these strata do not exercise them;
  R does. The report says so.

**A slip, corrected:** the planner's answer first cited line 889 for the generation-order sentence. A follow-up message
corrected it to 885.
**Notes:** Written by Cedar Datum by hand. Next free ID -52.

**ID:** 2026-10-28-52
**Timestamp:** 2026-10-30T07:42:22Z
**Continues:** 2026-10-28-51
**Title:** A wrong premise in -51: R1 keeps no log, so reach is read from R2; a run showing both panic and the limit
goes to ML5; both coders hand off at the context margin
**Decision:** Linden found two problems with -51's answer.
- **R1 runs without `debug.txt`,** by E3's design (`E3Jvm.kt`'s R1R2 branch checks that the file is absent). The
  "collected" and panic lines are `logInfo` only (`RoutingEngine.java:2025`, `:2031`), so they cannot be seen in
  R1. The planner's "its log for MP64, ML5 and PM5" was a **wrong premise.**
- **One run can show both panic mode and "memory limit reached"** [R, code; whether the data does is not known].
  Panic mode in pass 0 or pass 1 can be followed by the limit thrown in re-tracking (`:1739-1749`, `:1796-1799`,
  `:1893`), and a panic reset followed by an out-of-bounds collection also throws (`:1997-1998`, `:2027`).
  Linden stopped on this, as -51 told it to.

**The planner's rulings (technical):**
- **The reach source is R2,** the same case run at once in R1's own JVM with `debug.txt` (Linden's option (i)).
  - The selection pass is E3's R1R2 JVM over the candidates, in generation order.
  - A candidate's reach counts only if R2's tuple equals R1's, the same rule as -50's for coverage.
  - R3 to R6 run on the selected 59 only.
  - ML5's mark is R1's kind. MP64's and PM5's marks are R2's log lines.
  - **Rejected:** R5, which reads reach only after selection is already done, and a separate selection run, which
    costs a JVM per candidate and buys nothing R2 lacks.
- **A run showing both goes to ML5,** by its outcome (Linden's (a)).
  - ML5 = the run's kind is "memory limit reached", whatever came before it.
  - PM5 = R2's log shows panic mode enabled, and the kind is not the limit.
  - The count of ML5 cases that also showed panic is reported.
  - **Rejected:**
    - both strata, which puts one case in two and breaks E5's per-stratum sizes;
    - neither, which drops a real path;
    - PM5, which would put a fixed-message outcome in the stratum J predicts by its collections.
- **DL is unaffected.** It is selected on R1's route, with `hiking-mountain`.

**Context:**
- Plumb Ledger passed 180k tokens at 07:40:15Z, eight minutes into its session. Most of that went on reading
  `HashMap.java` and its probes.
- Linden passed 180k at 07:41:45Z, at 190,822.
- Both were asked to hand off. The helpers' limit is 200k (RECORD -15).
- **The planner's dispatches cost each session about 60k tokens before any work.** The next ones point at the
  handoff, the governing RECORD entries and the files they name, and nothing more.
**Notes:** Written by Cedar Datum by hand. Next free ID -53.

**ID:** 2026-10-28-53
**Timestamp:** 2026-10-30T07:45:31Z
**Continues:** 2026-10-28-50, 2026-10-28-52
**Title:** Both coders handed off; Plumb's `put` trace and partial tie-break count; the planner accepts its three
proposals
**Observed:**
- **Linden, session 18 (`fa54097` on `brouter-port`, work in progress):**
  - the rulings of -50 to -52 are written into the Phase 2 report;
  - `DLGenerator.kt` was pushed without being compiled or run, so CI on `fa54097` may go red;
  - no stratum ran.

  Session 19 started at once. Its first act is to compile the code and fix any red.
- **Plumb, session 3 (`7ce67e2` on `brouter-port-5b`, docs only):**
  - **`put`'s callers** [R, as reported]: every key in `hmap` is exactly `btools.mapaccess.OsmNode`. The only
    `put` is `OsmNodesMap.java:238-239`, and no path puts `OsmNodeNamed` or `OsmNogoPolygon` into it. So -50's
    class-name step has nothing to order. No wrong premise.
  - **`tieBreakOrder` on the oracle,** counted with the pinned Temurin's own `HashMap.java` patched into `java.base`
    with counters [C, as reported]:
    - 0 in 12 region loads;
    - 0 in F0's two routes;
    - 19 calls, all from `treeify`, at W75_N40 (106,130) sub 740, among the first 8,756 of 25,072 decoded caches.
      The rest of the run had not finished.

    **So the tie-break does occur on real tiles.** -50's stand-in is needed, not hypothetical.

**The planner's calls (technical), on Plumb's three proposals:**
- **Accepted: the stand-in's number lives on the `OsmNode` object, not on the map's entry.** HotSpot's identity hash
  belongs to the object, and `untreeify`, `treeifyBin` and `split` replace entries.
- **Accepted: a named "unsupported" in `tieBreakOrder` when the two keys' classes differ.** It never fires on
  today's callers, and it turns the one case where Java would order by class name into an explicit failure, never a
  silent difference.
- **Accepted: the new determinism test goes in `commonTest`,** so it also runs on the simulator (23 to 24,
  simulator-checked). It is the first 5b behaviour to run on iOS.
**Notes:** Written by Cedar Datum by hand. Next free ID -54.

**ID:** 2026-10-28-54
**Timestamp:** 2026-10-30T08:02:01Z
**Continues:** 2026-10-28-15, 2026-10-28-50, 2026-10-28-52, 2026-10-28-53
**Title:** The owner's stop: both coders handed off cleanly; 5b's branch is red on two questions for the planner; a
missed watcher alert let one session run to 453k tokens; the planner's handoff is written
**Decision:** the owner, verbatim: "Find a place to stop and write a handoff". Both coders were asked to finish the piece
in hand, start nothing new, and hand off. The planner's handoff is `docs/process/planner-handoff.md` on `main`.

**Where each branch stands (08:01Z, `git rev-parse` on origin):**
- **`main`:** `d0d0f5e` before this entry.
- **`brouter-port`: `82d310a`, Linden, session 19.** Clean tree.
  - `fa54097` compiled locally, and CI on it passed.
  - **R's 1,534-case sequence** was pinned by digest before `RGenerator` was edited, and is unchanged after (`437e047`). No
    revert was run for that test.
  - **`E3Select`, `E3Targeted`, `E3DLTest` and `E3MemoryTest`, with the seeds** 20261201 to 20261204, were pushed
    before any run (`85008b1`). CI on it read `jvm` 91, simulator 23, arm64 7 [R, as reported]. Those are 5a's counts,
    so they show only that the code compiles: the strata package is outside `jvmTest`.
  - **No targeted stratum has run.** No expected values have been written. The handoff is the Phase 2 report's "Step 1b:
    the handoff (nineteenth session)".
- **`brouter-port-5b`: `279194d`, Plumb, session 4.** Clean tree. **Red locally:** 101 tests, 3 failures.
  - **The map is written** (`1a5a25f`), from the pinned `HashMap.java` (SHA-256 `71c8d822…`), with the stand-in on
    `OsmNode` as -50 and -53 ruled.
  - `NodeHashMapTieBreakTest` (`commonTest`) passes, with ties met in all three places it checks.
  - `GraphDecodeTest` passes 5 of 5 with the real map.
  - **Question A, `CollectOutreachersTest` 2 of 2 failing** at W75_N40 near (105,130) sub 735. The port's new JVM
    stack-overflow catch fired 3 times. Whether the oracle's silent catch fired is [?].
    - Plumb's untested guess [I]: the port's frames are larger, so it overflows where the oracle does not.
    - If that is right, -50's premise that keeping the catch "keeps logic runs equal" fails on this region. It also
      means the oracle's own result there depends on the thread's stack size.
  - **Question B, `NodeHashMapOrderTest` failing its own refusal assertion:** 8 of 12 seeds were refused for an
    equal-hash tree bucket. The generator does not keep such buckets out, and -50 said the test "keeps refusing" them.
  - **Not done:** CI on `1a5a25f` and `279194d` (so the new `iosMain` `actual`s have not been compiled for iOS), both
    reverts, and the tie-break decode total. Session 3's count run stopped partway and is recorded as incomplete.
  - **A slip, corrected before any run:** the `DirectWeaver` revert's predicted total was 10, but its own per-class lines
    add up to 7. The addendum was pushed before any run.
- **`brouter-port-5a`: `2a8ba5e`.** Merged as `9f41799`; kept, not deleted.

**A missed watcher alert, disclosed.** Plumb's second session (5b, started 05:50Z) passed 180k at 05:56:14Z. The
watcher wrote "LIMIT 184347" at 05:56:33Z, but the planner never acted on it. The alert was most likely among the
notifications the 06:30Z restart lost, and on resuming the planner checked only the tasks the harness listed. The session
ran unchecked until 06:52Z and ended at **453,403** tokens of context. Its work was verified at -50, and none of it is
in doubt. **The rule now (the planner's call):** after any restart, read every watcher's output file, not only the
tasks the harness lists.

**Token counts, each session's final context [C: the transcripts' last usage]:**

| Session | Tokens |
|---|---:|
| Plumb 1 (5a) | 190,217 |
| Plumb 2 (5b) | 453,403 |
| Plumb 3 | 217,165 |
| Plumb 4 | 193,387 |
| Linden 17 | 148,299 |
| Linden 18 | 205,562 |
| Linden 19 | 178,000 |

Each session spent about 60k before its first action. The trimmed dispatches did not shrink that much.
**Notes:** Written by Cedar Datum by hand. Next free ID -55.

**ID:** 2026-10-28-55
**Timestamp:** 2026-10-30T08:05:13Z
**Continues:** 2026-10-28-54
**Title:** Forager-iOS paused: the owner moves this account to the trail data
**Decision:** the owner, verbatim: "I'm going to have this account work on the trail data instead, it needs to get pushed
out fast."
- **The iOS work is paused at -54's stop.** All four branches are pushed, all three clones are clean, and nothing is
  left running. `docs/process/planner-handoff.md` is the starting point on resume.
- **The planner's call for resuming (technical):** before relaunching any coder, check Forager's `pre-main` for
  trail-data changes that reach routing. These are `hiking-mountain.brf`, the routing call in
  `BRouterTrailRouter.kt`, and F0's fixture at SHA-256 `ff6d9d8e…`. The port is measured against Android's
  behaviour, so a change there moves its oracle, its F0 stratum or its pins. This is the profile's "keeping iOS level
  with Android" duty, due anyway at the next phase.
**Notes:** Written by Cedar Datum by hand. Next free ID -56.

**Kind:** outcome
**ID:** 2026-10-28-56
**Timestamp:** 2026-11-04T04:41:56Z
**Continues:** 2026-10-28-3, 2026-10-28-54, 2026-10-28-55
**Title:** The resume checks: Forager's `pre-main` moved one premise of Phase 1 (Android now reads each node's
height); 5b's CI read and equal to the coder's local run; no coder relaunched
**Decision:** the owner, verbatim: "Yes, start the resume checks", after being told the port was paused at -55 and
what the resume would check first. Nothing was relaunched, and no branch but `main` was written.

**Forager's `pre-main`, `e2969abe4` (every pin on `brouter-port`) to `d6e2a0415` (338 commits, read 2026-11-04):**
- **Unchanged** (`git diff --stat`, empty): `third_party/brouter` (the engine source) and every profile under
  `app/src/main/assets/routing`, `hiking-mountain.brf` among them.
- **F0's fixture is unchanged:** `app/src/test/resources/routing/segments/W75_N40.rd5` still hashes to `ff6d9d8e…5990`
  [C: `sha256sum`]. F0 stays valid.
- **The routing call is unchanged:** the engine's construction and `setParams` (`profile`, `alternativeidx`,
  `profile:use_dynamic_range`) have no hunk in the diff.
- **Changed: what Android reads from the result.** `ba6a52d7` (2026-10-30, Forager's "trail heights" dispatch
  2026-10-30-06) adds `heightsMetres` to each route, read from `OsmPathElement.getSElev()`, with `Short.MIN_VALUE` as
  "no height" (`BRouterTrailRouter.kt`, `trailRouteOf` and `heightMetresOf`, at `d6e2a0415`) [R].
- **Why that moves a premise:** Phase 1 put elevations under "recorded, not asserted" because "Forager reads none of
  them" (Phase 1 report at `brouter-port`, line 755). That was true at `e2969abe4` and is false at `d6e2a0415`. E3's
  compared tuple is kind, node count, cost, distance and a digest over each node's (ilon, ilat) (`E3Case.kt:54`), so no
  stratum can see a wrong height.
- **Why the current tuple would not catch one either:** `hiking-mountain.brf:13` sets `consider_elevation = false`, so
  its hill costs are 0 (`:73-77`), and Forager does not override it [R]. A wrong height on Forager's profile would
  most likely leave the route, cost and distance unchanged [I: from the profile, `StdPath`'s use of `delta_h` not
  read]. The check would pass without ever seeing the data that could fail it.
- **Heights are not only copied:** at a route's cut ends the engine interpolates in `double` and casts to `short`
  (`OsmPath.java:244-266` and `interpolateEle` at `:388-393`, Forager's `third_party` copy) [R]. The port must match
  that cast.
- **Also new in Forager, for use later:** a second fixture with 3″ heights, `segments-heighted/W75_N40.rd5` (178,867
  bytes, SHA-256 `14f595e8…a965c`), and the release jar's output on it for eight routes,
  `brouter-cli/heighted/*.geojson`, with every point carrying a height (Forager's `app/src/test/resources/routing/README.md`)
  [R, not re-run]. Forager's production squares carry heights since its elevation run (that README, and
  `server/pmtiles-worker/test/fixtures/rd5/README.md`) [R, as written there].
- **This is -3's abort condition "a moved base that changes a premise".** The planner's lean, technical, as distance
  was added at Phase 1 (line 753): add each node's `selev` to the compared outcome, and add a heighted F0 stratum from
  Forager's new fixture. It needs the expected values re-derived for the strata already run (F0 and R), and the owner
  is asked before anything is built.

**Step 5b's CI, read by this planner session** (run 41023633806 on `279194d`, both job logs):
- `jvm`: 101 tests, 3 failures, 0 errors; the failures are `CollectOutreachersTest` (both, at `:141`) and
  `NodeHashMapOrderTest` (at `:42`). That equals Plumb's local run (-54).
- `compileKotlinIosArm64` and `compileKotlinIosSimulatorArm64` both ran, with 0 ` e: ` lines in either log. The
  `jvmMain` and `iosMain` `actual`s from `1a5a25f` compile for iOS.
- Simulator: 24 tests, 0 failures (**simulator-checked**). That is 23 plus `NodeHashMapTieBreakTest`, as -53 said.
- arm64: 7 of 7, and the gate read "OK: all 2 classes ran".

**Question A, the read-only half (handoff, step 2):** Android starts the engine on
`Thread(r, "brouter-engine")`, with no stack size given (`BRouterTrailRouter.kt:206` at `d6e2a0415`), and runs
`engine.doRun` there (`:167`) [R]. So it gets the runtime's default stack. ART's default is [?], not read. The
data half (the oracle's catch counted; both engines at two stack sizes) is not started.

**Still open:** questions A and B (-54), and the heights premise above. Both coders stay stopped.
**Notes:** Written by a new planner session (a cloud session; not Cedar Datum) by hand, after reading this
repository's `CLAUDE.md` and Forager's on `pre-main` at `d6e2a0415`. Next free ID -57.

**Kind:** decision
**ID:** 2026-10-28-57
**Timestamp:** 2026-11-04T05:18:00Z
**Continues:** 2026-10-28-3, 2026-10-28-56
**Title:** Heights join the comparison (owner)
**Decision:** asked, at -56, whether to add each point's height to what the differential tests compare, or keep heights
out and accept that a wrong height on iOS would pass every test, the owner, verbatim: "Add heights to the comparison,
and open the PR".
- **What it changes:** Phase 1's "recorded, not asserted" for elevations (Phase 1 report, line 755) no longer holds for
  heights. Each node's `selev` joins E3's compared outcome, beside the node list, cost and distance, and is compared
  exactly, `Short.MIN_VALUE` ("no height") included.
- **The planner's calls (technical; the owner may overrule):**
  - **A heighted F0 stratum** from Forager's `segments-heighted/W75_N40.rd5` (SHA-256 `14f595e8…a965c`, at Forager
    `pre-main` `d6e2a0415`), pinned in the tile release like F0. F0's own fixture has no heights, so without it the
    new field could fail only where the brouter.de tiles carry heights, and whether they do is [?]. The new stratum is
    data that can fail the check.
  - **Expected values for F0 and R** are re-derived with the new field, and the 1,534 R cases re-run for stability. A
    case whose heights differ between E3's six runs is counted as unstable, under E3's own rule.
  - **The test is seen biting first:** a revert that breaks the `short` cast in `interpolateEle` (`OsmPath.java:392`)
    must fail it, with the prediction pushed before the run.
  - **Forager's pins move only for this:** the new fixture is read at `d6e2a0415`; every other pin stays at
    `e2969abe4`, since -56 found nothing else on the port's path changed.
- **Not yet dispatched.** The work goes on `brouter-port`, and this planner session can push only its own branch. It is
  dispatched once the record is on `main` and a session can write `brouter-port`.
**Notes:** Written by the planner session of -56 by hand. Next free ID -58.

**Kind:** intent
**ID:** 2026-10-28-58
**Timestamp:** 2026-11-04T19:35:15Z
**Continues:** 2026-10-28-56, 2026-10-28-57
**Title:** Heights in E3's comparison, a heighted F0 stratum, and F0 and R re-run (Linden, session 20)
**Decision first:** the owner, verbatim: "You may push to brouter-port and brouter-port-5b".

**Two corrections to -57, both the planner's own wrong premises:**
- **"Expected values for F0 and R are re-derived."** None were ever written (the planner's handoff: "expected values:
  none written"). So there is nothing to re-derive. They will carry heights from the start. What does need repeating
  is F0's and R's stability under the new tuple.
- **"Pinned in the tile release like F0."** The release is fixed once published, and a new one needs the owner's run of
  `publish-tiles` (-43 to -45). Forager's raw file at commit `d6e2a0415` is just as fixed, being addressed by commit,
  and `publish-tiles` fetched F0 itself that way (`F0_URL`, `.github/workflows/publish-tiles.yml:20` on
  `brouter-port`). So the heighted fixture is fetched from there through `tools/fetch-pinned.sh`, by size and SHA-256.
  No new release is made.

**Change** (on `brouter-port`, `jvmTest` and `tools` only):
- `E3Outcome` gains a heights digest: 64-bit FNV-1a over each node's `getSElev()`, in order, `Short.MIN_VALUE`
  included. It is compared exactly, like the node digest. The stored form changes, so every stratum log from before
  is marked as written under the old tuple.
- **Stratum F0h:** Forager's `app/src/test/resources/routing/segments-heighted/W75_N40.rd5` at `d6e2a0415` (178,867
  bytes, SHA-256 `14f595e8469455bcdcc600239102cb7d60641185759fcae29cfed444af8a965c`), into its own folder. The routes
  are the eight that Forager's `brouter-cli/heighted/*.geojson` cover, with ends as Forager's tests give them. Its
  reach evidence: R1's point count, and a digest of its heights, equal those of the command-line router's GeoJSON.
  Only counts and digests are committed, never coordinates beyond Forager's own ends (E1, as F0).
- **F0 and R are re-run** under the new tuple: F0 at 2 cases, R at 1,534 over 26 profiles, with instability counted.
- **Data owed:** on R's R1 routes, how many nodes have a height (not `Short.MIN_VALUE`), per tile. That says whether the
  brouter.de tiles exercise the new field at all.
- **The check bites:** a one-line revert in `E3CaseRun` that drops the heights from the digest must fail F0h's reach
  assertion and nothing else. The prediction is pushed before the run.

**Scope boundary:** writes only `brouter-port`. No `commonMain` change: the port's own heights come with step 6's path
model. It does not touch `brouter-port-5b`, `main`, slayer8366/Forager, the tile release or accounts.
**Baseline:** `brouter-port` `82d310a`; Forager `pre-main` `d6e2a0415` for the heighted fixture and its GeoJSON, and
`e2969abe4` for every other pin.
**Prediction (planner):**
- F0h's eight R1 routes match the command-line router on count and heights, all eight;
- F0, F0h and R are stable under the new tuple, apart from `R-shortest-025` (-50);
- the brouter.de tiles carry heights on most route nodes [I: not read].
**Finish line:** pushed, CI on the head read by the planner, the revert seen failing as predicted, F0h, F0 and R reported
in the Phase 2 report, and a handoff. Then the targeted strata (DL, MP64, ML5, PM5) resume as Linden's session 19
handed off.
**Abort conditions:** -3's, plus a height disagreement between R1 and the command-line router (a finding, not a fix).
**Notes:** Written by the planner session of -56 by hand. Next free ID -59.

**Kind:** intent
**ID:** 2026-10-28-59
**Timestamp:** 2026-11-04T19:35:15Z
**Continues:** 2026-10-28-50, 2026-10-28-53, 2026-10-28-54, 2026-10-28-58
**Title:** The planner's answers to Plumb's questions A and B; 5b resumes, without closing (Plumb, session 5)
**Answers (technical; the owner may overrule):**
- **A, `CollectOutreachersTest`'s overflow: data before any fix.** No fix is chosen in this session.
  - Count the oracle's own `StackOverflowError` catch (`OsmNodesMap.java:62-64`) on a patched copy, as
    `tieBreakOrder` was counted (-53), on this test's region loads at the default stack.
  - Re-run only this class with `-Xss1g` from a scratch init script, never committed, as Plumb's handoff step 3 says.
    The prediction is pushed first.
  - Report whether the oracle overflowed at the default stack, and whether either engine's counts change at 1g.
  - If the oracle's own result depends on the stack, that goes to the owner. It bears on what "Android's behaviour"
    means, since Android runs the engine at the default stack (-56).
- **B, `NodeHashMapOrderTest`'s refusals.** "Refuse" (-50) meant that such a draw is discarded and drawn again, not
  that the test fails.
  - The generator redraws a seed's keys until no equal-hash tree bucket forms, up to 20 draws per seed, with every
    redraw counted.
  - The test asserts order equality on every accepted seed, and fails if any of the 12 seeds is not accepted within
    its cap.
  - Equal-hash buckets stay covered by `NodeHashMapTieBreakTest`.
  - A revert of the filter must make the test fail, with the prediction pushed first.
- **Plumb's four calls beyond the handoff (fourth session) are accepted:** `tieBreakCalls`, the one `System.err` line,
  the runner's optional arguments, and `expect`/`actual` for the catch. Their compile on iOS was confirmed at -56.

**Change:** on `brouter-port-5b` only: B as above; A's data; both reverts (`DirectWeaver` at 7 and the tie-break at 1,
with their predictions restated for a red forward and pushed before they run); the tie-break decode total re-run.
**Scope boundary:** writes only `brouter-port-5b`. No fix for A.
**Finish line:** pushed, CI read by the planner, A's data reported. 5b stays open until A is answered.
**Abort conditions:** -3's.
**Notes:** Written by the planner session of -56 by hand. Next free ID -60.

**Kind:** outcome
**ID:** 2026-10-28-60
**Timestamp:** 2026-11-04T20:05:52Z
**Continues:** 2026-10-28-50, 2026-10-28-59
**Title:** 5b, Plumb's fifth session: B fixed and its revert bites; A's data shows the port overflowing where the oracle
does not; a test that cannot see its own false branch; handed off at 180k
**Observed:** Plumb Ledger, `brouter-port-5b` `279194d` to `98c01e6` (seven commits; clean tree). It handed off at the
planner's message after the watcher read 181,610 at 20:03:38Z. Report: `docs/audits/2026-10-30-brouter-port-step5b.md`,
"Fifth session". Still open, as -59 said.
- **B:** `NodeHashMapOrderTest` redraws refused draws (`9991929`, the only source change in the session: `git diff
  --stat 279194d 98c01e6 -- brouter/src`, one file). It passes, with 15 redraws, and every seed accepted by draw 5. The
  filter's revert was predicted at 3 of 101 and observed at 3, with "iteration order" [R, as reported].
- **Both older reverts, as restated before they ran** (`d8ccf2a`, then `84aafc7`): `DirectWeaver` 7 of 7. The
  tie-break revert was predicted at 2 or 3 and observed at 3, its own failure being `NodeHashMapTieBreakTest`. For two
  of the seven `DirectWeaver` failures the runner cut the message before the port's half [R, as reported].
- **The tie-break decode total, complete:** `[19, 0, 0, 0]`, all of it `treeify` at W75_N40 (106,130) sub 740. The
  25,072 cache lines hash equal to the plain jar's [R, as reported].
- **A, the data:**
  - **In the test's own JVM, the oracle did not overflow** at the default stack or at `-Xss1g`. Checked by the
    planner in `12-question-a/jvmtest-default.txt` and `jvmtest-1g.txt`: the counting class's load line prints in
    both, and its per-firing line (`oracle-catch-counter.diff`, line +67) appears in neither.
  - **The port did overflow at the default stack,** 2 to 4 times a run, and its mode 2 `C` line differed between
    runs (`22106 39455 39455` and `22157 37375 37375`). That run-to-run variation is JIT timing [I, untested].
  - **At 1g the port did not overflow,** and `collectOutreachersWithoutADestination` passes over all 12 regions
    (`jvmtest-1g.txt`).
  - **So -50's premise fails** at the default stack on this region: keeping the catch does not keep logic runs equal,
    because the port overflows where the oracle does not. The oracle's own result showed no dependence on stack size,
    so -59's stop did not trigger.
- **A weaker piece of evidence, discounted by the planner:** the standalone probe (`probe-regions-*.txt`) reports 0
  catches at both stacks. But its `C` values at sub 735 (`37435 41098 32946`) are not the test's (`38715 41099
  41099`), and its last line reads "max recursion depth 0". It did not run the computation in question, so its zeros
  are not evidence for A. The test-JVM runs above carry A.
- **A new finding:** `collectOutreachersWithADestinationVanishesFarNodes` fails its coverage assertion (`:116`) once
  its regions all agree: "canEscape did not answer both ways: false/true 0/3587" (`jvmtest-1g.txt`). The oracle's
  `canEscape` never answers false on these regions, so the assertion's false branch cannot be satisfied there.
- **CI, read by the planner** (run 41900695206 on `84c6fc2`, the same source as the head): `jvm` 101 tests, 2
  failures, both `CollectOutreachersTest`; 0 ` e: ` lines in either log; both `compileKotlinIos*` ran; simulator 24
  of 24 (**simulator-checked**); arm64 7 of 7, with the gate reading "OK: all 2 classes ran". The head's own run
  (41903010654) was still queued.
- **Calls beyond scope, accepted:** the counting class in both test-JVM runs, through scratch init scripts; the probe
  at 1g; revert predictions counting only failures that carry the revert's own message.

**For the owner: how the port should handle the deep recursion in `cleanupPeninsulas`** (not decided here).
**Planner's call (technical):** before any fix, check who calls `canEscape` and whether its false branch is reachable on
Forager's path. Then either choose regions that reach it, or record it as unreachable rather than demand coverage.
**Notes:** Written by the planner session of -56 by hand. Next free ID -61.

**Kind:** decision
**ID:** 2026-10-28-61
**Timestamp:** 2026-11-04T20:12:15Z
**Continues:** 2026-10-28-50, 2026-10-28-60
**Title:** The deep recursion in `cleanupPeninsulas` is decided after step 10 (owner); 5b stays red and unmerged
**Decision:** asked how the port should handle the recursion in `cleanupPeninsulas` that overflows where the oracle does not
(-60), the owner chose, verbatim: "Decide after step 10". The option read: "Leave CollectOutreachersTest red, close the
rest of 5b, and choose once the simulator run measures real stack use on iOS. 5b can't merge into brouter-port while
the test is red."
- **Rejected:** "Walk it without recursion (Recommended)", an explicit-stack rewrite of the port's subtree walk; and
  "Keep recursion, big stack", an explicit large stack on the engine thread in the tests and the app.
- **What follows:**
  - `CollectOutreachersTest` stays red. It is reported at every CI read, never silenced, skipped or weakened (Forager's
    CLAUDE.md, Testing).
  - 5b closes everything else: the report is marked done except A.
  - **5b is not merged into `brouter-port`.** The planner's call (technical): 5c and the steps after it are cut from
    `brouter-port-5b`, so they build on the graph half. Each later merge into `brouter-port` waits on this decision.
  - Step 10's simulator run measures the recursion depth on the router's thread on iOS (-50), and this question comes
    back to the owner with that number.
- **`canEscape`'s false branch (-60) is reachable** [R]: its only caller is `RoutingEngine.java:2016`, inside the
  memory collection (`:2005-2020`: direct weaving, not in panic mode, out of memory bounds). That is the path MP64
  targets (-51). The planner's call: the assertion stays. The coder looks for a region where the oracle answers false
  (an island with no hollow link), and if none is found among the pinned tiles, MP64's runs are read for one. The
  finding goes to the planner either way.
**Notes:** Written by the planner session of -56 by hand. Next free ID -62.

**Kind:** outcome
**ID:** 2026-10-28-62
**Timestamp:** 2026-11-04T20:50:14Z
**Continues:** 2026-10-28-50, 2026-10-28-57, 2026-10-28-58
**Title:** Heights in E3's comparison: F0h 8 of 8 equal to the command-line router; R 0 of 1,534 unstable under the new
tuple; the revert bites; handed off at 180k
**Observed:** Linden Caliper, session 20, `brouter-port` `82d310a` to `3688c9f` (ten commits; clean tree; no change
under `commonMain` or `iosMain`, by `git diff --stat`). It handed off at the planner's message after the watcher read
181,440 at 20:48:09Z. Report: the Phase 2 report, its twentieth-session section and handoff.
- **The tuple:** `E3Outcome` carries a heights digest (`E3Case.kt`, filled at `E3CaseRun.kt:66`). `fromField` reads
  the last five fields, so a kind containing `|` survives. `step1b/00-tuple-note.txt` marks the earlier logs as written
  under the old tuple.
- **F0h** (Forager's heighted fixture at `d6e2a0415`, fetched by `tools/fetch-f0h.sh`, size and SHA-256 checked; ends
  from `server/pmtiles-worker/test/fixtures/rd5/routes.json`):
  - all 8 cases routed and are stable;
  - **checked by the planner:** each of the eight digests in `step1b/12-f0h-expected.tsv`, computed from Forager's
    `brouter-cli/heighted/*.geojson` by `tools/geojson-heights.py`, appears in exactly one R1 tuple of
    `12-f0h-stability.tsv`, with the same point count;
  - the counts add to 3,630, every point with a height, 5.75 to 413.75 m, as Forager's README says;
  - **the height rule:** `FormatJson.java:109` writes `selev / 4.`, and omits the value for `Short.MIN_VALUE` [R, as
    reported].
- **F0:** 2 of 2 stable, tuples unchanged, and no node has a height (0 of 226, 0 of 306).
- **R:** 26 profiles, 1,534 cases, **0 unstable** (`16-r-summary.txt`, read by the planner). Every case's first five
  fields equal the old run's. `R-shortest-025`, unstable once at -50, was stable this time. The cause is [?], and -50's
  ruling on it stands.
- **Do the brouter.de tiles exercise heights?** Yes. Over R's 1,268 R1 routes, 427,924 of 428,277 nodes carry a height.
  The 353 without one fall on 30 routes. Where they are is [?].
- **The revert** (`fnv1aShorts(heights)` to `fnv1aShorts(emptyList())` at `E3CaseRun.kt:66`) was predicted and pushed
  first (`d971358`). **Checked by the planner** in `15-heights-revert.txt`: 0 ` e: ` lines; `jvmTest` 93 of 93; the
  one failure is at `E3F0hTest.kt:33`, with every digest reading the FNV offset basis; nothing else failed. It was
  restored from a saved copy, with `git diff` empty afterwards.
- **CI:** run 41900384198 on `9302765`, as the coder read it: `jvm` 93 tests, 0 ` e: `, `fetchF0h` ran. The run on the
  head (41910184631) was in progress when this was written.
- **A gap, the planner's own:** -57 named a revert of `interpolateEle`'s `short` cast, and -58 named a different one.
  -57's would mean editing the oracle jar, and the port has no `OsmPath` until step 6. So **nothing yet shows that the
  cut-end interpolation is covered.** It is owed at step 6, when the port's `OsmPath` is compared on F0h, whose
  heighted routes end partway along ways [I: not checked per route].
- **Calls beyond scope, accepted:** `E3OutcomeTest`; the per-tile height count; F0h's fetch running with every oracle
  test; F0's no-height digests predicted but not asserted; the sequence check inside R's run; `__pycache__` ignored.
**Next:** Linden's twenty-first session reads CI on the head first, then runs the targeted strata (DL, MP64, ML5/PM5)
under the new tuple. MP64 is also read for `canEscape` answering false (-61).
**Notes:** Written by the planner session of -56 by hand. Next free ID -63.

**Kind:** outcome
**ID:** 2026-10-28-63
**Timestamp:** 2026-11-04T20:55:16Z
**Continues:** 2026-10-28-62
**Title:** CI on `brouter-port`'s head `3688c9f`: green
**Observed:** run 41910184631, `success`, both job logs read by the planner:
- `jvm`: 93 tests, 0 failures; `fetchF0h` ran; `compileKotlinIosArm64` and `compileKotlinIosSimulatorArm64` ran, with 0
  ` e: ` lines in either log.
- Simulator: 23 tests, 0 failures (**simulator-checked**). That is `brouter-port`'s count; 5b's 24th test lives on
  `brouter-port-5b`.
- arm64: 7 of 7, and the gate read "OK: all 2 classes ran".
**Notes:** Written by the planner session of -56 by hand. Next free ID -64.

**Kind:** intent
**ID:** 2026-10-28-64
**Timestamp:** 2026-11-06T03:00:27Z
**Continues:** 2026-10-28-51, 2026-10-28-52, 2026-10-28-56, 2026-10-28-62
**Title:** Forager re-checked before relaunch: a second engine entry point, `BRouterRouteWays`; the targeted strata go
ahead (Linden, session 21)
**Decision first:** the owner, verbatim: "Begin next step".

**Forager `pre-main`, `d6e2a0415` to `a0535ece` (read 2026-11-06):**
- **Unchanged:** `third_party/brouter`, every profile, `BRouterTrailRouter.kt`, F0's and F0h's fixtures (`git diff
  --stat`, empty for each).
- **New, in production:** `app/src/main/java/com/zynergylabs/forager/app/routing/BRouterRouteWays.kt` (`3cd6b3d8`, the
  way-to series), wired at `AppContainer.kt:509` and called from `WayToEnds.kt:195` [R]. It skips `RoutingEngine`: it opens
  a square with `PhysicalFile`/`OsmFile`, decodes nodes, links and geometry (`OsmNode.parseNodeBody`,
  `GeometryDecoder.decodeGeometry`), and reads each way's tags through `BExpressionContextWay.getKeyValueDescription` [R].
- **No new code to port:** all 11 of its BRouter calls are on Phase 1's method-level reach list
  (`03-reach/out/T0/reached-methods.tsv`, matched by class and method name, not by overload [C]). `getKeyValueDescription`
  is already ported (`BExpressionContext.kt:124` on `brouter-port`).
- **A new use, and a premise that moves with it:** Phase 1 item B12 says `getKeyValueDescription`'s text "goes into messages
  Forager does not read". Forager now reads it, as tags (`tagsOf`, `BRouterRouteWays.kt:134`). No differential test
  compares tags or decoded geometry from this entry point. **Put to the owner** (-65), as heights were at -56.

**Change** (Linden, session 21, `brouter-port` only, `jvmTest` and `tools`): the targeted strata as the nineteenth
session's handoff and -51/-52 set them, under -58's tuple:
- DL-lat, then DL-lon, then MP64, then the shared ML5/PM5 sequence, one at a time;
- each generator was pushed before its run (`85008b1`);
- MP64's runs are also read for `canEscape` answering false, at `RoutingEngine.java:2016` (-61).
**Scope boundary:** writes only `brouter-port`. It does not touch `brouter-port-5b`, `main`, slayer8366/Forager or the
tile release.
**Baseline:** `brouter-port` `3688c9f` (CI green, -63).
**Prediction (planner):** each stratum fills its 59 within the cap of 1,180 draws; 0 unstable; at least one MP64 case
has `canEscape` answer false [I].
**Finish line:** each stratum's results in the Phase 2 report, CI on the head read by the planner, and a handoff, which
comes either at the end or at 180k.
**Abort conditions:** -3's; a run showing a case in two strata (-52); a stratum reported short.
**Notes:** Written by the planner session of -56 by hand. Next free ID -65.

**Kind:** decision
**ID:** 2026-10-28-65
**Timestamp:** 2026-11-06T03:37:03Z
**Continues:** 2026-10-28-61, 2026-10-28-64
**Title:** The route-ways path gets a differential test, in 5c (owner)
**Decision:** asked whether the port should prove that what `BRouterRouteWays` reads (way tags and geometry, straight from
the tile decoder) matches Java (-64), the owner, verbatim: "Add a differential test (Recommended)". The option read:
"Compare the port's decoded ways, tags and geometry against Java's around Forager's own test points (the drop-off fixture
and F0h's ends). It goes into 5c, which already compares tile decodes. A modest test, since the decoding is already ported."
- **Rejected:** "Defer until iOS needs it"; "Out of scope for iOS".
- **What it adds to 5c (the planner's calls, technical):**
  - the test drives the port through the same calls `BRouterRouteWays.read` makes (`BRouterRouteWays.kt:59-123` at Forager
    `a0535ece`), against the oracle making the same calls;
  - for each way near each point, it compares the tag map (from `getKeyValueDescription`), the decoded geometry in order,
    and each link's direction (`isReverse`);
  - the points are Forager's drop-off fixture (`app/src/test/resources/routing/dropoff/`) and F0h's ends, each pinned by
    SHA-256 at `a0535ece`, and only counts and digests are committed (E1);
  - it is seen failing first, by a revert that drops one tag;
  - **Phase 1 item B12's premise is corrected:** `getKeyValueDescription`'s text is now read by Forager. -42 already showed
    Kotlin/Native printing Java's `Float.toString` strings for every value tried, but this test is what checks it on
    this path.
- 5c is cut from `brouter-port-5b` (-61). Not yet dispatched: it waits until Linden's session 21 ends, so that only one
  heavy job runs at a time.
**Notes:** Written by the planner session of -56 by hand. Next free ID -66.

**Kind:** intent
**ID:** 2026-10-28-66
**Timestamp:** 2026-11-06T03:39:43Z
**Continues:** 2026-10-28-48, 2026-10-28-61, 2026-10-28-65
**Title:** Step 5c (waypoint matching) and the route-ways test start now, beside 1b, on `brouter-port-5b` (Plumb, session 6)
**Decision first:** the owner, verbatim: "Start 5c now". This overrules -65's call to wait for Linden's session 21.

**A wrong premise of the planner's, in -65 and in the option the owner chose:** "It goes into 5c, which already compares
tile decodes". Step 5c is **waypoint matching** (`WaypointMatcherImpl`, `MatchedWaypoint`; Phase 1 report on
`brouter-port`, line 1700). The tile decodes are compared in 5b (`GraphDecodeTest`, line 1699). The owner's ruling
(add the test) stands. Only its home changes: the route-ways test is written in this session, next to 5b's decode tests.

**The branch (the planner's call, technical):** the owner allowed pushes to `brouter-port` and `brouter-port-5b` only. So
5c is built on `brouter-port-5b`, from `98c01e6`, in a report of its own, `docs/audits/2026-11-06-brouter-port-step5c.md`.
That also satisfies -61, under which 5c builds on 5b. A separate `brouter-port-5c` would need the owner's word.

**Change:**
- **5c, as Phase 1's item I sets it** (line 1700):
  - port `WaypointMatcherImpl` and `MatchedWaypoint` into `commonMain`;
  - compare the matched waypoint (way, node pair, radius, direction) against the oracle for every generated endpoint
    of R. NT is not built yet, so its endpoints wait for it, and the report says so;
  - the revert drops the comparator's second line (`WaypointMatcherImpl.java:71`), with the prediction pushed first;
  - the `atan2` tie-break follows (a) to (c) as step 2b settled them.
- **The route-ways test, -65's planner's calls as written,** placed beside `GraphDecodeTest`:
  - points from Forager's `app/src/test/resources/routing/dropoff/` at `a0535ece` (pinned by SHA-256) and F0h's ends;
  - the revert drops one tag;
  - only counts and digests are committed.
- `CollectOutreachersTest` stays red as -61 ruled, and is reported at every CI read.
**Scope boundary:** writes only `brouter-port-5b`. It does not touch `brouter-port` (Linden is running there), `main`,
slayer8366/Forager or the tile release.
**Finish line:** both tests seen failing over stubs and then passing, both reverts biting as predicted, CI read by the
planner, and the report with a handoff.
**Abort conditions:** -3's, and any new red other than `CollectOutreachersTest`.
**Notes:** Written by the planner session of -56 by hand. Next free ID -67.

**Kind:** outcome
**ID:** 2026-10-28-67
**Timestamp:** 2026-11-06T03:55:55Z
**Continues:** 2026-10-28-64, 2026-10-28-65, 2026-10-28-66
**Title:** A wrong premise of the planner's in -64: the route-ways path needs code the port does not have; -65's ruling
goes back to the owner
**Observed:** Plumb Ledger stopped under -66's own stop, "a call in `BRouterRouteWays` the port lacks", at
`brouter-port-5b` `51c43b3`. **Checked by the planner at source:**
- `BRouterRouteWays.kt:84` (Forager `a0535ece`) calls `createMicroCache(..., null, null, true, null)`, with
  `hollowNodes` null. Java then takes the non-weaving decoder, `new MicroCache2(bc, ...)` (`OsmFile.java:139-140`,
  Forager's `third_party` copy) [R].
- The port throws there: `OsmFile.kt:140-143` on `brouter-port-5b` reads "unsupported: MicroCache2's decoder is not on T2's
  port list" [R]. Its comment states the premise that has now moved: "hollowNodes is null only under -DdisableDirectWeaving,
  which Forager never sets".
- `MicroCache2.<init>(StatCoderContext, ...)` appears on Phase 1's reach lists T0 and T1, but not T2 or T3, which the port
  was cut to (`03-reach/out/T*/reached-methods.tsv`: 1, 1, 0, 0) [C].
- Plumb also names, as not ported: `OsmNode(long)`, `parseNodeBody2`, and `MicroCache.getIdForIndex` [R, as reported].
  It sizes the gap at about 300 Java lines [I].
- **The planner's wrong premise:** -64's "No new code to port" matched by class and method name, not by overload or by
  argument, and -64 said so. The option the owner chose at -65 ("A modest test, since the decoding is already ported")
  rests on it. So -65's ruling goes back to the owner with the true cost.
- **5c's main part, as reported, not yet checked:** over the stubs, 1 test with 1 failure (`NotImplementedError`);
  forward, 3,068 of 3,068 R ends equal to the oracle; the revert's prediction is pushed (`51c43b3`) and its run was in
  progress. The planner checks it on the handoff.
**Notes:** Written by the planner session of -56 by hand. Next free ID -68.

**Kind:** decision
**ID:** 2026-10-28-68
**Timestamp:** 2026-11-06T04:00:26Z
**Continues:** 2026-10-28-65, 2026-10-28-67
**Title:** The route-ways path is ported, with its test (owner); replaces -65's ruling
**Decision:** asked again with the true cost (-67), the owner, verbatim: "Port it, with the test". The option read: "Add
MicroCache2's decoder, OsmNode(long), parseNodeBody2 and getIdForIndex in a step of their own (about 300 Java lines,
estimated), each seen failing over stubs first, then the route-ways differential test. The iOS app could then offer
way-to."
- **Rejected:** "Defer until iOS needs it"; "Out of scope for iOS".
- **The planner's calls (technical):**
  - **A step of its own, 5d,** on `brouter-port-5b` after 5c's handoff (one coder per branch at a time). Its own report.
  - **Its reach is measured first:** Phase 1's T-list method, from `BRouterRouteWays.read`'s calls, so the port list comes
    from data and not from -67's estimate. That also catches any overload -64 missed.
  - **Its tests:**
    - every micro-cache of each pinned tile decoded through `MicroCache2` by both engines, with a digest per cache, as
      5b does for `DirectWeaver`;
    - then -65's route-ways comparison, as written;
    - each is seen failing over stubs, and a revert bites, with its prediction pushed first.
  - Phase 1's T2 cut stands for `RoutingEngine`'s path. The new reach is added beside it, never in its place.
**Notes:** Written by the planner session of -56 by hand. Next free ID -69.

**Kind:** outcome
**ID:** 2026-10-28-69
**Timestamp:** 2026-11-06T04:02:14Z
**Continues:** 2026-10-28-61, 2026-10-28-66, 2026-10-28-67
**Title:** Step 5c (waypoint matching) ported: 3,068 of 3,068 R ends equal to the oracle; its revert bites; handed off
**Observed:** Plumb Ledger, session 6, `brouter-port-5b` `98c01e6` to `d1de74b` (four commits; clean tree). Report:
`docs/audits/2026-11-06-brouter-port-step5c.md`. It handed off at the planner's message after the watcher read 183,931
at 03:55:11Z.
- **Tests first:** `WaypointMatchTest` over the stubs read 1 failure (`NotImplementedError`) and 0 compile errors.
- **Forward:** 1,534 cases and 3,068 ends compared, 0 differing [R, as reported]. Each end compares the crosspoint, node
  pair, radius, `directionDiff`, `directionToNext`, name, type and the whole `wayNearest` list. The 350 unmatched ends
  and 3 oracle throws are compared too. `WaypointMatcherImpl.kt` is 252 lines [C].
- **The revert** (the comparator's second line becomes `0`, `WaypointMatcherImpl.java:71`): predicted at 1,364 to 2,718
  ends differing, observed at 2,105. **Checked by the planner** in `4-revert.txt`: 102 tests, 3 failures (that test and
  `CollectOutreachersTest`'s 2); restored identical to the saved copy, with the forward line present.
- **CI, read by the planner** (run 42159504256 on `3f691a7`, the same source as the head `d1de74b` by `git diff --stat`):
  `jvm` 102 tests, 2 failures, both `CollectOutreachersTest` (-61's known red); 0 ` e: ` lines in either log; both
  `compileKotlinIos*` ran; simulator 24 of 24 (**simulator-checked**); arm64 7 of 7, with the gate reading "OK: all 2
  classes ran".
- **Not covered:** `start()`'s island-pair branch (the set is always empty on this path); the dynamic-range branch
  (unreachable, since Forager passes `use_dynamic_range=0`); `WAYPOINT_TYPE_DIRECT`. NT's ends wait for NT.
- **A defect, owed first in 5d:** `WaypointMatchEngines.differingFields` splits on spaces, so a failure message carries
  fragments of coordinates. Not a problem while the test passes, but a red run would print them (E1).
- **Calls beyond scope, accepted:** R's seed companions made `internal`; exceptions compared as outcomes;
  `matchWaypointsToNodes` and `preloadPosition` ported here, as `NodesCache`'s header assigned them.
**Notes:** Written by the planner session of -56 by hand. Next free ID -70.

**Kind:** intent
**ID:** 2026-10-28-70
**Timestamp:** 2026-11-06T04:02:14Z
**Continues:** 2026-10-28-68, 2026-10-28-69
**Title:** Step 5d: the route-ways path (`MicroCache2` and its callers), reach measured first, then the test (Plumb, session 7)
**Change** (on `brouter-port-5b`, from `d1de74b`; report `docs/audits/2026-11-06-brouter-port-step5d.md`):
1. Fix -69's `differingFields` defect, so that only field names are printed. It is seen with a failing message first.
2. **Reach:** measure the methods `BRouterRouteWays.read` reaches (`BRouterRouteWays.kt:59-123` at Forager `a0535ece`),
   by Phase 1's T-list method, on a harness that makes those calls. List what is not yet ported, as a new reach file
   beside T2. **Stop and report the count before writing any port code** if it is over 600 Java lines, twice -67's
   estimate.
3. **Port** it into `commonMain`, with stubs first and a `MicroCache2` decode test: every micro-cache of each pinned
   tile, decoded through `createMicroCache(..., hollowNodes = null)` by both engines, compared by a digest per cache.
   Seen failing over the stubs, then passing. A revert (one field decoded wrong) bites, with its prediction pushed first.
4. **The route-ways test,** as -65 sets it. Forager's `app/src/test/resources/routing/dropoff/` at `a0535ece` is pinned by
   SHA-256 and fetched as `tools/fetch-f0h.sh` fetches F0h. F0h's ends are included. Only counts and digests are
   committed. Seen failing first, and a revert that drops one tag bites.
**Scope boundary:** writes only `brouter-port-5b`. It leaves T2's list and `RoutingEngine`'s path untouched.
**Finish line:** pushed, CI read by the planner, the report with its handoff.
**Abort conditions:** -3's; any new red besides `CollectOutreachersTest`; the 600-line stop.
**Notes:** Written by the planner session of -56 by hand. Next free ID -71.

**Kind:** decision
**ID:** 2026-10-28-71
**Timestamp:** 2026-11-06T04:11:45Z
**Continues:** 2026-10-28-51, 2026-10-28-52, 2026-10-28-61, 2026-10-28-64
**Title:** PM5 is short at 55 of 59 and stands, as E4 says; the first three targeted strata full and stable; `canEscape`
answers false on MP64
**Observed (Linden, session 21, as reported; checked on its handoff):**
- **DL-lat** 59 of 59 at draw 501, and **DL-lon** 59 of 59 at draw 326: 0 unstable each, and the selection's R1/R2
  equal E3's in 59 of 59.
- **MP64** 59 of 59 at draw 86: 0 limit, 0 panic, 0 unstable, 59 of 59.
- **`canEscape` (-61)** answers false in all 59 MP64 cases: 576,094 of 2,366,007 calls per run, the same in every
  regime, counted on a scratch copy. The counting run's tables equal the plain run's, apart from R1's milliseconds. So
  -60's coverage assertion can be met on MP64's data.
- **The shared M5 sequence** (seed 20261204, `memoryclass` 5, `hiking-mountain`): 1,180 draws, 978 of them routed.
  - **ML5** filled 59 of 59 at draw 246, and 54 of the 59 also showed panic mode (-52's count).
  - **PM5 took 55** by draw 1,180, the cap.
  - Rejected: 202 before routing; 232 as "ML5 already full"; the rest "neither", mostly routes with no panic.
  - E3's six regimes on both strata were still running.
**Decision (the planner's, technical; the stop was the planner's own at -64, and Phase 1's text settles it):**
- **PM5 stands at n = 55, reported short.** Phase 1's E4 (report at `20e1f92`, line ~908): "A stratum that cannot be filled
  from 20 times its size in candidates is reported short, with its n and its own bound (E5). It is never filled from
  another generator."
- **Its own bound:** a clean run bounds its rate below 1 − 0.05^(1/55) = **5.30%** [C], against 4.95% at 59 (E5).
- No change to the cap, the seed or the generator. The expected values use 55 for PM5.
- **ML5 is mostly panic-then-limit** (54 of 59). The report names that as a characteristic of the stratum, so that no
  figure from ML5 is later read as one about the limit without panic.
**Notes:** Written by the planner session of -56 by hand. Next free ID -72.

**Kind:** outcome
**ID:** 2026-10-28-72
**Timestamp:** 2026-11-06T04:26:22Z
**Continues:** 2026-10-28-67, 2026-10-28-69, 2026-10-28-70
**Title:** 5d, items 1 and 2: the message defect fixed; the route-ways reach measured, with 440 Java lines unported (under
the 600 stop); handed off at 180k
**Observed:** Plumb Ledger, session 7, `brouter-port-5b` `d1de74b` to `d293fd4` (two commits; clean tree). It handed off at
the planner's message after the watcher read 181,696 at 04:19:13Z. Report: `docs/audits/2026-11-06-brouter-port-step5d.md`.
- **Item 1:** `differingFields` now splits at its known keys and prints names only. On a scratch revert, never committed,
  the message held 33 coordinate fragments before the fix and 0 after, with the same 2,105 ends differing [R, as
  reported]. The committed evidence (`1-defect.txt`) holds counts only: the planner found no coordinate-like number in it.
- **Item 2, the reach** (`03-reach/out/RW/`, beside T2):
  - from `BRouterRouteWays.kt:59-123`, with a tier of its own (W) for that call's null arguments: 141 methods, all 15
    cuts used;
  - under JaCoCo, 129 ran and 0 ran unreached [R, as reported];
  - 27 classes and 87 JDK members, all already in T2's `jdk-use.txt`.
- **Not ported** (`not-ported.tsv`; **checked by the planner:** 29 rows, being 27 methods plus 2 rows for branches that
  throw, at 0 lines; the `java_lines` column sums to **440**):
  - `MicroCache2`, 251 lines;
  - `ByteDataWriter`, 53 lines;
  - `parseNodeBody2`, 37 lines;
  - `LinkedListContainer`, 34 lines, with no file in the port;
  - `ByteArrayUnifier.unify`, 28 lines, which throws today;
  - `ByteDataReader`, 26 lines;
  - `expandId` and `getIdForIndex`, 7 lines;
  - `OsmNode(long)`, 4 lines;
  - and the two branches that throw, `OsmFile.kt:140-143` and `OsmNode.kt:96-100`.
- **-67's estimate of about 300 lines was low:** the measured gap is 440, under -70's stop at 600.
- **A finding:** `parseNodeBody2` is on T2's reach list but was never ported (`OsmNode.kt:15`). So the not-ported list was
  read from the port's own source, not from T2. Whether an overload under the same name hides a gap is checked only for
  `addLink` [?].
- **CI, read by the planner** (run 42161896448 on `d293fd4`): `jvm` 102 tests, 2 failures, both `CollectOutreachersTest`
  (-61); 0 ` e: ` lines in either log; both `compileKotlinIos*` ran; simulator 24 of 24 (**simulator-checked**); arm64 7
  of 7, with the gate reading "OK: all 2 classes ran". So `WaypointMatchTest` passes with the fix.
**Next:** items 3 to 6 of -70 (the port, the `MicroCache2` decode test, the route-ways test, CI), in Plumb's session 8.
**Notes:** Written by the planner session of -56 by hand. Next free ID -73.

**Kind:** outcome
**ID:** 2026-10-28-73
**Timestamp:** 2026-11-06T04:31:03Z
**Continues:** 2026-10-28-61, 2026-10-28-64, 2026-10-28-71
**Title:** The four targeted sequences run: DL-lat, DL-lon, MP64 and ML5 full, PM5 short at 55; 291 cases, all stable;
`canEscape` false on all 59 MP64 cases; CI green
**Observed:** Linden Caliper, session 21, `brouter-port` `3688c9f` to `729e447` (fifteen commits, docs only: `git diff
--name-only` shows no path outside `docs/audits/`). Every prediction was pushed before its run. Logs: `step1b/17-*` to
`20-*`. It handed off at the planner's message after the watcher read 181,272 at 04:25:58Z.
- **Each stratum** (`hiking-mountain`, -58's tuple, Temurin 21.0.13+9):
  - **DL-lat:** 59 of 59 at draw 501.
  - **DL-lon:** 59 of 59 at draw 326.
  - **MP64:** 59 of 59 at draw 86.
  - **ML5:** 59 of 59 at draw 246.
  - **PM5:** 55, short at the cap, as ruled at -71.
- **Checked by the planner** in the five `stability.tsv` files: 291 cases, all "stable", and no id appears twice. The
  selection's R1/R2 equals E3's in every case, and "R2's tuple differs from R1's" never occurred [R, as reported].
- **ML5 is mostly panic-then-limit** (54 of 59). Over the 978 routed M5 draws, panic without the limit came up only 55
  times, and all 55 went to PM5. That is why PM5 is short [R, as reported].
- **`canEscape` (-61), checked by the planner** in `19-mp64-canescape-summary.txt`:
  - R3, R4 and R5 each read 576,094 false of 2,366,007 calls, and R1R2's JVMs read exactly twice that (1,152,188 of
    4,732,014);
  - all 59 cases answered false at least once;
  - the counting copy is the jar's class plus a counter [R, as reported], and run B's tables equal plain run A's apart
    from R1's milliseconds;
  - the first false nodes are listed for cutting a 5b region (not tried).
- **CI, read by the planner** (run 42162574478 on `729e447`, `success`): `jvm` 93 tests, 0 failures; 0 ` e: ` lines in
  either log; both `compileKotlinIos*` ran; simulator 23 of 23 (**simulator-checked**); arm64 7 of 7, with the gate
  reading "OK: all 2 classes ran". `E3MemoryTest`'s task fails by design on PM5's short assertion (`E3MemoryTest.kt:49`);
  it is outside `jvmTest`.
- **Wrong premises:**
  - -64's prediction that every stratum fills (PM5 did not);
  - the planner's search hint in -64's dispatch, `2026-10-28-6`, also matches -60 to -69;
  - Linden's Forager checkout is at `d6e2a041`, not `a0535ece`. Only `third_party/brouter` was read, and -64 found that
    unchanged.
- **Unverified:** why 84% of DL-lat's draws and 79% of DL-lon's were rejected on bearing before routing; R1's and R2's
  `canEscape` counts separately within one JVM (inferred equal from the doubled total).
**Next for 1b:** NT, LB, CR and FK, which are not built. NT and CR need the first coverage reader, seen failing first
(-50). Then the expected values, with PM5 at n = 55.
**Notes:** Written by the planner session of -56 by hand. Next free ID -74.

**Kind:** outcome
**ID:** 2026-10-28-74
**Timestamp:** 2026-11-06T04:44:19Z
**Continues:** 2026-10-28-65, 2026-10-28-70, 2026-10-28-72
**Title:** 5d item 3: `MicroCache2`'s decoder ported, 25,072 caches equal to the oracle; a silent stub left on a live
path, to be undone first; F0h's tile wired on this branch
**Observed:** Plumb Ledger, session 8, `brouter-port-5b` `d293fd4` to `4345f71` (four commits; clean tree). It handed off at
the planner's message after the watcher read 180,163 at 04:35:47Z.
- **The port:** 467 lines added and 9 removed in 7 `commonMain` files (`git diff --stat`, checked by the planner):
  - `MicroCache2`'s constructor, `readUnified`, `expandId` and `shrinkId`;
  - `LinkedListContainer`;
  - 5 `ByteDataReader` and 8 `ByteDataWriter` methods;
  - `MicroCache.getIdForIndex` and `expandId`.

  Overloads were checked by signature against `reached-methods.tsv` [R, as reported].
- **`MicroCache2DecodeTest`:**
  - over the stubs: 5 of 5 failed, each at cache 0, `[size]`, with 0 compile errors;
  - forward: 5 of 5 passed, with **25,072 caches** compared field by field over the five pinned tiles [R, as reported].
  - Its revert (one link's `dlat` sign flipped) is predicted and pushed (`ae97c7b`) but **not run**.
- **CI, read by the planner** (run 42163527776 on `4345f71`): `jvm` 107 tests, 2 failures, both `CollectOutreachersTest`
  (-61); 0 ` e: ` lines in either log; both `compileKotlinIos*` ran; simulator 24 of 24 (**simulator-checked**); arm64 7
  of 7, with the gate reading "OK: all 2 classes ran".
- **Unverified:** whether the sample reaches the `featureId == 13` escape or `injectSize`'s shift branch.

**A silent stub on a live path, against Forager's CLAUDE.md** ("An unsupported feature or capability returns an explicit
'unsupported,' never a fabricated plausible value"):
- `OsmNode.kt:110` is `fun parseNodeBody2(...) {}`, an empty body, and `parseNodeBody` calls it at `:103` [R, read by
  the planner]. So at `4345f71` a `MicroCache2` node decodes as nothing, where before it threw.
- Plumb disclosed it, and only tests reach it today.
- **The planner's call:** the next session's first act is to make the stub throw a named "unsupported", seen first in a
  test, before item 4 ports the method. No stub on a live path ever returns an empty value again: stubs throw
  `NotImplementedError`, as 5c's did.

**Plumb's open question, answered (the planner's call, technical):**
- F0h's tile is not wired on `brouter-port-5b`. It is fetched here with the same pin as on `brouter-port`, by
  `tools/fetch-pinned.sh` (as `fetch-f0h.sh` does there).
- **Rejected:** testing F0h's ends on F0's fixture. -65 names F0h's ends, and F0's square is a different extract, a week
  older and without heights (-56), so the comparison would be about other data.

**A small wrong premise in -70:** `tools/fetch-pinned.sh` already exists here. Item 4 adds a `tools/fetch-dropoff.sh` that
calls it.
**Notes:** Written by the planner session of -56 by hand. Next free ID -75.

**Kind:** decision
**ID:** 2026-10-28-75
**Timestamp:** 2026-11-06T05:03:50Z
**Continues:** 2026-10-28-68, 2026-10-28-74
**Title:** Step 2a's `ByteArrayUnifierTest` refusal is replaced by a differential check, because -68 moved its premise
**Observed:** Plumb Ledger, session 9, stopped on a red besides `CollectOutreachersTest`. Run 42165333186 on `a6eb12b`:
`jvm` 109 tests, 3 failures, the third being `ByteArrayUnifierTest.constructsAndRefusesUnifyAsUnsupported`
(`ByteArrayUnifierTest.kt:16`) [R, as reported].
- The cause is 5d's port of `ByteArrayUnifier.unify` (`7072d5b`), as -70 and -74 directed.
- Plumb's local runs were filtered to three classes, so they missed it.
**Decision (the planner's, technical):**
- **The test's premise is in its own header** (`3cd61ad`, step 2a): `unify` "fails as a named 'unsupported'" because it
  was "unkept", off T2's port list. -68's owner ruling put it on the port list.
- **So this is a planned change of behaviour, not a silenced test.** Forager's CLAUDE.md forbids weakening a test to
  reach green. This replaces an assertion whose premise no longer holds with a stronger one.
- **The replacement:**
  - the constructor check is kept;
  - a differential check against `btools.util.ByteArrayUnifier` compares the returned bytes and the instance-reuse
    pattern over a fixed input sequence, with `validateImmutability` true and false;
  - a one-line revert must make it fail, with the prediction pushed first;
  - the commit and the 5d report cite `3cd61ad` and this entry.
- **Practice from now on:** a coder runs the full `:brouter:jvmTest` before every push that touches `commonMain`.
**Notes:** Written by the planner session of -56 by hand. Next free ID -76.

**Kind:** outcome
**ID:** 2026-10-28-76
**Timestamp:** 2026-11-06T05:24:42Z
**Continues:** 2026-10-28-65, 2026-10-28-68, 2026-10-28-70, 2026-10-28-74, 2026-10-28-75
**Title:** Step 5d done: the route-ways path ported and equal to the oracle on both squares; every revert bites; the
silent stub undone; CI shows only -61's known red
**Observed:** Plumb Ledger, session 9, `brouter-port-5b` `4345f71` to `b65548f` (eleven commits; clean tree; `b65548f`
is docs only, by `git diff --stat`). Report: `docs/audits/2026-11-06-brouter-port-step5d.md`.
- **The silent stub (-74) is undone** (`3888c07`): `ParseNodeBody2StubTest` failed over the empty body ("completed
  successfully"), then passed with the stub throwing a named "unsupported".
- **The decode revert:** predicted at `ae97c7b` as 5 of 5 failing at cache 0, field `[link dlat]`, and observed exactly
  so. 0 ` e: ` lines; restored identical [R, as reported].
- **The route-ways test** (`RouteWaysTest`):
  - over the stubs: 2 of 2 failed on the stub's "unsupported";
  - forward: 2 of 2 passed, after porting `OsmNode(long)`, `parseNodeBody2` and `ByteArrayUnifier.unify`;
  - **compared:** the drop-off square (15 points, 120 links, 165 tags) and F0h's square (16 ends, 4,858 links, 15,596
    tags);
  - **the revert** (drop `highway` at `BExpressionContext.kt:131`): 2 of 2 failed at `[tags]`, as predicted. **Checked
    by the planner** in `4-routeways-revert.txt`: 0 ` e: `, restored identical, forward line present once.
- **The pins:** Forager's drop-off rd5 (4,475 bytes, `a332b322…d8f7`) and `points.json` (1,103 bytes, `2872493a…6b5a`)
  at `a0535ece`, through `tools/fetch-dropoff.sh`. F0h's tile is wired here by `tools/fetch-f0h.sh`, copied unchanged
  from `brouter-port`.
- **`ByteArrayUnifierTest`** was replaced under -75's five conditions (`9de9d48`). Its revert failed 2 of 3 as predicted,
  and the full `:brouter:jvmTest` ran before the push [R, as reported].
- **CI, read by the planner** (run 42167090127 on `1797412`, the last code head): `jvm` 111 tests, 2 failures, both
  `CollectOutreachersTest` (-61); 0 ` e: ` lines in either log; both `compileKotlinIos*` ran; simulator 24 of 24
  (**simulator-checked**); arm64 7 of 7, with the gate reading "OK: all 2 classes ran".
- **Bookkeeping:** the report's "Items 5 and 6: not started" refers to the planner's session-7 dispatch numbering, where 5
  was the CI read and 6 the report. Both are done. -70 itself has four items, and all four are done. **5d is closed.**
- **Unverified (a check that never saw its other value):**
  - on both squares, no described link is reverse and Forager's dedupe drops none, on the oracle and the port alike. So
    the route-ways test has never seen `isReverse` true on this path [C, as reported];
  - `MicroCache2DecodeTest` does compare `isReverse` per link over 25,072 caches (-74), which covers the decoder;
  - whether any Forager square has a reverse described link is [?].
- **Also unverified:** the test copies Forager's cell, square and `tagsOf` arithmetic from `a0535ece`, so a later Forager
  change would not show here. The crc-crosscheck branch of `unify` is reached only by `ByteArrayUnifierTest`.
**Notes:** Written by the planner session of -56 by hand. Next free ID -77.

**Kind:** intent
**ID:** 2026-10-28-77
**Timestamp:** 2026-11-06T05:30:46Z
**Continues:** 2026-10-28-50, 2026-10-28-51, 2026-10-28-58, 2026-10-28-73
**Title:** 1b's last strata, NT, CR, LB and FK, starting with the first coverage reader (Linden, session 22); PR #3
**Decision first:** asked "should Linden start NT, LB, CR and FK" and "should I open PR #3", the owner, verbatim: "1 and 2
yes".

**Change** (on `brouter-port`, in `jvmTest`, `tools` and `docs` only), as Phase 1's E4 sets the strata (report at `20e1f92`,
the E4 table):
1. **The coverage reader first.** It is the first code to read R6's JaCoCo `.exec` files (-51). -50's rule applies: coverage
   counts as reach evidence only where R6's tuple equals R1's. It is seen failing first, on a case known not to reach a
   line.
2. **NT, near-ties** (59): a start nearest a junction node, and an end placed so that the two ways' `directionDiff`s
   differ by at most 4 ulps on the oracle's JVM. Reach: coverage of the comparator's second line
   (`WaypointMatcherImpl.java:71`).
   - NT's ends then go into 5c's `WaypointMatchTest` (-69 left them owed). That is done on `brouter-port-5b` later, not
     in this session.
3. **CR, crossing** (59): R's generator with E's crossing profile. Reach: coverage of `StdPath.java:101-103` and
   `:105-113`.
4. **LB, latitude bands** (59): start and end in the (c) rows, as step 2b set them. Reach: the route lies in the band.
5. **FK, failure kinds** (110): constructed, 10 per kind, as E4 lists them. Reach: the oracle's message is in the kind's
   family.

Each stratum runs under -58's tuple, on `hiking-mountain` unless E4 names another profile (CR's crossing profile, and
FK's own), with each generator and prediction pushed before its run.
**Scope boundary:** writes only `brouter-port`. It does not touch `brouter-port-5b`, `main`, Forager or the tile release.
**Baseline:** `brouter-port` `729e447` (CI green, -73).
**Prediction (planner):** NT, CR and LB fill within the cap; FK's 110 are constructed and reach their families; 0
unstable in every stratum.
**Finish line:** each stratum in the Phase 2 report, CI read by the planner, and a handoff. It will likely take more than
one session. **Then the expected values** (E5's per-stratum sizes, with PM5 at 55), in a session of their own.
**Abort conditions:** -3's; a stratum short; instability over 5%; a coverage reading where R6's tuple differs from R1's.
**Notes:** Written by the planner session of -56 by hand. Next free ID -78.

**Kind:** outcome
**ID:** 2026-10-28-78
**Timestamp:** 2026-11-06T05:49:56Z
**Continues:** 2026-10-28-50, 2026-10-28-77
**Title:** The coverage reader built and seen failing first; NT built, not run; a wrong premise in -77 on NT's reach; a model
name in one commit
**Observed:** Linden Caliper, session 22, `brouter-port` `729e447` to `00b9886` (two commits). It handed off at the planner's
message after the watcher read 182,861 at 05:43:41Z, about 12 minutes in.
- **The coverage reader** (`E3Coverage.kt`; `E3CoverageRuleTest`, 2 tests, in `jvmTest`; `strata/E3CoverageTest.kt`):
  - **The "not covered" reading:** on F0's `inside-long`, with R6's tuple equal to R1's, CR's lines (`StdPath.java:101-103`,
    `:105-113`) read not covered, as predicted, since they sit in `if (rc.bikeMode)` on a foot profile. The control line
    `RoutingEngine.java:177` read covered [R, as reported].
  - **The bite:** a reader edited to call every line covered failed with the test's own message. 0 ` e: ` lines; restored
    by `cmp` [R, as reported].
- **NT:** the generator is built and compiles; nothing has run. -78's planner's call below sets its reach.
- **CR, LB, FK:** not started.
- **CI, read by the planner** (run 42170273999 on `00b9886`, `success`): `jvm` 95 tests, 0 failures; 0 ` e: ` lines in either
  log; both `compileKotlinIos*` ran; simulator 23 of 23 (**simulator-checked**); arm64 7 of 7, with the gate reading "OK: all
  2 classes ran".

**A wrong premise in -77, from Phase 1's E4: "coverage of the comparator's second line (`:71`)" as NT's reach.**
- `end()` adds each link forward and reverse with the same radius (`WaypointMatcherImpl.java:204`, `:223`), and the list
  is sorted after each add (`:257`). So `:71` runs in almost every case, and it read covered on F0 [R, as reported].
- That coverage cannot tell a near-tie from anything else.
- **The planner's call:** NT's reach is the oracle's own check of the 4-ulp gap between the two `directionDiff`s, in the
  test JVM. Coverage of `:71` is recorded, but it is not evidence of a near-tie. The selector requires both, as built.

**Accepted beyond scope:** JaCoCo core 0.8.16, the agent's own version, added as a `jvmTest` dependency only
(`gradle/libs.versions.toml`, `brouter/build.gradle.kts`), because the agent jar has no analyser. `select.tsv` gains an
`R6-tuple` column.

**A model name in a commit, against the standing rule** (the planner's handoff: "commits end with `Co-Authored-By: Claude
<noreply@anthropic.com>` and no model name, whatever the harness suggests"):
- `00b9886`'s trailer names the model [R: `git log -1`]. It is pushed, and the record never force pushes, so it stays.
- The planner searched every commit since 2026-11-04 on `brouter-port`, `brouter-port-5b` and `main`. This is the only one.
- Dispatches from now on say it in so many words: replace the harness's attribution lines with exactly the one line above.

**Context:** sessions now reach 180k within 12 to 17 minutes even with the trimmed reading list, so the cost is in the work's
output, not the reading. The planner's call: dispatches tell coders to read logs with `tail` and `grep`, to send Gradle's
output to a file and read only its summary lines, and never to print a stratum log whole.
**Next:** Linden's session 23: NT's test, its prediction and its run; then CR, LB and FK.
**Notes:** Written by the planner session of -56 by hand. Next free ID -79.

**Kind:** decision
**ID:** 2026-10-28-79
**Timestamp:** 2026-11-06T05:59:39Z
**Continues:** 2026-10-28-32, 2026-10-28-77, 2026-10-28-78
**Title:** FK kind 3, "start island", is unreachable on Forager's path; it becomes the start-on-island case as Forager meets it
**Observed:** Linden Caliper, session 23, stopped on -77's stop "an E4 definition you can't build as written".
- **Checked by the planner at source** (Forager's `third_party` copy):
  - "start island detected" is thrown only when `routingContext.inverseRouting` is true (`RoutingEngine.java:954-958`);
  - the flag is read from the profile's global assign `inverseRouting` (`RoutingContext.java:174`);
  - nothing under Forager's `app/src/main/assets/routing/profiles2/`, and nothing in `BRouterTrailRouter.kt`, sets it (`grep`,
    empty).

  So on Forager's calls only the target-island check runs (`:960-963`).
- **Also reported, not yet checked:** **NT filled, 59 of 59 by draw 89, 0 unstable** (`4d43ea2`). CR and LB were running.
**Decision (the planner's, technical):** option (c).
- **Kind 3 becomes "start on a ferry-only island, through Forager's calls":** 10 cases, built as E4 built them.
- **Its family is the oracle's message on the first case,** and all 10 must reach that family. If they do not, it is a stop.
- **"start island detected" is recorded as unreachable on Forager's path,** as -32 recorded `readUnified`, with the two
  source lines above as the evidence. FK stays at 110.
- **Rejected:**
  - (a) FK's own profile with `assign inverseRouting = true`: a call beyond Forager's, testing a path the app never takes;
  - (b) dropping kind 3: a start on an island is a real situation for a walker, and this is the one case that shows what
    Forager meets there.
- **A wrong premise in Phase 1's E4,** carried into -77: it listed kind 3 by the engine's message, without checking that
  Forager's configuration reaches it. "Check reachability before measuring behaviour" (Forager's CLAUDE.md) applies.
**Notes:** Written by the planner session of -56 by hand. Next free ID -80.

**Kind:** decision
**ID:** 2026-10-28-80
**Timestamp:** 2026-11-06T06:06:11Z
**Continues:** 2026-10-28-77, 2026-10-28-79
**Title:** FK's families for kinds 4, 5, 7 and 8; a cut-short or flipped tile can route without error, so three extra cases
are named; NT and CR full
**Observed (Linden, session 23, as reported; nothing committed for FK yet, probes in scratch):**
- **CR** 59 of 59 at draw 83, 0 unstable, 0 coverage stops. **NT** 59 of 59 by draw 89, 0 unstable (-79). LB was running.
- **The FK probes, on the oracle jar under Temurin with Forager's params:**
  - **Kinds 1 and 2:** 15 of 15 give "not mapped".
  - **Kind 6:** 12 of 12 give "datafile W80_N40.rd5 not found".
  - **Kind 3 (-79's start on an island):** "no track found at pass=0", on 11 islands.
  - **Kind 4:** "target island detected" from 7 of 32 guessed islands.
  - **Kind 5:** the matcher snaps only to ways the profile can use, so E4's construction gives "not mapped" or "no track
    found".
  - **Kind 7, cuts of E170_S50:**
    - at 200, 201, dataEnd/2, dataEnd−1 and dataEnd+1: "file of size N too short, should be 1061704";
    - **at dataEnd: a raw "Index -399101066 out of bounds for length 65636"**;
    - **at 1, 100, 199 and size−1: the route succeeds, with no error.**
  - **Kind 8, flips:**
    - bytes 37, 100, 150 and 199: "top index checksum error";
    - byte 1: "lookup version mismatch";
    - **bytes 203 to 219, in the sub-index: the route succeeds.**
**Decision (the planner's, technical):**
- **Kind 4:** islands drawn by a seeded generator, counted, capped at 200 draws (20 × 10). Family: "target island
  detected".
- **Kind 5 keeps E4's intent,** and Linden's proposal is rejected because it would duplicate kinds 3 and 4: an end on a way
  the profile cannot use, with no usable way within 250 m. Family: "not mapped", the same as kind 2. **The report states
  that on Forager's path kinds 2 and 5 share a family, as kinds 3 and 5 would have.**
- **Kind 7:** family "file of size * too short, should be *", with the 10 cuts chosen to reach it.
- **Kind 8:** family "top index checksum error", with flips in bytes 8 to 199.
- **Three named extra cases outside FK's 110,** compared on outcome, because the port must match what Java does with a
  damaged tile and none of these is padding:
  - **FK7-x1,** the cut at dataEnd, compared by exception class. Its message text is the JVM's, so **step 10 must read it
    on iOS**, where Kotlin/Native's wording may differ [I].
  - **FK7-x2,** one cut-short tile that routes with no error.
  - **FK8-x1,** a sub-index flip that routes with no error.
- **Kinds 9 to 11** (time limit, cancel modes) need harness changes and go to the next session.
**For the owner, information, not a decision:** Java's BRouter can route on a tile that is cut short or has a flipped byte
in its sub-index, without reporting an error. That is the engine Android runs. Whether Forager's own check before routing
(`RoutingDataFolder.headerProblem`, `RoutingDataFolder.kt:48-53` at Forager `e2969abe`, cited on `brouter-port` at
`Route.kt:51`) catches these copies is [?], not read here.
**Notes:** Written by the planner session of -56 by hand. Next free ID -81.

**Kind:** outcome
**ID:** 2026-10-28-81
**Timestamp:** 2026-11-06T06:11:04Z
**Continues:** 2026-10-28-77, 2026-10-28-78, 2026-10-28-79, 2026-10-28-80
**Title:** NT, CR and LB full, 59 of 59 each, all stable; FK probed, not built; CI green
**Observed:** Linden Caliper, session 23, `brouter-port` `00b9886` to `ffa06ab` (three commits; clean tree; no model name in
any trailer, by `git log` and `grep`, checked by the planner). It handed off at the planner's message after the watcher read
183,184 at 06:05:47Z. Logs: `step1b/23-*` (NT), `24-*` (CR), `25-*` (LB), `26-fk-probes/`.
- **Checked by the planner:** `23-nt-stability.tsv`, `24-cr-stability.tsv` and `25-lb-stability.tsv` each read 59 "stable".
  `26-fk-probes/counts.txt` holds no coordinate-like number.
- **Each stratum, as reported:**
  - **NT** (seed 20261205): full at draw 89, 30 rejected before routing. R1 kinds: 56 routes, 2 target island, 1
    to-position not mapped. Every taken case reads a gap of 0 ulps, an exact tie. `:71` is covered in 59 of 59, recorded
    only (-78).
  - **CR** (20261206): full at draw 83, 24 rejected. All 59 route.
    - **The tiles carry `estimated_crossing_class`,** which settles Phase 1's open question.
    - By tile: E170_S50 19, W70_N40 13, W75_N35 11, W75_N40 16.
  - **LB** (20261207): full at draw 99, 40 rejected. All 59 route.
    - **Only 5 of step 2b's 13 (c) rows lie inside the tiles,** not Phase 1's "12".
  - In all three, R2 equals R1 on every routed case, and the selection's R1/R2 equal E3's in 59 of 59.
- **CI, read by the planner** (run 42172432910 on `ffa06ab`, `success`): `jvm` 95 tests, 0 failures; 0 ` e: ` lines in either
  log; both `compileKotlinIos*` ran; simulator 23 of 23 (**simulator-checked**); arm64 7 of 7, with the gate reading "OK: all
  2 classes ran".
- **The planner's call on Linden's open point:** NT counts any outcome kind. The tie is broken in matching, before
  routing, and the route's outcome is compared whatever it is.
- **Unverified:** why one NT case's R1 reads "to-position not mapped" when the generator's own matching of the same ends
  did not throw [?].
- **Wrong premises:** Phase 1's "12 in-tile rows" for LB (5 under step 2b's definition), with -78 to -80 already recorded.
**1b now stands at:** F0, F0h, R, DL-lat, DL-lon, MP64, ML5, PM5 (short, 55), NT, CR and LB, all run and stable. FK is next,
under -79 and -80, including kinds 9 to 11's harness changes. Then the expected values.
**Notes:** Written by the planner session of -56 by hand. Next free ID -82.

**Kind:** decision
**ID:** 2026-10-28-82
**Timestamp:** 2026-11-06T06:25:46Z
**Continues:** 2026-10-28-80, 2026-10-28-81
**Title:** FK kind 5 short at 9 of 10 in construction; its start and end are drawn separately and it is rerun alone
**Observed (Linden, session 24, as reported):**
- FK's generator and prediction were pushed at `49e2711` before the run.
- Kinds 1, 2, 3 and 6 reached 10 of 10. **Kind 4** filled 10 of 10 at draw 74; of 64 rejections, 41 were "to-position not
  mapped" and 23 routed.
- **Kind 5 came out short, at 9 of 10 after 200 draws.** That cap was Linden's own: -80 set 200 for kind 4 only.
- Kind 5's rejections, **all before routing:** 111 failed the 300 m check, 68 had a start car-fast cannot use, and 12 fell
  at a tile edge. No oracle outcome was used in any of them.
**Decision (the planner's, technical):** Linden's option (a).
- **Why:** FK is "constructed, not sampled… no rate is claimed" (Phase 1's E5). E4's fill-or-short rule guards against
  choosing cases by their outcome, and every one of these rejections came before any outcome existed.
- **The rule for kind 5:**
  - the start and the end are drawn separately;
  - the cap is 400 draws, with every rejection counted by reason;
  - nothing is selected on the oracle's outcome;
  - the change and its prediction are pushed first, then kind 5 is rerun alone through E3's six regimes;
  - all 10 must reach kind 5's family ("not mapped", -80), or it is a stop.
- **The report keeps the first run's 9 of 10 and its counts,** and names the 200 cap as Linden's choice.
- **Rejected:** (b), keeping 9. A constructed set short by construction is a harness limit, not a finding about the oracle.
**Notes:** Written by the planner session of -56 by hand. Next free ID -83.

**Kind:** outcome
**ID:** 2026-10-28-83
**Timestamp:** 2026-11-06T06:27:53Z
**Continues:** 2026-10-28-54, 2026-10-28-82
**Title:** FK built and running; a committed file of OSM-derived coordinates found, against E1, put to the owner
**Observed:**
- **Linden Caliper, session 24:** `brouter-port` `ffa06ab` to `db0751a` (two commits; clean tree). It handed off at the
  planner's message after the watcher read 183,592 at 06:25:28Z.
  - FK's generator, its test and the harness changes for kinds 9 to 11 were pushed at `49e2711`, with the prediction,
    before the run and after a full `:brouter:jvmTest` (95, 0 failures).
  - The run was still going at handoff (Gradle PID 16430).
  - -82's kind 5 rerun is written as a scratch patch, not yet compiled or pushed, so as not to disturb the running JVMs.
  - CI on `49e2711` and `db0751a` not read.
- **A committed file against Phase 1's E1** ("No coordinates are committed", Phase 1 report at `20e1f92`, E1):
  - `step1b/11-r-sequence-before-edit.tsv`, committed in Linden's session 19 (`82d310a`, 2026-10-30), holds 1,534 rows,
    each with a start and an end in degrees to six places [R, read by the planner].
  - The ends are OpenStreetMap node positions from the pinned tiles, each moved a random 0 to 100 m and rounded
    (`RGenerator.kt:12`, `TileNodes`) [R].
  - The planner scanned every committed `.tsv` under `step1b/` for coordinate-like values. This is the only file that
    has any.
  - **The test pins the sequence by SHA-256** (`E3RSequenceTest`, -54), so the file is not needed to check it.
  - **It is pushed, and the record never force pushes.** A later commit can remove the file from the tree, but not from
    history.
- **FK's own `.tsv` files carry coordinates** (Linden). Linden was told to strip them before committing anything.
**For the owner:** whether the file is removed, and whether an OpenStreetMap (ODbL) notice is added for the copy that stays
in history. Under -3's abort conditions, a licence question beyond MIT and ODbL notices belongs to the owner.
**Notes:** Written by the planner session of -56 by hand. Next free ID -84.

**Kind:** decision
**ID:** 2026-10-28-84
**Timestamp:** 2026-11-06T07:40:39Z
**Continues:** 2026-10-28-83
**Title:** The committed file of OSM-derived coordinates is removed from the tree, with no notice added (owner)
**Decision:** asked what should happen to `step1b/11-r-sequence-before-edit.tsv` (-83), the owner chose, verbatim: "Remove it
only". The option read: "A new commit deletes the file. No notice is added; the copy in history stays without one."
- **Rejected:** "Remove it, add ODbL notice (Recommended)"; "Keep it, add ODbL notice".
- **Done by the planner** on `brouter-port`, in one docs-only commit, by a plain push. E3RSequenceTest's SHA-256 pin still
  holds the sequence. The copy at `82d310a` stays in history, because the record never force pushes.
- **E1 stands as written:** no coordinates are committed. Every dispatch from now on says so.

**Also observed, after a restart of the planner's container:** Linden's session 24 pushed one more docs-only commit
(`f0db0df`) after the planner's E1 note: no coordinates and no model name, checked by the planner. The FK run had ended
`BUILD FAILED` at kind 5's assertion (`E3FKTest.kt:182`), after selection and before E3's regimes. Selection, from the log:
- kinds 1, 2, 3 and 6 were 10 of 10 each, with kind 4 10 of 10 at draw 74;
- **kinds 9, 10 and 11 were 10 of 10 each** (49 draws counted; 10 voided by the measured run);
- kind 5 was 9 of 10 (-82).

No family or stability has been read yet. The next session applies -82's kind 5 patch and reruns FK.
**Notes:** Written by the planner session of -56 by hand. Next free ID -85.

**Kind:** decision
**ID:** 2026-10-28-85
**Timestamp:** 2026-11-06T07:46:50Z
**Continues:** 2026-10-28-50, 2026-10-28-79, 2026-10-28-80, 2026-10-28-82, 2026-10-28-84
**Title:** FK's first full run read: 10 of 11 kinds in one family; kind 3 reselected by reach as kind 4 is; kind 9's timing
voids counted; a correction to -84
**A wrong reading of the planner's, in -84:** "after selection and before E3's regimes". The run went through all six regimes.
`E3FKTest.kt:182` is its final assertion, after every case had run [R, Linden].
**Observed (Linden, session 25, from `27-fk-e3Strata.log`, as reported):**
- **In family:**
  - FK1 10 of 10, "from-position not mapped";
  - FK2 10 of 10, "to-position not mapped";
  - FK4 10 of 10, "target island detected";
  - FK5 9 of 9, "to-position not mapped";
  - FK6 10 of 10, "datafile W80_N40.rd5 not found";
  - FK7 10 of 10, "file of size * too short";
  - FK8 10 of 10, "top index checksum error";
  - FK9 10 of 10, the timeout family;
  - FK10 and FK11 10 of 10 each, the watchdog family.
- **FK3: 6 of 10** reach "from-position not mapped", and FK3-05, -07, -08 and -09 route, all stable.
- **Unstable: 4 of 113,** all in FK9 (FK9-0001, -0037, -0040, -0049). In each, R3 was voided by "measured run took under 1
  s". These are timing voids, not differing outcomes.
- **The extra cases:**
  - FK7-x1 gives R1 "to-position not mapped", with nothing thrown. Linden's scratch probe had given a raw "Index … out of
    bounds", and why the two differ is [?];
  - **FK7-x2 and FK8-x1 both route, identically to the intact control FK7-c0** (`route|234|6078|4768`). A cut-short or
    sub-index-flipped tile gives the same route as the intact one.
**Decision (the planner's, technical):**
- **FK3 is selected as FK4 is,** by reach on the oracle, which is E4's design for FK ("the oracle's message is in the kind's
  family"):
  - a seeded draw of islands, capped at 200;
  - a case is kept only if its R1 reaches "no track found at pass=0", which is -79's case, a start on a usable component
    that cannot reach the target;
  - rejections are counted by outcome. "From-position not mapped" means the island's ways are unusable, which is kind 1's
    family. "Route" means it is not an island for this profile;
  - the change and its prediction are pushed first, then kind 3 is rerun alone.
- **FK9's 4 timing voids** are counted as unstable and kept out of exact assertions, as -50 ruled for `R-shortest-025`. FK's
  4 of 113 is under E3's 5%. No change to FK9.
- **FK7-x1** is compared by outcome, and both readings are recorded.
**Notes:** Written by the planner session of -56 by hand. Next free ID -86.

**Kind:** outcome
**ID:** 2026-10-28-86
**Timestamp:** 2026-11-06T10:37:39Z
**Continues:** 2026-10-28-79, 2026-10-28-80, 2026-10-28-82, 2026-10-28-85
**Title:** FK complete: all 11 kinds 10 of 10 in one family, 3 of 114 unstable (kind 9's timing voids); 1b's strata are all run
**Observed:** Linden Caliper, session 25, `brouter-port` `eb6460d` to `15b821c` (four commits; no model name in any trailer,
checked by the planner). Its final message was lost in a restart of the planner's container, so this entry is read from the
Phase 2 report's section "FK finished (twenty-fifth session)" and from CI, both by the planner.
- **Kind 5** (-82), with start and end drawn separately: 10 of 10 after 194 of 400 draws, 0 unstable.
- **Kind 3** (-85), selected by the oracle: 10 of 10 by draw 61. The 51 rejections were 35 "from-position not mapped" and
  16 "route". 0 unstable.
- **The full run on `ce3db16`** (`30-fk-e3Strata.log`) passed, with 114 cases and 0 ` e: ` lines. In family, each 10 of 10:
  - 1, "from-position not mapped";
  - 2, "to-position not mapped";
  - 3, "no track found at pass=0";
  - 4, "target island detected";
  - 5, "to-position not mapped";
  - 6, "datafile W80_N40.rd5 not found";
  - 7, "file of size * too short";
  - 8, "top index checksum error";
  - 9, the timeout family;
  - 10 and 11, the watchdog family.
- **Unstable: 3 of 114,** all kind 9's timing voids (FK9-0001, -0037, -0040), kept out of exact assertions (-85).
- **The extra cases:**
  - **FK7-x1** gives "to-position not mapped", nothing thrown, in both runs. On the same cut tile, Linden's probe ends give
    the index exception, so **the outcome follows the ends** [C, as reported];
  - **FK7-x2 and FK8-x1 route identically to the intact control FK7-c0.**
- **E1:** FK's committed evidence holds no coordinates. The planner scanned its files for both decimal and integer forms.
- **CI, read by the planner** (run 42183317078 on `ce3db16`, the last code change, `success`): `jvm` 95 tests, 0 failures; 0
  ` e: ` lines in either log; both `compileKotlinIos*` ran; simulator 23 of 23 (**simulator-checked**); arm64 7 of 7, with the
  gate reading "OK: all 2 classes ran".
**1b now stands at every stratum run:** F0, F0h, R, DL-lat, DL-lon, MP64, ML5, PM5 (short, 55), NT, CR, LB and FK. Only the
expected values remain, with PM5 at 55 and kind 9's voids kept out.
**Notes:** Written by the planner session of -56 by hand. Next free ID -87.

**Kind:** decision
**ID:** 2026-10-28-87
**Timestamp:** 2026-11-06T10:37:39Z
**Continues:** 2026-10-28-83, 2026-10-28-84
**Title:** OSM-derived positions stored as integers are stripped from the committed evidence (owner); two wrong claims of
the planner's corrected
**Observed:**
- **Linden found the planner's scan for -83 too narrow:** it looked only for decimal coordinates. The `R1-extent` column of
  the selection tables (`E3Select.kt:146`) is each route's bounding box in BRouter's integer microdegrees.
- **The planner then scanned both code branches** for integer pairs and `node=` positions, and read every heavy hit.
  - **Real:** the `R1-extent` column in seven tables (`17` to `25-*-select.tsv`), and `19-mp64-canescape-counts.txt`, which
    holds 900 node positions (`node=105980942,130643010`, …).
  - **False alarms, read and cleared:** R's and ML5's stability tables (costs and digests), 5b's `decode.txt` (cell
    indices), and the scale tables.
- **So -83's "the only file that has any" was wrong,** and so was -84's "after selection and before E3's regimes" (-85).
**Decision:** asked whether to give these the same treatment as -84, the owner chose, verbatim: "Strip them, same as -84
(Recommended)". The option read: "One commit removes the R1-extent column from the 7 tables (the rest of each table is
evidence and stays) and the node positions from the canEscape file (counts stay). History keeps the old copies; no notice.
Future tables never write the column."
- **Rejected:** "Delete the 8 files whole"; "Leave them".
- **Done by the planner** in `f308f88` on `brouter-port`, by a plain push:
  - the column is removed from the seven tables, and the 900 positions are replaced by "-";
  - **`E3Select.kt` no longer writes `R1-extent`.** The extent is still computed and used in memory, where DL's and LB's
    reach need it;
  - the committed readers `selcheck.py` and `cesum.py` read only the first columns or the ` cmd=` split, so they are
    unaffected [R];
  - **the full `:brouter:jvmTest` ran fresh before the push** (`compileTestKotlinJvm` and `jvmTest` both executed, and the
    XML was written at run time): 95 tests, 0 failures, 0 ` e: ` lines;
  - a scan of the staged diff found no coordinate in either form.
- **The scan from now on,** before every evidence commit: decimal coordinates, integer microdegree pairs, and `node=` positions.
**Notes:** Written by the planner session of -56 by hand. Next free ID -88.

**Kind:** intent
**ID:** 2026-10-28-88
**Timestamp:** 2026-11-06T12:09:24Z
**Continues:** 2026-10-28-50, 2026-10-28-58, 2026-10-28-71, 2026-10-28-85, 2026-10-28-86, 2026-10-28-87
**Title:** 1b's expected values written from R1, under the pinned JDK (Linden, session 26); PR #4
**Decision first:** asked "should Linden start the expected values" and "should I open PR #4", the owner, verbatim: "1 and 2
yes".
**Change** (on `brouter-port`, `jvmTest` and `docs` only), as Phase 1's E1 to E3 set it (report at `20e1f92`, lines ~776-814):
- **The stored form, per case:**
  - the kind, the node count, the cost, the distance and the node digest;
  - plus -58's heights digest;
  - plus a 64-bit FNV-1a digest of the tuple's bits.

  **No coordinates.** The planner's scan is run (-87), for decimals, integer microdegree pairs and `node=`.
- **R1's outcome is the expected value** (E3, line 814), for every case of every stratum:
  - F0 (2), F0h (8), R (1,534), DL-lat, DL-lon, MP64, ML5, NT, CR and LB (59 each);
  - **PM5 (55, -71);**
  - FK (110, plus the four extra cases, compared on outcome as -80 and -85 set them).
- **Kept out of exact values,** each named in the file:
  - `R-shortest-025` (-50), if it is unstable when the values are written;
  - kind 9's timing voids (-85), which FK9 compares by family only.
- **The file's head** names the JDK it was written on (vendor, exact version, `os.arch`), which is Temurin 21.0.13+9 x64
  (E2).
- **On the JVM,** the oracle runs live beside the stored values, so a moved oracle shows as an oracle failure (E1). **iOS
  compares against the stored values** at step 10.
- **A test reads the file** and checks, for every case, that its R1 at write time equals the stored entry. It is seen
  failing first, on one entry changed by a scratch revert.
**Scope boundary:** writes only `brouter-port`. No `commonMain` change: the port's own routes start at step 6, where these
values are first compared.
**Baseline:** `brouter-port` `f308f88` (CI green, -87).
**Finish line:** the expected values committed, the test seen failing and then passing, CI read by the planner, and 1b's
milestone report to the owner (Amendment 3).
**Abort conditions:** -3's; a case whose R1 differs from the R1 its stratum recorded; any coordinate in a staged diff.
**Notes:** Written by the planner session of -56 by hand. Next free ID -89.

**Kind:** outcome
**ID:** 2026-10-28-89
**Timestamp:** 2026-11-06T12:47:59Z
**Continues:** 2026-10-28-3, 2026-10-28-88
**Title:** Milestone: step 1b complete; 2,126 expected values written from R1, each equal to its stratum's log; CI green
**Observed:** Linden Caliper, session 26, `brouter-port` `f308f88` to `fcd35ff` (three commits; plain pushes; no model name
in any trailer; checked by the planner).
- **The file** `brouter/src/jvmTest/resources/e3/expected-1b.tsv` (216,164 bytes). **Checked by the planner:**
  - **2,126 rows:** F0 2, F0h 8, R 1,534, DL-lat 59, DL-lon 59, MP64 59, ML5 59, **PM5 55**, NT 59, CR 59, LB 59, FK 114;
  - modes: 2,120 exact, 3 family (FK9-0001, -0037, -0040, named in the head), 3 outcome (FK's extra cases);
  - **every tuple digest recomputed by the planner's own script, 0 mismatches;**
  - no coordinate in any form (-87's scan);
  - the head names the JDK (Eclipse Adoptium, 21.0.13+9-LTS, amd64).
- **The tuple digest** is FNV-1a over the UTF-8 bytes of `kind|nodes|cost|distance|nodeDigest|heightsDigest`. That byte form
  is Linden's choice where E1 says only "the tuple's bits". Accepted: it is stated in the file's head, and checked outside
  the JVM.
- **`R-shortest-025` is exact:** it was stable through all six regimes at write time, so -50's exclusion did not apply.
- **The R1 cross-check:** the writer ran all strata again on the pinned Temurin (19 min). Each case's R1 equals the R1 its
  stratum logged, 2,126 of 2,126 [R, as reported]. In CI, `E3ExpectedFileTest.everyRowIsTheR1ItsStratumLogged` passes
  against the committed stability tables.
- **Failing first:** one cost changed in scratch (7405 to 7406) gave 2 of 6 failures naming only that row, as predicted. The
  results were live (0 ` e: `, the XML written after the edit), and the file was restored from a saved copy.
- **CI, read by the planner** (run 42219674828 on `fcd35ff`, `success`): `jvm` 101 tests, 0 failures, with all six
  `E3ExpectedFileTest` methods passing; 0 ` e: ` lines in either log; both `compileKotlinIos*` ran; simulator 23 of 23
  (**simulator-checked**); arm64 7 of 7, with the gate reading "OK: all 2 classes ran".
- **A fragility:** the per-case lists the writer reads (`build/e3/*/cases.tsv`) hold coordinates, so they are never
  committed (E1), and they exist only in this container. Rewriting the values in a fresh container means regenerating them.
  The generators are seeded and their sequences pinned by digest (R's by `E3RSequenceTest`), so they can be regenerated
  [I: R's pin is checked, the others' are not].
- **Unverified:** whether the values hold on another JDK, CPU or Android's runtime (E2 claims none); FK10's precondition,
  Linden's own addition; how iOS reads the file (step 10).
**The milestone, for the owner (Amendment 3):** what 1b proves, and what it does not.
- **It proves the oracle agrees with itself.** Across 2,126 cases in 12 strata, Java's BRouter gives one answer under six
  regimes (separate JVMs, parallel runs, logging, coverage), apart from 3 timing voids in FK9.
- **It fixes those answers as the targets.** Each stratum's clean run will bound the port's disagreement rate (E5):
  - R, pooled, below **0.195%**;
  - each 59-case stratum below **4.95%**;
  - PM5 below **5.30%**;
  - FK, F0 and F0h claim no rate.
- **1b ran no port code.** Nothing yet speaks for the port's routes: they are first compared with these values at step 6.
- **It speaks only for HotSpot 21.0.13+9 on x86_64** (E2), not for Android's runtime, not for arm64 HotSpot, and not yet
  for iOS.
**Next:** the port's own router (steps 6 to 8) on `brouter-port-5b`'s line, the graph half being there (-61), unmerged until
step 10 answers -61. The draft milestone report is in the Phase 2 report, under "Step 1b finished".
**Notes:** Written by the planner session of -56 by hand. Next free ID -90.

**Kind:** intent
**ID:** 2026-10-28-90
**Timestamp:** 2026-11-06T13:06:39Z
**Continues:** 2026-10-28-24, 2026-10-28-50, 2026-10-28-61, 2026-10-28-62, 2026-10-28-68, 2026-10-28-89
**Title:** The plan for steps 6 to 8, the port's router (planner; not yet dispatched)
**Decision first:** the owner, verbatim: "Plan 6 to 8".

**What the steps are** (Phase 1's item I, report at `20e1f92`, lines 1701-1703; Java line counts [C] there):
- **6, the path model,** 2,068 lines in 15 classes: `RoutingContext`, `OsmPath`, `StdPath`, `OsmPathElement`, `ProfileCache`,
  `RoutingParamCollector` and the rest.
  - **Test:** `RoutingParamCollector` on Forager's strings; `RoutingContext` read from each profile; each path extension's
    cost on decoded links compared to the oracle's.
  - **Revert:** one `(int)` cast moved inside a sum in `StdPath`.
- **7, the track and hints,** 1,367 lines in 9 classes: `OsmTrack`, `VoiceHint*` and `Format*` as far as reached.
  - **Test:** exercised through step 8's routes; the hints are recorded, not asserted (E1).
  - **Revert:** one distance rounding replaced by `roundToLong`.
- **8, `RoutingEngine` and the entry point,** 2,009 lines, **less 429 for via points, left out by the owner at -24,** so 1,580.
  It has four sub-steps:
  - **8a,** matching and the search without memory pressure: F0 first, then R and DL;
    **Corrected 2026-11-11 by the planner (-123):** R and DL are not free of memory pressure; 151 of R's StdModel cases
    and 51 of DL's 118 reach 8b's memory path, so they pass only at 8b.
  - **8b,** the memory path: MP64, ML5, PM5. Revert: `isInMemoryBounds`' comparison flipped;
  - **8c,** terminate, the time limit and the progress count: FK 9 to 11. Revert: `terminated` written but not read;
  - **8d,** `route()` and `checkDataFile` over the engine.
- **About 5,000 Java lines in all** (2,068 + 1,367 + 1,580) [C]. That is the largest half of the port. The port has ported
  about 7,100 Kotlin lines so far (`brouter-port-5b`'s `commonMain`, `wc -l`).

**The branch (the planner's call, technical):**
- Step 6 needs both lines of work: the graph half, decoders and matcher, on `brouter-port-5b`; and 1b's expected values,
  heights tuple and harness on `brouter-port`.
- `brouter-port-5b` may not merge into `brouter-port` until step 10 answers -61. The other direction is free.
- **So `brouter-port` is merged into `brouter-port-5b`** (by merge, never rebase), and steps 6 to 8 are built there.
  - `brouter-port` has no `commonMain` change since their base `9f41799` (`git diff --stat`, empty). The merge is
    therefore expected to touch only tests, tools, build files and docs [I: conflicts in `build.gradle.kts` and
    `libs.versions.toml` are possible].
- **That merge is the first act** of step 6's first session, with CI read after it.

**Who does what:**
- **Plumb Ledger, on `brouter-port-5b`:** 6, then 8a to 8d, step by step, one coder per branch at a time.
- **Step 7** may go to Linden in parallel only if its files touch none of Plumb's. Otherwise it follows 6. The planner
  decides from step 6's file list.

**Debts these steps must pay, already recorded:**
- **-62:** `interpolateEle`'s `short` cast is shown covered on F0h's cut ends, with a revert that bites, at step 6.
- **-42:** the 19 untested way getters, exercised at step 6.
- **-49:** `StorageConfigHelper` catches only `IOException`; the choice is reviewed at step 8.
- **-73:** `canEscape`'s false branch comes from MP64's nodes. 5b's `collectOutreachersWithADestinationVanishesFarNodes` gets
  a region cut around one of those nodes, so its coverage assertion can be met (-61's planner's call).
- **-61:** `CollectOutreachersTest` stays red, reported at every CI read, never silenced.
- **-24:** via points are a named "unsupported", with a test that shows it.

**How sessions are run** (from -78's lesson that output, not reading, fills the context):
- one sub-step per session where it fits, with stubs and tests seen failing first;
- the full `:brouter:jvmTest` before any code push, confirmed fresh;
- E1's coordinate scan before any evidence push;
- the exact attribution line, never a model name.

**Cost [I, an estimate, not measured]:**
- Steps 2a to 4 cost about 2.5 million tokens for about 3,570 Java lines (-42), about 0.7 million per 1,000 lines.
- At that rate steps 6 to 8 would take about **3.5 million**.
- Later sessions have each run to about 180k in 15 to 40 minutes, so the true figure may be higher. The planner counts it
  per session and reports it at each milestone.
**For the owner before step 6 starts:** `ProfileCache`'s lock (Phase 1, item B row 8), which no entry has ruled. Phase 1
recommends (a), keeping it with atomicfu 0.33.0's `SynchronizedObject`. That is a new dependency in shipped code.
**Finish line for this plan:** the owner's answer on the lock, then step 6's dispatch.
**Notes:** Written by the planner session of -56 by hand. Next free ID -91.

**Kind:** decision
**ID:** 2026-10-28-91
**Timestamp:** 2026-11-06T13:54:19Z
**Continues:** 2026-10-28-90
**Title:** `ProfileCache`'s lock is kept, with atomicfu (owner)
**Decision:** asked how the port should handle `ProfileCache`'s Java `synchronized` lock (-90; Phase 1, item B row 8), the
owner, verbatim: "Keep it, with atomicfu (Recommended)". The option read: "Phase 1's recommendation: keep the lock using
atomicfu 0.33.0's SynchronizedObject, a small, pinned dependency already proven to compile and run in Phase 1's probe. A
silent race on iOS is ruled out even if the app ever runs two routes at once."
- **Rejected:** "Drop it, one route at a time".
- **What follows (the planner's calls, technical):**
  - atomicfu **0.33.0**, pinned exactly in `gradle/libs.versions.toml` (Forager's CLAUDE.md, Building), `commonMain` only;
  - step 6 adds it, and checks that both iOS klibs compile with it before `ProfileCache` uses it;
  - a test shows the lock is held, with two threads on the JVM, and is seen failing with the lock removed by a revert.
- **-90's plan has no open owner question left.** Step 6's dispatch follows.
**Notes:** Written by the planner session of -56 by hand. Next free ID -92.

**Kind:** intent
**ID:** 2026-10-28-92
**Timestamp:** 2026-11-06T15:43:07Z
**Continues:** 2026-10-28-61, 2026-10-28-62, 2026-10-28-90, 2026-10-28-91
**Title:** Step 6, the path model, on `brouter-port-5b`: first the merge from `brouter-port` and atomicfu (Plumb, session 10)
**Decision first:** the owner, verbatim: "Start step 6".
**Change** (on `brouter-port-5b` only), in this order:
1. **The merge:** `git merge --no-ff origin/brouter-port` (`fcd35ff`) into `brouter-port-5b` (`b65548f`), per -90. Conflicts
   are resolved keeping both sides, and the resolution is named in the message. The full `:brouter:jvmTest` runs fresh
   before the push. Expected: `brouter-port`'s 101 tests and 5b's 111 combined, less shared ones, with only
   `CollectOutreachersTest`'s 2 red (-61). Then CI is read.
2. **atomicfu 0.33.0** (-91): pinned in `gradle/libs.versions.toml`, `commonMain`, with both iOS klibs compiled before use.
3. **Step 6 itself** (Phase 1's item I, line 1701), in the coder's own order:
   - stubs first, then tests over them seen failing, then the port, then the revert (one `(int)` cast moved inside a sum
     in `StdPath`), with its prediction pushed first;
   - **tests:** `RoutingParamCollector` on Forager's strings; `RoutingContext` read from each profile; each path extension's
     cost on decoded links compared with the oracle's;
   - **also owed here:** -62's `interpolateEle` cast, covered on F0h's cut ends; -42's way getters; `ProfileCache`'s lock,
     with a two-thread test that fails with the lock removed (-91).
**Scope boundary:** writes only `brouter-port-5b`. `brouter-port` is read, not written. No via points (-24).
**Finish line:** step 6 closed with its own report (`docs/audits/2026-11-06-brouter-port-step6.md`), or a handoff.
**Abort conditions:** -3's; a merge conflict in `commonMain` (none is expected, -90); a new red besides
`CollectOutreachersTest`; any coordinate in a staged diff.
**Notes:** Written by the planner session of -56 by hand. Next free ID -93.

**Kind:** decision
**ID:** 2026-10-28-93
**Timestamp:** 2026-11-06T15:52:58Z
**Continues:** 2026-10-28-84, 2026-10-28-87, 2026-10-28-92
**Title:** The merge into `brouter-port-5b` is pushed; its scan hits are Forager's own route ends, under E1's standing
exception; scans now gate pushes
**Observed:** Plumb Ledger, session 10, stopped under -92's abort condition "any coordinate in a staged diff".
- The merge `eac836d` (`origin/brouter-port` into `brouter-port-5b`) was pushed with the scan reading 12. Plumb's command
  chained the push after the scan without testing its count. Plumb reported this as its own error.
- **The merge itself:** two conflicts, neither in `commonMain` (`build.gradle.kts`, `TestTiles.kt`). Both were resolved by
  keeping 5b's side, which already carries `brouter-port`'s F0h wiring. `jvmTest` read 121, with only
  `CollectOutreachersTest`'s 2 red [R, as reported].
- **The 12 hits, checked by the planner:**
  - 10 are endpoint lines in `E3CoverageTest.kt` and `E3F0hTest.kt`, brought in from `brouter-port` byte for byte. Their
    values equal Forager's `server/pmtiles-worker/test/fixtures/rd5/routes.json` at `a0535ece` [C: matched by value].
    They are the same points `E3F0Test.kt` has carried since step 1b began;
  - 2 are false positives: a float-literal list in a test log, and a diff header's timestamp.
**Decision (the planner's, technical):**
- **Nothing is removed or rewritten.** These are Forager's own published test ends. Phase 1's practice, written in
  `E3F0Test.kt`'s header, is "no coordinates beyond Forager's own ends are committed (E1)". -84 and -87 removed positions
  derived from the pinned tiles, which is a different thing.
- **The scan from now on excludes Forager's own ends by exact value,** from `routes.json` and `points.json` at `a0535ece`.
  **It gates the push**: the push runs only if the count is 0.
**For the owner, information:** these route ends are OpenStreetMap node positions in Forager's own public test data. If
the owner wants them out of Forager-iOS too, that is a ruling against Phase 1's practice, and it would also touch
`E3F0Test.kt`.
**Notes:** Written by the planner session of -56 by hand. Next free ID -94.

**Kind:** decision
**ID:** 2026-10-28-94
**Timestamp:** 2026-11-06T16:00:54Z
**Continues:** 2026-10-28-90, 2026-10-28-92
**Title:** Step 6's file list; `OsmTrack` is a stub in step 6, and step 7 follows 6 on the same branch
**Observed (Plumb, session 10, as reported):**
- atomicfu 0.33.0 is pinned (`a9fb1d0`).
- Step 6's new files are 15 under `brouter/src/commonMain/.../router/`, a package that does not exist yet:
  `RoutingContext` (perhaps split in two), `OsmPath`, `StdPath`, `OsmPathElement`, `MessageData`, `StdModel`, `OsmPathModel`,
  `OsmPrePath`, `OsmNodeNamed`, `ProfileCache`, `RoutingParamCollector`, `RoutingIslandException`, `SearchBoundary`,
  `AreaInfo` and `OsmNogoPolygon`. Tests go under `jvmTest/.../router/`. No existing `commonMain` file is expected to change [I].
- **One overlap, and a stop:** `OsmPath` takes an `OsmTrack refTrack` (`init`, `addAddionalPenalty`) and calls
  `refTrack.containsNode` twice (`OsmPath.java:350`). `OsmTrack` is a step 7 class. `RoutingContext.createPath` also takes one.
**Decision (the planner's, technical):** Plumb's option (a).
- **Step 6 creates `router/OsmTrack.kt` holding only the type.** `containsNode` throws a named "unsupported"
  (`NotImplementedError`), never an empty body (-74). Step 7 fills the same file.
- **So step 7 follows step 6 on `brouter-port-5b`,** and does not run beside it. Step 7's classes all use step 6's types,
  so they could not compile alone. -90's parallel option is closed.
- **Reachability** [R for the lines, I for the conclusion]: `refTracks[i]` is created only when `lastTracks[i]` is non-null
  (`RoutingEngine.java:988-991`), which comes from earlier alternatives. Forager passes `alternativeidx=0`, so on its path
  `refTrack` is most likely null, and `containsNode` is unreachable at `:350`. **Checked at step 8** with the coverage
  reader: `:350`'s true branch should read not covered on R's R6 runs.
- **Rejected:** (b), waiting on step 7 to port `OsmTrack` first. It blocks step 6 on a class it does not need on Forager's
  path.
**Notes:** Written by the planner session of -56 by hand. Next free ID -95.

**Kind:** outcome
**ID:** 2026-10-28-95
**Timestamp:** 2026-11-06T17:16:27Z
**Continues:** 2026-10-28-24, 2026-10-28-73, 2026-10-28-89, 2026-10-28-91, 2026-10-28-92, 2026-10-28-94
**Title:** Step 6, first session: the merge, atomicfu, `ProfileCache` with its lock, and half of `RoutingContext`; the car
profiles are outside the port, and are refused (owner)
**Observed:** Plumb Ledger, session 10, `brouter-port-5b` `b65548f` to `46fc891` (six commits; clean tree). It handed off at
the planner's message after the watcher read 183,589 at 16:15:35Z. Report: `docs/audits/2026-11-06-brouter-port-step6.md`.
- **Trailers:** the planner found one model name in the range, in `00b9886`. That is -78's commit, brought in by the merge,
  not a new one.
- **The merge** `eac836d`: CI read by Plumb (run 42246987233) showed `jvm` 121, only `CollectOutreachersTest`'s 2 red;
  simulator 24 (**simulator-checked**); arm64 7, with its gate [R, as reported]. -93 covers its scan.
- **atomicfu 0.33.0** is pinned (`a9fb1d0`). Whether it links into the iOS klibs is first shown by CI on `de4d994`, where
  `ProfileCache` uses `SynchronizedObject`. That is read after this entry is written.
- **Ported:** `ProfileCache` with its lock, `OsmPathModel`, `OsmPrePath`, `StdModel`, `RoutingIslandException`, and
  `RoutingContext`'s configuration half.
  - `RoutingContextReadTest`: 76 fields equal the oracle's on 20 profiles.
  - `ProfileCacheLockTest`: two threads; only one is ever inside `parseProfile`.
  - Both were seen failing over the stubs. The lock's revert fails "expected 1 but was 2". The `maxSpeed` revert fails on
    the two foot profiles only, as predicted. Its first form did not compile and was not read.
  - The full run before the push: 123 tests, only `CollectOutreachersTest`'s 2 red [R, as reported].
- **Still stubs or not started:** `OsmPath`, `StdPath` (stubs); `OsmTrack` (type only, -94); `OsmPathElement`,
  `MessageData`, `OsmNodeNamed`, `RoutingParamCollector`, `SearchBoundary`, `AreaInfo`, `OsmNogoPolygon`.
- **The planner's own gap, found through Plumb's refusal of six profiles:**
  - Phase 1 counted the path model, which is chosen by reflection (`RoutingContext.java:86-97`), as "dead for the pinned
    profile" (premise 4).
  - T2's port list keeps 0 lines of `KinematicModel`, `KinematicNoCostModel`, `KinematicPath`, `KinematicNoCostPath` and
    `KinematicPrePath` (`03-reach/out/T2/port-list.tsv`) [C].
  - Yet R draws on all 26 profiles, and the six car profiles name `KinematicModel`. So 354 of R's 1,534 cases (6 × 59)
    and FK kind 5 (`car-fast`, -82) could never be compared. Neither -73, -86 nor -89 saw it.
**Decision:** asked how the port should treat these profiles, the owner chose, verbatim: "Refuse them, test the rest
(Recommended)". The option read: "The port refuses Kinematic profiles with a named 'unsupported', and a test shows it. R is
compared on its 20 foot/bike profiles (1,180 cases; the clean-run bound becomes 0.254% instead of 0.195%), and the 354 car
cases check the refusal. FK kind 5 is rebuilt on a foot profile. Nothing untested ships."
- **Rejected:** "Port the car models too".
- **What follows (the planner's calls, technical):**
  - **R's figure** is corrected in -89's milestone: compared on 1,180 cases, a clean run bounds the rate below **0.254%**
    (1 − 0.05^(1/1180) = 0.00254) [C]. The expected values keep all 1,534 rows. The 354 car rows check the refusal at
    step 9.
  - **FK kind 5** is rebuilt on a Std-model profile (`hiking-mountain`): an end more than 250 m from any way it can use. It
    is drawn as -82 drew it, and its expected rows are rewritten. That is Linden's, on `brouter-port`.
  - **The refusal** is a named "unsupported" with a test on all six profiles, already in Plumb's work [R, as reported].
**Notes:** Written by the planner session of -56 by hand. Next free ID -96.

**Kind:** outcome
**ID:** 2026-10-28-96
**Timestamp:** 2026-11-06T17:16:54Z
**Continues:** 2026-10-28-91, 2026-10-28-95
**Title:** atomicfu links into iOS: CI on `de4d994` read; next, step 6 continues (Plumb) and FK kind 5 is rebuilt (Linden)
**Observed, read by the planner** (run 42250446552 on `de4d994`, where `ProfileCache` first uses `SynchronizedObject`):
- `jvm` 123 tests, 2 failures, both `CollectOutreachersTest` (-61);
- 0 ` e: ` lines in either log; both `compileKotlinIos*` ran;
- **the simulator built, linked and ran 24 of 24** (**simulator-checked**). So atomicfu 0.33.0 links on
  `iosSimulatorArm64`. `iosArm64` is compiled, not linked, in CI [I];
- arm64 7 of 7, with the gate reading "OK: all 2 classes ran".
**Next (within the owner's "Start step 6" and -95's ruling):**
- **Plumb Ledger, session 11, on `brouter-port-5b`:** step 6's remaining files, by its handoff. Owed: `StdPath`'s cast revert,
  -62's `interpolateEle` cast and -42's way getters.
- **Linden Caliper, session 27, on `brouter-port`:** FK kind 5 rebuilt on `hiking-mountain` (-95). That means its generator,
  its prediction, its run through E3's six regimes, and its 10 rows rewritten in `expected-1b.tsv`, with
  `E3ExpectedFileTest` green.
  - The file's other 2,116 rows must be byte-unchanged (`cmp` on the rest).
  - `brouter-port` is then merged into `brouter-port-5b` again by Plumb, at a step boundary.
**Notes:** Written by the planner session of -56 by hand. Next free ID -97.

**Kind:** decision
**ID:** 2026-10-28-97
**Timestamp:** 2026-11-06T17:26:21Z
**Continues:** 2026-10-28-24, 2026-10-28-94, 2026-10-28-96
**Title:** `StdPath`'s `Math.exp` uses `kotlin.math.exp`; `OsmPath`'s start-direction branch is a named "unsupported"
**Observed (Plumb, session 11, as reported):**
- `StdPath.computeKinematic` and `calcIncline` call `Math.exp` only in detail mode, and only to set the track's time and
  energy, not its cost or its path. It is reached on Forager's foot profile (Tobler's speed in `footMode`). `LibmSeam` has no
  `exp`.
- `OsmPath`'s start-direction branch (`Math.sin`/`cos`, `OsmPath.java:282-283`) needs `rc.startDirectionValid`. That is set
  only at `RoutingEngine.java:1931-1932`, and only when `startDirection != null`.
**Checked by the planner:**
- Forager reads only the track's nodes, distance and heights (`BRouterTrailRouter.kt:95-114` at `a0535ece`), never its time
  or energy [R].
- Its call passes only `profile`, `alternativeidx` and `profile:use_dynamic_range` (`:150-155`), with no heading [R].
**Decision (the planner's, technical):**
- **`kotlin.math.exp`.** On the JVM it is `Math.exp`, so the oracle comparison stays exact. On iOS it is Apple's libm,
  which is [?].
  - Time and energy are recorded, not asserted (E1; Phase 1 report at `20e1f92`, line ~755).
  - **If Forager ever reads time or energy** (an ETA, say), `exp` moves behind `LibmSeam` as an msun port, as -24 did
    for `cos` and `atan2`. The KDoc and the step 6 report say so.
  - **Rejected now:** an msun `e_exp.c` port. It would add maths code for values Forager does not read.
- **The start-direction branch is a named "unsupported",** with a test that shows the refusal. **Step 8's coverage check**
  reads `OsmPath.java:282-283` as not covered on R's R6 runs, as -94 set for `containsNode`.
**Notes:** Written by the planner session of -56 by hand. Next free ID -98.

**Kind:** decision
**ID:** 2026-10-28-98
**Timestamp:** 2026-11-06T17:28:08Z
**Continues:** 2026-10-28-82, 2026-10-28-95, 2026-10-28-96
**Title:** FK kind 5 on `hiking-mountain` is short at 6 of 10 in construction; its cap is raised to 1,000
**Observed (Linden, session 27, `brouter-port` `fcd35ff` to `cc01338`, two commits, as reported):**
- The generator change and the prediction were pushed before the run (`d841cfd`).
- Kind 5 on `hiking-mountain` (start and end drawn separately, cap 400): 7 starts, all accepted; 393 end draws, 6
  accepted.
- **Rejections, all before routing:** 353 had a usable link within 300 m, and 34 had cells leaving the tile.
- The 6 cases are all "to-position not mapped", all stable.
- `expected-1b.tsv` was not touched. Linden reports that CI run 42261021182 on `cc01338` passed (`jvm` 101, simulator 23,
  arm64 7 with its gate).
**Decision (the planner's, technical):** Linden's option (a).
- **The cap is raised to 1,000 draws.** At this run's rate, about 1 end in 66 draws, 10 ends need about 660.
- **The reasoning is -82's:** every rejection is in construction, and none is selected on the oracle's outcome. FK is
  "constructed, not sampled", so a larger cap does not bias it.
- **Rejected:**
  - (b), a 250 m distance of kind 5's own: it would split a constant that kinds 1 and 2 share, for no gain;
  - (c), keeping 6, as -82 already rejected.
- **The rest as -96 set it:** kind 5's 10 rows are rewritten and every other row stays byte-identical (`cmp`). The writer's
  case list (`build/e3/FK/cases.tsv`, which still holds `car-fast`'s kind 5) is regenerated for kind 5 first.
- **Observed for the record:** kind 5 on a foot profile is now built like kind 2 (an end beyond any usable way), and shares
  its family, as -80 foresaw.
**Notes:** Written by the planner session of -56 by hand. Next free ID -99.

**Kind:** outcome
**ID:** 2026-10-28-99
**Timestamp:** 2026-11-06T17:41:08Z
**Continues:** 2026-10-28-24, 2026-10-28-49, 2026-10-28-76, 2026-10-28-95, 2026-10-28-97
**Title:** Step 6, second session: `RoutingContext`'s nogo half and three small classes ported; `NodesCache`'s graph half
was never ported, a debt from 5b; the planner's rulings on the path-cost comparison
**Observed:** Plumb Ledger, session 11, `brouter-port-5b` `46fc891` to `079537f` (two commits; clean tree; no model name;
checked by the planner).
- **Ported:**
  - `RoutingContext`'s second half (the nine methods T2 keeps, in `RoutingContextNogo.kt`);
  - `OsmNodeNamed`, with `distanceWithinRadius` and `decodeNogo` throwing a named "unsupported";
  - `OsmNogoPolygon` and `AreaInfo`, as types, with their unreached methods refusing.
- **`RoutingContextNogoTest`:** 4 tests, seen failing over the stubs, passing on the port. Revert C was predicted at 1 of 4
  failing on `wf=`, and observed exactly so [R, as reported].
  - It runs on `OracleLibm`, because `MsunLibm`'s scale table moved one radius's last bits. That is -24's rule: logic runs
    inject the oracle's table. Accepted.
- **Still to do:** `OsmPath` and `StdPath` (still stubs), `MessageData`, `OsmPathElement`, `RoutingParamCollector`,
  `SearchBoundary`. The `(int)` cast revert, -62's `interpolateEle` and the path-cost comparison all wait on them.
- **CI, read by the planner** (run 42262870661 on `079537f`): `jvm` 127 tests, 2 failures, both `CollectOutreachersTest`
  (-61); 0 ` e: ` lines in either log; both `compileKotlinIos*` ran; simulator 24 of 24 (**simulator-checked**); arm64 7 of
  7, with the gate reading "OK: all 2 classes ran".

**A debt from 5b, found by Plumb and confirmed by the planner:**
- `NodesCache`'s graph half was never ported: `getGraphNode`, `obtainNonHollowNode`, `expandHollowLinkTargets`,
  `hasHollowLinkTargets` and `getStartNode` (Java `NodesCache.java:213-300`, about 90 lines) [R].
- The port's own header says it "arrives with step 5b" (`NodesCache.kt:14-16`). -49 recorded it as "waiting for 5b and 5c".
  Neither step ported it, and the planner's checks at -60, -69 and -76 did not see that.

**Decision (the planner's, technical):**
- **Step 6's next session ports the graph half first,** as 5b's debt, with its own differential test against the oracle:
  the same nodes on both engines from real tiles, compared field by field. It is seen failing over the stubs, and a revert
  bites.
- **The path-cost comparison is then made from Forager's real ends,** by copying `RoutingEngine.getStartPath`'s steps in test
  code over the ported graph half: F0's and F0h's ends, and a fixed sample of R's ends on the 20 Std profiles.
  - Plumb's link-midpoint comparison on F0h is **accepted as an addition** for breadth, not instead of the real ends.
  - A cut at a link midpoint is not a place Forager starts.
- **The `(int)` cast revert runs on profiles whose `turncost` is above 0,** since `hiking-mountain`'s is 0 and the revert
  would not bite there. Its prediction names the profiles.
**Notes:** Written by the planner session of -56 by hand. Next free ID -100.

**Kind:** outcome
**ID:** 2026-10-28-100
**Timestamp:** 2026-11-06T18:01:05Z
**Continues:** 2026-10-28-95, 2026-10-28-96, 2026-10-28-98
**Title:** FK kind 5 on `hiking-mountain`: 10 of 10, stable; `expected-1b.tsv` byte-unchanged; CI green
**Observed:** Linden Caliper, session 28, `brouter-port` `cc01338` to `611ba54` (three commits; clean tree; no model name;
checked by the planner). The cap change and the prediction were pushed before the run (`b5715ec`).
- **Kind 5:** 10 of 10 after 937 of 1,000 draws; all 927 rejections came before routing. All 10 are "to-position not
  mapped" and stable through the six regimes [R, as reported].
  - **Checked by the planner:** `33-fk5-cap1000-stability.tsv` reads 10 "stable".
- **`expected-1b.tsv` is unchanged:** `git diff --stat fcd35ff 611ba54` on it is empty (planner).
  - Kind 5's rows store no profile and no ends, and its outcome (`0|0|0`, empty digests) is the same on `hiking-mountain`
    as on `car-fast`.
  - What changed is the table its rows are checked against: `E3Expected` now reads `FK5-` rows against kind 5's own table.
- **E1:** no line added by this session carries a coordinate, by the planner's scan of the added lines. The Phase 2 report
  holds three older hits: two false positives, and F0's route end rounded to five places (line 238), which is Forager's own
  (-93).
- **CI, read by the planner** (run 42265967539 on `11e1e8c`, the last code change, `success`): `jvm` 101 tests, 0 failures;
  0 ` e: ` lines in either log; both `compileKotlinIos*` ran; simulator 23 of 23 (**simulator-checked**); arm64 7 of 7, with
  the gate reading "OK: all 2 classes ran".
- **Unverified:** that session 27's six cases are among these ten (its case list was not kept).
**So -95's ruling is carried out on `brouter-port`.** Plumb merges `brouter-port` into `brouter-port-5b` again at a step 6
boundary (-96).
**Notes:** Written by the planner session of -56 by hand. Next free ID -101.

**Kind:** outcome
**ID:** 2026-10-28-101
**Timestamp:** 2026-11-06T18:13:54Z
**Continues:** 2026-10-28-90, 2026-10-28-97, 2026-10-28-99
**Title:** Step 6, third session: `NodesCache`'s graph half ported (5b's debt paid), with `MessageData` and `OsmPathElement`;
two process gaps; a CI run lost to a 503 and re-run once
**Observed:** Plumb Ledger, session 12, `brouter-port-5b` `079537f` to `a83ff3f` (three commits; clean tree; no model name;
checked by the planner). The watcher closed at 159,941.
- **The graph half** (`getGraphNode`, `obtainNonHollowNode`, `expandHollowLinkTargets`, `hasHollowLinkTargets`,
  `getStartNode`; `NodesCache.java:213-300`) is ported, and `NodesCache.kt`'s header is corrected.
  - **`GraphHalfTest`** starts from F0's 2 ends, F0h's 8 and 20 of R's on `hiking-mountain`. It covers cleanup modes 1 and
    2 plus a 64 KiB run.
  - Over the stubs it failed with `NotImplementedError`. On the port: 90 runs, 1,378,140 transcript lines equal [R, as
    reported].
  - Revert D (`obtainNonHollowNode`'s direct-weaving return flipped) was predicted at the first `obtain` line, and observed
    there.
- **`MessageData` and `OsmPathElement`** are ported with T2's methods. `PathElementTest` compares them by reflection over
  Java's field names. Reverts E (`copy` drops `extraTime`) and F (`calcDistance`'s floor) each failed exactly as predicted.
- **Still to do:** `OsmPath` and `StdPath` (stubs), `RoutingParamCollector`, `SearchBoundary`; the path-cost comparison; the
  `(int)` and `interpolateEle` reverts. That is 1,019 Java lines left in step 6 (T2's port list, summed by the planner).
- **CI, read by the planner:**
  - Run 42266847677 on `f848da4`, first attempt: the `jvm` job failed before any test, on a `curl` 503 in
    `:brouter:msunReference`. That is infrastructure, not this branch's code. **Re-run once by the planner.**
  - Attempt 2: `jvm` 130 tests, 2 failures, both `CollectOutreachersTest` (-61); 0 ` e: ` lines; both `compileKotlinIos*`
    ran. `ios-smoke` passed on attempt 1: simulator 24 of 24 (**simulator-checked**), arm64 7 of 7, with its gate.

**Two process gaps:**
- **`PathElementTest` was written over finished code,** not seen failing over stubs first. Reverts E and F show the test can
  fail, but Forager's CLAUDE.md asks to "see the failure before writing the fix". From now on, every test is seen failing
  over stubs before the port it checks. A revert does not stand in for that.
- **Reverts D, E and F's result files are in scratch only** (`rev/D-*`, `rev/E-*`, `rev/F-*`), cited in the report but not
  committed. Scratch does not survive a new container. The next session commits them under `step6/`, scanned for E1, as
  reverts A to C were.

**Cost (the planner's own gap):** -90 said the cost would be counted per session, and it has not been summed. From here,
each session's final context is recorded in its outcome. This planner run has dispatched 17 coder sessions, each ending
between 108k and 194k of context. That is the context at the end, not the tokens billed, which the planner cannot see.
**Notes:** Written by the planner session of -56 by hand. Next free ID -102.

**Kind:** decision
**ID:** 2026-10-28-102
**Timestamp:** 2026-11-06T18:17:53Z
**Continues:** 2026-10-28-54, 2026-10-28-101
**Title:** The owner's stop: the planner's handoff written, and the scratch tools saved into the repository
**Decision:** the owner, verbatim: "Write a hand off and save your space to continue work this Sunday".
- **Nothing is running:** no coder, no stratum run, no watcher.
- **The heads at handoff** (`git ls-remote`, 18:17Z): `main` `e006c7c`, `brouter-port` `611ba54`, `brouter-port-5b` `a83ff3f`,
  `brouter-port-5a` `2a8ba5e`. PR #5 is open with -90 to this entry.
- **`docs/process/planner-handoff.md`** is rewritten, replacing Cedar Datum's of -54, which stays in history. It holds the
  state, the first steps in order, the rulings since -89, the open items, and how this planner worked.
- **Saved into `docs/process/tools/`,** because scratch does not survive a new container:
  - `watch_tokens.py`, the context watcher;
  - `revert.sh`, Plumb's revert runner;
  - `scan.sh`, the gated E1 scan;
  - `make-forager-ends.sh`, which rebuilds the list of Forager's own ends from Forager's files at `a0535ece`, so that no
    coordinate list is committed. **Checked by the planner:** it reproduces Plumb's 63 values exactly. None of the four files
    holds a coordinate.
- **Still in scratch only, and lost with the container:** reverts D, E and F's result files (-101). Plumb's session 13
  commits them first, or re-runs the three reverts and says so.
**Notes:** Written by the planner session of -56 by hand. Next free ID -103.

**Kind:** decision
**ID:** 2026-10-28-103
**Timestamp:** 2026-11-11T05:29:32Z
**Continues:** 2026-10-28-90, 2026-10-28-101, 2026-10-28-102
**Title:** A new planner session picks up from -102's handoff; Plumb Ledger's session 13 dispatched on `brouter-port-5b`
**Decision:** the owner, verbatim: "Let's continue to BRouter port".
- **The heads, checked by the planner** (`git ls-remote`, 05:2xZ) against -102: `brouter-port` `611ba54`, `brouter-port-5b`
  `a83ff3f`, `brouter-port-5a` `2a8ba5e`, all as handed off. `main` is `d22a302`, the merge of PR #5 on the owner's word
  ("Go ahead and merge PR 5 now"), so -102's step 2 (asking about PR #5) is closed.
- **No older planner is live:** `ListAgents` lists no other session, and no watcher or coder was carried into this container.
- **CI on `a83ff3f`, read by the planner** (run 42266943851): `jvm` 130 tests, 2 failures, both `CollectOutreachersTest`
  (-61); 0 ` e: ` lines; both `compileKotlinIos*` ran. `ios-smoke`: simulator 24 of 24 (**simulator-checked**), arm64 7 of 7,
  the gate reading "OK: all 2 classes ran".
- **The E1 ends rebuilt:** `make-forager-ends.sh` on a fresh shallow clone of Forager's `pre-main` (`947f1be2`, with
  `a0535ece` reachable) gave 63 values, as at -102.
- **Reverts D, E and F's result files are lost** with the old container, as -102 warned. Plumb re-runs the three at the
  current head and says so.
- **Plumb Ledger, session 13,** dispatched in the background on Opus, in -102's order: the three reverts re-run and
  committed; `OsmPath` and `StdPath` under -97, each test seen failing over stubs first (-101); the path-cost comparison
  (-99); the `(int)` and `interpolateEle` reverts; then `RoutingParamCollector` and `SearchBoundary`. The merge of
  `brouter-port` into 5b waits for the step boundary. The context watcher runs at 180,000.
- **The record branch is `ccr-7e682f25-4tqqon`,** this session's, cut from `main` at `d22a302`, in place of -102's
  `ccr-362633cb-61f3bw`, whose PR is merged. The planner's call; the entries reach `main` by PR on the owner's word.
**Notes:** Written by the planner session by hand. Next free ID -104.

**Kind:** outcome
**ID:** 2026-10-28-104
**Timestamp:** 2026-11-11T05:42:16Z
**Continues:** 2026-10-28-90, 2026-10-28-101, 2026-10-28-103
**Title:** Step 6, Plumb's session 13: handed off at the watcher's limit after 12 minutes, with `OsmPath` and `StdPath`
drafted but not compiled; a coder starts at about 61k of context; a wrong premise of the planner's
**Observed:** Plumb Ledger, session 13, `brouter-port-5b` `a83ff3f` to `4815a6f` (one docs-only commit; clean tree; trailer
`Co-Authored-By: Claude <noreply@anthropic.com>`, no model name; checked by the planner).
- **Nothing in `brouter/` changed.** The drafts (`OsmPath`, `StdPath`, `PathEngines`, `StdPathTest`, `StartPathTest`, 1,457
  lines) are under `docs/audits/2026-11-06-brouter-port-step6/s13-drafts/` as `.kt.txt`, outside the source sets. None has
  been compiled, and none has been seen failing over stubs (-101). Section 10 of the step 6 report lists six steps for
  session 14.
- **Reverts D, E and F were not re-run.** Their result files are still missing (-102, -103).
- **The baseline, read by the planner** from Plumb's own run at `a83ff3f`, which finished after the handoff
  (`./gradlew :brouter:jvmTest --rerun-tasks`, 7m 31s): 130 tests, 2 failed, both `CollectOutreachersTest` (-61); 0 ` e: `
  lines. Its first attempt failed before any test, on HTTP 429 from Maven Central for okio-jvm, annotations and
  atomicfu-jvm [R, as reported]. That is infrastructure.
- **Cost, measured by the planner from the transcript:** the first assistant turn already carried 60,764 tokens of
  context, before any file was read. The watcher fired at 183,054 (05:39:57Z), and the last turn read 192,787. Plumb
  estimated its own context at "about 80–90k". The estimate was about 100k low, so a coder's self-estimate is not used
  as a measure. A coder has about 120k of working room per session.
- **A wrong premise, the planner's:** -103's dispatch placed `coder-profile.md` on `brouter-port-5b`. It is only on
  `main`. Plumb found it there.
**So step 6 has not moved in code.** Session 14 starts from section 10's list, with the baseline already read. It reads
the drafts against Java one method at a time, not both Java files whole.
**Notes:** Written by the planner session by hand. Next free ID -105.

**Kind:** outcome
**ID:** 2026-10-28-105
**Timestamp:** 2026-11-11T06:15:09Z
**Continues:** 2026-10-28-90, 2026-10-28-97, 2026-10-28-99, 2026-10-28-101, 2026-10-28-104
**Title:** Step 6, Plumb's session 14: `OsmPath` and `StdPath` ported, their tests seen failing over stubs first; reverts
D, E and F re-run and committed; revert G (the turncost cast) as predicted, with the port's side of its line unread
**Observed:** Plumb Ledger, session 14, `brouter-port-5b` `4815a6f` to `7282c67` (six commits; clean tree; every trailer
`Co-Authored-By: Claude <noreply@anthropic.com>`, no model name; checked by the planner). It ended at 167,215 tokens by
the harness's count; the watcher did not fire.
- **Reverts D, E and F, re-run at `4815a6f`** (predictions at `7928c2a`, results at `ecf4506`). **Checked by the planner**
  in the three result files: each has 0 compile errors, 1 fresh XML and a `cmp`-equal restore, and each failure message is
  specific to its own edit and matches session 12's observation (D: line 3, an `obtain` line, oracle `true`, port `false`,
  6,915 against 5,391 lines; E: `case 1: copy's extraTime`, port 0; F: `case 0: calcDistance`, oracle 1, port 0).
  -101's missing files are now committed, as re-runs, not the originals.
- **Tests over stubs first (-101), at `128dffd`, red on purpose:** `StdPathTest` (3) and `StartPathTest` (1), 4 of 4
  `NotImplementedError: ... step 6: OsmPath`, 0 compile errors, 2 fresh XML (planner read the file).
- **The port, at `bce3a3a`:** the drafts checked against Java method by method, with two compile fixes
  (`anglemeter.cosAngle`; `@JvmField` on `sourceNode`, `targetNode`, `link`) [R, as reported]. The four path classes 7 of 7.
  - **-97 carried out, checked by the planner:** `OsmPath.kt:267` throws `UnsupportedOperationException("unsupported:
    OsmPath's start direction (OsmPath.java:282-283)")`, and `startDirectionIsUnsupported` asserts that exact message, then
    the same path without a start direction. `OsmPath.kt:132` refuses `AreaInfo.checkAreaInfo` the same way.
- **Revert G, the `(int)` cast** (prediction pushed in `bce3a3a` before the run): it named the 16 of 20 Std profiles whose
  `turncost` is above 0, read from the brf files, and expected `fastbike-asia-pacific` first. Observed: `StdPathTest` 1 of
  3 and `StartPathTest` 1 of 1 failed, both on `fastbike-asia-pacific`, `F0-inside-across-the-degree-line`, at a cost line
  (oracle `cost=26923`; start, oracle `cost=2847`). The other two tests passed, as predicted. **Not confirmed:** the
  prediction's "port lower". The runner cuts messages at 200 characters, so the port's value is not in the result file.
- **CI on `bce3a3a`, read by the planner** (run 42969696836): `jvm` 134 tests, 2 failed, both `CollectOutreachersTest`
  (-61); 0 ` e: `; both `compileKotlinIos*` ran. `ios-smoke`: simulator 24 of 24 (**simulator-checked**), arm64 7 of 7, the
  gate reading "OK: all 2 classes ran". The two docs commits after it were still running at writing.
- **An anomaly in the container [?]:** Plumb's full local run left 10 of 59 XML files empty, while Gradle's log and CI both
  give 134. Not explained.
- **Plumb asked about the trailer:** the harness suggested one naming the model with a session link. Plumb used the
  dispatch's trailer. That is right: the record's rule is no model name in commits (-102's handoff), and the harness's own
  guidance also forbids model identifiers in commits.
**Left in step 6:** -62's `interpolateEle` revert; the empty XML; `StartPathTest`'s end-cut sample; revert G's port value;
`RoutingParamCollector` and `SearchBoundary`. Then the merge of `brouter-port` into 5b at the step boundary.
**Notes:** Written by the planner session by hand. Next free ID -106.

**Kind:** outcome
**ID:** 2026-10-28-106
**Timestamp:** 2026-11-11T06:56:40Z
**Continues:** 2026-10-28-61, 2026-10-28-62, 2026-10-28-90, 2026-10-28-105
**Title:** Step 6, Plumb's session 15: revert G's port value read (lower, as predicted); reverts H and I; `StartPathTest`'s
end cuts widened from 6 to 668; the empty XML not seen again
**Observed:** Plumb Ledger, session 15, `brouter-port-5b` `7282c67` to `d2c6993` (six commits; clean tree; every trailer
`Co-Authored-By: Claude <noreply@anthropic.com>`; checked by the planner). It ended at 141,920 tokens by the harness's
count; the watcher did not fire.
- **The empty XML of -105 did not recur:** the full fresh run at `7282c67` gave 134 tests in 59 XML files, none empty, 0
  ` e: ` (`10-full-run-7282c67.txt`). Its cause is unknown; Plumb's hypothesis, a process overlapping the moment all 59
  files are written, is [I].
- **Revert G's "port lower" is confirmed,** read from the whole XML messages (`11-revert-G3-port-value-result.txt`, read by
  the planner): oracle `cost=26923`, port `26914`; at the start, `2847` against `2842` [R for the second].
- **Revert H, -62's `interpolateEle` revert** (prediction at `4ffae38`, result at `016327f`): 0 compile errors, 2 fresh XML,
  `cmp`-equal restore; 2 of 4 failed, `interpolateEleMatchesTheOracle` and `startPathsMatchTheOracleFromForagersEnds`, as
  predicted. **One part missed:** the start-path failure came on one of R's ends, not on F0h's. Why F0h's cut starts pass
  the revert is [?].
- **`StartPathTest` widened:** synthetic end waypoints at each extension link's midpoint reach 668 of 1,868 end cuts,
  against 6 final links before [R, as reported]. Revert I (the end cut's height left unchanged), predicted at `61a8e4f`
  and observed at `370f384`, failed on an F0h cut line, as predicted [R, as reported].
- **CI, read by the planner** on `61a8e4f` (run 42974370259) and `370f384` (run 42974441912): `jvm` 134 tests, 2 failed, both
  `CollectOutreachersTest` (-61); 0 ` e: `; both `compileKotlinIos*` ran. `ios-smoke`: simulator 24 of 24
  (**simulator-checked**), arm64 7 of 7, the gate reading "OK".
- **`collectOutreachersWithoutADestination` passed once** in Plumb's local run at `7282c67`, and was red again in the next run
  and in CI, with no code change between. This matches what the record already holds for Question A: at the default stack,
  the port overflows 2 to 4 times a run, and at 1g this test passes. It is the deep-recursion question that waits for step
  10 (-61). Not touched.
**Left in step 6:** `SearchBoundary` (87 Java lines) and `RoutingParamCollector` (374), neither stubbed yet; then the merge
of `brouter-port` (`611ba54`) into 5b at the step boundary (-96).
**Notes:** Written by the planner session by hand. Next free ID -107.

**Kind:** outcome
**ID:** 2026-10-28-107
**Timestamp:** 2026-11-11T08:18:20Z
**Continues:** 2026-10-28-90, 2026-10-28-96, 2026-10-28-101, 2026-10-28-106
**Title:** Step 6, Plumb's session 16: `SearchBoundary` and `RoutingParamCollector` ported, each over a failing stub first;
reverts J and K; the boundary merge made but not pushed; a test loosened after the port first ran
**Observed:** Plumb Ledger, session 16, `brouter-port-5b` `d2c6993` to `028783c` (nine commits; every trailer
`Co-Authored-By: Claude <noreply@anthropic.com>`; checked by the planner). The watcher fired at 180,895 (07:52:34Z); the
planner asked for the handoff and told Plumb to leave the merge. It ended at 189,487 by the harness's count.
- **`SearchBoundary`, ported whole:** over the stub (`ca11871`, red on purpose) 1 of 1 failed, `NotImplementedError` naming
  `getFileName`'s stub; on the port it passes on 168,000 `isInBoundary` cases [R, as reported].
- **`RoutingParamCollector`, ported for Forager's calls:** `getWayPointList`, both `readPosition`s, and `setParams` for
  `profile`, `alternativeidx` and `profile:`. Over the stubs (`30293c8`, red on purpose) 3 of 3 failed. **Checked by the
  planner:** a non-empty `nogoLats`, each of the 18 other keys Java acts on, and a second `profile:` key throw
  `UnsupportedOperationException("unsupported: ...")` (`RoutingParamCollector.kt:92-117`). The 8 methods Forager never
  calls are not ported; §13 of the step 6 report names them.
- **Reverts,** each predicted in a pushed file before it ran: J (`SearchBoundary`'s inner east bound) failed on
  `getBoundaryDistance` direction 3, off by one degree, the branch Plumb had called less likely; K (`getWayPointList`'s
  latitude truncated) failed one lower on Forager-form ends, as predicted, but on F0h case 3, not the first [R, as
  reported].
- **CI, read by the planner:** on `30293c8` (run 42981080608), `jvm` 138 tests, 5 failed: the three
  `RoutingParamCollectorTest` stubs and `CollectOutreachersTest`'s two. On `6cd2ab9`, `12ae393` and `028783c` (runs
  42982527896, 42982592547, 42982873596), `jvm` 138, 2 failed, both `CollectOutreachersTest` (-61). Every run: 0 ` e: `;
  both `compileKotlinIos*`; simulator 24 of 24 (**simulator-checked**); arm64 7 of 7, the gate "OK".
- **The merge of `brouter-port` (`611ba54`) was not pushed.** **Checked by the planner:** `611ba54` is not an ancestor of
  the remote `brouter-port-5b`. Plumb's local merge commit `4f5492f` (clean, no conflicts) stays on a local branch; session
  17 redoes the merge from the remote and compares the trees.
**A flag of the planner's, for the owner:** after the port first ran, Plumb changed `SearchBoundaryTest` twice, with the
port unchanged. Out-of-range latitudes throw in both engines, and the test now compares those throws by class, and by
message only for Java's own `IllegalArgumentException`. Plumb's stated reason: an index error lost its message on a
JIT-compiled path [I]. That is a check loosened after the data was seen, against "fix choices before data". It touches only
the throwing cases, not the 168,000 compared results. A stricter route, a test-JVM flag that keeps those messages, changes
the build and is the owner's call. The planner proposes that the independent reviewer read it, once that role is named.
**Notes:** Written by the planner session by hand. Next free ID -108.

**Kind:** outcome
**ID:** 2026-10-28-108
**Timestamp:** 2026-11-11T08:20:40Z
**Continues:** 2026-10-28-42, 2026-10-28-62, 2026-10-28-90, 2026-10-28-94, 2026-10-28-96, 2026-10-28-107
**Closes:** step 6 of 2026-10-28-90
**Title:** Step 6 closed: `brouter-port` merged into `brouter-port-5b` at the boundary (Plumb's session 17); every file on
-94's list ported
**Observed:** Plumb Ledger, session 17, `brouter-port-5b` `028783c` to `7125739` (two commits; trailers
`Co-Authored-By: Claude <noreply@anthropic.com>`; checked by the planner). It ended at 97,993 by the harness's count.
- **The merge, `7491f3a`** (`028783c` and `611ba54`), clean. **Checked by the planner:** `611ba54` is now an ancestor of
  `brouter-port-5b`; the merge's diff against `028783c` is byte-identical, `index` lines aside, to `brouter-port`'s own
  `fcd35ff..611ba54`, so it adds nothing `brouter-port` did not carry, and its E1 scan is that branch's (0). The redone
  merge's tree matches session 16's unpushed `4f5492f` apart from §13 [R, as reported].
- **Tests:** local fresh run 138, 2 failed, both `CollectOutreachersTest` (-61), 61 of 61 XML non-empty [R]. Of the classes
  the merge changed, only `E3ExpectedFileTest` is in `jvmTest`, 6 of 6 [R]; `E3FKTest` and `E3ExpectedWriteTest` are strata
  tests outside it.
- **CI on `7491f3a`, read by the planner** (run 42984411711): `jvm` 138, 2 failed, both `CollectOutreachersTest`; 0 ` e: `;
  both `compileKotlinIos*`; simulator 24 of 24 (**simulator-checked**); arm64 7 of 7, the gate "OK".
- **Step 6 against -94's list, checked by the planner:** `router/` holds all 15 classes named there, `RoutingContext` split
  in two (`RoutingContextNogo.kt`), as -94 allowed, plus `OsmTrack.kt` holding only the type. The one stub left in
  `router/` is `OsmTrack.containsNode`, which throws a named "unsupported", as -94 ruled; step 7 fills it.
- **-90's debts for step 6:**
  - -62's `interpolateEle` cast: shown, by revert H (-106).
  - The step's own revert, the `(int)` cast: shown, by revert G (-105, -106).
  - -42's way getters: 19 named getters are called from `OsmPath.kt` and `StdPath.kt` (planner's `grep`), and the oracle
    comparisons that run those classes pass. That each call site executes is not counted [I], so -42 is paid by
    inference, not shown. Step 8's coverage reader can show it.
**So step 6, the path model, is done and compared with the oracle.** About 3,000 Java lines remain in steps 7 and 8 (-90's
counts: 1,367 and 1,580).
**Notes:** Written by the planner session by hand. Next free ID -109.

**Kind:** intent
**ID:** 2026-10-28-109
**Timestamp:** 2026-11-11T08:20:40Z
**Continues:** 2026-10-28-90, 2026-10-28-94, 2026-10-28-108
**Title:** Step 7, the track and hints, on `brouter-port-5b`: a verify-first session before any port
**Intent:** under -90 ("7, the track and hints, 1,367 lines in 9 classes: `OsmTrack`, `VoiceHint*` and `Format*` as far as
reached") and -94 (step 7 follows 6 on the same branch). The planner's calls, technical:
- **Plumb Ledger takes step 7,** since the branch has one coder at a time and Plumb holds step 6's knowledge.
- **The first session reads and reports, and builds nothing beyond stubs:** step 7's file list; which methods Forager's
  path reaches from `RoutingEngine` and `OsmPath`, traced to their callers; what Phase 1's item I says step 7 holds; and
  a test plan. -90 says step 7 is "exercised through step 8's routes; the hints are recorded, not asserted (E1)". Under
  -101, each test is seen failing over stubs first, so the report says what step 7 can test against the oracle on its
  own before step 8, and what must wait. Its revert, from -90: "one distance rounding replaced by `roundToLong`".
- **Stops:** anything that would assert a hint or a route coordinate (E1); any change to the plan's scope, such as
  `Format*` classes Forager never reaches. Those come to the planner, and scope to the owner.
**Notes:** Written by the planner session by hand. Next free ID -110.

**Kind:** outcome
**ID:** 2026-10-28-110
**Timestamp:** 2026-11-11T08:32:07Z
**Continues:** 2026-10-28-90, 2026-10-28-94, 2026-10-28-109
**Title:** Step 7's verify-first report (Plumb's session 18): the hints and every `Format*` output are unreached on
Forager's path; -90's revert has no target; Android's routes carry heights and the port's do not
**Observed:** Plumb Ledger, session 18, `brouter-port-5b` `7125739` to `916cb8f` (one docs commit,
`docs/audits/2026-11-11-brouter-port-step7.md`; trailer `Co-Authored-By: Claude <noreply@anthropic.com>`; E1 scan 0 [R]).
No code was built. The watcher fired at 190,601 (08:30:28Z), nine minutes in; the report was pushed before the planner's
handoff call landed. It ended at 196,212 by the harness's count. Plumb names the cost: a `grep` loop over `OsmTrack`'s
members, about 600 lines of Java in ranges, and item I's wide rows.
- **Step 7's files** (Java lines / T2's kept lines) [R]: `OsmTrack` 587/444, `VoiceHint` 223/201, `VoiceHintList` 61/33,
  `VoiceHintProcessor` 529/529, `Formatter` 569/84, `FormatGpx` 573/26, `FormatJson` 261/22, `FormatKml` 116/15,
  `FormatCsv` 43/13. 1,367 kept lines, as -90 says.
- **The hints are off on Forager's path. Checked by the planner** in Forager's vendored BRouter: every one of the 24 profiles
  that sets `turnInstructionMode` sets it to 1, "auto-choose", and `RoutingContext.java:177-179` leaves the field at its
  default 0 when the value is 1. Only `RoutingParamCollector.java:224-231` sets it otherwise, and the port refuses those
  keys (-107). So `processVoiceHints` returns early (`OsmTrack.java:457-460`, [R]), and `VoiceHint`, `VoiceHintProcessor`,
  the detour methods and `containsNode` are not reached. Phase 1's JaCoCo run agrees [R].
- **Reached** [R]: `OsmTrack`'s fields, `buildMap`, `getLink`, `appendTrack` (its one-section branch), `copyDetours`,
  `readBinary`'s null check, `prepareSpeedProfile`, the start of `processVoiceHints`, and `VoiceHintList`'s constructor and
  `setTransportMode`. `getTotalSeconds` and `Formatter`'s three methods are reached but only write `track.message`, which
  nothing reads. No `Format*` output is taken by Forager. `addNode` runs only on a timeout: unknown.
- **-90's revert has no target.** No distance is rounded in step 7. Plumb proposes instead `OsmTrack.java:346`'s
  `if (i > 0 || ourSize == 0)` made `if (i > 0)`, which drops the first node when tracks are joined.
- **A premise for step 8, checked by the planner:** Android's router keeps a height for every route point since 2026-10-30
  (`BRouterTrailRouter.kt:219`, `nodes.map { heightMetresOf(it.getSElev()) }`; Forager `ba6a52d7`, dispatch 2026-10-30-06
  Amendment 1). The port's `RouteOutcome.Found` carries `iLat`, `iLon` and `distance`, and no heights [R for the line,
  `Route.kt:40`]. That is a gap with Android, the planner's to raise.
- **Tests proposed** [R]: `TrackMapTest`, `AppendTrackTest`, `TrackHeadTest` and `TrackTimeTest`, each seen failing over stubs
  first, over tracks built from step 6's paths. They compare by index, bits and digest, and assert no positions and no
  hints. Real routes wait for step 8.
**Decision (the planner's, technical):** -90's revert is replaced by Plumb's `OsmTrack.java:346` revert, since the original
names a rounding step 7 does not have. `TrackTimeTest` asserts its time and energy values, since its inputs are step 6's
paths from Forager's own ends (excepted, -93) and it asserts no position or hint.
**For the owner:** whether the unreached hint and detour code, and the `Format*` classes, are ported or left as named
"unsupported"; and whether step 8's route result carries heights, as Android's does.
**Notes:** Written by the planner session by hand. Next free ID -111.

**Kind:** decision
**ID:** 2026-10-28-111
**Timestamp:** 2026-11-11T10:18:39Z
**Continues:** 2026-10-28-90, 2026-10-28-109, 2026-10-28-110
**Title:** The owner's rulings on step 7's scope and step 8's heights: BRouter's hints ported, the export formats ported,
heights added; Forager's own next-turn code to be ported for the iOS app as well
**Decision:** the owner, verbatim, to the planner's three questions:
- On BRouter's hint and detour code: "Option 2. I just added turns directions the last update." (Option 2 read "Port it
  all".) Asked again with the finding below, the owner chose "Both", whose option read: "Port BRouter's hint code now as
  ruled, and also plan the port of Forager's own next-turn code for the iOS app."
- On the export formats (GPX, JSON, KML, CSV): "Port them".
- On heights in step 8's route result: "Add heights at step 8 (Recommended)".
- On what to do with them: "Record it,Build it".
**The finding put to the owner, checked by the planner** in Forager's `pre-main` (`e1fbb635`): Android's turn directions
(PR #500, `next-turn`, merged 2026-11-09) come from Forager's own code, `domain/nextturn/` (`RouteTurns.kt` and its
neighbours), built on the route's ways through Forager's own interface. They do not use BRouter's hint code. Android's
profiles still set `turnInstructionMode` to 1, which leaves BRouter's hints off (-110).
**Two wrong premises, the planner's:**
- The first question said "Forager never switches turn hints on". That is true of BRouter's hints and was read as true of
  Forager. The planner had not checked Forager's `pre-main` for new features before step 7, which its profile names as its
  duty ("Keeping iOS level with Android is the planner's duty"). `pre-main` was read only at `947f1be2` for the E1 ends.
- The formats question said "About 160 kept Java lines". That was T2's count of the lines Forager's path reaches. Ported
  whole, the five format classes are 1,562 Java lines (-110's counts: 569 + 573 + 261 + 116 + 43). The ruling stands as
  given. The corrected figure goes to the owner in the next message.
**What this means for the plan (the planner's reading, labelled as such):**
- **Step 7 grows** from 1,367 kept lines to about 3,000: `OsmTrack` whole, `VoiceHint`, `VoiceHintList`,
  `VoiceHintProcessor`, `Formatter` and the four `Format*` classes. Each is compared with the oracle, with its test seen
  failing over stubs first (-101). On Forager's own path, hints and formats stay unreached, so their tests switch them on
  in test code. Inputs come from seeds or from Forager's own ends, and outputs are compared by digest, so no coordinate is
  committed beyond Forager's own ends (E1).
- **Order:** `OsmTrack`'s reached core first (session 19, -110's plan); then the hint code (one or two sessions); then the
  formats. The revert for the core is `OsmTrack.java:346` (-110).
- **Step 8** returns a height per route point, as Android's `BRouterTrailRouter.kt:219` does, compared with the oracle.
- **Forager's next-turn code** is an app-layer port, not part of BRouter. It is an open item for the iOS app's plan, after
  the BRouter port, and gets its own intent when planned.
**Notes:** Written by the planner session by hand. Next free ID -112.

**Kind:** outcome
**ID:** 2026-10-28-112
**Timestamp:** 2026-11-11T10:49:47Z
**Continues:** 2026-10-28-94, 2026-10-28-101, 2026-10-28-110, 2026-10-28-111
**Title:** Step 7, Plumb's session 19: `OsmTrack`'s reached core ported over failing stubs; revert (a) as predicted
**Observed:** Plumb Ledger, session 19, `brouter-port-5b` `916cb8f` to `737fdf2` (five commits; clean tree; every trailer
`Co-Authored-By: Claude <noreply@anthropic.com>`; checked by the planner). It ended at 170,728 by the harness's count; the
watcher did not fire.
- **Ported** (`cbd31d6`): `OsmTrack`'s fields, `addNode`, `copyDetours`, `buildMap`, `containsNode` (replacing -94's stub),
  `getLink`, `appendTrack`, `getTotalSeconds`, `prepareSpeedProfile`, `readBinary`'s null guard and `processVoiceHints`'
  head; `VoiceHintList`'s constructor, fields and `setTransportMode`; `Formatter`'s three statics. The hint body,
  `addDetours` and `readBinary`'s file branch are `TODO("step 7: hint code, a later session (RECORD -111)")` [R].
- **Tests, over stubs first** (`bf48f79`, red on purpose): `TrackMapTest`, `AppendTrackTest`, `TrackHeadTest`,
  `TrackTimeTest`, 6 of 6 `NotImplementedError` naming the stubs, 0 compile errors; on the port 6 of 6 [R]. Full fresh run
  144 tests, 2 failed, both `CollectOutreachersTest` [R].
- **Revert (a)** (`OsmTrack.java:346`, -110), prediction pushed at `c898514` before the run. **Checked by the planner** in
  the result file: 0 compile errors, 4 fresh XML, `cmp`-equal restore; 2 of 6 failed, as predicted:
  `appendTrackMatchesTheOracle` at mode 0, the first case, node count 564 against 563; and
  `totalSecondsOnBuiltTracksMatchTheOracle` on seconds and time (15,747 against 15,739), energy equal.
- **CI on `cbd31d6`, read by the planner** (run 43005311784): `jvm` 144, 2 failed, both `CollectOutreachersTest` (-61);
  0 ` e: `; both `compileKotlinIos*`; simulator 24 of 24 (**simulator-checked**); arm64 7 of 7, the gate "OK".
- **Disclosed by Plumb:**
  - A test literal changed to pass the E1 scan: a double in `TrackTimeTest`'s list, `0.0499` followed by one more 9, was
    cut to `0.0499`, because the scan's decimal pattern caught it. That was before the port existed, and the red run was
    repeated after it. It is not a coordinate; the scan's pattern is wider than E1.
  - A weak check [?]: `TrackHeadTest` asserts that `copyDetours` on a track with no detours gives null, which a stub
    returning null would also pass. Session 20 strengthens it with a real detour map.
**Next:** session 20, the hint code (-111): `VoiceHint`, `VoiceHintList`'s rest, `OsmTrack`'s detour methods and hint body;
then `VoiceHintProcessor`; then `readBinary`'s file branch and the formats.
**Notes:** Written by the planner session by hand. Next free ID -113.

**Kind:** outcome
**ID:** 2026-10-28-113
**Timestamp:** 2026-11-11T10:59:56Z
**Continues:** 2026-10-28-101, 2026-10-28-107, 2026-10-28-111, 2026-10-28-112
**Title:** Step 7, Plumb's session 20: the hint code's first half red over stubs; the port written, held at the stop on
changing tests after the result; two harness fixes accepted on conditions
**Observed:** Plumb Ledger, session 20, `brouter-port-5b` `737fdf2` to `46626f3` (two commits; trailers
`Co-Authored-By: Claude <noreply@anthropic.com>`; checked by the planner). It ended at 150,206 by the harness's count.
- **Red over stubs, `d2d8e34`:** `VoiceHintTest` (`VoiceHint` whole and `VoiceHintList`'s rest, on 30,000 seeded hints with
  zero positions), `TrackDetourTest` (the four detour methods, `getMinDistance`, `getMatchedWaypoint`, `removeVoiceHint`),
  and `TrackHeadTest`'s `copyDetours` on a real detour map. 5 of 5 failed, 0 ` e: ` [R].
- **The weak check of -112 is closed:** the stronger `copyDetours` check fails over a null-returning stub, "detourMap
  expected:<FrozenLongMap> but was:<null>" (`step7/04-copydetours-null-stub-result.txt`) [R].
- **The port is written and not pushed:** `step7/03-s20-port-unpushed.patch`, and a local stash.
- **The stop this prompt set, hit:** on the port, 2 of 5 failed on defects of the tests, not the port [R]:
  - `calcCommand`'s comparison read Java's void result as null, and Kotlin's as `Unit`;
  - the set-up froze one Java detour map twice, and Java's `FrozenLongMap` empties the map it is built from, so the oracle
    side threw an NPE.
  - With both fixed, uncommitted (`step7/05-s20-diagnostic-test-changes-not-applied.patch`), 5 of 5 passed [R].
- **E1:** the scan flagged 6 timestamps in the two patch files' diff headers. They were stripped, and the rescan read 0.
- **A false premise in -112's plan:** `processVoiceHints`' hint body cannot be compared with the oracle before
  `VoiceHintProcessor` is ported. The two, with `startSection`, are tested as one unit.
**Decision (the planner's, technical), after reading patch 05's lines:** both fixes are accepted.
- The first maps `Unit` to null and nothing else. A port method returning `Unit` where Java returns a value still fails,
  since the value is not null. So it cannot hide a port defect.
- The second makes a fresh map for each copy, which changes the set-up, not a comparison. It adds a check that a second
  copy from an emptied map throws on both engines alike.
  - **Corrected 2026-11-11T11:27:54Z by the planner (-114):** it adds a logged outcome, not an asserted check. In the
    committed test (`79f8d93`) the second copy's result is written to the log (an NPE on both engines, 20 of 20), and no
    assertion reads it. Plumb left it unasserted rather than add an assertion after seeing the port's result.
- **On a condition:** the fixed tests are committed and seen failing over the stubs again before the port goes in, so
  -101's evidence is about the tests that are kept.
- **Why this is not -107's case:** there a comparison of results was narrowed (throws compared by class) after the data
  was seen. Here no comparison of values is narrowed.
**Notes:** Written by the planner session by hand. Next free ID -114.

**Kind:** outcome
**ID:** 2026-10-28-114
**Timestamp:** 2026-11-11T11:27:54Z
**Continues:** 2026-10-28-101, 2026-10-28-111, 2026-10-28-113
**Title:** Step 7, Plumb's session 21: the hint code's first half ported over the fixed tests, red first; revert (b) as
predicted; `VoiceHintProcessor`'s unit red over stubs; -113 corrected in place
**Observed:** Plumb Ledger, session 21, `brouter-port-5b` `46626f3` to `7193465` (six commits; every trailer
`Co-Authored-By: Claude <noreply@anthropic.com>`; checked by the planner). It ended at 165,287 by the harness's count.
- **-113's condition met:** the two fixes committed with their reasons, and run over the stubs first (`79f8d93`, red on
  purpose): 5 of 5 failed, 0 ` e: ` [R]. Then the port, unchanged from session 20's (`7e3fb60`): the three classes 5 of 5.
  The stash was dropped only after it diffed equal to the pushed commit [R]; the planner saw no stash left.
- **Full fresh run:** 148 tests, 2 failed, both `CollectOutreachersTest` [R].
- **Revert (b)** (`addDetours`' `source.nodesMap!!.contains(id)` negated), predicted at `c6da1d1` before the run. **Checked
  by the planner** in the result file: 0 compile errors, 5 fresh XML, `cmp`-equal restore; 1 of 8 failed, "group 0
  addDetours variant 0: getFromDetourMap, candidate 26 expected:<[835]> but was:<null>", as predicted.
- **CI on `7e3fb60`, read by the planner** (run 43010170082): `jvm` 148, 2 failed, both `CollectOutreachersTest` (-61);
  0 ` e: `; both `compileKotlinIos*`; simulator 24 of 24 (**simulator-checked**); arm64 7 of 7, the gate "OK".
- **The next unit, red over stubs (`24603fb`):** `VoiceHintProcessor`, `startSection` and `processVoiceHints`' body, with
  `VoiceHintProcessTest` (100 cases, hints switched on in test code on both engines, compared by digest). 1 of 1 failed on
  the body's stub, 0 ` e: `. The oracle side alone gives 1,934 hints and 12 commands; commands 1, 10, 12 and 13 are not
  reached by this sample [R].
- **-113 corrected in place,** under the owner's rule: "The rule is corrections go into the same record that was recorded"
  (Forager's planner profile, "Keeping the record"). -113 said fix 2 "adds a check"; it adds a logged outcome, not an
  asserted one. The planner's misreading of patch 05.
**Next:** session 22 ports the `VoiceHintProcessor` unit. Its sample is widened to reach the four missing commands, if it
can be, before the port runs, while only the oracle's side has been seen.
**Notes:** Written by the planner session by hand. Next free ID -115.

**Kind:** outcome
**ID:** 2026-10-28-115
**Timestamp:** 2026-11-11T11:58:35Z
**Continues:** 2026-10-28-101, 2026-10-28-111, 2026-10-28-114
**Title:** Step 7, Plumb's session 22: `VoiceHintProcessor`, `startSection` and `processVoiceHints`' body ported over a
sample widened first; revert (c) as predicted; `OFFR` unreachable, `EL`/`ER` not reached on these inputs
**Observed:** Plumb Ledger, session 22, `brouter-port-5b` `7193465` to `dd43676` (five commits; every trailer
`Co-Authored-By: Claude <noreply@anthropic.com>`; checked by the planner). It ended at 167,529 by the harness's count.
- **The sample widened before the port (`5ba7488`, red on purpose):** only the inputs changed, from 10 walks of up to 25
  steps per end to 40 of up to 80, so from 100 cases to 400. An oracle-side coverage check was added for commands 1 to 16
  except 12, with the comparison untouched. Over the stubs 1 of 1 failed, 0 ` e: ` [R]. On the oracle's side: 17,279
  hints, every command 1 to 16 but 12 reached, 10 three times and 13 six times [R].
- **`OFFR` (12) cannot be reached. Checked by the planner:** it appears only in `Formatter.java`'s switches (`:165`, `:211`,
  `:259`, `:303`, `:516`), and no line sets it.
- **`EL`/`ER` (17, 18) are not reached on these inputs.** They are set at `VoiceHintProcessor.java:385-387` and `:444-446`
  (planner's `grep`), on a motorway link. Plumb reads Forager's profile as pricing motorways at 100000, so no walk takes one
  [I, not traced through the cost code].
- **The port (`080885f`):** the processor, `startSection` and the hint body; `VoiceHintProcessTest` and the three track
  tests 6 of 6 on the first run. Full fresh run 149 tests, 2 failed, both `CollectOutreachersTest` [R].
- **Revert (c)** (`postProcess`' catching-range check, `<` made `>`), predicted at `bf66b05` before the run: "case 26: hint
  count expected:<62> but was:<66>", as predicted, 0 compile errors, `cmp`-equal restore [R].
- **CI on `080885f`, read by the planner** (run 43014941206): `jvm` 149, 2 failed, both `CollectOutreachersTest` (-61);
  0 ` e: `; both `compileKotlinIos*`; simulator 24 of 24 (**simulator-checked**); arm64 7 of 7, the gate "OK".
**Decision (the planner's, technical):** session 23 first tries to reach `EL`/`ER` with a test-only set-up applied alike on
both engines, such as an input that takes a motorway link. If that is not cheap, the four lines are recorded as not
covered, with the reason. `OFFR` is recorded as unreachable. The low counts for 10 and 13 stand: each is reached and
compared.
**A slip, the planner's:** its message to the owner before this entry put revert (c)'s figures the wrong way round. The
oracle gives 62; the reverted port gives 66.
**Next:** session 23: `EL`/`ER` as above, then `readBinary`'s file branch and the formats (-111), 1,562 Java lines whole,
likely two sessions.
**Notes:** Written by the planner session by hand. Next free ID -116.

**Kind:** outcome
**ID:** 2026-10-28-116
**Timestamp:** 2026-11-11T12:38:07Z
**Continues:** 2026-10-28-101, 2026-10-28-107, 2026-10-28-111, 2026-10-28-115
**Title:** Step 7, Plumb's session 23: `Formatter` ported whole over failing stubs; revert (d) as predicted; `EL`/`ER` not
covered; `readBinary`'s file branch stopped on JDK 21's null-pointer wording; the planner's ruling on it
**Observed:** Plumb Ledger, session 23, `brouter-port-5b` `dd43676` to `2f2eb38` (partial, handed off at the watcher's
182,127; trailers `Co-Authored-By: Claude <noreply@anthropic.com>`; checked by the planner). It ended at 188,576 by the
harness's count.
- **`EL`/`ER`, recorded not covered** (`d413679`): a probe on `fastbike` with motorways allowed reached `goodWay` prio 29
  142 times, but never beside a motorway (`maxBadPrio` 30) [R]. The committed test is unchanged.
- **`Formatter` whole**, red over stubs at `08fdbba`, then passing at `3d64e31` on 63,728 calls against the oracle [R].
- **Revert (d)** (`getOruxAction`'s sum flipped), predicted in `3d64e31` before it ran: "getOruxAction(13, -12) expected
  996, was 1020", as predicted, `cmp`-equal restore [R].
- **CI, read by the planner:** on `08fdbba` (run 43017985612), `jvm` 151, 4 failed: `FormatterTest` and `ReadBinaryTest` red
  on purpose, and `CollectOutreachersTest`'s two. On `3d64e31` (run 43020174261), `jvm` 151, 3 failed: `ReadBinaryTest` red
  on purpose and `CollectOutreachersTest`'s two. Both: 0 ` e: `; both `compileKotlinIos*`; simulator 24 of 24
  (**simulator-checked**); arm64 7 of 7, the gate "OK".
- **The stop:** `ReadBinaryTest` passes tracks 0 to 29, then fails at track 30, the first empty one. The result is equal, but
  the debug text is 145 characters on the oracle and 93 on the port. With no nodes, Java's `last_pe.cost`
  (`OsmTrack.java:258`) throws a null-pointer exception, and JDK 21 adds its own message [I: inferred from the length].
  The port is held in `16-s23-readbinary-port-not-passing.patch`; the source keeps its stubs.
**Checked by the planner:**
- Java catches every exception there and only appends `"Error reading rawTrack: " + e` to `debugInfo`
  (`OsmTrack.java:286-290`). The differing text is the JVM's wording of its exception, not BRouter's.
- The branch is unreachable on Forager's path: it needs `rawTrackPath` (`RoutingEngine.java:914`), which Forager's
  `pre-main` never sets, and `RoutingParamCollector` has no key for it (both by `git grep`, no hit).
**Decision (the planner's, technical):** neither of Plumb's options.
- (a), JDK 21's wording copied into common code, would put a made-up JVM message on iOS, against "never a fabricated
  plausible value".
- (b), the empty track ruled out, removes the one input that reaches the error path.
- **Instead:** the port writes its own exception's description after BRouter's prefix. On the error path, the test
  compares the result exactly, and `debugInfo` exactly up to and including `"Error reading rawTrack: "`. It asserts that
  both engines took that path and that each wrote some text after the prefix. On every other path, `debugInfo` is
  compared whole, as now. The port's code names the difference in a comment.
- **Disclosed as what it is:** a comparison changed after a result was seen, like -107's. The planner puts it beside -107's
  case for the independent reviewer, once that role is named.
**Notes:** Written by the planner session by hand. Next free ID -117.

**Kind:** outcome
**ID:** 2026-10-28-117
**Timestamp:** 2026-11-11T14:10:20Z
**Continues:** 2026-10-28-101, 2026-10-28-111, 2026-10-28-116
**Title:** Step 7, Plumb's session 24: `readBinary` passing under -116's comparison; `FormatCsv`, `FormatKml` and
`StringUtils` ported whole over failing stubs; revert (e) as predicted; `FormatJson` and `FormatGpx` not started
**Observed:** Plumb Ledger, session 24, `brouter-port-5b` `2f2eb38` to `2f602b3` (partial, fast-forward only; trailers
`Co-Authored-By: Claude <noreply@anthropic.com>`, no model name; checked by the planner). It ended at 178,184 by the
harness's count, under the watcher's 180,000.
- **`readBinary`:** `ReadBinaryTest` changed as -116 rules and seen red over the stubs (`60bb212`), then patch 16 applied with
  a comment naming the difference (`12af1e9`): 9,029 reads, 1,549 of them on the error path [R].
- **`FormatCsv`** whole, with `aggregateMessages` and `MessageData.toMessage`/`add`: red at `b1c735b`, passing at `8334bcd`
  on 61 tracks, 11,266 lines and `Formatter.write`'s bytes [R].
- **`FormatKml` and `btools.util.StringUtils`** whole: red at `0709046`, passing at `6abe8f3` on 248 placemarks, 7 null-name
  throws matched by class, and 20,006 strings [R]. `StringUtils` is BRouter's own class, outside step 7's §1 file list; it
  is not a new dependency.
- **Revert (e)** (`linkdist +=` to `-=` in `MessageData.add`), predicted in a pushed file: "track 0: format", 4,637
  characters on the oracle against 4,607, as predicted, `cmp`-equal restore [R].
- **Slips, Plumb's own (§11):** a full run killed by its own 10-minute background limit, re-run whole. One local commit
  scanned E1 at 1 (the KML header's fixed `<distance>` constant, not a position); never pushed, undone with
  `reset --soft`, the literal split with a comment, output unchanged, re-committed at 0.
**Checked by the planner:**
- `git ls-remote`: `2f602b3`, a fast-forward of `2f2eb38`; no model name in any message.
- E1 scan over `2f2eb38..2f602b3`: 0.
- `ReadBinaryTest` at `60bb212`: the result compared exactly; on the error path, `debugInfo` compared by digest up to and
  including `"Error reading rawTrack: "`, both paths asserted taken, text asserted after the prefix on each side; whole
  elsewhere. This is -116's ruling as written.
- **CI on `6abe8f3`** (run 43033483479): `jvm` 154, 2 failed, both `CollectOutreachersTest`'s; 0 ` e: `; both
  `compileKotlinIos*`; simulator 24 of 24 (**simulator-checked**); arm64 7 of 7, the gate "OK". CI on the docs-only
  `2f602b3` was still running and is not read.
**Next:** session 25 ports `FormatJson` (261 lines) and `FormatGpx` (573), each red first, and closes step 7.
**Notes:** Written by the planner session by hand. Next free ID -118.

**Kind:** outcome
**ID:** 2026-10-28-118
**Timestamp:** 2026-11-11T14:33:49Z
**Continues:** 2026-10-28-101, 2026-10-28-111, 2026-10-28-117
**Title:** Step 7, Plumb's session 25: `FormatJson` stubbed and red on purpose; JSON and GPX ports held unrun in patch 29;
the planner's ruling on `OsmTrack.version`
**Observed:** Plumb Ledger, session 25, `brouter-port-5b` `2f602b3` to `a775e5c` (partial, handed off at the watcher's
182,216; ended at 189,019 by the harness's count; trailers `Co-Authored-By: Claude <noreply@anthropic.com>`; checked by the
planner).
- **`3108507`, red on purpose:** `FormatJson` stubbed (`format`, `formatAsWaypoint`, `formatDecimal3` for Java's
  `DecimalFormat`), with `OsmTrack.aggregateSpeedProfile`, `getVoiceHint` and `getVoiceHintTime`. `FormatJsonTest` sets
  hints through `processVoiceHints` for modes 0 to 9, with speed, speed profile, itinerary, POIs, waypoints and export
  flags; `formatDecimal3` against `DecimalFormat` on 250,023 floats. Full fresh run: 156 tests, 4 failed
  (`CollectOutreachersTest`'s 2, `FormatJsonTest`'s 2 as `NotImplementedError` naming a stub), 0 ` e: ` [R].
- **E1:** a first scan of 1 on a float test constant, not a position; rewritten as the same value before the commit, 0 [R].
- **Held, never compiled or run:** `29-s25-partial-formatjson-formatgpx-port.patch` (the `FormatJson` port, the
  `OsmTrack` helpers, `FormatGpx` whole with `read`, and `FormatGpxTest`). `FormatGpx` was never seen red. The revert
  has a prediction in §12 but none was pushed, and it did not run. CI on `3108507` was not read.
**Checked by the planner:** `git ls-remote` `a775e5c`, a fast-forward of `2f602b3`; no model name; E1 over
`2f602b3..a775e5c`: 0. CI on `3108507` and `a775e5c` was still running and is not read here.
**Found by the planner, not raised by Plumb:** the stub sets `OsmTrack.version` to the constant `"1.7.10"`, the oracle
jar's `Implementation-Version`. Java reads it at run time from the package manifest (`OsmTrack.java:26`) and writes it
into GPX's and JSON's `creator` (`FormatGpx.java:497`, `FormatJson.java:219`). Forager builds BRouter as a plain
`java-library` whose `build.gradle.kts` sets no `Implementation-Version` (read on `pre-main`), so Forager's own output
would read `BRouter-null` [I: from the build file; not run on Android]. The constant matches the test's oracle, not
Forager.
**Decision (the planner's, technical):** `OsmTrack.version` becomes a settable `String?`, `null` by default, which is
what Forager's build gives. The port writes it as Java does (`"BRouter-" + version`, so `BRouter-null` when unset). The
tests set it from the oracle's own `OsmTrack.version` at run time and compare outputs whole. No version number is written
into common code. This is ruled before any port result for it was seen, so it is not a comparison changed after a result.
**Next:** session 26 applies -118, compiles and runs patch 29, ports `FormatGpx` red first, runs the GPX revert with its
prediction pushed first, reads CI, and closes step 7 if reached.
**Notes:** Written by the planner session by hand. Next free ID -119.

**Kind:** outcome
**ID:** 2026-10-28-119
**Timestamp:** 2026-11-11T16:57:23Z
**Continues:** 2026-10-28-107, 2026-10-28-116, 2026-10-28-118
**Title:** Step 7, Plumb's session 26: -118 applied; `FormatJson` matching the oracle, held on `formatDecimal3` above
2^63; the owner's ruling on it; the planner's ruling on the seeded speeds
**Observed:** Plumb Ledger, session 26, `brouter-port-5b` `a775e5c` to `4f5791c` (partial, a stop; ended at 144,549 by the
harness's count; trailers `Co-Authored-By: Claude <noreply@anthropic.com>`; checked by the planner).
- **-118 applied** (`7ddd4ef`): `OsmTrack.version` a settable `String?`, `null` by default; `FormatJsonTest` sets it from
  the oracle's value. Full fresh run: 156 tests, 4 failed (`CollectOutreachersTest`'s 2, `FormatJsonTest`'s 2 on purpose
  naming the stubs), 0 ` e: ` [R].
- **JSON port, run 1:** every JSON comparison equal, but the test's own reached-branch check on the speed profile failed:
  step 6's paths never set message speeds, so the profile was empty in both engines. Plumb then seeded those speeds alike
  in both engines (in `7ddd4ef`); the comparison and the reached-branch list are unchanged [R].
- **`formatDecimal3`** (Java's `DecimalFormat` `"0.###"`, used only on `getTime()`, `FormatJson.java:82-85`): wrong
  digits near 3e17 on run 1, fixed with the JDK's integer fast path. Run 2: `formatJsonMatchesTheOracle` passes;
  `formatDecimal3` fails near 2.8e22, above 2^63, 17 digits on Java against 16 [R]. Plumb stopped there: matching it means
  porting about 360 lines of the JDK's GPLv2 digit code. Port held in patch 30, runs in file 31. `FormatGpx` not started
  (patch 29's GPX calls `FormatJson.speedText`); no revert.
- **CI on `3108507`**, read by Plumb: `jvm` 156, 4 failed as expected, 0 ` e: `; simulator 24 and arm64 7, 0 failed, the
  gate "OK" (**simulator-checked**).
**Checked by the planner:** `git ls-remote` `4f5791c`, a fast-forward of `a775e5c`; no model name; E1 over
`a775e5c..4f5791c`: 0. BRouter's licence is MIT (`third_party/brouter/LICENSE`). `getTime()` returns a `float` of seconds
(`OsmPathElement.java:47`); 2^63 seconds is about 9.2e18, beyond any route (the universe is about 4e17 seconds old).
CI on `7ddd4ef` not read.
**Decision (the planner's, technical), on the seeded speeds:** not a stop. The failing check was a reached-branch
assertion, not an oracle comparison; the change adds inputs alike to both engines so the branch the test already demands
is reached, and loosens nothing. Recorded because inputs changed after a result was seen.
**Decision (the owner's), asked with the recommendation first, on `formatDecimal3` above 2^63:** "Stop at the limit
(Recommended)". The port matches Java exactly below 2^63 and throws a clear error above it, never printing wrong digits;
the test checks every value below the limit and checks for the error above it. No JDK code is copied. Because this narrows
a test after a result was seen, it goes beside -107's and -116's cases for the independent reviewer, once named.
- **Not yet shown:** that the port matches Java on every tested value from 3e17 up to 2^63 (session 26 named its failure
  near 2.8e22 only). If any value below 2^63 differs, that is a stop, not part of this ruling.
**Notes:** Written by the planner session by hand. Next free ID -120.

**Kind:** outcome
**ID:** 2026-10-28-120
**Timestamp:** 2026-11-11T18:18:07Z
**Continues:** 2026-10-28-111, 2026-10-28-118, 2026-10-28-119
**Title:** Step 7, Plumb's session 27: `FormatJson` and `FormatGpx` ported and passing; revert (f) as predicted; step 7
left open on `equalsTrack`, `addNodes` and `writeBinary`; the planner's rulings on them
**Observed:** Plumb Ledger, session 27, `brouter-port-5b` `4f5791c` to `f29366e` (fast-forward only; ended at 180,473 by
the harness's count; trailers `Co-Authored-By: Claude <noreply@anthropic.com>`; checked by the planner).
- **-119's premise, checked first:** every tested value from 3e17 up to 2^63 matches Java (1,982, 0 differ); below 3e17,
  222,747 match; at or above 2^63, 16 of 25,294 differ [R]. So -119's "not yet shown" is now shown on the tested values.
- **`FormatJson`** (`f1a5f22`): `formatDecimal3` throws `UnsupportedOperationException` naming 2^63 and -119 for finite
  values at or above 2^63. The test compares 224,731 floats exactly and asserts the throw on 25,292. Full run 156, 2 failed
  (`CollectOutreachersTest`'s) [R]. Plumb kept the infinities in the exact comparison (they match Java and print no
  digits); its own reading of -119.
- **`FormatGpx`:** red over stubs at `61a1377` (157, 3 failed, the GPX one a `NotImplementedError` naming
  `FormatGpx.format`), passing at `fc4d946` (157, 2 failed) [R]. Run 1 failed on `creator="BRouter-null"`: patch 29's test
  lacked -118's `@BeforeTest` setting `OsmTrack.version` from the oracle; Plumb added it, the comparison unchanged.
- **Revert (f)**, prediction pushed first: "track 0: format", the port 16 characters longer, as predicted, `cmp`-equal
  restore [R].
- **E1:** a first scan of 3 on the 2^63 constant written as a long decimal, not a position; rewritten before the commit.
- **Not covered:** `EL`/`ER`, `OFFR`, GPX's middle-waypoint type branches (compared, not counted). Kotlin/Native number
  formatting is [?] for step 10.
**Checked by the planner:**
- `git ls-remote` `f29366e`, a fast-forward of `4f5791c`; no model name; E1 over `4f5791c..f29366e`: 0.
- **CI on `fc4d946`** (run 43074102819): `jvm` 157, 2 failed, both `CollectOutreachersTest`'s; 0 ` e: `; both
  `compileKotlinIos*`; simulator 24 of 24 (**simulator-checked**); arm64 7 of 7, the gate "OK". The earlier runs are
  Plumb's reading; `f29366e` (docs only) not read.
- `FormatGpxTest`'s `@BeforeTest` sets the version from the oracle's own value, as -118 rules.
- Callers, by `grep` over the vendored tree and Forager's `app/src/main`: `equalsTrack` at `RoutingEngine.java:261`,
  `addNodes` at `RoutingEngine.java:991`; `writeBinary` has no caller anywhere.
**Decisions (the planner's, technical):**
- **The infinities:** Plumb's reading stands. -119 forbids wrong digits; an infinity prints none and matches Java.
- **The GPX `@BeforeTest`:** not a comparison change. It applies -118, ruled before any result, to a test written before
  -118. Recorded because it was added after a port result.
- **`equalsTrack` and `addNodes`:** ported in step 7, red first, since step 8's `RoutingEngine` calls both.
- **`writeBinary`:** not ported, recorded out of scope. Nothing in BRouter or Forager calls it, and a missing method
  cannot return a wrong value. The tests keep using the oracle's to feed `readBinary`. If a caller ever appears, it is
  ported then.
**Next:** session 28 ports `equalsTrack` and `addNodes` and closes step 7.
**Notes:** Written by the planner session by hand. Next free ID -121.

**Kind:** outcome
**ID:** 2026-10-28-121
**Timestamp:** 2026-11-11T18:50:13Z
**Continues:** 2026-10-28-111, 2026-10-28-120
**Title:** Step 7 closed: Plumb's session 28 ports `equalsTrack` and `addNodes`; revert (g) as predicted; the planner's
check of the close
**Observed:** Plumb Ledger, session 28, `brouter-port-5b` `f29366e` to `461e4d6` (fast-forward only; ended at 127,241 by
the harness's count; trailers `Co-Authored-By: Claude <noreply@anthropic.com>`; checked by the planner). No stops.
- **Red first** (`cb8e1e1`): `equalsTrack` and `addNodes` stubbed with `TrackEqualsAddNodesTest`; 2 tests, 2 failures,
  each a `NotImplementedError` naming its stub [R].
- **Port** (`d49a3ff`), passing on the first run, the test unchanged: `equalsTrack` 240 true and 1,180 false, every
  variant reached; `addNodes` 120 runs, 111,426 nodes, digests equal. Full fresh run: 159 tests, 2 failed
  (`CollectOutreachersTest`'s), 0 ` e: ` [R].
- **Revert (g)** (the latitude comparison dropped), prediction pushed first: "walk 0: lat at 0, direction 0: equalsTrack
  expected false, was true", as predicted, `cmp`-equal restore [R].
- **§15** of the step 7 report holds the closing table.
**Checked by the planner:**
- `git ls-remote` `461e4d6`, a fast-forward of `f29366e`; no model name; E1 over `f29366e..461e4d6`: 0.
- **CI on `d49a3ff`** (run 43079285759): `jvm` 159, 2 failed, both `CollectOutreachersTest`'s; 0 ` e: `; both
  `compileKotlinIos*`; simulator 24 of 24 (**simulator-checked**); arm64 7 of 7, the gate "OK". `461e4d6` (docs only) was
  still running and is not read.
- Stubs in `commonMain` at `461e4d6`, by `git grep`: 3, all in `Route.kt` and all marked for step 8's
  `RoutingEngine`; none in step 7's files.
**Step 7 is closed,** with these left open and named:
- **Not ported:** `writeBinary` (-120, no caller).
- **Not covered by a test:** `EL`/`ER`, `OFFR`, GPX's middle-waypoint type branches (compared, not counted).
- **Unverified:** Kotlin/Native number formatting [?], for step 10. Every iOS result is simulator-checked only.
- **For the independent reviewer, once named:** the comparisons changed after a result in -107, -116 and -119.
- **Still failing, known:** `CollectOutreachersTest`'s 2 (-61), for step 10.
**Next:** step 8, `RoutingEngine`, with heights per route point (-111) and via points out (-24).
**Notes:** Written by the planner session by hand. Next free ID -122.

**Kind:** outcome
**ID:** 2026-10-28-122
**Timestamp:** 2026-11-11T19:15:27Z
**Continues:** 2026-10-28-24, 2026-10-28-90, 2026-10-28-111, 2026-10-28-121
**Title:** Step 8a, Plumb's session 29: `RoutingEngine`'s survey and test plan; F0's test red over the stub; the 8a port
held uncompiled in a patch; the planner's answers to two questions
**Observed:** Plumb Ledger, session 29, `brouter-port-5b` `461e4d6` to `2fddfcc` (partial, handed off at the watcher's
181,688; ended at 212,100 by the harness's count; trailers `Co-Authored-By: Claude <noreply@anthropic.com>`; checked by
the planner). No stops.
- **`22266d1`, pushed alone first:** `docs/audits/2026-11-11-brouter-port-step8.md` §1 places every method and field
  group of `RoutingEngine` in 8a, 8b, 8c, 8d or "left out", with its evidence; §2 is 8a's test plan, the revert named
  (`testPath.cost <` flipped to `>`). Via points are 424 lines in `RoutingEngine`; -24's other 5 (`getFromDetourMap`)
  were ported at step 7. Unported classes it calls: `AreaReader` and `StackSampler`, both left out.
- **`15a4aa2`, red on purpose:** `RoutingEngine` stubbed with `RoutingEngineF0Test`, which first holds the in-JVM oracle
  to `expected-1b.tsv` (passed), then compares the port field by field. 1 test, 1 failure, a `NotImplementedError` naming
  `RoutingEngine.doRun`. Full fresh run: 160 tests, 3 failed (`CollectOutreachersTest`'s 2, F0 on purpose), 0 ` e: ` [R].
- **Held, never compiled or run:** `02-partial-routingengine-8a.patch`. CI not read by Plumb.
**Checked by the planner:**
- `git ls-remote` `2fddfcc`, a fast-forward of `461e4d6`; no model name; E1 over `461e4d6..2fddfcc`: 0. CI on
  `15a4aa2` and `2fddfcc` was still running and is not read.
- **§1's premises about Forager, read on `pre-main` at `12eb9697`** (5 commits past the coder's checkout; no diff under
  `app/.../routing` or `third_party/brouter`): `BRouterTrailRouter.kt` passes three parameters only (`profile`,
  `alternativeidx` "0", `profile:use_dynamic_range` "0"), sets `quite = true`, builds the six-argument engine with both
  file bases `null` in `ROUTING` mode, and calls `doRun(maxRunningTimeMillis)` on its own thread. These hold.
- **Not checked by the planner:** §1's other "left out" rows, line by line. Each throws by name if reached, so a wrong
  row shows as a refusal, not as a wrong route.
**Decisions (the planner's, technical), on Plumb's two questions:**
1. **Heights at 8d change `RouteOutcome.Found`:** yes, wanted. That is what the owner's -111 ruling means in code.
2. **`debug.txt`:** the port throws a named "unsupported" if it or `stacks.txt` exists where Java looks, and is compared
   in E3's unlogged regime only. That is the right reading: Forager puts no such file there [I: Plumb's, from the profile
   folder], and logging to a file is not part of a route. **Recorded as not covered:** the port in E3's logged regime.
**Next:** session 30 reads CI, applies the patch fixing compile errors only, runs F0, then the revert, then R and DL.
**Notes:** Written by the planner session by hand. Next free ID -123.

**Kind:** outcome
**ID:** 2026-10-28-123
**Timestamp:** 2026-11-11T20:12:38Z
**Continues:** 2026-10-28-89, 2026-10-28-90, 2026-10-28-122
**Title:** Step 8a, Plumb's session 30: the search ported, F0 passing; the planned revert not caught by F0; R and DL red on
8b's memory path; -90's premise corrected; the planner's rulings
**Observed:** Plumb Ledger, session 30, `brouter-port-5b` `2fddfcc` to `0ab64b6` (fast-forward only; ended at 137,970 by
the harness's count; trailers `Co-Authored-By: Claude <noreply@anthropic.com>`; checked by the planner).
- **The 8a port** (`4dcd049`): session 29's patch applied clean and compiled with 0 errors, no fix needed. F0 passes, both
  routes equal to the oracle on all six fields. Full fresh run: 160 tests, 2 failed (`CollectOutreachersTest`'s) [R].
- **The §2 revert** (`testPath.cost <` flipped to `>`), prediction pushed first (`70c4c20`): **the prediction failed.** The
  reverted build compiled and ran, and F0 still passed. `cmp`-equal restore [R]. So F0 through the port has only been
  seen red over the stub, never over a wrong port.
- **R and DL** (`377d109`, red on purpose): both fail with a `NotImplementedError` naming 8b's memory path. Full run: 163
  tests, 5 failed (the known 2, R 1, DL 2) [R].
- **Diagnostics, run but not committed** (tests restored `cmp`-equal) [R, Plumb's only]: on R, 151 of the 1,180 StdModel
  cases reach the 8b stub and the other 1,029 equal the oracle; the 354 Kinematic cases are refused by name (-89). On DL,
  36 of 59 (lat) and 15 of 59 (lon) reach the stub, the rest equal. With the §2 revert applied, R shows 176 mismatches; F0
  and DL none.
- `E3DLTest`'s companion changed from `private` to `internal`, so the new DL test reads its seeds.
**Checked by the planner:**
- `git ls-remote` `0ab64b6`, a fast-forward of `2fddfcc`; no model name; E1 over `2fddfcc..0ab64b6`: 0.
- The test diff: two new test files and the one visibility word in `E3DLTest`; no comparison in an existing test changed.
- **CI on `377d109`** (run 43092122738): `jvm` 163, 5 failed: `CollectOutreachersTest`'s 2, `RoutingEngineRTest` 1,
  `RoutingEngineDLTest` 2; 0 ` e: `; both `compileKotlinIos*`; simulator 24 of 24 (**simulator-checked**); arm64 7 of 7,
  the gate "OK". The earlier runs are Plumb's reading.
**Corrected, the planner's own premise:** -90 put R and DL in 8a as "the search without memory pressure". That was not
checked and is false: 151 of R's StdModel cases and 51 of DL's 118 reach the memory path. -90 is corrected in place.
**Decisions (the planner's, technical):**
1. **R and DL stay red until 8b,** whole. No split by whether a case reaches the memory path: that would change a
   comparison after a port result, and 8b is next anyway.
2. **F0 needs a revert of its own that bites.** R catching the §2 revert shows the search is tested, but a test that has
   never caught a wrong port is not yet shown to test anything. Session 31 predicts one F0 catches, pushed first.
3. **8a is not closed** until R and DL pass, which is at 8b. 8b then adds its own strata (MP64, ML5, PM5) and -90's revert
   (`isInMemoryBounds`' comparison flipped).
**Notes:** Written by the planner session by hand. Next free ID -124.

**Kind:** outcome
**ID:** 2026-10-28-124
**Timestamp:** 2026-11-11T21:52:00Z
**Continues:** 2026-10-28-61, 2026-10-28-90, 2026-10-28-123
**Title:** Steps 8a and 8b, Plumb's session 31: the memory path ported; R, DL, MP64, ML5 and PM5 equal to the oracle; three
reverts caught as predicted; one false line in the report; the planner's rulings
**Observed:** Plumb Ledger, session 31, `brouter-port-5b` `0ab64b6` to `8deeb46` (fast-forward only; ended at 162,038 by
the harness's count, under the watcher's 170,000; trailers `Co-Authored-By: Claude <noreply@anthropic.com>`; checked by
the planner).
- **A revert F0 catches** (prediction pushed first, `b62ea53`; result `97b4167`): `compileTrack`'s end-node cut on the
  final track removed. F0 failed on nodes, 226 expected against 227, cost equal, as predicted [R, Plumb's]. Restored.
- **8b, the memory path** (`543693e`): `RoutingEngine.java:1970-2040` into `findTrackInner`. R passes with 1,534 cases and
  0 mismatches; DL-lat and DL-lon 59 each, 0 mismatches.
- **MP64, ML5, PM5** (`31aa7b1`, new `RoutingEngineMemoryTest`): 59, 59 and 55 cases, 0 mismatches each.
  `E3MemoryTest`'s companion changed from `private` to `internal`.
- **-90's revert** (`isInMemoryBounds`' `<=` flipped; prediction `e501548`, result `a3a246b`): MP64 59 of 59, PM5 55 of
  55, DL 59 and 59, R 805 mismatches; ML5 0; F0 also red (not predicted) [R, Plumb's]. Restored.
- **A revert only ML5 catches** (the limit's message changed; prediction `7e24058`, result `45c653b`): ML5 59 of 59 on
  kind, the other six groups 0 [R, Plumb's]. Restored.
- `CollectOutreachersTest`'s 2 failures are unchanged (-61).
**Checked by the planner:**
- `git ls-remote` `8deeb46`, a fast-forward of `0ab64b6`; 9 commits, one trailer each, no model name; E1 over
  `0ab64b6..8deeb46`: 0.
- **The port against the Java, line by line** (`RoutingEngine.java:1970-2038`): the same statements in the same order.
  The two `logInfo` lines and their two counters are left out; nothing else differs.
- **The reverts were restored:** no code differs between `0ab64b6` and `97b4167`, nor between `31aa7b1` and `8deeb46`.
- **The test diff:** one new test file and the one visibility word in `E3MemoryTest`. No comparison in an existing test
  changed.
- **CI on `543693e`** (run 43098746396): `jvm` 163 tests, 2 failed, both `CollectOutreachersTest`; 0 ` e: `; both
  `compileKotlinIos*`; simulator 24 of 24 (**simulator-checked**); arm64 7 of 7, the gate "OK".
- **CI on `31aa7b1`** (run 43099991963): `jvm` 166 tests, the same 2 failed; 0 ` e: `; both `compileKotlinIos*`;
  simulator 24 of 24 (**simulator-checked**); arm64 7 of 7, the gate "OK". Its log prints all six groups at 0
  mismatches. The head's own run (docs only) was still in progress at this reading.
- The three reverts' own results are Plumb's reading; their runs were local.
**A false line in the report (§5, `:264`):** "No stack overflow or out-of-memory in any memory-path run (the 5b graph
half is iterative)." The port's `cleanupPeninsulas` is recursive (`OsmNodesMap.kt:58-65`), and on the JVM it catches a
`StackOverflowError` and carries on, as Java does; -61's rejected option was the rewrite without recursion. What CI's
log on `31aa7b1` does show [R]: the catch's line is printed 3 times, all by `CollectOutreachersTest`, and by none of the
six route groups. So the first half of the line holds on the JVM in CI, and the reason given for it is wrong. Session 32
corrects the line in place.
**What this does not show:**
- **The collection itself still differs from Java's on one loaded region** (`CollectOutreachersTest`, -61), while every
  route through it is equal. Equal routes on 1,825 cases do not show the two collections equal.
- No overflow in the route groups on the JVM says nothing about iOS, where the stack is smaller and there is no catch
  (-50). That stays with step 10.
- None of the route comparisons runs on iOS. They are JVM tests.
**Decisions (the planner's, technical):**
1. **8a is closed.** R and DL pass whole, and F0 has caught a wrong port (-123's terms).
2. **Plumb's question 1: no revert that MP64 alone catches is owed.** -90 asked that a wrong memory path turn its strata
   red, and -90's revert turned MP64 red on 59 of 59. "Alone" was never the requirement. The same holds for PM5.
3. **Plumb's question 2: yes.** A changed message is not a changed decision. Session 32 runs two more reverts,
   predictions pushed first: one in the condition that throws the limit, one in a panic-mode line (the refill of the open
   set, or one of the two flags). Each prediction names the groups expected to fail. A revert that could loop for ever is
   run under a time limit, and a hang is reported as such.
4. **8b closes when those two reverts are caught,** or their misses are reported and ruled on.
5. **Next: 8c** (terminate, the time limit, the progress count, `catch (Error)`), then 8d.
**Notes:** Written by the planner session by hand. Next free ID -125.

**Kind:** outcome
**ID:** 2026-10-28-125
**Timestamp:** 2026-11-11T23:08:00Z
**Continues:** 2026-10-28-90, 2026-10-28-124
**Title:** Step 8b closed: Plumb's session 32, two more reverts caught, one prediction failed on its counts; 8c drafted
only; the planner's rulings
**Observed:** Plumb Ledger, session 32, `brouter-port-5b` `8deeb46` to `3fc4341` (fast-forward only; called at 170,243,
ended at 181,509 by the harness's count; trailers `Co-Authored-By: Claude <noreply@anthropic.com>`; checked by the
planner). Docs only: no code changed.
- **§5's false line corrected in place** (`4c096b7`), marked as the planner's correction. A fresh full run on `8deeb46`:
  166 tests, 2 failed (`CollectOutreachersTest`); the caught-overflow line 3 times, all in that class [R, Plumb's].
- **Revert (a), the limit's condition** (`maxTotalCost < 1000000000` to `<=`; prediction `1adb40c`, result `fdc5ec9`):
  PM5 55 of 55 mismatches, the other six groups 0. The prediction held [R, Plumb's].
- **Revert (b), panic mode's refill** (the refill's `openSet.add` removed; prediction `71a466e`, result `3fc4341`): PM5
  48 of 55, ML5 53 of 59, the other five groups 0. **The prediction failed on its counts** (at least 50 and 54 were
  predicted) and held on which groups fail [R, Plumb's]. Which 7 PM5 and 6 ML5 cases stay equal, and why, is not
  explained.
- **8c is not in code.** Two uncompiled drafts are saved as text (`step8/10-8c-draft-*.txt`).
**Checked by the planner:**
- `git ls-remote` `3fc4341`, a fast-forward of `8deeb46`; 5 commits, one trailer each, no model name; E1 over the range:
  0; nothing outside `docs/` changed, so both reverts were restored and no test changed.
- Both predictions precede their results in the history.
- `fastPartialRecalc` in the port: `findTrackInner` refuses it with a `check` (`RoutingEngine.kt:619`), and in Java it is
  the only way to `maxRunningTime / 3` (`RoutingEngine.java:1980`).
- No code CI to read. The revert results are Plumb's reading of local runs.
**Decisions (the planner's, technical):**
1. **Plumb's question 1: no second try is owed for revert (b).** What -124 asked is shown: a wrong refill turns 101
   cases red in the two groups that panic and none elsewhere. The prediction stays recorded as failed on its counts, and
   the unexplained 13 cases stay named as unexplained. They are not pursued: the port was `cmp`-equal throughout, so the
   miss is in the coder's model of the cases, not in the port.
2. **8b is closed.** Not covered, as -124 names: the collection on `CollectOutreachersTest`'s region (-61), and iOS.
3. **Plumb's question 2: no test of `maxRunningTime / 3` is owed.** No test can reach it while the port refuses
   `fastPartialRecalc`. 8c ports the plain time limit and names the third as left out, with the `check` as its guard.
4. **Next: 8c** in code, from the drafts, tests seen failing first.
**Notes:** Written by the planner session by hand. Next free ID -126.

**Kind:** outcome
**ID:** 2026-10-28-126
**Timestamp:** 2026-11-12T00:02:00Z
**Continues:** 2026-10-28-90, 2026-10-28-125
**Title:** Step 8c, Plumb's session 33: tests red over stubs, the port held as a patch; a stop on a draft test's false
premise; the JVM job hit its 30-minute limit; the planner's rulings
**Observed:** Plumb Ledger, session 33, `brouter-port-5b` `3fc4341` to `486588d` (fast-forward only; ended at 150,599 by
the harness's count, no handoff call; trailers `Co-Authored-By: Claude <noreply@anthropic.com>`; checked by the planner).
- **Red, pushed** (`124e6da`): `RoutingEngineStopTest` (9 tests: FK9, FK10, FK11, FK11's link count, and five on an
  injected clock or a thrown error), `EngineClock`, three stubs. 9 of 9 fail on a `NotImplementedError`.
  `E3FKTest`'s companion changed from `private` to `internal`.
- **The port, not in the tree** (`step8/11-8c-port.patch`, per the stop rule): with it, 8 of 9 pass; full run 175 tests,
  2 failed [R, Plumb's].
- **The stop:** `theTimeLimitReadsTheInjectedClock` expects "pass0 timeout after 30 seconds" and the port gives
  "target-island-check timeout after 30 seconds". Plumb made no fix. No revert was run.
- Locally `CollectOutreachersTest` failed 1 of 2 with 0 caught-overflow lines [R, Plumb's; ?].
**Checked by the planner:**
- `git ls-remote` `486588d`, a fast-forward of `3fc4341`; 2 commits, one trailer each, no model name; E1 over the range: 0.
- **The premise is false by the Java** [R]: `RoutingEngine.java:961` runs `findTrack("target-island-check", ...)` for
  every section before any pass, and the time limit is checked inside `_findTrack`'s loop (`:1979-1983`) under whatever
  operation name it was given. On a clock already past the limit, the first operation to strike is the island check. The
  expected text was session 32's hand-written reading, never an oracle's output. The port's answer agrees with the Java.
- **FK9's pattern has the same gap:** `(pass0|pass1|re-tracking) timeout after 0 seconds` leaves the island check out. It
  passes only while the island check finishes inside the 50 ms limit, on both engines.
- **CI on `124e6da`** (run 43118062551): `jvm` 175 tests, 11 failed: the 9 red on purpose and `CollectOutreachersTest`'s
  2; 0 ` e: `. **The job was cancelled at its 30-minute limit:** the test step alone ran 29.5 minutes, so "iOS klibs" was
  cancelled and `compileKotlinIosArm64` did not run. `ios-smoke`: `compileKotlinIosSimulatorArm64` ran, simulator 24 of
  24 (**simulator-checked**), arm64 7 of 7, the gate "OK". This run is over the stubs.
- **The caught-overflow line in that CI log: 4 prints,** against 3 in the earlier runs. With Plumb's local 0 and 1 of 2,
  the known pair is not stable in how it fails. That fits a stack-depth effect (-61) and shows nothing new about the port.
- The patch, skimmed against the Java, not yet line by line: the loop head (`:1975-1984`), the dirty track on a timeout
  (`:1749-1758`), `terminate`, `cleanOnOOM`, the link count. It reads a monotonic clock where Java reads the wall clock;
  the difference is named in `EngineClock`.
**Decisions (the planner's, technical):**
1. **The test's expected text is corrected to the Java's, and FK9's pattern gains `target-island-check`.** This is a
   comparison changed after a port result, so it is done under these terms and **flagged for the independent reviewer**
   with -107, -116 and -119:
   - evidence from the oracle alone is pushed first: the oracle run with a 1 ms limit on FK9's cases, the first words of
     its messages counted, no port in the run;
   - the change is a commit of its own, naming this entry, made while the port is still out of the tree, and seen red over
     the stubs again;
   - nothing else in either comparison moves. `start-island-check` is not added: Forager never routes inverse.
2. **The JVM job's limit goes from 30 to 60 minutes** (`brouter.yml:18`). It weakens no check; at 30 the klib compile is
   lost. The suite's length is now a cost on every push and is named as such.
3. **The monotonic clock is accepted** as a named difference from Java.
4. **Not covered:** `catch (Error)` on Kotlin/Native, where an out-of-memory normally ends the process. Step 10.
5. **Next:** session 34 does 1 and 2, applies the patch, runs 8c's two reverts.
**Notes:** Written by the planner session by hand. Next free ID -127.

**Kind:** outcome
**ID:** 2026-10-28-127
**Timestamp:** 2026-11-12T01:50:00Z
**Continues:** 2026-10-28-90, 2026-10-28-126
**Title:** Step 8c closed on the JVM: Plumb's session 34, the port in the tree, 9 of 9 stop tests passing, two reverts
caught; FK9 weakened by -126's change, named; the planner's rulings
**Observed:** Plumb Ledger, session 34, `brouter-port-5b` `486588d` to `94db5d1` (fast-forward only; ended at 141,180 by
the harness's count, no handoff call; trailers `Co-Authored-By: Claude <noreply@anthropic.com>`; checked by the planner).
- **In -126's order:** the CI limit (`3840315`); the oracle alone at 1 ms on FK9's cases names `target-island-check` 10
  of 10 (`47dcb5d`) [R, Plumb's]; the comparison change with the port out of the tree, red 9 of 9 over the stubs
  (`af875c6`); the patch applied unchanged, 9 of 9 passing (`1d1488b`).
- **Revert (a), `terminated` written but not read** (prediction `fa3a76c`): FK10, FK11 and the watchdog test fail, the
  7 route groups pass. Held [R, Plumb's].
- **Revert (b), the time limit's comparison flipped** (prediction in `42c6913`, result `d398b39`): the stopped-clock and
  injected-clock tests fail, as predicted; one predicted message was wrong (the port said "re-tracking", since
  re-tracking resets the clock, `RoutingEngine.java:1797`). **FK9 passes under this revert** [R, Plumb's].
**Checked by the planner:**
- `git ls-remote` `94db5d1`, a fast-forward of `486588d`; 8 commits, one trailer each, no model name; E1 over the range: 0.
- **The CI change is the one line** (`timeout-minutes: 30` to `60`).
- **The test change is what -126 allowed and no more:** the one expected text, the one pattern, and the class comment
  that describes them. The oracle-only evidence and the change both precede the port's commit.
- **The tree's port equals the patch** session 33 held back, line for line.
- **The port against the Java, line by line:** the loop head (`:1975-1984`, without the third), the dirty track on a
  timeout (`:1748-1760`), the clock's reset before re-tracking (`:1797`), `terminate`, `isTerminated`, `cleanOnOOM`, and
  `doRun`'s three catches (`:318-326`). The same statements in the same order; the log lines are left out.
- **CI on `1d1488b`** (run 43128205990): `jvm` 175 tests, 2 failed, both `CollectOutreachersTest`; 0 ` e: `; the test
  step ran 28 minutes and "iOS klibs" was reached, both `compileKotlinIos*`; FK9, FK10 and FK11 print 0 mismatches of 10
  each, FK11's link counts equal on all 10; simulator 24 of 24 (**simulator-checked**); arm64 7 of 7, the gate "OK". The
  caught-overflow line: 4 prints. The three later runs were in progress at this reading.
**A cost of -126's ruling, the planner's own:** widening FK9's pattern made FK9 blind to a time limit that strikes at
once. Before the change its pattern would have refused "target-island-check"; now a flipped limit passes it. What still
catches that error is the two tests on a driven clock, which have no oracle beside them. In CI both engines answered
"pass0" on 20 of 20, so the widening was not needed for that run; it was needed for a slow machine.
**Decisions (the planner's, technical):**
1. **Plumb's question 1: FK9 is not changed again.** A guard would be a second comparison change after a result. The
   weakening is named here and goes to the independent reviewer with -126.
2. **Plumb's question 2: the dirty track on a timeout is settled in 8d.** Session 35 first shows whether anything on
   Forager's path reads `foundRawTrack` after a failed run. If something does, a test on a driven clock that strikes in
   pass1 is owed. If nothing does, it is recorded as ported and not covered.
3. **8c is closed on the JVM.** Not covered: `catch (Error)` on Kotlin/Native (-126); the third of the limit, left out
   (-125); the dirty track, until 2 is answered; nothing in 8c runs on iOS beyond compiling.
4. **Next: 8d.**
**Notes:** Written by the planner session by hand. Next free ID -128.

**Kind:** outcome
**ID:** 2026-10-28-128
**Timestamp:** 2026-11-12T03:25:00Z
**Continues:** 2026-10-28-42, 2026-10-28-90, 2026-10-28-111, 2026-10-28-122, 2026-10-28-127
**Title:** Step 8d begun: Plumb's session 35, the survey, the plan and 4 red tests; the dirty track is not read by
Forager; heights stay raw in `Found`; the planner's rulings
**Observed:** Plumb Ledger, session 35, `brouter-port-5b` `94db5d1` to `9e0c17f` (fast-forward only; ended at 157,585 by
the harness's count, no handoff call; trailers `Co-Authored-By: Claude <noreply@anthropic.com>`; checked by the planner).
- §9 of the step 8 report: 8d's survey and test plan, written and corrected before any code.
- `RouteTest` (`1b7b688`), red on purpose: 4 tests, 4 `NotImplementedError`. `RouteOutcome.Found` gains
  `sElev: ShortArray`; an internal `route` takes the libm.
- The port is left for session 36.
**Checked by the planner:**
- `git ls-remote` `9e0c17f`, a fast-forward of `94db5d1`; 5 commits, one trailer each, no model name; E1 over the range: 0.
- Code changed: `Route.kt` (the new field, the internal overload as a stub) and the new `RouteTest`. No existing test
  changed.
- **The dirty track** [R]: Forager returns the failure as soon as `getErrorMessage()` is not null and reads
  `getFoundTrack()` only otherwise (`BRouterTrailRouter.kt:133-139` at Forager `947f1be2`); no `.kt` file under `app`
  names `getFoundRawTrack`, `foundRawTrack`, `rawTrackPath` or `readBinary`.
- **Heights in Forager** [R]: `heightMetresOf(selev)` is `null` for `Short.MIN_VALUE`, else `selev / 4.0`
  (`BRouterTrailRouter.kt:219`, `:228`), read from each node's `getSElev()`.
- **CI on `1b7b688`** (run 43138707152): `jvm` 179 tests, 6 failed: `RouteTest`'s 4 red on purpose and
  `CollectOutreachersTest`'s 2; 0 ` e: `; both `compileKotlinIos*`; simulator 24 of 24 (**simulator-checked**); arm64 7
  of 7, the gate "OK". The caught-overflow line: 4 prints.
**Decisions (the planner's, technical):**
1. **The dirty track is ported and not covered** (-127, decision 2). Nothing on Forager's path reads it; no test is owed.
2. **Plumb's question 1: `Found` carries Java's raw `sElev`,** quarter metres, `Short.MIN_VALUE` for no height. `Found`
   already carries raw `iLat` and `iLon`, and Forager's Android adapter does all three conversions. The iOS adapter will
   use Forager's rule as read above. This is the owner's -111 ruling in code (-122), not a new shape.
3. **Plumb's question 2: no JaCoCo on the port's tests.** -42's 19 getters are shown by a scratch counting patch, never
   committed: a counter in each, one run of R, the counts reported. A getter at 0 is named as not reached.
4. **Plumb's question 3: nothing more of `checkDataFile`.** It was ported and tested at 5a; §1's row is corrected in
   place to say so.
5. **Next:** session 36 ports 8d, runs its reverts, the getter counts and the coverage read of `OsmPath.java:282-283`
   and `:350`.
**Notes:** Written by the planner session by hand. Next free ID -129.
