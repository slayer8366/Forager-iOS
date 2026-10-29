Item D evidence (part 3, 2026-10-29). Scratch lived in <scratch>/part3/oracle; nothing here is a jar or a tile.
- logs/fetch.log, headers.txt: the release zip fetched from fetch-brouter.sh's URL; size and SHA-256 checked against its pins.
- logs/jars.txt: SHA-256 of the two jars inside the zip, and the -ro jar's manifest.
- tools/compare-classes.sh: class-name sets, class-file major versions, and `javap -c -p` with constant-pool indices removed.
- logs/ro-vs-fresh-build.txt: the -ro jar against the vendored source compiled fresh in part 3
  (javac 21, --release 11 -encoding UTF-8 -g, from /home/user/forager at e2969abe4; logs/fresh-javac.log).
- logs/ro-vs-vendored-build.txt: the same, against part 1's scratch build.
- logs/ro-vs-all.txt: the -ro jar against the -all jar's btools router packages.
- logs/negative-control.txt (+ .diffs.head120.txt, neg-javac.log): the vendored source compiled with
  -XDstringConcat=inline, to show the comparison can report a difference: 43 of 115 classes differ.
- logs/harness-source.log, harness-jar.log: part 1's 03-reach/tools/ForagerCalls.java run on the fresh source build and
  on the -ro jar, same profile files and fixture tile (SHA-256 ff6d9d8e...5ec5990).
