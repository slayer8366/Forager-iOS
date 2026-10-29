Superseded runs, kept because they show a defect in the handoff's harness (step1-handoff/TileCheck.java.txt).

check-*.log here ran the handoff's count loop, which passed MicroCache.debug as reallyDecode, as checkFileIntegrity
does (PhysicalFile.java:79 at v1.7.10). MicroCache.debug is false unless PhysicalFile.main sets it, and with
reallyDecode false OsmFile.createMicroCache CRC-checks a cell's bytes and returns null for every non-empty cell
(OsmFile.java:133-148). So the count read non-empty=0, nodes=0 on every tile, including F0's fixture, which routes.
It was caught by that: a known-good tile reading zero. The harness in ../TileCheck.java.txt decodes (reallyDecode
true, as PhysicalFile.main runs it). The open, checkFileIntegrity and route lines are the same in both runs.

bite-harness-firstfix.log ran the same superseded harness on the damaged copies; every case failed before the count.
