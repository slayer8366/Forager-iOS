# Step 5b tie-break count (scratch): attributes HashMap.PROBE_TIES deltas to each scenario of ProbeMain, and adds the
# region loads CollectOutreachersTest makes. Each edit must match exactly once.
import sys
p = sys.argv[1]
s = open(p).read()
def rep(a, b):
    global s
    if s.count(a) != 1: raise SystemExit('%d matches for %r' % (s.count(a), a))
    s = s.replace(a, b)
rep("          for (long id : ids) { OsmNode n = new OsmNode((int) (id >> 32), (int) id); n.setHollow(); m.put(n); seeds.add(n); }\n",
    "          long[] tb = ties();\n          for (long id : ids) { OsmNode n = new OsmNode((int) (id >> 32), (int) id); n.setHollow(); m.put(n); seeds.add(n); }\n          tie(out, at, \"known seeding\", tb);\n          tb = ties();\n")
rep("          check(of.createMicroCache(lonIdx, latIdx, buffers, way, null, true, m));\n",
    "          check(of.createMicroCache(lonIdx, latIdx, buffers, way, null, true, m));\n          tie(out, at, \"known decode\", tb);\n")
rep("            check(of.createMicroCache(lonIdx, latIdx, buffers, way, null, true, f));\n",
    "            long[] tf = ties();\n            check(of.createMicroCache(lonIdx, latIdx, buffers, way, null, true, f));\n            tie(out, at, \"fresh mode \" + mode, tf);\n")
rep("            check(of.createMicroCache(lonIdx, latIdx, buffers, way, null, true, whole));\n",
    "            long[] tw = ties();\n            check(of.createMicroCache(lonIdx, latIdx, buffers, way, null, true, whole));\n            tie(out, at, \"whole tile mode 1\", tw);\n")
rep("            String res = route(profile, dir, r);\n",
    "            long[] tr = ties();\n            String res = route(profile, dir, r);\n            tie(out, Probe.where, \"route\", tr);\n")
rep("      } else {\n        File segments = new File(a[3]);",
    "      } else if (a[0].equals(\"regions\")) {\n        way = wayContext(oracleDir, profile);\n        for (int i = 3; i < a.length; i++) regions(a[i].substring(0, a[i].indexOf('=')), new File(a[i].substring(a[i].indexOf('=') + 1)), out);\n      } else {\n        File segments = new File(a[3]);")
rep("      out.println(\"## counters (zero unless the shadowed classes are first on the classpath)\");\n",
    "      out.println(\"## tieBreakOrder totals by scenario (OsmNode keys: treeify, putTreeVal; other keys: treeify, putTreeVal)\");\n      for (Map.Entry<String, long[]> e : tieTotals.entrySet()) out.println(\"# total \" + e.getKey() + \" \" + Arrays.toString(e.getValue()));\n      out.println(\"# grand total \" + Arrays.toString(ties()));\n      out.println(\"## counters (zero unless the shadowed classes are first on the classpath)\");\n")
rep("  static int[] corner(String name) {",
    """  static final TreeMap<String, long[]> tieTotals = new TreeMap<>();

  /** java.util.HashMap.PROBE_TIES, from the instrumented HashMap patched into java.base; refuses to run without it. */
  static long[] ties() throws Exception {
    return ((long[]) java.util.HashMap.class.getField("PROBE_TIES").get(null)).clone();
  }

  static void tie(PrintWriter out, String at, String scenario, long[] before) throws Exception {
    long[] now = ties();
    long[] d = new long[4];
    for (int i = 0; i < 4; i++) d[i] = now[i] - before[i];
    long[] t = tieTotals.computeIfAbsent(scenario.startsWith("fresh") || scenario.startsWith("known") || scenario.startsWith("whole") ? scenario : scenario, k -> new long[4]);
    for (int i = 0; i < 4; i++) t[i] += d[i];
    if (d[0] + d[1] + d[2] + d[3] != 0) out.println("# tie " + at + " | " + scenario + " | OsmNode treeify " + d[0] + " putTreeVal " + d[1] + " | other " + d[2] + " " + d[3]);
  }

  /** CollectOutreachersTest.forEachRegion's oracle loads: the first degree square with data, its largest cache (the first
   *  of equal sizes, as Kotlin's maxBy), the 3 by 3 block around it in row order, loaded into one fresh map per mode. */
  static void regions(String label, File file, PrintWriter out) throws Exception {
    int[] c0 = corner(file.getName());
    PhysicalFile pf = new PhysicalFile(file, new DataBuffers(), -1, -1);
    try {
      for (int tileIndex = 0; tileIndex < 25; tileIndex++) {
        int lonDegree = c0[0] + tileIndex / 5, latDegree = c0[1] + tileIndex % 5;
        OsmFile of = new OsmFile(pf, lonDegree, latDegree, new DataBuffers());
        if (!of.hasData()) continue;
        int divisor = pf.divisor;
        byte[] buf = new byte[1 << 16];
        Map<Long, int[]> byIdx = new HashMap<>();
        int[] centre = null;
        for (int sub = 0; sub < divisor * divisor; sub++) {
          int size = of.getDataInputForSubIdx(sub, buf);
          if (size == 0) continue;
          if (size > buf.length) { buf = new byte[size]; size = of.getDataInputForSubIdx(sub, buf); }
          int[] c = {divisor * lonDegree + sub % divisor, divisor * latDegree + sub / divisor, size, sub};
          byIdx.put(((long) c[0] << 32) | (c[1] & 0xffffffffL), c);
          if (centre == null || size > centre[2]) centre = c;
        }
        List<int[]> cells = new ArrayList<>();
        for (int dy = -1; dy <= 1; dy++) for (int dx = -1; dx <= 1; dx++) {
          int[] c = byIdx.get(((long) (centre[0] + dx) << 32) | ((centre[1] + dy) & 0xffffffffL));
          if (c != null) cells.add(c);
        }
        for (int mode = 0; mode <= 2; mode++) {
          OsmNodesMap om = new OsmNodesMap();
          om.cleanupMode = mode;
          DataBuffers ob = new DataBuffers();
          long[] tb = ties();
          for (int[] c : cells) of.createMicroCache(c[0], c[1], ob, way, null, true, om);
          String at = label + " (" + lonDegree + "," + latDegree + ") region around sub " + centre[3] + " (" + centre[2] + " bytes), " + cells.size() + " caches";
          tie(out, at, "region mode " + mode, tb);
          out.println("region " + at + " mode " + mode + " loaded");
        }
        return;
      }
    } finally {
      pf.close();
    }
  }

  static int[] corner(String name) {""")
open(p, 'w').write(s)
print('patched')
