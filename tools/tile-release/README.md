# BRouter test tiles for Forager-iOS

Routing data files for the tests of Forager-iOS's Kotlin port of BRouter. The tests compare the port against BRouter 1.7.10
on these exact bytes; they are not for navigation.

## Data licence

These files contain OpenStreetMap data, © OpenStreetMap contributors. They are derivative databases of OpenStreetMap,
made available here under the Open Database License (ODbL) 1.0: https://opendatacommons.org/licenses/odbl/1-0/.
See https://www.openstreetmap.org/copyright.

The licence's notices cannot be placed inside an `.rd5` file, so they are given here and in the release notes, as ODbL 1.0
section 4.2(d) provides.

## Files

| Asset | Bytes | SHA-256 | Source |
|---|---:|---|---|
@FILES_TABLE@

- **The four brouter.de tiles** are BRouter routing data files from https://brouter.de/brouter/segments4/, downloaded once
  each between @CAPTURE_FIRST@ and @CAPTURE_LAST@, checked in BRouter 1.7.10, and not altered. brouter.de regenerates
  its files in place, so a later download from it gives different bytes; these copies are kept so the tests always read
  the same ones.
- **`forager-fixture-W75_N40.rd5`** is `app/src/test/resources/routing/segments/W75_N40.rd5` from Forager
  (github.com/slayer8366/Forager, commit e2969abe4), copied unaltered and renamed so it does not clash with the full
  `W75_N40.rd5`. The tests save it as `W75_N40.rd5` in a folder of its own. Its notice in Forager, kept intact:

  > `segments/W75_N40.rd5` and `brouter-cli/*.geojson` contain OpenStreetMap data, © OpenStreetMap contributors, available
  > under the Open Database License (ODbL) 1.0, https://opendatacommons.org/licenses/odbl/1-0/. See
  > https://www.openstreetmap.org/copyright.

  Forager's README there describes it as part of Harriman and Bear Mountain State Parks, New York, built by BRouter 1.7.10's
  map creator.

The sizes and SHA-256 sums above are those in `tile-pins.tsv`, attached, written by the run that captured and checked
the files. The tests check every download against them.
