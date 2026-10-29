Part 7 (Hazel, 2026-10-29): H step 3 (hash order), step 4 (simulator test design), and Android's Math.cos / Math.atan2.

00-bases-part7.txt     bases at the start of part 7
sources.txt            every page or file read from outside the repositories, with URL, date and SHA-256
outreach-recount.txt   part 6's collectOutreachers reduction (OutreachSim), recounted from part6/results/*/hash.txt
TreeBinOrder.java      HashMap(4096) over BRouter's own OsmNode: twelve keys with one ilon + ilat, 2,000 spread keys
treebin-order.txt      its output, three JVMs x five builds

Reproduce TreeBinOrder:
  javac -d <classes> $(find <Forager clone>/third_party/brouter/src/main/java -name '*.java')
  javac -cp <classes> -d <out> TreeBinOrder.java
  java -cp <classes>:<out> TreeBinOrder 5
