# Dispatch 2026-10-28-01, Amendment 1: timing in Phase 2, and the port is for Android too

Written by the Forager-iOS planner, 2026-10-28. RECORD -4, continuing -3. Phase 1 stays read-and-report.
This adds one item (K) and widens one (C). Forager facts below are read at Forager `pre-main`
`e2969abe4f98674c24ce3913ce97be568184a4de`, unchanged since the dispatch.

**Authorisation.** The owner, verbatim, after being told the port will not route better than Java (same
algorithm, identical routes by design), that its speed is unknown on iOS, and that real-phone speed cannot
be measured without an iPhone: "Yes, add timing to Phase 2. But let's do kotlin for Android also".

## C, widened: the port must serve Forager's Android app as well

Forager's Android app will use the Kotlin port instead of the Java engine, once it is proven identical
(ruling a's last step, now confirmed). The switch itself is Forager's work, recorded in Forager's record by
its own planner. It is not part of this dispatch, and nothing in slayer8366/Forager changes here.

Item C's layout must therefore also say how Forager's Android build would take the port in. Forager's
build, as read:
- AGP 9.3.1 (`gradle/libs.versions.toml:2`);
- Kotlin 2.3.10 (`:7`), with KSP 2.3.10 (`:48`);
- `android.builtInKotlin=false` (`gradle.properties:9`);
- Gradle 9.7.0 (`gradle/wrapper/gradle-wrapper.properties:3`);
- today's Java engine is the module `:brouter` (`settings.gradle.kts:21`).

Answer, with a recommendation each:
- **How Forager takes the port in:**
  - a published artifact (where it is published, and how it is pinned);
  - a Gradle composite build;
  - or the Kotlin source vendored at a pinned commit, the way `third_party/brouter/` is today.
- **Which Kotlin version the port may compile with,** so that Forager's Kotlin 2.3.10 compiler can read
  it. A newer compiler's metadata can be unreadable to an older one. Say what you found, read or tested,
  and whether either side has to move.
- **What the port exposes** in place of the seven classes Forager calls today (premise 1 of the
  dispatch). Keep the same names and shapes, or offer a small entry point Forager would wrap behind its
  own `TrailRouter` port.

Anything here that would need a change in slayer8366/Forager now is a stop.

## K, new: the timing plan for Phase 2

Write down, before any timing is run, how Phase 2 will measure the speed of the Kotlin port against the
Java engine. Then Phase 2 runs it. The plan covers:
- **What is timed:** each route of the differential sample (item E), on the JVM, for both the Java engine
  and the Kotlin port on the same inputs, in the same run. Say whether memory is measured too, and how.
- **How:** warm-up, repetitions, run order, which statistic is reported (median and spread, not one run),
  and the machine (CPU, JVM version, the CI runner's label).
- **iOS:** the same routes timed on the iOS simulator in CI. The report must say that simulator speed is
  not phone speed. The simulator runs on a Mac's processor, and the owner has no iPhone ("3 no iPhone",
  RECORD -2), so phone speed stays unmeasured.
- **What counts as a finding:** the planner fixes no pass mark for speed. The owner has not ruled one, and
  identical routes remain the bar. Propose what the report should flag (for example, the port slower than
  Java by more than a stated margin on the JVM) for the owner to rule on.

## Unchanged

Everything else in the dispatch stands: scope, stops, finish line and final message. The final message
gains one line each for C's Android answers and for K.
