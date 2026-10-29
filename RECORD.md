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
