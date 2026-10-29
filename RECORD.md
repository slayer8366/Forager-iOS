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
