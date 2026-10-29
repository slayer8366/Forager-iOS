# brouter: BRouter in Kotlin

A Kotlin Multiplatform port of [BRouter](https://github.com/abrensch/brouter) **v1.7.10** (upstream commit `4d2639af`),
for Forager on iOS and, later, Android. BRouter is MIT-licensed; its notice is [`LICENSE`](LICENSE), upstream's file
unchanged.

**Status: scaffold only (dispatch 2026-10-28-01, Phase 2 step 0).** Nothing is ported yet. The public entry point exists as
stubs, which fail with `NotImplementedError`.

## How the paths map to upstream's

Upstream's `btools.<package>.<Class>` becomes `com.zynergylabs.forager.brouter.<package>.<Class>`: the same sub-package and
the same class and file names, under Forager's namespace. For example, `btools/router/RoutingEngine.java` becomes
`com/zynergylabs/forager/brouter/router/RoutingEngine.kt`. Each ported file starts with one line naming its upstream file.
The rename lets the port and the Java engine load in one JVM for the differential tests, and lets Forager's Android app
carry both while it compares them (Phase 1 report, item C).

## What is ported, and what is not

- **Ported, step by step:** tier T2, the code Forager's calls reach, less the four via-point methods (RECORD -24): about
  10,800 Java lines in `util`, `codec`, `expressions`, `mapaccess` and `router`.
- **Not ported:** anything Forager's calls cannot reach. A feature outside T2 fails as "unsupported", never with a made-up
  value.

## The public surface

`route`, `RouteRequest`, `RouteOutcome`, `RouteCancel` and `checkDataFile` (`src/commonMain/.../Route.kt`). Everything else
will be `internal`.

## Tests

- `commonTest` runs on every target, the iOS simulator included.
- `jvmTest` holds the differential tests against the oracle: BRouter 1.7.10's own read-only release jar, fetched by
  `tools/fetch-oracle.sh` and refused unless its size and SHA-256 match the pins.
