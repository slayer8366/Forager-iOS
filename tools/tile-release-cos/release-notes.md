Test data for the BRouter port's tests: five BRouter routing data files captured from brouter.de on @CAPTURE_DATE@ (UTC),
for the eight latitude rows the first tile release does not reach. Each is pinned by size and SHA-256 in
`tile-pins.tsv`, attached. Not for navigation.

**Data licence.** These files contain OpenStreetMap data, © OpenStreetMap contributors. They are derivative databases of
OpenStreetMap, made available here under the Open Database License (ODbL) 1.0:
https://opendatacommons.org/licenses/odbl/1-0/. See https://www.openstreetmap.org/copyright. The licence's notices cannot
be placed inside an `.rd5` file, so they are given here, as ODbL 1.0 section 4.2(d) provides.

## Files

| Asset | Bytes | SHA-256 | Source |
|---|---:|---|---|
@FILES_TABLE@

The five files are BRouter routing data files from https://brouter.de/brouter/segments4/, downloaded once each between
@CAPTURE_FIRST@ and @CAPTURE_LAST@, checked in BRouter 1.7.10, and not altered. brouter.de regenerates its files in
place, so a later download from it gives different bytes; these copies are kept so the tests always read the same ones.
The check counted each file's nodes in the rows it is for and routed once in each of those rows; its counts are the
`#` lines at the end of `tile-pins.tsv`.
