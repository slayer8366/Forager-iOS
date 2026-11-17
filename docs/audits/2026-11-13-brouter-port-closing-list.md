# The BRouter port's closing list

Plan item 6 (`docs/plans/2026-11-12-app-port-plan.md`, part one): every "not covered" and "not shown" the record names
for steps 7 to 10, in one place, each with where it is owed. Written by the planner from `RECORD.md`, entries -121 to
-173, on 2026-11-13.

**How to read it.** Each line cites the record entry that names the gap, and the entry that closed it where one did.
"Closed" means the record says a test now covers it; the planner read those entries' own words, and did not rerun any
test for this list. A gap with no closing entry is open. Nothing here is new evidence. Every iOS result anywhere in
the record is from the simulator on a debug build ("simulator-checked").

## A. Open, and only a phone can close them (phase F, or the first device build)

| Gap | Named in | Owed to |
|---|---|---|
| A real iPhone: nothing in the port has run on one | every step 10 entry; plan section 2 | The first device build. The owner has no Mac and no iPhone (RECORD -2). |
| A release build. Every simulator run is a debug build | -142, -143 | The first release build |
| Speed and memory on a device (-24's 25% flag has no iOS measurement) | plan section 2 | Phase F |
| A real out-of-memory on iOS: the system ends the process before any catch. `catch (Error)` is shown on Native only with a thrown error | -126, -148 | Phase F; the adapter must not rely on a catch |
| The stack on a secondary thread: after the rewrite the ladder survives down to 512 KB on the simulator, on four cases in one tile | -143, -144, -146 | The adapter (phase C) names the thread and its stack size; a device run |
| Android's own runtime: the Java engine was run on a desktop JVM, not on a phone | -147; plan section 2 | Not owed by the port. Named so "equal to Java" is not read as "equal to Android" |

## B. Open, and a test here could close them

| Gap | Named in | Where it is owed |
|---|---|---|
| 8 of the 13 table rows where the port's cosine differs from desktop Java's lie in no test tile | -136, -137 | The owner, with the cost of capturing a tile for them (-137 decision 2). Not asked yet |
| FK 9 to 11 and R's 354 refusals have no production comparison; FK9's 3 unstable rows are held by family only; R's cases were not counted for rows touched | -137 | Open. No session is planned for it |
| On the simulator: R's other 940 cases and FK 9 to 11 have no run (802 cases do) | -140, -142, -143 | Open. FK 9 to 11 need the oracle, which iOS does not have; the stop tests have common siblings instead (-148, -169) |
| Deep recursion elsewhere in the engine: the ladder runs four cases in one tile | -146 | Open; a deeper map would show it |
| The order in which the rewritten walk visits children is argued equal, not shown | -147 | Accepted as not caught (-147 decision 1). Open as an argument |
| NT: why 36 of its 59 cases do not move under the order plant | -134 | Open, named |
| The number text: 11 of the 12 by-value places have no plant; only the GPX time was planted | -163, -168 | Open. The review of the test changes since -163 reads it |
| The by-value comparison means iOS exports are not byte-for-byte Java's in the 12 places that print a number | -163 (owner: "Record it, compare by value (Recommended)") | Ruled. Owed: the port's own note of what it does not match (-163) |
| The test JVM's flag `-XX:-OmitStackTraceInFastThrow`: its cause is inferred; the lost message never reproduced | -165, -167 | Open as an inference |
| FK9's guard is JVM-only, and a limit that strikes early in re-tracking after 50 ms would pass it | -170 | Open, named, not planted |
| `ReadBinaryTest`'s floors were not rederived by the planner; another JDK's wording was not seen | -171 | The review of the test changes since -163 |
| The oracle-only 1 ms test depends on the oracle jar's internals; that the hold changes the answer rests on an uncommitted check | -173 | The review of the test changes since -163 |
| The stopped-clock sibling's plant ran on the JVM only | -169 | Open; -139 allows a planted commit on `brouter-port-5b` to see it on the simulator |

## C. Not covered, and not on Forager's path (recorded, no test owed)

| Gap | Named in | Why no test is owed |
|---|---|---|
| `EL`/`ER`, `OFFR`, GPX's middle-waypoint type branches: compared, not counted | -121 | No ruling asks for a count |
| The port in E3's logged regime | -122 | Forager puts no log file there; logging is not part of a route |
| The third of the time limit (`maxRunningTime / 3`) | -125 | The port refuses `fastPartialRecalc`; no test can reach it |
| The dirty track after a failed run | -127, -128 | Nothing on Forager's path reads it |
| `getTrafficSourceDensity`; the start direction; the reference-track penalty | -132 | No call site, or reached by none of Forager's three parameters (the last is inferred, -132) |
| `readBinary`'s error path beyond the JVM | -116, -171 | The port refuses `rawTrackPath`; Forager never sets it |

## D. Named once as not covered, closed since

| Gap | Named in | Closed by |
|---|---|---|
| Kotlin/Native number formatting | -121 | -156, -160, -164: the one difference found, ruled by the owner, compared by value |
| The collection on `CollectOutreachersTest`'s region | -61, -124, -125, -132 | -146 (the overflow gone) and -148 (the test passes with its assertion unchanged) |
| `catch (Error)` on Kotlin/Native, with a thrown error | -126, -127 | -148 (see A for what it does not cover) |
| A real height through `route()`; the public cancel | -129 | -130, each seen red under its revert |
| The constructor's exception through `route()` | -131 | -132, by its title; the planner did not reread the test for this list |
| FK9 blind to a limit that strikes at once | -127, -132 | -170 (the guard), on the JVM |
| The production runs | -134 | -136, -137: 1,742 of 1,742 |
| NT-0062's hints in a logic run | -150 | -154, by a scratch test since deleted, so it cannot be rerun from the tree |
| The five tests changed after a result (-107, -116, -119, -126, -148) | plan section 2 | -163 (the review); its five should-fix items: -167, -170, -171, -173 |

## E. In flight when this was written

- The public entry for the route-ways reader (plan item 4; shape in -172; session 68) and its simulator sibling.
- One review of the test changes since -163 (sessions 60, 62, 64, 65, 66, 67).

Added 2026-11-14, after -174: the public entry landed (session 68, `readRouteWays`, jvm 249 of 249, compiled for the
simulator but run by no simulator test). Its gaps, for section B: no described link with `reverse` true in either point
set, no point spanning two squares, no real square of another divisor, the exceptions' pass-through not run. The
simulator sibling (session 69) and the review are still in flight.

Added 2026-11-14, after -179: nothing in this section is in flight any more. The simulator sibling landed (-175), the
second review found two should-fix items (-176), both closed (-177 the simulator class gate, -178 length beside value),
and the third review confirmed them (-179). New gaps named along the way, for section B: `StackLadderIosTest` is held by
no simulator gate; the text length is the whole text's, so opposite differences could cancel; at most 9 of the 12
by-value places occur in the 69 rows.

## What this list is not

It is built by searching the record for the words "not covered", "not shown", "not caught", "owed" and "inferred" and
reading what was found. A gap the record never put into those words is not here. Entries -151 to -153, -155 and -157
to -159 are about the app, not the engine, and were not searched for gaps.
