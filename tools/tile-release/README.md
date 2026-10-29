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
| `W75_N40.rd5` | 119750873 | `dd141c3ed1d11036373175dd1242d7a64bf101e11523a270efcb7371599d29e6` | brouter.de, Last-Modified Thu, 29 Oct 2026 01:03:01 GMT |
| `W75_N35.rd5` | 3571019 | `73d58fa5d4a4e5d4c9fef3b01af003d75489f0316169901259fb5daef4c6a721` | brouter.de, Last-Modified Thu, 29 Oct 2026 01:03:01 GMT |
| `W70_N40.rd5` | 5292557 | `c35113bb5448216b60e4805dc899580b6661b4bbe8155c9e174da562a3e0521c` | brouter.de, Last-Modified Thu, 29 Oct 2026 01:03:01 GMT |
| `E170_S50.rd5` | 1061589 | `878455e284d0e8636e22159f1081e658fe2131b35f3a5e12f2873036b2acd584` | brouter.de, Last-Modified Thu, 29 Oct 2026 01:03:01 GMT |
| `forager-fixture-W75_N40.rd5` | 152665 | `ff6d9d8e439c3cdf914b1566619f64466d9af993c28070ea893425e7f5ec5990` | Forager's test fixture (below) |

- **The four brouter.de tiles** are BRouter routing data files from https://brouter.de/brouter/segments4/, downloaded once
  each on 2026-10-29 between 20:09:48Z and 20:09:58Z and not altered. brouter.de regenerates its files in place, so a
  download from it today gives different bytes; these copies are kept so the tests always read the same ones.
- **`forager-fixture-W75_N40.rd5`** is `app/src/test/resources/routing/segments/W75_N40.rd5` from Forager
  (github.com/slayer8366/Forager, commit e2969abe4), copied unaltered and renamed so it does not clash with the full
  `W75_N40.rd5`. The tests save it as `W75_N40.rd5` in a folder of its own. Its notice in Forager, kept intact:

  > `segments/W75_N40.rd5` and `brouter-cli/*.geojson` contain OpenStreetMap data, © OpenStreetMap contributors, available
  > under the Open Database License (ODbL) 1.0, https://opendatacommons.org/licenses/odbl/1-0/. See
  > https://www.openstreetmap.org/copyright.

  Forager's README there describes it as part of Harriman and Bear Mountain State Parks, New York, built by BRouter 1.7.10's
  map creator.

The sizes and SHA-256 sums above are copied from `tools/tile-pins.tsv` in Forager-iOS, which the tests check every
download against.
