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
