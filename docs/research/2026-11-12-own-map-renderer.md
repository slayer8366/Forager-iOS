# A map renderer of Forager's own: sizing (dispatch 2026-11-12-02)

Researcher: Madrone Gauge. For: Cedar Datum, planner. Status: METHOD AND PREDICTIONS ONLY (no source read yet).

## Method (committed before any source is read)

- Source is Forager `pre-main` at `12eb9697`, a read-only export. Nothing is built. Nothing is written outside
  this report and its evidence folder.
- Marks: [R] read at source, [C] counted by a named command or committed script, [I] inferred, [?] not verified.
- Question 1, styles: one committed script (`evidence/style_inventory.py`, standard library only, run with
  `python3 -I`) walks every style JSON under `map-style/` and every style asset the app ships, and counts layer
  types, paint and layout property names, expression operators (first element of any array whose first element is
  a string, inside a property value or filter), legacy `stops` functions, sources by type, glyph and sprite URLs,
  and the text properties. Operators are separated from plain string arrays (font stacks, anchors) by a fixed list
  of properties whose values are enumerated arrays; the list is in the script.
- Question 1, app: a second script lists every `org.maplibre` import across the map files, with a count of files
  per class, and greps method-call families. Grouping by job is mine and is marked [I].
- Question 2: the style specification's reference JSON (`v8.json`) is fetched from MapLibre's repository and
  set-differenced against question 1 by script. Artifact sizes come from Maven Central's listing or HTTP
  `Content-Length`; an AAR is opened only in the scratchpad if at all, never in a repository.
- Questions 3, 4, 5: at most 12 web pages, each fetched for a stated question, listed in Sources with what was
  asked. A library is named as usable only if its own page was read. What was not fetched is [?].
- Question 7: line counts of comparable code come from repository listings or raw-file `wc -l`, not clones; where
  no count could be had it is [?].
- The planner's counts in RECORD -155 are re-counted, not trusted.
- Corrections and slips are kept in their own section.

## Predictions (before reading)

| # | Question | Prediction |
|---|---|---|
| 1a | Layer types used by shipped styles | 6 of the spec's types: background, fill, line, symbol, raster, hillshade (circle only at run time) |
| 1b | Distinct paint + layout properties used | about 35 |
| 1c | Distinct expression operators used | about 18; legacy `stops` functions also present: yes |
| 1d | Distinct `org.maplibre` classes imported by the map files | about 45 |
| 1e | Text along a line used; variable anchors used | yes; no |
| 2a | Share of the spec's expression operators Forager uses | under 25% |
| 2b | Android SDK AAR size; fetchable from Maven Central | between 6 and 14 MB; yes; arm64 native library 3 to 5 MB |
| 3a | A Kotlin Multiplatform renderer of styled vector tiles that is ready to build on exists | no (raster-tile and wrapper libraries exist) |
| 3b | JVM desktop target can render off-screen in a test | yes |
| 3c | A Skia path route avoids our own triangulation and line stroking | yes |
| 4 | MapLibre Native's build can switch off individual layer types | yes, by compile definitions, but not most of the engine |
| 5 | MapLibre publishes style-plus-expected-image fixtures under a permissive licence | yes (BSD-2-Clause); usable only within a tolerance, not exactly |
| 6 | Things not showable without a phone | the five named in the dispatch, all five; the simulator shows correctness, not speed |
| 7 | Pieces of (a); how many are open-ended | about 12 pieces; 2 open-ended (label placement, text on a line) |
